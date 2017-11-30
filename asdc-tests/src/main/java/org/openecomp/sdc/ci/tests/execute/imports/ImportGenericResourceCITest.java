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

package org.openecomp.sdc.ci.tests.execute.imports;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.ImportTestTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.RespJsonKeysEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.TODO.ImportCapabilityTypeCITest;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ImportRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import fj.data.Either;

public class ImportGenericResourceCITest extends ComponentBaseTest {
	private static Logger log = LoggerFactory.getLogger(ImportGenericResourceCITest.class.getName());
	private static final String FILE_NAME_MY_COMPUTE = "tosca.nodes.MyCompute";
	private static final String RESOURCE_NAME_UPDATE_COMPUTE = "userUpdateCompute";
	private static final String RESOURCE_NAME_MY_COMPUTE = "MyCompute";
	private static final String RESOURCE_NAME_USER_COMPUTE = "userCompute";
	private static final String FILE_NAME_USER_COMPUTE = "tosca.nodes.userCompute";
	private static final String FILE_NAME_USER_VFC = "Derived_VFC";
	@Rule
	public static TestName name = new TestName();

	public ImportGenericResourceCITest() {
		super(name, ImportGenericResourceCITest.class.getName());
	}

	@BeforeClass
	public static void beforeImportClass() throws IOException {
		ImportCapabilityTypeCITest.importAllCapabilityTypes();
		// removeAllNormativeTypeResources();
		// importAllNormativeTypesResources(UserRoleEnum.ADMIN);
	}

	static Config config = Config.instance();

	public static Map<NormativeTypesEnum, Boolean> removeAllNormativeTypeResources() throws ClientProtocolException, IOException {
		Map<NormativeTypesEnum, Boolean> normativeExistInDB = new HashMap<>();

		for (NormativeTypesEnum current : NormativeTypesEnum.values()) {
			Boolean existedBeforeDelete = ImportRestUtils.removeNormativeTypeResource(current);
			normativeExistInDB.put(current, existedBeforeDelete);
		}
		return normativeExistInDB;
	}

	public static Either<String, Boolean> getNormativeTypeResource(NormativeTypesEnum current) throws ClientProtocolException, IOException {
		return getResource(current.getNormativeName(), "1.0");
	}

