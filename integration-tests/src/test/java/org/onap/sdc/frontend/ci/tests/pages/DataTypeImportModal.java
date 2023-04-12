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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Handles the Resource Create Page UI actions
 */
public class DataTypeImportModal extends ComponentPage {

    public DataTypeImportModal(final WebDriver webDriver) {
        super(webDriver);
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        super.isLoaded();
        waitForElementVisibility(By.xpath(XpathSelector.MODEL_SELECT.getXpath()));
    }

    /**
     * Gets the name field value.
     *
     * @return the name field value
     */
    public String uploadDataTypeFile() {
        return null;
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MODEL_SELECT("selectModelName", "//select[@data-tests-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
