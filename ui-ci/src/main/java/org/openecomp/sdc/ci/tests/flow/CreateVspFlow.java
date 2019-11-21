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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.aventstack.extentreports.Status;
import java.util.Optional;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.pages.OnboardHomePage;
import org.openecomp.sdc.ci.tests.pages.PageObject;
import org.openecomp.sdc.ci.tests.pages.SoftwareProductOnboarding;
import org.openecomp.sdc.ci.tests.pages.TopNavComponent;
import org.openecomp.sdc.ci.tests.pages.VspCreationModal;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebDriver;

/**
 * UI Flow for VSP creation
 */
public class CreateVspFlow extends AbstractUiTestFlow {

    private final String resourceName;
    private final String packageFile;
    private final String rootFolder;

    public CreateVspFlow(final WebDriver webDriver, final String resourceName, final String packageFile,
                         final String rootFolder) {
        super(webDriver);
        this.resourceName = resourceName;
        this.packageFile = packageFile;
        this.rootFolder = rootFolder;
    }

    @Override
    public Optional<PageObject> run(final PageObject... pageObjects) {
        final TopNavComponent topNavComponent = findParameter(pageObjects, TopNavComponent.class);
        extendTest.log(Status.INFO, "Accessing the Onboard Home Page");
        topNavComponent.isLoaded();
        final OnboardHomePage onboardHomePage = goToOnboardHomePage(topNavComponent);
        final SoftwareProductOnboarding softwareProductOnboarding = createNewVsp(onboardHomePage);
        uploadPackage(softwareProductOnboarding);
        submitVsp(softwareProductOnboarding);
        goToHomePage(topNavComponent);
        return Optional.empty();
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
     * Creates a new VSP in the onboard home page.
     *
     * @param onboardHomePage the onboard home page representation
     * @return the software product onboarding page
     */
    private SoftwareProductOnboarding createNewVsp(final OnboardHomePage onboardHomePage) {
        extendTest.log(Status.INFO, "Creating a new VSP");
        final VspCreationModal vspCreationModal = onboardHomePage.clickOnCreateNewVsp();
        vspCreationModal.isLoaded();
        vspCreationModal.fillCreationForm(resourceName);
        ExtentTestActions.takeScreenshot(Status.INFO, "vsp-creation-form",
            "Creating VSP with given information");
        final SoftwareProductOnboarding softwareProductOnboarding = vspCreationModal.clickOnCreate();
        softwareProductOnboarding.isLoaded();
        extendTest.log(Status.INFO, String.format("VSP '%s' created", resourceName));
        final String actualResourceName = softwareProductOnboarding.getResourceName();
        assertThat(String.format("Should be in the Software Product '%s' page", resourceName),
            actualResourceName, is(resourceName));
        return softwareProductOnboarding;
    }

    /**
     * Uploads a package in the software product onboarding page.
     *
     * @param softwareProductOnboarding the software product onboarding page
     */
    private void uploadPackage(final SoftwareProductOnboarding softwareProductOnboarding) {
        extendTest.log(Status.INFO,
            String.format("Uploading package '%s' to VSP '%s'", packageFile, resourceName)
        );
        softwareProductOnboarding.uploadFile(rootFolder + packageFile);
        softwareProductOnboarding.attachmentScreenIsLoaded();
        extendTest.log(Status.INFO,
            String.format("Package '%s' was uploaded to VSP '%s'.", packageFile, resourceName)
        );
    }

    /**
     * Submits the VSP through the software product onboarding page.
     *
     * @param softwareProductOnboarding the software product onboarding page
     */
    private void submitVsp(final SoftwareProductOnboarding softwareProductOnboarding) {
        extendTest.log(Status.INFO, "Submitting the first VSP version.");
        softwareProductOnboarding.submit();
        ExtentTestActions.takeScreenshot(Status.INFO, "vsp-submitted", "The first VSP version was submitted");
    }

    /**
     * Go to the system home page through the top nav menu.
     *
     * @param topNavComponent the top nav component
     */
    private void goToHomePage(final TopNavComponent topNavComponent) {
        extendTest.log(Status.INFO, "Accessing the Home page to import the created VSP");
        topNavComponent.isLoaded();
        topNavComponent.clickOnHome();
        GeneralUIUtils.ultimateWait();
        topNavComponent.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "home-is-loaded", "The Home page is loaded.");
    }
}
