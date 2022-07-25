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

package org.openecomp.sdc.be.components.impl.validation;

import fj.data.Either;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.be.components.impl.exceptions.ToscaFunctionExceptionSupplier;
import org.openecomp.sdc.be.components.impl.exceptions.ToscaGetFunctionExceptionSupplier;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.ToscaPropertyData;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.validation.ToscaFunctionValidator;

@org.springframework.stereotype.Component
public class ToscaFunctionValidatorImpl implements ToscaFunctionValidator {

    private final ApplicationDataTypeCache applicationDataTypeCache;

    public ToscaFunctionValidatorImpl(final ApplicationDataTypeCache applicationDataTypeCache) {
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    @Override
    public <T extends PropertyDataDefinition> void validate(T property, final Component containerComponent) {
        if (property.getToscaFunction().getType() == null) {
            throw ToscaFunctionExceptionSupplier.missingFunctionType().get();
        }
        if (property.isToscaGetFunction()) {
            validateToscaGetFunction(property, containerComponent);
        }
    }

    private <T extends PropertyDataDefinition> void validateToscaGetFunction(T property, Component parentComponent) {
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) property.getToscaFunction();
        validateGetToscaFunctionAttributes(toscaGetFunction);
        validateGetPropertySource(toscaGetFunction.getFunctionType(), toscaGetFunction.getPropertySource());
        if (toscaGetFunction.getFunctionType() == ToscaGetFunctionType.GET_INPUT) {
            validateGetFunction(property, parentComponent.getInputs(), parentComponent.getModel());
            return;
        }
        if (toscaGetFunction.getFunctionType() == ToscaGetFunctionType.GET_PROPERTY) {
            if (toscaGetFunction.getPropertySource() == PropertySource.SELF) {
                validateGetFunction(property, parentComponent.getProperties(), parentComponent.getModel());
            } else if (toscaGetFunction.getPropertySource() == PropertySource.INSTANCE) {
                final ComponentInstance componentInstance =
                    parentComponent.getComponentInstanceById(toscaGetFunction.getSourceUniqueId())
                        .orElseThrow(ToscaGetFunctionExceptionSupplier.instanceNotFound(toscaGetFunction.getSourceName()));
                validateGetFunction(property, componentInstance.getProperties(), parentComponent.getModel());
            }

            return;
        }
        if (toscaGetFunction.getFunctionType() == ToscaGetFunctionType.GET_ATTRIBUTE) {
            if (toscaGetFunction.getPropertySource() == PropertySource.SELF) {
                validateGetFunction(property, parentComponent.getAttributes(), parentComponent.getModel());
            } else if (toscaGetFunction.getPropertySource() == PropertySource.INSTANCE) {
                final ComponentInstance componentInstance =
                    parentComponent.getComponentInstanceById(toscaGetFunction.getSourceUniqueId())
                        .orElseThrow(ToscaGetFunctionExceptionSupplier.instanceNotFound(toscaGetFunction.getSourceName()));
                validateGetFunction(property, componentInstance.getAttributes(), parentComponent.getModel());
            }

            return;
        }

        throw ToscaGetFunctionExceptionSupplier.functionNotSupported(toscaGetFunction.getFunctionType()).get();
    }

    private <T extends PropertyDataDefinition> void validateGetFunction(final T property,
                                                                    final List<? extends ToscaPropertyData> parentProperties,
                                                                    final String model) {
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) property.getToscaFunction();
        if (CollectionUtils.isEmpty(parentProperties)) {
            throw ToscaGetFunctionExceptionSupplier
                .propertyNotFoundOnTarget(toscaGetFunction.getPropertyName(), toscaGetFunction.getPropertySource(),
                    toscaGetFunction.getFunctionType()
                ).get();
        }
        final String getFunctionPropertyUniqueId = toscaGetFunction.getPropertyUniqueId();
        ToscaPropertyData referredProperty = parentProperties.stream()
            .filter(property1 -> getFunctionPropertyUniqueId.equals(property1.getUniqueId()))
            .findFirst()
            .orElseThrow(ToscaGetFunctionExceptionSupplier
                .propertyNotFoundOnTarget(toscaGetFunction.getPropertyName(), toscaGetFunction.getPropertySource()
                    , toscaGetFunction.getFunctionType())
            );
        if (toscaGetFunction.isSubProperty()) {
            referredProperty = findSubProperty(referredProperty, toscaGetFunction, model);
        }

