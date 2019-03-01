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

package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.*;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


@org.springframework.stereotype.Component("tosca-operation-facade")
public class ToscaOperationFacade {

    // region - Fields

    private static final String COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR = "Couldn't fetch a component with and UniqueId {}, error: {}";
    private static final String FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS = "Failed to find recently added property {} on the resource {}. Status is {}. ";
    private static final String FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS = "Failed to get updated resource {}. Status is {}. ";
    private static final String FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS = "Failed to add the property {} to the resource {}. Status is {}. ";
    private static final String SERVICE = "service";
    private static final String NOT_SUPPORTED_COMPONENT_TYPE = "Not supported component type {}";
    private static final String COMPONENT_CREATED_SUCCESSFULLY = "Component created successfully!!!";
    private static final String COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR = "Couldn't fetch component with and unique id {}, error: {}";
    @Autowired
    private NodeTypeOperation nodeTypeOperation;
    @Autowired
    private TopologyTemplateOperation topologyTemplateOperation;
    @Autowired
    private NodeTemplateOperation nodeTemplateOperation;
    @Autowired
    private GroupsOperation groupsOperation;
    @Autowired
    private TitanDao titanDao;

    private static final Logger log = Logger.getLogger(ToscaOperationFacade.class.getName());
    // endregion

    // region - ToscaElement - GetById
    public static final String PROXY_SUFFIX = "_proxy";

