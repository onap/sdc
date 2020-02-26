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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ORIG_PATH_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.ActionVersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

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
    public void testCreatePackageEtsiVNF() throws IOException {
        try (
            final InputStream metadataInput = getClass()
                .getResourceAsStream("/vspmanager.csar/metadata/ValidETSItosca.meta");
            final InputStream manifestInput = getClass()
                .getResourceAsStream("/vspmanager.csar/manifest/ValidNonManoTosca.mf")) {

            final FileContentHandler handler = new FileContentHandler();
            final byte[] metadataInputBytes = IOUtils.toByteArray(metadataInput);
            handler.addFile(TOSCA_META_PATH_FILE_NAME, metadataInputBytes);
            handler.addFile(TOSCA_META_ORIG_PATH_FILE_NAME, metadataInputBytes);
            handler.addFile(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME, IOUtils.toByteArray(manifestInput));
            final ToscaServiceModel toscaMetadata = new ToscaServiceModel(handler, new HashMap<>(), "");
            when(enrichedServiceModelDaoMock.getServiceModel(any(), any())).thenReturn(toscaMetadata);
            final VspDetails vsp =
                createVspDetails("0", new Version(), "Vsp_PNF", "Test-vsp-pnf", "vendorName", "esy", "icon",
                    "category", "subCategory", "123", null);
            //want to avoid triggering populateVersionsForVlm method
            vsp.setVlmVersion(null);

            when(vspInfoDaoMock.get(any())).thenReturn(vsp);
            when(licenseArtifactsServiceMock.createLicenseArtifacts(any(), any(), any(), any()))
                .thenReturn(new FileContentHandler());
            final PackageInfo packageInfo = vendorSoftwareProductManager.createPackage("0", new Version());
            assertEquals(packageInfo.getResourceType(), ResourceTypeEnum.VF.name());
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
    public void testCreatePackageEtsiPnfWithoutNonMano() throws IOException {
        try (
            final InputStream metadataInput = getClass()
                .getResourceAsStream("/vspmanager.csar/metadata/ValidETSItosca.meta");
            final InputStream manifestInput = getClass()
                .getResourceAsStream("/vspmanager.csar/manifest/ValidNonManoToscaPnfWithoutNonMano.mf")) {

            final FileContentHandler handler = new FileContentHandler();
            final byte[] metadataInputBytes = IOUtils.toByteArray(metadataInput);
            handler.addFile(TOSCA_META_ORIG_PATH_FILE_NAME, metadataInputBytes);
            handler.addFile(TOSCA_META_PATH_FILE_NAME, metadataInputBytes);
            handler.addFile(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME, IOUtils.toByteArray(manifestInput));
            final ToscaServiceModel toscaMetadata = new ToscaServiceModel(handler, new HashMap<>(), "");
            when(enrichedServiceModelDaoMock.getServiceModel(any(), any())).thenReturn(toscaMetadata);
            final VspDetails vsp =
                createVspDetails("0", new Version(), "Vsp_PNF", "Test-vsp-pnf", "vendorName", "esy", "icon",
                    "category", "subCategory", "123", null);
            //want to avoid triggering populateVersionsForVlm method
            vsp.setVlmVersion(null);

            when(vspInfoDaoMock.get(any())).thenReturn(vsp);
            when(licenseArtifactsServiceMock.createLicenseArtifacts(any(), any(), any(), any()))
                .thenReturn(new FileContentHandler());
            final PackageInfo packageInfo = vendorSoftwareProductManager.createPackage("0", new Version());
            assertEquals(packageInfo.getResourceType(), ResourceTypeEnum.PNF.name());
        }
    }

    @Test
    public void testCreatePackageEtsiPnfWithNonManoArtifacts() throws IOException {
        try (
            final InputStream metadataInput = getClass()
                .getResourceAsStream("/vspmanager.csar/metadata/ValidETSItosca.meta");
            final InputStream manifestInput = getClass()
                .getResourceAsStream("/vspmanager.csar/manifest/ValidNonManoToscaPNFWithNonMano.mf");
            final InputStream mainServiceTemplateYamlFile = getClass()
                .getResourceAsStream("/vspmanager.csar/descriptor/MainServiceTemplate.yaml")) {

            final FileContentHandler handler = new FileContentHandler();
            handler.addFile(TOSCA_META_ORIG_PATH_FILE_NAME, IOUtils.toByteArray(metadataInput));
            handler.addFile(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME, IOUtils.toByteArray(manifestInput));
            handler.addFile("Deployment/ANOTHER/authorized_keys", "".getBytes());

            final ServiceTemplate mainServiceTemplate = new YamlUtil()
                .yamlToObject(mainServiceTemplateYamlFile, ServiceTemplate.class);
            final String mainServiceTemplateName = "MainServiceTemplate.yaml";
            final HashMap<String, ServiceTemplate> serviceTemplateMap = new HashMap<>();
            serviceTemplateMap.put(mainServiceTemplateName, mainServiceTemplate);

            final ToscaServiceModel toscaMetadata = new ToscaServiceModel(handler, serviceTemplateMap,
                mainServiceTemplateName);
            when(enrichedServiceModelDaoMock.getServiceModel(any(), any())).thenReturn(toscaMetadata);
            final VspDetails vsp =
                createVspDetails("0", new Version(), "Vsp_PNF", "Test-vsp-pnf", "vendorName", "esy", "icon",
                    "category", "subCategory", "123", null);
            //want to avoid triggering populateVersionsForVlm method
            vsp.setVlmVersion(null);

            when(vspInfoDaoMock.get(any())).thenReturn(vsp);
            when(licenseArtifactsServiceMock.createLicenseArtifacts(any(), any(), any(), any()))
                .thenReturn(new FileContentHandler());
            final PackageInfo packageInfo = vendorSoftwareProductManager.createPackage("0", new Version());
            assertThat("Package Info should contain resource type", packageInfo.getResourceType(),
                equalTo(ResourceTypeEnum.PNF.name()));
            assertThat("Should not contain moved artifact", toscaMetadata.getArtifactFiles().getFileList(),
                not(hasItem("Deployment/ANOTHER/authorized_keys")));
            assertThat("Should contain moved artifact", toscaMetadata.getArtifactFiles().getFileList(),
                hasItem("Informational/OTHER/authorized_keys"));
            final String serviceTemplateAsYaml = new YamlUtil().objectToYaml(toscaMetadata.getServiceTemplates());
            assertThat("Descriptor should not contain reference to file", serviceTemplateAsYaml,
                not(containsString("Artifacts/Deployment/ANOTHER/authorized_keys")));
            assertThat("Descriptor should contain reference to file", serviceTemplateAsYaml,
                containsString("Artifacts/Informational/OTHER/authorized_keys"));
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
    Assert.assertEquals(1, validationResponse.getVspErrors().size());

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
    Assert.assertEquals(1, validationResponse.getVspErrors().size());
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

}
