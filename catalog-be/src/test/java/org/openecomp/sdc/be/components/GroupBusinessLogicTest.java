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

package org.openecomp.sdc.be.components;

import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * tests GroupBusinessLogic class
 * @author ns019t
 *
 */
public class GroupBusinessLogicTest {
	
	private static Logger log = LoggerFactory.getLogger(ServiceBusinessLogicTest.class.getName());
	ComponentsUtils componentsUtils = new ComponentsUtils();
	AuditingManager auditingManager = Mockito.mock(AuditingManager.class);
	final ServletContext servletContext = Mockito.mock(ServletContext.class);
	private static IGraphLockOperation graphLockOperation = Mockito.mock(IGraphLockOperation.class);
	private static GroupOperation groupOperation = Mockito.mock(GroupOperation.class);
	private static GroupDefinition groupDefenition = Mockito.mock(GroupDefinition.class);
	private static User user = Mockito.mock(User.class);
	private static String componentId = "vfUniqueId-xxxx";
	private static String groupUniqueId = "groupUniqueId-xxxx";
	@InjectMocks
	static GroupBusinessLogic bl = new GroupBusinessLogic();
	
	
	@Before
	public void setupBeforeMethod() {
		MockitoAnnotations.initMocks(this);
		ExternalConfiguration.setAppName("catalog-be");
		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
		when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
		
		componentsUtils.Init();
		componentsUtils.setAuditingManager(auditingManager);
		bl.setComponentsUtils(componentsUtils);
	}
	@BeforeClass
	public static void setupBeforeClass() {
		when(graphLockOperation.lockComponent(componentId, ComponentTypeEnum.RESOURCE.getNodeType())).thenReturn(StorageOperationStatus.OK);
//		when(groupOperation.getGroup(groupUniqueId)).thenReturn(Either.left(groupDefenition));
	}
	
	public enum ResponseEnum{
		INVALID_MIN_MAX("SVC4654"),
		INVALID_INITIAL_COUNT("SVC4655");
		
		String messageId;
		
		private ResponseEnum(String messageId){
			this.messageId = messageId;
		}

