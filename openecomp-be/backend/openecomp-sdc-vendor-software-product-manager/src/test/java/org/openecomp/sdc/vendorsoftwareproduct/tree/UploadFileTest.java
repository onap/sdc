package org.openecomp.sdc.vendorsoftwareproduct.tree;

import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VSPCommon;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipOutputStream;

public class UploadFileTest {


  public static final Version VERSION01 = new Version(0, 1);
  private static final String USER1 = "vspTestUser1";
  public static String id001 = null;
  public static String id002 = null;
  public static Version activeVersion002 = null;
  private static VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
  private static String vlm1Id;

  @BeforeClass
  static public void init() {

    //testCreateVSP
    vlm1Id = vendorLicenseFacade.createVendorLicenseModel(
        VSPCommon.createVendorLicenseModel("vlmName", "vlm1Id desc", "icon1"), USER1).getId();
    VspDetails expectedVsp = VSPCommon
        .createVspDetails(null, null, "VSP1", "Test-vsp", "vendorName", vlm1Id, "icon", "category",
            "subCategory", "123", null);

    VspDetails createdVsp = vendorSoftwareProductManager.createNewVsp(expectedVsp, USER1);
    id001 = createdVsp.getId();

    VspDetails actualVsp =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(id001, VERSION01));
    expectedVsp.setId(id001);
    expectedVsp.setVersion(VERSION01);


  }

  @Test
  public void testUploadFile() {
    //vspActiveVersion = vendorSoftwareProductManager.checkout(id001, USER1);
    vendorSoftwareProductManager.uploadFile(id001, getZipInputStream("/legalUpload"), USER1);
    //testLegalUpload(id001, vspActiveVersion, getZipInputStream("/legalUpload"), USER1);
  }


  private void testLegalUpload(String vspId, Version version, InputStream upload, String user) {
    vendorSoftwareProductManager.uploadFile(vspId, upload, user);

    UploadDataEntity uploadData =
        vendorSoftwareProductDao.getUploadData(new UploadDataEntity(vspId, version));

  }

  public InputStream getZipInputStream(String name) {
    URL url = this.getClass().getResource(name);
    File templateDir = new File(url.getFile());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    VSPCommon.zipDir(templateDir, "", zos, true);
    try {
      zos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }


}
