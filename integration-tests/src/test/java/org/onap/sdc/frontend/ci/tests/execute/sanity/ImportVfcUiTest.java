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
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.aventstack.extentreports.Status;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ModelName;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfcFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.GoToInterfaceDefinitionPageFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.AttributeModal;
import org.onap.sdc.frontend.ci.tests.pages.AttributesPage;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionInformationTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionInterfaceOperationsTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionOperationsModal;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionOperationsModal.InterfaceOperationsData.InputData;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class ImportVfcUiTest extends SetupCDTest {

    private String filePath;
    private WebDriver webDriver;
    private HomePage homePage;
    private ResourceCreateData vfcCreateData;
    private ResourceCreateData vfCreateData;
    private ComponentInstance createdComponentInstance;
    private final String vfcCategory = ResourceCategoryEnum.NETWORK_L4.getSubCategory();

    @BeforeClass
    public void beforeClass() {
        filePath = FileHandling.getFilePath("VFCs/");
        webDriver = DriverFactory.getDriver();
        homePage = new HomePage(webDriver);
    }

    @Test
    public void importVFC_createVF_addVFC2VF_test() throws UnzipException {
        ComponentPage componentPage;
        // TC - Import VFC with root namespace
        String fileName = "org.openecomp.resource.VFC-root.yml";
        CreateVfcFlow createVfcFlow = createVFC(fileName, ModelName.DEFAULT_MODEL_NAME.getName(), vfcCategory);

        componentPage = createVfcFlow.getLandedPage().orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        componentPage.isLoaded();
        componentPage.certifyComponent();
        componentPage.isLoaded();

        Map<String, Object> yamlObject = downloadToscaArtifact(componentPage);
        checkMetadata(yamlObject, vfcCreateData);
        checkNodeTypes(yamlObject);
        homePage.getTopNavComponent().clickOnHome();

        // TC - Import hierarchy of VFCs
        fileName = "org.openecomp.resource.VFC-child.yml";
        createVfcFlow = createVFC(fileName, ModelName.DEFAULT_MODEL_NAME.getName(), vfcCategory);
        componentPage = createVfcFlow.getLandedPage().orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        componentPage.isLoaded();

        componentPage = manageAttributes(componentPage);
        componentPage.isLoaded();
        componentPage.certifyComponent();
        componentPage.isLoaded();

        yamlObject = downloadToscaArtifact(componentPage);
        checkMetadata(yamlObject, vfcCreateData);
        checkNodeTypes(yamlObject);

        componentPage = viewInterfaceDefinitionFromVFC(componentPage);
        componentPage.isLoaded();

        homePage.getTopNavComponent().clickOnHome();

        // TC - Import VFC with interface inputs
        // TC - Import VFC with attributes
        final CreateVfFlow createVfFlow = createVF();
        componentPage = createVfFlow.getLandedPage().orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        componentPage.isLoaded();

        final CompositionPage compositionPage = addInterfaceOperations(componentPage);
        componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        componentPage.certifyComponent();
        componentPage.isLoaded();

        yamlObject = downloadToscaArtifact(componentPage);
        checkMetadata(yamlObject, vfCreateData);
        checkTopologyTemplate(yamlObject);

    }

    private ComponentPage viewInterfaceDefinitionFromVFC(final ComponentPage componentPage) {
        final GoToInterfaceDefinitionPageFlow goToInterfaceDefinitionPageFlow = new GoToInterfaceDefinitionPageFlow(webDriver);
        goToInterfaceDefinitionPageFlow.run(componentPage);
        final InterfaceDefinitionPage interfaceDefinitionPage = goToInterfaceDefinitionPageFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return InterfaceDefinitionPage"));
        final var operationName = "create";
        assertTrue(interfaceDefinitionPage.isInterfaceDefinitionOperationPresent(operationName));
        final InterfaceDefinitionOperationsModal interfaceDefinitionOperationsModal =
            interfaceDefinitionPage.clickOnInterfaceDefinitionOperation(operationName);
        interfaceDefinitionOperationsModal.isLoaded(true);
        ExtentTestActions
            .takeScreenshot(Status.INFO, "clickOnOInterfaceOperation", "Interface Definition Operation Modal opened");
        checkInterfaceDefinitionData(interfaceDefinitionOperationsModal);
        interfaceDefinitionOperationsModal.clickOnCancel();
        return interfaceDefinitionPage;
    }

    @Test
    public void importVfcWithModel() {
        final String fileName = "VFC-For-Model.yaml";
        final CreateVfcFlow createVfcFlow = createVFC(fileName, ModelName.ETSI_SOL001_v2_5_1.getName(), vfcCategory);
        final ComponentPage componentPage = createVfcFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        componentPage.isLoaded();
        componentPage.certifyComponent();
        componentPage.isLoaded();
    }

    private void checkInterfaceDefinitionData(final InterfaceDefinitionOperationsModal interfaceDefinitionOperationsModal) {
        assertFalse(interfaceDefinitionOperationsModal.getDescription().isEmpty());
        assertEquals("path/to/my/implementation.sh", interfaceDefinitionOperationsModal.getImplementationName());
        final List<InputData> inputList = interfaceDefinitionOperationsModal.getInputs();
        assertTrue(inputList.stream().anyMatch(inputData -> "first".equals(inputData.getName())), "Input of name 'first' expected");
    }

    private ComponentPage manageAttributes(final ComponentPage componentPage) {
        final AttributesPage attributesPage = componentPage.goToAttributes();
        attributesPage.isLoaded();

        assertTrue(attributesPage.isAttributePresent("test_1"));
        assertTrue(attributesPage.isAttributePresent("test_2"));
        assertTrue(attributesPage.isAttributePresent("test_3"));
        assertTrue(attributesPage.isAttributePresent("test_4"));

        attributesPage.deleteAttribute("test_2");
        assertFalse(attributesPage.isAttributePresent("test_2"));
        ExtentTestActions.takeScreenshot(Status.INFO, "attributesPage.deleteAttribute", "Attribute 'test_2' successfully deleted");
        attributesPage.addAttribute(new AttributeModal.AttributeData("test_9", "Additional attribute added from UI", "string", "one More Attribute"));
        attributesPage.isLoaded();
        assertTrue(attributesPage.isAttributePresent("test_9"));
        ExtentTestActions.takeScreenshot(Status.INFO, "attributesPage.addAttribute", "Additional Attribute 'test_9' successfully added");

        attributesPage.editAttribute(new AttributeModal.AttributeData("test_9", "Additional attribute added from UI".toUpperCase(), "string",
            "one More Attribute".toUpperCase()));
        attributesPage.isLoaded();
        assertTrue(attributesPage.isAttributePresent("test_9"));
        ExtentTestActions.takeScreenshot(Status.INFO, "attributesPage.editAttribute", "Additional Attribute 'test_9' successfully altered");

        return attributesPage.clickOnGeneralMenuItem(ResourceCreatePage.class);
    }

    private CompositionPage addInterfaceOperations(final ComponentPage componentPage) {
        final AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToCompositionFlow(componentPage);
        final CompositionPage compositionPage = addNodeToCompositionFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return CompositionPage"));
        final CompositionDetailSideBarComponent detailSideBar = compositionPage.getDetailSideBar();
        detailSideBar.isLoaded();

        createdComponentInstance = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a ComponentInstance"));

        compositionPage.selectNode(createdComponentInstance.getName());

        CompositionInterfaceOperationsTab compositionInterfaceOperationsTab =
            (CompositionInterfaceOperationsTab) detailSideBar.selectTab(CompositionDetailTabName.INTERFACE_OPERATIONS);
        compositionInterfaceOperationsTab.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "compositionInterfaceOperationsTab", "Composition Interface Operations Tab opened");
        assertTrue(compositionInterfaceOperationsTab.isOperationPresent("create"));
        InterfaceDefinitionOperationsModal compositionInterfaceOperationsModal = compositionInterfaceOperationsTab.clickOnOperation("create");
        compositionInterfaceOperationsModal.isLoaded();
        ExtentTestActions
            .takeScreenshot(Status.INFO, "compositionInterfaceOperationsTab.clickOnOperation", "Composition Interface Operations Modal opened");
        compositionInterfaceOperationsModal.deleteInput("first");
        ExtentTestActions.takeScreenshot(Status.INFO, "compositionInterfaceOperationsModal.clickOnDelete", "Input deleted");

        List<InputData> inputDataList = List.of(
            new InputData("stringInput", "string", "1"),
            new InputData("booleanInput", "boolean", true),
            new InputData("integerInput", "integer", 1)
        );
        final InterfaceDefinitionOperationsModal.InterfaceOperationsData interfaceOperationsData =
            new InterfaceDefinitionOperationsModal.InterfaceOperationsData(
                "This is CREATE operation", "fullPath/to/my/newImplementation.sh", inputDataList
            );
        compositionInterfaceOperationsModal.updateInterfaceOperation(interfaceOperationsData);
        compositionInterfaceOperationsTab.isLoaded();

        final CompositionInformationTab compositionInformationTab =
            (CompositionInformationTab) detailSideBar.selectTab(CompositionDetailTabName.INFORMATION);
        compositionInformationTab.isLoaded();

        compositionInterfaceOperationsTab =
            (CompositionInterfaceOperationsTab) detailSideBar.selectTab(CompositionDetailTabName.INTERFACE_OPERATIONS);
        compositionInterfaceOperationsTab.isLoaded();

        assertTrue(compositionInterfaceOperationsTab.isOperationPresent("create"));
        assertTrue(compositionInterfaceOperationsTab.isDescriptionPresent());
        ExtentTestActions.takeScreenshot(Status.INFO, "isDescriptionPresent", "Description is present");
        compositionInterfaceOperationsModal = compositionInterfaceOperationsTab.clickOnOperation("create");
        compositionInterfaceOperationsModal.isLoaded();
        ExtentTestActions
            .takeScreenshot(Status.INFO, "compositionInterfaceOperationsTab.clickOnOperation", "Composition Interface Operations Modal opened");

        checkCompositionInterfaceOperations(compositionInterfaceOperationsModal, interfaceOperationsData);
        compositionInterfaceOperationsModal.clickOnCancel();
        compositionInterfaceOperationsTab.isLoaded();
        return compositionPage;
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

    private Map<String, Object> downloadToscaArtifact(final ComponentPage componentPage) throws UnzipException {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadCsarArtifact(componentPage);
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

    private DownloadCsarArtifactFlow downloadCsarArtifact(final ComponentPage componentPage) {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.setWaitBeforeGetTheFile(5L);
        downloadCsarArtifactFlow.run(componentPage);
        return downloadCsarArtifactFlow;
    }

    private CreateVfcFlow createVFC(final String fileName, final String model, final String category) {
        vfcCreateData = createVfcFormData(model, category);
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

    private ResourceCreateData createVfcFormData(final String model, final String category) {
        final ResourceCreateData vfcCreateData = new ResourceCreateData();
        vfcCreateData.setRandomName(ElementFactory.getResourcePrefix() + "-VFC");
        vfcCreateData.setModel(model);
        vfcCreateData.setCategory(category);
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

        assertFalse(MapUtils.isEmpty(nodeTypes));
        assertEquals("aDescription", nodeTypes.get("description"));

        final Map<String, Object> properties = getMapEntry(nodeTypes, "properties");
        assertFalse(MapUtils.isEmpty(properties));

        final Map<String, Object> attributes = getMapEntry(nodeTypes, "attributes");
        assertFalse(MapUtils.isEmpty(attributes));

        final Map<String, Object> interfaces = getMapEntry(nodeTypes, "interfaces");
        assertFalse(MapUtils.isEmpty(interfaces));

    }

    private void checkTopologyTemplate(final Map<String, Object> map) {
        final Map<String, Object> topologyTemplate = getMapEntry(map, "topology_template");
        assertNotNull(topologyTemplate);

        final Map<String, Object> inputs = getMapEntry(topologyTemplate, "inputs");
        assertFalse(MapUtils.isEmpty(inputs));

        final Map<String, Object> nodeTemplates = getMapEntry(topologyTemplate, "node_templates");
        assertFalse(MapUtils.isEmpty(nodeTemplates));

        final Map<String, Object> attributes = getMapEntry((Map<String, Object>) nodeTemplates.get(createdComponentInstance.getName()), "attributes");
        assertTrue(MapUtils.isEmpty(attributes));

        final Map<String, Object> substitutionMappings = getMapEntry(topologyTemplate, "substitution_mappings");
        assertFalse(MapUtils.isEmpty(substitutionMappings));

    }

    private void checkCompositionInterfaceOperations(final InterfaceDefinitionOperationsModal compositionInterfaceOperationsModal,
                                                     final InterfaceDefinitionOperationsModal.InterfaceOperationsData interfaceOperationsData) {
        assertEquals(interfaceOperationsData.getDescription(), compositionInterfaceOperationsModal.getDescription());
        assertEquals(interfaceOperationsData.getImplementationName(), compositionInterfaceOperationsModal.getImplementationName());
        interfaceOperationsData.getInputList().forEach(inputData -> {
            final boolean hasInput = compositionInterfaceOperationsModal.getInputs().stream()
                .anyMatch(inputData1 -> inputData1.getName().equals(inputData.getName()));
            assertTrue(hasInput, String.format("Expecting input '%s'", inputData.getName()));
        });
    }
}
