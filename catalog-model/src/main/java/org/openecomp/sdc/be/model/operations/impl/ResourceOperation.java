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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.ResourceMetadataDefinition;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.IAttributeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.migration.MigrationErrorInformer;
import org.openecomp.sdc.be.model.operations.utils.GraphDeleteUtil;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.TagData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.PairUtils;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.Function;
import fj.data.Either;

@org.springframework.stereotype.Component("resource-operation")
@Deprecated
public class ResourceOperation extends ComponentOperation implements IResourceOperation {

	public ResourceOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(ResourceOperation.class.getName());

	@javax.annotation.Resource
	private PropertyOperation propertyOperation;

	@javax.annotation.Resource
	private IAttributeOperation attributeOperation;

	@javax.annotation.Resource
	private RequirementOperation requirementOperation;

	@javax.annotation.Resource
	private CapabilityOperation capabilityOperation;

	@javax.annotation.Resource
	private InterfaceLifecycleOperation interfaceLifecycleOperation;

	@javax.annotation.Resource
	private IElementOperation elementOperation;

	@javax.annotation.Resource
	private IAdditionalInformationOperation addioAdditionalInformationOperation;

	@javax.annotation.Resource
	private GroupOperation groupOperation;

	@javax.annotation.Resource
	private ComponentCache componentCache;

	private Gson prettyJson = new GsonBuilder().setPrettyPrinting().create();

	private GraphDeleteUtil graphDeleteUtil = new GraphDeleteUtil();

	public static Pattern uuidNewVersion = Pattern.compile("^\\d{1,}.1");
	public static Pattern uuidNormativeNewVersion = Pattern.compile("^\\d{1,}.0");

	@Override
	public Either<Resource, StorageOperationStatus> createResource(Resource resource) {
		return createResource(resource, false);
	}

	@Override
	public Either<Resource, StorageOperationStatus> createResource(Resource resource, boolean inTransaction) {

		Either<Resource, StorageOperationStatus> result = null;

		try {
			generateUUID(resource);

			ResourceMetadataData resourceData = getResourceMetaDataFromResource(resource);
			String resourceUniqueId = resource.getUniqueId();
			if (resourceUniqueId == null) {
				resourceUniqueId = UniqueIdBuilder.buildResourceUniqueId();
				resourceData.getMetadataDataDefinition().setUniqueId(resourceUniqueId);
			}
			resourceData.getMetadataDataDefinition().setHighestVersion(true);

			String userId = resource.getCreatorUserId();

			Either<TitanVertex, TitanOperationStatus> findUser = findUserVertex(userId);

			if (findUser.isRight()) {
				TitanOperationStatus status = findUser.right().value();
				log.error("Cannot find user {} in the graph. status is {}", userId, status);
				return sendError(status, StorageOperationStatus.USER_NOT_FOUND);
			}

			TitanVertex creatorVertex = findUser.left().value();
			TitanVertex updaterVertex = creatorVertex;
			String updaterUserId = resource.getLastUpdaterUserId();
			if (updaterUserId != null && !updaterUserId.equals(userId)) {
				findUser = findUserVertex(updaterUserId);
				if (findUser.isRight()) {
					TitanOperationStatus status = findUser.right().value();
					log.error("Cannot find user {} in the graph. status is {}", userId, status);
					return sendError(status, StorageOperationStatus.USER_NOT_FOUND);
				} else {
					updaterVertex = findUser.left().value();
				}
			}

			// get derived from resources
			List<ResourceMetadataData> derivedResources = null;
			Either<List<ResourceMetadataData>, StorageOperationStatus> derivedResourcesResult = findDerivedResources(resource);
			if (derivedResourcesResult.isRight()) {
				result = Either.right(derivedResourcesResult.right().value());
				return result;
			} else {
				derivedResources = derivedResourcesResult.left().value();
			}

			List<String> tags = resource.getTags();
			if (tags != null && false == tags.isEmpty()) {
				Either<List<TagData>, StorageOperationStatus> tagsResult = createNewTagsList(tags);
				if (tagsResult.isRight()) {
					result = Either.right(tagsResult.right().value());
					return result;
				}
				List<TagData> tagsToCreate = tagsResult.left().value();
				StorageOperationStatus status = createTagNodesOnGraph(tagsToCreate);
				if (!status.equals(StorageOperationStatus.OK)) {
					result = Either.right(status);
					return result;
				}
			}

			Either<TitanVertex, TitanOperationStatus> createdVertex = titanGenericDao.createNode(resourceData);
			if (createdVertex.isRight()) {
				TitanOperationStatus status = createdVertex.right().value();
				log.error("Error returned after creating resource data node {}. status returned is ", resourceData, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			TitanVertex metadataVertex = createdVertex.left().value();

			TitanOperationStatus associateMetadata = associateMetadataToResource(resourceData, creatorVertex, updaterVertex, derivedResources, metadataVertex);
			if (associateMetadata != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateMetadata));
				return result;
			}
			StorageOperationStatus associateCategory = assosiateMetadataToCategory(resource, resourceData);
			if (associateCategory != StorageOperationStatus.OK) {
				result = Either.right(associateCategory);
				return result;
			}
			
			TitanOperationStatus associateProperties = associatePropertiesToResource(metadataVertex, resourceUniqueId, resource.getProperties(), derivedResources);
			if (associateProperties != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateProperties));
				return result;
			}

