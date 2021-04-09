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
import org.openqa.selenium.support.ui.Select;

/**
 * Represents the relationship/capability screen from the relationship wizard dialog.
 */
public class RelationshipWizardRequirementCapabilityComponent extends AbstractPageObject {

    private WebElement wrappingElement;

    public RelationshipWizardRequirementCapabilityComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = waitForElementVisibility(XpathSelector.REQUIREMENT_CAPABILITY.getXpath());
    }

    /**
     * Select a requirement or capability from the <select> by the name.
     * @param requirementOrCapabilityName the requirement/capability name
     */
    public void selectRequirementOrCapability(final String requirementOrCapabilityName) {
        final Select reqCapSelect = new Select(wrappingElement.findElement(By.xpath(XpathSelector.SELECT_REQUIREMENT_CAPABILITY.getXpath())));
        reqCapSelect.selectByVisibleText(requirementOrCapabilityName);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        REQUIREMENT_CAPABILITY("//select-requirement-or-capability"),
        SELECT_REQUIREMENT_CAPABILITY("//select[@data-tests-id='value-select']");

        @Getter
        private final String xpath;
    }

}
