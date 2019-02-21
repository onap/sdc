/*
 * Copyright © 2019 iconectiv
 *
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
 */

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class TestSetTests {

  @Test
  public void testResponse() throws Exception {

    String json="{\n" +
        "  \"id\": \"all\",\n" +
        "  \"title\": \"All\",\n" +
        "  \"sets\": [\n" +
        "    {\n" +
        "      \"id\": \"certification\",\n" +
        "      \"title\": \"Certification Query\",\n" +
        "      \"tests\": [\n" +
        "        {\n" +
        "          \"id\": \"certquery\",\n" +
        "          \"title\": \"VSP Certifications\",\n" +
        "          \"parameters\": [\n" +
        "            {\n" +
        "              \"id\": \"vspId\",\n" +
        "              \"label\": \"VSP ID\",\n" +
        "              \"inputType\": \"text\",\n" +
        "              \"maxLength\": 36,\n" +
        "              \"minLength\": 1,\n" +
        "              \"disabled\": false,\n" +
        "              \"required\": true\n" +
        "            },\n" +
        "            {\n" +
        "              \"id\": \"vspVersion\",\n" +
        "              \"label\": \"Previous VSP Version\",\n" +
        "              \"inputType\": \"text\",\n" +
        "              \"maxLength\": 36,\n" +
        "              \"minLength\": 1,\n" +
        "              \"disabled\": false,\n" +
        "              \"required\": true\n" +
        "            }\n" +
        "          ]\n" +
        "        }\n" +
        "      ]\n" +
        "    },\n" +
        "    {\n" +
        "      \"id\": \"compliance\",\n" +
        "      \"title\": \"Compliance Checks\",\n" +
        "      \"sets\": [\n" +
        "        {\n" +
        "          \"id\": \"compliancetests\",\n" +
        "          \"title\": \"Compliance Tests\",\n" +
        "          \"sets\": [\n" +
        "            {\n" +
        "              \"id\": \"heattests\",\n" +
        "              \"title\": \"Heat Tests\",\n" +
        "              \"tests\": [\n" +
        "                {\n" +
        "                  \"id\": \"fht1\",\n" +
        "                  \"title\": \"Future Tests here\",\n" +
        "                  \"parameters\": []\n" +
        "                }\n" +
        "              ]\n" +
        "            },\n" +
        "            {\n" +
        "              \"id\": \"toscatests\",\n" +
        "              \"title\": \"TOSCA Tests\",\n" +
        "              \"tests\": [\n" +
        "                {\n" +
        "                  \"id\": \"ftt1\",\n" +
        "                  \"title\": \"Future Tests here\",\n" +
        "                  \"parameters\": []\n" +
        "                }\n" +
        "              ]\n" +
        "            }\n" +
        "          ],\n" +
        "          \"tests\": [\n" +
        "            {\n" +
        "              \"id\": \"computeflavor\",\n" +
        "              \"title\": \"Compute Flavours Test\",\n" +
        "              \"parameters\": [\n" +
        "                {\n" +
        "                  \"id\": \"something\",\n" +
        "                  \"label\": \"Something\",\n" +
        "                  \"inputType\": \"select\",\n" +
        "                  \"placeholder\": \"B/N/H\",\n" +
        "                  \"disabled\": false,\n" +
        "                  \"choices\": [\n" +
        "                    \"B\",\n" +
        "                    \"N\",\n" +
        "                    \"H\"\n" +
        "                  ],\n" +
        "                  \"required\": true\n" +
        "                },\n" +
        "                {\n" +
        "                  \"id\": \"vspId\",\n" +
        "                  \"label\": \"Id of VSP\",\n" +
        "                  \"inputType\": \"text\",\n" +
        "                  \"placeholder\": \"VSP ID\",\n" +
        "                  \"disabled\": false,\n" +
        "                  \"required\": true\n" +
        "                },\n" +
        "                {\n" +
        "                  \"id\": \"vspVersion\",\n" +
        "                  \"label\": \"version of VSP\",\n" +
        "                  \"inputType\": \"text\",\n" +
        "                  \"placeholder\": \"VSP Version\",\n" +
        "                  \"disabled\": false,\n" +
        "                  \"required\": true\n" +
        "                },\n" +
        "                {\n" +
        "                  \"id\": \"csar:MainServiceTemplate.yaml\",\n" +
        "                  \"inputType\": \"hidden\",\n" +
        "                  \"disabled\": false,\n" +
        "                  \"required\": true\n" +
        "                }\n" +
        "              ]\n" +
        "            },\n" +
        "            {\n" +
        "              \"id\": \"sriov\",\n" +
        "              \"title\": \"SR-IOV Test\",\n" +
        "              \"parameters\": [\n" +
        "                {\n" +
        "                  \"id\": \"allowSrIov\",\n" +
        "                  \"label\": \"SR-IOV Test\",\n" +
        "                  \"inputType\": \"select\",\n" +
        "                  \"placeholder\": \"No\",\n" +
        "                  \"defaultValue\": \"No\",\n" +
        "                  \"disabled\": true,\n" +
        "                  \"choices\": [\n" +
        "                    \"Yes\",\n" +
        "                    \"No\"\n" +
        "                  ],\n" +
        "                  \"required\": true\n" +
        "                },\n" +
        "                {\n" +
        "                  \"id\": \"vnfId\",\n" +
        "                  \"label\": \"Id of VNF\",\n" +
        "                  \"inputType\": \"text\",\n" +
        "                  \"placeholder\": \"VNF ID\",\n" +
        "                  \"disabled\": false,\n" +
        "                  \"required\": true\n" +
        "                }\n" +
        "              ]\n" +
        "            }\n" +
        "          ]\n" +
        "        },\n" +
        "        {\n" +
        "          \"id\": \"validationtests\",\n" +
        "          \"title\": \"Validation Tests\",\n" +
        "          \"tests\": [\n" +
        "            {\n" +
        "              \"id\": \"fvt1\",\n" +
        "              \"title\": \"Future Tests here\",\n" +
        "              \"parameters\": []\n" +
        "            }\n" +
        "          ]\n" +
        "        },\n" +
        "        {\n" +
        "          \"id\": \"performancetests\",\n" +
        "          \"title\": \"Performance Tests\",\n" +
        "          \"tests\": [\n" +
        "            {\n" +
        "              \"id\": \"fvt1\",\n" +
        "              \"title\": \"Future Tests here\",\n" +
        "              \"parameters\": []\n" +
        "            }\n" +
        "          ]\n" +
        "        }\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}";


    TestSet testSet = new ObjectMapper().readValue(json, TestSet.class);
    Assert.assertEquals("root id should be all", "all", testSet.getId());
    Assert.assertEquals("root title should be All", "All", testSet.getTitle());
    Assert.assertEquals("All should contain the right number of children", 2, testSet.getSets().size());
    Assert.assertNull("All should contain no tests", testSet.getTests());

    TestSet subSet = testSet.getSets().get(0);
    Assert.assertEquals("First sub set id", "certification", subSet.getId());
    Assert.assertEquals("First sub set should have 1 test", 1, subSet.getTests().size());

    org.openecomp.core.externaltesting.api.Test firstTest = subSet.getTests().get(0);
    Assert.assertEquals("First test id should match", "certquery", firstTest.getId());
    Assert.assertEquals("First test title should match", "VSP Certifications", firstTest.getTitle());
    Assert.assertNotNull("Parameters should be non null", firstTest.getParameters());

    /*
           "              \"id\": \"vspId\",\n" +
        "              \"label\": \"VSP ID\",\n" +
        "              \"inputType\": \"text\",\n" +
        "              \"maxLength\": 36,\n" +
        "              \"minLength\": 1,\n" +
        "              \"disabled\": false,\n" +
        "              \"required\": true\n" +
     */
    TestParameter tp = firstTest.getParameters().get(0);
    Assert.assertEquals("first param id", "vspId", tp.getId());
    Assert.assertEquals("first param label", "VSP ID", tp.getLabel());
    Assert.assertEquals("first param type", "text", tp.getInputType());
    Assert.assertEquals("first param maxLength", new Integer(36), tp.getMaxLength());
    Assert.assertEquals("first param minLength", new Integer(1), tp.getMinLength());
    Assert.assertFalse("first param disabled", tp.isDisabled());
    Assert.assertTrue("first param required", tp.isRequired());



  }
}