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

package org.openecomp.sdc.ci.tests.utilities;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.execute.setup.ArtifactsCorrelationManager;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

public class OnboardingUtils {

	protected static List<String> exludeVnfList = Arrays.asList("2016-197_vscp_vscp-fw_1610_e2e.zip", "2016-281_vProbes_BE_11_1_f_30_1610_e2e.zip", 
			"2016-282_vProbes_FE_11_1_f_30_1610_e2e.zip", "2016-044_vfw_fnat_30_1607_e2e.zip", "2017-376_vMOG_11_1.zip", "vMOG.zip", 
			"vMRF_USP_AIC3.0_1702.zip", "2016-211_vprobesbe_vprobes_be_30_1610_e2e.zip", "2016-005_vprobesfe_vprobes_fe_30_1607_e2e.zip", 
			"vMRF_RTT.zip", "2016-006_vvm_vvm_30_1607_e2e.zip", "2016-001_vvm_vvm_30_1607_e2e.zip");

	public OnboardingUtils() {
	}

	public static Pair<String, Map<String, String>> createVendorSoftwareProduct(String HeatFileName, String filepath, User user, AmdocsLicenseMembers amdocsLicenseMembers)
			throws Exception {
		Pair<String, Map<String, String>> pair = createVSP(HeatFileName, filepath, user, amdocsLicenseMembers);
		
		String vspid = pair.right.get("vspId");
				
		prepareVspForUse(user, vspid);
		
		return pair;
	}

	public static void prepareVspForUse(User user, String vspid) throws Exception {
		RestResponse checkin = checkinVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to checking new VSP", 200, checkin.getErrorCode().intValue());

		RestResponse submit = submitVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to submit new VSP", 200, submit.getErrorCode().intValue());

		RestResponse createPackage = createPackageOfVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to create package of new VSP ", 200, createPackage.getErrorCode().intValue());

		SetupCDTest.getExtendTest().log(Status.INFO, "Succeeded in creating the vendor software product");
	}

	public static Pair<String, Map<String, String>> createVSP(String HeatFileName, String filepath, User user, AmdocsLicenseMembers amdocsLicenseMembers) throws Exception {
		String vspName = handleFilename(HeatFileName);
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Starting to create the vendor software product");
		
		Pair<RestResponse, Map<String, String>> createNewVspPair = createNewVendorSoftwareProduct(vspName, amdocsLicenseMembers, user);
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
		
		RestResponse uploadHeatPackage = uploadHeatPackage(filepath, HeatFileName, vspid, user);
		assertEquals("did not succeed to upload HEAT package", 200, uploadHeatPackage.getErrorCode().intValue());
		
		RestResponse validateUpload = validateUpload(vspid, user);
		assertEquals("did not succeed to validate upload process", 200, validateUpload.getErrorCode().intValue());
		
		Pair<String, Map<String, String>> pair = new Pair<String, Map<String, String>>(vspName, vspObject);
		
		return pair;
	}
	
	public static void updateVspWithVfcArtifacts(String filepath, String vspId, String updatedSnmpPoll, String updatedSnmpTrap, String componentId, User user) throws Exception{
		RestResponse checkout = checkoutVendorSoftwareProduct(vspId, user);
		assertEquals("did not succeed to checkout new VSP", 200, checkout.getErrorCode().intValue());
		ExtentTestActions.log(Status.INFO, "Deleting SNMP POLL");
		deleteSnmpArtifact(componentId, vspId, user, SnmpTypeEnum.SNMP_POLL);
		ExtentTestActions.log(Status.INFO, "Deleting SNMP TRAP");
		deleteSnmpArtifact(componentId, vspId, user, SnmpTypeEnum.SNMP_TRAP);
		addVFCArtifacts(filepath, updatedSnmpPoll, updatedSnmpTrap, vspId, user, componentId);
		prepareVspForUse(user, vspId);
	}
	
