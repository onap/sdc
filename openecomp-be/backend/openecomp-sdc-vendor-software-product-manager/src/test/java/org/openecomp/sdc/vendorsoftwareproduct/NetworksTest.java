package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.core.utilities.CommonMethods;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;

public class NetworksTest {

  private static final String USER1 = "networksTestUser1";
  private static final String USER2 = "networksTestUser2";
  private static final Version VERSION01 = new Version(0, 1);
  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao
      vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();

  private static String vsp1Id;
  private static String vsp2Id;
  private static String networkId = "1";

  static org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity createNetwork(String vspId, Version version, String networkId) {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity
        networkEntity = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity(vspId, version, networkId);
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network
        networkData = new org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network();
    networkData.setName(networkId + " name");
    networkData.setDhcp(true);
    networkEntity.setNetworkCompositionData(networkData);
    vendorSoftwareProductDao.createNetwork(networkEntity);
    return networkEntity;
  }

  @BeforeClass
  private void init() {
    VspDetails vsp1 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp1", "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null), USER1);
    vsp1Id = vsp1.getId();

    VspDetails vsp2 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp2", "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null), USER1);
    vsp2Id = vsp2.getId();
  }

  @Test
  public void testListWhenNone() {
    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity> networks =
        vendorSoftwareProductManager.listNetworks(vsp1Id, null, USER1);
    Assert.assertEquals(networks.size(), 0);
  }

  @Test
  public void testCreateNonExistingVspId_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity("non existing vsp id", null, null), USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testCreateOnLockedVsp_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity(vsp1Id, null, null), USER2,
        VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
  }

/*    @Test(dependsOnMethods = "testListWhenNone")
    public void testCreate() {
        networkId = testCreate(vsp1Id);
    }

    private String testCreate(String vspId) {
        NetworkEntity expected = new NetworkEntity(vspId, null, null);
        Network networkData = new Network();
        networkData.setName("network1 name");
        networkData.setDhcp(true);
        expected.setNetworkCompositionData(networkData);


        NetworkEntity created = vendorSoftwareProductManager.createNetwork(expected, USER1);
        Assert.assertNotNull(created);
        expected.setId(created.getId());
        expected.setVersion(VERSION01);

        NetworkEntity actual = vendorSoftwareProductDao.getNetwork(vspId, VERSION01, created.getId());

        Assert.assertEquals(actual, expected);
        return created.getId();
    }

    @Test(dependsOnMethods = {"testCreate"})
    public void testCreateWithExistingName_negative() {
        NetworkEntity network = new NetworkEntity(vsp1Id, null, null);
        Network networkData = new Network();
        networkData.setName("network1 name");
        networkData.setDhcp(true);
        network.setNetworkCompositionData(networkData);
        testCreate_negative(network, USER1, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }*/

  @Test
  public void testCreateOnUploadVsp_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity(vsp1Id, null, null), USER1,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  /*    @Test(dependsOnMethods = {"testCreate"})
      public void testCreateWithExistingNameUnderOtherVsp() {
          testCreate(vsp2Id);
      }
  */
  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testGetNonExistingNetworkId_negative() {
    testGet_negative(vsp1Id, null, "non existing network id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testGetNonExistingVspId_negative() {
    testGet_negative("non existing vsp id", null, networkId, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testGet() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity
        expected = createNetwork(vsp1Id, VERSION01, networkId);
    testGet(vsp1Id, VERSION01, networkId, USER1, expected);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testUpdateNonExistingNetworkId_negative() {
    testUpdate_negative(vsp1Id, "non existing network id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testUpdateNonExistingVspId_negative() {
    testUpdate_negative("non existing vsp id", networkId, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = {"testGet"})
  public void testIllegalUpdateOnUploadVsp() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity(vsp1Id, null, networkId);
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network
        networkData = new org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network();
    networkData.setName(networkId + " name updated");
    networkData.setDhcp(false);
    expected.setNetworkCompositionData(networkData);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateNetwork(expected, USER1);
    Assert.assertNotNull(validationData);
    Assert.assertTrue(validationData.getErrors().size() > 0);
  }

  @Test(dependsOnMethods = {"testGet"})
  public void testListNonExistingVspId_negative() {
    testList_negative("non existing vsp id", null, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

    /*
           @Test(dependsOnMethods = {"testUpdateOnUploadVsp", "testList"})
           public void testCreateWithERemovedName() {
               testCreate(vsp1Id);
           }

    @Test(dependsOnMethods = "testList")
    public void testDeleteNonExistingNetworkId_negative() {
        testDelete_negative(vsp1Id, "non existing network id", USER1, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/

  @Test(dependsOnMethods = {"testGet"})
  public void testList() {
    createNetwork(vsp1Id, VERSION01, "2");

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity> actual =
        vendorSoftwareProductManager.listNetworks(vsp1Id, null, USER1);
    Assert.assertEquals(actual.size(), 2);
  }

  @Test(dependsOnMethods = "testList")
  public void testDeleteNonExistingVspId_negative() {
    testDelete_negative("non existing vsp id", networkId, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }
/*
           @Test(dependsOnMethods = "testList")
           public void testDelete() {
               vendorSoftwareProductManager.deleteNetwork(vsp1Id, networkId, USER1);
               NetworkEntity actual = vendorSoftwareProductDao.getNetwork(vsp1Id, VERSION01, networkId);
               Assert.assertNull(actual);
           }

           @Test
           public void testDeleteListNonExistingVspId_negative() {
               testDeleteList_negative("non existing vsp id", USER1, VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
           }

           @Test(dependsOnMethods = "testDelete")
           public void testDeleteList() {
               NetworkEntity network3 = new NetworkEntity(vsp1Id, null, null);
               network3.setName("network3 name");
               network3.setDescription("network3 desc");
               vendorSoftwareProductManager.createNetwork(network3, USER1);

               vendorSoftwareProductManager.deleteNetworks(vsp1Id, USER1);

               Collection<NetworkEntity> actual = vendorSoftwareProductManager.listNetworks(vsp1Id, null, USER1);
               Assert.assertEquals(actual.size(), 0);
           }*/

  @Test(dependsOnMethods = "testList")
  public void testDeleteOnUploadVsp_negative() {
    testDelete_negative(vsp1Id, networkId, USER1,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  private void testGet(String vspId, Version version, String networkId, String user,
                       org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity expected) {
    CompositionEntityResponse<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network> response =
        vendorSoftwareProductManager.getNetwork(vspId, null, networkId, user);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(response.getData(), expected.getNetworkCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

  private void testCreate_negative(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity network, String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.createNetwork(network, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String networkId, String user,
                                String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.getNetwork(vspId, version, networkId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, String networkId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.updateNetwork(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity(vspId, null, networkId), user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, Version version, String user,
                                 String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.listNetworks(vspId, version, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, String networkId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.deleteNetwork(vspId, networkId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }
}
