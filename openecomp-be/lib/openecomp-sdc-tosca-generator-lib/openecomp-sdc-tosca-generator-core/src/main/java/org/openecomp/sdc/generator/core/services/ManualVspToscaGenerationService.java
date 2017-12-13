/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.generator.core.services;

import static org.openecomp.sdc.generator.util.GeneratorConstants.ALLOWED_FLAVORS_PROPERTY;
import static org.openecomp.sdc.generator.util.GeneratorConstants.DISK_SIZE;
import static org.openecomp.sdc.generator.util.GeneratorConstants.DISK_SIZE_PROP_DESC_PREFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.IMAGES_PROPERTY;
import static org.openecomp.sdc.generator.util.GeneratorConstants.MEM_SIZE;
import static org.openecomp.sdc.generator.util.GeneratorConstants.MEM_SIZE_PROP_DESC_PREFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.NUM_CPUS;
import static org.openecomp.sdc.generator.util.GeneratorConstants.NUM_CPUS_PROP_DESC_PREFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.PORT_NODE_TEMPLATE_ID_SUFFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.PORT_TYPE_EXTERNAL_NODE_TEMPLATE_SUFFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.PORT_TYPE_INTERNAL_NODE_TEMPLATE_SUFFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.RELEASE_VENDOR;
import static org.openecomp.sdc.generator.util.GeneratorConstants.TOSCA_SERVICE_TEMPLATE_FILE_NAME_SUFFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.VNF_CONFIG_NODE_TEMPLATE_ID_SUFFIX;
import static org.openecomp.sdc.generator.util.GeneratorConstants.VNF_NODE_TEMPLATE_ID_SUFFIX;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.generator.core.utils.GeneratorUtils;
import org.openecomp.sdc.generator.datatypes.tosca.MultiFlavorVfcImage;
import org.openecomp.sdc.generator.datatypes.tosca.VspModelInfo;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The type Manual vsp tosca generator.
 */
public class ManualVspToscaGenerationService {

  //Map of the abstract node template id and substitution node type
  private Map<String, String> abstractSubstitutionIdTypes = new HashMap<>();
  //Map of service template name and service template for the generated service templates
  private Map<String, ServiceTemplate> generatedServiceTemplates = new HashMap<>();