	public static String updateVendorSoftwareProduct(String vspId, String HeatFileName, String filepath, User user)
			throws Exception, Throwable {
		String vspName = handleFilename(HeatFileName);
		SetupCDTest.getExtendTest().log(Status.INFO, "Starting to update the vendor software product");

		RestResponse checkout = checkoutVendorSoftwareProduct(vspId, user);
		assertEquals("did not succeed to checkout new VSP", 200, checkout.getErrorCode().intValue());

		RestResponse uploadHeatPackage = uploadHeatPackage(filepath, HeatFileName, vspId, user);
		assertEquals("did not succeed to upload HEAT package", 200, uploadHeatPackage.getErrorCode().intValue());
		
		RestResponse validateUpload = validateUpload(vspId, user);
		assertEquals("did not succeed to validate upload process", 200, validateUpload.getErrorCode().intValue());
		
		RestResponse checkin = checkinVendorSoftwareProduct(vspId, user);
		assertEquals("did not succeed to checking VSP", 200, checkin.getErrorCode().intValue());


		RestResponse submit = submitVendorSoftwareProduct(vspId, user);
		assertEquals("did not succeed to submit VSP", 200, submit.getErrorCode().intValue());

		RestResponse createPackage = createPackageOfVendorSoftwareProduct(vspId, user);
		assertEquals("did not succeed to update package of VSP ", 200, createPackage.getErrorCode().intValue());

		SetupCDTest.getExtendTest().log(Status.INFO, "Succeeded in updating the vendor software product");

		return vspName;
	}

	public static String handleFilename(String heatFileName) {
		final String namePrefix = "ciVFOnboarded-";
		final String nameSuffix = "-" + getShortUUID();

		String subHeatFileName = heatFileName.substring(0, heatFileName.lastIndexOf("."));

		if ((namePrefix + subHeatFileName + nameSuffix).length() >= 50) {
			subHeatFileName = subHeatFileName.substring(0, 50 - namePrefix.length() - nameSuffix.length());
		}

		if (subHeatFileName.contains("(") || subHeatFileName.contains(")")) {
			subHeatFileName = subHeatFileName.replace("(", "-");
			subHeatFileName = subHeatFileName.replace(")", "-");
		}

		String vnfName = namePrefix + subHeatFileName + nameSuffix;
		return vnfName;
	}
	
	public static String addVFCArtifacts(String filepath, String snmpPoll, String snmpTrap, String vspid, User user, String vspComponentId) throws Exception{
		vspComponentId = (vspComponentId == null) ? getVSPComponentId(vspid, user) : vspComponentId;
		if (vspComponentId != null){
			if (snmpPoll != null){
				ExtentTestActions.log(Status.INFO, "Adding VFC artifact of type SNMP POLL with the file " + snmpPoll);
				RestResponse uploadSnmpPollArtifact = uploadSnmpPollArtifact(filepath, snmpPoll, vspid, user, vspComponentId);
				assertEquals("Did not succeed to add SNMP POLL", 200, uploadSnmpPollArtifact.getErrorCode().intValue());
			}
			if (snmpTrap != null){
				ExtentTestActions.log(Status.INFO, "Adding VFC artifact of type SNMP TRAP with the file " + snmpTrap);
				RestResponse uploadSnmpTrapArtifact = uploadSnmpTrapArtifact(filepath, snmpTrap, vspid, user, vspComponentId);
				assertEquals("Did not succeed to add SNMP TRAP", 200, uploadSnmpTrapArtifact.getErrorCode().intValue());
			}
		}
		
		return vspComponentId;
	}
	
	public static String addVFCArtifacts(String filepath, String snmpPoll, String snmpTrap, String vspid, User user) throws Exception{
		return addVFCArtifacts(filepath, snmpPoll, snmpTrap, vspid, user, null);
	}

