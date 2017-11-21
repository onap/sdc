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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.composition;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
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
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ExtractCompositionDataContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompositionDataExtractorImpl implements CompositionDataExtractor {

  protected static Logger logger;
  private static ToscaAnalyzerService toscaAnalyzerService;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  static {
    logger = LoggerFactory.getLogger(CompositionDataExtractorImpl.class);
    toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
  }

  /**
   * Extract service composition data composition data.
   *
   * @param toscaServiceModel the tosca service model
   * @return the composition data
   */
  public CompositionData extractServiceCompositionData(ToscaServiceModel toscaServiceModel) {


    mdcDataDebugMessage.debugEntryMessage(null);

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

    mdcDataDebugMessage.debugExitMessage(null);
    return compositionData;
  }

  private void extractServiceCompositionData(String serviceTemplateFileName,
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

  private void handleSubstitution(ServiceTemplate serviceTemplate,
                                         ToscaServiceModel toscaServiceModel,
                                         ExtractCompositionDataContext context) {


    mdcDataDebugMessage.debugEntryMessage(null);

    Map<String, NodeTemplate> substitutableNodeTemplates =
        toscaAnalyzerService.getSubstitutableNodeTemplates(serviceTemplate);

    if (substitutableNodeTemplates != null) {
      for (String substitutableNodeTemplateId : substitutableNodeTemplates.keySet()) {
        handleSubstitutableNodeTemplate(serviceTemplate, toscaServiceModel,
            substitutableNodeTemplateId,
            substitutableNodeTemplates.get(substitutableNodeTemplateId), context);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void handleSubstitutableNodeTemplate(ServiceTemplate serviceTemplate,
                                                      ToscaServiceModel toscaServiceModel,
                                                      String substitutableNodeTemplateId,
                                                      NodeTemplate substitutableNodeTemplate,
                                                      ExtractCompositionDataContext context) {


    mdcDataDebugMessage.debugEntryMessage(null);

    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    Optional<String> substituteServiceTemplateFileName = toscaAnalyzerService
        .getSubstituteServiceTemplateName(substitutableNodeTemplateId, substitutableNodeTemplate);
    if (!substituteServiceTemplateFileName.isPresent()) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.EXTRACT_COMPOSITION_DATA, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(),
          LoggerErrorDescription.EXTRACT_COMPOSITION_DATA);
      throw new CoreException(
          new ToscaInvalidSubstituteNodeTemplateErrorBuilder(substitutableNodeTemplateId).build());
    }
    if (context.getHandledServiceTemplates().contains(substituteServiceTemplateFileName.get())) {
      //each substitution is should be handled once, and will get the connection to the upper
      // service level according to the first one which was processed
      mdcDataDebugMessage.debugExitMessage(null);
      return;
    }

    ServiceTemplate substituteServiceTemplate =
        toscaServiceModel.getServiceTemplates().get(substituteServiceTemplateFileName.get());
    extractServiceCompositionData(substituteServiceTemplateFileName.get(),
        substituteServiceTemplate, toscaServiceModel, context);

    List<Map<String, RequirementAssignment>> substitutableRequirements =
        substitutableNodeTemplate.getRequirements();

    if (CollectionUtils.isEmpty(substitutableRequirements)) {
      mdcDataDebugMessage.debugExitMessage(null);
      return;
    }

    for (Map<String, RequirementAssignment> substitutableReq : substitutableRequirements) {
      substitutableReq.keySet().stream().filter(reqId -> {
        RequirementAssignment reqAssignment = toscaExtensionYamlUtil
            .yamlToObject(toscaExtensionYamlUtil.objectToYaml(substitutableReq.get(reqId)),
                RequirementAssignment.class);

        mdcDataDebugMessage.debugExitMessage(null);
        return isLinkToNetworkRequirementAssignment(reqAssignment);
      }).forEach(reqId -> {
        RequirementAssignment linkToNetworkRequirement = toscaExtensionYamlUtil
            .yamlToObject(toscaExtensionYamlUtil.objectToYaml(substitutableReq.get(reqId)),
                RequirementAssignment.class);
        String connectedNodeId = linkToNetworkRequirement.getNode();
        Optional<NodeTemplate> connectedNodeTemplate =
            toscaAnalyzerService.getNodeTemplateById(serviceTemplate, connectedNodeId);

        if (connectedNodeTemplate.isPresent() && toscaAnalyzerService
            .isTypeOf(connectedNodeTemplate.get(), ToscaNodeType.NATIVE_NETWORK,
                serviceTemplate, toscaServiceModel)) {
          Optional<Map.Entry<String, NodeTemplate>> mappedNodeTemplate = toscaAnalyzerService
              .getSubstitutionMappedNodeTemplateByExposedReq(
                  substituteServiceTemplateFileName.get(), substituteServiceTemplate, reqId);
          if (!mappedNodeTemplate.isPresent()) {
            MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.EXTRACT_COMPOSITION_DATA, ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(),
                LoggerErrorDescription.EXTRACT_COMPOSITION_DATA);
            throw new CoreException(new ToscaMissingSubstitutionMappingForReqCapErrorBuilder(
                ToscaMissingSubstitutionMappingForReqCapErrorBuilder.MappingExposedEntry
                    .REQUIREMENT, connectedNodeId).build());
          }

          if (toscaAnalyzerService.isTypeOf(mappedNodeTemplate.get().getValue(),
              ToscaNodeType.NATIVE_NETWORK_PORT, serviceTemplate,
              toscaServiceModel)) {
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
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
              LoggerTragetServiceName.EXTRACT_COMPOSITION_DATA, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(),
              LoggerErrorDescription.EXTRACT_COMPOSITION_DATA);
          throw new CoreException(
              new ToscaInvalidEntryNotFoundErrorBuilder("Node Template", connectedNodeId).build());
        }
      });
    }
  }

  private boolean isLinkToNetworkRequirementAssignment(RequirementAssignment requirement) {
    return toscaAnalyzerService.isDesiredRequirementAssignment(requirement,
        ToscaCapabilityType.NATIVE_NETWORK_LINKABLE, null,
        ToscaRelationshipType.NATIVE_NETWORK_LINK_TO);
  }


  private void connectPortToNetwork(Nic port, NodeTemplate portNodeTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null);

    List<RequirementAssignment> linkRequirementsToNetwork =
        toscaAnalyzerService.getRequirements(portNodeTemplate, ToscaConstants.LINK_REQUIREMENT_ID);

    //port is connected to one network
    for (RequirementAssignment linkRequirementToNetwork : linkRequirementsToNetwork) {
      port.setNetworkName(linkRequirementToNetwork.getNode());
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  /*
  return Map with key - compute node template id, value - list of connected port node template id
   */
  private Map<String, List<String>> getComputeToPortsConnection(
      Map<String, NodeTemplate> portNodeTemplates) {


    mdcDataDebugMessage.debugEntryMessage(null);

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

    mdcDataDebugMessage.debugExitMessage(null);
    return computeToPortConnection;
  }

  private void extractComponents(ServiceTemplate serviceTemplate,
                                        ToscaServiceModel toscaServiceModel,
                                        ExtractCompositionDataContext context) {


    mdcDataDebugMessage.debugEntryMessage(null);

    Map<String, NodeTemplate> computeNodeTemplates = toscaAnalyzerService
        .getNodeTemplatesByType(serviceTemplate, ToscaNodeType.NATIVE_COMPUTE,
            toscaServiceModel);
    if (MapUtils.isEmpty(computeNodeTemplates)) {
      return;
    }
    Map<String, List<String>> imageNodeTemplates = getComponentImages(computeNodeTemplates,
        toscaServiceModel);
    Map<String, List<String>> computeFlavorNodeTemplates =
        getComponentComputeFlavor(computeNodeTemplates, toscaServiceModel);
    Map<String, NodeTemplate> portNodeTemplates = toscaAnalyzerService
        .getNodeTemplatesByType(serviceTemplate, ToscaNodeType.NATIVE_NETWORK_PORT,
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
            computesGroupedByType, imageNodeTemplates, computeFlavorNodeTemplates, nodeType,
            context));

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private Map<String,List<String>> getComponentImages(Map<String, NodeTemplate>
                                                          computeNodeTemplates,
                                                      ToscaServiceModel toscaServiceModel) {
    return getComponentProperty(ToscaConstants.COMPUTE_IMAGE, computeNodeTemplates, toscaServiceModel);
  }

  private Map<String,List<String>> getComponentComputeFlavor(Map<String, NodeTemplate>
                                                                 computeNodeTemplates,
                                                             ToscaServiceModel toscaServiceModel) {
    return getComponentProperty(ToscaConstants.COMPUTE_FLAVOR, computeNodeTemplates, toscaServiceModel);
  }

  private Map<String, List<String>> getComponentProperty(String propertyName,
                                                         Map<String, NodeTemplate> computeNodeTemplates,
                                                         ToscaServiceModel toscaServiceModel) {
    Map<String,List<String>> componentPropertyValues = new HashMap<>();
    for (String component : computeNodeTemplates.keySet()) {
      List<String> computes = new ArrayList<>();
      Map<String,Object> properties =  computeNodeTemplates.get(component).getProperties();

      if(MapUtils.isEmpty(properties)){
        continue;
      }

      List<Object> computesList = properties.entrySet()
          .stream()
          .filter(map -> map.getKey().equals(propertyName))
          .map(Map.Entry::getValue)
          .collect(Collectors.toList());
      for (Object obj : computesList) {
        if (obj instanceof String) {
          computes.add((String) obj);
        } else {
          Map<String, String> objMap = new ObjectMapper().convertValue(obj, Map.class);
          computes.add(getInputs(toscaServiceModel, objMap.get("get_input")));
        }
      }
      componentPropertyValues.put(component,computes);
    }
    return componentPropertyValues;
  }

  private String  getInputs(ToscaServiceModel toscaServiceModel, String inputValue) {
    String mainTemplate = toscaServiceModel.getEntryDefinitionServiceTemplate();
    List<ServiceTemplate> toscaServiceTemplates = toscaServiceModel.getServiceTemplates().entrySet()
        .stream()
        .filter(map -> map.getKey().equals(mainTemplate))
        .map(map -> map.getValue())
        .collect(Collectors.toList());
    ServiceTemplate serviceTemplate = toscaServiceTemplates.get(0);

    if (Objects.nonNull(serviceTemplate.getTopology_template())
        && MapUtils.isNotEmpty(serviceTemplate.getTopology_template().getInputs())) {
      for (Map.Entry<String, ParameterDefinition> inputEntry : serviceTemplate
          .getTopology_template().getInputs().entrySet()) {
        if (inputEntry.getKey().equals(inputValue)) {
          String value;
          try {
            value= (String) inputEntry.getValue().get_default();
          } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            value = inputEntry.getValue().get_default().toString();
          }
        return value;
        }
      }
    }
    return inputValue;
  }

  private void extractComponent(ServiceTemplate serviceTemplate,
                                       Map<String, List<String>> computeToPortsConnection,
                                       Map<String, List<String>> computesGroupedByType,
                                       Map<String, List<String>> imageList,
                                       Map<String, List<String>> computeFlavorNodeTemplates,
                                       String computeNodeType,
                                       ExtractCompositionDataContext context) {
    ComponentData component = new ComponentData();
    component.setName(computeNodeType);
    component.setDisplayName(getComponentDisplayName(component.getName()));
    component.setVfcCode(component.getDisplayName());
    Component componentModel = new Component();
    componentModel.setData(component);

    String computeId = computesGroupedByType.get(computeNodeType).get(0);
    List<String> connectedPortIds = computeToPortsConnection.get(computeId);
    List<String> images = imageList.get(computeId);
    List<String> computeFlavors = computeFlavorNodeTemplates.get(computeId);

    if (connectedPortIds != null) {
      componentModel.setNics(new ArrayList<>());
      componentModel.setImages(new ArrayList<>());
      componentModel.setCompute(new ArrayList<>());
      for (String portId : connectedPortIds) {
        Nic port = extractPort(serviceTemplate, portId);
        componentModel.getNics().add(port);
        context.addNic(portId, port);
      }
      for (String image : images) {
        Image img = new Image(image);
        componentModel.getImages().add(img);
        context.addImage(image, img);
      }
      for (String flavor : computeFlavors) {
        ComputeData computeFlavor = new ComputeData(flavor);
        componentModel.getCompute().add(computeFlavor);
        context.addCompute(flavor,computeFlavor);
      }
    }
    context.addComponent(componentModel);
    context.getCreatedComponents().add(computeNodeType);
  }

  private Nic extractPort(ServiceTemplate serviceTemplate, String portNodeTemplateId) {
    Optional<NodeTemplate> portNodeTemplate =
        toscaAnalyzerService.getNodeTemplateById(serviceTemplate, portNodeTemplateId);
    if (portNodeTemplate.isPresent()) {
      Nic port = new Nic();
      port.setName(portNodeTemplateId);
      connectPortToNetwork(port, portNodeTemplate.get());
      return port;
    } else {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.EXTRACT_COMPOSITION_DATA, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(),
          LoggerErrorDescription.EXTRACT_COMPOSITION_DATA);
      throw new CoreException(
          new ToscaInvalidEntryNotFoundErrorBuilder("Node Template", portNodeTemplateId).build());
    }
  }

  private Map<String, List<String>> getNodeTemplatesGroupedByType(
      Map<String, NodeTemplate> nodeTemplates) {


    mdcDataDebugMessage.debugEntryMessage(null);

    Map<String, List<String>> nodeTemplatesGrouped =
        new HashMap<>();   //key - node type, value - list of node ids with this type
    for (String nodeId : nodeTemplates.keySet()) {
      String nodeType = nodeTemplates.get(nodeId).getType();
      nodeTemplatesGrouped.putIfAbsent(nodeType, new ArrayList<>());
      nodeTemplatesGrouped.get(nodeType).add(nodeId);
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return nodeTemplatesGrouped;
  }

  private List<Network> extractNetworks(ServiceTemplate serviceTemplate,
                                               ToscaServiceModel toscaServiceModel) {


    mdcDataDebugMessage.debugEntryMessage(null);

    List<Network> networks = new ArrayList<>();
    Map<String, NodeTemplate> networkNodeTemplates = toscaAnalyzerService
        .getNodeTemplatesByType(serviceTemplate, ToscaNodeType.NATIVE_NETWORK,
            toscaServiceModel);
    if (MapUtils.isEmpty(networkNodeTemplates)) {
      mdcDataDebugMessage.debugExitMessage(null);
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

    mdcDataDebugMessage.debugExitMessage(null);
    return networks;
  }

  //dhcp default value is true
  private Optional<Boolean> getNetworkDhcpValue(ServiceTemplate serviceTemplate,
                                                       NodeTemplate networkNodeTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null);

    if (networkNodeTemplate == null) {
      return Optional.empty();
    }
    if (networkNodeTemplate.getProperties() == null
        || networkNodeTemplate.getProperties().get(ToscaConstants.DHCP_ENABLED_PROPERTY_NAME)
        == null) {
      mdcDataDebugMessage.debugExitMessage(null);
      return Optional.of(true);
    }

    Object dhcp =
        networkNodeTemplate.getProperties().get(ToscaConstants.DHCP_ENABLED_PROPERTY_NAME);
    if (dhcp instanceof String) {
      mdcDataDebugMessage.debugExitMessage(null);
      return Optional.of(Boolean.valueOf((String) dhcp));
    } else if (dhcp instanceof Boolean) {
      mdcDataDebugMessage.debugExitMessage(null);
      return Optional.of((Boolean) dhcp);
    } else if (dhcp instanceof Map) {
      String inputParameterName =
          (String) ((Map) dhcp).get(ToscaFunctions.GET_INPUT.getDisplayName());
      if (inputParameterName != null) {
        ParameterDefinition inputParameterDefinition =
            serviceTemplate.getTopology_template().getInputs().get(inputParameterName);
        if (inputParameterDefinition != null) {
          if (inputParameterDefinition.get_default() != null) {
            mdcDataDebugMessage.debugExitMessage(null);
            return Optional.of(Boolean.valueOf(inputParameterDefinition.get_default().toString()));
          }
        } else {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
              LoggerTragetServiceName.EXTRACT_COMPOSITION_DATA, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(),
              LoggerErrorDescription.EXTRACT_COMPOSITION_DATA);
          throw new CoreException(
              new ToscaInvalidEntryNotFoundErrorBuilder("Input Parameter", inputParameterName)
                  .build());
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return Optional.of(true);
  }

  @Override
  public String getComponentDisplayName(String componentName) {
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
