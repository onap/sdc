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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.Assert;

import com.relevantcodes.extentreports.LogStatus;

public class OnboardingUtils {

	public OnboardingUtils() {
	}

	private static String vendorId;
	private static String vendorLicenseName;
	private static String vendorLicenseAgreementId;
	private static String featureGroupId;

	public int countFilesInZipDirectory(String filepath, String filename) throws Exception, Throwable {
		ZipFile zipFile = new ZipFile(filepath + filename);
		return zipFile.size() - 1;
	}

	public static Object[] getZipFileNamesFromFolder(String filepath) {
		return FileHandling.getFileNamesFromFolder(filepath, ".zip");
	}

	public static String createVendorSoftwareProduct(String HeatFileName, String filepath, User user)
			throws Exception, Throwable {
		String vspName = handleFilename(HeatFileName);
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "Starting to create vendor software product");

		RestResponse createNewVendorSoftwareProduct = createNewVendorSoftwareProduct(vspName, vendorLicenseName,
				vendorId, vendorLicenseAgreementId, featureGroupId, user);
		assertEquals("did not succeed to create new VSP", 200,
				createNewVendorSoftwareProduct.getErrorCode().intValue());
		String vspid = ResponseParser.getValueFromJsonResponse(createNewVendorSoftwareProduct.getResponse(), "vspId");

		RestResponse uploadHeatPackage = uploadHeatPackage(filepath, HeatFileName, vspid, user);
		assertEquals("did not succeed to upload HEAT package", 200, uploadHeatPackage.getErrorCode().intValue());

		RestResponse checkin = checkinVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to checking new VSP", 200, checkin.getErrorCode().intValue());

		RestResponse submit = submitVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to submit new VSP", 200, submit.getErrorCode().intValue());

		RestResponse createPackage = createPackageOfVendorSoftwareProduct(vspid, user);
		assertEquals("did not succeed to create package of new VSP ", 200, createPackage.getErrorCode().intValue());

		SetupCDTest.getExtendTest().log(LogStatus.INFO, "Succeeded to create vendor software product");

