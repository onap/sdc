package org.openecomp.sdc.uici.tests.utilities;

import static org.testng.AssertJUnit.assertTrue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.AssertTrue;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.junit.Test;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;

import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

/**
 * Utility Class For Onboarding
 * 
 * @author mshitrit
 *
 */
public final class OnboardUtility {

	private OnboardUtility() {
		throw new UnsupportedOperationException();
	}

	private static final class Constants {
		private static final String VENDOR_SOFTWARE_PRODUCTS = "vendor-software-products";
		private static final String VENDOR_LICENSE_MODELS = "vendor-license-models";

		private static final String VSP_ID = "vspId";
		private static final String VALUE = "value";

		enum Actions {
			CHECK_IN("Checkin"), SUBMIT("Submit"), CREATE_PACKAGE("Create_Package");

			private String value;

			private Actions(String value) {
				this.value = value;
			}

			public String getValue() {
				return value;
			}
		};
	}

	/**
	 * @param heatFileName
	 * @param filepath
	 * @param userId
	 * @param vld
	 * @return
	 * @throws Exception
	 */
	public static void createVendorSoftwareProduct(String heatFileName, String filepath, String userId,
			VendorLicenseDetails vld) {
		RestResponse createNewVendorSoftwareProduct = FunctionalInterfaces
				.swallowException(() -> createNewVendorSoftwareProduct(vld, userId));
		assertTrue(createNewVendorSoftwareProduct.getErrorCode() == HttpStatus.SC_OK);
		String vspid = ResponseParser.getValueFromJsonResponse(createNewVendorSoftwareProduct.getResponse(),
				Constants.VSP_ID);

		RestResponse response = FunctionalInterfaces
				.swallowException(() -> uploadHeatPackage(filepath, heatFileName, vspid, userId));
		assertTrue(response.getErrorCode() == HttpStatus.SC_OK);

		response = actionOnComponent(vspid, Constants.Actions.CHECK_IN.getValue(), Constants.VENDOR_SOFTWARE_PRODUCTS,
				userId);
		assertTrue(response.getErrorCode() == HttpStatus.SC_OK);

		response = actionOnComponent(vspid, Constants.Actions.SUBMIT.getValue(), Constants.VENDOR_SOFTWARE_PRODUCTS,
				userId);
		assertTrue(response.getErrorCode() == HttpStatus.SC_OK);

		response = actionOnComponent(vspid, Constants.Actions.CREATE_PACKAGE.getValue(),
				Constants.VENDOR_SOFTWARE_PRODUCTS, userId);
		assertTrue(response.getErrorCode() == HttpStatus.SC_OK);

	}

	/**
	 * Contains Details Relevant to Vendor License
	 * 
	 * @author mshitrit
	 *
	 */
	public static final class VendorLicenseDetails {
		private final String vendorId;
		private final String vendorLicenseName;
		private final String vendorLicenseAgreementId;
		private final String featureGroupId;
		private final String vendorSoftwareProduct;

		private VendorLicenseDetails(String vendorId, String vendorLicenseName, String vendorLicenseAgreementId,
				String featureGroupId) {
			super();
			this.vendorId = vendorId;
			this.vendorLicenseName = vendorLicenseName;
			this.vendorLicenseAgreementId = vendorLicenseAgreementId;
			this.featureGroupId = featureGroupId;
			vendorSoftwareProduct = UUID.randomUUID().toString().split("-")[0];
		}

		public String getVendorId() {
			return vendorId;
		}

		public String getVendorLicenseName() {
			return vendorLicenseName;
		}

		public String getVendorLicenseAgreementId() {
			return vendorLicenseAgreementId;
		}

		public String getFeatureGroupId() {
			return featureGroupId;
		}

		public String getVendorSoftwareProduct() {
			return vendorSoftwareProduct;
		}

	}

