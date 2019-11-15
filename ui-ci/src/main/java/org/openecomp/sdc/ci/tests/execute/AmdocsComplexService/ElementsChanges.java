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

package org.openecomp.sdc.ci.tests.execute.AmdocsComplexService;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;


public class
ElementsChanges extends SetupCDTest {

    private static final int NUMBER_OF_LINKS = 3;
    protected static String filePath = FileHandling.getFilePath("ComplexService");
    private static String fullCompositionFile = "fullComposition.zip";
    private static String test = "test.zip";
    private static String fullCompositionFile2 = "test.zip";
    private static String HSSFile = "HSS.zip";
    private static String VMMEFile = "VMME.zip";
    private static String WithPort = "withPort.zip";
    private static String withoutPort = "withoutPort.zip";
    private static String makeDistributionValue;

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
        makeDistributionValue = makeDistributionReadValue;
    }

    //------------------------------------------Tests-----------------------------------------------------

    // Test#1 Jira issue 6324
    @Test
    public void DeleteComponent() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata = PathUtilities.createService(getUser());
        List<CanvasElement> VFs = PathUtilities.linkVFs(vspName, NUMBER_OF_LINKS);
        String pathName = PathUtilities.createPathWithoutLink("DeleteComponent", vspName);
        PathUtilities.deleteComponents(VFs);
        PathValidations.validatePathListIsEmpty();
        certifyServiceAsTester(serviceMetadata);

    }

    // Test#2 Jira issue 6325
    @Test
    public void DeleteServiceComponent() throws Exception {
        ServiceReqDetails serviceMetadata = getToComplexService(filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails complexService = PathUtilities.createService(getUser());
        List<CanvasElement> services = createComplexPath(serviceMetadata.getName());
        PathUtilities.deleteComponents(services);
        PathValidations.validatePathListIsEmpty();
        certifyServiceAsTester(complexService);
    }

    // Test#3 Jira issue 6364
    @Test
    public void ManualUpdateVF() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata = PathUtilities.createService(getUser());
        PathUtilities.createPath("ManualUpdateVF", vspName);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.HOME_FROM_COMPOSITION.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtons.ONBOARD_BUTTON.getValue());
        ///   GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.ONBOARD_CATALOG.getValue());
        ResourceUIUtils.clickOnElementByText(vspName, null);
        ResourceUIUtils.clickOnElementByText("Create New Version", null);
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ComplexServiceAmdocs.NEW_VSP_VERSION_DESCRIPTION.getValue()).sendKeys("new vsap version for service path");
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.SUBMIT_NEW_VSP_VERSION_DESCRIPTION.getValue());

        // VendorSoftwareProductObject v = new VendorSoftwareProductObject();
        //VendorSoftwareProductRestUtils.uploadHeatPackage(filePath,fullCompositionFile,v,getUser());
