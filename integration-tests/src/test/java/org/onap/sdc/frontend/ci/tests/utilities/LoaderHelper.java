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

import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoaderHelper extends AbstractPageObject {

    private final By loaderLocator = By.xpath("//*[@data-tests-id='loader' or @class='tlv-loader' or @class='sdc-loader' or @class='sdc-loader-global-wrapper sdc-loader-background']");

    public LoaderHelper(final WebDriver webDriver) {
        super(webDriver);
    }

    public void waitForLoader(final int timeout) {
        waitForElementVisibility(loaderLocator, 5);
        waitForElementInvisibility(loaderLocator, timeout);
    }

    public void waitForLoaderInvisibility(final int timeout) {
        waitForElementInvisibility(loaderLocator, timeout);
    }

    @Override
    public void isLoaded() {

    }
}
