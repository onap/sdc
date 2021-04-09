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

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.WebDriver;

/**
 * Represents the relationship wizard dialog that is used when creating a relationship between two nodes in the composition screen.
 */
public class RelationshipWizardComponent extends AbstractPageObject {

    public RelationshipWizardComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(XpathSelector.WIZARD.getXpath());
        waitForElementVisibility(XpathSelector.CANCEL_BUTTON.getXpath());
        waitForElementVisibility(XpathSelector.NEXT_BUTTON.getXpath());
    }

    public void clickOnNext() {
        waitToBeClickable(XpathSelector.NEXT_BUTTON.getXpath()).click();
    }

    public void clickOnBack() {
        waitToBeClickable(XpathSelector.BACK_BUTTON.getXpath()).click();
    }

    public void clickOnCancel() {
        waitToBeClickable(XpathSelector.CANCEL_BUTTON.getXpath()).click();
    }

    public void clickOnFinish() {
        waitToBeClickable(XpathSelector.FINISH_BUTTON.getXpath()).click();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        WIZARD("//multi-steps-wizard"),
        WIZARD_FOOTER("//multi-steps-wizard//div[@class='ng2-modal-footer']"),
        CANCEL_BUTTON(WIZARD_FOOTER.getXpath() + "//button[text()='Cancel']"),
        NEXT_BUTTON(WIZARD_FOOTER.getXpath() + "//div[contains(@class, 'white-arrow-next')]/.."),
        BACK_BUTTON(WIZARD_FOOTER.getXpath() + "//div[contains(@class, 'blue-arrow-back')]/.."),
        FINISH_BUTTON(WIZARD_FOOTER.getXpath() + "//button[contains(text(), 'Finish')]");

        @Getter
        private final String xpath;
    }
}
