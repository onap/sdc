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
package org.openecomp.sdc.be.datamodel.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ConstraintConvertor {

    public static final String EQUAL_OPERATOR = ConstraintType.EQUAL.getTypes().get(1);
    public static final String GREATER_THAN_OPERATOR = ConstraintType.GREATER_THAN.getTypes().get(1);
    public static final String LESS_THAN_OPERATOR = ConstraintType.LESS_THAN.getTypes().get(1);
    public static final String GREATER_OR_EQUAL_OPERATOR = ConstraintType.GREATER_OR_EQUAL.getTypes().get(1);
    public static final String LESS_OR_EQUAL_OPERATOR = ConstraintType.LESS_OR_EQUAL.getTypes().get(1);
    public static final String STATIC_CONSTRAINT = "static";
    public static final String PROPERTY_CONSTRAINT = "property";
    public static final String SERVICE_INPUT_CONSTRAINT = "service_input";
    public static final String SELF = "SELF";
    private static final Logger logger = LoggerFactory.getLogger(ConstraintConvertor.class);
    private static final Set<String> SUPPORTED_CONSTRAINT_LIST =
        Set.of(EQUAL_OPERATOR, GREATER_THAN_OPERATOR, LESS_THAN_OPERATOR, GREATER_OR_EQUAL_OPERATOR, LESS_OR_EQUAL_OPERATOR);

    public UIConstraint convert(final String constraintValue) {
        return convert(constraintValue, null);
    }

    public UIConstraint convert(final String inConstraint, final String valueType) {
        Yaml yamlSource = new Yaml();
        UIConstraint uiConstraint = new UIConstraint();
        Object content1 = yamlSource.load(inConstraint);
        if (!(content1 instanceof Map)) {
            return null;
        }
        Map propertyAndConstraintMap = (Map) content1;
        Object propertyNameKey = propertyAndConstraintMap.keySet().iterator().next();
        uiConstraint.setServicePropertyName(propertyNameKey.toString());
        Object operatorMapObj = propertyAndConstraintMap.get(propertyNameKey);
        if (!(operatorMapObj instanceof Map)) {
            return null;
        }
        Map operatorMap = (Map) operatorMapObj;
        Object operatorKey = operatorMap.keySet().iterator().next();
        final String operator = (String) operatorKey;
        if (SUPPORTED_CONSTRAINT_LIST.contains(operator)) {
            uiConstraint.setConstraintOperator(operator);
        }
        Object constraintValueObj = operatorMap.get(operatorKey);
        if (constraintValueObj instanceof String || constraintValueObj instanceof Number || constraintValueObj instanceof Boolean) {
            uiConstraint.setValue(constraintValueObj);
            uiConstraint.setSourceType(STATIC_CONSTRAINT);
            uiConstraint.setSourceName(STATIC_CONSTRAINT);
            return uiConstraint;
        } else if (constraintValueObj instanceof List) {
            uiConstraint.setSourceType(STATIC_CONSTRAINT);
            uiConstraint.setSourceName(STATIC_CONSTRAINT);
            uiConstraint.setValue(constraintValueObj);
            return uiConstraint;
        } else if ("string".equals(valueType)) {
            uiConstraint.setSourceType(STATIC_CONSTRAINT);
            uiConstraint.setSourceName(STATIC_CONSTRAINT);
            uiConstraint.setValue(dumpYamlString(constraintValueObj));
            return uiConstraint;
        } else if (constraintValueObj instanceof Map) {
            return handleMap(uiConstraint, (Map<Object, Object>) constraintValueObj);
        }
        return null;
    }

    private String dumpYamlString(final Object constraintValueObj) {
        final var dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        return new Yaml(dumperOptions).dump(constraintValueObj);
    }

    private UIConstraint handleMap(final UIConstraint uiConstraint, final Map<Object, Object> constraintValueAsMap) {
        final Map.Entry<Object, Object> entry = constraintValueAsMap.entrySet().iterator().next();
        final String firstKey = entry.getKey().toString().trim();
        final ToscaFunctionType toscaFunctionType = ToscaFunctionType.findType(firstKey).orElse(null);
        if (toscaFunctionType == null) {
            uiConstraint.setValue(constraintValueAsMap);
            return uiConstraint;
        }
        uiConstraint.setValue(constraintValueAsMap);
        uiConstraint.setSourceType(toscaFunctionType.getName());
        switch (toscaFunctionType) {
            case GET_INPUT:
                uiConstraint.setSourceName(PropertySource.SELF.getName());
                break;
            case GET_PROPERTY:
            case GET_ATTRIBUTE:
                final List<String> value = (List<String>) entry.getValue();
                uiConstraint.setSourceName(value.get(0));
                break;
            default:
                break;
        }

        return uiConstraint;
    }

    public List<String> convertToList(List<UIConstraint> uiConstraints) {
        List<String> retVal = new ArrayList<>();
        for (UIConstraint uiConstraint : uiConstraints) {
            String constraint = convert(uiConstraint);
            if (constraint != null) {
                retVal.add(constraint);
            }
        }
        return retVal;
    }

    public String convert(final UIConstraint uiConstraint) {
        try {
            final Map<String, Object> constraintAsMap = new HashMap<>();
            switch (uiConstraint.getSourceType()) {
                case STATIC_CONSTRAINT: {
                    Object value = uiConstraint.getValue();
                    if (value instanceof String) {
                        value = new Yaml().load(value.toString());
                    }
                    constraintAsMap.put(uiConstraint.getConstraintOperator(), value);
                    break;
                }
                case PROPERTY_CONSTRAINT:
                    constraintAsMap.put(uiConstraint.getConstraintOperator(),
                        Map.of(ToscaFunctions.GET_PROPERTY.getFunctionName(), List.of(uiConstraint.getSourceName(), uiConstraint.getValue()))
                    );
                    break;
                case SERVICE_INPUT_CONSTRAINT:
                    constraintAsMap.put(uiConstraint.getConstraintOperator(), Map.of(ToscaFunctions.GET_INPUT.getFunctionName(), uiConstraint.getValue()));
                    break;
                default: {
                    if (ToscaFunctionType.findType(uiConstraint.getSourceType()).isPresent()) {
                        Object value = uiConstraint.getValue();
                        if (value instanceof String) {
                            value = new Yaml().load((String) value);
                        }
                        constraintAsMap.put(uiConstraint.getConstraintOperator(), value);
                    }
                }
            }
            return new Yaml().dump(Map.of(uiConstraint.getServicePropertyName(), constraintAsMap));
        } catch (final Exception ex) {
            logger.error("Could not convert constraint", ex);
        }
        return null;
    }

    public UIConstraint getUiConstraint(final String inConstraint, final UIConstraint uiConstraint) {
        final Object constraintObject = new Yaml().load(inConstraint);
        if (!(constraintObject instanceof Map)) {
            return null;
        }
        final Map constraintMap = (Map) constraintObject;
        final Object capabilityName = constraintMap.keySet().iterator().next();
        uiConstraint.setServicePropertyName(capabilityName.toString());
        Object capabilityProperties = constraintMap.get(capabilityName);
        if (!(capabilityProperties instanceof Map)) {
            return null;
        }
        final Map capabilityPropertiesMap = (Map) capabilityProperties;
        final Object constraintOperator = capabilityPropertiesMap.keySet().iterator().next();
        final String operator = constraintOperator.toString();
        if (SUPPORTED_CONSTRAINT_LIST.contains(operator)) {
            uiConstraint.setConstraintOperator(operator);
        }
        final Object constraintValue = capabilityPropertiesMap.get(constraintOperator);
        if (constraintValue instanceof String || constraintValue instanceof Number || constraintValue instanceof Boolean) {
            uiConstraint.setValue(constraintValue);
            uiConstraint.setSourceType(STATIC_CONSTRAINT);
            uiConstraint.setSourceName(STATIC_CONSTRAINT);
            return uiConstraint;
        } else if (constraintValue instanceof List) {
            uiConstraint.setSourceType(STATIC_CONSTRAINT);
            uiConstraint.setSourceName(STATIC_CONSTRAINT);
            uiConstraint.setValue(constraintValue);
            return uiConstraint;
        } else if (constraintValue instanceof Map) {
            return handleMap(uiConstraint, (Map<Object, Object>) constraintValue);
        }
        return null;
    }
}
