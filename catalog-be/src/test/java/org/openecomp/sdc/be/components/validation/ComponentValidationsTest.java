/*
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
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
 */

package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;

public class ComponentValidationsTest {

	@InjectMocks
	ComponentValidations testSubject;

	@Mock
	ToscaOperationFacade toscaOperationFacadeMock;

	@Mock
	GraphLockOperation graphLockOperationMock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateComponentInstanceExist() throws Exception {
		Component component = new Resource();
		String instanceId = "";
		boolean result;

		// default test
		result = ComponentValidations.validateComponentInstanceExist(component, instanceId);
	}

	@Test
	public void testGetNormalizedName() throws Exception {
		ToscaDataDefinition toscaDataDefinition = new AdditionalInfoParameterDataDefinition();
		toscaDataDefinition.setToscaPresentationValue(JsonPresentationFields.NAME, "mock");
		String result;

		// default test
		result = ComponentValidations.getNormalizedName(toscaDataDefinition);
	}

	@Test
	public void testValidateNameIsUniqueInComponent() throws Exception {
		String currentName = "";
		String newName = "";
		String newName2 = "mock";
		Component component = new Resource();
		boolean result;

		// default test
		result = ComponentValidations.validateNameIsUniqueInComponent(currentName, newName, component);
		result = ComponentValidations.validateNameIsUniqueInComponent(currentName, newName2, component);
	}

	@Test(expected=ComponentException.class)
	public void testValidateComponentIsCheckedOutByUserAndLockIt() throws Exception {
		String componentId = "";
		String userId = "";
		Component result;
		Resource resource = new  Resource();
		resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		
		Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(resource));
		
		// default test
		result = testSubject.validateComponentIsCheckedOutByUser("",ComponentTypeEnum.RESOURCE,
				userId);
	}

	@Test
	public void testGetComponent() throws Exception {
		String componentId = "mock";
		ComponentTypeEnum componentType = null;
		Component result;
		Component resource = new Resource();
		resource.setComponentType(ComponentTypeEnum.RESOURCE);
		Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(resource));
		
		// default test
		result = Deencapsulation.invoke(testSubject, "getComponent", componentId, ComponentTypeEnum.RESOURCE);
	}

	@Test(expected = StorageException.class)
	public void testOnToscaOperationError() throws Exception {
		Component result;

		// default test
		result = Deencapsulation.invoke(testSubject, "onToscaOperationError",
				StorageOperationStatus.ARTIFACT_NOT_FOUND,"");
	}
}
