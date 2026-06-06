# Graph Report - .  (2026-06-06)

## Corpus Check
- 10 files · ~0 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 122 nodes · 141 edges · 22 communities (6 shown, 16 thin omitted)
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 23 edges (avg confidence: 0.85)
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
- [[_COMMUNITY_Community 21|Community 21]]

## God Nodes (most connected - your core abstractions)
1. `StreamBase System Design Learning Project` - 10 edges
2. `Redis (Docker Service)` - 8 edges
3. `KafkaConfig` - 8 edges
4. `VideoController` - 6 edges
5. `postgres-primary (Docker Service)` - 6 edges
6. `application-dev.yaml (Dev Profile Config)` - 6 edges
7. `Primary DataSource` - 6 edges
8. `Replica DataSource` - 6 edges
9. `LinkService` - 6 edges
10. `app-1 (Spring Boot Instance)` - 5 edges

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

## Communities (22 total, 16 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.21
Nodes (18): application-dev.yaml (Dev Profile Config), application.yaml (Spring Boot Config), Cache-Aside Pattern, Consistent Hashing for DB Sharding, Dead-Letter Topic (DLQ) for Failed Processing, Fan-Out on Read vs Write, Flyway SQL Migrations Convention, Horizontal Scaling — Stateless API Behind Load Balancer (+10 more)

### Community 1 - "Community 1"
Cohesion: 0.19
Nodes (3): VideoController, VideoMapper, LinkService

### Community 2 - "Community 2"
Cohesion: 0.17
Nodes (10): DB_PASSWORD Environment Variable, DB_REPLICA_URL Environment Variable, DB_URL Environment Variable, DB_USERNAME Environment Variable, Read Replica Pattern, Redis Configuration, REDIS_HOST Environment Variable, REDIS_PORT Environment Variable (+2 more)

### Community 3 - "Community 3"
Cohesion: 0.22
Nodes (9): application.yaml (Base Config), open-in-view: false, Database Read Replication, Day 4 Database Replication Completed, LazyConnectionDataSourceProxy Pattern, Debug Port 5005 (JDWP), JAVA_TOOL_OPTIONS Debug Agent, postgres-replica (Docker Service) (+1 more)

### Community 9 - "Community 9"
Cohesion: 0.5
Nodes (4): LinkExpiredException, LinkNotFoundException, VideoNotFoundException, RuntimeException

## Knowledge Gaps
- **22 isolated node(s):** `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto`, `VideoRepository`, `VideoEntity` (+17 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `StreamBase System Design Learning Project` connect `Community 0` to `Community 3`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **Why does `LinkService` connect `Community 1` to `Community 6`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **Why does `postgres-replica (Docker Service)` connect `Community 3` to `Community 0`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Redis (Docker Service)` (e.g. with `Cache-Aside Pattern` and `Sliding Window Rate Limiting via Redis`) actually correct?**
  _`Redis (Docker Service)` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto` to the rest of the system?**
  _22 weakly-connected nodes found - possible documentation gaps or missing edges._