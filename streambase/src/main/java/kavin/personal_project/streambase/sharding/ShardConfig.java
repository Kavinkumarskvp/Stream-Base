package kavin.personal_project.streambase.sharding;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShardConfig {

    @Value("${SHARD_1_URL}")
    private String shard1Url;

    @Value("${SHARD_2_URL}")
    private String shard2Url;

    @Value("${SHARD_3_URL}")
    private String shard3Url;

    @Value("${SHARD_DB_USER}")
    private String shardUser;

    @Value("${SHARD_DB_PASSWORD}")
    private String shardPassword;

    @Bean
    public ShardJdbcTemplates shardJdbcTemplates() {

        Map<String, JdbcTemplate> map = new LinkedHashMap<>();
        map.put("shard-1", new JdbcTemplate(buildDataSource(shard1Url)));
        map.put("shard-2", new JdbcTemplate(buildDataSource(shard2Url)));
        map.put("shard-3", new JdbcTemplate(buildDataSource(shard3Url)));

        return new ShardJdbcTemplates(map);
    }

    private DataSource buildDataSource(String url) {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(shardUser);
        hikariConfig.setPassword(shardPassword);
        hikariConfig.setMaximumPoolSize(5);

        return new HikariDataSource(hikariConfig);
    }
}
