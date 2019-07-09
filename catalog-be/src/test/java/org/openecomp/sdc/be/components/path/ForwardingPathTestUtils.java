/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
