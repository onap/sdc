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

import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class DaoStatusConverter {

	public static StorageOperationStatus convertTitanStatusToStorageStatus(TitanOperationStatus titanStatus) {

		if (titanStatus == null) {
			return StorageOperationStatus.GENERAL_ERROR;
		}

		switch (titanStatus) {

		case OK:
			return StorageOperationStatus.OK;

		case NOT_CONNECTED:
			return StorageOperationStatus.CONNECTION_FAILURE;

		case NOT_FOUND:
			return StorageOperationStatus.NOT_FOUND;

		case NOT_CREATED:
			return StorageOperationStatus.SCHEMA_ERROR;

		case INDEX_CANNOT_BE_CHANGED:
			return StorageOperationStatus.SCHEMA_ERROR;

		case MISSING_UNIQUE_ID:
			return StorageOperationStatus.BAD_REQUEST;
		case ALREADY_LOCKED:
			return StorageOperationStatus.FAILED_TO_LOCK_ELEMENT;

		case TITAN_SCHEMA_VIOLATION:
			return StorageOperationStatus.SCHEMA_VIOLATION;

		case INVALID_ID:
			return StorageOperationStatus.INVALID_ID;
		case MATCH_NOT_FOUND:
			return StorageOperationStatus.MATCH_NOT_FOUND;

		case ILLEGAL_ARGUMENT:
			return StorageOperationStatus.BAD_REQUEST;
		// case HTTP_PROTOCOL_ERROR:
		// return StorageOperationStatus.HTTP_PROTOCOL_ERROR;
		// case DB_NOT_AVAILABLE:
		// return StorageOperationStatus.STORAGE_NOT_AVAILABLE;
		// case DB_READ_ONLY:
		// return StorageOperationStatus.READ_ONLY_STORAGE;
		// case BAD_REQUEST:
		// return StorageOperationStatus.BAD_REQUEST;
		// case LEGACY_INDEX_ERROR:
		// return StorageOperationStatus.STORAGE_LEGACY_INDEX_ERROR;
		// case SCHEMA_ERROR:
		// return StorageOperationStatus.SCHEMA_ERROR;
		// case TRANSACTION_ERROR:
		// return StorageOperationStatus.TRANSACTION_ERROR;
		// case EXECUTION_FAILED:
		// return StorageOperationStatus.EXEUCTION_FAILED;
		case ALREADY_EXIST:
			return StorageOperationStatus.ENTITY_ALREADY_EXISTS;
		case PROPERTY_NAME_ALREADY_EXISTS:
			return StorageOperationStatus.PROPERTY_NAME_ALREADY_EXISTS;
		case INVALID_PROPERTY:
			return StorageOperationStatus.INVALID_PROPERTY;
		// case WRONG_INPUT:
		// return StorageOperationStatus.BAD_REQUEST;
		// case GENERAL_ERROR:
		// return StorageOperationStatus.GENERAL_ERROR;
		// case NOT_SUPPORTED:
		// return StorageOperationStatus.OPERATION_NOT_SUPPORTED;

		default:
			return StorageOperationStatus.GENERAL_ERROR;
		}

	}

	public static StorageOperationStatus convertRsrcUploadStatusToStorageStatus(
			ResourceUploadStatus resourceUploadStatus) {
		if (resourceUploadStatus == null) {
			return StorageOperationStatus.GENERAL_ERROR;
		}
		switch (resourceUploadStatus) {
		case OK:
			return StorageOperationStatus.OK;
		case ALREADY_EXIST:
			return StorageOperationStatus.ENTITY_ALREADY_EXISTS;
		case NOT_EXIST:
			return StorageOperationStatus.ARTIFACT_NOT_FOUND;
		case SERVICE_NOT_EXIST:
		case COMPONENT_NOT_EXIST:
			return StorageOperationStatus.NOT_FOUND;
		default:
			return StorageOperationStatus.GENERAL_ERROR;
		}
	}

	public static StorageOperationStatus convertCassandraStatusToStorageStatus(CassandraOperationStatus status) {
		if (status == null) {
			return StorageOperationStatus.GENERAL_ERROR;
		}
		switch (status) {
		case OK:
			return StorageOperationStatus.OK;
		case CLUSTER_NOT_CONNECTED:
			return StorageOperationStatus.CONNECTION_FAILURE;
		case KEYSPACE_NOT_CONNECTED:
			return StorageOperationStatus.STORAGE_NOT_AVAILABLE;
		case NOT_FOUND:
			return StorageOperationStatus.NOT_FOUND;

		default:
			return StorageOperationStatus.GENERAL_ERROR;
		}
	}
}
