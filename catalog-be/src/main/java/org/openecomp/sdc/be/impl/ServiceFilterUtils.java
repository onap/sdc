/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.sdc.be.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.javatuples.Pair;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.ui.model.UIConstraint;

public class ServiceFilterUtils {

    private ServiceFilterUtils() {
    }

    public static boolean isNodeFilterAffectedByPropertyRemoval(Service service, String ciName, String propertyName) {
        return service.getComponentInstances().stream().filter(ci -> ci.getNodeFilter() != null)
            .anyMatch(ci -> propertyIsUsedInCI(ci, ciName, propertyName));
    }

    private static boolean propertyIsUsedInCI(ComponentInstance ci, String ciName, String propertyName) {
        if (CollectionUtils.isEmpty(ci.getDirectives())) {
            return false;
        }
        if (ci.getNodeFilter() == null || ci.getNodeFilter().getProperties() == null
            || ci.getNodeFilter().getProperties().getListToscaDataDefinition() == null) {
            return false;
        }
        return ci.getNodeFilter().getProperties().getListToscaDataDefinition().stream().flatMap(prop -> prop.getConstraints().stream())
            .map(String::new)
            .map(constraint -> new ConstraintConvertor().convert(constraint))
            .filter(constraint ->
                List.of(ConstraintConvertor.PROPERTY_CONSTRAINT, ToscaFunctionType.GET_PROPERTY.getName()).contains(constraint.getSourceType())
            )
            .anyMatch(constraint -> constraint.getSourceName().equals(ciName) && extractGetFunctionValue(constraint).contains(propertyName));
    }

    private static List<String> extractGetFunctionValue(final UIConstraint uiConstraint) {
        final Object value = uiConstraint.getValue();
        if (value instanceof String) {
            return List.of((String) value);
        } else if (value instanceof Map) {
            final ToscaFunctionType toscaFunctionType = ToscaFunctionType.findType(uiConstraint.getSourceType()).orElse(null);
            if (toscaFunctionType != null) {
                final Object getFunctionValue = ((Map<?, ?>) uiConstraint.getValue()).get(toscaFunctionType.getName());
                if (getFunctionValue instanceof String) {
                    return List.of((String) getFunctionValue);
                }
                if (getFunctionValue instanceof List) {
                    final List<?> getFunctionValueList = (List<?>) getFunctionValue;
                    if (!(getFunctionValueList).isEmpty() && (getFunctionValueList).get(0) instanceof String) {
                        return (List<String>) getFunctionValueList;
                    }
                }
            }
        }
        return List.of();
    }

    public static Map<String, CINodeFilterDataDefinition> getRenamedNodesFilter(Service service, String oldName, String newName) {
        return service.getComponentInstances().stream().filter(ci -> isNodeFilterUsingChangedCi(ci, oldName))
            .map(ci -> renameOldCiNames(ci, oldName, newName)).collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
    }

    private static Pair<String, CINodeFilterDataDefinition> renameOldCiNames(ComponentInstance ci, String oldName, String newName) {
        ci.getNodeFilter().getProperties().getListToscaDataDefinition().stream()
            .filter(property -> isPropertyConstraintChangedByCi(property, oldName))
            .forEach(property -> renamePropertyCiNames(property, oldName, newName));
        return new Pair<>(ci.getUniqueId(), ci.getNodeFilter());
    }

    private static void renamePropertyCiNames(RequirementNodeFilterPropertyDataDefinition property, String oldName, String newName) {
        final List<String> constraints = property.getConstraints().stream().map(getConstraintString(oldName, newName)).collect(Collectors.toList());
        property.setConstraints(constraints);
    }

