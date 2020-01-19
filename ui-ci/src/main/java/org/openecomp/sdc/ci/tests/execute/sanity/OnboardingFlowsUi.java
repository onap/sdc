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

import static org.openecomp.sdc.ci.tests.pages.GeneralPageElements.clickOKButton;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.ci.tests.data.providers.OnboardingDataProviders;
import org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.XnfTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.DriverFactory;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.flow.CheckSoftwareVersionPropertyFlow;
import org.openecomp.sdc.ci.tests.flow.CreateResourceFlow;
import org.openecomp.sdc.ci.tests.flow.CreateVspFlow;
import org.openecomp.sdc.ci.tests.flow.ImportVspFlow;
import org.openecomp.sdc.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.OpsOperationPage;
import org.openecomp.sdc.ci.tests.pages.ResourceCreatePage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.pages.TopNavComponent;
import org.openecomp.sdc.ci.tests.pages.VspValidationPage;
import org.openecomp.sdc.ci.tests.pages.VspValidationResultsPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class OnboardingFlowsUi extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingFlowsUi.class);
    private static final String NO_TESTS_ARE_AVAILABLE = "No Tests are Available";
    private static final String NEXT_BUTTON_IS_ENABLED_IT_SHOULD_HAVE_BEEN_ENABLED =
            "Next Button is enabled, it should have been enabled";
    private static final String NEXT_BUTTON_IS_DISABLED_IT_SHOULD_HAVE_BEEN_ENABLED =
            "Next Button is disabled, it should have been enabled";
    private static final String RESULTS_ARE_NOT_AVAILABLE = "Results are not available";
    private static final String THE_TESTS_ARE_ALREADY_SELECTED_THE_LIST_SHOULD_INITIALLY_BE_EMPTY =
            "The tests are already selected, the list should initially be empty";
    private static final String THE_SELECTED_TESTS_ARE_NOT_POPULATED_IN_THE_LIST =
            "The selected tests are not populated in the list";
    private static final String THE_SELECTED_TESTS_ARE_NOT_DELETED_FROM_THE_LIST =
            "The selected tests are not deleted from the list";
    private static final String NEXT_BUTTON_IS_ENABLED_IT_SHOULD_HAVE_BEEN_DISABLED =
            "Next Button is enabled, it should have been disabled";

    protected static String filePath = FileHandling.getVnfRepositoryPath();
    private Boolean makeDistributionValue;

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
        LOGGER.debug("makeDistribution parameter is '{}'", makeDistributionReadValue);
        makeDistributionValue = Boolean.valueOf(makeDistributionReadValue);
    }

    @Test
    public void onboardVNFTestSanityOneFile() throws Exception {
        String vnfFile = "1-VF-vUSP-vCCF-DB_v11.1.zip";
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        runOnboardToCertificationFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test
    public void performanceTest() throws Exception {
        LOGGER.debug("Start test");
        Long actualTestRunTime = Utils.getActionDuration(() -> {
            try {
                onboardVNFTestSanityOneFile();
            } catch (final Exception e) {
                LOGGER.debug("An error has occurred during the performance test", e);
            }
        });
        long regularTestRunTime = 400L;
        double factor = 1.5;
        assertTrue("Expected test run time should be less from " + regularTestRunTime * factor + ", actual time is " + actualTestRunTime, regularTestRunTime * factor > actualTestRunTime);
    }

    @Test
    public void onboardVNFTestSanity() throws Exception {
        List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.VNF);
        String vnfFile = fileNamesFromFolder.get(0);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        runOnboardToCertificationFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVNFflow(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        runOnboardToCertificationFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPValidationsSanityFlow(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if (OnboardingUiUtils.getVspValidationCongiguration()) {
            goToVspScreen(true, vspName);

            //check links are available
            checkVspValidationLinksVisibility();

            VspValidationPage.navigateToVspValidationPageUsingNavbar();
            assertTrue(NEXT_BUTTON_IS_ENABLED_IT_SHOULD_HAVE_BEEN_DISABLED,
                    VspValidationPage.checkNextButtonDisabled());
            VspValidationResultsPage.navigateToVspValidationResultsPageUsingNavbar();
            GeneralUIUtils.ultimateWait();
            assertNotNull(GeneralUIUtils.findByText("No Test Performed"));
        } else {
            goToVspScreen(true, vspName);

            //check links are not available
            checkVspValidationLinksInvisibility();
        }
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "softwareInformationPnf")
    public void onboardPNFSoftwareInformationFlow(final String rootFolder, final String pnfFile,
                                                  final List<String> softwareVersionList) {
        setLog(pnfFile);
        final String resourceName = ElementFactory.addRandomSuffixToName(ElementFactory.getResourcePrefix());
        runOnboardPnfSoftwareVersion(resourceName, rootFolder, pnfFile, softwareVersionList);
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPValidationsConfigurationChangeCheck(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if (OnboardingUiUtils.getVspValidationCongiguration()) {
            goToVspScreen(true, vspName);
            //check links are available
            checkVspValidationLinksVisibility();

            //change config
            changeVspValidationConfig(false, vspName, OnboardingUiUtils.getVspValidationCongiguration());

            //check links are not available
            checkVspValidationLinksInvisibility();
        } else {
            goToVspScreen(true, vspName);
            //check links are not available
            checkVspValidationLinksInvisibility();

            changeVspValidationConfig(false, vspName, OnboardingUiUtils.getVspValidationCongiguration());

            //check links are available
            checkVspValidationLinksVisibility();
        }
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPCertificationQueryFlow(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if (!OnboardingUiUtils.getVspValidationCongiguration()) {
            //change config to true to test the feature
            changeVspValidationConfig(true, vspName, OnboardingUiUtils.getVspValidationCongiguration());
        } else {
            goToVspScreen(true, vspName);
        }
        VspValidationPage.navigateToVspValidationPageUsingNavbar();
        assertTrue(NEXT_BUTTON_IS_ENABLED_IT_SHOULD_HAVE_BEEN_DISABLED, VspValidationPage.checkNextButtonDisabled());

        if (VspValidationPage.checkCertificationQueryExists()) {
            VspValidationPage.clickCertificationQueryAll();
            GeneralUIUtils.ultimateWait();
            assertTrue(NEXT_BUTTON_IS_DISABLED_IT_SHOULD_HAVE_BEEN_ENABLED,
                    !VspValidationPage.checkNextButtonDisabled());
            VspValidationPage.clickOnNextButton();
            GeneralUIUtils.ultimateWait();
            VspValidationPage.clickOnSubmitButton();
            GeneralUIUtils.waitForLoader();
            assertTrue(RESULTS_ARE_NOT_AVAILABLE, VspValidationResultsPage.checkResultsExist());
        } else {
            assertNotNull(GeneralUIUtils.findByText("No Certifications Query are Available"));
        }

    }

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "Single_Vsp_Test_Csar")
    public void onapOnboardVSPComplianceCheckFlow(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if (!OnboardingUiUtils.getVspValidationCongiguration()) {
            //change config to true to test the feature
            changeVspValidationConfig(true, vspName, OnboardingUiUtils.getVspValidationCongiguration());
        } else {
            goToVspScreen(true, vspName);
        }

        VspValidationPage.navigateToVspValidationPageUsingNavbar();
        assertTrue(NEXT_BUTTON_IS_ENABLED_IT_SHOULD_HAVE_BEEN_ENABLED, VspValidationPage.checkNextButtonDisabled());
        if (VspValidationPage.checkComplianceCheckExists()) {
            VspValidationPage.clickComplianceChecksAll();
            GeneralUIUtils.ultimateWait();
            assertFalse(NEXT_BUTTON_IS_DISABLED_IT_SHOULD_HAVE_BEEN_ENABLED,
                    VspValidationPage.checkNextButtonDisabled());
            VspValidationPage.clickOnNextButton();
            GeneralUIUtils.ultimateWait();
            VspValidationPage.loadVSPFile(filePath, vnfFile);
            VspValidationPage.clickOnSubmitButton();
            GeneralUIUtils.ultimateWait();
            assertTrue(RESULTS_ARE_NOT_AVAILABLE, VspValidationResultsPage.checkResultsExist());
        } else {
            assertNotNull(GeneralUIUtils.findByText(NO_TESTS_ARE_AVAILABLE));
        }

    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPComplianceCheckOperations(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if (!OnboardingUiUtils.getVspValidationCongiguration()) {
            //change config to true to test the feature
            changeVspValidationConfig(true, vspName, OnboardingUiUtils.getVspValidationCongiguration());
        } else {
            goToVspScreen(true, vspName);
        }

        VspValidationPage.navigateToVspValidationPageUsingNavbar();
        assertTrue(NEXT_BUTTON_IS_ENABLED_IT_SHOULD_HAVE_BEEN_ENABLED, VspValidationPage.checkNextButtonDisabled());
        if (VspValidationPage.checkComplianceCheckExists()) {
            assertFalse(THE_TESTS_ARE_ALREADY_SELECTED_THE_LIST_SHOULD_INITIALLY_BE_EMPTY,
                    VspValidationPage.checkSelectedComplianceCheckExists());
            VspValidationPage.clickComplianceChecksAll();
            GeneralUIUtils.ultimateWait();
            assertTrue(THE_SELECTED_TESTS_ARE_NOT_POPULATED_IN_THE_LIST,
                    VspValidationPage.checkSelectedComplianceCheckExists());
            VspValidationPage.clickComplianceChecksAll();
            GeneralUIUtils.ultimateWait();
            assertFalse(THE_SELECTED_TESTS_ARE_NOT_DELETED_FROM_THE_LIST,
                    VspValidationPage.checkSelectedComplianceCheckExists());
        } else {
            assertNotNull(GeneralUIUtils.findByText(NO_TESTS_ARE_AVAILABLE));
        }

    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPCertificationQueryOperations(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if (!OnboardingUiUtils.getVspValidationCongiguration()) {
            //change config to true to test the feature
            changeVspValidationConfig(true, vspName, OnboardingUiUtils.getVspValidationCongiguration());
        } else {
            goToVspScreen(true, vspName);
        }

        VspValidationPage.navigateToVspValidationPageUsingNavbar();
        assertTrue(NEXT_BUTTON_IS_ENABLED_IT_SHOULD_HAVE_BEEN_ENABLED, VspValidationPage.checkNextButtonDisabled());
        if (VspValidationPage.checkCertificationQueryExists()) {
            assertFalse(THE_TESTS_ARE_ALREADY_SELECTED_THE_LIST_SHOULD_INITIALLY_BE_EMPTY,
                VspValidationPage.checkSelectedCertificationQueryExists());
            VspValidationPage.clickCertificationQueryAll();
            GeneralUIUtils.ultimateWait();
            assertTrue(THE_SELECTED_TESTS_ARE_NOT_POPULATED_IN_THE_LIST, VspValidationPage.checkSelectedCertificationQueryExists());
            VspValidationPage.clickCertificationQueryAll();
            GeneralUIUtils.ultimateWait();
            assertFalse(THE_SELECTED_TESTS_ARE_NOT_DELETED_FROM_THE_LIST,
                VspValidationPage.checkSelectedCertificationQueryExists());
        } else {
            assertNotNull(GeneralUIUtils.findByText(NO_TESTS_ARE_AVAILABLE));
        }

    }

    private void checkVspValidationLinksVisibility() {
        //check links are available
        assertTrue("Validation Link is not available", GeneralUIUtils.isElementVisibleByTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR.getValue()));
        assertTrue("Validation Results Link is not available", GeneralUIUtils.isElementVisibleByTestId(DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_NAVBAR.getValue()));
    }

    private void checkVspValidationLinksInvisibility() {
        //check links not available
        assertTrue("Validation Link is still available", GeneralUIUtils.isElementInvisibleByTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR.getValue()));
        assertTrue("Validation Results Link is still available", GeneralUIUtils.isElementInvisibleByTestId(DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_NAVBAR.getValue()));
    }

    private void changeVspValidationConfig(boolean isCurrentScreenCatalogPage, String vspName, boolean vspConfig) throws Exception {
        //change config
        OnboardingUiUtils.putVspValidationCongiguration(!vspConfig);
        assertTrue(String.format("Failed to set Congiguration to %s", !vspConfig), OnboardingUiUtils.getVspValidationCongiguration() != vspConfig);

        if (!isCurrentScreenCatalogPage) {
            GeneralUIUtils.refreshWebpage();
            GeneralUIUtils.ultimateWait();
        }

        goToVspScreen(isCurrentScreenCatalogPage, vspName);

        //revert the config
        OnboardingUiUtils.putVspValidationCongiguration(vspConfig);
        assertEquals(String.format("Failed to revert Configuration to %s", vspConfig), vspConfig,
            OnboardingUiUtils.getVspValidationCongiguration());
    }

    private void goToVspScreen(boolean isCurrentScreenCatalogPage, String vspName) {
        if (isCurrentScreenCatalogPage) {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtons.ONBOARD_BUTTON.getValue());
        }
        GeneralUIUtils.clickOnElementByText(vspName);
        GeneralUIUtils.ultimateWait();
    }

    private String createNewVSP(String filePath, String vnfFile) throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        return OnboardingUiUtils.createVSP(resourceReqDetails, vnfFile, filePath, getUser()).getName();
    }

    public void runOnboardPnfSoftwareVersion(final String resourceName, final String rootFolder,
                                             final String pnfFile, final List<String> softwareVersionList) {
        final ExtentTest extendTest = getExtendTest();

        final String swVersionsToString = String.join(", ", softwareVersionList);

        extendTest.log(Status.INFO,
            String.format("Creating VSP '%s' by onboarding package '%s' with software version '%s'",
                resourceName, pnfFile, swVersionsToString)
        );
        final WebDriver webDriver = DriverFactory.getDriver();
        final CreateVspFlow createVspFlow = new CreateVspFlow(webDriver, resourceName, pnfFile, rootFolder);
        createVspFlow.run(new TopNavComponent(webDriver));

        final ImportVspFlow importVspFlow = new ImportVspFlow(webDriver, resourceName);
        final ResourceCreatePage resourceCreatePage =
            (ResourceCreatePage) importVspFlow.run()
                .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));

        final CreateResourceFlow createResourceFlow = new CreateResourceFlow(webDriver, resourceName);
        createResourceFlow.run(resourceCreatePage);

        final CheckSoftwareVersionPropertyFlow checkSoftwareVersionPropertyFlow =
            new CheckSoftwareVersionPropertyFlow(webDriver, softwareVersionList);
        checkSoftwareVersionPropertyFlow.run();

        extendTest.log(Status.INFO,
            String.format("Successfully onboarded the package '%s' with software version '%s'",
                pnfFile, swVersionsToString)
        );
    }

    private void runOnboardToCertificationFlow(ResourceReqDetails resourceReqDetails, ServiceReqDetails serviceMetadata, String filePath, String vnfFile) throws Exception {
        getExtendTest().log(Status.INFO, "Going to create resource with category: " + resourceReqDetails.getCategories().get(0).getName()
                + " subCategory: " + resourceReqDetails.getCategories().get(0).getSubcategories().get(0).getName()
                + " and service category: " + serviceMetadata.getCategory());
        final String vspName = onboardAndCertify(resourceReqDetails, filePath, vnfFile);

        ServiceUIUtils.createService(serviceMetadata);

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        Service containerObject = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, serviceMetadata.getName(), "0.1");
        Resource instanceObject = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vspName, "1.0");
        ComponentInstance componentInstanceDetails = AtomicOperationUtils.addComponentInstanceToComponentContainer(instanceObject, containerObject, UserRoleEnum.DESIGNER, true).left().value();

        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.LifeCyleChangeButtons.CERTIFY.getValue());

        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.APPROVE_MESSAGE.getValue())
                .sendKeys("service " + serviceMetadata.getName() + " certified successfully");
        clickOKButton();
        getExtendTest().log(Status.INFO, String.format("Successfully onboarded the package '%s'", vnfFile));
    }

    private String onboardAndCertify(ResourceReqDetails resourceReqDetails, String filePath, String vnfFile) throws Exception {
        VendorSoftwareProductObject onboardAndValidate = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFile, getUser());
        String vspName = onboardAndValidate.getName();

        DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
        ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFile, "The topology template for " + vnfFile + " is as follows : ");

        DeploymentArtifactPage.clickCertifyButton(vspName);
        return vspName;
    }


    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "VNF_List")
    public void onboardVNFTest(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getRandomCategoryResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getRandomCategoryService();
        runOnboardToCertificationFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "VNF_List")
    public void onboardVNFShotFlow(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        onboardAndCertify(resourceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
    public void onboardRandomVNFsTest(String filePath, String vnfFile) throws Exception {
        setLog(vnfFile);
        LOGGER.debug("Vnf File name is: {}", vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getRandomCategoryResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getRandomCategoryService();
        runOnboardToCertificationFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }


    @Test
    public void onboardUpdateVNFTest() throws Exception {
        List<String> fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filePath);
        String vnfFile = fileNamesFromFolder.get(0);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        VendorSoftwareProductObject vsp = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFile, getUser());
        String vspName = vsp.getName();
        ResourceGeneralPage.clickCertifyButton(vspName);

        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CompositionPage.searchForElement(vspName);
        CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
        assertNotNull(vfElement);
        ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());

        if (!HomePage.navigateToHomePage()) {
            fail("Could not go to the home page");
        }

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
        GovernorOperationPage.approveService(serviceMetadata.getName());


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
        final String[] list = new File(pathFile).list();
        assertNotNull("Did not find vMMSCs", list);
        assertFalse("Did not find vMMSCs", list.length == 0);
        List<String> vmmscList = Arrays.stream(list).filter(e -> e.contains("vmmsc") && e.endsWith(".zip"))
            .collect(Collectors.toList());
        assertFalse("Did not find vMMSCs", vmmscList.isEmpty());

        Map<String, String> vspNames = new HashMap<>();
        for (String vnfFile : vmmscList) {
            String msg = String.format("Going to onboard the VNF %s", vnfFile);
            getExtendTest().log(Status.INFO, msg);
            LOGGER.info(msg);

            VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
            ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
            VendorSoftwareProductObject createVendorSoftwareProduct = VendorSoftwareProductRestUtils
                .createVendorSoftwareProduct(resourceReqDetails, vnfFile, pathFile, getUser(), vendorLicenseModel);

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

        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);
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
        GovernorOperationPage.approveService(serviceMetadata.getName());

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
