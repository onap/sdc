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

package org.openecomp.sdc.be.tosca.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.exception.InvalidArgumentException;

class ToscaTemplateRequirementTest {

    @Test
    void testSetRelationship() {
        final ToscaTemplateRequirement toscaTemplateRequirement = new ToscaTemplateRequirement();
        toscaTemplateRequirement.setRelationship(null);
        assertNull(toscaTemplateRequirement.getRelationship());

        final String relationshipType = "aType";
        toscaTemplateRequirement.setRelationship(relationshipType);
        Object actualRelationship = toscaTemplateRequirement.getRelationship();
        assertEquals(relationshipType, actualRelationship);

        final ToscaRelationship toscaRelationship = new ToscaRelationship();
        toscaRelationship.setType(relationshipType);
        toscaTemplateRequirement.setRelationship(toscaRelationship);
        actualRelationship = toscaTemplateRequirement.getRelationship();
        assertEquals(toscaRelationship, actualRelationship);

        assertThrows(InvalidArgumentException.class, () -> toscaTemplateRequirement.setRelationship(1));
    }

    @Test
    void testIsRelationshipComplexNotation() {
        final ToscaTemplateRequirement toscaTemplateRequirement = new ToscaTemplateRequirement();
        assertFalse(toscaTemplateRequirement.isRelationshipComplexNotation());
        toscaTemplateRequirement.setRelationship("");
        assertFalse(toscaTemplateRequirement.isRelationshipComplexNotation());
        toscaTemplateRequirement.setRelationship(new ToscaRelationship());
        assertTrue(toscaTemplateRequirement.isRelationshipComplexNotation());
    }

    @Test
    void testGetRelationshipAsComplexType() {
        final ToscaTemplateRequirement toscaTemplateRequirement = new ToscaTemplateRequirement();
        ToscaRelationship actualRelationship = toscaTemplateRequirement.getRelationshipAsComplexType();
        assertNull(actualRelationship);
        final String relationshipType = "aType";
        toscaTemplateRequirement.setRelationship(relationshipType);
        actualRelationship = toscaTemplateRequirement.getRelationshipAsComplexType();
        assertEquals(relationshipType, actualRelationship.getType());

        final ToscaRelationship expectedRelationship = new ToscaRelationship();
        toscaTemplateRequirement.setRelationship(expectedRelationship);
        actualRelationship = toscaTemplateRequirement.getRelationshipAsComplexType();
        assertEquals(expectedRelationship, actualRelationship);
    }
}