	private static RestResponse uploadSnmpPollArtifact(String filepath, String zipArtifact, String vspid, User user,
			String vspComponentId) throws FileNotFoundException, IOException, ClientProtocolException {
		Config config = Utils.getConfig();
		String snmpPollUrl = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/monitors/snmp/upload", 
				config.getCatalogBeHost(),config.getCatalogBePort(), vspid, vspComponentId);
		return uploadFile(filepath, zipArtifact, snmpPollUrl, user);
	}
	
	private static RestResponse uploadSnmpTrapArtifact(String filepath, String zipArtifact, String vspid, User user,
			String vspComponentId) throws FileNotFoundException, IOException, ClientProtocolException {
		Config config = Utils.getConfig();
		String snmpTrapUrl = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/monitors/snmp-trap/upload", 
				config.getCatalogBeHost(),config.getCatalogBePort(), vspid, vspComponentId);
		return uploadFile(filepath, zipArtifact, snmpTrapUrl, user);
	}
	
	private static RestResponse deleteSnmpArtifact(String componentId, String vspId, User user, SnmpTypeEnum snmpType) throws Exception
	{
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/monitors/%s", 
				config.getCatalogBeHost(),config.getCatalogBePort(), vspId, componentId, snmpType.getValue());
		String userId = user.getUserId();

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendDelete(url, headersMap);
		return response;
	}
	
	

	private static String getVSPComponentId(String vspid, User user) throws Exception, JSONException {
		RestResponse components = getVSPComponents(vspid, user);
		String response = components.getResponse();
		Map<String, Object> responseMap = (Map<String, Object>) JSONValue.parse(response);
		JSONArray results = (JSONArray)responseMap.get("results");
		for (Object res : results){
			Map<String, Object> compMap= (Map<String, Object>) JSONValue.parse(res.toString());
			String componentId = compMap.get("id").toString();
			return componentId;
		}
		return null;
	}
	
	private static RestResponse getVSPComponents(String vspid, User user) throws Exception{
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components", config.getCatalogBeHost(),config.getCatalogBePort(), vspid);
		String userId = user.getUserId();

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendGet(url, headersMap);
		return response;
	}

	public static AmdocsLicenseMembers createVendorLicense(User user) throws Exception {
		
		AmdocsLicenseMembers amdocsLicenseMembers;
		SetupCDTest.getExtendTest().log(Status.INFO, "Starting to create the vendor license");
		String vendorLicenseName = "ciLicense" + getShortUUID();
		RestResponse vendorLicenseResponse = createVendorLicenseModels_1(vendorLicenseName, user);
		assertEquals("did not succeed to create vendor license model", 200, vendorLicenseResponse.getErrorCode().intValue());
		String vendorId = ResponseParser.getValueFromJsonResponse(vendorLicenseResponse.getResponse(), "value");

		RestResponse vendorKeyGroupsResponse = createVendorKeyGroups_2(vendorId, user);
		assertEquals("did not succeed to create vendor key groups", 200, vendorKeyGroupsResponse.getErrorCode().intValue());
		String keyGroupId = ResponseParser.getValueFromJsonResponse(vendorKeyGroupsResponse.getResponse(), "value");

		RestResponse vendorEntitlementPool = createVendorEntitlementPool_3(vendorId, user);
		assertEquals("did not succeed to create vendor entitlement pool", 200, vendorEntitlementPool.getErrorCode().intValue());
		String entitlementPoolId = ResponseParser.getValueFromJsonResponse(vendorEntitlementPool.getResponse(), "value");

		RestResponse vendorLicenseFeatureGroups = createVendorLicenseFeatureGroups_4(vendorId, keyGroupId, entitlementPoolId, user);
		assertEquals("did not succeed to create vendor license feature groups", 200, vendorLicenseFeatureGroups.getErrorCode().intValue());
		String featureGroupId = ResponseParser.getValueFromJsonResponse(vendorLicenseFeatureGroups.getResponse(), "value");

		RestResponse vendorLicenseAgreement = createVendorLicenseAgreement_5(vendorId, featureGroupId, user);
		assertEquals("did not succeed to create vendor license agreement", 200, vendorLicenseAgreement.getErrorCode().intValue());
		String vendorLicenseAgreementId = ResponseParser.getValueFromJsonResponse(vendorLicenseAgreement.getResponse(), "value");

		RestResponse checkinVendorLicense = checkinVendorLicense(vendorId, user);
		assertEquals("did not succeed to checkin vendor license", 200, checkinVendorLicense.getErrorCode().intValue());

		RestResponse submitVendorLicense = submitVendorLicense(vendorId, user);
		assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

		SetupCDTest.getExtendTest().log(Status.INFO, "Succeeded in creating the vendor license");

		amdocsLicenseMembers = new AmdocsLicenseMembers(vendorId, vendorLicenseName, vendorLicenseAgreementId, featureGroupId);
		
		return amdocsLicenseMembers;
	}

	private static String getShortUUID() {
		return UUID.randomUUID().toString().split("-")[0];
	}

	private static RestResponse actionOnComponent(String vspid, String action, String onboardComponent, User user)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/" + onboardComponent + "/%s/versions/0.1/actions",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);
		String userId = user.getUserId();

		JSONObject jObject = new JSONObject();
		jObject.put("action", action);

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPut(url, jObject.toString(), headersMap);
		return response;
	}

	public static RestResponse checkinVendorLicense(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Checkin", "vendor-license-models", user);
	}

	public static RestResponse submitVendorLicense(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Submit", "vendor-license-models", user);
	}

	public static RestResponse createVendorLicenseModels_1(String name, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models", config.getCatalogBeHost(),
				config.getCatalogBePort());
		String userId = user.getUserId();

		JSONObject jObject = new JSONObject();
		jObject.put("vendorName", name);
		jObject.put("description", "new vendor license model");
		jObject.put("iconRef", "icon");

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
		return response;

	}

	public static RestResponse createVendorLicenseAgreement_5(String vspid, String featureGroupId, User user)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/license-agreements",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);
		String userId = user.getUserId();

		JSONObject licenseTermpObject = new JSONObject();
		licenseTermpObject.put("choice", "Fixed_Term");
		licenseTermpObject.put("other", "");

		JSONObject jObjectBody = new JSONObject();
		jObjectBody.put("name", "abc");
		jObjectBody.put("description", "new vendor license agreement");
		jObjectBody.put("requirementsAndConstrains", "abc");
		jObjectBody.put("licenseTerm", licenseTermpObject);
		jObjectBody.put("addedFeatureGroupsIds", Arrays.asList(featureGroupId).toArray());

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObjectBody.toString(), headersMap);
		return response;
	}

	public static RestResponse createVendorLicenseFeatureGroups_4(String vspid, String licenseKeyGroupId,
			String entitlementPoolId, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/feature-groups",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);
		String userId = user.getUserId();

		JSONObject jObject = new JSONObject();
		jObject.put("name", "xyz");
		jObject.put("description", "new vendor license feature groups");
		jObject.put("partNumber", "123abc456");
		jObject.put("manufacturerReferenceNumber", "5");
		jObject.put("addedLicenseKeyGroupsIds", Arrays.asList(licenseKeyGroupId).toArray());
		jObject.put("addedEntitlementPoolsIds", Arrays.asList(entitlementPoolId).toArray());

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
		return response;

	}

	public static RestResponse createVendorEntitlementPool_3(String vspid, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/entitlement-pools",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);
		String userId = user.getUserId();

		JSONObject jEntitlementMetricObject = new JSONObject();
		jEntitlementMetricObject.put("choice", "CPU");
		jEntitlementMetricObject.put("other", "");

		JSONObject jAggregationFunctionObject = new JSONObject();
		jAggregationFunctionObject.put("choice", "Peak");
		jAggregationFunctionObject.put("other", "");

		JSONObject jOperationalScope = new JSONObject();
		jOperationalScope.put("choices", Arrays.asList("Availability_Zone").toArray());
		jOperationalScope.put("other", "");

		JSONObject jTimeObject = new JSONObject();
		jTimeObject.put("choice", "Hour");
		jTimeObject.put("other", "");

		JSONObject jObjectBody = new JSONObject();
		jObjectBody.put("name", "def"+ getShortUUID());
		jObjectBody.put("description", "new vendor license entitlement pool");
		jObjectBody.put("thresholdValue", "23");
		jObjectBody.put("thresholdUnits", "Absolute");
		jObjectBody.put("entitlementMetric", jEntitlementMetricObject);
		jObjectBody.put("increments", "abcd");
		jObjectBody.put("aggregationFunction", jAggregationFunctionObject);
		jObjectBody.put("operationalScope", jOperationalScope);
		jObjectBody.put("time", jTimeObject);
		jObjectBody.put("manufacturerReferenceNumber", "123aaa");

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObjectBody.toString(), headersMap);
		return response;
	}

	public static RestResponse createVendorKeyGroups_2(String vspid, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/license-key-groups",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);
		String userId = user.getUserId();

		JSONObject jOperationalScope = new JSONObject();
		jOperationalScope.put("choices", Arrays.asList("Tenant").toArray());
		jOperationalScope.put("other", "");

		JSONObject jObjectBody = new JSONObject();
		jObjectBody.put("name", "keyGroup" + getShortUUID());
		jObjectBody.put("description", "new vendor license key group");
		jObjectBody.put("operationalScope", jOperationalScope);
		jObjectBody.put("type", "Universal");

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObjectBody.toString(), headersMap);
		return response;
	}

	public static Pair<RestResponse, Map<String, String>> createNewVendorSoftwareProduct(String name, AmdocsLicenseMembers amdocsLicenseMembers, User user) throws Exception {
		Map<String, String> vspMetadta = new HashMap<String, String>();
		
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products",
				config.getCatalogBeHost(), config.getCatalogBePort());

		String userId = user.getUserId();

		JSONObject jlicensingDataObj = new JSONObject();
		jlicensingDataObj.put("licenseAgreement", amdocsLicenseMembers.getVendorLicenseAgreementId());
		jlicensingDataObj.put("featureGroups", Arrays.asList(amdocsLicenseMembers.getFeatureGroupId()).toArray());
		
		JSONObject jlicensingVersionObj = new JSONObject();
		jlicensingVersionObj.put("id", "1.0");
		jlicensingVersionObj.put("label", "1.0");

		JSONObject jObject = new JSONObject();
		jObject.put("name", name);
		jObject.put("description", "new VSP description");
		jObject.put("category", "resourceNewCategory.generic");
		jObject.put("subCategory", "resourceNewCategory.generic.database");
		jObject.put("onboardingMethod", "HEAT");
		jObject.put("licensingVersion", jlicensingVersionObj);
		jObject.put("vendorName", amdocsLicenseMembers.getVendorLicenseName());
		jObject.put("vendorId", amdocsLicenseMembers.getVendorId());
		jObject.put("icon", "icon");
		jObject.put("licensingData", jlicensingDataObj);
		
		vspMetadta.put("description", jObject.getString("description"));
		vspMetadta.put("category", jObject.getString("category"));
		vspMetadta.put("subCategory", jObject.getString("subCategory").split("\\.")[2]);
		
		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();

		RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
		return new Pair<RestResponse, Map<String, String>>(response, vspMetadta);
	}
	
	public static RestResponse validateUpload(String vspid, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/orchestration-template-candidate/process",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

		String userId = user.getUserId();

		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();
		
		String body =null;

		RestResponse response = http.httpSendPut(url, body, headersMap);

		return response;
	}

	public static RestResponse uploadHeatPackage(String filepath, String filename, String vspid, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/orchestration-template-candidate", config.getCatalogBeHost(), config.getCatalogBePort(), vspid);
		return uploadFile(filepath, filename, url, user);
	}

	private static RestResponse uploadFile(String filepath, String filename, String url, User user)
			throws FileNotFoundException, IOException, ClientProtocolException {
		CloseableHttpResponse response = null;

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();
		mpBuilder.addPart("upload", new FileBody(getTestZipFile(filepath, filename)));

		Map<String, String> headersMap = prepareHeadersMap(user.getUserId());
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "multipart/form-data");

		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			RestResponse restResponse = new RestResponse();

			Iterator<String> iterator = headersMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = headersMap.get(key);
				httpPost.addHeader(key, value);
			}
			httpPost.setEntity(mpBuilder.build());
			response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String responseBody = null;
			if (entity != null) {
				InputStream instream = entity.getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(instream, writer);
				responseBody = writer.toString();
				try {

				} finally {
					instream.close();
				}
			}

			restResponse.setErrorCode(response.getStatusLine().getStatusCode());
			restResponse.setResponse(responseBody);

			return restResponse;

		} finally {
			closeResponse(response);
			closeHttpClient(client);

		}
	}

	private static void closeResponse(CloseableHttpResponse response) {
		try {
			if (response != null) {
				response.close();
			}
		} catch (IOException e) {
			System.out.println(String.format("failed to close client or response: %s", e.getMessage()));
		}
	}

	private static void closeHttpClient(CloseableHttpClient client) {
		try {
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			System.out.println(String.format("failed to close client or response: %s", e.getMessage()));
		}
	}

	private static File getTestZipFile(String filepath, String filename) throws IOException {
		Config config = Utils.getConfig();
		String sourceDir = config.getImportResourceTestsConfigDir();
		java.nio.file.Path filePath = FileSystems.getDefault().getPath(filepath + File.separator + filename);
		return filePath.toFile();
	}

	public static RestResponse checkinVendorSoftwareProduct(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Checkin", "vendor-software-products", user);
	}
	
	private static RestResponse checkoutVendorSoftwareProduct(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Checkout", "vendor-software-products", user);
	}

	public static RestResponse submitVendorSoftwareProduct(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Submit", "vendor-software-products", user);
	}

	public static RestResponse createPackageOfVendorSoftwareProduct(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Create_Package", "vendor-software-products", user);
	}

	protected static Map<String, String> prepareHeadersMap(String userId) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		return headersMap;
	}

	
	private static void importUpdateVSP(Pair<String, Map<String, String>> vsp, boolean isUpdate) throws Exception{
		String vspName = vsp.left;
		Map<String, String> vspMetadata = vsp.right;
		boolean vspFound = HomePage.searchForVSP(vspName);

		if (vspFound){

			List<WebElement> elemenetsFromTable = HomePage.getElemenetsFromTable();
//			WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 30);
//			WebElement findElement = wait.until(ExpectedConditions.visibilityOf(elemenetsFromTable.get(1)));
//			findElement.click();
			elemenetsFromTable.get(1).click();
			GeneralUIUtils.waitForLoader();
			
			if (isUpdate){
				GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.UPDATE_VSP.getValue());

			}
			else{
				GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.IMPORT_VSP.getValue());
			}
			
			String lifeCycleState = ResourceGeneralPage.getLifeCycleState();
			boolean needCheckout = lifeCycleState.equals(LifeCycleStateEnum.CHECKIN.getValue()) || lifeCycleState.equals(LifeCycleStateEnum.CERTIFIED.getValue());
			if (needCheckout)
			{
				try {
					ResourceGeneralPage.clickCheckoutButton();
					Assert.assertTrue(ResourceGeneralPage.getLifeCycleState().equals(LifeCycleStateEnum.CHECKOUT.getValue()), "Did not succeed to checkout");
					
				} catch (Exception e) {
					ExtentTestActions.log(Status.ERROR, "Did not succeed to checkout");
					e.printStackTrace();
				}
				GeneralUIUtils.waitForLoader();
			}
			
			//Metadata verification 
			VfVerificator.verifyOnboardedVnfMetadata(vspName, vspMetadata);
			
			ExtentTestActions.log(Status.INFO, "Clicking create/update VNF");
			String duration = GeneralUIUtils.getActionDuration(() -> waitUntilVnfCreated());
		    ExtentTestActions.log(Status.INFO, "Succeeded in importing/updating " + vspName, duration);
		}
		else{
			Assert.fail("Did not find VSP named " + vspName);
		}
	}

	private static void waitUntilVnfCreated() {
		GeneralUIUtils.clickOnElementByTestIdWithoutWait(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
		GeneralUIUtils.waitForLoader(60*10);
		GeneralUIUtils.waitForAngular();
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
	}
	
	public static void updateVSP(Pair<String, Map<String, String>> vsp) throws Exception{
		ExtentTestActions.log(Status.INFO, "Updating VSP " + vsp.left);
		importUpdateVSP(vsp, true);
	}
	
	public static void importVSP(Pair<String, Map<String, String>> vsp) throws Exception{
		ExtentTestActions.log(Status.INFO, "Importing VSP " + vsp.left);
		importUpdateVSP(vsp, false);
	}

	public static void updateVnfAndValidate(String filepath, Pair<String, Map<String, String>> vsp, String updatedVnfFile, User user) throws Exception, Throwable {
		ExtentTestActions.log(Status.INFO, String.format("Going to update the VNF with %s......", updatedVnfFile));
		System.out.println(String.format("Going to update the VNF with %s......", updatedVnfFile));
		
		Map<String, String> vspMap = vsp.right;
		String vspId = vspMap.get("vspId");
		
		updateVendorSoftwareProduct(vspId, updatedVnfFile, filepath, user);
		HomePage.showVspRepository();
		updateVSP(vsp);
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
		DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, updatedVnfFile);
	}

	public static Pair<String, Map<String, String>> onboardAndValidate(String filepath, String vnfFile, User user) throws Exception {
		ExtentTestActions.log(Status.INFO, String.format("Going to onboard the VNF %s", vnfFile));
		System.out.println(String.format("Going to onboard the VNF %s", vnfFile));
	
		AmdocsLicenseMembers amdocsLicenseMembers = createVendorLicense(user);
		Pair<String, Map<String, String>> createVendorSoftwareProduct = createVendorSoftwareProduct(vnfFile, filepath, user, amdocsLicenseMembers);
		String vspName = createVendorSoftwareProduct.left;
		
		DownloadManager.downloadCsarByNameFromVSPRepository(vspName, createVendorSoftwareProduct.right.get("vspId"));
		File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
		
		ExtentTestActions.log(Status.INFO, String.format("Searching for onboarded %s", vnfFile));
		HomePage.showVspRepository();
		ExtentTestActions.log(Status.INFO,String.format("Going to import %s", vnfFile.substring(0, vnfFile.indexOf("."))));
		importVSP(createVendorSoftwareProduct);
		
		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
		
		// Verify deployment artifacts
		Map<String, Object> combinedMap = ArtifactFromCsar.combineHeatArtifacstWithFolderArtifacsToMap(latestFilefromDir.getAbsolutePath());
		
		LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get("Deployment"));
		ArtifactsCorrelationManager.addVNFartifactDetails(vspName, deploymentArtifacts);
		
		List<String> heatEnvFilesFromCSAR = deploymentArtifacts.stream().filter(e -> e.getType().equals("HEAT_ENV")).
																		 map(e -> e.getFileName()).
																		 collect(Collectors.toList());

		validateDeploymentArtifactsVersion(deploymentArtifacts, heatEnvFilesFromCSAR);

		DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, vnfFile);
		return createVendorSoftwareProduct;
	}

	public static void validateDeploymentArtifactsVersion(LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts,
			List<String> heatEnvFilesFromCSAR) {
		String artifactVersion;
		String artifactName;

		for(HeatMetaFirstLevelDefinition deploymentArtifact: deploymentArtifacts) {
			artifactVersion = "1";

			if(deploymentArtifact.getType().equals("HEAT_ENV")) {
				continue;
			} else if(deploymentArtifact.getFileName().contains(".")) {
				artifactName = deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().lastIndexOf("."));
			} else {
				artifactName = deploymentArtifact.getFileName().trim();
			}

			if (heatEnvFilesFromCSAR.contains(artifactName + ".env")){
				artifactVersion = "2";
			}
			ArtifactUIUtils.validateArtifactNameVersionType(artifactName, artifactVersion, deploymentArtifact.getType());
		}
	}
	
	
	/**
	 * @return
	 * The method returns VNF names list from Files directory under sdc-vnfs repository
	 */
	public static List<String> getVnfNamesFileList() {
		String filepath = FileHandling.getVnfRepositoryPath();
		List<String> fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		fileNamesFromFolder.removeAll(exludeVnfList);
		return fileNamesFromFolder;
	}

}

	enum SnmpTypeEnum{
		SNMP_POLL ("snmp"),
		SNMP_TRAP ("snmp-trap");
		
		private String value;
	
		public String getValue() {
			return value;
		}
	
		private SnmpTypeEnum(String value) {
			this.value = value;
	}
	
}

