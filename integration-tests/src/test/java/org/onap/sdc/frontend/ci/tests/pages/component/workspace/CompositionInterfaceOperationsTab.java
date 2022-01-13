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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Represents the Composition Interface Operations tab.
 */
public class CompositionInterfaceOperationsTab extends AbstractPageObject {

    private WebElement webElement;

    public CompositionInterfaceOperationsTab(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.INTERFACE_OPERATIONS.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.INTERFACE_NAME_SPAN.getXPath()));
        webElement = findElement(By.xpath(XpathSelector.OPERATION_LIST.getXPath()));
    }

    public boolean isOperationPresent(final String operationName) {
        try {
            final WebElement webElementInterfaceRow = webElement.findElement(By.xpath(XpathSelector.INTERFACE_ROW.getXPath()));
            webElementInterfaceRow.findElement(By.xpath(XpathSelector.FIELD_NAME_SPAN.getXPath(operationName)));
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    public boolean isDescriptionPresent() {
        try {
            final WebElement webElementInterfaceRow = webElement.findElement(By.xpath(XpathSelector.INTERFACE_ROW.getXPath()));
            final WebElement rowElement = webElementInterfaceRow.findElement(By.xpath(XpathSelector.FIELD_DESCRIPTION_SPAN.getXPath()));
            return rowElement != null && !rowElement.getText().isEmpty();
        } catch (final Exception e) {
            return false;
        }
    }

    public InterfaceDefinitionOperationsModal clickOnOperation(final String operationName) {
        final WebElement webElementInterfaceRow = webElement.findElement(By.xpath(XpathSelector.INTERFACE_ROW.getXPath()));
        webElementInterfaceRow.findElement(By.xpath(XpathSelector.FIELD_NAME_SPAN.getXPath(operationName))).click();
        return new InterfaceDefinitionOperationsModal(webDriver);
    }

    @AllArgsConstructor
    private enum XpathSelector {
        INTERFACE_OPERATIONS("//div[@class='interface-operations']"),
        OPERATION_LIST("//div[@class='operation-list']"),
        EXPAND_COLLAPSE("//div[@class='expand-collapse']"),
        INTERFACE_ACCORDION("//div[@class='interface-accordion']"),
        INTERFACE_ROW("//div[contains(@class,'interface-row')]"),
        INTERFACE_NAME_SPAN("//span[@class='interface-name']"),
        FIELD_NAME_SPAN("//span[contains(@class,'field-name') and contains(text(), '%s')]"),
        FIELD_DESCRIPTION_SPAN("//span[contains(@class,'field-description')]");

        @Getter
        private final String xPath;

        public String getXPath(final String... xpathParams) {
            return String.format(xPath, xpathParams);
        }

    }
}
