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

package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.errors.ToscaInvalidEntryNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidSubstituteNodeTemplateErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaMissingSubstitutionMappingForReqCapErrorBuilder;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.ExtractCompositionDataContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The type Composition data extractor.
 */
public class CompositionDataExtractor {

  /**
   * The constant logger.
   */
  protected static Logger logger;
  private static ToscaAnalyzerService toscaAnalyzerService;

  static {
    logger = LoggerFactory.getLogger(CompositionDataExtractor.class);
    toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
  }

  /**
   * Extract service composition data composition data.
   *
   * @param toscaServiceModel the tosca service model
   * @return the composition data
   */
  public static CompositionData extractServiceCompositionData(ToscaServiceModel toscaServiceModel) {
    ExtractCompositionDataContext context = new ExtractCompositionDataContext();
    String entryDefinitionServiceTemplateFileName =
        toscaServiceModel.getEntryDefinitionServiceTemplate();
    ServiceTemplate entryDefinitionServiceTemplate =
        toscaServiceModel.getServiceTemplates().get(entryDefinitionServiceTemplateFileName);
    extractServiceCompositionData(entryDefinitionServiceTemplateFileName,
        entryDefinitionServiceTemplate, toscaServiceModel, context);

    CompositionData compositionData = new CompositionData();
    compositionData.setNetworks(context.getNetworks());
    compositionData.setComponents(context.getComponents());
    return compositionData;
  }

  private static void extractServiceCompositionData(String serviceTemplateFileName,
                                                    ServiceTemplate serviceTemplate,
                                                    ToscaServiceModel toscaServiceModel,
                                                    ExtractCompositionDataContext context) {
    if (context.getHandledServiceTemplates().contains(serviceTemplateFileName)) {
      return;
    }
    context.addNetworks(extractNetworks(serviceTemplate, toscaServiceModel));
    extractComponents(serviceTemplate, toscaServiceModel, context);
    handleSubstitution(serviceTemplate, toscaServiceModel, context);
    context.addHandledServiceTemplates(serviceTemplateFileName);
  }

  private static void handleSubstitution(ServiceTemplate serviceTemplate,
                                         ToscaServiceModel toscaServiceModel,
                                         ExtractCompositionDataContext context) {
    Map<String, NodeTemplate> substitutableNodeTemplates =
        toscaAnalyzerService.getSubstitutableNodeTemplates(serviceTemplate);

    if (substitutableNodeTemplates != null) {
      for (String substitutableNodeTemplateId : substitutableNodeTemplates.keySet()) {
        handleSubstitutableNodeTemplate(serviceTemplate, toscaServiceModel,
            substitutableNodeTemplateId,
            substitutableNodeTemplates.get(substitutableNodeTemplateId), context);
      }
    }
  }

  private static void handleSubstitutableNodeTemplate(ServiceTemplate serviceTemplate,
                                                      ToscaServiceModel toscaServiceModel,
                                                      String substitutableNodeTemplateId,
                                                      NodeTemplate substitutableNodeTemplate,
                                                      ExtractCompositionDataContext context) {
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    Optional<String> substituteServiceTemplateFileName = toscaAnalyzerService
        .getSubstituteServiceTemplateName(substitutableNodeTemplateId, substitutableNodeTemplate);
    if (!substituteServiceTemplateFileName.isPresent()) {
      throw new CoreException(
          new ToscaInvalidSubstituteNodeTemplateErrorBuilder(substitutableNodeTemplateId).build());
    }
    if (context.getHandledServiceTemplates().contains(substituteServiceTemplateFileName.get())) {
      return;
    }

    ServiceTemplate substituteServiceTemplate =
        toscaServiceModel.getServiceTemplates().get(substituteServiceTemplateFileName.get());
    extractServiceCompositionData(substituteServiceTemplateFileName.get(),
        substituteServiceTemplate, toscaServiceModel, context);

    List<Map<String, RequirementAssignment>> substitutableRequirements =
        substitutableNodeTemplate.getRequirements();

    if (CollectionUtils.isEmpty(substitutableRequirements)) {
      return;
    }

    for (Map<String, RequirementAssignment> substitutableReq : substitutableRequirements) {
      substitutableReq.keySet().stream().filter(reqId -> {
        RequirementAssignment reqAssignment = toscaExtensionYamlUtil
            .yamlToObject(toscaExtensionYamlUtil.objectToYaml(substitutableReq.get(reqId)),
                RequirementAssignment.class);
        return isLinkToNetworkRequirementAssignment(reqAssignment);
      }).forEach(reqId -> {
        RequirementAssignment linkToNetworkRequirement = toscaExtensionYamlUtil
            .yamlToObject(toscaExtensionYamlUtil.objectToYaml(substitutableReq.get(reqId)),
                RequirementAssignment.class);
        String connectedNodeId = linkToNetworkRequirement.getNode();
        Optional<NodeTemplate> connectedNodeTemplate =
            toscaAnalyzerService.getNodeTemplateById(serviceTemplate, connectedNodeId);

        if (connectedNodeTemplate.isPresent() && toscaAnalyzerService
            .isTypeOf(connectedNodeTemplate.get(), ToscaNodeType.NETWORK.getDisplayName(),
                serviceTemplate, toscaServiceModel)) {
          Optional<Map.Entry<String, NodeTemplate>> mappedNodeTemplate = toscaAnalyzerService
              .getSubstitutionMappedNodeTemplateByExposedReq(
                  substituteServiceTemplateFileName.get(), substituteServiceTemplate, reqId);
          if (!mappedNodeTemplate.isPresent()) {
            throw new CoreException(new ToscaMissingSubstitutionMappingForReqCapErrorBuilder(
                ToscaMissingSubstitutionMappingForReqCapErrorBuilder.MappingExposedEntry
                    .REQUIREMENT, connectedNodeId).build());
          }

          if (toscaAnalyzerService.isTypeOf(mappedNodeTemplate.get().getValue(),
              ToscaNodeType.NETWORK_PORT.getDisplayName(), serviceTemplate, toscaServiceModel)) {
            Nic port = context.getNics().get(mappedNodeTemplate.get().getKey());
            if (port != null) {
              port.setNetworkName(connectedNodeId);
            } else {
              logger.warn(
                  "Different ports define for the same component which is used in different "
                      + "substitution service templates.");
            }
          }
        } else if (!connectedNodeTemplate.isPresent()) {
          throw new CoreException(
              new ToscaInvalidEntryNotFoundErrorBuilder("Node Template", connectedNodeId).build());
        }
      });
    }
  }

