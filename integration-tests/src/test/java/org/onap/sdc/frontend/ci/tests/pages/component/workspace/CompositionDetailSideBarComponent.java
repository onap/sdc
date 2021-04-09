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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CompositionDetailSideBarComponent extends AbstractPageObject {

    private WebElement wrappingElement;

    public CompositionDetailSideBarComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = waitForElementVisibility(By.xpath(XpathSelector.MAIN_ELEMENT_DIV.getXpath()));
    }

    public String getSelectedComponentName() {
        return wrappingElement.findElement(By.xpath(XpathSelector.DETAIL_COMPONENT_NAME_DIV.getXpath())).getText();
    }

    public void checkComponentIsSelected(final String componentName) {
        assertThat("The selected component should be as expected", getSelectedComponentName(), is(componentName));
    }

    public Dimension getSize() {
        final WebElement sideBarToggle = waitForElementVisibility(XpathSelector.DETAIL_SIDE_BAR_TOGGLE_DIV.getXpath());
        if (!sideBarToggle.getAttribute("class").contains("active")) {
            return new Dimension(0, 0);
        }

        return wrappingElement.getSize();
    }

    public AbstractPageObject selectTab(final CompositionDetailTabName tabName) {
        final WebElement tabElement = wrappingElement.findElement(By.xpath(tabName.getXpathSelector().getXpath()));
        tabElement.click();
        switch (tabName) {
            case INFORMATION:
                return new CompositionInformationTab(webDriver);
            case INPUTS:
                return new CompositionInputsTab(webDriver);
            case DEPLOYMENT_ARTIFACTS:
                return new CompositionDeploymentArtifactsTab(webDriver);
            case INFORMATIONAL_ARTIFACTS:
                return new CompositionInformationalArtifactsTab(webDriver);
            case API_ARTIFACTS:
                return new CompositionApiArtifactsTab(webDriver);
            case SUBSTITUTION_FILTER:
                return new CompositionSubstitutionFilterTab(webDriver);
            case REQUIREMENTS_CAPABILITIES:
                return new CompositionRequirementsCapabilitiesTab(webDriver);
            default:
                throw new IllegalStateException("Not yet implemented: " + tabName);
        }
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_ELEMENT_DIV("w-sdc-designer-sidebar", "//div[@class='%s']"),
        DETAIL_SIDE_BAR_TOGGLE_DIV("w-sdc-designer-sidebar-toggle", "//div[contains(concat(' ',normalize-space(@class),' '),' %s ')]"),
        DETAIL_HEADER("w-sdc-designer-sidebar-head", "//div[@data-tests-id='%s']"),
        DETAIL_COMPONENT_NAME_DIV("selectedCompTitle", "//div[@data-tests-id='%s']"),
        TAB_LIST("sdc-tabs-list", "//ul[@class='%s']/li"),
        INFORMATION_TAB("detail-tab-information", "//li[@data-tests-id='%s']"),
        INPUTS_TAB("detail-tab-inputs", "//li[@data-tests-id='%s']"),
        DEPLOYMENT_ARTIFACTS_TAB("detail-tab-deployment-artifacts", "//li[@data-tests-id='%s']"),
        INFORMATION_ARTIFACTS_TAB("detail-tab-information-artifacts", "//li[@data-tests-id='%s']"),
        REQUIREMENTS_CAPABILITIES_TAB("detail-tab-requirements-capabilities", "//li[@data-tests-id='%s']"),
        API_ARTIFACTS_TAB("detail-tab-api-artifacts", "//li[@data-tests-id='%s']"),
        SUBSTITUTION_FILTER_TAB("detail-tab-substitution-filter", "//li[@data-tests-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum CompositionDetailTabName {
        INFORMATION(XpathSelector.INFORMATION_TAB),
        INPUTS(XpathSelector.INPUTS_TAB),
        DEPLOYMENT_ARTIFACTS(XpathSelector.DEPLOYMENT_ARTIFACTS_TAB),
        INFORMATIONAL_ARTIFACTS(XpathSelector.INFORMATION_ARTIFACTS_TAB),
        API_ARTIFACTS(XpathSelector.API_ARTIFACTS_TAB),
        SUBSTITUTION_FILTER(XpathSelector.SUBSTITUTION_FILTER_TAB),
        REQUIREMENTS_CAPABILITIES(XpathSelector.REQUIREMENTS_CAPABILITIES_TAB);

        private final XpathSelector xpathSelector;

    }
}

