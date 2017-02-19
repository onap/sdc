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

import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class Neo4jStatusConverter {

	public static StorageOperationStatus convertNeo4jStatusToStorageStatus(Neo4jOperationStatus neo4jStatus) {

		if (neo4jStatus == null) {
			return StorageOperationStatus.GENERAL_ERROR;
		}

		switch (neo4jStatus) {

		case OK:
			return StorageOperationStatus.OK;

		case NOT_CONNECTED:
			return StorageOperationStatus.CONNECTION_FAILURE;

		case NOT_AUTHORIZED:
			return StorageOperationStatus.PERMISSION_ERROR;

		case HTTP_PROTOCOL_ERROR:
			return StorageOperationStatus.HTTP_PROTOCOL_ERROR;
		case DB_NOT_AVAILABLE:
			return StorageOperationStatus.STORAGE_NOT_AVAILABLE;
		case DB_READ_ONLY:
			return StorageOperationStatus.READ_ONLY_STORAGE;
		case BAD_REQUEST:
			return StorageOperationStatus.BAD_REQUEST;
		case LEGACY_INDEX_ERROR:
			return StorageOperationStatus.STORAGE_LEGACY_INDEX_ERROR;
		case SCHEMA_ERROR:
			return StorageOperationStatus.SCHEMA_ERROR;
		case TRANSACTION_ERROR:
			return StorageOperationStatus.TRANSACTION_ERROR;
		case EXECUTION_FAILED:
			return StorageOperationStatus.EXEUCTION_FAILED;
		case ENTITY_ALREADY_EXIST:
			return StorageOperationStatus.ENTITY_ALREADY_EXISTS;
		case WRONG_INPUT:
			return StorageOperationStatus.BAD_REQUEST;
		case GENERAL_ERROR:
			return StorageOperationStatus.GENERAL_ERROR;
		case NOT_SUPPORTED:
			return StorageOperationStatus.OPERATION_NOT_SUPPORTED;
		case NOT_FOUND:
			return StorageOperationStatus.NOT_FOUND;

		default:
			return StorageOperationStatus.GENERAL_ERROR;
		}

	}

}
