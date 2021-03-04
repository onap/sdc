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

package org.onap.sdc.frontend.ci.tests.flow;

import java.util.Optional;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.ResourceWorkspaceTopBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.openqa.selenium.WebDriver;

public class ComponentFlow extends AbstractUiTestFlow {

    private ComponentPage componentPage;

    public ComponentFlow(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public Optional<ComponentPage> run(final PageObject... pageObjects) {
        componentPage = getParameter(pageObjects, ComponentPage.class)
            .orElseGet(() -> {
                final TopNavComponent topNavComponent = getParameter(pageObjects, TopNavComponent.class)
                    .orElse(new TopNavComponent(webDriver));
                final ResourceLeftSideMenu resourceLeftSideMenu =
                    getParameter(pageObjects, ResourceLeftSideMenu.class).orElse(new ResourceLeftSideMenu(webDriver));
                final ResourceWorkspaceTopBarComponent workspaceTopBarComponent =
                    getParameter(pageObjects, ResourceWorkspaceTopBarComponent.class)
                        .orElse(new ResourceWorkspaceTopBarComponent(webDriver));
                return new ComponentPage(webDriver, topNavComponent, resourceLeftSideMenu, workspaceTopBarComponent);
            });
        componentPage.isLoaded();
        return Optional.of(componentPage);
    }

    @Override
    public Optional<ComponentPage> getLandedPage() {
        return Optional.ofNullable(componentPage);
    }
}
