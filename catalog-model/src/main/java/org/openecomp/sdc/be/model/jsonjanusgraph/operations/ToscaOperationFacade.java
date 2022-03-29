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
package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.janusgraph.core.attribute.Text.REGEX;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.janusgraph.graphdb.query.JanusGraphPredicate;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapAttributesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapInterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.jsonjanusgraph.config.ContainerInstanceTypesData;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.utils.GroupUtils;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("tosca-operation-facade")
public class ToscaOperationFacade {

    // region - ToscaElement - GetById
    public static final String PROXY_SUFFIX = "_proxy";
    // region - Fields
    private static final String COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR = "Couldn't fetch a component with and UniqueId {}, error: {}";
    private static final String FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS = "Failed to find recently added property {} on the resource {}. Status is {}. ";
    private static final String FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS = "Failed to get updated resource {}. Status is {}. ";
    private static final String FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS = "Failed to add the property {} to the resource {}. Status is {}. ";
    private static final String SERVICE = "service";
    private static final String VF = "VF";
    private static final String NOT_SUPPORTED_COMPONENT_TYPE = "Not supported component type {}";
    private static final String COMPONENT_CREATED_SUCCESSFULLY = "Component created successfully!!!";
    private static final String COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR = "Couldn't fetch component with and unique id {}, error: {}";
    private static final Logger log = Logger.getLogger(ToscaOperationFacade.class.getName());
    @Autowired
    private IGraphLockOperation graphLockOperation;
    @Autowired
    private NodeTypeOperation nodeTypeOperation;
    @Autowired
    private TopologyTemplateOperation topologyTemplateOperation;
    @Autowired
    private NodeTemplateOperation nodeTemplateOperation;
    @Autowired
    private GroupsOperation groupsOperation;
    @Autowired
    private HealingJanusGraphDao janusGraphDao;
    // endregion
    @Autowired
    private ContainerInstanceTypesData containerInstanceTypesData;

