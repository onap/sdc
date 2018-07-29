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
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.execute.setup.ArtifactsCorrelationManager;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.DownloadManager;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;


public class PathValidations {

    public static String[] validateServiceExtendedPath(String vspName) throws Exception {
        PathUtilities.linkVFs(vspName, 5);
        PathUtilities.openCreatePath();
        String pathName = "name1";
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        PathValidations.extendPath(3);
        //delete line
        PathUtilities.deleteLines(1, 3);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        ExtentTestActions.log(Status.INFO, "path has been created");
        PathValidations.checkPathFilter(pathName, true);
        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.PATH_MENU_BUTTON.getValue() + "']/parent::*").get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());
        String PathListName1 = GeneralUIUtils.findByText(pathName).getText();
        // edit path
        String newPathName = "name2";
        PathUtilities.editPathName(pathName, newPathName);

        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.PATH_MENU_BUTTON.getValue() + "']/parent::*").get(0).click();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());
        String PathListName2 = GeneralUIUtils.findByText(newPathName).getText();

        // assert names changed
        assertNotSame("path name expected to change after edit but did not", PathListName1, PathListName2);

        // delete path
        int paths_before_deletion = GeneralUIUtils.findElementsByXpath("//*[text()='" + newPathName + "']/parent::*//span").size();
        GeneralUIUtils.findElementsByXpath(newPathName).get(1).click();
        GeneralUIUtils.ultimateWait();
        int paths_after_deletion = GeneralUIUtils.findElementsByXpath("//*[text()='" + newPathName + "']/parent::*//span").size();
        assertNotSame("path expected to be deleted but did not", paths_after_deletion, paths_before_deletion);

        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, "Path has been created");
        // check that path got deleted in the path filter list
        PathValidations.checkPathFilter(newPathName, false);
        return new String[] {pathName, newPathName};
    }

    public static String[] validateComplexExtendedPath(String[] services) throws Exception {
        PathUtilities.linkServices(services[0], services[1], 5);
        PathUtilities.openCreatePath();
        String pathName = "name1";
        PathUtilities.insertValues(pathName, "pathProtocol1", "pathPortNumbers");
        PathUtilities.selectFirstLineParam();
        int numOfLines = 3;
        PathValidations.extendPath(numOfLines);
        //delete line
        PathUtilities.deleteLines(1, numOfLines);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        ExtentTestActions.log(Status.INFO, "path has been created");
        PathUtilities.openPathList();
        String PathListName1 = GeneralUIUtils.findByText(pathName).getText();
        String newPathName = "name2";
        PathUtilities.editPathName(pathName, newPathName);
        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.PATH_MENU_BUTTON.getValue() + "']/parent::*").get(0).click();
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());
        String PathListName2 = GeneralUIUtils.findByText(newPathName).getText();

        // assert names changed
        assertNotSame("path name expected to change after edit but did not", PathListName1, PathListName2);

        // delete path
        int paths_before_deletion = GeneralUIUtils.findElementsByXpath("//*[text()='" + newPathName + "']/parent::*//span").size();
        GeneralUIUtils.findElementsByXpath("//*[text()='" + newPathName + "']/parent::*//span").get(1).click();
        GeneralUIUtils.ultimateWait();

        int paths_after_deletion = GeneralUIUtils.findElementsByXpath("//*[text()='" + newPathName + "']/parent::*//span").size();
        assertNotSame("path expected to be deleted but did not", paths_after_deletion, paths_before_deletion);

        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
        PathValidations.validateServicePath(services[2], pathName);
        PathValidations.validateServicePath(services[2], newPathName);
        SetupCDTest.getExtendTest().log(Status.INFO, "Paths have been validated");
        return new String[] {pathName, newPathName};
    }

    public static void createPathNumOfRows(int numOfPathRows)throws Exception{
        PathUtilities.openCreatePath();
        PathUtilities.insertValues("extended" + RandomStringUtils.randomAlphanumeric(8),"pathProtocol1", "pathPortNumbers1");
        PathUtilities.selectFirstLineParam();
        extendPath(numOfPathRows);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, "Path with " + numOfPathRows + " rows has been created");
    }

    public static void extendPath(int numOfLines) throws Exception {
        for (int i=0; i < numOfLines; i++) {
            String check;
            String index = Integer.toString(i + 2);
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.EXTEND_BUTTON.getValue());

            List<WebElement> linkSrcs =  GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='"+DataTestIdEnum.ComplexServiceAmdocs.LINK_SOURCE.getValue()+"']//select");
            List<WebElement> linkSrcCPs = GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='"+DataTestIdEnum.ComplexServiceAmdocs.LINK_SOURCE_CP.getValue()+"']//select");
            List<WebElement> linkTargets = GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='"+DataTestIdEnum.ComplexServiceAmdocs.LINK_TARGET.getValue()+"']//select");
            List<WebElement> linkTargetCPs = GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='"+DataTestIdEnum.ComplexServiceAmdocs.LINK_TARGET_CP.getValue()+"']//select");
            for (int j = 0; j < i + 2; j++) {
                validateExtendedPathDisabledButtons(linkSrcs, j, "Source should be disabled. open bug to UI team");
                check = linkSrcCPs.get(j).getAttribute("class");
                validateElementDisabledAttribute(check, "Source connection point");
            }
            for (int j = 0; j < i + 1; j++) {
                check = linkTargets.get(j).getAttribute("class");
                validateElementDisabledAttribute(check, "Target");
                check = linkTargetCPs.get(j).getAttribute("class");
                validateElementDisabledAttribute(check, "Target connection point");
            }
            List <WebElement> choices = GeneralUIUtils.findElementsByXpath("//*[" + index + "]/*[@data-tests-id='"+DataTestIdEnum.ComplexServiceAmdocs.LINK_TARGET.getValue()+"']//option");
            choices.get((new Random()).nextInt(choices.size())).click();
            choices = GeneralUIUtils.findElementsByXpath("//*[" + index + "]/*[@data-tests-id='"+DataTestIdEnum.ComplexServiceAmdocs.LINK_TARGET_CP.getValue()+"']//option");
            choices.get((new Random()).nextInt(choices.size())).click();
        }
    }

    public static void validateElementDisabledAttribute(String check, String param) throws Exception{
        assertEquals(param + " of last lines should be disabled", check.contains("disabled"), true);
    }

    public static void validateExtendedPathDisabledButtons(List<WebElement> linkSrcs, int i, String errMsg) throws Exception {
        String check = linkSrcs.get(i).getAttribute("class");
        assertEquals(errMsg, check.contains("disabled"), true);
    }

    public static void validatePathListIsEmpty() throws Exception {
        PathUtilities.openPathList();
        try {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_DELETE.getValue());
        }
        catch (Exception e)
        {
            SetupCDTest.getExtendTest().log(Status.INFO, "path list is empty");
        }
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CLOSE.getValue());
    }

    public static void AssertNameChangeFromPathList(String PathListName1, String PathListName2) throws Exception {
        assertNotSame("path name is expected to change after edit", PathListName1, PathListName2);
    }

    public static void ValidateAndDeletePathFromPathList(String pathName) throws Exception {
        // count paths
        List<WebElement> path_list_name = GeneralUIUtils.getWebElementsListByTestID(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_NAME.getValue());
        // delete paths
        List<WebElement> path_delete = GeneralUIUtils.getWebElementsListByTestID(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_DELETE.getValue());
        int paths_before_deletion = 0;
        for (int i = 0; i < path_list_name.size(); i++) {
            if (path_list_name.get(i).getText().equals(pathName)) {
                paths_before_deletion++;
                path_delete.get(i).click();
            }
        }
        int paths_after_deletion = 0;
        List<WebElement> path_list_name2;
        try {
            path_list_name2 = GeneralUIUtils.getWebElementsListByTestID(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_NAME.getValue());
            for (WebElement path_list_element : path_list_name2) {
                if (path_list_element.getText().equals(pathName)) {
                    paths_after_deletion++;
                }
            }
        } catch (Exception ignore) {}
        // assert deletion
        assertNotSame("path is expected to be deleted", paths_after_deletion, paths_before_deletion);
    }

    public static void checkPathFilter(String pathName, boolean isFound) throws Exception{
        List<WebElement> pathFilterList = GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='"+DataTestIdEnum.ComplexServiceAmdocs.SERVICE_PATH_SELECTOR.getValue()+"']//option");
        GeneralUIUtils.ultimateWait();
        if (isFound) for (int i = 0; i < pathFilterList.size(); i++) {
            String element_text = pathFilterList.get(i).getText();
            if (element_text.equals(pathName)) break;
            assertNotSame("path filter list is missing a path", i, pathFilterList.size() - 1);
        }
        else for (WebElement aPathFilterList : pathFilterList) {
            String element_text = aPathFilterList.getText();
            assertNotSame("path filter list is has a path that should be deleted", element_text, pathName);
        }
        SetupCDTest.getExtendTest().log(Status.INFO, "path list filter check passed");
    }

    public static void AssertCreatePath() throws Exception {
        String check = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue()).getAttribute("disabled");
        assertEquals("create button path should be disabled", check, "true");
    }

    public static void AssertCreatedPathExistInCompositionPage(String pathName) throws Exception {
        checkPathFilter(pathName, true);
        PathUtilities.openPathList();

        // assert path is in the list
        GeneralUIUtils.findByText(pathName).getText();
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_CLOSE.getValue());
//        GeneralUIUtils.ultimateWait();
        SetupCDTest.getExtendTest().log(Status.INFO, "Path is in the path list");
