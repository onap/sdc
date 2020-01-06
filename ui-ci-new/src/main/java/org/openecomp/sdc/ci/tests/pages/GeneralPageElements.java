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
import java.util.List;
import java.util.function.Supplier;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public final class GeneralPageElements {

    private static final int WAIT_FOR_ELEMENT_TIME_OUT = 60;
    private static final int WAIT_FOR_ELEMENT_TIME_OUT_DIVIDER = 10;

    private GeneralPageElements() {

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

    public static void clickSubmitForTestingButton(String componentName) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the submitting for testing button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.SUBMIT_FOR_TESTING_BUTTON.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.SUMBIT_FOR_TESTING_MESSAGE.getValue()).sendKeys("Submit for testing " + componentName);
        GeneralUIUtils.ultimateWait();
        clickOKButton();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue(), WAIT_FOR_ELEMENT_TIME_OUT);
        GeneralUIUtils.ultimateWait();
    }

    public static void restoreComponentFromElementPage(String componentName) {
        clickRestoreButton(componentName);
    }

    private static void clickRestoreButton(String componentName) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Going to restore component: %s", componentName));
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.RESTORE_BUTTON.getValue());
    }

    public static void clickCertifyButton(final String componentName) throws Exception {
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on certify button");
            GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.LifeCyleChangeButtons.CERTIFY.getValue());
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DistributionChangeButtons.APPROVE_MESSAGE.getValue())
                    .sendKeys("resource " + componentName + " certified successfully");
            clickOKButton();
            clickUpgradeServicesCloseButton();
            GeneralUIUtils.ultimateWait();
            HomePage.navigateToHomePage();
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue(), GeneralUIUtils.getTimeOut() / WAIT_FOR_ELEMENT_TIME_OUT_DIVIDER);
        } catch (final Exception e) {
            throw new Exception("Certification of " + componentName + " failed", e);
        }
    }

    public static void clickUpgradeServicesCloseButton() {
        try {
            final WebElement closeButton = GeneralUIUtils
                .getWebElementByTestID(DataTestIdEnum.ModalItems.UPGRADE_SERVICES_CANCEL.getValue(),
                    GeneralUIUtils.getTimeOut() / WAIT_FOR_ELEMENT_TIME_OUT_DIVIDER);
            SetupCDTest.getExtendTest().log(Status.INFO, "Closing Update Services popup with X button ...");
            closeButton.click();
        } catch (WebDriverException e) {
            // regular flow
            SetupCDTest.getExtendTest().log(Status.INFO, "Update Services popup is not shown, continuing ...");
        }
    }

    public static void clickCheckoutButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on CHECKOUT button ...");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CHECKOUT_BUTTON.getValue());
        GeneralUIUtils.ultimateWait();
    }


    public static String getLifeCycleState() {
        return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue()).getText();
    }

    public static List<WebElement> getElementsFromTable() {
        GeneralUIUtils.ultimateWait();
        return GeneralUIUtils.getElementsByLocator(By.className("flex-container"));
    }

    public static void checkElementsCountInTable(int expectedElementsCount, Supplier<List<WebElement>> func) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Checking the number of elements in the table; should be " + (expectedElementsCount - 1));
        GeneralUIUtils.ultimateWait();
    }

}
