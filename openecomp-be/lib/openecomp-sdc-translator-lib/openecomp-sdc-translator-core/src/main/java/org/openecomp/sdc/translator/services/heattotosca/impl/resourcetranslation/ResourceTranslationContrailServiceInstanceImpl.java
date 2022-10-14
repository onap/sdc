/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import static org.openecomp.sdc.tosca.services.ToscaConstants.MANDATORY_PROPERTY_NAME;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_INVALID_NETWORK_CONNECTION;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_MISSING_VIRTUAL_NETWORK_INTERFACE_LIST;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_MULTIPLE_SERVICE_INSTANCE_DIFF_INTERFACES;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_SERVICE_TEMPLATE_PROPERTY_GET_RESOURCE;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_SERVICE_TEMPLATE_PROPERTY_INVALID_TYPE;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_SERVICE_TEMPLATE_PROPERTY_UNSUPPORTED_RESOURCE;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_NETWORK_RESOURCE_CONNECTION;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.GroupDefinition;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.AsdPackageHelper;
import org.openecomp.sdc.tosca.csar.ManifestUtils;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.ContrailServiceInstanceTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailTranslationHelper;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

public class ResourceTranslationContrailServiceInstanceImpl extends ResourceTranslationBase {

    private static final String INTERFACE_LIST = "interface_list";
    protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationContrailServiceInstanceImpl.class);

    @Override
    public void translate(TranslateTo translateTo) {
        Resource serviceInstanceResource = translateTo.getResource();
        AttachedResourceId contrailServiceTemplateAttached = getServiceTemplateAttachedId(translateTo, serviceInstanceResource);
        if (contrailServiceTemplateAttached.isGetResource()) {
            translateContrailServiceInstance(translateTo, serviceInstanceResource, contrailServiceTemplateAttached);
        } else {
            logger.warn(LOG_SERVICE_TEMPLATE_PROPERTY_GET_RESOURCE, translateTo.getResourceId(), translateTo.getResource().getType());
        }
    }

    private void translateContrailServiceInstance(TranslateTo translateTo, Resource serviceInstanceResource,
                                                  AttachedResourceId contrailServiceTemplateAttached) {
        String contrailServiceTemplateResourceId = (String) contrailServiceTemplateAttached.getEntityId();
        Resource contrailServiceTemplateResource = HeatToToscaUtil
            .getResource(translateTo.getHeatOrchestrationTemplate(), contrailServiceTemplateResourceId, translateTo.getHeatFileName());
        if (!contrailServiceTemplateResource.getType().equals(HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource())) {
            logger.warn(LOG_SERVICE_TEMPLATE_PROPERTY_INVALID_TYPE, translateTo.getResourceId(), translateTo.getResource().getType(),
                contrailServiceTemplateResourceId, contrailServiceTemplateResource.getType(),
                HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource());
            return;
        }
        Optional<String> contrailServiceTemplateTranslatedId = ResourceTranslationFactory.getInstance(contrailServiceTemplateResource)
            .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(), translateTo.getHeatOrchestrationTemplate(),
                contrailServiceTemplateResource, contrailServiceTemplateResourceId, translateTo.getContext());
        if (!contrailServiceTemplateTranslatedId.isPresent()) {
            logger.warn(LOG_SERVICE_TEMPLATE_PROPERTY_UNSUPPORTED_RESOURCE, translateTo.getResourceId(), translateTo.getResource().getType(),
                contrailServiceTemplateResourceId, contrailServiceTemplateResource.getType());
            return;
        }
        ServiceTemplate globalSubstitutionServiceTemplate = translateTo.getContext().getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        String contrailStId = ResourceTranslationContrailServiceTemplateImpl
            .getContrailSubstitutedNodeTypeId(contrailServiceTemplateTranslatedId.get());
        NodeType substitutedNodeType = DataModelUtil.getNodeType(globalSubstitutionServiceTemplate, contrailStId);
        int numberOfPorts = getServiceInstanceNumberOfPorts(serviceInstanceResource);
        if (substitutedNodeType.getRequirements() != null && substitutedNodeType.getRequirements().size() != numberOfPorts) {
            logger.warn(LOG_MULTIPLE_SERVICE_INSTANCE_DIFF_INTERFACES, contrailServiceTemplateResourceId);
            return;
        }
        addNetworkLinkRequirements(substitutedNodeType, numberOfPorts);
        NodeTemplate substitutedNodeTemplate = createSubstitutedNodeTemplate(translateTo, contrailServiceTemplateResource, contrailStId,
            numberOfPorts);
        String computeNodeTypeId = new ContrailTranslationHelper()
            .getComputeNodeTypeId(contrailServiceTemplateResource, contrailServiceTemplateResourceId, contrailServiceTemplateTranslatedId.get(),
                translateTo.getContext());
        boolean orderedInterfaces = getOrderedInterfaces(contrailServiceTemplateResource);
        ServiceTemplate nestedServiceTemplate = createNestedServiceTemplate(translateTo, computeNodeTypeId, contrailStId, substitutedNodeTemplate,
            orderedInterfaces);
        addAbstractSubstitutionProperty(translateTo, substitutedNodeTemplate.getProperties(), nestedServiceTemplate, contrailServiceTemplateResource);
        translateTo.getContext().getTranslatedServiceTemplates().put(new ContrailTranslationHelper()
            .getSubstitutionContrailServiceTemplateMetadata(translateTo.getHeatFileName(), translateTo.getTranslatedId()), nestedServiceTemplate);
    }

    private void addAbstractSubstitutionProperty(TranslateTo translateTo, Map<String, Object> substitutionProperties,
                                                 ServiceTemplate nestedServiceTemplate, Resource contrailServiceTemplateResource) {
        Map<String, Object> innerProps = new HashMap<>();
        innerProps.put(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME, ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate));
        Object countValue = handleScaleOutProperty(translateTo, innerProps);
        handleServiceScalingProperty(translateTo, innerProps, contrailServiceTemplateResource);
        boolean mandatory = false;
        if (countValue instanceof Integer && (Integer) countValue > 0) {
            mandatory = true;
        }
        if (countValue == null) {
            mandatory = true;
        }
        innerProps.put(MANDATORY_PROPERTY_NAME, mandatory);
        substitutionProperties.put(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, innerProps);
    }

    private Object handleScaleOutProperty(TranslateTo translateTo, Map<String, Object> innerProps) {
        Object scaleOutPropertyValue = translateTo.getResource().getProperties().get(HeatConstants.SCALE_OUT_PROPERTY_NAME);
        Object countValue = null;
        if (scaleOutPropertyValue instanceof Map) {
            countValue = TranslatorHeatToToscaPropertyConverter
                .getToscaPropertyValue(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), Constants.MAX_INSTANCES_PROPERTY_NAME,
                    ((Map) scaleOutPropertyValue).get(Constants.MAX_INSTANCES_PROPERTY_NAME), null, translateTo.getHeatFileName(),
                    translateTo.getHeatOrchestrationTemplate(), null, translateTo.getContext());
            if (countValue != null) {
                innerProps.put(ToscaConstants.COUNT_PROPERTY_NAME, countValue);
            } else {
                innerProps.put(ToscaConstants.COUNT_PROPERTY_NAME, 1);
            }
        } else {
            innerProps.put(ToscaConstants.COUNT_PROPERTY_NAME, 1);
        }
        return countValue;
    }

    private void handleServiceScalingProperty(TranslateTo translateTo, Map<String, Object> innerProps, Resource contrailServiceTemplateResource) {
        Object serviceScalingPropertyValue = contrailServiceTemplateResource.getProperties().get(HeatConstants.SERVICE_SCALING_PROPERTY_NAME);
        Object serviceScalingValue;
        if (serviceScalingPropertyValue != null) {
            serviceScalingValue = TranslatorHeatToToscaPropertyConverter
                .getToscaPropertyValue(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), HeatConstants.SERVICE_SCALING_PROPERTY_NAME,
                    serviceScalingPropertyValue, null, translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), null,
                    translateTo.getContext());
            if (serviceScalingValue != null) {
                innerProps.put(ToscaConstants.SCALING_ENABLED_PROPERTY_NAME,
                    (HeatBoolean.isValueBoolean(serviceScalingValue)) ? HeatBoolean.eval(serviceScalingValue) : serviceScalingValue);
            }
        }
    }

    private boolean getOrderedInterfaces(Resource contrailServiceTemplate) {
        Object orderedInterfaces = contrailServiceTemplate.getProperties().get("ordered_interfaces");
        if (orderedInterfaces == null) {
            return false;
        }
        if (orderedInterfaces instanceof String) {
            return HeatBoolean.eval(orderedInterfaces);
        }
        //if get_param, set default value to true
        return true;
    }

    private ServiceTemplate createNestedServiceTemplate(TranslateTo translateTo, String computeNodeTypeId, String substitutedNodeTypeId,
                                                        NodeTemplate substitutedNodeTemplate, boolean orderedInterfaces) {
        ServiceTemplate nestedSubstitutionServiceTemplate = new ServiceTemplate();
        setNestedServiceTemplateGeneralDetails(translateTo, nestedSubstitutionServiceTemplate);
        String heatStackGroupKey = addHeatStackGroup(translateTo, nestedSubstitutionServiceTemplate);
        addSubstitutionMappingEntry(nestedSubstitutionServiceTemplate, substitutedNodeTypeId);
        handleInputParameters(nestedSubstitutionServiceTemplate, translateTo);
        String computeNodeTemplateId = handleComputeNodeTemplate(translateTo, computeNodeTypeId, nestedSubstitutionServiceTemplate,
            heatStackGroupKey);
        handleOutputParameters(nestedSubstitutionServiceTemplate, computeNodeTemplateId, translateTo);
        handleServiceInstanceInterfaces(translateTo,
            new ContrailServiceInstanceTo(nestedSubstitutionServiceTemplate, substitutedNodeTemplate, heatStackGroupKey, orderedInterfaces,
                computeNodeTemplateId));
        return nestedSubstitutionServiceTemplate;
    }

    private void handleOutputParameters(ServiceTemplate nestedSubstitutionServiceTemplate, String nodeTemplateId, TranslateTo translateTo) {
        if (nodeTemplateId == null) {
            return;
        }
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        Optional<NodeType> contrailAbstractNodeType = toscaAnalyzerService
            .fetchNodeType(ToscaNodeType.CONTRAIL_ABSTRACT_SUBSTITUTE, translateTo.getContext().getGlobalServiceTemplates().values());
        if (!contrailAbstractNodeType.isPresent()) {
            return;
        }
        Map<String, AttributeDefinition> contrailAbstractAttributes = contrailAbstractNodeType.get().getAttributes();
        Map<String, ParameterDefinition> nestedSubstitutionServiceTemplateOutputs = new HashMap<>();
        if (contrailAbstractAttributes == null) {
            return;
        }
        for (Map.Entry<String, AttributeDefinition> attributeDefinitionEntry : contrailAbstractAttributes.entrySet()) {
            AttributeDefinition abstractAttributeDef = attributeDefinitionEntry.getValue();
            if (abstractAttributeDef != null) {
                Map<String, List> outputValue = new HashMap<>();
                List<String> outputGetAttributeList = new ArrayList<>();
                outputGetAttributeList.add(nodeTemplateId);
                outputGetAttributeList.add(attributeDefinitionEntry.getKey());
                outputValue.put(ToscaFunctions.GET_ATTRIBUTE.getFunctionName(), outputGetAttributeList);
                nestedSubstitutionServiceTemplateOutputs
                    .put(attributeDefinitionEntry.getKey(), DataModelUtil.convertAttributeDefToParameterDef(abstractAttributeDef, outputValue));
            }
        }
        if (!nestedSubstitutionServiceTemplateOutputs.isEmpty()) {
            nestedSubstitutionServiceTemplate.getTopology_template().setOutputs(nestedSubstitutionServiceTemplateOutputs);
        }
    }

    private void handleServiceInstanceInterfaces(TranslateTo translateTo, ContrailServiceInstanceTo contrailServiceInstanceTo) {
        Resource serviceInstanceResource = translateTo.getResource();
        Object interfaceListProperty = serviceInstanceResource.getProperties().get(HeatConstants.INTERFACE_LIST_PROPERTY_NAME);
        if (interfaceListProperty == null) {
            return;
        }
        if (interfaceListProperty instanceof List) {
            for (int index = 0; index < ((List) interfaceListProperty).size(); index++) {
                Object interfaceEntry = ((List) interfaceListProperty).get(index);
                handleInterface(translateTo, interfaceEntry, index, contrailServiceInstanceTo);
            }
        } else if (interfaceListProperty instanceof Map) {
            handleInterface(translateTo, interfaceListProperty, null, contrailServiceInstanceTo);
        }
    }

    private void handleInterface(TranslateTo translateTo, Object interfacePropertyValue, Integer index,
                                 ContrailServiceInstanceTo contrailServiceInstanceTo) {
        if (index == null) {
            index = 0;
        }
        NodeTemplate portNodeTemplate = createPortNodeTemplate(index, contrailServiceInstanceTo.isOrderedInterfaces(),
            contrailServiceInstanceTo.getComputeNodeTemplateId());
        String portNodeTemplateId = Constants.SERVICE_INSTANCE_PORT_PREFIX + index;
        String portReqMappingKey = Constants.SERVICE_INSTANCE_LINK_PREFIX + portNodeTemplateId;
        DataModelUtil.addNodeTemplate(contrailServiceInstanceTo.getNestedSubstitutionServiceTemplate(), portNodeTemplateId, portNodeTemplate);
        updateSubstitutionMappingRequirement(contrailServiceInstanceTo.getNestedSubstitutionServiceTemplate(), portReqMappingKey, portNodeTemplateId);
        updateHeatStackGroup(contrailServiceInstanceTo.getNestedSubstitutionServiceTemplate(), contrailServiceInstanceTo.getHeatStackGroupKey(),
            portNodeTemplateId);
        connectPortToNetwork(translateTo, interfacePropertyValue, contrailServiceInstanceTo.getSubstitutedNodeTemplate(), portReqMappingKey);
    }

    private void connectPortToNetwork(TranslateTo translateTo, Object interfacePropertyValue, NodeTemplate substitutedNodeTemplate,
                                      String portReqMappingKey) {
        List<String> validNetworksForConnections = ImmutableList.of(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());
        if (!(interfacePropertyValue instanceof Map)) {
            return;
        }
        Object virtualNetworkValue = ((Map) interfacePropertyValue).get(HeatConstants.VIRTUAL_NETWORK_PROPERTY_NAME);
        if (virtualNetworkValue == null) {
            logger.warn(LOG_MISSING_VIRTUAL_NETWORK_INTERFACE_LIST, translateTo.getResourceId(), translateTo.getResource().getType());
            return;
        }
        Optional<AttachedResourceId> networkAttachedResourceId = HeatToToscaUtil
            .extractAttachedResourceId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
                virtualNetworkValue);
        if (!networkAttachedResourceId.isPresent()) {
            return;
        }
        Optional<String> networkResourceId = HeatToToscaUtil.getContrailAttachedHeatResourceId(networkAttachedResourceId.get());
        if (networkResourceId.isPresent()) {
            Resource networkResource = HeatToToscaUtil
                .getResource(translateTo.getHeatOrchestrationTemplate(), networkResourceId.get(), translateTo.getHeatFileName());
            if (validNetworksForConnections.contains(networkResource.getType())) {
                Optional<String> networkTranslatedId = getResourceTranslatedId(translateTo.getHeatFileName(),
                    translateTo.getHeatOrchestrationTemplate(), networkResourceId.get(), translateTo.getContext());
                networkTranslatedId
                    .ifPresent(translatedId -> addLinkToNetworkRequirementAssignment(substitutedNodeTemplate, translatedId, portReqMappingKey));
            } else {
                logger.warn(LOG_UNSUPPORTED_NETWORK_RESOURCE_CONNECTION, translateTo.getResourceId(), translateTo.getResource().getType());
            }
        } else if (networkAttachedResourceId.get().isGetParam() && networkAttachedResourceId.get().getEntityId() instanceof String) {
            TranslatedHeatResource translatedSharedResourceId = translateTo.getContext().getHeatSharedResourcesByParam()
                .get(networkAttachedResourceId.get().getEntityId());
            if (Objects.nonNull(translatedSharedResourceId) && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
                addLinkToNetworkRequirementAssignment(substitutedNodeTemplate, translatedSharedResourceId.getTranslatedId(), portReqMappingKey);
            }
        } else {
            logger.warn(LOG_INVALID_NETWORK_CONNECTION, translateTo.getResourceId(), translateTo.getResource().getType(),
                virtualNetworkValue.toString());
        }
    }

    private NodeTemplate createPortNodeTemplate(Integer index, boolean orderedInterfaces, String computeNodeTemplateId) {
        NodeTemplate portNodeTemplate = new NodeTemplate();
        portNodeTemplate.setType(ToscaNodeType.CONTRAIL_PORT);
        Map<String, Object> portProperties = new HashMap<>();
        portProperties.put("static_routes", DataModelUtil.createGetInputPropertyValueFromListParameter(INTERFACE_LIST, index, "static_routes"));
        portProperties.put("virtual_network", DataModelUtil.createGetInputPropertyValueFromListParameter(INTERFACE_LIST, index, "virtual_network"));
        portProperties
            .put("allowed_address_pairs", DataModelUtil.createGetInputPropertyValueFromListParameter(INTERFACE_LIST, index, "allowed_address_pairs"));
        portProperties.put("ip_address", DataModelUtil.createGetInputPropertyValueFromListParameter(INTERFACE_LIST, index, "ip_address"));
        portProperties.put("static_route", DataModelUtil.createGetInputPropertyValueFromListParameter("static_routes_list", index));
        portProperties.put("shared_ip", DataModelUtil.createGetInputPropertyValueFromListParameter("shared_ip_list", index));
        portProperties.put("interface_type", DataModelUtil.createGetInputPropertyValueFromListParameter("service_interface_type_list", index));
        if (orderedInterfaces) {
            portProperties.put("order", index);
        }
        portNodeTemplate.setProperties(portProperties);
        DataModelUtil.addBindingReqFromPortToCompute(computeNodeTemplateId, portNodeTemplate);
        return portNodeTemplate;
    }

    private void addLinkToNetworkRequirementAssignment(NodeTemplate nodeTemplate, String connectedNodeTranslatedId, String requirementId) {
        if (nodeTemplate == null || connectedNodeTranslatedId == null) {
            return;
        }
        RequirementAssignment requirement = new RequirementAssignment();
        requirement.setCapability(ToscaCapabilityType.NATIVE_NETWORK_LINKABLE);
        requirement.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_LINK_TO);
        requirement.setNode(connectedNodeTranslatedId);
        DataModelUtil.addRequirementAssignment(nodeTemplate, requirementId, requirement);
    }

    private void updateHeatStackGroup(ServiceTemplate serviceTemplate, String heatStackGroupKey, String memberId) {
        serviceTemplate.getTopology_template().getGroups().get(heatStackGroupKey).getMembers().add(memberId);
    }

    private void updateSubstitutionMappingRequirement(ServiceTemplate serviceTemplate, String portReqMappingKey, String portNodeTemplateId) {
        List<String> portReqMappingValue = new ArrayList<>();
        portReqMappingValue.add(portNodeTemplateId);
        portReqMappingValue.add(ToscaConstants.LINK_REQUIREMENT_ID);
        DataModelUtil.addSubstitutionMappingReq(serviceTemplate, portReqMappingKey, portReqMappingValue);
    }

    private void addSubstitutionMappingEntry(ServiceTemplate nestedSubstitutionServiceTemplate, String substitutedNodeTypeId) {
        SubstitutionMapping substitutionMappings = new SubstitutionMapping();
        substitutionMappings.setNode_type(substitutedNodeTypeId);
        DataModelUtil.addSubstitutionMapping(nestedSubstitutionServiceTemplate, substitutionMappings);
    }

    private void handleInputParameters(ServiceTemplate nestedSubstitutionServiceTemplate, TranslateTo translateTo) {
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        Optional<NodeType> contrailAbstractNodeType = toscaAnalyzerService
            .fetchNodeType(ToscaNodeType.CONTRAIL_ABSTRACT_SUBSTITUTE, translateTo.getContext().getGlobalServiceTemplates().values());
        Map<String, ParameterDefinition> nestedSubstitutionServiceTemplateInputs = new HashMap<>();
        if (contrailAbstractNodeType.isPresent()) {
            Map<String, PropertyDefinition> contrailAbstractProperties = contrailAbstractNodeType.get().getProperties();
            for (Map.Entry<String, PropertyDefinition> propertyEntry : contrailAbstractProperties.entrySet()) {
                PropertyDefinition abstractPropertyDef = contrailAbstractProperties.get(propertyEntry.getKey());
                if (abstractPropertyDef != null) {
                    nestedSubstitutionServiceTemplateInputs
                        .put(propertyEntry.getKey(), DataModelUtil.convertPropertyDefToParameterDef(abstractPropertyDef));
                }
            }
        }
        if (!nestedSubstitutionServiceTemplateInputs.isEmpty()) {
            nestedSubstitutionServiceTemplate.getTopology_template().setInputs(nestedSubstitutionServiceTemplateInputs);
        }
    }

    private String handleComputeNodeTemplate(TranslateTo translateTo, String computeNodeTypeId, ServiceTemplate nestedSubstitutionServiceTemplate,
                                             String heatStackGroupKey) {
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        Optional<NodeType> contrailComputeNodeType = toscaAnalyzerService
            .fetchNodeType(ToscaNodeType.CONTRAIL_COMPUTE, translateTo.getContext().getGlobalServiceTemplates().values());
        Map<String, Object> computeNodeTemplateProperties = null;
        if (contrailComputeNodeType.isPresent()) {
            Map<String, PropertyDefinition> contrailComputeProperties = contrailComputeNodeType.get().getProperties();
            computeNodeTemplateProperties = new HashMap<>();
            if (contrailComputeProperties != null) {
                for (String computePropertyKey : contrailComputeProperties.keySet()) {
                    Map<String, Object> getInputProperty = new HashMap<>();
                    getInputProperty.put(ToscaFunctions.GET_INPUT.getFunctionName(), computePropertyKey);
                    computeNodeTemplateProperties.put(computePropertyKey, getInputProperty);
                }
            }
        }
        NodeTemplate computeNodeTemplate = new NodeTemplate();
        computeNodeTemplate.setType(computeNodeTypeId);
        if (computeNodeTemplateProperties != null && !computeNodeTemplateProperties.isEmpty()) {
            computeNodeTemplate.setProperties(computeNodeTemplateProperties);
        }
        String computeNodeTemplateId = translateTo.getTranslatedId();
        DataModelUtil.addNodeTemplate(nestedSubstitutionServiceTemplate, computeNodeTemplateId, computeNodeTemplate);
        nestedSubstitutionServiceTemplate.getTopology_template().getGroups().get(heatStackGroupKey).getMembers().add(computeNodeTemplateId);
        return computeNodeTemplateId;
    }

    private String addHeatStackGroup(TranslateTo translateTo, ServiceTemplate serviceTemplate) {
        GroupDefinition serviceInstanceGroupDefinition = new GroupDefinition();
        serviceInstanceGroupDefinition.setType(ToscaGroupType.HEAT_STACK);
        Map<String, Object> groupProperties = new HashMap<>();
        groupProperties
            .put("heat_file", "../" + (new ToscaFileOutputServiceCsarImpl(new AsdPackageHelper(new ManifestUtils()))).getArtifactsFolderName() + "/"
                + translateTo.getHeatFileName());
        serviceInstanceGroupDefinition.setProperties(groupProperties);
        serviceInstanceGroupDefinition.setMembers(new ArrayList<>());
        String heatStackGroupKey = translateTo.getTranslatedId() + "_group";
        DataModelUtil.addGroupDefinitionToTopologyTemplate(serviceTemplate, heatStackGroupKey, serviceInstanceGroupDefinition);
        return heatStackGroupKey;
    }

    private void setNestedServiceTemplateGeneralDetails(TranslateTo translateTo, ServiceTemplate nestedSubstitutionServiceTemplate) {
        Map<String, String> nestedTemplateMetadata = new HashMap<>();
        String nestedTemplateName = new ContrailTranslationHelper()
            .getSubstitutionContrailServiceTemplateMetadata(translateTo.getHeatFileName(), translateTo.getResourceId());
        nestedTemplateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, nestedTemplateName);
        nestedSubstitutionServiceTemplate.setMetadata(nestedTemplateMetadata);
        nestedSubstitutionServiceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
        nestedSubstitutionServiceTemplate.setTopology_template(new TopologyTemplate());
        List<Map<String, Import>> globalTypesImportList = GlobalTypesGenerator.getGlobalTypesImportList();
        globalTypesImportList.addAll(HeatToToscaUtil.createImportList(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
        nestedSubstitutionServiceTemplate.setImports(globalTypesImportList);
    }

    private NodeTemplate createSubstitutedNodeTemplate(TranslateTo translateTo, Resource contrailServiceTemplateResource,
                                                       String contrailServiceTemplateTranslatedId, int numberOfPorts) {
        boolean isImportAddedToServiceTemplate = DataModelUtil
            .isImportAddedToServiceTemplate(translateTo.getServiceTemplate().getImports(), Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        if (!isImportAddedToServiceTemplate) {
            translateTo.getServiceTemplate().getImports().addAll(HeatToToscaUtil.createImportList(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
        }
        NodeTemplate substitutesNodeTemplate = new NodeTemplate();
        substitutesNodeTemplate.setType(contrailServiceTemplateTranslatedId);
        List<String> directiveList = new ArrayList<>();
        directiveList.add(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
        substitutesNodeTemplate.setDirectives(directiveList);
        substitutesNodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
            .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(), translateTo.getResourceId(),
                translateTo.getResource().getProperties(), substitutesNodeTemplate.getProperties(), translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE.getHeatResource(), substitutesNodeTemplate,
                translateTo.getContext()));
        substitutesNodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
            .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(), translateTo.getResourceId(),
                contrailServiceTemplateResource.getProperties(), substitutesNodeTemplate.getProperties(), translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(), substitutesNodeTemplate,
                translateTo.getContext()));
        HeatToToscaUtil.mapBoolean(substitutesNodeTemplate, HeatToToscaUtil
            .getToscaPropertyName(translateTo.getContext(), contrailServiceTemplateResource.getType(),
                HeatConstants.AVAILABILITY_ZONE_ENABLE_PROPERTY_NAME));
        HeatToToscaUtil.mapBoolean(substitutesNodeTemplate, HeatToToscaUtil
            .getToscaPropertyName(translateTo.getContext(), contrailServiceTemplateResource.getType(),
                HeatConstants.ORDERED_INTERFACES_PROPERTY_NAME));
        Object sharedIpListPropertyValue = contrailServiceTemplateResource.getProperties().get(HeatConstants.SHARED_IP_LIST_PROPERTY_NAME);
        String toscaSharedIpListPropertyName = HeatToToscaUtil
            .getToscaPropertyName(translateTo.getContext(), contrailServiceTemplateResource.getType(), HeatConstants.SHARED_IP_LIST_PROPERTY_NAME);
        Optional<List<Map<String, List>>> sharedIpTranslatedSplitFun = new ContrailTranslationHelper()
            .translateFnSplitFunction(sharedIpListPropertyValue, numberOfPorts, true);
        if (sharedIpTranslatedSplitFun.isPresent()) {
            substitutesNodeTemplate.getProperties().put(toscaSharedIpListPropertyName, sharedIpTranslatedSplitFun.get());
        } else {
            HeatToToscaUtil.mapBooleanList(substitutesNodeTemplate, toscaSharedIpListPropertyName);
        }
        Object staticRouteListPropertyValue = contrailServiceTemplateResource.getProperties().get(HeatConstants.STATIC_ROUTES_LIST_PROPERTY_NAME);
        String toscaStaticRoutesListPropertyName = HeatToToscaUtil
            .getToscaPropertyName(translateTo.getContext(), contrailServiceTemplateResource.getType(),
                HeatConstants.STATIC_ROUTES_LIST_PROPERTY_NAME);
        Optional<List<Map<String, List>>> staticRouteTranslatedSplitFun = new ContrailTranslationHelper()
            .translateFnSplitFunction(staticRouteListPropertyValue, numberOfPorts, true);
        if (staticRouteTranslatedSplitFun.isPresent()) {
            substitutesNodeTemplate.getProperties().put(toscaStaticRoutesListPropertyName, staticRouteTranslatedSplitFun.get());
        } else {
            HeatToToscaUtil.mapBooleanList(substitutesNodeTemplate, toscaStaticRoutesListPropertyName);
        }
        Object serviceInterfaceTypeListPropertyValue = contrailServiceTemplateResource.getProperties()
            .get(HeatConstants.SERVICE_INTERFCAE_TYPE_LIST_PROPERTY_NAME);
        String toscaServiceInterfaceTypeListPropertyName = HeatToToscaUtil
            .getToscaPropertyName(translateTo.getContext(), contrailServiceTemplateResource.getType(),
                HeatConstants.SERVICE_INTERFCAE_TYPE_LIST_PROPERTY_NAME);
        Optional<List<Map<String, List>>> serviceInterfaceTypeTranslatedSplitFun = new ContrailTranslationHelper()
            .translateFnSplitFunction(serviceInterfaceTypeListPropertyValue, numberOfPorts, false);
        serviceInterfaceTypeTranslatedSplitFun.ifPresent(
            translatedSplitFun -> substitutesNodeTemplate.getProperties().put(toscaServiceInterfaceTypeListPropertyName, translatedSplitFun));
        String substitutedNodeTemplateId = translateTo.getTranslatedId();
        DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), substitutedNodeTemplateId, substitutesNodeTemplate);
        return substitutesNodeTemplate;
    }

    private void addNetworkLinkRequirements(NodeType nodeType, int numberOfPorts) {
        if (nodeType.getRequirements() == null) {
            List<Map<String, RequirementDefinition>> requirementList = new ArrayList<>();
            for (int i = 0; i < numberOfPorts; i++) {
                Map<String, RequirementDefinition> requirementDefinitionMap = new HashMap<>();
                requirementDefinitionMap.put(ToscaConstants.LINK_REQUIREMENT_ID + "_port_" + i, DataModelUtil
                    .createRequirement(ToscaCapabilityType.NATIVE_NETWORK_LINKABLE, ToscaNodeType.NATIVE_ROOT,
                        ToscaRelationshipType.NATIVE_NETWORK_LINK_TO, null));
                requirementList.add(requirementDefinitionMap);
            }
            if (numberOfPorts > 0) {
                nodeType.setRequirements(requirementList);
            }
        }
    }

    private int getServiceInstanceNumberOfPorts(Resource serviceInstanceResource) {
        int numberOfPorts;
        Object interfaceTypeProperty = serviceInstanceResource.getProperties().get(HeatConstants.INTERFACE_LIST_PROPERTY_NAME);
        if (interfaceTypeProperty == null) {
            numberOfPorts = 0;
        } else if (interfaceTypeProperty instanceof List) {
            numberOfPorts = ((List) interfaceTypeProperty).size();
        } else if (interfaceTypeProperty instanceof Map) {
            numberOfPorts = 1;
        } else {
            numberOfPorts = 0;
        }
        return numberOfPorts;
    }

    private AttachedResourceId getServiceTemplateAttachedId(TranslateTo translateTo, Resource serviceInstanceResource) {
        Object serviceTemplateProperty = serviceInstanceResource.getProperties().get("service_template");
        Optional<AttachedResourceId> serviceTemplateId = HeatToToscaUtil
            .extractAttachedResourceId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
                serviceTemplateProperty);
        if (serviceTemplateId.isPresent()) {
            return serviceTemplateId.get();
        } else {
            throw new CoreException(new MissingMandatoryPropertyErrorBuilder("service_template").build());
        }
    }
}
