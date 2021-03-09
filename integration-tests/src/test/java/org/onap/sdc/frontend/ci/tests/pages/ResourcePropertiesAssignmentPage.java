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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.NotificationType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Handles the Resource Properties Assignment Page UI actions
 */
public class ResourcePropertiesAssignmentPage extends AbstractPageObject {

    private WebElement wrappingElement;
    private LoaderHelper loaderHelper;
    private NotificationComponent notificationComponent;

    public ResourcePropertiesAssignmentPage(final WebDriver webDriver) {
        super(webDriver);
        notificationComponent = new NotificationComponent(webDriver);
        loaderHelper = new LoaderHelper(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = getWait(5)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XpathSelector.MAIN_DIV.getXpath())));
        getWait(5)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XpathSelector.TITLE_DIV.getXpath())));
    }

    /**
     * Gets the software_version property values.
     *
     * @return the list of software versions found
     */
    public List<String> getSoftwareVersionProperty() {
        waitPropertiesToLoad();
        final By swVersionCheckboxLocator = By.xpath(XpathSelector.SOFTWARE_VERSION_PROPERTY_CHECKBOX.getXpath());
        waitForElementVisibility(swVersionCheckboxLocator, 5);

        final List<String> softwareVersionList = new ArrayList<>();
        final List<WebElement> elements = wrappingElement.findElements(By.xpath(XpathSelector.SOFTWARE_VERSION_INPUT.getXpath()));
        for (final WebElement element : elements) {
            softwareVersionList.add(element.getAttribute("value"));
        }

        return softwareVersionList;
    }

    /**
     * Gets the value of a string TOSCA property.
     *
     * @return the value of the property
     */
    public String getStringPropertyValue(final String propertyName) {
        waitPropertiesToLoad();
        final By propertyCheckboxLocator = By.xpath(XpathSelector.PROPERTY_CHECKBOX.getXpath(propertyName));
        final WebElement propertyCheckbox = waitForElementVisibility(propertyCheckboxLocator, 5);
        final WebElement propertyRow = propertyCheckbox.findElement(By.xpath("./../../.."));
        final WebElement propertyInput = propertyRow.findElement(By.xpath(XpathSelector.INPUT_PROPERTY.getXpath(propertyName)));
        return propertyInput.getAttribute("value");
    }

    /**
     * Set a value to a TOSCA string property.
     */
    public void setStringPropertyValue(final String propertyName, final String value) {
        if (value == null) {
            return;
        }
        waitPropertiesToLoad();
        final By propertyCheckboxLocator = By.xpath(XpathSelector.PROPERTY_CHECKBOX.getXpath(propertyName));
        final WebElement propertyCheckbox = waitForElementVisibility(propertyCheckboxLocator, 5);
        final WebElement propertyRow = propertyCheckbox.findElement(By.xpath("./../../.."));
        final WebElement propertyInput = propertyRow.findElement(By.xpath(XpathSelector.INPUT_PROPERTY.getXpath(propertyName)));
        propertyInput.sendKeys(value);
    }

    public void setPropertyValue(final String propertyName, final Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof String) {
            setStringPropertyValue(propertyName, (String) value);
            return;
        }

        if (value instanceof Integer) {
            setStringPropertyValue(propertyName, ((Integer) value).toString());
            return;
        }

        if (value instanceof Boolean) {
            setStringPropertyValue(propertyName, ((Boolean) value).toString());
            return;
        }

        throw new UnsupportedOperationException("Cannot set property value of type: " + value.getClass());
    }

    /**
     * Checks if a property exists.
     *
     * @return the value of the property
     */
    public boolean isPropertyPresent(final String propertyName) {
        waitPropertiesToLoad();
        try {
            waitForElementVisibility(By.xpath(XpathSelector.PROPERTY_CHECKBOX.getXpath(propertyName)), 5);
        } catch (final Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Waits for the properties loading.
     */
    private void waitPropertiesToLoad() {
        waitForElementVisibility(By.xpath(XpathSelector.PROPERTIES_TABLE.getXpath()), 5);
        waitForElementInvisibility(By.xpath(XpathSelector.NO_DATA_MESSAGE.getXpath()), 5);
    }

    public void saveProperties() {
        final WebElement saveBtn = waitForElementVisibility(By.xpath(XpathSelector.PROPERTY_SAVE_BTN.getXpath()));
        assertTrue(saveBtn.isEnabled(), "Property save button should be enabled.");
        saveBtn.click();
        loaderHelper.waitForLoader(20);
        notificationComponent.waitForNotification(NotificationType.SUCCESS, 20);
    }

    /**
     * Creates a map based on property names and data types
     */
    public Map<String, String> getPropertyNamesAndTypes() {
        waitPropertiesToLoad();
        final Map<String, String> namesAndTypes = new HashMap<String, String>();
        final List<WebElement> names = findElements(By.xpath(XpathSelector.PROPERTY_NAMES.getXpath()));
        final List<WebElement> types = findElements(By.xpath(XpathSelector.PROPERTY_TYPES.getXpath()));

        for (int i = 0;i < names.size();i++) {
            namesAndTypes.put(names.get(i).getAttribute("innerText"), types.get(i).getAttribute("innerText"));
        }

        return namesAndTypes;
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_DIV("w-sdc-main-right-container", "//div[@class='%s']"),
        TITLE_DIV("tab-title", "//div[contains(@class,'%s') and contains(text(), 'Properties Assignment')]"),
        PROPERTIES_TABLE("properties-table", "//div[contains(@class,'%s')]"),
        NO_DATA_MESSAGE("no-data", "//div[contains(@class,'%s') and text()='No data to display']"),
        SOFTWARE_VERSION_PROPERTY_CHECKBOX("software_versions", "//checkbox[@data-tests-id='%s']"),
        SOFTWARE_VERSION_INPUT("value-prop-software_versions", "//input[starts-with(@data-tests-id,'%s')]"),
        PROPERTY_CHECKBOX("//checkbox[@data-tests-id='%s']"),
        PROPERTY_SAVE_BTN("properties-save-button", "//button[@data-tests-id='%s']"),
        INPUT_PROPERTY("//input[@data-tests-id='value-prop-%s']"),
        PROPERTY_TYPES("//*[contains(@data-tests-id, 'propertyType')]"),
        PROPERTY_NAMES("//*[contains(@data-tests-id, 'propertyName')]");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

        public String getXpath(final String... xpathParams) {
            return String.format(xpathFormat, xpathParams);
        }
    }

}
