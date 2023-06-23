/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.pages.vf.composition;

import com.aventstack.extentreports.Status;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.ToscaFunction;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.execute.sanity.ServiceTemplateDesignUiTests;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.*;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.*;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPropertiesAttributesTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

public class CompositionFloatTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionFloatTests.class);

    private WebDriver webDriver;
    private HomePage homePage;
    private List<ResourceCreateData> vfcs = new ArrayList<>();
    private ServiceTemplateDesignUiTests serviceTemplateDesignUiTests;
    private ResourceCreateData vf;

    @BeforeMethod(alwaysRun = true)
    public void init() {
        webDriver = DriverFactory.getDriver();
        homePage = new HomePage(webDriver);
        serviceTemplateDesignUiTests = new ServiceTemplateDesignUiTests();
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "SimpleDataType")
    public void importDataType(final String rootFolder, final String typeFilename) {
        setLog(typeFilename);
        homePage.isLoaded();
        final ImportTypePage importTypePage = homePage.clickOnImportType(rootFolder + typeFilename);

        importTypePage.isLoaded();
        importTypePage.clickOnCreate();
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "floatVFC", dependsOnMethods = "importDataType")
    public void importVfc(final String rootFolder, final String vfcFilename) {
        setLog(vfcFilename);
        final String resourceName = ElementFactory.addRandomSuffixToName(ElementFactory.getResourcePrefix());
        final CreateVfcFlow createVfcFlow = createVFC(rootFolder + vfcFilename, resourceName);

        vfcs.stream().filter(vfc -> vfc.getName().startsWith(resourceName)).findFirst().orElseThrow(
            () -> new UiTestFlowRuntimeException(String.format("VFCs List should contain a VFC with the expected name %s", resourceName)));

        final ResourceCreatePage resourceCreatePage = createVfcFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ResourceCreatePage"));

        resourceCreatePage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "vfc-created",
            String.format("VFC '%s' was created", resourceName));
    }

    @Test(dependsOnMethods = "importVfc")
    private void certifyVFC() {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());

        final ResourceCreatePage resourceCreatePage = new ResourceCreatePage(webDriver);
        resourceCreatePage.isLoaded();
        resourceCreatePage.certifyComponent();
    }

    @Test(dependsOnMethods = "certifyVFC")
    private void verifyVFC() throws Exception {
        homePage.isLoaded();
        final ResourceCreatePage resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vfcs.get(0).getName());
        resourceCreatePage.isLoaded();
        final ToscaArtifactsPage toscaPage = resourceCreatePage.goToToscaArtifacts();

        final DownloadToscaTemplateFlow downloadFlow = new DownloadToscaTemplateFlow(webDriver);
        final Map<?, ?> yaml = downloadFlow.downloadToscaTemplate(toscaPage);
        assertNotNull(yaml, "No contents in TOSCA Template");

        final Map<String, Object> toscaYaml = (Map<String, Object>) yaml;
        final Map<String, Object> nodeTypesTosca = getMapEntry(toscaYaml, "node_types");
        final Map<String, Object> simpleVFCTosca = getMapEntry(nodeTypesTosca, "org.openecomp.resource.FloatVFC");
        final Map<String, Object> propertiesTosca = getMapEntry(simpleVFCTosca, "properties");
        assertThat(String.format("'%s' should contain a properties entry", toscaYaml), propertiesTosca, notNullValue());
        assertEquals(11, propertiesTosca.size());
    }

    /*
        ==========================
        ====== VF TESTS =====
        ==========================
     */

    @Test(dependsOnMethods = "certifyVFC")
    private void createVF() {
        final CreateVfFlow createVfFlow = new CreateVfFlow(webDriver, createVfFormData());
        createVfFlow.run(homePage);
    }
    
    @Test(dependsOnMethods = "createVF")
    private void addVFCToVF() {
        homePage.isLoaded();
        final ResourceCreatePage resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vf.getName());
        resourceCreatePage.isLoaded();

        final AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToCompositionFlow(resourceCreatePage);
        final CompositionPage compositionPage = addNodeToCompositionFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a CompositionPage"));
        compositionPage.isLoaded();
    }

    @Test(dependsOnMethods = "addVFCToVF")
    private void addPropertiesToVF() {
        final Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("VF_FLOAT_PROP_1", "float");
        propertyMap.put("VF_FLOAT_PROP_2", "float");
        propertyMap.put("VF_FLOAT_PROP_3", "float");

        final Map<String, Object> propertyValues = new HashMap<>();
        propertyValues.put("VF_FLOAT_PROP_1", "125");
        propertyValues.put("VF_FLOAT_PROP_2", "300");
        propertyValues.put("VF_FLOAT_PROP_3", "1");

        homePage.isLoaded();
        ResourceCreatePage resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vf.getName());

        final AddComponentPropertyFlow addComponentPropertyFlow = new AddComponentPropertyFlow(webDriver, propertyMap);
        resourceCreatePage.isLoaded();
        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage = resourceCreatePage.goToPropertiesAssignment();
        addComponentPropertyFlow.run(resourcePropertiesAssignmentPage);

        propertyValues.forEach((name, value) -> {
            resourcePropertiesAssignmentPage.setPropertyValue(name, value);
            assertEquals(value, resourcePropertiesAssignmentPage.getPropertyValue(name));
        });

        resourcePropertiesAssignmentPage.saveProperties();
    }

    @Test(dependsOnMethods = "addVFCToVF")
    private void addInputsToVF() {
        homePage.isLoaded();
        ResourceCreatePage resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vf.getName());
        resourceCreatePage.isLoaded();

        final Map<String, String> inputMap = new HashMap<>();
        inputMap.put("VF_FLOAT_INPUT_1", "float");
        inputMap.put("VF_FLOAT_INPUT_2", "float");
        inputMap.put("VF_FLOAT_INPUT_3", "float");

        final ResourcePropertiesAssignmentPage resourcePropertiesAssignmentPage = resourceCreatePage.goToPropertiesAssignment();
        resourcePropertiesAssignmentPage.selectInputTab();
        resourcePropertiesAssignmentPage.addInputs(inputMap);
    }


    /*
        ===============================
        ======= VF COMPOSER TESTS =====
        ===============================
     */

    @DataProvider(name = "compositionProperties", parallel = false)
    private Object[][] compositionProperties() {
        return new Object[][]{
            // get_property referencing a SELF property
            new Object[]{"GreaterFloatProp", ToscaFunction.GET_PROPERTY, "SELF", "VF_FLOAT_PROP_1"},
            // get_attribute referencing a SELF property
            new Object[]{"LessFloatProp", ToscaFunction.GET_ATTRIBUTE, "SELF", "VF_FLOAT_PROP_3"},
            // get_property referencing a property of in instance property
            new Object[]{"GEFloatProp", ToscaFunction.GET_PROPERTY, vfcs.get(0).getName() + " 0", "EqualFloatProp"},
            // get_attribute referencing a property of in instance property
            new Object[]{"LEFloatProp", ToscaFunction.GET_ATTRIBUTE, vfcs.get(0).getName() + " 0", "EqualFloatProp"},
            // get_property referencing a sub property of an instance property
            new Object[]{"float_prop_1", ToscaFunction.GET_PROPERTY, vfcs.get(0).getName() + " 0", "ComplexProp->float_prop"},
            // get_attribute referencing a sub property of an instance property
            new Object[]{"float_prop_2", ToscaFunction.GET_ATTRIBUTE, vfcs.get(0).getName() + " 0", "ComplexProp->float_prop"},
            new Object[]{"EqualFloatProp", ToscaFunction.GET_INPUT, null, "VF_FLOAT_INPUT_2"},
            new Object[]{"validValueFloatProp", ToscaFunction.YAML, null, "value: 125"}
        };
    }

    @Test(dependsOnMethods = "addPropertiesToVF", dataProvider = "compositionProperties")
    private void updateCompositionProperty(String propertyName, ToscaFunction toscaFunction, String propertySource, String attribute)
        throws InterruptedException, NotImplementedException {
        homePage.isLoaded();
        final ResourceCreatePage resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vf.getName());
        resourceCreatePage.isLoaded();

        final CompositionPage compositionPage = resourceCreatePage.goToComposition();
        compositionPage.isLoaded();
        compositionPage.selectNode(vfcs.get(0).getName());

        final CompositionDetailSideBarComponent sideBarComponent = compositionPage.getDetailSideBar();
        sideBarComponent.isLoaded();
        sideBarComponent.checkComponentIsSelected(vfcs.get(0).getName());

        CompositionPropertiesAttributesTab propertiesAttributesTab = (CompositionPropertiesAttributesTab) sideBarComponent.selectTab(CompositionDetailTabName.PROPERTIES_ATTRIBUTES);
        propertiesAttributesTab.isLoaded();
        propertiesAttributesTab.clickOnProperty(propertyName);

        PropertiesPage.getPropertyPopup().setToscaDefaultValue(toscaFunction, propertySource, attribute);
    }

    @Test(dependsOnMethods = "updateCompositionProperty")
    private void certifyAndVerifyVF() throws Exception {
        homePage.isLoaded();
        ResourceCreatePage resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vf.getName());
        resourceCreatePage.isLoaded();
        resourceCreatePage.certifyComponent();

        resourceCreatePage.isLoaded();
        final DownloadToscaTemplateFlow downloadFlow = new DownloadToscaTemplateFlow(webDriver);
        final Map<?, ?> yaml = downloadFlow.downloadToscaTemplate(resourceCreatePage);
        assertNotNull(yaml, "No contents in TOSCA Template");

        final Map<String, Object> toscaYaml = (Map<String, Object>) yaml;
        final Map<String, Object> topologyTosca = getMapEntry(toscaYaml, "topology_template");
        final Map<String, Object> nodeTemplateTosca = getMapEntry(topologyTosca, "node_templates");
        final Map<String, Object> vfcTosca = getMapEntry(nodeTemplateTosca, vfcs.get(0).getName() +" 0");
        final Map<String, Object> propertiesTosca = getMapEntry(vfcTosca, "properties");

        assertThat(String.format("'%s' should contain a properties entry", toscaYaml), propertiesTosca, notNullValue());
        assertEquals(11, propertiesTosca.size());

        final Map<String, Object> greaterFloatProp = getMapEntry(propertiesTosca, "GreaterFloatProp");
        final Map<String, Object> geFloatProp = getMapEntry(propertiesTosca, "GEFloatProp");
        final Map<String, Object> floatProp1 = getMapEntry(propertiesTosca, "float_prop_1");
        final Map<String, Object> floatProp2 = getMapEntry(propertiesTosca, "float_prop_2");
        final Map<String, Object> equalFloatProp = getMapEntry(propertiesTosca, "EqualFloatProp");
        final Map<String, Object> validValueFloatProp = getMapEntry(propertiesTosca, "validValueFloatProp");
        final Map<String, Object> lessFloatProp = getMapEntry(propertiesTosca, "LessFloatProp");
        final Map<String, Object> leFloatProp = getMapEntry(propertiesTosca, "LEFloatProp");

        assertEquals(1.2, (double) propertiesTosca.get("inRangeFloatProp"));
        assertEquals(1.2, (double) propertiesTosca.get("float_prop_3"));
        assertEquals(125, (int) validValueFloatProp.get("value"));

        ArrayList<String> greaterFloatPropGetProperty = (ArrayList<String>) greaterFloatProp.get("get_property");
        ArrayList<String> geFloatPropGetProperty = (ArrayList<String>) geFloatProp.get("get_property");
        ArrayList<String> floatProp1GetProperty = (ArrayList<String>) floatProp1.get("get_property");
        ArrayList<String> floatProp2GetAttribute = (ArrayList<String>) floatProp2.get("get_attribute");
        ArrayList<String> equalFloatPropGetInput = (ArrayList<String>) equalFloatProp.get("get_input");
        ArrayList<String> lessFloatPropValueGetAttribute = (ArrayList<String>) lessFloatProp.get("get_attribute");
        ArrayList<String> leFloatPropValueGetAttribute = (ArrayList<String>) leFloatProp.get("get_attribute");

        assertTrue(greaterFloatPropGetProperty.contains("SELF"), "GreaterFloatProp is missing SELF reference");
        assertTrue(greaterFloatPropGetProperty.contains("VF_FLOAT_PROP_1"), "GreaterFloatProp is missing VF_FLOAT_PROP_1 reference");
        assertTrue(geFloatPropGetProperty.contains(vfcs.get(0).getName() + " 0"), "GEFloatProp is missing VFC reference");
        assertTrue(geFloatPropGetProperty.contains("EqualFloatProp"), "GEFloatProp is missing EqualFloatProp reference");
        assertTrue(floatProp1GetProperty.contains(vfcs.get(0).getName() + " 0"), "float_prop_1 is missing VFC reference");
        assertTrue(floatProp1GetProperty.contains("ComplexProp"), "float_prop_1 is missing ComplexProp reference");
        assertTrue(floatProp1GetProperty.contains("float_prop"), "float_prop_1 is missing float_prop reference");
        assertTrue(floatProp2GetAttribute.contains(vfcs.get(0).getName() + " 0"), "float_prop_2 is missing VFC reference");
        assertTrue(floatProp2GetAttribute.contains("ComplexProp"), "float_prop_2 is missing ComplexProp reference");
        assertTrue(floatProp2GetAttribute.contains("float_prop"), "float_prop_2 is missing float_prop reference");
        assertTrue(equalFloatPropGetInput.contains("VF_FLOAT_INPUT_2"), "EqualFloatProp is missing VF_FLOAT_INPUT_2 reference");
        assertTrue(lessFloatPropValueGetAttribute.contains("SELF"), "LessFloatProp is missing SELF reference");
        assertTrue(lessFloatPropValueGetAttribute.contains("VF_FLOAT_PROP_3"), "LessFloatProp is missing VF_FLOAT_PROP_3 reference");
        assertTrue(leFloatPropValueGetAttribute.contains(vfcs.get(0).getName() + " 0"), "LEFloatProp is missing VFC reference");
        assertTrue(leFloatPropValueGetAttribute.contains("EqualFloatProp"), "LEFloatProp is missing EqualFloatProp reference");
    }

    /*
        =================================
        =======ADDITIONAL FUNCTIONS =====
        =================================
     */
    public CreateVfcFlow createVFC(final String vfcFullFilename, final String resourceName) {
        final ResourceCreateData vfcCreateData = serviceTemplateDesignUiTests.createVfcFormData(resourceName);
        final CreateVfcFlow createVfcFlow = new CreateVfcFlow(webDriver, vfcCreateData, vfcFullFilename);
        createVfcFlow.run(homePage);
        assertThat(vfcs, notNullValue());
        vfcs.add(vfcCreateData);
        ExtentTestActions.takeScreenshot(Status.INFO, "vfc-created", String.format("VFC '%s' was created", resourceName));
        return createVfcFlow;
    }

    private ResourceCreateData createVfFormData() {
        vf = new ResourceCreateData();
        vf.setRandomName(ElementFactory.getResourcePrefix() + "-VF");
        vf.setCategory(ResourceCategoryEnum.NETWORK_L4.getSubCategory());
        vf.setTagList(Arrays.asList(vf.getName(), "createVF"));
        vf.setDescription("aDescription");
        vf.setVendorName("Ericsson");
        vf.setVendorRelease("6.5.4");
        vf.setVendorModelNumber("3.2.1");
        return vf;
    }

    private AddNodeToCompositionFlow addNodeToCompositionFlow(final ComponentPage componentPage) {
        componentPage.isLoaded();
        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(vf.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.RESOURCE);
        final ComponentData componentToAdd = new ComponentData();
        componentToAdd.setName(vfcs.get(0).getName());
        componentToAdd.setVersion("1.0");
        componentToAdd.setComponentType(ComponentType.RESOURCE);
        final AddNodeToCompositionFlow addNodeToCompositionFlow = new AddNodeToCompositionFlow(webDriver, parentComponent, componentToAdd);
        addNodeToCompositionFlow.run(componentPage.goToComposition());
        return addNodeToCompositionFlow;
    }

    public Map<String, Object> getMapEntry(final Map<?, ?> yamlObj, final String entryName) {
        try {
            return (Map<String, Object>) yamlObj.get(entryName);
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not get the '%s' entry.", entryName);
            LOGGER.error(errorMsg, e);
            fail(errorMsg + "Error message: " + e.getMessage());
        }
        return null;
    }
}
