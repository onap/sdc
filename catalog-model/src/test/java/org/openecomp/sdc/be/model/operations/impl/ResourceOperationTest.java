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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.LifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
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
public class ResourceOperationTest extends ModelTestBase {

	private static Logger log = LoggerFactory.getLogger(ResourceOperationTest.class.getName());
	@javax.annotation.Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@javax.annotation.Resource(name = "resource-operation")
	private ResourceOperation resourceOperation;

	@javax.annotation.Resource(name = "property-operation")
	private PropertyOperation propertyOperation;

	@javax.annotation.Resource(name = "lifecycle-operation")
	private LifecycleOperation lifecycleOperation;

	@javax.annotation.Resource(name = "capability-operation")
	private CapabilityOperation capabilityOperation;

	@javax.annotation.Resource(name = "capability-type-operation")
	private CapabilityTypeOperation capabilityTypeOperation;

	private static String CATEGORY_NAME = "category/mycategory";
	private static String CATEGORY_NAME_UPDATED = "category1/updatedcategory";

	@BeforeClass
	public static void setupBeforeClass() {

		ModelTestBase.init();
	}

	public void setOperations(TitanGenericDao titanGenericDao, ResourceOperation resourceOperation,
			PropertyOperation propertyOperation) {
		this.titanDao = titanGenericDao;
		this.resourceOperation = resourceOperation;
		this.propertyOperation = propertyOperation;
	}

	@Test
	public void dummyTest() {

	}

	private Resource buildResourceMetadata(String userId, String category, String resourceName,
			String resourceVersion) {

		Resource resource = new Resource();
		resource.setName(resourceName);
		resource.setVersion(resourceVersion);
		;
		resource.setDescription("description 1");
		resource.setAbstract(false);
		resource.setCreatorUserId(userId);
		resource.setContactId("contactId@sdc.com");
		resource.setVendorName("vendor 1");
		resource.setVendorRelease("1.0.0");
		resource.setToscaResourceName(resourceName);
		String[] categoryArr = category.split("/");
		resource.addCategory(categoryArr[0], categoryArr[1]);
		resource.setIcon("images/my.png");
		List<String> tags = new ArrayList<String>();
		tags.add("TAG1");
		tags.add("TAG2");
		resource.setTags(tags);
		return resource;
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

	private void deleteAndCreateCategory(String category) {
		String[] names = category.split("/");
		OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanDao);
	}

	public Resource createResource(String userId, String category, String resourceName, String resourceVersion,
			String parentResourceName, boolean isAbstract, boolean isHighestVersion) {

		String propName1 = "disk_size";
		String propName2 = "num_cpus";

		List<String> derivedFrom = new ArrayList<String>();
		if (parentResourceName != null) {
			derivedFrom.add(parentResourceName);
		}
		Resource resource = buildResourceMetadata(userId, category, resourceName, resourceVersion);

		resource.setAbstract(isAbstract);
		resource.setHighestVersion(isHighestVersion);

		Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();

		PropertyDefinition property1 = new PropertyDefinition();
		property1.setDefaultValue("10");
		property1.setDescription(
				"Size of the local disk, in Gigabytes (GB), available to applications running on the Compute node.");
		property1.setType(ToscaType.INTEGER.name().toLowerCase());
		List<PropertyConstraint> constraints = new ArrayList<PropertyConstraint>();
		GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
		log.debug("{}", propertyConstraint1);

		constraints.add(propertyConstraint1);

		LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
		constraints.add(propertyConstraint2);

		property1.setConstraints(constraints);

		properties.put(propName1, property1);

		PropertyDefinition property2 = new PropertyDefinition();
		property2.setDefaultValue("2");
		property2.setDescription("Number of (actual or virtual) CPUs associated with the Compute node.");
		property2.setType(ToscaType.INTEGER.name().toLowerCase());
		List<PropertyConstraint> constraints3 = new ArrayList<PropertyConstraint>();
		List<String> range = new ArrayList<String>();
		range.add("1");
		range.add("4");

		InRangeConstraint propertyConstraint3 = new InRangeConstraint(range);
		constraints3.add(propertyConstraint3);
		// property2.setConstraints(constraints3);
		property2.setConstraints(constraints3);
		properties.put(propName2, property2);

		resource.setDerivedFrom(derivedFrom);

		resource.setProperties(convertMapToList(properties));

		Either<Resource, StorageOperationStatus> result = resourceOperation.createResource(resource, true);

		assertTrue(result.isLeft());
		Resource resultResource = result.left().value();

		// assertEquals("check resource unique id",
		// UniqueIdBuilder.buildResourceUniqueId(resourceName,
		// resourceVersion), resultResource.getUniqueId());
		assertEquals("check resource state", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				resultResource.getLifecycleState());

		// retrieve property from graph
		String resourceId = resultResource.getUniqueId();
		// String resourceId = UniqueIdBuilder.buildResourceUniqueId(
		// resource.getResourceName(), resource.getResourceVersion());

		Either<PropertyDefinition, StorageOperationStatus> either = propertyOperation.getPropertyOfResource(propName1,
				resourceId);

		assertTrue(either.isLeft());
		PropertyDefinition propertyDefinition = either.left().value();
		assertEquals("check property default value", property1.getDefaultValue(), propertyDefinition.getDefaultValue());
		assertEquals("check property description", property1.getDescription(), propertyDefinition.getDescription());
		assertEquals("check property type", property1.getType(), propertyDefinition.getType());
		assertEquals("check property unique id", property1.getUniqueId(), propertyDefinition.getUniqueId());
		assertEquals("check property consitraints size", property1.getConstraints().size(),
				propertyDefinition.getConstraints().size());

		return resultResource;

	}

