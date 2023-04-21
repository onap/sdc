package org.onap.sdc.frontend.ci.tests.pages.vfc.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.PropertiesPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPropertiesAttributesTab;
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

public class VFCIntTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VFCIntTests.class);

    private WebDriver webDriver;
    private HomePage homePage;
    private List<ResourceCreateData> vfcs = new ArrayList<>();
    private ServiceTemplateDesignUiTests serviceTemplateDesignUiTests;
    private ResourceCreatePage resourceCreatePage;
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
        resourceCreatePage = createVfcFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ResourceCreatePage"));
        resourceCreatePage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "vfc-created",
            String.format("VFC '%s' was created", resourceName));
    }

    /*
        ==========================================
        ====== CONSTRAINT AND PROPERTY TESTS =====
        ==========================================
     */

    @DataProvider(name = "invalidIntProperties", parallel = false)
    private Object[][] invalidIntProperties() {
        return new Object[][]{
            new Object[]{"1.2"},
            new Object[]{"1.2.3.4.5.6.7.8.9"},
            new Object[]{"String"},
            new Object[]{"                                        ~"},
        };
    }

    @Test(dependsOnMethods = "importVfc", expectedExceptions = InterruptedException.class, dataProvider = "invalidIntProperties")
    private void addInvalidIntProperty(String invalidValue) throws InterruptedException {
        setBrowserBeforeTest(getRole());
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setValue(invalidValue);
        intProp.setName("InvalidProp");
        PropertiesUIUtils.addNewProperty(intProp);
    }

    @Test(dependsOnMethods = "importVfc", expectedExceptions = InterruptedException.class)
    private void addIntPropertyWithFloatConstraint() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("InvalidProp");
        PropertiesUIUtils.addNewPropertyWithConstraint(intProp, ConstraintType.EQUAL, "1.2");
    }

    @Test(dependsOnMethods = "importVfc", expectedExceptions = InterruptedException.class)
    private void addIntPropertyWithInvalidConstraint() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("InvalidProp");
        PropertiesUIUtils.addNewPropertyWithConstraint(intProp, ConstraintType.EQUAL, "12");
    }

    @DataProvider(name = "validProperties", parallel = false)
    private Object[][] validProperties() {
        return new Object[][]{
            new Object[]{"EqualIntProp", ConstraintType.EQUAL, "125"},
            new Object[]{"GreaterIntProp", ConstraintType.GREATER_THAN, "1"},
            new Object[]{"GEIntProp", ConstraintType.GREATER_OR_EQUAL, "125"},
            new Object[]{"LessIntProp", ConstraintType.LESS_THAN, "200"},
            new Object[]{"LEIntProp", ConstraintType.LESS_OR_EQUAL, "125"}
        };
    }

    @Test(dependsOnMethods = "importVfc", dataProvider = "validProperties")
    private void addIntPropertyWithValidConstraint(String propName, ConstraintType constraintType, String constraintValue) throws InterruptedException {
        setBrowserBeforeTest(getRole());
        homePage.isLoaded();
        GeneralUIUtils.ultimateWait();
        homePage.clickOnComponent(vfcs.get(0).getName());

        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        GeneralUIUtils.ultimateWait();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName(propName);
        PropertiesUIUtils.addNewPropertyWithConstraint(intProp, constraintType, constraintValue);
    }

    @Test(dependsOnMethods = "importVfc")
    private void addIntPropertyWithInRangeConstraint() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("inRangeIntProp");
        PropertiesUIUtils.addNewPropertyWithInRangeConstraint(intProp, "1", "300");
    }

    @Test(dependsOnMethods = "importVfc")
    private void addIntPropertyWithValidValueConstraint() throws InterruptedException {
        GeneralUIUtils.ultimateWait();
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        String[] validValues = {"1", "125", "300"};
        intProp.setName("validValueIntProp");
        PropertiesUIUtils.addNewPropertyWithValidValuesConstraint(intProp, validValues);
    }

    @Test(dependsOnMethods = {"addIntPropertyWithValidConstraint", "addIntPropertyWithInRangeConstraint", "addIntPropertyWithValidValueConstraint"})
    private void certifyVFC()
    {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());

        resourceCreatePage = new ResourceCreatePage(webDriver);
        resourceCreatePage.isLoaded();
        resourceCreatePage.certifyComponent();
    }

    // TODO: Add tosca verification

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
        homePage.clickOnComponent(vf.getName());
        // TODO: Check to see if VF page loaded

        final AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToCompositionFlow(resourceCreatePage);
        final CompositionPage compositionPage = addNodeToCompositionFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a CompositionPage"));
        compositionPage.isLoaded();
    }

    @Test(dependsOnMethods = "addVFCToVF")
    private void addPropertiesToVF() {
        final Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("VF_INT_PROP_1", "integer");
        propertyMap.put("VF_INT_PROP_2", "integer");
        propertyMap.put("VF_INT_PROP_3", "integer");

        final Map<String, Object> propertyValues = new HashMap<>();
        propertyValues.put("VF_INT_PROP_1", "125");
        propertyValues.put("VF_INT_PROP_2", "300");
        propertyValues.put("VF_INT_PROP_3", "1");

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
        inputMap.put("VF_INT_INPUT_1", "integer");
        inputMap.put("VF_INT_INPUT_2", "integer");
        inputMap.put("VF_INT_INPUT_3", "integer");

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
            new Object[]{"GreaterIntProp", ToscaFunction.GET_PROPERTY, "SELF", "VF_INT_PROP_1"},
            // get_attribute referencing a SELF property
            new Object[]{"LessIntProp", ToscaFunction.GET_ATTRIBUTE, "SELF", "VF_INT_PROP_3"},
            // get_property referencing a property of in instance property
            new Object[]{"GEIntProp", ToscaFunction.GET_PROPERTY, vfcs.get(0).getName() + " 0", "EqualIntProp"},
            // get_attribute referencing a property of in instance property
            new Object[]{"LEIntProp", ToscaFunction.GET_ATTRIBUTE, vfcs.get(0).getName() + " 0", "EqualIntProp"},
            new Object[]{"EqualIntProp", ToscaFunction.GET_INPUT, null, "VF_INT_INPUT_2"}
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

    // TODO: Add VFC Property Tests get_property referencing a sub property of an instance property
    // TODO: Add VFC Property Tests get_attribute referencing a sub property of an instance property
    // TODO: Add VFC Property Tests yaml

    // TODO: Verify VF

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
}
