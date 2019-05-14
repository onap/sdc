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

package org.openecomp.sdc.be.datamodel.utils;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.validation.PolicyUtils;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.tosca.utils.NodeFilterConverter;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiComponentMetadata;
import org.openecomp.sdc.be.ui.model.UiResourceDataTransfer;
import org.openecomp.sdc.be.ui.model.UiResourceMetadata;
import org.openecomp.sdc.be.ui.model.UiServiceDataTransfer;
import org.openecomp.sdc.be.ui.model.UiServiceMetadata;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component("uiComponentDataConverter")
public class UiComponentDataConverter {

    private static final Logger log = Logger.getLogger(UiComponentDataConverter.class);
    public static final String INVALID_INPUT_GIVEN_TO_DATA_CONVERTER = "Invalid input given to data converter: {}";
    private final GroupTypeBusinessLogic groupTypeBusinessLogic;
    private final PolicyTypeBusinessLogic policyTypeBusinessLogic;

    public UiComponentDataConverter(GroupTypeBusinessLogic groupTypeBusinessLogic, PolicyTypeBusinessLogic policyTypeBusinessLogic) {
        this.groupTypeBusinessLogic = groupTypeBusinessLogic;
        this.policyTypeBusinessLogic = policyTypeBusinessLogic;
    }

    private void setUiTranferDataByFieldName(UiComponentDataTransfer dataTransfer, Component component, String fieldName) {
        ComponentFieldsEnum field = ComponentFieldsEnum.findByValue(fieldName);
        if (field == null) {
            log.error(INVALID_INPUT_GIVEN_TO_DATA_CONVERTER, fieldName);
            return;
        }
        switch (field) {
            case INPUTS:
                setInputs(dataTransfer, component);
                break;
            case COMPONENT_INSTANCE_RELATION:
                setComponentInstanceRelation(dataTransfer, component);
                break;
            case GROUPS:
                setGroups(dataTransfer, component);
                break;
            case NON_EXCLUDED_GROUPS:
                setNonExcludedGroups(dataTransfer, component);
                break;
            case COMPONENT_INSTANCES:
                setComponentInstances(dataTransfer, component);
                break;
            case COMPONENT_INSTANCES_PROPERTIES:
                setComponentInstanceProperties(dataTransfer, component);
                break;
            case CAPABILITIES:
                setCapabilities(dataTransfer, component);
                break;
            case POLICIES:
                setPolicies(dataTransfer, component);
                break;
            case NON_EXCLUDED_POLICIES:
                setNonExcludedPolicies(dataTransfer, component);
                break;
            case REQUIREMENTS:
                setRequirements(dataTransfer, component);
                break;
            case DEPLOYMENT_ARTIFACTS:
                setDeploymentArtifacts(dataTransfer, component);
                break;
            case TOSCA_ARTIFACTS:
                setToscaArtifacts(dataTransfer, component);
                break;
            case ARTIFACTS:
                setArtifacts(dataTransfer, component);
                break;
            case COMPONENT_INSTANCES_ATTRIBUTES:
                setComponentInstanceAttributes(dataTransfer, component);
                break;
            case COMPONENT_INSTANCE_INPUTS:
                setComponentInstanceInputs(dataTransfer, component);
                break;
            case NODE_FILTER:
                setNodeFilter(dataTransfer, component);
                break;
            case COMPONENT_INSTANCES_INTERFACES:
                setComponentInstanceInterfaces(dataTransfer, component);
                break;
            case PROPERTIES:
                setProperties(dataTransfer, component);
                break;
            case INTERFACES:
                setInterfaces(dataTransfer, component);
                break;
            default:
                break;
        }
    }

    private void setNodeFilter(UiComponentDataTransfer dataTransfer, Component component) {
        if(component.getNodeFilterComponents() == null) {
          dataTransfer.setNodeFilter(null);
        } else {
          dataTransfer.setNodeFilter(component.getNodeFilterComponents());
        }
    }