	public static List<PropertyDefinition> convertMapToList(Map<String, PropertyDefinition> properties) {
		if (properties == null) {
			return null;
		}

		List<PropertyDefinition> definitions = new ArrayList<>();
		for (Entry<String, PropertyDefinition> entry : properties.entrySet()) {
			String name = entry.getKey();
			PropertyDefinition propertyDefinition = entry.getValue();
			propertyDefinition.setName(name);
			definitions.add(propertyDefinition);
		}

		return definitions;
	}

	@Test
	public void testFollowed() {
		String rootName = "Root123";

		String userId = "jh0003";
		String category = CATEGORY_NAME;
		deleteAndCreateUser(userId, "first_" + userId, "last_" + userId);
		deleteAndCreateCategory(category);

		Resource rootResource = createResource(userId, category, rootName, "1.0", null, false, true);
		log.debug(" *** create **");
		log.debug("{}", rootResource);
		String resourceId = rootResource.getUniqueId();

		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		Set<LifecycleStateEnum> lastStateStates = new HashSet<LifecycleStateEnum>();
		lastStateStates.add(LifecycleStateEnum.CERTIFIED);

		Either<List<Resource>, StorageOperationStatus> followed = resourceOperation.getFollowed(userId, lifecycleStates,
				lastStateStates, false);
		assertTrue(followed.isLeft());
		List<Resource> list = followed.left().value();

		assertEquals(1, list.size());
		resourceOperation.deleteResource(resourceId);

		followed = resourceOperation.getFollowed(userId, lifecycleStates, lastStateStates, false);
		assertTrue(followed.isLeft());
		list = followed.left().value();
		assertTrue(list.isEmpty());

	}

	@Ignore
	@Test
	public void testGetLatestVersion() {
		String rootName = "Root123";

		String userId = "jh0003";
		String category = "category/mycategory";
		deleteAndCreateUser(userId, "first_" + userId, "last_" + userId);
		deleteAndCreateCategory(category);

		Either<Resource, StorageOperationStatus> latestByName = resourceOperation.getLatestByName(rootName, true);
		assertTrue(latestByName.isRight());
		assertEquals(StorageOperationStatus.NOT_FOUND, latestByName.right().value());

		Resource rootResource = createResource(userId, category, rootName, "1.0", null, false, true);

		latestByName = resourceOperation.getLatestByName(rootName, true);
		assertTrue(latestByName.isLeft());

		Resource rootResourceHighest = createResource(userId, category, rootName, "1.3", null, false, true);

		latestByName = resourceOperation.getLatestByName(rootName, false);
		assertTrue(latestByName.isLeft());
		assertEquals(rootResourceHighest.getUniqueId(), latestByName.left().value().getUniqueId());

		resourceOperation.deleteResource(rootResource.getUniqueId());
		resourceOperation.deleteResource(rootResourceHighest.getUniqueId());
	}

