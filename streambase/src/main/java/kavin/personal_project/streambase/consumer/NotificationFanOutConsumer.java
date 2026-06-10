package kavin.personal_project.streambase.consumer;

import kavin.personal_project.streambase.entity.SubscriptionEntity;
import kavin.personal_project.streambase.event.NotificationEvent;
import kavin.personal_project.streambase.event.VideoPublishedEvent;
import kavin.personal_project.streambase.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFanOutConsumer {

    private final SubscriptionRepository subscriptionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "video.published",
            groupId = "notification-fanout",
            containerFactory = "videoPublishedConcurrentKafkaListenerContainerFactory")
    public void fanOut(VideoPublishedEvent event) {
        log.info("Fan-out triggered: videoId={} creator={}",
                event.videoId(),
                event.creatorId());

        List<SubscriptionEntity> subscriptions = subscriptionRepository.findByCreatorId(event.creatorId());
        log.info("Fan-out: {} subscribers found for creator {}", subscriptions.size(), event.creatorId());

        subscriptions.forEach(subscription -> {
            NotificationEvent notificationEvent = new NotificationEvent(
                    subscription.getSubscriberId(),
                    subscription.getCreatorId(),
                    event.videoId(),
                    event.videoTitle()
            );

            kafkaTemplate.send("notification.mail", notificationEvent);
            kafkaTemplate.send("notification.push", notificationEvent);
            kafkaTemplate.send("notification.in_app", notificationEvent);
        });

        log.info("Fan-out completed: {} subscribers × 3 channels = {} messages published",
                subscriptions.size(),
                subscriptions.size() * 3);

    }
}
