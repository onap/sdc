/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.components.merge.output;

import static org.openecomp.sdc.be.utils.AttributeDefinitionUtils.getAttributesMappedToOutputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetOutputValueDataDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;

@org.springframework.stereotype.Component
public class DeclaredOutputsResolver {

    public List<OutputDefinition> getPreviouslyDeclaredOutputsToMerge(List<OutputDefinition> oldOutputs, List<AttributeDataDefinition> attributes,
                                                                      String instanceId) {
        List<AttributeDataDefinition> attributesMappedToOutputsOnNewInstance = getAttributesMappedToOutputs(attributes);
        return createOutputs(oldOutputs, attributesMappedToOutputsOnNewInstance, instanceId);
    }

    private List<OutputDefinition> createOutputs(List<OutputDefinition> oldOutputs,
                                                 List<AttributeDataDefinition> attributesMappedToOutputsOnNewInstance, String instanceId) {
        Map<String, OutputDefinition> oldOutputsById = MapUtil.toMap(oldOutputs, OutputDefinition::getUniqueId);
        List<OutputDefinition> outputsToRedeclare = new ArrayList<>();
        attributesMappedToOutputsOnNewInstance.forEach(attribute -> {
            List<OutputDefinition> outputDefinitions = prepareOutputsForRedeclaration(oldOutputsById, attribute, instanceId);
            outputsToRedeclare.addAll(outputDefinitions);
        });
        return outputsToRedeclare;
    }

    private List<OutputDefinition> prepareOutputsForRedeclaration(Map<String, OutputDefinition> oldOutputsById,
                                                                  AttributeDataDefinition attributeToCreateOutputFor, String instanceID) {
        List<GetOutputValueDataDefinition> getOutputValueDataDefinition = attributeToCreateOutputFor.getGetOutputValues();
        List<OutputDefinition> outputsForRedeclaration = new ArrayList<>();
        for (GetOutputValueDataDefinition out : getOutputValueDataDefinition) {
            if (oldOutputsById.containsKey(out.getOutputId())) {
                OutputDefinition test = oldOutputsById.get(out.getOutputId());
                test.setAttributeId(attributeToCreateOutputFor.getUniqueId());
                test.setInstanceUniqueId(instanceID);
                outputsForRedeclaration.add(test);
            }
        }
        return outputsForRedeclaration;
    }
}
