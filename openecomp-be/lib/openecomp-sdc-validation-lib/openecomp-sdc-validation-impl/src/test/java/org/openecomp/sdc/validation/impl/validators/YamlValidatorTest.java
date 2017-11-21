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
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class YamlValidatorTest {


  private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/yaml_validator";

  public Map<String, MessageContainer> runValidation(String path) {
    YamlValidator validator = new YamlValidator();
    return ValidationTestUtil.testValidator(validator, path);

  }

  @Test
  public void testValidYaml() {

    Map<String, MessageContainer> messages = runValidation(
        RESOURCE_PATH + "/valid_yaml/input/validHeat.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

  @Test
  public void testInvalidTabYaml() {

    Map<String, MessageContainer> messages = runValidation(
        RESOURCE_PATH + "/invalid_valid_yaml_structure/input/invalidYamlTab.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    ValidationTestUtil.validateErrorMessage(
        messages.get("invalidYamlTab.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: " +"[YML2]: "+ Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        "while scanning for the next tokenfound character '\\t(TAB)' that cannot start " +
            "any token. (Do not use \\t(TAB) for indentation) in 'reader', line 14, " +
            "column 5:        \tadmin_state_up: true        ^");
  }

  @Test
  public void testDuplicateKeyInYaml() {

    Map<String, MessageContainer> messages =
        runValidation(RESOURCE_PATH + "/duplicateKey.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("duplicateKey.yaml"));
    ValidationTestUtil.validateErrorMessage(
        messages.get("duplicateKey.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: " +"[YML2]: "+ Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        "while parsing MappingNode in 'reader', line 6, column 3:      " +
            "Key_1_unique:      ^duplicate key: Key_2_not_unique in 'reader', line 31, " +
            "column 1:        ^");
  }

  @Test
  public void testInvalidYamlStructure() {

    Map<String, MessageContainer> messages = runValidation(
        RESOURCE_PATH + "/invalidYamlStructure.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("invalidYamlStructure.yaml"));
    ValidationTestUtil.validateErrorMessage(
        messages.get("invalidYamlStructure.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: " +"[YML2]: "+ Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        "while parsing a block mapping in 'reader', line 8, column 7:          " +
            "admin_state_up: true          ^expected <block end>, but found BlockEntry in 'reader', " +
            "line 10, column 7:          - shared: true          ^");
  }

  @Test
  public void testEmptyYaml() {

    Map<String, MessageContainer> messages =
        runValidation(RESOURCE_PATH + "/emptyYaml.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("emptyYaml.yaml"));
    ValidationTestUtil.validateErrorMessage(messages.get("emptyYaml.yaml").getErrorMessageList()
            .get(0).getMessage(),
        "ERROR: " +"[YML1]: "+ Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        Messages.EMPTY_YAML_FILE.getErrorMessage());
  }

}
