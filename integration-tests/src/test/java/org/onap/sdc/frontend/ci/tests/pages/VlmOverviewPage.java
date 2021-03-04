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

package org.onap.sdc.frontend.ci.tests.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hamcrest.core.Is;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VlmOverviewPage extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(VlmOverviewPage.class);
    private static final String DIV_CLASS_XPATH_FORMAT = "//div[@class='%s']";
    private static final String DIV_DATA_TEST_ID_XPATH_FORMAT = "//div[@data-test-id='%s']";
    private final VlmSubmitModal vlmSubmitModal;
    private WebElement wrappingElement;

    public VlmOverviewPage(final WebDriver webDriver,
                           final VlmSubmitModal vlmSubmitModal) {
        super(webDriver);
        this.vlmSubmitModal = vlmSubmitModal;
        timeoutInSeconds = 10;
    }

    @Override
    public void isLoaded() {
        wrappingElement = getWrappingElement();
    }

    public void overviewScreenIsLoaded() {
        final String overviewPageXpath = String
            .format("%s%s", VlmOverviewPage.XpathSelector.PAGE_MAIN_DIV.getXpath(), XpathSelector.OVERVIEW_PAGE.getXpath());
        waitForElementVisibility(By.xpath(overviewPageXpath));
        final WebElement selectedNavBarGroupItem =
            findSubElement(wrappingElement, XpathSelector.SELECTED_NAV_BAR_GROUP_ITEM.getXpath());
        final String selectedNavBarGroupItemTestId = selectedNavBarGroupItem.getAttribute("data-test-id");
        assertThat("Overview page should be selected", selectedNavBarGroupItemTestId,
            Is.is(XpathSelector.NAV_BAR_GROUP_ITEM_OVERVIEW.getId()));
    }

    public String getVendorName() {
        return wrappingElement.findElement(By.xpath(XpathSelector.NAV_BAR_GROUP_NAME_XPATH.getXpath())).getText();
    }

    public void submit() {
        findSubElement(wrappingElement, XpathSelector.BNT_SUBMIT.getXpath()).click();
        vlmSubmitModal.isLoaded();
        vlmSubmitModal.confirmSuccess();
    }

    public WebElement getWrappingElement() {
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.PAGE_MAIN_DIV.getXpath());
        return waitForElementVisibility(XpathSelector.PAGE_MAIN_DIV.getXpath());
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        PAGE_MAIN_DIV("software-product-view", DIV_CLASS_XPATH_FORMAT),
        NAV_BAR_GROUP_ITEM_OVERVIEW("navbar-group-item-LICENSE_MODEL_OVERVIEW", DIV_CLASS_XPATH_FORMAT),
        BNT_SUBMIT("vc-submit-btn", DIV_DATA_TEST_ID_XPATH_FORMAT),
        NAV_BAR_GROUP_NAME_XPATH("navbar-group-name", DIV_DATA_TEST_ID_XPATH_FORMAT),
        SELECTED_NAV_BAR_GROUP_ITEM("navigation-group-item-name selected", DIV_CLASS_XPATH_FORMAT),
        OVERVIEW_PAGE("license-model-overview", DIV_CLASS_XPATH_FORMAT);

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

    }

}
