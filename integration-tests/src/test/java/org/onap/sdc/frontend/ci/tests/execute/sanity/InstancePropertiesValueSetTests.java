/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.aventstack.extentreports.Status;
import java.util.ArrayList;
import java.util.List;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.TopMenuButtonsEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfcFlow;
import org.onap.sdc.frontend.ci.tests.flow.ImportDataTypeFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.CatalogUIUtilitis;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class InstancePropertiesValueSetTests extends SetupCDTest {

    private WebDriver webDriver;
    private HomePage homePage;
    private List<ResourceCreateData> vfcs = new ArrayList<>();
    private ResourceCreatePage resourceCreatePage;
    private ResourceCreateData vfResourceCreateData;
    private ServiceTemplateDesignUiTests serviceTemplateDesignUiTests;

    @BeforeMethod
    public void init() {
        webDriver = DriverFactory.getDriver();
        homePage = new HomePage(webDriver);
        serviceTemplateDesignUiTests = new ServiceTemplateDesignUiTests();
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "dataTypesList")
    public void importCustomDataTypes(final String rootFolder, final String dataTypeFilename) {
        setLog(dataTypeFilename);
        final ImportDataTypeFlow importDataTypeFlow = importDataType(rootFolder + dataTypeFilename);
        final ResourceCreatePage vfcResourceCreatePage = importDataTypeFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected DataTypeCreatePage"));
        vfcResourceCreatePage.isDataTypePageLoaded();
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "vfcListWithProperties", dependsOnMethods = "importCustomDataTypes")
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
    public void addInstanceToBaseService() {
        homePage.isLoaded();
        resourceCreatePage = (ResourceCreatePage) homePage.clickOnComponent(vfResourceCreateData.getName());
        resourceCreatePage.isLoaded();
        addInstanceToBaseServiceComposition();
    }

    @AfterClass
    public void deleteDataTypeImported() {
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        deleteDataType("tosca.datatypes.testDataTypeSimple");
        deleteDataType("tosca.datatypes.testDataTypeComplex");
    }

    private ImportDataTypeFlow importDataType(final String dataTypeFullFilename) {
        final ImportDataTypeFlow importDataTypeFlow = new ImportDataTypeFlow(webDriver, dataTypeFullFilename);
        importDataTypeFlow.run(homePage);
        ExtentTestActions.takeScreenshot(Status.INFO, "dataType-created", String.format("DataType '%s' was created", dataTypeFullFilename));
        return importDataTypeFlow;
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

    private CreateVfFlow createVF() {
        vfResourceCreateData = serviceTemplateDesignUiTests.createVfFormData();
        final CreateVfFlow createVfFlow = new CreateVfFlow(webDriver, vfResourceCreateData);
        createVfFlow.run(homePage);
        ExtentTestActions.takeScreenshot(Status.INFO, "vf-created", String.format("VF '%s' was created", vfResourceCreateData.getName()));
        return createVfFlow;
    }

    private void addInstanceToBaseServiceComposition() {
        final List<AddNodeToCompositionFlow> addNodeToCompositionFlowList = new ArrayList<>();
        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(vfResourceCreateData.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.RESOURCE);

        // Add VFC to VF composition
        final CompositionPage compositionPage = resourceCreatePage.goToComposition();
        compositionPage.isLoaded();
        assertThat(vfcs, hasSize(3));
        for (ResourceCreateData vfcResourceCreateData: vfcs) {
            final ComponentData vfc = new ComponentData();
            vfc.setName(vfcResourceCreateData.getName());
            vfc.setVersion("1.0");
            vfc.setComponentType(ComponentType.RESOURCE);
            final AddNodeToCompositionFlow addNodeToCompositionFlow = serviceTemplateDesignUiTests.addNodeToComposition(parentComponent, vfc,
                compositionPage);
            addNodeToCompositionFlow.getCreatedComponentInstance()
                .orElseThrow(() -> new UiTestFlowRuntimeException("Could not get the created component instance"));
            addNodeToCompositionFlowList.add(addNodeToCompositionFlow);
        }
        ExtentTestActions.takeScreenshot(Status.INFO, "vfc-added-to-composition",
            String.format("VFCs added to sase service composition"));
    }

    private void deleteDataType(final String dataTypeName) {
        CatalogUIUtilitis.clickOnDataType(dataTypeName).click();
        GeneralUIUtils.getWebElementByTestID("delete").click();
        GeneralUIUtils.getWebElementByTestID("alert-modal-button-ok").click();
    }
}