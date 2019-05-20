package org.openecomp.sdc.be.model.operations;

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

    public StorageOperationStatus getStorageOperationStatus() {
        return storageOperationStatus;
    }

    public String[] getParams() {
        return params.clone();
    }
}
