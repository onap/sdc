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

package org.openecomp.sdc.be.components.distribution.engine;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("serviceDistributionArtifactsBuilder")
public class ServiceDistributionArtifactsBuilder {

    private static final Logger logger = Logger.getLogger(ServiceDistributionArtifactsBuilder.class.getName());

    private static final String BASE_ARTIFACT_URL = "/sdc/v1/catalog/services/%s/%s/";
    private static final String SERVICE_ARTIFACT_URL = BASE_ARTIFACT_URL + "artifacts/%s";
    private static final String RESOURCE_INSTANCE_ARTIFACT_URL = BASE_ARTIFACT_URL + "resourceInstances/%s/artifacts/%s";

    @javax.annotation.Resource
    InterfaceLifecycleOperation interfaceLifecycleOperation;

    @javax.annotation.Resource
    IArtifactOperation artifactOperation;

    @Autowired
    ToscaOperationFacade toscaOperationFacade;

    public InterfaceLifecycleOperation getInterfaceLifecycleOperation() {
        return interfaceLifecycleOperation;
    }

    public void setInterfaceLifecycleOperation(InterfaceLifecycleOperation interfaceLifecycleOperation) {
        this.interfaceLifecycleOperation = interfaceLifecycleOperation;
    }

    private String resolveWorkloadContext(String workloadContext) {
        return workloadContext != null ? workloadContext :
                ConfigurationManager.getConfigurationManager().getConfiguration().getWorkloadContext();
    }

    public INotificationData buildResourceInstanceForDistribution(Service service, String distributionId, String workloadContext) {
        INotificationData notificationData = new NotificationDataImpl();

        notificationData.setResources(convertRIsToJsonContanier(service));
        notificationData.setServiceName(service.getName());
        notificationData.setServiceVersion(service.getVersion());
        notificationData.setDistributionID(distributionId);
        notificationData.setServiceUUID(service.getUUID());
        notificationData.setServiceDescription(service.getDescription());
        notificationData.setServiceInvariantUUID(service.getInvariantUUID());
        workloadContext = resolveWorkloadContext(workloadContext);
        if (workloadContext!=null){
            notificationData.setWorkloadContext(workloadContext);
        }
        logger.debug("Before returning notification data object {}", notificationData);

        return notificationData;
    }

    public INotificationData buildServiceForDistribution(INotificationData notificationData, Service service) {

        notificationData.setServiceArtifacts(convertServiceArtifactsToArtifactInfo(service));

        logger.debug("Before returning notification data object {}", notificationData);

        return notificationData;
    }

    private List<ArtifactInfoImpl> convertServiceArtifactsToArtifactInfo(Service service) {

        Map<String, ArtifactDefinition> serviceArtifactsMap = service.getDeploymentArtifacts();
        List<ArtifactDefinition> extractedServiceArtifacts = serviceArtifactsMap.values().stream()
                //filters all artifacts with existing EsId
                .filter(ArtifactDefinition::checkEsIdExist)
                //collects all filtered artifacts with existing EsId to List
                .collect(Collectors.toList());

        Optional<ArtifactDefinition> toscaTemplateArtifactOptl = exrtactToscaTemplateArtifact(service);
        if(toscaTemplateArtifactOptl.isPresent()){
            extractedServiceArtifacts.add(toscaTemplateArtifactOptl.get());
        }

        Optional<ArtifactDefinition> toscaCsarArtifactOptl = exrtactToscaCsarArtifact(service);
        if(toscaCsarArtifactOptl.isPresent()){
            extractedServiceArtifacts.add(toscaCsarArtifactOptl.get());
        }

        return ArtifactInfoImpl.convertServiceArtifactToArtifactInfoImpl(service, extractedServiceArtifacts);
    }

