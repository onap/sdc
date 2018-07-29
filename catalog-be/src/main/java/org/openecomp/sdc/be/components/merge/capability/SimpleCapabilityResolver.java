package org.openecomp.sdc.be.components.merge.capability;

import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.openecomp.sdc.be.dao.utils.MapUtil.flattenMapValues;
@org.springframework.stereotype.Component
public class SimpleCapabilityResolver implements CapabilityResolver {

    private MergeInstanceUtils mergeInstanceUtils;

    public SimpleCapabilityResolver(MergeInstanceUtils mergeInstanceUtils) {
        this.mergeInstanceUtils = mergeInstanceUtils;
    }

    @Override
    public Map<CapabilityDefinition, CapabilityDefinition> resolvePrevCapToNewCapability(Component container, Component prevInstanceOrigNode, String cmptInstanceId, List<CapabilityDefinition> prevCapabilities) {
        List<CapabilityDefinition> newCapabilities = resolveInstanceCapabilities(container, cmptInstanceId);
        Map<String, String> oldCapOwnerToNewOwner = mapOldToNewCapabilitiesOwnerIds(container, prevInstanceOrigNode, cmptInstanceId, prevCapabilities);
        return mapOldToNewCapabilities(prevCapabilities, newCapabilities, oldCapOwnerToNewOwner);
    }

    @Override
    public Map<CapabilityDefinition, CapabilityDefinition> resolvePrevCapIdToNewCapability(ComponentInstance oldInstance, ComponentInstance currInstance) {
        List<CapabilityDefinition> newCapabilities = flattenMapValues(currInstance.getCapabilities());
        List<CapabilityDefinition> prevCapabilities = flattenMapValues(oldInstance.getCapabilities());
        Map<String, String> oldCapOwnerToNewOwner = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(oldInstance, currInstance);
        return mapOldToNewCapabilities(prevCapabilities, newCapabilities, oldCapOwnerToNewOwner);
    }

    private Map<CapabilityDefinition, CapabilityDefinition> mapOldToNewCapabilities(List<CapabilityDefinition> prevCapabilities, List<CapabilityDefinition> newCapabilities, Map<String, String> oldCapOwnerToNewOwner) {
        Map<CapabilityDefinition, CapabilityDefinition> oldToNewCapability = findNewCapByPrevCapabilityNameTypeAndOwner(prevCapabilities, newCapabilities, oldCapOwnerToNewOwner);
        removeNotFoundNewCapabilities(oldToNewCapability);
        return oldToNewCapability;
    }

    private Map<CapabilityDefinition, CapabilityDefinition> findNewCapByPrevCapabilityNameTypeAndOwner(List<CapabilityDefinition> prevCapabilities, List<CapabilityDefinition> newCapabilities, Map<String, String> oldCapOwnerToNewOwner) {
        Map<CapabilityDefinition, CapabilityDefinition> prevToNewCapabilityMapping = new HashMap<>();
        prevCapabilities.forEach(prevCap -> {
            CapabilityDefinition newCapability = mapOldToNewCapability(prevCap, newCapabilities, oldCapOwnerToNewOwner);
            prevToNewCapabilityMapping.put(prevCap, newCapability);
        });
        return prevToNewCapabilityMapping;
    }

    private CapabilityDefinition mapOldToNewCapability(CapabilityDefinition prevCap, List<CapabilityDefinition> newCapabilities, Map<String, String> oldCapOwnerToNewOwner) {
        String newOwnerId = oldCapOwnerToNewOwner.get(prevCap.getOwnerId());
        return newCapabilities.stream()
                       .filter(newCap -> newCap.getName().equals(prevCap.getName()))
                       .filter(newCap -> newCap.getType().equals(prevCap.getType()))
                       .filter(newCap -> newCap.getOwnerId().equals(newOwnerId))
                       .findFirst()
                       .orElse(null);
    }


    private Map<String, String> mapOldToNewCapabilitiesOwnerIds(Component container, Component origInstanceNode, String cmptInstanceId, List<CapabilityDefinition> prevCapabilities) {
        List<String> prevCapOwnerIds = prevCapabilities.stream().map(CapabilityDefinition::getOwnerId).distinct().collect(toList());
        return mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, origInstanceNode, cmptInstanceId, prevCapOwnerIds);
    }

    private void removeNotFoundNewCapabilities(Map<CapabilityDefinition, CapabilityDefinition> oldToNewCapMap) {
        oldToNewCapMap.values().removeIf(Objects::isNull);
    }

    private List<CapabilityDefinition> resolveInstanceCapabilities(Component capabilityOwnerContainer, String cmptInstanceId) {
        return capabilityOwnerContainer.getComponentInstanceById(cmptInstanceId)
                .map(ComponentInstance::getCapabilities)
                .map(MapUtil::flattenMapValues)
                .orElse(new ArrayList<>());
    }





}
