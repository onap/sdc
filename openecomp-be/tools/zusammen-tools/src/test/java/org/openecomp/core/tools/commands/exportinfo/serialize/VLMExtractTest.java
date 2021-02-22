/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.tools.commands.exportinfo.serialize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.Test;
import org.openecomp.core.tools.exportinfo.ExportSerializer;
public class VLMExtractTest {

    @Test
    public void extractVLM(){
        String vlmId = "979a56c7b2fa41e6a5742137f53a5c1b";
        String elemenet_info_string = "{\n" +
                "  \"name\": \"VendorSoftwareProduct\",\n" +
                "  \"properties\": {\n" +
                "    \"subCategory\": \"resourceNewCategory.dcae component.collector\",\n" +
                "    \"name\": \"vsp1\",\n" +
                "    \"icon\": \"icon\",\n" +
                "    \"onboardingMethod\": \"NetworkPackage\",\n" +
                "    \"description\": \"d\",\n" +
                "    \"vendorId\": \""+vlmId+"\",\n" +
                "    \"category\": \"resourceNewCategory.dcae component\",\n" +
                "    \"vendorName\": \"vlm1\",\n" +
                "    \"elementType\": \"VendorSoftwareProduct\"\n" +
                "  }\n" +
                "}\n"    ;
        String extractedVlmId = new CustomExportSerializer().extractVlm(elemenet_info_string);
        assertNotNull(extractedVlmId);
        assertEquals(extractedVlmId,vlmId);

    }

    @Test(expected = IllegalStateException.class)
    public void failToExtractVLMBecauseJsonIsCorrupted(){
        String elemenet_info_string = "gfhhhghgh";
        assertNull(new CustomExportSerializer().extractVlm(elemenet_info_string));
    }

    private static final class CustomExportSerializer extends ExportSerializer{
        public String extractVlm(String inJson) {
            return super.extractVlm(inJson);
        }
    }
}
