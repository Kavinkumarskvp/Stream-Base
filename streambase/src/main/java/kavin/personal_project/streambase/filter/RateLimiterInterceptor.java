package kavin.personal_project.streambase.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kavin.personal_project.streambase.config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final DefaultRedisScript<List> slidingWindowScript;
    private final RateLimiterProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (!properties.isEnabled() || isExcluded(request.getRequestURI())) {
            return true;
        }

        long now = Instant.now().getEpochSecond();

        // 1. Global IP limit
        String clientIp = resolveClientIp(request);
        if (!check(request, response, "ratelimit:ip", clientIp, properties.getIp().getLimit(), properties.getIp().getWindowSeconds(), now)) {
            return false;
        }

        if (isUpload(request)) {
            String userId = resolveUserId(request);
            if (!check(request, response, "ratelimit:upload", userId,
                    properties.getUpload().getLimit(),
                    properties.getUpload().getWindowSeconds(), now)) {
                return false;
            }
        }
        return true;
    }

    private boolean isExcluded(String uri) {
        return properties.getExcludedPaths().stream().anyMatch(path -> pathMatcher.match(path, uri));
    }

    private String resolveClientIp(HttpServletRequest request) {

        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri.trim();
        }

        return request.getRemoteAddr();
    }

    private boolean isUpload(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().equals("/api/videos");
    }

    private String resolveUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        return (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
    }

    private boolean check(HttpServletRequest request, HttpServletResponse response, String keyPrefix, String identifier, int limit, long window, long now) {
        long currentWindow = now / window;
        long previousWindow = currentWindow - 1;
        long elapsed = now % window;

        List<String> keys = List.of(
                keyPrefix + ":" + identifier + ":" + currentWindow,
                keyPrefix + ":" + identifier + ":" + previousWindow
        );

        try {
            List<Long> result = redisTemplate.execute(
                    slidingWindowScript,
                    keys,
                    String.valueOf(limit)
                    , String.valueOf(window)
                    , String.valueOf(elapsed)
            );

            long allowed = result.get(0);
            long count = result.get(1);
            long retryAfter = result.get(2);

            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - count)));

            if (allowed == 0L) {
                log.warn("Rate limit exceeded: ip={} path={} count={}", identifier, request.getRequestURI(), count);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{" + "\"error\":\"Rate limit exceeded\"," + "\"retryAfter\":" + retryAfter + "}");
                return false;
            }
            return true;
        } catch (Exception e) {
            // Fail-open: never let rate limiter outages takedown the API
            log.warn("Rate limiter unavailable, allowing request: {}", e.getMessage());
            return true;
        }
    }
}
