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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.clearspring.analytics.util.Pair;
import fj.data.Either;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertTrue;


public class OnboardViaApis{
	

	private static final String FULL_PATH = "C://tmp//CSARs//";
	protected static String filepath = FileHandling.getVnfRepositoryPath();
	
//-------------------------------------------------------------------------------------------------------
	User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
//	ResourceReqDetails resourceDetails;
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        
	@BeforeMethod
	public void before(){
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.getLogger("org.apache").setLevel(Level.OFF);
		lc.getLogger("org.*").setLevel(Level.OFF);
		lc.getLogger("org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest").setLevel(Level.OFF);
//		resourceDetails = ElementFactory.getDefaultResource();
	}
		
	@Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
	public void onboardVNFTestViaApis(String filepath, String vnfFile) throws Exception, Throwable {
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Service service = runOnboardViaApisOnly(serviceReqDetails, resourceReqDetails, filepath, vnfFile);
	}
	
	
	@Test
	public void onboardingAndParser() throws Exception {
		Service service = null;
		List<String> fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		String vnfFile = fileNamesFromFolder.get(7);
		System.err.println(timestamp + " Starting test with VNF: " + vnfFile);
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		service = runOnboardViaApisOnly(serviceReqDetails, resourceReqDetails, filepath, vnfFile);
	}
	
	@Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
	public void updateVSPFullScenario(String filepath, String vnfFile) throws Exception
	{
		//CREATE DATA REQUIRED FOR TEST
		boolean skipReport = true;
		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUtils.createVendorLicense(sdncDesignerDetails1);
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, sdncDesignerDetails1, amdocsLicenseMembers);
		VendorSoftwareProductObject vendorSoftwareProductObject = fillVendorSoftwareProductObjectWithMetaData(vnfFile, createVendorSoftwareProduct);
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
//		serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(serviceReqDetails, sdncDesignerDetails1);
		Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		ComponentInstance componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		// TEST START

		// Update exist VLM Version (From 1.0 to 2.0)
		OnboardingUtils.updateVendorLicense(amdocsLicenseMembers, sdncDesignerDetails1, "1.0");
		// Set VLM version to 2.0 in VLM Meta data object
		amdocsLicenseMembers.setLicenseVersionId("2.0");
		amdocsLicenseMembers.setLicenseVersionLabel("2.0");
		OnboardingUtils.validateVlmExist(amdocsLicenseMembers.getVendorId(), amdocsLicenseMembers.getLicenseVersionId(), sdncDesignerDetails1);

		// Update the VSP With the VLM new version and submit the VSP
		vendorSoftwareProductObject = OnboardingUtils.updateVSPWithNewVLMParameters(vendorSoftwareProductObject, amdocsLicenseMembers, sdncDesignerDetails1, "1.1", "2.0");
		OnboardingUtils.validateVspExist(vendorSoftwareProductObject.getVspId(),vendorSoftwareProductObject.getVersion(), sdncDesignerDetails1);
		Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
		assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
		System.out.println(distributeAndValidateService);
	}

	public static VendorSoftwareProductObject fillVendorSoftwareProductObjectWithMetaData(String vnfFile, Pair<String, Map<String, String>> createVendorSoftwareProduct) {
		VendorSoftwareProductObject vendorSoftwareProductObject = new VendorSoftwareProductObject();
		Map<String, String> map = createVendorSoftwareProduct.right;
		vendorSoftwareProductObject.setAttContact(map.get("attContact"));
		vendorSoftwareProductObject.setCategory(map.get("category"));
		vendorSoftwareProductObject.setComponentId(map.get("componentId"));
		vendorSoftwareProductObject.setDescription(map.get("description"));
		vendorSoftwareProductObject.setSubCategory(map.get("subCategory"));
		vendorSoftwareProductObject.setVendorName(map.get("vendorName"));
		vendorSoftwareProductObject.setVspId(map.get("vspId"));
		vendorSoftwareProductObject.setName(createVendorSoftwareProduct.left);
		String[] arrFileNameAndExtension = vnfFile.split("\\.");
		vendorSoftwareProductObject.setOnboardingMethod("NetworkPackage");
		vendorSoftwareProductObject.setNetworkPackageName(arrFileNameAndExtension[0]);
		vendorSoftwareProductObject.setOnboardingOrigin(arrFileNameAndExtension[1]);

		return vendorSoftwareProductObject;
	}

	public Service runOnboardViaApisOnly(ServiceReqDetails serviceReqDetails, ResourceReqDetails resourceReqDetails, String filepath, String vnfFile) throws Exception, AWTException {

		Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct = OnboardingUtillViaApis.createVspViaApis(resourceReqDetails, filepath, vnfFile, sdncDesignerDetails1);
		VendorSoftwareProductObject vendorSoftwareProductObject = createVendorSoftwareProduct.right;
		vendorSoftwareProductObject.setName(createVendorSoftwareProduct.left);
		
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
//		serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(serviceReqDetails, sdncDesignerDetails1);
		Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		ComponentInstance componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();

		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		return service;
	}

}
