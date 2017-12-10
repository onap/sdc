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

package org.openecomp.sdc.ci.tests.api;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
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
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.Assert;

public class ComponentInstanceBaseTest extends ComponentBaseTest {
	public static final String acceptHeaderData = "application/json";
	// Req/cap of container component
	protected Map<String, List<CapabilityDefinition>> expectedContainerCapabilities;
	protected Map<String, List<RequirementDefinition>> expectedContainerRequirements;
	protected Map<String, Map<String, List<RequirementDefinition>>> removedRequirements;
	protected Map<String, ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>>> expectedContInstReqCap;

	protected User sdncPsDetails1;
	protected User sdncPsDetails2;
	protected User sdncPmDetails1;
	protected User sdncPmDetails2;
	protected User sdncDesignerDetails;
	protected User sdncAdminDetails;
	protected User sdncTesterDetails;
	protected ResourceReqDetails resourceDetailsVFC_01;
	protected ResourceReqDetails resourceDetailsVFC_02;
	protected ResourceReqDetails resourceDetailsVF_01;
	protected ResourceReqDetails resourceDetailsVF_02;
	protected ResourceReqDetails resourceDetailsCP_01;
	protected ResourceReqDetails resourceDetailsCP_02;
	protected ResourceReqDetails resourceDetailsVL_01;
	protected ResourceReqDetails resourceDetailsVL_02;
	protected ServiceReqDetails serviceDetails_01;
	protected ServiceReqDetails serviceDetails_02;
	protected ServiceReqDetails serviceDetails_03;
	protected ProductReqDetails productDetails_01;
	protected ProductReqDetails productDetails_02;

