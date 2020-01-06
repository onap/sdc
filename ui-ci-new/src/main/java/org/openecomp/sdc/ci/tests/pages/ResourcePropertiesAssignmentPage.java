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

import static org.openecomp.sdc.ci.tests.pages.ResourcePropertiesAssignmentPage.XpathSelector.MAIN_DIV;
import static org.openecomp.sdc.ci.tests.pages.ResourcePropertiesAssignmentPage.XpathSelector.NO_DATA_MESSAGE;
import static org.openecomp.sdc.ci.tests.pages.ResourcePropertiesAssignmentPage.XpathSelector.PROPERTIES_TABLE;
import static org.openecomp.sdc.ci.tests.pages.ResourcePropertiesAssignmentPage.XpathSelector.SOFTWARE_VERSION_INPUT;
import static org.openecomp.sdc.ci.tests.pages.ResourcePropertiesAssignmentPage.XpathSelector.SOFTWARE_VERSION_PROPERTY_CHECKBOX;
import static org.openecomp.sdc.ci.tests.pages.ResourcePropertiesAssignmentPage.XpathSelector.TITLE_DIV;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Handles the Resource Properties Assignment Page UI actions
 */
public class ResourcePropertiesAssignmentPage extends AbstractPageObject {

    private WebElement wrappingElement;

    public ResourcePropertiesAssignmentPage(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = getWait(5)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(MAIN_DIV.getXpath())));
        getWait(5)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TITLE_DIV.getXpath())));
    }

    /**
     * Gets the software_version property values.
     *
     * @return the list of software versions found
     */
    public List<String> getSoftwareVersionProperty() {
        waitPropertiesToLoad();
        final By swVersionCheckboxLocator = By.xpath(SOFTWARE_VERSION_PROPERTY_CHECKBOX.getXpath());
        getWait(5).until(ExpectedConditions.visibilityOfElementLocated(swVersionCheckboxLocator));

        final List<String> softwareVersionList = new ArrayList<>();
        final List<WebElement> elements = wrappingElement.findElements(By.xpath(SOFTWARE_VERSION_INPUT.getXpath()));
        for (final WebElement element : elements) {
            softwareVersionList.add(element.getAttribute("value"));
        }

        return softwareVersionList;
    }

    /**
     * Waits for the properties loading.
     */
    private void waitPropertiesToLoad() {
        getWait(5)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PROPERTIES_TABLE.getXpath())));
        getWait(5)
            .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(NO_DATA_MESSAGE.getXpath())));
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    public enum XpathSelector {
        MAIN_DIV("w-sdc-main-right-container", "//div[@class='%s']"),
        TITLE_DIV("tab-title", "//div[contains(@class,'%s') and contains(text(), 'Properties Assignment')]"),
        PROPERTIES_TABLE("properties-table", "//div[contains(@class,'%s')]"),
        NO_DATA_MESSAGE("no-data", "//div[contains(@class,'%s') and text()='No data to display']"),
        SOFTWARE_VERSION_PROPERTY_CHECKBOX("software_versions", "//checkbox[@data-tests-id='%s']"),
        SOFTWARE_VERSION_INPUT("value-prop-software_versions.", "//input[starts-with(@data-tests-id,'%s')]");

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
