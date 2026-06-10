package kavin.personal_project.streambase.config;

import kavin.personal_project.streambase.event.NotificationEvent;
import kavin.personal_project.streambase.event.VideoPublishedEvent;
import kavin.personal_project.streambase.event.VideoUploadedEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        return new DefaultKafkaProducerFactory<>(config, new
                StringSerializer(), new EventSerializer());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, VideoUploadedEvent> videoUploadedConsumerFactory() {

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "video-processor");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new EventDeserializer<>(VideoUploadedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VideoUploadedEvent> videoUploadedListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, VideoUploadedEvent> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(videoUploadedConsumerFactory());
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, VideoPublishedEvent> videoPublishedConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-fanout");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new EventDeserializer<>(VideoPublishedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VideoPublishedEvent> videoPublishedConcurrentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VideoPublishedEvent> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(videoPublishedConsumerFactory());
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new EventDeserializer<>(NotificationEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> notificationConcurrentKafkaListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(notificationEventConsumerFactory());

        // After max retries → publish to notification.dlt
        DeadLetterPublishingRecoverer dlt = new DeadLetterPublishingRecoverer(kafkaTemplate, ((consumerRecord, e) -> new TopicPartition("notification.dlt", consumerRecord.partition())));

        // Exponential backoff: 1s, 2s, 4s — then DLT after 3 attempts
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxAttempts(3);

        listenerContainerFactory.setCommonErrorHandler(new DefaultErrorHandler(dlt, backOff));
        return listenerContainerFactory;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {

        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic videoUploadedTopic() {
        return TopicBuilder.name("video.uploaded")
                .partitions(1)
                .replicas(1)
                .build();
    }

    //DLT - Dead Letter Topic
    @Bean
    public NewTopic videoUploadedDltTopic() {
        return TopicBuilder.name("video.uploaded.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic videoPublishedTopic() {
        return TopicBuilder.name("video.published")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailNotificationTopic() {
        return TopicBuilder.name("notification.mail")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pushNotificationTopic() {
        return TopicBuilder.name("notification.push")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inAppNotificationTopic() {
        return TopicBuilder.name("notification.in_app")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationDltTopic() {
        return TopicBuilder.name("notification.dlt")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
