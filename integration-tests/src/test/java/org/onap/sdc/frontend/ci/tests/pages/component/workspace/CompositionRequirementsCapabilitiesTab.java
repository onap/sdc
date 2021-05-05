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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the composition page, details panel, Substitution Filters tab.
 */
public class CompositionRequirementsCapabilitiesTab extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionRequirementsCapabilitiesTab.class);

    private Map<String, WebElement> checkboxExternalRequirementMap;
    private final LoaderHelper loaderHelper;

    public CompositionRequirementsCapabilitiesTab(final WebDriver webDriver) {
        super(webDriver);
        loaderHelper = new LoaderHelper(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.REQ_CAPABILITIES_TAB.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.CAPABILITIES_ACCORDION.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.REQUIREMENTS_ACCORDION.getXPath()));
    }

    public void clickOnRequirements() {
        waitForElementVisibility(XpathSelector.REQUIREMENTS_ACCORDION.getXPath()).click();
        loadRequirements();
    }

    public void clickOnCapabilities() {
        waitForElementVisibility(XpathSelector.CAPABILITIES_ACCORDION.getXPath()).click();
    }

    private void loadRequirements() {
        final List<WebElement> webElements = waitForAllElementsVisibility(By.xpath(XpathSelector.REQUIREMENT_EXTERNAL_CHECKBOX.getXPath()));
        checkboxExternalRequirementMap = new HashMap<>();
        webElements.forEach(webElement -> {
            final String dataTestsId = webElement.getAttribute("data-tests-id");
            checkboxExternalRequirementMap.put(dataTestsId.substring("checkbox-external-req-".length()), webElement);
        });
    }

    public void toggleRequirementAsExternal(final String requirementName) {
        LOGGER.debug("Externalizing the requirement '{}'", requirementName);
        if (checkboxExternalRequirementMap == null) {
            throw new UiTestFlowRuntimeException("The requirements checkbox map is not loaded. Did you call clickOnRequirements?");
        }
        final WebElement element = checkboxExternalRequirementMap.get(requirementName);
        if (element == null) {
            fail(String.format("Could not find requirement '%s'", requirementName));
        }
        element.click();
        loaderHelper.waitForLoader(LoaderHelper.XpathSelector.LOADER_WITH_LOADER_BACKGROUND, 10);
    }


    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        REQ_CAPABILITIES_TAB("//req-capabilities-tab"),
        CAPABILITIES_ACCORDION("//div[@data-tests-id='Capabilities-accordion']"),
        REQUIREMENTS_ACCORDION("//div[@data-tests-id='Requirements-accordion']"),
        REQUIREMENT_EXTERNAL_CHECKBOX("//checkbox[starts-with(@data-tests-id, 'checkbox-external-req-')]");

        private final String xPath;

    }
}
