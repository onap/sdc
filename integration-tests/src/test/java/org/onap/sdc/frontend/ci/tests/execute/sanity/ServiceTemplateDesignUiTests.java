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
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.DirectiveType;
import org.onap.sdc.frontend.ci.tests.datatypes.LogicalOperator;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceDependencyProperty;
import org.onap.sdc.frontend.ci.tests.datatypes.composition.RelationshipInformation;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddComponentInputFlow;
import org.onap.sdc.frontend.ci.tests.flow.AddComponentPropertyFlow;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateDirectiveNodeFilterFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateSubstitutionFilterFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfcFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadToscaTemplateFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.EditComponentPropertiesFlow;
import org.onap.sdc.frontend.ci.tests.flow.composition.CreateRelationshipFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.AttributesOutputsPage;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionInformationTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.InterfaceDefinitionOperationsModal;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionInterfaceOperationsTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.RelationshipWizardInterfaceOperation.InterfaceOperationsData;
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

import com.aventstack.extentreports.Status;

public class ServiceTemplateDesignUiTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTemplateDesignUiTests.class);

    private WebDriver webDriver;
    private HomePage homePage;
    private List<ResourceCreateData> vfcs = new ArrayList<>();
    private ResourceCreateData vfResourceCreateData;
    private ComponentInstance networkFunctionInstance;
    private ComponentInstance networkServiceInstance;
    private AddNodeToCompositionFlow addNodeToCompositionFlow;
    private ComponentPage componentPage;
    private Map<String, String> propertiesToBeAddedMap;
    private ResourceCreatePage resourceCreatePage;
    private Map<String, String> inputsToBeAddedMap;
    private final List<ServiceDependencyProperty> substitutionFilterProperties = new ArrayList<>();
    private final String interfaceName = "Standard";
    private final String interfaceOperationName = "create";
    private final String implementationName = "IntegrationTest";
    private final String inputName = "InputName1";
    private final String inputValue = "InputValue1";

    @BeforeMethod
    public void init() {
        webDriver = DriverFactory.getDriver();
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
    public void createBaseService() {
        final CreateVfFlow createVfFlow = createVF();
       resourceCreatePage = createVfFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a ResourceCreatePage"));
        resourceCreatePage.isLoaded();
    }

    @Test(dependsOnMethods = "createBaseService")
    public void addComponentProperty() throws UnzipException {
        propertiesToBeAddedMap = loadPropertiesToAdd();
        addProperty(propertiesToBeAddedMap);
        componentPage = addValueToProperty(loadPropertiesToEdit());
        componentPage.isLoaded();
        downloadAndVerifyCsarPackageAfterAddProperty(componentPage);
    }

    @Test(dependsOnMethods = "createBaseService")
    public void addRelationshipTemplate() throws UnzipException {
        homePage.isLoaded();
        resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vfResourceCreateData.getName());
        resourceCreatePage.isLoaded();
        addNodeToCompositionFlow = addNodeToCompositionAndCreateRelationship();
        final CompositionPage compositionPage = addNodeToCompositionFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return CompositionPage"));
        compositionPage.isLoaded();
        componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        downloadAndVerifyCsarPackage(componentPage);
    }

    @Test(dependsOnMethods = "addRelationshipTemplate")
    public void createMetadataForServiceProperty() throws Exception {
        homePage.isLoaded();
        componentPage = (ComponentPage) homePage.clickOnComponent(vfResourceCreateData.getName());
        componentPage.isLoaded();
        final ResourcePropertiesAssignmentPage propertiesAssignmentPage = componentPage.goToPropertiesAssignment();

        propertiesAssignmentPage.isLoaded();
        propertiesAssignmentPage.selectInputTab();
        final var propertyName = propertiesAssignmentPage.getInputPropertyNames().get(0);
        final var key = "Key";
        final var value = "Test";
        propertiesAssignmentPage.setInputPropertyMetadata(propertyName, key, value);

        final var topologyTemplate = getMapEntry(downloadToscaTemplate(), "topology_template");
        final var inputs = getMapEntry(topologyTemplate, "inputs");
        final var serviceProperty = getMapEntry(inputs, propertyName);
        final var servicePropertyMetadata = getMapEntry(serviceProperty, "metadata");
        assertNotNull(servicePropertyMetadata, String.format("Metadata not found for property %s", propertyName));
        assertEquals(servicePropertyMetadata.get(key), value, "Created service property metadata has invalid value");
    }

    @Test(dependsOnMethods = "addRelationshipTemplate")
    public void addOutputsToVF_test() throws UnzipException, IOException {
        homePage.isLoaded();
        final ComponentPage resourceCreatePage = (ComponentPage) homePage.clickOnComponent(vfResourceCreateData.getName());
        resourceCreatePage.isLoaded();

        final AttributesOutputsPage attributesOutputsPage = resourceCreatePage.goToAttributesOutputs();
        attributesOutputsPage.isLoaded();

        final ComponentInstance createdComponentInstance = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a ComponentInstance"));

        attributesOutputsPage.clickOnAttributeNavigation(createdComponentInstance.getName());
        assertTrue(attributesOutputsPage.isAttributePresent("attr_1"));
        attributesOutputsPage.declareOutput("attr_1");
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputPresent("attr_1"));

        attributesOutputsPage.clickOnAttributesTab();
        assertTrue(attributesOutputsPage.isAttributePresent("attr_2"));
        attributesOutputsPage.declareOutput("attr_2");
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputPresent("attr_2"));

        attributesOutputsPage.clickOnAttributesTab();
        assertTrue(attributesOutputsPage.isAttributePresent("attr_3"));
        attributesOutputsPage.declareOutput("attr_3");
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputPresent("attr_3"));

        attributesOutputsPage.deleteOutput(createdComponentInstance.getName() + "_attr_2");
        attributesOutputsPage.clickOnAttributesTab();
        assertTrue(attributesOutputsPage.isAttributePresent("attr_2"));
        attributesOutputsPage.clickOnOutputsTab();
        assertTrue(attributesOutputsPage.isOutputDeleted("attr_2"));

        attributesOutputsPage.clickOnAttributesTab();
        ExtentTestActions.addScreenshot(Status.INFO, "AttributesTab", "The Attribute's list : ");

        attributesOutputsPage.clickOnOutputsTab();
        ExtentTestActions.addScreenshot(Status.INFO, "OutputsTab", "The Output's list : ");

        Map<String, Object> yamlObject = downloadToscaArtifact(attributesOutputsPage);
        checkMetadata(yamlObject, vfResourceCreateData);
        checkTopologyTemplate(yamlObject);
    }

    @Test(dependsOnMethods = "addRelationshipTemplate")
    public void updateInterfaceOperation() throws Exception {
        homePage.isLoaded();
        componentPage = (ComponentPage) homePage.clickOnComponent(vfResourceCreateData.getName());
        componentPage.isLoaded();
        final CompositionPage compositionPage = componentPage.goToComposition();
        compositionPage.isLoaded();
        ExtentTestActions.addScreenshot(Status.INFO, "select-VFC-node", "Selecting Node on composition");
        compositionPage.selectNode(vfcs.get(1).getName());
        final InterfaceDefinitionOperationsModal.InterfaceOperationsData interfaceOperationsData =
            new InterfaceDefinitionOperationsModal.InterfaceOperationsData("IT for updating an Interface Operation",
                "MyIntegrationTestImplementationName", "My_IT_InputName", "My_IT_InputValue");
        updateInterfaceOperation(compositionPage, interfaceOperationsData);
        componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        verifyToscaTemplateHasUpdatedInterfaceOperation(downloadToscaTemplate(), interfaceOperationsData);
    }

    @Test(dependsOnMethods = "addComponentProperty")
    public void createSubstitutionFilter() throws Exception {
        componentPage = (ComponentPage) homePage.clickOnComponent(vfResourceCreateData.getName());
        componentPage.isLoaded();
        loadSubstitutionFilterProperties();
        final CompositionPage compositionPage = componentPage.goToComposition();
        compositionPage.isLoaded();
        substitutionFilterProperties.forEach(substitutionFilterProperty -> {
            final CreateSubstitutionFilterFlow createSubstitutionFilterFlow = new CreateSubstitutionFilterFlow(webDriver, substitutionFilterProperty);
            createSubstitutionFilterFlow.run(compositionPage);
        });
        componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        verifyToscaTemplateHasSubstitutionFilter(downloadToscaTemplate());
    }

    @Test(dependsOnMethods = "createBaseService")
    public void createDirectiveNodeFilterTest() throws Exception {
        final ResourceCreateData vfcResourceCreateData = vfcs.get(1);
        final String vfcNameInComposition = vfcResourceCreateData.getName().concat(" 0");
        final String value = "Test";
        final LogicalOperator operator = LogicalOperator.EQUALS;
        homePage.isLoaded();
        componentPage = (ComponentPage) homePage.clickOnComponent(vfcResourceCreateData.getName());

        componentPage.isLoaded();
        final ResourcePropertiesPage vfcPropertiesPage = componentPage.goToProperties();
        vfcPropertiesPage.isLoaded();
        final Map<String, String> propertyNamesAndTypes = vfcPropertiesPage.getPropertyNamesAndTypes();
        final List<String> propertyNames = propertyNamesAndTypes.keySet().stream().collect(Collectors.toList());
        final ServiceDependencyProperty serviceDependencyProperty =
                new ServiceDependencyProperty(propertyNames.get(0), propertyNamesAndTypes.get(propertyNames.get(0)), value, operator);

        homePage.getTopNavComponent().clickOnHome();
        homePage.isLoaded();
        homePage.clickOnComponent(vfResourceCreateData.getName());

        componentPage.isLoaded();
        final CompositionPage compositionPage = componentPage.goToComposition();
        compositionPage.isLoaded();
        compositionPage.selectNode(vfcNameInComposition);

        final CreateDirectiveNodeFilterFlow createDirectiveNodeFilterFlow =
                new CreateDirectiveNodeFilterFlow(webDriver, 2, DirectiveType.SELECT, serviceDependencyProperty);
        createDirectiveNodeFilterFlow.run(componentPage);

        verifyAvailableDirectiveTypes(createDirectiveNodeFilterFlow.getDirectiveOptions());

        verifyAvailablePropertyNames(propertyNames, createDirectiveNodeFilterFlow.getPropertyOptions());

        componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        final Map<?, ?> yaml = downloadToscaTemplate();
        verifyToscaTemplateHasDirectiveNodeFilter(yaml, serviceDependencyProperty, vfcNameInComposition);
    }

    @Test(dependsOnMethods = "addComponentProperty")
    public void declareInputFromProperties() throws Exception {
        componentPage = (ComponentPage) homePage.clickOnComponent(vfResourceCreateData.getName());
        componentPage.isLoaded();

        ResourcePropertiesAssignmentPage propertiesAssignmentPage = componentPage.goToPropertiesAssignment();
        propertiesAssignmentPage.isLoaded();

        declareInputToBaseService(propertiesAssignmentPage, "property1");
        declareInputToInstanceProperties(propertiesAssignmentPage, "resourceSubtype");
        verifyToscaTemplateHasDeclareInput(downloadToscaTemplate());
    }

    @Test(dependsOnMethods = "createBaseService")
    public void addComponentInputs() throws Exception {
        inputsToBeAddedMap = loadInputsToAdd();
        addInput(inputsToBeAddedMap);
        verifyToscaTemplateAddInput(downloadToscaTemplate());
    }

    private void checkMetadata(final Map<String, Object> map, final ResourceCreateData createdData) {
        final Map<String, Object> metadata = getMapEntry(map, "metadata");

        assertEquals(createdData.getName(), metadata.get("name"));
        assertEquals(createdData.getDescription(), metadata.get("description"));
        assertEquals("Generic", metadata.get("category"));
        assertThat((String) metadata.get("type"), not(emptyString()));
        assertEquals(createdData.getCategory(), metadata.get("subcategory"));
        assertEquals(createdData.getVendorName(), metadata.get("resourceVendor"));
        assertEquals(createdData.getVendorRelease(), metadata.get("resourceVendorRelease"));
        assertEquals(createdData.getVendorModelNumber(), metadata.get("reourceVendorModelNumber"));
    }

    private void checkTopologyTemplate(final Map<String, Object> map) {
        final Map<String, Object> mapEntry = getMapEntry(map, "topology_template");
        assertNotNull(mapEntry);

        final Map<String, Object> inputs = getMapEntry(mapEntry, "inputs");
        assertThat(inputs, not(anEmptyMap()));

        final Map<String, Object> outputs = getMapEntry(mapEntry, "outputs");
        assertThat(outputs, not(anEmptyMap()));
        assertEquals(2, outputs.keySet().stream().filter(s -> (s.contains("_attr_1") || s.contains("_attr_3")) && !s.contains("_attr_2")).count());

        final Map<String, Object> nodeTemplates = getMapEntry(mapEntry, "node_templates");
        assertThat(nodeTemplates, not(anEmptyMap()));

        final Map<String, Object> substitutionMappings = getMapEntry(mapEntry, "substitution_mappings");
        assertThat(substitutionMappings, not(anEmptyMap()));

        final Map<String, Object> attributes = getMapEntry(substitutionMappings, "attributes");
        assertThat(attributes, not(anEmptyMap()));
        assertEquals(2, attributes.keySet().stream().filter(s -> (s.contains("_attr_1") || s.contains("_attr_3")) && !s.contains("_attr_2")).count());
    }

    /**
     * Updates an Interface operation from a selected Node (VFC)
     * @param compositionPage the composition page
     * @param interfaceOperationsData the interface definition
     * @throws IOException
     */
    private void updateInterfaceOperation(final CompositionPage compositionPage,
        final InterfaceDefinitionOperationsModal.InterfaceOperationsData interfaceOperationsData) throws IOException {
        final CompositionDetailSideBarComponent detailSideBar = compositionPage.getDetailSideBar();
        detailSideBar.isLoaded();
        final CompositionInterfaceOperationsTab compositionInterfaceOperationsTab =
            (CompositionInterfaceOperationsTab) detailSideBar.selectTab(CompositionDetailTabName.INTERFACE_OPERATIONS);
        compositionInterfaceOperationsTab.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "compositionInterfaceOperationsTab",
            "Composition Interface Operations Tab loaded");
        assertTrue(compositionInterfaceOperationsTab.isOperationPresent(interfaceOperationName));
        final InterfaceDefinitionOperationsModal compositionInterfaceOperationsModal = compositionInterfaceOperationsTab
            .clickOnOperation(interfaceOperationName);
        compositionInterfaceOperationsModal.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "update-interface-operation-modal", "Loading Interface Operations Modal");
        compositionInterfaceOperationsModal.addInput();
        compositionInterfaceOperationsModal.updateInterfaceOperation(interfaceOperationsData);
        compositionInterfaceOperationsTab.isLoaded();
        ExtentTestActions.addScreenshot(Status.INFO, "updated-interface-operation",
            "The Interface operation from the selected Node was successfully updated");
        // Gives time for UI to load the Updated Interface Operation
        final CompositionInformationTab compositionInformationTab =
            (CompositionInformationTab) detailSideBar.selectTab(CompositionDetailTabName.INFORMATION);
        compositionInformationTab.isLoaded();
        validateUpdatedInterfaceOperation(detailSideBar, interfaceOperationsData);
    }

    /**
     * Validates if the Updated Interface Operation has the expected values
     * @param detailSideBar The composition Page
     * @param interfaceOperationsData The Updated Interface Definition
     */
    private void validateUpdatedInterfaceOperation(final CompositionDetailSideBarComponent detailSideBar,
                                                   final InterfaceDefinitionOperationsModal.InterfaceOperationsData interfaceOperationsData) {
        final CompositionInterfaceOperationsTab compositionInterfaceOperationsTab = (CompositionInterfaceOperationsTab) detailSideBar
            .selectTab(CompositionDetailTabName.INTERFACE_OPERATIONS);
        compositionInterfaceOperationsTab.isLoaded();
        assertTrue(compositionInterfaceOperationsTab.isOperationPresent(interfaceOperationName));
        assertTrue(compositionInterfaceOperationsTab.isDescriptionPresent());
        final InterfaceDefinitionOperationsModal compositionInterfaceOperationsModal = compositionInterfaceOperationsTab
            .clickOnOperation(interfaceOperationName);
        compositionInterfaceOperationsModal.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "validate-updated-interface-operation",
            "Loading the Interface Operations Modal for validating");
        assertThat("The Interface Operation Description should match", interfaceOperationsData.getDescription(),
            equalToIgnoringCase(compositionInterfaceOperationsModal.getDescription()));
        assertThat("The Interface Operation Implementation Name should match", interfaceOperationsData.getImplementationName(),
            equalToIgnoringCase(compositionInterfaceOperationsModal.getImplementationName()));
        assertThat("The Interface Operation Input key should match", interfaceOperationsData.getInputName(),
            equalToIgnoringCase(compositionInterfaceOperationsModal.getInputName()));
        assertThat("The Interface Operation Input Value should match", interfaceOperationsData.getInputValue(),
            equalToIgnoringCase(compositionInterfaceOperationsModal.getInputValue()));
        compositionInterfaceOperationsModal.clickOnCancel();
    }

    private void verifyToscaTemplateHasUpdatedInterfaceOperation(final Map<?, ?> toscaTemplateYaml,
        final InterfaceDefinitionOperationsModal.InterfaceOperationsData interfaceOperationsData) {

        assertNotNull(toscaTemplateYaml, "No contents in TOSCA Template");
        final Map<String, Object> topologyTemplateTosca = getMapEntry((Map<String, Object>) toscaTemplateYaml, "topology_template");
        assertThat("Should contain a topology_template entry", toscaTemplateYaml, is(notNullValue()));
        final Map<String, Object> nodeTemplatesTosca = getMapEntry(topologyTemplateTosca, "node_templates");
        assertThat("Should contain a node_templates entry", nodeTemplatesTosca, is(notNullValue()));
        final Optional<Entry<String, Object>> nodeWithInterfaceOperation = nodeTemplatesTosca.entrySet().stream()
            .filter(s -> s.getKey().startsWith(vfcs.get(1).getName())).findFirst();
        assertThat("Should contain a node (VFC)", nodeWithInterfaceOperation.isPresent(), is(true));
        final Map<String, Object> interfacesEntry = (Map<String, Object>) nodeWithInterfaceOperation.get().getValue();
        assertThat("The Interfaces Entry should not be empty", interfacesEntry, not(anEmptyMap()));
        final Map<String, Object> interfaceOperations = (Map<String, Object>) interfacesEntry.get("interfaces");
        assertThat("The Interface Entry should have operations", interfaceOperations, not(anEmptyMap()));
        final Map<String, Object> interfaceNameMap = (Map<String, Object>) interfaceOperations.get(interfaceName);
        assertThat(String.format("'%s' should contain a Interface Name entry '%s'", interfaceNameMap, interfaceName),
            interfaceOperations, not(anEmptyMap()));
        final Map<String, Object> updatedInterfaceOperation = (Map<String, Object>) interfaceNameMap.get(interfaceOperationName);
        assertThat(String.format("'%s' should contain a Interface Operation Name '%s'", updatedInterfaceOperation, interfaceOperationName),
            updatedInterfaceOperation, not(anEmptyMap()));
        assertThat("The Interface Operation Description should match",
            updatedInterfaceOperation.get("description").equals(interfaceOperationsData.getDescription()));
        assertThat("The Interface Operation Implementation Name should match",
            updatedInterfaceOperation.get("implementation").equals(interfaceOperationsData.getImplementationName()));
        final Map<String, Object> updatedInterfaceOperationInput = (Map<String, Object>) updatedInterfaceOperation.get("inputs");
        assertThat("The Interface Operation Input Key should match",
            updatedInterfaceOperationInput.containsKey(interfaceOperationsData.getInputName()));
        assertThat("The Interface Operation Input Value should match",
            updatedInterfaceOperationInput.containsValue(interfaceOperationsData.getInputValue()));
    }

    private Map<String, Object> downloadToscaArtifact(final ComponentPage resourceCreatePage) throws UnzipException {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadToscaCsar(resourceCreatePage);
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

    private void declareInputToBaseService(ResourcePropertiesAssignmentPage propertiesAssignmentPage, String propertyName){
        propertiesAssignmentPage.selectProperty(propertyName);
        propertiesAssignmentPage.clickOnDeclareInput();
        propertiesAssignmentPage.clickInputTab(propertyName);
        propertiesAssignmentPage.isInputPresent(vfResourceCreateData.getName() + "_" + propertyName);
    }

    private void declareInputToInstanceProperties(ResourcePropertiesAssignmentPage propertiesAssignmentPage, String propertyName){
        propertiesAssignmentPage.selectPropertiesTab();
        propertiesAssignmentPage.loadCompositionTab();
        propertiesAssignmentPage.loadComponentInstanceProperties(vfcs.get(0).getName().concat(" 0"));
        propertiesAssignmentPage.selectProperty(propertyName);
        propertiesAssignmentPage.clickOnDeclareInput();
        propertiesAssignmentPage.clickInputTab(propertyName);
        propertiesAssignmentPage.isInputPresent(vfResourceCreateData.getName() + "_" + vfcs.get(0).getName());
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

    private AddNodeToCompositionFlow addNodeToCompositionAndCreateRelationship() {
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
        final CompositionPage compositionPage = resourceCreatePage.goToComposition();
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
        final CreateRelationshipFlow createRelationshipFlow = new CreateRelationshipFlow(webDriver, relationshipInformation,
            new InterfaceOperationsData(interfaceName, interfaceOperationName, implementationName, inputName, inputValue));
        createRelationshipFlow.run(compositionPage).orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a CompositionPage instance"));
        ExtentTestActions.takeScreenshot(Status.INFO, "relationship",
            String.format("Relationship from networkFunctionInstance '%s' to networkServiceInstanceResource '%s' was created",
                fromComponentInstanceName, toComponentInstanceName));
    }

    /**
     * Adds a property to the base service
     * @param propertyMap map of properties to be added
     */
    private void addProperty(final Map<String, String> propertyMap) {
        componentPage = (ComponentPage) homePage.clickOnComponent(vfResourceCreateData.getName());
        componentPage.isLoaded();
        final AddComponentPropertyFlow addComponentPropertyFlow = new AddComponentPropertyFlow(webDriver, propertyMap);
        addComponentPropertyFlow.run(componentPage.goToPropertiesAssignment());
    }

    /**
     * Adds a input to the base service
     * @param inputMap map of inputs to be added
     */
    private void addInput(final Map<String, String> inputMap) {
        componentPage = (ComponentPage) homePage.clickOnComponent(vfResourceCreateData.getName());
        componentPage.isLoaded();
        final AddComponentInputFlow addComponentInputFlow = new AddComponentInputFlow(webDriver, inputMap);
        addComponentInputFlow.run(componentPage.goToPropertiesAssignment());
    }

    /**
     * Edits a property to add a value
     * @param propertyMap map of properties to be edited
     */
    private ComponentPage addValueToProperty(final Map<String, Object> propertyMap) {
        final EditComponentPropertiesFlow editComponentPropertiesFlow = new EditComponentPropertiesFlow(webDriver, propertyMap);
        return editComponentPropertiesFlow.run().orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ComponentPage"));
    }

    /**
     * Downloads and verifies the generated tosca templates.
     * @param componentPage the component page
     * @throws UnzipException
     */
    private void downloadAndVerifyCsarPackage(final ComponentPage componentPage) throws UnzipException {
        checkCsarPackage(downloadCsarPackage(componentPage));
    }

    /**
     * Downloads and verifies if the generated Tosca template contains the expected properties.
     * @throws UnzipException
     * @param componentPage
     */
    private void downloadAndVerifyCsarPackageAfterAddProperty(final ComponentPage componentPage) throws UnzipException {
        verifyPropertiesOnGeneratedTemplate(downloadCsarPackage(componentPage));
    }

    private String downloadCsarPackage(final ComponentPage componentPage) {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadToscaCsar(componentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));
        return toscaArtifactsPage.getDownloadedArtifactList().get(0);
    }

    /**
     * Downloads the generated CSAR package.
     * @param componentPage the component page
     * @return the Downloaded Tosca CSAR file
     */
    private DownloadCsarArtifactFlow downloadToscaCsar(final ComponentPage componentPage) {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.setWaitBeforeGetTheFile(5L);
        downloadCsarArtifactFlow.run(componentPage);
        return downloadCsarArtifactFlow;
    }

    /**
     * Verifies if the generated Tosca template contains the expected properties.
     * @param downloadedCsarName the downloaded csar file name
     * @throws UnzipException
     */
    private void verifyPropertiesOnGeneratedTemplate(final String downloadedCsarName) throws UnzipException {
        final Map<String, byte[]> filesFromZip = extractFilesFromCsar(downloadedCsarName);
        final String virtualFunctionName = vfResourceCreateData.getName().replace("-", "").toLowerCase();
        final String vfResourceTemplateFile = "Definitions/resource-" + virtualFunctionName + "-template-interface.yml";
        final String interfaceTemplateFile = filesFromZip.keySet().stream()
            .filter(filename -> filename.equalsIgnoreCase(vfResourceTemplateFile)).findFirst()
            .orElseThrow(() -> new UiTestFlowRuntimeException(String.format("Resource template file not found %s", vfResourceTemplateFile)));
        final byte[] toscaInterfaceTemplateGenerated = filesFromZip.get(interfaceTemplateFile);
        assertThat("The Generated Tosca template should not be null", toscaInterfaceTemplateGenerated, is(notNullValue()));
        final Map<String, Object> interfaceTemplateYamlMap = loadYamlObject(toscaInterfaceTemplateGenerated);
        final Map<String, Object> nodeTypesYamlMap = getMapEntry(interfaceTemplateYamlMap, "node_types");
        assertThat(String.format("'%s' should contain a node_types entry", interfaceTemplateYamlMap), nodeTypesYamlMap, is(notNullValue()));
        final Map<String, Object> properties = (Map) nodeTypesYamlMap.values().stream().filter(stringObjectEntry -> stringObjectEntry != null)
            .collect(Collectors.toList()).get(0);
        final Map<String, Object> propertiesFoundMap = (Map<String, Object>) properties.get("properties");
        assertThat(String.format("The generated template file %s should contain all added properties", vfResourceTemplateFile),
            propertiesFoundMap.keySet().containsAll(propertiesToBeAddedMap.keySet()), is(true));
    }

    /**
     * Checks if the downloaded Tosca csar includes the node templates for the added VFCs,
     * the generated service template declared “tosca_simple_yaml_1_3” as its Tosca version,
     * the generated csar contains the node type definitions for the added VFCs in the Definitions directory,
     * the interface template contains the relationship declaration
     * @param downloadedCsarName download Tosca CSAR filename
     * @throws UnzipException
     */
    private void checkCsarPackage(final String downloadedCsarName) throws UnzipException {
        final Map<String, byte[]> filesFromZip = extractFilesFromCsar(downloadedCsarName);
        final String virtualFunctionName = vfResourceCreateData.getName().replace("-", "").toLowerCase();
        final List<String> expectedDefinitionFolderFileList = getExpectedDefinitionFolderFileList(virtualFunctionName);
        final Map<String, byte[]> expectedFilesFromZipMap = filesFromZip.entrySet().parallelStream()
            .filter(key -> expectedDefinitionFolderFileList.stream()
                .anyMatch(filename -> filename.equalsIgnoreCase(key.getKey()))).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        final String vfResourceTemplateFile = "Definitions/resource-" + virtualFunctionName + "-template.yml";
        final String generatedTemplateFile = expectedFilesFromZipMap.keySet().stream()
            .filter(filename -> filename.equalsIgnoreCase(vfResourceTemplateFile)).findFirst()
            .orElseThrow(() -> new UiTestFlowRuntimeException(String.format("Resource template file not found %s", vfResourceTemplateFile)));
        final byte[] toscaTemplateGenerated = filesFromZip.get(generatedTemplateFile);
        assertThat(toscaTemplateGenerated, is(notNullValue()));
        verifyGeneratedTemplate(toscaTemplateGenerated, generatedTemplateFile);
        verifyNodesRelationship(expectedFilesFromZipMap, virtualFunctionName, filesFromZip);
    }

    private Map<String, byte[]> extractFilesFromCsar(final String downloadedCsarName) throws UnzipException {
        final String downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> filesFromCsar = FileHandling.getFilesFromZip(downloadFolderPath, downloadedCsarName);
        return filesFromCsar;
    }

    private void verifyGeneratedTemplate(final byte[] generatedTemplateData, final String generatedTemplateFile) {
        final Map<String, Object> templateYamlMap = loadYamlObject(generatedTemplateData);
        final boolean hasToscaDefinitionVersionEntry = templateYamlMap.containsKey("tosca_definitions_version");
        assertThat(String.format("'%s' should contain tosca_definitions_version entry", generatedTemplateFile), hasToscaDefinitionVersionEntry,
            is(true));
        final String toscaVersion = (String) templateYamlMap.get("tosca_definitions_version");
        assertThat(String.format("'%s' tosca_definitions_version entry should have tosca_simple_yaml_1_3 value", generatedTemplateFile),
            toscaVersion.equalsIgnoreCase("tosca_simple_yaml_1_3"));
        final Map<String, Object> topologyTemplateTosca = getMapEntry(templateYamlMap, "topology_template");
        assertThat(String.format("'%s' should contain a topology_template entry", generatedTemplateFile), topologyTemplateTosca, is(notNullValue()));
        final Map<String, Object> nodeTemplatesTosca = getMapEntry(topologyTemplateTosca, "node_templates");
        assertThat(String.format("'%s' should contain a node_templates entry", generatedTemplateFile), nodeTemplatesTosca, is(notNullValue()));
        final List<String> nodeTemplateFound = nodeTemplatesTosca.keySet().parallelStream().filter(s -> vfcs.stream()
            .anyMatch(vfc -> s.startsWith(vfc.getName()))).collect(Collectors.toList());
        assertThat(String.format("'%s' should contain the node type definitions for the added VFCs '%s'", nodeTemplatesTosca, vfcs),
            nodeTemplateFound, hasSize(vfcs.size()));
        verifyRelationshipTemplate(topologyTemplateTosca, generatedTemplateFile);
    }

    private void verifyRelationshipTemplate(final Map<String, Object> topologyTemplateToscaMap, final String generatedTemplateFile) {
        final Map<String, Object> relationshipTemplateMap = getMapEntry(topologyTemplateToscaMap, "relationship_templates");
        assertThat(String.format("'%s' should contain a topology_template entry", generatedTemplateFile), relationshipTemplateMap,
            is(notNullValue()));
        final String result = Arrays.asList(relationshipTemplateMap.values()).toString();
        assertThat(String.format("'%s' should contain a DependsOn relationship", relationshipTemplateMap),
            result.contains("tosca.relationships.DependsOn"), is(true));
        assertThat(String.format("'%s' should contain interfaces entry", relationshipTemplateMap), result.contains("interfaces"), is(true));
        assertThat(String.format("'%s' should contain a Interface Name entry '%s'", relationshipTemplateMap, interfaceName),
            result.contains(interfaceName), is(true));
        assertThat(String.format("'%s' should contain a Interface Operation Name '%s'", relationshipTemplateMap, interfaceOperationName),
            result.contains(interfaceOperationName), is(true));
        assertThat(String.format("'%s' should contain Implementation Name '%s'", relationshipTemplateMap, implementationName),
            result.contains(implementationName), is(true));
        assertThat(String.format("'%s' should contain inputs entry", relationshipTemplateMap), result.contains("inputs"), is(true));
        assertThat(String.format("'%s' should contain Input Name '%s'", relationshipTemplateMap, inputName), result.contains(inputName),
            is(true));
        assertThat(String.format("'%s' should contain Input Value '%s'", relationshipTemplateMap, inputValue), result.contains(inputValue),
            is(true));
    }

    private void verifyNodesRelationship(final Map<String, byte[]> expectedFilesFromZipMap, final String virtualFunctionName,
                                         final Map<String, byte[]> filesFromZip) {
        final String vfResourceTemplateFile = "Definitions/resource-" + virtualFunctionName + "-template-interface.yml";
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
        vfcs.forEach(vfc -> expectedDefinitionFolderFileList.add("Definitions/resource-" + vfc.getName() + "-template.yml"));
        expectedDefinitionFolderFileList.add("Definitions/resource-" + vfResourceName + "-template.yml");
        expectedDefinitionFolderFileList.add("Definitions/resource-" + vfResourceName + "-template-interface.yml");
        return expectedDefinitionFolderFileList;
    }

    private void verifyToscaTemplateHasDeclareInput(Map<?, ?> yaml) {
        assertNotNull(yaml, "No contents in TOSCA Template");
        final Map<String, Object> toscaYaml = (Map<String, Object>) yaml;
        final Map<String, Object> topologyTemplateTosca = getMapEntry(toscaYaml, "topology_template");
        assertThat(String.format("'%s' should contain a topology_template entry", toscaYaml), topologyTemplateTosca,
            notNullValue());
        final Map<String, Object> inputsTosca = getMapEntry(topologyTemplateTosca, "inputs");
        assertThat(String.format("'%s' should contain a inputs entry", toscaYaml), inputsTosca, notNullValue());
        assertEquals(2, inputsTosca.keySet().stream()
            .filter(s -> (s.contains("resourceSubtype") || s.contains("property1"))).count());
        final Map<String, Object> substitutionMapping = getMapEntry(topologyTemplateTosca, "substitution_mappings");
        assertThat(String.format("'%s' should contain a substitution_mappings entry", toscaYaml), substitutionMapping,
            notNullValue());
        final Map<String, Object> substitutionMappingProperties = getMapEntry(substitutionMapping, "properties");
        assertThat(String.format("'%s' should contain a properties entry", toscaYaml), substitutionMappingProperties,
            notNullValue());
        assertEquals(2, substitutionMappingProperties.keySet().stream()
            .filter(s -> (s.contains("resourceSubtype") || s.contains("property1"))).count());
    }

    private void verifyToscaTemplateAddInput(Map<?, ?> yaml) {
        final Map<String, String> inputMap = loadInputsToAdd();
        assertNotNull(yaml, "No contents in TOSCA Template");
        final Map<String, Object> toscaYaml = (Map<String, Object>) yaml;
        final Map<String, Object> topologyTemplateTosca = getMapEntry(toscaYaml, "topology_template");
        assertThat(String.format("'%s' should contain a topology_template entry", toscaYaml), topologyTemplateTosca,
            notNullValue());
        final Map<String, Object> inputsTosca = getMapEntry(topologyTemplateTosca, "inputs");
        assertThat(String.format("'%s' should contain a inputs entry", toscaYaml), inputsTosca, notNullValue());
        assertEquals(3, inputsTosca.keySet().stream()
            .filter(s -> inputMap.containsKey(s)).count());
    }

    private Map<String, Object> getMapEntry(final Map<?, ?> yamlObj, final String entryName) {
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

    private Map<String, String> loadPropertiesToAdd() {
        final Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("property1", "string");
        propertyMap.put("property2", "integer");
        propertyMap.put("property3", "boolean");
        propertyMap.put("property4", "list");
        propertyMap.put("property5", "map");
        propertyMap.put("property6", "scalar-unit.size");
        return propertyMap;
    }

    private Map<String, Object> loadPropertiesToEdit() {
        final Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("property1", "Integration Test");
        propertyMap.put("property2", 100);
        propertyMap.put("property3", Boolean.TRUE);
        propertyMap.put("property4", Arrays.asList("PropListV1", "PropListV2", "PropListV3"));
        final Map<String, String> stringMap = new HashMap<>();
        stringMap.put("PropMapKey1", "PropMapValue1");
        stringMap.put("PropMapKey2", "PropMapValue2");
        stringMap.put("PropMapKey3", "PropMapValue3");
        propertyMap.put("property5", stringMap);
        propertyMap.put("property6", 500);
        return propertyMap;
    }

    private Map<String, String> loadInputsToAdd() {
        final Map<String, String> inputMap = new HashMap<>();
        inputMap.put("input1", "string");
        inputMap.put("input2", "integer");
        inputMap.put("input3", "boolean");
        return inputMap;
    }

    private void loadSubstitutionFilterProperties() {
        final ResourcePropertiesAssignmentPage propertiesPage = componentPage.goToPropertiesAssignment();
        propertiesPage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "propertiesAssigment",
            String.format("The %s Properties Assignment Page is loaded", vfResourceCreateData.getName()));
        Map<String, String> propertyNamesAndTypes = propertiesPage.getPropertyNamesAndTypes();
        assertThat(String.format("The Component '%s' should have properties", vfResourceCreateData.getName()), propertyNamesAndTypes,
            not(anEmptyMap()));
        propertyNamesAndTypes.forEach((name, type)
            -> substitutionFilterProperties.add(new ServiceDependencyProperty(name, type, getPropertyValueByType(type), LogicalOperator.EQUALS)));
    }

    private String getPropertyValueByType(final String type) {
        switch (type) {
            case "string":
                return "IntegrationTest";
            case "integer":
                return "202";
            case "size":
                return "500";
            case "boolean":
                return "TRUE";
            case "list":
                return "[\"value1\", \"value2\"]";
            case "map":
                return "{\"MyKey\": \"MyValue\"}";
            default:
                throw new UnsupportedOperationException("Not yet implemented for " + type);
        }
    }

    /**
     * Downloads Tosca Template file
     * @return the tosca template yaml file
     * @throws Exception
     */
    private Map<?, ?> downloadToscaTemplate() throws Exception {
        final DownloadToscaTemplateFlow downloadToscaTemplateFlow = new DownloadToscaTemplateFlow(webDriver);
        final ToscaArtifactsPage toscaArtifactsPage = (ToscaArtifactsPage) downloadToscaTemplateFlow.run(componentPage).get();
        return FileHandling.parseYamlFile(getConfig().getDownloadAutomationFolder()
            .concat(java.io.File.separator).concat(toscaArtifactsPage.getDownloadedArtifactList().get(0)));
    }

    private void verifyToscaTemplateHasSubstitutionFilter(final Map<?, ?> yaml) {
        assertNotNull(yaml, "No contents in TOSCA Template");
        final List<?> substitutionFilters = (List<?>) getSubstitutionFilterFromYaml(yaml).get("properties");
        substitutionFilterProperties.forEach(substitutionFilterProperty -> {
            final Map<?, ?> substitutionFilterMap = (Map<?, ?>) substitutionFilters.stream()
                .filter(subFilter -> ((Map<?, ?>) subFilter).containsKey(substitutionFilterProperty.getName())).findAny().get();
            assertThat("Added substitution filter not found in TOSCA Template",
                substitutionFilterMap.containsKey(substitutionFilterProperty.getName()));
            final Map<?, ?> substitutionFilterValue = (Map<?, ?>) ((List<?>) substitutionFilterMap.get(substitutionFilterProperty.getName())).get(0);
            assertThat("Substitution Filter Value should not be empty", substitutionFilterMap, not(anEmptyMap()));
            final String expectedSubstitutionPropertyValue = substitutionFilterProperty.getValue().replaceAll("[\"{}]", "");
            final String actualSubstitutionPropertyValue = substitutionFilterValue.values().stream().findFirst().get() instanceof Map
                ? substitutionFilterValue.values().stream().findFirst().get().toString().replace("=", ": ")
                .replaceAll("\\{(.*?)\\}", "$1").trim()
                : substitutionFilterValue.values().stream().findFirst().get().toString();
            assertThat("Invalid value for added substitution filters found in TOSCA Template",
                expectedSubstitutionPropertyValue.equalsIgnoreCase(actualSubstitutionPropertyValue));
            assertThat("Invalid logical operator for added substitution filters found in TOSCA Template",
                substitutionFilterValue.containsKey(substitutionFilterProperty.getLogicalOperator().getName()));
        });
    }

    private Map<?, ?> getSubstitutionFilterFromYaml(final Map<?, ?> yaml) {
        final Map<?, ?> topology = (Map<?, ?>) yaml.get("topology_template");
        final Map<?, ?> substitutionMappings = (Map<?, ?>) topology.get("substitution_mappings");
        return (Map<?, ?>) substitutionMappings.get("substitution_filter");
    }

    private void verifyAvailableDirectiveTypes(final List<String> availableDirectiveTypes) {
        assertNotNull(availableDirectiveTypes, "Expected list of available Directive Types, but recieved null");
        Arrays.asList(DirectiveType.values()).forEach(directiveType -> {
            assertTrue(availableDirectiveTypes.contains(directiveType.getName())
                    , String.format("Expected directive %s to be availabe in UI options %s"
                            , directiveType.getName(), availableDirectiveTypes.toString()));
        });
        ExtentTestActions.log(Status.PASS, "All expected directive types are available for selection");
    }

    private void verifyAvailablePropertyNames(List<String> propertyNames, List<String> propertyNameOptions) {
        assertEquals(propertyNameOptions.size(), propertyNames.size(), "Mismatch in the number of properties available for selection");
        propertyNames.forEach(name -> {
            assertNotEquals(false, propertyNameOptions.remove(name)
                    , String.format("Expected property %s not found in UI Select element", name));
        });
        ExtentTestActions.log(Status.PASS, "All expected properties are available for selection");
    }

    private void verifyToscaTemplateHasDirectiveNodeFilter(final Map<?, ?> yaml, ServiceDependencyProperty nodeFilterProperty, String nodeTemplateName) {
        assertNotNull(yaml, "Tosca Template Yaml is not expected to be empty");
        final List<?> nodeFilters = (List<?>) getDirectiveNodeFilterFromYaml(yaml, nodeTemplateName).get("properties");
        final Map<?, ?> nodeFilter = (Map<?, ?>) nodeFilters.stream()
                    .filter(yamlNodeFilter -> ((Map<?, ?>) yamlNodeFilter).containsKey(nodeFilterProperty.getName())).findAny().get();
        assertNotNull(nodeFilter, "Added directive node filter not found in TOSCA Template");

        final Map<?, ?> nodeFilterValue = (Map<?, ?>) ((List<?>) nodeFilter.get(nodeFilterProperty.getName())).get(0);
        assertTrue(nodeFilterValue.containsValue(nodeFilterProperty.getValue())
                , "Invalid value for added directive node filter found in TOSCA Template");
        assertTrue(nodeFilterValue.containsKey(nodeFilterProperty.getLogicalOperator().getName())
                , "Invalid logical operator for added directive node filter found in TOSCA Template");
    }

    private Map<?,?> getDirectiveNodeFilterFromYaml(final Map<?,?> yaml, String nodeTemplateName) {
        final Map<?, ?> topology = (Map<?, ?>) yaml.get("topology_template");
        final Map<?, ?> nodeTemplates = (Map<?, ?>) topology.get("node_templates");
        final Map<?, ?> resourceNode = (Map<?, ?>) nodeTemplates.get(nodeTemplateName);
        return (Map<?, ?>) resourceNode.get("node_filter");
    }
}
