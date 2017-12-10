package org.openecomp.sdc.ci.tests.execute.sanity;

import java.util.Map;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.ConnectionWizardPopUpObject;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceContainer;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.CircleSize;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.verificator.ServiceVerificator;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

import fj.data.Either;

public class PortMirroring extends SetupCDTest
{
    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER3;
    }

    String portMirroringElementNameInPallete = "Port Mirroring Configuration";
    String portMirroringCapReqType = "org.openecomp.capabilities.PortMirroring";
    String portMirroringSourceCapability = "Port Mirroring Configuration 0: source: [1, UNBOUNDED]";
    String portMirroringCollectorCapability = "Port Mirroring Configuration 0: collector: [1, 1]";

    @Test
    public void createPortMirroringServiceProxy() throws Throwable {
        //Using API onboard and certify 2 zip files Source: vmmme and Collector: Vprobe
        String filePath = FileHandling.getPortMirroringRepositoryPath();
        ServiceContainer serviceContainerVmme_Source = createServiceFromHeatFile(filePath,"2016-227_vmme_vmme_30_1610_e2e.zip");
        ServiceContainer serviceContainerVprobe_Collector = createServiceFromHeatFile(filePath,"vProbe_2017-10-22_07-24.zip");

//        String vmmeSourceName = "ciServiceb560327d162f";
//        String vprobeSourceName = "ciService3d9933d31791";

        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata, getUser());

        String vmmeSourceName = serviceContainerVmme_Source.getService().getName();
        String vprobeSourceName = serviceContainerVprobe_Collector.getService().getName();

        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();

        CompositionPage.searchForElement(vmmeSourceName);
        CanvasElement serviceElementVmmeSourceName = canvasManager.createElementOnCanvas(vmmeSourceName);

        CompositionPage.searchForElement(vprobeSourceName);
        CanvasElement serviceElementVprobeCollector = canvasManager.createElementOnCanvas(vprobeSourceName);

        CompositionPage.searchForElement(portMirroringElementNameInPallete);
        CanvasElement portMirroringConfigurationElement = canvasManager.createElementOnCanvas(portMirroringElementNameInPallete);

        ConnectionWizardPopUpObject connectionWizardPopUpObjectVMME = new ConnectionWizardPopUpObject("","", portMirroringCapReqType, portMirroringSourceCapability);
        ConnectionWizardPopUpObject connectionWizardPopUpObjectVProbe = new ConnectionWizardPopUpObject("","", portMirroringCapReqType, portMirroringCollectorCapability);

        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVmmeSourceName, CircleSize.SERVICE,portMirroringConfigurationElement, CircleSize.NORMATIVE, connectionWizardPopUpObjectVMME);
        canvasManager.linkElementsAndSelectCapReqTypeAndCapReqName(serviceElementVprobeCollector, CircleSize.SERVICE, portMirroringConfigurationElement, CircleSize.NORMATIVE, connectionWizardPopUpObjectVProbe);

        serviceMetadata.setVersion("0.1");
        ServiceVerificator.verifyLinkCreated(serviceMetadata, getUser(), 2);

        System.out.println("End");
    }

    public ServiceContainer createServiceFromHeatFile(String filePath, String vnfFile) throws Throwable
    {
        setLog(String.format("Distribute Service Test: Create VF from %s add it to service than distribute", vnfFile));
//		1. Import VSP v1.0
        User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER3);
        AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUtils.createVendorLicense(sdncDesignerDetails1);
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", amdocsLicenseMembers.getVendorLicenseName()));
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile));
        Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails1, amdocsLicenseMembers);
        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardViaApis.fillVendorSoftwareProductObjectWithMetaData(vnfFile, createVendorSoftwareProduct);
//		2. Create VF, certify - v1.0 is created
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Creating Virtual Function (VF): %s v1.0", resourceReqDetails.getName()));
        getExtendTest().log(Status.INFO, String.format("Certify the VF"));
//		3. Create Service add to it the certified VF and certify the Service v1.0
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        getExtendTest().log(Status.INFO, String.format("Creating Service: %s v1.0", serviceReqDetails.getName()));
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
        getExtendTest().log(Status.INFO, String.format("Adding VF instance to Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Certify the Service"));

        return new ServiceContainer(service,resource,vendorSoftwareProductObject,amdocsLicenseMembers);
    }


}