        if (!property.getType().equals(referredProperty.getType())) {
            throw ToscaGetFunctionExceptionSupplier
                .propertyTypeDiverge(toscaGetFunction.getType(), referredProperty.getType(), property.getType()).get();
        }
        if (PropertyType.typeHasSchema(referredProperty.getType()) && !referredProperty.getSchemaType().equals(property.getSchemaType())) {
            throw ToscaGetFunctionExceptionSupplier
                .propertySchemaDiverge(toscaGetFunction.getType(), referredProperty.getSchemaType(), property.getSchemaType()).get();
        }
    }

    private void validateGetToscaFunctionAttributes(final ToscaGetFunctionDataDefinition toscaGetFunction) {
        if (toscaGetFunction.getFunctionType() == null) {
            throw ToscaGetFunctionExceptionSupplier.targetFunctionTypeNotFound().get();
        }
        if (toscaGetFunction.getPropertySource() == null) {
            throw ToscaGetFunctionExceptionSupplier.targetPropertySourceNotFound(toscaGetFunction.getFunctionType()).get();
        }
        if (CollectionUtils.isEmpty(toscaGetFunction.getPropertyPathFromSource())) {
            throw ToscaGetFunctionExceptionSupplier
                .targetSourcePathNotFound(toscaGetFunction.getFunctionType()).get();
        }
        if (StringUtils.isEmpty(toscaGetFunction.getSourceName()) || StringUtils.isBlank(toscaGetFunction.getSourceName())) {
            throw ToscaGetFunctionExceptionSupplier.sourceNameNotFound(toscaGetFunction.getPropertySource()).get();
        }
        if (StringUtils.isEmpty(toscaGetFunction.getSourceUniqueId()) || StringUtils.isBlank(toscaGetFunction.getSourceUniqueId())) {
            throw ToscaGetFunctionExceptionSupplier.sourceIdNotFound(toscaGetFunction.getPropertySource()).get();
        }
        if (StringUtils.isEmpty(toscaGetFunction.getPropertyName()) || StringUtils.isBlank(toscaGetFunction.getPropertyName())) {
            throw ToscaGetFunctionExceptionSupplier.propertyNameNotFound(toscaGetFunction.getPropertySource()).get();
        }
        if (StringUtils.isEmpty(toscaGetFunction.getPropertyUniqueId()) || StringUtils.isBlank(toscaGetFunction.getPropertyUniqueId())) {
            throw ToscaGetFunctionExceptionSupplier.propertyIdNotFound(toscaGetFunction.getPropertySource()).get();
        }
    }

    private void validateGetPropertySource(final ToscaGetFunctionType functionType, final PropertySource propertySource) {
        if (functionType == ToscaGetFunctionType.GET_INPUT && propertySource != PropertySource.SELF) {
            throw ToscaGetFunctionExceptionSupplier
                .targetSourceNotSupported(functionType, propertySource).get();
        }
        if (functionType == ToscaGetFunctionType.GET_PROPERTY && !List.of(PropertySource.SELF, PropertySource.INSTANCE).contains(propertySource)) {
            throw ToscaGetFunctionExceptionSupplier
                .targetSourceNotSupported(functionType, propertySource).get();
        }
    }

    private ToscaPropertyData findSubProperty(final ToscaPropertyData referredProperty,
                                              final ToscaGetFunctionDataDefinition toscaGetFunction,
                                              final String model) {
        final Map<String, DataTypeDefinition> dataTypeMap = loadDataTypes(model);
        final List<String> propertyPathFromSource = toscaGetFunction.getPropertyPathFromSource();
        DataTypeDefinition dataType = dataTypeMap.get(referredProperty.getType());
        if (dataType == null) {
            throw ToscaGetFunctionExceptionSupplier
                .propertyDataTypeNotFound(propertyPathFromSource.get(0), referredProperty.getType(), toscaGetFunction.getFunctionType()).get();
        }
        ToscaPropertyData foundProperty = referredProperty;
        for (int i = 1; i < propertyPathFromSource.size(); i++) {
            final String currentPropertyName = propertyPathFromSource.get(i);
            foundProperty = dataType.getProperties().stream()
                .filter(propertyDefinition -> currentPropertyName.equals(propertyDefinition.getName())).findFirst()
                .orElseThrow(
                    ToscaGetFunctionExceptionSupplier
                        .propertyNotFoundOnTarget(propertyPathFromSource.subList(0, i), toscaGetFunction.getPropertySource(),
                            toscaGetFunction.getFunctionType())
                );
            dataType = dataTypeMap.get(foundProperty.getType());
            if (dataType == null) {
                throw ToscaGetFunctionExceptionSupplier
                    .propertyDataTypeNotFound(propertyPathFromSource.subList(0, i), foundProperty.getType(),
                        toscaGetFunction.getFunctionType()).get();
            }
        }
        return foundProperty;
    }

    private Map<String, DataTypeDefinition> loadDataTypes(String model) {
        final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypeEither =
            applicationDataTypeCache.getAll(model);
        if (dataTypeEither.isRight()) {
            throw ToscaGetFunctionExceptionSupplier.couldNotLoadDataTypes(model).get();
        }
        return dataTypeEither.left().value();
    }

}
