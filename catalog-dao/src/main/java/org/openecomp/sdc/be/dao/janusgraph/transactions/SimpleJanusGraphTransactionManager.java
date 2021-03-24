/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.dao.janusgraph.transactions;

import javax.annotation.PostConstruct;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphException;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * Simple transaction manager for the janusgraph database. This manager does not deal with transactions propagation and relies on the fact that
 * transactions are automatically created with the first operation on the graph
 */
@Component
public class SimpleJanusGraphTransactionManager implements PlatformTransactionManager {

    private static final Logger log = Logger.getLogger(SimpleJanusGraphTransactionManager.class.getName());
    private final JanusGraphClient janusGraphClient;
    private JanusGraph janusGraph;

    public SimpleJanusGraphTransactionManager(JanusGraphClient janusGraphClient) {
        this.janusGraphClient = janusGraphClient;
    }

    @PostConstruct
    public void onInit() {
        janusGraph = janusGraphClient.getGraph().left().on(this::onFailingToStartJanusGraph);
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
            janusGraph.tx().commit();
        } catch (JanusGraphException e) {
            log.debug("#commit - failed to commit transaction", e);
            throw new TransactionSystemException("failed to commit transaction", e);
        }
    }

    @Override
    public void rollback(TransactionStatus transactionStatus) {
        log.debug("#rollback - committing transaction");
        try {
            janusGraph.tx().rollback();
        } catch (JanusGraphException e) {
            log.debug("#rollback - failed to rollback transaction", e);
            throw new TransactionSystemException("failed to rollback transaction", e);
        }
    }

    private JanusGraph onFailingToStartJanusGraph(JanusGraphOperationStatus err) {
        log.debug("#onFailingToStartJanusGraph - could not open janusgraph client");
        throw new IllegalStateException("janusgraph could not be initialized: " + err);
    }
}
