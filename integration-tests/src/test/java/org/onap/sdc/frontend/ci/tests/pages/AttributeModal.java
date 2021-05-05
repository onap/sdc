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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Handles the 'Attributes' Edit Modal UI actions
 */
public class AttributeModal extends AbstractPageObject {

    private WebElement wrappingElement;

    public AttributeModal(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.TITLE_DIV.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.SAVE_BTN.getXPath()));
        wrappingElement = findElement(By.xpath(XpathSelector.ATTR_CONTAINER_DIV.getXPath()));
    }

    public void fillForm(final AttributeData attributeData, final boolean isUpdate) {
        if (!isUpdate) {
            editName(attributeData.getAttributeName());
        }
        editDescription(attributeData.getDescription());
        editType(attributeData.getAttributeType());
        editDefaultValue(attributeData.getDefaultValue());
    }

    private void editName(final String attributeName) {
        final WebElement webElement = waitForElementVisibility(By.xpath(XpathSelector.ATTRIBUTE_NAME_INPUT.getXPath()));
        webElement.clear();
        webElement.sendKeys(attributeName);
    }

    private void editDescription(final String description) {
        final WebElement webElement = waitForElementVisibility(By.xpath(XpathSelector.DESCRIPTION_INPUT.getXPath()));
        webElement.clear();
        webElement.sendKeys(description);
    }

    private void editType(final String attributeType) {
        waitToBeClickable(By.xpath(XpathSelector.ATTRIBUT_TYPE_ICON.getXPath())).click();
        final WebElement element = waitForElementVisibility(By.xpath(XpathSelector.DROPDOWN_RESULTS.getXPath()));
        element.findElement(By.xpath(XpathSelector.ATTRIBUTE_TYPE_LI.getXPath(attributeType))).click();
    }

    private void editDefaultValue(final String defaultValue) {
        final WebElement webElement = waitForElementVisibility(By.xpath(XpathSelector.DEFAULT_VALUE_INPUT.getXPath()));
        webElement.clear();
        webElement.sendKeys(defaultValue);
    }

    public void clickSave() {
        waitToBeClickable(By.xpath(XpathSelector.SAVE_BTN.getXPath())).click();
    }

    @Getter
    @AllArgsConstructor
    public static class AttributeData {

        private final String attributeName;
        private final String description;
        private final String attributeType;
        private final String defaultValue;
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        TITLE_DIV("//div[contains(@class,'title') and contains(text(), ' Attribute Details')]"),
        ATTR_CONTAINER_DIV("//div[@class='attr-container']"),
        ATTRIBUTE_NAME_INPUT("//input[@data-tests-id='attributeName']"),
        DESCRIPTION_INPUT("//textarea[@data-tests-id='description']"),
        ATTRIBUTE_TYPE_LI("//li[@data-tests-id='%s']"),
        ATTRIBUT_TYPE_ICON("//div[@data-tests-id='attributeType-icon']"),
        DROPDOWN_RESULTS("//ul[contains(@class,'dropdown-results')]"),
        ATTRIBUTE_TYPE_DIV("//div[@data-tests-id='attributeType']"),
        DEFAULT_VALUE_INPUT("//input[@data-tests-id='defaultValue']"),
        SAVE_BTN("//button[@data-tests-id='button-save']");

        @Getter
        private final String xPath;

        public String getXPath(final String... xpathParams) {
            return String.format(xPath, xpathParams);
        }

    }

}
