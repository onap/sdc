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

package org.openecomp.sdc.be.components.merge.property;

import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class PropertyInstanceMergeDataBuilder {

    private PropertyInstanceMergeDataBuilder() {
    }

    static <T extends PropertyDataDefinition> List<MergePropertyData> buildDataForMerging(List<T> oldProps,
                                                                                          List<InputDefinition> oldInputs,
                                                                                          List<T> newProps,
                                                                                          List<InputDefinition> newInputs) {

        Map<String, T> oldPropsByName = MapUtil.toMap(oldProps, T::getName);
        Map<String, InputDefinition> oldInputsByName = MapUtil.toMap(oldInputs, InputDefinition::getName);
        Map<String, T> newPropsByName = MapUtil.toMap(newProps, T::getName);
        Map<String, InputDefinition> newInputsByName = MapUtil.toMap(newInputs, InputDefinition::getName);
        return buildMergeData(oldPropsByName, oldInputsByName, newPropsByName, newInputsByName);

    }

    private static <T extends PropertyDataDefinition> List<MergePropertyData> buildMergeData(Map<String, T> oldPropsByName, Map<String, InputDefinition> oldInputsByName, Map<String, T> newPropsByName, Map<String, InputDefinition> newInputsByName) {
        List<MergePropertyData> mergeData = new ArrayList<>();
        newPropsByName.forEach((name, prop) -> {
            if (oldPropsByName.containsKey(name)) {
                mergeData.add(buildMergePropertyData(oldPropsByName.get(name), oldInputsByName, prop, newInputsByName));
            }
        });
        return mergeData;
    }

    private static MergePropertyData buildMergePropertyData(PropertyDataDefinition oldProp,
                                                    Map<String, InputDefinition> oldInputsByName,
                                                    PropertyDataDefinition newProp,
                                                    Map<String, InputDefinition> newInputsByName) {
        MergePropertyData mergePropertyData = new MergePropertyData();
        mergePropertyData.setOldProp(oldProp)
                         .setNewProp(newProp);
        if (oldProp.isGetInputProperty()) {
            setGetInputData(oldProp, oldInputsByName, newInputsByName, mergePropertyData);

        }
        return mergePropertyData;
    }

    private static void setGetInputData(PropertyDataDefinition oldProp, Map<String, InputDefinition> oldInputsByName, Map<String, InputDefinition> newInputsByName, MergePropertyData mergePropertyData) {
        List<String> oldDeclaredByUserInputNames = getOldDeclaredInputsByUser(oldProp.getGetInputValues(), oldInputsByName);
        List<String> oldGetInputNamesWhichExistInNewVersion = getOldGetInputNamesWhichExistInNewVersion(oldProp.getGetInputValues(), newInputsByName);
        mergePropertyData.addAddGetInputNamesToMerge(oldDeclaredByUserInputNames);
        mergePropertyData.addAddGetInputNamesToMerge(oldGetInputNamesWhichExistInNewVersion);
    }

    private static List<String> getOldGetInputNamesWhichExistInNewVersion(List<GetInputValueDataDefinition> getInputValues, Map<String, InputDefinition> newInputsByName) {
        return getInputValues.stream().map(GetInputValueDataDefinition::getInputName).filter(newInputsByName::containsKey).collect(Collectors.toList());
    }

    private static List<String> getOldDeclaredInputsByUser(List<GetInputValueDataDefinition> getInputValues, Map<String, InputDefinition> oldInputsByName) {
        return getInputValues.stream().map(GetInputValueDataDefinition::getInputName)
                                      .map(oldInputsByName::get)
                                      .filter(oldInput ->  Objects.nonNull(oldInput) && oldInput.getInstanceUniqueId() != null)
                                      .map(PropertyDataDefinition::getName)
                                      .collect(Collectors.toList());
    }
}
