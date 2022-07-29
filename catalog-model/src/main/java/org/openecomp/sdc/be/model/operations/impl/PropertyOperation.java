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
package org.openecomp.sdc.be.model.operations.impl;

import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import fj.data.Either;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.JanusGraphVertexProperty;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.validation.ToscaFunctionValidator;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MinLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("property-operation")
public class PropertyOperation extends AbstractOperation implements IPropertyOperation {

    private static final String AFTER_RETRIEVING_DERIVED_FROM_NODE_OF_STATUS_IS = "After retrieving DERIVED_FROM node of {}. status is {}";
    private static final String FAILED_TO_FETCH_PROPERTIES_OF_DATA_TYPE = "Failed to fetch properties of data type {}";
    private static final String DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS = "Data type {} cannot be found in graph. status is {}";
    private static final String GOING_TO_EXECUTE_COMMIT_ON_GRAPH = "Going to execute commit on graph.";
    private static final String GOING_TO_EXECUTE_ROLLBACK_ON_GRAPH = "Going to execute rollback on graph.";
    private static final String FAILED_TO_ASSOCIATE_RESOURCE_TO_PROPERTY_IN_GRAPH_STATUS_IS = "Failed to associate resource {} to property {} in graph. status is {}";
    private static final String AFTER_ADDING_PROPERTY_TO_GRAPH = "After adding property to graph {}";
    private static final String BEFORE_ADDING_PROPERTY_TO_GRAPH = "Before adding property to graph {}";
    private static final String THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID = "The value {} of property from type {} is invalid";
    private static final String PROPERTY = "Property";
    private static final String UPDATE_DATA_TYPE = "UpdateDataType";
    private static final Logger log = Logger.getLogger(PropertyOperation.class.getName());
    private final DerivedFromOperation derivedFromOperation;
    private ToscaFunctionValidator toscaFunctionValidator;
    private DataTypeOperation dataTypeOperation;

    @Autowired
    public PropertyOperation(final HealingJanusGraphGenericDao janusGraphGenericDao, final DerivedFromOperation derivedFromOperation) {
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.derivedFromOperation = derivedFromOperation;
    }

    @Autowired
    public void setToscaFunctionValidator(final ToscaFunctionValidator toscaFunctionValidator) {
        this.toscaFunctionValidator = toscaFunctionValidator;
    }

    //circular dependency DataTypeOperation->ModelOperation->ModelElementOperation->PropertyOperation
    @Autowired
    public void setDataTypeOperation(DataTypeOperation dataTypeOperation) {
        this.dataTypeOperation = dataTypeOperation;
    }

    public PropertyDefinition convertPropertyDataToPropertyDefinition(PropertyData propertyDataResult, String propertyName, String resourceId) {
        log.debug("The object returned after create property is {}", propertyDataResult);
        PropertyDefinition propertyDefResult = new PropertyDefinition(propertyDataResult.getPropertyDataDefinition());
        propertyDefResult.setConstraints(convertConstraints(propertyDataResult.getConstraints()));
        propertyDefResult.setName(propertyName);
        return propertyDefResult;
    }

    public Either<PropertyData, StorageOperationStatus> addProperty(String propertyName, PropertyDefinition propertyDefinition, String resourceId) {
        Either<PropertyData, JanusGraphOperationStatus> either = addPropertyToGraph(propertyName, propertyDefinition, resourceId);
        if (either.isRight()) {
            StorageOperationStatus storageStatus = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(either.right().value());
            return Either.right(storageStatus);
        }
        return Either.left(either.left().value());
    }

    /**
     * @param propertyDefinition
     * @return
     */
    @Override
    public StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {
        log.trace("Going to validate property type and value. {}", propertyDefinition);
        String propertyType = propertyDefinition.getType();
        String value = propertyDefinition.getDefaultValue();
        ToscaPropertyType type = getType(propertyType);
        if (type == null) {
            DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
            if (dataTypeDefinition == null) {
                log.debug("The type {} of property cannot be found.", propertyType);
                return StorageOperationStatus.INVALID_TYPE;
            }
            return validateAndUpdateComplexValue(propertyDefinition, propertyType, value, dataTypeDefinition, dataTypes);
        }
        String innerType = null;
        Either<String, JanusGraphOperationStatus> checkInnerType = getInnerType(type, propertyDefinition::getSchema);
        if (checkInnerType.isRight()) {
            return StorageOperationStatus.INVALID_TYPE;
        }
        innerType = checkInnerType.left().value();
        log.trace("After validating property type {}", propertyType);
        boolean isValidProperty = isValidValue(type, value, innerType, dataTypes);
        if (!isValidProperty) {
            log.info(THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID, value, type);
            return StorageOperationStatus.INVALID_VALUE;
        }
        PropertyValueConverter converter = type.getConverter();
        if (isEmptyValue(value)) {
            log.debug("Default value was not sent for property {}. Set default value to {}", propertyDefinition.getName(), EMPTY_VALUE);
            propertyDefinition.setDefaultValue(EMPTY_VALUE);
        } else if (!isEmptyValue(value)) {
            String convertedValue = converter.convert(value, innerType, dataTypes);
            propertyDefinition.setDefaultValue(convertedValue);
        }
        return StorageOperationStatus.OK;
    }

