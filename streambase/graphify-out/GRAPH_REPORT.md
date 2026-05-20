# Graph Report - .  (2026-05-20)

## Corpus Check
- 72 files · ~5,000 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 72 nodes · 84 edges · 16 communities (5 shown, 11 thin omitted)
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 15 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Video API Layer|Video API Layer]]
- [[_COMMUNITY_System Design Concepts|System Design Concepts]]
- [[_COMMUNITY_Dev Config Env Vars|Dev Config Env Vars]]
- [[_COMMUNITY_Infrastructure Config|Infrastructure Config]]
- [[_COMMUNITY_Application Bootstrap|Application Bootstrap]]
- [[_COMMUNITY_Redis Cache Config|Redis Cache Config]]
- [[_COMMUNITY_Error Handling|Error Handling]]
- [[_COMMUNITY_Routing DataSource|Routing DataSource]]
- [[_COMMUNITY_Update Request DTO|Update Request DTO]]
- [[_COMMUNITY_Video Repository|Video Repository]]
- [[_COMMUNITY_Video Entity|Video Entity]]
- [[_COMMUNITY_Health Endpoint|Health Endpoint]]
- [[_COMMUNITY_Custom Exceptions|Custom Exceptions]]
- [[_COMMUNITY_Create Request DTO|Create Request DTO]]
- [[_COMMUNITY_Video Response DTO|Video Response DTO]]

## God Nodes (most connected - your core abstractions)
1. `StreamBase System Design Learning Project` - 10 edges
2. `Redis (Docker Service)` - 8 edges
3. `VideoController` - 6 edges
4. `application-dev.yaml (Dev Profile Config)` - 6 edges
5. `VideoService` - 5 edges
6. `postgres-primary (Docker Service)` - 5 edges
7. `app-1 (Spring Boot Instance)` - 5 edges
8. `Primary DataSource` - 5 edges
9. `Replica DataSource` - 5 edges
10. `VideoMapper` - 4 edges

## Surprising Connections (you probably didn't know these)
- `Redis Pub/Sub for Cross-Server Comments` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Fan-Out on Read vs Write` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `application-dev.yaml (Dev Profile Config)` --references--> `Redis (Docker Service)`  [EXTRACTED]
  src/main/resources/application-dev.yaml → compose.yaml
- `Cache-Aside Pattern` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml
- `Sliding Window Rate Limiting via Redis` --conceptually_related_to--> `Redis (Docker Service)`  [INFERRED]
  CLAUDE.md → compose.yaml

## Communities (16 total, 11 thin omitted)

### Community 0 - "Video API Layer"
Cohesion: 0.26
Nodes (3): VideoController, VideoMapper, VideoService

### Community 1 - "System Design Concepts"
Cohesion: 0.24
Nodes (12): Cache-Aside Pattern, Consistent Hashing for DB Sharding, Database Read Replication, Dead-Letter Topic (DLQ) for Failed Processing, Fan-Out on Read vs Write, Flyway SQL Migrations Convention, Kafka Chosen Over RabbitMQ, Sliding Window Rate Limiting via Redis (+4 more)

### Community 2 - "Dev Config Env Vars"
Cohesion: 0.24
Nodes (10): DB_PASSWORD Environment Variable, DB_REPLICA_URL Environment Variable, DB_URL Environment Variable, DB_USERNAME Environment Variable, Read Replica Pattern, Redis Configuration, REDIS_HOST Environment Variable, REDIS_PORT Environment Variable (+2 more)

### Community 3 - "Infrastructure Config"
Cohesion: 0.46
Nodes (8): application-dev.yaml (Dev Profile Config), application.yaml (Spring Boot Config), Horizontal Scaling — Stateless API Behind Load Balancer, app-1 (Spring Boot Instance), app-2 (Spring Boot Instance), app-3 (Spring Boot Instance), Nginx (Load Balancer Service), postgres-primary (Docker Service)

## Knowledge Gaps
- **14 isolated node(s):** `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto`, `VideoRepository`, `VideoEntity` (+9 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `StreamBase System Design Learning Project` connect `System Design Concepts` to `Infrastructure Config`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **Why does `Redis (Docker Service)` connect `System Design Concepts` to `Infrastructure Config`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Redis (Docker Service)` (e.g. with `Cache-Aside Pattern` and `Sliding Window Rate Limiting via Redis`) actually correct?**
  _`Redis (Docker Service)` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto` to the rest of the system?**
  _14 weakly-connected nodes found - possible documentation gaps or missing edges._