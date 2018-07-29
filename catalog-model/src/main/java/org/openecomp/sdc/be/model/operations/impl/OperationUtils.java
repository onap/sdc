package org.openecomp.sdc.be.model.operations.impl;

import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.springframework.stereotype.Component;

@Component
public class OperationUtils {

    private final TitanDao titanDao;

    public OperationUtils(TitanDao titanDao) {
        this.titanDao = titanDao;
    }

    public <T> T onTitanOperationFailure(TitanOperationStatus status) {
        titanDao.rollback();
        throw new StorageException(status);
    }
}
