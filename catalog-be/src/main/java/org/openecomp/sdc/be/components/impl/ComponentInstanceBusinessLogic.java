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
import org.openecomp.sdc.be.components.impl.utils.DirectivesUtils;
import org.openecomp.sdc.be.components.merge.instance.ComponentInstanceMergeDataBusinessLogic;
import org.openecomp.sdc.be.components.merge.instance.DataForMergeHolder;
import org.openecomp.sdc.be.components.utils.PropertiesUtils;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datamodel.utils.PropertyValueConstraintValidationUtil;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.impl.ServiceFilterUtils;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition.PropertyNames;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
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

import java.util.ArrayList;
import java.util.Arrays;
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
import static org.openecomp.sdc.be.components.utils.PropertiesUtils.getPropertyCapabilityOfChildInstance;

@org.springframework.stereotype.Component
public class ComponentInstanceBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(ComponentInstanceBusinessLogic.class.getName());
    private static final String VF_MODULE = "org.openecomp.groups.VfModule";
    public static final String TRY_TO_CREATE_ENTRY_ON_GRAPH = "Try to create entry on graph";
    private static final String CLOUD_SPECIFIC_FIXED_KEY_WORD = "cloudtech";
    private static final String[][] CLOUD_SPECIFIC_KEY_WORDS = {{"k8s", "azure", "aws"}, /* cloud specific technology */
                                                                {"charts", "day0", "configtemplate"} /*cloud specific sub type*/};
    public static final String FAILED_TO_CREATE_ENTRY_ON_GRAPH_FOR_COMPONENT_INSTANCE = "Failed to create entry on graph for component instance {}";
    public static final String ENTITY_ON_GRAPH_IS_CREATED = "Entity on graph is created.";
    public static final String INVALID_COMPONENT_TYPE = "invalid component type";
    public static final String FAILED_TO_RETRIEVE_COMPONENT_COMPONENT_ID = "Failed to retrieve component, component id {}";
    public static final String FAILED_TO_LOCK_SERVICE = "Failed to lock service {}";
    public static final String CREATE_OR_UPDATE_PROPERTY_VALUE = "CreateOrUpdatePropertyValue";
    public static final String FAILED_TO_COPY_COMP_INSTANCE_TO_CANVAS = "Failed to copy the component instance to the canvas";
    public static final String COPY_COMPONENT_INSTANCE_OK = "Copy component instance OK";

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

    @Autowired
    private NodeFilterOperation serviceFilterOperation;

    public ComponentInstanceBusinessLogic() {
    }

    public Either<ComponentInstance, ResponseFormat> createComponentInstance(String containerComponentParam,
                                                                             String containerComponentId, String userId, ComponentInstance resourceInstance) {
        return createComponentInstance(containerComponentParam, containerComponentId, userId, resourceInstance, false,
                true);
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

    public Optional<ComponentInstanceProperty> getComponentInstancePropertyByPolicyId(Component component,
                                                                                      PolicyDefinition policy) {

        Optional<ComponentInstanceProperty> propertyCandidate = getComponentInstancePropertyByPolicy(component, policy);

        if(propertyCandidate.isPresent()) {
            ComponentInstanceProperty componentInstanceProperty = propertyCandidate.get();
            Optional<GetPolicyValueDataDefinition> getPolicyCandidate =
                    getGetPolicyValueDataDefinition(policy, componentInstanceProperty);

            getPolicyCandidate.ifPresent(getPolicyValue ->
                updateComponentInstancePropertyAfterUndeclaration(componentInstanceProperty, getPolicyValue, policy));
            return Optional.of(componentInstanceProperty);
        }

        return Optional.empty();

    }

    private void updateComponentInstancePropertyAfterUndeclaration(ComponentInstanceProperty componentInstanceProperty,
            GetPolicyValueDataDefinition getPolicyValue, PolicyDefinition policyDefinition) {
        componentInstanceProperty.setValue(getPolicyValue.getOrigPropertyValue());
        List<GetPolicyValueDataDefinition> getPolicyValues = componentInstanceProperty.getGetPolicyValues();
        if(CollectionUtils.isNotEmpty(getPolicyValues)) {
            getPolicyValues.remove(getPolicyValue);
            componentInstanceProperty.setGetPolicyValues(getPolicyValues);
            policyDefinition.setGetPolicyValues(getPolicyValues);
        }
    }

    private Optional<GetPolicyValueDataDefinition> getGetPolicyValueDataDefinition(PolicyDefinition policy,
            ComponentInstanceProperty componentInstanceProperty) {
        List<GetPolicyValueDataDefinition> getPolicyValues = policy.getGetPolicyValues();
        return getPolicyValues.stream()
                                                                            .filter(getPolicyValue -> getPolicyValue
                                                                                                              .getPropertyName()
                                                                                                              .equals(componentInstanceProperty
                                                                                                                              .getName()))
                                                                            .findAny();
    }

    private Optional<ComponentInstanceProperty> getComponentInstancePropertyByPolicy(Component component,
            PolicyDefinition policy) {
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties =
                component.getComponentInstancesProperties();

        if(MapUtils.isEmpty(componentInstancesProperties)) {
            return Optional.empty();
        }

        String instanceUniqueId = policy.getInstanceUniqueId();

        List<ComponentInstanceProperty> componentInstanceProperties =
                componentInstancesProperties.containsKey(instanceUniqueId)
                        ? componentInstancesProperties.get(instanceUniqueId)
                        : new ArrayList<>();

        return componentInstanceProperties
                       .stream().filter(property -> property.getName().equals(policy.getName())).findAny();
    }

    public List<ComponentInstanceInput> getComponentInstanceInputsByInputId(
            org.openecomp.sdc.be.model.Component component, String inputId) {
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
            log.debug(TRY_TO_CREATE_ENTRY_ON_GRAPH);
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
        filter.setIgnoreInterfaces(false);
        filter.setIgnoreProperties(false);
        filter.setIgnoreInputs(false);
        Either<Component, StorageOperationStatus> serviceRes =
                toscaOperationFacade.getToscaElement(resourceInstance.getComponentUid(), filter);
        if (serviceRes.isRight()) {
            return serviceRes.right().value();
        }
        Component service = serviceRes.left().value();
        Map<String, List<CapabilityDefinition>> capabilities = service.getCapabilities();
        resourceInstance.setCapabilities(capabilities);
        Map<String, List<RequirementDefinition>> req = service.getRequirements();
        resourceInstance.setRequirements(req);

        Map<String, InterfaceDefinition> serviceInterfaces = service.getInterfaces();
        if(MapUtils.isNotEmpty(serviceInterfaces)) {
            serviceInterfaces.forEach(resourceInstance::addInterface);
        }


        resourceInstance.setProperties(PropertiesUtils.getProperties(service));

        List<InputDefinition> serviceInputs = service.getInputs();
        resourceInstance.setInputs(serviceInputs);

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
            log.debug(TRY_TO_CREATE_ENTRY_ON_GRAPH);
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

            log.debug(ENTITY_ON_GRAPH_IS_CREATED);
            ComponentInstance resResourceInfo = result.left().value();
            if (associationInfo.getFromNode() == null || associationInfo.getFromNode().isEmpty()) {
                associationInfo.setFromNode(resResourceInfo.getUniqueId());
            } else {
                associationInfo.setToNode(resResourceInfo.getUniqueId());
            }

            RequirementCapabilityRelDef requirementCapabilityRelDef = associationInfo;
            Either<RequirementCapabilityRelDef, StorageOperationStatus> resultReqCapDef = toscaOperationFacade.associateResourceInstances(containerComponentId, requirementCapabilityRelDef);
            if (resultReqCapDef.isLeft()) {
                log.debug(ENTITY_ON_GRAPH_IS_CREATED);
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
            log.debug(FAILED_TO_CREATE_ENTRY_ON_GRAPH_FOR_COMPONENT_INSTANCE, componentInstance.getName());
            resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstance(componentsUtils.convertFromStorageResponseForResourceInstance(result.right().value(), true), "", null));
            return resultOp;
        }

        log.debug(ENTITY_ON_GRAPH_IS_CREATED);
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

  public boolean isCloudSpecificArtifact(String artifact) {
      if (artifact.contains(CLOUD_SPECIFIC_FIXED_KEY_WORD)) {
          for (int i = 0; i < CLOUD_SPECIFIC_KEY_WORDS.length; i++) {
              if (Arrays.stream(CLOUD_SPECIFIC_KEY_WORDS[i]).noneMatch(str -> artifact.contains(str))) {
                  return false;
              }
          }
          return true;
      } else {
          return false;
      }
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

            Map<String, ArtifactDefinition> finalDeploymentArtifacts = new HashMap<>();
            Map<String, List<ArtifactDefinition>> groupInstancesArtifacts = new HashMap<>();

            for (ArtifactDefinition artifact : componentDeploymentArtifacts.values()) {
                String type = artifact.getArtifactType();
                if (!type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV.getType())) {
                    finalDeploymentArtifacts.put(artifact.getArtifactLabel(), artifact);
                }
                if (!(type.equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType()) || type.equalsIgnoreCase(ArtifactTypeEnum.CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT.getType()))) {
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

                            if (isCloudSpecificArtifact(artifactDefinition.getArtifactName())) {
                                groupInstance.getArtifacts().add(artifactDefinition.getGeneratedFromId());
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

        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
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
            Map<String, ArtifactDefinition> tempDeploymentArtifacts = new HashMap<>(deploymentArtifacts);
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
            if(!DirectivesUtils.isValid(componentInstance.getDirectives())) {
                final String directivesStr =
                        componentInstance.getDirectives().stream().collect(Collectors.joining(" , ", " [ ", " ] "));
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG,
                        "Failed to update the directives of the component instance {} to {}. Directives data {} is invalid. ",
                        oldComponentInstance.getName(), newInstanceName ,
                        directivesStr);
                resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.DIRECTIVES_INVALID_VALUE,
                        directivesStr));
            }
        }
        String newInstanceName = componentInstance.getName();
        String oldInstanceName = null;
        if (resultOp == null) {
            oldComponentInstance = componentInstanceOptional.get();
            newInstanceName = componentInstance.getName();
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
                    if (containerComponent instanceof Service) {
                        Either<ComponentInstance, ResponseFormat> renameEither =
                                renameServiceFilter((Service) containerComponent, newInstanceName,
                                        oldInstanceName);
                        if (renameEither.isRight()) {
                            return renameEither;
                        }
                    }
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


    public Either<ComponentInstance, ResponseFormat> renameServiceFilter(Service containerComponent,
            String newInstanceName, String oldInstanceName) {

        Map<String, CINodeFilterDataDefinition> renamedNodesFilter =
                ServiceFilterUtils.getRenamedNodesFilter((Service) containerComponent,
                        oldInstanceName, newInstanceName);
        for( Entry<String, CINodeFilterDataDefinition> entry :  renamedNodesFilter.entrySet()){
            Either<CINodeFilterDataDefinition, StorageOperationStatus>
                    renameEither = serviceFilterOperation.updateNodeFilter(
                    containerComponent.getUniqueId(),entry.getKey(),entry.getValue());
            if (renameEither.isRight()){
                return  Either.right(componentsUtils.getResponseFormatForResourceInstance(
                        componentsUtils.convertFromStorageResponse(renameEither.right().value(), ComponentTypeEnum.SERVICE),
                        containerComponent.getName(), null));
            }

        }
        return Either.left(null);
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
        oldComponentInstance.setDirectives(newComponentInstance.getDirectives());
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
            if (containerComponent instanceof Service) {
                ComponentInstance componentInstance = containerComponent.getComponentInstanceById(componentInstanceId).get();
                Either<String, StorageOperationStatus> deleteServiceFilterEither =
                        serviceFilterOperation.deleteNodeFilter((Service) containerComponent, componentInstanceId);
                if (deleteServiceFilterEither.isRight()) {
                    ActionStatus status = componentsUtils.convertFromStorageResponse(deleteServiceFilterEither.right().value(),
                            containerComponentType);
                    titanDao.rollback();
                    return Either.right(componentsUtils.getResponseFormat(status, componentInstanceId));
                }
                resultOp = deleteServiceFiltersRelatedTobeDeletedComponentInstance((Service) containerComponent,
                        componentInstance, ComponentTypeEnum.SERVICE, userId);
                if (resultOp.isRight()) {
                    titanDao.rollback();
                    return resultOp;
                }
            }
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

    public Either<ComponentInstance, ResponseFormat> deleteServiceFiltersRelatedTobeDeletedComponentInstance(
            Service service, ComponentInstance componentInstance, ComponentTypeEnum containerComponentType, String userId) {
        if (containerComponentType.equals(ComponentTypeEnum.SERVICE)) {
            Set<String> serviceFiltersIDsToBeDeleted =
                    getServiceFiltersRelatedToComponentInstance(service.getUniqueId(), componentInstance);
            if (!serviceFiltersIDsToBeDeleted.isEmpty()) {
                Set<String> ids = service.getComponentInstances().stream()
                                         .filter(ci -> serviceFiltersIDsToBeDeleted
                                                               .contains(ci.getName()))
                                         .map(ComponentInstance::getUniqueId)
                                         .collect(Collectors.toSet());
                Either<Set<String>, StorageOperationStatus> deleteServiceFiltersEither =
                        serviceFilterOperation.deleteNodeFilters(service, ids);
                if (deleteServiceFiltersEither.isRight()) {
                    ActionStatus status = componentsUtils.convertFromStorageResponse(deleteServiceFiltersEither.right().value(),
                            containerComponentType);
                    return Either.right(componentsUtils.getResponseFormat(status, componentInstance.getName()));
                }
                for (String id : ids) {
                    final Optional<ComponentInstance> componentInstanceById = service.getComponentInstanceById(id);
                    if (!componentInstanceById.isPresent()){
                        return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
                    }
                    ComponentInstance ci = componentInstanceById.get();
                    List<String> directives = ci.getDirectives();
                    directives.remove(DirectivesUtils.SELECTABLE);
                    ci.setDirectives(directives);
                    final Either<ComponentInstance, ResponseFormat> componentInstanceResponseFormatEither =
                            updateComponentInstanceMetadata(ComponentTypeEnum.SERVICE_PARAM_NAME, service.getUniqueId(),
                                    ci.getUniqueId(), userId, ci, true, false, false);
                    if (componentInstanceResponseFormatEither.isRight()) {
                        return componentInstanceResponseFormatEither;
                    }
                }
            }
        }
        return Either.left(componentInstance);
    }



    private Set<String> getServiceFiltersRelatedToComponentInstance(String containerComponentId,
            ComponentInstance componentInstance) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstances(false);
        Either<Service, StorageOperationStatus> serviceFilterOrigin =
                toscaOperationFacade.getToscaElement(containerComponentId, filter);
        final Service service = serviceFilterOrigin.left().value();
        final Set<String> nodesFiltersToBeDeleted = ServiceFilterUtils.getNodesFiltersToBeDeleted(service,
                componentInstance);
        return nodesFiltersToBeDeleted;
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

        log.debug(TRY_TO_CREATE_ENTRY_ON_GRAPH);
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

    /**
     * @param componentId
     * @param userId
     * @param requirementDefList
     * @param componentTypeEnum
     * @return
     */
    public List<RequirementCapabilityRelDef> batchDissociateRIFromRI(
            String componentId,
            String userId,
            List<RequirementCapabilityRelDef> requirementDefList,
            ComponentTypeEnum componentTypeEnum) {

        List<RequirementCapabilityRelDef> delOkResult = new ArrayList<>();
        Either<Component, ResponseFormat> validateResponse = validateDissociateRI(componentId, userId, componentTypeEnum);
        if (validateResponse.isRight()) {

            return delOkResult;
        }
        Component containerComponent = validateResponse.left().value();
        Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "associateRIToRI");
        if (lockComponent.isRight()) {
            return delOkResult;
        }
        try {
            for (RequirementCapabilityRelDef requirementDef : requirementDefList) {
                Either<RequirementCapabilityRelDef, ResponseFormat> actionResponse = dissociateRIFromRI(
                        componentId, requirementDef, containerComponent);

                if (actionResponse.isLeft()) {
                    delOkResult.add(actionResponse.left().value());
                }
            }
        } finally {
            unlockComponent(validateResponse, containerComponent);
        }
        return delOkResult;
    }

    public Either<RequirementCapabilityRelDef, ResponseFormat> dissociateRIFromRI(
            String componentId, String userId, RequirementCapabilityRelDef requirementDef, ComponentTypeEnum componentTypeEnum) {
        Either<Component, ResponseFormat> validateResponse = validateDissociateRI(componentId,  userId,  componentTypeEnum);
        if(validateResponse.isRight())
        {
            return Either.right(validateResponse.right().value());
        }
        Either<RequirementCapabilityRelDef, ResponseFormat> actionResponse = null;
        Component containerComponent = validateResponse.left().value();
        Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "associateRIToRI");
        if (lockComponent.isRight()) {
            return Either.right(lockComponent.right().value());
        }
        try {
            actionResponse = dissociateRIFromRI(
                    componentId, requirementDef,containerComponent);
        } finally {
            unlockComponent(validateResponse, containerComponent);
        }
        return actionResponse;
    }

    private Either<Component, ResponseFormat> validateDissociateRI(
            String componentId, String userId, ComponentTypeEnum componentTypeEnum) {
        validateUserExists(userId, "dissociate RI From RI", false);


        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(componentId, componentTypeEnum, null);
        if (validateComponentExists.isRight()) {
            return Either.right(validateComponentExists.right().value());
        }
        org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

        Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
        if (validateCanWorkOnComponent.isRight()) {
            return Either.right(validateCanWorkOnComponent.right().value());
        }
        return Either.left(containerComponent);

    }
    private Either<RequirementCapabilityRelDef, ResponseFormat> dissociateRIFromRI(
            String componentId, RequirementCapabilityRelDef requirementDef, Component containerComponent) {

        Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;
        log.debug(TRY_TO_CREATE_ENTRY_ON_GRAPH);
        Either<RequirementCapabilityRelDef, StorageOperationStatus> result = toscaOperationFacade.dissociateResourceInstances(
                componentId, requirementDef);
        if (result.isLeft()) {
            log.debug("Enty on graph is created.");
            RequirementCapabilityRelDef requirementCapabilityRelDef = result.left().value();
            resultOp = Either.left(requirementCapabilityRelDef);
            return resultOp;

        } else {

            log.debug("Failed to dissocaite node  {} from node {}", requirementDef.getFromNode(), requirementDef.getToNode());
            String fromNameOrId = "";
            String toNameOrId = "";
            Either<ComponentInstance, StorageOperationStatus> fromResult = getResourceInstanceById(
                    containerComponent, requirementDef.getFromNode());
            Either<ComponentInstance, StorageOperationStatus> toResult = getResourceInstanceById(
                    containerComponent, requirementDef.getToNode());

            toNameOrId = requirementDef.getFromNode();
            fromNameOrId = requirementDef.getFromNode();
            if (fromResult.isLeft()) {
                fromNameOrId = fromResult.left().value().getName();
            }
            if (toResult.isLeft()) {
                toNameOrId = toResult.left().value().getName();
            }

            resultOp = Either
                    .right(componentsUtils.getResponseFormat(
                            componentsUtils.convertFromStorageResponseForResourceInstance(
                                    result.right().value(), true), fromNameOrId, toNameOrId, requirementDef.getRelationships().get(0).getRelation().getRequirement()));
            return resultOp;
        }
    }

    /**
     * Allows to get relation contained in specified component according to received Id
     * @param componentId
     * @param relationId
     * @param userId
     * @param componentTypeEnum
     * @return
     */
    public Either<RequirementCapabilityRelDef, ResponseFormat> getRelationById(String componentId, String relationId, String userId, ComponentTypeEnum componentTypeEnum) {

        Either<RequirementCapabilityRelDef, ResponseFormat> resultOp = null;
        try {
            org.openecomp.sdc.be.model.Component containerComponent = null;
            Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = null;
            RequirementCapabilityRelDef foundRelation = null;

            validateUserExists(userId, "get relation by Id", false);

            if(resultOp == null){
                validateComponentExists = validateComponentExists(componentId, componentTypeEnum, null);
                if (validateComponentExists.isRight()) {
                    resultOp = Either.right(validateComponentExists.right().value());
                }
            }
            if(resultOp == null){
                containerComponent = validateComponentExists.left().value();
                List<RequirementCapabilityRelDef> requirementCapabilityRelations = containerComponent.getComponentInstancesRelations();
                foundRelation = findRelation(relationId, requirementCapabilityRelations);
                if(foundRelation == null){
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RELATION_NOT_FOUND, relationId, componentId);
                    log.debug("Relation with id {} was not found on the component", relationId, componentId);
                    resultOp = Either.right(responseFormat);
                }
            }
            if(resultOp == null){
                resultOp = setRelatedCapability(foundRelation, containerComponent);
            }
            if(resultOp.isLeft()){
                resultOp = setRelatedRequirement(foundRelation, containerComponent);
            }
        } catch (Exception e) {
            log.error("The exception {} occured upon get relation {} of the component {} ", e, relationId, componentId);
            resultOp =  Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return resultOp;
    }

    private RequirementCapabilityRelDef findRelation(String relationId, List<RequirementCapabilityRelDef> requirementCapabilityRelations) {
        for(RequirementCapabilityRelDef relationship : requirementCapabilityRelations){
            if(relationship.getRelationships().stream().filter(r -> r.getRelation().getId().equals(relationId)).findFirst().isPresent()){
                return relationship;
            }
        }
        return null;
    }

    private Either<RequirementCapabilityRelDef, ResponseFormat> setRelatedRequirement(RequirementCapabilityRelDef foundRelation, Component containerComponent) {
        Either<RequirementCapabilityRelDef, ResponseFormat> result = null;
        RelationshipInfo relationshipInfo = foundRelation.resolveSingleRelationship().getRelation();
        String instanceId = foundRelation.getFromNode();
        Optional<RequirementDefinition> foundRequirement;
        Optional<ComponentInstance> instance = containerComponent.getComponentInstances().stream().filter(i -> i.getUniqueId().equals(instanceId)).findFirst();
        if(!instance.isPresent()){
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, instanceId, "instance", containerComponent.getComponentType().getValue(), containerComponent.getName());
            log.debug("Component instance with id {} was not found on the component", instanceId, containerComponent.getUniqueId());
            result = Either.right(responseFormat);
        }
        if(result == null){
            for(List<RequirementDefinition> requirements : instance.get().getRequirements().values()){
                foundRequirement = requirements.stream().filter(r -> isBelongingRequirement(relationshipInfo, r)).findFirst();
                if(foundRequirement.isPresent()){
                    foundRelation.resolveSingleRelationship().setRequirement(foundRequirement.get());
                    result = Either.left(foundRelation);
                }
            }
        }
        if(result == null){
            Either<RequirementDataDefinition, StorageOperationStatus> getfulfilledRequirementRes = toscaOperationFacade.getFulfilledRequirementByRelation(containerComponent.getUniqueId(), instanceId, foundRelation, (rel, req)->isBelongingRequirement(rel, req));
            if(getfulfilledRequirementRes.isRight()){
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.REQUIREMENT_OF_INSTANCE_NOT_FOUND_ON_CONTAINER, relationshipInfo.getRequirement(), instanceId, containerComponent.getUniqueId());
                log.debug("Requirement {} of instance {} was not found on the container {}. ", relationshipInfo.getCapability(), instanceId, containerComponent.getUniqueId());
                result = Either.right(responseFormat);
            } else {
                foundRelation.resolveSingleRelationship().setRequirement(getfulfilledRequirementRes.left().value());
            }
        }
        if(result == null){
            result = Either.left(foundRelation);
        }
        return result;
    }

    private boolean isBelongingRequirement(RelationshipInfo relationshipInfo, RequirementDataDefinition req) {
        return  req.getName().equals(relationshipInfo.getRequirement()) &&
                req.getUniqueId().equals(relationshipInfo.getRequirementUid()) &&
                req.getOwnerId().equals(relationshipInfo.getRequirementOwnerId());
    }

    private Either<RequirementCapabilityRelDef, ResponseFormat> setRelatedCapability(RequirementCapabilityRelDef foundRelation, Component containerComponent) {
        Either<RequirementCapabilityRelDef, ResponseFormat> result = null;
        RelationshipInfo relationshipInfo = foundRelation.resolveSingleRelationship().getRelation();
        String instanceId = foundRelation.getToNode();
        Optional<CapabilityDefinition> foundCapability;
        Optional<ComponentInstance> instance = containerComponent.getComponentInstances().stream().filter(i -> i.getUniqueId().equals(instanceId)).findFirst();
        if(!instance.isPresent()){
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, instanceId, "instance", containerComponent.getComponentType().getValue(), containerComponent.getName());
            log.debug("Component instance with id {} was not found on the component", instanceId, containerComponent.getUniqueId());
            result = Either.right(responseFormat);
        }
        if(result == null){
            for(List<CapabilityDefinition> capabilities : instance.get().getCapabilities().values()){
                foundCapability = capabilities.stream().filter(c -> isBelongingCapability(relationshipInfo, c)).findFirst();
                if(foundCapability.isPresent()){
                    foundRelation.resolveSingleRelationship().setCapability(foundCapability.get());
                    result = Either.left(foundRelation);
                }
            }
        }
        if(result == null){
            Either<CapabilityDataDefinition, StorageOperationStatus> getfulfilledRequirementRes =
                    toscaOperationFacade.getFulfilledCapabilityByRelation(containerComponent.getUniqueId(), instanceId, foundRelation, (rel, cap)->isBelongingCapability(rel, cap));
            if(getfulfilledRequirementRes.isRight()){
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_OF_INSTANCE_NOT_FOUND_ON_CONTAINER, relationshipInfo.getCapability(), instanceId, containerComponent.getUniqueId());
                log.debug("Capability {} of instance {} was not found on the container {}. ", relationshipInfo.getCapability(), instanceId, containerComponent.getUniqueId());
                result = Either.right(responseFormat);
            } else {
                foundRelation.resolveSingleRelationship().setCapability(getfulfilledRequirementRes.left().value());
            }
        }
        if(result == null){
            result = Either.left(foundRelation);
        }
        return result;
    }

    private boolean isBelongingCapability(RelationshipInfo relationshipInfo, CapabilityDataDefinition cap) {
        return     cap.getName().equals(relationshipInfo.getCapability()) &&
                cap.getUniqueId().equals(relationshipInfo.getCapabilityUid()) &&
                cap.getOwnerId().equals(relationshipInfo.getCapabilityOwnerId());
    }

    private Either<ComponentInstanceProperty, ResponseFormat> updateAttributeValue(ComponentInstanceProperty attribute, String resourceInstanceId) {
        Either<ComponentInstanceProperty, StorageOperationStatus> eitherAttribute = componentInstanceOperation.updateAttributeValueInResourceInstance(attribute, resourceInstanceId, true);
        Either<ComponentInstanceProperty, ResponseFormat> result;
        if (eitherAttribute.isLeft()) {
            log.debug("Attribute value {} was updated on graph.", attribute.getValueUniqueUid());
            ComponentInstanceProperty instanceAttribute = eitherAttribute.left().value();

            result = Either.left(instanceAttribute);

        } else {
            log.debug("Failed to update attribute value {} in resource instance {}", attribute, resourceInstanceId);

            ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(eitherAttribute.right().value());

            result = Either.right(componentsUtils.getResponseFormat(actionStatus, ""));

        }
        return result;
    }

    private Either<ComponentInstanceProperty, ResponseFormat> createAttributeValue(ComponentInstanceProperty attribute, String resourceInstanceId) {

        Either<ComponentInstanceProperty, ResponseFormat> result;

        Wrapper<Integer> indexCounterWrapper = new Wrapper<>();
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        validateIncrementCounter(resourceInstanceId, GraphPropertiesDictionary.ATTRIBUTE_COUNTER, indexCounterWrapper, errorWrapper);

        if (!errorWrapper.isEmpty()) {
            result = Either.right(errorWrapper.getInnerElement());
        } else {
            Either<ComponentInstanceProperty, StorageOperationStatus> eitherAttribute = componentInstanceOperation.addAttributeValueToResourceInstance(attribute, resourceInstanceId, indexCounterWrapper.getInnerElement(), true);
            if (eitherAttribute.isLeft()) {
                log.debug("Attribute value was added to resource instance {}", resourceInstanceId);
                ComponentInstanceProperty instanceAttribute = eitherAttribute.left().value();
                result = Either.left(instanceAttribute);

            } else {
                log.debug("Failed to add attribute value {}  to resource instance {}", attribute, resourceInstanceId);

                ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(eitherAttribute.right().value());
                result = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

            }
        }
        return result;
    }

    /**
     * Create Or Updates Attribute Instance
     *
     * @param componentTypeEnum
     * @param componentId
     * @param resourceInstanceId
     * @param attribute
     * @param userId
     * @return
     */
    public Either<ComponentInstanceProperty, ResponseFormat> createOrUpdateAttributeValue(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, ComponentInstanceProperty attribute, String userId) {
        Either<ComponentInstanceProperty, ResponseFormat> result = null;
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

        validateUserExist(userId, "create Or Update Attribute Value");
        if (errorWrapper.isEmpty()) {
            validateComponentTypeEnum(componentTypeEnum, "CreateOrUpdateAttributeValue", errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            validateCanWorkOnComponent(componentId, componentTypeEnum, userId, errorWrapper);
        }
        if (errorWrapper.isEmpty()) {
            validateComponentLock(componentId, componentTypeEnum, errorWrapper);
        }

        try {
            if (errorWrapper.isEmpty()) {
                final boolean isCreate = Objects.isNull(attribute.getValueUniqueUid());
                if (isCreate) {
                    result = createAttributeValue(attribute, resourceInstanceId);
                } else {
                    result = updateAttributeValue(attribute, resourceInstanceId);
                }
            } else {
                result = Either.right(errorWrapper.getInnerElement());
            }
            return result;
        }

        finally {
            if (result == null || result.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }
    }

    private boolean isNetworkRoleServiceProperty(ComponentInstanceProperty property, ComponentTypeEnum componentTypeEnum) {
        return StringUtils.isNotEmpty(property.getValue())
                && PropertyNames.NETWORK_ROLE.getPropertyName().equalsIgnoreCase(property.getName())
                && ComponentTypeEnum.SERVICE == componentTypeEnum;
    }

    // US833308 VLI in service - specific network_role property value logic
    private StorageOperationStatus concatServiceNameToVLINetworkRolePropertiesValues(ToscaOperationFacade toscaOperationFacade, ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, List<ComponentInstanceProperty> properties) {
        for (ComponentInstanceProperty property: properties) {
            if (isNetworkRoleServiceProperty(property, componentTypeEnum)) {
                ComponentParametersView componentParametersView = new ComponentParametersView();
                componentParametersView.disableAll();
                componentParametersView.setIgnoreComponentInstances(false);
                Either<Component, StorageOperationStatus> getServiceResult = toscaOperationFacade.getToscaElement(componentId, componentParametersView);
                if (getServiceResult.isRight()) {
                    return getServiceResult.right().value();
                }
                Component service = getServiceResult.left().value();
                Optional<ComponentInstance> getInstance = service.getComponentInstances().stream().filter(p -> p.getUniqueId().equals(resourceInstanceId)).findAny();
                if (!getInstance.isPresent()) {
                    return StorageOperationStatus.NOT_FOUND;
                }
                String prefix = service.getSystemName() + ".";
                String value = property.getValue();
                if (OriginTypeEnum.VL == getInstance.get().getOriginType() && (!value.startsWith(prefix) || value.equalsIgnoreCase(prefix))) {
                    property.setValue(prefix + value);
                }
            }
        }
        return StorageOperationStatus.OK;
    }

    public Either<List<ComponentInstanceProperty>, ResponseFormat> createOrUpdatePropertiesValues(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, List<ComponentInstanceProperty> properties, String userId) {

        Either<List<ComponentInstanceProperty>, ResponseFormat> resultOp = null;

        /*-------------------------------Validations---------------------------------*/

        validateUserExists(userId, "create Or Update Properties Values", false);

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError("CreateOrUpdatePropertiesValues", INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
            return resultOp;
        }
        Either<Component, StorageOperationStatus> getResourceResult = toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll);

        if (getResourceResult.isRight()) {
            log.debug(FAILED_TO_RETRIEVE_COMPONENT_COMPONENT_ID, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }
        Component containerComponent = getResourceResult.left().value();

        if (!ComponentValidationUtils.canWorkOnComponent(containerComponent, userId)) {
            log.info("Restricted operation for user: {} on service {}", userId, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }

		//Validate value and Constraint of property
		Either<Boolean, ResponseFormat> constraintValidatorResponse =
				PropertyValueConstraintValidationUtil.getInstance().
						validatePropertyConstraints(properties, applicationDataTypeCache);
		if (constraintValidatorResponse.isRight()) {
			log.error("Failed validation value and constraint of property: {}",
					constraintValidatorResponse.right().value());
			return Either.right(constraintValidatorResponse.right().value());
		}

        Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, resourceInstanceId);
        if (resourceInstanceStatus.isRight()) {
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, resourceInstanceId, componentId));
            return resultOp;
        }
        ComponentInstance foundResourceInstance = resourceInstanceStatus.left().value();
        // specific property value logic US833308
        StorageOperationStatus fetchByIdsStatus = concatServiceNameToVLINetworkRolePropertiesValues(toscaOperationFacade, componentTypeEnum, componentId, resourceInstanceId, properties);
        if (StorageOperationStatus.OK != fetchByIdsStatus) {
            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(fetchByIdsStatus)));
            return resultOp;
        }
        // lock resource
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug(FAILED_TO_LOCK_SERVICE, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
            return resultOp;
        }

        try {
            for (ComponentInstanceProperty property: properties) {
                String propertyParentUniqueId = property.getParentUniqueId();
                Either<String, ResponseFormat> updatedPropertyValue = updatePropertyObjectValue(property, false);
                Optional<CapabilityDefinition>
                        capPropDefinition = getPropertyCapabilityOfChildInstance(propertyParentUniqueId, foundResourceInstance.getCapabilities());
                if(capPropDefinition.isPresent()) {
                    updatedPropertyValue
                            .bimap(updatedValue -> updateCapabilityPropFromUpdateInstProp(property, updatedValue,
                                    containerComponent, foundResourceInstance, capPropDefinition.get().getType(),
                                    capPropDefinition.get().getName()), Either::right);
                }
                else {
                    updatedPropertyValue.bimap(updatedValue -> updatePropertyOnContainerComponent(property, updatedValue,
                            containerComponent, foundResourceInstance), Either::right);
                }
            }

            Either<Component, StorageOperationStatus> updateContainerRes = toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(containerComponent);
            if (updateContainerRes.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(updateContainerRes.right().value());
                resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
                return resultOp;
            }
            resultOp = Either.left(properties);
            return resultOp;

        } finally {
            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }
    }

    private ResponseFormat updateCapabilityPropertyOnContainerComponent(ComponentInstanceProperty property,
                                                                        String newValue, Component containerComponent, ComponentInstance foundResourceInstance,
                                                                        String capabilityType, String capabilityName) {
        String componentInstanceUniqueId = foundResourceInstance.getUniqueId();
        StringBuffer sb = new StringBuffer(componentInstanceUniqueId);
        sb.append(ModelConverter.CAP_PROP_DELIM).append(property.getOwnerId()).append(ModelConverter.CAP_PROP_DELIM)
                .append(capabilityType).append(ModelConverter.CAP_PROP_DELIM).append(capabilityName);
        String capKey = sb.toString();

        ResponseFormat actionStatus = updateCapPropOnContainerComponent(property, newValue, containerComponent,
                foundResourceInstance, capabilityType, capabilityName, componentInstanceUniqueId, capKey);
        if (actionStatus != null) {
            return actionStatus;
        }

        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private ResponseFormat updateCapabilityPropFromUpdateInstProp(ComponentInstanceProperty property,
                                                                  String newValue, Component containerComponent,
                                                                  ComponentInstance foundResourceInstance,
                                                                  String capabilityType, String capabilityName) {
        String componentInstanceUniqueId = foundResourceInstance.getUniqueId();
        Either<Component, StorageOperationStatus> getComponentRes =
                toscaOperationFacade.getToscaFullElement(foundResourceInstance.getComponentUid());
        if(getComponentRes.isRight()) {
            return componentsUtils.getResponseFormat(getComponentRes.right().value());
        }
        String propOwner;
        if(!PropertiesUtils.isNodeServiceProxy(getComponentRes.left().value())) {
            propOwner = componentInstanceUniqueId;
        } else {
            propOwner = foundResourceInstance.getSourceModelUid();
        }
        StringBuffer sb = new StringBuffer(componentInstanceUniqueId);

        sb.append(ModelConverter.CAP_PROP_DELIM).append(propOwner).append(ModelConverter.CAP_PROP_DELIM)
                .append(capabilityType).append(ModelConverter.CAP_PROP_DELIM).append(capabilityName);
        String capKey = sb.toString();

        ResponseFormat actionStatus = updateCapPropOnContainerComponent(property, newValue, containerComponent,
                foundResourceInstance, capabilityType, capabilityName, componentInstanceUniqueId, capKey);
        if (actionStatus != null) {
            return actionStatus;
        }

        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private ResponseFormat updateCapPropOnContainerComponent(ComponentInstanceProperty property, String newValue,
                                                             Component containerComponent,
                                                             ComponentInstance foundResourceInstance,
                                                             String capabilityType, String capabilityName,
                                                             String componentInstanceUniqueId, String capKey) {
        Map<String, List<CapabilityDefinition>> capabilities =
                Optional.ofNullable(foundResourceInstance.getCapabilities()).orElse(Collections.emptyMap());
        List<CapabilityDefinition> capPerType =
                Optional.ofNullable(capabilities.get(capabilityType)).orElse(Collections.EMPTY_LIST);
        Optional<CapabilityDefinition> cap =
                capPerType.stream().filter(c -> c.getName().equals(capabilityName)).findAny();
        if (cap.isPresent()) {
            List<ComponentInstanceProperty> capProperties = cap.get().getProperties();
            if (capProperties != null) {
                Optional<ComponentInstanceProperty> instanceProperty =
                        capProperties.stream().filter(p -> p.getUniqueId().equals(property.getUniqueId())).findAny();
                StorageOperationStatus status;
                if (instanceProperty.isPresent()) {
                    instanceProperty.get().setValue(newValue);
                    List<String> path = new ArrayList<>();
                    path.add(componentInstanceUniqueId);
                    path.add(capKey);
                    instanceProperty.get().setPath(path);
                    status = toscaOperationFacade.updateComponentInstanceCapabiltyProperty(containerComponent,
                            componentInstanceUniqueId, capKey, instanceProperty.get());
                    if (status != StorageOperationStatus.OK) {
                        ActionStatus actionStatus =
                                componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
                        return componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, "");

                    }
                    foundResourceInstance.setCustomizationUUID(UUID.randomUUID().toString());
                }
            }
        }
        return null;
    }

    private ResponseFormat updatePropertyOnContainerComponent(ComponentInstanceProperty property, String newValue,
                                                              Component containerComponent, ComponentInstance foundResourceInstance) {
        List<ComponentInstanceProperty> instanceProperties =
                containerComponent.getComponentInstancesProperties().get(foundResourceInstance.getUniqueId());
        Optional<ComponentInstanceProperty> instanceProperty =
                instanceProperties.stream().filter(p -> p.getUniqueId().equals(property.getUniqueId())).findAny();
        StorageOperationStatus status;
        instanceProperty.get().setValue(newValue);
        if (instanceProperty.isPresent()) {
            status = toscaOperationFacade
                    .updateComponentInstanceProperty(containerComponent, foundResourceInstance.getUniqueId(),
                            property);
        } else {
            status = toscaOperationFacade
                    .addComponentInstanceProperty(containerComponent, foundResourceInstance.getUniqueId(),
                            property);
        }
        if (status != StorageOperationStatus.OK) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
            return componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, "");
        }
        List<String> path = new ArrayList<>();
        path.add(foundResourceInstance.getUniqueId());
        property.setPath(path);

        foundResourceInstance.setCustomizationUUID(UUID.randomUUID().toString());
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private <T extends PropertyDefinition> Either<String,ResponseFormat> updatePropertyObjectValue(T property, boolean isInput) {
        Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypesEither = dataTypeCache.getAll();
        if (allDataTypesEither.isRight()) {
            TitanOperationStatus status = allDataTypesEither.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(status))));
        }
        Map<String, DataTypeDefinition> allDataTypes = allDataTypesEither.left().value();
        String innerType = null;
        String propertyType = property.getType();
        ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
        log.debug("The type of the property {} is {}", property.getUniqueId(), propertyType);

        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            SchemaDefinition def = property.getSchema();
            if (def == null) {
                log.debug("Schema doesn't exists for property of type {}", type);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
            innerType = propDef.getType();
        }
        // Specific Update Logic
        Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, property.getValue(), true, innerType, allDataTypes);
        String newValue = property.getValue();
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (!res) {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT))));
            }
        } else {
            Object object = isValid.left().value();
            if (object != null) {
                newValue = object.toString();
            }
        }
        if (!isInput) {
            ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, ((ComponentInstanceProperty) property).getRules(), innerType, allDataTypes, true);
            if (pair.getRight() != null && pair.getRight() == false) {
                BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), property.getName(), propertyType);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT))));
            }
        }
        return Either.left(newValue);
    }

    private ResponseFormat updateInputOnContainerComponent(ComponentInstanceInput input, String newValue, Component containerComponent, ComponentInstance foundResourceInstance) {
        List<ComponentInstanceInput> instanceProperties = containerComponent.getComponentInstancesInputs().get(foundResourceInstance.getUniqueId());
        Optional<ComponentInstanceInput> instanceProperty = instanceProperties.stream().filter(p -> p.getUniqueId().equals(input.getUniqueId())).findAny();
        StorageOperationStatus status;
        if (instanceProperty.isPresent()) {
            instanceProperty.get().setValue(input.getValue());
            status = toscaOperationFacade.updateComponentInstanceInput(containerComponent, foundResourceInstance.getUniqueId(), input);
        } else {
            status = toscaOperationFacade.addComponentInstanceInput(containerComponent, foundResourceInstance.getUniqueId(), input);
        }
        if (status != StorageOperationStatus.OK) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
            return componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, "");
        }
        foundResourceInstance.setCustomizationUUID(UUID.randomUUID().toString());
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    public Either<List<ComponentInstanceInput>, ResponseFormat> createOrUpdateInstanceInputValues(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, List<ComponentInstanceInput> inputs, String userId) {

        Either<List<ComponentInstanceInput>, ResponseFormat> resultOp = null;

        validateUserExists(userId, "create Or Update Property Value", false);

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_OR_UPDATE_PROPERTY_VALUE, INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
            return resultOp;
        }
        Either<Component, StorageOperationStatus> getResourceResult = toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll);

        if (getResourceResult.isRight()) {
            log.debug(FAILED_TO_RETRIEVE_COMPONENT_COMPONENT_ID, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }
        Component containerComponent = getResourceResult.left().value();

        if (!ComponentValidationUtils.canWorkOnComponent(containerComponent, userId)) {
            log.info("Restricted operation for user: {} on service {}", userId, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }
        Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, resourceInstanceId);
        if (resourceInstanceStatus.isRight()) {
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, resourceInstanceId, componentId));
            return resultOp;
        }

        ComponentInstance foundResourceInstance = resourceInstanceStatus.left().value();

        // lock resource
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug(FAILED_TO_LOCK_SERVICE, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
            return resultOp;
        }
        try {
            for (ComponentInstanceInput input: inputs) {
                Either<String, ResponseFormat> updatedInputValue = updatePropertyObjectValue(input, true);
                updatedInputValue.bimap(updatedValue -> updateInputOnContainerComponent(input,updatedValue, containerComponent, foundResourceInstance),
                        Either::right);

            }
            Either<Component, StorageOperationStatus> updateContainerRes = toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(containerComponent);

            if (updateContainerRes.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(updateContainerRes.right().value());
                resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
                return resultOp;
            }
            resultOp = Either.left(inputs);
            return resultOp;

        } finally {
            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }

    }

    public Either<ComponentInstanceProperty, ResponseFormat> createOrUpdateGroupInstancePropertyValue(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, String groupInstanceId, ComponentInstanceProperty property,
                                                                                                      String userId) {

        Either<ComponentInstanceProperty, ResponseFormat> resultOp = null;

        validateUserExists(userId, "create Or Update Property Value", false);

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_OR_UPDATE_PROPERTY_VALUE, INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
            return resultOp;
        }

        if (!ComponentValidationUtils.canWorkOnComponent(componentId, toscaOperationFacade, userId)) {
            log.info("Restricted operation for user: {} on service: {}", userId, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }
        // lock resource
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug(FAILED_TO_LOCK_SERVICE, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
            return resultOp;
        }
        try {
            String propertyValueUid = property.getValueUniqueUid();

            if (propertyValueUid == null) {

                Either<Integer, StorageOperationStatus> counterRes = groupInstanceOperation.increaseAndGetGroupInstancePropertyCounter(groupInstanceId);

                if (counterRes.isRight()) {
                    log.debug("increaseAndGetResourcePropertyCounter failed resource instance: {} property: {}", resourceInstanceId, property);
                    StorageOperationStatus status = counterRes.right().value();
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
                    resultOp = Either.right(componentsUtils.getResponseFormat(actionStatus));
                }
                Integer index = counterRes.left().value();
                Either<ComponentInstanceProperty, StorageOperationStatus> result = groupInstanceOperation.addPropertyValueToGroupInstance(property, resourceInstanceId, index, true);

                if (result.isLeft()) {
                    log.trace("Property value was added to resource instance {}", resourceInstanceId);
                    ComponentInstanceProperty instanceProperty = result.left().value();

                    resultOp = Either.left(instanceProperty);

                } else {
                    log.debug("Failed to add property value: {} to resource instance {}", property, resourceInstanceId);

                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

                    resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
                }

            } else {
                Either<ComponentInstanceProperty, StorageOperationStatus> result = groupInstanceOperation.updatePropertyValueInGroupInstance(property, resourceInstanceId, true);

                if (result.isLeft()) {
                    log.debug("Property value {} was updated on graph.", property.getValueUniqueUid());
                    ComponentInstanceProperty instanceProperty = result.left().value();

                    resultOp = Either.left(instanceProperty);

                } else {
                    log.debug("Failed to update property value: {}, in resource instance {}", property, resourceInstanceId);

                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

                    resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
                }
            }
            if (resultOp.isLeft()) {
                StorageOperationStatus updateCustomizationUUID = componentInstanceOperation.updateCustomizationUUID(resourceInstanceId);
                if (updateCustomizationUUID != StorageOperationStatus.OK) {
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(updateCustomizationUUID);

                    resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

                }
            }
            return resultOp;

        } finally {
            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }

    }

    public Either<ComponentInstanceInput, ResponseFormat> createOrUpdateInputValue(ComponentTypeEnum componentTypeEnum, String componentId, String resourceInstanceId, ComponentInstanceInput inputProperty, String userId) {

        Either<ComponentInstanceInput, ResponseFormat> resultOp = null;

        validateUserExists(userId, "create Or Update Input Value", false);

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError("createOrUpdateInputValue", INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
            return resultOp;
        }

        if (!ComponentValidationUtils.canWorkOnComponent(componentId, toscaOperationFacade, userId)) {
            log.info("Restricted operation for user: {} on service: {}", userId, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }
        // lock resource
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(componentId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug(FAILED_TO_LOCK_SERVICE, componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
            return resultOp;
        }
        try {
            String propertyValueUid = inputProperty.getValueUniqueUid();
            if (propertyValueUid == null) {

                Either<Integer, StorageOperationStatus> counterRes = componentInstanceOperation.increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, GraphPropertiesDictionary.INPUT_COUNTER, true);

                if (counterRes.isRight()) {
                    log.debug("increaseAndGetResourceInputCounter failed resource instance {} inputProperty {}", resourceInstanceId, inputProperty);
                    StorageOperationStatus status = counterRes.right().value();
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
                    resultOp = Either.right(componentsUtils.getResponseFormat(actionStatus));
                }
                Integer index = counterRes.left().value();
                Either<ComponentInstanceInput, StorageOperationStatus> result = componentInstanceOperation.addInputValueToResourceInstance(inputProperty, resourceInstanceId, index, true);

                if (result.isLeft()) {
                    log.debug("Property value was added to resource instance {}", resourceInstanceId);
                    ComponentInstanceInput instanceProperty = result.left().value();

                    resultOp = Either.left(instanceProperty);
                    return resultOp;

                } else {
                    log.debug("Failed to add input value {} to resource instance {}", inputProperty, resourceInstanceId);

                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

                    resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

                    return resultOp;
                }

            } else {
                Either<ComponentInstanceInput, StorageOperationStatus> result = componentInstanceOperation.updateInputValueInResourceInstance(inputProperty, resourceInstanceId, true);

                if (result.isLeft()) {
                    log.debug("Input value {} was updated on graph.", inputProperty.getValueUniqueUid());
                    ComponentInstanceInput instanceProperty = result.left().value();

                    resultOp = Either.left(instanceProperty);
                    return resultOp;

                } else {
                    log.debug("Failed to update property value {} in resource instance {}", inputProperty, resourceInstanceId);

                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

                    resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

                    return resultOp;
                }
            }

        } finally {
            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
        }

    }

    public Either<ComponentInstanceProperty, ResponseFormat> deletePropertyValue(ComponentTypeEnum componentTypeEnum, String serviceId, String resourceInstanceId, String propertyValueId, String userId) {

        validateUserExists(userId, "delete Property Value", false);

        Either<ComponentInstanceProperty, ResponseFormat> resultOp = null;

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(CREATE_OR_UPDATE_PROPERTY_VALUE, INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
            return resultOp;
        }

        if (!ComponentValidationUtils.canWorkOnComponent(serviceId, toscaOperationFacade, userId)) {
            log.info("Restricted operation for user {} on service {}", userId, serviceId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }
        // lock resource
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(serviceId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug(FAILED_TO_LOCK_SERVICE, serviceId);
            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
            return resultOp;
        }
        try {
            Either<ComponentInstanceProperty, StorageOperationStatus> result = propertyOperation.removePropertyValueFromResourceInstance(propertyValueId, resourceInstanceId, true);

            if (result.isLeft()) {
                log.debug("Property value {} was removed from graph.", propertyValueId);
                ComponentInstanceProperty instanceProperty = result.left().value();

                resultOp = Either.left(instanceProperty);
                return resultOp;

            } else {
                log.debug("Failed to remove property value {} in resource instance {}", propertyValueId, resourceInstanceId);

                ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(result.right().value());

                resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));

                return resultOp;
            }

        } finally {
            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(serviceId, componentTypeEnum.getNodeType());
        }

    }

    private Either<Component, ResponseFormat> getAndValidateOriginComponentOfComponentInstance(ComponentTypeEnum containerComponentType, ComponentInstance componentInstance) {

        Either<Component, ResponseFormat> eitherResponse = null;
        ComponentTypeEnum componentType = getComponentTypeByParentComponentType(containerComponentType);
        Component component;
        ResponseFormat errorResponse;
        Either<Component, StorageOperationStatus> getComponentRes = toscaOperationFacade.getToscaFullElement(componentInstance.getComponentUid());
        if (getComponentRes.isRight()) {
            log.debug("Failed to get the component with id {} for component instance {} creation. ", componentInstance.getComponentUid(), componentInstance.getName());
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentRes.right().value(), componentType);
            errorResponse = componentsUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
            eitherResponse = Either.right(errorResponse);
        }
        if (eitherResponse == null) {
            component = getComponentRes.left().value();
            LifecycleStateEnum resourceCurrState = component.getLifecycleState();
            if (resourceCurrState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
                ActionStatus actionStatus = ActionStatus.ILLEGAL_COMPONENT_STATE;
                errorResponse = componentsUtils.getResponseFormat(actionStatus, component.getComponentType().toString(), component.getName(), resourceCurrState.toString());
                eitherResponse = Either.right(errorResponse);
            }
        }
        if (eitherResponse == null) {
            eitherResponse = Either.left(getComponentRes.left().value());
        }
        return eitherResponse;
    }

    public Either<Set<String>, ResponseFormat> forwardingPathOnVersionChange(String containerComponentParam,
                                                                             String containerComponentId,
                                                                             String componentInstanceId,
                                                                             ComponentInstance newComponentInstance) {
        Either<Set<String>, ResponseFormat> resultOp;
        Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
        if (validateComponentType.isRight()) {
            return Either.right(validateComponentType.right().value());
        }
        final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
        ComponentParametersView componentParametersView = getComponentParametersViewForForwardingPath();

        //Fetch Component
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists =
                validateComponentExists(containerComponentId, containerComponentType, componentParametersView);
        if (validateComponentExists.isRight()) {
            return Either.right(validateComponentExists.right().value());
        }
        Component containerComponent = validateComponentExists.left().value();

        //Fetch current component instance
        Either<ComponentInstance, StorageOperationStatus> eitherResourceInstance =
                getResourceInstanceById(containerComponent, componentInstanceId);
        if (eitherResourceInstance.isRight()) {
            resultOp = Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceId, containerComponentId));
            return resultOp;
        }
        ComponentInstance currentResourceInstance = eitherResourceInstance.left().value();

        //Check whether new componentInstance exists
        String resourceId = newComponentInstance.getComponentUid();
        Either<Boolean, StorageOperationStatus> componentExistsRes = toscaOperationFacade.validateComponentExists(resourceId);
        if (componentExistsRes.isRight()) {
            log.debug("Failed to find resource {} ", resourceId);
            resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse
                    (componentExistsRes.right().value()), resourceId));
            return resultOp;
        } else if (!componentExistsRes.left().value()) {
            log.debug("The resource {} not found ", resourceId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
            return resultOp;
        }

        //Fetch component using new component instance uid
        Either<Component, ResponseFormat> eitherResourceName = getOriginComponentFromComponentInstance(newComponentInstance);
        if (eitherResourceName.isRight()) {
            resultOp = Either.right(eitherResourceName.right().value());
            return resultOp;
        }
        Component updatedContainerComponent=eitherResourceName.left().value();
        Set<String> toDeleteForwardingPaths = getForwardingPaths(containerComponent,
                currentResourceInstance, updatedContainerComponent);
        resultOp=Either.left(toDeleteForwardingPaths);

        return resultOp;
    }

    private Set<String> getForwardingPaths(Component containerComponent, ComponentInstance currentResourceInstance,
                                           Component updatedContainerComponent) {
        DataForMergeHolder dataForMergeHolder=new DataForMergeHolder();
        dataForMergeHolder.setOrigComponentInstId(currentResourceInstance.getUniqueId());

        Service service = (Service) containerComponent;
        ForwardingPathUtils forwardingPathUtils = new ForwardingPathUtils();

        return forwardingPathUtils.
                getForwardingPathsToBeDeletedOnVersionChange(service,dataForMergeHolder,updatedContainerComponent);
    }

    private ComponentParametersView getComponentParametersViewForForwardingPath() {
        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.setIgnoreCapabiltyProperties(false);
        componentParametersView.setIgnoreForwardingPath(false);
        return componentParametersView;
    }

    public Either<ComponentInstance, ResponseFormat> changeComponentInstanceVersion(String containerComponentParam, String containerComponentId, String componentInstanceId, String userId, ComponentInstance newComponentInstance) {

        User user = validateUserExists(userId, "change Component Instance Version", false);

        Either<ComponentInstance, ResponseFormat> resultOp = null;

        Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
        if (validateComponentType.isRight()) {
            return Either.right(validateComponentType.right().value());
        }

        final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.setIgnoreCapabiltyProperties(false);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponentExists = validateComponentExists(containerComponentId, containerComponentType, componentParametersView);
        if (validateComponentExists.isRight()) {
            return Either.right(validateComponentExists.right().value());
        }
        org.openecomp.sdc.be.model.Component containerComponent = validateComponentExists.left().value();

        Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
        if (validateCanWorkOnComponent.isRight()) {
            return Either.right(validateCanWorkOnComponent.right().value());
        }

        Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, componentInstanceId);
        if (resourceInstanceStatus.isRight()) {
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceId, containerComponentId));
            return resultOp;
        }

        ComponentInstance currentResourceInstance = resourceInstanceStatus.left().value();

        return changeInstanceVersion(containerComponent, currentResourceInstance, newComponentInstance, user, containerComponentType );
    }

    public Either<ComponentInstance, ResponseFormat> changeInstanceVersion(org.openecomp.sdc.be.model.Component containerComponent, ComponentInstance currentResourceInstance,
                                                                           ComponentInstance newComponentInstance, User user, final ComponentTypeEnum containerComponentType    ) {
        Either<ComponentInstance, ResponseFormat> resultOp = null;
        Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus;

        Either<Boolean, ResponseFormat> lockComponent = lockComponent(containerComponent, "changeComponentInstanceVersion");
        String containerComponentId = containerComponent.getUniqueId();
        String componentInstanceId = currentResourceInstance.getUniqueId();
        if (lockComponent.isRight()) {
            return Either.right(lockComponent.right().value());
        }

        try {


            if (currentResourceInstance.getComponentUid().equals(newComponentInstance.getComponentUid())) {
                resultOp = Either.left(currentResourceInstance);
                return resultOp;

            }
            String resourceId = newComponentInstance.getComponentUid();



            Either<Boolean, StorageOperationStatus> componentExistsRes = toscaOperationFacade.validateComponentExists(resourceId);
            if (componentExistsRes.isRight()) {
                log.debug("Failed to validate existing of the component {}. Status is {} ", resourceId);
                resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentExistsRes.right().value()), resourceId));
                return resultOp;
            } else if (!componentExistsRes.left().value()) {
                log.debug("The resource {} not found ", resourceId);
                resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
                return resultOp;
            }

            Either<Component, ResponseFormat> eitherOriginComponent = getInstanceOriginNode(currentResourceInstance);

            if (eitherOriginComponent.isRight()) {
                resultOp = Either.right(eitherOriginComponent.right().value());
                return resultOp;
            }
            DataForMergeHolder dataHolder = compInstMergeDataBL.saveAllDataBeforeDeleting(containerComponent, currentResourceInstance, eitherOriginComponent.left().value());
            resultOp = deleteComponentInstance(containerComponent, componentInstanceId, containerComponentType);
            if (resultOp.isRight()) {
                log.debug("failed to delete resource instance {}", resourceId);
                return resultOp;
            }
            ComponentInstance resResourceInfo = resultOp.left().value();
            Component origComponent = null;
            OriginTypeEnum originType = currentResourceInstance.getOriginType();
            if (originType == OriginTypeEnum.ServiceProxy) {
                Either<Component, StorageOperationStatus> serviceProxyOrigin = toscaOperationFacade.getLatestByName("serviceProxy");
                if (serviceProxyOrigin.isRight()) {
                    log.debug("Failed to fetch normative service proxy resource by tosca name, error {}", serviceProxyOrigin.right().value());
                    return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceProxyOrigin.right().value())));
                }
                origComponent = serviceProxyOrigin.left().value();

                StorageOperationStatus fillProxyRes = fillProxyInstanceData(newComponentInstance, origComponent);

                if (fillProxyRes != StorageOperationStatus.OK) {
                    log.debug("Failed to fill service proxy resource data with data from service, error {}", fillProxyRes);
                    return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(fillProxyRes)));

                }
                newComponentInstance.setOriginType(originType);
            }else{


                Either<Component, ResponseFormat> eitherResourceName = getOriginComponentFromComponentInstance(newComponentInstance);

                if (eitherResourceName.isRight()) {
                    resultOp = Either.right(eitherResourceName.right().value());
                    return resultOp;
                }

                origComponent = eitherResourceName.left().value();

                newComponentInstance.setName(resResourceInfo.getName());
            }

            newComponentInstance.setInvariantName(resResourceInfo.getInvariantName());
            newComponentInstance.setPosX(resResourceInfo.getPosX());
            newComponentInstance.setPosY(resResourceInfo.getPosY());
            newComponentInstance.setDescription(resResourceInfo.getDescription());

            resultOp = createComponentInstanceOnGraph(containerComponent, origComponent, newComponentInstance, user);

            if (resultOp.isRight()) {
                log.debug("failed to create resource instance {}", resourceId);
                return resultOp;
            }

            ComponentInstance updatedComponentInstance = resultOp.left().value();
            if (resultOp.isRight()) {
                log.debug("failed to create resource instance {}", resourceId);
                return resultOp;
            }

            dataHolder.setCurrInstanceNode(origComponent);
            Either<Component, ResponseFormat> mergeStatusEither = compInstMergeDataBL.mergeComponentUserOrigData(user, dataHolder, containerComponent, containerComponentId, newComponentInstance.getUniqueId());
            if (mergeStatusEither.isRight()) {
                return Either.right(mergeStatusEither.right().value());
            }

            ActionStatus postChangeVersionResult = onChangeInstanceOperationOrchestrator.doPostChangeVersionOperations(containerComponent, currentResourceInstance, newComponentInstance);
            if (postChangeVersionResult != ActionStatus.OK) {
                return Either.right(componentsUtils.getResponseFormat(postChangeVersionResult));
            }

            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreComponentInstances(false);
            Either<Component, StorageOperationStatus> updatedComponentRes = toscaOperationFacade.getToscaElement(containerComponentId, filter);
            if (updatedComponentRes.isRight()) {
                StorageOperationStatus storageOperationStatus = updatedComponentRes.right().value();
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus, containerComponent.getComponentType());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
                log.debug("Component with id {} was not found", containerComponentId);
                return Either.right(responseFormat);
            }
            resourceInstanceStatus = getResourceInstanceById(updatedComponentRes.left().value(), updatedComponentInstance.getUniqueId());
            if (resourceInstanceStatus.isRight()) {
                resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceInstanceStatus.right().value()), updatedComponentInstance.getUniqueId()));
                return resultOp;
            }
            resultOp = Either.left(resourceInstanceStatus.left().value());
            return resultOp;

        } finally {
            unlockComponent(resultOp, containerComponent);
        }
    }

    // US831698
    public Either<List<ComponentInstanceProperty>, ResponseFormat> getComponentInstancePropertiesById(String containerComponentTypeParam, String containerComponentId, String componentInstanceUniqueId, String userId) {
        final String ECOMP_ERROR_CONTEXT = "Get Component Instance Properties By Id";
        Component containerComponent = null;

        Either<List<ComponentInstanceProperty>, ResponseFormat> resultOp = null;
        try {
            validateUserExists(userId, ECOMP_ERROR_CONTEXT, false);

            Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentTypeParam);
            if (validateComponentType.isRight()) {
                resultOp = Either.right(validateComponentType.right().value());
                return resultOp;
            }

            Either<Component, StorageOperationStatus> validateContainerComponentExists = toscaOperationFacade.getToscaElement(containerComponentId);
            if (validateContainerComponentExists.isRight()) {
                resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(validateContainerComponentExists.right().value())));
                return resultOp;
            }
            containerComponent = validateContainerComponentExists.left().value();

            Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, componentInstanceUniqueId);
            if (resourceInstanceStatus.isRight()) {
                resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceUniqueId, containerComponentId));
                return resultOp;
            }

            List<ComponentInstanceProperty> instanceProperties = containerComponent.getComponentInstancesProperties().get(componentInstanceUniqueId);
            if (CollectionUtils.isEmpty(instanceProperties)) {
                instanceProperties = new ArrayList<>();
            }
            resultOp = Either.left(instanceProperties);
            return resultOp;
        } finally {
            unlockComponent(resultOp, containerComponent);
        }
    }

    protected void validateIncrementCounter(String resourceInstanceId, GraphPropertiesDictionary counterType, Wrapper<Integer> instaceCounterWrapper, Wrapper<ResponseFormat> errorWrapper) {
        Either<Integer, StorageOperationStatus> counterRes = componentInstanceOperation.increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, counterType, true);

        if (counterRes.isRight()) {
            log.debug("increase And Get {} failed resource instance {}", counterType, resourceInstanceId);
            StorageOperationStatus status = counterRes.right().value();
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
            errorWrapper.setInnerElement(componentsUtils.getResponseFormat(actionStatus));
        } else {
            instaceCounterWrapper.setInnerElement(counterRes.left().value());
        }

    }

    /**
     * updates componentInstance modificationTime
     *
     * @param componentInstance
     * @param componentInstanceType
     * @param modificationTime
     * @param inTransaction
     * @return
     */
    public Either<ComponentInstanceData, ResponseFormat> updateComponentInstanceModificationTimeAndCustomizationUuid(ComponentInstance componentInstance, NodeTypeEnum componentInstanceType, Long modificationTime, boolean inTransaction) {
        Either<ComponentInstanceData, ResponseFormat> result;
        Either<ComponentInstanceData, StorageOperationStatus> updateComponentInstanceRes = componentInstanceOperation.updateComponentInstanceModificationTimeAndCustomizationUuidOnGraph(componentInstance, componentInstanceType, modificationTime,
                inTransaction);
        if (updateComponentInstanceRes.isRight()) {
            log.debug("Failed to update component instance {} with new last update date and mofifier. Status is {}. ", componentInstance.getName(), updateComponentInstanceRes.right().value());
            result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateComponentInstanceRes.right().value())));
        } else {
            result = Either.left(updateComponentInstanceRes.left().value());
        }
        return result;
    }

    public Either<ComponentInstance, ResponseFormat> deleteServiceProxy() {
        // TODO Add implementation
        Either<ComponentInstance, ResponseFormat> result = Either.left(new ComponentInstance());
        return result;
    }

    public Either<ComponentInstance, ResponseFormat> createServiceProxy() {
        // TODO Add implementation
        Either<ComponentInstance, ResponseFormat> result = Either.left(new ComponentInstance());
        return result;
    }

    public Either<ComponentInstance, ResponseFormat> changeServiceProxyVersion() {
        // TODO Add implementation
        Either<ComponentInstance, ResponseFormat> result = Either.left(new ComponentInstance());
        return result;
    }

    private Boolean validateInstanceNameUniquenessUponUpdate(Component containerComponent, ComponentInstance oldComponentInstance, String newInstanceName) {
        return ComponentValidations.validateNameIsUniqueInComponent(oldComponentInstance.getName(), newInstanceName, containerComponent);
    }

    private Either<ComponentInstance, StorageOperationStatus> getResourceInstanceById(Component containerComponent, String instanceId) {

        Either<ComponentInstance, StorageOperationStatus> result = null;
        List<ComponentInstance> instances = containerComponent.getComponentInstances();
        Optional<ComponentInstance> foundInstance = null;
        if (CollectionUtils.isEmpty(instances)) {
            result = Either.right(StorageOperationStatus.NOT_FOUND);
        }
        if (result == null) {
            foundInstance = instances.stream().filter(i -> i.getUniqueId().equals(instanceId)).findFirst();
            if (!foundInstance.isPresent()) {
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }
        if (result == null) {
            result = Either.left(foundInstance.get());
        }
        return result;
    }

    private ComponentInstance buildComponentInstance(ComponentInstance resourceInstanceForUpdate, ComponentInstance origInstanceForUpdate) {

        Long creationDate = origInstanceForUpdate.getCreationTime();

        Long modificationTime = System.currentTimeMillis();
        resourceInstanceForUpdate.setCreationTime(creationDate);
        resourceInstanceForUpdate.setModificationTime(modificationTime);

        resourceInstanceForUpdate.setCustomizationUUID(origInstanceForUpdate.getCustomizationUUID());

        if (StringUtils.isEmpty(resourceInstanceForUpdate.getName()) && StringUtils.isNotEmpty(origInstanceForUpdate.getName())) {
            resourceInstanceForUpdate.setName(origInstanceForUpdate.getName());
        }

        resourceInstanceForUpdate.setNormalizedName(ValidationUtils.normalizeComponentInstanceName(resourceInstanceForUpdate.getName()));

        if (StringUtils.isEmpty(resourceInstanceForUpdate.getIcon()))
            resourceInstanceForUpdate.setIcon(origInstanceForUpdate.getIcon());

        if (StringUtils.isEmpty(resourceInstanceForUpdate.getComponentVersion()))
            resourceInstanceForUpdate.setComponentVersion(origInstanceForUpdate.getComponentVersion());

        if (StringUtils.isEmpty(resourceInstanceForUpdate.getComponentName()))
            resourceInstanceForUpdate.setComponentName(origInstanceForUpdate.getComponentName());

        if (StringUtils.isEmpty(resourceInstanceForUpdate.getToscaComponentName()))
            resourceInstanceForUpdate.setToscaComponentName(origInstanceForUpdate.getToscaComponentName());

        if (resourceInstanceForUpdate.getOriginType() == null) {
            resourceInstanceForUpdate.setOriginType(origInstanceForUpdate.getOriginType());
        }
        if(resourceInstanceForUpdate.getOriginType()  == OriginTypeEnum.ServiceProxy)
            resourceInstanceForUpdate.setIsProxy(true);
        if (resourceInstanceForUpdate.getSourceModelInvariant() == null) {
            resourceInstanceForUpdate.setSourceModelInvariant(origInstanceForUpdate.getSourceModelInvariant());
        }
        if (resourceInstanceForUpdate.getSourceModelName() == null) {
            resourceInstanceForUpdate.setSourceModelName(origInstanceForUpdate.getSourceModelName());
        }
        if (resourceInstanceForUpdate.getSourceModelUuid() == null) {
            resourceInstanceForUpdate.setSourceModelUuid(origInstanceForUpdate.getSourceModelUuid());
        }
        if (resourceInstanceForUpdate.getSourceModelUid() == null) {
            resourceInstanceForUpdate.setSourceModelUid(origInstanceForUpdate.getSourceModelUid());
        }
        return resourceInstanceForUpdate;
    }
    /**
     * Returns list of ComponentInstanceProperty belonging to component instance capability specified by name, type and ownerId
     * @param containerComponentType
     * @param containerComponentId
     * @param componentInstanceUniqueId
     * @param capabilityType
     * @param capabilityName
     * @param userId
     * @param ownerId
     * @return
     */
    public Either<List<ComponentInstanceProperty>, ResponseFormat> getComponentInstanceCapabilityPropertiesById(String containerComponentType, String containerComponentId, String componentInstanceUniqueId, String capabilityType, String capabilityName, String ownerId, String userId) {

        Component containerComponent = null;

        Either<List<ComponentInstanceProperty>, ResponseFormat> resultOp = null;
        try {
            validateUserExists(userId, "Get Component Instance Properties By Id", false);
            if(resultOp == null){
                Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentType);
                if (validateComponentType.isRight()) {
                    resultOp = Either.right(validateComponentType.right().value());
                }
            }
            if(resultOp == null){
                Either<Component, StorageOperationStatus> validateContainerComponentExists = toscaOperationFacade.getToscaFullElement(containerComponentId);
                if (validateContainerComponentExists.isRight()) {
                    resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(validateContainerComponentExists.right().value())));
                } else {
                    containerComponent = validateContainerComponentExists.left().value();
                }
            }
            if(resultOp == null){
                Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, componentInstanceUniqueId);
                if (resourceInstanceStatus.isRight()) {
                    resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceUniqueId, containerComponentId));
                } else {
                    resultOp = findCapabilityOfInstance(containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, ownerId, resourceInstanceStatus.left().value().getCapabilities());
                }
            }
            return resultOp;
        } finally {
            unlockComponent(resultOp, containerComponent);
        }
    }

    private Either<List<ComponentInstanceProperty>, ResponseFormat> findCapabilityOfInstance( String componentId, String instanceId, String capabilityType, String capabilityName, String ownerId, Map<String, List<CapabilityDefinition>> instanceCapabilities) {
        Either<List<ComponentInstanceProperty>, ResponseFormat> result = null;
        CapabilityDefinition foundCapability;
        if (MapUtils.isNotEmpty(instanceCapabilities)) {
            List<CapabilityDefinition> capabilitiesPerType = instanceCapabilities.get(capabilityType);
            if (capabilitiesPerType != null) {
                Optional<CapabilityDefinition> capabilityOpt = capabilitiesPerType.stream().filter(c -> c.getName().equals(capabilityName) && c.getOwnerId().equals(ownerId)).findFirst();
                if (capabilityOpt.isPresent()) {
                    foundCapability = capabilityOpt.get();
                    result = Either.left(foundCapability.getProperties() == null ? new ArrayList<>() : foundCapability.getProperties());
                }
            }
        }
        if (result == null) {
            result = fetchComponentInstanceCapabilityProperties(componentId, instanceId, capabilityType, capabilityName, ownerId);
        }
        return result;
    }

    private Either<List<ComponentInstanceProperty>, ResponseFormat> fetchComponentInstanceCapabilityProperties(String componentId, String instanceId, String capabilityType, String capabilityName, String ownerId) {
        Either<List<ComponentInstanceProperty>, ResponseFormat> resultOp = null;
        try {
            Either<List<ComponentInstanceProperty>, StorageOperationStatus> getComponentInstanceCapabilityProperties = toscaOperationFacade.getComponentInstanceCapabilityProperties(componentId, instanceId, capabilityName, capabilityType, ownerId);
            if(getComponentInstanceCapabilityProperties != null) {
                if (getComponentInstanceCapabilityProperties.isRight()) {
                    resultOp = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getComponentInstanceCapabilityProperties.right().value()), capabilityType, instanceId, componentId));
                } else {
                    resultOp = Either.left(getComponentInstanceCapabilityProperties.left().value());
                }
            } else {
                resultOp = Either.left(new ArrayList<>());
            }
        } catch(Exception e){
            log.error("The exception {} occurred upon the component {} instance {} capability {} properties retrieving. ", componentId, instanceId, capabilityName, e);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return resultOp;
    }

    private ResponseFormat updateCapabilityPropertyOnContainerComponent(ComponentInstanceProperty property, String newValue, Component containerComponent, ComponentInstance foundResourceInstance,
                                                                        String capabilityType, String capabilityName, String ownerId) {
        String componentInstanceUniqueId = foundResourceInstance.getUniqueId();
        StringBuilder sb = new StringBuilder(componentInstanceUniqueId);
        sb.append(ModelConverter.CAP_PROP_DELIM).append(property.getOwnerId()).append(ModelConverter.CAP_PROP_DELIM).append(capabilityType).append(ModelConverter.CAP_PROP_DELIM).append(capabilityName);
        String capKey = sb.toString();

        Map<String, List<CapabilityDefinition>> capabilities = Optional.ofNullable(foundResourceInstance.getCapabilities())
                .orElse(Collections.emptyMap());
        List<CapabilityDefinition> capPerType = Optional.ofNullable(capabilities.get(capabilityType)).orElse(Collections.emptyList());
        Optional<CapabilityDefinition> cap = capPerType.stream().filter(c -> c.getName().equals(capabilityName) && c.getOwnerId().equals(ownerId)).findAny();
        if (cap.isPresent()) {
            List<ComponentInstanceProperty> capProperties = cap.get().getProperties();
            if (capProperties != null) {
                Optional<ComponentInstanceProperty> instanceProperty = capProperties.stream().filter(p -> p.getUniqueId().equals(property.getUniqueId())).findAny();
                StorageOperationStatus status;
                if (instanceProperty.isPresent()) {
                    instanceProperty.get().setValue(newValue);
                    List<String> path = new ArrayList<>();
                    path.add(componentInstanceUniqueId);
                    path.add(capKey);
                    instanceProperty.get().setPath(path);
                    status = toscaOperationFacade.updateComponentInstanceCapabiltyProperty(containerComponent, componentInstanceUniqueId, capKey, instanceProperty.get());
                    if (status != StorageOperationStatus.OK) {
                        ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
                        return componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, "");

                    }
                    foundResourceInstance.setCustomizationUUID(UUID.randomUUID().toString());
                }
            }
        }
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    public Either<List<ComponentInstanceProperty>, ResponseFormat> updateInstanceCapabilityProperties(ComponentTypeEnum componentTypeEnum, String containerComponentId, String componentInstanceUniqueId, String capabilityType, String capabilityName, String ownerId,
                                                                                                      List<ComponentInstanceProperty> properties, String userId) {
        Either<List<ComponentInstanceProperty>, ResponseFormat> resultOp = null;

        validateUserExists(userId, "update instance capability property", false);

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError("updateInstanceCapabilityProperty", INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
        }
        Either<Component, StorageOperationStatus> getResourceResult = toscaOperationFacade.getToscaFullElement(containerComponentId);

        if (getResourceResult.isRight()) {
            log.debug(FAILED_TO_RETRIEVE_COMPONENT_COMPONENT_ID, containerComponentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
        }
        Component containerComponent = getResourceResult.left().value();

        if (!ComponentValidationUtils.canWorkOnComponent(containerComponent, userId)) {
            log.info("Restricted operation for user: {sourcePropList} on component {}", userId, containerComponentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
        }
        Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, componentInstanceUniqueId);
        if (resourceInstanceStatus.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceUniqueId, containerComponentId));
        }
        ComponentInstance foundResourceInstance = resourceInstanceStatus.left().value();
        // lock resource
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(containerComponentId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug("Failed to lock component {}", containerComponentId);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
        }

        Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
        if (allDataTypes.isRight()) {
            TitanOperationStatus status = allDataTypes.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(status))));
        }

        try {
            for (ComponentInstanceProperty property : properties) {
                Either<String, ResponseFormat> newPropertyValueEither = updatePropertyObjectValue(property, false);
                newPropertyValueEither.bimap(updatedValue ->
                                updateCapabilityPropertyOnContainerComponent(property, updatedValue, containerComponent, foundResourceInstance, capabilityType, capabilityName, ownerId),
                        Either::right);
            }
            Either<Component, StorageOperationStatus> updateContainerRes = toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(containerComponent);

            if (updateContainerRes.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(updateContainerRes.right().value());
                resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
                return resultOp;
            }
            resultOp = Either.left(properties);
            return resultOp;

        } finally {
            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(containerComponentId, componentTypeEnum.getNodeType());
        }
    }

    public Either<List<ComponentInstanceProperty>, ResponseFormat> updateInstanceCapabilityProperties(ComponentTypeEnum componentTypeEnum, String containerComponentId, String componentInstanceUniqueId, String capabilityType, String capabilityName,
                                                                                                      List<ComponentInstanceProperty> properties, String userId) {
        Either<List<ComponentInstanceProperty>, ResponseFormat> resultOp = null;

        validateUserExists(userId, "update instance capability property", false);

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError("updateInstanceCapabilityProperty", INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
        }
        Either<Component, StorageOperationStatus> getResourceResult = toscaOperationFacade.getToscaFullElement(containerComponentId);

        if (getResourceResult.isRight()) {
            log.debug(FAILED_TO_RETRIEVE_COMPONENT_COMPONENT_ID, containerComponentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
        }
        Component containerComponent = getResourceResult.left().value();

        if (!ComponentValidationUtils.canWorkOnComponent(containerComponent, userId)) {
            log.info("Restricted operation for user: {} on component {}", userId, containerComponentId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
        }
        Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, componentInstanceUniqueId);
        if (resourceInstanceStatus.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, componentInstanceUniqueId, containerComponentId));
        }
        ComponentInstance foundResourceInstance = resourceInstanceStatus.left().value();
        // lock resource
        StorageOperationStatus lockStatus = graphLockOperation.lockComponent(containerComponentId, componentTypeEnum.getNodeType());
        if (lockStatus != StorageOperationStatus.OK) {
            log.debug("Failed to lock component {}", containerComponentId);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(lockStatus)));
        }

        try {
            for (ComponentInstanceProperty property : properties) {
                Either<String, ResponseFormat> newPropertyValueEither = updatePropertyObjectValue(property, false);
                newPropertyValueEither.bimap(updatedValue ->
                                updateCapabilityPropertyOnContainerComponent(property, updatedValue, containerComponent, foundResourceInstance, capabilityType, capabilityName),
                        Either::right);
            }
            Either<Component, StorageOperationStatus> updateContainerRes = toscaOperationFacade.updateComponentInstanceMetadataOfTopologyTemplate(containerComponent);

            if (updateContainerRes.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(updateContainerRes.right().value());
                resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
                return resultOp;
            }
            resultOp = Either.left(properties);
            return resultOp;

        } finally {
            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();
            } else {
                titanDao.commit();
            }
            // unlock resource
            graphLockOperation.unlockComponent(containerComponentId, componentTypeEnum.getNodeType());
        }
    }

    public Either<Map<String, ComponentInstance>, ResponseFormat> copyComponentInstance(ComponentInstance inputComponentInstance,
                                                                                        String containerComponentId,
                                                                                        String componentInstanceId,
                                                                                        String userId) {

        Map<String, ComponentInstance> resultMap = new HashMap<>();
        Either<Component, StorageOperationStatus> getOrigComponent = toscaOperationFacade.getToscaElement(containerComponentId);
        if (getOrigComponent.isRight()) {
            log.error("Failed to get the original component information");
            return Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.USER_DEFINED, FAILED_TO_COPY_COMP_INSTANCE_TO_CANVAS));
        }

        Component origComponent = getOrigComponent.left().value();

        Either<Boolean, ResponseFormat> lockComponent = lockComponent(origComponent, "copyComponentInstance");
        if (lockComponent.isRight()) {
            log.error("destComponentInstance's data is {}", origComponent.toString());
            return Either.right(lockComponent.right().value());
        }


        Either<ComponentInstance, ResponseFormat> actionResponse = null;
        try {
            actionResponse = createComponentInstance(
                    "services", containerComponentId, userId, inputComponentInstance, true, false);

            if (actionResponse.isRight()) {
                log.error(FAILED_TO_COPY_COMP_INSTANCE_TO_CANVAS);
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.USER_DEFINED, FAILED_TO_COPY_COMP_INSTANCE_TO_CANVAS));
            }

        } finally {

            // on failure of the create instance unlock the resource and rollback the transaction.
            if (null == actionResponse || actionResponse.isRight()) {
                titanDao.rollback();
            }
            unlockComponent(actionResponse, origComponent);
        }

        Either<String, ResponseFormat> resultOp = null;

        try {
            ComponentInstance destComponentInstance = actionResponse.left().value();
            log.debug("destComponentInstance's data is {}", destComponentInstance.toString());


            resultOp = deepCopyComponentInstance(
                    origComponent, containerComponentId, componentInstanceId, destComponentInstance, userId);

            if (resultOp.isRight()) {
                log.error("Failed to deep copy component instance");
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.USER_DEFINED, "Failed to deep copy the component instance to the canvas"));
            }
            resultMap.put("componentInstance", destComponentInstance);
            return Either.left(resultMap);
        } finally {

            if (resultOp == null || resultOp.isRight()) {
                titanDao.rollback();

            } else {
                titanDao.commit();
                log.debug("Success trasaction commit");
            }
            // unlock resource
            unlockComponent(resultOp, origComponent);
        }
    }

    private Either<String, ResponseFormat> deepCopyComponentInstance(
            Component sourceComponent, String containerComponentId, String sourceComponentInstanceId,
            ComponentInstance destComponentInstance, String userId) {

        Either<Component, StorageOperationStatus> getDestComponent = toscaOperationFacade.getToscaElement(containerComponentId);
        if (getDestComponent.isRight()) {
            log.error("Failed to get the dest component information");
            return Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.USER_DEFINED, "Failed to copy the component instance to the canvas"));
        }

        Component destComponent = getDestComponent.left().value();

        Either<String, ResponseFormat> copyComponentInstanceWithPropertiesAndInputs = copyComponentInstanceWithPropertiesAndInputs(
                sourceComponent, destComponent, sourceComponentInstanceId, destComponentInstance, userId);
        if (copyComponentInstanceWithPropertiesAndInputs.isRight()) {
            log.error("Failed to copy component instance with properties and inputs as part of deep copy");
            return Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.USER_DEFINED, "Failed to copy the component instance with properties and inputs as part of deep copy"));
        }

        Either<String, ResponseFormat> copyComponentInstanceWithAttributes = copyComponentInstanceWithAttributes(
                sourceComponent, destComponent, sourceComponentInstanceId, destComponentInstance, userId);
        if (copyComponentInstanceWithAttributes.isRight()) {
            log.error("Failed to copy component instance with attributes as part of deep copy");
            return Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.USER_DEFINED, "Failed to copy the component instance with attributes as part of deep copy"));
        }
        return Either.left(COPY_COMPONENT_INSTANCE_OK);
    }

    private Either<String, ResponseFormat> copyComponentInstanceWithPropertiesAndInputs(
            Component sourceComponent, Component destComponent, String sourceComponentInstanceId,
            ComponentInstance destComponentInstance, String userId) {
        log.debug("start to copy ComponentInstance with properties and inputs");

        List<ComponentInstanceProperty> sourcePropList = null;
        if (sourceComponent.getComponentInstancesProperties() != null
                && sourceComponent.getComponentInstancesProperties().get(sourceComponentInstanceId) != null) {
            sourcePropList = sourceComponent.getComponentInstancesProperties().get(sourceComponentInstanceId);
            log.debug("sourcePropList");
        }

        List<ComponentInstanceProperty> destPropList = null;
        String destComponentInstanceId = destComponentInstance.getUniqueId();
        log.debug("destComponentInstanceId: {}", destComponentInstance.getUniqueId());
        if (destComponent.getComponentInstancesProperties() != null
                && destComponent.getComponentInstancesProperties().get(destComponentInstanceId) != null) {
            destPropList = destComponent.getComponentInstancesProperties().get(destComponentInstanceId);
            log.debug("destPropList {}");
        }

        List<ComponentInstancePropInput> componentInstancePropInputList = new ArrayList<>();

        if (null != destPropList && null != sourcePropList) {
            log.debug("start to set property and attribute");
            for (ComponentInstanceProperty destProp : destPropList) {
                String destPropertyName = destProp.getName();
                for (ComponentInstanceProperty sourceProp : sourcePropList) {
                    if (!destPropertyName.equals(sourceProp.getName())) {
                        continue;
                    }
                    log.debug("now set property");
                    if (sourceProp.getGetInputValues() == null && !StringUtils.isEmpty(sourceProp.getValue())
                            && (destProp.getValue() == null || !destProp.getValue().equals(sourceProp.getValue()))) {
                        log.debug("Now starting to copy the property {} in value {}", destPropertyName, sourceProp.getValue());

                        destProp.setValue(sourceProp.getValue());
                        Either<String, ResponseFormat> updatePropertyValueEither = updateComponentInstanceProperty(
                                destComponent.getUniqueId(), destComponentInstanceId, destProp);
                        if (updatePropertyValueEither.isRight()) {
                            log.error("Failed to copy the property {}", destPropertyName);
                            return Either.right(componentsUtils.getResponseFormat(
                                    ActionStatus.INVALID_CONTENT_PARAM, "Failed to paste component instance to the canvas, property copy"));
                        }
                        break;
                    }

                    log.debug("Now start to update inputs");

                    if (sourceProp.getGetInputValues() != null) {
                        if (sourceProp.getGetInputValues().isEmpty()) {
                            log.debug("source property input values empty");
                            break;
                        }
                        log.debug("Now starting to copy the {} property", destPropertyName);

                        Either<String, ResponseFormat> getSourceInputDefaultValue = getInputListDefaultValue(
                                sourceComponent, sourceProp.getGetInputValues().get(0).getInputId());
                        if (getSourceInputDefaultValue.isRight()) {
                            return Either.right(getSourceInputDefaultValue.right().value());
                        }
                        componentInstancePropInputList.add(new ComponentInstancePropInput(destProp));
                    }
                }
            }
        }
        return Either.left(COPY_COMPONENT_INSTANCE_OK);
    }

    private Either<String, ResponseFormat> copyComponentInstanceWithAttributes(Component sourceComponent,
                                                                               Component destComponent,
                                                                               String sourceComponentInstanceId,
                                                                               ComponentInstance destComponentInstance,
                                                                               String userId) {
        String destComponentInstanceId = destComponentInstance.getUniqueId();

        log.info("start to copy component instance with attributes");

        List<ComponentInstanceProperty> sourceAttributeList = null;
        if (sourceComponent.getComponentInstancesAttributes() != null
                && sourceComponent.getComponentInstancesAttributes().get(sourceComponentInstanceId) != null) {
            sourceAttributeList = sourceComponent.getComponentInstancesAttributes().get(sourceComponentInstanceId);
            log.info("sourceAttributes {}");
        }

        List<ComponentInstanceProperty> destAttributeList = null;
        if (destComponent.getComponentInstancesAttributes() != null
                && destComponent.getComponentInstancesAttributes().get(destComponentInstanceId) != null) {
            destAttributeList = destComponent.getComponentInstancesAttributes().get(destComponentInstanceId);
            log.info("destAttributeList {}");
        }
        if (null != sourceAttributeList && null != destAttributeList) {
            log.info("set attribute");

            for (ComponentInstanceProperty sourceAttribute : sourceAttributeList) {
                String sourceAttributeName = sourceAttribute.getName();
                for (ComponentInstanceProperty destAttribute : destAttributeList) {
                    if (sourceAttributeName.equals(destAttribute.getName())) {
                        if (sourceAttribute.getValue() != null && !sourceAttribute.getValue().isEmpty()) {
                            log.debug("Start to copy the attribute exists {}", sourceAttributeName);

                            sourceAttribute.setUniqueId(
                                    UniqueIdBuilder.buildResourceInstanceUniuqeId(
                                            "attribute" , destComponentInstanceId.split("\\.")[1] , sourceAttributeName));

                            Either<ComponentInstanceProperty, ResponseFormat> updateAttributeValueEither =
                                    createOrUpdateAttributeValueForCopyPaste(ComponentTypeEnum.SERVICE,
                                            destComponent.getUniqueId(), destComponentInstanceId, sourceAttribute,
                                            userId);
                            if (updateAttributeValueEither.isRight()) {
                                log.error("Failed to copy the attribute");
                                return Either.right(componentsUtils
                                        .getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM,
                                                "Failed to paste component instance to the canvas, attribute copy"));
                            }
                            break;
                        }
                    }
                }
            }
        }

        return Either.left(COPY_COMPONENT_INSTANCE_OK);
    }

    private Either<ComponentInstanceProperty, ResponseFormat> createOrUpdateAttributeValueForCopyPaste(ComponentTypeEnum componentTypeEnum,
                                                                                                       String componentId,
                                                                                                       String resourceInstanceId,
                                                                                                       ComponentInstanceProperty attribute,
                                                                                                       String userId) {

        Either<ComponentInstanceProperty, ResponseFormat> resultOp = null;

        validateUserExists(userId, "Create or Update attribute value", false);

        if (componentTypeEnum == null) {
            BeEcompErrorManager.getInstance().logInvalidInputError(
                    "createOrUpdateAttributeValue", INVALID_COMPONENT_TYPE, ErrorSeverity.INFO);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
            return resultOp;
        }

        Either<Component, StorageOperationStatus> getResourceResult = toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll);

        if (getResourceResult.isRight()) {
            log.info("Failed to retrieve component id {}", componentId);
            resultOp = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            return resultOp;
        }

        Component containerComponent = getResourceResult.left().value();

        Either<ComponentInstance, StorageOperationStatus> resourceInstanceStatus = getResourceInstanceById(containerComponent, resourceInstanceId);

        if (resourceInstanceStatus.isRight()) {
            resultOp = Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.RESOURCE_INSTANCE_NOT_FOUND_ON_SERVICE, resourceInstanceId, componentId));
            return resultOp;
        }

        ComponentInstance foundResourceInstance = resourceInstanceStatus.left().value();


        String propertyType = attribute.getType();
        ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
        log.info("The type of attribute id{},is {} ", attribute.getUniqueId(), propertyType);

        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            SchemaDefinition def = attribute.getSchema();
            if (def == null) {
                log.info("Schema doesn't exists for attribute of type {}", type);
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                log.info("Attribute in Schema Definition inside attribute of type {} doesn't exist", type);
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
        }

        List<ComponentInstanceProperty> instanceAttributes = containerComponent.
                getComponentInstancesAttributes().get(resourceInstanceId);
        Optional<ComponentInstanceProperty> instanceAttribute =
                instanceAttributes.stream().filter(p -> p.getUniqueId().equals(attribute.getUniqueId())).findAny();
        StorageOperationStatus status;

        if (instanceAttribute.isPresent()) {
            log.info("updateComponentInstanceAttribute");
            status = toscaOperationFacade.updateComponentInstanceAttribute(containerComponent, foundResourceInstance.getUniqueId(), attribute);
        } else {
            log.info("addComponentInstanceAttribute");
            status = toscaOperationFacade.addComponentInstanceAttribute(containerComponent, foundResourceInstance.getUniqueId(), attribute);
        }
        if (status != StorageOperationStatus.OK) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
            resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
            return resultOp;
        }
        List<String> path = new ArrayList<>();
        path.add(foundResourceInstance.getUniqueId());
        attribute.setPath(path);

        foundResourceInstance.setCustomizationUUID(UUID.randomUUID().toString());
        Either<Component, StorageOperationStatus> updateContainerRes = toscaOperationFacade.
                updateComponentInstanceMetadataOfTopologyTemplate(containerComponent);

        if (updateContainerRes.isRight()) {
            ActionStatus actionStatus = componentsUtils.
                    convertFromStorageResponseForResourceInstanceProperty(updateContainerRes.right().value());
            resultOp = Either.right(componentsUtils.
                    getResponseFormatForResourceInstanceProperty(actionStatus, ""));
            return resultOp;
        }
        resultOp = Either.left(attribute);
        return resultOp;



    }

    private Either<String, ResponseFormat> updateComponentInstanceProperty(String containerComponentId,
                                                                           String componentInstanceId,
                                                                           ComponentInstanceProperty property) {
        Either<String, ResponseFormat> resultOp;
        Either<Component, StorageOperationStatus> getComponent = toscaOperationFacade.getToscaElement(containerComponentId);

        if (getComponent.isRight()) {
            log.error("Failed to get the component information");
            return Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(
                    ActionStatus.INVALID_CONTENT_PARAM, "Failed to get the component information"));
        }

        Component containerComponent = getComponent.left().value();

        StorageOperationStatus status = toscaOperationFacade.updateComponentInstanceProperty(
                containerComponent, componentInstanceId, property);
        if (status != StorageOperationStatus.OK) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status);
            resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
            return resultOp;
        }

        Either<Component, StorageOperationStatus> updateContainerRes = toscaOperationFacade.
                updateComponentInstanceMetadataOfTopologyTemplate(containerComponent);

        if (updateContainerRes.isRight()) {
            ActionStatus actionStatus = componentsUtils.
                    convertFromStorageResponseForResourceInstanceProperty(updateContainerRes.right().value());
            resultOp = Either.right(componentsUtils.getResponseFormatForResourceInstanceProperty(actionStatus, ""));
            return resultOp;
        }

        return Either.left("Update OK");
    }

    private Either<String, ResponseFormat> getInputListDefaultValue(Component component, String inputId) {
        List<InputDefinition> inputList = component.getInputs();
        for (InputDefinition input : inputList) {
            if (input.getUniqueId().equals(inputId)) {
                if (input.getDefaultValue() == null) {
                    log.debug("The input's default value is null");
                    return Either.left(null);
                }
                return Either.left(input.getDefaultValue());
            }
        }
        log.error("The input's default value with id {} is not found", inputId);
        return Either.right(componentsUtils.getResponseFormat(
                ActionStatus.USER_DEFINED, "Failed to paste component instance to the canvas"));
    }

    /**
     * Method to delete selected nodes and edges on composition page
     * @param containerComponentType
     * @param componentId
     * @param componentInstanceIdList
     * @param userId
     * @return
     */
    public Map<String, List<String>> batchDeleteComponentInstance(String containerComponentType,
                                                                  String componentId,
                                                                  List<String> componentInstanceIdList,
                                                                  String userId) {

        List<String> deleteErrorIds = new ArrayList<>();
        Map<String, List<String>> deleteErrorMap = new HashMap<>();
        Either<Component, ResponseFormat> validateResponse = validateUser(containerComponentType, componentId, userId);
        if (validateResponse.isRight()) {
            deleteErrorMap.put("deleteFailedIds", componentInstanceIdList);
            return deleteErrorMap;
        }
        Component containerComponent = validateResponse.left().value();

        Either<Boolean, ResponseFormat> lockComponent = lockComponent(
                containerComponent, "batchDeleteComponentInstance");
        if (lockComponent.isRight()) {
            log.error("Failed to lockComponent containerComponent");
            deleteErrorMap.put("deleteFailedIds", componentInstanceIdList);
            return deleteErrorMap;
        }

        try {
            for (String eachInstanceId : componentInstanceIdList) {
                Either<ComponentInstance, ResponseFormat> actionResponse = batchDeleteComponentInstance(
                        containerComponent, containerComponentType, componentId, eachInstanceId);
                log.debug("batchDeleteResourceInstances actionResponse is {}", actionResponse);
                if (actionResponse.isRight()) {
                    log.error("Failed to delete ComponentInstance [{}]", eachInstanceId);
                    deleteErrorIds.add(eachInstanceId);
                }
            }
            //sending the ids of the error nodes that were not deleted to UI
            deleteErrorMap.put("deleteFailedIds", deleteErrorIds);
            return deleteErrorMap;
        } finally {
            unlockComponent(validateResponse, containerComponent);
        }
    }

    private Either<Component, ResponseFormat> validateUser(String containerComponentParam,
                                                           String containerComponentId,
                                                           String userId) {
        validateUserExists(userId, "delete Component Instance", false);
        Either<ComponentTypeEnum, ResponseFormat> validateComponentType = validateComponentType(containerComponentParam);
        if (validateComponentType.isRight()) {
            log.error("ComponentType[{}] doesn't support", containerComponentParam);
            return Either.right(validateComponentType.right().value());
        }

        final ComponentTypeEnum containerComponentType = validateComponentType.left().value();
        Either<Component, ResponseFormat> validateComponentExists = validateComponentExists(
                containerComponentId, containerComponentType, null);
        if (validateComponentExists.isRight()) {
            log.error("Component Id[{}] doesn't exist", containerComponentId);
            return Either.right(validateComponentExists.right().value());
        }

        Component containerComponent = validateComponentExists.left().value();
        Either<Boolean, ResponseFormat> validateCanWorkOnComponent = validateCanWorkOnComponent(containerComponent, userId);
        if (validateCanWorkOnComponent.isRight()) {
            return Either.right(validateCanWorkOnComponent.right().value());
        }
        return Either.left(containerComponent);
    }

    private Either<ComponentInstance, ResponseFormat> batchDeleteComponentInstance(Component containerComponent,
                                                                                   String containerComponentType,
                                                                                   String containerComponentId,
                                                                                   String componentInstanceId) {

        Either<ComponentInstance, ResponseFormat> resultOp;
        final ComponentTypeEnum containerComponentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);

        resultOp = deleteComponentInstance(containerComponent, componentInstanceId, containerComponentTypeEnum);

        if (resultOp.isRight()) {
            log.error("Failed to deleteComponentInstance with instanceId[{}]", componentInstanceId);
            return Either.right(resultOp.right().value());
        }

        log.info("Successfully deleted instance with id {}", componentInstanceId);
        return Either.left(resultOp.left().value());
    }
}
