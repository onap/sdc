package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.eq;

public class ProcessManagerImplTest {

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  private static final String USER1 = "processesTestUser";
  private static final String VSP_ID = "vsp";
  private static final Version VERSION = new Version(0, 1);
  private static final String COMPONENT_ID = "component";
  private static final String PROCESS1_ID = "process1";
  private static final String PROCESS2_ID = "process2";
  private static final String ARTIFACT_NAME = "artifact.sh";

  @Mock
  private VendorSoftwareProductDao vendorSoftwareProductDaoMock;
  @Mock
  private ActivityLogManager activityLogManagerMock;

  @InjectMocks
  @Spy
  private ProcessManagerImpl processManager;
  @Captor
  private ArgumentCaptor<ActivityLogEntity> activityLogEntityArg;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListWhenNone() {
    Collection<ProcessEntity> processes =
        processManager.listProcesses(VSP_ID, VERSION, COMPONENT_ID, USER1);
    Assert.assertEquals(processes.size(), 0);
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID),
        createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS2_ID)))
        .when(vendorSoftwareProductDaoMock).listProcesses(VSP_ID, VERSION, COMPONENT_ID);

    Collection<ProcessEntity> actual =
        processManager.listProcesses(VSP_ID, VERSION, COMPONENT_ID, USER1);
    Assert.assertEquals(actual.size(), 2);
  }

  @Test
  public void testDeleteListWhenNone() {
    processManager.deleteProcesses(VSP_ID, VERSION, COMPONENT_ID, USER1);
    verify(vendorSoftwareProductDaoMock, never()).deleteProcesses(VSP_ID, VERSION, COMPONENT_ID);
  }

  @Test
  public void testDeleteList() {
    ProcessEntity process1 = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    ProcessEntity process2 = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS2_ID);
    doReturn(Arrays.asList(process1, process2))
        .when(vendorSoftwareProductDaoMock).listProcesses(VSP_ID, VERSION, COMPONENT_ID);
    doNothing().when(processManager)
        .deleteUniqueValue(VSP_ID, VERSION, COMPONENT_ID, process1.getName());
    doNothing().when(processManager)
        .deleteUniqueValue(VSP_ID, VERSION, COMPONENT_ID, process2.getName());

    processManager.deleteProcesses(VSP_ID, VERSION, COMPONENT_ID, USER1);

    verify(vendorSoftwareProductDaoMock).deleteProcesses(VSP_ID, VERSION, COMPONENT_ID);
    verify(processManager)
        .deleteUniqueValue(VSP_ID, VERSION, COMPONENT_ID, process1.getName());
    verify(processManager)
        .deleteUniqueValue(VSP_ID, VERSION, COMPONENT_ID, process2.getName());
  }

  @Test
  public void testCreate() {
    ProcessEntity processToCreate = createProcess(VSP_ID, VERSION, COMPONENT_ID, null);
    processToCreate.setName("proc name");

    doNothing().when(processManager)
        .validateUniqueName(VSP_ID, VERSION, COMPONENT_ID, processToCreate.getName());
    doNothing().when(processManager)
        .createUniqueName(VSP_ID, VERSION, COMPONENT_ID, processToCreate.getName());

    ProcessEntity process = processManager.createProcess(processToCreate, USER1);
    Assert.assertNotNull(process);
    process.setId(process.getId());

    Assert.assertEquals(process, processToCreate);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testCreateWithExistingName_negative() {
    ProcessEntity process = createProcess(VSP_ID, VERSION, COMPONENT_ID, null);
    process.setName("p1 name");

    doThrow(new CoreException(
        new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).build()))
        .when(processManager).validateUniqueName(VSP_ID, VERSION, COMPONENT_ID, process.getName());

    processManager.createProcess(process, USER1);
  }

  @Test
  public void testUpdateNonExistingProcessId_negative() {
    doReturn(null).when(vendorSoftwareProductDaoMock)
        .getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);

    testUpdate_negative(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUpdateWithExistingName_negative() {
    ProcessEntity existingProcess = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doReturn(existingProcess).when
        (vendorSoftwareProductDaoMock).getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);

    ProcessEntity processToUpdate = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doThrow(new CoreException(
        new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).build()))
        .when(processManager)
        .updateUniqueName(VSP_ID, VERSION, COMPONENT_ID, existingProcess.getName(),
            processToUpdate.getName());

    processManager.updateProcess(processToUpdate, USER1);
  }

  @Test
  public void testUpdate() {
    ProcessEntity existingProcess = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doReturn(existingProcess).when
        (vendorSoftwareProductDaoMock).getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);

    ProcessEntity processToUpdate = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doNothing().when(processManager)
        .updateUniqueName(VSP_ID, VERSION, COMPONENT_ID, existingProcess.getName(),
            processToUpdate.getName());

    processManager.updateProcess(processToUpdate, USER1);
    verify(vendorSoftwareProductDaoMock).updateProcess(processToUpdate);
  }


  @Test
  public void testGetNonExistingProcessId_negative() {
    testGet_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGet() {
    ProcessEntity process = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doReturn(process).when
        (vendorSoftwareProductDaoMock).getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    ProcessEntity actual =
        processManager.getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1);
    Assert.assertEquals(actual, process);
    Assert.assertNull(actual.getArtifactName());
  }

  @Test
  public void testGetAfterUploadArtifact() {
    ProcessEntity process = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    process.setArtifactName(ARTIFACT_NAME);
    doReturn(process).when
        (vendorSoftwareProductDaoMock).getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    ProcessEntity actual =
        processManager.getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1);
    Assert.assertEquals(actual, process);
    Assert.assertEquals(actual.getArtifactName(), ARTIFACT_NAME);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testDeleteNonExistingProcessId_negative() {
    processManager.deleteProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1);
  }

  @Test
  public void testDelete() {
    ProcessEntity processToDelete = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doReturn(processToDelete).when
        (vendorSoftwareProductDaoMock).getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doNothing().when(processManager).deleteUniqueValue(VSP_ID, VERSION, COMPONENT_ID,
        processToDelete.getName());

    processManager.deleteProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1);
    verify(vendorSoftwareProductDaoMock).deleteProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    verify(processManager)
        .deleteUniqueValue(VSP_ID, VERSION, COMPONENT_ID, processToDelete.getName());
  }

  @Test
  public void testUploadArtifactNonExistingProcessId_negative() {
    testUploadArtifact_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUploadArtifact() {
    ProcessEntity process = createProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    doReturn(process).when
        (vendorSoftwareProductDaoMock).getProcess(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);

    byte[] artifactBytes = "bla bla".getBytes();
    processManager
        .uploadProcessArtifact(new ByteArrayInputStream(artifactBytes), ARTIFACT_NAME,
            VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1);
    verify(vendorSoftwareProductDaoMock)
        .uploadProcessArtifact(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, artifactBytes,
            ARTIFACT_NAME);

    verify(activityLogManagerMock).addActionLog(activityLogEntityArg.capture(), eq(USER1));
    ActivityLogEntity activityLogEntity = activityLogEntityArg.getValue();
    Assert.assertEquals(activityLogEntity.getVersionId(), String.valueOf(VERSION.getMajor()+1));
    Assert.assertTrue(activityLogEntity.isSuccess());
  }

  @Test
  public void testGetArtifactWhenNone_negative() {
    testGetFile_negative(VSP_ID, VERSION, COMPONENT_ID, PROCESS2_ID, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGetArtifactNonExistingProcessId_negative() {
    testGetFile_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGetArtifact() {
    ProcessEntity processArtifact =
        new ProcessEntity(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    processArtifact.setArtifact(ByteBuffer.wrap("bla bla".getBytes()));
    doReturn(processArtifact).when(vendorSoftwareProductDaoMock)
        .getProcessArtifact(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);

    File actual =
        processManager.getProcessArtifact(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1);
    Assert.assertNotNull(actual);
  }

  @Test
  public void testDeleteArtifactWhenNone_negative() {
    testDeleteArtifact_negative(VSP_ID, COMPONENT_ID, PROCESS2_ID, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteArtifactNonExistingProcessId_negative() {
    testDeleteArtifact_negative(VSP_ID, COMPONENT_ID, "non existing process id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteArtifact() {
    ProcessEntity processArtifact =
        new ProcessEntity(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
    processArtifact.setArtifact(ByteBuffer.wrap("bla bla".getBytes()));
    doReturn(processArtifact).when(vendorSoftwareProductDaoMock)
        .getProcessArtifact(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);

    processManager.deleteProcessArtifact(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID, USER1);
    verify(vendorSoftwareProductDaoMock)
        .deleteProcessArtifact(VSP_ID, VERSION, COMPONENT_ID, PROCESS1_ID);
  }


  private ProcessEntity createProcess(String vspId, Version version, String componentId,
                                      String processId) {
    ProcessEntity process = new ProcessEntity(vspId, version, componentId, processId);
    process.setName(processId + " name");
    process.setDescription(processId + " desc");
    process.setType(ProcessType.Other);
    return process;
  }

  private void testGet_negative(String vspId, Version version, String componentId, String processId,
                                String user, String expectedErrorCode) {
    try {
      processManager.getProcess(vspId, version, componentId, processId, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, Version version, String componentId,
                                   String processId, String user,
                                   String expectedErrorCode) {
    try {
      processManager
          .updateProcess(new ProcessEntity(vspId, version, componentId, processId), user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGetFile_negative(String vspId, Version version, String componentId,
                                    String processId, String user, String expectedErrorCode) {
    try {
      processManager.getProcessArtifact(vspId, version, componentId, processId, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUploadArtifact_negative(String vspId, Version version, String componentId,
                                           String processId, String user,
                                           String expectedErrorCode) {
    try {
      processManager
          .uploadProcessArtifact(new ByteArrayInputStream("bla bla".getBytes()), "artifact.sh",
              vspId, version, componentId, processId, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.error("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDeleteArtifact_negative(String vspId, String componentId, String processId,
                                           String user, String expectedErrorCode) {
    try {
      processManager.deleteProcessArtifact(vspId, VERSION, componentId, processId, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

}
