package org.openecomp.sdc.be.components.merge.capability;

import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.List;
import java.util.Map;

public interface CapabilityResolver {

    /**
     *
     * @param container the instance container
     * @param prevInstanceOrigNode the prev instance's original node type
     * @param cmptInstanceId the current instance id
     * @param prevCapabilities list of previous capabilities for which to find their corresponding new capabilities
     * @return a mapping between the prev capability to its corresponding new capability (if exists)
     */
    Map<CapabilityDefinition, CapabilityDefinition> resolvePrevCapToNewCapability(Component container, Component prevInstanceOrigNode, String cmptInstanceId, List<CapabilityDefinition> prevCapabilities);

    /**
     *
     * @param oldInstance the old instance of which its capabilities are to be mapped as the key
     * @param currInstance the curr instance of which its capabilities are to be mapped as the value
     * @return a mapping between the prev capability to its corresponding new capability (if exists)
     */
    Map<CapabilityDefinition, CapabilityDefinition> resolvePrevCapIdToNewCapability(ComponentInstance oldInstance, ComponentInstance currInstance);
}
