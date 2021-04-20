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

/**
 * Handles the 'Outputs' Tab UI component on 'Attributes & Outputs' Page
 */
public class OutputsTabComponent extends AbstractPageObject {

    public OutputsTabComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.OUTPUT_ATTRIBUTES_TABLE.getXpath()));
        waitForElementInvisibility(By.xpath(XpathSelector.NO_DATA_MESSAGE.getXpath()));
    }

    public void deleteOutput(final String outputName) {
        if (outputName == null) {
            return;
        }
        waitForElementVisibility(By.xpath(XpathSelector.DELETE_OUTPUT_BTN.getXpath(normalizeOutputName(outputName)))).click();
        waitToBeClickable(By.xpath(XpathSelector.DELETE_OUTPUT_CONFIRM_BTN.getXpath())).click();
        waitForElementInvisibility(By.xpath(XpathSelector.OUTPUT_NAME_SPAN.getXpath(normalizeOutputName(outputName))), 5);
    }

    /**
     * Checks if a output exists.
     *
     * @return true if exists, false if not
     */
    public boolean isOutputPresent(final String outputName) {
        try {
            waitForElementVisibility(By.xpath(XpathSelector.OUTPUT_NAME_SPAN.getXpath(outputName)));
        } catch (final Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a output deleted.
     *
     * @return true if deleted, false if not
     */
    public boolean isOutputDeleted(final String outputName) {
        try {
            findElement(By.xpath(XpathSelector.OUTPUT_NAME_SPAN.getXpath(outputName)));
        } catch (final Exception ignored) {
            return true;
        }
        return false;
    }

    private String normalizeOutputName(final String outputName) {
        return outputName.replaceAll(" ", "").replaceAll("-", "").toLowerCase();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        DELETE_OUTPUT_BTN("//span[@data-tests-id='delete-%s']"),
        OUTPUT_ATTRIBUTES_TABLE("output-attributes-table", "//div[contains(@class,'%s')]"),
        NO_DATA_MESSAGE("no-data", "//div[contains(@class,'%s') and text()='No data to display']"),
        DELETE_OUTPUT_CONFIRM_BTN("Delete", "//button[@data-tests-id='%s']"),
        OUTPUT_NAME_SPAN("//span[contains(@class,'attribute-name') and contains(text(), '%s')]");

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
