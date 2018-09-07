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

package org.openecomp.sdc.be.components.impl;

import com.google.common.collect.Sets;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.instance.ComponentInstanceChangeOperationOrchestrator;
import org.openecomp.sdc.be.components.merge.instance.ComponentInstanceMergeDataBusinessLogic;
import org.openecomp.sdc.be.components.merge.instance.DataForMergeHolder;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition.PropertyNames;
import org.openecomp.sdc.be.model.jsontitan.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.components.property.GetInputUtils.isGetInputValueForInput;

@org.springframework.stereotype.Component
public class ComponentInstanceBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(ComponentInstanceBusinessLogic.class.getName());
    private static final String VF_MODULE = "org.openecomp.groups.VfModule";
    public static final String TRY_TO_CREATE_ENTRY_ON_GRAPH = "Try to create entry on graph";
    public static final String FAILED_TO_CREATE_ENTRY_ON_GRAPH_FOR_COMPONENT_INSTANCE = "Failed to create entry on graph for component instance {}";
    public static final String ENTITY_ON_GRAPH_IS_CREATED = "Entity on graph is created.";
    public static final String INVALID_COMPONENT_TYPE = "invalid component type";
    public static final String FAILED_TO_RETRIEVE_COMPONENT_COMPONENT_ID = "Failed to retrieve component, component id {}";
    public static final String FAILED_TO_LOCK_SERVICE = "Failed to lock service {}";
    public static final String CREATE_OR_UPDATE_PROPERTY_VALUE = "CreateOrUpdatePropertyValue";

    @Autowired
    private IComponentInstanceOperation componentInstanceOperation;

    @Autowired
    private ArtifactsBusinessLogic artifactBusinessLogic;

    @Autowired
    private ComponentInstanceMergeDataBusinessLogic compInstMergeDataBL;

    @Autowired
    private ComponentInstanceChangeOperationOrchestrator onChangeInstanceOperationOrchestrator;

    @Autowired
    private ForwardingPathOperation forwardingPathOperation;


    public ComponentInstanceBusinessLogic() {
    }

    public Either<ComponentInstance, ResponseFormat> createComponentInstance(
            String containerComponentParam, String containerComponentId, String userId, ComponentInstance resourceInstance) {
        return createComponentInstance(containerComponentParam, containerComponentId, userId, resourceInstance, false, true);
    }

    public List<ComponentInstanceProperty> getComponentInstancePropertiesByInputId(org.openecomp.sdc.be.model.Component component, String inputId){
        List<ComponentInstanceProperty> resList = new ArrayList<>();
        Map<String, List<ComponentInstanceProperty>> ciPropertiesMap = component.getComponentInstancesProperties();
        if(ciPropertiesMap != null && !ciPropertiesMap.isEmpty()){
            ciPropertiesMap.forEach(new BiConsumer<String, List<ComponentInstanceProperty>>() {
                @Override
                public void accept(String s, List<ComponentInstanceProperty> ciPropList) {
                    String ciName = "";
                    Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci ->ci.getUniqueId().equals(s)).findAny();
                    if(ciOp.isPresent())
                        ciName = ciOp.get().getName();
                    if (ciPropList != null && !ciPropList.isEmpty()) {
                        for(ComponentInstanceProperty prop: ciPropList){
                            List<GetInputValueDataDefinition> inputsValues = prop.getGetInputValues();
                            if(inputsValues != null && !inputsValues.isEmpty()){
                                for(GetInputValueDataDefinition inputData: inputsValues){
                                    if(isGetInputValueForInput(inputData, inputId)){
                                        prop.setComponentInstanceId(s);
                                        prop.setComponentInstanceName(ciName);
                                        resList.add(prop);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                }
            });
        }
        return resList;
    }

    public List<ComponentInstanceInput> getComponentInstanceInputsByInputId(org.openecomp.sdc.be.model.Component component, String inputId){
        List<ComponentInstanceInput> resList = new ArrayList<>();
        Map<String, List<ComponentInstanceInput>> ciInputsMap = component.getComponentInstancesInputs();
        if(ciInputsMap != null && !ciInputsMap.isEmpty()){
            ciInputsMap.forEach(new BiConsumer<String, List<ComponentInstanceInput>>() {
                @Override
                public void accept(String s, List<ComponentInstanceInput> ciPropList) {
                    String ciName = "";
                    Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci ->ci.getUniqueId().equals(s)).findAny();
                    if(ciOp.isPresent())
                        ciName = ciOp.get().getName();
                    if (ciPropList != null && !ciPropList.isEmpty()) {
                        for(ComponentInstanceInput prop: ciPropList){
                            List<GetInputValueDataDefinition> inputsValues = prop.getGetInputValues();
                            if(inputsValues != null && !inputsValues.isEmpty()){
                                for(GetInputValueDataDefinition inputData: inputsValues){
                                    if(isGetInputValueForInput(inputData, inputId)){
                                        prop.setComponentInstanceId(s);
                                        prop.setComponentInstanceName(ciName);
                                        resList.add(prop);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                }
            });
        }
        return resList;
    }

    public Either<ComponentInstance, ResponseFormat> createComponentInstance(
            String containerComponentParam, String containerComponentId, String userId, ComponentInstance resourceInstance, boolean inTransaction, boolean needLock) {

        Component origComponent = null;
        Either<ComponentInstance, ResponseFormat> resultOp = null;
        User user = null;
        org.openecomp.sdc.be.model.Component containerComponent = null;
        ComponentTypeEnum containerComponentType;

        try {
            user = validateUserExists(userId, "create Component Instance", inTransaction);

            Either<Boolean, ResponseFormat> validateValidJson = validateJsonBody(resourceInstance, ComponentInstance.class);
            if (validateValidJson.isRight()) {
                return Either.right(validateValidJson.right().value());
            }

            Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
            if (validateComponentType.isRight()) {
                return Either.right(validateComponentType.right().value());
            } else {
                containerComponentType = validateComponentType.left().value();
            }

            Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, null);
            if (validateComponentExists.isRight()) {
                return Either.right(validateComponentExists.right().value());
            } else {
                containerComponent = validateComponentExists.left().value();
            }

            if (ModelConverter.isAtomicComponent(containerComponent)) {
                log.debug("Cannot attach resource instances to container resource of type {}", containerComponent.assetType());
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_CANNOT_CONTAIN_RESOURCE_INSTANCES, containerComponent.assetType()));
            }

            Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
            if (validateCanWorkOnComponent.isRight()) {
                return Either.right(validateCanWorkOnComponent.right().value());
            }

            if (resourceInstance != null && containerComponentType != null) {
                OriginTypeEnum originType = resourceInstance.getOriginType();
                if (originType == OriginTypeEnum.ServiceProxy) {
                    Either<Component, StorageOperationStatus> serviceProxyOrigin = toscaOperationFacade.getLatestByName("serviceProxy");
                    if (serviceProxyOrigin.isRight()) {
                        log.debug("Failed to fetch normative service proxy resource by tosca name, error {}", serviceProxyOrigin.right().value());
                        return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceProxyOrigin.right().value())));
                    }
                    origComponent = serviceProxyOrigin.left().value();

                    StorageOperationStatus fillProxyRes = fillProxyInstanceData(resourceInstance, origComponent);
                    if (fillProxyRes != StorageOperationStatus.OK) {
                        log.debug("Failed to fill service proxy resource data with data from service, error {}", fillProxyRes);
                        return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(fillProxyRes)));

                    }

                } else {
                    Either<Component, ResponseFormat> getOriginComponentRes = getAndValidateOriginComponentOfComponentInstance(containerComponentType, resourceInstance);

                    if (getOriginComponentRes.isRight()) {
                        return Either.right(getOriginComponentRes.right().value());
                    } else {
                        origComponent = getOriginComponentRes.left().value();
                    }
                }
            }
            if (needLock) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "createComponentInstance");
                if (lockComponent.isRight()) {
                    return Either.right(lockComponent.right().value());
                }
            }
            log.debug("Try to create entry on graph");
            resultOp = createComponentInstanceOnGraph(containerComponent, origComponent, resourceInstance, user);
            return resultOp;

        } finally {
            if (needLock)
                unlockComponent(resultOp, containerComponent);
        }
    }

    private StorageOperationStatus fillProxyInstanceData(ComponentInstance resourceInstance, Component proxyTemplate) {
        resourceInstance.setIsProxy(true);
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreCapabilities(false);
        filter.setIgnoreCapabiltyProperties(false);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreRequirements(false);
        Either<Component, StorageOperationStatus> serviceRes = toscaOperationFacade.getToscaElement(resourceInstance.getComponentUid(), filter);
        if (serviceRes.isRight()) {
            return serviceRes.right().value();
        }
        Component service = serviceRes.left().value();
        Map<String, List<CapabilityDefinition>> capabilities = service.getCapabilities();
        resourceInstance.setCapabilities(capabilities);
        Map<String, List<RequirementDefinition>> req = service.getRequirements();
        resourceInstance.setRequirements(req);

        String name = service.getNormalizedName() + ToscaOperationFacade.PROXY_SUFFIX;
        String toscaResourceName = ((Resource) proxyTemplate).getToscaResourceName();
        int lastIndexOf = toscaResourceName.lastIndexOf('.');
        if (lastIndexOf != -1) {
            String proxyToscaName = toscaResourceName.substring(0, lastIndexOf + 1) + name;
            resourceInstance.setToscaComponentName(proxyToscaName);
        }
        resourceInstance.setName(name);
        resourceInstance.setIsProxy(true);
        resourceInstance.setSourceModelInvariant(service.getInvariantUUID());
        resourceInstance.setSourceModelName(service.getName());
        resourceInstance.setSourceModelUuid(service.getUUID());
        resourceInstance.setSourceModelUid(service.getUniqueId());
        resourceInstance.setComponentUid(proxyTemplate.getUniqueId());
        resourceInstance.setDescription("A Proxy for Service " + service.getName());
        resourceInstance.setComponentVersion(service.getVersion());

        return StorageOperationStatus.OK;
    }

    public Either<CreateAndAssotiateInfo, ResponseFormat> createAndAssociateRIToRI(String containerComponentParam, String containerComponentId, String userId, CreateAndAssotiateInfo createAndAssotiateInfo) {

        Either<CreateAndAssotiateInfo, ResponseFormat> resultOp = null;
        ComponentInstance resourceInstance = createAndAssotiateInfo.getNode();
        RequirementCapabilityRelDef associationInfo = createAndAssotiateInfo.getAssociate();

        User user = validateUserExists(userId, "create And Associate RI To RI", false);

        Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
        if (validateComponentType.isRight()) {
            return Either.right(validateComponentType.right().value());
        }

        final ComponentTypeEnum containerComponentType = validateComponentType.left().value();

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, null);
        if (validateComponentExists.isRight()) {
            return Either.right(validateComponentExists.right().value());
        }
        org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

        if (ModelConverter.isAtomicComponent(containerComponent)) {
            log.debug("Cannot attach resource instances to container resource of type {}", containerComponent.assetType());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_CANNOT_CONTAIN_RESOURCE_INSTANCES, containerComponent.assetType()));
        }

        Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
        if (validateCanWorkOnComponent.isRight()) {
            return Either.right(validateCanWorkOnComponent.right().value());
        }

        Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "createAndAssociateRIToRI");
        if (lockComponent.isRight()) {
            return Either.right(lockComponent.right().value());
        }

        try {
            log.debug("Try to create entry on graph");
            Either<Component, ResponseFormat> eitherResourceName = getOriginComponentFromComponentInstance(resourceInstance);

            if (eitherResourceName.isRight()) {
                resultOp = Either.right(eitherResourceName.right().value());
                return resultOp;
            }
            Component origComponent = eitherResourceName.left().value();

            Either<ComponentInstance, ResponseFormat> result = createComponentInstanceOnGraph(containerComponent, origComponent, resourceInstance, user);
            if (result.isRight()) {
                log.debug("Failed to create resource instance {}", containerComponentId);
                resultOp = Either.right(result.right().value());
                return resultOp;

            }

            log.debug("Entity on graph is created.");
            ComponentInstance resResourceInfo = result.left().value();
            if (associationInfo.getFromNode() == null || associationInfo.getFromNode().isEmpty()) {
                associationInfo.setFromNode(resResourceInfo.getUniqueId());
            } else {
                associationInfo.setToNode(resResourceInfo.getUniqueId());
            }

            RequirementCapabilityRelDef requirementCapabilityRelDef = associationInfo;
            Either<RequirementCapabilityRelDef, StorageOperationStatus> resultReqCapDef = toscaOperationFacade.associateResourceInstances(containerComponentId, requirementCapabilityRelDef);
            if (resultReqCapDef.isLeft()) {
                log.debug("Enty on graph is created.");
                RequirementCapabilityRelDef resReqCapabilityRelDef = resultReqCapDef.left().value();
                CreateAndAssotiateInfo resInfo = new CreateAndAssotiateInfo(resResourceInfo, resReqCapabilityRelDef);
                resultOp = Either.left(resInfo);
                return resultOp;

            } else {
                log.info("Failed to associate node {} with node {}", associationInfo.getFromNode(), associationInfo.getToNode());
                resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(resultReqCapDef.right().value(), true), "", null));
                return resultOp;
            }

        } finally {
            unlockComponent(resultOp, containerComponent);
        }
    }

    private Either<Component, ResponseFormat> getOriginComponentFromComponentInstance(ComponentInstance componentInstance) {
        return getOriginComponentFromComponentInstance(componentInstance.getName(), componentInstance.getComponentUid());
    }

    private Either<Component, ResponseFormat> getInstanceOriginNode(ComponentInstance componentInstance) {
        return getOriginComponentFromComponentInstance(componentInstance.getName(), componentInstance.getActualComponentUid());
    }

    private Either<Component, ResponseFormat> getOriginComponentFromComponentInstance(String componentInstanceName, String origComponetId) {
        Either<Component, ResponseFormat> eitherResponse;
        Either<Component, StorageOperationStatus> eitherComponent = toscaOperationFacade.getToscaFullElement(origComponetId);
        if (eitherComponent.isRight()) {
            log.debug("Failed to get origin component with id {} for component instance {} ", origComponetId, componentInstanceName);
            eitherResponse = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponse(eitherComponent.right().value(), ComponentTypeEnum.RESOURCE), "", null));
        } else {
            eitherResponse = Either.left(eitherComponent.left().value());
        }
        return eitherResponse;
    }

    private Either<ComponentInstance, ResponseFormat> createComponentInstanceOnGraph(org.openecomp.sdc.be.model.Component containerComponent, Component originComponent, ComponentInstance componentInstance, User user) {
        Either<ComponentInstance, ResponseFormat> resultOp;

        Either<ImmutablePair<Component, String>, StorageOperationStatus> result = toscaOperationFacade.addComponentInstanceToTopologyTemplate(containerComponent, originComponent, componentInstance, false, user);

        if (result.isRight()) {
            log.debug("Failed to create entry on graph for component instance {}", componentInstance.getName());
            resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(result.right().value(), true), "", null));
            return resultOp;
        }

        log.debug("Entity on graph is created.");
        Component updatedComponent = result.left().value().getLeft();
        Map<String, String> existingEnvVersions = new HashMap<>();
        // TODO existingEnvVersions ??
        Either<ActionStatus, ResponseFormat> addComponentInstanceArtifacts = addComponentInstanceArtifacts(updatedComponent, componentInstance, originComponent, user, existingEnvVersions);
        if (addComponentInstanceArtifacts.isRight()) {
            log.debug("Failed to create component instance {}", componentInstance.getName());
            resultOp = Either.right(addComponentInstanceArtifacts.right().value());
            return resultOp;
        }

        Optional<ComponentInstance> updatedInstanceOptional = updatedComponent.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(result.left().value().getRight())).findFirst();
        if (!updatedInstanceOptional.isPresent()) {
            log.debug("Failed to fetch new added component instance {} from component {}", componentInstance.getName(), containerComponent.getName());
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstance.getName()));
            return resultOp;
        }
        resultOp = Either.left(updatedInstanceOptional.get());
        return resultOp;
    }

    /**
     * addResourceInstanceArtifacts - add artifacts (HEAT_ENV) to resource instance The instance artifacts are generated from the resource's artifacts
     * @param containerComponent
     * @param componentInstance
     * @param originComponent
     * @param user
     * @param existingEnvVersions
     * @return
     */
    protected Either<ActionStatus, ResponseFormat> addComponentInstanceArtifacts(org.openecomp.sdc.be.model.Component containerComponent, ComponentInstance componentInstance, org.openecomp.sdc.be.model.Component originComponent, User user,    Map<String, String> existingEnvVersions) {

        log.debug("add artifacts to resource instance");
        List<GroupDefinition> filteredGroups = null;
        ActionStatus status = setResourceArtifactsOnResourceInstance(componentInstance);
        if (!ActionStatus.OK.equals(status)) {
            ResponseFormat resultOp = componentsUtils.getResponseFormatForResourceInstance(status, "", null);
            return Either.right(resultOp);
        }
        StorageOperationStatus artStatus;
        // generate heat_env if necessary
        Map<String, ArtifactDefinition> componentDeploymentArtifacts = componentInstance.getDeploymentArtifacts();
        if (MapUtils.isNotEmpty(componentDeploymentArtifacts)) {

            Map<String, ArtifactDefinition> finalDeploymentArtifacts = new HashMap<String, ArtifactDefinition>();
            Map<String, List<ArtifactDefinition>> groupInstancesArtifacts = new HashMap<>();

            for (ArtifactDefinition artifact : componentDeploymentArtifacts.values()) {
                String type = artifact.getArtifactType();
                if (!type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV.getType())) {
                    finalDeploymentArtifacts.put(artifact.getArtifactLabel(), artifact);
                }
                if (!(type.equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType()))) {
                    continue;
                }
                if (artifact.checkEsIdExist()) {
                    Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactBusinessLogic.createHeatEnvPlaceHolder(artifact, ArtifactsBusinessLogic.HEAT_ENV_NAME, componentInstance.getUniqueId(), NodeTypeEnum.ResourceInstance,
                            componentInstance.getName(), user, containerComponent, existingEnvVersions);
                    if (createHeatEnvPlaceHolder.isRight()) {
                        return Either.right(createHeatEnvPlaceHolder.right().value());
                    }
                    ArtifactDefinition artifactDefinition = createHeatEnvPlaceHolder.left().value();
                    // put env
                    finalDeploymentArtifacts.put(artifactDefinition.getArtifactLabel(), artifactDefinition);

                    if (CollectionUtils.isNotEmpty(originComponent.getGroups())) {
                        filteredGroups = originComponent.getGroups().stream().filter(g -> g.getType().equals(VF_MODULE)).collect(Collectors.toList());
                    }
                    if (CollectionUtils.isNotEmpty(filteredGroups)) {
                        for (GroupDefinition groupInstance : filteredGroups) {
                            Optional<String> op = groupInstance.getArtifacts().stream().filter(p -> p.equals(artifactDefinition.getGeneratedFromId())).findAny();
                            if (op.isPresent()) {
                                List<ArtifactDefinition> artifactsUid;
                                if (groupInstancesArtifacts.containsKey(groupInstance.getUniqueId())) {
                                    artifactsUid = groupInstancesArtifacts.get(groupInstance.getUniqueId());
                                } else {
                                    artifactsUid = new ArrayList<>();
                                }
                                artifactsUid.add(artifactDefinition);
                                groupInstancesArtifacts.put(groupInstance.getUniqueId(), artifactsUid);
                                break;
                            }
                        }
                    }
                }
            }
            artStatus = toscaOperationFacade.addDeploymentArtifactsToInstance(containerComponent.getUniqueId(), componentInstance, finalDeploymentArtifacts);
            if (artStatus != StorageOperationStatus.OK) {
                log.debug("Failed to add instance deployment artifacts for instance {} in conatiner {} error {}", componentInstance.getUniqueId(), containerComponent.getUniqueId(), artStatus);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForResourceInstance(artStatus, false)));

            }
            StorageOperationStatus result = toscaOperationFacade.addGroupInstancesToComponentInstance(containerComponent, componentInstance, filteredGroups, groupInstancesArtifacts);
            if (result != StorageOperationStatus.OK) {
                log.debug("failed to update group instance for component instance {}", componentInstance.getUniqueId());
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(result)));
            }
            componentInstance.setDeploymentArtifacts(finalDeploymentArtifacts);
        }

        artStatus = toscaOperationFacade.addInformationalArtifactsToInstance(containerComponent.getUniqueId(), componentInstance, originComponent.getArtifacts());
        if (artStatus != StorageOperationStatus.OK) {
            log.debug("Failed to add informational artifacts to the instance {} belonging to the conatiner {}. Status is {}", componentInstance.getUniqueId(), containerComponent.getUniqueId(), artStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForResourceInstance(artStatus, false)));

        }
        componentInstance.setArtifacts(originComponent.getArtifacts());
        return Either.left(ActionStatus.OK);
    }

    private ActionStatus setResourceArtifactsOnResourceInstance(ComponentInstance resourceInstance) {
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getResourceDeploymentArtifacts = artifactBusinessLogic.getArtifacts(resourceInstance.getComponentUid(), NodeTypeEnum.Resource, ArtifactGroupTypeEnum.DEPLOYMENT, null);

        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<String, ArtifactDefinition>();
        if (getResourceDeploymentArtifacts.isRight()) {
            StorageOperationStatus status = getResourceDeploymentArtifacts.right().value();
            if (!status.equals(StorageOperationStatus.NOT_FOUND)) {
                log.debug("Failed to fetch resource: {} artifacts. status is {}", resourceInstance.getComponentUid(), status);
                return componentsUtils.convertFromStorageResponseForResourceInstance(status, true);
            }
        } else {
            deploymentArtifacts = getResourceDeploymentArtifacts.left().value();
        }

        if (!deploymentArtifacts.isEmpty()) {
            Map<String, ArtifactDefinition> tempDeploymentArtifacts = new HashMap<String, ArtifactDefinition>(deploymentArtifacts);
            for (Entry<String, ArtifactDefinition> artifact : deploymentArtifacts.entrySet()) {
                if (!artifact.getValue().checkEsIdExist()) {
                    tempDeploymentArtifacts.remove(artifact.getKey());
                }
            }

            resourceInstance.setDeploymentArtifacts(tempDeploymentArtifacts);
        }

        return ActionStatus.OK;
    }

    public Either<ComponentInstance, ResponseFormat> updateComponentInstanceMetadata(String containerComponentParam, String containerComponentId, String componentInstanceId, String userId, ComponentInstance componentInstance) {
        return updateComponentInstanceMetadata(containerComponentParam, containerComponentId, componentInstanceId, userId, componentInstance, false, true, true);
    }

    public Either<ComponentInstance, ResponseFormat> updateComponentInstanceMetadata(String containerComponentParam, String containerComponentId, String componentInstanceId, String userId, ComponentInstance componentInstance, boolean inTransaction,
                                                                                     boolean needLock, boolean createNewTransaction) {

        validateUserExists(userId, "update Component Instance", inTransaction);

        Either<ComponentInstance, ResponseFormat> resultOp = null;

        Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
        if (validateComponentType.isRight()) {
            return Either.right(validateComponentType.right().value());
        }

        final ComponentTypeEnum containerComponentType = validateComponentType.left().value();

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, null);
        if (validateComponentExists.isRight()) {
            return Either.right(validateComponentExists.right().value());
        }
        org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

        Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
        if (validateCanWorkOnComponent.isRight()) {
            return Either.right(validateCanWorkOnComponent.right().value());
        }
        ComponentTypeEnum instanceType = getComponentType(containerComponentType);
        Either<Boolean, StorageOperationStatus> validateParentStatus = toscaOperationFacade.validateComponentExists(componentInstance.getComponentUid());
        if (validateParentStatus.isRight()) {
            log.debug("Failed to get component instance {} on service {}", componentInstanceId, containerComponentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND, componentInstance.getName(), instanceType.getValue().toLowerCase()));
            return resultOp;
        }
        if (!validateParentStatus.left().value()) {
            resultOp = Either.right(
                    componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstance.getName(), instanceType.getValue().toLowerCase(), containerComponentType.getValue().toLowerCase(), containerComponentId));
            return resultOp;
        }

        if (needLock) {
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "updateComponentInstance");
            if (lockComponent.isRight()) {
                return Either.right(lockComponent.right().value());
            }
        }
        try {

            Either<Component, ResponseFormat> eitherResourceName = getOriginComponentFromComponentInstance(componentInstance);

            if (eitherResourceName.isRight()) {
                resultOp = Either.right(eitherResourceName.right().value());
                return resultOp;
            }
            Component origComponent = eitherResourceName.left().value();

            resultOp = updateComponentInstanceMetadata(containerComponent, containerComponentType, origComponent, componentInstanceId, componentInstance);
            return resultOp;

        } finally {
            if (needLock)
                unlockComponent(resultOp, containerComponent);
        }
    }

    // New Multiple Instance Update API
    public Either<List<ComponentInstance>, ResponseFormat> updateComponentInstance(String containerComponentParam, String containerComponentId, String userId, List<ComponentInstance> componentInstanceList, boolean needLock) {

        Either<List<ComponentInstance>, ResponseFormat> resultOp = null;
        org.openecomp.sdc.be.model.Component containerComponent = null;
        try {
            validateUserExists(userId, "update Component Instance", true);

            Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
            if (validateComponentType.isRight()) {
                return Either.right(validateComponentType.right().value());
            }

            final ComponentTypeEnum containerComponentType = validateComponentType.left().value();

            ComponentParametersView componentFilter = new ComponentParametersView();
            componentFilter.disableAll();
            componentFilter.setIgnoreUsers(false);
            componentFilter.setIgnoreComponentInstances(false);
            Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExistsByFilter(containerComponentId, containerComponentType, componentFilter);
            if (validateComponentExists.isRight()) {
                return Either.right(validateComponentExists.right().value());
            }

            containerComponent = validateComponentExists.left().value();

            Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
            if (validateCanWorkOnComponent.isRight()) {
                return Either.right(validateCanWorkOnComponent.right().value());
            }

            ComponentTypeEnum instanceType = getComponentType(containerComponentType);

            for (ComponentInstance componentInstance : componentInstanceList) {
                boolean validateParent = validateParent(containerComponent, componentInstance.getUniqueId());
                if (!validateParent) {
                    resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstance.getName(), instanceType.getValue().toLowerCase(), containerComponentType.getValue().toLowerCase(),
                            containerComponentId));
                    return resultOp;
                }
            }

            if (needLock) {

                Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "updateComponentInstance");
                if (lockComponent.isRight()) {
                    return Either.right(lockComponent.right().value());
                }
            }

            List<ComponentInstance> updatedList = new ArrayList<>();
            List<ComponentInstance> instancesFromContainerComponent = containerComponent.getComponentInstances();
            List<ComponentInstance> listForUpdate = new ArrayList<>();
            if (instancesFromContainerComponent == null || instancesFromContainerComponent.isEmpty())
                containerComponent.setComponentInstances(componentInstanceList);
            else {
                Iterator<ComponentInstance> iterator = instancesFromContainerComponent.iterator();
                while (iterator.hasNext()) {
                    ComponentInstance origInst = iterator.next();
                    Optional<ComponentInstance> op = componentInstanceList.stream().filter(ci -> ci.getUniqueId().equals(origInst.getUniqueId())).findAny();
                    if (op.isPresent()) {
                        ComponentInstance updatedCi = op.get();
                        updatedCi = buildComponentInstance(updatedCi, origInst);

                        Boolean isUniqueName = validateInstanceNameUniquenessUponUpdate(containerComponent, origInst, updatedCi.getName());
                        if (!isUniqueName) {
                            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the name of the component instance {} to {}. A component instance with the same name already exists. ", origInst.getName(), updatedCi.getName());
                            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, containerComponentType.getValue(), origInst.getName()));
                            return resultOp;
                        }

                        listForUpdate.add(updatedCi);
                    } else
                        listForUpdate.add(origInst);
                }
                containerComponent.setComponentInstances(listForUpdate);

                if (resultOp == null) {
                    Either<Component, StorageOperationStatus> updateStatus = toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(containerComponent, componentFilter);
                    if (updateStatus.isRight()) {
                        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update metadata belonging to container component {}. Status is {}. ", containerComponent.getName(), updateStatus.right().value());
                        resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(updateStatus.right().value(), true), "", null));
                        return resultOp;
                    }
                    for (ComponentInstance updatedInstance : updateStatus.left().value().getComponentInstances()) {
                        Optional<ComponentInstance> op = componentInstanceList.stream().filter(ci -> ci.getName().equals(updatedInstance.getName())).findAny();
                        if (op.isPresent()) {
                            updatedList.add(updatedInstance);
                        }
                    }
                }
            }

            resultOp = Either.left(updatedList);
            return resultOp;

        } finally {
            if (needLock) {
                unlockComponent(resultOp, containerComponent);
            }
        }
    }

    private boolean validateParent(org.openecomp.sdc.be.model.Component containerComponent, String nodeTemplateId) {
        return containerComponent.getComponentInstances().stream().anyMatch(p -> p.getUniqueId().equals(nodeTemplateId));
    }

    private ComponentTypeEnum getComponentType(ComponentTypeEnum containerComponentType) {
        if (ComponentTypeEnum.PRODUCT.equals(containerComponentType)) {
            return ComponentTypeEnum.SERVICE_INSTANCE;
        } else {
            return ComponentTypeEnum.RESOURCE_INSTANCE;
        }
    }

    private Either<ComponentInstance, ResponseFormat> updateComponentInstanceMetadata(Component containerComponent, ComponentTypeEnum containerComponentType, org.openecomp.sdc.be.model.Component origComponent, String componentInstanceId,
                                                                                      ComponentInstance componentInstance) {

        Either<ComponentInstance, ResponseFormat> resultOp = null;
        Optional<ComponentInstance> componentInstanceOptional = null;
        Either<ImmutablePair<Component, String>, StorageOperationStatus> updateRes = null;
        ComponentInstance oldComponentInstance = null;
        boolean isNameChanged = false;

        if (resultOp == null) {
            componentInstanceOptional = containerComponent.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(componentInstance.getUniqueId())).findFirst();
            if (!componentInstanceOptional.isPresent()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find the component instance {} in container component {}. ", componentInstance.getName(), containerComponent.getName());
                resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstance.getName()));
            }
        }
        if (resultOp == null) {
            oldComponentInstance = componentInstanceOptional.get();
            String newInstanceName = componentInstance.getName();
            if (oldComponentInstance != null && oldComponentInstance.getName() != null && !oldComponentInstance.getName().equals(newInstanceName))
                isNameChanged = true;
            Boolean isUniqueName = validateInstanceNameUniquenessUponUpdate(containerComponent, oldComponentInstance, newInstanceName);
            if (!isUniqueName) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the name of the component instance {} to {}. A component instance with the same name already exists. ", oldComponentInstance.getName(), newInstanceName);
                resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, containerComponentType.getValue(), componentInstance.getName()));
            }
        }
        if (resultOp == null) {
            updateRes = toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(containerComponent, origComponent, updateComponentInstanceMetadata(oldComponentInstance, componentInstance));
            if (updateRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update metadata of component instance {} belonging to container component {}. Status is {}. ", componentInstance.getName(), containerComponent.getName(),
                        updateRes.right().value());
                resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(updateRes.right().value(), true), "", null));
            } else {
                // region - Update instance Groups
                if (isNameChanged) {
                    Either result = toscaOperationFacade.cleanAndAddGroupInstancesToComponentInstance(containerComponent, oldComponentInstance, componentInstanceId);
                    if (result.isRight())
                        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to rename group instances for container {}. error {} ", componentInstanceId, result.right().value());
                }
                // endregion
            }
        }
        if (resultOp == null) {
            String newInstanceId = updateRes.left().value().getRight();
            Optional<ComponentInstance> updatedInstanceOptional = updateRes.left().value().getLeft().getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(newInstanceId)).findFirst();

            if (!updatedInstanceOptional.isPresent()) {
                log.debug("Failed to update metadata of component instance {} of container component {}", componentInstance.getName(), containerComponent.getName());
                resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstance.getName()));
            } else {
                resultOp = Either.left(updatedInstanceOptional.get());
            }

        }
        if (resultOp == null) {
            resultOp = Either.left(componentInstanceOptional.get());
        }
        return resultOp;
    }

    /**
     * @param oldPrefix-
     *            The normalized old vf name
     * @param newNormailzedPrefix-
     *            The normalized new vf name
     * @param qualifiedGroupInstanceName-
     *            old Group Instance Name
     **/
    // modify group names
    private String getNewGroupName(String oldPrefix, String newNormailzedPrefix, String qualifiedGroupInstanceName) {
        if (qualifiedGroupInstanceName == null) {
            log.info("CANNOT change group name ");
            return null;
        }
        if (qualifiedGroupInstanceName.startsWith(oldPrefix) || qualifiedGroupInstanceName.startsWith(ValidationUtils.normalizeComponentInstanceName(oldPrefix)))
            return qualifiedGroupInstanceName.replaceFirst(oldPrefix, newNormailzedPrefix);
        return qualifiedGroupInstanceName;
    }

    private ComponentInstance updateComponentInstanceMetadata(ComponentInstance oldComponentInstance, ComponentInstance newComponentInstance) {
        oldComponentInstance.setName(newComponentInstance.getName());
        oldComponentInstance.setModificationTime(System.currentTimeMillis());
        oldComponentInstance.setCustomizationUUID(UUID.randomUUID().toString());
        if (oldComponentInstance.getGroupInstances() != null)
            oldComponentInstance.getGroupInstances().forEach(group -> group.setName(getNewGroupName(oldComponentInstance.getNormalizedName(), ValidationUtils.normalizeComponentInstanceName(newComponentInstance.getName()), group.getName())));
        return oldComponentInstance;
    }

    public Either<ComponentInstance, ResponseFormat> deleteComponentInstance(String containerComponentParam, String containerComponentId, String componentInstanceId, String userId) {

        validateUserExists(userId, "delete Component Instance", false);

        Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
        if (validateComponentType.isRight()) {
            return Either.right(validateComponentType.right().value());
        }

        final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, null);
        if (validateComponentExists.isRight()) {
            return Either.right(validateComponentExists.right().value());
        }
        org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();
        Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
        if (validateCanWorkOnComponent.isRight()) {
            return Either.right(validateCanWorkOnComponent.right().value());
        }

        Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "deleteComponentInstance");
        if (lockComponent.isRight()) {
            return Either.right(lockComponent.right().value());
        }

        Either<ComponentInstance, ResponseFormat> resultOp = null;
        try {
            resultOp = deleteComponentInstance(containerComponent, componentInstanceId, containerComponentType);
            if (resultOp.isRight()){
                return resultOp;
            }
            Either<ComponentInstance, ResponseFormat> deleteEither = deleteForwardingPathsRelatedTobeDeletedComponentInstance(containerComponentId,
                    containerComponentType, resultOp);
            if (deleteEither.isRight()){
                return deleteEither;
            }
            return deleteEither;

        } finally {
            unlockComponent(resultOp, containerComponent);
        }
    }

    public Either<ComponentInstance, ResponseFormat> deleteForwardingPathsRelatedTobeDeletedComponentInstance(String containerComponentId, ComponentTypeEnum containerComponentType,
                                                                                                              Either<ComponentInstance, ResponseFormat> resultOp) {
        if(containerComponentType.equals(ComponentTypeEnum.SERVICE) && resultOp.isLeft() ){
            final ComponentInstance componentInstance = resultOp.left().value();
            List<String> pathIDsToBeDeleted = getForwardingPathsRelatedToComponentInstance(containerComponentId, componentInstance.getName());
            if (!pathIDsToBeDeleted.isEmpty()) {
                Either<Set<String>, ResponseFormat> deleteForwardingPathsEither = deleteForwardingPaths(containerComponentId,
                        pathIDsToBeDeleted);
                if(deleteForwardingPathsEither.isRight()) {
                    resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
                }

            }
        }
        return resultOp;
    }


    private Either<Set<String>, ResponseFormat> deleteForwardingPaths(String serviceId,  List<String> pathIdsToDelete){

        Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(serviceId);
        if(storageStatus.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(storageStatus.right().value()));
        }
        Either<Set<String>, StorageOperationStatus> result = forwardingPathOperation.deleteForwardingPath(storageStatus.left().value(),
                Sets.newHashSet(pathIdsToDelete));

        if(result.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(result.right().value()));
        }
        return  Either.left(result.left().value());
    }

    private List<String> getForwardingPathsRelatedToComponentInstance(String containerComponentId, String componentInstanceId){
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreForwardingPath(false);
        Either<Service, StorageOperationStatus> forwardingPathOrigin = toscaOperationFacade
                .getToscaElement(containerComponentId, filter);
        Collection<ForwardingPathDataDefinition> allPaths = forwardingPathOrigin.left().value().getForwardingPaths().values();
        List<String> pathIDsToBeDeleted = new ArrayList<>();

        allPaths.stream().filter(path -> isPathRelatedToComponent(path,componentInstanceId ))
                .forEach(path -> pathIDsToBeDeleted.add(path.getUniqueId()));

        return pathIDsToBeDeleted;
    }

    private boolean isPathRelatedToComponent(ForwardingPathDataDefinition pathDataDefinition,
                                             String componentInstanceId){
        return pathDataDefinition.getPathElements().getListToscaDataDefinition()
                .stream().anyMatch(elementDataDefinition -> elementDataDefinition.getFromNode().equalsIgnoreCase(componentInstanceId) ||
                        elementDataDefinition.getToNode()
                                .equalsIgnoreCase(componentInstanceId));
    }


    private Either<ComponentInstance, ResponseFormat> deleteComponentInstance(Component containerComponent, String componentInstanceId, ComponentTypeEnum containerComponentType) {

        Either<ComponentInstance, ResponseFormat> resultOp = null;
        ComponentInstance deletedInstance = null;
        Either<ImmutablePair<Component, String>, StorageOperationStatus> deleteRes = toscaOperationFacade.deleteComponentInstanceFromTopologyTemplate(containerComponent, componentInstanceId);

        if (deleteRes.isRight()) {
            log.debug("Failed to delete entry on graph for resourceInstance {}", componentInstanceId);
            ActionStatus status = componentsUtils.convertFromStorageResponse(deleteRes.right().value(), containerComponentType);
            resultOp = Either.right(componentsUtils.getResponseFormat(status, componentInstanceId));
        }
        if (resultOp == null) {
            log.debug("The component instance {} has been removed from container component {}. ", componentInstanceId, containerComponent);
            deletedInstance = findAndRemoveComponentInstanceFromContainerComponent(componentInstanceId, containerComponent);
            resultOp = Either.left(deletedInstance);
        }
        if (resultOp.isLeft() && CollectionUtils.isNotEmpty(containerComponent.getGroups())) {
            List<GroupDataDefinition> groupsToUpdate = new ArrayList<>();
            for (GroupDataDefinition currGroup : containerComponent.getGroups()) {
                Map<String, String> members = currGroup.getMembers();
                if (members != null && members.containsKey(deletedInstance.getName())) {
                    members.remove(deletedInstance.getName());
                    groupsToUpdate.add(currGroup);
                }
            }
            Either<List<GroupDefinition>, StorageOperationStatus> updateGroupsRes = toscaOperationFacade.updateGroupsOnComponent(containerComponent, groupsToUpdate);
            if (updateGroupsRes.isRight()) {
                log.debug("Failed to delete component instance {} from group members. ", componentInstanceId);
                ActionStatus status = componentsUtils.convertFromStorageResponse(updateGroupsRes.right().value(), containerComponentType);
                resultOp = Either.right(componentsUtils.getResponseFormat(status, componentInstanceId));
            }
        }
        if (resultOp.isLeft() && CollectionUtils.isNotEmpty(containerComponent.getInputs())) {
            List<InputDefinition> inputsToDelete = containerComponent.getInputs().stream().filter(i -> i.getInstanceUniqueId() != null && i.getInstanceUniqueId().equals(componentInstanceId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(inputsToDelete)) {
                StorageOperationStatus deleteInputsRes = toscaOperationFacade.deleteComponentInstanceInputsFromTopologyTemplate(containerComponent, inputsToDelete);
                if (deleteInputsRes != StorageOperationStatus.OK) {
                    log.debug("Failed to delete inputs of the component instance {} from container component. ", componentInstanceId);
                    resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteInputsRes, containerComponentType), componentInstanceId));
                }
            }
        }
        return resultOp;
    }

    private ComponentInstance findAndRemoveComponentInstanceFromContainerComponent(String componentInstanceId, Component containerComponent) {
        ComponentInstance foundInstance = null;
        for (ComponentInstance instance : containerComponent.getComponentInstances()) {
            if (instance.getUniqueId().equals(componentInstanceId)) {
                foundInstance = instance;
                containerComponent.getComponentInstances().remove(instance);
                break;
            }
        }
        findAndRemoveComponentInstanceRelations(componentInstanceId, containerComponent);
        return foundInstance;
    }

    private void findAndRemoveComponentInstanceRelations(String componentInstanceId, Component containerComponent) {
        if(CollectionUtils.isNotEmpty(containerComponent.getComponentInstancesRelations())){
            containerComponent.setComponentInstancesRelations(containerComponent.getComponentInstancesRelations().stream().filter(r -> isNotBelongingRelation(componentInstanceId, r)).collect(Collectors.toList()));
        }
    }

    private boolean isNotBelongingRelation(String componentInstanceId, RequirementCapabilityRelDef relation) {
        return !relation.getToNode().equals(componentInstanceId) && !relation.getFromNode().equals(componentInstanceId);
    }

    public Either<RequirementCapabilityRelDef, ResponseFormat> associateRIToRI(String componentId, String userId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum) {
        return associateRIToRI(componentId, userId, requirementDef, componentTypeEnum, false, true, true);
    }

    public Either<RequirementCapabilityRelDef, ResponseFormat> associateRIToRI(String componentId, String userId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum, boolean inTransaction, boolean needLock,
                                                                               boolean createNewTransaction) {

        validateUserExists(userId, "associate Ri To RI", inTransaction);

        Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(componentId, componentTypeEnum, null);
        if (validateComponentExists.isRight()) {
            return Either.right(validateComponentExists.right().value());
        }
        org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

        Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
        if (validateCanWorkOnComponent.isRight()) {
            return Either.right(validateCanWorkOnComponent.right().value());
        }
        if (needLock) {
            Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "associateRIToRI");

            if (lockComponent.isRight()) {
                return Either.right(lockComponent.right().value());
            }
        }

        try {

            resultOp = associateRIToRIOnGraph(validateComponentExists.left().value(), requirementDef, componentTypeEnum, inTransaction);

            return resultOp;

        } finally {
            if (needLock)
                unlockComponent(resultOp, containerComponent);
        }
    }

    public Either<RequirementCapabilityRelDef, ResponseFormat> associateRIToRIOnGraph(Component containerComponent, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum, boolean inTransaction) {

        log.debug("Try to create entry on graph");
        Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;

        Either<RequirementCapabilityRelDef, StorageOperationStatus> result = toscaOperationFacade.associateResourceInstances(containerComponent.getUniqueId(), requirementDef);

        if (result.isLeft()) {
            log.debug("Enty on graph is created.");
            RequirementCapabilityRelDef requirementCapabilityRelDef = result.left().value();
            resultOp = Either.left(requirementCapabilityRelDef);
            return resultOp;

        } else {
            log.debug("Failed to associate node: {} with node {}", requirementDef.getFromNode(), requirementDef.getToNode());
            String fromNameOrId = "";
            String toNameOrId = "";
            Either<ComponentInstance, StorageOperationStatus> fromResult = getResourceInstanceById(containerComponent, requirementDef.getFromNode());
            Either<ComponentInstance, StorageOperationStatus> toResult = getResourceInstanceById(containerComponent, requirementDef.getToNode());

            toNameOrId = requirementDef.getFromNode();
            fromNameOrId = requirementDef.getFromNode();
            if (fromResult.isLeft()) {
                fromNameOrId = fromResult.left().value().getName();
            }
            if (toResult.isLeft()) {
                toNameOrId = toResult.left().value().getName();
            }

            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForResourceInstance(result.right().value(), true), fromNameOrId, toNameOrId, requirementDef.getRelationships().get(0).getRelation().getRequirement()));

            return resultOp;
        }

    }

    public Either<RequirementCapabilityRelDef, ResponseFormat> dissociateRIFromRI(String componentId, String userId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum) {
        validateUserExists(userId, "dissociate RI From RI", false);

        Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> 