	@Test
	public void testOverrideResource() {
		String rootName = "Root123";

		String userId = "jh0003";
		String category = CATEGORY_NAME;
		String updatedCategory = CATEGORY_NAME_UPDATED;
		deleteAndCreateUser(userId, "first_" + userId, "last_" + userId);
		deleteAndCreateCategory(category);
		deleteAndCreateCategory(updatedCategory);

		Resource rootResource = createResource(userId, category, rootName, "1.1", null, false, true);

		rootResource.setCategories(null);
		String[] updateArr = updatedCategory.split("/");
		rootResource.addCategory(updateArr[0], updateArr[1]);
		List<PropertyDefinition> properties = rootResource.getProperties();
		PropertyDefinition propertyDefinition = findProperty(properties, "disk_size");

		rootResource.setProperties(new ArrayList<PropertyDefinition>());
		propertyDefinition.setName("myProperty");
		rootResource.getProperties().add(propertyDefinition);

		Either<Resource, StorageOperationStatus> overrideResource = resourceOperation.overrideResource(rootResource,
				rootResource, false);

		assertTrue(overrideResource.isLeft());
		Resource resourceAfter = overrideResource.left().value();
		assertEquals(1, resourceAfter.getProperties().size());

		assertNotNull(findProperty(resourceAfter.getProperties(), "myProperty"));
		assertEquals(1, resourceAfter.getCategories().size());
		assertEquals(1, resourceAfter.getCategories().get(0).getSubcategories().size());

		assertEquals(updateArr[0], resourceAfter.getCategories().get(0).getName());
		assertEquals(updateArr[1], resourceAfter.getCategories().get(0).getSubcategories().get(0).getName());

		resourceOperation.deleteResource(rootResource.getUniqueId());
	}

