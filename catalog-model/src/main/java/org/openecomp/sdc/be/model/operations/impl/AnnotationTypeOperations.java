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

import javax.validation.constraints.NotNull;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.TypeOperations;
import org.openecomp.sdc.be.resources.data.AnnotationTypeData;
import org.springframework.stereotype.Component;

@Component
public class AnnotationTypeOperations implements TypeOperations<AnnotationTypeDefinition> {

    private final CommonTypeOperations commonTypeOperations;

    public AnnotationTypeOperations(CommonTypeOperations commonTypeOperations) {
        this.commonTypeOperations = commonTypeOperations;
    }

    @Override
    public AnnotationTypeDefinition addType(AnnotationTypeDefinition newTypeDefinition) {
        AnnotationTypeData annotationTypeData = new AnnotationTypeData(newTypeDefinition);
        String uniqueId = UniqueIdBuilder.buildTypeUid(newTypeDefinition.getType(), newTypeDefinition.getVersion(), "annotationtype");
        annotationTypeData.setInitialCreationProperties(uniqueId);
        commonTypeOperations.addType(annotationTypeData, AnnotationTypeData.class);
        commonTypeOperations.addProperties(uniqueId, NodeTypeEnum.AnnotationType, newTypeDefinition.getProperties());
        return getType(uniqueId);
    }

    @Override
    public AnnotationTypeDefinition getType(String uniqueId) {
        return commonTypeOperations.getType(uniqueId, AnnotationTypeData.class, NodeTypeEnum.AnnotationType).map(this::populateTypeDefinition)
            .orElse(null);
    }

    private AnnotationTypeDefinition populateTypeDefinition(@NotNull AnnotationTypeData annotationTypeData) {
        AnnotationTypeDefinition annotationTypeDefinition = new AnnotationTypeDefinition(annotationTypeData.getAnnotationTypeDataDefinition());
        commonTypeOperations
            .fillProperties(annotationTypeDefinition.getUniqueId(), NodeTypeEnum.AnnotationType, annotationTypeDefinition::setProperties);
        return annotationTypeDefinition;
    }

    @Override
    public AnnotationTypeDefinition getLatestType(String type) {
        return commonTypeOperations.getLatestType(type, AnnotationTypeData.class, NodeTypeEnum.AnnotationType).map(this::populateTypeDefinition)
            .orElse(null);
    }

    @Override
    public boolean isSameType(AnnotationTypeDefinition type1, AnnotationTypeDefinition type2) {
        return type1.isSameDefinition(type2);
    }

    @Override
    public AnnotationTypeDefinition updateType(AnnotationTypeDefinition currentTypeDefinition, AnnotationTypeDefinition updatedTypeDefinition) {
        AnnotationTypeData updatedTypeData = new AnnotationTypeData(updatedTypeDefinition);
        updatedTypeData.setUpdateProperties(currentTypeDefinition);
        commonTypeOperations
            .updateType(updatedTypeData, updatedTypeDefinition.getProperties(), AnnotationTypeData.class, NodeTypeEnum.AnnotationType);
        return getType(updatedTypeData.getUniqueId());
    }
}
