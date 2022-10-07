/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("artifactTypeBusinessLogic")
public class ArtifactTypeBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(ArtifactTypeBusinessLogic.class.getName());

    private final ArtifactTypeOperation artifactTypeOperation;

    @Autowired
    public ArtifactTypeBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation,
                                     IGroupInstanceOperation groupInstanceOperation, IGroupTypeOperation groupTypeOperation,
                                     InterfaceOperation interfaceOperation, InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                     ArtifactsOperations artifactToscaOperation,
                                     ArtifactTypeOperation artifactTypeOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.artifactTypeOperation = artifactTypeOperation;
    }

    public ArtifactTypeDefinition getArtifactTypeByUid(String uniqueId) {
        Either<ArtifactTypeDefinition, StorageOperationStatus> eitherArtifact = artifactTypeOperation.getArtifactTypeByUid(uniqueId);
        if (eitherArtifact == null || eitherArtifact.isRight()) {
            return null;
        }
        return eitherArtifact.left().value();
    }

    public Map<String, ArtifactTypeDefinition> getAllToscaArtifactTypes(final String modelName) {
        if (StringUtils.isNotEmpty(modelName)) {
            artifactTypeOperation.validateModel(modelName);
        }
        return artifactTypeOperation.getAllArtifactTypes(modelName);
    }
}
