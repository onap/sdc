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

package org.openecomp.sdc.ci.tests.utils;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.Assert;

public class ReqCap {

	public static Map<String, List<CapabilityDefinition>> expectedContainerCapabilities;
	public static Map<String, List<RequirementDefinition>> expectedContainerRequirements;
	public static Map<String, RequirementDefinition> removedRequirements;
	public static Map<String, ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>>> expectedContInstReqCap;

	public static void verifyVFReqCap(String componentId) throws Exception {
		RestResponse restResponse = ResourceRestUtils.getResource(componentId);
		Resource resource = ResponseParser.parseToObject(restResponse.getResponse(), Resource.class);
		verifyReqCap(resource);
	}

	public static void verifyServiceReqCap(String componentId, User sdncDesignerDetails) throws Exception {
		RestResponse restResponse = ServiceRestUtils.getService(componentId, sdncDesignerDetails);
		Service service = ResponseParser.parseToObject(restResponse.getResponse(), Service.class);
		verifyReqCap(service);
	}

	public static void verifyProductReqCap(String componentId, User sdncPsDetails1) throws Exception {
		RestResponse restResponse = ProductRestUtils.getProduct(componentId, sdncPsDetails1.getUserId());
		Product product = ResponseParser.parseToObject(restResponse.getResponse(), Product.class);
		verifyReqCap(product);
	}

	public static void verifyReqCap(Component actualComponent) {
		verifyContainerReqCap(actualComponent);
		verifyCompInstReqCap(actualComponent);
	}

