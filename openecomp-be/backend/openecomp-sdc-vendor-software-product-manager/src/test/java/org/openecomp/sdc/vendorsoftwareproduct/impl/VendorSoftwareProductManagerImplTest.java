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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;
import org.openecomp.core.factory.impl.AbstractFactoryBase;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorsoftwareproduct.ManualVspToscaManager;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.mock.EnrichmentManagerFactoryImpl;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.errors.EditOnEntityLockedByOtherErrorBuilder;
import org.openecomp.sdc.versioning.errors.EntityNotExistErrorBuilder;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


public class VendorSoftwareProductManagerImplTest {
  private static final String INVALID_VERSION_MSG = "Invalid requested version.";

  private static String VSP_ID = "vspId";
  private static String VERSION_ID = "versionId";
  public static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION10 = new Version(1, 0);
  private static final String USER1 = "vspTestUser1";
  private static final String USER2 = "vspTestUser2";
  private static final String USER3 = "vspTestUser3";
  private static String id006 = null;
  private static String id007 = null;

  @Mock
  private VersioningManager versioningManagerMock;
  @Mock
  private OrchestrationTemplateDao orchestrationTemplateDataDaoMock;
  @Mock
  private VendorLicenseFacade vendorLicenseFacadeMock;
  @Mock
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDaoMock;
  @Mock
  private EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDaoMock;
  @Mock
  private HealingManager healingManagerMock;
  @Mock
  private VendorLicenseArtifactsService licenseArtifactsServiceMock;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private InformationArtifactGenerator informationArtifactGeneratorMock;
  @Mock
  private PackageInfoDao packageInfoDao;
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDaoMock;
  @Mock
  private ManualVspToscaManager manualVspToscaManager;
  @Mock
  private DeploymentFlavorDao deploymentFlavorDaoMock;


  @Spy
  @InjectMocks
  private VendorSoftwareProductManagerImpl vendorSoftwareProductManager;

  private OrchestrationTemplateCandidateManager candidateManager;
  private MonitoringUploadsManager monitoringUploadsManager;

  @Captor
  private ArgumentCaptor<ActivityLogEntity> activityLogEntityArg;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @AfterMethod
  public void tearDown(){
    vendorSoftwareProductManager = null;
  }


  @Test
  public void testCreate() {
    //doReturn(VERSION01).when(versioningManagerMock).create(anyObject(), anyObject(), anyObject());
    doReturn("{}")
        .when(vendorSoftwareProductManager).getVspQuestionnaireSchema(anyObject());

    VspDetails vspToCreate =
        createVspDetails(null, null, "Vsp1", "Test-vsp", "vendorName", "vlm1Id", "icon",
            "category", "subCategory", "123", null);

    VspDetails vsp = vendorSoftwareProductManager.createVsp(vspToCreate);

    Assert.assertNotNull(vsp);
    vspToCreate.setId(vsp.getId());
    vspToCreate.setVersion(VERSION01);
    assertVspsEquals(vsp, vspToCreate);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUpdateWithExistingName_negative() {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        VersionableEntityAction.Write);

    VspDetails existingVsp =
        createVspDetails(VSP_ID, VERSION01, "Vsp1", "Test-existingVsp", "vendorName", "vlm1Id",
            "icon", "category", "subCategory", "123", null);
    VspDetails updatedVsp =
        createVspDetails(VSP_ID, VERSION01, "Vsp1_updated", "Test-existingVsp", "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null);
    doReturn(existingVsp).when(vspInfoDaoMock)
        .get(any(VspDetails.class));
    doThrow(new CoreException(
        new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).build()))
        .when(vendorSoftwareProductManager)
        .updateUniqueName(existingVsp.getName(), updatedVsp.getName());

    vendorSoftwareProductManager.updateVsp(updatedVsp);
  }

