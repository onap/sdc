package org.openecomp.sdc.be.components.merge.instance;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.att.aft.dme2.internal.google.common.annotations.VisibleForTesting;

import fj.data.Either;


@org.springframework.stereotype.Component("ComponentInstanceRelashionMerge")
public class ComponentInstanceRelationMerge implements ComponentInstanceMergeInterface {
    private static Logger log = LoggerFactory.getLogger(ComponentInstanceRelationMerge.class);
    
    @Autowired
    private ComponentsUtils componentsUtils;
    
    @Autowired
    private MergeInstanceUtils mergeInstanceUtils;

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;


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
            List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(currentResourceInstance, originComponent);

            if  (vfcInstances != null) {
                List<RelationMergeInfo> fromRelInfoList = convert(relationsFrom, rel -> mapRelationRequirement(rel, vfcInstances));
                List<RelationMergeInfo> toRelInfoList = convert(relationsTo, rel -> mapRelationCapability(rel, vfcInstances));

                // Encapsulate all needed info in one container
                VfRelationsMergeInfo vfRelationsMergeInfo = new VfRelationsMergeInfo(fromRelInfoList, toRelInfoList);
                // Save it
                dataHolder.setVfRelationsInfo(vfRelationsMergeInfo);
            }
        }
        else {
            log.debug("No relations relevant to currentResourceInstance {} found in container component", currentResourceInstance);
        }

    }


    @Override
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Wrapper<Either<Component, ResponseFormat>> resultWrapper = new Wrapper<>();

        VfRelationsMergeInfo vfRelationsMergeInfo = getRelationsMergeInfo(dataHolder, updatedContainerComponent, resultWrapper);

        ComponentInstance newComponentInstance = null;
        if(resultWrapper.isEmpty()) {
            //Component Instance
            newComponentInstance = loadComponentInstance(updatedContainerComponent, newInstanceId, resultWrapper);
        }

        if(resultWrapper.isEmpty() && vfRelationsMergeInfo != null) {
            // Load VFCI and filter them by name
            List<ComponentInstance> vfcInstances = mergeInstanceUtils.getVfcInstances(newComponentInstance);
            if(vfcInstances != null) {
                Map<String, ComponentInstance> vfciMap = mergeInstanceUtils.convertToVfciNameMap(vfcInstances);

                // Process Relationships
                List<RelationMergeInfo> toRelationsInfo = vfRelationsMergeInfo.getToRelationsInfo();
                Stream<RequirementCapabilityRelDef> toRelationsInfoStream = null;
                if (toRelationsInfo != null) {
                    toRelationsInfoStream = toRelationsInfo.stream()
                                            .map(oldCapInfo -> restoreCapabilityRelation(oldCapInfo, newInstanceId, vfciMap, updatedContainerComponent))
                                            .filter(Objects::nonNull);
                }

                List<RelationMergeInfo> fromRelationsInfo = vfRelationsMergeInfo.getFromRelationsInfo();
                Stream<RequirementCapabilityRelDef> fromRelationsInfoStream = null;
                if( fromRelationsInfo != null) {
                    //For Each old requirement relation info
                    fromRelationsInfoStream = fromRelationsInfo.stream()
                                                .map(oldReqInfo -> restoreRequirementRelation(oldReqInfo, newInstanceId, vfciMap, updatedContainerComponent))
                                                .filter(Objects::nonNull);
                }

                // Save relations in updated container (service)
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
    
    @VisibleForTesting
    public void setToscaOperationFacade(ToscaOperationFacade toscaOperationFacade) {
        this.toscaOperationFacade = toscaOperationFacade;
    }
    
    
    @VisibleForTesting
    public void setComponentsUtils(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }
    
    @VisibleForTesting
    public void setMergeInstanceUtils(MergeInstanceUtils mergeInstanceUtils) {
        this.mergeInstanceUtils = mergeInstanceUtils;
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
        final String vfInstanceId = currentResourceInstance.getUniqueId();

        return componentInstancesRelations.stream()
                                            .filter(rel -> StringUtils.equals(getNodeFunc.apply(rel), vfInstanceId))
                                            .collect(Collectors.toList());
    }

    private List<RelationMergeInfo> convert(List<RequirementCapabilityRelDef> relationsDef, 
                                            Function<RequirementCapabilityRelDef, RelationMergeInfo> mapFunc) {
        return relationsDef.stream()
                            .map(mapFunc::apply)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
    }
    
    private RelationMergeInfo mapRelationCapability(RequirementCapabilityRelDef relDef, List<ComponentInstance> vfcInstances) {
        // Id of the VfcInstance that is the owner of the capability
        String ownerId = relDef.resolveSingleRelationship().getRelation().getCapabilityOwnerId();
        return createRelationMergeInfo(vfcInstances, ownerId, currVfcInst -> mapVfcInstanceCapability(currVfcInst, relDef));
    }
    
    private RelationMergeInfo mapRelationRequirement(RequirementCapabilityRelDef relDef, List<ComponentInstance> vfcInstances) {
        // Id of the VfcInstance that is the owner of the requirement
        String ownerId = relDef.resolveSingleRelationship().getRelation().getRequirementOwnerId();
        return createRelationMergeInfo(vfcInstances, ownerId, currVfcInst -> mapVfcInstanceRequirement(currVfcInst, relDef));
    }
    
    private RelationMergeInfo createRelationMergeInfo(List<ComponentInstance> vfcInstances, String ownerId, Function<ComponentInstance, RelationMergeInfo> mapFunc) {
        return vfcInstances.stream()
                            .filter(inst -> StringUtils.equals(inst.getUniqueId(), ownerId))
                            .map(mapFunc::apply)
                            .filter(Objects::nonNull)
                            .findAny()
                            .orElse(null);
    }
    
    
    private RelationMergeInfo mapVfcInstanceCapability(ComponentInstance vfcInstance, RequirementCapabilityRelDef relDef) {
        String capabilityUniqueId = relDef.resolveSingleRelationship().getRelation().getCapabilityUid();


        String vfcInstanceName = vfcInstance.getName();
        String vfcUid = vfcInstance.getComponentUid();

        Either<Resource, StorageOperationStatus> vfcResource = toscaOperationFacade.getToscaElement(vfcUid);
        if(vfcResource.isLeft()) {
            Resource vfc = vfcResource.left().value();

            CapabilityDefinition capabilityDef = retrieveCapabilityDefinition(capabilityUniqueId, vfc);
            String capabilityType;
            String capabilityName;
            if (capabilityDef != null) {
                capabilityType = capabilityDef.getType();
                capabilityName = capabilityDef.getName();
            }
            else {
                log.debug("Failed to retrieve capability type for relation with name: {} and uniqueId {}", relDef.resolveSingleRelationship().getRelation().getCapability(), capabilityUniqueId);
                capabilityType = null;
                capabilityName = null;
            }

            return new RelationMergeInfo(capabilityType, capabilityName, vfcInstanceName, relDef);
        }
        else {
            log.debug("Failed to load VFC by uid {}", vfcUid);
            return null;
        }
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
                log.debug("Failed to retrieve requirement type for relation with name: {} and uniqueId {}", relDef.resolveSingleRelationship().getRelation().getRequirement(), requirementUniqueId);
                requirementType = null;
                requirementName = null;
            }

            return new RelationMergeInfo(requirementType, requirementName, vfcInstanceName, relDef);
        }
        else {
            log.debug("Failed to load VFC by uid {}", vfcUid);
            return null;
        }
    }

    private CapabilityDefinition retrieveCapabilityDefinition(String uniqueId, Resource vfc) {
        return vfc.getCapabilities().values().stream()
                                                .flatMap(List::stream)
                                                .filter(Objects::nonNull)
                                                .filter(def -> uniqueId.equals(def.getUniqueId()))
                                                .findFirst()
                                                .orElse(null);
    }
    
    private String retrieveCapabilityUid(String name, Component vfc) {
        return vfc.getCapabilities().values().stream()
                                                .flatMap(List::stream)
                                                .filter(Objects::nonNull)
                                                .filter(def -> name.equals(def.getName()))
                                                .findFirst()
                                                .map(CapabilityDefinition::getUniqueId)
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

    private String retrieveRequirementUid(String name, Component vfc) {
        return vfc.getRequirements().values().stream()
                                                .flatMap(List::stream)
                                                .filter(Objects::nonNull)
                                                .filter(def -> name.equals(def.getName()))
                                                .findFirst()
                                                .map(RequirementDefinition::getUniqueId)
                                                .orElse(null);
    }



    private VfRelationsMergeInfo getRelationsMergeInfo(DataForMergeHolder dataHolder,
                                                        Component updatedContainerComponent,
                                                        Wrapper<Either<Component, ResponseFormat>> resultWrapper) {
        VfRelationsMergeInfo vfRelationsMergeInfo = dataHolder.getVfRelationsMergeInfo();
        if (vfRelationsMergeInfo == null) {
            log.debug("There is no info about relations should be restored.");
            resultWrapper.setInnerElement(Either.left(updatedContainerComponent));
        }

        return vfRelationsMergeInfo;
    }


    private RequirementCapabilityRelDef restoreCapabilityRelation(RelationMergeInfo oldCapInfo,
                                                                  String newInstanceId,
                                                                  Map<String, ComponentInstance> vfciMap,
                                                                  Component updatedContainerComponent) {
        String oldVfcInstanceName = oldCapInfo.getVfcInstanceName();

        ComponentInstance newVfcInstance = vfciMap.get(oldVfcInstanceName);
        if (newVfcInstance != null) {
            // Append relation to updated container
            RequirementCapabilityRelDef oldRelDef = oldCapInfo.getRelDef();
            oldRelDef.setToNode(newInstanceId);

            RelationshipInfo oldRelationshipInfo = oldRelDef.resolveSingleRelationship().getRelation();
            oldRelationshipInfo.setCapabilityOwnerId(newVfcInstance.getUniqueId());
            oldRelationshipInfo.getRelationship().setType(oldCapInfo.getCapReqType());


            String vfcUid = newVfcInstance.getComponentUid();
            Either<Component, StorageOperationStatus> eitherComponent = toscaOperationFacade.getToscaElement(vfcUid);

            if(eitherComponent.isLeft()) {
                String capabilityUid = retrieveCapabilityUid(oldCapInfo.getCapReqName() , eitherComponent.left().value());
                oldRelationshipInfo.setCapabilityUid(capabilityUid);
            }
            else {
                log.debug("Unexpected error: resource was not loaded for VF ID: {}", vfcUid);
            }

            updatedContainerComponent.getComponentInstancesRelations().add(oldRelDef);
            return oldRelDef;
        }
        else {
            log.debug("Skip relation since it was not found VFC Instance with name {}", oldVfcInstanceName);
            return null;
        }
    }



    private RequirementCapabilityRelDef restoreRequirementRelation(RelationMergeInfo oldReqInfo,
                                                                    String newInstanceId,
                                                                   Map<String, ComponentInstance> vfciMap,
                                                                   Component updatedContainerComponent) {
        String oldVfcInstanceName = oldReqInfo.getVfcInstanceName();

        ComponentInstance newVfcInstance = vfciMap.get(oldReqInfo.getVfcInstanceName());
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
                log.debug("Unexpected error: resource was not loaded for VF ID: {}", vfcUid);
            }

            updatedContainerComponent.getComponentInstancesRelations().add(oldRelDef);
            return oldRelDef;
        }
        else {
            log.debug("Skip relation since it was not found VFC Instance with name {}", oldVfcInstanceName);
            return null;
        }
    }

    
}
