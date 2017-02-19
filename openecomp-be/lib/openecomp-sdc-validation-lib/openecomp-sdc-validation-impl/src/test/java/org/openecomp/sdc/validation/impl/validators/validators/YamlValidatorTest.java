package org.openecomp.sdc.validation.impl.validators.validators;


import org.openecomp.sdc.validation.impl.validators.ValidatorBaseTest;
import org.openecomp.sdc.validation.impl.validators.YamlValidator;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.MessageContainer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class YamlValidatorTest extends ValidatorBaseTest {


  public Map<String, MessageContainer> runValidation(String path) {
    YamlValidator validator = new YamlValidator();
    return testValidator(validator, path);

  }

  @Test
  public void testValidYaml() {

    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/yaml_validator/valid_yaml/input/validHeat.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

//  @Test
  public void testInvalidTabYaml() {

    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/yaml_validator/invalid_valid_yaml_structure/input/invalidYamlTab.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    validateErrorMessage(
        messages.get("invalidYamlTab.yaml").getErrorMessageList().get(0).getMessage(),
        Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        "while scanning for the next tokenfound character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) for indentation) in 'reader', line 14, column 5:        \tadmin_state_up: true        ^");

  }


  @Test
  public void testDuplicateKeyInYaml() {

    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/yaml_validator/duplicateKey.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("duplicateKey.yaml"));
    validateErrorMessage(
        messages.get("duplicateKey.yaml").getErrorMessageList().get(0).getMessage(),
        Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        "while parsing MappingNode in 'reader', line 6, column 3:      Key_1_unique:      ^duplicate key: Key_2_not_unique in 'reader', line 31, column 1:        ^");
  }


  @Test
  public void testInvalidYamlStructure() {

    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/yaml_validator/invalidYamlStructure.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("invalidYamlStructure.yaml"));
    validateErrorMessage(
        messages.get("invalidYamlStructure.yaml").getErrorMessageList().get(0).getMessage(),
        Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        "while parsing a block mapping in 'reader', line 8, column 7:          admin_state_up: true          ^expected <block end>, but found BlockEntry in 'reader', line 10, column 7:          - shared: true          ^");
  }

  @Test
  public void testEmptyYaml() {

    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/yaml_validator/emptyYaml.yaml");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("emptyYaml.yaml"));
    validateErrorMessage(messages.get("emptyYaml.yaml").getErrorMessageList().get(0).getMessage(),
        Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
        Messages.EMPTY_YAML_FILE.getErrorMessage());
  }

}
