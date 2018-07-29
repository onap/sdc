package org.openecomp.sdc.be.components.path;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ForwardingPathDeleteCITest {

    private Service service;
    private static final String nodeA = "nodeA";
    private static final String nodeB = "nodeB";
    private static final String fpName = "fpName";

    @Before
    public void initService() {
        service = new Service();
        ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition(fpName);
        String protocol = "protocol";
        forwardingPath.setProtocol(protocol);
        forwardingPath.setDestinationPortNumber("DestinationPortNumber");
        ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition
            = new ListDataDefinition<>();

        forwardingPathElementListDataDefinition.add(
            new ForwardingPathElementDataDefinition(nodeA, nodeB, "nodeAcpType", "nodeBcpType", "nodeDcpName",
                "nodeBcpName"));
        forwardingPathElementListDataDefinition.add(
            new ForwardingPathElementDataDefinition(nodeB, "nodeC", "nodeBcpType", "nodeCcpType", "nodeDcpName",
                "nodeBcpName"));
        forwardingPathElementListDataDefinition.add(
            new ForwardingPathElementDataDefinition("nodeC", "nodeD", "nodeCcpType", "nodeDcpType", "nodeDcpName",
                "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);
        Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>();
        forwardingPaths.put("NEW", forwardingPath);
        service.setForwardingPaths(forwardingPaths);
    }


    @Test
    public void getListToDelete() {

        Set<String> forwardingPathNamesToDeleteOnComponenetInstanceDeletion = new ForwardingPathUtils()
            .findForwardingPathNamesToDeleteOnComponentInstanceDeletion(service, nodeA);
        assertEquals(1, forwardingPathNamesToDeleteOnComponenetInstanceDeletion.size());
        assertTrue(forwardingPathNamesToDeleteOnComponenetInstanceDeletion.contains(fpName));

        Set<String> forwardingPathNamesToDeleteOnCIDelete = new ForwardingPathUtils()
            .findForwardingPathNamesToDeleteOnComponentInstanceDeletion(service, nodeB);
        assertNotNull(forwardingPathNamesToDeleteOnCIDelete);
        assertEquals(1, forwardingPathNamesToDeleteOnCIDelete.size());
        assertTrue(forwardingPathNamesToDeleteOnComponenetInstanceDeletion.contains(fpName));

        forwardingPathNamesToDeleteOnCIDelete = new ForwardingPathUtils()
            .findForwardingPathNamesToDeleteOnComponentInstanceDeletion(service, "Does not exist");
        assertNotNull(forwardingPathNamesToDeleteOnCIDelete);
        assertEquals(0, forwardingPathNamesToDeleteOnCIDelete.size());
        assertFalse(forwardingPathNamesToDeleteOnCIDelete.contains(fpName));
    }

}
