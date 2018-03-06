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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.EntityConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.RequirementAssignmentData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * Utility class for consolidation data collection helper methods.
 */
public class ConsolidationDataUtil {

  private static final String UNDERSCORE = "_";
  private static final String DIGIT_REGEX = "\\d+";

  private ConsolidationDataUtil() {
  }

  /**
   * Gets compute template consolidation data.
   *
   * @param context               the translation context
   * @param serviceTemplate       the service template
   * @param computeNodeType       the compute node type
   * @param computeNodeTemplateId the compute node template id
   * @return the compute template consolidation data
   */
  public static ComputeTemplateConsolidationData getComputeTemplateConsolidationData(
      TranslationContext context,
      ServiceTemplate serviceTemplate,
      String computeNodeType,
      String computeNodeTemplateId) {

    ConsolidationData consolidationData = context.getConsolidationData();
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);

    ComputeConsolidationData computeConsolidationData = consolidationData
        .getComputeConsolidationData();

    FileComputeConsolidationData fileComputeConsolidationData = computeConsolidationData
        .getFileComputeConsolidationData(serviceTemplateFileName);

    if (fileComputeConsolidationData == null) {
      fileComputeConsolidationData = new FileComputeConsolidationData();
      computeConsolidationData.setFileComputeConsolidationData(serviceTemplateFileName,
          fileComputeConsolidationData);
    }

    TypeComputeConsolidationData typeComputeConsolidationData = fileComputeConsolidationData
        .getTypeComputeConsolidationData(computeNodeType);
    if (typeComputeConsolidationData == null) {
      typeComputeConsolidationData = new TypeComputeConsolidationData();
      fileComputeConsolidationData.setTypeComputeConsolidationData(computeNodeType,
          typeComputeConsolidationData);
    }

    ComputeTemplateConsolidationData computeTemplateConsolidationData =
        typeComputeConsolidationData.getComputeTemplateConsolidationData(computeNodeTemplateId);
    if (computeTemplateConsolidationData == null) {
      computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
      computeTemplateConsolidationData.setNodeTemplateId(computeNodeTemplateId);
      typeComputeConsolidationData.setComputeTemplateConsolidationData(computeNodeTemplateId,
          computeTemplateConsolidationData);
    }

