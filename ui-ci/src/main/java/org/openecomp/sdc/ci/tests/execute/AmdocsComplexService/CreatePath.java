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
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.CircleSize;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class CreatePath extends SetupCDTest {

    protected static String filePath = FileHandling.getFilePath("ComplexService");
    private static String fullCompositionFile = "fullComposition.zip";
    private static String fullCompositionFile2 = "fullCompositionNew.zip";
    private static String HSSFile = "HSS.zip";
    private static String VMMEFile = "VMME.zip";
    private static String makeDistributionValue;

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
        makeDistributionValue = makeDistributionReadValue;
    }

    //------------------------------------------Tests-----------------------------------------------------


    // Test#1 Jira issue 5610
    @Test
    public void AssertPathButtons() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        getToPathFlow(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        PathUtilities.createService(getUser());
        PathUtilities.openCreatePath();
        PathValidations.AssertCreatePath();
        PathValidations.AssertExtendPath();
    }

    // Test#2 Jira issue 5441
    @Test
    public void CreatePathTestSanity() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        Pair<String, ServiceReqDetails> pair = getToPathFlow(resourceReqDetails, filePath, fullCompositionFile);
        String vspName = pair.left;
        String serviceName = pair.right.getName();
        String pathName = "Test1";
        PathUtilities.createPath(pathName, vspName);
        PathValidations.validateServicePath(serviceName, pathName);
    }

    // Test#3 Jira issue 5611
    @Test
    public void CreatePathCheckIO() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        String serviceName = PathUtilities.createService(getUser()).getName();
        String pathName = PathUtilities.createPath("Test3", vspName);

        // @@ check in @@
        ResourceGeneralPage.clickCheckinButton(serviceName);
        GeneralUIUtils.findComponentAndClick(serviceName);
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        PathValidations.AssertCreatedPathExistInCompositionPage(pathName);

        // @@ check out @@
        ResourceGeneralPage.clickCheckoutButton();
        PathValidations.AssertCreatedPathExistInCompositionPage(pathName);
    }

    // Test#4 Jira issue 5441
    @Test
    public void CreateMultiplePaths() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        Pair<String, ServiceReqDetails> pair = getToPathFlow(resourceReqDetails, filePath, HSSFile);
        String vspName = pair.left;
        PathUtilities.createPath("Test4_path1", vspName);
        PathUtilities.createPath("Test4_path2", vspName);
        PathValidations.createPathNumOfRows(7);
        PathValidations.createPathNumOfRows(6);
        SetupCDTest.getExtendTest().log(Status.INFO, "multiple paths have been created");
    }

    // Test#5 Jira issue 5506
    @Test
    public void CreatePathWithComplex() throws Exception {
        List<String> vnfFiles = new ArrayList<>();
        vnfFiles.add(HSSFile);
        vnfFiles.add(VMMEFile);
        Pair<String, String> complex = CreatePathComplexServiceFlow(filePath, vnfFiles);
        String serviceName = complex.left;
        String pathName = complex.right;
        PathValidations.validateServicePath(serviceName, pathName);
        SetupCDTest.getExtendTest().log(Status.INFO, "path with complex service has been created");
    }

    // Test#6 Jira issue 5506
    @Test
    public void CreateExtendedPathWithComplex() throws Exception {
        List<String> vnfFiles = new ArrayList<>();
        vnfFiles.add(fullCompositionFile);
        vnfFiles.add(fullCompositionFile2);
        String[] services = getToComplexService(filePath, vnfFiles);
        PathValidations.validateComplexExtendedPath(services);
        SetupCDTest.getExtendTest().log(Status.INFO, "path with complex service has been created");
    }

    // Test#7 Jira issue 5441
    @Test
    public void CreatePathExtendedTest() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        runCreateExtendedPathFlow(resourceReqDetails, filePath, fullCompositionFile);
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

    public Pair<String, ServiceReqDetails> getToPathFlow(ResourceReqDetails resourceReqDetails, String filepath, String vnfFile) throws Exception {
        String vspName = onboardAndCertify(resourceReqDetails, filepath, vnfFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata = PathUtilities.createService(getUser());
        return new Pair<>(vspName, serviceMetadata);
    }

    public String[] getToComplexService(String filepath, List<String> vnfFiles) throws Exception {
        // create & certify  2 VF
        List<String> VFNames = onboardAndCertifyMultipleVFs(filepath, vnfFiles);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata1 = PathUtilities.createService(getUser());

        // create path 1
        PathUtilities.createPath("newPath", VFNames.get(0));
        PathUtilities.submitForTesting();
        GeneralUIUtils.ultimateWait();
        ServiceReqDetails serviceMetadata2 = PathUtilities.createService(getUser());

        // create path 2
        PathUtilities.createPath("newPath2", VFNames.get(1));
        PathUtilities.submitForTesting();

        //tester
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        TesterOperationPage.certifyComponent(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        TesterOperationPage.certifyComponent(serviceMetadata2.getName());

        //governor
        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        GovernorOperationPage.approveService(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        GovernorOperationPage.approveService(serviceMetadata2.getName());

        //create service for complex service
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata3 = PathUtilities.createService(getUser());
        return new String[]{serviceMetadata1.getName(), serviceMetadata2.getName(), serviceMetadata3.getName()};
    }

    public Pair<String, String> CreatePathComplexServiceFlow(String filepath, List<String> vnfFiles) throws Exception {
        String[] servicesName = getToComplexService(filepath, vnfFiles);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(servicesName[0]);
        CanvasElement service1 = canvasManager.createElementOnCanvas(servicesName[0]);
        CompositionPage.searchForElement(servicesName[1]);
        CanvasElement service2 = canvasManager.createElementOnCanvas(servicesName[1]);
        canvasManager.linkElements(service1, CircleSize.SERVICE, service2, CircleSize.SERVICE);

        // create path
        String pathName = "name1";
        PathUtilities.openCreatePath();
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue()).click();
        return new Pair<>(servicesName[2], pathName);
    }

    public void runCreateExtendedPathFlow(ResourceReqDetails resourceReqDetails, String filepath, String vnfFile) throws Exception {
        Pair<String, ServiceReqDetails> pair = getToPathFlow(resourceReqDetails, filepath, vnfFile);
        String vspName = pair.left;
        PathUtilities.linkVFs(vspName, 4);

        //create path
        PathUtilities.openCreatePath();
        String pathName = "name1";
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        int numOfLines = 3;
        PathValidations.extendPath(numOfLines);

        //delete line
        PathUtilities.deleteLines(1, numOfLines);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        ExtentTestActions.log(Status.INFO, "path has been created");

        // check path is on the list
        PathUtilities.openPathList();
        String PathListName1 = GeneralUIUtils.findByText(pathName).getText();
        String newPathName = "name2";
        PathUtilities.editPathName(pathName, newPathName);
        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.PATH_MENU_BUTTON.getValue() + "']/parent::*").get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());
        String PathListName2 = GeneralUIUtils.findByText(newPathName).getText();
        PathValidations.AssertNameChangeFromPathList(PathListName1, PathListName2);

        // delete path and validate
        PathValidations.ValidateAndDeletePathFromPathList(newPathName);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, "Path has been created");
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
