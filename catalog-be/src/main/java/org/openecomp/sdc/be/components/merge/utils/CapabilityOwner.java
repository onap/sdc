package org.openecomp.sdc.be.components.merge.utils;

import org.openecomp.sdc.be.model.CapabilityDefinition;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class CapabilityOwner {

    private String uniqueId;
    private String name;
    private Map<String, List<CapabilityDefinition>> capabilities;

    public CapabilityOwner(String uniqueId, String name, Map<String, List<CapabilityDefinition>> capabilities) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.capabilities = capabilities;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public Map<String, List<CapabilityDefinition>> getCapabilities() {
        return capabilities == null ? emptyMap() : capabilities;
    }
}