  private static boolean isLinkToNetworkRequirementAssignment(RequirementAssignment requirement) {
    return toscaAnalyzerService.isDesiredRequirementAssignment(requirement,
        ToscaCapabilityType.NETWORK_LINKABLE.getDisplayName(), null,
        ToscaRelationshipType.NETWORK_LINK_TO.getDisplayName());
  }


  private static void connectPortToNetwork(Nic port, NodeTemplate portNodeTemplate) {
    List<RequirementAssignment> linkRequirementsToNetwork =
        toscaAnalyzerService.getRequirements(portNodeTemplate, ToscaConstants.LINK_REQUIREMENT_ID);

    //port is connected to one network
    for (RequirementAssignment linkRequirementToNetwork : linkRequirementsToNetwork) {
      port.setNetworkName(linkRequirementToNetwork.getNode());
    }

  }

  /*
  return Map with key - compute node template id, value - list of connected port node template id
   */
  private static Map<String, List<String>> getComputeToPortsConnection(
      Map<String, NodeTemplate> portNodeTemplates) {
    Map<String, List<String>> computeToPortConnection = new HashMap<>();
    if (MapUtils.isEmpty(portNodeTemplates)) {
      return computeToPortConnection;
    }
    for (String portId : portNodeTemplates.keySet()) {
      List<RequirementAssignment> bindingRequirementsToCompute = toscaAnalyzerService
          .getRequirements(portNodeTemplates.get(portId), ToscaConstants.BINDING_REQUIREMENT_ID);
      for (RequirementAssignment bindingRequirementToCompute : bindingRequirementsToCompute) {
        computeToPortConnection
            .putIfAbsent(bindingRequirementToCompute.getNode(), new ArrayList<>());
        computeToPortConnection.get(bindingRequirementToCompute.getNode()).add(portId);
      }

    }

    return computeToPortConnection;
  }

  private static void extractComponents(ServiceTemplate serviceTemplate,
                                        ToscaServiceModel toscaServiceModel,
                                        ExtractCompositionDataContext context) {
    Map<String, NodeTemplate> computeNodeTemplates = toscaAnalyzerService
        .getNodeTemplatesByType(serviceTemplate, ToscaNodeType.COMPUTE.getDisplayName(),
            toscaServiceModel);
    if (MapUtils.isEmpty(computeNodeTemplates)) {
      return;
    }
    Map<String, NodeTemplate> portNodeTemplates = toscaAnalyzerService
        .getNodeTemplatesByType(serviceTemplate, ToscaNodeType.NETWORK_PORT.getDisplayName(),
            toscaServiceModel);
    Map<String, List<String>> computeToPortsConnection =
        getComputeToPortsConnection(portNodeTemplates);
    Map<String, List<String>> computesGroupedByType =
        getNodeTemplatesGroupedByType(computeNodeTemplates);

    computesGroupedByType.keySet()
        .stream()
        .filter(nodeType ->
            !context.getCreatedComponents().contains(nodeType))
        .forEach(nodeType -> extractComponent(serviceTemplate, computeToPortsConnection,
            computesGroupedByType, nodeType, context));
  }

