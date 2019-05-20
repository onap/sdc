package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.merge.utils.CapabilityOwner;
import org.openecomp.sdc.be.components.merge.utils.ComponentInstanceBuildingBlocks;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@org.springframework.stereotype.Component("ComponentInstanceRelashionMerge")
public class ComponentInstanceRelationMerge implements ComponentInstanceMergeInterface {
    private static final Logger log = Logger.getLogger(ComponentInstanceRelationMerge.class);
    
    private final ComponentsUtils componentsUtils;
    private final MergeInstanceUtils mergeInstanceUtils;
    private final ToscaOperationFacade toscaOperationFacade;

    public ComponentInstanceRelationMerge(ComponentsUtils componentsUtils, MergeInstanceUtils mergeInstanceUtils, ToscaOperationFacade toscaOperationFacade) {
        this.componentsUtils = componentsUtils;
        this.mergeInstanceUtils = mergeInstanceUtils;
        this.toscaOperationFacade = toscaOperationFacade;
    }
    

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent) {
        //All Relationships - container (service) holds info about all relations
        //Filter by UniqueId in from/to
        List<RequirementCapabilityRelDef> relationsFrom = getRelations(RequirementCapabilityRelDef::getFromNode,
                                                                      containerComponent,
                                                                      currentResourceInstance);

        List<RequirementCapabilityRelDef> relationsTo = getRelations(RequirementCapabilityRelDef::getToNode,
                                                                    containerComponent,
                                                                    currentResourceInstance);

        if (!relationsFrom.isEmpty() || !relationsTo.isEmpty()) {
            ComponentInstanceBuildingBlocks instBuildingBlocks = mergeInstanceUtils.getInstanceAtomicBuildingBlocks(currentResourceInstance, originComponent);

            if  (instBuildingBlocks != null) {
                List<RelationMergeInfo> fromRelInfoList = convert(relationsFrom, rel -> mergeInstanceUtils.mapRelationRequirement(rel, instBuildingBlocks.getVfcInstances()));
                List<RelationMergeInfo> toRelInfoList = convert(relationsTo, rel -> mergeInstanceUtils.mapRelationCapability(rel, instBuildingBlocks.getCapabilitiesOwners()));

                // Encapsulate all needed info in one container
                ContainerRelationsMergeInfo containerRelationsMergeInfo = new ContainerRelationsMergeInfo(fromRelInfoList, toRelInfoList);
                // Save it
                dataHolder.setVfRelationsInfo(containerRelationsMergeInfo);
            }
        }
        else {
            log.debug("No relations relevant to currentResourceInstance {} found in container component", currentResourceInstance);
        }

    }


    @Override
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Wrapper<Either<Component, ResponseFormat>> resultWrapper = new Wrapper<>();

        ContainerRelationsMergeInfo containerRelationsMergeInfo = getRelationsMergeInfo(dataHolder, updatedContainerComponent, resultWrapper);

        ComponentInstance newComponentInstance = null;
        if(resultWrapper.isEmpty()) {
            //Component Instance
            newComponentInstance = loadComponentInstance(updatedContainerComponent, newInstanceId, resultWrapper);
        }

        if(resultWrapper.isEmpty() && containerRelationsMergeInfo != null) {
            // Load VFCI and filter them by name
            ComponentInstanceBuildingBlocks instanceBuildBlocks = mergeInstanceUtils.getInstanceAtomicBuildingBlocks(newComponentInstance);
            if(instanceBuildBlocks != null) {
                // Process Relationships
                Stream<RequirementCapabilityRelDef> toRelationsInfoStream = getCapabilitiesRelationInfoStream(updatedContainerComponent, newInstanceId, containerRelationsMergeInfo, instanceBuildBlocks);
                Stream<RequirementCapabilityRelDef> fromRelationsInfoStream = getRequirementRelationsInfoStream(updatedContainerComponent, newInstanceId, containerRelationsMergeInfo, instanceBuildBlocks);
                List<RequirementCapabilityRelDef> updatedRelations = getUpdatedRelations(toRelationsInfoStream, fromRelationsInfoStream);
                StorageOperationStatus saveResult = toscaOperationFacade.associateResourceInstances(updatedContainerComponent.getUniqueId(), updatedRelations);
                if (saveResult == StorageOperationStatus.OK) {
                    resultWrapper.setInnerElement(Either.left(updatedContainerComponent));
                }
                else {
                    log.debug("Failed to associate instances of resource {} status is {}", updatedContainerComponent.getUniqueId(), saveResult);
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(saveResult), updatedContainerComponent.getUniqueId());
                    resultWrapper.setInnerElement(Either.right(responseFormat));
                }
            }
        }
        return resultWrapper.getInnerElement();
    }

    private Stream<RequirementCapabilityRelDef> getRequirementRelationsInfoStream(Component updatedContainerComponent, String newInstanceId, ContainerRelationsMergeInfo containerRelationsMergeInfo, ComponentInstanceBuildingBlocks instanceBuildBlocks) {
        Map<String, ComponentInstance> vfciMap = MapUtil.toMap(instanceBuildBlocks.getVfcInstances(), ComponentInstance::getName, (p1, p2) -> p1);
        List<RelationMergeInfo> fromRelationsInfo = containerRelationsMergeInfo.getFromRelationsInfo();
        Stream<RequirementCapabilityRelDef> fromRelationsInfoStream = null;
        if( fromRelationsInfo != null) {
            fromRelationsInfoStream = fromRelationsInfo.stream()
                                        .map(oldReqInfo -> mergeInstanceUtils.restoreRequirementRelation(oldReqInfo, newInstanceId, vfciMap, updatedContainerComponent))
                                        .filter(Objects::nonNull);
        }
        return fromRelationsInfoStream;
    }

    private Stream<RequirementCapabilityRelDef> getCapabilitiesRelationInfoStream(Component updatedContainerComponent, String newInstanceId, ContainerRelationsMergeInfo containerRelationsMergeInfo, ComponentInstanceBuildingBlocks instanceBuildBlocks) {
        Map<String, CapabilityOwner> capOwnersByName = MapUtil.toMap(instanceBuildBlocks.getCapabilitiesOwners(), CapabilityOwner::getName, (p1, p2) -> p1);
        List<RelationMergeInfo> toRelationsInfo = containerRelationsMergeInfo.getToRelationsInfo();
        Stream<RequirementCapabilityRelDef> toRelationsInfoStream = null;
        if (toRelationsInfo != null) {
            toRelationsInfoStream = toRelationsInfo.stream()
                                    .map(oldCapInfo -> mergeInstanceUtils.restoreCapabilityRelation(oldCapInfo, newInstanceId, capOwnersByName, updatedContainerComponent))
                                    .filter(Objects::nonNull);
        }
        return toRelationsInfoStream;
    }

    /**
     * @param containerComponent
     * @param instanceId
     * @param resultWrapper
     * @return
     */
    private ComponentInstance loadComponentInstance(Component containerComponent, String instanceId,
            Wrapper<Either<Component, ResponseFormat>> resultWrapper) {
        ComponentInstance componentInstance = containerComponent.getComponentInstanceById(instanceId).orElse(null);
        if (componentInstance == null) {
            log.debug("Failed to get VF instance by new VF instance ID: {}", instanceId);
            resultWrapper.setInnerElement(Either.left(containerComponent));
        }

        return componentInstance;
    }


    private List<RequirementCapabilityRelDef> getUpdatedRelations(Stream<RequirementCapabilityRelDef> toRelationsInfoStream, 
                                                                  Stream<RequirementCapabilityRelDef> fromRelationsInfoStream) {
        Stream<RequirementCapabilityRelDef> updatedRelationsStream = Stream.empty();

        if (toRelationsInfoStream != null) {
            updatedRelationsStream = Stream.concat(updatedRelationsStream, toRelationsInfoStream);
        }

        if (fromRelationsInfoStream != null) {
            updatedRelationsStream = Stream.concat(updatedRelationsStream, fromRelationsInfoStream);
        }

        return updatedRelationsStream.collect(Collectors.toList());
    }

    private List<RequirementCapabilityRelDef> getRelations(Function<RequirementCapabilityRelDef, String> getNodeFunc,
                                                           Component containerComponent,
                                                           ComponentInstance currentResourceInstance) {

        final List<RequirementCapabilityRelDef> componentInstancesRelations = containerComponent.getComponentInstancesRelations();
        if (componentInstancesRelations == null) {
            return Collections.emptyList();
        }
        
        final String vfInstanceId = currentResourceInstance.getUniqueId();

        return componentInstancesRelations.stream()
                                            .filter(rel -> StringUtils.equals(getNodeFunc.apply(rel), vfInstanceId))
                                            .collect(Collectors.toList());
    }

    private List<RelationMergeInfo> convert(List<RequirementCapabilityRelDef> relationsDef, 
                                            Function<RequirementCapabilityRelDef, RelationMergeInfo> mapFunc) {
        return relationsDef.stream()
                            .map(mapFunc)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
    }
    
    private ContainerRelationsMergeInfo getRelationsMergeInfo(DataForMergeHolder dataHolder,
                                                              Component updatedContainerComponent,
                                                              Wrapper<Either<Component, ResponseFormat>> resultWrapper) {
        ContainerRelationsMergeInfo containerRelationsMergeInfo = dataHolder.getContainerRelationsMergeInfo();
        if (containerRelationsMergeInfo == null) {
            log.debug("There is no info about relations should be restored.");
            resultWrapper.setInnerElement(Either.left(updatedContainerComponent));
        }

        return containerRelationsMergeInfo;
    }
}
