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

package org.openecomp.sdc.be.servlets;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Operation;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RepresentationUtilsTest  {

    private RepresentationUtils createTestSubject() {
        return new RepresentationUtils();
    }


    @Test
    public void testConvertJsonToArtifactDefinitionForUpdate() throws Exception {
        String content = "";
        Class<ArtifactDefinition> clazz = null;
        ArtifactDefinition result;

        // default test
        result = RepresentationUtils.convertJsonToArtifactDefinitionForUpdate(content, clazz);
    }


    @Test
    public void testToRepresentation() throws Exception {
        T elementToRepresent = null;
        Object result;

        // default test
        result = RepresentationUtils.toRepresentation(elementToRepresent);
    }

    @Test
    public void checkIsEmptyFiltering() throws Exception {
        HashMap<String, Operation> op = new HashMap<>();
        Operation opValue = new Operation();
        opValue.setName("eee");
        opValue.setDescription("ccc");
        op.put("Bla", opValue);
        Object result = RepresentationUtils.toRepresentation(op);
        assertNotNull(result);
        assertTrue(result.toString(), result.toString().contains("empty"));
        result = RepresentationUtils.toFilteredRepresentation(op);
        assertNotNull(result);
        assertFalse(result.toString(), result.toString().contains("empty"));
    }
}
