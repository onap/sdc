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

package org.onap.sdc.frontend.ci.tests.pages;

import com.aventstack.extentreports.Status;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.AssertJUnit;

import java.util.List;

public class CompositionPage extends GeneralPageElements {

    private CompositionPage() {
        super();
    }

    public static UploadArtifactPopup artifactPopup() {
        return new UploadArtifactPopup(true);
    }

    public static void searchForElement(String elementName) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + elementName + " in the left panel");
        WebElement searchField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.SEARCH_ASSET.getValue());
        searchField.clear();
        searchField.sendKeys(elementName);
        GeneralUIUtils.ultimateWait();
    }

    public static void showDeploymentArtifactTab() throws Exception {
        clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.DEPLOYMENT_ARTIFACT_TAB);
    }

    public static void showInformationsTab() throws Exception {
        clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.INFORMATION_TAB);
    }

    public static void showPropertiesAndAttributesTab() throws Exception {
        clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.PROPERTIES_AND_ATTRIBUTES_TAB);
    }

    public static List<WebElement> getProperties() {
        return PropertiesPage.getElemenetsFromTable();
    }

    public static List<WebElement> getDeploymentArtifacts() {
        return getAllAddedArtifacts();
    }

    public static List<WebElement> getAllAddedArtifacts() {
        String dataTestsId = DataTestIdEnum.CompositionScreenEnum.ARTIFACTS_LIST.getValue();
        return GeneralUIUtils.getWebElementsListBy(By.xpath("//*[contains(@data-tests-id,'" + dataTestsId + "')]"));
    }

    public static void moveToInputsScreen() throws Exception {
        openPagesMenu(2);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.MENU_INPUTS.getValue());
        //GeneralUIUtils.ultimateWait();
    }

    public static void moveToPropertiesScreen() throws Exception {
        openPagesMenu(2);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.MENU_PROPERTIES_ASSIGNMENT.getValue());
        //GeneralUIUtils.ultimateWait();
    }

    public static void moveToOnboardScreen() throws Exception {
        openPagesMenu(0);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.MENU_ONBOARD.getValue());
        //GeneralUIUtils.ultimateWait();
    }

    public static void moveToHomeScreen() throws Exception {
        openPagesMenu(0);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.MENU_HOME.getValue());
        //GeneralUIUtils.ultimateWait();
    }

    private static void openPagesMenu(int counter) {
        Actions actions = new Actions(GeneralUIUtils.getDriver());
        List<WebElement> triangleList = GeneralUIUtils.getWebElementsListByClassName(DataTestIdEnum.CompositionScreenEnum.MENU_TRIANGLE_DROPDOWN.getValue());
        WebElement pagesMenu = triangleList.get(counter);
        actions.moveToElement(pagesMenu).perform();
    }

    public static void changeComponentVersion(CanvasManager canvasManager, CanvasElement element, String version) {
        changeComponentVersion(canvasManager, element, version, false);
    }

    public static void changeComponentVersion(CanvasManager canvasManager, CanvasElement element, String version, boolean isValidate) {
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, String.format("Changing component version to  %s", version));
            canvasManager.clickOnCanvaElement(element);
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.CHANGE_VERSION.getValue());
            //GeneralUIUtils.ultimateWait();
            Select selectlist = new Select(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.CHANGE_VERSION.getValue()));
            while (selectlist.getOptions().size() == 0) {
                selectlist = new Select(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.CHANGE_VERSION.getValue()));
            }
            GeneralUIUtils.ultimateWait();
            selectlist.selectByValue(version);
            GeneralUIUtils.ultimateWait();
            GeneralUIUtils.clickSomewhereOnPage();

            // Validate Selection
            if (isValidate) {
                GeneralUIUtils.ultimateWait();
                canvasManager.clickOnCanvaElement(element);
                SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating component version changed to %s", version));
                String actualSelectedValue = GeneralUIUtils.getWebElementBy(By.xpath(String.format("//select[@data-tests-id='%s']//option[@selected='selected']", DataTestIdEnum.CompositionScreenEnum.CHANGE_VERSION.getValue()))).getText();
                AssertJUnit.assertTrue(actualSelectedValue.equals(version));
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void clickAddArtifactButton() throws Exception {
        clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.ADD_ARTIFACT);
        GeneralUIUtils.getWebElementByClassName("sdc-add-artifact");
    }

    public static String getSelectedInstanceName() {
        return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanel.COMPONENT_TITLE.getValue()).getText();
    }

    public static void showInformationArtifactTab() throws Exception {
        clickOnTab(DataTestIdEnum.CompositionScreenEnum.INFORMATION_ARTIFACTS);
    }

    public static void showAPIArtifactTab() throws Exception {
        clickOnTab(DataTestIdEnum.CompositionScreenEnum.API);
    }

    public static void showInformationTab() throws Exception {
        clickOnTab(DataTestIdEnum.CompositionScreenEnum.INFORMATION);
    }

    public static void showCompositionTab() throws Exception {
        clickOnTab(DataTestIdEnum.CompositionScreenEnum.COMPOSITION);
    }

    public static void showInputsTab() throws Exception {
        clickOnTab(DataTestIdEnum.CompositionScreenEnum.INPUTS);
    }

    public static void showRequirementsAndCapabilitiesTab() throws Exception {
        clickOnTab(DataTestIdEnum.CompositionScreenEnum.REQUIREMENTS_AND_CAPABILITIES);
    }

    public static List<WebElement> getOpenTabTitle() throws Exception {
        return GeneralUIUtils.getElementsByCSS("expand-collapse ng-transclude");
    }

    public static void clickOnTab(DataTestIdEnum.CompositionScreenEnum tabSelector) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", tabSelector.name()));
        GeneralUIUtils.getElementsByCSS(tabSelector.getValue()).get(0).click();
        GeneralUIUtils.ultimateWait();
    }

    public static void clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum tabSelector) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", tabSelector.name()));
        GeneralUIUtils.getWebElementByTestID(tabSelector.getValue()).click();
        GeneralUIUtils.ultimateWait();
    }

    public static CanvasElement addElementToCanvasScreen(LeftPanelCanvasItems elementName, CanvasManager vfCanvasManager) throws Exception {
        CompositionPage.searchForElement(elementName.name());
        return vfCanvasManager.createElementOnCanvas(elementName);
    }

    public static CanvasElement addElementToCanvasScreen(String elementName, CanvasManager vfCanvasManager) throws Exception {
        CompositionPage.searchForElement(elementName);
        return vfCanvasManager.createElementOnCanvas(elementName);
    }

    public static List<WebElement> getCompositionDeplymentArtifacts() {
        return GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue());
    }

    public static WebElement getCustomizationUUID() throws Exception {
        return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.CUSTOMIZATION_UUID.getValue());
    }


    public static List<WebElement> getCompositionEnvArtifacts() {
        return GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ENV.getValue());
    }

    public static WebElement clickDownloadEnvArtifactComposition(String fileName) {
        GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ENV.getValue() + fileName);
        return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue() + fileName);
    }

    public static void setSingleProperty(String propertyDataTestID, String propertyValue) {
        WebElement findElement = GeneralUIUtils.getWebElementByTestID(propertyDataTestID);
        findElement.click();
        PropertiesPage.getPropertyPopup().insertPropertyDefaultValue(propertyValue);
        PropertiesPage.getPropertyPopup().clickSave();
        GeneralUIUtils.ultimateWait();
        findElement = GeneralUIUtils.getWebElementByTestID("value_" + propertyDataTestID);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating property %s is set", propertyValue));
        AssertJUnit.assertTrue(findElement.getText().equals(propertyValue));
        GeneralUIUtils.ultimateWait();
    }


}
