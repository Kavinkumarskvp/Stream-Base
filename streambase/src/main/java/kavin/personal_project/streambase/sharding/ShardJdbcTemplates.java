package kavin.personal_project.streambase.sharding;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

public record ShardJdbcTemplates(
        Map<String, JdbcTemplate> templates
) {
}
