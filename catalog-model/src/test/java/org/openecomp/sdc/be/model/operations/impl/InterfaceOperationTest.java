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

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class InterfaceOperationTest {
	private static Logger log = LoggerFactory.getLogger(InterfaceOperationTest.class.getName());
	private Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	private static String USER_ID = "muUserId";
	private static String CATEGORY_NAME = "category/mycategory";
	// InterfaceLifecycleOperation interfaceOperation = new
	// InterfaceLifecycleOperation();

	// TitanGenericDao titanGenericDao = Mockito.mock(TitanGenericDao.class);
	@javax.annotation.Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@javax.annotation.Resource(name = "interface-operation")
	private InterfaceLifecycleOperation interfaceOperation;

	@javax.annotation.Resource(name = "property-operation")
	private PropertyOperation propertyOperation;

	// @Resource(name = "artifact-operation")
	// private ArtifactOperation artifactOperation;

	@Before
	public void createUserAndCategory() {
		deleteAndCreateCategory(CATEGORY_NAME);
		deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID);
	}

	@BeforeClass
	public static void setupBeforeClass() {
		// ExternalConfiguration.setAppName("catalog-model");
		// String appConfigDir = "src/test/resources/config/catalog-model";
		// ConfigurationSource configurationSource = new
		// FSConfigurationSource(ExternalConfiguration.getChangeListener(),
		// appConfigDir);

		ModelTestBase.init();

	}

	@Test
	public void testDummy() {

		assertTrue(interfaceOperation != null);

	}

