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
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceCreateData;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.openqa.selenium.WebDriver;

public class CreateServiceFlow extends AbstractUiTestFlow {

    @Getter
    private final ServiceCreateData serviceCreateData;
    @Getter
    private HomePage homePage;
    private ServiceCreatePage serviceCreatePage;

    public CreateServiceFlow(final WebDriver webDriver, final ServiceCreateData serviceCreateData) {
        super(webDriver);
        this.serviceCreateData = serviceCreateData;
    }

    @Override
    public Optional<ServiceCreatePage> run(final PageObject... pageObjects) {
        Objects.requireNonNull(serviceCreateData);
        extendTest.log(Status.INFO, String.format("Creating Service '%s'", serviceCreateData.getName()));
        homePage = findParameter(pageObjects, HomePage.class);
        homePage.isLoaded();
        serviceCreatePage = homePage.clickOnAddService();
        serviceCreatePage.isLoaded();
        serviceCreatePage.fillForm(serviceCreateData);
        ExtentTestActions.takeScreenshot(Status.INFO, "service-form-filled",
            String.format("Service '%s' form is filled", serviceCreateData.getName()));
        serviceCreatePage.clickOnCreate();
        ExtentTestActions.takeScreenshot(Status.INFO, "service-created",
            String.format("Service '%s' was created", serviceCreateData.getName()));
        return Optional.of(serviceCreatePage);
    }

    @Override
    public Optional<ServiceCreatePage> getLandedPage() {
        return Optional.ofNullable(serviceCreatePage);
    }
}
