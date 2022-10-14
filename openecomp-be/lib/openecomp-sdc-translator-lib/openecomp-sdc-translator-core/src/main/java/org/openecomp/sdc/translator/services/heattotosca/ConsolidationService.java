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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
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
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedConsolidationDataHandler;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.RequirementAssignmentData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;

public class ConsolidationService {

    private UnifiedCompositionService unifiedCompositionService;

    public ConsolidationService(UnifiedCompositionService unifiedCompositionService) {
        this.unifiedCompositionService = unifiedCompositionService;
    }

    ConsolidationService() {
    }

    static Map<String, String> getConsolidationEntityIdToType(ServiceTemplate serviceTemplate, ConsolidationData consolidationData) {
        Map<String, String> consolidationEntityIdToType = new HashMap<>();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        FileComputeConsolidationData fileComputeConsolidationData = consolidationData.getComputeConsolidationData()
            .getFileComputeConsolidationData(serviceTemplateFileName);
        FilePortConsolidationData filePortConsolidationData = consolidationData.getPortConsolidationData()
            .getFilePortConsolidationData(serviceTemplateFileName);
        if (Objects.nonNull(fileComputeConsolidationData)) {
            for (String computeType : fileComputeConsolidationData.getAllComputeTypes()) {
                TypeComputeConsolidationData typeComputeConsolidationData = fileComputeConsolidationData.getTypeComputeConsolidationData(computeType);
                Collection<String> computeNodeTemplateIds = typeComputeConsolidationData.getAllComputeNodeTemplateIds();
                for (String computeNodeTemplateId : computeNodeTemplateIds) {
                    consolidationEntityIdToType.put(computeNodeTemplateId, computeType);
                }
            }
        }
        if (Objects.nonNull(filePortConsolidationData)) {
            Set<String> portNodeTemplateIds = filePortConsolidationData.getAllPortNodeTemplateIds();
            for (String portNodeTemplateId : portNodeTemplateIds) {
                consolidationEntityIdToType
                    .put(portNodeTemplateId, filePortConsolidationData.getPortTemplateConsolidationData(portNodeTemplateId).getPortType());
            }
        }
        return consolidationEntityIdToType;
    }

    void serviceTemplateConsolidation(ServiceTemplate serviceTemplate, TranslationContext translationContext) {
        ConsolidationData consolidationData = translationContext.getConsolidationData();
        FileComputeConsolidationData fileComputeConsolidationData = consolidationData.getComputeConsolidationData()
            .getFileComputeConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
        if (Objects.isNull(fileComputeConsolidationData)) {
            return;
        }
        for (TypeComputeConsolidationData typeComputeConsolidationData : fileComputeConsolidationData.getAllTypeComputeConsolidationData()) {
            boolean preConditionResult = consolidationPreCondition(serviceTemplate, consolidationData, typeComputeConsolidationData);
            List<UnifiedCompositionData> unifiedCompositionDataList = createUnifiedCompositionDataList(serviceTemplate, consolidationData,
                typeComputeConsolidationData);
            if (preConditionResult) {
                boolean consolidationRuleCheckResult = checkConsolidationRules(serviceTemplate, typeComputeConsolidationData, consolidationData);
                unifiedCompositionService.createUnifiedComposition(serviceTemplate, null, unifiedCompositionDataList,
                    consolidationRuleCheckResult ? UnifiedCompositionMode.ScalingInstances : UnifiedCompositionMode.CatalogInstance,
                    translationContext);
            } else {
                unifiedCompositionService
                    .createUnifiedComposition(serviceTemplate, null, unifiedCompositionDataList, UnifiedCompositionMode.SingleSubstitution,
                        translationContext);
            }
        }
    }

    private boolean checkConsolidationRules(ServiceTemplate serviceTemplate, TypeComputeConsolidationData typeComputeConsolidationData,
                                            ConsolidationData consolidationData) {
        return checkComputeConsolidation(serviceTemplate, typeComputeConsolidationData) && checkPortConsolidation(serviceTemplate,
            typeComputeConsolidationData, consolidationData) && !checkGetAttrBetweenEntityConsolidationOfTheSameType(serviceTemplate,
            typeComputeConsolidationData, consolidationData);
    }

    private boolean checkGetAttrBetweenConsolidationDataEntitiesNotFromSameType(ServiceTemplate serviceTemplate,
                                                                                TypeComputeConsolidationData typeComputeConsolidationData,
                                                                                ConsolidationData consolidationData) {
        Collection<String> computeNodeTemplateIds = typeComputeConsolidationData.getAllComputeNodeTemplateIds();
        Map<String, List<String>> portTypeToIds = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        return typeComputeConsolidationData.isGetAttrOutFromEntityLegal(portTypeToIds) && checkGetAttrOutFromPortLegal(
            ToscaUtil.getServiceTemplateFileName(serviceTemplate), computeNodeTemplateIds, portTypeToIds, consolidationData);
    }