    private static Function<String, String> getConstraintString(String oldName, String newName) {
        return constraint -> {
            final ConstraintConvertor constraintConvertor = new ConstraintConvertor();
            UIConstraint uiConstraint = constraintConvertor.convert(constraint);
            if (uiConstraint.getSourceName().equals(oldName)) {
                uiConstraint.setSourceName(newName);
                final List<String> getFunctionValueList = extractGetFunctionValue(uiConstraint);
                if (!getFunctionValueList.isEmpty()) {
                    if (getFunctionValueList.size() == 1) {
                        uiConstraint.setValue(getFunctionValueList.get(0).replace(oldName, newName));
                    } else {
                        ToscaFunctionType.findType(uiConstraint.getSourceType()).ifPresent(toscaFunctionType -> {
                            getFunctionValueList.set(0, newName);
                            uiConstraint.setValue(Map.of(toscaFunctionType.getName(), getFunctionValueList));
                        });
                    }
                }
            }
            return constraintConvertor.convert(uiConstraint);
        };
    }

    public static Set<String> getNodesFiltersToBeDeleted(Service service, String ciName) {
        return service.getComponentInstances().stream().filter(ci -> isNodeFilterUsingChangedCi(ci, ciName)).map(ComponentInstance::getName)
            .collect(Collectors.toSet());
    }

    public static Set<String> getNodesFiltersToBeDeleted(Service service, ComponentInstance inCi) {
        return getNodesFiltersToBeDeleted(service, inCi.getName());
    }

    private static boolean isNodeFilterUsingChangedCi(ComponentInstance ci, String name) {
        if (CollectionUtils.isEmpty(ci.getDirectives())) {
            return false;
        }
        if (ci.getNodeFilter() == null || ci.getNodeFilter().getProperties() == null
            || ci.getNodeFilter().getProperties().getListToscaDataDefinition() == null) {
            return false;
        }
        return ci.getNodeFilter().getProperties().getListToscaDataDefinition().stream()
            .anyMatch(property -> isPropertyConstraintChangedByCi(property, name));
    }

    private static boolean isPropertyConstraintChangedByCi(RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition,
                                                           String name) {
        List<String> constraints = requirementNodeFilterPropertyDataDefinition.getConstraints();
        if (constraints == null) {
            return false;
        }
        return constraints.stream().anyMatch(constraint -> isConstraintChangedByCi(constraint, name));
    }

    private static boolean isConstraintChangedByCi(String constraint, String name) {
        UIConstraint uiConstraint = new ConstraintConvertor().convert(constraint);
        if (uiConstraint == null || uiConstraint.getSourceType() == null) {
            return false;
        }
        if (!List.of(ConstraintConvertor.PROPERTY_CONSTRAINT, ToscaFunctionType.GET_PROPERTY.getName())
            .contains(uiConstraint.getSourceType())) {
            return false;
        }
        return uiConstraint.getSourceName().equals(name);
    }

    public static Set<String> getNodesFiltersToBeDeleted(Service service, InputDefinition changedInput) {
        return service.getComponentInstances().stream().filter(ci -> isNodeFilterUsingChangedInput(ci, changedInput)).map(ComponentInstance::getName)
            .collect(Collectors.toSet());
    }

    private static boolean isNodeFilterUsingChangedInput(ComponentInstance ci, InputDefinition changedInput) {
        if (CollectionUtils.isEmpty(ci.getDirectives())) {
            return false;
        }
        return ci.getNodeFilter().getProperties().getListToscaDataDefinition().stream()
            .anyMatch(property -> isPropertyConstraintChangedByInput(property, changedInput));
    }

    private static boolean isPropertyConstraintChangedByInput(RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition,
                                                              InputDefinition changedInput) {
        List<String> constraints = requirementNodeFilterPropertyDataDefinition.getConstraints();
        return constraints.stream().anyMatch(constraint -> isConstraintChangedByInput(constraint, changedInput));
    }

    private static boolean isConstraintChangedByInput(String constraint, InputDefinition changedInput) {
        final UIConstraint uiConstraint = new ConstraintConvertor().convert(constraint);
        if (!List.of(ConstraintConvertor.SERVICE_INPUT_CONSTRAINT, ToscaFunctionType.GET_INPUT.getName()).contains(uiConstraint.getSourceType())) {
            return false;
        }
        final List<String> getFunctionValue = extractGetFunctionValue(uiConstraint);
        return getFunctionValue.contains(changedInput.getName());
    }
}
