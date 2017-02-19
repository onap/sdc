package org.openecomp.sdc.vendorlicense.licenseartifacts.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.vendorlicense.ArtifactTestUtils;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class VendorLicenseArtifactsServiceTest extends ArtifactTestUtils {
    private FileContentHandler licenseArtifacts;


    @Test
    public void createVNFArtifact() throws Exception {
        Version vlmVersion = vspDetails.getVlmVersion();
        licenseArtifacts = vendorLicenseArtifactsService.createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion, vspDetails.getFeatureGroups(), USER1);
        String actual = IOUtils.toString(licenseArtifacts.getFileContent(
            org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH), StandardCharsets.UTF_8);
//        System.out.println("createVnfArtifact = " + actual);

        Assert.assertTrue(actual.contains("type"));
        Assert.assertFalse(actual.contains(lkg13Id));
        Assert.assertTrue(actual.contains(OperationalScope.Availability_Zone.toString()));
        Assert.assertTrue(actual.contains("vf-id"));
        Assert.assertFalse(actual.contains(org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_REGEX_REMOVE));
        Assert.assertFalse(actual.contains("80.0"));
        Assert.assertTrue(actual.contains("80"));

    }

    @Test
    public void createVendorLicenseArtifact() throws Exception {
        Version vlmVersion = vspDetails.getVlmVersion();

        licenseArtifacts = vendorLicenseArtifactsService.createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion, vspDetails.getFeatureGroups(), USER1);
        String actual = IOUtils.toString(licenseArtifacts.getFileContent(
            org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH), StandardCharsets.UTF_8);

//        System.out.println("createVendorLicenseArtifact = " + actual);
        Assert.assertFalse(actual.contains(lkg11Id));
        Assert.assertFalse(actual.contains(ep11Id));
        Assert.assertTrue(actual.contains("type"));
        Assert.assertTrue(actual.contains(EntitlementMetric.Core.toString()));
        Assert.assertTrue(actual.contains("entitlement-pool-list"));
        Assert.assertFalse(actual.contains(org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_REGEX_REMOVE));

        Assert.assertTrue(actual.contains("vendor-license-model"));
        Assert.assertFalse(actual.contains("80.0"));
        Assert.assertTrue(actual.contains("80"));
    }

    @Test
    public void vNFArtifactContainsCurrentVLMVersion() throws IOException {
        super.setVlm2FirstVersion();
        licenseArtifacts = vendorLicenseArtifactsService.createLicenseArtifacts(vsp2.getId(), vsp2.getVendorId(), vsp2.getVlmVersion(), vsp2.getFeatureGroups(), USER1);
        String actual = IOUtils.toString(licenseArtifacts.getFileContent(
            org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH), StandardCharsets.UTF_8);

        Assert.assertTrue(actual.contains(ep21.getVersionUuId()));
    }

    @Test
    public void vnfArtifactContainsSameIdAsVLMArtifact() throws IOException {
        Version vlmVersion = vspDetails.getVlmVersion();
        licenseArtifacts = vendorLicenseArtifactsService.createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion, vspDetails.getFeatureGroups(), USER1);
        String actualVnfArtifact = IOUtils.toString(licenseArtifacts.getFileContent(
            org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH), StandardCharsets.UTF_8);
        String actualVendorLicenseArtifact = IOUtils.toString(licenseArtifacts.getFileContent(
            org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH), StandardCharsets.UTF_8);

        String firstLKGUUID = actualVnfArtifact.substring(actualVnfArtifact.indexOf("<license-key-group-uuid>") + 24, actualVnfArtifact.indexOf("<license-key-group-uuid>") + 60);
        Assert.assertTrue(actualVendorLicenseArtifact.contains(firstLKGUUID));

        String firstEPUUID = actualVnfArtifact.substring(actualVnfArtifact.indexOf("<<entitlement-pool-uuid>>") + 23, actualVnfArtifact.indexOf("<<entitlement-pool-uuid>>") + 60);
        Assert.assertTrue(actualVendorLicenseArtifact.contains(firstEPUUID));
    }


    @Test
    public void vNFArtifactContainsPreviousVLMVersionAndNotLatest() throws IOException {
        super.setVlm2SecondVersion();
        licenseArtifacts = vendorLicenseArtifactsService.createLicenseArtifacts(vsp2.getId(), vsp2.getVendorId(), vsp2.getVlmVersion(), vsp2.getFeatureGroups(), USER1);
        String actual = IOUtils.toString(licenseArtifacts.getFileContent(
            org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH), StandardCharsets.UTF_8);

        Assert.assertTrue(actual.contains(lkg21.getVersionUuId()));
        Assert.assertTrue(actual.contains(ep21.getVersionUuId()));
        Assert.assertFalse(actual.contains(lkg22Id));
        Assert.assertFalse(actual.contains(ep22Id));


        Assert.assertTrue(actual.contains("80"));
    }


    @Test
    public void onlyAddChangedEntitiesToVendorArtifact() throws IOException {
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

        createThirdFinalVersionForVLMChangeEpLKGInSome(ep11.getVendorLicenseModelId(), updatedEP, updatedLKG);
        licenseArtifacts = vendorLicenseArtifactsService.createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion, vspDetails.getFeatureGroups(), USER1);
        String actual = IOUtils.toString(licenseArtifacts.getFileContent(
            org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH), StandardCharsets.UTF_8);
//        System.out.println("onlyAddChangedEntitiesToVendorArtifact = " + actual);

        int countUpdatedLKG = StringUtils.countMatches(actual, updateDescLKG);
        Assert.assertEquals(countUpdatedLKG, 1);

        int countUpdatedEp = StringUtils.countMatches(actual, updatedNameEP);
        Assert.assertEquals(countUpdatedEp, 1);

        int epOccurrences = StringUtils.countMatches(actual, "<entitlement-pool>");
        Assert.assertEquals(epOccurrences, 3);
    }

    @BeforeClass
    public void setUp() {
        super.setUp();
    }
}

