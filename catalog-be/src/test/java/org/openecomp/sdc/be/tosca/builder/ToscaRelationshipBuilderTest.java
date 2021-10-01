/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.openecomp.sdc.be.tosca.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaOperationAssignment;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyAssignment;
import org.openecomp.sdc.be.tosca.model.ToscaRelationship;
import org.openecomp.sdc.be.ui.model.OperationUi;
import org.openecomp.sdc.be.ui.model.PropertyAssignmentUi;

class ToscaRelationshipBuilderTest {

    @Test
    void testBuildFromCapabilityRequirementRelationshipSuccess() {
        final CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        final RelationshipImpl relationshipImpl = new RelationshipImpl();
        final String relationshipType = "relationshipType";
        relationshipImpl.setType(relationshipType);
        final RelationshipInfo relationshipInfo = new RelationshipInfo();
        relationshipInfo.setRelationships(relationshipImpl);
        relationship.setRelation(relationshipInfo);

        final List<OperationUi> operationList = new ArrayList<>();
        final OperationUi operationUi1 = new OperationUi();
        operationUi1.setInterfaceType("interfaceType1");
        operationUi1.setOperationType("operation1");
        operationUi1.setImplementation(new ArtifactDataDefinition());
        operationList.add(operationUi1);

        final OperationUi operationUi2 = new OperationUi();
        operationUi2.setInterfaceType("interfaceType1");
        operationUi2.setOperationType("operation2");
        operationUi2.setImplementation(new ArtifactDataDefinition());
        operationList.add(operationUi2);
        final List<PropertyAssignmentUi> operation2InputList = new ArrayList<>();
        final PropertyAssignmentUi propertyAssignmentUi1 = new PropertyAssignmentUi();
        propertyAssignmentUi1.setValue("propertyAssignmentUi1Value");
        propertyAssignmentUi1.setType("string");
        propertyAssignmentUi1.setName("propertyAssignmentUi1");
        operation2InputList.add(propertyAssignmentUi1);
        final PropertyAssignmentUi propertyAssignmentUi2 = new PropertyAssignmentUi();
        propertyAssignmentUi2.setValue("propertyAssignmentUi2Value");
        propertyAssignmentUi2.setType("string");
        propertyAssignmentUi2.setName("propertyAssignmentUi2");
        operation2InputList.add(propertyAssignmentUi2);
        operationUi2.setInputs(operation2InputList);

        final OperationUi operationUi3 = new OperationUi();
        operationUi3.setInterfaceType("interfaceType2");
        operationUi3.setOperationType("operation1");
        operationUi3.setImplementation(new ArtifactDataDefinition());
        operationList.add(operationUi3);

        relationship.setOperations(operationList);


        final ToscaRelationship toscaRelationship = new ToscaRelationshipBuilder().from(relationship);
        assertEquals(toscaRelationship.getType(), relationshipType);
        final Map<String, ToscaInterfaceDefinition> interfaceMap = toscaRelationship.getInterfaces();
        assertNotNull(interfaceMap);
        assertFalse(interfaceMap.isEmpty());
        assertEquals(2, interfaceMap.size());
        final ToscaInterfaceDefinition toscaInterfaceDefinition = interfaceMap.get(operationUi1.getInterfaceType());
        assertNull(toscaInterfaceDefinition.getType());
        assertNotNull(toscaInterfaceDefinition.getOperations());
        assertEquals(2, toscaInterfaceDefinition.getOperations().size());
        final Object actualOperation1Obj = toscaInterfaceDefinition.getOperations().get(operationUi1.getOperationType());
        assertTrue(actualOperation1Obj instanceof ToscaOperationAssignment);
        final ToscaOperationAssignment actualOperation1 = (ToscaOperationAssignment) actualOperation1Obj;
        assertOperationUi(actualOperation1, operationUi1);
    }

    private void assertOperationUi(final ToscaOperationAssignment toscaOperationAssignment, final OperationUi operationUi1) {
        if (operationUi1 == null) {
            assertNull(toscaOperationAssignment);
            return;
        }
        assertEquals(toscaOperationAssignment.getImplementation(), operationUi1.getImplementation());
        if (operationUi1.getInputs() == null) {
            assertNull(toscaOperationAssignment.getInputs());
            return;
        }
        assertEquals(toscaOperationAssignment.getInputs().size(), operationUi1.getInputs().size());
        operationUi1.getInputs().forEach(propertyAssignmentUi -> {
            final ToscaPropertyAssignment toscaPropertyAssignment = toscaOperationAssignment.getInputs()
                .get(propertyAssignmentUi.getName());
            assertNotNull(toscaPropertyAssignment);
            assertEquals(propertyAssignmentUi.getValue(), toscaPropertyAssignment.getValue());
        });
    }
}