  @Test
  public void testUpdate() {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        VersionableEntityAction.Write);
    VspDetails existingVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1", null, "vendorName", "vlm1Id", "icon",
            "category",
            "subCategory", "456", null);
    VspDetails updatedVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1_updated", null, "vendorName", "vlm1Id", "icon",
            "category_updated",
            "subCategory", "456", null);
    existingVsp.setWritetimeMicroSeconds(8L);
    doReturn(existingVsp).when(vspInfoDaoMock)
        .get(any(VspDetails.class));
    doNothing().when(vendorSoftwareProductManager)
        .updateUniqueName(existingVsp.getName(), updatedVsp.getName());

    vendorSoftwareProductManager.updateVsp(updatedVsp);

    verify(vspInfoDaoMock).update(updatedVsp);
  }

  @Test
  public void testUpdateRemoveFG() {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        VersionableEntityAction.Write);
    List<String> fgs = new ArrayList<String>();
    fgs.add("fg1");
    fgs.add("fg2");
    VspDetails existingVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1", null, "vendorName", "vlm1Id", "icon",
            "category",
            "subCategory", "456", fgs);

    List<String> updFgs = new ArrayList<String>();
    //updFgs.add("fg2");
    VspDetails updatedVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1_updated", null, "vendorName", "vlm1Id", "icon",
            "category_updated",
            "subCategory", "456", updFgs);
    existingVsp.setWritetimeMicroSeconds(8L);
    doReturn(existingVsp).when(vspInfoDaoMock)
        .get(any(VspDetails.class));
    doNothing().when(vendorSoftwareProductManager)
        .updateUniqueName(existingVsp.getName(), updatedVsp.getName());

    DeploymentFlavorEntity dfEntity = new DeploymentFlavorEntity(VSP_ID, VERSION01, "DF_ID");
    DeploymentFlavor flavor = new DeploymentFlavor();
    flavor.setFeatureGroupId("fg1");
    dfEntity.setDeploymentFlavorCompositionData(flavor);

    List<DeploymentFlavorEntity> dfList = new ArrayList<DeploymentFlavorEntity>();
    dfList.add(dfEntity);

    doReturn(dfList).when(deploymentFlavorDaoMock).list(anyObject());

    vendorSoftwareProductManager.updateVsp(updatedVsp);

    verify(deploymentFlavorDaoMock).update(dfEntity);

    Assert.assertNull(dfEntity.getDeploymentFlavorCompositionData().getFeatureGroupId());

  }

  @Test(expectedExceptions = CoreException.class)
  public void testGetNonExistingVersion_negative() {
    Version notExistversion = new Version(43, 8);
    doReturn(null).when(vspInfoDaoMock).get(any(VspDetails.class));
    vendorSoftwareProductManager.getVsp(VSP_ID, notExistversion);
  }

  @Test
  public void testGetCheckedOutVersion() {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);
    versionInfo.setStatus(VersionStatus.Locked);
    versionInfo.setLockingUser(USER1);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        VersionableEntityAction.Read);

    VspDetails existingVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1", null, "vendorName", "vlm1Id", "icon",
            "category",
            "subCategory", "456", null);
    existingVsp.setWritetimeMicroSeconds(8L);
    doReturn(existingVsp).when(vspInfoDaoMock).get(any(VspDetails.class));

    VspDetails actualVsp =
        vendorSoftwareProductManager.getVsp(VSP_ID, VERSION01);

    assertVspsEquals(actualVsp, existingVsp);
  }

  @Test
  public void testGetOldVersion() {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(new Version(0, 2));
    versionInfo.setViewableVersions(Arrays.asList(VERSION01, new Version(0, 2)));
    versionInfo.setStatus(VersionStatus.Locked);
    versionInfo.setLockingUser(USER2);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        VersionableEntityAction.Read);

    VspDetails existingVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1", null, "vendorName", "vlm1Id", "icon",
            "category",
            "subCategory", "456", null);
    existingVsp.setWritetimeMicroSeconds(8L);
    doReturn(existingVsp)
        .when(vspInfoDaoMock).get(any(VspDetails.class));

    VspDetails actualVsp =
        vendorSoftwareProductManager.getVsp(VSP_ID, VERSION01);

    VspDetails expectedVsp =
        vspInfoDaoMock
            .get(new VspDetails(VSP_ID, VERSION01));
    assertVspsEquals(actualVsp, expectedVsp);
  }

