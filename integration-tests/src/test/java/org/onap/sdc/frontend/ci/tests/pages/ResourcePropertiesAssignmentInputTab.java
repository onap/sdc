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

package org.onap.sdc.frontend.ci.tests.pages;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Handles the Resource Properties Assignment Input Tab UI actions
 */
public class ResourcePropertiesAssignmentInputTab extends AbstractPageObject {

    public ResourcePropertiesAssignmentInputTab(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(XpathSelector.INPUT_TAB.getXpath());
        isInputPropertiesTableLoaded();
    }

    /**
     * Creates a List of property names from the inputs tab
     */
    public List<String> getInputPropertyNames() {
        isInputPropertiesTableLoaded();
        final List<WebElement> propertyNames = findElements(By.xpath(XpathSelector.INPUT_PROPERTY_NAME.getXpath()));
        return propertyNames.stream().map(propertyName -> propertyName.getAttribute("innerText")).collect(Collectors.toList());
    }

    /**
     * Adds metadata to a property within the inputs tab based on a property name
     * @param name used to determine which property to add metadata
     * @param key the metadata key to add
     * @param value the metadata value to add
     */
    public void setInputPropertyMetadata(String name, String key, String value) {
        isInputPropertiesTableLoaded();
        findElement(By.xpath(XpathSelector.INPUT_PROPERTY_ADD_METADATA_BUTTON.formatXpath(name))).click();
        waitForElementVisibility(XpathSelector.INPUT_PROPERTY_METADATA_KEY_VALUE_PAIR.formatXpath(name));
        List<WebElement> keyValueInputs = findElements(By.xpath(XpathSelector.INPUT_PROPERTY_METADATA_KEY_VALUE_PAIR.formatXpath(name)));
        keyValueInputs.get(0).sendKeys(key);
        keyValueInputs.get(1).sendKeys(value);
        saveInputProperties();
        ExtentTestActions.takeScreenshot(Status.INFO, name, String.format("Added metadata for property %s", name));
    }

    private void isInputPropertiesTableLoaded() {
        waitForElementVisibility(XpathSelector.PROPERTIES_TABLE.getXpath());
        waitForElementInvisibility(By.xpath(XpathSelector.NO_DATA_MESSAGE.getXpath()));
    }

    public void saveInputProperties() {
        findElement(By.xpath(XpathSelector.PROPERTY_SAVE_BTN.getXpath())).click();
        waitForElementVisibility(XpathSelector.PROPERTY_SAVE_MESSAGE.getXpath());
        waitForElementInvisibility(By.xpath(XpathSelector.PROPERTY_SAVE_MESSAGE.getXpath()));
    }

    /**
     * Adds a input
     * @param inputsMap the inputs map to be added
     */
    public void addInputs(final Map<String, String> inputsMap) {
        isInputPropertiesTableLoaded();
        inputsMap.forEach((inputName, inputType) -> {
            WebElement inputAddButton = findElement(By.xpath(XpathSelector.INPUT_ADD_BTN.getXpath()));
            assertTrue(inputAddButton.isDisplayed());
            inputAddButton.click();
            createInput(inputName, inputType);
            waitForElementInvisibility(By.xpath(XpathSelector.MODAL_BACKGROUND.getXpath()), 5);
            ExtentTestActions.takeScreenshot(Status.INFO, "added-input",
                String.format("Input '%s' was created on component", inputName));
        });
    }

    /**
     * Fills the creation input modal.
     * @param inputName the input name to be created
     * @param inputType the input type to be selected
     */
    private void createInput(final String inputName, final String inputType) {
        final AddPropertyModal addInputModal = new AddPropertyModal(webDriver);
        addInputModal.isLoaded();
        addInputModal.fillPropertyForm(inputName, inputType);
        addInputModal.clickOnCreate();
    }

    /**
     * Verifies if the added input is displayed on the UI.
     * @param inputsMap the input name to be found
     */
    public void verifyInputs(final Map<String, String> inputsMap ) {
        for (Map.Entry<String, String> input : inputsMap.entrySet()) {
            assertTrue(this.getInputPropertyNames().contains(input.getKey()),
                String.format("%s Input should be listed but found %s", input.getKey(),
                    this.getInputPropertyNames().toString()));
        }
    }

    /**
     * Checks if a input exists.
     * @param inputName the input name
     * @return the value of the input
     */
    public boolean isInputPresent(final String inputName) {
        isInputPropertiesTableLoaded();
        try {
            waitForElementVisibility(By.xpath(XpathSelector.INPUT_CHECKBOX.formatXpath(inputName)), 5);
        } catch (final Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        INPUT_TAB("//*[contains(@data-tests-id, 'Inputs') and contains(@class, 'active')]"),
        PROPERTIES_TABLE("//div[contains(@class,'properties-table')]"),
        INPUT_CHECKBOX("//checkbox[@data-tests-id='%s']"),
        NO_DATA_MESSAGE("//div[contains(@class,'no-data') and text()='No data to display']"),
        PROPERTY_SAVE_BTN("//button[@data-tests-id='properties-save-button']"),
        PROPERTY_SAVE_MESSAGE("//div[contains(text(), 'Successfully saved')]"),
        INPUT_PROPERTY_NAME("//*[contains(@class, 'property-name')]"),
        INPUT_PROPERTY_TABLE_ROW("//div[contains(@class, 'table-row') and descendant::*[text() = '%s']]"),
        INPUT_PROPERTY_ADD_METADATA_BUTTON(INPUT_PROPERTY_TABLE_ROW.getXpath().concat("//a")),
        INPUT_PROPERTY_METADATA_KEY_VALUE_PAIR(INPUT_PROPERTY_TABLE_ROW.getXpath().concat("//input")),
        INPUT_ADD_BTN("//div[contains(@class,'add-btn')]"),
        MODAL_BACKGROUND("//div[@class='modal-background']");

        @Getter
        private final String xpath;

        public String formatXpath(Object... params) {
            return String.format(xpath, params);
        }
    }

}