    public <T extends Component> Either<T, StorageOperationStatus> getToscaFullElement(String componentId) {
        ComponentParametersView filters = new ComponentParametersView();
        filters.setIgnoreCapabiltyProperties(false);
        filters.setIgnoreForwardingPath(false);
        return getToscaElement(componentId, filters);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(String componentId) {

        return getToscaElement(componentId, JsonParseFlagEnum.ParseAll);

    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(String componentId, ComponentParametersView filters) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, filters.detectParseFlag());
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }
        return getToscaElementByOperation(getVertexEither.left().value(), filters);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(String componentId, JsonParseFlagEnum parseFlag) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, parseFlag);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }
        return getToscaElementByOperation(getVertexEither.left().value());
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(GraphVertex componentVertex) {
        return getToscaElementByOperation(componentVertex);
    }

    public Either<Boolean, StorageOperationStatus> validateComponentExists(String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            TitanOperationStatus status = getVertexEither.right().value();
            if (status == TitanOperationStatus.NOT_FOUND) {
                return Either.left(false);
            } else {
                log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));
            }
        }
        return Either.left(true);
    }

    public <T extends Component> Either<T, StorageOperationStatus> findLastCertifiedToscaElementByUUID(T component) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.UUID, component.getUUID());
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        Either<List<GraphVertex>, TitanOperationStatus> getVertexEither = titanDao.getByCriteria(ModelConverter.getVertexType(component), props);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, component.getUniqueId(), getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }
        return getToscaElementByOperation(getVertexEither.left().value().get(0));
    }

    // endregion
    // region - ToscaElement - GetByOperation
    private <T extends Component> Either<T, StorageOperationStatus> getToscaElementByOperation(GraphVertex componentV) {
        return getToscaElementByOperation(componentV, new ComponentParametersView());
    }

    private <T extends Component> Either<T, StorageOperationStatus> getToscaElementByOperation(GraphVertex componentV, ComponentParametersView filters) {
        VertexTypeEnum label = componentV.getLabel();

        ToscaElementOperation toscaOperation = getToscaElementOperation(componentV);
        Either<ToscaElement, StorageOperationStatus> toscaElement;
        String componentId = componentV.getUniqueId();
        if (toscaOperation != null) {
            log.debug("Need to fetch tosca element for id {}", componentId);
            toscaElement = toscaOperation.getToscaElement(componentV, filters);
        } else {
            log.debug("not supported tosca type {} for id {}", label, componentId);
            toscaElement = Either.right(StorageOperationStatus.BAD_REQUEST);
        }
        if (toscaElement.isRight()) {
            return Either.right(toscaElement.right().value());
        }
        return Either.left(ModelConverter.convertFromToscaElement(toscaElement.left().value()));
    }

    // endregion
    private ToscaElementOperation getToscaElementOperation(GraphVertex componentV) {
        VertexTypeEnum label = componentV.getLabel();
        switch (label) {
            case NODE_TYPE:
                return nodeTypeOperation;
            case TOPOLOGY_TEMPLATE:
                return topologyTemplateOperation;
            default:
                return null;
        }
    }

    public <T extends Component> Either<T, StorageOperationStatus> createToscaComponent(T resource) {
        ToscaElement toscaElement = ModelConverter.convertToToscaElement(resource);

        ToscaElementOperation toscaElementOperation = getToscaElementOperation(resource);
        Either<ToscaElement, StorageOperationStatus> createToscaElement = toscaElementOperation.createToscaElement(toscaElement);
        if (createToscaElement.isLeft()) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            T dataModel = ModelConverter.convertFromToscaElement(createToscaElement.left().value());
            return Either.left(dataModel);
        }
        return Either.right(createToscaElement.right().value());
    }

    // region - ToscaElement Delete
    public StorageOperationStatus markComponentToDelete(Component componentToDelete) {

        if ((componentToDelete.getIsDeleted() != null) && componentToDelete.getIsDeleted() && !componentToDelete.isHighestVersion()) {
            // component already marked for delete
            return StorageOperationStatus.OK;
        } else {

            Either<GraphVertex, TitanOperationStatus> getResponse = titanDao.getVertexById(componentToDelete.getUniqueId(), JsonParseFlagEnum.ParseAll);
            if (getResponse.isRight()) {
                log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentToDelete.getUniqueId(), getResponse.right().value());
                return DaoStatusConverter.convertTitanStatusToStorageStatus(getResponse.right().value());

            }
            GraphVertex componentV = getResponse.left().value();

            // same operation for node type and topology template operations
            Either<GraphVertex, StorageOperationStatus> result = nodeTypeOperation.markComponentToDelete(componentV);
            if (result.isRight()) {
                return result.right().value();
            }
            return StorageOperationStatus.OK;
        }
    }

    public <T extends Component> Either<T, StorageOperationStatus> deleteToscaComponent(String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll);
        if (getVertexEither.isRight()) {
            log.debug("Couldn't fetch component vertex with and unique id {}, error: {}", componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }
        Either<ToscaElement, StorageOperationStatus> deleteElement = deleteToscaElement(getVertexEither.left().value());
        if (deleteElement.isRight()) {
            log.debug("Failed to delete component with and unique id {}, error: {}", componentId, deleteElement.right().value());
            return Either.right(deleteElement.right().value());
        }
        T dataModel = ModelConverter.convertFromToscaElement(deleteElement.left().value());

        return Either.left(dataModel);
    }

    private Either<ToscaElement, StorageOperationStatus> deleteToscaElement(GraphVertex componentV) {
        VertexTypeEnum label = componentV.getLabel();
        Either<ToscaElement, StorageOperationStatus> toscaElement;
        Object componentId = componentV.getUniqueId();
        switch (label) {
            case NODE_TYPE:
                log.debug("Need to fetch node type for id {}", componentId);
                toscaElement = nodeTypeOperation.deleteToscaElement(componentV);
                break;
            case TOPOLOGY_TEMPLATE:
                log.debug("Need to fetch topology template for id {}", componentId);
                toscaElement = topologyTemplateOperation.deleteToscaElement(componentV);
                break;
            default:
                log.debug("not supported tosca type {} for id {}", label, componentId);
                toscaElement = Either.right(StorageOperationStatus.BAD_REQUEST);
                break;
        }
        return toscaElement;
    }
    // endregion

    private ToscaElementOperation getToscaElementOperation(Component component) {
        return ModelConverter.isAtomicComponent(component) ? nodeTypeOperation : topologyTemplateOperation;
    }

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByToscaResourceName(String toscaResourceName) {
        return getLatestByName(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getFullLatestComponentByToscaResourceName(String toscaResourceName) {
        ComponentParametersView fetchAllFilter = new ComponentParametersView();
        fetchAllFilter.setIgnoreForwardingPath(true);
        fetchAllFilter.setIgnoreCapabiltyProperties(false);
        return getLatestByName(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName, JsonParseFlagEnum.ParseAll, fetchAllFilter);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByName(String resourceName) {
        return getLatestByName(GraphPropertyEnum.NAME, resourceName);

    }

    public StorageOperationStatus validateCsarUuidUniqueness(String csarUUID) {

        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.CSAR_UUID, csarUUID);

        Either<List<GraphVertex>, TitanOperationStatus> resources = titanDao.getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata);

        if (resources.isRight()) {
            if (resources.right().value() == TitanOperationStatus.NOT_FOUND) {
                return StorageOperationStatus.OK;
            } else {
                log.debug("failed to get resources from graph with property name: {}", csarUUID);
                return DaoStatusConverter.convertTitanStatusToStorageStatus(resources.right().value());
            }
        }
        return StorageOperationStatus.ENTITY_ALREADY_EXISTS;

    }

    public <T extends Component> Either<Set<T>, StorageOperationStatus> getFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, ComponentTypeEnum componentType) {
        Either<List<ToscaElement>, StorageOperationStatus> followedResources;
        if (componentType == ComponentTypeEnum.RESOURCE) {
            followedResources = nodeTypeOperation.getFollowedComponent(userId, lifecycleStates, lastStateStates, componentType);
        } else {
            followedResources = topologyTemplateOperation.getFollowedComponent(userId, lifecycleStates, lastStateStates, componentType);
        }

        Set<T> components = new HashSet<>();
        if (followedResources.isRight() && followedResources.right().value() != StorageOperationStatus.NOT_FOUND) {
            return Either.right(followedResources.right().value());
        }
        if (followedResources.isLeft()) {
            List<ToscaElement> toscaElements = followedResources.left().value();
            toscaElements.forEach(te -> {
                T component = ModelConverter.convertFromToscaElement(te);
                components.add(component);
            });
        }
        return Either.left(components);
    }

    public Either<Resource, StorageOperationStatus> getLatestCertifiedNodeTypeByToscaResourceName(String toscaResourceName) {

        return getLatestCertifiedByToscaResourceName(toscaResourceName, VertexTypeEnum.NODE_TYPE, JsonParseFlagEnum.ParseMetadata);
    }

    public Either<Resource, StorageOperationStatus> getLatestCertifiedByToscaResourceName(String toscaResourceName, VertexTypeEnum vertexType, JsonParseFlagEnum parseFlag) {

        Either<Resource, StorageOperationStatus> result = null;
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        Either<List<GraphVertex>, TitanOperationStatus> getLatestRes = titanDao.getByCriteria(vertexType, props, parseFlag);

        if (getLatestRes.isRight()) {
            TitanOperationStatus status = getLatestRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch {} with name {}. status={} ", vertexType, toscaResourceName, status);
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
        }
        if (result == null) {
            List<GraphVertex> resources = getLatestRes.left().value();
            double version = 0.0;
            GraphVertex highestResource = null;
            for (GraphVertex resource : resources) {
                double resourceVersion = Double.parseDouble((String) resource.getJsonMetadataField(JsonPresentationFields.VERSION));
                if (resourceVersion > version) {
                    version = resourceVersion;
                    highestResource = resource;
                }
            }
            result = getToscaFullElement(highestResource.getUniqueId());
        }
        return result;
    }

    public Either<Boolean, StorageOperationStatus> validateToscaResourceNameExists(String templateName) {
        Either<Boolean, StorageOperationStatus> validateUniquenessRes = validateToscaResourceNameUniqueness(templateName);
        if (validateUniquenessRes.isLeft()) {
            return Either.left(!validateUniquenessRes.left().value());
        }
        return validateUniquenessRes;
    }

    public Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances(String componentId, RequirementCapabilityRelDef requirementDef) {
        return nodeTemplateOperation.dissociateResourceInstances(componentId, requirementDef);
    }

    /**
     * Allows to get fulfilled requirement by relation and received predicate
     */
    public Either<RequirementDataDefinition, StorageOperationStatus> getFulfilledRequirementByRelation(String componentId, String instanceId, RequirementCapabilityRelDef relation, BiPredicate<RelationshipInfo, RequirementDataDefinition> predicate) {
        return nodeTemplateOperation.getFulfilledRequirementByRelation(componentId, instanceId, relation, predicate);
    }

    /**
     * Allows to get fulfilled capability by relation and received predicate
     */
    public Either<CapabilityDataDefinition, StorageOperationStatus> getFulfilledCapabilityByRelation(String componentId, String instanceId, RequirementCapabilityRelDef relation, BiPredicate<RelationshipInfo, CapabilityDataDefinition> predicate) {
        return nodeTemplateOperation.getFulfilledCapabilityByRelation(componentId, instanceId, relation, predicate);
    }

    public StorageOperationStatus associateResourceInstances(String componentId, List<RequirementCapabilityRelDef> relations) {
        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> status = nodeTemplateOperation.associateResourceInstances(componentId, relations);
        if (status.isRight()) {
            return status.right().value();
        }
        return StorageOperationStatus.OK;
    }

    protected Either<Boolean, StorageOperationStatus> validateToscaResourceNameUniqueness(String name) {

        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, name);

        Either<List<GraphVertex>, TitanOperationStatus> resources = titanDao.getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata);

        if (resources.isRight() && resources.right().value() != TitanOperationStatus.NOT_FOUND) {
            log.debug("failed to get resources from graph with property name: {}", name);
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resources.right().value()));
        }
        List<GraphVertex> resourceList = (resources.isLeft() ? resources.left().value() : null);
        if (isNotEmpty(resourceList)) {
            if (log.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder();
                for (GraphVertex resourceData : resourceList) {
                    builder.append(resourceData.getUniqueId() + "|");
                }
                log.debug("resources  with property name:{} exists in graph. found {}", name, builder);
            }
            return Either.left(false);
        } else {
            log.debug("resources  with property name:{} does not exists in graph", name);
            return Either.left(true);
        }

    }

    // region - Component Update

    public Either<Resource, StorageOperationStatus> overrideComponent(Resource newComponent, Resource oldComponent) {

        copyArtifactsToNewComponent(newComponent, oldComponent);

        Either<GraphVertex, TitanOperationStatus> componentVEither = titanDao.getVertexById(oldComponent.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (componentVEither.isRight()) {
            log.debug("Falied to fetch component {} error {}", oldComponent.getUniqueId(), componentVEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(componentVEither.right().value()));
        }
        GraphVertex componentv = componentVEither.left().value();
        Either<GraphVertex, TitanOperationStatus> parentVertexEither = titanDao.getParentVertex(componentv, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
        if (parentVertexEither.isRight() && parentVertexEither.right().value() != TitanOperationStatus.NOT_FOUND) {
            log.debug("Falied to fetch parent version for component {} error {}", oldComponent.getUniqueId(), parentVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(parentVertexEither.right().value()));
        }

        Either<ToscaElement, StorageOperationStatus> deleteToscaComponent = deleteToscaElement(componentv);
        if (deleteToscaComponent.isRight()) {
            log.debug("Falied to remove old component {} error {}", oldComponent.getUniqueId(), deleteToscaComponent.right().value());
            return Either.right(deleteToscaComponent.right().value());
        }
        Either<Resource, StorageOperationStatus> createToscaComponent = createToscaComponent(newComponent);
        if (createToscaComponent.isRight()) {
            log.debug("Falied to create tosca element component {} error {}", newComponent.getUniqueId(), createToscaComponent.right().value());
            return Either.right(createToscaComponent.right().value());
        }
        Resource newElement = createToscaComponent.left().value();
        Either<GraphVertex, TitanOperationStatus> newVersionEither = titanDao.getVertexById(newElement.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (newVersionEither.isRight()) {
            log.debug("Falied to fetch new tosca element component {} error {}", newComponent.getUniqueId(), newVersionEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(newVersionEither.right().value()));
        }
        if (parentVertexEither.isLeft()) {
            GraphVertex previousVersionV = parentVertexEither.left().value();
            TitanOperationStatus createEdge = titanDao.createEdge(previousVersionV, newVersionEither.left().value(), EdgeLabelEnum.VERSION, null);
            if (createEdge != TitanOperationStatus.OK) {
                log.debug("Falied to associate to previous version {} new version {} error {}", previousVersionV.getUniqueId(), newVersionEither.right().value(), createEdge);
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createEdge));
            }
        }
        return Either.left(newElement);
    }

    void copyArtifactsToNewComponent(Resource newComponent, Resource oldComponent) {
        // TODO - check if required
        Map<String, ArtifactDefinition> toscaArtifacts = oldComponent.getToscaArtifacts();
        if (toscaArtifacts != null && !toscaArtifacts.isEmpty()) {
            toscaArtifacts.values().stream().forEach(a -> a.setDuplicated(Boolean.TRUE));
        }
        newComponent.setToscaArtifacts(toscaArtifacts);

        Map<String, ArtifactDefinition> artifacts = oldComponent.getArtifacts();
        if (artifacts != null && !artifacts.isEmpty()) {
            artifacts.values().stream().forEach(a -> a.setDuplicated(Boolean.TRUE));
        }
        newComponent.setArtifacts(artifacts);

        Map<String, ArtifactDefinition> depArtifacts = oldComponent.getDeploymentArtifacts();
        if (depArtifacts != null && !depArtifacts.isEmpty()) {
            depArtifacts.values().stream().forEach(a -> a.setDuplicated(Boolean.TRUE));
        }
        newComponent.setDeploymentArtifacts(depArtifacts);

        newComponent.setGroups(oldComponent.getGroups());
        newComponent.setLastUpdateDate(null);
        newComponent.setHighestVersion(true);
    }

    public <T extends Component> Either<T, StorageOperationStatus> updateToscaElement(T componentToUpdate) {
        return updateToscaElement(componentToUpdate, new ComponentParametersView());
    }

    public <T extends Component> Either<T, StorageOperationStatus> updateToscaElement(T componentToUpdate, ComponentParametersView filterResult) {
        String componentId = componentToUpdate.getUniqueId();
        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex elementV = getVertexEither.left().value();
        ToscaElementOperation toscaElementOperation = getToscaElementOperation(elementV);

        ToscaElement toscaElementToUpdate = ModelConverter.convertToToscaElement(componentToUpdate);
        Either<ToscaElement, StorageOperationStatus> updateToscaElement = toscaElementOperation.updateToscaElement(toscaElementToUpdate, elementV, filterResult);
        if (updateToscaElement.isRight()) {
            log.debug("Failed to update tosca element {} error {}", componentId, updateToscaElement.right().value());
            return Either.right(updateToscaElement.right().value());
        }
        return Either.left(ModelConverter.convertFromToscaElement(updateToscaElement.left().value()));
    }

    private <T extends Component> Either<T, StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName, JsonParseFlagEnum parseFlag) {
        return getLatestByName(property, nodeName, parseFlag, new ComponentParametersView());
    }

    private <T extends Component> Either<T, StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName, JsonParseFlagEnum parseFlag, ComponentParametersView filter) {
        Either<T, StorageOperationStatus> result;

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(property, nodeName);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        Either<List<GraphVertex>, TitanOperationStatus> highestResources = titanDao.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, parseFlag);
        if (highestResources.isRight()) {
            TitanOperationStatus status = highestResources.right().value();
            log.debug("failed to find resource with name {}. status={} ", nodeName, status);
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            return result;
        }

        List<GraphVertex> resources = highestResources.left().value();
        double version = 0.0;
        GraphVertex highestResource = null;
        for (GraphVertex vertex : resources) {
            Object versionObj = vertex.getMetadataProperty(GraphPropertyEnum.VERSION);
            double resourceVersion = Double.parseDouble((String) versionObj);
            if (resourceVersion > version) {
                version = resourceVersion;
                highestResource = vertex;
            }
        }
        return getToscaElementByOperation(highestResource, filter);
    }

    // endregion
    // region - Component Get By ..
    private <T extends Component> Either<T, StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName) {
        return getLatestByName(property, nodeName, JsonParseFlagEnum.ParseMetadata);
    }

    public <T extends Component> Either<List<T>, StorageOperationStatus> getBySystemName(ComponentTypeEnum componentType, String systemName) {

        Either<List<T>, StorageOperationStatus> result = null;
        Either<T, StorageOperationStatus> getComponentRes;
        List<T> components = new ArrayList<>();
        List<GraphVertex> componentVertices;
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(GraphPropertyEnum.SYSTEM_NAME, systemName);
        if (componentType != null)
            propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());

        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        Either<List<GraphVertex>, TitanOperationStatus> getComponentsRes = titanDao.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        if (getComponentsRes.isRight()) {
            TitanOperationStatus status = getComponentsRes.right().value();
            log.debug("Failed to fetch the component with system name {}. Status is {} ", systemName, status);
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
        }
        if (result == null) {
            componentVertices = getComponentsRes.left().value();
            for (GraphVertex componentVertex : componentVertices) {
                getComponentRes = getToscaElementByOperation(componentVertex);
                if (getComponentRes.isRight()) {
                    log.debug("Failed to get the component {}. Status is {} ", componentVertex.getJsonMetadataField(JsonPresentationFields.NAME), getComponentRes.right().value());
                    result = Either.right(getComponentRes.right().value());
                    break;
                }
                T componentBySystemName = getComponentRes.left().value();
                log.debug("Found component, id: {}", componentBySystemName.getUniqueId());
                components.add(componentBySystemName);
            }
        }
        if (result == null) {
            result = Either.left(components);
        }
        return result;
    }

    public <T extends Component> Either<T, StorageOperationStatus> getComponentByNameAndVersion(ComponentTypeEnum componentType, String name, String version) {
        return getComponentByNameAndVersion(componentType, name, version, JsonParseFlagEnum.ParseAll);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getComponentByNameAndVersion(ComponentTypeEnum componentType, String name, String version, JsonParseFlagEnum parseFlag) {
        Either<T, StorageOperationStatus> result;

        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProperties = new EnumMap<>(GraphPropertyEnum.class);

        hasProperties.put(GraphPropertyEnum.NAME, name);
        hasProperties.put(GraphPropertyEnum.VERSION, version);
        hasNotProperties.put(GraphPropertyEnum.IS_DELETED, true);
        if (componentType != null) {
            hasProperties.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        }
        Either<List<GraphVertex>, TitanOperationStatus> getResourceRes = titanDao.getByCriteria(null, hasProperties, hasNotProperties, parseFlag);
        if (getResourceRes.isRight()) {
            TitanOperationStatus status = getResourceRes.right().value();
            log.debug("failed to find resource with name {}, version {}. Status is {} ", name, version, status);
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            return result;
        }
        return getToscaElementByOperation(getResourceRes.left().value().get(0));
    }

    public Either<List<CatalogComponent>, StorageOperationStatus> getCatalogOrArchiveComponents(boolean isCatalog, List<OriginTypeEnum> excludeTypes) {
        List<ResourceTypeEnum> excludedResourceTypes = Optional.ofNullable(excludeTypes).orElse(Collections.emptyList()).stream().filter(type -> !type.equals(OriginTypeEnum.SERVICE)).map(type -> ResourceTypeEnum.getTypeByName(type.name()))
                .collect(Collectors.toList());
        return topologyTemplateOperation.getElementCatalogData(isCatalog, excludedResourceTypes);
    }

    // endregion
    public <T extends Component> Either<List<T>, StorageOperationStatus> getCatalogComponents(ComponentTypeEnum componentType, List<OriginTypeEnum> excludeTypes, boolean isHighestVersions) {
        List<T> components = new ArrayList<>();
        Either<List<ToscaElement>, StorageOperationStatus> catalogDataResult;
        List<ToscaElement> toscaElements = new ArrayList<>();
        List<ResourceTypeEnum> excludedResourceTypes = Optional.ofNullable(excludeTypes).orElse(Collections.emptyList()).stream().filter(type -> !type.equals(OriginTypeEnum.SERVICE)).map(type -> ResourceTypeEnum.getTypeByName(type.name()))
                .collect(Collectors.toList());

        switch (componentType) {
            case RESOURCE:
                catalogDataResult = nodeTypeOperation.getElementCatalogData(ComponentTypeEnum.RESOURCE, excludedResourceTypes, isHighestVersions);
                if (catalogDataResult.isRight()) {
                    return Either.right(catalogDataResult.right().value());
                }
                toscaElements = catalogDataResult.left().value();
                break;
            case SERVICE:
                if (excludeTypes != null && excludeTypes.contains(OriginTypeEnum.SERVICE)) {
                    break;
                }
                catalogDataResult = topologyTemplateOperation.getElementCatalogData(ComponentTypeEnum.SERVICE, null, isHighestVersions);
                if (catalogDataResult.isRight()) {
                    return Either.right(catalogDataResult.right().value());
                }
                toscaElements = catalogDataResult.left().value();
                break;
            default:
                log.debug(NOT_SUPPORTED_COMPONENT_TYPE, componentType);
                return Either.right(StorageOperationStatus.BAD_REQUEST);
        }
        toscaElements.forEach(te -> {
            T component = ModelConverter.convertFromToscaElement(te);
            components.add(component);
        });
        return Either.left(components);
    }

    public Either<List<String>, StorageOperationStatus> deleteMarkedElements(ComponentTypeEnum componentType) {
        Either<List<GraphVertex>, StorageOperationStatus> allComponentsMarkedForDeletion;
        switch (componentType) {
            case RESOURCE:
                allComponentsMarkedForDeletion = nodeTypeOperation.getAllComponentsMarkedForDeletion(componentType);
                break;
            case SERVICE:
            case PRODUCT:
                allComponentsMarkedForDeletion = topologyTemplateOperation.getAllComponentsMarkedForDeletion(componentType);
                break;
            default:
                log.debug(NOT_SUPPORTED_COMPONENT_TYPE, componentType);
                return Either.right(StorageOperationStatus.BAD_REQUEST);
        }
        if (allComponentsMarkedForDeletion.isRight()) {
            return Either.right(allComponentsMarkedForDeletion.right().value());
        }
        List<GraphVertex> allMarked = allComponentsMarkedForDeletion.left().value();
        return Either.left(checkIfInUseAndDelete(allMarked));
    }

    private List<String> checkIfInUseAndDelete(List<GraphVertex> allMarked) {
        final List<EdgeLabelEnum> forbiddenEdgeLabelEnums = Arrays.asList(EdgeLabelEnum.INSTANCE_OF, EdgeLabelEnum.PROXY_OF, EdgeLabelEnum.ALLOTTED_OF);
        List<String> deleted = new ArrayList<>();

        for (GraphVertex elementV : allMarked) {
            boolean isAllowedToDelete = true;

            for (EdgeLabelEnum edgeLabelEnum : forbiddenEdgeLabelEnums) {
                Either<Edge, TitanOperationStatus> belongingEdgeByCriteria = titanDao.getBelongingEdgeByCriteria(elementV, edgeLabelEnum, null);
                if (belongingEdgeByCriteria.isLeft()){
                    log.debug("Marked element {} in use. don't delete it", elementV.getUniqueId());
                    isAllowedToDelete = false;
                    break;
                }
            }

            if (isAllowedToDelete) {
                Either<ToscaElement, StorageOperationStatus> deleteToscaElement = deleteToscaElement(elementV);
                if (deleteToscaElement.isRight()) {
                    log.debug("Failed to delete marked element UniqueID {}, Name {}, error {}", elementV.getUniqueId(), elementV.getMetadataProperties().get(GraphPropertyEnum.NAME), deleteToscaElement.right().value());
                    continue;
                }
                deleted.add(elementV.getUniqueId());
            }
        }
        return deleted;
    }

    public Either<List<String>, StorageOperationStatus> getAllComponentsMarkedForDeletion(ComponentTypeEnum componentType) {
        Either<List<GraphVertex>, StorageOperationStatus> allComponentsMarkedForDeletion;
        switch (componentType) {
            case RESOURCE:
                allComponentsMarkedForDeletion = nodeTypeOperation.getAllComponentsMarkedForDeletion(componentType);
                break;
            case SERVICE:
            case PRODUCT:
                allComponentsMarkedForDeletion = topologyTemplateOperation.getAllComponentsMarkedForDeletion(componentType);
                break;
            default:
                log.debug(NOT_SUPPORTED_COMPONENT_TYPE, componentType);
                return Either.right(StorageOperationStatus.BAD_REQUEST);
        }
        if (allComponentsMarkedForDeletion.isRight()) {
            return Either.right(allComponentsMarkedForDeletion.right().value());
        }
        return Either.left(allComponentsMarkedForDeletion.left().value().stream().map(GraphVertex::getUniqueId).collect(Collectors.toList()));
    }

    // region - Component Update
    public Either<ImmutablePair<Component, String>, StorageOperationStatus> addComponentInstanceToTopologyTemplate(Component containerComponent, Component origComponent, ComponentInstance componentInstance, boolean allowDeleted, User user) {

        Either<ImmutablePair<Component, String>, StorageOperationStatus> result = null;
        Either<ToscaElement, StorageOperationStatus> updateContainerComponentRes = null;
        if (StringUtils.isEmpty(componentInstance.getIcon())) {
            componentInstance.setIcon(origComponent.getIcon());
        }
        String nameToFindForCounter = componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy ? componentInstance.getSourceModelName() + PROXY_SUFFIX : origComponent.getName();
        String nextComponentInstanceCounter = getNextComponentInstanceCounter(containerComponent, nameToFindForCounter);
        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> addResult = nodeTemplateOperation.addComponentInstanceToTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent),
                ModelConverter.convertToToscaElement(origComponent), nextComponentInstanceCounter, componentInstance, allowDeleted, user);

        if (addResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add the component instance {} to container component {}. ", componentInstance.getName(), containerComponent.getName());
            result = Either.right(addResult.right().value());
        }
        if (result == null) {
            updateContainerComponentRes = topologyTemplateOperation.getToscaElement(containerComponent.getUniqueId());
            if (updateContainerComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch updated topology template {} with updated component instance {}. ", containerComponent.getName(), componentInstance.getName());
                result = Either.right(updateContainerComponentRes.right().value());
            }
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateContainerComponentRes.left().value());
            String createdInstanceId = addResult.left().value().getRight();
            CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "The component instance {} has been added to container component {}. ", createdInstanceId, updatedComponent.getName());
            result = Either.left(new ImmutablePair<>(updatedComponent, createdInstanceId));
        }
        return result;
    }

    public StorageOperationStatus associateComponentInstancesToComponent(Component containerComponent, Map<ComponentInstance, Resource> resourcesInstancesMap, boolean allowDeleted) {

        StorageOperationStatus result = null;
        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Going to add component instances to component {}", containerComponent.getUniqueId());

        Either<GraphVertex, TitanOperationStatus> metadataVertex = titanDao.getVertexById(containerComponent.getUniqueId(), JsonParseFlagEnum.ParseAll);
        if (metadataVertex.isRight()) {
            TitanOperationStatus status = metadataVertex.right().value();
            if (status == TitanOperationStatus.NOT_FOUND) {
                status = TitanOperationStatus.INVALID_ID;
            }
            result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
        }
        if (result == null) {
            result = nodeTemplateOperation.associateComponentInstancesToComponent(containerComponent, resourcesInstancesMap, metadataVertex.left().value(), allowDeleted);
        }
        return result;
    }

    public Either<ImmutablePair<Component, String>, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(Component containerComponent, Component origComponent, ComponentInstance componentInstance) {

        Either<ImmutablePair<Component, String>, StorageOperationStatus> result = null;

        CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to update the metadata of the component instance {} belonging to container component {}. ", componentInstance.getName(), containerComponent.getName());
        componentInstance.setIcon(origComponent.getIcon());
        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> updateResult = nodeTemplateOperation.updateComponentInstanceMetadataOfTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent),
                ModelConverter.convertToToscaElement(origComponent), componentInstance);
        if (updateResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the metadata of the component instance {} belonging to container component {}. ", componentInstance.getName(), containerComponent.getName());
            result = Either.right(updateResult.right().value());
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateResult.left().value().getLeft());
            String createdInstanceId = updateResult.left().value().getRight();
            CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "The metadata of the component instance {} has been updated to container component {}. ", createdInstanceId, updatedComponent.getName());
            result = Either.left(new ImmutablePair<>(updatedComponent, createdInstanceId));
        }
        return result;
    }

    public Either<Component, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(Component containerComponent) {
        return updateComponentInstanceMetadataOfTopologyTemplate(containerComponent, new ComponentParametersView());
    }

    public Either<Component, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(Component containerComponent, ComponentParametersView filter) {

        Either<Component, StorageOperationStatus> result = null;

        CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to update the metadata  belonging to container component {}. ", containerComponent.getName());

        Either<TopologyTemplate, StorageOperationStatus> updateResult = nodeTemplateOperation.updateComponentInstanceMetadataOfTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent), filter);
        if (updateResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the metadata  belonging to container component {}. ", containerComponent.getName());
            result = Either.right(updateResult.right().value());
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateResult.left().value());
            CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "The metadata has been updated to container component {}. ", updatedComponent.getName());
            result = Either.left(updatedComponent);
        }
        return result;
    }
    // endregion

    public Either<ImmutablePair<Component, String>, StorageOperationStatus> deleteComponentInstanceFromTopologyTemplate(Component containerComponent, String resourceInstanceId) {

        Either<ImmutablePair<Component, String>, StorageOperationStatus> result = null;

        CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to delete the component instance {} belonging to container component {}. ", resourceInstanceId, containerComponent.getName());

        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> updateResult = nodeTemplateOperation.deleteComponentInstanceFromTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent), resourceInstanceId);
        if (updateResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete the component instance {} belonging to container component {}. ", resourceInstanceId, containerComponent.getName());
            result = Either.right(updateResult.right().value());
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateResult.left().value().getLeft());
            String deletedInstanceId = updateResult.left().value().getRight();
            CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "The component instance {} has been deleted from container component {}. ", deletedInstanceId, updatedComponent.getName());
            result = Either.left(new ImmutablePair<>(updatedComponent, deletedInstanceId));
        }
        return result;
    }

    private String getNextComponentInstanceCounter(Component containerComponent, String originResourceName) {
        Integer nextCounter = 0;
        if (CollectionUtils.isNotEmpty(containerComponent.getComponentInstances())) {
            String normalizedName = ValidationUtils.normalizeComponentInstanceName(originResourceName);
            Integer maxCounter = getMaxCounterFromNamesAndIds(containerComponent, normalizedName);
            if (maxCounter != null) {
                nextCounter = maxCounter + 1;
            }
        }
        return nextCounter.toString();
    }

    /**
     * @return max counter of component instance Id's, null if not found
     */
    private Integer getMaxCounterFromNamesAndIds(Component containerComponent, String normalizedName) {
        List<String> countersInNames = containerComponent.getComponentInstances().stream()
                .filter(ci -> ci.getNormalizedName() != null && ci.getNormalizedName().startsWith(normalizedName))
                .map(ci -> ci.getNormalizedName().split(normalizedName)[1])
                .collect(Collectors.toList());
        List<String> countersInIds = containerComponent.getComponentInstances().stream()
                .filter(ci -> ci.getUniqueId() != null && ci.getUniqueId().contains(normalizedName))
                .map(ci -> ci.getUniqueId().split(normalizedName)[1])
                .collect(Collectors.toList());
        List<String> namesAndIdsList = new ArrayList<>(countersInNames);
        namesAndIdsList.addAll(countersInIds);
        return getMaxInteger(namesAndIdsList);
    }

    private Integer getMaxInteger(List<String> counters) {
        Integer maxCounter = 0;
        Integer currCounter = null;
        for (String counter : counters) {
            try {
                currCounter = Integer.parseInt(counter);
                if (maxCounter < currCounter) {
                    maxCounter = currCounter;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return currCounter == null ? null : maxCounter;
    }

    public Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances(String componentId, RequirementCapabilityRelDef requirementDef) {
        return nodeTemplateOperation.associateResourceInstances(componentId, requirementDef);

    }

    public Either<List<InputDefinition>, StorageOperationStatus> createAndAssociateInputs(Map<String, InputDefinition> inputs, String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }

        GraphVertex vertex = getVertexEither.left().value();
        Map<String, PropertyDataDefinition> inputsMap = inputs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new PropertyDataDefinition(e.getValue())));

        StorageOperationStatus status = topologyTemplateOperation.associateInputsToComponent(vertex, inputsMap, componentId);

        if (StorageOperationStatus.OK == status) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            List<InputDefinition> inputsResList = null;
            if (inputsMap != null && !inputsMap.isEmpty()) {
                inputsResList = inputsMap.values().stream()
                        .map(InputDefinition::new)
                        .collect(Collectors.toList());
            }
            return Either.left(inputsResList);
        }
        return Either.right(status);

    }

    public Either<List<InputDefinition>, StorageOperationStatus> addInputsToComponent(Map<String, InputDefinition> inputs, String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }

        GraphVertex vertex = getVertexEither.left().value();
        Map<String, PropertyDataDefinition> inputsMap = inputs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new PropertyDataDefinition(e.getValue())));

        StorageOperationStatus status = topologyTemplateOperation.addToscaDataToToscaElement(vertex, EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, inputsMap, JsonPresentationFields.NAME);

        if (StorageOperationStatus.OK == status) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            List<InputDefinition> inputsResList = null;
            if (inputsMap != null && !inputsMap.isEmpty()) {
                inputsResList = inputsMap.values().stream().map(InputDefinition::new).collect(Collectors.toList());
            }
            return Either.left(inputsResList);
        }
        return Either.right(status);

    }

	public Either<List<InputDefinition>, StorageOperationStatus> getComponentInputs(String componentId) {

		Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
		if (getVertexEither.isRight()) {
			log.debug("Couldn't fetch component with and unique id {}, error: {}", componentId, getVertexEither.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

		}

		Either<ToscaElement, StorageOperationStatus> toscaElement =
				topologyTemplateOperation.getToscaElement(componentId);
		if(toscaElement.isRight()) {
			return Either.right(toscaElement.right().value());
		}

		TopologyTemplate topologyTemplate = (TopologyTemplate) toscaElement.left().value();

		Map<String, PropertyDataDefinition> inputsMap = topologyTemplate.getInputs();

		List<InputDefinition> inputs = new ArrayList<>();
		if(MapUtils.isNotEmpty(inputsMap)) {
			inputs =
					inputsMap.values().stream().map(p -> new InputDefinition(p)).collect(Collectors.toList());
		}

		return Either.left(inputs);
	}

	public Either<List<InputDefinition>, StorageOperationStatus> updateInputsToComponent(List<InputDefinition> inputs, String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }

        GraphVertex vertex = getVertexEither.left().value();
        List<PropertyDataDefinition> inputsAsDataDef = inputs.stream().map(PropertyDataDefinition::new).collect(Collectors.toList());

        StorageOperationStatus status = topologyTemplateOperation.updateToscaDataOfToscaElement(vertex, EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, inputsAsDataDef, JsonPresentationFields.NAME);

        if (StorageOperationStatus.OK == status) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            List<InputDefinition> inputsResList = null;
            if (inputsAsDataDef != null && !inputsAsDataDef.isEmpty()) {
                inputsResList = inputsAsDataDef.stream().map(InputDefinition::new).collect(Collectors.toList());
            }
            return Either.left(inputsResList);
        }
        return Either.right(status);

    }

    // region - ComponentInstance
    public Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> associateComponentInstancePropertiesToComponent(Map<String, List<ComponentInstanceProperty>> instProperties, String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }

        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapPropertiesDataDefinition> instPropsMap = new HashMap<>();
        if (instProperties != null) {

            MapPropertiesDataDefinition propertiesMap;
            for (Entry<String, List<ComponentInstanceProperty>> entry : instProperties.entrySet()) {
                propertiesMap = new MapPropertiesDataDefinition();

                propertiesMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, e -> e)));

                instPropsMap.put(entry.getKey(), propertiesMap);
            }
        }

        StorageOperationStatus status = topologyTemplateOperation.associateInstPropertiesToComponent(vertex, instPropsMap);

        if (StorageOperationStatus.OK == status) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            return Either.left(instProperties);
        }
        return Either.right(status);

    }

    /**
     * saves the instInputs as the updated instance inputs of the component container in DB
     */
    public Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> updateComponentInstanceInputsToComponent(Map<String, List<ComponentInstanceInput>> instInputs, String componentId) {
        if (instInputs == null || instInputs.isEmpty()) {
            return Either.left(instInputs);
        }
        StorageOperationStatus status;
        for (Entry<String, List<ComponentInstanceInput>> inputsPerIntance : instInputs.entrySet()) {
            List<ComponentInstanceInput> toscaDataListPerInst = inputsPerIntance.getValue();
            List<String> pathKeysPerInst = new ArrayList<>();
            pathKeysPerInst.add(inputsPerIntance.getKey());
            status = topologyTemplateOperation.updateToscaDataDeepElementsOfToscaElement(componentId, EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, toscaDataListPerInst, pathKeysPerInst, JsonPresentationFields.NAME);
            if (status != StorageOperationStatus.OK) {
                log.debug("Failed to update component instance inputs for instance {} in component {} edge type {} error {}", inputsPerIntance.getKey(), componentId, EdgeLabelEnum.INST_INPUTS, status);
                return Either.right(status);
            }
        }

        return Either.left(instInputs);
    }

    /**
     * saves the instProps as the updated instance properties of the component container in DB
     */
    public Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> updateComponentInstancePropsToComponent(Map<String, List<ComponentInstanceProperty>> instProps, String componentId) {
        if (instProps == null || instProps.isEmpty()) {
            return Either.left(instProps);
        }
        StorageOperationStatus status;
        for (Entry<String, List<ComponentInstanceProperty>> propsPerIntance : instProps.entrySet()) {
            List<ComponentInstanceProperty> toscaDataListPerInst = propsPerIntance.getValue();
            List<String> pathKeysPerInst = new ArrayList<>();
            pathKeysPerInst.add(propsPerIntance.getKey());
            status = topologyTemplateOperation.updateToscaDataDeepElementsOfToscaElement(componentId, EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES, toscaDataListPerInst, pathKeysPerInst, JsonPresentationFields.NAME);
            if (status != StorageOperationStatus.OK) {
                log.debug("Failed to update component instance inputs for instance {} in component {} edge type {} error {}", propsPerIntance.getKey(), componentId, EdgeLabelEnum.INST_PROPERTIES, status);
                return Either.right(status);
            }
        }

        return Either.left(instProps);
    }

    public Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> associateComponentInstanceInputsToComponent(Map<String, List<ComponentInstanceInput>> instInputs, String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapPropertiesDataDefinition> instPropsMap = new HashMap<>();
        if (instInputs != null) {

            MapPropertiesDataDefinition propertiesMap;
            for (Entry<String, List<ComponentInstanceInput>> entry : instInputs.entrySet()) {
                propertiesMap = new MapPropertiesDataDefinition();

                propertiesMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, e -> e)));

                instPropsMap.put(entry.getKey(), propertiesMap);
            }
        }

        StorageOperationStatus status = topologyTemplateOperation.associateInstInputsToComponent(vertex, instPropsMap);

        if (StorageOperationStatus.OK == status) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            return Either.left(instInputs);
        }
        return Either.right(status);

    }

    public Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addComponentInstanceInputsToComponent(Component containerComponent, Map<String, List<ComponentInstanceInput>> instProperties) {
        requireNonNull(instProperties);
        StorageOperationStatus status;
        for (Entry<String, List<ComponentInstanceInput>> entry : instProperties.entrySet()) {
            List<ComponentInstanceInput> props = entry.getValue();
            String componentInstanceId = entry.getKey();
            if (!isEmpty(props)) {
                for (ComponentInstanceInput property : props) {
                    List<ComponentInstanceInput> componentInstancesInputs = containerComponent.getComponentInstancesInputs().get(componentInstanceId);
                    Optional<ComponentInstanceInput> instanceProperty = componentInstancesInputs.stream()
                            .filter(p -> p.getName().equals(property.getName()))
                            .findAny();
                    if (instanceProperty.isPresent()) {
                        status = updateComponentInstanceInput(containerComponent, componentInstanceId, property);
                    } else {
                        status = addComponentInstanceInput(containerComponent, componentInstanceId, property);
                    }
                    if (status != StorageOperationStatus.OK) {
                        log.debug("Failed to update instance input {} for instance {} error {} ", property, componentInstanceId, status);
                        return Either.right(status);
                    } else {
                        log.trace("instance input {} for instance {} updated", property, componentInstanceId);
                    }
                }
            }
        }
        return Either.left(instProperties);
    }

    public Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addComponentInstancePropertiesToComponent(Component containerComponent, Map<String, List<ComponentInstanceProperty>> instProperties) {
        requireNonNull(instProperties);
        StorageOperationStatus status;
        for (Entry<String, List<ComponentInstanceProperty>> entry : instProperties.entrySet()) {
            List<ComponentInstanceProperty> props = entry.getValue();
            String componentInstanceId = entry.getKey();
            List<ComponentInstanceProperty> instanceProperties = containerComponent.getComponentInstancesProperties().get(componentInstanceId);
            if (!isEmpty(props)) {
                for (ComponentInstanceProperty property : props) {
                    Optional<ComponentInstanceProperty> instanceProperty = instanceProperties.stream()
                            .filter(p -> p.getUniqueId().equals(property.getUniqueId()))
                            .findAny();
                    if (instanceProperty.isPresent()) {
                        status = updateComponentInstanceProperty(containerComponent, componentInstanceId, property);
                    } else {
                        status = addComponentInstanceProperty(containerComponent, componentInstanceId, property);
                    }
                    if (status != StorageOperationStatus.OK) {
                        log.debug("Failed to update instance property {} for instance {} error {} ", property, componentInstanceId, status);
                        return Either.right(status);
                    }
                }
            }
        }
        return Either.left(instProperties);
    }

    public StorageOperationStatus associateDeploymentArtifactsToInstances(Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts, String componentId, User user) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value());

        }

        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapArtifactDataDefinition> instArtMap = new HashMap<>();
        if (instDeploymentArtifacts != null) {

            MapArtifactDataDefinition artifactsMap;
            for (Entry<String, Map<String, ArtifactDefinition>> entry : instDeploymentArtifacts.entrySet()) {
                Map<String, ArtifactDefinition> artList = entry.getValue();
                Map<String, ArtifactDataDefinition> artifacts = artList.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                artifactsMap = nodeTemplateOperation.prepareInstDeploymentArtifactPerInstance(artifacts, entry.getKey(), user, NodeTemplateOperation.HEAT_VF_ENV_NAME);

                instArtMap.put(entry.getKey(), artifactsMap);
            }
        }

        return topologyTemplateOperation.associateInstDeploymentArtifactsToComponent(vertex, instArtMap);

    }

    public StorageOperationStatus associateArtifactsToInstances(Map<String, Map<String, ArtifactDefinition>> instArtifacts, String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value());

        }

        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapArtifactDataDefinition> instArtMap = new HashMap<>();
        if (instArtifacts != null) {

            MapArtifactDataDefinition artifactsMap;
            for (Entry<String, Map<String, ArtifactDefinition>> entry : instArtifacts.entrySet()) {
                Map<String, ArtifactDefinition> artList = entry.getValue();
                Map<String, ArtifactDataDefinition> artifacts = artList.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                artifactsMap = new MapArtifactDataDefinition(artifacts);

                instArtMap.put(entry.getKey(), artifactsMap);
            }
        }

        return topologyTemplateOperation.associateInstArtifactsToComponent(vertex, instArtMap);

    }

    public StorageOperationStatus associateInstAttributeToComponentToInstances(Map<String, List<PropertyDefinition>> instArttributes, String componentId) {

        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value());

        }

        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapPropertiesDataDefinition> instAttr = new HashMap<>();
        if (instArttributes != null) {

            MapPropertiesDataDefinition attributesMap;
            for (Entry<String, List<PropertyDefinition>> entry : instArttributes.entrySet()) {
                attributesMap = new MapPropertiesDataDefinition();
                attributesMap.setMapToscaDataDefinition(entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, e -> e)));
                instAttr.put(entry.getKey(), attributesMap);
            }
        }

        return topologyTemplateOperation.associateInstAttributeToComponent(vertex, instAttr);

    }
    // endregion

    public StorageOperationStatus associateOrAddCalculatedCapReq(Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties, Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instReg, String componentId) {
        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value());

        }

        GraphVertex vertex = getVertexEither.left().value();

        Map<String, MapListRequirementDataDefinition> calcRequirements = new HashMap<>();

        Map<String, MapListCapabilityDataDefinition> calcCapabilty = new HashMap<>();
        Map<String, MapCapabilityProperty> calculatedCapabilitiesProperties = new HashMap<>();
        if (instCapabilties != null) {
            for (Entry<ComponentInstance, Map<String, List<CapabilityDefinition>>> entry : instCapabilties.entrySet()) {

                Map<String, List<CapabilityDefinition>> caps = entry.getValue();
                Map<String, ListCapabilityDataDefinition> mapToscaDataDefinition = new HashMap<>();
                for (Entry<String, List<CapabilityDefinition>> instCapability : caps.entrySet()) {
                    mapToscaDataDefinition.put(instCapability.getKey(), new ListCapabilityDataDefinition(instCapability.getValue().stream().map(CapabilityDataDefinition::new).collect(Collectors.toList())));
                }

                ComponentInstanceDataDefinition componentInstance = new ComponentInstanceDataDefinition(entry.getKey());
                MapListCapabilityDataDefinition capMap = nodeTemplateOperation.prepareCalculatedCapabiltyForNodeType(mapToscaDataDefinition, componentInstance);

                MapCapabilityProperty mapCapabilityProperty = ModelConverter.convertToMapOfMapCapabiltyProperties(caps, componentInstance.getUniqueId(), true);

                calcCapabilty.put(entry.getKey().getUniqueId(), capMap);
                calculatedCapabilitiesProperties.put(entry.getKey().getUniqueId(), mapCapabilityProperty);
            }
        }

        if (instReg != null) {
            for (Entry<ComponentInstance, Map<String, List<RequirementDefinition>>> entry : instReg.entrySet()) {

                Map<String, List<RequirementDefinition>> req = entry.getValue();
                Map<String, ListRequirementDataDefinition> mapToscaDataDefinition = new HashMap<>();
                for (Entry<String, List<RequirementDefinition>> instReq : req.entrySet()) {
                    mapToscaDataDefinition.put(instReq.getKey(), new ListRequirementDataDefinition(instReq.getValue().stream().map(RequirementDataDefinition::new).collect(Collectors.toList())));
                }

                MapListRequirementDataDefinition capMap = nodeTemplateOperation.prepareCalculatedRequirementForNodeType(mapToscaDataDefinition, new ComponentInstanceDataDefinition(entry.getKey()));

                calcRequirements.put(entry.getKey().getUniqueId(), capMap);
            }
        }

        return topologyTemplateOperation.associateOrAddCalcCapReqToComponent(vertex, calcRequirements, calcCapabilty, calculatedCapabilitiesProperties);
    }

    private Either<List<Service>, StorageOperationStatus> getLatestVersionNonCheckoutServicesMetadataOnly(Map<GraphPropertyEnum, Object> hasProps, Map<GraphPropertyEnum, Object> hasNotProps) {
        List<Service> services = new ArrayList<>();
        List<LifecycleStateEnum> states = new ArrayList<>();
        // include props
        hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        hasProps.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        // exclude props
        states.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        hasNotProps.put(GraphPropertyEnum.STATE, states);
        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        hasNotProps.put(GraphPropertyEnum.IS_ARCHIVED, true);
        return fetchServicesByCriteria(services, hasProps, hasNotProps);
    }

    private Either<List<Component>, StorageOperationStatus> getLatestVersionNotAbstractToscaElementsMetadataOnly(boolean isAbstract, ComponentTypeEnum componentTypeEnum, String internalComponentType, VertexTypeEnum vertexType) {
        List<Service> services = null;
        Map<GraphPropertyEnum, Object> hasProps = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProps = new EnumMap<>(GraphPropertyEnum.class);
        fillPropsMap(hasProps, hasNotProps, internalComponentType, componentTypeEnum, isAbstract, vertexType);
        Either<List<GraphVertex>, TitanOperationStatus> getRes = titanDao.getByCriteria(vertexType, hasProps, hasNotProps, JsonParseFlagEnum.ParseMetadata);
        if (getRes.isRight()) {
            if (getRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
                return Either.left(new ArrayList<>());
            } else {
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getRes.right().value()));
            }
        }
        // region -> Fetch non checked-out services
        if (internalComponentType != null && internalComponentType.toLowerCase().trim().equals(SERVICE) && VertexTypeEnum.NODE_TYPE == vertexType) {
            Either<List<Service>, StorageOperationStatus> result = getLatestVersionNonCheckoutServicesMetadataOnly(new EnumMap<>(GraphPropertyEnum.class), new EnumMap<>(GraphPropertyEnum.class));
            if (result.isRight()) {
                log.debug("Failed to fetch services for");
                return Either.right(result.right().value());
            }
            services = result.left().value();
            if (log.isTraceEnabled() && isEmpty(services))
                log.trace("No relevant services available");
        }
        // endregion
        List<Component> nonAbstractLatestComponents = new ArrayList<>();
        ComponentParametersView params = new ComponentParametersView(true);
        params.setIgnoreAllVersions(false);
        for (GraphVertex vertexComponent : getRes.left().value()) {
            Either<ToscaElement, StorageOperationStatus> componentRes = topologyTemplateOperation.getLightComponent(vertexComponent, componentTypeEnum, params);
            if (componentRes.isRight()) {
                log.debug("Failed to fetch light element for {} error {}", vertexComponent.getUniqueId(), componentRes.right().value());
                return Either.right(componentRes.right().value());
            } else {
                Component component = ModelConverter.convertFromToscaElement(componentRes.left().value());
                nonAbstractLatestComponents.add(component);
            }
        }
        if (CollectionUtils.isNotEmpty(services)) {
            nonAbstractLatestComponents.addAll(services);
        }
        return Either.left(nonAbstractLatestComponents);
    }

    public Either<ComponentMetadataData, StorageOperationStatus> getLatestComponentMetadataByUuid(String componentUuid, JsonParseFlagEnum parseFlag, Boolean isHighest) {

        Either<ComponentMetadataData, StorageOperationStatus> result;
        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        hasProperties.put(GraphPropertyEnum.UUID, componentUuid);
        if (isHighest != null) {
            hasProperties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, isHighest);
        }
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_ARCHIVED, true); //US382674, US382683

        Either<List<GraphVertex>, TitanOperationStatus> getRes = titanDao.getByCriteria(null, hasProperties, propertiesNotToMatch, parseFlag);
        if (getRes.isRight()) {
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getRes.right().value()));
        } else {
            List<ComponentMetadataData> latestVersionList = getRes.left().value().stream().map(ModelConverter::convertToComponentMetadata).collect(Collectors.toList());
            ComponentMetadataData latestVersion = latestVersionList.size() == 1 ? latestVersionList.get(0)
                    : latestVersionList.stream().max((c1, c2) -> Double.compare(Double.parseDouble(c1.getMetadataDataDefinition().getVersion()), Double.parseDouble(c2.getMetadataDataDefinition().getVersion()))).get();
            result = Either.left(latestVersion);
        }
        return result;
    }

    public Either<ComponentMetadataData, StorageOperationStatus> getComponentMetadata(String componentId) {
        Either<ComponentMetadataData, StorageOperationStatus> result;
        Either<GraphVertex, TitanOperationStatus> getRes = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
        if (getRes.isRight()) {
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getRes.right().value()));
        } else {
            ComponentMetadataData componentMetadata = ModelConverter.convertToComponentMetadata(getRes.left().value());
            result = Either.left(componentMetadata);
        }
        return result;
    }

    public Either<List<Component>, StorageOperationStatus> getLatestVersionNotAbstractComponents(boolean isAbstract, ComponentTypeEnum componentTypeEnum,
                                                                                                 String internalComponentType, List<String> componentUids) {

        List<Component> components = new ArrayList<>();
        if (componentUids == null) {
            Either<List<String>, StorageOperationStatus> componentUidsRes = getComponentUids(isAbstract, componentTypeEnum, internalComponentType);
            if (componentUidsRes.isRight()) {
                return Either.right(componentUidsRes.right().value());
            }
            componentUids = componentUidsRes.left().value();
        }
        if (!isEmpty(componentUids)) {
            for (String componentUid : componentUids) {
                ComponentParametersView componentParametersView = buildComponentViewForNotAbstract();
                if ("vl".equalsIgnoreCase(internalComponentType)) {
                    componentParametersView.setIgnoreCapabilities(false);
                    componentParametersView.setIgnoreRequirements(false);
                }
                Either<ToscaElement, StorageOperationStatus> getToscaElementRes = nodeTemplateOperation.getToscaElementOperation(componentTypeEnum).getLightComponent(componentUid, componentTypeEnum, componentParametersView);
                if (getToscaElementRes.isRight()) {
                    log.debug("Failed to fetch resource for error is {}", getToscaElementRes.right().value());
                    return Either.right(getToscaElementRes.right().value());
                }
                Component component = ModelConverter.convertFromToscaElement(getToscaElementRes.left().value());
                nullifySomeComponentProperties(component);
                components.add(component);
            }
        }
        return Either.left(components);
    }

    public void nullifySomeComponentProperties(Component component) {
        component.setContactId(null);
        component.setCreationDate(null);
        component.setCreatorUserId(null);
        component.setCreatorFullName(null);
        component.setLastUpdateDate(null);
        component.setLastUpdaterUserId(null);
        component.setLastUpdaterFullName(null);
        component.setNormalizedName(null);
    }

    private Either<List<String>, StorageOperationStatus> getComponentUids(boolean isAbstract, ComponentTypeEnum componentTypeEnum, String internalComponentType) {

        Either<List<Component>, StorageOperationStatus> getToscaElementsRes = getLatestVersionNotAbstractMetadataOnly(isAbstract, componentTypeEnum, internalComponentType);
        if (getToscaElementsRes.isRight()) {
            return Either.right(getToscaElementsRes.right().value());
        }
        List<Component> collection = getToscaElementsRes.left().value();
        List<String> componentUids;
        if (collection == null) {
            componentUids = new ArrayList<>();
        } else {
            componentUids = collection.stream()
                    .map(Component::getUniqueId)
                    .collect(Collectors.toList());
        }
        return Either.left(componentUids);
    }

    private ComponentParametersView buildComponentViewForNotAbstract() {
        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreCategories(false);
        componentParametersView.setIgnoreAllVersions(false);
        return componentParametersView;
    }

    public Either<Boolean, StorageOperationStatus> validateComponentNameExists(String name, ResourceTypeEnum resourceType, ComponentTypeEnum componentType) {
        Either<Boolean, StorageOperationStatus> result = validateComponentNameUniqueness(name, resourceType, componentType);
        if (result.isLeft()) {
            result = Either.left(!result.left().value());
        }
        return result;
    }

    public Either<Boolean, StorageOperationStatus> validateComponentNameUniqueness(String name, ResourceTypeEnum resourceType, ComponentTypeEnum componentType) {
        VertexTypeEnum vertexType = ModelConverter.isAtomicComponent(resourceType) ? VertexTypeEnum.NODE_TYPE : VertexTypeEnum.TOPOLOGY_TEMPLATE;
        String normalizedName = ValidationUtils.normaliseComponentName(name);
        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.NORMALIZED_NAME, normalizedName);
        properties.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());

        Either<List<GraphVertex>, TitanOperationStatus> vertexEither = titanDao.getByCriteria(vertexType, properties, JsonParseFlagEnum.NoParse);
        if (vertexEither.isRight() && vertexEither.right().value() != TitanOperationStatus.NOT_FOUND) {
            log.debug("failed to get vertex from graph with property normalizedName: {}", normalizedName);
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexEither.right().value()));
        }
        List<GraphVertex> vertexList = vertexEither.isLeft() ? vertexEither.left().value() : null;
        if (vertexList != null && !vertexList.isEmpty()) {
            return Either.left(false);
        } else {
            return Either.left(true);
        }
    }

    private void fillNodeTypePropsMap(Map<GraphPropertyEnum, Object> hasProps, Map<GraphPropertyEnum, Object> hasNotProps, String internalComponentType) {
        switch (internalComponentType.toLowerCase()) {
            case "vf":
            case "cvfc":
                hasNotProps.put(GraphPropertyEnum.RESOURCE_TYPE, Arrays.asList(ResourceTypeEnum.VFCMT.name(), ResourceTypeEnum.Configuration.name()));
                break;
            case SERVICE:
            case "pnf":
            case "cr":
                hasNotProps.put(GraphPropertyEnum.RESOURCE_TYPE, Arrays.asList(ResourceTypeEnum.VFC.name(), ResourceTypeEnum.VFCMT.name()));
                break;
            case "vl":
                hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VL.name());
                break;
            default:
                break;
        }
    }

    private void fillTopologyTemplatePropsMap(Map<GraphPropertyEnum, Object> hasProps, Map<GraphPropertyEnum, Object> hasNotProps, ComponentTypeEnum componentTypeEnum) {
        switch (componentTypeEnum) {
            case RESOURCE:
                hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
                break;
            case SERVICE:
                hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
                break;
            default:
                break;
        }
        hasNotProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.CVFC.name());
    }

    private void fillPropsMap(Map<GraphPropertyEnum, Object> hasProps, Map<GraphPropertyEnum, Object> hasNotProps, String internalComponentType, ComponentTypeEnum componentTypeEnum, boolean isAbstract, VertexTypeEnum internalVertexType) {
        hasNotProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());

        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        hasNotProps.put(GraphPropertyEnum.IS_ARCHIVED, true);
        hasProps.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        if (VertexTypeEnum.NODE_TYPE == internalVertexType) {
            hasProps.put(GraphPropertyEnum.IS_ABSTRACT, isAbstract);
            if (internalComponentType != null) {
                fillNodeTypePropsMap(hasProps, hasNotProps, internalComponentType);
            }
        } else {
            fillTopologyTemplatePropsMap(hasProps, hasNotProps, componentTypeEnum);
        }
    }

    private List<VertexTypeEnum> getInternalVertexTypes(ComponentTypeEnum componentTypeEnum, String internalComponentType) {
        List<VertexTypeEnum> internalVertexTypes = new ArrayList<>();
        if (ComponentTypeEnum.RESOURCE == componentTypeEnum) {
            internalVertexTypes.add(VertexTypeEnum.NODE_TYPE);
        }
        if (ComponentTypeEnum.SERVICE == componentTypeEnum || SERVICE.equalsIgnoreCase(internalComponentType)) {
            internalVertexTypes.add(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        }
        return internalVertexTypes;
    }

    public Either<List<Component>, StorageOperationStatus> getLatestVersionNotAbstractMetadataOnly(boolean isAbstract, ComponentTypeEnum componentTypeEnum, String internalComponentType) {
        List<VertexTypeEnum> internalVertexTypes = getInternalVertexTypes(componentTypeEnum, internalComponentType);
        List<Component> result = new ArrayList<>();
        for (VertexTypeEnum vertexType : internalVertexTypes) {
            Either<List<Component>, StorageOperationStatus> listByVertexType = getLatestVersionNotAbstractToscaElementsMetadataOnly(isAbstract, componentTypeEnum, internalComponentType, vertexType);
            if (listByVertexType.isRight()) {
                return listByVertexType;
            }
            result.addAll(listByVertexType.left().value());
        }
        return Either.left(result);

    }

    private Either<List<Component>, StorageOperationStatus> getLatestComponentListByUuid(String componentUuid, Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        if (additionalPropertiesToMatch != null) {
            propertiesToMatch.putAll(additionalPropertiesToMatch);
        }
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        return getComponentListByUuid(componentUuid, propertiesToMatch);
    }

    public Either<Component, StorageOperationStatus> getComponentByUuidAndVersion(String componentUuid, String version) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(GraphPropertyEnum.UUID, componentUuid);
        propertiesToMatch.put(GraphPropertyEnum.VERSION, version);

        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, TitanOperationStatus> vertexEither = titanDao.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        if (vertexEither.isRight()) {
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexEither.right().value()));
        }

        List<GraphVertex> vertexList = vertexEither.isLeft() ? vertexEither.left().value() : null;
        if (vertexList == null || vertexList.isEmpty() || vertexList.size() > 1) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }

        return getToscaElementByOperation(vertexList.get(0));
    }

    public Either<List<Component>, StorageOperationStatus> getComponentListByUuid(String componentUuid, Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);

        if (additionalPropertiesToMatch != null) {
            propertiesToMatch.putAll(additionalPropertiesToMatch);
        }

        propertiesToMatch.put(GraphPropertyEnum.UUID, componentUuid);

        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_ARCHIVED, true); //US382674, US382683

        Either<List<GraphVertex>, TitanOperationStatus> vertexEither = titanDao.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);

        if (vertexEither.isRight()) {
            log.debug("Couldn't fetch metadata for component with uuid {}, error: {}", componentUuid, vertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexEither.right().value()));
        }
        List<GraphVertex> vertexList = vertexEither.isLeft() ? vertexEither.left().value() : null;

        if (vertexList == null || vertexList.isEmpty()) {
            log.debug("Component with uuid {} was not found", componentUuid);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }

        ArrayList<Component> latestComponents = new ArrayList<>();
        for (GraphVertex vertex : vertexList) {
            Either<Component, StorageOperationStatus> toscaElementByOperation = getToscaElementByOperation(vertex);

            if (toscaElementByOperation.isRight()) {
                log.debug("Could not fetch the following Component by UUID {}", vertex.getUniqueId());
                return Either.right(toscaElementByOperation.right().value());
            }

            latestComponents.add(toscaElementByOperation.left().value());
        }

        if (latestComponents.size() > 1) {
            for (Component component : latestComponents) {
                if (component.isHighestVersion()) {
                    LinkedList<Component> highestComponent = new LinkedList<>();
                    highestComponent.add(component);
                    return Either.left(highestComponent);
                }
            }
        }

        return Either.left(latestComponents);
    }

    public Either<Component, StorageOperationStatus> getLatestServiceByUuid(String serviceUuid) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        return getLatestComponentByUuid(serviceUuid, propertiesToMatch);
    }

    public Either<Component, StorageOperationStatus> getLatestComponentByUuid(String componentUuid) {
        return getLatestComponentByUuid(componentUuid, null);
    }

    public Either<Component, StorageOperationStatus> getLatestComponentByUuid(String componentUuid, Map<GraphPropertyEnum, Object> propertiesToMatch) {

        Either<List<Component>, StorageOperationStatus> latestVersionListEither = getLatestComponentListByUuid(componentUuid, propertiesToMatch);

        if (latestVersionListEither.isRight()) {
            return Either.right(latestVersionListEither.right().value());
        }

        List<Component> latestVersionList = latestVersionListEither.left().value();

        if (latestVersionList.isEmpty()) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        Component component = latestVersionList.size() == 1 ? latestVersionList.get(0) : latestVersionList.stream().max((c1, c2) -> Double.compare(Double.parseDouble(c1.getVersion()), Double.parseDouble(c2.getVersion()))).get();

        return Either.left(component);
    }

    public Either<List<Resource>, StorageOperationStatus> getAllCertifiedResources(boolean isAbstract, Boolean isHighest) {

        List<Resource> resources = new ArrayList<>();
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(GraphPropertyEnum.IS_ABSTRACT, isAbstract);
        if (isHighest != null) {
            propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, isHighest);
        }
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        Either<List<GraphVertex>, TitanOperationStatus> getResourcesRes = titanDao.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);

        if (getResourcesRes.isRight()) {
            log.debug("Failed to fetch all certified resources. Status is {}", getResourcesRes.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getResourcesRes.right().value()));
        }
        List<GraphVertex> resourceVerticies = getResourcesRes.left().value();
        for (GraphVertex resourceV : resourceVerticies) {
            Either<Resource, StorageOperationStatus> getResourceRes = getToscaElement(resourceV);
            if (getResourceRes.isRight()) {
                return Either.right(getResourceRes.right().value());
            }
            resources.add(getResourceRes.left().value());
        }
        return Either.left(resources);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByNameAndVersion(String name, String version, JsonParseFlagEnum parseFlag) {
        Either<T, StorageOperationStatus> result;

        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProperties = new EnumMap<>(GraphPropertyEnum.class);

        hasProperties.put(GraphPropertyEnum.NAME, name);
        hasProperties.put(GraphPropertyEnum.VERSION, version);
        hasProperties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        hasNotProperties.put(GraphPropertyEnum.IS_DELETED, true);

        Either<List<GraphVertex>, TitanOperationStatus> getResourceRes = titanDao.getByCriteria(null, hasProperties, hasNotProperties, parseFlag);
        if (getResourceRes.isRight()) {
            TitanOperationStatus status = getResourceRes.right().value();
            log.debug("failed to find resource with name {}, version {}. Status is {} ", name, version, status);
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
            return result;
        }
        return getToscaElementByOperation(getResourceRes.left().value().get(0));
    }

    public Either<Resource, StorageOperationStatus> getLatestComponentByCsarOrName(ComponentTypeEnum componentType, String csarUUID, String systemName) {
        return getLatestComponentByCsarOrName(componentType, csarUUID, systemName, JsonParseFlagEnum.ParseAll);
    }

    public Either<Resource, StorageOperationStatus> getLatestComponentByCsarOrName(ComponentTypeEnum componentType, String csarUUID, String systemName, JsonParseFlagEnum parseFlag) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propsHasNot = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.CSAR_UUID, csarUUID);
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        if (componentType != null) {
            props.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        }
        propsHasNot.put(GraphPropertyEnum.IS_DELETED, true);

        GraphVertex resourceMetadataData = null;
        List<GraphVertex> resourceMetadataDataList = null;
        Either<List<GraphVertex>, TitanOperationStatus> byCsar = titanDao.getByCriteria(null, props, propsHasNot, JsonParseFlagEnum.ParseMetadata);
        if (byCsar.isRight()) {
            if (TitanOperationStatus.NOT_FOUND == byCsar.right().value()) {
                // Fix Defect DE256036
                if (StringUtils.isEmpty(systemName)) {
                    return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_FOUND));
                }

                props.clear();
                props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                props.put(GraphPropertyEnum.SYSTEM_NAME, systemName);
                Either<List<GraphVertex>, TitanOperationStatus> bySystemname = titanDao.getByCriteria(null, props, JsonParseFlagEnum.ParseMetadata);
                if (bySystemname.isRight()) {
                    log.debug("getLatestResourceByCsarOrName - Failed to find by system name {}  error {} ", systemName, bySystemname.right().value());
                    return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(bySystemname.right().value()));
                }
                if (bySystemname.left().value().size() > 2) {
                    log.debug("getLatestResourceByCsarOrName - getByCriteria(by system name) must return only 2 latest version, but was returned - {}", bySystemname.left().value().size());
                    return Either.right(StorageOperationStatus.GENERAL_ERROR);
                }
                resourceMetadataDataList = bySystemname.left().value();
                if (resourceMetadataDataList.size() == 1) {
                    resourceMetadataData = resourceMetadataDataList.get(0);
                } else {
                    for (GraphVertex curResource : resourceMetadataDataList) {
                        if (!((String) curResource.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals("CERTIFIED")) {
                            resourceMetadataData = curResource;
                            break;
                        }
                    }
                }
                if (resourceMetadataData == null) {
                    log.debug("getLatestResourceByCsarOrName - getByCriteria(by system name) returned 2 latest CERTIFIED versions");
                    return Either.right(StorageOperationStatus.GENERAL_ERROR);
                }
                if (resourceMetadataData.getJsonMetadataField(JsonPresentationFields.CSAR_UUID) != null && !((String) resourceMetadataData.getJsonMetadataField(JsonPresentationFields.CSAR_UUID)).equals(csarUUID)) {
                    log.debug("getLatestResourceByCsarOrName - same system name {} but different csarUUID. exist {} and new {} ", systemName, resourceMetadataData.getJsonMetadataField(JsonPresentationFields.CSAR_UUID), csarUUID);
                    // correct error will be returned from create flow. with all
                    // correct audit records!!!!!
                    return Either.right(StorageOperationStatus.NOT_FOUND);
                }
                return getToscaElement((String) resourceMetadataData.getUniqueId());
            }
        } else {
            resourceMetadataDataList = byCsar.left().value();
            if (resourceMetadataDataList.size() > 2) {
                log.debug("getLatestResourceByCsarOrName - getByCriteria(by csar) must return only 2 latest version, but was returned - {}", byCsar.left().value().size());
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
            if (resourceMetadataDataList.size() == 1) {
                resourceMetadataData = resourceMetadataDataList.get(0);
            } else {
                for (GraphVertex curResource : resourceMetadataDataList) {
                    if (!((String) curResource.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals("CERTIFIED")) {
                        resourceMetadataData = curResource;
                        break;
                    }
                }
            }
            if (resourceMetadataData == null) {
                log.debug("getLatestResourceByCsarOrName - getByCriteria(by csar) returned 2 latest CERTIFIED versions");
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
            return getToscaElement((String) resourceMetadataData.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID), parseFlag);
        }
        return null;
    }

    public Either<Boolean, StorageOperationStatus> validateToscaResourceNameExtends(String templateNameCurrent, String templateNameExtends) {

        String currentTemplateNameChecked = templateNameExtends;

        while (currentTemplateNameChecked != null && !currentTemplateNameChecked.equalsIgnoreCase(templateNameCurrent)) {
            Either<Resource, StorageOperationStatus> latestByToscaResourceName = getLatestByToscaResourceName(currentTemplateNameChecked);

            if (latestByToscaResourceName.isRight()) {
                return latestByToscaResourceName.right().value() == StorageOperationStatus.NOT_FOUND ? Either.left(false) : Either.right(latestByToscaResourceName.right().value());
            }

            Resource value = latestByToscaResourceName.left().value();

            if (value.getDerivedFrom() != null) {
                currentTemplateNameChecked = value.getDerivedFrom().get(0);
            } else {
                currentTemplateNameChecked = null;
            }
        }

        return (currentTemplateNameChecked != null && currentTemplateNameChecked.equalsIgnoreCase(templateNameCurrent)) ? Either.left(true) : Either.left(false);
    }

    public Either<List<Component>, StorageOperationStatus> fetchMetaDataByResourceType(String resourceType, ComponentParametersView filterBy) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.RESOURCE_TYPE, resourceType);
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        Map<GraphPropertyEnum, Object> propsHasNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propsHasNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, TitanOperationStatus> resourcesByTypeEither = titanDao.getByCriteria(null, props, propsHasNotToMatch, JsonParseFlagEnum.ParseMetadata);

        if (resourcesByTypeEither.isRight()) {
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resourcesByTypeEither.right().value()));
        }

        List<GraphVertex> vertexList = resourcesByTypeEither.left().value();
        List<Component> components = new ArrayList<>();

        for (GraphVertex vertex : vertexList) {
            components.add(getToscaElementByOperation(vertex, filterBy).left().value());
        }

        return Either.left(components);
    }

    public void commit() {
        titanDao.commit();
    }

    public Either<Service, StorageOperationStatus> updateDistributionStatus(Service service, User user, DistributionStatusEnum distributionStatus) {
        Either<GraphVertex, StorageOperationStatus> updateDistributionStatus = topologyTemplateOperation.updateDistributionStatus(service.getUniqueId(), user, distributionStatus);
        if (updateDistributionStatus.isRight()) {
            return Either.right(updateDistributionStatus.right().value());
        }
        GraphVertex serviceV = updateDistributionStatus.left().value();
        service.setDistributionStatus(distributionStatus);
        service.setLastUpdateDate((Long) serviceV.getJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE));
        return Either.left(service);
    }

    public Either<ComponentMetadataData, StorageOperationStatus> updateComponentLastUpdateDateOnGraph(Component component) {

        Either<ComponentMetadataData, StorageOperationStatus> result = null;
        GraphVertex serviceVertex;
        Either<GraphVertex, TitanOperationStatus> updateRes = null;
        Either<GraphVertex, TitanOperationStatus> getRes = titanDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.ParseMetadata);
        if (getRes.isRight()) {
            TitanOperationStatus status = getRes.right().value();
            log.error("Failed to fetch component {}. status is {}", component.getUniqueId(), status);
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
        }
        if (result == null) {
            serviceVertex = getRes.left().value();
            long lastUpdateDate = System.currentTimeMillis();
            serviceVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, lastUpdateDate);
            component.setLastUpdateDate(lastUpdateDate);
            updateRes = titanDao.updateVertex(serviceVertex);
            if (updateRes.isRight()) {
                result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateRes.right().value()));
            }
        }
        if (result == null) {
            result = Either.left(ModelConverter.convertToComponentMetadata(updateRes.left().value()));
        }
        return result;
    }

    public TitanDao getTitanDao() {
        return titanDao;
    }

    public Either<List<Service>, StorageOperationStatus> getCertifiedServicesWithDistStatus(Set<DistributionStatusEnum> distStatus) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

        return getServicesWithDistStatus(distStatus, propertiesToMatch);
    }

    public Either<List<Service>, StorageOperationStatus> getServicesWithDistStatus(Set<DistributionStatusEnum> distStatus, Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {

        List<Service> servicesAll = new ArrayList<>();

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        if (additionalPropertiesToMatch != null && !additionalPropertiesToMatch.isEmpty()) {
            propertiesToMatch.putAll(additionalPropertiesToMatch);
        }

        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());

        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        if (distStatus != null && !distStatus.isEmpty()) {
            for (DistributionStatusEnum state : distStatus) {
                propertiesToMatch.put(GraphPropertyEnum.DISTRIBUTION_STATUS, state.name());
                Either<List<Service>, StorageOperationStatus> fetchServicesByCriteria = fetchServicesByCriteria(servicesAll, propertiesToMatch, propertiesNotToMatch);
                if (fetchServicesByCriteria.isRight()) {
                    return fetchServicesByCriteria;
                } else {
                    servicesAll = fetchServicesByCriteria.left().value();
                }
            }
            return Either.left(servicesAll);
        } else {
            return fetchServicesByCriteria(servicesAll, propertiesToMatch, propertiesNotToMatch);
        }
    }

    private Either<List<Service>, StorageOperationStatus> fetchServicesByCriteria(List<Service> servicesAll, Map<GraphPropertyEnum, Object> propertiesToMatch, Map<GraphPropertyEnum, Object> propertiesNotToMatch) {
        Either<List<GraphVertex>, TitanOperationStatus> getRes = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        if (getRes.isRight()) {
            if (getRes.right().value() != TitanOperationStatus.NOT_FOUND) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch certified services by match properties {} not match properties {} . Status is {}. ", propertiesToMatch, propertiesNotToMatch, getRes.right().value());
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getRes.right().value()));
            }
        } else {
            for (GraphVertex vertex : getRes.left().value()) {
                Either<ToscaElement, StorageOperationStatus> getServiceRes = topologyTemplateOperation.getLightComponent(vertex, ComponentTypeEnum.SERVICE, new ComponentParametersView(true));

                if (getServiceRes.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch certified service {}. Status is {}. ", vertex.getJsonMetadataField(JsonPresentationFields.NAME), getServiceRes.right().value());
                    return Either.right(getServiceRes.right().value());
                } else {
                    servicesAll.add(ModelConverter.convertFromToscaElement(getServiceRes.left().value()));
                }
            }
        }
        return Either.left(servicesAll);
    }

    public void rollback() {
        titanDao.rollback();
    }

    public StorageOperationStatus addDeploymentArtifactsToInstance(String componentId, ComponentInstance componentInstance, Map<String, ArtifactDefinition> finalDeploymentArtifacts) {
        Map<String, ArtifactDataDefinition> instDeplArtifacts = finalDeploymentArtifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));

        return nodeTemplateOperation.addDeploymentArtifactsToInstance(componentId, componentInstance.getUniqueId(), instDeplArtifacts);
    }

    public StorageOperationStatus addInformationalArtifactsToInstance(String componentId, ComponentInstance componentInstance, Map<String, ArtifactDefinition> artifacts) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        if (MapUtils.isNotEmpty(artifacts)) {
            Map<String, ArtifactDataDefinition> instDeplArtifacts = artifacts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
            status = nodeTemplateOperation.addInformationalArtifactsToInstance(componentId, componentInstance.getUniqueId(), instDeplArtifacts);
        }
        return status;
    }

    public StorageOperationStatus generateCustomizationUUIDOnInstance(String componentId, String instanceId) {
        return nodeTemplateOperation.generateCustomizationUUIDOnInstance(componentId, instanceId);
    }

    public StorageOperationStatus generateCustomizationUUIDOnInstanceGroup(String componentId, String instanceId, List<String> groupInstances) {
        return nodeTemplateOperation.generateCustomizationUUIDOnInstanceGroup(componentId, instanceId, groupInstances);
    }

		public Either<PropertyDefinition, StorageOperationStatus> addPropertyToComponent(String propertyName,
																					 PropertyDefinition newPropertyDefinition,
																					 Component component) {

		Either<PropertyDefinition, StorageOperationStatus> result = null;
		Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
		newPropertyDefinition.setName(propertyName);

		StorageOperationStatus status = getToscaElementOperation(component)
				.addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, newPropertyDefinition, JsonPresentationFields.NAME);
		if (status != StorageOperationStatus.OK) {
			CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add the property {} to the component {}. Status is {}. ", propertyName, component.getName(), status);
			result = Either.right(status);
		}
		if (result == null) {
			ComponentParametersView filter = new ComponentParametersView(true);
			filter.setIgnoreProperties(false);
			filter.setIgnoreInputs(false);
			getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
			if (getUpdatedComponentRes.isRight()) {
				CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get updated component {}. Status is {}. ", component.getUniqueId(), getUpdatedComponentRes.right().value());
				result = Either.right(status);
			}
		}
		if (result == null) {
			PropertyDefinition newProperty = null;
			List<PropertyDefinition> properties =
					(getUpdatedComponentRes.left().value()).getProperties();
			if (CollectionUtils.isNotEmpty(properties)) {
				Optional<PropertyDefinition> propertyOptional = properties.stream().filter(
						propertyEntry -> propertyEntry.getName().equals(propertyName)).findAny();
				if (propertyOptional.isPresent()) {
					newProperty = propertyOptional.get();
				}
			}
			if (newProperty == null) {
				CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find recently added property {} on the component {}. Status is {}. ", propertyName, component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
				result = Either.right(StorageOperationStatus.NOT_FOUND);
			} else {
				result = Either.left(newProperty);
			}
		}
		return result;
	}
	public StorageOperationStatus deletePropertyOfComponent(Component component, String propertyName) {
		return getToscaElementOperation(component).deleteToscaDataElement(component.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, propertyName, JsonPresentationFields.NAME);
	}

	public StorageOperationStatus deleteAttributeOfResource(Component component, String attributeName) {
		return getToscaElementOperation(component).deleteToscaDataElement(component.getUniqueId(), EdgeLabelEnum.ATTRIBUTES, VertexTypeEnum.ATTRIBUTES, attributeName, JsonPresentationFields.NAME);
	}

    public StorageOperationStatus deleteInputOfResource(Component resource, String inputName) {
        return getToscaElementOperation(resource).deleteToscaDataElement(resource.getUniqueId(), EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, inputName, JsonPresentationFields.NAME);
    }

	public Either<PropertyDefinition, StorageOperationStatus> updatePropertyOfComponent(Component component,
																						PropertyDefinition newPropertyDefinition) {

		Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
		Either<PropertyDefinition, StorageOperationStatus> result = null;
		StorageOperationStatus status = getToscaElementOperation(component).updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, newPropertyDefinition, JsonPresentationFields.NAME);
		if (status != StorageOperationStatus.OK) {
			CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add the property {} to the resource {}. Status is {}. ", newPropertyDefinition.getName(), component.getName(), status);
			result = Either.right(status);
		}
		if (result == null) {
			ComponentParametersView filter = new ComponentParametersView(true);
			filter.setIgnoreProperties(false);
			getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
			if (getUpdatedComponentRes.isRight()) {
				CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get updated resource {}. Status is {}. ", component.getUniqueId(), getUpdatedComponentRes.right().value());
				result = Either.right(status);
			}
		}
		if (result == null) {
			Optional<PropertyDefinition> newProperty = (getUpdatedComponentRes.left().value())
					.getProperties().stream().filter(p -> p.getName().equals(newPropertyDefinition.getName())).findAny();
			if (newProperty.isPresent()) {
				result = Either.left(newProperty.get());
			} else {
				CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find recently added property {} on the resource {}. Status is {}. ", newPropertyDefinition.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
				result = Either.right(StorageOperationStatus.NOT_FOUND);
			}
		}
		return result;
	}



	public Either<PropertyDefinition, StorageOperationStatus> addAttributeOfResource(Component component, PropertyDefinition newAttributeDef) {

        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<PropertyDefinition, StorageOperationStatus> result = null;
        if (newAttributeDef.getUniqueId() == null || newAttributeDef.getUniqueId().isEmpty()) {
            String attUniqueId = UniqueIdBuilder.buildAttributeUid(component.getUniqueId(), newAttributeDef.getName());
            newAttributeDef.setUniqueId(attUniqueId);
        }

        StorageOperationStatus status = getToscaElementOperation(component).addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.ATTRIBUTES, VertexTypeEnum.ATTRIBUTES, newAttributeDef, JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS, newAttributeDef.getName(), component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreAttributesFrom(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(), getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<PropertyDefinition> newAttribute = ((Resource) getUpdatedComponentRes.left().value()).getAttributes().stream().filter(p -> p.getName().equals(newAttributeDef.getName())).findAny();
            if (newAttribute.isPresent()) {
                result = Either.left(newAttribute.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS, newAttributeDef.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        return result;
    }

    public Either<PropertyDefinition, StorageOperationStatus> updateAttributeOfResource(Component component, PropertyDefinition newAttributeDef) {

        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<PropertyDefinition, StorageOperationStatus> result = null;
        StorageOperationStatus status = getToscaElementOperation(component).updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.ATTRIBUTES, VertexTypeEnum.ATTRIBUTES, newAttributeDef, JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS, newAttributeDef.getName(), component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreAttributesFrom(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(), getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<PropertyDefinition> newProperty = ((Resource) getUpdatedComponentRes.left().value()).getAttributes().stream().filter(p -> p.getName().equals(newAttributeDef.getName())).findAny();
            if (newProperty.isPresent()) {
                result = Either.left(newProperty.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS, newAttributeDef.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        return result;
    }

    public Either<InputDefinition, StorageOperationStatus> updateInputOfComponent(Component component, InputDefinition newInputDefinition) {

        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<InputDefinition, StorageOperationStatus> result = null;
        StorageOperationStatus status = getToscaElementOperation(component).updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, newInputDefinition, JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the input {} to the component {}. Status is {}. ", newInputDefinition.getName(), component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreInputs(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(), getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<InputDefinition> updatedInput = getUpdatedComponentRes.left().value().getInputs().stream().filter(p -> p.getName().equals(newInputDefinition.getName())).findAny();
            if (updatedInput.isPresent()) {
                result = Either.left(updatedInput.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find recently updated inputs {} on the resource {}. Status is {}. ", newInputDefinition.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        return result;
    }

    /**
     * method - ename the group instances after referenced container name renamed flow - VF rename -(triggers)-> Group rename
     *
     * @param containerComponent  - container such as service
     * @param componentInstance   - context component
     * @param componentInstanceId - id
     * @return - successfull/failed status
     **/
    public Either<StorageOperationStatus, StorageOperationStatus> cleanAndAddGroupInstancesToComponentInstance(Component containerComponent, ComponentInstance componentInstance, String componentInstanceId) {
        String uniqueId = componentInstance.getUniqueId();
        StorageOperationStatus status = nodeTemplateOperation.deleteToscaDataDeepElementsBlockOfToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, uniqueId);
        if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete group instances for container {}. error {] ", componentInstanceId, status);
            return Either.right(status);
        }
        if (componentInstance.getGroupInstances() != null) {
            status = addGroupInstancesToComponentInstance(containerComponent, componentInstance, componentInstance.getGroupInstances());
            if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add group instances for container {}. error {] ", componentInstanceId, status);
                return Either.right(status);
            }
        }
        return Either.left(status);
    }

    public StorageOperationStatus addGroupInstancesToComponentInstance(Component containerComponent, ComponentInstance componentInstance, List<GroupDefinition> groups, Map<String, List<ArtifactDefinition>> groupInstancesArtifacts) {
        return nodeTemplateOperation.addGroupInstancesToComponentInstance(containerComponent, componentInstance, groups, groupInstancesArtifacts);
    }

    public Either<List<GroupDefinition>, StorageOperationStatus> updateGroupsOnComponent(Component component, List<GroupDataDefinition> updatedGroups) {
        return groupsOperation.updateGroups(component, updatedGroups, true);
    }

    public Either<List<GroupInstance>, StorageOperationStatus> updateGroupInstancesOnComponent(Component component, String instanceId, List<GroupInstance> updatedGroupInstances) {
        return groupsOperation.updateGroupInstances(component, instanceId, updatedGroupInstances);
    }

    public StorageOperationStatus addGroupInstancesToComponentInstance(Component containerComponent, ComponentInstance componentInstance, List<GroupInstance> groupInstances) {
        return nodeTemplateOperation.addGroupInstancesToComponentInstance(containerComponent, componentInstance, groupInstances);
    }

    public StorageOperationStatus addDeploymentArtifactsToComponentInstance(Component containerComponent, ComponentInstance componentInstance, Map<String, ArtifactDefinition> deploymentArtifacts) {
        return nodeTemplateOperation.addDeploymentArtifactsToComponentInstance(containerComponent, componentInstance, deploymentArtifacts);
    }

    public StorageOperationStatus updateComponentInstanceProperty(Component containerComponent, String componentInstanceId, ComponentInstanceProperty property) {
        return nodeTemplateOperation.updateComponentInstanceProperty(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceProperties(Component containerComponent, String componentInstanceId, List<ComponentInstanceProperty> properties) {
        return nodeTemplateOperation.updateComponentInstanceProperties(containerComponent, componentInstanceId, properties);
    }


    public StorageOperationStatus addComponentInstanceProperty(Component containerComponent, String componentInstanceId, ComponentInstanceProperty property) {
        return nodeTemplateOperation.addComponentInstanceProperty(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceAttribute(Component containerComponent, String componentInstanceId, ComponentInstanceProperty property){
        return nodeTemplateOperation.updateComponentInstanceAttribute(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus addComponentInstanceAttribute(Component containerComponent, String componentInstanceId, ComponentInstanceProperty property){
        return nodeTemplateOperation.addComponentInstanceAttribute(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceInput(Component containerComponent, String componentInstanceId, ComponentInstanceInput property) {
        return nodeTemplateOperation.updateComponentInstanceInput(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceInputs(Component containerComponent, String componentInstanceId, List<ComponentInstanceInput> instanceInputs) {
        return nodeTemplateOperation.updateComponentInstanceInputs(containerComponent, componentInstanceId, instanceInputs);
    }

    public StorageOperationStatus addComponentInstanceInput(Component containerComponent, String componentInstanceId, ComponentInstanceInput property) {
        return nodeTemplateOperation.addComponentInstanceInput(containerComponent, componentInstanceId, property);
    }

    public void setNodeTypeOperation(NodeTypeOperation nodeTypeOperation) {
        this.nodeTypeOperation = nodeTypeOperation;
    }

    public void setTopologyTemplateOperation(TopologyTemplateOperation topologyTemplateOperation) {
        this.topologyTemplateOperation = topologyTemplateOperation;
    }

    public StorageOperationStatus deleteComponentInstanceInputsFromTopologyTemplate(Component containerComponent, List<InputDefinition> inputsToDelete) {
        return topologyTemplateOperation.deleteToscaDataElements(containerComponent.getUniqueId(), EdgeLabelEnum.INPUTS, inputsToDelete.stream().map(PropertyDataDefinition::getName).collect(Collectors.toList()));
    }

    public StorageOperationStatus updateComponentInstanceCapabiltyProperty(Component containerComponent, String componentInstanceUniqueId, String capabilityUniqueId, ComponentInstanceProperty property) {
        return nodeTemplateOperation.updateComponentInstanceCapabilityProperty(containerComponent, componentInstanceUniqueId, capabilityUniqueId, property);
    }

    public StorageOperationStatus updateComponentInstanceCapabilityProperties(Component containerComponent, String componentInstanceUniqueId) {
        return convertComponentInstanceProperties(containerComponent, componentInstanceUniqueId)
                .map(instanceCapProps -> topologyTemplateOperation.updateComponentInstanceCapabilityProperties(containerComponent, componentInstanceUniqueId, instanceCapProps))
                .orElse(StorageOperationStatus.NOT_FOUND);
    }

	public StorageOperationStatus updateComponentCalculatedCapabilitiesProperties(Component containerComponent) {
		Map<String, MapCapabilityProperty> mapCapabiltyPropertyMap =
        convertComponentCapabilitiesProperties(containerComponent);
		return nodeTemplateOperation.overrideComponentCapabilitiesProperties(containerComponent, mapCapabiltyPropertyMap);
	}

    public StorageOperationStatus deleteAllCalculatedCapabilitiesRequirements(String topologyTemplateId) {
        StorageOperationStatus status = topologyTemplateOperation.removeToscaData(topologyTemplateId, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES);
        if (status == StorageOperationStatus.OK) {
            status = topologyTemplateOperation.removeToscaData(topologyTemplateId, EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS);
        }
        if (status == StorageOperationStatus.OK) {
            status = topologyTemplateOperation.removeToscaData(topologyTemplateId, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES);
        }
        return status;
    }

    public Either<Component, StorageOperationStatus> shouldUpgradeToLatestDerived(Resource clonedResource) {
        String componentId = clonedResource.getUniqueId();
        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));

        }
        GraphVertex nodeTypeV = getVertexEither.left().value();

        ToscaElement toscaElementToUpdate = ModelConverter.convertToToscaElement(clonedResource);

        Either<ToscaElement, StorageOperationStatus> shouldUpdateDerivedVersion = nodeTypeOperation.shouldUpdateDerivedVersion(toscaElementToUpdate, nodeTypeV);
        if (shouldUpdateDerivedVersion.isRight() && StorageOperationStatus.OK != shouldUpdateDerivedVersion.right().value()) {
            log.debug("Failed to update derived version for node type {} derived {}, error: {}", componentId, clonedResource.getDerivedFrom().get(0), shouldUpdateDerivedVersion.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));
        }
        if (shouldUpdateDerivedVersion.isLeft()) {
            return Either.left(ModelConverter.convertFromToscaElement(shouldUpdateDerivedVersion.left().value()));
        }
        return Either.left(clonedResource);
    }

    /**
     * Returns list of ComponentInstanceProperty belonging to component instance capability specified by name, type and ownerId
     */
    public Either<List<ComponentInstanceProperty>, StorageOperationStatus> getComponentInstanceCapabilityProperties(String componentId, String instanceId, String capabilityName, String capabilityType, String ownerId) {
        return topologyTemplateOperation.getComponentInstanceCapabilityProperties(componentId, instanceId, capabilityName, capabilityType, ownerId);
    }

	private MapInterfaceDataDefinition convertComponentInstanceInterfaces(Component currComponent,
																																				String componentInstanceId) {
		MapInterfaceDataDefinition mapInterfaceDataDefinition = new MapInterfaceDataDefinition();
		List<ComponentInstanceInterface> componentInterface = currComponent.getComponentInstancesInterfaces().get(componentInstanceId);

		if(CollectionUtils.isNotEmpty(componentInterface)) {
			componentInterface.stream().forEach(interfaceDef -> mapInterfaceDataDefinition.put
					(interfaceDef.getUniqueId(), interfaceDef));
		}

		return mapInterfaceDataDefinition;
	}

  private Map<String, MapCapabilityProperty> convertComponentCapabilitiesProperties(Component currComponent) {
    Map<String, MapCapabilityProperty> map = ModelConverter.extractCapabilityPropertiesFromGroups(currComponent.getGroups(), true);
    map.putAll(ModelConverter.extractCapabilityProperteisFromInstances(currComponent.getComponentInstances(), true));
    return map;
  }

    private Optional<MapCapabilityProperty> convertComponentInstanceProperties(Component component, String instanceId) {
        return component.fetchInstanceById(instanceId)
                .map(ci -> ModelConverter.convertToMapOfMapCapabiltyProperties(ci.getCapabilities(), instanceId));
    }

    public Either<PolicyDefinition, StorageOperationStatus> associatePolicyToComponent(String componentId, PolicyDefinition policyDefinition, int counter) {
        Either<PolicyDefinition, StorageOperationStatus> result = null;
        Either<GraphVertex, TitanOperationStatus> getVertexEither;
        getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
        if (getVertexEither.isRight()) {
            log.error(COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));
        } else {
            if (getVertexEither.left().value().getLabel() != VertexTypeEnum.TOPOLOGY_TEMPLATE) {
                log.error("Policy association to component of Tosca type {} is not allowed. ", getVertexEither.left().value().getLabel());
                result = Either.right(StorageOperationStatus.BAD_REQUEST);
            }
        }
        if (result == null) {
            StorageOperationStatus status = topologyTemplateOperation.addPolicyToToscaElement(getVertexEither.left().value(), policyDefinition, counter);
            if (status != StorageOperationStatus.OK) {
                return Either.right(status);
            }
        }
        if (result == null) {
            result = Either.left(policyDefinition);
        }
        return result;
    }

    public StorageOperationStatus associatePoliciesToComponent(String componentId, List<PolicyDefinition> policies) {
        log.debug("#associatePoliciesToComponent - associating policies for component {}.", componentId);
        return titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata)
                .either(containerVertex -> topologyTemplateOperation.addPoliciesToToscaElement(containerVertex, policies),
                        DaoStatusConverter::convertTitanStatusToStorageStatus);
    }

    public Either<PolicyDefinition, StorageOperationStatus> updatePolicyOfComponent(String componentId, PolicyDefinition policyDefinition) {
        Either<PolicyDefinition, StorageOperationStatus> result = null;
        Either<GraphVertex, TitanOperationStatus> getVertexEither;
        getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.error(COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value()));
        }
        if (result == null) {
            StorageOperationStatus status = topologyTemplateOperation.updatePolicyOfToscaElement(getVertexEither.left().value(), policyDefinition);
            if (status != StorageOperationStatus.OK) {
                return Either.right(status);
            }
        }
        if (result == null) {
            result = Either.left(policyDefinition);
        }
        return result;
    }

    public StorageOperationStatus updatePoliciesOfComponent(String componentId, List<PolicyDefinition> policyDefinition) {
        log.debug("#updatePoliciesOfComponent - updating policies for component {}", componentId);
        return titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse)
                .right()
                .map(DaoStatusConverter::convertTitanStatusToStorageStatus)
                .either(containerVertex -> topologyTemplateOperation.updatePoliciesOfToscaElement(containerVertex, policyDefinition),
                        err -> err);
    }

    public StorageOperationStatus removePolicyFromComponent(String componentId, String policyId) {
        StorageOperationStatus status = null;
        Either<GraphVertex, TitanOperationStatus> getVertexEither = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.error(COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            status = DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexEither.right().value());
        }
        if (status == null) {
            status = topologyTemplateOperation.removePolicyFromToscaElement(getVertexEither.left().value(), policyId);
        }
        return status;
    }

    public boolean canAddGroups(String componentId) {
        GraphVertex vertex = titanDao.getVertexById(componentId)
                .left()
                .on(this::onTitanError);
        return topologyTemplateOperation.hasEdgeOfType(vertex, EdgeLabelEnum.GROUPS);
    }

    GraphVertex onTitanError(TitanOperationStatus toe) {
        throw new StorageException(
                DaoStatusConverter.convertTitanStatusToStorageStatus(toe));
    }

    public void updateNamesOfCalculatedCapabilitiesRequirements(String componentId){
        topologyTemplateOperation
                .updateNamesOfCalculatedCapabilitiesRequirements(componentId, getTopologyTemplate(componentId));
    }

    public void revertNamesOfCalculatedCapabilitiesRequirements(String componentId) {
        topologyTemplateOperation
                .revertNamesOfCalculatedCapabilitiesRequirements(componentId, getTopologyTemplate(componentId));
    }

    private TopologyTemplate getTopologyTemplate(String componentId) {
        return (TopologyTemplate)topologyTemplateOperation
                .getToscaElement(componentId, getFilterComponentWithCapProperties())
                .left()
                .on(this::throwStorageException);
    }

    private ComponentParametersView getFilterComponentWithCapProperties() {
        ComponentParametersView filter = new ComponentParametersView();
        filter.setIgnoreCapabiltyProperties(false);
        return filter;
    }

    private ToscaElement throwStorageException(StorageOperationStatus status) {
        throw new StorageException(status);
    }

    public Either<Boolean, StorageOperationStatus> isComponentInUse(String componentId) {
        final List<EdgeLabelEnum> forbiddenEdgeLabelEnums = Arrays.asList(EdgeLabelEnum.INSTANCE_OF, EdgeLabelEnum.PROXY_OF, EdgeLabelEnum.ALLOTTED_OF);
        Either<GraphVertex, TitanOperationStatus> vertexById = titanDao.getVertexById(componentId);
        if (vertexById.isLeft()) {
            for (EdgeLabelEnum edgeLabelEnum : forbiddenEdgeLabelEnums) {
                Iterator<Edge> edgeItr = vertexById.left().value().getVertex().edges(Direction.IN, edgeLabelEnum.name());
                if(edgeItr != null && edgeItr.hasNext()){
                    return Either.left(true);
                }
            }
        }
        return Either.left(false);
    }

	public Either<List<Component>, StorageOperationStatus> getComponentListByInvariantUuid
			(String componentInvariantUuid, Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {

		Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
		if (MapUtils.isNotEmpty(additionalPropertiesToMatch)) {
			propertiesToMatch.putAll(additionalPropertiesToMatch);
		}
		propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, componentInvariantUuid);

		Either<List<GraphVertex>, TitanOperationStatus> vertexEither = titanDao.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata);

		if (vertexEither.isRight()) {
			log.debug("Couldn't fetch metadata for component with type {} and invariantUUId {}, error: {}", componentInvariantUuid, vertexEither.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexEither.right().value()));
		}
		List<GraphVertex> vertexList = vertexEither.isLeft() ? vertexEither.left().value() : null;

		if (vertexList == null || vertexList.isEmpty()) {
			log.debug("Component with invariantUUId {} was not found", componentInvariantUuid);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}

		ArrayList<Component> components = new ArrayList<>();
		for (GraphVertex vertex : vertexList) {
			Either<Component, StorageOperationStatus> toscaElementByOperation = getToscaElementByOperation(vertex);
			if (toscaElementByOperation.isRight()) {
				log.debug("Could not fetch the following Component by Invariant UUID {}", vertex.getUniqueId());
				return Either.right(toscaElementByOperation.right().value());
			}
			components.add(toscaElementByOperation.left().value());
		}

		return Either.left(components);
	}

    public Either<List<Component>, StorageOperationStatus> getParentComponents(String componentId) {
        List<Component> parentComponents = new ArrayList<>();
        final List<EdgeLabelEnum> relationEdgeLabelEnums = Arrays.asList(EdgeLabelEnum.INSTANCE_OF, EdgeLabelEnum.PROXY_OF);
        Either<GraphVertex, TitanOperationStatus> vertexById = titanDao.getVertexById(componentId);
        if (vertexById.isLeft()) {
            for (EdgeLabelEnum edgeLabelEnum : relationEdgeLabelEnums) {
                Either<GraphVertex, TitanOperationStatus> parentVertexEither = titanDao.getParentVertex(vertexById.left().value(), edgeLabelEnum, JsonParseFlagEnum.ParseJson);
                if(parentVertexEither.isLeft()){
                    Either<Component, StorageOperationStatus> componentEither = getToscaElement(parentVertexEither.left().value().getUniqueId());
                    if(componentEither.isLeft()){
                        parentComponents.add(componentEither.left().value());
                    }
                }
            }
        }
        return Either.left(parentComponents);
    }
    public void updateCapReqOwnerId(String componentId) {
        topologyTemplateOperation
                .updateCapReqOwnerId(componentId, getTopologyTemplate(componentId));
    }
}
