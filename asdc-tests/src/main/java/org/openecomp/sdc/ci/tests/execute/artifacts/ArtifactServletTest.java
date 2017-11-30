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

package org.openecomp.sdc.ci.tests.execute.artifacts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import fj.data.Either;

public class ArtifactServletTest extends ComponentBaseTest {

	private static Logger log = LoggerFactory.getLogger(ArtifactServletTest.class.getName());
	protected static final String UPLOAD_ARTIFACT_PAYLOAD = "UHVUVFktVXNlci1LZXktRmlsZS0yOiBzc2gtcnNhDQpFbmNyeXB0aW9uOiBhZXMyNTYtY2JjDQpDb21tZW5wOA0K";
	protected static final String UPLOAD_ARTIFACT_NAME = "TLV_prv.ppk";
	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";
	protected Gson gson = new Gson();
	protected JSONParser jsonParser = new JSONParser();
	protected String serviceVersion;
	protected Resource resourceDetailsVFCcomp;
	protected Service defaultService1;

	protected User sdncUserDetails;

	@Rule
	public static TestName name = new TestName();

	public ArtifactServletTest() {
		super(name, ArtifactServletTest.class.getName());

	}

	@BeforeMethod
	public void create() throws Exception {

		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		Either<Resource, RestResponse> resourceDetailsVFCcompE = AtomicOperationUtils
				.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.COMPUTE,
						ResourceCategoryEnum.APPLICATION_L4_APP_SERVER, UserRoleEnum.DESIGNER, true);
		resourceDetailsVFCcomp = resourceDetailsVFCcompE.left().value();
		Either<Service, RestResponse> defaultService1e = AtomicOperationUtils
				.createDefaultService(UserRoleEnum.DESIGNER, true);
		defaultService1 = defaultService1e.left().value();
	}

	@Test
	public void upadteArtifactWithPayLoadToResourcseTest() throws Exception {

		ArtifactReqDetails defaultArtifact = ElementFactory.getDefaultArtifact();

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		int status = response.getErrorCode();
		AssertJUnit.assertEquals("add informational artifact request returned status: " + response.getErrorCode(), 200,
				status);

		defaultArtifact.setDescription("kjglkh");
		defaultArtifact.setArtifactName("install_apache.sh");
		defaultArtifact.setArtifactType("SHELL");
		defaultArtifact.setPayload("new payload");

		response = ArtifactRestUtils.updateInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		status = response.getErrorCode();
		AssertJUnit.assertEquals("failed to update artifact metatdata: " + response.getErrorCode(), 200, status);

		response = ArtifactRestUtils.deleteInformationalArtifactFromResource(resourceDetailsVFCcomp.getUniqueId(),
				defaultArtifact, sdncUserDetails);
		status = response.getErrorCode();
		AssertJUnit.assertEquals("failed to remove artifact: " + response.getErrorCode(), 200, status);

	}

	@Test
	public void createAndUpdateArtifactToInterface() throws Exception {

		CloseableHttpResponse response;
		int status;
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			// upload artifact to interface
			String interfaceName = "Standard";
			String operationName = "configure";

			String userBodyJson = createUploadArtifactBodyJson();
			String url = String.format(Urls.UPLOAD_ARTIFACT_BY_INTERFACE_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId(), interfaceName, operationName);

			HttpPost httpPost = createPostAddArtifactRequeast(userBodyJson, url, true);
			response = httpclient.execute(httpPost);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 200, returned :" + status, status, 200);

			// get artifact uniqueId
			String artifactId = getLifecycleArtifactUid(response);

			Map<String, Object> jsonBody = new HashMap<String, Object>();
			jsonBody.put("artifactName", "TLV_prv.ppk");
			jsonBody.put("artifactDisplayName", "configure");
			jsonBody.put("artifactType", "SHELL");
			jsonBody.put("mandatory", "false");
			String newDescription = "new something";
			jsonBody.put("description", newDescription);
			jsonBody.put("artifactLabel", "configure");
			userBodyJson = gson.toJson(jsonBody);

			url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_BY_INTERFACE_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId(), interfaceName, operationName,
					artifactId);

			httpPost = createPostAddArtifactRequeast(userBodyJson, url, false);

			response = httpclient.execute(httpPost);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 200, returned :" + status, 200, status);

			url = String.format(Urls.GET_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(),
					resourceDetailsVFCcomp.getUniqueId());
			HttpGet httpGet = createGetRequest(url);
			response = httpclient.execute(httpGet);
			AssertJUnit.assertTrue(response.getStatusLine().getStatusCode() == 200);
			String responseString = new BasicResponseHandler().handleResponse(response);

			JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
			responseMap = (JSONObject) responseMap.get("interfaces");
			responseMap = (JSONObject) responseMap.get(interfaceName.toLowerCase());
			responseMap = (JSONObject) responseMap.get("operations");
			responseMap = (JSONObject) responseMap.get(operationName.toLowerCase());
			responseMap = (JSONObject) responseMap.get("implementation");
			String description = (String) responseMap.get("description");

			AssertJUnit.assertEquals("the new description value was not set", newDescription, description);

			// delete artifact
			url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_BY_INTERFACE_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId(), interfaceName, operationName,
					artifactId);
			HttpDelete httpDelete = createDeleteArtifactRequest(url);

			response = httpclient.execute(httpDelete);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 200, returned :" + status, status, 200);
		} finally {
			httpclient.close();
		}

	}

	protected String createUploadArtifactBodyJson() {
		Map<String, Object> jsonBody = new HashMap<String, Object>();
		jsonBody.put("artifactName", UPLOAD_ARTIFACT_NAME);
		jsonBody.put("artifactDisplayName", "configure");
		jsonBody.put("artifactType", "SHELL");
		jsonBody.put("mandatory", "false");
		jsonBody.put("description", "ff");
		jsonBody.put("payloadData", UPLOAD_ARTIFACT_PAYLOAD);
		jsonBody.put("artifactLabel", "configure");
		return gson.toJson(jsonBody);
	}

	protected ArtifactDefinition getArtifactDataFromJson(String json) {
		Gson gson = new Gson();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact = gson.fromJson(json, ArtifactDefinition.class);

		/*
		 * atifact.setArtifactName(UPLOAD_ARTIFACT_NAME);
		 * artifact.setArtifactDisplayName("configure");
		 * artifact.setArtifactType("SHELL"); artifact.setMandatory(false);
		 * artifact.setDescription("ff");
		 * artifact.setPayloadData(UPLOAD_ARTIFACT_PAYLOAD);
		 * artifact.setArtifactLabel("configure");
		 */
		return artifact;
	}

	protected HttpGet createGetRequest(String url) {
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		httpGet.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		httpGet.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		return httpGet;
	}

	protected String getArtifactUid(HttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		String artifactId = (String) responseMap.get("uniqueId");
		return artifactId;
	}

	protected String getArtifactEsId(HttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		String esId = (String) responseMap.get("EsId");
		return esId;
	}

	protected ArtifactDefinition addArtifactDataFromResponse(HttpResponse response, ArtifactDefinition artifact)
			throws HttpResponseException, IOException, ParseException {
		// String responseString = new
		// BasicResponseHandler().handleResponse(response);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		artifact.setEsId((String) responseMap.get("esId"));
		artifact.setUniqueId((String) responseMap.get("uniqueId"));
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.findType((String) responseMap.get("artifactGroupType")));
		artifact.setTimeout(((Long) responseMap.get("timeout")).intValue());
		return artifact;
	}

	protected String getLifecycleArtifactUid(CloseableHttpResponse response)
			throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		responseMap = (JSONObject) responseMap.get("implementation");
		String artifactId = (String) responseMap.get("uniqueId");
		return artifactId;
	}

	protected HttpDelete createDeleteArtifactRequest(String url) {
		HttpDelete httpDelete = new HttpDelete(url);
		httpDelete.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		httpDelete.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		return httpDelete;
	}

	protected HttpPost createPostAddArtifactRequeast(String jsonBody, String url, boolean addMd5Header)
			throws UnsupportedEncodingException {
		HttpPost httppost = new HttpPost(url);
		httppost.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		httppost.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		httppost.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		if (addMd5Header) {
			httppost.addHeader(HttpHeaderEnum.Content_MD5.getValue(), GeneralUtility.calculateMD5Base64EncodedByString(jsonBody));
		}
		StringEntity input = new StringEntity(jsonBody);
		input.setContentType("application/json");
		httppost.setEntity(input);
		log.debug("Executing request {}", httppost.getRequestLine());
		return httppost;
	}

	protected String createLoadArtifactBody() {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("artifactName", "install_apache2.sh");
		json.put("artifactType", "SHELL");
		json.put("description", "ddd");
		json.put("payloadData", "UEsDBAoAAAAIAAeLb0bDQz");
		json.put("artifactLabel", "name123");

		String jsonStr = gson.toJson(json);
		return jsonStr;
	}

	protected void checkDeleteResponse(RestResponse response) {
		BaseRestUtils.checkStatusCode(response, "delete request failed", false, 204, 404);
	}

	protected ArtifactUiDownloadData getArtifactUiDownloadData(String artifactUiDownloadDataStr) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		try {
			ArtifactUiDownloadData artifactUiDownloadData = mapper.readValue(artifactUiDownloadDataStr,
					ArtifactUiDownloadData.class);
			return artifactUiDownloadData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// TODO
	// @Ignore("")
	@Test
	public void addArtifactNoPayLoadToResourcseTest() throws Exception {
		ArtifactReqDetails defaultArtifact = ElementFactory.getDefaultArtifact();
		defaultArtifact.setPayload(null);

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		int status = response.getErrorCode();
		AssertJUnit.assertTrue(status == 400);

	}

	@Test
	public void upadteArtifactNoPayLoadToResourcseTest() throws Exception {

		ArtifactReqDetails defaultArtifact = ElementFactory.getDefaultArtifact();

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		int status = response.getErrorCode();
		AssertJUnit.assertEquals("add informational artifact request returned status: " + response.getErrorCode(), 200,
				status);

		defaultArtifact.setDescription("kjglkh");
		defaultArtifact.setArtifactName("install_apache.sh");
		defaultArtifact.setArtifactType("SHELL");
		defaultArtifact.setPayload(null);

		response = ArtifactRestUtils.updateInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		status = response.getErrorCode();
		AssertJUnit.assertEquals("failed to update artifact metatdata: " + response.getErrorCode(), 200, status);

		response = ArtifactRestUtils.deleteInformationalArtifactFromResource(resourceDetailsVFCcomp.getUniqueId(),
				defaultArtifact, sdncUserDetails);
		status = response.getErrorCode();
		AssertJUnit.assertEquals("failed to remove artifact: " + response.getErrorCode(), 200, status);

	}

	// TODO
	@Test(enabled = false)
	public void updateDeploymentArtifactToResourcseTest() throws Exception {

		ArtifactReqDetails defaultArtifact = ElementFactory.getDefaultDeploymentArtifactForType("HEAT");

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		int status = response.getErrorCode();
		AssertJUnit.assertEquals("add informational artifact request returned status: " + response.getErrorCode(), 200,
				status);

		response = ArtifactRestUtils.updateInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		status = response.getErrorCode();
		AssertJUnit.assertEquals("failed to update artifact metatdata: " + response.getErrorCode(), 200, status);

		response = ArtifactRestUtils.deleteInformationalArtifactFromResource(resourceDetailsVFCcomp.getUniqueId(),
				defaultArtifact, sdncUserDetails);
		status = response.getErrorCode();
		AssertJUnit.assertEquals("failed to remove artifact: " + response.getErrorCode(), 200, status);

	}

	// --------------------
	@Test
	public void addArtifactToResourcse_AlreadyExistsTest() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String jsonBody = createLoadArtifactBody();

			String url = String.format(Urls.ADD_ARTIFACT_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId());
			HttpPost httppost = createPostAddArtifactRequeast(jsonBody, url, true);
			CloseableHttpResponse response = httpclient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertTrue("failed to add artifact", status == 200);

			String artifactId = getArtifactUid(response);

			httppost = createPostAddArtifactRequeast(jsonBody, url, true);
			response = httpclient.execute(httppost);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("the returned status code is in correct", status, 400);

			url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId(), artifactId);
			HttpDelete httpDelete = createDeleteArtifactRequest(url);
			response = httpclient.execute(httpDelete);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertTrue("failed to remove artifact", status == 200);
		} finally {
			httpclient.close();
		}

	}

	@Test
	public void addArtifactToResourcse_MissingContentTest() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			Map<String, Object> json = new HashMap<String, Object>();
			json.put("description", "desc");
			json.put("payloadData", "UEsDBAoAAAAIAAeLb0bDQz");
			json.put("Content-MD5", "YTg2Mjg4MWJhNmI5NzBiNzdDFkMWI=");

			String jsonBody = gson.toJson(json);

			String url = String.format(Urls.ADD_ARTIFACT_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId());
			HttpPost httppost = createPostAddArtifactRequeast(jsonBody, url, true);
			CloseableHttpResponse response = httpclient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("the returned status code is in correct", status, 400);
		} finally {
			httpclient.close();
		}

	}

	@Test
	public void addArtifactToResourcse_MissingMd5Test() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HashMap<String, Object> json = new HashMap<String, Object>();
			json.put("artifactName", "install_apache.sh");
			json.put("artifactType", "SHELL");
			json.put("description", "kjglkh");
			json.put("payloadData", "UEsDBYTEIWUYIFHWFMABCNAoAAAAIAAeLb0bDQz");
			json.put("artifactLabel", "name123");
			String url = String.format(Urls.ADD_ARTIFACT_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId());
			String jsonBody = gson.toJson(json);
			HttpPost httppost = createPostAddArtifactRequeast(jsonBody, url, false);
			CloseableHttpResponse response = httpclient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertTrue("failed to update artifact metatdata", status == 400);
		} finally {
			httpclient.close();
		}

	}

	@Test
	public void deleteArtifact_NotExistsTest() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId(), "someFakeId");
			HttpDelete httpDelete = createDeleteArtifactRequest(url);
			CloseableHttpResponse response = httpclient.execute(httpDelete);
			int status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("the returned status code is in correct", status, 404);
		} finally {
			httpclient.close();
		}

	}

	@Test
	public void createAndRemoveArtifactToInterface() throws Exception {
		CloseableHttpResponse response;
		int status;
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			// upload artifact to interface
			String interfaceName = "Standard";
			String operationName = "configure";

			String userBodyJson = createUploadArtifactBodyJson();
			String url = String.format(Urls.UPLOAD_ARTIFACT_BY_INTERFACE_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId(), interfaceName, operationName);

			HttpPost httpPost = createPostAddArtifactRequeast(userBodyJson, url, true);
			response = httpclient.execute(httpPost);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 200, returned :" + status, status, 200);

			// get artifact uniqueId
			String artifactId = getLifecycleArtifactUid(response);

			// delete artifact
			url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_BY_INTERFACE_TO_RESOURCE, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetailsVFCcomp.getUniqueId(), interfaceName, operationName,
					artifactId);
			HttpDelete httpDelete = createDeleteArtifactRequest(url);

			response = httpclient.execute(httpDelete);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 200, returned :" + status, status, 200);
		} finally {
			httpclient.close();
		}

	}

	@Test
	public void addArtifactToServiceTest() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			String jsonStr = createLoadArtifactBody();

			String url = String.format(Urls.ADD_ARTIFACT_TO_SERVICE, config.getCatalogBeHost(),
					config.getCatalogBePort(), defaultService1.getUniqueId());
			HttpPost httpPost = createPostAddArtifactRequeast(jsonStr, url, true);
			CloseableHttpResponse result = httpclient.execute(httpPost);
			int status = result.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 200, returned :" + status, 200, status);

			String artifactId = getArtifactUid(result);

			url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_SERVICE, config.getCatalogBeHost(),
					config.getCatalogBePort(), defaultService1.getUniqueId(), artifactId);
			HttpDelete httpDelete = createDeleteArtifactRequest(url);

			result = httpclient.execute(httpDelete);
			status = result.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 200, returned :" + status, 200, status);
		} finally {
			RestResponse response = ServiceRestUtils.deleteService(defaultService1.getName(), serviceVersion,
					sdncUserDetails);
			checkDeleteResponse(response);
			httpclient.close();
		}
	}

	@Test
	public void addArtifactNotSupportedTypeToServiceTest() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			Map<String, Object> json = new HashMap<String, Object>();
			json.put("artifactName", "install_apache.sh");
			json.put("artifactType", "SHELL11");
			json.put("description", "fff");
			json.put("payloadData", "UEsDBAoAAAAIAAeLb0bDQz");
			json.put("artifactLabel", "name123");

			String jsonStr = gson.toJson(json);

			String url = String.format(Urls.ADD_ARTIFACT_TO_SERVICE, config.getCatalogBeHost(),
					config.getCatalogBePort(), defaultService1.getUniqueId());

			HttpPost httpPost = createPostAddArtifactRequeast(jsonStr, url, true);
			CloseableHttpResponse result = httpclient.execute(httpPost);
			int status = result.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("response code is not 400, returned :" + status, 400, status);

			ErrorInfo errorInfo = ErrorValidationUtils
					.parseErrorConfigYaml(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name());

			String responseString = EntityUtils.toString(result.getEntity());

			JSONObject map = (JSONObject) jsonParser.parse(responseString);
			JSONObject requestError = (JSONObject) map.get("requestError");
			JSONObject serviceException = (JSONObject) requestError.get("serviceException");

			String msgId = (String) serviceException.get("messageId");
			AssertJUnit.assertEquals("message id did not match expacted", errorInfo.getMessageId(), msgId);

			String text = (String) serviceException.get("text");
			AssertJUnit.assertEquals("text did not match expacted", errorInfo.getMessage(), text);

			JSONArray variables = (JSONArray) serviceException.get("variables");
			String type = (String) variables.get(0);
			AssertJUnit.assertEquals("variable did not match expacted", "SHELL11", type);
		} finally {
			RestResponse response = ServiceRestUtils.deleteService(defaultService1.getName(), serviceVersion,
					sdncUserDetails);
			checkDeleteResponse(response);
			httpclient.close();
		}

	}

	@Test
	public void addArtifactToResourceTest() throws Exception {

		ArtifactReqDetails defaultArtifact = ElementFactory.getDefaultArtifact();

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(defaultArtifact, sdncUserDetails,
				resourceDetailsVFCcomp.getUniqueId());
		int status = response.getErrorCode();
		AssertJUnit.assertEquals("add informational artifact request returned status: " + response.getErrorCode(), 200,
				status);

		RestResponse resourceResp = ResourceRestUtils.getResource(resourceDetailsVFCcomp.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
		AssertJUnit.assertNotNull(resource);

		Map<String, ArtifactDefinition> artifacts = resource.getArtifacts();
		boolean isExist = false;
		for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
			if (entry.getKey().equals(defaultArtifact.getArtifactLabel())) {
				isExist = true;

			}
		}
		AssertJUnit.assertTrue(isExist);
	}
}
