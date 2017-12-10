package org.openecomp.sdc.ci.tests.execute.sanity;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.CvfcTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

import fj.data.Either;

public class OnboardingFlows extends SetupCDTest{
	
	protected boolean skipReport = false;
	protected User sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

	
//	https://sdp.web.att.com/fa3qm1/web/console/Application_Development_Tools_QM_20.20.01#action=com.ibm.rqm.planning.home.actionDispatcher&subAction=viewTestPlan&id=6184
	
	@Test
	public void addVesFileToVsp() throws Exception{
		String vnfFile = "vMME_Ericsson_small_v2.zip";
		String vesArtifactFile = "VES.zip";
		String filePath = FileHandling.getFilePath("VFCArtifacts");
		String vesArtifactFileLocation = filePath + File.separator + vesArtifactFile;
		List<String> vesArtifacts = FileHandling.getFileNamesFromZip(vesArtifactFileLocation);
		List<String> tempVesArtifacts = FileHandling.getFileNamesFromZip(vesArtifactFileLocation);
		Map<CvfcTypeEnum, String> cvfcArtifacts = new HashMap<>();
		cvfcArtifacts.put(CvfcTypeEnum.VES_EVENTS, vesArtifactFileLocation);
		getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
		
		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUiUtils.createVendorLicense(getUser());
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails, amdocsLicenseMembers, cvfcArtifacts);
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		List<ComponentInstance> componentInstances = resource.getComponentInstances();
		for(ComponentInstance componentInstance : componentInstances){
			if(componentInstance.getDeploymentArtifacts() !=null && !componentInstance.getDeploymentArtifacts().isEmpty()){
				Map<String, ArtifactDefinition> deploymentArtifacts = componentInstance.getDeploymentArtifacts();
				for(Entry<String, ArtifactDefinition> entry : deploymentArtifacts.entrySet()){
					if(entry.getValue().getArtifactType().equals(CvfcTypeEnum.VES_EVENTS.getValue())){
						for(String vesArtifact : vesArtifacts){
							if(entry.getValue().getArtifactName().equals(vesArtifact)){
								tempVesArtifacts.remove(vesArtifact);
							}
						}
					}
				}
			}
		}
		assertTrue("Not all VES_EVENTS artifact files are on the resource instance", tempVesArtifacts.isEmpty());		
	}
	
//	741433: Update Old VSP
//	2.	Updated VSP "JSA AUG 2017" with the attached zip from v3 to v4. Follow normal steps to update the VF
//	3.     Update the VSP "vHSS-EPC-RDM3-Lab-0830" using the attached zip. Follow the normal steps to update the VF
//	@Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
//	public void create2(String filePath, String vnfFile) throws Exception{
//		setLog(vnfFile);
//	}
	
	
//	741509: E2E flow using old VLM
	@Test
	public void VlmReuse() throws Exception{
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
		List<String> newRandomFileNamesFromFolder = OnbordingDataProviders.getRandomElements(2, fileNamesFromFolder);
		String filePath = FileHandling.getVnfRepositoryPath();
		String vnfFile = newRandomFileNamesFromFolder.get(0);
		getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
//		setLog(vnfFile);
		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUiUtils.createVendorLicense(getUser());
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails, amdocsLicenseMembers, null);
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
	
		ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
		org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		addComponentInstanceToComponentContainer.left().value();
		service = (org.openecomp.sdc.be.model.Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		assertTrue("Distribution of service " + service.getName() + " failed", distributeAndValidateService);
		
//		update
		vnfFile = newRandomFileNamesFromFolder.get(1);
		getExtendTest().log(Status.INFO, "Going to update VNF with file " + vnfFile);
		OnboardingUtils.updateVendorLicense(amdocsLicenseMembers, sdncDesignerDetails, "1.0");
		vendorSoftwareProductObject = OnboardingUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails, amdocsLicenseMembers, null);
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
	
		serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
		service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		
		addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		addComponentInstanceToComponentContainer.left().value();
		service = (org.openecomp.sdc.be.model.Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		assertTrue("Distribution of service " + service.getName() + " failed", distributeAndValidateService);
		
		
	}
	
	
