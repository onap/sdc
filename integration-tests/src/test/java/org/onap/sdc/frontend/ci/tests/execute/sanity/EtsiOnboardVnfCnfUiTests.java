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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.CreateResourceFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVlmFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadToscaCsarFlow;
import org.onap.sdc.frontend.ci.tests.flow.ImportVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.ResourceWorkspaceTopBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EtsiOnboardVnfCnfUiTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiOnboardVnfCnfUiTests.class);

    private WebDriver webDriver;
    private TopNavComponent topNavComponent;

    @BeforeMethod
    public void init() {
        webDriver = DriverFactory.getDriver();
        topNavComponent = new TopNavComponent(webDriver);
    }

    @Test
    public void createVlm() {
        final ExtentTest extendTest = getExtendTest();
        extendTest.log(Status.INFO, String.format("Starting flow to create a VLM"));
        final CreateVlmFlow createVlmFlow = new CreateVlmFlow(webDriver);
        createVlmFlow.run();
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "etsiVnfCnfOnboardPackages")
    public void onboardEtsiVnfCnfFlow(final String rootFolder, final String vnfFile) {
        setLog(vnfFile);
        final String resourceName = ElementFactory.addRandomSuffixToName(ElementFactory.getResourcePrefix());
        runOnboardEtsiVnfCnf(resourceName, rootFolder, vnfFile);
    }

    /**
     * Runs ETSI onboarding VNF/CNF UI flow
     *
     * @param resourceName VSP name
     * @param rootFolder VNF/CNF package location
     * @param vnfCnfFile file to be onboarded
     */
    public void runOnboardEtsiVnfCnf(final String resourceName, final String rootFolder, final String vnfCnfFile) {
        final ExtentTest extendTest = getExtendTest();
        extendTest.log(Status.INFO,
            String.format("Creating VSP '%s' by onboarding ETSI VNF/CNF package '%s'", resourceName, vnfCnfFile));
        final CreateVspFlow createVspFlow = new CreateVspFlow(webDriver, resourceName, vnfCnfFile, rootFolder);
        createVspFlow.run(topNavComponent);

        extendTest.log(Status.INFO, String.format("Importing VSP '%s'", resourceName));
        final ImportVspFlow importVspFlow = new ImportVspFlow(webDriver, resourceName);

        extendTest.log(Status.INFO, "Creating ResourceCreatePage");
        final ResourceCreatePage resourceCreatePage = importVspFlow.run()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));

        extendTest.log(Status.INFO, String.format("Onboarding '%s' package", vnfCnfFile));
        final CreateResourceFlow createResourceFlow = new CreateResourceFlow(webDriver, resourceName);
        createResourceFlow.run(resourceCreatePage);
        extendTest.log(Status.INFO, String.format("Successfully onboarded the package '%s'", vnfCnfFile));

        extendTest.log(Status.INFO, "Loading Component Page");
        final ComponentPage componentPage = loadComponentPage();
        extendTest.log(Status.INFO, "Downloading Tosca CSAR generated");
        final DownloadToscaCsarFlow downloadToscaCsarFlow = downloadToscaCsar(componentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadToscaCsarFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));
        extendTest.log(Status.INFO, "Tosca CSAR was successfully downloaded");

        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        extendTest.log(Status.INFO, String
            .format("Verifying if the onboarded package is included in the downloaded csar '%s'", downloadedCsarName));
        verifyOnboardedPackage(downloadedCsarName);
    }

    /**
     * Loads Component Page
     *
     * @return ComponentPage
     */
    private ComponentPage loadComponentPage() {
        final ResourceLeftSideMenu resourceLeftSideMenu = new ResourceLeftSideMenu(webDriver);
        resourceLeftSideMenu.isLoaded();
        final ResourceWorkspaceTopBarComponent workspaceTopBarComponent = new ResourceWorkspaceTopBarComponent(
            webDriver);
        workspaceTopBarComponent.isLoaded();
        final ComponentPage componentPage = Optional
            .of(new ComponentPage(webDriver, topNavComponent, resourceLeftSideMenu, workspaceTopBarComponent))
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ComponentPage"));
        componentPage.isLoaded();
        return componentPage;
    }

    /**
     * Download the generated packag
     *
     * @return DownloadToscaCsarFlow
     */
    private DownloadToscaCsarFlow downloadToscaCsar(final ComponentPage componentPage) {
        final DownloadToscaCsarFlow downloadToscaCsarFlow = new DownloadToscaCsarFlow(webDriver);
        downloadToscaCsarFlow.run(componentPage);
        return downloadToscaCsarFlow;
    }

    /**
     * Verifies if the onboarded package is included in the downloaded csar
     *
     * @param downloadedCsarName downloaded csar package name
     */
    private void verifyOnboardedPackage(final String downloadedCsarName) {
        final String downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> filesFromZip;
        try {
            filesFromZip = FileHandling.getFilesFromZip(downloadFolderPath, downloadedCsarName);
            final java.util.Optional<String> etsiPackageEntryOpt =
                filesFromZip.keySet().stream().filter(s -> s.startsWith("Artifacts/Deployment/ETSI_PACKAGE"))
                    .findFirst();
            if (etsiPackageEntryOpt.isEmpty()) {
                Assertions.fail("Could not find the  Onboarded Package in Artifacts/Deployment/ETSI_PACKAGE");
            }
        } catch (final UnzipException e) {
            final String errorMsg = "Could not unzip the downloaded csar package";
            LOGGER.info(errorMsg, e);
            fail(String.format("%s Error: %s", errorMsg, e.getMessage()));
        }
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }
}
