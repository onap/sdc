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

package org.onap.sdc.backend.externalApis;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceAssetStructure;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceExternalReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ErrorInfo;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.SearchCriteriaEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.AssetRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtilsExternalAPI;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;

public class VFCMTExternalAPI extends ComponentBaseTest {

	private static Logger log = LoggerFactory.getLogger(CRUDExternalAPI.class.getName());
	protected static final String UPLOAD_ARTIFACT_PAYLOAD = "UHVUVFktVXNlci1LZXktRmlsZS0yOiBzc2gtcnNhDQpFbmNyeXB0aW9uOiBhZXMyNTYtY2JjDQpDb21tZW5wOA0K";
	protected static final String UPLOAD_ARTIFACT_NAME = "TLV_prv.ppk";

	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";



	protected Gson gson = new Gson();
	protected JSONParser jsonParser = new JSONParser();


	protected String serviceVersion;
	protected ResourceReqDetails resourceDetails;
	protected User sdncUserDetails;
	protected ServiceReqDetails serviceDetails;
	

//	@BeforeMethod
//	public synchronized void init() throws Exception{
//		AtomicOperationUtils.createDefaultConsumer(true);
//	}
	
	
	@Rule 
	public static TestName name = new TestName();

	// Create VFCMT - validate response + search external api + retrieve metadata via external api  - success flow
	@Test
	public void createVfcmt() throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails defaultResource = ElementFactory.getDefaultResourceByType("ci", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VFCMT.toString());
		
		RestResponse restResponse = ResourceRestUtilsExternalAPI.createResource(defaultResource, defaultUser);
		ResourceAssetStructure parsedCreatedResponse = gson.fromJson(restResponse.getResponse(), ResourceAssetStructure.class);

	/*	// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CREATE_RESOURCE_BY_API;
		Map <AuditingFieldsKey, String> body = new HashMap<>();
		body.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME, parsedCreatedResponse.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultCreateResourceExternalAPI(parsedCreatedResponse.getName());
		AuditValidationUtils.validateAuditExternalCreateResource(expectedResourceAuditJavaObject, action.getName(), body);*/
		
