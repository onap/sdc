package org.openecomp.core.tools.Commands.exportinfo.serialize;

import org.openecomp.core.tools.exportinfo.ExportSerializer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
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

    @Test
    public void failToExtractVLMBecauseJsonIsCorrupted(){
        String elemenet_info_string = "gfhhhghgh";
        String extractedVlmId = new CustomExportSerializer().extractVlm(elemenet_info_string);
        assertNull(extractedVlmId);
    }

    private static final class CustomExportSerializer extends ExportSerializer{
        public String extractVlm(String injson) {
            return super.extractVlm(injson);
        }
    }
}
