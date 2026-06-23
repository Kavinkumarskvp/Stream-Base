# Graph Report - .  (2026-06-24)

## Corpus Check
- 7 files · ~0 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 288 nodes · 339 edges · 49 communities (22 shown, 27 thin omitted)
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 53 edges (avg confidence: 0.82)
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
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]

## God Nodes (most connected - your core abstractions)
1. `KafkaConfig` - 17 edges
2. `StreamBase System Design Learning Project` - 10 edges
3. `Redis (Docker Service)` - 8 edges
4. `CommentWebSocketHandler` - 8 edges
5. `VideoController` - 7 edges
6. `RateLimiterInterceptor` - 7 edges
7. `ShardRouter` - 7 edges
8. `postgres-primary (Docker Service)` - 6 edges
9. `application-dev.yaml (Dev Profile Config)` - 6 edges
10. `Primary DataSource` - 6 edges

## Surprising Connections (you probably didn't know these)
- `Redis Pub/Sub for Cross-Server Comments` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Fan-Out on Read vs Write` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Day 4 Database Replication Completed` --references--> `postgres-replica (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Cache-Aside Pattern` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Sliding Window Rate Limiting via Redis` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml

## Communities (49 total, 27 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.12
Nodes (27): application-dev.yaml (Dev Profile Config), application.yaml (Base Config), application.yaml (Spring Boot Config), open-in-view: false, Cache-Aside Pattern, Consistent Hashing for DB Sharding, Database Read Replication, Day 4 Database Replication Completed (+19 more)

### Community 1 - "Community 1"
Cohesion: 0.18
Nodes (4): VideoController, NotificationMapper, VideoMapper, LinkService

### Community 4 - "Community 4"
Cohesion: 0.21
Nodes (5): MessageListener, PresenceService, TextWebSocketHandler, CommentBroadcastListener, CommentWebSocketHandler

### Community 6 - "Community 6"
Cohesion: 0.17
Nodes (10): DB_PASSWORD Environment Variable, DB_REPLICA_URL Environment Variable, DB_URL Environment Variable, DB_USERNAME Environment Variable, Read Replica Pattern, Redis Configuration, REDIS_HOST Environment Variable, REDIS_PORT Environment Variable (+2 more)

### Community 8 - "Community 8"
Cohesion: 0.16
Nodes (4): NotificationFanOutConsumer, SubscriptionController, CreatorTierService, SubscriptionService

### Community 9 - "Community 9"
Cohesion: 0.29
Nodes (9): appendComment(), appendToFeed(), appendUserEvent(), connect(), formatTime(), join(), params, reconnect() (+1 more)

### Community 13 - "Community 13"
Cohesion: 0.29
Nodes (3): NotificationController, NotificationRepository, NotificationService

### Community 14 - "Community 14"
Cohesion: 0.33
Nodes (5): CommentEntity, LinkEntity, NotificationEntity, SubscriptionEntity, VideoEntity

### Community 18 - "Community 18"
Cohesion: 0.5
Nodes (4): LinkExpiredException, LinkNotFoundException, VideoNotFoundException, RuntimeException

### Community 20 - "Community 20"
Cohesion: 0.5
Nodes (3): EmailNotificationConsumer, InAppNotificationConsumer, PushNotificationConsumer

## Knowledge Gaps
- **41 isolated node(s):** `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto`, `VideoEntity`, `HealthController` (+36 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **27 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `VideoController` connect `Community 1` to `Community 3`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Why does `LinkService` connect `Community 1` to `Community 7`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Redis (Docker Service)` (e.g. with `Cache-Aside Pattern` and `Sliding Window Rate Limiting via Redis`) actually correct?**
  _`Redis (Docker Service)` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto` to the rest of the system?**
  _41 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._