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

import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.openqa.selenium.WebDriver;

public class ComponentPage extends AbstractPageObject {

    private final TopNavComponent topNavComponent;
    private final ResourceLeftSideMenu resourceLeftSideMenu;
    private final ResourceWorkspaceTopBarComponent workspaceTopBarComponent;

    public ComponentPage(final WebDriver webDriver) {
        super(webDriver);
        this.topNavComponent = new TopNavComponent(webDriver);
        this.resourceLeftSideMenu = new ResourceLeftSideMenu(webDriver);
        this.workspaceTopBarComponent = new ResourceWorkspaceTopBarComponent(webDriver);
    }

    @Override
    public void isLoaded() {
        topNavComponent.isLoaded();
        resourceLeftSideMenu.isLoaded();
        workspaceTopBarComponent.isLoaded();
    }

    public ToscaArtifactsPage goToToscaArtifacts() {
        return resourceLeftSideMenu.clickOnToscaArtifactsMenuItem();
    }
}
