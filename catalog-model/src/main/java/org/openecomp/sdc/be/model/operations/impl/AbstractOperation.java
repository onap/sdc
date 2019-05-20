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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOperation {

    private static final Logger log = Logger.getLogger(AbstractOperation.class.getName());

    @Autowired
    protected HealingJanusGraphGenericDao janusGraphGenericDao;

    public static final String EMPTY_VALUE = null;

    protected Gson gson = new Gson();

    @Autowired
    protected ApplicationDataTypeCache applicationDataTypeCache;

    protected DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

    protected <SomeData extends GraphNode, SomeDefenition> Either<SomeData, JanusGraphOperationStatus> addDefinitionToNodeType(SomeDefenition someDefinition, NodeTypeEnum nodeType, String nodeUniqueId, final GraphEdgeLabels edgeType,
                                                                                                                               Supplier<SomeData> dataBuilder, Supplier<String> defNameGenerator) {
        String defName = defNameGenerator.get();
        log.debug("Got {} {}", defName, someDefinition);

        SomeData someData = dataBuilder.get();

        log.debug("Before adding {} to graph. data = {}", defName, someData);

        @SuppressWarnings("unchecked")
        Either<SomeData, JanusGraphOperationStatus> eitherSomeData = janusGraphGenericDao
            .createNode(someData, (Class<SomeData>) someData.getClass());

        log.debug("After adding {} to graph. status is = {}", defName, eitherSomeData);

        if (eitherSomeData.isRight()) {
            JanusGraphOperationStatus operationStatus = eitherSomeData.right().value();
            log.error("Failed to add {}  to graph. status is {}", defName, operationStatus);
            return Either.right(operationStatus);
        }
        UniqueIdData uniqueIdData = new UniqueIdData(nodeType, nodeUniqueId);
        log.debug("Before associating {} to {}.", uniqueIdData, defName);

        Either<GraphRelation, JanusGraphOperationStatus> eitherRelations = janusGraphGenericDao
            .createRelation(uniqueIdData, eitherSomeData.left().value(), edgeType, null);
        if (eitherRelations.isRight()) {
            JanusGraphOperationStatus operationStatus = eitherRelations.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("AddDefinitionToNodeType", "Failed to associate" + nodeType.getName() + " " + nodeUniqueId + "to " + defName + "in graph. status is " + operationStatus, ErrorSeverity.ERROR);
            return Either.right(operationStatus);
        }
        return Either.left(eitherSomeData.left().value());
    }

    protected <SomeData extends GraphNode, SomeDefenition> JanusGraphOperationStatus addDefinitionToNodeType(JanusGraphVertex vertex, SomeDefenition someDefinition, NodeTypeEnum nodeType, String nodeUniqueId, final GraphEdgeLabels edgeType,
                                                                                                             Supplier<SomeData> dataBuilder, Supplier<String> defNameGenerator) {
        String defName = defNameGenerator.get();
        log.debug("Got {} {}", defName, someDefinition);

        SomeData someData = dataBuilder.get();

        log.debug("Before adding {} to graph. data = {}", defName, someData);

        @SuppressWarnings("unchecked")
        Either<JanusGraphVertex, JanusGraphOperationStatus> eitherSomeData = janusGraphGenericDao.createNode(someData);

        log.debug("After adding {} to graph. status is = {}", defName, eitherSomeData);

        if (eitherSomeData.isRight()) {
            JanusGraphOperationStatus operationStatus = eitherSomeData.right().value();
            log.error("Failed to add {}  to graph. status is {}", defName, operationStatus);
            return operationStatus;
        }

        JanusGraphOperationStatus
            relations = janusGraphGenericDao
            .createEdge(vertex, eitherSomeData.left().value(), edgeType, null);
        if (!relations.equals(JanusGraphOperationStatus.OK)) {
            BeEcompErrorManager.getInstance().logInternalFlowError("AddDefinitionToNodeType", "Failed to associate" + nodeType.getName() + " " + nodeUniqueId + "to " + defName + "in graph. status is " + relations, ErrorSeverity.ERROR);
            return relations;
        }
        return relations;
    }

    interface NodeElementFetcher<ElementDefinition> {
        JanusGraphOperationStatus findAllNodeElements(String nodeId, List<ElementDefinition> listTofill);
    }

    public <ElementDefinition> JanusGraphOperationStatus findAllResourceElementsDefinitionRecursively(String resourceId, List<ElementDefinition> elements, NodeElementFetcher<ElementDefinition> singleNodeFetcher) {

        if (log.isTraceEnabled())
            log.trace("Going to fetch elements under resource {}", resourceId);
        JanusGraphOperationStatus
            resourceAttributesStatus = singleNodeFetcher.findAllNodeElements(resourceId, elements);

        if (resourceAttributesStatus != JanusGraphOperationStatus.OK) {
            return resourceAttributesStatus;
        }

        Either<ImmutablePair<ResourceMetadataData, GraphEdge>, JanusGraphOperationStatus> parentNodes = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
                ResourceMetadataData.class);

        if (parentNodes.isRight()) {
            JanusGraphOperationStatus parentNodesStatus = parentNodes.right().value();
            if (parentNodesStatus != JanusGraphOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find parent elements of resource " + resourceId + ". status is " + parentNodesStatus, ErrorSeverity.ERROR);
                return parentNodesStatus;
            }
        }

        if (parentNodes.isLeft()) {
            ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
            String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
            JanusGraphOperationStatus addParentIntStatus = findAllResourceElementsDefinitionRecursively(parentUniqueId, elements, singleNodeFetcher);

            if (addParentIntStatus != JanusGraphOperationStatus.OK) {
                BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find all resource elements of resource " + parentUniqueId, ErrorSeverity.ERROR);

                return addParentIntStatus;
            }
        }
        return JanusGraphOperationStatus.OK;
    }

    protected <T, TStatus> void handleTransactionCommitRollback(boolean inTransaction, Either<T, TStatus> result) {
        if (!inTransaction) {
            if (result == null || result.isRight()) {
                log.error("Going to execute rollback on graph.");
                janusGraphGenericDao.rollback();
            } else {
                log.debug("Going to execute commit on graph.");
                janusGraphGenericDao.commit();
            }
        }
    }


    /**
     * @param propertyDefinition
     * @return
     */

    protected StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

        log.trace("Going to validate property type and value. {}", propertyDefinition);

        String propertyType = propertyDefinition.getType();
        String value = propertyDefinition.getDefaultValue();

        ToscaPropertyType type = getType(propertyType);

        if (type == null) {

            DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
            if (dataTypeDefinition == null) {
                log.debug("The type {}  of property cannot be found.", propertyType);
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
            log.info("The value {} of property from type {} is invalid", value, type);
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

    protected ToscaPropertyType getType(String propertyType) {

        return ToscaPropertyType.isValidType(propertyType);

    }

    protected boolean isValidValue(ToscaPropertyType type, String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (isEmptyValue(value)) {
            return true;
        }

        PropertyTypeValidator validator = type.getValidator();

        return validator.isValid(value, innerType, dataTypes);
    }

    public boolean isEmptyValue(String value) {
        return value == null;
    }

    public boolean isNullParam(String value) {
        return value == null;
    }

    protected StorageOperationStatus validateAndUpdateComplexValue(IComplexDefaultValue propertyDefinition, String propertyType,

            String value, DataTypeDefinition dataTypeDefinition, Map<String, DataTypeDefinition> dataTypes) {

        ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);

        if (!validateResult.right.booleanValue()) {
            log.debug("The value {} of property from type {} is invalid", propertyType, propertyType);
            return StorageOperationStatus.INVALID_VALUE;
        }

        JsonElement jsonElement = validateResult.left;

        log.trace("Going to update value in property definition {} {}" , propertyDefinition.getName() , (jsonElement != null ? jsonElement.toString() : null));

        updateValue(propertyDefinition, jsonElement);

        return StorageOperationStatus.OK;
    }

    protected void updateValue(IComplexDefaultValue propertyDefinition, JsonElement jsonElement) {

        propertyDefinition.setDefaultValue(getValueFromJsonElement(jsonElement));

    }

    protected String getValueFromJsonElement(JsonElement jsonElement) {
        String value = null;

        if (jsonElement == null || jsonElement.isJsonNull()) {
            value = EMPTY_VALUE;
        } else {
            value = jsonElement.toString();
        }

        return value;
    }

    protected Either<String, JanusGraphOperationStatus> getInnerType(ToscaPropertyType type, Supplier<SchemaDefinition> schemeGen) {
        String innerType = null;
        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {

            SchemaDefinition def = schemeGen.get();
            if (def == null) {
                log.debug("Schema doesn't exists for property of type {}", type);
                return Either.right(JanusGraphOperationStatus.ILLEGAL_ARGUMENT);
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
                return Either.right(JanusGraphOperationStatus.ILLEGAL_ARGUMENT);
            }
            innerType = propDef.getType();
        }
        return Either.left(innerType);
    }

    /**
     * Convert Constarint object to json in order to add it to the Graph
     *
     * @param constraints
     * @return
     */
    public List<String> convertConstraintsToString(List<PropertyConstraint> constraints) {

        if (constraints == null || constraints.isEmpty()) {
            return null;
        }

        return constraints.stream().map(gson::toJson).collect(Collectors.toList());
    }

    public List<PropertyConstraint> convertConstraints(List<String> constraints) {

        if (constraints == null || constraints.isEmpty()) {
            return null;
        }

        Type constraintType = new TypeToken<PropertyConstraint>() {
        }.getType();

        Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();

        return constraints.stream().map(c -> gson.fromJson(c, PropertyConstraint.class)).collect(Collectors.toList());
    }

}
