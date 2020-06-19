/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * Modifications (C) 2020 AT&T.
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
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class DaoStatusConverterTest {

    @Test
    public void shouldConvertJanusGraphStatusToStorageStatus() {
        assertEquals(StorageOperationStatus.GENERAL_ERROR,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(null));
        assertEquals(StorageOperationStatus.OK,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.OK));
        assertEquals(StorageOperationStatus.CONNECTION_FAILURE,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_CONNECTED));
        assertEquals(StorageOperationStatus.NOT_FOUND,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_FOUND));
        assertEquals(StorageOperationStatus.SCHEMA_ERROR,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_CREATED));
        assertEquals(StorageOperationStatus.SCHEMA_ERROR,
                DaoStatusConverter
                        .convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.INDEX_CANNOT_BE_CHANGED));
        assertEquals(StorageOperationStatus.BAD_REQUEST,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.MISSING_UNIQUE_ID));
        assertEquals(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ALREADY_LOCKED));
        assertEquals(StorageOperationStatus.SCHEMA_VIOLATION,
                DaoStatusConverter.
                        convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION));
        assertEquals(StorageOperationStatus.INVALID_ID,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.INVALID_ID));
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.MATCH_NOT_FOUND));
        assertEquals(StorageOperationStatus.BAD_REQUEST,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ILLEGAL_ARGUMENT));
        assertEquals(StorageOperationStatus.ENTITY_ALREADY_EXISTS,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ALREADY_EXIST));
        assertEquals(StorageOperationStatus.PROPERTY_NAME_ALREADY_EXISTS,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                        JanusGraphOperationStatus.PROPERTY_NAME_ALREADY_EXISTS));
        assertEquals(StorageOperationStatus.INVALID_PROPERTY,
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.INVALID_PROPERTY));
    }

    @Test
    public void shouldConvertCassandraStatusToStorageStatus() {
        assertEquals(StorageOperationStatus.GENERAL_ERROR,
                DaoStatusConverter.convertCassandraStatusToStorageStatus(null));
        assertEquals(StorageOperationStatus.OK,
                DaoStatusConverter.convertCassandraStatusToStorageStatus(CassandraOperationStatus.OK));
        assertEquals(StorageOperationStatus.CONNECTION_FAILURE,
                DaoStatusConverter
                             .convertCassandraStatusToStorageStatus(CassandraOperationStatus.CLUSTER_NOT_CONNECTED));
        assertEquals(StorageOperationStatus.STORAGE_NOT_AVAILABLE,
                DaoStatusConverter
                        .convertCassandraStatusToStorageStatus(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED));
        assertEquals(StorageOperationStatus.NOT_FOUND,
                DaoStatusConverter.convertCassandraStatusToStorageStatus(CassandraOperationStatus.NOT_FOUND));
    }
}