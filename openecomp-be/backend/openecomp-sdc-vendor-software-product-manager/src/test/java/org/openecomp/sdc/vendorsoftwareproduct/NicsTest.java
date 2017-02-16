package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.core.utilities.CommonMethods;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;

public class NicsTest {

  private static final String USER1 = "nicsTestUser1";
  private static final String USER2 = "nicsTestUser2";
  private static final org.openecomp.sdc.versioning.dao.types.Version
      VERSION01 = new org.openecomp.sdc.versioning.dao.types.Version(0, 1);
  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao
      vendorSoftwareProductDao =
      org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory.getInstance().createInterface();

  private static String vsp1Id;
  private static String vsp2Id;
  private static org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity network1;
  private static org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity network2;
  private static String component11Id;
  private static String component21Id;
  private static String nic1Id = "nic1";

  static org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity createNic(String vspId, org.openecomp.sdc.versioning.dao.types.Version version, String compId, String nicId,
                                                                              String networkId) {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        nicEntity = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vspId, version, compId, nicId);
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic
        nicData = new org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic();
    nicData.setName(nicId + " name");
    nicData.setDescription(nicId + " desc");
    nicData.setNetworkId(networkId);
    nicEntity.setNicCompositionData(nicData);
    vendorSoftwareProductDao.createNic(nicEntity);
    return nicEntity;
  }

  @BeforeClass
  private void init() {
    vsp1Id = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp1", "vendorName1",
            "vlm1Id", "icon", "category", "subCategory", "123", null), USER1).getId();
    network1 = NetworksTest.createNetwork(vsp1Id, VERSION01, "network1");
    component11Id = ComponentsTest.createComponent(vsp1Id, VERSION01, "component11").getId();

    vsp2Id = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp2", "vendorName1",
            "vlm1Id", "icon", "category", "subCategory", "123", null), USER1).getId();
    network2 = NetworksTest.createNetwork(vsp2Id, VERSION01, "network2");
    component21Id = ComponentsTest.createComponent(vsp2Id, VERSION01, "component21").getId();
  }

/*    @Test
    public void testCreateNonExistingComponentId_negative() {
        testCreate_negative(new NicEntity(vsp1Id, null, "non existing component id", null), USER1, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/

  @Test
  public void testListWhenNone() {
    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> nics =
        vendorSoftwareProductManager.listNics(vsp1Id, null, component11Id, USER1);
    Assert.assertEquals(nics.size(), 0);
  }

  @Test
  public void testCreateNonExistingVspId_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity("non existing vsp id", null, component11Id, null), USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testCreateOnLockedVsp_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vsp1Id, null, component11Id, null), USER2,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
  }

//    @Test(dependsOnMethods = "testListWhenNone")
//    public void testCreate() {
//        nic1Id = testCreate(vsp1Id, component11Id, network1.getId(), network1.getNetworkCompositionData().getName());
//    }

/*    @Test(dependsOnMethods = {"testCreate"})
    public void testCreateWithExistingName_negative() {
        NicEntity nic = new NicEntity(vsp1Id, null, component11Id, null);
        Nic nicData = new Nic();
        nicData.setName("nic1 name");
        nic.setNicCompositionData(nicData);
        testCreate_negative(nic, USER1, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }*/

//    @Test(dependsOnMethods = {"testCreate"})
//    public void testCreateWithExistingNameUnderOtherComponent() {
//        ComponentEntity component12 = new ComponentEntity(vsp1Id, null, null);
//        ComponentData compData12 = new ComponentData();
//        compData12.setName("comp12 name");
//        compData12.setDescription("comp12 desc");
//        component12.setComponentCompositionData(compData12);
//
//        String component12Id = vendorSoftwareProductManager.createComponent(component12, USER1).getId();
//        testCreate(vsp1Id, component12Id, network1.getId(), network1.getNetworkCompositionData().getName());
//    }

