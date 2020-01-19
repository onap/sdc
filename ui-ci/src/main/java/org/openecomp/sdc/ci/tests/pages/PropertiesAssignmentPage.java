/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.PropertiesAssignmentScreen;
import org.openecomp.sdc.ci.tests.exception.PropertiesAssignmentPageException;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesAssignmentPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesAssignmentPage.class);

    private PropertiesAssignmentPage() {

    }

    public static void clickOnInputTab() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Input Tab");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.INPUTS_TAB.getValue());
    }

    public static void clickOnPropertiesTab() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Properties Tab");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.PROPERTIES_TAB.getValue());
    }

    public static void clickOnCompositionRightTab() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Composition Right Tab");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.COMPOSITION_TAB.getValue());
    }

    public static void clickOnPropertyStructureRightTab() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Property Structure Right Tab");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.PROPERTY_STRUCTURE_TAB.getValue());
    }

    public static void clickOnDeclareButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Declare Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.DECLARE_BUTTON.getValue());
    }

    public static void clickOnSaveButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Save Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.SAVE_BUTTON.getValue());
    }

    public static void clickOnDiscardButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Discard Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.DISCARD_BUTTON.getValue());
    }

    public static void clickOnDialogSaveButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Save Button in Dialog Popup");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.SAVE_DIALOG_SAVE.getValue());
    }

    public static void clickOnDialogDiscardButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Discard Button in Dialog Popup");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.SAVE_DIALOG_DISCARD.getValue());
    }

    public static void clickOnDialogCancelButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Cancel Button in Dialog Popup");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.SAVE_DIALOG_CANCEL.getValue());
    }

    public static void clickOnEditButton(String propertyName) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Edit button ");
        GeneralUIUtils.clickOnElementByTestId(PropertyNameBuilder.buildPopupField(propertyName));
    }

    public static void clickOnSetButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Set button in a property popup ");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_SET_BUTTON.getValue());
    }

    public static void clickOnExpandButton(String propertyName, int index) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Expand Complex Property Button");
        GeneralUIUtils.clickOnElementByTestId(PropertyNameBuilder.buildIExpandButton(propertyName, index));
    }

    public static void findSearchBoxAndClick(final String resourceName) throws PropertiesAssignmentPageException {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " in Properties");
        try {
            final WebElement searchTextbox = GeneralUIUtils
                .getWebElementByTestID(DataTestIdEnum.PropertiesAssignmentScreen.SEARCH_BOX.getValue());
            searchTextbox.clear();
            searchTextbox.sendKeys(resourceName);
            GeneralUIUtils.ultimateWait();
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not interact with search bar, test id '%s'",
                PropertiesAssignmentScreen.SEARCH_BOX.getValue());
            LOGGER.error(errorMsg, e);
            SetupCDTest.getExtendTest().log(Status.ERROR, errorMsg);
            throw new PropertiesAssignmentPageException(errorMsg, e);
        }

        try {
            SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on the %s component in Properties", resourceName));
            GeneralUIUtils.clickOnElementByInputTestIdWithoutWait(resourceName);
            GeneralUIUtils.ultimateWait();
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not click on component named '%s'", resourceName);
            LOGGER.error(errorMsg, e);
            SetupCDTest.getExtendTest().log(Status.ERROR, errorMsg);
            throw new PropertiesAssignmentPageException(errorMsg, e);
        }
    }

    private static void clickOnProperty(String propertyName) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on the %s component in Properties", propertyName));
        GeneralUIUtils.clickOnElementByTestId(propertyName);
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
    }

    public static void clickOnAddValueToList(String propertyName) throws Exception {
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on the Add To List button of %s component in Properties", propertyName));
            GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.ADD_TO_LIST_BUTTON.getValue() + propertyName);
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Can't click on Add To List button of component named " + propertyName);
            e.printStackTrace();
        }
    }

    public static void clickODeleteValueFromList(String propertyName, int index) throws Exception {
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on the Delete From List button of %s component in Properties", propertyName));
            GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.DELETE_FROM_LIST_BUTTON.getValue() + propertyName + "." + index);
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Can't click on Delete From List button of component named " + propertyName);
            e.printStackTrace();
        }
    }

    public static void clickOnDeleteInputButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Delete Input Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.INPUT_DELETE_BUTTON.getValue());
    }

    public static void clickOnDeleteInputDialogConfirmationButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Delete Input Dialog Confirmation Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_DELETE.getValue());
    }

    public static void clickOnComponentInComposition(final String resourceName) throws Exception {
        SetupCDTest.getExtendTest()
            .log(Status.INFO, String.format("Clicking on the %s component on Properties screen", resourceName));
        try {
            GeneralUIUtils.clickOnElementByTestId(resourceName);
        } catch (final Exception e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Can't click on component named " + resourceName);
            throw e;
        }
    }

    public static void findInput(String componentName, String resourceName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + componentName + "_" + resourceName + " on Inputs screen");
        WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesAssignmentScreen.SEARCH_BOX.getValue());
        String searchPattern = componentName + "_" + resourceName;

        searchTextbox.clear();
        searchTextbox.sendKeys(searchPattern);
        GeneralUIUtils.ultimateWait();
    }

    public static void findProperty(String resourceName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " property on Properties screen");
        WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesAssignmentScreen.SEARCH_BOX.getValue());

        searchTextbox.clear();
        searchTextbox.sendKeys(resourceName);
        GeneralUIUtils.ultimateWait();
    }

    public static void editPropertyValue(String resourceName, String value) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " property on Properties screen");
        WebElement valueTextbox = GeneralUIUtils.getWebElementByTestID(resourceName);

        clickOnProperty(resourceName);
        valueTextbox.clear();
        SetupCDTest.getExtendTest().log(Status.INFO, "Editing " + resourceName + " property on Properties screen");
        valueTextbox.sendKeys(value);
        GeneralUIUtils.ultimateWait();

    }

    public static void deletePropertyValue(String resourceName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " property on Properties screen");
        WebElement valueTextbox = GeneralUIUtils.getWebElementByTestID(resourceName);

        clickOnProperty(resourceName);
        SetupCDTest.getExtendTest().log(Status.INFO, "Deleting " + resourceName + " property on Properties screen");
        valueTextbox.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        valueTextbox.sendKeys(Keys.BACK_SPACE);
        GeneralUIUtils.ultimateWait();

    }


    public static Boolean isPropertyChecked(String resourceName) {
//		TODO add better implementation for css string
        GeneralUIUtils.ultimateWait();
        Boolean isDisabled = GeneralUIUtils.checkForDisabledAttributeInHiddenElement("checkbox[data-tests-id='" + resourceName + "'] input");
        SetupCDTest.getExtendTest().log(Status.INFO, "Is property checkbox disabled? " + isDisabled);
        return isDisabled;
    }

    public static boolean isButtonDisabled(String dataTestId) {
        return GeneralUIUtils.checkForDisabledAttribute(dataTestId);
    }

    public static String selectBooleanPropertyValue(String propertyName, String propertyValue) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + propertyName + " property on Properties screen");
        String actualPropertyValue = null;
        try {
            actualPropertyValue = GeneralUIUtils.getSelectedElementFromDropDown(propertyName).getText();
        } catch (NoSuchElementException e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "#selectBooleanPropertyValue - Failed to get selected boolean property value ");
            SetupCDTest.getExtendTest().log(Status.INFO, e);
        }
        try {
            GeneralUIUtils.getSelectList(propertyValue, propertyName);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return actualPropertyValue;
    }


    //Filter Actions
    public static void clickOnFilterButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter button ");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_BUTTON.getValue());
    }

    public static void clickOnFilterAllCheckbox() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter All Checkbox ");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_CHECKBOX_ALL.getValue());
    }

    public static void clickOnFilterCPCheckbox() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter CP Checkbox ");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_CHECKBOX_CP.getValue());
    }

    public static void clickOnFilterVfcCheckbox() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter VFC Checkbox ");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_CHECKBOX_VFC.getValue());
    }

    public static void clickOnFilterVlCheckbox() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter VL Checkbox ");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_CHECKBOX_VL.getValue());
    }

    public static void clickOnFilterApplyButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter Apply Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_APPLY_BUTTON.getValue());
    }

    public static void clickOnFilterCloseButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter Close Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_CLOSE_BUTTON.getValue());
    }

    public static void clickOnFilterClearAllButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on Filter Clear All Button");
        GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.CLEAR_FILTER_BUTTON.getValue());
    }

    public static void findFilterBoxAndClick(String resourceName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " property in Properties table");
        WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesAssignmentScreen.FILTER_BOX.getValue());
        try {
            searchTextbox.clear();
            searchTextbox.sendKeys(resourceName);
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Can't interact with search bar");
            e.printStackTrace();
        }

    }


}
