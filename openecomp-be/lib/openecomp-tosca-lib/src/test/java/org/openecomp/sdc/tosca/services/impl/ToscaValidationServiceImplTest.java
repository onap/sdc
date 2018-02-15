/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.tosca.services.impl;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.tosca.services.ToscaValidationService;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ToscaValidationServiceImplTest {

  private static TestFeatureManager testFeatureManager;

  @BeforeClass
  public static void setTogglz(){
    testFeatureManager = new TestFeatureManager(ToggleableFeature.class);
  }

  @Test
  public void validateCSARContentErrorHandlingForInputToscaDefault() throws IOException {
    String resName = "/mock/validationService/csar/resource-Spgw-csar-ZTE.csar";
    byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(resName));
    FileContentHandler contentMap =
        CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.CSAR, uploadedFileData);
    ToscaValidationService handler = new ToscaValidationServiceImpl();
    Map<String, List<ErrorMessage>> errors = handler.validateInputTosca(contentMap);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void validateCSARContentErrorHandlingForTranslatedToscaDefault() throws IOException {
    String resName = "/mock/validationService/csar/sdc-onboarding-csar.csar";
    byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(resName));
    FileContentHandler contentMap =
        CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.CSAR, uploadedFileData);
    ToscaValidationService handler = new ToscaValidationServiceImpl();
    Map<String, List<ErrorMessage>> errors = handler.validateTranslatedTosca(contentMap);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void validateCSARContentErrorHandlingForInputTosca() throws IOException {
    // Enable Togglz
    testFeatureManager.enable(ToggleableFeature.VALIDATE_INPUT_TOSCA);
    TestFeatureManagerProvider.setFeatureManager(testFeatureManager);

    String resName = "/mock/validationService/csar/resource-Spgw-csar-ZTE.csar";
    byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(resName));
    FileContentHandler contentMap =
        CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.CSAR, uploadedFileData);
    ToscaValidationService handler = new ToscaValidationServiceImpl();
    Map<String, List<ErrorMessage>> errors = handler.validateInputTosca(contentMap);
    assertFalse(errors.isEmpty());

    // Disable Togglz
    testFeatureManager.disable(ToggleableFeature.VALIDATE_INPUT_TOSCA);
  }

  @Test
  public void validateCSARContentErrorHandlingForTranslatedTosca() throws IOException {
    // Enable Togglz
    testFeatureManager.enable(ToggleableFeature.VALIDATE_TRANSLATED_TOSCA);
    TestFeatureManagerProvider.setFeatureManager(testFeatureManager);

    String resName = "/mock/validationService/csar/sdc-onboarding-csar.csar";
    byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(resName));
    FileContentHandler contentMap =
        CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.CSAR, uploadedFileData);
    ToscaValidationService handler = new ToscaValidationServiceImpl();
    Map<String, List<ErrorMessage>> errors = handler.validateTranslatedTosca(contentMap);
    assertFalse(errors.isEmpty());

    // Disable Togglz
    testFeatureManager.disable(ToggleableFeature.VALIDATE_TRANSLATED_TOSCA);
  }

  @Test
  public void validateCSARContentErrorHandling() throws IOException {
    String resName = "/mock/validationService/csar/resource-Spgw-csar-ZTE.csar";
    byte[] uploadedFileData = IOUtils.toByteArray(this.getClass().getResource(resName));
    FileContentHandler contentMap =
        CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.CSAR, uploadedFileData);
    ToscaValidationService handler = new ToscaValidationServiceImpl();
    Map<String, List<ErrorMessage>> errors = handler.validate(contentMap);
    assertFalse(errors.isEmpty());
  }

  @AfterClass
  public static void unsetTogglz(){
    testFeatureManager = null;
    TestFeatureManagerProvider.setFeatureManager(null);
  }

}
