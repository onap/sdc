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
 * Handles the 'Attributes' Tab UI component on 'Attributes & Outputs' Page
 */
public class AttributesTabComponent extends AbstractPageObject {

    public AttributesTabComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.ATTRIBUTES_TABLE.getXpath()));
        waitForElementInvisibility(By.xpath(XpathSelector.NO_DATA_MESSAGE.getXpath()));
    }

    public void declareOutput(final String attributeName) {
        if (attributeName == null) {
            return;
        }
        waitForElementVisibility(By.xpath(XpathSelector.ATTRIBUTES_CHECKBOX.getXpath(attributeName))).click();
        waitToBeClickable(By.xpath(XpathSelector.DECLARE_OUTPUT_BTN.getXpath())).click();
        waitForAddedOutputNotification();
    }

    private void waitForAddedOutputNotification() {
        waitForElementVisibility(By.xpath(XpathSelector.ADDED_OUTPUT_NOTIFICATION.getXpath()));
    }

    /**
     * Checks if a attribute exists.
     *
     * @return true if exists, false if not
     */
    public boolean isAttributePresent(final String attributeName) {
        try {
            waitForElementVisibility(By.xpath(XpathSelector.ATTRIBUTES_CHECKBOX.getXpath(attributeName)));
        } catch (final Exception ignored) {
            return false;
        }
        return true;
    }

    public boolean isInstanceSelected(final String id) {
        final WebElement webElement = waitForElementVisibility(By.xpath(XpathSelector.INSTANCE_SPAN.getXpath()));
        final String text = webElement.getText();
        return text.equalsIgnoreCase(id);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        ATTRIBUTES_TABLE("attributes-table", "//div[contains(@class,'%s')]"),
        NO_DATA_MESSAGE("no-data", "//div[contains(@class,'%s') and text()='No data to display']"),
        ATTRIBUTES_CHECKBOX("//checkbox[@data-tests-id='%s']"),
        DECLARE_OUTPUT_BTN("declare-button declare-output", "//button[@data-tests-id='%s']"),
        INSTANCE_SPAN("//div[contains(@class,'table-rows-header')]"),
        ADDED_OUTPUT_NOTIFICATION("tab-indication", "//div[@data-tests-id='Outputs']/div[contains(@class, '%s')]");

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
