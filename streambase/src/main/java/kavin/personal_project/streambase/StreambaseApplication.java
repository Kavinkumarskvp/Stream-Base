package kavin.personal_project.streambase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableKafka
@EnableWebSocket
public class StreambaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreambaseApplication.class, args);
    }

}