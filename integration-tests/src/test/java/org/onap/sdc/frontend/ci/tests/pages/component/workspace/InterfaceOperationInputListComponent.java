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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionOperationsModal.InterfaceOperationsData.InputData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Handles the input list inside the interface operation modal.
 *
 * @see "catalog-ui app-input-list and app-input-list-item ui components"
 */
public class InterfaceOperationInputListComponent extends AbstractPageObject {

    private WebElement wrappingElement;
    private List<WebElement> inputList = new ArrayList<>();

    public InterfaceOperationInputListComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        this.wrappingElement = waitForElementVisibility(XpathSelector.WRAPPING_ELEMENT.getXPath());
        loadInputList();
    }

    /**
     * Loads the input list
     */
    public void loadInputList() {
        this.inputList = findSubElements(wrappingElement, XpathSelector.INPUT_LIST.getXPath());
    }

    /**
     * Fill an input value.
     *
     * @param inputName the input name
     * @param value     the value
     */
    public void fillInputValue(final String inputName, final Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String || value instanceof Integer) {
            fillSimpleValue(inputName, String.valueOf(value));
            return;
        }
        if (value instanceof Boolean) {
            fillBooleanValue(inputName, String.valueOf(value));
            return;
        }
        throw new UnsupportedOperationException("Set input value not yet implemented for value type: " + value.getClass().getName());
    }

    /**
     * Expands or retracts an input in the input list.
     *
     * @param name the input name
     */
    public void toggleInputExpansion(final String name) {
        final Optional<WebElement> inputOpt = findInput(name);
        final By expandIconSelector = By.xpath(XpathSelector.EXPAND_ICON.getXPath());
        inputOpt.ifPresent(webElement ->
            webElement.findElement(expandIconSelector).click()
        );
    }

    /**
     * Deletes an input from the input list.
     *
     * @param name the name of the input to delete
     */
    public void deleteInput(final String name) {
        final Optional<WebElement> inputOpt = findInput(name);
        final By deleteIconSelector = By.xpath(XpathSelector.DELETE_ICON.getXPath());
        inputOpt.ifPresent(webElement ->
            webElement.findElement(deleteIconSelector).click()
        );
        loadInputList();
        assertTrue(findInput(name).isEmpty());
    }

    public List<InputData> getInputList() {
        if (inputList.isEmpty()) {
            return Collections.emptyList();
        }
        final List<InputData> inputDataList = new ArrayList<>();
        final By inputLabelSelector = By.xpath(XpathSelector.INPUT_LABEL.getXPath());
        final By inputTypeSelector = By.xpath(XpathSelector.INPUT_TYPE.getXPath());
        inputList.forEach(inputWebElement -> {
            String inputLabel = inputWebElement.findElement(inputLabelSelector).getText();
            inputLabel = inputLabel.substring(0, inputLabel.length() -1);
            final String inputType = inputWebElement.findElement(inputTypeSelector).getText();
            var inputData = new InputData(inputLabel, inputType, null);
            inputDataList.add(inputData);
        });

        return inputDataList;
    }

    private Optional<WebElement> findInput(final String name) {
        final String label = name + ":";
        final By inputLabelSelector = By.xpath(XpathSelector.INPUT_LABEL.getXPath());
        return inputList.stream().filter(webElement -> {
            final WebElement inputLabel = webElement.findElement(inputLabelSelector);
            return label.equals(inputLabel.getText());
        }).findFirst();
    }

    private void fillSimpleValue(final String inputName, final String inputValue) {
        toggleInputExpansion(inputName);
        final Optional<WebElement> inputOpt = findInput(inputName);
        assertTrue(inputOpt.isPresent(), String.format("Could not set value for input '%s'. The input was not found.", inputName));
        final By simpleInputValueSelector = By.xpath(XpathSelector.SIMPLE_VALUE_INPUT_RELATIVE_FROM_INPUT_INFO.getXPath());
        inputOpt.ifPresent(webElement -> webElement.findElement(simpleInputValueSelector).sendKeys(inputValue));
    }

    private void fillBooleanValue(final String inputName, final String inputValue) {
        toggleInputExpansion(inputName);
        final Optional<WebElement> inputOpt = findInput(inputName);
        assertTrue(inputOpt.isPresent(), String.format("Could not set value for input '%s'. The input was not found.", inputName));
        final By simpleInputValueSelector = By.xpath(XpathSelector.BOOLEAN_VALUE_INPUT_RELATIVE_FROM_INPUT_INFO.getXPath());
        final WebElement booleanDropdownWebElement = inputOpt.get().findElement(simpleInputValueSelector);
        new Select(booleanDropdownWebElement).selectByValue(inputValue);
    }

    @AllArgsConstructor
    private enum XpathSelector {
        WRAPPING_ELEMENT("//div[@class='input-tree']"),
        INPUT_LIST("//div[@class='input-tree']/*/*/*/span[@class='input-info']"),
        INPUT_LABEL("label[@class='input-label']"),
        INPUT_TYPE("em[@data-tests-id='input-type']"),
        SIMPLE_VALUE_INPUT_RELATIVE_FROM_INPUT_INFO("..//li[@class='input-value']/input"),
        BOOLEAN_VALUE_INPUT_RELATIVE_FROM_INPUT_INFO("..//li[@class='input-value']/select"),
        EXPAND_ICON("em[contains(concat(' ',normalize-space(@class),' '),' round-expand-icon ')]"),
        DELETE_ICON("span[contains(concat(' ',normalize-space(@class),' '),' delete-btn ')]");

        private final String xPath;

        public String getXPath(final String... xpathParams) {
            return String.format(xPath, xpathParams);
        }
    }
}
