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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftwareProductOnboarding extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareProductOnboarding.class);
    private static final String DIV_CLASS_XPATH_FORMAT =
        "//div[contains(concat(' ', normalize-space(@class), ' '), ' %s ')]";
    private static final String DIV_DATA_TEST_ID_XPATH_FORMAT = "//div[@data-test-id='%s']";
    private final VspCommitModal vspCommitModal;
    private WebElement wrappingElement;

    public SoftwareProductOnboarding(final WebDriver webDriver,
                                     final VspCommitModal vspCommitModal) {
        super(webDriver);
        this.vspCommitModal = vspCommitModal;
        timeoutInSeconds = 30;
    }

    @Override
    public void isLoaded() {
        wrappingElement = getWrappingElement();
    }

    public String getResourceName() {
        return wrappingElement.findElement(By.xpath(XpathSelector.NAV_BAR_GROUP_NAME_XPATH.getXpath())).getText();
    }

    public void uploadFile(final String resourceFilePath) {
        LOGGER.debug("Uploading file '{}'", resourceFilePath);
        setInputValue(XpathSelector.FILE_INPUT, resourceFilePath);
    }

    public void attachmentScreenIsLoaded() {
        waitForElementInvisibility(By.xpath(XpathSelector.ONBOARDING_LOADER_DIV.getXpath()));
        final WebElement selectedNavBarGroupItem =
            findSubElement(wrappingElement, XpathSelector.SELECTED_NAV_BAR_GROUP_ITEM.getXpath());
        final String selectedNavBarGroupItemTestId = selectedNavBarGroupItem.getAttribute("data-test-id");

        if (XpathSelector.NAV_BAR_GROUP_ITEM_ATTACHMENT.getId().equals(selectedNavBarGroupItemTestId)) {
            return;
        }

        final var attachmentsTabElements = findSubElements(wrappingElement, By.xpath(XpathSelector.NAV_BAR_GROUP_ITEM_ATTACHMENT.getXpath()));
        if (attachmentsTabElements.isEmpty()) {
            LOGGER.warn("Could not find attachments tab. Selected tab is '{}'", selectedNavBarGroupItemTestId);
            return;
        }

        try {
            attachmentsTabElements.get(0).click();
        } catch (final Exception e) {
            LOGGER.warn("Could not click attachments tab. Selected tab is '{}'", selectedNavBarGroupItemTestId, e);
            return;
        }

        waitForElementInvisibility(By.xpath(XpathSelector.ONBOARDING_LOADER_DIV.getXpath()));
    }

    public void submit() {
        waitForElementInvisibility(By.xpath(XpathSelector.ONBOARDING_LOADER_DIV.getXpath()));
        waitForElementInvisibility(By.className("modal-background"));

        final WebElement submitButton = findSubElement(wrappingElement, XpathSelector.BNT_SUBMIT.getXpath());
        try {
            submitButton.click();
        } catch (final ElementClickInterceptedException e) {
            LOGGER.warn("Submit click intercepted, waiting for overlays and retrying", e);
            waitForElementInvisibility(By.xpath(XpathSelector.ONBOARDING_LOADER_DIV.getXpath()));
            waitForElementInvisibility(By.className("modal-background"));
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({block: 'center'});", submitButton);
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", submitButton);
        }
        vspCommitModal.isLoaded();
        vspCommitModal.fillCommentWithDefaulMessage();
        vspCommitModal.submit();
    }

    public WebElement getWrappingElement() {
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.PAGE_MAIN_DIV.getXpath());
        return waitForElementVisibility(XpathSelector.PAGE_MAIN_DIV.getXpath());

    }

    public void setInputValue(final XpathSelector inputTestId, final String value) {
        findSubElement(wrappingElement, inputTestId.getXpath()).sendKeys(value);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        PAGE_MAIN_DIV("software-product-view", DIV_CLASS_XPATH_FORMAT),
        UPLOAD_CSAR("upload-btn", "//input[@data-test-id='%s']"),
        FILE_INPUT("fileInput", "//input[@name='%s']"),
        NAV_BAR_GROUP_ITEM_ATTACHMENT("navbar-group-item-SOFTWARE_PRODUCT_ATTACHMENTS", DIV_DATA_TEST_ID_XPATH_FORMAT),
        BNT_SUBMIT("vc-submit-btn", DIV_DATA_TEST_ID_XPATH_FORMAT),
        NAV_BAR_GROUP_NAME_XPATH("navbar-group-name", DIV_DATA_TEST_ID_XPATH_FORMAT),
        SELECTED_NAV_BAR_GROUP_ITEM("selected", "//div[contains(@class,'navigation-group-item-name') and contains(@class,'%s')]"),
        ONBOARDING_LOADER_DIV("onboarding-loader-backdrop", DIV_CLASS_XPATH_FORMAT),
        ATTACHMENT_VIEW("vsp-attachments-view", DIV_CLASS_XPATH_FORMAT);

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

    }

}