  /**
   * Create manual vsp tosca service model tosca service model.
   *
   * @param vspModelInfo the vsp model info
   * @return the tosca service model
   */
  public ToscaServiceModel createManualVspToscaServiceModel(VspModelInfo vspModelInfo) {
    ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    Map<String, ServiceTemplate> serviceTemplates = new HashMap<>(GlobalTypesGenerator
        .getGlobalTypesServiceTemplate(OnboardingTypesEnum.MANUAL));
    toscaServiceModel.setServiceTemplates(serviceTemplates);
    toscaServiceModel.setEntryDefinitionServiceTemplate(Constants.MAIN_TEMPLATE_NAME
        + TOSCA_SERVICE_TEMPLATE_FILE_NAME_SUFFIX);
    createToscaFromVspData(vspModelInfo, toscaServiceModel);
    for (Map.Entry<String, ServiceTemplate> serviceTemplateEntry :
        generatedServiceTemplates.entrySet()) {
      ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates, serviceTemplateEntry
          .getValue());
    }
    return toscaServiceModel;
  }

  private void createToscaFromVspData(VspModelInfo vspModelInfo,
                                        ToscaServiceModel toscaServiceModel) {
    List<ServiceTemplate> serviceTemplates = new ArrayList<>();
    //Only one component supported
    Optional<String> componentName = getComponentNameFromVspModel(vspModelInfo);
    if (componentName.isPresent()) {
      //Create main service template
      ServiceTemplate mainServiceTemplate = createMainServiceTemplate(vspModelInfo);
      generatedServiceTemplates.put(Constants.MAIN_TEMPLATE_NAME, mainServiceTemplate);
      //Create substitution service template
      for (Map.Entry<String, String> entry : abstractSubstitutionIdTypes.entrySet()) {
        ServiceTemplate substitutionServiceTemplate =
            createSubstitutionServiceTemplate(componentName.get(), entry.getValue(), vspModelInfo,
                toscaServiceModel);
        generatedServiceTemplates.put(componentName.get(), substitutionServiceTemplate);
      }
    }

  }

  private Optional<String> getComponentNameFromVspModel(VspModelInfo vspModelInfo) {
    String componentName = null;
    if (MapUtils.isNotEmpty(vspModelInfo.getComponents())) {
      //supported for single component only
      componentName = vspModelInfo.getComponents().entrySet().iterator().next().getValue();
    }
    return Optional.ofNullable(componentName);
  }

  private ServiceTemplate createMainServiceTemplate(VspModelInfo vspModelInfo) {
    ServiceTemplate mainServiceTemplate = createInitMainServiceTemplate(vspModelInfo
        .getReleaseVendor());
    Map<String, String> components = vspModelInfo.getComponents();
    if (MapUtils.isNotEmpty(components)) {
      //Currently since only one component is supported we can fetch the component in this manner.
      // May be need to revisited for supporting multiple components
      String componentId = components.entrySet().iterator().next().getKey();
      createVnfConfigurationNodeTemplate(mainServiceTemplate, vspModelInfo);
      createVnfNodeTemplate(mainServiceTemplate, vspModelInfo, componentId);
    }
    return mainServiceTemplate;
  }

  private void createVnfConfigurationNodeTemplate(ServiceTemplate mainServiceTemplate,
                                                  VspModelInfo vspModelInfo) {
    Optional<String> componentName = getComponentNameFromVspModel(vspModelInfo);
    if (componentName.isPresent()) {
      NodeTemplate vnfConfigurationNodeTemplate = new NodeTemplate();
      vnfConfigurationNodeTemplate.setType(ToscaNodeType.VNF_CONFIG_NODE_TYPE);
      if (Objects.nonNull(vspModelInfo.getAllowedFlavors())) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(ALLOWED_FLAVORS_PROPERTY, vspModelInfo.getAllowedFlavors());
        vnfConfigurationNodeTemplate.setProperties(properties);
      }
      String nodeTemplateId = componentName.get() + VNF_CONFIG_NODE_TEMPLATE_ID_SUFFIX;
      DataModelUtil.addNodeTemplate(mainServiceTemplate, nodeTemplateId,
          vnfConfigurationNodeTemplate);
    }
  }

  private void createVnfNodeTemplate(ServiceTemplate mainServiceTemplate,
                                     VspModelInfo vspModelInfo, String componentId) {
    Optional<String> componentName = getComponentNameFromVspModel(vspModelInfo);
    if (componentName.isPresent()) {
      NodeTemplate vnfNodeTemplate = new NodeTemplate();
      vnfNodeTemplate.setType(ToscaNodeType.MULTIDEPLOYMENTFLAVOR_NODE_TYPE);
      List<String> directiveList = new ArrayList<>();
      directiveList.add(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
      vnfNodeTemplate.setDirectives(directiveList);
      vnfNodeTemplate.setProperties(getVnfNodeTemplateProperties(vspModelInfo, componentId));
      DataModelUtil
          .addSubstitutionFilteringProperty(getSubstitutionServiceTemplateFileName(componentName
                  .get()), vnfNodeTemplate, 1);
      //Enable below if we need "abstract_" as prefix like we have in Unified model
      //String nodeTemplateId =
      // Constants.ABSTRACT_NODE_TEMPLATE_ID_PREFIX + componentName + "VNF_NODE_TEMPLATE_ID_SUFFIX";
      String nodeTemplateId = componentName.get() + VNF_NODE_TEMPLATE_ID_SUFFIX;
      DataModelUtil.addNodeTemplate(mainServiceTemplate, nodeTemplateId,
          vnfNodeTemplate);
      abstractSubstitutionIdTypes.put(componentName.get(), ToscaNodeType
          .MULTIDEPLOYMENTFLAVOR_NODE_TYPE);
    }
  }

  private Map<String, Object> getVnfNodeTemplateProperties(VspModelInfo vspModelInfo,
                                                           String componentId) {
    Map<String, Object> properties = new LinkedHashMap<>();
    if (MapUtils.isNotEmpty(vspModelInfo.getMultiFlavorVfcImages())) {
      List<MultiFlavorVfcImage> componentImages =
          vspModelInfo.getMultiFlavorVfcImages().get(componentId);
      if (Objects.nonNull(componentImages)) {
        Map<String, MultiFlavorVfcImage> vfcImages = new HashMap<>();
        componentImages.stream()
            .forEach(multiFlavorVfcImage ->
                vfcImages.put(multiFlavorVfcImage.getSoftware_version(), multiFlavorVfcImage));
        properties.put(IMAGES_PROPERTY, vfcImages);
      }
    }
    return properties;
  }

  private String getSubstitutionServiceTemplateFileName(String componentName) {
    return componentName + TOSCA_SERVICE_TEMPLATE_FILE_NAME_SUFFIX;
  }

  private String getNodeTemplateId(String componentName, String idPrefix, String idSuffix) {
    StringBuilder builder = new StringBuilder();
    //builder.append(idPrefix);
    builder.append(componentName);
    builder.append(idSuffix);
    return builder.toString();
  }

  private ServiceTemplate createInitMainServiceTemplate(String releaseVendor) {
    ServiceTemplate mainServiceTemplate = new ServiceTemplate();
    Map<String, String> templateMetadata = new HashMap<>();
    templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, Constants.MAIN_TEMPLATE_NAME);
    if (Objects.nonNull(releaseVendor)) {
      templateMetadata.put(RELEASE_VENDOR, releaseVendor);
    }
    mainServiceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    mainServiceTemplate.setMetadata(templateMetadata);
    mainServiceTemplate.setTopology_template(new TopologyTemplate());
    mainServiceTemplate.setImports(getImports());
    return mainServiceTemplate;
  }

  private List<Map<String, Import>> getImports() {
    Map<String, Import> globalSubstitutionTypeImportMap = new HashMap<>();
    Import globalSubstitutionTypeImport = new Import();
    globalSubstitutionTypeImport.setFile(ToscaUtil.getServiceTemplateFileName(Constants
        .GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
    globalSubstitutionTypeImportMap.put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
        globalSubstitutionTypeImport);
    Map<String, Import> globalImports = new HashMap<>();
    List<Map<String, Import>> manualVspGlobalTypesImportList = GlobalTypesGenerator
        .getManualVspGlobalTypesImportList();
    manualVspGlobalTypesImportList.add(globalSubstitutionTypeImportMap);
    return manualVspGlobalTypesImportList;
  }

  private ServiceTemplate createSubstitutionServiceTemplate(String serviceTemplateName,
                                                            String substitutionNodeTypeId,
                                                            VspModelInfo vspModelInfo,
                                                            ToscaServiceModel toscaServiceModel) {
    ServiceTemplate substitutionServiceTemplate =
        createInitSubstitutionServiceTemplate(serviceTemplateName);
    createSubstitutionServiceTemplateComponents(substitutionServiceTemplate, vspModelInfo,
        toscaServiceModel);
    createSubstitutionServiceTemplateNics(substitutionServiceTemplate, vspModelInfo,
        toscaServiceModel);
    handleSubstitutionMapping(substitutionServiceTemplate, toscaServiceModel,
        substitutionNodeTypeId, serviceTemplateName);
    return substitutionServiceTemplate;
  }

  private void createSubstitutionServiceTemplateComponents(ServiceTemplate
                                                               substitutionServiceTemplate,
                                                           VspModelInfo vspModelInfo,
                                                           ToscaServiceModel toscaServiceModel) {
    Map<String, String> components = vspModelInfo.getComponents();
    if (MapUtils.isNotEmpty(components)) {
      for (String componentId : components.keySet()) {
        String componentName = components.get(componentId);
        String localNodeTypeId =
            createComponentDefinitionNodeTemplate(substitutionServiceTemplate, componentName);
        createLocalNodeType(substitutionServiceTemplate, localNodeTypeId);
      }
    }
  }

  private void createSubstitutionServiceTemplateNics(ServiceTemplate substitutionServiceTemplate,
                                                     VspModelInfo vspModelInfo,
                                                     ToscaServiceModel toscaServiceModel) {
    Map<String, List<Nic>> nics = vspModelInfo.getNics();
    if (MapUtils.isNotEmpty(nics)) {
      for (Map.Entry<String, List<Nic>> entry : nics.entrySet()) {
        String componentId = entry.getKey();
        String componentNodeTemplateId = getSubstitutionComponentNodeTemplateId(
            vspModelInfo.getComponents().get(componentId));
        List<Nic> nicList = entry.getValue();
        if (CollectionUtils.isNotEmpty(nicList)) {
          for (Nic nic : nicList) {
            NodeTemplate nicNodeTemplate = new NodeTemplate();
            nicNodeTemplate.setType(ToscaNodeType.NETWORK_PORT);
            DataModelUtil.addBindingReqFromPortToCompute(componentNodeTemplateId, nicNodeTemplate);
            DataModelUtil.addNodeTemplate(substitutionServiceTemplate,
                getNicNodeTemplateId(nic.getName(), nic.getNetworkType()), nicNodeTemplate);
          }
        }
      }
    }
  }

  private String getSubstitutionComponentNodeTemplateId(String componentName) {
    //TODO: Confirm if anything else is needed here
    return componentName;
  }

  private String getNicNodeTemplateId(String nicName, NetworkType nicNetworkType) {
    StringBuilder builder = new StringBuilder();
    builder.append(nicName);
    if (nicNetworkType == NetworkType.External) {
      builder.append(PORT_TYPE_EXTERNAL_NODE_TEMPLATE_SUFFIX);
    } else if (nicNetworkType == NetworkType.Internal) {
      builder.append(PORT_TYPE_INTERNAL_NODE_TEMPLATE_SUFFIX);
    }
    builder.append(PORT_NODE_TEMPLATE_ID_SUFFIX);
    return builder.toString();
  }

  private String createComponentDefinitionNodeTemplate(ServiceTemplate substitutionServiceTemplate,
                                                       String componentName) {
    NodeTemplate nodeTemplate = new NodeTemplate();
    String localNodeTypeId = getLocalNodeTypeId(componentName);
    nodeTemplate.setType(localNodeTypeId);
    DataModelUtil.addNodeTemplate(substitutionServiceTemplate, componentName, nodeTemplate);
    return localNodeTypeId;
  }

  private void createLocalNodeType(ServiceTemplate substitutionServiceTemplate,
                                   String localNodeTypeId) {
    NodeType localNodeType = new NodeType();
    localNodeType.setDerived_from(ToscaNodeType.COMPUTE);
    DataModelUtil.addNodeType(substitutionServiceTemplate, localNodeTypeId, localNodeType );
  }

  private String getLocalNodeTypeId(String componentName) {
    return ToscaNodeType.VFC_NODE_TYPE_PREFIX + componentName;
  }

  private ServiceTemplate createInitSubstitutionServiceTemplate(String serviceTemplateName) {
    ServiceTemplate substitutionServiceTemplate = new ServiceTemplate();
    Map<String, String> templateMetadata = new HashMap<>();
    substitutionServiceTemplate.setTosca_definitions_version(ToscaConstants
        .TOSCA_DEFINITIONS_VERSION);
    templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, serviceTemplateName);
    substitutionServiceTemplate.setMetadata(templateMetadata);
    substitutionServiceTemplate.setTopology_template(new TopologyTemplate());
    substitutionServiceTemplate.setImports(getImports());
    return substitutionServiceTemplate;
  }

  private void handleSubstitutionMapping(ServiceTemplate substitutionServiceTemplate,
                                         ToscaServiceModel toscaServiceModel,
                                         String substitutionNodeTypeId,
                                         String componentName) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    ServiceTemplate globalSubstitutionServiceTemplate = fetchGlobalSubstitutionServiceTemplate();
    NodeType substitutionNodeType =
        createGlobalSubstitutionNodeType(substitutionServiceTemplate, componentName);
    DataModelUtil.addNodeType(globalSubstitutionServiceTemplate, substitutionNodeTypeId,
        substitutionNodeType);
    Map<String, Map<String, List<String>>> substitutionMapping =
        GeneratorUtils.getSubstitutionNodeTypeExposedConnectionPoints(substitutionNodeType,
            substitutionServiceTemplate, toscaServiceModel);
    if (Objects.nonNull(substitutionMapping)) {
      //add substitution mapping after capability and requirement expose calculation
      substitutionServiceTemplate.getTopology_template().setSubstitution_mappings(
          DataModelUtil.createSubstitutionTemplateSubMapping(substitutionNodeTypeId,
              substitutionNodeType, substitutionMapping));
    }
  }

  //*************** CREATE GLOBAL SUBSTITUTION SERVICE TEMPLATE **********************

  private ServiceTemplate createGlobalSubstitutionServiceTemplate(ServiceTemplate
                                                                      substitutionServiceTemplate,
                                                                  String componentName) {
    ServiceTemplate globalSubstitutionServiceTemplate = fetchGlobalSubstitutionServiceTemplate();
    NodeType substitutionNodeType =
        createGlobalSubstitutionNodeType(substitutionServiceTemplate, componentName);
    String substitutionNodeTypeId = getSubstitutionNodeTypeId(componentName);
    DataModelUtil.addNodeType(globalSubstitutionServiceTemplate, substitutionNodeTypeId,
        substitutionNodeType);
    return globalSubstitutionServiceTemplate;
  }

  private ServiceTemplate createInitGlobalSubstitutionServiceTemplate() {
    ServiceTemplate globalSubstitutionServiceTemplate = new ServiceTemplate();
    Map<String, String> templateMetadata = new HashMap<>();
    globalSubstitutionServiceTemplate.setTosca_definitions_version(ToscaConstants
        .TOSCA_DEFINITIONS_VERSION);
    templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME,
        Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    globalSubstitutionServiceTemplate.setMetadata(templateMetadata);
    globalSubstitutionServiceTemplate.setImports(getImports());
    return globalSubstitutionServiceTemplate;
  }

  private NodeType createGlobalSubstitutionNodeType(ServiceTemplate substitutionServiceTemplate,
                                                String componentName) {
    NodeType substitutionNodeType = new ToscaAnalyzerServiceImpl()
        .createInitSubstitutionNodeType(substitutionServiceTemplate,
            ToscaNodeType.MULTIFLAVOR_VFC_NODE_TYPE);
    substitutionNodeType.setProperties(
        getManualVspSubstitutionNodeTypeProperties(substitutionNodeType, componentName));
    return substitutionNodeType;
  }

  private Map<String, PropertyDefinition> getManualVspSubstitutionNodeTypeProperties(
      NodeType substitutionNodeType, String componentName) {
    //Create num_cpus property
    PropertyDefinition numCpus = new PropertyDefinition();
    numCpus.setType(PropertyType.INTEGER.getDisplayName());
    numCpus.setDescription(NUM_CPUS_PROP_DESC_PREFIX + componentName);
    numCpus.setRequired(true);
    //Create disk_size property
    PropertyDefinition diskSize = new PropertyDefinition();
    diskSize.setType(PropertyType.SCALAR_UNIT_SIZE.getDisplayName());
    diskSize.setDescription(DISK_SIZE_PROP_DESC_PREFIX + componentName);
    diskSize.setRequired(true);
    //Create mem_size property
    PropertyDefinition memSize = new PropertyDefinition();
    memSize.setType(PropertyType.SCALAR_UNIT_SIZE.getDisplayName());
    memSize.setDescription(MEM_SIZE_PROP_DESC_PREFIX + componentName);
    memSize.setRequired(true);

    Map<String, PropertyDefinition> manualVspProperties = new LinkedHashMap<>();
    manualVspProperties.put(NUM_CPUS, numCpus);
    manualVspProperties.put(DISK_SIZE, diskSize);
    manualVspProperties.put(MEM_SIZE, memSize);

    return manualVspProperties;
  }

  private String getSubstitutionNodeTypeId(String componentName) {
    return ToscaNodeType.MULTIDEPLOYMENTFLAVOR_NODE_TYPE + "." + componentName;
  }

  /**
   * Fetch global substitution service template service template.
   *
   * @return the global substitution service template
   */
  private ServiceTemplate fetchGlobalSubstitutionServiceTemplate() {
    ServiceTemplate globalSubstitutionServiceTemplate =
        generatedServiceTemplates.get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    if (Objects.isNull(globalSubstitutionServiceTemplate)) {
      globalSubstitutionServiceTemplate = createInitGlobalSubstitutionServiceTemplate();
      generatedServiceTemplates.put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
              globalSubstitutionServiceTemplate);
    }
    return globalSubstitutionServiceTemplate;
  }
}