    return computeTemplateConsolidationData;
  }


  /**
   * Gets port template consolidation data.
   *
   * @param context            the context
   * @param serviceTemplate    the service template
   * @param portNodeTemplateId the port node template id
   * @return the port template consolidation data
   */
  public static PortTemplateConsolidationData getPortTemplateConsolidationData(TranslationContext context,
                                                                               ServiceTemplate serviceTemplate,
                                                                               String portResourceId,
                                                                               String portResourceType,
                                                                               String portNodeTemplateId) {

    ConsolidationData consolidationData = context.getConsolidationData();
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);

    PortConsolidationData portConsolidationData = consolidationData.getPortConsolidationData();

    FilePortConsolidationData filePortConsolidationData = portConsolidationData
        .getFilePortConsolidationData(serviceTemplateFileName);

    if (filePortConsolidationData == null) {
      filePortConsolidationData = new FilePortConsolidationData();
      portConsolidationData.setFilePortConsolidationData(serviceTemplateFileName,
          filePortConsolidationData);
    }

    PortTemplateConsolidationData portTemplateConsolidationData =
        filePortConsolidationData.getPortTemplateConsolidationData(portNodeTemplateId);
    if (portTemplateConsolidationData == null) {
      portTemplateConsolidationData = getInitPortTemplateConsolidationData(portNodeTemplateId,
          portResourceId, portResourceType);
      filePortConsolidationData.setPortTemplateConsolidationData(portNodeTemplateId, portTemplateConsolidationData);
    }

    return portTemplateConsolidationData;
  }

  private static PortTemplateConsolidationData getInitPortTemplateConsolidationData(String portNodeTemplateId,
                                                                             String portResourceId,
                                                                             String portResourceType) {
    PortTemplateConsolidationData portTemplateConsolidationData = new PortTemplateConsolidationData();
    portTemplateConsolidationData.setNodeTemplateId(portNodeTemplateId);
    Optional<String> portNetworkRole = HeatToToscaUtil.evaluateNetworkRoleFromResourceId(portResourceId,
        portResourceType);
    portNetworkRole.ifPresent(portTemplateConsolidationData::setNetworkRole);
    return portTemplateConsolidationData;
  }

  public static Optional<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationData(
      TranslateTo subInterfaceTo,
      String subInterfaceNodeTemplateId) {
    Optional<String> parentPortNodeTemplateId =
        HeatToToscaUtil.getSubInterfaceParentPortNodeTemplateId(subInterfaceTo);
    if (parentPortNodeTemplateId.isPresent()) {
      return Optional.ofNullable(getSubInterfaceTemplateConsolidationData(subInterfaceTo,
         parentPortNodeTemplateId.get(), subInterfaceNodeTemplateId));
    }
    return Optional.empty();
  }

  private static SubInterfaceTemplateConsolidationData getSubInterfaceTemplateConsolidationData(
      TranslateTo subInterfaceTo,
      String parentPortNodeTemplateId,
      String subInterfaceNodeTemplateId) {

    ConsolidationData consolidationData = subInterfaceTo.getContext().getConsolidationData();
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(subInterfaceTo.getServiceTemplate());

    PortConsolidationData portConsolidationData = consolidationData.getPortConsolidationData();

    FilePortConsolidationData filePortConsolidationData = portConsolidationData
        .getFilePortConsolidationData(serviceTemplateFileName);

    if (filePortConsolidationData == null) {
      filePortConsolidationData = new FilePortConsolidationData();
      portConsolidationData.setFilePortConsolidationData(serviceTemplateFileName,
          filePortConsolidationData);
    }

    PortTemplateConsolidationData portTemplateConsolidationData =
        filePortConsolidationData.getPortTemplateConsolidationData(parentPortNodeTemplateId);
    if (portTemplateConsolidationData == null) {
      portTemplateConsolidationData = new PortTemplateConsolidationData();
      portTemplateConsolidationData.setNodeTemplateId(parentPortNodeTemplateId);
      filePortConsolidationData.setPortTemplateConsolidationData(parentPortNodeTemplateId,
          portTemplateConsolidationData);
    }

    return portTemplateConsolidationData.getSubInterfaceResourceTemplateConsolidationData(subInterfaceTo.getResource(),
        subInterfaceNodeTemplateId, parentPortNodeTemplateId);
  }

  /**
   * Gets nested template consolidation data.
   *
   * @param context              the context
   * @param serviceTemplate      the service template
   * @param nestedNodeTemplateId the nested node template id  @return the nested template
   *                             consolidation data
   */
  public static NestedTemplateConsolidationData getNestedTemplateConsolidationData(
      TranslationContext context,
      ServiceTemplate serviceTemplate,
      String nestedHeatFileName, String nestedNodeTemplateId) {

    if (isNestedResourceIdOccuresInDifferentNestedFiles(context, nestedHeatFileName,
        nestedNodeTemplateId)) {
      throw new CoreException(
          new DuplicateResourceIdsInDifferentFilesErrorBuilder(nestedNodeTemplateId).build());
    }

    if (isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(
        nestedNodeTemplateId, nestedHeatFileName, context)) {
      return null;
    }

    ConsolidationData consolidationData = context.getConsolidationData();
    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);

    NestedConsolidationData nestedConsolidationData = consolidationData
        .getNestedConsolidationData();

    FileNestedConsolidationData fileNestedConsolidationData = nestedConsolidationData
        .getFileNestedConsolidationData(serviceTemplateFileName);

    if (fileNestedConsolidationData == null) {
      fileNestedConsolidationData = new FileNestedConsolidationData();
      nestedConsolidationData.setFileNestedConsolidationData(serviceTemplateFileName,
          fileNestedConsolidationData);
    }

    NestedTemplateConsolidationData nestedTemplateConsolidationData =
        fileNestedConsolidationData.getNestedTemplateConsolidationData(nestedNodeTemplateId);
    if (nestedTemplateConsolidationData == null) {
      nestedTemplateConsolidationData = new NestedTemplateConsolidationData();
      nestedTemplateConsolidationData.setNodeTemplateId(nestedNodeTemplateId);
      fileNestedConsolidationData.setNestedTemplateConsolidationData(nestedNodeTemplateId,
          nestedTemplateConsolidationData);
    }

    return nestedTemplateConsolidationData;
  }

  public static boolean isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(String
                                                                                      nestedNodeTemplateId,
                                                                                  String nestedHeatFileName,
                                                                                  TranslationContext context) {

    return context.isServiceTemplateWithoutNodeTemplatesSection(
        FileUtils.getFileWithoutExtention(nestedHeatFileName))
        || context.isNodeTemplateIdPointsToStWithoutNodeTemplates(nestedNodeTemplateId);
  }

  private static boolean isNestedResourceIdOccuresInDifferentNestedFiles(TranslationContext context,
                                                                         String nestedHeatFileName,
                                                                         String nestedNodeTemplateId) {
    return Objects.nonNull(nestedHeatFileName)
        && context.getAllTranslatedResourceIdsFromDiffNestedFiles(nestedHeatFileName)
        .contains(nestedNodeTemplateId);
  }

  /**
   * Update group id information in consolidation data.
   *
   * @param entityConsolidationData Entity consolidation data (Port/Compute)
   * @param translatedGroupId       Group id of which compute node is a part
   */
  public static void updateGroupIdInConsolidationData(EntityConsolidationData
                                                          entityConsolidationData,
                                                      String translatedGroupId) {
    if (entityConsolidationData.getGroupIds() == null) {
      entityConsolidationData.setGroupIds(new ArrayList<>());
    }
    entityConsolidationData.getGroupIds().add(translatedGroupId);
  }

  /**
   * Update volume information in consolidation data.
   *
   * @param translateTo           {@link TranslateTo} object
   * @param computeType           Local type of the compute node
   * @param computeNodeTemplateId Node template id of the compute node
   * @param requirementAssignment RequirementAssignment object
   */
  public static void updateComputeConsolidationDataVolumes(TranslateTo translateTo,
                                                           String computeType,
                                                           String computeNodeTemplateId,
                                                           String requirementId,
                                                           RequirementAssignment
                                                               requirementAssignment) {
    TranslationContext translationContext = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    ComputeTemplateConsolidationData computeTemplateConsolidationData =
        getComputeTemplateConsolidationData(translationContext, serviceTemplate, computeType,
            computeNodeTemplateId);
    computeTemplateConsolidationData.addVolume(requirementId, requirementAssignment);
  }


  /**
   * Update port in consolidation data.
   *
   * @param translateTo        the translate to
   * @param computeNodeType    the compute node type
   * @param portResourceId     the port resource id
   * @param portNodeTemplateId the port node template id
   */
  public static void updatePortInConsolidationData(TranslateTo translateTo,
                                                   String computeNodeType,
                                                   String portResourceId,
                                                   String portResourceType,
                                                   String portNodeTemplateId) {
    TranslationContext translationContext = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    ComputeTemplateConsolidationData computeTemplateConsolidationData =
        getComputeTemplateConsolidationData(translationContext, serviceTemplate, computeNodeType,
            translateTo.getTranslatedId());
    computeTemplateConsolidationData.addPort(getPortType(portNodeTemplateId), portNodeTemplateId);
    // create port in consolidation data
    getPortTemplateConsolidationData(translationContext, serviceTemplate, portResourceId,
        portResourceType, portNodeTemplateId);
  }

  /**
   * Update nodes connected in and out for Depends on and connectivity in consolidation data.
   *
   * @param translateTo           the translate to
   * @param targetResourceId      the target resource id
   * @param nodeTemplateId        the source node template id
   * @param requirementAssignment the requirement assignment
   */
  public static void updateNodesConnectedData(TranslateTo translateTo, String targetResourceId,
                                              Resource targetResource, Resource sourceResource,
                                              String nodeTemplateId, String requirementId,
                                              RequirementAssignment requirementAssignment) {
    ConsolidationEntityType consolidationEntityType = ConsolidationEntityType.OTHER;
    consolidationEntityType.setEntityType(sourceResource, targetResource, translateTo.getContext());
    // Add resource dependency information in nodesConnectedIn if the target node
    // is a consolidation entity
    if (isConsolidationEntity(consolidationEntityType.getTargetEntityType())) {
      ConsolidationDataUtil.updateNodesConnectedIn(translateTo,
          nodeTemplateId, consolidationEntityType.getTargetEntityType(), targetResourceId,
          requirementId, requirementAssignment);
    }

    //Add resource dependency information in nodesConnectedOut if the source node
    //is a consolidation entity
    if (isConsolidationEntity(consolidationEntityType.getSourceEntityType())) {
      ConsolidationDataUtil.updateNodesConnectedOut(translateTo,
          requirementAssignment.getNode(), consolidationEntityType.getSourceEntityType(),
          requirementId, requirementAssignment);

    }
  }


  private static boolean isConsolidationEntity(ConsolidationEntityType consolidationEntityType) {
    return ConsolidationEntityType.getSupportedConsolidationEntities().contains(consolidationEntityType);
  }

  /**
   * Update nodes connected from this node in consolidation data.
   *
   * @param translateTo             the translate to
   * @param nodeTemplateId          the node template id of the target node
   * @param consolidationEntityType the entity type (compute or port)
   * @param requirementId           the requirement id
   * @param requirementAssignment   the requirement assignment
   */
  public static void updateNodesConnectedOut(TranslateTo translateTo,
                                             String nodeTemplateId,
                                             ConsolidationEntityType consolidationEntityType,
                                             String requirementId,
                                             RequirementAssignment requirementAssignment) {
    EntityConsolidationData entityConsolidationData = null;
    TranslationContext translationContext = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();

    translationContext.updateRequirementAssignmentIdIndex(ToscaUtil.getServiceTemplateFileName
        (translateTo.getServiceTemplate()), translateTo.getResourceId(), requirementId);

    RequirementAssignmentData requirementAssignmentData = new RequirementAssignmentData(
        requirementId, requirementAssignment);

    if (consolidationEntityType == ConsolidationEntityType.COMPUTE) {
      String nodeType = DataModelUtil.getNodeTemplate(serviceTemplate, translateTo
          .getTranslatedId()).getType();
      entityConsolidationData = getComputeTemplateConsolidationData(translationContext,
          serviceTemplate, nodeType, translateTo.getTranslatedId());
    } else if (consolidationEntityType == ConsolidationEntityType.PORT) {
      entityConsolidationData = getPortTemplateConsolidationData(translationContext, serviceTemplate,
          translateTo.getResourceId(), translateTo.getResource().getType(), translateTo
          .getTranslatedId());
    } else if (consolidationEntityType == ConsolidationEntityType.SUB_INTERFACE
        && Objects.nonNull(serviceTemplate.getTopology_template().getNode_templates()
        .get(translateTo.getTranslatedId()))) {
      Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
          getSubInterfaceTemplateConsolidationData(translateTo, translateTo.getTranslatedId());
      if (subInterfaceTemplateConsolidationData.isPresent()) {
        entityConsolidationData = subInterfaceTemplateConsolidationData.get();
      }
    } else if (consolidationEntityType == ConsolidationEntityType.VFC_NESTED
        || consolidationEntityType == ConsolidationEntityType.NESTED) {
      entityConsolidationData =
          getNestedTemplateConsolidationData(translationContext, serviceTemplate,
              translateTo.getHeatFileName(),
              translateTo.getTranslatedId());
    }

    if (Objects.nonNull(entityConsolidationData)) {
      if (Objects.isNull(entityConsolidationData.getNodesConnectedOut())) {
        entityConsolidationData.setNodesConnectedOut(new HashMap<>());
      }

      entityConsolidationData.getNodesConnectedOut()
          .computeIfAbsent(nodeTemplateId, k -> new ArrayList<>())
          .add(requirementAssignmentData);
    }
  }

  /**
   * Update nodes connected from this node in consolidation data.
   *
   * @param translateTo             the translate to
   * @param sourceNodeTemplateId    the node template id of the source node
   * @param consolidationEntityType Entity type (compute or port)
   * @param requirementId           Requirement Id
   * @param requirementAssignment   the requirement assignment
   */
  public static void updateNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
                                            ConsolidationEntityType consolidationEntityType,
                                            String targetResourceId,
                                            String requirementId,
                                            RequirementAssignment requirementAssignment) {
    EntityConsolidationData entityConsolidationData = null;
    TranslationContext translationContext = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    RequirementAssignmentData requirementAssignmentData = new RequirementAssignmentData(
        requirementId, requirementAssignment);
    String dependentNodeTemplateId = requirementAssignment.getNode();
    if (consolidationEntityType == ConsolidationEntityType.COMPUTE) {
      NodeTemplate computeNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
          dependentNodeTemplateId);
      String nodeType;
      if (Objects.isNull(computeNodeTemplate)) {
        Resource targetResource =
            translateTo.getHeatOrchestrationTemplate().getResources().get(targetResourceId);
        NameExtractor nodeTypeNameExtractor =
            TranslationContext.getNameExtractorImpl(targetResource.getType());
        nodeType =
            nodeTypeNameExtractor.extractNodeTypeName(translateTo.getHeatOrchestrationTemplate()
                    .getResources().get(dependentNodeTemplateId),
                dependentNodeTemplateId, dependentNodeTemplateId);
      } else {
        nodeType = computeNodeTemplate.getType();
      }

      entityConsolidationData = getComputeTemplateConsolidationData(translationContext,
          serviceTemplate, nodeType, dependentNodeTemplateId);
    } else if (consolidationEntityType == ConsolidationEntityType.PORT) {
      entityConsolidationData = getPortTemplateConsolidationData(translationContext, serviceTemplate,
          translateTo.getResourceId(), translateTo.getResource().getType(), dependentNodeTemplateId);
    }  else if (consolidationEntityType == ConsolidationEntityType.SUB_INTERFACE) {
      Resource targetResource =
          translateTo.getHeatOrchestrationTemplate().getResources().get(targetResourceId);
      TranslateTo subInterfaceTo = new TranslateTo(translateTo.getHeatFileName(), serviceTemplate, translateTo
          .getHeatOrchestrationTemplate(), targetResource, targetResourceId, null, translationContext);
      Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
          getSubInterfaceTemplateConsolidationData(subInterfaceTo, targetResourceId);
      if (subInterfaceTemplateConsolidationData.isPresent()) {
        entityConsolidationData = subInterfaceTemplateConsolidationData.get();
      }
    } else if (consolidationEntityType == ConsolidationEntityType.NESTED
        || consolidationEntityType == ConsolidationEntityType.VFC_NESTED) {
      entityConsolidationData = getNestedTemplateConsolidationData(translationContext,
          serviceTemplate, translateTo.getHeatFileName(), dependentNodeTemplateId);
    }

    if (entityConsolidationData != null) {
      if (entityConsolidationData.getNodesConnectedIn() == null) {
        entityConsolidationData.setNodesConnectedIn(new HashMap<>());
      }

      entityConsolidationData.getNodesConnectedIn()
          .computeIfAbsent(sourceNodeTemplateId, k -> new ArrayList<>())
          .add(requirementAssignmentData);

    }
  }

  /**
   * Checks if the current HEAT resource if of type compute.
   *
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param resourceId                the resource id
   * @return true if the resource is of compute type and false otherwise
   */
  public static boolean isComputeResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                          String resourceId) {
    String resourceType = heatOrchestrationTemplate.getResources().get(resourceId).getType();
    Map<String, ImplementationConfiguration> supportedComputeResources = TranslationContext
        .getSupportedConsolidationComputeResources();
    if (supportedComputeResources.containsKey(resourceType)) {
      return supportedComputeResources.get(resourceType).isEnable();
    }
    return false;
  }

  /**
   * Checks if the current HEAT resource if of type compute.
   *
   * @param resource the resource
   * @return true if the resource is of compute type and false otherwise
   */
  public static boolean isComputeResource(Resource resource) {
    String resourceType = resource.getType();
    Map<String, ImplementationConfiguration> supportedComputeResources = TranslationContext
        .getSupportedConsolidationComputeResources();
    if (supportedComputeResources.containsKey(resourceType)) {
      return supportedComputeResources.get(resourceType).isEnable();
    }
    return false;
  }

  /**
   * Checks if the current HEAT resource if of type port.
   *
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param resourceId                the resource id
   * @return true if the resource is of port type and false otherwise
   */
  public static boolean isPortResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                       String resourceId) {
    String resourceType = heatOrchestrationTemplate.getResources().get(resourceId).getType();
    Map<String, ImplementationConfiguration> supportedPortResources = TranslationContext
        .getSupportedConsolidationPortResources();
    if (supportedPortResources.containsKey(resourceType)) {
      return supportedPortResources.get(resourceType).isEnable();
    }
    return false;
  }

  /**
   * Checks if the current HEAT resource if of type port.
   *
   * @param resource the resource
   * @return true if the resource is of port type and false otherwise
   */
  public static boolean isPortResource(Resource resource) {
    String resourceType = resource.getType();
    Map<String, ImplementationConfiguration> supportedPortResources = TranslationContext
        .getSupportedConsolidationPortResources();
    if (supportedPortResources.containsKey(resourceType)) {
      return supportedPortResources.get(resourceType).isEnable();
    }
    return false;
  }

  /**
   * Checks if the current HEAT resource if of type volume.
   *
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param resourceId                the resource id
   * @return true if the resource is of volume type and false otherwise
   */
  public static boolean isVolumeResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                         String resourceId) {
    String resourceType = heatOrchestrationTemplate.getResources().get(resourceId).getType();
    return resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE
        .getHeatResource());
  }

  /**
   * Checks if the current HEAT resource if of type volume.
   *
   * @param resource the resource
   * @return true if the resource is of volume type and false otherwise
   */
  public static boolean isVolumeResource(Resource resource) {
    String resourceType = resource.getType();
    return resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE
        .getHeatResource());
  }

  /**
   * Gets port type.
   *
   * @param portNodeTemplateId the port node template id
   * @return the port type
   */
  public static String getPortType(String portNodeTemplateId) {

    if (StringUtils.isBlank(portNodeTemplateId)) {
      return portNodeTemplateId;
    }

    String formattedName = portNodeTemplateId.replaceAll(UNDERSCORE + DIGIT_REGEX + "$", "");

    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (String token : formattedName.split(UNDERSCORE)) {

      if (StringUtils.isNotBlank(token)) {
        count++;
      }

      if (count != 2 || !token.matches(DIGIT_REGEX)) {
        sb.append(token).append(UNDERSCORE);
      }
    }

    return portNodeTemplateId.endsWith(UNDERSCORE) ? sb.toString() : sb.substring(0, sb.length() - 1);
  }

  /**
   * Update node template id for the nested node templates in the consolidation data.
   *
   * @param translateTo the translate to
   */
  public static void updateNestedNodeTemplateId(TranslateTo translateTo) {
    TranslationContext context = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    getNestedTemplateConsolidationData(
        context, serviceTemplate, translateTo.getHeatFileName(), translateTo.getTranslatedId());
  }

  public static void removeSharedResource(ServiceTemplate serviceTemplate,
                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
                                          TranslationContext context,
                                          String paramName,
                                          String contrailSharedResourceId,
                                          String sharedTranslatedResourceId) {
    if (ConsolidationDataUtil.isComputeResource(heatOrchestrationTemplate,
        contrailSharedResourceId)) {
      NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
          sharedTranslatedResourceId);
      EntityConsolidationData entityConsolidationData = getComputeTemplateConsolidationData(
          context, serviceTemplate, nodeTemplate.getType(), sharedTranslatedResourceId);
      List<GetAttrFuncData> getAttrFuncDataList = entityConsolidationData
          .getOutputParametersGetAttrIn();
      removeParamNameFromAttrFuncList(paramName, getAttrFuncDataList);
    }
    if (ConsolidationDataUtil.isPortResource(heatOrchestrationTemplate,
        contrailSharedResourceId)) {
      Resource resource = heatOrchestrationTemplate.getResources().get(contrailSharedResourceId);
      EntityConsolidationData entityConsolidationData = getPortTemplateConsolidationData(context, serviceTemplate,
          contrailSharedResourceId, resource.getType(), sharedTranslatedResourceId);
      List<GetAttrFuncData> getAttrFuncDataList = entityConsolidationData
          .getOutputParametersGetAttrIn();
      removeParamNameFromAttrFuncList(paramName, getAttrFuncDataList);
    }
  }

  private static void removeParamNameFromAttrFuncList(String paramName,
                                                      List<GetAttrFuncData> getAttrFuncDataList) {
    Iterator<GetAttrFuncData> itr = getAttrFuncDataList.iterator();
    while (itr.hasNext()) {
      GetAttrFuncData getAttrFuncData = itr.next();
      if (paramName.equals(getAttrFuncData.getFieldName())) {
        itr.remove();
      }
    }
  }

  public static void updateNodeGetAttributeIn(EntityConsolidationData entityConsolidationData,
                                              String nodeTemplateId, String propertyName,
                                              String attributeName) {
    GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
    getAttrFuncData.setFieldName(propertyName);
    getAttrFuncData.setAttributeName(attributeName);
    entityConsolidationData.addNodesGetAttrIn(nodeTemplateId, getAttrFuncData);

  }

  public static void updateNodeGetAttributeOut(EntityConsolidationData entityConsolidationData,
                                               String nodeTemplateId, String propertyName,
                                               String attributeName) {
    GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
    getAttrFuncData.setFieldName(propertyName);
    getAttrFuncData.setAttributeName(attributeName);
    entityConsolidationData.addNodesGetAttrOut(nodeTemplateId, getAttrFuncData);

  }

  public static void updateOutputGetAttributeInConsolidationData(EntityConsolidationData
                                                                     entityConsolidationData,
                                                                 String outputParameterName,
                                                                 String attributeName) {


    GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
    getAttrFuncData.setFieldName(outputParameterName);
    getAttrFuncData.setAttributeName(attributeName);
    entityConsolidationData.addOutputParamGetAttrIn(getAttrFuncData);

  }

  public static boolean isComputeReferenceToPortId(ComputeTemplateConsolidationData compute,
                                                   String portId) {
    if (MapUtils.isEmpty(compute.getPorts())) {
      return false;
    }
    for (List<String> portIdsPerType : compute.getPorts().values()) {
      if (portIdsPerType.contains(portId)) {
        return true;
      }
    }
    return false;
  }

}
