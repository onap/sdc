package org.openecomp.sdc.be.components.merge.utils;

import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.components.merge.instance.RelationMergeInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.openecomp.sdc.be.dao.utils.MapUtil.toMap;

/**
 * This class is Utils class but it should be bean
 * @author dr2032
 *
 */
@org.springframework.stereotype.Component
public class MergeInstanceUtils {
    private static final Logger log = Logger.getLogger(MergeInstanceUtils.class);
    
    private final ToscaOperationFacade toscaOperationFacade;
    private final ExceptionUtils exceptionUtils;

    public MergeInstanceUtils(ToscaOperationFacade toscaOperationFacade, ExceptionUtils exceptionUtils) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.exceptionUtils = exceptionUtils;
    }

    /**
     * @param container containing new component instance
     * @param origInstanceNode old component (in case of PROXY it should be actual service)
     * @param newInstanceId - ID of new instance of the component
     * @param oldCapabilitiesOwnerIds the old capabilities owner ids
     * @return a map of capability owner IDs of old component instance to capability owner IDs of the new component instance
     */
    public Map<String, String> mapOldToNewCapabilitiesOwnerIds(Component container,
                                                               Component origInstanceNode,
                                                               String newInstanceId,
                                                               List<String> oldCapabilitiesOwnerIds) {

        Map<String, String> resultMap;

        if (ModelConverter.isAtomicComponent(origInstanceNode) || isCVFC(origInstanceNode)) {
            resultMap = prepareMapForAtomicComponent(newInstanceId, oldCapabilitiesOwnerIds);
        }
        else {
            resultMap = prepareMapForNonAtomicComponent(container, origInstanceNode, newInstanceId, oldCapabilitiesOwnerIds);
        }

        return resultMap;
    }

    /**
     * @param oldInstance the old instance to find its capabilities owner ids
     * @param newInstance the new instance to find its capabilities owner ids
     * @return a map between capability owner IDs of old component instance to capability owner IDs of the new component instance
     */
    public Map<String, String> mapOldToNewCapabilitiesOwnerIds(ComponentInstance oldInstance, ComponentInstance newInstance) {
        List<CapabilityOwner> prevCapabilityOwners  = getInstanceAtomicBuildingBlocks(oldInstance).getCapabilitiesOwners();
        List<CapabilityOwner> newCapOwners  = getInstanceAtomicBuildingBlocks(newInstance).getCapabilitiesOwners();
        return getCapabilitiesOwnerMapping(prevCapabilityOwners, newCapOwners);
    }

    /**
     * @param oldResource - old version of the Resource
     * @param newResource - new version of the same Resource
     * @return list of updated Relations created in UI
     */
    public List<RequirementCapabilityRelDef> updateUiRelationsInResource(Resource oldResource, Resource newResource) {
        Map<String, ComponentInstance> mapOldComponentInstances = buildComponentInstanceMap(oldResource, ComponentInstance::getUniqueId);
        Map<String, ComponentInstance> mapNewComponentInstances = buildComponentInstanceMap(newResource, ComponentInstance::getName);

        return getUpdatedCapReqDefs(oldResource,
                mapOldComponentInstances,
                mapNewComponentInstances,
                RequirementCapabilityRelDef::isOriginUI);
    }

    /**
     *
     * @param componentInstance the instance which its building blocks are to be returned
     * @return the atomic building (groups and instances) blocks which the given component instance is a composition of
     */
    public ComponentInstanceBuildingBlocks getInstanceAtomicBuildingBlocks(ComponentInstance componentInstance) {
        if (componentInstance == null) {
            return ComponentInstanceBuildingBlocks.empty();
        }
        String componentId = componentInstance.getActualComponentUid();
        Component component = toscaOperationFacade.getToscaElement(componentId).left().on(err -> exceptionUtils.rollBackAndThrow(err, componentId));
        return getInstanceAtomicBuildingBlocks(componentInstance, component);
    }

    /**
     *
     * @param componentInstance the instance which its building blocks are to be returned
     * @param component the type thar the given component instance was created from
     * @return the atomic building blocks (groups and instances) which the given component instance is a composition of
     */
    public ComponentInstanceBuildingBlocks getInstanceAtomicBuildingBlocks(ComponentInstance componentInstance, Component component) {
        if (componentInstance == null || component == null) {
            return ComponentInstanceBuildingBlocks.empty();
        }
        ComponentInstanceBuildingBlocks instanceBuildingBlocks;
        if (ModelConverter.isAtomicComponent(component) || isCVFC(component)) {
            if (componentInstance.getIsProxy()) {
                // Component is proxy and it doesn't contain required data
                instanceBuildingBlocks = getInstanceAtomicBuildingBlocks(componentInstance);
            }
            else {
                instanceBuildingBlocks = ComponentInstanceBuildingBlocks.of(new ArrayList<>(), singletonList(componentInstance));
            }
            return instanceBuildingBlocks;
        }
        else {
            instanceBuildingBlocks = recursiveScanForAtomicBuildingBlocks(component);
            if(org.apache.commons.collections.MapUtils.isNotEmpty(component.getCapabilities()) || org.apache.commons.collections.MapUtils.isNotEmpty(component.getRequirements())) {
                ComponentInstanceBuildingBlocks nonAtomicBlocks = ComponentInstanceBuildingBlocks.of(new ArrayList<>(), singletonList(componentInstance));
                return ComponentInstanceBuildingBlocks.merge(instanceBuildingBlocks, nonAtomicBlocks);
            }
            return instanceBuildingBlocks;
        
        }
    }

    public RelationMergeInfo mapRelationCapability(RequirementCapabilityRelDef relDef, List<CapabilityOwner> capsOwners) {
        String ownerId = relDef.resolveSingleRelationship().getRelation().getCapabilityOwnerId();
        return createCapabilityRelationMergeInfo(capsOwners, ownerId, relDef);
    }

    public RelationMergeInfo mapRelationRequirement(RequirementCapabilityRelDef relDef, List<ComponentInstance> vfcInstances) {
        String ownerId = relDef.resolveSingleRelationship().getRelation().getRequirementOwnerId();
        return createRequirementRelationMergeInfo(vfcInstances, ownerId, relDef);
    }


    public RequirementCapabilityRelDef restoreCapabilityRelation(RelationMergeInfo oldCapInfo,
                                                                 String newInstanceId,
                                                                 Map<String, CapabilityOwner> capOwnerByName,
                                                                 Component updatedContainerComponent) {
        String oldCapOwnerName = oldCapInfo.getCapOwnerName();

        CapabilityOwner newCapOwner = capOwnerByName.get(oldCapOwnerName);
        if (newCapOwner != null) {
            // Append relation to updated container
            RequirementCapabilityRelDef oldRelDef = oldCapInfo.getRelDef();
            oldRelDef.setToNode(newInstanceId);
            RelationshipInfo oldRelationshipInfo = oldRelDef.resolveSingleRelationship().getRelation();
            oldRelationshipInfo.setCapabilityOwnerId(newCapOwner.getUniqueId());
            oldRelationshipInfo.getRelationship().setType(oldCapInfo.getCapReqType());
            String capabilityUid = retrieveCapabilityUid(oldCapInfo.getCapReqName(), newCapOwner);
            oldRelationshipInfo.setCapabilityUid(capabilityUid);
            if (updatedContainerComponent != null) {
                updatedContainerComponent.getComponentInstancesRelations().add(oldRelDef);
            }
            return oldRelDef;
        } else {
            log.debug("#restoreCapabilityRelation - Skip relation since it was not found VFC Instance with name {}", oldCapOwnerName);
            return null;
        }
    }



    public RequirementCapabilityRelDef restoreRequirementRelation(RelationMergeInfo oldReqInfo,
                                                                  String newInstanceId,
                                                                  Map<String, ComponentInstance> vfciMap,
                                                                  Component updatedContainerComponent) {
        String oldVfcInstanceName = oldReqInfo.getCapOwnerName();

        ComponentInstance newVfcInstance = vfciMap.get(oldReqInfo.getCapOwnerName());
        if (newVfcInstance != null) {
            // Append relation to updated container
            RequirementCapabilityRelDef oldRelDef = oldReqInfo.getRelDef();
            oldRelDef.setFromNode(newInstanceId);

            RelationshipInfo oldRelationshipInfo = oldRelDef.resolveSingleRelationship().getRelation();
            oldRelationshipInfo.setRequirementOwnerId(newVfcInstance.getUniqueId());
            oldRelationshipInfo.getRelationship().setType(oldReqInfo.getCapReqType());

            String vfcUid = newVfcInstance.getComponentUid();
            Either<Component, StorageOperationStatus> eitherComponent = toscaOperationFacade.getToscaElement(vfcUid);

            if(eitherComponent.isLeft()) {
                String requirementUid = retrieveRequirementUid(oldReqInfo.getCapReqName() , eitherComponent.left().value());
                oldRelationshipInfo.setRequirementUid(requirementUid);
            }
            else {
                log.debug("#restoreRequirementCapabilityRelDef - Unexpected error: resource was not loaded for VF ID: {}", vfcUid);
            }

            if (updatedContainerComponent != null) {
                updatedContainerComponent.getComponentInstancesRelations().add(oldRelDef);
            }
            return oldRelDef;
        }
        else {
            log.debug("#restoreRequirementCapabilityRelDef - Skip relation since it was not found VFC Instance with name {}", oldVfcInstanceName);
            return null;
        }
    }

    private List<ComponentInstance> getVfcInstances(ComponentInstance componentInstance) {
        return getInstanceAtomicBuildingBlocks(componentInstance).getVfcInstances();
    }

    private Map<String, String> getCapabilitiesOwnerMapping(List<CapabilityOwner> oldCapOwners, List<CapabilityOwner> newCapOwners) {
        Map<String, CapabilityOwner> newCapOwnerNameMap = toMap(newCapOwners, CapabilityOwner::getName, (p1, p2) -> p1);
        return oldCapOwners.stream()
                .filter(oldCapOwner -> newCapOwnerNameMap.containsKey(oldCapOwner.getName()))
                .collect(Collectors.toMap(CapabilityOwner::getUniqueId, oldCapOwner -> newCapOwnerNameMap.get(oldCapOwner.getName()).getUniqueId(), (p1, p2) -> p1));
    }

    private static boolean isCVFC(Component component) {
        ComponentTypeEnum componentType = component.getComponentType();
        if (!componentType.equals(ComponentTypeEnum.RESOURCE)) {
            return false;
        }
        Resource resource = (Resource) component;
        ResourceTypeEnum resourceType = resource.getResourceType();
        return resourceType == ResourceTypeEnum.CVFC;
    }


    private RequirementCapabilityRelDef mergeCapRelDefs(RequirementCapabilityRelDef capRelDefFrom, RequirementCapabilityRelDef capRelDefTo) {
        if (capRelDefFrom == capRelDefTo) {
            return capRelDefFrom;
        }
        else if (capRelDefFrom == null) {
            return capRelDefTo;
        }
        else if (capRelDefTo == null) {
            return capRelDefFrom;
        }
        
        RelationshipInfo relationshipInfoFrom = capRelDefFrom.resolveSingleRelationship().getRelation();
        RelationshipInfo relationshipInfoTo = capRelDefTo.resolveSingleRelationship().getRelation();
        
        relationshipInfoFrom.setCapabilityOwnerId(relationshipInfoTo.getCapabilityOwnerId());
        relationshipInfoFrom.setCapabilityUid(relationshipInfoTo.getCapabilityUid());
        
        return capRelDefFrom;
    }
    


    private Map<String, ComponentInstance> buildComponentInstanceMap(Resource oldRresource, Function<ComponentInstance, String> getKeyFunc) {
        return oldRresource.getComponentInstances().stream()
                .collect(Collectors.toMap(getKeyFunc, Function.identity(), (p1, p2) -> p1));
    }

    private List<RequirementCapabilityRelDef> getUpdatedCapReqDefs(Resource oldResource,
                                                                   Map<String, ComponentInstance> mapOldComponentInstances,
                                                                   Map<String, ComponentInstance> mapNewComponentInstances,
                                                                   Predicate<? super RequirementCapabilityRelDef> filter) {
        return oldResource.getComponentInstancesRelations().stream()
                        .filter(filter)
                        .map(rel -> createRelationMergeInfoPair(rel, mapOldComponentInstances))
                        .map(infoPair -> restoreRequirementCapabilityRelDef(infoPair, mapNewComponentInstances))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    private ImmutablePair<RelationMergeInfo, RelationMergeInfo> createRelationMergeInfoPair(RequirementCapabilityRelDef reqCapDef,
                                                                                            Map<String, ComponentInstance> mapOldComponentInstances) {
        
        ComponentInstance oldComponentInstanceFrom = mapOldComponentInstances.get(reqCapDef.getFromNode());
        RelationMergeInfo fromRelationMergeInfo = createRequirmentRelationMergeInfo(oldComponentInstanceFrom, reqCapDef);
        
        ComponentInstance oldComponentInstanceTo = mapOldComponentInstances.get(reqCapDef.getToNode());
        RelationMergeInfo toRelationMergeInfo = createCapabilityRelationMergeInfo(oldComponentInstanceTo, reqCapDef);
        return new ImmutablePair<>(fromRelationMergeInfo, toRelationMergeInfo);
    }

    private RelationMergeInfo createRequirmentRelationMergeInfo(ComponentInstance componentInstance, RequirementCapabilityRelDef reqCapDef ) {
        
        List<ComponentInstance> vfcInstances = getVfcInstances(componentInstance);
        if  (vfcInstances != null) { 
            return mapRelationRequirement(reqCapDef, vfcInstances);
        }
        else {
            log.debug("#createRelationMergeInfo - It's unexpected that vfc instnaces were not found for {}", componentInstance);
            return null;
        }
    }

    private RelationMergeInfo createCapabilityRelationMergeInfo(ComponentInstance componentInstance,
                                                      RequirementCapabilityRelDef reqCapDef) {
        List<CapabilityOwner> capabilityOwners = getInstanceAtomicBuildingBlocks(componentInstance).getCapabilitiesOwners();
        return mapRelationCapability(reqCapDef, capabilityOwners);
    }

    
    private RequirementCapabilityRelDef restoreRequirementCapabilityRelDef(ImmutablePair<RelationMergeInfo, RelationMergeInfo> mergeInfoPair, Map<String, ComponentInstance> mapNewComponentInstances) {
        RequirementCapabilityRelDef capRelDefFrom = restoreRequirementRelDef(mergeInfoPair, mapNewComponentInstances);
        RequirementCapabilityRelDef capRelDefTo = restoreCapabilityRelDef(mergeInfoPair, mapNewComponentInstances);

        return mergeCapRelDefs(capRelDefFrom, capRelDefTo);
    }

    private RequirementCapabilityRelDef restoreRequirementRelDef(ImmutablePair<RelationMergeInfo, RelationMergeInfo> mergeInfoPair, Map<String, ComponentInstance> mapNewComponentInstances) {
        RequirementCapabilityRelDef capRelDefFrom;
        RelationMergeInfo mergeInfoFrom = mergeInfoPair.getLeft();
        if (mergeInfoFrom != null) {
            ComponentInstance newComponentInstanceFrom = mapNewComponentInstances.get(mergeInfoFrom.getCapOwnerName());
            capRelDefFrom = restoreRequirementRelDef(newComponentInstanceFrom, mergeInfoFrom,  newComponentInstanceFrom.getUniqueId());
        }
        else {
            capRelDefFrom = null;
        }
        return capRelDefFrom;
    }

    private RequirementCapabilityRelDef restoreCapabilityRelDef(ImmutablePair<RelationMergeInfo, RelationMergeInfo> mergeInfoPair, Map<String, ComponentInstance> mapNewComponentInstances) {
        RequirementCapabilityRelDef capRelDefTo;
        RelationMergeInfo mergeInfoTo = mergeInfoPair.getRight();
        if (mergeInfoTo != null) {
            ComponentInstance newComponentInstanceTo = mapNewComponentInstances.get(mergeInfoTo.getCapOwnerName());
            capRelDefTo = restoreCapabilityRelDef(newComponentInstanceTo, mergeInfoTo, newComponentInstanceTo.getUniqueId());
        }
        else {
            capRelDefTo = null;
        }
        return capRelDefTo;
    }

    private RequirementCapabilityRelDef  restoreRequirementRelDef(ComponentInstance newComponentInstance, RelationMergeInfo mergeInfoFrom, String newComponentInstanceFromId) {
        if (newComponentInstance != null) {
            List<ComponentInstance> vfcInstances = getVfcInstances(newComponentInstance);
            if(vfcInstances != null) {
                Map<String, ComponentInstance> vfciMap = toMap(vfcInstances, ComponentInstance::getName, (p1, p2) -> p1);
                return restoreRequirementRelation(mergeInfoFrom, newComponentInstanceFromId, vfciMap, null);
            }
            else {
                log.debug("#restoreRequirementCapabilityRelDef - It was not found VFC instances for component instance {}", newComponentInstance);
            }
        }
        return null;
    }

    private RequirementCapabilityRelDef restoreCapabilityRelDef(ComponentInstance newComponentInstance, RelationMergeInfo mergeInfoTo, String newComponentInstanceToId) {
        if (newComponentInstance != null) {
            List<CapabilityOwner> capsOwners = getInstanceAtomicBuildingBlocks(newComponentInstance).getCapabilitiesOwners();
            if(capsOwners != null) {
                Map<String, CapabilityOwner> vfciMap = toMap(capsOwners, CapabilityOwner::getName, (p1, p2) -> p1);
                return restoreCapabilityRelation(mergeInfoTo, newComponentInstanceToId, vfciMap, null);
            }
            else {
                log.debug("#restoreRequirementCapabilityRelDef - It was not found VFC instances for component instance {}", newComponentInstance);
            }
        }
        return null;
    }


    private ComponentInstanceBuildingBlocks recursiveScanForAtomicBuildingBlocks(Component component) {
        ComponentInstanceBuildingBlocks capsOwners = ComponentInstanceBuildingBlocks.of(component.getGroups(), null);
        List<ComponentInstance> componentInstances = component.safeGetComponentInstances();
        // Go recursively to collect atomic components only
        ComponentInstanceBuildingBlocks propsOwnersRec = componentInstances.stream()
                .map(this::getInstanceAtomicBuildingBlocks)
                .reduce(ComponentInstanceBuildingBlocks::merge)
                .orElse(ComponentInstanceBuildingBlocks.empty());
        return ComponentInstanceBuildingBlocks.merge(capsOwners, propsOwnersRec);
    }


    private Map<String, String> prepareMapForAtomicComponent(String newInstanceId, List<String> oldCapabilitiesOwnerIds) {
        Map<String, String> resultMap;

        int oldCapabilityOwnerIdsSize = oldCapabilitiesOwnerIds.size();
        if (oldCapabilityOwnerIdsSize == 1) {
            resultMap = new HashMap<>();
            resultMap.put(oldCapabilitiesOwnerIds.get(0), newInstanceId);
        }
        else {
            log.debug("#prepareMapForAtomicComponent - For atomic component the list of old capabilities owner Ids should contains one element while actual size is {},", oldCapabilityOwnerIdsSize);
            resultMap = emptyMap();
        }

        return resultMap;
    }

    private Map<String, String> prepareMapForNonAtomicComponent(Component container, Component origInstanceNode,
                                                                    String newInstanceId, List<String> oldCapabilitiesOwnerIds) {
        ComponentInstance newInstance = container.getComponentInstanceById(newInstanceId).orElse(null);
        if (newInstance == null) {
            log.debug("#prepareMapForNonAtomicComponent - Failed to get component instance by newInstanceId: {}.", newInstanceId);
            return emptyMap();
        }
        List<CapabilityOwner> prevCapOwners = recursiveScanForAtomicBuildingBlocks(origInstanceNode).getCapabilitiesOwners();
        Component origNewCmpt = toscaOperationFacade.getToscaElement(newInstance.getActualComponentUid()).left().on(err -> exceptionUtils.rollBackAndThrow(err, newInstance.getActualComponentUid()));
        return mapOldOwnerIdsToNewOnes(oldCapabilitiesOwnerIds, prevCapOwners, newInstance, origNewCmpt);
    }

    private Map<String, String> mapOldOwnerIdsToNewOnes(List<String> oldCapabilitiesOwnerIds,
                                                        List<CapabilityOwner> prevCapOwners, ComponentInstance newInstance, Component origNewInstanceType) {
        List<CapabilityOwner> newCapOwners = getInstanceAtomicBuildingBlocks(newInstance, origNewInstanceType).getCapabilitiesOwners();
        return getCapabilitiesOwnerMapping(oldCapabilitiesOwnerIds, prevCapOwners, newCapOwners);
    }

    private Map<String, String> getCapabilitiesOwnerMapping(List<String> oldCapabilitiesOwnerIds, List<CapabilityOwner> prevCapOwners, List<CapabilityOwner> newCapOwners) {
        Map<String, CapabilityOwner> capOwnersByName = toMap(newCapOwners, CapabilityOwner::getName, (p1, p2) -> p1);
        return prevCapOwners
                .stream()
                .filter(oldCapOwner -> oldCapabilitiesOwnerIds.contains(oldCapOwner.getUniqueId()))
                .filter(oldCapOwner -> capOwnersByName.containsKey(oldCapOwner.getName()))
                .collect(Collectors.toMap(CapabilityOwner::getUniqueId, oldCapOwner -> capOwnersByName.get(oldCapOwner.getName()).getUniqueId(), (p1, p2) -> p1));
    }


    private RelationMergeInfo createCapabilityRelationMergeInfo(List<CapabilityOwner> vfcInstances, String ownerId, RequirementCapabilityRelDef relation) {
        return vfcInstances.stream()
                            .filter(inst -> StringUtils.equals(inst.getUniqueId(), ownerId))
                            .map(capabilityOwner -> getCapabilityMergeInfo(capabilityOwner, relation))
                            .findAny()
                            .orElse(null);
    }


    private RelationMergeInfo createRequirementRelationMergeInfo(List<ComponentInstance> vfcInstances, String ownerId, RequirementCapabilityRelDef relation) {
        return vfcInstances.stream()
                .filter(inst -> StringUtils.equals(inst.getUniqueId(), ownerId))
                .map(currVfcInst -> mapVfcInstanceRequirement(currVfcInst, relation))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    private RelationMergeInfo getCapabilityMergeInfo(CapabilityOwner capabilityOwner, RequirementCapabilityRelDef relDef) {
        String capabilityUniqueId = relDef.resolveSingleRelationship().getRelation().getCapabilityUid();
        String capOwnerName = capabilityOwner.getName();
        CapabilityDefinition capabilityDef = retrieveCapabilityDefinition(capabilityUniqueId, capabilityOwner);
        String capabilityType;
        String capabilityName;
        if (capabilityDef != null) {
            capabilityType = capabilityDef.getType();
            capabilityName = capabilityDef.getName();
        } else {
            log.debug("#getCapabilityMergeInfo - Failed to retrieve capability type for relation with name: {} and uniqueId {}", relDef.resolveSingleRelationship().getRelation().getCapability(), capabilityUniqueId);
            capabilityType = null;
            capabilityName = null;
        }
        return new RelationMergeInfo(capabilityType, capabilityName, capOwnerName, relDef);
    }
    
    private RelationMergeInfo mapVfcInstanceRequirement(ComponentInstance vfcInstance, RequirementCapabilityRelDef relDef) {
        String requirementUniqueId = relDef.resolveSingleRelationship().getRelation().getRequirementUid();
        
        String vfcInstanceName = vfcInstance.getName();
        String vfcUid = vfcInstance.getComponentUid();
        
        Either<Resource, StorageOperationStatus> vfcResource = toscaOperationFacade.getToscaElement(vfcUid);
        if(vfcResource.isLeft()) {
            Resource vfc = vfcResource.left().value();
            
            RequirementDefinition requirementDef = retrieveRequirementDefinition(requirementUniqueId, vfc);
            String requirementType;
            String requirementName;
            if (requirementDef != null) {
                requirementType = requirementDef.getCapability();
                requirementName = requirementDef.getName();
            }
            else {
                log.debug("#mapVfcInstanceRequirement - Failed to retrieve requirement type for relation with name: {} and uniqueId {}", relDef.resolveSingleRelationship().getRelation().getRequirement(), requirementUniqueId);
                requirementType = null;
                requirementName = null;                
            }
            
            return new RelationMergeInfo(requirementType, requirementName, vfcInstanceName, relDef);
        }
        else {
            log.debug("#mapVfcInstanceRequirement - Failed to load VFC by uid {}", vfcUid); 
            return null;
        }
    }

    private CapabilityDefinition retrieveCapabilityDefinition(String uniqueId, CapabilityOwner capabilityOwner) {
        return capabilityOwner.getCapabilities().values().stream()
                                                .flatMap(List::stream)
                                                .filter(Objects::nonNull)
                                                .filter(def -> uniqueId.equals(def.getUniqueId()))
                                                .findFirst()
                                                .orElse(null);
    }
    
    private RequirementDefinition retrieveRequirementDefinition(String uniqueId, Resource vfc) {
        return vfc.getRequirements().values().stream()
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .filter(def -> uniqueId.equals(def.getUniqueId()))
                .findFirst()
                .orElse(null);
    }
    
    private String retrieveCapabilityUid(String name, CapabilityOwner capabilityOwner) {
        return capabilityOwner.getCapabilities().values()
                                                .stream()
                                                .flatMap(List::stream)
                                                .filter(Objects::nonNull)
                                                .filter(def -> name.equals(def.getName()))
                                                .findFirst()
                                                .map(CapabilityDefinition::getUniqueId)
                                                .orElse(null);
    }

    private String retrieveRequirementUid(String name, Component vfc) {
        return vfc.getRequirements().values().stream()
                                                .flatMap(List::stream)
                                                .filter(Objects::nonNull)
                                                .filter(def -> name.equals(def.getName()))
                                                .findFirst()
                                                .map(RequirementDefinition::getUniqueId)
                                                .orElse(null);
    }
}
