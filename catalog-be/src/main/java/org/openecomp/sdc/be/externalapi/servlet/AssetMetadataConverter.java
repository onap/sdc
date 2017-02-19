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

package org.openecomp.sdc.be.externalapi.servlet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.distribution.servlet.DistributionCatalogServlet;
import org.openecomp.sdc.be.externalapi.servlet.representation.ArtifactMetadata;
import org.openecomp.sdc.be.externalapi.servlet.representation.AssetMetadata;
import org.openecomp.sdc.be.externalapi.servlet.representation.ResourceAssetDetailedMetadata;
import org.openecomp.sdc.be.externalapi.servlet.representation.ResourceAssetMetadata;
import org.openecomp.sdc.be.externalapi.servlet.representation.ResourceInstanceMetadata;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceAssetDetailedMetadata;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceAssetMetadata;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

@org.springframework.stereotype.Component("asset-metadata-utils")
public class AssetMetadataConverter {
	private static Logger log = LoggerFactory.getLogger(DistributionCatalogServlet.class.getName());

	@Autowired
	private ComponentsUtils componentsUtils;
	@Autowired
	private ResourceOperation resourceOperation;

	/*
	 * Relative asset’s URL. Should be used in REST GET API to download the asset’s CSAR. https://{serverBaseURL}/{csarPath} can be obtained from (HttpServletRequest)request.getServerName()
	 */
	public Either<List<? extends AssetMetadata>, ResponseFormat> convertToAssetMetadata(List<? extends Component> componentList, String serverBaseURL, boolean detailed) {
		if (componentList == null || componentList.isEmpty()) {
			return Either.left(new LinkedList<>());
		}

		List<AssetMetadata> retResList = new LinkedList<>();
		Component component = componentList.iterator().next();
		ComponentTypeEnum componentType = component.getComponentType();

		for (Component curr : componentList) {

			Either<? extends AssetMetadata, ResponseFormat> resMetaData = convertToMetadata(componentType, serverBaseURL, detailed, curr);

			if (resMetaData.isRight()) {
				return Either.right(resMetaData.right().value());
			}

			retResList.add(resMetaData.left().value());
		}

		return Either.left(retResList);

	}

