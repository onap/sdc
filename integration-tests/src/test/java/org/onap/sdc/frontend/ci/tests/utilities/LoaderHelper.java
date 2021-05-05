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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoaderHelper extends AbstractPageObject {

    private final By loaderLocator = By.xpath(
        "//*[@data-tests-id='loader' or @class='tlv-loader' or @class='sdc-loader' or @class='sdc-loader-global-wrapper sdc-loader-background']");

    public LoaderHelper(final WebDriver webDriver) {
        super(webDriver);
    }

    /**
     * Wait for the loader to be visible and then invisible.
     *
     * @param timeout the time in seconds to wait for the loader.
     * @deprecated this method uses a generic locator to find for the loader, but the selector is not generic enough to every loader and can find
     * unrelated loaders on the screen. Use {@link #waitForLoader(XpathSelector, int)} instead, where it is possible to use a specific loader
     * selector.
     */
    @Deprecated
    public void waitForLoader(final int timeout) {
        waitForElementVisibility(loaderLocator, timeout);
        waitForElementInvisibility(loaderLocator, timeout);
    }

    /**
     * Wait for the loader to be visible and then invisible.
     *
     * @param xpathSelector    the xpath to find the loader
     * @param timeoutInSeconds the time in seconds to wait for the loader.
     */
    public void waitForLoader(final XpathSelector xpathSelector, final int timeoutInSeconds) {
        final By xpath = By.xpath(xpathSelector.getXpath());
        try {
            waitForElementVisibility(xpath, timeoutInSeconds);
        } catch (final Exception ignored) {
            //if no loader is visible anymore, just ignore
        }
        waitForElementInvisibility(xpath, timeoutInSeconds);
    }

    public void waitForLoaderInvisibility(final int timeout) {
        waitForElementInvisibility(loaderLocator, timeout);
    }

    @Override
    public void isLoaded() {
        //is loaded is not applicable to this component.
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    @Getter
    public enum XpathSelector {
        LOADER_WITH_LOADER_BACKGROUND("//*[contains(@class, 'sdc-loader-background')]"),
        SDC_LOADER_LARGE("//div[@data-tests-id='loader']");

        private final String xpath;
    }
}
