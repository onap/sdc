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

package org.openecomp.sdc.be.ecomp.converters;

import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.distribution.servlet.DistributionCatalogServlet;
import org.openecomp.sdc.be.externalapi.servlet.representation.*;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@org.springframework.stereotype.Component("asset-metadata-utils")
public class AssetMetadataConverter {
    private static final Logger log = Logger.getLogger(DistributionCatalogServlet.class);

    @Autowired
    private ComponentsUtils componentsUtils;

    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    /*
     * Relative asset’s URL. Should be used in REST GET API to download the asset’s CSAR. https://{serverBaseURL}/{csarPath} can be obtained from (HttpServletRequest)request.getServerName()
     */
    public Either<List<? extends AssetMetadata>, ResponseFormat> convertToAssetMetadata(List<? extends Component> componentList, String serverBaseURL, boolean detailed) {
        if (componentList == null || componentList.isEmpty()) {
            return Either.left(new LinkedList<>());
        }
        List<AssetMetadata> retResList = new LinkedList<>();

        for (Component curr : componentList) {
            Either<? extends AssetMetadata, ResponseFormat> resMetaData = convertToSingleAssetMetadata(curr, serverBaseURL, detailed);
            if (resMetaData.isRight()) {
                return Either.right(resMetaData.right().value());
            }
            retResList.add(resMetaData.left().value());
        }

        return Either.left(retResList);

    }

    public <T extends Component> Either<? extends AssetMetadata, ResponseFormat> convertToSingleAssetMetadata(T component, String serverBaseURL, boolean detailed) {
        ComponentTypeEnum componentType = component.getComponentType();
        Either<? extends AssetMetadata, ResponseFormat> resMetaData = convertToMetadata(componentType, serverBaseURL, detailed, component);

        if (resMetaData.isRight()) {
            return Either.right(resMetaData.right().value());
        }
        else{
            return Either.left(resMetaData.left().value());
        }

    }

