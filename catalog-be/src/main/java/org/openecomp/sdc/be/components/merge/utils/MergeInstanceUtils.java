package org.openecomp.sdc.be.components.merge.utils;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * This class is Utils class but it should be bean
 * @author dr2032
 *
 */
@org.springframework.stereotype.Component("MergeInstanceUtils")
public class MergeInstanceUtils {
    private Logger log = LoggerFactory.getLogger(MergeInstanceUtils.class);
    
    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    /**
     * Maps capability owner IDs of old component instance to capability owner IDs of the new component instance
     * @param container containing new component instance
     * @param origInstanceNode old component (in case of PROXY it should be actual service)
     * @param newInstanceId - ID of new instance of the component
     * @param oldCapabilitiesOwnerIds
     * @return
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


    private static boolean isCVFC(Component component) {
        ComponentTypeEnum componentType = component.getComponentType();
        if (!componentType.equals(ComponentTypeEnum.RESOURCE)) {
            return false;
        }

        Resource resource = (Resource) component;
        ResourceTypeEnum resourceType = resource.getResourceType();
        return resourceType == ResourceTypeEnum.CVFC;
    }


    /**
     * Maps capability owner IDs of old component instance to capability owner IDs of the new component instance
     * @param oldInstance
     * @param newInstance
     * @return
     */
    public Map<String, String> mapOldToNewCapabilitiesOwnerIds(ComponentInstance oldInstance, ComponentInstance newInstance) {
        List<ComponentInstance> oldVfcInstances  = getVfcInstances(oldInstance);
        List<ComponentInstance> newVfcInstances  = getVfcInstances(newInstance);

        Map<String, ComponentInstance> newVfciNameMap = convertToVfciNameMap(newVfcInstances);

        return oldVfcInstances.stream()
                            .filter(oldVfci -> newVfciNameMap.containsKey(oldVfci.getName()))
                            .collect(Collectors.toMap(ComponentInstance::getUniqueId, oldVfci -> newVfciNameMap.get(oldVfci.getName()).getUniqueId()));
    }


    /**
     * Method converts list of Component Instances to map of the instances where the key is their name
     * @param componentInstances
     * @return
     */
    public Map<String, ComponentInstance> convertToVfciNameMap(List<ComponentInstance> componentInstances) {
        return componentInstances != null ?
                componentInstances.stream()
                            .collect(Collectors.toMap(ComponentInstance::getName, identity())): Collections.emptyMap();
    }


    
    /**
     * Returns List of componentInstances by specified componentInstance
     * If componentInstance is for atomic component the returned list will contain the specified componentInstance only.
     * @param componentInstance
     * @return
     */
    public List<ComponentInstance> getVfcInstances(ComponentInstance componentInstance) {
        if (componentInstance == null) {
            return Collections.emptyList();
        }


        List<ComponentInstance> vfcInstances;

        String componentId = componentInstance.getActualComponentUid();
        Either<Component, StorageOperationStatus> eitherComponent = toscaOperationFacade.getToscaElement(componentId);

        if(eitherComponent.isLeft()) {
            Component component = eitherComponent.left().value();
            vfcInstances = getVfcInstances(componentInstance, component);
        }
        else {
            log.debug("Unexpected error: resource was not loaded for VF ID: {}", componentId);
            vfcInstances =  Collections.emptyList();
        }

        return vfcInstances;
    }


    /**
     * Returns List of componentInstances by specified componentInstance and component
     * If componentInstance is for atomic component the returned list will contain the specified componentInstance only.
     * @param componentInstance
     * @param eitherComponent
     * @return
     */
    public List<ComponentInstance> getVfcInstances(ComponentInstance componentInstance, Component component) {
        if (componentInstance == null || component == null) {
            return Collections.emptyList();
        }


        List<ComponentInstance> vfcInstances;

        if (ModelConverter.isAtomicComponent(component) || isCVFC(component)) {
            if (componentInstance.getIsProxy()) {
                // Component is proxy and it doesn't contain required data
                vfcInstances = getVfcInstances(componentInstance);
            }
            else {
                vfcInstances = Arrays.asList(componentInstance);
            }
        }
        else {
            vfcInstances = recursiveScanForAtomicComponentInstances(component);
        }

        return vfcInstances;
    }


