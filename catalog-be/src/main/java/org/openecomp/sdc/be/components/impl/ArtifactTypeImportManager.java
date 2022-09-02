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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.normatives.ElementTypeEnum;
import org.openecomp.sdc.be.model.operations.impl.ArtifactTypeOperation;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("artifactTypeImportManager")
public class ArtifactTypeImportManager {

    private final ArtifactTypeOperation artifactTypeOperation;
    private final ComponentsUtils componentsUtils;
    private final CommonImportManager commonImportManager;

    @Autowired
    public ArtifactTypeImportManager(final ArtifactTypeOperation artifactTypeOperation, final ComponentsUtils componentsUtils,
                                     final CommonImportManager commonImportManager) {
        this.artifactTypeOperation = artifactTypeOperation;
        this.componentsUtils = componentsUtils;
        this.commonImportManager = commonImportManager;
    }

    public Either<List<ArtifactTypeDefinition>, ResponseFormat> createArtifactTypes(final String artifactTypesYml,
                                                                                    final String modelName,
                                                                                    final boolean includeToModelDefaultImports) {
        if (StringUtils.isNotEmpty(modelName)) {
            artifactTypeOperation.validateModel(modelName);
        }
        final Either<List<ArtifactTypeDefinition>, ActionStatus> artifactTypes = createArtifactTypeFromYml(artifactTypesYml, modelName);
        if (artifactTypes.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(artifactTypes.right().value()));
        }
        final List<ArtifactTypeDefinition> elementTypes = createArtifactTypesByDao(artifactTypes.left().value());
        if (includeToModelDefaultImports && StringUtils.isNotEmpty(modelName)) {
            commonImportManager.addTypesToDefaultImports(ElementTypeEnum.ARTIFACT_TYPE, artifactTypesYml, modelName);
        }
        return Either.left(elementTypes);
    }

    protected Either<List<ArtifactTypeDefinition>, ActionStatus> createArtifactTypeFromYml(
        final String artifactTypesYml, final String modelName) {
        final Either<List<ArtifactTypeDefinition>, ActionStatus> artifactTypes =
                commonImportManager.createElementTypesFromYml(artifactTypesYml, this::createArtifactTypeDefinition);
        if (artifactTypes.isLeft()) {
            artifactTypes.left().value().forEach(artifactType -> artifactType.setModel(modelName));
            return artifactTypes;
        }
        return artifactTypes;
    }

    private List<ArtifactTypeDefinition> createArtifactTypesByDao(
            final List<ArtifactTypeDefinition> artifactTypes) {
        final List<ArtifactTypeDefinition> createdTypes = new ArrayList<>();
        artifactTypes.forEach(type -> createdTypes.add(artifactTypeOperation.createArtifactType(type)));
        return createdTypes;
    }

    private ArtifactTypeDefinition createArtifactTypeDefinition(final String type,
            final Map<String, Object> toscaJson) {
        final ArtifactTypeDefinition artifactType = new ArtifactTypeDefinition();
        artifactType.setType(type);
        if (toscaJson != null) {
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(),
                    artifactType::setDescription);
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName(),
                    artifactType::setDerivedFrom);
            CommonImportManager.setProperties(toscaJson, artifactType::setProperties);
        }
        return artifactType;
    }
}
