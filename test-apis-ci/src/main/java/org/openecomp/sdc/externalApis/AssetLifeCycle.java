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

package org.openecomp.sdc.externalApis;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceExternalReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.SearchCriteriaEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtilsExternalAPI;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;

import fj.data.Either;

public class AssetLifeCycle extends ComponentBaseTest {

	private static Logger log = LoggerFactory.getLogger(CRUDExternalAPI.class.getName());
	protected static final String UPLOAD_ARTIFACT_PAYLOAD = "UHVUVFktVXNlci1LZXktRmlsZS0yOiBzc2gtcnNhDQpFbmNyeXB0aW9uOiBhZXMyNTYtY2JjDQpDb21tZW5wOA0K";
	protected static final String UPLOAD_ARTIFACT_NAME = "TLV_prv.ppk";

	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";
	protected static User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

	protected Gson gson = new Gson();
	protected JSONParser jsonParser = new JSONParser();


	protected String serviceVersion;
	protected ResourceReqDetails resourceDetails;
	protected User sdncUserDetails;
	protected ServiceReqDetails serviceDetails;
	
	public static final int STATUS_CODE_SUCCESS = 200;
	public static final int STATUS_CODE_CREATED = 201;
	public static final int STATUS_CODE_DELETE = 204;
	public static final int STATUS_CODE_NOT_FOUND = 404;
	public static final int STATUS_CODE_SUCCESS_NO_CONTENT = 204;
	public static final int STATUS_CODE_SUCCESS_DELETE = 204;
	public static final int STATUS_CODE_INVALID_CONTENT = 400;
	public static final int STATUS_CODE_MISSING_DATA = 400;
	public static final int STATUS_CODE_MISSING_INFORMATION = 403;
	public static final int STATUS_CODE_RESTRICTED_ACCESS = 403;
	public static final int STATUS_CODE_ALREADY_EXISTS = 409;
	public static final int STATUS_CODE_RESTRICTED_OPERATION = 409;
	public static final int STATUS_CODE_COMPONENT_NAME_EXCEEDS_LIMIT = 400;
	public static final int STATUS_CODE_MISSING_COMPONENT_NAME = 400;
	public static final int STATUS_CODE_UNSUPPORTED_ERROR = 400;
	public static final int STATUS_CODE_IMPORT_SUCCESS = 201;
	public static final int STATUS_CODE_UPDATE_SUCCESS = 200;
	public static final int RESTRICTED_OPERATION = 409;
	public static final int STATUS_CODE_GET_SUCCESS = 200;
	public static final int COMPONENT_IN_CHECKOUT_STATE = 403;
	public static final int COMPONENT_ALREADY_CHECKED_IN = 409;
	public static final int COMPONENT_NOT_READY_FOR_CERTIFICATION = 403;
	public static final int COMPONENT_SENT_FOR_CERTIFICATION = 403;
	public static final int COMPONENT_IN_CERT_IN_PROGRESS_STATE = 403;
	public static final int COMPONENT_ALREADY_CERTIFIED = 403; 
	
	
	
	@Rule 
	public static TestName name = new TestName();

	public AssetLifeCycle() {
		super(name, AssetLifeCycle.class.getName());

	}
	
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test
	public void createResourceCheckInThenCheckOut() throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails defaultResource = ElementFactory.getDefaultResourceByType("ci", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VF.toString());
		
		RestResponse restResponse = ResourceRestUtilsExternalAPI.createResource(defaultResource, defaultUser);
		defaultResource.setVersion(String.format("%.1f",0.1));
		ResourceAssetStructure parsedCreatedResponse = gson.fromJson(restResponse.getResponse(), ResourceAssetStructure.class);
		
