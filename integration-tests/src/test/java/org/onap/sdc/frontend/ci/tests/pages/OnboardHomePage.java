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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Handles the Onboard Home Page UI test actions
 */
public class OnboardHomePage extends AbstractPageObject {

    private final OnboardHeaderComponent onboardHeaderComponent;
    private WebElement createNewVspBtn;
    private WebElement createNewVlmBtn;

    public OnboardHomePage(final WebDriver webDriver,
                           final OnboardHeaderComponent onboardHeaderComponent) {
        super(webDriver);
        this.onboardHeaderComponent = onboardHeaderComponent;
    }

    @Override
    public void isLoaded() {
        onboardHeaderComponent.isLoaded();
        createNewVspBtn = getWait()
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XpathSelector.ADD_NEW_VSP_BTN.getXpath())));

        createNewVlmBtn = getWait()
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XpathSelector.ADD_NEW_VLM_BTN.getXpath())));
    }

    /**
     * Clicks on the button create new vsp.
     *
     * @return returns the next vsp creation page object
     */
    public VspCreationModal clickOnCreateNewVsp() {
        waitForElementInvisibility(By.xpath(XpathSelector.ONBOARDING_LOADER_DIV.getXpath()));
        createNewVspBtn.click();
        return new VspCreationModal(webDriver);
    }


    /**
     * Clicks on the button create new vlm.
     *
     * @return returns the next vlm creation page object
     */
    public VlmCreationModal clickOnCreateNewVlm() {
        waitForElementInvisibility(By.xpath(XpathSelector.ONBOARDING_LOADER_DIV.getXpath()));
        createNewVlmBtn.click();
        return new VlmCreationModal(webDriver);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        ADD_NEW_VSP_BTN("catalog-add-new-vsp", "//div[@data-test-id='%s']"),
        ADD_NEW_VLM_BTN("catalog-add-new-vlm", "//div[@data-test-id='%s']"),
        ONBOARDING_LOADER_DIV("onboarding-loader-backdrop","//div[@class='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
