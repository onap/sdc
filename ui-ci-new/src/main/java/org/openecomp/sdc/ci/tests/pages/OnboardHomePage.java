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

import static org.openecomp.sdc.ci.tests.pages.OnboardHomePage.XpathSelector.ADD_NEW_VLM_BTN;
import static org.openecomp.sdc.ci.tests.pages.OnboardHomePage.XpathSelector.ADD_NEW_VSP_BTN;

import org.openecomp.sdc.ci.tests.utilities.LoaderHelper;
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
    private final LoaderHelper loaderHelper;

    public OnboardHomePage(final WebDriver webDriver,
                           final OnboardHeaderComponent onboardHeaderComponent,
                           LoaderHelper loaderHelper) {
        super(webDriver);
        this.onboardHeaderComponent = onboardHeaderComponent;
        this.loaderHelper = loaderHelper;
    }

    @Override
    public void isLoaded() {
        loaderHelper.waitForLoader(30);
        onboardHeaderComponent.isLoaded();
        createNewVspBtn = getWait()
            .until(ExpectedConditions.elementToBeClickable(By.xpath(ADD_NEW_VSP_BTN.getXpath())));
        getWait()
            .until(ExpectedConditions.elementToBeClickable(By.xpath(ADD_NEW_VLM_BTN.getXpath())));
    }

    /**
     * Clicks on the button create new vsp.
     *
     * @return returns the next vsp creation page object
     */
    public VspCreationModal clickOnCreateNewVsp() {
        createNewVspBtn.click();
        return new VspCreationModal(webDriver);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        ADD_NEW_VSP_BTN("catalog-add-new-vsp", "//div[@data-test-id='%s']"),
        ADD_NEW_VLM_BTN("catalog-add-new-vlm", "//div[@data-test-id='%s']");

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
