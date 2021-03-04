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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.aventstack.extentreports.Status;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.OnboardHomePage;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.onap.sdc.frontend.ci.tests.pages.VlmCreationModal;
import org.onap.sdc.frontend.ci.tests.pages.VlmOverviewPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.openqa.selenium.WebDriver;

public class CreateVlmFlow extends AbstractUiTestFlow {

    private HomePage homePage;

    public CreateVlmFlow(WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public Optional<? extends PageObject> run(PageObject... pageObjects) {
        final TopNavComponent topNavComponent = getParameter(pageObjects, TopNavComponent.class)
            .orElse(new TopNavComponent(webDriver));
        extendTest.log(Status.INFO, "Accessing the Onboard Home Page to create a VLM");
        topNavComponent.isLoaded();
        final OnboardHomePage onboardHomePage = goToOnboardHomePage(topNavComponent);
        final VlmOverviewPage vlmOverviewPage = createNewVlm(onboardHomePage);
        submitVlm(vlmOverviewPage);
        goToHomePage(topNavComponent);
        return Optional.empty();
    }

    @Override
    public Optional<HomePage> getLandedPage() {
        return Optional.ofNullable(homePage);
    }

    /**
     * Goes to the onboard home page by clicking in the onboard tab in the top nav component.
     *
     * @param topNavComponent the top nav component
     * @return the onboard home page
     */
    private OnboardHomePage goToOnboardHomePage(final TopNavComponent topNavComponent) {
        final OnboardHomePage onboardHomePage = topNavComponent.clickOnOnboard();
        onboardHomePage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "onboard-homepage", "Onboard homepage is loaded");
        return onboardHomePage;
    }

    /**
     * Creates a new VLM in the onboard home page.
     *
     * @param onboardHomePage the onboard home page representation
     * @return the Vendor License Model Overview page
     */
    private VlmOverviewPage createNewVlm(final OnboardHomePage onboardHomePage) {
        final String vendorName = new StringBuilder("CiVlm").append(RandomStringUtils.randomAlphabetic(5)).toString();
        extendTest.log(Status.INFO, "Creating a new VLM");
        final VlmCreationModal vlmCreationModal = onboardHomePage.clickOnCreateNewVlm();
        vlmCreationModal.isLoaded();
        vlmCreationModal.fillCreationForm(vendorName, "My New VLM");
        ExtentTestActions.takeScreenshot(Status.INFO, "vlm-creation-form",
            "Creating VLM with given information");
        final VlmOverviewPage vlmOverviewPage = vlmCreationModal.clickOnCreate();
        vlmOverviewPage.isLoaded();
        extendTest.log(Status.INFO, String.format("VLM with vendor name '%s' created", vendorName));
        final String actualVendorName = vlmOverviewPage.getVendorName();
        assertThat(String.format("Should be in the Vendor License Model '%s' page", vendorName),
            actualVendorName, is(vendorName));
        return vlmOverviewPage;
    }

    /**
     * Submits the VLM through the software product view.
     *
     * @param vlmOverviewPage the Vendor Licence Overview page
     */
    private void submitVlm(final VlmOverviewPage vlmOverviewPage) {
        extendTest.log(Status.INFO, "Checking if the VLM Overview page is loaded.");
        ExtentTestActions.takeScreenshot(Status.INFO, "vlm-overview-page-loaded", "The first VLM version was submitted");
        vlmOverviewPage.overviewScreenIsLoaded();
        extendTest.log(Status.INFO, "Submitting the first VLM version.");
        vlmOverviewPage.submit();
        ExtentTestActions.takeScreenshot(Status.INFO, "vlm-submitted", "The first VLM version was submitted");
    }

    /**
     * Go to the system home page through the top nav menu.
     *
     * @param topNavComponent the top nav component
     */
    private void goToHomePage(final TopNavComponent topNavComponent) {
        extendTest.log(Status.INFO, "Accessing the Home page");
        topNavComponent.isLoaded();
        homePage = topNavComponent.clickOnHome();
        homePage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "home-is-loaded", "The Home page is loaded.");
    }

}
