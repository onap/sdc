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

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import java.util.ArrayList;
import java.util.List;

import org.onap.sdc.frontend.ci.tests.datatypes.DirectiveType;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.ServiceDependenciesEditor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the composition page, details panel, Directives and Node Filters tab
 */
public class CompositionDirectiveNodeFilterTab extends AbstractPageObject {

    public CompositionDirectiveNodeFilterTab(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.NODE_FILTER_TAB.xPath));
    }

    /**
     * Select option from the directive ng-select web element
     *
     */
    public void selectDirective() {
        waitToBeClickable(By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_DROPDOWN.getXPath())).click();
        waitToBeClickable(By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_OPTIONS_SELECTABLE.getXPath())).click();
        saveDirectivesSelected();
    }
    /**
     * Select multiple options from the directive ng-select web element
     *
     */
    public void updateDirectives() {
        waitToBeClickable(By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_EDIT_BTN.getXPath())).click();
        waitToBeClickable(By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_DROPDOWN.getXPath())).click();
        waitToBeClickable(By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_OPTIONS_SUBSTITUTABLE.getXPath())).click();
        saveDirectivesSelected();
    }

    /**
     * Returns a ServiceDependenciesEditor when the add node filter button is clicked
     *
     * @param index an index indicating which add node filter button to click
     * @return a new ServiceDependenciesEditor component instance
     */
    public ServiceDependenciesEditor clickAddNodeFilter(final int index) {
        waitForElementInvisibility(By.xpath(XpathSelector.LOADER_HELPER.getXPath()));
        waitForElementVisibility(By.xpath(XpathSelector.ADD_RULE_BUTTON.formatXPath(index))).click();
        return new ServiceDependenciesEditor(webDriver);
    }

    /**
     * Verify a rule has been created
     *
     * @param propertyName name of the created rule
     * @return true if the rule is present on screen otherwise false
     */
    public boolean isRulePresent(final String propertyName) {
        try {
            return waitForElementVisibility(By.xpath(XpathSelector.RULE_DESC.formatXPath(propertyName))) != null;
        } catch (final Exception ignored) {
            return false;
        }
    }

    /**
     * Return all available directive types from the directive ng-select web element
     *
     * @return list of strings in lower case based on visible text of the ng-select's web element options.
     * The List values should correspond to {@link DirectiveType}
     */
    public List<String> getDirectiveSelectOptions() {
        final List<String> directiveOptions =  new ArrayList<>();
        directiveOptions.add(DirectiveType.SELECT.getName());
        directiveOptions.add(DirectiveType.SELECTABLE.getName());
        directiveOptions.add(DirectiveType.SUBSTITUTE.getName());
        directiveOptions.add(DirectiveType.SUBSTITUTABLE.getName());
        return directiveOptions;
    }

    /**
     * Verify a directive has been selected
     *
     * @param type which directive type to verify
     * @return true if the directive type is selected on screen otherwise false
     */
    public boolean isDirectiveSelected(final DirectiveType type) {
        try {
            return waitForElementVisibility(
                    By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_SELECTED
                            .formatXPath(type.getName().toUpperCase())), 2) != null;
        } catch (final Exception ignored) {
            return false;
        }
    }

    private Select getDirectiveSelect() {
        return new Select(findElement(By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_SELECT.xPath)));
    }

    private void saveDirectivesSelected() {
        waitToBeClickable(By.xpath(XpathSelector.NODE_FILTER_DIRECTIVE_APPLY_BTN.formatXPath("Apply"))).click();
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        NODE_FILTER_TAB("//service-dependencies-tab"),
        NODE_FILTER_DIRECTIVE_DROPDOWN("//div[@class='select-directives']//div[@class='ng-input']"),
        NODE_FILTER_DIRECTIVE_SELECT("//select[@id='singleSelect']"),
        NODE_FILTER_DIRECTIVE_SELECTED("//label[contains(text(),': %s')]"),
        NODE_FILTER_DIRECTIVE_OPTIONS_SELECTABLE("//div[@class='select-directives']//*[contains(text(), 'selectable')]"),
        NODE_FILTER_DIRECTIVE_APPLY_BTN("//*[@id=\"multi-select-apply\"]"),
        NODE_FILTER_DIRECTIVE_EDIT_BTN("//*[@data-tests-id='directive-edit-icon']"),
        NODE_FILTER_DIRECTIVE_OPTIONS_SUBSTITUTABLE("//div[@class='select-directives']//*[contains(text(), 'substitutable')]"),
        ADD_RULE_BUTTON("(//*[@data-tests-id='add-rule-button'])[%d]"),
        RULE_DESC("//*[contains(text(),'%s')]"),
        LOADER_HELPER("//div[@class='tlv-loader-back tlv-loader-relative']");

        private final String xPath;

        public String formatXPath(Object value) {
            return String.format(xPath, value);
        }
    }
}
