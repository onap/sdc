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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.HeatParametersOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.RequirementOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.model.operations.impl.util.PrintGraph;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.HeatParameterData;
import org.openecomp.sdc.be.resources.data.HeatParameterValueData;
import org.openecomp.sdc.be.resources.data.RelationshipInstData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ResourceInstanceOperationTest extends ModelTestBase {
	private static Logger log = LoggerFactory.getLogger(ResourceInstanceOperationTest.class.getName());
	private Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	private static String USER_ID = "muserId";
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

	@javax.annotation.Resource(name = "component-instance-operation")
	private ComponentInstanceOperation resourceInstanceOperation;

	@javax.annotation.Resource
	private HeatParametersOperation heatParameterOperation;

	@javax.annotation.Resource
	private ArtifactOperation artifactOperation;

	private String CAPABILITY_1 = "mycapability101";
	private String CAPABILITY_2 = "mycapability102";

	private Integer TEST_CLASS_NUMBER = 1;

	public final static Pattern COMPONENT_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-\\_]+");

	public final static Pattern COMPONENT_INCTANCE_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-\\_]+");

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
		//
		// ////inmemory
		// boolean useInMemory = true;
		// if (useInMemory) {
		// configuration.setTitanInMemoryGraph(true);
		// } else {
		// configuration.setTitanInMemoryGraph(false);
		// configuration.setTitanCfgFile("C:\\Git_work\\D2-SDnC\\catalog-be\\src\\main\\resources\\config\\titan.properties");
		// }
		//
		//
		//
		// configurationManager.setConfiguration(configuration);
		ModelTestBase.init();
	}

	public void setOperations(TitanGenericDao titanDao, CapabilityTypeOperation capabilityTypeOperation,
			RequirementOperation requirementOperation, CapabilityOperation capabilityOperation,
			ResourceOperation resourceOperation, PropertyOperation propertyOperation,
			ComponentInstanceOperation resourceInstanceOperation2) {
		this.titanDao = titanDao;
		this.capabilityTypeOperation = capabilityTypeOperation;
		this.capabilityOperation = capabilityOperation;
		this.requirementOperation = requirementOperation;
		this.resourceOperation = resourceOperation;
		this.propertyOperation = propertyOperation;
		this.resourceInstanceOperation = resourceInstanceOperation2;
	}

	@Test
	public void testDummy() {

		assertTrue(requirementOperation != null);

	}

	@Test
	public void testAddResourceInstanceInvalidServiceId() {

		try {
			ComponentInstance instance = buildResourceInstance("tosca.nodes.Apache.2.0", "1", "tosca.nodes.Apache");

			Either<ComponentInstance, TitanOperationStatus> status = resourceInstanceOperation
					.addComponentInstanceToContainerComponent("service1", NodeTypeEnum.Service, "1", true, instance,
							NodeTypeEnum.Resource, false);
			assertEquals("check failed status - service is not in graph", true, status.isRight());
			assertEquals("check failed status value - service is not in graph", TitanOperationStatus.INVALID_ID,
					status.right().value());
		} finally {
			titanDao.rollback();
		}

	}

	@Test
	public void testAddResourceInstanceValidServiceIdInvalidResourceId() {
		try {

			ServiceMetadataData serviceData1 = createService("myservice1.1.0");

			ComponentInstance instance = buildResourceInstance("tosca.nodes.Apache.2.0", "1", "tosca.nodes.Apache");

			Either<ComponentInstance, TitanOperationStatus> status = resourceInstanceOperation
					.addComponentInstanceToContainerComponent((String) serviceData1.getUniqueId(), NodeTypeEnum.Service,
							"1", true, instance, NodeTypeEnum.Resource, false);

			assertEquals("check failed status - service is not in graph", true, status.isRight());
			assertEquals("check failed status value - service is not in graph", TitanOperationStatus.INVALID_ID,
					status.right().value());

		} finally {
			titanDao.rollback();
		}

	}

	@Test
	public void testAddResourceInstanceValidServiceId() {
		try {
			String serviceName = "myservice1.1.0";
			String resourceName = "tosca.nodes.Apache.2.0";
			ServiceMetadataData serviceData1 = createService(serviceName);
			ResourceMetadataData resourceData = createResource(resourceName);

			ComponentInstance instance = buildResourceInstance(resourceData.getMetadataDataDefinition().getUniqueId(),
					"1", "tosca.nodes.Apache");

			Either<ComponentInstance, TitanOperationStatus> status = resourceInstanceOperation
					.addComponentInstanceToContainerComponent((String) serviceData1.getUniqueId(), NodeTypeEnum.Service,
							"1", true, instance, NodeTypeEnum.Resource, false);

			assertEquals("check success status - service is not in graph", true, status.isLeft());

			ComponentInstance value = status.left().value();
			assertEquals("check name exists", "tosca.nodes.Apache 1", value.getName());

			ServiceMetadataData serviceData2 = deleteService(serviceName);
			ResourceMetadataData resourceData2 = deleteResource(resourceName);

		} finally {
			titanDao.rollback();
		}
	}

	@Test
	public void testUpdateResourceInstance() {
		try {
			String serviceName = "myservice1.1.0";
			String resourceName = "tosca.nodes.Apache.2.0";
			ServiceMetadataData serviceData1 = createService(serviceName);
			ResourceMetadataData resourceData = createResource(resourceName);

			ComponentInstance instance = buildResourceInstance(resourceData.getMetadataDataDefinition().getUniqueId(),
					"1", "tosca.nodes.Apache");

			Either<ComponentInstance, TitanOperationStatus> status = resourceInstanceOperation
					.addComponentInstanceToContainerComponent((String) serviceData1.getUniqueId(), NodeTypeEnum.Service,
							"1", true, instance, NodeTypeEnum.Resource, false);

			ComponentInstance resourceInstance = status.left().value();
			Long creationTime = resourceInstance.getCreationTime();
			String name = resourceInstance.getName();
			assertEquals("check success status - service is not in graph", true, status.isLeft());

			ComponentInstance value = status.left().value();
			assertEquals("check name exists", "tosca.nodes.Apache 1", value.getName());

			Either<ComponentInstance, StorageOperationStatus> u1Res = resourceInstanceOperation.updateResourceInstance(
					(String) serviceData1.getUniqueId(), NodeTypeEnum.Service, resourceInstance.getUniqueId(), value,
					true);
			assertTrue("check update succeed", u1Res.isLeft());

			Long lastModificationTimeNC = value.getModificationTime();
			String desc = "AAAAA";
			String posX = "15";
			String posY = "12";
			String updatedName = "Shlokshlik";
			value.setDescription(desc);
			value.setPosX(posX);
			Either<ComponentInstance, StorageOperationStatus> u2Res = resourceInstanceOperation.updateResourceInstance(
					(String) serviceData1.getUniqueId(), NodeTypeEnum.Service, resourceInstance.getUniqueId(), value,
					true);
			assertTrue("check update succeed", u2Res.isLeft());
			assertEquals("check resource instance updated", desc, u2Res.left().value().getDescription());
			assertEquals("check resource instance updated", posX, u2Res.left().value().getPosX());
			assertEquals("check resource instance updated", resourceInstance.getPosY(), u2Res.left().value().getPosY());
			assertEquals("check modification time was not updated since it was supplied",
					u2Res.left().value().getModificationTime(), lastModificationTimeNC);

			Long lastModificationTime = value.getModificationTime();
			value.setPosY(posY);
			value.setModificationTime(null);
			value.setName(updatedName);
			Either<ComponentInstance, StorageOperationStatus> u3Res = resourceInstanceOperation.updateResourceInstance(
					(String) serviceData1.getUniqueId(), NodeTypeEnum.Service, resourceInstance.getUniqueId(), value,
					true);
			assertTrue("check update succeed", u3Res.isLeft());
			assertEquals("check resource instance updated", desc, u3Res.left().value().getDescription());
			assertEquals("check resource pos x updated", posX, u3Res.left().value().getPosX());
			assertEquals("check resource pos y updated", posY, u3Res.left().value().getPosY());
			assertTrue("check modification time was updated",
					u3Res.left().value().getModificationTime() >= lastModificationTime);
			assertEquals("check creation time was not updated", creationTime, u3Res.left().value().getCreationTime());
			assertEquals("check name was  updated", updatedName, u3Res.left().value().getName());

			ServiceMetadataData serviceData2 = deleteService(serviceName);
			ResourceMetadataData resourceData2 = deleteResource(resourceName);

		} finally {
			titanDao.rollback();
		}
	}

	@Test
	public void testRemoveResourceInstance() {
		try {
			String serviceName = "myservice1.1.0";
			String resourceName = "tosca.nodes.Apache.2.0";
			ServiceMetadataData serviceData1 = createService(serviceName);
			ResourceMetadataData resourceData = createResource(resourceName);

			ComponentInstance instance = buildResourceInstance(resourceData.getMetadataDataDefinition().getUniqueId(),
					"1", "tosca.nodes.Apache");

			Either<ComponentInstance, TitanOperationStatus> status = resourceInstanceOperation
					.addComponentInstanceToContainerComponent((String) serviceData1.getUniqueId(), NodeTypeEnum.Service,
							"1", true, instance, NodeTypeEnum.Resource, false);

			assertEquals("check success status - service is not in graph", true, status.isLeft());

			ComponentInstance value = status.left().value();
			assertEquals("check name exists", "tosca.nodes.Apache 1", value.getName());

			Either<ComponentInstance, TitanOperationStatus> status1 = resourceInstanceOperation
					.removeComponentInstanceFromComponent(NodeTypeEnum.Service, serviceName, value.getUniqueId());

			assertTrue("check resource service was deleted.", status1.isLeft());
			assertEquals("check resource instance returned.", "tosca.nodes.Apache 1", status1.left().value().getName());

			ServiceMetadataData serviceData2 = deleteService(serviceName);
			ResourceMetadataData resourceData2 = deleteResource(resourceName);

		} finally {
			titanDao.rollback();
		}
	}

	@Test
	public void testRemoveResourceInstanceNotFound() {
		try {
			String serviceName = "myservice1.1.0";
			ServiceMetadataData serviceData1 = createService(serviceName);

			Either<ComponentInstance, TitanOperationStatus> status1 = resourceInstanceOperation
					.removeComponentInstanceFromComponent(NodeTypeEnum.Service, serviceName, "stam");

			assertTrue("check resource service was not deleted.", status1.isRight());
			assertEquals("check NOT_FOUND returned.", TitanOperationStatus.NOT_FOUND, status1.right().value());

			ServiceMetadataData serviceData2 = deleteService(serviceName);

		} finally {
			titanDao.rollback();
		}
	}

	public ServiceMetadataData createService(String serviceName) {

		ServiceMetadataData serviceData1 = new ServiceMetadataData();
		serviceData1.getMetadataDataDefinition().setUniqueId(serviceName);
		Either<ServiceMetadataData, TitanOperationStatus> createNode = titanDao.createNode(serviceData1,
				ServiceMetadataData.class);

		assertTrue("check service created", createNode.isLeft());
		return createNode.left().value();
	}

	public ServiceMetadataData deleteService(String serviceName) {

		ServiceMetadataData serviceData1 = new ServiceMetadataData();
		serviceData1.getMetadataDataDefinition().setUniqueId(serviceName);
		Either<ServiceMetadataData, TitanOperationStatus> createNode = titanDao.deleteNode(serviceData1,
				ServiceMetadataData.class);
		assertTrue("check service deleted", createNode.isLeft());
		return createNode.left().value();
	}

	public ResourceMetadataData createResource(String resourceName) {

		ResourceMetadataData serviceData1 = new ResourceMetadataData();
		serviceData1.getMetadataDataDefinition().setUniqueId(resourceName);
		Either<ResourceMetadataData, TitanOperationStatus> createNode = titanDao.createNode(serviceData1,
				ResourceMetadataData.class);

		assertTrue("check service created", createNode.isLeft());
		return createNode.left().value();
	}

	public ResourceMetadataData deleteResource(String resourceName) {

		ResourceMetadataData serviceData1 = new ResourceMetadataData();
		serviceData1.getMetadataDataDefinition().setUniqueId(resourceName);
		Either<ResourceMetadataData, TitanOperationStatus> createNode = titanDao.deleteNode(serviceData1,
				ResourceMetadataData.class);

		assertTrue("check service created", createNode.isLeft());
		return createNode.left().value();
	}

	@Test
	public void testAddResourceInstanceJson() {
		addResourceInstanceJson();
	}

	public ComponentInstance addResourceInstanceJson() {

		ComponentInstance resourceInstance = buildResourceInstance("tosca.nodes.Apache.2.0", "1", "tosca.nodes.Apache");

		String json = prettyGson.toJson(resourceInstance);
		log.debug(json);

		return resourceInstance;

	}

	private ComponentInstance buildResourceInstance(String respurceUid, String instanceNumber, String name) {
		ComponentInstance resourceInstance = new ComponentInstance();
		// resourceInstance
		// .setUniqueId("<SN>.tosca.nodes.Apache.2.0." + instanceNumber);
		resourceInstance.setName(name);
		resourceInstance.setDescription("desc1");
		resourceInstance.setPosX("20");
		resourceInstance.setPosY("40");
		resourceInstance.setComponentUid(respurceUid);
		resourceInstance.setCreationTime(System.currentTimeMillis());
		resourceInstance.setModificationTime(System.currentTimeMillis());
		resourceInstance.setNormalizedName(normaliseComponentName(name));

		// Map<String, RequirementInstance> requirements = new HashMap<String,
		// RequirementInstance>();
		//
		// RequirementInstance requirementInstance1 = new RequirementInstance();
		// requirementInstance1.setNode("NA");
		// RelationshipImpl relationshipImpl = new RelationshipImpl();
		// relationshipImpl.setType("tosca.relationships.HostedOn");
		// requirementInstance1.setRelationship(relationshipImpl);
		//
		// requirements.put("host", requirementInstance1);
		//
		// RequirementInstance requirementInstance2 = new RequirementInstance();
		// requirementInstance2.setNode("NA");
		// RelationshipImpl relationshipImpl2 = new RelationshipImpl();
		// relationshipImpl2.setType("tosca.relationships.LinkTo");
		// requirementInstance2.setRelationship(relationshipImpl2);
		//
		// requirements.put("link", requirementInstance2);
		//
		// resourceInstance.setRequirements(requirements);
		return resourceInstance;
	}

	@Test
	public void testConenctResourceInstancesJson() {
		RequirementCapabilityRelDef addRelationship = addRelationship("apache_1", "compute_100");
		String json = prettyGson.toJson(addRelationship);
		log.debug(json);

		RequirementCapabilityRelDef capabilityRelDef = prettyGson.fromJson(json, RequirementCapabilityRelDef.class);
		log.debug("{}", capabilityRelDef);

	}

	public RequirementCapabilityRelDef addRelationship(String from, String to) {
		RequirementCapabilityRelDef requirementCapabilityRelDef = new RequirementCapabilityRelDef();
		requirementCapabilityRelDef.setFromNode(from);
		requirementCapabilityRelDef.setToNode(to);
		List<RequirementAndRelationshipPair> relationships = new ArrayList<RequirementAndRelationshipPair>();

		String req = "host";
		RelationshipImpl relationshipImpl = new RelationshipImpl();
		relationshipImpl.setType("tosca.nodes.HostedOn");
		RequirementAndRelationshipPair rels = new RequirementAndRelationshipPair(req, relationshipImpl);
		relationships.add(rels);

		requirementCapabilityRelDef.setRelationships(relationships);

		return requirementCapabilityRelDef;
	}

	@Before
	public void createUserAndCategory() {
		deleteAndCreateCategory(CATEGORY_NAME);
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

	private void deleteAndCreateCategory(String category) {
		String[] names = category.split("/");
		OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanDao);
		OperationTestsUtil.deleteAndCreateServiceCategory(category, titanDao);

		/*
		 * CategoryData categoryData = new CategoryData();
		 * categoryData.setName(category);
		 * 
		 * titanDao.deleteNode(categoryData, CategoryData.class);
		 * Either<CategoryData, TitanOperationStatus> createNode = titanDao
		 * .createNode(categoryData, CategoryData.class);
		 * System.out.println("after creating caetgory " + createNode);
		 */
	}

	@Test
	@Ignore
	public void testConnectResourceInstances() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());
		try {

			String capabilityTypeName = CAPABILITY_2;
			String reqName = "host";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef = capabilityTypeOperationTest
					.createCapability(capabilityTypeName);
			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			// rollbackAndPrint();

			// Add capabilities to Compute Resource
			CapabilityDefinition addCapability = addCapabilityToResource(capabilityTypeName, "host", computeComponent);

			// CapabilityDefinition capabilityDefinition = new
			// CapabilityDefinition();
			// capabilityDefinition.setDescription("my capability");
			// capabilityDefinition.setType(capabilityTypeName);
			// List<String> validSourceTypes = new ArrayList<String>();
			// validSourceTypes.add("tosca.nodes.SC");
			// capabilityDefinition.setValidSourceTypes(validSourceTypes);
			// Either<CapabilityDefinition, StorageOperationStatus>
			// addCapability = capabilityOperation
			// .addCapability(computeComponent.getUniqueId(), "host",
			// capabilityDefinition, true);
			// //logger.debug("addCapability result " + addCapability);
			// assertTrue("check capability created ", addCapability.isLeft());
			//
			// =============================================

			// create requirement definition

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = addRequirementToResource(
					capabilityTypeName, reqName, reqNodeName, reqRelationship, softwareComponent);

			String parentReqUniqId = addRequirementToResource.left().value().getUniqueId();

			// create my resource derived from software component
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource",
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceName = "myservice.1.0";
			List<ComponentInstance> resInstances = buildServiceAndConnectBetweenResourceInstances(serviceName, resource,
					computeComponent, "host", false, addCapability.getUniqueId(),
					addRequirementToResource.left().value().getUniqueId());

			PrintGraph printGraph = new PrintGraph();
			String webGraph = printGraph.buildGraphForWebgraphWiz(titanDao.getGraph().left().value());
			log.debug(webGraph);

			Either<Resource, StorageOperationStatus> resourceFull = resourceOperation
					.getResource(resource.getUniqueId());
			assertTrue(resourceFull.isLeft());
			List<RequirementCapabilityRelDef> componentInstancesRelations = resourceFull.left().value()
					.getComponentInstancesRelations();

			RequirementCapabilityRelDef capabilityRelDef = componentInstancesRelations.get(0);
			capabilityRelDef.getRelationships().get(0).setRequirement("host");

			// disconnectResourcesInService(serviceName, resInstances.get(0),
			// "host");
			disconnectResourcesInService(serviceName, capabilityRelDef);

		} finally {
			rollbackAndPrint(false);
			compareGraphSize(numberOfVertices);
		}

	}

	@Test
	@Ignore
	public void testConnectResourceInstances1Requirement2Capabilities() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());

		try {

			String capabilityTypeName1 = CAPABILITY_1;
			String capabilityTypeName2 = CAPABILITY_2;
			String reqName1 = "host1";
			String reqName2 = "host2";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef1 = capabilityTypeOperationTest
					.createCapability(capabilityTypeName1);
			CapabilityTypeDefinition createCapabilityDef2 = capabilityTypeOperationTest
					.createCapability(capabilityTypeName2);

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);

			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			// Add capabilities to Compute Resource
			CapabilityDefinition capabilty1 = addCapabilityToResource(capabilityTypeName1, reqName1, computeComponent);
			CapabilityDefinition capabilty2 = addCapabilityToResource(capabilityTypeName2, reqName2, computeComponent);

			// rollbackAndPrint();

			// create requirement definition

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = addRequirementToResource(
					capabilityTypeName1, reqName1, reqNodeName, reqRelationship, softwareComponent);

			String requirementId = addRequirementToResource.left().value().getUniqueId();
			String parentReqUniqId = requirementId;

			// create my resource derived from software component
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource",
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceName = "myservice.1.0";
			List<ComponentInstance> resInstances = buildServiceAndConnectBetweenResourceInstances(serviceName, resource,
					computeComponent, reqName1, false, capabilty1.getUniqueId(), requirementId);

			PrintGraph printGraph = new PrintGraph();
			String webGraph = printGraph.buildGraphForWebgraphWiz(titanDao.getGraph().left().value());
			log.debug(webGraph);

			RequirementAndRelationshipPair relationPair = new RequirementAndRelationshipPair();
			relationPair.setRequirement(reqName2);

			relationPair.setCapabilityUid(capabilty1.getUniqueId());
			relationPair.setRequirementUid(requirementId);

			Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService1 = resourceInstanceOperation
					.connectResourcesInService(serviceName, NodeTypeEnum.Service, resInstances.get(0).getUniqueId(),
							resInstances.get(1).getUniqueId(), relationPair);
			assertEquals("check cannot associate resource instances", TitanOperationStatus.ILLEGAL_ARGUMENT,
					connectResourcesInService1.right().value());
			relationPair.setRequirement(reqName1);
			Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService2 = resourceInstanceOperation
					.connectResourcesInService(serviceName, NodeTypeEnum.Service, resInstances.get(0).getUniqueId(),
							resInstances.get(1).getUniqueId(), relationPair);
			assertEquals("check cannot associate resource instances", TitanOperationStatus.TITAN_SCHEMA_VIOLATION,
					connectResourcesInService2.right().value());

			relationPair.setRequirement(reqName1);

			RequirementCapabilityRelDef capabilityRelDef = new RequirementCapabilityRelDef();
			capabilityRelDef.setFromNode(resInstances.get(0).getUniqueId());
			capabilityRelDef.setToNode(resInstances.get(1).getUniqueId());
			List<RequirementAndRelationshipPair> list = new ArrayList<>();
			list.add(relationPair);

			disconnectResourcesInService(serviceName, capabilityRelDef);

		} finally {
			rollbackAndPrint();
			compareGraphSize(numberOfVertices);
		}

	}

	private void rollbackAndPrint() {
		rollbackAndPrint(false);
	}

	private void rollbackAndPrint(boolean print) {
		TitanOperationStatus rollback = titanDao.rollback();
		if (print) {
			log.debug("rollback status={}", rollback);
			PrintGraph printGraph = new PrintGraph();
			printGraph.printGraphVertices(titanDao.getGraph().left().value());
		}
	}

	@Test
	public void testConnectResourceInstances2Requirement2Capabilities() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());

		try {

			String capabilityTypeName1 = CAPABILITY_1;
			String capabilityTypeName2 = CAPABILITY_2;
			String reqName1 = "host1";
			String reqName2 = "host2";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef1 = capabilityTypeOperationTest
					.createCapability(capabilityTypeName1);
			CapabilityTypeDefinition createCapabilityDef2 = capabilityTypeOperationTest
					.createCapability(capabilityTypeName2);

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// rollbackAndPrint();
			// OKKKKKKK

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			// rollbackAndPrint();
			// OKKKKKKKKKK

			// Add capabilities to Compute Resource
			CapabilityDefinition capabilty1 = addCapabilityToResource(capabilityTypeName1, reqName1, computeComponent);
			CapabilityDefinition capabilty2 = addCapabilityToResource(capabilityTypeName2, reqName2, computeComponent);

			// rollbackAndPrint();

			// create requirement definition

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource1 = addRequirementToResource(
					capabilityTypeName1, reqName1, reqNodeName, reqRelationship, softwareComponent);

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource2 = addRequirementToResource(
					capabilityTypeName2, reqName2, reqNodeName, reqRelationship, softwareComponent);

			// create my resource derived from software component
			String MY_RESOURCE = "my-resource";
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, MY_RESOURCE,
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceName = "myservice.1.0";
			String requirementId1 = addRequirementToResource1.left().value().getUniqueId();
			String requirementId2 = addRequirementToResource2.left().value().getUniqueId();
			List<ComponentInstance> resInstances = buildServiceAndConnectBetweenResourceInstances(serviceName, resource,
					computeComponent, reqName1, false, capabilty1.getUniqueId(), requirementId1);

			RequirementAndRelationshipPair relationPair = new RequirementAndRelationshipPair();
			relationPair.setRequirement(reqName2);
			relationPair.setCapabilityUid(capabilty2.getUniqueId());
			relationPair.setRequirementUid(requirementId2);
			relationPair.setCapabilityOwnerId(resInstances.get(1).getUniqueId());
			relationPair.setRequirementOwnerId(resInstances.get(0).getUniqueId());
			Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService1 = resourceInstanceOperation
					.connectResourcesInService(serviceName, NodeTypeEnum.Service, resInstances.get(0).getUniqueId(),
							resInstances.get(1).getUniqueId(), relationPair);
			assertTrue("check associate resource instances succeed " + reqName2, connectResourcesInService1.isLeft());

			// rollbackAndPrint();

			PrintGraph printGraph = new PrintGraph();
			String webGraph = printGraph.buildGraphForWebgraphWiz(titanDao.getGraph().left().value());
			log.debug(webGraph);

			RequirementCapabilityRelDef reqCapDef = new RequirementCapabilityRelDef();
			reqCapDef.setFromNode(resInstances.get(0).getUniqueId());
			reqCapDef.setToNode(resInstances.get(1).getUniqueId());

			relationPair.setRequirement(reqName1);
			relationPair.setCapabilityUid(capabilty1.getUniqueId());
			relationPair.setRequirementUid(requirementId1);
			RelationshipImpl relationship = new RelationshipImpl();
			relationship.setType(reqName1);
			relationPair.setRelationships(relationship);

			List<RequirementAndRelationshipPair> list = new ArrayList<>();
			list.add(relationPair);
			reqCapDef.setRelationships(list);

			disconnectResourcesInService(serviceName, reqCapDef);

			reqCapDef.getRelationships().clear();

			RequirementAndRelationshipPair relationPair1 = new RequirementAndRelationshipPair();
			relationPair1.setRequirement(reqName2);
			relationPair1.setCapabilityUid(capabilty2.getUniqueId());
			relationPair1.setRequirementUid(requirementId2);
			relationPair1.setCapabilityOwnerId(resInstances.get(1).getUniqueId());
			relationPair1.setRequirementOwnerId(resInstances.get(0).getUniqueId());
			relationship.setType(reqName2);
			relationPair1.setRelationships(relationship);
			reqCapDef.getRelationships().add(relationPair1);

			disconnectResourcesInService(serviceName, reqCapDef);

			RequirementCapabilityRelDef relation = new RequirementCapabilityRelDef();
			String fromResUid = resInstances.get(0).getUniqueId();
			String toResUid = resInstances.get(1).getUniqueId();
			relation.setFromNode(fromResUid);
			relation.setToNode(toResUid);
			List<RequirementAndRelationshipPair> relationships = new ArrayList<RequirementAndRelationshipPair>();
			RequirementAndRelationshipPair immutablePair1 = new RequirementAndRelationshipPair(reqName1, null);
			RequirementAndRelationshipPair immutablePair2 = new RequirementAndRelationshipPair(reqName2, null);
			immutablePair1.setCapabilityUid(capabilty1.getUniqueId());
			immutablePair1.setRequirementUid(addRequirementToResource1.left().value().getUniqueId());
			immutablePair1.setRequirementOwnerId(resInstances.get(0).getUniqueId());
			immutablePair1.setCapabilityOwnerId(resInstances.get(1).getUniqueId());

			immutablePair2.setCapabilityUid(capabilty2.getUniqueId());
			immutablePair2.setRequirementUid(addRequirementToResource2.left().value().getUniqueId());
			immutablePair2.setRequirementOwnerId(resInstances.get(0).getUniqueId());
			immutablePair2.setCapabilityOwnerId(resInstances.get(1).getUniqueId());

			relationships.add(immutablePair1);
			relationships.add(immutablePair2);
			relation.setRelationships(relationships);

			Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances = resourceInstanceOperation
					.associateResourceInstances(serviceName, NodeTypeEnum.Service, relation, true);
			assertTrue("check return code after associating 2 requirements in one request",
					associateResourceInstances.isLeft());
			RequirementCapabilityRelDef capabilityRelDef = associateResourceInstances.left().value();
			String fromNode = capabilityRelDef.getFromNode();
			assertEquals("check from node", resInstances.get(0).getUniqueId(), fromNode);
			String toNode = capabilityRelDef.getToNode();
			assertEquals("check to node", resInstances.get(1).getUniqueId(), toNode);
			List<RequirementAndRelationshipPair> relationships2 = capabilityRelDef.getRelationships();
			assertEquals("check number of relations", 2, relationships2.size());

			for (RequirementAndRelationshipPair pair : relationships2) {
				String key = pair.getRequirement();
				RelationshipImpl relationshipImpl = pair.getRelationship();
				if (key.equals(reqName1)) {
					String type = relationshipImpl.getType();
					assertEquals("Check relationship type name", reqRelationship, type);
				} else if (key.equals(reqName2)) {
					String type = relationshipImpl.getType();
					assertEquals("Check relationship type name", reqRelationship, type);
				} else {
					assertTrue("requirement " + key + " was not found in the original request", false);
				}
			}

			verifyGetAllResourceInstanceFromService(reqName1, reqName2, serviceName, fromResUid, toResUid);

			List<ResourceMetadataData> resourcesPathList = new ArrayList<ResourceMetadataData>();
			TitanOperationStatus findResourcesPathRecursively = resourceOperation
					.findResourcesPathRecursively(resource.getUniqueId(), resourcesPathList);
			assertEquals("check returned status", TitanOperationStatus.OK, findResourcesPathRecursively);
			assertEquals("check list size", 3, resourcesPathList.size());

			TitanOperationStatus validateTheTargetResourceInstance = resourceInstanceOperation
					.validateTheTargetResourceInstance(MY_RESOURCE, resource.getUniqueId());
			assertEquals("check resource name in the path", TitanOperationStatus.OK, validateTheTargetResourceInstance);
			validateTheTargetResourceInstance = resourceInstanceOperation
					.validateTheTargetResourceInstance(softwareCompName, resource.getUniqueId());
			assertEquals("check resource name in the path", TitanOperationStatus.OK, validateTheTargetResourceInstance);

			validateTheTargetResourceInstance = resourceInstanceOperation
					.validateTheTargetResourceInstance(softwareCompName + "STAM", resource.getUniqueId());
			assertEquals("check resource name not in the path", TitanOperationStatus.MATCH_NOT_FOUND,
					validateTheTargetResourceInstance);

			Either<ComponentInstance, StorageOperationStatus> deleteResourceInstance = resourceInstanceOperation
					.deleteComponentInstance(NodeTypeEnum.Service, serviceName, toResUid, true);
			assertTrue("check resource instance was deleted.", deleteResourceInstance.isLeft());

		} finally {
			rollbackAndPrint(false);
			compareGraphSize(numberOfVertices);
		}

	}

	private void verifyGetAllResourceInstanceFromService(String reqName1, String reqName2, String serviceName,
			String fromResUid, String toResUid) {

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, StorageOperationStatus> allResourceInstances = resourceInstanceOperation
				.getAllComponentInstances(serviceName, NodeTypeEnum.Service, NodeTypeEnum.Resource, true);
		// assertTrue("check return code after get all resource instances",
		// associateResourceInstances.isLeft());
		ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>> immutablePair = allResourceInstances
				.left().value();
		List<ComponentInstance> nodes = immutablePair.getKey();
		List<RequirementCapabilityRelDef> edges = immutablePair.getValue();
		assertEquals("check 2 nodes returned", 2, nodes.size());
		assertEquals("check one relation returned", 1, edges.size());
		RequirementCapabilityRelDef requirementCapabilityRelDef = edges.get(0);
		assertEquals("check from node", requirementCapabilityRelDef.getFromNode(), fromResUid);
		requirementCapabilityRelDef.getToNode();
		assertEquals("check to node", requirementCapabilityRelDef.getToNode(), toResUid);
		int size = requirementCapabilityRelDef.getRelationships().size();
		assertEquals("check number of relations", 2, size);
		String req1 = requirementCapabilityRelDef.getRelationships().get(0).getRequirement();
		String req2 = requirementCapabilityRelDef.getRelationships().get(1).getRequirement();

		List<String> requirements = new ArrayList<String>();
		requirements.add(req1);
		requirements.add(req2);

		assertTrue("check requirement returned " + reqName1, requirements.contains(reqName1));
		assertTrue("check requirement returned " + reqName2, requirements.contains(reqName2));

		String nodesStr = prettyGson.toJson(nodes);
		String edgesStr = prettyGson.toJson(edges);

		log.debug(nodesStr);
		log.debug(edgesStr);
	}

	private Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource(String capabilityTypeName1,
			String reqName1, String reqNodeName, String reqRelationship, Resource softwareComponent) {
		RequirementDefinition reqDefinition1 = new RequirementDefinition();
		reqDefinition1.setNode(reqNodeName);
		reqDefinition1.setRelationship(reqRelationship);
		reqDefinition1.setCapability(capabilityTypeName1);
		// add requirement to software component
		Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = requirementOperation
				.addRequirementToResource(reqName1, reqDefinition1, softwareComponent.getUniqueId(), true);
		assertEquals("check requirement was added", true, addRequirementToResource.isLeft());
		return addRequirementToResource;
	}

	private CapabilityDefinition addCapabilityToResource(String capabilityTypeName1, String reqName1,
			Resource computeComponent) {
		CapabilityDefinition capabilityDefinition1 = new CapabilityDefinition();
		capabilityDefinition1.setDescription("my capability");
		capabilityDefinition1.setType(capabilityTypeName1);
		List<String> validSourceTypes = new ArrayList<String>();
		validSourceTypes.add("tosca.nodes.SC");
		capabilityDefinition1.setValidSourceTypes(validSourceTypes);
		Either<CapabilityDefinition, StorageOperationStatus> addCapability = capabilityOperation
				.addCapability(computeComponent.getUniqueId(), reqName1, capabilityDefinition1, true);
		assertTrue("check capability created ", addCapability.isLeft());
		return addCapability.left().value();
	}

	@Test
	public void testConnectResourceInstancesCapabilityNameDiffFromReqName() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());

		try {

			String capabilityTypeName = CAPABILITY_2;
			String reqName = "host";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			String DIFFERENT_CAPABILITY = "hostDiffernet";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef = capabilityTypeOperationTest
					.createCapability(capabilityTypeName);

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			CapabilityDefinition capabilty = addCapabilityToResource(capabilityTypeName, DIFFERENT_CAPABILITY,
					computeComponent);

			// create requirement definition

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = addRequirementToResource(
					capabilityTypeName, reqName, reqNodeName, reqRelationship, softwareComponent);

			String parentReqUniqId = addRequirementToResource.left().value().getUniqueId();

			// create my resource derived from software component
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource",
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceName = "myservice.1.0";
			List<ComponentInstance> resInstances = buildServiceAndConnectBetweenResourceInstances(serviceName, resource,
					computeComponent, "host", false, capabilty.getUniqueId(), parentReqUniqId);

			PrintGraph printGraph = new PrintGraph();
			String webGraph = printGraph.buildGraphForWebgraphWiz(titanDao.getGraph().left().value());
			// log.debug(webGraph);

		} finally {
			rollbackAndPrint();

			compareGraphSize(numberOfVertices);
		}

	}

	@Test
	public void testConnectResourceInstancesInvalidCapability() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());

		try {

			String capabilityTypeName = CAPABILITY_2;
			String reqName = "host";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			String capabilityTypeNameOther = CAPABILITY_2 + "othertype";

			String DIFFERENT_CAPABILITY = "hostDiffernet";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef = capabilityTypeOperationTest
					.createCapability(capabilityTypeName);

			CapabilityTypeDefinition createCapabilityDef2 = capabilityTypeOperationTest
					.createCapability(capabilityTypeNameOther);

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			addCapabilityToResource(capabilityTypeName, DIFFERENT_CAPABILITY, computeComponent);

			// create requirement definition

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = addRequirementToResource(
					capabilityTypeNameOther, reqName, reqNodeName, reqRelationship, softwareComponent);

			String parentReqUniqId = addRequirementToResource.left().value().getUniqueId();

			// create my resource derived from software component
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource",
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceName = "myservice.1.0";
			List<ComponentInstance> resInstances = buildServiceAndConnectBetweenResourceInstancesWithError(serviceName,
					resource, computeComponent, "host", false, TitanOperationStatus.ILLEGAL_ARGUMENT);

			PrintGraph printGraph = new PrintGraph();
			String webGraph = printGraph.buildGraphForWebgraphWiz(titanDao.getGraph().left().value());
			log.debug(webGraph);

		} finally {
			rollbackAndPrint();

			compareGraphSize(numberOfVertices);
		}

	}

	private void compareGraphSize(int numberOfVertices, Set<String> toRemoveFromSet) {
		PrintGraph printGraph2 = new PrintGraph();
		int numberOfVerticesCurr = printGraph2.getNumberOfVertices(titanDao.getGraph().left().value());

		Set<String> set = printGraph2.getVerticesSet(titanDao.getGraph().left().value());
		if (toRemoveFromSet != null) {
			set.removeAll(toRemoveFromSet);
		}

		assertEquals("check all data deleted from graph " + set, numberOfVertices, numberOfVerticesCurr);
	}

	private void compareGraphSize(int numberOfVertices) {
		PrintGraph printGraph2 = new PrintGraph();
		int numberOfVerticesCurr = printGraph2.getNumberOfVertices(titanDao.getGraph().left().value());

		assertEquals(
				"check all data deleted from graph " + printGraph2.getVerticesSet(titanDao.getGraph().left().value()),
				numberOfVertices, numberOfVerticesCurr);
	}

	@Test
	public void testConnectResourceInstancesRequirementNotFound() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());
		try {

			String capabilityTypeName = CAPABILITY_2;
			String reqName = "host";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			String DIFFERENT_CAPABILITY = "hostDiffernet";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef = capabilityTypeOperationTest
					.createCapability(capabilityTypeName);

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			addCapabilityToResource(capabilityTypeName, reqName, computeComponent);

			// create requirement definition

			RequirementDefinition reqDefinition = new RequirementDefinition();
			reqDefinition.setNode(reqNodeName);
			reqDefinition.setRelationship(reqRelationship);
			reqDefinition.setCapability(capabilityTypeName);
			// add requirement to software component
			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = requirementOperation
					.addRequirementToResource(reqName + "ssssssss", reqDefinition, softwareComponent.getUniqueId(),
							true);
			assertEquals("check requirement was added", true, addRequirementToResource.isLeft());

			String parentReqUniqId = addRequirementToResource.left().value().getUniqueId();

			// create my resource derived from software component
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource",
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceName = "myservice.1.0";
			List<ComponentInstance> resInstances = buildServiceAndConnectBetweenResourceInstancesWithError(serviceName,
					resource, computeComponent, "host", false, TitanOperationStatus.ILLEGAL_ARGUMENT);

			PrintGraph printGraph = new PrintGraph();
			String webGraph = printGraph.buildGraphForWebgraphWiz(titanDao.getGraph().left().value());
			log.debug(webGraph);

		} finally {
			titanDao.rollback();

			compareGraphSize(numberOfVertices);
		}

	}

	private void disconnectResourcesInService(String serviceName, RequirementCapabilityRelDef reqCapDef) {

		Either<List<RelationshipInstData>, TitanOperationStatus> disconnectResourcesInService = resourceInstanceOperation
				.disconnectResourcesInService(serviceName, NodeTypeEnum.Service, reqCapDef);
		assertTrue("check relatioship instance was deleted", disconnectResourcesInService.isLeft());

		disconnectResourcesInService = resourceInstanceOperation.disconnectResourcesInService(serviceName,
				NodeTypeEnum.Service, reqCapDef);
		assertTrue("check relatioship instance already was deleted", disconnectResourcesInService.isRight());
		assertEquals("check relatioship instance already was deleted. status NOT_FOUND", TitanOperationStatus.NOT_FOUND,
				disconnectResourcesInService.right().value());
	}

	private List<ComponentInstance> buildServiceAndConnectBetweenResourceInstancesWithError(String serviceName,
			Resource resource, Resource computeComponent, String requirement, boolean ignoreCreatingService,
			TitanOperationStatus titanOperationStatus) {

		String serviceId = "myservice.1.0";

		if (false == ignoreCreatingService) {
			ServiceMetadataData createService = createService(serviceId);
		}
		ComponentInstance myresourceInstance = buildResourceInstance(resource.getUniqueId(), "1", resource.getName());

		ComponentInstance computeInstance = buildResourceInstance(computeComponent.getUniqueId(), "2",
				computeComponent.getName());

		Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
						myresourceInstance, NodeTypeEnum.Resource, false);
		assertTrue("check instance added to service", myinstanceRes.isLeft());
		ComponentInstance value1 = myinstanceRes.left().value();
		Either<ComponentInstance, TitanOperationStatus> computeInstTes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "2", true, computeInstance,
						NodeTypeEnum.Resource, false);
		assertTrue("check instance added to service", computeInstTes.isLeft());
		ComponentInstance value2 = computeInstTes.left().value();

		RequirementAndRelationshipPair relationPair = new RequirementAndRelationshipPair();
		relationPair.setRequirement(requirement);

		Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService = resourceInstanceOperation
				.connectResourcesInService(serviceId, NodeTypeEnum.Service, value1.getUniqueId(), value2.getUniqueId(),
						relationPair);

		assertTrue("check relation was not created", connectResourcesInService.isRight());
		assertEquals("check error code after connect resource instances failed", titanOperationStatus,
				connectResourcesInService.right().value());

		List<ComponentInstance> resInstances = new ArrayList<ComponentInstance>();
		resInstances.add(value1);

		return resInstances;

	}

	private List<ComponentInstance> buildServiceAndConnectBetweenResourceInstances(String serviceName,
			Resource resource, Resource computeComponent, String requirement, boolean ignoreCreatingService,
			String capabilityId, String requirementId) {

		String serviceId = "myservice.1.0";

		if (false == ignoreCreatingService) {
			ServiceMetadataData createService = createService(serviceId);
		}
		ComponentInstance myresourceInstance = buildResourceInstance(resource.getUniqueId(), "1", resource.getName());

		ComponentInstance computeInstance = buildResourceInstance(computeComponent.getUniqueId(), "2",
				computeComponent.getName());

		Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
						myresourceInstance, NodeTypeEnum.Resource, false);
		assertTrue("check instance added to service", myinstanceRes.isLeft());
		ComponentInstance value1 = myinstanceRes.left().value();
		Either<ComponentInstance, TitanOperationStatus> computeInstTes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "2", true, computeInstance,
						NodeTypeEnum.Resource, false);
		assertTrue("check instance added to service", computeInstTes.isLeft());
		ComponentInstance value2 = computeInstTes.left().value();
		RequirementAndRelationshipPair relationPair = new RequirementAndRelationshipPair();
		relationPair.setRequirement(requirement);

		relationPair.setCapabilityUid(capabilityId);
		relationPair.setRequirementUid(requirementId);
		relationPair.setRequirementOwnerId(value1.getUniqueId());
		relationPair.setCapabilityOwnerId(value2.getUniqueId());

		Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService = resourceInstanceOperation
				.connectResourcesInService(serviceId, NodeTypeEnum.Service, value1.getUniqueId(), value2.getUniqueId(),
						relationPair);

		assertTrue("check relation created", connectResourcesInService.isLeft());

		List<ComponentInstance> resInstances = new ArrayList<ComponentInstance>();
		resInstances.add(value1);
		resInstances.add(value2);

		return resInstances;

	}

	@Test
	public void getAllResourceInstancesThree() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());
		try {

			Set<String> vertexSetBeforeMethod = printGraph1.getVerticesSet(titanDao.getGraph().left().value());

			String capabilityTypeName = CAPABILITY_2;
			String reqName = "host";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef = capabilityTypeOperationTest
					.createCapability(capabilityTypeName);

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			// rollbackAndPrint();

			// Add capabilities to Compute Resource
			CapabilityDefinition capability = addCapabilityToResource(capabilityTypeName, "host", computeComponent);

			// create requirement definition

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = addRequirementToResource(
					capabilityTypeName, reqName, reqNodeName, reqRelationship, softwareComponent);

			String parentReqUniqId = addRequirementToResource.left().value().getUniqueId();

			// create my resource derived from software component
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource",
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceId = "myservice.1.0";

			ServiceMetadataData createService = createService(serviceId);

			Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, StorageOperationStatus> allResourceInstances = resourceInstanceOperation
					.getAllComponentInstances(serviceId, NodeTypeEnum.Service, NodeTypeEnum.Resource, true);
			assertTrue("check NOT_FOUND is returned", allResourceInstances.isRight());
			assertEquals("check NOT_FOUND is returned", allResourceInstances.right().value(),
					StorageOperationStatus.NOT_FOUND);

			ComponentInstance myresourceInstance = buildResourceInstance(resource.getUniqueId(), "1", "my-resource");
			myresourceInstance.setName("my-resource");

			ComponentInstance computeInstance1 = buildResourceInstance(computeComponent.getUniqueId(), "2",
					"tosca.nodes.Compute2");

			Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
					.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
							myresourceInstance, NodeTypeEnum.Resource, false);
			assertTrue("check instance added to service", myinstanceRes.isLeft());
			ComponentInstance value1 = myinstanceRes.left().value();

			allResourceInstances = resourceInstanceOperation.getAllComponentInstances(serviceId, NodeTypeEnum.Service,
					NodeTypeEnum.Resource, true);
			assertTrue("check resource instances found", allResourceInstances.isLeft());
			ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>> immutablePair = allResourceInstances
					.left().value();
			List<ComponentInstance> nodes = immutablePair.getKey();
			List<RequirementCapabilityRelDef> edges = immutablePair.getValue();

			assertEquals("check resource instances size", 1, nodes.size());
			assertEquals("check resource instances size", 0, edges.size());

			Either<ComponentInstance, TitanOperationStatus> computeInstTes = resourceInstanceOperation
					.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "2", true,
							computeInstance1, NodeTypeEnum.Resource, false);
			assertTrue("check instance added to service", computeInstTes.isLeft());
			ComponentInstance value2 = computeInstTes.left().value();

			allResourceInstances = resourceInstanceOperation.getAllComponentInstances(serviceId, NodeTypeEnum.Service,
					NodeTypeEnum.Resource, true);
			assertTrue("check resource instances found", allResourceInstances.isLeft());
			immutablePair = allResourceInstances.left().value();
			nodes = immutablePair.getKey();
			edges = immutablePair.getValue();

			assertEquals("check resource instances size", 2, nodes.size());
			assertEquals("check resource instances size", 0, edges.size());

			String requirement = "host";
			RequirementAndRelationshipPair relationPair = new RequirementAndRelationshipPair();
			relationPair.setRequirement(requirement);
			relationPair.setCapabilityUid(capability.getUniqueId());
			relationPair.setRequirementUid(addRequirementToResource.left().value().getUniqueId());
			relationPair.setRequirementOwnerId(value1.getUniqueId());
			relationPair.setCapabilityOwnerId(value2.getUniqueId());

			Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService = resourceInstanceOperation
					.connectResourcesInService(serviceId, NodeTypeEnum.Service, value1.getUniqueId(),
							value2.getUniqueId(), relationPair);

			assertTrue("check relation created", connectResourcesInService.isLeft());

			allResourceInstances = resourceInstanceOperation.getAllComponentInstances(serviceId, NodeTypeEnum.Service,
					NodeTypeEnum.Resource, true);
			assertTrue("check resource instances found", allResourceInstances.isLeft());
			immutablePair = allResourceInstances.left().value();
			nodes = immutablePair.getKey();
			edges = immutablePair.getValue();

			assertEquals("check resource instances size", 2, nodes.size());
			assertEquals("check resource instances size", 1, edges.size());

			List<ComponentInstance> resInstances2 = new ArrayList<ComponentInstance>();
			resInstances2.add(value1);
			resInstances2.add(value2);

			ComponentInstance myresourceInstance2 = buildResourceInstance(resource.getUniqueId(), "1", "myresource2");

			Either<ComponentInstance, TitanOperationStatus> newResource = resourceInstanceOperation
					.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "3", true,
							myresourceInstance2, NodeTypeEnum.Resource, false);

			assertTrue("added resource instance successfully", newResource.isLeft());

			relationPair.setRequirement(requirement);
			relationPair.setRequirementOwnerId(newResource.left().value().getUniqueId());

			Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService2 = resourceInstanceOperation
					.connectResourcesInService(serviceId, NodeTypeEnum.Service,
							newResource.left().value().getUniqueId(), value2.getUniqueId(), relationPair);
			assertTrue("check resource instance was added to service", connectResourcesInService2.isLeft());

			allResourceInstances = resourceInstanceOperation.getAllComponentInstances(serviceId, NodeTypeEnum.Service,
					NodeTypeEnum.Resource, true);
			assertTrue("check resource instances found", allResourceInstances.isLeft());
			immutablePair = allResourceInstances.left().value();
			nodes = immutablePair.getKey();
			edges = immutablePair.getValue();

			assertEquals("check resource instances size", 3, nodes.size());
			assertEquals("check resource instances size", 2, edges.size());

			Either<List<ComponentInstance>, TitanOperationStatus> deleteAllResourceInstancesOfService = resourceInstanceOperation
					.deleteAllComponentInstancesInternal(serviceId, NodeTypeEnum.Service);
			assertTrue("check resource instances was deleted.", deleteAllResourceInstancesOfService.isLeft());
			assertEquals("check number of deleted resource instances.", 3,
					deleteAllResourceInstancesOfService.left().value().size());

			Either<List<RelationshipInstData>, TitanOperationStatus> allRelatinshipInst = titanDao
					.getAll(NodeTypeEnum.RelationshipInst, RelationshipInstData.class);
			assertTrue("allRelatinshipInst is empty", allRelatinshipInst.isRight());
			assertEquals("allRelatinshipInst result is NOT_FOUND", TitanOperationStatus.NOT_FOUND,
					allRelatinshipInst.right().value());

			Either<Resource, StorageOperationStatus> deleteComputeResource = resourceOperation
					.deleteResource(computeComponent.getUniqueId(), true);
			assertTrue("delete compute resource succeed", deleteComputeResource.isLeft());

			Either<Resource, StorageOperationStatus> deleteSCResource = resourceOperation
					.deleteResource(softwareComponent.getUniqueId(), true);
			assertTrue("delete software component resource succeed", deleteSCResource.isLeft());

			Either<Resource, StorageOperationStatus> deleteMyResource = resourceOperation
					.deleteResource(resource.getUniqueId(), true);
			assertTrue("delete my resource succeed", deleteMyResource.isLeft());

			Either<Resource, StorageOperationStatus> rootResourceDeleted = resourceOperation
					.deleteResource(rootResource.getUniqueId(), true);
			assertTrue("delete root resource succeed", rootResourceDeleted.isLeft());

			Set<String> vertexSetAfterDelete = printGraph1.getVerticesSet(titanDao.getGraph().left().value());

			vertexSetAfterDelete.removeAll(vertexSetBeforeMethod);

			log.debug("vertexSetAfterDelete={}", vertexSetAfterDelete);
			log.debug("vertexSetAfterDelete size={}", vertexSetAfterDelete.size());

			// int numberOfVerticesAfterOperation =
			// printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());
			// System.out.println(numberOfVerticesAfterOperation);
			// 6 - service, 2 tags, capability + 2 parameters
			// compareGraphSize(numberOfVertices + 6, vertexSetBeforeMethod);

		} finally {
			rollbackAndPrint(false);
			compareGraphSize(numberOfVertices);
			// printGraph1.printGraphVertices(titanDao.getGraph().left().value());
		}

	}

	public void testCreateRootResource() {

		String name = "tosca.nodes.Root";

		String state = LifecycleStateEnum.CERTIFIED.name();

		ResourceMetadataData resourceData1 = new ResourceMetadataData();
		resourceData1.getMetadataDataDefinition().setUniqueId(UniqueIdBuilder.buildResourceUniqueId());
		resourceData1.getMetadataDataDefinition().setName(name);
		resourceData1.getMetadataDataDefinition().setState(state);
		resourceData1.getMetadataDataDefinition().setHighestVersion(true);
		resourceData1.getMetadataDataDefinition().setContactId("contactId");
		Either<ResourceMetadataData, TitanOperationStatus> createNode1 = titanDao.createNode(resourceData1,
				ResourceMetadataData.class);

		log.debug("{}", createNode1);

		titanDao.commit();
	}

	public void testMultiResourceCertified() {
		boolean create = true;
		String name = "myresource7";
		if (create) {

			String state = LifecycleStateEnum.CERTIFIED.name();
			boolean isHighestVersion = true;

			ResourceMetadataData resourceData1 = new ResourceMetadataData();
			resourceData1.getMetadataDataDefinition().setUniqueId(name + "." + "1.0");
			resourceData1.getMetadataDataDefinition().setName(name);
			resourceData1.getMetadataDataDefinition().setState(state);
			resourceData1.getMetadataDataDefinition().setHighestVersion(true);
			resourceData1.getMetadataDataDefinition().setContactId("contactId");
			Either<ResourceMetadataData, TitanOperationStatus> createNode1 = titanDao.createNode(resourceData1,
					ResourceMetadataData.class);

			log.debug("{}", createNode1);

			titanDao.commit();

			// resourceData1.setHighestVersion(false);
			resourceData1.getMetadataDataDefinition().setContactId("222contactId222");
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData1,
					ResourceMetadataData.class);

			titanDao.commit();

			// TitanGraph titanGraph = titanDao.getGraph().left().value();
			// Iterable<Result<Vertex>> vertices =
			// titanGraph.indexQuery("highestVersion",
			// "v.highestVersion:true").vertices();
			// for (Result<Vertex> vertex : vertices) {
			// Vertex element = vertex.getElement();
			// System.out.println( ElementHelper.getProperties(element));
			// }

		}

		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = searchForResource(name);

		log.debug("{}", byCriteria.left().value().size());

		byCriteria = searchForResource(name);

		log.debug("{}", byCriteria.left().value().size());

	}

	private Either<List<ResourceMetadataData>, TitanOperationStatus> searchForResource(String name) {
		Map<String, Object> propertiesToMatch = new HashMap<String, Object>();
		propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());
		// propertiesToMatch.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(),
		// true);
		propertiesToMatch.put(GraphPropertiesDictionary.NAME.getProperty(), name);
		propertiesToMatch.put(GraphPropertiesDictionary.CONTACT_ID.getProperty(), "contactId");
		// propertiesToMatch.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(),
		// true);
		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = titanDao
				.getByCriteria(NodeTypeEnum.Resource, propertiesToMatch, ResourceMetadataData.class);
		return byCriteria;
	}

	@Test
	public void testCreateResourceInstanceTwice() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());
		try {

			String capabilityTypeName = CAPABILITY_2;
			String reqName = "host";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);
			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			ComponentInstance myresourceInstance = buildResourceInstance(softwareComponent.getUniqueId(), "1",
					softwareCompName);

			String serviceName = "myservice.1.0";
			ServiceMetadataData createService = createService(serviceName);
			Either<ComponentInstance, StorageOperationStatus> myinstanceRes1 = resourceInstanceOperation
					.createComponentInstance(serviceName, NodeTypeEnum.Service, "1", myresourceInstance,
							NodeTypeEnum.Resource, true);
			assertTrue("check resource instance was created", myinstanceRes1.isLeft());

			Either<ComponentInstance, StorageOperationStatus> myinstanceRes2 = resourceInstanceOperation
					.createComponentInstance(serviceName, NodeTypeEnum.Service, "1", myresourceInstance,
							NodeTypeEnum.Resource, true);
			assertTrue("check resource instance was not created", myinstanceRes2.isRight());
			assertEquals("check error code", StorageOperationStatus.SCHEMA_VIOLATION, myinstanceRes2.right().value());

			Either<ComponentInstance, StorageOperationStatus> deleteResourceInstance = resourceInstanceOperation
					.deleteComponentInstance(NodeTypeEnum.Service, serviceName,
							myinstanceRes1.left().value().getUniqueId(), true);
			assertTrue("check resource instance was deleted", deleteResourceInstance.isLeft());

			deleteResourceInstance = resourceInstanceOperation.deleteComponentInstance(NodeTypeEnum.Service,
					serviceName, myinstanceRes1.left().value().getUniqueId(), true);
			assertTrue("check resource instance was not deleted", deleteResourceInstance.isRight());
			assertEquals("check resource instance was not deleted", StorageOperationStatus.NOT_FOUND,
					deleteResourceInstance.right().value());

		} finally {
			rollbackAndPrint(false);
			compareGraphSize(numberOfVertices);
		}

	}

	@Test
	public void testConnectResourceInstancesTwice() {

		PrintGraph printGraph1 = new PrintGraph();
		int numberOfVertices = printGraph1.getNumberOfVertices(titanDao.getGraph().left().value());

		try {

			String capabilityTypeName1 = CAPABILITY_1;
			String capabilityTypeName2 = CAPABILITY_2;
			String reqName1 = "host1";
			String reqName2 = "host2";
			String reqNodeName = "tosca.nodes.Compute2" + TEST_CLASS_NUMBER;
			String rootName = "Root2" + TEST_CLASS_NUMBER;
			String softwareCompName = "tosca.nodes.SoftwareComponent2" + TEST_CLASS_NUMBER;
			String computeNodeName = reqNodeName;
			String myResourceVersion = "4.0" + TEST_CLASS_NUMBER;
			String reqRelationship = "myrelationship";

			// Create Capability type
			CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
			capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
			CapabilityTypeDefinition createCapabilityDef1 = capabilityTypeOperationTest
					.createCapability(capabilityTypeName1);
			CapabilityTypeDefinition createCapabilityDef2 = capabilityTypeOperationTest
					.createCapability(capabilityTypeName2);

			ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
			resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

			// create root resource
			Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null,
					true, true);

			ResourceMetadataData resourceData = new ResourceMetadataData();
			resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData,
					ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
					.getResource(rootResource.getUniqueId(), true);

			String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
			log.debug(rootResourceJson);

			// create software component
			Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
					"1.0", rootResource.getName(), true, true);

			resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
			resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
			updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
			assertTrue(updateNode.isLeft());

			// create compute component
			Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName,
					"1.0", rootResource.getName(), true, true);

			// Add capabilities to Compute Resource
			CapabilityDefinition capabilty1 = addCapabilityToResource(capabilityTypeName1, reqName1, computeComponent);
			CapabilityDefinition capabilty2 = addCapabilityToResource(capabilityTypeName2, reqName2, computeComponent);

			// rollbackAndPrint();

			// create requirement definition

			Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = addRequirementToResource(
					capabilityTypeName1, reqName1, reqNodeName, reqRelationship, softwareComponent);

			String parentReqUniqId = addRequirementToResource.left().value().getUniqueId();

			// create my resource derived from software component
			Resource resource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, "my-resource",
					myResourceVersion, softwareComponent.getName(), true, true);

			String serviceId = "myservice.1.0";

			ServiceMetadataData createService = createService(serviceId);
			ComponentInstance myresourceInstance = buildResourceInstance(resource.getUniqueId(), "1", "my-resource");

			ComponentInstance computeInstance = buildResourceInstance(computeComponent.getUniqueId(), "2",
					computeNodeName);

			Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
					.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
							myresourceInstance, NodeTypeEnum.Resource, false);
			assertTrue("check instance added to service", myinstanceRes.isLeft());
			ComponentInstance value1 = myinstanceRes.left().value();
			Either<ComponentInstance, TitanOperationStatus> computeInstTes = resourceInstanceOperation
					.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "2", true,
							computeInstance, NodeTypeEnum.Resource, false);
			assertTrue("check instance added to service", computeInstTes.isLeft());
			ComponentInstance value2 = computeInstTes.left().value();

			RequirementCapabilityRelDef relation = new RequirementCapabilityRelDef();
			String fromResUid = value1.getUniqueId();
			String toResUid = value2.getUniqueId();
			relation.setFromNode(fromResUid);
			relation.setToNode(toResUid);
			List<RequirementAndRelationshipPair> relationships = new ArrayList<RequirementAndRelationshipPair>();
			RequirementAndRelationshipPair immutablePair1 = new RequirementAndRelationshipPair(reqName1, null);
			immutablePair1.setCapabilityUid(capabilty1.getUniqueId());
			immutablePair1.setRequirementUid(parentReqUniqId);
			immutablePair1.setRequirementOwnerId(fromResUid);
			immutablePair1.setCapabilityOwnerId(toResUid);
			relationships.add(immutablePair1);

			relation.setRelationships(relationships);

			Either<RequirementCapabilityRelDef, StorageOperationStatus> connectResourcesInService = resourceInstanceOperation
					.associateResourceInstances(serviceId, NodeTypeEnum.Service, relation, true);
			assertTrue("check association succeed", connectResourcesInService.isLeft());

			relationships.clear();
			RequirementAndRelationshipPair immutablePair2 = new RequirementAndRelationshipPair(reqName2, null);
			immutablePair2.setCapabilityUid(capabilty2.getUniqueId());
			immutablePair2.setRequirementUid(parentReqUniqId);
			relationships.add(immutablePair2);

			RequirementCapabilityRelDef firstRelation = connectResourcesInService.left().value();
			connectResourcesInService = resourceInstanceOperation.associateResourceInstances(serviceId,
					NodeTypeEnum.Service, relation, true);
			assertTrue("check association succeed", connectResourcesInService.isRight());
			assertEquals("check association failed", StorageOperationStatus.MATCH_NOT_FOUND,
					connectResourcesInService.right().value());

			Either<RequirementCapabilityRelDef, StorageOperationStatus> disconnectResourcesInService = resourceInstanceOperation
					.dissociateResourceInstances(serviceId, NodeTypeEnum.Service, firstRelation, true);

			assertTrue("check dissociation succeed", disconnectResourcesInService.isLeft());

			disconnectResourcesInService = resourceInstanceOperation.dissociateResourceInstances(serviceId,
					NodeTypeEnum.Service, relation, true);

			assertTrue("check dissociation failed", disconnectResourcesInService.isRight());
			assertEquals("check association failed", StorageOperationStatus.NOT_FOUND,
					disconnectResourcesInService.right().value());
		} finally {
			rollbackAndPrint();
			compareGraphSize(numberOfVertices);
		}

	}

	private Resource createComputeWithCapability(String capabilityTypeName, String computeNodeName,
			ResourceOperationTest resourceOperationTest, Resource rootResource) {
		// create compute component
		// String id = UniqueIdBuilder.buildResourceUniqueId(computeNodeName,
		// "1.0");
		// if (resourceOperation.getResource(id).isLeft()){
		// resourceOperation.deleteResource(id);
		// }
		Either<List<Resource>, StorageOperationStatus> oldResource = resourceOperation
				.getResourceByNameAndVersion(computeNodeName, "1.0", false);
		if (oldResource.isLeft()) {
			for (Resource old : oldResource.left().value()) {
				if (old.getResourceType().equals(ResourceTypeEnum.VFC)) {
					resourceOperation.deleteResource(old.getUniqueId());
				}
			}

		}

		Resource computeComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, computeNodeName, "1.0",
				rootResource.getName(), true, true);

		// rollbackAndPrint();

		// Add capabilities to Compute Resource
		addCapabilityToResource(capabilityTypeName, "host", computeComponent);
		return resourceOperation.getResource(computeComponent.getUniqueId()).left().value();
	}

	private Resource createSoftwareComponentWithReq(String softwareCompName,
			ResourceOperationTest resourceOperationTest, Resource rootResource, String capabilityTypeName,
			String reqName, String reqRelationship, String reqNodeName) {
		Either<ResourceMetadataData, TitanOperationStatus> updateNode;
		ResourceMetadataData resourceData = new ResourceMetadataData();
		// create software component
		// String id = UniqueIdBuilder.buildResourceUniqueId(softwareCompName,
		// "1.0");
		// if (resourceOperation.getResource(id).isLeft()){
		// resourceOperation.deleteResource(id);
		// }
		Either<List<Resource>, StorageOperationStatus> oldResource = resourceOperation
				.getResourceByNameAndVersion(softwareCompName, "1.0", false);
		if (oldResource.isLeft()) {
			if (oldResource.isLeft()) {
				for (Resource old : oldResource.left().value()) {
					if (old.getResourceType().equals(ResourceTypeEnum.VFC)) {
						resourceOperation.deleteResource(old.getUniqueId());
					}
				}

			}
		}

		Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName,
				"1.0", rootResource.getName(), true, true);

		resourceData.getMetadataDataDefinition().setUniqueId(softwareComponent.getUniqueId());
		resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
		updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
		assertTrue(updateNode.isLeft());

		Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource = addRequirementToResource(
				capabilityTypeName, reqName, reqNodeName, reqRelationship, softwareComponent);

		String parentReqUniqId = addRequirementToResource.left().value().getUniqueId();

		return resourceOperation.getResource(softwareComponent.getUniqueId()).left().value();
	}

	private Resource createRootResource(String rootName, ResourceOperationTest resourceOperationTest) {
		// create root resource
		// String rootId = UniqueIdBuilder.buildResourceUniqueId(rootName,
		// "1.0");
		Either<List<Resource>, StorageOperationStatus> oldResource = resourceOperation
				.getResourceByNameAndVersion(rootName, "1.0", false);
		if (oldResource.isLeft()) {
			for (Resource old : oldResource.left().value()) {
				if (old.getResourceType().equals(ResourceTypeEnum.VFC)) {
					resourceOperation.deleteResource(old.getUniqueId());
				}
			}

		}
		Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "1.0", null, true,
				true);
		ResourceMetadataData rootResourceData = new ResourceMetadataData();
		rootResourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
		rootResourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
		Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(rootResourceData,
				ResourceMetadataData.class);
		assertTrue(updateNode.isLeft());

		Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation
				.getResource(rootResource.getUniqueId(), true);

		String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
		log.debug(rootResourceJson);
		return rootResource;
	}

	public void addResourceInstancesAndRelation(String serviceId) {

		String rootName = "tosca.nodes.test.root";
		String softwareCompName = "tosca.nodes.test.softwarecomponent";
		String capabilityTypeName = "myCapability";
		String reqName = "host";
		String computeNodeName = "tosca.nodes.test.compute";
		String reqRelationship = "myRelationship";

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource rootResource = createRootResource(rootName, resourceOperationTest);
		// Create Capability type
		CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
		capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
		CapabilityTypeDefinition createCapabilityDef = capabilityTypeOperationTest.createCapability(capabilityTypeName);

		Resource softwareComponentResource = createSoftwareComponentWithReq(softwareCompName, resourceOperationTest,
				rootResource, capabilityTypeName, reqName, reqRelationship, computeNodeName);
		Resource compute = createComputeWithCapability(capabilityTypeName, computeNodeName, resourceOperationTest,
				rootResource);

		// resource1
		ComponentInstance myresourceInstance = buildResourceInstance(softwareComponentResource.getUniqueId(), "1",
				"tosca.nodes.test.root");

		Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
						myresourceInstance, NodeTypeEnum.Resource, false);

		assertTrue("check instance added to service", myinstanceRes.isLeft());

		// resource2
		ComponentInstance computeInstance = buildResourceInstance(compute.getUniqueId(), "2",
				"tosca.nodes.test.compute");
		ComponentInstance value1 = myinstanceRes.left().value();

		Either<ComponentInstance, TitanOperationStatus> computeInstTes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "2", true, computeInstance,
						NodeTypeEnum.Resource, false);
		assertTrue("check instance added to service", computeInstTes.isLeft());
		ComponentInstance value2 = computeInstTes.left().value();

		RequirementAndRelationshipPair relationPair = new RequirementAndRelationshipPair();
		relationPair.setRequirement(reqName);
		relationPair.setCapability(capabilityTypeName);

		String capId = "";
		Map<String, List<CapabilityDefinition>> capabilities = compute.getCapabilities();
		for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
			capId = entry.getValue().get(0).getUniqueId();
		}
		relationPair.setCapabilityUid(capId);
		Map<String, List<RequirementDefinition>> requirements = softwareComponentResource.getRequirements();
		String reqId = "";
		for (Map.Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {
			reqId = entry.getValue().get(0).getUniqueId();
		}
		relationPair.setRequirementUid(reqId);
		relationPair.setRequirementOwnerId(value1.getUniqueId());
		relationPair.setCapabilityOwnerId(value2.getUniqueId());
		relationPair.setCapabilityUid(capId);

		Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService = resourceInstanceOperation
				.connectResourcesInService(serviceId, NodeTypeEnum.Service, value1.getUniqueId(), value2.getUniqueId(),
						relationPair);

		assertTrue("check relation created", connectResourcesInService.isLeft());

	}

	@Test
	public void addResourceInstancesResourceDeleted() {

		String rootName = "tosca.nodes.test.root";
		String softwareCompName = "tosca.nodes.test.softwarecomponent";
		String capabilityTypeName = "myCapability";
		String reqName = "host";
		String computeNodeName = "tosca.nodes.test.compute";
		String reqRelationship = "myRelationship";

		ServiceMetadataData origService = createService("myService");
		String serviceId = (String) origService.getUniqueId();

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource rootResource = createRootResource(rootName, resourceOperationTest);
		// Create Capability type
		CapabilityTypeOperationTest capabilityTypeOperationTest = new CapabilityTypeOperationTest();
		capabilityTypeOperationTest.setOperations(titanDao, capabilityTypeOperation);
		capabilityTypeOperationTest.createCapability(capabilityTypeName);

		Resource softwareComponentResource = createSoftwareComponentWithReq(softwareCompName, resourceOperationTest,
				rootResource, capabilityTypeName, reqName, reqRelationship, computeNodeName);

		deleteResource(softwareComponentResource.getUniqueId());

		// resource1
		ComponentInstance myresourceInstance = buildResourceInstance(softwareComponentResource.getUniqueId(), "1",
				"tosca.nodes.test.root");

		Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
						myresourceInstance, NodeTypeEnum.Resource, false);

		assertTrue("check instance not added to service", myinstanceRes.isRight());

	}

	@Test
	public void testDeploymentArtifactsOnRI() {

		String rootName = "tosca.nodes.test.root";

		ServiceMetadataData origService = createService("testDeploymentArtifactsOnRI");
		String serviceId = (String) origService.getUniqueId();

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource rootResource = createRootResource(rootName, resourceOperationTest);
		ArtifactDefinition addArtifactToResource = addArtifactToResource(USER_ID, rootResource.getUniqueId(),
				"myArtifact");

		// resource1
		ComponentInstance myresourceInstance = buildResourceInstance(rootResource.getUniqueId(), "1", rootName);

		Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
						myresourceInstance, NodeTypeEnum.Resource, false);

		assertTrue("check instance added to service", myinstanceRes.isLeft());

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> resourceInstancesOfService = resourceInstanceOperation
				.getComponentInstancesOfComponent(serviceId, NodeTypeEnum.Service, NodeTypeEnum.Resource);
		assertTrue(resourceInstancesOfService.isLeft());
		List<ComponentInstance> resourceInstanceList = resourceInstancesOfService.left().value().left;
		assertTrue(resourceInstanceList.size() == 1);
		ComponentInstance resourceInstance = resourceInstanceList.get(0);
		assertTrue(resourceInstance.getDeploymentArtifacts().size() == 1);
		Map<String, ArtifactDefinition> artifacts = resourceInstance.getDeploymentArtifacts();
		assertNotNull(artifacts.get(addArtifactToResource.getArtifactLabel()));

		ArtifactDefinition heatEnvArtifact = new ArtifactDefinition(addArtifactToResource);
		heatEnvArtifact.setArtifactType("HEAT_ENV");
		heatEnvArtifact.setArtifactLabel(addArtifactToResource.getArtifactLabel() + "env");
		heatEnvArtifact.setUniqueId(null);

		Either<ArtifactDefinition, StorageOperationStatus> either = artifactOperation.addHeatEnvArtifact(
				heatEnvArtifact, addArtifactToResource, resourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance,
				false);
		assertTrue(either.isLeft());

		resourceInstancesOfService = resourceInstanceOperation.getComponentInstancesOfComponent(serviceId,
				NodeTypeEnum.Service, NodeTypeEnum.Resource);
		assertTrue(resourceInstancesOfService.isLeft());
		resourceInstanceList = resourceInstancesOfService.left().value().left;
		assertTrue(resourceInstanceList.size() == 1);
		resourceInstance = resourceInstanceList.get(0);
		assertTrue(resourceInstance.getDeploymentArtifacts().size() == 2);
		artifacts = resourceInstance.getDeploymentArtifacts();
		assertNotNull(artifacts.get(addArtifactToResource.getArtifactLabel()));
		assertNotNull(artifacts.get(addArtifactToResource.getArtifactLabel() + "env"));
		ArtifactDefinition heatEnvFromRI = artifacts.get(addArtifactToResource.getArtifactLabel() + "env");
		assertEquals(addArtifactToResource.getUniqueId(), heatEnvFromRI.getGeneratedFromId());

		List<HeatParameterDefinition> heatParameters = artifacts.get(addArtifactToResource.getArtifactLabel())
				.getHeatParameters();
		assertNotNull(heatParameters);
		assertTrue(heatParameters.size() == 1);

		List<HeatParameterDefinition> heatEnvParameters = heatEnvFromRI.getHeatParameters();
		assertNotNull(heatEnvParameters);
		assertTrue(heatEnvParameters.size() == 1);

		resourceOperation.deleteResource(rootResource.getUniqueId());

	}

	@Test
	public void deleteResourceInstanceWithArtifacts() {
		String rootName = "tosca.nodes.test.root";

		ServiceMetadataData origService = createService("deleteResourceInstanceWithArtifacts");
		String serviceId = (String) origService.getUniqueId();

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource rootResource = createRootResource(rootName, resourceOperationTest);
		ArtifactDefinition addArtifactToResource = addArtifactToResource(USER_ID, rootResource.getUniqueId(),
				"myArtifact");

		// resource1
		ComponentInstance myresourceInstance = buildResourceInstance(rootResource.getUniqueId(), "1", rootName);

		Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
						myresourceInstance, NodeTypeEnum.Resource, false);

		ArtifactDefinition heatEnvArtifact = new ArtifactDefinition(addArtifactToResource);
		heatEnvArtifact.setArtifactType("HEAT_ENV");
		heatEnvArtifact.setArtifactLabel(addArtifactToResource.getArtifactLabel() + "env");
		heatEnvArtifact.setUniqueId(null);

		assertTrue("check instance added to service", myinstanceRes.isLeft());

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> resourceInstancesOfService = resourceInstanceOperation
				.getComponentInstancesOfComponent(serviceId, NodeTypeEnum.Service, NodeTypeEnum.Resource);
		assertTrue(resourceInstancesOfService.isLeft());
		List<ComponentInstance> resourceInstanceList = resourceInstancesOfService.left().value().left;
		assertTrue(resourceInstanceList.size() == 1);
		ComponentInstance resourceInstance = resourceInstanceList.get(0);

		Either<ArtifactDefinition, StorageOperationStatus> either = artifactOperation.addHeatEnvArtifact(
				heatEnvArtifact, addArtifactToResource, resourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance,
				false);
		assertTrue(either.isLeft());
		ArtifactDefinition heatEnvDefinition = either.left().value();

		// delete resource instance
		Either<ComponentInstance, StorageOperationStatus> deleteResourceInstance = resourceInstanceOperation
				.deleteComponentInstance(NodeTypeEnum.Service, serviceId, resourceInstance.getUniqueId());
		assertTrue(deleteResourceInstance.isLeft());

		// check heat env deleted
		ArtifactData artifactData = new ArtifactData();
		Either<ArtifactData, TitanOperationStatus> getDeletedArtifact = titanDao.getNode(artifactData.getUniqueIdKey(),
				heatEnvDefinition.getUniqueId(), ArtifactData.class);
		assertTrue(getDeletedArtifact.isRight());

		// check heat is not deleted
		getDeletedArtifact = titanDao.getNode(artifactData.getUniqueIdKey(), addArtifactToResource.getUniqueId(),
				ArtifactData.class);
		assertTrue(getDeletedArtifact.isLeft());

		HeatParameterData heatParamData = new HeatParameterData();
		Either<HeatParameterData, TitanOperationStatus> heatParamNode = titanDao.getNode(heatParamData.getUniqueIdKey(),
				addArtifactToResource.getHeatParameters().get(0).getUniqueId(), HeatParameterData.class);
		assertTrue(heatParamNode.isLeft());

		resourceOperation.deleteResource(rootResource.getUniqueId());

	}

	@Test
	public void getHeatEnvParams() {
		String rootName = "tosca.nodes.test.root";

		ServiceMetadataData origService = createService("getHeatEnvParams");
		String serviceId = (String) origService.getUniqueId();

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource rootResource = createRootResource(rootName, resourceOperationTest);
		ArtifactDefinition addArtifactToResource = addArtifactToResource(USER_ID, rootResource.getUniqueId(),
				"myArtifact");

		// resource1
		ComponentInstance myresourceInstance = buildResourceInstance(rootResource.getUniqueId(), "1", rootName);

		Either<ComponentInstance, TitanOperationStatus> myinstanceRes = resourceInstanceOperation
				.addComponentInstanceToContainerComponent(serviceId, NodeTypeEnum.Service, "1", true,
						myresourceInstance, NodeTypeEnum.Resource, false);

		ArtifactDefinition heatEnvArtifact = new ArtifactDefinition(addArtifactToResource);
		heatEnvArtifact.setArtifactType("HEAT_ENV");
		heatEnvArtifact.setArtifactLabel(addArtifactToResource.getArtifactLabel() + "env");
		heatEnvArtifact.setUniqueId(null);

		assertTrue("check instance added to service", myinstanceRes.isLeft());

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> resourceInstancesOfService = resourceInstanceOperation
				.getComponentInstancesOfComponent(serviceId, NodeTypeEnum.Service, NodeTypeEnum.Resource);
		assertTrue(resourceInstancesOfService.isLeft());
		List<ComponentInstance> resourceInstanceList = resourceInstancesOfService.left().value().left;
		assertTrue(resourceInstanceList.size() == 1);
		ComponentInstance resourceInstance = resourceInstanceList.get(0);

		Either<ArtifactDefinition, StorageOperationStatus> either = artifactOperation.addHeatEnvArtifact(
				heatEnvArtifact, addArtifactToResource, resourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance,
				false);
		assertTrue(either.isLeft());
		ArtifactDefinition heatEnvDefinition = either.left().value();

		// update value
		String newHeatValue = "123";
		addHeatValueToEnv(heatEnvDefinition.getUniqueId(), addArtifactToResource.getHeatParameters().get(0),
				newHeatValue);

		// check values received

		resourceInstancesOfService = resourceInstanceOperation.getComponentInstancesOfComponent(serviceId,
				NodeTypeEnum.Service, NodeTypeEnum.Resource);
		assertTrue(resourceInstancesOfService.isLeft());
		resourceInstanceList = resourceInstancesOfService.left().value().left;
		assertTrue(resourceInstanceList.size() == 1);
		resourceInstance = resourceInstanceList.get(0);
		assertTrue(resourceInstance.getDeploymentArtifacts().size() == 2);
		Map<String, ArtifactDefinition> artifacts = resourceInstance.getDeploymentArtifacts();
		assertNotNull(artifacts.get(addArtifactToResource.getArtifactLabel()));
		assertNotNull(artifacts.get(addArtifactToResource.getArtifactLabel() + "env"));

		List<HeatParameterDefinition> heatParameters = artifacts.get(addArtifactToResource.getArtifactLabel())
				.getHeatParameters();
		assertNotNull(heatParameters);
		assertTrue(heatParameters.size() == 1);
		HeatParameterDefinition heatParameterTemplate = heatParameters.get(0);

		List<HeatParameterDefinition> heatEnvParameters = artifacts
				.get(addArtifactToResource.getArtifactLabel() + "env").getHeatParameters();
		assertNotNull(heatEnvParameters);
		assertTrue(heatEnvParameters.size() == 1);
		HeatParameterDefinition heatParameterEnv = heatEnvParameters.get(0);

		assertEquals(heatParameterEnv.getDefaultValue(), heatParameterTemplate.getCurrentValue());
		assertEquals(newHeatValue, heatParameterEnv.getCurrentValue());
		assertFalse(newHeatValue.equals(heatParameterTemplate.getCurrentValue()));

		resourceOperation.deleteResource(rootResource.getUniqueId());

	}

	public void addHeatValueToEnv(String artifactId, HeatParameterDefinition heatDefinition, String value) {
		HeatParameterValueData heatValueData = new HeatParameterValueData();
		heatValueData.setValue(value);
		heatValueData.setUniqueId(artifactId + "." + heatDefinition.getName());
		Either<HeatParameterValueData, TitanOperationStatus> createValue = titanDao.createNode(heatValueData,
				HeatParameterValueData.class);
		assertTrue(createValue.isLeft());
		HeatParameterValueData value2 = createValue.left().value();
		HeatParameterData heatParamData = new HeatParameterData(heatDefinition);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanDao.createRelation(value2, heatParamData,
				GraphEdgeLabels.PROPERTY_IMPL, null);
		assertTrue(createRelation.isLeft());
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), heatDefinition.getName());
		Either<GraphRelation, TitanOperationStatus> createRelation2 = titanDao.createRelation(
				new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactId), value2, GraphEdgeLabels.PARAMETER_VALUE, props);
		assertTrue(createRelation2.isLeft());
		titanDao.commit();

	}

	public static String normaliseComponentName(String name) {
		String[] split = splitComponentName(name);
		StringBuffer sb = new StringBuffer();
		for (String splitElement : split) {
			sb.append(splitElement);
		}
		return sb.toString();

	}

	private static String[] splitComponentName(String name) {
		String normalizedName = name.toLowerCase();
		normalizedName = COMPONENT_NAME_DELIMETER_PATTERN.matcher(normalizedName).replaceAll(" ");
		String[] split = normalizedName.split(" ");
		return split;
	}

	public static String normaliseComponentInstanceName(String name) {
		String[] split = splitComponentInstanceName(name);
		StringBuffer sb = new StringBuffer();
		for (String splitElement : split) {
			sb.append(splitElement);
		}
		return sb.toString();

	}

	private static String[] splitComponentInstanceName(String name) {
		String normalizedName = name.toLowerCase();
		normalizedName = COMPONENT_INCTANCE_NAME_DELIMETER_PATTERN.matcher(normalizedName).replaceAll(" ");
		String[] split = normalizedName.split(" ");
		return split;
	}

	private ArtifactDefinition addArtifactToResource(String userId, String resourceId, String artifactName) {
		ArtifactDefinition artifactInfo = new ArtifactDefinition();

		artifactInfo.setArtifactName(artifactName + ".yml");
		artifactInfo.setArtifactType("HEAT");
		artifactInfo.setDescription("hdkfhskdfgh");
		artifactInfo.setArtifactChecksum("UEsDBAoAAAAIAAeLb0bDQz");
		artifactInfo.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

		artifactInfo.setUserIdCreator(userId);
		String fullName = "Jim H";
		artifactInfo.setUpdaterFullName(fullName);
		long time = System.currentTimeMillis();
		artifactInfo.setCreatorFullName(fullName);
		artifactInfo.setCreationDate(time);
		artifactInfo.setLastUpdateDate(time);
		artifactInfo.setUserIdLastUpdater(userId);
		artifactInfo.setArtifactLabel(artifactName);
		artifactInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, artifactInfo.getArtifactLabel()));
		artifactInfo.setEsId(artifactInfo.getUniqueId());

		List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
		HeatParameterDefinition heatParam = new HeatParameterDefinition();
		heatParam.setCurrentValue("11");
		heatParam.setDefaultValue("22");
		heatParam.setDescription("desc");
		heatParam.setName("myParam");
		heatParam.setType("number");
		heatParams.add(heatParam);
		artifactInfo.setHeatParameters(heatParams);

		Either<ArtifactDefinition, StorageOperationStatus> artifact = artifactOperation
				.addArifactToComponent(artifactInfo, resourceId, NodeTypeEnum.Resource, true, true);
		assertTrue(artifact.isLeft());
		return artifact.left().value();
	}
}
