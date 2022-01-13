/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class InterfaceDefinitionPage extends ComponentPage {

    private WebElement wrappingElement;

    public InterfaceDefinitionPage(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = waitForElementVisibility(By.xpath(XpathSelector.MAIN_DIV.getXpath()), 5);
        waitForElementVisibility(By.xpath(XpathSelector.TITLE_DIV.getXpath()), 5);
        waitForElementVisibility(By.xpath(XpathSelector.INTERFACE_NAME_SPAN.getXpath()), 5);
    }

    public boolean isInterfaceDefinitionOperationPresent(final String operationName) {
        try {
            final WebElement webElementInterfaceRow = wrappingElement.findElement(
                By.xpath(InterfaceDefinitionPage.XpathSelector.INTERFACE_ROW.getXpath()));
            webElementInterfaceRow.findElement(By.xpath(InterfaceDefinitionPage.XpathSelector.FIELD_NAME_SPAN.getXpath(operationName)));
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    public InterfaceDefinitionOperationsModal clickOnInterfaceDefinitionOperation(final String operationName) {
        final WebElement webElementInterfaceRow = wrappingElement.findElement(
            By.xpath(InterfaceDefinitionPage.XpathSelector.INTERFACE_ROW.getXpath()));
        webElementInterfaceRow.findElement(By.xpath(InterfaceDefinitionPage.XpathSelector.FIELD_NAME_SPAN.getXpath(operationName))).click();
        return new InterfaceDefinitionOperationsModal(webDriver);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_DIV("w-sdc-main-right-container", "//div[@class='%s']"),
        TITLE_DIV("tab-title", "//div[contains(@class,'%s') and contains(text(), 'Interfaces')]"),
        INTERFACE_NAME_SPAN("//span[@class='interface-name']"),

        INTERFACE_OPERATIONS("//div[@class='interface-operations']"),
        OPERATION_LIST("//div[@class='operation-list']"),
        EXPAND_COLLAPSE("//div[@class='expand-collapse']"),
        INTERFACE_ACCORDION("//div[@class='interface-accordion']"),
        INTERFACE_ROW("//div[contains(@class,'interface-row')]"),
        FIELD_NAME_SPAN("//span[contains(@class,'field-name') and contains(text(), '%s')]"),
        FIELD_DESCRIPTION_SPAN("//span[contains(@class,'field-description')]");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

        public String getXpath(final String... params) {
            return String.format(xpathFormat, params);
        }

    }
}
