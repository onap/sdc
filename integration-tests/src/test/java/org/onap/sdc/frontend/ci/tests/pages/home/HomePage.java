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

package org.onap.sdc.frontend.ci.tests.pages.home;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.onap.sdc.frontend.ci.tests.pages.home.HomePage.XpathSelector.ADD_BUTTONS_AREA;
import static org.onap.sdc.frontend.ci.tests.pages.home.HomePage.XpathSelector.ADD_SERVICE_BTN;

import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Represents the user home page
 */
public class HomePage extends AbstractPageObject {

    @Getter
    private final TopNavComponent topNavComponent;

    public HomePage(final WebDriver webDriver, final TopNavComponent topNavComponent) {
        super(webDriver);
        this.topNavComponent = topNavComponent;
    }

    @Override
    public void isLoaded() {
        topNavComponent.isLoaded();
        assertThat("The Home tab should be selected", topNavComponent.isHomeSelected(), is(true));
    }

    /**
     * Clicks on the add service button.
     *
     * @return the following service create page
     */
    public ServiceCreatePage clickOnAddService() {
        hoverToAddArea();
        final By addServiceBtnLocator = By.xpath(ADD_SERVICE_BTN.getXpath());
        waitForElementVisibility(addServiceBtnLocator);
        final WebElement addServiceBtn = findElement(addServiceBtnLocator);
        addServiceBtn.click();
        return new ServiceCreatePage(webDriver);
    }

    /**
     * Hovers to the Add buttons area.
     *
     * @return the add buttons area element
     */
    public WebElement hoverToAddArea() {
        final Actions actions = new Actions(webDriver);
        final By addButtonsAreaLocator = By.xpath(ADD_BUTTONS_AREA.getXpath());
        final WebElement addButtonsAreaElement = findElement(addButtonsAreaLocator);
        actions.moveToElement(addButtonsAreaElement).build().perform();
        return addButtonsAreaElement;
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        ADD_SERVICE_BTN("createServiceButton", "//*[@data-tests-id='%s']"),
        ADD_BUTTONS_AREA("AddButtonsArea", "//*[@data-tests-id='%s']");

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
