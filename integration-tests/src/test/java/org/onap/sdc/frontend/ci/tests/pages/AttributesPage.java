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
import org.onap.sdc.frontend.ci.tests.pages.AttributeModal.AttributeData;
import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Handles the 'Attributes' Page UI actions
 */
public class AttributesPage extends ComponentPage {

    private WebElement wrappingElement;

    public AttributesPage(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        super.isLoaded();
        waitForElementVisibility(By.xpath(XpathSelector.MAIN_DIV.getXpath()));
        waitForElementVisibility(By.xpath(XpathSelector.TITLE_DIV.getXpath()));
        waitForElementVisibility(By.xpath(XpathSelector.ATTRIBUTES_DIV.getXpath()));
        wrappingElement = findElement(By.xpath(XpathSelector.ATTRIBUTES_DIV.getXpath()));
    }

    public boolean isAttributePresent(final String attributeName) {
        try {
            final WebElement element = wrappingElement.findElement(By.xpath(XpathSelector.ATTRIBUTES_NAME_SPAN.getXpath(attributeName)));
            return element != null;
        } catch (final Exception e) {
            return false;
        }
    }

    public void addAttribute(final AttributeData attributeData) {
        final AttributeModal attributeModal = clickOnAdd();
        attributeModal.isLoaded();
        attributeModal.fillForm(attributeData, false);
        attributeModal.clickSave();
        loaderHelper.waitForLoader(LoaderHelper.XpathSelector.SDC_LOADER_LARGE, 5);
    }

    public AttributeModal clickOnAdd() {
        waitToBeClickable(By.xpath(XpathSelector.ADD_BTN.getXpath())).click();
        return new AttributeModal(webDriver);
    }

    public AttributeModal clickOnEdit(final String attributeName) {
        waitToBeClickable(By.xpath(XpathSelector.EDIT_BTN.getXpath(attributeName))).click();
        return new AttributeModal(webDriver);
    }

    public void deleteAttribute(final String attributeName) {
        if (attributeName == null) {
            return;
        }
        waitForElementVisibility(By.xpath(XpathSelector.DELETE_BTN.getXpath(attributeName))).click();
        waitToBeClickable(By.xpath(XpathSelector.DELETE_ATTRIBUTE_CONFIRM_BTN.getXpath())).click();
        waitForElementInvisibility(By.xpath(XpathSelector.DELETE_BTN.getXpath(attributeName)), 10);
    }

    public void editAttribute(final AttributeData attributeData) {
        final AttributeModal attributeModal = clickOnEdit(attributeData.getAttributeName());
        attributeModal.isLoaded();
        attributeModal.fillForm(attributeData, true);
        attributeModal.clickSave();
        loaderHelper.waitForLoader(LoaderHelper.XpathSelector.SDC_LOADER_LARGE, 5);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_DIV("w-sdc-main-right-container", "//div[@class='%s']"),
        TITLE_DIV("workspace-tab-title", "//div[contains(@class,'%s') and contains(text(), 'Attributes')]"),
        ATTRIBUTES_DIV("workspace-attributes", "//div[@class='%s']"),
        ADD_BTN("svg-icon-label", "//span[contains(@class,'%s') and contains(text(), 'Add')]"),
        ATTRIBUTES_NAME_SPAN("//div[@data-tests-id='attrib-name_%s']"),
        EDIT_BTN("//div[contains(@class,'svg-icon') and @data-tests-id='edit_%s']"),
        DELETE_BTN("//div[contains(@class,'svg-icon') and @data-tests-id='delete_%s']"),
        DELETE_ATTRIBUTE_CONFIRM_BTN("delete-modal-button-ok", "//button[@data-tests-id='%s']");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath(final String... xpathParams) {
            return String.format(xpathFormat, xpathParams);
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

    }

}
