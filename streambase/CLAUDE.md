# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Build & Run Commands

```bash
# Run the application (auto-starts Docker services via compose.yaml)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=StreambaseApplicationTests

# Build JAR
./mvnw clean package

# Start only the database manually
docker compose up postgres -d
```

## Project Layout

```
src/main/java/kavin/personal_project/streambase/
  StreambaseApplication.java          # Entry point

src/main/resources/
  application.yaml                    # App config (minimal, Docker Compose handles service URLs)
  db/migration/                       # Flyway SQL migrations — naming: V1__description.sql

compose.yaml                          # Docker services (PostgreSQL now; Redis, Kafka, Nginx added per week)
pom.xml                               # Maven build — Java 21, Spring Boot 4.0.5
```

**Package naming note:** Maven converted `personal-project` → `personal_project` (hyphens to underscores).

## Key Conventions

- **Database migrations:** All schema changes go in `src/main/resources/db/migration/` as Flyway SQL files (`V{n}__{description}.sql`). Flyway runs automatically on startup.
- **Docker Compose integration:** Spring Boot auto-connects to services defined in `compose.yaml` when running locally — no need to manually set datasource URLs in `application.yaml` during development.
- **Lombok:** Use `@Data`, `@Getter`, `@Builder`, etc. on entities and DTOs — annotation processor is configured.
- **Package structure to grow into:** `entity` → `repository` → `service` → `controller` (standard layered architecture). Add `config`, `consumer`, `filter` packages as Kafka/Redis/WebSocket are introduced.

---

# StreamBase — System Design Learning Project

## About This Project
Build a Netflix-like video streaming platform (StreamBase) hands-on to master system
design concepts. Every feature you build maps directly to a real interview question.
4 Weeks · 20 Sessions · 10+ Concepts.

**Designed for:** Mid-Level Engineers (3-4 yrs experience) — Java Backend

---

## Tech Stack
| Technology | Role |
|---|---|
| Spring Boot | API server |
| PostgreSQL | Primary relational database |
| Redis | Caching + Pub/Sub + Rate limiting |
| Kafka | Async event pipeline (replaces RabbitMQ) |
| Nginx | Load balancer |
| Elasticsearch | Search (future) |
| Docker / docker-compose | Local orchestration |

> ⚠️ **Note:** The original plan used RabbitMQ. We have switched to **Kafka** throughout
> as it is more commonly asked about in system design interviews.

---

## How to Help Me (Claude Code Instructions)
- Guide me step by step through whichever task I am currently on
- Explain the **"why"** behind each concept — I am learning system design, not just coding
- Point out Spring Boot best practices as we go
- When I finish a task, remind me to check it off and tell me what's next
- If I get stuck, break the task into smaller sub-steps
- Reference my StreamBase experience when I ask interview-style questions

---

## My Current Progress

### 🔄 In Progress
- [ ] **DAY 6, Task 1** — Write requirements: short link for any video, redirect on access, track click count

---

## WEEK 1 — Platform Foundation
*Set up StreamBase core: API, database, scaling, caching, and async processing*

---

### DAY 1 — Project Setup + Video Catalog API
**Theme:** Containerization + REST API

- [x] 1. Install Docker Desktop and verify with `docker --version`
- [x] 2. Create Spring Boot project with Web + JPA + PostgreSQL dependencies
- [x] 3. Define Video entity: `id`, `title`, `description`, `url`, `uploadedBy`, `createdAt`
- [x] 4. Create `VideoController` with CRUD endpoints (`GET/POST/PUT/DELETE /api/videos`)
- [x] 5. Write `docker-compose.yml` with app + postgres services
- [x] 6. Run `docker-compose up` and test all endpoints with curl
- [x] 7. Verify data persists across container restarts

**✅ Day 1 Outcome:** StreamBase API running in Docker. You can create, list, update,
and delete videos via REST.

---

### DAY 2 — Scale the API with Load Balancing
**Theme:** Load Balancing

- [x] 1. Modify docker-compose to run 3 instances of your API (app-1, app-2, app-3)
- [x] 2. Add Nginx service with upstream config pointing to all 3 instances
- [x] 3. Configure round-robin load balancing in `nginx.conf`
- [x] 4. Add a `/health` endpoint that returns the instance hostname
- [x] 5. Hit the API 20 times via Nginx — verify requests distribute across instances
- [x] 6. Try `least_conn` strategy in Nginx, compare behavior under load

