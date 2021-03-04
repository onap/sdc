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

package org.onap.sdc.frontend.ci.tests.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the VSP Repository Modal UI actions
 */
public class VspRepositoryModalComponent extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(VspRepositoryModalComponent.class);

    private WebElement wrappingElement;

    public VspRepositoryModalComponent(final WebDriver webDriver) {
        super(webDriver);
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        wrappingElement = getWrappingElement();
        GeneralUIUtils.ultimateWait();
        final List<WebElement> vspResultList = wrappingElement
            .findElements(By.className(XpathSelector.RESULTS_CONTAINER_DIV.getId()));
        assertThat("VSP Repository should contain at least one result", vspResultList, is(not(empty())));
    }

    /**
     * Clicks on the Import Vsp button of the given repository item position in the list.
     *
     * @param listPosition the position of the element in the VSP list, starting from 1
     * @return the next page object
     */
    public ResourceCreatePage clickOnImportVsp(final int listPosition) {
        final List<WebElement> vspResultList =
            findSubElements(wrappingElement, By.className(XpathSelector.RESULTS_CONTAINER_DIV.getId()));
        vspResultList.get(listPosition).click();
        GeneralUIUtils.clickOnElementByTestId(XpathSelector.IMPORT_VSP_BTN.getId());
        return new ResourceCreatePage(webDriver, new LoaderHelper(webDriver), new NotificationComponent(webDriver));
    }

    /**
     * Searches for a VSP in the repository list.
     *
     * @param vspName the VSP name to search
     */
    public void searchForVSP(final String vspName) {
        final WebElement searchTxtElement = findSubElement(wrappingElement, By.xpath(XpathSelector.SEARCH_TXT.getXpath()));
        searchTxtElement.sendKeys(vspName);
        GeneralUIUtils.ultimateWait();
    }

    /**
     * Gets the enclosing element of the modal.
     *
     * @return the enclosing element
     */
    public WebElement getWrappingElement() {
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.MODAL_DIV.getXpath());
        return waitForElementVisibility(XpathSelector.MODAL_DIV.getXpath());
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MODAL_DIV("importVspTable", "//*[@data-tests-id='%s']"),
        SEARCH_TXT("onboarding-search-input", "//input[@data-tests-id='%s']"),
        IMPORT_VSP_BTN("import-csar", "//*[@data-tests-id='%s']"),
        RESULTS_CONTAINER_DIV("datatable-body-cell-label", "//datatable-body[contains(@class,'%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