	private Either<? extends AssetMetadata, ResponseFormat> convertToMetadata(ComponentTypeEnum componentType, String serverBaseURL, boolean detailed, Component curr) {

		switch (componentType) {

		case RESOURCE:

			return generateResourceMeatdata(serverBaseURL, detailed, curr);

		case SERVICE:

			return generateServiceMetadata(serverBaseURL, detailed, curr);

		// For future US's that include product
		/*
		 * case PRODUCT: if (component instanceof Product) { List<ProductAssetMetadata> retResList = new LinkedList<>(); for (Component curr : componentList) { retResList.add(convertToProductAssetMetadata((Product) curr, serverBaseURL)); } return
		 * Either.left(retResList);
		 */
		default:

			ResponseFormat responseFormat = componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.COMPONENT_INVALID_CATEGORY);
			return Either.right(responseFormat);
		}
	}

	private Either<? extends AssetMetadata, ResponseFormat> generateResourceMeatdata(String serverBaseURL, boolean detailed, Component curr) {
		AssetMetadata metaData;
		metaData = createMetadaObject(detailed, curr.getComponentType());
		metaData = convertToResourceMetadata((ResourceAssetMetadata) metaData, (Resource) curr, serverBaseURL, detailed);

		if (detailed) {
			Either<ResourceAssetDetailedMetadata, StorageOperationStatus> converted = convertToResourceDetailedMetadata((ResourceAssetDetailedMetadata) metaData, (Resource) curr, serverBaseURL);
			if (converted.isRight()) {
				ActionStatus storageResponse = componentsUtils.convertFromStorageResponse(converted.right().value(), ComponentTypeEnum.RESOURCE);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(storageResponse);
				return Either.right(responseFormat);
			}
		}

		return Either.left(metaData);
	}

	private AssetMetadata createMetadaObject(boolean detailed, ComponentTypeEnum type) {
		AssetMetadata metaData = null;
		switch (type) {
		case SERVICE:
			if (!detailed) {
				metaData = new ServiceAssetMetadata();
			} else {
				metaData = new ServiceAssetDetailedMetadata();
			}
			break;
		case RESOURCE:
			if (!detailed) {
				metaData = new ResourceAssetMetadata();
			} else {
				metaData = new ResourceAssetDetailedMetadata();
			}
			break;
		default:
			break;
		}
		return metaData;
	}

	private Either<? extends AssetMetadata, ResponseFormat> generateServiceMetadata(String serverBaseURL, boolean detailed, Component curr) {
		AssetMetadata metaData = createMetadaObject(detailed, curr.getComponentType());

		metaData = convertToServiceAssetMetadata((ServiceAssetMetadata) metaData, (Service) curr, serverBaseURL, detailed);

		if (detailed) {
			Either<ServiceAssetDetailedMetadata, StorageOperationStatus> converted = convertToServiceDetailedMetadata((ServiceAssetDetailedMetadata) metaData, (Service) curr, serverBaseURL);
			if (converted.isRight()) {
				ActionStatus storageResponse = componentsUtils.convertFromStorageResponse(converted.right().value(), ComponentTypeEnum.RESOURCE);
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(storageResponse);
				return Either.right(responseFormat);
			}
		}

		return Either.left(metaData);
	}

	private <U extends AssetMetadata, T extends Component> U convertToAsset(U asset, T component, String serverBaseURL, boolean detailed) {
		asset.setUuid(component.getUUID());
		asset.setInvariantUUID(component.getInvariantUUID());
		asset.setName(component.getName());
		asset.setVersion(component.getVersion());
		if (!detailed) {
			asset.setToscaModelURL(serverBaseURL + "/" + component.getUUID() + "/toscaModel");
		} else {
			String toscaModelUrl = (new String(serverBaseURL)).replace("metadata", "toscaModel");
			asset.setToscaModelURL(toscaModelUrl);
		}

		return asset;
	}

	private <T extends ResourceAssetMetadata> T convertToResourceMetadata(T assetToPopulate, Resource resource, String serverBaseURL, boolean detailed) {
		assetToPopulate = convertToAsset(assetToPopulate, resource, serverBaseURL, detailed);
		CategoryDefinition categoryDefinition = resource.getCategories().iterator().next();
		assetToPopulate.setCategory(categoryDefinition.getName());

		SubCategoryDefinition subCategoryDefinition = categoryDefinition.getSubcategories().iterator().next();

		assetToPopulate.setSubCategory(subCategoryDefinition.getName());
		assetToPopulate.setResourceType(resource.getResourceType().name());
		assetToPopulate.setLifecycleState(resource.getLifecycleState().name());
		assetToPopulate.setLastUpdaterUserId(resource.getLastUpdaterUserId());

		return (T) assetToPopulate;
	}

	private <T extends ServiceAssetMetadata> T convertToServiceAssetMetadata(T assetToPopulate, Service service, String serverBaseURL, boolean detailed) {
		assetToPopulate = convertToAsset(assetToPopulate, service, serverBaseURL, detailed);

		CategoryDefinition categoryDefinition = service.getCategories().iterator().next();

		assetToPopulate.setCategory(categoryDefinition.getName());
		assetToPopulate.setLifecycleState(service.getLifecycleState().name());
		assetToPopulate.setLastUpdaterUserId(service.getLastUpdaterUserId());
		assetToPopulate.setDistributionStatus(service.getDistributionStatus().name());

		return (T) assetToPopulate;
	}

	private <T extends ResourceAssetDetailedMetadata> Either<T, StorageOperationStatus> convertToResourceDetailedMetadata(T assetToPopulate, Resource resource, String serverBaseURL) {

		List<ComponentInstance> componentInstances = resource.getComponentInstances();

		if (componentInstances != null) {
			Either<List<ResourceInstanceMetadata>, StorageOperationStatus> resourceInstanceMetadata = convertToResourceInstanceMetadata(componentInstances, ComponentTypeEnum.RESOURCE_PARAM_NAME, resource.getUUID());
			if (resourceInstanceMetadata.isRight()) {
				return Either.right(resourceInstanceMetadata.right().value());
			}

			assetToPopulate.setResources(resourceInstanceMetadata.left().value());
		}

		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		assetToPopulate = populateResourceWithArtifacts(assetToPopulate, resource, serverBaseURL, deploymentArtifacts);

		assetToPopulate.setLastUpdaterFullName(resource.getLastUpdaterFullName());
		assetToPopulate.setToscaResourceName(resource.getToscaResourceName());

		return Either.left(assetToPopulate);
	}

	private <T extends ServiceAssetDetailedMetadata> Either<T, StorageOperationStatus> convertToServiceDetailedMetadata(T assetToPopulate, Service service, String serverBaseURL) {

		List<ComponentInstance> componentInstances = service.getComponentInstances();

		if (componentInstances != null) {
			Either<List<ResourceInstanceMetadata>, StorageOperationStatus> resourceInstanceMetadata = convertToResourceInstanceMetadata(componentInstances, ComponentTypeEnum.SERVICE_PARAM_NAME, service.getUUID());
			if (resourceInstanceMetadata.isRight()) {
				return Either.right(resourceInstanceMetadata.right().value());
			}

			assetToPopulate.setResources(resourceInstanceMetadata.left().value());
		}

		Map<String, ArtifactDefinition> deploymentArtifacts = service.getDeploymentArtifacts();
		assetToPopulate = populateServiceWithArtifacts(assetToPopulate, service, deploymentArtifacts);

		assetToPopulate.setLastUpdaterFullName(service.getLastUpdaterFullName());

		return Either.left(assetToPopulate);
	}

	private <T extends ResourceAssetDetailedMetadata> T populateResourceWithArtifacts(T asset, Resource resource, String serverBaseURL, Map<String, ArtifactDefinition> artifacts) {

		List<ArtifactMetadata> artifactMetaList = populateAssetWithArtifacts(resource, artifacts);

		asset.setArtifacts(artifactMetaList);

		return asset;
	}

	private <T extends ServiceAssetDetailedMetadata> T populateServiceWithArtifacts(T asset, Service service, Map<String, ArtifactDefinition> artifacts) {

		List<ArtifactMetadata> artifactMetaList = populateAssetWithArtifacts(service, artifacts);

		asset.setArtifacts(artifactMetaList);

		return asset;
	}

	private List<ArtifactMetadata> populateAssetWithArtifacts(Component component, Map<String, ArtifactDefinition> artifacts) {
		List<ArtifactMetadata> artifactMetaList = null;
		if (artifacts != null) {
			artifactMetaList = new LinkedList<>();
			Collection<ArtifactDefinition> artefactDefList = artifacts.values();

			for (ArtifactDefinition artifactDefinition : artefactDefList) {
				if (artifactDefinition.getEsId() != null && !artifactDefinition.getEsId().isEmpty()) {
					ArtifactMetadata convertedArtifactMetadata = convertToArtifactMetadata(artifactDefinition, ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUUID(), null);
					artifactMetaList.add(convertedArtifactMetadata);
				}
			}
		}
		return artifactMetaList.isEmpty() ? null : artifactMetaList;
	}

	private ArtifactMetadata convertToArtifactMetadata(ArtifactDefinition artifact, String componentType, String componentUUID, String resourceInstanceName) {
		// /asdc/v1/catalog/{services/resources}/{componentUUID}/artifacts/{artifactUUID}
		final String COMPONENT_ARTIFACT_URL = "/asdc/v1/catalog/%s/%s/artifacts/%s";

		// /asdc/v1/catalog/{services/resources}/{componentUUID}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}
		final String RESOURCE_INSTANCE_ARTIFACT_URL = "/asdc/v1/catalog/%s/%s/resourceInstances/%s/artifacts/%s";

		ArtifactMetadata metadata = new ArtifactMetadata();

		metadata.setArtifactName(artifact.getArtifactName());
		metadata.setArtifactType(artifact.getArtifactType());

		if (resourceInstanceName == null || resourceInstanceName.isEmpty()) {
			metadata.setArtifactURL(String.format(COMPONENT_ARTIFACT_URL, componentType, componentUUID, artifact.getArtifactUUID()));
		} else {
			metadata.setArtifactURL(String.format(RESOURCE_INSTANCE_ARTIFACT_URL, componentType, componentUUID, resourceInstanceName, artifact.getArtifactUUID()));
		}

		metadata.setArtifactDescription(artifact.getDescription());
		metadata.setArtifactTimeout(artifact.getTimeout() > 0 ? artifact.getTimeout() : null);
		metadata.setArtifactChecksum(artifact.getArtifactChecksum());
		metadata.setArtifactUUID(artifact.getArtifactUUID());
		metadata.setArtifactVersion(artifact.getArtifactVersion());
		metadata.setGeneratedFromUUID(artifact.getGeneratedFromId());

		return metadata;
	}

	private Either<List<ResourceInstanceMetadata>, StorageOperationStatus> convertToResourceInstanceMetadata(List<ComponentInstance> componentInstances, String componentType, String componentUUID) {
		List<ResourceInstanceMetadata> retList = new LinkedList<>();
		Map<String, String> uuidDuplicatesMap = new HashMap<>();

		for (ComponentInstance componentInstance : componentInstances) {
			ResourceInstanceMetadata metadata = new ResourceInstanceMetadata();
			String componentUid = componentInstance.getComponentUid();
			String invariantUUID;

			if (!uuidDuplicatesMap.containsKey(componentUid)) {
				Either<String, StorageOperationStatus> getInvarUuidresponse = resourceOperation.getInvariantUUID(NodeTypeEnum.Resource, componentInstance.getComponentUid(), false);
				if (getInvarUuidresponse.isRight()) {
					log.debug("convertToResourceInstanceMetadata: Failed getting Invariant UUID");
					return Either.right(getInvarUuidresponse.right().value());
				} else {
					invariantUUID = getInvarUuidresponse.left().value();
					uuidDuplicatesMap.put(componentUid, invariantUUID);
				}
			} else {
				invariantUUID = uuidDuplicatesMap.get(componentUid);
			}

			metadata.setResourceInvariantUUID(invariantUUID);
			metadata.setResourceInstanceName(componentInstance.getName());
			metadata.setResourceName(componentInstance.getComponentName());
			metadata.setResourceVersion(componentInstance.getComponentVersion());
			metadata.setResoucreType(componentInstance.getOriginType().getValue());
			metadata.setResourceUUID(componentInstance.getComponentUid());

			Collection<ArtifactDefinition> values = componentInstance.getDeploymentArtifacts().values();
			LinkedList<ArtifactMetadata> artifactMetaList = new LinkedList<>();

			for (ArtifactDefinition artifactDefinition : values) {
				ArtifactMetadata converted = convertToArtifactMetadata(artifactDefinition, componentType, componentUUID, componentInstance.getNormalizedName());
				artifactMetaList.add(converted);
			}

			metadata.setArtifacts(artifactMetaList);

			retList.add(metadata);
		}

		return Either.left(retList);
	}

	// For future US to support Product
	/*
	 * private ProductAssetMetadata convertToProductAssetMetadata(Product product, String serverBaseURL) { ProductAssetMetadata retProdAsset = new ProductAssetMetadata();
	 * 
	 * retProdAsset = convertToAsset(retProdAsset, product, serverBaseURL); retProdAsset.setLifecycleState(product.getLifecycleState().name()); retProdAsset.setLastUpdaterUserId(product.getLastUpdaterUserId());
	 * retProdAsset.setActive(product.getIsActive()); retProdAsset.setContacts(product.getContacts());
	 * 
	 * List<CategoryDefinition> categories = product.getCategories(); List<ProductCategoryGroupMetadata> categoryMetadataList = new LinkedList<>();
	 * 
	 * if (categories == null || categories.isEmpty()) { return retProdAsset; } else { for (CategoryDefinition categoryDefinition : categories) { String categoryName = categoryDefinition.getName(); List<SubCategoryDefinition> subcategories =
	 * categoryDefinition.getSubcategories(); for (SubCategoryDefinition subCategoryDefinition : subcategories) { String subCategoryName = subCategoryDefinition.getName(); List<GroupDefinition> groups = product.getGroups(); for (GroupDefinition
	 * groupDefinition : groups) { String groupName = groupDefinition.getName(); categoryMetadataList.add(new ProductCategoryGroupMetadata(categoryName, subCategoryName, groupName)); } } } retProdAsset.setProductGroupings(categoryMetadataList);
	 * return retProdAsset; } }
	 */
}
