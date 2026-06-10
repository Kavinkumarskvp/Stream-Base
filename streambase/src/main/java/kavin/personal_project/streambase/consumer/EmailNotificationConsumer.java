package kavin.personal_project.streambase.consumer;

import kavin.personal_project.streambase.event.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationConsumer {

    @KafkaListener(topics = "notification.mail",
            groupId = "notification-email",
    containerFactory = "notificationConcurrentKafkaListenerContainerFactory")
    public void handle(NotificationEvent event) throws InterruptedException {

        log.info("EMAIL → to={} video='{}' creator={}",
                event.subscriberId(),
                event.videoTitle(),
                event.creatorId()
        );

        Thread.sleep(500); //Simulate email API call

        log.info("EMAIL sent to {}", event.subscriberId());
    }
}