/*	@Test
	public void addInterfaceToResourceTest() {

		String capabilityTypeName = "mycapability1";
		String reqName = "host";
		String reqNodeName = "tosca.nodes.Compute1";
		String rootName = "Root100";
		String softwareCompName = "tosca.nodes.SoftwareComponent";
		String computeNodeName = "tosca.nodes.Compute";
		String myResourceVersion = "300.0";
		String reqRelationship = "myrelationship";

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "100.0", null, true, true);

		String interfaceName = "standard";
		InterfaceDefinition interfaceDefinition = buildInterfaceDefinition();

		Operation op = buildOperationDefinition();
		Map<String, Operation> operations = new HashMap<String, Operation>();
		operations.put("Create", op);
		interfaceDefinition.setOperations(operations);

		Either<InterfaceDefinition, StorageOperationStatus> result = interfaceOperation.addInterfaceToResource(interfaceDefinition, rootResource.getUniqueId(), "standard");

		assertTrue(result.isLeft());
		log.debug("{}", result.left().value());

		Either<Resource, StorageOperationStatus> getResourceRes = resourceOperation.getResource(rootResource.getUniqueId());
		assertTrue(getResourceRes.isLeft());
		Resource resourceWithInterface = getResourceRes.left().value();
		Map<String, InterfaceDefinition> interfaces = resourceWithInterface.getInterfaces();
		assertNotNull(interfaces);
		assertFalse(interfaces.isEmpty());
		InterfaceDefinition interfaceDefinition2 = interfaces.get(interfaceName);
		assertNotNull(interfaceDefinition2.getOperations());
		assertFalse(interfaceDefinition2.getOperations().isEmpty());

	}

	@Test
	public void updateInterfaceToResourceTest() {

		String reqName = "host";
		String rootName = "Root200";
		String softwareCompName = "tosca.nodes.SoftwareComponent";

		ResourceOperationTest resourceOperationTest = new ResourceOperationTest();
		resourceOperationTest.setOperations(titanDao, resourceOperation, propertyOperation);

		Resource rootResource = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, rootName, "200.0", null, true, true);

		String interfaceName = "standard";
		InterfaceDefinition interfaceDefinition = buildInterfaceDefinition();

		Operation op = buildOperationDefinition();
		Map<String, Operation> operations = new HashMap<String, Operation>();
		operations.put("create", op);
		interfaceDefinition.setOperations(operations);

		Either<InterfaceDefinition, StorageOperationStatus> result = interfaceOperation.addInterfaceToResource(interfaceDefinition, rootResource.getUniqueId(), "standard");

		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(rootResource.getUniqueId());
		resourceData.getMetadataDataDefinition().setState(LifecycleStateEnum.CERTIFIED.name());
		Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceData, ResourceMetadataData.class);
		assertTrue(updateNode.isLeft());

		Either<Resource, StorageOperationStatus> fetchRootResource = resourceOperation.getResource(rootResource.getUniqueId());

		assertTrue(fetchRootResource.isLeft());
		String rootResourceJson = prettyGson.toJson(fetchRootResource.left().value());
		log.debug(rootResourceJson);

		Resource softwareComponent = resourceOperationTest.createResource(USER_ID, CATEGORY_NAME, softwareCompName, "400.0", rootResource.getName(), true, true);

		assertTrue(result.isLeft());
		log.debug("{}", result.left().value());

		addImplementationToOperation(op);
		// String resourceId, String interfaceName, String
		// operationName,Operation interf

		Either<Operation, StorageOperationStatus> opResult = interfaceOperation.updateInterfaceOperation(softwareComponent.getUniqueId(), "standard", "create", op);
		// PrintGraph pg = new PrintGraph();
		// System.out.println(pg.buildGraphForWebgraphWiz(titanDao.getGraph().left().value()));
		assertTrue(opResult.isLeft());
		log.debug("{}", opResult.left().value());

		Either<Resource, StorageOperationStatus> getResourceRes = resourceOperation.getResource(softwareComponent.getUniqueId());
		assertTrue(getResourceRes.isLeft());
		Resource resourceWithInterface = getResourceRes.left().value();
		Map<String, InterfaceDefinition> interfaces = resourceWithInterface.getInterfaces();
		assertNotNull(interfaces);
		assertFalse(interfaces.isEmpty());
		InterfaceDefinition interfaceDefinition2 = interfaces.get(interfaceName);
		assertNotNull(interfaceDefinition2.getOperations());
		assertFalse(interfaceDefinition2.getOperations().isEmpty());
		Operation operation = interfaceDefinition2.getOperations().get("create");
		assertNotNull(operation);
		assertNotNull(operation.getImplementation());
	}
*/
	private void addImplementationToOperation(Operation op) {
		ArtifactDataDefinition artifactDataDef = new ArtifactDataDefinition();
		artifactDataDef.setArtifactChecksum("YTg2Mjg4MWJhNmI5NzBiNzdDFkMWI=");
		artifactDataDef.setArtifactName("create_myRoot.sh");
		artifactDataDef.setArtifactLabel("create_myRoot");
		artifactDataDef.setArtifactType("SHELL");
		artifactDataDef.setDescription("good description");
		artifactDataDef.setEsId("esId");
		artifactDataDef.setUniqueId(op.getUniqueId() + "." + artifactDataDef.getArtifactLabel());
		ArtifactDefinition artifactDef = new ArtifactDefinition(artifactDataDef, "UEsDBAoAAAAIAAeLb0bDQz");
		op.setImplementation(artifactDef);
	}

	private InterfaceDefinition buildInterfaceDefinition() {
		InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
		interfaceDefinition.setType("tosca.interfaces.standard");
		interfaceDefinition.setCreationDate(new Long(101232));

		return interfaceDefinition;
	}

	private Operation buildOperationDefinition() {
		Operation op = new Operation();
		op.setCreationDate(new Long(101232));
		op.setDescription("asda");

		return op;
	}

	private void deleteAndCreateCategory(String category) {
		String[] names = category.split("/");
		OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanDao);

		/*
		 * CategoryData categoryData = new CategoryData(); categoryData.setName(category);
		 * 
		 * titanDao.deleteNode(categoryData, CategoryData.class); Either<CategoryData, TitanOperationStatus> createNode = titanDao .createNode(categoryData, CategoryData.class); System.out.println("after creating caetgory " + createNode);
		 */

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

}
