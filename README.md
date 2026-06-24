<div align="center">

# 🎬 StreamBase

### A production-grade Netflix-like video streaming platform
### built hands-on to master system design concepts

<br/>

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

<br/>

> Every feature built here maps directly to a real system design interview question.
> **16 days · 10+ concepts · 13 Docker services · all running locally.**

</div>

📖 **[BUILD_LOG.md](./BUILD_LOG.md)** — Full engineering log: every decision, trade-off, failure, and lesson learned across all 16 days.
🏗️ **[ARCHITECTURE.md](./ARCHITECTURE.md)** — Deep dive: every layer with flow diagrams, DB schema, Kafka topics map, and Docker services.

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│   CLIENT                                                                 │
│     │                                                                    │
│     ▼                                                                    │
│  ┌──────────────────┐                                                    │
│  │   Nginx (LB)     │  ← Round-Robin / Least-Conn                        │
│  └─────┬────┬───────┘                                                    │
│        │    │                                                            │
│   ┌────▼┐ ┌─▼────┐ ┌──────┐                                              │
│   │app-1│ │app-2 │ │app-3 │   Spring Boot instances                      │
│   └──┬──┘ └──┬───┘ └──┬───┘                                              │
│      └───────┴────────┘                                                  │
│                  │                                                       │
│        ┌─────────┼───────────────────────────────────┐                   │
│        │         │                                   │                   │
│   ┌────▼─────┐  ┌▼─────────────────┐   ┌─────────────▼──────────┐        │
│   │  Redis   │  │   PostgreSQL     │   │        Kafka           │        │
│   │          │  │                  │   │                        │        │
│   │ • Cache  │  │ Primary (writes) │   │ • video.uploaded       │        │
│   │ • Pub/Sub│  │ Replica  (reads) │   │ • video.published      │        │
│   │ • Rate   │  │ Shard-1  (hash)  │   │ • notification.*       │        │
│   │   Limit  │  │ Shard-2  (hash)  │   │ • feed-fanout          │        │
│   │ • ZSET   │  │ Shard-3  (hash)  │   │ • *.DLT (dead letter)  │        │
│   └────▲─────┘  └──────────────────┘   └────────────────────────┘        │
│        │                                                                 │
│   ┌────┴──────────────────────────────────────────────┐                  │
│   │              WebSocket (Live Comments)            │                  │
│   │   app-1 ◄──── Redis Pub/Sub ────► app-2, app-3    │                  │
│   └───────────────────────────────────────────────────┘                  │ 
│                                                                          │
│                      ┌─────────────────────┐                             │
│                      │   Elasticsearch     │                             │
│                      │  Full-text search   │                             │
│                      │  + Autocomplete     │                             │
│                      └─────────────────────┘                             │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## ⚡ Features Built

| Day | Feature | System Design Concept | Key Classes |
|:---:|---------|----------------------|-------------|
| 1 | Video CRUD API | REST API design | `VideoController` · `VideoService` |
| 2 | Load Balancing | Horizontal scaling | `nginx.conf` · 3 app instances |
| 3 | Redis Caching | Cache-aside · TTL · invalidation | `CacheConfig` · `VideoService` |
| 4 | Read Replicas | DB replication · failover | `RoutingDataSource` · `ReplicaHealthMonitor` |
| 5 | Kafka Pipeline | Async messaging · DLQ · replay | `VideoProcesscor` · `KafkaConfig` |
| 6–7 | Short Links | URL shortening · base62 · hot cache | `LinkController` · `LinkCacheService` |
| 8 | Rate Limiter | Sliding window · Redis Lua script | `RateLimiterInterceptor` |
| 9 | Notifications | Kafka fan-out · retry · dead letter | `NotificationFanOutConsumer` |
| 10 | Chaos Day | Failure modes · circuit breaker | `Resilience4jConfig` · `CacheConfig` |
| 11–13 | Live Comments | WebSocket · multi-server broadcast | `CommentWebSocketHandler` · `CommentBroadcastListener` |
| 14 | Subscription Feed | Fan-out read vs write · celebrity problem | `FeedService` · `CreatorTierService` |
| 15 | DB Sharding | Consistent hashing · rebalancing | `ConsistentHashRing` · `ShardRouter` · `SnowFlakeIdGenerator` |
| 16 | Full-Text Search | Inverted index · autocomplete · fuzziness | `SearchController` · `SearchIndexConsumer` |

---

## 🧠 Key Design Decisions

<details>
<summary><b>1. Redis Pub/Sub for WebSocket broadcast (not Kafka)</b></summary>

When User A on `app-1` sends a comment, User B on `app-3` must see it instantly. Sticky sessions would break horizontal scaling. Kafka would work, but it's overkill here.

```
User A (app-1) ──► publish "comments:videoId" ──► Redis Pub/Sub
                                                        │
                              ┌─────────────────────────┤
                              ▼                         ▼
                           app-1                     app-3
                    (push to local WS)        (push to local WS)
```

