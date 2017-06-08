package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MibDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MibUploadStatus;
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

public class MibManagerImplTest {

  private static final String USER1 = "ComponentsUploadTestUser";
  private static final String COMPONENT_ID = "COMPONENT_ID";
  private static final String VSP_ID = "vspId";
  private static final Version VERSION = new Version(0, 1);
  private static final String trapFileName = "MMSC.zip";
  private static final String pollFileName = "MNS OAM FW.zip";
  private static final String notZipFileName = "notZipFile";
  private static final String zipWithFoldersFileName = "zipFileWithFolder.zip";
  private static final String emptyZipFileName = "emptyZip.zip";
  private static final String ZIP_DIR = "/vspmanager/zips/";

  @Mock
  private VendorSoftwareProductDao vendorSoftwareProductDaoMock;
  @Mock
  private MibDao mibDaoMock;
  @InjectMocks
  private MibManagerImpl mibManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUploadEmptyZip() {
    InputStream zis = getFileInputStream(ZIP_DIR + emptyZipFileName);
    mibManager.upload(zis, emptyZipFileName, VSP_ID, VERSION, COMPONENT_ID,
        ArtifactType.SNMP_TRAP, USER1);
  }

  @Test
  public void testUploadInvalidZip() {
    URL url = this.getClass().getResource("/notZipFile");
    try {
      mibManager
          .upload(url.openStream(), notZipFileName, VSP_ID, VERSION, COMPONENT_ID,
              ArtifactType.SNMP_TRAP, USER1);
      Assert.fail();
    } catch (Exception exception) {
      Assert.assertEquals(exception.getMessage(), "Invalid zip file");
    }
  }

  @Test
  public void testUploadZipWithFolders() {
    InputStream zis = getFileInputStream(ZIP_DIR + zipWithFoldersFileName);

    try {
      mibManager
          .upload(zis, zipWithFoldersFileName, VSP_ID, VERSION, COMPONENT_ID,
              ArtifactType.SNMP_TRAP, USER1);
      Assert.fail();
    } catch (Exception exception) {
      Assert.assertEquals(exception.getMessage(), "Zip file should not contain folders");
    }
  }


  @Test
  public void testListMibFilenames() {
    MibEntity artifact1 =
        new MibEntity(VSP_ID, VERSION, COMPONENT_ID, "artifact1");
    artifact1.setType(ArtifactType.SNMP_TRAP);
    artifact1.setArtifactName(trapFileName);

    MibEntity artifact2 =
        new MibEntity(VSP_ID, VERSION, COMPONENT_ID, "artifact2");
    artifact2.setType(ArtifactType.SNMP_POLL);
    artifact2.setArtifactName(pollFileName);

    doReturn(Arrays.asList(artifact1, artifact2))
        .when(mibDaoMock).list(anyObject());

    MibUploadStatus mibUploadStatus =
        mibManager.listFilenames(VSP_ID, VERSION, COMPONENT_ID, USER1);

    Assert.assertEquals(mibUploadStatus.getSnmpTrap(), trapFileName);
    Assert.assertEquals(mibUploadStatus.getSnmpPoll(), pollFileName);
  }

  @Test (expectedExceptions = CoreException.class)
  public void testDeleteComponentMibWhenNone() {
    doReturn(Optional.empty()).when(mibDaoMock).getByType(any());
    mibManager.delete(VSP_ID, VERSION, COMPONENT_ID, ArtifactType.SNMP_POLL, USER1);

    verify(mibDaoMock, never()).delete(anyObject());
  }

  @Test
  public void testDeleteComponentMib() {
    doReturn(Optional.of(new MibEntity(VSP_ID, VERSION, COMPONENT_ID, "artifactId"))).when
        (mibDaoMock).getByType(anyObject());

    mibManager.delete(VSP_ID, VERSION, COMPONENT_ID, ArtifactType.SNMP_POLL, USER1);

    verify(mibDaoMock).delete(anyObject());
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
}
