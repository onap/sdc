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

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.validation.ToscaFunctionValidator;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.GroupInstanceData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("group-instance-operation")
public class GroupInstanceOperation extends AbstractOperation implements IGroupInstanceOperation {

    private static final String UPDATE_PROPERTY_VALUE_ON_COMPONENT_INSTANCE = "UpdatePropertyValueOnComponentInstance";
    private static final String FAILED_TO_UPDATE_PROPERTY_VALUE_ON_INSTANCE_STATUS_IS = "Failed to update property value on instance. Status is ";
    private static final Logger log = Logger.getLogger(GroupInstanceOperation.class.getName());
    private final GroupOperation groupOperation;
    private final PropertyOperation propertyOperation;
    private final ToscaFunctionValidator toscaFunctionValidator;
    private final ApplicationDataTypeCache applicationDataTypeCache;

    @Autowired
    public GroupInstanceOperation(final GroupOperation groupOperation, final PropertyOperation propertyOperation,
                                  final ToscaFunctionValidator toscaFunctionValidator,
                                  final ApplicationDataTypeCache applicationDataTypeCache) {
        this.groupOperation = groupOperation;
        this.propertyOperation = propertyOperation;
        this.toscaFunctionValidator = toscaFunctionValidator;
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    public Either<List<GroupInstance>, StorageOperationStatus> getAllGroupInstances(String parentId, NodeTypeEnum parentType) {
        Either<List<GroupInstance>, StorageOperationStatus> result = null;
        List<GroupInstance> groupInstanceRes = new ArrayList<>();
        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphGenericDao.getGraph();
        if (graph.isRight()) {
            log.debug("Failed to work with graph {}", graph.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(graph.right().value()));
        }
        JanusGraph tGraph = graph.left().value();
        @SuppressWarnings("unchecked") Iterable<JanusGraphVertex> vertices = tGraph.query()
            .has(UniqueIdBuilder.getKeyByNodeType(parentType), parentId).vertices();
        if (vertices == null || vertices.iterator() == null || !vertices.iterator().hasNext()) {
            log.debug("No nodes for type {}  for id = {}", parentType, parentId);
            result = Either.right(StorageOperationStatus.NOT_FOUND);
            return result;
        }
        Iterator<JanusGraphVertex> iterator = vertices.iterator();
        Vertex vertex = iterator.next();
        Map<String, Object> edgeProperties = null;
        Either<List<ImmutablePair<GroupInstanceData, GraphEdge>>, JanusGraphOperationStatus> childrenByEdgeCriteria = janusGraphGenericDao
            .getChildrenByEdgeCriteria(vertex, parentId, GraphEdgeLabels.GROUP_INST, NodeTypeEnum.GroupInstance, GroupInstanceData.class,
                edgeProperties);
        if (childrenByEdgeCriteria.isRight()) {
            JanusGraphOperationStatus status = childrenByEdgeCriteria.right().value();
            log.debug("Failed to find group instance {} on graph", childrenByEdgeCriteria.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        List<ImmutablePair<GroupInstanceData, GraphEdge>> list = childrenByEdgeCriteria.left().value();
        for (ImmutablePair<GroupInstanceData, GraphEdge> pair : list) {
            GroupInstanceData groupInstData = pair.getLeft();
            GroupInstance groupInstance = new GroupInstance(groupInstData.getGroupDataDefinition());
            String instOriginGroupId = groupInstance.getGroupUid();
            Either<GroupDefinition, StorageOperationStatus> groupRes = groupOperation.getGroupFromGraph(instOriginGroupId, false, true, false);
            if (groupRes.isRight()) {
                return Either.right(groupRes.right().value());
            }
            GroupDefinition groupDefinition = groupRes.left().value();
            Either<Map<String, PropertyValueData>, JanusGraphOperationStatus> groupInstancePropertyValuesRes = getAllGroupInstancePropertyValuesData(
                groupInstData);
            if (groupInstancePropertyValuesRes.isRight()) {
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(groupInstancePropertyValuesRes.right().value()));
            }
            buildGroupInstanceFromGroup(groupInstance, groupDefinition, groupInstancePropertyValuesRes.left().value());
            Either<List<ImmutablePair<String, String>>, JanusGraphOperationStatus> artifactsRes = getGroupArtifactsPairs(groupInstance.getUniqueId());
            if (artifactsRes.isRight()) {
                JanusGraphOperationStatus status = artifactsRes.right().value();
                if (status != JanusGraphOperationStatus.OK) {
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                    return result;
                }
            } else {
                List<String> artifactsUid = new ArrayList<>();
                List<String> artifactsUUID = new ArrayList<>();
                List<ImmutablePair<String, String>> list1 = artifactsRes.left().value();
                if (list != null) {
                    for (ImmutablePair<String, String> pair1 : list1) {
                        String uid = pair1.left;
                        String UUID = pair1.right;
                        artifactsUid.add(uid);
                        artifactsUUID.add(UUID);
                    }
                    groupInstance.setGroupInstanceArtifacts(artifactsUid);
                    groupInstance.setGroupInstanceArtifactsUuid(artifactsUUID);
                }
            }
            groupInstanceRes.add(groupInstance);
            log.debug("GroupInstance {} was added to list ", groupInstance.getUniqueId());
        }
        result = Either.left(groupInstanceRes);
        return result;
    }

    public Either<Integer, StorageOperationStatus> increaseAndGetGroupInstancePropertyCounter(String instanceId) {
        return propertyOperation.increaseAndGetObjInstancePropertyCounter(instanceId, NodeTypeEnum.GroupInstance);
    }

    public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToGroupInstance(ComponentInstanceProperty groupInstanceProperty,
                                                                                                     String groupInstanceId, Integer index,
                                                                                                     boolean inTransaction) {
        /// #RULES SUPPORT

        /// Ignore rules received from client till support
        groupInstanceProperty.setRules(null);
        ///

        ///
        Either<ComponentInstanceProperty, StorageOperationStatus> result = null;
        try {
            Either<PropertyValueData, JanusGraphOperationStatus> eitherStatus = addPropertyToGroupInstance(groupInstanceProperty, groupInstanceId,
                index);
            if (eitherStatus.isRight()) {
                log.error("Failed to add property value {} to resource instance {} in Graph. status is {}", groupInstanceProperty, groupInstanceId,
                    eitherStatus.right().value().name());
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(eitherStatus.right().value()));
                return result;
            } else {
                PropertyValueData propertyValueData = eitherStatus.left().value();
                ComponentInstanceProperty propertyValueResult = propertyOperation
                    .buildResourceInstanceProperty(propertyValueData, groupInstanceProperty);
                log.debug("The returned GroupInstanceProperty is {}", propertyValueResult);
                Either<String, JanusGraphOperationStatus> findDefaultValue = propertyOperation
                    .findDefaultValueFromSecondPosition(groupInstanceProperty.getPath(), groupInstanceProperty.getUniqueId(),
                        groupInstanceProperty.getDefaultValue());
                if (findDefaultValue.isRight()) {
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(findDefaultValue.right().value()));
                    return result;
                }
                String defaultValue = findDefaultValue.left().value();
                propertyValueResult.setDefaultValue(defaultValue);
                log.debug("The returned default value in ResourceInstanceProperty is {}", defaultValue);
                result = Either.left(propertyValueResult);
                return result;
            }
        } finally {
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
    }

    public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInGroupInstance(
        ComponentInstanceProperty gropuInstanceProperty, String groupInstanceId, boolean inTransaction) {
        // TODO Auto-generated method stub

        // change Propety class
        return null;
    }

    public void generateCustomizationUUID(GroupInstance groupInstance) {
        UUID uuid = UUID.randomUUID();
        groupInstance.setCustomizationUUID(uuid.toString());
    }

    /**
     * add property to resource instance
     *
     * @param index
     * @return
     */
    public Either<PropertyValueData, JanusGraphOperationStatus> addPropertyToGroupInstance(ComponentInstanceProperty groupInstanceProperty,
                                                                                           String groupInstanceId, Integer index) {
        Either<GroupInstanceData, JanusGraphOperationStatus> findResInstanceRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupInstance), groupInstanceId, GroupInstanceData.class);
        if (findResInstanceRes.isRight()) {
            JanusGraphOperationStatus status = findResInstanceRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return Either.right(status);
        }
        String propertyId = groupInstanceProperty.getUniqueId();
        Either<PropertyData, JanusGraphOperationStatus> findPropertyDefRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);
        if (findPropertyDefRes.isRight()) {
            JanusGraphOperationStatus status = findPropertyDefRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return Either.right(status);
        }
        String valueUniqueUid = groupInstanceProperty.getValueUniqueUid();
        if (valueUniqueUid == null) {
            PropertyData propertyData = findPropertyDefRes.left().value();
            GroupInstanceData resourceInstanceData = findResInstanceRes.left().value();
            ImmutablePair<JanusGraphOperationStatus, String> isPropertyValueExists = propertyOperation.findPropertyValue(groupInstanceId, propertyId);
            if (isPropertyValueExists.getLeft() == JanusGraphOperationStatus.ALREADY_EXIST) {
                log.debug("The property {} already added to the resource instance {}", propertyId, groupInstanceId);
                groupInstanceProperty.setValueUniqueUid(isPropertyValueExists.getRight());
                Either<PropertyValueData, JanusGraphOperationStatus> updatePropertyOfResourceInstance = updatePropertyOfGroupInstance(
                    groupInstanceProperty, groupInstanceId);
                if (updatePropertyOfResourceInstance.isRight()) {
                    BeEcompErrorManager.getInstance().logInternalFlowError(UPDATE_PROPERTY_VALUE_ON_COMPONENT_INSTANCE,
                        FAILED_TO_UPDATE_PROPERTY_VALUE_ON_INSTANCE_STATUS_IS + updatePropertyOfResourceInstance.right().value(),
                        ErrorSeverity.ERROR);
                    return Either.right(updatePropertyOfResourceInstance.right().value());
                }
                return Either.left(updatePropertyOfResourceInstance.left().value());
            }
            if (isPropertyValueExists.getLeft() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("After finding property value of {} on componenet instance {}", propertyId, groupInstanceId);
                return Either.right(isPropertyValueExists.getLeft());
            }
            String innerType = null;
            PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
            String propertyType = propDataDef.getType();
            String value = groupInstanceProperty.getValue();
            ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
            if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
                SchemaDefinition def = propDataDef.getSchema();
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
            log.debug("Before validateAndUpdatePropertyValue");
            Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes =
                applicationDataTypeCache.getAll(groupInstanceProperty.getModel());
            if (allDataTypes.isRight()) {
                JanusGraphOperationStatus status = allDataTypes.right().value();
                BeEcompErrorManager.getInstance()
                    .logInternalFlowError(UPDATE_PROPERTY_VALUE_ON_COMPONENT_INSTANCE, FAILED_TO_UPDATE_PROPERTY_VALUE_ON_INSTANCE_STATUS_IS + status,
                        ErrorSeverity.ERROR);
                return Either.right(status);
            }
            Either<Object, Boolean> isValid = propertyOperation
                .validateAndUpdatePropertyValue(propertyType, value, innerType, allDataTypes.left().value());
            log.debug("After validateAndUpdatePropertyValue. isValid = {}", isValid);
            String newValue = value;
            if (isValid.isRight()) {
                Boolean res = isValid.right().value();
                if (!res) {
                    return Either.right(JanusGraphOperationStatus.ILLEGAL_ARGUMENT);
                }
            } else {
                Object object = isValid.left().value();
                if (object != null) {
                    newValue = object.toString();
                }
            }
            String uniqueId = UniqueIdBuilder.buildResourceInstancePropertyValueUid(resourceInstanceData.getUniqueId(), index);
            PropertyValueData propertyValueData = new PropertyValueData();
            propertyValueData.setUniqueId(uniqueId);
            propertyValueData.setValue(newValue);
            log.debug("Before validateAndUpdateRules");
            ImmutablePair<String, Boolean> pair = propertyOperation
                .validateAndUpdateRules(propertyType, groupInstanceProperty.getRules(), innerType, allDataTypes.left().value(), false);
            log.debug("After validateAndUpdateRules. pair = {}", pair);
            if (pair.getRight() != null && !pair.getRight()) {
                BeEcompErrorManager.getInstance()
                    .logBeInvalidValueError("Add property value", pair.getLeft(), groupInstanceProperty.getName(), propertyType);
                return Either.right(JanusGraphOperationStatus.ILLEGAL_ARGUMENT);
            }
            propertyOperation.addRulesToNewPropertyValue(propertyValueData, groupInstanceProperty, groupInstanceId);
            log.debug("Before adding property value to graph {}", propertyValueData);
            Either<PropertyValueData, JanusGraphOperationStatus> createNodeResult = janusGraphGenericDao
                .createNode(propertyValueData, PropertyValueData.class);
            log.debug("After adding property value to graph {}", propertyValueData);
            if (createNodeResult.isRight()) {
                JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
                return Either.right(operationStatus);
            }
            propertyValueData = createNodeResult.left().value();
            Either<GraphRelation, JanusGraphOperationStatus> createRelResult = janusGraphGenericDao
                .createRelation(propertyValueData, propertyData, GraphEdgeLabels.PROPERTY_IMPL, null);
            if (createRelResult.isRight()) {
                JanusGraphOperationStatus operationStatus = createRelResult.right().value();
                log.error("Failed to associate property value {} to property {} in graph. status is {}", uniqueId, propertyId, operationStatus);
                return Either.right(operationStatus);
            }
            createRelResult = janusGraphGenericDao.createRelation(resourceInstanceData, propertyValueData, GraphEdgeLabels.PROPERTY_VALUE, null);
            if (createRelResult.isRight()) {
                JanusGraphOperationStatus operationStatus = createRelResult.right().value();
                log.error("Failed to associate resource instance {} property value {} in graph. status is {}", groupInstanceId, uniqueId,
                    operationStatus);
                return Either.right(operationStatus);
            }
            return Either.left(propertyValueData);
        } else {
            log.error("property value already exists.");
            return Either.right(JanusGraphOperationStatus.ALREADY_EXIST);
        }
    }

    /**
     * update value of attribute on resource instance
     *
     * @return
     */
    public Either<PropertyValueData, JanusGraphOperationStatus> updatePropertyOfGroupInstance(ComponentInstanceProperty groupInstanceProperty,
                                                                                              String groupInstanceId) {
        Wrapper<JanusGraphOperationStatus> errorWrapper = new Wrapper<>();
        UpdateDataContainer<PropertyData, PropertyValueData> updateDataContainer = new UpdateDataContainer<>(GraphEdgeLabels.PROPERTY_IMPL,
            (() -> PropertyData.class), (() -> PropertyValueData.class), NodeTypeEnum.Property, NodeTypeEnum.PropertyValue);
        preUpdateElementOfResourceInstanceValidations(updateDataContainer, groupInstanceProperty, groupInstanceId, errorWrapper);
        if (!errorWrapper.isEmpty()) {
            return Either.right(errorWrapper.getInnerElement());
        }

        String value = groupInstanceProperty.getValue();
        // Specific Validation Logic
        PropertyData propertyData = updateDataContainer.getDataWrapper().getInnerElement();
        PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
        String propertyType = propDataDef.getType();
        final ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
        log.debug("The type of the property {} is {}", propertyData.getUniqueId(), propertyType);
        final Either<String, JanusGraphOperationStatus> innerTypeEither = propertyOperation.getInnerType(type, propDataDef::getSchema);
        if (innerTypeEither.isRight()) {
            return Either.right(innerTypeEither.right().value());
        }
        // Specific Update Logic
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes =
            applicationDataTypeCache.getAll(groupInstanceProperty.getModel());
        if (allDataTypes.isRight()) {
            JanusGraphOperationStatus status = allDataTypes.right().value();
            BeEcompErrorManager.getInstance()
                .logInternalFlowError(UPDATE_PROPERTY_VALUE_ON_COMPONENT_INSTANCE, FAILED_TO_UPDATE_PROPERTY_VALUE_ON_INSTANCE_STATUS_IS + status,
                    ErrorSeverity.ERROR);
            return Either.right(status);
        }
        PropertyValueData propertyValueData = updateDataContainer.getValueDataWrapper().getInnerElement();
        if (propDataDef.isToscaFunction()) {
            Either<GroupInstanceData, JanusGraphOperationStatus> findResInstanceRes = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupInstance), groupInstanceId, GroupInstanceData.class);
            final GroupInstanceData groupInstanceData = findResInstanceRes.left().value();
            //TODO fix
            toscaFunctionValidator.validate(propDataDef, null);
            propertyValueData.setValue(propDataDef.getToscaFunction().getValue());
        } else {
            final String innerType = innerTypeEither.left().value();
            Either<Object, Boolean> isValid = propertyOperation
                .validateAndUpdatePropertyValue(propertyType, value, innerType, allDataTypes.left().value());
            String newValue = value;
            if (isValid.isRight()) {
                Boolean res = isValid.right().value();
                if (!res) {
                    return Either.right(JanusGraphOperationStatus.ILLEGAL_ARGUMENT);
                }
            } else {
                Object object = isValid.left().value();
                if (object != null) {
                    newValue = object.toString();
                }
            }
            log.debug("Going to update property value from {} to {}", propertyValueData.getValue(), newValue);
            propertyValueData.setValue(newValue);
            ImmutablePair<String, Boolean> pair = propertyOperation
                .validateAndUpdateRules(propertyType, groupInstanceProperty.getRules(), innerType, allDataTypes.left().value(), true);
            if (pair.getRight() != null && !pair.getRight()) {
                BeEcompErrorManager.getInstance()
                    .logBeInvalidValueError("Add property value", pair.getLeft(), groupInstanceProperty.getName(), propertyType);
                return Either.right(JanusGraphOperationStatus.ILLEGAL_ARGUMENT);
            }
        }

        propertyOperation.updateRulesInPropertyValue(propertyValueData, groupInstanceProperty, groupInstanceId);
        Either<PropertyValueData, JanusGraphOperationStatus> updateRes = janusGraphGenericDao
            .updateNode(propertyValueData, PropertyValueData.class);
        if (updateRes.isRight()) {
            JanusGraphOperationStatus status = updateRes.right().value();
            return Either.right(status);
        } else {
            return Either.left(updateRes.left().value());
        }
    }

    private <SomeData extends GraphNode, SomeValueData extends GraphNode> void preUpdateElementOfResourceInstanceValidations(
        UpdateDataContainer<SomeData, SomeValueData> updateDataContainer, IComponentInstanceConnectedElement resourceInstanceProerty,
        String resourceInstanceId, Wrapper<JanusGraphOperationStatus> errorWrapper) {
        if (errorWrapper.isEmpty()) {
            // Verify VFC instance Exist
            validateGIExist(resourceInstanceId, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            // Example: Verify Property connected to VFC exist
            validateElementConnectedToComponentExist(updateDataContainer, resourceInstanceProerty, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            // Example: Verify PropertyValue connected to VFC Instance exist
            validateElementConnectedToComponentInstanceExist(updateDataContainer, resourceInstanceProerty, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            // Example: Verify PropertyValue connected Property
            validateElementConnectedToInstance(updateDataContainer, resourceInstanceProerty, errorWrapper);
        }
    }

    private <SomeData extends GraphNode, SomeValueData extends GraphNode> void validateElementConnectedToInstance(
        UpdateDataContainer<SomeData, SomeValueData> updateDataContainer, IComponentInstanceConnectedElement resourceInstanceProerty,
        Wrapper<JanusGraphOperationStatus> errorWrapper) {
        Either<ImmutablePair<SomeData, GraphEdge>, JanusGraphOperationStatus> child = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(updateDataContainer.getNodeTypeValue()), resourceInstanceProerty.getValueUniqueUid(),
                updateDataContainer.getGraphEdge(), updateDataContainer.getNodeType(), updateDataContainer.getSomeDataClassGen().get());
        if (child.isRight()) {
            JanusGraphOperationStatus status = child.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            errorWrapper.setInnerElement(status);
        } else {
            updateDataContainer.getDataWrapper().setInnerElement(child.left().value().left);
        }
    }

    private <SomeValueData extends GraphNode, SomeData extends GraphNode> void validateElementConnectedToComponentInstanceExist(
        UpdateDataContainer<SomeData, SomeValueData> updateDataContainer, IComponentInstanceConnectedElement resourceInstanceProerty,
        Wrapper<JanusGraphOperationStatus> errorWrapper) {
        String valueUniqueUid = resourceInstanceProerty.getValueUniqueUid();
        if (valueUniqueUid == null) {
            errorWrapper.setInnerElement(JanusGraphOperationStatus.INVALID_ID);
        } else {
            Either<SomeValueData, JanusGraphOperationStatus> findPropertyValueRes = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(updateDataContainer.getNodeTypeValue()), valueUniqueUid,
                    updateDataContainer.getSomeValueDataClassGen().get());
            if (findPropertyValueRes.isRight()) {
                JanusGraphOperationStatus status = findPropertyValueRes.right().value();
                if (status == JanusGraphOperationStatus.NOT_FOUND) {
                    status = JanusGraphOperationStatus.INVALID_ID;
                }
                errorWrapper.setInnerElement(status);
            } else {
                updateDataContainer.getValueDataWrapper().setInnerElement(findPropertyValueRes.left().value());
            }
        }
    }

    private <SomeData extends GraphNode, SomeValueData extends GraphNode> void validateElementConnectedToComponentExist(
        UpdateDataContainer<SomeData, SomeValueData> updateDataContainer, IComponentInstanceConnectedElement resourceInstanceElementConnected,
        Wrapper<JanusGraphOperationStatus> errorWrapper) {
        String uniqueId = resourceInstanceElementConnected.getUniqueId();
        Either<SomeData, JanusGraphOperationStatus> findPropertyDefRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(updateDataContainer.getNodeType()), uniqueId, updateDataContainer.getSomeDataClassGen().get());
        if (findPropertyDefRes.isRight()) {
            JanusGraphOperationStatus status = findPropertyDefRes.right().value();
            errorWrapper.setInnerElement(status);
        }
    }

    private void validateGIExist(String resourceInstanceId, Wrapper<JanusGraphOperationStatus> errorWrapper) {
        validateGIExist(resourceInstanceId, null, errorWrapper);
    }

    private void validateGIExist(String resourceInstanceId, Wrapper<GroupInstanceData> compInsDataWrapper,
                                 Wrapper<JanusGraphOperationStatus> errorWrapper) {
        validateElementExistInGraph(resourceInstanceId, NodeTypeEnum.GroupInstance, () -> GroupInstanceData.class, compInsDataWrapper, errorWrapper);
    }

    public <ElementData extends GraphNode> void validateElementExistInGraph(String elementUniqueId, NodeTypeEnum elementNodeType,
                                                                            Supplier<Class<ElementData>> elementClassGen,
                                                                            Wrapper<ElementData> elementDataWrapper,
                                                                            Wrapper<JanusGraphOperationStatus> errorWrapper) {
        Either<ElementData, JanusGraphOperationStatus> findResInstanceRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(elementNodeType), elementUniqueId, elementClassGen.get());
        if (findResInstanceRes.isRight()) {
            JanusGraphOperationStatus status = findResInstanceRes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            errorWrapper.setInnerElement(status);
        } else {
            if (elementDataWrapper != null) {
                elementDataWrapper.setInnerElement(findResInstanceRes.left().value());
            }
        }
    }

    private void buildGroupInstanceFromGroup(GroupInstance groupInstance, GroupDefinition groupDefinition,
                                             Map<String, PropertyValueData> groupInstancePropertyValues) {
        groupInstance.setGroupName(groupDefinition.getName());
        groupInstance.setInvariantUUID(groupDefinition.getInvariantUUID());
        groupInstance.setDescription(groupDefinition.getDescription());
        groupInstance.setVersion(groupDefinition.getVersion());
        groupInstance.setArtifacts(groupDefinition.getArtifacts());
        groupInstance.setArtifactsUuid(groupDefinition.getArtifactsUuid());
        groupInstance.setType(groupDefinition.getType());
        groupInstance.setGroupUUID(groupDefinition.getGroupUUID());
        List<GroupInstanceProperty> groupInstanceProperties = groupDefinition.convertToGroupProperties()
            //converts List of GroupProperties to List of GroupInstanceProperties and updates it with group instance property data
            .stream().map(p -> getUpdatedConvertedProperty(p, groupInstancePropertyValues)).collect(Collectors.toList());
        groupInstance.convertFromGroupInstancesProperties(groupInstanceProperties);
    }

    private GroupInstanceProperty getUpdatedConvertedProperty(GroupProperty groupProperty,
                                                              Map<String, PropertyValueData> groupInstancePropertyValues) {
        GroupInstanceProperty updatedProperty = new GroupInstanceProperty(groupProperty, groupProperty.getValue());
        if (!MapUtils.isEmpty(groupInstancePropertyValues) && groupInstancePropertyValues.containsKey(groupProperty.getName())) {
            PropertyValueData groupInstancePropertyValue = groupInstancePropertyValues.get(groupProperty.getName());
            updatedProperty.setValue(groupInstancePropertyValue.getValue());
            updatedProperty.setValueUniqueUid(groupInstancePropertyValue.getUniqueId());
        }
        return updatedProperty;
    }

    private Either<List<ImmutablePair<String, String>>, JanusGraphOperationStatus> getGroupArtifactsPairs(String groupUniqueId) {
        Either<List<ImmutablePair<String, String>>, JanusGraphOperationStatus> result = null;
        Either<List<ImmutablePair<ArtifactData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupInstance), groupUniqueId, GraphEdgeLabels.GROUP_ARTIFACT_REF,
                NodeTypeEnum.ArtifactRef, ArtifactData.class);
        if (childrenNodes.isRight()) {
            JanusGraphOperationStatus status = childrenNodes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.OK;
            }
            result = Either.right(status);
        } else {
            List<ImmutablePair<String, String>> artifactsList = new ArrayList<>();
            List<ImmutablePair<ArtifactData, GraphEdge>> list = childrenNodes.left().value();
            if (list != null) {
                for (ImmutablePair<ArtifactData, GraphEdge> pair : list) {
                    ArtifactData artifactData = pair.getKey();
                    String uniqueId = artifactData.getArtifactDataDefinition().getUniqueId();
                    String UUID = artifactData.getArtifactDataDefinition().getArtifactUUID();
                    ImmutablePair<String, String> artifact = new ImmutablePair<>(uniqueId, UUID);
                    artifactsList.add(artifact);
                }
            }
            log.debug("The artifacts list related to group {} is {}", groupUniqueId, artifactsList);
            result = Either.left(artifactsList);
        }
        return result;
    }

    public StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifact(String componentId, NodeTypeEnum componentTypeEnum,
                                                                                   String oldArtifactId, ArtifactData newArtifact) {
        return this.dissociateAndAssociateGroupsInstanceFromArtifactOnGraph(componentId, componentTypeEnum, oldArtifactId, newArtifact);
    }

    private StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum,
                                                                                           String oldArtifactId, ArtifactData newArtifact) {
        Either<List<GroupInstance>, StorageOperationStatus> allGroupsFromGraph = getAllGroupInstances(componentId, componentTypeEnum);
        if (allGroupsFromGraph.isRight()) {
            return allGroupsFromGraph.right().value();
        }
        List<GroupInstance> allGroups = allGroupsFromGraph.left().value();
        if (allGroups == null || allGroups.isEmpty()) {
            return StorageOperationStatus.OK;
        }
        // Find all groups which contains this artifact id
        List<GroupInstance> associatedGroups = allGroups.stream()
            .filter(p -> p.getGroupInstanceArtifacts() != null && p.getGroupInstanceArtifacts().contains(oldArtifactId)).collect(Collectors.toList());
        if (associatedGroups != null && !associatedGroups.isEmpty()) {
            log.debug("The groups {} contains the artifact {}",
                associatedGroups.stream().map(GroupInstanceDataDefinition::getName).collect(Collectors.toList()), oldArtifactId);
            UniqueIdData oldArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, oldArtifactId);
            UniqueIdData newArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, newArtifact.getArtifactDataDefinition().getUniqueId());
            Map<String, Object> props = new HashMap<>();
            props.put(GraphPropertiesDictionary.NAME.getProperty(), newArtifactData.getLabel());
            for (GroupInstance groupDefinition : associatedGroups) {
                UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.GroupInstance, groupDefinition.getUniqueId());
                Either<GraphRelation, JanusGraphOperationStatus> deleteRelation = janusGraphGenericDao
                    .deleteRelation(groupData, oldArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF);
                log.trace("After dissociate group {} from artifact {}", groupDefinition.getName(), oldArtifactId);
                if (deleteRelation.isRight()) {
                    JanusGraphOperationStatus status = deleteRelation.right().value();
                    if (status == JanusGraphOperationStatus.NOT_FOUND) {
                        status = JanusGraphOperationStatus.INVALID_ID;
                    }
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
                }
                Either<GraphRelation, JanusGraphOperationStatus> createRelation = janusGraphGenericDao
                    .createRelation(groupData, newArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF, props);
                log.trace("After associate group {} to artifact {}", groupDefinition.getName(), newArtifact.getUniqueIdKey());
                if (createRelation.isRight()) {
                    JanusGraphOperationStatus status = createRelation.right().value();
                    if (status == JanusGraphOperationStatus.NOT_FOUND) {
                        status = JanusGraphOperationStatus.INVALID_ID;
                    }
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
                }
            }
        }
        return StorageOperationStatus.OK;
    }

    private Either<Map<String, PropertyValueData>, JanusGraphOperationStatus> getAllGroupInstancePropertyValuesData(GroupInstanceData groupInstData) {
        Either<Map<String, PropertyValueData>, JanusGraphOperationStatus> result = null;
        try {
            Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, JanusGraphOperationStatus> getPropertyValueChildrenRes = janusGraphGenericDao
                .getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), groupInstData.getUniqueId(), GraphEdgeLabels.PROPERTY_VALUE,
                    NodeTypeEnum.PropertyValue, PropertyValueData.class, true);
            if (getPropertyValueChildrenRes.isRight()) {
                JanusGraphOperationStatus status = getPropertyValueChildrenRes.right().value();
                log.debug("Failed to fetch property value nodes for group instance {}. Status is {}. ", groupInstData.getName(), status);
                if (status == JanusGraphOperationStatus.NOT_FOUND) {
                    result = Either.left(null);
                } else {
                    result = Either.right(status);
                }
            } else {
                result = Either.left(getPropertyValueChildrenRes.left().value().stream().collect(Collectors
                    .toMap(pair -> (String) (pair.getRight().getProperties().get(GraphPropertiesDictionary.PROPERTY_NAME.getProperty())),
                        ImmutablePair::getLeft)));
            }
        } catch (Exception e) {
            log.debug("The Exception occured during fetch group instance () property values. The message is {}. ", groupInstData.getName(),
                e.getMessage(), e);
            if (result == null) {
                result = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
            }
        }
        return result;
    }

    private static final class UpdateDataContainer<SomeData, SomeValueData> {

        final Wrapper<SomeValueData> valueDataWrapper;
        final Wrapper<SomeData> dataWrapper;
        final GraphEdgeLabels graphEdge;
        final Supplier<Class<SomeData>> someDataClassGen;
        final Supplier<Class<SomeValueData>> someValueDataClassGen;
        final NodeTypeEnum nodeType;
        final NodeTypeEnum nodeTypeValue;

        private UpdateDataContainer(GraphEdgeLabels graphEdge, Supplier<Class<SomeData>> someDataClassGen,
                                    Supplier<Class<SomeValueData>> someValueDataClassGen, NodeTypeEnum nodeType, NodeTypeEnum nodeTypeValue) {
            super();
            this.valueDataWrapper = new Wrapper<>();
            this.dataWrapper = new Wrapper<>();
            this.graphEdge = graphEdge;
            this.someDataClassGen = someDataClassGen;
            this.someValueDataClassGen = someValueDataClassGen;
            this.nodeType = nodeType;
            this.nodeTypeValue = nodeTypeValue;
        }

        public Wrapper<SomeValueData> getValueDataWrapper() {
            return valueDataWrapper;
        }

        public Wrapper<SomeData> getDataWrapper() {
            return dataWrapper;
        }

        public GraphEdgeLabels getGraphEdge() {
            return graphEdge;
        }

        public Supplier<Class<SomeData>> getSomeDataClassGen() {
            return someDataClassGen;
        }

        public Supplier<Class<SomeValueData>> getSomeValueDataClassGen() {
            return someValueDataClassGen;
        }

        public NodeTypeEnum getNodeType() {
            return nodeType;
        }

        public NodeTypeEnum getNodeTypeValue() {
            return nodeTypeValue;
        }
    }
}
