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

package org.openecomp.sdc.translator.datatypes.heattotosca;



import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.collections.MapUtils;

import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestFile;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionEntity;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedSubstitutionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeConsolidationDataHandler;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationDataHandler;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortConsolidationDataHandler;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedConsolidationDataHandler;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceConsolidationDataHandler;
import org.openecomp.sdc.translator.services.heattotosca.ConfigConstants;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class TranslationContext {

  private ManifestFile manifest;


  private static final Map<String, Map<String, Map<String, String>>> translationMapping;
  private static final Map<String, ServiceTemplate> globalServiceTemplates;
  private static final Map<String, ImplementationConfiguration> nameExtractorImplMap;
  private static final List<String> vfcGroupSubInterfaceExposedProperties;
  private static final List<String> enrichPortResourceProperties;
  private static final ImplementationConfiguration vfcInstanceGroupConfiguration;

  private static Map<String, ImplementationConfiguration> supportedConsolidationComputeResources;
  private static Map<String, ImplementationConfiguration> supportedConsolidationPortResources;

  private final FileContentHandler files = new FileContentHandler();
  private final Map<String, FileData.Type> manifestFiles = new HashMap<>();
  //Key - file name, value - file type
  private final Set<String> nestedHeatsFiles = new HashSet<>();
  private final FileContentHandler externalArtifacts = new FileContentHandler();
  // Key - heat file name,value - set of heat resource ids which were translated
  private final Map<String, Set<String>> translatedResources = new HashMap<>();
  // Key - heat file name, value - translated Node template id
  private final Map<String, Set<String>> heatStackGroupMembers = new HashMap<>();
  // Key - heat file name, value - Map with Key - heat resource Id, Value - tosca entity template id
  private final Map<String, Map<String, String>> translatedIds = new HashMap<>();
  // key - service template type, value - translated service templates
  private final Map<String, ServiceTemplate> translatedServiceTemplates = new HashMap<>();
  //key - heat param name, value - shared resource data
  private final Map<String, TranslatedHeatResource> heatSharedResourcesByParam = new HashMap<>();
  //key - translated substitute service template file name, value - source nested heat file name
  private final Map<String, String> nestedHeatFileName = new HashMap<>();
  //Key - heat file name,value - Map eith key - heat pseudo param name,
  // value - translated tosca parameter name
  private final Map<String, Map<String, String>> usedHeatPseudoParams = new HashMap<>();
  //Consolidation data gathered for Unified TOSCA model
  private ConsolidationData consolidationData = new ConsolidationData();
  private Map<String, UnifiedSubstitutionData> unifiedSubstitutionData = new HashMap<>();
  private final Set<String> unifiedHandledServiceTemplates = new HashSet<>();

  private final Map<String, Map<String, Map<String, Integer>>> requirementIdAppearanceInNodeTemplate = new HashMap<>();

  private final Set<String> serviceTemplatesWithoutNodeTemplateSection = new HashSet<>();

  private final Set<String> nodeTemplateIdsPointingToStWithoutNodeTemplates = new HashSet<>();

  //Key - service template name, value - Map of key: node template id, value: properties with %index%
  private final Map<String, ListMultimap<String, String>> indexVarProperties = new HashMap<>();

  static {
    Configuration config = ConfigurationManager.lookup();
    translationMapping =
            config.generateMap(ConfigConstants.MAPPING_NAMESPACE, ConfigConstants.RESOURCE_MAPPING_KEY);
    try {
      globalServiceTemplates =
              GlobalTypesGenerator.getGlobalTypesServiceTemplate(OnboardingTypesEnum.ZIP);
    } catch (Exception exc) {
      throw new RuntimeException("Failed to load GlobalTypes", exc);
    }
    nameExtractorImplMap = config.populateMap(ConfigConstants.TRANSLATOR_NAMESPACE,
            ConfigConstants.NAMING_CONVENTION_EXTRACTOR_IMPL_KEY, ImplementationConfiguration.class);
    supportedConsolidationComputeResources = config.populateMap(ConfigConstants.MANDATORY_UNIFIED_MODEL_NAMESPACE,
            ConfigConstants.SUPPORTED_CONSOLIDATION_COMPUTE_RESOURCES_KEY, ImplementationConfiguration.class);
    supportedConsolidationPortResources = config.populateMap(ConfigConstants.MANDATORY_UNIFIED_MODEL_NAMESPACE,
            ConfigConstants.SUPPORTED_CONSOLIDATION_PORT_RESOURCES_KEY, ImplementationConfiguration.class);
    enrichPortResourceProperties = config.getAsStringValues(ConfigConstants.MANDATORY_UNIFIED_MODEL_NAMESPACE,
            ConfigConstants.ENRICH_PORT_RESOURCE_PROP);
    vfcInstanceGroupConfiguration = getVfcInstanceGroupConfiguration(config);
    vfcGroupSubInterfaceExposedProperties = config.getAsStringValues(ConfigConstants.UNIFIED_MODEL_NAMESPACE,
            ConfigConstants.FULL_EXPOSED_PROPERTIES_KEY);
  }


  private static ImplementationConfiguration getVfcInstanceGroupConfiguration(Configuration config) {
    Map<String, ImplementationConfiguration> supportedUnifiedModelProperties =
            config.populateMap(ConfigConstants.UNIFIED_MODEL_NAMESPACE, ConfigConstants.UNIFIED_MODEL_IMPL_KEY,
                    ImplementationConfiguration.class);
    return MapUtils.isEmpty(supportedUnifiedModelProperties) ? null : supportedUnifiedModelProperties.get(ConfigConstants.VFC_INSTANCE_GROUP_KEY);
  }

  public static boolean isVfcInstanceGroupingEnabled() {
    return Objects.nonNull(vfcInstanceGroupConfiguration) && vfcInstanceGroupConfiguration.isEnable();
  }

  public static List<String> getExposedVfcInstanceGroupingProperties() {
    return vfcGroupSubInterfaceExposedProperties;
  }

  public static List<String> getEnrichPortResourceProperties() {
    return enrichPortResourceProperties;
  }

  public static Map<String, ImplementationConfiguration> getSupportedConsolidationComputeResources() {
    return supportedConsolidationComputeResources;
  }

  public static void setSupportedConsolidationComputeResources(Map<String, ImplementationConfiguration> supportedConsolidationComputeResources) {
    TranslationContext.supportedConsolidationComputeResources = supportedConsolidationComputeResources;
  }

  public static Map<String, ImplementationConfiguration> getSupportedConsolidationPortResources() {
    return supportedConsolidationPortResources;
  }

  public static void setSupportedConsolidationPortResources(Map<String, ImplementationConfiguration> supportedConsolidationPortResources) {
    TranslationContext.supportedConsolidationPortResources = supportedConsolidationPortResources;
  }

  /**
   * Get nameExtractor implemetation class instance.
   *
   * @param extractorImplKey configuration key for the implementation class
   * @return implemetation class instance
   */
  public static NameExtractor getNameExtractorImpl(String extractorImplKey) {
    String nameExtractorImplClassName = nameExtractorImplMap.get(extractorImplKey).getImplementationClass();

    return CommonMethods.newInstance(nameExtractorImplClassName, NameExtractor.class);
  }

  public Map<String, UnifiedSubstitutionData> getUnifiedSubstitutionData() {
    return unifiedSubstitutionData;
  }

  public void setUnifiedSubstitutionData(Map<String, UnifiedSubstitutionData> unifiedSubstitutionData) {
    this.unifiedSubstitutionData = unifiedSubstitutionData;
  }

  public void addCleanedNodeTemplate(String serviceTemplateName, String nodeTemplateId, UnifiedCompositionEntity unifiedCompositionEntity,
                                            NodeTemplate nodeTemplate) {
    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(serviceTemplateName)
                                .addCleanedNodeTemplate(nodeTemplateId, unifiedCompositionEntity, nodeTemplate);
  }

  public Optional<List<String>> getIndexVarProperties(String serviceTemplateName, String nodeTemplateId) {
    ListMultimap<String, String> serviceTemplateIndexVarProperties = this.indexVarProperties.get(serviceTemplateName);
    if (Objects.nonNull(serviceTemplateIndexVarProperties)) {
      return Optional.of(this.indexVarProperties.get(serviceTemplateName).get(nodeTemplateId));
    }
    return Optional.empty();
  }

  public void addIndexVarProperties(String serviceTemplateName, String nodeTemplateId, List<String> indexVarProperties) {
    this.indexVarProperties.putIfAbsent(serviceTemplateName, ArrayListMultimap.create());
    this.indexVarProperties.get(serviceTemplateName).putAll(nodeTemplateId, indexVarProperties);
  }

  public NodeTemplate getCleanedNodeTemplate(String serviceTemplateName, String nodeTemplateId) {
    return this.unifiedSubstitutionData.get(serviceTemplateName).getCleanedNodeTemplate(nodeTemplateId);
  }

  public void addUnifiedNestedNodeTemplateId(String serviceTemplateName, String nestedNodeTemplateId, String unifiedNestedNodeTemplateId) {
    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(serviceTemplateName)
                                .addUnifiedNestedNodeTemplateId(nestedNodeTemplateId, unifiedNestedNodeTemplateId);
  }

  public Optional<String> getUnifiedNestedNodeTemplateId(String serviceTemplateName, String nestedNodeTemplateId) {
    return this.unifiedSubstitutionData.get(serviceTemplateName) == null ? Optional.empty() :
                   this.unifiedSubstitutionData.get(serviceTemplateName).getUnifiedNestedNodeTemplateId(nestedNodeTemplateId);
  }

  public void addUnifiedNestedNodeTypeId(String serviceTemplateName, String nestedNodeTypeId, String unifiedNestedNodeTypeId) {
    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(serviceTemplateName)
                                .addUnifiedNestedNodeTypeId(nestedNodeTypeId, unifiedNestedNodeTypeId);
  }

  public Optional<String> getUnifiedNestedNodeTypeId(String serviceTemplateName, String nestedNodeTemplateId) {
    UnifiedSubstitutionData substitutionData = this.unifiedSubstitutionData.get(serviceTemplateName);
    return substitutionData == null ? Optional.empty() :
                   substitutionData.getUnifiedNestedNodeTypeId(nestedNodeTemplateId);
  }

  public ConsolidationData getConsolidationData() {
    return consolidationData;
  }

  public void setConsolidationData(ConsolidationData consolidationData) {
    this.consolidationData = consolidationData;
  }

  public Optional<ConsolidationDataHandler> getConsolidationDataHandler(ConsolidationEntityType type) {
    return consolidationData.getConsolidationDataHandler(type);
  }

  public ComputeConsolidationDataHandler getComputeConsolidationDataHandler() {
    return consolidationData.getComputeConsolidationDataHandler();
  }

  public PortConsolidationDataHandler getPortConsolidationDataHandler() {
    return consolidationData.getPortConsolidationDataHandler();
  }

  public NestedConsolidationDataHandler getNestedConsolidationDataHandler() {
    return consolidationData.getNestedConsolidationDataHandler();
  }

  public SubInterfaceConsolidationDataHandler getSubInterfaceConsolidationDataHandler() {
    return consolidationData.getSubInterfaceConsolidationDataHandler();
  }

  public void addManifestFile(String fileName, FileData.Type fileType) {
    this.manifestFiles.put(fileName, fileType);
  }

  public Set<String> getNestedHeatsFiles() {
    return nestedHeatsFiles;
  }

  public Map<String, Set<String>> getHeatStackGroupMembers() {
    return heatStackGroupMembers;
  }

  public FileContentHandler getFiles() {
    return files;
  }

  public void setFiles(Map<String, byte[]> files) {
    this.files.setFiles(files);
  }

  public InputStream getFileContentAsStream(final String fileName) {
    return files.getFileContentAsStream(fileName);
  }

  public void addFile(String name, byte[] content) {
    files.addFile(name, content);
  }

  public ManifestFile getManifest() {
    return manifest;
  }

  public void setManifest(ManifestFile manifest) {
    this.manifest = manifest;
  }

  public Map<String, Set<String>> getTranslatedResources() {
    return translatedResources;
  }

  public Map<String, Map<String, String>> getTranslatedIds() {
    return translatedIds;
  }

  public Set<String> getAllTranslatedResourceIdsFromDiffNestedFiles(String
                                                                        nestedHeatFileNameToSkip) {
    Set<String> allTranslatedResourceIds = new HashSet<>();

    this.translatedIds.entrySet().stream().filter(
        heatFileNameToTranslatedIdsEntry -> !heatFileNameToTranslatedIdsEntry.getKey()
            .equals(nestedHeatFileNameToSkip)).forEach(heatFileNameToTranslatedIdsEntry ->
      allTranslatedResourceIds.addAll(heatFileNameToTranslatedIdsEntry.getValue().keySet())
    );

    return allTranslatedResourceIds;
  }

  // get tosca name from mapping configuration file
  //element type - parameter/attribute
  // element name - heat parameter/attribute name
  //return value - tosca parameter/attribute name
  public String getElementMapping(String resourceType, String elementType, String elementName) {
    if (Objects.isNull(translationMapping.get(resourceType))) {
      return null;
    }
    if (Objects.isNull(translationMapping.get(resourceType).get(elementType))) {
      return null;
    }
    return translationMapping.get(resourceType).get(elementType).get(elementName);
  }

  public Map<String, String> getElementMapping(String resourceType, String elementType) {
    if (Objects.isNull(translationMapping.get(resourceType))) {
      return null;
    }
    return translationMapping.get(resourceType).get(elementType);
  }

  public Set<String> getElementSet(String resourceType, String elementType) {
    if (Objects.isNull(translationMapping.get(resourceType))) {
      return new HashSet<>();
    }
    if (Objects.isNull(translationMapping.get(resourceType).get(elementType))) {
      return new HashSet<>();
    }
    return translationMapping.get(resourceType).get(elementType).keySet();
  }

  public Map<String, ServiceTemplate> getTranslatedServiceTemplates() {
    return translatedServiceTemplates;
  }

  public ServiceTemplate getGlobalSubstitutionServiceTemplate() {
    return getTranslatedServiceTemplates().get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
  }

  public FileContentHandler getExternalArtifacts() {
    return externalArtifacts;
  }

  public void addExternalArtifacts(String name, byte[] content) {
    this.externalArtifacts.addFile(name, content);
  }

  public Map<String, TranslatedHeatResource> getHeatSharedResourcesByParam() {
    return heatSharedResourcesByParam;
  }

  public void addHeatSharedResourcesByParam(String parameterName, String resourceId,
                                            Resource resource) {
    this.addHeatSharedResourcesByParam(parameterName,
        new TranslatedHeatResource(resourceId, resource));
  }

  private void addHeatSharedResourcesByParam(String parameterName,
                                             TranslatedHeatResource translatedHeatResource) {
    this.heatSharedResourcesByParam.put(parameterName, translatedHeatResource);
  }

  public Map<String, ServiceTemplate> getGlobalServiceTemplates() {
    return globalServiceTemplates;
  }

  public Map<String, String> getNestedHeatFileName() {
    return nestedHeatFileName;
  }

  public void addNestedHeatFileName(String substituteServiceTempalteName,
                                    String nestedHeatFileName) {
    this.nestedHeatFileName.put(substituteServiceTempalteName, nestedHeatFileName);
  }

  public Map<String, Map<String, String>> getUsedHeatPseudoParams() {
    return usedHeatPseudoParams;
  }

  public void addUsedHeatPseudoParams(String heatFileName, String heatPseudoParam, String
      translatedToscaParam) {
    if (Objects.isNull(this.usedHeatPseudoParams.get(heatFileName))) {
      this.usedHeatPseudoParams.put(heatFileName, new HashMap<>());
    }
    this.usedHeatPseudoParams.get(heatFileName).put(heatPseudoParam, translatedToscaParam);
  }

  public Set<String> getTranslatedResourceIdsFromOtherFiles(String fileNameToIgnore){
    if(MapUtils.isEmpty(this.translatedResources)){
      return new HashSet<>();
    }

    Set<String> translatedResourceIds = new HashSet<>();

    this.translatedResources.entrySet().stream().filter(entry -> !entry.getKey().equals(fileNameToIgnore))
        .forEach(entry -> translatedResourceIds.addAll(entry.getValue()));

    return translatedResourceIds;
  }

  /**
   * Add the unified substitution data info in context. Contains a mapping of original node
   * template id and the new node template id in the abstract substitute
   *
   * @param serviceTemplateFileName the service template file name
   * @param originalNodeTemplateId  the original node template id
   * @param abstractNodeTemplateId  the node template id in the abstract substitute
   */
  public void addUnifiedSubstitutionData(String serviceTemplateFileName,
                                         String originalNodeTemplateId,
                                         String abstractNodeTemplateId) {

    Map<String, String> nodeAbstractNodeTemplateIdMap = this.getUnifiedSubstitutionData()
        .computeIfAbsent(serviceTemplateFileName, k -> new UnifiedSubstitutionData())
        .getNodesRelatedAbstractNode();

    if (nodeAbstractNodeTemplateIdMap == null) {
      nodeAbstractNodeTemplateIdMap = new HashMap<>();
    }

    if(nodeAbstractNodeTemplateIdMap.containsKey(originalNodeTemplateId)){
      throw new CoreException(new DuplicateResourceIdsInDifferentFilesErrorBuilder(originalNodeTemplateId).build());
    }
    nodeAbstractNodeTemplateIdMap.put(originalNodeTemplateId, abstractNodeTemplateId);
    this.getUnifiedSubstitutionData().get(serviceTemplateFileName).setNodesRelatedAbstractNode(
        nodeAbstractNodeTemplateIdMap);
  }

  /**
   * Add the unified substitution data info in context. Contains a mapping of original node
   * template id and the new node template id in the abstract substitute
   *
   * @param serviceTemplateFileName                   the service template file name
   * @param originalNodeTemplateId                    the original node template id
   * @param substitutionServiceTemplateNodeTemplateId the node template id in the substitution
   *                                                  service template
   */
  public void addSubstitutionServiceTemplateUnifiedSubstitutionData(
      String serviceTemplateFileName,
      String originalNodeTemplateId,
      String substitutionServiceTemplateNodeTemplateId) {

    Map<String, String> nodesRelatedSubstitutionServiceTemplateNodeTemplateIdMap = this
        .getUnifiedSubstitutionData()
        .computeIfAbsent(serviceTemplateFileName, k -> new UnifiedSubstitutionData())
        .getNodesRelatedSubstitutionServiceTemplateNode();

    if (nodesRelatedSubstitutionServiceTemplateNodeTemplateIdMap == null) {
      nodesRelatedSubstitutionServiceTemplateNodeTemplateIdMap = new HashMap<>();
    }
    nodesRelatedSubstitutionServiceTemplateNodeTemplateIdMap.put(originalNodeTemplateId,
        substitutionServiceTemplateNodeTemplateId);
    this.getUnifiedSubstitutionData().get(serviceTemplateFileName)
        .setNodesRelatedSubstitutionServiceTemplateNode(
            nodesRelatedSubstitutionServiceTemplateNodeTemplateIdMap);
  }

  /**
   * Get unified abstract node template which is mapped to the input node template id.
   *
   * @param serviceTemplate the service template
   * @param nodeTemplateId  the node template id
   */
  public String getUnifiedAbstractNodeTemplateId(ServiceTemplate serviceTemplate,
                                                 String nodeTemplateId) {
    UnifiedSubstitutionData unifiedSubsData =
        this.unifiedSubstitutionData.get(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
    return unifiedSubsData.getNodesRelatedAbstractNode().get(nodeTemplateId);
  }

  /**
   * Get unified node template in the substitution service template which is mapped to the
   * original input node template id.
   *
   * @param serviceTemplate the service template
   * @param nodeTemplateId  the node template id
   */
  public String getUnifiedSubstitutionNodeTemplateId(ServiceTemplate serviceTemplate,
                                                     String nodeTemplateId) {
    UnifiedSubstitutionData unifiedSubsData =
        this.unifiedSubstitutionData.get(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
    return unifiedSubsData.getNodesRelatedSubstitutionServiceTemplateNode()
        .get(nodeTemplateId);
  }

  public int getHandledNestedComputeNodeTemplateIndex(String serviceTemplateName,
                                                      String computeType) {
    return this.unifiedSubstitutionData.get(serviceTemplateName)
        .getHandledNestedComputeNodeTemplateIndex(computeType);
  }

  public void updateHandledComputeType(String serviceTemplateName,
                                       String handledComputeType,
                                       String nestedServiceTemplateFileName) {
    String globalSTName =
        ToscaUtil.getServiceTemplateFileName(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    this.unifiedSubstitutionData.putIfAbsent(
        globalSTName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(globalSTName)
        .addHandledComputeType(handledComputeType);
    this.unifiedSubstitutionData.get(globalSTName).addHandlesNestedServiceTemplate(nestedServiceTemplateFileName);

    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(serviceTemplateName).addHandlesNestedServiceTemplate(nestedServiceTemplateFileName);
  }

  public void addHandledComputeTypeInServiceTemplate(String serviceTemplateName,
                                                     String handledComputeType){
    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(serviceTemplateName).addHandledComputeType(handledComputeType);
  }

  public boolean isComputeTypeHandledInServiceTemplate(String serviceTemplateName,
                                                       String computeType) {
    return !Objects.isNull(this.unifiedSubstitutionData.get(serviceTemplateName))
        && this.unifiedSubstitutionData.get(serviceTemplateName)
        .isComputeTypeHandledInServiceTemplate(computeType);
  }

  public boolean isNestedServiceTemplateWasHandled(String serviceTemplateName,
                                                   String nestedServiceTemplateFileName) {
    if (Objects.isNull(this.unifiedSubstitutionData.get(serviceTemplateName))) {
      return false;
    }
    return this.unifiedSubstitutionData.get(serviceTemplateName)
        .isNestedServiceTemplateWasHandled(nestedServiceTemplateFileName);
  }

  public Set<String> getAllRelatedNestedNodeTypeIds() {
    String globalName = "GlobalSubstitutionTypes";
    if (Objects.isNull(this.unifiedSubstitutionData)
        || Objects.isNull(this.unifiedSubstitutionData.get(globalName))) {
      return new HashSet<>();
    }

    return this.unifiedSubstitutionData.get(globalName).getAllRelatedNestedNodeTypeIds();
  }

  public boolean isUnifiedHandledServiceTemplate(ServiceTemplate serviceTemplate) {
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    return unifiedHandledServiceTemplates.contains(serviceTemplateFileName);
  }



  public void addUnifiedHandledServiceTeamplte(ServiceTemplate serviceTemplate) {
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    this.unifiedHandledServiceTemplates.add(serviceTemplateFileName);
  }

  public boolean isNestedNodeWasHandled(String serviceTemplateName,
                                        String nestedNodeTemplateId) {
    if (Objects.isNull(this.unifiedSubstitutionData.get(serviceTemplateName))) {
      return false;
    }
    return this.unifiedSubstitutionData.get(serviceTemplateName)
        .isNestedNodeWasHandled(nestedNodeTemplateId);
  }

  public void addNestedNodeAsHandled(String serviceTemplateName,
                                     String nestedNodeTemplateId) {
    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(serviceTemplateName)
        .addHandledNestedNodes(nestedNodeTemplateId);
  }

  public void updateUsedTimesForNestedComputeNodeType(String serviceTemplateName,
                                                      String computeType) {
    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());

    this.unifiedSubstitutionData.get(serviceTemplateName)
        .updateUsedTimesForNestedComputeNodeType(computeType);
  }

  public int getGlobalNodeTypeIndex(String serviceTemplateName,
                                    String computeType) {
    if (Objects.isNull(this.unifiedSubstitutionData.get(serviceTemplateName))) {
      return 0;
    }
    return this.unifiedSubstitutionData.get(serviceTemplateName).getGlobalNodeTypeIndex
        (computeType);
  }

  public void addNewPropertyIdToNodeTemplate(String serviceTemplateName,
                                             String newPropertyId,
                                             Object origPropertyValue){
    this.unifiedSubstitutionData.putIfAbsent(serviceTemplateName, new UnifiedSubstitutionData());
    this.unifiedSubstitutionData.get(serviceTemplateName).addNewPropertyIdToNodeTemplate(
        newPropertyId, origPropertyValue);
  }

  public Optional<Object> getNewPropertyInputParamId(String serviceTemplateName,
                                                     String newPropertyId){
    if(Objects.isNull(this.unifiedSubstitutionData.get(serviceTemplateName))){
      return Optional.empty();
    }

    return this.unifiedSubstitutionData.get(serviceTemplateName).getNewPropertyInputParam
        (newPropertyId);
  }

  public Map<String, Object> getAllNewPropertyInputParamIdsPerNodeTenplateId(String serviceTemplateName){
    if(Objects.isNull(this.unifiedSubstitutionData.get(serviceTemplateName))){
      return new HashMap<>();
    }

    return this.unifiedSubstitutionData.get(serviceTemplateName).getAllNewPropertyInputParamIds();

  }

  public boolean isServiceTemplateWithoutNodeTemplatesSection(String serviceTemplateName){
    return Objects.nonNull(serviceTemplateName)
          && serviceTemplatesWithoutNodeTemplateSection.contains(serviceTemplateName);
  }

  public void addServiceTemplateWithoutNodeTemplates(String serviceTemplateName){
    this.serviceTemplatesWithoutNodeTemplateSection.add(serviceTemplateName);
  }

  public void addNestedNodeTemplateIdPointsToStWithoutNodeTemplates(String nodeTemplateId){
    this.nodeTemplateIdsPointingToStWithoutNodeTemplates.add(nodeTemplateId);
  }

  public boolean isNodeTemplateIdPointsToStWithoutNodeTemplates(String nodeTemplateId){
    return Objects.nonNull(nodeTemplateId)
        && nodeTemplateIdsPointingToStWithoutNodeTemplates.contains(nodeTemplateId);
  }

  public void updateRequirementAssignmentIdIndex(String serviceTemplateName,
                                                 String nodeTemplateId,
                                                 String requirementId){
    requirementIdAppearanceInNodeTemplate.putIfAbsent(serviceTemplateName, new HashMap<>());
    requirementIdAppearanceInNodeTemplate
        .get(serviceTemplateName).putIfAbsent(nodeTemplateId, new HashMap<>());

    Map<String, Integer> requirementIdToAppearance =
        requirementIdAppearanceInNodeTemplate.get(serviceTemplateName).get(nodeTemplateId);

    if(requirementIdToAppearance.containsKey(requirementId)){
      requirementIdToAppearance
          .put(requirementId, requirementIdToAppearance.get(requirementId) + 1);
    } else {
      requirementIdToAppearance.put(requirementId, 0);
    }
  }

}
