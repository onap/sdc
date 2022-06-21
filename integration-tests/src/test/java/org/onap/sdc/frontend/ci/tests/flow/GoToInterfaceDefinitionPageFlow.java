/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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

import com.aventstack.extentreports.Status;
import java.util.Optional;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionPage;
import org.openqa.selenium.WebDriver;

/**
 * UI Flow to go to the interfaces page of a VFC from the VFC Workspace
 */
public class GoToInterfaceDefinitionPageFlow extends AbstractUiTestFlow {

    private InterfaceDefinitionPage interfaceDefinitionPage;

    public GoToInterfaceDefinitionPageFlow(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public Optional<PageObject> run(final PageObject... pageObjects) {
        extendTest.log(Status.INFO, "Going to access the Interfaces page from the resource Workspace");
        final ComponentPage componentPage = findParameter(pageObjects, ComponentPage.class);
        componentPage.isLoaded();
        interfaceDefinitionPage = componentPage.goToInterfaceDefinition();
        interfaceDefinitionPage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "interface-definition-page", "Interface Definition page loaded");
        return Optional.of(interfaceDefinitionPage);
    }

    @Override
    public Optional<InterfaceDefinitionPage> getLandedPage() {
        return Optional.ofNullable(interfaceDefinitionPage);
    }
}
