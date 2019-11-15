/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.HeatParamNameBuilder;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.PropertiesAssignmentPage;
import org.openecomp.sdc.ci.tests.pages.PropertiesPage;
import org.openecomp.sdc.ci.tests.pages.PropertyNameBuilder;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.CatalogUIUtilitis;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.openecomp.sdc.ci.tests.verificator.PropertiesAssignmentVerificator;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

public class PropertiesAssignmentUpdateFlow_New extends SetupCDTest {

    private static String filePath;
    private static String origFile = "virc_fe_be.csar";
    private static String origFile1 = "virc.csar";
    private User sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);


    @BeforeClass
    public void beforeClass() {
        filePath = FileHandling.getFilePath("");
    }

    @BeforeMethod
    public void beforeTest() {
        System.out.println("File repository is : " + filePath);
//        SetupCDTest.getExtendTest().log(Status.INFO, "File repository is : " + filePath);
    }

    //Update VSP tests - validating VF properties/inputs values after updating VF with a new VSP
    @DataProvider(name = "assetFilesInputs", parallel = false)
    public Object[][] createDataInputs() {
        return new Object[][]{
                {"editInputValueTopChange.csar", ""},
                {"editInputValueNoTopChange.csar", "Updated-SRE-Mgt"}};
    }

    /**
     * Topoplogy change in updated file - designer changes in inputs values are not kept after update
     * No topoplogy change in updated file - designer changes in inputs values are kept after update
     *
     * @param updFileName     - csar for update
     * @param validationValue - expected property value after update
     * @throws Exception
     */
    @Test(dataProvider = "assetFilesInputs")
    public void updateInputDefaultValueTest(String updFileName, String validationValue) throws Exception {
        setLog(updFileName);

        String origTestFile = origFile;
        String componentName = "abstract_virc_fe_be_0";
        String inputName = "management_net_name";
        String inputUpdValue = "Updated-SRE-Mgt";

        //Import csar
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT,
                ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, origTestFile, getUser());

        //Edit Input value
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildInputField(inputName), inputUpdValue);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Certify  VF via API
        Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();


        HomePage.navigateToHomePage();
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());

        //Update VF
        ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
        GeneralPageElements.clickCheckoutButton();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file"));
        ResourceUIUtils.updateVfWithCsar(filePath, updFileName);

        //Validate Input value - 1) empty in case of topology change; 2) user value is kept if no topology change
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildInputField(inputName), validationValue);

    }


    @DataProvider(name = "assetFilesProperties", parallel = false)
    public Object[][] createDataProp() {
        return new Object[][]{
                {"editedPropValueToEmpty.csar", "value-prop-vm_type_tag", "updated_by_designer", "updated_by_designer", false}, //no topology change - simple
                {"newPropValueToEmpty.csar", "value-prop-high_availablity", "updated_by_designer", "updated_by_designer", false},
                {"editedPropValueToNew.csar", "value-prop-vm_type_tag", "updated_by_designer", "updated_by_file", false},
                {"virc_fe_be.csar", "value-prop-vm_type_tag", "", "virc_fe_be", true},
                {"editedPropValueToEmptyTopChange.csar", "value-prop-vm_type_tag", "updated_by_designer", "", false}, //topology change - simple
                {"newPropValueToEmptyTopChange.csar", "value-prop-high_availablity", "updated_by_designer", "", false},
                {"editedPropValueToNewTopChange.csar", "value-prop-vm_type_tag", "updated_by_designer", "updated_by_file", false},
                {"virc_fe_be.csar", "value-prop-port_virc_fe_be_data_1_port_mac_requirements.mac_range_plan", "updated_by_designer", "updated_by_designer", false}, //no topology change - complex
                {"virc_fe_be.csar", "value-prop-service_template_filter.count", "12345", "1", false},
                {"virc_fe_be.csar", "value-prop-service_template_filter.substitute_service_template", "", "Nested_virc_fe_beServiceTemplate.yaml", true}};
    }

    /**
     * No topology change in updated file - designer changes in properties values are kept after update
     * OrigFile       Designer       UpdFile       Result After Update
     * ---------------------------------------------------------------
     * prop1=value1   prop1=value2   prop1=null    prop1=value2
     * prop1=null     prop1=value1   prop1=null    prop1=value1
     * prop1=value1   prop1=value2   prop1=value3  prop1=value3
     * prop1=value1   prop1=null     prop1=value1  prop1=value1
     *
     *
     * Topoplogy change in updated file - designer changes in properties values are not kept after update
     * OrigFile       Designer       UpdFile       Result After Update
     * ---------------------------------------------------------------
     * prop1=value1   prop1=value2   prop1=null    prop1=null
     * prop1=null     prop1=value1   prop1=null    prop1=null
     * prop1=value1   prop1=value2   prop1=value3  prop1=value3
     *
     *
     * No topology change in updated files - complex property value changes
     * OrigFile       Designer       UpdFile       Result After Update
     * ---------------------------------------------------------------
     * prop1=null     prop1=value1   prop1=null    prop1=value1
     * prop1=value1   prop1=value2   prop1=value1  prop1=value1
     * prop1=value1   prop1=null     prop1=value1  prop1=value1
     *
     * @param updFileName     - csar for update
     * @param propertyName
     * @param validationValue - expected property value after update
     * @throws Exception
     */
    @Test(dataProvider = "assetFilesProperties")
    public void updatePropertyDefaultValueTest(String updFileName, String propertyName, String updValue, String validationValue, boolean userDeletesValue) throws Exception {
        setLog(updFileName);
        String origTestFile = origFile;
        String componentName = "abstract_virc_fe_be_0";
//        String propertyUpdValue = "updated_by_designer";

        //Import csar
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT,
                ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, origTestFile, getUser());

        //Edit or delete Input value
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        if (userDeletesValue) {
            PropertiesAssignmentPage.deletePropertyValue(propertyName);
        } else {
            PropertiesAssignmentPage.editPropertyValue(propertyName, updValue);
        }
        PropertiesAssignmentPage.clickOnSaveButton();

        //Certify  VF via API
        Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        //Update VF
        HomePage.navigateToHomePage();
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToGeneralScreen();
        GeneralPageElements.clickCheckoutButton();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating VF with new file"));
        ResourceUIUtils.updateVfWithCsar(filePath, updFileName);

        //Validate Input value
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyName, validationValue);
    }


    //ChangeVersion tests - validating service property values after changing VFi version on service
    @DataProvider(name = "assetFilesServiceProperties", parallel = false)
    public Object[][] createDataServiceProp() {
        return new Object[][]{
                {"max_instances", "", "111", "111"}, //edit service prop value, update with original empty value, keep user value after update
                {"nfc_naming_code", "virc_fe_be", "updated_by_designer", "virc_fe_be"}}; //edit service prop value, update with original value, set original value after update
    }

    @Test(dataProvider = "assetFilesServiceProperties")
    public void changeVfiVersionServicePropertyEditValueTest(String propertyName, String origValue, String updValue, String expectedValue) throws Throwable {
        setLog(propertyName);
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "abstract_virc_fe_be_1";
        String inputName = componentName + "_" + propertyName;

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //VF - Declare property as input
        declarePropertyAsInput(propertyName, componentName, resourceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildInputField(inputName), origValue);

        //Check in VF, create Service and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF inputs in Service properties, add or edit properties values
        findServiceProperty(inputName, serviceReqDetails);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(inputName), updValue);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Checkout and check in VF, change VFi version on Service to 0.2
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

        //Service - Validate properties values
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(inputName), expectedValue);
    }


    @Test
    public void changeVfiVersionServicePropertyDeleteValueTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "abstract_virc_fe_be_1";
        String propertyName = "min_instances";
        String propertyValue = "0";
        String inputName = componentName + "_" + propertyName;

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //VF - Declare properties as inputs
        declarePropertyAsInput(propertyName, componentName, resourceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildInputField(inputName), propertyValue);

        //Check in VF, create Service and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF inputs in Service properties, delete property value
        findServiceProperty(inputName, serviceReqDetails);
        PropertiesAssignmentPage.deletePropertyValue(PropertyNameBuilder.buildSimpleField(inputName));
        PropertiesAssignmentPage.clickOnSaveButton();

        //Checkout and check in VF, change VFi version on Service to 0.2
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

        //Service - Validate properties values
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(inputName), propertyValue);
    }

    @Test
    public void changeVfiVersionServiceGenericPropertyValueTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "abstract_virc_fe_be_1";
        String propertyName = "nf_type";
        String propertyUpdValue = "added by designer on service";

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //Check in VF, create Service and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF inputs in Service properties, add or edit property value
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(propertyName), propertyUpdValue);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Checkout and check in VF, change VFi version on Service to 0.2
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

        //Service - Validate property value
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(propertyName), propertyUpdValue);
    }


    //ChangeVersion tests - validating service input values after changing VFi version on service
    @Test
    public void changeVfiServiceInputEditValueTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "virc_fe_be_volume_0";
        String propertyName = "disk_bus";
        String propertyValue = "added_by_designer";
        String propertyValueUpd = "updated_by_designer";
        String inputName = componentName + "_" + propertyName;

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //Edit Property Value and declare as input
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        viewVfComponentProperties(componentName, resourceReqDetails);
        editAndDeclareSimpleProperty(propertyName, propertyValue);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildInputField(inputName), propertyValue);

        //Check in VF and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF input in Service properties, declare it as service input
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(inputName), propertyValue);
        findAndDeclareServiceProperty(inputName);
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName), propertyValue);

        //Checkout VF, update input value and check in - v0.2
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        viewVfInputs(resourceReqDetails);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildDeclaredInputField(componentName, propertyName),
                propertyValueUpd);
        PropertiesAssignmentPage.clickOnSaveButton();
        ResourceGeneralPage.clickCheckinButton(resourceReqDetails.getName());

        //Change resource version on service
        resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resource.getName(), "0.2");
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

        //Validate that service contains property with "get_input" value and input with the updated value
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.isPropertyChecked(inputName);
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServicePropertyValue(componentName, propertyName),
                        PropertyNameBuilder.buildServiceDeclaredPropertyValue(componentInstance, componentName, propertyName));
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName), propertyValueUpd);
    }

    @Test
    public void changeVfiServiceInputEditBooleanTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "virc_fe_be_volume_0";
        String propertyName = "multi_stage_design";
        String origValue = "FALSE";
        String updValue = "TRUE";

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //Check in VF and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF input in Service properties, declare it as service input
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
        PropertiesAssignmentPage.selectBooleanPropertyValue(PropertyNameBuilder.buildSimpleField(propertyName), updValue);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.clickOnDeclareButton();
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validateBooleanPropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldServiceLevel(componentInstance, propertyName),
                        updValue);

