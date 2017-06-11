package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.migration.MigrationMalformedDataLogger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InvariantUUIDResolver <T extends Component> {

    public String resolveInvariantUUID(List<T> components, T missingInvariantCmpt) {
        String uuid = missingInvariantCmpt.getUUID();
        String systemName = missingInvariantCmpt.getSystemName();
        String invariantUid = findInvariantUUidByAllVersionsMap(missingInvariantCmpt, components).orElseGet(() -> findInvariantUUidByUUIDOrSystemName(components, uuid, systemName));
        if (invariantUid == null) {
            MigrationMalformedDataLogger.logMalformedDataMsg(String.format("could not find invariant uuid for component %s with id %s", missingInvariantCmpt.getName(), missingInvariantCmpt.getUniqueId()));
        }
        return invariantUid;
    }

    private String findInvariantUUidByUUIDOrSystemName(List<T> components, String uuid, String systemName) {
        return components.stream()
                .filter(c -> c.getUUID().equals(uuid) || c.getSystemName().equals(systemName))
                .map(Component::getInvariantUUID)
                .filter(c -> c != null)
                .findAny().orElse(null);
    }

    private Optional<String> findInvariantUUidByAllVersionsMap(T component, List<T> allComponents) {
        if (component.getAllVersions() == null) return Optional.empty();
        Collection<String> allVersionsComponentIds = component.getAllVersions().values();
        return allComponents.stream().filter(c -> allVersionsComponentIds.contains(c.getUniqueId()))
                .map(Component::getInvariantUUID)
                .filter(c -> c != null)
                .findAny();


    }

}
