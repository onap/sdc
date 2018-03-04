package org.openecomp.sdc.be.components.merge.capability;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class SimpleCapabilityResolver implements CapabilityResolver {

    private MergeInstanceUtils mergeInstanceUtils;

    public SimpleCapabilityResolver(MergeInstanceUtils mergeInstanceUtils) {
        this.mergeInstanceUtils = mergeInstanceUtils;
    }

    @Override
    public Map<CapabilityDefinition, CapabilityDefinition> resolvePrevCapToNewCapability(Component container, Component prevInstanceOrigNode, String cmptInstanceId, List<CapabilityDefinition> prevCapabilities) {
        Map<String, List<CapabilityDefinition>> newCapabilitiesByType = resolveInstanceCapabilities(container, cmptInstanceId).getCapabilities();
        Map<String, String> oldCapOwnerToNewOwner = mapOldToNewCapabilitiesOwnerIds(container, prevInstanceOrigNode, cmptInstanceId, prevCapabilities);
        return mapOldToNewCapabilities(prevCapabilities, newCapabilitiesByType, oldCapOwnerToNewOwner);
    }

    @Override
    public Map<CapabilityDefinition, CapabilityDefinition> resolvePrevCapIdToNewCapability(ComponentInstance oldInstance, ComponentInstance currInstance) {
        Map<String, List<CapabilityDefinition>> newCapabilitiesByType = currInstance.getCapabilities();
        Map<String, String> oldCapOwnerToNewOwner = mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(oldInstance, currInstance);
        List<CapabilityDefinition> prevCapabilities = oldInstance.getCapabilities().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        return mapOldToNewCapabilities(prevCapabilities, newCapabilitiesByType, oldCapOwnerToNewOwner);
    }

    private Map<CapabilityDefinition, CapabilityDefinition> mapOldToNewCapabilities(List<CapabilityDefinition> prevCapabilities, Map<String, List<CapabilityDefinition>> newCapabilitiesByType, Map<String, String> oldCapOwnerToNewOwner) {
        Map<CapabilityDefinition, CapabilityDefinition> oldToNewCapability = prevCapabilities
                .stream()
                .collect(HashMap::new,
                        (resultMap, prevCap) -> mapOldToNewCapability(newCapabilitiesByType, oldCapOwnerToNewOwner, resultMap, prevCap),
                        HashMap::putAll);
        removeNotFoundNewCapabilities(oldToNewCapability);
        return oldToNewCapability;
    }

    private CapabilityDefinition mapOldToNewCapability(Map<String, List<CapabilityDefinition>> newCapabilitiesByType, Map<String, String> oldCapOwnerToNewOwner, Map<CapabilityDefinition, CapabilityDefinition> resultMap, CapabilityDefinition prevCap) {
        return resultMap.put(prevCap, findCurrCapability(newCapabilitiesByType, prevCap, oldCapOwnerToNewOwner.get(prevCap.getOwnerId())));
    }

    private Map<String, String> mapOldToNewCapabilitiesOwnerIds(Component container, Component origInstanceNode, String cmptInstanceId, List<CapabilityDefinition> prevCapabilities) {
        List<String> prevCapOwnerIds = prevCapabilities.stream().map(CapabilityDefinition::getOwnerId).distinct().collect(Collectors.toList());
        return mergeInstanceUtils.mapOldToNewCapabilitiesOwnerIds(container, origInstanceNode, cmptInstanceId, prevCapOwnerIds);
    }

    private void removeNotFoundNewCapabilities(Map<CapabilityDefinition, CapabilityDefinition> oldToNewCapMap) {
        oldToNewCapMap.values().removeIf(Objects::isNull);
    }

    private ComponentInstance resolveInstanceCapabilities(Component capabilityOwnerContainer, String cmptInstanceId) {
        return MapUtil.toMap(capabilityOwnerContainer.getComponentInstances(), ComponentInstance::getUniqueId).get(cmptInstanceId);
    }


    private CapabilityDefinition findCurrCapability(Map<String, List<CapabilityDefinition>> capabilitiesByType, CapabilityDefinition oldCap, String newCapOwnerId) {
        List<CapabilityDefinition> newCapOfType = capabilitiesByType.get(oldCap.getType());
        if (newCapOwnerId == null || CollectionUtils.isEmpty(newCapOfType)) {
            return null;
        }
        return newCapOfType.stream().filter(sameNameAndOwner(oldCap.getName(), newCapOwnerId))
                                             .findFirst().orElse(null);

    }

    private Predicate<CapabilityDefinition> sameNameAndOwner(String capName, String newCapOwnerId) {
        return newCap -> newCap.getName().equals(capName) && newCap.getOwnerId().equals(newCapOwnerId);
    }



}
