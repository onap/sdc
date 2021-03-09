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

import java.util.Optional;

import org.onap.sdc.frontend.ci.tests.datatypes.ServiceDependencyProperty;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ServiceDependenciesEditor;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionSubstitutionFilterTab;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.Status;

public class CreateSubtitutionFilterFlow extends AbstractUiTestFlow {

    private CompositionPage compositionPage;
    private final ServiceDependencyProperty substitutionFilterProperty;

    public CreateSubtitutionFilterFlow(final WebDriver webDriver, final ServiceDependencyProperty substitutionFilterProperty) {
        super(webDriver);
        this.substitutionFilterProperty = substitutionFilterProperty;
    }

    @Override
    public Optional<PageObject> run(final PageObject... pageObjects) {
        extendTest.log(Status.INFO, "Creating substitution filter");

        compositionPage = getCompositionPage(pageObjects);
        compositionPage.isLoaded();

        final CompositionDetailSideBarComponent sideBar = compositionPage.getDetailSideBar();
        sideBar.isLoaded();

        final CompositionSubstitutionFilterTab compositionSubstitutionFilterTab = (CompositionSubstitutionFilterTab) sideBar.selectTab(CompositionDetailTabName.SUBSTITUTION_FILTER);
        compositionSubstitutionFilterTab.isLoaded();

        final ServiceDependenciesEditor compositionSubstitutionDependenciesEditor = compositionSubstitutionFilterTab.clickAddSubstitutionFilter();
        compositionSubstitutionDependenciesEditor.isLoaded();
        compositionSubstitutionDependenciesEditor.addProperty(substitutionFilterProperty);

        compositionSubstitutionFilterTab.isLoaded();
        assertTrue(compositionSubstitutionFilterTab.isSubstitutionFilterPresent(substitutionFilterProperty.getName()), "Created substitution filter is not present");

        return Optional.of(compositionPage);
    }

    @Override
    public Optional<? extends PageObject> getLandedPage() {
        return Optional.ofNullable(compositionPage);
    }

    private CompositionPage getCompositionPage(final PageObject... pageObjects) {
        return getParameter(pageObjects, CompositionPage.class)
        .orElseGet(() -> {
            return new CompositionPage(webDriver);
        });
    }
}
