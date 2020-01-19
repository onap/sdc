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

package org.openecomp.sdc.ci.tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openecomp.sdc.ci.tests.pages.VspCreationModal.XpathSelector.METHOD_RADIO;
import static org.openecomp.sdc.ci.tests.pages.VspCreationModal.XpathSelector.MODAL_XPATH;

/**
 * Handles the VSP Creation Modal UI actions
 */
public class VspCreationModal extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(VspCreationModal.class);

    private WebElement wrappingElement;

    public VspCreationModal(final WebDriver webDriver) {
        super(webDriver);
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        LOGGER.debug("Finding element with xpath '{}'", MODAL_XPATH.getXpath());
        wrappingElement = waitForElementVisibility(MODAL_XPATH.getXpath());
    }

    /**
     * Fills the creation form for the given vsp name.
     *
     * @param vspName the name of the Vendor Software Product
     */
    public void fillCreationForm(final String vspName) {
        fillName(vspName);
        selectVendorFirstVendor();
        selectCategory("resourceNewCategory.network l4+.common network resources");
        fillDescription(vspName);
        selectNetworkPackageOnboardingProcedure();
    }

    /**
     * Clicks on the create button.
     *
     * @return the next page object
     */
    public SoftwareProductOnboarding clickOnCreate() {
        clickElement(XpathSelector.CREATE_BTN);
        return new SoftwareProductOnboarding(webDriver, new VspCommitModal(webDriver));
    }

    /**
     * Fills the VSP name.
     *
     * @param vspName the VSP name
     */
    public void fillName(final String vspName) {
        setInputValue(XpathSelector.NAME_TXT, vspName);
    }

    /**
     * Fills the VSP description.
     *
     * @param description the VSP description
     */
    public void fillDescription(final String description) {
        setInputValue(XpathSelector.DESCRIPTION_TXT, description);
    }

    /**
     * Selects the first vendor in the vendor list.
     */
    public void selectVendorFirstVendor() {
        setSelectIndex(XpathSelector.VENDOR_SELECT, 1);
    }

    /**
     * Selects a category in the category list based on the option value.
     *
     * @param categoryOptionValue the option value
     */
    public void selectCategory(final String categoryOptionValue) {
        setSelectValue(XpathSelector.CATEGORY_SELECT, categoryOptionValue);
    }

    /**
     * Selects the network package onboarding procedure option.
     */
    public void selectNetworkPackageOnboardingProcedure() {
        wrappingElement.findElement(By.xpath(METHOD_RADIO.getXpath())).click();
    }

    private void setInputValue(final XpathSelector inputTestId, final String value) {
        findSubElement(wrappingElement, By.xpath(inputTestId.getXpath())).sendKeys(value);
    }

    private void setSelectIndex(final XpathSelector inputTestId, final int index) {
        new Select(findSubElement(wrappingElement, By.xpath(inputTestId.getXpath()))).selectByIndex(index);
    }

    private void setSelectValue(final XpathSelector inputTestId, final String value) {
        new Select(findSubElement(wrappingElement, By.xpath(inputTestId.getXpath()))).selectByValue(value);
    }

    private void clickElement(final XpathSelector elementTestId) {
        wrappingElement.findElement(By.xpath(elementTestId.getXpath())).click();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        MODAL_XPATH("software-product-creation-page", "//div[@class='%s']"),
        NAME_TXT("new-vsp-name", "//input[@data-test-id='%s']"),
        VENDOR_SELECT("new-vsp-vendor", "//select[@data-test-id='%s']"),
        CATEGORY_SELECT("new-vsp-category", "//select[@data-test-id='%s']"),
        DESCRIPTION_TXT("new-vsp-description", "//textarea[@data-test-id='%s']"),
        METHOD_RADIO("new-vsp-creation-procedure-heat", "//input[@data-test-id='%s']/parent::label"),
        CREATE_BTN("form-submit-button", "//*[@data-test-id='%s']");

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