	public void init() {
		// Req/caps of inner componentInstances
		expectedContainerCapabilities = new LinkedHashMap<String, List<CapabilityDefinition>>();
		expectedContainerRequirements = new LinkedHashMap<String, List<RequirementDefinition>>();
		removedRequirements = new HashMap<>();
		expectedContInstReqCap = new HashMap<>();

		sdncPsDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);
		sdncPsDetails2 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST2);
		sdncPmDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		sdncPmDetails2 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER2);
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncTesterDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		resourceDetailsVFC_01 = ElementFactory.getDefaultResourceByType("ciVFC100", NormativeTypesEnum.SOFTWARE_COMPONENT, ResourceCategoryEnum.GENERIC_DATABASE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VFC.toString()); // resourceType = VFC
		resourceDetailsVFC_02 = ElementFactory.getDefaultResourceByType("ciVFC200", NormativeTypesEnum.COMPUTE, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VFC.toString());
		resourceDetailsVF_01 = ElementFactory.getDefaultResourceByType("ciVF100", NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VF.toString());
		resourceDetailsVF_02 = ElementFactory.getDefaultResourceByType("ciVF200", NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VF.toString());
		resourceDetailsCP_01 = ElementFactory.getDefaultResourceByType("ciCP100", NormativeTypesEnum.PORT, ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, sdncDesignerDetails.getUserId(), ResourceTypeEnum.CP.toString());
		resourceDetailsCP_02 = ElementFactory.getDefaultResourceByType("ciCP200", NormativeTypesEnum.PORT, ResourceCategoryEnum.GENERIC_DATABASE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.CP.toString());
		resourceDetailsVL_01 = ElementFactory.getDefaultResourceByType("ciVL100", NormativeTypesEnum.NETWORK, ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VL.toString());
		resourceDetailsVL_02 = ElementFactory.getDefaultResourceByType("ciVL200", NormativeTypesEnum.NETWORK, ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VL.toString());
		serviceDetails_01 = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncDesignerDetails.getUserId());
		serviceDetails_02 = ElementFactory.getDefaultService("ciNewtestservice2", ServiceCategoriesEnum.MOBILITY, sdncDesignerDetails.getUserId());
		serviceDetails_03 = ElementFactory.getDefaultService("ciNewtestservice3", ServiceCategoriesEnum.MOBILITY, sdncDesignerDetails.getUserId());
		productDetails_01 = ElementFactory.getDefaultProduct("ciProduct01");
		productDetails_02 = ElementFactory.getDefaultProduct("ciProduct02");
	}

	public ComponentInstanceBaseTest(TestName testName, String className) {
		super(testName, className);
	}

	public void verifyVFReqCap(String componentId) throws Exception {
		RestResponse restResponse = ResourceRestUtils.getResource(componentId);
		Resource resource = ResponseParser.parseToObject(restResponse.getResponse(), Resource.class);
		verifyReqCap(resource);
	}

	public void verifyServiceReqCap(String componentId) throws Exception {
		RestResponse restResponse = ServiceRestUtils.getService(componentId, sdncDesignerDetails);
		Service service = ResponseParser.parseToObject(restResponse.getResponse(), Service.class);
		verifyReqCap(service);
	}

	public void verifyProductReqCap(String componentId) throws Exception {
		RestResponse restResponse = ProductRestUtils.getProduct(componentId, sdncPsDetails1.getUserId());
		Product product = ResponseParser.parseToObject(restResponse.getResponse(), Product.class);
		verifyReqCap(product);
	}

	public void verifyReqCap(Component actualComponent) {
		verifyContainerReqCap(actualComponent);
		verifyCompInstReqCap(actualComponent);
	}

	public RestResponse changeServiceInstanceVersion(String componentUniqueId, String serviceInstanceToReplaceUniqueId, String serviceUniqueId, User sdncModifierDetails, ComponentTypeEnum componentType, boolean isHighestLevel) throws Exception {
		RestResponse changeResourceInstanceVersion = ProductRestUtils.changeServiceInstanceVersion(componentUniqueId, serviceInstanceToReplaceUniqueId, serviceUniqueId, sdncModifierDetails, componentType);
		if (changeResourceInstanceVersion.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS) && isHighestLevel) {
			/*
			 * // Add RI Capabilities and Requirements to expected MAP --> expectedVfCapabilities and expectedVfRequirements
			 * 
			 * ComponentInstance componentInstance = ResponseParser.parseToObjectUsingMapper( changeResourceInstanceVersion.getResponse(), ComponentInstance.class); addCompInstReqCapToExpected(componentInstance, componentType);
			 */
		}
		return changeResourceInstanceVersion;
	}

	protected void updateExpectedReqCapAfterChangeLifecycleState(String oldContainerUniqueIdToReplace, String newContainerUniqueId) {

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
			ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>> immutablePair = expectedContInstReqCap.get(oldKey);
			if (immutablePair != null) {
				expectedContInstReqCap.remove(oldKey);
				String newKey = oldKey.replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId);
				expectedContInstReqCap.put(newKey, immutablePair);
			}
		}

		// Update of removed req
		for (String oldKey : compInstKeysToChange) {
			Map<String, List<RequirementDefinition>> map = removedRequirements.get(oldKey);
			if (map != null) {
				removedRequirements.remove(oldKey);
				String newKey = oldKey.replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId);
				Collection<List<RequirementDefinition>> values = map.values();
				if (values != null) {
					for (List<RequirementDefinition> list : values) {
						for (RequirementDefinition reqDef : list) {
							reqDef.setOwnerId(reqDef.getOwnerId().replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId));
						}
					}
				}
				removedRequirements.put(newKey, map);
			}
		}
	}

	private void verifyCompInstReqCap(Component actualComponent) {
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
				ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>> expReqCap = expectedContInstReqCap.get(uniqueId);
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

	private void verifyContainerReqCap(Component actualComponent) {
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

	private <T> void compareReqCapMaps(Map<String, List<T>> expectedMap, Map<String, List<T>> actualMap) {
		assertEquals(expectedMap.size(), actualMap.size());
		for (String expKey : expectedMap.keySet()) {
			List<?> expCapList = expectedMap.get(expKey);
			List<?> actCapList = actualMap.get(expKey);
			assertEquals(expCapList.size(), actCapList.size());
			assertEquals(new HashSet<>(expCapList), new HashSet<>(actCapList));
		}
	}

	public void addCompInstReqCapToExpected(ComponentInstance componentInstance, ComponentTypeEnum containerComponentType) throws Exception {
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
		
		Function<? super Entry<String, List<CapabilityDefinition>>, List<CapabilityDefinition>> capabilityDefinitionMapper = e -> new ArrayList<>(e.getValue().stream().map(item -> new CapabilityDefinition(item)).collect(Collectors.toList()));
		Map<String, List<CapabilityDefinition>> capCopy = resourceCapabilities.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), capabilityDefinitionMapper));
		
		setupContainerExpectedReqCap(uniqueId, name, resourceRequirements, resourceCapabilities);
		if (component.getComponentType().equals(ComponentTypeEnum.RESOURCE) && ((Resource) component).getResourceType() != ResourceTypeEnum.VF) {
			setupConstInstExpectedReqCap(uniqueId, name, reqCopy, capCopy);
		}

		// adding entry for expected componentInstance
		ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>> compInstReqCapPair = new ImmutablePair<Map<String, List<CapabilityDefinition>>, Map<String, List<RequirementDefinition>>>(capCopy, reqCopy);
		expectedContInstReqCap.put(uniqueId, compInstReqCapPair);
	}

	private void setupContainerExpectedReqCap(String uniqueId, String name, Map<String, List<RequirementDefinition>> componentRequirements, Map<String, List<CapabilityDefinition>> componentCapabilities) {
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

	private void setupConstInstExpectedReqCap(String uniqueId, String name, Map<String, List<RequirementDefinition>> componentRequirements, Map<String, List<CapabilityDefinition>> componentCapabilities) {
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

	private ComponentTypeEnum getCompInstTypeByContainerType(ComponentTypeEnum componentType) {
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

	public void deleteCompInstReqCapFromExpected(String componentInstanceId) {
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
	protected RestResponse createAtomicInstanceForVF(ResourceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.RESOURCE, true);
	}

	// Automatically updates the expected req/cap of the container
	protected RestResponse createAtomicInstanceForService(ServiceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE, true);
	}

	// Automatically updates the expected req/cap of the container
	protected RestResponse createVFInstance(ServiceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE, true);
	}

	// Automatically updates the expected req/cap of the container
	protected RestResponse createServiceInstance(ProductReqDetails containerDetails, ServiceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.PRODUCT, true);
	}

	// Automatically updates the expected req/cap of the container
	protected RestResponse deleteAtomicInstanceForVF(String compInstUniqueId, ResourceReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.RESOURCE, true);
	}

	// Automatically updates the expected req/cap of the container
	protected RestResponse deleteAtomicInstanceForService(String compInstUniqueId, ServiceReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, true);
	}

	// Automatically updates the expected req/cap of the container
	protected RestResponse deleteVFInstance(String compInstUniqueId, ServiceReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, true);

	}

	// Automatically updates the expected req/cap of the container
	protected RestResponse deleteServiceInstance(String compInstUniqueId, ProductReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.PRODUCT, true);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse createAtomicInstanceForVFDuringSetup(ResourceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.RESOURCE, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse createAtomicInstanceForServiceDuringSetup(ServiceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse createVFInstanceDuringSetup(ServiceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse createServiceInstanceDuringSetup(ProductReqDetails containerDetails, ServiceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.PRODUCT, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse deleteAtomicInstanceForVFDuringSetup(String compInstUniqueId, ResourceReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.RESOURCE, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse deleteAtomicInstanceForServiceDuringSetup(String compInstUniqueId, ServiceReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, false);
	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse deleteVFInstanceDuringSetup(String compInstUniqueId, ServiceReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.SERVICE, false);

	}

	// Setup of lower components - Doesn't affect req/cap of the container (for
	// example, setup of VF for testing a Product)
	protected RestResponse deleteServiceInstanceDuringSetup(String compInstUniqueId, ProductReqDetails containerDetails, User modifier) throws IOException, Exception {
		return deleteComponentInstance(compInstUniqueId, containerDetails, modifier, ComponentTypeEnum.PRODUCT, false);
	}

	protected Component getComponentAndValidateRIs(ComponentReqDetails componentDetails, int numberOfRIs, int numberOfRelations) throws IOException, Exception {

		RestResponse getResponse = null;
		Component component = null;
		if (componentDetails instanceof ResourceReqDetails) {
			getResponse = ResourceRestUtils.getResource(sdncAdminDetails, componentDetails.getUniqueId());
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Resource.class);
		} else if (componentDetails instanceof ServiceReqDetails) {
			getResponse = ServiceRestUtils.getService((ServiceReqDetails) componentDetails, sdncAdminDetails);
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Service.class);
		} else if (componentDetails instanceof ProductReqDetails) {
			getResponse = ProductRestUtils.getProduct(componentDetails.getUniqueId(), sdncAdminDetails.getUserId());
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Product.class);
		} else {
			Assert.fail("Unsupported type of componentDetails - " + componentDetails.getClass().getSimpleName());
		}
		ResourceRestUtils.checkSuccess(getResponse);
		int numberOfActualRIs = component.getComponentInstances() != null ? component.getComponentInstances().size() : 0;
		int numberOfActualRelations = component.getComponentInstancesRelations() != null ? component.getComponentInstancesRelations().size() : 0;
		assertEquals("Check number of RIs meet the expected number", numberOfRIs, numberOfActualRIs);
		assertEquals("Check number of RI relations meet the expected number", numberOfRelations, numberOfActualRelations);
		verifyReqCap(component);

		return component;
	}

	protected void getComponentAndValidateRIsAfterChangeLifecycleState(String oldComponentUniqueIdToReplace, ComponentReqDetails componentDetails, int numOfRIs, int numOfRelations) throws IOException, Exception {
		updateExpectedReqCapAfterChangeLifecycleState(oldComponentUniqueIdToReplace, componentDetails.getUniqueId());
		getComponentAndValidateRIs(componentDetails, numOfRIs, numOfRelations);
	}

	private RestResponse createComponentInstance(ComponentReqDetails containerDetails, ComponentReqDetails compInstOriginDetails, User modifier, ComponentTypeEnum containerComponentTypeEnum, boolean isHighestLevel) throws IOException, Exception {
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory.getComponentResourceInstance(compInstOriginDetails);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, modifier, containerDetails.getUniqueId(), containerComponentTypeEnum);
		if (createResourceInstanceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_CREATED) && isHighestLevel) {
			// Add RI Capabilities and Requirements to expected MAP -->
			// expectedVfCapabilities and expectedVfRequirements
			ComponentInstance componentInstance = ResponseParser.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
			addCompInstReqCapToExpected(componentInstance, containerComponentTypeEnum);
		}
		return createResourceInstanceResponse;
	}

	private RestResponse deleteComponentInstance(String compInstUniqueId, ComponentReqDetails containerDetails, User modifier, ComponentTypeEnum componentTypeEnum, boolean isHighestLevel) throws Exception {
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(modifier, containerDetails.getUniqueId(), compInstUniqueId, componentTypeEnum);
		if (deleteResourceInstanceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_DELETE) && isHighestLevel) {
			deleteCompInstReqCapFromExpected(compInstUniqueId);
		}
		return deleteResourceInstanceResponse;
	}

	// Create Atomic resource ( VFC/CP/VL)
	protected void createAtomicResource(ResourceReqDetails resourceDetails) throws Exception {
		RestResponse createResourceResponse = ResourceRestUtils.createResource(resourceDetails, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceResponse);

	}

	protected void createVF(ResourceReqDetails resourceDetails) throws Exception {
		createVF(resourceDetails, sdncDesignerDetails);

	}

	protected void createVF(ResourceReqDetails resourceDetails, User sdncModifier) throws Exception {
		RestResponse createVfResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifier);
		ResourceRestUtils.checkCreateResponse(createVfResponse);
	}

	protected void createService(ServiceReqDetails serviceDetails) throws Exception {
		createService(serviceDetails, sdncDesignerDetails);
	}

	protected void createService(ServiceReqDetails serviceDetails, User sdncModifier) throws Exception {
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifier);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
	}

	protected void createProduct(ProductReqDetails productDetails) throws Exception {
		createProduct(productDetails, sdncPmDetails1);
	}

	protected void createProduct(ProductReqDetails productDetails, User sdncModifier) throws Exception {
		RestResponse createProductResponse = ProductRestUtils.createProduct(productDetails, sdncModifier);
		ResourceRestUtils.checkCreateResponse(createProductResponse);
	}

	protected RestResponse associateComponentInstancesForService(RequirementCapabilityRelDef requirementDef, ComponentReqDetails containerDetails, User user) throws IOException {

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, user, containerDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		deleteAssociatedFromExpected(requirementDef);

		return associateInstances;
	}

	private void deleteAssociatedFromExpected(RequirementCapabilityRelDef requirementDef) {
		// removing from requirements
		RelationshipInfo relationship = requirementDef.getRelationships().get(0).getRelation();
		String type = relationship.getRelationship().getType();
		String fromId = requirementDef.getFromNode();
		List<RequirementDefinition> reqList = expectedContainerRequirements.get(type);
		List<CapabilityDefinition> capList = expectedContainerCapabilities.get(type);
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
				String ownerId = toDelete.getOwnerId();
				Map<String, List<RequirementDefinition>> map = removedRequirements.get(ownerId);
				if (map == null) {
					map = new HashMap<>();
					removedRequirements.put(ownerId, map);
				}
				List<RequirementDefinition> list = map.get(type);
				if (list == null) {
					list = new ArrayList<>();
					map.put(type, list);
				}
				list.add(toDelete);
			}
		}

		for (CapabilityDefinition capabilityDefinition : capList) {
			if (capabilityDefinition.getType().equals(type)) {
				int minOccurrences = Integer.parseInt(capabilityDefinition.getMinOccurrences()) - 1;
				if (minOccurrences < 0)
					minOccurrences = 0;
				String minOccurrencesString = Integer.toString(minOccurrences);
				capabilityDefinition.setMinOccurrences(minOccurrencesString);
				if (!capabilityDefinition.getMaxOccurrences().equals("UNBOUNDED")) {
					int maxOccurrences = Integer.parseInt(capabilityDefinition.getMaxOccurrences()) - 1;
					if (maxOccurrences < 0)
						maxOccurrences = 0;
					String maxOccurrencesString = Integer.toString(maxOccurrences);
					capabilityDefinition.setMaxOccurrences(maxOccurrencesString);
				}
			}
		}
		expectedContainerCapabilities.put(type, capList);
	}

	protected void dissociateComponentInstancesForService(RequirementCapabilityRelDef requirementDef, ComponentReqDetails containerDetails, User user) throws IOException {

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef, user, containerDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(dissociateInstances);
		addDissociatedToExpected(requirementDef);
	}

	protected void fulfillCpRequirement(ComponentReqDetails component, String cpCompInstId, String cpReqFulfillerCompInstId, String cpReqFulfillerOwnerId, User user, ComponentTypeEnum containerCompType) throws IOException {
		// Fulfilling cp's "binding" requirement - US626240
		String requirementName = "binding";
		String capType = "tosca.capabilities.network.Bindable";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(user, component);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef reqCapRelation = ElementFactory.getReqCapRelation(cpCompInstId, cpReqFulfillerCompInstId, cpCompInstId, cpReqFulfillerOwnerId, capType, requirementName, capList, reqList);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(reqCapRelation, user, component.getUniqueId(), containerCompType);
		ResourceRestUtils.checkSuccess(associateInstances);
	}

	protected void consumeVlCapability(ComponentReqDetails component, String vlCapConsumerCompInstId, String vlCompInstId, String vlCapConsumerOwnerId, User user, ComponentTypeEnum containerCompType) throws IOException {
		// Consuming vl's "link" capability - US626240
		String requirementName = "link";
		String capType = "tosca.capabilities.network.Linkable";
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(user, component);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);
		RequirementCapabilityRelDef reqCapRelation = ElementFactory.getReqCapRelation(vlCapConsumerCompInstId, vlCompInstId, vlCapConsumerOwnerId, vlCompInstId, capType, requirementName, capList, reqList);
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(reqCapRelation, user, component.getUniqueId(), containerCompType);
		ResourceRestUtils.checkSuccess(associateInstances);
	}

	private void addDissociatedToExpected(RequirementCapabilityRelDef requirementDef) {
		// adding to requirements
		RelationshipInfo relationship = requirementDef.getRelationships().get(0).getRelation();
		String type = relationship.getRelationship().getType();
		String fromId = requirementDef.getFromNode();
		Map<String, List<RequirementDefinition>> map = removedRequirements.get(fromId);
		if (map != null) {
			List<RequirementDefinition> list = map.get(type);
			if (list != null && !list.isEmpty()) {
				List<RequirementDefinition> reqList = expectedContainerRequirements.get(type);
				if (reqList == null) {
					reqList = new ArrayList<>();
					expectedContainerRequirements.put(type, reqList);
				}
				reqList.add(list.remove(0));
			}
		}

		List<CapabilityDefinition> capList = expectedContainerCapabilities.get(type);

		for (CapabilityDefinition capabilityDefinition : capList) {
			if (capabilityDefinition.getType().equals(type)) {
				int minOccurrences = Integer.parseInt(capabilityDefinition.getMinOccurrences()) + 1;
				String minOccurrencesString = Integer.toString(minOccurrences);
				capabilityDefinition.setMinOccurrences(minOccurrencesString);
				if (!capabilityDefinition.getMaxOccurrences().equals("UNBOUNDED")) {
					int maxOccurrences = Integer.parseInt(capabilityDefinition.getMaxOccurrences()) + 1;
					String maxOccurrencesString = Integer.toString(maxOccurrences);
					capabilityDefinition.setMaxOccurrences(maxOccurrencesString);
				}
			}
		}
		expectedContainerCapabilities.put(type, capList);
	}
}
