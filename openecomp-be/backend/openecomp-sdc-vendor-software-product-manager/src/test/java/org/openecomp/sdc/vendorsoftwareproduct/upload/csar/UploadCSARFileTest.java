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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.OrchestrationTemplateCandidateManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.CandidateServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.ManifestCreatorNamingConventionImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

public class UploadCSARFileTest {

  public static final Version VERSION01 = new Version("0.1");

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


  private static String id001 = "dummyId";
  private static Version activeVersion002 = new Version("dummyVersion");
  private static final String BASE_DIR = "/vspmanager.csar";
  private static final String CSAR = "csar";


  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    candidateService = new CandidateServiceImpl(manifestCreator, orchestrationTemplateCandidateDao);
    candidateManager = new OrchestrationTemplateCandidateManagerImpl(vspInfoDaoMock,
        candidateService);
  }

  @Test
  public void testSuccessfulUploadFile() throws Exception {
    VspDetails vspDetails = new VspDetails(id001, activeVersion002);
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    testCsarUpload("successfulUpload.csar", 0);
  }

  @Test
  public void testIllegalUploadInvalidFileInRoot() throws Exception {
    VspDetails vspDetails = new VspDetails(id001, activeVersion002);
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    UploadFileResponse response = testCsarUpload("invalidFileInRoot.csar", 1);
    assertTrue(response.getErrors().values().stream().anyMatch(
        getListPredicate(Messages.CSAR_FILES_NOT_ALLOWED.getErrorMessage().substring(0, 7))));
  }

  @Test
  public void testIllegalUploadMissingMainServiceTemplate() throws Exception {
    VspDetails vspDetails = new VspDetails(id001, activeVersion002);
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    UploadFileResponse response = testCsarUpload("missingMainServiceTemplate.csar", 1);
    assertTrue(response.getErrors().values().stream().anyMatch(
        getListPredicate(Messages.CSAR_FILE_NOT_FOUND.getErrorMessage().substring(0, 7))));
  }

  @Test
  public void testUploadFileIsNotZip() throws Exception {
    VspDetails vspDetails = new VspDetails(id001, activeVersion002);
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    UploadFileResponse response = testCsarUpload("notCsar.txt", 1);
    assertEquals("no csar file was uploaded or file doesn't exist",
        response.getErrors().values().iterator().next().get(0).getMessage());
  }

  @Test
  public void testUploadFileIsEmpty() throws Exception {
    VspDetails vspDetails = new VspDetails(id001, activeVersion002);
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

    try (InputStream is = new ByteArrayInputStream(new byte[]{})) {
      UploadFileResponse uploadFileResponse = candidateManager.upload(id001,
          activeVersion002, is, "csar", "file");
      assertEquals(1, uploadFileResponse.getErrors().size());
    }
  }

  @Test
  public void testInvalidManifestContent() throws Exception {
    VspDetails vspDetails = new VspDetails(id001, activeVersion002);
    doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));


    try (InputStream is = getClass()
        .getResourceAsStream(BASE_DIR + "/invalidManifestContent.csar")) {
      UploadFileResponse response =
          candidateManager.upload(id001, activeVersion002, is, "csar", "invalidManifestContent");
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

  private UploadFileResponse testCsarUpload(String csarFileName, int expectedErrorsNumber)
      throws IOException {
    UploadFileResponse uploadFileResponse;
    try (InputStream is = getClass()
        .getResourceAsStream(BASE_DIR + File.separator + csarFileName)) {
      uploadFileResponse =
          candidateManager.upload(id001, activeVersion002, is, CSAR, csarFileName);
      assertEquals(expectedErrorsNumber, uploadFileResponse.getErrors().size());
    }
    return uploadFileResponse;
  }


}