//        //Checkout VF and check in - v0.2
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

//        //Change resource version on service
        resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resource.getName(), "0.2");
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

//        //Validate that service contains property with "get_input" value and input with the updated value
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(propertyName),
                PropertyNameBuilder.buildServiceDeclaredPropValueServiceLevel(componentInstance, propertyName));
        PropertiesAssignmentPage.isPropertyChecked(propertyName);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validateBooleanPropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldServiceLevel(componentInstance, propertyName),
                        origValue);
    }

    @Test
    public void changeVfiServiceInputDeleteValueTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "abstract_virc_fe_be_0";
        String propertyName = "vm_type_tag";
        String propertyValue = "updated_by_designer";
        String inputName = componentName + "_" + propertyName;

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //Edit Property Value and declare as input
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        viewVfComponentProperties(componentName, resourceReqDetails);
        editAndDeclareSimpleProperty(propertyName, propertyValue);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildInputField(inputName), propertyValue);

        //Check in VF and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF input in Service properties, declare it as service input
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(inputName), propertyValue);
        findAndDeclareServiceProperty(inputName);
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName), propertyValue);

        //Checkout VF, update input value and check in - v0.2
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        viewVfInputs(resourceReqDetails);
        PropertiesAssignmentPage.deletePropertyValue(PropertyNameBuilder.buildDeclaredInputField(componentName, propertyName));
        PropertiesAssignmentPage.clickOnSaveButton();
        ResourceGeneralPage.clickCheckinButton(resourceReqDetails.getName());

        //Change resource version on service
        resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resource.getName(), "0.2");
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

        //Validate that service contains property with "get_input" value and input with the updated value
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.isPropertyChecked(inputName);
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServicePropertyValue(componentName, propertyName),
                        PropertyNameBuilder.buildServiceDeclaredPropertyValue(componentInstance, componentName, propertyName));
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName), propertyValue);
    }

    @Test
    public void changeVfiServiceInputAddValueTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "virc_fe_be_volume_0";
        String propertyName = "disk_bus";
        String propertyValue = "updated_by_designer";
        String inputName = componentName + "_" + propertyName;

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //Edit Property Value and declare as input
        declarePropertyAsInput(propertyName, componentName, resourceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildInputField(inputName), "");

        //Check in VF and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF input in Service properties, declare it as service input
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(inputName), "");
        findAndDeclareServiceProperty(inputName);
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName), "");

        //Checkout VF, update input value and check in - v0.2
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        viewVfInputs(resourceReqDetails);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildDeclaredInputField(componentName, propertyName), propertyValue);
        PropertiesAssignmentPage.clickOnSaveButton();
        ResourceGeneralPage.clickCheckinButton(resourceReqDetails.getName());

        //Change resource version on service
        resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resource.getName(), "0.2");
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

        //Validate that service contains property with "get_input" value and input with the updated value
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.isPropertyChecked(inputName);
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServicePropertyValue(componentName, propertyName),
                        PropertyNameBuilder.buildServiceDeclaredPropertyValue(componentInstance, componentName, propertyName));
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName), propertyValue);
    }

    @Test
    public void changeVfiServiceInputDeleteInputTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String componentName = "virc_fe_be_volume_0";
        String propertyName = "disk_bus";
        String propertyValue = "added_by_designer";
        String inputName = componentName + "_" + propertyName;

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //Edit Property Value and declare as input
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        viewVfComponentProperties(componentName, resourceReqDetails);
        editAndDeclareSimpleProperty(propertyName, propertyValue);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildInputField(inputName), propertyValue);

        //Check in VF and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();

        //Find the VF input in Service properties, declare it as service input
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(inputName), propertyValue);
        findAndDeclareServiceProperty(inputName);
        PropertiesAssignmentVerificator.validatePropertyValue(
                PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName), propertyValue);

        //Checkout VF, delete input  and check in - v0.2
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        viewVfInputs(resourceReqDetails);
        PropertiesAssignmentPage.clickOnDeleteInputButton();
        PropertiesAssignmentPage.clickOnDeleteInputDialogConfirmationButton();
        ResourceGeneralPage.clickCheckinButton(resourceReqDetails.getName());

        //Change resource version on service
        resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resource.getName(), "0.2");
        AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);

        //Validate that service property and input are deleted
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.findProperty(inputName);
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId(PropertyNameBuilder.buildSimpleField(inputName)));
        PropertiesAssignmentPage.clickOnInputTab();
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId(PropertyNameBuilder.buildServiceDeclaredFieldVfLevel(componentInstance, componentName, propertyName)));
    }


    //ChangeVersion test - validating VF input values after changing VFCi version on VF
    @Test
    public void updateVFCPropertyChangeVFCiVersionTest() throws Throwable {
        ResourceReqDetails atomicResourceMetaData;
        String prop1 = "network_role";
        String propValue1 = "added_value_1";
        String prop2 = "is_default";
        String propValue2 = "TRUE";
        String propDefaultValue2 = "FALSE";

        //Import VFC, certify VFC
        String fileName = "importVFC_VFC23.yml";
        atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT,
                ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
        ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
        String vfcName = atomicResourceMetaData.getName();
        ResourceGeneralPage.clickCheckinButton(vfcName);

        //Create VF
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        //Add VFCi to VF canvas
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement vfcElement = vfCanvasManager.createElementOnCanvas(vfcName);

        //VF Properties Assignment - edit properties values and declare as input
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.findSearchBoxAndClick(prop1);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(prop1), propValue1);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.clickOnDeclareButton();
        PropertiesAssignmentPage.findSearchBoxAndClick(prop2);
        PropertiesAssignmentPage.selectBooleanPropertyValue(PropertyNameBuilder.buildSimpleField(prop2), propValue2);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.clickOnDeclareButton();

        //Check out and check in VFC - v0.2
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(vfcName);
        ResourceGeneralPage.clickCheckoutButton();
        ResourceGeneralPage.clickCheckinButton(vfcName);

        //Change VFCi version in VF
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        vfCanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.changeComponentVersion(vfCanvasManager, vfcElement, "0.2");
        VfVerificator.verifyInstanceVersion(vfMetaData, getUser(), atomicResourceMetaData.getName(), "0.2");

        Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, vfMetaData.getName(), "0.1");
        List<ComponentInstance> components = resource.getComponentInstances();
        String normalizedName = components.get(0).getNormalizedName();
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.clickOnInputTab();
        //Verify that input value of the declared property that is EMPTY by default hasn't changed
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildDeclaredInputField(normalizedName, prop1), propValue1);
        //Verify that input value of the declared property that is NOT EMPTY by default has been changed
        PropertiesAssignmentVerificator.validateBooleanPropertyValue(PropertyNameBuilder.buildDeclaredInputField(normalizedName, prop2), propDefaultValue2);

        //Check out VFC - v0.3, delete a property, check in VFC
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(vfcName);
        ResourceGeneralPage.clickCheckoutButton();
        ResourceGeneralPage.getLeftMenu().moveToPropertiesScreen();
        PropertiesPage.clickDeletePropertyFromPopup(prop1);
        PropertiesPage.clickDeletePropertyFromPopup(prop2);
        ResourceGeneralPage.clickCheckinButton(vfcName);

        //Change VFCi version in VF
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        vfCanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.changeComponentVersion(vfCanvasManager, vfcElement, "0.3");
        VfVerificator.verifyInstanceVersion(vfMetaData, getUser(), atomicResourceMetaData.getName(), "0.3");

        //Verify that properties and inputs were removed
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.findProperty(prop1);
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId(PropertyNameBuilder.buildSimpleField(prop1)));
        PropertiesAssignmentPage.findProperty(prop2);
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId(PropertyNameBuilder.buildSimpleField(prop2)));
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.findProperty(normalizedName + "_" + prop1);
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId(PropertyNameBuilder.buildDeclaredInputField(normalizedName, prop1)));
        PropertiesAssignmentPage.findProperty(normalizedName + "_" + prop2);
        assertTrue(GeneralUIUtils.isElementInvisibleByTestId(PropertyNameBuilder.buildDeclaredInputField(normalizedName, prop2)));
    }

    //ChangeVersion test - validating HEAT param values after changing VFi version on Service
    @Test
    public void updateHeatParamChangeVfiVersionTest() throws Throwable {
        String vnfFile = "vIRC_FE_BE.zip";
        String artifactName = "base_fe_be";
        String heatParamName = "availability_zone_0";
        String heatParamUpdValue = "Updated_ZoneA";

        //Import VSP, create VF - v0.1
        String filePath = org.openecomp.sdc.ci.tests.utils.general.FileHandling.getVnfRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);

        //Check in VF and add VFi to Service
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service: " + serviceReqDetails.getName());
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(
                serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
        CanvasElement vfElement = vfCanvasManager.createElementOnCanvas(resource.getName());

        //Select VFi on canvas, open Deployment Artifacts tab
        vfCanvasManager.clickOnCanvaElement(vfElement);
        CompositionPage.showDeploymentArtifactTab();
        DeploymentArtifactPage.hoverArtifact(artifactName);
        DeploymentArtifactPage.clickEditEnvArtifact(artifactName);
        DeploymentArtifactPage.editHeatParamValue(HeatParamNameBuilder.buildCurrentHeatParamValue(heatParamName), heatParamUpdValue);
        DeploymentArtifactPage.clickSaveEnvParameters();

        //Checkout and check in VF - v0.2
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

        //Change VFi version on Service canvas - v0.2
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service.getName());
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        vfCanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.changeComponentVersion(vfCanvasManager, vfElement, "0.2");
//        VfVerificator.verifyInstanceVersion(service, getUser(), resource.getName(), "0.2");

        //Validate that edited heatparam value is kept
        CompositionPage.showDeploymentArtifactTab();
        DeploymentArtifactPage.hoverArtifact(artifactName);
        DeploymentArtifactPage.clickEditEnvArtifact(artifactName);
        PropertiesAssignmentVerificator.validatePropertyValue(HeatParamNameBuilder.buildCurrentHeatParamValue(heatParamName), heatParamUpdValue);
        DeploymentArtifactPage.clickCloseEnvParameters();

        //Check out VF, delete heatparam value, check in VF - v0.3
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(resource.getName());
        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        DeploymentArtifactPage.clickEditEnvArtifact(artifactName);
        DeploymentArtifactPage.clickOnDeleteHeatParamValue(heatParamName);
        DeploymentArtifactPage.clickSaveEnvParameters();
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

        //Change VFi version on Service canvas - v0.3
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service.getName());
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        vfCanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.changeComponentVersion(vfCanvasManager, vfElement, "0.3");

        //Validate that edited heatparam value is kept, default value is empty
        CompositionPage.showDeploymentArtifactTab();
        DeploymentArtifactPage.hoverArtifact(artifactName);
        DeploymentArtifactPage.clickEditEnvArtifact(artifactName);
        PropertiesAssignmentVerificator.validatePropertyValue(HeatParamNameBuilder.buildCurrentHeatParamValue(heatParamName), heatParamUpdValue);
        PropertiesAssignmentVerificator.validatePropertyValueIsNull(HeatParamNameBuilder.buildDefaultHeatParamValue(heatParamName));
        DeploymentArtifactPage.clickCloseEnvParameters();
    }


    private void declarePropertyAsInput(String propertyName, String componentName, ResourceReqDetails resourceReqDetails) throws Exception {
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        viewVfComponentProperties(componentName, resourceReqDetails);
        findAndDeclareServiceProperty(propertyName);
    }

    private void viewServiceProperties(ServiceReqDetails serviceReqDetails) throws Exception {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());
        CompositionPage.moveToPropertiesScreen();
    }

    private void findServiceProperty(String inputName, ServiceReqDetails serviceReqDetails) throws Exception {
        viewServiceProperties(serviceReqDetails);
        PropertiesAssignmentPage.findSearchBoxAndClick(inputName);
    }

    private void viewVfComponentProperties(String componentName, ResourceReqDetails resourceReqDetails) throws Exception {
        GeneralUIUtils.findComponentAndClick(resourceReqDetails.getName());
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
    }

    private void findAndDeclareServiceProperty(String inputName) throws Exception {
        PropertiesAssignmentPage.findSearchBoxAndClick(inputName);
        PropertiesAssignmentPage.clickOnDeclareButton();
        PropertiesAssignmentPage.clickOnInputTab();
    }

    private void editAndDeclareSimpleProperty(String propertyName, String propertyValue) throws Exception {
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(propertyName), propertyValue);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
        PropertiesAssignmentPage.clickOnDeclareButton();
    }

    private void viewVfInputs(ResourceReqDetails resourceReqDetails) throws Exception {
        GeneralUIUtils.findComponentAndClick(resourceReqDetails.getName());
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnInputTab();
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}


