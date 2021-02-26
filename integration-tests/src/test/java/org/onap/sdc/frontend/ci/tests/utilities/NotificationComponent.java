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

package org.onap.sdc.frontend.ci.tests.utilities;

import static org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.XpathSelector.MAIN_CONTAINER_DIV;
import static org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.XpathSelector.MESSAGE_CONTENT_DIV;
import static org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.XpathSelector.MESSAGE_SUCCESS_DIV;

import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationComponent extends AbstractPageObject {

    public NotificationComponent(final WebDriver webDriver) {
        super(webDriver);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationComponent.class);

    public void waitForNotification(final NotificationType notificationType, final int timeout) {
        final By messageLocator = getMessageLocator(notificationType);
        final WebElement webElement = waitForElementVisibility(messageLocator, timeout);
        webElement.click();
        waitForElementInvisibility(messageLocator, 5);
    }

    private By getMessageLocator(final NotificationType notificationType) {
        return By.xpath(getMessageXpath(notificationType));
    }

    private String getMessageXpath(final NotificationType notificationType) {
        if (notificationType == NotificationType.SUCCESS) {
            return String.format("%s%s%s", MAIN_CONTAINER_DIV.getXpath(), MESSAGE_CONTENT_DIV.getXpath(), MESSAGE_SUCCESS_DIV.getXpath());
        }

        LOGGER.warn("Xpath for NotificationType {} not yet implemented.", notificationType);
        return "notYetImplemented";
    }

    @Override
    public void isLoaded() {
        //will not be loaded when needed
    }

    public enum XpathSelector {
        MAIN_CONTAINER_DIV("notification-container", "//div[@class='%s']"),
        MESSAGE_CONTENT_DIV("msg-content", "//div[@class='%s']"),
        MESSAGE_SUCCESS_DIV("message", "//div[contains(@class, 'message') and (contains(text(),'successfully') or contains(text(), 'Successfully'))]");

        private final String id;
        private final String xpath;

        XpathSelector(String id, String xpath) {
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

    public enum NotificationType {
        SUCCESS;
    }

}