    private Either<? extends AssetMetadata, ResponseFormat> convertToMetadata(ComponentTypeEnum componentType, String serverBaseURL, boolean detailed, Component curr) {

        switch (componentType) {

        case RESOURCE:

            return generateResourceMeatdata(serverBaseURL, detailed, curr);

        case SERVICE:

            return generateServiceMetadata(serverBaseURL, detailed, curr);

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
            Either<ServiceAssetDetailedMetadata, StorageOperationStatus> converted = convertToServiceDetailedMetadata((ServiceAssetDetailedMetadata) metaData, (Service) curr);
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
        if(resource.getCategories() != null && !resource.getCategories().isEmpty()){
            CategoryDefinition categoryDefinition = resource.getCategories().get(0);
            assetToPopulate.setCategory(categoryDefinition.getName());
            assetToPopulate.setSubCategory(categoryDefinition.getSubcategories().get(0).getName());
        }
        assetToPopulate.setResourceType(resource.getResourceType().name());
        assetToPopulate.setLifecycleState(resource.getLifecycleState().name());
        assetToPopulate.setLastUpdaterUserId(resource.getLastUpdaterUserId());

        return (T) assetToPopulate;
    }

    private <T extends ServiceAssetMetadata> T convertToServiceAssetMetadata(T assetToPopulate, Service service, String serverBaseURL, boolean detailed) {
        assetToPopulate = convertToAsset(assetToPopulate, service, serverBaseURL, detailed);

        if(service.getCategories() != null && !service.getCategories().isEmpty()){
            CategoryDefinition categoryDefinition = service.getCategories().get(0);
            assetToPopulate.setCategory(categoryDefinition.getName());
        }

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
        assetToPopulate = populateResourceWithArtifacts(assetToPopulate, resource, deploymentArtifacts);

        assetToPopulate.setLastUpdaterFullName(resource.getLastUpdaterFullName());
        assetToPopulate.setToscaResourceName(resource.getToscaResourceName());
        assetToPopulate.setDescription(resource.getDescription());
        return Either.left(assetToPopulate);
    }

    private <T extends ServiceAssetDetailedMetadata> Either<T, StorageOperationStatus> convertToServiceDetailedMetadata(T assetToPopulate, Service service) {

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

    private <T extends ResourceAssetDetailedMetadata> T populateResourceWithArtifacts(T asset, Resource resource, Map<String, ArtifactDefinition> artifacts) {

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
        List<ArtifactMetadata> artifactMetaList =  new LinkedList<>();
        if (artifacts != null) {
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
        final String COMPONENT_ARTIFACT_URL = "/sdc/v1/catalog/%s/%s/artifacts/%s";

        final String RESOURCE_INSTANCE_ARTIFACT_URL = "/sdc/v1/catalog/%s/%s/resourceInstances/%s/artifacts/%s";

        ArtifactMetadata metadata = new ArtifactMetadata();

        metadata.setArtifactName(artifact.getArtifactName());
        metadata.setArtifactType(artifact.getArtifactType());

        if (resourceInstanceName == null || resourceInstanceName.isEmpty()) {
            metadata.setArtifactURL(String.format(COMPONENT_ARTIFACT_URL, componentType, componentUUID, artifact.getArtifactUUID()));
        } else {
            metadata.setArtifactURL(String.format(RESOURCE_INSTANCE_ARTIFACT_URL, componentType, componentUUID, resourceInstanceName, artifact.getArtifactUUID()));
        }

        metadata.setArtifactDescription(artifact.getDescription());
        metadata.setArtifactTimeout(artifact.getTimeout() != null && artifact.getTimeout() > 0 ? artifact.getTimeout() : null);
        metadata.setArtifactChecksum(artifact.getArtifactChecksum());
        metadata.setArtifactUUID(artifact.getArtifactUUID());
        metadata.setArtifactVersion(artifact.getArtifactVersion());
        metadata.setGeneratedFromUUID(artifact.getGeneratedFromId());
        metadata.setArtifactLabel(artifact.getArtifactLabel());
        metadata.setArtifactGroupType(artifact.getArtifactGroupType().getType());
        return metadata;
    }

    private Either<List<ResourceInstanceMetadata>, StorageOperationStatus> convertToResourceInstanceMetadata(List<ComponentInstance> componentInstances, String componentType, String componentUUID) {
        List<ResourceInstanceMetadata> retList = new LinkedList<>();
        Map<String, ImmutablePair<String, String>> uuidDuplicatesMap = new HashMap<>();

        for (ComponentInstance componentInstance : componentInstances) {
            ResourceInstanceMetadata metadata = new ResourceInstanceMetadata();
            String componentUid = componentInstance.getComponentUid();
            String invariantUUID, resourceUUID;

            if (!uuidDuplicatesMap.containsKey(componentUid)) {
                Either<Resource, StorageOperationStatus> eitherResource = toscaOperationFacade.getToscaElement(componentInstance.getComponentUid());
                if (eitherResource.isRight()) {
                    log.debug("convertToResourceInstanceMetadata: Failed getting resource with Uid: {}", componentInstance.getComponentUid());
                    return Either.right(eitherResource.right().value());
                } else {
                    final Resource resource = eitherResource.left().value();
                    invariantUUID = resource.getInvariantUUID();
                    resourceUUID = resource.getUUID();
                    ImmutablePair<String, String> uuidInvariantUUIDPair = new ImmutablePair<>(resourceUUID, invariantUUID);
                    uuidDuplicatesMap.put(componentUid, uuidInvariantUUIDPair);
                }
            } else {
                invariantUUID = uuidDuplicatesMap.get(componentUid).getRight();
                resourceUUID =  uuidDuplicatesMap.get(componentUid).getLeft();
            }

            metadata.setResourceInvariantUUID(invariantUUID);
            metadata.setResourceUUID(resourceUUID);
            metadata.setResourceInstanceName(componentInstance.getName());
            metadata.setResourceName(componentInstance.getComponentName());
            metadata.setResourceVersion(componentInstance.getComponentVersion());
            metadata.setResoucreType(componentInstance.getOriginType().getValue());

            if(MapUtils.isNotEmpty(componentInstance.getDeploymentArtifacts())){
                LinkedList<ArtifactMetadata> artifactMetaList = new LinkedList<>();
                Collection<ArtifactDefinition> values = componentInstance.getDeploymentArtifacts().values();
                for (ArtifactDefinition artifactDefinition : values) {
                    ArtifactMetadata converted = convertToArtifactMetadata(artifactDefinition, componentType, componentUUID, componentInstance.getNormalizedName());
                    artifactMetaList.add(converted);
                }
                metadata.setArtifacts(artifactMetaList);
            }
            retList.add(metadata);
        }
        return Either.left(retList);
    }

}
