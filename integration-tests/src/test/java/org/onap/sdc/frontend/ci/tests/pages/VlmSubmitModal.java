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
 * Handles the VLM Submit Modal UI actions
 */
public class VlmSubmitModal extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(VlmSubmitModal.class);


    public VlmSubmitModal(final WebDriver webDriver) {
        super(webDriver);
    }

    public void isLoaded() {
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.MODAL_DIV.getXpath());
        waitForElementVisibility(XpathSelector.MODAL_DIV.getXpath());
    }

    /**
     * Confirms the success of the modal submission.
     */
    public void confirmSuccess() {
        final WebElement successModal = waitForElementVisibility(XpathSelector.SUCCESS_MODAL_DIV.getXpath());
        successModal.findElement(By.xpath(XpathSelector.MODAL_CANCEL_BTN.getXpath())).click();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MODAL_DIV("sdc-modal", "//div[contains(@class, '%s')]"),
        SUBMIT_BTN("vc-submit-btn", "//div[@data-test-id='%s']"),
        SUCCESS_MODAL_DIV("sdc-modal-type-info", "//div[contains(@class, '%s')]"),
        MODAL_CANCEL_BTN("sdc-modal-cancel-button", "//button[@data-test-id='%s']");

        @Getter
        private final String id;
        private final String xpath;

        public String getXpath() {
            return String.format(xpath, id);
        }
    }

}
