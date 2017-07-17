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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bouncycastle.util.encoders.Base64;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDistributionStatus;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.toscaparser.api.common.JToscaException;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.clearspring.analytics.util.Pair;
import com.google.gson.Gson;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import fj.data.Either;

public class OnboardViaApis{
	

	private static final String FULL_PATH = "C://tmp//CSARs//";
	
	public static Object[][] provideData(Object[] fileNamesFromFolder, String filepath) {
		Object[][] arObject = new Object[fileNamesFromFolder.length][];

		int index = 0;
		for (Object obj : fileNamesFromFolder) {
			arObject[index++] = new Object[] { filepath, obj };
		}
		return arObject;
	}

	@DataProvider(name = "VNF_List" , parallel = false)
	private static final Object[][] VnfList() throws Exception {
		String filepath = FileHandling.getVnfRepositoryPath();
		
		Object[] fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		System.out.println(String.format("There are %s zip file(s) to test", fileNamesFromFolder.length));
		return provideData(fileNamesFromFolder, filepath);
	}

	
//-------------------------------------------------------------------------------------------------------
	User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		private static String vendorId;
		private static String vendorLicenseName;
		private static String vendorLicenseAgreementId;
		private static String featureGroupId;
		ResourceReqDetails resourceDetails;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        
		@BeforeMethod
		public void before(){
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			lc.getLogger("org.apache").setLevel(Level.OFF);
			lc.getLogger("org.*").setLevel(Level.OFF);
			lc.getLogger("org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest").setLevel(Level.OFF);
			resourceDetails = ElementFactory.getDefaultResource();
			
		}
		
