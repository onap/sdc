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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.CreateAndImportButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.Dashboard;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.List;
import java.util.Random;

public final class ResourceUIUtils {
    public static final String RESOURCE_NAME_PREFIX = "ResourceCDTest-";
    protected static final boolean IS_BEFORE_TEST = true;
    public static final String INITIAL_VERSION = "0.1";
    public static final String ICON_RESOURCE_NAME = "call_controll";
    protected static final String UPDATED_RESOURCE_ICON_NAME = "objectStorage";
    private static final int BASIC_TIMEOUT = 10 * 60;

    private ResourceUIUtils() {
    }

    private static WebDriver driver = GeneralUIUtils.getDriver();

    // click and upload tosca file //**to be changed.
    public static void importFileWithSendKey(String filePath, String fileName, CreateAndImportButtonsEnum type)
            throws Exception {
        WebElement importButton = HomeUtils.createAndImportButtons(type, driver).findElement(By.tagName("input"));
        importButton.sendKeys(filePath + fileName);
    }

    public static String defineUserId(String userId) {
        //
        WebElement resourceUserIdTextbox = ResourceGeneralPage.getContactIdField();
        resourceUserIdTextbox.clear();
        resourceUserIdTextbox.sendKeys(userId);
        return userId;
    }

    static String definePropertyName(String name) {

        WebElement nameProperty = GeneralUIUtils.getDriver().findElement(By.name("propertyName"));
        nameProperty.sendKeys(name);
        return name;
    }

