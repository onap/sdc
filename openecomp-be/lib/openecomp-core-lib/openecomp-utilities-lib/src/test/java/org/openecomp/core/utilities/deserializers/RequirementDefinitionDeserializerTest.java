/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.core.utilities.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;

import static org.junit.Assert.assertEquals;
import static org.openecomp.core.utilities.deserializers.RequirementDefinitionDeserializer.CAPABILITY;
import static org.openecomp.core.utilities.deserializers.RequirementDefinitionDeserializer.NODE;
import static org.openecomp.core.utilities.deserializers.RequirementDefinitionDeserializer.RELATIONSHIP;

public class RequirementDefinitionDeserializerTest {

    @Test
    public void deserializeTest() {
        RequirementDefinitionDeserializer deserializer = new RequirementDefinitionDeserializer();

        String jsonString = new StringBuilder()
                .append("{\n")
                .append("  \"occurrences\": [\n")
                .append("    1,\n")
                .append("    2,\n")
                .append("  \"3\"\n")
                .append("  ],\n")
                .append("  \"capability\": \"").append(CAPABILITY).append("\",\n")
                .append("  \"relationship\": \"").append(RELATIONSHIP).append("\",\n")
                .append("  \"node\": \"").append(NODE).append("\"\n")
                .append("}")
                .toString();
        JsonElement jsonElement = new JsonParser().parse(jsonString);

        RequirementDefinition requirement =
                deserializer.deserialize(jsonElement, null, null);

        assertEquals(requirement.getOccurrences()[0], 1);
        assertEquals(requirement.getOccurrences()[1], 2);
        assertEquals(requirement.getOccurrences()[2], "3");
        assertEquals(requirement.getCapability(), CAPABILITY);
        assertEquals(requirement.getRelationship(), RELATIONSHIP);
        assertEquals(requirement.getNode(), NODE);
    }
}