//    @Test(dependsOnMethods = {"testCreate"})
//    public void testCreateWithExistingNameUnderOtherVsp() {
//        testCreate(vsp2Id, component21Id, network2.getId(), network2.getNetworkCompositionData().getName());
//    }

  @Test
  public void testCreateOnUploadVsp_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vsp1Id, null, component11Id, null), USER1,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test
  public void testGetNonExistingNicId_negative() {
    testGet_negative(vsp1Id, null, component11Id, "non existing nic id", USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testGetNonExistingComponentId_negative() {
    testGet_negative(vsp1Id, null, "non existing component id", nic1Id, USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testGetNonExistingVspId_negative() {
    testGet_negative("non existing vsp id", null, component11Id, nic1Id, USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testGet() {
    createNic(vsp1Id, VERSION01, component11Id, nic1Id, network1.getId());
    testGet(vsp1Id, VERSION01, component11Id, nic1Id, USER1);
  }

  @Test
  public void testUpdateNonExistingNicId_negative() {
    testUpdate_negative(vsp1Id, component11Id, "non existing nic id", USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testUpdateNonExistingComponentId_negative() {
    testUpdate_negative(vsp1Id, "non existing component id", nic1Id, USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testUpdateNonExistingVspId_negative() {
    testUpdate_negative("non existing vsp id", component11Id, nic1Id, USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = {"testGet"})
  public void testUpdateOnUploadVsp() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vsp1Id, null, component11Id, nic1Id);
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic
        nicData = new org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic();
    nicData.setName(nic1Id + " name");
    nicData.setDescription(nic1Id + " desc updated");
    nicData.setNetworkId(network1.getId());
    expected.setNicCompositionData(nicData);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateNic(expected, USER1);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    expected.setVersion(VERSION01);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        actual = vendorSoftwareProductDao.getNic(vsp1Id, VERSION01, component11Id, nic1Id);
    Assert.assertEquals(actual, expected);
  }

  @Test(dependsOnMethods = {"testGet"})
  public void testIllegalUpdateOnUploadVsp() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vsp1Id, null, component11Id, nic1Id);
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic
        nicData = new org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic();
    nicData.setName(nic1Id + " name updated");
    nicData.setDescription(nic1Id + " desc updated");
    nicData.setNetworkId(network1.getId());
    expected.setNicCompositionData(nicData);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateNic(expected, USER1);
    Assert.assertNotNull(validationData);
    Assert.assertTrue(validationData.getErrors().size() > 0);
  }

  @Test
  public void testListNonExistingComponentId_negative() {
    testList_negative(vsp1Id, null, "non existing component id", USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testListNonExistingVspId_negative() {
    testList_negative("non existing vsp id", null, component11Id, USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }
/*
    @Test(dependsOnMethods = {"testUpdateOnUploadVsp", "testList"})
    public void testCreateWithRemovedName() {
        testCreate(vsp1Id, component11Id);
    }

    @Test
    public void testDeleteNonExistingNicId_negative() {
        testDelete_negative(vsp1Id, component11Id, "non existing nic id", USER1, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testList")
    public void testDeleteNonExistingComponentId_negative() {
        testDelete_negative(vsp1Id, "non existing component id", nic1Id, USER1, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/

  @Test(dependsOnMethods = {"testGet"})
  public void testList() {
    createNic(vsp1Id, VERSION01, component11Id, "nic2", network1.getId());

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> actual =
        vendorSoftwareProductManager.listNics(vsp1Id, null, component11Id, USER1);
    Assert.assertEquals(actual.size(), 2);
  }

  @Test(dependsOnMethods = "testList")
  public void testDeleteNonExistingVspId_negative() {
    testDelete_negative("non existing vsp id", component11Id, nic1Id, USER1,
        org.openecomp.sdc.versioning.errors.VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }
/*
    @Test(dependsOnMethods = "testList")
    public void testDelete() {
        vendorSoftwareProductManager.deleteNic(vsp1Id, component11Id, nic1Id, USER1);
        NicEntity actual = vendorSoftwareProductDao.getNic(vsp1Id, VERSION01, component11Id, nic1Id);
        Assert.assertNull(actual);
    }*/

  @Test(dependsOnMethods = "testList")
  public void testDeleteOnUploadVsp_negative() {
    testDelete_negative(vsp1Id, component11Id, nic1Id, USER1,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  private String testCreate(String vspId, String componentId, String networkId,
                            String networkName) {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vspId, null, componentId, null);

    org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic
        nicData = new org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic();
    nicData.setName("nic1 name");
    nicData.setNetworkId(networkId);
    //nicData.setNetworkName(networkName);
    nicData.setNetworkType(org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType.External);
    expected.setNicCompositionData(nicData);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity created = vendorSoftwareProductManager.createNic(expected, USER1);
    Assert.assertNotNull(created);
    expected.setId(created.getId());
    expected.setVersion(VERSION01);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity actual =
        vendorSoftwareProductDao.getNic(vspId, VERSION01, componentId, created.getId());

    Assert.assertEquals(actual, expected);

    return created.getId();
  }

  private void testGet(String vspId, org.openecomp.sdc.versioning.dao.types.Version version, String componentId, String nicId,
                       String user) {
    CompositionEntityResponse<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic> response =
        vendorSoftwareProductManager.getNic(vspId, null, componentId, nicId, user);
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        expected = vendorSoftwareProductDao.getNic(vspId, version, componentId, nicId);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(response.getData(), expected.getNicCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

  private void testCreate_negative(org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nic, String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.createNic(nic, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, org.openecomp.sdc.versioning.dao.types.Version version, String componentId, String nicId,
                                String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.getNic(vspId, version, componentId, nicId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, String componentId, String nicId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.updateNic(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vspId, null, componentId, nicId), user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, org.openecomp.sdc.versioning.dao.types.Version version, String componentId, String user,
                                 String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.listNics(vspId, version, componentId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, String componentId, String nicId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.deleteNic(vspId, componentId, nicId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }
}
