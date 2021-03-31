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

import com.aventstack.extentreports.Status;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.openqa.selenium.WebDriver;

public class CreateVfcFlow extends AbstractUiTestFlow {

    private final ResourceCreateData vfcCreateData;
    private final String fullFileName;
    private ResourceCreatePage vfcCreatePage;

    public CreateVfcFlow(final WebDriver webDriver, final ResourceCreateData vfcCreateData, final String fullFileName) {
        super(webDriver);
        this.vfcCreateData = vfcCreateData;
        this.fullFileName = fullFileName;
    }

    @Override
    public Optional<? extends PageObject> run(final PageObject... pageObjects) {
        Objects.requireNonNull(vfcCreateData);
        extendTest.log(Status.INFO, String.format("Creating VFC '%s'", vfcCreateData.getName()));
        final HomePage homePage = findParameter(pageObjects, HomePage.class);
        homePage.isLoaded();

        vfcCreatePage = homePage.clickOnImportVfc(fullFileName);
        vfcCreatePage.isLoaded();
        vfcCreatePage.fillForm(vfcCreateData);

        vfcCreatePage.clickOnCreate();
        return Optional.of(vfcCreatePage);
    }

    @Override
    public Optional<ResourceCreatePage> getLandedPage() {
        return Optional.ofNullable(vfcCreatePage);
    }

}
