package kavin.personal_project.streambase.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSourceProperties replicaProperties() {
        return new DataSourceProperties();
    }

    @Bean("primaryDataSource")
    public DataSource primaryDataSource(@Qualifier("primaryProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean("replicaDataSource")
    public DataSource replicaDataSource(@Qualifier("replicaProperties") DataSourceProperties properties) {

        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        dataSource.setConnectionTimeout(2_000);
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }

    @Bean
    public RoutingDataSource routingDataSource(@Qualifier("primaryDataSource") DataSource primary,
                                               @Qualifier("replicaDataSource") DataSource replica) {

        Map<Object, Object> targets = new HashMap<>();
        targets.put("primary", primary);
        targets.put("replica", replica);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setPrimaryDataSource(primary);
        routingDataSource.setTargetDataSources(targets);
        routingDataSource.setDefaultTargetDataSource(primary);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource(RoutingDataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
