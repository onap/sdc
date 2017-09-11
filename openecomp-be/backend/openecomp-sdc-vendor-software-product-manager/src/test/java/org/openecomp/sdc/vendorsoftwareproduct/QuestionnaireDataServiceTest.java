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


import org.apache.commons.collections4.MapUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.impl.OrchestrationTemplateCandidateManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactData;
import org.openecomp.sdc.vendorsoftwareproduct.questionnaire.QuestionnaireDataService;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.tree.UploadFileTest;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.ComponentQuestionnaire;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.eq;

public class QuestionnaireDataServiceTest {
  public static final Version VERSION = new Version(0, 1);
  private QuestionnaireDataService questionnaireDataService;// = new QuestionnaireDataServiceImpl();

  @Mock
  private VendorSoftwareProductDao vendorSoftwareProductDaoMock;
  @Mock
  private CandidateService candidateServiceMock;
  @Mock
  private HealingManager healingManagerMock;
  @Mock
  private CompositionDataExtractor compositionDataExtractorMock;
  @Mock
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDaoMock;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private ActivityLogManager activityLogManagerMock;

  @Captor
  private ArgumentCaptor<ActivityLogEntity> activityLogEntityArg;

  @InjectMocks
  private OrchestrationTemplateCandidateManagerImpl candidateManager;

  private UploadFileTest uploadFileTest = new UploadFileTest();

  private static String vspId;
  private static Version vspActiveVersion;
  private static final String USER1 = "vspTestUser1";

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  // TODO: 3/15/2017 fix and enable   //@Test
  public void testQuestionnaireDataAfterLegalUploadWithComposition() {
    InformationArtifactData informationArtifactData =
        uploadFileAndValidateInformationArtifactData("/fullComposition", 5);

    assertQuestionnaireValuesAreAsExpected(informationArtifactData, false);
  }


  // TODO: 3/15/2017 fix and enable   //@Test
  public void testQuestionnaireDataAfterLegalUploadEmptyComposition() {
    uploadFileAndValidateInformationArtifactData("/emptyComposition", 0);
  }


  // TODO: 3/15/2017 fix and enable   //@Test
  public void testQuestionnaireDataAfterIllegalUpload() {
    InputStream zipInputStream = uploadFileTest.getZipInputStream("/missingYml");
    UploadFileResponse uploadFileResponse = candidateManager
        .upload(vspId, VERSION, zipInputStream, USER1, "zip", "missingYml");

    InformationArtifactData informationArtifactData = questionnaireDataService
        .generateQuestionnaireDataForInformationArtifact(vspId, vspActiveVersion);

  }

  private InformationArtifactData uploadFileAndValidateInformationArtifactData(String filePath,
                                                                               int listSizeToCheck) {
    InputStream zipInputStream = uploadFileTest.getZipInputStream(filePath);
    UploadFileResponse uploadFileResponse = candidateManager
        .upload(vspId, VERSION,
            zipInputStream, USER1,"zip", "file");
    candidateManager.process(vspId, VERSION, USER1);

    Assert.assertTrue(MapUtils.isEmpty(
        MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, uploadFileResponse.getErrors())));

    InformationArtifactData informationArtifactData = questionnaireDataService
        .generateQuestionnaireDataForInformationArtifact(vspId, vspActiveVersion);
    Assert.assertNotNull(informationArtifactData);

    List<ComponentQuestionnaire> componentQuestionnaireList =
        informationArtifactData.getComponentQuestionnaires();
    Assert.assertEquals(componentQuestionnaireList.size(), listSizeToCheck);

    verify(activityLogManagerMock).addActionLog(activityLogEntityArg.capture(),eq(USER1));
    ActivityLogEntity activityLogEntity = activityLogEntityArg.getValue();
    Assert.assertEquals(activityLogEntity.getVersionId(), String.valueOf(VERSION.getMajor()+1));
    Assert.assertTrue(activityLogEntity.isSuccess());

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

}