	/**
	 * Creates Vendor License
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public static VendorLicenseDetails createVendorLicense(String userId) {
		final String fieldNameValue = Constants.VALUE;
		String vendorLicenseName = UUID.randomUUID().toString().split("-")[0];
		RestResponse vendorLicenseResponse = FunctionalInterfaces
				.swallowException(() -> createVendorLicenseModels(vendorLicenseName, userId));
		assertTrue(vendorLicenseResponse.getErrorCode() == HttpStatus.SC_OK);

		String vendorId = ResponseParser.getValueFromJsonResponse(vendorLicenseResponse.getResponse(), fieldNameValue);

		RestResponse vendorKeyGroupsResponse = FunctionalInterfaces
				.swallowException(() -> createVendorKeyGroups(vendorId, userId));
		assertTrue(vendorKeyGroupsResponse.getErrorCode() == HttpStatus.SC_OK);
		String keyGroupId = ResponseParser.getValueFromJsonResponse(vendorKeyGroupsResponse.getResponse(),
				fieldNameValue);

		RestResponse vendorEntitlementPool = FunctionalInterfaces
				.swallowException(() -> createVendorEntitlementPool(vendorId, userId));
		assertTrue(vendorEntitlementPool.getErrorCode() == HttpStatus.SC_OK);
		String entitlementPoolId = ResponseParser.getValueFromJsonResponse(vendorEntitlementPool.getResponse(),
				fieldNameValue);

		RestResponse vendorLicenseFeatureGroups = FunctionalInterfaces.swallowException(
				() -> createVendorLicenseFeatureGroups(vendorId, keyGroupId, entitlementPoolId, userId));
		assertTrue(vendorLicenseFeatureGroups.getErrorCode() == HttpStatus.SC_OK);
		String featureGroupId = ResponseParser.getValueFromJsonResponse(vendorLicenseFeatureGroups.getResponse(),
				fieldNameValue);

		RestResponse vendorLicenseAgreement = FunctionalInterfaces
				.swallowException(() -> createVendorLicenseAgreement(vendorId, featureGroupId, userId));
		assertTrue(vendorLicenseAgreement.getErrorCode() == HttpStatus.SC_OK);
		String vendorLicenseAgreementId = ResponseParser.getValueFromJsonResponse(vendorLicenseAgreement.getResponse(),
				fieldNameValue);

		RestResponse actionOnComponent = actionOnComponent(vendorId, Constants.Actions.CHECK_IN.getValue(),
				Constants.VENDOR_LICENSE_MODELS, userId);
		assertTrue(actionOnComponent.getErrorCode() == HttpStatus.SC_OK);

		actionOnComponent = actionOnComponent(vendorId, Constants.Actions.SUBMIT.getValue(),
				Constants.VENDOR_LICENSE_MODELS, userId);
		assertTrue(actionOnComponent.getErrorCode() == HttpStatus.SC_OK);

		return new VendorLicenseDetails(vendorId, vendorLicenseName, vendorLicenseAgreementId, featureGroupId);
	}

	private static RestResponse actionOnComponent(String vspid, String action, String onboardComponent, String userId) {
		Config config = FunctionalInterfaces.swallowException(() -> Utils.getConfig());
		String url = String.format("http://%s:%s/onboarding-api/v1.0/%s/%s/actions", config.getCatalogBeHost(),
				config.getCatalogBePort(), onboardComponent, vspid);

		JSONObject jObject = new JSONObject();
		FunctionalInterfaces.swallowException(() -> jObject.put("action", action));

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = FunctionalInterfaces
				.swallowException(() -> http.httpSendPut(url, jObject.toString(), headersMap));
		return response;
	}

	private static RestResponse createVendorLicenseModels(String name, String userId) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models", config.getCatalogBeHost(),
				config.getCatalogBePort());

		JSONObject jObject = new JSONObject();
		jObject.put("vendorName", name);
		jObject.put("description", "new vendor license model");
		jObject.put("iconRef", "icon");

		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
		return response;

	}

	private static RestResponse createVendorLicenseAgreement(String vspid, String featureGroupId, String userId)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/license-agreements",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

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

	private static RestResponse createVendorLicenseFeatureGroups(String vspid, String licenseKeyGroupId,
			String entitlementPoolId, String userId) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/feature-groups",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

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

	private static RestResponse createVendorEntitlementPool(String vspid, String userId) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/entitlement-pools",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

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

	private static RestResponse createVendorKeyGroups(String vspid, String userId) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/license-key-groups",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

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

	private static RestResponse createNewVendorSoftwareProduct(VendorLicenseDetails vld, String userId)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products",
				config.getCatalogBeHost(), config.getCatalogBePort());

		JSONObject jlicensingDataObj = new JSONObject();
		jlicensingDataObj.put("licenseAgreement", vld.getVendorLicenseAgreementId());
		jlicensingDataObj.put("featureGroups", Arrays.asList(vld.getFeatureGroupId()).toArray());

		JSONObject jObject = new JSONObject();
		jObject.put("name", vld.getVendorSoftwareProduct());
		jObject.put("description", "new VSP description");
		jObject.put("category", "resourceNewCategory.generic");
		jObject.put("subCategory", "resourceNewCategory.generic.database");
		jObject.put("licensingVersion", "1.0");
		jObject.put("vendorName", vld.getVendorLicenseName());
		jObject.put("vendorId", vld.getVendorId());
		jObject.put("icon", "icon");
		jObject.put("licensingData", jlicensingDataObj);

		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();

		RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);

		return response;
	}

	private static RestResponse uploadHeatPackage(String filepath, String filename, String vspid, String userId)
			throws Exception {
		Config config = Utils.getConfig();
		CloseableHttpResponse response = null;

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();
		mpBuilder.addPart("resourceZip", new FileBody(getTestZipFile(filepath, filename)));

		String url = String.format("http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/upload",
				config.getCatalogBeHost(), config.getCatalogBePort(), vspid);

		Map<String, String> headersMap = prepareHeadersMap(userId);
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

	protected static Map<String, String> prepareHeadersMap(String userId) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		return headersMap;
	}

	public static void createVfFromOnboarding(String userID, String zipFile, String filepath) {
		VendorLicenseDetails vld = createVendorLicense(userID);
		createVendorSoftwareProduct(zipFile, filepath, userID, vld);

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.OPEN_MODAL_BUTTON.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.ONBOARDING_SEARCH.getValue())
				.sendKeys(vld.getVendorSoftwareProduct());

		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.sleep(1000);
		GeneralUIUtils.getWebElementWaitForClickable(vld.getVendorSoftwareProduct()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.IMPORT_ICON.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.LifeCyleChangeButtons.CREATE.getValue()).click();
		GeneralUIUtils.waitForLoader(300);
	}

}
