package org.openecomp.sdc.be.components.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.exception.ResponseFormat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.when;

import fj.data.Either;


public class AttributeBusinessLogicTest {

	private AttributeBusinessLogic createTestSubject() {
		return new AttributeBusinessLogic();
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
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}


	@Test
	public void testIsAttributeExist() throws Exception {
		AttributeBusinessLogic testSubject;List<PropertyDefinition> attributes = null;
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
		Either<PropertyDefinition, ResponseFormat> response;
		PropertyDefinition prop= new PropertyDefinition();

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isRight());

	}

	@Test
	public void createAttribute_Success() throws Exception {

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		PropertyDefinition prop= new PropertyDefinition();
		prop.setType(ToscaPropertyType.STRING.getType());

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		Either<PropertyDefinition, StorageOperationStatus> either = Either.left(propertyDefinition);
		when(toscaOperationFacade.addAttributeOfResource(anyObject(),anyObject())).thenReturn(either);

		when(propertyOperation.isPropertyTypeValid(anyObject())).thenReturn(true);

		Map<String,DataTypeDefinition> data=new HashMap<>();
		data.put("ONE",new DataTypeDefinition());
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
		when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

		when(propertyOperation.isPropertyDefaultValueValid(anyObject(),anyObject())).thenReturn(true);
		Either<PropertyDefinition, ResponseFormat> response;

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isLeft());

	}

	@Test
	public void createAttribute_failtogettoscaelement() throws Exception {

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

		Either<PropertyDefinition, ResponseFormat> response;
		PropertyDefinition prop= new PropertyDefinition();

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isRight());

	}

	@Test
	public void createAttribute_componentalreadyexist_fails() throws Exception {

		Either<PropertyDefinition, ResponseFormat> response;
		PropertyDefinition prop= new PropertyDefinition();
		prop.setName("RES01");
		prop.setParentUniqueId("RES01");

		List<PropertyDefinition> attributes = new ArrayList<>();
		attributes.add(prop);

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");
		((Resource) resource).setAttributes(attributes);

		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isRight());

	}


	@Test
	public void createAttribute_addresourcetostoragefails() throws Exception {

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		PropertyDefinition prop= new PropertyDefinition();
		prop.setType(ToscaPropertyType.STRING.getType());

		IGraphLockOperation igraphLockOperation = Mockito.mock(IGraphLockOperation.class);
		when(igraphLockOperation.lockComponent(any(),any())).thenReturn(StorageOperationStatus.OK);

		//Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.INVALID_PROPERTY);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		Either<PropertyDefinition, StorageOperationStatus> either = Either.right(StorageOperationStatus.CONNECTION_FAILURE);
		when(toscaOperationFacade.addAttributeOfResource(anyObject(),anyObject())).thenReturn(either);

		when(propertyOperation.isPropertyTypeValid(anyObject())).thenReturn(true);

		Map<String,DataTypeDefinition> data=new HashMap<>();
		data.put("ONE",new DataTypeDefinition());
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
		when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

		when(propertyOperation.isPropertyDefaultValueValid(anyObject(),anyObject())).thenReturn(true);
		Either<PropertyDefinition, ResponseFormat> response;

		response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

		Assert.assertEquals(true,response.isRight());

	}

	@Test
	public void testgetAttribute_ATTRIBUTE_NOT_FOUND() throws Exception {
		Either<PropertyDefinition, ResponseFormat> result;

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
		Either<PropertyDefinition, ResponseFormat> result;

		Component resource= new Resource();
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		resource.setIsDeleted(false);
		resource.setLastUpdaterUserId("USR01");

		PropertyDefinition prop= new PropertyDefinition();
		prop.setUniqueId("ATTR01");
		prop.setParentUniqueId("RES01");

		List<PropertyDefinition> attr = new ArrayList<>();
		attr.add(prop);

		((Resource) resource).setAttributes(attr);
		Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.getAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isLeft());
	}

	@Test
	public void testgetAttribute_RESOURCE_NOT_FOUND() throws Exception {
		Either<PropertyDefinition, ResponseFormat> result;

		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.getAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_FAILED_TO_LOCK_COMPONENT() throws Exception {
		Either<PropertyDefinition, ResponseFormat> result;

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_get_RESOURCE_from_DB_failed() throws Exception {
		Either<PropertyDefinition, ResponseFormat> result;

		Either<Component, StorageOperationStatus> toscastatus=Either.right(StorageOperationStatus.CONNECTION_FAILURE);
		when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isRight());
	}

	@Test
	public void testdeleteAttribute_get_RESOURCE_verification_failed() throws Exception {
		Either<PropertyDefinition, ResponseFormat> result;


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
		Either<PropertyDefinition, ResponseFormat> result;


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
		Either<PropertyDefinition, ResponseFormat> result;


		Component resource= new Resource();
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
		((Resource) resource).setAttributes(attributes);

		result=attributeBusinessLogic.deleteAttribute("RES01","ATTR01", "USR01");
		Assert.assertEquals(true,result.isLeft());
	}


}