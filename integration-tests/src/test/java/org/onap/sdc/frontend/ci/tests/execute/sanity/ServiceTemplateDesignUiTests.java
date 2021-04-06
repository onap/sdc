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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;

import com.aventstack.extentreports.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.composition.RelationshipInformation;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfcFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadToscaCsarFlow;
import org.onap.sdc.frontend.ci.tests.flow.composition.CreateRelationshipFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class ServiceTemplateDesignUiTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTemplateDesignUiTests.class);

    private WebDriver webDriver;
    private TopNavComponent topNavComponent;
    private HomePage homePage;
    private List<ResourceCreateData> vfcs = new ArrayList<>();
    private ResourceCreateData vfResourceCreateData;
    private ComponentInstance networkFunctionInstance;
    private ComponentInstance networkServiceInstance;

    @BeforeMethod
    public void init() {
        webDriver = DriverFactory.getDriver();
        topNavComponent = new TopNavComponent(webDriver);
        homePage = new HomePage(webDriver);
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "vfcList")
    public void importAndCertifyVfc(final String rootFolder, final String vfcFilename) {
        setLog(vfcFilename);
        final String resourceName = ElementFactory.addRandomSuffixToName(ElementFactory.getResourcePrefix());
        final CreateVfcFlow createVfcFlow = createVFC(rootFolder + vfcFilename, resourceName);
        vfcs.stream().filter(vfc -> vfc.getName().startsWith(resourceName)).findFirst().orElseThrow(
            () -> new UiTestFlowRuntimeException(String.format("VFCs List should contain a VFC with the expected name %s", resourceName)));
        final ResourceCreatePage vfcResourceCreatePage = createVfcFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ResourceCreatePage"));
        vfcResourceCreatePage.isLoaded();
        vfcResourceCreatePage.certifyComponent();
        ExtentTestActions.takeScreenshot(Status.INFO, "vfc-certified",
            String.format("VFC '%s' was certified", resourceName));
    }

    @Test(dependsOnMethods = "importAndCertifyVfc")
    public void runServiceDesign() throws UnzipException {
        final CreateVfFlow createVfFlow = createVF();
        final AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToCompositionAndCreateRelationship(createVfFlow);
        final CompositionPage compositionPage = addNodeToCompositionFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return CompositionPage"));
        compositionPage.isLoaded();
        final ComponentPage componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        downloadAndVerifyCsarPackage(componentPage);
    }

    private CreateVfFlow createVF() {
        final ResourceCreateData vfCreateData = createVfFormData();
        final CreateVfFlow createVfFlow = new CreateVfFlow(webDriver, vfCreateData);
        createVfFlow.run(homePage);
        return createVfFlow;
    }

    private ResourceCreateData createVfFormData() {
        vfResourceCreateData = new ResourceCreateData();
        vfResourceCreateData.setRandomName(ElementFactory.getResourcePrefix() + "-VF");
        vfResourceCreateData.setCategory(ResourceCategoryEnum.GENERIC_ABSTRACT.getSubCategory());
        vfResourceCreateData.setTagList(Arrays.asList(vfResourceCreateData.getName(), "createVF"));
        vfResourceCreateData.setDescription("aDescription");
        vfResourceCreateData.setVendorName("EST");
        vfResourceCreateData.setVendorRelease("4.1.1");
        vfResourceCreateData.setVendorModelNumber("0001");
        return vfResourceCreateData;
    }

    private CreateVfcFlow createVFC(final String vfcFullFilename, final String resourceName) {
        final ResourceCreateData vfcCreateData = createVfcFormData(resourceName);
        final CreateVfcFlow createVfcFlow = new CreateVfcFlow(webDriver, vfcCreateData, vfcFullFilename);
        createVfcFlow.run(homePage);
        ExtentTestActions.takeScreenshot(Status.INFO, "vfc-created", String.format("VFC '%s' was created", resourceName));
        assertThat(vfcs, notNullValue());
        vfcs.add(vfcCreateData);
        return createVfcFlow;
    }

    private ResourceCreateData createVfcFormData(final String resourceName) {
        final ResourceCreateData vfcCreateData = new ResourceCreateData();
        vfcCreateData.setRandomName(resourceName);
        vfcCreateData.setCategory(ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS.getSubCategory());
        vfcCreateData.setTagList(Arrays.asList(vfcCreateData.getName(), "importVFC"));
        vfcCreateData.setDescription("aDescription");
        vfcCreateData.setVendorName("EST");
        vfcCreateData.setVendorRelease("4.1.1");
        vfcCreateData.setVendorModelNumber("0001");
        return vfcCreateData;
    }

    private AddNodeToCompositionFlow addNodeToCompositionAndCreateRelationship(final CreateVfFlow createVfFlow) {
        final ResourceCreatePage resourceCreatePage = createVfFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a ResourceCreatePage"));
        resourceCreatePage.isLoaded();
        assertThat(vfcs, hasSize(2));
        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(vfResourceCreateData.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.RESOURCE);

        // Adds networkFunction to VF composition
        final ComponentData networkFunction = new ComponentData();
        networkFunction.setName(vfcs.get(0).getName());
        networkFunction.setVersion("1.0");
        networkFunction.setComponentType(ComponentType.RESOURCE);
        CompositionPage compositionPage = resourceCreatePage.goToComposition();
        compositionPage.isLoaded();
        AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToComposition(parentComponent, networkFunction, compositionPage);
        networkFunctionInstance = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Could not get the created component instance"));

        // Adds networkService to VF composition
        final ComponentData networkService = new ComponentData();
        networkService.setName(vfcs.get(1).getName());
        networkService.setVersion("1.0");
        networkService.setComponentType(ComponentType.RESOURCE);
        addNodeToCompositionFlow = addNodeToComposition(parentComponent, networkService, compositionPage);
        networkServiceInstance = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Could not get the created component instance"));

        // Creates a dependsOn relationship from networkServiceInstance to networkFunctionInstance
        createRelationship(compositionPage, networkFunctionInstance.getName(), "tosca.capabilities.Node",
            networkServiceInstance.getName(), "tosca.capabilities.Node");

        return addNodeToCompositionFlow;
    }

    public AddNodeToCompositionFlow addNodeToComposition(final ComponentData parentComponent,
                                                         final ComponentData resourceToAdd,
                                                         CompositionPage compositionPage) {
        final AddNodeToCompositionFlow addNodeToCompositionFlow = new AddNodeToCompositionFlow(webDriver, parentComponent, resourceToAdd);
        compositionPage = (CompositionPage) addNodeToCompositionFlow.run(compositionPage)
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected CompositionPage"));
        compositionPage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "node-added-to-composition",
            String.format("Resource '%s' was added to composition", resourceToAdd.getName()));
        return addNodeToCompositionFlow;
    }

    /**
     * Creates a DependsOn relationship between the imported VFCs
     * @param compositionPage Composition Page
     * @param fromComponentInstanceName VFC - Network Function
     * @param fromCapability Node Capability
     * @param toComponentInstanceName  VFC - Network Service
     * @param toRequirement Node Requirement
     */
    private void createRelationship(final CompositionPage compositionPage, final String fromComponentInstanceName,
                                    final String fromCapability, final String toComponentInstanceName, final String toRequirement) {
        final RelationshipInformation relationshipInformation =
            new RelationshipInformation(fromComponentInstanceName, fromCapability, toComponentInstanceName, toRequirement);
        CreateRelationshipFlow createRelationshipFlow = new CreateRelationshipFlow(webDriver, relationshipInformation);
        createRelationshipFlow.run(compositionPage).orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a CompositionPage instance"));
        ExtentTestActions.takeScreenshot(Status.INFO, "relationship",
            String.format("Relationship from networkFunctionInstance '%s' to networkServiceInstanceResource '%s' was created",
                fromComponentInstanceName, toComponentInstanceName));
    }

    private void downloadAndVerifyCsarPackage(final ComponentPage componentPage) throws UnzipException {
        final DownloadToscaCsarFlow downloadToscaCsarFlow = downloadToscaCsar(componentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadToscaCsarFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));
        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        checkCsarPackage(vfResourceCreateData.getName(), downloadedCsarName);
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
     * Checks if the downloaded Tosca csar includes the node templates for the added VFCs,
     * the generated service template declared “tosca_simple_yaml_1_3” as its Tosca version,
     * the generated csar contains the node type definitions for the added VFCs in the Definitions directory,
     * the interface template contains the relationship declaration
     * @param vfResourceName VF created
     * @param downloadedCsarName download Tosca CSAR filename
     * @throws UnzipException
     */
    private void checkCsarPackage(final String vfResourceName, final String downloadedCsarName) throws UnzipException {
        final String downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> filesFromZip = FileHandling.getFilesFromZip(downloadFolderPath, downloadedCsarName);
        final String virtualFunctionName = vfResourceName.replace("-", "").toLowerCase();
        final List<String> expectedDefinitionFolderFileList = getExpectedDefinitionFolderFileList(virtualFunctionName);
        final Map<String, byte[]> expectedFilesFromZipMap = filesFromZip.entrySet().parallelStream().filter(key -> expectedDefinitionFolderFileList.stream()
            .anyMatch(filename -> filename.equalsIgnoreCase(key.getKey()))).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        final String vfResourceTemplateFile = "Definitions/resource-"+ virtualFunctionName +"-template.yml";
        final String generatedTemplateFile = expectedFilesFromZipMap.keySet().stream()
            .filter(filename -> filename.equalsIgnoreCase(vfResourceTemplateFile)).findFirst()
            .orElseThrow(() -> new UiTestFlowRuntimeException(String.format("Resource template file not found %s", vfResourceTemplateFile)));
        final byte[] toscaTemplateGenerated = filesFromZip.get(generatedTemplateFile);
        assertThat(toscaTemplateGenerated, is(notNullValue()));
        verifyGeneratedTemplate(toscaTemplateGenerated, generatedTemplateFile);
        verifyNodesRelationship(expectedFilesFromZipMap, virtualFunctionName, filesFromZip);
    }

    private void verifyGeneratedTemplate(final byte[] generatedTemplateData, final String generatedTemplateFile) {
        final Map<String, Object> templateYamlMap = loadYamlObject(generatedTemplateData);
        final boolean hasToscaDefinitionVersionEntry = templateYamlMap.containsKey("tosca_definitions_version");
        assertThat(String.format("'%s' should contain tosca_definitions_version entry", generatedTemplateFile), hasToscaDefinitionVersionEntry, is(true));
        final String toscaVersion = (String) templateYamlMap.get("tosca_definitions_version");
        assertThat(String.format("'%s' tosca_definitions_version entry should have tosca_simple_yaml_1_3 value", generatedTemplateFile),
            toscaVersion.equalsIgnoreCase("tosca_simple_yaml_1_3"));
        final Map<String, Object> topologyTemplateTosca = getMapEntry(templateYamlMap, "topology_template");
        assertThat(String.format("'%s' should contain a topology_template entry", generatedTemplateFile), topologyTemplateTosca, is(notNullValue()));
        final Map<String, Object> nodeTemplatesTosca = getMapEntry(topologyTemplateTosca, "node_templates");
        assertThat(String.format("'%s' should contain a node_templates entry", generatedTemplateFile), nodeTemplatesTosca, is(notNullValue()));
        final List<String> nodeTemplateFound = nodeTemplatesTosca.keySet().parallelStream().filter(s -> vfcs.stream()
            .anyMatch(vfc -> s.startsWith(vfc.getName()))).collect(Collectors.toList());
        assertThat(String.format("'%s' should contain the node type definitions for the added VFCs '%s'", nodeTemplatesTosca, vfcs), nodeTemplateFound, hasSize(vfcs.size()));
    }

    private void verifyNodesRelationship(final Map<String, byte[]> expectedFilesFromZipMap, final String virtualFunctionName,
                                         final Map<String, byte[]> filesFromZip) {
        final String vfResourceTemplateFile = "Definitions/resource-"+ virtualFunctionName +"-template-interface.yml";
        final String interfaceTemplateFile = expectedFilesFromZipMap.keySet().stream()
            .filter(filename -> filename.equalsIgnoreCase(vfResourceTemplateFile)).findFirst()
            .orElseThrow(() -> new UiTestFlowRuntimeException(String.format("Resource template file not found %s", vfResourceTemplateFile)));
        final byte[] toscaInterfaceTemplateGenerated = filesFromZip.get(interfaceTemplateFile);
        assertThat(toscaInterfaceTemplateGenerated, is(notNullValue()));
        final Map<String, Object> interfaceTemplateYamlMap = loadYamlObject(toscaInterfaceTemplateGenerated);
        final Map<String, Object> nodeTypesYamlMap = getMapEntry(interfaceTemplateYamlMap, "node_types");
        assertThat(String.format("'%s' should contain a node_types entry", interfaceTemplateYamlMap), nodeTypesYamlMap, is(notNullValue()));
        final String result = Arrays.asList(nodeTypesYamlMap.values()).toString();
        assertThat(String.format("'%s' should contain a capabilities entry", nodeTypesYamlMap), result.contains("capabilities"), is(true));
        assertThat(String.format("'%s' should contain a requirements entry", nodeTypesYamlMap), result.contains("requirements"), is(true));
        assertThat(String.format("'%s' should contain a relationship entry", nodeTypesYamlMap), result.contains("relationship"), is(true));
        assertThat(String.format("'%s' should contain a DependsOn relationship value", nodeTypesYamlMap),
            result.contains("tosca.relationships.DependsOn"), is(true));
    }

    private List<String> getExpectedDefinitionFolderFileList(final String vfResourceName) {
        final List<String> expectedDefinitionFolderFileList = new ArrayList<>();
        vfcs.forEach(vfc -> expectedDefinitionFolderFileList.add("Definitions/resource-"+ vfc.getName() +"-template.yml"));
        expectedDefinitionFolderFileList.add("Definitions/resource-"+ vfResourceName +"-template.yml");
        expectedDefinitionFolderFileList.add("Definitions/resource-"+ vfResourceName +"-template-interface.yml");
        return expectedDefinitionFolderFileList;
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

    private Map<String, Object> loadYamlObject(final byte[] definitionYamlFile) {
        return new Yaml().load(new String(definitionYamlFile));
    }

}
