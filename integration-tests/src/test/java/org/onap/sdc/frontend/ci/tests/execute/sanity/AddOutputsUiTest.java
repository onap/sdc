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
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.aventstack.extentreports.Status;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfcFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.AttributesOutputsPage;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class AddOutputsUiTest extends SetupCDTest {

    private String filePath;
    private WebDriver webDriver;
    private HomePage homePage;
    private ResourceCreateData vfcCreateData;
    private ResourceCreateData vfCreateData;

    @BeforeClass
    public void beforeClass() throws IOException {
        filePath = FileHandling.getFilePath("VFCs/");
    }

    @Test
    public void addOutputsToVF_test() throws UnzipException, IOException {
        ComponentPage resourceCreatePage;
        webDriver = DriverFactory.getDriver();
        homePage = new HomePage(webDriver);
        // TC - Import VFC with root namespace
        String fileName = "org.openecomp.resource.VFC-root.yml";
        CreateVfcFlow createVfcFlow = createVFC(fileName);

        resourceCreatePage = createVfcFlow.getLandedPage().orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a ResourceCreatePage"));
        resourceCreatePage.isLoaded();
        resourceCreatePage.certifyComponent();
        resourceCreatePage.isLoaded();

        Map<String, Object> yamlObject = downloadToscaArtifact(resourceCreatePage);
        checkMetadata(yamlObject, vfcCreateData);
        checkNodeTypes(yamlObject);

        homePage = homePage.getTopNavComponent().clickOnHome();
        homePage.isLoaded();

        // TC - Import hierarchy of VFCs
        fileName = "org.openecomp.resource.VFC-child.yml";
        createVfcFlow = createVFC(fileName);
        resourceCreatePage = createVfcFlow.getLandedPage().orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a ResourceCreatePage"));
        resourceCreatePage.isLoaded();
        resourceCreatePage.certifyComponent();
        resourceCreatePage.isLoaded();

        yamlObject = downloadToscaArtifact(resourceCreatePage);
        checkMetadata(yamlObject, vfcCreateData);
        checkNodeTypes(yamlObject);

        homePage = homePage.getTopNavComponent().clickOnHome();
        homePage.isLoaded();

        // TC - Import VFC with interface inputs
        // TC - Import VFC with attributes
        final CreateVfFlow createVfFlow = createVF();

        final AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToCompositionFlow(resourceCreatePage);
        final CompositionPage compositionPage = addNodeToCompositionFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a CompositionPage"));
        compositionPage.isLoaded();
        resourceCreatePage = compositionPage.goToGeneral();
        resourceCreatePage.isLoaded();

        final AttributesOutputsPage attributesOutputsPage = resourceCreatePage.goToAttributesOutputs();
        attributesOutputsPage.isLoaded();

        final ComponentInstance createdComponentInstance = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a ComponentInstance"));

        attributesOutputsPage.clickOnAttributeNavigation(createdComponentInstance.getName());
        assertTrue(attributesOutputsPage.isAttributePresent("test_1"));
        attributesOutputsPage.declareOutput("test_1");
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputPresent("test_1"));

        attributesOutputsPage.clickOnAttributesTab();
        assertTrue(attributesOutputsPage.isAttributePresent("test_2"));
        attributesOutputsPage.declareOutput("test_2");
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputPresent("test_2"));

        attributesOutputsPage.clickOnAttributesTab();
        assertTrue(attributesOutputsPage.isAttributePresent("test_3"));
        attributesOutputsPage.declareOutput("test_3");
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputPresent("test_3"));

        attributesOutputsPage.deleteOutput(createdComponentInstance.getName() + "_test_2");
        attributesOutputsPage.clickOnAttributesTab();
        assertTrue(attributesOutputsPage.isAttributePresent("test_2"));
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputDeleted("test_2"));

        attributesOutputsPage.clickOnAttributesTab();
        ExtentTestActions.addScreenshot(Status.INFO, "AttributesTab", "The Attribute's list : ");

        attributesOutputsPage.clickOnOutputsTab();
        ExtentTestActions.addScreenshot(Status.INFO, "OutputsTab", "The Output's list : ");

        attributesOutputsPage.certifyComponent();
        attributesOutputsPage.isLoaded();

        yamlObject = downloadToscaArtifact(attributesOutputsPage);
        checkMetadata(yamlObject, vfCreateData);
        checkTopologyTemplate(yamlObject);

    }

    private AddNodeToCompositionFlow addNodeToCompositionFlow(final ComponentPage componentPage) {
        componentPage.isLoaded();
        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(vfCreateData.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.RESOURCE);
        final ComponentData componentToAdd = new ComponentData();
        componentToAdd.setName(vfcCreateData.getName());
        componentToAdd.setVersion("1.0");
        componentToAdd.setComponentType(ComponentType.RESOURCE);
        final AddNodeToCompositionFlow addNodeToCompositionFlow = new AddNodeToCompositionFlow(webDriver, parentComponent, componentToAdd);
        addNodeToCompositionFlow.run(componentPage.goToComposition());
        return addNodeToCompositionFlow;
    }

    private Map<String, Object> downloadToscaArtifact(final ComponentPage resourceCreatePage) throws UnzipException {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadCsarArtifact(resourceCreatePage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));

        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));
        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        final String downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> filesFromZip = FileHandling.getFilesFromZip(downloadFolderPath, downloadedCsarName);
        final Optional<String> resourceEntryOpt = filesFromZip.keySet().stream()
            .filter(s -> s.equals("Definitions/" + downloadedCsarName.replace("-csar.csar", "-template.yml")))
            .findFirst();
        if (resourceEntryOpt.isEmpty()) {
            fail("Could not find the resource package in Definitions");
        }
        return loadYamlObject(filesFromZip.get(resourceEntryOpt.get()));
    }

    private Map<String, Object> getMapEntry(final Map<String, Object> yamlObj, final String entryName) {
        try {
            return (Map<String, Object>) yamlObj.get(entryName);
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not get the '%s' entry.", entryName);
            fail(errorMsg + "Error message: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> loadYamlObject(final byte[] mainDefinitionFileBytes) {
        return new Yaml().load(new String(mainDefinitionFileBytes));
    }

    private DownloadCsarArtifactFlow downloadCsarArtifact(final ComponentPage resourceCreatePage) {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.setWaitBeforeGetTheFile(5L);
        downloadCsarArtifactFlow.run(resourceCreatePage);
        return downloadCsarArtifactFlow;
    }

    private CreateVfcFlow createVFC(final String fileName) {
        vfcCreateData = createVfcFormData();
        final CreateVfcFlow createVfcFlow = new CreateVfcFlow(webDriver, vfcCreateData, filePath + fileName);
        createVfcFlow.run(homePage);
        return createVfcFlow;
    }

    private CreateVfFlow createVF() {
        vfCreateData = createVfFormData();
        final CreateVfFlow createVfFlow = new CreateVfFlow(webDriver, vfCreateData);
        createVfFlow.run(homePage);
        return createVfFlow;
    }

    private ResourceCreateData createVfcFormData() {
        final ResourceCreateData vfcCreateData = new ResourceCreateData();
        vfcCreateData.setRandomName(ElementFactory.getResourcePrefix() + "-VFC");
        vfcCreateData.setCategory(ResourceCategoryEnum.NETWORK_L4.getSubCategory());
        vfcCreateData.setTagList(Arrays.asList(vfcCreateData.getName(), "importVFC"));
        vfcCreateData.setDescription("aDescription");
        vfcCreateData.setVendorName("Ericsson");
        vfcCreateData.setVendorRelease("1.2.3");
        vfcCreateData.setVendorModelNumber("4.5.6");
        return vfcCreateData;
    }

    private ResourceCreateData createVfFormData() {
        final ResourceCreateData vfCreateData = new ResourceCreateData();
        vfCreateData.setRandomName(ElementFactory.getResourcePrefix() + "-VF");
        vfCreateData.setCategory(ResourceCategoryEnum.NETWORK_L4.getSubCategory());
        vfCreateData.setTagList(Arrays.asList(vfCreateData.getName(), "createVF"));
        vfCreateData.setDescription("aDescription");
        vfCreateData.setVendorName("Ericsson");
        vfCreateData.setVendorRelease("6.5.4");
        vfCreateData.setVendorModelNumber("3.2.1");
        return vfCreateData;
    }

    private void checkMetadata(final Map<String, Object> map, final ResourceCreateData createdData) {
        final Map<String, Object> metadata = getMapEntry(map, "metadata");

        assertEquals(createdData.getName(), metadata.get("name"));
        assertEquals(createdData.getDescription(), metadata.get("description"));
        assertEquals("Network L4+", metadata.get("category"));
        assertThat((String) metadata.get("type"), not(emptyString()));
        assertEquals(createdData.getCategory(), metadata.get("subcategory"));
        assertEquals(createdData.getVendorName(), metadata.get("resourceVendor"));
        assertEquals(createdData.getVendorRelease(), metadata.get("resourceVendorRelease"));
        assertEquals(createdData.getVendorModelNumber(), metadata.get("reourceVendorModelNumber"));
    }

    private void checkNodeTypes(final Map<String, Object> map) {
        final Map<String, Object> mapEntry = getMapEntry(map, "node_types");
        final Map<String, Object> nodeTypes = getMapEntry(mapEntry, mapEntry.keySet().iterator().next());

        assertNotNull(nodeTypes);
        assertEquals("aDescription", nodeTypes.get("description"));

        final Map<String, Object> properties = getMapEntry(nodeTypes, "properties");
        assertThat(properties, not(anEmptyMap()));

        final Map<String, Object> attributes = getMapEntry(nodeTypes, "attributes");
        assertThat(attributes, not(anEmptyMap()));

        final Map<String, Object> interfaces = getMapEntry(nodeTypes, "interfaces");
        assertThat(interfaces, not(anEmptyMap()));

    }

    private void checkTopologyTemplate(final Map<String, Object> map) {
        final Map<String, Object> mapEntry = getMapEntry(map, "topology_template");
        assertNotNull(mapEntry);

        final Map<String, Object> inputs = getMapEntry(mapEntry, "inputs");
        assertThat(inputs, not(anEmptyMap()));

        final Map<String, Object> outputs = getMapEntry(mapEntry, "outputs");
        assertThat(outputs, not(anEmptyMap()));
        assertEquals(2, outputs.keySet().stream().filter(s -> (s.contains("_test_1") || s.contains("_test_3")) && !s.contains("_test_2")).count());

        final Map<String, Object> nodeTemplates = getMapEntry(mapEntry, "node_templates");
        assertThat(nodeTemplates, not(anEmptyMap()));

        final Map<String, Object> substitutionMappings = getMapEntry(mapEntry, "substitution_mappings");
        assertThat(substitutionMappings, not(anEmptyMap()));

        final Map<String, Object> attributes = getMapEntry(substitutionMappings, "attributes");
        assertThat(attributes, not(anEmptyMap()));
        assertEquals(2, attributes.keySet().stream().filter(s -> (s.contains("_test_1") || s.contains("_test_3")) && !s.contains("_test_2")).count());

    }
}
