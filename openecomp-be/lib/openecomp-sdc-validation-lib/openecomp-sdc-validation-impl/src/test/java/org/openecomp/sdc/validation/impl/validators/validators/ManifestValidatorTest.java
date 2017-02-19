package org.openecomp.sdc.validation.impl.validators.validators;


import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.validation.impl.validators.ManifestValidator;
import org.openecomp.sdc.validation.impl.validators.ValidatorBaseTest;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.MessageContainer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class ManifestValidatorTest extends ValidatorBaseTest {


  @Test
  public void testValidManifest() {
    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/manifestValidator/validFiles");
    Assert.assertNotNull(messages);
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

  @Test
  public void testManifestMissingFileInZip() {
    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/manifestValidator/missingFileInZip");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("singleVol.yaml"));
    validateErrorMessage(messages.get("singleVol.yaml").getErrorMessageList().get(0).getMessage(),
        Messages.MISSING_FILE_IN_ZIP.getErrorMessage());
  }

  @Test
  public void testInvalidManifest() {
    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/manifestValidator/invalidManifest");
    Assert.assertNotNull(messages);
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey(AsdcCommon.MANIFEST_NAME));
    validateErrorMessage(
        messages.get(AsdcCommon.MANIFEST_NAME).getErrorMessageList().get(0).getMessage(),
        Messages.INVALID_MANIFEST_FILE.getErrorMessage(), AsdcCommon.MANIFEST_NAME);

  }

  @Test
  public void testMissingFileInManifest() {
    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/manifestValidator/missingFileInManifest");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("extraFile.env"));
    validateErrorMessage(messages.get("extraFile.env").getErrorMessageList().get(0).getMessage(),
        Messages.MISSING_FILE_IN_MANIFEST.getErrorMessage());

  }

  @Test
  public void testInvalidFileTypeInManifest() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/manifestValidator/invalidFileTypeInManifest");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 4);
    Assert.assertTrue(messages.containsKey("single.env.illegalSuffix"));
    Assert.assertTrue(messages.containsKey("illegalTypeFile.yaml"));
    Assert.assertTrue(messages.containsKey("single.yaml.illegalSuffix"));
    Assert.assertTrue(messages.containsKey("singleVol.yaml.illegalSuffix"));
    validateErrorMessage(
        messages.get("single.env.illegalSuffix").getErrorMessageList().get(0).getMessage(),
        Messages.WRONG_ENV_FILE_EXTENSION.getErrorMessage(), "single.env.illegalSuffix");
    validateErrorMessage(
        messages.get("illegalTypeFile.yaml").getErrorMessageList().get(0).getMessage(),
        Messages.INVALID_FILE_TYPE.getErrorMessage(), "illegalTypeFile.yaml");
    validateErrorMessage(
        messages.get("single.yaml.illegalSuffix").getErrorMessageList().get(0).getMessage(),
        Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(), "single.yaml.illegalSuffix");
    validateErrorMessage(
        messages.get("singleVol.yaml.illegalSuffix").getErrorMessageList().get(0).getMessage(),
        Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(), "singleVol.yaml.illegalSuffix");

  }


  @Test
  public void testMissingFileInManifestAndInZip() {

    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/manifestValidator/missingFileInManifestAndInZip");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);
    Assert.assertTrue(messages.containsKey("extraFile.env"));
    Assert.assertTrue(messages.containsKey("singleVol.yaml"));
    validateErrorMessage(messages.get("extraFile.env").getErrorMessageList().get(0).getMessage(),
        Messages.MISSING_FILE_IN_MANIFEST.getErrorMessage());
    validateErrorMessage(messages.get("singleVol.yaml").getErrorMessageList().get(0).getMessage(),
        Messages.MISSING_FILE_IN_ZIP.getErrorMessage());

  }


  @Test
  public void testEnvInRoot() {
    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/manifestValidator/envInRoot");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("second.env"));
    validateErrorMessage(messages.get("second.env").getErrorMessageList().get(0).getMessage(),
        "ENV file must be associated to a HEAT file");
  }

  public Map<String, MessageContainer> runValidation(String path) {
    ManifestValidator manifestValidator = new ManifestValidator();
    return testValidator(manifestValidator, path);

  }


}
