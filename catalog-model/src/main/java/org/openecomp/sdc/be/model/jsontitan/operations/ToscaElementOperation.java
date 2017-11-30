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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fj.data.Either;

public abstract class ToscaElementOperation extends BaseOperation {
	private static Logger log = LoggerFactory.getLogger(ToscaElementOperation.class.getName());

	private static final Gson gson = new Gson();

	protected Gson getGson() {
		return gson;
	}

	@Autowired
	protected CategoryOperation categoryOperation;

	protected Either<GraphVertex, StorageOperationStatus> getComponentByLabelAndId(String uniqueId, ToscaElementTypeEnum nodeType, JsonParseFlagEnum parseFlag) {

		Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<GraphPropertyEnum, Object>();
		propertiesToMatch.put(GraphPropertyEnum.UNIQUE_ID, uniqueId);

		VertexTypeEnum vertexType = ToscaElementTypeEnum.getVertexTypeByToscaType(nodeType);
		Either<List<GraphVertex>, TitanOperationStatus> getResponse = titanDao.getByCriteria(vertexType, propertiesToMatch, parseFlag);
		if (getResponse.isRight()) {
			log.debug("Couldn't fetch component with type {} and unique id {}, error: {}", vertexType, uniqueId, getResponse.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getResponse.right().value()));

		}
		List<GraphVertex> componentList = getResponse.left().value();
		if (componentList.isEmpty()) {
			log.debug("Component with type {} and unique id {} was not found", vertexType, uniqueId);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		GraphVertex vertexG = componentList.get(0);
		return Either.left(vertexG);
	}

	public Either<ToscaElement, StorageOperationStatus> getToscaElement(String uniqueId) {
		return getToscaElement(uniqueId, new ComponentParametersView());
	}

	public Either<GraphVertex, StorageOperationStatus> markComponentToDelete(GraphVertex componentToDelete) {
		Either<GraphVertex, StorageOperationStatus> result = null;

		Boolean isDeleted = (Boolean) componentToDelete.getMetadataProperty(GraphPropertyEnum.IS_DELETED);
		if (isDeleted != null && isDeleted && !(Boolean) componentToDelete.getMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION)) {
			// component already marked for delete
			result = Either.left(componentToDelete);
			return result;
		} else {

			componentToDelete.addMetadataProperty(GraphPropertyEnum.IS_DELETED, Boolean.TRUE);
			componentToDelete.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());

			Either<GraphVertex, TitanOperationStatus> updateNode = titanDao.updateVertex(componentToDelete);

