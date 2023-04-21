package org.onap.sdc.frontend.ci.tests.pages.vfc.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertTrue;

import com.aventstack.extentreports.Status;
import java.util.ArrayList;
import java.util.List;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.execute.sanity.ServiceTemplateDesignUiTests;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfcFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ErrorModal;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
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
    private ResourceCreatePage vfcResourceCreatePage;

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
        vfcResourceCreatePage = createVfcFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ResourceCreatePage"));
        vfcResourceCreatePage.isLoaded();
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
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
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

    // TODO: Change dependant methods
//    @Test(dependsOnMethods = {"addIntPropertyWithValidConstraint", "addIntPropertyWithInRangeConstraint", "addIntPropertyWithValidValueConstraint"})
    @Test(dependsOnMethods = {"addIntPropertyWithValidConstraint", "addIntPropertyWithInRangeConstraint"})
    private void certifyVFC()
    {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());

        vfcResourceCreatePage = new ResourceCreatePage(webDriver);
        vfcResourceCreatePage.isLoaded();
        vfcResourceCreatePage.certifyComponent();
    }

    // TODO: Add tosca verification

    /*
        ==========================
        ====== SERVICE TESTS =====
        ==========================
     */

    // TODO: Create Service
    // TODO: Add VFC to Service
    // TODO: Add VFC Property Tests
    // TODO: Verify Service CSAR

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
}
