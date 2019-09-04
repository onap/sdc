/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class Neo4jStatusConverterTest {

    @Test
    public void shouldConvertNeo4jStatusToStorageStatus() {
        assertEquals(StorageOperationStatus.GENERAL_ERROR,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(null));
        assertEquals(StorageOperationStatus.OK,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.OK));
        assertEquals(StorageOperationStatus.CONNECTION_FAILURE,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.NOT_CONNECTED));
        assertEquals(StorageOperationStatus.PERMISSION_ERROR,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.NOT_AUTHORIZED));
        assertEquals(StorageOperationStatus.STORAGE_NOT_AVAILABLE,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.DB_NOT_AVAILABLE));
        assertEquals(StorageOperationStatus.HTTP_PROTOCOL_ERROR,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.HTTP_PROTOCOL_ERROR));
        assertEquals(StorageOperationStatus.READ_ONLY_STORAGE,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.DB_READ_ONLY));
        assertEquals(StorageOperationStatus.BAD_REQUEST,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.BAD_REQUEST));
        assertEquals(StorageOperationStatus.STORAGE_LEGACY_INDEX_ERROR,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.LEGACY_INDEX_ERROR));
        assertEquals(StorageOperationStatus.SCHEMA_ERROR,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.SCHEMA_ERROR));
        assertEquals(StorageOperationStatus.TRANSACTION_ERROR,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.TRANSACTION_ERROR));
        assertEquals(StorageOperationStatus.EXEUCTION_FAILED,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.EXECUTION_FAILED));
        assertEquals(StorageOperationStatus.ENTITY_ALREADY_EXISTS,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.ENTITY_ALREADY_EXIST));
        assertEquals(StorageOperationStatus.BAD_REQUEST,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.WRONG_INPUT));
        assertEquals(StorageOperationStatus.GENERAL_ERROR,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.GENERAL_ERROR));
        assertEquals(StorageOperationStatus.OPERATION_NOT_SUPPORTED,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.NOT_SUPPORTED));
        assertEquals(StorageOperationStatus.NOT_FOUND,
                Neo4jStatusConverter.convertNeo4jStatusToStorageStatus(Neo4jOperationStatus.NOT_FOUND));
    }
}