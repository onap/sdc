package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.core.utilities.CommonMethods;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;

public class ComponentsTest {

  private static final String USER1 = "componentsTestUser1";
  private static final String USER2 = "componentsTestUser2";
  private static final Version VERSION01 = new Version(0, 1);
  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao
      vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();

  private static String vsp1Id;
  private static String vsp2Id;
  private static String comp1Id = "1";
  private static String comp2Id = "2";

  static org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity createComponent(String vspId, Version version, String compId) {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity
        componentEntity = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity(vspId, version, compId);
    ComponentData compData = new ComponentData();
    compData.setName(compId + " name");
    compData.setDisplayName(compId + " display name");
    compData.setDescription(compId + " desc");
    componentEntity.setComponentCompositionData(compData);
    vendorSoftwareProductDao.createComponent(componentEntity);
    return componentEntity;
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
    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity> components =
        vendorSoftwareProductManager.listComponents(vsp1Id, null, USER1);
    Assert.assertEquals(components.size(), 0);
  }

  @Test
  public void testCreateNonExistingVspId_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity("non existing vsp id", null, null), USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testCreateOnLockedVsp_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity(vsp1Id, null, null), USER2,
        VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
  }

/*    @Test(dependsOnMethods = "testListWhenNone")
    public void testCreate() {
        comp1Id = testCreate(vsp1Id);
    }

    private String testCreate(String vspId) {
        ComponentEntity expected = new ComponentEntity(vspId, null, null);
        ComponentData compData = new ComponentData();
        compData.setName("comp1 name");
        compData.setDescription("comp1 desc");
        expected.setComponentCompositionData(compData);

        ComponentEntity created = vendorSoftwareProductManager.createComponent(expected, USER1);
        Assert.assertNotNull(created);
        expected.setId(created.getId());
        expected.setVersion(VERSION01);

        ComponentEntity actual = vendorSoftwareProductDao.getComponent(vspId, VERSION01, created.getId());

        Assert.assertEquals(actual, expected);
        return created.getId();
    }*/

/*    @Test(dependsOnMethods = {"testCreate"})
    public void testCreateWithExistingName_negative() {
        ComponentEntity component = new ComponentEntity(vsp1Id, null, null);
        ComponentData compData = new ComponentData();
        compData.setName("comp1 name");
        compData.setDescription("comp1 desc");
        component.setComponentCompositionData(compData);
        testCreate_negative(component, USER1, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }*/

/*    @Test(dependsOnMethods = {"testCreate"})
    public void testCreateWithExistingNameUnderOtherVsp() {
        testCreate(vsp2Id);
    }*/

  @Test
  public void testCreateOnUploadVsp_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity(vsp1Id, null, null), USER1,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test
  public void testGetNonExistingComponentId_negative() {
    testGet_negative(vsp1Id, null, "non existing component id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGetNonExistingVspId_negative() {
    testGet_negative("non existing vsp id", null, comp1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testListWhenNone")//"testCreate")
  public void testGet() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity
        expected = createComponent(vsp1Id, VERSION01, comp1Id);
    testGet(vsp1Id, VERSION01, comp1Id, USER1, expected);
  }

  @Test
  public void testUpdateNonExistingComponentId_negative() {
    testUpdate_negative(vsp1Id, "non existing component id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUpdateNonExistingVspId_negative() {
    testUpdate_negative("non existing vsp id", comp1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = {"testGet"})
  public void testUpdateOnUploadVsp() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity(vsp1Id, null, comp1Id);
    ComponentData compData = new ComponentData();
    compData.setName(comp1Id + " name");                // no change
    compData.setDisplayName(comp1Id + " display name"); // no change
    compData.setDescription(comp1Id + " desc updated"); // allowed change
    expected.setComponentCompositionData(compData);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateComponent(expected, USER1);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    expected.setVersion(VERSION01);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity actual = vendorSoftwareProductDao.getComponent(vsp1Id, VERSION01, comp1Id);
    Assert.assertEquals(actual, expected);
  }

  @Test(dependsOnMethods = {"testUpdateOnUploadVsp"})
  public void testIllegalUpdateOnUploadVsp() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity(vsp1Id, null, comp1Id);
    ComponentData compData = new ComponentData();
    compData
        .setName("comp1 name updated");         // not allowed: changed name + omitted display name
    expected.setComponentCompositionData(compData);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateComponent(expected, USER1);
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getErrors().size(), 2);
  }

  @Test
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
    public void testDeleteNonExistingComponentId_negative() {
        testDelete_negative(vsp1Id, "non existing component id", USER1, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/

  @Test(dependsOnMethods = {"testGet"})
  public void testList() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity
        createdP2 = createComponent(vsp1Id, VERSION01, comp2Id);

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity> actual =
        vendorSoftwareProductManager.listComponents(vsp1Id, null, USER1);
    Assert.assertEquals(actual.size(), 2);
  }

  @Test
  public void testDeleteNonExistingVspId_negative() {
    testDelete_negative("non existing vsp id", comp1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }
/*
    @Test(dependsOnMethods = "testList")
    public void testDelete() {
        vendorSoftwareProductManager.deleteComponent(vsp1Id, comp1Id, USER1);
        ComponentEntity actual = vendorSoftwareProductDao.getComponent(vsp1Id, VERSION01, comp1Id);
        Assert.assertNull(actual);
    }*/

  @Test
  public void testDeleteOnUploadVsp_negative() {
    testDelete_negative(vsp1Id, comp1Id, USER1,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test
  public void testDeleteListNonExistingVspId_negative() {
    testDeleteList_negative("non existing vsp id", USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }
/*
    @Test(dependsOnMethods = "testDelete")
    public void testDeleteList() {
        ComponentEntity comp3 = new ComponentEntity(vsp1Id, null, null);
        comp3.setName("comp3 name");
        comp3.setDescription("comp3 desc");
        vendorSoftwareProductManager.createComponent(comp3, USER1);

        vendorSoftwareProductManager.deleteComponents(vsp1Id, USER1);

        Collection<ComponentEntity> actual = vendorSoftwareProductManager.listComponents(vsp1Id, null, USER1);
        Assert.assertEquals(actual.size(), 0);
    }*/

  @Test
  public void testDeleteListOnUploadVsp_negative() {
    testDeleteList_negative(vsp1Id, USER1,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  private void testGet(String vspId, Version version, String componentId, String user,
                       org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity expected) {
    CompositionEntityResponse<ComponentData> response =
        vendorSoftwareProductManager.getComponent(vspId, null, componentId, user);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(response.getData(), expected.getComponentCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

  private void testCreate_negative(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity component, String user,
      String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.createComponent(component, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId, String user,
                                String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.getComponent(vspId, version, componentId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, String componentId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager
          .updateComponent(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity(vspId, null, componentId), user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, Version version, String user,
                                 String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.listComponents(vspId, version, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testDeleteList_negative(String vspId, String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.deleteComponents(vspId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, String componentId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.deleteComponent(vspId, componentId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }
}