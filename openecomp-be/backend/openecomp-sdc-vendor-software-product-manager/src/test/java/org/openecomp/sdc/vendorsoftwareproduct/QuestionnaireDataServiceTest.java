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

package org.openecomp.sdc.vendorsoftwareproduct;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.OrchestrationTemplateCandidateManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactData;
import org.openecomp.sdc.vendorsoftwareproduct.questionnaire.QuestionnaireDataService;
import org.openecomp.sdc.vendorsoftwareproduct.tree.UploadFileTest;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.ComponentQuestionnaire;
import org.openecomp.sdc.versioning.dao.types.Version;

public class QuestionnaireDataServiceTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(QuestionnaireDataServiceTest.class);

  public static final Version VERSION = new Version(0, 1);
  private QuestionnaireDataService questionnaireDataService;// = new QuestionnaireDataServiceImpl();

  @InjectMocks
  private OrchestrationTemplateCandidateManagerImpl candidateManager;

  private UploadFileTest uploadFileTest = new UploadFileTest();
  private OnboardPackageInfo onboardPackageInfo;

  private static String vspId;
  private static Version vspActiveVersion;
  private static final String USER1 = "vspTestUser1";
  private static final VspDetails vspDetails = new VspDetails(vspId, VERSION);
  private static final String CSAR = "csar";
  private static final String ZIP = "zip";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  // TODO: 3/15/2017 fix and enable   //@Test
  public void testQuestionnaireDataAfterLegalUploadWithComposition() throws IOException {
    InformationArtifactData informationArtifactData =
        uploadFileAndValidateInformationArtifactData("/fullComposition", 5);

    assertQuestionnaireValuesAreAsExpected(informationArtifactData, false);
  }


  // TODO: 3/15/2017 fix and enable   //@Test
  public void testQuestionnaireDataAfterLegalUploadEmptyComposition() throws IOException {
    uploadFileAndValidateInformationArtifactData("/emptyComposition", 0);
  }


  // TODO: 3/15/2017 fix and enable   //@Test
  public void testQuestionnaireDataAfterIllegalUpload() throws IOException {
    try (InputStream zipInputStream = uploadFileTest.getZipInputStream("/missingYml")) {
      onboardPackageInfo = new OnboardPackageInfo("missingYml", CSAR, convertFileInputStream(zipInputStream), OnboardingTypesEnum.CSAR);
      UploadFileResponse uploadFileResponse =
              candidateManager.upload(vspDetails, onboardPackageInfo);
    }
    InformationArtifactData informationArtifactData = questionnaireDataService
        .generateQuestionnaireDataForInformationArtifact(vspId, vspActiveVersion);

  }

  private InformationArtifactData uploadFileAndValidateInformationArtifactData(final String filePath,
                                                                               final int listSizeToCheck)
      throws IOException {

    try (final InputStream zipInputStream = uploadFileTest.getZipInputStream(filePath)) {
      onboardPackageInfo = new OnboardPackageInfo("file", OnboardingTypesEnum.CSAR.toString(),
          convertFileInputStream(zipInputStream), OnboardingTypesEnum.CSAR);
      final UploadFileResponse uploadFileResponse = candidateManager.upload(vspDetails, onboardPackageInfo);
      candidateManager.process(vspId, VERSION);

      Assert.assertTrue(MapUtils.isEmpty(
              MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));
    }
    final InformationArtifactData informationArtifactData = questionnaireDataService
        .generateQuestionnaireDataForInformationArtifact(vspId, vspActiveVersion);
    Assert.assertNotNull(informationArtifactData);

    final List<ComponentQuestionnaire> componentQuestionnaireList =
        informationArtifactData.getComponentQuestionnaires();
    Assert.assertEquals(componentQuestionnaireList.size(), listSizeToCheck);

    return informationArtifactData;
  }

  private void assertQuestionnaireValuesAreAsExpected(
      InformationArtifactData informationArtifactData, boolean condition) {
    Assert.assertEquals(
        Objects.isNull(informationArtifactData.getComponentQuestionnaires().get(0).getCompute()),
        condition);
    Assert.assertEquals(
        Objects.isNull(informationArtifactData.getComponentQuestionnaires().get(0).getStorage()),
        condition);
  }

  private ByteBuffer convertFileInputStream(final InputStream fileInputStream) {
    byte[] fileContent = new byte[0];
    try {
      fileContent = IOUtils.toByteArray(fileInputStream);
    } catch (final IOException e) {
      LOGGER.error(String.format("Could not convert %s into byte[]", fileInputStream), e);
    }
    return ByteBuffer.wrap(fileContent);
  }

}