    private boolean checkGetAttrOutFromPortLegal(String serviceTemplateName, Collection<String> computeNodeTemplateIds,
                                                 Map<String, List<String>> portTypeToIds, ConsolidationData consolidationData) {
        for (List<String> portIdsFromSameType : portTypeToIds.values()) {
            List<PortTemplateConsolidationData> portTemplateConsolidationDataList = collectAllPortsTemplateConsolidationData(portIdsFromSameType,
                serviceTemplateName, consolidationData);
            if (!(checkGetAttrOutFromEntityToPortIsLegal(portTemplateConsolidationDataList, portTypeToIds)
                && checkGetAttrOutFromConsolidationEntityToEntityNotFromSameTypeIsLegal(portTemplateConsolidationDataList, computeNodeTemplateIds))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkGetAttrOutFromEntityToPortIsLegal(Collection<? extends EntityConsolidationData> entities,
                                                           Map<String, List<String>> portTypeToIds) {
        return CollectionUtils.isEmpty(entities) || entities.iterator().next().isGetAttrOutFromEntityLegal(entities, portTypeToIds);
    }

    private boolean checkGetAttrOutFromConsolidationEntityToEntityNotFromSameTypeIsLegal(List entityConsolidationDataList,
                                                                                         Collection<String> consolidationEntityNodeTemplateIds) {
        if (CollectionUtils.isEmpty(entityConsolidationDataList)) {
            return true;
        }
        EntityConsolidationData startingEntityTemplate = (EntityConsolidationData) entityConsolidationDataList.get(0);
        Map<String, Set<GetAttrFuncData>> startingGetAttrOutFuncData = getConsolidationEntityGetAttrOutFuncData(
            startingEntityTemplate.getNodesGetAttrOut(), consolidationEntityNodeTemplateIds);
        for (int i = 1; i < entityConsolidationDataList.size(); i++) {
            EntityConsolidationData currentEntityTemplate = (EntityConsolidationData) entityConsolidationDataList.get(i);
            Map<String, Set<GetAttrFuncData>> currentGetAttrOutFuncData = getConsolidationEntityGetAttrOutFuncData(
                currentEntityTemplate.getNodesGetAttrOut(), consolidationEntityNodeTemplateIds);
            if (!isGetAttrRelationToEntitySimilarBetweenEntities(startingGetAttrOutFuncData, currentGetAttrOutFuncData)) {
                return false;
            }
        }
        return true;
    }

    private boolean isGetAttrRelationToEntitySimilarBetweenEntities(Map<String, Set<GetAttrFuncData>> firstMap,
                                                                    Map<String, Set<GetAttrFuncData>> secondMap) {
        if (MapUtils.isEmpty(firstMap) != MapUtils.isEmpty(secondMap)) {
            return false;
        }
        return (MapUtils.isEmpty(firstMap) && MapUtils.isEmpty(secondMap)) || (new ArrayList<>(firstMap.values())
            .equals(new ArrayList<>(secondMap.values())));
    }

    private boolean checkSubInterfaceConsolidationPreCondition(ServiceTemplate serviceTemplate, ConsolidationData consolidationData,
                                                               TypeComputeConsolidationData typeComputeConsolidationData) {
        FilePortConsolidationData filePortConsolidationData = consolidationData.getPortConsolidationData()
            .getFilePortConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
        if (Objects.isNull(filePortConsolidationData)) {
            return true;
        }
        Map<String, List<String>> portTypeToPortIds = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        Collection<String> computeNodeTemplateIds = typeComputeConsolidationData.getAllComputeNodeTemplateIds();
        for (List<String> portIdsFromSameType : portTypeToPortIds.values()) {
            List<PortTemplateConsolidationData> portTemplateConsolidationDataList = getAllPortTemplateConsolidationData(portIdsFromSameType,
                filePortConsolidationData);
            if (!areSubInterfacePreConditionRulesValid(portTypeToPortIds, computeNodeTemplateIds, portTemplateConsolidationDataList,
                portTemplateConsolidationDataList.get(0))) {
                return false;
            }
        }
        return true;
    }

    private boolean areSubInterfacePreConditionRulesValid(Map<String, List<String>> portTypeToPortIds, Collection<String> computeNodeTemplateIds,
                                                          List<PortTemplateConsolidationData> portTemplateConsolidationDataList,
                                                          PortTemplateConsolidationData subInterfaceConsolidationData) {
        return areSubInterfaceTypesSimilarBetweenPorts(portTemplateConsolidationDataList, subInterfaceConsolidationData)
            && isNumberOfSubInterfacesPerTypeSimilar(portTemplateConsolidationDataList, subInterfaceConsolidationData)
            && isGetAttrFromSubInterfaceToOtherEntitiesLegal(computeNodeTemplateIds, portTypeToPortIds, portTemplateConsolidationDataList);
    }

    private boolean isGetAttrFromSubInterfaceToOtherEntitiesLegal(Collection<String> computeNodeTemplateIds,
                                                                  Map<String, List<String>> portTypeToPortIds,
                                                                  List<PortTemplateConsolidationData> portTemplateConsolidationDataList) {
        ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfacesFromSameTypeFromPorts = collectAllSubInterfacesFromSameTypeFromPorts(
            portTemplateConsolidationDataList);
        List<SubInterfaceTemplateConsolidationData> subInterfaceList = new ArrayList<>(subInterfacesFromSameTypeFromPorts.values());
        return areGetAttrRelationshipsBetweenSubInterfaceToConsolidationEntitiesValid(computeNodeTemplateIds, portTypeToPortIds,
            portTemplateConsolidationDataList, subInterfaceList);
    }

    private boolean areGetAttrRelationshipsBetweenSubInterfaceToConsolidationEntitiesValid(Collection<String> computeNodeTemplateIds,
                                                                                           Map<String, List<String>> portTypeToPortIds,
                                                                                           List<PortTemplateConsolidationData> portTemplateConsolidationDataList,
                                                                                           List<SubInterfaceTemplateConsolidationData> subInterfaceList) {
        return checkGetAttrOutFromEntityToPortIsLegal(subInterfaceList, portTypeToPortIds)
            && checkGetAttrOutFromConsolidationEntityToEntityNotFromSameTypeIsLegal(portTemplateConsolidationDataList,
            getSubInterfaceIdsFromSameType(subInterfaceList)) && checkGetAttrOutFromConsolidationEntityToEntityNotFromSameTypeIsLegal(
            subInterfaceList, computeNodeTemplateIds);
    }

    private boolean areSubInterfaceTypesSimilarBetweenPorts(List<PortTemplateConsolidationData> portTemplateConsolidationDataList,
                                                            PortTemplateConsolidationData subInterfaceConsolidationData) {
        return portTemplateConsolidationDataList.stream().allMatch(element -> element.hasSameSubInterfaceTypes(subInterfaceConsolidationData));
    }

    private boolean isNumberOfSubInterfacesPerTypeSimilar(List<PortTemplateConsolidationData> portTemplateConsolidationDataList,
                                                          PortTemplateConsolidationData subInterfaceConsolidationData) {
        return portTemplateConsolidationDataList.stream()
            .allMatch(element -> element.isNumberOfSubInterfacesPerTypeSimilar(subInterfaceConsolidationData));
    }

    private Map<String, Set<GetAttrFuncData>> getConsolidationEntityGetAttrOutFuncData(Map<String, List<GetAttrFuncData>> nodesGetAttrOut,
                                                                                       Collection<String> computeNodeTemplateIds) {
        Map<String, Set<GetAttrFuncData>> computeGetAttrFuncData = new HashMap<>();
        if (MapUtils.isEmpty(nodesGetAttrOut)) {
            return computeGetAttrFuncData;
        }
        for (Map.Entry<String, List<GetAttrFuncData>> getAttrFuncEntry : nodesGetAttrOut.entrySet()) {
            if (computeNodeTemplateIds.contains(getAttrFuncEntry.getKey())) {
                computeGetAttrFuncData.put(getAttrFuncEntry.getKey(), new HashSet<>(getAttrFuncEntry.getValue()));
            }
        }
        return computeGetAttrFuncData;
    }

    private boolean checkGetAttrBetweenEntityConsolidationOfTheSameType(ServiceTemplate serviceTemplate,
                                                                        TypeComputeConsolidationData typeComputeConsolidationData,
                                                                        ConsolidationData consolidationData) {
        return areThereGetAttrRelationsBetweenComputesOfSameType(typeComputeConsolidationData) || areThereGetAttrRelationsBetweenPortsOfTheSameType(
            serviceTemplate, typeComputeConsolidationData, consolidationData);
    }

    private boolean areThereGetAttrRelationsBetweenComputesOfSameType(TypeComputeConsolidationData typeComputeConsolidationData) {
        Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationEntities = typeComputeConsolidationData
            .getAllComputeTemplateConsolidationData();
        Collection<String> computeNodeTemplateIds = typeComputeConsolidationData.getAllComputeNodeTemplateIds();
        return checkGetAttrRelationsForEntityConsolidationData(computeTemplateConsolidationEntities, computeNodeTemplateIds,
            EntityConsolidationData::getNodesGetAttrIn);
    }

    private boolean areThereGetAttrRelationsBetweenPortsOfTheSameType(ServiceTemplate serviceTemplate,
                                                                      TypeComputeConsolidationData typeComputeConsolidationData,
                                                                      ConsolidationData consolidationData) {
        Map<String, List<String>> portTypeToPortIds = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        FilePortConsolidationData filePortConsolidationData = consolidationData.getPortConsolidationData()
            .getFilePortConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
        for (List<String> portsOfTheSameTypeIds : portTypeToPortIds.values()) {
            List<PortTemplateConsolidationData> portTemplateConsolidationDataOfSameType = getAllPortTemplateConsolidationData(portsOfTheSameTypeIds,
                filePortConsolidationData);
            if (!checkGetAttrRelationsForEntityConsolidationData(portTemplateConsolidationDataOfSameType, portsOfTheSameTypeIds,
                EntityConsolidationData::getNodesGetAttrIn)) {
                return false;
            }
        }
        return true;
    }

    private boolean areThereGetAttrRelationsBetweenSubInterfacesOfSameType(List<String> subInterfacesIdsFromSameType,
                                                                           List<SubInterfaceTemplateConsolidationData> subInterfaceList) {
        return checkGetAttrRelationsForEntityConsolidationData(subInterfaceList, subInterfacesIdsFromSameType,
            EntityConsolidationData::getNodesGetAttrIn) || checkGetAttrRelationsForEntityConsolidationData(subInterfaceList,
            subInterfacesIdsFromSameType, EntityConsolidationData::getNodesGetAttrOut);
    }

    private List<PortTemplateConsolidationData> getAllPortTemplateConsolidationData(List<String> portsIds,
                                                                                    FilePortConsolidationData filePortConsolidationData) {
        List<PortTemplateConsolidationData> portTemplateConsolidationDataOfSameType = new ArrayList<>();
        for (String portId : portsIds) {
            PortTemplateConsolidationData portTemplateConsolidationData = filePortConsolidationData.getPortTemplateConsolidationData(portId);
            if (Objects.nonNull(portTemplateConsolidationData)) {
                portTemplateConsolidationDataOfSameType.add(portTemplateConsolidationData);
            }
        }
        return portTemplateConsolidationDataOfSameType;
    }

    private boolean checkGetAttrRelationsForEntityConsolidationData(Collection entities, Collection<String> nodeTemplateIdsOfTheSameType,
                                                                    Function<EntityConsolidationData, Map<String, List<GetAttrFuncData>>> getAttrValuesMethod) {
        for (Object entity : entities) {
            Map<String, List<GetAttrFuncData>> getAttrValue = getAttrValuesMethod.apply((EntityConsolidationData) entity);
            Set<String> getAttrNodeIds = getAttrValue == null ? new HashSet<>() : getAttrValue.keySet();
            if (getAttrNodeIds.stream().anyMatch(nodeTemplateIdsOfTheSameType::contains)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkComputeConsolidation(ServiceTemplate serviceTemplate, TypeComputeConsolidationData typeComputeConsolidationData) {
        Collection<String> computeNodeTemplateIds = typeComputeConsolidationData.getAllComputeNodeTemplateIds();
        List<String> propertiesWithIdenticalVal = getComputePropertiesWithIdenticalVal();
        return arePropertiesSimilarBetweenComputeNodeTemplates(serviceTemplate, computeNodeTemplateIds, propertiesWithIdenticalVal)
            && checkComputeRelations(typeComputeConsolidationData.getAllComputeTemplateConsolidationData());
    }

    private boolean checkComputeRelations(Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationEntities) {
        return checkEntityConsolidationDataRelations(computeTemplateConsolidationEntities) && checkComputesRelationsToVolume(
            computeTemplateConsolidationEntities);
    }

    private boolean checkEntityConsolidationDataRelations(Collection entities) {
        EntityConsolidationData startingEntity = (EntityConsolidationData) entities.iterator().next();
        for (Object entity : entities) {
            EntityConsolidationData currentEntity = (EntityConsolidationData) entity;
            if (!(checkNodesConnectedInRelations(startingEntity, currentEntity) && (checkNodesConnectedOutRelations(startingEntity, currentEntity))
                && (checkGroupIdsRelations(startingEntity, currentEntity)))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkNodesConnectedInRelations(EntityConsolidationData firstEntity, EntityConsolidationData secondEntity) {
        return compareNodeConnectivity(firstEntity.getNodesConnectedIn(), secondEntity.getNodesConnectedIn());
    }

    private boolean checkNodesConnectedOutRelations(EntityConsolidationData firstEntity, EntityConsolidationData secondEntity) {
        return compareNodeConnectivity(firstEntity.getNodesConnectedOut(), secondEntity.getNodesConnectedOut());
    }

    private boolean compareNodeConnectivity(Multimap<String, RequirementAssignmentData> firstEntityMap,
                                            Multimap<String, RequirementAssignmentData> secondEntityMap) {
        if (CommonUtil.isMultimapEmpty(firstEntityMap) && CommonUtil.isMultimapEmpty(secondEntityMap)) {
            return true;
        }
        return !CommonUtil.isMultimapEmpty(firstEntityMap) && !CommonUtil.isMultimapEmpty(secondEntityMap) && equalsIgnoreSuffix(
            new HashSet<>(firstEntityMap.keySet()), new HashSet<>(secondEntityMap.keySet()));
    }

    private boolean equalsIgnoreSuffix(Set<String> firstKeySet, Set<String> secondKeySet) {
        Set<String> firstKeySetTrimmed = firstKeySet.stream().map(this::trimSuffix).collect(Collectors.toSet());
        Set<String> secondKeySetTrimmed = secondKeySet.stream().map(this::trimSuffix).collect(Collectors.toSet());
        return firstKeySetTrimmed.equals(secondKeySetTrimmed);
    }

    private String trimSuffix(String volumeName) {
        int suffixPosition = volumeName.lastIndexOf("_");
        return volumeName.substring(0, suffixPosition);
    }

    private boolean checkGroupIdsRelations(EntityConsolidationData startingEntity, EntityConsolidationData currentEntity) {
        return CollectionUtils.isEmpty(startingEntity.getGroupIds()) && CollectionUtils.isEmpty(currentEntity.getGroupIds()) || startingEntity
            .getGroupIds().equals(currentEntity.getGroupIds());
    }

    private boolean checkComputesRelationsToVolume(Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationEntities) {
        Iterator<ComputeTemplateConsolidationData> iterator = computeTemplateConsolidationEntities.iterator();
        Multimap<String, RequirementAssignmentData> startingVolumes = iterator.next().getVolumes();
        for (ComputeTemplateConsolidationData compute : computeTemplateConsolidationEntities) {
            Multimap<String, RequirementAssignmentData> currentVolumes = compute.getVolumes();
            if (!compareNodeConnectivity(startingVolumes, currentVolumes)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPortConsolidation(ServiceTemplate serviceTemplate, TypeComputeConsolidationData typeComputeConsolidationData,
                                           ConsolidationData consolidationData) {
        return validateWantedPortProperties(serviceTemplate, typeComputeConsolidationData) && checkPortRelations(
            ToscaUtil.getServiceTemplateFileName(serviceTemplate), typeComputeConsolidationData, consolidationData);
    }

    private boolean validateWantedPortProperties(ServiceTemplate serviceTemplate, TypeComputeConsolidationData typeComputeConsolidationData) {
        Map<String, List<String>> portTypeToIds = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        List<String> propertiesWithIdenticalVal = getPortPropertiesWithIdenticalVal();
        List<String> propertiesThatNeedToHaveSameUsage = getPortPropertiesThatNeedToHaveSameUsage();
        for (List<String> portsIds : portTypeToIds.values()) {
            if (!arePortPropertiesValid(serviceTemplate, propertiesWithIdenticalVal, propertiesThatNeedToHaveSameUsage, portsIds)) {
                return false;
            }
        }
        return true;
    }

    private boolean arePortPropertiesValid(ServiceTemplate serviceTemplate, List<String> propertiesWithIdenticalVal,
                                           List<String> propertiesThatNeedToHaveSameUsage, List<String> portsIds) {
        Map<String, NodeTemplate> nodeTemplates = serviceTemplate.getTopology_template().getNode_templates();
        Predicate<String> similar = property -> isPropertyValueSimilarBetweenNodeTemplates(property, portsIds, nodeTemplates);
        Predicate<String> exists = property -> isPropertyUsageSimilarBetweenAllNodeTemplates(property, portsIds, nodeTemplates);
        return areWantedPortPropertiesValid(propertiesWithIdenticalVal, similar) && areWantedPortPropertiesValid(propertiesThatNeedToHaveSameUsage,
            exists);
    }

    private boolean checkPortRelations(String serviceTemplateName, TypeComputeConsolidationData typeComputeConsolidationData,
                                       ConsolidationData consolidationData) {
        Map<String, List<String>> portTypeToIds = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        for (List<String> portIds : portTypeToIds.values()) {
            List<PortTemplateConsolidationData> portTemplateConsolidationDataList = collectAllPortsTemplateConsolidationData(portIds,
                serviceTemplateName, consolidationData);
            if (!checkEntityConsolidationDataRelations(portTemplateConsolidationDataList) || !checkSubInterfaceRules(
                portTemplateConsolidationDataList)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSubInterfaceRules(List<PortTemplateConsolidationData> portTemplateConsolidationDataList) {
        ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfaceTypeToEntity = collectAllSubInterfacesFromSameTypeFromPorts(
            portTemplateConsolidationDataList);
        List<SubInterfaceTemplateConsolidationData> subInterfaceList = new ArrayList<>(subInterfaceTypeToEntity.values());
        return areSubInterfacePropertiesAndRelationsValid(subInterfaceList);
    }

    private boolean areSubInterfacePropertiesAndRelationsValid(List<SubInterfaceTemplateConsolidationData> subInterfaceList) {
        return isResourceGroupPropertiesSimilarBetweenSubPorts(subInterfaceList) && checkSubInterfaceRelations(subInterfaceList)
            && !areThereGetAttrRelationsBetweenSubInterfacesOfSameType(getSubInterfaceIdsFromSameType(subInterfaceList), subInterfaceList);
    }

    private boolean checkSubInterfaceRelations(List<SubInterfaceTemplateConsolidationData> subInterfaceList) {
        return CollectionUtils.isEmpty(subInterfaceList) || checkEntityConsolidationDataRelations(subInterfaceList);
    }

    private boolean isResourceGroupPropertiesSimilarBetweenSubPorts(List<SubInterfaceTemplateConsolidationData> subInterfaceList) {
        if (CollectionUtils.isEmpty(subInterfaceList)) {
            return true;
        }
        SubInterfaceTemplateConsolidationData startingSubInterface = subInterfaceList.get(0);
        for (SubInterfaceTemplateConsolidationData subInterface : subInterfaceList) {
            if (!startingSubInterface.getResourceGroupCount().equals(subInterface.getResourceGroupCount()) || !StringUtils
                .equals(startingSubInterface.getNetworkRole(), subInterface.getNetworkRole())) {
                return false;
            }
        }
        return true;
    }

    private List<String> getSubInterfaceIdsFromSameType(List<SubInterfaceTemplateConsolidationData> subInterfaceList) {
        if (CollectionUtils.isEmpty(subInterfaceList)) {
            return new ArrayList<>();
        }
        return subInterfaceList.stream().map(SubInterfaceTemplateConsolidationData::getNodeTemplateId).collect(Collectors.toList());
    }

    private ListMultimap<String, SubInterfaceTemplateConsolidationData> collectAllSubInterfacesFromSameTypeFromPorts(
        List<PortTemplateConsolidationData> portTemplateConsolidationDataList) {
        ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfaceTypeToEntity = ArrayListMultimap.create();
        for (PortTemplateConsolidationData portTemplateConsolidationData : portTemplateConsolidationDataList) {
            portTemplateConsolidationData.copyMappedInto(subInterfaceTypeToEntity);
        }
        return subInterfaceTypeToEntity;
    }

    private List<PortTemplateConsolidationData> collectAllPortsTemplateConsolidationData(List<String> portIds, String serviceTemplateName,
                                                                                         ConsolidationData consolidationData) {
        FilePortConsolidationData filePortConsolidationData = consolidationData.getPortConsolidationData()
            .getFilePortConsolidationData(serviceTemplateName);
        List<PortTemplateConsolidationData> portTemplateConsolidationDataList = new ArrayList<>();
        for (String portId : portIds) {
            PortTemplateConsolidationData portTemplateConsolidationData = filePortConsolidationData.getPortTemplateConsolidationData(portId);
            if (Objects.nonNull(portTemplateConsolidationData)) {
                portTemplateConsolidationDataList.add(portTemplateConsolidationData);
            }
        }
        return portTemplateConsolidationDataList;
    }

    private boolean areWantedPortPropertiesValid(List<String> propertiesToCheck, Predicate<String> condition) {
        return propertiesToCheck.stream().allMatch(condition);
    }

    private boolean arePropertiesSimilarBetweenComputeNodeTemplates(ServiceTemplate serviceTemplate, Collection<String> computeNodeTemplateIds,
                                                                    List<String> propertiesThatNeedToBeSimilar) {
        Map<String, NodeTemplate> idToNodeTemplate = serviceTemplate.getTopology_template().getNode_templates();
        for (String property : propertiesThatNeedToBeSimilar) {
            if (!isPropertyValueSimilarBetweenNodeTemplates(property, computeNodeTemplateIds, idToNodeTemplate)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPropertyUsageSimilarBetweenAllNodeTemplates(String propertyToCheck, List<String> entityNodeTemplateIds,
                                                                  Map<String, NodeTemplate> idToNodeTemplate) {
        NodeTemplate startingNodeTemplate = idToNodeTemplate.get(entityNodeTemplateIds.get(0));
        if (Objects.isNull(startingNodeTemplate)) {
            throw new CoreException(new DuplicateResourceIdsInDifferentFilesErrorBuilder(entityNodeTemplateIds.get(0)).build());
        }
        boolean propertyExistCondition = isPropertyExistInNodeTemplate(propertyToCheck, startingNodeTemplate);
        for (int i = 1; i < entityNodeTemplateIds.size(); i++) {
            NodeTemplate currentNodeTemplate = idToNodeTemplate.get(entityNodeTemplateIds.get(i));
            if (Objects.isNull(currentNodeTemplate)) {
                throw new CoreException(new DuplicateResourceIdsInDifferentFilesErrorBuilder(entityNodeTemplateIds.get(i)).build());
            }
            if (propertyExistCondition != isPropertyExistInNodeTemplate(propertyToCheck, currentNodeTemplate)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPropertyValueSimilarBetweenNodeTemplates(String propertyToCheck, Collection<String> entityNodeTemplateIds,
                                                               Map<String, NodeTemplate> idToNodeTemplate) {
        Set<Object> propertiesValues = new HashSet<>();
        Iterator<String> iterator = entityNodeTemplateIds.iterator();
        handlePropertyValue(propertyToCheck, idToNodeTemplate, propertiesValues, iterator.next());
        while (iterator.hasNext()) {
            handlePropertyValue(propertyToCheck, idToNodeTemplate, propertiesValues, iterator.next());
        }
        return propertiesValues.size() == 1;
    }

    private void handlePropertyValue(String propertyToCheck, Map<String, NodeTemplate> idToNodeTemplate, Set<Object> propertiesValues,
                                     String nodeId) {
        NodeTemplate startingNodeTemplate = idToNodeTemplate.get(nodeId);
        if (Objects.isNull(startingNodeTemplate)) {
            throw new CoreException(new DuplicateResourceIdsInDifferentFilesErrorBuilder(nodeId).build());
        }
        addPropertyValue(propertyToCheck, startingNodeTemplate, propertiesValues);
    }

    private void addPropertyValue(String property, NodeTemplate nodeTemplate, Set<Object> propertiesValues) {
        propertiesValues.add(isPropertyExistInNodeTemplate(property, nodeTemplate) ? nodeTemplate.getProperties().get(property) : "");
    }

    private boolean isPropertyExistInNodeTemplate(String propertyToCheck, NodeTemplate nodeTemplate) {
        return !(nodeTemplate.getProperties() == null || nodeTemplate.getProperties().get(propertyToCheck) == null);
    }

    void substitutionServiceTemplateConsolidation(String substituteNodeTemplateId, ServiceTemplate serviceTemplate,
                                                  ServiceTemplate substitutionServiceTemplate, TranslationContext translationContext) {
        ConsolidationData consolidationData = translationContext.getConsolidationData();
        boolean substitutionConsolidationRuleResult = substitutionServiceTemplateConsolidationRule(substitutionServiceTemplate, translationContext);
        if (substitutionConsolidationRuleResult) {
            List<UnifiedCompositionData> unifiedCompositionDataList = createSubstitutionUnifiedCompositionDataList(substituteNodeTemplateId,
                serviceTemplate, substitutionServiceTemplate, consolidationData);
            unifiedCompositionService.createUnifiedComposition(serviceTemplate, substitutionServiceTemplate, unifiedCompositionDataList,
                UnifiedCompositionMode.NestedSingleCompute, translationContext);
        } else {
            //The node template does not represent unified VFC but complexVFC

            //Adding the id in the context for fixing connectivity from/to nested non-unified nodes
            translationContext.addUnifiedNestedNodeTemplateId(ToscaUtil.getServiceTemplateFileName(serviceTemplate), substituteNodeTemplateId,
                substituteNodeTemplateId);
            if (!translationContext.isUnifiedHandledServiceTemplate(substitutionServiceTemplate)) {
                serviceTemplateConsolidation(substitutionServiceTemplate, translationContext);
            }
        }
    }

    private boolean substitutionServiceTemplateConsolidationRule(ServiceTemplate nestedServiceTemplate, TranslationContext context) {
        ConsolidationData consolidationData = context.getConsolidationData();
        return consolidationData.getComputeConsolidationDataHandler()
            .isNumberOfComputeTypesLegal(ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate)) && !isThereMoreThanOneNestedLevel(
            nestedServiceTemplate, context);
    }

    private boolean isThereMoreThanOneNestedLevel(ServiceTemplate nestedServiceTemplate, TranslationContext context) {
        String nestedServiceTemplateName = ToscaUtil.getServiceTemplateFileName(nestedServiceTemplate);
        if (Objects.isNull(nestedServiceTemplateName)) {
            return false;
        }
        NestedConsolidationDataHandler nestedConsolidationDataHandler = context.getNestedConsolidationDataHandler();
        //Condition to check if there is nested file and if file contains only sub interfaces then

        // return false
        return nestedConsolidationDataHandler.isNestedConsolidationDataExist(nestedServiceTemplateName) && !ifNestedFileContainsOnlySubInterface(
            nestedServiceTemplate, context);
    }

    private boolean ifNestedFileContainsOnlySubInterface(ServiceTemplate serviceTemplate, TranslationContext context) {
        Map<String, NodeTemplate> nestedNodeTemplateMap = DataModelUtil.getNodeTemplates(serviceTemplate);
        Set<String> nestedHeatFileNames = getNestedHeatFileNames(nestedNodeTemplateMap);
        return ifAllResourceAreSubInterface(nestedHeatFileNames, context);
    }

    private Set<String> getNestedHeatFileNames(Map<String, NodeTemplate> nestedNodeTemplateMap) {
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        return nestedNodeTemplateMap.entrySet().stream().filter(
                entry -> toscaAnalyzerService.isSubstitutableNodeTemplate(entry.getValue()) && toscaAnalyzerService
                    .getSubstituteServiceTemplateName(entry.getKey(), entry.getValue()).isPresent())
            .map(entry -> toscaAnalyzerService.getSubstituteServiceTemplateName(entry.getKey(), entry.getValue()).get()).collect(Collectors.toSet());
    }

    // Method returns true if all of the resource are sub interface
    private boolean ifAllResourceAreSubInterface(Set<String> nestedHeatFileNames, TranslationContext context) {
        if (nestedHeatFileNames.isEmpty()) {
            return true;
        }
        for (String fileName : nestedHeatFileNames) {
            String heatFileName = context.getNestedHeatFileName().get(fileName);
            if (Objects.nonNull(heatFileName) && !context.getTranslatedServiceTemplates().get(heatFileName).getTopology_template().getNode_templates()
                .values().stream().allMatch(nodeTemplate -> ToscaNodeType.CONTRAILV2_VLAN_SUB_INTERFACE.equals(nodeTemplate.getType()))) {
                return false;
            }
        }
        return true;
    }

    private List<UnifiedCompositionData> createUnifiedCompositionDataList(ServiceTemplate serviceTemplate, ConsolidationData consolidationData,
                                                                          TypeComputeConsolidationData typeComputeConsolidationData) {
        List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
        for (ComputeTemplateConsolidationData computeTemplateConsolidationData : typeComputeConsolidationData
            .getAllComputeTemplateConsolidationData()) {
            UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
            unifiedCompositionData.setComputeTemplateConsolidationData(computeTemplateConsolidationData);
            FilePortConsolidationData filePortConsolidationData = consolidationData.getPortConsolidationData()
                .getFilePortConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
            setUnifiedCompositionDataWithPortTemplateData(computeTemplateConsolidationData, filePortConsolidationData, unifiedCompositionData);
            unifiedCompositionDataList.add(unifiedCompositionData);
        }
        return unifiedCompositionDataList;
    }

    private void setPortTemplateConsolidationData(FilePortConsolidationData filePortConsolidationData, String portId,
                                                  UnifiedCompositionData unifiedCompositionData,
                                                  List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList) {
        if (Objects.isNull(filePortConsolidationData)) {
            return;
        }
        PortTemplateConsolidationData portTemplateConsolidationData = filePortConsolidationData.getPortTemplateConsolidationData(portId);
        unifiedCompositionData.addPortTemplateConsolidationData(portTemplateConsolidationData);
        if (portTemplateConsolidationData != null) {
            portTemplateConsolidationData.copyFlatInto(subInterfaceTemplateConsolidationDataList);
        }
    }

    private List<UnifiedCompositionData> createSubstitutionUnifiedCompositionDataList(String substituteNodeTemplateId,
                                                                                      ServiceTemplate serviceTemplate,
                                                                                      ServiceTemplate substitutionServiceTemplate,
                                                                                      ConsolidationData consolidationData) {
        List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>();
        FileNestedConsolidationData fileNestedConsolidationData = consolidationData.getNestedConsolidationData()
            .getFileNestedConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
        if (Objects.nonNull(fileNestedConsolidationData)) {
            NestedTemplateConsolidationData nestedTemplateConsolidationData = fileNestedConsolidationData
                .getNestedTemplateConsolidationData(substituteNodeTemplateId);
            UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
            unifiedCompositionData.setNestedTemplateConsolidationData(nestedTemplateConsolidationData);
            unifiedCompositionDataList.add(unifiedCompositionData);
            addSubInterfaceDataToNestedCompositionData(substitutionServiceTemplate, consolidationData, unifiedCompositionData);
        }
        return unifiedCompositionDataList;
    }

    private void addSubInterfaceDataToNestedCompositionData(ServiceTemplate substitutionServiceTemplate, ConsolidationData consolidationData,
                                                            UnifiedCompositionData unifiedCompositionData) {
        FileComputeConsolidationData nestedFileComputeConsolidationData = consolidationData.getComputeConsolidationData()
            .getFileComputeConsolidationData(ToscaUtil.getServiceTemplateFileName(substitutionServiceTemplate));
        FilePortConsolidationData nestedFilePortConsolidationData = consolidationData.getPortConsolidationData()
            .getFilePortConsolidationData(ToscaUtil.getServiceTemplateFileName(substitutionServiceTemplate));
        if (Objects.isNull(nestedFileComputeConsolidationData) || Objects.isNull(nestedFilePortConsolidationData)) {
            return;
        }
        TypeComputeConsolidationData computeType = nestedFileComputeConsolidationData.getAllTypeComputeConsolidationData().iterator().next();
        if (Objects.isNull(computeType)) {
            return;
        }
        ComputeTemplateConsolidationData computeTemplateConsolidationData = computeType.getAllComputeTemplateConsolidationData().iterator().next();
        setUnifiedCompositionDataWithPortTemplateData(computeTemplateConsolidationData, nestedFilePortConsolidationData, unifiedCompositionData);
    }

    private void setUnifiedCompositionDataWithPortTemplateData(ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                               FilePortConsolidationData filePortConsolidationData,
                                                               UnifiedCompositionData unifiedCompositionData) {
        Collection<List<String>> portCollection =
            computeTemplateConsolidationData.getPorts() == null ? Collections.emptyList() : computeTemplateConsolidationData.getPorts().values();
        List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList = new ArrayList<>();
        portCollection.stream().flatMap(Collection::stream).forEach(
            portId -> setPortTemplateConsolidationData(filePortConsolidationData, portId, unifiedCompositionData,
                subInterfaceTemplateConsolidationDataList));
        unifiedCompositionData.setSubInterfaceTemplateConsolidationDataList(subInterfaceTemplateConsolidationDataList);
    }

    private boolean consolidationPreCondition(ServiceTemplate serviceTemplate, ConsolidationData consolidationData,
                                              TypeComputeConsolidationData typeComputeConsolidationData) {
        return (typeComputeConsolidationData.isThereMoreThanOneComputeTypeInstance() && typeComputeConsolidationData
            .isNumberOfPortsEqualsBetweenComputeNodes() && typeComputeConsolidationData.isNumberOfPortFromEachTypeLegal()
            && typeComputeConsolidationData.isPortTypesEqualsBetweenComputeNodes() && checkGetAttrBetweenConsolidationDataEntitiesNotFromSameType(
            serviceTemplate, typeComputeConsolidationData, consolidationData) && checkSubInterfaceConsolidationPreCondition(serviceTemplate,
            consolidationData, typeComputeConsolidationData));
    }

    List<String> getPropertiesWithIdenticalVal(UnifiedCompositionEntity entity) {
        switch (entity) {
            case COMPUTE:
                return getComputePropertiesWithIdenticalVal();
            case OTHER:
                return getComputePropertiesWithIdenticalVal();
            case PORT:
                return getPortPropertiesWithIdenticalVal();
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

    private List<String> getPortPropertiesWithIdenticalVal() {
        List<String> propertiesThatNeedToHaveIdenticalVal = new ArrayList<>();
        propertiesThatNeedToHaveIdenticalVal.add(ToscaConstants.PORT_ALLOWED_ADDRESS_PAIRS);
        propertiesThatNeedToHaveIdenticalVal.add(ToscaConstants.MAC_ADDRESS);
        propertiesThatNeedToHaveIdenticalVal.addAll(TranslationContext.getEnrichPortResourceProperties());
        return propertiesThatNeedToHaveIdenticalVal;
    }

    private List<String> getPortPropertiesThatNeedToHaveSameUsage() {
        List<String> propertiesThatNeedToHaveSameUsage = new ArrayList<>();
        propertiesThatNeedToHaveSameUsage.add(ToscaConstants.PORT_FIXED_IPS);
        propertiesThatNeedToHaveSameUsage.add(ToscaConstants.PORT_ALLOWED_ADDRESS_PAIRS);
        propertiesThatNeedToHaveSameUsage.add(ToscaConstants.MAC_ADDRESS);
        propertiesThatNeedToHaveSameUsage.addAll(TranslationContext.getEnrichPortResourceProperties());
        return propertiesThatNeedToHaveSameUsage;
    }
}
