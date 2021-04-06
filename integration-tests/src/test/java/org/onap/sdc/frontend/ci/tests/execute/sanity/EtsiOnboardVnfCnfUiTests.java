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

import com.aventstack.extentreports.Status;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceCreateData;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateResourceFromVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateServiceFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVlmFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadToscaCsarFlow;
import org.onap.sdc.frontend.ci.tests.flow.ImportVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
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
    private HomePage homePage;

    @BeforeMethod
    public void init() {
        webDriver = DriverFactory.getDriver();
        topNavComponent = new TopNavComponent(webDriver);
    }

    @Test
    public void createVlm() {
        final CreateVlmFlow createVlmFlow = new CreateVlmFlow(webDriver);
        createVlmFlow.run();
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "etsiVnfCnfOnboardPackages")
    public void onboardEtsiVnfCnfFlow(final String rootFolder, final String vnfFile) {
        setLog(vnfFile);
        final String resourceName = ElementFactory.addRandomSuffixToName(ElementFactory.getResourcePrefix());
        runOnboardEtsiVnfCnf(resourceName, rootFolder, vnfFile);
        runDistribution(resourceName);
    }

    /**
     * Runs ETSI onboarding VNF/CNF UI flow
     *
     * @param resourceName VSP name
     * @param rootFolder VNF/CNF package location
     * @param vnfCnfFile file to be onboarded
     */
    private void runOnboardEtsiVnfCnf(final String resourceName, final String rootFolder, final String vnfCnfFile) {
        final CreateVspFlow createVspFlow = new CreateVspFlow(webDriver, resourceName, vnfCnfFile, rootFolder);
        createVspFlow.run(topNavComponent);
        final ImportVspFlow importVspFlow = new ImportVspFlow(webDriver, resourceName);
        ResourceCreatePage resourceCreatePage = importVspFlow.run()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        final CreateResourceFromVspFlow createResourceFlow = new CreateResourceFromVspFlow(webDriver, resourceName);
        resourceCreatePage = createResourceFlow.run(resourceCreatePage)
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        resourceCreatePage.isLoaded();
        resourceCreatePage.certifyComponent();
        ExtentTestActions.takeScreenshot(Status.INFO, "resource-certified",
            String.format("Resource '%s' was certified", resourceName));
        downloadAndVerifyOnboardedPackage(loadComponentPage());
    }

    private void runDistribution(final String resourceName) {
        final ServiceCreateData serviceCreateData = createServiceFormData();
        final ServiceCreatePage serviceCreatePage = createService(goToHomePage(topNavComponent), serviceCreateData);
        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(serviceCreateData.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.SERVICE);
        final ComponentData componentToAdd = new ComponentData();
        componentToAdd.setName(resourceName);
        componentToAdd.setVersion("1.0");
        componentToAdd.setComponentType(ComponentType.RESOURCE);
        final AddNodeToCompositionFlow addNodeToCompositionFlow = new AddNodeToCompositionFlow(webDriver, parentComponent, componentToAdd);
        ComponentPage componentPage = loadComponentPage();
        final CompositionPage compositionPage = (CompositionPage) addNodeToCompositionFlow.run(componentPage.goToComposition())
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return CompositionPage"));
        compositionPage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "node-added-to-composition",
            String.format("Resource '%s' was added to composition", serviceCreateData.getName()));
        componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        componentPage.certifyComponent();
        ExtentTestActions.takeScreenshot(Status.INFO, "service-certified",
            String.format("Service '%s' was certified", serviceCreateData.getName()));

        downloadAndVerifyOnboardedPackage(componentPage);
    }

    private void downloadAndVerifyOnboardedPackage(final ComponentPage componentPage) {
        final DownloadToscaCsarFlow downloadToscaCsarFlow = downloadToscaCsar(componentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadToscaCsarFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));
        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        verifyOnboardedPackage(downloadedCsarName);
    }

    private ServiceCreatePage createService(final HomePage homePage, final ServiceCreateData serviceCreateData) {
        final CreateServiceFlow createServiceFlow = new CreateServiceFlow(webDriver, serviceCreateData);
        return createServiceFlow.run(homePage)
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ServiceCreatePage"));
    }

    private ServiceCreateData createServiceFormData() {
        final ServiceCreateData serviceCreateData = new ServiceCreateData();
        serviceCreateData.setRandomName(ElementFactory.addRandomSuffixToName(ElementFactory.getServicePrefix()));
        serviceCreateData.setCategory(ServiceCategoriesEnum.E2E_SERVICE.getValue());
        serviceCreateData.setDescription("aDescription");
        return serviceCreateData;
    }

    /**
     * Go to the system home page through the top nav menu.
     *
     * @param topNavComponent the top nav component
     */
    private HomePage goToHomePage(final TopNavComponent topNavComponent) {
        topNavComponent.isLoaded();
        homePage = topNavComponent.clickOnHome();
        homePage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "home-is-loaded", "The Home page is loaded.");
        return homePage;
    }

    /**
     * Loads Component Page
     *
     * @return ComponentPage
     */
    private ComponentPage loadComponentPage() {
        final ComponentPage componentPage = Optional.of(new ComponentPage(webDriver))
        	.orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ComponentPage"));
        componentPage.isLoaded();
        return componentPage;
    }

    /**
     * Download the generated package
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
}
