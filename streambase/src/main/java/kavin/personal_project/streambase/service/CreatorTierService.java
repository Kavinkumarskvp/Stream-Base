package kavin.personal_project.streambase.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import kavin.personal_project.streambase.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreatorTierService {

    private static final String CB = "redis";
    private static final long CELEBRITY_THRESHOLD = 10_000L;
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final SubscriptionRepository subscriptionRepository;
    private final StringRedisTemplate redisTemplate;

    public boolean isCelebrity(String creatorId) {
        return getFollowerCount(creatorId) >= CELEBRITY_THRESHOLD;
    }

    @CircuitBreaker(
            name = CB,
            fallbackMethod = "getFollowerCountFallback"
    )
    public long getFollowerCount(String creatorId) {

        String key = "creator:followers:" + creatorId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        long count = subscriptionRepository.countByCreatorId(creatorId);
        redisTemplate.opsForValue().set(key, String.valueOf(count), CACHE_TTL);
        return count;
    }

    private long getFollowerCountFallback(String creatorId, Throwable t) {
        log.warn("CB fallback getFollowerCount({}): {}",
                creatorId,
                t.getMessage());
        return subscriptionRepository.countByCreatorId(creatorId);
    }
}
