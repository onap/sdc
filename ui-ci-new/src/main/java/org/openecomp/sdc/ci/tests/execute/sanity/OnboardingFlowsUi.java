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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.aventstack.extentreports.Status;
import java.util.List;
import org.openecomp.sdc.ci.tests.data.providers.OnboardingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.XnfTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ArtifactsCorrelationManager;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.HomePage.PageElement;
import org.openecomp.sdc.ci.tests.pages.OpsOperationPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.pages.VspValidationPage;
import org.openecomp.sdc.ci.tests.pages.VspValidationResultsPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.validation.ServiceValidation;
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

    protected static String filePath = FileHandling.getVnfRepositoryPath();
    private Boolean makeDistributionValue;

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") final String makeDistributionReadValue) {
        LOGGER.debug("makeDistribution parameter is '{}'", makeDistributionReadValue);
        makeDistributionValue = Boolean.valueOf(makeDistributionReadValue);
    }

    @Test
    public void onboardVNFTestSanity() throws Exception {
        List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.VNF);
        String vnfFile = fileNamesFromFolder.get(0);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        runOnboardToDistributionFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVNFflow(final String filePath, final String vnfFile) throws Exception {
        setLog(vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        runOnboardToDistributionFlow(resourceReqDetails, serviceReqDetails, filePath, vnfFile);
    }

    private void runOnboardToDistributionFlow(final ResourceReqDetails resourceReqDetails,
                                              final ServiceReqDetails serviceMetadata,
                                              final String filePath, final String vnfFile) throws Exception {
        getExtendTest().log(Status.INFO,
            "Going to create resource with category: " + resourceReqDetails.getCategories().get(0).getName()
                + " subCategory: " + resourceReqDetails.getCategories().get(0).getSubcategories().get(0).getName()
                + " and service category: " + serviceMetadata.getCategory());
        final String vspName = onboardAndCertify(resourceReqDetails, filePath, vnfFile);

        ServiceUIUtils.createService(serviceMetadata);

        GeneralPageElements.getLeftMenu().moveToCompositionScreen();
        CompositionPage.searchForElement(vspName);
        final CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
        final CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
        ArtifactsCorrelationManager.addVNFtoServiceArtifactCorrelation(serviceMetadata.getName(), vspName);

        assertNotNull(vfElement);
        ServiceValidation.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());
        ExtentTestActions
            .addScreenshot(Status.INFO, "ServiceComposition_" + vnfFile, "The service topology is as follows: ");

        GeneralPageElements.clickSubmitForTestingButton(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        TesterOperationPage.certifyComponent(serviceMetadata.getName());

        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        HomePage.waitForElement(PageElement.COMPONENT_PANEL);
        HomePage.findComponentAndClick(serviceMetadata.getName());
        GovernorOperationPage.approveService(serviceMetadata.getName());

        runDistributionFlow(serviceMetadata);

        getExtendTest().log(Status.INFO, String.format("Successfully onboarded the package '%s'", vnfFile));
    }

    private void runDistributionFlow(final ServiceReqDetails serviceMetadata) throws Exception {
        if (makeDistributionValue) {
            reloginWithNewRole(UserRoleEnum.OPS);
            GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
            OpsOperationPage.distributeService();
            OpsOperationPage.displayMonitor();

            final List<WebElement> rowsFromMonitorTable = OpsOperationPage.getRowsFromMonitorTable();
            AssertJUnit.assertEquals(1, rowsFromMonitorTable.size());

            OpsOperationPage.waitUntilArtifactsDistributed(0);
        }
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Single_VNF")
    public void onapOnboardVSPValidationsSanityFlow(final String filePath, final String vnfFile) throws Exception {
        setLog(vnfFile);
        String vspName = createNewVSP(filePath, vnfFile);
        if (OnboardingUiUtils.getVspValidationCongiguration()) {
            goToVspScreen(vspName);

            //check links are available
            checkVspValidationLinksVisibility();

            VspValidationPage.navigateToVspValidationPageUsingNavbar();
            assertTrue("Next Button is enabled, it should have been disabled",
                VspValidationPage.checkNextButtonDisabled());
            VspValidationResultsPage.navigateToVspValidationResultsPageUsingNavbar();
            GeneralUIUtils.ultimateWait();
            assertNotNull(GeneralUIUtils.findByText("No Validation Checks Performed"));
        } else {
            goToVspScreen(vspName);

            //check links are not available
            checkVspValidationLinksInvisibility();
        }
    }

    private void checkVspValidationLinksVisibility() {
        //check links are available
        assertTrue("Validation Link is not available", GeneralUIUtils
            .isElementVisibleByTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR.getValue()));
        assertTrue("Validation Results Link is not available", GeneralUIUtils.isElementVisibleByTestId(
            DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_NAVBAR.getValue()));
    }

    private void checkVspValidationLinksInvisibility() {
        //check links not available
        assertTrue("Validation Link is still available", GeneralUIUtils
            .isElementInvisibleByTestId(DataTestIdEnum.VspValidationPage.VSP_VALIDATION_PAGE_NAVBAR.getValue()));
        assertTrue("Validation Results Link is still available", GeneralUIUtils.isElementInvisibleByTestId(
            DataTestIdEnum.VspValidationResultsPage.VSP_VALIDATION_RESULTS_PAGE_NAVBAR.getValue()));
    }

    private void goToVspScreen(final String vspName) {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtons.ONBOARD_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByText(vspName);
        GeneralUIUtils.ultimateWait();
    }

    private String createNewVSP(final String filePath, final String vnfFile) throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        return OnboardingUiUtils.createVSP(resourceReqDetails, vnfFile, filePath, getUser()).getName();
    }

    private String onboardAndCertify(final ResourceReqDetails resourceReqDetails, final String filePath,
                                     final String vnfFile)
        throws Exception {
        VendorSoftwareProductObject onboardAndValidate = OnboardingUiUtils
            .onboardAndValidate(resourceReqDetails, filePath, vnfFile, getUser());
        String vspName = onboardAndValidate.getName();

        DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
        ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFile,
            "The topology template for " + vnfFile + " is as follows : ");

        GeneralPageElements.clickCertifyButton(vspName);
        return vspName;
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
