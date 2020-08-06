/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.exception.ResponseFormat;


public class AttributeBusinessLogicTest extends BaseBusinessLogicMock {

	private AttributeBusinessLogic createTestSubject() {
		return new AttributeBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
			interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation );
	}

	private UserValidations userValidations  = Mockito.mock(UserValidations.class);
	private ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
	private JanusGraphDao janusGraphDao = Mockito.mock(JanusGraphDao.class);
	private ToscaOperationFacade toscaOperationFacade  = Mockito.mock(ToscaOperationFacade.class);
	private ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
	private PropertyOperation propertyOperation = Mockito.mock(PropertyOperation.class);
	private Field baseBusinessLogic;
	private AttributeBusinessLogic attributeBusinessLogic=createTestSubject();
	private IGraphLockOperation igraphLockOperation = Mockito.mock(IGraphLockOperation.class);


	@Before
	public void setup() throws Exception {
		baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("graphLockOperation");
		baseBusinessLogic.setAccessible(true);
		baseBusinessLogic.set(attributeBusinessLogic, igraphLockOperation);

		final Field baseBusinessLogic1 = attributeBusinessLogic.getClass().getSuperclass()
			.getDeclaredField("userValidations");
		baseBusinessLogic1.setAccessible(true);
		baseBusinessLogic1.set(attributeBusinessLogic, userValidations);

		final Field baseBusinessLogic2 = attributeBusinessLogic.getClass().getSuperclass()
			.getDeclaredField("componentsUtils");
		baseBusinessLogic2.setAccessible(true);
		baseBusinessLogic2.set(attributeBusinessLogic, componentsUtils);

		final Field baseBusinessLogic3 = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("janusGraphDao");
		baseBusinessLogic3.setAccessible(true);
		baseBusinessLogic3.set(attributeBusinessLogic, janusGraphDao);

		baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("toscaOperationFacade");
		baseBusinessLogic.setAccessible(true);
		baseBusinessLogic.set(attributeBusinessLogic, toscaOperationFacade);

		baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("applicationDataTypeCache");
		baseBusinessLogic.setAccessible(true);
		baseBusinessLogic.set(attributeBusinessLogic, applicationDataTypeCache);

		baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("propertyOperation");
		baseBusinessLogic.setAccessible(true);
		baseBusinessLogic.set(attributeBusinessLogic, propertyOperation);
	}

	@Test
	public void createAttribute_lockfail() {
		Either<PropertyDefinition, ResponseFormat> response;
		PropertyDefinition prop= new PropertyDefinition();

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		assertTrue(response.isRight());
	}

	@Test
	public void createAttribute_Success() {
		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		PropertyDefinition prop= new PropertyDefinition();
		prop.setType(ToscaPropertyType.STRING.getType());

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		Either<PropertyDefinition, StorageOperationStatus> either = Either.left(propertyDefinition);
		when(toscaOperationFacade.addAttributeOfResource(any(), any())).thenReturn(either);

		when(propertyOperation.isPropertyTypeValid(any(), any())).thenReturn(true);

		Map<String,DataTypeDefinition> data=new HashMap<>();
		data.put("ONE",new DataTypeDefinition());
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
		when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

		when(propertyOperation.isPropertyDefaultValueValid(any(), any())).thenReturn(true);
		Either<PropertyDefinition, ResponseFormat> response;

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		assertTrue(response.isLeft());
	}

	@Test
	public void createAttribute_failtogettoscaelement() throws NoSuchFieldException, IllegalAccessException {
		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<PropertyDefinition, ResponseFormat> response;
		PropertyDefinition prop= new PropertyDefinition();

		baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("toscaOperationFacade");
		baseBusinessLogic.setAccessible(true);
		baseBusinessLogic.set(attributeBusinessLogic, toscaOperationFacade);
		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.GENERAL_ERROR);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		assertTrue(response.isRight());
	}

	@Test
	public void createAttribute_componentvalidationfails() {
		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR02");

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<PropertyDefinition, ResponseFormat> response;
		PropertyDefinition prop= new PropertyDefinition();

		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		assertTrue(response.isRight());
	}

	@Test
	public void createAttribute_componentalreadyexist_fails() {
		Either<PropertyDefinition, ResponseFormat> response;
		PropertyDefinition prop= new PropertyDefinition();
		prop.setName("RES01");
		prop.setParentUniqueId("RES01");

		List<PropertyDefinition> attributes = new ArrayList<>();
		attributes.add(prop);

		final Resource resource = new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");
		resource.setAttributes(attributes);

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		assertTrue(response.isRight());
	}


	@Test
	public void createAttribute_addresourcetostoragefails() {

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		PropertyDefinition prop= new PropertyDefinition();
		prop.setType(ToscaPropertyType.STRING.getType());

		IGraphLockOperation igraphLockOperation = Mockito.mock(IGraphLockOperation.class);
		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		Either<PropertyDefinition, StorageOperationStatus> either = Either.right(StorageOperationStatus.CONNECTION_FAILURE);
		when(toscaOperationFacade.addAttributeOfResource(any(),any())).thenReturn(either);

		when(propertyOperation.isPropertyTypeValid(any())).thenReturn(true);

		Map<String,DataTypeDefinition> data=new HashMap<>();
		data.put("ONE",new DataTypeDefinition());
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
		when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

		when(propertyOperation.isPropertyDefaultValueValid(any(),any())).thenReturn(true);
		Either<PropertyDefinition, ResponseFormat> response;

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		assertTrue(response.isRight());

	}

	@Test
	public void testgetAttribute_ATTRIBUTE_NOT_FOUND() {
		Either<PropertyDefinition, ResponseFormat> result;

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.getAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isRight());
	}

	@Test
	public void testgetAttribute_success() {
		Either<PropertyDefinition, ResponseFormat> result;

		final Resource resource = new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		PropertyDefinition prop= new PropertyDefinition();
		prop.setUniqueId("ATTR01");
		prop.setParentUniqueId("RES01");

		List<PropertyDefinition> attr = new ArrayList<>();
		attr.add(prop);

		resource.setAttributes(attr);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.getAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isLeft());
	}

	@Test
	public void testgetAttribute_RESOURCE_NOT_FOUND() {
		Either<PropertyDefinition, ResponseFormat> result;

		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.getAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isRight());
	}

	@Test
	public void testdeleteAttribute_FAILED_TO_LOCK_COMPONENT() {
		Either<PropertyDefinition, ResponseFormat> result;

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isRight());
	}

	@Test
	public void testdeleteAttribute_get_RESOURCE_from_DB_failed() {
		Either<PropertyDefinition, ResponseFormat> result;

		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.CONNECTION_FAILURE);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isRight());
	}

	@Test
	public void testdeleteAttribute_get_RESOURCE_verification_failed() {
		Either<PropertyDefinition, ResponseFormat> result;
		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(true);
		resource.setLastUpdaterUserId("USR01");
		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isRight());
	}

	@Test
	public void testdeleteAttribute_nonexistingresource() {
		Either<PropertyDefinition, ResponseFormat> result;


		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isRight());
	}

	@Test
	public void testdeleteAttribute_success() {
		Either<PropertyDefinition, ResponseFormat> result;
		final Resource resource = new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		when(toscaOperationFacade.deleteAttributeOfResource(any(),any())).thenReturn(StorageOperationStatus.OK);

		PropertyDefinition prop= new PropertyDefinition();
		prop.setUniqueId("ATTR01");
		prop.setParentUniqueId("RES01");
		List<PropertyDefinition> attributes = new ArrayList<>();
		attributes.add(prop);
		resource.setAttributes(attributes);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		assertTrue(result.isLeft());
	}


}