# Graph Report - .  (2026-06-14)

## Corpus Check
- 15 files · ~0 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 236 nodes · 271 edges · 44 communities (18 shown, 26 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 37 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]

## God Nodes (most connected - your core abstractions)
1. `KafkaConfig` - 17 edges
2. `StreamBase System Design Learning Project` - 10 edges
3. `Redis (Docker Service)` - 8 edges
4. `CommentWebSocketHandler` - 8 edges
5. `RateLimiterInterceptor` - 7 edges
6. `VideoController` - 6 edges
7. `postgres-primary (Docker Service)` - 6 edges
8. `application-dev.yaml (Dev Profile Config)` - 6 edges
9. `Primary DataSource` - 6 edges
10. `Replica DataSource` - 6 edges

## Surprising Connections (you probably didn't know these)
- `Day 4 Database Replication Completed` --references--> `postgres-replica (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Redis Pub/Sub for Cross-Server Comments` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Fan-Out on Read vs Write` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Cache-Aside Pattern` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Sliding Window Rate Limiting via Redis` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml

## Communities (44 total, 26 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.15
Nodes (23): application-dev.yaml (Dev Profile Config), application.yaml (Spring Boot Config), Cache-Aside Pattern, Consistent Hashing for DB Sharding, Database Read Replication, Dead-Letter Topic (DLQ) for Failed Processing, Fan-Out on Read vs Write, Flyway SQL Migrations Convention (+15 more)

### Community 1 - "Community 1"
Cohesion: 0.13
Nodes (3): LinkController, LinkRepository, LinkService

### Community 3 - "Community 3"
Cohesion: 0.21
Nodes (5): MessageListener, PresenceService, TextWebSocketHandler, CommentBroadcastListener, CommentWebSocketHandler

### Community 4 - "Community 4"
Cohesion: 0.17
Nodes (10): DB_PASSWORD Environment Variable, DB_REPLICA_URL Environment Variable, DB_URL Environment Variable, DB_USERNAME Environment Variable, Read Replica Pattern, Redis Configuration, REDIS_HOST Environment Variable, REDIS_PORT Environment Variable (+2 more)

### Community 5 - "Community 5"
Cohesion: 0.23
Nodes (3): VideoController, NotificationMapper, VideoMapper

### Community 6 - "Community 6"
Cohesion: 0.29
Nodes (9): appendComment(), appendToFeed(), appendUserEvent(), connect(), formatTime(), join(), params, reconnect() (+1 more)

### Community 10 - "Community 10"
Cohesion: 0.25
Nodes (4): NotificationFanOutConsumer, SubscriptionController, SubscriptionRepository, SubscriptionService

### Community 11 - "Community 11"
Cohesion: 0.29
Nodes (3): NotificationController, NotificationRepository, NotificationService

### Community 12 - "Community 12"
Cohesion: 0.33
Nodes (5): CommentEntity, LinkEntity, NotificationEntity, SubscriptionEntity, VideoEntity

### Community 15 - "Community 15"
Cohesion: 0.5
Nodes (4): LinkExpiredException, LinkNotFoundException, VideoNotFoundException, RuntimeException

### Community 16 - "Community 16"
Cohesion: 0.5
Nodes (4): application.yaml (Base Config), open-in-view: false, Day 4 Database Replication Completed, LazyConnectionDataSourceProxy Pattern

### Community 18 - "Community 18"
Cohesion: 0.5
Nodes (3): EmailNotificationConsumer, InAppNotificationConsumer, PushNotificationConsumer

## Knowledge Gaps
- **42 isolated node(s):** `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto`, `VideoRepository`, `VideoEntity` (+37 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **26 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LinkService` connect `Community 1` to `Community 5`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Why does `CommentService` connect `Community 7` to `Community 5`?**
  _High betweenness centrality (0.010) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Redis (Docker Service)` (e.g. with `Cache-Aside Pattern` and `Sliding Window Rate Limiting via Redis`) actually correct?**
  _`Redis (Docker Service)` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto` to the rest of the system?**
  _42 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.13 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._