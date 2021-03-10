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
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.Is.is;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.datatypes.LifeCycleStateEnum;
import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.NotificationType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the Resource Create Page UI actions
 */
public class ResourceCreatePage extends AbstractPageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCreatePage.class);
    private final LoaderHelper loaderHelper;
    private final NotificationComponent notificationComponent;
    private WebElement createBtn;

    public ResourceCreatePage(final WebDriver webDriver, final LoaderHelper loaderHelper,
                              final NotificationComponent notificationComponent) {
        super(webDriver);
        this.loaderHelper = loaderHelper;
        this.notificationComponent = notificationComponent;
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        LOGGER.debug("Waiting for element visibility with xpath '{}'", XpathSelector.FORM_LIFE_CYCLE_STATE.getXpath());
        final WebElement lifeCycleState = waitForElementVisibility(XpathSelector.FORM_LIFE_CYCLE_STATE.getXpath());
        assertThat("Life cycle state should be as expected",
            lifeCycleState.getText(), is(equalToIgnoringCase(LifeCycleStateEnum.IN_DESIGN.getValue())));
        createBtn = getWait()
            .until(ExpectedConditions.elementToBeClickable(By.xpath(XpathSelector.CREATE_BTN.getXpath())));
    }

    /**
     * Creates the resource and wait for success notification.
     */
    public void createResource() {
        createBtn.click();
        loaderHelper.waitForLoader(60);
        notificationComponent.waitForNotification(NotificationType.SUCCESS, 60);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        CREATE_BTN("create/save", "//button[@data-tests-id='%s']"),
        FORM_LIFE_CYCLE_STATE("formlifecyclestate", "//span[@data-tests-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
