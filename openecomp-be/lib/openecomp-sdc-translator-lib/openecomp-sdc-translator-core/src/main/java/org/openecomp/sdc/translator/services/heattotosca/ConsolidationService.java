package org.openecomp.sdc.translator.services.heattotosca;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionEntity;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionMode;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.EntityConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.RequirementAssignmentData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConsolidationService {

  private MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private UnifiedCompositionService unifiedCompositionService;


  public ConsolidationService(UnifiedCompositionService unifiedCompositionService) {
    this.unifiedCompositionService = unifiedCompositionService;
  }

  ConsolidationService() {

  }

  void serviceTemplateConsolidation(ServiceTemplate serviceTemplate,
                                    TranslationContext translationContext) {

    ConsolidationData consolidationData = translationContext.getConsolidationData();

    FileComputeConsolidationData fileComputeConsolidationData =
        consolidationData.getComputeConsolidationData()
            .getFileComputeConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));

    if (Objects.isNull(fileComputeConsolidationData)) {
      return;
    }
    for (TypeComputeConsolidationData typeComputeConsolidationData :
        fileComputeConsolidationData.getAllTypeComputeConsolidationData()) {
      boolean preConditionResult =
          consolidationPreCondition(
              serviceTemplate, consolidationData, typeComputeConsolidationData);

      List<UnifiedCompositionData> unifiedCompositionDataList =
          createUnifiedCompositionDataList(
              serviceTemplate, consolidationData, typeComputeConsolidationData);

      if (preConditionResult) {
        boolean consolidationRuleCheckResult =
            checkConsolidationRules(serviceTemplate, typeComputeConsolidationData,
                consolidationData, translationContext);

        unifiedCompositionService.createUnifiedComposition(
            serviceTemplate, null, unifiedCompositionDataList,
            consolidationRuleCheckResult ? UnifiedCompositionMode.ScalingInstances
                : UnifiedCompositionMode.CatalogInstance,
            translationContext);
      } else {
        unifiedCompositionService.createUnifiedComposition(
            serviceTemplate, null, unifiedCompositionDataList, UnifiedCompositionMode
                .SingleSubstitution,
            translationContext);
      }
    }

  }

  public static Map<String, String> getConsolidationEntityIdToType(ServiceTemplate serviceTemplate,
                                                                   ConsolidationData consolidationData) {
    Map<String, String> consolidationEntityIdToType = new HashMap<>();

    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    FileComputeConsolidationData fileComputeConsolidationData =
        consolidationData.getComputeConsolidationData()
            .getFileComputeConsolidationData(serviceTemplateFileName);
    FilePortConsolidationData filePortConsolidationData =
        consolidationData.getPortConsolidationData()
            .getFilePortConsolidationData(serviceTemplateFileName);
    if (Objects.nonNull(fileComputeConsolidationData)) {
      for (String computeType : fileComputeConsolidationData.getAllComputeTypes()) {
        TypeComputeConsolidationData typeComputeConsolidationData =
            fileComputeConsolidationData.getTypeComputeConsolidationData(computeType);
        Set<String> computeNodeTemplateIds =
            typeComputeConsolidationData.getAllComputeNodeTemplateIds();
        for (String computeNodeTemplateId : computeNodeTemplateIds) {
          consolidationEntityIdToType.put(computeNodeTemplateId, computeType);
        }
      }
    }

    if (Objects.nonNull(filePortConsolidationData)) {
      Set<String> portNodeTemplateIds = filePortConsolidationData.getAllPortNodeTemplateIds();
      for (String portNodeTemplateId : portNodeTemplateIds) {
        consolidationEntityIdToType
            .put(portNodeTemplateId, ConsolidationDataUtil.getPortType(portNodeTemplateId));
      }
    }

    return consolidationEntityIdToType;
  }


  private boolean checkConsolidationRules(ServiceTemplate serviceTemplate,
                                          TypeComputeConsolidationData typeComputeConsolidationData,
                                          ConsolidationData consolidationData,
                                          TranslationContext context) {
    return checkComputeConsolidation(serviceTemplate, typeComputeConsolidationData)
        && checkPortConsolidation(serviceTemplate, typeComputeConsolidationData,
        consolidationData, context)
        && !checkGetAttrBetweenEntityConsolidationOfTheSameType(serviceTemplate,
        typeComputeConsolidationData, consolidationData);
  }

  private boolean checkGetAttrBetweenConsolidationDataEntitiesNotFromSameType(
      ServiceTemplate serviceTemplate,
      TypeComputeConsolidationData typeComputeConsolidationData,
      ConsolidationData consolidationData) {
    List<ComputeTemplateConsolidationData> computeTemplateConsolidationDataList =
        new ArrayList(typeComputeConsolidationData.getAllComputeTemplateConsolidationData());

    Set<String> computeNodeTemplateIds =
        typeComputeConsolidationData.getAllComputeNodeTemplateIds();

    Map<String, Set<String>> portTypeToIds = UnifiedCompositionUtil
        .collectAllPortsFromEachTypesFromComputes(computeTemplateConsolidationDataList);

    return
        checkGetAttrOutFromEntityToPortIsLegal(computeTemplateConsolidationDataList, portTypeToIds)
            && checkGetAttrOutFromPortLegal(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
            computeNodeTemplateIds, portTypeToIds, consolidationData);

  }

  private boolean checkGetAttrInEntityConsolidationWithPortIsLegal(
      List entityConsolidationDatas,
      TypeComputeConsolidationData typeComputeConsolidationData) {
    Map<String, Set<String>> portTypeToIds =
        UnifiedCompositionUtil.collectAllPortsFromEachTypesFromComputes(
            typeComputeConsolidationData.getAllComputeTemplateConsolidationData());

    Set<String> startingPortTypesPointByGetAttr =
        getPortTypesPointedByGetAttrFromEntity(
            (EntityConsolidationData) entityConsolidationDatas.get(0), portTypeToIds);

    for (int i = 1; i < entityConsolidationDatas.size(); i++) {
      Set<String> currentPortTypesPointByGetAttr =
          getPortTypesPointedByGetAttrFromEntity(
              (EntityConsolidationData) entityConsolidationDatas.get(i), portTypeToIds);
      if (!startingPortTypesPointByGetAttr.equals(currentPortTypesPointByGetAttr)) {
        return false;
      }
    }

    return true;
  }

  private Set<String> getPortTypesPointedByGetAttrFromEntity(
      EntityConsolidationData entity,
      Map<String, Set<String>> portTypeToIds) {
    return getPortTypeToIdPointByGetAttrInOrOut(
        entity.getNodesGetAttrIn(), portTypeToIds, entity).keySet();
  }

  private boolean checkGetAttrInToPortIsLegal(
      ServiceTemplate serviceTemplate,
      TypeComputeConsolidationData typeComputeConsolidationData,
      ConsolidationData consolidationData) {

    Map<String, Set<String>> portTypeToIds = UnifiedCompositionUtil
        .collectAllPortsFromEachTypesFromComputes(
            typeComputeConsolidationData.getAllComputeTemplateConsolidationData());

    for (Set<String> portIdsFromSameType : portTypeToIds.values()) {
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
          collectAllPortsTemplateConsolidationData(
              portIdsFromSameType, ToscaUtil.getServiceTemplateFileName(serviceTemplate),
              consolidationData);

      if (!checkGetAttrInEntityConsolidationWithPortIsLegal(
          portTemplateConsolidationDataList, typeComputeConsolidationData)) {
        return false;
      }
    }

    return true;
  }


  private boolean checkGetAttrOutFromPortLegal(String serviceTemplateName,
                                               Set<String> computeNodeTemplateIds,
                                               Map<String, Set<String>> portTypeToIds,
                                               ConsolidationData consolidationData) {
    for (Set<String> portIdsFromSameType : portTypeToIds.values()) {
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
          collectAllPortsTemplateConsolidationData(portIdsFromSameType, serviceTemplateName,
              consolidationData);

      if (!(checkGetAttrOutFromEntityToPortIsLegal(portTemplateConsolidationDataList, portTypeToIds)
          && checkGetAttrOutFromPortToComputeIsLegal(portTemplateConsolidationDataList,
          computeNodeTemplateIds))) {
        return false;
      }
    }

    return true;
  }

  private boolean checkGetAttrOutFromEntityToPortIsLegal(List entityConsolidationDataList,
                                                         Map<String, Set<String>> portTypeToIds) {

    for (String portType : portTypeToIds.keySet()) {
      Set<GetAttrFuncData> startingGetAttrFunc =
          getEntityGetAttrFuncAsSet(portType,
              (EntityConsolidationData) entityConsolidationDataList.get(0));
      for (int i = 1; i < entityConsolidationDataList.size(); i++) {
        Object entity = entityConsolidationDataList.get(i);
        Set<GetAttrFuncData> currentGetAttrFuncData =
            getEntityGetAttrFuncAsSet(portType,
                (EntityConsolidationData) entity);
        if (!(startingGetAttrFunc.equals(currentGetAttrFuncData))) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean checkGetAttrOutFromPortToComputeIsLegal(
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList,
      Set<String> computeNodeTemplateIds) {
    PortTemplateConsolidationData startingPortTemplate =
        portTemplateConsolidationDataList.get(0);
    Map<String, Set<GetAttrFuncData>> startingComputeGetAttrOutFuncData =
        getComputeGetAttrOutFuncData(startingPortTemplate.getNodesGetAttrOut(),
            computeNodeTemplateIds);

    for (int i = 1; i < portTemplateConsolidationDataList.size(); i++) {
      PortTemplateConsolidationData currentPortTemplate =
          portTemplateConsolidationDataList.get(i);
      Map<String, Set<GetAttrFuncData>> currentComputeGetAttrOutFuncData =
          getComputeGetAttrOutFuncData(currentPortTemplate.getNodesGetAttrOut(),
              computeNodeTemplateIds);

      if (!isGetAttrRelationToComputeSimilarBetweenEntities(startingComputeGetAttrOutFuncData,
          currentComputeGetAttrOutFuncData)) {
        return false;
      }
    }

    return true;
  }

  private boolean isGetAttrRelationToComputeSimilarBetweenEntities(
      Map<String, Set<GetAttrFuncData>> firstMap,
      Map<String, Set<GetAttrFuncData>> secondMap) {
    if (MapUtils.isEmpty(firstMap) != MapUtils.isEmpty(secondMap)) {
      return false;
    }

    if (MapUtils.isEmpty(firstMap) && MapUtils.isEmpty(secondMap)) {
      return true;
    }

    return new ArrayList<>(firstMap.values()).equals(new ArrayList<>(secondMap.values()));
  }

  private Set<GetAttrFuncData> getEntityGetAttrFuncAsSet(
      String portType,
      EntityConsolidationData entityConsolidationData) {

    Set<GetAttrFuncData> getAttrFuncDataFromPortsWithSameType = new HashSet<>();
    Map<String, List<GetAttrFuncData>> nodesGetAttrOut =
        entityConsolidationData.getNodesGetAttrOut();

    if (MapUtils.isEmpty(nodesGetAttrOut)) {
      return getAttrFuncDataFromPortsWithSameType;
    }

    for (Map.Entry<String, List<GetAttrFuncData>> entry : nodesGetAttrOut.entrySet()) {
      if (portType.equals(ConsolidationDataUtil.getPortType(entry.getKey()))) {
        getAttrFuncDataFromPortsWithSameType.addAll(entry.getValue());
      }
    }

    return getAttrFuncDataFromPortsWithSameType;
  }

  private Map<String, Set<GetAttrFuncData>> getComputeGetAttrOutFuncData(
      Map<String, List<GetAttrFuncData>> nodesGetAttrOut,
      Set<String> computeNodeTemplateIds) {
    Map<String, Set<GetAttrFuncData>> computeGetAttrFuncData = new HashMap<>();

    if (MapUtils.isEmpty(nodesGetAttrOut)) {
      return computeGetAttrFuncData;
    }

    for (Map.Entry<String, List<GetAttrFuncData>> getAttrFuncEntry : nodesGetAttrOut.entrySet()) {
      if (computeNodeTemplateIds.contains(getAttrFuncEntry.getKey())) {
        computeGetAttrFuncData.put(getAttrFuncEntry.getKey(), new HashSet<>(getAttrFuncEntry
            .getValue()));
      }
    }

    return computeGetAttrFuncData;
  }

  private Map<String, List<String>> getPortTypeToIdPointByGetAttrInOrOut(
      Map<String, List<GetAttrFuncData>> getAttr,
      Map<String, Set<String>> portTypeToIds,
      EntityConsolidationData entityConsolidationData) {
    Map<String, List<String>> portIdToType = new HashMap<>();

    if (MapUtils.isEmpty(getAttr)) {
      return portIdToType;
    }

    for (String getAttrId : getAttr.keySet()) {
      if (isNodeTemplateIdIsInComputeConsolidationData(getAttrId, portTypeToIds)) {
        String portType = ConsolidationDataUtil.getPortType(getAttrId);
        portIdToType.putIfAbsent(portType, new ArrayList<>());
        portIdToType.get(portType).add(getAttrId);
      }
    }

    return portIdToType;

  }


  private boolean isNodeTemplateIdIsInComputeConsolidationData(
      String getAttrInId,
      Map<String, Set<String>> portTypeToIds) {
    return portTypeToIds.keySet().contains(ConsolidationDataUtil.getPortType(getAttrInId));
  }

  private boolean checkGetAttrBetweenEntityConsolidationOfTheSameType(
      ServiceTemplate serviceTemplate,
      TypeComputeConsolidationData typeComputeConsolidationData,
      ConsolidationData consolidationData) {
    return checkGetAttrRelationsBetweenComputesOfSameType(typeComputeConsolidationData)
        || checkGetAttrRelationsBetweenPortsOfTheSameType(serviceTemplate,
        typeComputeConsolidationData, consolidationData);

  }

  private boolean checkGetAttrRelationsBetweenComputesOfSameType(
      TypeComputeConsolidationData typeComputeConsolidationData) {

    Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDatas =
        typeComputeConsolidationData.getAllComputeTemplateConsolidationData();
    Set<String> computeNodeTemplateIds =
        typeComputeConsolidationData.getAllComputeNodeTemplateIds();

    return checkGetAttrRelationsForEntityConsolidationData(
        computeTemplateConsolidationDatas, computeNodeTemplateIds);
  }

  private boolean checkGetAttrRelationsBetweenPortsOfTheSameType(
      ServiceTemplate serviceTemplate,
      TypeComputeConsolidationData typeComputeConsolidationData,
      ConsolidationData consolidationData) {

    Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDatas =
        typeComputeConsolidationData.getAllComputeTemplateConsolidationData();
    Map<String, Set<String>> portTypeToPortIds = UnifiedCompositionUtil
        .collectAllPortsFromEachTypesFromComputes(computeTemplateConsolidationDatas);

    FilePortConsolidationData filePortConsolidationData =
        consolidationData.getPortConsolidationData().getFilePortConsolidationData(ToscaUtil
            .getServiceTemplateFileName(serviceTemplate));

    for (Set<String> portsOfTheSameTypeIds : portTypeToPortIds.values()) {
      List<PortTemplateConsolidationData> portTemplateConsolidationDataOfSameType =
          getAllPortTemplateConsolidationData(portsOfTheSameTypeIds, filePortConsolidationData);
      if (!checkGetAttrRelationsForEntityConsolidationData(portTemplateConsolidationDataOfSameType,
          portsOfTheSameTypeIds)) {
        return false;
      }
    }

    return true;
  }

  private List<PortTemplateConsolidationData> getAllPortTemplateConsolidationData(
      Set<String> portsIds,
      FilePortConsolidationData filePortConsolidationData) {
    List<PortTemplateConsolidationData> portTemplateConsolidationDataOfSameType = new ArrayList<>();

    for (String portId : portsIds) {
      PortTemplateConsolidationData portTemplateConsolidationData =
          filePortConsolidationData.getPortTemplateConsolidationData(portId);
      if (Objects.nonNull(portTemplateConsolidationData)) {
        portTemplateConsolidationDataOfSameType.add(portTemplateConsolidationData);
      }
    }

    return portTemplateConsolidationDataOfSameType;
  }

  private boolean checkGetAttrRelationsForEntityConsolidationData(
      Collection entities,
      Set<String> nodeTemplateIdsOfTheSameType) {

    List<EntityConsolidationData> entityConsolidationDataList =
        new ArrayList(entities);

    for (EntityConsolidationData entityConsolidationData : entityConsolidationDataList) {
      Set<String> getAttrInNodeIds =
          entityConsolidationData.getNodesGetAttrIn() == null ? new HashSet<>()
              : entityConsolidationData.getNodesGetAttrIn().keySet();
      for (String nodeId : getAttrInNodeIds) {
        if (nodeTemplateIdsOfTheSameType.contains(nodeId)) {
          return true;
        }
      }
    }

    return false;
  }


  private boolean checkComputeConsolidation(
      ServiceTemplate serviceTemplate,
      TypeComputeConsolidationData typeComputeConsolidationData) {
    List<String> computeNodeTemplateIds =
        new ArrayList(typeComputeConsolidationData.getAllComputeNodeTemplateIds());
    List<String> propertiesWithIdenticalVal = getComputePropertiesWithIdenticalVal();

    return arePropertiesSimilarBetweenComputeNodeTemplates(serviceTemplate, computeNodeTemplateIds,
        propertiesWithIdenticalVal)
        && checkComputeRelations(
        typeComputeConsolidationData.getAllComputeTemplateConsolidationData());
  }


  private boolean checkComputeRelations(
      Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDatas) {

    return checkEntityConsolidationDataRelations(computeTemplateConsolidationDatas)
        && checkComputesRelationsToVolume(computeTemplateConsolidationDatas);
  }

  private boolean checkEntityConsolidationDataRelations(Collection entities) {
    List<EntityConsolidationData> entityConsolidationDataList =
        new ArrayList(entities);
    EntityConsolidationData startingEntity = entityConsolidationDataList.get(0);

    for (int i = 1; i < entityConsolidationDataList.size(); i++) {
      EntityConsolidationData currentEntity = entityConsolidationDataList.get(i);
      if (!(checkNodesConnectedInRelations(startingEntity, currentEntity)
          && (checkNodesConnectedOutRelations(startingEntity, currentEntity))
          && (checkGroupIdsRelations(startingEntity, currentEntity)))) {
        return false;
      }
    }
    return true;
  }

  private boolean checkNodesConnectedInRelations(EntityConsolidationData firstEntity,
                                                 EntityConsolidationData secondEntity) {
    return compareNodeConnectivity(firstEntity.getNodesConnectedIn(),
        secondEntity.getNodesConnectedIn());
  }

  private boolean checkNodesConnectedOutRelations(EntityConsolidationData firstEntity,
                                                  EntityConsolidationData secondEntity) {
    return compareNodeConnectivity(firstEntity.getNodesConnectedOut(),
        secondEntity.getNodesConnectedOut());
  }

  private boolean compareNodeConnectivity(
      Map<String, List<RequirementAssignmentData>> firstEntityMap,
      Map<String, List<RequirementAssignmentData>> secondEntityMap) {
    if (MapUtils.isEmpty(firstEntityMap)
        && MapUtils.isEmpty(secondEntityMap)) {
      return true;
    }
    if (!MapUtils.isEmpty(firstEntityMap)
        && !MapUtils.isEmpty(secondEntityMap)) {
      return firstEntityMap.keySet().equals(secondEntityMap.keySet());
    }
    return false;
  }

  private boolean checkGroupIdsRelations(EntityConsolidationData startingEntity,
                                         EntityConsolidationData currentEntity) {
    if (CollectionUtils.isEmpty(startingEntity.getGroupIds())
        && CollectionUtils.isEmpty(currentEntity.getGroupIds())) {
      return true;
    }

    return startingEntity.getGroupIds().equals(currentEntity.getGroupIds());
  }

  private boolean checkComputesRelationsToVolume(
      Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDatas) {

    Set<String> volumeRelationsFromComputes = new HashSet<>();
    List<ComputeTemplateConsolidationData> computeTemplateConsolidationDataList =
        new ArrayList(computeTemplateConsolidationDatas);

    Map<String, List<RequirementAssignmentData>> startingVolumes =
        computeTemplateConsolidationDataList.get(0).getVolumes();

    for (int i = 1; i < computeTemplateConsolidationDataList.size(); i++) {
      Map<String, List<RequirementAssignmentData>> currentVolumes =
          computeTemplateConsolidationDataList.get(i).getVolumes();
      if (!compareNodeConnectivity(startingVolumes, currentVolumes)) {
        return false;
      }
    }
    return true;
  }


  private boolean checkPortConsolidation(ServiceTemplate serviceTemplate,
                                         TypeComputeConsolidationData typeComputeConsolidationData,
                                         ConsolidationData consolidationData,
                                         TranslationContext context) {
    return validateWantedPortProperties(serviceTemplate,
        typeComputeConsolidationData, context)
        && checkPortRelations(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
        typeComputeConsolidationData, consolidationData);
  }


  private boolean validateWantedPortProperties(ServiceTemplate serviceTemplate,
                                               TypeComputeConsolidationData typeComputeConsolidationData,
                                               TranslationContext context) {

    Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDataCollection =
        typeComputeConsolidationData.getAllComputeTemplateConsolidationData();
    Map<String, Set<String>> portTypeToIds = UnifiedCompositionUtil
        .collectAllPortsFromEachTypesFromComputes(computeTemplateConsolidationDataCollection);
    List<String> propertiesWithIdenticalVal = getPortPropertiesWithIdenticalVal(context);
    List<String> propertiesThatNeedToHaveSameUsage =
        getPortPropertiesThatNeedToHaveSameUsage(context);

    for (Set<String> portsIds : portTypeToIds.values()) {
      if (!arePortPropertiesValid(serviceTemplate, propertiesWithIdenticalVal,
          propertiesThatNeedToHaveSameUsage, portsIds)) {
        return false;
      }
    }

    return true;
  }

  private boolean arePortPropertiesValid(ServiceTemplate serviceTemplate,
                                         List<String> propertiesWithIdenticalVal,
                                         List<String> propertiesThatNeedToHaveSameUsage,
                                         Set<String> portsIds) {
    return areWantedPortPropertiesValid(
        serviceTemplate, portsIds, propertiesWithIdenticalVal, PropertyCheckCondition.EQUAL)
        && areWantedPortPropertiesValid(
        serviceTemplate, portsIds, propertiesThatNeedToHaveSameUsage,
        PropertyCheckCondition.EXIST);
  }

  private boolean checkPortRelations(String serviceTemplateName,
                                     TypeComputeConsolidationData typeComputeConsolidationData,
                                     ConsolidationData consolidationData) {
    Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDataCollection =
        typeComputeConsolidationData.getAllComputeTemplateConsolidationData();
    Map<String, Set<String>> portTypeToIds = UnifiedCompositionUtil
        .collectAllPortsFromEachTypesFromComputes(computeTemplateConsolidationDataCollection);

    for (Set<String> portIds : portTypeToIds.values()) {
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList =
          collectAllPortsTemplateConsolidationData(
              portIds, serviceTemplateName, consolidationData);

      if (!checkEntityConsolidationDataRelations(portTemplateConsolidationDataList)) {
        return false;
      }
    }

    return true;
  }

  private List<PortTemplateConsolidationData>
  collectAllPortsTemplateConsolidationData(Set<String> portIds,
                                           String serviceTemplateName,
                                           ConsolidationData consolidationData) {

    FilePortConsolidationData filePortConsolidationData =
        consolidationData.getPortConsolidationData()
            .getFilePortConsolidationData(serviceTemplateName);
    List<PortTemplateConsolidationData> portTemplateConsolidationDataList = new ArrayList<>();

    for (String portId : portIds) {
      PortTemplateConsolidationData portTemplateConsolidationData = filePortConsolidationData
          .getPortTemplateConsolidationData(portId);
      if (Objects.nonNull(portTemplateConsolidationData)) {
        portTemplateConsolidationDataList.add(portTemplateConsolidationData);
      }
    }

    return portTemplateConsolidationDataList;
  }

  private boolean areWantedPortPropertiesValid(ServiceTemplate serviceTemplate,
                                               Set<String> portNodeTemplateIds,
                                               List<String> propertiesToCheck,
                                               PropertyCheckCondition condition) {
    Map<String, NodeTemplate> nodeTemplates =
        serviceTemplate.getTopology_template().getNode_templates();

    switch (condition) {
      case EQUAL:
        for (String property : propertiesToCheck) {
          if (!isPropertyValueSimilarBetweenNodeTemplates(property,
              new ArrayList<>(portNodeTemplateIds), nodeTemplates)) {
            return false;
          }
        }
        break;

      case EXIST:
        for (String property : propertiesToCheck) {
          if (!isPropertyUsageSimilarBetweenAllNodeTemplates(property,
              new ArrayList<>(portNodeTemplateIds), nodeTemplates)) {
            return false;
          }
        }
        break;
    }
    return true;
  }


  private boolean arePropertiesSimilarBetweenComputeNodeTemplates(
      ServiceTemplate serviceTemplate,
      List<String> computeNodeTemplateIds,
      List<String> propertiesThatNeedToBeSimilar) {

    Map<String, NodeTemplate> idToNodeTemplate =
        serviceTemplate.getTopology_template().getNode_templates();

    for (String property : propertiesThatNeedToBeSimilar) {
      if (!isPropertyValueSimilarBetweenNodeTemplates(property, computeNodeTemplateIds,
          idToNodeTemplate)) {
        return false;
      }
    }
    return true;
  }

  private boolean isPropertyUsageSimilarBetweenAllNodeTemplates(String propertyToCheck,
                                                                List<String> entityNodeTemplateIds,
                                                                Map<String, NodeTemplate> idToNodeTemplate) {
    NodeTemplate startingNodeTemplate = idToNodeTemplate.get(entityNodeTemplateIds.get(0));
    if (Objects.isNull(startingNodeTemplate)) {
      throw new CoreException(
          new DuplicateResourceIdsInDifferentFilesErrorBuilder(entityNodeTemplateIds.get(0))
              .build());
    }

    boolean propertyExistCondition =
        isPropertyExistInNodeTemplate(propertyToCheck, startingNodeTemplate);

    for (int i = 1; i < entityNodeTemplateIds.size(); i++) {
      NodeTemplate currentNodeTemplate = idToNodeTemplate.get(entityNodeTemplateIds.get(i));
      if (Objects.isNull(currentNodeTemplate)) {
        throw new CoreException(
            new DuplicateResourceIdsInDifferentFilesErrorBuilder(entityNodeTemplateIds.get(i))
                .build());
      }
      if (propertyExistCondition !=
          isPropertyExistInNodeTemplate(propertyToCheck, currentNodeTemplate)) {
        return false;
      }
    }

    return true;

  }

  private boolean isPropertyValueSimilarBetweenNodeTemplates(String propertyToCheck,
                                                             List<String> entityNodeTemplateIds,
                                                             Map<String, NodeTemplate> idToNodeTemplate) {

    Set<Object> propertiesValues = new HashSet<>();
    NodeTemplate startingNodeTemplate = idToNodeTemplate.get(entityNodeTemplateIds.get(0));
    if (Objects.isNull(startingNodeTemplate)) {
      throw new CoreException(
          new DuplicateResourceIdsInDifferentFilesErrorBuilder(entityNodeTemplateIds.get(0))
              .build());
    }

    addPropertyValue(propertyToCheck, startingNodeTemplate, propertiesValues);

    for (int i = 1; i < entityNodeTemplateIds.size(); i++) {
      NodeTemplate currentNodeTemplate = idToNodeTemplate.get(entityNodeTemplateIds.get(i));
      if (Objects.isNull(currentNodeTemplate)) {
        throw new CoreException(
            new DuplicateResourceIdsInDifferentFilesErrorBuilder(entityNodeTemplateIds.get(i))
                .build());
      }
      addPropertyValue(propertyToCheck, currentNodeTemplate, propertiesValues);
    }

    return propertiesValues.size() == 1;
  }

  private void addPropertyValue(String property,
                                NodeTemplate nodeTemplate,
                                Set<Object> propertiesValues) {
    propertiesValues.add(
        isPropertyExistInNodeTemplate(property, nodeTemplate) ? nodeTemplate.getProperties()
            .get(property) : "");
  }

  private boolean isPropertyExistInNodeTemplate(String propertyToCheck, NodeTemplate nodeTemplate) {
    return !(nodeTemplate.getProperties() == null ||
        nodeTemplate.getProperties().get(propertyToCheck) == null);
  }

  public void substitutionServiceTemplateConsolidation(String substituteNodeTemplateId,
                                                       ServiceTemplate serviceTemplate,
                                                       ServiceTemplate substitutionServiceTemplate,
                                                       TranslationContext translationContext) {

    ConsolidationData consolidationData = translationContext.getConsolidationData();

    FileComputeConsolidationData fileComputeConsolidationData =
        translationContext.getConsolidationData().getComputeConsolidationData()
            .getFileComputeConsolidationData(
                ToscaUtil.getServiceTemplateFileName(substitutionServiceTemplate));
    boolean substitutionConsolidationRuleResult =
        substitutionServiceTemplateConsolidationRule(substitutionServiceTemplate,
            fileComputeConsolidationData, translationContext);

    if (substitutionConsolidationRuleResult) {
      List<UnifiedCompositionData> unifiedCompositionDataList =
          createSubstitutionUnifiedCompositionDataList(substituteNodeTemplateId,
              serviceTemplate, consolidationData);
      unifiedCompositionService
          .createUnifiedComposition(serviceTemplate, substitutionServiceTemplate,
              unifiedCompositionDataList, UnifiedCompositionMode.NestedSingleCompute,
              translationContext);
    } else {
      //The node template does not represent unified VFC but complexVFC
      //Adding the id in the context for fixing connectivity from/to nested non-unified nodes
      translationContext.addUnifiedNestedNodeTemplateId(ToscaUtil
              .getServiceTemplateFileName(serviceTemplate),
          substituteNodeTemplateId, substituteNodeTemplateId);

      if (!translationContext.isUnifiedHandledServiceTemplate(substitutionServiceTemplate)) {
        serviceTemplateConsolidation(substitutionServiceTemplate, translationContext);
      }
    }
  }

  private boolean substitutionServiceTemplateConsolidationRule(
      ServiceTemplate nestedServiceTemplate,
      FileComputeConsolidationData fileComputeConsolidationData,
      TranslationContext context) {
    if (Objects.isNull(fileComputeConsolidationData)) {
      return false;
    }
    return isNumberOfComputeTypesLegal(fileComputeConsolidationData)
        && isNumberOfComputeConsolidationDataPerTypeLegal(
        fileComputeConsolidationData.getAllTypeComputeConsolidationData().iterator().next())
        && !isThereMoreThanOneNestedLevel(nestedServiceTemplate, context.getConsolidationData());
  }

  private boolean isNumberOfComputeTypesLegal(
      FileComputeConsolidationData fileComputeConsolidationData) {
    return fileComputeConsolidationData.getAllTypeComputeConsolidationData().size() == 1;
  }

  private boolean isNumberOfComputeConsolidationDataPerTypeLegal(
      TypeComputeConsolidationData typeComputeConsolidationData) {
    return typeComputeConsolidationData.getAllComputeTemplateConsolidationData().size() == 1;
  }

  private boolean isThereMoreThanOneNestedLevel(ServiceTemplate nestedServiceTemplate,
                                                ConsolidationData consolidationData) {
    String nestedServiceTemplateName = ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate);
    if (Objects.isNull(nestedServiceTemplateName)) {
      return false;
    }

    FileNestedConsolidationData fileNestedConsolidationData =
        consolidationData.getNestedConsolidationData() == null ? new FileNestedConsolidationData()
            : consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData(nestedServiceTemplateName);

    if (Objects.isNull(fileNestedConsolidationData)) {
      return false;
    }

    return !CollectionUtils.isEmpty(fileNestedConsolidationData.getAllNestedNodeTemplateIds());
  }


  private List<UnifiedCompositionData> createUnifiedCompositionDataList(
      ServiceTemplate serviceTemplate,
      ConsolidationData consolidationData,
      TypeComputeConsolidationData typeComputeConsolidationData) {

    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();

    for (ComputeTemplateConsolidationData computeTemplateConsolidationData : typeComputeConsolidationData
        .getAllComputeTemplateConsolidationData()) {

      UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
      unifiedCompositionData.setComputeTemplateConsolidationData(computeTemplateConsolidationData);

      Collection<List<String>> portCollection =
          computeTemplateConsolidationData.getPorts() == null ? Collections.emptyList()
              : computeTemplateConsolidationData.getPorts().values();

      FilePortConsolidationData filePortConsolidationData =
          consolidationData.getPortConsolidationData().getFilePortConsolidationData(ToscaUtil
              .getServiceTemplateFileName(serviceTemplate));

      for (List<String> portList : portCollection) {
        for (String portId : portList) {
          if (!Objects.isNull(filePortConsolidationData)) {
            unifiedCompositionData.addPortTemplateConsolidationData(
                (filePortConsolidationData.getPortTemplateConsolidationData(portId)));
          }
        }
      }
      unifiedCompositionDataList.add(unifiedCompositionData);
    }

    return unifiedCompositionDataList;
  }

  private List<UnifiedCompositionData> createSubstitutionUnifiedCompositionDataList(
      String substituteNodeTemplateId,
      ServiceTemplate serviceTemplate,
      ConsolidationData consolidationData) {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
    FileNestedConsolidationData fileNestedConsolidationData =
        consolidationData.getNestedConsolidationData()
            .getFileNestedConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));

    if (Objects.nonNull(fileNestedConsolidationData)) {
      NestedTemplateConsolidationData nestedTemplateConsolidationData =
          fileNestedConsolidationData.getNestedTemplateConsolidationData(substituteNodeTemplateId);
      UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
      unifiedCompositionData.setNestedTemplateConsolidationData(nestedTemplateConsolidationData);
      unifiedCompositionDataList.add(unifiedCompositionData);
      return unifiedCompositionDataList;
    }

    return unifiedCompositionDataList;
  }

  private boolean consolidationPreCondition(
      ServiceTemplate serviceTemplate,
      ConsolidationData consolidationData,
      TypeComputeConsolidationData typeComputeConsolidationData) {

    return (isThereMoreThanOneComputeTypeInstance(typeComputeConsolidationData)
        && isNumberOfPortsEqualsBetweenComputeNodes(typeComputeConsolidationData)
        && isNumberOfPortFromEachTypeLegal(typeComputeConsolidationData)
        && isPortTypesEqualsBetweenComputeNodes(typeComputeConsolidationData)
        && checkGetAttrBetweenConsolidationDataEntitiesNotFromSameType(serviceTemplate,
        typeComputeConsolidationData, consolidationData));

  }

  private boolean isThereMoreThanOneComputeTypeInstance(
      TypeComputeConsolidationData typeComputeConsolidationData) {
    return typeComputeConsolidationData.getAllComputeNodeTemplateIds().size() > 1;
  }

  private boolean isNumberOfPortsEqualsBetweenComputeNodes(
      TypeComputeConsolidationData typeComputeConsolidationData) {

    ArrayList<ComputeTemplateConsolidationData> computeTemplateConsolidationDataList =
        new ArrayList(typeComputeConsolidationData.getAllComputeTemplateConsolidationData());
    int startingNumberOfPorts =
        getNumberOfPortsPerCompute(computeTemplateConsolidationDataList.get(0));


    for (int i = 1; i < computeTemplateConsolidationDataList.size(); i++) {
      int currNumberOfPorts =
          getNumberOfPortsPerCompute(computeTemplateConsolidationDataList.get(i));
      if (currNumberOfPorts != startingNumberOfPorts) {
        return false;
      }
    }

    return true;
  }


  private boolean isNumberOfPortFromEachTypeLegal(
      TypeComputeConsolidationData typeComputeConsolidationData) {

    ArrayList<ComputeTemplateConsolidationData> computeTemplateConsolidationDataList =
        new ArrayList(typeComputeConsolidationData.getAllComputeTemplateConsolidationData());

    for (ComputeTemplateConsolidationData computeTemplate : computeTemplateConsolidationDataList) {
      Map<String, List<String>> currPortsMap = computeTemplate.getPorts();
      if (MapUtils.isEmpty(currPortsMap)) {
        return true;
      }
      for (List<String> portList : currPortsMap.values()) {
        if (portList.size() > 1) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean isPortTypesEqualsBetweenComputeNodes(
      TypeComputeConsolidationData typeComputeConsolidationData) {

    ArrayList<ComputeTemplateConsolidationData> computeTemplateConsolidationDataList =
        new ArrayList(typeComputeConsolidationData.getAllComputeTemplateConsolidationData());
    Set<String> staringPortIds = getPortsIds(computeTemplateConsolidationDataList.get(0));

    for (int i = 1; i < computeTemplateConsolidationDataList.size(); i++) {
      Set<String> currentPortIds = getPortsIds(computeTemplateConsolidationDataList.get(i));
      if (!currentPortIds.equals(staringPortIds)) {
        return false;
      }
    }

    return true;
  }

  private int getNumberOfPortsPerCompute(
      ComputeTemplateConsolidationData computeTemplateConsolidationData) {
    return getPortsIds(computeTemplateConsolidationData) == null ? 0 :
        getPortsIds(computeTemplateConsolidationData).size();
  }

  private Set<String> getPortsIds(
      ComputeTemplateConsolidationData computeTemplateConsolidationData) {
    return computeTemplateConsolidationData.getPorts() == null ? new HashSet<>()
        : computeTemplateConsolidationData
            .getPorts().keySet();
  }

  public List<String> getPropertiesWithIdenticalVal(UnifiedCompositionEntity entity,
                                                    TranslationContext context) {
    switch (entity) {
      case Compute:
        return getComputePropertiesWithIdenticalVal();

      case Other:
        return getComputePropertiesWithIdenticalVal();

      case Port:
        return getPortPropertiesWithIdenticalVal(context);

      default:
        return new ArrayList<>();
    }
  }

  private List<String> getComputePropertiesWithIdenticalVal() {
    List<String> propertyWithIdenticalValue = new ArrayList<>();
    propertyWithIdenticalValue.add(ToscaConstants.COMPUTE_IMAGE);
    propertyWithIdenticalValue.add(ToscaConstants.COMPUTE_FLAVOR);
    return propertyWithIdenticalValue;
  }

  private List<String> getPortPropertiesWithIdenticalVal(TranslationContext context) {
    List<String> propertiesThatNeedToHaveUsage = new ArrayList<>();
    propertiesThatNeedToHaveUsage.add(ToscaConstants.PORT_ALLOWED_ADDRESS_PAIRS);
    propertiesThatNeedToHaveUsage.add(ToscaConstants.MAC_ADDRESS);

    propertiesThatNeedToHaveUsage.addAll(context.getEnrichPortResourceProperties());

    return propertiesThatNeedToHaveUsage;
  }

  private List<String> getPortPropertiesThatNeedToHaveSameUsage(TranslationContext context) {
    List propertiesThatNeedToHaveSameUsage = new ArrayList();
    propertiesThatNeedToHaveSameUsage.add(ToscaConstants.PORT_FIXED_IPS);
    propertiesThatNeedToHaveSameUsage.add(ToscaConstants.PORT_ALLOWED_ADDRESS_PAIRS);
    propertiesThatNeedToHaveSameUsage.add(ToscaConstants.MAC_ADDRESS);

    propertiesThatNeedToHaveSameUsage.addAll(context.getEnrichPortResourceProperties());

    return propertiesThatNeedToHaveSameUsage;
  }

  private enum PropertyCheckCondition {
    EXIST,
    EQUAL
  }
}

