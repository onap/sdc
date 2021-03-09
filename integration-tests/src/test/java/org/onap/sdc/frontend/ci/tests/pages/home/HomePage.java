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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.ResourceWorkspaceTopBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
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
        waitToBeClickable(XpathSelector.HOME_RIGHT_CONTAINER.getXpath());
        waitToBeClickable(XpathSelector.HOME_SIDE_BAR.getXpath());
        topNavComponent.isLoaded();
        topNavComponent.waitRepositoryToBeClickable();
        assertThat("The Home tab should be selected", topNavComponent.isHomeSelected(), is(true));
    }

    /**
     * Clicks on the add service button.
     *
     * @return the following service create page
     */
    public ServiceCreatePage clickOnAddService() {
        clickOnAdd(By.xpath(XpathSelector.ADD_SERVICE_BTN.getXpath()));
        return new ServiceCreatePage(webDriver);
    }

    /**
     * Clicks on the add VF button.
     *
     * @return the following resource create page
     */
    public ResourceCreatePage clickOnAddVf() {
        clickOnAdd(By.xpath(XpathSelector.ADD_VF_BTN.getXpath()));
        return new ResourceCreatePage(webDriver);
    }

    /**
     * Clicks on the add CR button.
     *
     * @return the following resource create page
     */
    public ResourceCreatePage clickOnAddCr() {
        clickOnAdd(By.xpath(XpathSelector.ADD_CR_BTN.getXpath()));
        return new ResourceCreatePage(webDriver);
    }

    /**
     * Clicks on the add PNF button.
     *
     * @return the following resource create page
     */
    public ResourceCreatePage clickOnAddPnf() {
        clickOnAdd(By.xpath(XpathSelector.ADD_PNF_BTN.getXpath()));
        return new ResourceCreatePage(webDriver);
    }

    /**
     * Clicks on the Import VFC button.
     *
     * @return the following resource create page
     */
    public ResourceCreatePage clickOnImportVfc() {
        clickOnImport(By.xpath(XpathSelector.IMPORT_VFC_BTN.getXpath()));
        return new ResourceCreatePage(webDriver);
    }

    public AbstractPageObject clickOnComponent(final String component) {
        WebElement element = waitForElementVisibility(By.xpath(XpathSelector.COMPONENT.getXpath(component)));
        final WebElement componentTypeDiv = element.findElement(By.xpath("./../../../div[contains(@class, 'sdc-tile-header')]/div"));
        final String text = componentTypeDiv.getText();
        element.click();
        if ("S".equals(text)) {
            return new ServiceComponentPage(webDriver, topNavComponent,
                new ResourceLeftSideMenu(webDriver), new ResourceWorkspaceTopBarComponent(webDriver));
        }

        throw new UnsupportedOperationException("Return not yet implemented for " + text);
    }


    private void clickOnAdd(final By locator) {
        hoverToAddArea();
        waitForElementVisibility(locator);
        findElement(locator).click();
    }

    private void clickOnImport(final By locator) {
        hoverToImportArea();
        waitForElementVisibility(locator);
        findElement(locator).click();
    }

    /**
     * Hovers to the Add buttons area.
     *
     * @return the add buttons area element
     */
    public WebElement hoverToAddArea() {
        return hoverTo(By.xpath(XpathSelector.ADD_BUTTONS_AREA.getXpath()));
    }

    /**
     * Hovers to the Import buttons area.
     *
     * @return the Import buttons area element
     */
    public WebElement hoverToImportArea() {
        return hoverTo(By.xpath(XpathSelector.IMPORT_BUTTONS_AREA.getXpath()));
    }


    private WebElement hoverTo(final By locator) {
        final WebElement addButtonsAreaElement = findElement(locator);
        final Actions actions = new Actions(webDriver);
        actions.moveToElement(addButtonsAreaElement).build().perform();
        return addButtonsAreaElement;
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        HOME_RIGHT_CONTAINER("w-sdc-main-right-container", "//div[@class='%s']"),
        HOME_SIDE_BAR("w-sdc-left-sidebar", "//div[@class='%s']"),
        ADD_SERVICE_BTN("createServiceButton", "//*[@data-tests-id='%s']"),
        ADD_VF_BTN("createResourceButton", "//*[@data-tests-id='%s']"),
        ADD_PNF_BTN("createPNFButton", "//*[@data-tests-id='%s']"),
        ADD_CR_BTN("createCRButton", "//*[@data-tests-id='%s']"),
        IMPORT_VFC_BTN("fileimportVFCbutton", "//*[@data-tests-id='%s']"),
        ADD_BUTTONS_AREA("AddButtonsArea", "//*[@data-tests-id='%s']"),
        IMPORT_BUTTONS_AREA("importButtonsArea", "//*[@data-tests-id='%s']"),
        COMPONENT("//*[@data-tests-id='%s']");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

        public String getXpath(String... parameters) {
            return String.format(xpathFormat, parameters);
        }
    }
}
