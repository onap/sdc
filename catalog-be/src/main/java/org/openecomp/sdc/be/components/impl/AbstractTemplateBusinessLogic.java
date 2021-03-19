/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.externalapi.servlet.representation.AbstractResourceInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.AbstractTemplateInfo;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
@org.springframework.stereotype.Component("abstractTemplateBusinessLogic")
public class AbstractTemplateBusinessLogic extends BaseBusinessLogic {

    private static final String INITIAL_VERSION = "0.1";
    private static final Logger log = Logger.getLogger(AbstractTemplateBusinessLogic.class);
    @Autowired
    protected ServiceImportManager serviceImportManager;
    @Autowired
    protected ServiceBusinessLogic serviceBusinessLogic;
    @Autowired
    ServiceDistributionValidation serviceDistributionValidation;

    @Autowired
    public AbstractTemplateBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                         IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                                         InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
    }

    public Either<AbstractTemplateInfo, ResponseFormat> getServiceAbstractStatus(List<? extends Component> componentList) {
        AbstractTemplateInfo abstractTemplateInfo = new AbstractTemplateInfo();
        if (componentList == null || componentList.isEmpty()) {
            return Either.left(abstractTemplateInfo);
        }
        for (Component curr : componentList) {
            Service service = (Service) curr;
            List<ComponentInstance> componentInstances = service.getComponentInstances();
            List<RequirementCapabilityRelDef> componentInstancesRelations = service.getComponentInstancesRelations();
            abstractTemplateInfo.setServiceUniqueId(service.getUniqueId());
            String serviceUniqueId = abstractTemplateInfo.getServiceUniqueId();
            Either<Boolean, ResponseFormat> isAbstractResourceData = getEveryServiceAbstractStatus(componentInstances, abstractTemplateInfo,
                componentInstancesRelations, serviceUniqueId);
            if (isAbstractResourceData.isRight()) {
                return Either.right(isAbstractResourceData.right().value());
            }
        }
        return Either.left(abstractTemplateInfo);
    }

    private Either<Boolean, ResponseFormat> getEveryServiceAbstractStatus(List<ComponentInstance> componentInstances,
                                                                          AbstractTemplateInfo abstractTemplateInfo,
                                                                          List<RequirementCapabilityRelDef> componentInstancesRelations,
                                                                          String serviceUniqueId) {
        Map<String, ImmutablePair<String, String>> uuidDuplicatesMap = new HashMap<>();
        List<AbstractResourceInfo> abstractResourceInfoList = new ArrayList<>();
        Boolean isContainAbstractResource = false;
        for (ComponentInstance componentInstance : componentInstances) {
            String componentUid = componentInstance.getComponentUid();
            String invariantUUID, resourceUUID;
            if (!uuidDuplicatesMap.containsKey(componentUid)) {
                Either<Resource, StorageOperationStatus> eitherResource = toscaOperationFacade.getToscaElement(componentInstance.getComponentUid());
                if (eitherResource.isRight()) {
                    log.debug("getEveryServiceAbstractStatus: Failed getting resource with UUid: {}", componentInstance.getComponentUid());
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(eitherResource.right().value());
                    return Either.right(responseFormat);
                } else {
                    final Resource resource = eitherResource.left().value();
                    invariantUUID = resource.getInvariantUUID();
                    resourceUUID = resource.getUUID();
                    ImmutablePair<String, String> uuidInvariantUUIDPair = new ImmutablePair<>(resourceUUID, invariantUUID);
                    uuidDuplicatesMap.put(componentUid, uuidInvariantUUIDPair);
                    isContainAbstractResource = getResourceAbstractStatus(resource, isContainAbstractResource, componentInstancesRelations,
                        serviceUniqueId, abstractResourceInfoList);
                }
            }
        }
        abstractTemplateInfo.setIsAbstractTemplate(isContainAbstractResource);
        abstractTemplateInfo.setAbstractResourceInfoList(abstractResourceInfoList);
        return Either.left(isContainAbstractResource);
    }

    private boolean getResourceAbstractStatus(Resource resource, Boolean isContainAbstractResource,
                                              List<RequirementCapabilityRelDef> componentInstancesRelations, String serviceUniqueId,
                                              List<AbstractResourceInfo> abstractResourceInfoList) {
        boolean isAbstract = getIsAbstract(resource.getCategories());
        log.debug("before if isAbstract,get resource:{}", resource);
        if (isAbstract) {
            log.debug("getResourceAbstractStatus: resource {} with id {} ,NormalizedName:{},isAbstract{} is abstract resource", resource.getName(),
                resource.getUUID(), resource.getNormalizedName(), true);
            isContainAbstractResource = true;
            AbstractResourceInfo abstractResourceInfo = getAbstractResourceInfo(resource, componentInstancesRelations, serviceUniqueId);
            abstractResourceInfoList.add(abstractResourceInfo);
        } else {
            log.debug("getResourceAbstractStatus:resource {} ,with id {} isAbstract{} is missing the isAbstract parameter", resource.getName(),
                resource.getUUID(), false);
        }
        return isContainAbstractResource;
    }

    private AbstractResourceInfo getAbstractResourceInfo(Resource resource, List<RequirementCapabilityRelDef> componentInstancesRelations,
                                                         String serviceUniqueId) {
        AbstractResourceInfo abstractResourceInfo = new AbstractResourceInfo();
        abstractResourceInfo.setAbstractResourceUUid(resource.getUUID());
        abstractResourceInfo.setAbstractResourceName(resource.getName());
        abstractResourceInfo.setAbstractResourceUniqueId(resource.getUniqueId());
        String uniqueId = serviceUniqueId + "." + resource.getUniqueId() + "." + resource.getNormalizedName();
        List<RequirementCapabilityRelDef> resourceComponentInstancesRelations = new ArrayList<>();
        log.debug("get serviceUniqueId:{},get UniqueId:{},get NormalizedName:{}", serviceUniqueId, resource.getUniqueId(),
            resource.getNormalizedName());
        log.debug("get is Abstract,componentInstancesRelations:{}", componentInstancesRelations);
        for (RequirementCapabilityRelDef componentInstancesRelation : componentInstancesRelations) {
            log.debug("for componentInstancesRelation,get componentInstancesRelation:{}", componentInstancesRelation);
            String toNode = componentInstancesRelation.getToNode();
            String fromNode = componentInstancesRelation.getFromNode();
            log.debug("for componentInstancesRelation,get fromNode:{},uniqueId:{},ToNode:{},Relationships:{}", fromNode, uniqueId, toNode,
                componentInstancesRelation.getRelationships());
            if (toNode.toUpperCase().contains(uniqueId.toUpperCase()) || fromNode.toUpperCase().contains(uniqueId.toUpperCase())) {
                RequirementCapabilityRelDef resourceComponentInstancesRelation = new RequirementCapabilityRelDef();
                log.debug("fromNode contains name,get componentInstancesRelation:{}", componentInstancesRelation);
                resourceComponentInstancesRelation.setFromNode(componentInstancesRelation.getFromNode());
                resourceComponentInstancesRelation.setOriginUI(componentInstancesRelation.isOriginUI());
                resourceComponentInstancesRelation.setRelationships(componentInstancesRelation.getRelationships());
                resourceComponentInstancesRelation.setToNode(componentInstancesRelation.getToNode());
                resourceComponentInstancesRelation.setUid(componentInstancesRelation.getUid());
                resourceComponentInstancesRelations.add(resourceComponentInstancesRelation);
            }
        }
        abstractResourceInfo.setComponentInstancesRelations(resourceComponentInstancesRelations);
        return abstractResourceInfo;
    }

    private boolean getIsAbstract(List<CategoryDefinition> categories) {
        boolean anAbstract = false;
        if (categories != null && !categories.isEmpty()) {
            CategoryDefinition categoryDef = categories.get(0);
            if (categoryDef != null && categoryDef.getName() != null && categoryDef.getName().equals(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME)) {
                SubCategoryDefinition subCategoryDef = categoryDef.getSubcategories().get(0);
                if (subCategoryDef != null && subCategoryDef.getName().equals(ImportUtils.Constants.ABSTRACT_SUBCATEGORY)) {
                    anAbstract = true;
                }
            }
        }
        return anAbstract;
    }
}