**✅ Day 2 Outcome:** StreamBase runs behind a load balancer. Three API instances share
traffic. You can explain horizontal scaling.

---

### DAY 3 — Cache Video Catalog with Redis
**Theme:** Caching Strategies

- [x] 1. Add Redis service to `docker-compose.yml`
- [x] 2. Add `spring-boot-starter-data-redis` to your project
- [x] 3. Implement cache-aside pattern on `GET /api/videos/{id}`
- [x] 4. Set TTL of 5 minutes on cached video entries
- [x] 5. Add cache invalidation: on PUT/DELETE, remove the Redis key
- [x] 6. Use `redis-cli` inside the container to inspect cached keys manually
- [x] 7. Test: call GET 100 times, verify only 1st hit touches PostgreSQL

**✅ Day 3 Outcome:** Video metadata is cached. Reads are ~10x faster on cache hit.
You understand cache-aside, TTL, and invalidation.

---

### DAY 4 — Add Read Replicas for the Database
**Theme:** Database Replication

- [x] 1. Add a second PostgreSQL container (`postgres-replica`) to docker-compose
- [x] 2. Configure streaming replication from primary to replica
- [x] 3. Modify your repository layer: route writes to primary, reads to replica
- [x] 4. Verify: insert a video, then immediately read from replica
- [x] 5. Intentionally stop the replica — observe what your app does
- [x] 6. Add fallback logic: if replica is down, read from primary

**✅ Day 4 Outcome:** StreamBase has read scaling via replicas. You've experienced
replication lag and failover firsthand.

---

### DAY 5 — Async Video Processing Pipeline
**Theme:** Message Queues (Kafka)

- [x] 1. Add Kafka service to `docker-compose.yml` (use `apache/kafka:4.3.0` image, KRaft mode)
- [x] 2. On video upload (`POST /api/videos`), publish a `video.uploaded` event to Kafka topic
- [x] 3. Create a `VideoProcessor` consumer service that listens to the topic
- [x] 4. Consumer simulates: thumbnail generation (sleep 3s) + metadata extraction
- [x] 5. Update video status: `UPLOADED` → `PROCESSING` → `READY`
- [x] 6. Crash the consumer mid-processing, restart it — verify Kafka replays the message
- [x] 7. Add a dead-letter topic for permanently failed processing jobs

**✅ Day 5 Outcome:** Videos are processed asynchronously. The upload API returns
instantly. You understand Kafka topics, consumer groups, DLQ, and replay.

---

## WEEK 2 — Platform Features
*Add sharing, protection, notifications, and resilience to StreamBase*

---

### DAY 6 — Shareable Short Links — Design
**Theme:** System Design Process

- [ ] 1. Write requirements: short link for any video, redirect on access, track click count
- [ ] 2. Estimate scale: 100K links/day, 10:1 read/write ratio, 5-year retention
- [ ] 3. Calculate storage: ~500M links x 100 bytes = ~50GB
- [ ] 4. Design the API: `POST /api/links` (returns short code), `GET /s/{code}` (redirects)
- [ ] 5. Choose encoding: base62 on auto-increment ID (write the trade-off vs MD5)
- [ ] 6. Design the DB schema: `links(id, code, video_id, click_count, created_at)`
- [ ] 7. Plan: cache hot links in Redis for fast redirect

**✅ Day 6 Outcome:** A complete design document for the short-link feature.
This IS your interview answer for "Design TinyURL".

---

### DAY 7 — Shareable Short Links — Build
**Theme:** URL Shortening + Caching

- [ ] 1. Create `Link` entity and `LinkController` (`POST` + `GET` endpoints)
- [ ] 2. Implement base62 encoding on auto-increment ID
- [ ] 3. Add redirect: `GET /s/{code}` returns HTTP 302 with video URL
- [ ] 4. Increment `click_count` atomically on each redirect
- [ ] 5. Cache hot links in Redis (top 20% by click count)
- [ ] 6. Load-test: generate 1000 links, hit random ones 10K times, measure latency

**✅ Day 7 Outcome:** StreamBase has shareable short links with click tracking.
Cached links redirect in <5ms.

---

### DAY 8 — Rate Limiter for API Protection
**Theme:** Rate Limiting

