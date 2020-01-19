/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.ConnectionWizardPopUpObject;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.PortMirrioringConfigurationObject;
import org.openecomp.sdc.ci.tests.datatypes.PortMirroringEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.PropertiesAssignmentPage;
import org.openecomp.sdc.ci.tests.pages.PropertyNameBuilder;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.pages.UpgradeServicesPopup;
import org.openecomp.sdc.ci.tests.utilities.CatalogUIUtilitis;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.PortMirroringUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.openecomp.sdc.ci.tests.verificator.PropertiesAssignmentVerificator;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

public class UpgradeServices extends SetupCDTest {


    private static final int CREATIN_UPDATE_BUTTON_TIMEOUT = 10 * 60;
    private User sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
    private String filePath;

    @BeforeClass
    public void beforeClass() {
        filePath = org.openecomp.sdc.ci.tests.utilities.FileHandling.getFilePath("");
    }

    @Test
    public void upgradeVfOnServiceLevel() throws Throwable {
        String vnfFile = "2016-227_vmme_vmme_30_1610_e2e.zip";
        //1. Import VSP, create VF, certify
        Resource vfResource = createAndCertifyVFfromVSP(vnfFile);
        //2. Create CR, certify
        ResourceReqDetails crMetaData = createCRAndCertify();
        //3. Create PNF, certify
        ResourceReqDetails pnfMetaData = createPnfAndCertify();

        //4. Create Service1. Service composition - add 2 VFi
        Service service1 = createService();
        getExtendTest().log(Status.INFO, "Add VFi to service twice");
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CanvasManager canvasManager1 = createCanvasManager(service1);
        CanvasElement vfElementVmme1 = canvasManager1.createElementOnCanvas(vfResource.getName());
        CanvasElement vfElementVmme11 = canvasManager1.createElementOnCanvas(vfResource.getName());

        //5. Add CRi, PNFi and PMC to Service1
        getExtendTest().log(Status.INFO, "Add PMC element to service");
        CompositionPage.searchForElement(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());
        CanvasElement pmcElement = canvasManager1.createElementOnCanvas(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());
        getExtendTest().log(Status.INFO, "Add CR and PNF elements to service");
        canvasManager1.createElementOnCanvas(crMetaData.getName());
        canvasManager1.createElementOnCanvas(pnfMetaData.getName());

        //6. Link between VF and PMC. Give values to capability properties. Certify Service1.
        ConnectionWizardPopUpObject connectionWizardPopUpObject = new ConnectionWizardPopUpObject("", "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(), PortMirroringEnum.PMC_SOURCE_CAP.getValue());
        Map<String, String> capPropValues1 = canvasManager1.linkElementsWithCapPropAssignment(vfElementVmme1,
                pmcElement, connectionWizardPopUpObject); //link elements, assign values to properties and save map of values for later validation
        certifyServiceInUI(service1);
        service1.setVersion("1.0");
        service1 = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, service1.getName(), service1.getVersion());
        String vf1CustUuidOrig = AtomicOperationUtils.getServiceComponentInstanceByName(
                service1, vfElementVmme1.getElementNameOnCanvas(), true).getCustomizationUUID();

        //7. Create Service2. Service composition - add VFi. Start certifying Service2 ("certification in progress" stage)
        Service service2 = createService();
        getExtendTest().log(Status.INFO, "Add VFi to service");
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CanvasManager canvasManager2 = createCanvasManager(service2);
        CanvasElement vfElementVmme2 = canvasManager2.createElementOnCanvas(vfResource.getName());
        getExtendTest().log(Status.INFO, "Start service certification");
        AtomicOperationUtils.changeComponentState(service2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();
        service2 = AtomicOperationUtils.getServiceObject(service2.getUniqueId());
        String vf2CustUuidOrig = AtomicOperationUtils.getServiceComponentInstanceByName(
                service2, vfElementVmme2.getElementNameOnCanvas(), true).getCustomizationUUID();

        //8. Create Service3. Service composition - add VFi. Leave service in "checked out" state
        Service service3 = createService();
        getExtendTest().log(Status.INFO, "Add VFi to service");
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        CanvasManager canvasManager3 = createCanvasManager(service3);
        CanvasElement vfElementVmme3 = canvasManager3.createElementOnCanvas(vfResource.getName());
        service3 = AtomicOperationUtils.getServiceObject(service3.getUniqueId()); //updated
        String vf3CustUuidOrig = AtomicOperationUtils.getServiceComponentInstanceByName(
                service3, vfElementVmme3.getElementNameOnCanvas(), true).getCustomizationUUID();

        //9. VF - checkout, save, click "certify"
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(vfResource.getName());
        GeneralPageElements.clickCheckoutButton();
        GeneralPageElements.clickCreateUpdateButton(CREATIN_UPDATE_BUTTON_TIMEOUT);
        GeneralPageElements.clickCertifyButtonNoUpgradePopupDismiss(vfResource.getName());

        //10. Validate that Service1 can be upgraded (checked), Service2 and Service3 are locked
        //TODO - add validation in UI

        //11. Click Upgrade button
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.UPGRADE_SERVICES_OK.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.UPGRADE_SERVICES_CLOSE.getValue());

        //12. Open Service1, verify version (1.1), state(checked in), VFi version (v2.0)
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service1.getName());
        ServiceVerificator.verifyVersionUI("1.1");
        ServiceVerificator.verifyServiceLifecycleInUI(LifeCycleStateEnum.CHECKIN);
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager4 = CanvasManager.getCanvasManager();
        canvasManager4.clickOnCanvaElement(vfElementVmme1);
        ServiceVerificator.verifyResourceInstanceVersionUI("2.0");
        canvasManager4.clickOnCanvaElement(vfElementVmme11);
        ServiceVerificator.verifyResourceInstanceVersionUI("2.0");

