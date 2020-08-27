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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceGeneralPage;
import org.onap.sdc.frontend.ci.tests.utilities.CatalogUIUtilitis;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.HomeUtils;
import org.onap.sdc.frontend.ci.tests.utilities.PortMirroringUtils;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.general.FileHandling;
import org.onap.sdc.frontend.ci.tests.verificator.PortMirroringVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.ConnectionWizardPopUpObject;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.PortMirrioringConfigurationObject;
import org.onap.sdc.frontend.ci.tests.datatypes.PortMirroringEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceContainer;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.frontend.ci.tests.datatypes.TopMenuButtonsEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class PortMirroring extends SetupCDTest {

    private static final int THREAD_SLEEP_TIME = 3000;

    @Test
    public void createPortMirroringConfigurationServiceProxy() throws Throwable {
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        ServiceReqDetails serviceReqDetails = portMirrioringConfigurationObject.getServiceReqDetails();
        CanvasManager canvasManager = portMirrioringConfigurationObject.getCanvasManager();
        CanvasElement serviceElementVmmeSourceName = portMirrioringConfigurationObject.getServiceElementVmmeSourceName();
        String vmmeSourceName = portMirrioringConfigurationObject.getVmmeSourceName();
        CanvasElement serviceElementVprobeCollector = portMirrioringConfigurationObject.getServiceElementVprobeCollector();
        String vprobeSourceName = portMirrioringConfigurationObject.getVprobeSourceName();

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating links between elements were created");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 2);

        canvasManager.clickOnCanvaElement(serviceElementVmmeSourceName);
        PortMirroringVerificator.validatingProxyServiceNameAndType(vmmeSourceName, "0");

        canvasManager.clickOnCanvaElement(serviceElementVprobeCollector);
        PortMirroringVerificator.validatingProxyServiceNameAndType(vprobeSourceName, "0");
    }

    @Test
    public void distributePortMirroringConfigurationServiceProxy() throws Throwable {
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        ServiceReqDetails serviceReqDetails = portMirrioringConfigurationObject.getServiceReqDetails();
        Service service = portMirrioringConfigurationObject.getService();

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating links between elements were created");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 2);

        getExtendTest().log(Status.INFO, String.format("Going to certify the Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Service is certified"));
        Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
        getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
        AssertJUnit.assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
    }

    @Test
    public void createPortMirroringByPolicyServiceProxy() throws Throwable {
        //Using API onboard and certify 2 zip files Source: vmmme and Collector: Vprobe
        String filePath = FileHandling.getPortMirroringRepositoryPath();
        ServiceContainer serviceContainerVmme_Source = PortMirroringUtils.createServiceFromHeatFile(filePath, "2016-227_vmme_vmme_30_1610_e2e.zip");

        // create service with required pnf's and certify it
        Resource resourceCisco = PortMirroringUtils.generatePNFAndUpdateInput(PortMirroringEnum.CISCO_VENDOR_NAME.getValue(), PortMirroringEnum.CISCO_VENDOR_MODEL_NUMBER.getValue(), getUser());
        Resource resourceAPCON1 = PortMirroringUtils.generatePNFAndUpdateInput(PortMirroringEnum.APCON1_VENDOR_NAME.getValue(), PortMirroringEnum.APCON1_VENDOR_MODEL_NUMBER.getValue(), getUser());
        Resource resourceAPCON2 = PortMirroringUtils.generatePNFAndUpdateInput(PortMirroringEnum.APCON2_VENDOR_NAME.getValue(), PortMirroringEnum.APCON2_VENDOR_MODEL_NUMBER.getValue(), getUser());

        ServiceReqDetails serviceReqDetailsCollector = ElementFactory.getDefaultService();
        serviceReqDetailsCollector.setServiceType(PortMirroringEnum.SERVICE_TYPE.getValue());
        getExtendTest().log(Status.INFO, String.format("Creating collector service %s (PNF container)", serviceReqDetailsCollector.getName()));
        Service serviceCollector = AtomicOperationUtils.createCustomService(serviceReqDetailsCollector, UserRoleEnum.DESIGNER, true).left().value();

        getExtendTest().log(Status.INFO, String.format("Adding pnf's: %s,%s,%s to service %s", resourceCisco.getName(), resourceAPCON1.getName(), resourceAPCON2.getName(), serviceCollector.getName()));

        AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceCisco, serviceCollector, UserRoleEnum.DESIGNER, true, "80", "80");
        AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceAPCON1, serviceCollector, UserRoleEnum.DESIGNER, true, "80", "200");
        AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceAPCON2, serviceCollector, UserRoleEnum.DESIGNER, true, "200", "200");

        serviceCollector = (Service) AtomicOperationUtils.changeComponentState(serviceCollector, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        // create container service
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        getExtendTest().log(Status.INFO, String.format("Creating container service %s", serviceReqDetails.getName()));
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        String vmmeSourceName = serviceContainerVmme_Source.getService().getName();
        String collectorServiceName = serviceCollector.getName();

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CompositionPage.searchForElement(vmmeSourceName);
        CanvasElement serviceElementVmmeSource = canvasManager.createElementOnCanvas(vmmeSourceName);

        CompositionPage.searchForElement(collectorServiceName);
        CanvasElement serviceElementCollectorService = canvasManager.createElementOnCanvas(collectorServiceName);

        CompositionPage.searchForElement(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationByPolicyElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME = new ConnectionWizardPopUpObject("",
                "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(),
                PortMirroringEnum.PMCP_SOURCE_CAP.getValue());

        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSource,
                portMirroringConfigurationByPolicyElement,
                connectionWizardPopUpObjectVMME);

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating link between elements was created");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 1);

        canvasManager.clickOnCanvaElement(serviceElementVmmeSource);
        PortMirroringVerificator.validatingProxyServiceNameAndType(vmmeSourceName, "0");

        getExtendTest().log(Status.INFO, "Adding properties to PMCP");

        SetupCDTest.getExtendTest().log(Status.INFO, "Open bug 373762, can't update properties on CP or VFC instance  on Composition screen");

        canvasManager.clickOnCanvaElement(portMirroringConfigurationByPolicyElement);
        CompositionPage.showPropertiesAndAttributesTab();
        CompositionPage.setSingleProperty(DataTestIdEnum.PortMirroring.COLLECTOR_NODE.getValue(), PortMirroringUtils.createproxyinstanceservicename(collectorServiceName, "1"));
        CompositionPage.setSingleProperty(DataTestIdEnum.PortMirroring.EQUIP_MODEL.getValue(), PortMirroringEnum.CISCO_VENDOR_MODEL_NUMBER.getValue());
        CompositionPage.setSingleProperty(DataTestIdEnum.PortMirroring.EQUIP_VENDOR.getValue(), PortMirroringEnum.CISCO_VENDOR_NAME.getValue());

        // Distribute the Port Mirroning Configuration By Policy
        getExtendTest().log(Status.INFO, String.format("Going to certify the Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Service is certified"));
        Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
        getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
        AssertJUnit.assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
    }

    @Test
    public void deletePMCPLink() throws Throwable {
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        getExtendTest().log(Status.INFO, String.format("Creating container service %s", serviceReqDetails.getName()));
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        String filePath = FileHandling.getPortMirroringRepositoryPath();
        ServiceContainer serviceContainerVmme_Source = PortMirroringUtils.createServiceFromHeatFile(filePath, "2016-227_vmme_vmme_30_1610_e2e.zip");

        String vmmeSourceName = serviceContainerVmme_Source.getService().getName();

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CompositionPage.searchForElement(vmmeSourceName);
        CanvasElement serviceElementVmmeSource = canvasManager.createElementOnCanvas(vmmeSourceName);

        CompositionPage.searchForElement(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationByPolicyElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME = new ConnectionWizardPopUpObject("",
                "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(),
                PortMirroringEnum.PMCP_SOURCE_CAP.getValue());

        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSource,
                portMirroringConfigurationByPolicyElement,
                connectionWizardPopUpObjectVMME);

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating link between elements was created");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 1);

        canvasManager.openLinkPopupReqsCapsConnection(serviceElementVmmeSource, portMirroringConfigurationByPolicyElement);
        canvasManager.closeLinkPopupReqsCapsConnection();
        canvasManager.deleteLinkPopupReqsCapsConnection(serviceElementVmmeSource, portMirroringConfigurationByPolicyElement);

        getExtendTest().log(Status.INFO, "Validating link deleted");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 0);
    }

    @Test
    public void verifyPMCNotExistInVF() throws Exception {
        Resource resource = PortMirroringUtils.getresourcebytype(ResourceTypeEnum.VF, "VF", "VendorModel");

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);

        GeneralUIUtils.findComponentAndClick(resource.getName());
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        CompositionPage.searchForElement(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());

        getExtendTest().log(Status.INFO, "Verify that Port Mirroring Configuration and Configuration by Policy doesn't exist for VF ");
        boolean isPMCFound = GeneralUIUtils.isElementInvisibleByTestId(DataTestIdEnum.PortMirroring.PMC_NAME_IN_PALLETE.getValue());
        boolean isPMCPFound = GeneralUIUtils.isElementInvisibleByTestId(DataTestIdEnum.PortMirroring.PMCP_NAME_IN_PALLETE.getValue());

        assertTrue(isPMCFound);
        assertTrue(isPMCPFound);
    }

    @Test
    public void verifyPMCPTabs() throws Exception {

        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        getExtendTest().log(Status.INFO, String.format("Creating container service %s", serviceReqDetails.getName()));
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CompositionPage.searchForElement(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationByPolicyElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        canvasManager.clickOnCanvaElement(portMirroringConfigurationByPolicyElement);

        PortMirroringVerificator.validateGeneralInfo();
        PortMirroringVerificator.validateReqsAndCapsTabExist();
    }


    @Test
    public void editPMCPName() throws Exception {

        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        getExtendTest().log(Status.INFO, String.format("Creating container service %s", serviceReqDetails.getName()));
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CompositionPage.searchForElement(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationByPolicyElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());

        canvasManager.clickOnCanvaElement(portMirroringConfigurationByPolicyElement);

        getExtendTest().log(Status.INFO, "Edit PMCP Name");
        canvasManager.updateElementNameInCanvas(portMirroringConfigurationByPolicyElement, PortMirroringEnum.PMCP_NEWNAME.getValue());

        PortMirroringVerificator.validateElementName(PortMirroringEnum.PMCP_NEWNAME.getValue());
    }


    @Test
    public void deletePMCP() throws Exception {

        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        getExtendTest().log(Status.INFO, String.format("Creating container service %s", serviceReqDetails.getName()));
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CompositionPage.searchForElement(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationByPolicyElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());

        canvasManager.clickOnCanvaElement(portMirroringConfigurationByPolicyElement);

        getExtendTest().log(Status.INFO, String.format("Delete element %s", portMirroringConfigurationByPolicyElement.getElementType()));
        canvasManager.deleteElementFromCanvas(portMirroringConfigurationByPolicyElement);

        PortMirroringVerificator.validateElementName(service.getName());
    }

    @Test
    public void createPortMirroringConfigurationMulipleInstances() throws Throwable {
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        ServiceReqDetails serviceReqDetails = portMirrioringConfigurationObject.getServiceReqDetails();
        CanvasElement serviceElementVmmeSourceName = portMirrioringConfigurationObject.getServiceElementVmmeSourceName();

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating 2 links between elements were created");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 2);

        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());

        getExtendTest().log(Status.INFO, "Adding second PMC to composition");
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();

        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME = new ConnectionWizardPopUpObject("", "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(), PortMirroringEnum.PMC_SOURCE_CAP.getValue());

        getExtendTest().log(Status.INFO, "Connect VMME to PMC again");
        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSourceName, portMirroringConfigurationElement,
                connectionWizardPopUpObjectVMME);

        getExtendTest().log(Status.INFO, "Connect VMME to PMC again");
        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSourceName, portMirroringConfigurationElement,
                connectionWizardPopUpObjectVMME);

        getExtendTest().log(Status.INFO, "Validating 4 links between elements exist");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 4);

        getExtendTest().log(Status.INFO, "Adding second PMCP to composition");
        CompositionPage.searchForElement(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationByPolicyElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME_PMCP = new ConnectionWizardPopUpObject("",
                "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(),
                PortMirroringEnum.PMCP_SOURCE_CAP.getValue());

        getExtendTest().log(Status.INFO, "Connect VMME to PMCP again");
        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSourceName, portMirroringConfigurationByPolicyElement,
                connectionWizardPopUpObjectVMME_PMCP);

        getExtendTest().log(Status.INFO, "Connect VMME to PMCP again");
        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSourceName, portMirroringConfigurationByPolicyElement,
                connectionWizardPopUpObjectVMME_PMCP);

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating 6 links between elements exist");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 6);
    }

    @Test
    public void downloadArtifactFromPMCService() throws Throwable {
        //Scenario of bug 362271
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        ServiceReqDetails serviceReqDetails = portMirrioringConfigurationObject.getServiceReqDetails();

        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.findComponentAndClick(serviceReqDetails.getName());

        ServiceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();

        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ToscaArtifactsScreenEnum.TOSCA_MODEL.getValue());
        File latestFilefromDir = org.onap.sdc.frontend.ci.tests.utilities.FileHandling.getLastModifiedFileNameFromDir();
        String actualToscaModelFilename = latestFilefromDir.getName();

        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ToscaArtifactsScreenEnum.TOSCA_TEMPLATE.getValue());
        latestFilefromDir = org.onap.sdc.frontend.ci.tests.utilities.FileHandling.getLastModifiedFileNameFromDir();
        String actualToscaTemplateFilename = latestFilefromDir.getName();

        String expectedToscaModelFilename = String.format("service-%s-csar.csar", serviceReqDetails.getName());
        String expectedToscaTemplateFilename = String.format("service-%s-template.yml", serviceReqDetails.getName());

        getExtendTest().log(Status.INFO, "Validating tosca artifact downloaded successfully");
        AssertJUnit.assertTrue(actualToscaModelFilename.equalsIgnoreCase(expectedToscaModelFilename));
        AssertJUnit.assertTrue(actualToscaTemplateFilename.equalsIgnoreCase(expectedToscaTemplateFilename));
    }

    @Test
    public void checkoutMirroringConfigurationServiceProxyAndDeletePMC() throws Throwable {
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        ServiceReqDetails serviceReqDetails = portMirrioringConfigurationObject.getServiceReqDetails();
        Service service = portMirrioringConfigurationObject.getService();
        CanvasElement portMirroringConfigurationElement = portMirrioringConfigurationObject.getPortMirroringConfigurationElement();

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating links between elements were created");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 2);

        getExtendTest().log(Status.INFO, String.format("Going to certify the Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        getExtendTest().log(Status.INFO, String.format("Checkout Port Mirroring Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();

        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();
        HomeUtils.findComponentAndClickByVersion(service.getName(), "1.1");

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        GeneralUIUtils.ultimateWait();
        canvasManager.clickOnCanvaElement(portMirroringConfigurationElement);
        getExtendTest().log(Status.INFO, String.format("Delete element %s", portMirroringConfigurationElement.getElementType()));
        canvasManager.deleteElementFromCanvas(portMirroringConfigurationElement);

        PortMirroringVerificator.validateElementName(service.getName());
        getExtendTest().log(Status.INFO, "Validating 0 links after delete the port mirroring element");
        serviceReqDetails.setVersion("1.1");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 0);

        getExtendTest().log(Status.INFO, String.format("Going to certify the Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        getExtendTest().log(Status.INFO, String.format("Service is certified"));
        Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
        getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
        AssertJUnit.assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
    }

    @Test
    public void updatePortMirroringServiceInstance() throws Throwable {

        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        Service service = portMirrioringConfigurationObject.getService();
        CanvasElement vmmeCanvasElement = portMirrioringConfigurationObject.getServiceElementVmmeSourceName();

        getExtendTest().log(Status.INFO, String.format("Going to certify the Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        Service serviceContainerVmme_Source = portMirrioringConfigurationObject.getServiceContainerVmme_Source();
        getExtendTest().log(Status.INFO, String.format("Checkout the vmme Service"));
        serviceContainerVmme_Source = (Service) AtomicOperationUtils.changeComponentState(serviceContainerVmme_Source, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Going to certify the vmme Service"));
        serviceContainerVmme_Source = (Service) AtomicOperationUtils.changeComponentState(serviceContainerVmme_Source, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        getExtendTest().log(Status.INFO, String.format("Checkout Port Mirroring Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();

        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();
        HomeUtils.findComponentAndClickByVersion(service.getName(), "1.1");

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        getExtendTest().log(Status.INFO, String.format("Change vmme source %s instance to version 2.0", serviceContainerVmme_Source.getName()));
        CompositionPage.changeComponentVersion(canvasManager, vmmeCanvasElement, "2.0", false);

        getExtendTest().log(Status.INFO, String.format("Going to certify the Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        getExtendTest().log(Status.INFO, String.format("Service is certified"));
        Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
        getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
        AssertJUnit.assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
    }

    @Test
    public void updateLinkPropertiesPortMirroringService() throws Throwable {

        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        getExtendTest().log(Status.INFO, String.format("Creating container service %s", serviceReqDetails.getName()));
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        String filePath = FileHandling.getPortMirroringRepositoryPath();
        ServiceContainer serviceContainerVmme_Source = PortMirroringUtils.createServiceFromHeatFile(filePath, "2016-227_vmme_vmme_30_1610_e2e.zip");

        String vmmeSourceName = serviceContainerVmme_Source.getService().getName();

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CompositionPage.searchForElement(vmmeSourceName);
        CanvasElement serviceElementVmmeSource = canvasManager.createElementOnCanvas(vmmeSourceName);

        CompositionPage.searchForElement(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationByPolicyElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMCP_ELEMENT_IN_PALLETE.getValue());

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME = new ConnectionWizardPopUpObject("",
                "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(),
                PortMirroringEnum.PMCP_SOURCE_CAP.getValue());

        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSource,
                portMirroringConfigurationByPolicyElement,
                connectionWizardPopUpObjectVMME);

        serviceReqDetails.setVersion("0.1");
        getExtendTest().log(Status.INFO, "Validating link between elements was created");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 1);

        canvasManager.openLinkPopupReqsCapsConnection(serviceElementVmmeSource, portMirroringConfigurationByPolicyElement);

        getExtendTest().log(Status.INFO, "Fill link properties with data");
        GeneralUIUtils.setTextInElementByXpath(PortMirroringEnum.NETWORK_ROLE_XPATH.getValue(), PortMirroringEnum.NETWORK_ROLE_VALUE.getValue());
        GeneralUIUtils.setTextInElementByXpath(PortMirroringEnum.NFC_TYPE_XPATH.getValue(), PortMirroringEnum.NFC_TYPE_VALUE.getValue());
        GeneralUIUtils.setTextInElementByXpath(PortMirroringEnum.PPS_CAPACITY_XPATH.getValue(), PortMirroringEnum.PPS_CAPACITY_VALUE.getValue());
        GeneralUIUtils.setTextInElementByXpath(PortMirroringEnum.NF_TYPE_XPATH.getValue(), PortMirroringEnum.NF_TYPE_VALUE.getValue());
        GeneralUIUtils.ultimateWait();

        canvasManager.clickSaveOnLinkPopup();
        Thread.sleep(THREAD_SLEEP_TIME); //Temp solution. Don't remove.
        canvasManager.openLinkPopupReqsCapsConnection(serviceElementVmmeSource, portMirroringConfigurationByPolicyElement);

        PortMirroringVerificator.validateLinkProperties();
    }

    @Test
    public void restorePortMirroringServiceLink() throws Throwable {

        //Scenario is taken from bug 361475 - Second Scenario
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        ServiceReqDetails serviceReqDetails = portMirrioringConfigurationObject.getServiceReqDetails();
        Service service = portMirrioringConfigurationObject.getService();
        CanvasElement vmmeCanvasElement = portMirrioringConfigurationObject.getServiceElementVmmeSourceName();
        Service serviceContainerVmme_Source = portMirrioringConfigurationObject.getServiceContainerVmme_Source();

        getExtendTest().log(Status.INFO, String.format("Checkout the vmme Service"));
        serviceContainerVmme_Source = (Service) AtomicOperationUtils.changeComponentState(serviceContainerVmme_Source, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Going to certify the vmme Service"));
        serviceContainerVmme_Source = (Service) AtomicOperationUtils.changeComponentState(serviceContainerVmme_Source, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();
        HomeUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        getExtendTest().log(Status.INFO, String.format("Changing vmme source %s instance to version 2.0", serviceContainerVmme_Source.getName()));
        CompositionPage.changeComponentVersion(canvasManager, vmmeCanvasElement, "2.0", false);

        getExtendTest().log(Status.INFO, "Validating 1 link exist after change version to the vmme service (Newer version)");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 2);

        getExtendTest().log(Status.INFO, String.format("Changing vmme source %s instance to version 1.0", serviceContainerVmme_Source.getName()));
        CompositionPage.changeComponentVersion(canvasManager, vmmeCanvasElement, "1.0", false);

        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 2);
    }

    @Test
    public void restoreServiceVersionOnContainerService() throws Throwable {
        //Scenario is taken from bug 361475 - First Scenario
        PortMirrioringConfigurationObject portMirrioringConfigurationObject = PortMirroringUtils.createPortMirriongConfigurationStructure(false);
        ServiceReqDetails serviceReqDetails = portMirrioringConfigurationObject.getServiceReqDetails();
        Service service = portMirrioringConfigurationObject.getService();
        CanvasElement vmmeCanvasElement = portMirrioringConfigurationObject.getServiceElementVmmeSourceName();
        Service serviceContainerVmme_Source = portMirrioringConfigurationObject.getServiceContainerVmme_Source();
        Service serviceContainerVprobe_Vprobe_Collector = portMirrioringConfigurationObject.getServiceContainerVprobe_Collector();
        CanvasElement vprobeCanvasElement = portMirrioringConfigurationObject.getServiceElementVprobeCollector();


        getExtendTest().log(Status.INFO, String.format("Checkout the vmme Service"));
        serviceContainerVmme_Source = (Service) AtomicOperationUtils.changeComponentState(serviceContainerVmme_Source, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Going to certify the vmme Service"));
        serviceContainerVmme_Source = (Service) AtomicOperationUtils.changeComponentState(serviceContainerVmme_Source, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
        GeneralUIUtils.ultimateWait();
        HomeUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        canvasManager.linkElements(vmmeCanvasElement, vprobeCanvasElement);

        getExtendTest().log(Status.INFO, String.format("Changing vmme source %s instance to version 2.0", serviceContainerVmme_Source.getName()));
        CompositionPage.changeComponentVersion(canvasManager, vmmeCanvasElement, "2.0", false);

        getExtendTest().log(Status.INFO, "Validating 1 link exist after change version to the vmme service (Newer version)");
        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 3);

        getExtendTest().log(Status.INFO, String.format("Changing vmme source %s instance to version 1.0", serviceContainerVmme_Source.getName()));
        CompositionPage.changeComponentVersion(canvasManager, vmmeCanvasElement, "1.0", false);

        ServiceVerificator.verifyLinkCreated(serviceReqDetails, getUser(), 3);
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }
}


