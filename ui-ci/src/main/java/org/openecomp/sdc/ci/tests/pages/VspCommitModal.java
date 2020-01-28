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

import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openecomp.sdc.ci.tests.pages.VspCommitModal.XpathSelector.COMMIT_AND_SUBMIT_BTN;
import static org.openecomp.sdc.ci.tests.pages.VspCommitModal.XpathSelector.COMMIT_COMMENT_TXT;
import static org.openecomp.sdc.ci.tests.pages.VspCommitModal.XpathSelector.MODAL_CANCEL_BTN;
import static org.openecomp.sdc.ci.tests.pages.VspCommitModal.XpathSelector.MODAL_DIV;
import static org.openecomp.sdc.ci.tests.pages.VspCommitModal.XpathSelector.SUCCESS_MODAL_DIV;

/**
 * Handles the VSP Commit Modal UI actions
 */
public class VspCommitModal extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(VspCommitModal.class);

    private WebElement wrappingElement;

    public VspCommitModal(final WebDriver webDriver) {
        super(webDriver);
    }

    public void isLoaded() {
        LOGGER.debug("Finding element with xpath '{}'", MODAL_DIV.getXpath());
        wrappingElement = waitForElementVisibility(MODAL_DIV.getXpath());
    }

    /**
     * Fills the comment text area with a default message.
     */
    public void fillCommentWithDefaulMessage() {
        final WebElement commentTxt = wrappingElement.findElement(By.xpath(COMMIT_COMMENT_TXT.getXpath()));
        commentTxt.sendKeys("First VSP version");
    }

    /**
     * Clicks on the modal submit and confirms success.
     */
    public void submit() {
        final WebElement commitAndSubmitBtn = wrappingElement.findElement(By.xpath(COMMIT_AND_SUBMIT_BTN.getXpath()));
        commitAndSubmitBtn.click();
        GeneralUIUtils.ultimateWait();
        confirmSuccess();
    }

    /**
     * Confirms the success of the modal submission.
     */
    private void confirmSuccess() {
        final WebElement successModal = getWait()
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SUCCESS_MODAL_DIV.getXpath())));
        successModal.findElement(By.xpath(MODAL_CANCEL_BTN.getXpath())).click();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        MODAL_DIV("sdc-modal-type-custom", "//div[contains(@class, '%s')]"),
        COMMIT_AND_SUBMIT_BTN("form-submit-button", "//button[@data-test-id='%s']"),
        COMMIT_COMMENT_TXT("commit-comment-text", "//textarea[@data-test-id='%s']"),
        SUCCESS_MODAL_DIV("sdc-modal-type-info", "//div[contains(@class, '%s')]"),
        MODAL_CANCEL_BTN("sdc-modal-cancel-button", "//button[@data-test-id='%s']");

        private final String id;
        private final String xpath;

        XpathSelector(final String id, final String xpath) {
            this.id = id;
            this.xpath = xpath;
        }

        public String getId() {
            return id;
        }

        public String getXpath() {
            return String.format(xpath, id);
        }
    }

}
