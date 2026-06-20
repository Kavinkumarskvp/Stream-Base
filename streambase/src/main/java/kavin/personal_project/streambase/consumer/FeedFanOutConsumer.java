package kavin.personal_project.streambase.consumer;

import kavin.personal_project.streambase.entity.SubscriptionEntity;
import kavin.personal_project.streambase.event.VideoPublishedEvent;
import kavin.personal_project.streambase.repository.SubscriptionRepository;
import kavin.personal_project.streambase.service.CreatorTierService;
import kavin.personal_project.streambase.service.FeedCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedFanOutConsumer {

    private final SubscriptionRepository subscriptionRepository;
    private final FeedCacheService feedCacheService;
    private final CreatorTierService creatorTierService;

    @KafkaListener(
            topics = "video.published",
            groupId = "feed-fanout",
            containerFactory = "videoPublishedConcurrentKafkaListenerContainerFactory"
    )
    public void fanOut(VideoPublishedEvent event) {

        long start = Instant.now().toEpochMilli();

        if(creatorTierService.isCelebrity(event.creatorId())) {

            log.info("Feed fan-out SKIPPED for celebrity creator={} videoId={}",
                    event.creatorId(),
                    event.videoId()
            );
            return;
        }

        List<SubscriptionEntity> followers = subscriptionRepository.findByCreatorId(event.creatorId());
        for (SubscriptionEntity follower : followers) {
            feedCacheService.addToFeed(
                    follower.getSubscriberId(),
                    event.videoId(),
                    event.publishedAt()
            );
        }

        log.info("Feed fan-out: videoId={} creator={} followers={} duration={}ms",
                event.videoId(),
                event.creatorId(),
                followers.size(),
                Instant.now().toEpochMilli() - start
        );
    }
}
