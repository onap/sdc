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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MonitoringUploadsManagerImplTest {

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  private static final String USER1 = "ComponentsUploadTestUser";
  private static final String COMPONENT_ID = "COMPONENT_ID";
  private static final String VSP_ID = "vspId";
  private static final Version VERSION = new Version(0, 1);
  private static final String trapFileName = "MMSC.zip";
  private static final String pollFileName = "MNS OAM FW.zip";
  private static final String vesFileName = "vesTest-yml_only.zip";
  private static final String invalidVesFileName = "invalid_ves_file.zip";
  private static final String notZipFileName = "notZipFile";
  private static final String zipWithFoldersFileName = "zipFileWithFolder.zip";
  private static final String emptyZipFileName = "emptyZip.zip";
  private static final String ZIP_DIR = "/vspmanager/zips/";

  @Mock
  private ComponentArtifactDao componentArtifactDaoMock;
  @InjectMocks
  private MonitoringUploadsManagerImpl moitoringUploadsManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUploadEmptyZip() {
    InputStream zis = getFileInputStream(ZIP_DIR + emptyZipFileName);
    moitoringUploadsManager.upload(zis, emptyZipFileName, VSP_ID, VERSION, COMPONENT_ID,
        MonitoringUploadType.SNMP_TRAP, USER1);
  }

  @Test
  public void testUploadInvalidZip() {
    URL url = this.getClass().getResource("/notZipFile");
    try {
      moitoringUploadsManager
          .upload(url.openStream(), notZipFileName, VSP_ID, VERSION, COMPONENT_ID,
              MonitoringUploadType.VES_EVENTS, USER1);
      Assert.fail();
    } catch (Exception exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.getMessage(), "Invalid zip file");
    }
  }

  @Test
  public void testUploadZipWithFolders() {
    InputStream zis = getFileInputStream(ZIP_DIR + zipWithFoldersFileName);

    try {
      moitoringUploadsManager
          .upload(zis, zipWithFoldersFileName, VSP_ID, VERSION, COMPONENT_ID,
              MonitoringUploadType.SNMP_TRAP, USER1);
      Assert.fail();
    } catch (Exception exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.getMessage(), "Zip file should not contain folders");
    }
  }

  @Test
  public void testUploadVEsEventZipWithNonYamlFiles() {
    InputStream zis = getFileInputStream(ZIP_DIR + invalidVesFileName);

    try {
      moitoringUploadsManager
          .upload(zis, invalidVesFileName, VSP_ID, VERSION, COMPONENT_ID,
              MonitoringUploadType.VES_EVENTS, USER1);
      Assert.fail();
    } catch (Exception exception) {
      log.debug("",exception);
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
    artifact1.setArtifactName(trapFileName);

    ComponentMonitoringUploadEntity artifact2 =
        new ComponentMonitoringUploadEntity(VSP_ID, VERSION, COMPONENT_ID, "artifact2");
    artifact2.setType(MonitoringUploadType.SNMP_POLL);
    artifact2.setArtifactName(pollFileName);

    ComponentMonitoringUploadEntity artifact3 =
        new ComponentMonitoringUploadEntity(VSP_ID, VERSION, COMPONENT_ID, "artifact3");
    artifact3.setType(MonitoringUploadType.VES_EVENTS);
    artifact3.setArtifactName(vesFileName);

    doReturn(Arrays.asList(artifact1, artifact2, artifact3))
        .when(componentArtifactDaoMock).list(anyObject());

    MonitoringUploadStatus monitoringUploadStatus =
        moitoringUploadsManager.listFilenames(VSP_ID, VERSION, COMPONENT_ID, USER1);

    Assert.assertEquals(monitoringUploadStatus.getSnmpTrap(), trapFileName);
    Assert.assertEquals(monitoringUploadStatus.getSnmpPoll(), pollFileName);
    Assert.assertEquals(monitoringUploadStatus.getVesEvent(), vesFileName);
  }

  @Test (expectedExceptions = CoreException.class)
  public void testDeleteComponentMibWhenNone() {
    doReturn(Optional.empty()).when(componentArtifactDaoMock).getByType(any());
    moitoringUploadsManager
        .delete(VSP_ID, VERSION, COMPONENT_ID, MonitoringUploadType.SNMP_POLL, USER1);

    verify(componentArtifactDaoMock, never()).delete(anyObject());
  }

  @Test
  public void testDeleteComponentMonitoringUpload() {
    doReturn(Optional
        .of(new ComponentMonitoringUploadEntity(VSP_ID, VERSION, COMPONENT_ID, "artifactId")))
        .when
            (componentArtifactDaoMock).getByType(anyObject());

    moitoringUploadsManager
        .delete(VSP_ID, VERSION, COMPONENT_ID, MonitoringUploadType.SNMP_POLL, USER1);

    verify(componentArtifactDaoMock).delete(anyObject());
  }


  private InputStream getFileInputStream(String fileName) {
    URL url = this.getClass().getResource(fileName);
    try {
      return url.openStream();
    } catch (IOException exception) {
      log.debug("",exception);
      return null;
    }
  }
}
