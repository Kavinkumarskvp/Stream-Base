# StreamBase — Build Log & Engineering Notes

> Personal engineering log documenting every decision, trade-off, and lesson learned while building StreamBase from scratch. Written as I built it — one concept per day.

---

## Week 1 — Platform Foundation

### Day 1 · Project Setup + Video Catalog API
**Theme:** Containerization + REST API

Set up the Spring Boot project with Docker from day one — not as an afterthought. The key discipline: every service runs in a container, even in development. This forces you to think about environment variables, networking, and port exposure early rather than scrambling at deployment.

**What I built:** Video entity, full CRUD API, PostgreSQL in Docker, data persistence verified across container restarts.

---

### Day 2 · Load Balancing
**Theme:** Horizontal Scaling

Ran 3 identical Spring Boot instances behind Nginx. The key insight: for horizontal scaling to work, your app instances must be **stateless** — no in-memory session, no local cache that isn't shared. If one instance stores something the others don't have, Nginx's round-robin will break user experience.

**Tested:** Round-robin (default) and `least_conn` (routes to instance with fewest active connections). `least_conn` is better under uneven load — if one instance handles a slow request, new requests go elsewhere.

**What I built:** nginx.conf with upstream block, `/health` endpoint returning hostname, verified distribution across all 3 instances.

---

### Day 3 · Redis Caching
**Theme:** Cache-Aside Pattern

