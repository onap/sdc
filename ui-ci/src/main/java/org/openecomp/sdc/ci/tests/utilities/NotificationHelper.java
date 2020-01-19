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

package org.openecomp.sdc.ci.tests.utilities;

import org.openecomp.sdc.ci.tests.execute.setup.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openecomp.sdc.ci.tests.utilities.NotificationHelper.XpathSelector.MAIN_CONTAINER_DIV;
import static org.openecomp.sdc.ci.tests.utilities.NotificationHelper.XpathSelector.MESSAGE_CONTENT_DIV;
import static org.openecomp.sdc.ci.tests.utilities.NotificationHelper.XpathSelector.MESSAGE_SUCCESS_DIV;

public class NotificationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHelper.class);

    public void waitForNotification(final NotificationType notificationType, final int timeout) {
        final By messageLocator = getMessageLocator(notificationType);
        waitForVisibility(messageLocator, timeout);
        waitForInvisibility(messageLocator, timeout);
    }

    private By getMessageLocator(final NotificationType notificationType) {
        return By.xpath(getMessageXpath(notificationType));
    }

    private String getMessageXpath(final NotificationType notificationType) {
        if (notificationType == NotificationType.SUCCESS) {
            return String.format("%s%s%s", MAIN_CONTAINER_DIV.getXpath(), MESSAGE_CONTENT_DIV.getXpath(), MESSAGE_SUCCESS_DIV.getXpath());
        }

        LOGGER.warn("Xpath for NotificationType {} not yet implemented. Returning empty Xpath.", notificationType);
        return "";
    }

    private void waitForVisibility(By messageLocator, final int timeout) {
        getWait(timeout)
            .until(ExpectedConditions.visibilityOfElementLocated(messageLocator));
    }

    private void waitForInvisibility(By messageLocator, int timeout) {
        getWait(timeout)
            .until(ExpectedConditions.invisibilityOfElementLocated(messageLocator));
    }

    private WebDriverWait getWait(final int timeout) {
        return new WebDriverWait(DriverFactory.getDriver(), timeout);
    }

    public enum XpathSelector {
        MAIN_CONTAINER_DIV("notification-container", "//div[@class='%s']"),
        MESSAGE_CONTENT_DIV("msg-content", "//div[@class='%s']"),
        MESSAGE_SUCCESS_DIV("message", "//div[contains(@class, '%s') and contains(text(),'successfully')]");

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
