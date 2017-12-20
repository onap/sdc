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

package org.openecomp.sdc.vendorsoftwareproduct.tree;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.OrchestrationTemplateCandidateManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.CandidateServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VSPCommon;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipOutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;

public class UploadFileTest {
  private static final String USER1 = "vspTestUser1";

  public static final Version VERSION01 = new Version(0, 1);

  @Mock
  private OrchestrationTemplateDao orchestrationTemplateDataDaoMock;
  @Spy
  private CandidateServiceImpl candidateService;
  @Mock
  private HealingManager healingManagerMock;
  @Mock
  private CompositionDataExtractor compositionDataExtractorMock;
  @Mock
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDaoMock;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDaoMock;

  @InjectMocks
  private OrchestrationTemplateCandidateManagerImpl candidateManager;

  private static String vlm1Id;
  public static String id001 = null;
  public static String id002 = null;

  public static Version activeVersion002 = null;


  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testUploadFile() {
    VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));
    candidateManager.upload(id001, activeVersion002, getZipInputStream("/legalUpload"),
        OnboardingTypesEnum.ZIP.toString(), "legalUpload");
  }


  private void testLegalUpload(String vspId, Version version, InputStream upload, String user) {
    UploadFileResponse uploadFileResponse = candidateManager.upload(vspId, activeVersion002,
        upload, OnboardingTypesEnum.ZIP.toString(), "file");
    assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.ZIP);
    OrchestrationTemplateEntity uploadData = orchestrationTemplateDataDaoMock.get(vspId, version);

  }

  public InputStream getZipInputStream(String name) {
    URL url = this.getClass().getResource(name);
    File templateDir = new File(url.getFile());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      VSPCommon.zipDir(templateDir, "", zos, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }


}
