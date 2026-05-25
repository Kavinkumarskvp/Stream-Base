package kavin.personal_project.streambase.config;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Log
@Component
public class ReplicaHealthMonitor {

    private final RoutingDataSource routingDataSource;
    private final DataSource replicaDataSource;

    public ReplicaHealthMonitor(RoutingDataSource routingDataSource, @Qualifier("replicaDataSource") DataSource replicaDataSource) {
        this.routingDataSource = routingDataSource;
        this.replicaDataSource = replicaDataSource;
    }


    @Scheduled(fixedDelay = 30_000)
    public void checkReplica() {
        if (routingDataSource.isReplicaAvailable()) return;

        try (Connection connection = replicaDataSource.getConnection();
             var statement = connection.createStatement()) {

            statement.execute("SELECT 1");
            routingDataSource.makeReplicaUp();

        } catch (SQLException e) {
            log.fine("Replica still down: " + e.getMessage());
        }
    }
}
