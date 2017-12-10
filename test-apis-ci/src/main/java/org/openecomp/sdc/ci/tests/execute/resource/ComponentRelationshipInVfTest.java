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

package org.openecomp.sdc.ci.tests.execute.resource;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ComponentRelationshipInVfTest extends ComponentBaseTest {

	public ComponentRelationshipInVfTest() {
		super(new TestName(), ComponentRelationshipInVfTest.class.getName());
	}

	private ResourceReqDetails resourceDetailsVF;
	private User designerUser;
	private User adminUser;
	private User testerUser;
	private ResourceReqDetails resourceDetailsReq;
	private ResourceReqDetails resourceDetailsCap;

	@BeforeMethod
	public void before() throws Exception {
		designerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		adminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		testerUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);

		resourceDetailsVF = ElementFactory.getDefaultResourceByType("VF100", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, designerUser.getUserId(), ResourceTypeEnum.VF.toString());
		createResource(resourceDetailsVF, designerUser);

		resourceDetailsReq = ElementFactory.getDefaultResourceByType("SoftCompRouter",
				NormativeTypesEnum.SOFTWARE_COMPONENT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS,
				designerUser.getUserId(), ResourceTypeEnum.CP.toString()); // resourceType
																			// =
																			// VFC
		resourceDetailsCap = ElementFactory.getDefaultResourceByType("MyCompute", NormativeTypesEnum.COMPUTE,
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, designerUser.getUserId(), ResourceTypeEnum.CP.toString()); // resourceType
																														// =
																														// VFC

	}

	private void createResource(ResourceReqDetails resourceDetails, User user) throws Exception, IOException {
		RestResponse createResourceResponse = ResourceRestUtils.createResource(resourceDetails, user);
		ResourceRestUtils.checkCreateResponse(createResourceResponse);
		if (!resourceDetails.getResourceType().equals("VF"))
			LifecycleRestUtils.changeResourceState(resourceDetails, user, "0.1", LifeCycleStatesEnum.CHECKIN);
	}

	private void createAtomicResource(ResourceReqDetails resourceDetails, User user) throws Exception {
		createResource(resourceDetails, user);
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

	private ComponentInstance createComponentInstance(ResourceReqDetails res) throws Exception {
		return createComponentInstance(res, designerUser);
	}

	private ComponentInstance createComponentInstance(ResourceReqDetails res, User user, ResourceReqDetails vf)
			throws Exception {
		RestResponse response = ResourceRestUtils.createResourceInstance(res, user, vf.getUniqueId());
		ResourceRestUtils.checkCreateResponse(response);
		ComponentInstance compInstance = ResponseParser.parseToObject(response.getResponse(), ComponentInstance.class);
		return compInstance;
	}

	private ComponentInstance createComponentInstance(ResourceReqDetails res, User user) throws Exception {
		return createComponentInstance(res, user, resourceDetailsVF);
	}

	private void createTwoAtomicResourcesByType(String reqType, String capType, User user1, User user2)
			throws Exception {
		resourceDetailsReq.setResourceType(reqType);
		createAtomicResource(resourceDetailsReq, user1);
		resourceDetailsCap.setResourceType(capType);
		createAtomicResource(resourceDetailsCap, user2);
	}

	private void createTwoAtomicResourcesByType(String reqType, String capType) throws Exception {
		createTwoAtomicResourcesByType(reqType, capType, designerUser, designerUser);
	}

	@Test
	public void associateInVF() throws Exception {

		createTwoAtomicResourcesByType(ResourceTypeEnum.VFC.toString(), ResourceTypeEnum.VFC.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDef = getResourceReqCap();

		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get("tosca.capabilities.Container");
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get("tosca.capabilities.Container");

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
		pair.setCapabilityUid(capList.get(0).getUniqueId());
		pair.setRequirementUid(reqList.get(0).getUniqueId());
		List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
		CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
		relationships.add(capReqRel);
		capReqRel.setRelation(pair);
		requirementDef.setRelationships(relationships);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());

		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(designerUser,
				resourceDetailsVF);
		capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);

		List<RequirementDefinition> list = capReqDef.getRequirements().get("tosca.capabilities.Container");
		assertEquals("Check requirement", null, list);

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, dissociateInstances.getErrorCode().intValue());

		getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(designerUser, resourceDetailsVF);
		capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);

		list = capReqDef.getRequirements().get("tosca.capabilities.Container");
		assertEquals("Check requirement", 1, list.size());
	}

	//////////////////////////////// Q A //////////////////////////////
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

	private List<ComponentInstance> getComponentInstancesList(String resourceUniqueId) throws Exception {
		Resource resource = getVfAsResourceObject(resourceUniqueId);
		List<ComponentInstance> componentInstances = resource.getComponentInstances();
		return componentInstances;
	}

	@Test
	public void associateCpToCpTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.CP.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		CapReqDef capReqDef = getResourceReqCap();

		requirementsBeforeAssociate.remove("tosca.capabilities.Container");
		assertTrue(capReqDef.getRequirements().equals(requirementsBeforeAssociate));

		List<CapabilityDefinition> list = capabilitiesBeforeAssociate.get("tosca.capabilities.Container");
		for (CapabilityDefinition cap : list) {
			cap.setMinOccurrences("0");
		}

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));
	}

	private CapReqDef getResourceReqCap(ResourceReqDetails res) throws IOException {
		RestResponse getResourceBeforeAssociate = ComponentRestUtils.getComponentRequirmentsCapabilities(designerUser,
				resourceDetailsVF);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceBeforeAssociate.getResponse(), CapReqDef.class);
		return capReqDef;
	}

	private CapReqDef getResourceReqCap() throws IOException {
		return getResourceReqCap(resourceDetailsVF);
	}

	@Test
	public void associateCpToVLTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		CapReqDef capReqDef = getResourceReqCap();

		requirementsBeforeAssociate.remove("tosca.capabilities.Container");
		assertTrue(capReqDef.getRequirements().equals(requirementsBeforeAssociate));

		List<CapabilityDefinition> list = capabilitiesBeforeAssociate.get("tosca.capabilities.Container");
		for (CapabilityDefinition cap : list) {
			cap.setMinOccurrences("0");
		}

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));

	}

	// Error handling
	// ELLA - more informative error
	@Test
	public void associateCpToVlInVFCTest() throws Exception {
		ResourceReqDetails vfcDetails = ElementFactory.getDefaultResourceByType("VFC100", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, designerUser.getUserId(), ResourceTypeEnum.VFC.toString());
		RestResponse createVfcResponse = ResourceRestUtils.createResource(vfcDetails, designerUser);
		ResourceRestUtils.checkCreateResponse(createVfcResponse);

		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				vfcDetails.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 400, associateInstances.getErrorCode().intValue());

		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "SoftCompRouter 1",
		// "MyCompute 2",
		// "host"
		// ]
	}

	// Error handling
	@Test
	public void associateCpToVfTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riCapInVfInstance = createComponentInstance(resourceDetailsCap, designerUser,
				resourceDetailsVF);
		ComponentInstance riReqInVfInstance = createComponentInstance(resourceDetailsReq, designerUser,
				resourceDetailsVF);

		ResourceReqDetails vfHigh = new ResourceReqDetails(resourceDetailsVF, "0.1");
		vfHigh.setName("vfHigh");
		vfHigh.setTags(new ArrayList<String>(Arrays.asList(vfHigh.getName())));
		vfHigh.setResourceType(ResourceTypeEnum.VF.toString());
		createResource(vfHigh, designerUser);

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq, designerUser, vfHigh);
		LifecycleRestUtils.changeResourceState(resourceDetailsVF, designerUser, "0.1", LifeCycleStatesEnum.CHECKIN);
		ComponentInstance riCap = createComponentInstance(resourceDetailsVF, designerUser, vfHigh);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 409, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				associateInstances.getResponse());
		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "SoftCompRouter 1",
		// "VF100 2",
		// "host"
		// ]
	}

	// Error handling
	@Test
	public void associateVfcToVfcNotFoundTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.VFC.toString(), ResourceTypeEnum.VFC.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);
		riCap.setUniqueId("123");

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 400, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_BAD_REQUEST.name(),
				new ArrayList<String>(), associateInstances.getResponse());

		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "SoftCompRouter 1",
		// "012f6dcd-bcdf-4d9b-87be-ff1442b95831.5d265453-0b6a-4453-8f3d-57a253b88432.softcomprouter1",
		// "host"
	}

	@Test
	public void associateCpToDeletedVfcTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VFC.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse deleteResourceResponse = ResourceRestUtils.deleteResource(resourceDetailsCap.getUniqueId(),
				designerUser.getUserId());
		ResourceRestUtils.checkDeleteResponse(deleteResourceResponse);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

	}

	@Test
	public void associateCpToDeletedVlTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse deleteResourceResponse = ResourceRestUtils.deleteResource(resourceDetailsCap.getUniqueId(),
				designerUser.getUserId());
		ResourceRestUtils.checkDeleteResponse(deleteResourceResponse);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

	}

	@Test
	public void associateCpToDeletedCpTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.CP.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse deleteResourceResponse = ResourceRestUtils.deleteResource(resourceDetailsCap.getUniqueId(),
				designerUser.getUserId());
		ResourceRestUtils.checkDeleteResponse(deleteResourceResponse);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

	}

	// Error handling
	@Test
	public void associateCpToDeletedCpInstanceTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.CP.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse deleteComponentInstance = ComponentInstanceRestUtils.deleteComponentInstance(designerUser,
				resourceDetailsVF.getUniqueId(), riReq.getUniqueId(), ComponentTypeEnum.RESOURCE);
		ComponentInstanceRestUtils.checkDeleteResponse(deleteComponentInstance);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 400, associateInstances.getErrorCode().intValue());

		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "7d6aca08-9321-4ea1-a781-c52c8214a30e.c0e63466-5283-44d8-adff-365c0885a6ba.softcomprouter1",
		// "MyCompute 2",
		// "host"
		// ]
	}

	// Error handling
	@Test
	public void associateVfcToDeletedVFCInstanceTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.VFC.toString(), ResourceTypeEnum.VFC.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse deleteComponentInstance = ComponentInstanceRestUtils.deleteComponentInstance(designerUser,
				resourceDetailsVF.getUniqueId(), riReq.getUniqueId(), ComponentTypeEnum.RESOURCE);
		ComponentInstanceRestUtils.checkDeleteResponse(deleteComponentInstance);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 400, associateInstances.getErrorCode().intValue());

		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "7d6aca08-9321-4ea1-a781-c52c8214a30e.c0e63466-5283-44d8-adff-365c0885a6ba.softcomprouter1",
		// "MyCompute 2",
		// "host"
		// ]
	}

	@Test
	public void associateWithDifferentOwnerOfVf() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2), resourceDetailsVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 409, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				associateInstances.getResponse());

		CapReqDef capReqDef = getResourceReqCap();

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsAfterAssociate = capReqDef.getRequirements();

		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));
		assertTrue(requirementsAfterAssociate.equals(requirementsBeforeAssociate));
	}

	@Test
	public void associateWithTester() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), resourceDetailsVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 409, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				associateInstances.getResponse());

		CapReqDef capReqDef = getResourceReqCap();

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsAfterAssociate = capReqDef.getRequirements();

		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));
		assertTrue(requirementsAfterAssociate.equals(requirementsBeforeAssociate));
	}

	// Error handling
	@Test
	public void associateCpToVLIntoVFNotFound() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		String uidNotFound = "123";
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				uidNotFound, ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 404, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(),
				new ArrayList<String>(Arrays.asList("")), associateInstances.getResponse());

		// {"serviceException":{"messageId":"SVC4063","text":"Error: Requested
		// '%1' resource was not found.","variables":[""]}}}
	}

	// Error Handling
	@Test
	public void associateCpToVlWithMissingUid() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		requirementDef.setToNode("");
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 400, associateInstances.getErrorCode().intValue());
		// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(),
		// new ArrayList<String>(), associateInstances.getResponse());

		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "SoftCompRouter 1",
		// "fd3a689b-fa1c-4105-933d-d1310e642f05.95bce626-ce73-413b-8c14-2388d1589d5c.softcomprouter1",
		// "host"
		// ]
	}

	@Test
	public void associateInServiceWithUidOfVf() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals("Check response code ", 404, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(),
				new ArrayList<String>(Arrays.asList("")), associateInstances.getResponse());
	}

	@Test
	public void associateCpToVl_DifferentOwners() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString(), designerUser,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2));

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		CapReqDef capReqDef = getResourceReqCap();

		requirementsBeforeAssociate.remove("tosca.capabilities.Container");
		assertTrue(capReqDef.getRequirements().equals(requirementsBeforeAssociate));

		List<CapabilityDefinition> list = capabilitiesBeforeAssociate.get("tosca.capabilities.Container");
		for (CapabilityDefinition cap : list) {
			cap.setMinOccurrences("0");
		}

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));
	}

	@Test(enabled = false)
	public void associateToNotCheckedoutVf() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse changeResourceStateToCheckin = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckin);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 409, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				associateInstances.getResponse());

		CapReqDef capReqDef = getResourceReqCap();
		assertTrue(capReqDef.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDef.getCapabilities().equals(capabilitiesBeforeAssociate));

		String firstUniqueId = resourceDetailsVF.getUniqueId();

		// checkout

		RestResponse changeResourceStateToCheckout = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckout);
		String secondUniqueId = resourceDetailsVF.getUniqueId();

		CapReqDef capReqDefAfterFirstCheckout = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesAfterFirstCheckout = capReqDefAfterFirstCheckout
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsAfterFirstCheckout = capReqDefAfterFirstCheckout
				.getRequirements();

		requirementDef = setUidsOfInstancesAfterLifecycleStateChange(riReq, riCap, capReqDefBeforeAssociate);

		RestResponse firstAssociateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				designerUser, resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, firstAssociateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		CapReqDef capReqDefAfterFirstAssociate = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesAfterFirstAssociate = capReqDefAfterFirstAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsAfterFirstAssociate = capReqDefAfterFirstAssociate
				.getRequirements();

		requirementsAfterFirstCheckout.remove("tosca.capabilities.Container");
		assertTrue(requirementsAfterFirstAssociate.equals(requirementsAfterFirstCheckout));
		assertTrue(capabilitiesAfterFirstAssociate.equals(capabilitiesAfterFirstCheckout));

		resourceDetailsVF.setUniqueId(firstUniqueId);
		CapReqDef capReqDefOfFirstVersion = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesOfFirstVersion = capReqDefOfFirstVersion.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsOfFirstVersion = capReqDefOfFirstVersion.getRequirements();

		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());
		assertTrue(requirementsBeforeAssociate.equals(requirementsOfFirstVersion));
		assertTrue(capabilitiesBeforeAssociate.equals(capabilitiesOfFirstVersion));

		// checkin-checkout
		resourceDetailsVF.setUniqueId(secondUniqueId);
		RestResponse changeResourceStateToCheckin2 = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckin2);
		RestResponse changeResourceStateToCheckout2 = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckout2);

		List<RequirementCapabilityRelDef> componentInstancesRelations = getComponentInstancesRelations(
				resourceDetailsVF.getUniqueId());
		assertFalse(componentInstancesRelations.isEmpty());
		assertEquals(1, componentInstancesRelations.size());
		List<ComponentInstance> componentInstancesList = getComponentInstancesList(resourceDetailsVF.getUniqueId());
		for (ComponentInstance comp : componentInstancesList) {
			String instanceUid = comp.getUniqueId();
			assertTrue(checkNodesInRelations(instanceUid, componentInstancesRelations.get(0)));
		}
		assertEquals(2, componentInstancesList.size());

	}

	private RequirementCapabilityRelDef setUidsOfInstancesAfterLifecycleStateChange(ComponentInstance riReq,
			ComponentInstance riCap, CapReqDef capReqDefBeforeAssociate)
			throws ClientProtocolException, IOException, Exception {
		RequirementCapabilityRelDef requirementDef;
		// RestResponse getResourceResponse =
		// ResourceRestUtils.getResource(resourceDetailsVF.getUniqueId());
		// Resource resource_0_2 =
		// ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(),
		// Resource.class);
		// List<ComponentInstance> componentInstances =
		// resource_0_2.getComponentInstances();
		List<ComponentInstance> componentInstances = getComponentInstancesList(resourceDetailsVF.getUniqueId());

		for (ComponentInstance comp : componentInstances) {
			if (comp.getName().equals(riReq.getName())) {
				riReq.setUniqueId(comp.getUniqueId());
			} else if (comp.getName().equals(riCap.getName())) {
				riCap.setUniqueId(comp.getUniqueId());
			}
		}
		requirementDef = setRelationshipBetweenInstances(riReq, riCap, capReqDefBeforeAssociate);
		return requirementDef;
	}

	private boolean checkNodesInRelations(String instanceUid, RequirementCapabilityRelDef relation) {
		if (relation.getToNode().equals(instanceUid)) {
			return true;
		} else if (relation.getFromNode().equals(instanceUid)) {
			return true;
		} else {
			return false;
		}
	}

	@Test
	public void associateOneOfTwoCPsToVl_ThenDiscocciate() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());
		ResourceReqDetails secondResourceDetailsReq = new ResourceReqDetails(resourceDetailsReq, "0.1");
		secondResourceDetailsReq.setName("secondCP");
		secondResourceDetailsReq.setTags(Arrays.asList(secondResourceDetailsReq.getName()));
		createAtomicResource(secondResourceDetailsReq, designerUser);

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riReq2 = createComponentInstance(secondResourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		CapReqDef capReqDef = getResourceReqCap();

		List<RequirementDefinition> expectedList = requirementsBeforeAssociate.get("tosca.capabilities.Container");
		for (RequirementDefinition req : expectedList) {
			if (req.getOwnerName().equals(riReq2.getName())) {
				expectedList = new ArrayList<RequirementDefinition>(Arrays.asList(req));
				break;
			}
		}
		requirementsBeforeAssociate.put("tosca.capabilities.Container", expectedList);
		assertTrue(capReqDef.getRequirements().equals(requirementsBeforeAssociate));

		List<CapabilityDefinition> list = capabilitiesBeforeAssociate.get("tosca.capabilities.Container");
		for (CapabilityDefinition cap : list) {
			cap.setMinOccurrences("0");
		}

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));

		// second relationship

		RequirementCapabilityRelDef secondRequirementDef = setRelationshipBetweenInstances(riReq2, riCap,
				capReqDefBeforeAssociate);
		RestResponse secondAssociateInstances = ComponentInstanceRestUtils.associateInstances(secondRequirementDef,
				designerUser, resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, secondAssociateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(secondRequirementDef.getFromNode(), secondRequirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		CapReqDef capReqDefAfterSecondAssociation = getResourceReqCap();

		requirementsBeforeAssociate.remove("tosca.capabilities.Container");
		assertTrue(capReqDefAfterSecondAssociation.getRequirements().equals(requirementsBeforeAssociate));

		Map<String, List<CapabilityDefinition>> capabilitiesAfterSecondAssociate = capReqDefAfterSecondAssociation
				.getCapabilities();
		assertTrue(capabilitiesAfterSecondAssociate.equals(capabilitiesBeforeAssociate));

		// dissociate

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(secondRequirementDef,
				designerUser, resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, dissociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());

		CapReqDef capReqDefAfterDissociation = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesAfterDissociate = capReqDefAfterDissociation
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsAfterDissociate = capReqDefAfterDissociation
				.getRequirements();

		assertTrue(capabilitiesAfterDissociate.equals(capReqDef.getCapabilities()));
		requirementsBeforeAssociate.put("tosca.capabilities.Container", expectedList);
		assertTrue(requirementsAfterDissociate.equals(requirementsBeforeAssociate));
	}

	@Test
	public void associateNotCompitableCapAndReq() throws Exception {
		resourceDetailsReq = ElementFactory.getDefaultResourceByType("Database", NormativeTypesEnum.DATABASE,
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, designerUser.getUserId(), ResourceTypeEnum.CP.toString()); // resourceType
																														// =
																														// VFC
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		assertTrue(requirementDef.getRelationships().size() == 1);
		String requirement = requirementDef.getRelationships().get(0).getRelation().getRequirement();
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 404, associateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND.name(),
				new ArrayList<String>(Arrays.asList(riReq.getName(), riCap.getName(), requirement)),
				associateInstances.getResponse());

		CapReqDef capReqDef = getResourceReqCap();

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsAfterAssociate = capReqDef.getRequirements();

		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));
		assertTrue(requirementsAfterAssociate.equals(requirementsBeforeAssociate));

	}

	@Test
	public void disassociateCpAndCpTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.CP.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, dissociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertEquals("Check requirement", 1, listOfRequierments.size());
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));
	}

	@Test
	public void disassociateCpAndVfcTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VFC.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, dissociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertEquals("Check requirement", 1, listOfRequierments.size());
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));
	}

	@Test
	public void disassociateCpAndVLTest() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, dissociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertEquals("Check requirement", 1, listOfRequierments.size());
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));
	}

	// Error handliing
	// in the error should we get the unique id of instances instead of names
	@Test
	public void disassociateNotFoundAssociation() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();
		String requirementName = requirementDef.getRelationships().get(0).getRelation().getRequirement();

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 404, dissociateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND.name(),
				new ArrayList<String>(Arrays.asList(riReq.getName(), riCap.getName(), requirementName)),
				dissociateInstances.getResponse());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertEquals("Check requirement", 1, listOfRequierments.size());
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));
	}

	// Error handliing
	@Test
	public void disassociateRelationInVfNotFound() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		String uidNotFound = "123";
		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				uidNotFound, ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 404, dissociateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(),
				new ArrayList<String>(Arrays.asList(uidNotFound)), dissociateInstances.getResponse());

		// "serviceException": {
		// "messageId": "SVC4063",
		// "text": "Error: Requested \u0027%1\u0027 resource was not found.",
		// "variables": [
		// ""
		// ]
	}

	@Test
	public void disassociateWithDifferentDesigner() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2), resourceDetailsVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 409, dissociateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				dissociateInstances.getResponse());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertEquals("Check requirement", 1, listOfRequierments.size());
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));

	}

	@Test
	public void disassociateWithTester() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), resourceDetailsVF.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 409, dissociateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				dissociateInstances.getResponse());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertNotNull("Requierment is null after disassociate with tester", listOfRequierments);
		assertEquals("Check requirement", 1, listOfRequierments.size());
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));
	}

	@Test
	public void disassociateServiceWithUidOfVF() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VFC.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals("Check response code ", 404, dissociateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(),
				new ArrayList<String>(Arrays.asList("")), dissociateInstances.getResponse());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertTrue(listOfRequierments == null);
		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).size() != 0);
	}

	@Test
	public void disassociateWithEmptyVfUid() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				"", ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 404, dissociateInstances.getErrorCode().intValue());

		CapReqDef capReqDef = getResourceReqCap();

		requirementsBeforeAssociate.remove("tosca.capabilities.Container");
		assertTrue(capReqDef.getRequirements().equals(requirementsBeforeAssociate));

		List<CapabilityDefinition> list = capabilitiesBeforeAssociate.get("tosca.capabilities.Container");
		for (CapabilityDefinition cap : list) {
			cap.setMinOccurrences("0");
		}

		Map<String, List<CapabilityDefinition>> capabilitiesAfterAssociate = capReqDef.getCapabilities();
		assertTrue(capabilitiesAfterAssociate.equals(capabilitiesBeforeAssociate));
	}

	@Test
	public void disassociateOneComponentDeleted() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);

		RestResponse deleteResourceResponse = ResourceRestUtils.deleteResource(resourceDetailsCap.getUniqueId(),
				designerUser.getUserId());
		ResourceRestUtils.checkDeleteResponse(deleteResourceResponse);

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, dissociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();

		List<RequirementDefinition> listOfRequierments = capReqDefAfterDissociate.getRequirements()
				.get("tosca.capabilities.Container");
		assertEquals("Check requirement", 1, listOfRequierments.size());
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));
	}

	@Test
	public void disassociateNotCheckedoutVf() throws Exception {
		createTwoAtomicResourcesByType(ResourceTypeEnum.CP.toString(), ResourceTypeEnum.VL.toString());

		ComponentInstance riReq = createComponentInstance(resourceDetailsReq);
		ComponentInstance riCap = createComponentInstance(resourceDetailsCap);

		CapReqDef capReqDefBeforeAssociate = getResourceReqCap();
		Map<String, List<CapabilityDefinition>> capabilitiesBeforeAssociate = capReqDefBeforeAssociate
				.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementsBeforeAssociate = capReqDefBeforeAssociate
				.getRequirements();

		RequirementCapabilityRelDef requirementDef = setRelationshipBetweenInstances(riReq, riCap,
				capReqDefBeforeAssociate);

		RestResponse changeResourceStateToCheckin = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckin);

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", 409, dissociateInstances.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				dissociateInstances.getResponse());

		CapReqDef capReqDefAfterDissociate = getResourceReqCap();
		assertTrue(capReqDefAfterDissociate.getRequirements().equals(requirementsBeforeAssociate));
		assertTrue(capReqDefAfterDissociate.getCapabilities().equals(capabilitiesBeforeAssociate));

		RestResponse changeResourceStateToCheckout = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckout);

		requirementDef = setUidsOfInstancesAfterLifecycleStateChange(riReq, riCap, capReqDefBeforeAssociate);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, designerUser,
				resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
		assertTrue(checkRealtionship(requirementDef.getFromNode(), requirementDef.getToNode(),
				resourceDetailsVF.getUniqueId()));

		RestResponse secondDisociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef,
				designerUser, resourceDetailsVF.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, secondDisociateInstances.getErrorCode().intValue());
		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());

		RestResponse changeResourceStateToCheckout2 = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckout2);
		RestResponse changeResourceStateToCheckout3 = LifecycleRestUtils.changeResourceState(resourceDetailsVF,
				designerUser, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(changeResourceStateToCheckout3);

		assertTrue(getComponentInstancesRelations(resourceDetailsVF.getUniqueId()).isEmpty());

	}

}
