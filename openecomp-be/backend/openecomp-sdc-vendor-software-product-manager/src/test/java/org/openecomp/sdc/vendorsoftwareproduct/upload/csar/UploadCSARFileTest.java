/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.vendorsoftwareproduct.upload.csar;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.OrchestrationTemplateCandidateManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.CandidateServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.ManifestCreatorNamingConventionImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

public class UploadCSARFileTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(UploadCSARFileTest.class);

  @Spy
  private CandidateServiceImpl candidateService;
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDaoMock;
  @Mock
  private OrchestrationTemplateCandidateDao orchestrationTemplateCandidateDao;
  @Mock
  private ManifestCreatorNamingConventionImpl manifestCreator;

  @InjectMocks
  private OrchestrationTemplateCandidateManagerImpl candidateManager;

  private OnboardPackageInfo onboardPackageInfo;
  private final VspDetails vspDetails = new VspDetails(id001, activeVersion002);

  private static String id001 = "dummyId";
  private static Version activeVersion002 = new Version("dummyVersion");
  private static final String BASE_DIR = "/vspmanager.csar";


  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    candidateService = new CandidateServiceImpl(manifestCreator, orchestrationTemplateCandidateDao);
    candidateManager = new OrchestrationTemplateCandidateManagerImpl(vspInfoDaoMock,
        candidateService);
  }

  @Test
  public void testSuccessfulUploadFile() throws Exception {
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    testCsarUpload("successfulUpload.csar", 0);
  }

  @Test
  public void testIllegalUploadInvalidFileInRoot() throws Exception {
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    UploadFileResponse response = testCsarUpload("invalidFileInRoot.csar", 1);
    assertTrue(response.getErrors().values().stream().anyMatch(
        getListPredicate(Messages.CSAR_FILES_NOT_ALLOWED.getErrorMessage().substring(0, 7))));
  }

  @Test
  public void testIllegalUploadMissingMainServiceTemplate() throws Exception {
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    UploadFileResponse response = testCsarUpload("missingMainServiceTemplate.csar", 1);
    assertTrue(response.getErrors().values().stream().anyMatch(
        getListPredicate(Messages.CSAR_FILE_NOT_FOUND.getErrorMessage().substring(0, 7))));
  }

  @Test
  public void testUploadFileIsNotZip() throws Exception {
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    UploadFileResponse response = testCsarUpload("notCsar.txt", 1);
    assertEquals("no csar file was uploaded or file doesn't exist",
        response.getErrors().values().iterator().next().get(0).getMessage());
  }

  @Test
  public void testUploadFileIsEmpty() throws Exception {
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));
    onboardPackageInfo = new OnboardPackageInfo("file", OnboardingTypesEnum.CSAR.toString(),
            ByteBuffer.wrap(new byte[]{}));
    UploadFileResponse uploadFileResponse = candidateManager.upload(vspDetails, onboardPackageInfo);
    assertEquals(1, uploadFileResponse.getErrors().size());
  }

  @Test
  public void testInvalidManifestContent() throws Exception {

    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    try (InputStream inputStream = getClass()
        .getResourceAsStream(BASE_DIR + "/invalidManifestContent.csar")) {
      onboardPackageInfo = new OnboardPackageInfo("invalidManifestContent",
              OnboardingTypesEnum.CSAR.toString(), convertFileInputStream(inputStream));
      UploadFileResponse response =
          candidateManager.upload(vspDetails, onboardPackageInfo);
      assertEquals(1, response.getErrors().size());
      assertEquals(response.getErrors().values().iterator().next().get(0).getMessage(),
          "Manifest " +
              "contains invalid line : aaa: vCSCF");

    }
  }

  private Predicate<List<ErrorMessage>> getListPredicate(String substring) {
    return error -> isEquals(substring, error);
  }

  private boolean isEquals(String substring, List<ErrorMessage> error) {
    return error.iterator().next().getMessage().contains(substring);
  }

  private UploadFileResponse testCsarUpload(final String csarFileName,
                                            final int expectedErrorsNumber) throws IOException {
    UploadFileResponse uploadFileResponse;
    try (final InputStream inputStream = getClass()
        .getResourceAsStream(BASE_DIR + File.separator + csarFileName)) {
      onboardPackageInfo = new OnboardPackageInfo(csarFileName, OnboardingTypesEnum.CSAR.toString(),
              convertFileInputStream(inputStream));
      uploadFileResponse = candidateManager.upload(vspDetails, onboardPackageInfo);
      assertEquals(expectedErrorsNumber, uploadFileResponse.getErrors().size());
    }
    return uploadFileResponse;
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
