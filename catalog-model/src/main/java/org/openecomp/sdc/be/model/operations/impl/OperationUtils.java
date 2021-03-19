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
package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import java.util.Map;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component
public class OperationUtils {

    private static final Logger logger = Logger.getLogger(OperationUtils.class.getName());
    private final JanusGraphDao janusGraphDao;

    public OperationUtils(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }

    static Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> fillProperties(String uniqueId, PropertyOperation propertyOperation,
                                                                                             NodeTypeEnum nodeTypeEnum) {
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesOfNode = propertyOperation
            .findPropertiesOfNode(nodeTypeEnum, uniqueId);
        if (findPropertiesOfNode.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = findPropertiesOfNode.right().value();
            logger.debug("After looking for properties of vertex {}. status is {}", uniqueId, janusGraphOperationStatus);
            if (JanusGraphOperationStatus.NOT_FOUND.equals(janusGraphOperationStatus)) {
                return Either.right(JanusGraphOperationStatus.OK);
            } else {
                return Either.right(janusGraphOperationStatus);
            }
        } else {
            return Either.left(findPropertiesOfNode.left().value());
        }
    }

    public <T> T onJanusGraphOperationFailure(JanusGraphOperationStatus status) {
        janusGraphDao.rollback();
        throw new StorageException(status);
    }
}
