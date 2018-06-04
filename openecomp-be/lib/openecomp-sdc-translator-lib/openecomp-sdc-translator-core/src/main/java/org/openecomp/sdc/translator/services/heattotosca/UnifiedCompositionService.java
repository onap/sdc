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

package org.openecomp.sdc.translator.services.heattotosca;

import static org.openecomp.sdc.tosca.datatypes.ToscaFunctions.GET_INPUT;
import static org.openecomp.sdc.tosca.datatypes.ToscaNodeType.GROUP_TYPE_PREFIX;
import static org.openecomp.sdc.tosca.datatypes.ToscaNodeType.VFC_INSTANCE_GROUP;
import static org.openecomp.sdc.tosca.services.DataModelUtil.getClonedObject;
import static org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionEntity.COMPUTE;
import static org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionEntity.PORT;
import static org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionEntity.SUB_INTERFACE;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.ABSTRACT_NODE_TEMPLATE_ID_PREFIX;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.COMPUTE_IDENTICAL_VALUE_PROPERTY_PREFIX;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.COMPUTE_IDENTICAL_VALUE_PROPERTY_SUFFIX;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.GROUP;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.PORT_IDENTICAL_VALUE_PROPERTY_PREFIX;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.SUB_INTERFACE_PROPERTY_VALUE_PREFIX;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.SUB_INTERFACE_ROLE;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.VFC_PARENT_PORT_ROLE;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getComputeTypeSuffix;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getConnectedComputeConsolidationData;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getNewComputeNodeTemplateId;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getNewPortNodeTemplateId;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getNewSubInterfaceNodeTemplateId;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getSubInterfaceTemplateConsolidationDataList;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getSubInterfaceTypeSuffix;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.onap.sdc.tosca.datatypes.model.GroupDefinition;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyType;
import org.onap.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.onap.sdc.tosca.datatypes.model.heatextend.PropertyTypeExt;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionEntity;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionMode;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedSubstitutionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.CommandImplNames;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.UnifiedSubstitutionNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.EntityConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.RequirementAssignmentData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;

public class UnifiedCompositionService {

  private static final Map<String, ImplementationConfiguration> unifiedCompositionImplMap;

  private static final EnumMap<UnifiedCompositionEntity, String> unifiedSubstitutionNodeTemplateIdGeneratorImplMap;
  private static final String SUB_INTERFACE_INDICATOR_PROPERTY = "subinterface_indicator";
  private final ConsolidationService consolidationService = new ConsolidationService();

  static {
    Configuration config = ConfigurationManager.lookup();
    unifiedCompositionImplMap =
            config.populateMap(ConfigConstants.MANDATORY_UNIFIED_MODEL_NAMESPACE,
                    ConfigConstants.UNIFIED_COMPOSITION_IMPL_KEY, ImplementationConfiguration.class);
    unifiedSubstitutionNodeTemplateIdGeneratorImplMap = new EnumMap<>(UnifiedCompositionEntity.class);
    initNodeTemplateIdGeneratorImplMap();
  }

  private static void initNodeTemplateIdGeneratorImplMap() {
    unifiedSubstitutionNodeTemplateIdGeneratorImplMap.put(COMPUTE, CommandImplNames
            .COMPUTE_NEW_NODE_TEMPLATE_ID_GENERATOR_IMPL);
    unifiedSubstitutionNodeTemplateIdGeneratorImplMap.put(PORT, CommandImplNames
            .PORT_NEW_NODE_TEMPLATE_ID_GENERATOR_IMPL);
    unifiedSubstitutionNodeTemplateIdGeneratorImplMap.put(SUB_INTERFACE, CommandImplNames
            .SUB_INTERFACE_NEW_NODE_TEMPLATE_ID_GENERATOR_IMPL);
  }

  private static List<EntityConsolidationData> getPortConsolidationDataList(
          List<String> portIds,
          List<UnifiedCompositionData> unifiedCompositionDataList) {
    return unifiedCompositionDataList.stream()
            .flatMap(unifiedCompositionData -> unifiedCompositionData.getPortTemplateConsolidationDataList().stream())
            .filter(portTemplateConsolidationData -> portIds.contains(portTemplateConsolidationData.getNodeTemplateId()))
            .collect(Collectors.toList());
  }

  /**
   * Create unified composition.
   *
   * @param serviceTemplate            the service template
   * @param nestedServiceTemplate      the nested service template
   * @param unifiedCompositionDataList the unified composition data list. In case no consolidation,
   *                                   one entry will be in this list, in case of having
   *                                   consolidation, all entries in the list are the once which
   *                                   need to be consolidated.
   * @param mode                       the mode
   * @param context                    the context
   */
  public void createUnifiedComposition(ServiceTemplate serviceTemplate,
                                       ServiceTemplate nestedServiceTemplate,
                                       List<UnifiedCompositionData> unifiedCompositionDataList,
                                       UnifiedCompositionMode mode, TranslationContext context) {
    Optional<UnifiedComposition> unifiedCompositionInstance = getUnifiedCompositionInstance(mode);
    if (!unifiedCompositionInstance.isPresent()) {
      return;
    }
    unifiedCompositionInstance.get()
            .createUnifiedComposition(serviceTemplate, nestedServiceTemplate,
                    unifiedCompositionDataList, context);
  }

  /**
   * Create unified substitution service template according to the input service template, based on
   * the unified composition data.
   *
   * @param serviceTemplate            the service template
   * @param unifiedCompositionDataList the unified composition data list. In case no consolidation,
   *                                   one entry will be in this list, in case of having
   *                                   consolidation, all entries in the list are the once which
   *                                   need to be consolidated.
   * @param context                    the translation context
   * @return the substitution service template
   */
  public Optional<ServiceTemplate> createUnifiedSubstitutionServiceTemplate(
          ServiceTemplate serviceTemplate,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          TranslationContext context,
          String substitutionNodeTypeId,
          Integer index) {
    if (CollectionUtils.isEmpty(unifiedCompositionDataList)) {
      return Optional.empty();
    }
    String templateName = getTemplateName(substitutionNodeTypeId, index);
    ServiceTemplate substitutionServiceTemplate =
            HeatToToscaUtil.createInitSubstitutionServiceTemplate(templateName);

    createIndexInputParameter(substitutionServiceTemplate);

    String computeNodeType =
            handleCompute(serviceTemplate, substitutionServiceTemplate, unifiedCompositionDataList,
                    context);
    handlePorts(serviceTemplate, substitutionServiceTemplate, unifiedCompositionDataList,
            computeNodeType, context);

    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(serviceTemplate,
            substitutionServiceTemplate, unifiedCompositionDataList, context, null);
    handleSubInterfaces(unifiedCompositionTo);
    createOutputParameters(unifiedCompositionTo, computeNodeType);
    NodeType substitutionGlobalNodeType =
            handleSubstitutionGlobalNodeType(serviceTemplate, substitutionServiceTemplate,
                    context, substitutionNodeTypeId);

    HeatToToscaUtil.handleSubstitutionMapping(context,
            substitutionNodeTypeId,
            substitutionServiceTemplate, substitutionGlobalNodeType);

    context.getTranslatedServiceTemplates().put(templateName, substitutionServiceTemplate);
    return Optional.of(substitutionServiceTemplate);
  }


  /**
   * Create abstract substitute node template that can be substituted by the input
   * substitutionServiceTemplate.
   *
   * @param serviceTemplate             the service template
   * @param substitutionServiceTemplate the subtitution service template
   * @param unifiedCompositionDataList  the unified composition data list. In case no consolidation,
   *                                    one entry will be in this list, in case of having
   *                                    consolidation, all entries in the list are the once which
   *                                    need to be consolidated.
   * @param context                     the translation context
   * @return the abstract substitute node template id
   */
  public String createAbstractSubstituteNodeTemplate(
          ServiceTemplate serviceTemplate,
          ServiceTemplate substitutionServiceTemplate,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          String substituteNodeTypeId,
          TranslationContext context,
          Integer index) {

    NodeTemplate substitutionNodeTemplate = new NodeTemplate();
    List<String> directiveList = new ArrayList<>();
    directiveList.add(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
    substitutionNodeTemplate.setDirectives(directiveList);
    substitutionNodeTemplate.setType(substituteNodeTypeId);
    Map<String, ParameterDefinition> substitutionTemplateInputs = DataModelUtil
            .getInputParameters(substitutionServiceTemplate);
    Optional<Map<String, Object>> abstractSubstitutionProperties = Optional.empty();
    if (Objects.nonNull(substitutionTemplateInputs)) {
      abstractSubstitutionProperties = createAbstractSubstitutionProperties(serviceTemplate,
              substitutionTemplateInputs, unifiedCompositionDataList, context);
    }
    abstractSubstitutionProperties.ifPresent(substitutionNodeTemplate::setProperties);

    //Add substitution filtering property
    String substitutionServiceTemplateName = ToscaUtil.getServiceTemplateFileName(
            substitutionServiceTemplate);
    int count = unifiedCompositionDataList.size();
    DataModelUtil.addSubstitutionFilteringProperty(substitutionServiceTemplateName,
            substitutionNodeTemplate, count);
    //Add index_value property
    addIndexValueProperty(substitutionNodeTemplate);
    String substituteNodeTemplateId = getSubstituteNodeTemplateId(substituteNodeTypeId, index);
    //Add node template id and related abstract node template id in context
    addUnifiedSubstitionData(context, serviceTemplate, unifiedCompositionDataList,
            substituteNodeTemplateId);
    DataModelUtil
            .addNodeTemplate(serviceTemplate, substituteNodeTemplateId, substitutionNodeTemplate);
    return substituteNodeTemplateId;

  }

  public void createVfcInstanceGroup(String abstractNodeTemplateId,
                                     ServiceTemplate serviceTemplate,
                                     List<UnifiedCompositionData> unifiedCompositionDataList,
                                     TranslationContext context) {
    if (!TranslationContext.isVfcInstanceGroupingEnabled()) {
      return;
    }
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(serviceTemplate, null,
        unifiedCompositionDataList, context, null);
    unifiedCompositionDataList.forEach(unifiedCompositionData ->
        createSubInterfaceVfcInstanceGroup(abstractNodeTemplateId, unifiedCompositionTo, unifiedCompositionData));
  }

  private void createSubInterfaceVfcInstanceGroup(String abstractNodeTemplateId,
                                                  UnifiedCompositionTo unifiedCompositionTo,
                                                  UnifiedCompositionData unifiedCompositionData) {
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
        getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
    for (SubInterfaceTemplateConsolidationData subInterface : subInterfaceTemplateConsolidationDataList) {
      Optional<String> parentPortNetworkRole;
      if (Objects.isNull(unifiedCompositionTo.getSubstitutionServiceTemplate())) {
        parentPortNetworkRole = subInterface.getParentPortNetworkRole(unifiedCompositionTo.getServiceTemplate(),
            unifiedCompositionTo.getContext());
      } else {
        parentPortNetworkRole = subInterface.getParentPortNetworkRole(unifiedCompositionTo
            .getSubstitutionServiceTemplate(), unifiedCompositionTo.getContext());
      }
      String subInterfaceNetworkRole = subInterface.getNetworkRole();
      if (Objects.nonNull(subInterfaceNetworkRole) && parentPortNetworkRole.isPresent()) {
        createVfcInstanceGroupPerSubInterfaceNetworkRole(abstractNodeTemplateId, subInterfaceNetworkRole,
            parentPortNetworkRole.get(), unifiedCompositionTo.getServiceTemplate());
      }
    }
  }

  private void createVfcInstanceGroupPerSubInterfaceNetworkRole(String abstractNodeTemplateId,
                                                                String subInterfaceNetworkRole,
                                                                String parentPortNetworkRole,
                                                                ServiceTemplate serviceTemplate) {
    String vfcNetworkRoleGroupId = getVfcNetworkRoleGroupId(subInterfaceNetworkRole);
    Map<String, GroupDefinition> groups = DataModelUtil.getGroups(serviceTemplate);
    if (!groups.containsKey(vfcNetworkRoleGroupId)) {
      createNewVfcInstanceGroup(serviceTemplate, parentPortNetworkRole, subInterfaceNetworkRole, vfcNetworkRoleGroupId);
    }
    DataModelUtil.addGroupMember(serviceTemplate, vfcNetworkRoleGroupId, abstractNodeTemplateId);
  }

  private void createNewVfcInstanceGroup(ServiceTemplate serviceTemplate,
                                         String parentPortNetworkRole,
                                         String subInterfaceNetworkRole,
                                         String vfcNetworkRoleGroupId) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(SUB_INTERFACE_ROLE, subInterfaceNetworkRole);
    properties.put(VFC_PARENT_PORT_ROLE, parentPortNetworkRole);

    updateVfcInstanceGroupExposedProperties(subInterfaceNetworkRole,
        serviceTemplate, properties);

    GroupDefinition groupDefinition = new GroupDefinition();
    groupDefinition.setType(GROUP_TYPE_PREFIX + VFC_INSTANCE_GROUP);
    groupDefinition.setProperties(properties);

    DataModelUtil.addGroupDefinitionToTopologyTemplate(serviceTemplate,
        vfcNetworkRoleGroupId, groupDefinition);
  }

  private void updateVfcInstanceGroupExposedProperties(String subInterfaceNetworkRole,
                                                       ServiceTemplate serviceTemplate,
                                                       Map<String, Object> properties) {
    List<String> exposedVfcInstanceGroupingProperties =
        TranslationContext.getExposedVfcInstanceGroupingProperties();

    if (CollectionUtils.isEmpty(exposedVfcInstanceGroupingProperties)) {
      return;
    }

    for (String propertyName : exposedVfcInstanceGroupingProperties) {
      Map<String, Object> getInputMap = new HashMap<>();
      String vfcGroupPropertyInputName = subInterfaceNetworkRole + "_" + propertyName;
      getInputMap.put(GET_INPUT.getDisplayName(), vfcGroupPropertyInputName);
      properties.put(propertyName, getInputMap);

      addInputParameter(vfcGroupPropertyInputName, PropertyType.STRING.getDisplayName(), null,
          serviceTemplate);
    }
  }

  private String getVfcNetworkRoleGroupId(String subInterfaceNetworkRole) {
    StringBuilder sb = new StringBuilder();
    sb.append(subInterfaceNetworkRole).append("_").append(GROUP);
    return sb.toString();
  }

  /**
   * Update the connectivity from/to the "moved" nodes from the original service template to the new
   * substitution service template.
   *
   * @param serviceTemplate            the service template
   * @param unifiedCompositionDataList the unified composition data list. In case no consolidation,
   *                                   one entry will be in this list, in case of having
   *                                   consolidation, all entries in the list are the once which
   *                                   need to be consolidated.
   * @param context                    the translation context
   */
  public void updateCompositionConnectivity(ServiceTemplate serviceTemplate,
                                            List<UnifiedCompositionData> unifiedCompositionDataList,
                                            TranslationContext context) {
    updOutputParamGetAttrInConnectivity(serviceTemplate, unifiedCompositionDataList, context);
    updNodesGetAttrInConnectivity(serviceTemplate, unifiedCompositionDataList, context);
    updNodesConnectedOutConnectivity(serviceTemplate, unifiedCompositionDataList, context);
    updNodesConnectedInConnectivity(serviceTemplate, unifiedCompositionDataList, context);
    updVolumeConnectivity(serviceTemplate, unifiedCompositionDataList, context);
    updGroupsConnectivity(serviceTemplate, unifiedCompositionDataList, context);
  }

  /**
   * Delete the "moved" nodes from the original service template to the new substitution service
   * template.
   *
   * @param serviceTemplate            the service template
   * @param unifiedCompositionDataList the unified composition data list. In case no consolidation,
   *                                   one entry will be in this list, in case of having
   *                                   consolidation, all entries in the list are the once which
   *                                   need to be consolidated.
   * @param context                    the translation context
   */
  public void cleanUnifiedCompositionEntities(
          ServiceTemplate serviceTemplate,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          TranslationContext context) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      //Clean compute node template data from top level service template
      ComputeTemplateConsolidationData computeTemplateConsolidationData =
              unifiedCompositionData.getComputeTemplateConsolidationData();
      cleanServiceTemplate(serviceTemplate, computeTemplateConsolidationData, context);

