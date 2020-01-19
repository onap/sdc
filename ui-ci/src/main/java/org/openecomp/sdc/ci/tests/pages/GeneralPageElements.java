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

package org.openecomp.sdc.ci.tests.pages;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;

import org.openecomp.sdc.ci.tests.utilities.CatalogUIUtilitis;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Supplier;

public class GeneralPageElements {

    private static final int WAIT_FOR_ELEMENT_TIME_OUT = 60;
    private static final int WAIT_FOR_ELEMENT_TIME_OUT_DIVIDER = 10;

    protected GeneralPageElements() {

    }

    public static ResourceLeftMenu getLeftMenu() {
        return new ResourceLeftMenu();
    }

    public static void clickOKButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the OK button");
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
    }

    public static void clickCreateButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the CREATE/UPDATE button.");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
        ExtentTestActions.log(Status.INFO, "Succeeded.");
    }

    public static void clickCreateButton(int timeout) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the CREATE/UPDATE button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue(), timeout);
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
    }

    public static void clickCreateUpdateButton(int timeout) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the CREATE/UPDATE button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue(), timeout);
        GeneralUIUtils.ultimateWait();
    }

    public static void clickUpdateButton() {
        clickCreateButton();
    }

    public static void clickCheckinButton(String componentName) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the CHECKIN button");
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.ACCEPT_TESTING_MESSAGE.getValue()).sendKeys("Checkin " + componentName);
        clickOKButton();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue());
        GeneralUIUtils.ultimateWait();
    }

    public static void clickSubmitForTestingButton(String componentName) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the submitting for testing button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.SUBMIT_FOR_TESTING_BUTTON.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.SUMBIT_FOR_TESTING_MESSAGE.getValue()).sendKeys("Submit for testing " + componentName);
        GeneralUIUtils.ultimateWait();
        clickOKButton();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue(), WAIT_FOR_ELEMENT_TIME_OUT);
        GeneralUIUtils.ultimateWait();
    }

    public static void clickSubmitForTestingButtonErrorCase(String componentName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the submitting for testing button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.SUBMIT_FOR_TESTING_BUTTON.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.SUMBIT_FOR_TESTING_MESSAGE.getValue()).sendKeys("Submit for testing " + componentName);
        GeneralUIUtils.ultimateWait();
        clickOKButton();
        clickOKButton();
    }

    public static void restoreComponentFromArchivedCatalog(String componentName) throws Exception {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CatalogSwitchButtons.CATALOG_SWITCH_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CatalogSwitchButtons.CATALOG_ARCHIVE.getValue());
        GeneralUIUtils.findComponentAndClick(componentName);
        clickRestoreButton(componentName);
    }

    public static void restoreComponentFromElementPage(String componentName) throws Exception {
        clickRestoreButton(componentName);
    }

    private static void clickRestoreButton(String componentName) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Going to restore component: %s", componentName));
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.RESTORE_BUTTON.getValue());
    }

    public static void clickArchivedButtonFromCatalog(String componentName) throws Exception {
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(componentName);
        clickArchiveButton(componentName);
    }

    public static void archiveComponentFromElementPage(String componentName) throws Exception {
        clickArchiveButton(componentName);
    }

    private static void clickArchiveButton(String componentName) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Going to archive component: %s", componentName));
        GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.GeneralElementsEnum.ARCHIVE_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.ARCHIVE_BUTTON.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Component %s archived successfully", componentName));
    }

    //TODO should implement real code
    public static void clickCertifyButton(String componentName) throws Exception {
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on certify button");
            GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.LifeCyleChangeButtons.CERTIFY.getValue());
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.APPROVE_MESSAGE.getValue())
                    .sendKeys("resource " + componentName + " certified successfully");
            clickOKButton();
            GeneralUIUtils.ultimateWait();
            HomePage.navigateToHomePage();
            GeneralUIUtils.ultimateWait();
            HomePage.navigateToHomePage();
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue(), GeneralUIUtils.getTimeOut() / WAIT_FOR_ELEMENT_TIME_OUT_DIVIDER);
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            throw new Exception("Certification of " + componentName + " failed");
        }
    }

    public static void clickCertifyButtonNoUpgradePopupDismiss(String componentName) throws Exception {
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on certify button");
            GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.LifeCyleChangeButtons.CERTIFY.getValue());
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.APPROVE_MESSAGE.getValue())
                    .sendKeys("resource " + componentName + " certified successfully");
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.OK.getValue()).click();
        } catch (Exception e) {
            throw new Exception("Certification of " + componentName + " failed");
        }
    }

    public static void clickCertifyButtonNoUpgradePopupDismissErrorCase(String componentName) throws Exception {
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on certify button");
            GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.LifeCyleChangeButtons.CERTIFY.getValue());
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.APPROVE_MESSAGE.getValue())
                    .sendKeys("resource " + componentName + " certified successfully");
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.OK.getValue()).click();
            clickOKButton();
        } catch (Exception e) {
            throw new Exception("Certification of " + componentName + " failed");
        }
    }

    public static void clickUpgradeServicesCloseButton() {
        WebElement closeButton = null;
        try {
            closeButton = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.UPGRADE_SERVICES_CANCEL.getValue(), GeneralUIUtils.getTimeOut() / WAIT_FOR_ELEMENT_TIME_OUT_DIVIDER);
            UpgradeServicesPopup.setUpgradePopupShown(true);
            SetupCDTest.getExtendTest().log(Status.INFO, "Closing Update Services popup with X button ...");
            closeButton.click();
        } catch (WebDriverException e) {
            // regular flow
            UpgradeServicesPopup.setUpgradePopupShown(false);
            SetupCDTest.getExtendTest().log(Status.INFO, "Update Services popup is not shown, continuing ...");
        }
    }

    public static void clickCheckoutButton() throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on CHECKOUT button ...");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CHECKOUT_BUTTON.getValue());
        GeneralUIUtils.ultimateWait();
    }


    public static void clickDeleteVersionButton() throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on DELETE VERSION button ...");
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.DELETE_VERSION_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.OK.getValue());
    }

    public static void clickRevertButton() throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on REVERT button ...");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.REVERT_BUTTON.getValue());
    }

    public static String getLifeCycleState() {
        return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue()).getText();
    }

    public static String getWebElementTextByTestId(String dataTestIdEnumValue) {
        return GeneralUIUtils.getWebElementByTestID(dataTestIdEnumValue).getText();
    }

    public static void selectVersion(String version) {
        GeneralUIUtils.getSelectList(version, DataTestIdEnum.GeneralElementsEnum.VERSION_HEADER.getValue());
        GeneralUIUtils.ultimateWait();
    }

    public static List<WebElement> getElementsFromTable() {
        GeneralUIUtils.ultimateWait();
        return GeneralUIUtils.getElementsByLocator(By.className("datatable-body"));
    }

    public static boolean checkElementsCountInTable(int expectedElementsCount) {
        return checkElementsCountInTable(expectedElementsCount, () -> getElementsFromTable());
    }


    public static void clickTrashButtonAndConfirm() throws InterruptedException {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on TRASH button ...");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.DELETE_VERSION_BUTTON.getValue());
        clickOKButton();
    }

    public static void clickBrowseButton() throws InterruptedException {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Browse button ...");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.ModalItems.BROWSE_BUTTON.getValue());
    }

    public static boolean checkElementsCountInTable(int expectedElementsCount, Supplier<List<WebElement>> func) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Checking the number of elements in the table; should be " + (expectedElementsCount - 1));
        GeneralUIUtils.ultimateWait();
        return true;
    }

    public static void clickDeleteFile() throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on delete file X-button ...");
        GeneralUIUtils.clickOnAreaJS(GeneralUIUtils.getWebElementBy(By.cssSelector("div[class='i-sdc-form-file-upload-x-btn']")));
        GeneralUIUtils.ultimateWait();
    }

    public static void clickOnHomeButton() {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
    }

}
