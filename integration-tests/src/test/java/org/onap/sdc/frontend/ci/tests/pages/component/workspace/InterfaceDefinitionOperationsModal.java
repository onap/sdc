/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import com.aventstack.extentreports.Status;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Represents the Composition Interface Operations Modal.
 */
public class InterfaceDefinitionOperationsModal extends AbstractPageObject {

    public InterfaceDefinitionOperationsModal(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.TITLE_SPAN.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.INTERFACE_NAME_LABEL.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.OPERATION_NAME_LABEL.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.INPUT_NAME_SPAN.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.INPUT_VALUE_SPAN.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.ADD_INPUT_BTN.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.SAVE_BTN.getXPath()));
        waitToBeClickable(By.xpath(XpathSelector.CANCEL_BTN.getXPath()));
    }

    public void clickOnSave() {
        waitToBeClickable(By.xpath(XpathSelector.SAVE_BTN.getXPath())).click();
    }

    public void clickOnCancel() {
        waitToBeClickable(By.xpath(XpathSelector.CANCEL_BTN.getXPath())).click();
    }

    public void clickOnDelete() {
        waitToBeClickable(By.xpath(XpathSelector.DELETE_BTN.getXPath())).click();
    }

    public void updateInterfaceOperation(final InterfaceOperationsData interfaceOperationsData) {
        fillDescription(interfaceOperationsData.getDescription());
        fillImplementationName(interfaceOperationsData.getImplementationName());
        fillInputName(interfaceOperationsData.getInputName());
        fillInputValue(interfaceOperationsData.getInputValue());
        clickOnSave();
        //there is no feedback from the UI to check if the update was successful. Forcing a wait time trying to guarantee that,
        // although time is never a guarantee in this case.
        new Actions(webDriver).pause(Duration.ofSeconds(5)).perform();
    }

    private void fillDescription(final String description) {
        setInputField(By.xpath(XpathSelector.INTERFACE_OPERATION_DESCRIPTION_INPUT.getXPath()), description);
    }

    private void fillImplementationName(final String implementationName) {
        setInputField(By.xpath(XpathSelector.INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT.getXPath()), implementationName);
    }

    private void fillInputName(final String inputName) {
        setInputField(By.xpath(XpathSelector.FIELD_INPUT_NAME_INPUT.getXPath()), inputName);
    }

    private void fillInputValue(final String inputValue) {
        setInputField(By.xpath(XpathSelector.FIELD_INPUT_VALUE_INPUT.getXPath()), inputValue);
    }

    private void setInputField(final By locator, final String value) {
        if (value == null) {
            return;
        }
        final WebElement webElement = findElement(locator);
        webElement.clear();
        webElement.sendKeys(value);
        ExtentTestActions.takeScreenshot(Status.INFO, value, value);
    }

    public void addInput() {
        waitToBeClickable(By.xpath(XpathSelector.ADD_INPUT_BTN.getXPath())).click();
    }

    public String getDescription() {
        return findElement(By.xpath(XpathSelector.INTERFACE_OPERATION_DESCRIPTION_INPUT.getXPath())).getAttribute("value");
    }

    public String getImplementationName() {
        return findElement(By.xpath(XpathSelector.INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT.getXPath())).getAttribute("value");
    }

    public String getInputName() {
        return findElement(By.xpath(XpathSelector.FIELD_INPUT_NAME_INPUT.getXPath())).getAttribute("value");
    }

    public String getInputValue() {
        return findElement(By.xpath(XpathSelector.FIELD_INPUT_VALUE_INPUT.getXPath())).getAttribute("value");
    }

    @Getter
    @AllArgsConstructor
    public static class InterfaceOperationsData {

        private final String description;
        private final String implementationName;
        private final String inputName;
        private final String inputValue;
    }

    @AllArgsConstructor
    private enum XpathSelector {
        TITLE_SPAN("//span[@class='title' and contains(text(), 'Edit Operation')]"),
        ADD_INPUT_BTN("//a[contains(@class,'add-param-link add-btn') and contains(text(), 'Add Input')]"),
        DELETE_BTN("//svg-icon[@name='trash-o']"),
        SAVE_BTN("//button[@data-tests-id='Save']"),
        CANCEL_BTN("//button[@data-tests-id='Cancel']"),
        INTERFACE_NAME_LABEL("//label[contains(@class,'sdc-input') and contains(text(), 'Interface Name')]"),
        OPERATION_NAME_LABEL("//label[contains(@class,'sdc-input') and contains(text(), 'Operation Name')]"),
        INTERFACE_OPERATION_DESCRIPTION_INPUT("//input[@data-tests-id='interface-operation-description']"),
        INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT("//input[@data-tests-id='interface-operation-implementation-name']"),
        INPUT_NAME_SPAN("//span[contains(@class,'field-input-name') and contains(text(), 'Name')]"),
        INPUT_VALUE_SPAN("//span[contains(@class,'field-input-value') and contains(text(), 'Value')]"),
        FIELD_INPUT_NAME_INPUT("//input[@data-tests-id='interface-operation-input-name']"),
        FIELD_INPUT_VALUE_INPUT("//input[@data-tests-id='interface-operation-input-value']");

        @Getter
        private final String xPath;

    }
}
