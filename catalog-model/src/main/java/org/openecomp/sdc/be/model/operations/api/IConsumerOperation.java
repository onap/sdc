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

package org.openecomp.sdc.be.model.operations.api;

import java.util.List;

import org.openecomp.sdc.be.resources.data.ConsumerData;

import fj.data.Either;

public interface IConsumerOperation {

	/**
	 * the method updates the node in the graph with the given ConsumerData
	 * 
	 * @param consumerData
	 *            the object we want to store
	 * @param inTransaction
	 *            inTransaction is the operation part of a transaction, in case the value is false the action will be committed in the end of the method
	 * @return the updated object returned from the graph
	 */
	Either<ConsumerData, StorageOperationStatus> updateCredentials(ConsumerData consumerData, boolean inTransaction);

	/**
	 * the method updates the node in the graph with the given ConsumerData
	 * 
	 * @param consumerData
	 *            the object we want to store
	 * @return the updated object returned from the graph
	 */
	Either<ConsumerData, StorageOperationStatus> updateCredentials(ConsumerData consumerData);

	/**
	 * the method deletes the node with the given unique id
	 * 
	 * @param consumerName
	 *            the unique id by witch we will look up the credential we want to delete
	 * @param inTransaction
	 *            inTransaction is the operation part of a transaction, in case the value is false the action will be committed in the end of the method
	 * @return the deleted object returned from the graph
	 */
	Either<ConsumerData, StorageOperationStatus> deleteCredentials(String consumerName, boolean inTransaction);

	/**
	 * the method deletes the node with the given unique id
	 * 
	 * @param consumerName
	 *            the unique id by witch we will look up the credential we want to delete
	 * @return the deleted object returned from the graph
	 */
	Either<ConsumerData, StorageOperationStatus> deleteCredentials(String consumerName);

	/**
	 * the method creates a new nod in the grape representing the supplied credential object
	 * 
	 * @param consumerData
	 *            the object we want to store
	 * @param inTransaction
	 *            is the operation part of a transaction, in case the value is false the action will be committed in the end of the method
	 * @return the newly stored object returned from the graph
	 */
	Either<ConsumerData, StorageOperationStatus> createCredentials(ConsumerData consumerData, boolean inTransaction);

	/**
	 * the method creates a new nod in the grape representing the supplied credential object
	 * 
	 * @param consumerData
	 *            the object we want to store
	 * @return the newly stored object returned from the graph
	 */
	Either<ConsumerData, StorageOperationStatus> createCredentials(ConsumerData consumerData);

	/**
	 * the method retrieves the credential for the given consumer name
	 * 
	 * @param consumerName
	 *            the unique id by witch we will look up the credential
	 * @return ConsumerData or the error received during the operation
	 */
	Either<ConsumerData, StorageOperationStatus> getCredentials(String consumerName);

	/**
	 *
	 * @return all consumers
     */
	Either<List<ConsumerData>, StorageOperationStatus> getAll();

}
