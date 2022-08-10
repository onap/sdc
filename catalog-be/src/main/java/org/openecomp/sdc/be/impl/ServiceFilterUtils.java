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
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionParameter;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.ui.mapper.FilterConstraintMapper;

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
        return ci.getNodeFilter().getProperties().getListToscaDataDefinition().stream()
            .flatMap(prop -> prop.getConstraints().stream())
            .filter(constraint ->
                List.of(ConstraintConvertor.PROPERTY_CONSTRAINT, ToscaFunctionType.GET_PROPERTY.getName())
                    .contains(constraint.getValueType().getName())
            )
            .map(new FilterConstraintMapper()::mapFrom)
            .anyMatch(constraint -> {
                final ToscaGetFunctionDataDefinition toscaGetFunction = constraint.getAsToscaGetFunction().orElse(null);
                if (toscaGetFunction == null) {
                    return false;
                }
                return toscaGetFunction.getSourceName().equals(ciName) && toscaGetFunction.getPropertyPathFromSource().contains(propertyName);
            });
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

    private static void renamePropertyCiNames(final PropertyFilterDataDefinition propertyFilter, final String oldInstanceName,
                                              final String newInstanceName) {
        final List<FilterValueType> instanceValueTypes =
            List.of(FilterValueType.GET_PROPERTY, FilterValueType.GET_ATTRIBUTE, FilterValueType.CONCAT);
        final List<PropertyFilterConstraintDataDefinition> constraints = propertyFilter.getConstraints().stream()
            .filter(propertyFilter1 -> instanceValueTypes.contains(propertyFilter1.getValueType()))
            .map(replaceConstraintsInstanceSource(oldInstanceName, newInstanceName))
            .collect(Collectors.toList());
        propertyFilter.setConstraints(constraints);
    }

    private static Function<PropertyFilterConstraintDataDefinition, PropertyFilterConstraintDataDefinition> replaceConstraintsInstanceSource(
        final String oldInstanceName, final String newInstanceName) {

        return constraint -> {
            final ToscaFunction toscaFunction = new FilterConstraintMapper().parseValueToToscaFunction(constraint.getValue()).orElse(null);
            if (toscaFunction == null) {
                return constraint;
            }
            renameToscaFunctionComponentInstance(toscaFunction, oldInstanceName, newInstanceName);
            return constraint;
        };
    }

    private static void renameToscaFunctionComponentInstance(final ToscaFunction toscaFunction, final String oldInstanceName,
                                                             final String newInstanceName) {
        switch (toscaFunction.getType()) {
            case GET_PROPERTY:
            case GET_ATTRIBUTE: {
                final ToscaGetFunctionDataDefinition toscaGetFunctionDataDefinition = (ToscaGetFunctionDataDefinition) toscaFunction;
                if (toscaGetFunctionDataDefinition.getSourceName().equals(oldInstanceName)) {
                    toscaGetFunctionDataDefinition.setSourceName(newInstanceName);
                }
                break;
            }
            case CONCAT: {
                final ToscaConcatFunction toscaConcatFunction = (ToscaConcatFunction) toscaFunction;
                for (final ToscaFunctionParameter parameter : toscaConcatFunction.getParameters()) {
                    switch (parameter.getType()) {
                        case GET_PROPERTY:
                        case GET_ATTRIBUTE:
                        case CONCAT:
                            renameToscaFunctionComponentInstance((ToscaFunction) parameter, oldInstanceName, newInstanceName);
                            break;
                        default:
                    }
                }
                break;
            }
            default:
        }
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

    public static boolean isPropertyConstraintChangedByCi(PropertyFilterDataDefinition propertyFilterDataDefinition,
                                                           String name) {
        List<PropertyFilterConstraintDataDefinition> constraints = propertyFilterDataDefinition.getConstraints();
        if (CollectionUtils.isEmpty(constraints)) {
            return false;
        }
        return constraints.stream().anyMatch(constraint -> isConstraintChangedByCi(constraint, name));
    }

    private static boolean isConstraintChangedByCi(final PropertyFilterConstraintDataDefinition constraint, final String name) {
        if (constraint.getValueType() == FilterValueType.GET_PROPERTY || constraint.getValueType() == FilterValueType.GET_ATTRIBUTE) {
            final ToscaFunction toscaFunction = new FilterConstraintMapper().parseValueToToscaFunction(constraint.getValue()).orElse(null);
            if (toscaFunction != null) {
                final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) toscaFunction;
                return toscaGetFunction.getSourceName().equals(name);
            }
        }
        return false;
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

    private static boolean isPropertyConstraintChangedByInput(final PropertyFilterDataDefinition propertyFilterDataDefinition,
                                                              final InputDefinition changedInput) {
        final List<PropertyFilterConstraintDataDefinition> constraints = propertyFilterDataDefinition.getConstraints();
        return constraints.stream().anyMatch(constraint -> isConstraintChangedByInput(constraint, changedInput));
    }

    private static boolean isConstraintChangedByInput(final PropertyFilterConstraintDataDefinition constraint, final InputDefinition changedInput) {
        if (constraint.getValueType() == FilterValueType.GET_INPUT) {
            final ToscaFunction toscaFunction = new FilterConstraintMapper().parseValueToToscaFunction(constraint.getValue()).orElse(null);
            if (toscaFunction != null) {
                final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) toscaFunction;
                return toscaGetFunction.getPropertyPathFromSource().contains(changedInput.getName());
            }
        }
        return false;
    }
}