**Why Redis Pub/Sub over Kafka:**
- Comments already persisted in PostgreSQL — Kafka's disk durability is wasted
- Redis latency: **~1ms** vs Kafka's **10–50ms**
- Kafka needs 3 separate consumer groups (one per app instance) — unnecessary overhead
</details>

<details>
<summary><b>2. Consistent Hashing for DB Sharding (not modular hashing)</b></summary>

`id % N` remaps **~75% of keys** when adding a shard. Consistent hashing with virtual nodes remaps only **~1/N keys**.

**Observed results:**
- 2 shards → distribution: `122 / 141` (46% / 54%) ✅
- Added shard-3 → keys moved: **29.3%** (theoretical: 33.3%) ✅
- New distribution: `79 / 107 / 77` ✅

Uses **Snowflake IDs** (timestamp + machine ID + sequence) for globally unique keys without a central coordinator.
</details>

<details>
<summary><b>3. Hybrid Feed — Fan-Out on Write + Read</b></summary>

Pure fan-out on write fails for celebrities: **1 upload × 10M followers = 10M Redis writes** → "Bieber Problem."

| Creator Type | Strategy | Why |
|---|---|---|
| Regular (< 10K followers) | Fan-out on **write** → Redis ZSET per follower | Fast reads, bounded writes |
| Celebrity (≥ 10K followers) | Skip fan-out → **pull** at read time | Unbounded fan-out avoided |

`GET /api/feed` merges both sources at read time — bounded cost per request regardless of creator size.
</details>

<details>
<summary><b>4. Circuit Breaker on Redis (Chaos Day discovery)</b></summary>

When Redis died, the API returned **504 Gateway Timeout** — the worst failure found on Chaos Day.

**Fix layered in 3 levels:**
1. `500ms` connection timeout on Redis
2. `CacheErrorHandler` on `@Cacheable` → falls back to PostgreSQL on cache miss
3. Resilience4j `@CircuitBreaker` on `LinkCacheService` → opens at 50% failure rate, auto-recovers
</details>

<details>
<summary><b>5. Sliding Window Counter over Token Bucket</b></summary>

**Sliding window:** strictly caps to exactly N requests per window — no bursting. Best for abuse prevention.

**Token bucket:** users accumulate tokens during quiet periods and burst. Better for user-facing APIs.

We chose sliding window for StreamBase because strict enforcement matters more than user experience.

Implementation: **Redis Lua script** for atomic `INCR + EXPIRE` across all 3 app instances — zero race conditions.
</details>

---

## 📊 Real Numbers

| What Was Measured | Setup | Result |
|---|---|---|
| Short link redirects | 10K hits · 1000 links · 500 req/s | p50 = **0.98ms** · p95 = **4ms** · p99 = 307ms · **100% success** |
| Feed pull (warm cache) | 100 follows × 50 videos | **7–12ms** with composite index |
| Shard distribution | 263 videos across 3 shards | **79 / 107 / 77** — within tolerance |
| Shard rebalance cost | Added shard-3 to 2-shard setup | **29.3%** keys moved (theoretical 33%) |
| Search crossover | ES vs PostgreSQL ILIKE | ILIKE wins at 5K rows (3–7ms vs 7–22ms) · ES wins at **~100K rows** |
| Rate limiter accuracy | 110 requests, limit = 100 | Exactly **100 allowed · 10 rejected** |

---

## 💥 Failure Stories (Chaos Day)

| Scenario | Impact | Root Cause | Resolution |
|---|---|---|---|
| Kill `app-1` mid-request | **0 requests lost** | Nginx health checks reroute | Already handled by Nginx |
| Stop Redis | **504 Gateway Timeout** | Connection blocked indefinitely | 500ms timeout + `CacheErrorHandler` + Circuit Breaker |
| Flood `POST /api/videos` × 1000 | **Held at exactly 100** | Lua script is atomic | Already handled |
| Stop Kafka during upload | **Upload fails cleanly** | `@Transactional` rolls back | Known trade-off → Transactional Outbox (deferred) |
| Stop read replica | **Auto-failover to primary** | `ReplicaHealthMonitor` detects | Auto-recovery on replica restart |

---

## 🗂️ Project Structure

