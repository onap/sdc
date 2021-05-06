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

import com.aventstack.extentreports.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Represents the relationship wizard dialog that is used to add interface operations to a node.
 */
public class RelationshipWizardInterfaceOperation extends AbstractPageObject {

    public RelationshipWizardInterfaceOperation(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitToBeClickable(By.xpath(XpathSelector.ADD_INPUT_BTN.getXPath()));
    }

    public void addInterfaceOperation(final InterfaceOperationsData interfaceOperationsData) {
        selectInterfaceName(interfaceOperationsData.getInterfaceName());
        selectOperationName(interfaceOperationsData.getOperationName());
        fillImplementationName(interfaceOperationsData.getImplementation());
        addInput();
        fillInputName(interfaceOperationsData.getInputName());
        fillInputValue(interfaceOperationsData.getInputValue());
        saveInterfaceOperationAndInput();
    }

    private void selectInterfaceName(final String interfaceName) {
        waitToBeClickable(By.xpath(XpathSelector.INTERFACE_AND_OPERATION_SVG_ICON.getXPath("interface-name-icon"))).click();
        findElement(By.xpath(XpathSelector.INTERFACE_AND_OPERATION_DROPDOWN.getXPath(interfaceName))).click();
    }

    private void selectOperationName(final String interfaceOperationName) {
        waitToBeClickable(By.xpath(XpathSelector.INTERFACE_AND_OPERATION_SVG_ICON.getXPath("operation-name-icon"))).click();
        findElement(By.xpath(XpathSelector.INTERFACE_AND_OPERATION_DROPDOWN.getXPath(interfaceOperationName))).click();
    }

    private void fillImplementationName(final String implementationName) {
        setInputField(By.xpath(XpathSelector.INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT.getXPath()), implementationName);
    }

    private void fillInputName(final String inputName) {
        setInputField(By.xpath(XpathSelector.FIELD_INPUT_NAME.getXPath()), inputName);
    }

    private void fillInputValue(final String inputValue) {
        setInputField(By.xpath(XpathSelector.FIELD_INPUT_VALUE.getXPath()), inputValue);
    }

    private void setInputField(final By locator, final String value) {
        if (value == null) {
            return;
        }
        final WebElement webElement = findElement(locator);
        webElement.clear();
        webElement.sendKeys(value);
        ExtentTestActions.takeScreenshot(Status.INFO, value, value);
    }

    private void addInput() {
        waitToBeClickable(By.xpath(XpathSelector.ADD_INPUT_BTN.getXPath())).click();
    }

    private void saveInterfaceOperationAndInput() {
        waitToBeClickable(By.xpath(XpathSelector.ADD_BTN.getXPath())).click();
    }

    @Getter
    @AllArgsConstructor
    public static class InterfaceOperationsData {

        private final String interfaceName;
        private final String operationName;
        private final String implementation;
        private final String inputName;
        private final String inputValue;
    }

    @AllArgsConstructor
    private enum XpathSelector {
        INTERFACE_AND_OPERATION_SVG_ICON("//div[@data-tests-id='%s']"),
        INTERFACE_AND_OPERATION_DROPDOWN("//li[@class='sdc-dropdown__option ng-star-inserted' and contains(text(), '%s')]"),
        INTERFACE_OPERATION_DESCRIPTION_INPUT("//input[@data-tests-id='interface-operation-description']"),
        INTERFACE_OPERATION_IMPLEMENTATION_NAME_INPUT("//input[@id='implementationInput']"),
        ADD_INPUT_BTN("//a[contains(@class,'add-param-link add-btn') and contains(text(), 'Add Input')]"),
        FIELD_INPUT_NAME("//input[@id='propertyAssignmentNameInput']"),
        FIELD_INPUT_VALUE("//input[@id='propertyAssignmentValueInput']"),
        ADD_BTN("//button[@data-tests-id='addBtn']");

        @Getter
        private String xPath;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXPath() {
            return String.format(xpathFormat, xPath);
        }

        public String getXPath(final String... xpathParams) {
            return String.format(xpathFormat, xpathParams);
        }
    }
}
