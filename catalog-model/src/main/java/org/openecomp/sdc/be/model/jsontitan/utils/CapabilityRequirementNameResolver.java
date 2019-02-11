package org.openecomp.sdc.be.model.jsontitan.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.utils.ComponentUtilities;
import org.openecomp.sdc.common.log.wrappers.Logger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class CapabilityRequirementNameResolver {

    private static final Logger log = Logger.getLogger(CapabilityRequirementNameResolver.class);
    private static final String PATH_DELIMITER = ".";

    private CapabilityRequirementNameResolver() {
    }

    public static void updateNamesOfCalculatedCapabilitiesRequirements(TopologyTemplate toscaElement, String ownerId, String ownerName, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        Map<String, ToscaElement> componentCacheToRepair = new HashMap<>();
        log.debug("#updateNamesOfCalculatedCapabilitiesRequirements");
        updateCalculatedCapabilitiesNames(componentCacheToRepair, toscaElement, ownerId, ownerName, originGetter);
        updateCalculatedRequirementsNames(componentCacheToRepair, toscaElement, ownerId, ownerName, originGetter);
        updateCalculatedCapabilitiesPropertiesKeys(toscaElement, ownerId);
    }

    private static void updateCalculatedCapabilitiesPropertiesKeys(TopologyTemplate toscaElement, String ownerId) {
        if (calCapPropertiesExist(toscaElement, ownerId)) {
            MapCapabilityProperty newProps = new MapCapabilityProperty();
            toscaElement.getCalculatedCapabilitiesProperties().get(ownerId)
                    .getMapToscaDataDefinition()
                    .forEach((k, v) -> updateAndAddCalculatedCapabilitiesProperties(k, v, toscaElement.getCalculatedCapabilities().get(ownerId), newProps));
            if (MapUtils.isNotEmpty(newProps.getMapToscaDataDefinition())) {
                toscaElement.getCalculatedCapabilitiesProperties().put(ownerId, newProps);
            }
        }
    }

    private static boolean calCapPropertiesExist(TopologyTemplate toscaElement, String ownerId) {
        return toscaElement.getCalculatedCapabilitiesProperties() != null
                && toscaElement.getCalculatedCapabilitiesProperties().get(ownerId) != null
                && MapUtils.isNotEmpty(toscaElement.getCalculatedCapabilitiesProperties().get(ownerId).getMapToscaDataDefinition())
                && capabilitiesExist(toscaElement, ownerId);
    }

    private static void updateCalculatedRequirementsNames(Map<String, ToscaElement> componentCacheToRepair, TopologyTemplate toscaElement, String ownerId, String ownerName, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        if (requirementsExist(toscaElement, ownerId)) {
            String prefix = ownerName + PATH_DELIMITER;
            repairReqNames(componentCacheToRepair, toscaElement, ownerId, originGetter);
            toscaElement.getCalculatedRequirements().get(ownerId)
                    .getMapToscaDataDefinition().values().stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(r -> {
                        if (isRequiredToRepair(r.getName())) {
                            BeEcompErrorManager.getInstance()
                                    .logBeComponentMissingError("The empty name of the requirement was found. Id: " + r.getUniqueId() + ", ownerId: " + ownerId + ", ownerName: " + ownerName,
                                            toscaElement.getComponentType().getValue(), toscaElement.getName());
                        }
                        if (ComponentUtilities.isNotUpdatedCapReqName(prefix, r.getName(), r.getPreviousName())) {
                            if (StringUtils.isNotEmpty(r.getPreviousName())) {
                                r.setParentName(r.getPreviousName());
                            }
                            r.setPreviousName(r.getName());
                        }
                        r.setName(prefix + r.getPreviousName());
                    });
        }
    }

    private static boolean requirementsExist(TopologyTemplate toscaElement, String ownerId) {
        return toscaElement.getCalculatedRequirements() != null
                && toscaElement.getCalculatedRequirements().get(ownerId) != null
                && MapUtils.isNotEmpty(toscaElement.getCalculatedRequirements().get(ownerId).getMapToscaDataDefinition());
    }

    private static void updateCalculatedCapabilitiesNames(Map<String, ToscaElement> componentCacheToRepair, TopologyTemplate toscaElement, String ownerId, String ownerName, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        if (capabilitiesExist(toscaElement, ownerId)) {
            String prefix = ownerName + PATH_DELIMITER;
            repairCapNames(componentCacheToRepair, toscaElement, ownerId, originGetter);
            toscaElement.getCalculatedCapabilities().get(ownerId)
                    .getMapToscaDataDefinition().values().stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(c -> {
                        if (isRequiredToRepair(c.getName())) {
                            BeEcompErrorManager.getInstance()
                                    .logBeComponentMissingError("The empty name of the capability was found. Id: " + c.getUniqueId() + ", ownerId: " + ownerId + ", ownerName: " + ownerName,
                                            toscaElement.getComponentType().getValue(), toscaElement.getName());
                        }
                        if (ComponentUtilities.isNotUpdatedCapReqName(prefix, c.getName(), c.getPreviousName())) {
                            if (StringUtils.isNotEmpty(c.getPreviousName())) {
                                c.setParentName(c.getPreviousName());
                            }
                            c.setPreviousName(c.getName());
                        }
                        c.setName(prefix + c.getPreviousName());
                    });
        }
    }

    private static boolean capabilitiesExist(TopologyTemplate toscaElement, String ownerId) {
        return toscaElement.getCalculatedCapabilities() != null
                && toscaElement.getCalculatedCapabilities().get(ownerId) != null
                && MapUtils.isNotEmpty(toscaElement.getCalculatedCapabilities().get(ownerId).getMapToscaDataDefinition());
    }

    private static void repairCapNames(Map<String, ToscaElement> componentCacheToRepair, TopologyTemplate toscaElement, String ownerId, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        log.debug("#repairCapNames");
        boolean emptyNameFound = toscaElement.getCalculatedCapabilities() != null
                && toscaElement.getCalculatedCapabilities().get(ownerId) != null
                && toscaElement.getCalculatedCapabilities().get(ownerId).getMapToscaDataDefinition() != null
                && toscaElement.getCalculatedCapabilities().get(ownerId).getMapToscaDataDefinition().values()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(l -> l.getListToscaDataDefinition().stream())
                .filter(Objects::nonNull)
                .anyMatch(c -> isRequiredToRepair(c.getName()));

        ComponentInstanceDataDefinition instance = toscaElement.getComponentInstances() != null ?
                toscaElement.getComponentInstances().get(ownerId) : null;
        if (instance != null && emptyNameFound) {
            log.debug("#repairCapNames - Going to repair the name of the capability for the owner {}. ", ownerId);
            toscaElement.getCalculatedCapabilities().get(ownerId)
                    .getMapToscaDataDefinition().values()
                    .stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(c -> repairCapName(componentCacheToRepair, instance, c, originGetter));
        }
    }

    private static void repairReqNames(Map<String, ToscaElement> componentCacheToRepair, TopologyTemplate toscaElement, String ownerId, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        log.debug("#repairReqNames");
        boolean emptyNameFound = toscaElement.getCalculatedRequirements() != null
                && toscaElement.getCalculatedRequirements().get(ownerId) != null
                && toscaElement.getCalculatedRequirements().get(ownerId).getMapToscaDataDefinition() != null
                && toscaElement.getCalculatedRequirements().get(ownerId).getMapToscaDataDefinition().values()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(l -> l.getListToscaDataDefinition().stream())
                .filter(Objects::nonNull)
                .anyMatch(r -> isRequiredToRepair(r.getName()));

        ComponentInstanceDataDefinition instance = toscaElement.getComponentInstances() != null ?
                toscaElement.getComponentInstances().get(ownerId) : null;
        if (instance != null && emptyNameFound) {
            log.debug("#repairReqNames - Going to repair the name of the requirement for the owner {}. ", ownerId);
            toscaElement.getCalculatedRequirements().get(ownerId)
                    .getMapToscaDataDefinition().values()
                    .stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(r -> repairReqName(componentCacheToRepair, instance, r, originGetter));
        }
    }

    private static void repairCapName(Map<String, ToscaElement> componentCacheToRepair, ComponentInstanceDataDefinition instance, CapabilityDataDefinition capability, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        if (isRequiredToRepair(capability.getName())) {
            log.debug("#repairTopologyTemplateCapName - Going to build the name for the capability: ", capability.getUniqueId());
            buildSetCapName(componentCacheToRepair, capability, instance, originGetter);
        }
    }

    private static boolean isRequiredToRepair(String name) {
        boolean isRequiredToRepair = StringUtils.isEmpty(name) || name.endsWith(".null") || name.contains(".null.");
        if (isRequiredToRepair) {
            log.debug("#isRequiredToRepair - The name {} should be repaired. ", name);
        } else {
            log.debug("#isRequiredToRepair - The name {} should not be repaired. ", name);
        }
        return isRequiredToRepair;
    }

    private static void repairReqName(Map<String, ToscaElement> componentCacheToRepair, ComponentInstanceDataDefinition instance, RequirementDataDefinition requirement, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        if (isRequiredToRepair(requirement.getName())) {
            log.debug("#repairTopologyTemplateCapName - Going to build the name for the requirement: ", requirement.getUniqueId());
            buildSetReqName(componentCacheToRepair, requirement, instance, originGetter);
        }
    }

    private static void updateAndAddCalculatedCapabilitiesProperties(String stringKey, MapPropertiesDataDefinition properties, MapListCapabilityDataDefinition calculatedCapabilities, MapCapabilityProperty newProps) {
        String[] key = stringKey.split(ModelConverter.CAP_PROP_DELIM);
        String capType = key[key.length - 2];
        String capName = key[key.length - 1];
        Optional<CapabilityDataDefinition> foundCapOpt = calculatedCapabilities.getMapToscaDataDefinition().get(capType)
                .getListToscaDataDefinition().stream()
                .filter(c -> StringUtils.isNotEmpty(c.getPreviousName()) && c.getPreviousName().equals(capName))
                .findFirst();
        foundCapOpt.ifPresent(capabilityDataDefinition -> key[key.length - 1] = capabilityDataDefinition.getName());
        newProps.put(buildCaLCapPropKey(key), properties);
    }

    public static void revertNamesOfCalculatedCapabilitiesRequirements(TopologyTemplate toscaElement, String ownerId, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        Map<String, ToscaElement> componentCacheToRepair = new HashMap<>();
        log.debug("#revertNamesOfCalculatedCapabilitiesRequirements");
        revertCalculatedCapabilitiesPropertiesKeys(componentCacheToRepair, toscaElement, ownerId, originGetter);
        revertCalculatedCapabilitiesNames(toscaElement, ownerId);
        revertCalculatedRequirementsNames(componentCacheToRepair, toscaElement, ownerId, originGetter);
    }

    private static void revertCalculatedCapabilitiesPropertiesKeys(Map<String, ToscaElement> componentCacheToRepair, TopologyTemplate toscaElement, String ownerId, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        repairCapNames(componentCacheToRepair, toscaElement, ownerId, originGetter);
        if (calCapPropertiesExist(toscaElement, ownerId)) {
            MapCapabilityProperty newProps = new MapCapabilityProperty();
            toscaElement.getCalculatedCapabilitiesProperties().get(ownerId)
                    .getMapToscaDataDefinition()
                    .forEach((k, v) -> revertAndAddCalculatedCapabilitiesProperties(k, v, toscaElement.getCalculatedCapabilities().get(ownerId), newProps));
            if (MapUtils.isNotEmpty(newProps.getMapToscaDataDefinition())) {
                toscaElement.getCalculatedCapabilitiesProperties().put(ownerId, newProps);
            }
        }
    }

    private static void revertCalculatedRequirementsNames(Map<String, ToscaElement> componentCacheToRepair, TopologyTemplate toscaElement, String ownerId, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        repairReqNames(componentCacheToRepair, toscaElement, ownerId, originGetter);
        if (requirementsExist(toscaElement, ownerId)) {
            toscaElement.getCalculatedRequirements().get(ownerId)
                    .getMapToscaDataDefinition().values().stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(CapabilityRequirementNameResolver::revertReqNames);
        }
    }

    private static void revertReqNames(RequirementDataDefinition requirement) {
        if (StringUtils.isNotEmpty(requirement.getPreviousName())) {
            requirement.setName(requirement.getPreviousName());
            requirement.setPreviousName(requirement.getParentName());
        }
    }

    private static void revertCalculatedCapabilitiesNames(TopologyTemplate toscaElement, String ownerId) {
        if (capabilitiesExist(toscaElement, ownerId)) {
            toscaElement.getCalculatedCapabilities().get(ownerId)
                    .getMapToscaDataDefinition().values().stream()
                    .flatMap(l -> l.getListToscaDataDefinition().stream())
                    .forEach(CapabilityRequirementNameResolver::revertCapNames);
        }
    }

    private static void revertCapNames(CapabilityDataDefinition capability) {
        if (StringUtils.isNotEmpty(capability.getPreviousName())) {
            capability.setName(capability.getPreviousName());
            capability.setPreviousName(capability.getParentName());
        }
    }

    private static void revertAndAddCalculatedCapabilitiesProperties(String stringKey, MapPropertiesDataDefinition properties, MapListCapabilityDataDefinition calculatedCapabilities, MapCapabilityProperty newProps) {
        String[] key = stringKey.split(ModelConverter.CAP_PROP_DELIM);
        String capType = key[key.length - 2];
        String capName = key[key.length - 1];
        Optional<CapabilityDataDefinition> foundCapOpt = calculatedCapabilities.getMapToscaDataDefinition().get(capType)
                .getListToscaDataDefinition().stream()
                .filter(c -> c.getName().equals(capName) && StringUtils.isNotEmpty(c.getPreviousName()))
                .findFirst();
        foundCapOpt.ifPresent(capabilityDataDefinition -> key[key.length - 1] = capabilityDataDefinition.getPreviousName());
        newProps.put(buildCaLCapPropKey(key), properties);
    }

    private static String buildCaLCapPropKey(String[] keyArray) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < keyArray.length; ++i) {
            key.append(keyArray[i]);
            if (i < keyArray.length - 1) {
                key.append(ModelConverter.CAP_PROP_DELIM);
            }
        }
        return key.toString();
    }

    private static void buildSetCapName(Map<String, ToscaElement> componentsCache, CapabilityDataDefinition capability, ComponentInstanceDataDefinition instance, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        List<String> reducedPath = capability.getOwnerId() != null ? getReducedPathByOwner(capability.getPath(), capability.getOwnerId()) : getReducedPath(capability.getPath());
        log.debug("reducedPath for ownerId {}, reducedPath {} ", capability.getOwnerId(), reducedPath);
        reducedPath.remove(reducedPath.size() - 1);
        ToscaElement originComponent = getOriginComponent(componentsCache, instance, originGetter);
        String name = isRequiredToRepair(capability.getParentName()) ?
                extractNameFromUniqueId(capability.getUniqueId()) : capability.getParentName();
        StringBuilder repairedName = buildSubstitutedName(componentsCache, originComponent, reducedPath, originGetter);
        log.debug("#buildSetCapName - The name for the capability was built: {}", repairedName);

        capability.setName(repairedName.append(name).toString());
        if (isRequiredToRepair(capability.getPreviousName())) {
            capability.setPreviousName(capability.getName().substring(capability.getName().indexOf(PATH_DELIMITER) + 1));
        }
        if (isRequiredToRepair(capability.getParentName())) {
            capability.setParentName(name);
        }
    }

    private static void buildSetReqName(Map<String, ToscaElement> componentsCache, RequirementDataDefinition requirement, ComponentInstanceDataDefinition instance, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        List<String> reducedPath = requirement.getOwnerId() != null ? getReducedPathByOwner(requirement.getPath(), requirement.getOwnerId()) : getReducedPath(requirement.getPath());
        log.debug("reducedPath for ownerId {}, reducedPath {} ", requirement.getOwnerId(), reducedPath);
        reducedPath.remove(reducedPath.size() - 1);
        ToscaElement originComponent = getOriginComponent(componentsCache, instance, originGetter);
        String name = isRequiredToRepair(requirement.getParentName()) ?
                extractNameFromUniqueId(requirement.getUniqueId()) : requirement.getParentName();

        StringBuilder repairedName = buildSubstitutedName(componentsCache, originComponent, reducedPath, originGetter);
        log.debug("#buildSetReqName - The name for the capability was built: ", repairedName);
        requirement.setName(repairedName.append(name).toString());
        if (isRequiredToRepair(requirement.getPreviousName())) {
            requirement.setPreviousName(requirement.getName().substring(requirement.getName().indexOf(PATH_DELIMITER) + 1));
        }
        if (isRequiredToRepair(requirement.getParentName())) {
            requirement.setParentName(name);
        }
    }

    private static String extractNameFromUniqueId(String uniqueId) {
        String[] uid = uniqueId.split("\\.");
        return uid[uid.length - 1];
    }

    private static StringBuilder buildSubstitutedName(Map<String, ToscaElement> componentsCache, ToscaElement originComponent, List<String> path, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        StringBuilder substitutedName = new StringBuilder();
        log.debug("#buildSubstitutedName");
        if (isNotEmpty(path) && isTopologyTemplateNotCvfc(originComponent)) {
            log.debug("#buildSubstitutedName");
            List<String> reducedPath = getReducedPath(path);
            Collections.reverse(reducedPath);
            appendNameRecursively(componentsCache, originComponent, reducedPath.iterator(), substitutedName, originGetter);
        }
        return substitutedName;
    }

    private static boolean isTopologyTemplateNotCvfc(ToscaElement originComponent) {
        return originComponent.getToscaType() == ToscaElementTypeEnum.TOPOLOGY_TEMPLATE && originComponent.getResourceType() != ResourceTypeEnum.CVFC;
    }

    private static ToscaElement getOriginComponent(Map<String, ToscaElement> componentsCache, ComponentInstanceDataDefinition instance, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        if (componentsCache.containsKey(getActualComponentUid(instance))) {
            return componentsCache.get(getActualComponentUid(instance));
        }
        ToscaElement origin = originGetter.apply(instance);
        componentsCache.put(origin.getUniqueId(), origin);
        return origin;
    }

    public static String getActualComponentUid(ComponentInstanceDataDefinition instance) {
        return instance.getIsProxy() ? instance.getSourceModelUid() : instance.getComponentUid();
    }

    private static List<String> getReducedPath(List<String> path) {
        return path.stream().distinct().collect(Collectors.toList());
    }

    private static void appendNameRecursively(Map<String, ToscaElement> componentsCache, ToscaElement originComponent, Iterator<String> instanceIdIter, StringBuilder substitutedName, Function<ComponentInstanceDataDefinition, ToscaElement> originGetter) {
        log.debug("#appendNameRecursively");
        if (isTopologyTemplateNotCvfc(originComponent)
                && MapUtils.isNotEmpty(((TopologyTemplate) originComponent).getComponentInstances()) && instanceIdIter.hasNext()) {

            String ownerId = instanceIdIter.next();
            Optional<ComponentInstanceDataDefinition> instanceOpt = ((TopologyTemplate) originComponent).getComponentInstances().values().stream().filter(i -> i.getUniqueId().equals(ownerId)).findFirst();
            if (instanceOpt.isPresent()) {
                substitutedName.append(instanceOpt.get().getNormalizedName()).append(PATH_DELIMITER);
                ToscaElement getOriginRes = getOriginComponent(componentsCache, instanceOpt.get(), originGetter);
                appendNameRecursively(componentsCache, getOriginRes, instanceIdIter, substitutedName, originGetter);
            } else if (MapUtils.isNotEmpty(((TopologyTemplate) originComponent).getGroups())) {
                Optional<GroupDataDefinition> groupOpt = ((TopologyTemplate) originComponent).getGroups().values().stream().filter(g -> g.getUniqueId().equals(ownerId)).findFirst();
                groupOpt.ifPresent(groupDataDefinition -> substitutedName.append(groupDataDefinition.getName()).append(PATH_DELIMITER));
            } else {
                log.debug("Failed to find an capability owner with uniqueId {} on a component with uniqueId {}", ownerId, originComponent.getUniqueId());
            }
        }
    }

    private static List<String> getReducedPathByOwner(List<String> path, String ownerId) {
        log.debug("ownerId {}, path {} ", ownerId, path);
        if (CollectionUtils.isEmpty(path)) {
            log.debug("cannot perform reduce by owner, path to component is empty");
            return path;
        }
        if (isBlank(ownerId)) {
            log.debug("cannot perform reduce by owner, component owner is empty");
            return path;
        }
        //reduce by owner
        Map map = path.stream().collect(Collectors.toMap(it -> dropLast(it, PATH_DELIMITER), Function.identity(), (a, b) -> a.endsWith(ownerId) ? a : b));
        //reduce list&duplicates and preserve order
        return path.stream().distinct().filter(it -> map.values().contains(it)).collect(Collectors.toList());
    }

    private static String dropLast(String path, String delimiter) {
        if (isBlank(path) || isBlank(delimiter)) {
            return path;
        }
        return path.substring(0, path.lastIndexOf(delimiter));
    }
}