	@Test
	public void testResourceWithCapabilities() {
		String rootName = "Root123";

		String userId = "jh0003";
		String category = CATEGORY_NAME;
		String updatedCategory = CATEGORY_NAME_UPDATED;
		deleteAndCreateUser(userId, "first_" + userId, "last_" + userId);
		deleteAndCreateCategory(category);
		deleteAndCreateCategory(updatedCategory);

		Resource rootResource = createResource(userId, category, rootName, "1.1", null, false, true);

		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
		capabilityTypeDefinition.setType("tosca.capabilities.Container");
		PropertyDefinition delaultProperty1 = new PropertyDefinition();
		delaultProperty1.setName("def");
		delaultProperty1.setType("string");
		delaultProperty1.setDefaultValue("def");

		PropertyDefinition delaultProperty2 = new PropertyDefinition();
		delaultProperty2.setName("host");
		delaultProperty2.setType("string");
		delaultProperty2.setDefaultValue("true");

		HashMap<String, PropertyDefinition> props = new HashMap<String, PropertyDefinition>();
		props.put(delaultProperty1.getName(), delaultProperty1);
		props.put(delaultProperty2.getName(), delaultProperty2);
		capabilityTypeDefinition.setProperties(props);

		Either<CapabilityTypeDefinition, StorageOperationStatus> addTypeRes = capabilityTypeOperation
				.addCapabilityType(capabilityTypeDefinition);
		assertTrue(addTypeRes.isLeft());

		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		capabilityDefinition.setDescription("firstCap");
		capabilityDefinition.setName("firstCap");
		capabilityDefinition.setType("tosca.capabilities.Container");

		List<ComponentInstanceProperty> properties = new ArrayList<ComponentInstanceProperty>();
		ComponentInstanceProperty propertyDefinition1 = new ComponentInstanceProperty();
		propertyDefinition1.setName("version");
		propertyDefinition1.setType("string");
		propertyDefinition1.setDefaultValue("007");
		properties.add(propertyDefinition1);

		ComponentInstanceProperty propertyDefinition2 = new ComponentInstanceProperty();
		propertyDefinition2.setName("host");
		propertyDefinition2.setType("string");
		propertyDefinition2.setDefaultValue("localhost");
		properties.add(propertyDefinition2);

		capabilityDefinition.setProperties(properties);

		Either<CapabilityDefinition, StorageOperationStatus> addCapabilityRes = capabilityOperation
				.addCapability(rootResource.getUniqueId(), capabilityDefinition.getName(), capabilityDefinition);
		assertTrue(addCapabilityRes.isLeft());

		List<PropertyDefinition> newProperties = new ArrayList<PropertyDefinition>();
		propertyDefinition1 = new ComponentInstanceProperty();
		propertyDefinition1.setName("num_cpu");
		propertyDefinition1.setType("string");
		propertyDefinition1.setDefaultValue("4");
		newProperties.add(propertyDefinition1);

		propertyDefinition2 = new ComponentInstanceProperty();
		propertyDefinition2.setName("port");
		propertyDefinition2.setType("string");
		propertyDefinition2.setDefaultValue("4444");
		newProperties.add(propertyDefinition2);

		CapabilityDefinition addedCap = addCapabilityRes.left().value();

		Either<Map<String, PropertyData>, StorageOperationStatus> updatePropertiesRes = capabilityOperation
				.updatePropertiesOfCapability(addedCap.getUniqueId(), addedCap.getType(), newProperties);
		assertTrue(updatePropertiesRes.isLeft());

		PropertyDefinition invalidProperty = new PropertyDefinition();
		invalidProperty.setName("port");
		invalidProperty.setType("rrr");
		invalidProperty.setDefaultValue("666");
		newProperties.add(invalidProperty);

		Either<Map<String, PropertyData>, StorageOperationStatus> updatePropertiesInvalidRes = capabilityOperation
				.updatePropertiesOfCapability(addedCap.getUniqueId(), addedCap.getType(), newProperties);
		assertTrue(updatePropertiesInvalidRes.isRight());

		Either<CapabilityDefinition, StorageOperationStatus> getCapabilityRes = capabilityOperation
				.getCapability(addedCap.getUniqueId());
		assertTrue(getCapabilityRes.isLeft());

		Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> deletePropertiesOfCapabilityRes = capabilityOperation
				.deletePropertiesOfCapability(addedCap.getUniqueId());
		assertTrue(deletePropertiesOfCapabilityRes.isLeft());

		StorageOperationStatus deleteCapabilityRes = capabilityOperation
				.deleteCapabilityFromGraph(addedCap.getUniqueId());
		assertTrue(deleteCapabilityRes.equals(StorageOperationStatus.OK));

		getCapabilityRes = capabilityOperation.getCapability(addedCap.getUniqueId());
		assertTrue(getCapabilityRes.isRight()
				&& getCapabilityRes.right().value().equals(StorageOperationStatus.NOT_FOUND));

		resourceOperation.deleteResource(rootResource.getUniqueId());
	}

	private PropertyDefinition findProperty(List<PropertyDefinition> properties, String propName) {

		if (properties == null) {
			return null;
		}

		for (PropertyDefinition propertyDefinition : properties) {
			String name = propertyDefinition.getName();
			if (name.equals(propName)) {
				return propertyDefinition;
			}
		}

		return null;
	}

	@Test
	public void testOverrideResourceNotExist() {
		String rootName = "Root123";

		String userId = "jh0003";
		String category = CATEGORY_NAME;
		deleteAndCreateUser(userId, "first_" + userId, "last_" + userId);
		deleteAndCreateCategory(category);

		Resource rootResource = buildResourceMetadata(userId, category, rootName, "1.1");
		rootResource.setUniqueId(UniqueIdBuilder.buildResourceUniqueId());

		Either<Resource, StorageOperationStatus> overrideResource = resourceOperation.overrideResource(rootResource,
				rootResource, false);

		assertTrue(overrideResource.isRight());

	}

	@Ignore
	@Test
	public void testCatalogResource() {
		String resName = "myResource";
		String userId = "jh0003";
		String category = CATEGORY_NAME;
		deleteAndCreateCategory(category);
		// resourceOperation.deleteResource(UniqueIdBuilder.buildResourceUniqueId(resName,"0.1"));
		Resource newResource = createResource(userId, category, resName, "0.1", null, false, true);
		String resourceId = newResource.getUniqueId();

		Map<String, Object> propertiesToMatch = new HashMap<>();
		propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());

