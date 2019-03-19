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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.ManualVspToscaManager;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.mock.EnrichmentManagerFactoryImpl;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.ActionVersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

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
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_PATH_FILE_NAME;



public class VendorSoftwareProductManagerImplTest {

  private static final String VSP_ID = "vspId";
  private static final Version VERSION01 = new Version("0, 1");
  private static final Version VERSION10 = new Version("1, 0");
  private static final String USER1 = "vspTestUser1";
  private static final String USER2 = "vspTestUser2";

  @Mock
  private ActionVersioningManager versioningManagerMock;
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

  @Mock
  private OrchestrationTemplateCandidateManager orchestrationTemplateCandidateManagerMock;
  @Mock
  private ComponentDependencyModelDao componentDependencyModelDao;
  private OrchestrationTemplateCandidateManager candidateManager;
  private MonitoringUploadsManager monitoringUploadsManager;

  @Captor
  private ArgumentCaptor<ActivityLogEntity> activityLogEntityArg;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    vendorSoftwareProductManager = null;
  }

  @Test
  public void testCreatePackageEtsiVNF(){
    try(InputStream metadataInput = getClass().getResourceAsStream("/vspmanager.csar/metadata/ValidETSItosca.meta");
        InputStream manifestInput = getClass().getResourceAsStream("/vspmanager.csar/manifest/ValidNonManoTosca.mf")) {

      FileContentHandler handler = new FileContentHandler();
      handler.addFile(TOSCA_META_PATH_FILE_NAME, IOUtils.toByteArray(metadataInput));
      handler.addFile(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME, IOUtils.toByteArray(manifestInput));
      ToscaServiceModel toscaMetadata = new ToscaServiceModel(handler, new HashMap<>(), "");
      when(enrichedServiceModelDaoMock.getServiceModel(any(), any())).thenReturn(toscaMetadata );
      VspDetails vsp =
              createVspDetails("0", new Version(), "Vsp_PNF", "Test-vsp-pnf", "vendorName", "esy", "icon",
                      "category", "subCategory", "123", null);
      //want to avoid triggering populateVersionsForVlm method
      vsp.setVlmVersion(null);

      when(vspInfoDaoMock.get(any())).thenReturn(vsp);
      when(licenseArtifactsServiceMock.createLicenseArtifacts(any(),any(), any(), any())).thenReturn(new FileContentHandler());
      PackageInfo packageInfo = vendorSoftwareProductManager.createPackage("0", new Version());
      assertEquals(packageInfo.getResourceType(), ResourceTypeEnum.VF.name());
    } catch (IOException e) {
      fail();
    }
  }

  @Test(expected = IOException.class)
  public void testCreatePackageEtsiNoManifest() throws IOException {
    try(InputStream metadataInput = getClass().getResourceAsStream("/vspmanager.csar/metadata/ValidETSItosca.meta"))
    {
      FileContentHandler handler = new FileContentHandler();
      handler.addFile(TOSCA_META_PATH_FILE_NAME, IOUtils.toByteArray(metadataInput));
      ToscaServiceModel toscaMetadata = new ToscaServiceModel(handler, new HashMap<>(), "");
      when(enrichedServiceModelDaoMock.getServiceModel(any(), any())).thenReturn(toscaMetadata );
      VspDetails vsp =
              createVspDetails("0", new Version(), "Vsp_PNF", "Test-vsp-pnf", "vendorName", "esy", "icon",
                      "category", "subCategory", "123", null);
      //want to avoid triggering populateVersionsForVlm method
      vsp.setVlmVersion(null);

      when(vspInfoDaoMock.get(any())).thenReturn(vsp);
      when(licenseArtifactsServiceMock.createLicenseArtifacts(any(),any(), any(), any())).thenReturn(new FileContentHandler());
      vendorSoftwareProductManager.createPackage("0", new Version());
      fail();
    }
  }

  @Test
  public void testCreatePackageEtsiPNF(){
    try(InputStream metadataInput = getClass().getResourceAsStream("/vspmanager.csar/metadata/ValidETSItosca.meta");
        InputStream manifestInput = getClass().getResourceAsStream("/vspmanager.csar/manifest/ValidNonManoToscaPNF.mf")) {

      FileContentHandler handler = new FileContentHandler();
      handler.addFile(TOSCA_META_PATH_FILE_NAME, IOUtils.toByteArray(metadataInput));
      handler.addFile(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME, IOUtils.toByteArray(manifestInput));
      ToscaServiceModel toscaMetadata = new ToscaServiceModel(handler, new HashMap<>(), "");
      when(enrichedServiceModelDaoMock.getServiceModel(any(), any())).thenReturn(toscaMetadata );
      VspDetails vsp =
              createVspDetails("0", new Version(), "Vsp_PNF", "Test-vsp-pnf", "vendorName", "esy", "icon",
                      "category", "subCategory", "123", null);
      //want to avoid triggering populateVersionsForVlm method
      vsp.setVlmVersion(null);

      when(vspInfoDaoMock.get(any())).thenReturn(vsp);
      when(licenseArtifactsServiceMock.createLicenseArtifacts(any(),any(), any(), any())).thenReturn(new FileContentHandler());
      PackageInfo packageInfo = vendorSoftwareProductManager.createPackage("0", new Version());
      assertEquals(packageInfo.getResourceType(), ResourceTypeEnum.PNF.name());
    } catch (IOException e) {
      fail();
    }
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

  @Test(expected = CoreException.class)
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
    List<String> fgs = new ArrayList<>();
    fgs.add("fg1");
    fgs.add("fg2");
    VspDetails existingVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1", null, "vendorName", "vlm1Id", "icon",
            "category",
            "subCategory", "456", fgs);

    List<String> updFgs = new ArrayList<>();
    //updFgs.add("fg2");
    VspDetails updatedVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1_updated", null, "vendorName", "vlm1Id", "icon",
            "category_updated",
            "subCategory", "456", updFgs);
    doReturn(existingVsp).when(vspInfoDaoMock)
        .get(any(VspDetails.class));
    doNothing().when(vendorSoftwareProductManager)
        .updateUniqueName(existingVsp.getName(), updatedVsp.getName());

    DeploymentFlavorEntity dfEntity = new DeploymentFlavorEntity(VSP_ID, VERSION01, "DF_ID");
    DeploymentFlavor flavor = new DeploymentFlavor();
    flavor.setFeatureGroupId("fg1");
    dfEntity.setDeploymentFlavorCompositionData(flavor);

    List<DeploymentFlavorEntity> dfList = new ArrayList<>();
    dfList.add(dfEntity);

    doReturn(dfList).when(deploymentFlavorDaoMock).list(anyObject());

    vendorSoftwareProductManager.updateVsp(updatedVsp);

    verify(deploymentFlavorDaoMock).update(dfEntity);

    Assert.assertNull(dfEntity.getDeploymentFlavorCompositionData().getFeatureGroupId());

  }

  @Test(expected = CoreException.class)
  public void testGetNonExistingVersion_negative() {
    Version notExistversion = new Version("43, 8");
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
    doReturn(existingVsp).when(vspInfoDaoMock).get(any(VspDetails.class));

    VspDetails actualVsp =
        vendorSoftwareProductManager.getVsp(VSP_ID, VERSION01);

    assertVspsEquals(actualVsp, existingVsp);
  }

  @Test
  public void testGetOldVersion() {
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(new Version("0, 2"));
    versionInfo.setViewableVersions(Arrays.asList(VERSION01, new Version("0, 2")));
    versionInfo.setStatus(VersionStatus.Locked);
    versionInfo.setLockingUser(USER2);
    doReturn(versionInfo).when(versioningManagerMock).getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, VSP_ID, USER1,
        VersionableEntityAction.Read);

    VspDetails existingVsp =
        createVspDetails(VSP_ID, VERSION01, "VSP1", null, "vendorName", "vlm1Id", "icon",
            "category",
            "subCategory", "456", null);
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
        vendorSoftwareProductManager.validate(vsp);
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
        vendorSoftwareProductManager.validate(vsp);
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
  public void testUploadNotZipFile() {
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
  @Test
  public void testValidateWithCandidateDataNotProcessed() throws IOException {
    VspDetails vsp =
        createVspDetails(VSP_ID, VERSION01, "Vsp1", "Test-vsp", "vendorName", "vlm1Id", "icon",
            "category", "subCategory", "licenseAgreementId",
            Collections.singletonList("featureGroupId"));
    vsp.setOnboardingMethod("NetworkPackage");
    doReturn(vsp).when(vspInfoDaoMock).get(anyObject());

    OrchestrationTemplateCandidateData orchestrationTemplateCandidateData = new
        OrchestrationTemplateCandidateData();
    orchestrationTemplateCandidateData.setFileSuffix("zip");
    orchestrationTemplateCandidateData.setFilesDataStructure("testdata");
    orchestrationTemplateCandidateData.setValidationData("");
    doReturn(Optional.of(orchestrationTemplateCandidateData))
        .when(orchestrationTemplateCandidateManagerMock)
        .getInfo(VSP_ID, VERSION01);
    ValidationResponse validationResponse =
        vendorSoftwareProductManager.validate(vsp);
    Assert.assertNotNull(validationResponse);
    Assert.assertFalse(validationResponse.isValid());
    Assert.assertNotNull(validationResponse.getVspErrors());
    Assert.assertEquals(validationResponse.getVspErrors().size(), 1);

  }

  @Test
  public void testValidateWithCandidateProcessedIsInvalid() throws IOException {
    VspDetails vsp = createVspDetails(VSP_ID, VERSION01, "Vsp1", "Test-VSP", "vendorName",
        "vl1Id", "icond", "category", "subcategory", "licenseAgreementId", Collections
            .singletonList("featureGroupId"));
    vsp.setOnboardingMethod("NetworkPackage");
    doReturn(vsp).when(vspInfoDaoMock).get(anyObject());

    OrchestrationTemplateCandidateData orchestrationTemplateCandidateData = new
        OrchestrationTemplateCandidateData();
    orchestrationTemplateCandidateData.setFileSuffix("zip");
    orchestrationTemplateCandidateData.setValidationData("Invalid processed data");
    doReturn(Optional.of(orchestrationTemplateCandidateData))
        .when(orchestrationTemplateCandidateManagerMock)
        .getInfo(VSP_ID, VERSION01);
    ValidationResponse validationResponse =
        vendorSoftwareProductManager.validate(vsp);
    Assert.assertNotNull(validationResponse);
    Assert.assertFalse(validationResponse.isValid());
    Assert.assertNotNull(validationResponse.getVspErrors());
    Assert.assertEquals(validationResponse.getVspErrors().size(), 1);
  }

  private void testLegalUpload(String vspId, Version version, InputStream upload, String user) {
    candidateManager.upload(vspId, VERSION01, upload, "zip", "file");
    candidateManager.process(vspId, VERSION01);

    OrchestrationTemplateEntity uploadData =
        orchestrationTemplateDataDaoMock.get(vspId, version);
    Assert.assertNotNull(uploadData);
  }


  private InputStream getFileInputStream(String fileName) {
    URL url = this.getClass().getResource(fileName);
    try {
      return url.openStream();
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private static VspDetails createVspDetails(String id, Version version, String name, String desc,
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
    vspDetails.setVlmVersion(new Version("1, 0"));
    vspDetails.setLicenseAgreement(licenseAgreement);
    vspDetails.setFeatureGroups(featureGroups);
    vspDetails.setOnboardingMethod("HEAT");
    return vspDetails;
  }

  private static void assertVspsEquals(VspDetails actual, VspDetails expected) {
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