    private void setPolicies(UiComponentDataTransfer dataTransfer, Component component) {
        Map<String, PolicyDefinition> policies = component.getPolicies();
        Set<PolicyDefinition> policyDefinitions =
                MapUtils.isEmpty(policies) ? new HashSet<>() : new HashSet<>(policies.values());

        policyDefinitions.addAll(getDeclaredPolicies(component.getComponentInstancesProperties()));

        dataTransfer.setPolicies(new ArrayList<>(policyDefinitions));
    }

    private Set<PolicyDefinition> getDeclaredPolicies(Map<String, List<ComponentInstanceProperty>> componentInstanceProperties) {
        if(MapUtils.isEmpty(componentInstanceProperties)) {
            return new HashSet<>();
        }

        Set<PolicyDefinition> declaredPolicies = new HashSet<>();
        for(Map.Entry<String, List<ComponentInstanceProperty>> instancePropertyEntry : componentInstanceProperties.entrySet()) {
            declaredPolicies.addAll(instancePropertyEntry.getValue().stream()
                    .filter(property -> CollectionUtils.isNotEmpty(property.getGetPolicyValues()))
                    .map(instanceProperty -> PolicyUtils.getDeclaredPolicyDefinition(instancePropertyEntry.getKey(), instanceProperty))
                    .collect(Collectors.toSet()));
        }

        return declaredPolicies;
    }


