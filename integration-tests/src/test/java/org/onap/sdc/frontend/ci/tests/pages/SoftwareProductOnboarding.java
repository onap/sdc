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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hamcrest.core.Is;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftwareProductOnboarding extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareProductOnboarding.class);
    private static final String DIV_CLASS_XPATH_FORMAT = "//div[@class='%s']";
    private static final String DIV_DATA_TEST_ID_XPATH_FORMAT = "//div[@data-test-id='%s']";
    private final VspCommitModal vspCommitModal;
    private WebElement wrappingElement;

    public SoftwareProductOnboarding(final WebDriver webDriver,
                                     final VspCommitModal vspCommitModal) {
        super(webDriver);
        this.vspCommitModal = vspCommitModal;
        timeoutInSeconds = 5;
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
        final String attachmentViewXpath = String.format("%s%s", XpathSelector.PAGE_MAIN_DIV.getXpath(), XpathSelector.ATTACHMENT_VIEW.getXpath());
        waitForElementVisibility(By.xpath(attachmentViewXpath));
        waitForElementInvisibility(By.xpath(XpathSelector.ONBOARDING_LOADER_DIV.getXpath()));
        final WebElement selectedNavBarGroupItem =
            findSubElement(wrappingElement, XpathSelector.SELECTED_NAV_BAR_GROUP_ITEM.getXpath());
        final String selectedNavBarGroupItemTestId = selectedNavBarGroupItem.getAttribute("data-test-id");
        assertThat("Attachment menu should be selected", selectedNavBarGroupItemTestId,
            Is.is(XpathSelector.NAV_BAR_GROUP_ITEM_ATTACHMENT.getId()));
    }

    public void submit() {
        findSubElement(wrappingElement, XpathSelector.BNT_SUBMIT.getXpath()).click();
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
        SELECTED_NAV_BAR_GROUP_ITEM("navigation-group-item-name selected", DIV_CLASS_XPATH_FORMAT),
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
