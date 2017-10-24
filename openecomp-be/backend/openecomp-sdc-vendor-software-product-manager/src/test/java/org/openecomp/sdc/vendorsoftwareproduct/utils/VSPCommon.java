/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.utils;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

        if (!path.isEmpty()) {
          path += File.separator;
        }

        try (InputStream is = new FileInputStream(file)) {
          zos.putNextEntry(new ZipEntry(path + file.getName()));
          byte[] data = FileUtils.toByteArray(is);
          zos.write(data);
          zos.closeEntry();
        } catch (IOException exception) {
          throw new RuntimeException("Failed to create Zip entry for file: " + file, exception);
        }
    }
  }

}