    @VisibleForTesting
    public void setToscaOperationFacade(ToscaOperationFacade toscaOperationFacade) {
        this.toscaOperationFacade = toscaOperationFacade;
    }



    /**
     * @param component
     * @return
     */
    private List<ComponentInstance> recursiveScanForAtomicComponentInstances(Component component) {
        List<ComponentInstance> vfcInstances;

        List<ComponentInstance> componentInstances = component.getComponentInstances();
        if (componentInstances != null) {
            // Go recursively to collect atomic components only
            vfcInstances = componentInstances.stream()
                                             .map(this::getVfcInstances)
                                             .flatMap(List::stream)
                                             .collect(Collectors.toList());
        }
        else {
            vfcInstances = Collections.emptyList();
        }

        return vfcInstances;
    }



    /**
     * @param newInstanceId
     * @param oldCapabilitiesOwnerIds
     * @return
     */
    private Map<String, String> prepareMapForAtomicComponent(String newInstanceId, List<String> oldCapabilitiesOwnerIds) {
        Map<String, String> resultMap;

        int oldCapabilityOwnerIdsSize = oldCapabilitiesOwnerIds.size();
        if (oldCapabilityOwnerIdsSize == 1) {
            resultMap = new HashMap<>();
            resultMap.put(oldCapabilitiesOwnerIds.get(0), newInstanceId);
        }
        else {
            log.debug("For automic component the list of old capabilities owner Ids should contains one element while actual size is {},", oldCapabilityOwnerIdsSize);
            resultMap = Collections.emptyMap();
        }

        return resultMap;
    }

    /**
     * @param container
     * @param origInstanceNode
     * @param newInstanceId
     * @param oldCapabilitiesOwnerIds
     * @return
     */
    private Map<String, String> prepareMapForNonAtomicComponent(Component container, Component origInstanceNode,
                                                                    String newInstanceId, List<String> oldCapabilitiesOwnerIds) {
        Map<String, String> resultMap;
        List<ComponentInstance> oldVfcInstances = recursiveScanForAtomicComponentInstances(origInstanceNode);

        ComponentInstance newInstance = container.getComponentInstanceById(newInstanceId).orElse(null);
        if (newInstance == null) {
            log.debug("Failed to get component instance by newInstanceId: {}.", newInstanceId);
            resultMap = Collections.emptyMap();
        }
        else {
            resultMap = mapOldVfcIdsToNewOnes(oldCapabilitiesOwnerIds, oldVfcInstances, newInstance);
        }
        return resultMap;
    }

    /**
     * @param oldCapabilitiesOwnerIds
     * @param oldVfcInstances
     * @param newInstance
     * @return
     */
    private Map<String, String> mapOldVfcIdsToNewOnes(List<String> oldCapabilitiesOwnerIds,
                                                                List<ComponentInstance> oldVfcInstances, ComponentInstance newInstance) {
        List<ComponentInstance> newVfcInstances = getVfcInstances(newInstance);
        Map<String, ComponentInstance> newVfciNameMap = convertToVfciNameMap(newVfcInstances);

        return oldVfcInstances.stream()
                        .filter(oldVfc -> oldCapabilitiesOwnerIds.contains(oldVfc.getUniqueId()))
                        .filter(oldVfci -> newVfciNameMap.containsKey(oldVfci.getName()))
                        .collect(Collectors.toMap(ComponentInstance::getUniqueId, oldVfci -> newVfciNameMap.get(oldVfci.getName()).getUniqueId()));
    }

}
