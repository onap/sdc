/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
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

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("model-element-operation")
public class ModelElementOperation {

    private final JanusGraphGenericDao janusGraphGenericDao;
    private final DataTypeOperation dataTypeOperation;
    private final PolicyTypeOperation policyTypeOperation;

    @Autowired
    public ModelElementOperation(final JanusGraphGenericDao janusGraphGenericDao,
                                 final DataTypeOperation dataTypeOperation,
                                 final PolicyTypeOperation policyTypeOperation) {
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.dataTypeOperation = dataTypeOperation;
        this.policyTypeOperation = policyTypeOperation;
    }

    /**
     * Deletes the given model if it exists, along with its MODEL_ELEMENT edges and import files.
     *
     * @param model         the model
     * @param inTransaction if the operation is called in the middle of a janusgraph transaction
     */
    public void deleteModelElements(final Model model, final boolean inTransaction) {
        boolean rollback = false;

        try {
            final String modelId = UniqueIdBuilder.buildModelUid(model.getName());
            dataTypeOperation.deleteDataTypesByModelId(modelId);
            policyTypeOperation.deletePolicyTypesByModelId(modelId);
        } catch (final Exception e) {
            rollback = true;
            throw new OperationException(e, ActionStatus.COULD_NOT_DELETE_MODEL_ELEMENTS, model.getName());
        } finally {
            if (!inTransaction) {
                if (rollback) {
                    janusGraphGenericDao.rollback();
                } else {
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

}
