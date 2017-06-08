/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator;

import org.everit.json.schema.EmptySchema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentQuestionnaireSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NetworkCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NicCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;

public class SchemaGeneratorTest {

  private static int getMinOfVmMax(JSONObject schemaJson) {
    return schemaJson.getJSONObject("properties").getJSONObject("compute")
        .getJSONObject("properties").getJSONObject("numOfVMs").getJSONObject("properties")
        .getJSONObject("maximum").getInt("minimum");
  }

  private static JSONObject validateSchema(String schema) {
    System.out.println(schema);
    Assert.assertNotNull(schema);
    Assert.assertTrue(JsonUtil.isValidJson(schema));
    JSONObject schemaJson = new JSONObject(schema);
    Assert.assertFalse(SchemaLoader.load(schemaJson) instanceof EmptySchema);
    return schemaJson;
  }

  // TODO: 3/15/2017 fix and enable   //@Test
  public void testGenerateVspQuestionnaire() {
    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, null);
    validateSchema(schema);
  }

  @Test
  public void testGenerateNetworkCompositionUpload() {
    Network network = new Network();
    network.setName("upload network1 name");
    network.setDhcp(true);

    NetworkCompositionSchemaInput input = new NetworkCompositionSchemaInput();
    input.setManual(false);
    input.setNetwork(network);

    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.network, input);
    validateSchema(schema);
  }

  @Test
  public void testGenerateNetworkCompositionManual() {
    NetworkCompositionSchemaInput input = new NetworkCompositionSchemaInput();
    input.setManual(true);

    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.network, input);

    validateSchema(schema);
  }

  @Test
  public void testGenerateComponentQuestionnaireWithoutInput() {
    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component, null);
    validateSchema(schema);
  }

  @Test
  public void testGenerateComponentQuestionnaireWithMissingInput() {
    ComponentQuestionnaireSchemaInput
        input = new ComponentQuestionnaireSchemaInput(Arrays.asList("nic1", "nic2"),
        JsonUtil.json2Object("{\n" +
            "  \"compute\": {\n" +
            "    \"numOfVMs\": {\n" +
            "      \"blabla\": 70\n" + // no minimum
            "    }\n" +
            "  }\n" +
            "}", Map.class));
    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component, input);
    JSONObject schemaJson = validateSchema(schema);
    //Assert.assertEquals(getMinOfVmMax(schemaJson), 0);
  }

  @Test
  public void testGenerateComponentQuestionnaireWithInvalidTypeInput() {
    ComponentQuestionnaireSchemaInput input =
        new ComponentQuestionnaireSchemaInput(Arrays.asList("nic1", "nic2"),
            JsonUtil.json2Object("{\n" +
                "  \"compute\": {\n" +
                "    \"numOfVMs\": {\n" +
                "      \"minimum\": \"some string instead of integer\"\n" +
                // invalid minimum - string
                "    }\n" +
                "  }\n" +
                "}", Map.class));
    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component, input);
    JSONObject schemaJson = validateSchema(schema);
    Assert.assertEquals(getMinOfVmMax(schemaJson), 0);
  }

  @Test
  public void testGenerateComponentQuestionnaireWithInvalidRangeInput() {
    ComponentQuestionnaireSchemaInput input =
        new ComponentQuestionnaireSchemaInput(Arrays.asList("nic1", "nic2"),
            JsonUtil.json2Object("{\n" +
                "  \"compute\": {\n" +
                "    \"numOfVMs\": {\n" +
                "      \"minimum\": 150\n" + // invalid minimum - integer out of range (0-100)
                "    }\n" +
                "  }\n" +
                "}", Map.class));
    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component, input);
    JSONObject schemaJson = validateSchema(schema);
    Assert.assertEquals(getMinOfVmMax(schemaJson), 0);
  }

  @Test
  public void testGenerateComponentQuestionnaireWithValidInput() {
    ComponentQuestionnaireSchemaInput input =
        new ComponentQuestionnaireSchemaInput(Arrays.asList("nic1", "nic2"),
            JsonUtil.json2Object("{\n" +
                "  \"compute\": {\n" +
                "    \"numOfVMs\": {\n" +
                "      \"minimum\": 30\n" + // valid minimum - integer at the correct range (0-100)
                "    }\n" +
                "  }\n" +
                "}", Map.class));
    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component, input);
    JSONObject schemaJson = validateSchema(schema);
    Assert.assertEquals(getMinOfVmMax(schemaJson), 30);
  }

  @Test
  public void testGenerateNicQuestionnaire() {
    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.nic, null);
    validateSchema(schema);
  }

  @Test
  public void testGenerateComponentCompositionUpload() {
    ComponentData component = new ComponentData();
    component.setName("upload comp1 name");
    component.setDescription("upload comp1 desc");

    ComponentCompositionSchemaInput input = new ComponentCompositionSchemaInput();
    input.setManual(false);
    input.setComponent(component);

    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.component, input);
    validateSchema(schema);
  }

  @Test
  public void testGenerateComponentCompositionManual() {
    ComponentCompositionSchemaInput input = new ComponentCompositionSchemaInput();
    input.setManual(true);

    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.component, input);
    validateSchema(schema);
  }

  @Test
  public void testGenerateNicCompositionUpload() {
    Nic nic = new Nic();
    nic.setName("upload nic1 name");
    nic.setDescription("upload nic1 desc");
    nic.setNetworkId("upload nic1 networkId");
    //nic.setNetworkName("upload nic1 networkName");
    nic.setNetworkType(NetworkType.External);

    NicCompositionSchemaInput input = new NicCompositionSchemaInput();
    input.setManual(false);
    input.setNic(nic);

    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.nic, input);
    validateSchema(schema);
  }


//    @Test
//    public void testGenerateNicCompositionManualWithoutNetworkId() {
//        Nic nic = new Nic();
//        nic.setName("upload nic1 name");
//        nic.setDescription("upload nic1 desc");
//        //nic.setNetworkName("upload nic1 networkName");
//        nic.setNetworkType(NetworkType.External);
//
//        NicCompositionSchemaInput input = new NicCompositionSchemaInput();
//        input.setManual(true);
//        input.setNic(nic);
//
//        String schema = SchemaGenerator.generate(SchemaTemplateContext.composition, CompositionEntityType.nic, input);
//        validateSchema(schema);
//    }

  @Test
  public void testGenerateNicCompositionUploadWithoutNetworkId() {
    Nic nic = new Nic();
    nic.setName("upload nic1 name");
    nic.setDescription("upload nic1 desc");
    //nic.setNetworkName("upload nic1 networkName");
    nic.setNetworkType(NetworkType.External);

    NicCompositionSchemaInput input = new NicCompositionSchemaInput();
    input.setManual(false);
    input.setNic(nic);

    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.nic, input);
    validateSchema(schema);
  }

  @Test
  public void testGenerateNicCompositionManual() {
    NicCompositionSchemaInput input = new NicCompositionSchemaInput();
    input.setManual(true);
    input.setNetworkIds(
        Arrays.asList("manual networkId1", "manual networkId2", "manual networkId3"));

    String schema = SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.nic, input);
    validateSchema(schema);
  }
}
