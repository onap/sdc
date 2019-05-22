/*
 * Copyright © 2016-2019 European Support Limited
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

package org.openecomp.sdc.be.tosca.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;
import org.openecomp.sdc.be.tosca.PropertyConvertor;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;

public class ToscaExportUtils {

    private ToscaExportUtils() {
        //Hiding implicit default constructor
    }

    public static Optional<Map<String, Object>> getProxyNodeTypeInterfaces(Component proxyComponent,
                                                                           Map<String, DataTypeDefinition> dataTypes) {
        if (Objects.isNull(proxyComponent) || MapUtils.isEmpty(proxyComponent.getInterfaces())) {
            return Optional.empty();
        }
        return Optional.ofNullable(InterfacesOperationsToscaUtil
                .getInterfacesMap(proxyComponent, null, proxyComponent.getInterfaces(), dataTypes, false, false));
    }

    public static Optional<Map<String, ToscaProperty>> getProxyNodeTypeProperties(Component proxyComponent,
                                                                                  Map<String, DataTypeDefinition>
                                                                                          dataTypes) {
        if (Objects.isNull(proxyComponent)) {
            return Optional.empty();
        }
        Map<String, ToscaProperty> proxyProperties = new HashMap<>();
        addInputsToProperties(dataTypes, proxyComponent.getInputs(), proxyProperties);
        if (CollectionUtils.isNotEmpty(proxyComponent.getProperties())) {
            proxyProperties.putAll(proxyComponent.getProperties().stream()
                    .collect(Collectors.toMap(PropertyDataDefinition::getName,
                            property -> PropertyConvertor.getInstance().convertProperty(dataTypes, property,
                                    PropertyConvertor.PropertyType.PROPERTY))));
        }
        resolvePropertyDefaultValueFromInput(proxyComponent.getInputs(), proxyProperties, dataTypes);

        return MapUtils.isNotEmpty(proxyProperties) ? Optional.of(proxyProperties) : Optional.empty();
    }


    public static void resolvePropertyDefaultValueFromInput(List<InputDefinition> componentInputs,
                                                      Map<String, ToscaProperty> mergedProperties,
                                                      Map<String, DataTypeDefinition> dataTypes) {
        if (MapUtils.isEmpty(mergedProperties) || CollectionUtils.isEmpty(componentInputs)) {
            return;
        }
        for (Map.Entry<String, ToscaProperty> mergedPropertyEntry : mergedProperties.entrySet()) {
            ToscaProperty value = mergedPropertyEntry.getValue();
            if (Objects.nonNull(value) && value.getDefaultp() instanceof Map) {
                Map<String, String> valueAsMap = (Map<String, String>) value.getDefaultp();
                String inputName = valueAsMap.get(ToscaFunctions.GET_INPUT.getFunctionName());
                Optional<InputDefinition> matchedInputDefinition = componentInputs.stream()
                        .filter(componentInput -> componentInput.getName().equals(inputName))
                        .findFirst();
                if (matchedInputDefinition.isPresent()) {
                    InputDefinition matchedInput = matchedInputDefinition.get();
                    Object resolvedDefaultValue = new PropertyConvertor().convertToToscaObject(matchedInput.getType(),
                            matchedInput.getDefaultValue(), matchedInput.getSchemaType(), dataTypes, false);
                    value.setDefaultp(resolvedDefaultValue);
                    mergedProperties.put(mergedPropertyEntry.getKey(), value);
                }
            }
        }
    }

    public static void addInputsToProperties(Map<String, DataTypeDefinition> dataTypes,
                                       List<InputDefinition> componentInputs,
                                       Map<String, ToscaProperty> mergedProperties) {
        if (CollectionUtils.isEmpty(componentInputs)) {
            return;
        }
        for(InputDefinition input : componentInputs) {
            ToscaProperty property = new PropertyConvertor().convertProperty(dataTypes, input,
                    PropertyConvertor.PropertyType.INPUT);
            mergedProperties.put(input.getName(), property);
        }
    }

}
