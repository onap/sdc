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

import com.aventstack.extentreports.Status;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Handles the Resource Properties Assignment Page UI actions.
 */
public class ResourcePropertiesAssignmentPage extends ComponentPage {

    private ResourcePropertiesAssignmentTab resourcePropertiesAssignmentTab;
    private ResourcePropertiesAssignmentInputTab resourcePropertiesAssignmentInputTab;

    public ResourcePropertiesAssignmentPage(final WebDriver webDriver) {
        super(webDriver);
        resourcePropertiesAssignmentTab = new ResourcePropertiesAssignmentTab(webDriver);
        resourcePropertiesAssignmentInputTab = new ResourcePropertiesAssignmentInputTab(webDriver);
    }

    @Override
    public void isLoaded() {
        super.isLoaded();
        waitForElementVisibility(XpathSelector.MAIN_DIV.getXpath());
        waitForElementVisibility(XpathSelector.TITLE_DIV.getXpath());
        resourcePropertiesAssignmentTab.isLoaded();
    }

    /**
     * Select the Properties Tab to be displayed
     */
    public void selectPropertiesTab() {
        findElement(XpathSelector.PROPERTIES_TAB.getXpath()).click();
        resourcePropertiesAssignmentTab.isLoaded();
    }

    /**
     * Select the Input Tab to be displayed
     */
    public void selectInputTab() {
        findElement(XpathSelector.INPUT_TAB.getXpath()).click();
        resourcePropertiesAssignmentInputTab.isLoaded();
    }

    public List<String> getSoftwareVersionProperty() {
        return resourcePropertiesAssignmentTab.getSoftwareVersionProperty();
    }

    public void setPropertyValue(final String propertyName, final Object value) {
        resourcePropertiesAssignmentTab.setPropertyValue(propertyName, value);
    }

    public void setInputValue(final String inputName, final Object value) {
        resourcePropertiesAssignmentInputTab.setInputValue(inputName, value);
    }

    /**
     * Retrieves a property value.
     *
     * @param propertyName the property name
     * @return the property value
     */
    public Object getPropertyValue(final String propertyName) {
        return resourcePropertiesAssignmentTab.getPropertyValue(propertyName);
    }

    public boolean isPropertyPresent(final String propertyName) {
        return resourcePropertiesAssignmentTab.isPropertyPresent(propertyName);
    }

    public boolean isInputPresent(final String inputName) {
        return resourcePropertiesAssignmentInputTab.isInputPresent(inputName);
    }

    /**
     * Saves a property
     */
    public void saveProperties() {
        resourcePropertiesAssignmentTab.saveProperties();
    }

    public void saveInputs() {
        resourcePropertiesAssignmentInputTab.saveInputProperties();
    }

    public void addProperties(final Map<String, String> propertiesMap) {
        resourcePropertiesAssignmentTab.addProperties(propertiesMap);
    }

    public void addInputs(final Map<String, String> inputsMap) {
        resourcePropertiesAssignmentInputTab.addInputs(inputsMap);
    }

    public void verifyInputs(final Map<String, String> inputsMap) {
        resourcePropertiesAssignmentInputTab.verifyInputs(inputsMap);
    }

    public Map<String, String> getPropertyNamesAndTypes() {
        return resourcePropertiesAssignmentTab.getPropertyNamesAndTypes();
    }

    public void setInputPropertyMetadata(String name, String key, String value) {
        resourcePropertiesAssignmentInputTab.setInputPropertyMetadata(name, key, value);
    }

    public List<String> getInputPropertyNames() {
        return resourcePropertiesAssignmentInputTab.getInputPropertyNames();
    }

    /**
     * select property
     */
    public void selectProperty(String propertyName) {
        resourcePropertiesAssignmentTab.selectProperty(propertyName);
    }

    public void loadComponentInstanceProperties(final String instanceName) {
        resourcePropertiesAssignmentTab.loadComponentInstanceProperties(instanceName);
    }

    public void clickOnDeclareInput() {
        resourcePropertiesAssignmentTab.clickOnDeclareInput();
    }

    public void loadCompositionTab() {
        resourcePropertiesAssignmentTab.loadCompositionTab();
    }

    public void clickInputTab(String propertyName) {
        waitForElementVisibility(By.xpath(XpathSelector.DECLARE_NOTIFIFICATION.getXpath()));
        ExtentTestActions.takeScreenshot(Status.INFO, "Declare-Input", String.format("Added declared input for property %s", propertyName));
        selectInputTab();
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        MAIN_DIV("w-sdc-main-right-container", "//div[@class='%s']"),
        TITLE_DIV("tab-title", "//div[contains(@class,'%s') and contains(text(), 'Properties Assignment')]"),
        PROPERTIES_TAB("//*[contains(@data-tests-id, 'Properties') and contains(@class, 'tab')]"),
        INPUT_TAB("//*[contains(@data-tests-id, 'Inputs') and contains(@class, 'tab')]"),
        DECLARE_NOTIFIFICATION("//div[@data-tests-id='Inputs']/div[contains(@class, 'tab-indication')]");

        @Getter
        private String id;
        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