    private Optional<ArtifactDefinition> exrtactToscaTemplateArtifact(Service service) {
        return service.getToscaArtifacts().values().stream()
                //filters TOSCA_TEMPLATE artifact
                .filter(e -> e.getArtifactType().equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType())).findAny();
    }

    private Optional<ArtifactDefinition> exrtactToscaCsarArtifact(Service service) {
        return service.getToscaArtifacts().values().stream()
                //filters TOSCA_CSAR artifact
                .filter(e -> e.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())).findAny();
    }

    private List<JsonContainerResourceInstance> convertRIsToJsonContanier(Service service) {
        List<JsonContainerResourceInstance> ret = new ArrayList<>();
        if (service.getComponentInstances() != null) {
            for (ComponentInstance instance : service.getComponentInstances()) {
                JsonContainerResourceInstance jsonContainer = new JsonContainerResourceInstance(instance, convertToArtifactsInfoImpl(service, instance));
                ComponentParametersView filter = new ComponentParametersView();
                filter.disableAll();
                filter.setIgnoreCategories(false);
                toscaOperationFacade.getToscaElement(instance.getComponentUid(), filter)
                    .left()
                    .bind(r->{fillJsonContainer(jsonContainer, (Resource) r); return Either.left(r);})
                    .right()
                    .forEach(r->logger.debug("Resource {} Invariant UUID & Categories retrieving failed", instance.getComponentUid()));
                ret.add(jsonContainer);
            }
        }
        return ret;
    }

    private void fillJsonContainer(JsonContainerResourceInstance jsonContainer, Resource resource) {
        jsonContainer.setResourceInvariantUUID(resource.getInvariantUUID());
        setCategories(jsonContainer, resource.getCategories());
    }

    private List<ArtifactInfoImpl> convertToArtifactsInfoImpl(Service service, ComponentInstance resourceInstance) {
        List<ArtifactInfoImpl> artifacts = ArtifactInfoImpl.convertToArtifactInfoImpl(service, resourceInstance, getArtifactsWithPayload(resourceInstance));
        artifacts.stream().forEach(ArtifactInfoImpl::updateArtifactTimeout);
        return artifacts;
    }

    private void setCategories(JsonContainerResourceInstance jsonContainer, List<CategoryDefinition> categories) {
        if (categories != null) {
            CategoryDefinition categoryDefinition = categories.get(0);

            if (categoryDefinition != null) {
                jsonContainer.setCategory(categoryDefinition.getName());
                List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
                if (null != subcategories) {
                    SubCategoryDefinition subCategoryDefinition = subcategories.get(0);

                    if (subCategoryDefinition != null) {
                        jsonContainer.setSubcategory(subCategoryDefinition.getName());
                    }
                }
            }
        }
    }

    private List<ArtifactDefinition> getArtifactsWithPayload(ComponentInstance resourceInstance) {
        List<ArtifactDefinition> ret = new ArrayList<>();

        List<ArtifactDefinition> deployableArtifacts = new ArrayList<>();
        if (resourceInstance.getDeploymentArtifacts() != null) {
            deployableArtifacts.addAll(resourceInstance.getDeploymentArtifacts().values());
        }

        for (ArtifactDefinition artifactDef : deployableArtifacts) {
            if (artifactDef.checkEsIdExist()) {
                ret.add(artifactDef);
            }
        }

        return ret;
    }

    /**
     * build the URL for resource instance artifact
     *
     * @param    service
     * @param    resourceInstance
     * @param    artifactName
     * @return    URL string
     */
    public static String buildResourceInstanceArtifactUrl(Service service, ComponentInstance resourceInstance,
            String artifactName) {

        String url = String.format(RESOURCE_INSTANCE_ARTIFACT_URL, service.getSystemName(), service.getVersion(),
                resourceInstance.getNormalizedName(), artifactName);

        logger.debug("After building artifact url {}", url);

        return url;
    }

    /**
     * build the URL for resource instance artifact
     *
     * @param    service
     * @param    artifactName
     * @return    URL string
     */
    public static String buildServiceArtifactUrl(Service service, String artifactName) {

        String url = String.format(SERVICE_ARTIFACT_URL, service.getSystemName(), service.getVersion(), artifactName);

        logger.debug("After building artifact url {}", url);

        return url;

    }

    /**
     * Verifies that the service or at least one of its instance contains deployment artifacts
     *
     * @param    the service
     * @return    boolean
     */
    public boolean verifyServiceContainsDeploymentArtifacts(Service service) {
        if (MapUtils.isNotEmpty(service.getDeploymentArtifacts())) {
            return true;
        }
        boolean contains = false;
        List<ComponentInstance> resourceInstances = service.getComponentInstances();
        if (CollectionUtils.isNotEmpty(resourceInstances)) {
            contains = resourceInstances.stream().anyMatch(i -> isContainsPayload(i.getDeploymentArtifacts()));
        }
        return contains;
    }

    private boolean isContainsPayload(Map<String, ArtifactDefinition> deploymentArtifacts) {
       return deploymentArtifacts != null && deploymentArtifacts.values().stream().anyMatch(ArtifactDefinition::checkEsIdExist);
    }

}
