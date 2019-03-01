package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OperationUtils {

    private final TitanDao titanDao;

    private static final Logger logger = Logger.getLogger(OperationUtils.class.getName());

    public OperationUtils(TitanDao titanDao) {
        this.titanDao = titanDao;
    }

    public <T> T onTitanOperationFailure(TitanOperationStatus status) {
        titanDao.rollback();
        throw new StorageException(status);
    }

    static Either<Map<String, PropertyDefinition>, TitanOperationStatus> fillProperties(String uniqueId,
                                                                                        PropertyOperation propertyOperation,
                                                                                        NodeTypeEnum nodeTypeEnum) {

        Either<Map<String, PropertyDefinition>, TitanOperationStatus> findPropertiesOfNode =
                propertyOperation.findPropertiesOfNode(nodeTypeEnum, uniqueId);
        if (findPropertiesOfNode.isRight()) {
            TitanOperationStatus titanOperationStatus = findPropertiesOfNode.right().value();
            logger.debug("After looking for properties of vertex {}. status is {}", uniqueId, titanOperationStatus);
            if (TitanOperationStatus.NOT_FOUND.equals(titanOperationStatus)) {
                return Either.right(TitanOperationStatus.OK);
            } else {
                return Either.right(titanOperationStatus);
            }
        } else {
            return Either.left(findPropertiesOfNode.left().value());
        }
    }
}
