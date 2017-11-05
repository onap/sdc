package org.openecomp.sdc.translator.services.heattotosca;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
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
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Utility class for consolidation data collection helper methods.
 */
public class ConsolidationDataUtil {

  protected static Logger logger = (Logger) LoggerFactory.getLogger(ConsolidationDataUtil.class);

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
  public static PortTemplateConsolidationData getPortTemplateConsolidationData(
      TranslationContext context,
      ServiceTemplate serviceTemplate,
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
      portTemplateConsolidationData = new PortTemplateConsolidationData();
      portTemplateConsolidationData.setNodeTemplateId(portNodeTemplateId);
      filePortConsolidationData.setPortTemplateConsolidationData(portNodeTemplateId,
          portTemplateConsolidationData);
    }

    return portTemplateConsolidationData;
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
   * @param portNodeTemplateId the port node template id
   */
  public static void updatePortInConsolidationData(TranslateTo translateTo,
                                                   String computeNodeType,
                                                   String portNodeTemplateId) {
    TranslationContext translationContext = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    ComputeTemplateConsolidationData computeTemplateConsolidationData =
        getComputeTemplateConsolidationData(translationContext, serviceTemplate, computeNodeType,
            translateTo.getTranslatedId());
    computeTemplateConsolidationData.addPort(getPortType(portNodeTemplateId), portNodeTemplateId);
    // create port in consolidation data
    getPortTemplateConsolidationData(translationContext, serviceTemplate, portNodeTemplateId);
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
    HeatOrchestrationTemplate heatOrchestrationTemplate = translateTo
        .getHeatOrchestrationTemplate();
    TranslationContext translationContext = translateTo.getContext();

    consolidationEntityType.setEntityType(heatOrchestrationTemplate, sourceResource,
        targetResource, translateTo.getContext());
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
    return (consolidationEntityType == ConsolidationEntityType.COMPUTE
        || consolidationEntityType == ConsolidationEntityType.PORT
        || consolidationEntityType == ConsolidationEntityType.NESTED
        || consolidationEntityType == ConsolidationEntityType.VFC_NESTED);
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
      entityConsolidationData = getPortTemplateConsolidationData(translationContext,
          serviceTemplate, translateTo.getTranslatedId());
    } else if (consolidationEntityType == ConsolidationEntityType.VFC_NESTED
        || consolidationEntityType == ConsolidationEntityType.NESTED) {
      entityConsolidationData =
          getNestedTemplateConsolidationData(translationContext, serviceTemplate,
              translateTo.getHeatFileName(),
              translateTo.getTranslatedId());
    }

    if (Objects.isNull(entityConsolidationData)) {
      return;
    }

    if (Objects.isNull(entityConsolidationData.getNodesConnectedOut())) {
      entityConsolidationData.setNodesConnectedOut(new HashMap<>());
    }

    entityConsolidationData.getNodesConnectedOut()
        .computeIfAbsent(nodeTemplateId, k -> new ArrayList<>())
        .add(requirementAssignmentData);
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
      String nodeType = null;
      if (Objects.isNull(computeNodeTemplate)) {
        Resource targetResource =
            translateTo.getHeatOrchestrationTemplate().getResources().get(targetResourceId);
        NameExtractor nodeTypeNameExtractor =
            translateTo.getContext().getNameExtractorImpl(targetResource.getType());
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
      entityConsolidationData = getPortTemplateConsolidationData(translationContext,
          serviceTemplate, dependentNodeTemplateId);
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
      if (supportedComputeResources.get(resourceType).isEnable()) {
        return true;
      }
      return false;
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
      if (supportedComputeResources.get(resourceType).isEnable()) {
        return true;
      }
      return false;
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
      if (supportedPortResources.get(resourceType).isEnable()) {
        return true;
      }
      return false;
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
      if (supportedPortResources.get(resourceType).isEnable()) {
        return true;
      }
      return false;
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
    return (resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE
        .getHeatResource()));
  }

  /**
   * Checks if the current HEAT resource if of type volume.
   *
   * @param resource the resource
   * @return true if the resource is of volume type and false otherwise
   */
  public static boolean isVolumeResource(Resource resource) {
    String resourceType = resource.getType();
    return (resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE
        .getHeatResource()));
  }

  /**
   * Gets port type.
   *
   * @param portNodeTemplateId the port node template id
   * @return the port type
   */
  public static String getPortType(String portNodeTemplateId) {
    String[] portSplitArr = portNodeTemplateId.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    String finalValue = "";
    if (NumberUtils.isNumber(portSplitArr[portSplitArr.length - 1])) {
      for (String id : portSplitArr) {
        finalValue = finalValue + id;
      }
      while (finalValue.length() > 0) {
        if (Character.isLetter(finalValue.charAt(finalValue.length() - 1))) {
          break;
        }
        finalValue = finalValue.substring(0, finalValue.length() - 1);
      }
    } else {
      for (String id : portSplitArr) {
        if (!NumberUtils.isNumber(id)) {
          finalValue = finalValue + id;
        }
      }
    }
    return finalValue;
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
      NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
          sharedTranslatedResourceId);
      EntityConsolidationData entityConsolidationData = getPortTemplateConsolidationData(context,
          serviceTemplate, sharedTranslatedResourceId);
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
