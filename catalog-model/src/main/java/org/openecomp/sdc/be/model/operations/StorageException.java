package org.openecomp.sdc.be.model.operations;

import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
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

    public StorageException(String message, Throwable cause, TitanOperationStatus titanOperationStatus, String... params){
        super(message, cause);
        storageOperationStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(titanOperationStatus);
        this.params = params;
    }

    public StorageException(TitanOperationStatus titanOperationStatus, String... params) {
        storageOperationStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(titanOperationStatus);
        this.params = params;
    }

    public StorageOperationStatus getStorageOperationStatus() {
        return storageOperationStatus;
    }

    public String[] getParams() {
        return params.clone();
    }
}
