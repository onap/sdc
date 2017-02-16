package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessArtifactEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;

public class ProcessesTest {

  protected static final String USER1 = "processesTestUser1";
  protected static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final String USER2 = "processesTestUser2";
  private static final String ARTIFACT_NAME = "artifact.sh";
  private static final Version VERSION01 = new Version(0, 1);
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();

  protected String vsp1Id;
  protected String vsp2Id;
  protected String component11Id = VendorSoftwareProductConstants.GENERAL_COMPONENT_ID;
  protected String component21Id = VendorSoftwareProductConstants.GENERAL_COMPONENT_ID;
  private String p1Id;
  private String p2Id;

  @BeforeClass
  protected void init() {
    VspDetails vsp1 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp1", "vendorName1",
            "vlm1Id", "icon", "category", "subCategory", "123", null), USER1);
    vsp1Id = vsp1.getId();

    VspDetails vsp2 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp2", "vendorName1",
            "vlm1Id", "icon", "category", "subCategory", "123", null), USER1);
    vsp2Id = vsp2.getId();
  }

  @Test
  public void testListWhenNone() {
    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity> processes =
        vendorSoftwareProductManager.listProcesses(vsp1Id, null, component11Id, USER1);
    Assert.assertEquals(processes.size(), 0);
  }

  @Test
  public void testCreateNonExistingComponentId_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vsp1Id, null, "non existing component id", null), USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testCreateNonExistingVspId_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity("non existing vsp id", null, component11Id, null), USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testCreateOnLockedVsp_negative() {
    testCreate_negative(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vsp1Id, null, component11Id, null), USER2,
        VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
  }

  @Test(dependsOnMethods = "testListWhenNone")
  public void testCreate() {
    p1Id = testCreate(vsp1Id, component11Id);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testCreateWithExistingName_negative() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity
        process = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vsp1Id, null, component11Id, null);
    process.setName("p1 name");
    testCreate_negative(process, USER1, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testCreateWithExistingNameUnderOtherComponent() {
    // This method is implemented in the sub class ComponentProcessesTest, it is here in order to keep the tests sequence down there (using @Test).
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testCreateWithExistingNameUnderOtherVsp() {
    testCreate(vsp2Id, component21Id);
  }

  @Test
  public void testGetNonExistingProcessId_negative() {
    testGet_negative(vsp1Id, null, component11Id, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testGetNonExistingComponentId_negative() {
    testGet_negative(vsp1Id, null, "non existing component id", p1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testGetNonExistingVspId_negative() {
    testGet_negative("non existing vsp id", null, component11Id, p1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testGet() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity
        actual = testGet(vsp1Id, VERSION01, component11Id, p1Id, USER1);
    Assert.assertNull(actual.getArtifactName());
  }

  @Test
  public void testUpdateNonExistingProcessId_negative() {
    testUpdate_negative(vsp1Id, component11Id, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testUpdateNonExistingComponentId_negative() {
    testUpdate_negative(vsp1Id, "non existing component id", p1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testUpdateNonExistingVspId_negative() {
    testUpdate_negative("non existing vsp id", component11Id, p1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = {"testGet"})
  public void testUpdate() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vsp1Id, null, component11Id, p1Id);
    expected.setName("p1 name updated");
    expected.setDescription("p1 desc updated");

    vendorSoftwareProductManager.updateProcess(expected, USER1);
    expected.setVersion(VERSION01);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity actual =
        vendorSoftwareProductDao.getProcess(vsp1Id, VERSION01, component11Id, p1Id);
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void testListNonExistingComponentId_negative() {
    testList_negative(vsp1Id, null, "non existing component id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testListNonExistingVspId_negative() {
    testList_negative("non existing vsp id", null, component11Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = {"testGet"})
  public void testList() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity
        p2 = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vsp1Id, null, component11Id, null);
    p2.setName("p2 name");
    p2.setDescription("p2 desc");

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity createdP2 = vendorSoftwareProductManager.createProcess(p2, USER1);
    p2Id = createdP2.getId();

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity> actual =
        vendorSoftwareProductManager.listProcesses(vsp1Id, null, component11Id, USER1);
    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity> expected =
        vendorSoftwareProductDao.listProcesses(vsp1Id, VERSION01, component11Id);
    Assert.assertEquals(actual.size(), 2);
    Assert.assertEquals(actual, expected);
  }

  @Test(dependsOnMethods = {"testUpdate", "testList"})
  public void testCreateWithRemovedName() {
    testCreate(vsp1Id, component11Id);
  }

  @Test
  public void testDeleteNonExistingProcessId_negative() {
    testDelete_negative(vsp1Id, component11Id, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testDeleteNonExistingComponentId_negative() {
    testDelete_negative(vsp1Id, "non existing component id", p1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testDeleteNonExistingVspId_negative() {
    testDelete_negative("non existing vsp id", component11Id, p1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testList")
  public void testDelete() {
    vendorSoftwareProductManager.deleteProcess(vsp1Id, component11Id, p1Id, USER1);
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity actual =
        vendorSoftwareProductDao.getProcess(vsp1Id, VERSION01, component11Id, p1Id);
    Assert.assertNull(actual);
  }

  @Test
  public void testUploadFileNonExistingProcessId_negative() {
    testUploadFile_negative(vsp1Id, component11Id, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testUploadFileNonExistingComponentId_negative() {
    testUploadFile_negative(vsp1Id, "non existing component id", p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testUploadFileNonExistingVspId_negative() {
    testUploadFile_negative("non existing vsp id", component11Id, p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testList")
  public void testGetFileWhenNone_negative() {
    testGetFile_negative(vsp1Id, null, component11Id, p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testDeleteFileWhenNone_negative() {
    testDeleteFile_negative(vsp1Id, component11Id, p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = {"testGetFileWhenNone_negative", "testDeleteFileWhenNone_negative"})
  public void testUploadFile() {
    vendorSoftwareProductManager
        .uploadProcessArtifact(new ByteArrayInputStream("bla bla".getBytes()), ARTIFACT_NAME,
            vsp1Id, component11Id, p2Id, USER1);
    ProcessArtifactEntity actual =
        vendorSoftwareProductDao.getProcessArtifact(vsp1Id, VERSION01, component11Id, p2Id);
    Assert.assertNotNull(actual);
    Assert.assertNotNull(actual.getArtifact());
    Assert.assertEquals(actual.getArtifactName(), ARTIFACT_NAME);
  }

  @Test(dependsOnMethods = "testUploadFile")
  public void testGetAfterUploadFile() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity
        actual = testGet(vsp1Id, VERSION01, component11Id, p2Id, USER1);
    Assert.assertEquals(actual.getArtifactName(), ARTIFACT_NAME);
  }

  @Test
  public void testGetFileNonExistingProcessId_negative() {
    testGetFile_negative(vsp1Id, null, component11Id, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testGetFileNonExistingComponentId_negative() {
    testGetFile_negative(vsp1Id, null, "non existing component id", p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testGetFileNonExistingVspId_negative() {
    testGetFile_negative("non existing vsp id", null, component11Id, p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testUploadFile")
  public void testGetFile() {
    File actual =
        vendorSoftwareProductManager.getProcessArtifact(vsp1Id, null, component11Id, p2Id, USER1);
    Assert.assertNotNull(actual);
    ProcessArtifactEntity expected =
        vendorSoftwareProductDao.getProcessArtifact(vsp1Id, VERSION01, component11Id, p2Id);
    Assert.assertNotNull(expected);
    Assert.assertNotNull(expected.getArtifact());
  }

  @Test
  public void testDeleteFileNonExistingProcessId_negative() {
    testDeleteFile_negative(vsp1Id, component11Id, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testDeleteFileNonExistingComponentId_negative() {
    testDeleteFile_negative(vsp1Id, "non existing component id", p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testList")
  public void testDeleteFileNonExistingVspId_negative() {
    testDeleteFile_negative("non existing vsp id", component11Id, p2Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = "testGetFile")
  public void testDeleteFile() {
    vendorSoftwareProductManager.deleteProcessArtifact(vsp1Id, component11Id, p2Id, USER1);
    ProcessArtifactEntity expected =
        vendorSoftwareProductDao.getProcessArtifact(vsp1Id, VERSION01, component11Id, p2Id);
    Assert.assertNull(expected.getArtifact());
  }

  @Test
  public void testDeleteListNonExistingComponentId_negative() {
    testDeleteList_negative(vsp1Id, "non existing component id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteListNonExistingVspId_negative() {
    testDeleteList_negative("non existing vsp id", component11Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test(dependsOnMethods = {"testDeleteFile"})
  public void testDeleteList() {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity
        p3 = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vsp1Id, null, component11Id, null);
    p3.setName("p3 name");
    p3.setDescription("p3 desc");
    vendorSoftwareProductManager.createProcess(p3, USER1);

    vendorSoftwareProductManager.deleteProcesses(vsp1Id, component11Id, USER1);

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity> actual =
        vendorSoftwareProductManager.listProcesses(vsp1Id, null, component11Id, USER1);
    Assert.assertEquals(actual.size(), 0);
  }

  protected String testCreate(String vspId, String componentId) {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity
        expected = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vspId, null, componentId, null);
    expected.setName("p1 name");
    expected.setDescription("p1 desc");

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity created = vendorSoftwareProductManager.createProcess(expected, USER1);
    Assert.assertNotNull(created);
    expected.setId(created.getId());
    expected.setVersion(VERSION01);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity actual =
        vendorSoftwareProductDao.getProcess(vspId, VERSION01, componentId, created.getId());

    Assert.assertEquals(actual, expected);

    return created.getId();
  }

  private org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity testGet(String vspId, Version version, String componentId, String processId,
                                                                                 String user) {
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity actual =
        vendorSoftwareProductManager.getProcess(vspId, null, componentId, processId, user);
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity expected =
        vendorSoftwareProductDao.getProcess(vspId, version, componentId, processId);
    Assert.assertEquals(actual, expected);
    return actual;
  }

  private void testCreate_negative(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity process, String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.createProcess(process, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId, String processId,
                                String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.getProcess(vspId, version, componentId, processId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, String componentId, String processId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager
          .updateProcess(new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vspId, null, componentId, processId), user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, Version version, String componentId, String user,
                                 String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.listProcesses(vspId, version, componentId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testDeleteList_negative(String vspId, String componentId, String user,
                                       String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.deleteProcesses(vspId, componentId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, String componentId, String processId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.deleteProcess(vspId, componentId, processId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testGetFile_negative(String vspId, Version version, String componentId,
                                    String processId, String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.getProcessArtifact(vspId, version, componentId, processId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testUploadFile_negative(String vspId, String componentId, String processId,
                                       String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager
          .uploadProcessArtifact(new ByteArrayInputStream("bla bla".getBytes()), "artifact.sh",
              vspId, componentId, processId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

  private void testDeleteFile_negative(String vspId, String componentId, String processId,
                                       String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.deleteProcessArtifact(vspId, componentId, processId, user);
      Assert.fail();
    } catch (CoreException e) {
      Assert.assertEquals(e.code().id(), expectedErrorCode);
    }
  }

}
