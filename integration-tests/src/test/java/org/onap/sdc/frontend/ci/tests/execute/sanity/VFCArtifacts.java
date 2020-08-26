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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.apache.http.HttpStatus;
import org.onap.sdc.backend.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.VendorLicenseModel;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.execute.devCI.ArtifactFromCsar;
import org.onap.sdc.frontend.ci.tests.pages.DeploymentArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.HomePage;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.onap.sdc.backend.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.onap.sdc.frontend.ci.tests.verificator.VFCArtifactVerificator;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.frontend.ci.tests.utilities.DownloadManager;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.OnboardingUiUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.RestCDUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;

public class VFCArtifacts extends SetupCDTest {

    private static final String DEPLOYMENT = "Deployment";
    private static final String INFORMATIONAL = "Informational";
    private static final String ARTIFACTS = "artifacts";
    private static final String DEPLOYMENT_ARTIFACTS = "deploymentArtifacts";
    private String filePath;

    @BeforeClass
    public void beforeClass() {
        filePath = FileHandling.getFilePath("VFCArtifacts");
    }

    @Test
    public void ImportMultiVFCTest_TC1407998() throws Exception {

        String csarFile = "Import_Multi_VFC.csar";

        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarFile, getUser());

        RestResponse getResponse = RestCDUtils.getResource(resourceMetaData, getUser());
        assertTrue(getResponse.getErrorCode().intValue() == HttpStatus.SC_OK);

        Map<String, LinkedList<HeatMetaFirstLevelDefinition>> expectedArtifactMap = verifyVfcArtifacts(filePath, csarFile, resourceMetaData, getResponse);

