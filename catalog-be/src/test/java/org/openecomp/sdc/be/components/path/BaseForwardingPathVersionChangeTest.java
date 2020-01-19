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

import com.google.common.collect.Lists;
import org.junit.Before;
import org.openecomp.sdc.be.components.merge.instance.DataForMergeHolder;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseForwardingPathVersionChangeTest {
    protected Service service;
    protected ComponentInstance nodeACI;
    protected ComponentInstance newNodeACI;
    protected Component newNodeAC;
    protected Component newNodeWithoutCapability;
    protected DataForMergeHolder dataHolder;
    protected static final String nodeA = "nodeA";
    protected static final String NODE_A_FORWARDER_CAPABILITY = "nodeA_FORWARDER_CAPABILITY";
    protected static final String nodeB = "nodeB";
    protected static final String newNodeA = "newNodeA";
    protected static final String fpName = "fpName";
    protected static final String FPId = "1122";


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
            new ForwardingPathElementDataDefinition(nodeA, nodeB, NODE_A_FORWARDER_CAPABILITY, "nodeBcpType" , "nodeDcpName",
                "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);
        Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>();
        forwardingPaths.put(FPId, forwardingPath);
        service.setForwardingPaths(forwardingPaths);
        nodeACI = new ComponentInstance();
        initComponentInstance(nodeACI, nodeA);
        newNodeACI = new ComponentInstance();
        initComponentInstance(newNodeACI, newNodeA);
        newNodeAC = new Resource();
        newNodeWithoutCapability=new Resource();
        initComponent(newNodeAC, newNodeA);
        service.setComponentInstances(Lists.newArrayList(newNodeACI));
        initComponentWithoutForwarder(newNodeWithoutCapability,"newNodeC");
        service.setComponentInstances(Lists.newArrayList(nodeACI));

        dataHolder = new DataForMergeHolder();
        dataHolder.setOrigComponentInstId(nodeA);
    }

    private void initComponent(Component component, String uniqueId) {
        component.setUniqueId(uniqueId);
        HashMap<String, List<CapabilityDefinition>> capabilities = initCapabilites();
        component.setCapabilities(capabilities);
    }

    private void initComponentWithoutForwarder(Component component, String uniqueId) {
        component.setUniqueId(uniqueId);
        HashMap<String, List<CapabilityDefinition>> capabilities = initCapabilitesWithoutForwarder();
        component.setCapabilities(capabilities);
    }

    private HashMap<String, List<CapabilityDefinition>> initCapabilites() {
        HashMap<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        CapabilityDefinition forwarder = new CapabilityDefinition();
        forwarder.setType(ForwardingPathUtils.FORWARDER_CAPABILITY);
        forwarder.setUniqueId(NODE_A_FORWARDER_CAPABILITY);
        forwarder.setName(NODE_A_FORWARDER_CAPABILITY);
        capabilities.put("bla bla", Arrays.asList(forwarder));
        return capabilities;
    }

    private HashMap<String, List<CapabilityDefinition>> initCapabilitesWithoutForwarder() {
        HashMap<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setType("tosca.capabilities.Node");
        capabilityDefinition.setUniqueId("tosca capability");
        capabilityDefinition.setName("tosca capability");
        capabilities.put("bla bla", Arrays.asList(capabilityDefinition));
        return capabilities;
    }

    private void initComponentInstance(ComponentInstance component, String uniqueId) {
        component.setUniqueId(uniqueId);
        component.setName(uniqueId);
        HashMap<String, List<CapabilityDefinition>> capabilities = initCapabilites();
        component.setCapabilities(capabilities);
    }

}
