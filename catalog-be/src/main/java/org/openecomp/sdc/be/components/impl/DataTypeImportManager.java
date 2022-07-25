/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.normatives.ElementTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component("dataTypeImportManager")
public class DataTypeImportManager {

    private static final Logger log = Logger.getLogger(DataTypeImportManager.class.getName());
    @Resource
    private PropertyOperation propertyOperation;
    @Resource
    private ComponentsUtils componentsUtils;
    @Resource
    private CommonImportManager commonImportManager;
    @Resource
    private ModelOperation modelOperation;

    public Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat> createDataTypes(final String dataTypeYml, final String modelName,
                                                                                                    final boolean includeToModelDefaultImports) {
        final var elementTypes = commonImportManager.createElementTypes(
            dataTypeYml, dataTypesFromYml -> createDataTypesFromYml(dataTypeYml, modelName), this::createDataTypesByDao, ElementTypeEnum.DATA_TYPE);

        if (includeToModelDefaultImports && StringUtils.isNotEmpty(modelName)) {
            commonImportManager.addTypesToDefaultImports(ElementTypeEnum.DATA_TYPE, dataTypeYml, modelName);
        }
        return elementTypes;
    }

    private Either<List<DataTypeDefinition>, ActionStatus> createDataTypesFromYml(final String dataTypesYml, final String modelName) {
        final Either<List<DataTypeDefinition>, ActionStatus> dataTypesEither = commonImportManager.createElementTypesFromYml(dataTypesYml,
            this::createDataType);
        if (dataTypesEither.isRight()) {
            return dataTypesEither;
        }
        final List<DataTypeDefinition> dataTypes = dataTypesEither.left().value();
        if (StringUtils.isNotEmpty(modelName)) {
            final Optional<Model> modelOptional = modelOperation.findModelByName(modelName);
            if (modelOptional.isPresent()) {
                dataTypes.forEach(dataType -> dataType.setModel(modelName));
            } else {
                return Either.right(ActionStatus.INVALID_MODEL);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Unsorted datatypes order:");
            dataTypes.stream().forEach(dt -> log.trace(dt.getName()));
        }

        long startTime = System.currentTimeMillis();
        List<DataTypeDefinition> sortedDataTypeDefinitions = sortDataTypesByDependencyOrder(dataTypes);

        if (log.isTraceEnabled()) {
            long sortTime = System.currentTimeMillis() - startTime;
            log.trace("Sorting " + sortedDataTypeDefinitions.size() + " data types from model: " + modelName + " took: " + sortTime);
            log.trace("Sorted datatypes order:");
            sortedDataTypeDefinitions.stream().forEach(dt -> log.trace(dt.getName()));
        }
        return Either.left(sortedDataTypeDefinitions);
    }
    
    private List<DataTypeDefinition> sortDataTypesByDependencyOrder(final List<DataTypeDefinition> dataTypes) {
        final List<DataTypeDefinition> sortedDataTypeDefinitions = new ArrayList<>();
        final Map<String, DataTypeDefinition> dataTypeDefinitionsMap = new HashMap<>();
        
        dataTypes.forEach(dataType -> {
            
            int highestDependencyIndex = -1;
            for (final String dependencyName : getDependencyTypes(dataType)) {
                final DataTypeDefinition dependency = dataTypeDefinitionsMap.get(dependencyName);
                final int indexOfDependency = sortedDataTypeDefinitions.lastIndexOf(dependency);
                highestDependencyIndex = indexOfDependency > highestDependencyIndex ? indexOfDependency : highestDependencyIndex;
            }
            sortedDataTypeDefinitions.add(highestDependencyIndex + 1, dataType);
            dataTypeDefinitionsMap.put(dataType.getName(), dataType);
       
            } );
        
        return sortedDataTypeDefinitions;
    }
    
    private Collection<String> getDependencyTypes(final DataTypeDefinition dataType) {
        final Set<String> dependencies = new HashSet<>();
        if (dataType.getDerivedFromName() != null) {
            dependencies.add(dataType.getDerivedFromName());
        }
        if (dataType.getProperties() != null) {
            dataType.getProperties().stream().forEach(property -> dependencies.add(property.getType()));
        }
        return dependencies;
    }

    private Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat> createDataTypesByDao(
        List<DataTypeDefinition> dataTypesToCreate) {
        return commonImportManager.createElementTypesByDao(dataTypesToCreate, this::validateDataType,
            dataType -> new ImmutablePair<>(ElementTypeEnum.DATA_TYPE, UniqueIdBuilder.buildDataTypeUid(dataType.getModel(), dataType.getName())),
            dataTypeUid -> propertyOperation.getDataTypeByUidWithoutDerived(dataTypeUid, true),
            dataType -> propertyOperation.addDataType(dataType),
            (newDataType, oldDataType) -> propertyOperation.updateDataType(newDataType, oldDataType));
    }

    private Either<ActionStatus, ResponseFormat> validateDataType(DataTypeDefinition dataType) {
        String dataTypeName = dataType.getName();
        List<PropertyDefinition> properties = dataType.getProperties();
        if (properties == null) {
            // At least one parameter should be defined either in the properties

            // section or at one of the parents
            String derivedDataType = dataType.getDerivedFromName();
            // If there are no properties, then we can create a data type if it

            // is an abstract one or it derives from non abstract data type
            if (derivedDataType == null || derivedDataType.isEmpty()) {
                if (!isAbstract(dataType.getName()) && !ToscaPropertyType.isScalarType(dataTypeName)) {
                    log.debug("Data type {} must have properties unless it derives from non abstract data type", dataType.getName());
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormatByDataType(ActionStatus.DATA_TYPE_NOR_PROPERTIES_NEITHER_DERIVED_FROM, dataType, null);
                    return Either.right(responseFormat);
                }
            } else {
                if (!ToscaPropertyType.isScalarType(dataTypeName) && isAbstract(derivedDataType)) {
                    log.warn("Creating data type {} which derived from abstract data type with no properties", dataType.getName());
                }
            }
        } else {
            // properties tag cannot be empty
            if (properties.isEmpty()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROPERTIES_CANNOT_BE_EMPTY, dataType, null);
                return Either.right(responseFormat);
            }
            // check no duplicates
            Set<String> collect = properties.stream().map(PropertyDataDefinition::getName).collect(Collectors.toSet());
            if (collect != null && properties.size() != collect.size()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByDataType(ActionStatus.DATA_TYPE_DUPLICATE_PROPERTY, dataType, null);
                return Either.right(responseFormat);
            }
            List<String> propertiesWithSameTypeAsDataType = properties.stream().filter(p -> p.getType().equals(dataType.getName()))
                .map(PropertyDataDefinition::getName).collect(Collectors.toList());
            if (propertiesWithSameTypeAsDataType != null && !propertiesWithSameTypeAsDataType.isEmpty()) {
                log.debug("The data type {} contains properties with the type {}", dataType.getName(), dataType.getName());
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROEPRTY_CANNOT_HAVE_SAME_TYPE_OF_DATA_TYPE, dataType,
                        propertiesWithSameTypeAsDataType);
                return Either.right(responseFormat);
            }
        }
        String derivedDataType = dataType.getDerivedFromName();
        if (derivedDataType != null) {
            Either<DataTypeDefinition, StorageOperationStatus> derivedDataTypeByName = propertyOperation.getDataTypeByName(derivedDataType,
                dataType.getModel());
            if (derivedDataTypeByName.isRight()) {
                StorageOperationStatus status = derivedDataTypeByName.right().value();
                if (status == StorageOperationStatus.NOT_FOUND) {
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormatByDataType(ActionStatus.DATA_TYPE_DERIVED_IS_MISSING, dataType, null);
                    return Either.right(responseFormat);
                } else {
                    ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.GENERAL_ERROR, dataType, null);
                    return Either.right(responseFormat);
                }
            } else {
                DataTypeDefinition derivedDataTypeDef = derivedDataTypeByName.left().value();
                if (properties != null && !properties.isEmpty() && derivedDataTypeDef != null) {
                    if (isScalarType(derivedDataTypeDef)) {
                        ResponseFormat responseFormat = componentsUtils
                            .getResponseFormatByDataType(ActionStatus.DATA_TYPE_CANNOT_HAVE_PROPERTIES, dataType, null);
                        return Either.right(responseFormat);
                    }
                    Set<String> allParentsProps = new HashSet<>();
                    do {
                        List<PropertyDefinition> currentParentsProps = derivedDataTypeDef.getProperties();
                        if (currentParentsProps != null) {
                            for (PropertyDefinition propertyDefinition : currentParentsProps) {
                                allParentsProps.add(propertyDefinition.getName());
                            }
                        }
                        derivedDataTypeDef = derivedDataTypeDef.getDerivedFrom();
                    } while (derivedDataTypeDef != null);
                    // Check that no property is already defined in one of the

                    // ancestors
                    Set<String> alreadyExistPropsCollection = properties.stream().filter(p -> allParentsProps.contains(p.getName()))
                        .map(PropertyDataDefinition::getName).collect(Collectors.toSet());
                    if (alreadyExistPropsCollection != null && !alreadyExistPropsCollection.isEmpty()) {
                        List<String> duplicateProps = new ArrayList<>();
                        duplicateProps.addAll(alreadyExistPropsCollection);
                        ResponseFormat responseFormat = componentsUtils
                            .getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROPERTY_ALREADY_DEFINED_IN_ANCESTOR, dataType, duplicateProps);
                        return Either.right(responseFormat);
                    }
                }
            }
        }
        return Either.left(ActionStatus.OK);
    }

    private boolean isAbstract(String dataTypeName) {
        ToscaPropertyType isPrimitiveToscaType = ToscaPropertyType.isValidType(dataTypeName);
        return isPrimitiveToscaType != null && isPrimitiveToscaType.isAbstract();
    }

    private boolean isScalarType(DataTypeDefinition dataTypeDef) {
        boolean isScalar = false;
        DataTypeDefinition dataType = dataTypeDef;
        while (dataType != null) {
            String name = dataType.getName();
            if (ToscaPropertyType.isScalarType(name)) {
                isScalar = true;
                break;
            }
            dataType = dataType.getDerivedFrom();
        }
        return isScalar;
    }

    private DataTypeDefinition createDataType(String dataTypeName, Map<String, Object> toscaJson) {
        DataTypeDefinition dataType = new DataTypeDefinition();
        dataType.setName(dataTypeName);
        if (toscaJson != null) {
            // Description
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DESCRIPTION.getElementName(), dataType::setDescription);
            // Derived From
            commonImportManager.setField(toscaJson, TypeUtils.ToscaTagNamesEnum.DERIVED_FROM.getElementName(), dataType::setDerivedFromName);
            // Properties
            CommonImportManager.setProperties(toscaJson, dataType::setProperties);
        }
        return dataType;
    }
}
