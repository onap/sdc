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

//package org.openecomp.sdc.vendorsoftwareproduct.informationartifact;
//
//import org.openecomp.core.util.UniqueValueUtil;
//import org.openecomp.sdc.vendorsoftwareproduct.utils.VSPCommon;
//import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
//import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
//import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
//import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
//import org.openecomp.sdc.versioning.dao.types.Version;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.io.File;
//import java.io.IOException;
//
//import static org.testng.Assert.*;
//
///**
// * Created by Talio on 12/6/2016.
// */
//public class InformationArtifactGeneratorImplTest {
//  private static VendorSoftwareProductManager vendorSoftwareProductManager =
//      new VendorSoftwareProductManagerImpl();
//  private static String vspId;
//  private static Version vspActiveVersion;
//  private static final String USER1 = "vspTestUser1";
//
//
//  @BeforeClass
//  public void init(){
//    UniqueValueUtil.deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME, "Test_download_info");
//    VspDetails vspDetails = vendorSoftwareProductManager.createVsp
//        (VSPCommon.createVspDetails(null, null, "Test_download_info", "Test-vsp-empty",
//            "vendorName", "vlm1Id", "icon", "category", "subCategory", "123", null), USER1);
//    vspId = vspDetails.getId();
//    vspActiveVersion = vspDetails.getVersion();
//  }
//
//  @Test
//  public void testDownloadInformationArtifact() throws IOException {
//    File informationArtifact =
//        vendorSoftwareProductManager.getInformationArtifact(vspId, vspActiveVersion, USER1);
//
//    Assert.assertNotNull(informationArtifact);
//  }
//
//  @Test
//  public void negativeTestDownloadInformationArtifactOnNoneExistiongVsp() throws IOException {
//    try {
//      File informationArtifact = vendorSoftwareProductManager
//          .getInformationArtifact("non_existing_id", vspActiveVersion, USER1);
//    }catch(Exception e){
//      Assert.assertEquals(e.getMessage(), "Versionable entity VendorSoftwareProduct with id " +
//          "non_existing_id does not exist.");
//    }
//  }
//
//}