    public static void selectRandomResourceIcon() throws Exception {
        final int webDriverWaitingTimeout = 4;
        GeneralUIUtils.moveToStep(StepsEnum.ICON);
        WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), webDriverWaitingTimeout);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@data-tests-id, 'iconBox')]")));
        List<WebElement> iconElement = GeneralUIUtils.getDriver()
                .findElements(By.xpath("//*[contains(@data-tests-id, 'iconBox')]"));
        iconElement.get(0).click();
    }

    static void defineDefaultValueByType(String value) {

        WebElement valueString = GeneralUIUtils.getDriver().findElement(By.name("value"));
        valueString.clear();
        valueString.sendKeys(value);
    }

    static void defineBoolenDefaultValue(String value) {

        WebElement elementBoolean = GeneralUIUtils.getDriver().findElement(By.name("value"));
        Select se = new Select(elementBoolean);
        se.selectByValue(value);
    }

    public static void fillResourceGeneralInformationPage(ResourceReqDetails resource, User user, boolean isNewResource) {
        try {
            ResourceGeneralPage.defineName(resource.getName());
            ResourceGeneralPage.defineDescription(resource.getDescription());
            ResourceGeneralPage.defineCategory(resource.getCategories().get(0).getSubcategories().get(0).getName());
            ResourceGeneralPage.defineVendorName(resource.getVendorName());
            ResourceGeneralPage.defineVendorRelease(resource.getVendorRelease());
            if (isNewResource) {
                ResourceGeneralPage.defineTagsList(resource, new String[]{"This-is-tag", "another-tag", "Test-automation-tag"});
            } else {
                ResourceGeneralPage.defineTagsList(resource, new String[]{"one-more-tag"});
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillMaxValueResourceGeneralInformationPage(ResourceReqDetails resource) {
        final int stringPatternLength = 5000;
        String stringPattern = "ABCDabcd123456";
        GeneralUIUtils.addStringtoClipboard(buildStringFromPattern(stringPattern, stringPatternLength));
        ResourceGeneralPage.defineNameWithPaste();
        ResourceGeneralPage.defineDescriptionWithPaste();
        ResourceGeneralPage.defineVendorNameWithPaste();
        ResourceGeneralPage.defineVendorReleaseWithPaste();
        ResourceGeneralPage.defineTagsListWithPaste();
        GeneralUIUtils.waitForAngular();
    }

    public static String buildStringFromPattern(String stringPattern, int stringLength) {
        char[] chars = stringPattern.toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < stringLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * @deprecated Use {@link #createVF(ResourceReqDetails, User)} instead
     */
    public static void createResource(ResourceReqDetails resource, User user) throws Exception {
        createVF(resource, user);
    }

    public static void createVF(ResourceReqDetails resource, User user) {
        ExtentTestActions.log(Status.INFO, "Going to create a new VF.");
        createResource(resource, user, DataTestIdEnum.Dashboard.BUTTON_ADD_VF);
    }

    private static void createResource(ResourceReqDetails resource, User user, DataTestIdEnum.Dashboard button) {
        WebElement addVFButton;
        try {
            GeneralUIUtils.ultimateWait();
            try {
                GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
                addVFButton = GeneralUIUtils.getWebElementByTestID(button.getValue());
            } catch (Exception e) {
                File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resource.getName());
                final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
                SetupCDTest.getExtendTest().log(Status.WARNING, "Add button is not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
                showButtonsADD();
                addVFButton = GeneralUIUtils.getWebElementByTestID(button.getValue());
            }
            addVFButton.click();
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on ADD button, retrying ... "));
            GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
            GeneralUIUtils.ultimateWait();
            GeneralUIUtils.getWebElementByTestID(button.getValue()).click();
            GeneralUIUtils.ultimateWait();
        }
        fillResourceGeneralInformationPage(resource, user, true);
        resource.setVersion("0.1");
        GeneralPageElements.clickCreateButton();
    }

    public static void updateResource(ResourceReqDetails resource, User user) {
        ResourceGeneralPage.defineContactId(resource.getContactId());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating General screen fields ..."));
        fillResourceGeneralInformationPage(resource, user, false);
        ResourceGeneralPage.clickUpdateButton();
    }

    /**
     * Click on HTML element.
     *
     * @param dataTestId
     * @throws Exception
     */
    public static void getWebElementByTestID(String dataTestId) throws Exception {
        final int webDriverWaitingTimeout = 20;
        WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), webDriverWaitingTimeout);
        WebElement element = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
        element.click();
        // wait.until(ExpectedConditions.elemetto)
        // WebElement serviceButton =
        // GeneralUIUtils.getDriver().findElement(By.xpath("//*[@data-tests-id='"
        // + dataTestId + "']"));
        // serviceButton.
        // serviceButton.click();
    }

    /**
     * Import VFC
     *
     * @param user
     * @param filePath
     * @param fileName
     * @return
     * @throws Exception
     */

    public static void importVfc(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
            throws Exception {
        GeneralUIUtils.ultimateWait();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating new VFC resource ", resourceMetaData.getName()));
        GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
        GeneralUIUtils.ultimateWait();
        // Insert file to the browse dialog
        WebElement buttonVFC = GeneralUIUtils.findByText("Import VFC");
        WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VFC_FILE.getValue());
        if (!buttonVFC.isDisplayed()) {
            File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
            final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
            SetupCDTest.getExtendTest().log(Status.WARNING, "VFC button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
        }
        try {
            fileInputElement.sendKeys(filePath + fileName);
        } catch (ElementNotVisibleException e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting VFC file input to visible"));
            showButtons();
            fileInputElement.sendKeys(filePath + fileName);
        }
        // Fill the general page fields.
        GeneralUIUtils.ultimateWait();
        fillResourceGeneralInformationPage(resourceMetaData, user, true);
        GeneralPageElements.clickCreateButton();
    }

    public static void importVfcNoCreate(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
            throws Exception {
        GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
        // Insert file to the browse dialog
        WebElement buttonVFC = GeneralUIUtils.findByText("Import VFC");
        WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VFC_FILE.getValue());
        if (!buttonVFC.isDisplayed()) {
            File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
            final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
            SetupCDTest.getExtendTest().log(Status.WARNING, "VFC button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
        }
        try {
            fileInputElement.sendKeys(filePath + fileName);
        } catch (ElementNotVisibleException e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting VFC file input to visible"));
            showButtons();
            fileInputElement.sendKeys(filePath + fileName);
        }
        // Fill the general page fields.
        GeneralUIUtils.waitForLoader();
        fillResourceGeneralInformationPage(resourceMetaData, user, true);
    }


    public static void importVfFromCsar(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
            throws Exception {
        GeneralUIUtils.ultimateWait();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating new VF asset resource %s", resourceMetaData.getName()));
        GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
        GeneralUIUtils.ultimateWait();
        // Insert file to the browse dialog
        WebElement buttonDCAE = GeneralUIUtils.findByText("Import DCAE asset");
        WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VF_FILE.getValue());
        if (!buttonDCAE.isDisplayed()) {
            File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
            final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
            SetupCDTest.getExtendTest().log(Status.WARNING, "DCAE button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
        }
        try {
            fileInputElement.sendKeys(filePath + fileName);
        } catch (ElementNotVisibleException e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting DCAE file input to visible"));
            showButtons();
            fileInputElement.sendKeys(filePath + fileName);
        }
        // Fill the general page fields.
        GeneralUIUtils.ultimateWait();
        fillResourceGeneralInformationPage(resourceMetaData, user, true);
        GeneralPageElements.clickCreateButton(BASIC_TIMEOUT);
        //GeneralUIUtils.ultimateWait(); "don't change import of csar can take longer then 3 minutes"
        GeneralUIUtils.waitForLoader(BASIC_TIMEOUT);
    }

    public static void importVfFromCsarNoCreate(ResourceReqDetails resourceMetaData, String filePath, String fileName, User user)
            throws Exception {
        GeneralUIUtils.ultimateWait();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating new VF asset resource %s, Create button will not be clicked", resourceMetaData.getName()));
        GeneralUIUtils.hoverOnAreaByTestId(Dashboard.IMPORT_AREA.getValue());
        GeneralUIUtils.ultimateWait();
        // Insert file to the browse dialog
        WebElement buttonDCAE = GeneralUIUtils.findByText("Import DCAE asset");
        WebElement fileInputElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VF_FILE.getValue());
        if (!buttonDCAE.isDisplayed()) {
            File imageFilePath = GeneralUIUtils.takeScreenshot(null, SetupCDTest.getScreenshotFolder(), "Warning_" + resourceMetaData.getName());
            final String absolutePath = new File(SetupCDTest.getReportFolder()).toURI().relativize(imageFilePath.toURI()).getPath();
            SetupCDTest.getExtendTest().log(Status.WARNING, "DCAE button not visible after hover on import area of Home page, moving on ..." + SetupCDTest.getExtendTest().addScreenCaptureFromPath(absolutePath));
        }
        try {
            fileInputElement.sendKeys(filePath + fileName);
        } catch (ElementNotVisibleException e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exeption catched on file input, converting DCAE file input to visible"));
            showButtons();
            fileInputElement.sendKeys(filePath + fileName);
        }
        // Fill the general page fields.
        GeneralUIUtils.ultimateWait();
        fillResourceGeneralInformationPage(resourceMetaData, user, true);
        GeneralUIUtils.waitForLoader(BASIC_TIMEOUT);
    }

    public static void updateVfWithCsar(String filePath, String fileName) {
        ExtentTestActions.log(Status.INFO, "Updating VF with updated CSAR file named " + fileName);
        WebElement browseWebElement = GeneralUIUtils.getInputElement(DataTestIdEnum.GeneralElementsEnum.UPLOAD_FILE_INPUT.getValue());
        browseWebElement.sendKeys(filePath + fileName);
        GeneralUIUtils.ultimateWait();
        GeneralPageElements.clickUpdateButton();
        GeneralUIUtils.waitForLoader();
        ExtentTestActions.log(Status.INFO, "VF is updated.");
    }

    private static void showButtons() {
        String parentElementClassAttribute = "sdc-dashboard-import-element-container";
        WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
        GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
        GeneralUIUtils.ultimateWait();
        SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Input buttons now visible..."));
    }

    private static void showButtonsADD() {
        try {
            GeneralUIUtils.ultimateWait();
            String parentElementClassAttribute = "sdc-dashboard-create-element-container";
            WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
            GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            GeneralUIUtils.ultimateWait();
            String parentElementClassAttribute = "sdc-dashboard-create-element-container";
            WebElement fileInputElementWithVisible = GeneralUIUtils.getDriver().findElement(By.className(parentElementClassAttribute));
            GeneralUIUtils.unhideElement(fileInputElementWithVisible, parentElementClassAttribute);
            GeneralUIUtils.ultimateWait();
        }
        SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Input buttons now visible..."));
    }

    public static void clickOnElementByText(String textToClick, String customizationFoLog) {
        String customizationFoLogLocal = customizationFoLog != null ? customizationFoLog : "";
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s %s", textToClick, customizationFoLogLocal));
        GeneralUIUtils.clickOnElementByText(textToClick);
    }

    public static void createPNF(ResourceReqDetails resource, User user) throws Exception {
        ExtentTestActions.log(Status.INFO, "Going to create a new PNF");
        createResource(resource, user, DataTestIdEnum.Dashboard.BUTTON_ADD_PNF);
    }

    public static void createCR(ResourceReqDetails resource, User user) throws Exception {
        ExtentTestActions.log(Status.INFO, "Going to create a new CR");
        createResource(resource, user, DataTestIdEnum.Dashboard.BUTTON_ADD_CR);
    }

    public static ImmutablePair<String, String> getFirstRIPos(ResourceReqDetails createResourceInUI, User user) {
        String responseAfterDrag = RestCDUtils.getResource(createResourceInUI, user).getResponse();
        JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
        String xPosPostDrag = (String) ((JSONObject) ((JSONArray) jsonResource.get("componentInstances")).get(0))
                .get("posX");
        String yPosPostDrag = (String) ((JSONObject) ((JSONArray) jsonResource.get("componentInstances")).get(0))
                .get("posY");
        return new ImmutablePair<String, String>(xPosPostDrag, yPosPostDrag);

    }


}
