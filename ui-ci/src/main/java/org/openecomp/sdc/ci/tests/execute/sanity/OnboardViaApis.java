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

import static org.testng.AssertJUnit.assertEquals;

import java.awt.AWTException;
import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.clearspring.analytics.util.Pair;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import fj.data.Either;

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
		
	@Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "VNF_List")
	public void onboardVNFTestViaApis(String filepath, String vnfFile) throws Exception, Throwable {
		Service service = null;
		String fullFileName = FULL_PATH + vnfFile + ".csar";
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Starting test with VNF: " + vnfFile);
		service = runOnboardViaApisOnly(filepath, vnfFile);
		timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Finished test with VNF: " + vnfFile);
		timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Starting download service csar file: " + vnfFile);
		File file = new File(fullFileName);
		OnboardingUtillViaApis.downloadToscaCsarToDirectory(service, file);
		timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Finished download service csar file: " + vnfFile);
		System.out.println("end");
		
	}
	
	
	@Test
	public void onboardingAndParser() throws Exception {
		Service service = null;
		List<String> fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		String vnfFile = fileNamesFromFolder.get(7);
		System.err.println(timestamp + " Starting test with VNF: " + vnfFile);
		service = runOnboardViaApisOnly(filepath, vnfFile);
		
//		AtomicOperationUtils.getServiceObjectByNameAndVersion(sdncModifierDetails, serviceName, serviceVersion);
//        RestResponse distributeService = AtomicOperationUtils.distributeService(service, true);
//        Map<Long, ServiceDistributionStatus> convertServiceDistributionStatusToObject = ResponseParser.convertServiceDistributionStatusToObject(distributeService.getResponse());
//        convertServiceDistributionStatusToObject.
	}
	
	

	
	public Service runOnboardViaApisOnly(String filepath, String vnfFile) throws Exception, AWTException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Starting onboard VNF: " + vnfFile);
		Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct = OnboardingUtillViaApis.createVspViaApis(filepath, vnfFile, sdncDesignerDetails1);
		String vspName = createVendorSoftwareProduct.left;
		VendorSoftwareProductObject vendorSoftwareProductObject = createVendorSoftwareProduct.right;
		timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Finished onboard VNF: " + vnfFile);
		ResourceReqDetails resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(vendorSoftwareProductObject, vspName);
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails, vspName);
		
		AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		resource = AtomicOperationUtils.getResourceObject(resource.getUniqueId());
		// create service
		
		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		Either<ComponentInstance,RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		return service;
	}	
	
	
	



	
	public static Pair<String, Map<String, String>> createVendorSoftwareProduct(String HeatFileName, String filepath, User user, AmdocsLicenseMembers amdocsLicenseMembers)
			throws Exception {
		Pair<String, Map<String, String>> pair = createVSP(HeatFileName, filepath, user, amdocsLicenseMembers);
		
		String vspid = pair.right.get("vspId");
				
		prepareVspForUse(user, vspid);
		
		return pair;
	}
	
	public static void prepareVspForUse(User user, String vspid) throws Exception {
		RestResponse checkin = OnboardingUtils.checkinVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to checking new VSP", 200, checkin.getErrorCode().intValue());

		RestResponse submit = OnboardingUtils.submitVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to submit new VSP", 200, submit.getErrorCode().intValue());

		RestResponse createPackage = OnboardingUtils.createPackageOfVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to create package of new VSP ", 200, createPackage.getErrorCode().intValue());

	}
	public static AmdocsLicenseMembers createVendorLicense(User user) throws Exception {
		
		AmdocsLicenseMembers amdocsLicenseMembers;
		String vendorLicenseName = "ciLicense" + UUID.randomUUID().toString().split("-")[0];
		RestResponse vendorLicenseResponse = OnboardingUtils.createVendorLicenseModels_1(vendorLicenseName, user);
		assertEquals("did not succeed to create vendor license model", 200, vendorLicenseResponse.getErrorCode().intValue());
		String vendorId = ResponseParser.getValueFromJsonResponse(vendorLicenseResponse.getResponse(), "value");

		RestResponse vendorKeyGroupsResponse = OnboardingUtils.createVendorKeyGroups_2(vendorId, user);
		assertEquals("did not succeed to create vendor key groups", 200, vendorKeyGroupsResponse.getErrorCode().intValue());
		String keyGroupId = ResponseParser.getValueFromJsonResponse(vendorKeyGroupsResponse.getResponse(), "value");

		RestResponse vendorEntitlementPool = OnboardingUtils.createVendorEntitlementPool_3(vendorId, user);
		assertEquals("did not succeed to create vendor entitlement pool", 200, vendorEntitlementPool.getErrorCode().intValue());
		String entitlementPoolId = ResponseParser.getValueFromJsonResponse(vendorEntitlementPool.getResponse(), "value");

		RestResponse vendorLicenseFeatureGroups = OnboardingUtils.createVendorLicenseFeatureGroups_4(vendorId, keyGroupId, entitlementPoolId, user);
		assertEquals("did not succeed to create vendor license feature groups", 200, vendorLicenseFeatureGroups.getErrorCode().intValue());
		String featureGroupId = ResponseParser.getValueFromJsonResponse(vendorLicenseFeatureGroups.getResponse(), "value");

		RestResponse vendorLicenseAgreement = OnboardingUtils.createVendorLicenseAgreement_5(vendorId, featureGroupId, user);
		assertEquals("did not succeed to create vendor license agreement", 200, vendorLicenseAgreement.getErrorCode().intValue());
		String vendorLicenseAgreementId = ResponseParser.getValueFromJsonResponse(vendorLicenseAgreement.getResponse(), "value");

		RestResponse checkinVendorLicense = OnboardingUtils.checkinVendorLicense(vendorId, user);
		assertEquals("did not succeed to checkin vendor license", 200, checkinVendorLicense.getErrorCode().intValue());

		RestResponse submitVendorLicense = OnboardingUtils.submitVendorLicense(vendorId, user);
		assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

		amdocsLicenseMembers = new AmdocsLicenseMembers(vendorId, vendorLicenseName, vendorLicenseAgreementId, featureGroupId);
		
		return amdocsLicenseMembers;
	}
	
	
	public static Pair<String, Map<String, String>> createVSP(String HeatFileName, String filepath, User user, AmdocsLicenseMembers amdocsLicenseMembers) throws Exception {
		String vspName = OnboardingUtils.handleFilename(HeatFileName);
		
		Pair<RestResponse, Map<String, String>> createNewVspPair = OnboardingUtils.createNewVendorSoftwareProduct(vspName, amdocsLicenseMembers, user);
		RestResponse createNewVendorSoftwareProduct = createNewVspPair.left;
		assertEquals("did not succeed to create new VSP", 200,createNewVendorSoftwareProduct.getErrorCode().intValue());
		String vspid = ResponseParser.getValueFromJsonResponse(createNewVendorSoftwareProduct.getResponse(), "vspId");
		String componentId = ResponseParser.getValueFromJsonResponse(createNewVendorSoftwareProduct.getResponse(), "componentId");
		
		Map<String, String> vspMeta = createNewVspPair.right;
		Map<String, String> vspObject = new HashMap<String, String>();
		Iterator<String> iterator = vspMeta.keySet().iterator();
		while(iterator.hasNext()){
			Object key = iterator.next();
			Object value = vspMeta.get(key);
			vspObject.put(key.toString(), value.toString());
		}
		vspObject.put("vspId", vspid);
		vspObject.put("componentId", componentId);
		vspObject.put("vendorName", amdocsLicenseMembers.getVendorLicenseName());
		vspObject.put("attContact", user.getUserId());
		
		RestResponse uploadHeatPackage = OnboardingUtils.uploadHeatPackage(filepath, HeatFileName, vspid, user);
		assertEquals("did not succeed to upload HEAT package", 200, uploadHeatPackage.getErrorCode().intValue());
		
		RestResponse validateUpload = OnboardingUtils.validateUpload(vspid, user);
		assertEquals("did not succeed to validate upload process", 200, validateUpload.getErrorCode().intValue());
		
		Pair<String, Map<String, String>> pair = new Pair<String, Map<String, String>>(vspName, vspObject);
		
		return pair;
	}

	
	
	
	
	
	
	
	
//	----------------------------------------------------------------------------------------------------------------------------------------
	
	
	
	


}
