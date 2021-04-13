
/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.components.attribute;

import fj.data.Either;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetOutputValueDataDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@org.springframework.stereotype.Component
public class ComponentAttributeDeclarator extends DefaultAttributeDeclarator<Component, AttributeDataDefinition> {

    private final ToscaOperationFacade toscaOperationFacade;

    public ComponentAttributeDeclarator(final ToscaOperationFacade toscaOperationFacade) {
        this.toscaOperationFacade = toscaOperationFacade;
    }

    @Override
    public AttributeDataDefinition createDeclaredAttribute(final AttributeDataDefinition attributeDataDefinition) {
        return new AttributeDataDefinition(attributeDataDefinition);
    }

    @Override
    public Either<?, StorageOperationStatus> updateAttributesValues(final Component component, final String propertiesOwnerId,
                                                                    final List<AttributeDataDefinition> attributetypeList) {
        if (CollectionUtils.isNotEmpty(attributetypeList)) {
            for (AttributeDataDefinition attribute : attributetypeList) {
                Either<AttributeDefinition, StorageOperationStatus> storageStatus = toscaOperationFacade
                    .updateAttributeOfComponent(component, new AttributeDefinition(attribute));
                if (storageStatus.isRight()) {
                    return Either.right(storageStatus.right().value());
                }
            }
        }
        return Either.left(attributetypeList);
    }

    @Override
    public Optional<Component> resolvePropertiesOwner(final Component component, final String propertiesOwnerId) {
        return Optional.of(component);
    }

    @Override
    public StorageOperationStatus unDeclareAttributesAsOutputs(final Component component, final OutputDefinition output) {
        final Optional<AttributeDefinition> attributeToUpdateCandidate = getDeclaredAttributeByOutputId(component, output.getUniqueId());
        if (attributeToUpdateCandidate.isPresent()) {
            AttributeDefinition attributeToUpdate = attributeToUpdateCandidate.get();
            return unDeclareOutput(component, output, attributeToUpdate);
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus unDeclareOutput(final Component component, final OutputDefinition output,
                                                   final AttributeDefinition attributeToUpdate) {
        attributeToUpdate.setValue(output.getDefaultValue());
        Either<AttributeDefinition, StorageOperationStatus> status = toscaOperationFacade.updateAttributeOfComponent(component, attributeToUpdate);
        if (status.isRight()) {
            return status.right().value();
        }
        return StorageOperationStatus.OK;
    }

    private Optional<AttributeDefinition> getDeclaredAttributeByOutputId(final Component component, final String outputId) {
        List<AttributeDefinition> attributes = component.getAttributes();
        if (CollectionUtils.isEmpty(attributes)) {
            return Optional.empty();
        }
        for (AttributeDefinition attributeDefinition : attributes) {
            List<GetOutputValueDataDefinition> getOutputValues = attributeDefinition.getGetOutputValues();
            if (CollectionUtils.isEmpty(getOutputValues)) {
                continue;
            }
            Optional<GetOutputValueDataDefinition> getOutputCandidate = getOutputValues.stream()
                .filter(getOutput -> getOutput.getOutputId().equals(outputId)).findAny();
            if (getOutputCandidate.isPresent()) {
                return Optional.of(attributeDefinition);
            }
        }
        return Optional.empty();
    }
}
