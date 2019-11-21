/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.ci.tests.pages;

import static org.openecomp.sdc.ci.tests.pages.OnboardHeaderComponent.XpathSelector.MAIN_DIV;
import static org.openecomp.sdc.ci.tests.pages.OnboardHeaderComponent.XpathSelector.ONBOARD_TAB_DIV;
import static org.openecomp.sdc.ci.tests.pages.OnboardHeaderComponent.XpathSelector.WORKSPACE_TAB_DIV;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Handles the Onboard Header Component UI test actions
 */
public class OnboardHeaderComponent extends AbstractPageObject {

    private WebElement wrappingElement;

    public OnboardHeaderComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = getWait()
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(MAIN_DIV.getXpath())));
        getWait()
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(WORKSPACE_TAB_DIV.getXpath())));
        getWait()
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ONBOARD_TAB_DIV.getXpath())));
    }

    /**
     * Clicks on the workspace tab.
     */
    public void clickOnWorkspaceTab() {
        wrappingElement.findElement(By.xpath(WORKSPACE_TAB_DIV.getXpath()));
    }

    /**
     * Clicks on the workspace tab.
     */
    public void clickOnOnboardTab() {
        wrappingElement.findElement(By.xpath(ONBOARD_TAB_DIV.getXpath()));
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        MAIN_DIV("onboard-header", "//div[contains(@class, '%s')]"),
        WORKSPACE_TAB_DIV("onboard-workspace-tab", "//div[@data-test-id='%s']"),
        ONBOARD_TAB_DIV("onboard-onboard-tab", "//div[@data-test-id='%s']");

        private final String id;
        private final String xpathFormat;

        XpathSelector(final String id, final String xpathFormat) {
            this.id = id;
            this.xpathFormat = xpathFormat;
        }

        public String getId() {
            return id;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
