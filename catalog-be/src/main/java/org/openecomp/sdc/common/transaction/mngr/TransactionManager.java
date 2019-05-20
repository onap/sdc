/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.common.transaction.mngr;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import org.openecomp.sdc.be.dao.impl.ESCatalogDAO;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.transaction.api.ITransactionSdnc;
import org.openecomp.sdc.common.transaction.api.TransactionUtils;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ActionTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

@Component("transactionManager")
public class TransactionManager {

    private static final Logger log = Logger.getLogger(TransactionManager.class.getName());

    private AtomicInteger transactionIDCounter = new AtomicInteger(0);

    private Queue<ITransactionSdnc> transactions;
    @Resource
    private ESCatalogDAO esCatalogDao;
    @Resource
    private JanusGraphGenericDao janusGraphGenericDao;

    /**
     * userId and actionType parameters are used only for logging purposes.
     */
    public ITransactionSdnc getTransaction(String userId, ActionTypeEnum actionType) {
        if (transactions == null) {
            init();
        }
        log.debug("TransactionManager creating new SdncTransaction");
        ITransactionSdnc tx = new TransactionSdncImpl(generateTransactionID(), userId, actionType, esCatalogDao,
            janusGraphGenericDao);
        transactions.add(tx);

        return tx;

    }

    private Integer generateTransactionID() {
        boolean generatedSuccessfully = false;
        int nextId = 0;

        while (!generatedSuccessfully) {
            int prevId = transactionIDCounter.get();
            if (prevId > TransactionUtils.TRANSACTION_ID_RESET_LIMIT) {
                resetTransactionId();
            }
            nextId = prevId + 1;
            generatedSuccessfully = transactionIDCounter.compareAndSet(prevId, nextId);
        }
        return nextId;
    }

    private void resetTransactionId() {

        boolean resetSuccessfully = false;
        while (!resetSuccessfully) {
            int prevId = transactionIDCounter.get();
            resetSuccessfully = transactionIDCounter.compareAndSet(prevId, 0);
        }

    }

    private synchronized void init() {
        if (transactions == null) {
            log.info("TransactionManager Initialized");
            EvictingQueue<ITransactionSdnc> queue = EvictingQueue
                    .<ITransactionSdnc>create(TransactionUtils.MAX_SIZE_TRANSACTION_LIST);
            // make thread-safe
            transactions = Queues.synchronizedQueue(queue);
        }
    }

}
