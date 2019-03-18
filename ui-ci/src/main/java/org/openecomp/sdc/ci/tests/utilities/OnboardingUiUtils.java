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

package org.openecomp.sdc.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.*;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.execute.setup.ArtifactsCorrelationManager;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class OnboardingUiUtils {

    private static void importUpdateVSP(VendorSoftwareProductObject vsp, boolean isUpdate, boolean restore) throws Exception {
        String vspName = vsp.getName();
        boolean vspFound = HomePage.searchForVSP(vspName);

        if (vspFound) {
            List<WebElement> elementsFromTable = HomePage.getElemenetsFromTable();
            elementsFromTable.get(1).click();
            GeneralUIUtils.waitForLoader();

            if (isUpdate) {
                GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.UPDATE_VSP.getValue());
            } else {
                GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.IMPORT_VSP.getValue());
            }
            if (restore) {
                GeneralPageElements.restoreComponentFromElementPage(vspName);
            }
            doCheckOut();
            //Metadata verification
            onboardedVnfMetadataVerification(vsp, isUpdate);
            String duration = GeneralUIUtils.getActionDuration(() -> waitUntilVnfCreated());
            ExtentTestActions.log(Status.INFO, "Succeeded in importing/updating " + vspName, duration);
        } else {
            Assert.fail("Did not find VSP named " + vspName);
        }
    }

    private static void onboardedVnfMetadataVerification(VendorSoftwareProductObject vsp, boolean isUpdate) {
        if (isUpdate) {
            VfVerificator.verifyOnboardedVnfMetadataAfterUpdateVNF(vsp.getName(), vsp);
        } else {
            VfVerificator.verifyOnboardedVnfMetadata(vsp.getName(), vsp);
        }
    }

    public static void doCheckOut() {
        String lifeCycleState = ResourceGeneralPage.getLifeCycleState();
        boolean needCheckout = lifeCycleState.equals(LifeCycleStateEnum.CHECKIN.getValue()) || lifeCycleState.equals(LifeCycleStateEnum.CERTIFIED.getValue());
        if (needCheckout) {
            try {
                ResourceGeneralPage.clickCheckoutButton();
                Assert.assertTrue(ResourceGeneralPage.getLifeCycleState().equals(LifeCycleStateEnum.CHECKOUT.getValue()), "Did not succeed to checkout");
            } catch (Exception e) {
                ExtentTestActions.log(Status.ERROR, "Did not succeed to checkout");
                e.printStackTrace();
            }
            GeneralUIUtils.waitForLoader();
        }
    }

    private static void waitUntilVnfCreated() {
        ExtentTestActions.log(Status.INFO, "Clicking create/update VNF");
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
        GeneralUIUtils.waitForLoader(60 * 10);
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
    }

    public static void updateVSP(VendorSoftwareProductObject vsp, boolean restore) throws Exception {
        ExtentTestActions.log(Status.INFO, "Updating VSP " + vsp.getName());
        importUpdateVSP(vsp, true, restore);
    }

    public static void updateVSP(VendorSoftwareProductObject vsp) throws Exception {
        ExtentTestActions.log(Status.INFO, "Updating VSP " + vsp.getName());
        importUpdateVSP(vsp, true, false);
    }

    public static void importVSP(VendorSoftwareProductObject vsp) throws Exception {
        ExtentTestActions.log(Status.INFO, "Importing VSP " + vsp.getName());
        importUpdateVSP(vsp, false, false);
    }

    public static void updateVnfAndValidate(String filePath, VendorSoftwareProductObject vsp, String updatedVnfFile, User user) throws Throwable {
        ExtentTestActions.log(Status.INFO, String.format("Going to update the VNF with %s......", updatedVnfFile));
        System.out.println(String.format("Going to update the VNF with %s......", updatedVnfFile));

        VendorSoftwareProductRestUtils.updateVendorSoftwareProductToNextVersion(vsp, user, filePath, updatedVnfFile);
        HomePage.showVspRepository();
        updateVSP(vsp);
        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.verifyArtifactsExistInTable(filePath, updatedVnfFile);
    }

    public static VendorSoftwareProductObject createVSP(ResourceReqDetails resourceReqDetails, String vnfFile, String filepath, User user) throws Exception {
        ExtentTestActions.log(Status.INFO, String.format("Going to onboard the VNF %s", vnfFile));
        System.out.println(String.format("Going to onboard the VNF %s", vnfFile));

        AmdocsLicenseMembers amdocsLicenseMembers = VendorLicenseModelRestUtils.createVendorLicense(user);
        return VendorSoftwareProductRestUtils.createVSP(resourceReqDetails, vnfFile, filepath, user, amdocsLicenseMembers);
    }


    public static VendorSoftwareProductObject onboardAndValidate(ResourceReqDetails resourceReqDetails, String filepath, String vnfFile, User user) throws Exception {
        ExtentTestActions.log(Status.INFO, String.format("Going to onboard the VNF %s", vnfFile));
        System.out.println(String.format("Going to onboard the VNF %s", vnfFile));

        AmdocsLicenseMembers amdocsLicenseMembers = VendorLicenseModelRestUtils.createVendorLicense(user);
        VendorSoftwareProductObject createVendorSoftwareProduct = VendorSoftwareProductRestUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, user, amdocsLicenseMembers);
        String vspName = createVendorSoftwareProduct.getName();

        DownloadManager.downloadCsarByNameFromVSPRepository(vspName, createVendorSoftwareProduct.getVspId());
        File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();

        ExtentTestActions.log(Status.INFO, String.format("Searching for onboarded %s", vnfFile));
        HomePage.showVspRepository();
        ExtentTestActions.log(Status.INFO, String.format("Going to import %s", vnfFile.substring(0, vnfFile.indexOf("."))));
        importVSP(createVendorSoftwareProduct);

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

        // Verify deployment artifacts
        Map<String, Object> combinedMap = ArtifactFromCsar.combineHeatArtifacstWithFolderArtifacsToMap(latestFilefromDir.getAbsolutePath());

        LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get("Deployment"));
        ArtifactsCorrelationManager.addVNFartifactDetails(vspName, deploymentArtifacts);

        List<String> heatEnvFilesFromCSAR = deploymentArtifacts.stream().filter(e -> e.getType().equals("HEAT_ENV")).
                map(e -> e.getFileName()).
                collect(Collectors.toList());

        validateDeploymentArtifactsVersion(deploymentArtifacts, heatEnvFilesFromCSAR);

        DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, vnfFile);
        return createVendorSoftwareProduct;
    }

    public static void validateDeploymentArtifactsVersion(LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts,
                                                          List<String> heatEnvFilesFromCSAR) {
        String artifactVersion;
        String artifactName;

        for (HeatMetaFirstLevelDefinition deploymentArtifact : deploymentArtifacts) {
            artifactVersion = "1";

            if (deploymentArtifact.getType().equals("HEAT_ENV")) {
                continue;
            } else if (deploymentArtifact.getFileName().contains(".")) {
                artifactName = deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().lastIndexOf("."));
            } else {
                artifactName = deploymentArtifact.getFileName().trim();
            }

            if (heatEnvFilesFromCSAR.contains(artifactName + ".env")) {
                artifactVersion = "2";
            }
            ArtifactUIUtils.validateArtifactNameVersionType(artifactName, artifactVersion, deploymentArtifact.getType());
        }
    }

}
