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

package org.openecomp.sdc.ci.tests.execute.service;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentInstanceBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.ImportUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReqCapOccurrencesTest extends ComponentInstanceBaseTest {

	private ImportReqDetails importReqDetails1; // atomic resource
	private ImportReqDetails importReqDetails2;
	private ImportReqDetails importReqDetails3;
	private ImportReqDetails importReqDetails4;
	private Resource resourceVFC1;
	private Resource resourceVFC2;
	private Resource resourceVFC3;
	private Resource resourceVFC4;
	private ResourceReqDetails resourceDetailsVF100;
	private ResourceReqDetails resourceDetailsVF200;
	private Resource resourceVF100;
	private Resource resourceVF200;
	protected String testResourcesPath;

	protected final String importYmlWithReq11 = "softwareComponentReq11.yml";
	protected final String importYmlWithReq12 = "softwareComponentReq12.yml";
	protected final String importYmlWithCap11 = "computeCap11.yml";
	protected final String importYmlWithCap1Unbounded = "computeCap1UNBOUNDED.yml";
	protected final String capabilitiesAndRequirementsType = "tosca.capabilities.Container";

	public ReqCapOccurrencesTest() {
		super(new TestName(), ReqCapOccurrencesTest.class.getSimpleName());
	}

	@BeforeMethod
	public void before() throws Exception {
		// Do not use call init() from ComponentInstanceBaseTest
		expectedContainerCapabilities = new LinkedHashMap<String, List<CapabilityDefinition>>();
		expectedContainerRequirements = new LinkedHashMap<String, List<RequirementDefinition>>();
		removedRequirements = new HashMap<>();
		expectedContInstReqCap = new HashMap<>();

		RestResponse importResourceResponse;
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// import yml file location
		String sourceDir = config.getResourceConfigDir();
		final String workDir = "importToscaResourceByCreateUrl";
		testResourcesPath = sourceDir + File.separator + workDir;
		///// Create atomic resources /////////////////////////
		// import VFC1 with Requirements : MIN=1 MAX=2
		///// (tosca.capabilities.Container)
		importReqDetails1 = ElementFactory.getDefaultImportResource("VFC1");
		importResourceResponse = importedResource(importReqDetails1, importYmlWithReq12);
		// resourceVFC1 =
		// ResponseParser.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(importReqDetails1, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		resourceVFC1 = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		// import VFC2 with Capabilities : MIN 1 MAX UNBOUNDED
		// (tosca.capabilities.Container)
		importReqDetails2 = ElementFactory.getDefaultImportResource("VFC2");
		importResourceResponse = importedResource(importReqDetails2, importYmlWithCap1Unbounded);
		// resourceVFC2 =
		// ResponseParser.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		restResponse = LifecycleRestUtils.changeResourceState(importReqDetails2, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		resourceVFC2 = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		// import VFC3 with Capabilities : MIN 1 MAX 1
		// (tosca.capabilities.Container)
		importReqDetails3 = ElementFactory.getDefaultImportResource("VFC3");
		importResourceResponse = importedResource(importReqDetails3, importYmlWithCap11);
		// resourceVFC3 =
		// ResponseParser.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		restResponse = LifecycleRestUtils.changeResourceState(importReqDetails3, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		resourceVFC3 = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		// import VFC4 with Requirements : MIN 1 MAX 1
		// (tosca.capabilities.Container)
		importReqDetails4 = ElementFactory.getDefaultImportResource("VFC4");
		importResourceResponse = importedResource(importReqDetails4, importYmlWithReq11);
		// resourceVFC4 =
		// ResponseParser.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		restResponse = LifecycleRestUtils.changeResourceState(importReqDetails4, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		resourceVFC4 = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());

		// create VF100
		resourceDetailsVF100 = ElementFactory.getDefaultResourceByType("VF1000", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(),
				ResourceTypeEnum.VF.toString());
		RestResponse createResourceVF100 = ResourceRestUtils.createResource(resourceDetailsVF100, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceVF100);
		// create VF200
		resourceDetailsVF200 = ElementFactory.getDefaultResourceByType("VF2000", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(),
				ResourceTypeEnum.VF.toString());
		RestResponse createResourceVF200 = ResourceRestUtils.createResource(resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceVF200);
		// Create Service
		serviceDetails_01 = ElementFactory.getDefaultService("newtestservice1", ServiceCategoriesEnum.MOBILITY,
				sdncDesignerDetails.getUserId());
		RestResponse createServiceRestResponse = ServiceRestUtils.createService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createServiceRestResponse);

	}

	// US628514 Capability/Requirement "Occurrences" attribute in CREATE/DELETE
	// Relation APIs
	// Container = SERVICE , Container instance = VF
	@Test
	public void capAndReqOccurrencesInServiceAndHisInstancesNoAssociation() throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC1 (Req MIN=1 MAX=2) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC21 (Cap MIN=1 MAX=UNBOUNDED) to VF2000
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		RestResponse createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "1";
		String containerMaxReq = "2";
		String containerMinCap = "1";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
	}

	@Test
	public void serviceInstanceAssociationReqMaxOccurrencesNotReached() throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC1 (Req MIN=1 MAX=2) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC2 (Cap MIN=1 MAX=UNBOUNDED) to VF2000
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		RestResponse createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// associate 2 VFs
		String capType = capabilitiesAndRequirementsType;
		String reqName = "host";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList, componentInstanceReq.getUniqueId(), componentInstanceCap.getUniqueId());
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "1";
		String containerMinCap = "0";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
	}

	@Test
	public void serviceInstanceAssociationReqMaxOccurrencesIsReached() throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC4 (Req MIN=1 MAX=1) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails4, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC2 (Cap MIN=1 MAX=UNBOUNDED) to VF2000
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		RestResponse createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// associate 2 VFs
		String capType = capabilitiesAndRequirementsType;
		String reqName = "host";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList, componentInstanceReq.getUniqueId(), componentInstanceCap.getUniqueId());
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "0";
		String containerMinCap = "0";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
	}

	@Test
	public void associateServiceInstanceWhenReqMaxOccurrencesAlreadyReached() throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC4 (Req MIN=1 MAX=1) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails4, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC2 (Cap MIN=1 MAX=UNBOUNDED) to VF2.00
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		RestResponse createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String vf1Name = ResponseParser.getValueFromJsonResponse(createVFInstResp.getResponse(), "name");
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String vf2Name = ResponseParser.getValueFromJsonResponse(createVFInstResp.getResponse(), "name");
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// associate 2 VFs
		String capType = capabilitiesAndRequirementsType;
		String reqName = "host";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList, componentInstanceReq.getUniqueId(), componentInstanceCap.getUniqueId());
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		// associate same instances again - when requirement Max Occurrences
		// reached
		associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, sdncDesignerDetails,
				serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_NOT_FOUND,
				associateInstances.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(vf1Name);
		varibales.add(vf2Name);
		varibales.add("host");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND.name(), varibales,
				associateInstances.getResponse());
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "0";
		String containerMinCap = "0";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
	}

	@Test
	public void serviceInstanceAssociationCapMaxOccurrencesIsReached() throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC1 (Req MIN=1 MAX=2) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC3 (Cap MIN=1 MAX=1) to VF2000
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails3, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		RestResponse createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// associate 2 VFs
		String capType = capabilitiesAndRequirementsType;
		String reqName = "host";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList, componentInstanceReq.getUniqueId(), componentInstanceCap.getUniqueId());
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "1";
		String containerMinCap = "0";
		String containerMaxCap = "0";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
	}

	@Test
	public void associationServiceInstanceWhenCapMaxOccurrencesAlreadyReached() throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC1 (Req MIN=1 MAX=2) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC3 (Cap MIN=1 MAX=1) to VF2000
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails3, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		RestResponse createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
				sdncDesignerDetails);
		// RestResponse createVFInstResp = createVFInstance(serviceDetails_01,
		// resourceDetailsVF100, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String vf1Name = ResponseParser.getValueFromJsonResponse(createVFInstResp.getResponse(), "name");
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		// createVFInstResp = createVFInstance(serviceDetails_01,
		// resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String vf2Name = ResponseParser.getValueFromJsonResponse(createVFInstResp.getResponse(), "name");
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// associate 2 VFs
		String capType = capabilitiesAndRequirementsType;
		String reqName = "host";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList, componentInstanceReq.getUniqueId(), componentInstanceCap.getUniqueId());
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "1";
		String containerMinCap = "0";
		String containerMaxCap = "0";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
		// associate same instances again - when requirement Max Occurrences
		// reached
		associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, sdncDesignerDetails,
				serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_NOT_FOUND,
				associateInstances.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(vf1Name);
		varibales.add(vf2Name);
		varibales.add("host");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND.name(),
				varibales, associateInstances.getResponse());
	}

	@Test
	public void associationAndDisassociateServiceInstancesWhenReqMaxOccurrencesAlreadyReached()
			throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC4 (Req MIN=1 MAX=1) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails4, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC3 (Cap MIN=1 MAX=1) to VF2000
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails3, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		RestResponse createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
				sdncDesignerDetails);
		// RestResponse createVFInstResp = createVFInstance(serviceDetails_01,
		// resourceDetailsVF100, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		// createVFInstResp = createVFInstance(serviceDetails_01,
		// resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// associate 2 VF Instances
		String capType = capabilitiesAndRequirementsType;
		String reqName = "host";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList, componentInstanceReq.getUniqueId(), componentInstanceCap.getUniqueId());
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "0";
		String containerMinCap = "0";
		String containerMaxCap = "0";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
		// Disassociate 2 VF Instances
		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS,
				dissociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF100.getUniqueId()).isEmpty());
		// get service and verify Occurrences in container and container
		// instance requirements and Capabilities
		getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		containerMinReq = "1";
		containerMaxReq = "1";
		containerMinCap = "1";
		containerMaxCap = "1";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
	}

	@Test(enabled = false)
	public void aaaa() throws Exception, Exception {
		RestResponse getResourseRestResponse;
		// Add instance of VFC1 (Req MIN=1 MAX=2) to VF1000
		ComponentInstance componentInstanceReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceReq);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Add instance of VFC3 (Cap MIN=1 MAX=1) to VF2000
		ComponentInstance componentInstanceCap = createComponentInstance(importReqDetails3, sdncDesignerDetails,
				resourceDetailsVF200);
		assertNotNull(componentInstanceCap);
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF200.getUniqueId());
		resourceVF200 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Check-In both VFs
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF100, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF200, sdncDesignerDetails,
				LifeCycleStatesEnum.CHECKIN);
		ResourceRestUtils.checkSuccess(restResponse);
		// Create VF instances
		// RestResponse createVFInstResp =
		// createVFInstanceDuringSetup(serviceDetails_01, resourceDetailsVF100,
		// sdncDesignerDetails);
		RestResponse createVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF100, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String vf1Name = ResponseParser.getValueFromJsonResponse(createVFInstResp.getResponse(), "name");
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// createVFInstResp = createVFInstanceDuringSetup(serviceDetails_01,
		// resourceDetailsVF200, sdncDesignerDetails);
		createVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF200, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String vf2Name = ResponseParser.getValueFromJsonResponse(createVFInstResp.getResponse(), "name");
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// associate 2 VFs
		String capType = capabilitiesAndRequirementsType;
		String reqName = "host";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList, componentInstanceReq.getUniqueId(), componentInstanceCap.getUniqueId());
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		getComponentAndValidateRIs(serviceDetails_01, 2, 1);
		// get service
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkSuccess(getServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "1";
		String containerMinCap = "0";
		String containerMaxCap = "0";
		verifyContainerCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType, containerMinReq,
				containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(service, capabilitiesAndRequirementsType,
				resourceVF200, resourceVF100);
		// associate same instances again - when requirement Max Occurrences
		// reached
		associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, sdncDesignerDetails,
				serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_NOT_FOUND,
				associateInstances.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(vf1Name);
		varibales.add(vf2Name);
		varibales.add("host");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND.name(),
				varibales, associateInstances.getResponse());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// US628514 Capability/Requirement "Occurrences" attribute in CREATE/DELETE
	////////////////////////////////////////////////////////////////////////////////////////////////// Relation
	////////////////////////////////////////////////////////////////////////////////////////////////// APIs
	// Container = VF , Container instance = VFC
	@Test
	public void capAndReqOccurrencesInVfAndHisInstancesNoAssociation() throws Exception, Exception {
		// Add VFC1 and VFC2 instances in VF
		ComponentInstance createComponentInstance1 = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(createComponentInstance1);
		ComponentInstance createComponentInstance2 = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(createComponentInstance2);
		// GET resource
		RestResponse getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "1";
		String containerMaxReq = "2";
		String containerMinCap = "1";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC2, resourceVFC1);
	}

	@Test
	public void vfInstanceAssociationReqMaxOccurrencesNotReached() throws Exception, Exception {
		// Add VFC1 (with Requirements: tosca.capabilities.Container, MIN=1
		// MAX=2) instance to VF
		ComponentInstance componentInstanceWithReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithReq);
		// Add VFC2 (with Capabilities: tosca.capabilities.Container, MIN=1,
		// MAX=UNBOUNDED ) instance to VF
		ComponentInstance componentInstanceWithCap = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithCap);
		// associate Instances
		CapReqDef capReqDefBeforeAssociate = getResourceReqCap(resourceDetailsVF100);
		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(componentInstanceWithReq,
				componentInstanceWithCap, capReqDefBeforeAssociate);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF100.getUniqueId()));
		// GET resource
		RestResponse getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "1";
		String containerMinCap = "0";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC2, resourceVFC1);

	}

	@Test
	public void vfInstanceAssociationReqMaxOccurrencesIsReached() throws Exception, Exception {
		// Add VFC4 (with Requirements: tosca.capabilities.Container, MIN=1
		// MAX=1) instance to VF
		ComponentInstance componentInstanceWithReq = createComponentInstance(importReqDetails4, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithReq);
		// Add VFC2 (with Capabilities: tosca.capabilities.Container, MIN=1,
		// MAX=UNBOUNDED ) instance to VF
		ComponentInstance componentInstanceWithCap = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithCap);
		// associate Instances
		CapReqDef capReqDefBeforeAssociate = getResourceReqCap(resourceDetailsVF100);
		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(componentInstanceWithReq,
				componentInstanceWithCap, capReqDefBeforeAssociate);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF100.getUniqueId()));
		// GET resource
		RestResponse getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "0";
		String containerMinCap = "0";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC2, resourceVFC4);
	}

	@Test
	public void associateVfInstanceWhenReqMaxOccurrencesAlreadyReached() throws Exception, Exception {
		// Add VFC4 (with Requirements: tosca.capabilities.Container, MIN=1
		// MAX=1) instance to VF
		ComponentInstance componentInstanceWithReq = createComponentInstance(importReqDetails4, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithReq);
		// Add VFC2 (with Capabilities: tosca.capabilities.Container, MIN=1,
		// MAX=UNBOUNDED ) instance to VF
		ComponentInstance componentInstanceWithCap = createComponentInstance(importReqDetails2, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithCap);
		// associate Instances
		CapReqDef capReqDefBeforeAssociate = getResourceReqCap(resourceDetailsVF100);
		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(componentInstanceWithReq,
				componentInstanceWithCap, capReqDefBeforeAssociate);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF100.getUniqueId()));
		// GET resource
		RestResponse getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "0";
		String containerMinCap = "0";
		String containerMaxCap = "UNBOUNDED";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC2, resourceVFC4);
		// associate same instances again - when requirement Max Occurrences
		// reached
		associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_NOT_FOUND,
				associateInstances.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(componentInstanceWithReq.getName());
		varibales.add(componentInstanceWithCap.getName());
		varibales.add("host");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND.name(), varibales,
				associateInstances.getResponse());

	}

	@Test
	public void vfInstanceAssociationCapMaxOccurrencesIsReached() throws Exception, Exception {
		// Add VFC1 (with Requirements: tosca.capabilities.Container, MIN=1
		// MAX=2) instance to VF
		ComponentInstance componentInstanceWithReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithReq);
		// Add VFC3 (with Capabilities: tosca.capabilities.Container, MIN=1
		// MAX=1 ) instance to VF
		ComponentInstance componentInstanceWithCap = createComponentInstance(importReqDetails3, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithCap);
		// associate Instances
		CapReqDef capReqDefBeforeAssociate = getResourceReqCap(resourceDetailsVF100);
		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(componentInstanceWithReq,
				componentInstanceWithCap, capReqDefBeforeAssociate);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF100.getUniqueId()));
		// GET resource
		RestResponse getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "1";
		String containerMinCap = "0";
		String containerMaxCap = "0";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC3, resourceVFC1);
	}

	@Test
	public void associationVfInstanceWhenCapMaxOccurrencesAlreadyReached() throws Exception, Exception {
		// Add VFC1 (with Requirements: tosca.capabilities.Container, MIN=1
		// MAX=2) instance to VF
		ComponentInstance componentInstanceWithReq = createComponentInstance(importReqDetails1, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithReq);
		// Add VFC3 (with Capabilities: tosca.capabilities.Container, MIN=1
		// MAX=1 ) instance to VF
		ComponentInstance componentInstanceWithCap = createComponentInstance(importReqDetails3, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithCap);
		// associate Instances
		CapReqDef capReqDefBeforeAssociate = getResourceReqCap(resourceDetailsVF100);
		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(componentInstanceWithReq,
				componentInstanceWithCap, capReqDefBeforeAssociate);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF100.getUniqueId()));
		// GET resource
		RestResponse getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "1";
		String containerMinCap = "0";
		String containerMaxCap = "0";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC3, resourceVFC1);
		// associate same instances again - when requirement Max Occurrences
		// reached
		associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_NOT_FOUND,
				associateInstances.getErrorCode().intValue());
		ArrayList<String> varibales = new ArrayList<String>();
		varibales.add(componentInstanceWithReq.getName());
		varibales.add(componentInstanceWithCap.getName());
		varibales.add("host");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND.name(),
				varibales, associateInstances.getResponse());
	}

	@Test
	public void associationAndDisassociateVfInstancesWhenReqMaxOccurrencesAlreadyReached() throws Exception, Exception {
		// Add VFC4 (with Requirements: tosca.capabilities.Container, MIN=1
		// MAX=1) instance to VF
		ComponentInstance componentInstanceWithReq = createComponentInstance(importReqDetails4, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithReq);
		// Add VFC3 (with Capabilities: tosca.capabilities.Container, MIN=1
		// MAX=1 ) instance to VF
		ComponentInstance componentInstanceWithCap = createComponentInstance(importReqDetails3, sdncDesignerDetails,
				resourceDetailsVF100);
		assertNotNull(componentInstanceWithCap);
		// associate Instances
		CapReqDef capReqDefBeforeAssociate = getResourceReqCap(resourceDetailsVF100);
		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(componentInstanceWithReq,
				componentInstanceWithCap, capReqDefBeforeAssociate);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(associateInstances);
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF100.getUniqueId()));
		// GET resource
		RestResponse getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		String containerMinReq = "0";
		String containerMaxReq = "0";
		String containerMinCap = "0";
		String containerMaxCap = "0";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC3, resourceVFC4);
		// Disassociate 2 Instances
		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF100.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS,
				dissociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF100.getUniqueId()).isEmpty());
		// GET resource
		getResourseRestResponse = ResourceRestUtils.getResource(sdncDesignerDetails,
				resourceDetailsVF100.getUniqueId());
		resourceVF100 = ResponseParser.parseToObjectUsingMapper(getResourseRestResponse.getResponse(), Resource.class);
		// Verify Container requirements and Capabilities
		containerMinReq = "1";
		containerMaxReq = "1";
		containerMinCap = "1";
		containerMaxCap = "1";
		verifyContainerCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				containerMinReq, containerMaxReq, containerMinCap, containerMaxCap);
		verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(resourceVF100, capabilitiesAndRequirementsType,
				resourceVFC3, resourceVFC4);
	}

	///////////////////////////////////////

	private boolean checkRealtionship(String fromNode, String toNode, String resourceUniqueId) throws Exception {
		List<RequirementCapabilityRelDef> componentInstancesRelations = getComponentInstancesRelations(
				resourceUniqueId);
		RequirementCapabilityRelDef requirementCapabilityRelDef = componentInstancesRelations.get(0);
		boolean fromNodeCheck = requirementCapabilityRelDef.getFromNode().equals(fromNode);
		boolean toNodeCheck = requirementCapabilityRelDef.getToNode().equals(toNode);

		return fromNodeCheck && toNodeCheck;
	}

	private List<RequirementCapabilityRelDef> getComponentInstancesRelations(String resourceUniqueId)
			throws ClientProtocolException, IOException {
		Resource resource = getVfAsResourceObject(resourceUniqueId);
		List<RequirementCapabilityRelDef> componenRelationInstances = resource.getComponentInstancesRelations();

		return componenRelationInstances;
	}

	private Resource getVfAsResourceObject(String resourceUniqueId) throws ClientProtocolException, IOException {
		RestResponse getResource = ResourceRestUtils.getResource(resourceUniqueId);
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		return resource;
	}

	private RequirementCapabilityRelDef setRelationshipBetweenInstances(ComponentInstance riReq,
			ComponentInstance riCap, CapReqDef capReqDef) throws Exception {

		String capbilityUid = capReqDef.getCapabilities().get("tosca.capabilities.Container").get(0).getUniqueId();
		String requirementUid = capReqDef.getRequirements().get("tosca.capabilities.Container").get(0).getUniqueId();

		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		requirementDef.setFromNode(riReq.getUniqueId());
		requirementDef.setToNode(riCap.getUniqueId());

		RelationshipInfo pair = new RelationshipInfo();
		pair.setRequirementOwnerId(riReq.getUniqueId());
		pair.setCapabilityOwnerId(riCap.getUniqueId());
		pair.setRequirement("host");
		RelationshipImpl relationship = new RelationshipImpl();
		relationship.setType("tosca.capabilities.Container");
		pair.setRelationships(relationship);
		pair.setCapabilityUid(capbilityUid);
		pair.setRequirementUid(requirementUid);
		List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
		CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
		relationships.add(capReqRel);
		capReqRel.setRelation(pair);
		requirementDef.setRelationships(relationships);
		return requirementDef;
	}

	private CapReqDef getResourceReqCap(ResourceReqDetails res) throws IOException {
		RestResponse getResourceBeforeAssociate = ComponentRestUtils
				.getComponentRequirmentsCapabilities(sdncDesignerDetails, resourceDetailsVF100);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceBeforeAssociate.getResponse(), CapReqDef.class);
		return capReqDef;
	}

	private RestResponse importedResource(ImportReqDetails importReqDetails, String ymlFile) throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFile);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails,
				sdncDesignerDetails, null);
		assertEquals("Check response code after importing resource", BaseRestUtils.STATUS_CODE_CREATED,
				importResourceResponse.getErrorCode().intValue());
		return importResourceResponse;
	}

	private ComponentInstance createComponentInstance(ResourceReqDetails res, User user, ResourceReqDetails vf)
			throws Exception {
		RestResponse response = ResourceRestUtils.createResourceInstance(res, user, vf.getUniqueId());
		ResourceRestUtils.checkCreateResponse(response);
		ComponentInstance compInstance = ResponseParser.parseToObject(response.getResponse(), ComponentInstance.class);
		return compInstance;
	}

	private void verifyContainerCapabilitiesAndRequirementsOccurrences(Component component,
			String CapabilitiesAndRequirementsType, String minReqOccurrences, String maxReqOccurrences,
			String minCapabilities, String maxCapabilities) throws Exception {
		boolean isRequirementAppear = false;
		boolean isCapabilityAppear = false;
		List<RequirementDefinition> requirements;
		List<CapabilityDefinition> capabilities;
		requirements = component.getRequirements().get(CapabilitiesAndRequirementsType);
		if (maxReqOccurrences == "0") {
			assertTrue(requirements == null);
		} // if container MAX requirement = 0
		if (maxReqOccurrences != "0") {
			assertNotNull(requirements);
			for (RequirementDefinition req : requirements) {
				switch (req.getName()) {
				case "host":
					assertTrue("Check Min Requirement Occurrences ", req.getMinOccurrences().equals(minReqOccurrences));
					assertTrue("Check Max Requirement Occurrences ", req.getMaxOccurrences().equals(maxReqOccurrences));
					isRequirementAppear = true;
					break;
				}
				assertTrue(isRequirementAppear);
				isRequirementAppear = false;
			}
		}
		// Container Capabilities
		capabilities = component.getCapabilities().get(CapabilitiesAndRequirementsType);
		if (maxCapabilities == "0") {// if container MAX capabilities = 0
			assertTrue(capabilities == null);
		}
		if (maxCapabilities != "0") {
			assertNotNull(capabilities);
			for (CapabilityDefinition cap : capabilities) {
				switch (cap.getName()) {
				case "host":
					assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals(minCapabilities));
					assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals(maxCapabilities));
					isCapabilityAppear = true;
					break;
				}
				assertTrue(isCapabilityAppear);
				isCapabilityAppear = false;
			}
		}

	}

	private void verifyContainerInstanceCapabilitiesAndRequirementsOccurrences(Component component,
			String CapabilitiesAndRequirementsType, Resource vfWithCapabilities, Resource vfWithRequirements)
			throws Exception {
		boolean isCapReqAppear = false;
		List<ComponentInstance> listOfComponentInstances = component.getComponentInstances();

		for (ComponentInstance instance : listOfComponentInstances) {
			if (instance.getComponentUid().equals(vfWithCapabilities.getUniqueId())) {
				List<CapabilityDefinition> capFromResource = vfWithCapabilities.getCapabilities()
						.get(CapabilitiesAndRequirementsType);
				List<CapabilityDefinition> capFromInstance = instance.getCapabilities()
						.get(CapabilitiesAndRequirementsType);
				for (CapabilityDefinition resourceCap : capFromResource)
					for (CapabilityDefinition instanceReq : capFromInstance) {
						if (resourceCap.getUniqueId().equals(instanceReq.getUniqueId())) {
							assertTrue("Check Min capability Occurrences ",
									resourceCap.getMinOccurrences().equals(instanceReq.getMinOccurrences()));
							assertTrue("Check Max capability Occurrences ",
									resourceCap.getMaxOccurrences().equals(instanceReq.getMaxOccurrences()));
							isCapReqAppear = true;
							break;
						}

					}
			}

			if (instance.getComponentUid().equals(vfWithRequirements.getUniqueId())) {
				List<RequirementDefinition> reqFromAtomicResource = vfWithRequirements.getRequirements()
						.get(CapabilitiesAndRequirementsType);
				List<RequirementDefinition> reqFromInstance = instance.getRequirements()
						.get(CapabilitiesAndRequirementsType);
				for (RequirementDefinition resourceReq : reqFromAtomicResource)
					for (RequirementDefinition instanceReq : reqFromInstance) {
						if (resourceReq.getUniqueId().equals(instanceReq.getUniqueId())) {
							assertTrue("Check Min Requirement Occurrences ",
									resourceReq.getMinOccurrences().equals(instanceReq.getMinOccurrences()));
							assertTrue("Check Max Requirement Occurrences ",
									resourceReq.getMaxOccurrences().equals(instanceReq.getMaxOccurrences()));
							isCapReqAppear = true;
							break;
						}
					}
			}
			assertTrue(isCapReqAppear);
			isCapReqAppear = false;
		}

	}

	private RequirementCapabilityRelDef getReqCapRelation(String reqCompInstId, String capCompInstId, String capType,
			String reqName, List<CapabilityDefinition> capList, List<RequirementDefinition> reqList,
			String vfc1UniqueId, String vfc2UniqueId) {
		return ElementFactory.getReqCapRelation(reqCompInstId, capCompInstId, vfc1UniqueId, vfc2UniqueId, capType,
				reqName, capList, reqList);
	}

}
