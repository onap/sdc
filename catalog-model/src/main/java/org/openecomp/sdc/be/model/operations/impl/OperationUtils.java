package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OperationUtils {

    private final JanusGraphDao janusGraphDao;

    private static final Logger logger = Logger.getLogger(OperationUtils.class.getName());

    public OperationUtils(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }

    public <T> T onJanusGraphOperationFailure(JanusGraphOperationStatus status) {
        janusGraphDao.rollback();
        throw new StorageException(status);
    }

    static Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> fillProperties(String uniqueId,
                                                                                             PropertyOperation propertyOperation,
                                                                                             NodeTypeEnum nodeTypeEnum) {

        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesOfNode =
                propertyOperation.findPropertiesOfNode(nodeTypeEnum, uniqueId);
        if (findPropertiesOfNode.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = findPropertiesOfNode.right().value();
            logger.debug("After looking for properties of vertex {}. status is {}", uniqueId,
                janusGraphOperationStatus);
            if (JanusGraphOperationStatus.NOT_FOUND.equals(janusGraphOperationStatus)) {
                return Either.right(JanusGraphOperationStatus.OK);
            } else {
                return Either.right(janusGraphOperationStatus);
            }
        } else {
            return Either.left(findPropertiesOfNode.left().value());
        }
    }
}
