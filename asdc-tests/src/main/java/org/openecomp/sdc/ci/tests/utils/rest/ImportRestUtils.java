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

package org.openecomp.sdc.ci.tests.utils.rest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONException;
import org.openecomp.sdc.be.dao.rest.HttpRestClient;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.ImportTestTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.rest.api.RestResponseAsByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportRestUtils extends BaseRestUtils {

	private static Logger log = LoggerFactory.getLogger(ImportRestUtils.class.getName());
	private static Properties downloadCsarHeaders = new Properties();

	static {
		downloadCsarHeaders.put("Accept", "application/octet-stream");
	}

	@SuppressWarnings("unused")
	private static Integer importNormativeResource(NormativeTypesEnum resource, UserRoleEnum userRole)
			throws IOException {
		Config config = Utils.getConfig();
		CloseableHttpResponse response = null;
		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("resourceZip", new FileBody(getTestZipFile(resource.getFolderName())));
		mpBuilder.addPart("resourceMetadata",
				new StringBody(getTestJsonStringOfFile(resource.getFolderName(), resource.getFolderName() + ".json"),
						ContentType.APPLICATION_JSON));

		String url = String.format(Urls.IMPORT_RESOURCE_NORMATIVE, config.getCatalogBeHost(),
				config.getCatalogBePort());

		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("USER_ID", userRole.getUserId());
			httpPost.setEntity(mpBuilder.build());
			response = client.execute(httpPost);
			return response.getStatusLine().getStatusCode();
		} finally {
			closeResponse(response);
			closeHttpClient(client);

		}
	}

	/*
	 * public static RestResponse importResourceByName(String resourceName, User
	 * user) throws IOException { Config config = Utils.getConfig();
	 * CloseableHttpResponse response = null; MultipartEntityBuilder mpBuilder =
	 * MultipartEntityBuilder.create();
	 * 
	 * mpBuilder.addPart("resourceZip", new
	 * FileBody(getTestZipFile(resourceName)));
	 * mpBuilder.addPart("resourceMetadata", new
	 * StringBody(getTestJsonStringOfFile(resourceName, resourceName + ".json"),
	 * ContentType.APPLICATION_JSON));
	 * 
	 * String url = String.format(Urls.IMPORT_RESOURCE_NORMATIVE,
	 * config.getCatalogBeHost(), config.getCatalogBePort());
	 * 
	 * CloseableHttpClient client = HttpClients.createDefault(); try { HttpPost
	 * httpPost = new HttpPost(url); RestResponse restResponse = new
	 * RestResponse(); httpPost.addHeader("USER_ID", user.getUserId());
	 * httpPost.setEntity(mpBuilder.build()); response =
	 * client.execute(httpPost); HttpEntity entity = response.getEntity();
	 * String responseBody = null; if (entity != null) { InputStream instream =
	 * entity.getContent(); StringWriter writer = new StringWriter();
	 * IOUtils.copy(instream, writer); responseBody = writer.toString(); try {
	 * 
	 * } finally { instream.close(); } }
	 * 
	 * restResponse.setErrorCode(response.getStatusLine().getStatusCode());
	 * restResponse.setResponse(responseBody); if (restResponse.getErrorCode()
	 * == STATUS_CODE_CREATED ){
	 * 
	 * }
	 * 
	 * return restResponse;
	 * 
	 * } finally { closeResponse(response); closeHttpClient(client);
	 * 
	 * }
	 * 
	 * }
	 */

	public static RestResponse importResourceByName(ResourceReqDetails resourceDetails, User importer)
			throws Exception {
		Config config = Utils.getConfig();
		CloseableHttpResponse response = null;
		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("resourceZip", new FileBody(getTestZipFile(resourceDetails.getName())));
		mpBuilder.addPart("resourceMetadata",
				new StringBody(getTestJsonStringOfFile(resourceDetails.getName(), resourceDetails.getName() + ".json"),
						ContentType.APPLICATION_JSON));

		String url = String.format(Urls.IMPORT_RESOURCE_NORMATIVE, config.getCatalogBeHost(),
				config.getCatalogBePort());

		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			RestResponse restResponse = new RestResponse();
			httpPost.addHeader("USER_ID", importer.getUserId());
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

			if (restResponse.getErrorCode() == STATUS_CODE_CREATED) {
				resourceDetails.setUUID(ResponseParser.getUuidFromResponse(restResponse));
				resourceDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(restResponse));
				resourceDetails.setVersion(ResponseParser.getVersionFromResponse(restResponse));
				resourceDetails.setCreatorUserId(importer.getUserId());
				resourceDetails.setCreatorFullName(importer.getFullName());
			}

			return restResponse;

		} finally {
			closeResponse(response);
			closeHttpClient(client);

		}

	}

	public static RestResponse importNewResourceByName(String resourceName, UserRoleEnum userRole) throws IOException {
		Config config = Utils.getConfig();

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("resourceZip", new FileBody(getTestZipFile(resourceName)));
		mpBuilder.addPart("resourceMetadata", new StringBody(
				getTestJsonStringOfFile(resourceName, resourceName + ".json"), ContentType.APPLICATION_JSON));
		HttpEntity requestEntity = mpBuilder.build();
		String url = String.format(Urls.IMPORT_USER_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("USER_ID", userRole.getUserId());

		return HttpRequest.sendHttpPostWithEntity(requestEntity, url, headers);
	}

	public static RestResponse importNormativeResourceByName(String resourceName, UserRoleEnum userRole)
			throws IOException {
		Config config = Utils.getConfig();

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("resourceZip", new FileBody(getTestZipFile(resourceName)));
		mpBuilder.addPart("resourceMetadata", new StringBody(
				getTestJsonStringOfFile(resourceName, resourceName + ".json"), ContentType.APPLICATION_JSON));
		HttpEntity requestEntity = mpBuilder.build();
		String url = String.format(Urls.IMPORT_RESOURCE_NORMATIVE, config.getCatalogBeHost(),
				config.getCatalogBePort());
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("USER_ID", userRole.getUserId());

		return HttpRequest.sendHttpPostWithEntity(requestEntity, url, headers);
	}

	public static RestResponse importTestResource(ImportTestTypesEnum resource, UserRoleEnum userRole)
			throws IOException {
		Config config = Utils.getConfig();
		CloseableHttpResponse response = null;
		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("resourceZip", new FileBody(getTestZipFile(resource.getFolderName())));
		mpBuilder.addPart("resourceMetadata",
				new StringBody(getTestJsonStringOfFile(resource.getFolderName(), resource.getFolderName() + ".json"),
						ContentType.APPLICATION_JSON));

		String url = String.format(Urls.IMPORT_RESOURCE_NORMATIVE, config.getCatalogBeHost(),
				config.getCatalogBePort());

		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			RestResponse restResponse = new RestResponse();
			httpPost.addHeader("USER_ID", UserRoleEnum.ADMIN.getUserId());
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
			// restResponse.setResponse(response.getEntity().toString());
			restResponse.setResponse(responseBody);
			return restResponse;
		} finally {
			closeResponse(response);
			closeHttpClient(client);

		}
	}

	public static Boolean removeNormativeTypeResource(NormativeTypesEnum current)
			throws FileNotFoundException, IOException, ClientProtocolException {
		User user = new User(UserRoleEnum.ADMIN.getFirstName(), UserRoleEnum.ADMIN.getLastName(),
				UserRoleEnum.ADMIN.getUserId(), null, null, null);
		RestResponse deleteResponse = ResourceRestUtils.deleteResourceByNameAndVersion(user, current.getNormativeName(),
				"1.0");
		if (deleteResponse.getErrorCode() == 200) {
			return true;
		}
		return false;
	}

	public static void validateImportTestTypesResp(ImportTestTypesEnum currResource, RestResponse restResponse)
			throws IOException, JSONException {

		// assertTrue( status != ResourceUtils.STATUS_CODE_IMPORT_SUCCESS );

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(currResource.getActionStatus().name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual
		List<String> variables = (currResource.getErrorParams() != null ? currResource.getErrorParams()
				: new ArrayList<String>());
		if (restResponse.getErrorCode() != 200) {
			ErrorValidationUtils.checkBodyResponseOnError(currResource.getActionStatus().name(), variables,
					restResponse.getResponse());
		}
	}

	private static String getTestJsonStringOfFile(String folderName, String fileName) throws IOException {
		// String sourceDir = "src/test/resources/CI/importResourceTests";
		Config config = Utils.getConfig();
		String sourceDir = config.getImportResourceTestsConfigDir();
		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir + File.separator + folderName,
				fileName);
		byte[] fileContent = Files.readAllBytes(filePath);
		String content = new String(fileContent);
		return content;
	}

	private static File getTestZipFile(String elementName) throws IOException {
		Config config = Utils.getConfig();
		String sourceDir = config.getImportResourceTestsConfigDir();
		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir + File.separator + elementName,
				"normative-types-new-" + elementName + ".zip");
		return filePath.toFile();
	}

	private static void closeHttpClient(CloseableHttpClient client) {
		try {
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			log.debug("failed to close client or response: ", e);
		}
	}

	private static void closeResponse(CloseableHttpResponse response) {
		try {
			if (response != null) {
				response.close();
			}
		} catch (IOException e) {
			log.debug("failed to close client or response: {}", e);
		}
	}

	public static RestResponseAsByteArray getCsar(String csarUid, User sdncModifierDetails) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_CSAR_USING_SIMULATOR, config.getCatalogBeHost(), config.getCatalogBePort(),
				csarUid);

		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRestClient httpRestClient = new HttpRestClient();

		for (Map.Entry<String, String> mapEntry : headersMap.entrySet()) {

			downloadCsarHeaders.put(mapEntry.getKey(), mapEntry.getValue());
		}
		RestResponseAsByteArray doGetAsByteArray = httpRestClient.doGetAsByteArray(url, downloadCsarHeaders);
		// RestResponse getCsar = http.httpSendGet(url, headersMap);

		return doGetAsByteArray;

	}

	private static File getGroupTypeZipFile(String elementName) throws IOException {
		Config config = Utils.getConfig();
		String sourceDir = config.getImportResourceTestsConfigDir();
		sourceDir += File.separator + ".." + File.separator + "importTypesTest" + File.separator;
		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir + File.separator + elementName,
				elementName + ".zip");
		return filePath.toFile();
	}

	public static RestResponse importNewGroupTypeByName(String groupTypeName, UserRoleEnum userRole)
			throws IOException {
		Config config = Utils.getConfig();

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("groupTypesZip", new FileBody(getGroupTypeZipFile(groupTypeName)));
		HttpEntity requestEntity = mpBuilder.build();
		String url = String.format(Urls.IMPORT_GROUP_TYPE, config.getCatalogBeHost(), config.getCatalogBePort());
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("USER_ID", userRole.getUserId());

		return HttpRequest.sendHttpPostWithEntity(requestEntity, url, headers);
	}

}
