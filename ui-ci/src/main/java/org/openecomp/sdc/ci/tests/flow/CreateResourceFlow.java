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
import org.openqa.selenium.WebDriver;

/**
 * UI Flow for Resource creation
 */
public class CreateResourceFlow extends AbstractUiTestFlow {

    private final String resourceName;

    public CreateResourceFlow(final WebDriver webDriver, final String resourceName) {
        super(webDriver);
        this.resourceName = resourceName;
    }

    @Override
    public Optional<PageObject> run(final PageObject... pageObjects) {
        final ResourceCreatePage resourceCreatePage = findParameter(pageObjects, ResourceCreatePage.class);
        extendTest.log(Status.INFO, String.format("Creating the Resource '%s'", resourceName));
        resourceCreatePage.createResource();
        ExtentTestActions.takeScreenshot(Status.INFO, "resource-created",
            String.format("Resource '%s' was created", resourceName));
        return Optional.empty();
    }

}
