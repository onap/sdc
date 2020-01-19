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

package org.openecomp.sdc.be.model.operations;

import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;

public class StorageException extends RuntimeException{

    private final StorageOperationStatus storageOperationStatus;
    private final String[] params;

    public StorageException(StorageOperationStatus storageOperationStatus, String... params) {
        super();
        this.storageOperationStatus = storageOperationStatus;
        this.params = params;
    }

    public StorageException(String message, Throwable cause, JanusGraphOperationStatus janusGraphOperationStatus, String... params){
        super(message, cause);
        storageOperationStatus = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
            janusGraphOperationStatus);
        this.params = params;
    }

    public StorageException(JanusGraphOperationStatus janusGraphOperationStatus, String... params) {
        storageOperationStatus = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
            janusGraphOperationStatus);
        this.params = params;
    }

    public StorageException(CassandraOperationStatus cassandraOperationStatus, String... params) {
        storageOperationStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(cassandraOperationStatus);
        this.params = params;
    }

    public StorageOperationStatus getStorageOperationStatus() {
        return storageOperationStatus;
    }

    public String[] getParams() {
        return params.clone();
    }
}