		// search for vfcmt via external api - validate created resource exist
		RestResponse searchResult = ResourceRestUtils.getResourceListFilterByCriteria(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), SearchCriteriaEnum.RESOURCE_TYPE.getValue(), ResourceTypeEnum.VFCMT.toString());
		JsonArray listSearchResult = gson.fromJson(searchResult.getResponse(), JsonArray.class);
		boolean found = false;
		for(JsonElement result: listSearchResult) {
			ResourceAssetStructure parsedResult  = gson.fromJson(result, ResourceAssetStructure.class);
			if(parsedResult.getName().equals(defaultResource.getName())) {
				found = true;
			}
		}
		Assert.assertEquals(found, true);
		
		// get created vfcmt metadata via external api - validate data
		RestResponse resourceMetadata = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, parsedCreatedResponse.getUuid());
		ResourceAssetStructure parsedMetadata  = gson.fromJson(resourceMetadata.getResponse(), ResourceAssetStructure.class);
		
		Assert.assertEquals(parsedCreatedResponse.getUuid(), parsedMetadata.getUuid());
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), parsedMetadata.getInvariantUUID());
		Assert.assertEquals(parsedCreatedResponse.getName(), parsedMetadata.getName());
		Assert.assertEquals(parsedCreatedResponse.getVersion(), parsedMetadata.getVersion());
		Assert.assertEquals(parsedCreatedResponse.getCategory(), parsedMetadata.getCategory());
		Assert.assertEquals(parsedCreatedResponse.getSubCategory(), parsedMetadata.getSubCategory());
		Assert.assertEquals(parsedCreatedResponse.getResourceType(), parsedMetadata.getResourceType());
		Assert.assertEquals(parsedCreatedResponse.getLifecycleState(), parsedMetadata.getLifecycleState());
		Assert.assertEquals(parsedCreatedResponse.getLastUpdaterUserId(), parsedMetadata.getLastUpdaterUserId());
		Assert.assertEquals(parsedCreatedResponse.getLastUpdaterFullName(), parsedMetadata.getLastUpdaterFullName());
		Assert.assertEquals(parsedCreatedResponse.getToscaResourceName(), parsedMetadata.getToscaResourceName());
	}
	
	
	
	// Create two VFCMT with same name - validate error + audit - failure flow
	@Test
	public void createTwoVfcmtWithSameName() throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails defaultResource = ElementFactory.getDefaultResourceByType("ci", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VFCMT.toString());
		
		// create vfcmt
		RestResponse firstTryToCreate = ResourceRestUtilsExternalAPI.createResource(defaultResource, defaultUser);
		ResourceAssetStructure parsedCreatedResponse = gson.fromJson(firstTryToCreate.getResponse(), ResourceAssetStructure.class);

		/*// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CREATE_RESOURCE_BY_API;
		Map <AuditingFieldsKey, String> body = new HashMap<>();
		body.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME, parsedCreatedResponse.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultCreateResourceExternalAPI(parsedCreatedResponse.getName());	
		AuditValidationUtils.validateAuditExternalCreateResource(expectedResourceAuditJavaObject, action.getName(), body);*/
		
		// try to create another vfcmt wit same name
		RestResponse secondTryToCreate = ResourceRestUtilsExternalAPI.createResource(defaultResource, defaultUser);
		Assert.assertEquals((int)secondTryToCreate.getErrorCode(), 409);
		
		/*body.put(AuditingFieldsKey.AUDIT_STATUS, "409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_NAME_ALREADY_EXIST.name());
		List<String> variables = asList(ComponentTypeEnum.RESOURCE.getValue(), defaultResource.getName());
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		expectedResourceAuditJavaObject.setStatus("409");
		AuditValidationUtils.validateAuditExternalCreateResource(expectedResourceAuditJavaObject, action.getName(), body);*/
	}
	
	
	
	@DataProvider(name="createVfcmtVariousFailureFlows", parallel=true)
	public static Object[][] dataProviderCreateVfcmtVariousFailureFlows() {
		return new Object[][] {
			{"name_missing"},
			{"name_to_long"},
			{"name_with_invalid_char"},
			{"description_missing"},
			{"description_to_long"},
			{"description_with_invalid_char"},
			{"resource_type_missing"},
			{"resource_type_invalid"},
			{"category_type_missing"},
			{"category_type_invalid"},
			{"subcategory_type_missing"},
			{"subcategory_type_invalid"},
			{"vendor_name_missing"},
			{"vendor_name_to_long"},
			{"vendor_name_with_invalid_char"},
			{"vendor_release_missing"},
			{"vendor_release_to_long"},
			{"vendor_release_with_invalid_char"},
			{"tags_missing"},
			{"tags_to_long"},
			{"tags_invalid"},
			{"icon_missing"},
			{"user_contact_missing"},
			{"user_contact_invalid"},
			};
	}
	
	// Various failure flows
	@Test(dataProvider="createVfcmtVariousFailureFlows")
	public void createVfcmtVariousFailureFlows(String flow) throws Exception {
		
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails defaultResource = ElementFactory.getDefaultResourceByType("ci", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VFCMT.toString());
		
		ErrorInfo errorInfo = null;
		/*List<String> variables = null;
		AuditingActionEnum action = AuditingActionEnum.CREATE_RESOURCE_BY_API;
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultCreateResourceExternalAPI(defaultResource.getName());	
		Map <AuditingFieldsKey, String> body = new HashMap<>();
		body.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME, defaultResource.getName());*/
		
		switch (flow) {
		case "name_missing":
			defaultResource.setName("");
			List<String> resourceTags = defaultResource.getTags();
			resourceTags.add("");
			defaultResource.setTags(resourceTags);
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_COMPONENT_NAME.name());
			/*expectedResourceAuditJavaObject.setResourceName("");
			variables = asList(ComponentTypeEnum.RESOURCE.getValue());
			body.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME, "");*/
			break;
		case "name_to_long":
			defaultResource.setName("asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT.name());
			/*expectedResourceAuditJavaObject.setResourceName("asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1");
			variables = asList(ComponentTypeEnum.RESOURCE.getValue(), "1024");
			body.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME, "asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1");*/
			break;
		case "name_with_invalid_char":
			defaultResource.setName("!@#$%^&*(");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_COMPONENT_NAME.name());
			/*expectedResourceAuditJavaObject.setResourceName("!@#$%^&*(");
			variables = asList(ComponentTypeEnum.RESOURCE.getValue());
			body.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME, "!@#$%^&*(");*/
			break;
		case "description_missing":
			defaultResource.setDescription("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name());
			/*expectedResourceAuditJavaObject.setDesc("");
			variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "description_to_long":
			defaultResource.setDescription("asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT.name());
			/*expectedResourceAuditJavaObject.setDesc("asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1asdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjklasdfghjk1");
			variables = asList(ComponentTypeEnum.RESOURCE.getValue(), "1024");*/
			break;
		case "description_with_invalid_char":
			defaultResource.setDescription("\uC2B5");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INVALID_DESCRIPTION.name());
			/*expectedResourceAuditJavaObject.setDesc("t");
			variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		// TODO: defect on the flow - need to get error instead create VFC
		case "resource_type_missing":
			defaultResource.setResourceType("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_CONTENT.name());
			/*expectedResourceAuditJavaObject.setResourceType("");
			variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		// TODO: in audit RESOURCE_NAME is empty
		case "resource_type_invalid":
			defaultResource.setResourceType("invalid");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_RESOURCE_TYPE.name());
			/*expectedResourceAuditJavaObject.setResourceType(ComponentTypeEnum.RESOURCE.getValue());
			variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "category_type_missing":
			defaultResource.setCategory("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_CATEGORY.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		// TODO: not correct response code in this flow - 500 instead 400
		case "category_type_invalid":
			defaultResource.setCategory("invalid");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INVALID_CATEGORY.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "subcategory_type_missing":
			defaultResource.setSubcategory("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_SUBCATEGORY.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		// TODO: not correct error - it not missing it not correct
		case "subcategory_type_invalid":
			defaultResource.setSubcategory("invalid");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INVALID_SUBCATEGORY.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "vendor_name_missing":
			defaultResource.setVendorName("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_VENDOR_NAME.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "vendor_name_to_long":
			defaultResource.setVendorName("asdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdff");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT.name());
			/*variables = asList(ValidationUtils.VENDOR_NAME_MAX_LENGTH.toString());*/
			break;
		case "vendor_name_with_invalid_char":
			defaultResource.setVendorName("!@#$*()&*^%$#@");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_VENDOR_NAME.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "vendor_release_missing":
			defaultResource.setVendorRelease("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_VENDOR_RELEASE.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "vendor_release_to_long":
			defaultResource.setVendorRelease("asdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdff");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT.name());
			/*variables = asList("25");*/
			break;
		case "vendor_release_with_invalid_char":
			defaultResource.setVendorRelease("!@#$*()&*^%$#@");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_VENDOR_RELEASE.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "tags_missing":
			defaultResource.setTags(asList(""));
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_FIELD_FORMAT.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue(), "tag");*/
			break;
		case "tags_to_long":
			defaultResource.setTags(asList("asdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdffasdff"));
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT.name());
			/*variables = asList("1024");*/
			break;
		case "tags_invalid":
			defaultResource.setTags(asList("asfdg"));
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "icon_missing":
			defaultResource.setIcon("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_ICON.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "user_contact_missing":
			defaultResource.setContactId("");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_CONTACT.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		case "user_contact_invalid":
		default:
			defaultResource.setContactId("abcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfdabcderfffdfdfd");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INVALID_CONTACT.name());
			/*variables = asList(ComponentTypeEnum.RESOURCE.getValue());*/
			break;
		}
			
		// create vfcmt
		RestResponse restResponse = ResourceRestUtilsExternalAPI.createResource(defaultResource, defaultUser);
		assertTrue(errorInfo.getCode().equals(restResponse.getErrorCode()), "Expected response code on [" + flow + "] is: " + errorInfo.getCode() + ", actual is: " + restResponse.getErrorCode());
		
		/*expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalCreateResource(expectedResourceAuditJavaObject, action.getName(), body);*/
		
	}
	


	
	
	
	
	
	

	
}
