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
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;


public class ManifestValidatorTest {


  private ManifestValidator validator = new ManifestValidator();

  @Test
  public void testValidManifest() {

    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/manifestValidator/validFiles");
    Assert.assertNotNull(messages);
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

  @Test
  public void testManifestMissingFileInZip() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new ManifestValidator(),
        "/org/openecomp/validation/validators/manifestValidator/missingFileInZip");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("singleVol.yaml"));
    ValidationTestUtil.validateErrorMessage(messages.get("singleVol.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: " + Messages.MISSING_FILE_IN_ZIP.getErrorMessage());
  }

  @Test
  public void testInvalidManifest() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new ManifestValidator(),
        "/org/openecomp/validation/validators/manifestValidator/invalidManifest");
    Assert.assertNotNull(messages);
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey(SdcCommon.MANIFEST_NAME));
    ValidationTestUtil.validateErrorMessage(
        messages.get(SdcCommon.MANIFEST_NAME).getErrorMessageList().get(0).getMessage(),
        "ERROR: " + Messages.INVALID_MANIFEST_FILE.getErrorMessage(), SdcCommon.MANIFEST_NAME);

  }

  @Test
  public void testMissingFileInManifest() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new ManifestValidator(),
        "/org/openecomp/validation/validators/manifestValidator/missingFileInManifest");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("extraFile.env"));
    ValidationTestUtil.validateErrorMessage(messages.get("extraFile.env").getErrorMessageList().get(0).getMessage(),
        "WARNING: " + Messages.MISSING_FILE_IN_MANIFEST.getErrorMessage());

  }

  @Test
  public void testInvalidFileTypeInManifest() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new ManifestValidator(),
        "/org/openecomp/validation/validators/manifestValidator/invalidFileTypeInManifest");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 4);
    Assert.assertTrue(messages.containsKey("single.env.illegalSuffix"));
    Assert.assertTrue(messages.containsKey("illegalTypeFile.yaml"));
    Assert.assertTrue(messages.containsKey("single.yaml.illegalSuffix"));
    Assert.assertTrue(messages.containsKey("singleVol.yaml.illegalSuffix"));
    ValidationTestUtil.validateErrorMessage(
        messages.get("single.env.illegalSuffix").getErrorMessageList().get(0).getMessage(),
        "ERROR: " + Messages.WRONG_ENV_FILE_EXTENSION.getErrorMessage(),
        "single.env.illegalSuffix");
    ValidationTestUtil.validateErrorMessage(
        messages.get("illegalTypeFile.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: " + Messages.INVALID_FILE_TYPE.getErrorMessage(), "illegalTypeFile.yaml");
    ValidationTestUtil.validateErrorMessage(
        messages.get("single.yaml.illegalSuffix").getErrorMessageList().get(0).getMessage(),
        "ERROR: " + Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(),
        "single.yaml.illegalSuffix");
    ValidationTestUtil.validateErrorMessage(
        messages.get("singleVol.yaml.illegalSuffix").getErrorMessageList().get(0).getMessage(),
        "ERROR: " + Messages.WRONG_HEAT_FILE_EXTENSION.getErrorMessage(),
        "singleVol.yaml.illegalSuffix");

  }


  @Test
  public void testMissingFileInManifestAndInZip() {

    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new ManifestValidator(),
        "/org/openecomp/validation/validators/manifestValidator/missingFileInManifestAndInZip");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);
    Assert.assertTrue(messages.containsKey("extraFile.env"));
    Assert.assertTrue(messages.containsKey("singleVol.yaml"));
    ValidationTestUtil.validateErrorMessage(messages.get("extraFile.env").getErrorMessageList().get(0).getMessage(),
        "WARNING: " + Messages.MISSING_FILE_IN_MANIFEST.getErrorMessage());
    ValidationTestUtil.validateErrorMessage(messages.get("singleVol.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: " + Messages.MISSING_FILE_IN_ZIP.getErrorMessage());

  }


  @Test
  public void testEnvInRoot() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(new ManifestValidator(),
        "/org/openecomp/validation/validators/manifestValidator/envInRoot");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertTrue(messages.containsKey("second.env"));
    ValidationTestUtil.validateErrorMessage(messages.get("second.env").getErrorMessageList().get(0).getMessage(),
        "ERROR: ENV file must be associated to a HEAT file");
  }
}
