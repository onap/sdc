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

package org.openecomp.sdc.validation.impl.validators;


import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;


public class HeatValidatorTest {


  private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/heat_validator";
  private Validator validator = new HeatValidator();

  @Test
  public void testInvalidHeatFormat() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/invalid_heat_format/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT12]: Invalid HEAT format problem - [Cannot create property=kuku for " +
            "JavaBean=Resource{type='null', properties=null, metadata=null, depends_on=null, " +
            "update_policy='null', deletion_policy='null'}\n" + " in 'reader', line 25, column 5:\n" +
            "        kuku: kuku\n" + "        ^\n" +
            "Unable to find property 'kuku' on class: org.openecomp.sdc.heat.datatypes.model." +
            "Resource\n" + " in 'reader', line 25, column 11:\n" + "        kuku: kuku\n" +
            "              ^\n" + "]");
  }

  @Test
  public void testDependsOn() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/depends_on_points_to_existing_resource/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT8]: a Missing resource in depend On, Missing Resource ID [resource_not_exist]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "ERROR: [HOT7]: a Missing resource in depend On, Missing Resource ID [resource_3]");
  }


  @Test
  public void testResourcesReferencesExistInHeat() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/resource_references_exist_in_heat/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT16]: Referenced resource - not_existing_resource not found");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "ERROR: [HOT13]: invalid get_resource syntax is in use - null , get_resource " +
            "function should get the resource id of the referenced resource");
  }


  @Test
  public void testGetResourceValueIsValid() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/get_resource_value_valid/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT13]: invalid get_resource syntax is in use - [param_1, param_2] , " +
            "get_resource function should get the resource id of the referenced resource");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "ERROR: [HOT13]: invalid get_resource syntax is in use - {get_param=param_1} , " +
            "get_resource function should get the resource id of the referenced resource");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(2).getMessage(),
        "ERROR: [HOT13]: invalid get_resource syntax is in use - null , get_resource " +
            "function should get the resource id of the referenced resource");
  }

  @Test
  public void testTwoResourcesDoesNotHoldSameId() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/two_resources_does_not_hold_same_id/positive_test/input");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

  @Test
  public void testWithWrongFileExtension(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/wrongFileExtension");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("single.aad").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT2]: Wrong ENV file extension - single.aad");
    Assert.assertEquals(messages.get("single.ya").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT6]: Wrong HEAT file extension - single.ya");
  }

  @Test
  public void testWithMissingManifestContent(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/missingManifestContent");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("single.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT9]: Invalid HEAT format problem - [missing template version]");
    Assert.assertEquals(messages.get("single.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [HOT10]: Invalid HEAT format problem - " +
            "[The heat file does not contain any resources]");
  }

  @Test
  public void testWithInvalidHeatContent(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/invalidHeatContent");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("single.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT9]: Invalid HEAT format problem - [missing template version]");
    Assert.assertEquals(messages.get("single.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [HOT10]: Invalid HEAT format problem - " +
            "[The heat file does not contain any resources]");
    Assert.assertEquals(messages.get("mount_iso_script.sh").getErrorMessageList().get(0).
        getMessage(),"WARNING: [HOT11]: Artifact file is not referenced.");
  }

  @Test
  public void negativeTestGetParamPointToExistingParameter() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/get_param_points_to_existing_parameter/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT1]: Referenced parameter - not_existing_param_1 - not found, used in " +
            "resource [server_pcrf_psm_001]");
  }

  @Test
  public void testGetAttrFromNested() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/get_attr_from_nested/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT17]: get_attr attribute not found, Attribute name [nested_output], " +
            "Resource ID [server_pcrf_psm_001]");
  }

  @Test
  public void testDefaultValueAlignWithType() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/default_value_align_with_type/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT4]: Parameter - pcrf_pps_image_name_1 default value not align with " +
            "type number");
  }


  @Test
  public void testEnvParametersMatchDefinedHeatParameterTypes() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/env_parameters_match_defined_types/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT5]: Parameter env value pcrf_pps_flavor_name not align with type");

  }

  @Test
  public void testReferencedArtifactsExist() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/referenced_artifacts_exist/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT15]: Missing artifact - nimbus-ethernet");

  }

  @Test
  public void testEnvContentIsSubSetOfHeatParameters() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/env_content_is_subset_of_heat/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().get(0).getMessage(),
        "ERROR: [HOT3]: Env file hot-nimbus-pps_v1.0.env includes a parameter not in " +
            "HEAT - mock_param");
  }

  @Test
  public void testGetParamPseudoParameters() {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(new HeatValidator(),
        RESOURCE_PATH + "/pseudo_parameters/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

  }
  @Test
  public void testNoErrorWhenEmptyValueForParameterInEnv() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/env_empty_value/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

}