		return vspName;
	}

	private static String handleFilename(String heatFileName) {
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

	public static void createVendorLicense(User user) throws Exception {
		SetupCDTest.getExtendTest().log(LogStatus.INFO, "Starting to create vendor license");
		vendorLicenseName = "ciLicense" + getShortUUID();
		RestResponse vendorLicenseResponse = createVendorLicenseModels_1(vendorLicenseName, user);
		assertEquals("did not succeed to create vendor license model", 200,
				vendorLicenseResponse.getErrorCode().intValue());
		vendorId = ResponseParser.getValueFromJsonResponse(vendorLicenseResponse.getResponse(), "value");

		RestResponse vendorKeyGroupsResponse = createVendorKeyGroups_2(vendorId, user);
		assertEquals("did not succeed to create vendor key groups", 200,
				vendorKeyGroupsResponse.getErrorCode().intValue());
		String keyGroupId = ResponseParser.getValueFromJsonResponse(vendorKeyGroupsResponse.getResponse(), "value");

		RestResponse vendorEntitlementPool = createVendorEntitlementPool_3(vendorId, user);
		assertEquals("did not succeed to create vendor entitlement pool", 200,
				vendorEntitlementPool.getErrorCode().intValue());
		String entitlementPoolId = ResponseParser.getValueFromJsonResponse(vendorEntitlementPool.getResponse(),
				"value");

		RestResponse vendorLicenseFeatureGroups = createVendorLicenseFeatureGroups_4(vendorId, keyGroupId,
				entitlementPoolId, user);
		assertEquals("did not succeed to create vendor license feature groups", 200,
				vendorLicenseFeatureGroups.getErrorCode().intValue());
		featureGroupId = ResponseParser.getValueFromJsonResponse(vendorLicenseFeatureGroups.getResponse(), "value");

		RestResponse vendorLicenseAgreement = createVendorLicenseAgreement_5(vendorId, featureGroupId, user);
		assertEquals("did not succeed to create vendor license agreement", 200,
				vendorLicenseAgreement.getErrorCode().intValue());
		vendorLicenseAgreementId = ResponseParser.getValueFromJsonResponse(vendorLicenseAgreement.getResponse(),
				"value");

		RestResponse checkinVendorLicense = checkinVendorLicense(vendorId, user);
		assertEquals("did not succeed to checkin vendor license", 200, checkinVendorLicense.getErrorCode().intValue());

		RestResponse submitVendorLicense = submitVendorLicense(vendorId, user);
		assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

		SetupCDTest.getExtendTest().log(LogStatus.INFO, "Succeeded to create vendor license");
	}

	private static String getShortUUID() {
		return UUID.randomUUID().toString().split("-")[0];
	}

	private static RestResponse actionOnComponent(String vspid, String action, String onboardComponent, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/" + onboardComponent + "/%s/actions", config.getCatalogBeHost(), config.getCatalogBePort(), vspid);
		String userId = user.getUserId();

		JSONObject jObject = new JSONObject();
		jObject.put("action", action);

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPut(url, jObject.toString(), headersMap);
		return response;
	}

	private static RestResponse checkinVendorLicense(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Checkin", "vendor-license-models", user);
	}

	private static RestResponse submitVendorLicense(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Submit", "vendor-license-models", user);
	}

	private static RestResponse createVendorLicenseModels_1(String name, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models", config.getCatalogBeHost(), config.getCatalogBePort());
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

	private static RestResponse createVendorLicenseAgreement_5(String vspid, String featureGroupId, User user)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/license-agreements",
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

	private static RestResponse createVendorLicenseFeatureGroups_4(String vspid, String licenseKeyGroupId,
			String entitlementPoolId, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/feature-groups",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

		String userId = user.getUserId();

		JSONObject jObject = new JSONObject();
		jObject.put("name", "xyz");
		jObject.put("description", "new vendor license feature groups");
		jObject.put("partNumber", "123abc456");
		jObject.put("addedLicenseKeyGroupsIds", Arrays.asList(licenseKeyGroupId).toArray());
		jObject.put("addedEntitlementPoolsIds", Arrays.asList(entitlementPoolId).toArray());

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
		return response;

	}

	private static RestResponse createVendorEntitlementPool_3(String vspid, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/entitlement-pools",
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
		jObjectBody.put("name", "def");
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

	private static RestResponse createVendorKeyGroups_2(String vspid, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/license-key-groups",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

		String userId = user.getUserId();

		JSONObject jOperationalScope = new JSONObject();
		jOperationalScope.put("choices", Arrays.asList("Tenant").toArray());
		jOperationalScope.put("other", "");

		JSONObject jObjectBody = new JSONObject();
		jObjectBody.put("name", "keyGroup");
		jObjectBody.put("description", "new vendor license key group");
		jObjectBody.put("operationalScope", jOperationalScope);
		jObjectBody.put("type", "Universal");

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObjectBody.toString(), headersMap);
		return response;
	}

	private static RestResponse createNewVendorSoftwareProduct(String name, String vendorName, String vendorId,
			String licenseAgreementId, String featureGroupsId, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products",
				config.getCatalogBeHost(), config.getCatalogBePort());

		String userId = user.getUserId();

		JSONObject jlicensingDataObj = new JSONObject();
		jlicensingDataObj.put("licenseAgreement", licenseAgreementId);
		jlicensingDataObj.put("featureGroups", Arrays.asList(featureGroupsId).toArray());

		JSONObject jObject = new JSONObject();
		jObject.put("name", name);
		jObject.put("description", "new VSP description");
		jObject.put("category", "resourceNewCategory.generic");
		jObject.put("subCategory", "resourceNewCategory.generic.database");
		jObject.put("licensingVersion", "1.0");
		jObject.put("vendorName", vendorName);
		jObject.put("vendorId", vendorId);
		jObject.put("icon", "icon");
		jObject.put("licensingData", jlicensingDataObj);

		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();

		RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);

		return response;
	}

	private static RestResponse uploadHeatPackage(String filepath, String filename, String vspid, User user)
			throws Exception {
		Config config = Utils.getConfig();
		CloseableHttpResponse response = null;

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();
		mpBuilder.addPart("upload", new FileBody(getTestZipFile(filepath, filename)));

		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/upload", config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

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

	private static RestResponse checkinVendorSoftwareProduct(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Checkin", "vendor-software-products", user);
	}

	private static RestResponse submitVendorSoftwareProduct(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Submit", "vendor-software-products", user);
	}

	private static RestResponse createPackageOfVendorSoftwareProduct(String vspid, User user) throws Exception {
		return actionOnComponent(vspid, "Create_Package", "vendor-software-products", user);
	}

	protected static Map<String, String> prepareHeadersMap(String userId) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		
		return headersMap;
	}

}