      //Clean port node template data from top level service template
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        cleanServiceTemplate(serviceTemplate, portTemplateConsolidationData, context);
      }

      //Clean sub-interface node template data from top level service template
      List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
              getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
      for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
              subInterfaceTemplateConsolidationDataList) {
        cleanServiceTemplate(serviceTemplate, subInterfaceTemplateConsolidationData, context);
      }
    }
  }

  /**
   * Clean node types.
   *
   * @param serviceTemplate            the service template
   * @param unifiedCompositionDataList the unified composition data list
   * @param context                    the context
   */
  public void cleanNodeTypes(ServiceTemplate serviceTemplate,
                             List<UnifiedCompositionData> unifiedCompositionDataList,
                             TranslationContext context) {
    for (UnifiedCompositionData unifiedData : unifiedCompositionDataList) {
      removeCleanedNodeType(
              unifiedData.getComputeTemplateConsolidationData().getNodeTemplateId(), serviceTemplate,
              context);
    }
    if (MapUtils.isEmpty(serviceTemplate.getNode_types())) {
      serviceTemplate.setNode_types(null);
    }
  }

  public void updateSubstitutionNodeTypePrefix(ServiceTemplate substitutionServiceTemplate) {
    Map<String, NodeTemplate> nodeTemplates =
            substitutionServiceTemplate.getTopology_template().getNode_templates();

    for (Map.Entry<String, NodeTemplate> nodeTemplateEntry : nodeTemplates.entrySet()) {
      String nodeTypeId = nodeTemplateEntry.getValue().getType();
      NodeType origNodeType = substitutionServiceTemplate.getNode_types().get(nodeTypeId);
      if (Objects.nonNull(origNodeType)
              && nodeTypeId.startsWith(ToscaNodeType.VFC_TYPE_PREFIX)
              && origNodeType.getDerived_from().equals(ToscaNodeType.NOVA_SERVER)) {
        substitutionServiceTemplate.getNode_types().remove(nodeTypeId);

        String newNodeTypeId =
                nodeTypeId.replace(ToscaNodeType.VFC_TYPE_PREFIX, ToscaNodeType.COMPUTE_TYPE_PREFIX);
        nodeTemplateEntry.getValue().setType(newNodeTypeId);
        DataModelUtil
                .addNodeTemplate(substitutionServiceTemplate, nodeTemplateEntry.getKey(),
                        nodeTemplateEntry.getValue());
        substitutionServiceTemplate.getNode_types().put(newNodeTypeId, origNodeType);
      }
    }
  }

  /**
   * Update unified abstract nodes connectivity.
   *
   * @param serviceTemplate the service template
   * @param context         the context
   */
  public void updateUnifiedAbstractNodesConnectivity(ServiceTemplate serviceTemplate,
                                                     TranslationContext context) {


    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    UnifiedSubstitutionData unifiedSubstitutionData = context.getUnifiedSubstitutionData()
            .get(serviceTemplateFileName);

    if (Objects.nonNull(unifiedSubstitutionData)) {
      //Handle get attribute in connectivity for abstarct node to abstract node templates
      Set<String> abstractNodeIds =
              new HashSet<>(unifiedSubstitutionData.getAllRelatedAbstractNodeIds());
      handleGetAttrInConnectivity(serviceTemplate, abstractNodeIds, context);
      //Handle get attribute in connectivity for abstract node templates to nested node template
      Set<String> nestedNodeIds =
              new HashSet<>(unifiedSubstitutionData.getAllUnifiedNestedNodeTemplateIds());
      handleGetAttrInConnectivity(serviceTemplate, nestedNodeIds, context);
    }
  }

  /**
   * Handle unified nested definition.
   *
   * @param unifiedCompositionTo    the unified composition data transfer object
   * @param unifiedCompositionData  the unified composition data
   */
  public void handleUnifiedNestedDefinition(UnifiedCompositionTo unifiedCompositionTo,
                                            UnifiedCompositionData unifiedCompositionData) {
    handleUnifiedNestedNodeType(unifiedCompositionTo.getServiceTemplate(), unifiedCompositionTo
        .getSubstitutionServiceTemplate(), unifiedCompositionTo.getContext());
    updateUnifiedNestedTemplates(unifiedCompositionTo.getServiceTemplate(), unifiedCompositionTo
        .getSubstitutionServiceTemplate(), unifiedCompositionData, unifiedCompositionTo.getContext());
  }

  private void handleGetAttrInConnectivity(ServiceTemplate serviceTemplate,
                                           Set<String> unifiedNodeIds,
                                           TranslationContext context) {
    Map<String, NodeTemplate> nodeTemplates =
            serviceTemplate.getTopology_template().getNode_templates();
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    for (String unifiedNodeId : unifiedNodeIds) {
      NodeTemplate nodeTemplate = nodeTemplates.get(unifiedNodeId);
      handleGetAttrInAbstractNodeTemplate(serviceTemplate, context, serviceTemplateFileName,
              nodeTemplate);
    }
  }

  private void handleUnifiedNestedNodeType(ServiceTemplate mainServiceTemplate,
                                           ServiceTemplate nestedServiceTemplate,
                                           TranslationContext context) {


    SubstitutionMapping substitutionMappings =
            nestedServiceTemplate.getTopology_template().getSubstitution_mappings();
    String nodeTypeId = substitutionMappings.getNode_type();

    Optional<String> newNestedNodeTypeId = getNewNestedNodeTypeId(nestedServiceTemplate, context);

    ServiceTemplate globalSubstitutionServiceTemplate =
            context.getGlobalSubstitutionServiceTemplate();

    if (isNestedServiceTemplateWasHandled(globalSubstitutionServiceTemplate, nestedServiceTemplate,
            context,
            newNestedNodeTypeId)) {
      context
              .updateHandledComputeType(ToscaUtil.getServiceTemplateFileName(mainServiceTemplate),
                      newNestedNodeTypeId.get(),
                      ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate));
      return;
    }


    newNestedNodeTypeId.ifPresent(
            newNestedNodeTypeIdVal -> handleNestedNodeType(nodeTypeId, newNestedNodeTypeIdVal,
                    nestedServiceTemplate, mainServiceTemplate, globalSubstitutionServiceTemplate,
                    context));

  }

  private boolean isNestedServiceTemplateWasHandled(ServiceTemplate mainServiceTemplate,
                                                    ServiceTemplate nestedServiceTemplate,
                                                    TranslationContext context,
                                                    Optional<String> newNestedNodeTypeId) {
    return newNestedNodeTypeId.isPresent()
            && context.isNestedServiceTemplateWasHandled(
            ToscaUtil.getServiceTemplateFileName(mainServiceTemplate),
            ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate));
  }

  private void handleNestedNodeType(String nodeTypeId, String newNestedNodeTypeId,
                                    ServiceTemplate nestedServiceTemplate,
                                    ServiceTemplate mainServiceTemplate,
                                    ServiceTemplate globalSubstitutionServiceTemplate,
                                    TranslationContext context) {
    updateNestedServiceTemplate(nestedServiceTemplate, context);
    updateNestedNodeType(nodeTypeId, newNestedNodeTypeId, nestedServiceTemplate,
            mainServiceTemplate,
            globalSubstitutionServiceTemplate, context);


  }

  private void updateNestedServiceTemplate(ServiceTemplate nestedServiceTemplate,
                                           TranslationContext context) {
    enrichPortProperties(nestedServiceTemplate, context);
  }

  private void enrichPortProperties(ServiceTemplate nestedServiceTemplate,
                                    TranslationContext context) {
    String nestedServiceTemplateFileName =
            ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate);
    FilePortConsolidationData filePortConsolidationData =
            context.getConsolidationData().getPortConsolidationData().getFilePortConsolidationData
                    (nestedServiceTemplateFileName);

    if (Objects.nonNull(filePortConsolidationData)) {
      Set<String> portNodeTemplateIds = filePortConsolidationData.getAllPortNodeTemplateIds();
      if (Objects.nonNull(portNodeTemplateIds)) {
        for (String portNodeTemplateId : portNodeTemplateIds) {
          NodeTemplate portNodeTemplate = DataModelUtil.getNodeTemplate(nestedServiceTemplate,
                  portNodeTemplateId);
          List<EntityConsolidationData> portEntityConsolidationDataList = new ArrayList<>();
          portEntityConsolidationDataList.add(filePortConsolidationData
                  .getPortTemplateConsolidationData(portNodeTemplateId));

          handleNodeTypeProperties(nestedServiceTemplate,
                  portEntityConsolidationDataList, portNodeTemplate, UnifiedCompositionEntity.PORT,
                  null, context);
          //Add subinterface_indicator property to PORT
          addPortSubInterfaceIndicatorProperty(portNodeTemplate.getProperties(),
              filePortConsolidationData.getPortTemplateConsolidationData(portNodeTemplateId));
        }
      }
    }
  }

  private void updateNestedNodeType(String nodeTypeId, String newNestedNodeTypeId,
                                    ServiceTemplate nestedServiceTemplate,
                                    ServiceTemplate mainServiceTemplate,
                                    ServiceTemplate globalSubstitutionServiceTemplate,
                                    TranslationContext context) {
    String indexedNewNestedNodeTypeId =
            updateNodeTypeId(nodeTypeId, newNestedNodeTypeId, nestedServiceTemplate,
                    mainServiceTemplate,
                    globalSubstitutionServiceTemplate, context);

    updateNodeTypeProperties(nestedServiceTemplate, globalSubstitutionServiceTemplate,
            indexedNewNestedNodeTypeId);
  }

  private void updateNodeTypeProperties(ServiceTemplate nestedServiceTemplate,
                                        ServiceTemplate globalSubstitutionServiceTemplate,
                                        String nodeTypeId) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    Map<String, PropertyDefinition> nodeTypePropertiesDefinition =
            toscaAnalyzerService.manageSubstitutionNodeTypeProperties(nestedServiceTemplate);
    NodeType nestedNodeType =
            DataModelUtil.getNodeType(globalSubstitutionServiceTemplate, nodeTypeId);
    nestedNodeType.setProperties(nodeTypePropertiesDefinition);
  }

  private String updateNodeTypeId(String nodeTypeId, String newNestedNodeTypeId,
                                  ServiceTemplate nestedServiceTemplate,
                                  ServiceTemplate mainServiceTemplate,
                                  ServiceTemplate globalSubstitutionServiceTemplate,
                                  TranslationContext context) {
    String indexedNewNestedNodeTypeId =
            handleNestedNodeTypeInGlobalSubstitutionTemplate(nodeTypeId, newNestedNodeTypeId,
                    globalSubstitutionServiceTemplate, context);

    handleSubstitutionMappingInNestedServiceTemplate(indexedNewNestedNodeTypeId,
            nestedServiceTemplate, context);

    context
            .updateHandledComputeType(
                    ToscaUtil.getServiceTemplateFileName(mainServiceTemplate),
                    ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate),
                    newNestedNodeTypeId);
    return indexedNewNestedNodeTypeId;
  }

  private String handleNestedNodeTypeInGlobalSubstitutionTemplate(String nodeTypeId,
                                                                  String newNestedNodeTypeId,
                                                                  ServiceTemplate globalSubstitutionServiceTemplate,
                                                                  TranslationContext context) {
    String indexedNodeType =
            getIndexedGlobalNodeTypeId(newNestedNodeTypeId, context);
    context.updateUsedTimesForNestedComputeNodeType(
            ToscaUtil.getServiceTemplateFileName(globalSubstitutionServiceTemplate),
            newNestedNodeTypeId);
    handleNestedNodeTypesInGlobalSubstituteServiceTemplate(nodeTypeId, indexedNodeType,
            globalSubstitutionServiceTemplate, context);
    return indexedNodeType;
  }

  private String getIndexedGlobalNodeTypeId(String newNestedNodeTypeId,
                                            TranslationContext context) {
    int globalNodeTypeIndex =
            context.getGlobalNodeTypeIndex(
                    ToscaUtil.getServiceTemplateFileName(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME),
                    newNestedNodeTypeId);
    return globalNodeTypeIndex > 0 ? newNestedNodeTypeId + "_"
            + globalNodeTypeIndex : newNestedNodeTypeId;
  }

  private void updateUnifiedNestedTemplates(ServiceTemplate mainServiceTemplate,
                                            ServiceTemplate nestedServiceTemplate,
                                            UnifiedCompositionData unifiedCompositionData,
                                            TranslationContext context) {

    NestedTemplateConsolidationData nestedTemplateConsolidationData =
            unifiedCompositionData.getNestedTemplateConsolidationData();
    if (Objects.isNull(nestedTemplateConsolidationData)) {
      return;
    }
    handleNestedNodeTemplateInMainServiceTemplate(
        nestedTemplateConsolidationData.getNodeTemplateId(), mainServiceTemplate,
        nestedServiceTemplate, context);
  }

  /**
   * Update connectivity for unified nested patterns.
   *
   * @param unifiedCompositionTo    the unified composition data transfer object
   * @param unifiedCompositionData  the unified composition data
   */
  public void updateUnifiedNestedConnectivity(UnifiedCompositionTo unifiedCompositionTo,
                                              UnifiedCompositionData unifiedCompositionData) {

    updNestedCompositionNodesConnectedInConnectivity(unifiedCompositionTo.getServiceTemplate(), unifiedCompositionData,
        unifiedCompositionTo.getContext());
    updNestedCompositionNodesConnectedOutConnectivity(unifiedCompositionTo.getServiceTemplate(),
        unifiedCompositionTo.getSubstitutionServiceTemplate(), unifiedCompositionData, unifiedCompositionTo
            .getContext());
    updNestedCompositionNodesGetAttrInConnectivity(unifiedCompositionTo.getServiceTemplate(), unifiedCompositionData,
        unifiedCompositionTo.getContext());
    updNestedCompositionOutputParamGetAttrInConnectivity(unifiedCompositionTo.getServiceTemplate(),
        unifiedCompositionData, unifiedCompositionTo.getContext());
  }


  /**
   * Clean unified nested entities. Update the heat stack group with the new node template ids.
   *
   * @param unifiedCompositionTo        the unified composition data transfer object
   * @param unifiedCompositionData the unified composition data
   */
  public void cleanUnifiedNestedEntities(UnifiedCompositionTo unifiedCompositionTo,
                                         UnifiedCompositionData unifiedCompositionData) {
    EntityConsolidationData entityConsolidationData =
        unifiedCompositionData.getNestedTemplateConsolidationData();
    updateHeatStackGroupNestedComposition(unifiedCompositionTo.getServiceTemplate(), entityConsolidationData,
        unifiedCompositionTo.getContext());

  }

  public void createNestedVfcInstanceGroup(String nestedNodeTemplateId,
                                           UnifiedCompositionTo unifiedCompositionTo,
                                           UnifiedCompositionData unifiedCompositionData) {
    if (!TranslationContext.isVfcInstanceGroupingEnabled()) {
      return;
    }
    createSubInterfaceVfcInstanceGroup(nestedNodeTemplateId, unifiedCompositionTo, unifiedCompositionData);
  }

  public void handleComplexVfcType(ServiceTemplate serviceTemplate, TranslationContext context) {
    SubstitutionMapping substitutionMapping =
            serviceTemplate.getTopology_template().getSubstitution_mappings();

    if (Objects.isNull(substitutionMapping)) {
      return;
    }

    ServiceTemplate globalSubstitutionServiceTemplate =
            context.getGlobalSubstitutionServiceTemplate();

    String substitutionNT = substitutionMapping.getNode_type();
    if (globalSubstitutionServiceTemplate.getNode_types().containsKey(substitutionNT)) {
      //This needs to be done when catalog is ready for complex VFC
    }
  }


  protected void updNodesConnectedOutConnectivity(ServiceTemplate serviceTemplate,
                                                  List<UnifiedCompositionData>
                                                          unifiedCompositionDataList,
                                                  TranslationContext context) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionData
              .getComputeTemplateConsolidationData();
      //Add requirements in the abstract node template for nodes connected out for computes
      String newComputeNodeTemplateId = getNewComputeNodeTemplateId(serviceTemplate,
              computeTemplateConsolidationData.getNodeTemplateId());
      Map<String, List<RequirementAssignmentData>> computeNodesConnectedOut =
              computeTemplateConsolidationData.getNodesConnectedOut();
      if (computeNodesConnectedOut != null) {
        updateRequirementInAbstractNodeTemplate(serviceTemplate, computeTemplateConsolidationData,
                newComputeNodeTemplateId, computeNodesConnectedOut, context);
      }
      String computeType = getComputeTypeSuffix(serviceTemplate, computeTemplateConsolidationData
              .getNodeTemplateId());
      //Add requirements in the abstract node template for nodes connected out for ports
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        String newPortNodeTemplateId = getNewPortNodeTemplateId(portTemplateConsolidationData
                .getNodeTemplateId(), computeType, computeTemplateConsolidationData);
        Map<String, List<RequirementAssignmentData>> portNodesConnectedOut =
                portTemplateConsolidationData.getNodesConnectedOut();
        if (portNodesConnectedOut != null) {
          updateRequirementInAbstractNodeTemplate(serviceTemplate, portTemplateConsolidationData,
                  newPortNodeTemplateId, portNodesConnectedOut, context);
        }
      }
      //For sub-interface
      //Add requirements in the abstract node template for nodes connected out for ports
      updateSubInterfaceNodesConnectedOut(serviceTemplate, unifiedCompositionData,
              computeTemplateConsolidationData, computeType, context);
    }
  }

  private void updateSubInterfaceNodesConnectedOut(ServiceTemplate serviceTemplate,
                                                   UnifiedCompositionData unifiedCompositionData,
                                                   ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                   String computeType,
                                                   TranslationContext context) {
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
            getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
    for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
            subInterfaceTemplateConsolidationDataList) {
      String newSubInterfaceNodeTemplateId = getNewSubInterfaceNodeTemplateId(serviceTemplate, computeType,
              computeTemplateConsolidationData, subInterfaceTemplateConsolidationData, context);
      Map<String, List<RequirementAssignmentData>> subInterfaceNodesConnectedOut =
              subInterfaceTemplateConsolidationData.getNodesConnectedOut();
      if (subInterfaceNodesConnectedOut != null) {
        updateRequirementInAbstractNodeTemplate(serviceTemplate, subInterfaceTemplateConsolidationData,
                newSubInterfaceNodeTemplateId, subInterfaceNodesConnectedOut, context);
      }
    }
  }

  private void updNestedCompositionNodesConnectedOutConnectivity(ServiceTemplate serviceTemplate,
                                                                 ServiceTemplate nestedServiceTemplate,
                                                                 UnifiedCompositionData unifiedCompositionData,
                                                                 TranslationContext context) {
    NestedTemplateConsolidationData nestedTemplateConsolidationData =
            unifiedCompositionData.getNestedTemplateConsolidationData();
    Map<String, List<RequirementAssignmentData>> nodesConnectedOut =
            Objects.isNull(nestedTemplateConsolidationData) ? new HashMap<>()
                    : nestedTemplateConsolidationData.getNodesConnectedOut();

    FileComputeConsolidationData nestedFileComputeConsolidationData =
            context.getConsolidationData().getComputeConsolidationData().getFileComputeConsolidationData
                    (ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate));

    if (Objects.isNull(nestedFileComputeConsolidationData)) {
      return;
    }

    TypeComputeConsolidationData computeType =
            nestedFileComputeConsolidationData.getAllTypeComputeConsolidationData().iterator().next();
    if (Objects.isNull(computeType)) {
      return;
    }

    String singleComputeId = computeType.getAllComputeNodeTemplateIds().iterator().next();
    if (Objects.nonNull(singleComputeId) && (Objects.nonNull(nestedTemplateConsolidationData))) {
      updateRequirementInNestedNodeTemplate(serviceTemplate, nestedTemplateConsolidationData,
              singleComputeId, nodesConnectedOut);
    }
  }

  private void updNodesConnectedInConnectivity(ServiceTemplate serviceTemplate,
                                               List<UnifiedCompositionData>
                                                       unifiedCompositionDataList,
                                               TranslationContext context) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionData
              .getComputeTemplateConsolidationData();
      //Update requirements in the node template which pointing to the computes
      String newComputeNodeTemplateId = getNewComputeNodeTemplateId(serviceTemplate,
              computeTemplateConsolidationData.getNodeTemplateId());
      updNodesConnectedInConnectivity(serviceTemplate, computeTemplateConsolidationData,
              newComputeNodeTemplateId, context, false);

      String computeType = getComputeTypeSuffix(serviceTemplate, computeTemplateConsolidationData
              .getNodeTemplateId());
      //Update requirements in the node template which pointing to the ports
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        String newPortNodeTemplateId = getNewPortNodeTemplateId(portTemplateConsolidationData
                .getNodeTemplateId(), computeType, computeTemplateConsolidationData);
        updNodesConnectedInConnectivity(serviceTemplate, portTemplateConsolidationData,
                newPortNodeTemplateId, context, false);
      }

      //Update requirements in the node template which pointing to the sub-interface
      updateSubInterfaceNodesConnectedIn(serviceTemplate, unifiedCompositionData,
              computeTemplateConsolidationData, computeType, context);
    }
  }

  private void updNodesConnectedInConnectivity(ServiceTemplate serviceTemplate,
                                               EntityConsolidationData entityConsolidationData,
                                               String newNodeTemplateId,
                                               TranslationContext context,
                                               boolean isNested) {
    Map<String, List<RequirementAssignmentData>> nodesConnectedIn =
            entityConsolidationData.getNodesConnectedIn();
    if (nodesConnectedIn == null) {
      //No nodes connected in info
      return;
    }
    for (Map.Entry<String, List<RequirementAssignmentData>> entry : nodesConnectedIn
            .entrySet()) {
      List<RequirementAssignmentData> requirementAssignmentDataList = entry.getValue();
      for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList) {
        RequirementAssignment requirementAssignment = requirementAssignmentData
                .getRequirementAssignment();
        if (!requirementAssignment.getNode().equals(entityConsolidationData
                .getNodeTemplateId())) {
          //The requirement assignment target node should be the one which we are handling in the
          //consolidation object
          continue;
        }
        //Update the requirement assignment object in the original node template
        if (isNested) {
          updateRequirementForNestedCompositionNodesConnectedIn(serviceTemplate,
                  requirementAssignmentData, newNodeTemplateId);
        } else {
          updateRequirementForNodesConnectedIn(serviceTemplate, requirementAssignmentData,
                  entityConsolidationData, entry.getKey(), newNodeTemplateId, context);
        }

      }
    }
  }

  private void updateSubInterfaceNodesConnectedIn(ServiceTemplate serviceTemplate,
                                                  UnifiedCompositionData unifiedCompositionData,
                                                  ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                  String computeType,
                                                  TranslationContext context) {
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
            getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
    for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
            subInterfaceTemplateConsolidationDataList) {
      String newSubInterfaceNodeTemplateId = getNewSubInterfaceNodeTemplateId(serviceTemplate, computeType,
              computeTemplateConsolidationData, subInterfaceTemplateConsolidationData, context);
      updNodesConnectedInConnectivity(serviceTemplate, subInterfaceTemplateConsolidationData,
              newSubInterfaceNodeTemplateId, context, false);
    }
  }

  protected void updNestedCompositionNodesConnectedInConnectivity(
          ServiceTemplate serviceTemplate,
          UnifiedCompositionData unifiedCompositionData,
          TranslationContext context) {
    NestedTemplateConsolidationData nestedTemplateConsolidationData = unifiedCompositionData
            .getNestedTemplateConsolidationData();
    //Update requirements in the node template which pointing to the nested nodes
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    Optional<String> newNestedNodeTemplateId = context.getUnifiedNestedNodeTemplateId(
            serviceTemplateFileName, nestedTemplateConsolidationData.getNodeTemplateId());
    newNestedNodeTemplateId.ifPresent(
            newNestedNodeTemplateIdVal -> updNodesConnectedInConnectivity(serviceTemplate,
                    nestedTemplateConsolidationData,
                    newNestedNodeTemplateIdVal, context, true));

  }

  private void updVolumeConnectivity(ServiceTemplate serviceTemplate,
                                     List<UnifiedCompositionData>
                                             unifiedCompositionDataList,
                                     TranslationContext context) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionData
              .getComputeTemplateConsolidationData();
      //Add requirements in the abstract node template for compute volumes
      String newComputeNodeTemplateId = getNewComputeNodeTemplateId(serviceTemplate,
              computeTemplateConsolidationData.getNodeTemplateId());
      Map<String, List<RequirementAssignmentData>> computeVolumes =
              computeTemplateConsolidationData.getVolumes();
      if (computeVolumes != null) {
        updateRequirementInAbstractNodeTemplate(serviceTemplate, computeTemplateConsolidationData,
                newComputeNodeTemplateId, computeVolumes, context);
      }
    }
  }

  private void updGroupsConnectivity(ServiceTemplate serviceTemplate,
                                     List<UnifiedCompositionData>
                                             unifiedCompositionDataList,
                                     TranslationContext context) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionData
              .getComputeTemplateConsolidationData();
      //Add requirements in the abstract node template for nodes connected in for computes
      updGroupsConnectivity(serviceTemplate, computeTemplateConsolidationData, context);

      //Add requirements in the abstract node template for nodes connected in for ports
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        updGroupsConnectivity(serviceTemplate, portTemplateConsolidationData, context);
      }

      //Add requirements in the abstract node template for nodes connected in for subInterface
      List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
              getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
      for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
              subInterfaceTemplateConsolidationDataList) {
        updGroupsConnectivity(serviceTemplate, subInterfaceTemplateConsolidationData, context);
      }
    }
  }

  private void updGroupsConnectivity(ServiceTemplate serviceTemplate, EntityConsolidationData
          entityConsolidationData, TranslationContext context) {
    List<String> groupIds = entityConsolidationData.getGroupIds();
    if (groupIds == null) {
      return;
    }
    String oldNodeTemplateId = entityConsolidationData.getNodeTemplateId();
    String abstractNodeTemplateId = context.getUnifiedAbstractNodeTemplateId(
            serviceTemplate, entityConsolidationData.getNodeTemplateId());
    Map<String, GroupDefinition> groups = serviceTemplate.getTopology_template().getGroups();
    if (groups == null) {
      return;
    }
    for (String groupId : groupIds) {
      GroupDefinition groupDefinition = groups.get(groupId);
      if (groupDefinition == null) {
        continue;
      }
      List<String> groupMembers = groupDefinition.getMembers();
      if (groupMembers.contains(oldNodeTemplateId)) {
        //Replace the old node template id
        groupMembers.remove(oldNodeTemplateId);
        if (!groupMembers.contains(abstractNodeTemplateId)) {
          //Add the abstract node template id if not already present
          groupMembers.add(abstractNodeTemplateId);
        }
      }
    }
  }

  private void updOutputParamGetAttrInConnectivity(
          ServiceTemplate serviceTemplate, List<UnifiedCompositionData> unifiedComposotionDataList,
          TranslationContext context) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedComposotionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData =
              unifiedCompositionData.getComputeTemplateConsolidationData();
      String newComputeNodeTemplateId = getNewComputeNodeTemplateId(serviceTemplate,
              computeTemplateConsolidationData.getNodeTemplateId());

      updOutputParamGetAttrInConnectivity(serviceTemplate, computeTemplateConsolidationData,
              computeTemplateConsolidationData.getNodeTemplateId(), newComputeNodeTemplateId,
              context, false);

      String computeType =
              getComputeTypeSuffix(serviceTemplate,
                      computeTemplateConsolidationData.getNodeTemplateId());
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        String newPortNodeTemplateId =
                getNewPortNodeTemplateId(portTemplateConsolidationData.getNodeTemplateId(), computeType,
                        computeTemplateConsolidationData);

        updOutputParamGetAttrInConnectivity(serviceTemplate, portTemplateConsolidationData,
                portTemplateConsolidationData.getNodeTemplateId(), newPortNodeTemplateId, context,
                false);
      }

      updateSubInterfaceOutputParamGetAttrIn(serviceTemplate, unifiedCompositionData,
              computeTemplateConsolidationData, computeType, context);
    }
  }

  private void updateSubInterfaceOutputParamGetAttrIn(ServiceTemplate serviceTemplate,
                                                      UnifiedCompositionData unifiedCompositionData,
                                                      ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                      String computeType,
                                                      TranslationContext context) {
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
            getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
    for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
            subInterfaceTemplateConsolidationDataList) {
      String newSubInterfaceNodeTemplateId = getNewSubInterfaceNodeTemplateId(serviceTemplate, computeType,
              computeTemplateConsolidationData, subInterfaceTemplateConsolidationData, context);
      updOutputParamGetAttrInConnectivity(serviceTemplate, subInterfaceTemplateConsolidationData,
              subInterfaceTemplateConsolidationData.getNodeTemplateId(), newSubInterfaceNodeTemplateId, context,
              false);
    }
  }

  private void updNodesGetAttrInConnectivity(
          ServiceTemplate serviceTemplate,
          List<UnifiedCompositionData> unifiedComposotionDataList,
          TranslationContext context) {
    Map<String, UnifiedCompositionEntity> consolidationNodeTemplateIdAndType =
            getAllConsolidationNodeTemplateIdAndType(unifiedComposotionDataList);
    for (UnifiedCompositionData unifiedCompositionData : unifiedComposotionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData =
              unifiedCompositionData.getComputeTemplateConsolidationData();
      String newComputeNodeTemplateId = getNewComputeNodeTemplateId(serviceTemplate,
              computeTemplateConsolidationData.getNodeTemplateId());

      updNodeGetAttrInConnectivity(serviceTemplate, computeTemplateConsolidationData,
              computeTemplateConsolidationData.getNodeTemplateId(),
              newComputeNodeTemplateId, context, consolidationNodeTemplateIdAndType, false);

      String computeType =
              getComputeTypeSuffix(serviceTemplate,
                      computeTemplateConsolidationData.getNodeTemplateId());

      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        String newPotNodeTemplateId =
                getNewPortNodeTemplateId(portTemplateConsolidationData.getNodeTemplateId(), computeType,
                        computeTemplateConsolidationData);

        updNodeGetAttrInConnectivity(serviceTemplate, portTemplateConsolidationData,
                portTemplateConsolidationData.getNodeTemplateId(),
                newPotNodeTemplateId, context, consolidationNodeTemplateIdAndType, false);
      }

      updateSubInterfaceNodesGetAttrIn(serviceTemplate, unifiedCompositionData,
              computeTemplateConsolidationData, computeType, consolidationNodeTemplateIdAndType, context);
    }
  }

  private void updateSubInterfaceNodesGetAttrIn(ServiceTemplate serviceTemplate,
                                                UnifiedCompositionData unifiedCompositionData,
                                                ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                String computeType,
                                                Map<String, UnifiedCompositionEntity> consolidationNodeTemplateIdAndType,
                                                TranslationContext context) {
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
            getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
    for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
            subInterfaceTemplateConsolidationDataList) {
      String newSubInterfaceNodeTemplateId = getNewSubInterfaceNodeTemplateId(serviceTemplate, computeType,
              computeTemplateConsolidationData, subInterfaceTemplateConsolidationData, context);
      updNodeGetAttrInConnectivity(serviceTemplate, subInterfaceTemplateConsolidationData,
              subInterfaceTemplateConsolidationData.getNodeTemplateId(),
              newSubInterfaceNodeTemplateId, context,
              consolidationNodeTemplateIdAndType, false);
    }
  }

  protected void updNestedCompositionOutputParamGetAttrInConnectivity(
          ServiceTemplate serviceTemplate, UnifiedCompositionData unifiedCompositionData,
          TranslationContext context) {
    NestedTemplateConsolidationData nestedTemplateConsolidationData =
            unifiedCompositionData.getNestedTemplateConsolidationData();
    if (Objects.isNull(nestedTemplateConsolidationData)) {
      return;
    }
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    Optional<String> newNestedNodeTemplateId = context.getUnifiedNestedNodeTemplateId(
            serviceTemplateFileName, nestedTemplateConsolidationData.getNodeTemplateId());

    newNestedNodeTemplateId.ifPresent(
            newNestedNodeTemplateIdVal -> updOutputParamGetAttrInConnectivity(serviceTemplate,
                    nestedTemplateConsolidationData, nestedTemplateConsolidationData.getNodeTemplateId(),
                    newNestedNodeTemplateIdVal, context, true));
  }

  protected void updNestedCompositionNodesGetAttrInConnectivity(
          ServiceTemplate serviceTemplate,
          UnifiedCompositionData unifiedCompositionData,
          TranslationContext context) {
    NestedTemplateConsolidationData nestedTemplateConsolidationData =
            unifiedCompositionData.getNestedTemplateConsolidationData();
    if (Objects.isNull(nestedTemplateConsolidationData)) {
      return;
    }
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    Optional<String> newNestedNodeTemplateId = context.getUnifiedNestedNodeTemplateId(
            serviceTemplateFileName, nestedTemplateConsolidationData.getNodeTemplateId());

    newNestedNodeTemplateId.ifPresent(
            newNestedNodeTemplateIdVal -> updNodeGetAttrInConnectivity(serviceTemplate,
                    nestedTemplateConsolidationData, nestedTemplateConsolidationData.getNodeTemplateId(),
                    newNestedNodeTemplateIdVal, context, null, true));
  }

  private void updateRequirementForNodesConnectedIn(
          ServiceTemplate serviceTemplate,
          RequirementAssignmentData requirementAssignmentData,
          EntityConsolidationData entityConsolidationData,
          String originalNodeTemplateId,
          String newNodeTemplateId,
          TranslationContext context) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    RequirementAssignment requirementAssignment = requirementAssignmentData
            .getRequirementAssignment();
    String newAbstractUnifiedNodeTemplateId = context.getUnifiedAbstractNodeTemplateId(
            serviceTemplate, entityConsolidationData.getNodeTemplateId());
    NodeTemplate abstractUnifiedNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
            newAbstractUnifiedNodeTemplateId);
    Optional<String> newCapabilityId = getNewCapabilityForNodesConnectedIn(serviceTemplate,
            abstractUnifiedNodeTemplate, requirementAssignment, newNodeTemplateId, context);
    if (newCapabilityId.isPresent()) {
      //Creating a copy of the requirement object and checking if it already exists in the
      // original node template
      RequirementAssignment requirementAssignmentCopy = (RequirementAssignment) getClonedObject(
              requirementAssignmentData.getRequirementAssignment(), RequirementAssignment.class);
      NodeTemplate originalNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
              originalNodeTemplateId);
      requirementAssignmentCopy.setCapability(newCapabilityId.get());
      requirementAssignmentCopy.setNode(newAbstractUnifiedNodeTemplateId);
      if (!toscaAnalyzerService.isRequirementExistInNodeTemplate(originalNodeTemplate,
              requirementAssignmentData.getRequirementId(), requirementAssignmentCopy)) {
        //Update the existing requirement
        requirementAssignmentData.getRequirementAssignment().setCapability(newCapabilityId
                .get());
        requirementAssignmentData.getRequirementAssignment()
                .setNode(newAbstractUnifiedNodeTemplateId);
      } else {
        //The updated requirement already exists in the node template so simply remove the
        // current one
        DataModelUtil.removeRequirementAssignment(originalNodeTemplate, requirementAssignmentData
                .getRequirementId(), requirementAssignmentData.getRequirementAssignment());
      }
    }
  }

  private void updateRequirementForNestedCompositionNodesConnectedIn(
          ServiceTemplate serviceTemplate,
          RequirementAssignmentData requirementAssignmentData,
          String newNodeTemplateId) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    String newAbstractUnifiedNodeTemplateId = newNodeTemplateId;
    RequirementAssignment requirementAssignment = requirementAssignmentData
            .getRequirementAssignment();
    //Creating a copy of the requirement object and checking if it already exists in the
    // original node template
    RequirementAssignment requirementAssignmentCopy = (RequirementAssignment) getClonedObject(
            requirementAssignmentData.getRequirementAssignment(), RequirementAssignment.class);
    NodeTemplate unifiedAbstractNestedNodeTemplate = DataModelUtil
            .getNodeTemplate(serviceTemplate, newAbstractUnifiedNodeTemplateId);
    requirementAssignmentCopy.setCapability(requirementAssignment.getCapability());
    requirementAssignmentCopy.setNode(newAbstractUnifiedNodeTemplateId);
    if (!toscaAnalyzerService.isRequirementExistInNodeTemplate(unifiedAbstractNestedNodeTemplate,
            requirementAssignmentData.getRequirementId(), requirementAssignmentCopy)) {
      //Update the existing requirement
      requirementAssignmentData.getRequirementAssignment()
              .setNode(newAbstractUnifiedNodeTemplateId);
    } else {
      //The updated requirement already exists in the node template so simply remove the
      // current one
      DataModelUtil.removeRequirementAssignment(unifiedAbstractNestedNodeTemplate,
              requirementAssignmentData.getRequirementId(), requirementAssignmentData
                      .getRequirementAssignment());
    }
  }

  private Optional<String> getNewCapabilityForNodesConnectedIn(ServiceTemplate serviceTemplate,
                                                               NodeTemplate unifiedNodeTemplate,
                                                               RequirementAssignment
                                                                       requirementAssignment,
                                                               String newNodeTemplateId,
                                                               TranslationContext context) {
    ServiceTemplate globalSubstitutionServiceTemplate =
            HeatToToscaUtil.fetchGlobalSubstitutionServiceTemplate(serviceTemplate, context);
    Map<String, NodeType> nodeTypes = globalSubstitutionServiceTemplate.getNode_types();
    String unifiedNodeTemplateType = unifiedNodeTemplate.getType();
    NodeType unifiedNodeType = nodeTypes.get(unifiedNodeTemplateType);
    Map<String, CapabilityDefinition> abstractNodeTypeCapabilities = unifiedNodeType
            .getCapabilities();
    for (Map.Entry<String, CapabilityDefinition> entry : abstractNodeTypeCapabilities.entrySet()) {
      String capabilityId = entry.getKey();
      CapabilityDefinition capabilityDefinition = entry.getValue();
      String capabilityType = capabilityDefinition.getType();
      if (capabilityType.equals(requirementAssignment.getCapability())
              && capabilityId.endsWith(newNodeTemplateId)) {
        //Matching capability type found..Check if the id ends with new node template id
        return Optional.ofNullable(capabilityId);
      }
    }
    return Optional.empty();
  }


  private void updateRequirementInAbstractNodeTemplate(ServiceTemplate serviceTemplate,
                                                       EntityConsolidationData
                                                               entityConsolidationData,
                                                       String newNodeTemplateId,
                                                       Map<String, List<RequirementAssignmentData>>
                                                               requirementAssignmentDataMap,
                                                       TranslationContext context) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    for (Map.Entry<String, List<RequirementAssignmentData>> entry : requirementAssignmentDataMap
            .entrySet()) {
      String abstractNodeTemplateId = context.getUnifiedAbstractNodeTemplateId(
              serviceTemplate, entityConsolidationData.getNodeTemplateId());
      NodeTemplate abstractNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
              abstractNodeTemplateId);
      if (abstractNodeTemplate == null) {
        //The abstract node template is not found from id in the context
        return;
      }
      List<RequirementAssignmentData> requirementAssignmentDataList = entry.getValue();
      for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList) {
        String oldRequirementId = requirementAssignmentData.getRequirementId();
        RequirementAssignment abstractRequirementAssignment = (RequirementAssignment)
                getClonedObject(requirementAssignmentData.getRequirementAssignment(),
                        RequirementAssignment.class);
        String newRequirementId = oldRequirementId + "_" + newNodeTemplateId;
        //Check if the requirement is not already present in the list of requirements of the
        // abstract node template
        if (!toscaAnalyzerService.isRequirementExistInNodeTemplate(abstractNodeTemplate,
                newRequirementId, abstractRequirementAssignment)) {
          DataModelUtil.addRequirementAssignment(abstractNodeTemplate, newRequirementId,
                  abstractRequirementAssignment);
          //Update the volume relationship template if required
          updateVolumeRelationshipTemplate(serviceTemplate, abstractRequirementAssignment
                  .getRelationship(), context);
        }
      }
    }
  }

  private void updateRequirementInNestedNodeTemplate(ServiceTemplate serviceTemplate,
                                                     EntityConsolidationData
                                                             entityConsolidationData,
                                                     String newNodeTemplateId,
                                                     Map<String, List<RequirementAssignmentData>>
                                                             requirementAssignmentDataMap) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();

    if (MapUtils.isEmpty(requirementAssignmentDataMap)) {
      return;
    }

    for (Map.Entry<String, List<RequirementAssignmentData>> entry : requirementAssignmentDataMap
            .entrySet()) {
      String nodeTemplateId = entityConsolidationData.getNodeTemplateId();
      NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, nodeTemplateId);
      if (nodeTemplate == null) {
        //The node template is not found from id in the context
        return;
      }
      List<RequirementAssignmentData> requirementAssignmentDataList = entry.getValue();
      for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList) {
        String oldRequirementId = requirementAssignmentData.getRequirementId();
        RequirementAssignment clonedRequirementAssignment = (RequirementAssignment)
                getClonedObject(requirementAssignmentData.getRequirementAssignment(),
                        RequirementAssignment.class);
        String newRequirementId = oldRequirementId + "_" + newNodeTemplateId;
        //Check if the requirement is not already present in the list of requirements of the
        // node template
        if (!toscaAnalyzerService.isRequirementExistInNodeTemplate(nodeTemplate,
                newRequirementId, clonedRequirementAssignment)) {
          DataModelUtil.removeRequirementAssignment(nodeTemplate, oldRequirementId,
                  requirementAssignmentData.getRequirementAssignment());
          DataModelUtil.addRequirementAssignment(nodeTemplate, newRequirementId,
                  clonedRequirementAssignment);
        }
      }
    }
  }

  private void updNodeGetAttrInConnectivity(
          ServiceTemplate serviceTemplate,
          EntityConsolidationData entityConsolidationData,
          String oldNodeTemplateId, String newNodeTemplateId,
          TranslationContext context,
          Map<String, UnifiedCompositionEntity> consolidationNodeTemplateIdAndType,
          boolean isNested) {
    Map<String, List<GetAttrFuncData>> nodesGetAttrIn = entityConsolidationData.getNodesGetAttrIn();
    if (MapUtils.isEmpty(nodesGetAttrIn)) {
      return;
    }

    for (Map.Entry<String, List<GetAttrFuncData>> nodesGetAttrInEntry : nodesGetAttrIn.entrySet()) {
      String sourceNodeTemplateId = nodesGetAttrInEntry.getKey();
      NodeTemplate sourceNodeTemplate =
              DataModelUtil.getNodeTemplate(serviceTemplate, sourceNodeTemplateId);
      if (!isNested && consolidationNodeTemplateIdAndType.keySet().contains(sourceNodeTemplateId)) {
        continue;
      }
      List<GetAttrFuncData> getAttrFuncDataList = nodesGetAttrInEntry.getValue();
      for (GetAttrFuncData getAttrFuncData : getAttrFuncDataList) {
        Object propertyValue =
                DataModelUtil.getPropertyValue(sourceNodeTemplate, getAttrFuncData.getFieldName());
        String newAttrName = null;
        String newGetAttrAbstractNodeTemplateId = newNodeTemplateId;
        if (!isNested) {
          newGetAttrAbstractNodeTemplateId =
                  context.getUnifiedAbstractNodeTemplateId(serviceTemplate, oldNodeTemplateId);
          newAttrName = getNewSubstitutionOutputParameterId(newNodeTemplateId, getAttrFuncData
                  .getAttributeName());
        }
        List<List<Object>> getAttrFuncValueList = extractGetAttrFunction(propertyValue);
        updateGetAttrValue(oldNodeTemplateId, getAttrFuncData, newGetAttrAbstractNodeTemplateId,
                newAttrName, getAttrFuncValueList, isNested);
      }
    }
  }

  private void updateGetAttrValue(String oldNodeTemplateId, GetAttrFuncData getAttrFuncData,
                                  String newNodeTemplateId, String newAttrName,
                                  List<List<Object>> getAttrFuncValueList, boolean isNested) {
    for (List<Object> getAttrFuncValue : getAttrFuncValueList) {
      if (oldNodeTemplateId.equals(getAttrFuncValue.get(0))
              && getAttrFuncData.getAttributeName().equals(getAttrFuncValue.get(1))) {
        getAttrFuncValue.set(0, newNodeTemplateId);
        if (!isNested) {
          getAttrFuncValue.set(1, newAttrName);
        }
      }
    }
  }

  private String getTemplateName(String nodeTypeId,
                                 Integer index) {
    String computeType = getComputeTypeSuffix(nodeTypeId);
    String templateName = "Nested_" + computeType;
    if (Objects.nonNull(index)) {
      templateName = templateName + "_" + index.toString();
    }
    return templateName;
  }

  private void updOutputParamGetAttrInConnectivity(ServiceTemplate serviceTemplate,
                                                   EntityConsolidationData entityConsolidationData,
                                                   String oldNodeTemplateId,
                                                   String newNodeTemplateId,
                                                   TranslationContext context,
                                                   boolean isNested) {
    List<GetAttrFuncData> outputParametersGetAttrIn =
            entityConsolidationData.getOutputParametersGetAttrIn();
    if (CollectionUtils.isEmpty(outputParametersGetAttrIn)) {
      return;
    }
    for (GetAttrFuncData getAttrFuncData : outputParametersGetAttrIn) {
      Object outputParamValue =
              DataModelUtil.getOuputParameter(serviceTemplate, getAttrFuncData.getFieldName())
                      .getValue();
      String newAttrName = null;
      String newGetAttrAbstractNodeTemplateId = newNodeTemplateId;
      if (!isNested) {
        newGetAttrAbstractNodeTemplateId =
                context.getUnifiedAbstractNodeTemplateId(serviceTemplate, oldNodeTemplateId);
        newAttrName = getNewSubstitutionOutputParameterId(newNodeTemplateId, getAttrFuncData
                .getAttributeName());
      }
      List<List<Object>> getAttrFuncValueList = extractGetAttrFunction(outputParamValue);
      updateGetAttrValue(oldNodeTemplateId, getAttrFuncData, newGetAttrAbstractNodeTemplateId,
              newAttrName,
              getAttrFuncValueList, isNested);
    }

  }

  private List<List<Object>> extractGetAttrFunction(Object valueObject) {

    List<List<Object>> getAttrValueList = new ArrayList<>();

    if (valueObject instanceof Map) {
      if (((Map) valueObject).containsKey(ToscaFunctions.GET_ATTRIBUTE.getDisplayName())) {
        getAttrValueList.add(
                (List<Object>) ((Map) valueObject).get(ToscaFunctions.GET_ATTRIBUTE.getDisplayName()));
      }

      for (Object key : ((Map) valueObject).keySet()) {
        getAttrValueList.addAll(extractGetAttrFunction(((Map) valueObject).get(key)));
      }


    } else if (valueObject instanceof List) {
      for (Object valueEntity : (List) valueObject) {
        getAttrValueList.addAll(extractGetAttrFunction(valueEntity));
      }
    }
    return getAttrValueList;
  }

  private boolean isIncludeToscaFunc(Object valueObject, ToscaFunctions toscaFunction) {
    if (valueObject instanceof Map) {
      if (((Map) valueObject).containsKey(toscaFunction.getDisplayName())) {
        return true;
      }

      Set<Map.Entry<String, Object>> entries = ((Map<String, Object>) valueObject).entrySet();
      for (Map.Entry<String, Object> valueObjectEntry : entries) {
        if (isIncludeToscaFunc(valueObjectEntry.getValue(), toscaFunction)) {
          return true;
        }
      }
    } else if (valueObject instanceof List) {
      for (Object valueEntity : (List) valueObject) {
        if (isIncludeToscaFunc(valueEntity, toscaFunction)) {
          return true;
        }
      }
    }
    return false;
  }

  private void createOutputParameters(UnifiedCompositionTo unifiedCompositionTo,
                                      String computeNodeType) {

    createOutputParametersForCompute(unifiedCompositionTo.getServiceTemplate(),
            unifiedCompositionTo.getSubstitutionServiceTemplate(), unifiedCompositionTo.getUnifiedCompositionDataList(),
            unifiedCompositionTo.getContext());
    createOutputParameterForPorts(unifiedCompositionTo.getSubstitutionServiceTemplate(),
            unifiedCompositionTo.getUnifiedCompositionDataList(), computeNodeType, unifiedCompositionTo.getContext());
    createOutputParameterForSubInterfaces(unifiedCompositionTo, computeNodeType);
  }

  private void createOutputParameterForPorts(
          ServiceTemplate substitutionServiceTemplate,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          String connectedComputeNodeType,
          TranslationContext context) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      if (CollectionUtils.isEmpty(portTemplateConsolidationDataList)) {
        return;
      }

      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        String newPortNodeTemplateId =
                getNewPortNodeTemplateId(portTemplateConsolidationData.getNodeTemplateId(),
                        connectedComputeNodeType,
                        unifiedCompositionData.getComputeTemplateConsolidationData());
        addOutputParameters(portTemplateConsolidationData, newPortNodeTemplateId,
                substitutionServiceTemplate, unifiedCompositionDataList, context);
      }
    }
  }

  private void createOutputParameterForSubInterfaces(UnifiedCompositionTo unifiedCompositionTo,
                                                     String connectedComputeNodeType) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionTo.getUnifiedCompositionDataList()) {
      List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
              getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
      if (CollectionUtils.isEmpty(subInterfaceTemplateConsolidationDataList)) {
        return;
      }

      for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
              subInterfaceTemplateConsolidationDataList) {
        String newSubInterfaceNodeTemplateId = getNewSubInterfaceNodeTemplateId(unifiedCompositionTo
                        .getServiceTemplate(), connectedComputeNodeType, unifiedCompositionData
                        .getComputeTemplateConsolidationData(), subInterfaceTemplateConsolidationData,
                unifiedCompositionTo.getContext());
        addOutputParameters(subInterfaceTemplateConsolidationData, newSubInterfaceNodeTemplateId,
                unifiedCompositionTo.getSubstitutionServiceTemplate(), unifiedCompositionTo.getUnifiedCompositionDataList(),
                unifiedCompositionTo.getContext());
      }
    }
  }

  private void createOutputParametersForCompute(
          ServiceTemplate serviceTemplate,
          ServiceTemplate substitutionServiceTemplate,
          List<UnifiedCompositionData>
                  unifiedCompositionDataList,
          TranslationContext context) {
    List<EntityConsolidationData> computeConsolidationDataList =
            getComputeConsolidationDataList(unifiedCompositionDataList);

    for (EntityConsolidationData computeTemplateConsolidationData : computeConsolidationDataList) {
      String newComputeNodeTemplateId =
              getNewComputeNodeTemplateId(serviceTemplate,
                      computeTemplateConsolidationData.getNodeTemplateId());
      addOutputParameters(computeTemplateConsolidationData, newComputeNodeTemplateId,
              substitutionServiceTemplate, unifiedCompositionDataList, context);
    }
  }

  private void addOutputParameters(EntityConsolidationData entityConsolidationData,
                                   String newNodeTemplateId,
                                   ServiceTemplate substitutionServiceTemplate,
                                   List<UnifiedCompositionData> unifiedCompositionDataList,
                                   TranslationContext context) {
    handleNodesGetAttrIn(entityConsolidationData, newNodeTemplateId, substitutionServiceTemplate,
            unifiedCompositionDataList, context);

    handleOutputParamGetAttrIn(entityConsolidationData, newNodeTemplateId,
            substitutionServiceTemplate, context);
  }

  private void handleOutputParamGetAttrIn(EntityConsolidationData entityConsolidationData,
                                          String newNodeTemplateId,
                                          ServiceTemplate substitutionServiceTemplate,
                                          TranslationContext context) {
    List<GetAttrFuncData> outputParametersGetAttrIn =
            entityConsolidationData.getOutputParametersGetAttrIn();
    if (!CollectionUtils.isEmpty(outputParametersGetAttrIn)) {
      for (GetAttrFuncData getAttrFuncData : outputParametersGetAttrIn) {
        createAndAddOutputParameter(newNodeTemplateId,
                substitutionServiceTemplate, getAttrFuncData, context);
      }
    }
  }

  private void handleNodesGetAttrIn(EntityConsolidationData entityConsolidationData,
                                    String newNodeTemplateId,
                                    ServiceTemplate substitutionServiceTemplate,
                                    List<UnifiedCompositionData> unifiedCompositionDataList,
                                    TranslationContext context) {
    Map<String, List<GetAttrFuncData>> getAttrIn = entityConsolidationData.getNodesGetAttrIn();
    if (MapUtils.isEmpty(getAttrIn)) {
      return;
    }
    Map<String, UnifiedCompositionEntity> consolidationNodeTemplateIdAndType =
            getAllConsolidationNodeTemplateIdAndType(unifiedCompositionDataList);
    for (Map.Entry<String, List<GetAttrFuncData>> getAttrInEntry : getAttrIn.entrySet()) {
      String sourceNodeTemplateId = getAttrInEntry.getKey();
      if (!consolidationNodeTemplateIdAndType.keySet().contains(sourceNodeTemplateId)) {
        List<GetAttrFuncData> getAttrFuncDataList = getAttrInEntry.getValue();
        for (GetAttrFuncData getAttrFuncData : getAttrFuncDataList) {
          createAndAddOutputParameter(newNodeTemplateId,
                  substitutionServiceTemplate, getAttrFuncData, context);
        }
      }
    }
  }

  private void createAndAddOutputParameter(String newNodeTemplateId,
                                           ServiceTemplate substitutionServiceTemplate,
                                           GetAttrFuncData getAttrFuncData,
                                           TranslationContext context) {
    Map<String, List<Object>> parameterValue = new HashMap<>();
    List<Object> valueList = new ArrayList<>();
    valueList.add(newNodeTemplateId);
    valueList.add(getAttrFuncData.getAttributeName());
    parameterValue.put(ToscaFunctions.GET_ATTRIBUTE.getDisplayName(), valueList);
    ParameterDefinition outputParameter = new ParameterDefinition();
    outputParameter.setValue(parameterValue);
    setOutputParameterType(substitutionServiceTemplate, newNodeTemplateId, getAttrFuncData
            .getAttributeName(), outputParameter, context);
    DataModelUtil.addOutputParameterToTopologyTemplate(substitutionServiceTemplate,
            getNewSubstitutionOutputParameterId(newNodeTemplateId, getAttrFuncData.getAttributeName()),
            outputParameter);
  }

  private void setOutputParameterType(ServiceTemplate substitutionServiceTemplate,
                                      String newNodeTemplateId,
                                      String outputParameterName,
                                      ParameterDefinition outputParameter,
                                      TranslationContext context) {
    NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(substitutionServiceTemplate,
            newNodeTemplateId);
    //Get the type and entry schema of the output parameter from the node type flat hierarchy
    String outputParameterType;
    EntrySchema outputParameterEntrySchema;
    NodeType nodeTypeWithFlatHierarchy =
            HeatToToscaUtil.getNodeTypeWithFlatHierarchy(nodeTemplate.getType(),
                    substitutionServiceTemplate, context);
    //Check if the parameter is present in the attributes
    AttributeDefinition outputParameterDefinitionFromAttributes =
            getOutputParameterDefinitionFromAttributes(nodeTypeWithFlatHierarchy, outputParameterName);
    if (Objects.nonNull(outputParameterDefinitionFromAttributes)) {
      outputParameterType = outputParameterDefinitionFromAttributes.getType();
      outputParameterEntrySchema = outputParameterDefinitionFromAttributes.getEntry_schema();
    } else {
      //If the below fails due to null pointer then we need to check if the heatToToscaMapping
      // properties and global types are in sync. Ideally the parameter should be found in either
      // properties or attributes collected from global types
      PropertyDefinition outputParameterDefinitionFromProperties =
              nodeTypeWithFlatHierarchy.getProperties().get(outputParameterName);
      outputParameterType = outputParameterDefinitionFromProperties.getType();
      outputParameterEntrySchema = outputParameterDefinitionFromProperties.getEntry_schema();
    }
    //Set the type and entry schema for the output param obtained from the node type hierarchy
    outputParameter.setType(outputParameterType);
    outputParameter.setEntry_schema(outputParameterEntrySchema);
  }

  private AttributeDefinition getOutputParameterDefinitionFromAttributes(NodeType
                                                                                 nodeTypeWithFlatHierarchy,
                                                                         String outputParameterName) {
    AttributeDefinition outputParameterDefinition = null;
    if ((Objects.nonNull(nodeTypeWithFlatHierarchy.getAttributes()))
            && (nodeTypeWithFlatHierarchy.getAttributes().containsKey(outputParameterName))) {
      outputParameterDefinition =
              nodeTypeWithFlatHierarchy.getAttributes().get(outputParameterName);
    }
    return outputParameterDefinition;
  }

  private String getNewSubstitutionOutputParameterId(String newNodeTemplateId,
                                                     String attributeName) {
    return newNodeTemplateId + "_" + attributeName;
  }

  private void addUnifiedSubstitionData(TranslationContext context, ServiceTemplate
          serviceTemplate, List<UnifiedCompositionData> unifiedCompositionDataList, String
                                                substituteNodeTemplateId) {
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      //Add compute node template mapping information
      ComputeTemplateConsolidationData computeTemplateConsolidationData =
              unifiedCompositionData.getComputeTemplateConsolidationData();
      String computeNodeTemplateId = computeTemplateConsolidationData.getNodeTemplateId();
      context.addUnifiedSubstitutionData(serviceTemplateFileName, computeNodeTemplateId,
              substituteNodeTemplateId);
      //Add Port template mapping information
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);

      if (CollectionUtils.isNotEmpty(portTemplateConsolidationDataList)) {
        for (PortTemplateConsolidationData portTemplateConsolidationData :
                portTemplateConsolidationDataList) {
          String oldPortNodeTemplateId = portTemplateConsolidationData.getNodeTemplateId();
          context.addUnifiedSubstitutionData(serviceTemplateFileName, oldPortNodeTemplateId,
                  substituteNodeTemplateId);
        }
      }
      //Add Sub-interface template mapping information
      List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
              getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
      if (CollectionUtils.isNotEmpty(subInterfaceTemplateConsolidationDataList)) {
        for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
                subInterfaceTemplateConsolidationDataList) {
          context.addUnifiedSubstitutionData(serviceTemplateFileName,
                  subInterfaceTemplateConsolidationData.getNodeTemplateId(), substituteNodeTemplateId);
        }
      }
    }
  }

  private void addIndexValueProperty(NodeTemplate nodeTemplate) {
    List<String> indexValueGetPropertyValue = new ArrayList<>();
    indexValueGetPropertyValue.add(ToscaConstants.MODELABLE_ENTITY_NAME_SELF);
    indexValueGetPropertyValue.add(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
    indexValueGetPropertyValue.add(ToscaConstants.INDEX_VALUE_PROPERTY_NAME);

    Map<String, Object> indexPropertyValue = new HashMap<>();
    Map<String, Object> properties = nodeTemplate.getProperties();
    indexPropertyValue.put(ToscaFunctions.GET_PROPERTY.getDisplayName(),
            indexValueGetPropertyValue);
    properties.put(ToscaConstants.INDEX_VALUE_PROPERTY_NAME,
            indexPropertyValue);
    nodeTemplate.setProperties(properties);
  }

  private String getSubstituteNodeTemplateId(String nodeTypeId,
                                             Integer index) {
    String nodeTemplateId = ABSTRACT_NODE_TEMPLATE_ID_PREFIX + DataModelUtil
            .getNamespaceSuffix(nodeTypeId);
    if (Objects.nonNull(index)) {
      nodeTemplateId = nodeTemplateId + "_" + index.toString();
    }
    return nodeTemplateId;
  }

  /**
   * Gets substitution node type id.
   *
   * @param serviceTemplate        the service template
   * @param unifiedCompositionData the unified composition data
   * @param index                  the index
   * @return the substitution node type id
   */
  public String getSubstitutionNodeTypeId(ServiceTemplate serviceTemplate,
                                          UnifiedCompositionData unifiedCompositionData,
                                          Integer index,
                                          TranslationContext context) {
    String computeNodeTemplateId =
            unifiedCompositionData.getComputeTemplateConsolidationData().getNodeTemplateId();
    NodeTemplate computeNodeTemplate =
            DataModelUtil.getNodeTemplate(serviceTemplate, computeNodeTemplateId);
    String computeType = computeNodeTemplate.getType();
    String globalSTName = ToscaUtil.getServiceTemplateFileName(Constants
            .GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);

    String nodeTypeId = ToscaNodeType.ABSTRACT_NODE_TYPE_PREFIX
            + DataModelUtil.getNamespaceSuffix(getIndexedGlobalNodeTypeId(computeType, context));

    context.updateUsedTimesForNestedComputeNodeType(globalSTName, computeType);

    if (Objects.nonNull(index)) {
      nodeTypeId = nodeTypeId + "_" + index.toString();
    }
    return nodeTypeId;
  }

  private NodeType handleSubstitutionGlobalNodeType(ServiceTemplate serviceTemplate,
                                                    ServiceTemplate substitutionServiceTemplate,
                                                    TranslationContext context,
                                                    String substitutionNodeTypeId) {
    NodeType substitutionNodeType = new ToscaAnalyzerServiceImpl()
            .createInitSubstitutionNodeType(substitutionServiceTemplate,
                    ToscaNodeType.VFC_ABSTRACT_SUBSTITUTE);
    ServiceTemplate globalSubstitutionServiceTemplate =
            HeatToToscaUtil.fetchGlobalSubstitutionServiceTemplate(serviceTemplate, context);
    DataModelUtil.addNodeType(globalSubstitutionServiceTemplate, substitutionNodeTypeId,
            substitutionNodeType);

    return substitutionNodeType;
  }

  private void handlePorts(ServiceTemplate serviceTemplate,
                           ServiceTemplate substitutionServiceTemplate,
                           List<UnifiedCompositionData> unifiedCompositionDataList,
                           String connectedComputeNodeType,
                           TranslationContext context) {

    if (unifiedCompositionDataList.size() > 1) {
      handleConsolidationPorts(serviceTemplate, substitutionServiceTemplate,
              unifiedCompositionDataList, connectedComputeNodeType, context);
    } else {
      handleSinglePorts(serviceTemplate, substitutionServiceTemplate, connectedComputeNodeType,
              unifiedCompositionDataList, context);
    }
  }

  private void handleSinglePorts(ServiceTemplate serviceTemplate,
                                 ServiceTemplate substitutionServiceTemplate,
                                 String connectedComputeNodeType,
                                 List<UnifiedCompositionData> unifiedCompositionDataList,
                                 TranslationContext context) {
    UnifiedCompositionData unifiedCompositionData = unifiedCompositionDataList.get(0);
    List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
            getPortTemplateConsolidationDataList(unifiedCompositionData);
    if (CollectionUtils.isEmpty(portTemplateConsolidationDataList)) {
      return;
    }
    for (PortTemplateConsolidationData portTemplateConsolidationData :
            portTemplateConsolidationDataList) {
      List<EntityConsolidationData> portConsolidationDataList = new ArrayList<>();
      portConsolidationDataList.add(portTemplateConsolidationData);
      handlePortNodeTemplate(serviceTemplate, substitutionServiceTemplate,
              portConsolidationDataList, connectedComputeNodeType,
              unifiedCompositionData.getComputeTemplateConsolidationData(),
              unifiedCompositionDataList, context);
    }
  }

    private void handleConsolidationPorts(ServiceTemplate serviceTemplate,
                                                 ServiceTemplate substitutionServiceTemplate,
                                                 List<UnifiedCompositionData> unifiedCompositionDataList,
                                                 String connectedComputeNodeType,
                                                 TranslationContext context) {
        Map<String, List<String>> portIdsPerPortType =
                UnifiedCompositionUtil.collectAllPortsOfEachTypeFromComputes(unifiedCompositionDataList);

        for (Map.Entry<String, List<String>> portIdsPerPortTypeEntry : portIdsPerPortType.entrySet()) {
            List<EntityConsolidationData> portTemplateConsolidationDataList =
                    getPortConsolidationDataList(portIdsPerPortTypeEntry.getValue(), unifiedCompositionDataList);
            if (CollectionUtils.isEmpty(portTemplateConsolidationDataList)) {
                continue;
            }

            handlePortNodeTemplate(serviceTemplate, substitutionServiceTemplate, portTemplateConsolidationDataList,
                    connectedComputeNodeType, unifiedCompositionDataList.get(0).getComputeTemplateConsolidationData(),
                    unifiedCompositionDataList, context);
        }
    }

  private void handlePortNodeTemplate(
          ServiceTemplate serviceTemplate,
          ServiceTemplate substitutionServiceTemplate,
          List<EntityConsolidationData> portTemplateConsolidationDataList,
          String connectedComputeNodeType,
          ComputeTemplateConsolidationData computeTemplateConsolidationData,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          TranslationContext context) {
    EntityConsolidationData portTemplateConsolidationData =
            portTemplateConsolidationDataList.get(0);
    NodeTemplate newPortNodeTemplate = getNodeTemplate(
            portTemplateConsolidationData.getNodeTemplateId(), serviceTemplate, context).clone();

    removeConnectivityOut(portTemplateConsolidationData, newPortNodeTemplate);
    handleProperties(serviceTemplate, newPortNodeTemplate,
            substitutionServiceTemplate, UnifiedCompositionEntity.PORT,
            portTemplateConsolidationDataList, computeTemplateConsolidationData,
            unifiedCompositionDataList, context);

    //Add subinterface_indicator property to PORT
    portTemplateConsolidationDataList.forEach(entity ->
          addPortSubInterfaceIndicatorProperty(newPortNodeTemplate.getProperties(), entity));

    String newPortNodeTemplateId =
            getNewPortNodeTemplateId(portTemplateConsolidationData
                            .getNodeTemplateId(), connectedComputeNodeType,
                    computeTemplateConsolidationData);
    //Update requirements for relationships between the consolidation entities
    handleConsolidationEntitiesRequirementConnectivity(newPortNodeTemplate,
            serviceTemplate, context);
    DataModelUtil.addNodeTemplate(substitutionServiceTemplate, newPortNodeTemplateId,
            newPortNodeTemplate);

    //Add the node template mapping in the context for handling requirement updation
    for (EntityConsolidationData data : portTemplateConsolidationDataList) {
      String newPortTemplateId = getNewPortNodeTemplateId(data.getNodeTemplateId(),
              connectedComputeNodeType, computeTemplateConsolidationData);
      context.addSubstitutionServiceTemplateUnifiedSubstitutionData(ToscaUtil
                      .getServiceTemplateFileName(serviceTemplate), data.getNodeTemplateId(),
              newPortTemplateId);
    }

  }

  private void handleSubInterfaces(UnifiedCompositionTo unifiedCompositionTo) {
    if (unifiedCompositionTo.getUnifiedCompositionDataList().size() > 1) {
      handleConsolidationSubInterfaces(unifiedCompositionTo);
    } else {
      handleSingleSubInterfaces(unifiedCompositionTo);
    }
  }

  private void handleSingleSubInterfaces(UnifiedCompositionTo unifiedCompositionTo) {
    UnifiedCompositionData unifiedCompositionData = unifiedCompositionTo.getUnifiedCompositionDataList().get(0);
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
            getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
    for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
            subInterfaceTemplateConsolidationDataList) {
      List<SubInterfaceTemplateConsolidationData> subInterfaceDataList = new ArrayList<>();
      subInterfaceDataList.add(subInterfaceTemplateConsolidationData);
      createSubInterfaceSubstitutionNodeTemplate(unifiedCompositionTo, subInterfaceDataList);
    }
  }

    private void handleConsolidationSubInterfaces(UnifiedCompositionTo unifiedCompositionTo) {
        Map<String, List<String>> portIdsPerPortType =
                UnifiedCompositionUtil.collectAllPortsOfEachTypeFromComputes(
                        unifiedCompositionTo.getUnifiedCompositionDataList());

        for (Map.Entry<String, List<String>> portIdsPerPortTypeEntry : portIdsPerPortType.entrySet()) {
            List<EntityConsolidationData> portEntityConsolidationDataList =
                    getPortConsolidationDataList(portIdsPerPortTypeEntry.getValue(),
                            unifiedCompositionTo.getUnifiedCompositionDataList());
            if (CollectionUtils.isEmpty(portEntityConsolidationDataList)) {
                continue;
            }

            List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
                    portEntityConsolidationDataList.stream().map(data -> (PortTemplateConsolidationData) data)
                                                   .collect(Collectors.toList());

            ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfacesByType =
                    UnifiedCompositionUtil.collectAllSubInterfacesOfEachTypesFromPorts(
                            portTemplateConsolidationDataList);
            Set<String> subInterfaceTypes = subInterfacesByType.keySet();
            for (String subInterfaceType : subInterfaceTypes) {
                List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
                        subInterfacesByType.get(subInterfaceType);
                createSubInterfaceSubstitutionNodeTemplate(unifiedCompositionTo,
                        subInterfaceTemplateConsolidationDataList);
            }
        }
    }

  private void createSubInterfaceSubstitutionNodeTemplate(UnifiedCompositionTo unifiedCompositionTo,
                                                          List<SubInterfaceTemplateConsolidationData>
                                                                  subInterfaceTemplateConsolidationDataList) {
    SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData =
            subInterfaceTemplateConsolidationDataList.get(0);
    Optional<PortTemplateConsolidationData> portTemplateConsolidationDataOptional =
        subInterfaceTemplateConsolidationData.getParentPortTemplateConsolidationData(unifiedCompositionTo
                .getServiceTemplate(), unifiedCompositionTo.getContext());
    if (!portTemplateConsolidationDataOptional.isPresent()) {
      return;
    }
    PortTemplateConsolidationData portTemplateConsolidationData = portTemplateConsolidationDataOptional.get();
    String originalSubInterfaceNodeTemplateId = subInterfaceTemplateConsolidationDataList.get(0)
            .getNodeTemplateId();
    NodeTemplate originalSubInterfaceNodeTemplate = DataModelUtil.getNodeTemplate(unifiedCompositionTo
            .getServiceTemplate(), originalSubInterfaceNodeTemplateId);
    if (Objects.isNull(originalSubInterfaceNodeTemplate)) {
      return;
    }
    NodeTemplate newSubInterfaceNodeTemplate = originalSubInterfaceNodeTemplate.clone();
    ComputeTemplateConsolidationData connectedComputeConsolidationData =
            getConnectedComputeConsolidationData(unifiedCompositionTo.getUnifiedCompositionDataList(),
                    portTemplateConsolidationData.getNodeTemplateId());
    if (Objects.nonNull(connectedComputeConsolidationData)) {
      NodeTemplate connectedComputeNodeTemplate = DataModelUtil.getNodeTemplate(unifiedCompositionTo
              .getServiceTemplate(), connectedComputeConsolidationData.getNodeTemplateId());
      String newSubInterfaceNodeTemplateId = getNewSubInterfaceNodeTemplateId(unifiedCompositionTo
                      .getServiceTemplate(), connectedComputeNodeTemplate.getType(), connectedComputeConsolidationData,
              subInterfaceTemplateConsolidationData, unifiedCompositionTo.getContext());
      DataModelUtil.addNodeTemplate(unifiedCompositionTo.getSubstitutionServiceTemplate(),
              newSubInterfaceNodeTemplateId, newSubInterfaceNodeTemplate);
      List<EntityConsolidationData> entityConsolidationDataList =
              new ArrayList<>(subInterfaceTemplateConsolidationDataList);
      //Remove all the existing properties as we are going to create new based on the
      // naming convention for the substitution
      handleSubInterfaceProperties(unifiedCompositionTo, originalSubInterfaceNodeTemplateId,
          newSubInterfaceNodeTemplate, entityConsolidationDataList, portTemplateConsolidationData);
      //Update requirements for relationships between the consolidation entities
      handleConsolidationEntitiesRequirementConnectivity(newSubInterfaceNodeTemplate, unifiedCompositionTo
              .getServiceTemplate(), unifiedCompositionTo.getContext());
      removeConnectivityOut(subInterfaceTemplateConsolidationData,newSubInterfaceNodeTemplate);
    }
  }

  private void handleSubInterfaceProperties(UnifiedCompositionTo unifiedCompositionTo,
                                            String subInterfaceNodeTemplateId,
                                            NodeTemplate newSubInterfaceNodeTemplate,
                                            List<EntityConsolidationData>
                                                    entityConsolidationDataList,
                                            PortTemplateConsolidationData
                                                    portTemplateConsolidationData) {
    UnifiedCompositionData unifiedCompositionData = unifiedCompositionTo.getUnifiedCompositionDataList().get(0);
    ServiceTemplate serviceTemplate = unifiedCompositionTo.getServiceTemplate();
    TranslationContext context = unifiedCompositionTo.getContext();
    newSubInterfaceNodeTemplate.setProperties(new HashMap<>());
    for (EntityConsolidationData entityConsolidationData : entityConsolidationDataList) {
      String nodeTemplateId = entityConsolidationData.getNodeTemplateId();
      Optional<List<String>> indexVarProperties =
              context.getIndexVarProperties(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
                      nodeTemplateId);
      Map<String, Object> properties =
              DataModelUtil.getNodeTemplateProperties(serviceTemplate, nodeTemplateId);
      if (MapUtils.isEmpty(properties)) {
        continue;
      }

      for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
        NodeType nodeTypeWithFlatHierarchy =
                HeatToToscaUtil.getNodeTypeWithFlatHierarchy(newSubInterfaceNodeTemplate.getType(),
                        serviceTemplate, context);
        PropertyDefinition propertyDefinition =
                nodeTypeWithFlatHierarchy.getProperties().get(propertyEntry.getKey());
        String propertyType = propertyDefinition.getType();
        //Handle service_template_filter property for subinterface as we should not create inputs
        // for this property
        if (propertyEntry.getKey().equals(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME)) {
          handleSubInterfaceServiceTemplateFilterProperty(subInterfaceNodeTemplateId, newSubInterfaceNodeTemplate,
                  propertyEntry.getKey(), propertyEntry.getValue(), portTemplateConsolidationData,
                  unifiedCompositionTo.getSubstitutionServiceTemplate());
        } else if (indexVarProperties.isPresent()
                && indexVarProperties.get().contains(propertyEntry.getKey())) {
          //Handle index property
          handleIndexVarProperty(propertyEntry.getKey(), propertyEntry.getValue(),
                  newSubInterfaceNodeTemplate);
        } else {
          Optional<String> parameterId =
                  updateProperty(serviceTemplate, nodeTemplateId, newSubInterfaceNodeTemplate,
                          propertyEntry, UnifiedCompositionEntity.SUB_INTERFACE, unifiedCompositionData
                                  .getComputeTemplateConsolidationData(), portTemplateConsolidationData,
                          unifiedCompositionTo.getUnifiedCompositionDataList(), context);
          parameterId.ifPresent(
                  parameterIdValue -> addPropertyInputParameter(propertyType,
                          unifiedCompositionTo.getSubstitutionServiceTemplate(),
                          propertyDefinition.getEntry_schema(), parameterIdValue));
        }
      }
    }
  }

  private NodeTemplate getNodeTemplate(String nodeTemplateId, ServiceTemplate serviceTemplate,
                                       TranslationContext context) {

    NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, nodeTemplateId);

    if (Objects.isNull(nodeTemplate)) {
      nodeTemplate = context
              .getCleanedNodeTemplate(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
                      nodeTemplateId);

    }
    return nodeTemplate;
  }


  private String handleCompute(ServiceTemplate serviceTemplate,
                               ServiceTemplate substitutionServiceTemplate,
                               List<UnifiedCompositionData> unifiedCompositionDataList,
                               TranslationContext context) {
    ComputeTemplateConsolidationData computeTemplateConsolidationData =
            unifiedCompositionDataList.get(0).getComputeTemplateConsolidationData();
    handleComputeNodeTemplate(serviceTemplate, substitutionServiceTemplate,
            unifiedCompositionDataList, context);
    return handleComputeNodeType(serviceTemplate, substitutionServiceTemplate,
            computeTemplateConsolidationData);
  }

  private String handleComputeNodeType(
          ServiceTemplate serviceTemplate,
          ServiceTemplate substitutionServiceTemplate,
          ComputeTemplateConsolidationData computeTemplateConsolidationData) {
    NodeTemplate computeNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
            computeTemplateConsolidationData.getNodeTemplateId());
    String computeNodeTypeId = computeNodeTemplate.getType();
    NodeType computeNodeType =
            DataModelUtil.getNodeType(serviceTemplate, computeNodeTypeId);
    DataModelUtil
            .addNodeType(substitutionServiceTemplate, computeNodeTypeId, computeNodeType);

    return computeNodeTypeId;
  }

  private void handleComputeNodeTemplate(ServiceTemplate serviceTemplate,
                                         ServiceTemplate substitutionServiceTemplate,
                                         List<UnifiedCompositionData> unifiedCompositionDataList,
                                         TranslationContext context) {
    ComputeTemplateConsolidationData computeTemplateConsolidationData =
            unifiedCompositionDataList.get(0).getComputeTemplateConsolidationData();
    NodeTemplate newComputeNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
            computeTemplateConsolidationData.getNodeTemplateId()).clone();

    removeConnectivityOut(computeTemplateConsolidationData, newComputeNodeTemplate);
    removeVolumeConnectivity(computeTemplateConsolidationData, newComputeNodeTemplate);

    List<EntityConsolidationData> computeConsolidationDataList =
            getComputeConsolidationDataList(unifiedCompositionDataList);

    handleProperties(serviceTemplate, newComputeNodeTemplate,
            substitutionServiceTemplate, COMPUTE,
            computeConsolidationDataList, computeTemplateConsolidationData, unifiedCompositionDataList,
            context);

    String newComputeNodeTemplateId = getNewComputeNodeTemplateId(serviceTemplate,
            computeTemplateConsolidationData.getNodeTemplateId());
    //Update requirements for relationships between the consolidation entities
    handleConsolidationEntitiesRequirementConnectivity(
            newComputeNodeTemplate,
            serviceTemplate, context);
    DataModelUtil
            .addNodeTemplate(substitutionServiceTemplate,
                    newComputeNodeTemplateId, newComputeNodeTemplate);
    //Add the node template mapping in the context for handling requirement updation
    for (EntityConsolidationData data : computeConsolidationDataList) {
      String newComputeTemplateId = getNewComputeNodeTemplateId(serviceTemplate,
              computeTemplateConsolidationData.getNodeTemplateId());
      context.addSubstitutionServiceTemplateUnifiedSubstitutionData(ToscaUtil
                      .getServiceTemplateFileName(serviceTemplate), data.getNodeTemplateId(),
              newComputeTemplateId);
    }
  }

  private List<EntityConsolidationData> getComputeConsolidationDataList(
          List<UnifiedCompositionData> unifiedCompositionDataList) {
    return unifiedCompositionDataList.stream()
            .map(UnifiedCompositionData::getComputeTemplateConsolidationData)
            .collect(Collectors.toList());
  }


  private void handleProperties(ServiceTemplate serviceTemplate,
                                NodeTemplate nodeTemplate,
                                ServiceTemplate substitutionServiceTemplate,
                                UnifiedCompositionEntity unifiedCompositionEntity,
                                List<EntityConsolidationData> entityConsolidationDataList,
                                ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                List<UnifiedCompositionData> unifiedCompositionDataList,
                                TranslationContext context) {
    nodeTemplate.setProperties(new HashedMap());
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(serviceTemplate,  substitutionServiceTemplate,unifiedCompositionDataList, context, nodeTemplate);
       handleNodeTemplateProperties(unifiedCompositionTo, unifiedCompositionEntity, entityConsolidationDataList, computeTemplateConsolidationData);
    //Add enrich properties from openecomp node type as input to global and substitution ST
    handleNodeTypeProperties(substitutionServiceTemplate,
            entityConsolidationDataList, nodeTemplate, unifiedCompositionEntity,
            computeTemplateConsolidationData, context);
  }

  private void addPortSubInterfaceIndicatorProperty(Map<String, Object> properties,
                                                    EntityConsolidationData entityConsolidationData) {
    if (ToggleableFeature.VLAN_TAGGING.isActive()) {
      properties.put(SUB_INTERFACE_INDICATOR_PROPERTY,
          ((PortTemplateConsolidationData) entityConsolidationData).isPortBoundToSubInterface());
    }
  }

  private void handleNodeTemplateProperties(UnifiedCompositionTo unifiedCompositionTo,
                                            UnifiedCompositionEntity unifiedCompositionEntity,
                                            List<EntityConsolidationData>
                                                    entityConsolidationDataList,
                                            ComputeTemplateConsolidationData
                                                    computeTemplateConsolidationData
                                            ) {
    List<String> propertiesWithIdenticalVal =
            consolidationService.getPropertiesWithIdenticalVal(unifiedCompositionEntity);

    for (EntityConsolidationData entityConsolidationData : entityConsolidationDataList) {
      String nodeTemplateId = entityConsolidationData.getNodeTemplateId();
      Optional<List<String>> indexVarProperties =
          unifiedCompositionTo.getContext().getIndexVarProperties(ToscaUtil.getServiceTemplateFileName(unifiedCompositionTo.getServiceTemplate()),
              nodeTemplateId);
      Map<String, Object> properties =
              DataModelUtil.getNodeTemplateProperties(unifiedCompositionTo.getServiceTemplate(),
              nodeTemplateId);
      if (MapUtils.isEmpty(properties)) {
        continue;
      }

      for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
        NodeType nodeTypeWithFlatHierarchy =
            HeatToToscaUtil.getNodeTypeWithFlatHierarchy(unifiedCompositionTo.getNodeTemplate().getType(),
                unifiedCompositionTo.getServiceTemplate(), unifiedCompositionTo.getContext());
        PropertyDefinition propertyDefinition =
                nodeTypeWithFlatHierarchy.getProperties().get(propertyEntry.getKey());
        String propertyType = propertyDefinition.getType();

        if (propertiesWithIdenticalVal.contains(propertyEntry.getKey())) {
          String parameterId =
              updateIdenticalProperty(nodeTemplateId, propertyEntry.getKey(),
                  unifiedCompositionTo.getNodeTemplate(),unifiedCompositionEntity, unifiedCompositionTo.getUnifiedCompositionDataList());

          addInputParameter(
              parameterId, propertyType,
              propertyType.equals(PropertyType.LIST.getDisplayName()) ? propertyDefinition
                  .getEntry_schema() : null,
              unifiedCompositionTo.getSubstitutionServiceTemplate());
        } else if (indexVarProperties.isPresent()
                && indexVarProperties.get().contains(propertyEntry.getKey())) {
          //Handle index property
          handleIndexVarProperty(propertyEntry.getKey(), propertyEntry.getValue(),
              unifiedCompositionTo.getNodeTemplate());
        } else {
          Optional<String> parameterId =
              updateProperty(unifiedCompositionTo.getServiceTemplate(), nodeTemplateId, unifiedCompositionTo.getNodeTemplate(), propertyEntry,
                  unifiedCompositionEntity, computeTemplateConsolidationData, null,
                  unifiedCompositionTo.getUnifiedCompositionDataList(),
                  unifiedCompositionTo.getContext());
          parameterId.ifPresent(
              parameterIdValue -> addPropertyInputParameter(propertyType,
                  unifiedCompositionTo.getSubstitutionServiceTemplate(),
                  propertyDefinition.getEntry_schema(), parameterIdValue));
        }
      }
    }
  }

  private void handleIndexVarProperty(String propertyKey, Object propertyValue,
                                      NodeTemplate nodeTemplate) {
    //Retain properties translated from %index% value in heat
    nodeTemplate.getProperties().put(propertyKey, propertyValue);
  }

  private void handleSubInterfaceServiceTemplateFilterProperty(String subInterfaceNodeTemplateId,
                                                               NodeTemplate nodeTemplate,
                                                               String propertyKey,
                                                               Object propertyValue,
                                                               PortTemplateConsolidationData
                                                                   portTemplateConsolidationData,
                                                               ServiceTemplate substitutionServiceTemplate) {
    //Retain service_template_filter (Can be present in a sub-interface resource-def)
    if (propertyValue instanceof Map) {
      Map<String, Object> serviceTemplateFilterPropertyMap = new HashMap<>((Map<String, Object>) propertyValue);
      handleCountProperty(subInterfaceNodeTemplateId, nodeTemplate, portTemplateConsolidationData,
          substitutionServiceTemplate, serviceTemplateFilterPropertyMap);
      DataModelUtil.addNodeTemplateProperty(nodeTemplate, propertyKey, serviceTemplateFilterPropertyMap);
    }
  }

  private void handleCountProperty(String subInterfaceNodeTemplateId, NodeTemplate nodeTemplate,
                                   PortTemplateConsolidationData portTemplateConsolidationData,
                                   ServiceTemplate substitutionServiceTemplate,
                                   Map<String, Object> serviceTemplatePropertyMap) {
    String countInputParameterId = getSubInterfaceInputParameterId(nodeTemplate.getType(), subInterfaceNodeTemplateId,
            ToscaConstants.SERVICE_TEMPLATE_FILTER_COUNT, portTemplateConsolidationData);
    EntrySchema entrySchema = new EntrySchema();
    entrySchema.setType(PropertyType.FLOAT.getDisplayName());
    addInputParameter(countInputParameterId, PropertyType.LIST.getDisplayName(), entrySchema,
        substitutionServiceTemplate);
    Map<String, List<String>> countPropertyValueInputParam = getPropertyValueInputParam(countInputParameterId);
    serviceTemplatePropertyMap.remove(ToscaConstants.COUNT_PROPERTY_NAME);
    serviceTemplatePropertyMap.put(ToscaConstants.COUNT_PROPERTY_NAME, countPropertyValueInputParam);
  }

  private void handleNodeTypeProperties(ServiceTemplate substitutionServiceTemplate,
                                        List<EntityConsolidationData> entityConsolidationDataList,
                                        NodeTemplate nodeTemplate,
                                        UnifiedCompositionEntity compositionEntity,
                                        ComputeTemplateConsolidationData
                                                computeTemplateConsolidationData,
                                        TranslationContext context) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    Optional<NodeType> enrichNodeType;
    List<String> enrichProperties;

    if (compositionEntity.equals(UnifiedCompositionEntity.PORT)) {
      enrichNodeType =
              toscaAnalyzerService.fetchNodeType(ToscaNodeType.NETWORK_PORT,
                      context.getGlobalServiceTemplates().values());
      enrichProperties = TranslationContext.getEnrichPortResourceProperties();
      if (!enrichNodeType.isPresent() || Objects.isNull(enrichProperties)) {
        return;
      }
    } else {
      return;
    }

    Map<String, Object> nodeTemplateProperties = nodeTemplate.getProperties();
    Map<String, PropertyDefinition> enrichNodeTypeProperties = enrichNodeType.get().getProperties();
    if (Objects.nonNull(enrichNodeTypeProperties)) {
      for (String enrichPropertyName : enrichProperties) {
        handleEntityConsolidationDataNodeTypeProperties(
                enrichPropertyName, substitutionServiceTemplate,
                enrichNodeType.get(), nodeTemplate, compositionEntity, computeTemplateConsolidationData,
                entityConsolidationDataList, nodeTemplateProperties, context);
      }
    }
  }

  private void handleEntityConsolidationDataNodeTypeProperties(String enrichPropertyName,
                                                               ServiceTemplate substitutionServiceTemplate,
                                                               NodeType enrichNodeType,
                                                               NodeTemplate nodeTemplate,
                                                               UnifiedCompositionEntity compositionEntity,
                                                               ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                               List<EntityConsolidationData> entityConsolidationDataList,
                                                               Map<String, Object> nodeTemplateProperties,
                                                               TranslationContext context) {

    String propertyType;

    for (EntityConsolidationData entityConsolidationData : entityConsolidationDataList) {
      String nodeTemplateId = entityConsolidationData.getNodeTemplateId();

      String inputParamId =
              getParameterId(nodeTemplateId, nodeTemplate, enrichPropertyName,
                      compositionEntity, computeTemplateConsolidationData, null);
      Map<String, String> propertyValMap = new HashMap<>();

      context
              .addNewPropertyIdToNodeTemplate(
                      ToscaUtil.getServiceTemplateFileName(substitutionServiceTemplate),
                      inputParamId, nodeTemplateProperties.get(enrichPropertyName));

      if (nodeTemplateProperties.containsKey(enrichPropertyName)) {
        handleExistingEnrichedProperty(enrichPropertyName, nodeTemplateProperties, inputParamId);
      } else {
        propertyValMap.put(ToscaFunctions.GET_INPUT.getDisplayName(), inputParamId);
        nodeTemplate.getProperties().put(enrichPropertyName, propertyValMap);
      }
      propertyType =
              enrichNodeType.getProperties().get(enrichPropertyName).getType();

      addPropertyInputParameter(propertyType, substitutionServiceTemplate, enrichNodeType
                      .getProperties().get(enrichPropertyName).getEntry_schema(),
              inputParamId);

    }
  }

  private void handleExistingEnrichedProperty(String enrichPropertyName,
                                              Map<String, Object> nodeTemplateProperties,
                                              String inputParamId) {
    Object enrichedProperty = nodeTemplateProperties.get(enrichPropertyName);
    if (!isPropertyContainsToscaFunction(enrichedProperty)) {
      Map<String, Object> propertyWithGetInput = new HashMap<>();
      propertyWithGetInput.put(ToscaFunctions.GET_INPUT.getDisplayName(), inputParamId);
      nodeTemplateProperties.put(enrichPropertyName, propertyWithGetInput);
    }
  }


  private boolean isPropertyContainsToscaFunction(Object propertyValue) {
    ToscaFunctions[] values = ToscaFunctions.values();
    for (ToscaFunctions toscaFunction : values) {
      if (isIncludeToscaFunc(propertyValue, toscaFunction)) {
        return true;
      }
    }

    return false;
  }


  private void addPropertyInputParameter(String propertyType,
                                         ServiceTemplate substitutionServiceTemplate,
                                         EntrySchema entrySchema, String parameterId) {
    if (Objects.isNull(propertyType)) {
      return;
    }
    if (isParameterBelongsToEnrichedPortProperties(parameterId)) {
      addInputParameter(parameterId,
              propertyType,
              propertyType.equals(PropertyType.LIST.getDisplayName()) ? entrySchema : null,
              substitutionServiceTemplate);
    } else if (isPropertySimpleType(propertyType)) {
      addInputParameter(parameterId, PropertyType.LIST.getDisplayName(),
              DataModelUtil.createEntrySchema(propertyType.toLowerCase(), null, null),
              substitutionServiceTemplate);

    } else if (propertyType.equals(PropertyTypeExt.JSON.getDisplayName()) ||
            (Objects.nonNull(entrySchema) && isPropertySimpleType(entrySchema.getType()))) {
      addInputParameter(parameterId, PropertyType.LIST.getDisplayName(),
              DataModelUtil.createEntrySchema(PropertyTypeExt.JSON.getDisplayName(), null, null),
              substitutionServiceTemplate);
    } else {
      addInputParameter(parameterId, analyzeParameterType(propertyType), DataModelUtil
                      .createEntrySchema(analyzeEntrySchemaType(propertyType, entrySchema), null, null),
              substitutionServiceTemplate);
    }
  }

  private boolean isParameterBelongsToEnrichedPortProperties(String parameterId) {
    List enrichPortResourceProperties = TranslationContext.getEnrichPortResourceProperties();

    for (int i = 0; i < enrichPortResourceProperties.size(); i++) {
      if (parameterId.contains((CharSequence) enrichPortResourceProperties.get(i))) {
        return true;
      }
    }

    return false;
  }

  private boolean isPropertySimpleType(String propertyType) {
    return !Objects.isNull(propertyType)
            && (PropertyType.getSimplePropertyTypes().contains(propertyType.toLowerCase()));
  }

  private String analyzeParameterType(String propertyType) {
    return propertyType.equalsIgnoreCase(PropertyType.LIST.getDisplayName()) ? PropertyType.LIST
            .getDisplayName() : propertyType;
  }

  private String analyzeEntrySchemaType(String propertyType, EntrySchema entrySchema) {
    return propertyType.equalsIgnoreCase(PropertyType.LIST.getDisplayName()) && entrySchema != null ?
            entrySchema.getType() : null;
  }

  private void handleConsolidationEntitiesRequirementConnectivity(NodeTemplate nodeTemplate,
                                                                  ServiceTemplate serviceTemplate,
                                                                  TranslationContext context) {
    List<Map<String, RequirementAssignment>> nodeTemplateRequirements = DataModelUtil
            .getNodeTemplateRequirementList(nodeTemplate);
    if (CollectionUtils.isEmpty(nodeTemplateRequirements)) {
      return;
    }

    for (Map<String, RequirementAssignment> requirement : nodeTemplateRequirements) {
      for (Map.Entry<String, RequirementAssignment> entry : requirement.entrySet()) {
        RequirementAssignment requirementAssignment = entry.getValue();
        String requirementNode = requirementAssignment.getNode();
        String unifiedNodeTemplateId =
                context.getUnifiedSubstitutionNodeTemplateId(serviceTemplate,
                        requirementNode);
        if (unifiedNodeTemplateId != null) {
          //Update the node id in the requirement
          requirementAssignment.setNode(unifiedNodeTemplateId);
        }
      }
    }
    nodeTemplate.setRequirements(nodeTemplateRequirements);
  }

  /**
   * Update the node references in the volume relationship templates.
   *
   * @param serviceTemplate the service template
   * @param context         the context
   */
  private void updateVolumeRelationshipTemplate(ServiceTemplate serviceTemplate,
                                                String relationshipId,
                                                TranslationContext context) {
    Map<String, RelationshipTemplate> relationshipTemplates = DataModelUtil
            .getRelationshipTemplates(serviceTemplate);
    if (relationshipTemplates != null) {
      RelationshipTemplate relationshipTemplate = relationshipTemplates.get(relationshipId);
      if (relationshipTemplate != null) {
        String relationshipTemplateType = relationshipTemplate.getType();
        if (relationshipTemplateType.equals(ToscaRelationshipType.CINDER_VOLUME_ATTACHES_TO)) {
          handleCinderVolumeAttachmentRelationshipTemplate(serviceTemplate,
                  relationshipTemplate, context);
        }
      }
    }
  }


  private void handleCinderVolumeAttachmentRelationshipTemplate(ServiceTemplate
                                                                        substitutionServiceTemplate,
                                                                RelationshipTemplate
                                                                        relationshipTemplate,
                                                                TranslationContext context) {
    Map<String, Object> properties = relationshipTemplate.getProperties();
    properties.computeIfPresent(HeatConstants.INSTANCE_UUID_PROPERTY_NAME, (key, value) ->
            context.getUnifiedAbstractNodeTemplateId(substitutionServiceTemplate,
                    (String) value));
  }

  private String updateIdenticalProperty(String nodeTemplateId, String propertyId,
                                         NodeTemplate nodeTemplate,
                                         UnifiedCompositionEntity unifiedCompositionEntity,
                                         List<UnifiedCompositionData> unifiedCompositionDataList) {

    String inputParamId = null;
    Map<String, Object> propertyVal = new HashMap<>();

    switch (unifiedCompositionEntity) {
      case COMPUTE:
        inputParamId = COMPUTE_IDENTICAL_VALUE_PROPERTY_PREFIX + propertyId
                + COMPUTE_IDENTICAL_VALUE_PROPERTY_SUFFIX;
        propertyVal.put(ToscaFunctions.GET_INPUT.getDisplayName(), inputParamId);
        nodeTemplate.getProperties().put(propertyId, propertyVal);
        break;
      case PORT:
        String portType = ConsolidationDataUtil.getPortType(nodeTemplateId);
        ComputeTemplateConsolidationData computeTemplateConsolidationData =
                getConnectedComputeConsolidationData(unifiedCompositionDataList, nodeTemplateId);
        inputParamId = getInputParamIdForPort(nodeTemplateId, propertyId, portType, computeTemplateConsolidationData);
        propertyVal.put(ToscaFunctions.GET_INPUT.getDisplayName(), inputParamId);
        nodeTemplate.getProperties().put(propertyId, propertyVal);
        break;
      default:
        break;
    }
    return inputParamId;
  }

  private String getInputParamIdForPort(String nodeTemplateId, String propertyId, String portType,
                                        ComputeTemplateConsolidationData computeTemplateConsolidationData) {
    String inputParamId;
    if (Objects.isNull(computeTemplateConsolidationData)
            || computeTemplateConsolidationData.getPorts().get(portType).size() > 1) {
      inputParamId =
              UnifiedCompositionEntity.PORT.getDisplayName().toLowerCase() + "_" + nodeTemplateId + "_" +
                      propertyId;

    } else {
      inputParamId =
              UnifiedCompositionEntity.PORT.getDisplayName().toLowerCase() + "_" + portType + "_"
                      + propertyId;
    }
    return inputParamId;
  }

  private void addInputParameter(String parameterId,
                                 String parameterType,
                                 EntrySchema entrySchema,
                                 ServiceTemplate serviceTemplate) {

    ParameterDefinition parameterDefinition = DataModelUtil.createParameterDefinition(parameterType, null,  true,
            null, entrySchema, null);


    DataModelUtil
            .addInputParameterToTopologyTemplate(serviceTemplate, parameterId, parameterDefinition);
  }

  // Return the input parameter Id which is used in the new property value if there is one
  private Optional<String> updateProperty(
          ServiceTemplate serviceTemplate,
          String nodeTemplateId, NodeTemplate nodeTemplate,
          Map.Entry<String, Object> propertyEntry,
          UnifiedCompositionEntity compositionEntity,
          ComputeTemplateConsolidationData computeTemplateConsolidationData,
          PortTemplateConsolidationData portTemplateConsolidationData,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          TranslationContext context) {

    if (handleGetAttrFromConsolidationNodes(serviceTemplate, nodeTemplateId, nodeTemplate,
            propertyEntry, unifiedCompositionDataList, context)) {
      return Optional.empty();
    }


    String inputParamId =
            getParameterId(nodeTemplateId, nodeTemplate, propertyEntry.getKey(), compositionEntity,
                    computeTemplateConsolidationData, portTemplateConsolidationData);
    Map<String, List<String>> propertyVal = getPropertyValueInputParam(inputParamId);
    nodeTemplate.getProperties().put(propertyEntry.getKey(), propertyVal);
    return Optional.of(inputParamId);
  }

  private Map<String, List<String>> getPropertyValueInputParam(String inputParamId) {
    Map<String, List<String>> propertyVal = new HashMap<>();
    List<String> getInputFuncParams = new ArrayList<>();
    getInputFuncParams.add(inputParamId);
    getInputFuncParams.add(ToscaConstants.INDEX_VALUE_PROPERTY_NAME);
    propertyVal.put(ToscaFunctions.GET_INPUT.getDisplayName(), getInputFuncParams);
    return propertyVal;
  }

  private boolean handleGetAttrFromConsolidationNodes(
          ServiceTemplate serviceTemplate,
          String nodeTemplateId, NodeTemplate nodeTemplate,
          Map.Entry<String, Object> propertyEntry,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          TranslationContext context) {
    Map<String, UnifiedCompositionEntity> consolidationNodeTemplateIdAndType =
            getAllConsolidationNodeTemplateIdAndType(unifiedCompositionDataList);

    Set<String> consolidationNodeTemplateIds = consolidationNodeTemplateIdAndType.keySet();
    Map<String, String> entityIdToType = ConsolidationService.getConsolidationEntityIdToType(
            serviceTemplate, context.getConsolidationData());
    boolean includeGetAttrFromConsolidationNodes = false;
    boolean includeGetAttrFromOutsideNodes = false;
    boolean isGetAttrFromConsolidationIsFromSameType = false;
    List<List<Object>> getAttrFunctionList = extractGetAttrFunction(propertyEntry.getValue());
    for (List<Object> getAttrFunc : getAttrFunctionList) {
      String getAttrNodeId = (String) getAttrFunc.get(0);
      if (consolidationNodeTemplateIds.contains(getAttrNodeId)) {
        includeGetAttrFromConsolidationNodes = true;
        if (isGetAttrNodeTemplateFromSameType(nodeTemplateId, getAttrNodeId, entityIdToType)) {
          isGetAttrFromConsolidationIsFromSameType = true;
        }
      } else {
        includeGetAttrFromOutsideNodes = true;
      }
    }
    if ((includeGetAttrFromConsolidationNodes && includeGetAttrFromOutsideNodes)
            ||
            (includeGetAttrFromConsolidationNodes && isIncludeToscaFunc(propertyEntry.getValue(),
                    ToscaFunctions.GET_INPUT))) {
      //This case is currently not supported - this property will be ignored
      return true;
    } else if (includeGetAttrFromConsolidationNodes && !isGetAttrFromConsolidationIsFromSameType) {
      Object clonedPropertyValue = getClonedPropertyValue(propertyEntry);
      List<List<Object>> clonedGetAttrFuncList = extractGetAttrFunction(clonedPropertyValue);
      for (List<Object> getAttrFunc : clonedGetAttrFuncList) {
        String targetNodeTemplateId = (String) getAttrFunc.get(0);
        if (consolidationNodeTemplateIds.contains(targetNodeTemplateId)) {
          updatePropertyGetAttrFunc(serviceTemplate, unifiedCompositionDataList,
                  consolidationNodeTemplateIdAndType, targetNodeTemplateId, getAttrFunc, context);
        }
      }
      nodeTemplate.getProperties().put(propertyEntry.getKey(), clonedPropertyValue);
      return true;
    }
    return false;
  }

  private boolean isGetAttrNodeTemplateFromSameType(String sourceNodeTemplateId,
                                                    String targetNodeTemplateId,
                                                    Map<String, String> nodeTemplateIdToType) {

    if (Objects.isNull(nodeTemplateIdToType.get(sourceNodeTemplateId))
            || Objects.isNull(nodeTemplateIdToType.get(targetNodeTemplateId))) {
      return false;
    }

    return nodeTemplateIdToType.get(sourceNodeTemplateId).equals(nodeTemplateIdToType
            .get(targetNodeTemplateId));
  }

  private void updatePropertyGetAttrFunc(
          ServiceTemplate serviceTemplate,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          Map<String, UnifiedCompositionEntity> consolidationNodeTemplateIdAndType,
          String targetNodeTemplateId,
          List<Object> getAttrFunc, TranslationContext context) {
    UnifiedCompositionEntity targetCompositionEntity =
            consolidationNodeTemplateIdAndType.get(targetNodeTemplateId);
    String targetNewNodeTemplateId =
            getNewNodeTemplateId(serviceTemplate, unifiedCompositionDataList, targetNodeTemplateId,
                    targetCompositionEntity, context);
    getAttrFunc.set(0, targetNewNodeTemplateId);
  }

  private String getNewNodeTemplateId(ServiceTemplate serviceTemplate,
                                      List<UnifiedCompositionData> unifiedCompositionDataList,
                                      String nodeTemplateId,
                                      UnifiedCompositionEntity compositionEntity,
                                      TranslationContext context) {
    String newNodeTemplateId = nodeTemplateId;
    String nodeTemplateIdGeneratorImpl = unifiedSubstitutionNodeTemplateIdGeneratorImplMap.get(compositionEntity);
    UnifiedSubstitutionNodeTemplateIdGenerator nodeTemplateIdGenerator =
            CommonMethods.newInstance(nodeTemplateIdGeneratorImpl, UnifiedSubstitutionNodeTemplateIdGenerator.class);
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(serviceTemplate, null,
            unifiedCompositionDataList, context, null);
    Optional<String> generatedNodeTemplateId = nodeTemplateIdGenerator.generate(unifiedCompositionTo, nodeTemplateId);
    if (generatedNodeTemplateId.isPresent()) {
      newNodeTemplateId = generatedNodeTemplateId.get();
    }
    return newNodeTemplateId;
  }

  private String getNewNodeTemplateId(String origNodeTemplateId,
                                      String serviceTemplateFileName,
                                      ServiceTemplate serviceTemplate,
                                      TranslationContext context) {
    ConsolidationData consolidationData = context.getConsolidationData();

    if (isIdIsOfExpectedType(origNodeTemplateId, UnifiedCompositionEntity.PORT,
            serviceTemplateFileName,
            context)) {
      return handleIdOfPort(origNodeTemplateId, serviceTemplateFileName, consolidationData);
    } else if (isIdIsOfExpectedType(origNodeTemplateId, COMPUTE,
            serviceTemplateFileName, context)) {
      NodeTemplate nodeTemplate =
              getComputeNodeTemplate(origNodeTemplateId, serviceTemplate, context);
      return getComputeTypeSuffix(nodeTemplate.getType());
    }

    return null;
  }

  private Object getClonedPropertyValue(Map.Entry<String, Object> propertyEntry) {
    if (propertyEntry.getValue() instanceof Map) {
      return getClonedObject(propertyEntry.getValue(), Map.class);
    } else if (propertyEntry.getValue() instanceof List) {
      return getClonedObject(propertyEntry.getValue(), List.class);
    }
    return propertyEntry.getValue();
  }


  private String getParameterId(String nodeTemplateId, NodeTemplate nodeTemplate, String propertyId,
                                UnifiedCompositionEntity unifiedCompositionEntity,
                                ComputeTemplateConsolidationData
                                        computeTemplateConsolidationData,
                                PortTemplateConsolidationData portTemplateConsolidationData) {
    String paramterId = propertyId;
    switch (unifiedCompositionEntity) {
      case COMPUTE:
        paramterId = COMPUTE.getDisplayName().toLowerCase() + "_"
                + getComputeTypeSuffix(nodeTemplate.getType()) + "_" + propertyId;
        break;
      case PORT:
        String portType = ConsolidationDataUtil.getPortType(nodeTemplateId);
        if (Objects.isNull(computeTemplateConsolidationData)
                || computeTemplateConsolidationData.getPorts().get(portType).size() > 1) {
          paramterId = UnifiedCompositionEntity.PORT.getDisplayName().toLowerCase() + "_"
                  + nodeTemplateId + "_" + propertyId;
        } else {
          paramterId = UnifiedCompositionEntity.PORT.getDisplayName().toLowerCase() + "_" + portType + "_"
                  + propertyId;
        }
        break;
      case SUB_INTERFACE:
        paramterId = getSubInterfaceInputParameterId(nodeTemplate.getType(), nodeTemplateId, propertyId,
                portTemplateConsolidationData);
        break;
      default:
        break;
    }
    return paramterId;
  }

  private String getSubInterfaceInputParameterId(String type,
                                                 String nodeTemplateId,
                                                 String propertyId,
                                                 PortTemplateConsolidationData portTemplateConsolidationData) {
    String subInterfaceType = getSubInterfaceTypeSuffix(type);
    if (Objects.isNull(portTemplateConsolidationData)
        || portTemplateConsolidationData.isSubInterfaceNodeTemplateIdParameter(type)) {
      return UnifiedCompositionEntity.SUB_INTERFACE.getDisplayName().toLowerCase() + "_"
          + nodeTemplateId + "_" + propertyId;
    }
    return UnifiedCompositionEntity.SUB_INTERFACE.getDisplayName().toLowerCase() + "_"
        + subInterfaceType + "_" + propertyId;
  }

  private void removeConnectivityOut(EntityConsolidationData entityConsolidationData,
                                     NodeTemplate nodeTemplate) {
    if (MapUtils.isEmpty(entityConsolidationData.getNodesConnectedOut())) {
      return;
    }

    for (List<RequirementAssignmentData> requirementAssignmentDataList : entityConsolidationData
            .getNodesConnectedOut().values()) {
      for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList) {
        DataModelUtil.removeRequirementsAssignment(nodeTemplate.getRequirements(),
                requirementAssignmentData.getRequirementId());
      }
      if (nodeTemplate.getRequirements().isEmpty()) {
        nodeTemplate.setRequirements(null);
      }
    }
  }

  private void removeVolumeConnectivity(
          ComputeTemplateConsolidationData computeTemplateConsolidationData,
          NodeTemplate computeNodeTemplate) {
    if (MapUtils.isEmpty(computeTemplateConsolidationData.getVolumes())) {
      return;
    }
    Collection<List<RequirementAssignmentData>> volumeCollection =
            computeTemplateConsolidationData.getVolumes().values();
    for (List<RequirementAssignmentData> requirementAssignmentDataList : volumeCollection) {
      for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList) {
        DataModelUtil.removeRequirementsAssignment(computeNodeTemplate.getRequirements(),
                requirementAssignmentData.getRequirementId());
      }
    }
    if (computeNodeTemplate.getRequirements().isEmpty()) {
      computeNodeTemplate.setRequirements(null);
    }
  }

  private void createIndexInputParameter(ServiceTemplate substitutionServiceTemplate) {
    ParameterDefinition indexParameterDefinition =
            DataModelUtil.createParameterDefinition(PropertyType.INTEGER.getDisplayName(),
                    "Index value of this substitution service template runtime instance",
                    false, createIndexValueConstraint(), null, 0);
    DataModelUtil.addInputParameterToTopologyTemplate(substitutionServiceTemplate,
            ToscaConstants.INDEX_VALUE_PROPERTY_NAME, indexParameterDefinition);
  }


  private List<Constraint> createIndexValueConstraint() {
    List<Constraint> constraints;
    constraints = new ArrayList<>();
    Constraint constraint = new Constraint();
    constraint.setGreater_or_equal(0);
    constraints.add(constraint);
    return constraints;
  }

  private Optional<UnifiedComposition> getUnifiedCompositionInstance(UnifiedCompositionMode mode) {
    String unifiedCompositionImplClassName =
            unifiedCompositionImplMap.get(mode.name()).getImplementationClass();
    if (StringUtils.isEmpty(unifiedCompositionImplClassName)) {
      return Optional.empty();
    }
    return Optional
            .of(CommonMethods.newInstance(unifiedCompositionImplClassName, UnifiedComposition.class));
  }

  private Optional<Map<String, Object>> createAbstractSubstitutionProperties(
          ServiceTemplate serviceTemplate,
          Map<String, ParameterDefinition> substitutionTemplateInputs,
          List<UnifiedCompositionData> unifiedCompositionDataList,
          TranslationContext context) {
    Map<String, Object> abstractSubstituteProperties = new LinkedHashMap<>();
    //Since all the computes have the same type fetching the type from the first entry
    NodeTemplate firstComputeNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
            unifiedCompositionDataList.get(0)
                    .getComputeTemplateConsolidationData().getNodeTemplateId());
    String computeType = getComputeTypeSuffix(firstComputeNodeTemplate.getType());
    for (Map.Entry<String, ParameterDefinition> input : substitutionTemplateInputs.entrySet()) {
      String substitutionTemplateInputName = input.getKey();
      ParameterDefinition inputParameterDefinition = input.getValue();
      String inputType = inputParameterDefinition.getType();
      UnifiedCompositionEntity inputUnifiedCompositionEntity =
              getInputCompositionEntity(substitutionTemplateInputName);

      if (isIdenticalValueProperty(substitutionTemplateInputName, inputUnifiedCompositionEntity)
          || !inputType.equalsIgnoreCase(PropertyType.LIST.getDisplayName())) {
        //Handle identical value properties
        Optional<String> identicalValuePropertyName =
            getIdenticalValuePropertyName(substitutionTemplateInputName,
                inputUnifiedCompositionEntity);

        identicalValuePropertyName.ifPresent(propertyName -> updateIdenticalPropertyValue(propertyName,
            substitutionTemplateInputName, inputUnifiedCompositionEntity,
            unifiedCompositionDataList.get(0), serviceTemplate, abstractSubstituteProperties,
            context));
        continue;
      }

      //Check if the input is of type compute, port or sub interface
      List<Object> abstractPropertyValue = new ArrayList<>();
      switch (inputUnifiedCompositionEntity) {
        case COMPUTE:
          createAbstractComputeProperties(unifiedCompositionDataList,
                  substitutionTemplateInputName, serviceTemplate, abstractPropertyValue);
          break;
        case PORT:
          createAbstractPortProperties(unifiedCompositionDataList, substitutionTemplateInputName,
                  computeType, serviceTemplate, abstractPropertyValue);
          break;
        case SUB_INTERFACE:
          createAbstractSubInterfaceProperties(unifiedCompositionDataList,
                  substitutionTemplateInputName, serviceTemplate, abstractPropertyValue);
          break;
        default:
          break;
      }
      //Add the property only if it has at least one non-null value
      if (abstractPropertyValue.stream().anyMatch(Objects::nonNull)) {
        updateAbstractPropertyValue(substitutionTemplateInputName, inputParameterDefinition,
                abstractPropertyValue, abstractSubstituteProperties);
      }
    }
    return Optional.ofNullable(abstractSubstituteProperties);
  }

  private void createAbstractComputeProperties(List<UnifiedCompositionData>
                                                       unifiedCompositionDataList,
                                               String substitutionTemplateInputName,
                                               ServiceTemplate serviceTemplate,
                                               List<Object> abstractPropertyValue) {
    for (UnifiedCompositionData compositionData : unifiedCompositionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData =
              compositionData.getComputeTemplateConsolidationData();
      Object propertyValue = getComputePropertyValue(substitutionTemplateInputName,
              serviceTemplate, computeTemplateConsolidationData);
      if (!(propertyValue instanceof Optional)) {
        abstractPropertyValue.add(propertyValue);
      }
    }
  }

  private void createAbstractPortProperties(List<UnifiedCompositionData>
                                                    unifiedCompositionDataList,
                                            String substitutionTemplateInputName,
                                            String computeType,
                                            ServiceTemplate serviceTemplate,
                                            List<Object> abstractPropertyValue) {
    for (UnifiedCompositionData compositionData : unifiedCompositionDataList) {
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(compositionData);
      //Get the input type for this input whether it is of type
      // port_<port_node_template_id>_<property_name> or port_<port_type>_<property_name>
      PropertyInputType portInputType = getPortInputType(substitutionTemplateInputName,
              compositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        //Get the port property value
        String portNodeTemplateId = portTemplateConsolidationData.getNodeTemplateId();
        Object propertyValue = getPortPropertyValue(substitutionTemplateInputName,
                computeType, portInputType, serviceTemplate,
                portNodeTemplateId);
        //If the value object is Optional.empty it implies that the property name was not
        // found in the input name
        if (!(propertyValue instanceof Optional)) {
          abstractPropertyValue.add(propertyValue);
        }
      }
    }
  }

  private void createAbstractSubInterfaceProperties(List<UnifiedCompositionData>
                                                            unifiedCompositionDataList,
                                                    String substitutionTemplateInputName,
                                                    ServiceTemplate serviceTemplate,
                                                    List<Object> abstractPropertyValue) {
    for (UnifiedCompositionData compositionData : unifiedCompositionDataList) {
      List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
              getSubInterfaceTemplateConsolidationDataList(compositionData);
      //Get the input type for this input whether it is of type
      // subInterface_<subinterface_node_template_id>_<property_name> or
      // subInterface_<subinterface_type>_<property_name>
      PropertyInputType subInterfaceInputType =
              getSubInterfaceInputType(substitutionTemplateInputName, compositionData);
      for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
              subInterfaceTemplateConsolidationDataList) {
        //Get the subInterface property value
        String subInterfaceNodeTemplateId = subInterfaceTemplateConsolidationData
                .getNodeTemplateId();
        NodeTemplate subInterfaceNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
                subInterfaceNodeTemplateId);
        String subInterfaceType = getSubInterfaceTypeSuffix(subInterfaceNodeTemplate
                .getType());
        Object propertyValue = getSubInterfacePropertyValue(substitutionTemplateInputName,
                subInterfaceType, subInterfaceInputType, serviceTemplate,
                subInterfaceNodeTemplateId);
        //If the value object is Optional.empty it implies that the property name was not
        // found in the input name
        if (!(propertyValue instanceof Optional)) {
          abstractPropertyValue.add(propertyValue);
        }
      }
    }
  }

  private void updateAbstractPropertyValue(String substitutionTemplateInputName,
                                           ParameterDefinition parameterDefinition,
                                           List<Object> abstractPropertyValue,
                                           Map<String, Object> abstractSubstituteProperties) {
    if (abstractPropertyValue.size() > 1) {
      abstractSubstituteProperties.put(substitutionTemplateInputName, abstractPropertyValue);
    } else {
      Object propertyValue = abstractPropertyValue.get(0);
      String entrySchemaType = parameterDefinition.getEntry_schema().getType();
      if (PropertyType.getSimplePropertyTypes().contains(entrySchemaType.toLowerCase())
              || entrySchemaType.equals(PropertyTypeExt.JSON.getDisplayName())) {
        abstractSubstituteProperties.put(substitutionTemplateInputName, abstractPropertyValue);
      } else {
        abstractSubstituteProperties.put(substitutionTemplateInputName, propertyValue);
      }
    }
  }

  private void updateIdenticalPropertyValue(String identicalValuePropertyName,
                                            String substitutionTemplateInputName,
                                            UnifiedCompositionEntity entity,
                                            UnifiedCompositionData unifiedCompositionData,
                                            ServiceTemplate serviceTemplate,
                                            Map<String, Object> abstractSubstituteProperties,
                                            TranslationContext context) {
    Optional<Object> identicalPropertyValueByType =
            getIdenticalPropertyValueByType(identicalValuePropertyName, substitutionTemplateInputName,
                    entity, unifiedCompositionData, serviceTemplate, context);

    if (identicalPropertyValueByType.isPresent()) {
      abstractSubstituteProperties
              .put(substitutionTemplateInputName, identicalPropertyValueByType.get());

    }


  }

  private Optional<Object> getIdenticalPropertyValueByType(String identicalValuePropertyName,
                                                           String substitutionTemplateInputName,
                                                           UnifiedCompositionEntity entity,
                                                           UnifiedCompositionData
                                                                   unifiedCompositionData,
                                                           ServiceTemplate serviceTemplate,
                                                           TranslationContext context) {

    ComputeTemplateConsolidationData computeTemplateConsolidationData =
            unifiedCompositionData.getComputeTemplateConsolidationData();

    Optional<Object> identicalPropertyValue = Optional.empty();
    switch (entity) {
      case COMPUTE:
        identicalPropertyValue =
                getIdenticalPropertyValue(identicalValuePropertyName, serviceTemplate,
                        computeTemplateConsolidationData, context);
        break;
      case OTHER:
        identicalPropertyValue =
                getIdenticalPropertyValue(identicalValuePropertyName, serviceTemplate,
                        computeTemplateConsolidationData, context);
        break;
      case PORT:
        PropertyInputType portInputType = getPortInputType(substitutionTemplateInputName,
                                         unifiedCompositionData);
        Optional <PortTemplateConsolidationData>  portTemplateConsolidationData =
                unifiedCompositionData.getPortTemplateConsolidationDataList()
                        .stream()
                        .filter(s -> substitutionTemplateInputName.
                                contains(getPropertyInputPrefix(s.getNodeTemplateId(),
                                ConsolidationDataUtil.getPortType(s.getNodeTemplateId()),
                                portInputType, UnifiedCompositionEntity.PORT)))
                        .findFirst();

        if(portTemplateConsolidationData.isPresent()) {
          return getIdenticalPropertyValue(identicalValuePropertyName, serviceTemplate,
                  portTemplateConsolidationData.get(), context);
        }
        break;
      default:
        break;
    }
    return identicalPropertyValue;
  }


  private PropertyInputType getPortInputType(String inputName,
                                             UnifiedCompositionData unifiedCompositionData) {
    String portInputPrefix = UnifiedCompositionEntity.PORT.getDisplayName().toLowerCase() + "_";
    ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionData
            .getComputeTemplateConsolidationData();
    List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
            getPortTemplateConsolidationDataList(unifiedCompositionData);
    //Scan the available port node template ids to check if the input is of the form
    // "port_<port_node_template_id>_<property_name>"
    if (portTemplateConsolidationDataList.stream().map(EntityConsolidationData::getNodeTemplateId)
            .map(portNodeTemplateId -> portInputPrefix + portNodeTemplateId).anyMatch(inputName::startsWith)) {
      return PropertyInputType.NODE_TEMPLATE_ID;
    }
    //Check whether the input is of the form "port_<port_type>_<property_name>"
    Set<String> portTypes = computeTemplateConsolidationData.getPorts().keySet();
    if (portTypes.stream().map(portType -> portInputPrefix + portType + "_").anyMatch(inputName::startsWith)) {
      return PropertyInputType.TYPE;
    }
    return PropertyInputType.OTHER;
  }

  private PropertyInputType getSubInterfaceInputType(String inputName,
                                                     UnifiedCompositionData unifiedCompositionData) {
    String subInterfaceInputPrefix = UnifiedCompositionEntity.SUB_INTERFACE.getDisplayName().toLowerCase()
            + "_";
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
            getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
    //Scan the available port node template ids to check if the input is of the form
    // "subinterface_<subinterface_node_template_id>_<property_name>"
    if (subInterfaceTemplateConsolidationDataList.stream().map(EntityConsolidationData::getNodeTemplateId)
            .map(subInterfaceNodeTemplateId -> subInterfaceInputPrefix
                    + subInterfaceNodeTemplateId)
            .anyMatch(inputName::startsWith)) {
      return PropertyInputType.NODE_TEMPLATE_ID;
    }
    //Check whether the input is of the form "subinterface_<subinterface_type>_<property_name>"
    Set<String> subInterfaceTypes = new HashSet<>();
    List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
            getPortTemplateConsolidationDataList(unifiedCompositionData);
    for (PortTemplateConsolidationData portTemplateConsolidationData :
            portTemplateConsolidationDataList) {
      ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfaceTypeToEntity = ArrayListMultimap.create();
      portTemplateConsolidationData.copyMappedInto(subInterfaceTypeToEntity);
      subInterfaceTypes.addAll(subInterfaceTypeToEntity.keySet());
    }

    if (subInterfaceTypes.stream().map(UnifiedCompositionUtil::getSubInterfaceTypeSuffix)
            .map(subInterfaceTypeSuffix -> subInterfaceInputPrefix + subInterfaceTypeSuffix + "_")
            .anyMatch(inputName::startsWith)) {
      return PropertyInputType.TYPE;
    }
    return PropertyInputType.OTHER;
  }

  private void cleanServiceTemplate(ServiceTemplate serviceTemplate,
                                    EntityConsolidationData entity,
                                    TranslationContext context) {
    removeNodeTemplateFromServiceTemplate(serviceTemplate, entity, context);
    updateHeatStackGroup(serviceTemplate, entity, context);
    updateSubstitutionMapping(serviceTemplate, context);
  }

  private void removeNodeTemplateFromServiceTemplate(ServiceTemplate serviceTemplate,
                                                     EntityConsolidationData entity,
                                                     TranslationContext context) {
    String nodeTemplateIdToRemove = entity.getNodeTemplateId();
    Map<String, NodeTemplate> nodeTemplates =
            serviceTemplate.getTopology_template().getNode_templates();
    NodeTemplate nodeTemplateToRemove =
            nodeTemplates.get(nodeTemplateIdToRemove);
    nodeTemplates.remove(nodeTemplateIdToRemove);

    context.addCleanedNodeTemplate(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
            nodeTemplateIdToRemove,
            entity.getClass() == ComputeTemplateConsolidationData.class
                    ? COMPUTE
                    : UnifiedCompositionEntity.PORT,
            nodeTemplateToRemove);

  }

  private void removeCleanedNodeType(String cleanedNodeTemplateId,
                                     ServiceTemplate serviceTemplate,
                                     TranslationContext context) {
    NodeTemplate cleanedNodeTemplate =
            context
                    .getCleanedNodeTemplate(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
                            cleanedNodeTemplateId);
    String typeToRemove = cleanedNodeTemplate.getType();

    if (Objects.nonNull(typeToRemove)
            && serviceTemplate.getNode_types().containsKey(typeToRemove)) {
      serviceTemplate.getNode_types().remove(typeToRemove);
    }
  }

  private void updateHeatStackGroup(ServiceTemplate serviceTemplate,
                                    EntityConsolidationData entity,
                                    TranslationContext context) {
    Map<String, GroupDefinition> groups = serviceTemplate.getTopology_template()
            .getGroups() == null ? new HashMap<>()
            : serviceTemplate.getTopology_template().getGroups();
    String nodeRelatedAbstractNodeId =
            context.getUnifiedAbstractNodeTemplateId(serviceTemplate, entity.getNodeTemplateId());

    for (Map.Entry<String, GroupDefinition> groupEntry : groups.entrySet()) {
      GroupDefinition groupDefinition = groupEntry.getValue();
      if (isHeatStackGroup(groupDefinition.getType())) {
        updateGroupMembersWithNewUnifiedNodeTemplateId(entity, nodeRelatedAbstractNodeId,
                groupEntry);
      }
    }
  }

  private void updateGroupMembersWithNewUnifiedNodeTemplateId(
          EntityConsolidationData entity,
          String newNodetemplateId,
          Map.Entry<String, GroupDefinition> groupEntry) {
    List<String> members = groupEntry.getValue().getMembers();
    if (members.contains(entity.getNodeTemplateId())) {
      members.remove(entity.getNodeTemplateId());
      if (!members.contains(newNodetemplateId)) {
        members.add(newNodetemplateId);
      }
    }
    groupEntry.getValue().setMembers(members);
  }

  private void updateSubstitutionMapping(ServiceTemplate serviceTemplate,
                                         TranslationContext context) {
    SubstitutionMapping substitutionMappings =
            DataModelUtil.getSubstitutionMappings(serviceTemplate);
    if (Objects.nonNull(substitutionMappings)) {

      if (Objects.nonNull(substitutionMappings.getRequirements())) {
        updateSubstitutionMappingRequirements(substitutionMappings.getRequirements(),
                serviceTemplate, context);
      }

      if (Objects.nonNull(substitutionMappings.getCapabilities())) {
        updateSubstitutionMappingCapabilities(substitutionMappings.getCapabilities(),
                serviceTemplate, context);
      }
    }
  }

  private void updateSubstitutionMappingRequirements(Map<String, List<String>>
                                                             substitutionMappingRequirements,
                                                     ServiceTemplate serviceTemplate,
                                                     TranslationContext context) {
    for (Map.Entry<String, List<String>> entry : substitutionMappingRequirements.entrySet()) {
      List<String> requirement = entry.getValue();
      String oldNodeTemplateId = requirement.get(0);
      String newAbstractNodeTemplateId = context.getUnifiedAbstractNodeTemplateId(serviceTemplate,
              requirement.get(0));
      String newSubstitutionNodeTemplateId = context.getUnifiedSubstitutionNodeTemplateId(
              serviceTemplate, oldNodeTemplateId);
      if (Objects.nonNull(newAbstractNodeTemplateId)
              && Objects.nonNull(newSubstitutionNodeTemplateId)) {
        requirement.set(0, newAbstractNodeTemplateId);
        String newRequirementValue = requirement.get(1) + "_" + newSubstitutionNodeTemplateId;
        requirement.set(1, newRequirementValue);
      }
    }
  }

  private void updateSubstitutionMappingCapabilities(Map<String, List<String>>
                                                             substitutionMappingCapabilities,
                                                     ServiceTemplate serviceTemplate,
                                                     TranslationContext context) {
    for (Map.Entry<String, List<String>> entry : substitutionMappingCapabilities.entrySet()) {
      List<String> capability = entry.getValue();
      String oldNodeTemplateId = capability.get(0);
      String newAbstractNodeTemplateId = context.getUnifiedAbstractNodeTemplateId(serviceTemplate,
              capability.get(0));
      String newSubstitutionNodeTemplateId = context.getUnifiedSubstitutionNodeTemplateId(
              serviceTemplate, oldNodeTemplateId);
      if (Objects.nonNull(newAbstractNodeTemplateId)
              && Objects.nonNull(newSubstitutionNodeTemplateId)) {
        capability.set(0, newAbstractNodeTemplateId);
        String newRequirementValue = capability.get(1) + "_" + newSubstitutionNodeTemplateId;
        capability.set(1, newRequirementValue);
      }
    }
  }

  private void updateHeatStackGroupNestedComposition(ServiceTemplate serviceTemplate,
                                                     EntityConsolidationData entity,
                                                     TranslationContext context) {
    Map<String, GroupDefinition> groups = serviceTemplate.getTopology_template()
            .getGroups() == null ? new HashMap<>() : serviceTemplate.getTopology_template().getGroups();
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    Optional<String> nestedNodeTemplateId =
            context.getUnifiedNestedNodeTemplateId(serviceTemplateFileName, entity.getNodeTemplateId());
    if (nestedNodeTemplateId.isPresent()) {
      for (Map.Entry<String, GroupDefinition> groupEntry : groups.entrySet()) {
        GroupDefinition groupDefinition = groupEntry.getValue();
        if (isHeatStackGroup(groupDefinition.getType())) {
          updateGroupMembersWithNewUnifiedNodeTemplateId(entity, nestedNodeTemplateId.get(),
                  groupEntry);
        }
      }
    }
  }

  private void handleNestedNodeTemplateInMainServiceTemplate(String nestedNodeTemplateId,
                                                             ServiceTemplate mainServiceTemplate,
                                                             ServiceTemplate nestedServiceTemplate,
                                                             TranslationContext context) {
    NodeTemplate nestedNodeTemplate = DataModelUtil.getNodeTemplate(mainServiceTemplate,
            nestedNodeTemplateId);
    if (Objects.isNull(nestedNodeTemplate)) {
      return;
    }

    updateNestedNodeTemplateProperties(nestedServiceTemplate, nestedNodeTemplate, context);

    Optional<String> unifiedNestedNodeTypeId = context
            .getUnifiedNestedNodeTypeId(
                    ToscaUtil.getServiceTemplateFileName(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME),
                    nestedNodeTemplate.getType());
    unifiedNestedNodeTypeId
            .ifPresent(unifiedNestedNodeTypeIdVal -> updateNestedNodeTemplate(
                    unifiedNestedNodeTypeIdVal, nestedNodeTemplateId, nestedNodeTemplate,
                    mainServiceTemplate, context));
  }

  private void updateNestedNodeTemplateProperties(ServiceTemplate nestedServiceTemplate,
                                                  NodeTemplate nestedNodeTemplate,
                                                  TranslationContext context) {

    Map<String, Object> newPropertyInputParamIds =
            context.getAllNewPropertyInputParamIdsPerNodeTenplateId(ToscaUtil
                    .getServiceTemplateFileName(nestedServiceTemplate));

    for (Map.Entry<String, Object> entry : newPropertyInputParamIds.entrySet()) {
      if (Objects.nonNull(entry.getValue())) {
        Object value = getClonedObject(entry.getValue());
        nestedNodeTemplate.getProperties().put(entry.getKey(), value);
      }
    }

    String subNodeType =
            nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getNode_type();
    nestedNodeTemplate.setType(subNodeType);

  }

  private void handleSubstitutionMappingInNestedServiceTemplate(
          String newNestedNodeType,
          ServiceTemplate nestedServiceTemplate,
          TranslationContext context) {
    if (Objects.isNull(newNestedNodeType)) {
      return;
    }

    Set<String> relatedNestedNodeTypeIds =
            context.getAllRelatedNestedNodeTypeIds();

    SubstitutionMapping substitutionMappings =
            nestedServiceTemplate.getTopology_template().getSubstitution_mappings();
    if (!relatedNestedNodeTypeIds.contains(substitutionMappings.getNode_type())) {
      substitutionMappings.setNode_type(newNestedNodeType);
    }
  }

  private void updateNestedNodeTemplate(String newNestedNodeTypeId,
                                        String nestedNodeTemplateId,
                                        NodeTemplate nestedNodeTemplate,
                                        ServiceTemplate mainServiceTemplate,
                                        TranslationContext context) {
    String mainSTName = ToscaUtil.getServiceTemplateFileName(mainServiceTemplate);
    String globalSTName =
            ToscaUtil.getServiceTemplateFileName(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    int index =
            context.getHandledNestedComputeNodeTemplateIndex(globalSTName, newNestedNodeTypeId);
    String newNodeTemplateId =
            Constants.ABSTRACT_NODE_TEMPLATE_ID_PREFIX + getComputeTypeSuffix(newNestedNodeTypeId)
                    + "_" + index;

    nestedNodeTemplate.setType(newNestedNodeTypeId);
    mainServiceTemplate.getTopology_template().getNode_templates().remove(nestedNodeTemplateId);
    mainServiceTemplate.getTopology_template().getNode_templates()
            .put(newNodeTemplateId, nestedNodeTemplate);

    context.addUnifiedNestedNodeTemplateId(mainSTName, nestedNodeTemplateId, newNodeTemplateId);
  }

  private void handleNestedNodeTypesInGlobalSubstituteServiceTemplate(
          String origNestedNodeTypeId,
          String newNestedNodeTypeId,
          ServiceTemplate globalSubstitutionServiceTemplate,
          TranslationContext context) {
    Set<String> relatedNestedNodeTypeIds =
            context.getAllRelatedNestedNodeTypeIds();

    Map<String, NodeType> nodeTypes = globalSubstitutionServiceTemplate.getNode_types();
    if (!relatedNestedNodeTypeIds.contains(origNestedNodeTypeId)) {
      NodeType nested = DataModelUtil.getNodeType(globalSubstitutionServiceTemplate,
              origNestedNodeTypeId);
      setNewValuesForNestedNodeType(origNestedNodeTypeId, newNestedNodeTypeId, nested, nodeTypes);
    } else {
      NodeType nested =
              (NodeType) DataModelUtil.getClonedObject(
                      DataModelUtil.getNodeType(globalSubstitutionServiceTemplate, origNestedNodeTypeId));
      nested.setDerived_from(ToscaNodeType.VFC_ABSTRACT_SUBSTITUTE);
      nodeTypes.put(newNestedNodeTypeId, nested);
    }
    context.addUnifiedNestedNodeTypeId(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
            origNestedNodeTypeId, newNestedNodeTypeId);
  }

  private void setNewValuesForNestedNodeType(String origNestedNodeType,
                                             String newNestedNodeTypeId,
                                             NodeType nested,
                                             Map<String, NodeType> nodeTypes) {
    if (Objects.nonNull(nested)) {
      nested.setDerived_from(ToscaNodeType.VFC_ABSTRACT_SUBSTITUTE);
      nodeTypes.remove(origNestedNodeType);
      nodeTypes.put(newNestedNodeTypeId, nested);
    }
  }

  private Optional<String> getNewNestedNodeTypeId(ServiceTemplate nestedServiceTemplate,
                                                  TranslationContext context) {
    FileComputeConsolidationData fileComputeConsolidationData =
            context.getConsolidationData().getComputeConsolidationData()
                    .getFileComputeConsolidationData(
                            ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate));

    if (Objects.nonNull(fileComputeConsolidationData)) {
      String nestedNodeTypePrefix = ToscaNodeType.ABSTRACT_NODE_TYPE_PREFIX + "heat.";
      return Optional
              .of(nestedNodeTypePrefix + getComputeTypeInNestedFile(fileComputeConsolidationData));
    }
    return Optional.empty();
  }

  private String getComputeTypeInNestedFile(
          FileComputeConsolidationData fileComputeConsolidationData) {
    List<TypeComputeConsolidationData> typeComputeConsolidationDatas =
            new ArrayList<>(fileComputeConsolidationData.getAllTypeComputeConsolidationData());
    if (typeComputeConsolidationDatas.isEmpty()) {
      return null;
    } else {
      String computeNodeType = fileComputeConsolidationData.getAllComputeTypes().iterator().next();
      return getComputeTypeSuffix(computeNodeType);
    }
  }

  private void handleGetAttrInAbstractNodeTemplate(ServiceTemplate serviceTemplate,
                                                   TranslationContext context,
                                                   String serviceTemplateFileName,
                                                   NodeTemplate abstractNodeTemplate) {
    Map<String, Object> properties =
            abstractNodeTemplate == null || abstractNodeTemplate.getProperties() == null
                    ? new HashMap<>()
                    : abstractNodeTemplate.getProperties();
    for (Object propertyValue : properties.values()) {
      List<List<Object>> getAttrList = extractGetAttrFunction(propertyValue);
      for (List<Object> getAttrFuncValue : getAttrList) {
        String origNodeTemplateId = (String) getAttrFuncValue.get(0);
        Optional<String> nestedNodeTemplateId = context.getUnifiedNestedNodeTemplateId(ToscaUtil
                .getServiceTemplateFileName(serviceTemplate), origNodeTemplateId);
        if (nestedNodeTemplateId.isPresent()) {
          getAttrFuncValue.set(0, nestedNodeTemplateId.get());
        } else {
          replaceGetAttrNodeIdAndAttrName(serviceTemplate, context, serviceTemplateFileName,
                  getAttrFuncValue);
        }
      }
    }
  }

  private void replaceGetAttrNodeIdAndAttrName(ServiceTemplate serviceTemplate,
                                               TranslationContext context,
                                               String serviceTemplateFileName,
                                               List<Object> getAttrFuncValue) {
    String origNodeTemplateId = (String) getAttrFuncValue.get(0);
    String attributeName = (String) getAttrFuncValue.get(1);

    String unifiedAbstractNodeTemplateId =
            context.getUnifiedAbstractNodeTemplateId(serviceTemplate, origNodeTemplateId);

    if (Objects.isNull(unifiedAbstractNodeTemplateId)) {
      return;
    }

    String newNodeTemplateId =
            getNewNodeTemplateId(origNodeTemplateId, serviceTemplateFileName, serviceTemplate, context);

    String newSubstitutionOutputParameterId =
            getNewSubstitutionOutputParameterId(newNodeTemplateId, attributeName);

    getAttrFuncValue.set(0, unifiedAbstractNodeTemplateId);
    getAttrFuncValue.set(1, newSubstitutionOutputParameterId);
  }

  private NodeTemplate getComputeNodeTemplate(String origNodeTemplateId,
                                              ServiceTemplate serviceTemplate,
                                              TranslationContext context) {
    NodeTemplate computeNodeTemplate =
            DataModelUtil.getNodeTemplate(serviceTemplate, origNodeTemplateId);
    if (computeNodeTemplate == null) {
      computeNodeTemplate =
              context.getCleanedNodeTemplate(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
                      origNodeTemplateId);
    }
    return computeNodeTemplate;
  }

  private String handleIdOfPort(String origNodeTemplateId, String serviceTemplateFileName,
                                ConsolidationData consolidationData) {
    Optional<Pair<String, ComputeTemplateConsolidationData>>
            computeTypeAndComputeTemplateByPortId =
            getComputeTypeAndComputeTemplateByPortId(origNodeTemplateId, serviceTemplateFileName,
                    consolidationData);
    if (computeTypeAndComputeTemplateByPortId.isPresent()) {
      Pair<String, ComputeTemplateConsolidationData> computeIdToComputeData =
              computeTypeAndComputeTemplateByPortId.get();
      return getNewPortNodeTemplateId(origNodeTemplateId, computeIdToComputeData.getKey(),
              computeIdToComputeData.getValue());
    }

    return null;
  }

  private Optional<Pair<String, ComputeTemplateConsolidationData>>
  getComputeTypeAndComputeTemplateByPortId(String portId, String serviceTemplateFileName,
                                           ConsolidationData consolidationData) {
    FileComputeConsolidationData fileComputeConsolidationData =
            consolidationData.getComputeConsolidationData()
                    .getFileComputeConsolidationData(serviceTemplateFileName);
    Set<String> computeTypes =
            fileComputeConsolidationData.getAllComputeTypes();

    for (String computeType : computeTypes) {
      Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDatas =
              fileComputeConsolidationData.getTypeComputeConsolidationData(computeType)
                      .getAllComputeTemplateConsolidationData();

      for (ComputeTemplateConsolidationData compute : computeTemplateConsolidationDatas) {
        if (ConsolidationDataUtil.isComputeReferenceToPortId(compute, portId)) {
          return Optional.of(new ImmutablePair<>(computeType, compute));
        }
      }
    }

    return Optional.empty();
  }

  private boolean isIdIsOfExpectedType(String id,
                                       UnifiedCompositionEntity expectedUnifiedCompositionEntity,
                                       String serviceTemplateFileName,
                                       TranslationContext context) {
    UnifiedSubstitutionData unifiedSubstitutionData =
            context.getUnifiedSubstitutionData().get(serviceTemplateFileName);
    if (Objects.isNull(unifiedSubstitutionData)) {
      return false;
    }

    UnifiedCompositionEntity actualUnifiedCompositionEntity =
            unifiedSubstitutionData.getCleanedNodeTemplateCompositionEntity(id);

    return actualUnifiedCompositionEntity == null ? false
            : actualUnifiedCompositionEntity.equals(expectedUnifiedCompositionEntity);
  }

  private boolean isHeatStackGroup(String groupType) {
    return groupType.equals(ToscaGroupType.HEAT_STACK);
  }

  private Object getPortPropertyValue(String inputName,
                                      String computeType,
                                      PropertyInputType portInputType,
                                      ServiceTemplate serviceTemplate,
                                      String portNodeTemplateId) {
    //Get the input prefix to extract the property name from the input name
    String portType = ConsolidationDataUtil.getPortType(portNodeTemplateId);
    String portInputPrefix = getPropertyInputPrefix(
            portNodeTemplateId, portType, portInputType, UnifiedCompositionEntity.PORT);
    //Get the property name from the input
    Optional<String> propertyName = getPropertyNameFromInput(inputName,
            UnifiedCompositionEntity.PORT, computeType, portInputPrefix);
    //Get the property value from the node template
    if (propertyName.isPresent()) {
      NodeTemplate portNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
              portNodeTemplateId);
      if (Objects.nonNull(portNodeTemplate)) {
        return getPropertyValueFromNodeTemplate(propertyName.get(), portNodeTemplate);
      }
    }
    return Optional.empty();
  }

  private Object getComputePropertyValue(
          String inputName,
          ServiceTemplate serviceTemplate,
          ComputeTemplateConsolidationData computeTemplateConsolidationData) {
    NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
            computeTemplateConsolidationData.getNodeTemplateId());
    String nodeType = getComputeTypeSuffix(nodeTemplate.getType());
    Optional<String> propertyName =
            getPropertyNameFromInput(inputName, COMPUTE, nodeType, null);
    if (propertyName.isPresent()) {
      return getPropertyValueFromNodeTemplate(propertyName.get(), nodeTemplate);
    }
    return Optional.empty();
  }

  private Object getSubInterfacePropertyValue(String inputName,
                                              String subInterfaceTypeSuffix,
                                              PropertyInputType propertyInputType,
                                              ServiceTemplate serviceTemplate,
                                              String subInterfaceNodeTemplateId) {
    //Get the input prefix to extract the property name from the input name
    String propertyInputPrefix = getPropertyInputPrefix(subInterfaceNodeTemplateId,
            subInterfaceTypeSuffix, propertyInputType, UnifiedCompositionEntity.SUB_INTERFACE);
    //Get the property name from the input
    Optional<String> propertyName = getPropertyNameFromInput(inputName,
            UnifiedCompositionEntity.SUB_INTERFACE, null, propertyInputPrefix);
    //Get the property value from the node template
    if (propertyName.isPresent()) {
      NodeTemplate subInterfaceNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
              subInterfaceNodeTemplateId);
      if (Objects.nonNull(subInterfaceNodeTemplate)) {
        return getPropertyValueFromNodeTemplate(propertyName.get(), subInterfaceNodeTemplate);
      }
    }
    return Optional.empty();
  }

  private Optional<Object> getIdenticalPropertyValue(String identicalValuePropertyName,
                                                     ServiceTemplate serviceTemplate,
                                                     EntityConsolidationData entity,
                                                     TranslationContext context) {
    NodeTemplate nodeTemplate =
            getNodeTemplate(entity.getNodeTemplateId(), serviceTemplate, context);

    Object propertyValueFromNodeTemplate =
            getPropertyValueFromNodeTemplate(identicalValuePropertyName, nodeTemplate);

    return Objects.isNull(propertyValueFromNodeTemplate) ? Optional.empty()
            : Optional.of(propertyValueFromNodeTemplate);
  }

  private UnifiedCompositionEntity getInputCompositionEntity(String inputName) {
    UnifiedCompositionEntity inputCompositionEntity = UnifiedCompositionEntity.OTHER;
    if (inputName.indexOf('_') != -1) {
      String inputType = inputName.substring(0, inputName.indexOf('_'));
      if (inputType.equalsIgnoreCase(COMPUTE.getDisplayName())) {
        inputCompositionEntity = COMPUTE;
      } else if (inputType.equalsIgnoreCase(UnifiedCompositionEntity.PORT.getDisplayName())) {
        inputCompositionEntity = UnifiedCompositionEntity.PORT;
      } else if (inputType.equalsIgnoreCase(UnifiedCompositionEntity.SUB_INTERFACE
              .getDisplayName())) {
        inputCompositionEntity = UnifiedCompositionEntity.SUB_INTERFACE;
      }
    }
    return inputCompositionEntity;
  }

  private Optional<String> getPropertyNameFromInput(
          String inputName,
          UnifiedCompositionEntity compositionEntity,
          String entityType, String propertyInputPrefix) {
    String propertyName = null;
    switch (compositionEntity) {
      case COMPUTE:
        propertyName = inputName.substring(inputName.lastIndexOf(entityType)
                + entityType.length() + 1);
        break;
      case PORT:
      case SUB_INTERFACE:
        if (inputName.startsWith(propertyInputPrefix)) {
          propertyName = inputName.split(propertyInputPrefix)[1];
        }
        break;
      default:
        break;
    }
    return Optional.ofNullable(propertyName);
  }

  private String getPropertyInputPrefix(String nodeTemplateId,
                                        String propertyEntityType,
                                        PropertyInputType propertyInputType,
                                        UnifiedCompositionEntity unifiedCompositionEntity) {
    String propertyInputPrefix = unifiedCompositionEntity.getDisplayName().toLowerCase() + "_";
    if (propertyInputType == PropertyInputType.NODE_TEMPLATE_ID) {
      propertyInputPrefix += nodeTemplateId + "_";
    } else if (propertyInputType == PropertyInputType.TYPE) {
      propertyInputPrefix += propertyEntityType + "_";
    }
    return propertyInputPrefix;
  }

  private boolean isIdenticalValueProperty(String inputName,
                                           UnifiedCompositionEntity unifiedCompositionEntity) {

    List<String> identicalValuePropertyList =
            consolidationService.getPropertiesWithIdenticalVal(unifiedCompositionEntity);

    StringBuilder builder = getPropertyValueStringBuilder(unifiedCompositionEntity);
    if (Objects.isNull(builder)) {
      return false;
    }

    boolean isMatchingProperty = Pattern.matches(builder.toString(), inputName);
    return isMatchingProperty
            && isPropertyFromIdenticalValuesList(inputName, unifiedCompositionEntity,
            identicalValuePropertyList);
  }

  private boolean isPropertyFromIdenticalValuesList(String inputName,
                                                    UnifiedCompositionEntity unifiedCompositionEntity,
                                                    List<String> identicalValuePropertyList) {
    switch (unifiedCompositionEntity) {
      case COMPUTE:
        return identicalValuePropertyList.contains(getIdenticalValuePropertyName(inputName,
                unifiedCompositionEntity).get());

      case OTHER:
        return identicalValuePropertyList.contains(getIdenticalValuePropertyName(inputName,
                unifiedCompositionEntity).get());

      case PORT:
        return getPortPropertyNameFromInput(inputName, identicalValuePropertyList).isPresent();

      default:
        return false;
    }
  }

  private Optional<String> getPortPropertyNameFromInput(String inputName,
                                                        List<String> identicalValuePropertyList) {
    for (String identicalProperty : identicalValuePropertyList) {
      if (inputName.endsWith(identicalProperty)) {
        return Optional.of(identicalProperty);
      }
    }
    return Optional.empty();
  }

  private StringBuilder getPropertyValueStringBuilder(
          UnifiedCompositionEntity unifiedCompositionEntity) {

    switch (unifiedCompositionEntity) {
      case COMPUTE:
        return getComputePropertyValueStringBuilder();

      case OTHER:
        return getComputePropertyValueStringBuilder();

      case PORT:
        return getPortPropertyValueStringBuilder();

      case SUB_INTERFACE:
        return getSubInterfacePropertyValueStringBuilder();

      default:
        return null;
    }
  }

  private StringBuilder getPortPropertyValueStringBuilder() {
    StringBuilder builder;
    builder = new StringBuilder(PORT_IDENTICAL_VALUE_PROPERTY_PREFIX);
    builder.append(".+");
    return builder;
  }

  private StringBuilder getComputePropertyValueStringBuilder() {
    StringBuilder builder;
    builder = new StringBuilder(COMPUTE_IDENTICAL_VALUE_PROPERTY_PREFIX);
    builder.append("[a-z]+");
    builder.append(COMPUTE_IDENTICAL_VALUE_PROPERTY_SUFFIX);
    return builder;
  }

  private StringBuilder getSubInterfacePropertyValueStringBuilder() {
    StringBuilder builder;
    builder = new StringBuilder(SUB_INTERFACE_PROPERTY_VALUE_PREFIX);
    builder.append(".+");
    return builder;
  }

  private Optional<String> getIdenticalValuePropertyName(String input,
                                                         UnifiedCompositionEntity
                                                                 unifiedCompositionEntity) {
    switch (unifiedCompositionEntity) {
      case COMPUTE:
        return Optional.of(input.split("_")[1]);

      case OTHER:
        return Optional.of(input.split("_")[1]);

      case PORT:
        return getPortPropertyNameFromInput(input, consolidationService
                .getPropertiesWithIdenticalVal(unifiedCompositionEntity));

      default:
        return Optional.empty();
    }
  }

  private Object getPropertyValueFromNodeTemplate(String propertyName, NodeTemplate nodeTemplate) {
    Map<String, Object> nodeTemplateProperties = nodeTemplate.getProperties();
    if (nodeTemplateProperties != null) {
      Object propertyValue;
      if (propertyName.startsWith(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME)) {
        propertyValue = getServiceTemplateFilterPropertyValue(propertyName, nodeTemplateProperties);
      } else {
        propertyValue = nodeTemplateProperties.get(propertyName);
        propertyValue = getClonedObject(propertyValue);
      }
      return propertyValue;
    }
    return null;
  }

  private Object getServiceTemplateFilterPropertyValue(String propertyName,
                                                       Map<String, Object> nodeTemplateProperties) {
    Object propertyValue = null;
    Object serviceTemplateFilterProperties =
        nodeTemplateProperties.get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
    String serviceTemplateFilterPropertyName =
        propertyName.replace(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME + "_", "");

    if (Objects.nonNull(serviceTemplateFilterProperties)
        && serviceTemplateFilterProperties instanceof Map) {
      propertyValue = ((Map<String, Object>) serviceTemplateFilterProperties).get(serviceTemplateFilterPropertyName);
    }
    return propertyValue;
  }

  private Map<String, UnifiedCompositionEntity> getAllConsolidationNodeTemplateIdAndType(
          List<UnifiedCompositionData> unifiedCompositionDataList) {

    Map<String, UnifiedCompositionEntity> consolidationNodeTemplateIdAndType = new HashMap<>();
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      ComputeTemplateConsolidationData computeTemplateConsolidationData =
              unifiedCompositionData.getComputeTemplateConsolidationData();
      if (Objects.nonNull(computeTemplateConsolidationData)) {
        consolidationNodeTemplateIdAndType.put(computeTemplateConsolidationData.getNodeTemplateId(), COMPUTE);
      }
      List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
              getSubInterfaceTemplateConsolidationDataList(unifiedCompositionData);
      for (SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData :
              subInterfaceTemplateConsolidationDataList) {
        consolidationNodeTemplateIdAndType.put(subInterfaceTemplateConsolidationData.getNodeTemplateId(),
                UnifiedCompositionEntity.SUB_INTERFACE);
      }
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
              getPortTemplateConsolidationDataList(unifiedCompositionData);
      for (PortTemplateConsolidationData portTemplateConsolidationData :
              portTemplateConsolidationDataList) {
        consolidationNodeTemplateIdAndType.put(portTemplateConsolidationData.getNodeTemplateId(),
                UnifiedCompositionEntity.PORT);
      }
      NestedTemplateConsolidationData nestedTemplateConsolidationData =
              unifiedCompositionData.getNestedTemplateConsolidationData();
      if (Objects.nonNull(nestedTemplateConsolidationData)) {
        consolidationNodeTemplateIdAndType
                .put(nestedTemplateConsolidationData.getNodeTemplateId(),
                        UnifiedCompositionEntity.NESTED);
      }
    }
    return consolidationNodeTemplateIdAndType;
  }

  private List<PortTemplateConsolidationData> getPortTemplateConsolidationDataList(
          UnifiedCompositionData unifiedCompositionData) {
    return unifiedCompositionData.getPortTemplateConsolidationDataList() == null ? new
            ArrayList<>() : unifiedCompositionData.getPortTemplateConsolidationDataList();
  }

  private enum PropertyInputType {
    NODE_TEMPLATE_ID,
    TYPE,
    OTHER
  }
}