		restResponse = LifecycleRestUtils.checkInResource(parsedCreatedResponse.getUuid(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_CREATED, "Fail to check in.");
		
		Component resourceDetails = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, defaultResource.getName(), defaultResource.getVersion());
		Assert.assertEquals(resourceDetails.getLifecycleState().toString(), LifeCycleStatesEnum.CHECKIN.getComponentState().toString(), "Life cycle state not changed.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, parsedCreatedResponse.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.RESOURCES);	
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	
		restResponse = LifecycleRestUtils.checkOutResource(parsedCreatedResponse.getUuid(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_CREATED, "Fail to check out.");
		
		resourceDetails = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, defaultResource.getName(), String.format("%.1f", Double.parseDouble(defaultResource.getVersion()) + 0.1));
		Assert.assertEquals(resourceDetails.getLifecycleState().toString(), LifeCycleStatesEnum.CHECKOUT.getComponentState().toString(), "Life cycle state not changed.");
		
		// auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, parsedCreatedResponse.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setCurrVersion("0.2");
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test
	public void createServiceCheckInThenCheckOut() throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		Component resourceDetails = null;
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		
		RestResponse restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_CREATED, "Fail to check in.");
		
		resourceDetails = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceDetails.getName(), resourceDetails.getVersion());
		Assert.assertEquals(resourceDetails.getLifecycleState().toString(), LifeCycleStatesEnum.CHECKIN.getComponentState().toString(), "Life cycle state not changed.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.SERVICES);	
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	
		restResponse = LifecycleRestUtils.checkOutService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_CREATED, "Fail to check out.");
		
		resourceDetails = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceDetails.getName(), String.format("%.1f", Double.parseDouble(resourceDetails.getVersion()) + 0.1));
		Assert.assertEquals(resourceDetails.getLifecycleState().toString(), LifeCycleStatesEnum.CHECKOUT.getComponentState().toString(), "Life cycle state not changed.");
		
		// auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setCurrVersion("0.2");
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@DataProvider(name="invalidAssetUUID") 
	public static Object[][] dataProviderInvalidAssetUUID() {
		return new Object[][] {
			{AssetTypeEnum.SERVICES, LifeCycleStatesEnum.CHECKIN},
			{AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CHECKIN},
			
			{AssetTypeEnum.SERVICES, LifeCycleStatesEnum.CHECKOUT},
			{AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CHECKOUT},
			
			{AssetTypeEnum.SERVICES, LifeCycleStatesEnum.CERTIFICATIONREQUEST},
			{AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CERTIFICATIONREQUEST},
			
			{AssetTypeEnum.SERVICES, LifeCycleStatesEnum.STARTCERTIFICATION},
			{AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.STARTCERTIFICATION},
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidAssetUUID")
	public void invalidAssetUUID(AssetTypeEnum assetTypeEnum, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String assetUUID = "InvalidAssetUUID";
		ErrorInfo errorInfo = null;
		
		RestResponse restResponse = null;
		if(assetTypeEnum.equals(AssetTypeEnum.SERVICES)) {
			
			if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CHECKIN)) {
				restResponse = LifecycleRestUtils.checkInService(assetUUID, defaultUser);
			} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CHECKOUT)) {
				restResponse = LifecycleRestUtils.checkOutService(assetUUID, defaultUser);
			} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFICATIONREQUEST)) {
				restResponse = LifecycleRestUtils.certificationRequestService(assetUUID, defaultUser);
			} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.STARTCERTIFICATION)) {
				restResponse = LifecycleRestUtils.startTestingService(assetUUID, defaultUser);
			}
				
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.SERVICE_NOT_FOUND.name());
		} else {
			
			if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CHECKIN)) {
				restResponse = LifecycleRestUtils.checkInResource(assetUUID, defaultUser);
			} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CHECKOUT)) {
				restResponse = LifecycleRestUtils.checkOutResource(assetUUID, defaultUser);
			} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFICATIONREQUEST)) {
				restResponse = LifecycleRestUtils.certificationRequestResource(assetUUID, defaultUser);
			} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.STARTCERTIFICATION)) {
				restResponse = LifecycleRestUtils.startTestingResource(assetUUID, defaultUser);
			}
			
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
			
		}
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_NOT_FOUND, "Asset found.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", assetTypeEnum.getValue().toLowerCase(), assetUUID, lifeCycleStatesEnum.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultInvalidChangeAssetLifeCycleExternalAPI(assetUUID, defaultUser, lifeCycleStatesEnum, assetTypeEnum);	
		
		List<String> variables = asList(assetUUID);
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@DataProvider(name="invalidUserCheckinForCheckedOutService") 
	public static Object[][] dataProviderInvalidUserCheckinForCheckedOutService() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
			/*due to those roles are not exists in the system		{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckinForCheckedOutService")
	public void invalidUserCheckinForCheckedOutService(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		
		RestResponse restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	@DataProvider(name="invalidUserCheckinForCheckedInService") 
	public static Object[][] dataProviderInvalidUserCheckinForCheckedInService() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
			/*due to those roles are not exists in the system					{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckinForCheckedInService")
	public void invalidUserCheckinForCheckedInService(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		
		RestResponse restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	@DataProvider(name="invalidUserCheckoutForCheckedOutService") 
	public static Object[][] dataProviderInvalidUserCheckoutForCheckedOutService() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
			/*due to those roles are not exists in the system		{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckoutForCheckedOutService")
	public void invalidUserCheckoutForCheckedOutService(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		
		RestResponse restResponse = LifecycleRestUtils.checkOutService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	@DataProvider(name="invalidUserCheckoutForCheckedInService") 
	public static Object[][] dataProviderInvalidUserCheckoutForCheckedInService() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
			/*due to those roles are not exists in the system		{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckoutForCheckedInService")
	public void invalidUserCheckoutForCheckedInService(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		
		RestResponse restResponse = LifecycleRestUtils.checkOutService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	@DataProvider(name="invalidUserCheckinForCheckedOutResource") 
	public static Object[][] dataProviderInvalidUserCheckinForCheckedOutResource() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
	/*due to those roles are not exists in the system		{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckinForCheckedOutResource")
	public void invalidUserCheckinForCheckedOutResource(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	@DataProvider(name="invalidUserCheckinForCheckedInResource") 
	public static Object[][] dataProviderInvalidUserCheckinForCheckedInResource() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
			/*due to those roles are not exists in the system		{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckinForCheckedInResource")
	public void invalidUserCheckinForCheckedInResource(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	@DataProvider(name="invalidUserCheckoutForCheckedOutResource") 
	public static Object[][] dataProviderInvalidUserCheckoutForCheckedOutResource() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
			/*due to those roles are not exists in the system		{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckoutForCheckedOutResource")
	public void invalidUserCheckoutForCheckedOutResource(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		
		RestResponse restResponse = LifecycleRestUtils.checkOutResource(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	@DataProvider(name="invalidUserCheckoutForCheckedInResource") 
	public static Object[][] dataProviderInvalidUserCheckoutForCheckedInResource() {
		return new Object[][] {
			{ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			// TODO: remove comment after talk with renana if it is defect or not
//			{ElementFactory.getDefaultUser(UserRoleEnum.ADMIN)},
			{ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR)},
			{ElementFactory.getDefaultUser(UserRoleEnum.OPS)},
			/*due to those roles are not exists in the system		{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1)},
			{ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1)},*/
			};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidUserCheckoutForCheckedInResource")
	public void invalidUserCheckoutForCheckedInResource(User defaultUser) throws Exception {
		Component resourceDetails = null;
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetails = createdComponent.left().value();
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		
		RestResponse restResponse = LifecycleRestUtils.checkOutResource(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)RESTRICTED_OPERATION, "Expected for restricted operation.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setStatus("409");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList("");
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	@DataProvider(name="invalidStatesForService") 
	public static Object[][] dataProviderInvalidStatesForService() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CHECKED_IN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "409", ActionStatus.COMPONENT_ALREADY_CHECKED_IN, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.CHECKOUT, COMPONENT_IN_CHECKOUT_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_IN_CHECKOUT_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_SENT_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, "403", ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, LifeCycleStatesEnum.CERTIFY, RESTRICTED_OPERATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, "409", ActionStatus.RESTRICTED_OPERATION, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CHECKIN, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CHECKOUT, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},

			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
		};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidStatesForService")
	public void invalidStatesForService(LifeCycleStatesEnum initState, LifeCycleStatesEnum targetState, int errorCode,
			LifecycleStateEnum preState, LifecycleStateEnum currState, String status,
			ActionStatus actionStatus, User user, User operationUser) throws Exception {
		
		if(initState.equals(LifeCycleStatesEnum.STARTCERTIFICATION) && targetState.equals(LifeCycleStatesEnum.CHECKIN)){
			throw new SkipException("Open bug DE270217 or TDP number: 154592");			
		}
		
		getExtendTest().log(Status.INFO, String.format("initState: %s, targetState: %s, errorCode: %s,"
				+ " preState: %s, currState: %s, status: %s, actionStatus: %s, user: %s, operationUser: %s", initState, targetState, errorCode, preState,
				currState, status, actionStatus, user, operationUser));
		
		
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, initState, true).getLeft();
		
		RestResponse restResponse = null;
		
		if(targetState.equals(LifeCycleStatesEnum.CHECKOUT)) {
			restResponse = LifecycleRestUtils.checkOutService(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CHECKIN)) {
			restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CERTIFICATIONREQUEST)) {
			restResponse = LifecycleRestUtils.certificationRequestService(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.STARTCERTIFICATION)) {
			restResponse = LifecycleRestUtils.startTestingService(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CERTIFY)) {
			restResponse = LifecycleRestUtils.certifyService(resourceDetails.getUUID(), user);
		}
		
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)errorCode, "Expected that response code will be equal.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, user, targetState, AssetTypeEnum.SERVICES);	
		if(initState.equals(LifeCycleStatesEnum.CERTIFY)) {
			expectedResourceAuditJavaObject.setCurrVersion("1.0");
			expectedResourceAuditJavaObject.setPrevVersion("1.0");
		}
		expectedResourceAuditJavaObject.setPrevState(preState.toString());
		expectedResourceAuditJavaObject.setCurrState(currState.toString());
		expectedResourceAuditJavaObject.setStatus(status);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		List<String> variables = null;
		if(ActionStatus.RESTRICTED_OPERATION.equals(actionStatus)) {
			variables = asList("");
		} else if(ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION.equals(actionStatus)) {
			variables = asList(resourceDetails.getName(), AssetTypeEnum.SERVICES.getCorrespondingComponent().toLowerCase());
		} else {
			if(operationUser == null) {
				variables = asList(resourceDetails.getName(), AssetTypeEnum.SERVICES.getCorrespondingComponent().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
			} else {
				variables = asList(resourceDetails.getName(), AssetTypeEnum.SERVICES.getCorrespondingComponent().toLowerCase(), operationUser.getFirstName(), operationUser.getLastName(), operationUser.getUserId());
			}
			
		}
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@DataProvider(name="invalidStatesForResource") 
	public static Object[][] dataProviderInvalidStatesForResource() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CHECKED_IN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "409", ActionStatus.COMPONENT_ALREADY_CHECKED_IN, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.CHECKOUT, COMPONENT_IN_CHECKOUT_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_IN_CHECKOUT_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_SENT_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, "403", ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, LifeCycleStatesEnum.CERTIFY, RESTRICTED_OPERATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, "409", ActionStatus.RESTRICTED_OPERATION, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CHECKIN, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CHECKOUT, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},

			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
		};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidStatesForResource")
	public void invalidStatesForResource(LifeCycleStatesEnum initState, LifeCycleStatesEnum targetState, int errorCode,
			LifecycleStateEnum preState, LifecycleStateEnum currState, String status,
			ActionStatus actionStatus, User user, User operationUser) throws Exception {
		getExtendTest().log(Status.INFO, String.format("initState: %s, targetState: %s, errorCode: %s,"
				+ " preState: %s, currState: %s, status: %s, actionStatus: %s, user: %s, operationUser: %s", initState, targetState, errorCode, preState,
				currState, status, actionStatus, user, operationUser));
		
		if(initState.equals(LifeCycleStatesEnum.STARTCERTIFICATION) && targetState.equals(LifeCycleStatesEnum.CHECKIN)){
			throw new SkipException("Open bug DE270217 or TDP number: 154592");			
		}
		
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, initState, true).getLeft();
		
		RestResponse restResponse = null;
		
		if(targetState.equals(LifeCycleStatesEnum.CHECKOUT)) {
			restResponse = LifecycleRestUtils.checkOutResource(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CHECKIN)) {
			restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CERTIFICATIONREQUEST)) {
			restResponse = LifecycleRestUtils.certificationRequestResource(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.STARTCERTIFICATION)) {
			restResponse = LifecycleRestUtils.startTestingResource(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CERTIFY)) {
			restResponse = LifecycleRestUtils.certifyResource(resourceDetails.getUUID(), user);
		}
		
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)errorCode, "Expected that response code will be equal.");
		
		// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, user, targetState, AssetTypeEnum.RESOURCES);	
		if(initState.equals(LifeCycleStatesEnum.CERTIFY)) {
			expectedResourceAuditJavaObject.setCurrVersion("1.0");
			expectedResourceAuditJavaObject.setPrevVersion("1.0");
		}
		expectedResourceAuditJavaObject.setPrevState(preState.toString());
		expectedResourceAuditJavaObject.setCurrState(currState.toString());
		expectedResourceAuditJavaObject.setStatus(status);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		List<String> variables = null;
		if(ActionStatus.RESTRICTED_OPERATION.equals(actionStatus)) {
			variables = asList("");
		} else if(ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION.equals(actionStatus)) {
			variables = asList(resourceDetails.getName(), AssetTypeEnum.RESOURCES.getCorrespondingComponent().toLowerCase());
		} else {
			if(operationUser == null) {
				variables = asList(resourceDetails.getName(), AssetTypeEnum.RESOURCES.getCorrespondingComponent().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
			} else {
				variables = asList(resourceDetails.getName(), AssetTypeEnum.RESOURCES.getCorrespondingComponent().toLowerCase(), operationUser.getFirstName(), operationUser.getLastName(), operationUser.getUserId());
			}
			
		}
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void BasicFlowForResourceSubmitForTestingStartTesting() throws Exception {
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.CERTIFICATIONREQUEST.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CERTIFICATIONREQUEST, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.READY_FOR_CERTIFICATION.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
		// Start testing
		restResponse = LifecycleRestUtils.startTestingResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
		// Auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.STARTCERTIFICATION.getState()));
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.READY_FOR_CERTIFICATION.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void BasicFlowForServiceSubmitForTestingStartTesting() throws Exception {
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.SERVICES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.CERTIFICATIONREQUEST.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CERTIFICATIONREQUEST, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.READY_FOR_CERTIFICATION.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
		// Start testing
		restResponse = LifecycleRestUtils.startTestingService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
		// Auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.SERVICES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.STARTCERTIFICATION.getState()));
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.READY_FOR_CERTIFICATION.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void specialCaseInvalidFlowForVfcmtSubmitForTesting() throws Exception {
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFCMT, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.CERTIFICATIONREQUEST.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CERTIFICATIONREQUEST, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setStatus("400");
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_VFCMT_LIFECYCLE_STATE_NOT_VALID.name());
		List<String> variables = asList(LifeCycleStatesEnum.CERTIFICATIONREQUEST.getState());
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void specialCaseInvalidFlowForVfcmtStartTesting() throws Exception {
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFCMT, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.startTestingResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
		// Auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.STARTCERTIFICATION.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setStatus("400");
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_VFCMT_LIFECYCLE_STATE_NOT_VALID.name());
		List<String> variables = asList(LifeCycleStatesEnum.STARTCERTIFICATION.getState());
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void BasicFlowForResourceCertify() throws Exception {
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Start testing
		restResponse = LifecycleRestUtils.startTestingResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
		// Certify
		restResponse = LifecycleRestUtils.certifyResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
		// Auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.CERTIFY.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.toString());
		expectedResourceAuditJavaObject.setCurrVersion("1.0");
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
	}
		
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void BasicFlowForServiceCertify() throws Exception {
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Start testing
		restResponse = LifecycleRestUtils.startTestingService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
		// Certify
		restResponse = LifecycleRestUtils.certifyService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
		// Auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.SERVICES.getValue().toLowerCase(), resourceDetails.getUUID(), LifeCycleStatesEnum.CERTIFY.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.toString());
		expectedResourceAuditJavaObject.setCurrVersion("1.0");
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
	}
	
	
	
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void theFlow() throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails defaultResource = ElementFactory.getDefaultResourceByType("ci", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VFCMT.toString());
		
		// 1. Create VFCMT.
		RestResponse restResponse = ResourceRestUtilsExternalAPI.createResource(defaultResource, defaultUser);
		BaseRestUtils.checkCreateResponse(restResponse);
		ResourceAssetStructure parsedCreatedResponse = gson.fromJson(restResponse.getResponse(), ResourceAssetStructure.class);
		
		// 2. Using search external API with resourceType=VFCMT to retrieve VFCMT.
		restResponse = ResourceRestUtils.getResourceListFilterByCriteria(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), SearchCriteriaEnum.RESOURCE_TYPE.getValue(), ResourceTypeEnum.VFCMT.toString());
		BaseRestUtils.checkSuccess(restResponse);
		ResourceAssetStructure dataOutOfSearchResponseForResourceName = ResponseParser.getDataOutOfSearchExternalAPIResponseForResourceName(restResponse.getResponse(), parsedCreatedResponse.getName());
		Assert.assertEquals(parsedCreatedResponse.getUuid(), dataOutOfSearchResponseForResourceName.getUuid(), "Created resourceUUID not equal to search retrive resourceUUID.");
		
		// 3. Use getSpecificMetadata external API to receive full information of the VFCMT.
		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, dataOutOfSearchResponseForResourceName.getUuid());
		BaseRestUtils.checkSuccess(assetResponse);
		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		Assert.assertEquals(resourceAssetMetadata.getUuid(), parsedCreatedResponse.getUuid(), "Created resourceUUID not equal to getSpecificMetadata resourceUUID.");
		
		// 4. Upload artifact via upload artifact external API.
		Component initComponentVersion = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, parsedCreatedResponse.getName(), parsedCreatedResponse.getVersion());
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", "OTHER", true, false);
		RestResponse uploadArtifactRestResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(initComponentVersion, defaultUser, artifactReqDetails);
		BaseRestUtils.checkSuccess(uploadArtifactRestResponse);
		ArtifactDefinition responseArtifact = ArtifactRestUtils.getArtifactDataFromJson(uploadArtifactRestResponse.getResponse());
		initComponentVersion = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, parsedCreatedResponse.getName(), parsedCreatedResponse.getVersion());
		
		// 5. Update artifact via external API.
		CRUDExternalAPI crudExternalAPI = new CRUDExternalAPI();
		crudExternalAPI.updateArtifactOnAssetViaExternalAPI(initComponentVersion, ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, "OTHER");
	
		
		// 6. Use external API to checkin the VFCMT.
		RestResponse checkInRestResponse = LifecycleRestUtils.checkInResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(checkInRestResponse);
		parsedCreatedResponse = gson.fromJson(checkInRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "0.1", "Expect that version will not change.");
		Assert.assertEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will not change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
		// Auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, initComponentVersion.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), initComponentVersion.getUUID(), LifeCycleStatesEnum.CHECKIN.getState()));
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(initComponentVersion, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.RESOURCES);	
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
		// 7. Then checkout the VFCMT via external API.
		RestResponse checkOutRestResponse = LifecycleRestUtils.checkOutResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(checkOutRestResponse);
		parsedCreatedResponse = gson.fromJson(checkOutRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "0.2", "Expect that version will change to 0.2.");
		Assert.assertEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will not change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
		// Auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, initComponentVersion.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), initComponentVersion.getUUID(), LifeCycleStatesEnum.CHECKOUT.getState()));
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(initComponentVersion, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setCurrVersion("0.2");
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
		// 8. The minor version must be incremented, the invariantUUID, and UUID must stay the same, the uniqueId should be changed, the artifacts from first version exists on the new version.
		Component newComponentVersion = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, parsedCreatedResponse.getName(), String.format("%.1f", Double.parseDouble(parsedCreatedResponse.getVersion())));
		Assert.assertEquals(newComponentVersion.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		Assert.assertEquals(newComponentVersion.getUUID(), initComponentVersion.getUUID(), "Expected that UUID will not change.");
		Assert.assertNotEquals(newComponentVersion.getUniqueId(), initComponentVersion.getUniqueId(), "Expected that uniqueId will change.");
		Assert.assertTrue(newComponentVersion.getDeploymentArtifacts().keySet().contains(responseArtifact.getArtifactLabel()), "Expected that artifact from first version exists on the new version.");
		
		// 9. The previous version remains untouched, the highest version flag on the first version is false, all information (metadata / artifacts) are the same on the first version.
		initComponentVersion = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, parsedCreatedResponse.getName(), String.format("%.1f", Double.parseDouble("0.1")));
		Assert.assertEquals(initComponentVersion.isHighestVersion(), (Boolean)false, "Expected that highest version flag on first version is false.");
		
		// 10. Check in via external API.
		checkInRestResponse = LifecycleRestUtils.checkInResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(checkInRestResponse);
		parsedCreatedResponse = gson.fromJson(checkInRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "0.2", "Expect that version will not change.");
		Assert.assertEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will not change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
		// Auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, initComponentVersion.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), initComponentVersion.getUUID(), LifeCycleStatesEnum.CHECKIN.getState()));
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(initComponentVersion, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrVersion("0.2");
		expectedResourceAuditJavaObject.setPrevVersion("0.2");
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
		// 11. Certify via external API.
		RestResponse certifyRestResponse = LifecycleRestUtils.certifyResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(certifyRestResponse);
		parsedCreatedResponse = gson.fromJson(certifyRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "1.0", "Expect that version will change to 1.0");
		Assert.assertEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will not change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
		// Auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, initComponentVersion.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), initComponentVersion.getUUID(), LifeCycleStatesEnum.CERTIFY.getState()));
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(initComponentVersion, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CERTIFY, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.toString());
		expectedResourceAuditJavaObject.setCurrVersion("1.0");
		expectedResourceAuditJavaObject.setPrevVersion("0.2");
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
		// 12. Check out via external API.
		checkOutRestResponse = LifecycleRestUtils.checkOutResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(checkOutRestResponse);
		parsedCreatedResponse = gson.fromJson(checkOutRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "1.1", "Expect that version will change to 1.1");
		Assert.assertNotEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
		// Auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, initComponentVersion.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, String.format("/sdc/v1/catalog/%s/%s/lifecycleState/%s", AssetTypeEnum.RESOURCES.getValue().toLowerCase(), initComponentVersion.getUUID(), LifeCycleStatesEnum.CHECKOUT.getState()));
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(initComponentVersion, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFIED.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setCurrVersion("1.1");
		expectedResourceAuditJavaObject.setPrevVersion("1.0");
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
