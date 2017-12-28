package org.openecomp.sdc.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;
import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.ci.tests.datatypes.*;
import org.openecomp.sdc.ci.tests.datatypes.enums.CircleSize;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.utils.general.*;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.rest.PropertyRestUtils;

import java.util.List;

public class PortMirroringUtils {


    public static ServiceContainer createServiceFromHeatFile(String filePath, String vnfFile) throws Throwable {
//		1. Import VSP v1.0
        User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        AmdocsLicenseMembers amdocsLicenseMembers = VendorLicenseModelRestUtils.createVendorLicense(sdncDesignerDetails1);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", amdocsLicenseMembers.getVendorLicenseName()));
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile));
        Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct = VendorSoftwareProductRestUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails1, amdocsLicenseMembers);
//        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardViaApis.fillVendorSoftwareProductObjectWithMetaData(vnfFile, createVendorSoftwareProduct);
        VendorSoftwareProductObject vendorSoftwareProductObject = createVendorSoftwareProduct.right;
//		2. Create VF, certify - v1.0 is created
        resourceReqDetails = org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Virtual Function (VF): %s v1.0", resourceReqDetails.getName()));
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Certify the VF"));
//		3. Create Service add to it the certified VF and certify the Service v1.0
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating Service: %s v1.0", serviceReqDetails.getName()));
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Adding VF instance to Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Certify the Service"));

        return new ServiceContainer(service, resource, vendorSoftwareProductObject, amdocsLicenseMembers);
    }

    public static Resource GeneratePNFAndUpdateInput(String resourceName, String vendorModelNumber, User user) throws Exception {
        Resource resource = getResourceByType(ResourceTypeEnum.PNF, resourceName, vendorModelNumber);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Creating pnf %s and certify it", resource.getName()));
        Component componentObject = AtomicOperationUtils.getComponentObject(resource, UserRoleEnum.DESIGNER);
        UpdateResourceInputViaAPI(user, componentObject, "physicalProbe", "nf_role");
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        return resource;
    }

    private static void UpdateResourceInputViaAPI(User user, Component componentObject, String defaultValue, String inputName) throws Exception {
        List<InputDefinition> componentInputs = componentObject.getInputs();
        PropertyObject propertyObject = new PropertyObject(defaultValue, inputName, componentInputs.get(1).getParentUniqueId(), componentInputs.get(1).getUniqueId());
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Update input %s to %s", "nf_role", "physicalProbe"));
        PropertyRestUtils.updateInput(componentObject, propertyObject, user);
    }

    public static Resource getResourceByType(ResourceTypeEnum resourceTypeEnum, String resourceName, String vendorModelNumber) {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResourceByType(resourceTypeEnum, resourceName, ResourceCategoryEnum.NETWORK_L2_3_INFRASTRUCTURE, resourceName, vendorModelNumber);
        return AtomicOperationUtils.createResourceByResourceDetails(resourceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
    }

    public static String createProxyInstanceServiceName(String serviceName, String instanceId) {
        String serviceProxyInstanceName = String.format("%s_proxy %s", serviceName, instanceId);
        return serviceProxyInstanceName;
    }

    public static PortMirrioringConfigurationObject createPortMirriongConfigurationStructure() throws Throwable {

        //Using API onboard and certify 2 zip files Source: vmmme and Collector: Vprobe
        String filePath = FileHandling.getPortMirroringRepositoryPath();
        ServiceContainer serviceContainerVmme_Source = PortMirroringUtils.createServiceFromHeatFile(filePath, PortMirroringEnum.VMME_ZIP.getValue());
        ServiceContainer serviceContainerVprobe_Collector = PortMirroringUtils.createServiceFromHeatFile(filePath, PortMirroringEnum.VPROBE_ZIP.getValue());

//        String vmmeSourceName = "ciServiceb560327d162f";
//        String vprobeSourceName = "ciService3d9933d31791";

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

        CompositionPage.searchForElement(vmmeSourceName);
        CanvasElement serviceElementVmmeSourceName = canvasManager.createElementOnCanvas(vmmeSourceName);

        CompositionPage.searchForElement(vprobeSourceName);
        CanvasElement serviceElementVprobeCollector = canvasManager.createElementOnCanvas(vprobeSourceName);

        CompositionPage.searchForElement(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());
        CanvasElement portMirroringConfigurationElement = canvasManager.createElementOnCanvas(PortMirroringEnum.PMC_ELEMENT_IN_PALLETE.getValue());

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME = new ConnectionWizardPopUpObject("", "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(), PortMirroringEnum.PMC_SOURCE_CAP.getValue());
        ConnectionWizardPopUpObject connectionWizardPopUpObjectVProbe = new ConnectionWizardPopUpObject("", "",
                PortMirroringEnum.PM_REQ_TYPE.getValue(), PortMirroringEnum.PMC_COLLECTOR_CAP.getValue());

        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSourceName, CircleSize.SERVICE, portMirroringConfigurationElement, CircleSize.NORMATIVE, connectionWizardPopUpObjectVMME);
        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVprobeCollector, CircleSize.SERVICE, portMirroringConfigurationElement, CircleSize.NORMATIVE, connectionWizardPopUpObjectVProbe);

        PortMirrioringConfigurationObject portMirrioringConfigurationObject = new PortMirrioringConfigurationObject(serviceReqDetails, vmmeSourceName,
                vprobeSourceName, canvasManager, serviceElementVmmeSourceName, serviceElementVprobeCollector, service,
                portMirroringConfigurationElement, serviceContainerVmme_Source.getService(), serviceContainerVprobe_Collector.getService());

        return portMirrioringConfigurationObject;
    }

}
