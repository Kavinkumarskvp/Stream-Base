# Graph Report - .  (2026-06-10)

## Corpus Check
- 3 files · ~0 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 187 nodes · 202 edges · 35 communities (13 shown, 22 thin omitted)
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 30 edges (avg confidence: 0.84)
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
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]

## God Nodes (most connected - your core abstractions)
1. `KafkaConfig` - 17 edges
2. `StreamBase System Design Learning Project` - 10 edges
3. `Redis (Docker Service)` - 8 edges
4. `RateLimiterInterceptor` - 7 edges
5. `VideoController` - 6 edges
6. `postgres-primary (Docker Service)` - 6 edges
7. `application-dev.yaml (Dev Profile Config)` - 6 edges
8. `Primary DataSource` - 6 edges
9. `Replica DataSource` - 6 edges
10. `LinkService` - 6 edges

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

## Communities (35 total, 22 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.12
Nodes (27): application-dev.yaml (Dev Profile Config), application.yaml (Base Config), application.yaml (Spring Boot Config), open-in-view: false, Cache-Aside Pattern, Consistent Hashing for DB Sharding, Database Read Replication, Day 4 Database Replication Completed (+19 more)

### Community 1 - "Community 1"
Cohesion: 0.18
Nodes (4): VideoController, NotificationMapper, VideoMapper, LinkService

### Community 3 - "Community 3"
Cohesion: 0.17
Nodes (10): DB_PASSWORD Environment Variable, DB_REPLICA_URL Environment Variable, DB_URL Environment Variable, DB_USERNAME Environment Variable, Read Replica Pattern, Redis Configuration, REDIS_HOST Environment Variable, REDIS_PORT Environment Variable (+2 more)

### Community 7 - "Community 7"
Cohesion: 0.25
Nodes (4): NotificationFanOutConsumer, SubscriptionController, SubscriptionRepository, SubscriptionService

### Community 8 - "Community 8"
Cohesion: 0.29
Nodes (3): NotificationController, NotificationRepository, NotificationService

### Community 11 - "Community 11"
Cohesion: 0.4
Nodes (4): LinkEntity, NotificationEntity, SubscriptionEntity, VideoEntity

### Community 12 - "Community 12"
Cohesion: 0.5
Nodes (4): LinkExpiredException, LinkNotFoundException, VideoNotFoundException, RuntimeException

### Community 14 - "Community 14"
Cohesion: 0.5
Nodes (3): EmailNotificationConsumer, InAppNotificationConsumer, PushNotificationConsumer

## Knowledge Gaps
- **38 isolated node(s):** `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto`, `VideoRepository`, `VideoEntity` (+33 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LinkService` connect `Community 1` to `Community 4`?**
  _High betweenness centrality (0.015) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Redis (Docker Service)` (e.g. with `Cache-Aside Pattern` and `Sliding Window Rate Limiting via Redis`) actually correct?**
  _`Redis (Docker Service)` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto` to the rest of the system?**
  _38 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._