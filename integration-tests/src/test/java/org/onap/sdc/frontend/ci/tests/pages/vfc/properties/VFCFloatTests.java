package org.onap.sdc.frontend.ci.tests.pages.vfc.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.aventstack.extentreports.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.ToscaFunction;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.execute.sanity.ServiceTemplateDesignUiTests;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddComponentPropertyFlow;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfcFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadToscaTemplateFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ImportTypePage;
import org.onap.sdc.frontend.ci.tests.pages.PropertiesPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPropertiesAttributesTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.PropertiesUIUtils;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class VFCFloatTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VFCFloatTests.class);

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

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "SimpleVFC")
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

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "SimpleDataType")
    public void importDataType(final String rootFolder, final String typeFilename) {
        setLog(typeFilename);
        homePage.isLoaded();
        final ImportTypePage importTypePage = homePage.clickOnImportType(rootFolder + typeFilename);

        importTypePage.isLoaded();
        importTypePage.clickOnCreate();
    }

    /*
        ==========================================
        ====== CONSTRAINT AND PROPERTY TESTS =====
        ==========================================
     */

    @DataProvider(name = "invalidFloatProperties", parallel = false)
    private Object[][] invalidFloatProperties() {
        return new Object[][]{
            new Object[]{"1.2.3.4.5.6.7.8.9"},
            new Object[]{"String"},
            new Object[]{"                                        ~"},
        };
    }

    @Test(dependsOnMethods = "importVfc", expectedExceptions = InterruptedException.class, dataProvider = "invalidFloatProperties")
    private void addInvalidFloatProperty(String invalidValue) throws InterruptedException {
        setBrowserBeforeTest(getRole());
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum floatProp = PropertyTypeEnum.FLOAT;
        floatProp.setValue(invalidValue);
        floatProp.setName("InvalidProp");
        PropertiesUIUtils.addNewProperty(floatProp);
    }

    @Test(dependsOnMethods = "importVfc", expectedExceptions = InterruptedException.class)
    private void addFloatPropertyWithInvalidConstraint() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum floatProp = PropertyTypeEnum.FLOAT;
        floatProp.setName("InvalidProp");
        PropertiesUIUtils.addNewPropertyWithConstraint(floatProp, ConstraintType.EQUAL, "12");
    }

    @Test(dependsOnMethods = "importVfc")
    private void addFloatProperties() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum floatProp = PropertyTypeEnum.FLOAT;
        floatProp.setName("float_prop_1");
        PropertiesUIUtils.addNewProperty(floatProp);

        floatProp.setName("float_prop_2");
        PropertiesUIUtils.addNewProperty(floatProp);

        floatProp.setName("float_prop_3");
        PropertiesUIUtils.addNewProperty(floatProp);
    }

    @DataProvider(name = "validProperties", parallel = false)
    private Object[][] validProperties() {
        return new Object[][]{
            new Object[]{"EqualFloatProp", ConstraintType.EQUAL, "1.2"},
            new Object[]{"GreaterFloatProp", ConstraintType.GREATER_THAN, "1"},
            new Object[]{"GEFloatProp", ConstraintType.GREATER_OR_EQUAL, "1.1"},
            new Object[]{"LessFloatProp", ConstraintType.LESS_THAN, "200"},
            new Object[]{"LEFloatProp", ConstraintType.LESS_OR_EQUAL, "125"}
        };
    }

    @Test(dependsOnMethods = "importVfc", dataProvider = "validProperties")
    private void addFloatPropertyWithValidConstraint(String propName, ConstraintType constraintType, String constraintValue) throws InterruptedException {
        LOGGER.info(String.format("Adding property %s with constraint type %s and value %s", propName, constraintType.getType(), constraintValue));
        setBrowserBeforeTest(getRole());
        homePage.isLoaded();
        GeneralUIUtils.ultimateWait();
        homePage.clickOnComponent(vfcs.get(0).getName());

        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        GeneralUIUtils.ultimateWait();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum floatProp = PropertyTypeEnum.FLOAT;
        floatProp.setName(propName);
        PropertiesUIUtils.addNewPropertyWithConstraint(floatProp, constraintType, constraintValue);
    }

    @Test(dependsOnMethods = "importVfc")
    private void addFloatPropertyWithInRangeConstraint() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum floatProp = PropertyTypeEnum.FLOAT;
        floatProp.setName("inRangeFloatProp");
        PropertiesUIUtils.addNewPropertyWithInRangeConstraint(floatProp, "1", "300");
    }

    @Test(dependsOnMethods = "importVfc")
    private void addFloatPropertyWithValidValueConstraint() throws InterruptedException {
        GeneralUIUtils.ultimateWait();
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum floatProp = PropertyTypeEnum.FLOAT;
        String[] validValues = {"1", "1.1", "1.2"};
        floatProp.setName("validValueFloatProp");
        PropertiesUIUtils.addNewPropertyWithValidValuesConstraint(floatProp, validValues);
    }

    @Test(dependsOnMethods = {"importVfc", "importDataType"})
    private void addComplexProperty() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertiesUIUtils.addNewProperty("ComplexProp", "testing.simple.data.type", null, null);
    }

    @Test(dependsOnMethods = {"addFloatPropertyWithValidConstraint", "addFloatPropertyWithInRangeConstraint", "addFloatPropertyWithValidValueConstraint",
        "addComplexProperty", "addFloatProperties"})
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
        final Map<String, Object> simpleVFCTosca = getMapEntry(nodeTypesTosca, "org.openecomp.resource.SimpleVFC");
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
        propertyValues.put("VF_FLOAT_PROP_1", "1.2");
        propertyValues.put("VF_FLOAT_PROP_2", "3.5");
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
            new Object[]{"validValueFloatProp", ToscaFunction.YAML, null, "value: 1.1"}
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