- [ ] 1. Create a `RateLimiterFilter` (Spring `HandlerInterceptor`)
- [ ] 2. Implement sliding window counter using Redis `INCR` + `EXPIRE`
- [ ] 3. Apply: 100 requests/min per IP for general endpoints
- [ ] 4. Apply: 10 uploads/hour per user for `POST /api/videos`
- [ ] 5. Return HTTP 429 with `Retry-After` header when limit exceeded
- [ ] 6. Test: write a script that sends 120 requests in 1 minute, verify throttling
- [ ] 7. Bonus: document token bucket vs sliding window trade-offs in your notes

**✅ Day 8 Outcome:** StreamBase APIs are protected against abuse. You can implement
and explain distributed rate limiting.

---

### DAY 9 — Subscription Notifications
**Theme:** Event-Driven Architecture (Kafka)

- [ ] 1. Add `Subscription` entity: `subscriber_id` follows `creator_id`
- [ ] 2. On new video upload, publish `video.published` event to Kafka topic
- [ ] 3. Create `NotificationConsumer`: fans out to each subscriber's notification queue
- [ ] 4. Add notification types: `EMAIL`, `PUSH`, `IN_APP` (separate topics per type)
- [ ] 5. Implement retry with exponential backoff (1s, 2s, 4s, 8s, max 3 retries)
- [ ] 6. Failed notifications go to dead-letter topic
- [ ] 7. Add `GET /api/notifications?userId={id}` to fetch user's notifications

**✅ Day 9 Outcome:** Creators upload; subscribers get notified. Multiple notification
channels with retry. Classic interview question solved.

---

### DAY 10 — Chaos Day: Break StreamBase
**Theme:** Resilience + Failure Modes

- [ ] 1. Kill one API instance mid-request — does Nginx route to healthy ones?
- [ ] 2. Stop Redis — does the app crash or fall back to DB?
- [ ] 3. Flood `POST /api/videos` with 1000 rapid requests — does rate limiter hold?
- [ ] 4. Stop Kafka while uploading — what happens to the processing pipeline?
- [ ] 5. Stop the read replica — does read traffic failover to primary?
- [ ] 6. Document each failure: "When X dies, Y happens because Z"
- [ ] 7. Fix the worst failure you found (add circuit breaker or fallback)

**✅ Day 10 Outcome:** You know exactly how StreamBase fails. These failure stories
are gold in interviews.

---

## WEEK 3 — Advanced Features
*Add real-time comments, recommendation feed, search, and data sharding*

---

### DAY 11 — Live Comments — Design
**Theme:** Real-Time Systems

- [ ] 1. Define requirements: users see comments appear in real-time while watching
- [ ] 2. Compare approaches: WebSocket vs SSE vs long polling (write trade-offs)
- [ ] 3. Choose WebSocket for bidirectional real-time communication
- [ ] 4. Design: how to handle users connected to different server instances?
- [ ] 5. Plan: Redis Pub/Sub to broadcast messages across servers
- [ ] 6. Design message format: `{videoId, userId, text, timestamp}`
- [ ] 7. Plan storage: recent comments in Redis list, older ones in PostgreSQL

**✅ Day 11 Outcome:** Complete real-time comment system design. You can explain
WebSocket vs alternatives with real trade-offs.

---

### DAY 12 — Live Comments — Build Core
**Theme:** WebSocket Implementation

- [ ] 1. Add `spring-boot-starter-websocket` dependency
- [ ] 2. Create WebSocket endpoint: `/ws/comments/{videoId}`
- [ ] 3. Implement: when user sends comment, broadcast to all viewers of that video
- [ ] 4. Store each comment in PostgreSQL for persistence
- [ ] 5. Cache last 50 comments per video in Redis (`LPUSH` + `LTRIM`)
- [ ] 6. On connect: send cached recent comments to the new viewer
- [ ] 7. Test with 2 browser tabs watching same video — comments appear in real-time

**✅ Day 12 Outcome:** Live comments work on a single server instance.
Comments persist and recent ones are cached.

---

### DAY 13 — Live Comments — Multi-Server
**Theme:** Cross-Server Messaging

