package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileStatus;
import org.openecomp.sdc.vendorsoftwareproduct.utils.ZipFileUtils;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;

/**
 * @author Avrahamg
 * @since November 08, 2016
 */
public class OrchestrationTemplateCandidateManagerImplTest {
  private static final String USER1 = "vspTestUser1";
  private static final String VSP_ID = "vspId";
  private static final Version VERSION01 = new Version(0, 1);

  @Mock
  private VendorSoftwareProductDao vendorSoftwareProductDaoMock;
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDaoMock;
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
  @InjectMocks
  private OrchestrationTemplateCandidateManagerImpl candidateManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  // TODO: 3/15/2017 fix and enable
  //@Test
  public void testProcessEmptyUpload() throws IOException {
/*    testLegalUpload(VSP_ID, activeVersion002,
        new ZipFileUtils().getZipInputStream("/vspmanager/zips/emptyComposition.zip"), USER1);*/

    OrchestrationTemplateCandidateDataEntity orchTemplate =
        new OrchestrationTemplateCandidateDataEntity(VSP_ID, VERSION01);
    orchTemplate
        .setContentData(ByteBuffer.wrap(FileUtils.toByteArray(new ZipFileUtils().getZipInputStream
            ("/vspmanager/zips/emptyComposition.zip"))));
    orchTemplate.setFilesDataStructure("{\n" +
        "  \"modules\": [\n" +
        "    {\n" +
        "      \"isBase\": false,\n" +
        "      \"yaml\": \"ep-jsa_net.yaml\"\n" +
        "    }\n" +
        "  ]\n" +
        "}");
    doReturn(orchTemplate)
        .when(candidateServiceMock).getOrchestrationTemplateCandidate(VSP_ID, VERSION01);

    doReturn(new VspDetails(VSP_ID, VERSION01))
        .when(vspInfoDaoMock).get(anyObject());

    doReturn("{}").when(candidateServiceMock).createManifest(anyObject(), anyObject());
    doReturn(Optional.empty()).when(candidateServiceMock)
        .fetchZipFileByteArrayInputStream(anyObject(), anyObject(), anyObject(), anyObject());


    OrchestrationTemplateActionResponse response =
        candidateManager.process(VSP_ID, VERSION01, USER1);

    Assert.assertNotNull(response);
  }
  /*
  @Test(dependsOnMethods = {"testUploadFile"})
  public void testUploadNotExistingFile() throws IOException {
    URL url = this.getClass().getResource("notExist.zip");
    testLegalUpload(VSP_ID, activeVersion002, url == null ? null : url.openStream(), USER1);
  }

  private void testLegalUpload(String vspId, Version version, InputStream upload, String user) {
    candidateManager.upload(vspId, VERSION01, upload, USER1);
    candidateManager.process(vspId, VERSION01, user);

    UploadDataEntity
        uploadData =
        vendorSoftwareProductDaoMock.getUploadData(new UploadDataEntity(vspId, version));
    Assert.assertNotNull(uploadData);
  }*/

  // TODO: 3/15/2017 fix and enable
  //@Test
  public void testUploadWith2VolsIn1HeatInManifest() {
    doReturn(Optional.empty()).when(candidateServiceMock).validateNonEmptyFileToUpload(anyObject());
    doReturn(Optional.empty()).when(candidateServiceMock).validateRawZipData(anyObject());

    UploadFileResponse uploadFileResponse = candidateManager
        .upload(VSP_ID, VERSION01, new ZipFileUtils().getZipInputStream("/legalUploadWithWarning"),
            USER1);
    Assert.assertTrue(uploadFileResponse.getStatus() == UploadFileStatus.Success);
    Assert.assertTrue(
        uploadFileResponse.getErrors().get("uploadFile").get(0).getLevel() == ErrorLevel.WARNING);
    Assert.assertTrue(uploadFileResponse.getErrors().get("uploadFile").get(0).getMessage()
        .equals("heat contains more then one vol. selecting only first vol"));
  }

  // TODO: 3/15/2017 fix and enable
  //@Test
  public void testUploadWithManifest() {
    UploadFileResponse uploadFileResponse = candidateManager
        .upload(VSP_ID, VERSION01, new ZipFileUtils().getZipInputStream("/legalUploadWithWarning"),
            USER1);
    Assert.assertTrue(uploadFileResponse.getStatus() == UploadFileStatus.Success);
    Assert.assertTrue(
        uploadFileResponse.getErrors().get("uploadFile").get(0).getLevel() == ErrorLevel.WARNING);
    Assert.assertTrue(uploadFileResponse.getErrors().get("uploadFile").get(0).getMessage()
        .equals("heat contains more then one vol. selecting only first vol"));
  }


}