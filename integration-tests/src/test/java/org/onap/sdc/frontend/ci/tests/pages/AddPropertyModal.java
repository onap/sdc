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

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the Property Creation Modal UI actions
 */
public class AddPropertyModal extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddPropertyModal.class);

    private WebElement wrappingElement;

    public AddPropertyModal(final WebDriver webDriver) {
        super(webDriver);
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.MODAL_XPATH.getXpath());
        wrappingElement = waitForElementVisibility(XpathSelector.MODAL_XPATH.getXpath());
    }

    /**
     * Fills the property modal creation.
     * @param propertyName the property name to be created
     * @param propertyType the property type to be selected
     */
    public void fillPropertyForm(final String propertyName, final String propertyType) {
        fillName(propertyName);
        selectType(propertyType);
        if (isComplexType(propertyType)) {
            setSchemaType();
        }
        fillDescription("Integration Test for adding property to a component");
    }

    /**
     * Clicks on the create button.
     */
    public void clickOnCreate() {
        clickElement(XpathSelector.SAVE_BTN);
    }

    /**
     * Fills the Property name.
     *
     * @param propertyName the property name
     */
    private void fillName(final String propertyName) {
        setInputValue(XpathSelector.NAME_TXT, propertyName);
    }

    /**
     * Selects a property type based on the option value
     *
     * @param propertyType the option value
     */
    private void selectType(final String propertyType) {
        setSelectValue(propertyType);
    }

    /**
     * Fills the property creation description.
     *
     * @param description the property description
     */
    private void fillDescription(final String description) {
        setInputValue(XpathSelector.DESCRIPTION_TXT, description);
    }

    /**
     * Sets Input value
     * @param inputTestId Data test id Xpath
     * @param value Input value
     */
    private void setInputValue(final XpathSelector inputTestId, final String value) {
        findSubElement(wrappingElement, By.xpath(inputTestId.getXpath())).sendKeys(value);
    }

    /**
     * Selects the option from the given propertyType value
     * @param propertyType option value to be selected
     */
    private void setSelectValue(final String propertyType) {
        new Select(findElement(By.xpath(XpathSelector.PROPERTY_TYPE_SELECT.getXpath()))).selectByVisibleText(propertyType);
    }

    /**
     * Sets Schema Type for complex types
     */
    private void setSchemaType() {
        new Select(findElement(By.xpath(XpathSelector.PROPERTY_SCHEMA_TYPE_SELECT.getXpath()))).selectByVisibleText("string");
    }

    private void clickElement(final XpathSelector elementTestId) {
        wrappingElement.findElement(By.xpath(elementTestId.getXpath())).click();
    }

    /**
     * Verifies if the given property type is a complex type
     * @param propertyType Property type
     * @return true if property type is found
     */
    private boolean isComplexType(final String propertyType) {
        return ImmutableSet.of("map", "list").contains(propertyType);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MODAL_XPATH("custom-modal", "//div[contains(@class,'%s')]"),
        NAME_TXT("property-name", "//input[@data-tests-id='%s']"),
        PROPERTY_TYPE_SELECT("value-property-type", "//select[@data-tests-id='%s']"),
        PROPERTY_SCHEMA_TYPE_SELECT("value-property-schema-type", "//select[@data-tests-id='%s']"),
        PROPERTY_CHECKBOX("//checkbox[@data-tests-id='%s']"),
        DESCRIPTION_TXT("property-description", "//textarea[@data-tests-id='%s']"),
        SAVE_BTN("Save", "//*[@data-tests-id='%s']");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

        public String getXpath(final String... xpathParams) {
            return String.format(xpathFormat, xpathParams);
        }
    }

}