        VFCArtifactVerificator.verifyVFCArtifactsNotInVFArtifactList(resourceMetaData, getUser(), getResponse, expectedArtifactMap);


    }

    @Test
    public void updateCsarWithVFCArtifacts_ModifyArtifacts_TC1449482() throws Exception {

        String csarFile = "LDSA-ORIG.csar";
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarFile, getUser());

        Map<String, Object> artifactsFromCsar = ArtifactFromCsar.getVFCArtifacts(filePath + csarFile);
        List<String> vfcKeys = artifactsFromCsar.keySet().stream().filter(p -> p.contains("vfc")).collect(Collectors.toList());
        for (String key : vfcKeys) {
            VFCArtifactVerificator.setActualVfcArtifactList(key, resourceMetaData, getUser());
        }

        String updatedCsarFile = "LDSA-MODIFY.csar";
        ResourceUIUtils.updateVfWithCsar(filePath, updatedCsarFile);

        Map<String, Object> updatedArtifactsFromCsar = ArtifactFromCsar.getVFCArtifacts(filePath + updatedCsarFile);
        List<String> updatedVfcKeys = updatedArtifactsFromCsar.keySet().stream().filter(p -> p.contains("vfc")).collect(Collectors.toList());
        for (String key : updatedVfcKeys) {
            verifyVfcInstanceArtifacts(resourceMetaData, null, updatedArtifactsFromCsar, key);
            VFCArtifactVerificator.verifyVfcArtifactUpdated(key, resourceMetaData, getUser());
        }
    }

    @Test
    public void updateCsarWithVFCArtifacts_DeleteAndAddArtifacts_TC1449473() throws Exception {

        String csarFile = "LDSA-ORIG.csar";
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarFile, getUser());

        String updatedCsarFile = "LDSA-DELETE-ADD.csar";
        ResourceUIUtils.updateVfWithCsar(filePath, updatedCsarFile);

        verifyVfcArtifacts(filePath, updatedCsarFile, resourceMetaData, null);
    }

    @Test
    public void updateCsarWithVFCArtifacts_AddFirstVFCIdentifier_TC1425896() throws Exception {

        String csarFile = "LDSA-ORIG-OLD_STRUCTURE.csar";
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarFile, getUser());

        VFCArtifactVerificator.verifyNoVfcArtifacts(resourceMetaData, getUser(), null);

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        String[] artifactNamesFromFile = ArtifactFromCsar.getArtifactNamesFromCsar(filePath, csarFile);
        String[] artifactsFromFileBeforeUpdate = DeploymentArtifactPage.verifyArtifactsExistInTable(artifactNamesFromFile);
        DeploymentArtifactPage.getLeftMenu().moveToGeneralScreen();

        String updatedCsarFile = "LDSA-ADD.csar";
        ResourceUIUtils.updateVfWithCsar(filePath, updatedCsarFile);

        verifyVfcArtifacts(filePath, updatedCsarFile, resourceMetaData, null);

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.verifyArtifactsExistInTable(artifactsFromFileBeforeUpdate);
    }


    @Test
    public void updateCsarWithVFCArtifacts_AddAdditionalVFCIdentifier_TC1425898() throws Exception {

        String csarFile = "LDSA-SINGLE.csar";
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarFile, getUser());

        Map<String, Object> artifactsFromCsar = ArtifactFromCsar.getVFCArtifacts(filePath + csarFile);
        List<String> vfcKeys = artifactsFromCsar.keySet().stream().filter(p -> p.contains("vfc")).collect(Collectors.toList());
        for (String key : vfcKeys) {
            VFCArtifactVerificator.setActualVfcArtifactList(key, resourceMetaData, getUser());
        }

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        String[] artifactNamesFromFile = ArtifactFromCsar.getArtifactNamesFromCsar(filePath, csarFile);
        String[] artifactsFromFileBeforeUpdate = DeploymentArtifactPage.verifyArtifactsExistInTable(artifactNamesFromFile);
        DeploymentArtifactPage.getLeftMenu().moveToGeneralScreen();

        String updatedCsarFile = "LDSA-MULTI.csar";
        ResourceUIUtils.updateVfWithCsar(filePath, updatedCsarFile);

        Map<String, Object> updatedArtifactsFromCsar = ArtifactFromCsar.getVFCArtifacts(filePath + updatedCsarFile);
        List<String> updatedVfcKeys = updatedArtifactsFromCsar.keySet().stream().filter(p -> p.contains("vfc")).collect(Collectors.toList());
        for (String key : updatedVfcKeys) {
            verifyVfcInstanceArtifacts(resourceMetaData, null, updatedArtifactsFromCsar, key);
            if (vfcKeys.contains(key)) {
                VFCArtifactVerificator.verifyVFCArtifactNotChanged(key, resourceMetaData, getUser());
            }
        }

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.verifyArtifactsExistInTable(artifactsFromFileBeforeUpdate);
    }

    @Test
    public void updateCsarWithVFCArtifacts_DeleteAll_TC1425581() throws Exception {

        String csarFile = "LDSA-ORIG.csar";
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarFile, getUser());

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        String[] artifactNamesFromFile = ArtifactFromCsar.getArtifactNamesFromCsar(filePath, csarFile);
        String[] artifactsFromFileBeforeUpdate = DeploymentArtifactPage.verifyArtifactsExistInTable(artifactNamesFromFile);
        DeploymentArtifactPage.getLeftMenu().moveToGeneralScreen();

        String updatedCsarFile = "LDSA-DELETE-ALL.csar";
        ResourceUIUtils.updateVfWithCsar(filePath, updatedCsarFile);

        VFCArtifactVerificator.verifyNoVfcArtifacts(resourceMetaData, getUser(), null);

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.verifyArtifactsExistInTable(artifactsFromFileBeforeUpdate);
    }

    @Test
    public void importComplexVFCArtifacts_Onboarding_TC1484153() throws Exception {

        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());

        String vnfFile = "2016-043_vsaegw_fdnt_30_1607_e2e.zip";
        String snmpFile = "Fault-alarms-ASDC-vprobes-vLB.zip";

        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        VendorSoftwareProductObject createVSP = VendorSoftwareProductRestUtils.createVSP(resourceMetaData, vnfFile, filePath, getUser(),
            vendorLicenseModel);
        String vspName = createVSP.getName();
        resourceMetaData.setName(vspName);
