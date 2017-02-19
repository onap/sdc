package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.utilities.file.FileUtils;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VSPCommon {

  public static VspDetails createVspDetails(String id, Version version, String name, String desc,
                                            String vendorName, String vlm, String icon,
                                            String category, String subCategory,
                                            String licenseAgreement, List<String> featureGroups) {
    VspDetails vspDetails = new VspDetails(id, version);
    vspDetails.setName(name);
    vspDetails.setDescription(desc);
    vspDetails.setIcon(icon);
    vspDetails.setCategory(category);
    vspDetails.setSubCategory(subCategory);
    vspDetails.setVendorName(vendorName);
    vspDetails.setVendorId(vlm);
    vspDetails.setVlmVersion(new Version(1, 0));
    vspDetails.setLicenseAgreement(licenseAgreement);
    vspDetails.setFeatureGroups(featureGroups);
    return vspDetails;
  }


  public static VendorLicenseModelEntity createVendorLicenseModel(String name, String desc,
                                                                  String icon) {
    VendorLicenseModelEntity vendorLicenseModel = new VendorLicenseModelEntity();
    vendorLicenseModel.setVendorName(name);
    vendorLicenseModel.setDescription(desc);
    vendorLicenseModel.setIconRef(icon);
    return vendorLicenseModel;
  }

  public static void zipDir(File file, String path, ZipOutputStream zos) {
    zipDir(file, path, zos, false);
  }

  public static void zipDir(File file, String path, ZipOutputStream zos, boolean isRootDir) {
    if (file.isDirectory()) {
      path += File.separator + file.getName();
      File[] files = file.listFiles();
      if (files != null) {
        for (File innerFile : files) {
          if (isRootDir) {
            zipDir(innerFile, "", zos, false);
          } else {
            zipDir(innerFile, path, zos, false);
          }
        }
      }
    } else {

      try {
        if (!path.isEmpty()) {
          path += File.separator;
        }
        zos.putNextEntry(new ZipEntry(path + file.getName()));
        InputStream is = new FileInputStream(file);
        byte[] data = FileUtils.toByteArray(is);
        zos.write(data);
        zos.closeEntry();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
