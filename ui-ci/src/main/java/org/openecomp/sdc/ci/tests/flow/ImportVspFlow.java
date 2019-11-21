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

package org.openecomp.sdc.ci.tests.flow;

import com.aventstack.extentreports.Status;
import java.util.Optional;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.pages.PageObject;
import org.openecomp.sdc.ci.tests.pages.ResourceCreatePage;
import org.openecomp.sdc.ci.tests.pages.TopNavComponent;
import org.openecomp.sdc.ci.tests.pages.VspRepositoryModalComponent;
import org.openqa.selenium.WebDriver;

/**
 * UI Flow for importing a VSP
 */
public class ImportVspFlow extends AbstractUiTestFlow {

    private final String resourceName;

    public ImportVspFlow(final WebDriver webDriver, final String resourceName) {
        super(webDriver);
        this.resourceName = resourceName;
    }

    @Override
    public Optional<PageObject> run(final PageObject... pageObjects) {
        final VspRepositoryModalComponent vspRepositoryModalComponent = openVspRepository();
        searchForVsp(vspRepositoryModalComponent);
        return Optional.of(importVsp(vspRepositoryModalComponent));
    }

    /**
     * Opens the VSP repository modal by clicking in its icon from the top nav menu.
     *
     * @return the VSP repository modal
     */
    private VspRepositoryModalComponent openVspRepository() {
        extendTest.log(Status.INFO, "Opening the VSP repository");
        final TopNavComponent topNavComponent = new TopNavComponent(webDriver);
        topNavComponent.isLoaded();
        final VspRepositoryModalComponent vspRepositoryModalComponent = topNavComponent.clickOnRepositoryIcon();
        vspRepositoryModalComponent.isLoaded();
        return vspRepositoryModalComponent;
    }

    /**
     * Searches for a VSP in the repository modal.
     *
     * @param vspRepositoryModalComponent the repository modal component
     */
    private void searchForVsp(final VspRepositoryModalComponent vspRepositoryModalComponent) {
        extendTest.log(Status.INFO, String.format("Searching for VSP '%s' in the repository", resourceName));
        vspRepositoryModalComponent.searchForVSP(resourceName);
        ExtentTestActions.takeScreenshot(Status.INFO, "vsp-found-repository",
            String.format("Searching for VSP '%s' found in the repository", resourceName));
    }

    /**
     * Imports the first VSP in the repository list.
     *
     * @param vspRepositoryModalComponent the repository modal component that contains the VSP list
     * @return the resource creation page that the import action redirects
     */
    private ResourceCreatePage importVsp(final VspRepositoryModalComponent vspRepositoryModalComponent) {
        extendTest.log(Status.INFO, String.format("Importing VSP '%s'", resourceName));
        final ResourceCreatePage resourceCreatePage = vspRepositoryModalComponent.clickOnImportVsp(1);
        resourceCreatePage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "vsp-imported",
            String.format("VSP '%s' was imported", resourceName));
        return resourceCreatePage;
    }
}