		public String getMessageId() {
			return messageId;
		}
		
	}
	/**
	 * tests the ValidateMinMaxAndInitialCountPropertyValues() method
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testValidateMinMaxAndInitialCountPropertyValues(){
		
//		Class<GroupBusinessLogic> targetClass = GroupBusinessLogic.class;
//		String methodName = "validateMinMaxAndInitialCountPropertyValues";
//		Either<Boolean, ResponseFormat> validationRes;
//
//		Map<PropertyNames, String> parentPropertyValues = new EnumMap<>(PropertyNames.class);
//		parentPropertyValues.put(PropertyNames.MIN_INSTANCES, "20");
//		parentPropertyValues.put(PropertyNames.MAX_INSTANCES, "100");
//		parentPropertyValues.put(PropertyNames.INITIAL_COUNT, "40");
//		
//		Map<PropertyNames, String> parentPropertyValues1 = new EnumMap<>(PropertyNames.class);
//		parentPropertyValues1.put(PropertyNames.MIN_INSTANCES, "20");
//		parentPropertyValues1.put(PropertyNames.MAX_INSTANCES, null);
//		parentPropertyValues1.put(PropertyNames.INITIAL_COUNT, "40");
//		
//		Map<PropertyNames, String> parentPropertyValues2 = new EnumMap<>(PropertyNames.class);
//		parentPropertyValues2.put(PropertyNames.MIN_INSTANCES, "20");
//		parentPropertyValues2.put(PropertyNames.MAX_INSTANCES, "null");
//		parentPropertyValues2.put(PropertyNames.INITIAL_COUNT, "40");
//		
//		Map<PropertyNames, String> validNewPropertyValues = new EnumMap<>(PropertyNames.class);
//		validNewPropertyValues.put(PropertyNames.MIN_INSTANCES, "20");
//		validNewPropertyValues.put(PropertyNames.MAX_INSTANCES, "100");
//		validNewPropertyValues.put(PropertyNames.INITIAL_COUNT, "50");
//		
//		Map<PropertyNames, String> validNewPropertyValues1 = new EnumMap<>(PropertyNames.class);
//		validNewPropertyValues1.put(PropertyNames.MIN_INSTANCES, "40");
//		validNewPropertyValues1.put(PropertyNames.MAX_INSTANCES, "90");
//		validNewPropertyValues1.put(PropertyNames.INITIAL_COUNT, "60");
//		
//		Map<PropertyNames, String> validNewPropertyValues2 = new EnumMap<>(PropertyNames.class);
//		validNewPropertyValues2.put(PropertyNames.MIN_INSTANCES, "40");
//		validNewPropertyValues2.put(PropertyNames.MAX_INSTANCES, null);
//		validNewPropertyValues2.put(PropertyNames.INITIAL_COUNT, "60");
//		
//		Map<PropertyNames, String> validNewPropertyValues3 = new EnumMap<>(PropertyNames.class);
//		validNewPropertyValues3.put(PropertyNames.MIN_INSTANCES, "40");
//		validNewPropertyValues3.put(PropertyNames.MAX_INSTANCES, "null");
//		validNewPropertyValues3.put(PropertyNames.INITIAL_COUNT, "60");
//		
//		Map<PropertyNames, String> validNewPropertyValues4 = new EnumMap<>(PropertyNames.class);
//		validNewPropertyValues4.put(PropertyNames.MIN_INSTANCES, null);
//		validNewPropertyValues4.put(PropertyNames.MAX_INSTANCES, null);
//		validNewPropertyValues4.put(PropertyNames.INITIAL_COUNT, "60");
//		
//		Map<PropertyNames, String> invalidNewPropertyValues = new EnumMap<>(PropertyNames.class);
//		invalidNewPropertyValues.put(PropertyNames.MIN_INSTANCES, "20");
//		invalidNewPropertyValues.put(PropertyNames.MAX_INSTANCES, "10");
//		invalidNewPropertyValues.put(PropertyNames.INITIAL_COUNT, "5");
//		
//		Map<PropertyNames, String> invalidNewPropertyValues1 = new EnumMap<>(PropertyNames.class);
//		invalidNewPropertyValues1.put(PropertyNames.MIN_INSTANCES, "5");
//		invalidNewPropertyValues1.put(PropertyNames.MAX_INSTANCES, "10");
//		invalidNewPropertyValues1.put(PropertyNames.INITIAL_COUNT, "20");
//		
//		Map<PropertyNames, String> invalidNewPropertyValues2 = new EnumMap<>(PropertyNames.class);
//		invalidNewPropertyValues2.put(PropertyNames.MIN_INSTANCES, "25");
//		invalidNewPropertyValues2.put(PropertyNames.MAX_INSTANCES, "95");
//		invalidNewPropertyValues2.put(PropertyNames.INITIAL_COUNT, "100");
//		
//		Map<PropertyNames, String> invalidNewPropertyValues3 = new EnumMap<>(PropertyNames.class);
//		invalidNewPropertyValues3.put(PropertyNames.MIN_INSTANCES, null);
//		invalidNewPropertyValues3.put(PropertyNames.MAX_INSTANCES, "95");
//		invalidNewPropertyValues3.put(PropertyNames.INITIAL_COUNT, "10");
//		
//		Map<PropertyNames, String> invalidNewPropertyValues4 = new EnumMap<>(PropertyNames.class);
//		invalidNewPropertyValues4.put(PropertyNames.MIN_INSTANCES, "30");
//		invalidNewPropertyValues4.put(PropertyNames.MAX_INSTANCES, "80");
//		invalidNewPropertyValues4.put(PropertyNames.INITIAL_COUNT, null);
//		
//		
//		Class[] argClasses = {Map.class, Map.class};
//	    try {
//	    	Method method = targetClass.getDeclaredMethod(methodName, argClasses);
//	    	method.setAccessible(true);
//	    	
//	    	Object[] argObjects2 = {invalidNewPropertyValues, parentPropertyValues};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects2);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isRight());
//	    	assertTrue(validationRes.right().value().getMessageId().equals(ResponseEnum.INVALID_MIN_MAX.getMessageId()));
//	    	
//	    	Object[] argObjects3 = {invalidNewPropertyValues1, parentPropertyValues};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects3);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isRight());
//	    	assertTrue(validationRes.right().value().getMessageId().equals(ResponseEnum.INVALID_MIN_MAX.getMessageId()));
//	    	
//	    	Object[] argObjects7 = {invalidNewPropertyValues3, parentPropertyValues};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects7);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isRight());
//	    	assertTrue(validationRes.right().value().getMessageId().equals(ResponseEnum.INVALID_MIN_MAX.getMessageId()));
//	    	
//	    	Object[] argObjects = {validNewPropertyValues, parentPropertyValues};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isLeft());
//	    	assertTrue(validationRes.left().value());
//	    	
//	    	Object[] argObjects1 = {validNewPropertyValues1, parentPropertyValues};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects1);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isLeft());
//	    	assertTrue(validationRes.left().value());
//	    	
//	    	Object[] argObjects5 = {validNewPropertyValues2, parentPropertyValues2};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects5);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isLeft());
//	    	assertTrue(validationRes.left().value());
//
//	    	Object[] argObjects6 = {validNewPropertyValues3, parentPropertyValues1};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects6);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isLeft());
//	    	assertTrue(validationRes.left().value());
//	    	
//	    	Object[] argObjects9 = {validNewPropertyValues4, parentPropertyValues1};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects9);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isLeft());
//	    	assertTrue(validationRes.left().value());
//	    	
//	    	Object[] argObjects4 = {invalidNewPropertyValues2, parentPropertyValues};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects4);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isRight());
//	    	assertTrue(validationRes.right().value().getMessageId().equals(ResponseEnum.INVALID_INITIAL_COUNT.getMessageId()));
//	    	
//	    	Object[] argObjects8 = {invalidNewPropertyValues4, parentPropertyValues};
//	    	validationRes = (Either<Boolean, ResponseFormat>) method.invoke(bl, argObjects8);
//	    	assertTrue(validationRes != null);
//	    	assertTrue(validationRes.isRight());
//	    	assertTrue(validationRes.right().value().getMessageId().equals(ResponseEnum.INVALID_INITIAL_COUNT.getMessageId()));
//	    }
//	    catch (Exception e) {
//	    	e.printStackTrace();
//	    }
	}

}
