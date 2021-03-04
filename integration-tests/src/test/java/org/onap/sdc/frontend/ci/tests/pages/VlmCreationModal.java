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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the VLM Creation Modal UI actions
 */
public class VlmCreationModal extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(VlmCreationModal.class);

    private WebElement wrappingElement;

    public VlmCreationModal(final WebDriver webDriver) {
        super(webDriver);
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.MODAL_XPATH.getXpath());
        wrappingElement = waitForElementVisibility(XpathSelector.MODAL_XPATH.getXpath());
    }

    /**
     * Fills VLM mandatory entries
     * @param vendorName  the VLM vendor name
     * @param description the VLM description
     */
    public void fillCreationForm(final String vendorName, final String description) {
        fillVendorName(vendorName);
        fillDescription(description);
    }

    /**
     * Fills the VLM Vendor Name field
     * @param vendorName the VLM vendor name
     */
    private void fillVendorName(final String vendorName) {
        setInputValue(XpathSelector.VENDOR_NAME_TXT, vendorName);
    }

    /**
     * Fills the VLM description field.
     *
     * @param description the VLM description
     */
    public void fillDescription(final String description) {
        setInputValue(XpathSelector.DESCRIPTION_TXT, description);
    }

    /**
     * Sets input value to the given Xpath
     * @param inputTestId the Xpath selected
     * @param value the value
     */
    private void setInputValue(final XpathSelector inputTestId, final String value) {
        findSubElement(wrappingElement, By.xpath(inputTestId.getXpath())).sendKeys(value);
    }

    /**
     * Clicks on the create button.
     * @return the next page object
     */
    public VlmOverviewPage clickOnCreate() {
        clickElement(XpathSelector.CREATE_BTN);
        return new VlmOverviewPage(webDriver, new VlmSubmitModal(webDriver));
    }

    /**
     * Clicks on the given Xpath element
     * @param elementTestId the ui element
     */
    private void clickElement(final XpathSelector elementTestId) {
        wrappingElement.findElement(By.xpath(elementTestId.getXpath())).click();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MODAL_XPATH("license-model-modal", "//div[@class='%s']"),
        VENDOR_NAME_TXT("vendor-name", "//*[@data-test-id='%s']"),
        DESCRIPTION_TXT("vendor-description", "//*[@data-test-id='%s']"),
        CREATE_BTN("form-submit-button", "//*[@data-test-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