- [ ] 1. Run 2 WebSocket server instances in docker-compose
- [ ] 2. Problem: User A on Server 1 can't see User B's comments from Server 2
- [ ] 3. Add Redis Pub/Sub: on new comment, publish to channel `comments:{videoId}`
- [ ] 4. Each server subscribes to relevant channels and pushes to its local clients
- [ ] 5. Test: User A connects to Server 1, User B to Server 2 — both see each other's comments
- [ ] 6. Add presence tracking: store connected users in Redis SET with TTL heartbeat

**✅ Day 13 Outcome:** Comments work across multiple servers. This is the key insight
interviewers look for in chat system questions.

---

### DAY 14 — Subscription Feed ("For You" Page)
**Theme:** Feed Generation + Fan-Out

- [ ] 1. Create Follow relationship: `user_id` follows `creator_id`
- [ ] 2. Build pull model first: `GET /api/feed` queries all followed creators' recent videos
- [ ] 3. Measure: with 100 follows x 50 videos each, how slow is the query?
- [ ] 4. Build push model: on new video, write entry to each follower's feed in Redis
- [ ] 5. `GET /api/feed` now just reads pre-computed Redis list — compare speed
- [ ] 6. Discuss: what about creators with 1M followers? (celebrity problem)
- [ ] 7. Implement hybrid: push for normal users, pull for celebrities (>10K followers)

**✅ Day 14 Outcome:** StreamBase has a personalized feed. You can explain fan-out
on read vs write and the celebrity problem.

---

### DAY 15 — Shard the Video Database
**Theme:** Consistent Hashing + Sharding

- [ ] 1. Implement a consistent hashing ring in Java (~50 lines)
- [ ] 2. Create 2 PostgreSQL instances (shard-1, shard-2) in docker-compose
- [ ] 3. Hash video IDs to determine which shard stores each video
- [ ] 4. Implement `ShardRouter`: routes queries to correct shard based on hash
- [ ] 5. Insert 1000 videos — verify roughly even distribution across shards
- [ ] 6. Add shard-3: observe that only ~1/3 of keys need to move (not all)
- [ ] 7. Write down: how would you handle a hot shard? (rebalancing strategies)

**✅ Day 15 Outcome:** StreamBase data is distributed across shards. You understand
consistent hashing, rebalancing, and hot spots.

---

## Core Concepts Checklist
Map each concept to the StreamBase feature where you implemented it.

### Scaling & Infrastructure
- [ ] **Load Balancing** (Day 2) — Nginx in front of 3 API instances
- [ ] **Caching** (Day 3) — Redis for video metadata + catalog pages
- [ ] **Rate Limiting** (Day 8) — Token bucket on upload + streaming APIs
- [ ] **Horizontal Scaling** (Day 2) — Stateless API behind load balancer

### Data Layer
- [ ] **SQL vs NoSQL** (Days 1-3) — PostgreSQL for relational data, Redis for fast lookups
- [ ] **Replication** (Day 4) — Read replica for catalog/search queries
- [ ] **Sharding** (Day 15) — Consistent hashing for video metadata
- [ ] **CAP Theorem** (Days 4, 10) — Experienced during replica lag + chaos day

### Async & Communication
- [ ] **Message Queues / Kafka** (Day 5) — Kafka for video processing pipeline
- [ ] **Event-Driven Architecture** (Day 9) — Upload triggers transcode + notify
- [ ] **WebSocket vs Polling** (Days 12-13) — Real-time comments with WebSocket
- [ ] **API Design** (Day 1+) — REST endpoints for all StreamBase features

### Advanced Patterns
- [ ] **Fan-out (Read vs Write)** (Day 14) — Subscription feed generation
- [ ] **Consistent Hashing** (Day 15) — Data distribution across DB shards
- [ ] **Back-of-Envelope Math** (Day 6+) — QPS, storage, bandwidth for StreamBase

---

## Final Architecture
```
Client --> Nginx (LB) --> 3x Spring Boot API --> Redis (Cache)
                                             --> PostgreSQL (Primary + Read Replica)
                                             --> Kafka (Async Pipeline)
                                             --> Elasticsearch (Search)
                                             --> WebSocket Server (Real-time)

All running locally in one docker-compose file.
```

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- ALWAYS read graphify-out/GRAPH_REPORT.md before reading any source files, running grep/glob searches, or answering codebase questions. The graph is your primary map of the codebase.
- IF graphify-out/wiki/index.md EXISTS, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