//upload new heat +commit submit
        //go to home
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtons.ONBOARD_BUTTON.getValue());

        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // CanvasManager.getCanvasManager().clickOnCanvaElement(vf);
        //update version
        //validate path still exist
        certifyServiceAsTester(serviceMetadata);
    }

    @Test
    public void UpdateComponent() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, VMMEFile);

    }

    @Test
    public void UpdateComponentWithouForwarder() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vsp_v2_description = "change description for version 2";
        VendorSoftwareProductObject vendorSoftwareProductObject = getToPathServiceVersions(resourceReqDetails, filePath, WithPort, vsp_v2_description, withoutPort, getUser());

        // create service
        ServiceReqDetails service = PathUtilities.createService(getUser());

        // create path
        String vspName = vendorSoftwareProductObject.getName();
        String pathName = "path1";
        List<CanvasElement> VFs = PathUtilities.linkVFs(vspName, 2);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        for (CanvasElement element : VFs) {
            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
        }
        PathUtilities.openCreatePath();
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        PathValidations.extendPath(NUMBER_OF_LINKS);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());

        /////////////////
        //  test case  //
        /////////////////

        // validate version change has no effect
        for (CanvasElement element : VFs) {
            RestResponse ServiceForwardingPathsResponse = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            // change to version with different capabiliteis
            CompositionPage.changeComponentVersion(canvasManager, element, "3.0", false);
            // click on warning message
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths changed
            RestResponse ServiceForwardingPathsResponse2 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertNotSame(
                    "response message failed expectation, expectation is to change",
                    ServiceForwardingPathsResponse.getResponse(),
                    ServiceForwardingPathsResponse2.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();

            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths hasn't changed
            RestResponse ServiceForwardingPathsResponse3 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertEquals(
                    "response message failed expectation, expectation is not to change",
                    ServiceForwardingPathsResponse2.getResponse(),
                    ServiceForwardingPathsResponse3.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();
        }

        ///////////////////////
        //  post requisites  //
        ///////////////////////

        // finish flow
        certifyServiceAsTester(service);
    }

    //TODO run and debug
// Test#3 Jira issue - 6230
    @Test
    public void ValidatePathServiceVersions() throws Exception {

        //////////////////////
        //  pre requisites  //
        //////////////////////

        // start flow
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vsp_v2_description = "change description for version 2";
        VendorSoftwareProductObject vendorSoftwareProductObject = getToPathServiceVersions(resourceReqDetails, filePath, HSSFile, vsp_v2_description, fullCompositionFile, getUser());

        // create service
        ServiceReqDetails service = PathUtilities.createService(getUser());

        // create path
        String vspName = vendorSoftwareProductObject.getName();
        String pathName = "path1";
        List<CanvasElement> VFs = PathUtilities.linkVFs(vspName, 2);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        for (CanvasElement element : VFs) {
            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
        }
        PathUtilities.openCreatePath();
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        PathValidations.extendPath(NUMBER_OF_LINKS);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());

        /////////////////
        //  test case  //
        /////////////////

        // validate version change has no effect
        for (CanvasElement element : VFs) {
            RestResponse ServiceForwardingPathsResponse = PathUtilities.getServiceForwardingPathsAPI(service.getName());

            CompositionPage.changeComponentVersion(canvasManager, element, "2.0", false);
            // validate paths hasn't changed
            RestResponse ServiceForwardingPathsResponse2 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertEquals(
                    "response message failed expectation, expectation is not to change",
                    ServiceForwardingPathsResponse.getResponse(),
                    ServiceForwardingPathsResponse2.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();

            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
            // validate paths hasn't changed
            RestResponse ServiceForwardingPathsResponse3 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertEquals(
                    "response message failed expectation, expectation is not to change",
                    ServiceForwardingPathsResponse.getResponse(),
                    ServiceForwardingPathsResponse3.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();
        }

        ///////////////////////
        //  post requisites  //
        ///////////////////////

        // finish flow
        certifyServiceAsTester(service);
    }

    //TODO run and debug
// Test#3 Jira issue - 6189
    @Test
    public void ValidatePathServiceVersionsDifferentCapabilities() throws Exception {

        //////////////////////
        //  pre requisites  //
        //////////////////////

        // start flow
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vsp_v2_description = "change description for version 2";
        VendorSoftwareProductObject vendorSoftwareProductObject = getToPathServiceVersions(resourceReqDetails, filePath, HSSFile, vsp_v2_description, fullCompositionFile, getUser());

        // create service
        ServiceReqDetails service = PathUtilities.createService(getUser());

        // create path
        String vspName = vendorSoftwareProductObject.getName();
        String pathName = "path1";
        List<CanvasElement> VFs = PathUtilities.linkVFs(vspName, 2);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        for (CanvasElement element : VFs) {
            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
        }
        PathUtilities.openCreatePath();
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        PathValidations.extendPath(NUMBER_OF_LINKS);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());

        /////////////////
        //  test case  //
        /////////////////

        // validate version change has no effect
        for (CanvasElement element : VFs) {
            RestResponse ServiceForwardingPathsResponse = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            // change to version with different capabiliteis
            CompositionPage.changeComponentVersion(canvasManager, element, "3.0", false);
            // click on warning message
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths changed
            RestResponse ServiceForwardingPathsResponse2 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertNotSame(
                    "response message failed expectation, expectation is to change",
                    ServiceForwardingPathsResponse.getResponse(),
                    ServiceForwardingPathsResponse2.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();

            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths hasn't changed
            RestResponse ServiceForwardingPathsResponse3 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertEquals(
                    "response message failed expectation, expectation is not to change",
                    ServiceForwardingPathsResponse2.getResponse(),
                    ServiceForwardingPathsResponse3.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();
        }

        ///////////////////////
        //  post requisites  //
        ///////////////////////

        // finish flow
        certifyServiceAsTester(service);
    }

    //TODO run and debug
// Test#3 Jira issue - 6231
    @Test
    public void ValidatePathServiceVersionsnegative() throws Exception {

        //////////////////////
        //  pre requisites  //
        //////////////////////

        // start flow
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vsp_v2_description = "change description for version 2";
        VendorSoftwareProductObject vendorSoftwareProductObject = getToPathServiceVersions(resourceReqDetails, filePath, HSSFile, vsp_v2_description, fullCompositionFile, getUser());

        // create service
        ServiceReqDetails service = PathUtilities.createService(getUser());

        // create path
        String vspName = vendorSoftwareProductObject.getName();
        String pathName = "path1";
        List<CanvasElement> VFs = PathUtilities.linkVFs(vspName, 2);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        for (CanvasElement element : VFs) {
            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
        }
        PathUtilities.openCreatePath();
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        PathValidations.extendPath(NUMBER_OF_LINKS);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());

        /////////////////
        //  test case  //
        /////////////////

        // validate version change has no effect
        for (CanvasElement element : VFs) {
            RestResponse ServiceForwardingPathsResponse = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            // change to version with different capabiliteis
            CompositionPage.changeComponentVersion(canvasManager, element, "3.0", false);
            // click on warning message to cancel
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ModalItems.CANCEL.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths changed
            RestResponse ServiceForwardingPathsResponse2 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertEquals(
                    "response message failed expectation, expectation is not to change",
                    ServiceForwardingPathsResponse.getResponse(),
                    ServiceForwardingPathsResponse2.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();

            CompositionPage.changeComponentVersion(canvasManager, element, "3.0", true);
            // click on warning message
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths hasn't changed
            RestResponse ServiceForwardingPathsResponse3 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertNotSame(
                    "response message failed expectation, expectation is to change",
                    ServiceForwardingPathsResponse2.getResponse(),
                    ServiceForwardingPathsResponse3.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();
        }

        ///////////////////////
        //  post requisites  //
        ///////////////////////

        // finish flow
        certifyServiceAsTester(service);
    }

    //TODO finish
// Test#3 Jira issue - 6189
    @Test
    public void ValidatePathServiceVersionsDifferentCapabilitiesInComplexService() throws Exception {

        //////////////////////
        //  pre requisites  //
        //////////////////////

        // start flow
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vsp_v2_description = "change description for version 2";
        VendorSoftwareProductObject vendorSoftwareProductObject = getToPathServiceVersions(resourceReqDetails, filePath, HSSFile, vsp_v2_description, fullCompositionFile, getUser());

        // create service
        ServiceReqDetails service = PathUtilities.createService(getUser());

        // create path
        String vspName = vendorSoftwareProductObject.getName();
        String pathName = "path1";
        List<CanvasElement> VFs = PathUtilities.linkVFs(vspName, 2);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        for (CanvasElement element : VFs) {
            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
        }
        PathUtilities.openCreatePath();
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        PathValidations.extendPath(NUMBER_OF_LINKS);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());

        // certify Service
        certifyServiceAsTester(service);

        reloginWithNewRole(UserRoleEnum.DESIGNER);

        // create new version
        GeneralUIUtils.findComponentAndClick(service.getName());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        canvasManager = CanvasManager.getCanvasManager();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.LifeCyleChangeButtons.CHECKOUT.getValue());

        /////////////////
        //  test case  //
        /////////////////

        // validate version change has no effect
        for (CanvasElement element : VFs) {
            RestResponse ServiceForwardingPathsResponse = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            // change to version with different capabilities
            CompositionPage.changeComponentVersion(canvasManager, element, "3.0", false);
            // click on warning message
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths changed
            RestResponse ServiceForwardingPathsResponse2 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertNotSame(
                    "response message failed expectation, expectation is to change",
                    ServiceForwardingPathsResponse.getResponse(),
                    ServiceForwardingPathsResponse2.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();

            CompositionPage.changeComponentVersion(canvasManager, element, "1.0", false);
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']").get(0).click();
            GeneralUIUtils.ultimateWait();
            // validate paths hasn't changed
            RestResponse ServiceForwardingPathsResponse3 = PathUtilities.getServiceForwardingPathsAPI(service.getName());
            assertEquals(
                    "response message failed expectation, expectation is not to change",
                    ServiceForwardingPathsResponse2.getResponse(),
                    ServiceForwardingPathsResponse3.getResponse());
            // validate there are no error messages
            PathValidations.ValidateThereIsNoErrorMessage();
        }

        ///////////////////////
        //  post requisites  //
        ///////////////////////

        // finish flow
        certifyServiceAsTester(service);
    }

    @Test
    public void ComplexServiceSanity() throws Exception {
        // create & certify  2 VFs
        List<String> vnfFiles = new ArrayList<>();
        vnfFiles.add(HSSFile);
        vnfFiles.add(fullCompositionFile);
        List<String> VFNames = onboardAndCertifyMultipleVFs(filePath, vnfFiles);
        reloginWithNewRole(UserRoleEnum.DESIGNER);

        // create service 1
        ServiceReqDetails serviceMetadata1 = PathUtilities.createService(getUser());

        // create path 1
        String path1 = "newPath";
        PathUtilities.createPath(path1, VFNames.get(0));
        PathUtilities.submitForTesting();
        GeneralUIUtils.ultimateWait();

        // create service 2
        ServiceReqDetails serviceMetadata2 = PathUtilities.createService(getUser());

        // create path 2
        String path2 = "newPath2";
        PathUtilities.createPath(path2, VFNames.get(1));
        PathUtilities.submitForTesting();

        // tester
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        TesterOperationPage.certifyComponent(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        TesterOperationPage.certifyComponent(serviceMetadata2.getName());

        // governor
        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        GovernorOperationPage.approveService(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        GovernorOperationPage.approveService(serviceMetadata2.getName());

        // create service for complex service
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata3 = PathUtilities.createService(getUser());

        // validate complex's path
        String[] services = {serviceMetadata1.getName(), serviceMetadata2.getName(), serviceMetadata3.getName()};
        PathValidations.validateComplexExtendedPath(services);

        // validate certification
        certifyServiceAsTester(serviceMetadata3);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                               flow methods                                         //
    ////////////////////////////////////////////////////////////////////////////////////////

    // workflow leading to path
    public String onboardAndCertify(ResourceReqDetails resourceReqDetails, String filePath, String vnfFile) throws Exception {
        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFile, getUser());
        String vspName = vendorSoftwareProductObject.getName();

        DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
        ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFile, "The topology template for " + vnfFile + " is as follows : ");

        DeploymentArtifactPage.clickSubmitForTestingButton(vspName);
        SetupCDTest.getExtendTest().log(Status.INFO, "relogin as TESTER");
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(vspName);
        TesterOperationPage.certifyComponent(vspName);
        return vspName;
    }

    public List<String> onboardAndCertifyMultipleVFs(String filePath, List<String> vnfFiles) throws Exception {
        List<String> VFNames = new ArrayList<>();
        for (int i = 0; i < vnfFiles.size(); i++) {
            ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
            VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFiles.get(i), getUser());
            VFNames.add(i, vendorSoftwareProductObject.getName());
            DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
            ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFiles.get(i), "The topology template for " + vnfFiles.get(i) + " is as follows : ");
            DeploymentArtifactPage.clickSubmitForTestingButton(VFNames.get(i));
        }
        SetupCDTest.getExtendTest().log(Status.INFO, "relogin as TESTER");
        reloginWithNewRole(UserRoleEnum.TESTER);
        for (String VFName : VFNames) {
            GeneralUIUtils.findComponentAndClick(VFName);
            TesterOperationPage.certifyComponent(VFName);
        }
        return VFNames;
    }

    public static List<CanvasElement> createComplexPath(String service) throws Exception {
        List<CanvasElement> services = PathUtilities.linkServices(service, service, NUMBER_OF_LINKS);
        PathUtilities.createPathWithoutLink("name1", service);
        PathUtilities.createPathWithoutLink("name2", service);
        return services;
    }

    public ServiceReqDetails getToComplexService(String filepath, String vnfFile) throws Exception {
        // create & certify  VF
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filepath, vnfFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata1 = PathUtilities.createService(getUser());

        // create path
        PathUtilities.createPath("newPath", vspName);
        PathUtilities.submitForTesting();
        GeneralUIUtils.ultimateWait();

        //tester
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        TesterOperationPage.certifyComponent(serviceMetadata1.getName());

        //governor
        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        GovernorOperationPage.approveService(serviceMetadata1.getName());
        return serviceMetadata1;
    }

    public VendorSoftwareProductObject getToPathServiceVersions(ResourceReqDetails resourceReqDetails, String filepath, String vnfFileV1, String descriptionV2, String vnfFileV3, User user) throws Exception {

        ////////////////////////////////////
        //             VF v1              //
        ////////////////////////////////////

        VendorSoftwareProductObject vendorSoftwareProduct = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFileV1, getUser());
        String vspName = vendorSoftwareProduct.getName();

        DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
        ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFileV1, "The topology template for " + vnfFileV1 + " is as follows : ");
        DeploymentArtifactPage.clickSubmitForTestingButton(vspName);
        SetupCDTest.getExtendTest().log(Status.INFO, "relogin as TESTER");
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(vspName);
        TesterOperationPage.certifyComponent(vspName);
        reloginWithNewRole(UserRoleEnum.DESIGNER);

        ////////////////////////////////////
        //             VF v2              //
        ////////////////////////////////////

        // vsp create new version
        Pair<RestResponse, OnboardItemObject> newItemVersion = PathUtilities.createNewItemVersion(
                vendorSoftwareProduct.getVspId(),
                vendorSoftwareProduct.getComponentId(),
                "version with different description", user);
        assertEquals("did not succeed to create new item version", HttpStatus.SC_OK, newItemVersion.left.getErrorCode().intValue());
        vendorSoftwareProduct.setVersion(newItemVersion.right.getItemId());
        vendorSoftwareProduct.setComponentId(newItemVersion.right.getItemId());

        // update vsp description
        vendorSoftwareProduct.setDescription(descriptionV2);
        RestResponse restResponse = PathUtilities.updateVendorSoftwareProduct(vendorSoftwareProduct, user);
        assertEquals("did not succeed to update vsp", HttpStatus.SC_OK, restResponse.getErrorCode().intValue());

        // commit & submit vsp
        VendorSoftwareProductRestUtils.prepareVspForUse(user, vendorSoftwareProduct, false);

        // update VF
        HomePage.showVspRepository();
        PathUtilities.updateVF(vspName, vendorSoftwareProduct);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.UPDATE_VF.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.SUBMIT_FOR_TESTING_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();

        // certify VF
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(vspName);
        TesterOperationPage.certifyComponent(vspName);
        reloginWithNewRole(UserRoleEnum.DESIGNER);

        ////////////////////////////////////
        //             VF v3              //
        ////////////////////////////////////

        // create new version
        newItemVersion = PathUtilities.createNewItemVersion(
                vendorSoftwareProduct.getVspId(),
                vendorSoftwareProduct.getVersion(),
                "version with different heat", user);
        assertEquals("did not succeed to create new item version", HttpStatus.SC_OK, newItemVersion.left.getErrorCode().intValue());
        vendorSoftwareProduct.setVersion(newItemVersion.right.getItemId());
        vendorSoftwareProduct.setComponentId(newItemVersion.right.getItemId());

        // upload new heat
        RestResponse uploadHeatPackage = VendorSoftwareProductRestUtils.uploadHeatPackage(filepath, vnfFileV3, vendorSoftwareProduct, user);
        assertEquals("did not succeed to upload HEAT package", HttpStatus.SC_OK, uploadHeatPackage.getErrorCode().intValue());

        RestResponse validateUpload = VendorSoftwareProductRestUtils.validateUpload(vendorSoftwareProduct, user);
        assertEquals("did not succeed to validate upload process, reason: " + validateUpload.getResponse(), HttpStatus.SC_OK, validateUpload.getErrorCode().intValue());

        // commit & submit vsp
        VendorSoftwareProductRestUtils.prepareVspForUse(user, vendorSoftwareProduct, false);

        // update VF
        HomePage.showVspRepository();
        PathUtilities.updateVF(vspName, vendorSoftwareProduct);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.UPDATE_VSP.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.SUBMIT_FOR_TESTING_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();

        // certify VF
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(vspName);
        TesterOperationPage.certifyComponent(vspName);
        reloginWithNewRole(UserRoleEnum.DESIGNER);

        return vendorSoftwareProduct;
    }

    public void certifyServiceAsTester(ServiceReqDetails serviceMetaData) throws Exception {
        PathUtilities.submitForTesting();
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetaData.getName());
        TesterOperationPage.certifyComponent(serviceMetaData.getName());
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }
}