        //13. Verify that VFi CustomizationID is different
        service1.setVersion("1.1");
        service1 = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, service1.getName(), service1.getVersion()); //updated
        String vf1CustUuidUpd = AtomicOperationUtils.getServiceComponentInstanceByName(
                service1, vfElementVmme1.getElementNameOnCanvas(), true).getCustomizationUUID();
        Assert.assertTrue(!vf1CustUuidOrig.equals(vf1CustUuidUpd));

        //14. Verify that capabilities properties assignment is kept
        canvasManager4.openLinkPopupReqsCapsConnection(vfElementVmme1, pmcElement); //open connection wizard
        Map<String, String> capPropValues2 = canvasManager4.connectionWizardCollectCapPropValues(); //collect cap prop values
        Assert.assertTrue(capPropValues1.equals(capPropValues2)); // compare cap prop values before and after changing VF version
        canvasManager4.clickSaveOnLinkPopup();

        //15. Open Service2, verify version (0.1), state ("in testing"), VFi version (1.0) and VFi Customization UUID (hasn't changed)
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service2.getName());
        ServiceVerificator.verifyVersionUI("0.1");
        ServiceVerificator.verifyServiceLifecycleInUI(LifeCycleStateEnum.IN_TESTING);
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager5 = CanvasManager.getCanvasManager();
        canvasManager5.clickOnCanvaElement(vfElementVmme2);
        ServiceVerificator.verifyResourceInstanceVersionUI("1.0");
        service2 = AtomicOperationUtils.getServiceObject(service2.getUniqueId()); //updated
        String vf2CustUuidUpd = AtomicOperationUtils.getServiceComponentInstanceByName(
                service2, vfElementVmme2.getElementNameOnCanvas(), true).getCustomizationUUID();
        Assert.assertTrue(vf2CustUuidOrig.equals(vf2CustUuidUpd));

        //16. Open Service3, verify version (0.1), state ("checked out"), VFi version (1.0) and VFi Customization UUID (hasn't changed)
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service3.getName());
        ServiceVerificator.verifyVersionUI("0.1");
        ServiceVerificator.verifyServiceLifecycleInUI(LifeCycleStateEnum.CHECKOUT);
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager6 = CanvasManager.getCanvasManager();
        canvasManager6.clickOnCanvaElement(vfElementVmme3);
        ServiceVerificator.verifyResourceInstanceVersionUI("1.0");
        service3 = AtomicOperationUtils.getServiceObject(service3.getUniqueId()); //updated
        String vf3CustUuidUpd = AtomicOperationUtils.getServiceComponentInstanceByName(
                service3, vfElementVmme3.getElementNameOnCanvas(), true).getCustomizationUUID();
        Assert.assertTrue(vf3CustUuidOrig.equals(vf3CustUuidUpd));

        //17. Open CR, checkout, certify. Verify there is no upgrade popup
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(crMetaData.getName());
        ResourceGeneralPage.clickCheckoutButton();
        ResourceGeneralPage.clickCertifyButton(crMetaData.getName());
        Assert.assertTrue(!UpgradeServicesPopup.isUpgradePopupShown());

        //18. Open PNF, checkout, certify. Verify there is no upgrade popup
        GeneralUIUtils.findComponentAndClick(pnfMetaData.getName());
        ResourceGeneralPage.clickCheckoutButton();
        ResourceGeneralPage.clickCertifyButton(pnfMetaData.getName());
        Assert.assertTrue(!UpgradeServicesPopup.isUpgradePopupShown());
    }

    @Test
    public void upgradeAllottedVfOnServiceLevel() throws Throwable {
        String propUUID = "depending_service_uuid";
        String propInvUUID = "depending_service_invariant_uuid";
        String propName = "depending_service_name";

        //1. Create Service1, certify
        Service service1 = createService();
        getExtendTest().log(Status.INFO, "Certify Service1");
        AtomicOperationUtils.changeComponentState(service1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        String serviceName = service1.getName();
        String serviceInvUUID = service1.getInvariantUUID();
        String serviceUUIDv1 = service1.getUUID();

        //2. Import AllottedResource yaml, create VFC, certify
        String fileName = "Allottedresource.yml";
        ResourceReqDetails vfcMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT,
                ResourceCategoryEnum.ALLOTTED_RESOURCE, getUser());
        ResourceUIUtils.importVfc(vfcMetaData, filePath, fileName, getUser());
        ResourceGeneralPage.clickCertifyButton(vfcMetaData.getName());

        //3. Create VF, add VFCi to canvas
        ResourceReqDetails vfMetaData = createVFviaAPI(ResourceCategoryEnum.ALLOTTED_RESOURCE_TUNNEL_XCONNECT);
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager1 = CanvasManager.getCanvasManager();
        CanvasElement vfcElement1 = canvasManager1.createElementOnCanvas(vfcMetaData.getName());

        //4. VF Properties Assignment: edit values of depending service properties, certify VF
        CompositionPage.moveToPropertiesScreen();
        PropertiesAssignmentPage.findSearchBoxAndClick(propName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(propName), serviceName);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.findSearchBoxAndClick(propUUID);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(propUUID), serviceUUIDv1);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.findSearchBoxAndClick(propInvUUID);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(propInvUUID), serviceInvUUID);
        PropertiesAssignmentPage.clickOnSaveButton();
        ResourceGeneralPage.clickCertifyButton(vfMetaData.getName());

        //5. Create Service2, add VFi to Service2, certify Service2
        Service service2 = createService();
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CanvasManager canvasManager2 = createCanvasManager(service2);
        CanvasElement vfElement1 = canvasManager2.createElementOnCanvas(vfMetaData.getName());
        getExtendTest().log(Status.INFO, "Certify Service2");
        AtomicOperationUtils.changeComponentState(service2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        //6. Create Service3, add VFi to Service3, leave Service3 in checked-out state
        Service service3 = createService();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        CanvasManager canvasManager3 = createCanvasManager(service3);
        CanvasElement vfElement2 = canvasManager3.createElementOnCanvas(vfMetaData.getName());

        //7. Check out and certify Service1 (keep new UUID)
        AtomicOperationUtils.changeComponentState(service1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        AtomicOperationUtils.changeComponentState(service1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        service1 = AtomicOperationUtils.getServiceObject(service1.getUniqueId());
        String serviceUUIDv2 = service1.getUUID();

        //8. Open Service1 and click “Upgrade Services” button
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service1.getName());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.UPDATE_SERVICES_BUTTON.getValue()).click();

        //9. Validate that Service2 can be upgraded (checked), Service3 is locked
        //TODO - add validation in UI

        //10. Click Upgrade button
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.UPDATE_SERVICES_OK.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.UPGRADE_SERVICES_CLOSE.getValue());

        //11. Open VF, verify that version is 2.0
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        VfVerificator.verifyVersionUI("2.0");

        //12. VF properties assignment - verify that UUID value is updated, other values are the same
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(propUUID), serviceUUIDv2);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(propInvUUID), serviceInvUUID);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(propName), serviceName);

        //13. Open Service2, verify version 1.1 and state "in design check in", select VFi, verify version 2.0
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service2.getName());
        ServiceVerificator.verifyVersionUI("1.1");
        ServiceVerificator.verifyServiceLifecycleInUI(LifeCycleStateEnum.CHECKIN);
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager4 = CanvasManager.getCanvasManager();
        canvasManager4.clickOnCanvaElement(vfElement1);
        ServiceVerificator.verifyResourceInstanceVersionUI("2.0");

        //14. Open Service3, select VFi in composition, verify version 1.0
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service3.getName());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager5 = CanvasManager.getCanvasManager();
        canvasManager5.clickOnCanvaElement(vfElement2);
        ServiceVerificator.verifyResourceInstanceVersionUI("1.0");
    }

    @Test
    public void upgradeServiceProxyOnServiceLevel() throws Throwable {
        //1. Create Port Mirroring Service1, certify
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(true);
        Service service1 = portMirrioringConfigurationObject.getService();
        Service serviceSource = portMirrioringConfigurationObject.getServiceContainerVmme_Source();
        CanvasElement pmcElement = portMirrioringConfigurationObject.getPortMirroringConfigurationElement();
        CanvasElement sourceElement = portMirrioringConfigurationObject.getServiceElementVmmeSourceName();
        Map<String, String> capPropValues1 = portMirrioringConfigurationObject.getCapPropValues();
        certifyServiceInUI(service1);
        service1 = AtomicOperationUtils.getServiceObject(service1.getUniqueId());
        String serviceSourceName = portMirrioringConfigurationObject.getServiceElementVmmeSourceName().getElementNameOnCanvas();
        String sourceUuidOrig = AtomicOperationUtils.getServiceComponentInstanceByName(
                service1, serviceSourceName, true).getCustomizationUUID();

        //2. Create Service2, add source service to it
        Service service2 = createService();
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CanvasManager canvasManager1 = createCanvasManager(service2);
        CanvasElement serviceSourceElement = canvasManager1.createElementOnCanvas(serviceSource.getName());

        //3. Checkout and certify serviceSource
        AtomicOperationUtils.changeComponentState(serviceSource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        AtomicOperationUtils.changeComponentState(serviceSource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        serviceSource = AtomicOperationUtils.getServiceObject(serviceSource.getUniqueId());

        //4. Open Service2, click Update button
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(serviceSource.getName());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.UPDATE_SERVICES_BUTTON.getValue()).click();

        //5. Validate that Service1 can be upgraded (checked), Service2 is locked
        //TODO - add validation in UI

        //6. Click Upgrade button
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.UPDATE_SERVICES_OK.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.UPGRADE_SERVICES_CLOSE.getValue());

        //7. Open Service1, verify version 1.1 and state "in design check in", select VFi, verify version 2.0
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service1.getName());
        ServiceVerificator.verifyVersionUI("1.1");
        ServiceVerificator.verifyServiceLifecycleInUI(LifeCycleStateEnum.CHECKIN);
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager2 = CanvasManager.getCanvasManager();
        canvasManager2.clickOnCanvaElement(serviceSourceElement);
        ServiceVerificator.verifyResourceInstanceVersionUI("2.0");

        //8. Verify that capabilities properties values are kept
        canvasManager2.openLinkPopupReqsCapsConnection(sourceElement, pmcElement); //open connection wizard
        Map<String, String> capPropValues2 = canvasManager2.connectionWizardCollectCapPropValues(); //collect cap prop values
        Assert.assertTrue(capPropValues1.equals(capPropValues2)); // compare cap prop values before and after changing VF version
        canvasManager2.clickSaveOnLinkPopup();

        //9. Verify that CustomizationUUID of service source instance is changed
        service1.setVersion("1.1"); //updated
        service1 = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, service1.getName(), service1.getVersion());
        String sourceUuidUpd = AtomicOperationUtils.getServiceComponentInstanceByName(
                service1, serviceSourceName, true).getCustomizationUUID();
        Assert.assertTrue(!sourceUuidOrig.equals(sourceUuidUpd));

        //10. Open Service3, select VFi in composition, verify version 1.0
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.findComponentAndClick(service2.getName());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager3 = CanvasManager.getCanvasManager();
        canvasManager3.clickOnCanvaElement(serviceSourceElement);
        ServiceVerificator.verifyResourceInstanceVersionUI("1.0");
    }


    private CanvasManager createCanvasManager(Service service1) throws Exception {
        GeneralUIUtils.findComponentAndClick(service1.getName());
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        return CanvasManager.getCanvasManager();
    }

    private void certifyServiceInUI(Service service1) throws Exception {
        getExtendTest().log(Status.INFO, "Certify Service");
        ServiceGeneralPage.clickSubmitForTestingButton(service1.getName());
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(service1.getName());
        TesterOperationPage.certifyComponent(service1.getName());
        reloginWithNewRole(UserRoleEnum.DESIGNER);
    }

    private Service createService() throws Exception {
        ServiceReqDetails serviceReqDetails1 = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, "Create Service " + serviceReqDetails1.getName());
        return AtomicOperationUtils.createCustomService(serviceReqDetails1, UserRoleEnum.DESIGNER, true).left().value();
    }

    private ResourceReqDetails createPnfAndCertify() throws Exception {
        ResourceReqDetails pnfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, getUser());
        ResourceUIUtils.createPNF(pnfMetaData, getUser());
        ResourceGeneralPage.clickCertifyButton(pnfMetaData.getName());
        return pnfMetaData;
    }

    private Resource createAndCertifyVFfromVSP(String vnfFile) throws Exception {
        String filePath = FileHandling.getPortMirroringRepositoryPath();
        getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails,
            vendorLicenseModel, null);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource vfResource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        vfResource = (Resource) AtomicOperationUtils.changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        return vfResource;
    }

    private ResourceReqDetails createCRAndCertify() throws Exception {
        ResourceReqDetails crMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.CR, getUser());
        ResourceUIUtils.createCR(crMetaData, getUser());
        ResourceGeneralPage.clickCertifyButton(crMetaData.getName());
        return crMetaData;
    }

    private ResourceReqDetails createVFviaAPI(ResourceCategoryEnum resourceCategory) {
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, resourceCategory, getUser());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating VF %s", vfMetaData.getName()));
        AtomicOperationUtils.createResourceByResourceDetails(vfMetaData, UserRoleEnum.DESIGNER, true).left().value();
        return vfMetaData;
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
