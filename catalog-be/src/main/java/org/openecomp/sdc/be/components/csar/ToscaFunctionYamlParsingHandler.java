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

package org.openecomp.sdc.be.components.csar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.CustomYamlFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaCustomFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionParameter;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaStringParameter;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

@org.springframework.stereotype.Component
public class ToscaFunctionYamlParsingHandler {

    private static Optional<ToscaFunction> handleGetPropertyFunction(Map<String, Object> toscaFunctionPropertyValueMap, String functionType,
                                                                     ToscaFunctionType toscaFunctionType) {
        final ToscaGetFunctionDataDefinition toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(
            toscaFunctionType == ToscaFunctionType.GET_PROPERTY ? ToscaGetFunctionType.GET_PROPERTY : ToscaGetFunctionType.GET_ATTRIBUTE
        );
        final Object functionValueObj = toscaFunctionPropertyValueMap.get(functionType);
        if (!(functionValueObj instanceof List)) {
            return Optional.empty();
        }
        final List<String> functionParameters;
        try {
            functionParameters = ((List<Object>) functionValueObj).stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());
        } catch (final ClassCastException ignored) {
            return Optional.empty();
        }
        if (functionParameters.size() < 2) {
            return Optional.empty();
        }
        final String propertySourceType = functionParameters.get(0);
        final PropertySource propertySource = PropertySource.findType(propertySourceType).orElse(null);
        if (propertySource == PropertySource.SELF) {
            toscaGetFunction.setPropertySource(propertySource);
        } else {
            toscaGetFunction.setPropertySource(PropertySource.INSTANCE);
            toscaGetFunction.setSourceName(propertySourceType);
        }
        List<String> propertySourceIndex = functionParameters.subList(1, functionParameters.size());
        List<String> propertySourcePath = new ArrayList<>();
        propertySourcePath.add((String)propertySourceIndex.get(0));
        if (propertySourceIndex.size() > 1 ) {
            List<Object> indexParsedList = new ArrayList<Object>();
            List<String> indexObjectList = propertySourceIndex.subList(1,propertySourceIndex.size());
            boolean loopFlag = true;
            for (String indexValue : indexObjectList) {
                if (!indexValue.equalsIgnoreCase("INDEX") && !StringUtils.isNumeric(indexValue) && loopFlag) {
                    propertySourcePath.add(indexValue);
                } else {
                    loopFlag = false;
                    if (StringUtils.isNumeric(indexValue)) {
                        indexParsedList.add(Integer.parseInt(indexValue));
                    } else {
                        indexParsedList.add(indexValue);
                    }
                }
            }
            toscaGetFunction.setToscaIndexList(indexParsedList);
        }
        toscaGetFunction.setPropertyPathFromSource(propertySourcePath);
        final String propertyName = toscaGetFunction.getPropertyPathFromSource().get(toscaGetFunction.getPropertyPathFromSource().size() - 1);
        toscaGetFunction.setPropertyName(propertyName);
        return Optional.of(toscaGetFunction);
    }

    private static Optional<ToscaFunction> handleGetInputFunction(Map<String, Object> toscaFunctionPropertyValueMap, String functionType) {
        final ToscaGetFunctionDataDefinition toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        toscaGetFunction.setPropertySource(PropertySource.SELF);
        final Object functionValueObj = toscaFunctionPropertyValueMap.get(functionType);
        if (!(functionValueObj instanceof List) && !(functionValueObj instanceof String)) {
            return Optional.empty();
        }
        if (functionValueObj instanceof String) {
            toscaGetFunction.setPropertyPathFromSource(List.of((String) functionValueObj));
        } else {
            final List<String> functionParameters;
            try {
                functionParameters = ((List<Object>) functionValueObj).stream()
                    .map(object -> Objects.toString(object, null))
                    .collect(Collectors.toList());
            } catch (final ClassCastException ignored) {
                return Optional.empty();
            }
            List<String> propertySourcePath = new ArrayList<>();
            propertySourcePath.add((String)functionParameters.get(0));
            if (functionParameters.size() > 1 ) {
                List<Object> indexParsedList = new ArrayList<Object>();
                List<String> indexObjectList = functionParameters.subList(1,functionParameters.size());
                boolean loopFlag = true;
                for (String indexValue : indexObjectList) {
                    if (!indexValue.equalsIgnoreCase("INDEX") && !StringUtils.isNumeric(indexValue) && loopFlag) {
                        propertySourcePath.add(indexValue);
                    } else {
                        loopFlag = false;
                        if (StringUtils.isNumeric(indexValue)) {
                            indexParsedList.add(Integer.parseInt(indexValue));
                        } else {
                            indexParsedList.add(indexValue);
                        }
                    }
                }
                toscaGetFunction.setToscaIndexList(indexParsedList);
            }
            toscaGetFunction.setPropertyPathFromSource(propertySourcePath);
        }
        final String propertyName = toscaGetFunction.getPropertyPathFromSource().get(toscaGetFunction.getPropertyPathFromSource().size() - 1);
        toscaGetFunction.setPropertyName(propertyName);
        return Optional.of(toscaGetFunction);
    }

    /**
     * Builds a {@link ToscaFunction} based on the property value. It will build the object with the maximum information available in the property
     * value, as not all the necessary information can be extracted from it. It will only parse values from supported functions in
     * {@link ToscaFunctionType}.
     *
     * @param toscaFunctionPropertyValueMap the value of a property calls a TOSCA function
     * @return the partially filled {@link ToscaFunction} object
     */
    public Optional<ToscaFunction> buildToscaFunctionBasedOnPropertyValue(final Map<String, Object> toscaFunctionPropertyValueMap) {
        if (!isPropertyValueToscaFunction(toscaFunctionPropertyValueMap)) {
            return Optional.empty();
        }
        final String functionType = toscaFunctionPropertyValueMap.keySet().iterator().next();
        final ToscaFunctionType toscaFunctionType =
            ToscaFunctionType.findType(functionType).orElse(functionType.startsWith("$") ? ToscaFunctionType.CUSTOM : null);
        if (toscaFunctionType == null) {
            return Optional.empty();
        }
        switch (toscaFunctionType) {
            case GET_INPUT: {
                return handleGetInputFunction(toscaFunctionPropertyValueMap, functionType);
            }
            case GET_PROPERTY:
            case GET_ATTRIBUTE: {
                return handleGetPropertyFunction(toscaFunctionPropertyValueMap, functionType, toscaFunctionType);
            }
            case CONCAT:
                return handleConcatFunction(toscaFunctionPropertyValueMap, functionType);
            case CUSTOM:
                return handleCustomFunction(toscaFunctionPropertyValueMap, functionType);
            default:
                return Optional.empty();
        }
    }

    private Optional<ToscaFunction> handleCustomFunction(Map<String, Object> toscaFunctionPropertyValueMap, String functionType) {
        final ToscaCustomFunction toscaCustomFunction = new ToscaCustomFunction();
        toscaCustomFunction.setName(functionType.substring(1));
        final Object functionValueObj = toscaFunctionPropertyValueMap.get(functionType);
        toscaCustomFunction.setToscaFunctionType(getCustomFunctionType(toscaCustomFunction.getName()));
        if (ToscaFunctionType.GET_INPUT.equals(toscaCustomFunction.getToscaFunctionType())) {
            return handelCustomFunctionGetInputType(toscaCustomFunction, functionValueObj);
        }
        return handelCustomFunctionCustomType(toscaCustomFunction, functionValueObj);
    }

    private Optional<ToscaFunction> handelCustomFunctionCustomType(ToscaCustomFunction toscaCustomFunction, Object functionValueObj) {
        if (!(functionValueObj instanceof List)) {
            return Optional.empty();
        }
        final List<Object> functionParameters = (List<Object>) functionValueObj;
        functionParameters.forEach(parameter -> {
            if (parameter instanceof String) {
                final var stringParameter = new ToscaStringParameter();
                stringParameter.setValue((String) parameter);
                toscaCustomFunction.addParameter(stringParameter);
                return;
            }
            if (isPropertyValueToscaFunction(parameter)) {
                buildToscaFunctionBasedOnPropertyValue((Map<String, Object>) parameter).ifPresent(toscaFunction -> {
                    if (toscaFunction instanceof ToscaFunctionParameter) {
                        toscaCustomFunction.addParameter((ToscaFunctionParameter) toscaFunction);
                    }
                });
                return;
            }
            final var customYamlFunction = new CustomYamlFunction();
            customYamlFunction.setYamlValue(parameter);
            toscaCustomFunction.addParameter(customYamlFunction);
        });
        return Optional.of(toscaCustomFunction);
    }

    private Optional<ToscaFunction> handelCustomFunctionGetInputType(ToscaCustomFunction toscaCustomFunction, Object functionValueObj) {
        if (!(functionValueObj instanceof String) && !(functionValueObj instanceof List)) {
            return Optional.empty();
        }
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put(ToscaFunctionType.GET_INPUT.getName(), functionValueObj);
        buildToscaFunctionBasedOnPropertyValue(parameterMap).ifPresent(toscaFunction -> {
            if (toscaFunction instanceof ToscaFunctionParameter) {
                toscaCustomFunction.addParameter((ToscaFunctionParameter) toscaFunction);
            }
        });
        return Optional.of(toscaCustomFunction);
    }

    private ToscaFunctionType getCustomFunctionType(String name) {
        List<Configuration.CustomToscaFunction> customFunctions =
            ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultCustomToscaFunctions();
        if (CollectionUtils.isEmpty(customFunctions)) {
            return ToscaFunctionType.CUSTOM;
        }
        Optional<Configuration.CustomToscaFunction> optionalFunc = customFunctions.stream().filter(func -> func.getName().equals(name)).findFirst();
        if (optionalFunc.isEmpty()) {
            return ToscaFunctionType.CUSTOM;
        }
        String type = optionalFunc.get().getType();
        return ToscaFunctionType.findType(type).get();
    }

    /**
     * Checks if the property value is a supported TOSCA function.
     *
     * @param propValueObj the value of a property
     * @return {@code true} if the value is a supported TOSCA function, {@code false} otherwise
     */
    public boolean isPropertyValueToscaFunction(final Object propValueObj) {
        if (propValueObj instanceof Map) {
            final Map<String, Object> propValueMap = (Map<String, Object>) propValueObj;
            if (propValueMap.keySet().size() > 1) {
                return false;
            }
            if (propValueMap.keySet().stream().anyMatch(keyValue -> keyValue.startsWith("$"))) {
                return true;
            }

            return Stream.of(ToscaFunctionType.GET_INPUT, ToscaFunctionType.GET_PROPERTY, ToscaFunctionType.GET_ATTRIBUTE, ToscaFunctionType.CONCAT)
                .anyMatch(type -> propValueMap.containsKey(type.getName()));
        }
        return false;
    }

    private Optional<ToscaFunction> handleConcatFunction(Map<String, Object> toscaFunctionPropertyValueMap, String functionType) {
        final ToscaConcatFunction toscaConcatFunction = new ToscaConcatFunction();
        final Object functionValueObj = toscaFunctionPropertyValueMap.get(functionType);
        if (!(functionValueObj instanceof List)) {
            return Optional.empty();
        }
        final List<Object> functionParameters = (List<Object>) functionValueObj;
        if (functionParameters.size() < 2) {
            return Optional.empty();
        }
        functionParameters.forEach(parameter -> {
            if (parameter instanceof String) {
                final var stringParameter = new ToscaStringParameter();
                stringParameter.setValue((String) parameter);
                toscaConcatFunction.addParameter(stringParameter);
                return;
            }
            if (isPropertyValueToscaFunction(parameter)) {
                buildToscaFunctionBasedOnPropertyValue((Map<String, Object>) parameter).ifPresent(toscaFunction -> {
                    if (toscaFunction instanceof ToscaFunctionParameter) {
                        toscaConcatFunction.addParameter((ToscaFunctionParameter) toscaFunction);
                    }
                });
                return;
            }
            final var customYamlFunction = new CustomYamlFunction();
            customYamlFunction.setYamlValue(parameter);
            toscaConcatFunction.addParameter(customYamlFunction);
        });
        return Optional.of(toscaConcatFunction);
    }

}