Implemented cache-aside manually (not Spring's `@Cacheable`) to understand the mechanics: check cache → on miss, query DB and populate cache → on update/delete, invalidate key.

**Key decisions:**
- TTL: 5 minutes. Trade-off: stale data risk vs DB load reduction. For video metadata, 5 min staleness is acceptable.
- Cache invalidation: on PUT/DELETE, explicitly delete the Redis key. Don't try to update it — simpler and safer.
- Verified: 100 GET requests → only 1 DB query (on cold start). Everything else served from Redis.

---

### Day 4 · Read Replicas
**Theme:** Database Replication

Added a PostgreSQL streaming replica. Implemented custom `RoutingDataSource` that routes:
- `@Transactional(readOnly = true)` → replica
- All writes → primary

**What I learned firsthand:**
- **Replication lag is real.** Insert a row, immediately read from replica → sometimes not there yet. This is why strong consistency requires reading from primary.
- **Replica failure.** When the replica stopped, the app tried to read from it and threw errors. Fixed with `ReplicaHealthMonitor` — background thread pings replica every 10s, falls back to primary on failure, auto-recovers when replica comes back.

---

### Day 5 · Kafka Async Processing Pipeline
**Theme:** Message Queues

On video upload, instead of processing synchronously (slow, blocks the API), the `POST /api/videos` returns instantly after saving to DB and publishing a `video.uploaded` event to Kafka. A separate `VideoProcessor` consumer picks it up asynchronously.

**State machine:** `UPLOADED` → `PROCESSING` → `READY`

**Key experiments:**
- Crashed consumer mid-processing (after status=PROCESSING, before READY). On restart: Kafka replayed the message from the last committed offset. Consumer handled it correctly.
- Added dead-letter topic (`video.uploaded.DLT`) — after 3 retry attempts, permanently failed jobs go here instead of blocking the queue.

**KRaft mode:** Used `apache/kafka:4.3.0` in KRaft mode — no ZooKeeper dependency. Simpler, closer to how modern Kafka deployments work.

---

## Week 2 — Platform Features

### Day 6 · Short Links — Design
**Theme:** System Design Process

Before writing code, designed the system fully on paper. This is the discipline most engineers skip.

**Back-of-envelope math:**
- 100K links/day × 365 × 5 years = ~182M links
- Each link ~100 bytes → ~18GB storage (well within a single DB)
- 10:1 read/write ratio → reads are the bottleneck

**Encoding decision — base62 vs MD5:**

| | base62 on auto-increment ID | MD5 hash of URL |
|---|---|---|
| Collision risk | Zero (sequential) | Possible (need to check) |
| Predictability | Guessable (sequential) | Not guessable |
| Length | Grows over time | Fixed 6–8 chars |
| My choice | ✅ | |

Chose base62 — zero collision risk, simpler implementation, acceptable for a platform where link privacy isn't critical.

---

### Day 7 · Short Links — Build
**Theme:** URL Shortening + Caching

Implemented the design from Day 6. Click counting was the interesting part — naive approach (`UPDATE links SET click_count = click_count + 1`) hits the DB on every redirect. At high volume, this creates a hot row.

**Solution (Bitly-style):**
- On redirect: `INCR clicks:{code}` in Redis (atomic, ~0.1ms)
- Background job: every 60s, flush Redis counters to PostgreSQL in batch

**Load test results:**
- 10K redirects across 1000 links at 500 req/s
- p50: **0.98ms** · p95: **4ms** · p99: **307ms** (cold-cache tail on first hit) · **100% success rate**

---

### Day 8 · Rate Limiter
**Theme:** Distributed Rate Limiting

**Why not a simple counter?** With 3 app instances, each has its own memory. A per-instance counter would allow 3× the intended limit. Rate limiting must be centralized — Redis.

**Implementation:** Redis Lua script for atomic sliding window counter:
```lua
local count = redis.call('INCR', key)
if count == 1 then redis.call('EXPIRE', key, window_seconds) end
return count
```
Lua scripts execute atomically in Redis — no race conditions across 3 instances.

**Limits applied:**
- 100 requests/min per IP (general endpoints)
- 10 uploads/hour per user (POST /api/videos, identified by X-User-Id header)
- Returns HTTP 429 with `Retry-After` header

**Sliding window vs token bucket:**

| | Sliding Window | Token Bucket |
|---|---|---|
| Behavior | Strict cap, no bursting | Allows bursting |
| Best for | Abuse prevention, DDoS protection | User-facing APIs with natural usage patterns |
| Used by | Stripe (writes), Twitter | AWS API Gateway, Cloudflare, GitHub |
| My choice | ✅ for StreamBase | Better for read APIs |

---

### Day 9 · Subscription Notifications
**Theme:** Event-Driven Fan-Out

When a video reaches `READY` status, `VideoProcessor` publishes a `video.published` event. `NotificationFanOutConsumer` picks it up, queries all subscribers of that creator, and fans out to 3 separate Kafka topics: `notification.email`, `notification.push`, `notification.in_app`.

**Retry strategy:** Spring's `DefaultErrorHandler` with exponential backoff — 3 attempts before giving up. Failed messages go to `notification.dlt` via `DeadLetterPublishingRecoverer`.

**Why separate topics per channel?** If email delivery is slow, it shouldn't block in-app notifications. Each channel consumer scales independently.

**Known gap — Notification Idempotency:** If the fan-out consumer crashes after publishing to email + push but before in_app, Kafka replays the event. Some subscribers get duplicate notifications. Fix: idempotency key `(subscriberId, videoId, channel)` — check before sending, skip if already processed. Used by every payment system; Stripe requires `Idempotency-Key` headers for this exact reason.

---

### Day 10 · Chaos Day
**Theme:** Resilience Engineering

Intentionally broke every major component to understand failure modes. The discipline: don't just handle failures — **understand them before they happen in production.**

| Failure | Impact | Root Cause | Fix |
|---|---|---|---|
| Kill app-1 mid-request | 0 requests lost | Nginx health checks → reroute | Already handled |
| **Stop Redis** | **504 Gateway Timeout** | Connection blocked indefinitely | 500ms timeout + `CacheErrorHandler` + Resilience4j circuit breaker |
| Flood POST /api/videos × 1000 | Held at exactly 100 | Lua script is atomic across instances | Already handled |
| Stop Kafka during upload | Upload fails cleanly, no orphan rows | `@Transactional` rolls back | Known trade-off → see Transactional Outbox below |
| Stop read replica | Auto-failover to primary | `ReplicaHealthMonitor` | Auto-recovery on restart |

**Worst failure:** Redis going down caused 504s across the entire API — not just cache misses, but complete unavailability. Fixed in 3 layers: connection timeout (fail fast) → `CacheErrorHandler` (fall back to DB) → circuit breaker (stop hammering a dead Redis).

---

## Week 3 — Advanced Features

### Day 11 · Live Comments — Design
**Theme:** Real-Time Systems

**Technology comparison:**

| | WebSocket | SSE | Long Polling |
|---|---|---|---|
| Direction | Bidirectional | Server → Client only | Simulated push |
| Latency | ~1ms | ~10ms | ~1–2s |
| Connection | Persistent | Persistent | New per message |
| Use case | Chat, live comments | Stock prices, notifications | Fallback |
| My choice | ✅ | Good for notifications | Last resort |

SSE is one-way — browsers can't send comments back on the same connection. Long polling has ~1–2s latency which feels broken for live comments.

**The multi-server problem identified at design time:** Users A and B connect to the same video, but A is on app-1 and B is on app-3. Each server only knows about its own WebSocket connections. Solved in Day 13.

---

### Day 12 · Live Comments — Build (Single Server)
**Theme:** WebSocket Implementation

Implemented `CommentWebSocketHandler` extending Spring's `TextWebSocketHandler`. On new comment:
1. Persist to PostgreSQL (`CommentEntity`)
2. Push to Redis list (`LPUSH comments:{videoId}` + `LTRIM` to keep last 50)
3. Broadcast to all local WebSocket sessions watching that video

On new viewer connects: send cached 50 comments immediately so they're not starting cold.

---

### Day 13 · Live Comments — Multi-Server
**Theme:** Cross-Server Pub/Sub

**The bug (verified firsthand):** User A on app-1 sends a comment. app-1 broadcasts to its local sessions. User B on app-3 sees nothing — app-3 has no idea a comment was sent.

**Why sticky sessions don't fix this:**
- Route all viewers of a video to the same server → that server becomes a hot spot
- Server crashes → all viewers lose connection simultaneously
- Defeats the purpose of running 3 instances

**Fix — Redis Pub/Sub:**
```
app-1 receives comment → PUBLISH comments:videoId payload
                              ↓
          all 3 servers subscribed to PatternTopic("comments:*")
                              ↓
          each pushes to its own local WebSocket clients
```

Each server subscribes once on startup. ~1ms broadcast latency. No durability needed — PostgreSQL already has the source of truth.

**Bonus — Presence tracking:** Used Redis ZSET (score = epoch timestamp). On connect: `ZADD viewers:{videoId} timestamp userId`. On disconnect: `ZREM`. Viewer count pushed over WebSocket rather than polled.

---

### Day 14 · Subscription Feed
**Theme:** Fan-Out on Read vs Write

**Pull model (naïve):**
```sql
SELECT v.* FROM videos v
JOIN subscriptions s ON v.uploaded_by = s.creator_id
WHERE s.subscriber_id = ?
ORDER BY v.created_at DESC LIMIT 20
```
Warm latency with composite index: **7–12ms** for 100 follows × 50 videos. Scales poorly — O(follows × videos_per_creator).

**Push model (fan-out on write):**
When a video is published, `FeedFanOutConsumer` writes to every follower's Redis ZSET: `ZADD feed:{userId} timestamp videoId`. Feed read is a single `ZREVRANGE` — O(1).

**The Bieber Problem:** 1 upload × 10M followers = 10M Redis writes. At 10K followers/sec, that's ~17 minutes of fan-out for one upload.

**Hybrid solution (what I built):**

| Creator | Strategy |
|---|---|
| < 10K followers | Fan-out on write → Redis ZSET |
| ≥ 10K followers | Skip fan-out → pull at read time |

`CreatorTierService` classifies creators. `GET /api/feed` merges both: read from ZSET + pull from celebrity creators' tables. Bounded cost per request regardless of who you follow.

---

### Day 15 · Database Sharding
**Theme:** Consistent Hashing

**Why not `videoId % N`?** Adding a 4th shard remaps `(N-1)/N ≈ 75%` of keys — massive data migration.

**Consistent hashing with virtual nodes:**
- Each shard gets 150 virtual nodes on the hash ring (MD5-hashed)
- `ring.getShard(videoId)` → O(log N) lookup via `TreeMap.ceilingKey()`
- Adding a shard moves only ~1/N keys

**Observed results (real data):**
- 2 shards, 263 videos: `122 / 141` distribution (46% / 54%) — within tolerance ✅
- Added shard-3: `29.3%` of keys moved (theoretical: 33.3%) ✅
- New distribution: `79 / 107 / 77` ✅

**Snowflake IDs** used instead of auto-increment — globally unique across all shards without a central ID coordinator. Format: `timestamp (41 bits) + machine ID (10 bits) + sequence (12 bits)`.

**Hot shard mitigations** (documented, not built):
- Cache hot rows in Redis to avoid DB reads
- Sub-sharding: split a hot shard into smaller ones
- Dedicated shards for known-hot data (celebrity videos)
- Per-key rate limiting at the shard router level

---

### Day 16 · Full-Text Search
**Theme:** Inverted Index + Autocomplete

**Why not PostgreSQL ILIKE?**

| | PostgreSQL ILIKE | Elasticsearch |
|---|---|---|
| Speed at 5K rows | **3–7ms** ✅ | 7–22ms |
| Speed at 100K+ rows | Degrades badly | Consistent |
| Fuzziness | ❌ | ✅ (`AUTO`) |
| Ranking by relevance | ❌ | ✅ (BM25) |
| Autocomplete | Hacky | ✅ (native) |
| Crossover point | ~100K rows | ~100K rows |

ILIKE wins on raw speed at small scale. But **Elasticsearch wins on features at any scale** — fuzziness, relevance ranking, autocomplete, aggregations. These are features ILIKE can never have.

**Search implementation:**
- `multi_match` across `title^3` (boosted) + `description`, `fuzziness: AUTO`
- Indexed via `SearchIndexConsumer` on `video.published` Kafka event (same pipeline, new consumer group)

**Autocomplete implementation:**
- Custom `edge_ngram` analyzer (`min_gram=2, max_gram=20`) on `title.autocomplete` sub-field
- `cook` → matches `cooking`, `cookbook`, `cookware`
- Settings loaded from `elasticsearch/autocomplete_settings.json` at index creation

---

## Technologies Considered But Not Used

### SSE (Server-Sent Events)
**Why rejected for live comments:** One-way only. Browser can't send comments back on the same connection — requires a separate HTTP POST. WebSocket handles both directions on one connection.
**Right choice when:** Server-only push (stock prices, live scores, notifications).

### Long Polling
**Why rejected:** ~1–2s latency feels broken for live comments. New HTTP connection per message cycle = high overhead.
**Right choice when:** WebSocket is blocked by corporate firewalls, or sub-second latency isn't required.

### RabbitMQ
**Why rejected:** No message replay — once consumed, gone. No consumer group offset tracking. Kafka replays are essential for the video processing pipeline (crash recovery).
**Right choice when:** Task queues, job scheduling, request/reply patterns where routing flexibility matters more than throughput.

### Sticky Sessions (for WebSocket)
**Why rejected:** Routes all viewers of a video to one server → hot spot + single point of failure. If that server crashes, all viewers disconnect simultaneously.
**Right choice when:** Single server, simple setups, no scaling concern.

### Kafka (for WebSocket broadcast)
**Why rejected:** Writes every comment to disk — unnecessary since PostgreSQL already persists comments. Requires 3 consumer groups (one per app instance). ~10–50ms latency vs Redis Pub/Sub's ~1ms.
**Right choice when:** Broadcast messages also need durability and replay (audit logs, financial events).

### Modular Hashing (`id % N`) for Sharding
**Why rejected:** Adding a shard remaps ~75% of keys. At scale, this is a massive, expensive migration.
**Right choice when:** Number of shards is fixed permanently and will never change.

---

## Deferred Improvements (Strong Interview Talking Points)

### Transactional Outbox Pattern
**Problem:** `POST /api/videos` does `videoRepository.save()` + `kafkaTemplate.send()` in one `@Transactional`. When Kafka is down, the transaction rolls back — upload breaks entirely. Kafka is a hard dependency.

**Solution:**
1. Save video + outbox row in the same DB transaction
2. Background poller reads unsent outbox rows → publishes to Kafka → marks sent

**Two poller flavors:**
- **Polling:** `SELECT * FROM outbox WHERE sent = false` every N seconds. Simple, some DB load.
- **CDC with Debezium:** Tails PostgreSQL WAL, pushes changes automatically. Zero polling overhead, near-real-time.

**Result:** Uploads only need the DB. Kafka can be down for hours — events queue in outbox and replay when Kafka returns.

---

### Notification Idempotency
**Problem:** Fan-out consumer crashes after publishing to email + push but before in_app. Kafka replays. Some subscribers get duplicates.

**Solution:** Idempotency keys — dedupe by `(subscriberId, videoId, channel)` before processing. Use Redis SET with TTL or DB unique constraint.

Used by every payment system. Stripe requires `Idempotency-Key` headers for exactly this reason.

---

### Token Bucket Rate Limiter
**Why it's sometimes better than sliding window:**
- Quiet users accumulate tokens → can burst when needed
- More natural for human usage patterns
- Used by: AWS API Gateway, Cloudflare, GitHub

**Build idea:** Token bucket on read endpoints (`GET /api/videos`), sliding window on write endpoints (`POST /api/videos`).

---

### Cuckoo Filter for Search (Zero-Result Guard)
**Problem:** Every `GET /api/search?q=xyznonexistent` hits Elasticsearch — wasted CPU at high QPS.

**Solution:** Cuckoo filter in Redis Stack (`CF.*`) — probabilistic check before hitting ES.
- `SearchIndexConsumer` indexes video → also `CF.ADD videoTerms {term}` for each word
- On search: `CF.EXISTS videoTerms {query}` → definite "no" → return `[]` immediately, skip ES
- False positive rate: ~1% tunable

Chose Cuckoo over Bloom filter because Cuckoo supports deletion (videos can be unpublished).

Industrial precedent: Cassandra uses Bloom filters per SSTable for the same pattern — cheap RAM check before expensive disk read.

---

*Built by Kavinkumar S · [LinkedIn](https://linkedin.com/in/contactkavinkumar)*
