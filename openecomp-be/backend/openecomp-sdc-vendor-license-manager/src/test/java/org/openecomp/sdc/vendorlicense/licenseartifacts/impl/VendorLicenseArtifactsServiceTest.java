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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.vendorlicense.ArtifactTestUtils;


public class VendorLicenseArtifactsServiceTest extends ArtifactTestUtils {
  private FileContentHandler licenseArtifacts;
/*


  @Test
  public void createVNFArtifact() throws Exception {
    Version vlmVersion = vspDetails.getVlmVersion();
    licenseArtifacts = vendorLicenseArtifactsService
        .createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion,
            vspDetails.getFeatureGroups(), USER1);
    String actual = IOUtils.toString(
        licenseArtifacts.getFileContent(VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);
        System.out.println("createVNFArtifact = " + actual);

    Assert.assertTrue(actual.contains("type"));
    Assert.assertFalse(actual.contains(lkg13Id));
    Assert.assertTrue(actual.contains(OperationalScope.Availability_Zone.toString()));
    Assert.assertTrue(actual.contains("vf-id"));
    Assert.assertFalse(
        actual.contains(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_REGEX_REMOVE));
    Assert.assertFalse(actual.contains("80.0"));
    Assert.assertTrue(actual.contains("80"));
    Assert.assertFalse(actual.contains("versionForArtifact"));
  }

  @Test
  public void createVendorLicenseArtifact() throws Exception {
    Version vlmVersion = vspDetails.getVlmVersion();

    licenseArtifacts = vendorLicenseArtifactsService
        .createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion,
            vspDetails.getFeatureGroups(), USER1);
    String actual = IOUtils.toString(licenseArtifacts
            .getFileContent(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);

     //System.out.println("createVendorLicenseArtifact = " + actual);
    Assert.assertTrue(actual.contains(lkg11Id));
    Assert.assertTrue(actual.contains(ep11Id));
    Assert.assertTrue(actual.contains("type"));
    Assert.assertTrue(actual.contains(EntitlementMetric.Core.toString()));
    Assert.assertTrue(actual.contains("entitlement-pool-list"));
    Assert.assertFalse(
        actual.contains(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_REGEX_REMOVE));

    Assert.assertTrue(actual.contains("vendor-license-model"));
    Assert.assertFalse(actual.contains("80.0"));
    Assert.assertTrue(actual.contains("80"));
  }

  @Test
  public void vNFArtifactContainsCurrentVLMVersion() throws IOException {
    super.setVlm2FirstVersion();
    licenseArtifacts = vendorLicenseArtifactsService
        .createLicenseArtifacts(vsp2.getId(), vsp2.getVendorId(), vsp2.getVlmVersion(),
            vsp2.getFeatureGroups(), USER1);
    String actual = IOUtils.toString(
        licenseArtifacts.getFileContent(VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);

//        Assert.assertTrue(actual.contains(lkg21Id));
    Assert.assertTrue(actual.contains(ep21.getVersionUuId()));
  }

  @Test
  public void vnfArtifactContainsSameIdAsVLMArtifact() throws IOException {
    Version vlmVersion = vspDetails.getVlmVersion();
    licenseArtifacts = vendorLicenseArtifactsService
        .createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion,
            vspDetails.getFeatureGroups(), USER1);
    String actualVnfArtifact = IOUtils.toString(
        licenseArtifacts.getFileContent(VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);
    String actualVendorLicenseArtifact = IOUtils.toString(licenseArtifacts
            .getFileContent(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);

    String firstLKGUUID = actualVnfArtifact
        .substring(actualVnfArtifact.indexOf("<license-key-group-uuid>") + 24,
            actualVnfArtifact.indexOf("<license-key-group-uuid>") + 60);
    Assert.assertTrue(actualVendorLicenseArtifact.contains(firstLKGUUID));

    String firstEPUUID = actualVnfArtifact
        .substring(actualVnfArtifact.indexOf("<<entitlement-pool-uuid>>") + 23,
            actualVnfArtifact.indexOf("<<entitlement-pool-uuid>>") + 60);
    Assert.assertTrue(actualVendorLicenseArtifact.contains(firstEPUUID));
  }


  //@Test
  public void vNFArtifactContainsPreviousVLMVersionAndNotLatest() throws IOException {
    super.setVlm2SecondVersion();
    licenseArtifacts = vendorLicenseArtifactsService
        .createLicenseArtifacts(vsp2.getId(), vsp2.getVendorId(), vsp2.getVlmVersion(),
            vsp2.getFeatureGroups(), USER1);
    String actual = IOUtils.toString(
        licenseArtifacts.getFileContent(VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);

    Assert.assertTrue(actual.contains(lkg21.getVersionUuId()));
    Assert.assertTrue(actual.contains(ep21.getVersionUuId()));
    Assert.assertFalse(actual.contains(lkg22Id));
    Assert.assertFalse(actual.contains(ep22Id));


    Assert.assertTrue(actual.contains("80"));
  }


  @Test
  public void vlmVersionTwoThenUpdatingLKGAndEpInThird() throws IOException {
    Version vlmVersion = vspDetails.getVlmVersion();

    EntitlementPoolEntity updatedEP = ep11;
    String updatedNameEP = "updatedNameEP";
    updatedEP.setName(updatedNameEP);
    LicenseKeyGroupEntity updatedLKG = new LicenseKeyGroupEntity();
    updatedLKG.setId(lkg11Id);
    updatedLKG.setVendorLicenseModelId(lkg11.getVendorLicenseModelId());
    String updateDescLKG = "UpdateDescLKG";
    updatedLKG.setName(lkg11.getName());
    updatedLKG.setDescription(updateDescLKG);

    createThirdFinalVersionForVLMChangeEpLKGInSome(ep11.getVendorLicenseModelId(), updatedEP,
        updatedLKG);
    licenseArtifacts = vendorLicenseArtifactsService
        .createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion,
            vspDetails.getFeatureGroups(), USER1);
    String actual = IOUtils.toString(licenseArtifacts
            .getFileContent(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);
    System.out.println("onlyAddChangedEntitiesToVendorArtifact = " + actual);

    int countUpdatedLKG = StringUtils.countMatches(actual, updateDescLKG);
    Assert.assertEquals(countUpdatedLKG, 1);

    int countUpdatedEp = StringUtils.countMatches(actual, updatedNameEP);
    Assert.assertEquals(countUpdatedEp, 1);

    int epOccurrences = StringUtils.countMatches(actual, "<entitlement-pool>");
    Assert.assertEquals(epOccurrences, 3);
  }


  @Test(invocationCount = 5)
  public void vlmVersionOneThenAddEp_bug2629() throws IOException {
    createAndSetupVlm3();
    addEpToVLM(vlm3Id);
    createVspWithSpecifiedVlmVersion(vlm3Id,new Version(2,0),featureGroupsforVlm3,
        licenseAgreementVlm3.getId());

    Version vlmVersion = vspDetailsVsp3.getVlmVersion();

    licenseArtifacts = vendorLicenseArtifactsService
        .createLicenseArtifacts(vspDetailsVsp3.getId(), vspDetailsVsp3.getVendorId(),
            vlmVersion,
            vspDetailsVsp3.getFeatureGroups(), USER1);
    String actual = IOUtils.toString(licenseArtifacts
            .getFileContent(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH),
        StandardCharsets.UTF_8);
    System.out.println("vlmVersionOneThenAddEp_bug2629 = " + actual);

    int countVersion2appearances = StringUtils.countMatches(actual, "<version>2.0</version>");
    int countVersion1appearances = StringUtils.countMatches(actual, "<version>1.0</version>");
    Assert.assertEquals(countVersion2appearances, 1);
    Assert.assertEquals(countVersion1appearances, 2);
  }


  @BeforeClass
  public void setUp() {
    super.setUp();
  }

*/

}
