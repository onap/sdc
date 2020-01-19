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

package org.openecomp.sdc.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.ConnectionWizardPopUpObject;
import org.openecomp.sdc.ci.tests.datatypes.PortMirrioringConfigurationObject;
import org.openecomp.sdc.ci.tests.datatypes.PortMirroringEnum;
import org.openecomp.sdc.ci.tests.datatypes.PropertyObject;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceContainer;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.PropertyRestUtils;

import java.util.List;
import java.util.Map;

public class PortMirroringUtils {


    private static final int WAITING_FOR_LOADER_TIME_OUT = 2000;

    private PortMirroringUtils() {

    }

    public static ServiceContainer createServiceFromHeatFile(String filePath, String vnfFile) throws Throwable {
//1. Import VSP v1.0
        User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(sdncDesignerDetails1);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", vendorLicenseModel
            .getVendorLicenseName()));
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile));
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails1,
            vendorLicenseModel);
//        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardViaApis.fillVendorSoftwareProductObjectWithMetaData(vnfFile, createVendorSoftwareProduct);
//2. Create VF, certify - v1.0 is created
        resourceReqDetails = org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Virtual Function (VF): %s v1.0", resourceReqDetails.getName()));
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Certify the VF"));
//3. Create Service add to it the certified VF and certify the Service v1.0
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Service: %s v1.0", serviceReqDetails.getName()));
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Adding VF instance to Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Certify the Service"));

        return new ServiceContainer(service, resource, vendorSoftwareProductObject, vendorLicenseModel);
    }

    public static Resource generatePNFAndUpdateInput(String resourceName, String vendorModelNumber, User user) throws Exception {
        Resource resource = getresourcebytype(ResourceTypeEnum.PNF, resourceName, vendorModelNumber);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating pnf %s and certify it", resource.getName()));
        Component componentObject = AtomicOperationUtils.getComponentObject(resource, UserRoleEnum.DESIGNER);
        updateResourceInputViaAPI(user, componentObject, "physicalProbe", "nf_role");
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        return resource;
    }

    private static void updateResourceInputViaAPI(User user, Component componentObject, String defaultValue, String inputName) throws Exception {
        List<InputDefinition> componentInputs = componentObject.getInputs();
        PropertyObject propertyObject = new PropertyObject(defaultValue, inputName, componentInputs.get(1).getParentUniqueId(), componentInputs.get(1).getUniqueId());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Update input %s to %s", "nf_role", "physicalProbe"));
        PropertyRestUtils.updateInput(componentObject, propertyObject, user);
    }

    public static Resource getresourcebytype(ResourceTypeEnum resourceTypeEnum, String resourceName, String vendorModelNumber) {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResourceByType(resourceTypeEnum, resourceName, ResourceCategoryEnum.NETWORK_L2_3_INFRASTRUCTURE, resourceName, vendorModelNumber);
        return AtomicOperationUtils.createResourceByResourceDetails(resourceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
    }

    public static String createproxyinstanceservicename(String serviceName, String instanceId) {
        String serviceProxyInstanceName = String.format("%s_proxy %s", serviceName, instanceId);
        return serviceProxyInstanceName;
    }

    public static PortMirrioringConfigurationObject createPortMirriongConfigurationStructure(boolean isCapPropAssign) throws Throwable {

        //Using API onboard and certify 2 zip files Source: vmmme and Collector: Vprobe
        String filePath = FileHandling.getPortMirroringRepositoryPath();
        ServiceContainer serviceContainerVmme_Source = PortMirroringUtils.createServiceFromHeatFile(filePath, PortMirroringEnum.VMME_ZIP.getValue());
        ServiceContainer serviceContainerVprobe_Collector = PortMirroringUtils.createServiceFromHeatFile(filePath, PortMirroringEnum.VPROBE_ZIP.getValue());

        // create service
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        //ServiceUIUtils.createService(serviceMetadata, getUser());

        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating container %s: ", serviceReqDetails.getName()));
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        String vmmeSourceName = serviceContainerVmme_Source.getService().getName();
        String vprobeSourceName = serviceContainerVprobe_Collector.getService().getName();

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(service.getName());

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CanvasElement serviceElementVmmeSourceName = canvasManager.createElementOnCanvas(vmmeSourceName);

        CanvasElement serviceElementVprobeCollector = canvasManager.createElementOnCanvas(vprobeSourceName);

        CompositionPage.searchForElement(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME = new ConnectionWizardPopUpObject("", "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(), PortMirroringEnum.PMC_SOURCE_CAP.getValue());
        ConnectionWizardPopUpObject connectionWizardPopUpObjectVProbe = new ConnectionWizardPopUpObject("", "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(), PortMirroringEnum.PMC_COLLECTOR_CAP.getValue());
        Map<String, String> capPropValues1 = null;

        if (isCapPropAssign) {
            capPropValues1 = canvasManager.linkElementsWithCapPropAssignment(serviceElementVmmeSourceName, portMirroringConfigurationElement, connectionWizardPopUpObjectVMME);
            GeneralUIUtils.waitForLoader(WAITING_FOR_LOADER_TIME_OUT);
            canvasManager.linkElementsWithCapPropAssignment(serviceElementVprobeCollector, portMirroringConfigurationElement, connectionWizardPopUpObjectVProbe);
        } else {
            canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSourceName, portMirroringConfigurationElement, connectionWizardPopUpObjectVMME);
            canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVprobeCollector, portMirroringConfigurationElement, connectionWizardPopUpObjectVProbe);
        }


        PortMirrioringConfigurationObject portMirrioringConfigurationObject = new PortMirrioringConfigurationObject(serviceReqDetails, vmmeSourceName,
                vprobeSourceName, canvasManager, serviceElementVmmeSourceName, serviceElementVprobeCollector, service,
                portMirroringConfigurationElement, serviceContainerVmme_Source.getService(), serviceContainerVprobe_Collector.getService());

        if (capPropValues1 != null) {
            portMirrioringConfigurationObject.setCapPropValues(capPropValues1);
        }

        return portMirrioringConfigurationObject;
    }

}
