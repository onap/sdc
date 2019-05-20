package org.openecomp.sdc.be.components.impl.utils;

import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component
public class ExceptionUtils {

    private final JanusGraphDao janusGraphDao;

    public ExceptionUtils(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }

    public <T> T rollBackAndThrow(ActionStatus actionStatus, String ... params) {
         janusGraphDao.rollback();
         throw new ComponentException(actionStatus, params);
     }

    public <T> T rollBackAndThrow(ResponseFormat responseFormat) {
        janusGraphDao.rollback();
        throw new ComponentException(responseFormat);
    }

    public <T> T rollBackAndThrow(StorageOperationStatus status, String ... params) {
        janusGraphDao.rollback();
        throw new StorageException(status, params);
    }

    public <T> T rollBackAndThrow(JanusGraphOperationStatus status, String ... params) {
        janusGraphDao.rollback();
        throw new StorageException(status, params);
    }




}
