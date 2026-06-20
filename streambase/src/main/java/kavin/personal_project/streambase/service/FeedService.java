package kavin.personal_project.streambase.service;

import kavin.personal_project.streambase.dto.VideoDto;
import kavin.personal_project.streambase.entity.VideoEntity;
import kavin.personal_project.streambase.mapper.VideoMapper;
import kavin.personal_project.streambase.repository.SubscriptionRepository;
import kavin.personal_project.streambase.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final SubscriptionRepository subscriptionRepository;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    private final FeedCacheService feedCacheService;
    private final CreatorTierService creatorTierService;

    @Transactional(readOnly = true)
    public List<VideoDto> getFeedPull(String userId) {

        long start = Instant.now().toEpochMilli();

        List<String> creatorIds = subscriptionRepository.findCreatorIdsBySubscriberId(userId);
        if (creatorIds.isEmpty()) {
            log.info("Feed pull: user={} follows nobody, empty feed", userId);
            return Collections.emptyList();
        }

        List<VideoDto> feed = videoRepository.findTop50ByUploadedByInOrderByCreatedAtDesc(creatorIds)
                .stream()
                .map(videoMapper::toDto)
                .toList();

        log.info("Feed pull: user={} follows={} videos={} latency={}ms",
                userId,
                creatorIds.size(),
                feed.size(),
                Instant.now().toEpochMilli() - start);

        return feed;
    }

    @Transactional(readOnly = true)
    public List<VideoDto> getFeedPush(String userId) {

        long start = Instant.now().toEpochMilli();

        // 1. ZSET (push) — normal creators' videos pre-fanned
        Set<String> videoIdStrings = feedCacheService.getFeedVideoIds(userId, 50);
        List<Long> pushIds = videoIdStrings
                .stream()
                .map(Long::parseLong)
                .toList();

        // 2. Pull celebrities the user follows
        List<String> creatorIds = subscriptionRepository.findCreatorIdsBySubscriberId(userId);
        List<String> celebrityIds = creatorIds
                .stream()
                .filter(creatorTierService::isCelebrity)
                .toList();

        // 3. Fetch all videos
        Set<Long> allIds = new LinkedHashSet<>(pushIds);
        List<VideoEntity> celebrityVideos = celebrityIds.isEmpty()
                ? List.of()
                : videoRepository.findTop50ByUploadedByInOrderByCreatedAtDesc(celebrityIds);
        celebrityVideos.forEach(
                v -> allIds.add(v.getId())
        );

        Map<Long, VideoEntity> byId = allIds.isEmpty()
                ? Map.of()
                : videoRepository.findAllById(allIds)
                .stream()
                .collect(Collectors.toMap(VideoEntity::getId, v -> v));

        // 4. Merge + sort by createdAt DESC, limit 50
        List<VideoDto> feed = byId.values()
                .stream()
                .sorted(Comparator.comparing(VideoEntity::getCreatedAt).reversed())
                .limit(50)
                .map(videoMapper::toDto)
                .toList();

        log.info("Feed push (hybrid): user={} push={} celebrities={} merged={} latency={}ms",
                userId,
                pushIds.size(),
                celebrityIds.size(),
                feed.size(),
                Instant.now().toEpochMilli() - start);
        return feed;
    }
}
