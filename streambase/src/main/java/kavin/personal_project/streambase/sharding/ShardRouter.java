package kavin.personal_project.streambase.sharding;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ShardRouter {

    private static final int VIRTUAL_NODES_PER_SHARD = 100;

    private final ConsistentHashRing ring;
    private final Map<String, JdbcTemplate> templates;

    public ShardRouter(ShardJdbcTemplates wrapper) {

        this.templates = wrapper.templates();
        this.ring = new ConsistentHashRing(VIRTUAL_NODES_PER_SHARD);

        templates.keySet().forEach(ring::addShard);
        log.info("ShardRouter initialized with shards: {}",
                templates.keySet()
        );
    }

    @PostConstruct
    public void initSchema() {
        templates.forEach((name, jdbc) -> {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS sharded_videos (
                        id BIGINT PRIMARY KEY,
                        title TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT NOW()
                    )
                    """);
            log.info("Schema ready on {}", name);
        });
    }

    public String shardFor(Long videoId) {
        return ring.getShard(String.valueOf(videoId));
    }

    public void save(Long videoId, String title) {
        String shard = shardFor(videoId);
        templates.get(shard).update(
                "INSERT INTO sharded_videos (id, title) VALUES(?, ?) ON CONFLICT (id) DO NOTHING",
                videoId,
                title
        );
        log.debug("Saved videoId={} to {}", videoId, shard);
    }

    public Map<String, Object> findById(Long videoId) {

        String shard = shardFor(videoId);
        try {

            Map<String, Object> row = templates.get(shard).queryForMap(
                    "SELECT id, title, created_at FROM sharded_videos WHERE id=?",
                    videoId
            );
            row.put("_shard", shard);
            return row;
        } catch (Exception e) {
            return Map.of(
                    "_shard", shard,
                    "found", false
            );
        }
    }

    public Map<String, Long> distribution() {

        Map<String, Long> counts = new LinkedHashMap<>();
        templates.forEach(
                (name, jdbc) -> {
                    Long count = jdbc.queryForObject(
                            "SELECT COUNT(*) FROM sharded_videos",
                            Long.class
                    );
                    counts.put(name, count == null ? 0L : count);
                }
        );
        return counts;
    }

    public Map<String, Object> rebalance() {

        long total = 0;
        long moved = 0;
        Map<String, Long> movedOutOf = new LinkedHashMap<>();

        // Snapshot all rows with their current shard
        List<Object[]> allRows = new ArrayList<>();
        templates.forEach(
                (shardName, jdbc) -> {
                    jdbc.queryForList(
                            "SELECT id, title FROM sharded_videos"
                    ).forEach(
                            row -> {
                                Long id = ((Number) row.get("id")).longValue();
                                String title = (String) row.get("title");
                                allRows.add(new Object[]{
                                        id,
                                        title,
                                        shardName
                                });
                            }
                    );
                }
        );
        total = allRows.size();

        for (Object[] row : allRows) {

            Long id = (Long) row[0];
            String title = (String) row[1];
            String currentShard = (String) row[2];
            String correctShard = ring.getShard((String.valueOf(id)));

            if (!correctShard.equals(currentShard)) {

                templates.get(correctShard).update(
                        "INSERT INTO sharded_videos (id, title) VALUES (?, ?) ON CONFLICT (id) DO NOTHING",
                        id,
                        title
                );
                templates.get(currentShard).update(
                        "DELETE FROM sharded_videos WHERE id = ?",
                        id
                );
                moved++;
                movedOutOf.merge(currentShard, 1L, Long::sum);
            }
        }

        double ratio = total == 0 ? 0.0 : (double) moved / total;
        log.info("Rebalance complete: total={} moved={} ratio={}",
                total,
                moved,
                ratio
        );

        return Map.of(
                "total", total,
                "moved", moved,
                "moveRatio", ratio,
                "movedOutOf", movedOutOf,
                "newDistribution", distribution()
        );
    }
}
