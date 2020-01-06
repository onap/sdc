/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openecomp.sdc.ci.tests.pages.ResourceCreatePage.XpathSelector.FORM_LIFE_CYCLE_STATE;

import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.utilities.LoaderHelper;
import org.openecomp.sdc.ci.tests.utilities.NotificationHelper;
import org.openecomp.sdc.ci.tests.utilities.NotificationHelper.NotificationType;
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
    private final NotificationHelper notificationHelper;
    private WebElement createBtn;

    public ResourceCreatePage(final WebDriver webDriver, final LoaderHelper loaderHelper,
                              final NotificationHelper notificationHelper) {
        super(webDriver);
        this.loaderHelper = loaderHelper;
        this.notificationHelper = notificationHelper;
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        LOGGER.debug("Waiting for element visibility with xpath '{}'", FORM_LIFE_CYCLE_STATE.getXpath());
        final WebElement lifeCycleState = waitForElementVisibility(FORM_LIFE_CYCLE_STATE.getXpath());
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
        notificationHelper.waitForNotification(NotificationType.SUCCESS, 60);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        CREATE_BTN("create/save", "//button[@data-tests-id='%s']"),
        FORM_LIFE_CYCLE_STATE("formlifecyclestate", "//span[@data-tests-id='%s']");

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
