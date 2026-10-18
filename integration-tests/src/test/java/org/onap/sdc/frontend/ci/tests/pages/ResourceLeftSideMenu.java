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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Handles the Resource Page Left Side Menu UI actions
 */
public class ResourceLeftSideMenu extends AbstractPageObject {

    public ResourceLeftSideMenu(final WebDriver webDriver) {
        super(webDriver);
        timeoutInSeconds = 5;
    }

    @Override
    public void isLoaded() {
        waitToBeClickable(By.xpath(XpathSelector.GENERAL_MENU.getXpath()));
    }

    /**
     * Clicks on the properties assignment menu item.
     *
     * @return the next page object
     */
    public ResourcePropertiesAssignmentPage clickOnPropertiesAssignmentMenuItem() {
        clickOnMenuItem(XpathSelector.PROPERTIES_ASSIGNMENT_MENU);
        return new ResourcePropertiesAssignmentPage(webDriver);
    }

    /**
     * Clicks on the 'Attributes & Outputs' menu item.
     *
     * @return the next page object
     */
    public AttributesOutputsPage clickOnAttributesOutputsMenuItem() {
        clickOnMenuItem(XpathSelector.ATTRIBUTES_OUTPUTS_MENU);
        return new AttributesOutputsPage(webDriver);
    }

    /**
     * Clicks on the 'Attributes' menu item.
     *
     * @return the next page object
     */
    public AttributesPage clickOnAttributesMenuItem() {
        clickOnMenuItem(XpathSelector.ATTRIBUTES_MENU);
        return new AttributesPage(webDriver);
    }

    /**
     * Clicks on the TOSCA artifacts menu item.
     *
     * @return the next page object
     */
    public ToscaArtifactsPage clickOnToscaArtifactsMenuItem() {
        clickOnMenuItem(XpathSelector.TOSCA_ARTIFACTS_MENU);
        return new ToscaArtifactsPage(webDriver);
    }

    /**
     * Clicks on the Interface Definition menu item.
     *
     * @return the next page object
     */
    public InterfaceDefinitionPage clickOnInterfaceDefinitionMenuItem() {
        clickOnMenuItem(XpathSelector.INTERFACE_DEFINITION_MENU);
        return new InterfaceDefinitionPage(webDriver);
    }

    /**
     * Clicks on the 'General' menu item.
     *
     * @return the next page object
     */
    public <T extends ComponentPage> T clickOnGeneralMenuItem(Class<? extends T> clazz) {
        clickOnMenuItem(XpathSelector.GENERAL_MENU);
        return (T) new ComponentPage(webDriver);
    }

    public CompositionPage clickOnCompositionMenuItem() {
        clickOnMenuItem(XpathSelector.COMPOSITION_MENU);
        return new CompositionPage(webDriver);
    }

    public ResourcePropertiesPage clickOnPropertiesMenuItem() {
        clickOnMenuItem(XpathSelector.PROPERTIES_MENU);
        return new ResourcePropertiesPage(webDriver);
    }

    /**
     * Looks the item up afresh on every attempt instead of under a cached sidebar element: HOME renders a
     * {@code div.w-sdc-left-sidebar} too, and right after a tile click that one can still be the match while
     * the workspace is resolving, only to be detached when the workspace replaces it.
     */
    private void clickOnMenuItem(final XpathSelector menuItem) {
        final By locator = By.xpath(menuItem.getXpath());
        getWait().ignoring(StaleElementReferenceException.class).until(driver -> {
            final WebElement item = ExpectedConditions.elementToBeClickable(locator).apply(driver);
            if (item == null) {
                return false;
            }
            item.click();
            return true;
        });
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    private enum XpathSelector {
        PROPERTIES_ASSIGNMENT_MENU("Properties AssignmentLeftSideMenu", "//*[@data-tests-id='%s']"),
        PROPERTIES_MENU("PropertiesLeftSideMenu", "//*[@data-tests-id='%s']"),
        ATTRIBUTES_OUTPUTS_MENU("Attributes & OutputsLeftSideMenu", "//*[@data-tests-id='%s']"),
        ATTRIBUTES_MENU("AttributesLeftSideMenu", "//*[@data-tests-id='%s']"),
        GENERAL_MENU("GeneralLeftSideMenu", "//*[@data-tests-id='%s']"),
        COMPOSITION_MENU("CompositionLeftSideMenu", "//*[@data-tests-id='%s']"),
        REQUIREMENT_CAPABILITY_MENU("Req. & CapabilitiesLeftSideMenu", "//*[@data-tests-id='%s']"),
        TOSCA_ARTIFACTS_MENU("TOSCA ArtifactsLeftSideMenu", "//*[@data-tests-id='%s']"),
        INTERFACE_DEFINITION_MENU("InterfacesLeftSideMenu", "//*[@data-tests-id='%s']");

        @Getter
        private final String id;
        private final String xpathFormat;

        public String getXpath() {
            return String.format(xpathFormat, id);
        }
    }

}