			StorageOperationStatus updateComponent;
			if (updateNode.isRight()) {
				log.debug("Failed to update component {}. status is {}", componentToDelete.getUniqueId(), updateNode.right().value());
				updateComponent = DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value());
				result = Either.right(updateComponent);
				return result;
			}

			result = Either.left(componentToDelete);
			return result;
		}
	}

	/**
	 * Performs a shadow clone of previousToscaElement
	 * 
	 * @param previousToscaElement
	 * @param nextToscaElement
	 * @param user
	 * @return
	 */
	public Either<GraphVertex, StorageOperationStatus> cloneToscaElement(GraphVertex previousToscaElement, GraphVertex nextToscaElement, GraphVertex user) {

		Either<GraphVertex, StorageOperationStatus> result = null;
		GraphVertex createdToscaElementVertex = null;
		TitanOperationStatus status;

		Either<GraphVertex, TitanOperationStatus> createNextVersionRes = titanDao.createVertex(nextToscaElement);
		if (createNextVersionRes.isRight()) {
			status = createNextVersionRes.right().value();
			CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create tosca element vertex {} with version {} on graph. Status is {}. ", previousToscaElement.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME),
					previousToscaElement.getMetadataProperty(GraphPropertyEnum.VERSION), status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		if (result == null) {
			createdToscaElementVertex = createNextVersionRes.left().value();
			Map<EdgePropertyEnum, Object> properties = new HashMap<EdgePropertyEnum, Object>();
			properties.put(EdgePropertyEnum.STATE, createdToscaElementVertex.getMetadataProperty(GraphPropertyEnum.STATE));
			status = titanDao.createEdge(user.getVertex(), createdToscaElementVertex.getVertex(), EdgeLabelEnum.STATE, properties);
			if (status != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge with label {} from user vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.STATE, user.getUniqueId(),
						previousToscaElement.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (result == null) {
			status = titanDao.createEdge(user.getVertex(), createdToscaElementVertex.getVertex(), EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());
			if (status != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge with label {} from user vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.LAST_MODIFIER, user.getUniqueId(),
						nextToscaElement.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (result == null) {
			status = titanDao.createEdge(user.getVertex(), createdToscaElementVertex.getVertex(), EdgeLabelEnum.CREATOR, new HashMap<>());
			if (status != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge with label {} from user vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.CREATOR, user.getUniqueId(),
						nextToscaElement.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (result == null) {
			Iterator<Edge> edgesToCopyIter = previousToscaElement.getVertex().edges(Direction.OUT);
			while (edgesToCopyIter.hasNext()) {
				Edge currEdge = edgesToCopyIter.next();
				Vertex currVertex = currEdge.inVertex();
				// if(EdgeLabelEnum.getEdgeLabelEnum(currEdge.label()).equals(EdgeLabelEnum.VERSION)){
				// continue;
				// }
				status = titanDao.createEdge(createdToscaElementVertex.getVertex(), currVertex, EdgeLabelEnum.getEdgeLabelEnum(currEdge.label()), currEdge);
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge with label {} from tosca element vertex {} to vertex with label {} on graph. Status is {}. ", currEdge.label(), createdToscaElementVertex.getUniqueId(),
							currVertex.property(GraphPropertyEnum.LABEL.getProperty()), status);
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					break;
				}
			}
		}

		if (result == null) {
			result = Either.left(createdToscaElementVertex);
		} else {
			CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to clone tosca element {} with the name {}. ", previousToscaElement.getUniqueId(), previousToscaElement.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME));
		}
		return result;
	}

	protected TitanOperationStatus setLastModifierFromGraph(GraphVertex componentV, ToscaElement toscaElement) {
		Either<GraphVertex, TitanOperationStatus> parentVertex = titanDao.getParentVertex(componentV, EdgeLabelEnum.LAST_MODIFIER, JsonParseFlagEnum.NoParse);
		if (parentVertex.isRight()) {
			log.debug("Failed to fetch last modifier for tosca element with id {} error {}", componentV.getUniqueId(), parentVertex.right().value());
			return parentVertex.right().value();
		}
		GraphVertex userV = parentVertex.left().value();
		String userId = (String) userV.getMetadataProperty(GraphPropertyEnum.USERID);
		toscaElement.setLastUpdaterUserId(userId);
		toscaElement.setLastUpdaterFullName(buildFullName(userV));
		return TitanOperationStatus.OK;
	}

	public String buildFullName(GraphVertex userV) {

		String fullName = (String) userV.getMetadataProperty(GraphPropertyEnum.FIRST_NAME);
		if (fullName == null) {
			fullName = "";
		} else {
			fullName = fullName + " ";
		}
		String lastName = (String) userV.getMetadataProperty(GraphPropertyEnum.LAST_NAME);
		if (lastName != null) {
			fullName += lastName;
		}
		return fullName;
	}

	protected TitanOperationStatus setCreatorFromGraph(GraphVertex componentV, ToscaElement toscaElement) {
		Either<GraphVertex, TitanOperationStatus> parentVertex = titanDao.getParentVertex(componentV, EdgeLabelEnum.CREATOR, JsonParseFlagEnum.NoParse);
		if (parentVertex.isRight()) {
			log.debug("Failed to fetch creator for tosca element with id {} error {}", componentV.getUniqueId(), parentVertex.right().value());
			return parentVertex.right().value();
		}
		GraphVertex userV = parentVertex.left().value();
		String creatorUserId = (String) userV.getMetadataProperty(GraphPropertyEnum.USERID);
		toscaElement.setCreatorUserId(creatorUserId);
		toscaElement.setCreatorFullName(buildFullName(userV));

		return TitanOperationStatus.OK;
	}

	protected <T extends ToscaElement> T getResourceMetaDataFromResource(T toscaElement) {
		if (toscaElement.getNormalizedName() == null || toscaElement.getNormalizedName().isEmpty()) {
			toscaElement.setNormalizedName(ValidationUtils.normaliseComponentName(toscaElement.getName()));
		}
		if (toscaElement.getSystemName() == null || toscaElement.getSystemName().isEmpty()) {
			toscaElement.setSystemName(ValidationUtils.convertToSystemName(toscaElement.getName()));
		}

		LifecycleStateEnum lifecycleStateEnum = toscaElement.getLifecycleState();
		if (lifecycleStateEnum == null) {
			toscaElement.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		}
		long currentDate = System.currentTimeMillis();
		if (toscaElement.getCreationDate() == null) {
			toscaElement.setCreationDate(currentDate);
		}
		toscaElement.setLastUpdateDate(currentDate);

		return toscaElement;
	}

	protected void fillCommonMetadata(GraphVertex nodeTypeVertex, ToscaElement toscaElement) {
		if (toscaElement.isHighestVersion() == null) {
			toscaElement.setHighestVersion(true);
		}
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.IS_DELETED, toscaElement.getMetadataValue(JsonPresentationFields.IS_DELETED));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, toscaElement.getMetadataValueOrDefault(JsonPresentationFields.HIGHEST_VERSION, Boolean.TRUE));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.STATE, toscaElement.getMetadataValue(JsonPresentationFields.LIFECYCLE_STATE));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.RESOURCE_TYPE, toscaElement.getMetadataValue(JsonPresentationFields.RESOURCE_TYPE));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.VERSION, toscaElement.getMetadataValue(JsonPresentationFields.VERSION));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME, toscaElement.getMetadataValue(JsonPresentationFields.NORMALIZED_NAME));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, toscaElement.getMetadataValue(JsonPresentationFields.UNIQUE_ID));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaElement.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.UUID, toscaElement.getMetadataValue(JsonPresentationFields.UUID));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.IS_ABSTRACT, toscaElement.getMetadataValue(JsonPresentationFields.IS_ABSTRACT));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.INVARIANT_UUID, toscaElement.getMetadataValue(JsonPresentationFields.INVARIANT_UUID));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.NAME, toscaElement.getMetadataValue(JsonPresentationFields.NAME));
		nodeTypeVertex.addMetadataProperty(GraphPropertyEnum.SYSTEM_NAME, toscaElement.getMetadataValue(JsonPresentationFields.SYSTEM_NAME));
		toscaElement.getMetadata().entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> nodeTypeVertex.setJsonMetadataField(JsonPresentationFields.getByPresentation(e.getKey()), e.getValue()));

		nodeTypeVertex.setUniqueId(toscaElement.getUniqueId());
		nodeTypeVertex.setType(toscaElement.getComponentType());

	}

	protected StorageOperationStatus assosiateToUsers(GraphVertex nodeTypeVertex, ToscaElement toscaElement) {
		// handle user
		String userId = toscaElement.getCreatorUserId();

		Either<GraphVertex, TitanOperationStatus> findUser = findUserVertex(userId);

		if (findUser.isRight()) {
			TitanOperationStatus status = findUser.right().value();
			log.error("Cannot find user {} in the graph. status is {}", userId, status);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);

		}
		GraphVertex creatorVertex = findUser.left().value();
		GraphVertex updaterVertex = creatorVertex;
		String updaterId = toscaElement.getLastUpdaterUserId();
		if (updaterId != null && !updaterId.equals(userId)) {
			findUser = findUserVertex(updaterId);
			if (findUser.isRight()) {
				TitanOperationStatus status = findUser.right().value();
				log.error("Cannot find user {} in the graph. status is {}", userId, status);
				return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			} else {
				updaterVertex = findUser.left().value();
			}
		}
		Map<EdgePropertyEnum, Object> props = new HashMap<EdgePropertyEnum, Object>();
		props.put(EdgePropertyEnum.STATE, (String) toscaElement.getMetadataValue(JsonPresentationFields.LIFECYCLE_STATE));

		TitanOperationStatus result = titanDao.createEdge(updaterVertex, nodeTypeVertex, EdgeLabelEnum.STATE, props);
		log.debug("After associating user {} to resource {}. Edge type is {}", updaterVertex, nodeTypeVertex.getUniqueId(), EdgeLabelEnum.STATE);
		if (TitanOperationStatus.OK != result) {
			return DaoStatusConverter.convertTitanStatusToStorageStatus(result);
		}
		result = titanDao.createEdge(updaterVertex, nodeTypeVertex, EdgeLabelEnum.LAST_MODIFIER, null);
		log.debug("After associating user {}  to resource {}. Edge type is {}", updaterVertex, nodeTypeVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
		if (!result.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate user {}  to resource {}. Edge type is {}", updaterVertex, nodeTypeVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(result);
		}

		toscaElement.setLastUpdaterUserId(toscaElement.getCreatorUserId());
		toscaElement.setLastUpdaterFullName(toscaElement.getCreatorFullName());

		result = titanDao.createEdge(creatorVertex, nodeTypeVertex, EdgeLabelEnum.CREATOR, null);
		log.debug("After associating user {} to resource {}. Edge type is {} ", creatorVertex, nodeTypeVertex.getUniqueId(), EdgeLabelEnum.CREATOR);
		if (!result.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate user {} to resource {}. Edge type is {} ", creatorVertex, nodeTypeVertex.getUniqueId(), EdgeLabelEnum.CREATOR);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(result);
		}
		return StorageOperationStatus.OK;
	}

	protected StorageOperationStatus assosiateResourceMetadataToCategory(GraphVertex nodeTypeVertex, ToscaElement nodeType) {
		String subcategoryName = nodeType.getCategories().get(0).getSubcategories().get(0).getName();
		String categoryName = nodeType.getCategories().get(0).getName();
		Either<GraphVertex, StorageOperationStatus> getCategoryVertex = getResourceCategoryVertex(nodeType.getUniqueId(), subcategoryName, categoryName);

		if (getCategoryVertex.isRight()) {
			return getCategoryVertex.right().value();
		}

		GraphVertex subCategoryV = getCategoryVertex.left().value();

		TitanOperationStatus createEdge = titanDao.createEdge(nodeTypeVertex, subCategoryV, EdgeLabelEnum.CATEGORY, new HashMap<>());
		if (createEdge != TitanOperationStatus.OK) {
			log.trace("Failed to associate resource {} to category {} with id {}", nodeType.getUniqueId(), subcategoryName, subCategoryV.getUniqueId());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(createEdge);
		}
		return StorageOperationStatus.OK;
	}

	protected Either<GraphVertex, StorageOperationStatus> getResourceCategoryVertex(String elementId, String subcategoryName, String categoryName) {
		Either<GraphVertex, StorageOperationStatus> category = categoryOperation.getCategory(categoryName, VertexTypeEnum.RESOURCE_CATEGORY);
		if (category.isRight()) {
			log.trace("Failed to fetch category {} for resource {} error {}", categoryName, elementId, category.right().value());
			return Either.right(category.right().value());
		}
		GraphVertex categoryV = category.left().value();

		if (subcategoryName != null) {
			Either<GraphVertex, StorageOperationStatus> subCategory = categoryOperation.getSubCategoryForCategory(categoryV, subcategoryName);
			if (subCategory.isRight()) {
				log.trace("Failed to fetch subcategory {} of category for resource {} error {}", subcategoryName, categoryName, elementId, subCategory.right().value());
				return Either.right(subCategory.right().value());
			}

			GraphVertex subCategoryV = subCategory.left().value();
			return Either.left(subCategoryV);
		}
		return Either.left(categoryV);
	}

	private StorageOperationStatus associateArtifactsToResource(GraphVertex nodeTypeVertex, ToscaElement toscaElement) {
		Map<String, ArtifactDataDefinition> artifacts = toscaElement.getArtifacts();
		Either<GraphVertex, StorageOperationStatus> status;
		if (artifacts != null) {
			artifacts.values().stream().filter(a -> a.getUniqueId() == null).forEach(a -> {
				String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(nodeTypeVertex.getUniqueId().toLowerCase(), a.getArtifactLabel().toLowerCase());
				a.setUniqueId(uniqueId);
			});
			status = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.ARTIFACTS, EdgeLabelEnum.ARTIFACTS, artifacts);
			if (status.isRight()) {
				return status.right().value();
			}
		}
		Map<String, ArtifactDataDefinition> toscaArtifacts = toscaElement.getToscaArtifacts();
		if (toscaArtifacts != null) {
			toscaArtifacts.values().stream().filter(a -> a.getUniqueId() == null).forEach(a -> {
				String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(nodeTypeVertex.getUniqueId().toLowerCase(), a.getArtifactLabel().toLowerCase());
				a.setUniqueId(uniqueId);
			});
			status = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.TOSCA_ARTIFACTS, EdgeLabelEnum.TOSCA_ARTIFACTS, toscaArtifacts);
			if (status.isRight()) {
				return status.right().value();
			}
		}
		Map<String, ArtifactDataDefinition> deploymentArtifacts = toscaElement.getDeploymentArtifacts();
		if (deploymentArtifacts != null) {
			deploymentArtifacts.values().stream().filter(a -> a.getUniqueId() == null).forEach(a -> {
				String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(nodeTypeVertex.getUniqueId().toLowerCase(), a.getArtifactLabel().toLowerCase());
				a.setUniqueId(uniqueId);
			});
			status = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.DEPLOYMENT_ARTIFACTS, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS, deploymentArtifacts);
			if (status.isRight()) {
				return status.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	protected TitanOperationStatus disassociateAndDeleteCommonElements(GraphVertex toscaElementVertex) {
		TitanOperationStatus status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.ARTIFACTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disaccociate artifact for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.TOSCA_ARTIFACTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disaccociate tosca artifact for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to deployment artifact for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.PROPERTIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disaccociate properties for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.ATTRIBUTES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disaccociate attributes for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.ADDITIONAL_INFORMATION);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disaccociate additional information for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CAPABILITIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disaccociate capabilities for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.REQUIREMENTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disaccociate requirements for {} error {}", toscaElementVertex.getUniqueId(), status);
			return status;
		}
		return TitanOperationStatus.OK;
	}

	protected StorageOperationStatus assosiateCommonForToscaElement(GraphVertex nodeTypeVertex, ToscaElement toscaElement, List<GraphVertex> derivedResources) {

		StorageOperationStatus associateUsers = assosiateToUsers(nodeTypeVertex, toscaElement);
		if (associateUsers != StorageOperationStatus.OK) {
			return associateUsers;
		}
		StorageOperationStatus associateArtifacts = associateArtifactsToResource(nodeTypeVertex, toscaElement);
		if (associateArtifacts != StorageOperationStatus.OK) {
			return associateArtifacts;
		}
		StorageOperationStatus associateProperties = associatePropertiesToResource(nodeTypeVertex, toscaElement, derivedResources);
		if (associateProperties != StorageOperationStatus.OK) {
			return associateProperties;
		}
		StorageOperationStatus associateAdditionaInfo = associateAdditionalInfoToResource(nodeTypeVertex, toscaElement);
		if (associateAdditionaInfo != StorageOperationStatus.OK) {
			return associateAdditionaInfo;
		}

		return StorageOperationStatus.OK;
	}

	protected StorageOperationStatus associatePropertiesToResource(GraphVertex nodeTypeVertex, ToscaElement nodeType, List<GraphVertex> derivedResources) {
		// Note : currently only one derived supported!!!!
		Either<Map<String, PropertyDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.PROPERTIES);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, PropertyDataDefinition> propertiesAll = dataFromDerived.left().value();

		Map<String, PropertyDataDefinition> properties = nodeType.getProperties();

		if (properties != null) {
			properties.values().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
				String uid = UniqueIdBuilder.buildPropertyUniqueId(nodeTypeVertex.getUniqueId(), p.getName());
				p.setUniqueId(uid);
			});

			Either<Map<String, PropertyDataDefinition>, String> eitherMerged = ToscaDataDefinition.mergeDataMaps(propertiesAll, properties);
			if (eitherMerged.isRight()) {
				// TODO re-factor error handling - moving BL to operation resulted in loss of info about the invalid property
				log.debug("property {} cannot be overriden", eitherMerged.right().value());
				return StorageOperationStatus.INVALID_PROPERTY;
			}
		}
		if (!propertiesAll.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.PROPERTIES, EdgeLabelEnum.PROPERTIES, propertiesAll);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateAdditionalInfoToResource(GraphVertex nodeTypeVertex, ToscaElement nodeType) {
		Map<String, AdditionalInfoParameterDataDefinition> additionalInformation = nodeType.getAdditionalInformation();
		if (additionalInformation != null) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.ADDITIONAL_INFORMATION, EdgeLabelEnum.ADDITIONAL_INFORMATION, additionalInformation);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	protected <T extends ToscaDataDefinition> Either<Map<String, T>, StorageOperationStatus> getDataFromDerived(List<GraphVertex> derivedResources, EdgeLabelEnum edge) {
		Map<String, T> propertiesAll = new HashMap<>();

		if (derivedResources != null && !derivedResources.isEmpty()) {
			for (GraphVertex derived : derivedResources) {
				Either<List<GraphVertex>, TitanOperationStatus> derivedProperties = titanDao.getChildrenVertecies(derived, edge, JsonParseFlagEnum.ParseJson);
				if (derivedProperties.isRight()) {
					if (derivedProperties.right().value() != TitanOperationStatus.NOT_FOUND) {
						log.debug("Failed to get properties for derived from {} error {}", derived.getUniqueId(), derivedProperties.right().value());
						return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(derivedProperties.right().value()));
					} else {
						continue;
					}
				}
				List<GraphVertex> propList = derivedProperties.left().value();
				for (GraphVertex propV : propList) {
					Map<String, T> propertiesFromDerived = (Map<String, T>) propV.getJson();
					if (propertiesFromDerived != null) {
						propertiesFromDerived.entrySet().forEach(x -> x.getValue().setOwnerIdIfEmpty(derived.getUniqueId()));
						propertiesAll.putAll(propertiesFromDerived);
					}
				}
			}
		}
		return Either.left(propertiesAll);
	}

	protected TitanOperationStatus setArtifactsFromGraph(GraphVertex componentV, ToscaElement toscaElement) {
		Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.ARTIFACTS);
		if (result.isLeft()) {
			toscaElement.setArtifacts(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		result = getDataFromGraph(componentV, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
		if (result.isLeft()) {
			toscaElement.setDeploymentArtifacts(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		result = getDataFromGraph(componentV, EdgeLabelEnum.TOSCA_ARTIFACTS);
		if (result.isLeft()) {
			toscaElement.setToscaArtifacts(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	protected TitanOperationStatus setAllVersions(GraphVertex componentV, ToscaElement toscaElement) {
		Map<String, String> allVersion = new HashMap<>();

		allVersion.put((String) componentV.getMetadataProperty(GraphPropertyEnum.VERSION), componentV.getUniqueId());
		ArrayList<GraphVertex> allChildrenAndParants = new ArrayList<GraphVertex>();
		Either<GraphVertex, TitanOperationStatus> childResourceRes = titanDao.getChildVertex(componentV, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
		while (childResourceRes.isLeft()) {
			GraphVertex child = childResourceRes.left().value();
			allChildrenAndParants.add(child);
			childResourceRes = titanDao.getChildVertex(child, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
		}
		TitanOperationStatus operationStatus = childResourceRes.right().value();

		if (operationStatus != TitanOperationStatus.NOT_FOUND) {
			return operationStatus;
		} else {
			Either<GraphVertex, TitanOperationStatus> parentResourceRes = titanDao.getParentVertex(componentV, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
			while (parentResourceRes.isLeft()) {
				GraphVertex parent = parentResourceRes.left().value();
				allChildrenAndParants.add(parent);
				parentResourceRes = titanDao.getParentVertex(parent, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
			}
			operationStatus = parentResourceRes.right().value();
			if (operationStatus != TitanOperationStatus.NOT_FOUND) {
				return operationStatus;
			} else {
				allChildrenAndParants.stream().filter(vertex -> {
					Boolean isDeleted = (Boolean) vertex.getMetadataProperty(GraphPropertyEnum.IS_DELETED);
					return (isDeleted == null || isDeleted == false);
				}).forEach(vertex -> allVersion.put((String) vertex.getMetadataProperty(GraphPropertyEnum.VERSION), vertex.getUniqueId()));

				toscaElement.setAllVersions(allVersion);
				return TitanOperationStatus.OK;
			}
		}
	}

	protected <T extends ToscaElement> Either<List<T>, StorageOperationStatus> getFollowedComponent(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, ComponentTypeEnum neededType) {

		Either<List<T>, StorageOperationStatus> result = null;

		Map<GraphPropertyEnum, Object> props = null;

		if (userId != null) {
			props = new HashMap<>();
			// for Designer retrieve specific user
			props.put(GraphPropertyEnum.USERID, userId);
		}
		// in case of user id == null -> get all users by label
		// for Tester and Admin retrieve all users
		Either<List<GraphVertex>, TitanOperationStatus> usersByCriteria = titanDao.getByCriteria(VertexTypeEnum.USER, props, JsonParseFlagEnum.NoParse);
		if (usersByCriteria.isRight()) {
			log.debug("Failed to fetch users by criteria {} error {}", props, usersByCriteria.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(usersByCriteria.right().value()));
		}
		List<GraphVertex> users = usersByCriteria.left().value();

		List<T> components = new ArrayList<>();
		List<T> componentsPerUser;
		for (GraphVertex userV : users) {

			HashSet<String> ids = new HashSet<String>();
			Either<List<GraphVertex>, TitanOperationStatus> childrenVertecies = titanDao.getChildrenVertecies(userV, EdgeLabelEnum.STATE, JsonParseFlagEnum.NoParse);
			if (childrenVertecies.isRight() && childrenVertecies.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.debug("Failed to fetch children vertices for user {} by edge {} error {}", userV.getMetadataProperty(GraphPropertyEnum.USERID), EdgeLabelEnum.STATE, childrenVertecies.right().value());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(childrenVertecies.right().value()));
			}

			// get all resource with current state
			if (childrenVertecies.isLeft()) {
				componentsPerUser = fetchComponents(lifecycleStates, childrenVertecies.left().value(), neededType, EdgeLabelEnum.STATE);

				if (componentsPerUser != null) {
					for (T comp : componentsPerUser) {
						ids.add(comp.getUniqueId());
						components.add(comp);
					}
				}
			}
			if (lastStateStates != null && !lastStateStates.isEmpty()) {
				// get all resource with last state
				childrenVertecies = titanDao.getChildrenVertecies(userV, EdgeLabelEnum.LAST_STATE, JsonParseFlagEnum.NoParse);
				if (childrenVertecies.isRight() && childrenVertecies.right().value() != TitanOperationStatus.NOT_FOUND) {
					log.debug("Failed to fetch children vertices for user {} by edge {} error {}", userV.getMetadataProperty(GraphPropertyEnum.USERID), EdgeLabelEnum.LAST_STATE, childrenVertecies.right().value());
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(childrenVertecies.right().value()));
				}
				if (childrenVertecies.isLeft()) {
					boolean isFirst;
					componentsPerUser = fetchComponents(lastStateStates, childrenVertecies.left().value(), neededType, EdgeLabelEnum.LAST_STATE);
					if (componentsPerUser != null) {
						for (T comp : componentsPerUser) {
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
			}

		} // whlile users
		;
		result = Either.left(components);
		return result;

	}

	private <T extends ToscaElement> List<T> fetchComponents(Set<LifecycleStateEnum> lifecycleStates, List<GraphVertex> vertices, ComponentTypeEnum neededType, EdgeLabelEnum edgelabel) {
		List<T> components = new ArrayList<>();
		for (GraphVertex node : vertices) {

			Iterator<Edge> edges = node.getVertex().edges(Direction.IN, edgelabel.name());
			while (edges.hasNext()) {
				Edge edge = edges.next();
				String stateStr = (String) titanDao.getProperty(edge, EdgePropertyEnum.STATE);

				LifecycleStateEnum nodeState = LifecycleStateEnum.findState(stateStr);
				if (nodeState == null) {
					log.debug("no supported STATE {} for element  {}", stateStr, node.getUniqueId());
					continue;
				}
				if (lifecycleStates != null && lifecycleStates.contains(nodeState)) {

					Boolean isDeleted = (Boolean) node.getMetadataProperty(GraphPropertyEnum.IS_DELETED);
					if (isDeleted != null && isDeleted) {
						log.trace("Deleted element  {}, discard", node.getUniqueId());
						continue;
					}

					Boolean isHighest = (Boolean) node.getMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION);
					if (isHighest) {

						ComponentTypeEnum componentType = node.getType();
						// get only latest versions

						if (componentType == null) {
							log.debug("No supported type {} for vertex {}", componentType, node.getUniqueId());
							continue;
						}
						if (neededType == componentType) {
							switch (componentType) {
							case SERVICE:
							case PRODUCT:
								handleNode(components, node, componentType);
								break;
							case RESOURCE:
								Boolean isAbtract = (Boolean) node.getMetadataProperty(GraphPropertyEnum.IS_ABSTRACT);
								if (isAbtract == null || false == isAbtract) {
									handleNode(components, node, componentType);
								} // if not abstract
								break;
							default:
								log.debug("not supported node type {}", componentType);
								break;
							}// case
						} // needed type
					}
				} // if
			} // while edges
		} // while resources
		return components;
	}

	protected <T extends ToscaElement> void handleNode(List<T> components, GraphVertex vertexComponent, ComponentTypeEnum nodeType) {

		Either<T, StorageOperationStatus> component = getLightComponent(vertexComponent, nodeType, new ComponentParametersView(true));
		if (component.isRight()) {
			log.debug("Failed to get component for id =  {}  error : {} skip resource", vertexComponent.getUniqueId(), component.right().value());
		} else {
			components.add(component.left().value());
		}
	}

	protected <T extends ToscaElement> Either<T, StorageOperationStatus> getLightComponent(String componentUid, ComponentTypeEnum nodeType, ComponentParametersView parametersFilter) {
		Either<GraphVertex, TitanOperationStatus> getVertexRes = titanDao.getVertexById(componentUid);
		if (getVertexRes.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVertexRes.right().value()));
		}
		return getLightComponent(getVertexRes.left().value(), nodeType, parametersFilter);
	}

	protected <T extends ToscaElement> Either<T, StorageOperationStatus> getLightComponent(GraphVertex vertexComponent, ComponentTypeEnum nodeType, ComponentParametersView parametersFilter) {

		log.trace("Starting to build light component of type {}, id {}", nodeType, vertexComponent.getUniqueId());

		titanDao.parseVertexProperties(vertexComponent, JsonParseFlagEnum.ParseMetadata);

		T toscaElement = convertToComponent(vertexComponent);

		TitanOperationStatus status = setCreatorFromGraph(vertexComponent, toscaElement);
		if (status != TitanOperationStatus.OK) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}

		status = setLastModifierFromGraph(vertexComponent, toscaElement);
		if (status != TitanOperationStatus.OK) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = setCategoriesFromGraph(vertexComponent, toscaElement);
		if (status != TitanOperationStatus.OK) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		if (!parametersFilter.isIgnoreAllVersions()) {
			status = setAllVersions(vertexComponent, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (!parametersFilter.isIgnoreCapabilities()) {
			status = setCapabilitiesFromGraph(vertexComponent, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (!parametersFilter.isIgnoreRequirements()) {
			status = setRequirementsFromGraph(vertexComponent, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		log.debug("Ended to build light component of type {}, id {}", nodeType, vertexComponent.getUniqueId());
		return Either.left(toscaElement);
	}

	@SuppressWarnings("unchecked")
	protected <T extends ToscaElement> T convertToComponent(GraphVertex componentV) {
		ToscaElement toscaElement = null;
		VertexTypeEnum label = componentV.getLabel();
		switch (label) {
		case NODE_TYPE:
			toscaElement = new NodeType();
			break;
		case TOPOLOGY_TEMPLATE:
			toscaElement = new TopologyTemplate();
			break;
		default:
			log.debug("Not supported tosca type {}", label);
			break;
		}

		Map<String, Object> jsonMetada = componentV.getMetadataJson();
		if (toscaElement != null) {
			toscaElement.setMetadata(jsonMetada);
		}
		return (T) toscaElement;
	}

	protected TitanOperationStatus setResourceCategoryFromGraph(GraphVertex componentV, ToscaElement toscaElement) {
		List<CategoryDefinition> categories = new ArrayList<>();
		SubCategoryDefinition subcategory;

		Either<GraphVertex, TitanOperationStatus> childVertex = titanDao.getChildVertex(componentV, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.NoParse);
		if (childVertex.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", EdgeLabelEnum.CATEGORY, componentV.getUniqueId(), childVertex.right().value());
			return childVertex.right().value();
		}
		GraphVertex subCategoryV = childVertex.left().value();
		Map<GraphPropertyEnum, Object> metadataProperties = subCategoryV.getMetadataProperties();
		subcategory = new SubCategoryDefinition();
		subcategory.setUniqueId(subCategoryV.getUniqueId());
		subcategory.setNormalizedName((String) metadataProperties.get(GraphPropertyEnum.NORMALIZED_NAME));
		subcategory.setName((String) metadataProperties.get(GraphPropertyEnum.NAME));

		Type listTypeSubcat = new TypeToken<List<String>>() {
		}.getType();
		List<String> iconsfromJsonSubcat = getGson().fromJson((String) metadataProperties.get(GraphPropertyEnum.ICONS), listTypeSubcat);
		subcategory.setIcons(iconsfromJsonSubcat);

		Either<GraphVertex, TitanOperationStatus> parentVertex = titanDao.getParentVertex(subCategoryV, EdgeLabelEnum.SUB_CATEGORY, JsonParseFlagEnum.NoParse);
		if (parentVertex.isRight()) {
			log.debug("failed to fetch {} for category with id {}, error {}", EdgeLabelEnum.SUB_CATEGORY, subCategoryV.getUniqueId(), parentVertex.right().value());
			return childVertex.right().value();
		}
		GraphVertex categoryV = parentVertex.left().value();
		metadataProperties = categoryV.getMetadataProperties();

		CategoryDefinition category = new CategoryDefinition();
		category.setUniqueId(categoryV.getUniqueId());
		category.setNormalizedName((String) metadataProperties.get(GraphPropertyEnum.NORMALIZED_NAME));
		category.setName((String) metadataProperties.get(GraphPropertyEnum.NAME));

		Type listTypeCat = new TypeToken<List<String>>() {
		}.getType();
		List<String> iconsfromJsonCat = getGson().fromJson((String) metadataProperties.get(GraphPropertyEnum.ICONS), listTypeCat);
		category.setIcons(iconsfromJsonCat);

		category.addSubCategory(subcategory);
		categories.add(category);
		toscaElement.setCategories(categories);

		return TitanOperationStatus.OK;
	}

	public <T extends ToscaElement> Either<T, StorageOperationStatus> updateToscaElement(T toscaElementToUpdate, GraphVertex elementV, ComponentParametersView filterResult) {
		Either<T, StorageOperationStatus> result = null;

		log.debug("In updateToscaElement. received component uid = {}", (toscaElementToUpdate == null ? null : toscaElementToUpdate.getUniqueId()));
		if (toscaElementToUpdate == null) {
			log.error("Service object is null");
			result = Either.right(StorageOperationStatus.BAD_REQUEST);
			return result;
		}

		String modifierUserId = toscaElementToUpdate.getLastUpdaterUserId();
		if (modifierUserId == null || modifierUserId.isEmpty()) {
			log.error("UserId is missing in the request.");
			result = Either.right(StorageOperationStatus.BAD_REQUEST);
			return result;
		}
		Either<GraphVertex, TitanOperationStatus> findUser = findUserVertex(modifierUserId);

		if (findUser.isRight()) {
			TitanOperationStatus status = findUser.right().value();
			log.error("Cannot find user {} in the graph. status is {}", modifierUserId, status);
			return result;
		}

		GraphVertex modifierV = findUser.left().value();
		// UserData modifierUserData = findUser.left().value();
		String toscaElementId = toscaElementToUpdate.getUniqueId();

		Either<GraphVertex, TitanOperationStatus> parentVertex = titanDao.getParentVertex(elementV, EdgeLabelEnum.LAST_MODIFIER, JsonParseFlagEnum.NoParse);
		if (parentVertex.isRight()) {
			log.debug("Failed to fetch last modifier for tosca element with id {} error {}", toscaElementId, parentVertex.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(parentVertex.right().value()));
		}
		GraphVertex userV = parentVertex.left().value();
		String currentModifier = (String) userV.getMetadataProperty(GraphPropertyEnum.USERID);

		String prevSystemName = (String) elementV.getMetadataProperty(GraphPropertyEnum.SYSTEM_NAME);

		if (currentModifier.equals(modifierUserId)) {
			log.debug("Graph LAST MODIFIER edge should not be changed since the modifier is the same as the last modifier.");
		} else {
			log.debug("Going to update the last modifier user of the resource from {} to {}", currentModifier, modifierUserId);
			StorageOperationStatus status = moveLastModifierEdge(elementV, modifierV);
			log.debug("Finish to update the last modifier user of the resource from {} to {}. status is {}", currentModifier, modifierUserId, status);
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
				return result;
			}
		}

		final long currentTimeMillis = System.currentTimeMillis();
		log.debug("Going to update the last Update Date of the resource from {} to {}", elementV.getJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE), currentTimeMillis);
		elementV.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, currentTimeMillis);

		StorageOperationStatus checkCategories = validateCategories(toscaElementToUpdate, elementV);
		if (checkCategories != StorageOperationStatus.OK) {
			result = Either.right(checkCategories);
			return result;
		}

		// update all data on vertex
		fillToscaElementVertexData(elementV, toscaElementToUpdate, JsonParseFlagEnum.ParseMetadata);

		Either<GraphVertex, TitanOperationStatus> updateElement = titanDao.updateVertex(elementV);

		if (updateElement.isRight()) {
			log.error("Failed to update resource {}. status is {}", toscaElementId, updateElement.right().value());
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateElement.right().value()));
			return result;
		}
		GraphVertex updateElementV = updateElement.left().value();

		// DE230195 in case resource name changed update TOSCA artifacts
		// file names accordingly
		String newSystemName = (String) updateElementV.getMetadataProperty(GraphPropertyEnum.SYSTEM_NAME);
		if (newSystemName != null && !newSystemName.equals(prevSystemName)) {
			Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> resultToscaArt = getDataFromGraph(updateElementV, EdgeLabelEnum.TOSCA_ARTIFACTS);
			if (resultToscaArt.isRight()) {
				log.debug("Failed to get  tosca artifact from graph for tosca element {} error {}", toscaElementId, resultToscaArt.right().value());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resultToscaArt.right().value()));
			}

			Map<String, ArtifactDataDefinition> toscaArtifacts = resultToscaArt.left().value();
			if (toscaArtifacts != null) {
				for (Entry<String, ArtifactDataDefinition> artifact : toscaArtifacts.entrySet()) {
					generateNewToscaFileName(toscaElementToUpdate.getComponentType().getValue().toLowerCase(), newSystemName, artifact.getValue());
				}
				// TODO call to new Artifact operation in order to update list of artifacts

			}
			// US833308 VLI in service - specific network_role property value logic
			if (ComponentTypeEnum.SERVICE == toscaElementToUpdate.getComponentType()) {
				// update method logs success/error and returns boolean (true if nothing fails)
				// TODO!!!!
				// updateServiceNameInVLIsNetworkRolePropertyValues(component, prevSystemName, newSystemName);
			}
		}

		if (toscaElementToUpdate.getComponentType() == ComponentTypeEnum.RESOURCE) {
			StorageOperationStatus resultDerived = updateDerived(toscaElementToUpdate, updateElementV);
			if (resultDerived != StorageOperationStatus.OK) {
				log.debug("Failed to update from derived data for element {} error {}", toscaElementId, resultDerived);
				return Either.right(resultDerived);
			}
		}

		Either<T, StorageOperationStatus> updatedResource = getToscaElement(updateElementV, filterResult);
		if (updatedResource.isRight()) {
			log.error("Failed to fetch tosca element {} after update , error {}", toscaElementId, updatedResource.right().value());
			result = Either.right(StorageOperationStatus.BAD_REQUEST);
			return result;
		}

		T updatedResourceValue = updatedResource.left().value();
		result = Either.left(updatedResourceValue);

		return result;
	}

	protected StorageOperationStatus moveLastModifierEdge(GraphVertex elementV, GraphVertex modifierV) {
		return DaoStatusConverter.convertTitanStatusToStorageStatus(titanDao.moveEdge(elementV, modifierV, EdgeLabelEnum.LAST_MODIFIER, Direction.IN));
	}

	protected StorageOperationStatus moveCategoryEdge(GraphVertex elementV, GraphVertex categoryV) {
		return DaoStatusConverter.convertTitanStatusToStorageStatus(titanDao.moveEdge(elementV, categoryV, EdgeLabelEnum.CATEGORY, Direction.OUT));
	}

	private void generateNewToscaFileName(String componentType, String componentName, ArtifactDataDefinition artifactInfo) {
		Map<String, Object> getConfig = (Map<String, Object>) ConfigurationManager.getConfigurationManager().getConfiguration().getToscaArtifacts().entrySet().stream().filter(p -> p.getKey().equalsIgnoreCase(artifactInfo.getArtifactLabel()))
				.findAny().get().getValue();
		artifactInfo.setArtifactName(componentType + "-" + componentName + getConfig.get("artifactName"));
	}

	protected <T extends ToscaElement> StorageOperationStatus validateResourceCategory(T toscaElementToUpdate, GraphVertex elementV) {
		StorageOperationStatus status = StorageOperationStatus.OK;
		List<CategoryDefinition> newCategoryList = toscaElementToUpdate.getCategories();
		CategoryDefinition newCategory = newCategoryList.get(0);

		Either<GraphVertex, TitanOperationStatus> childVertex = titanDao.getChildVertex(elementV, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.NoParse);
		if (childVertex.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", EdgeLabelEnum.CATEGORY, elementV.getUniqueId(), childVertex.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(childVertex.right().value());
		}
		GraphVertex subCategoryV = childVertex.left().value();
		Map<GraphPropertyEnum, Object> metadataProperties = subCategoryV.getMetadataProperties();
		String subCategoryNameCurrent = (String) metadataProperties.get(GraphPropertyEnum.NAME);

		Either<GraphVertex, TitanOperationStatus> parentVertex = titanDao.getParentVertex(subCategoryV, EdgeLabelEnum.SUB_CATEGORY, JsonParseFlagEnum.NoParse);
		if (parentVertex.isRight()) {
			log.debug("failed to fetch {} for category with id {}, error {}", EdgeLabelEnum.SUB_CATEGORY, subCategoryV.getUniqueId(), parentVertex.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(childVertex.right().value());
		}
		GraphVertex categoryV = parentVertex.left().value();
		metadataProperties = categoryV.getMetadataProperties();
		String categoryNameCurrent = (String) metadataProperties.get(GraphPropertyEnum.NAME);

		boolean categoryWasChanged = false;

		String newCategoryName = newCategory.getName();
		SubCategoryDefinition newSubcategory = newCategory.getSubcategories().get(0);
		String newSubCategoryName = newSubcategory.getName();
		if (newCategoryName != null && false == newCategoryName.equals(categoryNameCurrent)) {
			// the category was changed
			categoryWasChanged = true;
		} else {
			// the sub-category was changed
			if (newSubCategoryName != null && false == newSubCategoryName.equals(subCategoryNameCurrent)) {
				log.debug("Going to update the category of the resource from {} to {}", categoryNameCurrent, newCategory);
				categoryWasChanged = true;
			}
		}
		if (categoryWasChanged) {
			Either<GraphVertex, StorageOperationStatus> getCategoryVertex = getResourceCategoryVertex(elementV.getUniqueId(), newSubCategoryName, newCategoryName);

			if (getCategoryVertex.isRight()) {
				return getCategoryVertex.right().value();
			}
			GraphVertex newCategoryV = getCategoryVertex.left().value();
			status = moveCategoryEdge(elementV, newCategoryV);
			log.debug("Going to update the category of the resource from {} to {}. status is {}", categoryNameCurrent, newCategory, status);
		}
		return status;
	}

	public <T extends ToscaElement> Either<List<T>, StorageOperationStatus> getElementCatalogData(ComponentTypeEnum componentType, List<ResourceTypeEnum> excludeTypes, boolean isHighestVersions) {
		Either<List<GraphVertex>, TitanOperationStatus> listOfComponents;
		if (isHighestVersions) {
			listOfComponents = getListOfHighestComponents(componentType, excludeTypes, JsonParseFlagEnum.NoParse);
		}
		else {
			listOfComponents = getListOfHighestAndAllCertifiedComponents(componentType, excludeTypes);
		}

		if (listOfComponents.isRight() && listOfComponents.right().value() != TitanOperationStatus.NOT_FOUND) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(listOfComponents.right().value()));
		}
		List<T> result = new ArrayList<>();
		if (listOfComponents.isLeft()) {
			List<GraphVertex> highestAndAllCertified = listOfComponents.left().value();
			if (highestAndAllCertified != null && false == highestAndAllCertified.isEmpty()) {
				for (GraphVertex vertexComponent : highestAndAllCertified) {
					Either<T, StorageOperationStatus> component = getLightComponent(vertexComponent, componentType, new ComponentParametersView(true));
					if (component.isRight()) {
						log.debug("Failed to fetch light element for {} error {}", vertexComponent.getUniqueId(), component.right().value());
						return Either.right(component.right().value());
					} else {
						result.add(component.left().value());
					}
				}
			}
		}
		return Either.left(result);
	}

	private Either<List<GraphVertex>, TitanOperationStatus> getListOfHighestComponents(ComponentTypeEnum componentType, List<ResourceTypeEnum> excludeTypes, JsonParseFlagEnum parseFlag) {
		Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<>();
		Map<GraphPropertyEnum, Object> propertiesHasNotToMatch = new HashMap<>();
		propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
		propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

		if (componentType == ComponentTypeEnum.RESOURCE) {
			propertiesToMatch.put(GraphPropertyEnum.IS_ABSTRACT, false);
			propertiesHasNotToMatch.put(GraphPropertyEnum.RESOURCE_TYPE, excludeTypes);
		}
		propertiesHasNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

		return titanDao.getByCriteria(null, propertiesToMatch, propertiesHasNotToMatch, parseFlag);
	}

	// highest + (certified && !highest)
	public Either<List<GraphVertex>, TitanOperationStatus> getListOfHighestAndAllCertifiedComponents(ComponentTypeEnum componentType, List<ResourceTypeEnum> excludeTypes) {
		long startFetchAllStates = System.currentTimeMillis();
		Either<List<GraphVertex>, TitanOperationStatus> highestNodes = getListOfHighestComponents(componentType, excludeTypes, JsonParseFlagEnum.ParseMetadata);

		Map<GraphPropertyEnum, Object> propertiesToMatchCertified = new HashMap<>();
		Map<GraphPropertyEnum, Object> propertiesHasNotToMatchCertified = new HashMap<>();
		propertiesToMatchCertified.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		propertiesToMatchCertified.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
		if (componentType == ComponentTypeEnum.RESOURCE) {
			propertiesToMatchCertified.put(GraphPropertyEnum.IS_ABSTRACT, false);
			propertiesHasNotToMatchCertified.put(GraphPropertyEnum.RESOURCE_TYPE, excludeTypes);
		}

		propertiesHasNotToMatchCertified.put(GraphPropertyEnum.IS_DELETED, true);
		propertiesHasNotToMatchCertified.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

		Either<List<GraphVertex>, TitanOperationStatus> certifiedNotHighestNodes = titanDao.getByCriteria(null, propertiesToMatchCertified, propertiesHasNotToMatchCertified,
				JsonParseFlagEnum.ParseMetadata);
		if (certifiedNotHighestNodes.isRight() && certifiedNotHighestNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
			return Either.right(certifiedNotHighestNodes.right().value());
		}

		long endFetchAllStates = System.currentTimeMillis();

		List<GraphVertex> allNodes = new ArrayList<>();

		if (certifiedNotHighestNodes.isLeft()) {
			allNodes.addAll(certifiedNotHighestNodes.left().value());
		}
		if (highestNodes.isLeft()) {
			allNodes.addAll(highestNodes.left().value());
		}

		log.debug("Fetch catalog {}s all states from graph took {} ms", componentType, endFetchAllStates - startFetchAllStates);
		return Either.left(allNodes);
	}

	protected Either<List<GraphVertex>, StorageOperationStatus> getAllComponentsMarkedForDeletion(ComponentTypeEnum componentType) {

		// get all components marked for delete
		Map<GraphPropertyEnum, Object> props = new HashMap<GraphPropertyEnum, Object>();
		props.put(GraphPropertyEnum.IS_DELETED, true);
		props.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());

		Either<List<GraphVertex>, TitanOperationStatus> componentsToDelete = titanDao.getByCriteria(null, props, JsonParseFlagEnum.NoParse);

		if (componentsToDelete.isRight()) {
			TitanOperationStatus error = componentsToDelete.right().value();
			if (error.equals(TitanOperationStatus.NOT_FOUND)) {
				log.trace("no components to delete");
				return Either.left(new ArrayList<>());
			} else {
				log.info("failed to find components to delete. error : {}", error.name());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(error));
			}
		}
		return Either.left(componentsToDelete.left().value());
	}

	protected TitanOperationStatus setAdditionalInformationFromGraph(GraphVertex componentV, ToscaElement toscaElement) {
		Either<Map<String, AdditionalInfoParameterDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.ADDITIONAL_INFORMATION);
		if (result.isLeft()) {
			toscaElement.setAdditionalInformation(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	// --------------------------------------------
	public abstract <T extends ToscaElement> Either<T, StorageOperationStatus> getToscaElement(String uniqueId, ComponentParametersView componentParametersView);

	public abstract <T extends ToscaElement> Either<T, StorageOperationStatus> getToscaElement(GraphVertex toscaElementVertex, ComponentParametersView componentParametersView);

	public abstract <T extends ToscaElement> Either<T, StorageOperationStatus> deleteToscaElement(GraphVertex toscaElementVertex);

	public abstract <T extends ToscaElement> Either<T, StorageOperationStatus> createToscaElement(ToscaElement toscaElement);

	protected abstract <T extends ToscaElement> TitanOperationStatus setCategoriesFromGraph(GraphVertex vertexComponent, T toscaElement);

	protected abstract <T extends ToscaElement> TitanOperationStatus setCapabilitiesFromGraph(GraphVertex componentV, T toscaElement);

	protected abstract <T extends ToscaElement> TitanOperationStatus setRequirementsFromGraph(GraphVertex componentV, T toscaElement);

	protected abstract <T extends ToscaElement> StorageOperationStatus validateCategories(T toscaElementToUpdate, GraphVertex elementV);

	protected abstract <T extends ToscaElement> StorageOperationStatus updateDerived(T toscaElementToUpdate, GraphVertex updateElementV);

	public abstract <T extends ToscaElement> void fillToscaElementVertexData(GraphVertex elementV, T toscaElementToUpdate, JsonParseFlagEnum flag);

}