    private static Optional<CapabilityDefinition> getPropertyCapability(String propertyParentUniqueId, Component containerComponent) {
        Map<String, List<CapabilityDefinition>> componentCapabilities = containerComponent.getCapabilities();
        if (MapUtils.isEmpty(componentCapabilities)) {
            return Optional.empty();
        }
        List<CapabilityDefinition> capabilityDefinitionList = componentCapabilities.values().stream().flatMap(Collection::stream)
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(capabilityDefinitionList)) {
            return Optional.empty();
        }
        return capabilityDefinitionList.stream().filter(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(propertyParentUniqueId))
            .findAny();
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaFullElement(String componentId) {
        ComponentParametersView filters = new ComponentParametersView();
        filters.setIgnoreCapabiltyProperties(false);
        filters.setIgnoreServicePath(false);
        return getToscaElement(componentId, filters);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(String componentId) {
        return getToscaElement(componentId, JsonParseFlagEnum.ParseAll);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(String componentId, ComponentParametersView filters) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, filters.detectParseFlag());
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        return getToscaElementByOperation(getVertexEither.left().value(), filters);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(String componentId, JsonParseFlagEnum parseFlag) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, parseFlag);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        return getToscaElementByOperation(getVertexEither.left().value());
    }

    public <T extends Component> Either<T, StorageOperationStatus> getToscaElement(GraphVertex componentVertex) {
        return getToscaElementByOperation(componentVertex);
    }

    public Either<Boolean, StorageOperationStatus> validateComponentExists(String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            JanusGraphOperationStatus status = getVertexEither.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                return Either.left(false);
            } else {
                log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
            }
        }
        return Either.left(true);
    }
    // endregion

    public <T extends Component> Either<T, StorageOperationStatus> findLastCertifiedToscaElementByUUID(T component) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.UUID, component.getUUID());
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> getVertexEither = janusGraphDao
            .getByCriteria(ModelConverter.getVertexType(component), props);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, component.getUniqueId(), getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        return getToscaElementByOperation(getVertexEither.left().value().get(0));
    }

    // region - ToscaElement - GetByOperation
    private <T extends Component> Either<T, StorageOperationStatus> getToscaElementByOperation(GraphVertex componentV) {
        return getToscaElementByOperation(componentV, new ComponentParametersView());
    }

    private <T extends Component> Either<T, StorageOperationStatus> getToscaElementByOperation(GraphVertex componentV,
                                                                                               ComponentParametersView filters) {
        if (componentV == null) {
            log.debug("Unexpected null value for `componentV`");
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        } else {
            VertexTypeEnum label = componentV.getLabel();
            ToscaElementOperation toscaOperation = getToscaElementOperation(componentV);
            if (toscaOperation != null) {
                log.debug("getToscaElementByOperation: toscaOperation={}", toscaOperation.getClass());
            }
            Either<ToscaElement, StorageOperationStatus> toscaElement;
            String componentId = componentV.getUniqueId();
            if (toscaOperation != null) {
                log.debug("Need to fetch tosca element for id {}", componentId);
                toscaElement = toscaOperation.getToscaElement(componentV, filters);
            } else {
                log.debug("not supported tosca type {} for id {}", label, componentId);
                toscaElement = Either.right(StorageOperationStatus.BAD_REQUEST);
            }
            return toscaElement.left().map(ModelConverter::convertFromToscaElement);
        }
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
        if (Boolean.TRUE.equals(componentToDelete.getIsDeleted()) && Boolean.FALSE.equals(componentToDelete.isHighestVersion())) {
            // component already marked for delete
            return StorageOperationStatus.OK;
        } else {
            Either<GraphVertex, JanusGraphOperationStatus> getResponse = janusGraphDao
                .getVertexById(componentToDelete.getUniqueId(), JsonParseFlagEnum.ParseAll);
            if (getResponse.isRight()) {
                log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentToDelete.getUniqueId(), getResponse.right().value());
                return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getResponse.right().value());
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
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll);
        if (getVertexEither.isRight()) {
            log.debug("Couldn't fetch component vertex with and unique id {}, error: {}", componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
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

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByToscaResourceNameAndModel(final String toscaResourceName,
                                                                                                        final String model) {
        return getLatestByNameAndModel(toscaResourceName, JsonParseFlagEnum.ParseMetadata, new ComponentParametersView(), model);
    }

    private <T extends Component> Either<T, StorageOperationStatus> getLatestByNameAndModel(final String nodeName,
                                                                                            final JsonParseFlagEnum parseFlag,
                                                                                            final ComponentParametersView filter,
                                                                                            final String model) {
        Either<T, StorageOperationStatus> result;
        final Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        final Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, nodeName);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        final Either<List<GraphVertex>, JanusGraphOperationStatus> highestResources = janusGraphDao
            .getByCriteria(null, propertiesToMatch, propertiesNotToMatch, parseFlag, model);
        if (highestResources.isRight()) {
            final JanusGraphOperationStatus status = highestResources.right().value();
            log.debug("failed to find resource with name {}. status={} ", nodeName, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            return result;
        }
        final List<GraphVertex> resources = highestResources.left().value();
        double version = 0.0;
        GraphVertex highestResource = null;
        for (final GraphVertex vertex : resources) {
            final Object versionObj = vertex.getMetadataProperty(GraphPropertyEnum.VERSION);
            double resourceVersion = Double.parseDouble((String) versionObj);
            if (resourceVersion > version) {
                version = resourceVersion;
                highestResource = vertex;
            }
        }
        return getToscaElementByOperation(highestResource, filter);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByToscaResourceName(String toscaResourceName, String modelName) {
        return getLatestByName(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName, modelName);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getFullLatestComponentByToscaResourceName(String toscaResourceName) {
        ComponentParametersView fetchAllFilter = new ComponentParametersView();
        fetchAllFilter.setIgnoreServicePath(true);
        fetchAllFilter.setIgnoreCapabiltyProperties(false);
        return getLatestByName(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName, JsonParseFlagEnum.ParseAll, fetchAllFilter, null);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByName(String resourceName, String modelName) {
        return getLatestByName(GraphPropertyEnum.NAME, resourceName, modelName);
    }

    public StorageOperationStatus validateCsarUuidUniqueness(String csarUUID) {
        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.CSAR_UUID, csarUUID);
        Either<List<GraphVertex>, JanusGraphOperationStatus> resources = janusGraphDao
            .getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata);
        if (resources.isRight()) {
            if (resources.right().value() == JanusGraphOperationStatus.NOT_FOUND) {
                return StorageOperationStatus.OK;
            } else {
                log.debug("failed to get resources from graph with property name: {}", csarUUID);
                return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(resources.right().value());
            }
        }
        return StorageOperationStatus.ENTITY_ALREADY_EXISTS;
    }

    public <T extends Component> Either<Set<T>, StorageOperationStatus> getFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates,
                                                                                    Set<LifecycleStateEnum> lastStateStates,
                                                                                    ComponentTypeEnum componentType) {
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

    public Either<Resource, StorageOperationStatus> getByToscaResourceNameMatchingVendorRelease(final String toscaResourceName,
                                                                                                final String vendorVersion) {
        return getByToscaResourceNameMatchingVendorRelease(toscaResourceName, VertexTypeEnum.NODE_TYPE, JsonParseFlagEnum.ParseMetadata,
            vendorVersion);
    }

    public Either<Resource, StorageOperationStatus> getByToscaResourceNameMatchingVendorRelease(String toscaResourceName, VertexTypeEnum vertexType,
                                                                                                JsonParseFlagEnum parseFlag, String vendorRelease) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        Map<String, Entry<JanusGraphPredicate, Object>> predicateCriteria = getVendorVersionPredicate(vendorRelease);
        Either<List<GraphVertex>, JanusGraphOperationStatus> getLatestRes = janusGraphDao
            .getByCriteria(vertexType, props, null, predicateCriteria, parseFlag, null);
        if (getLatestRes.isRight() || CollectionUtils.isEmpty(getLatestRes.left().value())) {
            getLatestRes = janusGraphDao.getByCriteria(vertexType, props, parseFlag);
        }
        return getLatestRes.right().map(status -> {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch {} with name {}. status={} ", vertexType, toscaResourceName, status);
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }).left().bind(resources -> {
            double version = 0.0;
            GraphVertex highestResource = null;
            for (GraphVertex resource : resources) {
                double resourceVersion = Double.parseDouble((String) resource.getJsonMetadataField(JsonPresentationFields.VERSION));
                if (resourceVersion > version && isValidForVendorRelease(resource, vendorRelease)) {
                    version = resourceVersion;
                    highestResource = resource;
                }
            }
            if (highestResource != null) {
                return getToscaFullElement(highestResource.getUniqueId());
            } else {
                log.debug("The vertex with the highest version could not be found for {}", toscaResourceName);
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
        });
    }

    public <T extends Component> Either<T, StorageOperationStatus> getByToscaResourceNameAndVersion(final String toscaResourceName,
                                                                                                    final String version, final String model) {
        Either<T, StorageOperationStatus> result;

        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProperties = new EnumMap<>(GraphPropertyEnum.class);

        hasProperties.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        hasProperties.put(GraphPropertyEnum.VERSION, version);
        hasNotProperties.put(GraphPropertyEnum.IS_DELETED, true);

        Either<List<GraphVertex>, JanusGraphOperationStatus> getResourceRes = janusGraphDao
            .getByCriteria(VertexTypeEnum.NODE_TYPE, hasProperties, hasNotProperties, JsonParseFlagEnum.ParseAll, model);
        if (getResourceRes.isRight()) {
            JanusGraphOperationStatus status = getResourceRes.right().value();
            log.debug("failed to find resource with toscaResourceName {}, version {}. Status is {} ", toscaResourceName, version, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            return result;
        }
        return getToscaElementByOperation(getResourceRes.left().value().get(0));
    }

    private Map<String, Entry<JanusGraphPredicate, Object>> getVendorVersionPredicate(final String vendorRelease) {
        Map<String, Entry<JanusGraphPredicate, Object>> predicateCriteria = new HashMap<>();
        if (!"1.0".equals(vendorRelease)) {
            String[] vendorReleaseElements = vendorRelease.split("\\.");
            if (vendorReleaseElements.length > 0) {
                String regex = ".*\"vendorRelease\":\"";
                for (int i = 0; i < vendorReleaseElements.length; i++) {
                    regex += vendorReleaseElements[i];
                    regex += i < vendorReleaseElements.length - 1 ? "\\." : "\".*";
                }
                predicateCriteria.put("metadata", new HashMap.SimpleEntry<>(REGEX, regex));
            }
        }
        return predicateCriteria;
    }

    public boolean isNodeAssociatedToModel(final String model, final Resource resource) {
        final List<GraphVertex> modelElementVertices = getResourceModelElementVertices(resource);
        if (model == null) {
            return modelElementVertices.isEmpty();
        }
        return modelElementVertices.stream().anyMatch(graphVertex -> graphVertex.getMetadataProperty(GraphPropertyEnum.NAME).equals(model));
    }

    public List<GraphVertex> getResourceModelElementVertices(final Resource resource) {
        final Either<GraphVertex, JanusGraphOperationStatus> vertex =
            janusGraphDao.getVertexById(resource.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (vertex.isRight() || Objects.isNull(vertex.left().value())) {
            return Collections.emptyList();
        }
        final Either<List<GraphVertex>, JanusGraphOperationStatus> nodeModelVertices =
            janusGraphDao.getParentVertices(vertex.left().value(), EdgeLabelEnum.MODEL_ELEMENT, JsonParseFlagEnum.NoParse);
        if (nodeModelVertices.isRight() || nodeModelVertices.left().value() == null) {
            return Collections.emptyList();
        }
        return nodeModelVertices.left().value();
    }

    private boolean isValidForVendorRelease(final GraphVertex resource, final String vendorRelease) {
        if (!vendorRelease.equals("1.0")) {
            try {
                Semver resourceSemVer = new Semver((String) resource.getJsonMetadataField(JsonPresentationFields.VENDOR_RELEASE), SemverType.NPM);
                Semver packageSemVer = new Semver(vendorRelease, SemverType.NPM);
                return !resourceSemVer.isGreaterThan(packageSemVer);
            } catch (Exception exception) {
                log.debug("Error in comparing vendor release", exception);
                return true;
            }
        }
        return true;
    }

    public Either<Resource, StorageOperationStatus> getLatestCertifiedByToscaResourceName(String toscaResourceName, VertexTypeEnum vertexType,
                                                                                          JsonParseFlagEnum parseFlag) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        Either<List<GraphVertex>, JanusGraphOperationStatus> getLatestRes = janusGraphDao.getByCriteria(vertexType, props, parseFlag);
        return getLatestRes.right().map(status -> {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch {} with name {}. status={} ", vertexType, toscaResourceName, status);
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }).left().bind(resources -> {
            double version = 0.0;
            GraphVertex highestResource = null;
            for (GraphVertex resource : resources) {
                double resourceVersion = Double.parseDouble((String) resource.getJsonMetadataField(JsonPresentationFields.VERSION));
                if (resourceVersion > version) {
                    version = resourceVersion;
                    highestResource = resource;
                }
            }
            if (highestResource != null) {
                return getToscaFullElement(highestResource.getUniqueId());
            } else {
                log.debug("The vertex with the highest version could not be found for {}", toscaResourceName);
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
        });
    }

    public Either<Resource, StorageOperationStatus> getLatestResourceByToscaResourceName(String toscaResourceName) {
        if (toscaResourceName != null && toscaResourceName.contains("org.openecomp.resource.vf")) {
            return getLatestResourceByToscaResourceName(toscaResourceName, VertexTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseMetadata);
        } else {
            return getLatestResourceByToscaResourceName(toscaResourceName, VertexTypeEnum.NODE_TYPE, JsonParseFlagEnum.ParseMetadata);
        }
    }

    public Either<Resource, StorageOperationStatus> getLatestResourceByToscaResourceName(String toscaResourceName, VertexTypeEnum vertexType,
                                                                                         JsonParseFlagEnum parseFlag) {
        Either<Resource, StorageOperationStatus> result = null;
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        if (!toscaResourceName.contains("org.openecomp.resource.vf")) {
            props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        }
        Either<List<GraphVertex>, JanusGraphOperationStatus> getLatestRes = janusGraphDao.getByCriteria(vertexType, props, parseFlag);
        if (getLatestRes.isRight()) {
            JanusGraphOperationStatus status = getLatestRes.right().value();
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch {} with name {}. status={} ", vertexType, toscaResourceName, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
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
            if (highestResource != null) {
                result = getToscaFullElement(highestResource.getUniqueId());
            } else {
                log.debug("The vertex with the highest version could not be found for {}", toscaResourceName);
                result = Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
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

    public Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances(String componentId,
                                                                                                   RequirementCapabilityRelDef requirementDef) {
        return nodeTemplateOperation.dissociateResourceInstances(componentId, requirementDef);
    }

    /**
     * Allows to get fulfilled requirement by relation and received predicate
     */
    public Either<RequirementDataDefinition, StorageOperationStatus> getFulfilledRequirementByRelation(String componentId, String instanceId,
                                                                                                       RequirementCapabilityRelDef relation,
                                                                                                       BiPredicate<RelationshipInfo, RequirementDataDefinition> predicate) {
        return nodeTemplateOperation.getFulfilledRequirementByRelation(componentId, instanceId, relation, predicate);
    }

    /**
     * Allows to get fulfilled capability by relation and received predicate
     */
    public Either<CapabilityDataDefinition, StorageOperationStatus> getFulfilledCapabilityByRelation(String componentId, String instanceId,
                                                                                                     RequirementCapabilityRelDef relation,
                                                                                                     BiPredicate<RelationshipInfo, CapabilityDataDefinition> predicate) {
        return nodeTemplateOperation.getFulfilledCapabilityByRelation(componentId, instanceId, relation, predicate);
    }

    public Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> associateResourceInstances(Component component, String componentId,
                                                                                                        List<RequirementCapabilityRelDef> relations) {
        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> reqAndCapListEither = nodeTemplateOperation
            .associateResourceInstances(component, componentId, relations);
        if (component != null) {
            updateInstancesCapAndReqOnComponentFromDB(component);
        }
        return reqAndCapListEither;
    }

    protected Either<Boolean, StorageOperationStatus> validateToscaResourceNameUniqueness(String name) {
        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, name);
        Either<List<GraphVertex>, JanusGraphOperationStatus> resources = janusGraphDao
            .getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata);
        if (resources.isRight() && resources.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            log.debug("failed to get resources from graph with property name: {}", name);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(resources.right().value()));
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
        Either<GraphVertex, JanusGraphOperationStatus> componentVEither = janusGraphDao
            .getVertexById(oldComponent.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (componentVEither.isRight()) {
            log.debug("Failed to fetch component {} error {}", oldComponent.getUniqueId(), componentVEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(componentVEither.right().value()));
        }
        GraphVertex componentv = componentVEither.left().value();
        Either<GraphVertex, JanusGraphOperationStatus> parentVertexEither = janusGraphDao
            .getParentVertex(componentv, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
        if (parentVertexEither.isRight() && parentVertexEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            log.debug("Failed to fetch parent version for component {} error {}", oldComponent.getUniqueId(), parentVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(parentVertexEither.right().value()));
        }
        Either<ToscaElement, StorageOperationStatus> deleteToscaComponent = deleteToscaElement(componentv);
        if (deleteToscaComponent.isRight()) {
            log.debug("Failed to remove old component {} error {}", oldComponent.getUniqueId(), deleteToscaComponent.right().value());
            return Either.right(deleteToscaComponent.right().value());
        }
        Either<Resource, StorageOperationStatus> createToscaComponent = createToscaComponent(newComponent);
        if (createToscaComponent.isRight()) {
            log.debug("Failed to create tosca element component {} error {}", newComponent.getUniqueId(), createToscaComponent.right().value());
            return Either.right(createToscaComponent.right().value());
        }
        Resource newElement = createToscaComponent.left().value();
        Either<GraphVertex, JanusGraphOperationStatus> newVersionEither = janusGraphDao
            .getVertexById(newElement.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (newVersionEither.isRight()) {
            log.debug("Failed to fetch new tosca element component {} error {}", newComponent.getUniqueId(), newVersionEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(newVersionEither.right().value()));
        }
        if (parentVertexEither.isLeft()) {
            GraphVertex previousVersionV = parentVertexEither.left().value();
            JanusGraphOperationStatus createEdge = janusGraphDao
                .createEdge(previousVersionV, newVersionEither.left().value(), EdgeLabelEnum.VERSION, null);
            if (createEdge != JanusGraphOperationStatus.OK) {
                log.debug("Failed to associate to previous version {} new version {} error {}", previousVersionV.getUniqueId(),
                    newVersionEither.right().value(), createEdge);
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createEdge));
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
        newComponent.setLastUpdateDate(null);
        newComponent.setHighestVersion(true);
    }

    public <T extends Component> Either<T, StorageOperationStatus> updateToscaElement(T componentToUpdate) {
        return updateToscaElement(componentToUpdate, new ComponentParametersView());
    }

    public <T extends Component> Either<T, StorageOperationStatus> updateToscaElement(T componentToUpdate, ComponentParametersView filterResult) {
        String componentId = componentToUpdate.getUniqueId();
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseAll);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex elementV = getVertexEither.left().value();
        ToscaElementOperation toscaElementOperation = getToscaElementOperation(elementV);
        ToscaElement toscaElementToUpdate = ModelConverter.convertToToscaElement(componentToUpdate);
        Either<ToscaElement, StorageOperationStatus> updateToscaElement = null;
        if (toscaElementOperation != null) {
            updateToscaElement = toscaElementOperation.updateToscaElement(toscaElementToUpdate, elementV, filterResult);
        } else {
            log.debug("Null value returned by `getToscaElementOperation` with value {}", elementV);
            updateToscaElement = Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
        return updateToscaElement.bimap(ModelConverter::convertFromToscaElement, status -> {
            log.debug("Failed to update tosca element {} error {}", componentId, status);
            return status;
        });
    }

    private <T extends Component> Either<T, StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName,
                                                                                    JsonParseFlagEnum parseFlag, String modelName) {
        return getLatestByName(property, nodeName, parseFlag, new ComponentParametersView(), modelName);
    }

    private <T extends Component> Either<T, StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName,
                                                                                    JsonParseFlagEnum parseFlag, ComponentParametersView filter,
                                                                                    String model) {
        Either<T, StorageOperationStatus> result;
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(property, nodeName);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> highestResources = janusGraphDao
            .getByCriteria(null, propertiesToMatch, propertiesNotToMatch, parseFlag, model);
        if (highestResources.isRight()) {
            JanusGraphOperationStatus status = highestResources.right().value();
            log.debug("failed to find resource with name {}. status={} ", nodeName, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
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

    // region - Component Get By ..
    private <T extends Component> Either<T, StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName, String modelName) {
        return getLatestByName(property, nodeName, JsonParseFlagEnum.ParseMetadata, modelName);
    }

    public <T extends Component> Either<List<T>, StorageOperationStatus> getBySystemName(ComponentTypeEnum componentType, String systemName) {
        Either<List<T>, StorageOperationStatus> result = null;
        Either<T, StorageOperationStatus> getComponentRes;
        List<T> components = new ArrayList<>();
        List<GraphVertex> componentVertices;
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.SYSTEM_NAME, systemName);
        if (componentType != null) {
            propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        }
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> getComponentsRes = janusGraphDao
            .getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        if (getComponentsRes.isRight()) {
            JanusGraphOperationStatus status = getComponentsRes.right().value();
            log.debug("Failed to fetch the component with system name {}. Status is {} ", systemName, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        if (result == null) {
            componentVertices = getComponentsRes.left().value();
            for (GraphVertex componentVertex : componentVertices) {
                getComponentRes = getToscaElementByOperation(componentVertex);
                if (getComponentRes.isRight()) {
                    log.debug("Failed to get the component {}. Status is {} ", componentVertex.getJsonMetadataField(JsonPresentationFields.NAME),
                        getComponentRes.right().value());
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

    public <T extends Component> Either<T, StorageOperationStatus> getComponentByNameAndVersion(ComponentTypeEnum componentType, String name,
                                                                                                String version) {
        return getComponentByNameAndVersion(componentType, name, version, JsonParseFlagEnum.ParseAll);
    }

    public <T extends Component> Either<T, StorageOperationStatus> getComponentByNameAndVersion(ComponentTypeEnum componentType, String name,
                                                                                                String version, JsonParseFlagEnum parseFlag) {
        Either<T, StorageOperationStatus> result;
        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProperties = new EnumMap<>(GraphPropertyEnum.class);
        hasProperties.put(GraphPropertyEnum.NAME, name);
        hasProperties.put(GraphPropertyEnum.VERSION, version);
        hasNotProperties.put(GraphPropertyEnum.IS_DELETED, true);
        if (componentType != null) {
            hasProperties.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        }
        Either<List<GraphVertex>, JanusGraphOperationStatus> getResourceRes = janusGraphDao
            .getByCriteria(null, hasProperties, hasNotProperties, parseFlag);
        if (getResourceRes.isRight()) {
            JanusGraphOperationStatus status = getResourceRes.right().value();
            log.debug("failed to find resource with name {}, version {}. Status is {} ", name, version, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            return result;
        }
        return getToscaElementByOperation(getResourceRes.left().value().get(0));
    }

    public <T extends Component> Either<T, StorageOperationStatus> getComponentByNameAndVendorRelease(final ComponentTypeEnum componentType,
                                                                                                      final String name, final String vendorRelease,
                                                                                                      final JsonParseFlagEnum parseFlag,
                                                                                                      final String modelName) {
        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProperties = new EnumMap<>(GraphPropertyEnum.class);
        hasProperties.put(GraphPropertyEnum.NAME, name);
        hasNotProperties.put(GraphPropertyEnum.IS_DELETED, true);
        if (componentType != null) {
            hasProperties.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        }
        Map<String, Entry<JanusGraphPredicate, Object>> predicateCriteria = getVendorVersionPredicate(vendorRelease);
        Either<List<GraphVertex>, JanusGraphOperationStatus> getResourceRes = janusGraphDao.getByCriteria(null, hasProperties, hasNotProperties,
            predicateCriteria, parseFlag, modelName);
        if (getResourceRes.isRight()) {
            JanusGraphOperationStatus status = getResourceRes.right().value();
            log.debug("failed to find resource with name {}, version {}. Status is {} ", name, predicateCriteria, status);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        return getToscaElementByOperation(getResourceRes.left().value().get(0));
    }

    public Either<List<CatalogComponent>, StorageOperationStatus> getCatalogOrArchiveComponents(boolean isCatalog,
                                                                                                List<OriginTypeEnum> excludeTypes) {
        List<ResourceTypeEnum> excludedResourceTypes = Optional.ofNullable(excludeTypes).orElse(Collections.emptyList()).stream()
            .filter(type -> !type.equals(OriginTypeEnum.SERVICE)).map(type -> ResourceTypeEnum.getTypeByName(type.name()))
            .collect(Collectors.toList());
        return topologyTemplateOperation.getElementCatalogData(isCatalog, excludedResourceTypes);
    }

    // endregion
    public <T extends Component> Either<List<T>, StorageOperationStatus> getCatalogComponents(ComponentTypeEnum componentType,
                                                                                              List<OriginTypeEnum> excludeTypes,
                                                                                              boolean isHighestVersions) {
        List<T> components = new ArrayList<>();
        Either<List<ToscaElement>, StorageOperationStatus> catalogDataResult;
        List<ToscaElement> toscaElements = new ArrayList<>();
        List<ResourceTypeEnum> excludedResourceTypes = Optional.ofNullable(excludeTypes).orElse(Collections.emptyList()).stream()
            .filter(type -> !type.equals(OriginTypeEnum.SERVICE)).map(type -> ResourceTypeEnum.getTypeByName(type.name()))
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

    public List<String> deleteService(String serviceID) {
        Either<GraphVertex, StorageOperationStatus> componentToDelete = topologyTemplateOperation
                .getComponentByLabelAndId(serviceID, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll);
        if (componentToDelete.isRight()) {
            throwStorageException(componentToDelete.right().value());
        }
        GraphVertex highestVersion = topologyTemplateOperation.getHighestVersionFrom(componentToDelete.left().value());
        highestVersion = janusGraphDao.getVertexById(highestVersion.getUniqueId(), JsonParseFlagEnum.ParseAll).left().value();
        List<GraphVertex> allServiceVerticesToDelete = new ArrayList<>();
        allServiceVerticesToDelete.add(highestVersion);
        List<GraphVertex> allParents = getAllParents(highestVersion);
        if (allParents != null) {
            allServiceVerticesToDelete.addAll(allParents);
        }
        boolean isServiceInUse = isAnyComponentInUse(allServiceVerticesToDelete);
        if (isServiceInUse) {
            List<GraphVertex> listOfServices = getComponentsUsingComponent(allServiceVerticesToDelete);
            List<String> listOfStringServices = new ArrayList<>();
            for (GraphVertex serviceVertex : listOfServices) {
                listOfStringServices.add( serviceVertex.getMetadataJson().get("componentType") + " " + serviceVertex.getMetadataJson().get("name"));
            }
            String stringOfServices = String.join(", ", listOfStringServices);
            throw ModelOperationExceptionSupplier.componentInUse(stringOfServices).get();
        }
        List<String> affectedComponentIds = new ArrayList<>();
        try {
            for (GraphVertex elementV : allServiceVerticesToDelete) {
                StorageOperationStatus storageOperationStatus = graphLockOperation.lockComponent(elementV.getUniqueId(), NodeTypeEnum.Service);
                if (!storageOperationStatus.equals(StorageOperationStatus.OK)) {
                    throwStorageException(storageOperationStatus);
                }
                Either<ToscaElement, StorageOperationStatus> deleteToscaElement = deleteToscaElement(elementV);
                if (deleteToscaElement.isRight()) {
                    log.debug("Failed to delete element UniqueID {}, Name {}, error {}", elementV.getUniqueId(),
                        elementV.getMetadataProperties().get(GraphPropertyEnum.NAME), deleteToscaElement.right().value());
                    throwStorageException(deleteToscaElement.right().value());
                }
                affectedComponentIds.add(elementV.getUniqueId());
            }
            commitAndCheck(highestVersion.getUniqueId());
        } catch (Exception exception) {
            janusGraphDao.rollback();
            throw exception;
        } finally {
            for (GraphVertex elementV : allServiceVerticesToDelete) {
                graphLockOperation.unlockComponent(elementV.getUniqueId(), NodeTypeEnum.Service);
            }
        }
        return affectedComponentIds;
    }

    private List<GraphVertex> getAllParents(GraphVertex vertex) {
        Either<List<GraphVertex>, JanusGraphOperationStatus> parentVertices = janusGraphDao
                .getParentVertices(vertex, EdgeLabelEnum.VERSION, JsonParseFlagEnum.ParseAll);
        if (parentVertices.isRight()) {
            log.debug("No parent vortices found for {}", vertex.getUniqueId());
            return null;
        }
        Set<GraphVertex> allParents = new TreeSet<>(Comparator.comparing(GraphVertex::getUniqueId));
        for (GraphVertex currentParentVertex: parentVertices.left().value()) {
            GraphVertex currentVertex = currentParentVertex;
            while (currentVertex != null) {
                Either<List<GraphVertex>, JanusGraphOperationStatus> currentVertexParentVertices =
                    janusGraphDao.getParentVertices(currentVertex, EdgeLabelEnum.VERSION, JsonParseFlagEnum.ParseAll);
                if (currentVertexParentVertices.isLeft()) {
                    allParents.addAll(currentVertexParentVertices.left().value());
                }
                allParents.add(currentVertex);
                currentVertex = janusGraphDao.getParentVertex(currentVertex, EdgeLabelEnum.VERSION, JsonParseFlagEnum.ParseAll)
                    .either(graphVertex -> graphVertex, janusGraphOperationStatus -> null);
            }
        }
        return new ArrayList<>(allParents);
    }

    private void commitAndCheck(String componentId) {
        JanusGraphOperationStatus status = janusGraphDao.commit();
        if (!status.equals(JanusGraphOperationStatus.OK)) {
            log.debug("error occurred when trying to DELETE {}. Return code is: {}", componentId, status);
            throwStorageException(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
    }

    private List<GraphVertex> getComponentsUsingComponent(List<GraphVertex> componentVertices) {
        Set<GraphVertex> inUseBy = new TreeSet<>(Comparator.comparing(GraphVertex::getUniqueId));
        for (final GraphVertex elementV : componentVertices) {
            final List<EdgeLabelEnum> forbiddenEdgeLabelEnums = Arrays
                    .asList(EdgeLabelEnum.INSTANCE_OF, EdgeLabelEnum.PROXY_OF, EdgeLabelEnum.ALLOTTED_OF);
            for (EdgeLabelEnum edgeLabelEnum : forbiddenEdgeLabelEnums) {
                Either<Edge, JanusGraphOperationStatus> belongingEdgeByCriteria = janusGraphDao
                        .getBelongingEdgeByCriteria(elementV, edgeLabelEnum, null);
                if (belongingEdgeByCriteria.isLeft()) {
                    Either<List<GraphVertex>, JanusGraphOperationStatus> inUseByVertex =
                            janusGraphDao.getParentVertices(elementV, edgeLabelEnum, JsonParseFlagEnum.ParseAll);
                    if (inUseByVertex.isLeft()) {
                        inUseBy.addAll(inUseByVertex.left().value());
                    }
                }
            }
        }
        return new ArrayList<>(inUseBy);
    }

    private boolean isAnyComponentInUse(List<GraphVertex> componentVertices) {
        boolean isComponentInUse = false;
        if (log.isDebugEnabled()) {
            for (final GraphVertex graphVertex : componentVertices) {
                if (checkIfInUse(graphVertex)) {
                    isComponentInUse = true;
                }
            }
        } else {
            isComponentInUse = componentVertices.stream().anyMatch(this::checkIfInUse);
        }
        return isComponentInUse;
    }

    private boolean checkIfInUse(GraphVertex elementV) {
        final List<EdgeLabelEnum> forbiddenEdgeLabelEnums = Arrays
                .asList(EdgeLabelEnum.INSTANCE_OF, EdgeLabelEnum.PROXY_OF, EdgeLabelEnum.ALLOTTED_OF);
        for (EdgeLabelEnum edgeLabelEnum : forbiddenEdgeLabelEnums) {
            Either<Edge, JanusGraphOperationStatus> belongingEdgeByCriteria = janusGraphDao
                    .getBelongingEdgeByCriteria(elementV, edgeLabelEnum, null);
            if (belongingEdgeByCriteria.isLeft()) {
                Either<List<GraphVertex>, JanusGraphOperationStatus> inUseBy =
                        janusGraphDao.getParentVertices(elementV, edgeLabelEnum, JsonParseFlagEnum.ParseAll);
                if (inUseBy.isLeft()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Element {} in use.", elementV.getUniqueId());
                        for (GraphVertex v : inUseBy.left().value()) {
                            log.debug("Unable to delete {} {}. It is in use by {} {}.", elementV.getType(), elementV.getUniqueId(),
                                    v.getType(), v.getUniqueId());
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> checkIfInUseAndDelete(List<GraphVertex> allMarked) {
        List<String> deleted = new ArrayList<>();
        for (GraphVertex elementV : allMarked) {
            boolean isAllowedToDelete = !checkIfInUse(elementV);
            if (isAllowedToDelete) {
                Either<ToscaElement, StorageOperationStatus> deleteToscaElement = deleteToscaElement(elementV);
                if (deleteToscaElement.isRight()) {
                    log.debug("Failed to delete marked element UniqueID {}, Name {}, error {}", elementV.getUniqueId(),
                        elementV.getMetadataProperties().get(GraphPropertyEnum.NAME), deleteToscaElement.right().value());
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
    public Either<ImmutablePair<Component, String>, StorageOperationStatus> addComponentInstanceToTopologyTemplate(Component containerComponent,
                                                                                                                   Component origComponent,
                                                                                                                   ComponentInstance componentInstance,
                                                                                                                   boolean allowDeleted, User user) {
        Either<ImmutablePair<Component, String>, StorageOperationStatus> result = null;
        Either<ToscaElement, StorageOperationStatus> updateContainerComponentRes = null;
        if (StringUtils.isEmpty(componentInstance.getIcon())) {
            componentInstance.setIcon(origComponent.getIcon());
        }
        String nameToFindForCounter;
        switch (componentInstance.getOriginType()) {
            case ServiceProxy:
                nameToFindForCounter = ValidationUtils.normaliseComponentName(componentInstance.getSourceModelName()) + PROXY_SUFFIX;
                break;
            case ServiceSubstitution:
                nameToFindForCounter = ValidationUtils.normaliseComponentName(componentInstance.getSourceModelName());
                break;
            default:
                nameToFindForCounter = origComponent.getName();
        }
        String nextComponentInstanceCounter = getNextComponentInstanceCounter(containerComponent, nameToFindForCounter);
        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> addResult = nodeTemplateOperation
            .addComponentInstanceToTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent),
                ModelConverter.convertToToscaElement(origComponent), nextComponentInstanceCounter, componentInstance, allowDeleted, user);
        if (addResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add the component instance {} to container component {}. ",
                componentInstance.getName(), containerComponent.getName());
            result = Either.right(addResult.right().value());
        }
        if (result == null) {
            updateContainerComponentRes = topologyTemplateOperation.getToscaElement(containerComponent.getUniqueId());
            if (updateContainerComponentRes.isRight()) {
                CommonUtility
                    .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch updated topology template {} with updated component instance {}. ",
                        containerComponent.getName(), componentInstance.getName());
                result = Either.right(updateContainerComponentRes.right().value());
            }
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateContainerComponentRes.left().value());
            String createdInstanceId = addResult.left().value().getRight();
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.TRACE, "The component instance {} has been added to container component {}. ", createdInstanceId,
                    updatedComponent.getName());
            result = Either.left(new ImmutablePair<>(updatedComponent, createdInstanceId));
        }
        return result;
    }

    public void associateComponentInstancesToComponent(Component containerComponent, Map<ComponentInstance, Resource> resourcesInstancesMap,
                                                       boolean allowDeleted, boolean isUpdateCsar) {
        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Going to add component instances to component {}", containerComponent.getUniqueId());
        Either<GraphVertex, JanusGraphOperationStatus> metadataVertex = janusGraphDao
            .getVertexById(containerComponent.getUniqueId(), JsonParseFlagEnum.ParseAll);
        if (metadataVertex.isRight()) {
            JanusGraphOperationStatus status = metadataVertex.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.INVALID_ID;
            }
            throw new StorageException(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        Map<String, ComponentInstanceDataDefinition> compnentInstancesMap = nodeTemplateOperation
            .associateComponentInstancesToComponent(containerComponent, resourcesInstancesMap, metadataVertex.left().value(), allowDeleted,
                isUpdateCsar);
        containerComponent.setComponentInstances(ModelConverter.getComponentInstancesFromMapObject(compnentInstancesMap, containerComponent));
    }

    public Either<ImmutablePair<Component, String>, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(
        Component containerComponent, Component origComponent, ComponentInstance componentInstance) {
        Either<ImmutablePair<Component, String>, StorageOperationStatus> result = null;
        CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE,
            "Going to update the metadata of the component instance {} belonging to container component {}. ", componentInstance.getName(),
            containerComponent.getName());
        componentInstance.setIcon(origComponent.getIcon());
        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> updateResult = nodeTemplateOperation
            .updateComponentInstanceMetadataOfTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent),
                ModelConverter.convertToToscaElement(origComponent), componentInstance);
        if (updateResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG,
                "Failed to update the metadata of the component instance {} belonging to container component {}. ", componentInstance.getName(),
                containerComponent.getName());
            result = Either.right(updateResult.right().value());
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateResult.left().value().getLeft());
            String createdInstanceId = updateResult.left().value().getRight();
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.TRACE, "The metadata of the component instance {} has been updated to container component {}. ",
                    createdInstanceId, updatedComponent.getName());
            result = Either.left(new ImmutablePair<>(updatedComponent, createdInstanceId));
        }
        return result;
    }

    public Either<Component, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(Component containerComponent) {
        return updateComponentInstanceMetadataOfTopologyTemplate(containerComponent, new ComponentParametersView());
    }

    public Either<Component, StorageOperationStatus> updateComponentInstanceMetadataOfTopologyTemplate(Component containerComponent,
                                                                                                       ComponentParametersView filter) {
        Either<Component, StorageOperationStatus> result = null;
        CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to update the metadata  belonging to container component {}. ",
            containerComponent.getName());
        Either<TopologyTemplate, StorageOperationStatus> updateResult = nodeTemplateOperation
            .updateComponentInstanceMetadataOfTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent), filter);
        if (updateResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the metadata  belonging to container component {}. ",
                containerComponent.getName());
            result = Either.right(updateResult.right().value());
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateResult.left().value());
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.TRACE, "The metadata has been updated to container component {}. ", updatedComponent.getName());
            result = Either.left(updatedComponent);
        }
        return result;
    }

    // endregion
    public Either<ImmutablePair<Component, String>, StorageOperationStatus> deleteComponentInstanceFromTopologyTemplate(Component containerComponent,
                                                                                                                        String resourceInstanceId) {
        Either<ImmutablePair<Component, String>, StorageOperationStatus> result = null;
        CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to delete the component instance {} belonging to container component {}. ",
            resourceInstanceId, containerComponent.getName());
        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> updateResult = nodeTemplateOperation
            .deleteComponentInstanceFromTopologyTemplate(ModelConverter.convertToToscaElement(containerComponent), resourceInstanceId);
        if (updateResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete the component instance {} belonging to container component {}. ",
                resourceInstanceId, containerComponent.getName());
            result = Either.right(updateResult.right().value());
        }
        if (result == null) {
            Component updatedComponent = ModelConverter.convertFromToscaElement(updateResult.left().value().getLeft());
            String deletedInstanceId = updateResult.left().value().getRight();
            CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "The component instance {} has been deleted from container component {}. ",
                deletedInstanceId, updatedComponent.getName());
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
            .map(ci -> ci.getNormalizedName().split(normalizedName)[1]).collect(Collectors.toList());
        List<String> countersInIds = containerComponent.getComponentInstances().stream()
            .filter(ci -> ci.getUniqueId() != null && ci.getUniqueId().contains(normalizedName)).map(ci -> ci.getUniqueId().split(normalizedName)[1])
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

    public Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances(Component component, String componentId,
                                                                                                  RequirementCapabilityRelDef requirementDef) {
        return nodeTemplateOperation.associateResourceInstances(component, componentId, requirementDef);
    }

    public Either<List<InputDefinition>, StorageOperationStatus> createAndAssociateInputs(Map<String, InputDefinition> inputs, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, PropertyDataDefinition> inputsMap = inputs.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new PropertyDataDefinition(e.getValue())));
        StorageOperationStatus status = topologyTemplateOperation.associateInputsToComponent(vertex, inputsMap, componentId);
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

    public Either<List<InputDefinition>, StorageOperationStatus> addInputsToComponent(Map<String, InputDefinition> inputs, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, PropertyDefinition> inputsMap = inputs.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new PropertyDefinition(e.getValue())));
        StorageOperationStatus status = topologyTemplateOperation
            .addToscaDataToToscaElement(vertex, EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, inputsMap, JsonPresentationFields.NAME);
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

    public Either<List<OutputDefinition>, StorageOperationStatus> addOutputsToComponent(Map<String, OutputDefinition> outputs, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, AttributeDefinition> outputsMap = outputs.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new AttributeDefinition(e.getValue())));
        StorageOperationStatus status = topologyTemplateOperation
            .addToscaDataToToscaElement(vertex, EdgeLabelEnum.OUTPUTS, VertexTypeEnum.OUTPUTS, outputsMap, JsonPresentationFields.NAME);
        if (StorageOperationStatus.OK == status) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            List<OutputDefinition> outputsResList = null;
            if (outputsMap != null && !outputsMap.isEmpty()) {
                outputsResList = outputsMap.values().stream().map(OutputDefinition::new).collect(Collectors.toList());
            }
            return Either.left(outputsResList);
        }
        return Either.right(status);
    }

    /**
     * Add data types into a Component.
     *
     * @param dataTypes   datatypes to be added. the key should be each name of data type.
     * @param componentId unique ID of Component.
     * @return list of data types.
     */
    public Either<List<DataTypeDefinition>, StorageOperationStatus> addDataTypesToComponent(Map<String, DataTypeDefinition> dataTypes,
                                                                                            String componentId) {
        log.trace("#addDataTypesToComponent - enter, componentId={}", componentId);
        /* get component vertex */
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao
            .getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            /* not found / error */
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex vertex = getVertexEither.left().value();
        log.trace("#addDataTypesToComponent - get vertex ok");
        // convert DataTypeDefinition to DataTypeDataDefinition
        Map<String, DataTypeDataDefinition> dataTypeDataMap = dataTypes.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> convertDataTypeToDataTypeData(e.getValue())));
        // add datatype(s) to the Component.

        // if child vertex does not exist, it will be created.
        StorageOperationStatus status = topologyTemplateOperation
            .addToscaDataToToscaElement(vertex, EdgeLabelEnum.DATA_TYPES, VertexTypeEnum.DATA_TYPES, dataTypeDataMap, JsonPresentationFields.NAME);
        if (StorageOperationStatus.OK == status) {
            log.debug(COMPONENT_CREATED_SUCCESSFULLY);
            List<DataTypeDefinition> inputsResList = null;
            if (!dataTypes.isEmpty()) {
                inputsResList = new ArrayList<>(dataTypes.values());
            }
            return Either.left(inputsResList);
        }
        log.trace("#addDataTypesToComponent - leave");
        return Either.right(status);
    }

    private DataTypeDataDefinition convertDataTypeToDataTypeData(DataTypeDefinition dataType) {
        DataTypeDataDefinition dataTypeData = new DataTypeDataDefinition(dataType);
        if (CollectionUtils.isNotEmpty(dataType.getProperties())) {
            List<PropertyDataDefinition> propertyDataList = dataType.getProperties().stream().map(PropertyDataDefinition::new)
                .collect(Collectors.toList());
            dataTypeData.setPropertiesData(propertyDataList);
        }
        // if "derivedFrom" data_type exists, copy the name to "derivedFromName"
        if (dataType.getDerivedFrom() != null && StringUtils.isNotEmpty(dataType.getDerivedFrom().getName())) {
            // if names are different, log it
            if (!StringUtils.equals(dataTypeData.getDerivedFromName(), dataType.getDerivedFrom().getName())) {
                log.debug("#convertDataTypeToDataTypeData - derivedFromName(={}) overwritten by derivedFrom.name(={})", dataType.getDerivedFromName(),
                    dataType.getDerivedFrom().getName());
            }
            dataTypeData.setDerivedFromName(dataType.getDerivedFrom().getName());
        }
        // supply "name" field to toscaPresentationValue in each datatype object for DAO operations
        dataTypeData.setToscaPresentationValue(JsonPresentationFields.NAME, dataType.getName());
        return dataTypeData;
    }

    public Either<List<InputDefinition>, StorageOperationStatus> getComponentInputs(String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        Either<ToscaElement, StorageOperationStatus> toscaElement = topologyTemplateOperation.getToscaElement(componentId);
        if (toscaElement.isRight()) {
            return Either.right(toscaElement.right().value());
        }
        TopologyTemplate topologyTemplate = (TopologyTemplate) toscaElement.left().value();
        Map<String, PropertyDataDefinition> inputsMap = topologyTemplate.getInputs();
        List<InputDefinition> inputs = new ArrayList<>();
        if (MapUtils.isNotEmpty(inputsMap)) {
            inputs = inputsMap.values().stream().map(p -> new InputDefinition(p)).collect(Collectors.toList());
        }
        return Either.left(inputs);
    }

    public Either<List<InputDefinition>, StorageOperationStatus> updateInputsToComponent(List<InputDefinition> inputs, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex vertex = getVertexEither.left().value();
        List<PropertyDataDefinition> inputsAsDataDef = inputs.stream().map(PropertyDataDefinition::new).collect(Collectors.toList());
        StorageOperationStatus status = topologyTemplateOperation
            .updateToscaDataOfToscaElement(vertex, EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, inputsAsDataDef, JsonPresentationFields.NAME);
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
    public Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> associateComponentInstancePropertiesToComponent(
        Map<String, List<ComponentInstanceProperty>> instProperties, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapPropertiesDataDefinition> instPropsMap = new HashMap<>();
        if (instProperties != null) {
            MapPropertiesDataDefinition propertiesMap;
            for (Entry<String, List<ComponentInstanceProperty>> entry : instProperties.entrySet()) {
                propertiesMap = new MapPropertiesDataDefinition();
                propertiesMap.setMapToscaDataDefinition(
                    entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, e -> e)));
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
    public Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> updateComponentInstanceInputsToComponent(
        Map<String, List<ComponentInstanceInput>> instInputs, String componentId) {
        if (instInputs == null || instInputs.isEmpty()) {
            return Either.left(instInputs);
        }
        StorageOperationStatus status;
        for (Entry<String, List<ComponentInstanceInput>> inputsPerIntance : instInputs.entrySet()) {
            List<ComponentInstanceInput> toscaDataListPerInst = inputsPerIntance.getValue();
            List<String> pathKeysPerInst = new ArrayList<>();
            pathKeysPerInst.add(inputsPerIntance.getKey());
            status = topologyTemplateOperation
                .updateToscaDataDeepElementsOfToscaElement(componentId, EdgeLabelEnum.INST_INPUTS, VertexTypeEnum.INST_INPUTS, toscaDataListPerInst,
                    pathKeysPerInst, JsonPresentationFields.NAME);
            if (status != StorageOperationStatus.OK) {
                log.debug("Failed to update component instance inputs for instance {} in component {} edge type {} error {}",
                    inputsPerIntance.getKey(), componentId, EdgeLabelEnum.INST_INPUTS, status);
                return Either.right(status);
            }
        }
        return Either.left(instInputs);
    }

    /**
     * saves the instProps as the updated instance properties of the component container in DB
     */
    public Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> updateComponentInstancePropsToComponent(
        Map<String, List<ComponentInstanceProperty>> instProps, String componentId) {
        if (instProps == null || instProps.isEmpty()) {
            return Either.left(instProps);
        }
        StorageOperationStatus status;
        for (Entry<String, List<ComponentInstanceProperty>> propsPerIntance : instProps.entrySet()) {
            List<ComponentInstanceProperty> toscaDataListPerInst = propsPerIntance.getValue();
            List<String> pathKeysPerInst = new ArrayList<>();
            pathKeysPerInst.add(propsPerIntance.getKey());
            status = topologyTemplateOperation
                .updateToscaDataDeepElementsOfToscaElement(componentId, EdgeLabelEnum.INST_PROPERTIES, VertexTypeEnum.INST_PROPERTIES,
                    toscaDataListPerInst, pathKeysPerInst, JsonPresentationFields.NAME);
            if (status != StorageOperationStatus.OK) {
                log.debug("Failed to update component instance inputs for instance {} in component {} edge type {} error {}",
                    propsPerIntance.getKey(), componentId, EdgeLabelEnum.INST_PROPERTIES, status);
                return Either.right(status);
            }
        }
        return Either.left(instProps);
    }

    public Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> associateComponentInstanceInputsToComponent(
        Map<String, List<ComponentInstanceInput>> instInputs, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapPropertiesDataDefinition> instPropsMap = new HashMap<>();
        if (instInputs != null) {
            MapPropertiesDataDefinition propertiesMap;
            for (Entry<String, List<ComponentInstanceInput>> entry : instInputs.entrySet()) {
                propertiesMap = new MapPropertiesDataDefinition();
                propertiesMap.setMapToscaDataDefinition(
                    entry.getValue().stream().map(PropertyDataDefinition::new).collect(Collectors.toMap(PropertyDataDefinition::getName, e -> e)));
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

    public Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addComponentInstanceInputsToComponent(
        Component containerComponent, Map<String, List<ComponentInstanceInput>> instProperties) {
        requireNonNull(instProperties);
        StorageOperationStatus status;
        for (Entry<String, List<ComponentInstanceInput>> entry : instProperties.entrySet()) {
            List<ComponentInstanceInput> props = entry.getValue();
            String componentInstanceId = entry.getKey();
            if (!isEmpty(props)) {
                for (ComponentInstanceInput property : props) {
                    List<ComponentInstanceInput> componentInstancesInputs = containerComponent.getComponentInstancesInputs().get(componentInstanceId);
                    Optional<ComponentInstanceInput> instanceProperty = componentInstancesInputs.stream()
                        .filter(p -> p.getName().equals(property.getName())).findAny();
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

    public Either<Map<String, List<ComponentInstanceOutput>>, StorageOperationStatus> addComponentInstanceOutputsToComponent(
        Component containerComponent, Map<String, List<ComponentInstanceOutput>> instProperties) {
        requireNonNull(instProperties);
        StorageOperationStatus status;
        for (final Entry<String, List<ComponentInstanceOutput>> entry : instProperties.entrySet()) {
            final List<ComponentInstanceOutput> props = entry.getValue();
            final String componentInstanceId = entry.getKey();
            if (!isEmpty(props)) {
                for (final ComponentInstanceOutput property : props) {
                    final List<ComponentInstanceOutput> componentInstancesInputs = containerComponent.getComponentInstancesOutputs()
                        .get(componentInstanceId);
                    final Optional<ComponentInstanceOutput> instanceProperty = componentInstancesInputs.stream()
                        .filter(p -> p.getName().equals(property.getName())).findAny();
                    if (instanceProperty.isPresent()) {
                        status = updateComponentInstanceOutput(containerComponent, componentInstanceId, property);
                    } else {
                        status = addComponentInstanceOutput(containerComponent, componentInstanceId, property);
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

    public Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addComponentInstancePropertiesToComponent(
        Component containerComponent, Map<String, List<ComponentInstanceProperty>> instProperties) {
        requireNonNull(instProperties);
        for (Entry<String, List<ComponentInstanceProperty>> entry : instProperties.entrySet()) {
            List<ComponentInstanceProperty> props = entry.getValue();
            String componentInstanceId = entry.getKey();
            List<ComponentInstanceProperty> originalComponentInstProps = containerComponent.getComponentInstancesProperties()
                .get(componentInstanceId);
            Map<String, List<CapabilityDefinition>> containerComponentCapabilities = containerComponent.getCapabilities();
            if (isEmpty(props)) {
                continue;
            }
            for (ComponentInstanceProperty property : props) {
                StorageOperationStatus status = null;
                String propertyParentUniqueId = property.getParentUniqueId();
                Optional<CapabilityDefinition> capPropDefinition = getPropertyCapability(propertyParentUniqueId, containerComponent);
                if (capPropDefinition.isPresent() && MapUtils.isNotEmpty(containerComponentCapabilities)) {
                    status = populateAndUpdateInstanceCapProperty(containerComponent, componentInstanceId, containerComponentCapabilities, property,
                        capPropDefinition.get());
                }
                if (status == null) {
                    status = updateOrAddComponentInstanceProperty(containerComponent, componentInstanceId, originalComponentInstProps, property);
                }
                if (status != StorageOperationStatus.OK) {
                    return Either.right(status);
                }
            }
        }
        return Either.left(instProperties);
    }

    public Either<Map<String, List<ComponentInstanceAttribute>>, StorageOperationStatus> addComponentInstanceAttributesToComponent(
        final Component containerComponent, final Map<String, List<ComponentInstanceAttribute>> instProperties) {
        requireNonNull(instProperties);
        for (final Entry<String, List<ComponentInstanceAttribute>> entry : instProperties.entrySet()) {
            final List<ComponentInstanceAttribute> props = entry.getValue();
            if (isEmpty(props)) {
                continue;
            }
            final String componentInstanceId = entry.getKey();
            final List<ComponentInstanceAttribute> originalComponentInstProps = containerComponent.getComponentInstancesAttributes()
                .get(componentInstanceId);
            for (final ComponentInstanceAttribute property : props) {
                final StorageOperationStatus status = updateOrAddComponentInstanceAttribute(containerComponent, componentInstanceId,
                    originalComponentInstProps, property);
                if (status != StorageOperationStatus.OK) {
                    return Either.right(status);
                }
            }
        }
        return Either.left(instProperties);
    }

    private StorageOperationStatus populateAndUpdateInstanceCapProperty(Component containerComponent, String componentInstanceId,
                                                                        Map<String, List<CapabilityDefinition>> containerComponentCapabilities,
                                                                        ComponentInstanceProperty property,
                                                                        CapabilityDefinition capabilityDefinition) {
        List<CapabilityDefinition> capabilityDefinitions = containerComponentCapabilities.get(capabilityDefinition.getType());
        if (CollectionUtils.isEmpty(capabilityDefinitions)) {
            return null;
        }
        Optional<CapabilityDefinition> capDefToGetProp = capabilityDefinitions.stream()
            .filter(cap -> cap.getUniqueId().equals(capabilityDefinition.getUniqueId()) && cap.getPath().size() == 1).findAny();
        if (capDefToGetProp.isPresent()) {
            return updateInstanceCapabilityProperty(containerComponent, componentInstanceId, property, capDefToGetProp.get());
        }
        return null;
    }

    private StorageOperationStatus updateOrAddComponentInstanceProperty(Component containerComponent, String componentInstanceId,
                                                                        List<ComponentInstanceProperty> originalComponentInstProps,
                                                                        ComponentInstanceProperty property) {
        StorageOperationStatus status;
        // check if the property already exists or not
        Optional<ComponentInstanceProperty> instanceProperty = originalComponentInstProps.stream()
            .filter(p -> p.getUniqueId().equals(property.getUniqueId())).findAny();
        if (instanceProperty.isPresent()) {
            status = updateComponentInstanceProperty(containerComponent, componentInstanceId, property);
        } else {
            status = addComponentInstanceProperty(containerComponent, componentInstanceId, property);
        }
        if (status != StorageOperationStatus.OK) {
            log.debug("Failed to update instance property {} for instance {} error {} ", property, componentInstanceId, status);
        }
        return status;
    }

    private StorageOperationStatus updateOrAddComponentInstanceAttribute(Component containerComponent, String componentInstanceId,
                                                                         List<ComponentInstanceAttribute> originalComponentInstProps,
                                                                         ComponentInstanceAttribute property) {
        StorageOperationStatus status;
        // check if the property already exists or not
        Optional<ComponentInstanceAttribute> instanceProperty = originalComponentInstProps.stream()
            .filter(p -> p.getUniqueId().equals(property.getUniqueId())).findAny();
        if (instanceProperty.isPresent()) {
            status = updateComponentInstanceAttribute(containerComponent, componentInstanceId, property);
        } else {
            status = addComponentInstanceAttribute(containerComponent, componentInstanceId, property);
        }
        if (status != StorageOperationStatus.OK) {
            log.debug("Failed to update instance property {} for instance {} error {} ", property, componentInstanceId, status);
        }
        return status;
    }

    public StorageOperationStatus updateInstanceCapabilityProperty(Component containerComponent, String componentInstanceId,
                                                                   ComponentInstanceProperty property, CapabilityDefinition capabilityDefinition) {
        Optional<ComponentInstance> fetchedCIOptional = containerComponent.getComponentInstanceById(componentInstanceId);
        if (!fetchedCIOptional.isPresent()) {
            return StorageOperationStatus.GENERAL_ERROR;
        }
        Either<Component, StorageOperationStatus> getComponentRes = getToscaFullElement(fetchedCIOptional.get().getComponentUid());
        if (getComponentRes.isRight()) {
            return StorageOperationStatus.GENERAL_ERROR;
        }
        Optional<Component> componentOptional = isNodeServiceProxy(getComponentRes.left().value());
        String propOwner;
        if (!componentOptional.isPresent()) {
            propOwner = componentInstanceId;
        } else {
            propOwner = fetchedCIOptional.get().getSourceModelUid();
        }
        StorageOperationStatus status;
        StringBuilder sb = new StringBuilder(componentInstanceId);
        sb.append(ModelConverter.CAP_PROP_DELIM).append(propOwner).append(ModelConverter.CAP_PROP_DELIM).append(capabilityDefinition.getType())
            .append(ModelConverter.CAP_PROP_DELIM).append(capabilityDefinition.getName());
        String capKey = sb.toString();
        status = updateComponentInstanceCapabiltyProperty(containerComponent, componentInstanceId, capKey, property);
        if (status != StorageOperationStatus.OK) {
            log.debug("Failed to update instance capability property {} for instance {} error {} ", property, componentInstanceId, status);
            return status;
        }
        return StorageOperationStatus.OK;
    }

    private Optional<Component> isNodeServiceProxy(Component component) {
        if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
            return Optional.empty();
        }
        Resource resource = (Resource) component;
        ResourceTypeEnum resType = resource.getResourceType();
        if (resType.equals(ResourceTypeEnum.ServiceProxy)) {
            return Optional.of(component);
        }
        return Optional.empty();
    }

    public StorageOperationStatus associateCapabilitiesToService(Map<String, ListCapabilityDataDefinition> capabilities, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value());
        }
        GraphVertex vertex = getVertexEither.left().value();
        if (MapUtils.isNotEmpty(capabilities)) {
            Either<GraphVertex, StorageOperationStatus> associateElementToData = topologyTemplateOperation
                .associateElementToData(vertex, VertexTypeEnum.CAPABILITIES, EdgeLabelEnum.CAPABILITIES, capabilities);
            if (associateElementToData.isRight()) {
                return associateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus associateRequirementsToService(Map<String, ListRequirementDataDefinition> requirements, String componentId) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value());
        }
        GraphVertex vertex = getVertexEither.left().value();
        if (MapUtils.isNotEmpty(requirements)) {
            Either<GraphVertex, StorageOperationStatus> associateElementToData = topologyTemplateOperation
                .associateElementToData(vertex, VertexTypeEnum.REQUIREMENTS, EdgeLabelEnum.REQUIREMENTS, requirements);
            if (associateElementToData.isRight()) {
                return associateElementToData.right().value();
            }
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus associateDeploymentArtifactsToInstances(Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts,
                                                                          Component component, User user) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao
            .getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, component.getUniqueId(), getVertexEither.right().value());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value());
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapArtifactDataDefinition> instArtMap = new HashMap<>();
        if (instDeploymentArtifacts != null) {
            MapArtifactDataDefinition artifactsMap;
            for (Entry<String, Map<String, ArtifactDefinition>> entry : instDeploymentArtifacts.entrySet()) {
                Map<String, ArtifactDefinition> artList = entry.getValue();
                Map<String, ArtifactDataDefinition> artifacts = artList.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                artifactsMap = nodeTemplateOperation
                    .prepareInstDeploymentArtifactPerInstance(artifacts, entry.getKey(), user, NodeTemplateOperation.HEAT_VF_ENV_NAME);
                instArtMap.put(entry.getKey(), artifactsMap);
            }
        }
        ModelConverter.setComponentInstancesDeploymentArtifactsToComponent(instArtMap, component);
        return topologyTemplateOperation.associateInstDeploymentArtifactsToComponent(vertex, instArtMap);
    }

    public StorageOperationStatus associateArtifactsToInstances(Map<String, Map<String, ArtifactDefinition>> instArtifacts, Component component) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao
            .getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, component.getUniqueId(), getVertexEither.right().value());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value());
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapArtifactDataDefinition> instArtMap = new HashMap<>();
        if (instArtifacts != null) {
            MapArtifactDataDefinition artifactsMap;
            for (Entry<String, Map<String, ArtifactDefinition>> entry : instArtifacts.entrySet()) {
                Map<String, ArtifactDefinition> artList = entry.getValue();
                Map<String, ArtifactDataDefinition> artifacts = artList.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                artifactsMap = new MapArtifactDataDefinition(artifacts);
                instArtMap.put(entry.getKey(), artifactsMap);
            }
        }
        ModelConverter.setComponentInstancesInformationalArtifactsToComponent(instArtMap, component);
        return topologyTemplateOperation.associateInstArtifactsToComponent(vertex, instArtMap);
    }

    public StorageOperationStatus associateInstAttributeToComponentToInstances(Map<String, List<AttributeDefinition>> instArttributes,
                                                                               Component component) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao
            .getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, component.getUniqueId(), getVertexEither.right().value());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value());
        }
        GraphVertex vertex = getVertexEither.left().value();
        Map<String, MapAttributesDataDefinition> instAttr = new HashMap<>();
        if (instArttributes != null) {
            MapAttributesDataDefinition attributesMap;
            for (Entry<String, List<AttributeDefinition>> entry : instArttributes.entrySet()) {
                final List<AttributeDefinition> value = entry.getValue();
                attributesMap = new MapAttributesDataDefinition();
                attributesMap.setMapToscaDataDefinition(
                    value.stream().map(AttributeDefinition::new).collect(Collectors.toMap(AttributeDefinition::getName, e -> e)));
                instAttr.put(entry.getKey(), attributesMap);
            }
        }
        setComponentInstanceAttributesOnComponent(component, instAttr);
        return topologyTemplateOperation.associateInstAttributeToComponent(vertex, instAttr);
    }

    // endregion
    private void setComponentInstanceAttributesOnComponent(Component resource, Map<String, MapAttributesDataDefinition> instAttr) {
        Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes = resource.getComponentInstancesAttributes();
        if (componentInstancesAttributes == null) {
            componentInstancesAttributes = new HashMap<>();
        }
        componentInstancesAttributes.putAll(ModelConverter.getComponentInstancesAttributes(instAttr));
        resource.setComponentInstancesAttributes(componentInstancesAttributes);
    }

    public StorageOperationStatus associateOrAddCalculatedCapReq(Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties,
                                                                 Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instReg,
                                                                 Component component) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao
            .getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, component.getUniqueId(), getVertexEither.right().value());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value());
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
                    mapToscaDataDefinition.put(instCapability.getKey(), new ListCapabilityDataDefinition(
                        instCapability.getValue().stream().map(CapabilityDataDefinition::new).collect(Collectors.toList())));
                }
                ComponentInstanceDataDefinition componentInstance = new ComponentInstanceDataDefinition(entry.getKey());
                MapListCapabilityDataDefinition capMap = nodeTemplateOperation
                    .prepareCalculatedCapabiltyForNodeType(mapToscaDataDefinition, componentInstance);
                MapCapabilityProperty mapCapabilityProperty = ModelConverter
                    .convertToMapOfMapCapabiltyProperties(caps, componentInstance.getUniqueId(), true);
                calcCapabilty.put(entry.getKey().getUniqueId(), capMap);
                calculatedCapabilitiesProperties.put(entry.getKey().getUniqueId(), mapCapabilityProperty);
            }
        }
        if (instReg != null) {
            for (Entry<ComponentInstance, Map<String, List<RequirementDefinition>>> entry : instReg.entrySet()) {
                Map<String, List<RequirementDefinition>> req = entry.getValue();
                Map<String, ListRequirementDataDefinition> mapToscaDataDefinition = new HashMap<>();
                for (Entry<String, List<RequirementDefinition>> instReq : req.entrySet()) {
                    mapToscaDataDefinition.put(instReq.getKey(), new ListRequirementDataDefinition(
                        instReq.getValue().stream().map(RequirementDataDefinition::new).collect(Collectors.toList())));
                }
                MapListRequirementDataDefinition reqMap = nodeTemplateOperation
                    .prepareCalculatedRequirementForNodeType(mapToscaDataDefinition, new ComponentInstanceDataDefinition(entry.getKey()));
                String componentInstanceId = entry.getKey().getUniqueId();
                calcRequirements.put(componentInstanceId, reqMap);
            }
        }
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation
            .associateOrAddCalcCapReqToComponent(vertex, calcRequirements, calcCapabilty, calculatedCapabilitiesProperties);
        updateInstancesCapAndReqOnComponentFromDB(component);
        return storageOperationStatus;
    }

    public StorageOperationStatus updateCalculatedCapabilitiesRequirements(
        final Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties,
        final Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instReg,
        final Component component) {
        StorageOperationStatus storageOperationStatus = StorageOperationStatus.OK;
        if (instCapabilties != null) {
            for (Entry<ComponentInstance, Map<String, List<CapabilityDefinition>>> entry : instCapabilties.entrySet()) {
                final Map<String, List<CapabilityDefinition>> cap = entry.getValue();
                for (List<CapabilityDefinition> capabilityList : cap.values()) {
                    for (CapabilityDefinition capability : capabilityList) {
                        nodeTemplateOperation.updateComponentInstanceCapabilities(component.getUniqueId(), entry.getKey().getUniqueId(), capability);
                    }
                }
            }
        }
        if (instReg != null) {
            for (Entry<ComponentInstance, Map<String, List<RequirementDefinition>>> entry : instReg.entrySet()) {
                final Map<String, List<RequirementDefinition>> req = entry.getValue();
                for (List<RequirementDefinition> requirementList : req.values()) {
                    for (RequirementDefinition requirement : requirementList) {
                        storageOperationStatus = nodeTemplateOperation.updateComponentInstanceRequirement(component.getUniqueId(),
                            entry.getKey().getUniqueId(), requirement);
                        if (storageOperationStatus != StorageOperationStatus.OK) {
                            return storageOperationStatus;
                        }
                    }
                }
            }
        }
        return storageOperationStatus;
    }

    private void updateInstancesCapAndReqOnComponentFromDB(Component component) {
        ComponentParametersView componentParametersView = new ComponentParametersView(true);
        componentParametersView.setIgnoreCapabilities(false);
        componentParametersView.setIgnoreRequirements(false);
        componentParametersView.setIgnoreCapabiltyProperties(false);
        componentParametersView.setIgnoreComponentInstances(false);
        Either<Component, StorageOperationStatus> componentEither = getToscaElement(component.getUniqueId(), componentParametersView);
        if (componentEither.isRight()) {
            throw new StorageException(StorageOperationStatus.NOT_FOUND);
        }
        Component updatedComponent = componentEither.left().value();
        component.setCapabilities(updatedComponent.getCapabilities());
        component.setRequirements(updatedComponent.getRequirements());
        component.setComponentInstances(updatedComponent.getComponentInstances());
    }

    private Either<List<Service>, StorageOperationStatus> getLatestVersionNonCheckoutServicesMetadataOnly(Map<GraphPropertyEnum, Object> hasProps,
                                                                                                          Map<GraphPropertyEnum, Object> hasNotProps,
                                                                                                          String modelName) {
        List<Service> services = new ArrayList<>();
        List<LifecycleStateEnum> states = new ArrayList<>();
        // include props
        hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        hasProps.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        if (modelName != null) {
            hasProps.put(GraphPropertyEnum.MODEL, modelName);
        }
        // exclude props
        states.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        hasNotProps.put(GraphPropertyEnum.STATE, states);
        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        hasNotProps.put(GraphPropertyEnum.IS_ARCHIVED, true);
        return fetchServicesByCriteria(services, hasProps, hasNotProps, modelName);
    }

    private Either<List<Component>, StorageOperationStatus> getLatestVersionNotAbstractToscaElementsMetadataOnly(final boolean isAbstract,
                                                                                                                 final ComponentTypeEnum componentTypeEnum,
                                                                                                                 final String internalComponentType,
                                                                                                                 final VertexTypeEnum vertexType,
                                                                                                                 final String modelName,
                                                                                                                 final boolean includeNormativeExtensionModels) {
        List<Service> services = null;
        Map<GraphPropertyEnum, Object> hasProps = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProps = new EnumMap<>(GraphPropertyEnum.class);
        fillPropsMap(hasProps, hasNotProps, internalComponentType, componentTypeEnum, isAbstract, vertexType, modelName);
        Either<List<GraphVertex>, JanusGraphOperationStatus> getRes = janusGraphDao
            .getByCriteria(vertexType, hasProps, hasNotProps, JsonParseFlagEnum.ParseMetadata, modelName, includeNormativeExtensionModels);
        if (getRes.isRight() && !JanusGraphOperationStatus.NOT_FOUND.equals(getRes.right().value())) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getRes.right().value()));
        }
        // region -> Fetch non checked-out services
        if (internalComponentType != null && internalComponentType.toLowerCase().trim().equals(SERVICE) && VertexTypeEnum.NODE_TYPE == vertexType) {
            Either<List<Service>, StorageOperationStatus> result = getLatestVersionNonCheckoutServicesMetadataOnly(
                new EnumMap<>(GraphPropertyEnum.class), new EnumMap<>(GraphPropertyEnum.class), modelName);
            if (result.isRight()) {
                log.debug("Failed to fetch services for");
                return Either.right(result.right().value());
            }
            services = result.left().value();
            if (log.isTraceEnabled() && isEmpty(services)) {
                log.trace("No relevant services available");
            }
        }
        // endregion
        List<Component> nonAbstractLatestComponents = new ArrayList<>();
        ComponentParametersView params = new ComponentParametersView(true);
        params.setIgnoreAllVersions(false);
        if (getRes.isLeft()) {
            for (GraphVertex vertexComponent : getRes.left().value()) {
                Either<ToscaElement, StorageOperationStatus> componentRes = topologyTemplateOperation
                    .getLightComponent(vertexComponent, componentTypeEnum, params);
                if (componentRes.isRight()) {
                    log.debug("Failed to fetch light element for {} error {}", vertexComponent.getUniqueId(), componentRes.right().value());
                    return Either.right(componentRes.right().value());
                } else {
                    Component component = ModelConverter.convertFromToscaElement(componentRes.left().value());
                    nonAbstractLatestComponents.add(component);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(services)) {
            nonAbstractLatestComponents.addAll(services);
        }
        return Either.left(nonAbstractLatestComponents);
    }

    public Either<ComponentMetadataData, StorageOperationStatus> getLatestComponentMetadataByUuid(String componentUuid, JsonParseFlagEnum parseFlag,
                                                                                                  Boolean isHighest) {
        Either<ComponentMetadataData, StorageOperationStatus> result;
        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        hasProperties.put(GraphPropertyEnum.UUID, componentUuid);
        if (isHighest != null) {
            hasProperties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, isHighest);
        }
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_ARCHIVED, true); //US382674, US382683
        Either<List<GraphVertex>, JanusGraphOperationStatus> getRes = janusGraphDao
            .getByCriteria(null, hasProperties, propertiesNotToMatch, parseFlag);
        if (getRes.isRight()) {
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getRes.right().value()));
        } else {
            List<ComponentMetadataData> latestVersionList = getRes.left().value().stream().map(ModelConverter::convertToComponentMetadata)
                .collect(Collectors.toList());
            ComponentMetadataData latestVersion = latestVersionList.size() == 1 ? latestVersionList.get(0) : latestVersionList.stream().max(
                (c1, c2) -> Double.compare(Double.parseDouble(c1.getMetadataDataDefinition().getVersion()),
                    Double.parseDouble(c2.getMetadataDataDefinition().getVersion()))).get();
            result = Either.left(latestVersion);
        }
        return result;
    }

    public Either<ComponentMetadataData, StorageOperationStatus> getComponentMetadata(String componentId) {
        Either<ComponentMetadataData, StorageOperationStatus> result;
        Either<GraphVertex, JanusGraphOperationStatus> getRes = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
        if (getRes.isRight()) {
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getRes.right().value()));
        } else {
            ComponentMetadataData componentMetadata = ModelConverter.convertToComponentMetadata(getRes.left().value());
            result = Either.left(componentMetadata);
        }
        return result;
    }

    public Either<List<Component>, StorageOperationStatus> getLatestVersionNotAbstractComponents(boolean isAbstract,
                                                                                                 ComponentTypeEnum componentTypeEnum,
                                                                                                 String internalComponentType,
                                                                                                 List<String> componentUids) {
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
                Either<ToscaElement, StorageOperationStatus> getToscaElementRes = nodeTemplateOperation.getToscaElementOperation(componentTypeEnum)
                    .getLightComponent(componentUid, componentTypeEnum, componentParametersView);
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

    private Either<List<String>, StorageOperationStatus> getComponentUids(boolean isAbstract, ComponentTypeEnum componentTypeEnum,
                                                                          String internalComponentType) {
        Either<List<Component>, StorageOperationStatus> getToscaElementsRes = getLatestVersionNotAbstractMetadataOnly(isAbstract, componentTypeEnum,
            internalComponentType, null, false);
        if (getToscaElementsRes.isRight()) {
            return Either.right(getToscaElementsRes.right().value());
        }
        List<Component> collection = getToscaElementsRes.left().value();
        List<String> componentUids;
        if (collection == null) {
            componentUids = new ArrayList<>();
        } else {
            componentUids = collection.stream().map(Component::getUniqueId).collect(Collectors.toList());
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

    public Either<Boolean, StorageOperationStatus> validateComponentNameExists(String name, ResourceTypeEnum resourceType,
                                                                               ComponentTypeEnum componentType) {
        Either<Boolean, StorageOperationStatus> result = validateComponentNameUniqueness(name, resourceType, componentType);
        if (result.isLeft()) {
            result = Either.left(!result.left().value());
        }
        return result;
    }

    public Either<Boolean, StorageOperationStatus> validateComponentNameUniqueness(String name, ResourceTypeEnum resourceType,
                                                                                   ComponentTypeEnum componentType) {
        String normalizedName = ValidationUtils.normaliseComponentName(name);
        Either<List<GraphVertex>, JanusGraphOperationStatus> vertexEither = janusGraphDao
            .getByCriteria(getVertexTypeEnum(resourceType), propertiesToMatch(normalizedName, componentType), JsonParseFlagEnum.NoParse);
        if (vertexEither.isRight() && vertexEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            log.debug("failed to get vertex from graph with property normalizedName: {}", normalizedName);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(vertexEither.right().value()));
        }
        return Either.left(CollectionUtils.isEmpty(vertexEither.isLeft() ? vertexEither.left().value() : null));
    }

    public Either<Boolean, StorageOperationStatus> validateComponentNameAndModelExists(final String resourceName, final String model,
                                                                                       final ResourceTypeEnum resourceType,
                                                                                       final ComponentTypeEnum componentType) {
        Either<Boolean, StorageOperationStatus> result = validateComponentNameAndModelUniqueness(resourceName, model, resourceType, componentType);
        if (result.isLeft()) {
            result = Either.left(!result.left().value());
        }
        return result;
    }

    private Either<Boolean, StorageOperationStatus> validateComponentNameAndModelUniqueness(final String resourceName, final String modelName,
                                                                                            final ResourceTypeEnum resourceType,
                                                                                            final ComponentTypeEnum componentType) {
        final String normalizedName = ValidationUtils.normaliseComponentName(resourceName);
        final Either<List<GraphVertex>, JanusGraphOperationStatus> vertexEither = janusGraphDao
            .getByCriteria(getVertexTypeEnum(resourceType), propertiesToMatch(normalizedName, componentType), null, null, JsonParseFlagEnum.NoParse,
                modelName);
        if (vertexEither.isRight() && vertexEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            log.debug("failed to get vertex from graph with property normalizedName: {} and model: {}", normalizedName, modelName);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(vertexEither.right().value()));
        }
        return Either.left(CollectionUtils.isEmpty(vertexEither.isLeft() ? vertexEither.left().value().stream()
            .collect(Collectors.toList()) : null));
    }

    private VertexTypeEnum getVertexTypeEnum(final ResourceTypeEnum resourceType) {
        return ModelConverter.isAtomicComponent(resourceType) ? VertexTypeEnum.NODE_TYPE : VertexTypeEnum.TOPOLOGY_TEMPLATE;
    }

    private Map<GraphPropertyEnum, Object> propertiesToMatch(final String normalizedName, final ComponentTypeEnum componentType) {
        final Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.NORMALIZED_NAME, normalizedName);
        properties.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        return properties;
    }

    private void fillNodeTypePropsMap(final Map<GraphPropertyEnum, Object> hasProps, final Map<GraphPropertyEnum, Object> hasNotProps,
                                      final String internalComponentType, String modelName) {
        final Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
        final List<String> allowedTypes;
        if (ComponentTypeEnum.SERVICE.getValue().equalsIgnoreCase(internalComponentType)) {
            allowedTypes = containerInstanceTypesData.getServiceAllowedList(modelName);
        } else {
            final ResourceTypeEnum resourceType = ResourceTypeEnum.getTypeIgnoreCase(internalComponentType);
            allowedTypes = containerInstanceTypesData.getComponentAllowedList(ComponentTypeEnum.RESOURCE, resourceType);
        }
        final List<String> allResourceTypes = configuration.getResourceTypes();
        if (allowedTypes == null) {
            hasNotProps.put(GraphPropertyEnum.RESOURCE_TYPE, allResourceTypes);
            return;
        }
        if (ResourceTypeEnum.VL.getValue().equalsIgnoreCase(internalComponentType)) {
            hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, allowedTypes);
        } else {
            final List<String> notAllowedTypes = allResourceTypes.stream().filter(s -> !allowedTypes.contains(s)).collect(Collectors.toList());
            hasNotProps.put(GraphPropertyEnum.RESOURCE_TYPE, notAllowedTypes);
        }
    }

    private void fillTopologyTemplatePropsMap(Map<GraphPropertyEnum, Object> hasProps, Map<GraphPropertyEnum, Object> hasNotProps,
                                              ComponentTypeEnum componentTypeEnum) {
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

    private void fillPropsMap(Map<GraphPropertyEnum, Object> hasProps, Map<GraphPropertyEnum, Object> hasNotProps, String internalComponentType,
                              ComponentTypeEnum componentTypeEnum, boolean isAbstract, VertexTypeEnum internalVertexType, String modelName) {
        hasNotProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        hasNotProps.put(GraphPropertyEnum.IS_ARCHIVED, true);
        hasProps.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        if (VertexTypeEnum.NODE_TYPE == internalVertexType) {
            hasProps.put(GraphPropertyEnum.IS_ABSTRACT, isAbstract);
            if (internalComponentType != null) {
                fillNodeTypePropsMap(hasProps, hasNotProps, internalComponentType, modelName);
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
        if (ComponentTypeEnum.SERVICE == componentTypeEnum || SERVICE.equalsIgnoreCase(internalComponentType) || VF.equalsIgnoreCase(
            internalComponentType)) {
            internalVertexTypes.add(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        }
        return internalVertexTypes;
    }

    public Either<List<Component>, StorageOperationStatus> getLatestVersionNotAbstractMetadataOnly(boolean isAbstract,
                                                                                                   final ComponentTypeEnum componentTypeEnum,
                                                                                                   final String internalComponentType,
                                                                                                   final String modelName,
                                                                                                   final boolean includeNormativeExtensionModels) {
        List<VertexTypeEnum> internalVertexTypes = getInternalVertexTypes(componentTypeEnum, internalComponentType);
        List<Component> result = new ArrayList<>();
        for (VertexTypeEnum vertexType : internalVertexTypes) {
            Either<List<Component>, StorageOperationStatus> listByVertexType = getLatestVersionNotAbstractToscaElementsMetadataOnly(isAbstract,
                componentTypeEnum, internalComponentType, vertexType, modelName, includeNormativeExtensionModels);
            if (listByVertexType.isRight()) {
                return listByVertexType;
            }
            result.addAll(listByVertexType.left().value());
        }
        return Either.left(result);
    }

    private Either<List<Component>, StorageOperationStatus> getLatestComponentListByUuid(String componentUuid,
                                                                                         Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {
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
        Either<List<GraphVertex>, JanusGraphOperationStatus> vertexEither = janusGraphDao
            .getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        if (vertexEither.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(vertexEither.right().value()));
        }
        List<GraphVertex> vertexList = vertexEither.isLeft() ? vertexEither.left().value() : null;
        if (vertexList == null || vertexList.isEmpty() || vertexList.size() > 1) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        return getToscaElementByOperation(vertexList.get(0));
    }

    public Either<List<Component>, StorageOperationStatus> getComponentListByUuid(String componentUuid,
                                                                                  Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        if (additionalPropertiesToMatch != null) {
            propertiesToMatch.putAll(additionalPropertiesToMatch);
        }
        propertiesToMatch.put(GraphPropertyEnum.UUID, componentUuid);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_ARCHIVED, true); //US382674, US382683
        Either<List<GraphVertex>, JanusGraphOperationStatus> vertexEither = janusGraphDao
            .getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        if (vertexEither.isRight()) {
            log.debug("Couldn't fetch metadata for component with uuid {}, error: {}", componentUuid, vertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(vertexEither.right().value()));
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
                if (Boolean.TRUE.equals(component.isHighestVersion())) {
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

    public Either<Component, StorageOperationStatus> getLatestComponentByUuid(String componentUuid,
                                                                              Map<GraphPropertyEnum, Object> propertiesToMatch) {
        Either<List<Component>, StorageOperationStatus> latestVersionListEither = getLatestComponentListByUuid(componentUuid, propertiesToMatch);
        if (latestVersionListEither.isRight()) {
            return Either.right(latestVersionListEither.right().value());
        }
        List<Component> latestVersionList = latestVersionListEither.left().value();
        if (latestVersionList.isEmpty()) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        Component component = latestVersionList.size() == 1 ? latestVersionList.get(0)
            : latestVersionList.stream().max((c1, c2) -> Double.compare(Double.parseDouble(c1.getVersion()), Double.parseDouble(c2.getVersion())))
                .get();
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
        Either<List<GraphVertex>, JanusGraphOperationStatus> getResourcesRes = janusGraphDao
            .getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        if (getResourcesRes.isRight()) {
            log.debug("Failed to fetch all certified resources. Status is {}", getResourcesRes.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getResourcesRes.right().value()));
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

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByNameAndVersion(String name, String version,
                                                                                             JsonParseFlagEnum parseFlag, String model) {
        Either<T, StorageOperationStatus> result;
        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProperties = new EnumMap<>(GraphPropertyEnum.class);
        hasProperties.put(GraphPropertyEnum.NAME, name);
        hasProperties.put(GraphPropertyEnum.VERSION, version);
        hasProperties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        hasNotProperties.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> getResourceRes = janusGraphDao
            .getByCriteria(null, hasProperties, hasNotProperties, parseFlag, model);
        if (getResourceRes.isRight()) {
            JanusGraphOperationStatus status = getResourceRes.right().value();
            log.debug("failed to find resource with name {}, version {}. Status is {} ", name, version, status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            return result;
        }
        return getToscaElementByOperation(getResourceRes.left().value().get(0));
    }

    public Either<Resource, StorageOperationStatus> getLatestComponentByCsarOrName(ComponentTypeEnum componentType, String csarUUID,
                                                                                   String systemName) {
        return getLatestComponentByCsarOrName(componentType, csarUUID, systemName, JsonParseFlagEnum.ParseAll);
    }

    public Either<Resource, StorageOperationStatus> getLatestComponentByCsarOrName(ComponentTypeEnum componentType, String csarUUID,
                                                                                   String systemName, JsonParseFlagEnum parseFlag) {
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
        Either<List<GraphVertex>, JanusGraphOperationStatus> byCsar = janusGraphDao
            .getByCriteria(null, props, propsHasNot, JsonParseFlagEnum.ParseMetadata);
        if (byCsar.isRight()) {
            if (JanusGraphOperationStatus.NOT_FOUND == byCsar.right().value()) {
                // Fix Defect DE256036
                if (StringUtils.isEmpty(systemName)) {
                    return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_FOUND));
                }
                props.clear();
                props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
                props.put(GraphPropertyEnum.SYSTEM_NAME, systemName);
                Either<List<GraphVertex>, JanusGraphOperationStatus> bySystemname = janusGraphDao
                    .getByCriteria(null, props, JsonParseFlagEnum.ParseMetadata);
                if (bySystemname.isRight()) {
                    log.debug("getLatestResourceByCsarOrName - Failed to find by system name {}  error {} ", systemName,
                        bySystemname.right().value());
                    return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(bySystemname.right().value()));
                }
                if (bySystemname.left().value().size() > 2) {
                    log.debug(
                        "getLatestResourceByCsarOrName - getByCriteria(by system name) must return only 2 latest version, but was returned - {}",
                        bySystemname.left().value().size());
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
                final Object csarUuid = resourceMetadataData.getJsonMetadataField(JsonPresentationFields.CSAR_UUID);
                if (csarUuid != null && !csarUuid.equals(csarUUID)) {
                    log.debug("getLatestResourceByCsarOrName - same system name {} but different csarUUID. exist {} and new {} ", systemName,
                        csarUuid, csarUUID);
                    // correct error will be returned from create flow. with all

                    // correct audit records!!!!!
                    return Either.right(StorageOperationStatus.NOT_FOUND);
                }
                return getToscaElement(resourceMetadataData.getUniqueId());
            }
        } else {
            resourceMetadataDataList = byCsar.left().value();
            if (resourceMetadataDataList.size() > 2) {
                log.debug("getLatestResourceByCsarOrName - getByCriteria(by csar) must return only 2 latest version, but was returned - {}",
                    byCsar.left().value().size());
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

    public Either<Boolean, StorageOperationStatus> validateToscaResourceNameExtends(String templateNameCurrent, String templateNameExtends,
                                                                                    String model) {
        String currentTemplateNameChecked = templateNameExtends;
        while (currentTemplateNameChecked != null && !currentTemplateNameChecked.equalsIgnoreCase(templateNameCurrent)) {
            Either<Resource, StorageOperationStatus> latestByToscaResourceName = getLatestByToscaResourceName(currentTemplateNameChecked, model);
            if (latestByToscaResourceName.isRight()) {
                return latestByToscaResourceName.right().value() == StorageOperationStatus.NOT_FOUND ? Either.left(false)
                    : Either.right(latestByToscaResourceName.right().value());
            }
            Resource value = latestByToscaResourceName.left().value();
            if (value.getDerivedFrom() != null) {
                currentTemplateNameChecked = value.getDerivedFrom().get(0);
            } else {
                currentTemplateNameChecked = null;
            }
        }
        return (currentTemplateNameChecked != null && currentTemplateNameChecked.equalsIgnoreCase(templateNameCurrent)) ? Either.left(true)
            : Either.left(false);
    }

    public Either<List<Component>, StorageOperationStatus> fetchMetaDataByResourceType(String resourceType, ComponentParametersView filterBy) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.RESOURCE_TYPE, resourceType);
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        Map<GraphPropertyEnum, Object> propsHasNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propsHasNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> resourcesByTypeEither = janusGraphDao
            .getByCriteria(null, props, propsHasNotToMatch, JsonParseFlagEnum.ParseMetadata);
        if (resourcesByTypeEither.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(resourcesByTypeEither.right().value()));
        }
        List<GraphVertex> vertexList = resourcesByTypeEither.left().value();
        List<Component> components = new ArrayList<>();
        for (GraphVertex vertex : vertexList) {
            components.add(getToscaElementByOperation(vertex, filterBy).left().value());
        }
        return Either.left(components);
    }

    public void commit() {
        janusGraphDao.commit();
    }

    public Either<Service, StorageOperationStatus> updateDistributionStatus(Service service, User user, DistributionStatusEnum distributionStatus) {
        Either<GraphVertex, StorageOperationStatus> updateDistributionStatus = topologyTemplateOperation
            .updateDistributionStatus(service.getUniqueId(), user, distributionStatus);
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
        Either<GraphVertex, JanusGraphOperationStatus> updateRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getRes = janusGraphDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.ParseMetadata);
        if (getRes.isRight()) {
            JanusGraphOperationStatus status = getRes.right().value();
            log.error("Failed to fetch component {}. status is {}", component.getUniqueId(), status);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        if (result == null) {
            serviceVertex = getRes.left().value();
            long lastUpdateDate = System.currentTimeMillis();
            serviceVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, lastUpdateDate);
            component.setLastUpdateDate(lastUpdateDate);
            updateRes = janusGraphDao.updateVertex(serviceVertex);
            if (updateRes.isRight()) {
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updateRes.right().value()));
            }
        }
        if (result == null) {
            result = Either.left(ModelConverter.convertToComponentMetadata(updateRes.left().value()));
        }
        return result;
    }

    public HealingJanusGraphDao getJanusGraphDao() {
        return janusGraphDao;
    }

    public Either<List<Service>, StorageOperationStatus> getCertifiedServicesWithDistStatus(Set<DistributionStatusEnum> distStatus) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        return getServicesWithDistStatus(distStatus, propertiesToMatch);
    }

    public Either<List<Service>, StorageOperationStatus> getServicesWithDistStatus(Set<DistributionStatusEnum> distStatus,
                                                                                   Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {
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
                Either<List<Service>, StorageOperationStatus> fetchServicesByCriteria = fetchServicesByCriteria(servicesAll, propertiesToMatch,
                    propertiesNotToMatch, null);
                if (fetchServicesByCriteria.isRight()) {
                    return fetchServicesByCriteria;
                } else {
                    servicesAll = fetchServicesByCriteria.left().value();
                }
            }
            return Either.left(servicesAll);
        } else {
            return fetchServicesByCriteria(servicesAll, propertiesToMatch, propertiesNotToMatch, null);
        }
    }

    private Either<List<Service>, StorageOperationStatus> fetchServicesByCriteria(List<Service> servicesAll,
                                                                                  Map<GraphPropertyEnum, Object> propertiesToMatch,
                                                                                  Map<GraphPropertyEnum, Object> propertiesNotToMatch,
                                                                                  String modelName) {
        Either<List<GraphVertex>, JanusGraphOperationStatus> getRes = janusGraphDao
            .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll, modelName);
        if (getRes.isRight()) {
            if (getRes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG,
                    "Failed to fetch certified services by match properties {} not match properties {} . Status is {}. ", propertiesToMatch,
                    propertiesNotToMatch, getRes.right().value());
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getRes.right().value()));
            }
        } else {
            for (final GraphVertex vertex : getRes.left().value()) {
                Either<ToscaElement, StorageOperationStatus> getServiceRes = topologyTemplateOperation
                    .getLightComponent(vertex, ComponentTypeEnum.SERVICE, new ComponentParametersView(true));
                if (getServiceRes.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch certified service {}. Status is {}. ",
                        vertex.getJsonMetadataField(JsonPresentationFields.NAME), getServiceRes.right().value());
                    return Either.right(getServiceRes.right().value());
                } else {
                    servicesAll.add(ModelConverter.convertFromToscaElement(getServiceRes.left().value()));
                }
            }
        }
        return Either.left(servicesAll);
    }

    public void rollback() {
        janusGraphDao.rollback();
    }

    public StorageOperationStatus addDeploymentArtifactsToInstance(String componentId, ComponentInstance componentInstance,
                                                                   Map<String, ArtifactDefinition> finalDeploymentArtifacts) {
        Map<String, ArtifactDataDefinition> instDeplArtifacts = finalDeploymentArtifacts.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
        return nodeTemplateOperation.addDeploymentArtifactsToInstance(componentId, componentInstance.getUniqueId(), instDeplArtifacts);
    }

    public StorageOperationStatus addInformationalArtifactsToInstance(String componentId, ComponentInstance componentInstance,
                                                                      Map<String, ArtifactDefinition> artifacts) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        if (MapUtils.isNotEmpty(artifacts)) {
            Map<String, ArtifactDataDefinition> instDeplArtifacts = artifacts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
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

    public Either<PropertyDefinition, StorageOperationStatus> addPropertyToComponent(String propertyName, PropertyDefinition newPropertyDefinition,
                                                                                     Component component) {
        newPropertyDefinition.setName(propertyName);
        StorageOperationStatus status = getToscaElementOperation(component)
            .addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, newPropertyDefinition,
                JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add the property {} to the component {}. Status is {}. ", propertyName,
                component.getName(), status);
            return Either.right(status);
        }
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreProperties(false);
        filter.setIgnoreInputs(false);
        Either<Component, StorageOperationStatus> getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
        if (getUpdatedComponentRes.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get updated component {}. Status is {}. ", component.getUniqueId(),
                getUpdatedComponentRes.right().value());
            return Either.right(status);
        }
        PropertyDefinition newProperty = null;
        List<PropertyDefinition> properties = (getUpdatedComponentRes.left().value()).getProperties();
        if (CollectionUtils.isNotEmpty(properties)) {
            Optional<PropertyDefinition> propertyOptional = properties.stream().filter(propertyEntry -> propertyEntry.getName().equals(propertyName))
                .findAny();
            if (propertyOptional.isPresent()) {
                newProperty = propertyOptional.get();
            }
        }
        if (newProperty == null) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find recently added property {} on the component {}. Status is {}. ",
                propertyName, component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        return Either.left(newProperty);
    }

    public Either<InputDefinition, StorageOperationStatus> addInputToComponent(String inputName, InputDefinition newInputDefinition,
                                                                               Component component) {
        newInputDefinition.setName(inputName);
        StorageOperationStatus status = getToscaElementOperation(component)
            .addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, newInputDefinition,
                JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add the input {} to the component {}. Status is {}. ", inputName,
                component.getName(), status);
            return Either.right(status);
        }
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreProperties(false);
        filter.setIgnoreInputs(false);
        Either<Component, StorageOperationStatus> getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
        if (getUpdatedComponentRes.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get updated component {}. Status is {}. ", component.getUniqueId(),
                getUpdatedComponentRes.right().value());
            return Either.right(status);
        }
        InputDefinition newInput = null;
        List<InputDefinition> inputs = (getUpdatedComponentRes.left().value()).getInputs();
        if (CollectionUtils.isNotEmpty(inputs)) {
            Optional<InputDefinition> inputOptional = inputs.stream().filter(inputEntry -> inputEntry.getName().equals(inputName)).findAny();
            if (inputOptional.isPresent()) {
                newInput = inputOptional.get();
            }
        }
        if (newInput == null) {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find recently added input {} " + "on the component {}. Status is {}. ", inputs,
                    component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        return Either.left(newInput);
    }

    public StorageOperationStatus deletePropertyOfComponent(Component component, String propertyName) {
        return getToscaElementOperation(component)
            .deleteToscaDataElement(component.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, propertyName,
                JsonPresentationFields.NAME);
    }

    public StorageOperationStatus deleteAttributeOfResource(Component component, String attributeName) {
        return getToscaElementOperation(component)
            .deleteToscaDataElement(component.getUniqueId(), EdgeLabelEnum.ATTRIBUTES, VertexTypeEnum.ATTRIBUTES, attributeName,
                JsonPresentationFields.NAME);
    }

    public StorageOperationStatus deleteInputOfResource(Component resource, String inputName) {
        return getToscaElementOperation(resource)
            .deleteToscaDataElement(resource.getUniqueId(), EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, inputName, JsonPresentationFields.NAME);
    }

    public StorageOperationStatus deleteOutputOfResource(final Component resource, final String outputName) {
        return getToscaElementOperation(resource)
            .deleteToscaDataElement(resource.getUniqueId(), EdgeLabelEnum.OUTPUTS, VertexTypeEnum.OUTPUTS, outputName, JsonPresentationFields.NAME);
    }

    /**
     * Deletes a data type from a component.
     *
     * @param component    the container which has the data type
     * @param dataTypeName the data type name to be deleted
     * @return Operation result.
     */
    public StorageOperationStatus deleteDataTypeOfComponent(Component component, String dataTypeName) {
        return getToscaElementOperation(component)
            .deleteToscaDataElement(component.getUniqueId(), EdgeLabelEnum.DATA_TYPES, VertexTypeEnum.DATA_TYPES, dataTypeName,
                JsonPresentationFields.NAME);
    }

    public Either<PropertyDefinition, StorageOperationStatus> updatePropertyOfComponent(Component component,
                                                                                        PropertyDefinition newPropertyDefinition) {
        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<PropertyDefinition, StorageOperationStatus> result = null;
        StorageOperationStatus status = getToscaElementOperation(component)
            .updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, newPropertyDefinition,
                JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS, newPropertyDefinition.getName(),
                    component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreProperties(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(),
                    getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<PropertyDefinition> newProperty = (getUpdatedComponentRes.left().value()).getProperties().stream()
                .filter(p -> p.getName().equals(newPropertyDefinition.getName())).findAny();
            if (newProperty.isPresent()) {
                result = Either.left(newProperty.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS,
                    newPropertyDefinition.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        return result;
    }

    public Either<AttributeDefinition, StorageOperationStatus> updateAttributeOfComponent(Component component,
                                                                                          AttributeDefinition newPropertyDefinition) {
        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<AttributeDefinition, StorageOperationStatus> result = null;
        StorageOperationStatus status = getToscaElementOperation(component)
            .updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.ATTRIBUTES, VertexTypeEnum.ATTRIBUTES, newPropertyDefinition,
                JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS, newPropertyDefinition.getName(),
                    component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreProperties(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(),
                    getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<AttributeDefinition> newProperty = (getUpdatedComponentRes.left().value()).getAttributes().stream()
                .filter(p -> p.getName().equals(newPropertyDefinition.getName())).findAny();
            if (newProperty.isPresent()) {
                result = Either.left(newProperty.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS,
                    newPropertyDefinition.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        return result;
    }

    public Either<AttributeDefinition, StorageOperationStatus> addAttributeOfResource(Component component, AttributeDefinition newAttributeDef) {
        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<AttributeDefinition, StorageOperationStatus> result = null;
        if (newAttributeDef.getUniqueId() == null || newAttributeDef.getUniqueId().isEmpty()) {
            String attUniqueId = UniqueIdBuilder.buildAttributeUid(component.getUniqueId(), newAttributeDef.getName());
            newAttributeDef.setUniqueId(attUniqueId);
            newAttributeDef.setOwnerId(component.getUniqueId());
        }
        StorageOperationStatus status = getToscaElementOperation(component)
            .addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.ATTRIBUTES, VertexTypeEnum.ATTRIBUTES, newAttributeDef,
                JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS, newAttributeDef.getName(),
                component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreAttributes(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(),
                    getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<AttributeDefinition> newAttribute = ((Resource) getUpdatedComponentRes.left().value()).getAttributes().stream()
                .filter(p -> p.getName().equals(newAttributeDef.getName())).findAny();
            if (newAttribute.isPresent()) {
                result = Either.left(newAttribute.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS,
                    newAttributeDef.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        return result;
    }

    public Either<AttributeDefinition, StorageOperationStatus> updateAttributeOfResource(Component component, AttributeDefinition newAttributeDef) {
        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<AttributeDefinition, StorageOperationStatus> result = null;
        StorageOperationStatus status = getToscaElementOperation(component)
            .updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.ATTRIBUTES, VertexTypeEnum.ATTRIBUTES, newAttributeDef,
                JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_ADD_THE_PROPERTY_TO_THE_RESOURCE_STATUS_IS, newAttributeDef.getName(),
                component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreAttributes(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(),
                    getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<AttributeDefinition> newProperty = ((Resource) getUpdatedComponentRes.left().value()).getAttributes().stream()
                .filter(p -> p.getName().equals(newAttributeDef.getName())).findAny();
            if (newProperty.isPresent()) {
                result = Either.left(newProperty.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_FIND_RECENTLY_ADDED_PROPERTY_ON_THE_RESOURCE_STATUS_IS,
                    newAttributeDef.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        return result;
    }

    public Either<InputDefinition, StorageOperationStatus> updateInputOfComponent(Component component, InputDefinition newInputDefinition) {
        Either<Component, StorageOperationStatus> getUpdatedComponentRes = null;
        Either<InputDefinition, StorageOperationStatus> result = null;
        StorageOperationStatus status = getToscaElementOperation(component)
            .updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, newInputDefinition,
                JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the input {} to the component {}. Status is {}. ",
                newInputDefinition.getName(), component.getName(), status);
            result = Either.right(status);
        }
        if (result == null) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreInputs(false);
            getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
            if (getUpdatedComponentRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_UPDATED_RESOURCE_STATUS_IS, component.getUniqueId(),
                    getUpdatedComponentRes.right().value());
                result = Either.right(status);
            }
        }
        if (result == null) {
            Optional<InputDefinition> updatedInput = getUpdatedComponentRes.left().value().getInputs().stream()
                .filter(p -> p.getName().equals(newInputDefinition.getName())).findAny();
            if (updatedInput.isPresent()) {
                result = Either.left(updatedInput.get());
            } else {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find recently updated inputs {} on the resource {}. Status is {}. ",
                    newInputDefinition.getName(), component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
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
    public Either<StorageOperationStatus, StorageOperationStatus> cleanAndAddGroupInstancesToComponentInstance(Component containerComponent,
                                                                                                               ComponentInstance componentInstance,
                                                                                                               String componentInstanceId) {
        String uniqueId = componentInstance.getUniqueId();
        StorageOperationStatus status = nodeTemplateOperation
            .deleteToscaDataDeepElementsBlockOfToscaElement(containerComponent.getUniqueId(), EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS,
                uniqueId);
        if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete group instances for container {}. error {] ", componentInstanceId, status);
            return Either.right(status);
        }
        if (componentInstance.getGroupInstances() != null) {
            status = addGroupInstancesToComponentInstance(containerComponent, componentInstance, componentInstance.getGroupInstances());
            if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
                CommonUtility
                    .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add group instances for container {}. error {] ", componentInstanceId,
                        status);
                return Either.right(status);
            }
        }
        return Either.left(status);
    }

    public StorageOperationStatus addGroupInstancesToComponentInstance(Component containerComponent, ComponentInstance componentInstance,
                                                                       List<GroupDefinition> groups,
                                                                       Map<String, List<ArtifactDefinition>> groupInstancesArtifacts) {
        return nodeTemplateOperation.addGroupInstancesToComponentInstance(containerComponent, componentInstance, groups, groupInstancesArtifacts);
    }

    public Either<List<GroupDefinition>, StorageOperationStatus> updateGroupsOnComponent(Component component,
                                                                                         List<GroupDataDefinition> updatedGroups) {
        return groupsOperation.updateGroups(component, updatedGroups, PromoteVersionEnum.MINOR);
    }

    public Either<List<GroupInstance>, StorageOperationStatus> updateGroupInstancesOnComponent(Component component, String instanceId,
                                                                                               List<GroupInstance> updatedGroupInstances) {
        return groupsOperation.updateGroupInstances(component, instanceId, updatedGroupInstances);
    }

    public StorageOperationStatus addGroupInstancesToComponentInstance(Component containerComponent, ComponentInstance componentInstance,
                                                                       List<GroupInstance> groupInstances) {
        return nodeTemplateOperation.addGroupInstancesToComponentInstance(containerComponent, componentInstance, groupInstances);
    }

    public StorageOperationStatus addDeploymentArtifactsToComponentInstance(Component containerComponent, ComponentInstance componentInstance,
                                                                            Map<String, ArtifactDefinition> deploymentArtifacts) {
        return nodeTemplateOperation.addDeploymentArtifactsToComponentInstance(containerComponent, componentInstance, deploymentArtifacts);
    }

    public StorageOperationStatus updateComponentInstanceProperty(Component containerComponent, String componentInstanceId,
                                                                  ComponentInstanceProperty property) {
        return nodeTemplateOperation.updateComponentInstanceProperty(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceProperties(Component containerComponent, String componentInstanceId,
                                                                    List<ComponentInstanceProperty> properties) {
        return nodeTemplateOperation.updateComponentInstanceProperties(containerComponent, componentInstanceId, properties);
    }

    public StorageOperationStatus updateComponentInstanceAttributes(final Component containerComponent, final String componentInstanceId,
                                                                    final List<ComponentInstanceAttribute> attributes) {
        return nodeTemplateOperation.updateComponentInstanceAttributes(containerComponent, componentInstanceId, attributes);
    }

    public StorageOperationStatus addComponentInstanceProperty(Component containerComponent, String componentInstanceId,
                                                               ComponentInstanceProperty property) {
        return nodeTemplateOperation.addComponentInstanceProperty(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceAttribute(final Component containerComponent, final String componentInstanceId,
                                                                   final ComponentInstanceAttribute attribute) {
        return nodeTemplateOperation.updateComponentInstanceAttribute(containerComponent, componentInstanceId, attribute);
    }

    public StorageOperationStatus addComponentInstanceAttribute(Component containerComponent, String componentInstanceId,
                                                                ComponentInstanceAttribute attribute) {
        return nodeTemplateOperation.addComponentInstanceAttribute(containerComponent, componentInstanceId, attribute);
    }

    public StorageOperationStatus updateComponentInstanceInput(Component containerComponent, String componentInstanceId,
                                                               ComponentInstanceInput property) {
        return nodeTemplateOperation.updateComponentInstanceInput(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceOutput(Component containerComponent, String componentInstanceId,
                                                                ComponentInstanceOutput property) {
        return nodeTemplateOperation.updateComponentInstanceOutput(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus updateComponentInstanceInputs(Component containerComponent, String componentInstanceId,
                                                                List<ComponentInstanceInput> instanceInputs) {
        return nodeTemplateOperation.updateComponentInstanceInputs(containerComponent, componentInstanceId, instanceInputs);
    }

    public StorageOperationStatus updateComponentInstanceOutputs(Component containerComponent, String componentInstanceId,
                                                                 List<ComponentInstanceOutput> instanceInputs) {
        return nodeTemplateOperation.updateComponentInstanceOutputs(containerComponent, componentInstanceId, instanceInputs);
    }

    public StorageOperationStatus addComponentInstanceInput(Component containerComponent, String componentInstanceId,
                                                            ComponentInstanceInput property) {
        return nodeTemplateOperation.addComponentInstanceInput(containerComponent, componentInstanceId, property);
    }

    public StorageOperationStatus addComponentInstanceOutput(Component containerComponent, String componentInstanceId,
                                                             ComponentInstanceOutput property) {
        return nodeTemplateOperation.addComponentInstanceOutput(containerComponent, componentInstanceId, property);
    }

    public void setNodeTypeOperation(NodeTypeOperation nodeTypeOperation) {
        this.nodeTypeOperation = nodeTypeOperation;
    }

    public void setTopologyTemplateOperation(TopologyTemplateOperation topologyTemplateOperation) {
        this.topologyTemplateOperation = topologyTemplateOperation;
    }

    public StorageOperationStatus deleteComponentInstanceInputsFromTopologyTemplate(Component containerComponent,
                                                                                    List<InputDefinition> inputsToDelete) {
        return topologyTemplateOperation.deleteToscaDataElements(containerComponent.getUniqueId(), EdgeLabelEnum.INPUTS,
            inputsToDelete.stream().map(PropertyDataDefinition::getName).collect(Collectors.toList()));
    }

    public StorageOperationStatus deleteComponentInstanceOutputsFromTopologyTemplate(final Component containerComponent,
                                                                                     final List<OutputDefinition> outputsToDelete) {
        return topologyTemplateOperation.deleteToscaDataElements(containerComponent.getUniqueId(), EdgeLabelEnum.OUTPUTS,
            outputsToDelete.stream().map(AttributeDataDefinition::getName).collect(Collectors.toList()));
    }

    public StorageOperationStatus updateComponentInstanceCapabiltyProperty(Component containerComponent, String componentInstanceUniqueId,
                                                                           String capabilityPropertyKey, ComponentInstanceProperty property) {
        return nodeTemplateOperation
            .updateComponentInstanceCapabilityProperty(containerComponent, componentInstanceUniqueId, capabilityPropertyKey, property);
    }

    public StorageOperationStatus updateComponentInstanceCapabilityProperties(Component containerComponent, String componentInstanceUniqueId) {
        return convertComponentInstanceProperties(containerComponent, componentInstanceUniqueId).map(instanceCapProps -> topologyTemplateOperation
                .updateComponentInstanceCapabilityProperties(containerComponent, componentInstanceUniqueId, instanceCapProps))
            .orElse(StorageOperationStatus.NOT_FOUND);
    }

    public StorageOperationStatus updateComponentInstanceRequirement(String containerComponentId, String componentInstanceUniqueId,
                                                                     RequirementDataDefinition requirementDataDefinition) {
        return nodeTemplateOperation.updateComponentInstanceRequirement(containerComponentId, componentInstanceUniqueId, requirementDataDefinition);
    }

    public CapabilityDataDefinition updateComponentInstanceCapability(final String containerComponentId, final String componentInstanceUniqueId,
                                                                      final CapabilityDataDefinition capabilityDataDefinition) {

        return nodeTemplateOperation.updateComponentInstanceCapabilities(containerComponentId, componentInstanceUniqueId, capabilityDataDefinition);
    }

    public StorageOperationStatus updateComponentInstanceInterfaces(Component containerComponent, String componentInstanceUniqueId) {
        MapInterfaceDataDefinition mapInterfaceDataDefinition = convertComponentInstanceInterfaces(containerComponent, componentInstanceUniqueId);
        return topologyTemplateOperation.updateComponentInstanceInterfaces(containerComponent, componentInstanceUniqueId, mapInterfaceDataDefinition);
    }

    public StorageOperationStatus updateComponentInterfaces(final Component component, final String componentInterfaceUpdatedKey) {
        MapInterfaceDataDefinition mapInterfaceDataDefinition = convertComponentInterfaces(component.getInterfaces());
        return topologyTemplateOperation.updateComponentInterfaces(component.getUniqueId(), mapInterfaceDataDefinition, componentInterfaceUpdatedKey);
    }

    public Either<InterfaceDefinition, StorageOperationStatus> addInterfaceToComponent(final String interfaceName,
                                                                                       final InterfaceDefinition interfaceDefinition,
                                                                                       final Component component) {

        final boolean match = component.getInterfaces().keySet().stream().anyMatch(s -> s.equals(interfaceName));
        StorageOperationStatus status = StorageOperationStatus.OK;
        final ToscaElementOperation toscaElementOperation = getToscaElementOperation(component);
        if (match) {
            status = toscaElementOperation.updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.INTERFACE_ARTIFACTS,
                VertexTypeEnum.INTERFACE_ARTIFACTS, interfaceDefinition, JsonPresentationFields.TYPE);
        } else {
            status = toscaElementOperation.addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.INTERFACE_ARTIFACTS,
                VertexTypeEnum.INTERFACE_ARTIFACTS, interfaceDefinition, JsonPresentationFields.TYPE);
        }

        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add the interface {} to the component {}. Status is {}. ",
                interfaceName, component.getName(), status);
            return Either.right(status);
        }
        final ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreInterfaces(false);
        filter.setIgnoreInterfaceInstances(false);
        final Either<Component, StorageOperationStatus> getUpdatedComponentRes = getToscaElement(component.getUniqueId(), filter);
        if (getUpdatedComponentRes.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get updated component {}. Status is {}. ",
                component.getUniqueId(), getUpdatedComponentRes.right().value());
            return Either.right(getUpdatedComponentRes.right().value());
        }
        InterfaceDefinition newInterfaceDefinition = null;
        final Map<String, InterfaceDefinition> interfaces = (getUpdatedComponentRes.left().value()).getInterfaces();
        if (MapUtils.isNotEmpty(interfaces)) {
            final Optional<String> interfaceNameOptional = interfaces.keySet().stream().filter(key -> key.equals(interfaceName)).findAny();
            if (interfaceNameOptional.isPresent()) {
                newInterfaceDefinition = interfaces.get(interfaceNameOptional.get());
            }
        }
        if (newInterfaceDefinition == null) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find recently added interface {} on the component {}. Status is {}. ",
                interfaceName, component.getUniqueId(), StorageOperationStatus.NOT_FOUND);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        return Either.left(newInterfaceDefinition);
    }

    public StorageOperationStatus updateComponentCalculatedCapabilitiesProperties(Component containerComponent) {
        Map<String, MapCapabilityProperty> mapCapabiltyPropertyMap = convertComponentCapabilitiesProperties(containerComponent);
        return nodeTemplateOperation.overrideComponentCapabilitiesProperties(containerComponent, mapCapabiltyPropertyMap);
    }

    public StorageOperationStatus deleteAllCalculatedCapabilitiesRequirements(String topologyTemplateId) {
        StorageOperationStatus status = topologyTemplateOperation
            .removeToscaData(topologyTemplateId, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES);
        if (status == StorageOperationStatus.OK) {
            status = topologyTemplateOperation
                .removeToscaData(topologyTemplateId, EdgeLabelEnum.CALCULATED_REQUIREMENTS, VertexTypeEnum.CALCULATED_REQUIREMENTS);
        }
        if (status == StorageOperationStatus.OK) {
            status = topologyTemplateOperation
                .removeToscaData(topologyTemplateId, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES);
        }
        return status;
    }

    public Either<Component, StorageOperationStatus> shouldUpgradeToLatestDerived(Resource clonedResource) {
        String componentId = clonedResource.getUniqueId();
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.debug(COULDNT_FETCH_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        GraphVertex nodeTypeV = getVertexEither.left().value();
        ToscaElement toscaElementToUpdate = ModelConverter.convertToToscaElement(clonedResource);
        Either<ToscaElement, StorageOperationStatus> shouldUpdateDerivedVersion = nodeTypeOperation
            .shouldUpdateDerivedVersion(toscaElementToUpdate, nodeTypeV);
        if (shouldUpdateDerivedVersion.isRight() && StorageOperationStatus.OK != shouldUpdateDerivedVersion.right().value()) {
            log.debug("Failed to update derived version for node type {} derived {}, error: {}", componentId, clonedResource.getDerivedFrom().get(0),
                shouldUpdateDerivedVersion.right().value());
            return Either.right(shouldUpdateDerivedVersion.right().value());
        }
        if (shouldUpdateDerivedVersion.isLeft()) {
            return Either.left(ModelConverter.convertFromToscaElement(shouldUpdateDerivedVersion.left().value()));
        }
        return Either.left(clonedResource);
    }

    /**
     * Returns list of ComponentInstanceProperty belonging to component instance capability specified by name, type and ownerId
     */
    public Either<List<ComponentInstanceProperty>, StorageOperationStatus> getComponentInstanceCapabilityProperties(String componentId,
                                                                                                                    String instanceId,
                                                                                                                    String capabilityName,
                                                                                                                    String capabilityType,
                                                                                                                    String ownerId) {
        return topologyTemplateOperation.getComponentInstanceCapabilityProperties(componentId, instanceId, capabilityName, capabilityType, ownerId);
    }

    private MapInterfaceDataDefinition convertComponentInstanceInterfaces(Component currComponent, String componentInstanceId) {
        MapInterfaceDataDefinition mapInterfaceDataDefinition = new MapInterfaceDataDefinition();
        List<ComponentInstanceInterface> componentInterface = currComponent.getComponentInstancesInterfaces().get(componentInstanceId);
        if (CollectionUtils.isNotEmpty(componentInterface)) {
            componentInterface.stream().forEach(interfaceDef -> mapInterfaceDataDefinition.put(interfaceDef.getUniqueId(), interfaceDef));
        }
        return mapInterfaceDataDefinition;
    }

    private MapInterfaceDataDefinition convertComponentInterfaces(final Map<String, InterfaceDefinition> interfaces) {
        final MapInterfaceDataDefinition mapInterfaceDataDefinition = new MapInterfaceDataDefinition();
        if (MapUtils.isNotEmpty(interfaces)) {
            interfaces.values().stream().forEach(interfaceDef -> mapInterfaceDataDefinition.put(interfaceDef.getType(), interfaceDef));
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
            .map(ci -> ModelConverter.convertToMapOfMapCapabilityProperties(ci.getCapabilities(), instanceId, ci.getOriginType().isAtomicType()));
    }

    public Either<PolicyDefinition, StorageOperationStatus> associatePolicyToComponent(String componentId, PolicyDefinition policyDefinition,
                                                                                       int counter) {
        Either<PolicyDefinition, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither;
        getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
        if (getVertexEither.isRight()) {
            log.error(COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        } else {
            if (getVertexEither.left().value().getLabel() != VertexTypeEnum.TOPOLOGY_TEMPLATE) {
                log.error("Policy association to component of Tosca type {} is not allowed. ", getVertexEither.left().value().getLabel());
                result = Either.right(StorageOperationStatus.BAD_REQUEST);
            }
        }
        if (result == null) {
            StorageOperationStatus status = topologyTemplateOperation
                .addPolicyToToscaElement(getVertexEither.left().value(), policyDefinition, counter);
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
        return janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata)
            .either(containerVertex -> topologyTemplateOperation.addPoliciesToToscaElement(containerVertex, policies),
                DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    public Either<PolicyDefinition, StorageOperationStatus> updatePolicyOfComponent(String componentId, PolicyDefinition policyDefinition,
                                                                                    PromoteVersionEnum promoteVersionEnum) {
        Either<PolicyDefinition, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither;
        getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.error(COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value()));
        }
        if (result == null) {
            policyDefinition.setVersion(GroupUtils.updateVersion(promoteVersionEnum, policyDefinition.getVersion()));
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
        return janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse).right()
            .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus)
            .either(containerVertex -> topologyTemplateOperation.updatePoliciesOfToscaElement(containerVertex, policyDefinition), err -> err);
    }

    public StorageOperationStatus removePolicyFromComponent(String componentId, String policyId) {
        StorageOperationStatus status = null;
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getVertexEither.isRight()) {
            log.error(COULDNT_FETCH_A_COMPONENT_WITH_AND_UNIQUE_ID_ERROR, componentId, getVertexEither.right().value());
            status = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVertexEither.right().value());
        }
        if (status == null) {
            status = topologyTemplateOperation.removePolicyFromToscaElement(getVertexEither.left().value(), policyId);
        }
        return status;
    }

    public boolean canAddGroups(String componentId) {
        GraphVertex vertex = janusGraphDao.getVertexById(componentId).left().on(this::onJanusGraphError);
        return topologyTemplateOperation.hasEdgeOfType(vertex, EdgeLabelEnum.GROUPS);
    }

    GraphVertex onJanusGraphError(JanusGraphOperationStatus toe) {
        throw new StorageException(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toe));
    }

    public CatalogUpdateTimestamp updateCatalogTimes() {
        long now = System.currentTimeMillis();
        GraphVertex catalogRoot = janusGraphDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT).left().on(this::onJanusGraphError);
        Long currentTime = (Long) catalogRoot.getMetadataProperty(GraphPropertyEnum.CURRENT_CATALOG_UPDATE_TIME);
        catalogRoot.addMetadataProperty(GraphPropertyEnum.PREV_CATALOG_UPDATE_TIME, currentTime);
        catalogRoot.addMetadataProperty(GraphPropertyEnum.CURRENT_CATALOG_UPDATE_TIME, now);
        janusGraphDao.updateVertex(catalogRoot).left().on(this::onJanusGraphError);
        return new CatalogUpdateTimestamp(currentTime, now);
    }

    public CatalogUpdateTimestamp getCatalogTimes() {
        GraphVertex catalogRoot = janusGraphDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT).left().on(this::onJanusGraphError);
        Long currentTime = (Long) catalogRoot.getMetadataProperty(GraphPropertyEnum.CURRENT_CATALOG_UPDATE_TIME);
        Long prevTime = (Long) catalogRoot.getMetadataProperty(GraphPropertyEnum.PREV_CATALOG_UPDATE_TIME);
        return new CatalogUpdateTimestamp(prevTime == null ? 0 : prevTime.longValue(), currentTime == null ? 0 : currentTime.longValue());
    }

    public void updateNamesOfCalculatedCapabilitiesRequirements(String componentId) {
        topologyTemplateOperation.updateNamesOfCalculatedCapabilitiesRequirements(componentId, getTopologyTemplate(componentId));
    }

    public void revertNamesOfCalculatedCapabilitiesRequirements(String componentId) {
        topologyTemplateOperation.revertNamesOfCalculatedCapabilitiesRequirements(componentId, getTopologyTemplate(componentId));
    }

    private TopologyTemplate getTopologyTemplate(String componentId) {
        return (TopologyTemplate) topologyTemplateOperation.getToscaElement(componentId, getFilterComponentWithCapProperties()).left()
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
        final List<EdgeLabelEnum> forbiddenEdgeLabelEnums = Arrays
            .asList(EdgeLabelEnum.INSTANCE_OF, EdgeLabelEnum.PROXY_OF, EdgeLabelEnum.ALLOTTED_OF);
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(componentId);
        if (vertexById.isLeft()) {
            for (EdgeLabelEnum edgeLabelEnum : forbiddenEdgeLabelEnums) {
                Iterator<Edge> edgeItr = vertexById.left().value().getVertex().edges(Direction.IN, edgeLabelEnum.name());
                if (edgeItr != null && edgeItr.hasNext()) {
                    return Either.left(true);
                }
            }
        }
        return Either.left(false);
    }

    public Either<List<Component>, StorageOperationStatus> getComponentListByInvariantUuid(String componentInvariantUuid,
                                                                                           Map<GraphPropertyEnum, Object> additionalPropertiesToMatch) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        if (MapUtils.isNotEmpty(additionalPropertiesToMatch)) {
            propertiesToMatch.putAll(additionalPropertiesToMatch);
        }
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, componentInvariantUuid);
        Either<List<GraphVertex>, JanusGraphOperationStatus> vertexEither = janusGraphDao
            .getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata);
        if (vertexEither.isRight()) {
            log.debug("Couldn't fetch metadata for component with type {} and invariantUUId {}, error: {}", componentInvariantUuid,
                vertexEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(vertexEither.right().value()));
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
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(componentId);
        if (vertexById.isLeft()) {
            for (EdgeLabelEnum edgeLabelEnum : relationEdgeLabelEnums) {
                Either<GraphVertex, JanusGraphOperationStatus> parentVertexEither = janusGraphDao
                    .getParentVertex(vertexById.left().value(), edgeLabelEnum, JsonParseFlagEnum.ParseJson);
                if (parentVertexEither.isLeft()) {
                    Either<Component, StorageOperationStatus> componentEither = getToscaElement(parentVertexEither.left().value().getUniqueId());
                    if (componentEither.isLeft()) {
                        parentComponents.add(componentEither.left().value());
                    }
                }
            }
        }
        return Either.left(parentComponents);
    }

    public void updateCapReqPropertiesOwnerId(String componentId) {
        topologyTemplateOperation.updateCapReqPropertiesOwnerId(componentId, getTopologyTemplate(componentId));
    }

    public <T extends Component> Either<T, StorageOperationStatus> getLatestByServiceName(String serviceName) {
        return getLatestByName(GraphPropertyEnum.NAME, serviceName, null);
    }
}
