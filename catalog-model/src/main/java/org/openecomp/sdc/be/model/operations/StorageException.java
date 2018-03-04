package org.openecomp.sdc.be.model.operations;

import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class StorageException extends RuntimeException{

    private final StorageOperationStatus storageOperationStatus;

    public StorageException(StorageOperationStatus storageOperationStatus) {
        super();
        this.storageOperationStatus = storageOperationStatus;
    }

    public StorageOperationStatus getStorageOperationStatus() {
        return storageOperationStatus;
    }
}
