package kavin.personal_project.streambase.consumer;

import kavin.personal_project.streambase.entity.NotificationEntity;
import kavin.personal_project.streambase.event.NotificationEvent;
import kavin.personal_project.streambase.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationConsumer {

    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "notification.in_app",
            groupId = "notification-in-app",
            containerFactory = "notificationConcurrentKafkaListenerContainerFactory")
    public void handle(NotificationEvent event) {
        NotificationEntity entity = NotificationEntity.builder()
                .subscriberId(event.subscriberId())
                .creatorId(event.creatorId())
                .videoId(event.videoId())
                .videoTitle(event.videoTitle())
                .isRead(false)
                .build();

        notificationRepository.save(entity);
        log.info("IN_APP saved for {} — video={}",
                event.subscriberId(),
                event.videoId());
    }
}
