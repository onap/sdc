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

package org.openecomp.sdc.be.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.datamodel.ServiceRelations;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PathTest extends BaseServiceBusinessLogicTest {
    public static final String fromNode = "fromNode";

    @Override
    protected Service createServiceObject(boolean afterCreate) {
        Service service = super.createServiceObject(afterCreate);
        ArrayList<RequirementCapabilityRelDef> resourceInstancesRelations = new ArrayList<>();
        String toNode = "toNode";
        resourceInstancesRelations.add(createRequirementCapabilityRelDef(fromNode, toNode));
        String fromNode1 = "fromNode1";
        String toNode1 = "toNode1";
        resourceInstancesRelations.add(createRequirementCapabilityRelDef(fromNode1, toNode1));
        service.setComponentInstances(new java.util.ArrayList<>());
        service.getComponentInstances().add(getComponentInstance(fromNode, "fromNode"));
        service.getComponentInstances().add(getComponentInstance(fromNode1, "fromNode1"));
        service.getComponentInstances().add(getComponentInstance(toNode, toNode));
        service.getComponentInstances().add(getComponentInstance(toNode1, toNode1));

       service.setComponentInstancesRelations(resourceInstancesRelations);
        return service;
    }

    private ComponentInstance getComponentInstance(String uniquId, String normalizedName) {
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId(uniquId);
        ci.setNormalizedName(normalizedName);
        ci.setName(normalizedName);
        HashMap<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        CapabilityDefinition capabilityDefinition = getCapabilityDefinition(ci);
        capabilities.put(capabilityDefinition.getUniqueId(), Arrays.asList(capabilityDefinition));
        capabilityDefinition = getCapabilityDefinition(ci);
        capabilities.put(capabilityDefinition.getUniqueId(), Arrays.asList(capabilityDefinition));
        ci.setCapabilities(capabilities);
        return ci;
    }

    private static int i = 0;

    private CapabilityDefinition getCapabilityDefinition(ComponentInstance ci) {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setUniqueId(UUID.randomUUID().toString());
        capabilityDefinition.setName("My name " + i++);
        capabilityDefinition.setType(ForwardingPathUtils.FORWARDER_CAPABILITY);
        capabilityDefinition.setOwnerId(ci.getUniqueId());
        return capabilityDefinition;
    }


    protected RequirementCapabilityRelDef createRequirementCapabilityRelDef(String fromNode, String toNode) {
        RequirementCapabilityRelDef requirementCapabilityRelDef = new RequirementCapabilityRelDef();
        requirementCapabilityRelDef.setFromNode(fromNode);
        requirementCapabilityRelDef.setRelationships(new ArrayList<>());
        CapabilityRequirementRelationship capabilityRequirementRelationship = new CapabilityRequirementRelationship();
        RelationshipImpl relationship = new RelationshipImpl();
        relationship.setType("something.LINK");
        requirementCapabilityRelDef.getRelationships().add(capabilityRequirementRelationship);
        requirementCapabilityRelDef.setToNode(toNode);
        return requirementCapabilityRelDef;
    }

    @Test
    public void validateSerialization() throws IOException {
        Service service = new Service();
        ForwardingPathDataDefinition forwardingPath = new ForwardingPathDataDefinition("Name");
        String protocol = "protocol";
        forwardingPath.setProtocol(protocol);
        forwardingPath.setDestinationPortNumber("DestinationPortNumber");
        ListDataDefinition<ForwardingPathElementDataDefinition> forwardingPathElementListDataDefinition = new ListDataDefinition<>();
        String nodeA = "nodeA";
        forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition(nodeA, "nodeB", "nodeAcpType", "nodeBcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition("nodeB", "nodeC", "nodeBcpType", "nodeCcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPathElementListDataDefinition.add(new ForwardingPathElementDataDefinition("nodeC", "nodeD", "nodeCcpType", "nodeDcpType", "nodeDcpName", "nodeBcpName"));
        forwardingPath.setPathElements(forwardingPathElementListDataDefinition);
        Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>();
        forwardingPaths.put("NEW", forwardingPath);
        service.setForwardingPaths(forwardingPaths);
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(service);
        System.out.println(jsonResult);

        Either<Service, ResponseFormat> serviceResponseFormatEither = new ComponentsUtils(Mockito.mock(AuditingManager.class)).convertJsonToObjectUsingObjectMapper(jsonResult, user, Service.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
        assertTrue(serviceResponseFormatEither.isLeft());
        Map<String, ForwardingPathDataDefinition> paths = serviceResponseFormatEither.left().value().getForwardingPaths();
        assertEquals(paths.size(), 1);
        ForwardingPathDataDefinition forwardingPathDataDefinition = paths.values().stream().findAny().get();
        assertEquals(protocol, forwardingPathDataDefinition.getProtocol());
        List<ForwardingPathElementDataDefinition> listToscaDataDefinition = forwardingPathDataDefinition.getPathElements().getListToscaDataDefinition();
        assertEquals(3, listToscaDataDefinition.size());
        assertEquals(listToscaDataDefinition.get(0).getFromNode(), nodeA);
    }

    @Test
    public void shouldReturnEmptyRelationsObjectsWhenNoComponentInstanceExist() {
        ServiceRelations serviceRelations = new ForwardingPathUtils().convertServiceToServiceRelations(super.createServiceObject(false));
        assertTrue(serviceRelations.isEmpty());
    }

    @Test
    public void convertServiceToServiceRelations() {
        ServiceRelations serviceRelations = new ForwardingPathUtils().convertServiceToServiceRelations(createServiceObject(false));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String resultString = gson.toJson(serviceRelations);
        System.out.println(" RESPONSE BODY: " + resultString);
        assertTrue(resultString.contains(fromNode));
    }

    @Test
    public void emptyCapabilitesShouldNotCauseFailure() {
        Service serviceObject = createServiceObject(false);
        ComponentInstance ci = new ComponentInstance();
        ci.setCapabilities(null);
        serviceObject.getComponentInstances().add(ci);
        ServiceRelations serviceRelations = new ForwardingPathUtils().convertServiceToServiceRelations(
            serviceObject);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String resultString = gson.toJson(serviceRelations);
        System.out.println(" RESPONSE BODY: " + resultString);
        assertTrue(resultString.contains(fromNode));
    }
}