		Either<Set<Resource>, StorageOperationStatus> catalog = resourceOperation.getCatalogData(propertiesToMatch,
				false);
		assertTrue(catalog.isLeft());
		Set<Resource> catalogSet = catalog.left().value();
		Set<String> idSet = new HashSet<>();
		for (Resource resource : catalogSet) {
			idSet.add(resource.getUniqueId());
		}
		assertTrue(idSet.contains(resourceId));
		resourceOperation.deleteResource(resourceId);
	}

	@Ignore
	@Test
	public void testTesterFollowed() {
		String rootName = "Test1";
		String rootName2 = "Test2";
		String rootName3 = "Test3";
		String userId = "jh0003";
		String testerUserId = "tt0004";
		String category = CATEGORY_NAME;
		deleteAndCreateUser(testerUserId, "tester", "last");
		deleteAndCreateCategory(category);

		String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
		Either<UserData, TitanOperationStatus> findUser = titanDao.getNode(key, userId, UserData.class);
		User adminUser = OperationTestsUtil.convertUserDataToUser(findUser.left().value());
		Either<UserData, TitanOperationStatus> findTesterUser = titanDao.getNode(key, testerUserId, UserData.class);
		User testerUser = OperationTestsUtil.convertUserDataToUser(findTesterUser.left().value());

		// Create 3 new resources
		Resource resultResource = createResource(userId, category, rootName, "1.0", null, false, true);
		log.debug("{}", resultResource);
		String resourceId = resultResource.getUniqueId();
		Resource resultResource2 = createResource(userId, category, rootName2, "1.0", null, false, true);
		log.debug("{}", resultResource2);
		String resourceId2 = resultResource2.getUniqueId();
		Resource resultResource3 = createResource(userId, category, rootName3, "1.0", null, false, true);
		log.debug("{}", resultResource3);
		String resourceId3 = resultResource3.getUniqueId();

		// update 1 resource to READY_FOR_CERTIFICATION
		Either<Resource, StorageOperationStatus> certReqResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Resource, resultResource, adminUser, adminUser, false);
		Resource RFCResource = certReqResponse.left().value();
		assertEquals(RFCResource.getLifecycleState(), LifecycleStateEnum.READY_FOR_CERTIFICATION);

		// update 1 resource to CERTIFICATION_IN_PROGRESS
		Either<Resource, StorageOperationStatus> startCertificationResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Resource, resultResource2, testerUser, adminUser, false);
		Resource IPResource = startCertificationResponse.left().value();
		assertEquals(IPResource.getLifecycleState(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		lifecycleStates.add(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

		Either<List<Resource>, StorageOperationStatus> resources = resourceOperation.getTesterFollowed(testerUserId,
				lifecycleStates, false);

		assertTrue(resources.isLeft());
		List<Resource> result = resources.left().value();

		List<String> idSet = new ArrayList();
		for (Resource resource : result) {
			idSet.add(resource.getUniqueId());
		}
		assertTrue(idSet.contains(resourceId));
		assertTrue(idSet.contains(resourceId2));
		assertFalse(idSet.contains(resourceId3));
		resourceOperation.deleteResource(resourceId);
		resourceOperation.deleteResource(resourceId2);
		resourceOperation.deleteResource(resourceId3);

	}

	@Test
	public void getVersionListNotDeleted() {
		String resName = "myResource";
		String userId = "jh0003";
		String category = CATEGORY_NAME;
		deleteAndCreateCategory(category);

		Resource newResource = createResource(userId, category, resName, "0.1", null, false, true);
		String resourceId1 = newResource.getUniqueId();

		User admin = new User("j", "h", userId, null, "ADMIN", System.currentTimeMillis());
		Either<Resource, StorageOperationStatus> checkoutResource = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, newResource, admin, admin, false);
		assertTrue(checkoutResource.isLeft());
		Resource newResource2 = checkoutResource.left().value();
		String resourceId2 = newResource2.getUniqueId();

		Resource newResource3 = createResource(userId, category, resName, "0.1", null, false, true);
		String resourceId3 = newResource3.getUniqueId();

		Either<Map<String, String>, TitanOperationStatus> versionList = resourceOperation.getVersionList(
				NodeTypeEnum.Resource, "0.2", newResource2.getUUID(), newResource2.getSystemName(),
				ResourceMetadataData.class);
		assertTrue(versionList.isLeft());
		Map<String, String> versionMap = versionList.left().value();

		assertTrue(versionMap.size() == 2);
		assertTrue(versionMap.containsValue(resourceId1));
		assertTrue(versionMap.containsValue(resourceId2));
		assertFalse(versionMap.containsValue(resourceId3));

		Either<Resource, StorageOperationStatus> deleteResource = resourceOperation.deleteResource(resourceId1);
		assertTrue(deleteResource.isLeft());
		deleteResource = resourceOperation.deleteResource(resourceId2);
		assertTrue(deleteResource.isLeft());
		deleteResource = resourceOperation.deleteResource(resourceId3);
		assertTrue(deleteResource.isLeft());

	}

	@Test
	public void getVersionListWithDeleted() {
		String resName = "myResource";
		String userId = "jh0003";
		String category = CATEGORY_NAME;
		deleteAndCreateCategory(category);

		Resource newResource = createResource(userId, category, resName, "0.1", null, false, true);
		String resourceId1 = newResource.getUniqueId();

		User admin = new User("j", "h", userId, null, "ADMIN", System.currentTimeMillis());
		Either<Resource, StorageOperationStatus> checkoutResource = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, newResource, admin, admin, false);
		assertTrue(checkoutResource.isLeft());
		Resource newResource2 = checkoutResource.left().value();
		String resourceId2 = newResource2.getUniqueId();

		Either<Resource, StorageOperationStatus> resource = resourceOperation.getResource(resourceId1, false);
		assertTrue(resource.isLeft());
		Either<Component, StorageOperationStatus> markResourceToDelete = resourceOperation
				.markComponentToDelete(resource.left().value(), false);
		assertTrue(markResourceToDelete.isLeft());

		Either<Map<String, String>, TitanOperationStatus> versionList = resourceOperation.getVersionList(
				NodeTypeEnum.Resource, "0.2", newResource2.getUUID(), newResource2.getSystemName(),
				ResourceMetadataData.class);

		assertTrue(versionList.isLeft());
		Map<String, String> versionMap = versionList.left().value();

		assertTrue(versionMap.size() == 1);
		assertFalse(versionMap.containsValue(resourceId1));
		assertTrue(versionMap.containsValue(resourceId2));

		Either<Resource, StorageOperationStatus> deleteResource = resourceOperation.deleteResource(resourceId1);
		assertTrue(deleteResource.isLeft());
		deleteResource = resourceOperation.deleteResource(resourceId2);
		assertTrue(deleteResource.isLeft());
	}
	
	@Test
	public void testDerviedPropertiesInResource() {

		try {
			String userId = "jh0003";
			String category = CATEGORY_NAME;

			deleteAndCreateUser(userId, "first_" + userId, "last_" + userId);
			deleteAndCreateCategory(category);

			Resource createResource1 = createResource(userId, category, "myResource1", "0.1", null, true, false);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(createResource1.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			String json = gson.toJson(createResource1);
			log.debug(json);

			Resource createResource2 = createResource(userId, category, "myResource2", "0.1", createResource1.getName(),
					true, false);

			json = gson.toJson(createResource2);
			log.debug(json);

			List<PropertyDefinition> propList1 = new ArrayList<>();
			TitanOperationStatus findAllResourcePropertiesRecursively1 = propertyOperation
					.findAllResourcePropertiesRecursively(createResource1.getUniqueId(), propList1);
			assertEquals("check search properties succeed", findAllResourcePropertiesRecursively1,
					TitanOperationStatus.OK);

			List<PropertyDefinition> propList2 = new ArrayList<>();
			TitanOperationStatus findAllResourcePropertiesRecursively2 = propertyOperation
					.findAllResourcePropertiesRecursively(createResource2.getUniqueId(), propList2);
			assertEquals("check search properties succeed", findAllResourcePropertiesRecursively2,
					TitanOperationStatus.OK);

			assertEquals("check number of properties", propList1.size() * 2, propList2.size());

			resourceOperation.deleteResource(createResource1.getUniqueId());
			resourceOperation.deleteResource(createResource2.getUniqueId());

		} finally {

		}
		
	}

}
