# StreamBase — Architecture Deep Dive

> Technical breakdown of every architectural layer — how the pieces connect, why they were designed this way, and what happens when things go wrong.

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [API Layer — Load Balancing](#2-api-layer--load-balancing)
3. [Data Layer — PostgreSQL](#3-data-layer--postgresql)
4. [Caching Layer — Redis](#4-caching-layer--redis)
5. [Async Pipeline — Kafka](#5-async-pipeline--kafka)
6. [Real-Time Layer — WebSocket + Redis Pub/Sub](#6-real-time-layer--websocket--redis-pubsub)
7. [Feed System — Fan-Out Architecture](#7-feed-system--fan-out-architecture)
8. [Sharding — Consistent Hashing](#8-sharding--consistent-hashing)
9. [Search — Elasticsearch](#9-search--elasticsearch)
10. [Resilience — Circuit Breaker + Failover](#10-resilience--circuit-breaker--failover)
11. [Rate Limiting](#11-rate-limiting)
12. [Database Schema](#12-database-schema)
13. [Kafka Topics Map](#13-kafka-topics-map)
14. [Docker Services Map](#14-docker-services-map)

---

## 1. System Overview

```
                         ┌──────────────────────────────────────────────────┐
                         │              StreamBase System                   │
                         │                                                  │
  ┌────────┐             │  ┌──────────┐    ┌───────┐   ┌───────┐ ┌───────┐ │
  │ Client │──HTTP/WS───►│  │  Nginx   │───►│ app-1 │   │ app-2 │ │ app-3 │ │
  └────────┘             │  │    LB    │    └───┬───┘   └───┬───┘ └───┬───┘ │
                         │  └──────────┘        └───────────┴─────────┘     │
                         │                                  │               │
                         │          ┌──────────┬────────────┤               │
                         │          │          │            │               │
                         │   ┌──────▼──┐  ┌────▼──────┐  ┌──▼───────────┐   │
                         │   │  Redis  │  │PostgreSQL │  │    Kafka     │   │
                         │   └─────────┘  └───────────┘  └──────────────┘   │
                         │                                                  │
                         │                    ┌─────────────────────┐       │
                         │                    │    Elasticsearch    │       │
                         │                    └─────────────────────┘       │
                         └──────────────────────────────────────────────────┘
```

**Request flow:**
1. Client hits `http://localhost:80` → Nginx
2. Nginx load balances across `app-1`, `app-2`, `app-3`
3. App instance handles request — reads/writes to PostgreSQL, Redis, Kafka, Elasticsearch as needed
4. Async events (upload processing, notifications, feed fan-out, search indexing) flow through Kafka consumers

---

## 2. API Layer — Load Balancing

### Nginx Configuration

Two strategies configured in `nginx.conf`:

```nginx
upstream streambase {
    least_conn;           # or round_robin (default)
    server app-1:8080;
    server app-2:8080;
    server app-3:8080;
}
```

| Strategy | Behavior | Best For |
|---|---|---|
| `round_robin` | Requests distributed 1-2-3-1-2-3 | Uniform request sizes |
| `least_conn` | Routes to instance with fewest active connections | Mixed request sizes |

**Why stateless matters:** All 3 app instances must be identical and share no local state. Any shared state (cache, sessions, rate limit counters) lives in Redis — not in the app instances. This is what makes horizontal scaling work.

### Health Check

`GET /health` returns the container hostname (`app-1`, `app-2`, or `app-3`). Used to verify distribution across instances.

---

## 3. Data Layer — PostgreSQL

### Primary + Replica Routing

```
Write operations ──────────────────► postgres-primary (port 5432)
                                           │
                                     WAL streaming
                                           │
Read operations  ──────────────────► postgres-replica (port 5433)
```

**Implementation:** Custom `RoutingDataSource` extends Spring's `AbstractRoutingDataSource`. Determines which datasource to use based on transaction type:

```java
@Override
protected Object determineCurrentLookupKey() {
    return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
        ? "replica" : "primary";
}
```

Mark read-only services with `@Transactional(readOnly = true)` → automatically routes to replica.

### Replica Failover

`ReplicaHealthMonitor` runs a background thread every 10 seconds:
- Pings replica with `SELECT 1`
- On failure: switches all reads to primary
- On recovery: switches reads back to replica

**Replication lag:** PostgreSQL streaming replication is asynchronous. A write to primary may not be immediately visible on replica. For strong consistency (e.g. "read your own write"), read from primary explicitly.

### Sharding (3 Additional PostgreSQL Instances)

Separate from the primary/replica setup — 3 dedicated shard instances (`shard-1`, `shard-2`, `shard-3`) for the sharded video table. See [Section 8](#8-sharding--consistent-hashing).

---

## 4. Caching Layer — Redis

Redis serves 4 distinct roles in StreamBase:

```
┌────────────────────────────────────────────────────┐
│                      Redis                         │
│                                                    │
│  ┌─────────────────┐   ┌────────────────────────┐  │
│  │  Cache          │   │  Pub/Sub               │  │
│  │  video:{id}     │   │  comments:{videoId}    │  │
│  │  link:{code}    │   │  (cross-server WS)     │  │
│  │  comments:{id}  │   └────────────────────────┘  │
│  └─────────────────┘                               │
│  ┌─────────────────┐   ┌────────────────────────┐  │
│  │  Rate Limiting  │   │  Feed / Presence       │  │
│  │  rate:{ip}      │   │  feed:{userId} (ZSET)  │  │
│  │  rate:{userId}  │   │  viewers:{videoId}     │  │
│  │  clicks:{code}  │   │  (ZSET)                │  │
│  └─────────────────┘   └────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### Cache-Aside Pattern

```
GET /api/videos/{id}
    │
    ▼
Check Redis key "video:{id}"
    │
    ├── HIT  → return cached value (TTL: 5 min)
    │
    └── MISS → query PostgreSQL
                    │
                    ▼
              populate Redis key
                    │
                    ▼
              return value

PUT/DELETE /api/videos/{id}
    │
    ▼
Update/Delete in PostgreSQL
    │
    ▼
Delete Redis key "video:{id}"   ← invalidation, not update
```

**Why delete on update, not update?** Simpler, safer. Updating means the cache write could fail after the DB write — leaving stale data indefinitely. Deleting means the next read will always fetch fresh from DB.

---

## 5. Async Pipeline — Kafka

### Event Flow

```
POST /api/videos
      │
      ▼
Save to PostgreSQL (status=UPLOADED)
      │
      ▼
Publish to Kafka: video.uploaded
      │
      └──► VideoProcessor consumer
                │
                ▼
          status = PROCESSING
          (simulate thumbnail gen: sleep 3s)
          (simulate metadata extraction)
                │
                ▼
          status = READY
                │
                ▼
          Publish: video.published
                │
                ├──► NotificationFanOutConsumer
                │           │
                │     ┌─────┴────────┬──────────────┐
                │     ▼              ▼              ▼
                │  notification   notification  notification
                │  .email         .push         .in_app
                │
                ├──► FeedFanOutConsumer
                │     └──► ZADD feed:{subscriberId} for each follower
                │
                └──► SearchIndexConsumer
                      └──► Index into Elasticsearch
```

### Consumer Groups

Each consumer has its own group ID — they all receive every `video.published` event independently:

| Consumer | Group ID | Action |
|---|---|---|
| `VideoProcessor` | `video-processor` | Thumbnail + metadata simulation |
| `NotificationFanOutConsumer` | `notification-fanout` | Fan-out to 3 channel topics |
| `FeedFanOutConsumer` | `feed-fanout` | Write to follower Redis ZSETs |
| `SearchIndexConsumer` | `search-indexer` | Index into Elasticsearch |

### Fault Tolerance

- **Retry:** `DefaultErrorHandler` with exponential backoff — 3 attempts before giving up
- **Dead Letter:** Failed messages → `*.DLT` topic via `DeadLetterPublishingRecoverer`
- **Replay:** Consumer offset tracked by Kafka. Crash mid-processing → restart picks up from last committed offset

### KRaft Mode

No ZooKeeper. Kafka manages its own metadata internally. Simpler setup, closer to modern production deployments.

---

## 6. Real-Time Layer — WebSocket + Redis Pub/Sub

### Single Server (Trivial)

```
User A ──WS──► app-1 ──► broadcast to local sessions watching videoId
User B ──WS──► app-1 ──► receives broadcast
```

### Multi-Server Problem

```
User A ──WS──► app-1     User B ──WS──► app-3
                │                           │
         local broadcast              local broadcast
                │                           │
         User A sees it             User B sees it
                ✗                           ✗
         User B does NOT             User A does NOT
         (on app-3)                  (on app-1)
```

### Fix — Redis Pub/Sub

```
User A sends comment to app-1
      │
      ▼
app-1: save to PostgreSQL + PUBLISH "comments:{videoId}" payload
                                         │
                    ┌────────────────────┼────────────────────┐
                    ▼                    ▼                    ▼
                  app-1               app-2               app-3
             (subscribed)         (subscribed)         (subscribed)
                  │                    │                    │
           push to local          push to local        push to local
           WS sessions            WS sessions          WS sessions
```

Each app subscribes to `PatternTopic("comments:*")` on startup. When any app publishes, all 3 receive it and push to their own local WebSocket clients.

### Presence Tracking

```
On WS connect:    ZADD viewers:{videoId} {epochMs} {userId}
On WS disconnect: ZREM viewers:{videoId} {userId}
Stale cleanup:    ZREMRANGEBYSCORE viewers:{videoId} 0 {epochMs - 30000}
Viewer count:     ZCARD viewers:{videoId}  →  pushed over WS (not polled)
```

### Comment Cache

```
On new comment:   LPUSH comments:{videoId} {payload}
                  LTRIM comments:{videoId} 0 49        ← keep last 50 only
On WS connect:    LRANGE comments:{videoId} 0 49       ← send history to new viewer
```

---

## 7. Feed System — Fan-Out Architecture

### Pull Model

```sql
SELECT v.* FROM videos v
JOIN subscriptions s ON v.uploaded_by = s.creator_id
WHERE s.subscriber_id = :userId
ORDER BY v.created_at DESC
LIMIT 20
```

Warm latency: **7–12ms** (composite index on `creator_id, created_at`). Scales as O(follows × videos_per_creator) — expensive for users following many active creators.

### Push Model (Fan-Out on Write)

```
video.published event
      │
      ▼
FeedFanOutConsumer
      │
      ▼
SELECT subscriber_id FROM subscriptions WHERE creator_id = ?
      │
      ▼
For each subscriber:
  ZADD feed:{subscriberId} {timestamp} {videoId}

GET /api/feed?userId=X
      │
      ▼
ZREVRANGE feed:{userId} 0 19   ← O(1), no joins
```

### Hybrid Model (What's Actually Built)

```
On video.published:
      │
      ▼
CreatorTierService.isCelebrity(creatorId)?
      │
      ├── NO (< 10K followers)
      │      └──► FeedFanOutConsumer writes to all follower ZSETs
      │
      └── YES (≥ 10K followers)
             └──► Skip fan-out entirely

GET /api/feed?userId=X
      │
      ▼
1. ZREVRANGE feed:{userId}           ← push feed (regular creators)
2. SELECT from videos WHERE uploaded_by IN (celebrity_ids the user follows)
3. Merge + sort by timestamp
4. Return top 20
```

Cost per feed read is bounded regardless of who you follow — celebrity creators are always pulled, regular creators are always pushed.

---

## 8. Sharding — Consistent Hashing

### The Problem with Modular Hashing

```
2 shards:  videoId % 2  →  shard-1 or shard-2
Add shard: videoId % 3  →  ~67% of keys map to different shards
                            → massive data migration required
```

### Consistent Hash Ring

```
         0
        /  \
  shard-1   shard-2
  (virtual  (virtual
   nodes)    nodes)
        \  /
        360

Each shard gets 150 virtual nodes spread around the ring.
getShard(videoId):
  hash = MD5(videoId)
  return ring.ceilingKey(hash)   ← O(log N) via TreeMap
```

### Adding a Shard

```
Before (2 shards):     After (3 shards):
shard-1: 122 videos    shard-1: 79 videos
shard-2: 141 videos    shard-2: 107 videos
                       shard-3: 77 videos

Keys moved: 29.3%  (theoretical: 33.3%)  ✅
```

### Snowflake ID Generator

```
┌──────────────────┬──────────────┬──────────────────┐
│  Timestamp 41b   │ Machine ID   │   Sequence 12b   │
│  (ms since epoch)│    10b       │  (per-ms counter)│
└──────────────────┴──────────────┴──────────────────┘
```

Globally unique IDs across all shards — no central coordinator needed.

### ShardRouter

```java
String shard = consistentHashRing.getShard(videoId);
JdbcTemplate template = shardJdbcTemplates.get(shard);
template.update("INSERT INTO videos ...");
```

Each shard has its own `JdbcTemplate`. The router picks the right one based on the consistent hash.

---

## 9. Search — Elasticsearch

### Index Structure

```
VideoSearchDocument {
    id:           keyword
    title:        text (standard analyzer)
                  + title.autocomplete (edge_ngram analyzer)
    description:  text
    uploadedBy:   keyword
    createdAt:    date
}
```

### Autocomplete Analyzer

```json
{
  "analyzer": {
    "autocomplete": {
      "tokenizer": "autocomplete_tokenizer",
      "filter": ["lowercase"]
    }
  },
  "tokenizer": {
    "autocomplete_tokenizer": {
      "type": "edge_ngram",
      "min_gram": 2,
      "max_gram": 20,
      "token_chars": ["letter", "digit"]
    }
  }
}
```

`cook` → tokens: `co`, `coo`, `cook` → matches `cooking`, `cookbook`, `cookware`

### Search Query

```json
{
  "multi_match": {
    "query": "user input",
    "fields": ["title^3", "description"],
    "fuzziness": "AUTO"
  }
}
```

`title^3` means title matches are ranked 3× higher than description matches. `AUTO` fuzziness handles typos (`coking` → `cooking`).

### Indexing Pipeline

```
video.published (Kafka)
      │
      ▼
SearchIndexConsumer (groupId: search-indexer)
      │
      ▼
Elasticsearch index: videos
```

Decoupled from the API — search index is eventually consistent with PostgreSQL. Failure mode: if Elasticsearch is down, search returns 503. No SQL fallback (by design — ILIKE is not a substitute for full-text search features).

---

## 10. Resilience — Circuit Breaker + Failover

### Redis Failure (Worst Case)

```
Normal:  API ──► Redis (cache hit) ──► return
         API ──► Redis (miss) ──► PostgreSQL ──► return

Redis down (without fix):
         API ──► Redis (connection hangs) ──► 504 timeout

Redis down (with fix — 3 layers):
Layer 1: 500ms connection timeout ──► fail fast
Layer 2: CacheErrorHandler ──► on any cache error, fall back to PostgreSQL
Layer 3: Resilience4j @CircuitBreaker on LinkCacheService
           ├── CLOSED: normal operation
           ├── OPEN (>50% failures): skip Redis entirely, go to PostgreSQL
           └── HALF-OPEN: test Redis with 1 request, close if successful
```

### Read Replica Failure

```
ReplicaHealthMonitor (background thread, every 10s):
      │
      ├── Replica healthy  → reads go to replica
      └── Replica down     → reads go to primary (automatic)
                                   │
                                   └── Replica recovers → reads go back to replica
```

### Kafka Failure

```
POST /api/videos:
  videoRepository.save()    ← DB write
  kafkaTemplate.send()      ← Kafka publish
  (both inside @Transactional)

Kafka down:
  kafkaTemplate.send() throws
  @Transactional rolls back
  videoRepository.save() is undone
  No orphan rows in DB ✅
  Upload returns error to client
```

Known trade-off: Kafka is a hard dependency for uploads. Transactional Outbox pattern (deferred) would decouple this.

---

## 11. Rate Limiting

### Architecture

```
HTTP Request
      │
      ▼
RateLimiterInterceptor (preHandle)
      │
      ▼
Determine limit key:
  General endpoints  → "rate:ip:{ip}"         (100 req/min)
  POST /api/videos   → "rate:upload:{userId}"  (10 req/hour)
      │
      ▼
Redis Lua script (atomic):
  count = INCR key
  if count == 1: EXPIRE key window_seconds
  return count
      │
      ├── count ≤ limit  → proceed
      └── count > limit  → HTTP 429 + Retry-After header
```

### Why Lua Script?

Three app instances share one Redis. Without atomicity:
- Instance A reads count = 99
- Instance B reads count = 99
- Both increment → count = 100 (should be 101)
- Both allow the request → limit bypassed

Lua scripts execute atomically in Redis — no concurrent execution possible.

---

## 12. Database Schema

```sql
-- Core video table (primary + replica)
videos (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR NOT NULL,
    description TEXT,
    url         VARCHAR NOT NULL,
    uploaded_by VARCHAR NOT NULL,
    status      VARCHAR DEFAULT 'UPLOADED',  -- UPLOADED | PROCESSING | READY
    created_at  TIMESTAMP DEFAULT NOW()
)

-- Short links
links (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR UNIQUE NOT NULL,     -- base62 encoded
    video_id    BIGINT REFERENCES videos(id),
    click_count BIGINT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT NOW()
)

-- Subscriptions (reused for both notifications and feed)
subscriptions (
    id           BIGSERIAL PRIMARY KEY,
    subscriber_id VARCHAR NOT NULL,
    creator_id    VARCHAR NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW(),
    UNIQUE (subscriber_id, creator_id)
)

-- Notifications
notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR NOT NULL,
    message     TEXT NOT NULL,
    type        VARCHAR NOT NULL,           -- EMAIL | PUSH | IN_APP
    video_id    BIGINT,
    read        BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT NOW()
)

-- Comments
comments (
    id          BIGSERIAL PRIMARY KEY,
    video_id    BIGINT NOT NULL,
    user_id     VARCHAR NOT NULL,
    text        TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
)

-- Shard tables (identical schema on shard-1, shard-2, shard-3)
videos_sharded (
    id          BIGINT PRIMARY KEY,         -- Snowflake ID
    title       VARCHAR NOT NULL,
    url         VARCHAR NOT NULL,
    uploaded_by VARCHAR NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
)
```

---

## 13. Kafka Topics Map

| Topic | Producer | Consumer(s) | Purpose |
|---|---|---|---|
| `video.uploaded` | `VideoService` | `VideoProcessor` | Trigger async processing |
| `video.published` | `VideoProcessor` | `NotificationFanOutConsumer`, `FeedFanOutConsumer`, `SearchIndexConsumer` | Trigger all downstream reactions |
| `notification.email` | `NotificationFanOutConsumer` | `EmailNotificationConsumer` | Email delivery |
| `notification.push` | `NotificationFanOutConsumer` | `PushNotificationConsumer` | Push delivery |
| `notification.in_app` | `NotificationFanOutConsumer` | `InAppNotificationConsumer` | In-app delivery |
| `feed-fanout` | `VideoService` | `FeedFanOutConsumer` | Write to follower feed ZSETs |
| `video.uploaded.DLT` | Spring DLQ | (manual inspection) | Failed video processing jobs |
| `notification.dlt` | Spring DLQ | (manual inspection) | Failed notification deliveries |

---

## 14. Docker Services Map

| Service | Image | Port | Role |
|---|---|---|---|
| `nginx` | nginx:1.29.8-alpine | 80 | Load balancer |
| `app-1` | custom build | 5005 (debug) | Spring Boot instance 1 |
| `app-2` | custom build | — | Spring Boot instance 2 |
| `app-3` | custom build | — | Spring Boot instance 3 |
| `postgres-primary` | postgres:18.3-alpine | 5432 | Primary DB (writes) |
| `postgres-replica` | postgres:18.3-alpine | 5433 | Read replica |
| `shard-1` | postgres:18.3-alpine | 5434 | DB shard 1 |
| `shard-2` | postgres:18.3-alpine | 5435 | DB shard 2 |
| `shard-3` | postgres:18.3-alpine | 5436 | DB shard 3 |
| `redis` | redis:8.6.2-alpine | 6379 | Cache + Pub/Sub + Rate limiting |
| `kafka` | apache/kafka:4.3.0 | 9092 | Event streaming (KRaft mode) |
| `elasticsearch` | elasticsearch:9.4.2 | 9200 | Full-text search |

**Total: 13 services — entire production-like infrastructure running locally in one `docker compose up`.**

---

---

## 15. Interactive Codebase Graph

An interactive knowledge graph of the entire codebase is available locally:

```bash
open graphify-out/graph.html
```

288 nodes · 339 edges · 49 communities. Shows how every class, config, and concept connects across the project. Click any node to see its relationships.

---

*Built by Kavinkumar S · [LinkedIn](https://linkedin.com/in/contactkavinkumar)*
