package org.openecomp.sdc.be.dao.titan.transactions;

import com.thinkaurelius.titan.core.TitanException;
import com.thinkaurelius.titan.core.TitanGraph;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.SimpleTransactionStatus;

import javax.annotation.PostConstruct;

/**
 * Simple transaction manager for the titan database.
 * This manager does not deal with transactions propagation and relies on the fact that transactions are automatically created with the first operation on the graph
 */
@Component
public class SimpleTitanTransactionManager implements PlatformTransactionManager {

    private static final Logger log = Logger.getLogger(SimpleTitanTransactionManager.class.getName());
    private final TitanGraphClient titanClient;
    private TitanGraph titanGraph;

    public SimpleTitanTransactionManager(TitanGraphClient titanClient) {
        this.titanClient = titanClient;
    }

    @PostConstruct
    public void onInit() {
        titanGraph = titanClient.getGraph().left().on(this::onFailingToStartTitan);
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition transactionDefinition) {
        log.debug("#getTransaction - returning simple transaction status");
        return new SimpleTransactionStatus();
    }

    @Override
    public void commit(TransactionStatus transactionStatus) {
        log.debug("#commit - committing transaction");
        try {
            titanGraph.tx().commit();
        } catch (TitanException e) {
            log.debug("#commit - failed to commit transaction", e);
            throw new TransactionSystemException("failed to commit transaction", e);
        }
    }

    @Override
    public void rollback(TransactionStatus transactionStatus) {
        log.debug("#rollback - committing transaction");
        try {
            titanGraph.tx().rollback();
        } catch (TitanException e) {
            log.debug("#rollback - failed to rollback transaction", e);
            throw new TransactionSystemException("failed to rollback transaction", e);
        }
    }

    private TitanGraph onFailingToStartTitan(TitanOperationStatus err) {
        log.debug("#onFailingToStartTitan - could not open titan client");
        throw new IllegalStateException("titan could not be initialized: " + err);
    }

}
