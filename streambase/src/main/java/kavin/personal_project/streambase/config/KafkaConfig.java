package kavin.personal_project.streambase.config;

import kavin.personal_project.streambase.event.VideoUploadedEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

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
}
