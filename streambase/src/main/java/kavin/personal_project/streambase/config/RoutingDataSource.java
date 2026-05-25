package kavin.personal_project.streambase.config;

import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Log
public class RoutingDataSource extends AbstractRoutingDataSource {

    private volatile boolean replicaAvailable = true;

    @Setter
    private DataSource primaryDataSource;

    @Override
    protected Object determineCurrentLookupKey() {
        String key = TransactionSynchronizationManager.isCurrentTransactionReadOnly() && replicaAvailable ? "replica" : "primary";
        log.info("Routing to: [" + key + "]");
        return key;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return super.getConnection();
        } catch (SQLException e) {
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly() && replicaAvailable) {
                log.warning("Replica down, falling back to primary: " + e.getMessage());
                replicaAvailable = false;
                return primaryDataSource.getConnection();
            }
            throw e;
        }
    }

    public boolean isReplicaAvailable() {
        return replicaAvailable;
    }

    public void makeReplicaUp() {
        replicaAvailable = true;
        log.info("Replica restored — routing reads to replica again");
    }
}