//        GeneralUIUtils.ultimateWait();
    }

    public static void AssertExtendPath() throws Exception {
        String check = GeneralUIUtils.getWebElementsListByTestID(DataTestIdEnum.ComplexServiceAmdocs.EXTEND_BUTTON.getValue()).get(0).getAttribute("class");
        assertEquals("Extend button should be disabled", check, "disabled");
    }

    public static Pair<RestResponse, ServiceReqDetails> validateServicePath(String serviceName, String name) throws Exception {
        Pair<RestResponse, ServiceReqDetails> servicePaths = PathUtilities.getServicePathsAPI(serviceName);
        String response = servicePaths.left.getResponse();

        JSONObject jsonResponse = new JSONObject(response);
        JSONObject forwardingPaths = jsonResponse.getJSONObject("forwardingPaths");
        Boolean validation_complete = Boolean.FALSE;
        for (Object key : forwardingPaths.keySet()){
            String keyStr = (String)key;
            JSONObject forwardingPath = forwardingPaths.getJSONObject(keyStr);
            if (forwardingPath.getString("name").equals(name)) {
                JSONObject pathElements = forwardingPath.getJSONObject("pathElements");
                Boolean empty = pathElements.getBoolean("empty");
                assertEquals("assert pathElements are not empty", empty, Boolean.FALSE);
                validation_complete = Boolean.TRUE;
                break;
            }
        }
        assertEquals("validation success", validation_complete, Boolean.TRUE);
        return servicePaths;
    }

    public static void ValidateThereIsNoErrorMessage() throws Exception {
        try {
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.OK.getValue() + "']");
            throw new Exception("element should not be found");
        } catch (Exception e) {
            if (e.getMessage().equals("element should not be found")) {
                throw e;
            }
        }
    }

    public static void validateEditToExistingName(String firstPathName, String secondPathName)throws Exception {
        PathUtilities.openPathList();
        PathUtilities.editPathName(secondPathName, firstPathName);
        try {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
        } catch (Exception e) {
            throw new Exception("when creating another path with duplicate name, expected error did not appear");
        }
        PathUtilities.openPathList();
        PathUtilities.editPathName(secondPathName, firstPathName + "       ");
        try {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
        } catch (Exception e) {
            throw new Exception("space in beggining or end does not count. when creating another path with duplicate name, expected error did not appear");
        }
        PathUtilities.openPathList();
        PathUtilities.editPathName(secondPathName,"           " + firstPathName);
        try {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
        } catch (Exception e) {
            throw new Exception("space in beggining or end does not count. when creating another path with duplicate name, expected error did not appear");
        }
        PathUtilities.openPathList();
        PathUtilities.editPathProtocol(secondPathName, "pathProtocol2");
    }

    public static void validateNameWithSpaces(String pathName, String vspName) throws Exception{
        PathUtilities.createPath(pathName + "           ", vspName);
        PathUtilities.openPathList();
        PathUtilities.editPathName(pathName, "newName");
        PathUtilities.createPathWithoutLink("               " + pathName, vspName);
        PathUtilities.openPathList();
        PathUtilities.editPathName(pathName, "newName2");
    }

    public static void importAndVerifyVSP(VendorSoftwareProductObject createVendorSoftwareProduct, String filepath, String vnfFile) throws Exception {
        DownloadManager.downloadCsarByNameFromVSPRepository(createVendorSoftwareProduct.getName(), createVendorSoftwareProduct.getVspId());
        File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();

        OnboardingUiUtils.importVSP(createVendorSoftwareProduct);

        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();

        // Verify deployment artifacts
        Map<String, Object> combinedMap = ArtifactFromCsar.combineHeatArtifacstWithFolderArtifacsToMap(latestFilefromDir.getAbsolutePath());

        LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get("Deployment"));
        ArtifactsCorrelationManager.addVNFartifactDetails(vnfFile, deploymentArtifacts);

        List<String> heatEnvFilesFromCSAR = deploymentArtifacts.stream().filter(e -> e.getType().equals("HEAT_ENV")).
                map(e -> e.getFileName()).
                collect(Collectors.toList());

        OnboardingUiUtils.validateDeploymentArtifactsVersion(deploymentArtifacts, heatEnvFilesFromCSAR);

        DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, vnfFile);
    }

}