	@Test(dataProvider = "VNF_List")
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
		downloadToscaCsarToDirectory(service, file);
		timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Finished download service csar file: " + vnfFile);
		System.out.println("end");
		
	}
	
	
	@Test
	public void onboardingAndParser() throws Exception {
		Service service = null;
		String filepath = getFilePath();
		Object[] fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		String vnfFile = fileNamesFromFolder[7].toString();
		System.err.println(timestamp + " Starting test with VNF: " + vnfFile);
		service = runOnboardViaApisOnly(filepath, vnfFile);
		
//		AtomicOperationUtils.getServiceObjectByNameAndVersion(sdncModifierDetails, serviceName, serviceVersion);
//        RestResponse distributeService = AtomicOperationUtils.distributeService(service, true);
//        Map<Long, ServiceDistributionStatus> convertServiceDistributionStatusToObject = ResponseParser.convertServiceDistributionStatusToObject(distributeService.getResponse());
//        convertServiceDistributionStatusToObject.
	}
	
	public static String getFilePath() {
		String filepath = System.getProperty("filepath");
		if (filepath == null && System.getProperty("os.name").contains("Windows")) {
			filepath = FileHandling.getResourcesFilesPath() +"VNFs";
		}
		
		else if(filepath.isEmpty() && !System.getProperty("os.name").contains("Windows")){
				filepath = FileHandling.getBasePath() + File.separator + "Files" + File.separator +"VNFs";
		}
		return filepath;
	}
	
	public static void downloadToscaCsarToDirectory(Service service, File file) {
		try {
			Either<String,RestResponse> serviceToscaArtifactPayload = AtomicOperationUtils.getServiceToscaArtifactPayload(service, "assettoscacsar");
			if(serviceToscaArtifactPayload.left().value() != null){
				Gson gson = new Gson();
				@SuppressWarnings("unchecked")
				Map<String, String> fromJson = gson.fromJson(serviceToscaArtifactPayload.left().value(), Map.class);
				String string = fromJson.get("base64Contents").toString();
				byte[] byteArray = Base64.decode(string.getBytes(StandardCharsets.UTF_8));
				File downloadedFile = new File(file.getAbsolutePath());
				FileOutputStream fos = new FileOutputStream(downloadedFile);
				fos.write(byteArray);
				fos.flush();
				fos.close();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Service runOnboardViaApisOnly(String filepath, String vnfFile) throws Exception, AWTException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Starting onboard VNF: " + vnfFile);
		Pair<String,Map<String,String>> onboardAndValidate = onboardAndValidateViaApi(filepath, vnfFile, sdncDesignerDetails1);
		String vspName = onboardAndValidate.left;
		timestamp = new Timestamp(System.currentTimeMillis());
		System.err.println(timestamp + " Finished onboard VNF: " + vnfFile);
		Resource resource = AtomicOperationUtils.getResourceObject(resourceDetails.getUniqueId());
		
		AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		resource = AtomicOperationUtils.getResourceObject(resource.getUniqueId());
		// create service
		
		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		Either<ComponentInstance,RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		return service;
	}	
	
	
	
	public Pair<String, Map<String, String>> onboardAndValidateViaApi(String filepath, String vnfFile, User user) throws Exception {
	
		createVendorLicense(user);
		Pair<String, Map<String, String>> createVendorSoftwareProduct = createVendorSoftwareProduct(vnfFile, filepath, user);
		String vspName = createVendorSoftwareProduct.left;
		List<String> tags = new ArrayList<>();
		tags.add(vspName);
		Map<String, String> map = createVendorSoftwareProduct.right;
		
		resourceDetails.setCsarUUID(map.get("vspId"));
		resourceDetails.setCsarVersion("1.0");
		resourceDetails.setName(vspName);
		resourceDetails.setTags(tags);
		resourceDetails.setResourceType(map.get("componentType"));
		resourceDetails.setVendorName(map.get("vendorName"));
		resourceDetails.setVendorRelease("1.0");
		resourceDetails.setResourceType("VF");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncDesignerDetails1);
		
		return createVendorSoftwareProduct;
	}
	
	public static Pair<String, Map<String, String>> createVendorSoftwareProduct(String HeatFileName, String filepath, User user)
			throws Exception {
		Pair<String, Map<String, String>> pair = createVSP(HeatFileName, filepath, user);
		
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
	public static void createVendorLicense(User user) throws Exception {
		vendorLicenseName = "ciLicense" + UUID.randomUUID().toString().split("-")[0];
		RestResponse vendorLicenseResponse = OnboardingUtils.createVendorLicenseModels_1(vendorLicenseName, user);
		assertEquals("did not succeed to create vendor license model", 200,
				vendorLicenseResponse.getErrorCode().intValue());
		vendorId = ResponseParser.getValueFromJsonResponse(vendorLicenseResponse.getResponse(), "value");

		RestResponse vendorKeyGroupsResponse = OnboardingUtils.createVendorKeyGroups_2(vendorId, user);
		assertEquals("did not succeed to create vendor key groups", 200,
				vendorKeyGroupsResponse.getErrorCode().intValue());
		String keyGroupId = ResponseParser.getValueFromJsonResponse(vendorKeyGroupsResponse.getResponse(), "value");

		RestResponse vendorEntitlementPool = OnboardingUtils.createVendorEntitlementPool_3(vendorId, user);
		assertEquals("did not succeed to create vendor entitlement pool", 200,
				vendorEntitlementPool.getErrorCode().intValue());
		String entitlementPoolId = ResponseParser.getValueFromJsonResponse(vendorEntitlementPool.getResponse(),
				"value");

		RestResponse vendorLicenseFeatureGroups = OnboardingUtils.createVendorLicenseFeatureGroups_4(vendorId, keyGroupId,
				entitlementPoolId, user);
		assertEquals("did not succeed to create vendor license feature groups", 200,
				vendorLicenseFeatureGroups.getErrorCode().intValue());
		featureGroupId = ResponseParser.getValueFromJsonResponse(vendorLicenseFeatureGroups.getResponse(), "value");

		RestResponse vendorLicenseAgreement = OnboardingUtils.createVendorLicenseAgreement_5(vendorId, featureGroupId, user);
		assertEquals("did not succeed to create vendor license agreement", 200,
				vendorLicenseAgreement.getErrorCode().intValue());
		vendorLicenseAgreementId = ResponseParser.getValueFromJsonResponse(vendorLicenseAgreement.getResponse(),
				"value");

		RestResponse checkinVendorLicense = OnboardingUtils.checkinVendorLicense(vendorId, user);
		assertEquals("did not succeed to checkin vendor license", 200, checkinVendorLicense.getErrorCode().intValue());

		RestResponse submitVendorLicense = OnboardingUtils.submitVendorLicense(vendorId, user);
		assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

	}
	
	
	public static Pair<String, Map<String, String>> createVSP(String HeatFileName, String filepath, User user) throws Exception {
		String vspName = OnboardingUtils.handleFilename(HeatFileName);
		
		Pair<RestResponse, Map<String, String>> createNewVspPair = OnboardingUtils.createNewVendorSoftwareProduct(vspName, vendorLicenseName, vendorId, vendorLicenseAgreementId, featureGroupId, user);
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
		vspObject.put("vendorName", vendorLicenseName);
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
