package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class DeploymentFlavorManagerImplTest {
  private static final String VSP_ID = "VSP_ID";
  private static final Version VERSION = new Version("version_id");
  private static final String COMPONENT_ID = "COMPONENT_ID";
  private static final String DF1_ID = "df1";
  private static final String DF2_ID = "df2";

  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDao;
  @Mock
  private DeploymentFlavorDao deploymentFlavorDaoMock;
  @Mock
  private ComponentDao componentDaoMock;
  @Mock
  private ComputeDao computeDaoMock;
  @InjectMocks
  @Spy
  private DeploymentFlavorManagerImpl deploymentFlavorManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListWhenNone() {
    final Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorManager.listDeploymentFlavors(VSP_ID, VERSION);
    Assert.assertEquals(deploymentFlavorEntities.size(), 0);
  }

  @Test
  public void testCreateOnNotManual_negative() {

    testCreate_negative(new DeploymentFlavorEntity(VSP_ID, VERSION, null),
        VendorSoftwareProductErrorCodes.CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
  }

  @Test
  public void testCreateManualDepFlavor() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

    VspDetails vspDetails = new VspDetails(VSP_ID, VERSION);
    doReturn(vspDetails).when(vspInfoDao).get(anyObject());

    deploymentFlavorManager.createDeploymentFlavor(expected);
    verify(compositionEntityDataManagerMock).createDeploymentFlavor(expected);
  }

  @Test
  public void testCreateManualDepFlavorWithDuplicateName() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

    DeploymentFlavorEntity expectedDiffName = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    DeploymentFlavor deploymentFlavor = expectedDiffName.getDeploymentFlavorCompositionData();
    deploymentFlavor.setModel(DF1_ID + "Name");
    expectedDiffName.setDeploymentFlavorCompositionData(deploymentFlavor);
    List<DeploymentFlavorEntity> list = new ArrayList<>();
    list.add(expectedDiffName);
    doReturn(list).when(deploymentFlavorDaoMock).list(anyObject());

    try {
      deploymentFlavorManager.createDeploymentFlavor(expected);
      Assert.fail();
    } catch (CoreException ex) {
      Assert.assertEquals(
          VendorSoftwareProductErrorCodes.DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED,
          ex.code().id());
    }
  }

  @Test
  public void testCreateManualDepFlavorWithFGNotInVSP() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    final DeploymentFlavor deploymentFlavor =
        JsonUtil.json2Object(expected.getCompositionData(), DeploymentFlavor.class);
    deploymentFlavor.setFeatureGroupId("fg3");
    expected.setCompositionData(JsonUtil.object2Json(deploymentFlavor));

    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

    List<String> featureGrps = new ArrayList<>();
    featureGrps.add("fg1");
    featureGrps.add("fg2");

    VspDetails vspDetails = new VspDetails(VSP_ID, VERSION);
    vspDetails.setFeatureGroups(featureGrps);
    doReturn(vspDetails).when(vspInfoDao).get(anyObject());


    try {
      deploymentFlavorManager.createDeploymentFlavor(expected);
      Assert.fail();
    } catch (CoreException ex) {
      Assert.assertEquals(VendorSoftwareProductErrorCodes.FEATURE_GROUP_NOT_EXIST_FOR_VSP,
          ex.code().id());
    }
  }

  @Test
  public void testCreateManualDepFlavorWithNullCompInAssociation() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    final DeploymentFlavor deploymentFlavor =
        JsonUtil.json2Object(expected.getCompositionData(), DeploymentFlavor.class);
    ComponentComputeAssociation association = new ComponentComputeAssociation();
    association.setComponentId(null);
    association.setComputeFlavorId("CF1");
    List<ComponentComputeAssociation> list = new ArrayList<>();
    list.add(association);
    deploymentFlavor.setComponentComputeAssociations(list);
    expected.setCompositionData(JsonUtil.object2Json(deploymentFlavor));

    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

    VspDetails vspDetails = new VspDetails(VSP_ID, VERSION);
    doReturn(vspDetails).when(vspInfoDao).get(anyObject());

    try {
      deploymentFlavorManager.createDeploymentFlavor(expected);
    } catch (CoreException ex) {
      Assert.assertEquals(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_COMPUTE_ASSOCIATION,
          ex.code().id());
      Assert.assertEquals(
          "Invalid request,for valid association please provide ComponentId for Compute Flavor",
          ex.getMessage());
    }
  }

  @Test
  public void testCreateManualDepFlavorWithInvalidComputeInAssociation() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    final DeploymentFlavor deploymentFlavor =
        JsonUtil.json2Object(expected.getCompositionData(), DeploymentFlavor.class);
    ComponentComputeAssociation association = new ComponentComputeAssociation();
    association.setComponentId(COMPONENT_ID);
    association.setComputeFlavorId("CF1");
    List<ComponentComputeAssociation> list = new ArrayList<>();
    list.add(association);
    deploymentFlavor.setComponentComputeAssociations(list);
    expected.setCompositionData(JsonUtil.object2Json(deploymentFlavor));

    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

    VspDetails vspDetails = new VspDetails(VSP_ID, VERSION);
    doReturn(vspDetails).when(vspInfoDao).get(anyObject());

    ComponentEntity component = new ComponentEntity(VSP_ID, VERSION, null);
    doReturn(component).when(componentDaoMock).get(anyObject());

    doReturn(null).when(computeDaoMock).get(anyObject());

    try {
      deploymentFlavorManager.createDeploymentFlavor(expected);
    } catch (CoreException ex) {
      Assert.assertEquals(VendorSoftwareProductErrorCodes.INVALID_COMPUTE_FLAVOR_ID,
          ex.code().id());
    }
  }

  @Test
  public void testCreateManualDepFlavorWithDuplicateVfcAssociation() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    final DeploymentFlavor deploymentFlavor =
        JsonUtil.json2Object(expected.getCompositionData(), DeploymentFlavor.class);
    ComponentComputeAssociation association = new ComponentComputeAssociation();
    association.setComponentId(COMPONENT_ID);
    association.setComputeFlavorId("CF1");
    List<ComponentComputeAssociation> list = new ArrayList<>();
    list.add(association);
    list.add(association);
    deploymentFlavor.setComponentComputeAssociations(list);
    expected.setCompositionData(JsonUtil.object2Json(deploymentFlavor));

    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

    VspDetails vspDetails = new VspDetails(VSP_ID, VERSION);
    doReturn(vspDetails).when(vspInfoDao).get(anyObject());

    ComponentEntity component = new ComponentEntity(VSP_ID, VERSION, null);
    doReturn(component).when(componentDaoMock).get(anyObject());

    ComputeEntity computeEntity = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, "CF1");
    doReturn(computeEntity).when(computeDaoMock).get(anyObject());

    try {
      deploymentFlavorManager.createDeploymentFlavor(expected);
    } catch (CoreException ex) {
      Assert.assertEquals(
          VendorSoftwareProductErrorCodes.SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED,
          ex.code().id());
    }
  }

  @Test
  public void testList() {

    doReturn(Arrays.asList(
        createDeploymentFlavor(VSP_ID, VERSION, DF1_ID),
        createDeploymentFlavor(VSP_ID, VERSION, DF2_ID)))
        .when(deploymentFlavorDaoMock).list(anyObject());


    final Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorManager.listDeploymentFlavors(VSP_ID, VERSION);
    Assert.assertEquals(deploymentFlavorEntities.size(), 2);
    for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
      Assert.assertEquals(deploymentFlavorEntity.getDeploymentFlavorCompositionData().getModel()
          , DF1_ID.equals(deploymentFlavorEntity.getId()) ? DF1_ID + "name" : DF2_ID + "name");
    }
  }

  @Test
  public void testUpdateHeatDepFlavor() {
    testUpdate_negative(VSP_ID, VERSION, DF1_ID,
        VendorSoftwareProductErrorCodes.EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
  }

  @Test
  public void testUpdateNonExistingManualDepFlavorId_negative() {
    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());
    testUpdate_negative(VSP_ID, VERSION, DF1_ID,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testManualUpdateDepFlavor() {
    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

    doReturn(createDeploymentFlavor(VSP_ID, VERSION, DF1_ID))
        .when(deploymentFlavorDaoMock).get(anyObject());

    doReturn(new CompositionEntityValidationData(CompositionEntityType.image, DF1_ID))
        .when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(), anyObject(), anyObject());

    VspDetails vspDetails = new VspDetails(VSP_ID, VERSION);
    doReturn(vspDetails).when(vspInfoDao).get(anyObject());

    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(VSP_ID, VERSION, DF1_ID);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel(DF1_ID + "_name");
    deploymentFlavor.setDescription(DF1_ID + " desc updated");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);

    CompositionEntityValidationData validationData =
        deploymentFlavorManager.updateDeploymentFlavor(deploymentFlavorEntity);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    verify(deploymentFlavorDaoMock).update(deploymentFlavorEntity);
  }

  @Test
  public void testGetNonExistingDepFlavorId_negative() {
    testGet_negative(VSP_ID, VERSION, "non existing image id",
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  /*
  @Test
  public void testGet() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    doReturn(expected).when(deploymentFlavorDaoMock).get(anyObject());

    VspDetails vspDetails = new VspDetails(VSP_ID, VERSION);
    doReturn(vspDetails).when(vspInfoDao).get(anyObject());

    CompositionEntityResponse<DeploymentFlavor> response =
        deploymentFlavorManager.getDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert
        .assertEquals(response.getData().getModel(), expected.getDeploymentFlavorCompositionData().
            getModel());
    Assert.assertEquals(response.getData().getDescription(),
        expected.getDeploymentFlavorCompositionData().
            getDescription());
  }
*/
  @Test
  public void testDeleteDepFlavorOnHEAT() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    doReturn(expected).when(deploymentFlavorDaoMock).get(anyObject());
    testDelete_negative(VSP_ID, VERSION, DF1_ID,
        VendorSoftwareProductErrorCodes.DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
  }

  @Test
  public void testDeleteOnNotExistImage() {
    testDelete_negative(VSP_ID, VERSION, DF1_ID,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteOnManualImage() {
    DeploymentFlavorEntity expected = createDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    doReturn(expected).when(deploymentFlavorDaoMock).get(anyObject());
    doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());
    deploymentFlavorManager.deleteDeploymentFlavor(VSP_ID, VERSION, DF1_ID);
    verify(deploymentFlavorDaoMock).delete(anyObject());
  }

  private void testCreate_negative(DeploymentFlavorEntity deploymentFlavorEntity,
                                   String expectedErrorCode) {
    try {
      deploymentFlavorManager.createDeploymentFlavor(deploymentFlavorEntity);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, Version version, String deploymentFlavorId,
                                   String expectedErrorCode) {
    try {
      deploymentFlavorManager.deleteDeploymentFlavor(vspId, version, deploymentFlavorId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private static DeploymentFlavorEntity createDeploymentFlavor(String vspId, Version version,
                                                               String deploymentFlavorId) {

    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vspId, version, deploymentFlavorId);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel(deploymentFlavorId + "name");
    deploymentFlavor.setDescription(deploymentFlavorId + " desc");

    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    return deploymentFlavorEntity;
  }

  private void testUpdate_negative(String vspId, Version version, String deploymentFlavorId,
                                   String expectedErrorCode) {
    try {
      DeploymentFlavorEntity deploymentFlavorEntity =
          new DeploymentFlavorEntity(vspId, version, deploymentFlavorId);
      DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
      deploymentFlavor.setModel("Name");
      deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
      deploymentFlavorManager
          .updateDeploymentFlavor(new DeploymentFlavorEntity(vspId, version, deploymentFlavorId));
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String deploymentFlavorId,
                                String expectedErrorCode) {
    try {
      deploymentFlavorManager.getDeploymentFlavor(vspId, version, deploymentFlavorId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

}
