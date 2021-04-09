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
 * Handles the Commit Modal UI action when certifying a component
 */
public class ComponentCertificationModal extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentCertificationModal.class);

    private WebElement wrappingElement;

    public ComponentCertificationModal(final WebDriver webDriver) {
        super(webDriver);
    }

    public void isLoaded() {
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.MAIN_MODAL_DIV.getXpath());
        waitForElementVisibility(XpathSelector.COMMIT_COMMENT_TXT.getXpath());
        waitToBeClickable(XpathSelector.CANCEL_BUTTON.getXpath());
        wrappingElement = waitForElementVisibility(XpathSelector.MAIN_MODAL_DIV.getXpath());
    }

    /**
     * Fills commit text area with given message.
     *
     * @param comment the comment message
     */
    public void fillComment(final String comment) {
        final WebElement commentTxt = wrappingElement.findElement(By.xpath(XpathSelector.COMMIT_COMMENT_TXT.getXpath()));
        commentTxt.sendKeys(comment);
    }

    /**
     * Click on the Ok button, submitting the certification.
     */
    public void clickOnOkButton() {
        wrappingElement.findElement(By.xpath(XpathSelector.OK_BUTTON.getXpath())).click();
    }

    /**
     * Click on the "x" to close the dialog.
     */
    public void clickOnCloseButton() {
        wrappingElement.findElement(By.xpath(XpathSelector.CLOSE_X_BUTTON.getXpath())).click();
    }

    /**
     * Click on the cancel button, closing the dialog.
     */
    public void clickOnCancelButton() {
        wrappingElement.findElement(By.xpath(XpathSelector.CANCEL_BUTTON.getXpath())).click();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_MODAL_DIV("sdc-modal-type-custom", "//div[contains(@class, '%s')]"),
        COMMIT_COMMENT_TXT("checkindialog", "//textarea[@data-tests-id='%s']"),
        OK_BUTTON("confirm-modal-button-ok", "//button[@data-tests-id='%s']"),
        CANCEL_BUTTON("confirm-modal-button-cancel", "//button[@data-tests-id='%s']"),
        CLOSE_X_BUTTON("confirm-modal-close", "//button[@data-tests-id='%s']");

        @Getter
        private final String id;
        private final String xpath;

        public String getXpath() {
            return String.format(xpath, id);
        }
    }

}
