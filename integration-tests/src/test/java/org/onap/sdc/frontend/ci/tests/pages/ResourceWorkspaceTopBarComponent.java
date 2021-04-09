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
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.NotificationType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the top bar component of the a resource workspace, which contains the resource version, status, progress bar, and some action buttons.
 */
public class ResourceWorkspaceTopBarComponent extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceWorkspaceTopBarComponent.class);

    private final NotificationComponent notificationComponent;

    private WebElement wrappingElement;
    private WebElement lifecycleStateDiv;
    private WebElement versionContainerDiv;
    private WebElement actionButtonsDiv;

    public ResourceWorkspaceTopBarComponent(final WebDriver webDriver) {
        super(webDriver);
        notificationComponent = new NotificationComponent(webDriver);
    }

    @Override
    public void isLoaded() {
        LOGGER.debug("Waiting for element visibility with xpath '{}'", XpathSelector.MAIN_DIV.getXpath());
        wrappingElement = waitForElementVisibility(By.xpath(XpathSelector.MAIN_DIV.getXpath()), 5);
        lifecycleStateDiv = wrappingElement.findElement(By.xpath(XpathSelector.LIFECYCLE_STATE_DIV.getXpath()));
        versionContainerDiv = wrappingElement.findElement(By.xpath(XpathSelector.VERSION_CONTAINER_DIV.getXpath()));
        actionButtonsDiv = wrappingElement.findElement(By.xpath(XpathSelector.ACTION_BUTTON_DIV.getXpath()));
    }

    public void clickOnCreate() {
        waitToBeClickable(XpathSelector.CREATE_BTN.getXpath()).click();
    }

    public ComponentCertificationModal clickOnCertify() {
        waitToBeClickable(XpathSelector.CERTIFY_BTN.getXpath()).click();
        return new ComponentCertificationModal(webDriver);
    }

    /**
     * Certify the resource and wait for success notification.
     */
    public void certifyResource() {
        final ComponentCertificationModal componentCertificationModal = clickOnCertify();
        componentCertificationModal.isLoaded();
        componentCertificationModal.fillComment("Certifying for the UI Integration Test");
        componentCertificationModal.clickOnOkButton();
        notificationComponent.waitForNotification(NotificationType.SUCCESS, 20);
    }

    public String getLifecycleState() {
        return lifecycleStateDiv.findElement(By.xpath(XpathSelector.FORM_LIFE_CYCLE_STATE.getXpath())).getText();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_DIV("sdc-workspace-top-bar", "//div[@class='%s']"),
        LIFECYCLE_STATE_DIV("lifecycle-state", "//div[@class='%s']"),
        VERSION_CONTAINER_DIV("version-container", "//div[@class='%s']"),
        ACTION_BUTTON_DIV("sdc-workspace-top-bar-buttons", "//div[@class='%s']"),
        CREATE_BTN("create/save", "//button[@data-tests-id='%s']"),
        CERTIFY_BTN("certify", "//button[@data-tests-id='%s']"),
        FORM_LIFE_CYCLE_STATE("formlifecyclestate", "//span[@data-tests-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

    }
}
