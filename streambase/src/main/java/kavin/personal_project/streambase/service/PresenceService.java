package kavin.personal_project.streambase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final static Duration STALE_AFTER = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;

    public void heartbeat(Long videoId, String userId) {

        if (userId == null) return;

        String key = key(videoId);
        double now = Instant.now().toEpochMilli();
        try {
            redisTemplate.opsForZSet().add(key, userId, now);
            redisTemplate.expire(key, Duration.ofMinutes(5));

        } catch (Exception e) {
            log.warn("Failed to record heartbeat for {} on video {}: {}",
                    userId, videoId,
                    e.getMessage());
        }
    }

    public void leave(Long videoId, String userId) {
        if (userId == null) return;

        try {
            redisTemplate.opsForZSet().remove(key(videoId), userId);

        } catch (Exception e) {
            log.warn("Failed to remove viewer {} from video {}: {}",
                    userId,
                    videoId,
                    e.getMessage());
        }
    }

    public long activeViewerCount(Long videoId) {

        String key = key(videoId);
        try {
            double cutOff = Instant.now().toEpochMilli() - STALE_AFTER.toMillis();
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, cutOff);
            Long count = redisTemplate.opsForZSet().zCard(key);
            return count != null ? count : 0;

        } catch (Exception e) {
            log.warn("Failed to read viewer count for video {}: {}",
                    videoId,
                    e.getMessage());
            return 0;
        }
    }

    private String key(Long videoId) {
        return "viewers:" + videoId;
    }
}
