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

import org.onap.sdc.frontend.ci.tests.datatypes.ServiceDependencyProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the Service Dependencies Editor
 */
public class ServiceDependenciesEditor extends AbstractPageObject {

    public ServiceDependenciesEditor(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.SERVICE_DEPENDENCIES_EDITOR.xPath));
    }

    public void addProperty(final ServiceDependencyProperty property) {
        final Select properties = new Select(webDriver.findElement(By.xpath(XpathSelector.SERVICE_PROPERTY_NAME.xPath)));
        properties.selectByVisibleText(property.getName());
        final Select logicalOperator = new Select(webDriver.findElement(By.xpath(XpathSelector.CONSTRAINT_OPERATOR.xPath)));
        logicalOperator.selectByVisibleText(property.getLogicalOperator().getOperator());
        final Select sourceType = new Select(webDriver.findElement(By.xpath(XpathSelector.SOURCE_TYPE.xPath)));
        sourceType.selectByVisibleText(property.getSource());
        addRuleAssignedValue(webDriver.findElement(
                By.xpath(XpathSelector.RULE_ASSIGNED_VALUE.xPath)), property.getValue());
        webDriver.findElement(By.xpath(XpathSelector.CREATE_BUTTON.xPath)).click();
    }

    private void addRuleAssignedValue(final WebElement element, final String value) {
        if ("select".equals(element.getTagName())) {
            new Select(element).selectByVisibleText(value);
        } else {
            element.sendKeys(value);
        }
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        SERVICE_DEPENDENCIES_EDITOR("//service-dependencies-editor"),
        SERVICE_PROPERTY_NAME("//*[@data-tests-id='servicePropertyName']/select"),
        CONSTRAINT_OPERATOR("//*[@data-tests-id='constraintOperator']/select"),
        SOURCE_TYPE("//*[@data-tests-id='sourceType']/select"),
        RULE_ASSIGNED_VALUE("//*[@data-tests-id='ruleAssignedValue']//*[self::input or self::select]"),
        CREATE_BUTTON("//button[text()='Create']");

        private final String xPath;

    }
}