			TitanOperationStatus associateAttributes = associateAttributesToResource(metadataVertex, resource.getAttributes(), resourceUniqueId);
			if (associateAttributes != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateAttributes));
				return result;
			}

			TitanOperationStatus associateInputs = associateInputsToComponent(metadataVertex, resourceUniqueId, resource.getInputs());
			if (associateInputs != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateInputs));
				return result;
			}

			StorageOperationStatus associateRequirements = associateRequirementsToResource(metadataVertex, resourceUniqueId, resource.getRequirements());
			if (associateRequirements != StorageOperationStatus.OK) {
				result = Either.right(associateRequirements);
				return result;
			}

			StorageOperationStatus associateCapabilities = associateCapabilitiesToResource(metadataVertex, resourceUniqueId, resource.getCapabilities());
			if (associateCapabilities != StorageOperationStatus.OK) {
				result = Either.right(associateCapabilities);
				return result;
			}

			StorageOperationStatus associateInterfaces = associateInterfacesToResource(resourceData, resource.getInterfaces(), metadataVertex);
			if (associateInterfaces != StorageOperationStatus.OK) {
				result = Either.right(associateInterfaces);
				return result;
			}

			Map<String, ArtifactDefinition> resourceArtifacts = resource.getArtifacts();
			Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
			Map<String, ArtifactDefinition> toscaArtifacts = resource.getToscaArtifacts();
			if (resourceArtifacts != null) {
				if (deploymentArtifacts != null) {
					resourceArtifacts.putAll(deploymentArtifacts);
				}
			} else {
				resourceArtifacts = deploymentArtifacts;
			}
			if (toscaArtifacts != null) {
				if (resourceArtifacts != null) {
					resourceArtifacts.putAll(toscaArtifacts);
				} else {
					resourceArtifacts = toscaArtifacts;
				}
			}

			StorageOperationStatus associateArtifacts = associateArtifactsToResource(metadataVertex, resourceUniqueId, resourceArtifacts);
			if (associateArtifacts != StorageOperationStatus.OK) {
				result = Either.right(associateArtifacts);
				return result;
			}

			List<AdditionalInformationDefinition> additionalInformation = resource.getAdditionalInformation();
			StorageOperationStatus addAdditionalInformation = addAdditionalInformationToResource(metadataVertex, resourceUniqueId, additionalInformation);
			if (addAdditionalInformation != StorageOperationStatus.OK) {
				result = Either.right(addAdditionalInformation);
				return result;
			}

			result = this.getResource(resourceUniqueId, true);
			if (result.isRight()) {
				log.error("Cannot get full resource from the graph. status is {}", result.right().value());
				return Either.right(result.right().value());
			}

			if (log.isDebugEnabled()) {
				String json = prettyJson.toJson(result.left().value());
				log.debug("Resource retrieved is {}", json);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private StorageOperationStatus assosiateMetadataToCategory(Resource resource, ResourceMetadataData resourceData) {
		// get category
		String categoryName = resource.getCategories().get(0).getName();
		String subcategoryName = resource.getCategories().get(0).getSubcategories().get(0).getName();

		CategoryData categoryData = null;
		Either<CategoryData, StorageOperationStatus> categoryResult = elementOperation.getNewCategoryData(categoryName, NodeTypeEnum.ResourceNewCategory, CategoryData.class);
		if (categoryResult.isRight()) {
			StorageOperationStatus status = categoryResult.right().value();
			log.error("Cannot find category {} in the graph. status is {}", categoryName, status);
			return categoryResult.right().value();
		}
		categoryData = categoryResult.left().value();
		if (categoryData != null) {
			Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceNewCategory), (String) categoryData.getUniqueId(),
					GraphEdgeLabels.SUB_CATEGORY, NodeTypeEnum.ResourceSubcategory, SubCategoryData.class);
			if (childrenNodes.isRight()) {
				log.debug("Faield to fetch sub categories for  resource category {}", categoryData.getCategoryDataDefinition().getName());
				return DaoStatusConverter.convertTitanStatusToStorageStatus(childrenNodes.right().value());
			}
			for (ImmutablePair<SubCategoryData, GraphEdge> pair : childrenNodes.left().value()) {
				SubCategoryData subcategoryData = pair.left;
				if (subcategoryData.getSubCategoryDataDefinition().getName().equals(subcategoryName)) {
					Either<GraphRelation, TitanOperationStatus> result = titanGenericDao.createRelation(resourceData, subcategoryData, GraphEdgeLabels.CATEGORY, null);
					log.debug("After associating resource {} to subcategory {}. Edge type is {}", resourceData.getUniqueId(), subcategoryData, GraphEdgeLabels.CATEGORY);
					if (result.isRight()) {
						log.error("Faield to associate resource {} to category {}. Edge type is {}", resourceData.getUniqueId(), categoryData, GraphEdgeLabels.CATEGORY);
						return DaoStatusConverter.convertTitanStatusToStorageStatus(result.right().value());
					}

				}
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus addAdditionalInformationToResource(TitanVertex metadataVertex, String resourceUniqueId, List<AdditionalInformationDefinition> additionalInformation) {

		StorageOperationStatus result = null;
		if (additionalInformation == null || true == additionalInformation.isEmpty()) {
			result = super.addAdditionalInformation(NodeTypeEnum.Resource, resourceUniqueId, null, metadataVertex);
		} else {
			if (additionalInformation.size() == 1) {
				result = super.addAdditionalInformation(NodeTypeEnum.Resource, resourceUniqueId, additionalInformation.get(0));
			} else {
				result = StorageOperationStatus.BAD_REQUEST;
				log.info("Cannot create resource with more than one additional information object. The number of received object is {}", additionalInformation.size());
			}
		}
		return result;
	}

	private void generateUUID(Resource resource) {
		String prevUUID = resource.getUUID();
		String version = resource.getVersion();
		if ((prevUUID == null && uuidNormativeNewVersion.matcher(version).matches()) || uuidNewVersion.matcher(version).matches()) {
			UUID uuid = UUID.randomUUID();
			resource.setUUID(uuid.toString());
			MDC.put("serviceInstanceID", uuid.toString());
		}
	}

	@Override
	public Either<Resource, StorageOperationStatus> overrideResource(Resource resource, Resource resourceSaved, boolean inTransaction) {
		Either<Resource, StorageOperationStatus> result = null;
		try {
			String resourceId = resourceSaved.getUniqueId();

			// override interfaces to copy only resource's interfaces and not
			// derived interfaces
			Either<Map<String, InterfaceDefinition>, StorageOperationStatus> interfacesOfResourceOnly = interfaceLifecycleOperation.getAllInterfacesOfResource(resourceSaved.getUniqueId(), false, true);
			if (interfacesOfResourceOnly.isRight()) {
				log.error("failed to get interfaces of resource. resourceId {} status is {}", resourceId, interfacesOfResourceOnly.right().value());
				result = Either.right(interfacesOfResourceOnly.right().value());
				return result;
			}
			resource.setInterfaces(interfacesOfResourceOnly.left().value());
			resource.setArtifacts(resourceSaved.getArtifacts());
			resource.setDeploymentArtifacts(resourceSaved.getDeploymentArtifacts());
			resource.setGroups(resourceSaved.getGroups());
			resource.setInputs(null);
			resource.setLastUpdateDate(null);
			resource.setHighestVersion(true);

			// delete former resource
			Either<Resource, StorageOperationStatus> deleteResource = deleteResource(resourceId, true);
			if (deleteResource.isRight()) {
				log.error("failed to delete old resource with id {}. status = {}", resourceId, deleteResource.right().value());
				result = deleteResource;
				return result;
			}

			Either<Resource, StorageOperationStatus> createResource = createResource(resource, true);
			if (createResource.isRight()) {
				log.error("failed to create new version of resource {} status = {}", resourceId, createResource.right().value());
				result = createResource;
				return result;
			}
			Resource newResource = createResource.left().value();

			Either<List<GroupDefinition>, StorageOperationStatus> cloneGroupEither = cloneGroups(resource, newResource, null, inTransaction);
			if (cloneGroupEither.isLeft()) {
				newResource.setGroups(cloneGroupEither.left().value());
			} else if (cloneGroupEither.right().value() != StorageOperationStatus.OK) {
				log.error("failed to clone group of resource {} status = {}", resourceId, cloneGroupEither.right().value());
				result = Either.right(cloneGroupEither.right().value());
				return result;
			}

			result = Either.left(newResource);
			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	private StorageOperationStatus associateCapabilitiesToResource(TitanVertex metadataVertex, String resourceIda, Map<String, List<CapabilityDefinition>> capabilities) {
		StorageOperationStatus addCapabilityToResource = null;
		if (capabilities != null) {
			for (Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {

				List<CapabilityDefinition> capDefinition = entry.getValue();
				for (CapabilityDefinition item : capDefinition) {
					addCapabilityToResource = capabilityOperation.addCapability(metadataVertex, resourceIda, item.getName(), item, true);
					if (!addCapabilityToResource.equals(StorageOperationStatus.OK)) {
						return addCapabilityToResource;
					}

				}
			}

		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateRequirementsToResource(TitanVertex metadataVertex, String resourceId, Map<String, List<RequirementDefinition>> requirements) {

		if (requirements != null) {
			for (Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {

				List<RequirementDefinition> reqDefinition = entry.getValue();
				for (RequirementDefinition item : reqDefinition) {
					StorageOperationStatus addRequirementToResource = requirementOperation.addRequirementToResource(metadataVertex, item.getName(), item, resourceId, true);

					if (!addRequirementToResource.equals(StorageOperationStatus.OK)) {
						return addRequirementToResource;
					}
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateArtifactsToResource(TitanVertex metadataVertex, String resourceId, Map<String, ArtifactDefinition> artifacts) {

		StorageOperationStatus status = StorageOperationStatus.OK;
		if (artifacts != null) {
			Map<ArtifactDefinition, ArtifactDefinition> heatEnvMap = new HashMap<ArtifactDefinition, ArtifactDefinition>();
			for (Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {

				ArtifactDefinition artifactDefinition = entry.getValue();
				
				ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactDefinition.getArtifactType());
				if(artifactType != ArtifactTypeEnum.HEAT_ENV){
					status = artifactOperation.addArifactToComponent(artifactDefinition, resourceId, NodeTypeEnum.Resource, false, metadataVertex);
				}else{
					Optional<ArtifactDefinition> op = artifacts.values().stream().filter(p -> p.getUniqueId().equals(artifactDefinition.getGeneratedFromId())).findAny();
					if(op.isPresent()){
						heatEnvMap.put(artifactDefinition, op.get());
					}
					
					
				}

				if (!status.equals(StorageOperationStatus.OK)) {
					return status;
				}
			}
			for(Entry<ArtifactDefinition, ArtifactDefinition> entry : heatEnvMap.entrySet()){
				Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact = artifactOperation.addHeatEnvArtifact(entry.getKey(), entry.getValue(), resourceId, NodeTypeEnum.Resource, false);
				if (addHeatEnvArtifact.isRight()) {
					log.debug("failed to create heat env artifact on resource instance");
					return addHeatEnvArtifact.right().value();
				}
			}
		}
		return status;

	}

	private StorageOperationStatus associateInterfacesToResource(ResourceMetadataData resourceData, Map<String, InterfaceDefinition> interfaces, TitanVertex metadataVertex) {

		if (interfaces != null) {
			for (Entry<String, InterfaceDefinition> entry : interfaces.entrySet()) {

				InterfaceDefinition interfaceDefinition = entry.getValue();
				StorageOperationStatus status;
				if (((ResourceMetadataDataDefinition) resourceData.getMetadataDataDefinition()).isAbstract()) {
					status = interfaceLifecycleOperation.associateInterfaceToNode(resourceData, interfaceDefinition, metadataVertex);
				} else {
					status = interfaceLifecycleOperation.createInterfaceOnResource(interfaceDefinition, resourceData.getMetadataDataDefinition().getUniqueId(), interfaceDefinition.getType(), false, true, metadataVertex);
				}

				if (!status.equals(StorageOperationStatus.OK)) {
					return status;
				}
			}
		}
		return StorageOperationStatus.OK;

	}

	private Either<Resource, StorageOperationStatus> sendError(TitanOperationStatus status, StorageOperationStatus statusIfNotFound) {
		Either<Resource, StorageOperationStatus> result;
		if (status == TitanOperationStatus.NOT_FOUND) {
			result = Either.right(statusIfNotFound);
			return result;
		} else {
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}
	}

	private TitanOperationStatus associatePropertiesToResource(TitanVertex metadatVertex, String resourceId, List<PropertyDefinition> properties, List<ResourceMetadataData> derivedResources) {

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			log.debug("Cannot find any data type. Status is {}.", status);
			return status;
		}
		
		Map<String, PropertyDefinition> convertedProperties = new HashMap<>();

		if (properties != null) {
			for (PropertyDefinition propertyDefinition : properties) {
				convertedProperties.put(propertyDefinition.getName(), propertyDefinition);
			}
			
			Either<Map<String, PropertyDefinition>, TitanOperationStatus> getPropertiesOfAllDerivedFromRes = getPropertiesOfAllDerivedFrom(derivedResources);
			
			if(getPropertiesOfAllDerivedFromRes.isRight()){
				TitanOperationStatus status = getPropertiesOfAllDerivedFromRes.right().value();
				log.debug("Cannot fetch properties of all derived from resources. Status is {}.", status);
				return status;
			}
			
			Map<String, PropertyDefinition> allDerivedFromProperties = getPropertiesOfAllDerivedFromRes.left().value();
			
			TitanOperationStatus validatePropertyNamesUniqunessStatus = validatePropertyNamesUniquness(properties, allDerivedFromProperties);
			
			if(validatePropertyNamesUniqunessStatus != TitanOperationStatus.OK){
				return validatePropertyNamesUniqunessStatus;
			}
			
			return propertyOperation.addPropertiesToGraph(metadatVertex, convertedProperties, allDataTypes.left().value(), resourceId);
		}

		return TitanOperationStatus.OK;

	}

	private TitanOperationStatus validatePropertyNamesUniquness(List<PropertyDefinition> properties, Map<String, PropertyDefinition> allDerivedFromProperties) {
		
		TitanOperationStatus result  = TitanOperationStatus.OK;
		Optional<PropertyDefinition> propertyOptional= properties.stream()
				//filters out properties with the same name and different type
				.filter(prop -> allDerivedFromProperties.containsKey(prop.getName()) && !prop.getType().equals(allDerivedFromProperties.get(prop.getName()).getType()))
				//Searches for any matching value
				.findAny();
		if(propertyOptional.isPresent()){
			log.error("Property with name {} and type {} already exists in derived from resource. ", propertyOptional.get().getName(), allDerivedFromProperties.get( propertyOptional.get().getName()).getType());
			result = TitanOperationStatus.ALREADY_EXIST;
		}
		return result;
	}

	private Either<Map<String, PropertyDefinition>, TitanOperationStatus> getPropertiesOfAllDerivedFrom(List<ResourceMetadataData> derivedResources) {
		Map<String, PropertyDefinition> allDerivedProperties = new HashMap<>();
		Either<Map<String, PropertyDefinition>, TitanOperationStatus> getPropertiesOfAllDerivedFromRes = Either.left(allDerivedProperties);
		String currResourceName = null ;
		if(!CollectionUtils.isEmpty(derivedResources)){
			try{
				for(int i = derivedResources.size() - 1; i >= 0 ; --i){
					ResourceMetadataData currDerivedResource = derivedResources.get(i);
					currResourceName = currDerivedResource.getMetadataDataDefinition().getName();
					Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus>  res = 
							titanGenericDao.getChildrenNodes( currDerivedResource.getUniqueIdKey(), (String)currDerivedResource.getUniqueId(), GraphEdgeLabels.PROPERTY, NodeTypeEnum.Property, PropertyData.class);
					if(res.isRight() && res.right().value() != TitanOperationStatus.NOT_FOUND){
						getPropertiesOfAllDerivedFromRes = Either.right(res.right().value());
						break;
					}else if(res.isLeft()){
						allDerivedProperties.putAll(res.left().value().stream()
								//Maps PropertyData converted to PropertyDefinition
								.map(pair->	propertyOperation.convertPropertyDataToPropertyDefinition(pair.getLeft(), (String)pair.getRight().getProperties().get(GraphPropertiesDictionary.NAME.getProperty()), (String)currDerivedResource.getUniqueId()))
								//and collects it to a map
								.collect(Collectors.toMap(entry->entry.getName(), entry->entry)));
					}
				}
			}
			catch(Exception e){
				log.error("Exception occured during fetch properties of resource {}. ", currResourceName);
			}
		}
		return getPropertiesOfAllDerivedFromRes;
	}

	private TitanOperationStatus associateAttributesToResource(TitanVertex metadataVertex, List<AttributeDefinition> attributes, String resourceId) {
		TitanOperationStatus operationStatus = TitanOperationStatus.OK;

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			log.debug("Cannot find any data type. Status is {}.", status);
			return status;
		}

		if (attributes != null) {
			Map<String, AttributeDefinition> convertedAttributes = attributes.stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
			operationStatus = attributeOperation.addAttributesToGraph(metadataVertex, convertedAttributes, resourceId, allDataTypes.left().value());
		}
		return operationStatus;
	}

	private TitanOperationStatus associateMetadataToResource(ResourceMetadataData resourceData, TitanVertex creatorVertex, TitanVertex updaterVertex, List<ResourceMetadataData> derivedResources, TitanVertex metadataVertex) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.STATE.getProperty(), resourceData.getMetadataDataDefinition().getState());

		TitanOperationStatus result = titanGenericDao.createEdge(updaterVertex, metadataVertex, GraphEdgeLabels.STATE, props);
		log.debug("After associating user {} to resource {}. Edge type is {}", updaterVertex, resourceData.getUniqueId(), GraphEdgeLabels.STATE);
		if (!result.equals(TitanOperationStatus.OK)) {
			return result;
		}
		result = titanGenericDao.createEdge(updaterVertex, metadataVertex, GraphEdgeLabels.LAST_MODIFIER, null);
		log.debug("After associating user {}  to resource {}. Edge type is {}", updaterVertex, resourceData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
		if (!result.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate user {}  to resource {}. Edge type is {}", updaterVertex, resourceData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
			return result;
		}

		result = titanGenericDao.createEdge(creatorVertex, metadataVertex, GraphEdgeLabels.CREATOR, null);
		log.debug("After associating user {} to resource {}. Edge type is {} ", creatorVertex, resourceData.getUniqueId(), GraphEdgeLabels.CREATOR);
		if (!result.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate user {} to resource {}. Edge type is {} ", creatorVertex, resourceData.getUniqueId(), GraphEdgeLabels.CREATOR);
			return result;
		}
		// TODO Evg : need to change too..
		if (derivedResources != null) {
			for (ResourceMetadataData derivedResource : derivedResources) {
				log.debug("After associating resource {} to parent resource {}. Edge type is {}", resourceData.getUniqueId(), derivedResource.getUniqueId(), GraphEdgeLabels.DERIVED_FROM);
				Either<GraphRelation, TitanOperationStatus> createRelationResult = titanGenericDao.createRelation(resourceData, derivedResource, GraphEdgeLabels.DERIVED_FROM, null);
				if (createRelationResult.isRight()) {
					log.error("Failed to associate resource {} to derived ", resourceData.getUniqueId());
					return createRelationResult.right().value();
				}
			}
		}

		return TitanOperationStatus.OK;
	}

	public Either<List<ResourceMetadataData>, StorageOperationStatus> findDerivedResources(Resource resource) {

		List<ResourceMetadataData> derivedResources = new ArrayList<ResourceMetadataData>();
		List<String> derivedFromResources = resource.getDerivedFrom();
		if (derivedFromResources != null && false == derivedFromResources.isEmpty()) {

			for (String parentResource : derivedFromResources) {

				Map<String, Object> propertiesToMatch = new HashMap<String, Object>();
				propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());
				// propertiesToMatch.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(),
				// true);
				propertiesToMatch.put(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty(), parentResource);
				propertiesToMatch.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

				Either<List<ResourceMetadataData>, TitanOperationStatus> getParentResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, propertiesToMatch, ResourceMetadataData.class);
				List<ResourceMetadataData> resources = null;
				if (getParentResources.isRight()) {
					/*
					 * log.debug( "Cannot find parent resource by tosca resource name {} in the graph. Try to find by name", parentResource); 
					 * Map<String, Object> propertiesWithResourceNameToMatch = new HashMap<String, Object>();
					 * propertiesWithResourceNameToMatch.put( GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name()); propertiesWithResourceNameToMatch.put( GraphPropertiesDictionary.NAME.getProperty(), parentResource);
					 * propertiesWithResourceNameToMatch.put( GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty( ), true);
					 * 
					 * getParentResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, propertiesWithResourceNameToMatch, ResourceData.class); if (getParentResources.isRight()) { log.error(
					 * "Cannot find parent resource by tosca resource name" + parentResource + " in the graph."); return Either.right(StorageOperationStatus. PARENT_RESOURCE_NOT_FOUND); }else{ resources = getParentResources.left().value();
					 * hea
					 * }
					 */
					log.error("Cannot find parent resource by tosca resource name {} in the graph.", parentResource);
					return Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);

				} else {
					resources = getParentResources.left().value();
					if (resources == null || resources.size() == 0) {
						log.error("Cannot find parent resource by tosc name {} in the graph. resources size is empty", parentResource);
						return Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);
					} else {
						if (resources.size() > 1) {
							log.error("Multiple parent resources called {} found in the graph.", parentResource);
							return Either.right(StorageOperationStatus.MULTIPLE_PARENT_RESOURCE_FOUND);
						}
						ResourceMetadataData parentResourceData = resources.get(0);
						derivedResources.add(parentResourceData);
					}

				}

			}
		}
		return Either.left(derivedResources);
	}

	private ResourceMetadataData getResourceMetaDataFromResource(Resource resource) {
		ResourceMetadataData resourceData = new ResourceMetadataData((ResourceMetadataDataDefinition) resource.getComponentMetadataDefinition().getMetadataDataDefinition());
		if (resource.getNormalizedName() == null || resource.getNormalizedName().isEmpty()) {
			resourceData.getMetadataDataDefinition().setNormalizedName(ValidationUtils.normaliseComponentName(resource.getName()));
		}
		if (resource.getSystemName() == null || resource.getSystemName().isEmpty()) {
			resourceData.getMetadataDataDefinition().setSystemName(ValidationUtils.convertToSystemName(resource.getName()));
		}

		LifecycleStateEnum lifecycleStateEnum = resource.getLifecycleState();
		if (lifecycleStateEnum == null) {
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		}
		long currentDate = System.currentTimeMillis();
		if (resource.getCreationDate() == null) {
			resourceData.getMetadataDataDefinition().setCreationDate(currentDate);
		}
		resourceData.getMetadataDataDefinition().setLastUpdateDate(currentDate);

		return resourceData;
	}

	private ResourceMetadataData getResourceMetaDataForUpdate(Resource resource) {
		// PA - please note: if you add here any fields, make sure they are
		// validated (if needed)
		// at ResourceBusinessLogic.validateResourceFieldsBeforeUpdate() and
		// tested at ResourceBusinessLogicTest.
		ResourceMetadataData resourceData = getResourceMetaDataFromResource(resource);
		// resourceData.setLastUpdateDate(System.currentTimeMillis());
		// resourceData.setHighestVersion(resource.isHighestVersion());
		// resourceData.setNormalizedName(resource.getNormalizedName());
		// resourceData.setResourceType(resource.getResourceType().name());

		return resourceData;
	}

	public Either<Resource, StorageOperationStatus> getResource(String uniqueId) {
		return getResource(uniqueId, false);
	}

	public Either<Resource, StorageOperationStatus> getResource(String uniqueId, boolean inTransaction) {
		ComponentParametersView componentParametersView = new ComponentParametersView();
		return getResource(uniqueId, componentParametersView, inTransaction);
	}

	private TitanOperationStatus setComponentInstancesAttributesFromGraph(String uniqueId, Resource component) {
		Map<String, List<ComponentInstanceAttribute>> resourceInstancesAttributes = new HashMap<>();
		TitanOperationStatus status = TitanOperationStatus.OK;
		List<ComponentInstance> componentInstances = component.getComponentInstances();
		if (componentInstances != null) {
			for (ComponentInstance resourceInstance : componentInstances) {
				Either<List<ComponentInstanceAttribute>, TitanOperationStatus> eitherRIAttributes = attributeOperation.getAllAttributesOfResourceInstance(resourceInstance);
				if (eitherRIAttributes.isRight()) {
					status = eitherRIAttributes.right().value();
					break;
				} else {
					resourceInstancesAttributes.put(resourceInstance.getUniqueId(), eitherRIAttributes.left().value());
				}
			}

			component.setComponentInstancesAttributes(resourceInstancesAttributes);
		}

		return status;

	}

	private StorageOperationStatus setResourceAdditionalInformationFromGraph(String uniqueId, Resource resource) {

		List<AdditionalInformationDefinition> additionalInformation = new ArrayList<>();

		Either<AdditionalInformationDefinition, StorageOperationStatus> either = additionalInformationOperation.getAllAdditionalInformationParameters(NodeTypeEnum.Resource, uniqueId, true, true);

		if (either.isRight()) {
			StorageOperationStatus status = either.right().value();
			if (status == StorageOperationStatus.NOT_FOUND) {
				return StorageOperationStatus.OK;
			}
			return status;
		}

		AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
		additionalInformation.add(additionalInformationDefinition);

		resource.setAdditionalInformation(additionalInformation);

		return StorageOperationStatus.OK;

	}

	private StorageOperationStatus setResourceInterfacesFromGraph(String uniqueId, Resource resource) {

		Either<Map<String, InterfaceDefinition>, StorageOperationStatus> statusRes = interfaceLifecycleOperation.getAllInterfacesOfResource(uniqueId, true, true);
		if (statusRes.isRight()) {
			return statusRes.right().value();
		}
		Map<String, InterfaceDefinition> value = statusRes.left().value();

		resource.setInterfaces(value);

		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus setResourceCapabilitiesFromGraph(String uniqueId, Resource resource) {
		StorageOperationStatus retStatus;
		Either<Map<String, CapabilityDefinition>, StorageOperationStatus> result = capabilityOperation.getAllCapabilitiesOfResource(uniqueId, true, true);
		if (result.isRight()) {
			StorageOperationStatus status = result.right().value();
			if (status != StorageOperationStatus.NOT_FOUND) {
				retStatus = status;
			} else {
				retStatus = StorageOperationStatus.OK;
			}
		} else {
			Map<String, CapabilityDefinition> capabilities = result.left().value();
			if (capabilities != null && !capabilities.isEmpty() && resource.getResourceType().equals(ResourceTypeEnum.VF)) {
				log.error(String.format("VF %s has direct capabilities.!!!!!!!!!!!!!", resource.getName()));
				MigrationErrorInformer.addMalformedVF(resource.getUniqueId());
			}
			if (capabilities == null || capabilities.isEmpty() || resource.getResourceType().equals(ResourceTypeEnum.VF)) {
				Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> eitherCapabilities = super.getCapabilities(resource, NodeTypeEnum.Resource, true);
				if (eitherCapabilities.isLeft()) {
					retStatus = StorageOperationStatus.OK;
					Map<String, List<CapabilityDefinition>> calculatedCapabilities = eitherCapabilities.left().value();
					resource.setCapabilities(calculatedCapabilities);
				} else {
					retStatus = StorageOperationStatus.GENERAL_ERROR;
				}

			} else {
				retStatus = StorageOperationStatus.OK;
				resource.setCapabilities(capabilityOperation.convertCapabilityMap(capabilities, null, null));
			}

		}
		return retStatus;

	}

	public Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> getCapabilities(org.openecomp.sdc.be.model.Component component, NodeTypeEnum componentTypeEnum, boolean inTransaction) {

		try {
			Either<Map<String, CapabilityDefinition>, StorageOperationStatus> result = capabilityOperation.getAllCapabilitiesOfResource(component.getUniqueId(), true, true);
			if (result.isRight() || result.left().value().isEmpty()) {
				final Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> eitherCapabilities = super.getCapabilities(component, componentTypeEnum, inTransaction);
				return eitherCapabilities;
			} else {
				return Either.left(capabilityOperation.convertCapabilityMap(result.left().value(), null, null));
			}
		} finally {
			if (inTransaction == false) {
				titanGenericDao.commit();
			}
		}
	}

	public Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> getRequirements(org.openecomp.sdc.be.model.Component component, NodeTypeEnum componentTypeEnum, boolean inTransaction) {
		try {
			Either<Map<String, RequirementDefinition>, StorageOperationStatus> result = requirementOperation.getAllResourceRequirements(component.getUniqueId(), true);
			if (result.isRight() || result.left().value().isEmpty()) {
				final Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> eitherCapabilities = super.getRequirements(component, componentTypeEnum, true);
				return eitherCapabilities;
			} else {
				return Either.left(requirementOperation.convertRequirementMap(result.left().value(), null, null));
			}
		} finally {
			if (inTransaction == false) {
				titanGenericDao.commit();
			}
		}

	}

	private StorageOperationStatus setResourceRequirementsFromGraph(String uniqueId, Resource resource, boolean inTransaction) {
		StorageOperationStatus retStatus;
		Either<Map<String, RequirementDefinition>, StorageOperationStatus> result = requirementOperation.getAllResourceRequirements(uniqueId, inTransaction);
		;
		if (result.isRight()) {
			StorageOperationStatus status = result.right().value();
			if (status != StorageOperationStatus.NOT_FOUND) {
				retStatus = status;
			} else {
				retStatus = StorageOperationStatus.OK;
			}
		} else {
			Map<String, RequirementDefinition> requirements = result.left().value();
			if (requirements != null && !requirements.isEmpty() && resource.getResourceType().equals(ResourceTypeEnum.VF)) {
				log.error(String.format("VF %s has direct requirements.!!!!!!!!!!!!!", resource.getName()));
				MigrationErrorInformer.addMalformedVF(resource.getUniqueId());
			}
			if (requirements == null || requirements.isEmpty() || resource.getResourceType() == ResourceTypeEnum.VF) {
				Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> eitherCapabilities = super.getRequirements(resource, NodeTypeEnum.Resource, true);
				if (eitherCapabilities.isLeft()) {
					retStatus = StorageOperationStatus.OK;
					Map<String, List<RequirementDefinition>> calculatedCapabilities = eitherCapabilities.left().value();
					resource.setRequirements(calculatedCapabilities);
				} else {
					retStatus = StorageOperationStatus.GENERAL_ERROR;
				}

			} else {
				retStatus = StorageOperationStatus.OK;
				resource.setRequirements(requirementOperation.convertRequirementMap(requirements, null, null));
			}

		}
		return retStatus;
	}

	private TitanOperationStatus setResourcePropertiesFromGraph(String uniqueId, Resource resource) {

		List<PropertyDefinition> properties = new ArrayList<>();
		TitanOperationStatus status = propertyOperation.findAllResourcePropertiesRecursively(uniqueId, properties);
		if (status == TitanOperationStatus.OK) {
			resource.setProperties(properties);
		}

		return status;

	}

	private TitanOperationStatus setResourceAttributesFromGraph(String uniqueId, Resource resource) {

		List<AttributeDefinition> attributes = new ArrayList<>();
		TitanOperationStatus status = attributeOperation.findAllResourceAttributesRecursively(uniqueId, attributes);
		if (status == TitanOperationStatus.OK) {
			resource.setAttributes(attributes);
		}

		return status;

	}

	private TitanOperationStatus setResourceDerivedFromGraph(String uniqueId, Resource resource) {
		List<String> derivedFromList = new ArrayList<String>();

		TitanOperationStatus listFromGraphStatus = fillResourceDerivedListFromGraph(uniqueId, derivedFromList);
		if (!TitanOperationStatus.OK.equals(listFromGraphStatus)) {
			return listFromGraphStatus;
		}

		if (false == derivedFromList.isEmpty()) {
			if (derivedFromList.size() > 1) {
				List<String> lastDerivedFrom = new ArrayList<String>();
				lastDerivedFrom.add(derivedFromList.get(1));
				resource.setDerivedFrom(lastDerivedFrom);
				resource.setDerivedList(derivedFromList);
			} else {
				resource.setDerivedFrom(null);
				resource.setDerivedList(derivedFromList);
			}

		}

		return TitanOperationStatus.OK;
	}

	public TitanOperationStatus fillResourceDerivedListFromGraph(String uniqueId, List<String> derivedFromList) {
		// Either<List<ImmutablePair<ResourceMetadataData, GraphEdge>>,
		// TitanOperationStatus> childrenNodes =
		// titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource),
		// uniqueId, GraphEdgeLabels.DERIVED_FROM,
		// NodeTypeEnum.Resource, ResourceMetadataData.class);
		//
		// if (childrenNodes.isRight() && (childrenNodes.right().value() !=
		// TitanOperationStatus.NOT_FOUND)) {
		// return childrenNodes.right().value();
		// } else if (childrenNodes.isLeft()) {
		//
		// List<ImmutablePair<ResourceMetadataData, GraphEdge>> pairList =
		// childrenNodes.left().value();
		// for (ImmutablePair<ResourceMetadataData, GraphEdge> pair : pairList)
		// {
		// derivedFromList.add(pair.left.getMetadataDataDefinition().getName());
		// return
		// fillResourceDerivedListFromGraph(pair.left.getMetadataDataDefinition().getUniqueId(),
		// derivedFromList);
		// }
		// }
		List<ResourceMetadataData> derivedData = new ArrayList<ResourceMetadataData>();
		TitanOperationStatus findResourcesPathRecursively = findResourcesPathRecursively(uniqueId, derivedData);
		if (!findResourcesPathRecursively.equals(TitanOperationStatus.OK)) {
			return findResourcesPathRecursively;
		}
		derivedData.forEach(resourceData -> derivedFromList.add(((ResourceMetadataDataDefinition) resourceData.getMetadataDataDefinition()).getToscaResourceName()));
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setResourceLastModifierFromGraph(Resource resource, String resourceId) {

		Either<ImmutablePair<UserData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.LAST_MODIFIER, NodeTypeEnum.User,
				UserData.class);
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
		resource.setLastUpdaterUserId(userData.getUserId());
		resource.setLastUpdaterFullName(fullName);

		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setResourceCreatorFromGraph(Resource resource, String resourceId) {

		Either<ImmutablePair<UserData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.CREATOR, NodeTypeEnum.User, UserData.class);
		if (parentNode.isRight()) {
			log.debug("Failed to find the creator of resource {}", resourceId);
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
			log.debug("Build resource : set creator full name to {}", fullName);
		resource.setCreatorUserId(userData.getUserId());
		resource.setCreatorFullName(fullName);

		return TitanOperationStatus.OK;
	}

	@Override
	TitanOperationStatus setComponentCategoriesFromGraph(Component resource) {
		String uniqueId = resource.getUniqueId();
		Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, TitanOperationStatus> parentNode = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), uniqueId, GraphEdgeLabels.CATEGORY,
				NodeTypeEnum.ResourceSubcategory, SubCategoryData.class);
		if (parentNode.isRight()) {
			return parentNode.right().value();
		}

		List<ImmutablePair<SubCategoryData, GraphEdge>> listValue = parentNode.left().value();
		log.debug("Result after looking for subcategory nodes pointed by resource {}. status is {}", uniqueId, listValue);
		if (listValue.size() > 1) {
			log.error("Multiple edges foud between resource {} to subcategory nodes.", uniqueId);
		}
		ImmutablePair<SubCategoryData, GraphEdge> value = listValue.get(0);
		log.debug("Found parent node {}", value);

		SubCategoryData subcategoryData = value.getKey();

		Either<ImmutablePair<CategoryData, GraphEdge>, TitanOperationStatus> categoryNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceSubcategory), (String) subcategoryData.getUniqueId(),
				GraphEdgeLabels.SUB_CATEGORY, NodeTypeEnum.ResourceNewCategory, CategoryData.class);
		if (categoryNode.isRight()) {
			return categoryNode.right().value();
		}

		CategoryData categoryData = categoryNode.left().value().left;
		CategoryDefinition catDef = new CategoryDefinition(categoryData.getCategoryDataDefinition());
		SubCategoryDefinition subcatDef = new SubCategoryDefinition(subcategoryData.getSubCategoryDataDefinition());

		resource.addCategory(catDef, subcatDef);
		return TitanOperationStatus.OK;
	}

	public String buildFullName(UserData userData) {

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

	private Resource convertResourceDataToResource(ResourceMetadataData resourceData) {

		ResourceMetadataDefinition resourceMetadataDataDefinition = new ResourceMetadataDefinition((ResourceMetadataDataDefinition) resourceData.getMetadataDataDefinition());

		Resource resource = new Resource(resourceMetadataDataDefinition);

		return resource;
	}

	@Override
	public Either<Resource, StorageOperationStatus> deleteResource(String resourceId) {
		return deleteResource(resourceId, false);
	}

	@Override
	public Either<Resource, StorageOperationStatus> updateResource(Resource resource) {

		return updateResource(resource, false);

	}

	@Override
	public Either<Integer, StorageOperationStatus> getNumberOfResourcesByName(String resourceName) {

		Map<String, Object> propertiesToMatch = new HashMap<String, Object>();
		propertiesToMatch.put(GraphPropertiesDictionary.NAME.getProperty(), resourceName);

		Either<List<ResourceMetadataData>, TitanOperationStatus> getParentResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, propertiesToMatch, ResourceMetadataData.class);
		log.debug("result after searching for resources called {} is {}", resourceName, getParentResources);
		if (getParentResources.isRight()) {
			TitanOperationStatus titanStatus = getParentResources.right().value();
			if (titanStatus == TitanOperationStatus.NOT_FOUND) {
				log.debug("Number of returned resources is 0.");
				return Either.left(0);
			}
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus));
		} else {
			List<ResourceMetadataData> value = getParentResources.left().value();
			int numberOFResources = (value == null ? 0 : value.size());
			log.debug("The number of resources returned after searching for resource called {} is {}", resourceName, numberOFResources);
			return Either.left(numberOFResources);
		}
	}

	public PropertyOperation getPropertyOperation() {
		return propertyOperation;
	}

	public void setPropertyOperation(PropertyOperation propertyOperation) {
		this.propertyOperation = propertyOperation;
	}

	public RequirementOperation getRequirementOperation() {
		return requirementOperation;
	}

	public void setRequirementOperation(RequirementOperation requirementOperation) {
		this.requirementOperation = requirementOperation;
	}

	public CapabilityOperation getCapabilityOperation() {
		return capabilityOperation;
	}

	public void setCapabilityOperation(CapabilityOperation capabilityOperation) {
		this.capabilityOperation = capabilityOperation;
	}

	public IArtifactOperation getArtifactOperation() {
		return artifactOperation;
	}

	public void setArtifactOperation(IArtifactOperation artifactOperation) {
		this.artifactOperation = artifactOperation;
	}

	public InterfaceLifecycleOperation getInterfaceLifecycleOperation() {
		return interfaceLifecycleOperation;
	}

	public void setInterfaceLifecycleOperation(InterfaceLifecycleOperation interfaceLifecycleOperation) {
		this.interfaceLifecycleOperation = interfaceLifecycleOperation;
	}

	public TitanGenericDao getTitanGenericDao() {
		return titanGenericDao;
	}

	public IElementOperation getElementOperation() {
		return elementOperation;
	}

	public void setElementOperation(IElementOperation elementOperation) {
		this.elementOperation = elementOperation;
	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getAllCertifiedResources(boolean isAbstract) {

		return getAllCertifiedResources(isAbstract, null);

	}

	@Override
	/**
	 * Deletes the resource node, property nodes and relation to artifacts. MUST handle deletion of artifact from artifacts repository outside this method (in catalog-be)
	 */
	public Either<Resource, StorageOperationStatus> deleteResource(String resourceId, boolean inTransaction) {

		Either<Resource, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR);
		try {

			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
				return result;
			}

			TitanGraph titanGraph = graphResult.left().value();
			Iterable<TitanVertex> vertecies = titanGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId).vertices();
			Either<Resource, StorageOperationStatus> resourceEither = getResource(resourceId, true);
			Resource resource = resourceEither.left().value();
			if (vertecies != null && resourceEither.isLeft()) {
				Iterator<TitanVertex> iterator = vertecies.iterator();
				if (iterator != null && iterator.hasNext()) {
					Vertex rootVertex = iterator.next();
					TitanOperationStatus deleteChildrenNodes = graphDeleteUtil.deleteChildrenNodes(rootVertex, GraphEdgeLabels.PROPERTY);
					log.debug("After deleting properties nodes in the graph. status is {}", deleteChildrenNodes);
					if (deleteChildrenNodes != TitanOperationStatus.OK) {
						result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(deleteChildrenNodes));
						return result;
					}
					StorageOperationStatus removeInterfacesFromResource = removeInterfacesFromResource(resource);
					log.debug("After deleting interfaces nodes in the graph. status is {}", removeInterfacesFromResource);
					if (!removeInterfacesFromResource.equals(StorageOperationStatus.OK)) {
						result = Either.right(removeInterfacesFromResource);
						return result;
					}
					StorageOperationStatus removeArtifactsFromResource = removeArtifactsFromResource(resource);
					log.debug("After deleting artifacts nodes in the graph. status is {}", removeArtifactsFromResource);
					if (!removeArtifactsFromResource.equals(StorageOperationStatus.OK)) {
						result = Either.right(removeArtifactsFromResource);
						return result;
					}
					StorageOperationStatus removeCapabilitiesFromResource = removeCapabilitiesFromResource(resource);
					log.debug("After deleting capabilities nodes in the graph. status is {}", removeCapabilitiesFromResource);
					if (!removeCapabilitiesFromResource.equals(StorageOperationStatus.OK)) {
						result = Either.right(removeCapabilitiesFromResource);
						return result;
					}

					StorageOperationStatus removeRequirementsFromResource = removeRequirementsFromResource(resource);
					log.debug("After deleting requirements nodes in the graph. status is {}", removeRequirementsFromResource);
					if (!removeRequirementsFromResource.equals(StorageOperationStatus.OK)) {
						result = Either.right(removeRequirementsFromResource);
						return result;
					}

					StorageOperationStatus removeRIsFromResource = removeResourceInstanceFromResource(resource);
					log.debug("After deleting resource instance nodes in the graph. status is {}", removeRIsFromResource);
					if (!removeRIsFromResource.equals(StorageOperationStatus.OK)) {
						result = Either.right(removeRIsFromResource);
						return result;
					}

					StorageOperationStatus removeAttributesFromResource = removeAttributesFromResource(resource);
					log.debug("After deleting requirements nodes in the graph. status is {}", removeRequirementsFromResource);
					if (removeAttributesFromResource != StorageOperationStatus.OK) {
						result = Either.right(removeAttributesFromResource);
						return result;
					}

					StorageOperationStatus removeInputsFromResource = removeInputsFromComponent(NodeTypeEnum.Resource, resource);
					log.debug("After deleting requirements nodes in the graph. status is {}", removeInputsFromResource);
					if (removeInputsFromResource != StorageOperationStatus.OK) {
						result = Either.right(removeInputsFromResource);
						return result;
					}

					StorageOperationStatus removeAdditionalInformationFromResource = super.deleteAdditionalInformation(NodeTypeEnum.Resource, resource.getUniqueId());
					log.debug("After deleting additional information node in the graph. status is {}", removeAdditionalInformationFromResource);
					if (!removeAdditionalInformationFromResource.equals(StorageOperationStatus.OK)) {
						result = Either.right(removeAdditionalInformationFromResource);
						return result;
					}

					StorageOperationStatus removeGroupsFromResource = super.deleteGroups(NodeTypeEnum.Resource, resource.getUniqueId());
					log.debug("After deleting group nodes in the graph. status is {}", removeGroupsFromResource);
					if (!removeGroupsFromResource.equals(StorageOperationStatus.OK)) {
						result = Either.right(removeGroupsFromResource);
						return result;
					}

					rootVertex.remove();

				} else {
					result = Either.right(StorageOperationStatus.NOT_FOUND);
					return result;
				}
			} else {
				result = Either.right(StorageOperationStatus.NOT_FOUND);
				return result;
			}

			result = Either.left(resource);
			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("deleteResource operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("deleteResource operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	private StorageOperationStatus removeAttributesFromResource(Resource resource) {
		Either<Map<String, AttributeDefinition>, StorageOperationStatus> deleteAllAttributeAssociatedToNode = attributeOperation.deleteAllAttributeAssociatedToNode(NodeTypeEnum.Resource, resource.getUniqueId());
		return deleteAllAttributeAssociatedToNode.isRight() ? deleteAllAttributeAssociatedToNode.right().value() : StorageOperationStatus.OK;
	}

	private StorageOperationStatus removeArtifactsFromResource(Resource resource) {

		String resourceId = resource.getUniqueId();
		Map<String, ArtifactDefinition> allArtifacts = new HashMap<String, ArtifactDefinition>();
		if (resource.getArtifacts() != null) {
			allArtifacts.putAll(resource.getArtifacts());
		}
		if (resource.getDeploymentArtifacts() != null) {
			allArtifacts.putAll(resource.getDeploymentArtifacts());
		}
		if (allArtifacts != null) {
			for (Entry<String, ArtifactDefinition> entry : allArtifacts.entrySet()) {

				ArtifactDefinition artifactDefinition = entry.getValue();
				Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromResource = artifactOperation.removeArifactFromResource(resourceId, artifactDefinition.getUniqueId(), NodeTypeEnum.Resource, true, true);
				if (removeArifactFromResource.isRight()) {
					return removeArifactFromResource.right().value();
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus removeInterfacesFromResource(Resource resource) {

		String resourceId = resource.getUniqueId();
		// delete only interfaces of this resource (not interfaces derived)
		Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource = interfaceLifecycleOperation.getAllInterfacesOfResource(resourceId, false, true);
		if (allInterfacesOfResource.isRight()) {
			log.error("failed to get interfaces for resource {}. status is {}", resourceId, allInterfacesOfResource.right().value());
			return allInterfacesOfResource.right().value();
		}
		Map<String, InterfaceDefinition> interfaces = allInterfacesOfResource.left().value();
		if (interfaces != null) {
			for (Entry<String, InterfaceDefinition> entry : interfaces.entrySet()) {
				Boolean isAbstract = resource.isAbstract();

				InterfaceDefinition interfaceDefinition = entry.getValue();
				// esofer - in case the resource is abstract, we deleting only
				// the edge to the interface.
				if (isAbstract != null && true == isAbstract.booleanValue()) {
					log.debug("Going to dissociate resource {} from interface {}", resourceId, interfaceDefinition.getUniqueId());
					UniqueIdData uniqueIdData = new UniqueIdData(NodeTypeEnum.Resource, resourceId);
					Either<InterfaceDefinition, StorageOperationStatus> dissociateInterfaceFromNode = interfaceLifecycleOperation.dissociateInterfaceFromNode(uniqueIdData, interfaceDefinition);
					if (dissociateInterfaceFromNode.isRight()) {
						log.error("failed to dissociate resource {} from interface {}. status is {}", resourceId, interfaceDefinition.getUniqueId(), dissociateInterfaceFromNode.right().value());
						return dissociateInterfaceFromNode.right().value();
					}
				} else {
					Either<InterfaceDefinition, StorageOperationStatus> deleteInterfaceOfResourceOnGraph = interfaceLifecycleOperation.deleteInterfaceOfResourceOnGraph(resourceId, interfaceDefinition, true);
					if (deleteInterfaceOfResourceOnGraph.isRight()) {
						return deleteInterfaceOfResourceOnGraph.right().value();
					}
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus removeCapabilitiesFromResource(Resource resource) {

		String resourceId = resource.getUniqueId();

		Either<Map<String, CapabilityDefinition>, StorageOperationStatus> deleteAllRes = capabilityOperation.deleteAllCapabilities(resourceId, true);
		if (deleteAllRes.isRight()) {
			StorageOperationStatus status = deleteAllRes.right().value();
			if (status == StorageOperationStatus.NOT_FOUND) {
				return StorageOperationStatus.OK;
			}
			return status;
		}

		return StorageOperationStatus.OK;

	}

	private StorageOperationStatus removeRequirementsFromResource(Resource resource) {

		String resourceId = resource.getUniqueId();

		Either<Map<String, RequirementDefinition>, StorageOperationStatus> deleteAllRes = requirementOperation.deleteAllRequirements(resourceId, true);

		if (deleteAllRes.isRight()) {
			StorageOperationStatus status = deleteAllRes.right().value();
			if (status == StorageOperationStatus.NOT_FOUND) {
				return StorageOperationStatus.OK;
			}
			return status;
		}

		return StorageOperationStatus.OK;

	}

	private StorageOperationStatus removeResourceInstanceFromResource(Resource resource) {
		String resourceId = resource.getUniqueId();

		Either<List<ComponentInstance>, StorageOperationStatus> deleteAllResourceInstancesRes = componentInstanceOperation.deleteAllComponentInstances(resourceId, NodeTypeEnum.Resource, true);
		if (deleteAllResourceInstancesRes.isRight()) {
			StorageOperationStatus status = deleteAllResourceInstancesRes.right().value();
			if (status == StorageOperationStatus.NOT_FOUND) {
				return StorageOperationStatus.OK;
			}
			return status;
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<Resource, StorageOperationStatus> updateResource(Resource resource, boolean inTransaction) {
		return (Either<Resource, StorageOperationStatus>) updateComponent(resource, inTransaction, titanGenericDao, resource.getClass(), NodeTypeEnum.Resource);

	}

	@Override
	protected <T extends Component> StorageOperationStatus updateDerived(Component component, Component currentComponent, ComponentMetadataData updatedResourceData, Class<T> clazz) {
		Resource resource = (Resource) component;
		Resource currentResource = (Resource) currentComponent;
		if (resource.getDerivedFrom() != null) {// meaning derived from changed

			Either<List<ResourceMetadataData>, StorageOperationStatus> findDerivedResourcesOld = findDerivedResources(currentResource);
			if (findDerivedResourcesOld.isRight()) {
				log.debug("Couldn't find derived resource {} for current resource in the graph", currentResource.getDerivedFrom().get(0));
				return findDerivedResourcesOld.right().value();
			}

			List<ResourceMetadataData> oldDerived = findDerivedResourcesOld.left().value();
			if (oldDerived.isEmpty()) {
				log.debug("Derived from list fetched from DB for current resource is empty");
				return StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND;
			}

			Either<List<ResourceMetadataData>, StorageOperationStatus> findDerivedResourcesNew = findDerivedResources((Resource) resource);
			if (findDerivedResourcesNew.isRight()) {
				log.debug("Couldn't find derived resource {} for update resource in the graph", resource.getDerivedFrom().get(0));
				return findDerivedResourcesNew.right().value();
			}

			List<ResourceMetadataData> newDerived = findDerivedResourcesNew.left().value();
			if (newDerived.isEmpty()) {
				log.debug("Derived from list fetched from DB for updated resource is empty");
				return StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND;
			}

			Either<Boolean, TitanOperationStatus> reassociateDerivedFrom = reassociateDerivedFrom((ResourceMetadataData) updatedResourceData, oldDerived, newDerived);
			if (reassociateDerivedFrom.isRight()) {
				log.debug("Couldn't change derived from for the resoure");
				return DaoStatusConverter.convertTitanStatusToStorageStatus(reassociateDerivedFrom.right().value());
			}
		}
		return StorageOperationStatus.OK;
	}

	private Either<Boolean, TitanOperationStatus> reassociateDerivedFrom(ResourceMetadataData resourceData, List<ResourceMetadataData> oldDerived, List<ResourceMetadataData> newDerived) {
		ResourceMetadataData oldDerivedNode = oldDerived.get(0);
		log.debug("Dissociating resource {} from old parent resource {}", resourceData.getUniqueId(), oldDerivedNode.getUniqueId());
		Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(resourceData, oldDerivedNode, GraphEdgeLabels.DERIVED_FROM);
		if (deleteRelation.isRight()) {
			log.debug("Failed to dissociate resource {} from old parent resource {}", resourceData.getUniqueId(), oldDerivedNode.getUniqueId());
			return Either.right(deleteRelation.right().value());
		}
		ResourceMetadataData newDerivedNode = newDerived.get(0);
		log.debug("Associating resource {} with new parent resource {}", resourceData.getUniqueId(), newDerivedNode.getUniqueId());
		Either<GraphRelation, TitanOperationStatus> addRelation = titanGenericDao.createRelation(resourceData, newDerivedNode, GraphEdgeLabels.DERIVED_FROM, null);
		if (addRelation.isRight()) {
			log.debug("Failed to associate resource {} with new parent resource {}", resourceData.getUniqueId(), newDerivedNode.getUniqueId());
			return Either.right(addRelation.right().value());
		}

		return Either.left(true);
	}

	private StorageOperationStatus moveCategoryEdge(Resource resource, ResourceMetadataData resourceData, CategoryDefinition newCategory) {

		StorageOperationStatus result = StorageOperationStatus.OK;

		GraphRelation categoryRelation = new GraphRelation();
		categoryRelation.setType(GraphEdgeLabels.CATEGORY.getProperty());
		RelationEndPoint relationEndPoint = new RelationEndPoint(NodeTypeEnum.Resource, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resource.getUniqueId());
		categoryRelation.setFrom(relationEndPoint);
		Either<GraphRelation, TitanOperationStatus> deleteOutgoingRelation = titanGenericDao.deleteOutgoingRelation(categoryRelation);
		if (deleteOutgoingRelation.isRight()) {
			log.error("Failed to delete category from resource {}. Edge type is {}", resourceData.getUniqueId(), GraphEdgeLabels.CATEGORY);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(deleteOutgoingRelation.right().value());
			return result;
		}

		log.debug("After removing edge from graph {}", deleteOutgoingRelation);

		return assosiateMetadataToCategory(resource, resourceData);
	}

	private StorageOperationStatus moveLastModifierEdge(Resource resource, ResourceMetadataData resourceData, UserData modifierUserData) {

		StorageOperationStatus result = StorageOperationStatus.OK;

		GraphRelation lastModifierRelation = new GraphRelation();
		lastModifierRelation.setType(GraphEdgeLabels.LAST_MODIFIER.getProperty());
		RelationEndPoint relationEndPoint = new RelationEndPoint(NodeTypeEnum.Resource, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resource.getUniqueId());
		lastModifierRelation.setTo(relationEndPoint);
		Either<GraphRelation, TitanOperationStatus> deleteIncomingRelation = titanGenericDao.deleteIncomingRelation(lastModifierRelation);
		if (deleteIncomingRelation.isRight()) {
			log.error("Failed to delete user from resource {}. Edge type is {}", resourceData.getUniqueId(),GraphEdgeLabels.LAST_MODIFIER);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(deleteIncomingRelation.right().value());
			return result;
		}

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(modifierUserData, resourceData, GraphEdgeLabels.LAST_MODIFIER, null);
		log.debug("After associating user {} to resource {}. Edge type is {}", modifierUserData, resourceData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
		if (createRelation.isRight()) {
			log.error("Failed to associate user {} to resource {}. Edge type is {}", modifierUserData, resourceData.getUniqueId(), GraphEdgeLabels.LAST_MODIFIER);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(createRelation.right().value());
			return result;
		}

		return result;
	}

	private Either<ResourceMetadataData, TitanOperationStatus> findResource(String resourceId) {

		String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource);
		Either<ResourceMetadataData, TitanOperationStatus> findResource = titanGenericDao.getNode(key, resourceId, ResourceMetadataData.class);

		return findResource;
	}

	private StorageOperationStatus setArtifactFromGraph(String uniqueId, Resource resource) {
		StorageOperationStatus result = StorageOperationStatus.OK;
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = artifactOperation.getArtifacts(uniqueId, NodeTypeEnum.Resource, true);
		if (artifacts.isRight()) {
			result = artifacts.right().value();
		} else {
			createSpecificArtifactList(resource, artifacts.left().value());
		}
		return result;
	}

	@Override
	public <T extends Component> Either<T, StorageOperationStatus> getComponent(String id, Class<T> clazz) {

		Either<Resource, StorageOperationStatus> component = getResource(id);
		if (component.isRight()) {
			return Either.right(component.right().value());
		}
		return Either.left(clazz.cast(component.left().value()));
	}

	@Override
	public Either<Boolean, StorageOperationStatus> validateResourceNameExists(String resourceName, ResourceTypeEnum resourceType) {
		if (resourceType != null) {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());
			if (resourceType.equals(ResourceTypeEnum.VF)) {
				return validateResourceNameUniqueness(resourceName, properties, null, titanGenericDao);
			} else {
				return validateResourceNameUniqueness(resourceName, null, properties, titanGenericDao);
			}

		} else {
			return validateResourceNameUniqueness(resourceName, null, null, titanGenericDao);
		}

	}

	public Either<Boolean, StorageOperationStatus> validateToscaResourceNameExists(String templateName) {
		return validateToscaResourceNameUniqueness(templateName, titanGenericDao);
	}
	
	//Tal G for US815447
	public Either<Boolean, StorageOperationStatus> validateToscaResourceNameExtends(String templateNameCurrent, String templateNameExtends) {
		
		String currentTemplateNameChecked = templateNameExtends;
		
		while(currentTemplateNameChecked != null && !currentTemplateNameChecked.equalsIgnoreCase(templateNameCurrent)){
			Either<Resource, StorageOperationStatus> latestByToscaResourceName = getLatestByToscaResourceName(currentTemplateNameChecked, true);
			
			if(latestByToscaResourceName.isRight()){
				return latestByToscaResourceName.right().value() == StorageOperationStatus.NOT_FOUND ? Either.left(false) : Either.right(latestByToscaResourceName.right().value());
			}
			
			Resource value = latestByToscaResourceName.left().value();	
			
			if(value.getDerivedFrom() != null){
				currentTemplateNameChecked = value.getDerivedFrom().get(0);				
			} else {
				currentTemplateNameChecked = null;
			}
		}
		
		return (currentTemplateNameChecked != null && currentTemplateNameChecked.equalsIgnoreCase(templateNameCurrent)) ? Either.left(true) : Either.left(false);
	}

	public Either<List<ArtifactDefinition>, StorageOperationStatus> getAdditionalArtifacts(String resourceId, boolean recursively, boolean inTransaction) {
		List<ArtifactDefinition> artifacts = new ArrayList<>();

		Either<Map<String, InterfaceDefinition>, StorageOperationStatus> interfacesOfResource = interfaceLifecycleOperation.getAllInterfacesOfResource(resourceId, false, true);
		if (interfacesOfResource.isRight()) {
			log.error("failed to get all resource interfaces. resource id={}. status ={}", resourceId, interfacesOfResource.right().value());
			return Either.right(interfacesOfResource.right().value());
		}

		Map<String, InterfaceDefinition> interfaces = interfacesOfResource.left().value();
		if (interfaces != null && !interfaces.isEmpty()) {
			for (Entry<String, InterfaceDefinition> entry : interfaces.entrySet()) {

				InterfaceDefinition interfaceDefinition = entry.getValue();
				Map<String, Operation> operations = interfaceDefinition.getOperationsMap();
				if (operations != null && !operations.isEmpty()) {
					for (Entry<String, Operation> opEntry : operations.entrySet()) {

						Operation operation = opEntry.getValue();
						ArtifactDefinition artifactDefinition = operation.getImplementationArtifact();
						if (artifactDefinition != null) {
							artifacts.add(artifactDefinition);
						}
					}
				}
			}
		}
		return Either.left(artifacts);
	}

	@SuppressWarnings("unchecked")
	public Either<List<Resource>, StorageOperationStatus> getFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, boolean inTransaction) {
		return (Either<List<Resource>, StorageOperationStatus>) (Either<?, StorageOperationStatus>) getFollowedComponent(userId, lifecycleStates, lastStateStates, inTransaction, titanGenericDao, NodeTypeEnum.Resource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> getComponent(String id, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) getResource(id, inTransaction);
	}

	// @Override
	// public <T> Either<T, StorageOperationStatus> getComponent_tx(String id,
	// boolean inTransaction) {
	// return (Either<T, StorageOperationStatus>) getResource_tx(id,
	// inTransaction);
	// }

	private Optional<ImmutablePair<SubCategoryData, GraphEdge>> validateCategoryHierarcy(List<ImmutablePair<SubCategoryData, GraphEdge>> childNodes, String subCategoryName) {
		Predicate<ImmutablePair<SubCategoryData, GraphEdge>> matchName = p -> p.getLeft().getSubCategoryDataDefinition().getName().equals(subCategoryName);
		return childNodes.stream().filter(matchName).findAny();
	}

	private Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, StorageOperationStatus> getAllSubCategories(String categoryName) {
		Either<CategoryData, StorageOperationStatus> categoryResult = elementOperation.getNewCategoryData(categoryName, NodeTypeEnum.ResourceNewCategory, CategoryData.class);
		if (categoryResult.isRight()) {
			return Either.right(categoryResult.right().value());
		}
		CategoryData categoryData = categoryResult.left().value();

		Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceNewCategory), (String) categoryData.getUniqueId(),
				GraphEdgeLabels.SUB_CATEGORY, NodeTypeEnum.ResourceSubcategory, SubCategoryData.class);
		if (childrenNodes.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(childrenNodes.right().value()));
		}
		return Either.left(childrenNodes.left().value());
	}

	@Override
	public <T> Either<List<T>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters, boolean inTransaction) {

		String subCategoryName = filters.get(FilterKeyEnum.SUB_CATEGORY);
		String categoryName = filters.get(FilterKeyEnum.CATEGORY);
		ResourceTypeEnum resourceType = ResourceTypeEnum.getType( filters.get(FilterKeyEnum.RESOURCE_TYPE));
		Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, StorageOperationStatus> subcategories = null;
		Optional<ImmutablePair<SubCategoryData, GraphEdge>> subCategoryData;

		if (categoryName != null) {
			subcategories = getAllSubCategories(categoryName);
			if (subcategories.isRight()) {
				filters.remove(FilterKeyEnum.SUB_CATEGORY);
				return Either.right(subcategories.right().value());
			}
		}
		if (subCategoryName != null) { // primary filter
			if (categoryName != null) {
				subCategoryData = validateCategoryHierarcy(subcategories.left().value(), subCategoryName);
				if (!subCategoryData.isPresent()) {
					return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
				}
				return fetchByCategoryOrSubCategoryUid((String) subCategoryData.get().getLeft().getUniqueId(), NodeTypeEnum.ResourceSubcategory, GraphEdgeLabels.SUB_CATEGORY.getProperty(), NodeTypeEnum.Resource, inTransaction,
						ResourceMetadataData.class, resourceType);
			}

			return fetchByCategoryOrSubCategoryName(subCategoryName, NodeTypeEnum.ResourceSubcategory, GraphEdgeLabels.SUB_CATEGORY.getProperty(), NodeTypeEnum.Resource, inTransaction, ResourceMetadataData.class, resourceType);
		}
		if(subcategories != null){
			return fetchByMainCategory(subcategories.left().value(), inTransaction, resourceType);
		}
		return fetchByResourceType(NodeTypeEnum.Resource, filters.get(FilterKeyEnum.RESOURCE_TYPE), ResourceMetadataData.class, inTransaction);
	}

	@SuppressWarnings("unchecked")
	private <T, S extends ComponentMetadataData> Either<List<T>, StorageOperationStatus> fetchByResourceType(NodeTypeEnum nodeType, String resourceType,
			Class<S> clazz, boolean inTransaction) {
		List<T> components = null;
		TitanOperationStatus status;
		Wrapper<StorageOperationStatus> statusWrapper = new Wrapper<>();
		Either<List<T>, StorageOperationStatus> result;
		try {
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), resourceType);
			props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
			Either<List<S>, TitanOperationStatus> getResources = titanGenericDao.getByCriteria(nodeType, props, clazz);
			if (getResources.isRight()) {
				status = getResources.right().value();
				if(status != TitanOperationStatus.NOT_FOUND){
					statusWrapper.setInnerElement(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}else{
					components = new ArrayList<>();
				}
			}else{
				components = getResources.left().value().stream().
				map(c->(T)convertComponentMetadataDataToComponent(c)).collect(Collectors.toList());
			}
			if(!statusWrapper.isEmpty()){
				result = Either.right(statusWrapper.getInnerElement());
			}else{
				result = Either.left(components);
			}
			return result;
		} finally {
			if (!inTransaction) {
				titanGenericDao.commit();
			}
		}
	}

	private <T> Either<List<T>, StorageOperationStatus> fetchByMainCategory(List<ImmutablePair<SubCategoryData, GraphEdge>> subcategories, boolean inTransaction, ResourceTypeEnum resourceType) {
		List<T> components = new ArrayList<>();

		for (ImmutablePair<SubCategoryData, GraphEdge> subCategory : subcategories) {
			Either<List<T>, StorageOperationStatus> fetched = fetchByCategoryOrSubCategoryUid((String) subCategory.getLeft().getUniqueId(), NodeTypeEnum.ResourceSubcategory, GraphEdgeLabels.SUB_CATEGORY.getProperty(), NodeTypeEnum.Resource,
					inTransaction, ResourceMetadataData.class, resourceType);
			if (fetched.isRight()) {
				// return fetched;
				continue;
			}
			components.addAll(fetched.left().value());
		}
		return Either.left(components);
	}

	@Override
	public <T> Either<T, StorageOperationStatus> getLightComponent(String id, boolean inTransaction) {
		return getLightComponent(id, NodeTypeEnum.Resource, inTransaction);
	}

	// will be implement later
	@Override
	protected ComponentMetadataData getMetaDataFromComponent(Component component) {
		return getResourceMetaDataFromResource((Resource) component);
	}

	@Override
	public Either<Set<Resource>, StorageOperationStatus> getCatalogData(Map<String, Object> propertiesToMatch, boolean inTransaction) {
		return getComponentCatalogData(NodeTypeEnum.Resource, propertiesToMatch, Resource.class, ResourceMetadataData.class, inTransaction);
	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getAllDerivedResources(Resource resource) {
		try {
			Either<List<ImmutablePair<ResourceMetadataData, GraphEdge>>, TitanOperationStatus> childrenNodes = getDerivingChildren(resource);
			return childrenNodes.either((childrenPairs) -> convertToResources(PairUtils.leftSequence(childrenPairs)),
										(status) -> Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status)));
		} finally {
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getRootResources() {
		Map<String, Object> rootToscaResource = new HashMap<>();
		rootToscaResource.put(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty(), Resource.ROOT_RESOURCE);
		return getResourceListByCriteria(rootToscaResource, false);
	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getVFResources() {
		Map<String, Object> rootToscaResource = new HashMap<>();
		rootToscaResource.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF);
		return getResourceListByCriteria(rootToscaResource, false);
	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getAll() {
		Either<List<Resource>, StorageOperationStatus> resourceListByCriteria = getResourceListByCriteria(new HashMap<>(), false);
		if (resourceListByCriteria.isRight() && resourceListByCriteria.right().value() == StorageOperationStatus.NOT_FOUND) {
			return Either.left(Collections.emptyList());
		}
		return resourceListByCriteria;
	}


	private Either<List<ImmutablePair<ResourceMetadataData, GraphEdge>>, TitanOperationStatus> getDerivingChildren(Resource resource) {
		return titanGenericDao.getParentNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resource.getUniqueId(), GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource, ResourceMetadataData.class);
	}

	private Either<List<Resource>, StorageOperationStatus> convertToResources(List<ResourceMetadataData> resourcesMetaData) {
		List<Either<Resource, StorageOperationStatus>> resources = resourcesMetaData.stream()
				.map(resourceMetaData -> this.getResource(resourceMetaData.getMetadataDataDefinition().getUniqueId()))
				.collect(Collectors.toList());
		return Either.sequenceLeft(fj.data.List.iterableList(resources)).bimap(fj.data.List::toJavaList, Function.identity());
	}

	protected TitanOperationStatus findResourcesPathRecursively(String resourceId, List<ResourceMetadataData> resourcesPathList) {

		Either<ResourceMetadataData, TitanOperationStatus> nodeRes = this.titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, ResourceMetadataData.class);

		if (nodeRes.isRight()) {
			TitanOperationStatus status = nodeRes.right().value();
			log.error("Failed to fetch resource {} . status is {}", resourceId, status);
			return status;
		}

		ResourceMetadataData resourceData = nodeRes.left().value();
		resourcesPathList.add(resourceData);
		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentResourceRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM,
				NodeTypeEnum.Resource, ResourceMetadataData.class);

		while (parentResourceRes.isLeft()) {

			ImmutablePair<ResourceMetadataData, GraphEdge> value = parentResourceRes.left().value();
			ResourceMetadataData parentResourceData = value.getKey();

			resourcesPathList.add(parentResourceData);

			parentResourceRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), parentResourceData.getMetadataDataDefinition().getUniqueId(), GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
					ResourceMetadataData.class);
		}

		TitanOperationStatus operationStatus = parentResourceRes.right().value();

		if (operationStatus != TitanOperationStatus.NOT_FOUND) {
			return operationStatus;
		} else {
			return TitanOperationStatus.OK;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> updateComponent(T component, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) updateResource((Resource) component, inTransaction);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<Component, StorageOperationStatus> deleteComponent(String id, boolean inTransaction) {
		return (Either<Component, StorageOperationStatus>) (Either<?, StorageOperationStatus>) deleteResource(id, inTransaction);
	}

	@Override
	public Either<Resource, StorageOperationStatus> getLatestByToscaResourceName(String toscaResourceName, boolean inTransaction) {
		return getLatestByName(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty(), toscaResourceName, inTransaction);

	}

	@Override
	public Either<Resource, StorageOperationStatus> getLatestByName(String resourceName, boolean inTransaction) {
		return getLatestByName(GraphPropertiesDictionary.NAME.getProperty(), resourceName, inTransaction);

	}

	private Either<Resource, StorageOperationStatus> getLatestByName(String property, String resourceName, boolean inTransaction) {
		Either<Resource, StorageOperationStatus> result = null;
		try {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(property, resourceName);
			props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

			Either<List<ResourceMetadataData>, TitanOperationStatus> highestResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
			if (highestResources.isRight()) {
				TitanOperationStatus status = highestResources.right().value();
				log.debug("failed to find resource with name {}. status={} ", resourceName, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			List<ResourceMetadataData> resources = highestResources.left().value();
			double version = 0.0;
			ResourceMetadataData highestResource = null;
			for (ResourceMetadataData resource : resources) {
				double resourceVersion = Double.parseDouble(resource.getMetadataDataDefinition().getVersion());
				if (resourceVersion > version) {
					version = resourceVersion;
					highestResource = resource;
				}
			}
			result = getResource(highestResource.getMetadataDataDefinition().getUniqueId(), true);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("getLatestByName operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("getLatestByName operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<List<Resource>, StorageOperationStatus> getTesterFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, boolean inTransaction) {
		return (Either<List<Resource>, StorageOperationStatus>) (Either<?, StorageOperationStatus>) getTesterFollowedComponent(userId, lifecycleStates, inTransaction, NodeTypeEnum.Resource);
	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getResourceCatalogData(boolean inTransaction) {
		return getResourceCatalogData(inTransaction, null);
	}

	private Either<List<Resource>, StorageOperationStatus> getResourceCatalogData(boolean inTransaction, Map<String, Object> otherToMatch) {

		long start = System.currentTimeMillis();

		long startFetchAllStates = System.currentTimeMillis();
		Map<String, Object> propertiesToMatchHigest = new HashMap<>();
		propertiesToMatchHigest.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
		propertiesToMatchHigest.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), false);
		Either<List<ResourceMetadataData>, TitanOperationStatus> allHighestStates = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, propertiesToMatchHigest, ResourceMetadataData.class);
		if (allHighestStates.isRight() && allHighestStates.right().value() != TitanOperationStatus.NOT_FOUND) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(allHighestStates.right().value()));
		}

		if (allHighestStates.isRight()) {
			return Either.left(new ArrayList<>());
		}
		List<ResourceMetadataData> list = allHighestStates.left().value();

		List<ResourceMetadataData> certified = new ArrayList<>();
		List<ResourceMetadataData> noncertified = new ArrayList<>();
		for (ResourceMetadataData reData : list) {
			if (reData.getMetadataDataDefinition().getState().equals(LifecycleStateEnum.CERTIFIED.name())) {
				certified.add(reData);
			} else {
				noncertified.add(reData);
			}
		}

		long endFetchAll = System.currentTimeMillis();
		log.debug("Fetch catalog resources all states: certified {}, noncertified {}", certified.size(), noncertified.size());
		log.debug("Fetch catalog resources all states from graph took {} ms", endFetchAll - startFetchAllStates);

		try {
			List<ResourceMetadataData> notCertifiedHighest = noncertified;
			List<ResourceMetadataData> certifiedHighestList = certified;

			HashMap<String, String> VFNames = new HashMap<>();
			HashMap<String, String> VFCNames = new HashMap<>();
			for (ResourceMetadataData data : notCertifiedHighest) {
				String serviceName = data.getMetadataDataDefinition().getName();
				if (((ResourceMetadataDataDefinition) data.getMetadataDataDefinition()).getResourceType().equals(ResourceTypeEnum.VF)) {
					VFNames.put(serviceName, serviceName);
				} else {
					VFCNames.put(serviceName, serviceName);
				}
			}

			for (ResourceMetadataData data : certifiedHighestList) {
				String serviceName = data.getMetadataDataDefinition().getName();
				if (((ResourceMetadataDataDefinition) data.getMetadataDataDefinition()).getResourceType().equals(ResourceTypeEnum.VF)) {
					if (!VFNames.containsKey(serviceName)) {
						notCertifiedHighest.add(data);
					}
				} else {
					if (!VFCNames.containsKey(serviceName)) {
						notCertifiedHighest.add(data);
					}
				}
			}

			long endFetchAllFromGraph = System.currentTimeMillis();
			log.debug("Fetch all catalog resources metadata from graph took {} ms", endFetchAllFromGraph - start);

			long startFetchAllFromCache = System.currentTimeMillis();

			List<Resource> result = new ArrayList<>();

			Map<String, Long> components = notCertifiedHighest.stream().collect(Collectors.toMap(p -> p.getMetadataDataDefinition().getUniqueId(), p -> p.getMetadataDataDefinition().getLastUpdateDate()));

			Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> componentsForCatalog = componentCache.getComponentsForCatalog(components, ComponentTypeEnum.RESOURCE);
			if (componentsForCatalog.isLeft()) {
				ImmutablePair<List<Component>, Set<String>> immutablePair = componentsForCatalog.left().value();
				List<Component> foundComponents = immutablePair.getLeft();
				if (foundComponents != null) {
					foundComponents.forEach(p -> result.add((Resource) p));
					log.debug("The number of resources added to catalog from cache is {}", foundComponents.size());

					List<String> foundComponentsUid = foundComponents.stream().map(p -> p.getUniqueId()).collect(Collectors.toList());
					notCertifiedHighest = notCertifiedHighest.stream().filter(p -> false == foundComponentsUid.contains(p.getUniqueId())).collect(Collectors.toList());
				}
				Set<String> nonCachedComponents = immutablePair.getRight();
				int numberNonCached = nonCachedComponents == null ? 0 : nonCachedComponents.size();
				log.debug("The number of left resources for catalog is {}", numberNonCached);

			}

			long endFetchAllFromCache = System.currentTimeMillis();
			log.debug("Fetch all catalog resources metadata from cache took {} ms", (endFetchAllFromCache - startFetchAllFromCache));

			long startFetchFromGraph = System.currentTimeMillis();
			log.debug("The number of resources needed to be fetch as light component is {}", notCertifiedHighest.size());
			for (ResourceMetadataData data : notCertifiedHighest) {
				String uniqueId = data.getMetadataDataDefinition().getUniqueId();
				log.trace("Fetch catalog resource non cached {} {}", uniqueId, data.getMetadataDataDefinition().getName());
				Either<Resource, StorageOperationStatus> component = getLightComponent(uniqueId, inTransaction);
				if (component.isRight()) {
					log.debug("Failed to get Service for id =  {}  error : {} skip resource", data.getUniqueId(), component.right().value());
				} else {
					result.add(component.left().value());
				}
			}
			long endFetchFromGraph = System.currentTimeMillis();
			log.debug("Fetch catalog resources from graph took {} ms", (endFetchFromGraph - startFetchFromGraph));

			return Either.left(result);

		} finally {
			long end = System.currentTimeMillis();
			log.debug("Fetch all catalog resources took {} ms", end - start);
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}
	}

	public Either<List<Resource>, StorageOperationStatus> getResourceCatalogDataVFLatestCertifiedAndNonCertified(boolean inTransaction) {
		Map<String, Object> propertiesToMatch = new HashMap<>();
		propertiesToMatch.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());

		return getResourceCatalogDataLatestCertifiedAndNonCertified(inTransaction, propertiesToMatch);
	}

	public Either<List<Resource>, StorageOperationStatus> getResourceCatalogDataLatestCertifiedAndNonCertified(boolean inTransaction, Map<String, Object> otherToMatch) {
		Map<String, Object> propertiesToMatch = new HashMap<>();

		if (otherToMatch != null) {
			propertiesToMatch.putAll(otherToMatch);
		}

		propertiesToMatch.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

		Either<List<ResourceMetadataData>, TitanOperationStatus> lastVersionNodes = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, propertiesToMatch, ResourceMetadataData.class);

		List<Resource> result = new ArrayList<>();

		if (lastVersionNodes.isRight() && lastVersionNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(lastVersionNodes.right().value()));
		}

		List<ResourceMetadataData> listOfHighest;

		if (lastVersionNodes.isLeft()) {
			listOfHighest = lastVersionNodes.left().value();
		} else {
			return Either.left(result);
		}

		for (ResourceMetadataData data : listOfHighest) {
			Either<Resource, StorageOperationStatus> component = getLightComponent(data.getMetadataDataDefinition().getUniqueId(), inTransaction);
			if (component.isRight()) {
				log.debug("Failed to get Service for id =  {} error : {} skip resource", data.getUniqueId(), component.right().value());
			} else {
				result.add(component.left().value());
			}
		}
		return Either.left(result);
	}

	private Either<List<Resource>, StorageOperationStatus> getResourceListByCriteria(Map<String, Object> props, boolean inTransaction) {

		props.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Resource.getName());

		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);

		if (byCriteria.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(byCriteria.right().value()));
		}
		List<Resource> resources = new ArrayList<Resource>();
		List<ResourceMetadataData> resourcesDataList = byCriteria.left().value();
		for (ResourceMetadataData data : resourcesDataList) {
			Either<Resource, StorageOperationStatus> resource = getResource(data.getMetadataDataDefinition().getUniqueId(), inTransaction);
			if (resource.isLeft()) {
				resources.add(resource.left().value());
			} else {
				log.debug("Failed to fetch resource for name = {} and id = {}", data.getUniqueId(), data.getMetadataDataDefinition().getName());
			}
		}
		return Either.left(resources);
	}

	public Either<List<Resource>, StorageOperationStatus> getResourceListByUuid(String uuid, boolean inTransaction) {
		return getLatestResourceByUuid(uuid, false, inTransaction);
	}

	public Either<List<Resource>, StorageOperationStatus> getLatestResourceByUuid(String uuid, boolean inTransaction) {
		return getLatestResourceByUuid(uuid, true, inTransaction);
	}

	private Either<List<Resource>, StorageOperationStatus> getLatestResourceByUuid(String uuid, boolean isLatest, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		if (isLatest) {
			props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), isLatest);
		}
		props.put(GraphPropertiesDictionary.UUID.getProperty(), uuid);
		return getResourceListByCriteria(props, inTransaction);
	}

	public Either<List<Resource>, StorageOperationStatus> getResourceListBySystemName(String systemName, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.SYSTEM_NAME.getProperty(), systemName);
		return getResourceListByCriteria(props, inTransaction);
	}

	public Either<List<Resource>, StorageOperationStatus> getResourceListByToscaName(String toscaName, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty(), toscaName);
		return getResourceListByCriteria(props, inTransaction);
	}

	public Either<List<Resource>, StorageOperationStatus> getResourceByNameAndVersion(String name, String version, boolean inTransaction) {
		return getByNamesAndVersion(GraphPropertiesDictionary.NAME.getProperty(), name, version, null, inTransaction);
	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getResourceByNameAndVersion(String name, String version) {
		return getResourceByNameAndVersion(name, version, false);
	}

	protected Either<List<Resource>, StorageOperationStatus> getByNamesAndVersion(String nameKey, String nameValue, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(nameKey, nameValue);
		props.put(GraphPropertiesDictionary.VERSION.getProperty(), version);
		props.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Resource.getName());
		if (additionalParams != null && !additionalParams.isEmpty()) {
			props.putAll(additionalParams);
		}

		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		List<Resource> resourcesList = new ArrayList<Resource>();
		if (byCriteria.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(byCriteria.right().value()));
		}
		List<ResourceMetadataData> dataList = byCriteria.left().value();
		if (dataList != null && !dataList.isEmpty()) {
			for (ResourceMetadataData resourceData : dataList) {
				// ResourceMetadataData resourceData = dataList.get(0);
				Either<Resource, StorageOperationStatus> resource = getResource(resourceData.getMetadataDataDefinition().getUniqueId(), inTransaction);
				if (resource.isRight()) {
					log.debug("Failed to fetch resource for name = {} and id = {}", resourceData.getMetadataDataDefinition().getName(), resourceData.getUniqueId());
					return Either.right(resource.right().value());
				}
				resourcesList.add(resource.left().value());
			}
			// return resource;
			return Either.left(resourcesList);
		} else {
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
	}

	@Override
	protected <T> Either<T, StorageOperationStatus> getComponentByNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) getResourceBySystemNameAndVersion(name, version, additionalParams, inTransaction);
	}

	@Override
	public Either<Resource, StorageOperationStatus> getResourceBySystemNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		Either<List<Resource>, StorageOperationStatus> byNamesAndVersion = getByNamesAndVersion(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normaliseComponentName(name), version, additionalParams, inTransaction);
		if (byNamesAndVersion.isRight()) {
			return Either.right(byNamesAndVersion.right().value());
		}
		List<Resource> resourcesList = byNamesAndVersion.left().value();
		if (resourcesList.size() > 1) {
			log.debug("More that one instance of resource for name = {} and version = {}", name, version);
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		return Either.left(resourcesList.get(0));
	}

	private TitanOperationStatus setAllVersions(Resource resource) {
		Either<Map<String, String>, TitanOperationStatus> res = getVersionList(NodeTypeEnum.Resource, resource.getVersion(), resource, ResourceMetadataData.class);
		if (res.isRight()) {
			return res.right().value();
		}
		resource.setAllVersions(res.left().value());
		return TitanOperationStatus.OK;
	}

	@Override
	protected <T extends GraphNode> Either<Map<String, String>, TitanOperationStatus> getVersionList(NodeTypeEnum type, String version, Component component, Class<T> clazz) {
		Map<String, Object> props = new HashMap<String, Object>();
		Map<String, Object> hasNotProps = new HashMap<String, Object>();

		if (version.startsWith("0")) {
			props.put(GraphPropertiesDictionary.UUID.getProperty(), component.getUUID());
		} else {
			props.put(GraphPropertiesDictionary.SYSTEM_NAME.getProperty(), component.getSystemName());
			props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ((Resource) component).getResourceType().name());
		}
		hasNotProps.put(GraphPropertiesDictionary.IS_DELETED.getProperty(), true);
		Either<List<T>, TitanOperationStatus> result = titanGenericDao.getByCriteria(type, props, hasNotProps, clazz);

		Map<String, String> versionMap = new HashMap<String, String>();
		if (result.isRight()) {
			if (!result.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				return Either.right(result.right().value());
			}

		} else {
			List<ResourceMetadataData> components = (List<ResourceMetadataData>) result.left().value();
			for (ResourceMetadataData data : components) {
				versionMap.put(data.getMetadataDataDefinition().getVersion(), (String) data.getUniqueId());
			}
		}

		return Either.left(versionMap);
	}

	/**
	 * update only the resource object itself without tag, derived from or any other neighbours.
	 * 
	 * @param resource
	 * @param inTransaction
	 * @return
	 */
	protected Either<Resource, StorageOperationStatus> updateResourceMetadata(Resource resource, boolean inTransaction) {

		Either<Resource, StorageOperationStatus> result = null;

		try {

			log.debug("In updateResource. received resource = {}", (resource == null ? null : resource.toString()));
			if (resource == null) {
				log.error("Resource object is null");
				result = Either.right(StorageOperationStatus.BAD_REQUEST);
				return result;
			}

			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(resource.getUniqueId());
			resourceData.getMetadataDataDefinition().setHighestVersion(resource.isHighestVersion());
			log.debug("After converting resource to ResourceData. ResourceData = {}", resourceData);

			if (resourceData.getUniqueId() == null) {
				log.error("Resource id is missing in the request.");
				return Either.right(StorageOperationStatus.BAD_REQUEST);
			}

			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(resourceData, ResourceMetadataData.class);

			if (updateNode.isRight()) {
				log.error("Failed to update resource {}. status is {}", resource.getUniqueId(), updateNode.right().value());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
				return result;
			}

			Either<Resource, StorageOperationStatus> updatedResource = getResource(resource.getUniqueId(), true);
			if (updatedResource.isRight()) {
				log.error("Resource id is missing in the request. status is {}", updatedResource.right().value());
				result = Either.right(StorageOperationStatus.BAD_REQUEST);
				return result;
			}

			Resource updatedResourceValue = updatedResource.left().value();
			result = Either.left(updatedResourceValue);

			if (log.isDebugEnabled()) {
				String json = prettyJson.toJson(result.left().value());
				log.debug("Resource retrieved after update is {}", json);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	@Override
	public Either<List<Resource>, StorageOperationStatus> getAllCertifiedResources(boolean isAbstract, Boolean isHighest) {

		try {
			List<Resource> result = new ArrayList<>();
			Map<String, Object> propertiesToMatch = new HashMap<>();
			propertiesToMatch.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), isAbstract);
			propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());

			if (isHighest != null) {
				propertiesToMatch.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), isHighest.booleanValue());
			}

			Either<List<ResourceMetadataData>, TitanOperationStatus> resourceNodes = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, propertiesToMatch, ResourceMetadataData.class);

			titanGenericDao.commit();
			if (resourceNodes.isRight()) {
				// in case of NOT_FOUND from Titan client return to UI empty
				// list
				if (resourceNodes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					return Either.left(result);
				} else {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resourceNodes.right().value()));
				}
			} else {
				List<ResourceMetadataData> resourceDataList = resourceNodes.left().value();
				for (ResourceMetadataData resourceData : resourceDataList) {
					Either<Resource, StorageOperationStatus> resource = getResource(resourceData.getMetadataDataDefinition().getUniqueId());
					if (resource.isRight()) {
						log.debug("Failed to fetch resource for id = {} error is {}", resourceData.getUniqueId(), resource.right().value());
						return Either.right(resource.right().value());
					}
					result.add(resource.left().value());
				}
				return Either.left(result);
			}
		} finally {
			titanGenericDao.commit();
		}

	}

	public Either<Resource, StorageOperationStatus> getLatestCertifiedByToscaResourceName(String toscaResourceName, boolean inTransaction) {
		return getLatestCertifiedByCriteria(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty(), toscaResourceName, inTransaction);

	}

	public Either<Resource, StorageOperationStatus> getLatestCertifiedByCriteria(String property, String resourceName, boolean inTransaction) {
		Either<Resource, StorageOperationStatus> result = null;
		try {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(property, resourceName);
			props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
			props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());

			Either<List<ResourceMetadataData>, TitanOperationStatus> highestResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
			if (highestResources.isRight()) {
				TitanOperationStatus status = highestResources.right().value();
				log.debug("failed to find resource with name {}. status={} ", resourceName, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			List<ResourceMetadataData> resources = highestResources.left().value();
			double version = 0.0;
			ResourceMetadataData highestResource = null;
			for (ResourceMetadataData resource : resources) {
				double resourceVersion = Double.parseDouble(resource.getMetadataDataDefinition().getVersion());
				if (resourceVersion > version) {
					version = resourceVersion;
					highestResource = resource;
				}
			}
			result = getResource(highestResource.getMetadataDataDefinition().getUniqueId(), true);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("getLatestByName operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("getLatestByName operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}

		}
	}

	public Either<List<Resource>, StorageOperationStatus> findLastCertifiedResourceByName(Resource resource) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), resource.getName());
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());
		return getResourceListByCriteria(props, false);

	}

	public Either<List<Resource>, StorageOperationStatus> findLastCertifiedResourceByUUID(Resource resource) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.UUID.getProperty(), resource.getUUID());
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());
		return getResourceListByCriteria(props, false);

	}

	@Override
	public boolean isComponentExist(String resourceId) {
		return isComponentExist(resourceId, NodeTypeEnum.Resource);
	}

	@Override
	public <T> Either<T, StorageOperationStatus> cloneComponent(T other, String version, LifecycleStateEnum targetLifecycle, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) cloneResource((Resource) other, version, targetLifecycle, inTransaction);
	}

	@Override
	public Either<Integer, StorageOperationStatus> increaseAndGetComponentInstanceCounter(String componentId, boolean inTransaction) {
		return increaseAndGetComponentInstanceCounter(componentId, NodeTypeEnum.Resource, inTransaction);
	}

	private Either<Resource, StorageOperationStatus> cloneResource(Resource other, String version, boolean inTransaction) {
		return cloneResource(other, version, null, inTransaction);
	}

	private Either<Resource, StorageOperationStatus> cloneResource(Resource other, String version, LifecycleStateEnum targetLifecycle, boolean inTransaction) {
		Either<Resource, StorageOperationStatus> result = null;

		try {
			String origRsourceId = other.getUniqueId();

			StorageOperationStatus overrideStatus = overrideRecursiveMembers(other, origRsourceId);
			if (!overrideStatus.equals(StorageOperationStatus.OK)) {
				return Either.right(overrideStatus);
			}
			other.setVersion(version);
			other.setUniqueId(null);
			
			List<InputDefinition> inputs = other.getInputs();
			Map<String, List<ComponentInstanceProperty>> inputsPropMap = new HashMap<String, List<ComponentInstanceProperty>>();
			
			if(inputs != null){
				for(InputDefinition input: inputs){
					
					Either<List<ComponentInstanceProperty>, TitanOperationStatus> inputPropStatus  = inputOperation.getComponentInstancePropertiesByInputId(input.getUniqueId());
					if(inputPropStatus.isLeft()){
						if(inputPropStatus.left().value() != null)
							inputsPropMap.put(input.getName(), inputPropStatus.left().value());
						
					}
					
					
				}
			}

			Either<Resource, StorageOperationStatus> createResourceMD = createResource(other, inTransaction);
			if (createResourceMD.isRight()) {
				StorageOperationStatus status = createResourceMD.right().value();
				log.error("failed to clone resource. status= {}", status);
				result = Either.right(status);
				return result;
			}
			Resource resource = createResourceMD.left().value();
			Either<TitanVertex, TitanOperationStatus> metadataVertexEither = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), resource.getUniqueId());
			if (metadataVertexEither.isRight()) {
				TitanOperationStatus error = metadataVertexEither.right().value();
				log.debug("Failed to fetch vertex of metadata {} error {}", resource.getUniqueId(), error);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(error));
				return result;
			}

			TitanVertex metadataVertex = metadataVertexEither.left().value();
			Either<ImmutablePair<List<ComponentInstance>, Map<String, String>>, StorageOperationStatus> cloneInstances = componentInstanceOperation.cloneAllComponentInstancesFromContainerComponent(origRsourceId, resource.getUniqueId(),
					NodeTypeEnum.Resource, NodeTypeEnum.Resource, targetLifecycle, metadataVertex, other, resource, inputsPropMap);
			if (cloneInstances.isRight()) {
				result = Either.right(cloneInstances.right().value());
				return result;
			}

			Either<Integer, StorageOperationStatus> counterStatus = getComponentInstanceCoutner(origRsourceId, NodeTypeEnum.Resource);
			if (counterStatus.isRight()) {
				StorageOperationStatus status = counterStatus.right().value();
				log.error("failed to get resource instance counter on service {}. status={}", origRsourceId, counterStatus);
				result = Either.right(status);
				return result;
			}

			Either<Integer, StorageOperationStatus> setResourceInstanceCounter = setComponentInstanceCounter(resource.getUniqueId(), NodeTypeEnum.Resource, counterStatus.left().value(), true);
			if (setResourceInstanceCounter.isRight()) {
				StorageOperationStatus status = setResourceInstanceCounter.right().value();
				log.error("failed to set resource instance counter on service {}. status={}", resource.getUniqueId(), status);
				result = Either.right(status);
				return result;
			}

			Either<List<GroupDefinition>, StorageOperationStatus> clonedGroups = cloneGroups(other, resource, cloneInstances.left().value(), true);
			if (clonedGroups.isRight()) {
				StorageOperationStatus status = clonedGroups.right().value();
				if (status != StorageOperationStatus.OK) {
					result = Either.right(status);
					return result;
				}
			}

			result = this.getResource(resource.getUniqueId(), true);
			if (result.isRight()) {
				log.error("Cannot get full service from the graph. status is {}", result.right().value());
				return Either.right(result.right().value());
			}

			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private StorageOperationStatus overrideRecursiveMembers(Resource resource, String prevId) {
		// override requirements to copy only resource's requirements and not
		// derived requirements
		Either<Map<String, List<RequirementDefinition>>, StorageOperationStatus> requirementsOfResourceOnly = getRequirementOperation().getAllRequirementsOfResourceOnly(prevId, true);
		if (requirementsOfResourceOnly.isRight()) {
			log.error("failed to get requirements of resource. resourceId {} status is {}", prevId, requirementsOfResourceOnly.right().value());
			return requirementsOfResourceOnly.right().value();
		}
		resource.setRequirements(requirementsOfResourceOnly.left().value());

		// override capabilities to copy only resource's requirements and not
		// derived requirements
		Either<Map<String, List<CapabilityDefinition>>, StorageOperationStatus> capabilitiesOfResourceOnly = getResourceCapabilitiesMap(prevId);

		resource.setCapabilities(capabilitiesOfResourceOnly.left().value());

		// override interfaces to copy only resource's interfaces and not
		// derived interfaces
		Either<Map<String, InterfaceDefinition>, StorageOperationStatus> interfacesOfResourceOnly = getInterfaceLifecycleOperation().getAllInterfacesOfResource(prevId, false, true);
		if (interfacesOfResourceOnly.isRight()) {
			log.error("failed to get interfaces of resource. resourceId {} status is {}", prevId, interfacesOfResourceOnly.right().value());
			return interfacesOfResourceOnly.right().value();
		}
		resource.setInterfaces(interfacesOfResourceOnly.left().value());

		List<AttributeDefinition> attributes = new ArrayList<>();
		TitanOperationStatus status = attributeOperation.findNodeNonInheretedAttribues(prevId, NodeTypeEnum.Resource, attributes);
		if (status != TitanOperationStatus.OK) {
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		} else {
			resource.setAttributes(attributes);
		}

		// override properties to copy only resource's properties and not
		// derived properties
		Either<Map<String, PropertyDefinition>, TitanOperationStatus> propertiesOfResourceOnly = getPropertyOperation().findPropertiesOfNode(NodeTypeEnum.Resource, prevId);

		List<PropertyDefinition> resourceProperties = null;
		if (propertiesOfResourceOnly.isRight()) {
			TitanOperationStatus titanStatus = propertiesOfResourceOnly.right().value();
			if (titanStatus != TitanOperationStatus.NOT_FOUND) {
				log.error("failed to get properties of resource. resourceId {} status is {}", prevId, propertiesOfResourceOnly.right().value());
				return DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
			}
		} else {
			Map<String, PropertyDefinition> propertiesMap = propertiesOfResourceOnly.left().value();
			if (propertiesMap != null) {
				resourceProperties = new ArrayList<PropertyDefinition>();
				resourceProperties.addAll(propertiesMap.values());
			}
		}
		resource.setProperties(resourceProperties);

		return StorageOperationStatus.OK;
	}

	private Either<Map<String, List<CapabilityDefinition>>, StorageOperationStatus> getResourceCapabilitiesMap(String prevId) {

		Either<Map<String, CapabilityDefinition>, StorageOperationStatus> capabilitiesOfResourceOnly = getCapabilityOperation().getAllCapabilitiesOfResource(prevId, false, true);
		if (capabilitiesOfResourceOnly.isRight()) {
			log.error("failed to get capabilities of resource. resourceId {} status is {}", prevId, capabilitiesOfResourceOnly.right().value());
			return Either.right(capabilitiesOfResourceOnly.right().value());
		}
		Map<String, List<CapabilityDefinition>> capabilityMap = getCapabilityOperation().convertCapabilityMap(capabilitiesOfResourceOnly.left().value(), null, null);
		return Either.left(capabilityMap);
	}

	@Override
	protected StorageOperationStatus validateCategories(Component currentComponent, Component component, ComponentMetadataData componentData, NodeTypeEnum type) {
		StorageOperationStatus status = StorageOperationStatus.OK;
		List<CategoryDefinition> newCategoryList = component.getCategories();
		CategoryDefinition newCategory = newCategoryList.get(0);
		CategoryDefinition currentCategory = currentComponent.getCategories().get(0);
		boolean categoryWasChanged = false;

		if (newCategory.getName() != null && false == newCategory.getName().equals(currentCategory.getName())) {
			// the category was changed
			categoryWasChanged = true;
		} else {
			// the sub-category was changed
			SubCategoryDefinition currSubcategory = currentCategory.getSubcategories().get(0);
			SubCategoryDefinition newSubcategory = newCategory.getSubcategories().get(0);
			if (newSubcategory.getName() != null && false == newSubcategory.getName().equals(currSubcategory.getName())) {
				log.debug("Going to update the category of the resource from {} to {}", currentCategory, newCategory);
				categoryWasChanged = true;
			}
		}
		if (categoryWasChanged) {
			status = moveCategoryEdge((Resource) component, (ResourceMetadataData) componentData, newCategory);
			log.debug("Going to update the category of the resource from {} to {}. status is {}", currentCategory, newCategory, status);
		}
		return status;
	}

	@Override
	public Resource getDefaultComponent() {
		return new Resource();
	}

	@Override
	public Either<Component, StorageOperationStatus> getMetadataComponent(String id, boolean inTransaction) {
		return getMetadataComponent(id, NodeTypeEnum.Resource, inTransaction);
	}

	@Override
	Component convertComponentMetadataDataToComponent(ComponentMetadataData componentMetadataData) {
		return convertResourceDataToResource((ResourceMetadataData) componentMetadataData);
	}

	@Override
	public Either<Boolean, StorageOperationStatus> validateComponentNameExists(String componentName) {
		return validateComponentNameUniqueness(componentName, titanGenericDao, NodeTypeEnum.Resource);
	}

	@Override
	public Either<Component, StorageOperationStatus> markComponentToDelete(Component componentToDelete, boolean inTransaction) {
		return internalMarkComponentToDelete(componentToDelete, inTransaction);
	}

	@Override
	public Either<Boolean, StorageOperationStatus> isComponentInUse(String componentId) {
		return isResourceInUse(componentId);
	}

	@Override
	public Either<List<String>, StorageOperationStatus> getAllComponentsMarkedForDeletion() {
		return getAllResourcesMarkedForDeletion();
	}

	@Override
	public Either<List<String>, StorageOperationStatus> getAllResourcesMarkedForDeletion() {
		return getAllComponentsMarkedForDeletion(NodeTypeEnum.Resource);
	}

	@Override
	public Either<Boolean, StorageOperationStatus> isResourceInUse(String resourceToDelete) {
		return isComponentInUse(resourceToDelete, NodeTypeEnum.Resource);
	}

	public Either<List<GroupDefinition>, StorageOperationStatus> cloneGroups(Resource resource, Resource newResource, ImmutablePair<List<ComponentInstance>, Map<String, String>> cloneInstances, boolean inTransaction) {

		Either<List<GroupDefinition>, StorageOperationStatus> result = null;

		if (resource.getGroups() == null) {
			return Either.right(StorageOperationStatus.OK);
		}

		Either<List<GroupDefinition>, StorageOperationStatus> prepareGroupsForCloning = groupOperation.prepareGroupsForCloning(resource, cloneInstances);
		if (prepareGroupsForCloning.isRight()) {
			StorageOperationStatus status = prepareGroupsForCloning.right().value();
			if (status != StorageOperationStatus.OK) {
				BeEcompErrorManager.getInstance().logInternalFlowError("CloneResource", "Failed to prepare groups for cloning", ErrorSeverity.ERROR);
			}
			result = Either.right(status);
			return result;
		} else {
			List<GroupDefinition> groupsToCreate = prepareGroupsForCloning.left().value();
			if (groupsToCreate != null && false == groupsToCreate.isEmpty()) {
				Either<List<GroupDefinition>, StorageOperationStatus> addGroups = groupOperation.addGroups(NodeTypeEnum.Resource, newResource.getUniqueId(), groupsToCreate, inTransaction);
				if (addGroups.isRight()) {
					BeEcompErrorManager.getInstance().logInternalFlowError("CloneResource", "Failed to clone groups", ErrorSeverity.ERROR);
					result = Either.right(addGroups.right().value());
					return result;
				}

				return Either.left(addGroups.left().value());
			} else {
				return Either.right(StorageOperationStatus.OK);
			}
		}
	}

	public Either<Resource, StorageOperationStatus> getLatestResourceByCsarOrName(String csarUUID, String systemName) {
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.CSAR_UUID.getProperty(), csarUUID);
		props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
		ResourceMetadataData resourceMetadataData = null;
		List<ResourceMetadataData> resourceMetadataDataList = null;
		Either<List<ResourceMetadataData>, TitanOperationStatus> byCsar = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		if (byCsar.isRight()) {
			if (TitanOperationStatus.NOT_FOUND == byCsar.right().value()) {
				//Fix Defect DE256036 
				if( StringUtils.isEmpty(systemName)){
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_FOUND));
				}
				
				props.clear();
				props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
				props.put(GraphPropertiesDictionary.SYSTEM_NAME.getProperty(), systemName);
				Either<List<ResourceMetadataData>, TitanOperationStatus> bySystemname = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
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
					for (ResourceMetadataData curResource : resourceMetadataDataList) {
						if (!curResource.getMetadataDataDefinition().getState().equals("CERTIFIED")) {
							resourceMetadataData = curResource;
							break;
						}
					}
				}
				if (resourceMetadataData == null) {
					log.debug("getLatestResourceByCsarOrName - getByCriteria(by system name) returned 2 latest CERTIFIED versions");
					return Either.right(StorageOperationStatus.GENERAL_ERROR);
				}
				if (resourceMetadataData.getMetadataDataDefinition().getCsarUUID() != null && !resourceMetadataData.getMetadataDataDefinition().getCsarUUID().equals(csarUUID)) {
					log.debug("getLatestResourceByCsarOrName - same system name {} but different csarUUID. exist {} and new {} ", systemName, resourceMetadataData.getMetadataDataDefinition().getCsarUUID(), csarUUID);
					// correct error will be returned from create flow. with all
					// correct audit records!!!!!
					return Either.right(StorageOperationStatus.NOT_FOUND);
				}
				Either<Resource, StorageOperationStatus> resource = getResource((String) resourceMetadataData.getUniqueId());
				return resource;
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
				for (ResourceMetadataData curResource : resourceMetadataDataList) {
					if (!curResource.getMetadataDataDefinition().getState().equals("CERTIFIED")) {
						resourceMetadataData = curResource;
						break;
					}
				}
			}
			if (resourceMetadataData == null) {
				log.debug("getLatestResourceByCsarOrName - getByCriteria(by csar) returned 2 latest CERTIFIED versions");
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			Either<Resource, StorageOperationStatus> resource = getResource((String) resourceMetadataData.getMetadataDataDefinition().getUniqueId());
			return resource;
		}
		return null;
	}

	public Either<List<ResourceMetadataData>, StorageOperationStatus> validateCsarUuidUniqueness(String csarUUID) {

		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.CSAR_UUID.getProperty(), csarUUID);

		Either<List<ResourceMetadataData>, TitanOperationStatus> byCsar = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		if (byCsar.isRight()) {
			if (TitanOperationStatus.NOT_FOUND.equals(byCsar.right().value())) {
				return Either.left(null);
			} else {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(byCsar.right().value()));
			}
		}
		return Either.left(byCsar.left().value());
	}

	public Either<Resource, StorageOperationStatus> getResource(String uniqueId, ComponentParametersView componentParametersView, boolean inTransaction) {

		Resource resource = null;
		try {

			NodeTypeEnum resourceNodeType = NodeTypeEnum.Resource;
			NodeTypeEnum compInstNodeType = NodeTypeEnum.Resource;

			Either<ResourceMetadataData, StorageOperationStatus> componentByLabelAndId = getComponentByLabelAndId(uniqueId, resourceNodeType, ResourceMetadataData.class);
			if (componentByLabelAndId.isRight()) {
				return Either.right(componentByLabelAndId.right().value());
			}
			ResourceMetadataData resourceData = componentByLabelAndId.left().value();

			// Try to fetch resource from the cache. The resource will be
			// fetched only if the time on the cache equals to
			// the time on the graph.
			Either<Resource, ActionStatus> componentFromCacheIfUpToDate = this.getComponentFromCacheIfUpToDate(uniqueId, resourceData, componentParametersView, Resource.class, ComponentTypeEnum.RESOURCE);
			if (componentFromCacheIfUpToDate.isLeft()) {
				Resource cachedResource = componentFromCacheIfUpToDate.left().value();
				log.debug("Resource {} with uid {} was fetched from cache.", cachedResource.getName(), cachedResource.getUniqueId());
				return Either.left(cachedResource);
			}

			resource = convertResourceDataToResource(resourceData);

			TitanOperationStatus status = null;
			if (false == componentParametersView.isIgnoreUsers()) {
				status = setResourceCreatorFromGraph(resource, uniqueId);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}

				status = setResourceLastModifierFromGraph(resource, uniqueId);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			if (false == componentParametersView.isIgnoreProperties()) {
				status = setResourcePropertiesFromGraph(uniqueId, resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			if (false == componentParametersView.isIgnoreAttributesFrom()) {
				status = setResourceAttributesFromGraph(uniqueId, resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			if (false == componentParametersView.isIgnoreDerivedFrom()) {
				status = setResourceDerivedFromGraph(uniqueId, resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			if (false == componentParametersView.isIgnoreCategories()) {
				status = setComponentCategoriesFromGraph(resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			// Since capabilities and requirements and instances properties are
			// based on component instances, then we must fetch the instances.
			if (false == componentParametersView.isIgnoreComponentInstances() || false == componentParametersView.isIgnoreComponentInstancesProperties() || false == componentParametersView.isIgnoreComponentInstancesInputs()
					|| false == componentParametersView.isIgnoreCapabilities() || false == componentParametersView.isIgnoreRequirements()) {

				status = setComponentInstancesFromGraph(uniqueId, resource, resourceNodeType, compInstNodeType);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

				}
			}

			if (false == componentParametersView.isIgnoreRequirements()) {
				StorageOperationStatus setRequirementsStatus = setResourceRequirementsFromGraph(uniqueId, resource, true);
				if (setRequirementsStatus != StorageOperationStatus.OK) {
					log.error("Failed to set requirement of resource {}. status is {}", uniqueId, setRequirementsStatus);
					return Either.right(setRequirementsStatus);
				}
			}

			if (false == componentParametersView.isIgnoreInputs()) {
				status = setComponentInputsFromGraph(uniqueId, resource, true);
				if (status != TitanOperationStatus.OK) {
					log.error("Failed to set inputs of resource {}. status is {}", uniqueId, status);
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}

			}

			StorageOperationStatus storageStatus = null;
			if (false == componentParametersView.isIgnoreCapabilities()) {
				storageStatus = setResourceCapabilitiesFromGraph(uniqueId, resource);
				if (storageStatus != StorageOperationStatus.OK) {
					return Either.right(storageStatus);
				}
			}

			if (false == componentParametersView.isIgnoreArtifacts()) {
				storageStatus = setArtifactFromGraph(uniqueId, resource);
				if (storageStatus != StorageOperationStatus.OK) {
					return Either.right(storageStatus);
				}
			}
			if (false == componentParametersView.isIgnoreComponentInstancesAttributesFrom()) {
				status = setComponentInstancesAttributesFromGraph(uniqueId, resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

				}
			}

			if (false == componentParametersView.isIgnoreComponentInstancesProperties()) {
				status = setComponentInstancesPropertiesFromGraph(resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

				}
			}

			if (false == componentParametersView.isIgnoreComponentInstancesInputs()) {
				status = setComponentInstancesInputsFromGraph(uniqueId, resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

				}
			}

			if (false == componentParametersView.isIgnoreInterfaces()) {
				storageStatus = setResourceInterfacesFromGraph(uniqueId, resource);
				if (storageStatus != StorageOperationStatus.OK) {
					return Either.right(storageStatus);
				}
			}

			if (false == componentParametersView.isIgnoreAdditionalInformation()) {
				storageStatus = setResourceAdditionalInformationFromGraph(uniqueId, resource);
				if (storageStatus != StorageOperationStatus.OK) {
					return Either.right(storageStatus);
				}
			}

			if (false == componentParametersView.isIgnoreAllVersions()) {
				status = setAllVersions(resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			if (false == componentParametersView.isIgnoreGroups()) {
				status = setGroupsFromGraph(uniqueId, resource, NodeTypeEnum.Resource);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			if (true == componentParametersView.isIgnoreComponentInstances()) {
				resource.setComponentInstances(null);
				resource.setComponentInstancesRelations(null);
			}

		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}

		return Either.left(resource);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> getComponent(String id, ComponentParametersView componentParametersView, boolean inTrasnaction) {

		Either<Resource, StorageOperationStatus> component = getResource(id, componentParametersView, inTrasnaction);
		if (component.isRight()) {
			return Either.right(component.right().value());
		}
		return (Either<T, StorageOperationStatus>) component;
	}

	// @Override
	public Either<Resource, StorageOperationStatus> updateResource(Resource resource, boolean inTransaction, ComponentParametersView filterResultView) {
		return (Either<Resource, StorageOperationStatus>) updateComponentFilterResult(resource, inTransaction, titanGenericDao, resource.getClass(), NodeTypeEnum.Resource, filterResultView);
	}

	@Override
	protected <T> Either<T, StorageOperationStatus> updateComponentFilterResult(T component, boolean inTransaction, ComponentParametersView filterResultView) {
		return (Either<T, StorageOperationStatus>) updateResource((Resource) component, inTransaction, filterResultView);
	}
}
