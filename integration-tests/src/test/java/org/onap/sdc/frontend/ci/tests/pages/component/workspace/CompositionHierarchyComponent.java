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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CompositionHierarchyComponent extends AbstractPageObject {

    private WebElement wrappingElement;

    public CompositionHierarchyComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = waitForElementVisibility(By.xpath(XpathSelector.MAIN_ELEMENT.getXpath()));
        assertNotNull(wrappingElement);
        wrappingElement.findElement(By.xpath(XpathSelector.COMPOSITION_TAB.getXpath()));
        wrappingElement.findElement(By.xpath(XpathSelector.STRUCTURE_TAB.getXpath()));
    }

    public void clickOnAttributeNavigation(final String id) {
        wrappingElement.findElement(By.xpath(XpathSelector.INSTANCE_SPAN.getXpath(id))).click();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_ELEMENT("right-column", "//div[@class='%s']"),
        COMPOSITION_TAB("Composition", "//div[contains(@class,'tab') and contains(text(), '%s')]"),
        STRUCTURE_TAB(" Structure", "//div[contains(@class,'tab') and contains(text(), '%s')]"),
        SELF_SPAN("SELF", "//span[@data-tests-id='%s']"),
        INSTANCE_SPAN("//span[@data-tests-id='%s']");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

        public String getXpath(final String... xpathParams) {
            return String.format(xpathFormat, xpathParams);
        }
    }

}
