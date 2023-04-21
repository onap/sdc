/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CompositionPropertiesAttributesTab extends AbstractPageObject {

    private WebElement wrapperElement;

    public CompositionPropertiesAttributesTab(WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrapperElement = waitForElementVisibility(By.xpath(XpathSelector.PROPERTIES_TAB.xPath));
    }

    public void clickOnProperty(String propertyName) {
        final WebElement propElement = wrapperElement.findElement(By.xpath(XpathSelector.PROPERTY.formatXPath(propertyName)));
        propElement.click();
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        PROPERTIES_TAB("//properties-tab"),
        PROPERTY("//span[@data-tests-id='%s']");

        private final String xPath;

        public String formatXPath(Object value) {
            return String.format(xPath, value);
        }
    }
}