	@Test
	public void importAllTestResources() throws Exception {
		for (ImportTestTypesEnum currResource : ImportTestTypesEnum.values()) {
			DbUtils.cleanAllAudits();

			RestResponse importResponse = ImportRestUtils.importTestResource(currResource, UserRoleEnum.ADMIN);
			// System.err.println("import Resource
			// "+"<"+currResource+">"+"response:
			// "+importResponse.getErrorCode());
			ImportRestUtils.validateImportTestTypesResp(currResource, importResponse);
			if (currResource.getvalidateAudit() == true) {
				// validate audit
				String baseVersion = "1.0";
				ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(currResource.getActionStatus().name());
				ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
				String auditAction = "ResourceImport";
				expectedResourceAuditJavaObject.setAction(auditAction);
				expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.ADMIN.getUserId());
				expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.ADMIN.getUserName());
				expectedResourceAuditJavaObject.setResourceName(currResource.getNormativeName());
				expectedResourceAuditJavaObject.setResourceType("Resource");
				expectedResourceAuditJavaObject.setPrevVersion("");
				expectedResourceAuditJavaObject.setCurrVersion(baseVersion);
				expectedResourceAuditJavaObject.setPrevState("");
				expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.toString());
				expectedResourceAuditJavaObject.setComment(null);
				expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
				List<String> variables = (currResource.getErrorParams() != null ? currResource.getErrorParams() : new ArrayList<String>());
				String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
				expectedResourceAuditJavaObject.setDesc(auditDesc);
				AuditValidationUtils.validateAuditImport(expectedResourceAuditJavaObject, auditAction);
			}
		}
	}

	// -----------------------------------------------------------------------------------
	protected void validateMyComputeCapabilities(Map<String, Object> map) {
		assertTrue(map.containsKey("capabilities"));
		Map<String, Object> capabilities = (Map<String, Object>) map.get("capabilities");
		assertTrue(capabilities.containsKey("tosca.capabilities.Container"));
		List<Object> hostCapList = (List<Object>) capabilities.get("tosca.capabilities.Container");
		assertFalse(hostCapList.isEmpty());
		Map<String, Object> hostCap = (Map<String, Object>) hostCapList.get(0);
		validateField(hostCap, "type", "tosca.capabilities.Container");
		validateField(hostCap, "name", "host");
		validateField(hostCap, "validSourceTypes", Arrays.asList(new String[] { "tosca.nodes.SoftwareComponent" }));

		assertTrue(capabilities.containsKey("tosca.capabilities.Endpoint.Admin"));
		List<Object> endPointCapList = (List<Object>) capabilities.get("tosca.capabilities.Endpoint.Admin");
		assertFalse(endPointCapList.isEmpty());
		Map<String, Object> endPointCap = (Map<String, Object>) endPointCapList.get(0);
		validateField(endPointCap, "name", "endpoint");
		validateField(endPointCap, "type", "tosca.capabilities.Endpoint.Admin");

		assertTrue(capabilities.containsKey("tosca.capabilities.OperatingSystem"));
		List<Object> osCapList = (List<Object>) capabilities.get("tosca.capabilities.OperatingSystem");
		assertFalse(osCapList.isEmpty());
		Map<String, Object> osCap = (Map<String, Object>) osCapList.get(0);
		validateField(osCap, "name", "os");
		validateField(osCap, "type", "tosca.capabilities.OperatingSystem");

		assertTrue(capabilities.containsKey("tosca.capabilities.Scalable"));
		List<Object> scalableCapList = (List<Object>) capabilities.get("tosca.capabilities.Scalable");
		assertFalse(scalableCapList.isEmpty());
		Map<String, Object> scalableCap = (Map<String, Object>) scalableCapList.get(0);
		validateField(scalableCap, "name", "scalable");
		validateField(scalableCap, "type", "tosca.capabilities.Scalable");

		assertTrue(capabilities.containsKey("tosca.capabilities.network.Bindable"));
		List<Object> bindingCapList = (List<Object>) capabilities.get("tosca.capabilities.network.Bindable");
		assertFalse(bindingCapList.isEmpty());
		Map<String, Object> bindingCap = (Map<String, Object>) bindingCapList.get(0);
		validateField(bindingCap, "name", "binding");
		validateField(bindingCap, "type", "tosca.capabilities.network.Bindable");

	}

	protected void validateMyComputeResource(String resourceName, String resourceVersion, String expectedState) throws ClientProtocolException, IOException {
		Either<String, Boolean> eitherMyCompute = getResource(resourceName, resourceVersion);
		assertTrue(eitherMyCompute.isLeft());
		String testComputeYml = eitherMyCompute.left().value();

		Map<String, Object> map = new HashMap<String, Object>();
		map = (Map<String, Object>) new Gson().fromJson(testComputeYml, map.getClass());

		validateMyComputeBasicFields(map, resourceName, resourceVersion, expectedState);

		validateMyComputeCapabilities(map);

		validateMyComputeRequirements(map);
		validateField(map, RespJsonKeysEnum.RESOURCE_VERSION.getRespJsonKeyName(), resourceVersion);

	}

	protected void validateMyComputeResource(String uid, String resourceName, String resourceVersion, String expectedState) throws ClientProtocolException, IOException {
		RestResponse resourceResponse = ResourceRestUtils.getResource(uid);
		ResourceRestUtils.checkSuccess(resourceResponse);
		String testComputeYml = resourceResponse.getResponse();

		// Either<String, Boolean> eitherMyCompute = getResource(resourceName,
		// resourceVersion);
		// assertTrue( eitherMyCompute.isLeft() );
		// String testComputeYml = eitherMyCompute.left().value();

		Map<String, Object> map = new HashMap<String, Object>();
		map = (Map<String, Object>) new Gson().fromJson(testComputeYml, map.getClass());

		validateMyComputeBasicFields(map, resourceName, resourceVersion, expectedState);

		validateMyComputeCapabilities(map);

		validateMyComputeRequirements(map);
		validateField(map, RespJsonKeysEnum.RESOURCE_VERSION.getRespJsonKeyName(), resourceVersion);

	}

	protected void validateMyComputeResourceAfterUpdate(String uid, String resourceName, String resourceVersion, String expectedState) throws ClientProtocolException, IOException {
		RestResponse resourceResponse = ResourceRestUtils.getResource(uid);
		ResourceRestUtils.checkSuccess(resourceResponse);
		String testComputeYml = resourceResponse.getResponse();

		// Either<String, Boolean> eitherMyCompute = getResource(resourceName,
		// resourceVersion);
		// assertTrue( eitherMyCompute.isLeft() );

		// String testComputeYml = eitherMyCompute.left().value();

		Map<String, Object> map = new HashMap<String, Object>();
		map = (Map<String, Object>) new Gson().fromJson(testComputeYml, map.getClass());

		validateMyComputeBasicFields(map, resourceName, resourceVersion, expectedState);
		validateField(map, RespJsonKeysEnum.DESCRIPTION.getRespJsonKeyName(), "Short description");
		validateField(map, RespJsonKeysEnum.VENDOR_NAME.getRespJsonKeyName(), "UserVendor");
		validateField(map, RespJsonKeysEnum.VENDOR_RELEASE.getRespJsonKeyName(), "1.1.2");

		// validateMyComputeCapabilities(map);
		// AssertJUnit.assertTrue(map.containsKey("capabilities"));
		// Map<String, Object> capabilities = (Map<String, Object>)
		// map.get("capabilities");
		// AssertJUnit.assertTrue(capabilities.containsKey("host"));
		// Map<String, Object> hostCap = (Map<String, Object>)
		// capabilities.get("host");
		// validateField(hostCap, "type", "tosca.capabilities.Container");
		// validateField(hostCap, "validSourceTypes", Arrays.asList(new
		// String[]{"tosca.nodes.SoftwareComponent"}));
		//
		// AssertJUnit.assertTrue(capabilities.containsKey("endpoint"));
		// Map<String, Object> endPointCap = (Map<String, Object>)
		// capabilities.get("endpoint");
		// validateField(endPointCap, "type",
		// "tosca.capabilities.Endpoint.Admin");

		assertTrue(map.containsKey("capabilities"));
		Map<String, Object> capabilities = (Map<String, Object>) map.get("capabilities");
		assertTrue(capabilities.containsKey("tosca.capabilities.Container"));
		List<Object> hostCapList = (List<Object>) capabilities.get("tosca.capabilities.Container");
		assertFalse(hostCapList.isEmpty());
		Map<String, Object> hostCap = (Map<String, Object>) hostCapList.get(0);
		validateField(hostCap, "type", "tosca.capabilities.Container");
		validateField(hostCap, "name", "host");
		validateField(hostCap, "validSourceTypes", Arrays.asList(new String[] { "tosca.nodes.SoftwareComponent" }));

		assertTrue(capabilities.containsKey("tosca.capabilities.Endpoint.Admin"));
		List<Object> endPointCapList = (List<Object>) capabilities.get("tosca.capabilities.Endpoint.Admin");
		assertFalse(endPointCapList.isEmpty());
		Map<String, Object> endPointCap = (Map<String, Object>) endPointCapList.get(0);
		validateField(endPointCap, "name", "endpoint");
		validateField(endPointCap, "type", "tosca.capabilities.Endpoint.Admin");

		validateMyComputeRequirements(map);
		validateField(map, RespJsonKeysEnum.RESOURCE_VERSION.getRespJsonKeyName(), resourceVersion);

	}

	protected void validateMyComputeRequirements(Map<String, Object> map) {
		assertTrue(map.containsKey("requirements"));
		Map<String, Object> requirements = (Map<String, Object>) map.get("requirements");

		assertTrue(requirements.containsKey("tosca.capabilities.Attachment"));
		List<Object> localStorageReqList = (List<Object>) requirements.get("tosca.capabilities.Attachment");
		assertFalse(localStorageReqList.isEmpty());
		Map<String, Object> localStorageReq = (Map<String, Object>) localStorageReqList.get(0);
		validateField(localStorageReq, "capability", "tosca.capabilities.Attachment");
		validateField(localStorageReq, "node", "tosca.nodes.BlockStorage");
		validateField(localStorageReq, "relationship", "tosca.relationships.AttachesTo");
		validateField(localStorageReq, "name", "local_storage");
	}

	protected void validateMyComputeBasicFields(Map<String, Object> map, String resourceName, String resourceVersion, String expectedState) {
		validateField(map, RespJsonKeysEnum.IS_ABSTRACT.getRespJsonKeyName(), false);
		// validateField(map, RespJsonKeysEnum.CATEGORIES.getRespJsonKeyName(),
		// categoryDefinition);
		// validateField(map, RespJsonKeysEnum.UNIQUE_ID.getRespJsonKeyName(),
		// UniqueIdBuilder.buildResourceUniqueId(resourceName,
		// resourceVersion));
		validateField(map, RespJsonKeysEnum.RESOURCE_NAME.getRespJsonKeyName(), resourceName);
		validateField(map, RespJsonKeysEnum.TAGS.getRespJsonKeyName(), Arrays.asList(new String[] { resourceName }));
		validateField(map, RespJsonKeysEnum.LIFE_CYCLE_STATE.getRespJsonKeyName(), expectedState);

		validateField(map, RespJsonKeysEnum.DERIVED_FROM.getRespJsonKeyName(), Arrays.asList(new String[] { "tosca.nodes.Root" }));
	}

	protected static void validateField(Map<String, Object> map, String jsonField, Object expectedValue) {
		if (expectedValue == null) {
			assertTrue(!map.containsKey(jsonField));
		} else {
			assertTrue("map does not contain field " + jsonField, map.containsKey(jsonField));
			Object foundValue = map.get(jsonField);
			compareElements(expectedValue, foundValue);
		}
	}

	protected static void compareElements(Object expectedValue, Object foundValue) {
		if (expectedValue instanceof String) {
			assertTrue(foundValue instanceof String);
			assertTrue(foundValue.equals(expectedValue));
		}

		else if (expectedValue instanceof Boolean) {
			assertTrue(foundValue instanceof Boolean);
			assertTrue(foundValue == expectedValue);
		} else if (expectedValue instanceof Map) {
			assertTrue(foundValue instanceof Map);
			Map<String, Object> foundMap = (Map<String, Object>) foundValue;
			Map<String, Object> excpectedMap = (Map<String, Object>) expectedValue;
			assertTrue(foundMap.size() == excpectedMap.size());
			Iterator<String> foundkeyItr = foundMap.keySet().iterator();
			while (foundkeyItr.hasNext()) {
				String foundKey = foundkeyItr.next();
				assertTrue(excpectedMap.containsKey(foundKey));
				compareElements(excpectedMap.get(foundKey), foundMap.get(foundKey));
			}

		} else if (expectedValue instanceof List) {
			assertTrue(foundValue instanceof List);
			List<Object> foundList = (List<Object>) foundValue;
			List<Object> excpectedList = (List<Object>) expectedValue;
			assertTrue(foundList.size() == excpectedList.size());
			for (int i = 0; i < foundList.size(); i++) {
				compareElements(excpectedList.get(i), foundList.get(i));
			}

		} else if (expectedValue instanceof CategoryDefinition) {
			assertTrue(foundValue instanceof Map);
			CategoryDefinition expCat = (CategoryDefinition) expectedValue;
			Map<String, Object> actCat = (Map<String, Object>) foundValue;
			assertEquals(expCat.getName(), actCat.get("name"));

			// assertEquals(expCat.getSubcategories().get(0).getName(),
			// actCat.get("subcategories").getName());
		} else {
			assertTrue(foundValue.equals(expectedValue));
		}
	}

	public static void restoreToOriginalState(Map<NormativeTypesEnum, Boolean> originalState, UserRoleEnum userRole) throws IOException {
		removeAllNormativeTypeResources();

		Iterator<Entry<NormativeTypesEnum, Boolean>> iterator = originalState.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<NormativeTypesEnum, Boolean> entry = iterator.next();
			Boolean isExistBeforeDelete = entry.getValue();
			if (isExistBeforeDelete) {
				importNormativeResource(entry.getKey(), userRole);
			}
		}

	}

	public static void importAllNormativeTypesResources(UserRoleEnum userRole) throws IOException {
		for (NormativeTypesEnum currResource : NormativeTypesEnum.values()) {
			Either<String, Boolean> resource = getResource(currResource.getNormativeName(), "1.0");
			if (resource.isRight()) {
				importNormativeResource(currResource, userRole);
			}
		}

	}

	protected static Integer importNormativeResource(NormativeTypesEnum resource, UserRoleEnum userRole) throws IOException {
		return importResource(resource.getFolderName(), userRole, true);
	}

	protected static Integer importResource(String folderName, UserRoleEnum userRole, boolean isNormative) throws IOException {
		Config config = Utils.getConfig();
		CloseableHttpResponse response = null;
		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("resourceZip", new FileBody(getZipFile(folderName)));
		mpBuilder.addPart("resourceMetadata", new StringBody(getJsonStringOfFile(folderName, folderName + ".json"), ContentType.APPLICATION_JSON));

		String url = String.format(Urls.IMPORT_RESOURCE_NORMATIVE, config.getCatalogBeHost(), config.getCatalogBePort());
		if (!isNormative) {
			url = String.format(Urls.IMPORT_USER_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());
		}

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

	public static void closeHttpClient(CloseableHttpClient client) {
		try {
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			log.debug("failed to close client or response: ", e);
		}
	}

	public static void closeResponse(CloseableHttpResponse response) {
		try {
			if (response != null) {
				response.close();
			}
		} catch (IOException e) {
			log.debug("failed to close client or response: ", e);
		}
	}

	protected static String getJsonStringOfFile(String folderName, String fileName) throws IOException {
		String sourceDir = config.getImportResourceConfigDir();
		sourceDir += File.separator + "normative-types";

		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir + File.separator + folderName, fileName);
		byte[] fileContent = Files.readAllBytes(filePath);
		String content = new String(fileContent);
		return content;
	}

	protected static File getZipFile(String elementName) throws IOException {
		String sourceDir = config.getImportResourceConfigDir();
		sourceDir += File.separator + "normative-types";

		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir + File.separator + elementName, "normative-types-new-" + elementName + ".zip");
		return filePath.toFile();
	}

	protected static String getTestJsonStringOfFile(String folderName, String fileName) throws IOException {
		String sourceDir = config.getImportResourceTestsConfigDir();
		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir + File.separator + folderName, fileName);
		byte[] fileContent = Files.readAllBytes(filePath);
		String content = new String(fileContent);
		return content;
	}

	protected static File getTestZipFile(String elementName) throws IOException {
		String sourceDir = config.getImportResourceTestsConfigDir();

		java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir + File.separator + elementName, "normative-types-new-" + elementName + ".zip");
		return filePath.toFile();
	}

	protected static Either<String, Boolean> getResource(String name, String version) throws IOException {
		RestResponse resource = ResourceRestUtils.getResourceByNameAndVersion(UserRoleEnum.DESIGNER.getUserId(), name, version);
		if (resource.getErrorCode() == ImportRestUtils.STATUS_CODE_GET_SUCCESS) {
			return Either.left(resource.getResponse());
			// return Either.right(true);

		}
		return Either.right(false);
	}

	@Test
	public void testImportWithRequirmentsAndCapabilities() throws IOException {
		String fileName = FILE_NAME_MY_COMPUTE;
		RestResponse response = ImportRestUtils.importNormativeResourceByName(RESOURCE_NAME_MY_COMPUTE, UserRoleEnum.ADMIN);
		Integer statusCode = response.getErrorCode();
		assertTrue(statusCode == ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS);
		String uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "1.0", "CERTIFIED");
	}

	@Test
	public void testImportWithUpdateNormativeType() throws IOException {
		String fileName = FILE_NAME_MY_COMPUTE;
		RestResponse response = ImportRestUtils.importNormativeResourceByName(RESOURCE_NAME_MY_COMPUTE, UserRoleEnum.ADMIN);
		Integer statusCode = response.getErrorCode();
		assertTrue(String.format("Expected code %s and got code %s",ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS,statusCode),statusCode == ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS);
		String uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "1.0", "CERTIFIED");

		// update
		response = ImportRestUtils.importNormativeResourceByName(RESOURCE_NAME_MY_COMPUTE, UserRoleEnum.ADMIN);
		statusCode = response.getErrorCode();
		assertTrue(statusCode == ImportRestUtils.STATUS_CODE_UPDATE_SUCCESS);
		uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "2.0", "CERTIFIED");

	}

	@Test
	public void testImportWithInvalidDefaultValue() throws IOException {
		RestResponse response = ImportRestUtils.importNewResourceByName("portInvalidDefaultValue", UserRoleEnum.DESIGNER);
		assertTrue(response.getErrorCode() == HttpStatus.SC_BAD_REQUEST);
	}

	@Test
	public void testImportUserResource() throws IOException {
		String fileName = FILE_NAME_USER_COMPUTE;
		RestResponse response = ImportRestUtils.importNewResourceByName(RESOURCE_NAME_USER_COMPUTE, UserRoleEnum.DESIGNER);
		Integer statusCode = response.getErrorCode();
		assertTrue(statusCode == ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS);
		String uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "0.1", "NOT_CERTIFIED_CHECKOUT");

	}

	@Test
	public void testImportAndUpdateUserResource() throws IOException {
		String fileName = FILE_NAME_USER_COMPUTE;
		RestResponse response = ImportRestUtils.importNewResourceByName(RESOURCE_NAME_USER_COMPUTE, UserRoleEnum.DESIGNER);
		Integer statusCode = response.getErrorCode();
		assertTrue(statusCode == ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS);
		String uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "0.1", "NOT_CERTIFIED_CHECKOUT");
		response = ImportRestUtils.importNewResourceByName(RESOURCE_NAME_UPDATE_COMPUTE, UserRoleEnum.DESIGNER);
		statusCode = response.getErrorCode();
		assertTrue(statusCode == ImportRestUtils.STATUS_CODE_UPDATE_SUCCESS);
		uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResourceAfterUpdate(uid, fileName, "0.1", "NOT_CERTIFIED_CHECKOUT");

	}

	@Test
	public void testImportAndUpdateChangesUserResource() throws IOException {
		String fileName = FILE_NAME_USER_COMPUTE;
		RestResponse response = ImportRestUtils.importNewResourceByName(RESOURCE_NAME_USER_COMPUTE, UserRoleEnum.DESIGNER);
		Integer statusCode = response.getErrorCode();
		assertTrue(statusCode == ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS);
		String uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "0.1", "NOT_CERTIFIED_CHECKOUT");
		// Either<String, Boolean> resource = getResource(fileName, "0.1");
		// assertTrue(resource.isLeft());

		response = ImportRestUtils.importNewResourceByName(RESOURCE_NAME_UPDATE_COMPUTE, UserRoleEnum.DESIGNER);
		statusCode = response.getErrorCode();
		assertTrue(statusCode == ImportRestUtils.STATUS_CODE_UPDATE_SUCCESS);
		validateMyComputeResourceAfterUpdate(uid, fileName, "0.1", "NOT_CERTIFIED_CHECKOUT");

	}

	@Test
	public void testImportCheckoutAndUpdateUserResource() throws IOException {
		String fileName = FILE_NAME_USER_COMPUTE;
		RestResponse response = ImportRestUtils.importNormativeResourceByName(RESOURCE_NAME_USER_COMPUTE, UserRoleEnum.ADMIN);
		Integer statusCode = response.getErrorCode();
		assertTrue(String.format("Expected code %s and got code %s",ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS,statusCode),statusCode == ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS);
		String uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "1.0", "CERTIFIED");

		response = ImportRestUtils.importNewResourceByName(RESOURCE_NAME_USER_COMPUTE, UserRoleEnum.DESIGNER);
		statusCode = response.getErrorCode();
		assertEquals("check response code after update resource", ImportRestUtils.STATUS_CODE_UPDATE_SUCCESS, statusCode.intValue());
		uid = ResponseParser.getUniqueIdFromResponse(response);
		validateMyComputeResource(uid, fileName, "1.1", "NOT_CERTIFIED_CHECKOUT");

	}

	@Test
	public void importNormativeTypesTesterUserRole() throws Exception {
		Integer statusCode = ImportRestUtils.importNormativeResourceByName(RESOURCE_NAME_MY_COMPUTE, UserRoleEnum.TESTER).getErrorCode();
		assertTrue(statusCode == ImportRestUtils.RESTRICTED_OPERATION);
	}

	@Test
	public void importNormativeTypesDesignerUserRole() throws Exception {
		Integer statusCode = ImportRestUtils.importNormativeResourceByName(RESOURCE_NAME_MY_COMPUTE, UserRoleEnum.DESIGNER).getErrorCode();
		assertTrue(statusCode == 409);
	}
	
	@Test
	public void testImportVFCDerivedFromGeneric() throws IOException {
	
		RestResponse response = ImportRestUtils.importNewResourceByName(FILE_NAME_USER_VFC, UserRoleEnum.ADMIN);
		Integer statusCode = response.getErrorCode();
		assertTrue(String.format("Expected code %s and got code %s",ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS,statusCode),statusCode == ImportRestUtils.STATUS_CODE_IMPORT_SUCCESS);
		String uid = ResponseParser.getUniqueIdFromResponse(response);
		response = ResourceRestUtils.getResource(uid);
		Resource VFC = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		List<PropertyDefinition> props = VFC.getProperties();
		for (PropertyDefinition prop : props) {
			assertTrue(null != prop.getOwnerId() && !uid.equals(prop.getOwnerId()));
			
		}

	}

}
