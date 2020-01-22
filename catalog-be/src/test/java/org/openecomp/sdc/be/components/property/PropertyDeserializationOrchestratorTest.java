/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PropertyDeserializationOrchestratorTest {

	@InjectMocks
	PropertyDeclarationOrchestrator testSubject;

	@Mock
	List<PropertyDeclarator> propertyDeceleratorsMock;
	
	@Mock
	private ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDecelerator;
	@Mock
	private ComponentInstancePropertyDeclarator componentInstancePropertyDecelerator;
	@Mock
	private PolicyPropertyDeclarator policyPropertyDecelerator;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeclarePropertiesToInputs() throws Exception {
		Component component = new Resource();
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		componentInstInputsMap.setComponentInstanceInputsMap(new HashMap<>());
		componentInstInputsMap.setComponentInstancePropInput(new HashMap<>());
		componentInstInputsMap.setPolicyProperties(new HashMap<>());
		componentInstInputsMap.setGroupProperties(new HashMap<>());
		Either<List<InputDefinition>, StorageOperationStatus> result;

		// default test
		result = testSubject.declarePropertiesToInputs(component, componentInstInputsMap);
	}

	@Test
	public void testUnDeclarePropertiesAsInputs() throws Exception {
		Component component = new Resource();
		InputDefinition inputToDelete = new InputDefinition();
		StorageOperationStatus result;

		Iterator<PropertyDeclarator> mockIter = Mockito.mock(Iterator.class);
		Mockito.when(propertyDeceleratorsMock.iterator()).thenReturn(mockIter);
		Mockito.when(mockIter.hasNext()).thenReturn(false);
		
		// default test
		result = testSubject.unDeclarePropertiesAsInputs(component, inputToDelete);
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
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceInputsMap.put("mock", value);
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIProperties() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceProperties.put("mock", value);
		componentInstInputsMap.setComponentInstancePropInput(componentInstanceProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIPolicy() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> policyProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		policyProperties.put("mock", value);
		componentInstInputsMap.setPolicyProperties(policyProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}
}
