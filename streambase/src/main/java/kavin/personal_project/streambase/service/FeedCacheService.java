package kavin.personal_project.streambase.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedCacheService {

    private static final String CB = "redis";
    private static final int FEED_MAX_SIZE = 1000;
    private static final Duration FEED_TTL = Duration.ofDays(30);

    private final StringRedisTemplate redisTemplate;

    @CircuitBreaker(
            name = CB,
            fallbackMethod = "addToFeedFallback")
    public void addToFeed(String userId, Long videoId, long timestamp) {
        String key = feedKey(userId);
        redisTemplate.opsForZSet().add(
                key,
                String.valueOf(videoId),
                timestamp);

        // Keep the newest FEED_MAX_SIZE entries (remove from bottom)
        redisTemplate.opsForZSet().removeRange(
                key,
                0,
                -(FEED_MAX_SIZE + 1));
        redisTemplate.expire(key, FEED_TTL);
    }

    private void addToFeedFallback(String userId, Long videoId, long timestamp, Throwable t) {
        log.warn("CB fallback addToFeed(user={} video={}): {}",
                userId,
                videoId,
                t.getMessage());
    }

    @CircuitBreaker(
            name = CB,
            fallbackMethod = "getFeedVideoIdsFallback")
    public Set<String> getFeedVideoIds(String userId, int limit) {
        // Newest first; we stored timestamp as score, so reverse range
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(
                feedKey(userId),
                0,
                limit - 1);
        return ids == null ?
                Collections.emptySet() :
                ids;
    }

    private Set<String> getFeedVideoIdsFallback(String userId, int limit, Throwable t) {
        log.warn("CB fallback getFeed(user={}): {}",
                userId,
                t.getMessage());
        return Collections.emptySet();
    }

    private String feedKey(String userId) {
        return "feed:" + userId;
    }
}
