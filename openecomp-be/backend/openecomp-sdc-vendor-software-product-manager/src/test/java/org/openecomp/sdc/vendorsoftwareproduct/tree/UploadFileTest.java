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


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.exception.OnboardPackageException;
import org.openecomp.sdc.vendorsoftwareproduct.impl.OrchestrationTemplateCandidateManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.CandidateServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VSPCommon;
import org.openecomp.sdc.versioning.dao.types.Version;

public class UploadFileTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileTest.class);
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

  private OnboardPackageInfo onboardPackageInfo;

  @InjectMocks
  private OrchestrationTemplateCandidateManagerImpl candidateManager;

  public static String id001 = "dummyId";
  public static Version activeVersion002 = new Version(1, 0);

  private final VspDetails vspDetails = new VspDetails(id001, activeVersion002);

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testUploadFile() throws IOException, OnboardPackageException {
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));
    try (final InputStream inputStream = getZipInputStream("/legalUpload")) {
      onboardPackageInfo = new OnboardPackageInfo("legalUpload", OnboardingTypesEnum.ZIP.toString(),
              convertFileInputStream(inputStream), OnboardingTypesEnum.ZIP);
      candidateManager.upload(vspDetails, onboardPackageInfo);

    }
  }

  private void testLegalUpload(String vspId, Version version, InputStream upload, String user)
      throws IOException, OnboardPackageException {
    onboardPackageInfo = new OnboardPackageInfo("file", OnboardingTypesEnum.ZIP.toString(),
            convertFileInputStream(upload), OnboardingTypesEnum.ZIP);
    final UploadFileResponse uploadFileResponse = candidateManager.upload(vspDetails, onboardPackageInfo);
    assertEquals(OnboardingTypesEnum.ZIP, uploadFileResponse.getOnboardingType());
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
