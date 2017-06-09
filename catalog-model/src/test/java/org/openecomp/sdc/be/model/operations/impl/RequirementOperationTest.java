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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabiltyInstance;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class RequirementOperationTest extends ModelTestBase {
	private static Logger log = LoggerFactory.getLogger(RequirementOperationTest.class.getName());
	private Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	private static String USER_ID = "muUserId";
	private static String CATEGORY_NAME = "category/mycategory";

	@javax.annotation.Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@javax.annotation.Resource(name = "requirement-operation")
	private RequirementOperation requirementOperation;

	@javax.annotation.Resource(name = "resource-operation")
	private ResourceOperation resourceOperation;

	@javax.annotation.Resource(name = "property-operation")
	private PropertyOperation propertyOperation;

	@javax.annotation.Resource(name = "capability-operation")
	private CapabilityOperation capabilityOperation;

	@javax.annotation.Resource(name = "capability-type-operation")
	private CapabilityTypeOperation capabilityTypeOperation;

	@BeforeClass
	public static void setupBeforeClass() {

		// configurationManager = new ConfigurationManager(
		// new ConfigurationSource() {
		//
		// @Override
		// public <T> T getAndWatchConfiguration(Class<T> className,
		// ConfigurationListener configurationListener) {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// @Override
		// public <T> void addWatchConfiguration(Class<T> className,
		// ConfigurationListener configurationListener) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		//
		// Configuration configuration = new Configuration();
		// configuration.setTitanInMemoryGraph(true);
		//
		// configurationManager.setConfiguration(configuration);
		ModelTestBase.init();
	}

	@Test
	public void testDummy() {

		assertTrue(requirementOperation != null);

	}

	@Test
	public void testAddRequirementNotExistCapability() {

		String reqName = "host";
		RequirementDefinition reqDefinition = new RequirementDefinition();
		reqDefinition.setNode("tosca.nodes.Compute");
		reqDefinition.setRelationship("myrelationship");
		reqDefinition.setCapability("mycapability___2");

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource", "0.1", null, true, true);

		Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = requirementOperation.addRequirementToResource(reqName, reqDefinition, resource.getUniqueId());
		assertEquals("check error", StorageOperationStatus.INVALID_ID, addRequirementToResource.right().value());

	}

	@Before
	public void createUserAndCategory() {
		String[] category = CATEGORY_NAME.split("/");
		OperationTestsUtil.deleteAndCreateResourceCategory(category[0], category[1], titanDao);
		deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID);
	}

	private UserData deleteAndCreateUser(String userId, String firstName, String lastName) {
		UserData userData = new UserData();
		userData.setUserId(userId);
		userData.setFirstName(firstName);
		userData.setLastName(lastName);

		titanDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
		titanDao.createNode(userData, UserData.class);
		titanDao.commit();

		return userData;
	}

	/*
	 * private void deleteAndCreateCategory(String category) { CategoryData categoryData = new CategoryData(); categoryData.setName(category);
	 * 
	 * titanDao.deleteNode(categoryData, CategoryData.class); Either<CategoryData, TitanOperationStatus> createNode = titanDao .createNode(categoryData, CategoryData.class); System.out.println("after creating caetgory " + createNode);
	 * 
	 * }
	 */

	@Test
	@Ignore
	public void testAddRequirementWithCapability() {

		String capabilityTypeName = "tosca.nodes.Container";

		String reqName = "host";
		RequirementDefinition reqDefinition = new RequirementDefinition();
		reqDefinition.setNode("tosca.nodes.Compute");
		reqDefinition.setRelationship("myrelationship");
		reqDefinition.setCapability(capabilityTypeName);

		CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
		capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);

		capabilityTypeOperationTest.createCapability(capabilityTypeName);

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource", "2.0", null, true, true);

		Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = requirementOperation.addRequirementToResource(reqName, reqDefinition, resource.getUniqueId());

		assertEquals("check requirement was added", true, addRequirementToResource.isLeft());

		Either<Resource, StorageOperationStatus> resource2 = resourceOperation.getResource(resource.getUniqueId());
		String json = prettyGson.toJson(resource2);
		log.debug(json);
	}

	private void compareProperties(Map<String, PropertyDefinition> capabilityProperties, CapabiltyInstance capabiltyInstance, Map<String, String> actual) {

		Map<String, String> properties = capabiltyInstance.getProperties();

		for (Entry<String, PropertyDefinition> entry : capabilityProperties.entrySet()) {
			String paramName = entry.getKey();
			PropertyDefinition propertyDefinition = entry.getValue();
			String defaultValue = propertyDefinition.getDefaultValue();

			String value = properties.get(paramName);

			String actualValue = null;
			if (actual != null) {
				actualValue = actual.get(paramName);
			}
			if (actualValue != null) {
				assertEquals("check property value of key " + paramName, value, actualValue);
			} else {
				assertEquals("check property value of key " + paramName, value, defaultValue);
			}
		}

	}

}
