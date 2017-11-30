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

import java.util.Collections;
import java.util.List;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.api.IConsumerOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ConsumerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("consumer-operation")
public class ConsumerOperation implements IConsumerOperation {

	private TitanGenericDao titanGenericDao;

	private static Logger log = LoggerFactory.getLogger(ConsumerOperation.class.getName());

	public ConsumerOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
	public Either<ConsumerData, StorageOperationStatus> getCredentials(String consumerName) {
		Either<ConsumerData, StorageOperationStatus> result = null;
		log.debug("retriving Credentials for: {}", consumerName);
		Either<ConsumerData, TitanOperationStatus> getNode = titanGenericDao.getNode(GraphPropertiesDictionary.CONSUMER_NAME.getProperty(), consumerName, ConsumerData.class);
		if (getNode.isRight()) {
			TitanOperationStatus status = getNode.right().value();
			log.error("Error returned after get Consumer Data node {}. status returned is {}", consumerName, status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}
		ConsumerData consumerData = getNode.left().value();
		return Either.left(consumerData);
	}

	@Override
	public Either<List<ConsumerData>, StorageOperationStatus> getAll() {
		log.debug("retrieving all consumers");
		return titanGenericDao.getByCriteria(NodeTypeEnum.ConsumerCredentials, Collections.emptyMap(), ConsumerData.class)
				.right().map(DaoStatusConverter::convertTitanStatusToStorageStatus);
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
			Either<ConsumerData, TitanOperationStatus> createNode = titanGenericDao.createNode(consumerData, ConsumerData.class);
			if (createNode.isRight()) {
				TitanOperationStatus status = createNode.right().value();
				log.error("Error returned after creating Consumer Data node {}. status returned is {}", consumerData.getUniqueId(), status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
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
			Either<ConsumerData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(GraphPropertiesDictionary.CONSUMER_NAME.getProperty(), consumerName, ConsumerData.class);
			if (deleteNode.isRight()) {
				TitanOperationStatus status = deleteNode.right().value();
				log.error("Error returned after delete Consumer Data node {}. status returned is {}", consumerName, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
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
			Either<ConsumerData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(consumerData, ConsumerData.class);
			if (updateNode.isRight()) {
				TitanOperationStatus status = updateNode.right().value();
				log.error("Error returned after delete Consumer Data node {}. status returned is {}", consumerData.getUniqueId(), status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
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
		if (false == inTransaction) {
			if (result == null || result.isRight()) {
				log.error("Going to execute rollback on graph.");
				titanGenericDao.rollback();
			} else {
				log.debug("Going to execute commit on graph.");
				titanGenericDao.commit();
			}
		}
	}

}