	public RestResponse changeServiceInstanceVersion(String componentUniqueId, String serviceInstanceToReplaceUniqueId,
			String serviceUniqueId, User sdncModifierDetails, ComponentTypeEnum componentType, boolean isHighestLevel)
			throws Exception {
		RestResponse changeResourceInstanceVersion = ProductRestUtils.changeServiceInstanceVersion(componentUniqueId,
				serviceInstanceToReplaceUniqueId, serviceUniqueId, sdncModifierDetails, componentType);
		if (changeResourceInstanceVersion.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS) && isHighestLevel) {
			/*
			 * // Add RI Capabilities and Requirements to expected MAP -->
			 * expectedVfCapabilities and expectedVfRequirements
			 * 
			 * ComponentInstance componentInstance =
			 * ResponseParser.parseToObjectUsingMapper(
			 * changeResourceInstanceVersion.getResponse(),
			 * ComponentInstance.class);
			 * addCompInstReqCapToExpected(componentInstance, componentType);
			 */
		}
		return changeResourceInstanceVersion;
	}

	public static void updateExpectedReqCapAfterChangeLifecycleState(String oldContainerUniqueIdToReplace,
			String newContainerUniqueId) {

		// Update of container req/cap

		Set<String> compInstKeysToChange = new HashSet<>();

		for (String expKey : expectedContainerCapabilities.keySet()) {
			List<CapabilityDefinition> expCapList = expectedContainerCapabilities.get(expKey);
			for (CapabilityDefinition cap : expCapList) {
				String ownerId = cap.getOwnerId();

				if (ownerId.contains(oldContainerUniqueIdToReplace)) {
					compInstKeysToChange.add(ownerId);
					cap.setOwnerId(cap.getOwnerId().replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId));
				}
			}
		}

		for (String expKey : expectedContainerRequirements.keySet()) {
			List<RequirementDefinition> expCapList = expectedContainerRequirements.get(expKey);
			for (RequirementDefinition cap : expCapList) {
				String ownerId = cap.getOwnerId();
				if (ownerId.contains(oldContainerUniqueIdToReplace)) {
					compInstKeysToChange.add(ownerId);
					cap.setOwnerId(cap.getOwnerId().replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId));
				}
			}
		}

		// Update of internal comp instances req/cap
		for (String oldKey : compInstKeysToChange) {
			ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>> immutablePair = expectedContInstReqCap
					.get(oldKey);
			if (immutablePair != null) {
				expectedContInstReqCap.remove(oldKey);
				String newKey = oldKey.replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId);
				expectedContInstReqCap.put(newKey, immutablePair);
			}
		}
	}

	private static void verifyCompInstReqCap(Component actualComponent) {
		List<ComponentInstance> componentInstances = actualComponent.getComponentInstances();
		if (componentInstances != null) {
			assertEquals(expectedContInstReqCap.size(), componentInstances.size());
			for (ComponentInstance compInst : componentInstances) {
				String uniqueId = compInst.getUniqueId();
				// System.out.println("Verifying req/cap of component instance
				// "+ uniqueId);
				Map<String, List<RequirementDefinition>> actualCompInstReq = compInst.getRequirements();
				if (actualCompInstReq == null) {
					actualCompInstReq = new HashMap<>();
				}
				Map<String, List<CapabilityDefinition>> actualCompInstCap = compInst.getCapabilities();
				if (actualCompInstCap == null) {
					actualCompInstCap = new HashMap<>();
				}
				ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>> expReqCap = expectedContInstReqCap
						.get(uniqueId);
				assertNotNull(expReqCap);
				// System.out.println("expected instance requirements:
				// "+expReqCap.right);
				// System.out.println("expected instance capabilities:
				// "+expReqCap.left);
				// System.out.println("actual instance requirements:
				// "+actualCompInstReq);
				// System.out.println("actual instance capabilities:
				// "+actualCompInstCap);

				// REQ comparison
				compareReqCapMaps(expReqCap.right, actualCompInstReq);

				// CAP comparison
				compareReqCapMaps(expReqCap.left, actualCompInstCap);
			}

		} else {
			assertTrue(expectedContInstReqCap.isEmpty());
		}
	}

	private static void verifyContainerReqCap(Component actualComponent) {
		Map<String, List<RequirementDefinition>> actualContainerRequirements = actualComponent.getRequirements();
		if (actualContainerRequirements == null) {
			actualContainerRequirements = new HashMap<>();
		}
		Map<String, List<CapabilityDefinition>> actualContainerCapabilities = actualComponent.getCapabilities();
		if (actualContainerCapabilities == null) {
			actualContainerCapabilities = new HashMap<>();
		}
		// System.out.println("Verifying req/cap of container component "+
		// actualComponent.getUniqueId());
		// System.out.println("expected container requirements:
		// "+expectedContainerRequirements);
		// System.out.println("expected container capabilities:
		// "+expectedContainerCapabilities);
		// System.out.println("actual container requirements:
		// "+actualContainerRequirements);
		// System.out.println("actual container capabilities:
		// "+actualContainerCapabilities);

		// REQ comparison
		compareReqCapMaps(expectedContainerRequirements, actualContainerRequirements);

		// CAP comparison
		compareReqCapMaps(expectedContainerCapabilities, actualContainerCapabilities);
	}

	private static <T> void compareReqCapMaps(Map<String, List<T>> expectedMap, Map<String, List<T>> actualMap) {
		assertEquals(expectedMap.size(), actualMap.size());
		for (String expKey : expectedMap.keySet()) {
			List<?> expCapList = expectedMap.get(expKey);
			List<?> actCapList = actualMap.get(expKey);
			assertEquals(expCapList.size(), actCapList.size());
			assertEquals(new HashSet<>(expCapList), new HashSet<>(actCapList));
		}
	}

	public static void addCompInstReqCapToExpected(ComponentInstance componentInstance,
			ComponentTypeEnum containerComponentType, User sdncDesignerDetails) throws Exception {

		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String uniqueId = componentInstance.getUniqueId();
		String name = componentInstance.getName();
		String originComponentId = componentInstance.getComponentUid();
		RestResponse getResponse = null;
		ComponentTypeEnum compInstType = getCompInstTypeByContainerType(containerComponentType);
		Component component = null;
		if (compInstType == ComponentTypeEnum.RESOURCE) {
			getResponse = ResourceRestUtils.getResource(sdncDesignerDetails, originComponentId);
			ResourceRestUtils.checkSuccess(getResponse);
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Resource.class);
		} else if (compInstType == ComponentTypeEnum.SERVICE) {
			getResponse = ServiceRestUtils.getService(originComponentId, sdncDesignerDetails);
			ResourceRestUtils.checkSuccess(getResponse);
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Service.class);
		} else {
			Assert.fail("Unsupported type - " + containerComponentType);
		}

		Map<String, List<RequirementDefinition>> resourceRequirements = component.getRequirements();
		if (resourceRequirements == null) {
			resourceRequirements = new HashMap<>();
		}
		
		Function<Entry<String, List<RequirementDefinition>>, List<RequirementDefinition>> requirementDefinitionMapper = e -> new ArrayList<>(e.getValue().stream().map(item -> new RequirementDefinition(item)).collect(Collectors.toList()));
		Map<String, List<RequirementDefinition>> reqCopy = resourceRequirements.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), requirementDefinitionMapper));

		Map<String, List<CapabilityDefinition>> resourceCapabilities = component.getCapabilities();
		if (resourceCapabilities == null) {
			resourceCapabilities = new HashMap<>();
		}
		
		Function<Entry<String, List<CapabilityDefinition>>, List<CapabilityDefinition>> capabilityDefinitionMapper = e -> new ArrayList<>(e.getValue().stream().map(item -> new CapabilityDefinition(item)).collect(Collectors.toList()));
		Map<String, List<CapabilityDefinition>> capCopy = resourceCapabilities.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), capabilityDefinitionMapper));

		setupContainerExpectedReqCap(uniqueId, name, resourceRequirements, resourceCapabilities);
		if (component.getComponentType().equals(ComponentTypeEnum.RESOURCE)
				&& ((Resource) component).getResourceType() != ResourceTypeEnum.VF) {
			setupConstInstExpectedReqCap(uniqueId, name, reqCopy, capCopy);
		}

		// adding entry for expected componentInstance
		ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>> compInstReqCapPair = new ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>>(
				capCopy, reqCopy);
		expectedContInstReqCap.put(uniqueId, compInstReqCapPair);
	}

	private static void setupContainerExpectedReqCap(String uniqueId, String name,
			Map<String, List<RequirementDefinition>> componentRequirements,
			Map<String, List<CapabilityDefinition>> componentCapabilities) {
		for (Entry<String, List<RequirementDefinition>> resReq : componentRequirements.entrySet()) {
			List<RequirementDefinition> reqListToAdd = resReq.getValue();
			for (RequirementDefinition requirementDefinition : reqListToAdd) {
				requirementDefinition.setOwnerId(uniqueId);
				requirementDefinition.setOwnerName(name);
			}
			List<RequirementDefinition> expectedReqList = expectedContainerRequirements.get(resReq.getKey());
			if (expectedReqList == null) {
				expectedReqList = reqListToAdd;
			} else {
				expectedReqList.addAll(reqListToAdd);
			}
			expectedContainerRequirements.put(resReq.getKey(), expectedReqList);
		}

		for (Entry<String, List<CapabilityDefinition>> resCap : componentCapabilities.entrySet()) {
			List<CapabilityDefinition> capListToAdd = resCap.getValue();
			for (CapabilityDefinition capDefinition : capListToAdd) {
				capDefinition.setOwnerId(uniqueId);
				capDefinition.setOwnerName(name);
			}
			List<CapabilityDefinition> expectedCapList = expectedContainerCapabilities.get(resCap.getKey());
			if (expectedCapList == null) {
				expectedCapList = capListToAdd;
			} else {
				expectedCapList.addAll(capListToAdd);
			}
			expectedContainerCapabilities.put(resCap.getKey(), expectedCapList);
		}
	}

	private static void setupConstInstExpectedReqCap(String uniqueId, String name,
			Map<String, List<RequirementDefinition>> componentRequirements,
			Map<String, List<CapabilityDefinition>> componentCapabilities) {
		for (Entry<String, List<RequirementDefinition>> resReq : componentRequirements.entrySet()) {
			List<RequirementDefinition> reqListToAdd = resReq.getValue();
			for (RequirementDefinition requirementDefinition : reqListToAdd) {
				requirementDefinition.setOwnerId(uniqueId);
				requirementDefinition.setOwnerName(name);
			}
		}

		for (Entry<String, List<CapabilityDefinition>> resCap : componentCapabilities.entrySet()) {
			List<CapabilityDefinition> capListToAdd = resCap.getValue();
			for (CapabilityDefinition capDefinition : capListToAdd) {
				capDefinition.setOwnerId(uniqueId);
				capDefinition.setOwnerName(name);
			}
		}
	}

	private static ComponentTypeEnum getCompInstTypeByContainerType(ComponentTypeEnum componentType) {
		switch (componentType) {
		case RESOURCE:
			return ComponentTypeEnum.RESOURCE;
		case SERVICE:
			return ComponentTypeEnum.RESOURCE;
		case PRODUCT:
			return ComponentTypeEnum.SERVICE;
		default:
			break;
		}
		return null;
	}

	public static void deleteCompInstReqCapFromExpected(String componentInstanceId) {
		List<String> entriesRequirementsToRemove = new ArrayList<>();
		List<String> entriesCapabilitiesToRemove = new ArrayList<>();
		for (Entry<String, List<RequirementDefinition>> reqEntry : expectedContainerRequirements.entrySet()) {
			List<RequirementDefinition> reqList = reqEntry.getValue();
			List<RequirementDefinition> reqListToDelete = new ArrayList<>();
			for (RequirementDefinition requirementDefinition : reqList) {
				if (requirementDefinition.getOwnerId().equals(componentInstanceId)) {
					reqListToDelete.add(requirementDefinition);
				}
			}
			reqList.removeAll(reqListToDelete);
			if (reqList.isEmpty()) {
				entriesRequirementsToRemove.add(reqEntry.getKey());
			}
		}

		for (String ekey : entriesRequirementsToRemove) {
			expectedContainerRequirements.remove(ekey);
		}

		for (Entry<String, List<CapabilityDefinition>> capEntry : expectedContainerCapabilities.entrySet()) {
			List<CapabilityDefinition> capList = capEntry.getValue();
			List<CapabilityDefinition> capListToDelete = new ArrayList<>();
			for (CapabilityDefinition capabilityDefinition : capList) {
				if (capabilityDefinition.getOwnerId().equals(componentInstanceId)) {
					capListToDelete.add(capabilityDefinition);
				}
			}
			capList.removeAll(capListToDelete);
			if (capList.isEmpty()) {
				entriesCapabilitiesToRemove.add(capEntry.getKey());
			}
		}
		for (String ekey : entriesCapabilitiesToRemove) {
			expectedContainerCapabilities.remove(ekey);
		}

		expectedContInstReqCap.remove(componentInstanceId);

	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse createAtomicInstanceForVF(Resource containerDetails, Resource compInstOriginDetails,
			User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.RESOURCE,
				true);
	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse createAtomicInstanceForService(Service containerDetails, Resource compInstOriginDetails,
			User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE,
				true);
	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse createVFInstance(Service containerDetails, Resource compInstOriginDetails, User modifier)
			throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE,
				true);
	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse createServiceInstance(Product containerDetails, Service compInstOriginDetails,
			User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.PRODUCT,
				true);
	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse deleteAtomicInstanceForVF(String compInstUniqueId, Resource containerDetails,
			User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.RESOURCE, true);
	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse deleteAtomicInstanceForService(String compInstUniqueId, Service containerDetails,
			User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, true);
	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse deleteVFInstance(String compInstUniqueId, Service containerDetails, User modifier)
			throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, true);

	}

	// Automatically updates the expected req/cap of the container
	public static RestResponse deleteServiceInstance(String compInstUniqueId, Product containerDetails, User modifier)
			throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.PRODUCT, true);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse createAtomicInstanceForVFDuringSetup(Resource containerDetails,
			Resource compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.RESOURCE,
				false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse createAtomicInstanceForServiceDuringSetup(Service containerDetails,
			Resource compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE,
				false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse createVFInstanceDuringSetup(Service containerDetails, Resource compInstOriginDetails,
			User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE,
				false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse createServiceInstanceDuringSetup(Product containerDetails, Service compInstOriginDetails,
			User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.PRODUCT,
				false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse deleteAtomicInstanceForVFDuringSetup(String compInstUniqueId, Resource containerDetails,
			User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.RESOURCE, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse deleteAtomicInstanceForServiceDuringSetup(String compInstUniqueId,
			Service containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse deleteVFInstanceDuringSetup(String compInstUniqueId, Service containerDetails,
			User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, false);

	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	public static RestResponse deleteServiceInstanceDuringSetup(String compInstUniqueId, Product containerDetails,
			User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.PRODUCT, false);
	}

	public static Component getComponentAndValidateRIs(Component componentDetails, int numberOfRIs,
			int numberOfRelations, User sdncAdminDetails) throws IOException, Exception {

		RestResponse getResponse = null;
		Component component = null;
		if (componentDetails instanceof Resource) {
			getResponse = ResourceRestUtils.getResource(sdncAdminDetails, componentDetails.getUniqueId());
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Resource.class);
		} else if (componentDetails instanceof Service) {
			getResponse = ServiceRestUtils.getService((componentDetails.getUniqueId()), sdncAdminDetails);
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Service.class);
		} else if (componentDetails instanceof Product) {
			getResponse = ProductRestUtils.getProduct(componentDetails.getUniqueId(), sdncAdminDetails.getUserId());
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Product.class);
		} else {
			Assert.fail("Unsupported type of componentDetails - " + componentDetails.getClass().getSimpleName());
		}
		ResourceRestUtils.checkSuccess(getResponse);
		int numberOfActualRIs = component.getComponentInstances() != null ? component.getComponentInstances().size()
				: 0;
		int numberOfActualRelations = component.getComponentInstancesRelations() != null
				? component.getComponentInstancesRelations().size() : 0;
		assertEquals("Check number of RIs meet the expected number", numberOfRIs, numberOfActualRIs);
		assertEquals("Check number of RI relations meet the expected number", numberOfRelations,
				numberOfActualRelations);
		verifyReqCap(component);

		return component;
	}

	public static void getComponentAndValidateRIsAfterChangeLifecycleState(String oldComponentUniqueIdToReplace,
			Component componentDetails, int numOfRIs, int numOfRelations, User sdncAdminDetails)
			throws IOException, Exception {
		updateExpectedReqCapAfterChangeLifecycleState(oldComponentUniqueIdToReplace, componentDetails.getUniqueId());
		getComponentAndValidateRIs(componentDetails, numOfRIs, numOfRelations, sdncAdminDetails);
	}

	private static RestResponse createComponentInstance(Component containerDetails, Component compInstOriginDetails,
			User modifier, ComponentTypeEnum containerComponentTypeEnum, boolean isHighestLevel)
			throws IOException, Exception {
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentInstance(compInstOriginDetails);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, modifier, containerDetails.getUniqueId(), containerComponentTypeEnum);
		if (createResourceInstanceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_CREATED) && isHighestLevel) {
			// Add RI Capabilities and Requirements to expected MAP -->
			// expectedVfCapabilities and expectedVfRequirements
			ComponentInstance componentInstance = ResponseParser
					.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
			addCompInstReqCapToExpected(componentInstance, containerComponentTypeEnum, modifier);
		}
		return createResourceInstanceResponse;
	}

	private static RestResponse deleteComponentInstance(String compInstUniqueId, Component containerDetails,
			User modifier, ComponentTypeEnum componentTypeEnum, boolean isHighestLevel) throws Exception {
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(modifier,
				containerDetails.getUniqueId(), compInstUniqueId, componentTypeEnum);
		if (deleteResourceInstanceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_DELETE) && isHighestLevel) {
			deleteCompInstReqCapFromExpected(compInstUniqueId);
		}
		return deleteResourceInstanceResponse;
	}

	public static RestResponse associateComponentInstancesForService(RequirementCapabilityRelDef requirementDef,
			ComponentReqDetails containerDetails, User user) throws IOException {

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, user,
				containerDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		deleteAssociatedFromExpected(requirementDef);
		return associateInstances;
	}

	private static void deleteAssociatedFromExpected(RequirementCapabilityRelDef requirementDef) {
		// removing from requirements
		RelationshipInfo relationship = requirementDef.getRelationships().get(0).getRelation();
		String type = relationship.getRelationship().getType();
		String fromId = requirementDef.getFromNode();
		List<RequirementDefinition> reqList = expectedContainerRequirements.get(type);
		RequirementDefinition toDelete = null;
		if (reqList != null) {
			for (RequirementDefinition reqDef : reqList) {
				if (reqDef.getOwnerId().equals(fromId)) {
					toDelete = reqDef;
				}
			}
			if (toDelete != null) {
				reqList.remove(toDelete);
				if (reqList.isEmpty()) {
					expectedContainerRequirements.remove(type);
				}
				removedRequirements.put(toDelete.getCapability() + " " + toDelete.getOwnerId(), toDelete);
			}
		}
	}

	public static void dissociateComponentInstancesForService(RequirementCapabilityRelDef requirementDef,
			ComponentReqDetails containerDetails, User user) throws IOException {

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, user,
				containerDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(dissociateInstances);
		addDissociatedToExpected(requirementDef);
	}

	private static void addDissociatedToExpected(RequirementCapabilityRelDef requirementDef) {
		// adding to requirements
		RelationshipInfo relationship = requirementDef.getRelationships().get(0).getRelation();
		String type = relationship.getRelationship().getType();
		String fromId = requirementDef.getFromNode();
		String key = type + " " + fromId;
		RequirementDefinition requirementDefinition = removedRequirements.get(key);
		if (requirementDefinition != null) {
			List<RequirementDefinition> reqList = expectedContainerRequirements.get(type);
			if (reqList == null) {
				reqList = new ArrayList<>();
				expectedContainerRequirements.put(type, reqList);
			}
			reqList.add(requirementDefinition);
		}
	}

}