/*
  @Test
  public void testSubmitWithMissingData() throws IOException {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);

    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        VSP_ID, USER1, VersionableEntityAction.Read);

    VspDetails vsp = new VspDetails(VSP_ID, VERSION01);
    vsp.setOnboardingMethod("Manual");
    doReturn(vsp).when(vspInfoDaoMock).get(anyObject());

    VspQuestionnaireEntity vspQuestionnaire = new VspQuestionnaireEntity(VSP_ID, VERSION01);
    vspQuestionnaire.setQuestionnaireData("{}");
    doReturn(vspQuestionnaire).when(vspInfoDaoMock).getQuestionnaire(VSP_ID, VERSION01);

    ComponentEntity comp1 = new ComponentEntity(VSP_ID, VERSION01, "comp1");
    comp1.setQuestionnaireData("{}");
    doReturn(Collections.singleton(comp1)).when(vendorSoftwareProductDaoMock)
        .listComponentsCompositionAndQuestionnaire(VSP_ID, VERSION01);

    NicEntity nic1 = new NicEntity(VSP_ID, VERSION01, "comp1", "nic1");
    nic1.setQuestionnaireData("{}");
    doReturn(Collections.singleton(nic1))
        .when(vendorSoftwareProductDaoMock).listNicsByVsp(VSP_ID, VERSION01);

    ValidationResponse validationResponse = vendorSoftwareProductManager.submit(VSP_ID, USER1);
    Assert.assertNotNull(validationResponse);
    Assert.assertFalse(validationResponse.isValid());
    List<String> errorIds = validationResponse.getVspErrors().stream().map(ErrorCode::id).distinct()
        .collect(Collectors.toList());
    Assert.assertTrue(errorIds.contains(ValidationErrorBuilder.FIELD_VALIDATION_ERROR_ERR_ID));
    Assert.assertTrue(errorIds.contains(VendorSoftwareProductErrorCodes.VSP_INVALID));

    verify(versioningManagerMock, never())
        .submit(VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID,
            USER1, null);
    verify(activityLogManagerMock, never()).addActionLog(any(ActivityLogEntity.class), eq(USER1));
  }

  */

  // TODO: 3/15/2017 fix and enable
  //@Test
  public void testSubmitWithInvalidLicensingData() throws IOException {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        VSP_ID, USER1, VersionableEntityAction.Read);

    VspDetails vsp =
        createVspDetails(VSP_ID, VERSION01, "Vsp1", "Test-vsp", "vendorName", "vlm1Id", "icon",
            "category", "subCategory", "licenseAgreementId",
            Collections.singletonList("featureGroupId"));
    doReturn(vsp).when(vspInfoDaoMock).get(anyObject());
    OrchestrationTemplateEntity uploadData = new OrchestrationTemplateEntity(VSP_ID, VERSION01);
    uploadData.setContentData(
        ByteBuffer.wrap(FileUtils.toByteArray(getFileInputStream("/emptyComposition"))));
    doReturn(uploadData).when(orchestrationTemplateDataDaoMock)
        .get(anyObject(), anyObject());
    doReturn(new ToscaServiceModel(new FileContentHandler(), new HashMap<>(),
        "MainServiceTemplate.yaml"))
        .when(serviceModelDaoMock).getServiceModel(VSP_ID, VERSION01);

    ValidationResponse validationResponse =
        vendorSoftwareProductManager.validate(VSP_ID, VERSION01);
    Assert.assertNotNull(validationResponse);
    Assert.assertFalse(validationResponse.isValid());
    Assert.assertNull(validationResponse.getVspErrors());
    Assert.assertEquals(validationResponse.getLicensingDataErrors().size(), 1);

    verify(versioningManagerMock, never())
        .submit(VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID,
            USER1, null);
  }

  // TODO: 3/15/2017 fix and enable
  //@Test
  public void testSubmit() throws IOException {
    mockVersioning(VersionableEntityAction.Read);

    EnrichmentManagerFactory.getInstance();
    AbstractFactoryBase
        .registerFactory(EnrichmentManagerFactory.class, EnrichmentManagerFactoryImpl.class);

    VspDetails vsp =
        createVspDetails(VSP_ID, VERSION01, "Vsp1", "Test-vsp", "vendorName", "vlm1Id", "icon",
            "category", "subCategory", "123", Collections.singletonList("fg1"));
    doReturn(vsp).when(vspInfoDaoMock).get(anyObject());
    OrchestrationTemplateEntity uploadData = new OrchestrationTemplateEntity(VSP_ID, VERSION01);
    uploadData.setContentData(
        ByteBuffer.wrap(FileUtils.toByteArray(getFileInputStream("/emptyComposition"))));
    doReturn(uploadData).when(orchestrationTemplateDataDaoMock)
        .get(anyObject(), anyObject());
    doReturn(new ToscaServiceModel(new FileContentHandler(), new HashMap<>(),
        "MainServiceTemplate.yaml"))
        .when(serviceModelDaoMock).getServiceModel(VSP_ID, VERSION01);

    ValidationResponse validationResponse =
        vendorSoftwareProductManager.validate(VSP_ID, VERSION01);
    Assert.assertTrue(validationResponse.isValid());

/*    Assert.assertEquals(vsp2.getVersionInfo().getVersion(), VERSION10);
    Assert.assertEquals(vsp2.getVersionInfo().getStatus(), VersionStatus.Certified);
    Assert.assertNull(vsp2.getVersionInfo().getLockingUser());*/

    verify(versioningManagerMock)
        .submit(VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID,
            USER1, null);
  }

  @Test
  public void testCreatePackage() throws IOException {
    /*VspDetails vspDetailsMock = new VspDetails("vspId", new Version(1, 0));
    doReturn(vspDetailsMock).when(vspInfoDaoMock).get(anyObject());*/
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION10);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        VersionableEntityAction.Read);

    doReturn(new ToscaServiceModel(new FileContentHandler(), new HashMap<>(), "")).when
        (enrichedServiceModelDaoMock).getServiceModel(VSP_ID, VERSION10);
    doNothing().when(vendorSoftwareProductManager).populateVersionsForVlm(anyObject(), anyObject());
    VspDetails vsp = new VspDetails(VSP_ID, VERSION10);
    vsp.setVendorId("vendorId");
    vsp.setVlmVersion(VERSION10);
    vsp.setFeatureGroups(Arrays.asList("fg1", "fg2"));
    doReturn(vsp).when(vspInfoDaoMock).get(any(VspDetails.class));

    doReturn(new FileContentHandler()).when(licenseArtifactsServiceMock)
        .createLicenseArtifacts(VSP_ID, vsp.getVendorId(), VERSION10, vsp.getFeatureGroups()
        );

    PackageInfo packageInfo = vendorSoftwareProductManager.createPackage(VSP_ID, VERSION10);
    Assert.assertNotNull(packageInfo.getVspId());
  }

  // TODO: 3/15/2017 fix and enable
  //@Test(dependsOnMethods = {"testListFinals"})
  public void testUploadFileMissingFile() throws IOException {
    try (InputStream zis = getFileInputStream("/vspmanager/zips/missingYml.zip")) {

      UploadFileResponse uploadFileResponse =
              candidateManager.upload(VSP_ID, VERSION01, zis, "zip", "file");

      Assert.assertEquals(uploadFileResponse.getErrors().size(), 0);
    }
  }

  // TODO: 3/15/2017 fix and enable
  //@Test(dependsOnMethods = {"testUploadFileMissingFile"})
  public void testUploadNotZipFile() throws IOException {
    URL url = this.getClass().getResource("/notZipFile");

    try {
      candidateManager.upload(VSP_ID, VERSION01, url.openStream(), "zip", "file");
      candidateManager.process(VSP_ID, VERSION01);
    } catch (Exception ce) {
      Assert.assertEquals(ce.getMessage(), Messages.CREATE_MANIFEST_FROM_ZIP.getErrorMessage());
    }
  }

  private List<String> getWantedFileNamesFromCsar(String pathInCsar)
      throws IOException {
    File translatedFile = vendorSoftwareProductManager.getTranslatedFile(VSP_ID, VERSION10);

    return getFileNamesFromFolderInCsar(translatedFile,
        pathInCsar);
  }

  private List<String> getFileNamesFromFolderInCsar(File csar, String folderName)
      throws IOException {
    List<String> fileNames = new ArrayList<>();

    try (ZipInputStream zip = new ZipInputStream(new FileInputStream(csar))) {
      ZipEntry ze;

      while ((ze = zip.getNextEntry()) != null) {
        String name = ze.getName();
        if (name.contains(folderName)) {
          fileNames.add(name);
        }
      }
    }

    return fileNames;
  }
  /*
  //Disabled for sonar null pointer issue for componentEntities
  private Pair<String, String> uploadMib(String vspId, String user, String filePath,
                                         String fileName) {
    List<ComponentEntity> componentEntities = null;
    //(List<ComponentEntity>) vendorSoftwareProductManager.listComponents(vspId, null, user);
    monitoringUploadsManager.upload(getFileInputStream(filePath),
        fileName, vspId,
<<<<<<< HEAD
        VERSION01, componentEntities.get(0).getId(), ArtifactType.SNMP_POLL);
    //TODO: add validate of logActivity() func call
=======
        VERSION01, componentEntities.get(0).getId(), MonitoringUploadType.SNMP_POLL, user);
    //TODO: add validate of addActionLog() func call
>>>>>>> feature/Amdocs-ASDC-1710

    return new ImmutablePair<>(componentEntities.get(0).getId(),
        componentEntities.get(0).getComponentCompositionData()
            .getDisplayName());
  }*/

  // TODO: 3/15/2017 fix and enable
