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

import static org.openecomp.sdc.be.utils.AttributeDefinitionUtils.resolveGetOutputAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetOutputValueDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.OutputDefinition;

@org.springframework.stereotype.Component
public class DeclaredOutputsResolver {

    public List<OutputDefinition> getPreviouslyDeclaredOutputsToMerge(List<OutputDefinition> oldOutputs, Component newComponent,
                                                                      Map<String, List<AttributeDataDefinition>> attributes) {
        Map<String, List<AttributeDataDefinition>> getOutputAttributes = resolveGetOutputAttributes(attributes);
        List<RedeclareOutputData> outputsToRedeclareData = buildRedeclareOutputData(newComponent, getOutputAttributes);
        return findPrevDeclaredOutputs(oldOutputs, outputsToRedeclareData);
    }

    private List<RedeclareOutputData> buildRedeclareOutputData(Component newComponent, Map<String, List<AttributeDataDefinition>> getOutputAttributes) {
        Map<String, OutputDefinition> outputsById = MapUtil.toMap(newComponent.getOutputs(), OutputDefinition::getUniqueId);
        List<RedeclareOutputData> redeclareOutputData = new ArrayList<>();
        getOutputAttributes
            .forEach((instanceId, getOutputAttributes1) -> redeclareOutputData.addAll(findOutputsToRedeclare(outputsById, instanceId, getOutputAttributes1)));
        return redeclareOutputData;
    }

    private List<OutputDefinition> findPrevDeclaredOutputs(List<OutputDefinition> oldOutputs, List<RedeclareOutputData> outputsToRedeclareData) {
        Map<String, OutputDefinition> oldOutputsById = MapUtil.toMap(oldOutputs, OutputDefinition::getUniqueId);
        List<OutputDefinition> outputsToRedeclare = new ArrayList<>();
        outputsToRedeclareData.forEach(redeclareOutputData -> {
            List<OutputDefinition> outputDefinitions = prepareOutputsForRedeclaration(oldOutputsById, redeclareOutputData);
            outputsToRedeclare.addAll(outputDefinitions);
        });
        return outputsToRedeclare;
    }

    private List<RedeclareOutputData> findOutputsToRedeclare(Map<String, OutputDefinition> outputsById, String instanceId,
                                                             List<AttributeDataDefinition> getOutputAttributes) {
        List<RedeclareOutputData> redeclareOutputDataList = new ArrayList<>();
        getOutputAttributes.forEach(attribute -> {
            List<String> inputsToRedeclareIds = findOutputsToRedeclareIds(outputsById, attribute);
            RedeclareOutputData redeclareOutputData = new RedeclareOutputData(attribute.getUniqueId(), inputsToRedeclareIds, instanceId);
            redeclareOutputDataList.add(redeclareOutputData);
        });
        return redeclareOutputDataList;
    }

    private List<OutputDefinition> prepareOutputsForRedeclaration(Map<String, OutputDefinition> oldOutputsById, RedeclareOutputData redeclareOutputData) {
        List<OutputDefinition> outputsForRedeclaration = redeclareOutputData.declaredOutputIds.stream().filter(oldOutputsById::containsKey)
            .map(oldOutputsById::get).filter(Objects::nonNull).map(OutputDefinition::new).collect(Collectors.toList());
        outputsForRedeclaration.forEach(output -> {
            output.setAttributeId(redeclareOutputData.attributeId);
            output.setInstanceUniqueId(redeclareOutputData.attributeOwnerId);
        });
        return outputsForRedeclaration;
    }

    private List<String> findOutputsToRedeclareIds(Map<String, OutputDefinition> outputsById, AttributeDataDefinition attribute) {
        List<GetOutputValueDataDefinition> getOutputValues = attribute.getGetOutputValues();
        return getOutputValues.stream().filter(getOutputVal -> isGetOutputValueHasNoCorrespondingOutput(getOutputVal, outputsById))
            .map(GetOutputValueDataDefinition::getOutputId).collect(Collectors.toList());
    }

    private boolean isGetOutputValueHasNoCorrespondingOutput(GetOutputValueDataDefinition getOutputVal, Map<String, OutputDefinition> outputsById) {
        return !outputsById.containsKey(getOutputVal.getOutputId());
    }

    private static class RedeclareOutputData {

        private final String attributeId;
        private final List<String> declaredOutputIds;
        private final String attributeOwnerId;

        RedeclareOutputData(String attributeId, List<String> declaredOutputIds, String attributeOwnerId) {
            this.attributeId = attributeId;
            this.declaredOutputIds = declaredOutputIds;
            this.attributeOwnerId = attributeOwnerId;
        }
    }
}
