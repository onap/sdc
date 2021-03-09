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

import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.ServiceDependenciesEditor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the composition page, details panel, Substitution Filters tab
 */
public class CompositionSubstitutionFilterTab extends AbstractPageObject {

    public CompositionSubstitutionFilterTab(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.SUBSTITUTION_FILTER_TAB.xPath));
        waitForElementVisibility(By.xpath(XpathSelector.SUBSTITUTION_FILTER_ADD_BTN.xPath));
    }

    public ServiceDependenciesEditor clickAddSubstitutionFilter() {
        findElement(By.xpath(XpathSelector.SUBSTITUTION_FILTER_ADD_BTN.xPath)).click();
        return new ServiceDependenciesEditor(webDriver);
    }

    public boolean isSubstitutionFilterPresent(final String propertyName) {
        try {
            return waitForElementVisibility(By.xpath(String.format(XpathSelector.SUBSTITUTION_FILTER_DESC.xPath, propertyName))) != null;
        } catch (final Exception ignored) {
            return false;
        }
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        SUBSTITUTION_FILTER_TAB("//substitution-filter-tab"),
        SUBSTITUTION_FILTER_ADD_BTN("//button[@data-tests-id='add-substitution-filter-button']"),
        SUBSTITUTION_FILTER_DESC("//*[@class='rule-desc' and contains(text(),'%s')]");

        private final String xPath;

    }
}
