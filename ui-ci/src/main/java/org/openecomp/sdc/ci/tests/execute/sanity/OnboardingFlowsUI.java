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

package org.openecomp.sdc.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.*;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.XnfTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ArtifactsCorrelationManager;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.*;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class OnboardingFlowsUI extends SetupCDTest {

    protected static String filePath = FileHandling.getVnfRepositoryPath();
    protected String makeDistributionValue;

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
        makeDistributionValue = makeDistributionReadValue;
    }

    @Test
    public void onboardVNFTestSanityOneFile() throws Throwable {
        String vnfFile = "1-VF-vUSP-vCCF-DB_v11.1.zip";
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        runOnboardToDistributionFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test
    public void performanceTest() throws Throwable {
        System.out.println("Start test");
        Long actualTestRunTime = Utils.getActionDuration(() -> {
            try {
                onboardVNFTestSanityOneFile();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        Long regularTestRunTime = 400L;
        double factor = 1.5;
        assertTrue("Expected test run time should be less from " + regularTestRunTime * factor + ", actual time is " + actualTestRunTime, regularTestRunTime * factor > actualTestRunTime);
    }

    @Test
    public void onboardVNFTestSanity() throws Throwable {
        List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.VNF);
        String vnfFile = fileNamesFromFolder.get(0).toString();
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        runOnboardToDistributionFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVNFflow(String filePath, String vnfFile) throws Exception, Throwable {
        setLog(vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        System.out.println("print - >" + makeDistributionValue);
        runOnboardToDistributionFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPValidationsSanityFlow(String filePath, String vnfFile) throws Exception, Throwable {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if(OnboardingUiUtils.getVspValidationCongiguration()){
            goToVspScreen(true, vspName);

            //check links are available
            checkVspValidationLinksVisibility();

            VspValidationPage.navigateToVspValidationPageUsingNavbar();
            assertTrue("Next Button is enabled, it should have been disabled", VspValidationPage.checkNextButtonDisabled());
            VspValidationResultsPage.navigateToVspValidationResultsPageUsingNavbar();
            GeneralUIUtils.ultimateWait();
            assertNotNull(GeneralUIUtils.findByText("No Validation Checks Performed"));
        }
        else {
            goToVspScreen(true, vspName);

            //check links are not available
            checkVspValidationLinksInvisibility();
        }
    }


    @Test(dataProviderClass = org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPValidationsConfigurationChangeCheck(String filePath, String vnfFile) throws Exception, Throwable {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if(OnboardingUiUtils.getVspValidationCongiguration()){
            goToVspScreen(true, vspName);
            //check links are available
            checkVspValidationLinksVisibility();

            //change config
            changeVspValidationConfig(false, vspName, OnboardingUiUtils.getVspValidationCongiguration());

            //check links are not available
            checkVspValidationLinksInvisibility();
        }
        else {
            goToVspScreen(true, vspName);
            //check links are not available
            checkVspValidationLinksInvisibility();

            changeVspValidationConfig(false, vspName, OnboardingUiUtils.getVspValidationCongiguration());

            //check links are available
            checkVspValidationLinksVisibility();
        }
    }

    @Test(dataProviderClass = org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPCertificationQueryFlow(String filePath, String vnfFile) throws Exception, Throwable {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if(!OnboardingUiUtils.getVspValidationCongiguration()){
            //change config to true to test the feature
            changeVspValidationConfig(true, vspName, OnboardingUiUtils.getVspValidationCongiguration());
        }
        else {
            goToVspScreen(true, vspName);
        }
        VspValidationPage.navigateToVspValidationPageUsingNavbar();
        assertTrue("Next Button is enabled, it should have been disabled", VspValidationPage.checkNextButtonDisabled());

        if(VspValidationPage.checkCertificationQueryExists()){
            VspValidationPage.clickCertificationQueryAll();
            GeneralUIUtils.ultimateWait();
            assertTrue("Next Button is disabled, it should have been enabled", !VspValidationPage.checkNextButtonDisabled());
            VspValidationPage.clickOnNextButton();
            GeneralUIUtils.ultimateWait();
            VspValidationPage.clickOnSubmitButton();
            GeneralUIUtils.waitForLoader();
            assertTrue("Results are not available", VspValidationResultsPage.checkResultsExist());
        }
        else {
            assertNotNull(GeneralUIUtils.findByText("No Certifications Query are Available"));
        }

    }

    @Test(dataProviderClass = org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPComplianceCheckFlow(String filePath, String vnfFile) throws Exception, Throwable {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if(!OnboardingUiUtils.getVspValidationCongiguration()){
            //change config to true to test the feature
            changeVspValidationConfig(true, vspName, OnboardingUiUtils.getVspValidationCongiguration());
        }
        else {
            goToVspScreen(true, vspName);
        }

        VspValidationPage.navigateToVspValidationPageUsingNavbar();
        assertTrue("Next Button is enabled, it should have been enabled", VspValidationPage.checkNextButtonDisabled());
        if(VspValidationPage.checkComplianceCheckExists()){
            VspValidationPage.clickComplianceChecksAll();
            GeneralUIUtils.ultimateWait();
            assertTrue("Next Button is disabled, it should have been enabled", !VspValidationPage.checkNextButtonDisabled());
            VspValidationPage.clickOnNextButton();
            GeneralUIUtils.ultimateWait();
            VspValidationPage.clickOnSubmitButton();
            GeneralUIUtils.waitForLoader();
            assertTrue("Results are not available", VspValidationResultsPage.checkResultsExist());
        }
        else {
            assertNotNull(GeneralUIUtils.findByText("No Compliance Checks are Available"));
        }

    }

    private void checkVspValidationLinksVisibility(){
        //check links are available
        assertTrue("Validation Link is not available", GeneralUIUtils.isElementVisibleByTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR.getValue()));
        assertTrue("Validation Results Link is not available", GeneralUIUtils.isElementVisibleByTestId(DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_NAVBAR.getValue()));
    }

    private void checkVspValidationLinksInvisibility(){
        //check links not available
        assertTrue("Validation Link is still available", GeneralUIUtils.isElementInvisibleByTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR.getValue()));
        assertTrue("Validation Results Link is still available", GeneralUIUtils.isElementInvisibleByTestId(DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_NAVBAR.getValue()));
    }

    private void changeVspValidationConfig(boolean isCurrentScreenCatalogPage, String vspName, boolean vspConfig) throws Exception{
        //change config
        OnboardingUiUtils.putVspValidationCongiguration(!vspConfig);
        assertTrue(String.format("Failed to set Congiguration to %s", !vspConfig), OnboardingUiUtils.getVspValidationCongiguration() != vspConfig);

        if(!isCurrentScreenCatalogPage){
            GeneralUIUtils.refreshWebpage();
            GeneralUIUtils.ultimateWait();
        }

        goToVspScreen(isCurrentScreenCatalogPage, vspName);

        //revert the config
        OnboardingUiUtils.putVspValidationCongiguration(vspConfig);
        assertTrue(String.format("Failed to revert Congiguration to %s", vspConfig), OnboardingUiUtils.getVspValidationCongiguration() == vspConfig);
    }

    private void goToVspScreen(boolean isCurrentScreenCatalogPage, String vspName) throws Exception{
        if(isCurrentScreenCatalogPage)
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtons.ONBOARD_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByText(vspName);
        GeneralUIUtils.waitForLoader();
    }

    private String createNewVSP(String filePath, String vnfFile) throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        return OnboardingUiUtils.createVSP(resourceReqDetails, vnfFile, filePath, getUser()).getName();
    }
    public void runOnboardToDistributionFlow(ResourceReqDetails resourceReqDetails, ServiceReqDetails serviceMetadata, String filePath, String vnfFile) throws Exception {
        getExtendTest().log(Status.INFO, "Going to create resource with category: " + resourceReqDetails.getCategories().get(0).getName()
                + " subCategory: " + resourceReqDetails.getCategories().get(0).getSubcategories().get(0).getName()
                + " and service category: " + serviceMetadata.getCategory());
        String vspName = onboardAndCertify(resourceReqDetails, filePath, vnfFile);

        //TODO Andrey check return window after certification
        /*reloginWithNewRole(UserRoleEnum.DESIGNER);*/
        // create service
//		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CompositionPage.searchForElement(vspName);
        CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
        ArtifactsCorrelationManager.addVNFtoServiceArtifactCorrelation(serviceMetadata.getName(), vspName);

        assertNotNull(vfElement);
        ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());
        ExtentTestActions.addScreenshot(Status.INFO, "ServiceComposition_" + vnfFile, "The service topology is as follows: ");

        ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        TesterOperationPage.certifyComponent(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata.getName());

        if (makeDistributionValue.equals("true")) {


            reloginWithNewRole(UserRoleEnum.OPS);
            GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
            OpsOperationPage.distributeService();
            OpsOperationPage.displayMonitor();

            List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
            AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());

            OpsOperationPage.waitUntilArtifactsDistributed(0);

//		validateInputArtsVSouput(serviceMetadata.getName());

        }

        getExtendTest().log(Status.INFO, String.format("The onboarding %s test is passed ! ", vnfFile));
    }

    public String onboardAndCertify(ResourceReqDetails resourceReqDetails, String filePath, String vnfFile) throws Exception {
        VendorSoftwareProductObject onboardAndValidate = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFile, getUser());
        String vspName = onboardAndValidate.getName();

        DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
        ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFile, "The topology template for " + vnfFile + " is as follows : ");

        DeploymentArtifactPage.clickCertifyButton(vspName);
        return vspName;
    }


    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "VNF_List")
    public void onboardVNFTest(String filePath, String vnfFile) throws Throwable {
        setLog(vnfFile);
        System.out.println("printttttttttttttt - >" + makeDistributionValue);
        ResourceReqDetails resourceReqDetails = ElementFactory.getRandomCategoryResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getRandomCategoryService();
        runOnboardToDistributionFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "VNF_List")
    public void onboardVNFShotFlow(String filePath, String vnfFile) throws Throwable {
        setLog(vnfFile);
        System.out.println("printttttttttttttt - >" + makeDistributionValue);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        onboardAndCertify(resourceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
    public void onboardRandomVNFsTest(String filePath, String vnfFile) throws Throwable {
        setLog(vnfFile);
        System.out.println("printttttttttttttt - >" + makeDistributionValue);
        System.out.println("Vnf File name is: " + vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getRandomCategoryResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getRandomCategoryService();
        runOnboardToDistributionFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }


    @Test
    public void onboardUpdateVNFTest() throws Throwable {
        List<String> fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filePath);
        String vnfFile = fileNamesFromFolder.get(0);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFile, getUser());
        String vspName = vsp.getName();
        ResourceGeneralPage.clickCertifyButton(vspName);

        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CompositionPage.searchForElement(vspName);
        CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
        assertNotNull(vfElement);
        ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

        HomePage.navigateToHomePage();

        ///update flow
        String updatedVnfFile = fileNamesFromFolder.get(1);

        getExtendTest().log(Status.INFO, String.format("Going to update the VNF with %s......", updatedVnfFile));
        // update VendorSoftwareProduct
        OnboardingUiUtils.updateVnfAndValidate(filePath, vsp, updatedVnfFile, getUser());
        ResourceGeneralPage.clickCertifyButton(vspName);

        // replace exiting VFI in service with new updated

        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        serviceCanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.changeComponentVersion(serviceCanvasManager, vfElement, "2.0");
        ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

        ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        TesterOperationPage.certifyComponent(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata.getName());


        reloginWithNewRole(UserRoleEnum.OPS);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        OpsOperationPage.distributeService();
        OpsOperationPage.displayMonitor();

        List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
        AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());

        OpsOperationPage.waitUntilArtifactsDistributed(0);
        getExtendTest().log(Status.INFO, String.format("Onboarding %s test is passed ! ", vnfFile));
    }


    @Test
    public void threeVMMSCsInServiceTest() throws Exception {

        String pathFile = FileHandling.getFilePath("VmmscArtifacts");
        List<String> vmmscList = Arrays.asList(new File(pathFile).list()).stream().filter(e -> e.contains("vmmsc") && e.endsWith(".zip")).collect(Collectors.toList());
        assertTrue("Did not find vMMSCs", vmmscList.size() > 0);

        Map<String, String> vspNames = new HashMap<>();
        for (String vnfFile : vmmscList) {
            getExtendTest().log(Status.INFO, String.format("Going to onboard the VNF %s......", vnfFile));
            System.out.println(String.format("Going to onboard the VNF %s......", vnfFile));

            AmdocsLicenseMembers amdocsLicenseMembers = VendorLicenseModelRestUtils.createVendorLicense(getUser());
            ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
            VendorSoftwareProductObject createVendorSoftwareProduct = VendorSoftwareProductRestUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, pathFile, getUser(), amdocsLicenseMembers);

            getExtendTest().log(Status.INFO, String.format("Searching for onboarded %s", vnfFile));
            HomePage.showVspRepository();
            getExtendTest().log(Status.INFO, String.format("Going to import %s......", vnfFile.substring(0, vnfFile.indexOf("."))));
            OnboardingUiUtils.importVSP(createVendorSoftwareProduct);

            ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
            DeploymentArtifactPage.verifyArtifactsExistInTable(pathFile, vnfFile);

            String vspName = createVendorSoftwareProduct.getName();
            DeploymentArtifactPage.clickCertifyButton(vspName);
            vspNames.put(vnfFile, vspName);
        }
		
	/*	reloginWithNewRole(UserRoleEnum.TESTER);
		for (String vsp : vspNames.values()){
			GeneralUIUtils.findComponentAndClick(vsp);
			TesterOperationPage.certifyComponent(vsp);
		}
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);*/
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();

        for (String vsp : vspNames.values()) {
            CompositionPage.searchForElement(vsp);
            CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vsp);
            assertNotNull(vfElement);
        }
        ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", vspNames.values().size(), getUser());
        File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Info_" + getExtendTest().getModel().getName());
        final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
        SetupCDTest.getExtendTest().log(Status.INFO, "Three kinds of vMMSC are in canvas now." + getExtendTest().addScreenCaptureFromPath(absolutePath));

        ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        TesterOperationPage.certifyComponent(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.OPS);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        OpsOperationPage.distributeService();
        OpsOperationPage.displayMonitor();

        List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
        AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());

        OpsOperationPage.waitUntilArtifactsDistributed(0);

    }


    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
