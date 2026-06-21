package kavin.personal_project.streambase.controller;

import kavin.personal_project.streambase.sharding.ShardRouter;
import kavin.personal_project.streambase.sharding.SnowFlakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sharded-videos")
public class ShardedVideoController {

    private final ShardRouter shardRouter;
    private final SnowFlakeIdGenerator snowflakeGenerator;

    @PostMapping
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {

        long id = snowflakeGenerator.nextId();
        String title = (String) body.getOrDefault("title", "Untitled");

        shardRouter.save(id, title);
        return Map.of(
                "id", id,
                "title", title,
                "shard", shardRouter.shardFor(id)
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable Long id) {

        return shardRouter.findById(id);
    }

    @GetMapping("/distribution")
    public Map<String, Long> distribution() {

        return shardRouter.distribution();
    }

    @PostMapping("/rebalance")
    public Map<String, Object> rebalance() {
        return shardRouter.rebalance();
    }
}
