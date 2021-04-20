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

import com.aventstack.extentreports.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionHierarchyComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Handles the 'Attributes & Outputs' Page UI actions
 */
public class AttributesOutputsPage extends ComponentPage {

    private final AttributesTabComponent attributesTabComponent;
    private final OutputsTabComponent outputsTabComponent;
    private final CompositionHierarchyComponent compositionHierarchyComponent;

    public AttributesOutputsPage(final WebDriver webDriver) {
        super(webDriver);
        attributesTabComponent = new AttributesTabComponent(webDriver);
        outputsTabComponent = new OutputsTabComponent(webDriver);
        compositionHierarchyComponent = new CompositionHierarchyComponent(webDriver);
        setTimeout(5);
    }

    @Override
    public void isLoaded() {
        super.isLoaded();
        waitForElementVisibility(By.xpath(XpathSelector.MAIN_DIV.getXpath()));
        waitForElementVisibility(By.xpath(XpathSelector.TITLE_DIV.getXpath()));
        attributesTabComponent.isLoaded();
        compositionHierarchyComponent.isLoaded();
    }

    public void clickOnAttributeNavigation(final String id) {
        compositionHierarchyComponent.clickOnAttributeNavigation(id);
        assertTrue(attributesTabComponent.isInstanceSelected(id));
    }

    /**
     * Checks if a attribute exists.
     *
     * @return true if exists, false if not
     */
    public boolean isAttributePresent(final String attributeName) {
        ExtentTestActions.log(Status.INFO, "Going to check if Attribute '" + attributeName + "' is present");
        return attributesTabComponent.isAttributePresent(attributeName);
    }

    /**
     * Checks if a output exists.
     *
     * @return true if exists, false if not
     */
    public boolean isOutputPresent(final String outputName) {
        ExtentTestActions.log(Status.INFO, "Going to check if Output '" + outputName + "' is present");
        return outputsTabComponent.isOutputPresent(outputName);
    }

    /**
     * Checks if a output deleted.
     *
     * @return true if deleted, false if not
     */
    public boolean isOutputDeleted(final String outputName) {
        ExtentTestActions.log(Status.INFO, "Going to check if Output '" + outputName + "' deleted");
        return outputsTabComponent.isOutputDeleted(outputName);
    }

    public void declareOutput(final String attributeName) {
        ExtentTestActions.log(Status.INFO, "Going to declare Attribute '" + attributeName + "' as Output");
        attributesTabComponent.declareOutput(attributeName);
    }

    public void deleteOutput(final String outputName) {
        ExtentTestActions.log(Status.INFO, "Going to delete Output '" + outputName + "'");
        outputsTabComponent.deleteOutput(outputName);
    }

    public void clickOnOutputsTab() {
        waitForElementVisibility(By.xpath(XpathSelector.OUTPUTS_TAB.getXpath())).click();
        outputsTabComponent.isLoaded();
    }

    public void clickOnAttributesTab() {
        waitForElementVisibility(By.xpath(XpathSelector.ATTRIBUTES_TAB.getXpath())).click();
        attributesTabComponent.isLoaded();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_DIV("w-sdc-main-right-container", "//div[@class='%s']"),
        TITLE_DIV("tab-title", "//div[contains(@class,'%s') and contains(text(), 'Attributes & Outputs')]"),
        ATTRIBUTES_TAB("Attributes", "//*[@data-tests-id='%s']"),
        OUTPUTS_TAB("Outputs", "//*[@data-tests-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

    }

}
