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

public class VFCIntTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VFCIntTests.class);

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

    @Test(dependsOnMethods = "importVfc")
    private void addIntProperties() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertyTypeEnum intProp = PropertyTypeEnum.INTEGER;
        intProp.setName("int_prop_1");
        PropertiesUIUtils.addNewProperty(intProp);

        intProp.setName("int_prop_2");
        PropertiesUIUtils.addNewProperty(intProp);

        intProp.setName("int_prop_3");
        PropertiesUIUtils.addNewProperty(intProp);
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
        LOGGER.info(String.format("Adding property %s with constraint type %s and value %s", propName, constraintType.getType(), constraintValue));
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

    @Test(dependsOnMethods = {"importVfc", "importDataType"})
    private void addComplexProperty() throws InterruptedException {
        homePage.isLoaded();
        homePage.clickOnComponent(vfcs.get(0).getName());
        ResourceLeftSideMenu leftMenu = new ResourceLeftSideMenu(webDriver);
        leftMenu.isLoaded();
        leftMenu.clickOnPropertiesMenuItem();

        PropertiesUIUtils.addNewProperty("ComplexProp", "testing.simple.data.type", null, null);
    }

    @Test(dependsOnMethods = {"addIntPropertyWithValidConstraint", "addIntPropertyWithInRangeConstraint", "addIntPropertyWithValidValueConstraint",
        "addComplexProperty", "addIntProperties"})
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
