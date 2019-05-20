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

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.AdditionalInfoParameterData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component("additional-information-operation")
public class AdditionalInformationOperation implements IAdditionalInformationOperation {

    private static final Logger log = Logger.getLogger(AdditionalInformationOperation.class.getName());

    @javax.annotation.Resource
    private JanusGraphGenericDao janusGraphGenericDao;

    private static final String GOING_TO_EXECUTE_COMMIT_ON_GRAPH = "Going to execute commit on graph.";
	private static final String GOING_TO_EXECUTE_ROLLBACK_ON_GRAPH = "Going to execute rollback on graph.";
	private static final String ADDITIONAL_INFORMATION_OF = "additional information of ";
	public static final String EMPTY_VALUE = null;

    public AdditionalInformationOperation() {
        super();
    }


    @Override
    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> addAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String key, String value) {

        JanusGraphOperationStatus
            verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
        if (verifyNodeTypeVsComponent != JanusGraphOperationStatus.OK) {
            return Either.right(verifyNodeTypeVsComponent);
        }

        Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> getResult = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

        if (getResult.isRight()) {
            JanusGraphOperationStatus status = getResult.right().value();
            return Either.right(status);
        }

        ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
        AdditionalInfoParameterData parameterData = immutablePair.getLeft();
        Map<String, String> parameters = parameterData.getParameters();
        if (parameters == null) {
            parameters = new HashMap<>();
            parameterData.setParameters(parameters);
        }
        Map<String, String> idToKey = parameterData.getIdToKey();
        if (idToKey == null) {
            idToKey = new HashMap<>();
            parameterData.setIdToKey(idToKey);
        }

        Integer lastCreatedCounter = parameterData.getAdditionalInfoParameterDataDefinition().getLastCreatedCounter();
        lastCreatedCounter++;

        if (parameters.containsKey(key)) {
            log.debug("The key {} already exists under component {}", key, componentId);
            return Either.right(JanusGraphOperationStatus.ALREADY_EXIST);
        }

        idToKey.put(String.valueOf(lastCreatedCounter), key);
        parameters.put(key, value);
        parameterData.getAdditionalInfoParameterDataDefinition().setLastCreatedCounter(lastCreatedCounter);

        Either<AdditionalInfoParameterData, JanusGraphOperationStatus> updateNode = janusGraphGenericDao
            .updateNode(parameterData, AdditionalInfoParameterData.class);

        if (updateNode.isRight()) {
            JanusGraphOperationStatus status = updateNode.right().value();
            BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("UpdateAdditionalInformationParameter", ADDITIONAL_INFORMATION_OF + nodeType.getName() + " " + componentId, String.valueOf(status));
            return Either.right(status);
        }

        AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, updateNode.left().value());

        return Either.left(informationDefinition);

    }

    @Override
    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String id, String key, String value) {

        JanusGraphOperationStatus
            verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
        if (verifyNodeTypeVsComponent != JanusGraphOperationStatus.OK) {
            return Either.right(verifyNodeTypeVsComponent);
        }

        Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> getResult = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

        if (getResult.isRight()) {
            JanusGraphOperationStatus status = getResult.right().value();
            return Either.right(status);
        }

        ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
        AdditionalInfoParameterData parameterData = immutablePair.getLeft();
        Map<String, String> parameters = parameterData.getParameters();
        Map<String, String> idToKey = parameterData.getIdToKey();
        if (idToKey == null || !idToKey.containsKey(id)) {
            return Either.right(JanusGraphOperationStatus.INVALID_ID);
        }

        String origKey = idToKey.get(id);

        if (!origKey.equals(key)) {
            if (parameters.containsKey(key)) {
                log.debug("The key {} already exists", key);
                return Either.right(JanusGraphOperationStatus.ALREADY_EXIST);
            }
            String removed = parameters.remove(origKey);
            log.trace("The key-value {} = {} was removed from additionalInformation", origKey, removed);
        }
        parameters.put(key, value);
        idToKey.put(id, key);

        Either<AdditionalInfoParameterData, JanusGraphOperationStatus> updateNode = janusGraphGenericDao
            .updateNode(parameterData, AdditionalInfoParameterData.class);

        if (updateNode.isRight()) {
            JanusGraphOperationStatus status = updateNode.right().value();
            BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("UpdateAdditionalInformationParameter", "additional information of resource " + componentId, String.valueOf(status));
            return Either.right(status);
        }

        AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, updateNode.left().value());

        return Either.left(informationDefinition);

    }

    @Override
    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String id) {

        JanusGraphOperationStatus
            verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
        if (verifyNodeTypeVsComponent != JanusGraphOperationStatus.OK) {
            return Either.right(verifyNodeTypeVsComponent);
        }

        Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> getResult = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

        if (getResult.isRight()) {
            JanusGraphOperationStatus status = getResult.right().value();
            return Either.right(status);
        }

        ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
        AdditionalInfoParameterData parameterData = immutablePair.getLeft();
        Map<String, String> parameters = parameterData.getParameters();
        Map<String, String> idToKey = parameterData.getIdToKey();

        if (idToKey == null || !idToKey.containsKey(id)) {
            return Either.right(JanusGraphOperationStatus.INVALID_ID);
        }

        String key = idToKey.get(id);
        String removedKey = idToKey.remove(id);
        String removedValue = parameters.remove(key);
        log.trace("The key-value {} = {} was removed from additionalInformation", removedKey, removedValue);

        Either<AdditionalInfoParameterData, JanusGraphOperationStatus> updateNode = janusGraphGenericDao
            .updateNode(parameterData, AdditionalInfoParameterData.class);

        if (updateNode.isRight()) {
            JanusGraphOperationStatus status = updateNode.right().value();
            BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("DeleteAdditionalInformationParameter", ADDITIONAL_INFORMATION_OF + nodeType.getName() + " " + componentId, String.valueOf(status));
            return Either.right(status);
        }

        AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, updateNode.left().value());

        return Either.left(informationDefinition);

    }

    private AdditionalInformationDefinition createInformationDefinitionFromNode(String resourceId, Map<String, String> parameters, Map<String, String> idToKey, AdditionalInfoParameterData additionalInfoParameterData) {
        AdditionalInfoParameterDataDefinition dataDefinition = additionalInfoParameterData.getAdditionalInfoParameterDataDefinition();

        return new AdditionalInformationDefinition(dataDefinition, resourceId, convertParameters(parameters, idToKey));
    }

    private List<AdditionalInfoParameterInfo> convertParameters(Map<String, String> parameters, Map<String, String> idToKey) {

        List<AdditionalInfoParameterInfo> list = new ArrayList<>();

        if (parameters != null) {
            for (Entry<String, String> idToKeyEntry : idToKey.entrySet()) {

                String id = idToKeyEntry.getKey();
                String key = idToKeyEntry.getValue();

                String value = parameters.get(key);

                AdditionalInfoParameterInfo parameterInfo = new AdditionalInfoParameterInfo(id, key, value);
                list.add(parameterInfo);
            }

        }

        return list;
    }

    @Override
    public Either<AdditionalInfoParameterData, JanusGraphOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId) {

        UniqueIdData from = new UniqueIdData(nodeType, componentId);

        String uniqueId = UniqueIdBuilder.buildAdditionalInformationUniqueId(componentId);
        AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition = new AdditionalInfoParameterDataDefinition();
        additionalInfoParameterDataDefinition.setUniqueId(uniqueId);

        AdditionalInfoParameterData additionalInfoParameterData = new AdditionalInfoParameterData(additionalInfoParameterDataDefinition, new HashMap<>(), new HashMap<>());

        Either<AdditionalInfoParameterData, JanusGraphOperationStatus> createNode = janusGraphGenericDao
            .createNode(additionalInfoParameterData, AdditionalInfoParameterData.class);
        if (createNode.isRight()) {
            JanusGraphOperationStatus status = createNode.right().value();
            BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("AddAdditionalInformationNode", uniqueId, String.valueOf(status));
            return Either.right(status);
        }

        AdditionalInfoParameterData to = createNode.left().value();

        Either<GraphRelation, JanusGraphOperationStatus> createRelation = janusGraphGenericDao
            .createRelation(from, to, GraphEdgeLabels.ADDITIONAL_INFORMATION, null);
        if (createRelation.isRight()) {
            JanusGraphOperationStatus status = createRelation.right().value();
            return Either.right(status);
        }

        return Either.left(to);
    }

    @Override
    public Either<JanusGraphVertex, JanusGraphOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, JanusGraphVertex metadataVertex) {

        String uniqueId = UniqueIdBuilder.buildAdditionalInformationUniqueId(componentId);
        AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition = new AdditionalInfoParameterDataDefinition();
        additionalInfoParameterDataDefinition.setUniqueId(uniqueId);

        AdditionalInfoParameterData additionalInfoParameterData = new AdditionalInfoParameterData(additionalInfoParameterDataDefinition, new HashMap<>(), new HashMap<>());

        Either<JanusGraphVertex, JanusGraphOperationStatus> createNode = janusGraphGenericDao.createNode(additionalInfoParameterData);
        if (createNode.isRight()) {
            JanusGraphOperationStatus status = createNode.right().value();
            BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("AddAdditionalInformationNode", uniqueId, String.valueOf(status));
            return Either.right(status);
        }

        JanusGraphVertex additionalInfoVertex = createNode.left().value();

        JanusGraphOperationStatus createRelation = janusGraphGenericDao
            .createEdge(metadataVertex, additionalInfoVertex, GraphEdgeLabels.ADDITIONAL_INFORMATION, null);

        if (!createRelation.equals(JanusGraphOperationStatus.OK)) {
            return Either.right(createRelation);
        }
        return Either.left(additionalInfoVertex);
    }

    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters) {

        Either<AdditionalInfoParameterData, JanusGraphOperationStatus> status = this.addAdditionalInformationNode(nodeType, componentId);

        if (status.isRight()) {
            return Either.right(status.right().value());
        }

        AdditionalInfoParameterData parameterData = status.left().value();

        populateParameterNodeWithParameters(parameterData, parameters);

        Either<AdditionalInfoParameterData, JanusGraphOperationStatus> updateNode = janusGraphGenericDao
            .updateNode(parameterData, AdditionalInfoParameterData.class);

        if (updateNode.isRight()) {
            return Either.right(updateNode.right().value());
        }

        AdditionalInformationDefinition informationDefinition = convertAdditionalInformationDataToDefinition(updateNode.left().value(), componentId);

        return Either.left(informationDefinition);
    }

    public JanusGraphOperationStatus addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters, JanusGraphVertex metadataVertex) {

        Either<JanusGraphVertex, JanusGraphOperationStatus> status = this.addAdditionalInformationNode(nodeType, componentId, metadataVertex);

        if (status.isRight()) {
            return status.right().value();
        }
        JanusGraphVertex additionalInfoVertex = status.left().value();

        Map<String, Object> newProp = janusGraphGenericDao.getProperties(additionalInfoVertex);
        AdditionalInfoParameterData parameterData = GraphElementFactory.createElement(NodeTypeEnum.AdditionalInfoParameters.getName(), GraphElementTypeEnum.Node, newProp, AdditionalInfoParameterData.class);

        populateParameterNodeWithParameters(parameterData, parameters);

        return janusGraphGenericDao.updateVertex(parameterData, additionalInfoVertex);
    }

    private void populateParameterNodeWithParameters(AdditionalInfoParameterData parameterData, AdditionalInformationDefinition aiDefinition) {

        if (aiDefinition != null) {

            Integer lastCreatedCounter = aiDefinition.getLastCreatedCounter();
            parameterData.getAdditionalInfoParameterDataDefinition().setLastCreatedCounter(lastCreatedCounter);
            log.trace("Set last created counter of additional information to {}", lastCreatedCounter);

            List<AdditionalInfoParameterInfo> parameters = aiDefinition.getParameters();
            if (parameters != null) {

                Map<String, String> idToKey = new HashMap<>();
                Map<String, String> parametersMap = new HashMap<>();
                for (AdditionalInfoParameterInfo additionalInfoParameterInfo : parameters) {
                    String uniqueId = additionalInfoParameterInfo.getUniqueId();
                    String key = additionalInfoParameterInfo.getKey();
                    String value = additionalInfoParameterInfo.getValue();

                    if (key != null && !key.isEmpty()) {
                        idToKey.put(uniqueId, key);
                        parametersMap.put(key, value);
                    }
                }
                parameterData.setIdToKey(idToKey);
                parameterData.setParameters(parametersMap);
            }
        }

    }

    @Override
    public JanusGraphOperationStatus findResourceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties) {

        log.trace("Going to fetch additional information under resource {}", uniqueId);
        JanusGraphOperationStatus resourceCapabilitiesStatus = findAdditionalInformationOfNode(NodeTypeEnum.Resource, uniqueId, properties);

        if (!resourceCapabilitiesStatus.equals(JanusGraphOperationStatus.OK)) {
            return resourceCapabilitiesStatus;
        }

        Either<ImmutablePair<ResourceMetadataData, GraphEdge>, JanusGraphOperationStatus> parentNodes = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
                ResourceMetadataData.class);

        if (parentNodes.isRight()) {
            JanusGraphOperationStatus parentNodesStatus = parentNodes.right().value();
            if (!parentNodesStatus.equals(JanusGraphOperationStatus.NOT_FOUND)) {
                log.error("Failed to find parent additional information of resource {}. status is {}", uniqueId, parentNodesStatus);
                return parentNodesStatus;
            }
        }

        if (parentNodes.isLeft()) {
            ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
            String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
            JanusGraphOperationStatus addParentIntStatus = findResourceAllAdditionalInformationRecursively(parentUniqueId, properties);

            if (addParentIntStatus != JanusGraphOperationStatus.OK) {
                log.error("Failed to find all resource additional information of resource {}", parentUniqueId);
                return addParentIntStatus;
            }
        }
        return JanusGraphOperationStatus.OK;

    }

    @Override
    public JanusGraphOperationStatus findServiceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties) {

        log.trace("Going to fetch additional information under service {}", uniqueId);
        JanusGraphOperationStatus resourceCapabilitiesStatus = findAdditionalInformationOfNode(NodeTypeEnum.Service, uniqueId, properties);

        if (!resourceCapabilitiesStatus.equals(JanusGraphOperationStatus.OK)) {
            return resourceCapabilitiesStatus;
        }

        Either<ImmutablePair<ServiceMetadataData, GraphEdge>, JanusGraphOperationStatus> parentNodes = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Service), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Service,
                ServiceMetadataData.class);

        if (parentNodes.isRight()) {
            JanusGraphOperationStatus parentNodesStatus = parentNodes.right().value();
            if (!parentNodesStatus.equals(JanusGraphOperationStatus.NOT_FOUND)) {
                log.error("Failed to find parent additional information of resource {}. status is {}", uniqueId, parentNodesStatus);
                return parentNodesStatus;
            }
        }

        if (parentNodes.isLeft()) {
            ImmutablePair<ServiceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
            String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
            JanusGraphOperationStatus addParentIntStatus = findServiceAllAdditionalInformationRecursively(parentUniqueId, properties);

            if (addParentIntStatus != JanusGraphOperationStatus.OK) {
                log.error("Failed to find all resource additional information of resource {}", parentUniqueId);
                return addParentIntStatus;
            }
        }
        return JanusGraphOperationStatus.OK;

    }

    private JanusGraphOperationStatus findAdditionalInformationOfNode(NodeTypeEnum nodeType, String uniqueId, List<AdditionalInformationDefinition> properties) {

        Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> childNode = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

        if (childNode.isRight()) {
            JanusGraphOperationStatus status = childNode.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.OK;
            }
            return status;
        }

        ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = childNode.left().value();
        AdditionalInfoParameterData propertyData = immutablePair.getKey();

        Map<String, String> parameters = propertyData.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            AdditionalInformationDefinition additionalInfoDef = this.convertAdditionalInformationDataToDefinition(propertyData, uniqueId);
            properties.add(additionalInfoDef);
        }

        return JanusGraphOperationStatus.OK;

    }

    private AdditionalInformationDefinition convertAdditionalInformationDataToDefinition(AdditionalInfoParameterData additionalInfoData, String uniqueId) {

        Map<String, String> parameters = additionalInfoData.getParameters();
        Map<String, String> idToKey = additionalInfoData.getIdToKey();

        return new AdditionalInformationDefinition(additionalInfoData.getAdditionalInfoParameterDataDefinition(), uniqueId, convertParameters(parameters, idToKey));
    }

    @Override
    public Either<AdditionalInformationDefinition, StorageOperationStatus> createAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key, String value, boolean inTransaction) {

        Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

        try {

            Either<AdditionalInformationDefinition, JanusGraphOperationStatus> either = this.addAdditionalInformationParameter(nodeType, resourceId, key, value);

            if (either.isRight()) {
                JanusGraphOperationStatus status = either.right().value();
                log.debug("Failed to add additional information property {} to component {}. Status is {}", key, resourceId, status);
                BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("CreateAdditionalInformationParameter", ADDITIONAL_INFORMATION_OF + nodeType.getName() + " " + resourceId, String.valueOf(status));
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            } else {
                AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
                result = Either.left(additionalInformationDefinition);
            }

            return result;
        } finally {
            commitOrRollback(inTransaction, result);
        }

    }

    @Override
    public Either<AdditionalInformationDefinition, StorageOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, String key, String value, boolean inTransaction) {

        Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

        try {

            Either<AdditionalInformationDefinition, JanusGraphOperationStatus> either = this.updateAdditionalInformationParameter(nodeType, resourceId, id, key, value);

            if (either.isRight()) {
                log.info("Failed to update additional information property {} to component {}", key, resourceId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(either.right().value()));
            } else {
                AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
                result = Either.left(additionalInformationDefinition);
            }

            return result;

        } finally {
            commitOrRollback(inTransaction, result);
        }

    }

    @Override
    public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction) {

        Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

        try {

            Either<AdditionalInformationDefinition, JanusGraphOperationStatus> either = this.deleteAdditionalInformationParameter(nodeType, resourceId, id);

            if (either.isRight()) {
                log.error("Failed to delete additional information id {} to component {}", id, resourceId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(either.right().value()));
            } else {
                AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
                result = Either.left(additionalInformationDefinition);
            }

            return result;

        } finally {
            commitOrRollback(inTransaction, result);
        }

    }

    @Override
    public Either<Integer, StorageOperationStatus> getNumberOfAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction) {

        Either<Integer, StorageOperationStatus> result = null;

        try {

            Either<Integer, JanusGraphOperationStatus> either = this.getNumberOfParameters(nodeType, resourceId);

            if (either.isRight()) {
                log.error("Failed to get the number of additional information properties in component {}", resourceId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(either.right().value()));
            } else {
                Integer counter = either.left().value();
                result = Either.left(counter);
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

    @Override
    public Either<Integer, JanusGraphOperationStatus> getNumberOfParameters(NodeTypeEnum nodeType, String resourceId) {

        Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> getResult = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), resourceId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

        if (getResult.isRight()) {
            JanusGraphOperationStatus status = getResult.right().value();
            return Either.right(status);
        }

        ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
        AdditionalInfoParameterData parameterData = immutablePair.getLeft();
        Map<String, String> parameters = parameterData.getParameters();

        Integer counter = 0;
        if (parameters != null) {
            counter = parameters.size();
        }

        return Either.left(counter);

    }

    @Override
    public Either<AdditionalInfoParameterInfo, JanusGraphOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String id) {

        JanusGraphOperationStatus
            verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
        if (verifyNodeTypeVsComponent != JanusGraphOperationStatus.OK) {
            return Either.right(verifyNodeTypeVsComponent);
        }

        Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> getResult = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

        if (getResult.isRight()) {
            JanusGraphOperationStatus status = getResult.right().value();
            return Either.right(status);
        }

        ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
        AdditionalInfoParameterData parameterData = immutablePair.getLeft();
        Map<String, String> parameters = parameterData.getParameters();
        Map<String, String> idToKey = parameterData.getIdToKey();

        if (idToKey == null || !idToKey.containsKey(id)) {
            return Either.right(JanusGraphOperationStatus.INVALID_ID);
        }

        String key = idToKey.get(id);
        String value = parameters.get(key);

        log.trace("The key-value {} = {} was retrieved for id {}", key, value, id);

        Either<AdditionalInfoParameterData, JanusGraphOperationStatus> updateNode = janusGraphGenericDao
            .updateNode(parameterData, AdditionalInfoParameterData.class);

        if (updateNode.isRight()) {
            JanusGraphOperationStatus status = updateNode.right().value();
            if (status != JanusGraphOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("GetAdditionnalInformationParameter", ADDITIONAL_INFORMATION_OF + nodeType.getName() + " " + componentId, String.valueOf(status));
            }
            return Either.right(status);
        }

        AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(id, key, value);

        return Either.left(additionalInfoParameterInfo);

    }

    @Override
    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String componentId, boolean ignoreVerification) {

        if (!ignoreVerification) {
            JanusGraphOperationStatus
                verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
            if (verifyNodeTypeVsComponent != JanusGraphOperationStatus.OK) {
                return Either.right(verifyNodeTypeVsComponent);
            }
        }

        Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> getResult = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

        if (getResult.isRight()) {
            JanusGraphOperationStatus status = getResult.right().value();
            if (status != JanusGraphOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("GetAdditionnalInformationParameters", ADDITIONAL_INFORMATION_OF + nodeType.getName() + " " + componentId, String.valueOf(status));
            }
            return Either.right(status);
        }

        ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
        AdditionalInfoParameterData parameterData = immutablePair.getLeft();
        Map<String, String> parameters = parameterData.getParameters();
        Map<String, String> idToKey = parameterData.getIdToKey();

        AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, parameterData);

        return Either.left(informationDefinition);

    }

    @Override
    public Either<AdditionalInformationDefinition, StorageOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean ignoreVerification, boolean inTransaction) {

        Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

        try {

            Either<AdditionalInformationDefinition, JanusGraphOperationStatus> either = this.getAllAdditionalInformationParameters(nodeType, resourceId, ignoreVerification);

            if (either.isRight()) {
                JanusGraphOperationStatus status = either.right().value();
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            } else {
                AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
                result = Either.left(additionalInformationDefinition);
            }

            return result;

        } finally {
            commitOrRollback(inTransaction, result);
        }

    }

    private void commitOrRollback(boolean inTransaction, Either<? extends Object, StorageOperationStatus> result) {

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
    

    @Override
    public Either<AdditionalInfoParameterInfo, StorageOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction) {

        Either<AdditionalInfoParameterInfo, StorageOperationStatus> result = null;

        try {

            Either<AdditionalInfoParameterInfo, JanusGraphOperationStatus> either = this.getAdditionalInformationParameter(nodeType, resourceId, id);

            if (either.isRight()) {
                log.error("Failed to fetch additional information property with id {} of component {}", id, resourceId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(either.right().value()));
            } else {
                AdditionalInfoParameterInfo additionalInformationDefinition = either.left().value();
                result = Either.left(additionalInformationDefinition);
            }

            return result;

        } finally {
            commitOrRollback(inTransaction, result);
        }
    }

    @Override
    public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction) {

        Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

        try {

            Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, JanusGraphOperationStatus> getResult = janusGraphGenericDao
                .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), resourceId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
                    NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

            if (getResult.isRight()) {
                JanusGraphOperationStatus status = getResult.right().value();
                if (status == JanusGraphOperationStatus.NOT_FOUND) {
                    return Either.right(StorageOperationStatus.OK);
                } else {
                    BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("DeleteAdditionalInformationNode", ADDITIONAL_INFORMATION_OF + nodeType.getName() + " " + resourceId, String.valueOf(status));
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                }
                return result;
            }

            ImmutablePair<AdditionalInfoParameterData, GraphEdge> value = getResult.left().value();
            AdditionalInfoParameterData parameterData = value.getLeft();

            Either<AdditionalInfoParameterData, JanusGraphOperationStatus> deleteNodeRes = janusGraphGenericDao
                .deleteNode(parameterData, AdditionalInfoParameterData.class);
            if (deleteNodeRes.isRight()) {
                JanusGraphOperationStatus status = getResult.right().value();
                BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("DeleteAdditionalInformationNode", (String) parameterData.getUniqueId(), String.valueOf(status));
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                return result;
            }

            AdditionalInformationDefinition informationDefinition = convertAdditionalInformationDataToDefinition(deleteNodeRes.left().value(), resourceId);

            result = Either.left(informationDefinition);

            return result;

        } finally {
            commitOrRollback(inTransaction, result);
        }
    }

    private JanusGraphOperationStatus verifyNodeTypeVsComponent(NodeTypeEnum nodeType, String componentId) {
        Either<JanusGraphVertex, JanusGraphOperationStatus> vertexByProperty = janusGraphGenericDao
            .getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId);
        if (vertexByProperty.isRight()) {
            JanusGraphOperationStatus status = vertexByProperty.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            return status;
        } else {
            Vertex v = vertexByProperty.left().value();
            String label = (String) v.property(GraphPropertiesDictionary.LABEL.getProperty()).value();
            if (label != null) {
                if (!label.equals(nodeType.getName())) {
                    log.debug("The node type {} is not appropriate to component {}", nodeType, componentId);
                    return JanusGraphOperationStatus.INVALID_ID;
                }
            } else {
                log.debug("The node type {}  with id {} does not have a label property.", nodeType, componentId);
                return JanusGraphOperationStatus.INVALID_ID;
            }
        }
        return JanusGraphOperationStatus.OK;
    }

}
