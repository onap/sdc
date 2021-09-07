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
import org.onap.sdc.frontend.ci.tests.datatypes.CategorySelect;
import org.onap.sdc.frontend.ci.tests.datatypes.VspCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.VspOnboardingProcedure;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOGGER.debug("Finding element with xpath '{}'", XpathSelector.MODAL_XPATH.getXpath());
        wrappingElement = waitForElementVisibility(XpathSelector.MODAL_XPATH.getXpath());
    }

    /**
     * Fills the creation form with the given data.
     *
     * @param vspCreateData the data to fill the Vendor Software Product create form
     */
    public void fillCreationForm(final VspCreateData vspCreateData) {
        fillName(vspCreateData.getName());
        selectVendorOrElseAny(vspCreateData.getVendor());
        selectCategory(vspCreateData.getCategory());
        fillDescription(vspCreateData.getDescription());
        selectOnboardingProcedure(vspCreateData.getOnboardingProcedure());
        selectModel(vspCreateData.getModel());
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
     * Selects the given vendor option. If a null value is given, selects the first option available.
     *
     * @param vendor the vendor option to select
     */
    public void selectVendorOrElseAny(final String vendor) {
        if (vendor == null) {
            selectVendorFirstVendor();
            return;
        }
        setSelectValue(XpathSelector.VENDOR_SELECT, vendor);
    }

    /**
     * Selects the first vendor in the vendor list.
     */
    public void selectVendorFirstVendor() {
        setSelectIndex(XpathSelector.VENDOR_SELECT, 1);
    }

    /**
     * Selects the default model.
     */
    public void selectDefaultModel() {
        clickElement(XpathSelector.DEFAULT_MODEL_RADIO);
    }

    public void selectModel(final String model) {
        if (model == null) {
            selectDefaultModel();
            return;
        }
        clickElement(XpathSelector.OTHER_MODEL_RADIO);
        final WebElement modelSelect = findSubElement(wrappingElement, XpathSelector.MODEL_SELECT.getXpath());
        modelSelect.sendKeys(model);
        modelSelect.sendKeys(Keys.ENTER);
    }

    /**
     * Selects a category in the category list based on the option value.
     *
     * @param categoryOptionValue the option value
     */
    public void selectCategory(final CategorySelect categoryOptionValue) {
        setSelectValue(XpathSelector.CATEGORY_SELECT, categoryOptionValue.getOption());
    }

    /**
     * Selects the network package onboarding procedure option.
     *
     * @param vspOnboardingProcedure the onboarding procedure to select
     */
    public void selectOnboardingProcedure(final VspOnboardingProcedure vspOnboardingProcedure) {
        if (VspOnboardingProcedure.MANUAL == vspOnboardingProcedure) {
            wrappingElement.findElement(By.xpath(XpathSelector.ONBOARDING_MANUAL_PROCEDURE_RADIO.getXpath())).click();
            return;
        }
        if (VspOnboardingProcedure.NETWORK_PACKAGE == vspOnboardingProcedure) {
            wrappingElement.findElement(By.xpath(XpathSelector.ONBOARDING_PACKAGE_PROCEDURE_RADIO.getXpath())).click();
            return;
        }
        throw new UnsupportedOperationException(String.format("Onboarding procedure option '%s' not yet supported", vspOnboardingProcedure));
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
    @AllArgsConstructor
    private enum XpathSelector {
        MODAL_XPATH("software-product-creation-page", "//div[@class='%s']"),
        NAME_TXT("new-vsp-name", "//input[@data-test-id='%s']"),
        VENDOR_SELECT("new-vsp-vendor", "//select[@data-test-id='%s']"),
        CATEGORY_SELECT("new-vsp-category", "//select[@data-test-id='%s']"),
        DESCRIPTION_TXT("new-vsp-description", "//textarea[@data-test-id='%s']"),
        ONBOARDING_PACKAGE_PROCEDURE_RADIO("new-vsp-creation-procedure-heat", "//input[@data-test-id='%s']/parent::label"),
        ONBOARDING_MANUAL_PROCEDURE_RADIO("new-vsp-creation-procedure-manual", "//input[@data-test-id='%s']/parent::label"),
        DEFAULT_MODEL_RADIO("model-option-default", "//input[@data-test-id='%s']/parent::label"),
        OTHER_MODEL_RADIO("model-option-other", "//input[@data-test-id='%s']/parent::label"),
        MODEL_SELECT("model-option-select", "//div[@data-test-id='%s']//input"),
        CREATE_BTN("form-submit-button", "//*[@data-test-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
