package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MonitoringUploadsManagerImplTest {

  private static final Logger LOG = LoggerFactory.getLogger(MonitoringUploadsManagerImplTest.class);

  private static final String USER1 = "ComponentsUploadTestUser";
  private static final String COMPONENT_ID = "COMPONENT_ID";
  private static final String VSP_ID = "vspId";
  private static final Version VERSION = new Version(0, 1);
  private static final String TRAP_FILE_NAME = "MMSC.zip";
  private static final String POLL_FILE_NAME = "MNS OAM FW.zip";
  private static final String VES_FILE_NAME = "vesTest-yml_only.zip";
  private static final String INVALID_VES_FILE_NAME = "invalid_ves_file.zip";
  private static final String NOT_ZIP_FILE_NAME = "notZipFile";
  private static final String ZIP_WITH_FOLDERS_FILE_NAME = "zipFileWithFolder.zip";
  private static final String EMPTY_ZIP_FILE_NAME = "emptyZip.zip";
  private static final String ZIP_DIR = "/vspmanager/zips/";

  @Mock
  private ComponentArtifactDao componentArtifactDaoMock;
  @InjectMocks
  private MonitoringUploadsManagerImpl monitoringUploadsManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUploadEmptyZip() {
    processFile(ZIP_DIR + EMPTY_ZIP_FILE_NAME, inputStream ->
      monitoringUploadsManager.upload(inputStream, EMPTY_ZIP_FILE_NAME, VSP_ID, VERSION, COMPONENT_ID,
          MonitoringUploadType.SNMP_TRAP, USER1));
  }

  @Test
  public void testUploadInvalidZip() {

    try {
      processFile("/notZipFile", inputStream ->
        monitoringUploadsManager
            .upload(inputStream, NOT_ZIP_FILE_NAME, VSP_ID, VERSION, COMPONENT_ID,
                    MonitoringUploadType.VES_EVENTS, USER1));
      Assert.fail();
    } catch (Exception exception) {
      Assert.assertEquals(exception.getMessage(), "Invalid zip file");
    }
  }

  @Test
  public void testUploadZipWithFolders() {

    try {
      processFile(ZIP_DIR + ZIP_WITH_FOLDERS_FILE_NAME, inputStream -> monitoringUploadsManager
          .upload(inputStream, ZIP_WITH_FOLDERS_FILE_NAME, VSP_ID, VERSION, COMPONENT_ID,
              MonitoringUploadType.SNMP_TRAP, USER1));
      Assert.fail();
    } catch (Exception exception) {
      Assert.assertEquals(exception.getMessage(), "Zip file should not contain folders");
    }
  }

  @Test
  public void testUploadVEsEventZipWithNonYamlFiles() {

    try {
      processFile(ZIP_DIR + INVALID_VES_FILE_NAME, inputStream -> monitoringUploadsManager
          .upload(inputStream, INVALID_VES_FILE_NAME, VSP_ID, VERSION, COMPONENT_ID,
              MonitoringUploadType.VES_EVENTS, USER1));
      Assert.fail();
    } catch (Exception exception) {
      LOG.debug("",exception);
      Assert.assertEquals(exception.getMessage(),
          "Wrong VES EVENT Artifact was uploaded - all files contained in Artifact must be YAML " +
              "files (using .yaml/.yml extensions)");
    }
  }


  @Test
  public void testListMonitoringFilenames() {
    ComponentMonitoringUploadEntity artifact1 =
        new ComponentMonitoringUploadEntity(VSP_ID, VERSION, COMPONENT_ID, "artifact1");
    artifact1.setType(MonitoringUploadType.SNMP_TRAP);
    artifact1.setArtifactName(TRAP_FILE_NAME);

    ComponentMonitoringUploadEntity artifact2 =
        new ComponentMonitoringUploadEntity(VSP_ID, VERSION, COMPONENT_ID, "artifact2");
    artifact2.setType(MonitoringUploadType.SNMP_POLL);
    artifact2.setArtifactName(POLL_FILE_NAME);

    ComponentMonitoringUploadEntity artifact3 =
        new ComponentMonitoringUploadEntity(VSP_ID, VERSION, COMPONENT_ID, "artifact3");
    artifact3.setType(MonitoringUploadType.VES_EVENTS);
    artifact3.setArtifactName(VES_FILE_NAME);

    doReturn(Arrays.asList(artifact1, artifact2, artifact3))
        .when(componentArtifactDaoMock).list(anyObject());

    MonitoringUploadStatus monitoringUploadStatus =
        monitoringUploadsManager.listFilenames(VSP_ID, VERSION, COMPONENT_ID, USER1);

    Assert.assertEquals(monitoringUploadStatus.getSnmpTrap(), TRAP_FILE_NAME);
    Assert.assertEquals(monitoringUploadStatus.getSnmpPoll(), POLL_FILE_NAME);
    Assert.assertEquals(monitoringUploadStatus.getVesEvent(), VES_FILE_NAME);
  }

  @Test (expectedExceptions = CoreException.class)
  public void testDeleteComponentMibWhenNone() {
    doReturn(Optional.empty()).when(componentArtifactDaoMock).getByType(any());
    monitoringUploadsManager
        .delete(VSP_ID, VERSION, COMPONENT_ID, MonitoringUploadType.SNMP_POLL, USER1);

    verify(componentArtifactDaoMock, never()).delete(anyObject());
  }

  @Test
  public void testDeleteComponentMonitoringUpload() {
    doReturn(Optional
        .of(new ComponentMonitoringUploadEntity(VSP_ID, VERSION, COMPONENT_ID, "artifactId")))
        .when
            (componentArtifactDaoMock).getByType(anyObject());

    monitoringUploadsManager
        .delete(VSP_ID, VERSION, COMPONENT_ID, MonitoringUploadType.SNMP_POLL, USER1);

    verify(componentArtifactDaoMock).delete(anyObject());
  }


  private void processFile(String fileName, Consumer<InputStream> processor) {

    URL url = this.getClass().getResource(fileName);
    try (InputStream inputStream = url.openStream()) {
      processor.accept(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Failed to process file: " + fileName);
    }
  }
}