//	741607: E2E flow using old VSP
	@Test
	public void updateVfiVersionOnServiceLevel() throws Throwable{
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
		List<String> newRandomFileNamesFromFolder = OnbordingDataProviders.getRandomElements(2, fileNamesFromFolder);
		String filePath = FileHandling.getVnfRepositoryPath();
		String vnfFile = newRandomFileNamesFromFolder.get(0);
		getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUiUtils.createVendorLicense(getUser());
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails, amdocsLicenseMembers, null);
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
	
		ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails);
		org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
		service = (org.openecomp.sdc.be.model.Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		assertTrue("Distribution of service " + service.getName() + " failed", distributeAndValidateService);
		
//		update resource to v2.0
		String updateVnfFile = newRandomFileNamesFromFolder.get(1);
		getExtendTest().log(Status.INFO, "Going to update VNF with file " + vnfFile);
		OnboardingUiUtils.updateVendorSoftwareProduct(vendorSoftwareProductObject.getVspId(), updateVnfFile, filePath, sdncDesignerDetails, "2.0");
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resourceReqDetails.setUniqueId(resource.getUniqueId());
		resourceReqDetails.setVersion(resource.getVersion());
		resource = AtomicOperationUtils.updateResource(resourceReqDetails, sdncDesignerDetails, true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
	
		service = (org.openecomp.sdc.be.model.Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);		
		
		service = (org.openecomp.sdc.be.model.Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		assertTrue("Distribution of service " + service.getName() + " failed", distributeAndValidateService);
	}
	
	
//	741608: E2E flow using old Service
//	@Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
//	public void create5(String filePath, String vnfFile) throws Exception{
//		setLog(vnfFile);
//		// 1. Create Service with old resource -> Certify this Service - > Distribute
//		// 2. Service is distributed
//		// 3. Update old Service: fetch few new resources and few old resources -> Certify this Service - > Distribute
//		// 4. Service is distributed
//	}
	
//	741633: Update HEAT parameter value
	@Test()
	public void updateHeatParametersValue() throws Throwable{
		String msg = "VfArtifacts-->checkDefaultCreatedEnvArtifactsAfterVspUpdate tests with data provider index 4(last one) check it fully";
		getExtendTest().log(Status.INFO, msg);
	}

	@Test()
	public void UpdateVSPRevertToEarlierVersion() throws Throwable
	{
		// Test Case: 745821
//		1. Import VSP v1.0
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
		List<String> newRandomFileNamesFromFolder = OnbordingDataProviders.getRandomElements(1, fileNamesFromFolder);
		String filePath = FileHandling.getVnfRepositoryPath();
		String vnfFile = newRandomFileNamesFromFolder.get(0);
		getExtendTest().log(Status.INFO, "Going to upload VNF " + vnfFile);
		User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUiUtils.createVendorLicense(sdncDesignerDetails1);
		getExtendTest().log(Status.INFO, "Create Vendor License Model " + amdocsLicenseMembers.getVendorLicenseName());
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		getExtendTest().log(Status.INFO, "Create Vendor Software Product " + resourceReqDetails.getName());
		Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filePath, sdncDesignerDetails1, amdocsLicenseMembers);
		VendorSoftwareProductObject vendorSoftwareProductObject = OnboardViaApis.fillVendorSoftwareProductObjectWithMetaData(vnfFile, createVendorSoftwareProduct);
//		2. Create VF, certify - v1.0 is created
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		getExtendTest().log(Status.INFO, "Create VF " + resourceReqDetails.getName());
		Resource resource_v1 = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		getExtendTest().log(Status.INFO, "Certify VF " + resourceReqDetails.getName());
		resource_v1 = (Resource) AtomicOperationUtils.changeComponentState(resource_v1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
//		3. Update VSP to v2.0
		getExtendTest().log(Status.INFO, "Update VSP to version 2.0");
		OnboardingUtils.updateVendorSoftwareProductToNextVersion(vendorSoftwareProductObject, "2.0", sdncDesignerDetails1, filePath, vnfFile);
		OnboardingUtils.validateVspExist(vendorSoftwareProductObject.getVspId(),vendorSoftwareProductObject.getVersion(),sdncDesignerDetails1);
//		4. Update the VF with v2.0 of the VSP
		getExtendTest().log(Status.INFO, "Checkout VF v1.1");
		resource_v1 = (Resource) AtomicOperationUtils.changeComponentState(resource_v1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resourceReqDetails.setUniqueId(resource_v1.getUniqueId());
		resourceReqDetails.setVersion("1.1");
		resourceReqDetails.setCsarVersion("2.0");
		getExtendTest().log(Status.INFO, "Update VF to v2.0");
		resource_v1 = AtomicOperationUtils.updateResource(resourceReqDetails, sdncDesignerDetails, true).left().value();
		getExtendTest().log(Status.INFO, "Certify VF");
		Resource resource_v2 = (Resource) AtomicOperationUtils.changeComponentState(resource_v1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
//		5. Update VSP to v3.0 wih the zip from v1.0
		getExtendTest().log(Status.INFO, "Update VSP to version 3.0");
		OnboardingUtils.updateVendorSoftwareProductToNextVersion(vendorSoftwareProductObject, "3.0", sdncDesignerDetails1);
		OnboardingUtils.validateVspExist(vendorSoftwareProductObject.getVspId(),vendorSoftwareProductObject.getVersion(),sdncDesignerDetails1);
		getExtendTest().log(Status.INFO, "Checkout VF v2.1");
		resource_v1 = (Resource) AtomicOperationUtils.changeComponentState(resource_v1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resourceReqDetails.setUniqueId(resource_v1.getUniqueId());
		resourceReqDetails.setVersion("2.1");
		resourceReqDetails.setCsarVersion("3.0");
		getExtendTest().log(Status.INFO, "Update VF to v3.0");
		ResourceRestUtils.updateResource(resourceReqDetails,sdncDesignerDetails1,resource_v1.getUniqueId());
//		6. Update VF to v3.0
		getExtendTest().log(Status.INFO, "Certify VF");
		Resource resource_v3 = (Resource) AtomicOperationUtils.changeComponentState(resource_v1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
//		7. Compare versions v1.0 and v3.0 - should be the same
//      TODO: Shay add resource comparison.
//		8. Add each of the versions to service, certify - OK
		ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(sdncDesignerDetails1);
		getExtendTest().log(Status.INFO, "Create Service " + serviceReqDetails.getName() );
		org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		getExtendTest().log(Status.INFO, "Add vf's v1 & v2 to service");
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource_v1, service, UserRoleEnum.DESIGNER, true);
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer1 = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource_v3, service, UserRoleEnum.DESIGNER, true);
		getExtendTest().log(Status.INFO, "Certify Service");
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		System.out.println("");
	}

	@Test(dataProviderClass = org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders.class, dataProvider = "updateList")
	public void distributeServiceAndUpgradeWithNewHeatFile(String vnfFile1, String vnfFile2) throws Throwable
	{
		setLog(String.format("Update VSP Test: Create VF from %s add it to service, distribute than upgrade the VF with file %s and update the service and distribute", vnfFile1, vnfFile2));
//		1. Import VSP v1.0
		String filePath = org.openecomp.sdc.ci.tests.utilities.FileHandling.getUpdateVSPVnfRepositoryPath();
		User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUtils.createVendorLicense(sdncDesignerDetails1);
		getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", amdocsLicenseMembers.getVendorLicenseName()));
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
		getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile1));
		Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile1, filePath, sdncDesignerDetails1, amdocsLicenseMembers);
		VendorSoftwareProductObject vendorSoftwareProductObject = OnboardViaApis.fillVendorSoftwareProductObjectWithMetaData(vnfFile1, createVendorSoftwareProduct);
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
//		5. Distribute the Service v1.0
		Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
		assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
//		6. Update VSP to v2.0
		getExtendTest().log(Status.INFO, "Upgrading the VSP with new file: " + vnfFile2);
		OnboardingUtils.updateVendorSoftwareProductToNextVersion(vendorSoftwareProductObject, "2.0", sdncDesignerDetails1, filePath, vnfFile2);
		getExtendTest().log(Status.INFO, String.format("Validating VSP %s upgrade to version 2.0: " ,vnfFile2));
		OnboardingUtils.validateVspExist(vendorSoftwareProductObject.getVspId(),vendorSoftwareProductObject.getVersion(),sdncDesignerDetails1);
//		7. Update the VF with v2.0 of the VSP and certify the VF
		getExtendTest().log(Status.INFO, String.format("Checkout the VF %s v1.1 " ,resourceReqDetails.getName()));
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resourceReqDetails.setUniqueId(resource.getUniqueId());
		resourceReqDetails.setVersion("1.1");
		resourceReqDetails.setCsarVersion("2.0");
		getExtendTest().log(Status.INFO, String.format("Upgrade the VF %s v1.1 with the new VSP %s v2.0 " ,resourceReqDetails.getName(),vendorSoftwareProductObject.getName()));
		resource = AtomicOperationUtils.updateResource(resourceReqDetails, sdncDesignerDetails, true).left().value();
		getExtendTest().log(Status.INFO, String.format("Certify the VF to v2.0"));
		Resource resource_v2 = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
//		8. Update the Service with the VFi version 2.0
		getExtendTest().log(Status.INFO, String.format("Checkout the Service v1.1"));
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		getExtendTest().log(Status.INFO, String.format("Change the instance of the VF in the service to VFi v2.0"));
		AtomicOperationUtils.changeComponentInstanceVersion(service, componentInstance, resource, UserRoleEnum.DESIGNER, true);
		getExtendTest().log(Status.INFO, String.format("Certify the Service to v2.0"));
		service = (org.openecomp.sdc.be.model.Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
//		9. Distribute the service v2.0
		distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
		assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
	}

	@Test(dataProviderClass = org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders.class, dataProvider = "VNF_List")
	public void distributeServiceFromHeatFile(String filePath, String vnfFile) throws Throwable
	{
//		String vnfFile1 = "1-2016-20-visbc3vf-(VOIP)_v2.1.zip";
//		String vnfFile2 = "2-2016-20-visbc3vf-(VOIP)_v2.0.zip";
		setLog(String.format("Distribute Service Test: Create VF from %s add it to service than distribute", vnfFile));
//		1. Import VSP v1.0
		//String filePath = FileHandling.getVnfRepositoryPath();
		User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
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
//		5. Distribute the Service v1.0
		Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
		assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
	}
	  
	  @Test()
	  public void onboardE2EviaAPI() throws Throwable
		{
//			1. Import VSP v1.0
			String filePath = FileHandling.getVnfRepositoryPath();
			String vnfFile1 = "HeatCandidate_2017-09-20_13-37_70Name_2017-491-4vshaken-HTTP-CM-vf-v1.0-(VOIP)_10202017.zip";
			User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
			AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUtils.createVendorLicense(sdncDesignerDetails1);
			getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", amdocsLicenseMembers.getVendorLicenseName()));
			ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
			getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile1));
			Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile1, filePath, sdncDesignerDetails1, amdocsLicenseMembers);
			VendorSoftwareProductObject vendorSoftwareProductObject = OnboardViaApis.fillVendorSoftwareProductObjectWithMetaData(vnfFile1, createVendorSoftwareProduct);
//			2. Create VF, certify - v1.0 is created
			resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
			Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
			resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
			getExtendTest().log(Status.INFO, String.format("Creating Virtual Function (VF): %s v1.0", resourceReqDetails.getName()));
			getExtendTest().log(Status.INFO, String.format("Certify the VF"));
//			3. Create Service add to it the certified VF and certify the Service v1.0
			ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
			Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
			getExtendTest().log(Status.INFO, String.format("Creating Service: %s v1.0", serviceReqDetails.getName()));
			Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
			ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
			getExtendTest().log(Status.INFO, String.format("Adding VF instance to Service"));
			service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
			getExtendTest().log(Status.INFO, String.format("Certify the Service"));
//			5. Distribute the Service v1.0
			Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
			getExtendTest().log(Status.INFO, String.format("Distribute and validate the Service"));
			assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
			}
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}
	
	
}
