package kavin.personal_project.streambase.config;

import lombok.extern.java.Log;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Log
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String key = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "replica" : "primary";
        log.info("Routing to: [" + key + "]");
        return key;
    }
}