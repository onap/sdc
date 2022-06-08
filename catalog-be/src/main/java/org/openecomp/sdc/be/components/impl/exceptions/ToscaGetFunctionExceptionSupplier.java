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

package org.openecomp.sdc.be.components.impl.exceptions;

import java.util.List;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ToscaGetFunctionExceptionSupplier {

    public static Supplier<ByActionStatusComponentException> targetSourceNotSupported(final ToscaGetFunctionType toscaGetFunctionType,
                                                                                      final PropertySource propertySource) {
        final String errorMsg = String.format("%s on %s", toscaGetFunctionType.getFunctionName(), propertySource.getName());
        return () -> new ByActionStatusComponentException(ActionStatus.NOT_SUPPORTED, errorMsg);
    }
    public static Supplier<ByActionStatusComponentException> propertyNotFoundOnTarget(final String propertyName,
                                                                                      final PropertySource propertySource,
                                                                                      final ToscaGetFunctionType functionType) {
        return propertyNotFoundOnTarget(List.of(propertyName), propertySource, functionType);
    }

    public static Supplier<ByActionStatusComponentException> targetFunctionTypeNotFound() {
        return () -> new ByActionStatusComponentException(ActionStatus.TOSCA_FUNCTION_NOT_FOUND, "Tosca function type");
    }

    public static Supplier<ByActionStatusComponentException> targetPropertySourceNotFound(final ToscaGetFunctionType toscaGetFunctionType) {
        return () -> new ByActionStatusComponentException(ActionStatus.TOSCA_FUNCTION_NOT_FOUND, "Tosca property source for " + toscaGetFunctionType);
    }

    public static Supplier<ByActionStatusComponentException> targetSourcePathNotFound(final ToscaGetFunctionType toscaGetFunctionType,
                                                                                       final PropertySource propertySource) {
        final String errorMsg = String.format("%s on %s", toscaGetFunctionType.getFunctionName(), propertySource.getName());
        return () -> new ByActionStatusComponentException(ActionStatus.PROPERTY_VALUE_NOT_FOUND, errorMsg);
    }

    public static Supplier<ByActionStatusComponentException> propertyNameNotFound(final PropertySource propertySource) {
        return () -> new ByActionStatusComponentException(ActionStatus.TOSCA_FUNCTION_NOT_FOUND, "Tosca function property name of source "
            + propertySource);
    }

    public static Supplier<ByActionStatusComponentException> propertyIdNotFound(final PropertySource propertySource) {
        return () -> new ByActionStatusComponentException(ActionStatus.TOSCA_FUNCTION_NOT_FOUND, "Tosca function property id of source "
            + propertySource);
    }

    public static Supplier<ByActionStatusComponentException> sourceNameNotFound(final PropertySource propertySource) {
        return () -> new ByActionStatusComponentException(ActionStatus.TOSCA_FUNCTION_NOT_FOUND, "Tosca function source name of "
            + propertySource);
    }

    public static Supplier<ByActionStatusComponentException> sourceIdNotFound(final PropertySource propertySource) {
        return () -> new ByActionStatusComponentException(ActionStatus.TOSCA_FUNCTION_NOT_FOUND, "Tosca function source id of "
            + propertySource);
    }

    public static Supplier<ByActionStatusComponentException> propertyNotFoundOnTarget(final List<String> propertyPathFromSource,
                                                                                      final PropertySource propertySource,
                                                                                      final ToscaGetFunctionType functionType) {
        return () -> new ByActionStatusComponentException(ActionStatus.TOSCA_GET_FUNCTION_PROPERTY_NOT_FOUND, functionType.getPropertyType(),
            String.join("->", propertyPathFromSource), propertySource.getName());
    }

    public static Supplier<ByActionStatusComponentException> propertyDataTypeNotFound(final String propertyName,
                                                                                      final String dataType,
                                                                                      final ToscaGetFunctionType functionType) {
        return propertyDataTypeNotFound(List.of(propertyName), dataType, functionType);
    }

    public static Supplier<ByActionStatusComponentException> propertyDataTypeNotFound(final List<String> propertyPathFromSource,
                                                                                      final String dataType,
                                                                                      final ToscaGetFunctionType functionType) {
        return () -> new ByActionStatusComponentException(
            ActionStatus.TOSCA_GET_FUNCTION_PROPERTY_DATA_TYPE_NOT_FOUND,
            functionType.getPropertyType(), String.join("->", propertyPathFromSource), dataType
        );
    }

    public static Supplier<ByActionStatusComponentException> couldNotLoadDataTypes(final String model) {
        return () -> new ByActionStatusComponentException(ActionStatus.DATA_TYPES_NOT_LOADED, model);
    }

    public static Supplier<ByActionStatusComponentException> functionNotSupported(final ToscaGetFunctionType functionType) {
        return () -> new ByActionStatusComponentException(ActionStatus.NOT_SUPPORTED, "Tosca function " + functionType.getFunctionName());
    }

    public static Supplier<ByActionStatusComponentException> propertyTypeDiverge(final ToscaGetFunctionType functionType,
                                                                                 final String referredPropertyType,
                                                                                 final String propertyType) {
        return () -> new ByActionStatusComponentException(
            ActionStatus.TOSCA_GET_FUNCTION_TYPE_DIVERGE,
            functionType.getFunctionName(), referredPropertyType, propertyType
        );
    }

    public static Supplier<ByActionStatusComponentException> propertySchemaDiverge(final ToscaGetFunctionType functionType,
                                                                                   final String referredPropertySchemaType,
                                                                                   final String propertySchemaType) {
        return () -> new ByActionStatusComponentException(
            ActionStatus.TOSCA_GET_FUNCTION_SCHEMA_DIVERGE,
            functionType.getFunctionName(), referredPropertySchemaType, propertySchemaType
        );
    }

    public static Supplier<ByActionStatusComponentException> instanceNotFound(final String instanceName) {
        return () -> new ByActionStatusComponentException(
            ActionStatus.TOSCA_GET_FUNCTION_INSTANCE_NOT_FOUND,
            instanceName
        );
    }

}
