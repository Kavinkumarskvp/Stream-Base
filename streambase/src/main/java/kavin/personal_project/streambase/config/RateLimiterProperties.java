package kavin.personal_project.streambase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimiterProperties {

    private boolean enabled = true;
    private Bucket ip = new Bucket();
    private Bucket upload = new Bucket();
    private List<String> excludedPaths = new ArrayList<>();

    @Data
    public static class Bucket {
        private int limit = 100;
        private long windowSeconds = 60;
    }
}