    public Either<PropertyData, JanusGraphOperationStatus> addPropertyToGraph(String propertyName, PropertyDefinition propertyDefinition,
                                                                              String resourceId) {
        ResourceMetadataData resourceData = new ResourceMetadataData();
        resourceData.getMetadataDataDefinition().setUniqueId(resourceId);
        List<PropertyConstraint> constraints = propertyDefinition.getConstraints();
        propertyDefinition.setUniqueId(UniqueIdBuilder.buildComponentPropertyUniqueId(resourceId, propertyName));
        PropertyData propertyData = new PropertyData(propertyDefinition, convertConstraintsToString(constraints));
        log.debug(BEFORE_ADDING_PROPERTY_TO_GRAPH, propertyData);
        Either<PropertyData, JanusGraphOperationStatus> createNodeResult = janusGraphGenericDao.createNode(propertyData, PropertyData.class);
        log.debug(AFTER_ADDING_PROPERTY_TO_GRAPH, propertyData);
        if (createNodeResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
            log.error("Failed to add property {} to graph. status is {}", propertyName, operationStatus);
            return Either.right(operationStatus);
        }
        Map<String, Object> props = new HashMap<>();
        props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);
        Either<GraphRelation, JanusGraphOperationStatus> createRelResult = janusGraphGenericDao
            .createRelation(resourceData, propertyData, GraphEdgeLabels.PROPERTY, props);
        if (createRelResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
            log.error(FAILED_TO_ASSOCIATE_RESOURCE_TO_PROPERTY_IN_GRAPH_STATUS_IS, resourceId, propertyName, operationStatus);
            return Either.right(operationStatus);
        }
        return Either.left(createNodeResult.left().value());
    }

    public JanusGraphOperationStatus addPropertyToGraphByVertex(JanusGraphVertex metadataVertex, String propertyName,
                                                                PropertyDefinition propertyDefinition, String resourceId) {
        List<PropertyConstraint> constraints = propertyDefinition.getConstraints();
        propertyDefinition.setUniqueId(UniqueIdBuilder.buildComponentPropertyUniqueId(resourceId, propertyName));
        PropertyData propertyData = new PropertyData(propertyDefinition, convertConstraintsToString(constraints));
        log.debug(BEFORE_ADDING_PROPERTY_TO_GRAPH, propertyData);
        Either<JanusGraphVertex, JanusGraphOperationStatus> createNodeResult = janusGraphGenericDao.createNode(propertyData);
        log.debug(AFTER_ADDING_PROPERTY_TO_GRAPH, propertyData);
        if (createNodeResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
            log.error("Failed to add property {} to graph. status is ", propertyName, operationStatus);
            return operationStatus;
        }
        Map<String, Object> props = new HashMap<>();
        props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);
        JanusGraphVertex propertyVertex = createNodeResult.left().value();
        JanusGraphOperationStatus createRelResult = janusGraphGenericDao.createEdge(metadataVertex, propertyVertex, GraphEdgeLabels.PROPERTY, props);
        if (!createRelResult.equals(JanusGraphOperationStatus.OK)) {
            log.error(FAILED_TO_ASSOCIATE_RESOURCE_TO_PROPERTY_IN_GRAPH_STATUS_IS, resourceId, propertyName, createRelResult);
            return createRelResult;
        }
        return createRelResult;
    }

    public JanusGraphGenericDao getJanusGraphGenericDao() {
        return janusGraphGenericDao;
    }

    /**
     * FOR TEST ONLY
     *
     * @param janusGraphGenericDao
     */
    public void setJanusGraphGenericDao(HealingJanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    public Either<PropertyData, JanusGraphOperationStatus> deletePropertyFromGraph(String propertyId) {
        log.debug("Before deleting property from graph {}", propertyId);
        return janusGraphGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);
    }

    public Either<PropertyData, StorageOperationStatus> updateProperty(String propertyId, PropertyDefinition newPropertyDefinition,
                                                                       Map<String, DataTypeDefinition> dataTypes) {
        StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(newPropertyDefinition, dataTypes);
        if (validateAndUpdateProperty != StorageOperationStatus.OK) {
            return Either.right(validateAndUpdateProperty);
        }
        Either<PropertyData, JanusGraphOperationStatus> either = updatePropertyFromGraph(propertyId, newPropertyDefinition);
        if (either.isRight()) {
            StorageOperationStatus storageStatus = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(either.right().value());
            return Either.right(storageStatus);
        }
        return Either.left(either.left().value());
    }

    public Either<PropertyData, JanusGraphOperationStatus> updatePropertyFromGraph(String propertyId, PropertyDefinition propertyDefinition) {
        if (log.isDebugEnabled()) {
            log.debug("Before updating property on graph {}", propertyId);
        }
        // get the original property data
        Either<PropertyData, JanusGraphOperationStatus> statusProperty = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);
        if (statusProperty.isRight()) {
            log.debug("Problem while get property with id {}. Reason - {}", propertyId, statusProperty.right().value().name());
            return Either.right(statusProperty.right().value());
        }
        PropertyData orgPropertyData = statusProperty.left().value();
        PropertyDataDefinition orgPropertyDataDefinition = orgPropertyData.getPropertyDataDefinition();
        // create new property data to update
        PropertyData newPropertyData = new PropertyData();
        newPropertyData.setPropertyDataDefinition(propertyDefinition);
        PropertyDataDefinition newPropertyDataDefinition = newPropertyData.getPropertyDataDefinition();
        // update the original property data with new values
        if (orgPropertyDataDefinition.getDefaultValue() == null) {
            orgPropertyDataDefinition.setDefaultValue(newPropertyDataDefinition.getDefaultValue());
        } else {
            if (!orgPropertyDataDefinition.getDefaultValue().equals(newPropertyDataDefinition.getDefaultValue())) {
                orgPropertyDataDefinition.setDefaultValue(newPropertyDataDefinition.getDefaultValue());
            }
        }
        if (orgPropertyDataDefinition.getDescription() == null) {
            orgPropertyDataDefinition.setDescription(newPropertyDataDefinition.getDescription());
        } else {
            if (!orgPropertyDataDefinition.getDescription().equals(newPropertyDataDefinition.getDescription())) {
                orgPropertyDataDefinition.setDescription(newPropertyDataDefinition.getDescription());
            }
        }
        if (!orgPropertyDataDefinition.getType().equals(newPropertyDataDefinition.getType())) {
            orgPropertyDataDefinition.setType(newPropertyDataDefinition.getType());
        }
        if (newPropertyData.getConstraints() != null) {
            orgPropertyData.setConstraints(newPropertyData.getConstraints());
        }
        orgPropertyDataDefinition.setSchema(newPropertyDataDefinition.getSchema());
        return janusGraphGenericDao.updateNode(orgPropertyData, PropertyData.class);
    }

    public Either<PropertyData, JanusGraphOperationStatus> addPropertyToNodeType(String propertyName, PropertyDefinition propertyDefinition,
                                                                                 NodeTypeEnum nodeType, String uniqueId) {
        List<PropertyConstraint> constraints = propertyDefinition.getConstraints();
        propertyDefinition.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(uniqueId, propertyName));
        PropertyData propertyData = new PropertyData(propertyDefinition, convertConstraintsToString(constraints));
        if (log.isDebugEnabled()) {
            log.debug(BEFORE_ADDING_PROPERTY_TO_GRAPH, propertyData);
        }
        Either<PropertyData, JanusGraphOperationStatus> createNodeResult = janusGraphGenericDao.createNode(propertyData, PropertyData.class);
        if (log.isDebugEnabled()) {
            log.debug(AFTER_ADDING_PROPERTY_TO_GRAPH, propertyData);
        }
        if (createNodeResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
            log.error("Failed to add property {} to graph. status is {}", propertyName, operationStatus);
            return Either.right(operationStatus);
        }
        Map<String, Object> props = new HashMap<>();
        props.put(GraphPropertiesDictionary.NAME.getProperty(), propertyName);
        UniqueIdData uniqueIdData = new UniqueIdData(nodeType, uniqueId);
        log.debug("Before associating {} to property {}", uniqueIdData, propertyName);
        Either<GraphRelation, JanusGraphOperationStatus> createRelResult = janusGraphGenericDao
            .createRelation(uniqueIdData, propertyData, GraphEdgeLabels.PROPERTY, props);
        if (createRelResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
            log.error(FAILED_TO_ASSOCIATE_RESOURCE_TO_PROPERTY_IN_GRAPH_STATUS_IS, uniqueId, propertyName, operationStatus);
            return Either.right(operationStatus);
        }
        return Either.left(createNodeResult.left().value());
    }

    public Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesOfNode(NodeTypeEnum nodeType, String uniqueId) {
        Map<String, PropertyDefinition> resourceProps = new HashMap<>();
        Either<List<ImmutablePair<PropertyData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.PROPERTY, NodeTypeEnum.Property,
                PropertyData.class);
        if (childrenNodes.isRight()) {
            JanusGraphOperationStatus operationStatus = childrenNodes.right().value();
            return Either.right(operationStatus);
        }
        List<ImmutablePair<PropertyData, GraphEdge>> values = childrenNodes.left().value();
        if (values != null) {
            for (ImmutablePair<PropertyData, GraphEdge> immutablePair : values) {
                GraphEdge edge = immutablePair.getValue();
                String propertyName = (String) edge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());
                log.debug("Property {} is associated to node {}", propertyName, uniqueId);
                PropertyData propertyData = immutablePair.getKey();
                PropertyDefinition propertyDefinition = this.convertPropertyDataToPropertyDefinition(propertyData, propertyName, uniqueId);
                resourceProps.put(propertyName, propertyDefinition);
            }
        }
        log.debug("The properties associated to node {} are {}", uniqueId, resourceProps);
        return Either.left(resourceProps);
    }

    public Either<Map<String, PropertyDefinition>, StorageOperationStatus> deletePropertiesAssociatedToNode(NodeTypeEnum nodeType, String uniqueId) {
        return deleteAllPropertiesAssociatedToNode(nodeType, uniqueId).right()
            .bind(err -> err == StorageOperationStatus.OK ? Either.left(Collections.emptyMap()) : Either.right(err));
    }

    public Either<Map<String, PropertyData>, JanusGraphOperationStatus> mergePropertiesAssociatedToNode(NodeTypeEnum nodeType, String uniqueId,
                                                                                                        Map<String, PropertyDefinition> newProperties) {
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> oldPropertiesRes = findPropertiesOfNode(nodeType, uniqueId);
        Map<String, PropertyDefinition> reallyNewProperties;
        Map<String, PropertyData> unchangedPropsData;
        if (oldPropertiesRes.isRight()) {
            JanusGraphOperationStatus err = oldPropertiesRes.right().value();
            if (err == JanusGraphOperationStatus.NOT_FOUND) {
                reallyNewProperties = newProperties;
                unchangedPropsData = Collections.emptyMap();
            } else {
                return Either.right(err);
            }
        } else {
            Map<String, PropertyDefinition> oldProperties = oldPropertiesRes.left().value();
            reallyNewProperties = collectReallyNewProperties(newProperties, oldProperties);
            for (Entry<String, PropertyDefinition> oldEntry : oldProperties.entrySet()) {
                String key = oldEntry.getKey();
                PropertyDefinition newPropDef = newProperties != null ? newProperties.get(key) : null;
                PropertyDefinition oldPropDef = oldEntry.getValue();
                JanusGraphOperationStatus status = updateOldProperty(newPropDef, oldPropDef);
                if (status != JanusGraphOperationStatus.OK) {
                    return Either.right(status);
                }
            }
            unchangedPropsData = oldProperties.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> new PropertyData(e.getValue(), null)));
        }
        // add other properties
        return addPropertiesToElementType(nodeType, uniqueId, reallyNewProperties, unchangedPropsData);
    }

    /**
     * @param newProperties
     * @param oldProperties
     * @return
     */
    private Map<String, PropertyDefinition> collectReallyNewProperties(Map<String, PropertyDefinition> newProperties,
                                                                       Map<String, PropertyDefinition> oldProperties) {
        return newProperties != null ? newProperties.entrySet().stream().filter(entry -> !oldProperties.containsKey(entry.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue)) : null;
    }

    /**
     * @param newPropDef
     * @param oldPropDef
     */
    private JanusGraphOperationStatus updateOldProperty(PropertyDefinition newPropDef, PropertyDefinition oldPropDef) {
        if (!isUpdateAllowed(newPropDef, oldPropDef)) {
            return JanusGraphOperationStatus.MATCH_NOT_FOUND;
        }
        if (isUpdateRequired(newPropDef, oldPropDef)) {
            modifyOldPropByNewOne(newPropDef, oldPropDef);
            List<PropertyConstraint> constraints = oldPropDef.getConstraints();
            PropertyData node = new PropertyData(oldPropDef, convertConstraintsToString(constraints));
            Either<PropertyData, JanusGraphOperationStatus> updateResult = janusGraphGenericDao.updateNode(node, PropertyData.class);
            if (updateResult.isRight()) {
                return updateResult.right().value();
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    /**
     * @param newPropDef
     * @param oldPropDef
     */
    private boolean isUpdateAllowed(PropertyDefinition newPropDef, PropertyDefinition oldPropDef) {
        if (newPropDef == null) {
            log.error("#mergePropertiesAssociatedToNode - Failed due attempt to delete the property with id {}", oldPropDef.getUniqueId());
            return false;
        }
        // If the property type is missing it's something that we could want to fix
        if (oldPropDef.getType() != null && !oldPropDef.getType().equals(newPropDef.getType())) {
            log.error("#mergePropertiesAssociatedToNode - Failed due attempt to change type of the property with id {}", oldPropDef.getUniqueId());
            return false;
        }
        return true;
    }

    /**
     * Update only fields which modification is permitted.
     *
     * @param newPropDef
     * @param oldPropDef
     */
    private void modifyOldPropByNewOne(PropertyDefinition newPropDef, PropertyDefinition oldPropDef) {
        oldPropDef.setDefaultValue(newPropDef.getDefaultValue());
        oldPropDef.setDescription(newPropDef.getDescription());
        oldPropDef.setRequired(newPropDef.isRequired());
        // Type is updated to fix possible null type issue in janusGraph DB
        oldPropDef.setType(newPropDef.getType());
    }

    private boolean isUpdateRequired(PropertyDefinition newPropDef, PropertyDefinition oldPropDef) {
        return !StringUtils.equals(oldPropDef.getDefaultValue(), newPropDef.getDefaultValue()) || !StringUtils
            .equals(oldPropDef.getDescription(), newPropDef.getDescription()) || oldPropDef.isRequired() != newPropDef.isRequired();
    }

    /**
     * Adds newProperties and returns in case of success (left part of Either) map of all properties i. e. added ones and contained in
     * unchangedPropsData
     *
     * @param nodeType
     * @param uniqueId
     * @param newProperties
     * @param unchangedPropsData
     * @return
     */
    private Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToElementType(NodeTypeEnum nodeType, String uniqueId,
                                                                                                    Map<String, PropertyDefinition> newProperties,
                                                                                                    Map<String, PropertyData> unchangedPropsData) {
        return addPropertiesToElementType(uniqueId, nodeType, newProperties).left().map(m -> {
            m.putAll(unchangedPropsData);
            return m;
        });
    }

    public Either<Map<String, PropertyDefinition>, StorageOperationStatus> deleteAllPropertiesAssociatedToNode(NodeTypeEnum nodeType,
                                                                                                               String uniqueId) {
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> propertiesOfNodeRes = findPropertiesOfNode(nodeType, uniqueId);
        if (propertiesOfNodeRes.isRight()) {
            JanusGraphOperationStatus status = propertiesOfNodeRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                return Either.right(StorageOperationStatus.OK);
            }
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        Map<String, PropertyDefinition> value = propertiesOfNodeRes.left().value();
        for (PropertyDefinition propertyDefinition : value.values()) {
            String propertyUid = propertyDefinition.getUniqueId();
            Either<PropertyData, JanusGraphOperationStatus> deletePropertyRes = deletePropertyFromGraph(propertyUid);
            if (deletePropertyRes.isRight()) {
                log.error("Failed to delete property with id {}", propertyUid);
                JanusGraphOperationStatus status = deletePropertyRes.right().value();
                if (status == JanusGraphOperationStatus.NOT_FOUND) {
                    status = JanusGraphOperationStatus.INVALID_ID;
                }
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }
        log.debug("The properties deleted from node {} are {}", uniqueId, value);
        return Either.left(value);
    }

    /**
     * Checks existence of a property with the same name belonging to the same resource or existence of property with the same name and different type
     * (including derived from hierarchy)
     *
     * @param properties
     * @param resourceUid
     * @param propertyName
     * @param propertyType
     * @return
     */
    public boolean isPropertyExist(List<PropertyDefinition> properties, String resourceUid, String propertyName, String propertyType) {
        boolean result = false;
        if (!CollectionUtils.isEmpty(properties)) {
            for (PropertyDefinition propertyDefinition : properties) {
                if (propertyDefinition.getName().equals(propertyName) && (propertyDefinition.getParentUniqueId().equals(resourceUid)
                    || !propertyDefinition.getType().equals(propertyType))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public ImmutablePair<String, Boolean> validateAndUpdateRules(String propertyType, List<PropertyRule> rules, String innerType,
                                                                 Map<String, DataTypeDefinition> dataTypes, boolean isValidate) {
        if (rules == null || rules.isEmpty()) {
            return new ImmutablePair<>(null, true);
        }
        for (PropertyRule rule : rules) {
            String value = rule.getValue();
            Either<Object, Boolean> updateResult = validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, dataTypes);
            if (updateResult.isRight()) {
                Boolean status = updateResult.right().value();
                if (!status) {
                    return new ImmutablePair<>(value, status);
                }
            } else {
                String newValue = null;
                Object object = updateResult.left().value();
                if (object != null) {
                    newValue = object.toString();
                }
                rule.setValue(newValue);
            }
        }
        return new ImmutablePair<>(null, true);
    }

    public void addRulesToNewPropertyValue(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty,
                                           String resourceInstanceId) {
        List<PropertyRule> rules = resourceInstanceProperty.getRules();
        if (rules == null) {
            PropertyRule propertyRule = buildRuleFromPath(propertyValueData, resourceInstanceProperty, resourceInstanceId);
            rules = new ArrayList<>();
            rules.add(propertyRule);
        } else {
            rules = sortRules(rules);
        }
        propertyValueData.setRules(rules);
    }

    private PropertyRule buildRuleFromPath(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty,
                                           String resourceInstanceId) {
        List<String> path = resourceInstanceProperty.getPath();
        // FOR BC. Since old Property values on VFC/VF does not have rules on

        // graph.

        // Update could be done on one level only, thus we can use this

        // operation to avoid migration.
        if (path == null || path.isEmpty()) {
            path = new ArrayList<>();
            path.add(resourceInstanceId);
        }
        PropertyRule propertyRule = new PropertyRule();
        propertyRule.setRule(path);
        propertyRule.setValue(propertyValueData.getValue());
        return propertyRule;
    }

    private List<PropertyRule> sortRules(List<PropertyRule> rules) {
        // TODO: sort the rules by size and binary representation.

        // (x, y, .+) --> 110 6 priority 1

        // (x, .+, z) --> 101 5 priority 2
        return rules;
    }

    public ImmutablePair<JanusGraphOperationStatus, String> findPropertyValue(String resourceInstanceId, String propertyId) {
        log.debug("Going to check whether the property {} already added to resource instance {}", propertyId, resourceInstanceId);
        Either<List<ComponentInstanceProperty>, JanusGraphOperationStatus> getAllRes = this
            .getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceId);
        if (getAllRes.isRight()) {
            JanusGraphOperationStatus status = getAllRes.right().value();
            log.trace("After fetching all properties of resource instance {}. Status is {}", resourceInstanceId, status);
            return new ImmutablePair<>(status, null);
        }
        List<ComponentInstanceProperty> list = getAllRes.left().value();
        if (list != null) {
            for (ComponentInstanceProperty instanceProperty : list) {
                String propertyUniqueId = instanceProperty.getUniqueId();
                String valueUniqueUid = instanceProperty.getValueUniqueUid();
                log.trace("Go over property {} under resource instance {}. valueUniqueId = {}", propertyUniqueId, resourceInstanceId, valueUniqueUid);
                if (propertyId.equals(propertyUniqueId) && valueUniqueUid != null) {
                    log.debug("The property {} already created under resource instance {}", propertyId, resourceInstanceId);
                    return new ImmutablePair<>(JanusGraphOperationStatus.ALREADY_EXIST, valueUniqueUid);
                }
            }
        }
        return new ImmutablePair<>(JanusGraphOperationStatus.NOT_FOUND, null);
    }

    public void updateRulesInPropertyValue(PropertyValueData propertyValueData, ComponentInstanceProperty resourceInstanceProperty,
                                           String resourceInstanceId) {
        List<PropertyRule> currentRules = propertyValueData.getRules();
        List<PropertyRule> rules = resourceInstanceProperty.getRules();
        // if rules are not supported.
        if (rules == null) {
            PropertyRule propertyRule = buildRuleFromPath(propertyValueData, resourceInstanceProperty, resourceInstanceId);
            rules = new ArrayList<>();
            rules.add(propertyRule);
            if (currentRules != null) {
                rules = mergeRules(currentRules, rules);
            }
        } else {
            // Full mode. all rules are sent in update operation.
            rules = sortRules(rules);
        }
        propertyValueData.setRules(rules);
    }

    private List<PropertyRule> mergeRules(List<PropertyRule> currentRules, List<PropertyRule> newRules) {
        List<PropertyRule> mergedRules = new ArrayList<>();
        if (newRules == null || newRules.isEmpty()) {
            return currentRules;
        }
        for (PropertyRule rule : currentRules) {
            PropertyRule propertyRule = new PropertyRule(rule.getRule(), rule.getValue());
            mergedRules.add(propertyRule);
        }
        for (PropertyRule rule : newRules) {
            PropertyRule foundRule = findRuleInList(rule, mergedRules);
            if (foundRule != null) {
                foundRule.setValue(rule.getValue());
            } else {
                mergedRules.add(rule);
            }
        }
        return mergedRules;
    }

    private PropertyRule findRuleInList(PropertyRule rule, List<PropertyRule> rules) {
        if (rules == null || rules.isEmpty() || rule.getRule() == null || rule.getRule().isEmpty()) {
            return null;
        }
        PropertyRule foundRule = null;
        for (PropertyRule propertyRule : rules) {
            if (rule.getRuleSize() != propertyRule.getRuleSize()) {
                continue;
            }
            boolean equals = propertyRule.compareRule(rule);
            if (equals) {
                foundRule = propertyRule;
                break;
            }
        }
        return foundRule;
    }

    /**
     * return all properties associated to resource instance. The result does contains the property unique id but not its type, default value...
     *
     * @param resourceInstanceUid
     * @return
     */
    public Either<List<ComponentInstanceProperty>, JanusGraphOperationStatus> getAllPropertiesOfResourceInstanceOnlyPropertyDefId(
        String resourceInstanceUid) {
        return getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceUid, NodeTypeEnum.ResourceInstance);
    }

    public Either<PropertyValueData, JanusGraphOperationStatus> removePropertyOfResourceInstance(String propertyValueUid, String resourceInstanceId) {
        Either<ComponentInstanceData, JanusGraphOperationStatus> findResInstanceRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, ComponentInstanceData.class);
        if (findResInstanceRes.isRight()) {
            JanusGraphOperationStatus status = findResInstanceRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return Either.right(status);
        }
        Either<PropertyValueData, JanusGraphOperationStatus> findPropertyDefRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueUid, PropertyValueData.class);
        if (findPropertyDefRes.isRight()) {
            JanusGraphOperationStatus status = findPropertyDefRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return Either.right(status);
        }
        Either<GraphRelation, JanusGraphOperationStatus> relation = janusGraphGenericDao
            .getRelation(findResInstanceRes.left().value(), findPropertyDefRes.left().value(), GraphEdgeLabels.PROPERTY_VALUE);
        if (relation.isRight()) {
            // TODO: add error in case of error
            JanusGraphOperationStatus status = relation.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return Either.right(status);
        }
        Either<PropertyValueData, JanusGraphOperationStatus> deleteNode = janusGraphGenericDao
            .deleteNode(findPropertyDefRes.left().value(), PropertyValueData.class);
        if (deleteNode.isRight()) {
            return Either.right(deleteNode.right().value());
        }
        PropertyValueData value = deleteNode.left().value();
        return Either.left(value);
    }

    public Either<ComponentInstanceProperty, StorageOperationStatus> removePropertyValueFromResourceInstance(String propertyValueUid,
                                                                                                             String resourceInstanceId,
                                                                                                             boolean inTransaction) {
        Either<ComponentInstanceProperty, StorageOperationStatus> result = null;
        try {
            Either<PropertyValueData, JanusGraphOperationStatus> eitherStatus = this
                .removePropertyOfResourceInstance(propertyValueUid, resourceInstanceId);
            if (eitherStatus.isRight()) {
                log.error("Failed to remove property value {} from resource instance {} in Graph. status is {}", propertyValueUid, resourceInstanceId,
                    eitherStatus.right().value().name());
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(eitherStatus.right().value()));
                return result;
            } else {
                PropertyValueData propertyValueData = eitherStatus.left().value();
                ComponentInstanceProperty propertyValueResult = new ComponentInstanceProperty();
                propertyValueResult.setUniqueId(resourceInstanceId);
                propertyValueResult.setValue(propertyValueData.getValue());
                log.debug("The returned ResourceInstanceProperty is  {}", propertyValueResult);
                result = Either.left(propertyValueResult);
                return result;
            }
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.error(GOING_TO_EXECUTE_ROLLBACK_ON_GRAPH);
                    janusGraphGenericDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_GRAPH);
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    public ComponentInstanceProperty buildResourceInstanceProperty(PropertyValueData propertyValueData,
                                                                   ComponentInstanceProperty resourceInstanceProperty) {
        String value = propertyValueData.getValue();
        String uid = propertyValueData.getUniqueId();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty(resourceInstanceProperty, value, uid);
        instanceProperty.setPath(resourceInstanceProperty.getPath());
        return instanceProperty;
    }

    @Override
    public boolean isPropertyDefaultValueValid(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {
        if (propertyDefinition == null) {
            return false;
        }
        String innerType = null;
        String propertyType = propertyDefinition.getType();
        ToscaPropertyType type = getType(propertyType);
        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            SchemaDefinition def = propertyDefinition.getSchema();
            if (def == null) {
                return false;
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                return false;
            }
            innerType = propDef.getType();
        }
        String value = propertyDefinition.getDefaultValue();
        if (type != null) {
            return isValidValue(type, value, innerType, dataTypes);
        } else {
            log.trace("The given type {} is not a pre defined one.", propertyType);
            DataTypeDefinition foundDt = dataTypes.get(propertyType);
            if (foundDt != null) {
                return isValidComplexValue(foundDt, value, dataTypes);
            } else {
                return false;
            }
        }
    }

    public boolean isPropertyTypeValid(final IComplexDefaultValue property, final String model) {
        if (property == null) {
            return false;
        }
        if (ToscaPropertyType.isValidType(property.getType()) == null) {
            Either<Boolean, JanusGraphOperationStatus> definedInDataTypes = isDefinedInDataTypes(property.getType(), model);
            if (definedInDataTypes.isRight()) {
                return false;
            } else {
                Boolean isExist = definedInDataTypes.left().value();
                return isExist.booleanValue();
            }
        }
        return true;
    }
    
    public boolean isPropertyTypeValid(final IComplexDefaultValue property, final Map<String, DataTypeDefinition> dataTypes) {
        if (property == null) {
            return false;
        }
        return ToscaPropertyType.isValidType(property.getType()) != null || dataTypes.containsKey(property.getType());
    }

    @Override
    public ImmutablePair<String, Boolean> isPropertyInnerTypeValid(IComplexDefaultValue property, Map<String, DataTypeDefinition> dataTypes) {
        if (property == null) {
            return new ImmutablePair<>(null, false);
        }
        SchemaDefinition schema;
        PropertyDataDefinition innerProp;
        String innerType = null;
        if ((schema = property.getSchema()) != null) {
            if ((innerProp = schema.getProperty()) != null) {
                innerType = innerProp.getType();
            }
        }
        ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);
        if (innerToscaType == null) {
            DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
            if (dataTypeDefinition == null) {
                log.debug("The inner type {} is not a data type.", innerType);
                return new ImmutablePair<>(innerType, false);
            } else {
                log.debug("The inner type {} is a data type. Data type definition is {}", innerType, dataTypeDefinition);
            }
        }
        return new ImmutablePair<>(innerType, true);
    }

    private boolean isValidComplexValue(DataTypeDefinition foundDt, String value, Map<String, DataTypeDefinition> dataTypes) {
        ImmutablePair<JsonElement, Boolean> validateAndUpdate = dataTypeValidatorConverter.validateAndUpdate(value, foundDt, dataTypes);
        log.trace("The result after validating complex value of type {} is {}", foundDt.getName(), validateAndUpdate);
        return validateAndUpdate.right.booleanValue();
    }

    public Either<List<ComponentInstanceProperty>, JanusGraphOperationStatus> getAllPropertiesOfResourceInstanceOnlyPropertyDefId(
        String resourceInstanceUid, NodeTypeEnum instanceNodeType) {
        Either<JanusGraphVertex, JanusGraphOperationStatus> findResInstanceRes = janusGraphGenericDao
            .getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid);
        if (findResInstanceRes.isRight()) {
            JanusGraphOperationStatus status = findResInstanceRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return Either.right(status);
        }
        Either<List<ImmutablePair<JanusGraphVertex, Edge>>, JanusGraphOperationStatus> propertyImplNodes = janusGraphGenericDao
            .getChildrenVertecies(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid, GraphEdgeLabels.PROPERTY_VALUE);
        if (propertyImplNodes.isRight()) {
            JanusGraphOperationStatus status = propertyImplNodes.right().value();
            return Either.right(status);
        }
        List<ImmutablePair<JanusGraphVertex, Edge>> list = propertyImplNodes.left().value();
        if (list == null || list.isEmpty()) {
            return Either.right(JanusGraphOperationStatus.NOT_FOUND);
        }
        List<ComponentInstanceProperty> result = new ArrayList<>();
        for (ImmutablePair<JanusGraphVertex, Edge> propertyValue : list) {
            JanusGraphVertex propertyValueDataVertex = propertyValue.getLeft();
            String propertyValueUid = (String) janusGraphGenericDao
                .getProperty(propertyValueDataVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
            String value = (String) janusGraphGenericDao.getProperty(propertyValueDataVertex, GraphPropertiesDictionary.VALUE.getProperty());
            ImmutablePair<JanusGraphVertex, Edge> propertyDefPair = janusGraphGenericDao
                .getChildVertex(propertyValueDataVertex, GraphEdgeLabels.PROPERTY_IMPL);
            if (propertyDefPair == null) {
                return Either.right(JanusGraphOperationStatus.NOT_FOUND);
            }
            Map<String, Object> properties = janusGraphGenericDao.getProperties(propertyValueDataVertex);
            PropertyValueData propertyValueData = GraphElementFactory
                .createElement(NodeTypeEnum.PropertyValue.getName(), GraphElementTypeEnum.Node, properties, PropertyValueData.class);
            String propertyUniqueId = (String) janusGraphGenericDao
                .getProperty(propertyDefPair.left, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
            ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty();
            // set property original unique id
            resourceInstanceProperty.setUniqueId(propertyUniqueId);
            // set resource id

            // TODO: esofer add resource id
            resourceInstanceProperty.setParentUniqueId(null);
            // set value
            resourceInstanceProperty.setValue(value);
            // set property value unique id
            resourceInstanceProperty.setValueUniqueUid(propertyValueUid);
            // set rules
            resourceInstanceProperty.setRules(propertyValueData.getRules());
            result.add(resourceInstanceProperty);
        }
        return Either.left(result);
    }

    /**
     * Find the default value from the list of component instances. Start the search from the second component instance
     *
     * @param pathOfComponentInstances
     * @param propertyUniqueId
     * @param defaultValue
     * @return
     */
    public Either<String, JanusGraphOperationStatus> findDefaultValueFromSecondPosition(List<String> pathOfComponentInstances,
                                                                                        String propertyUniqueId, String defaultValue) {
        log.trace("In find default value: path= {} propertyUniqId={} defaultValue= {}", pathOfComponentInstances, propertyUniqueId, defaultValue);
        if (pathOfComponentInstances == null || pathOfComponentInstances.size() < 2) {
            return Either.left(defaultValue);
        }
        String result = defaultValue;
        for (int i = 1; i < pathOfComponentInstances.size(); i++) {
            String compInstanceId = pathOfComponentInstances.get(i);
            Either<List<ComponentInstanceProperty>, JanusGraphOperationStatus> propertyValuesResult = this
                .getAllPropertiesOfResourceInstanceOnlyPropertyDefId(compInstanceId, NodeTypeEnum.ResourceInstance);
            log.trace("After fetching properties values of component instance {}. {}", compInstanceId, propertyValuesResult);
            if (propertyValuesResult.isRight()) {
                JanusGraphOperationStatus status = propertyValuesResult.right().value();
                if (status != JanusGraphOperationStatus.NOT_FOUND) {
                    return Either.right(status);
                } else {
                    continue;
                }
            }
            ComponentInstanceProperty foundCompInstanceProperty = fetchByPropertyUid(propertyValuesResult.left().value(), propertyUniqueId);
            log.trace("After finding the component instance property on{} . {}", compInstanceId, foundCompInstanceProperty);
            if (foundCompInstanceProperty == null) {
                continue;
            }
            List<PropertyRule> rules = getOrBuildRulesIfNotExists(pathOfComponentInstances.size() - i, pathOfComponentInstances.get(i),
                foundCompInstanceProperty.getRules(), foundCompInstanceProperty.getValue());
            log.trace("Rules of property {} on component instance {} are {}", propertyUniqueId, compInstanceId, rules);
            PropertyRule matchedRule = findMatchRule(pathOfComponentInstances, i, rules);
            log.trace("Match rule is {}", matchedRule);
            if (matchedRule != null) {
                result = matchedRule.getValue();
                break;
            }
        }
        return Either.left(result);
    }

    private ComponentInstanceProperty fetchByPropertyUid(List<ComponentInstanceProperty> list, String propertyUniqueId) {
        ComponentInstanceProperty result = null;
        if (list == null) {
            return null;
        }
        for (ComponentInstanceProperty instProperty : list) {
            if (instProperty.getUniqueId().equals(propertyUniqueId)) {
                result = instProperty;
                break;
            }
        }
        return result;
    }

    private List<PropertyRule> getOrBuildRulesIfNotExists(int ruleSize, String compInstanceId, List<PropertyRule> rules, String value) {
        if (rules != null) {
            return rules;
        }
        rules = buildDefaultRule(compInstanceId, ruleSize, value);
        return rules;
    }

    private List<PropertyRule> getRulesOfPropertyValue(int size, String instanceId, ComponentInstanceProperty componentInstanceProperty) {
        List<PropertyRule> rules = componentInstanceProperty.getRules();
        if (rules == null) {
            rules = buildDefaultRule(instanceId, size, componentInstanceProperty.getValue());
        }
        return rules;
    }

    private List<PropertyRule> buildDefaultRule(String componentInstanceId, int size, String value) {
        List<PropertyRule> rules = new ArrayList<>();
        List<String> rule = new ArrayList<>();
        rule.add(componentInstanceId);
        for (int i = 0; i < size - 1; i++) {
            rule.add(PropertyRule.getRuleAnyMatch());
        }
        PropertyRule propertyRule = new PropertyRule(rule, value);
        rules.add(propertyRule);
        return rules;
    }

    private PropertyRule findMatchRule(List<String> pathOfInstances, int level, List<PropertyRule> rules) {
        PropertyRule propertyRule = null;
        String stringForMatch = buildStringForMatch(pathOfInstances, level);
        String firstCompInstance = pathOfInstances.get(level);
        if (rules != null) {
            for (PropertyRule rule : rules) {
                int ruleSize = rule.getRule().size();
                // check the length of the rule equals to the length of the

                // instances path.
                if (ruleSize != pathOfInstances.size() - level) {
                    continue;
                }
                // check that the rule starts with correct component instance id
                if (!checkFirstItem(firstCompInstance, rule.getFirstToken())) {
                    continue;
                }
                String secondToken = rule.getToken(2);
                if (secondToken != null && (secondToken.equals(PropertyRule.getForceAll()) || secondToken.equals(PropertyRule.getALL()))) {
                    propertyRule = rule;
                    break;
                }
                String patternStr = buildStringForMatch(rule.getRule(), 0);
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(stringForMatch);
                if (matcher.matches()) {
                    if (log.isTraceEnabled()) {
                        log.trace("{} matches the rule {}", stringForMatch, patternStr);
                    }
                    propertyRule = rule;
                    break;
                }
            }
        }
        return propertyRule;
    }

    private boolean checkFirstItem(String left, String right) {
        if (left != null && left.equals(right)) {
            return true;
        }
        return false;
    }

    private String buildStringForMatch(List<String> pathOfInstances, int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = level; i < pathOfInstances.size(); i++) {
            builder.append(pathOfInstances.get(i));
            if (i < pathOfInstances.size() - 1) {
                builder.append("#");
            }
        }
        return builder.toString();
    }

    public void updatePropertyByBestMatch(String propertyUniqueId, ComponentInstanceProperty instanceProperty,
                                          Map<String, ComponentInstanceProperty> instanceIdToValue) {
        List<String> pathOfInstances = instanceProperty.getPath();
        int level = 0;
        int size = pathOfInstances.size();
        int numberOfMatches = 0;
        for (String instanceId : pathOfInstances) {
            ComponentInstanceProperty componentInstanceProperty = instanceIdToValue.get(instanceId);
            if (componentInstanceProperty != null) {
                List<PropertyRule> rules = getRulesOfPropertyValue(size - level, instanceId, componentInstanceProperty);
                // If it is the first level instance, then update valueUniuqeId

                // parameter in order to know on update that

                // we should update and not create new node on graph.
                if (level == 0) {
                    instanceProperty.setValueUniqueUid(componentInstanceProperty.getValueUniqueUid());
                    instanceProperty.setRules(rules);
                }
                PropertyRule rule = findMatchRule(pathOfInstances, level, rules);
                if (rule != null) {
                    numberOfMatches++;
                    String value = rule.getValue();
                    if (numberOfMatches == 1) {
                        instanceProperty.setValue(value);
                        if (log.isDebugEnabled()) {
                            log.debug("Set the value of property {} {} on path {} to be {}", propertyUniqueId, instanceProperty.getName(),
                                pathOfInstances, value);
                        }
                    } else if (numberOfMatches == 2) {
                        // In case of another property value match, then use the

                        // value to be the default value of the property.
                        instanceProperty.setDefaultValue(value);
                        if (log.isDebugEnabled()) {
                            log.debug("Set the default value of property {} {} on path {} to be {}", propertyUniqueId, instanceProperty.getName(),
                                pathOfInstances, value);
                        }
                        break;
                    }
                }
            }
            level++;
        }
    }

    /**
     * Add data type to graph.
     * <p>
     * 1. Add data type node
     * <p>
     * 2. Add edge between the former node to its parent(if exists)
     * <p>
     * 3. Add property node and associate it to the node created at #1. (per property & if exists)
     *
     * @param dataTypeDefinition
     * @return
     */
    private Either<DataTypeData, JanusGraphOperationStatus> addDataTypeToGraph(DataTypeDefinition dataTypeDefinition) {
        log.debug("Got data type {}", dataTypeDefinition);
        String dtUniqueId = UniqueIdBuilder.buildDataTypeUid(dataTypeDefinition.getModel(), dataTypeDefinition.getName());
        DataTypeData dataTypeData = buildDataTypeData(dataTypeDefinition, dtUniqueId);
        log.debug("Before adding data type to graph. dataTypeData = {}", dataTypeData);
        Either<DataTypeData, JanusGraphOperationStatus> createDataTypeResult = janusGraphGenericDao.createNode(dataTypeData, DataTypeData.class);
        log.debug("After adding data type to graph. status is = {}", createDataTypeResult);
        if (createDataTypeResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createDataTypeResult.right().value();
            log.debug("Failed to data type {} to graph. status is {}", dataTypeDefinition.getName(), operationStatus);
            BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("AddDataType", NodeTypeEnum.DataType.getName());
            return Either.right(operationStatus);
        }
        DataTypeData resultCTD = createDataTypeResult.left().value();
        List<PropertyDefinition> properties = dataTypeDefinition.getProperties();
        Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToDataType = addPropertiesToDataType(resultCTD.getUniqueId(), dataTypeDefinition.getModel(),
            properties);
        if (addPropertiesToDataType.isRight()) {
            log.debug("Failed add properties {} to data type {}", properties, dataTypeDefinition.getName());
            return Either.right(addPropertiesToDataType.right().value());
        }
        
        final Either<GraphRelation, JanusGraphOperationStatus> modelRelationship = addDataTypeToModel(dataTypeDefinition);
        if (modelRelationship.isRight()) {
            return Either.right(modelRelationship.right().value());
        }        
        
        String derivedFrom = dataTypeDefinition.getDerivedFromName();
        if (derivedFrom != null) {
            final Either<DataTypeDefinition, JanusGraphOperationStatus> derivedFromDataType = getDataTypeByNameValidForModel(derivedFrom, dataTypeDefinition.getModel());
            if (derivedFromDataType.isRight()) {
                return Either.right(derivedFromDataType.right().value());
            }
            
            log.debug("Before creating relation between data type {} to its parent {}", dtUniqueId, derivedFrom);
            UniqueIdData from = new UniqueIdData(NodeTypeEnum.DataType, dtUniqueId);
            final String deriveFromUid = derivedFromDataType.left().value().getUniqueId();
            UniqueIdData to = new UniqueIdData(NodeTypeEnum.DataType, deriveFromUid);
            Either<GraphRelation, JanusGraphOperationStatus> createRelation = janusGraphGenericDao
                .createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null);
            log.debug("After create relation between capability type {} to its parent {}. status is {}", dtUniqueId, derivedFrom, createRelation);
            if (createRelation.isRight()) {
                return Either.right(createRelation.right().value());
            }
        }
        return Either.left(createDataTypeResult.left().value());
    }
    
    private Either<GraphRelation, JanusGraphOperationStatus> addDataTypeToModel(final DataTypeDefinition dataTypeDefinition) {
      final String model = dataTypeDefinition.getModel();
      if (model == null) {
          return Either.left(null);
      }
      final GraphNode from = new UniqueIdData(NodeTypeEnum.Model, UniqueIdBuilder.buildModelUid(model));
      final GraphNode to = new UniqueIdData(NodeTypeEnum.DataType, dataTypeDefinition.getUniqueId());
      log.info("Connecting model {} to type {}", from, to);
      return janusGraphGenericDao.createRelation(from , to, GraphEdgeLabels.MODEL_ELEMENT, Collections.emptyMap());
  }

    private DataTypeData buildDataTypeData(DataTypeDefinition dataTypeDefinition, String ctUniqueId) {
        DataTypeData dataTypeData = new DataTypeData(dataTypeDefinition);
        dataTypeData.getDataTypeDataDefinition().setUniqueId(ctUniqueId);
        Long creationDate = dataTypeData.getDataTypeDataDefinition().getCreationTime();
        if (creationDate == null) {
            creationDate = System.currentTimeMillis();
        }
        dataTypeData.getDataTypeDataDefinition().setCreationTime(creationDate);
        dataTypeData.getDataTypeDataDefinition().setModificationTime(creationDate);
        return dataTypeData;
    }

    /**
     * add properties to capability type.
     * <p>
     * Per property, add a property node and associate it to the capability type
     *
     * @param uniqueId
     * @param properties
     * @return
     */
    private Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToDataType(final String uniqueId, final String modelName,
                                                                                                 final List<PropertyDefinition> properties) {
        Map<String, PropertyData> propertiesData = new HashMap<>();
        if (properties != null && !properties.isEmpty()) {
            for (PropertyDefinition propertyDefinition : properties) {
                String propertyName = propertyDefinition.getName();
                String propertyType = propertyDefinition.getType();
                Either<Boolean, JanusGraphOperationStatus> validPropertyType = isValidPropertyType(propertyType, modelName);
                if (validPropertyType.isRight()) {
                    log.debug("Data type {} contains invalid property type {}", uniqueId, propertyType);
                    return Either.right(validPropertyType.right().value());
                }
                Boolean isValid = validPropertyType.left().value();
                if (isValid == null || !isValid.booleanValue()) {
                    log.debug("Data type {} contains invalid property type {}", uniqueId, propertyType);
                    return Either.right(JanusGraphOperationStatus.INVALID_TYPE);
                }
                Either<PropertyData, JanusGraphOperationStatus> addPropertyToNodeType = this
                    .addPropertyToNodeType(propertyName, propertyDefinition, NodeTypeEnum.DataType, uniqueId);
                if (addPropertyToNodeType.isRight()) {
                    JanusGraphOperationStatus operationStatus = addPropertyToNodeType.right().value();
                    log.debug("Failed to associate data type {} to property {} in graph. status is {}", uniqueId, propertyName, operationStatus);
                    BeEcompErrorManager.getInstance()
                        .logInternalFlowError("AddPropertyToDataType", "Failed to associate property to data type. Status is " + operationStatus,
                            ErrorSeverity.ERROR);
                    return Either.right(operationStatus);
                }
                propertiesData.put(propertyName, addPropertyToNodeType.left().value());
            }
            DataTypeData dataTypeData = new DataTypeData();
            dataTypeData.getDataTypeDataDefinition().setUniqueId(uniqueId);
            long modificationTime = System.currentTimeMillis();
            dataTypeData.getDataTypeDataDefinition().setModificationTime(modificationTime);
            Either<DataTypeData, JanusGraphOperationStatus> updateNode = janusGraphGenericDao.updateNode(dataTypeData, DataTypeData.class);
            if (updateNode.isRight()) {
                JanusGraphOperationStatus operationStatus = updateNode.right().value();
                log.debug("Failed to update modification time data type {} from graph. status is {}", uniqueId, operationStatus);
                BeEcompErrorManager.getInstance()
                    .logInternalFlowError("AddPropertyToDataType", "Failed to fetch data type. Status is " + operationStatus, ErrorSeverity.ERROR);
                return Either.right(operationStatus);
            } else {
                log.debug("Update data type uid {}. Set modification time to {}", uniqueId, modificationTime);
            }
        }
        return Either.left(propertiesData);
    }
    
    public Either<DataTypeDefinition, JanusGraphOperationStatus> getDataTypeByNameValidForModel(final String name, final String modelName) {
        final Either<DataTypeData, JanusGraphOperationStatus> dataTypesRes = janusGraphGenericDao
            .getNode(GraphPropertiesDictionary.NAME.getProperty(), name, DataTypeData.class, modelName);
        if (dataTypesRes.isRight()) {
            final JanusGraphOperationStatus status = dataTypesRes.right().value();
            log.debug(DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS, name, status);
            return Either.right(status);
        }
        final DataTypeData dataType = dataTypesRes.left().value();
        final DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(dataType.getDataTypeDataDefinition());
        final JanusGraphOperationStatus propertiesStatus = fillProperties(dataTypeDefinition.getUniqueId(), dataTypeDefinition);
        if (propertiesStatus != JanusGraphOperationStatus.OK) {
            log.error(BUSINESS_PROCESS_ERROR, FAILED_TO_FETCH_PROPERTIES_OF_DATA_TYPE, dataTypeDefinition.getUniqueId());
            return Either.right(propertiesStatus);
        }
        final Either<ImmutablePair<DataTypeData, GraphEdge>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), dataTypeDefinition.getUniqueId(), GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.DataType,
              DataTypeData.class);
        log.debug(AFTER_RETRIEVING_DERIVED_FROM_NODE_OF_STATUS_IS, dataTypeDefinition.getUniqueId(), parentNode);
        if (parentNode.isRight()) {
            final JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
            if (janusGraphOperationStatus != JanusGraphOperationStatus.NOT_FOUND) {
                log.error(BUSINESS_PROCESS_ERROR, "Failed to find the parent data type of data type {}. status is {}", dataTypeDefinition.getUniqueId(), janusGraphOperationStatus);
                return Either.right(janusGraphOperationStatus);
            }
        } else {
            // derived from node was found
            final ImmutablePair<DataTypeData, GraphEdge> immutablePair = parentNode.left().value();
            final DataTypeData parentDataType = immutablePair.getKey();
            final Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeByUid = getDataTypeByUid(parentDataType.getUniqueId());
            if (dataTypeByUid.isRight()) {
                return Either.right(dataTypeByUid.right().value());
            }
            DataTypeDefinition parentDataTypeDefinition = dataTypeByUid.left().value();
            dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);
        }
        return Either.left(dataTypeDefinition);
  }

    /**
     * Build Data type object from graph by unique id
     *
     * @param uniqueId
     * @return
     */
    public Either<DataTypeDefinition, JanusGraphOperationStatus> getDataTypeByUid(String uniqueId) {
        Either<DataTypeDefinition, JanusGraphOperationStatus> result = null;
        Either<DataTypeData, JanusGraphOperationStatus> dataTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);
        if (dataTypesRes.isRight()) {
            JanusGraphOperationStatus status = dataTypesRes.right().value();
            log.debug(DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS, uniqueId, status);
            return Either.right(status);
        }
        DataTypeData ctData = dataTypesRes.left().value();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(ctData.getDataTypeDataDefinition());
        JanusGraphOperationStatus propertiesStatus = fillProperties(uniqueId, dataTypeDefinition);
        if (propertiesStatus != JanusGraphOperationStatus.OK) {
            log.error(FAILED_TO_FETCH_PROPERTIES_OF_DATA_TYPE, uniqueId);
            return Either.right(propertiesStatus);
        }
        Either<ImmutablePair<DataTypeData, GraphEdge>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.DataType,
                DataTypeData.class);
        log.debug(AFTER_RETRIEVING_DERIVED_FROM_NODE_OF_STATUS_IS, uniqueId, parentNode);
        if (parentNode.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
            if (janusGraphOperationStatus != JanusGraphOperationStatus.NOT_FOUND) {
                log.error("Failed to find the parent data type of data type {}. status is {}", uniqueId, janusGraphOperationStatus);
                result = Either.right(janusGraphOperationStatus);
                return result;
            }
        } else {
            // derived from node was found
            ImmutablePair<DataTypeData, GraphEdge> immutablePair = parentNode.left().value();
            DataTypeData parentCT = immutablePair.getKey();
            String parentUniqueId = parentCT.getUniqueId();
            Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeByUid = getDataTypeByUid(parentUniqueId);
            if (dataTypeByUid.isRight()) {
                return Either.right(dataTypeByUid.right().value());
            }
            DataTypeDefinition parentDataTypeDefinition = dataTypeByUid.left().value();
            dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);
        }
        result = Either.left(dataTypeDefinition);
        return result;
    }

    private JanusGraphOperationStatus fillProperties(String uniqueId, DataTypeDefinition dataTypeDefinition) {
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesOfNode = this
            .findPropertiesOfNode(NodeTypeEnum.DataType, uniqueId);
        if (findPropertiesOfNode.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = findPropertiesOfNode.right().value();
            log.debug("After looking for properties of vertex {}. status is {}", uniqueId, janusGraphOperationStatus);
            if (JanusGraphOperationStatus.NOT_FOUND.equals(janusGraphOperationStatus)) {
                return JanusGraphOperationStatus.OK;
            } else {
                return janusGraphOperationStatus;
            }
        } else {
            Map<String, PropertyDefinition> properties = findPropertiesOfNode.left().value();
            if (properties != null && !properties.isEmpty()) {
                List<PropertyDefinition> listOfProps = new ArrayList<>();
                for (Entry<String, PropertyDefinition> entry : properties.entrySet()) {
                    String propName = entry.getKey();
                    PropertyDefinition propertyDefinition = entry.getValue();
                    PropertyDefinition newPropertyDefinition = new PropertyDefinition(propertyDefinition);
                    newPropertyDefinition.setName(propName);
                    listOfProps.add(newPropertyDefinition);
                }
                dataTypeDefinition.setProperties(listOfProps);
            }
            return JanusGraphOperationStatus.OK;
        }
    }

    private Either<DataTypeDefinition, StorageOperationStatus> addDataType(DataTypeDefinition dataTypeDefinition, boolean inTransaction) {
        Either<DataTypeDefinition, StorageOperationStatus> result = null;
        try {
            Either<DataTypeData, JanusGraphOperationStatus> eitherStatus = addDataTypeToGraph(dataTypeDefinition);
            if (eitherStatus.isRight()) {
                log.debug("Failed to add data type {} to Graph. status is {}", dataTypeDefinition, eitherStatus.right().value().name());
                BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("AddDataType", "DataType");
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(eitherStatus.right().value()));
                return result;
            } else {
                DataTypeData capabilityTypeData = eitherStatus.left().value();
                DataTypeDefinition dataTypeDefResult = convertDTDataToDTDefinition(capabilityTypeData);
                log.debug("The returned CapabilityTypeDefinition is {}", dataTypeDefResult);
                result = Either.left(dataTypeDefResult);
                return result;
            }
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.error(GOING_TO_EXECUTE_ROLLBACK_ON_GRAPH);
                    janusGraphGenericDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_GRAPH);
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    @Override
    public Either<DataTypeDefinition, StorageOperationStatus> addDataType(DataTypeDefinition dataTypeDefinition) {
        return addDataType(dataTypeDefinition, true);
    }

    @Override
    public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(final String name, final String validForModel, final boolean inTransaction) {
        Either<DataTypeDefinition, StorageOperationStatus> result = null;
        try {
            Either<DataTypeDefinition, JanusGraphOperationStatus> ctResult = this.getDataTypeByNameValidForModel(name, validForModel);
            if (ctResult.isRight()) {
                JanusGraphOperationStatus status = ctResult.right().value();
                if (status != JanusGraphOperationStatus.NOT_FOUND) {
                    log.error("Failed to retrieve information on capability type {} status is {}", name, status);
                }
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(ctResult.right().value()));
                return result;
            }
            result = Either.left(ctResult.left().value());
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.error(GOING_TO_EXECUTE_ROLLBACK_ON_GRAPH);
                    janusGraphGenericDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_GRAPH);
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    @Override
    public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(final String name, final String validForModel) {
        return getDataTypeByName(name, validForModel, true);
    }
    
    public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByUidWithoutDerived(String uid, boolean inTransaction) {
        Either<DataTypeDefinition, StorageOperationStatus> result = null;
        try {
            Either<DataTypeDefinition, JanusGraphOperationStatus> ctResult = this.getDataTypeByUidWithoutDerivedDataTypes(uid);
            if (ctResult.isRight()) {
                JanusGraphOperationStatus status = ctResult.right().value();
                if (status != JanusGraphOperationStatus.NOT_FOUND) {
                  log.error(BUSINESS_PROCESS_ERROR, "Failed to retrieve information on data type {} status is {}", uid, status);
                }
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(ctResult.right().value()));
                return result;
            }
            result = Either.left(ctResult.left().value());
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.error(GOING_TO_EXECUTE_ROLLBACK_ON_GRAPH);
                    janusGraphGenericDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_GRAPH);
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    public Either<DataTypeDefinition, JanusGraphOperationStatus> getDataTypeByUidWithoutDerivedDataTypes(String uniqueId) {
        Either<DataTypeData, JanusGraphOperationStatus> dataTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);
        if (dataTypesRes.isRight()) {
            JanusGraphOperationStatus status = dataTypesRes.right().value();
            log.debug(DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS, uniqueId, status);
            return Either.right(status);
        }
        DataTypeData ctData = dataTypesRes.left().value();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(ctData.getDataTypeDataDefinition());
        JanusGraphOperationStatus propertiesStatus = fillProperties(uniqueId, dataTypeDefinition);
        if (propertiesStatus != JanusGraphOperationStatus.OK) {
            log.error(FAILED_TO_FETCH_PROPERTIES_OF_DATA_TYPE, uniqueId);
            return Either.right(propertiesStatus);
        }
        return Either.left(dataTypeDefinition);
    }

    /**
     * convert between graph Node object to Java object
     *
     * @param dataTypeData
     * @return
     */
    protected DataTypeDefinition convertDTDataToDTDefinition(DataTypeData dataTypeData) {
        log.debug("The object returned after create data type is {}", dataTypeData);
        return new DataTypeDefinition(dataTypeData.getDataTypeDataDefinition());
    }

    private Either<Boolean, JanusGraphOperationStatus> isValidPropertyType(String propertyType, final String modelName) {
        if (propertyType == null || propertyType.isEmpty()) {
            return Either.left(false);
        }
        ToscaPropertyType toscaPropertyType = ToscaPropertyType.isValidType(propertyType);
        if (toscaPropertyType == null) {
            return isDefinedInDataTypes(propertyType, modelName);
        } else {
            return Either.left(true);
        }
    }

    public Either<Boolean, JanusGraphOperationStatus> isDefinedInDataTypes(final String propertyType, final String modelName) {
        Either<DataTypeDefinition, JanusGraphOperationStatus> dataType = getDataTypeByNameValidForModel(propertyType, modelName);
        if (dataType.isRight()) {
            JanusGraphOperationStatus status = dataType.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                return Either.left(false);
            }
            return Either.right(status);
        }
        return Either.left(true);
    }

    public Either<Map<String, Map<String, DataTypeDefinition>>, JanusGraphOperationStatus> getAllDataTypes() {
        final Map<String, Map<String, DataTypeDefinition>> dataTypes = new HashMap<>();
        Either<Map<String, Map<String, DataTypeDefinition>>, JanusGraphOperationStatus> result = Either.left(dataTypes);
        final Map<String, DataTypeDefinition> allDataTypesFound = new HashMap<>();
        
        final Map<String, List<String>> dataTypeUidstoModels = dataTypeOperation.getAllDataTypeUidsToModels();

        if (dataTypeUidstoModels != null) {
            log.trace("Number of data types to load is {}", dataTypeUidstoModels.size());
            for (Map.Entry<String, List<String>> entry : dataTypeUidstoModels.entrySet()) {
                log.trace("Going to fetch data type with uid {}", entry.getKey());
                Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeByUid = this
                    .getAndAddDataTypeByUid(entry.getKey(), allDataTypesFound);
                if (dataTypeByUid.isRight()) {
                    JanusGraphOperationStatus status = dataTypeByUid.right().value();
                    if (status == JanusGraphOperationStatus.NOT_FOUND) {
                        status = JanusGraphOperationStatus.INVALID_ID;
                    }
                    return Either.right(status);
                }
                for (final String model: entry.getValue()) {
                    if (!dataTypes.containsKey(model)) {
                        dataTypes.put(model, new HashMap<String, DataTypeDefinition>());
                    }
                    DataTypeDefinition dataTypeDefinition = allDataTypesFound.get(entry.getKey());
                    dataTypes.get(model).put(dataTypeDefinition.getName(), dataTypeDefinition);
                }
            }
            
        }
        if (log.isTraceEnabled()) {
            if (result.isRight()) {
                log.trace("After fetching all data types {}", result);
            } else {
                Map<String, Map<String, DataTypeDefinition>> map = result.left().value();
                if (map != null) {
                    String types = map.keySet().stream().collect(Collectors.joining(",", "[", "]"));
                    log.trace("After fetching all data types {} ", types);
                }
            }
        }
        return result;
    }

    /**
     * Build Data type object from graph by unique id
     *
     * @param uniqueId
     * @return
     */
    private Either<DataTypeDefinition, JanusGraphOperationStatus> getAndAddDataTypeByUid(String uniqueId,
                                                                                         Map<String, DataTypeDefinition> allDataTypes) {
        Either<DataTypeDefinition, JanusGraphOperationStatus> result = null;
        if (allDataTypes.containsKey(uniqueId)) {
            return Either.left(allDataTypes.get(uniqueId));
        }
        Either<DataTypeData, JanusGraphOperationStatus> dataTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, DataTypeData.class);
        if (dataTypesRes.isRight()) {
            JanusGraphOperationStatus status = dataTypesRes.right().value();
            log.debug(DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS, uniqueId, status);
            return Either.right(status);
        }
        DataTypeData ctData = dataTypesRes.left().value();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition(ctData.getDataTypeDataDefinition());
        JanusGraphOperationStatus propertiesStatus = fillProperties(uniqueId, dataTypeDefinition);
        if (propertiesStatus != JanusGraphOperationStatus.OK) {
            log.error(FAILED_TO_FETCH_PROPERTIES_OF_DATA_TYPE, uniqueId);
            return Either.right(propertiesStatus);
        }
        allDataTypes.put(dataTypeDefinition.getUniqueId(), dataTypeDefinition);
        String derivedFrom = dataTypeDefinition.getDerivedFromName();
        if (allDataTypes.containsKey(derivedFrom)) {
            DataTypeDefinition parentDataTypeDefinition = allDataTypes.get(derivedFrom);
            dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);
            return Either.left(dataTypeDefinition);
        }
        Either<ImmutablePair<DataTypeData, GraphEdge>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.DataType,
                DataTypeData.class);
        log.debug(AFTER_RETRIEVING_DERIVED_FROM_NODE_OF_STATUS_IS, uniqueId, parentNode);
        if (parentNode.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
            if (janusGraphOperationStatus != JanusGraphOperationStatus.NOT_FOUND) {
                log.error("Failed to find the parent data type of data type {}. status is {}", uniqueId, janusGraphOperationStatus);
                result = Either.right(janusGraphOperationStatus);
                return result;
            }
        } else {
            // derived from node was found
            ImmutablePair<DataTypeData, GraphEdge> immutablePair = parentNode.left().value();
            DataTypeData parentCT = immutablePair.getKey();
            String parentUniqueId = parentCT.getUniqueId();
            Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeByUid = getDataTypeByUid(parentUniqueId);
            if (dataTypeByUid.isRight()) {
                return Either.right(dataTypeByUid.right().value());
            }
            DataTypeDefinition parentDataTypeDefinition = dataTypeByUid.left().value();
            dataTypeDefinition.setDerivedFrom(parentDataTypeDefinition);
            final var model = getModel(uniqueId);
            if (StringUtils.isNotEmpty(model)) {
                dataTypeDefinition.setModel(model);
            }
        }
        result = Either.left(dataTypeDefinition);
        return result;
    }

    private String getModel(final String uniqueId) {
        final Either<ImmutablePair<ModelData, GraphEdge>, JanusGraphOperationStatus> model = janusGraphGenericDao.getParentNode(
            UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.DataType), uniqueId, GraphEdgeLabels.MODEL_ELEMENT,
            NodeTypeEnum.Model, ModelData.class);
        return model.isLeft() ? model.left().value().getLeft().getName() : StringUtils.EMPTY;
    }

    public Either<String, JanusGraphOperationStatus> checkInnerType(PropertyDataDefinition propDataDef) {
        String propertyType = propDataDef.getType();
        ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
        return getInnerType(type, propDataDef::getSchema);
    }

    public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, boolean isValidate, String innerType,
                                                                  Map<String, DataTypeDefinition> dataTypes) {
        log.trace("Going to validate property value and its type. type = {}, value = {}", propertyType, value);
        final ToscaPropertyType type = getType(propertyType);
        if (isValidate) {
            if (type == null) {
                DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
                ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter
                    .validateAndUpdate(value, dataTypeDefinition, dataTypes);
                if (Boolean.FALSE.equals(validateResult.right)) {
                    log.debug(THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID, value, propertyType);
                    return Either.right(false);
                }
                JsonElement jsonElement = validateResult.left;
                String valueFromJsonElement = getValueFromJsonElement(jsonElement);
                return Either.left(valueFromJsonElement);
            }
            log.trace("before validating property type {}", propertyType);
            boolean isValidProperty = isValidValue(type, value, innerType, dataTypes);
            if (!isValidProperty) {
                log.debug(THE_VALUE_OF_PROPERTY_FROM_TYPE_IS_INVALID, value, type);
                return Either.right(false);
            }
        }
        Object convertedValue = value;
        if (!isEmptyValue(value) && isValidate) {
            PropertyValueConverter converter = type.getConverter();
            convertedValue = converter.convert(value, innerType, dataTypes);
        }
        return Either.left(convertedValue);
    }

    public Either<Object, Boolean> validateAndUpdatePropertyValue(String propertyType, String value, String innerType,
                                                                  Map<String, DataTypeDefinition> dataTypes) {
        return validateAndUpdatePropertyValue(propertyType, value, true, innerType, dataTypes);
    }

    public Either<Object, Boolean> validateAndUpdatePropertyValue(final Component containerComponent, final PropertyDataDefinition property,
                                                                  final Map<String, DataTypeDefinition> dataTypes) {
        if (property.isToscaFunction()) {
            toscaFunctionValidator.validate(property, containerComponent);
            property.setValue(property.getToscaFunction().getValue());
            return Either.left(property.getValue());
        }
        Either<String, JanusGraphOperationStatus> checkInnerType = checkInnerType(property);
        if (checkInnerType.isRight()) {
            return Either.right(false);
        }
        final String innerType = checkInnerType.left().value();
        return validateAndUpdatePropertyValue(property.getType(), property.getValue(), true, innerType, dataTypes);
    }

    public <T extends GraphNode> Either<List<PropertyDefinition>, StorageOperationStatus> getAllPropertiesRec(String uniqueId, NodeTypeEnum nodeType,
                                                                                                              Class<T> clazz) {
        return this.findPropertiesOfNode(nodeType, uniqueId).right().bind(this::handleNotFoundProperties).left()
            .bind(props -> getAllDerivedFromChainProperties(uniqueId, nodeType, clazz, props.values()));
    }

    private Either<Map<String, PropertyDefinition>, StorageOperationStatus> handleNotFoundProperties(
        JanusGraphOperationStatus janusGraphOperationStatus) {
        if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
            return Either.left(new HashMap<>());
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(janusGraphOperationStatus));
    }

    private <T extends GraphNode> Either<List<PropertyDefinition>, StorageOperationStatus> getAllDerivedFromChainProperties(String uniqueId,
                                                                                                                            NodeTypeEnum nodeType,
                                                                                                                            Class<T> clazz,
                                                                                                                            Collection<PropertyDefinition> nodeProps) {
        List<PropertyDefinition> accumulatedProps = new ArrayList<>(nodeProps);
        String currentNodeUid = uniqueId;
        Either<T, StorageOperationStatus> derivedFrom;
        while ((derivedFrom = derivedFromOperation.getDerivedFromChild(currentNodeUid, nodeType, clazz)).isLeft()) {
            currentNodeUid = derivedFrom.left().value().getUniqueId();
            JanusGraphOperationStatus janusGraphOperationStatus = fillPropertiesList(currentNodeUid, nodeType, accumulatedProps::addAll);
            if (janusGraphOperationStatus != JanusGraphOperationStatus.OK) {
                log.debug("failed to fetch properties for type {} with id {}", nodeType, currentNodeUid);
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(janusGraphOperationStatus));
            }
        }
        StorageOperationStatus getDerivedResult = derivedFrom.right().value();
        return isReachedEndOfDerivedFromChain(getDerivedResult) ? Either.left(accumulatedProps) : Either.right(getDerivedResult);
    }

    private boolean isReachedEndOfDerivedFromChain(StorageOperationStatus getDerivedResult) {
        return getDerivedResult == StorageOperationStatus.NOT_FOUND;
    }

    /*
     * @Override public PropertyOperation getPropertyOperation() { return this; }
     */
    public JanusGraphOperationStatus fillPropertiesList(String uniqueId, NodeTypeEnum nodeType, Consumer<List<PropertyDefinition>> propertySetter) {
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesRes = findPropertiesifExist(uniqueId, nodeType);
        if (findPropertiesRes.isRight()) {
            return findPropertiesRes.right().value();
        }
        Map<String, PropertyDefinition> properties = findPropertiesRes.left().value();
        if (properties != null) {
            List<PropertyDefinition> propertiesAsList = properties.entrySet().stream().map(Entry::getValue).collect(Collectors.toList());
            propertySetter.accept(propertiesAsList);
        }
        return JanusGraphOperationStatus.OK;
    }

    Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesifExist(String uniqueId, NodeTypeEnum nodeType) {
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> findPropertiesOfNode = this.findPropertiesOfNode(nodeType, uniqueId);
        if (findPropertiesOfNode.isRight()) {
            log.debug("After looking for properties of vertex {}. status is {}", uniqueId, findPropertiesOfNode.right().value());
            if (findPropertiesOfNode.right().value() == JanusGraphOperationStatus.NOT_FOUND) {
                return Either.left(Maps.newHashMap());
            }
            return findPropertiesOfNode;
        }
        return findPropertiesOfNode;
    }

    /**
     * add properties to element type.
     * <p>
     * Per property, add a property node and associate it to the element type
     *
     * @param uniqueId
     * @param propertiesMap
     * @return
     */
    protected Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToElementType(String uniqueId, NodeTypeEnum nodeType,
                                                                                                      Map<String, PropertyDefinition> propertiesMap) {
        Map<String, PropertyData> propertiesData = new HashMap<>();
        if (propertiesMap != null) {
            for (Entry<String, PropertyDefinition> propertyDefinitionEntry : propertiesMap.entrySet()) {
                String propertyName = propertyDefinitionEntry.getKey();
                Either<PropertyData, JanusGraphOperationStatus> addPropertyToNodeType = this
                    .addPropertyToNodeType(propertyName, propertyDefinitionEntry.getValue(), nodeType, uniqueId);
                if (addPropertyToNodeType.isRight()) {
                    JanusGraphOperationStatus operationStatus = addPropertyToNodeType.right().value();
                    log.error("Failed to associate {} {} to property {} in graph. status is {}", nodeType.getName(), uniqueId, propertyName,
                        operationStatus);
                    return Either.right(operationStatus);
                }
                propertiesData.put(propertyName, addPropertyToNodeType.left().value());
            }
        }
        return Either.left(propertiesData);
    }

    public Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToElementType(String uniqueId, NodeTypeEnum elementType,
                                                                                                   List<PropertyDefinition> properties) {
        Map<String, PropertyDefinition> propMap;
        if (properties == null) {
            propMap = null;
        } else {
            propMap = properties.stream().collect(Collectors.toMap(PropertyDataDefinition::getName, propDef -> propDef));
        }
        return addPropertiesToElementType(uniqueId, elementType, propMap);
    }

    @Override
    public Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition,
                                                                             DataTypeDefinition oldDataTypeDefinition) {
        return updateDataType(newDataTypeDefinition, oldDataTypeDefinition, true);
    }

    private Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition,
                                                                              DataTypeDefinition oldDataTypeDefinition, boolean inTransaction) {
        Either<DataTypeDefinition, StorageOperationStatus> result = null;
        try {
            List<PropertyDefinition> newProperties = newDataTypeDefinition.getProperties();
            List<PropertyDefinition> oldProperties = oldDataTypeDefinition.getProperties();
            String newDerivedFromName = newDataTypeDefinition.getDerivedFromName();
            String oldDerivedFromName = oldDataTypeDefinition.getDerivedFromName();
            String dataTypeName = newDataTypeDefinition.getName();
            List<PropertyDefinition> propertiesToAdd = new ArrayList<>();
            if (isPropertyOmitted(newProperties, oldProperties, dataTypeName) || isPropertyTypeChanged(dataTypeName, newProperties, oldProperties,
                propertiesToAdd) || isDerivedFromNameChanged(dataTypeName, newDerivedFromName, oldDerivedFromName)) {
                log.debug("The new data type {} is invalid.", dataTypeName);
                result = Either.right(StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY);
                return result;
            }
            if (propertiesToAdd == null || propertiesToAdd.isEmpty()) {
                log.debug("No new properties has been defined in the new data type {}", newDataTypeDefinition);
                result = Either.right(StorageOperationStatus.OK);
                return result;
            }
            Map<String, String> newDescriptions = getPropertyDescriptionsToUpdate(oldProperties, newProperties);
            if (MapUtils.isNotEmpty(newDescriptions)) {
                JanusGraphOperationStatus updatePropertiesStatus = updateDataTypePropertyDescriptions(oldDataTypeDefinition.getUniqueId(),
                    newDescriptions);
                if (updatePropertiesStatus != JanusGraphOperationStatus.OK) {
                    log.debug("#updateDataType - Failed to update the descriptions of the properties of the data type {}. Status is {}",
                        oldDataTypeDefinition, updatePropertiesStatus);
                    BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError(UPDATE_DATA_TYPE, PROPERTY);
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updatePropertiesStatus));
                    return result;
                }
            }
            Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToDataType = addPropertiesToDataType(
                oldDataTypeDefinition.getUniqueId(), oldDataTypeDefinition.getModel(), propertiesToAdd);
            if (addPropertiesToDataType.isRight()) {
                log.debug("Failed to update data type {} to Graph. Status is {}", oldDataTypeDefinition,
                    addPropertiesToDataType.right().value().name());
                BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError(UPDATE_DATA_TYPE, PROPERTY);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(addPropertiesToDataType.right().value()));
                return result;
            } else {
                Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeByUid = this.getDataTypeByUid(oldDataTypeDefinition.getUniqueId());
                if (dataTypeByUid.isRight()) {
                    JanusGraphOperationStatus status = addPropertiesToDataType.right().value();
                    log.debug("Failed to get data type {} after update. Status is {}", oldDataTypeDefinition.getUniqueId(), status.name());
                    BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError(UPDATE_DATA_TYPE, PROPERTY, status.name());
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                } else {
                    result = Either.left(dataTypeByUid.left().value());
                }
            }
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.error(GOING_TO_EXECUTE_ROLLBACK_ON_GRAPH);
                    janusGraphGenericDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_GRAPH);
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    private boolean isPropertyTypeChanged(String dataTypeName, List<PropertyDefinition> newProperties, List<PropertyDefinition> oldProperties,
                                          List<PropertyDefinition> outputPropertiesToAdd) {
        if (newProperties != null && oldProperties != null) {
            Map<String, PropertyDefinition> newPropsMapper = newProperties.stream()
                .collect(Collectors.toMap(PropertyDataDefinition::getName, p -> p));
            Map<String, PropertyDefinition> oldPropsMapper = oldProperties.stream()
                .collect(Collectors.toMap(PropertyDataDefinition::getName, p -> p));
            for (Entry<String, PropertyDefinition> newPropertyEntry : newPropsMapper.entrySet()) {
                String propName = newPropertyEntry.getKey();
                PropertyDefinition propDef = newPropertyEntry.getValue();
                PropertyDefinition oldPropertyDefinition = oldPropsMapper.get(propName);
                if (oldPropertyDefinition == null) {
                    log.debug("New property {} received in the data type {}", propName, dataTypeName);
                    outputPropertiesToAdd.add(propDef);
                    continue;
                }
                String oldType = oldPropertyDefinition.getType();
                String oldEntryType = getEntryType(oldPropertyDefinition);
                String newType = propDef.getType();
                String newEntryType = getEntryType(propDef);
                if (!oldType.equals(newType)) {
                    log.debug("Existing property {} in data type {} has a differnet type {} than the new one {}", propName, dataTypeName, oldType,
                        newType);
                    return true;
                }
                if (!equalsEntryTypes(oldEntryType, newEntryType)) {
                    log.debug("Existing property {} in data type {} has a differnet entry type {} than the new one {}", propName, dataTypeName,
                        oldEntryType, newEntryType);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean equalsEntryTypes(String oldEntryType, String newEntryType) {
        if (oldEntryType == null && newEntryType == null) {
            return true;
        } else if (oldEntryType != null && newEntryType != null) {
            return oldEntryType.equals(newEntryType);
        } else {
            return false;
        }
    }

    private String getEntryType(PropertyDefinition oldPropertyDefinition) {
        String entryType = null;
        SchemaDefinition schema = oldPropertyDefinition.getSchema();
        if (schema != null) {
            PropertyDataDefinition schemaProperty = schema.getProperty();
            if (schemaProperty != null) {
                entryType = schemaProperty.getType();
            }
        }
        return entryType;
    }

    private boolean isPropertyOmitted(List<PropertyDefinition> newProperties, List<PropertyDefinition> oldProperties, String dataTypeName) {
        boolean isValid = validateChangeInCaseOfEmptyProperties(newProperties, oldProperties, dataTypeName);
        if (!isValid) {
            log.debug("At least one property is missing in the new data type {}", dataTypeName);
            return false;
        }
        if (newProperties != null && oldProperties != null) {
            List<String> newProps = newProperties.stream().map(PropertyDataDefinition::getName).collect(Collectors.toList());
            List<String> oldProps = oldProperties.stream().map(PropertyDataDefinition::getName).collect(Collectors.toList());
            if (!newProps.containsAll(oldProps)) {
                StringJoiner joiner = new StringJoiner(",", "[", "]");
                newProps.forEach(joiner::add);
                log.debug("Properties {} in data type {} are missing, but they already defined in the existing data type", joiner.toString(),
                    dataTypeName);
                return true;
            }
        }
        return false;
    }

    private boolean validateChangeInCaseOfEmptyProperties(List<PropertyDefinition> newProperties, List<PropertyDefinition> oldProperties,
                                                          String dataTypeName) {
        if (newProperties != null) {
            if (newProperties.isEmpty()) {
                newProperties = null;
            }
        }
        if (oldProperties != null) {
            if (oldProperties.isEmpty()) {
                oldProperties = null;
            }
        }
        if ((newProperties == null && oldProperties == null) || (newProperties != null && oldProperties != null)) {
            return true;
        }
        return false;
    }

    private boolean isDerivedFromNameChanged(String dataTypeName, String newDerivedFromName, String oldDerivedFromName) {
        if (newDerivedFromName != null) {
            boolean isEqual = newDerivedFromName.equals(oldDerivedFromName);
            if (!isEqual) {
                log.debug("The new datatype {} derived from another data type {} than the existing one {}", dataTypeName, newDerivedFromName,
                    oldDerivedFromName);
            }
            return !isEqual;
        } else if (oldDerivedFromName == null) {
            return false;
        } else {// new=null, old != null
            log.debug("The new datatype {} derived from another data type {} than the existing one {}", dataTypeName, newDerivedFromName,
                oldDerivedFromName);
            return true;
        }
    }

    /**
     * @param instanceId
     * @param nodeType
     * @return
     */
    public Either<Integer, StorageOperationStatus> increaseAndGetObjInstancePropertyCounter(String instanceId, NodeTypeEnum nodeType) {
        Either<JanusGraph, JanusGraphOperationStatus> graphResult = janusGraphGenericDao.getGraph();
        if (graphResult.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(graphResult.right().value()));
        }
        Either<JanusGraphVertex, JanusGraphOperationStatus> vertexService = janusGraphGenericDao
            .getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), instanceId);
        if (vertexService.isRight()) {
            log.debug("failed to fetch vertex of resource instance for id = {}", instanceId);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(vertexService.right().value()));
        }
        Vertex vertex = vertexService.left().value();
        VertexProperty<Object> vertexProperty = vertex.property(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty());
        Integer counter = 0;
        if (vertexProperty.isPresent() && vertexProperty.value() != null) {
            counter = (Integer) vertexProperty.value();
        }
        counter++;
        vertex.property(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty(), counter);
        return Either.left(counter);
    }

    public Either<List<PropertyDefinition>, JanusGraphOperationStatus> validatePropertiesUniqueness(
        Map<String, PropertyDefinition> inheritedProperties, List<PropertyDefinition> properties) {
        Either<List<PropertyDefinition>, JanusGraphOperationStatus> result = Either.left(properties);
        for (PropertyDefinition property : properties) {
            JanusGraphOperationStatus status = validatePropertyUniqueness(inheritedProperties, property);
            if (status != JanusGraphOperationStatus.OK) {
                result = Either.right(status);
                break;
            }
        }
        return result;
    }

    /**
     * Validates uniqueness of examined property by comparing it with properties in propertiesOfType and updates if need type and inner type of the
     * property.
     */
    private JanusGraphOperationStatus validatePropertyUniqueness(Map<String, PropertyDefinition> inheritedProperties, PropertyDefinition property) {
        String propertyName = property.getName();
        String propertyType = property.getType();
        JanusGraphOperationStatus result = JanusGraphOperationStatus.OK;
        if (inheritedProperties.containsKey(propertyName)) {
            PropertyDefinition defaultProperty = inheritedProperties.get(propertyName);
            if (typesMismatch(propertyType, defaultProperty.getType())) {
                log.error("#validatePropertyUniqueness - Property with name {} and different type already exists.", propertyName);
                result = JanusGraphOperationStatus.PROPERTY_NAME_ALREADY_EXISTS;
            } else {
                property.setType(defaultProperty.getType());
                String innerType = defaultProperty.getSchemaType();
                PropertyDataDefinition schemaProperty = property.getSchemaProperty();
                if (schemaProperty != null) {
                    schemaProperty.setType(innerType);
                }
            }
        }
        return result;
    }

    private boolean typesMismatch(String type1, String type2) {
        return type1 != null && type2 != null && !type2.equals(type1);
    }

    public <T extends GraphNode> Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> getAllTypePropertiesFromAllDerivedFrom(
        String nextParentUid, NodeTypeEnum nodeType, Class<T> clazz) {
        Map<String, PropertyDefinition> allProperies = new HashMap<>();
        return getTypePropertiesFromDerivedFromRecursively(nextParentUid, allProperies, nodeType, clazz);
    }

    private <T extends GraphNode> Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> getTypePropertiesFromDerivedFromRecursively(
        String nextParentUid, Map<String, PropertyDefinition> allProperies, NodeTypeEnum nodeType, Class<T> clazz) {
        JanusGraphOperationStatus error;
        Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), nextParentUid, GraphEdgeLabels.DERIVED_FROM, nodeType, clazz);
        if (childrenNodes.isRight()) {
            if (childrenNodes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                error = childrenNodes.right().value();
                log.debug("#getTypePropertiesFromDerivedFromRecursively - Couldn't fetch derived from node with UID {}, error: {}", nextParentUid,
                    error);
                return Either.right(error);
            } else {
                log.debug("#getTypePropertiesFromDerivedFromRecursively - Derived from node is not found with UID {} - this is OK for root.",
                    nextParentUid);
                return Either.left(allProperies);
            }
        } else {
            Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> allPropertiesOfTypeRes = findPropertiesOfNode(nodeType, nextParentUid);
            if (allPropertiesOfTypeRes.isRight() && !allPropertiesOfTypeRes.right().value().equals(JanusGraphOperationStatus.NOT_FOUND)) {
                error = allPropertiesOfTypeRes.right().value();
                log.error(
                    "#getTypePropertiesFromDerivedFromRecursively - Failed to retrieve properties for node with UID {} from graph. status is {}",
                    nextParentUid, error);
                return Either.right(error);
            } else if (allPropertiesOfTypeRes.isLeft()) {
                if (allProperies.isEmpty()) {
                    allProperies.putAll(allPropertiesOfTypeRes.left().value());
                } else {
                    allProperies.putAll(allPropertiesOfTypeRes.left().value().entrySet().stream().filter(e -> !allProperies.containsKey(e.getKey()))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
                }
            }
            return getTypePropertiesFromDerivedFromRecursively(childrenNodes.left().value().get(0).getLeft().getUniqueId(), allProperies, nodeType,
                clazz);
        }
    }

    private JanusGraphOperationStatus updateDataTypePropertyDescriptions(String uniqueId, Map<String, String> newDescriptions) {
        if (MapUtils.isNotEmpty(newDescriptions)) {
            Either<List<ImmutablePair<JanusGraphVertex, Edge>>, JanusGraphOperationStatus> getDataTypePropertiesRes = janusGraphGenericDao
                .getChildrenVertecies(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueId, GraphEdgeLabels.PROPERTY);
            if (getDataTypePropertiesRes.isRight()) {
                log.debug("#updateDataTypePropertiesDescriptions - Failed to fetch the property verticies of the Data type {} ", uniqueId);
                return getDataTypePropertiesRes.right().value();
            }
            getDataTypePropertiesRes.left().value().stream().filter(pair -> newDescriptions.containsKey(getPropertyNameFromEdge(pair)))
                .forEach(pair -> setNewDescriptionToVertex(newDescriptions.get(getPropertyNameFromEdge(pair)), pair));
        }
        return JanusGraphOperationStatus.OK;
    }

    private JanusGraphVertexProperty<String> setNewDescriptionToVertex(String newDescription, ImmutablePair<JanusGraphVertex, Edge> pair) {
        return pair.getLeft().property(GraphPropertiesDictionary.DESCRIPTION.getProperty(), newDescription);
    }

    private String getPropertyNameFromEdge(ImmutablePair<JanusGraphVertex, Edge> pair) {
        return (String) pair.getRight().property(GraphPropertiesDictionary.NAME.getProperty()).value();
    }

    private Map<String, String> getPropertyDescriptionsToUpdate(List<PropertyDefinition> oldProperties, List<PropertyDefinition> newProperties) {
        Map<String, PropertyDefinition> newPropertiesMap = newProperties.stream().collect(Collectors.toMap(PropertyDefinition::getName, p -> p));
        return oldProperties.stream()
            .filter(p -> newPropertiesMap.containsKey(p.getName()) && !descriptionsEqual(p, newPropertiesMap.get(p.getName())))
            .collect(Collectors.toMap(PropertyDefinition::getName, p -> newPropertiesMap.get(p.getName()).getDescription()));
    }

    private boolean descriptionsEqual(PropertyDefinition property, PropertyDefinition otherProperty) {
        if (StringUtils.isEmpty(property.getDescription()) && StringUtils.isEmpty(otherProperty.getDescription())) {
            return true;
        }
        if (StringUtils.isNotEmpty(property.getDescription()) && StringUtils.isEmpty(otherProperty.getDescription())) {
            return false;
        }
        if (StringUtils.isEmpty(property.getDescription()) && StringUtils.isNotEmpty(otherProperty.getDescription())) {
            return false;
        }
        return property.getDescription().equals(otherProperty.getDescription());
    }

    public static class PropertyConstraintSerialiser implements JsonSerializer<PropertyConstraint> {

        @Override
        public JsonElement serialize(PropertyConstraint src, Type typeOfSrc, JsonSerializationContext context) {
            JsonParser parser = new JsonParser();
            JsonObject result = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            if (src instanceof InRangeConstraint) {
                InRangeConstraint rangeConstraint = (InRangeConstraint) src;
                jsonArray.add(parser.parse(rangeConstraint.getRangeMinValue()));
                jsonArray.add(parser.parse(rangeConstraint.getRangeMaxValue()));
                result.add("inRange", jsonArray);
            } else if (src instanceof GreaterThanConstraint) {
                GreaterThanConstraint greaterThanConstraint = (GreaterThanConstraint) src;
                jsonArray.add(parser.parse(greaterThanConstraint.getGreaterThan()));
                result.add("greaterThan", jsonArray);
            } else if (src instanceof LessOrEqualConstraint) {
                LessOrEqualConstraint lessOrEqualConstraint = (LessOrEqualConstraint) src;
                jsonArray.add(parser.parse(lessOrEqualConstraint.getLessOrEqual()));
                result.add("lessOrEqual", jsonArray);
            } else {
                log.warn("PropertyConstraint {} is not supported. Ignored.", src.getClass().getName());
            }
            return result;
        }
    }

    public static class PropertyConstraintDeserialiser implements JsonDeserializer<PropertyConstraint> {

        private static final String THE_VALUE_OF_GREATER_THAN_CONSTRAINT_IS_NULL = "The value of GreaterThanConstraint is null";

        @Override
        public PropertyConstraint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            PropertyConstraint propertyConstraint = null;
            Set<Entry<String, JsonElement>> set = json.getAsJsonObject().entrySet();
            if (set.size() == 1) {
                Entry<String, JsonElement> element = set.iterator().next();
                String key = element.getKey();
                JsonElement value = element.getValue();
                ConstraintType constraintType = ConstraintType.getByType(key);
                if (constraintType == null) {
                    log.warn("ConstraintType was not found for constraint name:{}", key);
                } else {
                    switch (constraintType) {
                        case IN_RANGE:
                            if (value != null) {
                                if (value instanceof JsonArray) {
                                    JsonArray rangeArray = (JsonArray) value;
                                    if (rangeArray.size() != 2) {
                                        log.error("The range constraint content is invalid. value = {}", value);
                                    } else {
                                        InRangeConstraint rangeConstraint = new InRangeConstraint();
                                        String minValue = rangeArray.get(0).getAsString();
                                        String maxValue;
                                        JsonElement maxElement = rangeArray.get(1);
                                        if (maxElement.isJsonNull()) {
                                            maxValue = String.valueOf(maxElement.getAsJsonNull());
                                        } else {
                                            maxValue = maxElement.getAsString();
                                        }
                                        rangeConstraint.setRangeMinValue(minValue);
                                        rangeConstraint.setRangeMaxValue(maxValue);
                                        propertyConstraint = rangeConstraint;
                                    }
                                }
                            } else {
                                log.warn(THE_VALUE_OF_GREATER_THAN_CONSTRAINT_IS_NULL);
                            }
                            break;
                        case GREATER_THAN:
                            if (value != null) {
                                String asString = value.getAsString();
                                log.debug("Before adding value to GreaterThanConstraint object. value = {}", asString);
                                propertyConstraint = new GreaterThanConstraint(asString);
                                break;
                            } else {
                                log.warn(THE_VALUE_OF_GREATER_THAN_CONSTRAINT_IS_NULL);
                            }
                            break;
                        case LESS_THAN:
                            if (value != null) {
                                String asString = value.getAsString();
                                log.debug("Before adding value to LessThanConstraint object. value = {}", asString);
                                propertyConstraint = new LessThanConstraint(asString);
                                break;
                            } else {
                                log.warn("The value of LessThanConstraint is null");
                            }
                            break;
                        case GREATER_OR_EQUAL:
                            if (value != null) {
                                String asString = value.getAsString();
                                log.debug("Before adding value to GreaterThanConstraint object. value = {}", asString);
                                propertyConstraint = new GreaterOrEqualConstraint(asString);
                                break;
                            } else {
                                log.warn("The value of GreaterOrEqualConstraint is null");
                            }
                            break;
                        case LESS_OR_EQUAL:
                            if (value != null) {
                                String asString = value.getAsString();
                                log.debug("Before adding value to LessOrEqualConstraint object. value = {}", asString);
                                propertyConstraint = new LessOrEqualConstraint(asString);
                            } else {
                                log.warn(THE_VALUE_OF_GREATER_THAN_CONSTRAINT_IS_NULL);
                            }
                            break;
                        case VALID_VALUES:
                            if (value != null) {
                                JsonArray rangeArray = (JsonArray) value;
                                if (rangeArray.size() == 0) {
                                    log.error("The valid values constraint content is invalid. value = {}", value);
                                } else {
                                    ValidValuesConstraint vvConstraint = new ValidValuesConstraint();
                                    List<String> validValues = new ArrayList<>();
                                    for (JsonElement jsonElement : rangeArray) {
                                        String item = jsonElement.getAsString();
                                        validValues.add(item);
                                    }
                                    vvConstraint.setValidValues(validValues);
                                    propertyConstraint = vvConstraint;
                                }
                            }
                            break;
                        case MIN_LENGTH:
                            if (value != null) {
                                int asInt = value.getAsInt();
                                log.debug("Before adding value to Min Length object. value = {}", asInt);
                                propertyConstraint = new MinLengthConstraint(asInt);
                                break;
                            } else {
                                log.warn("The value of MinLengthConstraint is null");
                            }
                            break;
                        default:
                            log.warn("Key {} is not supported. Ignored.", key);
                    }
                }
            }
            return propertyConstraint;
        }
    }

    public static class PropertyConstraintJacksonDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<PropertyConstraint> {

        @Override
        public PropertyConstraint deserialize(com.fasterxml.jackson.core.JsonParser json, DeserializationContext context) throws IOException {
            ObjectCodec oc = json.getCodec();
            JsonNode node = oc.readTree(json);
            return null;
        }
    }

}
