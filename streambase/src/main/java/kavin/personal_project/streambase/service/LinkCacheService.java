package kavin.personal_project.streambase.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class LinkCacheService {

    private final String REDIS_CB = "redis";
    private final StringRedisTemplate redisTemplate;

    @CircuitBreaker(name = REDIS_CB,
            fallbackMethod = "getUrlFallback")
    public String getUrl(String code) {
        return redisTemplate.opsForValue().get("link:" + code);
    }

    private String getUrlFallback(String code, Throwable t) {
        log.warn("CB fallback getUrl({}): {}",
                code,
                t.getMessage());
        return null;  // signals cache miss → service will fall through to DB
    }

    @CircuitBreaker(name = REDIS_CB,
            fallbackMethod = "cacheUrlFallback")
    public void cacheUrl(String code,
                         String url,
                         Long ttlSeconds) {

        redisTemplate.opsForValue().set("link:" + code,
                url,
                ttlSeconds,
                TimeUnit.SECONDS);
    }

    private void cacheUrlFallback(String code,
                                  String url,
                                  Long ttlSeconds,
                                  Throwable t) {

        log.warn("CB fallback cacheUrl({}): {}",
                code,
                t.getMessage()); // do nothing — cache write is best-effort
    }


    @CircuitBreaker(name = REDIS_CB,
            fallbackMethod = "incrementClicksFallback")
    public void incrementClicks(String code) {
        redisTemplate.opsForValue().increment("clicks:" + code);
    }

    private void incrementClicksFallback(String code, Throwable t) {
        log.warn("CB fallback incrementClicks({}): {}",
                code,
                t.getMessage()); // do nothing — click counts are approximate
    }
}
