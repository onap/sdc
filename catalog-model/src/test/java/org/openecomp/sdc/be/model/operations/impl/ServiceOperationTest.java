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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thinkaurelius.titan.core.TitanGraph;
//import com.tinkerpop.blueprints.Vertex;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ServiceOperationTest extends ModelTestBase {

	@javax.annotation.Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@javax.annotation.Resource(name = "service-operation")
	private ServiceOperation serviceOperation;

	@javax.annotation.Resource
	private IGraphLockOperation graphLockOperation;

	@javax.annotation.Resource
	private ArtifactOperation artifactOperation;

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

	@javax.annotation.Resource(name = "component-instance-operation")
	private ComponentInstanceOperation resourceInstanceOperation;

	@javax.annotation.Resource(name = "lifecycle-operation")
	private LifecycleOperation lifecycleOperation;

	private static Logger log = LoggerFactory.getLogger(ServiceOperation.class.getName());
	private static String USER_ID = "muserId";
	private static String CATEGORY_NAME = "category/mycategory";

	@BeforeClass
	public static void setupBeforeClass() {
		// ExternalConfiguration.setAppName("catalog-model");
		// String appConfigDir = "src/test/resources/config/catalog-model";
		// ConfigurationSource configurationSource = new
		// FSConfigurationSource(ExternalConfiguration.getChangeListener(),
		// appConfigDir);

		// configurationManager = new ConfigurationManager(new
		// ConfigurationSource() {
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

		// String appConfigDir = "src/test/resources/config";
		// ConfigurationSource configurationSource = new
		// FSConfigurationSource(ExternalConfiguration.getChangeListener(),
		// appConfigDir);
		// configurationManager = new ConfigurationManager(configurationSource);
		//
		// Configuration configuration = new Configuration();
		// configuration.setTitanInMemoryGraph(true);
		//// configuration.setTitanInMemoryGraph(false);
		//// configuration.setTitanCfgFile("C:\\Dev\\d2\\D2-SDnC\\catalog-be\\src\\main\\resources\\config\\titan.properties");
		//
		// configurationManager.setConfiguration(configuration);

		ModelTestBase.init();
	}

	@Before
	public void createUserAndCategory() {
		deleteAndCreateCategory(CATEGORY_NAME);
		deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID, null);
	}

	@Test
	public void dummyTest() {

	}

	@Test
	public void testCreateService() {
		String category = CATEGORY_NAME;
		String serviceName = "servceTest";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug(" *** create **");
		log.debug("{}", serviceAfterSave);
		String uniqueId = serviceAfterSave.getUniqueId();

		Either<Service, StorageOperationStatus> serviceGet = serviceOperation.getService(uniqueId);
		assertTrue(serviceGet.isLeft());
		log.debug(" *** get **");
		log.debug("{}", serviceGet.left().value());

		Either<Service, StorageOperationStatus> serviceDelete = serviceOperation.deleteService(uniqueId);

		assertTrue(serviceDelete.isLeft());
		log.debug(" *** delete **");
		log.debug("{}", serviceDelete.left().value());

		Either<List<ArtifactData>, TitanOperationStatus> artifacts = titanDao.getByCriteria(NodeTypeEnum.ArtifactRef,
				null, ArtifactData.class);
		assertTrue(artifacts.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, artifacts.right().value());

		serviceOperation.deleteService(serviceAfterSave.getUniqueId());
	}

	@Test
	public void testUtilsService() {
		String category = CATEGORY_NAME;
		String serviceName = "servceTest2";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug(" *** create **");
		log.debug("{}", serviceAfterSave);
		String uniqueId = serviceAfterSave.getUniqueId();

		boolean canWorkOnComponent = ComponentValidationUtils.canWorkOnComponent(uniqueId, serviceOperation, userId);
		assertTrue(canWorkOnComponent);

		canWorkOnComponent = ComponentValidationUtils.canWorkOnComponent(serviceAfterSave, userId);
		assertTrue(canWorkOnComponent);

		StorageOperationStatus lockComponent = graphLockOperation.lockComponent(uniqueId, NodeTypeEnum.Service);
		assertEquals(StorageOperationStatus.OK, lockComponent);

		lockComponent = graphLockOperation.unlockComponent(uniqueId, NodeTypeEnum.Service);
		assertEquals(StorageOperationStatus.OK, lockComponent);

		Either<Service, StorageOperationStatus> serviceDelete = serviceOperation.deleteService(uniqueId);
	}

	@Test
	public void testInstanceCounter() {
		String category = CATEGORY_NAME;
		String serviceName = "servceTest2";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug(" *** create **");
		log.debug("{}", serviceAfterSave);

		Either<Integer, StorageOperationStatus> counter = serviceOperation
				.increaseAndGetComponentInstanceCounter(serviceAfterSave.getUniqueId(), NodeTypeEnum.Service, false);
		assertTrue(counter.isLeft());
		assertEquals(new Integer(1), (Integer) counter.left().value());

		counter = serviceOperation.increaseAndGetComponentInstanceCounter(serviceAfterSave.getUniqueId(),
				NodeTypeEnum.Service, false);
		assertTrue(counter.isLeft());
		assertEquals(new Integer(2), (Integer) counter.left().value());
		Either<Service, StorageOperationStatus> serviceDelete = serviceOperation
				.deleteService(serviceAfterSave.getUniqueId());
	}

	@Test
	public void testAddArtifactToService() {
		String category = CATEGORY_NAME;
		String serviceName = "servceTest2";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug("{}", serviceAfterSave);
		String serviceId = serviceAfterSave.getUniqueId();

		ArtifactDefinition artifactInfo = addArtifactToService(userId, serviceId, "install_apache");

		Either<Service, StorageOperationStatus> service = serviceOperation.getService(serviceId);
		assertTrue(service.isLeft());

		Map<String, ArtifactDefinition> artifacts = service.left().value().getArtifacts();
		assertEquals(1, artifacts.size());

		for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
			String artifactId = entry.getValue().getUniqueId();
			String description = entry.getValue().getDescription();
			assertEquals("hdkfhskdfgh", description);

			artifactInfo.setDescription("jghlsk new desfnjdh");

			artifactOperation.updateArifactOnResource(artifactInfo, serviceId, artifactId, NodeTypeEnum.Service, false);
		}

		service = serviceOperation.getService(serviceId);
		assertTrue(service.isLeft());

		artifacts = service.left().value().getArtifacts();
		for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
			String artifactId = entry.getValue().getUniqueId();
			String description = entry.getValue().getDescription();
			assertEquals("jghlsk new desfnjdh", description);

			artifactOperation.removeArifactFromResource(serviceId, artifactId, NodeTypeEnum.Service, true, false);
		}
		service = serviceOperation.getService(serviceId);
		assertTrue(service.isLeft());

		artifacts = service.left().value().getArtifacts();
		assertEquals(0, artifacts.size());

		Either<Service, StorageOperationStatus> serviceDelete = serviceOperation.deleteService(serviceId);

		Either<List<ArtifactData>, TitanOperationStatus> byCriteria = titanDao.getByCriteria(NodeTypeEnum.ArtifactRef,
				null, ArtifactData.class);
		assertTrue(byCriteria.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, byCriteria.right().value());

		serviceOperation.deleteService(serviceAfterSave.getUniqueId());

	}

	private ArtifactDefinition addArtifactToService(String userId, String serviceId, String artifactName) {
		ArtifactDefinition artifactInfo = new ArtifactDefinition();

		artifactInfo.setArtifactName(artifactName + ".sh");
		artifactInfo.setArtifactType("SHELL");
		artifactInfo.setDescription("hdkfhskdfgh");
		artifactInfo.setPayloadData("UEsDBAoAAAAIAAeLb0bDQz");

		artifactInfo.setUserIdCreator(userId);
		String fullName = "Jim H";
		artifactInfo.setUpdaterFullName(fullName);
		long time = System.currentTimeMillis();
		artifactInfo.setCreatorFullName(fullName);
		artifactInfo.setCreationDate(time);
		artifactInfo.setLastUpdateDate(time);

		artifactInfo.setUserIdLastUpdater(userId);
		artifactInfo.setArtifactLabel(artifactName);
		artifactInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(serviceId, artifactInfo.getArtifactLabel()));

		Either<ArtifactDefinition, StorageOperationStatus> artifact = artifactOperation
				.addArifactToComponent(artifactInfo, serviceId, NodeTypeEnum.Service, true, true);
		assertTrue(artifact.isLeft());
		return artifactInfo;
	}

	@Test
	public void testFollowed() {
		String category = CATEGORY_NAME;
		String serviceName = "servceTest2";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug("{}", serviceAfterSave);
		String serviceId = serviceAfterSave.getUniqueId();

		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		Set<LifecycleStateEnum> lastStateStates = new HashSet<LifecycleStateEnum>();
		lastStateStates.add(LifecycleStateEnum.CERTIFIED);

		Either<List<Service>, StorageOperationStatus> followed = serviceOperation.getFollowed(userId, lifecycleStates,
				lastStateStates, false);
		assertTrue(followed.isLeft());
		List<Service> list = followed.left().value();
		assertEquals(1, list.size());
		serviceOperation.deleteService(serviceId);
	}

	@Test
	public void testUpdateService() {
		String category = CATEGORY_NAME;
		String serviceName = "12";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug("{}", serviceAfterSave);
		String serviceId = serviceAfterSave.getUniqueId();
		serviceAfterSave.setDescription("new description");
		Either<Service, StorageOperationStatus> updateService = serviceOperation.updateService(serviceAfterSave, false);
		assertTrue(updateService.isLeft());

		titanDao.commit();

		Either<Service, StorageOperationStatus> serviceAfterUpdate = serviceOperation.getService(serviceId, false);
		assertTrue(serviceAfterUpdate.isLeft());

		serviceOperation.deleteService(serviceId);
		assertEquals("new description", serviceAfterUpdate.left().value().getDescription());

	}

	public Service createService(String userId, String category, String serviceName, String serviceVersion,
			boolean isHighestVersion) {

		Service service = buildServiceMetadata(userId, category, serviceName, serviceVersion);

		service.setHighestVersion(isHighestVersion);

		Either<Service, StorageOperationStatus> result = serviceOperation.createService(service, true);

		log.info(result.toString());
		assertTrue(result.isLeft());
		Service resultService = result.left().value();

		// assertEquals("check resource unique id",
		// UniqueIdBuilder.buildServiceUniqueId(serviceName, serviceVersion),
		// resultService.getUniqueId());
		assertEquals("check resource state", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				resultService.getLifecycleState());

		return resultService;
	}

	// @Test
	public void testCloneFullService() {
		// try{
		String userId = USER_ID;
		// Either<Service, StorageOperationStatus> deleteService =
		// serviceOperation.deleteService(UniqueIdBuilder.buildServiceUniqueId("my-service",
		// "1.0"), false);
		// log.info("testCloneFullService - after delete service. result
		// is="+deleteService);
		Service origService = createService(userId, CATEGORY_NAME, "my-service", "1.0", true);

		// add artifacts
		addArtifactToService(userId, origService.getUniqueId(), "install_apache");
		addArtifactToService(userId, origService.getUniqueId(), "start_apache");

		// add resource instances
		ResourceInstanceOperationTest riTest = new ResourceInstanceOperationTest();
		riTest.setOperations(titanDao, capabilityTypeOperation, requirementOperation, capabilityOperation,
				resourceOperation, propertyOperation, resourceInstanceOperation);
		riTest.addResourceInstancesAndRelation(origService.getUniqueId());

		Either<Service, StorageOperationStatus> service2 = serviceOperation.getService(origService.getUniqueId(),
				false);
		assertTrue(service2.isLeft());
		origService = service2.left().value();

		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String json = prettyGson.toJson(origService);
		log.debug(json);

		Service fullService = origService;

		Either<Service, StorageOperationStatus> createService = serviceOperation.cloneService(fullService, "2.0",
				false);
		assertTrue(createService.isLeft());
		Either<Service, StorageOperationStatus> serviceAfterCreate = serviceOperation
				.getServiceByNameAndVersion("my-service", "2.0", null, false);
		assertTrue(serviceAfterCreate.isLeft());
		fullService = serviceAfterCreate.left().value();

		Either<Service, StorageOperationStatus> getOrigService = serviceOperation
				.getServiceByNameAndVersion("my-service", "1.0", null, false);
		assertTrue(getOrigService.isLeft());
		origService = getOrigService.left().value();

		// assertEquals(origService.getComponentMetadataDefinition(),
		// fullService.getComponentMetadataDefinition());
		assertEquals(origService.getArtifacts().size(), fullService.getArtifacts().size());
		assertEquals(origService.getComponentInstances().size(), fullService.getComponentInstances().size());
		assertEquals(origService.getComponentInstancesRelations().size(),
				fullService.getComponentInstancesRelations().size());

		origService.setUniqueId(fullService.getUniqueId());
		origService.setVersion(fullService.getVersion());

		assertEquals(origService.getComponentMetadataDefinition(), fullService.getComponentMetadataDefinition());
		assertEquals(origService.getCategories(), fullService.getCategories());

		serviceOperation.deleteService(origService.getUniqueId());
		serviceOperation.deleteService(serviceAfterCreate.left().value().getUniqueId());

		// } finally {
		// titanDao.rollback();
		// Either<Service, StorageOperationStatus> serviceAfterCreate =
		// serviceOperation.getService(UniqueIdBuilder.buildServiceUniqueId("my-service",
		// "2.0"), true);
		// assertTrue(serviceAfterCreate.isRight());
		//
		// Either<Service, StorageOperationStatus> getOrigService =
		// serviceOperation.getService(UniqueIdBuilder.buildServiceUniqueId("my-service",
		// "1.0"), true);
		// assertTrue(getOrigService.isRight());
		// titanDao.rollback();
		// }
	}

	// @Test
	public void testCloneServiceWithoutResourceInstances() {
		// try{
		String userId = USER_ID;
		// Either<Service, StorageOperationStatus> deleteService =
		// serviceOperation.deleteService(UniqueIdBuilder.buildServiceUniqueId("my-service",
		// "1.0"), false);
		// log.info("testCloneServiceWithoutResourceInstances - after delete
		// service. result is="+deleteService);
		Service origService = createService(userId, CATEGORY_NAME, "my-service", "1.0", true);

		// add artifacts
		addArtifactToService(userId, origService.getUniqueId(), "install_apache");
		addArtifactToService(userId, origService.getUniqueId(), "start_apache");

		Either<Service, StorageOperationStatus> service2 = serviceOperation.getService(origService.getUniqueId(),
				false);
		assertTrue(service2.isLeft());
		origService = service2.left().value();

		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String json = prettyGson.toJson(origService);
		log.debug(json);

		Service fullService = origService;

		Either<Service, StorageOperationStatus> createService = serviceOperation.cloneService(fullService, "2.0",
				false);
		assertTrue(createService.isLeft());
		Either<Service, StorageOperationStatus> serviceAfterCreate = serviceOperation
				.getServiceByNameAndVersion("my-service", "2.0", null, false);
		assertTrue(serviceAfterCreate.isLeft());
		fullService = serviceAfterCreate.left().value();

		Either<Service, StorageOperationStatus> getOrigService = serviceOperation
				.getServiceByNameAndVersion("my-service", "1.0", null, false);
		assertTrue(getOrigService.isLeft());
		origService = getOrigService.left().value();

		// assertEquals(origService.getComponentMetadataDefinition(),
		// fullService.getComponentMetadataDefinition());
		assertEquals(origService.getArtifacts().size(), fullService.getArtifacts().size());
		assertEquals(origService.getComponentInstances(), fullService.getComponentInstances());
		assertEquals(origService.getComponentInstancesRelations(), fullService.getComponentInstancesRelations());

		origService.setUniqueId(fullService.getUniqueId());
		origService.setVersion(fullService.getVersion());

		assertEquals(origService.getComponentMetadataDefinition(), fullService.getComponentMetadataDefinition());
		assertEquals(origService.getCategories(), fullService.getCategories());

		serviceOperation.deleteService(getOrigService.left().value().getUniqueId());
		serviceOperation.deleteService(serviceAfterCreate.left().value().getUniqueId());

		// } finally {
		// titanDao.rollback();
		// }
	}

	// @Test
	public void testCloneServiceWithoutArtifacts() {
		// try{

		String userId = USER_ID;

		Service origService = createService(userId, CATEGORY_NAME, "my-service", "1.0", true);

		// add resource instances
		ResourceInstanceOperationTest riTest = new ResourceInstanceOperationTest();
		riTest.setOperations(titanDao, capabilityTypeOperation, requirementOperation, capabilityOperation,
				resourceOperation, propertyOperation, resourceInstanceOperation);
		riTest.addResourceInstancesAndRelation(origService.getUniqueId());

		Either<Service, StorageOperationStatus> service2 = serviceOperation.getService(origService.getUniqueId(),
				false);
		assertTrue(service2.isLeft());
		origService = service2.left().value();

		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String json = prettyGson.toJson(origService);
		log.debug(json);

		Service fullService = origService;

		Either<Service, StorageOperationStatus> createService = serviceOperation.cloneService(fullService, "2.0",
				false);
		assertTrue(createService.isLeft());
		Either<Service, StorageOperationStatus> serviceAfterCreate = serviceOperation
				.getServiceByNameAndVersion("my-service", "2.0", null, false);
		assertTrue(serviceAfterCreate.isLeft());
		fullService = serviceAfterCreate.left().value();

		Either<Service, StorageOperationStatus> getOrigService = serviceOperation
				.getServiceByNameAndVersion("my-service", "1.0", null, false);
		assertTrue(getOrigService.isLeft());
		origService = getOrigService.left().value();

		assertEquals(origService.getArtifacts(), fullService.getArtifacts());
		assertEquals(origService.getComponentInstances().size(), fullService.getComponentInstances().size());
		assertEquals(origService.getComponentInstancesRelations().size(),
				fullService.getComponentInstancesRelations().size());

		origService.setUniqueId(fullService.getUniqueId());
		origService.setVersion(fullService.getVersion());

		assertEquals(origService.getComponentMetadataDefinition(), fullService.getComponentMetadataDefinition());
		assertEquals(origService.getCategories(), fullService.getCategories());

		serviceOperation.deleteService(serviceAfterCreate.left().value().getUniqueId());
		serviceOperation.deleteService(getOrigService.left().value().getUniqueId());

		// } finally {
		// titanDao.rollback();
		// }
	}

	// @Test
	public void testCloneServiceSimple() {
		// try{
		String userId = USER_ID;
		String serviceName = "serviceToClone";
		//
		// Either<Service, StorageOperationStatus> deleteService =
		// serviceOperation.deleteService(UniqueIdBuilder.buildServiceUniqueId(serviceName,
		// "1.0"));
		// log.info("testCloneServiceSimple - after delete service. result
		// is="+deleteService);

		Service origService = createService(userId, CATEGORY_NAME, serviceName, "1.0", true);

		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String json = prettyGson.toJson(origService);
		log.debug(json);

		Service fullService = origService;

		Either<Service, StorageOperationStatus> createService = serviceOperation.cloneService(fullService, "2.0",
				false);
		assertTrue(createService.isLeft());
		Either<Service, StorageOperationStatus> serviceAfterCreate = serviceOperation
				.getServiceByNameAndVersion(serviceName, "2.0", null, false);
		assertTrue(serviceAfterCreate.isLeft());
		fullService = serviceAfterCreate.left().value();

		Either<Service, StorageOperationStatus> getOrigService = serviceOperation
				.getServiceByNameAndVersion(serviceName, "1.0", null, false);
		assertTrue(getOrigService.isLeft());
		origService = getOrigService.left().value();

		// assertEquals(origService.getComponentMetadataDefinition(),
		// fullService.getComponentMetadataDefinition());
		assertEquals(origService.getArtifacts(), fullService.getArtifacts());
		assertEquals(origService.getComponentInstances(), fullService.getComponentInstances());
		assertEquals(origService.getComponentInstancesRelations(), fullService.getComponentInstancesRelations());
		origService.setUniqueId(fullService.getUniqueId());
		origService.setVersion(fullService.getVersion());

		assertEquals(origService.getComponentMetadataDefinition(), fullService.getComponentMetadataDefinition());
		assertEquals(origService.getCategories(), fullService.getCategories());

		serviceOperation.deleteService(getOrigService.left().value().getUniqueId());
		serviceOperation.deleteService(serviceAfterCreate.left().value().getUniqueId());

		// } finally {
		// titanDao.rollback();
		// }
	}

	private Service buildServiceMetadata(String userId, String category, String serviceName, String serviceVersion) {

		Service service = new Service();
		service.setName(serviceName);
		service.setVersion(serviceVersion);
		service.setDescription("description 1");

		service.setCreatorUserId(userId);
		service.setContactId("contactId@sdc.com");
		CategoryDefinition categoryDef = new CategoryDefinition();
		categoryDef.setName(category);

		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(categoryDef);
		service.setCategories(categories);

		service.setIcon("images/my.png");
		List<String> tags = new ArrayList<String>();
		tags.add("TAG1");
		tags.add("TAG2");
		service.setTags(tags);
		return service;
	}

	private void deleteAndCreateCategory(String category) {
		String[] names = category.split("/");
		OperationTestsUtil.deleteAndCreateServiceCategory(category, titanDao);
		OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanDao);

		/*
		 * CategoryData categoryData = new CategoryData();
		 * categoryData.setName(category);
		 * 
		 * titanDao.deleteNode(categoryData, CategoryData.class);
		 * Either<CategoryData, TitanOperationStatus> createNode =
		 * titanDao.createNode(categoryData, CategoryData.class);
		 * System.out.println("after creating caetgory " + createNode);
		 */

	}

	private UserData deleteAndCreateUser(String userId, String firstName, String lastName, String role) {
		UserData userData = new UserData();
		userData.setUserId(userId);
		userData.setFirstName(firstName);
		userData.setLastName(lastName);
		if (role != null && !role.isEmpty()) {
			userData.setRole(role);
		} else {
			userData.setRole("ADMIN");
		}

		titanDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
		titanDao.createNode(userData, UserData.class);
		titanDao.commit();

		return userData;
	}

	@Test
	public void testCatalogService() {
		String userId = USER_ID;
		String category = CATEGORY_NAME;
		String serviceName = "MyService";
		String serviceVersion = "0.1";
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug("{}", serviceAfterSave);
		String serviceId = serviceAfterSave.getUniqueId();

		Map<String, Object> propertiesToMatch = new HashMap<>();
		propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());

		Either<Set<Service>, StorageOperationStatus> catalog = serviceOperation.getCatalogData(propertiesToMatch,
				false);
		assertTrue(catalog.isLeft());
		Set<Service> catalogSet = catalog.left().value();
		Set<String> idSet = new HashSet<>();
		for (Service service : catalogSet) {
			idSet.add(service.getUniqueId());
		}
		assertTrue(idSet.contains(serviceId));
		serviceOperation.deleteService(serviceId);
	}

	@After
	public void teardown() {
		clearGraph();
	}

	private void clearGraph() {
		Either<TitanGraph, TitanOperationStatus> graphResult = titanDao.getGraph();
		TitanGraph graph = graphResult.left().value();

		Iterable<TitanVertex> vertices = graph.query().vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				// graph.removeVertex(vertex);
				vertex.remove();
			}

		}
		titanDao.commit();
	}

	@Test
	public void testTesterFollowed() {
		String serviceName = "Test1";
		String serviceName2 = "Test2";
		String serviceName3 = "Test3";
		String userId = USER_ID;
		String testerUserId = "tt0004";
		String category = CATEGORY_NAME;
		deleteAndCreateUser(testerUserId, "tester", "last", "TESTER");
		// deleteAndCreateCategory(category);

		String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
		Either<UserData, TitanOperationStatus> findUser = titanDao.getNode(key, userId, UserData.class);
		User adminUser = OperationTestsUtil.convertUserDataToUser(findUser.left().value());
		Either<UserData, TitanOperationStatus> findTesterUser = titanDao.getNode(key, testerUserId, UserData.class);
		User testerUser = OperationTestsUtil.convertUserDataToUser(findTesterUser.left().value());

		// Create 3 new services
		Service resultService = createService(userId, category, serviceName, "0.1", false);
		log.debug("{}", resultService);
		String serviceId = resultService.getUniqueId();
		Service resultService2 = createService(userId, category, serviceName2, "0.1", false);
		log.debug("{}", resultService2);
		String serviceId2 = resultService2.getUniqueId();
		Service resultService3 = createService(userId, category, serviceName3, "0.1", false);
		log.debug("{}", resultService3);
		String serviceId3 = resultService3.getUniqueId();

		// update 1 service to READY_FOR_CERTIFICATION
		Either<? extends Component, StorageOperationStatus> certReqResponse = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService, adminUser, adminUser, false);
		Service RFCService = (Service) certReqResponse.left().value();
		assertEquals(RFCService.getLifecycleState(), LifecycleStateEnum.READY_FOR_CERTIFICATION);

		// update 1 service to CERTIFICATION_IN_PROGRESS
		Either<? extends Component, StorageOperationStatus> startCertificationResponse = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService2, testerUser, adminUser, false);
		Service IPService = (Service) startCertificationResponse.left().value();
		assertEquals(IPService.getLifecycleState(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		lifecycleStates.add(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

		Either<List<Service>, StorageOperationStatus> services = serviceOperation.getTesterFollowed(testerUserId,
				lifecycleStates, false);

		assertTrue(services.isLeft());
		List<Service> result = services.left().value();

		List<String> ids = new ArrayList<>();
		for (Service service : result) {
			ids.add(service.getUniqueId());
		}
		assertTrue(ids.contains(serviceId));
		assertTrue(ids.contains(serviceId2));
		assertFalse(ids.contains(serviceId3));
		serviceOperation.deleteService(serviceId);
		serviceOperation.deleteService(serviceId2);
		serviceOperation.deleteService(serviceId3);

	}

	@Test
	public void testOpsFollowed() {
		String serviceName = "Test1";
		String serviceName2 = "Test2";
		String serviceName3 = "Test3";
		String serviceName4 = "Test4";
		String userId = USER_ID;
		String category = CATEGORY_NAME;
		String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
		Either<UserData, TitanOperationStatus> findUser = titanDao.getNode(key, userId, UserData.class);
		User adminUser = OperationTestsUtil.convertUserDataToUser(findUser.left().value());

		// Create 4 new services
		Service resultService = createService(userId, category, serviceName, "0.1", false);
		log.debug("{}", resultService);
		String serviceId = resultService.getUniqueId();
		Service resultService2 = createService(userId, category, serviceName2, "0.1", false);
		log.debug("{}", resultService2);
		String serviceId2 = resultService2.getUniqueId();
		Service resultService3 = createService(userId, category, serviceName3, "0.1", false);
		log.debug("{}", resultService3);
		String serviceId3 = resultService3.getUniqueId();
		Service resultService4 = createService(userId, category, serviceName4, "0.1", false);
		log.debug("{}", resultService3);
		String serviceId4 = resultService4.getUniqueId();

		// update 1 service to CERTIFIED dist status DISTRIBUTED
		Either<? extends Component, StorageOperationStatus> reqCertificationResult = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService, adminUser, adminUser, false);
		Either<? extends Component, StorageOperationStatus> startCertificationResult = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService, adminUser, adminUser, false);
		Service actualService = (Service) startCertificationResult.left().value();

		Either<? extends Component, StorageOperationStatus> certResponse = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Service, resultService, adminUser, adminUser, false);
		Service certifiedService = (Service) certResponse.left().value();
		serviceOperation.updateDestributionStatus(resultService, adminUser, DistributionStatusEnum.DISTRIBUTED);

		// update 1 service to CERTIFIED dist status DISTRIBUTION_APPROVED
		Either<? extends Component, StorageOperationStatus> reqCertificationResult2 = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService2, adminUser, adminUser, false);
		Either<? extends Component, StorageOperationStatus> startCertificationResult2 = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService2, adminUser, adminUser, false);
		Service actualService2 = (Service) startCertificationResult2.left().value();

		Either<? extends Component, StorageOperationStatus> certResponse2 = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Service, resultService2, adminUser, adminUser, false);
		Service certifiedService2 = (Service) certResponse2.left().value();
		serviceOperation.updateDestributionStatus(resultService2, adminUser,
				DistributionStatusEnum.DISTRIBUTION_APPROVED);

		// update 1 service to CERTIFIED dist status DISTRIBUTION_REJECTED
		Either<? extends Component, StorageOperationStatus> reqCertificationResult3 = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService3, adminUser, adminUser, false);
		Either<? extends Component, StorageOperationStatus> startCertificationResult3 = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService3, adminUser, adminUser, false);
		Service actualService3 = (Service) startCertificationResult3.left().value();

		Either<? extends Component, StorageOperationStatus> certResponse3 = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Service, actualService3, adminUser, adminUser, false);
		Service certifiedService3 = (Service) certResponse3.left().value();
		serviceOperation.updateDestributionStatus(certifiedService3, adminUser,
				DistributionStatusEnum.DISTRIBUTION_REJECTED);

		Map<String, Object> propertiesToMatch = new HashMap<>();
		propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());

		Set<DistributionStatusEnum> distStatus = new HashSet<DistributionStatusEnum>();
		distStatus.add(DistributionStatusEnum.DISTRIBUTION_APPROVED);
		distStatus.add(DistributionStatusEnum.DISTRIBUTED);

		Either<Set<Service>, StorageOperationStatus> services = serviceOperation
				.getCertifiedServicesWithDistStatus(propertiesToMatch, distStatus, false);

		assertTrue(services.isLeft());
		Set<Service> result = services.left().value();

		List<String> ids = new ArrayList<>();
		for (Service service : result) {
			ids.add(service.getUniqueId());
		}
		assertTrue(ids.contains(certifiedService.getUniqueId()));
		assertTrue(ids.contains(certifiedService2.getUniqueId()));
		assertFalse(ids.contains(certifiedService3.getUniqueId()));
		assertFalse(ids.contains(resultService4.getUniqueId()));
		serviceOperation.deleteService(serviceId);
		serviceOperation.deleteService(serviceId2);
		serviceOperation.deleteService(serviceId3);
		serviceOperation.deleteService(serviceId4);
	}

	@Test
	public void testGovernorFollowed() {
		String serviceName = "Test1";
		String serviceName2 = "Test2";
		String serviceName3 = "Test3";
		String userId = USER_ID;
		String category = CATEGORY_NAME;
		String key = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
		Either<UserData, TitanOperationStatus> findUser = titanDao.getNode(key, userId, UserData.class);
		User adminUser = OperationTestsUtil.convertUserDataToUser(findUser.left().value());

		// Create 3 new services
		Service resultService = createService(userId, category, serviceName, "0.1", false);
		log.debug("{}", resultService);
		String serviceId = resultService.getUniqueId();
		Service resultService2 = createService(userId, category, serviceName2, "0.1", false);
		log.debug("{}", resultService2);
		String serviceId2 = resultService2.getUniqueId();
		Service resultService3 = createService(userId, category, serviceName3, "0.1", false);
		log.debug("{}", resultService3);
		String serviceId3 = resultService3.getUniqueId();

		// update 1 service to CERTIFIED + DISTRIBUTED
		Either<? extends Component, StorageOperationStatus> reqCertificationResult = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService, adminUser, adminUser, false);
		Either<? extends Component, StorageOperationStatus> startCertificationResult = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService, adminUser, adminUser, false);
		Service actualService = (Service) startCertificationResult.left().value();

		Either<? extends Component, StorageOperationStatus> certResponse = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Service, actualService, adminUser, adminUser, false);
		Service certifiedService = (Service) certResponse.left().value();
		serviceOperation.updateDestributionStatus(certifiedService, adminUser, DistributionStatusEnum.DISTRIBUTED);

		// update 1 service to CERTIFIED dist status + DISTRIBUTION_REJECTED
		Either<? extends Component, StorageOperationStatus> reqCertificationResult2 = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService2, adminUser, adminUser, false);
		Either<? extends Component, StorageOperationStatus> startCertificationResult2 = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService2, adminUser, adminUser, false);
		Service actualService2 = (Service) startCertificationResult2.left().value();

		Either<? extends Component, StorageOperationStatus> certResponse2 = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Service, actualService2, adminUser, adminUser, false);
		Service certifiedService2 = (Service) certResponse2.left().value();
		serviceOperation.updateDestributionStatus(certifiedService2, adminUser, DistributionStatusEnum.DISTRIBUTED);

		Map<String, Object> propertiesToMatch = new HashMap<>();
		propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());

		Either<Set<Service>, StorageOperationStatus> services = serviceOperation
				.getCertifiedServicesWithDistStatus(propertiesToMatch, null, false);

		assertTrue(services.isLeft());
		Set<Service> result = services.left().value();

		List<String> ids = new ArrayList<>();
		for (Service service : result) {
			ids.add(service.getUniqueId());
		}
		assertTrue(ids.contains(certifiedService.getUniqueId()));
		assertTrue(ids.contains(certifiedService2.getUniqueId()));
		assertFalse(ids.contains(serviceId3));
		serviceOperation.deleteService(serviceId);
		serviceOperation.deleteService(serviceId2);
		serviceOperation.deleteService(serviceId3);
	}

}
