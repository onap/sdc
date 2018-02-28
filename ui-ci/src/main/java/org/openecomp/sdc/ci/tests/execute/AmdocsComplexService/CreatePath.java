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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.AWTException;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;
import com.mongodb.util.JSON;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.*;
import org.openecomp.sdc.ci.tests.datatypes.enums.CircleSize;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.sanity.Service;
import org.openecomp.sdc.ci.tests.execute.setup.ArtifactsCorrelationManager;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.OpsOperationPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.stringtemplate.v4.ST;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

public class CreatePath extends SetupCDTest {

    protected static String filepath = FileHandling.getVnfRepositoryPath();
    protected String makeDistributionValue;

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
        makeDistributionValue = makeDistributionReadValue;
    }

    //------------------------------------------Tests-----------------------------------------------------


    // Jira issue 5610
    @Test
    public void AssertPathButtons() throws Exception, Throwable {
        filepath = "C:\\Users\\ShiraShe\\Desktop";
        String vnfFile = "fullComposition.zip";
        String vspName = getToPathFlow(filepath, vnfFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        openCreatePath();
        AssertCreatePath();
        AssertExtendPath();
        //SetupCDTest.getExtendTest().log(Status.INFO, String.format("disables buttons are OK"));
    }

    // Jira issue 5441
    @Test
    public void CreatePathTestSanity() throws Exception, Throwable {
        filepath = "C:\\Users\\ShiraShe\\Desktop";
        // List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileList();
        //String vnfFile = fileNamesFromFolder.get(0).toString();
        String vnfFile = "fullComposition.zip";
        String vspName = getToPathFlow(filepath, vnfFile);
        createPath(vspName);
    }

    // Jira issue 5611
    @Test
    public void CreatePathCheckIO() throws Exception, Throwable {
        filepath = "C:\\Users\\ShiraShe\\Desktop";
        // List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileList();
        //String vnfFile = fileNamesFromFolder.get(0).toString();
        String vnfFile = "fullComposition.zip";

        // getToPathFlow
        String vspName = onboardAndCertify(filepath, vnfFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);

        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();

        // create path
        String pathName = createPath(vspName);

        // @@ check in @@
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.LifeCyleChangeButtons.CHECK_IN.getValue());
        GeneralUIUtils.getWebElementByTestID("checkindialog").sendKeys("check in automated confirmation message");
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
        GeneralUIUtils.ultimateWait();

        // go to service composition
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue()).sendKeys(serviceMetadata.getName());
        getDriver().findElements(By.xpath("//*[@data-tests-id='dashboard-Elements']//*[@data-tests-id='" + serviceMetadata.getName() + "']")).get(0).click();
        GeneralUIUtils.ultimateWait();
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();

        AssertCreatedPathExistInCompositionPage(pathName);

        // @@ check out @@
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.LifeCyleChangeButtons.CHECKOUT.getValue());
        GeneralUIUtils.ultimateWait();

        AssertCreatedPathExistInCompositionPage(pathName);

    }

    // Jira issue 5441
    @Test
    public void CreateMultiplePaths() throws Exception, Throwable {
        filepath = "C:\\Users\\ShiraShe\\Desktop";
        // List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileList();
        //String vnfFile = fileNamesFromFolder.get(0).toString();
        String vnfFile = "fullComposition.zip";
        String vspName = getToPathFlow(filepath, vnfFile);
        createPath(vspName);
        createPath(vspName);
        createPathNumOfRows(7);
        createPathNumOfRows(6);
        createPathNumOfRows(4);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("multiple paths have been created"));
    }

    // Jira issue 5506
    @Test
    public void CreatePathWithComplex() throws Exception, Throwable {
        List<String> vnfFiles = new ArrayList<>();
        vnfFiles.add("fullComposition.zip");
        vnfFiles.add("fullCompositionNew.zip");
        filepath = "C:\\Users\\ShiraShe\\Desktop";
        CreatePathComplexServiceFlow(filepath, vnfFiles);
        //String vnfFile = "fullComposition.zip";
       // String vnfFile2 = "fullCompositionNew.zip";
       // runCreatePathComplexServiceFlow(filepath, vnfFile, vnfFile2);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("path with complex service has been created"));
    }

    // Jira issue 5506
    @Test
    public void RealScenarioComplex() throws Exception, Throwable {
        filepath = "C:\\Users\\ShiraShe\\Desktop\\Srini";
        List<String> vnfFiles = new ArrayList<>();
        vnfFiles.add("HSS.zip");
        vnfFiles.add("VMME.zip");
        CreatePathComplexServiceFlow(filepath, vnfFiles);
        ExtentTestActions.addScreenshot(Status.INFO, "Complex service_" + vnfFiles.get(0) ,"Complex service created " + vnfFiles.get(1) + " is as follows : ");
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("path with complex service has been created"));
    }

    // Jira issue 5506
    @Test
    public void CreateExtendedPathWithComplex() throws Exception, Throwable {
        filepath = "C:\\Users\\ShiraShe\\Desktop";
        List<String> vnfFiles = new ArrayList<>();
        vnfFiles.add("fullComposition.zip");
        vnfFiles.add("fullCompositionNew.zip");
        //String vnfFile = "fullComposition.zip";
        //String vnfFile2 = "fullCompositionNew.zip";
        String[] services = ToComplexService(filepath, vnfFiles);
        createComplexExtendedPath(services[0], services[1]);
        ExtentTestActions.addScreenshot(Status.INFO, "Complex service_" + services[0] ,"Complex service created " + services[1] + " is as follows : ");

    }

    // Jira issue 5441
    @Test
    public void CreatePathExtendedTest() throws Exception, Throwable {
        filepath = "C:\\Users\\ShiraShe\\Desktop";
        // List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileList();
        //String vnfFile = fileNamesFromFolder.get(0).toString();
        String vnfFile = "fullComposition.zip";
        runCreateExtendedPathFlow(filepath, vnfFile);
    }

    //create service
    public ServiceReqDetails createService() throws Exception, AWTException {
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        return serviceMetadata;
    }

    // workflow leading to path
    public String onboardAndCertify(String filepath, String vnfFile) throws Exception, IOException {

        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        Pair<String, VendorSoftwareProductObject> onboardAndValidate = OnboardingUiUtils.onboardAndValidate(resourceReqDetails,filepath, vnfFile, getUser());
        String vspName = onboardAndValidate.left;

        DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
        ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFile ,"The topology template for " + vnfFile + " is as follows : ");

        DeploymentArtifactPage.clickSubmitForTestingButton(vspName);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("relogin as TESTER"));
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(vspName);
        TesterOperationPage.certifyComponent(vspName);
        return vspName;
    }

    public List<String> onboardAndCertifyMultipleVFs(String filepath, List<String> vnfFiles) throws Exception, IOException {

        List<String> VFNames = new ArrayList<>();
        for(int i = 0; i<vnfFiles.size(); i++)
        {
            ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
            Pair<String, VendorSoftwareProductObject> onboardAndValidate = OnboardingUiUtils.onboardAndValidate(resourceReqDetails,filepath, vnfFiles.get(i), getUser());
            VFNames.add(i, onboardAndValidate.left);
            DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
            ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFiles.get(i) ,"The topology template for " + vnfFiles.get(i) + " is as follows : ");
            DeploymentArtifactPage.clickSubmitForTestingButton(VFNames.get(i));
        }
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("relogin as TESTER"));
        reloginWithNewRole(UserRoleEnum.TESTER);
        for (int j = 0; j< VFNames.size(); j++)
        {
            GeneralUIUtils.findComponentAndClick(VFNames.get(j));
            TesterOperationPage.certifyComponent(VFNames.get(j));
        }

        return VFNames;
    }

    // path components

    public void AssertCreatePath()  throws Exception, IOException {
        String check = getDriver().findElements(By.xpath("//*[@data-tests-id='Create']")).get(0).getAttribute("disabled");
        if (check.intern() != "true")
        {
            throw new Exception("Button create path should be disabled. open bug to UI team");
        }
    }

    public void AssertCreatedPathExistInCompositionPage(String pathName)  throws Exception, IOException {
        // check path in the list filter
        checkPathFilter(pathName, true);

        openPathList();

        // assert path is in the list
        String PathListName1 = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']")).get(0).getText();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
    }

    public void AssertExtendPath()  throws Exception, IOException {
        String check = getDriver().findElements(By.xpath("//*[@data-tests-id='extendPathlnk']")).get(0).getAttribute("class");
        if (check.intern() != "disabled")
        {
            throw new Exception("Button extend path should be disabled. open bug to UI team");
        }
    }

    public void createPathNumOfRows(int numOfPathRows)throws Exception, AWTException{
        openCreatePath();
        insertValues("extended" + RandomStringUtils.randomAlphanumeric(8),"pathProtocol1", "pathPortNumbers1");
        selectFirstLineParam();
        extendPath(numOfPathRows);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
    }

    public void linkVFs(String vspName, int linksNum)throws Exception, AWTException {

        CompositionPage.searchForElement(vspName);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        List <CanvasElement> VFs = new  ArrayList<CanvasElement>();

        VFs.add(canvasManager.createElementOnCanvas(vspName));

        for (int i = 1; i<linksNum; i++)
        {
            VFs.add(canvasManager.createElementOnCanvas(vspName));
            GeneralUIUtils.ultimateWait();
            canvasManager.linkElements(VFs.get(i), CircleSize.VF, VFs.get(i-1), CircleSize.VF);
            SetupCDTest.getExtendTest().log(Status.INFO, String.format("link VFs"));
            GeneralUIUtils.ultimateWait();
        }
    }

    public void linkServices(String Service1, String Service2, int linksNum)throws Exception, AWTException {

        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        List <CanvasElement> VFs = new  ArrayList<CanvasElement>();

        // get first service
        CompositionPage.searchForElement(Service1);
        VFs.add(canvasManager.createElementOnCanvas(Service1));

        String service = Service2;
        for (int i = 1; i<linksNum; i++)
        {
            CompositionPage.searchForElement(service);
            VFs.add(canvasManager.createElementOnCanvas(service));
            GeneralUIUtils.ultimateWait();
            canvasManager.linkElements(VFs.get(i), CircleSize.SERVICE, VFs.get(i-1), CircleSize.SERVICE);
            SetupCDTest.getExtendTest().log(Status.INFO, String.format("link services"));
            GeneralUIUtils.ultimateWait();

            // change service to link
            if (service.equals(Service2)) {
                service = Service1;
            }
            else {
                service = Service2;
            }
        }
    }

    public void openCreatePath() throws Exception, AWTException{

        getDriver().findElements(By.xpath("//*[@data-tests-id='pathsMenuBtn']/parent::*")).get(0).click();
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_PATH_MENU_BUTTON.getValue());
    }

    public void openPathList() throws Exception, AWTException {
        getDriver().findElements(By.xpath("//*[@data-tests-id='pathsMenuBtn']/parent::*")).get(0).click();
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());
    }

    public void sendValue(String DataTestId, String value) throws Exception, AWTException {

        GeneralUIUtils.getWebElementByTestID(DataTestId).sendKeys(value);
        GeneralUIUtils.ultimateWait();
    }

    public void insertValues(String pathName, String pathProtocol, String pathPortNumbers) throws Exception, AWTException {

        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_NAME.getValue(),pathName);
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_PROTOCOL.getValue(),pathProtocol);
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_PORT_NUMBER.getValue(),pathPortNumbers);
    }

    public void selectFirstLineParam() throws Exception, AWTException{
        getDriver().findElements(By.xpath("//*[@data-tests-id='linkSrc']//option")).get(0).click();
        GeneralUIUtils.ultimateWait();
        getDriver().findElements(By.xpath("//*[@data-tests-id='linkSrcCP']//option")).get(0).click();
        GeneralUIUtils.ultimateWait();
        getDriver().findElements(By.xpath("//*[@data-tests-id='linkTarget']//option")).get(0).click();
        GeneralUIUtils.ultimateWait();
        getDriver().findElements(By.xpath("//*[@data-tests-id='linkTargetCP']//option")).get(0).click();
        GeneralUIUtils.ultimateWait();
    }

    public void checkPathFilter(String pathName, boolean isFound) throws Exception, AWTException{
        GeneralUIUtils.ultimateWait();
        List<WebElement> pathFilterList = getDriver().findElements(By.xpath("//*[@data-tests-id='service-path-selector']//option"));
        if (isFound) {
            for (int i = 0; i < pathFilterList.size(); i++) {
                String element_text = pathFilterList.get(i).getText();
                if (element_text.equals(pathName))
                {
                    break;
                }
                if (i == pathFilterList.size() -1 )
                {
                    SetupCDTest.getExtendTest().log(Status.INFO, String.format("path list filter check failed"));
                    throw new Exception("path filter list is missing a path");
                }
            }
        }
        else {
            for (int i = 0; i < pathFilterList.size(); i++) {
                String element_text = pathFilterList.get(i).getText();
                if (element_text.equals(pathName))
                {
                    SetupCDTest.getExtendTest().log(Status.INFO, String.format("path list filter check failed"));
                    throw new Exception("path filter list is has a path that should be deleted");
                }
            }
        }
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("path list filter check passed"));
    }

    public void extendPath(int numOfLines) throws Exception, AWTException {

        int i;
        for (i = 0; i < numOfLines; i++) {
            String check;
            String index = Integer.toString(i + 2);
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.EXTEND_BUTTON.getValue());
            List<WebElement> linkSrcs = getDriver().findElements(By.xpath("//*[@data-tests-id='linkSrc']//select"));
            List<WebElement> linkSrcCPs = getDriver().findElements(By.xpath("//*[@data-tests-id='linkSrcCP']//select"));
            List<WebElement> linkTargets = getDriver().findElements(By.xpath("//*[@data-tests-id='linkTarget']//select"));
            List<WebElement> linkTargetCPs = getDriver().findElements(By.xpath("//*[@data-tests-id='linkTargetCP']//select"));

            for (int j = 0; j < i + 2; j++) {
                check = linkSrcs.get(j).getAttribute("class");
                if (!check.intern().contains("disabled")) {
                    throw new Exception("Source should be disabled. open bug to UI team");
                }
                check = linkSrcCPs.get(j).getAttribute("class");
                if (!check.intern().contains("disabled")) {
                    throw new Exception("Source connection point of last lines should be disabled. open bug to UI team");
                }
            }
            for (int j = 0; j < i + 1; j++) {

                check = linkTargets.get(j).getAttribute("class");
                if (!check.intern().contains("disabled")) {
                    throw new Exception("Target of last lines should be disabled. open bug to UI team");
                }
                check = linkTargetCPs.get(j).getAttribute("class");
                if (!check.intern().contains("disabled")) {
                    throw new Exception("Target connection point of last lines should be disabled. open bug to UI team");
                }
            }

            List <WebElement> choices = getDriver().findElements(By.xpath("//*[" + index + "]/*[@data-tests-id='linkTarget']//option"));
            choices.get((new Random()).nextInt(choices.size())).click();
            choices = getDriver().findElements(By.xpath("//*[" + index + "]/*[@data-tests-id='linkTargetCP']//option"));
            choices.get((new Random()).nextInt(choices.size())).click();
        }
    }

    public String editPath(String pathName) throws Exception, AWTException{
        getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).get(0).click();
        pathName = "name2";
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ComplexServiceAmdocs.PATH_NAME.getValue()).clear();
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_NAME.getValue(), pathName);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.SAVE.getValue());
        return pathName;
    }

    public int deleteLines(int numOfLinesToDelete,int numOfLines)throws Exception, AWTException{

        for (int i=0; i<numOfLinesToDelete; i++){
            getDriver().findElements(By.xpath("//*[@data-tests-id='removeLnk']//span")).get(0).click();
            numOfLines--;
        }
        if (getDriver().findElements(By.xpath("//*[@data-tests-id='linkTargetCP']//option")).size()>(numOfLines+1))
        {
            throw new Exception("Path element was not deleted");
        }
        GeneralUIUtils.ultimateWait();
        return numOfLines;
    }

    public String getToPathFlow(String filepath, String vnfFile) throws Exception, AWTException {

        String vspName = onboardAndCertify(filepath, vnfFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        return vspName;
    }

    // path scenarios

    public String createPath(String vspName) throws Exception, AWTException {

        linkVFs(vspName, 3);
        openCreatePath();
        String pathName = "name1";
        insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        //select path elements
        selectFirstLineParam();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("path has been created"));
        return pathName;
    }

    // flows

    public void createExtendedPath(String vspName) throws Exception, AWTException {
        //drag and drop VFs and link them
        linkVFs(vspName, 5);

        //open create path box
        openCreatePath();
        String pathName = "name1";
        insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        selectFirstLineParam();
        //extend path
        int numOfLines = 3;
        extendPath(numOfLines);
        //delete line
        int numOfLinesToDelete = 1;
        numOfLines = deleteLines(numOfLinesToDelete, numOfLines);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        ExtentTestActions.log(Status.INFO, "path has been created");

        // check that path exist in the path filter list
        checkPathFilter(pathName, true);

        // check that path exist in the path list
        getDriver().findElements(By.xpath("//*[@data-tests-id='pathsMenuBtn']/parent::*")).get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());
        String PathListName1 = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']")).get(0).getText();

        // edit path
        pathName = editPath(pathName);

        // go back to path's list
        getDriver().findElements(By.xpath("//*[@data-tests-id='pathsMenuBtn']/parent::*")).get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());

        // get name
        String PathListName2 = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']")).get(0).getText();

        // assert names changed
        if (PathListName1.equals(PathListName2))
        {
            throw new Exception("path name expected to change after edit but did not");
        }

        // delete path
        int paths_before_deletion = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).size();
        getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).get(1).click();
        GeneralUIUtils.ultimateWait();
        int paths_after_deletion = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).size();
        if (paths_after_deletion == paths_before_deletion)
        {
            throw new Exception("path expected to be deleted but did not");
        }

        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
        //ArtifactsCorrelationManager.addVNFtoServiceArtifactCorrelation(serviceMetadata.getName(), vspName);
        //ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());
        //ExtentTestActions.addScreenshot(Status.INFO, "ServiceComposition_" + vnfFile, "The service topology is as follows: ");
        //ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Path has been created"));

        // check that path got deleted in the path filter list
        checkPathFilter(pathName, false);
    }

    public void createComplexExtendedPath(String Service1, String Service2) throws Exception, AWTException {
        //drag and drop Services and link them
        linkServices(Service1, Service2, 5);

        //-----------------------------------------create path-------------------------------------------------

        //open create path box
        openCreatePath();

        String pathName = "name1";
        String pathProtocol = "pathProtocol1";
        String pathPortNumbers = "pathPortNumbers1";

        insertValues(pathName, pathProtocol, pathPortNumbers);

        //select path parameters
        selectFirstLineParam();
        //extend path
        int numOfLines = 3;
        extendPath(numOfLines);

        //delete line
        int numOfLinesToDelete = 1;
        numOfLines = deleteLines(numOfLinesToDelete, numOfLines);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        ExtentTestActions.log(Status.INFO, "path has been created");

        //---------------------------------------check path's list-----------------------------

        // go to path's list
        openPathList();

        // get name
        String PathListName1 = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']")).get(0).getText();

        // edit path
        pathName = editPath(pathName);

        // go back to path's list
        getDriver().findElements(By.xpath("//*[@data-tests-id='pathsMenuBtn']/parent::*")).get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());

        // get name
        String PathListName2 = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']")).get(0).getText();

        // assert names changed
        if (PathListName1.equals(PathListName2))
        {
            throw new Exception("path name expected to change after edit but did not");
        }

        // delete path
        int paths_before_deletion = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).size();
        getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).get(1).click();
        Thread.sleep(1000);
        int paths_after_deletion = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).size();
        if (paths_after_deletion == paths_before_deletion)
        {
            throw new Exception("path expected to be deleted but did not");
        }
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
        //ArtifactsCorrelationManager.addVNFtoServiceArtifactCorrelation(serviceMetadata.getName(), vspName);

        //assertNotNull(vfElement);
        //ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());
        //ExtentTestActions.addScreenshot(Status.INFO, "ServiceComposition_" + vnfFile, "The service topology is as follows: ");

        //ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Path has been created"));
    }

    public String[] getToComplexService(String filepath, String vnfFile, String vnfFile2) throws Exception, AWTException{
        // create & certify  2 VF
        String vspName1 = onboardAndCertify(filepath, vnfFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        String vspName2 = onboardAndCertify(filepath, vnfFile2);

        // create service 1
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata1 = createService();
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(vspName1);
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();

        // create service 2
        ServiceReqDetails serviceMetadata2 = createService();
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(vspName2);
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();

        //tester
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        TesterOperationPage.certifyComponent(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        TesterOperationPage.certifyComponent(serviceMetadata2.getName());

        //governor
        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata2.getName());

        //create service for complex service
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();

        return new String[]{serviceMetadata1.getName(), serviceMetadata2.getName()};
    }

    public String[] ToComplexService(String filepath, List<String> vnfFiles) throws Exception, AWTException{
        // create & certify  2 VF
        List<String> VFNames = onboardAndCertifyMultipleVFs(filepath, vnfFiles);

        // create service 1
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata1 = createService();
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(VFNames.get(0));
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();
        GeneralUIUtils.ultimateWait();;

        // create service 2
        ServiceReqDetails serviceMetadata2 = createService();
        GeneralUIUtils.ultimateWait();;
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(VFNames.get(1));
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();

        //tester
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        TesterOperationPage.certifyComponent(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        TesterOperationPage.certifyComponent(serviceMetadata2.getName());

        //governor
        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata2.getName());

        //create service for complex service
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();

        return new String[]{serviceMetadata1.getName(), serviceMetadata2.getName()};
    }

    public void runCreatePathComplexServiceFlow(String filepath, String vnfFile, String vnfFile2) throws Exception, AWTException {
        // create & certify  2 VF
        String vspName1 = onboardAndCertify(filepath, vnfFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        String vspName2 = onboardAndCertify(filepath, vnfFile2);

        // create service 1
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata1 = createService();
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(vspName1);
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();

        // create service 2
        ServiceReqDetails serviceMetadata2 = createService();
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(vspName2);
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();

        //tester
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        TesterOperationPage.certifyComponent(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        TesterOperationPage.certifyComponent(serviceMetadata2.getName());

        //governor
        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata2.getName());

        //create service for complex service
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // link services
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(serviceMetadata1.getName());
        CanvasElement service1 = canvasManager.createElementOnCanvas(serviceMetadata1.getName());
        CompositionPage.searchForElement(serviceMetadata2.getName());
        CanvasElement service2 = canvasManager.createElementOnCanvas(serviceMetadata2.getName());
        canvasManager.linkElements(service1,CircleSize.SERVICE, service2, CircleSize.SERVICE);

        // create path
        openCreatePath();
        insertValues("name1", "pathProtocol1", "pathPortNumbers1");
        selectFirstLineParam();
        GeneralUIUtils.getWebElementByTestID("Create").click();
    }

    public void CreatePathComplexServiceFlow(String filepath, List<String> vnfFiles) throws Exception, AWTException {
        // create & certify  2 VF
        List<String> vfNames = onboardAndCertifyMultipleVFs(filepath, vnfFiles);

        // create service 1
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceMetadata1 = createService();
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(vfNames.get(0));
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();

        // create service 2
        ServiceReqDetails serviceMetadata2 = createService();
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // create path
        createPath(vfNames.get(0));
        getDriver().findElements(By.xpath("//*[@data-tests-id='submit_for_testing']")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='changeLifeCycleMessage']")).get(0).sendKeys("new Service to test");
        getDriver().findElements(By.xpath("//*[@data-tests-id='OK']")).get(0).click();

        //tester
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        TesterOperationPage.certifyComponent(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        TesterOperationPage.certifyComponent(serviceMetadata2.getName());

        //governor
        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(serviceMetadata1.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata1.getName());
        GeneralUIUtils.findComponentAndClick(serviceMetadata2.getName());
        GovernorOperationPage.approveSerivce(serviceMetadata2.getName());

        //create service for complex service
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());
        // go to composition
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        // link services
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(serviceMetadata1.getName());
        CanvasElement service1 = canvasManager.createElementOnCanvas(serviceMetadata1.getName());
        CompositionPage.searchForElement(serviceMetadata2.getName());
        CanvasElement service2 = canvasManager.createElementOnCanvas(serviceMetadata2.getName());
        canvasManager.linkElements(service1,CircleSize.SERVICE, service2, CircleSize.SERVICE);

        // create path
        openCreatePath();
        insertValues("name1", "pathProtocol1", "pathPortNumbers1");
        selectFirstLineParam();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
    }

    public void runCreateExtendedPathFlow(String filepath, String vnfFile) throws Exception, AWTException {

        String vspName = getToPathFlow(filepath, vnfFile);

        //drag and drop VFs and link them
        linkVFs(vspName, 5);

        //-----------------------------------------create path-------------------------------------------------

        //open create path box
        openCreatePath();
        String pathName = "name1";
        insertValues(pathName, "pathProtocol1", "pathPortNumbers1");

        //select path parameters
        selectFirstLineParam();
        //extend path
        int numOfLines = 3;
        extendPath(numOfLines);

        //delete line
        int numOfLinesToDelete = 1;
        numOfLines = deleteLines(numOfLinesToDelete, numOfLines);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        ExtentTestActions.log(Status.INFO, "path has been created");

        // check path is on the list
        openPathList();
        String PathListName1 = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']")).get(0).getText();

        // edit path
        pathName = editPath(pathName);

        // go back to path's list
        getDriver().findElements(By.xpath("//*[@data-tests-id='pathsMenuBtn']/parent::*")).get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());

        // get name
        String PathListName2 = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']")).get(0).getText();

        // assert names changes
        if (PathListName1.equals(PathListName2))
        {
            throw new Exception("path name expected to change after edit but did not");
        }

        // delete path
        int paths_before_deletion = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).size();
        getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).get(1).click();
        GeneralUIUtils.ultimateWait();
        int paths_after_deletion = getDriver().findElements(By.xpath("//*[text()='"+pathName+"']/parent::*//span")).size();
        if (paths_after_deletion == paths_before_deletion)
        {
            throw new Exception("path expected to be deleted but did not");
        }
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
        //ArtifactsCorrelationManager.addVNFtoServiceArtifactCorrelation(serviceMetadata.getName(), vspName);
        //ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());
        //ExtentTestActions.addScreenshot(Status.INFO, "ServiceComposition_" + vnfFile, "The service topology is as follows: ");
        //ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Path has been created"));
    }

    public void runCreatePathFlow(String filepath, String vnfFile) throws Exception, AWTException {
        String vspName = onboardAndCertify(filepath, vnfFile);

        reloginWithNewRole(UserRoleEnum.DESIGNER);
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();

        CompositionPage.searchForElement(vspName);
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CanvasElement vfElement1 = canvasManager.createElementOnCanvas(vspName);
        CanvasElement vfElement2 = canvasManager.createElementOnCanvas(vspName);
        CanvasElement vfElement3 = canvasManager.createElementOnCanvas(vspName);
        CanvasElement vfElement4 = canvasManager.createElementOnCanvas(vspName);
        CanvasElement vfElement5 = canvasManager.createElementOnCanvas(vspName);
        canvasManager.linkElements(vfElement1, CircleSize.VF, vfElement2, CircleSize.VF);
        canvasManager.linkElements(vfElement3,CircleSize.VF, vfElement2, CircleSize.VF);
        canvasManager.linkElements(vfElement3, CircleSize.VF, vfElement4, CircleSize.VF);
        canvasManager.linkElements(vfElement5, CircleSize.VF, vfElement4, CircleSize.VF);
        canvasManager.linkElements(vfElement5, CircleSize.VF, vfElement1, CircleSize.VF);

        //create path

        getDriver().findElements(By.xpath("//*[@data-tests-id='pathsMenuBtn']/parent::*")).get(0).click();
//        GeneralUIUtils.getWebElementByTestID("pathsMenuBtn").click();
        GeneralUIUtils.getWebElementByTestID("createPathMenuItem").click();
        //insert values
        GeneralUIUtils.getWebElementByTestID("pathName").sendKeys("name");
        GeneralUIUtils.getWebElementByTestID("pathProtocol").sendKeys("pathProtocol");
        GeneralUIUtils.getWebElementByTestID("pathPortNumbers").sendKeys("pathPortNumbers");
        //select path parameters

        getDriver().findElements(By.xpath("//*[@data-tests-id='linkSrc']//option")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='linkSrcCP']//option")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='linkTarget']//option")).get(0).click();
        getDriver().findElements(By.xpath("//*[@data-tests-id='linkTargetCP']//option")).get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());

        //ArtifactsCorrelationManager.addVNFtoServiceArtifactCorrelation(serviceMetadata.getName(), vspName);

        //assertNotNull(vfElement);
        //ServiceVerificator.verifyNumOfComponentInstances(serviceMetadata, "0.1", 1, getUser());
        //ExtentTestActions.addScreenshot(Status.INFO, "ServiceComposition_" + vnfFile, "The service topology is as follows: ");

        //ServiceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}