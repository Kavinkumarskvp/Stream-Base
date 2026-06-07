package kavin.personal_project.streambase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class RateLimiterConfig {

    @Bean
    public DefaultRedisScript<List> slidingWindowScript() {

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/sliding_window_counter.lua"));
        script.setResultType(List.class);
        return script;
    }
}
