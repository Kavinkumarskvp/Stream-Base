# Graph Report - .  (2026-05-29)

## Corpus Check
- 13 files · ~0 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 102 nodes · 119 edges · 19 communities (4 shown, 15 thin omitted)
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 21 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Kafka Event Pipeline|Kafka Event Pipeline]]
- [[_COMMUNITY_Video CRUD & Caching|Video CRUD & Caching]]
- [[_COMMUNITY_Database Routing|Database Routing]]
- [[_COMMUNITY_Docker Infrastructure|Docker Infrastructure]]
- [[_COMMUNITY_Data Mapping Layer|Data Mapping Layer]]
- [[_COMMUNITY_Entity & Schema|Entity & Schema]]
- [[_COMMUNITY_Error Handling|Error Handling]]
- [[_COMMUNITY_Health Monitoring|Health Monitoring]]
- [[_COMMUNITY_Load Balancing|Load Balancing]]
- [[_COMMUNITY_Project Config|Project Config]]
- [[_COMMUNITY_Consumer Processing|Consumer Processing]]
- [[_COMMUNITY_Replica Failover|Replica Failover]]
- [[_COMMUNITY_Serialization|Serialization]]
- [[_COMMUNITY_Service Layer|Service Layer]]
- [[_COMMUNITY_Repository Layer|Repository Layer]]
- [[_COMMUNITY_Security|Security]]
- [[_COMMUNITY_API Controllers|API Controllers]]
- [[_COMMUNITY_Migration Scripts|Migration Scripts]]

## God Nodes (most connected - your core abstractions)
1. `StreamBase System Design Learning Project` - 10 edges
2. `Redis (Docker Service)` - 8 edges
3. `KafkaConfig` - 8 edges
4. `VideoController` - 6 edges
5. `postgres-primary (Docker Service)` - 6 edges
6. `application-dev.yaml (Dev Profile Config)` - 6 edges
7. `Primary DataSource` - 6 edges
8. `Replica DataSource` - 6 edges
9. `app-1 (Spring Boot Instance)` - 5 edges
10. `VideoMapper` - 4 edges

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

## Communities (19 total, 15 thin omitted)

### Community 0 - "Kafka Event Pipeline"
Cohesion: 0.21
Nodes (18): application-dev.yaml (Dev Profile Config), application.yaml (Spring Boot Config), Cache-Aside Pattern, Consistent Hashing for DB Sharding, Dead-Letter Topic (DLQ) for Failed Processing, Fan-Out on Read vs Write, Flyway SQL Migrations Convention, Horizontal Scaling — Stateless API Behind Load Balancer (+10 more)

### Community 1 - "Video CRUD & Caching"
Cohesion: 0.17
Nodes (10): DB_PASSWORD Environment Variable, DB_REPLICA_URL Environment Variable, DB_URL Environment Variable, DB_USERNAME Environment Variable, Read Replica Pattern, Redis Configuration, REDIS_HOST Environment Variable, REDIS_PORT Environment Variable (+2 more)

### Community 3 - "Docker Infrastructure"
Cohesion: 0.22
Nodes (9): application.yaml (Base Config), open-in-view: false, Database Read Replication, Day 4 Database Replication Completed, LazyConnectionDataSourceProxy Pattern, Debug Port 5005 (JDWP), JAVA_TOOL_OPTIONS Debug Agent, postgres-replica (Docker Service) (+1 more)

## Knowledge Gaps
- **17 isolated node(s):** `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto`, `VideoRepository`, `VideoEntity` (+12 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `StreamBase System Design Learning Project` connect `Kafka Event Pipeline` to `Docker Infrastructure`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Why does `postgres-replica (Docker Service)` connect `Docker Infrastructure` to `Kafka Event Pipeline`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Why does `postgres-primary (Docker Service)` connect `Kafka Event Pipeline` to `Docker Infrastructure`?**
  _High betweenness centrality (0.015) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Redis (Docker Service)` (e.g. with `Cache-Aside Pattern` and `Sliding Window Rate Limiting via Redis`) actually correct?**
  _`Redis (Docker Service)` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `UpdateVideoRequest`, `CreateVideoRequest`, `VideoDto` to the rest of the system?**
  _17 weakly-connected nodes found - possible documentation gaps or missing edges._