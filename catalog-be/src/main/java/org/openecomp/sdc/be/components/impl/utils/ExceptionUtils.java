package org.openecomp.sdc.be.components.impl.utils;

import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component
public class ExceptionUtils {

    private final TitanDao titanDao;

    public ExceptionUtils(TitanDao titanDao) {
        this.titanDao = titanDao;
    }

    public <T> T rollBackAndThrow(ActionStatus actionStatus, String ... params) {
         titanDao.rollback();
         throw new ComponentException(actionStatus, params);
     }

    public <T> T rollBackAndThrow(ResponseFormat responseFormat) {
        titanDao.rollback();
        throw new ComponentException(responseFormat);
    }

    public <T> T rollBackAndThrow(StorageOperationStatus status, String ... params) {
        titanDao.rollback();
        throw new StorageException(status, params);
    }

    public <T> T rollBackAndThrow(TitanOperationStatus status, String ... params) {
        titanDao.rollback();
        throw new StorageException(status, params);
    }




}
