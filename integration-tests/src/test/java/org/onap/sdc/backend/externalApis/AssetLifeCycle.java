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

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;
import fj.data.Either;
import java.io.FileNotFoundException;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.*;
import org.onap.sdc.backend.ci.tests.datatypes.enums.*;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.onap.sdc.backend.ci.tests.datatypes.enums.*;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
	
	public static final int STATUS_CODE_SUCCESS = getResponseCodeByAction(ActionStatus.OK);
	public static final int STATUS_CODE_CREATED = getResponseCodeByAction(ActionStatus.CREATED);
	public static final int STATUS_CODE_NOT_FOUND = getResponseCodeByAction(ActionStatus.RESOURCE_NOT_FOUND);
	public static final int STATUS_CODE_INVALID_CONTENT = getResponseCodeByAction(ActionStatus.INVALID_CONTENT);
	public static final int STATUS_CODE_MISSING_INFORMATION = getResponseCodeByAction(ActionStatus.MISSING_INFORMATION);
	public static final int STATUS_CODE_ALREADY_EXISTS = getResponseCodeByAction(ActionStatus.RESOURCE_ALREADY_EXISTS);
	public static final Integer RESTRICTED_OPERATION = getResponseCodeByAction(ActionStatus.RESTRICTED_OPERATION);
	public static final int COMPONENT_IN_CHECKOUT_STATE = getResponseCodeByAction(ActionStatus.COMPONENT_IN_CHECKOUT_STATE);
	public static final int COMPONENT_ALREADY_CHECKED_IN = getResponseCodeByAction(ActionStatus.COMPONENT_ALREADY_CHECKED_IN);
	public static final int COMPONENT_NOT_READY_FOR_CERTIFICATION = getResponseCodeByAction(ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION);
	public static final int COMPONENT_SENT_FOR_CERTIFICATION = getResponseCodeByAction(ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION);
	public static final int COMPONENT_IN_CERT_IN_PROGRESS_STATE = getResponseCodeByAction(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE);
	public static final int COMPONENT_ALREADY_CERTIFIED = getResponseCodeByAction(ActionStatus.COMPONENT_ALREADY_CERTIFIED);
	
	

	public static int getResponseCodeByAction(ActionStatus actionStatus ){
		ErrorInfo errorInfo;
		try {
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
			return errorInfo.getCode();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return 100500;
	}

	@Rule 
	public static TestName name = new TestName();

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
		
		/*// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, parsedCreatedResponse.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.RESOURCES);	
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);*/
	
		restResponse = LifecycleRestUtils.checkOutResource(parsedCreatedResponse.getUuid(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_CREATED, "Fail to check out.");
		
		resourceDetails = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, defaultResource.getName(), String.format("%.1f", Double.parseDouble(defaultResource.getVersion()) + 0.1));
		Assert.assertEquals(resourceDetails.getLifecycleState().toString(), LifeCycleStatesEnum.CHECKOUT.getComponentState().toString(), "Life cycle state not changed.");
		
		/*// auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, parsedCreatedResponse.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.RESOURCES);	
		expectedResourceAuditJavaObject.setCurrVersion("0.2");
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);*/
		
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
		
		/*// auditing verification
		AuditingActionEnum action = AuditingActionEnum.CHANGE_LIFECYCLE_BY_API;
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKIN, AssetTypeEnum.SERVICES);	
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);*/
	
		restResponse = LifecycleRestUtils.checkOutService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_CREATED, "Fail to check out.");
		
		resourceDetails = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceDetails.getName(), String.format("%.1f", Double.parseDouble(resourceDetails.getVersion()) + 0.1));
		Assert.assertEquals(resourceDetails.getLifecycleState().toString(), LifeCycleStatesEnum.CHECKOUT.getComponentState().toString(), "Life cycle state not changed.");
		
		/*// auditing verification
		body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject = ElementFactory.getDefaultChangeAssetLifeCycleExternalAPI(resourceDetails, defaultUser, LifeCycleStatesEnum.CHECKOUT, AssetTypeEnum.SERVICES);	
		expectedResourceAuditJavaObject.setCurrVersion("0.2");
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		AuditValidationUtils.validateAuditExternalChangeAssetLifeCycle(expectedResourceAuditJavaObject, action.getName(), body);*/
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
			/*{AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CERTIFICATIONREQUEST},*/
			
			{AssetTypeEnum.SERVICES, LifeCycleStatesEnum.STARTCERTIFICATION},
			/*{AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.STARTCERTIFICATION},*/
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
			/*} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFICATIONREQUEST)) {
				restResponse = LifecycleRestUtils.certificationRequestResource(assetUUID, defaultUser);
			} else if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.STARTCERTIFICATION)) {
				restResponse = LifecycleRestUtils.startTestingResource(assetUUID, defaultUser);*/
			}
			
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
			
		}
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)STATUS_CODE_NOT_FOUND, "Asset found.");
		
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
		Component resourceDetails = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
		
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
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
		
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

		Component resourceDetails = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		RestResponse restResponse = LifecycleRestUtils.checkOutService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
		
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
		Component resourceDetails = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		RestResponse restResponse = LifecycleRestUtils.checkOutService(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
		
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

		Component resourceDetails = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true).left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
		
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
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
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

		Component resourceDetails = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true).left().value();
		RestResponse restResponse = LifecycleRestUtils.checkOutResource(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
		
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

		Component resourceDetails = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true).left().value();
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		
		RestResponse restResponse = LifecycleRestUtils.checkOutResource(resourceDetails.getUUID(), defaultUser);
		Assert.assertEquals(restResponse.getErrorCode(), RESTRICTED_OPERATION, "Expected for restricted operation.");
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	@DataProvider(name="invalidStatesForService") 
	public static Object[][] dataProviderInvalidStatesForService() {
		return new Object[][] {
				{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CHECKED_IN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "409", ActionStatus.COMPONENT_ALREADY_CHECKED_IN, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
				{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.CHECKOUT, COMPONENT_IN_CHECKOUT_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_IN_CHECKOUT_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
				{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER)},
				{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
		};
	}
	
	// US849997 - Story [BE]: External API for asset lifecycle - checkout
	@Test(dataProvider="invalidStatesForService")
	public void invalidStatesForService(LifeCycleStatesEnum initState, LifeCycleStatesEnum targetState, int errorCode,
			LifecycleStateEnum preState, LifecycleStateEnum currState, String status,
			ActionStatus actionStatus, User user, User operationUser) throws Exception {

		getExtendTest().log(Status.INFO, String.format("initState: %s, targetState: %s, errorCode: %s,"
				+ " preState: %s, currState: %s, status: %s, actionStatus: %s, user: %s, operationUser: %s", initState, targetState, errorCode, preState,
				currState, status, actionStatus, user, operationUser));
		
		
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		Component service = createdComponent.left().value();
		
		service = AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, initState, true).getLeft();
		
		RestResponse restResponse = null;
		
		if(targetState.equals(LifeCycleStatesEnum.CHECKOUT)) {
			restResponse = LifecycleRestUtils.checkOutService(service.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CHECKIN)) {
			restResponse = LifecycleRestUtils.checkInService(service.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CERTIFY)) {
			restResponse = LifecycleRestUtils.certifyService(service.getUUID(), user);
		}
		
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)errorCode, "Expected that response code will be equal.");
		
	}

	@DataProvider(name="invalidStatesForResource") 
	public static Object[][] dataProviderInvalidStatesForResource() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CHECKED_IN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "409", ActionStatus.COMPONENT_ALREADY_CHECKED_IN, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
		/*	{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},*/
			{LifeCycleStatesEnum.CHECKIN, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.CHECKOUT, COMPONENT_IN_CHECKOUT_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_IN_CHECKOUT_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
		/*	{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},*/
			{LifeCycleStatesEnum.CHECKOUT, LifeCycleStatesEnum.CERTIFY, COMPONENT_NOT_READY_FOR_CERTIFICATION, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, "403", ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},
			
			/*{LifeCycleStatesEnum.CERTIFICATIONREQUEST, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_SENT_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, "403", ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, LifeCycleStatesEnum.CERTIFY, RESTRICTED_OPERATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, LifecycleStateEnum.READY_FOR_CERTIFICATION, "409", ActionStatus.RESTRICTED_OPERATION, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), null},
			
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CHECKIN, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CHECKOUT, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.STARTCERTIFICATION, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_IN_CERT_IN_PROGRESS_STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, "403", ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},*/

			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CHECKIN, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			/*{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.CERTIFICATIONREQUEST, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ElementFactory.getDefaultUser(UserRoleEnum.TESTER)},
			{LifeCycleStatesEnum.CERTIFY, LifeCycleStatesEnum.STARTCERTIFICATION, COMPONENT_ALREADY_CERTIFIED, LifecycleStateEnum.CERTIFIED, LifecycleStateEnum.CERTIFIED, "403", ActionStatus.COMPONENT_ALREADY_CERTIFIED, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), null},*/
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

		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		
		resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, UserRoleEnum.DESIGNER, initState, true).getLeft();
		
		RestResponse restResponse = null;
		
		if(targetState.equals(LifeCycleStatesEnum.CHECKOUT)) {
			restResponse = LifecycleRestUtils.checkOutResource(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CHECKIN)) {
			restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), user);
		} else if(targetState.equals(LifeCycleStatesEnum.CERTIFY)) {
			restResponse = LifecycleRestUtils.certifyResource(resourceDetails.getUUID(), user);
		}
		
		Assert.assertEquals(restResponse.getErrorCode(), (Integer)errorCode, "Expected that response code will be equal.");
		
	}
	
	

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
/*	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void BasicFlowForResourceSubmitForTestingStartTesting() throws Exception {
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
	}*/
	
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void BasicFlowForServiceSubmitForTestingStartTesting() throws Exception {
		Either<Service, RestResponse> createdComponent = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Start testing
		restResponse = LifecycleRestUtils.startTestingService(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
	}
	
	/*// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void specialCaseInvalidFlowForVfcmtSubmitForTesting() throws Exception {
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFCMT, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.certificationRequestResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
	}*/
	
	// US824692 - Story [BE]: External API for asset lifecycle - submit for test / start testing
	@Test
	public void specialCaseInvalidFlowForVfcmtStartTesting() throws Exception {
		Either<Resource, RestResponse> createdComponent = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFCMT, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetails = createdComponent.left().value();
		RestResponse restResponse = LifecycleRestUtils.checkInResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Certification request
		restResponse = LifecycleRestUtils.startTestingResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));
		
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
		
		/*// Certification request
		restResponse = LifecycleRestUtils.certificationRequestResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		
		// Start testing
		restResponse = LifecycleRestUtils.startTestingResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER));*/
		
		// Certify
		restResponse = LifecycleRestUtils.certifyResource(resourceDetails.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
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
		Assert.assertEquals(parsedCreatedResponse.getUuid(), dataOutOfSearchResponseForResourceName.getUuid(), "Created resourceUUID not equal to search retrieve resourceUUID.");
		
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

		ArtifactDefinition responseArtifact = ResponseParser.convertArtifactDefinitionResponseToJavaObject(uploadArtifactRestResponse.getResponse());
//		ArtifactDefinition responseArtifact = ArtifactRestUtils.getArtifactDataFromJson(uploadArtifactRestResponse.getResponse());
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
		
		// 7. Then checkout the VFCMT via external API.
		RestResponse checkOutRestResponse = LifecycleRestUtils.checkOutResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(checkOutRestResponse);
		parsedCreatedResponse = gson.fromJson(checkOutRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "0.2", "Expect that version will change to 0.2.");
		Assert.assertEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will not change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
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
		
		// 11. Certify via external API.
		RestResponse certifyRestResponse = LifecycleRestUtils.certifyResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(certifyRestResponse);
		parsedCreatedResponse = gson.fromJson(certifyRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "1.0", "Expect that version will change to 1.0");
		Assert.assertEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will not change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
		// 12. Check out via external API.
		checkOutRestResponse = LifecycleRestUtils.checkOutResource(initComponentVersion.getUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(checkOutRestResponse);
		parsedCreatedResponse = gson.fromJson(checkOutRestResponse.getResponse(), ResourceAssetStructure.class);
		Assert.assertEquals(parsedCreatedResponse.getVersion(), "1.1", "Expect that version will change to 1.1");
		Assert.assertNotEquals(parsedCreatedResponse.getUuid(), initComponentVersion.getUUID(), "Expect that UUID will change.");
		Assert.assertEquals(parsedCreatedResponse.getInvariantUUID(), initComponentVersion.getInvariantUUID(), "Expected that invariantUUID will not change.");
		
	}
	
}
