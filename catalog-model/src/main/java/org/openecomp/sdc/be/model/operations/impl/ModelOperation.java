/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import java.util.Objects;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("model-operation")
public class ModelOperation extends AbstractOperation {

    private static final Logger log = Logger.getLogger(ModelOperation.class);

    private final JanusGraphGenericDao genericDao;

    @Autowired
    public ModelOperation(final JanusGraphGenericDao janusGraphGenericDao) {
        this.genericDao = janusGraphGenericDao;
    }

    public Model createModel(final Model model, final boolean inTransaction) {
        Model result = null;
        final ModelData modelData = new ModelData(model.getName(), UniqueIdBuilder.buildModelUid(model.getName()));
        try {
            final Either<ModelData, JanusGraphOperationStatus> createNode = genericDao.createNode(modelData, ModelData.class);
            if (createNode.isRight()) {
                final JanusGraphOperationStatus janusGraphOperationStatus = createNode.right().value();
                log.error(EcompLoggerErrorCode.DATA_ERROR, ModelOperation.class.getName(), "Problem while creating model, reason {}",
                    janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION) {
                    throw new OperationException(ActionStatus.MODEL_ALREADY_EXISTS, model.getName());
                }
                throw new OperationException(ActionStatus.GENERAL_ERROR,
                    String.format("Failed to create model %s on JanusGraph with %s error", model, janusGraphOperationStatus));
            }
            result = new Model(createNode.left().value().getName());
            return result;
        } finally {
            if (!inTransaction) {
                if (Objects.nonNull(result)) {
                    genericDao.commit();
                } else {
                    genericDao.rollback();
                }
            }
        }
    }

}