//		VendorSoftwareProductObject resourceMeta = createVSP.right;
        String vspid = createVSP.getVspId();
        VendorSoftwareProductRestUtils.addVFCArtifacts(filePath, snmpFile, null, createVSP, getUser());
        VendorSoftwareProductRestUtils.prepareVspForUse(getUser(), createVSP, true);

        String downloadDirectory = getWindowTest().getDownloadDirectory();
        String csarFile = vspid + ".csar";

        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, vspid);
        HomePage.showVspRepository();
        OnboardingUiUtils.importVSP(createVSP);
        resourceMetaData.setVersion("0.1");

        verifyVfcArtifacts(downloadDirectory, csarFile, resourceMetaData, null);

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.verifyArtifactsExistInTable(filePath, vnfFile);

    }

    @Test
    public void updateComplexVFCArtifacts_AddRemove_Onboarding_TC1484185() throws Exception {

        //check of version is 1
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());

        String vnfFile = "2016-043_vsaegw_fdnt_30_1607_e2e.zip";
        String snmpPollFile = "vprobes-vLB.zip";
        String updatedSnmpPollFile = "vprobes-vLBAgent.zip";

        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        VendorSoftwareProductObject createVSP = VendorSoftwareProductRestUtils.createVSP(resourceMetaData, vnfFile, filePath, getUser(),
            vendorLicenseModel);
        String vspName = createVSP.getName();
        resourceMetaData.setName(vspName);
        String vspid = createVSP.getVspId();
        String monitoringComponentId = VendorSoftwareProductRestUtils.addVFCArtifacts(filePath, snmpPollFile, null, createVSP, getUser());
        VendorSoftwareProductRestUtils.prepareVspForUse(getUser(), createVSP, true);

        String downloadDirectory = getWindowTest().getDownloadDirectory();
        String csarFile = vspid + ".csar";

        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, vspid);
        HomePage.showVspRepository();
        OnboardingUiUtils.importVSP(createVSP);
        ResourceGeneralPage.clickCertifyButton(vspName);

        VendorSoftwareProductRestUtils.updateVspWithVfcArtifacts(filePath, updatedSnmpPollFile, null, monitoringComponentId, getUser(), createVSP);
        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, vspid);
        HomePage.showVspRepository();
        OnboardingUiUtils.updateVSP(createVSP);
        resourceMetaData.setVersion("1.1");

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.verifyArtifactsExistInTable(filePath, vnfFile);

        verifyVfcArtifacts(downloadDirectory, csarFile, resourceMetaData, null);

    }

    @Test
    public void updateComplexVFCArtifacts_Modify_Onboarding_TC1484195() throws Exception {

        //check of version is 2
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());

        String vnfFile = "2016-043_vsaegw_fdnt_30_1607_e2e.zip";
        String snmpFile = "vprobes-vLB.zip";
        String updatedSnmpFile = "vprobes-vLB-Modified.zip";

        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        VendorSoftwareProductObject createVSP = VendorSoftwareProductRestUtils.createVSP(resourceMetaData, vnfFile, filePath, getUser(),
            vendorLicenseModel);
        String vspName = createVSP.getName();
        resourceMetaData.setName(vspName);
        String vspid = createVSP.getVspId();
        String monitoringId = VendorSoftwareProductRestUtils.addVFCArtifacts(filePath, snmpFile, null, createVSP, getUser());
        VendorSoftwareProductRestUtils.prepareVspForUse(getUser(), createVSP, true);

        String downloadDirectory = getWindowTest().getDownloadDirectory();
        String csarFile = vspid + ".csar";

        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, vspid);
        HomePage.showVspRepository();
        OnboardingUiUtils.importVSP(createVSP);

        Map<String, Object> artifactsFromCsar = ArtifactFromCsar.getVFCArtifacts(downloadDirectory + csarFile);
        List<String> vfcKeys = artifactsFromCsar.keySet().stream().filter(p -> p.contains("vfc")).collect(Collectors.toList());
        for (String key : vfcKeys) {
            resourceMetaData.setVersion("0.1");
            VFCArtifactVerificator.setActualVfcArtifactList(key, resourceMetaData, getUser());
        }

        ResourceGeneralPage.clickCertifyButton(vspName);
        VendorSoftwareProductRestUtils.updateVspWithVfcArtifacts(filePath, updatedSnmpFile, null, monitoringId, getUser(), createVSP);
        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, vspid);
        HomePage.showVspRepository();
        OnboardingUiUtils.updateVSP(createVSP);
        resourceMetaData.setVersion("1.1");

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.verifyArtifactsExistInTable(filePath, vnfFile);

        Map<String, Object> artifactsFromCsarAfterUpdate = ArtifactFromCsar.getVFCArtifacts(downloadDirectory + csarFile);
        List<String> vfcKeysAfterUpdate = artifactsFromCsarAfterUpdate.keySet().stream().filter(p -> p.contains("vfc")).collect(Collectors.toList());
        for (String key : vfcKeysAfterUpdate) {
            verifyVfcInstanceArtifacts(resourceMetaData, null, artifactsFromCsarAfterUpdate, key);
            VFCArtifactVerificator.verifyVfcArtifactUpdated(key, resourceMetaData, getUser());
        }

    }


    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

    private Map<String, LinkedList<HeatMetaFirstLevelDefinition>> verifyVfcArtifacts(String filepath, String csarFile,
                                                                                     ResourceReqDetails resourceMetaData, RestResponse getResponse) throws Exception {

        ExtentTestActions.log(Status.INFO, "Verifying VFC artifacts");
        Map<String, LinkedList<HeatMetaFirstLevelDefinition>> expectedArtifactMap = null;
        ExtentTestActions.log(Status.INFO, "Reading artifacts in CSAR file");
        Map<String, Object> artifactsFromCsar = ArtifactFromCsar.getVFCArtifacts(filepath + csarFile);
        List<String> vfcKeys = artifactsFromCsar.keySet().stream().filter(p -> p.contains("vfc")).collect(Collectors.toList());
        for (String key : vfcKeys) {
            expectedArtifactMap = verifyVfcInstanceArtifacts(resourceMetaData, getResponse, artifactsFromCsar, key);
        }
        return expectedArtifactMap;
    }

    private Map<String, LinkedList<HeatMetaFirstLevelDefinition>> verifyVfcInstanceArtifacts(
            ResourceReqDetails resourceMetaData, RestResponse getResponse, Map<String, Object> artifactsFromCsar, String key) {

        Map<String, LinkedList<HeatMetaFirstLevelDefinition>> expectedArtifactMap;
        Map<String, LinkedList<HeatMetaFirstLevelDefinition>> vfcDeploymentArtifacts = (Map<String, LinkedList<HeatMetaFirstLevelDefinition>>) artifactsFromCsar.get(key);
        LinkedList<HeatMetaFirstLevelDefinition> deploymentList = vfcDeploymentArtifacts.get(DEPLOYMENT);
        LinkedList<HeatMetaFirstLevelDefinition> informationalList = (LinkedList<HeatMetaFirstLevelDefinition>) vfcDeploymentArtifacts.get(INFORMATIONAL);

        expectedArtifactMap = new HashMap<String, LinkedList<HeatMetaFirstLevelDefinition>>();
        if (deploymentList == null) {
            expectedArtifactMap.put(DEPLOYMENT_ARTIFACTS, new LinkedList<HeatMetaFirstLevelDefinition>());
        } else {
            expectedArtifactMap.put(DEPLOYMENT_ARTIFACTS, deploymentList);
        }
        if (informationalList == null) {
            expectedArtifactMap.put(ARTIFACTS, new LinkedList<HeatMetaFirstLevelDefinition>());
        } else {
            expectedArtifactMap.put(ARTIFACTS, informationalList);
        }

        VFCArtifactVerificator.verifyVfcArtifacts(resourceMetaData, getUser(), key, expectedArtifactMap, getResponse);
        return expectedArtifactMap;
    }
}