    private void setComponentInstanceRelation(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getComponentInstancesRelations() == null) {
            dataTransfer.setComponentInstancesRelations(new ArrayList<>());
        } else {
            dataTransfer.setComponentInstancesRelations(component.getComponentInstancesRelations());
        }
    }

    private void setInputs(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getInputs() == null) {
            dataTransfer.setInputs(new ArrayList<>());
        } else {
            dataTransfer.setInputs(component.getInputs());
        }
    }

    private void setComponentInstanceInputs(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getComponentInstancesInputs() == null) {
            dataTransfer.setComponentInstancesInputs(new HashMap<>());
        } else {
            dataTransfer.setComponentInstancesInputs(component.getComponentInstancesInputs());
        }
    }
    private void setComponentInstanceAttributes(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getComponentInstancesAttributes() == null) {
            dataTransfer.setComponentInstancesAttributes(new HashMap<>());
        } else {
            dataTransfer.setComponentInstancesAttributes(component.getComponentInstancesAttributes());
        }
    }
    private void setArtifacts(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getArtifacts() == null) {
            dataTransfer.setArtifacts(new HashMap<>());
        } else {
            dataTransfer.setArtifacts(component.getArtifacts());
        }
    }
    private void setToscaArtifacts(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getToscaArtifacts() == null) {
            dataTransfer.setToscaArtifacts(new HashMap<>());
        } else {
            dataTransfer.setToscaArtifacts(component.getToscaArtifacts());
        }
    }

    private void setDeploymentArtifacts(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getDeploymentArtifacts() == null) {
            dataTransfer.setDeploymentArtifacts(new HashMap<>());
        } else {
            dataTransfer.setDeploymentArtifacts(component.getDeploymentArtifacts());
        }
    }

    private void setRequirements(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getRequirements() == null) {
            dataTransfer.setRequirements(new HashMap<>());
        } else {
            dataTransfer.setRequirements(component.getRequirements());
        }
    }

    private void setCapabilities(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getCapabilities() == null) {
            dataTransfer.setCapabilities(new HashMap<>());
        } else {
            dataTransfer.setCapabilities(getFilteredCapabilities(component));
        }
    }

    private Map<String,List<CapabilityDefinition>> getFilteredCapabilities(Component component) {
        if(component.getComponentType() != ComponentTypeEnum.SERVICE){
            return component.getCapabilities().values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(c -> c.getOwnerType() != CapabilityDataDefinition.OwnerType.GROUP)
                    .collect(groupingBy(CapabilityDefinition::getType, toList()));
        }
        return component.getCapabilities();
    }

    private void setComponentInstanceProperties(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getComponentInstancesProperties() == null) {
            dataTransfer.setComponentInstancesProperties(new HashMap<>());
        } else {
            dataTransfer.setComponentInstancesProperties(component.getComponentInstancesProperties());
        }
    }

    private void setComponentInstances(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getComponentInstances() == null) {
            dataTransfer.setComponentInstances(new ArrayList<>());
        } else {
            dataTransfer.setComponentInstances(component.getComponentInstances());
        }
    }

    private void setGroups(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getGroups() == null) {
            dataTransfer.setGroups(new ArrayList<>());
        } else {
            dataTransfer.setGroups(component.getGroups());
        }
    }

    private void setProperties(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getProperties() == null) {
            dataTransfer.setProperties(new ArrayList<>());
        } else {
            dataTransfer.setProperties(component.getProperties());
        }
    }

    private void setInterfaces(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getInterfaces() == null) {
            dataTransfer.setInterfaces(new HashMap<>());
        } else {
            dataTransfer.setInterfaces(component.getInterfaces());
        }
    }

    private void setComponentInstanceInterfaces(UiComponentDataTransfer dataTransfer, Component component) {
        if (component.getComponentInstancesInterfaces() == null) {
            dataTransfer.setComponentInstancesInterfaces(new HashMap<>());
        } else {
            dataTransfer.setComponentInstancesInterfaces(component.getComponentInstancesInterfaces());
        }
    }

    private void setNonExcludedGroups(UiComponentDataTransfer dataTransfer, Component component) {
        List<GroupDefinition> groups = component.getGroups();
        if (groups == null) {
            dataTransfer.setGroups(new ArrayList<>());
        } else {
            Set<String> nonExcludedGroupTypes = groupTypeBusinessLogic.getExcludedGroupTypes(component.getActualComponentType());
            List<GroupDefinition> nonExcludedGroups = groups.stream()
                    .filter(gd -> !nonExcludedGroupTypes.contains(gd.getType()))
                    .collect(toList());
            dataTransfer.setGroups(nonExcludedGroups);
        }
    }

    private void setNonExcludedPolicies(UiComponentDataTransfer dataTransfer, Component component) {
        List<PolicyDefinition> policyDefinitions = component.resolvePoliciesList();
        Set<String> nonExcludedPolicyTypes = policyTypeBusinessLogic.getExcludedPolicyTypes(component.getActualComponentType());
        List<PolicyDefinition> nonExcludedPolicies = policyDefinitions.stream()
                .filter(pd -> !nonExcludedPolicyTypes.contains(pd.getPolicyTypeName()))
                .collect(toList());
        dataTransfer.setPolicies(nonExcludedPolicies);
    }

    public UiComponentDataTransfer getUiDataTransferFromResourceByParams(Resource resource, List<String> paramsToReturn) {
        UiResourceDataTransfer dataTransfer = new UiResourceDataTransfer();

        for (String fieldName : paramsToReturn) {

            ComponentFieldsEnum field = ComponentFieldsEnum.findByValue(fieldName);
            if (field == null) {
                log.error(INVALID_INPUT_GIVEN_TO_DATA_CONVERTER, fieldName);
                continue;
            }
            switch (field) {
                case DERIVED_FROM:
                    setDerivedFrom(resource, dataTransfer);
                    break;

                case ATTRIBUTES:
                    setAttributes(resource, dataTransfer);
                    break;

                case ADDITIONAL_INFORMATION:
                    setAdditionalInfo(resource, dataTransfer);
                    break;
                case METADATA:
                    UiResourceMetadata metadata = new UiResourceMetadata(resource.getCategories(), resource.getDerivedFrom(), (ResourceMetadataDataDefinition) resource.getComponentMetadataDefinition().getMetadataDataDefinition());
                    dataTransfer.setMetadata(metadata);
                    break;

                default:
                    setUiTranferDataByFieldName(dataTransfer, resource, fieldName);
            }
        }

        return dataTransfer;
    }

    private void setDerivedFrom(Resource resource, UiResourceDataTransfer dataTransfer) {
        if (resource.getDerivedFrom() == null) {
            dataTransfer.setDerivedFrom(new ArrayList<>());
        } else {
            dataTransfer.setDerivedFrom(resource.getDerivedFrom());
        }
    }

    private void setAttributes(Resource resource, UiResourceDataTransfer dataTransfer) {
        if (resource.getAttributes() == null) {
            dataTransfer.setAttributes(new ArrayList<>());
        } else {
            dataTransfer.setAttributes(resource.getAttributes());
        }
    }

    private void setAdditionalInfo(Resource resource, UiResourceDataTransfer dataTransfer) {
        if (resource.getAdditionalInformation() == null) {
            dataTransfer.setAdditionalInformation(new ArrayList<>());
        } else {
            dataTransfer.setAdditionalInformation(resource.getAdditionalInformation());
        }
    }

    public UiComponentDataTransfer getUiDataTransferFromServiceByParams(Service service, List<String> paramsToReturn) {
        UiServiceDataTransfer dataTransfer = new UiServiceDataTransfer();
        for (String fieldName : paramsToReturn) {
            ComponentFieldsEnum field = ComponentFieldsEnum.findByValue(fieldName);
            if (field == null) {
                log.error(INVALID_INPUT_GIVEN_TO_DATA_CONVERTER, fieldName);
                continue;
            }
            switch (field) {
                case SERVICE_API_ARTIFACTS:
                    setServiceApiArtifacts(service, dataTransfer);
                    break;
                case FORWARDING_PATHS:
                    setForwardingPaths(service, dataTransfer);
                    break;
                case METADATA:
                    UiServiceMetadata metadata = new UiServiceMetadata(service.getCategories(), (ServiceMetadataDataDefinition) service.getComponentMetadataDefinition().getMetadataDataDefinition());
                    dataTransfer.setMetadata(metadata);
                    break;
                case NODE_FILTER:
                    if(service.getNodeFilterComponents() == null) {
                        dataTransfer.setNodeFilterData(null);
                    } else {
                        NodeFilterConverter nodeFilterConverter = new NodeFilterConverter();
                        dataTransfer.setNodeFilterData(nodeFilterConverter.convertDataMapToUI(service.getNodeFilterComponents()));
                    }

                    break;
                default:
                    setUiTranferDataByFieldName(dataTransfer, service, fieldName);
            }
        }
        return dataTransfer;
    }

    private void setServiceApiArtifacts(Service service, UiServiceDataTransfer dataTransfer) {
        if (service.getServiceApiArtifacts() == null) {
            dataTransfer.setServiceApiArtifacts(new org.openecomp.sdc.be.ui.model.SerializedHashMap<>());
        } else {
            dataTransfer.setServiceApiArtifacts(service.getServiceApiArtifacts());
        }
    }

    private void setForwardingPaths(Service service, UiServiceDataTransfer dataTransfer) {
        if (service.getForwardingPaths() == null) {
            dataTransfer.setForwardingPaths(new org.openecomp.sdc.be.ui.model.SerializedHashMap<>());
        } else {
            dataTransfer.setForwardingPaths(service.getForwardingPaths());
        }
    }

    public static UiComponentMetadata convertToUiComponentMetadata(Component component) {

        UiComponentMetadata uiComponentMetadata = null;
        switch (component.getComponentType()) {
            case RESOURCE:
                Resource resource = (Resource) component;
                uiComponentMetadata = new UiResourceMetadata(component.getCategories(), resource.getDerivedFrom(), (ResourceMetadataDataDefinition) resource.getComponentMetadataDefinition().getMetadataDataDefinition());
                break;
            case SERVICE:
                uiComponentMetadata = new UiServiceMetadata(component.getCategories(), (ServiceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition());
                break;
            default:
                break;
        }
        return uiComponentMetadata;
    }
}
