/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
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

import lombok.AllArgsConstructor;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionOperationsModal.InterfaceOperationsData.InputData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Handles the add input inside the interface operation modal.
 *
 * @see "catalog-ui app-add-input component"
 */
public class InterfaceOperationAddInputComponent extends AbstractPageObject {

    public InterfaceOperationAddInputComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(XpathSelector.ADD_INPUT_LINK.getXPath());
    }

    /**
     * Clicks on the add input link, that opens the add input form.
     */
    public void clickOnAddInputLink() {
        waitToBeClickable(By.xpath(XpathSelector.ADD_INPUT_LINK.getXPath())).click();
        waitForElementVisibility(XpathSelector.NAME_INPUT.getXPath());
        waitForElementVisibility(XpathSelector.TYPE_INPUT.getXPath());
        waitForElementVisibility(XpathSelector.ADD_BTN.getXPath());
        waitForElementVisibility(XpathSelector.CANCEL_BTN.getXPath());
    }

    /**
     * Clicks on the add button that submits the input form.
     */
    public void clickOnAddButton() {
        waitToBeClickable(By.xpath(XpathSelector.ADD_BTN.getXPath())).click();
    }

    /**
     * Fills the input form fields with the given data.
     *
     * @param inputData the input information
     */
    public void fillInput(final InputData inputData) {
        fillName(inputData.getName());
        fillType(inputData.getType());
    }

    /**
     * Fills an input value, in the input list, based on the given input data.
     *
     * @param inputData the input information
     */
    public void fillValue(final InputData inputData) {
        var interfaceOperationInputListComponent = new InterfaceOperationInputListComponent(webDriver);
        interfaceOperationInputListComponent.isLoaded();
        interfaceOperationInputListComponent.fillInputValue(inputData.getName(), inputData.getValue());
    }

    /**
     * Fills the input name field.
     *
     * @param name the name to fill
     */
    public void fillName(final String name) {
        setInputValue(By.xpath(XpathSelector.NAME_INPUT.getXPath()), name);
    }

    /**
     * Fills the input type field.
     *
     * @param type the type to fill
     */
    public void fillType(final String type) {
        final WebElement inputElement = findElement(By.xpath(XpathSelector.TYPE_INPUT.getXPath()));
        inputElement.click();
        waitForElementVisibility(By.xpath(XpathSelector.DROPDOWN_RESULTS.getXPath()));
        inputElement.sendKeys(type);
        waitForElementVisibility(By.xpath(XpathSelector.DROPDOWN_OPTION.getXPath(type))).click();
    }

    private void setInputValue(final By locator, final String value) {
        if (value == null) {
            return;
        }

        final WebElement webElement = findElement(locator);
        webElement.clear();
        webElement.sendKeys(value);
    }

    @AllArgsConstructor
    private enum XpathSelector {
        ADD_INPUT_LINK("//a[@data-tests-id='add-input.add-input-link']"),
        NAME_INPUT("//input[@data-tests-id='add-input.input-name']"),
        TYPE_INPUT("//input[starts-with(@data-tests-id, 'add-input.input-type')]"),
        ADD_BTN("//button[@data-tests-id='add-input.add-input-btn']"),
        CANCEL_BTN("//button[@data-tests-id='add-input.cancel-btn']"),
        DROPDOWN_RESULTS("//dropdown-results"),
        DROPDOWN_OPTION("//li[@data-tests-id='%s']");

        private final String xPath;

        public String getXPath(final String... xpathParams) {
            return String.format(xPath, xpathParams);
        }
    }
}
