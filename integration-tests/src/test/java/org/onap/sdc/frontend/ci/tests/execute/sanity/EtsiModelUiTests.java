/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
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
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import com.aventstack.extentreports.Status;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.PackageTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.CategorySelect;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentProperty;
import org.onap.sdc.frontend.ci.tests.datatypes.ModelName;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.VspCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.VspOnboardingProcedure;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CheckComponentPropertiesFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateResourceFromVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateServiceFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVlmFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.ImportVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class EtsiModelUiTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiModelUiTests.class);

    private WebDriver webDriver;
    private String resourceName;

    @Test
    public void etsiNetworkServiceTest() throws UnzipException {
        webDriver = DriverFactory.getDriver();

        createVlm();
        resourceName = createVsp();
        ResourceCreatePage resourceCreatePage = importVsp(resourceName);
        resourceCreatePage = createVf(resourceName, resourceCreatePage);
        resourceCreatePage.isLoaded();
        resourceCreatePage.certifyComponent();
        ExtentTestActions.takeScreenshot(Status.INFO, "resource-certified", String.format("Resource '%s' was certified", resourceName));
        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage = checkVfProperties(resourceCreatePage);
        resourcePropertiesAssignmentPage.isLoaded();
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadCsarArtifact(resourcePropertiesAssignmentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        toscaArtifactsPage.isLoaded();
        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        checkCsarPackage(resourceName, downloadedCsarName);
        toscaArtifactsPage.goToHomePage();
    }

    @Test(dependsOnMethods = "etsiNetworkServiceTest")
    public void createServiceWithModel() throws UnzipException {
        webDriver = DriverFactory.getDriver();
        final ServiceCreateData serviceCreateData = createServiceFormData();
        createService(serviceCreateData);
        //adding vf to composition
        ComponentPage componentPage = loadComponentPage();
        componentPage = addVfToComposition(resourceName, serviceCreateData, componentPage);
        componentPage.isLoaded();
        componentPage.certifyComponent();
        ExtentTestActions.takeScreenshot(Status.INFO, "service-certified", String.format("Service '%s' was certified",
            serviceCreateData.getName()));

        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadCsarArtifact(componentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));
        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        checkCsarPackage(resourceName, downloadedCsarName);
        toscaArtifactsPage.goToHomePage();
    }

    private ResourcePropertiesAssignmentPage checkVfProperties(final ComponentPage componentPage) {
        final Set<ComponentProperty<?>> componentPropertySet = Set.of(
            new ComponentProperty<>("descriptor_id", "descriptor_id"),
            new ComponentProperty<>("descriptor_version", "v1.0.1"),
            new ComponentProperty<>("flavour_description", "flavour_description"),
            new ComponentProperty<>("flavour_id", "flavour_id"),
            new ComponentProperty<>("product_name", "product_name"),
            new ComponentProperty<>("provider", "provider"),
            new ComponentProperty<>("software_version", "v1.0.1"),
            new ComponentProperty<>("vnfm_info", List.of("etsivnfm:v1.0.1"))
        );

        final CheckComponentPropertiesFlow checkComponentPropertiesFlow = new CheckComponentPropertiesFlow(componentPropertySet, webDriver);
        final Optional<ResourcePropertiesAssignmentPage> run = checkComponentPropertiesFlow.run(componentPage);
        return run.orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
    }

    private ResourceCreatePage createVf(final String resourceName, final ResourceCreatePage resourceCreatePage) {
        final CreateResourceFromVspFlow createResourceFlow = new CreateResourceFromVspFlow(webDriver, resourceName);
        final ResourceCreatePage resourceCreatePage1 = createResourceFlow.run(resourceCreatePage)
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        resourceCreatePage1.isLoaded();
        return resourceCreatePage1;
    }

    private ResourceCreatePage importVsp(final String resourceName) {
        final ImportVspFlow importVspFlow = new ImportVspFlow(webDriver, resourceName);
        return importVspFlow.run()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
    }

    private String createVsp() {
        final String resourceName = new ElementFactory().addRandomSuffixToName(new ElementFactory().getResourcePrefix());
        final String virtualLinkableVnf = "Vnf-ETSI-SOL001-2.5.1.csar";
        final String rootFolder = org.onap.sdc.backend.ci.tests.utils.general.FileHandling.getPackageRepositoryPath(PackageTypeEnum.ETSI);
        var vspCreateData = new VspCreateData();
        vspCreateData.setName(resourceName);
        vspCreateData.setCategory(CategorySelect.COMMON_NETWORK_RESOURCES);
        vspCreateData.setDescription("description");
        vspCreateData.setOnboardingProcedure(VspOnboardingProcedure.NETWORK_PACKAGE);
        vspCreateData.setModel(ModelName.ETSI_SOL001_v2_5_1.getName());
        final CreateVspFlow createVspFlow = new CreateVspFlow(webDriver, vspCreateData, virtualLinkableVnf, rootFolder);
        createVspFlow.run(new TopNavComponent(webDriver));
        return resourceName;
    }

    private void createVlm() {
        getExtendTest().log(Status.INFO, "Creating a VLM");
        final CreateVlmFlow createVlmFlow = new CreateVlmFlow(webDriver);
        createVlmFlow.run();
    }

    private DownloadCsarArtifactFlow downloadCsarArtifact(final ComponentPage componentPage) {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.run(componentPage);
        return downloadCsarArtifactFlow;
    }

    private void checkCsarPackage(final String serviceName, final String downloadedCsarName) throws UnzipException {
        final String downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> filesFromZip = FileHandling.getFilesFromZip(downloadFolderPath, downloadedCsarName);
        final String mainDefinitionFileName = String.format("resource-%s-template.yml", serviceName.substring(0, 1).toUpperCase() + serviceName.substring(1).toLowerCase());
        final Path mainDefinitionFilePath = Path.of("Definitions", mainDefinitionFileName);
        final byte[] mainDefinitionFile = filesFromZip.get(mainDefinitionFilePath.toString());
        final Map<String, Object> mainDefinitionYamlMap = loadYamlObject(mainDefinitionFile);
        final Map<String, Object> topologyTemplateTosca = getMapEntry(mainDefinitionYamlMap, "topology_template");
        assertThat(String.format("'%s' should contain a topology_template entry", mainDefinitionFilePath), topologyTemplateTosca, notNullValue());
        final Map<String, Object> substitutionMappingsTosca = getMapEntry(topologyTemplateTosca, "substitution_mappings");
        assertThat(String.format("'%s' should contain a substitution_mappings entry", mainDefinitionFilePath), substitutionMappingsTosca, notNullValue());
        final var nodeType = (String) substitutionMappingsTosca.get("node_type");
        assertThat("substitution_mappings->node_type should be as expected", nodeType, is("org.openecomp.resource.EtsiDummyVnf"));

        final Map<String, Object> nodeTemplatesTosca = getMapEntry(topologyTemplateTosca, "node_templates");
        assertThat(String.format("'%s' should contain a node_templates entry", mainDefinitionFilePath), nodeTemplatesTosca, notNullValue());
        final var expectedNode1 = "external_connection_point";
        assertThat(String.format("'%s' should contain a node_template %s entry", mainDefinitionFilePath, expectedNode1),
            nodeTemplatesTosca, hasKey(expectedNode1));
        final var expectedNode2 = "vnf_virtual_link";
        assertThat(String.format("'%s' should contain a node_template %s entry", mainDefinitionFilePath, expectedNode2),
            nodeTemplatesTosca, hasKey(expectedNode2));
        final var notExpectedNode1 = "etsi_dummy_vnf";
        assertThat(String.format("'%s' should not contain a node_template %s entry, as it represents the substitutable node",
                mainDefinitionFilePath, notExpectedNode1),
            nodeTemplatesTosca, not(hasKey(notExpectedNode1))
        );
    }

    private Map<String, Object> getMapEntry(final Map<String, Object> yamlObj, final String entryName) {
        try {
            return (Map<String, Object>) yamlObj.get(entryName);
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not get the '%s' entry.", entryName);
            LOGGER.error(errorMsg, e);
            fail(errorMsg + "Error message: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> loadYamlObject(final byte[] mainDefinitionFileBytes) {
        return new Yaml().load(new String(mainDefinitionFileBytes));
    }

    private ServiceCreateData createServiceFormData() {
        final ServiceCreateData serviceCreateData = new ServiceCreateData();
        serviceCreateData.setRandomName("CI-Service-For-Model");
        serviceCreateData.setModel(ModelName.ETSI_SOL001_v2_5_1.getName());
        serviceCreateData.setCategory(ServiceCategoriesEnum.ETSI_NFV_NETWORK_SERVICE.getValue());
        serviceCreateData.setDescription("aDescription");
        return serviceCreateData;
    }

    private ServiceCreatePage createService(final ServiceCreateData serviceCreateData) {
        final CreateServiceFlow createServiceFlow = new CreateServiceFlow(webDriver, serviceCreateData);
        return createServiceFlow.run(new HomePage(webDriver))
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ServiceCreatePage"));
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

    private ServiceComponentPage addVfToComposition(final String resourceName, final ServiceCreateData serviceCreateData,
                                                    final ComponentPage componentPage) {
        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(serviceCreateData.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.SERVICE);
        final ComponentData resourceToAdd = new ComponentData();
        resourceToAdd.setName(resourceName);
        resourceToAdd.setVersion("1.0");
        resourceToAdd.setComponentType(ComponentType.RESOURCE);
        CompositionPage compositionPage = componentPage.goToComposition();
        AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToComposition(parentComponent, resourceToAdd, compositionPage);
        addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Could not get the created component instance"));
        final ServiceComponentPage serviceComponentPage = compositionPage.goToServiceGeneral();
        serviceComponentPage.isLoaded();
        return serviceComponentPage;
    }


    public AddNodeToCompositionFlow addNodeToComposition(final ComponentData parentComponent, final ComponentData resourceToAdd,
                                                         CompositionPage compositionPage) {

        final AddNodeToCompositionFlow addNodeToCompositionFlow = new AddNodeToCompositionFlow(webDriver, parentComponent, resourceToAdd);
        addNodeToCompositionFlow.run(compositionPage);
        return addNodeToCompositionFlow;
    }

}

