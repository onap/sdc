package org.openecomp.sdc.be.components.path;

import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;

public interface ForwardingPathTestUtils {

    default ForwardingPathDataDefinition createPath(String pathName, String protocol, String ports, String uniqueId){
        ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition(pathName);
        forwardingPath.setProtocol(protocol);
        forwardingPath.setDestinationPortNumber(ports);
        forwardingPath.setUniqueId(uniqueId);
        ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition = new ListDataDefinition<>();
        String nodeA = "nodeA";
        forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition(nodeA, "nodeB", "nodeAcpType", "nodeBcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition("nodeB", "nodeC", "nodeBcpType", "nodeCcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition("nodeC", "nodeD", "nodeCcpType", "nodeDcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);

        return forwardingPath;
    }
}
