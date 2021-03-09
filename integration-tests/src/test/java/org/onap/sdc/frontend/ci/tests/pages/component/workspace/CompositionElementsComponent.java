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

import java.time.Duration;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositionElementsComponent extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionElementsComponent.class);

    private WebElement wrappingElement;

    public CompositionElementsComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        final By xpath = By.xpath(XpathSelector.MAIN_ELEMENT.getXpath());
        wrappingElement = waitToBeClickable(xpath);
    }

    public Optional<WebElement> searchElement(final String elementName) {
        final WebElement searchElementInput = wrappingElement.findElement(By.xpath(XpathSelector.SEARCH_INPUT.getXpath()));
        searchElementInput.sendKeys(elementName);
        new Actions(webDriver).pause(Duration.ofSeconds(1)).perform();
        try {
            final WebElement accordionElement = waitForElementVisibility("//div[@class='sdc-accordion-header']");
            final String aClass = accordionElement.getAttribute("class");
            if (!aClass.contains("open")) {
                accordionElement.click();
            }
            return Optional.ofNullable(waitToBeClickable(XpathSelector.ELEMENT_DIV.getXpath(elementName)));
        } catch (final Exception e) {
            LOGGER.debug("Could not find element " + elementName, e);
            return Optional.empty();
        }
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_ELEMENT("composition-palette", "//composition-palette"),
        SEARCH_INPUT("searchAsset-input", "//*[@data-tests-id='%s']"),
        ELEMENT_DIV("//*[@data-tests-id='%s']");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

        public String getXpath(final String... params) {
            return String.format(xpathFormat, params);
        }
    }
}
