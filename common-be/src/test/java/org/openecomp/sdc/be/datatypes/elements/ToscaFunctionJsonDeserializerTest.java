/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

class ToscaFunctionJsonDeserializerTest {

    @Test
    void testGetInputToscaFunction() throws JsonProcessingException {
        final String toscaGetInputFunction = "{\n"
            + "            \"propertyUniqueId\": \"e57525d7-2115-4934-9ba4-9cebfa22bad2.nf_naming\",\n"
            + "            \"type\": \"GET_INPUT\",\n"
            + "            \"propertySource\": \"SELF\",\n"
            + "            \"propertyName\": \"instance_name\",\n"
            + "            \"sourceName\": \"ciResVFc26a0b30ec20\",\n"
            + "            \"sourceUniqueId\": \"aee643c9-6c8e-4a24-af7a-a9aff5c072c0\",\n"
            + "            \"propertyPathFromSource\": [\n"
            + "                \"nf_naming\",\n"
            + "                \"instance_name\"\n"
            + "            ]\n"
            + "        }";
        ToscaFunction toscaFunction = parseToscaFunction(toscaGetInputFunction);
        assertTrue(toscaFunction instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) toscaFunction;
        assertEquals(ToscaFunctionType.GET_INPUT, toscaGetFunction.getType());
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunction.getFunctionType());
        assertEquals("e57525d7-2115-4934-9ba4-9cebfa22bad2.nf_naming", toscaGetFunction.getPropertyUniqueId());
        assertEquals(PropertySource.SELF, toscaGetFunction.getPropertySource());
        assertEquals("instance_name", toscaGetFunction.getPropertyName());
        assertEquals("ciResVFc26a0b30ec20", toscaGetFunction.getSourceName());
        assertEquals("aee643c9-6c8e-4a24-af7a-a9aff5c072c0", toscaGetFunction.getSourceUniqueId());
        assertEquals(List.of("nf_naming", "instance_name"), toscaGetFunction.getPropertyPathFromSource());
    }

    @Test
    void testGetInputToscaFunctionLegacyConversion() throws JsonProcessingException {
        final String toscaGetInputFunction = "{\n"
            + "            \"propertyUniqueId\": \"e57525d7-2115-4934-9ba4-9cebfa22bad2.nf_naming\",\n"
            + "            \"functionType\": \"GET_INPUT\",\n"
            + "            \"propertySource\": \"SELF\",\n"
            + "            \"propertyName\": \"instance_name\",\n"
            + "            \"sourceName\": \"ciResVFc26a0b30ec20\",\n"
            + "            \"sourceUniqueId\": \"aee643c9-6c8e-4a24-af7a-a9aff5c072c0\",\n"
            + "            \"propertyPathFromSource\": [\n"
            + "                \"nf_naming\",\n"
            + "                \"instance_name\"\n"
            + "            ]\n"
            + "        }";
        ToscaFunction toscaFunction = parseToscaFunction(toscaGetInputFunction);
        assertTrue(toscaFunction instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) toscaFunction;
        assertEquals(ToscaFunctionType.GET_INPUT, toscaGetFunction.getType());
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunction.getFunctionType());
    }

    @Test
    void testNoFunctionTypeProvided() {
        final String toscaGetInputFunction = "{\n"
            + "            \"propertyUniqueId\": \"e57525d7-2115-4934-9ba4-9cebfa22bad2.nf_naming\",\n"
            + "            \"propertySource\": \"SELF\",\n"
            + "            \"propertyName\": \"instance_name\",\n"
            + "            \"sourceName\": \"ciResVFc26a0b30ec20\",\n"
            + "            \"sourceUniqueId\": \"aee643c9-6c8e-4a24-af7a-a9aff5c072c0\",\n"
            + "            \"propertyPathFromSource\": [\n"
            + "                \"nf_naming\",\n"
            + "                \"instance_name\"\n"
            + "            ]\n"
            + "        }";
        final ValueInstantiationException actualException =
            assertThrows(ValueInstantiationException.class, () -> parseToscaFunction(toscaGetInputFunction));
        assertTrue(actualException.getMessage().contains("Attribute type not provided"));
    }

    @Test
    void testConcatToscaFunction() throws JsonProcessingException {
        final String toscaGetInputFunction = "{\n"
            + "  \"type\": \"CONCAT\",\n"
            + "  \"parameters\": [\n"
            + "    {\n"
            + "      \"propertyUniqueId\": \"e57525d7-2115-4934-9ba4-9cebfa22bad2.nf_naming\",\n"
            + "      \"type\": \"GET_INPUT\",\n"
            + "      \"propertySource\": \"SELF\",\n"
            + "      \"propertyName\": \"instance_name\",\n"
            + "      \"sourceName\": \"ciResVFc26a0b30ec20\",\n"
            + "      \"sourceUniqueId\": \"aee643c9-6c8e-4a24-af7a-a9aff5c072c0\",\n"
            + "      \"propertyPathFromSource\": [\n"
            + "        \"nf_naming\",\n"
            + "        \"instance_name\"\n"
            + "      ]\n"
            + "    }, {\n"
            + "      \"type\": \"STRING\",\n"
            + "      \"value\": \"my string\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"type\": \"CONCAT\",\n"
            + "      \"parameters\": [\n"
            + "        {\n"
            + "          \"type\": \"STRING\",\n"
            + "          \"value\": \"string1\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"type\": \"STRING\",\n"
            + "          \"value\": \"string2\"\n"
            + "        }\n"
            + "      ]\n"
            + "    }\n"
            + "  ]\n"
            + "}";
        ToscaFunction toscaFunction = parseToscaFunction(toscaGetInputFunction);
        assertTrue(toscaFunction instanceof ToscaConcatFunction);
    }

    @Test
    void testYamlFunction() throws JsonProcessingException {
        String yamlFunction = "{\n"
            + "  \"type\": \"YAML\",\n"
            + "  \"value\": \"tosca_definitions_version: tosca_simple_yaml_1_0_0\\nnode_types: \\n  tosca.nodes.Compute:\\n    derived_from: tosca.nodes.Root\\n    attributes:\\n      private_address:\\n        type: string\\n      public_address:\\n        type: string\\n      networks:\\n        type: map\\n        entry_schema:\\n          type: tosca.datatypes.network.NetworkInfo\\n      ports:\\n        type: map\\n        entry_schema:\\n          type: tosca.datatypes.network.PortInfo\\n    requirements:\\n      - local_storage: \\n          capability: tosca.capabilities.Attachment\\n          node: tosca.nodes.BlockStorage\\n          relationship: tosca.relationships.AttachesTo\\n          occurrences: [0, UNBOUNDED]  \\n    capabilities:\\n      host: \\n        type: tosca.capabilities.Container\\n        valid_source_types: [tosca.nodes.SoftwareComponent] \\n      endpoint :\\n        type: tosca.capabilities.Endpoint.Admin \\n      os: \\n        type: tosca.capabilities.OperatingSystem\\n      scalable:\\n        type: tosca.capabilities.Scalable\\n      binding:\\n        type: tosca.capabilities.network.Bindable\\n\"\n"
            + "}";
        ToscaFunction toscaFunction = parseToscaFunction(yamlFunction);
        assertTrue(toscaFunction instanceof CustomYamlFunction);
    }

    private ToscaFunction parseToscaFunction(final String toscaFunctionJson) throws JsonProcessingException {
        return new ObjectMapper().readValue(toscaFunctionJson, ToscaFunction.class);
    }
}