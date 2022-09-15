/*
 * ============LICENSE_START=======================================================
 *  SDC
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model.operations.impl;

import com.google.gson.JsonElement;
import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("attribute-operation")
public class AttributeOperation extends AbstractOperation {

    private static final String FAILED_TO_FETCH_ATTRIBUTES_OF_DATA_TYPE = "Failed to fetch attributes of data type {}";
    private static final String DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS = "Data type {} cannot be found in graph. status is {}";
    private static final String THE_VALUE_OF_ATTRIBUTE_FROM_TYPE_IS_INVALID = "The value {} of attribute from type {} is invalid";
    private static final Logger log = Logger.getLogger(AttributeOperation.class.getName());

    @Autowired
    public AttributeOperation(HealingJanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    public boolean isAttributeTypeValid(final AttributeDataDefinition attributeDefinition) {
        if (attributeDefinition == null) {
            return false;
        }
        if (ToscaPropertyType.isValidType(attributeDefinition.getType()) == null) {
            final Either<Boolean, JanusGraphOperationStatus> definedInDataTypes = isDefinedInDataTypes(attributeDefinition.getType());
            if (definedInDataTypes.isRight()) {
                return false;
            } else {
                Boolean isExist = definedInDataTypes.left().value();
                return isExist.booleanValue();
            }
        }
        return true;
    }

    private Either<Boolean, JanusGraphOperationStatus> isDefinedInDataTypes(final String propertyType) {
        final String dataTypeUid = UniqueIdBuilder.buildDataTypeUid(null, propertyType);
        final Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeByUid = getDataTypeByUid(dataTypeUid);
        if (dataTypeByUid.isRight()) {
            final JanusGraphOperationStatus status = dataTypeByUid.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                return Either.left(false);
            }
            return Either.right(status);
        }
        return Either.left(true);
    }

    /**
     * Build Data type object from graph by unique id
     */
    private Either<DataTypeDefinition, JanusGraphOperationStatus> getDataTypeByUid(final String uniqueId) {
        final Either<DataTypeData, JanusGraphOperationStatus> dataTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);
        if (dataTypesRes.isRight()) {
            JanusGraphOperationStatus status = dataTypesRes.right().value();
            log.debug(DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS, uniqueId, status);
            return Either.right(status);
        }
        final DataTypeData ctData = dataTypesRes.left().value();
        final DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(ctData.getDataTypeDataDefinition());
        final JanusGraphOperationStatus propertiesStatus = fillProperties(uniqueId, dataTypeDefinition);
        if (propertiesStatus != JanusGraphOperationStatus.OK) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, FAILED_TO_FETCH_ATTRIBUTES_OF_DATA_TYPE, uniqueId);
            return Either.right(propertiesStatus);
        }
        final Either<ImmutablePair<DataTypeData, GraphEdge>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.DataType,
                DataTypeData.class);
        log.debug("After retrieving DERIVED_FROM node of {}. status is {}", uniqueId, parentNode);
        if (parentNode.isRight()) {
            final JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
            if (janusGraphOperationStatus != JanusGraphOperationStatus.NOT_FOUND) {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, "Failed to find the parent data type of data type {}. status is {}", uniqueId,
                    janusGraphOperationStatus);
                return Either.right(janusGraphOperationStatus);
            }
        } else {
            // derived from node was found
            final ImmutablePair<DataTypeData, GraphEdge> immutablePair = parentNode.left().value();
            final DataTypeData parentCT = immutablePair.getKey();
            final String parentUniqueId = parentCT.getUniqueId();
            final Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeByUid = getDataTypeByUid(parentUniqueId);
            if (dataTypeByUid.isRight()) {
                return Either.right(dataTypeByUid.right().value());
            }
            final DataTypeDefinition parentDataTypeDefinition = dataTypeByUid.left().value();
            dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);
        }
        return Either.left(dataTypeDefinition);
    }

    private JanusGraphOperationStatus fillProperties(final String uniqueId, final DataTypeDefinition dataTypeDefinition) {
        final Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesOfNode = findPropertiesOfNode(NodeTypeEnum.DataType,
            uniqueId);
        if (findPropertiesOfNode.isRight()) {
            final JanusGraphOperationStatus janusGraphOperationStatus = findPropertiesOfNode.right().value();
            log.debug("After looking for properties of vertex {}. status is {}", uniqueId, janusGraphOperationStatus);
            if (JanusGraphOperationStatus.NOT_FOUND.equals(janusGraphOperationStatus)) {
                return JanusGraphOperationStatus.OK;
            } else {
                return janusGraphOperationStatus;
            }
        } else {
            final Map<String, PropertyDefinition> properties = findPropertiesOfNode.left().value();
            if (properties != null && !properties.isEmpty()) {
                List<PropertyDefinition> listOfProps = new ArrayList<>();
                for (final Entry<String, PropertyDefinition> entry : properties.entrySet()) {
                    final String propName = entry.getKey();
                    final PropertyDefinition propertyDefinition = entry.getValue();
                    final PropertyDefinition newPropertyDefinition = new PropertyDefinition(propertyDefinition);
                    newPropertyDefinition.setName(propName);
                    listOfProps.add(newPropertyDefinition);
                }
                dataTypeDefinition.setProperties(listOfProps);
            }
            return JanusGraphOperationStatus.OK;
        }
    }

    private Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesOfNode(final NodeTypeEnum nodeType,
                                                                                                    final String uniqueId) {
        final Map<String, PropertyDefinition> resourceProps = new HashMap<>();
        final Either<List<ImmutablePair<PropertyData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.PROPERTY, NodeTypeEnum.Property,
                PropertyData.class);
        if (childrenNodes.isRight()) {
            final JanusGraphOperationStatus operationStatus = childrenNodes.right().value();
            return Either.right(operationStatus);
        }
        final List<ImmutablePair<PropertyData, GraphEdge>> values = childrenNodes.left().value();
        if (values != null) {
            for (final ImmutablePair<PropertyData, GraphEdge> immutablePair : values) {
                final GraphEdge edge = immutablePair.getValue();
                final String propertyName = (String) edge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());
                log.debug("Attribute {} is associated to node {}", propertyName, uniqueId);
                final PropertyData propertyData = immutablePair.getKey();
                final PropertyDefinition propertyDefinition = this.convertPropertyDataToPropertyDefinition(propertyData, propertyName);
                resourceProps.put(propertyName, propertyDefinition);
            }
        }
        log.debug("The properties associated to node {} are {}", uniqueId, resourceProps);
        return Either.left(resourceProps);
    }

    private PropertyDefinition convertPropertyDataToPropertyDefinition(final PropertyData propertyDataResult, final String propertyName) {
        log.debug("The object returned after create property is {}", propertyDataResult);
        final PropertyDefinition propertyDefResult = new PropertyDefinition(propertyDataResult.getPropertyDataDefinition());
        propertyDefResult.setConstraints(convertConstraints(propertyDataResult.getConstraints()));
        propertyDefResult.setName(propertyName);
        return propertyDefResult;
    }

    public ImmutablePair<String, Boolean> isAttributeInnerTypeValid(final AttributeDataDefinition attributeDefinition,
                                                                    final Map<String, DataTypeDefinition> dataTypes) {
        if (attributeDefinition == null) {
            return new ImmutablePair<>(null, false);
        }
        SchemaDefinition schema;
        PropertyDataDefinition innerProp;
        String innerType = null;
        if ((schema = attributeDefinition.getSchema()) != null && ((innerProp = schema.getProperty()) != null)) {
            innerType = innerProp.getType();
        }
        final ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);
        if (innerToscaType == null) {
            final DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
            if (dataTypeDefinition == null) {
                log.debug("The inner type {} is not a data type.", innerType);
                return new ImmutablePair<>(innerType, false);
            } else {
                log.debug("The inner type {} is a data type. Data type definition is {}", innerType, dataTypeDefinition);
            }
        }
        return new ImmutablePair<>(innerType, true);
    }

    public boolean isAttributeDefaultValueValid(final AttributeDataDefinition attributeDefinition, final Map<String, DataTypeDefinition> dataTypes) {
        if (attributeDefinition == null) {
            return false;
        }
        boolean isValid;
        String innerType = null;
        final String propertyType = attributeDefinition.getType();
        final ToscaPropertyType type = getType(propertyType);
        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            final SchemaDefinition def = attributeDefinition.getSchema();
            if (def == null) {
                return false;
            }
            final PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                return false;
            }
            innerType = propDef.getType();
        }
        final String value = (String) attributeDefinition.get_default();
        if (type != null) {
            isValid = isValidValue(type, value, innerType, dataTypes);
        } else {
            log.trace("The given type {} is not a pre defined one.", propertyType);
            final DataTypeDefinition foundDt = dataTypes.get(propertyType);
            if (foundDt != null) {
                isValid = isValidComplexValue(foundDt, value, dataTypes);
            } else {
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean isValidComplexValue(final DataTypeDefinition foundDt, final String value, final Map<String, DataTypeDefinition> dataTypes) {
        final ImmutablePair<JsonElement, Boolean> validateAndUpdate = dataTypeValidatorConverter.validateAndUpdate(value, foundDt, dataTypes);
        log.trace("The result after validating complex value of type {} is {}", foundDt.getName(), validateAndUpdate);
        return validateAndUpdate.right.booleanValue();
    }

    public StorageOperationStatus validateAndUpdateAttribute(final AttributeDataDefinition attributeDefinition,
                                                             final Map<String, DataTypeDefinition> dataTypes) {
        log.trace("Going to validate attribute type and value. {}", attributeDefinition);
        final String attributeDefinitionType = attributeDefinition.getType();
        final String value = (String) attributeDefinition.get_default();
        final ToscaPropertyType type = getType(attributeDefinitionType);
        if (type == null) {
            final DataTypeDefinition dataTypeDefinition = dataTypes.get(attributeDefinitionType);
            if (dataTypeDefinition == null) {
                log.debug("The type {} of attribute cannot be found.", attributeDefinitionType);
                return StorageOperationStatus.INVALID_TYPE;
            }
            return validateAndUpdateAttributeComplexValue(attributeDefinition, attributeDefinitionType, value, dataTypeDefinition, dataTypes);
        }
        String innerType;
        final Either<String, JanusGraphOperationStatus> checkInnerType = getInnerType(type, attributeDefinition::getSchema);
        if (checkInnerType.isRight()) {
            return StorageOperationStatus.INVALID_TYPE;
        }
        innerType = checkInnerType.left().value();
        log.trace("After validating property type {}", attributeDefinitionType);
        if (!isValidValue(type, value, innerType, dataTypes)) {
            log.info(THE_VALUE_OF_ATTRIBUTE_FROM_TYPE_IS_INVALID, value, type);
            return StorageOperationStatus.INVALID_VALUE;
        }
        final PropertyValueConverter converter = type.getConverter();
        if (isEmptyValue(value)) {
            log.debug("Default value was not sent for attribute {}. Set default value to {}", attributeDefinition.getName(), EMPTY_VALUE);
            attributeDefinition.set_default(EMPTY_VALUE);
        } else if (!isEmptyValue(value)) {
            attributeDefinition.set_default(converter.convert(value, innerType, dataTypes));
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus validateAndUpdateAttributeComplexValue(final AttributeDataDefinition attributeDefinition,
                                                                          final String attributeType, final String value,
                                                                          final DataTypeDefinition dataTypeDefinition,
                                                                          final Map<String, DataTypeDefinition> dataTypes) {
        final ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);
        if (!validateResult.right.booleanValue()) {
            log.debug(THE_VALUE_OF_ATTRIBUTE_FROM_TYPE_IS_INVALID, attributeType, attributeType);
            return StorageOperationStatus.INVALID_VALUE;
        }
        final JsonElement jsonElement = validateResult.left;
        log.trace("Going to update value in attribute definition {} {}", attributeDefinition.getName(),
            (jsonElement != null ? jsonElement.toString() : null));
        updateAttributeValue(attributeDefinition, jsonElement);
        return StorageOperationStatus.OK;
    }

    private void updateAttributeValue(final AttributeDataDefinition attributeDefinition, final JsonElement jsonElement) {
        attributeDefinition.set_default(jsonElement);
    }

    public Either<Object, Boolean> validateAndUpdateAttributeValue(final AttributeDataDefinition attribute,
                                                                   final String innerType,
                                                                   final Map<String, DataTypeDefinition> dataTypes) {
        final var attributeType = attribute.getType();
        final var value = attribute.getValue();
        log.trace("Going to validate attribute value and its type. type = {}, value = {}", attributeType, value);
        final var type = getType(attributeType);
        if (type == null) {
            final var dataTypeDefinition = dataTypes.get(attributeType);
            final var validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);
            if (Boolean.FALSE.equals(validateResult.right)) {
                log.debug(THE_VALUE_OF_ATTRIBUTE_FROM_TYPE_IS_INVALID, value, attributeType);
                return Either.right(false);
            }
            return Either.left(getValueFromJsonElement(validateResult.left));
        }
        log.trace("before validating property type {}", attributeType);
        if (!isValidValue(type, value, innerType, dataTypes)) {
            log.debug(THE_VALUE_OF_ATTRIBUTE_FROM_TYPE_IS_INVALID, value, type);
            return Either.right(false);
        }
        Object convertedValue = value;
        if (!isEmptyValue(value)) {
            convertedValue = type.getConverter().convert(value, innerType, dataTypes);
        }
        return Either.left(convertedValue);
    }

}
