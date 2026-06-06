package kavin.personal_project.streambase.service;

import kavin.personal_project.streambase.dto.CreateLinkRequest;
import kavin.personal_project.streambase.dto.LinkDto;
import kavin.personal_project.streambase.entity.LinkEntity;
import kavin.personal_project.streambase.exception.LinkExpiredException;
import kavin.personal_project.streambase.exception.LinkNotFoundException;
import kavin.personal_project.streambase.exception.VideoNotFoundException;
import kavin.personal_project.streambase.repository.LinkRepository;
import kavin.personal_project.streambase.repository.VideoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class LinkService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String BASE_URL = "http://localhost/s/";

    private final VideoRepository videoRepository;
    private final LinkRepository linkRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public LinkDto createLink(CreateLinkRequest request) {

        if (!videoRepository.existsById(request.getVideoId())) {
            throw new VideoNotFoundException();
        }

        LinkEntity link = LinkEntity.builder()
                .videoId(request.getVideoId())
                .expiresAt(LocalDateTime.now().plusMonths(request.getValidityInMonths()))
                .prefix(request.getCustomPrefix())
                .clickCount(0L)
                .build();

        link = linkRepository.save(link);

        String code = toBase62(link.getId());

        if (request.getCustomPrefix() != null && !request.getCustomPrefix().isBlank()) {
            code = request.getCustomPrefix() + "-" + code;
        }

        link.setCode(code);
        linkRepository.save(link);

        return toDto(link);
    }

    private String toBase62(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    private LinkDto toDto(LinkEntity entity) {

        LinkDto dto = new LinkDto();
        dto.setCode(entity.getCode());
        dto.setClickCount(entity.getClickCount());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setVideoId(entity.getVideoId());
        dto.setShortUrl(BASE_URL + entity.getCode());
        return dto;
    }

    @Transactional
    public String redirect(String code) {

        String url = redisTemplate.opsForValue().get("link:" + code);

        if (url == null) {
            LinkEntity link = linkRepository.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));

            if (link.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new LinkExpiredException(code);
            }

            url = "/api/videos/" + link.getVideoId();
            Long ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), link.getExpiresAt());
            redisTemplate.opsForValue().set("link:" + code, url, ttlSeconds, TimeUnit.SECONDS);
        }

        redisTemplate.opsForValue().increment("clicks:" + code);

        return url;
    }

    @Transactional
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void flushClickCounts() {

        Set<String> keys = redisTemplate.keys("clicks:*");
        if(keys == null) return;

        for (String key : keys) {
            String code = key.substring("clicks:".length());
            String value = redisTemplate.opsForValue().getAndDelete(key);

            if(value != null) {
                long count = Long.parseLong(value);
                linkRepository.incrementClickCount(code, count);
            }
        }
    }
}
