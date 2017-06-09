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

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.QueryType;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition.PropertyNames;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.ICapabilityOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IRequirementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.api.ToscaDefinitionPathCalculator;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ProductMetadataData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.TagData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.GroupingData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.be.workers.Job;
import org.openecomp.sdc.be.workers.Manager;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.util.StreamUtils;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public abstract class ComponentOperation {
	private static Logger log = LoggerFactory.getLogger(ComponentOperation.class.getName());

	@Autowired
	protected TitanGenericDao titanGenericDao;

	@Autowired
	protected IArtifactOperation artifactOperation;

	@Autowired
	protected IElementOperation elementOperation;

	@Autowired
	protected ICapabilityOperation capabilityOperation;

	@Autowired
	protected IRequirementOperation requirementOperation;

	@Autowired
	protected ComponentInstanceOperation componentInstanceOperation;

	@Autowired
	private PropertyOperation propertyOperation;

	@Autowired
	protected InputsOperation inputOperation;

	@Autowired
	protected IAdditionalInformationOperation additionalInformationOperation;

	@Autowired
	protected GroupOperation groupOperation;

	@Autowired
	protected InputsOperation inputsOperation;

	@Autowired
	protected ApplicationDataTypeCache applicationDataTypeCache;

	@Autowired
	private ComponentCache componentCache;

	@Autowired
	private ToscaDefinitionPathCalculator toscaDefinitionPathCalculator;

	private static Pattern uuidNewVersion = Pattern.compile("^\\d{1,}.1");

	protected Gson prettyJson = new GsonBuilder().setPrettyPrinting().create();

	protected Either<List<TagData>, StorageOperationStatus> createNewTagsList(List<String> tags) {

		List<TagData> existingTags = new ArrayList<TagData>();
		List<TagData> tagsToCreate = new ArrayList<TagData>();
		Either<List<TagData>, TitanOperationStatus> either = titanGenericDao.getAll(NodeTypeEnum.Tag, TagData.class);

		if ((either.isRight()) && (either.right().value() != TitanOperationStatus.NOT_FOUND)) {
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		} else if (either.isLeft()) {
			existingTags = either.left().value();
		}

		for (String tagName : tags) {
			TagData tag = new TagData(tagName);
			if ((existingTags == null) || (!existingTags.contains(tag))) {
				tagsToCreate.add(tag);
			}
		}
		return Either.left(tagsToCreate);

	}
 
	protected StorageOperationStatus createTagNodesOnGraph(List<TagData> tagsToCreate) {
		StorageOperationStatus result = StorageOperationStatus.OK;
		// In order to avoid duplicate tags
		tagsToCreate = ImmutableSet.copyOf(tagsToCreate).asList();
		if (tagsToCreate != null && false == tagsToCreate.isEmpty()) {
			for (TagData tagData : tagsToCreate) {
				log.debug("Before creating tag {}" , tagData);
				Either<TagData, TitanOperationStatus> createTagResult = titanGenericDao.createNode(tagData, TagData.class);
				if (createTagResult.isRight()) {
					TitanOperationStatus status = createTagResult.right().value();
					log.error("Cannot create {} in the graph. status is {}", tagData, status);
					result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);

				}
				log.debug("After creating tag {}", tagData);
			}
		}
		return result;
	}

	public Either<Component, StorageOperationStatus> getLatestComponentByUuid(NodeTypeEnum nodeType, String uuid) {
		Either<Component, StorageOperationStatus> getComponentResult = null;
		Either<ComponentMetadataData, StorageOperationStatus> latestComponentMetadataRes = getLatestComponentMetadataByUuid(nodeType, uuid, false);
		if (latestComponentMetadataRes.isRight()) {
			getComponentResult = Either.right(latestComponentMetadataRes.right().value());
		}
		if (getComponentResult == null) {
			ComponentMetadataData latestVersion = latestComponentMetadataRes.left().value();
			String id = latestVersion.getMetadataDataDefinition().getUniqueId();
			Either<Component, StorageOperationStatus> component = getComponent(id, false);
			if (component.isRight()) {
				log.debug("Couldn't fetch component with type {} and id {}, error: {}", nodeType, id, component.right().value());
				getComponentResult = Either.right(component.right().value());
			} else {
				getComponentResult = Either.left(component.left().value());
			}
		}
		return getComponentResult;
	}

	public Either<ComponentMetadataData, StorageOperationStatus> getLatestComponentMetadataByUuid(NodeTypeEnum nodeType, String uuid, boolean inTransaction) {

		Either<ComponentMetadataData, StorageOperationStatus> getComponentResult = null;
		List<ComponentMetadataData> latestVersionList = null;
		ComponentMetadataData latestVersion = null;

		Map<String, Object> propertiesToMatch = new HashMap<String, Object>();
		propertiesToMatch.put(GraphPropertiesDictionary.UUID.getProperty(), uuid);
		propertiesToMatch.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
		try{
			Either<List<ComponentMetadataData>, TitanOperationStatus> getComponentEither = titanGenericDao.getByCriteria(nodeType, propertiesToMatch, ComponentMetadataData.class);
			if (getComponentEither.isRight()) {
				log.debug("Couldn't fetch metadata for component with type {} and uuid {}, error: {}", nodeType, uuid, getComponentEither.right().value());
				getComponentResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentEither.right().value()));
	
			}
			if (getComponentResult == null) {
				latestVersionList = getComponentEither.left().value();
				if (latestVersionList.isEmpty()) {
					log.debug("Component with type {} and uuid {} was not found", nodeType, uuid);
					getComponentResult = Either.right(StorageOperationStatus.NOT_FOUND);
				}
			}
			if (getComponentResult == null) {
				latestVersion = latestVersionList.size() == 1 ? latestVersionList.get(0)
						: latestVersionList.stream().max((c1, c2) -> Double.compare(Double.parseDouble(c1.getMetadataDataDefinition().getVersion()), Double.parseDouble(c2.getMetadataDataDefinition().getVersion()))).get();
				getComponentResult = Either.left(latestVersion);
			}
		} catch (Exception e){
			log.debug("Failed to get latest component metadata with type {} by uuid {}. ", nodeType.getName(), uuid, e);
		}finally {
			if (!inTransaction) {
				titanGenericDao.commit();
			}
		}
		return getComponentResult;
	}

	public <T extends GraphNode> Either<T, StorageOperationStatus> getComponentByLabelAndId(String uniqueId, NodeTypeEnum nodeType, Class<T> clazz) {

		Map<String, Object> propertiesToMatch = new HashMap<String, Object>();
		propertiesToMatch.put(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId);
		Either<List<T>, TitanOperationStatus> getResponse = titanGenericDao.getByCriteria(nodeType, propertiesToMatch, clazz);
		if (getResponse.isRight()) {
			log.debug("Couldn't fetch component with type {} and unique id {}, error: {}", nodeType, uniqueId, getResponse.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getResponse.right().value()));

		}
		List<T> serviceDataList = getResponse.left().value();
		if (serviceDataList.isEmpty()) {
			log.debug("Component with type {} and unique id {} was not found", nodeType, uniqueId);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		T serviceData = serviceDataList.get(0);
		return Either.left(serviceData);
	}

	/**
	 * 
	 * @param component
	 * @param uniqueId
	 * @param nodeType
	 * @return
	 */
	protected TitanOperationStatus setComponentCreatorFromGraph(Component component, String uniqueId, NodeTypeEnum nodeType) {
		Either<ImmutablePair<UserData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.CREATOR, NodeTypeEnum.User, UserData.class);
		if (parentNode.isRight()) {
			return parentNode.right().value();
		}

		ImmutablePair<UserData, GraphEdge> value = parentNode.left().value();
		if (log.isDebugEnabled())
			log.debug("Found parent node {}", value);
		UserData userData = value.getKey();
		if (log.isDebugEnabled())
			log.debug("Build resource : set creator userId to {}", userData.getUserId());
		String fullName = buildFullName(userData);
		if (log.isDebugEnabled())
			log.debug("Build resource : set last modifier full name to {} ", fullName);
		component.setCreatorUserId(userData.getUserId());
		component.setCreatorFullName(fullName);

		return TitanOperationStatus.OK;
	}

	protected TitanOperationStatus setComponentLastModifierFromGraph(Component component, String uniqueId, NodeTypeEnum nodeType) {

		Either<ImmutablePair<UserData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.LAST_MODIFIER, NodeTypeEnum.User, UserData.class);
		if (parentNode.isRight()) {
			return parentNode.right().value();
		}

		ImmutablePair<UserData, GraphEdge> value = parentNode.left().value();
		if (log.isDebugEnabled())
			log.debug("Found parent node {}", value);
		UserData userData = value.getKey();

		if (log.isDebugEnabled())
			log.debug("Build resource : set last modifier userId to {}", userData.getUserId());
		String fullName = buildFullName(userData);
		if (log.isDebugEnabled())
			log.debug("Build resource : set last modifier full name to {}", fullName);
		component.setLastUpdaterUserId(userData.getUserId());
		component.setLastUpdaterFullName(fullName);

		return TitanOperationStatus.OK;
	}

	/**
	 * 
	 * @param userData
	 * @return
	 */
	protected String buildFullName(UserData userData) {

		String fullName = userData.getFirstName();
		if (fullName == null) {
			fullName = "";
		} else {
			fullName = fullName + " ";
		}
		String lastName = userData.getLastName();
		if (lastName != null) {
			fullName += lastName;
		}
		return fullName;
	}

	protected Either<UserData, TitanOperationStatus> findUser(String userId) {
		String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
		Either<UserData, TitanOperationStatus> findUser = titanGenericDao.getNode(key, userId, UserData.class);
		return findUser;
	}

	protected Either<TitanVertex, TitanOperationStatus> findUserVertex(String userId) {
		String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
		return titanGenericDao.getVertexByProperty(key, userId);
	}

	protected Either<GroupingData, TitanOperationStatus> findGrouping(NodeTypeEnum nodeType, String groupingId) {
		String key = UniqueIdBuilder.getKeyByNodeType(nodeType);
		Either<GroupingData, TitanOperationStatus> findGrouping = titanGenericDao.getNode(key, groupingId, GroupingData.class);
		return findGrouping;
	}

	protected Either<SubCategoryData, TitanOperationStatus> findSubCategory(NodeTypeEnum nodeType, String subCategoryId) {
		String key = UniqueIdBuilder.getKeyByNodeType(nodeType);
		Either<SubCategoryData, TitanOperationStatus> findSubCategory = titanGenericDao.getNode(key, subCategoryId, SubCategoryData.class);
		return findSubCategory;
	}

	protected Either<CategoryData, TitanOperationStatus> findCategory(NodeTypeEnum nodeType, String categoryId) {
		String key = UniqueIdBuilder.getKeyByNodeType(nodeType);
		Either<CategoryData, TitanOperationStatus> findCategory = titanGenericDao.getNode(key, categoryId, CategoryData.class);
		return findCategory;
	}

	protected TitanOperationStatus associateMetadataToComponent(ComponentMetadataData componentData, UserData userData, UserData updater, CategoryData categoryData, List<ResourceMetadataData> derivedResources) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.STATE.getProperty(), componentData.getMetadataDataDefinition().getState());
		Either<GraphRelation, TitanOperationStatus> result = titanGenericDao.createRelation(updater, componentData, GraphEdgeLabels.STATE, props);
		log.debug("After associating user {} to component {}. Edge type is {}" , updater, componentData.getUniqueId(),  GraphEdgeLabels.STATE);
		if (result.isRight()) {
			return result.right().value();
		}

		result = titanGenericDao.createRelation(updater, componentData, GraphEdgeLabels.LAST_MODIFIER, null);
		log.debug("After associating user {} to component {}. Edge type is {}",  updater,  componentData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
		if (result.isRight()) {
			log.error("Failed to associate user {} to component {}. Edge type is {}", updater, componentData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
			return result.right().value();
		}

		result = titanGenericDao.createRelation(userData, componentData, GraphEdgeLabels.CREATOR, null);
		log.debug("After associating user {} to component {}. Edge type is {}" , userData, componentData.getUniqueId(), GraphEdgeLabels.CREATOR);
		if (result.isRight()) {
			log.error("Failed to associate user {} to component {}. Edge type is {}", userData, componentData.getUniqueId(), GraphEdgeLabels.CREATOR);
			return result.right().value();
		}

		if (derivedResources != null) {
			for (ResourceMetadataData derivedResource : derivedResources) {
				log.debug("After associating component {} to parent component {}. Edge type is {}" ,componentData.getUniqueId(), derivedResource.getUniqueId(), GraphEdgeLabels.DERIVED_FROM);
				result = titanGenericDao.createRelation(componentData, derivedResource, GraphEdgeLabels.DERIVED_FROM, null);
				if (result.isRight()) {
					log.error("Failed to associate user {} to component {}. Edge type is {}", userData, componentData.getUniqueId(), GraphEdgeLabels.CREATOR);
					return result.right().value();
				}
			}
		}

		if (categoryData != null) {
			result = titanGenericDao.createRelation(componentData, categoryData, GraphEdgeLabels.CATEGORY, null);
			log.debug("After associating component {} to category {}. Edge type is {}", componentData.getUniqueId(), categoryData, GraphEdgeLabels.CATEGORY);
			if (result.isRight()) {
				log.error("Faield to associate component {} to category {}. Edge type is {}", componentData.getUniqueId(), categoryData, GraphEdgeLabels.CATEGORY);
				return result.right().value();
			}
		}

		return TitanOperationStatus.OK;
	}

	protected StorageOperationStatus associateArtifactsToComponent(NodeTypeEnum nodeType, ComponentMetadataData componentData, Map<String, ArtifactDefinition> artifacts) {

		if (artifacts != null) {
			for (Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {

				ArtifactDefinition artifactDefinition = entry.getValue();
				Either<ArtifactDefinition, StorageOperationStatus> addArifactToResource = Either.left(artifactDefinition);
				addArifactToResource = artifactOperation.addArifactToComponent(artifactDefinition, (String) componentData.getUniqueId(), nodeType, false, true);
				if (addArifactToResource.isRight()) {
					return addArifactToResource.right().value();
				}
			}
		}
		return StorageOperationStatus.OK;

	}

	protected Either<Boolean, StorageOperationStatus> validateResourceNameUniqueness(String name, Map<String, Object> hasProps, Map<String, Object> hasNotProps, TitanGenericDao titanGenericDao) {
		if (hasProps == null) {
			hasProps = new HashMap<String, Object>();
		}
		String normalizedName = ValidationUtils.normaliseComponentName(name);
		hasProps.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), normalizedName);

		Either<List<ResourceMetadataData>, TitanOperationStatus> resources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, hasProps, hasNotProps, ResourceMetadataData.class);
		if (resources.isRight() && resources.right().value() != TitanOperationStatus.NOT_FOUND) {
			log.debug("failed to get resources from graph with property name: {}", name);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resources.right().value()));
		}
		List<ResourceMetadataData> resourceList = (resources.isLeft() ? resources.left().value() : null);
		if (resourceList != null && resourceList.size() > 0) {
			if (log.isDebugEnabled()) {
				StringBuilder builder = new StringBuilder();
				for (ResourceMetadataData resourceData : resourceList) {
					builder.append(resourceData.getUniqueId() + "|");
				}
				log.debug("resources  with property name:{} exists in graph. found {}",name, builder.toString());
			}
			return Either.left(false);
		} else {
			log.debug("resources  with property name:{} does not exists in graph", name);
			return Either.left(true);
		}

	}
	
	protected Either<Boolean, StorageOperationStatus> validateServiceNameUniqueness(String name, TitanGenericDao titanGenericDao) {
		Map<String, Object> properties = new HashMap<>();
		String normalizedName = ValidationUtils.normaliseComponentName(name);
		properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), normalizedName);

		Either<List<ServiceMetadataData>, TitanOperationStatus> services = titanGenericDao.getByCriteria(NodeTypeEnum.Service, properties, ServiceMetadataData.class);
		if (services.isRight() && services.right().value() != TitanOperationStatus.NOT_FOUND) {
			log.debug("failed to get services from graph with property name: {}" , name);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(services.right().value()));
		}
		List<ServiceMetadataData> serviceList = (services.isLeft() ? services.left().value() : null);
		if (serviceList != null && serviceList.size() > 0) {
			if (log.isDebugEnabled()) {
				StringBuilder builder = new StringBuilder();
				for (ServiceMetadataData serviceData : serviceList) {
					builder.append(serviceData.getUniqueId() + "|");
				}
				log.debug("Service with property name:{} exists in graph. found {}" , name, builder.toString());
			}

			return Either.left(false);
		} else {
			log.debug("Service  with property name:{} does not exists in graph", name);
			return Either.left(true);
		}
	}
	
	protected Either<Boolean, StorageOperationStatus> validateToscaResourceNameUniqueness(String name, TitanGenericDao titanGenericDao) {
		Map<String, Object> properties = new HashMap<>();

		properties.put(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty(), name);

		Either<List<ResourceMetadataData>, TitanOperationStatus> resources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, properties, ResourceMetadataData.class);
		if (resources.isRight() && resources.right().value() != TitanOperationStatus.NOT_FOUND) {
			log.debug("failed to get resources from graph with property name: {}" , name);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resources.right().value()));
		}
		List<ResourceMetadataData> resourceList = (resources.isLeft() ? resources.left().value() : null);
		if (resourceList != null && resourceList.size() > 0) {
			if (log.isDebugEnabled()) {
				StringBuilder builder = new StringBuilder();
				for (ResourceMetadataData resourceData : resourceList) {
					builder.append(resourceData.getUniqueId() + "|");
				}
				log.debug("resources  with property name:{} exists in graph. found {}" , name, builder.toString());
			}
			return Either.left(false);
		} else {
			log.debug("resources  with property name:{} does not exists in graph", name);
			return Either.left(true);
		}

	}

	protected Either<Boolean, StorageOperationStatus> validateComponentNameUniqueness(String name, TitanGenericDao titanGenericDao, NodeTypeEnum type) {
		Map<String, Object> properties = new HashMap<>();
		String normalizedName = ValidationUtils.normaliseComponentName(name);
		properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), normalizedName);

		Either<List<ComponentMetadataData>, TitanOperationStatus> components = titanGenericDao.getByCriteria(type, properties, ComponentMetadataData.class);
		if (components.isRight() && components.right().value() != TitanOperationStatus.NOT_FOUND) {
			log.debug("failed to get components from graph with property name: {}" , name);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(components.right().value()));
		}
		List<ComponentMetadataData> componentList = (components.isLeft() ? components.left().value() : null);
		if (componentList != null && componentList.size() > 0) {
			if (log.isDebugEnabled()) {
				StringBuilder builder = new StringBuilder();
				for (ComponentMetadataData componentData : componentList) {
					builder.append(componentData.getUniqueId() + "|");
				}
				log.debug("Component with property name:{} exists in graph. found {}" , name, builder.toString());
			}

			return Either.left(false);
		} else {
			log.debug("Component with property name:{} does not exists in graph", name);
			return Either.left(true);
		}
	}

	protected StorageOperationStatus setArtifactFromGraph(String uniqueId, Component component, NodeTypeEnum type, IArtifactOperation artifactOperation) {
		StorageOperationStatus result = StorageOperationStatus.OK;
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = artifactOperation.getArtifacts(uniqueId, type, true);
		if (artifacts.isRight()) {
			result = artifacts.right().value();
		} else {
			// component.setArtifacts(artifacts.left().value());
			createSpecificArtifactList(component, artifacts.left().value());
		}
		return result;
	}

	protected Component createSpecificArtifactList(Component component, Map<String, ArtifactDefinition> artifacts) {

		if (artifacts != null) {
			Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
			Map<String, ArtifactDefinition> serviceApiArtifacts = new HashMap<>();
			Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();

			Set<Entry<String, ArtifactDefinition>> specificet = new HashSet<>();

			for (Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
				ArtifactDefinition artifact = entry.getValue();
				ArtifactGroupTypeEnum artifactGroupType = artifact.getArtifactGroupType();
				if (artifactGroupType == null) {
					artifactGroupType = ArtifactGroupTypeEnum.INFORMATIONAL;
				}

				switch (artifactGroupType) {
				case DEPLOYMENT:
					deploymentArtifacts.put(artifact.getArtifactLabel(), artifact);
					specificet.add(entry);
					break;
				case SERVICE_API:
					serviceApiArtifacts.put(artifact.getArtifactLabel(), artifact);
					specificet.add(entry);
					break;
				case TOSCA:
					toscaArtifacts.put(artifact.getArtifactLabel(), artifact);
					specificet.add(entry);
					break;
				default:
					break;
				}

			}
			artifacts.entrySet().removeAll(specificet);

			component.setSpecificComponetTypeArtifacts(serviceApiArtifacts);
			component.setDeploymentArtifacts(deploymentArtifacts);
			component.setToscaArtifacts(toscaArtifacts);
			component.setArtifacts(artifacts);
		}
		return component;
	}

	private <T, S extends ComponentMetadataData> Either<List<T>, StorageOperationStatus> collectComponents(TitanGraph graph, NodeTypeEnum neededType, String categoryUid, NodeTypeEnum categoryType, Class<S> clazz, ResourceTypeEnum resourceType) {
		List<T> components = new ArrayList<>();
		Either<List<ImmutablePair<S, GraphEdge>>, TitanOperationStatus> parentNodes = titanGenericDao.getParentNodes(UniqueIdBuilder.getKeyByNodeType(categoryType), categoryUid, GraphEdgeLabels.CATEGORY, neededType, clazz);
		if (parentNodes.isLeft()) {
			for (ImmutablePair<S, GraphEdge> component : parentNodes.left().value()) {
				ComponentMetadataDataDefinition componentData = component.getLeft().getMetadataDataDefinition();
				Boolean isHighest = componentData.isHighestVersion();
				boolean isMatchingResourceType = isMatchingByResourceType(neededType, resourceType, componentData);
				
				if (isHighest && isMatchingResourceType) {
					Either<T, StorageOperationStatus> result = getLightComponent(componentData.getUniqueId(), true);
					if (result.isRight()) {
						return Either.right(result.right().value());
					}
					components.add(result.left().value());
				}
			}
		}
		return Either.left(components);
	}

	private boolean isMatchingByResourceType(NodeTypeEnum componentType, ResourceTypeEnum resourceType,
			ComponentMetadataDataDefinition componentData) {

		boolean isMatching;
		if (componentType == NodeTypeEnum.Resource) {
			if (resourceType == null) {
				isMatching = true;
			} else {
				isMatching = resourceType == ((ResourceMetadataDataDefinition)componentData).getResourceType();
			}
		} else {
			isMatching = true;
		}
		return isMatching;
	}

	protected <T, S extends ComponentMetadataData> Either<List<T>, StorageOperationStatus> fetchByCategoryOrSubCategoryUid(String categoryUid, NodeTypeEnum categoryType, String categoryLabel, NodeTypeEnum neededType, boolean inTransaction,
			Class<S> clazz, ResourceTypeEnum resourceType) {
		try {
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));

			}
			return collectComponents(graph.left().value(), neededType, categoryUid, categoryType, clazz, resourceType);

		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}
	}

	protected <T, S extends ComponentMetadataData> Either<List<T>, StorageOperationStatus> fetchByCategoryOrSubCategoryName(String categoryName, NodeTypeEnum categoryType, String categoryLabel, NodeTypeEnum neededType, boolean inTransaction,
			Class<S> clazz, ResourceTypeEnum resourceType) {
		List<T> components = new ArrayList<>();
		try {
			Class categoryClazz = categoryType == NodeTypeEnum.ServiceNewCategory ? CategoryData.class : SubCategoryData.class;
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
			Either<List<GraphNode>, TitanOperationStatus> getCategory = titanGenericDao.getByCriteria(categoryType, props, categoryClazz);
			if (getCategory.isRight()) {
				return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
			}
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));

			}
			for (GraphNode category : getCategory.left().value()) {
				Either<List<T>, StorageOperationStatus> result = collectComponents(graph.left().value(), neededType, (String) category.getUniqueId(), categoryType, clazz, resourceType);
				if (result.isRight()) {
					return result;
				}
				components.addAll(result.left().value());
			}

			return Either.left(components);
		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}
	}

	<T> Either<List<T>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters, boolean inTransaction, NodeTypeEnum neededType) {
		return null;
	}

	protected Either<List<Component>, StorageOperationStatus> getFollowedComponent(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, boolean inTransaction, TitanGenericDao titanGenericDao,
			NodeTypeEnum neededType) {

		Either<List<Component>, StorageOperationStatus> result = null;

		try {
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
				return result;
			}
			Iterable<TitanVertex> users;

			if (userId == null) {
				// get all users by label
				// for Tester and Admin retrieve all users

				// users =
				// graph.left().value().getVertices(GraphPropertiesDictionary.LABEL.getProperty(),
				// NodeTypeEnum.User.getName());
				users = graph.left().value().query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.User.getName()).vertices();

			} else {
				// for Designer retrieve specific user
				String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
				users = graph.left().value().query().has(key, userId).vertices();
			}
			Iterator<TitanVertex> userIterator = users.iterator();

			List<Component> components = new ArrayList<>();
			while (userIterator.hasNext()) {
				Vertex vertexUser = userIterator.next();

				// get all resource with current state
				Iterator<Edge> iterator = vertexUser.edges(Direction.OUT, GraphEdgeLabels.STATE.getProperty());

				List<Component> componentsPerUser = fetchComponents(lifecycleStates, iterator, neededType, inTransaction);

				HashSet<String> ids = new HashSet<String>();

				if (componentsPerUser != null) {
					for (Component comp : componentsPerUser) {
						ids.add(comp.getUniqueId());
						components.add(comp);
					}
				}

				if (lastStateStates != null && !lastStateStates.isEmpty()) {
					// get all resource with last state
					iterator = vertexUser.edges(Direction.OUT, GraphEdgeLabels.LAST_STATE.getProperty());
					boolean isFirst;
					componentsPerUser = fetchComponents(lastStateStates, iterator, neededType, inTransaction);
					if (componentsPerUser != null) {
						for (Component comp : componentsPerUser) {
							isFirst = true;

							if (ids.contains(comp.getUniqueId())) {
								isFirst = false;
							}
							if (isFirst == true) {
								components.add(comp);
							}

						}
					}
				}

			} // whlile users

			result = Either.left(components);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					titanGenericDao.rollback();
				} else {
					titanGenericDao.commit();
				}
			}
		}

	}

	private List<Component> fetchComponents(Set<LifecycleStateEnum> lifecycleStates, Iterator<Edge> iterator, NodeTypeEnum neededType, boolean inTransaction) {
		List<Component> components = new ArrayList<>();
		while (iterator.hasNext()) {
			Edge edge = iterator.next();

			String stateStr = edge.value(GraphEdgePropertiesDictionary.STATE.getProperty());
			LifecycleStateEnum state = LifecycleStateEnum.findState(stateStr);
			if (state == null) {
				log.debug("not supported STATE for element  {}" , stateStr);
				continue;
			}
			if (lifecycleStates != null && lifecycleStates.contains(state)) {
				Vertex vertexComponent = edge.inVertex();

				Boolean isHighest = vertexComponent.value(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty());
				if (isHighest) {

					String nodeTypeStr = vertexComponent.value(GraphPropertiesDictionary.LABEL.getProperty());
					// get only latest versions
					NodeTypeEnum nodeType = NodeTypeEnum.getByName(nodeTypeStr);

					if (nodeType == null) {
						log.debug("missing node label for vertex {}", vertexComponent);
						continue;
					}

					if (neededType.equals(nodeType)) {
						switch (nodeType) {
						case Service:
							handleNode(components, vertexComponent, nodeType, inTransaction);
							break;
						case Resource:
							Boolean isAbtract = vertexComponent.value(GraphPropertiesDictionary.IS_ABSTRACT.getProperty());
							if (false == isAbtract) {
								handleNode(components, vertexComponent, nodeType, inTransaction);
							} // if not abstract
							break;
						case Product:
							handleNode(components, vertexComponent, nodeType, inTransaction);
							break;
						default:
							log.debug("not supported node type {}", nodeType);
							break;
						}// case
					} // needed type
				}
			} // if
		} // while resources
		return components;
	}

	protected <T> void handleNode(List<T> components, Vertex vertexComponent, NodeTypeEnum nodeType, boolean inTransaction) {
		String id;

		id = vertexComponent.value(UniqueIdBuilder.getKeyByNodeType(nodeType));
		if (id != null) {
			Either<T, StorageOperationStatus> component = getLightComponent(id, inTransaction);
			if (component.isRight()) {
				log.debug("Failed to get component for id =  {}  error : {} skip resource", id, component.right().value());
			} else {
				components.add(component.left().value());
			}
		} else {

			Map<String, Object> properties = this.titanGenericDao.getProperties(vertexComponent);
			log.debug("missing resource unique id for node with properties {}", properties);
		}
	}

	/**
	 * 
	 * @param component
	 * @param inTransaction
	 * @param titanGenericDao
	 * @param clazz
	 * @return
	 */
	public <T> Either<T, StorageOperationStatus> updateComponent(Component component, boolean inTransaction, TitanGenericDao titanGenericDao, Class<T> clazz, NodeTypeEnum type) {

		ComponentParametersView componentParametersView = new ComponentParametersView();
		return updateComponentFilterResult(component, inTransaction, titanGenericDao, clazz, type, componentParametersView);

	}

	private Either<ArtifactData, StorageOperationStatus> generateAndUpdateToscaFileName(String componentType, String componentName, String componentId, NodeTypeEnum type, ArtifactDefinition artifactInfo) {
		Map<String, Object> getConfig = (Map<String, Object>) ConfigurationManager.getConfigurationManager().getConfiguration().getToscaArtifacts().entrySet().stream()
				.filter(p -> p.getKey().equalsIgnoreCase(artifactInfo.getArtifactLabel()))
				.findAny()
				.get()
				.getValue();
		artifactInfo.setArtifactName(componentType + "-" + componentName + getConfig.get("artifactName"));
		return artifactOperation.updateToscaArtifactNameOnGraph(artifactInfo, artifactInfo.getUniqueId(), type, componentId);
	}

	protected StorageOperationStatus moveCategoryEdge(Component component, ComponentMetadataData componentData, CategoryDefinition newCategory, NodeTypeEnum type) {

		StorageOperationStatus result = StorageOperationStatus.OK;

		GraphRelation categoryRelation = new GraphRelation();
		categoryRelation.setType(GraphEdgeLabels.CATEGORY.getProperty());
		RelationEndPoint relationEndPoint = new RelationEndPoint(type, UniqueIdBuilder.getKeyByNodeType(type), component.getUniqueId());
		categoryRelation.setFrom(relationEndPoint);
		Either<GraphRelation, TitanOperationStatus> deleteOutgoingRelation = titanGenericDao.deleteOutgoingRelation(categoryRelation);
		if (deleteOutgoingRelation.isRight()) {
			log.error("Failed to delete category from component {}. Edge type is {}", componentData.getUniqueId(), GraphEdgeLabels.CATEGORY);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(deleteOutgoingRelation.right().value());
			return result;
		}

		log.debug("After removing edge from graph {}", deleteOutgoingRelation);

		NodeTypeEnum categoryType;
		if (NodeTypeEnum.Service.name().equalsIgnoreCase(type.name())) {
			categoryType = NodeTypeEnum.ServiceCategory;
		} else {
			categoryType = NodeTypeEnum.ResourceCategory;
		}
		Either<CategoryData, StorageOperationStatus> categoryResult = elementOperation.getNewCategoryData(newCategory.getName(), NodeTypeEnum.ServiceNewCategory, CategoryData.class);
		if (categoryResult.isRight()) {
			StorageOperationStatus status = categoryResult.right().value();
			log.error("Cannot find category {} in the graph. status is {}", newCategory.getName(), status);
			return status;
		}

		CategoryData categoryData = categoryResult.left().value();
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(componentData, categoryData, GraphEdgeLabels.CATEGORY, null);
		log.debug("After associating category {} to component {}. Edge type is {}", categoryData, componentData.getUniqueId(), GraphEdgeLabels.CATEGORY);
		if (createRelation.isRight()) {
			log.error("Failed to associate category {} to component {}. Edge type is {}", categoryData, componentData.getUniqueId(), GraphEdgeLabels.CATEGORY);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(createRelation.right().value());
			return result;
		}

		return result;
	}

	private StorageOperationStatus moveLastModifierEdge(Component component, ComponentMetadataData componentData, UserData modifierUserData, NodeTypeEnum type) {

		StorageOperationStatus result = StorageOperationStatus.OK;

		GraphRelation lastModifierRelation = new GraphRelation();
		lastModifierRelation.setType(GraphEdgeLabels.LAST_MODIFIER.getProperty());
		RelationEndPoint relationEndPoint = new RelationEndPoint(type, UniqueIdBuilder.getKeyByNodeType(type), component.getUniqueId());
		lastModifierRelation.setTo(relationEndPoint);
		Either<GraphRelation, TitanOperationStatus> deleteIncomingRelation = titanGenericDao.deleteIncomingRelation(lastModifierRelation);
		if (deleteIncomingRelation.isRight()) {
			log.error("Failed to delete user from component {}. Edge type is {}", componentData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(deleteIncomingRelation.right().value());
			return result;
		}

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(modifierUserData, componentData, GraphEdgeLabels.LAST_MODIFIER, null);
		log.debug("After associating user {} to component {}. Edge type is {}", modifierUserData, componentData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
		if (createRelation.isRight()) {
			log.error("Failed to associate user {} to component {}. Edge type is {}", modifierUserData, componentData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(createRelation.right().value());
			return result;
		}
		return result;
	}

	protected abstract ComponentMetadataData getMetaDataFromComponent(Component component);

	public abstract <T> Either<T, StorageOperationStatus> getComponent(String id, boolean inTransaction);

	public abstract <T> Either<T, StorageOperationStatus> getComponent(String id, ComponentParametersView componentParametersView, boolean inTrasnaction);

	protected abstract <T> Either<T, StorageOperationStatus> getComponentByNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction);

	public abstract <T> Either<T, StorageOperationStatus> getLightComponent(String id, boolean inTransaction);

	public abstract <T> Either<List<T>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters, boolean inTransaction);

	abstract Component convertComponentMetadataDataToComponent(ComponentMetadataData componentMetadataData);

	abstract TitanOperationStatus setComponentCategoriesFromGraph(Component component);

	protected abstract Either<Component, StorageOperationStatus> getMetadataComponent(String id, boolean inTransaction);

	protected abstract <T> Either<T, StorageOperationStatus> updateComponent(T component, boolean inTransaction);

	protected abstract <T> Either<T, StorageOperationStatus> updateComponentFilterResult(T component, boolean inTransaction, ComponentParametersView filterParametersView);

	public abstract Either<Component, StorageOperationStatus> deleteComponent(String id, boolean inTransaction);

	public <T> Either<T, StorageOperationStatus> cloneComponent(T other, String version, boolean inTransaction) {
		return cloneComponent(other, version, null, inTransaction);
	}

	public abstract <T> Either<T, StorageOperationStatus> cloneComponent(T other, String version, LifecycleStateEnum targetLifecycle, boolean inTransaction);

	public abstract Component getDefaultComponent();

	public abstract boolean isComponentExist(String componentId);

	public abstract Either<Boolean, StorageOperationStatus> validateComponentNameExists(String componentName);

	public abstract Either<Boolean, StorageOperationStatus> isComponentInUse(String componentId);

	protected Either<Boolean, StorageOperationStatus> isComponentInUse(String componentId, NodeTypeEnum nodeType) {

		Either<GraphRelation, TitanOperationStatus> relationByCriteria = titanGenericDao.getIncomingRelationByCriteria(new UniqueIdData(nodeType, componentId), GraphEdgeLabels.INSTANCE_OF, null);
		if (relationByCriteria.isRight() && !relationByCriteria.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			log.debug("failed to check relations for component node. id = {}, type = {}, error = {}", componentId, nodeType, relationByCriteria.right().value().name());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(relationByCriteria.right().value()));
		}

		if (relationByCriteria.isLeft()) {
			// component is in use
			return Either.left(true);
		} else {
			return Either.left(false);
		}

	}

	public abstract Either<List<String>, StorageOperationStatus> getAllComponentsMarkedForDeletion();

	protected Either<List<String>, StorageOperationStatus> getAllComponentsMarkedForDeletion(NodeTypeEnum nodeType) {

		List<String> componentIdsToDelete = new ArrayList<String>();
		// get all components marked for delete
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.IS_DELETED.getProperty(), true);

		Either<List<ComponentMetadataData>, TitanOperationStatus> componentsToDelete = titanGenericDao.getByCriteria(nodeType, props, ComponentMetadataData.class);

		if (componentsToDelete.isRight()) {
			TitanOperationStatus error = componentsToDelete.right().value();
			if (error.equals(TitanOperationStatus.NOT_FOUND)) {
				log.trace("no components to delete");
				return Either.left(componentIdsToDelete);
			} else {
				log.info("failed to find components to delete. error : {}", error.name());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(error));
			}

		}
		for (ComponentMetadataData resourceData : componentsToDelete.left().value()) {
			componentIdsToDelete.add(resourceData.getMetadataDataDefinition().getUniqueId());
		}
		return Either.left(componentIdsToDelete);
	}

	protected <T extends GraphNode> Either<List<T>, TitanOperationStatus> __getLastVersion(NodeTypeEnum type, Map<String, Object> props, Class<T> clazz) {
		try {

			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				return Either.right(graph.right().value());
			}

			TitanGraph tGraph = graph.left().value();
			TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
			query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());

			if (props != null && !props.isEmpty()) {
				for (Map.Entry<String, Object> entry : props.entrySet()) {
					query = query.hasNot(entry.getKey(), entry.getValue());
				}
			}
			query.has(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

			Iterable<TitanVertex> vertices = query.vertices();

			if (vertices == null) {
				return Either.right(TitanOperationStatus.NOT_FOUND);
			}

			Iterator<TitanVertex> iterator = vertices.iterator();
			List<T> result = new ArrayList<T>();

			while (iterator.hasNext()) {
				Vertex vertex = iterator.next();

				Map<String, Object> newProp = titanGenericDao.getProperties(vertex);
				T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
				result.add(element);
			}
			if (result.size() == 0) {
				return Either.right(TitanOperationStatus.NOT_FOUND);
			}
			log.debug("No nodes in graph for criteria : from type = {} and properties = {}", type, props);
			return Either.left(result);
		} catch (Exception e) {
			log.debug("Failed get by criteria for type = {} and properties = {}", type, props, e);
			return Either.right(TitanGraphClient.handleTitanException(e));
		}
	}

	protected <T extends GraphNode> Either<List<T>, TitanOperationStatus> getLastVersion(NodeTypeEnum type, Map<String, Object> hasNotProps, Class<T> clazz) {
		return getLastVersion(type, null, hasNotProps, clazz);
	}

	protected <T extends GraphNode> Either<List<T>, TitanOperationStatus> getLastVersion(NodeTypeEnum type, Map<String, Object> hasProps, Map<String, Object> hasNotProps, Class<T> clazz) {

		Map<String, Object> props = new HashMap<>();

		if (hasProps != null) {
			props.putAll(hasProps);
		}
		props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

		Either<List<T>, TitanOperationStatus> byCriteria = titanGenericDao.getByCriteria(type, props, hasNotProps, clazz);

		return byCriteria;

	}

	public <T, S extends GraphNode> Either<Set<T>, StorageOperationStatus> getComponentCatalogData(NodeTypeEnum type, Map<String, Object> propertiesToMatch, Class<T> clazz1, Class<S> clazz2, boolean inTransaction) {
		log.debug("Start getComponentCatalogData for type: {}", type.name());
		Set<T> result = new HashSet<T>();
		Either<List<S>, TitanOperationStatus> lastVersionNodes = getLastVersion(type, propertiesToMatch, clazz2);
		Either<Set<T>, StorageOperationStatus> last = retrieveComponentsFromNodes(lastVersionNodes, inTransaction);
		if (last.isLeft() && last.left().value() != null) {
			result.addAll(last.left().value());
		}
		if (type == NodeTypeEnum.Resource) {
			propertiesToMatch.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), false);
		}
		Either<List<S>, TitanOperationStatus> componentsNodes = titanGenericDao.getByCriteria(type, propertiesToMatch, clazz2);
		Either<Set<T>, StorageOperationStatus> certified = retrieveComponentsFromNodes(componentsNodes, inTransaction);
		if (certified.isLeft() && certified.left().value() != null) {
			result.addAll(certified.left().value());
		}
		return Either.left(result);

	}

	protected <T, S extends GraphNode> Either<Set<T>, StorageOperationStatus> retrieveComponentsFromNodes(Either<List<S>, TitanOperationStatus> componentsNodes, boolean inTransaction) {
		Set<T> result = new HashSet<T>();
		if (componentsNodes.isRight()) {
			// in case of NOT_FOUND from Titan client return to UI empty list
			if (componentsNodes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("No components were found");
			} else {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(componentsNodes.right().value()));
			}
		} else {
			List<S> componentDataList = componentsNodes.left().value();
			for (S componentData : componentDataList) {
				// Either<T, StorageOperationStatus> component =
				// getComponent((String) componentData.getUniqueId(),
				// inTransaction);
				Either<T, StorageOperationStatus> component = getLightComponent((String) componentData.getUniqueId(), inTransaction);
				if (component.isRight()) {
					log.debug("Failed to get component for id =  {}  error : {} skip resource", componentData.getUniqueId(), component.right().value());
					// return Either.right(service.right().value());
				} else {
					result.add(component.left().value());
				}
			}
		}
		return Either.left(result);
	}

	protected StorageOperationStatus removeArtifactsFromComponent(Component component, NodeTypeEnum componentType) {

		String componentId = component.getUniqueId();
		// Map<String, ArtifactDefinition> artifacts = component.getArtifacts();
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsRes = artifactOperation.getArtifacts(componentId, componentType, true);
		if (artifactsRes.isRight() && !artifactsRes.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
			return artifactsRes.right().value();
		}
		if (artifactsRes.isLeft() && artifactsRes.left().value() != null) {
			Map<String, ArtifactDefinition> artifacts = artifactsRes.left().value();
			for (Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {

				ArtifactDefinition artifactDefinition = entry.getValue();
				Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromResource = artifactOperation.removeArifactFromResource(componentId, artifactDefinition.getUniqueId(), componentType, true, true);
				if (removeArifactFromResource.isRight()) {
					return removeArifactFromResource.right().value();
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	public Either<List<Component>, StorageOperationStatus> getTesterFollowedComponent(String userId, Set<LifecycleStateEnum> lifecycleStates, boolean inTransaction, NodeTypeEnum neededType) {
		List<Component> resList = new ArrayList<>();
		Either<List<Component>, StorageOperationStatus> rip = getFollowedComponent(userId, lifecycleStates, null, inTransaction, titanGenericDao, neededType);
		if (rip.isLeft()) {
			List<Component> ripRes = rip.left().value();
			if (ripRes != null && !ripRes.isEmpty()) {
				resList.addAll(ripRes);
			}
			Set<LifecycleStateEnum> rfcState = new HashSet<>();
			rfcState.add(LifecycleStateEnum.READY_FOR_CERTIFICATION);
			Either<List<Component>, StorageOperationStatus> rfc = getFollowedComponent(null, rfcState, null, inTransaction, titanGenericDao, neededType);
			if (rfc.isLeft()) {
				List<Component> rfcRes = rfc.left().value();
				if (rfcRes != null && !rfcRes.isEmpty()) {
					resList.addAll(rfcRes);
				}
			} else {
				return Either.right(rfc.right().value());
			}

		} else {
			return Either.right(rip.right().value());
		}
		return Either.left(resList);

	}

	/**
	 * generate UUID only for case that version is "XX.01" - (start new version)
	 * 
	 * @param component
	 */
	protected void generateUUID(Component component) {
		String version = component.getVersion();
		if (uuidNewVersion.matcher(version).matches()) {
			UUID uuid = UUID.randomUUID();
			component.getComponentMetadataDefinition().getMetadataDataDefinition().setUUID(uuid.toString());
			MDC.put("serviceInstanceID", uuid.toString());
		}
	}

	protected <T extends GraphNode> Either<Map<String, String>, TitanOperationStatus> getVersionList(NodeTypeEnum type, String version, Component component, Class<T> clazz) {
		return getVersionList(type, version, component.getUUID(), component.getSystemName(), clazz);
	}

	protected <T extends GraphNode> Either<Map<String, String>, TitanOperationStatus> getVersionList(NodeTypeEnum type, String version, String uuid, String systemName, Class<T> clazz) {
		Map<String, Object> props = new HashMap<String, Object>();
		Map<String, Object> hasNotProps = new HashMap<String, Object>();

		if (version.startsWith("0")) {
			props.put(GraphPropertiesDictionary.UUID.getProperty(), uuid);
		} else {
			props.put(GraphPropertiesDictionary.SYSTEM_NAME.getProperty(), systemName);
		}
		hasNotProps.put(GraphPropertiesDictionary.IS_DELETED.getProperty(), true);
		Either<List<T>, TitanOperationStatus> result = titanGenericDao.getByCriteria(type, props, hasNotProps, clazz);

		Map<String, String> versionMap = new HashMap<String, String>();
		if (result.isRight()) {
			if (!result.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				return Either.right(result.right().value());
			}

		} else {
			switch (type) {
			case Resource:
				List<ResourceMetadataData> components = (List<ResourceMetadataData>) result.left().value();
				for (ResourceMetadataData data : components) {
					versionMap.put(data.getMetadataDataDefinition().getVersion(), (String) data.getUniqueId());
				}
				break;
			case Service:
				List<ServiceMetadataData> componentsS = (List<ServiceMetadataData>) result.left().value();
				for (ServiceMetadataData data : componentsS) {
					versionMap.put(data.getMetadataDataDefinition().getVersion(), (String) data.getUniqueId());
				}
				break;
			case Product:
				List<ProductMetadataData> componentsP = (List<ProductMetadataData>) result.left().value();
				for (ProductMetadataData data : componentsP) {
					versionMap.put(data.getMetadataDataDefinition().getVersion(), (String) data.getUniqueId());
				}
				break;
			default:
				break;
			}
		}

		return Either.left(versionMap);
	}

	protected StorageOperationStatus deleteAdditionalInformation(NodeTypeEnum nodeType, String componentId) {

		Either<AdditionalInformationDefinition, StorageOperationStatus> deleteRes = additionalInformationOperation.deleteAllAdditionalInformationParameters(nodeType, componentId, true);

		if (deleteRes.isRight()) {
			StorageOperationStatus status = deleteRes.right().value();
			return status;
		}

		return StorageOperationStatus.OK;

	}

	protected StorageOperationStatus addAdditionalInformation(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition informationDefinition) {

		Either<AdditionalInformationDefinition, TitanOperationStatus> status = additionalInformationOperation.addAdditionalInformationNode(nodeType, componentId, informationDefinition);

		if (status.isRight()) {
			TitanOperationStatus titanStatus = status.right().value();
			return DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
		}

		log.trace("After adding additional information to component {}. Result is {}" , componentId ,status.left().value());

		return StorageOperationStatus.OK;

	}

	protected StorageOperationStatus addAdditionalInformation(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition informationDefinition, TitanVertex metadataVertex) {

		TitanOperationStatus status = additionalInformationOperation.addAdditionalInformationNode(nodeType, componentId, informationDefinition, metadataVertex);
		log.trace("After adding additional information to component {}. Result is {}", componentId, status);

		if (!status.equals(TitanOperationStatus.OK)) {
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}

		return StorageOperationStatus.OK;

	}

	public Either<List<ArtifactDefinition>, StorageOperationStatus> getComponentArtifactsForDelete(String parentId, NodeTypeEnum parentType, boolean inTransacton) {
		List<ArtifactDefinition> artifacts = new ArrayList<ArtifactDefinition>();
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsResponse = artifactOperation.getArtifacts(parentId, parentType, inTransacton);
		if (artifactsResponse.isRight()) {
			if (!artifactsResponse.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				log.debug("failed to retrieve artifacts for {} {}", parentType, parentId);
				return Either.right(artifactsResponse.right().value());
			}
		} else {
			artifacts.addAll(artifactsResponse.left().value().values());
		}

		if (NodeTypeEnum.Resource.equals(parentType)) {
			Either<List<ArtifactDefinition>, StorageOperationStatus> interfacesArtifactsForResource = getAdditionalArtifacts(parentId, false, true);
			if (artifactsResponse.isRight() && !interfacesArtifactsForResource.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
				log.debug("failed to retrieve interface artifacts for {} {}", parentType, parentId);
				return Either.right(interfacesArtifactsForResource.right().value());
			} else if (artifactsResponse.isLeft()) {
				artifacts.addAll(interfacesArtifactsForResource.left().value());
			}
		}
		return Either.left(artifacts);
	}

	protected void addComponentInternalFields(ComponentMetadataData componentMetadataData) {
		org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition metadataDataDefinition = componentMetadataData.getMetadataDataDefinition();
		Long creationDate = metadataDataDefinition.getCreationDate();

		long currentDate = System.currentTimeMillis();
		if (creationDate == null) {
			metadataDataDefinition.setCreationDate(currentDate);
		}
		metadataDataDefinition.setLastUpdateDate(currentDate);

		String lifecycleStateEnum = metadataDataDefinition.getState();
		if (lifecycleStateEnum == null) {
			metadataDataDefinition.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		}
		String componentUniqueId = UniqueIdBuilder.buildComponentUniqueId();
		metadataDataDefinition.setUniqueId(componentUniqueId);
		metadataDataDefinition.setHighestVersion(true);
	}

	protected StorageOperationStatus createTagsForComponent(Component component) {
		List<String> tags = component.getTags();
		if (tags != null && false == tags.isEmpty()) {
			Either<List<TagData>, StorageOperationStatus> tagsResult = createNewTagsList(tags);

			if (tagsResult == null) {
				log.debug("tagsResult is null");
				return StorageOperationStatus.GENERAL_ERROR;
			}
			if (tagsResult.isRight()) {
				return tagsResult.right().value();
			}
			List<TagData> tagsToCreate = tagsResult.left().value();
			return createTagNodesOnGraph(tagsToCreate);
		}
		log.trace("All tags created succesfully for component {}", component.getUniqueId());
		return StorageOperationStatus.OK;
	}

	protected Either<List<GroupingData>, StorageOperationStatus> findGroupingsForComponent(NodeTypeEnum nodeTypeEnum, Component component) {
		List<CategoryDefinition> categories = component.getCategories();
		List<GroupingData> groupingDataToAssociate = new ArrayList<>();
		if (categories != null) {
			groupingDataToAssociate = new ArrayList<>();
			for (CategoryDefinition categoryDefinition : categories) {
				List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
				if (subcategories != null) {
					for (SubCategoryDefinition subCategoryDefinition : subcategories) {
						List<GroupingDefinition> groupingDataDefinitions = subCategoryDefinition.getGroupings();
						if (groupingDataDefinitions != null) {
							for (GroupingDataDefinition grouping : groupingDataDefinitions) {
								String groupingId = grouping.getUniqueId();
								Either<GroupingData, TitanOperationStatus> findGroupingEither = findGrouping(nodeTypeEnum, groupingId);
								if (findGroupingEither.isRight()) {
									TitanOperationStatus status = findGroupingEither.right().value();
									log.error("Cannot find grouping {} in the graph. status is {}", groupingId, status);
									if (status == TitanOperationStatus.NOT_FOUND) {
										return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
									}
									return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
								} else {
									groupingDataToAssociate.add(findGroupingEither.left().value());
								}
							}
						}
					}
				}
			}
		}
		return Either.left(groupingDataToAssociate);
	}

	protected TitanOperationStatus associateGroupingsToComponent(ComponentMetadataData componentMetadataData, List<GroupingData> groupingDataToAssociate) {
		for (GroupingData groupingData : groupingDataToAssociate) {
			GraphEdgeLabels groupingLabel = GraphEdgeLabels.GROUPING;
			Either<GraphRelation, TitanOperationStatus> result = titanGenericDao.createRelation(componentMetadataData, groupingData, groupingLabel, null);
			log.debug("After associating grouping {} to component {}. Edge type is {}", groupingData, componentMetadataData, groupingLabel);
			if (result.isRight()) {
				return result.right().value();
			}
		}
		log.trace("All groupings associated succesfully to component {}", componentMetadataData);
		return TitanOperationStatus.OK;
	}

	public abstract Either<Integer, StorageOperationStatus> increaseAndGetComponentInstanceCounter(String componentId, boolean inTransaction);

	protected Either<Integer, StorageOperationStatus> increaseAndGetComponentInstanceCounter(String componentId, NodeTypeEnum nodeType, boolean inTransaction) {
		Either<Integer, StorageOperationStatus> result = null;
		try {

			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
				return result;
			}
			Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId);
			if (vertexService.isRight()) {
				log.debug("failed to fetch vertex of component metadata, nodeType:{} , id: {}", nodeType, componentId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexService.right().value()));
				return result;
			}
			Vertex vertex = vertexService.left().value();
			Integer instanceCounter = vertex.value(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty());
			++instanceCounter;
			vertex.property(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty(), instanceCounter);
			result = Either.left(instanceCounter);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("increaseAndGetComponentInstanceCounter operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("increaseAndGetComponentInstanceCounter operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	protected Either<Integer, StorageOperationStatus> setComponentInstanceCounter(String componentId, NodeTypeEnum nodeType, int counter, boolean inTransaction) {
		Either<Integer, StorageOperationStatus> result = null;
		try {

			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
				return result;
			}
			Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId);
			if (vertexService.isRight()) {
				log.debug("failed to fetch vertex of component metadata ofor id = {}", componentId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexService.right().value()));
				return result;
			}
			Vertex vertex = vertexService.left().value();
			vertex.property(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty(), counter);
			result = Either.left(counter);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("deleteService operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("deleteService operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	protected TitanOperationStatus setComponentInstancesFromGraph(String uniqueId, Component component, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType) {

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> resourceInstancesOfService = componentInstanceOperation.getComponentInstancesOfComponent(uniqueId, containerNodeType, compInstNodeType);

		if (resourceInstancesOfService.isRight()) {
			TitanOperationStatus status = resourceInstancesOfService.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			} else {
				log.error("Failed to fetch resource instances and their relations. status is {}", status);
			}
			return status;
		}

		ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>> immutablePair = resourceInstancesOfService.left().value();
		List<ComponentInstance> instances = immutablePair.getKey();
		List<RequirementCapabilityRelDef> relations = immutablePair.getValue();

		component.setComponentInstances(instances);
		component.setComponentInstancesRelations(relations);

		return TitanOperationStatus.OK;
	}

	/**
	 * set all properties of all of its resources
	 * 
	 * @param uniqueId
	 * @return
	 */
	protected TitanOperationStatus ___setComponentInstancesPropertiesFromGraph(String uniqueId, Component component) {

		List<ComponentInstance> resourceInstances = component.getComponentInstances();

		Map<String, List<ComponentInstanceProperty>> resourceInstancesProperties = new HashMap<>();

		Map<String, List<PropertyDefinition>> alreadyProcessedResources = new HashMap<>();

		if (resourceInstances != null) {
			for (ComponentInstance resourceInstance : resourceInstances) {

				log.debug("Going to update properties of resource instance {}", resourceInstance.getUniqueId());
				String resourceUid = resourceInstance.getComponentUid();

				List<PropertyDefinition> properties = alreadyProcessedResources.get(resourceUid);
				if (properties == null) {
					properties = new ArrayList<>();
					TitanOperationStatus findAllRes = propertyOperation.findAllResourcePropertiesRecursively(resourceUid, properties);
					if (findAllRes != TitanOperationStatus.OK) {
						return findAllRes;
					}
					alreadyProcessedResources.put(resourceUid, properties);
				}
				log.debug("After getting properties of resource {}. Number of properties is {}", resourceUid, (properties == null ? 0 : properties.size()));
				if (false == properties.isEmpty()) {

					String resourceInstanceUid = resourceInstance.getUniqueId();

					Either<List<ComponentInstanceProperty>, TitanOperationStatus> propertyValuesRes = propertyOperation.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceUid);
					log.debug("After fetching property under resource instance {}", resourceInstanceUid);
					if (propertyValuesRes.isRight()) {
						TitanOperationStatus status = propertyValuesRes.right().value();
						if (status != TitanOperationStatus.NOT_FOUND) {
							return status;
						}
					}

					Map<String, ComponentInstanceProperty> propertyIdToValue = new HashMap<>();
					populateMapperWithPropertyValues(propertyValuesRes, propertyIdToValue);

					List<ComponentInstanceProperty> resourceInstancePropertyList = new ArrayList<>();
					for (PropertyDefinition propertyDefinition : properties) {

						String defaultValue = propertyDefinition.getDefaultValue();
						String value = defaultValue;
						String valueUid = null;

						String propertyId = propertyDefinition.getUniqueId();
						ComponentInstanceProperty valuedProperty = propertyIdToValue.get(propertyId);
						if (valuedProperty != null) {
							String newValue = valuedProperty.getValue();
							// if (newValue != null) {
							value = newValue;
							// }

							valueUid = valuedProperty.getValueUniqueUid();
							log.trace("Found value {} under resource instance which override the default value {}" , value, defaultValue);
						}
						ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty(propertyDefinition, value, valueUid);

						// TODO: currently ignore constraints since they are not
						// inuse and cause to error in convertion to object.
						resourceInstanceProperty.setConstraints(null);

						resourceInstancePropertyList.add(resourceInstanceProperty);

					}

					resourceInstancesProperties.put(resourceInstanceUid, resourceInstancePropertyList);
				}

			}

			component.setComponentInstancesProperties(resourceInstancesProperties);
		}

		return TitanOperationStatus.OK;
	}

	private void populateMapperWithPropertyValues(Either<List<ComponentInstanceProperty>, TitanOperationStatus> propertyValuesRes, Map<String, ComponentInstanceProperty> propertyIdToValue) {

		if (propertyValuesRes.isLeft()) {
			List<ComponentInstanceProperty> resourceInstanceValues = propertyValuesRes.left().value();
			if (resourceInstanceValues != null) {
				for (ComponentInstanceProperty resourceInstanceProperty : resourceInstanceValues) {
					propertyIdToValue.put(resourceInstanceProperty.getUniqueId(), resourceInstanceProperty);
				}
			}
		}
	}

	public abstract Either<List<ArtifactDefinition>, StorageOperationStatus> getAdditionalArtifacts(String resourceId, boolean recursively, boolean inTransaction);

	protected abstract StorageOperationStatus validateCategories(Component currentComponent, Component component, ComponentMetadataData componentData, NodeTypeEnum type);

	protected abstract <T extends Component> StorageOperationStatus updateDerived(Component component, Component currentComponent, ComponentMetadataData updatedResourceData, Class<T> clazz);

	public abstract Either<Component, StorageOperationStatus> markComponentToDelete(Component componentToDelete, boolean inTransaction);

	protected Either<Component, StorageOperationStatus> internalMarkComponentToDelete(Component componentToDelete, boolean inTransaction) {
		Either<Component, StorageOperationStatus> result = null;

		if ((componentToDelete.getIsDeleted() != null) && componentToDelete.getIsDeleted() && !componentToDelete.isHighestVersion()) {
			// component already marked for delete
			result = Either.left(componentToDelete);
			return result;
		} else {

			ComponentMetadataData componentMetaData = getMetaDataFromComponent(componentToDelete);

			componentMetaData.getMetadataDataDefinition().setIsDeleted(true);
			componentMetaData.getMetadataDataDefinition().setHighestVersion(false);
			componentMetaData.getMetadataDataDefinition().setLastUpdateDate(System.currentTimeMillis());
			try {
				Either<ComponentMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(componentMetaData, ComponentMetadataData.class);

				StorageOperationStatus updateComponent;
				if (updateNode.isRight()) {
					log.debug("Failed to update component {}. status is {}", componentMetaData.getUniqueId(), updateNode.right().value());
					updateComponent = DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value());
					result = Either.right(updateComponent);
					return result;
				}

				result = Either.left(componentToDelete);
				return result;
			} finally {

				if (false == inTransaction) {
					if (result == null || result.isRight()) {
						log.error("updateResource operation : Going to execute rollback on graph.");
						titanGenericDao.rollback();
					} else {
						log.debug("updateResource operation : Going to execute commit on graph.");
						titanGenericDao.commit();
					}
				}

			}
		}
	}

	private Either<List<RequirementDefinition>, TitanOperationStatus> convertReqDataListToReqDefList(ComponentInstance componentInstance, List<ImmutablePair<RequirementData, GraphEdge>> requirementData) {
		ConvertDataToDef<RequirementDefinition, RequirementData> convertor = (instance, data, edge) -> convertReqDataToReqDef(instance, data, edge);
		AddOwnerData<RequirementDefinition> dataAdder = (reqDef, compInstance) -> addOwnerDataReq(reqDef, compInstance);
		return convertDataToDefinition(componentInstance, requirementData, convertor, dataAdder);
	}

	private Either<List<CapabilityDefinition>, TitanOperationStatus> convertCapDataListToCapDefList(ComponentInstance componentInstance, List<ImmutablePair<CapabilityData, GraphEdge>> capabilityData) {
		ConvertDataToDef<CapabilityDefinition, CapabilityData> convertor = (instance, data, edge) -> convertCapDataToCapDef(instance, data, edge);
		AddOwnerData<CapabilityDefinition> dataAdder = (capDef, compInstance) -> addOwnerDataCap(capDef, compInstance);
		Either<List<CapabilityDefinition>, TitanOperationStatus> convertationResult = convertDataToDefinition(componentInstance, capabilityData, convertor, dataAdder);
		if (convertationResult.isLeft()) {
			convertationResult = componentInstanceOperation.updateCapDefPropertyValues(componentInstance, convertationResult.left().value());
		}
		return convertationResult;
	}

	private Either<CapabilityDefinition, TitanOperationStatus> convertCapDataToCapDef(ComponentInstance componentInstance, CapabilityData data, GraphEdge edge) {
		Either<CapabilityDefinition, TitanOperationStatus> eitherDef = capabilityOperation.getCapabilityByCapabilityData(data);

		if (eitherDef.isLeft()) {
			CapabilityDefinition capDef = eitherDef.left().value();
			Map<String, Object> properties = edge.getProperties();
			if (properties != null) {
				String name = (String) properties.get(GraphEdgePropertiesDictionary.NAME.getProperty());
				String source = (String) properties.get(GraphEdgePropertiesDictionary.SOURCE.getProperty());
				List<String> sourcesList = new ArrayList<String>();
				capabilityOperation.getCapabilitySourcesList(source, sourcesList);
				capDef.setName(name);
				capDef.setCapabilitySources(sourcesList);
				capDef.setPath(toscaDefinitionPathCalculator.calculateToscaDefinitionPath(componentInstance, edge));

				String requiredOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
				if (requiredOccurrences != null) {
					capDef.setMinOccurrences(requiredOccurrences);
				}
				String leftOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
				if (leftOccurrences != null) {
					capDef.setMaxOccurrences(leftOccurrences);
				}

			}
			eitherDef = Either.left(capDef);
		}
		return eitherDef;
	}

	private Either<RequirementDefinition, TitanOperationStatus> convertReqDataToReqDef(ComponentInstance componentInstance, RequirementData data, GraphEdge edge) {
		Either<RequirementDefinition, TitanOperationStatus> eitherDef = requirementOperation.getRequirement(data.getUniqueId());

		if (eitherDef.isLeft()) {
			RequirementDefinition requirementDef = eitherDef.left().value();
			Map<String, Object> properties = edge.getProperties();
			if (properties != null) {
				String name = (String) properties.get(GraphEdgePropertiesDictionary.NAME.getProperty());
				requirementDef.setName(name);
				String requiredOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
				if (requiredOccurrences != null) {
					requirementDef.setMinOccurrences(requiredOccurrences);
				}
				requirementDef.setPath(toscaDefinitionPathCalculator.calculateToscaDefinitionPath(componentInstance, edge));
				String leftOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
				if (leftOccurrences != null) {
					requirementDef.setMaxOccurrences(leftOccurrences);
				}
			}
			eitherDef = Either.left(requirementDef);
		}
		return eitherDef;
	}

	private <Def, Data> Either<List<Def>, TitanOperationStatus> convertDataToDefinition(ComponentInstance componentInstance, List<ImmutablePair<Data, GraphEdge>> requirementData, ConvertDataToDef<Def, Data> convertor, AddOwnerData<Def> dataAdder) {
		Either<List<Def>, TitanOperationStatus> eitherResult;
		// Convert Data To Definition
		Stream<Either<Def, TitanOperationStatus>> reqDefStream = requirementData.stream().map(e -> convertor.convert(componentInstance, e.left, e.right));

		// Collect But Stop After First Error
		List<Either<Def, TitanOperationStatus>> filteredReqDefList = StreamUtils.takeWhilePlusOne(reqDefStream, p -> p.isLeft()).collect(Collectors.toList());
		Optional<Either<Def, TitanOperationStatus>> optionalError = filteredReqDefList.stream().filter(p -> p.isRight()).findAny();
		if (optionalError.isPresent()) {
			eitherResult = Either.right(optionalError.get().right().value());
		} else {
			// Convert From Either To Definition And Collect
			List<Def> reqDefList = filteredReqDefList.stream().map(e -> e.left().value()).collect(Collectors.toList());
			// Add Owner Data
			reqDefList.forEach(e -> dataAdder.addData(e, componentInstance));
			eitherResult = Either.left(reqDefList);
		}

		return eitherResult;
	}

	interface ConvertDataToDef<Def, Data> {
		Either<Def, TitanOperationStatus> convert(ComponentInstance compInstance, Data d, GraphEdge edge);
	}

	interface AddOwnerData<Def> {
		void addData(Def def, ComponentInstance compInstance);
	}

	private void addOwnerDataCap(CapabilityDefinition capDef, ComponentInstance componentInstance) {
		capDef.setOwnerId(componentInstance.getUniqueId());
		capDef.setOwnerName(componentInstance.getName());
	}

	private void addOwnerDataReq(RequirementDefinition reqDef, ComponentInstance componentInstance) {
		reqDef.setOwnerId(componentInstance.getUniqueId());
		reqDef.setOwnerName(componentInstance.getName());
	}

	public Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> getRequirements(Component component, NodeTypeEnum nodeTypeEnum, boolean inTransaction) {
		final HashMap<String, List<RequirementDefinition>> emptyMap = new HashMap<>();
		Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> eitherResult = Either.left(emptyMap);
		try {
			List<ComponentInstance> componentInstances = component.getComponentInstances();
			if (componentInstances != null) {
				Function<ComponentInstance, Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus>> dataCollector = e -> componentInstanceOperation.getRequirements(e, nodeTypeEnum);
				Either<List<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus>>>, TitanOperationStatus> eitherDataCollected = collectDataFromComponentsInstances(componentInstances,
						dataCollector);
				if (eitherDataCollected.isRight()) {
					eitherResult = Either.right(eitherDataCollected.right().value());
				} else {
					// Converts Data to Def stop if encountered conversion error
					DataDefConvertor<RequirementDefinition, RequirementData> someConvertor = (e1, e2) -> convertReqDataListToReqDefList(e1, e2);
					Either<List<List<RequirementDefinition>>, TitanOperationStatus> fullDefList = convertDataToDefComponentLevel(eitherDataCollected.left().value(), someConvertor);
					if (fullDefList.isRight()) {
						eitherResult = Either.right(fullDefList.right().value());
					} else {
						Stream<RequirementDefinition> defStream = fullDefList.left().value().stream().flatMap(e -> e.stream());
						// Collect to Map and using grouping by
						Map<String, List<RequirementDefinition>> capTypeCapListMap = defStream.collect(Collectors.groupingBy(e -> e.getCapability()));
						eitherResult = Either.left(capTypeCapListMap);
					}

				}

			}
		} finally {
			if (inTransaction == false) {
				titanGenericDao.commit();
			}
		}

		return eitherResult;
	}

	public Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> getCapabilities(Component component, NodeTypeEnum nodeTypeEnum, boolean inTransaction) {
		final HashMap<String, List<CapabilityDefinition>> emptyMap = new HashMap<>();
		Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> eitherResult = Either.left(emptyMap);
		try {
			List<ComponentInstance> componentInstances = component.getComponentInstances();
			if (componentInstances != null) {
				Function<ComponentInstance, Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus>> dataCollector = e -> componentInstanceOperation.getCapabilities(e, nodeTypeEnum);
				Either<List<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus>>>, TitanOperationStatus> eitherDataCollected = collectDataFromComponentsInstances(componentInstances,
						dataCollector);
				if (eitherDataCollected.isRight()) {
					eitherResult = Either.right(eitherDataCollected.right().value());
				} else {
					// Converts CapData to CapDef removes stop if encountered
					// conversion error
					DataDefConvertor<CapabilityDefinition, CapabilityData> someConvertor = (e1, e2) -> convertCapDataListToCapDefList(e1, e2);
					Either<List<List<CapabilityDefinition>>, TitanOperationStatus> fullDefList = convertDataToDefComponentLevel(eitherDataCollected.left().value(), someConvertor);
					if (fullDefList.isRight()) {
						eitherResult = Either.right(fullDefList.right().value());
					} else {
						Stream<CapabilityDefinition> defStream = fullDefList.left().value().stream().flatMap(e -> e.stream());
						// Collect to Map grouping by Type
						Map<String, List<CapabilityDefinition>> capTypeCapListMap = defStream.collect(Collectors.groupingBy(e -> e.getType()));
						eitherResult = Either.left(capTypeCapListMap);
					}

				}

			}
		} finally {
			if (inTransaction == false) {
				titanGenericDao.commit();
			}
		}

		return eitherResult;
	}

	public <Data> Either<List<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>>>, TitanOperationStatus> collectDataFromComponentsInstances(List<ComponentInstance> componentInstances,
			Function<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>> dataGetter) {
		Either<List<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>>>, TitanOperationStatus> eitherResult;

		// Get List of Each componentInstance and it's Capabilities Data
		Stream<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>>> ownerDataStream = componentInstances.stream().map(element -> new ImmutablePair<>(element, dataGetter.apply(element)));
		// Collect but stop after first error
		List<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>>> ownerCapDataList = StreamUtils
				.takeWhilePlusOne(ownerDataStream, p -> p.right.isLeft() || p.right.isRight() && p.right.right().value() == TitanOperationStatus.NOT_FOUND).collect(Collectors.toList());

		Optional<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>>> optionalError = ownerCapDataList.stream()
				.filter(p -> p.right.isRight() && p.right.right().value() != TitanOperationStatus.NOT_FOUND).findAny();
		if (optionalError.isPresent()) {
			eitherResult = Either.right(optionalError.get().right.right().value());
		} else {
			eitherResult = Either.left(ownerCapDataList.stream().filter(p -> p.right.isLeft()).collect(Collectors.toList()));
		}

		return eitherResult;
	}

	interface DataDefConvertor<Def, Data> {
		Either<List<Def>, TitanOperationStatus> convertDataToDefComponentInstance(ComponentInstance componentInstance, List<ImmutablePair<Data, GraphEdge>> data);
	}

	public <Def, Data> Either<List<List<Def>>, TitanOperationStatus> convertDataToDefComponentLevel(List<ImmutablePair<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>>> ownerCapDataList,
			DataDefConvertor<Def, Data> convertor) {
		// Converts CapData to CapDef removes stop if encountered conversion
		// error
		TitanOperationStatus error = null;
		List<List<Def>> defList = new ArrayList<>();
		for (int i = 0; i < ownerCapDataList.size(); i++) {
			ImmutablePair<ComponentInstance, Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus>> immutablePair = ownerCapDataList.get(i);
			Either<List<Def>, TitanOperationStatus> convertCapDataListToCapDefList = convertor.convertDataToDefComponentInstance(immutablePair.left, immutablePair.right.left().value());
			if (convertCapDataListToCapDefList.isRight()) {
				error = convertCapDataListToCapDefList.right().value();
				break;
			} else {
				defList.add(convertCapDataListToCapDefList.left().value());
			}

		}
		Either<List<List<Def>>, TitanOperationStatus> eitherResult = (error != null) ? Either.right(error) : Either.left(defList);
		return eitherResult;

	}

	private Map<String, ComponentMetadataData> findLatestVersion(List<ComponentMetadataData> resourceDataList) {
		Map<Pair<String, String>, ComponentMetadataData> latestVersionMap = new HashMap<Pair<String, String>, ComponentMetadataData>();
		for (ComponentMetadataData resourceData : resourceDataList) {
			ComponentMetadataData latestVersionData = resourceData;

			ComponentMetadataDataDefinition metadataDataDefinition = resourceData.getMetadataDataDefinition();
			Pair<String, String> pair = createKeyPair(latestVersionData);
			if (latestVersionMap.containsKey(pair)) {
				latestVersionData = latestVersionMap.get(pair);
				String currentVersion = latestVersionData.getMetadataDataDefinition().getVersion();
				String newVersion = metadataDataDefinition.getVersion();
				if (CommonBeUtils.compareAsdcComponentVersions(newVersion, currentVersion)) {
					latestVersionData = resourceData;
				}
			}
			if (log.isDebugEnabled())
				log.debug("last certified version of resource = {}  version is {}", latestVersionData.getMetadataDataDefinition().getName(), latestVersionData.getMetadataDataDefinition().getVersion());

			latestVersionMap.put(pair, latestVersionData);
		}

		Map<String, ComponentMetadataData> resVersionMap = new HashMap<String, ComponentMetadataData>();
		for (ComponentMetadataData resourceData : latestVersionMap.values()) {
			ComponentMetadataData latestVersionData = resourceData;
			ComponentMetadataDataDefinition metadataDataDefinition = resourceData.getMetadataDataDefinition();
			if (resVersionMap.containsKey(metadataDataDefinition.getUUID())) {
				latestVersionData = resVersionMap.get(metadataDataDefinition.getUUID());
				String currentVersion = latestVersionData.getMetadataDataDefinition().getVersion();
				String newVersion = metadataDataDefinition.getVersion();
				if (CommonBeUtils.compareAsdcComponentVersions(newVersion, currentVersion)) {
					latestVersionData = resourceData;
				}
			}
			if (log.isDebugEnabled())
				log.debug("last uuid version of resource = {}  version is {}", latestVersionData.getMetadataDataDefinition().getName(), latestVersionData.getMetadataDataDefinition().getVersion());
			resVersionMap.put(latestVersionData.getMetadataDataDefinition().getUUID(), latestVersionData);
		}

		return resVersionMap;
	}

	private Pair<String, String> createKeyPair(ComponentMetadataData metadataData) {
		Pair<String, String> pair = null;
		NodeTypeEnum label = NodeTypeEnum.getByName(metadataData.getLabel());
		switch (label) {
		case Resource:
			pair = new ImmutablePair<String, String>(metadataData.getMetadataDataDefinition().getName(), ((ResourceMetadataDataDefinition) metadataData.getMetadataDataDefinition()).getResourceType().name());
			break;
		default:
			pair = new ImmutablePair<String, String>(metadataData.getMetadataDataDefinition().getName(), metadataData.getLabel());
			break;
		}

		return pair;
	}

	public Either<Collection<ComponentMetadataData>, StorageOperationStatus> getLatestVersionNotAbstractComponentsMetadataOnly(boolean isAbstract, Boolean isHighest, ComponentTypeEnum componentTypeEnum, String internalComponentType) {
		try {

			// Map<String, Object> hasPpropertiesToMatch = new HashMap<>();
			// Map<String, Object> hasNotPpropertiesToMatch = new HashMap<>();
			List<ImmutableTriple<QueryType, String, Object>> properties = new ArrayList<>();
			if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE)) {
				// hasPpropertiesToMatch.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(),
				// isAbstract);
				properties.add(new ImmutableTriple<>(QueryType.HAS, GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), isAbstract));

				if (internalComponentType != null) {
					switch (internalComponentType.toLowerCase()) {
					case "vf":
						properties.add(new ImmutableTriple<>(QueryType.HAS_NOT, GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name()));
//						properties.add(new ImmutableTriple<>(QueryType.HAS_NOT, GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VL.name()));
						// hasNotPpropertiesToMatch.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(),
						// ResourceTypeEnum.VF.name());
						break;
					case "service":
						properties.add(new ImmutableTriple<>(QueryType.HAS_NOT, GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VFC.name()));
						properties.add(new ImmutableTriple<>(QueryType.HAS_NOT, GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VFCMT.name()));
//						properties.add(new ImmutableTriple<>(QueryType.HAS_NOT, GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VL.name()));
						// hasNotPpropertiesToMatch.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(),
						// ResourceTypeEnum.VFC.name());
						break;
					case "vl":
						properties.add(new ImmutableTriple<>(QueryType.HAS, GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VL.name()));
						// hasPpropertiesToMatch.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(),
						// ResourceTypeEnum.VL.name());
						break;
					default:
						break;
					}
				}
			}
			// hasNotPpropertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(),
			// LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
			properties.add(new ImmutableTriple<>(QueryType.HAS_NOT, GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name()));
			// hasNotPpropertiesToMatch.put(GraphPropertiesDictionary.IS_DELETED.getProperty(),
			// true);
			properties.add(new ImmutableTriple<>(QueryType.HAS_NOT, GraphPropertiesDictionary.IS_DELETED.getProperty(), true));
			// Either<List<ComponentMetadataData>, TitanOperationStatus>
			// resourceNodes = titanGenericDao.getByCriteria(
			// componentTypeEnum.getNodeType(), hasPpropertiesToMatch,
			// hasNotPpropertiesToMatch,
			// ComponentMetadataData.class);
			Either<List<ComponentMetadataData>, TitanOperationStatus> resourceNodes = titanGenericDao.getByCriteria(componentTypeEnum.getNodeType(), ComponentMetadataData.class, properties);
			if (resourceNodes.isRight()) {
				// in case of NOT_FOUND from Titan client return to UI empty
				// list
				if (resourceNodes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					return Either.left(new ArrayList<>());
				} else {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resourceNodes.right().value()));
				}
			} else {
				List<ComponentMetadataData> resourceDataList = resourceNodes.left().value();
				Collection<ComponentMetadataData> resCollection = resourceDataList;
				if (isHighest != null && isHighest) {
					Map<String, ComponentMetadataData> latestVersionListMap = findLatestVersion(resourceDataList);
					resCollection = latestVersionListMap.values();
				}
				return Either.left(resCollection);
			}
		} finally {
			titanGenericDao.commit();
		}

	}

	public Either<List<Component>, StorageOperationStatus> getLatestVersionNotAbstractComponents(boolean isAbstract, Boolean isHighest, ComponentTypeEnum componentTypeEnum, String internalComponentType, List<String> componentUids) {
		try {
			List<Component> result = new ArrayList<>();
			Map<String, ResourceTypeEnum> componentUidsMap = new HashMap<>();
			if (componentUids == null) {
				Either<Collection<ComponentMetadataData>, StorageOperationStatus> resourceNodes = getLatestVersionNotAbstractComponentsMetadataOnly(isAbstract, isHighest, componentTypeEnum, internalComponentType);
				if (resourceNodes.isRight()) {
					return Either.right(resourceNodes.right().value());
				}
				Collection<ComponentMetadataData> collection = resourceNodes.left().value();

				if (collection == null) {
					componentUids = new ArrayList<>();
				} else {
					componentUids = collection.stream().map(p -> p.getMetadataDataDefinition().getUniqueId()).collect(Collectors.toList());
					// collection.forEach(p -> {
					// if (NodeTypeEnum.Resource.getName().equals(p.getLabel()))
					// {
					// componentUidsMap.put(p.getMetadataDataDefinition().getUniqueId(),
					// ((ResourceMetadataDataDefinition)
					// p.getMetadataDataDefinition()).getResourceType());
					// }
					// });

				}

			}
			if (false == componentUids.isEmpty()) {

				Manager manager = new Manager();
				int numberOfWorkers = 5;

				manager.init(numberOfWorkers);
				for (String componentUid : componentUids) {
					ComponentParametersView componentParametersView = buildComponentViewForNotAbstract();
					// ResourceTypeEnum type =
					// componentUidsMap.get(componentUid);
					// if (type != null && ResourceTypeEnum.VL.equals(type)) {
					if (internalComponentType != null && "vl".equalsIgnoreCase(internalComponentType)) {
						componentParametersView.setIgnoreCapabilities(false);
						componentParametersView.setIgnoreRequirements(false);
					}
					manager.addJob(new Job() {
						@Override
						public Either<Component, StorageOperationStatus> doWork() {
							Either<Component, StorageOperationStatus> component = getComponent(componentUid, componentParametersView, false);
							return component;
						}
					});
				}
				LinkedBlockingQueue<Either<Component, StorageOperationStatus>> res = manager.start();

				for (Either<Component, StorageOperationStatus> resource : res) {
					if (resource == null) {
						if (log.isDebugEnabled())
							log.debug("Failed to fetch resource returned null ");
						return Either.right(StorageOperationStatus.GENERAL_ERROR);
					}
					if (resource.isRight()) {
						if (log.isDebugEnabled())
							log.debug("Failed to fetch resource for error is {}", resource.right().value());
						return Either.right(resource.right().value());
					}
					Component component = resource.left().value();
					component.setContactId(null);
					component.setCreationDate(null);
					component.setCreatorUserId(null);
					component.setCreatorFullName(null);
					component.setLastUpdateDate(null);
					component.setLastUpdaterUserId(null);
					component.setLastUpdaterFullName(null);
					component.setNormalizedName(null);
					result.add(resource.left().value());
				}

				if (componentUids.size() != result.size()) {
					if (log.isDebugEnabled())
						log.debug("one of the workers failed to complete job ");
					return Either.right(StorageOperationStatus.GENERAL_ERROR);
				}
			}

			return Either.left(result);

		} finally {
			titanGenericDao.commit();
		}
	}

	private ComponentParametersView buildComponentViewForNotAbstract() {
		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		// componentParametersView.setIgnoreRequirements(false);
		// componentParametersView.setIgnoreCapabilities(false);
		componentParametersView.setIgnoreCategories(false);
		componentParametersView.setIgnoreAllVersions(false);
		componentParametersView.setIgnoreAllVersions(false);
		return componentParametersView;
	}

	protected TitanOperationStatus setCapabilitiesFromGraph(String uniqueId, Component component, NodeTypeEnum nodeType) {
		TitanOperationStatus titanStatus;
		Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> eitherCapabilities = getCapabilities(component, nodeType, true);
		if (eitherCapabilities.isLeft()) {
			titanStatus = TitanOperationStatus.OK;
			Map<String, List<CapabilityDefinition>> capabilities = eitherCapabilities.left().value();
			if (capabilities != null && !capabilities.isEmpty()) {
				component.setCapabilities(capabilities);
			}
		} else {
			titanStatus = eitherCapabilities.right().value();
		}
		return titanStatus;
	}

	protected TitanOperationStatus setRequirementsFromGraph(String uniqueId, Component component, NodeTypeEnum nodeType) {
		TitanOperationStatus status;
		Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> eitherRequirements = getRequirements(component, nodeType, false);
		if (eitherRequirements.isLeft()) {
			status = TitanOperationStatus.OK;
			Map<String, List<RequirementDefinition>> requirements = eitherRequirements.left().value();
			if (requirements != null && !requirements.isEmpty()) {
				component.setRequirements(requirements);
			}
		} else {
			status = eitherRequirements.right().value();
		}
		return status;
	}

	protected boolean isComponentExist(String componentId, NodeTypeEnum nodeType) {
		boolean result = true;
		Either<TitanVertex, TitanOperationStatus> compVertex = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId);
		if (compVertex.isRight()) {
			log.debug("failed to fetch vertex of component data for id {}", componentId);
			result = false;

		}
		return result;
	}

	<T> Either<T, StorageOperationStatus> getLightComponent(String id, NodeTypeEnum nodeType, boolean inTransaction) {

		T component = null;
		try {
			log.debug("Starting to build light component of type {}, id {}", nodeType, id);
			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
			}
			TitanGraph titanGraph = graphResult.left().value();
			Iterable<TitanVertex> vertecies = titanGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), id).vertices();
			if (vertecies != null) {
				Iterator<TitanVertex> iterator = vertecies.iterator();
				if (iterator != null && iterator.hasNext()) {
					Vertex vertex = iterator.next();
					Map<String, Object> resourceProperties = titanGenericDao.getProperties(vertex);
					ComponentMetadataData componentMetadataData = GraphElementFactory.createElement(nodeType.getName(), GraphElementTypeEnum.Node, resourceProperties, ComponentMetadataData.class);
					component = (T) convertComponentMetadataDataToComponent(componentMetadataData);

					// get creator
					Iterator<Edge> iterCreator = vertex.edges(Direction.IN, GraphEdgeLabels.CREATOR.name());
					if (iterCreator.hasNext() == false) {
						log.debug("no creator was defined for component {}", id);
						return Either.right(StorageOperationStatus.GENERAL_ERROR);
					}
					Vertex vertexCreator = iterCreator.next().outVertex();
					UserData creator = GraphElementFactory.createElement(NodeTypeEnum.User.getName(), GraphElementTypeEnum.Node, titanGenericDao.getProperties(vertexCreator), UserData.class);
					log.debug("Build component : set creator userId to {}", creator.getUserId());
					String fullName = buildFullName(creator);
					log.debug("Build component : set creator full name to {}", fullName);
					((Component) component).setCreatorUserId(creator.getUserId());
					((Component) component).setCreatorFullName(fullName);

					// get modifier
					Iterator<Edge> iterModifier = vertex.edges(Direction.IN, GraphEdgeLabels.LAST_MODIFIER.name());

					if (iterModifier.hasNext() == false) {
						log.debug("no modifier was defined for component {}", id);
						return Either.right(StorageOperationStatus.GENERAL_ERROR);
					}
					Vertex vertexModifier = iterModifier.next().outVertex();
					UserData modifier = GraphElementFactory.createElement(NodeTypeEnum.User.getName(), GraphElementTypeEnum.Node, titanGenericDao.getProperties(vertexModifier), UserData.class);
					log.debug("Build component : set last modifier userId to {}", creator.getUserId());
					fullName = buildFullName(modifier);
					log.debug("Build component : set last modifier full name to {}", fullName);
					((Component) component).setLastUpdaterUserId(modifier.getUserId());
					((Component) component).setLastUpdaterFullName(fullName);

					// get category
					TitanOperationStatus status = setComponentCategoriesFromGraph((Component) component);
					if (status != TitanOperationStatus.OK) {
						return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					}
				} else {
					// Nothing found
					log.debug("Component with id {} not found", id);
					return Either.right(StorageOperationStatus.NOT_FOUND);
				}
			} else {
				// Nothing found
				log.debug("Component with id {} not found", id);
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}
			log.debug("Ended to build light component of type {}, id {}", nodeType, id);
			return Either.left(component);
		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}
	}

	Either<Component, StorageOperationStatus> getMetadataComponent(String id, NodeTypeEnum nodeType, boolean inTransaction) {
		Component component = null;
		try {
			log.debug("Starting to build metadata component of type {}, id {}", nodeType, id);
			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
			}
			TitanGraph titanGraph = graphResult.left().value();
			Iterable<TitanVertex> vertecies = titanGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), id).vertices();
			if (vertecies != null) {
				Iterator<TitanVertex> iterator = vertecies.iterator();
				if (iterator != null && iterator.hasNext()) {
					Vertex vertex = iterator.next();
					Map<String, Object> resourceProperties = titanGenericDao.getProperties(vertex);
					ComponentMetadataData componentMetadataData = GraphElementFactory.createElement(nodeType.getName(), GraphElementTypeEnum.Node, resourceProperties, ComponentMetadataData.class);
					component = convertComponentMetadataDataToComponent(componentMetadataData);
				} else {
					// Nothing found
					log.debug("Component with id {} not found", id);
					return Either.right(StorageOperationStatus.NOT_FOUND);
				}
			} else {
				// Nothing found
				log.debug("Component with id {} not found", id);
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}
			log.debug("Ended to build metadata component of type {}, id {}", nodeType, id);
			return Either.left(component);
		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}
	}

	public Either<Integer, StorageOperationStatus> getComponentInstanceCoutner(String origServiceId, NodeTypeEnum nodeType) {
		Either<Integer, StorageOperationStatus> result;
		Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
		if (graphResult.isRight()) {
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
			return result;
		}
		Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), origServiceId);
		if (vertexService.isRight()) {
			log.debug("failed to fetch vertex of component metadata, nodeType:{} , id: {}", nodeType, origServiceId);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexService.right().value()));
			return result;
		}
		Vertex vertex = vertexService.left().value();
		Integer instanceCounter = vertex.value(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty());
		return Either.left(instanceCounter);
	}

	protected TitanOperationStatus setComponentInstancesPropertiesFromGraph(Component component) {

		List<ComponentInstance> resourceInstances = component.getComponentInstances();

		Map<String, List<ComponentInstanceProperty>> resourceInstancesProperties = new HashMap<>();

		Map<String, List<PropertyDefinition>> alreadyProcessedResources = new HashMap<>();

		Map<String, List<ComponentInstanceProperty>> alreadyProcessedInstances = new HashMap<>();

		Map<String, ImmutablePair<ComponentInstance, Integer>> processedInstances = new HashMap<>();

		if (resourceInstances != null) {

			for (ComponentInstance resourceInstance : resourceInstances) {

				List<String> path = new ArrayList<>();
				path.add(resourceInstance.getUniqueId());
				Either<List<ComponentInstanceProperty>, TitanOperationStatus> componentInstanceProperties = componentInstanceOperation.getComponentInstanceProperties(resourceInstance, alreadyProcessedResources, alreadyProcessedInstances,
						processedInstances, path);

				if (componentInstanceProperties.isRight()) {
					TitanOperationStatus status = componentInstanceProperties.right().value();
					if (status != TitanOperationStatus.OK) {
						return status;
					}
				}

				List<ComponentInstanceProperty> listOfProps = componentInstanceProperties.left().value();
				String resourceInstanceUid = resourceInstance.getUniqueId();
				resourceInstancesProperties.put(resourceInstanceUid, listOfProps);

				// alreadyProcessedInstances.put(resourceInstance.getUniqueId(),
				// resourceInstance);

				processedInstances.put(resourceInstance.getUniqueId(), new ImmutablePair<ComponentInstance, Integer>(resourceInstance, path.size()));
				path.remove(path.size() - 1);

			}

		}

		Either<Map<String, Map<String, ComponentInstanceProperty>>, TitanOperationStatus> findAllPropertiesValuesOnInstances = componentInstanceOperation.findAllPropertyValueOnInstances(processedInstances);
		// 1. check status
		if (findAllPropertiesValuesOnInstances.isRight()) {
			TitanOperationStatus status = findAllPropertiesValuesOnInstances.right().value();
			if (status != TitanOperationStatus.OK) {
				return status;
			}
		}
		// 2. merge data from rules on properties (resourceInstancesProperties)
		propertyOperation.updatePropertiesByPropertyValues(resourceInstancesProperties, findAllPropertiesValuesOnInstances.left().value());

		component.setComponentInstancesProperties(resourceInstancesProperties);

		return TitanOperationStatus.OK;
	}
	
	protected TitanOperationStatus setComponentInstancesInputsFromGraph(String uniqueId, Component component) {

		Map<String, List<ComponentInstanceInput>> resourceInstancesInputs = new HashMap<>();
		TitanOperationStatus status = TitanOperationStatus.OK;
		List<ComponentInstance> componentInstances = component.getComponentInstances();
		if (componentInstances != null) {
			for (ComponentInstance resourceInstance : componentInstances) {
				Either<List<ComponentInstanceInput>, TitanOperationStatus> eitherRIAttributes = inputOperation.getAllInputsOfResourceInstance(resourceInstance);
				if (eitherRIAttributes.isRight()) {
					if (eitherRIAttributes.right().value() != TitanOperationStatus.NOT_FOUND) {
						status = eitherRIAttributes.right().value();
						break;
					}
				} else {
					resourceInstancesInputs.put(resourceInstance.getUniqueId(), eitherRIAttributes.left().value());
				}
			}
			if (!resourceInstancesInputs.isEmpty())
				component.setComponentInstancesInputs(resourceInstancesInputs);
		}

		return status;
	}

	public Either<String, StorageOperationStatus> getInvariantUUID(NodeTypeEnum nodeType, String componentId, boolean inTransaction) {
		Either<String, StorageOperationStatus> res = null;
		try {
			Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId);
			if (vertexByProperty.isRight()) {
				TitanOperationStatus status = vertexByProperty.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				res = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				Vertex v = vertexByProperty.left().value();
				String invariantUUID = v.value(GraphPropertiesDictionary.INVARIANT_UUID.getProperty());

				if (invariantUUID == null || invariantUUID.isEmpty()) {

					log.info("The component {} has empty invariant UUID.", componentId);
					res = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.INVALID_ELEMENT));

				}
				res = Either.left(invariantUUID);
			}
		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}
		return res;
	}

	protected TitanOperationStatus setGroupsFromGraph(String uniqueId, Component component, NodeTypeEnum nodeTypeEnum) {

		Either<List<GroupDefinition>, TitanOperationStatus> res = groupOperation.getAllGroupsFromGraph(uniqueId, nodeTypeEnum);
		if (res.isRight()) {
			TitanOperationStatus status = res.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				return TitanOperationStatus.OK;
			} else {
				return status;
			}
		}
		component.setGroups(res.left().value());

		return TitanOperationStatus.OK;

	}

	protected TitanOperationStatus setComponentInputsFromGraph(String uniqueId, Component component, boolean inTransaction) {

		List<InputDefinition> inputs = new ArrayList<>();
		TitanOperationStatus status = inputsOperation.findAllResourceInputs(uniqueId, inputs);
		if (status == TitanOperationStatus.OK) {
			component.setInputs(inputs);
		}

		return status;

	}

	protected StorageOperationStatus deleteGroups(NodeTypeEnum nodeType, String componentId) {

		Either<List<GroupDefinition>, StorageOperationStatus> deleteRes = groupOperation.deleteAllGroups(componentId, nodeType, true);

		if (deleteRes.isRight()) {
			StorageOperationStatus status = deleteRes.right().value();
			return status;
		}

		return StorageOperationStatus.OK;

	}

	protected StorageOperationStatus removeInputsFromComponent(NodeTypeEnum typeEnum, Component component) {
		Either<Map<String, InputDefinition>, StorageOperationStatus> deleteAllInputsAssociatedToNode = inputsOperation.deleteAllInputsAssociatedToNode(typeEnum, component.getUniqueId());
		return deleteAllInputsAssociatedToNode.isRight() ? deleteAllInputsAssociatedToNode.right().value() : StorageOperationStatus.OK;
	}

	protected TitanOperationStatus associateInputsToComponent(NodeTypeEnum nodeType, ComponentMetadataData resourceData, List<InputDefinition> properties) {

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			log.debug("Cannot find any data type. Status is {}.", status);
			return status;
		}

		Map<String, InputDefinition> convertedProperties = new HashMap<>();

		if (properties != null) {
			for (InputDefinition propertyDefinition : properties) {
				convertedProperties.put(propertyDefinition.getName(), propertyDefinition);
			}

			Either<List<InputDefinition>, TitanOperationStatus> operationStatus = inputsOperation.addInputsToGraph(resourceData.getMetadataDataDefinition().getUniqueId(), nodeType, convertedProperties, allDataTypes.left().value());
			if (operationStatus.isLeft())
				return TitanOperationStatus.OK;
			else
				return operationStatus.right().value();
		}

		return TitanOperationStatus.OK;

	}

	protected TitanOperationStatus associateInputsToComponent(TitanVertex metadataVertex, String componentId, List<InputDefinition> properties) {

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			log.debug("Cannot find any data type. Status is {}.", status);
			return status;
		}

		Map<String, InputDefinition> convertedProperties = new HashMap<>();

		if (properties != null) {
			for (InputDefinition propertyDefinition : properties) {
				convertedProperties.put(propertyDefinition.getName(), propertyDefinition);
			}

			return inputsOperation.addInputsToGraph(metadataVertex, componentId, convertedProperties, allDataTypes.left().value());
		}

		return TitanOperationStatus.OK;

	}

	public Either<List<ComponentInstance>, StorageOperationStatus> getAllComponentInstncesMetadata(String componentId, NodeTypeEnum nodeType) {
		Instant start = Instant.now();
		Either<List<ComponentInstance>, StorageOperationStatus> resourceInstancesOfService = componentInstanceOperation.getAllComponentInstancesMetadataOnly(componentId, nodeType);
		Instant end = Instant.now();
		log.debug("TOTAL TIME BL GET INSTANCES: {}", Duration.between(start, end));
		return resourceInstancesOfService;
	}

	@Deprecated
	public Either<List<Component>, ActionStatus> getComponentsFromCacheForCatalog(Set<String> components, ComponentTypeEnum componentType) {

		Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> componentsForCatalog = componentCache.getComponentsForCatalog(components, componentType);
		if (componentsForCatalog.isLeft()) {
			ImmutableTriple<List<Component>, List<Component>, Set<String>> immutableTriple = componentsForCatalog.left().value();
			List<Component> foundComponents = immutableTriple.getLeft();

			if (foundComponents != null) {
				// foundComponents.forEach(p -> result.add((Resource)p));
				log.debug("The number of {}s added to catalog from cache is {}", componentType.name().toLowerCase(), foundComponents.size());

			}
			List<Component> foundDirtyComponents = immutableTriple.getMiddle();
			Set<String> nonCachedComponents = immutableTriple.getRight();
			int numberDirtyResources = foundDirtyComponents == null ? 0 : foundDirtyComponents.size();
			int numberNonCached = nonCachedComponents == null ? 0 : nonCachedComponents.size();
			log.debug("The number of left {}s for catalog is {}", componentType.name().toLowerCase(), numberDirtyResources + numberNonCached);
			return Either.left(foundComponents);
		}

		return Either.right(componentsForCatalog.right().value());
	}

	public <T extends ComponentMetadataData> Either<List<T>, TitanOperationStatus> getListOfHighestComponents(NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		long startFetchAllStates = System.currentTimeMillis();
		Map<String, Object> propertiesToMatchHigest = new HashMap<>();
		propertiesToMatchHigest.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
		Either<List<T>, TitanOperationStatus> allHighestStates = titanGenericDao.getByCriteria(nodeTypeEnum, propertiesToMatchHigest, clazz);
		if (allHighestStates.isRight() && allHighestStates.right().value() != TitanOperationStatus.NOT_FOUND) {
			return Either.right(allHighestStates.right().value());
		}
		long endFetchAllStates = System.currentTimeMillis();

		if (allHighestStates.isRight()) {
			return Either.left(new ArrayList<>());
		}
		List<T> services = allHighestStates.left().value();

		List<T> certifiedHighest = new ArrayList<>();
		List<T> notCertifiedHighest = new ArrayList<>();
		for (T reData : services) {
			if (reData.getMetadataDataDefinition().getState().equals(LifecycleStateEnum.CERTIFIED.name())) {
				certifiedHighest.add(reData);
			} else {
				notCertifiedHighest.add(reData);
			}
		}

		log.debug("Fetch catalog {}s all states: certified {}, noncertified {}", nodeTypeEnum.getName(), certifiedHighest.size(), notCertifiedHighest.size());
		log.debug("Fetch catalog {}s all states from graph took {} ms", nodeTypeEnum.getName(), endFetchAllStates - startFetchAllStates);

		HashMap<String, String> serviceNames = new HashMap<>();
		for (T data : notCertifiedHighest) {
			String serviceName = data.getMetadataDataDefinition().getName();
			serviceNames.put(serviceName, serviceName);
		}

		for (T data : certifiedHighest) {
			String serviceName = data.getMetadataDataDefinition().getName();
			if (!serviceNames.containsKey(serviceName)) {
				notCertifiedHighest.add(data);
			}
		}

		return Either.left(notCertifiedHighest);
	}

	protected <T extends Component> Either<T, ActionStatus> getComponentFromCacheIfUpToDate(String uniqueId, ComponentMetadataData componentMetadataData, ComponentParametersView componentParametersView, Class<T> clazz,
			ComponentTypeEnum componentTypeEnum) {

		long start = System.currentTimeMillis();
		try {

			long lastModificationTime = componentMetadataData.getMetadataDataDefinition().getLastUpdateDate();
			Either<Component, ActionStatus> cacheComponentRes = this.componentCache.getComponent(uniqueId, lastModificationTime);
			if (cacheComponentRes.isLeft()) {
				Component cachedComponent = cacheComponentRes.left().value();

				// Must calculate allVersions
				if (false == componentParametersView.isIgnoreAllVersions()) {
					Class<? extends ComponentMetadataData> clazz1 = null;
					switch (componentTypeEnum) {
					case RESOURCE:
						clazz1 = ResourceMetadataData.class;
						break;
					case SERVICE:
						clazz1 = ServiceMetadataData.class;
						break;
					case PRODUCT:
						clazz1 = ProductMetadataData.class;
						break;
					default:
						break;
					}
					if (clazz1 != null) {
						Either<Map<String, String>, TitanOperationStatus> versionList = getVersionList(componentTypeEnum.getNodeType(), cachedComponent.getVersion(), cachedComponent.getUUID(), cachedComponent.getSystemName(), clazz1);
						if (versionList.isRight()) {
							return Either.right(ActionStatus.GENERAL_ERROR);
						}

						Map<String, String> allVersions = versionList.left().value();
						cachedComponent.setAllVersions(allVersions);
					} else {
						return Either.right(ActionStatus.GENERAL_ERROR);
					}
				}
				if (componentParametersView != null) {
					cachedComponent = componentParametersView.filter(cachedComponent, componentTypeEnum);
				}
				return Either.left(clazz.cast(cachedComponent));
			}

			return Either.right(cacheComponentRes.right().value());

		} finally {
			log.trace("Fetch component {} with uid {} from cache took {} ms", componentTypeEnum.name().toLowerCase(), uniqueId, System.currentTimeMillis() - start);
		}
	}

	public Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> getComponentsFromCacheForCatalog(Map<String, Long> components, ComponentTypeEnum componentType) {

		Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> componentsForCatalog = componentCache.getComponentsForCatalog(components, componentType);
		if (componentsForCatalog.isLeft()) {
			ImmutablePair<List<Component>, Set<String>> immutablePair = componentsForCatalog.left().value();
			List<Component> foundComponents = immutablePair.getLeft();

			if (foundComponents != null) {
				// foundComponents.forEach(p -> result.add((Resource)p));
				log.debug("The number of {}s added to catalog from cache is {}", componentType.name().toLowerCase(), foundComponents.size());
			}
			Set<String> leftComponents = immutablePair.getRight();
			int numberNonCached = leftComponents == null ? 0 : leftComponents.size();
			log.debug("The number of left {}s for catalog is {}", componentType.name().toLowerCase(), numberNonCached);

			ImmutablePair<List<Component>, Set<String>> result = new ImmutablePair<List<Component>, Set<String>>(foundComponents, leftComponents);
			return Either.left(result);
		}

		return Either.right(componentsForCatalog.right().value());
	}

	/**
	 * 
	 * @param component
	 * @param inTransaction
	 * @param titanGenericDao
	 * @param clazz
	 * @return
	 */
	public <T> Either<T, StorageOperationStatus> updateComponentFilterResult(Component component, boolean inTransaction, TitanGenericDao titanGenericDao, Class<T> clazz, NodeTypeEnum type, ComponentParametersView filterResult) {
		Either<T, StorageOperationStatus> result = null;

		try {

			log.debug("In updateComponent. received component uid = {}", (component == null ? null : component.getUniqueId()));
			if (component == null) {
				log.error("Service object is null");
				result = Either.right(StorageOperationStatus.BAD_REQUEST);
				return result;
			}

			ComponentMetadataData componentData = getMetaDataFromComponent(component);

			log.debug("After converting component to componentData. ComponentData = {}", componentData);

			if (componentData.getUniqueId() == null) {
				log.error("Resource id is missing in the request.");
				return Either.right(StorageOperationStatus.BAD_REQUEST);
			}

			Either<Integer, StorageOperationStatus> counterStatus = this.getComponentInstanceCoutner(component.getUniqueId(), component.getComponentType().getNodeType());

			if (counterStatus.isRight()) {

				log.error("Cannot find componentInstanceCounter for component {} in the graph. status is {}", componentData.getUniqueId(), counterStatus);
				// result = sendError(status,
				// StorageOperationStatus.USER_NOT_FOUND);
				return result;
			}

			componentData.setComponentInstanceCounter(counterStatus.left().value());

			String modifierUserId = component.getLastUpdaterUserId();
			if (modifierUserId == null || modifierUserId.isEmpty()) {
				log.error("UserId is missing in the request.");
				result = Either.right(StorageOperationStatus.BAD_REQUEST);
				return result;
			}
			Either<UserData, TitanOperationStatus> findUser = findUser(modifierUserId);

			if (findUser.isRight()) {
				TitanOperationStatus status = findUser.right().value();
				log.error("Cannot find user {} in the graph. status is {}", modifierUserId, status);
				// result = sendError(status,
				// StorageOperationStatus.USER_NOT_FOUND);
				return result;
			}

			UserData modifierUserData = findUser.left().value();
			String resourceId = component.getUniqueId();

			ComponentParametersView componentParametersView = new ComponentParametersView();
			componentParametersView.disableAll();
			componentParametersView.setIgnoreUsers(false);
			componentParametersView.setIgnoreCategories(false);
			componentParametersView.setIgnoreDerivedFrom(false);
			componentParametersView.setIgnoreArtifacts(false);
			Either<T, StorageOperationStatus> currentComponentResult = this.getComponent(resourceId, componentParametersView, inTransaction);
			if (currentComponentResult.isRight()) {
				log.error("Cannot find resource with id {} in the graph.", resourceId);
				result = Either.right(currentComponentResult.right().value());
				return result;
			}

			Component currentComponent = (Component) currentComponentResult.left().value();
			String currentModifier = currentComponent.getLastUpdaterUserId();

			if (currentModifier.equals(modifierUserData.getUniqueId())) {
				log.debug("Graph LAST MODIFIER edge should not be changed since the modifier is the same as the last modifier.");
			} else {
				log.debug("Going to update the last modifier user of the resource from {} to {}", currentModifier, modifierUserId);
				StorageOperationStatus status = moveLastModifierEdge(component, componentData, modifierUserData, type);
				log.debug("Finish to update the last modifier user of the resource from {} to {}. status is {}", currentModifier, modifierUserId, status);
				if (status != StorageOperationStatus.OK) {
					result = Either.right(status);
					return result;
				}
			}
			final long currentTimeMillis = System.currentTimeMillis();
			log.debug("Going to update the last Update Date of the resource from {} to {}", component.getLastUpdateDate(), currentTimeMillis);
			component.setLastUpdateDate(currentTimeMillis);

			StorageOperationStatus checkCategories = validateCategories(currentComponent, component, componentData, type);
			if (checkCategories != StorageOperationStatus.OK) {
				result = Either.right(checkCategories);
				return result;
			}

			List<String> tags = component.getTags();
			if (tags != null && false == tags.isEmpty()) {
				Either<List<TagData>, StorageOperationStatus> tagsResult = createNewTagsList(tags);
				if (tagsResult.isRight()) {
					result = Either.right(tagsResult.right().value());
					return result;
				}
				List<TagData> tagsToCreate = tagsResult.left().value();
				if (tagsToCreate != null && !tagsToCreate.isEmpty()) {
					tagsToCreate = ImmutableSet.copyOf(tagsToCreate).asList();
					for (TagData tagData : tagsToCreate) {
						log.debug("Before creating tag {}", tagData);
						Either<TagData, TitanOperationStatus> createTagResult = titanGenericDao.createNode(tagData, TagData.class);
						if (createTagResult.isRight()) {
							TitanOperationStatus status = createTagResult.right().value();
							log.error("Cannot find tag {} in the graph. status is {}", tagData, status);
							result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
							return result;
						}
						log.debug("After creating tag {}", tagData);
					}
				}
			}

			Either<ComponentMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(componentData, ComponentMetadataData.class);

			if (updateNode.isRight()) {
				log.error("Failed to update resource {}. status is {}", component.getUniqueId(), updateNode.right().value());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
				return result;
			}

			ComponentMetadataData updatedResourceData = updateNode.left().value();
			log.debug("ComponentData After update is {}", updatedResourceData);

			// DE230195 in case resource name changed update TOSCA artifacts
			// file names accordingly
			String newSystemName = updatedResourceData.getMetadataDataDefinition().getSystemName();
			String prevSystemName = currentComponent.getSystemName();
			if (newSystemName != null && !newSystemName.equals(prevSystemName)) {
				Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
				if (toscaArtifacts != null) {
					for (Entry<String, ArtifactDefinition> artifact : toscaArtifacts.entrySet()) {
						Either<ArtifactData, StorageOperationStatus> updateName = generateAndUpdateToscaFileName(component.getComponentType().getValue().toLowerCase(), newSystemName, updatedResourceData.getMetadataDataDefinition().getUniqueId(),
								type, artifact.getValue());
						if (updateName.isRight()) {
							result = Either.right(updateName.right().value());
							return result;
						}
					}
				}
				//TODO call to new Artifact operation in order to update list of artifacts 
				
		     //US833308 VLI in service - specific network_role property value logic
				if (ComponentTypeEnum.SERVICE == component.getComponentType()) {
					//update method logs success/error and returns boolean (true if nothing fails)
					updateServiceNameInVLIsNetworkRolePropertyValues(component, prevSystemName, newSystemName);
				}
			}
			

			if (component.getComponentType().equals(ComponentTypeEnum.RESOURCE)) {
				updateDerived(component, currentComponent, componentData, component.getClass());
			}

			Either<T, StorageOperationStatus> updatedResource = getComponent(component.getUniqueId(), filterResult, inTransaction);
			if (updatedResource.isRight()) {
				log.error("Resource id is missing in the request. status is {}", updatedResource.right().value());
				result = Either.right(StorageOperationStatus.BAD_REQUEST);
				return result;
			}

			T updatedResourceValue = updatedResource.left().value();
			result = Either.left(updatedResourceValue);

			return result;
		} finally {

			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("updateComponent operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("updateComponent operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}
	
	private boolean updateServiceNameInVLIsNetworkRolePropertyValues (Component component, String prevSystemName, String newSystemName) {
		// find VLIs in service
		boolean res = true;
		if(null == component.getComponentInstances() || component.getComponentInstances().isEmpty()){
			return res;
		}
		
		List <ComponentInstance> vlInstances = 
				component.getComponentInstances().stream()
				.filter(p -> OriginTypeEnum.VL == p.getOriginType())
				.collect(Collectors.toList());
		if (!vlInstances.isEmpty()) {
			for (ComponentInstance vlInstance : vlInstances){
				// find network_role property 
				Optional <ComponentInstanceProperty> networkRoleProperty = component.getComponentInstancesProperties().get(vlInstance.getUniqueId()).stream()
						.filter(p -> PropertyNames.NETWORK_ROLE.getPropertyName().equalsIgnoreCase(p.getName()))
						.findAny();	
				res = res && updateNetworkRolePropertyValue(prevSystemName, newSystemName, vlInstance, networkRoleProperty);		
			}	
		}
		return res;	
	}

	private boolean updateNetworkRolePropertyValue(String prevSystemName, String newSystemName, ComponentInstance vlInstance, Optional<ComponentInstanceProperty> networkRoleProperty) {
		if (networkRoleProperty.isPresent() && !StringUtils.isEmpty(networkRoleProperty.get().getValue()) ) {
			ComponentInstanceProperty property = networkRoleProperty.get();
			String updatedValue = property.getValue().replaceFirst(prevSystemName, newSystemName);
			property.setValue(updatedValue);
			StorageOperationStatus updateCustomizationUUID;
			//disregard property value rule 
			property.setRules(null);
			Either<ComponentInstanceProperty, StorageOperationStatus> result = componentInstanceOperation.updatePropertyValueInResourceInstance(property, vlInstance.getUniqueId(), true);
			if (result.isLeft()) {
				log.debug("Property value {} was updated on graph.", property.getValueUniqueUid());
				updateCustomizationUUID = componentInstanceOperation.updateCustomizationUUID(vlInstance.getUniqueId());
			} else {
				updateCustomizationUUID = StorageOperationStatus.EXEUCTION_FAILED;
				log.debug("Failed to update property value: {} in resource instance {}", updatedValue, vlInstance.getUniqueId());
			}
			return result.isLeft() && StorageOperationStatus.OK == updateCustomizationUUID;
		}
		return true;
	}

	public Either<ComponentMetadataData, StorageOperationStatus> updateComponentLastUpdateDateAndLastModifierOnGraph( Component component, User modifier, NodeTypeEnum componentType, boolean inTransaction) {
		
		log.debug("Going to update last update date and last modifier info of component {}. ", component.getName());
		Either<ComponentMetadataData, StorageOperationStatus> result = null;
		try{
			String modifierUserId = modifier.getUserId();
			ComponentMetadataData componentData = getMetaDataFromComponent(component);
			String currentUser = component.getLastUpdaterUserId();
			UserData modifierUserData = new UserData();
			modifierUserData.setUserId(modifierUserId);
			if (currentUser.equals(modifierUserId)) {
				log.debug("Graph last modifier edge should not be changed since the modifier is the same as the last modifier.");
			} else {
				log.debug("Going to update the last modifier user of the component from {} to {}", currentUser, modifierUserId);
				StorageOperationStatus status = moveLastModifierEdge(component, componentData, modifierUserData, componentType);
				log.debug("Finish to update the last modifier user of the resource from {} to {}. status is {}", currentUser, modifierUserId, status);
				if (status != StorageOperationStatus.OK) {
					result = Either.right(status);
				}
			}
			Either<ComponentMetadataData, TitanOperationStatus> updateNode = null;
			if(result == null){
				log.debug("Going to update the component {} with new last update date. ", component.getName());
				componentData.getMetadataDataDefinition().setLastUpdateDate(System.currentTimeMillis());
				updateNode = titanGenericDao.updateNode(componentData, ComponentMetadataData.class);
				if (updateNode.isRight()) {
					log.error("Failed to update component {}. status is {}", component.getUniqueId(), updateNode.right().value());
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
				}
			}
			if(result == null){
				result = Either.left(updateNode.left().value());
			}
		}catch(Exception e){
			log.error("Exception occured during  update last update date and last modifier info of component {}. The message is {}. ", component.getName(), e.getMessage());
		}finally {
			if(!inTransaction){
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
		return result;
	}
	/**
	 * updates component lastUpdateDate on graph node
	 * @param component
	 * @param componentType
	 * @param lastUpdateDate
	 * @param inTransaction
	 * @return
	 */
	public Either<ComponentMetadataData, StorageOperationStatus> updateComponentLastUpdateDateOnGraph( Component component, NodeTypeEnum componentType, Long lastUpdateDate, boolean inTransaction) {
		
		log.debug("Going to update last update date of component {}. ", component.getName());
		Either<ComponentMetadataData, StorageOperationStatus> result = null;
		try{
			ComponentMetadataData componentData = getMetaDataFromComponent(component);
			Either<ComponentMetadataData, TitanOperationStatus> updateNode = null;
			if(result == null){
				log.debug("Going to update the component {} with new last update date. ", component.getName());
				componentData.getMetadataDataDefinition().setLastUpdateDate(lastUpdateDate);
				updateNode = titanGenericDao.updateNode(componentData, ComponentMetadataData.class);
				if (updateNode.isRight()) {
					log.error("Failed to update component {}. status is {}", component.getUniqueId(), updateNode.right().value());
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
				}
			}
			if(result == null){
				result = Either.left(updateNode.left().value());
			}
		}catch(Exception e){
			log.error("Exception occured during  update last update date of component {}. The message is {}. ", component.getName(), e.getMessage());
		}finally {
			if(!inTransaction){
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
		return result;
	}
}
