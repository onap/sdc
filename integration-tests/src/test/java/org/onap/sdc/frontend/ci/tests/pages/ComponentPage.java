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

import org.onap.sdc.frontend.ci.tests.datatypes.LifeCycleStateEnum;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.LoaderHelper;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent;
import org.onap.sdc.frontend.ci.tests.utilities.NotificationComponent.NotificationType;
import org.openqa.selenium.WebDriver;

public class ComponentPage extends AbstractPageObject {

    protected final TopNavComponent topNavComponent;
    protected final ResourceLeftSideMenu resourceLeftSideMenu;
    protected final ResourceWorkspaceTopBarComponent workspaceTopBarComponent;
    protected final LoaderHelper loaderHelper;
    protected final NotificationComponent notificationComponent;

    public ComponentPage(final WebDriver webDriver) {
        super(webDriver);
        topNavComponent = new TopNavComponent(webDriver);
        resourceLeftSideMenu = new ResourceLeftSideMenu(webDriver);
        workspaceTopBarComponent = new ResourceWorkspaceTopBarComponent(webDriver);
        loaderHelper = new LoaderHelper(webDriver);
        notificationComponent = new NotificationComponent(webDriver);
    }

    @Override
    public void isLoaded() {
        topNavComponent.isLoaded();
        resourceLeftSideMenu.isLoaded();
        workspaceTopBarComponent.isLoaded();
    }

    public HomePage goToHomePage() {
        return topNavComponent.clickOnHome();
    }

    public ToscaArtifactsPage goToToscaArtifacts() {
        return resourceLeftSideMenu.clickOnToscaArtifactsMenuItem();
    }

    public InterfaceDefinitionPage goToInterfaceDefinition() {
        return resourceLeftSideMenu.clickOnInterfaceDefinitionMenuItem();
    }

    public CompositionPage goToComposition() {
        return resourceLeftSideMenu.clickOnCompositionMenuItem();
    }

    /**
     * Certifies the resource and wait for success notification.
     */
    public void certifyComponent() {
        workspaceTopBarComponent.certifyResource();
    }

    /**
     * Creates the resource and wait for success notification.
     */
    public void clickOnCreate() {
        workspaceTopBarComponent.clickOnCreate();
        loaderHelper.waitForLoader(60);
        notificationComponent.waitForNotification(NotificationType.SUCCESS, 60);
    }

    public String getLifecycleState() {
        return workspaceTopBarComponent.getLifecycleState();
    }

    public boolean isInDesign() {
        return LifeCycleStateEnum.IN_DESIGN.getValue().equalsIgnoreCase(getLifecycleState());
    }

    public AttributesOutputsPage goToAttributesOutputs() {
        return resourceLeftSideMenu.clickOnAttributesOutputsMenuItem();
    }

    public AttributesPage goToAttributes() {
        return resourceLeftSideMenu.clickOnAttributesMenuItem();
    }

    public <T extends ComponentPage> T clickOnGeneralMenuItem(Class<? extends T> clazz) {
        return resourceLeftSideMenu.clickOnGeneralMenuItem(clazz);
    }

    public ResourcePropertiesAssignmentPage goToPropertiesAssignment() {
        return resourceLeftSideMenu.clickOnPropertiesAssignmentMenuItem();
    }

    public ResourcePropertiesPage goToProperties() {
        return resourceLeftSideMenu.clickOnPropertiesMenuItem();
    }
}
