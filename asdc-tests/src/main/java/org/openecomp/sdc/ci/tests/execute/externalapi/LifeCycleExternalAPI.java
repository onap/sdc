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

package org.openecomp.sdc.ci.tests.execute.externalapi;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceExternalReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtilsExternalAPI;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class LifeCycleExternalAPI extends ComponentBaseTest{
	@Rule
	public static final TestName name = new TestName();
	protected Gson gson = new Gson();
	
	public LifeCycleExternalAPI() {
		super(name, LifeCycleExternalAPI.class.getName());
	}
	
	@BeforeMethod
	public void setup() {
		AtomicOperationUtils.createDefaultConsumer(true);		
	}
	
	@Test
	public void testReseourceSuccsesfullTransition() throws Exception{
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("ciResource1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				defaultUser);
		ResourceAssetStructure parsedCreatedResponse  = gson.fromJson(createResource.getResponse(), ResourceAssetStructure.class);
		BaseRestUtils.checkCreateResponse(createResource);
			
		//CHECKIN
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,defaultUser, 
				LifeCycleStatesEnum.CHECKIN,null);
		//testAudit(defaultUser,parsedCreatedResponse,"0.1",LifeCycleStatesEnum.CHECKOUT.name(),"201","OK");
		
		//CERTIFICATIONREQUEST
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,defaultUser, 
				LifeCycleStatesEnum.CERTIFICATIONREQUEST,null);	
		
		//CERTIFICATIONREQUEST
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 
				LifeCycleStatesEnum.STARTCERTIFICATION,null);	
		
		//CERTIFICATIONREQUEST
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 
				LifeCycleStatesEnum.CERTIFY,"1.0");				
	}
	
	@Test
	public void testVFCMTSuccsesfullTransitionDesigner() throws Exception{
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails resourceDetails = ElementFactory.getDefaultResourceByType("ci1", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VFCMT.toString());
		
		RestResponse restResponse = ResourceRestUtilsExternalAPI.createResource(resourceDetails, defaultUser);
		ResourceAssetStructure parsedCreatedResponse = gson.fromJson(restResponse.getResponse(), ResourceAssetStructure.class);
		
		BaseRestUtils.checkCreateResponse(restResponse);
			
		//CHECKIN
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.CHECKIN,null);	
		
		//CERTIFICATIONREQUEST
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.CERTIFY,"1.0");	
	}
	
	@Test
	public void testVFCMTSuccsesfullTransitionTester() throws Exception{
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails resourceDetails = ElementFactory.getDefaultResourceByType("ci1", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VFCMT.toString());
		
		RestResponse restResponse = ResourceRestUtilsExternalAPI.createResource(resourceDetails, defaultUser);
		ResourceAssetStructure parsedCreatedResponse = gson.fromJson(restResponse.getResponse(), ResourceAssetStructure.class);
		
		BaseRestUtils.checkCreateResponse(restResponse);
			
		//CHECKIN
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.CHECKIN,null);	
		
		//CERTIFICATIONREQUEST
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 
				LifeCycleStatesEnum.CERTIFY,"1.0");	
	}
	
	@Test
	public void testVFCMTFailSubmitForTesting() throws Exception{
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceExternalReqDetails resourceDetails = ElementFactory.getDefaultResourceByType("ci1", ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE, defaultUser.getUserId(), ResourceTypeEnum.VFCMT.toString());
		
		RestResponse restResponse = ResourceRestUtilsExternalAPI.createResource(resourceDetails, defaultUser);
		ResourceAssetStructure parsedCreatedResponse = gson.fromJson(restResponse.getResponse(), ResourceAssetStructure.class);
		
		BaseRestUtils.checkCreateResponse(restResponse);
			
		//CHECKIN
		testResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.CHECKIN,null);	
		
		//CERTIFICATIONREQUEST
		testFailResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.CERTIFICATIONREQUEST,400,ActionStatus.RESOURCE_VFCMT_LIFECYCLE_STATE_NOT_VALID.name(),Arrays.asList(LifeCycleStatesEnum.CERTIFICATIONREQUEST.getState()));	
		
		//CERTIFICATIONREQUEST
		testFailResourceTransitionOfLifeCycle(parsedCreatedResponse,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.STARTCERTIFICATION,400,ActionStatus.RESOURCE_VFCMT_LIFECYCLE_STATE_NOT_VALID.name(),Arrays.asList(LifeCycleStatesEnum.STARTCERTIFICATION.getState()));
	}
	
	//@Test
	public void testServiceSuccsesfullTransition() throws Exception{
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setName("ciService1");
		RestResponse createService = ServiceRestUtils.createService(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		ServiceAssetStructure parsedCreatedService  = gson.fromJson(createService.getResponse(), ServiceAssetStructure.class);
		BaseRestUtils.checkCreateResponse(createService);
			
		//CHECKIN
		testServiceTransitionOfLifeCycle(serviceDetails, parsedCreatedService,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.CHECKIN,null);	
		
		//CERTIFICATIONREQUEST
		testServiceTransitionOfLifeCycle(serviceDetails, parsedCreatedService,ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), 
				LifeCycleStatesEnum.CERTIFICATIONREQUEST,null);	
		
		//CERTIFICATIONREQUEST
		testServiceTransitionOfLifeCycle(serviceDetails, parsedCreatedService,ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 
				LifeCycleStatesEnum.STARTCERTIFICATION,null);	
		
		//CERTIFICATIONREQUEST
		testServiceTransitionOfLifeCycle(serviceDetails, parsedCreatedService,ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 
				LifeCycleStatesEnum.CERTIFY,"1.0");	
	}
	
	private void testFailResourceTransitionOfLifeCycle(ResourceAssetStructure parsedCreatedResponse, User user, 
			LifeCycleStatesEnum lifeCycleStatesEnum,int errorCode, String error,List<String> variables) throws IOException, JSONException {
		RestResponse response = LifecycleRestUtils.changeExternalResourceState(parsedCreatedResponse.getUuid(),user, lifeCycleStatesEnum);
		AssertJUnit.assertNotNull("check response object is not null", response);
		AssertJUnit.assertNotNull("check error code exists in response", response.getErrorCode());
		AssertJUnit.assertEquals("Check response code is succses", errorCode, response.getErrorCode().intValue());
		
		ErrorValidationUtils.checkBodyResponseOnError(error, variables,response.getResponse());
	}		

	private void testResourceTransitionOfLifeCycle(ResourceAssetStructure parsedCreatedResponse, User user, 
			LifeCycleStatesEnum lifeCycleStatesEnum,String version) throws IOException {
		RestResponse response = LifecycleRestUtils.changeExternalResourceState(parsedCreatedResponse.getUuid(),user, lifeCycleStatesEnum);
		AssertJUnit.assertNotNull("check response object is not null", response);
		AssertJUnit.assertNotNull("check error code exists in response", response.getErrorCode());
		AssertJUnit.assertEquals("Check response code is succses", 201, response.getErrorCode().intValue());
		ResourceAssetStructure parsedMetadata  = gson.fromJson(response.getResponse(), ResourceAssetStructure.class);
		
		Assert.assertEquals(parsedMetadata.getUuid(),parsedCreatedResponse.getUuid());
		Assert.assertEquals(parsedMetadata.getInvariantUUID(),parsedCreatedResponse.getInvariantUUID());
		Assert.assertEquals(parsedMetadata.getName(),parsedCreatedResponse.getName());
		if (version==null){
			Assert.assertEquals(parsedMetadata.getVersion(),parsedCreatedResponse.getVersion());
		} else {
			Assert.assertEquals(parsedMetadata.getVersion(),version);
		}
		Assert.assertEquals(parsedMetadata.getResourceType(),parsedCreatedResponse.getResourceType());
		Assert.assertEquals(parsedMetadata.getLifecycleState(),lifeCycleStatesEnum.getComponentState());
		Assert.assertEquals(parsedMetadata.getLastUpdaterUserId(),user.getUserId());
		Assert.assertEquals(parsedMetadata.getLastUpdaterFullName(),user.getFullName());
	}		
	
	private void testServiceTransitionOfLifeCycle(ServiceReqDetails resourceDetails,
			ServiceAssetStructure parsedCreatedResponse, User user, LifeCycleStatesEnum lifeCycleStatesEnum,String version) throws IOException {
		RestResponse response = LifecycleRestUtils.changeExternalServiceState(resourceDetails,user, lifeCycleStatesEnum);
		AssertJUnit.assertNotNull("check response object is not null", response);
		AssertJUnit.assertNotNull("check error code exists in response", response.getErrorCode());
		AssertJUnit.assertEquals("Check response code is succses", 201, response.getErrorCode().intValue());
		ServiceAssetStructure parsedMetadata  = gson.fromJson(response.getResponse(), ServiceAssetStructure.class);
		
		Assert.assertEquals(parsedMetadata.getUuid(),parsedCreatedResponse.getUuid());
		Assert.assertEquals(parsedMetadata.getInvariantUUID(),parsedCreatedResponse.getInvariantUUID());
		Assert.assertEquals(parsedMetadata.getName(),parsedCreatedResponse.getName());
		if (version==null){
			Assert.assertEquals(parsedMetadata.getVersion(),parsedCreatedResponse.getVersion());
		} else {
			Assert.assertEquals(parsedMetadata.getVersion(),version);
		}
		Assert.assertEquals(parsedMetadata.getLifecycleState(),lifeCycleStatesEnum.getComponentState());
		Assert.assertEquals(parsedMetadata.getLastUpdaterUserId(),user.getUserId());		
	}		
	
	private void testAudit(User modifier,ResourceAssetStructure resource,String prevVersion,String prevLifecycle,
			String errorCode, String error) throws Exception{
		DbUtils.cleanAllAudits();
		
		//ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(currResource.getActionStatus().name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		String auditAction = "ChangeLyfecycleByAPI";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setModifierUid(modifier.getUserId());
		expectedResourceAuditJavaObject.setModifierName(modifier.getFullName());
		expectedResourceAuditJavaObject.setResourceName(resource.getName());
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setPrevVersion(prevVersion);
		expectedResourceAuditJavaObject.setCurrVersion(resource.getVersion());
		expectedResourceAuditJavaObject.setPrevState(prevLifecycle);
		expectedResourceAuditJavaObject.setCurrState(resource.getLifecycleState());
		expectedResourceAuditJavaObject.setStatus(errorCode);
//		List<String> variables = (currResource.getErrorParams() != null ? currResource.getErrorParams() : new ArrayList<String>());
//		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(error);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resource.getName());
		AuditValidationUtils.validateAuditExternalChangeLifecycle(expectedResourceAuditJavaObject, auditAction,body);
	}
}
