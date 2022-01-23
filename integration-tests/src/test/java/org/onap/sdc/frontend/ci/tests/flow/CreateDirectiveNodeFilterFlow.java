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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.onap.sdc.frontend.ci.tests.datatypes.ServiceDependencyProperty;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ServiceDependenciesEditor;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDirectiveNodeFilterTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.Status;

import lombok.Getter;

public class CreateDirectiveNodeFilterFlow extends AbstractUiTestFlow {

    @Getter
    private List<String> directiveOptions;
    @Getter
    private List<String> propertyOptions;

    private final int buttonIndex;
    private final ServiceDependencyProperty serviceDependencyProperty;
    private CompositionPage compositionPage;

    public CreateDirectiveNodeFilterFlow(final WebDriver webDriver, final int buttonIndex, final ServiceDependencyProperty serviceDependencyProperty) {
        super(webDriver);
        this.buttonIndex = buttonIndex;
        this.serviceDependencyProperty = serviceDependencyProperty;
    }

    @Override
    public Optional<CompositionPage> run(final PageObject... pageObjects) {
        extendTest.log(Status.INFO, "Creating directive node filter");

        compositionPage = getCompositionPage(pageObjects);
        compositionPage.isLoaded();

        final CompositionDetailSideBarComponent sideBar = compositionPage.getDetailSideBar();
        sideBar.isLoaded();
        final CompositionDirectiveNodeFilterTab compositionDirectiveNodeFilterTab = (CompositionDirectiveNodeFilterTab)
                sideBar.selectTab(CompositionDetailTabName.DIRECTIVE_NODE_FILTER);
        compositionDirectiveNodeFilterTab.isLoaded();
        directiveOptions = compositionDirectiveNodeFilterTab.getDirectiveSelectOptions();
        compositionDirectiveNodeFilterTab.selectDirective();
        compositionDirectiveNodeFilterTab.updateDirectives();
        ExtentTestActions.takeScreenshot(Status.INFO, "multiple directives", String.format("multiple directives created"));

        final ServiceDependenciesEditor compositionDependenciesEditor = compositionDirectiveNodeFilterTab.clickAddNodeFilter(buttonIndex);
        compositionDependenciesEditor.isLoaded();
        propertyOptions = compositionDependenciesEditor.getPropertySelectOptions();
        compositionDependenciesEditor.addProperty(serviceDependencyProperty);
        assertTrue(compositionDirectiveNodeFilterTab.isRulePresent(serviceDependencyProperty.getName()), "Created Directive Node filter is not present");
        ExtentTestActions.takeScreenshot(Status.INFO, serviceDependencyProperty.getName(), "Created directive node filter");
        return Optional.of(compositionPage);
    }

    @Override
    public Optional<CompositionPage> getLandedPage() {
        return Optional.ofNullable(compositionPage);
    }

    private CompositionPage getCompositionPage(final PageObject... pageObjects) {
        return getParameter(pageObjects, CompositionPage.class).orElse(new CompositionPage(webDriver));
    }
}
