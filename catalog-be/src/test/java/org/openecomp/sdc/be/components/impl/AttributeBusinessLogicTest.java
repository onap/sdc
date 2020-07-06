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

import fj.data.Either;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.exception.ResponseFormat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.when;

import org.openecomp.sdc.be.model.DataTypeDefinition;

public class AttributeBusinessLogicTest extends BaseBusinessLogicMock{

	private AttributeBusinessLogic createTestSubject() {
		return new AttributeBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
			interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation );
	}

	UserValidations userValidations  = Mockito.mock(UserValidations.class);
	ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
	JanusGraphDao janusGraphDao = Mockito.mock(JanusGraphDao.class);
	ToscaOperationFacade toscaOperationFacade  = Mockito.mock(ToscaOperationFacade.class);
	ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
	PropertyOperation propertyOperation = Mockito.mock(PropertyOperation.class);
	Field baseBusinessLogic;
	Field baseBusinessLogic1;
	Field baseBusinessLogic2;
	Field baseBusinessLogic3;
	AttributeBusinessLogic attributeBusinessLogic=createTestSubject();
	IGraphLockOperation igraphLockOperation = Mockito.mock(IGraphLockOperation.class);

	@Before
	public void setup() throws Exception{

		baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("graphLockOperation");
		baseBusinessLogic.setAccessible(true);
		baseBusinessLogic.set(attributeBusinessLogic, igraphLockOperation);

		baseBusinessLogic1 = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("userValidations");
		baseBusinessLogic1.setAccessible(true);
		baseBusinessLogic1.set(attributeBusinessLogic, userValidations);

		baseBusinessLogic2 = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("componentsUtils");
		baseBusinessLogic2.setAccessible(true);
		baseBusinessLogic2.set(attributeBusinessLogic, componentsUtils);

		baseBusinessLogic3 = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("janusGraphDao");
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
	public void testCreateAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		PropertyDefinition newAttributeDef = null;
		String userId = "";
		Either<AttributeDataDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	@Test
	public void testIsAttributeExist() throws Exception {
		AttributeBusinessLogic testSubject;List<AttributeDataDefinition> attributes = null;
		String resourceUid = "";
		String propertyName = "";
		boolean result;

		// test 1
		testSubject=createTestSubject();attributes = null;
	}

	@Test
	public void testGetAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		String attributeId = "";
		String userId = "";
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	@Test
	public void testUpdateAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		String attributeId = "";
		PropertyDefinition newAttDef = null;
		String userId = "";
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	@Test
	public void testDeleteAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		String attributeId = "";
		String userId = "";
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	@Test
	public void createAttribute_lockfail() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> response;
		AttributeDataDefinition prop= new AttributeDataDefinition();

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isRight());

	}

	@Test
	public void createAttribute_Success() throws Exception {

		Component resource = new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		AttributeDefinition attrib = new AttributeDefinition();
		attrib.setType(ToscaPropertyType.STRING.getType());

		when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		AttributeDataDefinition attributeDataDefinition = new AttributeDataDefinition();
		Either<AttributeDataDefinition, StorageOperationStatus> either = Either.left(attributeDataDefinition);
		when(toscaOperationFacade.addAttributeOfResource(anyObject(), anyObject())).thenReturn(either);

		when(propertyOperation.isPropertyTypeValid(anyObject())).thenReturn(true);

		Map<String, DataTypeDefinition> data = new HashMap<>();
		data.put("ONE", new DataTypeDefinition());
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
		when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

		when(propertyOperation.isPropertyDefaultValueValid(anyObject(), anyObject())).thenReturn(true);
		Either<AttributeDataDefinition, ResponseFormat> response;

		response = attributeBusinessLogic.createAttribute("RES01", attrib, "USR01");

		Assert.assertEquals(true, response.isLeft());

	}

	@Test
	public void createAttribute_failtogettoscaelement() throws Exception {

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<AttributeDataDefinition, ResponseFormat> response;
		AttributeDataDefinition prop= new AttributeDataDefinition();

		baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("toscaOperationFacade");
		baseBusinessLogic.setAccessible(true);
		baseBusinessLogic.set(attributeBusinessLogic, toscaOperationFacade);
		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.GENERAL_ERROR);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isRight());

	}

	@Test
	public void createAttribute_componentvalidationfails() throws Exception {

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR02");

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<AttributeDataDefinition, ResponseFormat> response;
		AttributeDataDefinition prop= new AttributeDataDefinition();

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isRight());

	}

	@Test
	public void createAttribute_componentalreadyexist_fails() throws Exception {

		Either<AttributeDataDefinition, ResponseFormat> response;
		AttributeDefinition attrib = new AttributeDefinition();
		attrib.setName("RES01");
		attrib.setParentUniqueId("RES01");

		List<AttributeDataDefinition> attributes = new ArrayList<>();
		attributes.add(attrib);

		Component resource = new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");
		((Resource) resource).setAttributes(attributes);

		when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", attrib, "USR01");

		Assert.assertEquals(true, response.isRight());

	}

	@Test
	public void createAttribute_addresourcetostoragefails() throws Exception {

		Component resource = new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		IGraphLockOperation igraphLockOperation = Mockito.mock(IGraphLockOperation.class);
		when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		AttributeDataDefinition attributeDataDefinition = new AttributeDataDefinition();
		Either<AttributeDataDefinition, StorageOperationStatus> either = Either
			.right(StorageOperationStatus.CONNECTION_FAILURE);
		when(toscaOperationFacade.addAttributeOfResource(anyObject(), anyObject())).thenReturn(either);

		when(propertyOperation.isPropertyTypeValid(anyObject())).thenReturn(true);

		Map<String, DataTypeDefinition> data = new HashMap<>();
		data.put("ONE", new DataTypeDefinition());
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
		when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

		when(propertyOperation.isPropertyDefaultValueValid(anyObject(), anyObject())).thenReturn(true);
		Either<AttributeDataDefinition, ResponseFormat> response;

		AttributeDataDefinition attrib = new AttributeDefinition();
		response = attributeBusinessLogic.createAttribute("RES01", attrib, "USR01");

		Assert.assertEquals(true, response.isRight());

	}

	@Test
	public void testgetAttribute_ATTRIBUTE_NOT_FOUND() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.getAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testgetAttribute_success() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;

		Component resource = new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		AttributeDefinition attrib = new AttributeDefinition();
		attrib.setUniqueId("ATTR01");
		attrib.setParentUniqueId("RES01");

		List<AttributeDataDefinition> attr = new ArrayList<>();
		attr.add(attrib);

		((Resource) resource).setAttributes(attr);
		Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result = attributeBusinessLogic.getAttribute("RES01", "ATTR01", "USR01");
		Assert.assertEquals(true, result.isLeft());
	}

	@Test
	public void testgetAttribute_RESOURCE_NOT_FOUND() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;

		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.getAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_FAILED_TO_LOCK_COMPONENT() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_get_RESOURCE_from_DB_failed() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;

		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.CONNECTION_FAILURE);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_get_RESOURCE_verification_failed() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;


		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(true);
		resource.setLastUpdaterUserId("USR01");


		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);




		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_nonexistingresource() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;


		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_success() throws Exception {
		Either<AttributeDataDefinition, ResponseFormat> result;


		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

		Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		when(toscaOperationFacade.deleteAttributeOfResource(any(), any())).thenReturn(StorageOperationStatus.OK);

		AttributeDefinition attrib = new AttributeDefinition();
		attrib.setUniqueId("ATTR01");
		attrib.setParentUniqueId("RES01");
		List<AttributeDataDefinition> attributes = new ArrayList<>();
		attributes.add(attrib);
		((Resource) resource).setAttributes(attributes);

		result = attributeBusinessLogic.deleteAttribute("RES01", "ATTR01", "USR01");
		Assert.assertEquals(true, result.isLeft());
	}

}
