/*
 * ============LICENSE_START=============================================================================================================
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * ============LICENSE_END===============================================================================================================
 *
 */
package org.openecomp.sdc.be.components.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentInstancePropertyToPolicyDeclarator;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentPropertyToPolicyDeclarator;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class PropertyDeclarationOrchestratorTest {

	@InjectMocks
	PropertyDeclarationOrchestrator testSubject;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	List<PropertyDeclarator> propertyDeceleratorsMock;

	@Mock
	private ComponentInstancePropertyToPolicyDeclarator componentInstancePropertyToPolicyDeclarator;

	@Mock
	private ComponentPropertyToPolicyDeclarator componentPropertyToPolicyDeclarator;

	@Mock
	private ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDecelerator;

	@Mock
	private ComponentInstancePropertyDeclarator componentInstancePropertyDecelerator;

	@Mock
	private ComponentPropertyDeclarator servicePropertyDeclarator;

	@Mock
	private PolicyPropertyDeclarator policyPropertyDecelerator;

	@Mock
	private GroupPropertyDeclarator groupPropertyDeclarator;;

	private static final String PROP_UID = "propertyUid";
	private static final String SERVICE_UID = "serviceUid";

	private Service service;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		service = new ServiceBuilder().setUniqueId(SERVICE_UID).build();
	}

	@Test(expected = IllegalStateException.class)
	public void testDeclarePropertiesToInputs() throws Exception {
		Component component = new Resource();
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Either<List<InputDefinition>, StorageOperationStatus> result;

		// default test
		result = testSubject.declarePropertiesToInputs(component, componentInstInputsMap);
	}

	@Test
	public void testDeclarePropertiesToListInputs() throws Exception {
		Component component = new Resource();
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = getPropertiesMapToDeclare();
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		InputDefinition input = new InputDefinition();
		input.setUniqueId(PROP_UID);
		Either<InputDefinition, StorageOperationStatus> result;

		when(componentInstanceInputPropertyDecelerator.declarePropertiesAsListInput(eq(component), anyString(), anyList(), eq(input))).thenReturn(Either.left(input));
		// default test
		result = testSubject.declarePropertiesToListInput(component, componentInstInputsMap, input);

		assertTrue(result.isLeft());
		assertEquals(PROP_UID, result.left().value().getUniqueId());
	}

	@Test
	public void testUnDeclarePropertiesAsInputs() throws Exception {
		Component component = new Resource();
		InputDefinition inputToDelete = new InputDefinition();
		StorageOperationStatus result;

		Iterator<PropertyDeclarator> mockIter = Mockito.mock(Iterator.class);
		when(propertyDeceleratorsMock.iterator()).thenReturn(mockIter);
		when(mockIter.hasNext()).thenReturn(false);

		setInputUndeclarationStubbings(component, inputToDelete);

		// default test
		result = testSubject.unDeclarePropertiesAsInputs(component, inputToDelete);

		assertEquals(StorageOperationStatus.OK, result);
	}

	@Test
	public void testUnDeclarePropertiesAsListInputs() throws Exception {
		Component component = new Resource();
		InputDefinition inputToDelete = new InputDefinition();
		StorageOperationStatus result;

		Iterator<PropertyDeclarator> mockIter = Mockito.mock(Iterator.class);
		Mockito.when(propertyDeceleratorsMock.iterator()).thenReturn(mockIter);
		Mockito.when(mockIter.hasNext()).thenReturn(false);

		// default test
		result = testSubject.unDeclarePropertiesAsListInputs(component, inputToDelete);
	}

	@Test
	public void testGetPropOwnerId() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceInputsMap.put("mock", value);
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		String result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropOwnerId", componentInstInputsMap);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetPropertyDecelerator() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithInputsMap() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = getPropertiesMapToDeclare();
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);

		assertTrue(result instanceof ComponentInstanceInputPropertyDeclarator);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIProperties() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceProperties.put("mock", value);
		componentInstInputsMap.setComponentInstanceProperties(componentInstanceProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);

		assertTrue(result instanceof ComponentInstancePropertyDeclarator);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIPolicy() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> policyProperties = getPropertiesMapToDeclare();
		componentInstInputsMap.setPolicyProperties(policyProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);

		assertTrue(result instanceof PolicyPropertyDeclarator);
	}

	@Test
	public void testDeclarePropertiesToPolicies() {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> policyProperties = getPropertiesMapToDeclare();
		componentInstInputsMap.setComponentInstancePropertiesToPolicies(policyProperties);

		PolicyDefinition declaredPolicy = getDeclaredPolicy();

		when(componentInstancePropertyToPolicyDeclarator.declarePropertiesAsPolicies(any(), anyString(), anyList())).thenReturn(
				Either.left(Collections.singletonList(declaredPolicy)));

		Either<List<PolicyDefinition>, StorageOperationStatus> declareEither =
				testSubject.declarePropertiesToPolicies(service, componentInstInputsMap);

		validatePolicyDeclaration(declaredPolicy, declareEither);
	}

	@Test
	public void testUndeclarePropertiesAsPolicies() {
		when(componentInstancePropertyToPolicyDeclarator.unDeclarePropertiesAsPolicies(any(), any(PolicyDefinition.class))).thenReturn(StorageOperationStatus.OK);
		when(componentPropertyToPolicyDeclarator.unDeclarePropertiesAsPolicies(any(), any(PolicyDefinition.class))).thenReturn(StorageOperationStatus.OK);

		StorageOperationStatus undeclareStatus =
				testSubject.unDeclarePropertiesAsPolicies(service, getDeclaredPolicy());

		assertEquals(StorageOperationStatus.OK, undeclareStatus);
	}

	@Test
	public void testDeclareServiceProperties() {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> serviceProperties = getPropertiesMapToDeclare();
		componentInstInputsMap.setServiceProperties(serviceProperties);

		PolicyDefinition declaredPolicy = getDeclaredPolicy();

		when(servicePropertyDeclarator.declarePropertiesAsPolicies(any(), anyString(), anyList())).thenReturn(
				Either.left(Collections.singletonList(declaredPolicy)));

		Either<List<PolicyDefinition>, StorageOperationStatus> declareEither =
				testSubject.declarePropertiesToPolicies(service, componentInstInputsMap);

		validatePolicyDeclaration(declaredPolicy, declareEither);
	}

	private PolicyDefinition getDeclaredPolicy() {
		PolicyDefinition declaredPolicy = new PolicyDefinition();
		declaredPolicy.setUniqueId(PROP_UID);
		return declaredPolicy;
	}

	private Map<String, List<ComponentInstancePropInput>> getPropertiesMapToDeclare() {
		Map<String, List<ComponentInstancePropInput>> policyProperties = new HashMap<>();

		ComponentInstancePropInput propertyToDeclare = new ComponentInstancePropInput();
		propertyToDeclare.setUniqueId(PROP_UID);
		propertyToDeclare.setPropertiesName(PROP_UID);

		policyProperties.put("mock", Collections.singletonList(propertyToDeclare));
		return policyProperties;
	}

	private void validatePolicyDeclaration(PolicyDefinition declaredPolicy,
			Either<List<PolicyDefinition>, StorageOperationStatus> declareEither) {
		assertTrue(declareEither.isLeft());

		List<PolicyDefinition> declaredPolicies = declareEither.left().value();
		assertEquals(1, declaredPolicies.size());
		assertEquals(declaredPolicy.getUniqueId(), declaredPolicies.get(0).getUniqueId());
	}

	private void setInputUndeclarationStubbings(Component component, InputDefinition inputToDelete) {
		when(policyPropertyDecelerator.unDeclarePropertiesAsInputs(eq(component), eq(inputToDelete))).thenReturn(
				StorageOperationStatus.OK);
		when(servicePropertyDeclarator.unDeclarePropertiesAsInputs(eq(component), eq(inputToDelete))).thenReturn(StorageOperationStatus.OK);
		when(componentInstancePropertyDecelerator.unDeclarePropertiesAsInputs(eq(component), eq(inputToDelete))).thenReturn(StorageOperationStatus.OK);
		when(componentInstanceInputPropertyDecelerator.unDeclarePropertiesAsInputs(eq(component), eq(inputToDelete))).thenReturn(StorageOperationStatus.OK);
		when(groupPropertyDeclarator.unDeclarePropertiesAsInputs(eq(component), eq(inputToDelete))).thenReturn(StorageOperationStatus.OK);
	}
}
