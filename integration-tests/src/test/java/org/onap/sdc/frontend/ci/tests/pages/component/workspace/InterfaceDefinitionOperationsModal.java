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
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionOperationsModal.InterfaceOperationsData.InputData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils.waitForLoader;

/**
 * Represents the Composition Interface Operations Modal.
 */
public class InterfaceDefinitionOperationsModal extends AbstractPageObject {

    private InterfaceOperationInputListComponent inputListComponent;
    private InterfaceOperationAddInputComponent addInputComponent;

    public InterfaceDefinitionOperationsModal(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        isLoaded(false);
    }

    public void isLoaded(boolean isInViewMode) {
        waitForElementVisibility(By.xpath(XpathSelector.TITLE_SPAN.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.INTERFACE_NAME_LABEL.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.OPERATION_NAME_LABEL.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.SAVE_BTN.getXPath()));
        waitToBeClickable(By.xpath(XpathSelector.CANCEL_BTN.getXPath()));
        this.inputListComponent = new InterfaceOperationInputListComponent(webDriver);
        this.inputListComponent.isLoaded();
        if (!isInViewMode) {
            this.addInputComponent = new InterfaceOperationAddInputComponent(webDriver);
            this.addInputComponent.isLoaded();
        }
    }

    public void isUnloaded() {
        waitForElementInvisibility(By.xpath(XpathSelector.TITLE_SPAN.getXPath()));
        waitForElementInvisibility(By.xpath(XpathSelector.INTERFACE_NAME_LABEL.getXPath()));
        waitForElementInvisibility(By.xpath(XpathSelector.OPERATION_NAME_LABEL.getXPath()));
        waitForElementInvisibility(By.xpath(XpathSelector.SAVE_BTN.getXPath()));
        waitForElementInvisibility(By.xpath(XpathSelector.CANCEL_BTN.getXPath()));
    }

    private void clickOnSave() {
        waitToBeClickable(By.xpath(XpathSelector.SAVE_BTN.getXPath())).click();
    }

    public void clickOnCancel() {
        waitToBeClickable(By.xpath(XpathSelector.CANCEL_BTN.getXPath())).click();
    }

    public void clickOnDelete() {
        waitToBeClickable(By.xpath(XpathSelector.DELETE_BTN.getXPath())).click();
    }

    public void deleteInput(String inputName) {
        inputListComponent.loadInputList();
        inputListComponent.deleteInput(inputName);
    }

    public void updateInterfaceOperation(final InterfaceOperationsData interfaceOperationsData) {
        fillDescription(interfaceOperationsData.getDescription());
        fillImplementationName(interfaceOperationsData.getImplementationName());
        interfaceOperationsData.getInputList().forEach(inputData -> {
            final InterfaceOperationAddInputComponent addInputComponent = new InterfaceOperationAddInputComponent(webDriver);
            addInputComponent.isLoaded();
            addInputComponent.clickOnAddInputLink();
            addInputComponent.fillInput(inputData);
            addInputComponent.clickOnAddButton();
            ExtentTestActions.takeScreenshot(Status.INFO,
                "compositionInterfaceOperationsModal.addInput." + inputData.getName(),
                String.format("Input '%s' added", inputData.getName())
            );
            addInputComponent.fillValue(inputData);
        });
        clickOnSave();
        waitForLoader();
    }

    private void fillDescription(final String description) {
        setInputField(By.xpath(XpathSelector.INTERFACE_OPERATION_DESCRIPTION_INPUT.getXPath()), description);
    }

    private void fillImplementationName(final String implementationName) {
        setInputField(By.xpath(XpathSelector.INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT.getXPath()), implementationName);
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

    public void clickOnAddInput() {
        addInputComponent.clickOnAddInputLink();
    }

    public String getDescription() {
        return findElement(By.xpath(XpathSelector.INTERFACE_OPERATION_DESCRIPTION_INPUT.getXPath())).getAttribute("value");
    }

    public String getImplementationName() {
        return findElement(By.xpath(XpathSelector.INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT.getXPath())).getAttribute("value");
    }

    public List<InputData> getInputs() {
        inputListComponent.loadInputList();
        return inputListComponent.getInputList();
    }

    @AllArgsConstructor
    private enum XpathSelector {
        TITLE_SPAN("//span[@class='title' and contains(text(), 'Edit Operation')]"),
        DELETE_BTN("//svg-icon[@name='trash-o']"),
        SAVE_BTN("//button[@data-tests-id='Save']"),
        CANCEL_BTN("//button[@data-tests-id='Cancel']"),
        INTERFACE_NAME_LABEL("//label[contains(@class,'sdc-input') and contains(text(), 'Interface Name')]"),
        OPERATION_NAME_LABEL("//label[contains(@class,'sdc-input') and contains(text(), 'Operation Name')]"),
        INTERFACE_OPERATION_DESCRIPTION_INPUT("//input[@data-tests-id='interface-operation-description']"),
        INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT("//input[@data-tests-id='interface-operation-implementation-name']");

        private final String xPath;

        public String getXPath(final String... xpathParams) {
            return String.format(xPath, xpathParams);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class InterfaceOperationsData {

        private final String description;
        private final String implementationName;
        private final List<InputData> inputList;

        @Getter
        @AllArgsConstructor
        public static class InputData {

            private final String name;
            private final String type;
            private final Object value;
        }
    }
}
