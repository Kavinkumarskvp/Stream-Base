package kavin.personal_project.streambase.consumer;

import kavin.personal_project.streambase.event.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushNotificationConsumer {

    @KafkaListener(topics = "notification.push",
            groupId = "notification-push",
            containerFactory = "notificationConcurrentKafkaListenerContainerFactory")
    public void handle(NotificationEvent event) throws InterruptedException {

        log.info("PUSH → to={} video='{}'",
                event.subscriberId(),
                event.videoTitle());

        Thread.sleep(200);  // simulate push provider call

        log.info("PUSH sent to {}", event.subscriberId());
    }
}
