package org.onap.sdc.frontend.ci.tests.pages.vfc.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

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
import org.onap.sdc.frontend.ci.tests.pages.PropertiesPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceLeftSideMenu;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.PropertiesUIUtils;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VFCIntTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VFCIntTests.class);

    private WebDriver webDriver;
    private HomePage homePage;
    private List<ResourceCreateData> vfcs = new ArrayList<>();
    private ServiceTemplateDesignUiTests serviceTemplateDesignUiTests;
    private ResourceCreatePage vfcResourceCreatePage;

    @BeforeMethod
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

    @Test(dependsOnMethods = "importVfc", expectedExceptions = InterruptedException.class)
    private void addInvalidIntProperty() throws InterruptedException{
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setValue("1.2");
        intProp.setName("InvalidProp");
        PropertiesUIUtils.addNewProperty(intProp);
    }

    @Test(dependsOnMethods = "importVfc", expectedExceptions = InterruptedException.class)
    private void addIntPropertyWithFloatConstraint() throws InterruptedException {
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
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("InvalidProp");
        PropertiesUIUtils.addNewPropertyWithConstraint(intProp, ConstraintType.EQUAL, "12");
        //TODO: Add error popup check
    }

    @Test(dependsOnMethods = "importVfc")
    private void addIntPropertyWithEqualConstraint() throws InterruptedException {
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("EqualIntProp");
        PropertiesUIUtils.addNewPropertyWithConstraint(intProp, ConstraintType.EQUAL, "125");
    }

    @Test(dependsOnMethods = "importVfc")
    private void addIntPropertyWithGreaterOrEqualConstraint() throws InterruptedException {
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("GEIntProp");
        PropertiesUIUtils.addNewPropertyWithConstraint(intProp, ConstraintType.GREATER_OR_EQUAL, "125");
    }

    @Test(dependsOnMethods = "importVfc")
    private void addIntPropertyWithLessOrEqualConstraint() throws InterruptedException {
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("LEIntProp");
        PropertiesUIUtils.addNewPropertyWithConstraint(intProp, ConstraintType.LESS_OR_EQUAL, "125");
    }

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
