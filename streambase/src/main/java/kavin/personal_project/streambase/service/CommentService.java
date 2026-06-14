package kavin.personal_project.streambase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import kavin.personal_project.streambase.dto.CommentDto;
import kavin.personal_project.streambase.entity.CommentEntity;
import kavin.personal_project.streambase.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int RECENT_LIMIT = 50;
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final CommentRepository commentRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CommentDto saveComment(Long videoId, String userId, String text) {
        CommentEntity entity = CommentEntity.builder()
                .videoId(videoId)
                .userId(userId)
                .text(text)
                .build();

        entity = commentRepository.save(entity);

        CommentDto dto = toDto(entity);

        // Cache the comment in Redis list (best-effort; if Redis is down, no harm)
        try {
            String json = objectMapper.writeValueAsString(dto);
            String key = recentKey(videoId);
            redisTemplate.opsForList().leftPush(key, json);
            redisTemplate.opsForList().trim(key, 0, RECENT_LIMIT - 1);
            redisTemplate.expire(key, CACHE_TTL);

        } catch (Exception e) {
            log.warn("Failed to cache comment in Redis: {}",
                    e.getMessage());
        }
        return dto;
    }

    @CircuitBreaker(name = "redis",
            fallbackMethod = "getRecentCommentsFallback")
    public List<CommentDto> getRecentComments(Long videoId) {

        String key = recentKey(videoId);
        List<String> cached = redisTemplate.opsForList().range(key, 0, RECENT_LIMIT - 1);
        if (cached != null && !cached.isEmpty()) {
            List<CommentDto> comments = new ArrayList<>(cached.size());

            for (String json : cached) {
                try {
                    comments.add(objectMapper.readValue(json, CommentDto.class));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to deserialize comment from Redis, skipping: {}", e.getMessage());
                }
            }
            Collections.reverse(comments);
            return comments;
        }

        //Cache miss — Redis healthy, data just not there
        return fetchFromDbAndWarmCache(videoId);
    }

    private List<CommentDto> getRecentCommentsFallback(Long videoId, Throwable t) {

        log.warn("Redis down, falling back to DB: {}", t.getMessage());
        return fetchFromDb(videoId);
    }

    private List<CommentDto> fetchFromDbAndWarmCache(Long videoId) {

        List<CommentDto> comments = fetchFromDb(videoId);

        try {
            for (int i = comments.size() - 1; i >= 0; i--) {  //push oldest first so list ends up newest-first

                redisTemplate.opsForList().rightPush(recentKey(videoId),
                        objectMapper.writeValueAsString(comments.get(i)));
            }
            redisTemplate.expire(recentKey(videoId), CACHE_TTL);

        } catch (Exception e) {
            log.warn("Failed to warm Redis cache: {}",
                    e.getMessage());
        }

        return comments;
    }

    private List<CommentDto> fetchFromDb(Long videoId) {

        return commentRepository.findTop50ByVideoIdOrderByCreatedAtDesc(videoId)
                .stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(CommentDto::getTimestamp))
                .toList();
    }

    private String recentKey(Long videoId) {
        return "comments:" + videoId + ":recent";
    }

    private CommentDto toDto(CommentEntity e) {
        return CommentDto.builder()
                .id(e.getId())
                .videoId(e.getVideoId())
                .userId(e.getUserId())
                .text(e.getText())
                .timestamp(e.getCreatedAt())
                .build();
    }
}
