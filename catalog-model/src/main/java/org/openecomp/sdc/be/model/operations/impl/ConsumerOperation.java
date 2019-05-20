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

package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.api.IConsumerOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ConsumerData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("consumer-operation")
public class ConsumerOperation implements IConsumerOperation {

    private JanusGraphGenericDao janusGraphGenericDao;

    private static final Logger log = Logger.getLogger(ConsumerOperation.class.getName());

    public ConsumerOperation(@Qualifier("janusgraph-generic-dao")
                                 JanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    @Override
    public Either<ConsumerData, StorageOperationStatus> getCredentials(String consumerName) {
        Either<ConsumerData, StorageOperationStatus> result = null;
        log.debug("retriving Credentials for: {}", consumerName);
        Either<ConsumerData, JanusGraphOperationStatus> getNode = janusGraphGenericDao
            .getNode(GraphPropertiesDictionary.CONSUMER_NAME.getProperty(), consumerName, ConsumerData.class);
        if (getNode.isRight()) {
            JanusGraphOperationStatus status = getNode.right().value();
            log.error("Error returned after get Consumer Data node {}. status returned is {}", consumerName, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            return result;
        }
        ConsumerData consumerData = getNode.left().value();
        return Either.left(consumerData);
    }

    @Override
    public Either<List<ConsumerData>, StorageOperationStatus> getAll() {
        log.debug("retrieving all consumers");
        return janusGraphGenericDao
            .getByCriteria(NodeTypeEnum.ConsumerCredentials, Collections.emptyMap(), ConsumerData.class)
                .right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    @Override
    public Either<ConsumerData, StorageOperationStatus> createCredentials(ConsumerData consumerData) {
        return createCredentials(consumerData, false);
    }

    @Override
    public Either<ConsumerData, StorageOperationStatus> createCredentials(ConsumerData consumerData, boolean inTransaction) {
        Either<ConsumerData, StorageOperationStatus> result = null;
        try {
            log.debug("creating Credentials for: {}", consumerData.getUniqueId());
            Either<ConsumerData, JanusGraphOperationStatus> createNode = janusGraphGenericDao
                .createNode(consumerData, ConsumerData.class);
            if (createNode.isRight()) {
                JanusGraphOperationStatus status = createNode.right().value();
                log.error("Error returned after creating Consumer Data node {}. status returned is {}", consumerData.getUniqueId(), status);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                return result;
            }
            ConsumerData createdConsumerData = createNode.left().value();
            result = Either.left(createdConsumerData);
            return result;
        } finally {
            handleTransaction(inTransaction, result);
        }
    }

    @Override
    public Either<ConsumerData, StorageOperationStatus> deleteCredentials(String consumerName) {
        return deleteCredentials(consumerName, false);
    }

    @Override
    public Either<ConsumerData, StorageOperationStatus> deleteCredentials(String consumerName, boolean inTransaction) {
        Either<ConsumerData, StorageOperationStatus> result = null;
        try {
            log.debug("delete Credentials for: {}", consumerName);
            Either<ConsumerData, JanusGraphOperationStatus> deleteNode = janusGraphGenericDao
                .deleteNode(GraphPropertiesDictionary.CONSUMER_NAME.getProperty(), consumerName, ConsumerData.class);
            if (deleteNode.isRight()) {
                JanusGraphOperationStatus status = deleteNode.right().value();
                log.error("Error returned after delete Consumer Data node {}. status returned is {}", consumerName, status);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                return result;
            }

            ConsumerData deletedConsumerData = deleteNode.left().value();
            result = Either.left(deletedConsumerData);
            return result;
        } finally {
            handleTransaction(inTransaction, result);
        }

    }

    @Override
    public Either<ConsumerData, StorageOperationStatus> updateCredentials(ConsumerData consumerData) {
        return updateCredentials(consumerData, false);
    }

    @Override
    public Either<ConsumerData, StorageOperationStatus> updateCredentials(ConsumerData consumerData, boolean inTransaction) {

        Either<ConsumerData, StorageOperationStatus> result = null;
        try {
            log.debug("update Credentials for: {}", consumerData.getUniqueId());
            Either<ConsumerData, JanusGraphOperationStatus> updateNode = janusGraphGenericDao
                .updateNode(consumerData, ConsumerData.class);
            if (updateNode.isRight()) {
                JanusGraphOperationStatus status = updateNode.right().value();
                log.error("Error returned after delete Consumer Data node {}. status returned is {}", consumerData.getUniqueId(), status);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                return result;
            }
            ConsumerData updatedConsumerData = updateNode.left().value();
            result = Either.left(updatedConsumerData);
            return result;
        } finally {
            handleTransaction(inTransaction, result);
        }
    }

    private void handleTransaction(boolean inTransaction, Either<ConsumerData, StorageOperationStatus> result) {
        if (!inTransaction) {
            if (result == null || result.isRight()) {
                log.error("Going to execute rollback on graph.");
                janusGraphGenericDao.rollback();
            } else {
                log.debug("Going to execute commit on graph.");
                janusGraphGenericDao.commit();
            }
        }
    }

}
