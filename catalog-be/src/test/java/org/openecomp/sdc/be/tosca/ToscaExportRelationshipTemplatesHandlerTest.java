/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.tosca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaRelationship;
import org.openecomp.sdc.be.tosca.model.ToscaRelationshipTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;

class ToscaExportRelationshipTemplatesHandlerTest {

    @Test
    void testCreateFromEmptyNodeTemplateMapReturnsEmptyMap() {
        final Map<String, ToscaRelationshipTemplate> actualRelationshipTemplateMap =
            new ToscaExportRelationshipTemplatesHandler().createFrom(Collections.emptyMap());
        assertNotNull(actualRelationshipTemplateMap);
        assertTrue(actualRelationshipTemplateMap.isEmpty());
    }

    @Test
    void testCreateFromSuccess() {
        final Map<String, ToscaNodeTemplate> nodeTemplateMap = new HashMap<>();

        final ToscaNodeTemplate nodeTemplateWithNoRequirements = new ToscaNodeTemplate();
        nodeTemplateMap.put("nodeTemplateWithNoRequirements", nodeTemplateWithNoRequirements);

        final ToscaNodeTemplate nodeTemplateWithRequirements = new ToscaNodeTemplate();
        final List<Map<String, ToscaTemplateRequirement>> requirements = new ArrayList<>();

        final Map<String, ToscaTemplateRequirement> requirementMap = new HashMap<>();
        final ToscaTemplateRequirement complexRequirement = new ToscaTemplateRequirement();
        complexRequirement.setNode("aNode");

        final ToscaRelationship toscaRelationship = new ToscaRelationship();
        final String relationshipType = "tosca.relationships.ConnectsTo";
        toscaRelationship.setType(relationshipType);

        final Map<String, ToscaInterfaceDefinition> interfaces = new HashMap<>();
        final ToscaInterfaceDefinition toscaInterfaceDefinition = new ToscaInterfaceDefinition();
        final String interfaceConfigureType = "tosca.interfaces.relationship.Configure";
        toscaInterfaceDefinition.setType(interfaceConfigureType);
        final HashMap<String, Object> operationMap = new HashMap<>();
        final String preConfigSourceOperationType = "pre_configure_source";
        operationMap.put(preConfigSourceOperationType, new Object());
        toscaInterfaceDefinition.setOperations(operationMap);

        interfaces.put(interfaceConfigureType, toscaInterfaceDefinition);
        toscaRelationship.setInterfaces(interfaces);
        complexRequirement.setRelationship(toscaRelationship);
        requirementMap.put("requirement1", complexRequirement);

        final ToscaTemplateRequirement simpleRequirement = new ToscaTemplateRequirement();
        simpleRequirement.setNode("anotherNode");
        simpleRequirement.setRelationship("aRelationship");
        requirementMap.put("requirement2", simpleRequirement);

        requirements.add(requirementMap);
        nodeTemplateWithRequirements.setRequirements(requirements);
        nodeTemplateMap.put("nodeTemplateWithRequirements", nodeTemplateWithRequirements);

        final Map<String, ToscaRelationshipTemplate> actualRelationshipTemplateMap =
            new ToscaExportRelationshipTemplatesHandler().createFrom(nodeTemplateMap);

        assertNotNull(actualRelationshipTemplateMap);
        assertEquals(1, actualRelationshipTemplateMap.size());
        final ToscaRelationshipTemplate actualRelationshipTemplate = actualRelationshipTemplateMap.values().iterator().next();
        assertEquals(relationshipType, actualRelationshipTemplate.getType());

        final Map<String, ToscaInterfaceDefinition> actualInterfaceMap = actualRelationshipTemplate.getInterfaces();
        assertNotNull(actualInterfaceMap);
        assertEquals(1, actualInterfaceMap.size());
        assertTrue(actualInterfaceMap.containsKey(interfaceConfigureType));

        final ToscaInterfaceDefinition actualToscaInterfaceDefinition =
            actualInterfaceMap.get(interfaceConfigureType);
        assertEquals(toscaInterfaceDefinition.getType(), actualToscaInterfaceDefinition.getType());

        final Map<String, Object> actualOperationMap = actualToscaInterfaceDefinition.getOperations();
        assertNotNull(actualOperationMap);
        assertEquals(1, actualOperationMap.size());
        assertTrue(actualOperationMap.containsKey(preConfigSourceOperationType));
    }
}