```
streambase/
│
├── 📄 compose.yaml                    # 13 Docker services — entire infra in one file
├── 📄 nginx.conf                      # Load balancer config (round-robin + least_conn)
│
└── src/main/java/.../streambase/
    │
    ├── 📁 config/                     # Infrastructure wiring
    │   ├── DataSourceConfig           # Primary/Replica routing
    │   ├── RoutingDataSource          # Read → replica, Write → primary
    │   ├── ReplicaHealthMonitor       # Failover detection
    │   ├── KafkaConfig                # Topics, consumers, error handlers
    │   ├── RedisConfig                # Cache + Pub/Sub setup
    │   ├── RedisPublisherSubscriberConfig
    │   ├── Resilience4jConfig         # Circuit breaker
    │   ├── RateLimiterConfig          # Interceptor registration
    │   └── WebSocketConfig
    │
    ├── 📁 controller/                 # REST endpoints
    │   ├── VideoController            # CRUD /api/videos
    │   ├── LinkController             # POST /api/links · GET /s/{code}
    │   ├── FeedController             # GET /api/feed
    │   ├── NotificationController     # GET /api/notifications
    │   ├── SearchController           # GET /api/search · /api/search/suggest
    │   ├── SubscriptionController     # POST /api/subscriptions
    │   ├── ShardedVideoController     # Sharded video ops
    │   └── HealthController           # GET /health
    │
    ├── 📁 consumer/                   # Kafka consumers
    │   ├── VideoProcesscor            # video.uploaded → PROCESSING → READY
    │   ├── NotificationFanOutConsumer # video.published → fan-out to channels
    │   ├── EmailNotificationConsumer  # notification.email topic
    │   ├── InAppNotificationConsumer  # notification.in_app topic
    │   ├── PushNotificationConsumer   # notification.push topic
    │   ├── FeedFanOutConsumer         # feed-fanout → Redis ZSET
    │   └── SearchIndexConsumer        # video.published → Elasticsearch
    │
    ├── 📁 service/                    # Business logic
    ├── 📁 sharding/                   # ConsistentHashRing · ShardRouter · SnowFlakeIdGenerator
    ├── 📁 websocket/                  # CommentWebSocketHandler · CommentBroadcastListener
    ├── 📁 search/                     # VideoSearchDocument · VideoSearchRepository
    ├── 📁 entity/                     # JPA entities
    ├── 📁 filter/                     # RateLimiterInterceptor
    └── 📁 exception/                  # GlobalExceptionHandler

src/main/resources/
    ├── 📁 db/migration/               # Flyway SQL (V1–V7)
    └── 📁 elasticsearch/              # autocomplete_settings.json (edge n-gram analyzer)
```

---

## 🚀 Quick Start

```bash
# Prerequisites: Docker Desktop · Java 21 · Maven

git clone <repo-url> && cd streambase

# Start all 13 services (Nginx, PostgreSQL, Redis, Kafka, Elasticsearch, 3 app instances)
docker compose up --build

# Access via Nginx load balancer
curl http://localhost/health
```

<details>
<summary><b>Sample API Calls</b></summary>

```bash
# Upload a video
curl -X POST http://localhost/api/videos \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user-1" \
  -d '{"title":"My Video","description":"Test","url":"https://example.com/v.mp4","uploadedBy":"user-1"}'

# Search
curl "http://localhost/api/search?q=test"

# Autocomplete
curl "http://localhost/api/search/suggest?q=my"

# Create short link
curl -X POST http://localhost/api/links \
  -H "Content-Type: application/json" \
  -d '{"videoId": 1}'

# Get personalised feed
curl "http://localhost/api/feed?userId=user-1"
```
</details>

---

## 🎯 Interview Question Mapping

| "Design a ..." | What's Built Here |
|---|---|
| YouTube / Netflix | Full pipeline: upload → Kafka → search → personalised feed |
| Chat System (WhatsApp) | WebSocket + Redis Pub/Sub across 3 servers (Days 11–13) |
| TinyURL | base62 encoding · Redis hot-link cache · atomic click counter (Days 6–7) |
| Rate Limiter | Sliding window · Redis Lua · 429 with Retry-After (Day 8) |
| Notification System | Kafka fan-out · exponential backoff · dead letter queue (Day 9) |
| Twitter / Instagram Feed | Fan-out on write vs read · Bieber problem · hybrid model (Day 14) |
| Distributed DB / Sharding | Consistent hashing · Snowflake IDs · live rebalancing (Day 15) |
| YouTube Search | Elasticsearch · inverted index · edge n-gram autocomplete (Day 16) |
| "How do you handle failure?" | 5 real failure scenarios broken intentionally + fixed (Day 10) |

---

## ⚖️ Tech Choices Explained

| Chose | Over | Reason |
|---|---|---|
| **Kafka** | RabbitMQ | Replay, offset tracking, consumer groups — interview-relevant |
| **Redis Pub/Sub** | Kafka (for WS) | ~1ms latency · fire-and-forget · no disk write needed |
| **Consistent Hashing** | Modular (`id % N`) | Remaps only 1/N keys on shard add vs ~75% |
| **Sliding Window** | Token Bucket | Hard limit, no bursting — better for abuse prevention |
| **WebSocket** | SSE · Long Polling | Bidirectional — comments flow both ways |
| **Elasticsearch** | PostgreSQL ILIKE | Fuzziness · ranking · autocomplete — features ILIKE can never match |

---

<div align="center">

**Built by [Kavinkumar S](https://linkedin.com/in/contactkavinkumar)**

*Software Engineer · 3+ years at Zoho Corporation (ManageEngine ADSelfService Plus)*

kavinkumarskvp@hotmail.com

</div>