  private static void extractComponent(ServiceTemplate serviceTemplate,
                                       Map<String, List<String>> computeToPortsConnection,
                                       Map<String, List<String>> computesGroupedByType,
                                       String computeNodeType,
                                       ExtractCompositionDataContext context) {
    ComponentData component = new ComponentData();
    component.setName(computeNodeType);
    component.setDisplayName(getComponentDisplayName(component.getName()));
    Component componentModel = new Component();
    componentModel.setData(component);

    String computeId = computesGroupedByType.get(computeNodeType).get(0);
    List<String> connectedPortIds = computeToPortsConnection.get(computeId);

    if (connectedPortIds != null) {
      componentModel.setNics(new ArrayList<>());
      for (String portId : connectedPortIds) {
        Nic port = extractPort(serviceTemplate, portId);
        componentModel.getNics().add(port);
        context.addNic(portId, port);
      }
    }
    context.addComponent(componentModel);
    context.getCreatedComponents().add(computeNodeType);
  }

  private static Nic extractPort(ServiceTemplate serviceTemplate, String portNodeTemplateId) {
    Optional<NodeTemplate> portNodeTemplate =
        toscaAnalyzerService.getNodeTemplateById(serviceTemplate, portNodeTemplateId);
    if (portNodeTemplate.isPresent()) {
      Nic port = new Nic();
      port.setName(portNodeTemplateId);
      connectPortToNetwork(port, portNodeTemplate.get());
      return port;
    } else {
      throw new CoreException(
          new ToscaInvalidEntryNotFoundErrorBuilder("Node Template", portNodeTemplateId).build());
    }
  }


  private static Map<String, List<String>> getNodeTemplatesGroupedByType(
      Map<String, NodeTemplate> nodeTemplates) {
    Map<String, List<String>> nodeTemplatesGrouped =
        new HashMap<>();   //key - node type, value - list of node ids with this type
    for (String nodeId : nodeTemplates.keySet()) {
      String nodeType = nodeTemplates.get(nodeId).getType();
      nodeTemplatesGrouped.putIfAbsent(nodeType, new ArrayList<>());
      nodeTemplatesGrouped.get(nodeType).add(nodeId);
    }
    return nodeTemplatesGrouped;
  }

  private static List<Network> extractNetworks(ServiceTemplate serviceTemplate,
                                               ToscaServiceModel toscaServiceModel) {
    List<Network> networks = new ArrayList<>();
    Map<String, NodeTemplate> networkNodeTemplates = toscaAnalyzerService
        .getNodeTemplatesByType(serviceTemplate, ToscaNodeType.NETWORK.getDisplayName(),
            toscaServiceModel);
    if (MapUtils.isEmpty(networkNodeTemplates)) {
      return networks;
    }
    for (String networkId : networkNodeTemplates.keySet()) {
      Network network = new Network();
      network.setName(networkId);
      Optional<Boolean> networkDhcpValue =
          getNetworkDhcpValue(serviceTemplate, networkNodeTemplates.get(networkId));
      network.setDhcp(networkDhcpValue.isPresent() ? networkDhcpValue.get() : true);
      networks.add(network);
    }

    return networks;
  }

  //dhcp default value is true
  private static Optional<Boolean> getNetworkDhcpValue(ServiceTemplate serviceTemplate,
                                                       NodeTemplate networkNodeTemplate) {
    if (networkNodeTemplate == null) {
      return Optional.empty();
    }
    if (networkNodeTemplate.getProperties() == null
        || networkNodeTemplate.getProperties().get(ToscaConstants.DHCP_ENABLED_PROPERTY_NAME)
        == null) {
      return Optional.of(true);
    }

    Object dhcp =
        networkNodeTemplate.getProperties().get(ToscaConstants.DHCP_ENABLED_PROPERTY_NAME);
    if (dhcp instanceof String) {
      return Optional.of(Boolean.valueOf((String) dhcp));
    } else if (dhcp instanceof Boolean) {
      return Optional.of((Boolean) dhcp);
    } else if (dhcp instanceof Map) {
      String inputParameterName =
          (String) ((Map) dhcp).get(ToscaFunctions.GET_INPUT.getDisplayName());
      if (inputParameterName != null) {
        ParameterDefinition inputParameterDefinition =
            serviceTemplate.getTopology_template().getInputs().get(inputParameterName);
        if (inputParameterDefinition != null) {
          if (inputParameterDefinition.get_default() != null) {
            return Optional.of(Boolean.valueOf(inputParameterDefinition.get_default().toString()));
          }
        } else {
          throw new CoreException(
              new ToscaInvalidEntryNotFoundErrorBuilder("Input Parameter", inputParameterName)
                  .build());
        }
      }
    }

    return Optional.of(true);
  }

  private static String getComponentDisplayName(String componentName) {
    if (componentName == null) {
      return null;
    }
    String delimiterChar = ".";
    if (componentName.contains(delimiterChar)) {
      return componentName.substring(componentName.lastIndexOf(delimiterChar) + 1);
    }
    return componentName;

  }

}
