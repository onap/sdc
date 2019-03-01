/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.impl;

import java.util.Map;

import fj.data.Either;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.RelationshipTypeOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("relationshipTypeBusinessLogic")
public class RelationshipTypeBusinessLogic {

    @Autowired
    private RelationshipTypeOperation relationshipTypeOperation;

    @Autowired
    protected ComponentsUtils componentsUtils;

    public Either<Map<String, RelationshipTypeDefinition>, ResponseFormat> getAllRelationshipTypes() {
        Either<Map<String, RelationshipTypeDefinition>, TitanOperationStatus> allRelationshipTypes =
                relationshipTypeOperation.getAllRelationshipTypes();
        if (allRelationshipTypes.isRight()) {
            TitanOperationStatus operationStatus = allRelationshipTypes.right().value();
            if (TitanOperationStatus.NOT_FOUND == operationStatus) {
                BeEcompErrorManager.getInstance().logInternalDataError("FetchRelationshipTypes", "Relationship types "
                                + "are "
                                + "not loaded",
                        BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY));
            } else {
                BeEcompErrorManager.getInstance().logInternalFlowError("FetchRelationshipTypes", "Failed to fetch "
                                + "relationship types",
                        BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        }
        return Either.left(allRelationshipTypes.left().value());
    }

}
