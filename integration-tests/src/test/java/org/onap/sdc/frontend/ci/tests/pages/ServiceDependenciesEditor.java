/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceDependencyProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Represents the Service Dependencies Editor
 */
public class ServiceDependenciesEditor extends AbstractPageObject {

    public ServiceDependenciesEditor(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.SERVICE_DEPENDENCIES_EDITOR.xPath));
    }
    /**
     * Returns a list of strings based on the property UI Select
     * @return List of property names which can be selected
     */
    public List<String> getPropertySelectOptions() {
        return new Select(webDriver.findElement(By.xpath(XpathSelector.SERVICE_PROPERTY_NAME.xPath)))
                .getOptions().stream()
                .map(option -> option.getAttribute("innerText")).collect(Collectors.toList());
    }

    public void addProperty(final ServiceDependencyProperty property) {
        final Select properties = new Select(webDriver.findElement(By.xpath(XpathSelector.SERVICE_PROPERTY_NAME.xPath)));
        properties.selectByVisibleText(property.getName());
        final Select logicalOperator = new Select(webDriver.findElement(By.xpath(XpathSelector.CONSTRAINT_OPERATOR.xPath)));
        logicalOperator.selectByVisibleText(property.getLogicalOperator().getOperator());
        findElement(XpathSelector.VALUE_TYPE_STATIC.getXPath()).click();
        try {
            addRuleAssignedValue(property);
        } catch (Exception e) {
            fail("Failed to add property due to exception while adding rule value :: {}", e);
        }
        webDriver.findElement(By.xpath(XpathSelector.CREATE_BUTTON.xPath)).click();
    }

    private void addRuleAssignedValue(final ServiceDependencyProperty property) throws Exception {
        final var type = property.getType();
        final var value = property.getValue();
        switch (type) {
            case "list":
                addListInput(property.getName(), value);
                break;
            case "map":
                addMapInput(property.getName(), value);
                break;
            default:
                addStringInput(waitForElementVisibility(By.xpath(XpathSelector.RULE_ASSIGNED_VALUE.xPath)), value);
                break;
        }
    }

    private void addStringInput(WebElement element, Object value) {
        if ("select".equals(element.getTagName())) {
            new Select(element).selectByVisibleText(value.toString());
        } else {
            element.sendKeys(value.toString());
        }
    }

    private void addListInput(final String name, final String value) throws Exception {
        final List<?> values = new JsonMapper().readValue(value, List.class);
        final WebElement addToListElement = waitForElementVisibility(By.xpath(XpathSelector.RULE_ASSIGNED_VALUE_ADD_TO_LIST.formatXpath(name)));
        for (int i=0;i<values.size();i++) {
            addToListElement.click();
            addStringInput(waitForElementVisibility(By.xpath(XpathSelector.RULE_ASSIGNED_LIST_VALUE.formatXpath(name, i))), values.get((i)));
        }
    }

    private void addMapInput(final String name, final String value) throws Exception {
        final Map<?, ?> values = new JsonMapper().readValue(value, Map.class);
        int i = 0;
        final WebElement addToListElement = waitForElementVisibility(By.xpath(XpathSelector.RULE_ASSIGNED_VALUE_ADD_TO_LIST.formatXpath(name)));
        for(Entry<?, ?> entry : values.entrySet()) {
            addToListElement.click();
            final List<WebElement> KeyValueInputs = waitForAllElementsVisibility(By.xpath(XpathSelector.RULE_ASSIGNED_MAP_KEY_VALUE.formatXpath(name, i++)));
            addStringInput(KeyValueInputs.get(0), entry.getKey());
            addStringInput(KeyValueInputs.get(1), entry.getValue());
        }
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        SERVICE_DEPENDENCIES_EDITOR("//service-dependencies-editor"),
        SERVICE_PROPERTY_NAME("//*[@data-tests-id='servicePropertyName']/select"),
        CONSTRAINT_OPERATOR("//*[@data-tests-id='constraintOperator']/select"),
        VALUE_TYPE_STATIC("//*[@data-tests-id='value-type-static']"),
        VALUE_TYPE_TOSCA_FUNCTION("//*[@data-tests-id='value-type-tosca-function']"),
        RULE_ASSIGNED_VALUE("//*[@data-tests-id='ruleAssignedValue']//*[self::input or self::select]"),
        RULE_ASSIGNED_VALUE_ADD_TO_LIST("//a[@data-tests-id = 'add-to-list-%s']"),
        RULE_ASSIGNED_LIST_VALUE("//*[@data-tests-id='value-prop-%s.%d']"),
        RULE_ASSIGNED_MAP_KEY_VALUE("//*[contains(@data-tests-id, 'value-prop') and contains(@data-tests-id, '%s.%d')]"),
        CREATE_BUTTON("//button[text()='Create']");

        private final String xPath;

        public String formatXpath(Object... values) {
            return String.format(xPath, values);
        }
    }
}
