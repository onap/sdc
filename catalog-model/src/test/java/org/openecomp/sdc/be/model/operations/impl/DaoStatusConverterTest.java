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
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class DaoStatusConverterTest {

    @Test
    public void shouldConvertJanusGraphStatusToStorageStatus() {
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(null),
                StorageOperationStatus.GENERAL_ERROR);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.OK),
                StorageOperationStatus.OK);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_CONNECTED),
                StorageOperationStatus.CONNECTION_FAILURE);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_FOUND),
                StorageOperationStatus.NOT_FOUND);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_CREATED),
                StorageOperationStatus.SCHEMA_ERROR);
        assertEquals(DaoStatusConverter
                             .convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.INDEX_CANNOT_BE_CHANGED),
                StorageOperationStatus.SCHEMA_ERROR);
        assertEquals(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.MISSING_UNIQUE_ID),
                StorageOperationStatus.BAD_REQUEST);
        assertEquals(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ALREADY_LOCKED),
                StorageOperationStatus.FAILED_TO_LOCK_ELEMENT);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION), StorageOperationStatus.SCHEMA_VIOLATION);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.INVALID_ID),
                StorageOperationStatus.INVALID_ID);
        assertEquals(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.MATCH_NOT_FOUND),
                StorageOperationStatus.MATCH_NOT_FOUND);
        assertEquals(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ILLEGAL_ARGUMENT),
                StorageOperationStatus.BAD_REQUEST);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ALREADY_EXIST),
                StorageOperationStatus.ENTITY_ALREADY_EXISTS);
        assertEquals(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                JanusGraphOperationStatus.PROPERTY_NAME_ALREADY_EXISTS),
                StorageOperationStatus.PROPERTY_NAME_ALREADY_EXISTS);
        assertEquals(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.INVALID_PROPERTY),
                StorageOperationStatus.INVALID_PROPERTY);
    }

    @Test
    public void shouldConvertCassandraStatusToStorageStatus() {
        assertEquals(DaoStatusConverter.convertCassandraStatusToStorageStatus(null),
                StorageOperationStatus.GENERAL_ERROR);
        assertEquals(DaoStatusConverter.convertCassandraStatusToStorageStatus(CassandraOperationStatus.OK),
                StorageOperationStatus.OK);
        assertEquals(DaoStatusConverter
                             .convertCassandraStatusToStorageStatus(CassandraOperationStatus.CLUSTER_NOT_CONNECTED),
                StorageOperationStatus.CONNECTION_FAILURE);
        assertEquals(DaoStatusConverter
                             .convertCassandraStatusToStorageStatus(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED),
                StorageOperationStatus.STORAGE_NOT_AVAILABLE);
        assertEquals(DaoStatusConverter.convertCassandraStatusToStorageStatus(CassandraOperationStatus.NOT_FOUND),
                StorageOperationStatus.NOT_FOUND);
    }
}