/*

  public void testUpdatedVSPShouldBeInBeginningOfList() {
    vendorSoftwareProductManager.updateVsp(new VspDetails(), USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);

    InputStream zis = getFileInputStream("/vspmanager/zips/fullComposition.zip");
    candidateManager.upload(id007, VERSION01, zis, USER3);
    candidateManager.process(id007, VERSION01, USER3);
    assertVSPInWantedLocationInVSPList(id007, 0, USER3);
  }

  @Test(dependsOnMethods = {"testUpdatedVSPShouldBeInBeginningOfList"})
  public void testVSPInBeginningOfListAfterCheckin() {
    vendorSoftwareProductManager.checkin(id006, USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);

    vendorSoftwareProductManager.checkin(id007, USER3);
    assertVSPInWantedLocationInVSPList(id007, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVSPInBeginningOfListAfterCheckin"})
  public void testVSPInBeginningOfListAfterCheckout() {
    vendorSoftwareProductManager.checkout(id006, USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVSPInBeginningOfListAfterCheckout"})
  public void testVSPInBeginningOfListAfterUndoCheckout() {
    vendorSoftwareProductManager.checkout(id007, USER3);
    assertVSPInWantedLocationInVSPList(id007, 0, USER3);

    vendorSoftwareProductManager.undoCheckout(id006, USER3);
    assertVSPInWantedLocationInVSPList(id006, 0, USER3);
  }

  @Test(dependsOnMethods = {"testVSPInBeginningOfListAfterUndoCheckout"})
  public void testVSPInBeginningOfListAfterSubmit() throws IOException {
    vendorSoftwareProductManager.checkin(id007, USER3);
    vendorSoftwareProductManager.submit(id007, USER3);

    assertVSPInWantedLocationInVSPList(id007, 0, USER3);
  }
*/

  private void testLegalUpload(String vspId, Version version, InputStream upload, String user) {
    candidateManager.upload(vspId, VERSION01, upload, "zip", "file");
    candidateManager.process(vspId, VERSION01);

    OrchestrationTemplateEntity uploadData =
        orchestrationTemplateDataDaoMock.get(vspId, version);
    Assert.assertNotNull(uploadData);
  }


  public InputStream getFileInputStream(String fileName) {
    URL url = this.getClass().getResource(fileName);
    try {
      return url.openStream();
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  static VspDetails createVspDetails(String id, Version version, String name, String desc,
                                     String vendorName, String vlm, String icon,
                                     String category, String subCategory,
                                     String licenseAgreement, List<String> featureGroups) {
    VspDetails vspDetails = new VspDetails(id, version);
    vspDetails.setName(name);
    vspDetails.setDescription(desc);
    vspDetails.setIcon(icon);
    vspDetails.setCategory(category);
    vspDetails.setSubCategory(subCategory);
    vspDetails.setVendorName(vendorName);
    vspDetails.setVendorId(vlm);
    vspDetails.setVlmVersion(new Version(1, 0));
    vspDetails.setLicenseAgreement(licenseAgreement);
    vspDetails.setFeatureGroups(featureGroups);
    vspDetails.setOnboardingMethod("HEAT");
    return vspDetails;
  }

  static void assertVspsEquals(VspDetails actual, VspDetails expected) {
    Assert.assertEquals(actual.getId(), expected.getId());
    Assert.assertEquals(actual.getVersion(), expected.getVersion());
    Assert.assertEquals(actual.getName(), expected.getName());
    Assert.assertEquals(actual.getDescription(), expected.getDescription());
    Assert.assertEquals(actual.getIcon(), expected.getIcon());
    Assert.assertEquals(actual.getCategory(), expected.getCategory());
    Assert.assertEquals(actual.getSubCategory(), expected.getSubCategory());
    Assert.assertEquals(actual.getVendorName(), expected.getVendorName());
    Assert.assertEquals(actual.getVendorId(), expected.getVendorId());
    Assert.assertEquals(actual.getLicenseAgreement(), expected.getLicenseAgreement());
    Assert.assertEquals(actual.getFeatureGroups(), expected.getFeatureGroups());
  }


  // todo ********************** move to common **************************************

  private void mockVersioning(VersionableEntityAction action) {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        action);
  }

}