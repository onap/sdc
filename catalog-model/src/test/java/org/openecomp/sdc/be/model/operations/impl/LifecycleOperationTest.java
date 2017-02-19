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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.model.operations.impl.util.ResourceCreationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.InterfaceData;
import org.openecomp.sdc.be.resources.data.OperationData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
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
public class LifecycleOperationTest extends ModelTestBase {
	private static Logger log = LoggerFactory.getLogger(LifecycleOperationTest.class.getName());
	private static final String CAPABILITY_HOSTED_ON = "HostedOn";

	private static final String INTERFACE_OPERATION_CREATE = "create";

	private static final String INTERFACE_NAME = "standard";

	private static final String CATEGORY_NAME = "category/mycategory";

	private static final String SERVICE_NAME = "myService";

	private static final String REQUIREMENT_NAME = "requirementName";

	private static final String CAPABILITY_NAME = "capName";

	private static final String USER_ID = "muserId";

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;

	@javax.annotation.Resource
	private ResourceOperation resourceOperation;

	@javax.annotation.Resource
	private ServiceOperation serviceOperation;

	@javax.annotation.Resource
	private LifecycleOperation lifecycleOperation;

	@javax.annotation.Resource
	private CapabilityTypeOperation capabilityTypeOperation;

	@javax.annotation.Resource
	private ArtifactOperation artifactOperation;

	@javax.annotation.Resource
	private InterfaceLifecycleOperation interfaceOperation;

	@javax.annotation.Resource(name = "property-operation")
	private PropertyOperation propertyOperation;

	@javax.annotation.Resource(name = "capability-operation")
	private CapabilityOperation capabilityOperation;

	@javax.annotation.Resource(name = "component-instance-operation")
	private ComponentInstanceOperation resourceInstanceOperation;

	@javax.annotation.Resource(name = "requirement-operation")
	private RequirementOperation requirementOperation;

	User checkoutUser;
	User checkinUser;
	User rfcUser;
	User testerUser;
	User adminUser;

	@Rule
	public TestName name = new TestName();

	@BeforeClass
	public static void initLifecycleOperation() {
		ModelTestBase.init();
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

	}

	@Before
	public void setupBefore() {
		clearGraph();
		UserData modifierData = deleteAndCreateUser(ResourceCreationUtils.MODIFIER_ATT_UID + "co",
				ResourceCreationUtils.MODIFIER_FIRST_NAME, ResourceCreationUtils.MODIFIER_LAST_NAME, "ADMIN");
		checkoutUser = convertUserDataToUser(modifierData);

		modifierData = deleteAndCreateUser(ResourceCreationUtils.MODIFIER_ATT_UID + "ci",
				ResourceCreationUtils.MODIFIER_FIRST_NAME, ResourceCreationUtils.MODIFIER_LAST_NAME, "ADMIN");
		checkinUser = convertUserDataToUser(modifierData);

		modifierData = deleteAndCreateUser(ResourceCreationUtils.MODIFIER_ATT_UID + "rfc",
				ResourceCreationUtils.MODIFIER_FIRST_NAME, ResourceCreationUtils.MODIFIER_LAST_NAME, "ADMIN");
		rfcUser = convertUserDataToUser(modifierData);

		modifierData = deleteAndCreateUser(ResourceCreationUtils.MODIFIER_ATT_UID + "tester",
				ResourceCreationUtils.MODIFIER_FIRST_NAME, ResourceCreationUtils.MODIFIER_LAST_NAME, "TESTER");
		testerUser = convertUserDataToUser(modifierData);

		modifierData = deleteAndCreateUser(ResourceCreationUtils.MODIFIER_ATT_UID + "admin",
				ResourceCreationUtils.MODIFIER_FIRST_NAME, ResourceCreationUtils.MODIFIER_LAST_NAME, "ADMIN");
		adminUser = convertUserDataToUser(modifierData);

		modifierData = deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID, "ADMIN");
		adminUser = convertUserDataToUser(modifierData);

		String[] category = CATEGORY_NAME.split("/");
		OperationTestsUtil.deleteAndCreateServiceCategory(CATEGORY_NAME, titanGenericDao);
		OperationTestsUtil.deleteAndCreateResourceCategory(category[0], category[1], titanGenericDao);

	}

	@After
	public void teardown() {
		clearGraph();
	}

	private void clearGraph() {
		Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
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
		titanGenericDao.commit();
	}

	@Test
	public void getOwnerTest() {

		Resource resultResource = createTestResource(checkoutUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null);

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(resultResource.getUniqueId(), NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();
		assertEquals("check modifier", checkoutUser.getUserId(), resourceOwner.getUserId());

	}

	/*********************** CHECKOUT ***************************************************************/

	@Test
	public void checkoutCertifiedTest() {

		Resource resultResource = createTestResource(adminUser.getUserId(), "1.0", LifecycleStateEnum.CERTIFIED, null);
		String origUniqueId = resultResource.getUniqueId();
		Either<Resource, StorageOperationStatus> origResourceResult = resourceOperation.getResource(origUniqueId);
		Resource origResource = origResourceResult.left().value();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkout
		Either<Resource, StorageOperationStatus> checkoutResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, resultResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkoutResponse.isLeft());
		Resource checkoutResource = checkoutResponse.left().value();

		assertEquals(checkoutResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(checkoutResource.getVersion(), "1.1");
		assertEquals(checkoutResource.getCreatorUserId(), adminUser.getUserId());
		assertEquals(checkoutResource.getLastUpdaterUserId(), checkoutUser.getUserId());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(checkoutResource.getUniqueId(), NodeTypeEnum.Resource, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkoutUser));

		// assert original resource not deleted
		Either<Resource, StorageOperationStatus> getOrigResource = resourceOperation.getResource(origUniqueId);
		assertEquals("check resource created", true, getOrigResource.isLeft());
		// assertEquals("assert original resource not changed", origResource,
		// getOrigResource.left().value());
	}

	@Test
	public void checkoutDefaultTest() {

		Resource resultResource = createTestResource(checkinUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkout
		Either<Resource, StorageOperationStatus> checkoutResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, resultResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkoutResponse.isLeft());
		Resource checkoutResource = checkoutResponse.left().value();

		assertEquals(checkoutResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(checkoutResource.getVersion(), "0.2");
		assertEquals(checkoutResource.getCreatorUserId(), checkinUser.getUserId());
		assertEquals(checkoutResource.getLastUpdaterUserId(), checkoutUser.getUserId());
		assertEquals(checkoutResource.isHighestVersion(), true);

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(checkoutResource.getUniqueId(), NodeTypeEnum.Resource, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkoutUser));

		// assert original resource not deleted
		Either<Resource, StorageOperationStatus> getOrigResource = resourceOperation.getResource(origUniqueId);
		assertEquals("check resource created", true, getOrigResource.isLeft());
		// assertEquals("assert original resource not changed", origResource,
		// getOrigResource.left().value());
		assertEquals("assert original resource not highest version", false,
				getOrigResource.left().value().isHighestVersion());
	}

	@Test
	public void checkoutFullResourceTest() {

		Resource origResource = createFullTestResource(checkinUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		String origUniqueId = origResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkout
		Either<Resource, StorageOperationStatus> checkoutResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, origResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkoutResponse.isLeft());
		Resource checkoutResource = checkoutResponse.left().value();

		assertEquals(checkoutResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(checkoutResource.getVersion(), "0.2");
		assertEquals(checkoutResource.getCreatorUserId(), checkinUser.getUserId());
		assertEquals(checkoutResource.getLastUpdaterUserId(), checkoutUser.getUserId());
		assertEquals(checkoutResource.isHighestVersion(), true);

		assertNotNull(checkoutResource.getArtifacts());
		assertFalse(checkoutResource.getArtifacts().isEmpty());
		assertNotNull(checkoutResource.getInterfaces());
		assertFalse(checkoutResource.getInterfaces().isEmpty());
		Map<String, InterfaceDefinition> interfaces = checkoutResource.getInterfaces();
		assertTrue(interfaces.containsKey(INTERFACE_NAME));
		InterfaceDefinition interfaceDef = interfaces.get(INTERFACE_NAME);
		Map<String, Operation> operations = interfaceDef.getOperations();
		assertNotNull(operations);
		assertFalse(operations.isEmpty());
		assertTrue(operations.containsKey(INTERFACE_OPERATION_CREATE));
		Operation op = operations.get(INTERFACE_OPERATION_CREATE);
		assertNotNull(op.getImplementation());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(checkoutResource.getUniqueId(), NodeTypeEnum.Resource, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkoutUser));

		// assert original resource not deleted
		Either<Resource, StorageOperationStatus> getOrigResource = resourceOperation.getResource(origUniqueId);
		assertEquals("check resource created", true, getOrigResource.isLeft());
		// assertEquals("assert original resource not changed", origResource,
		// getOrigResource.left().value());
		assertEquals("assert original resource not highest version", false,
				getOrigResource.left().value().isHighestVersion());
	}

	@Test
	public void getResourceOwnerResourceNotExistTest() {

		// create resource metadata
		Resource resource = buildResourceMetadata(adminUser.getUserId(), CATEGORY_NAME);
		resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		resource.setUniqueId("my-resource.0.1");

		Either<Resource, StorageOperationStatus> origResourceResult = resourceOperation.getResource("my-resource.0.1");
		assertEquals("assert resource not exist", true, origResourceResult.isRight());

		// get resource owner

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner("my-resource.0.1",
				NodeTypeEnum.Resource, false);

		assertEquals("assert no owner", true, getOwnerResponse.isRight());
		StorageOperationStatus status = getOwnerResponse.right().value();

		assertEquals(StorageOperationStatus.INVALID_ID, status);

	}

	@Test
	public void checkoutResourceTwice() {

		Resource resultResource = createTestResource(adminUser.getUserId(), "1.0", LifecycleStateEnum.CERTIFIED, null);
		String origUniqueId = resultResource.getUniqueId();
		Either<Resource, StorageOperationStatus> origResourceResult = resourceOperation.getResource(origUniqueId);
		Resource origResource = origResourceResult.left().value();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// first checkout
		Either<Resource, StorageOperationStatus> checkoutResponse1 = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, resultResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkoutResponse1.isLeft());

		// second checkout
		Either<Resource, StorageOperationStatus> checkoutResponse2 = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, origResource, checkoutUser, resourceOwner, false);
		assertEquals("check checkout failed", true, checkoutResponse2.isRight());
		assertEquals(StorageOperationStatus.ENTITY_ALREADY_EXISTS, checkoutResponse2.right().value());

	}

	/******** SERVICE */
	@Test
	public void checkoutServiceDefaultTest() {

		Service resultResource = createTestService(checkinUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkout
		Either<? extends Component, StorageOperationStatus> checkoutResponse = lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Service, resultResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkoutResponse.isLeft());
		Component checkoutResource = checkoutResponse.left().value();

		assertEquals(checkoutResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(checkoutResource.getVersion(), "0.2");
		assertEquals(checkoutResource.getCreatorUserId(), checkinUser.getUserId());
		assertEquals(checkoutResource.getLastUpdaterUserId(), checkoutUser.getUserId());
		assertEquals(checkoutResource.isHighestVersion(), true);

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(checkoutResource.getUniqueId(), NodeTypeEnum.Service, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkoutUser));

		// assert original resource not deleted
		Either<Service, StorageOperationStatus> getOrigResource = serviceOperation.getService(origUniqueId);
		assertEquals("check resource created", true, getOrigResource.isLeft());
		// assertEquals("assert original resource not changed", origResource,
		// getOrigResource.left().value());
		assertEquals("assert original resource not highest version", false,
				getOrigResource.left().value().isHighestVersion());
	}

	@Test
	public void checkoutFullServiceTest() {

		Service origService = createTestService(checkinUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);
		String origUniqueId = origService.getUniqueId();

		// add artifacts
		addArtifactToService(checkinUser.getUserId(), origService.getUniqueId(), "install_apache");
		addArtifactToService(checkinUser.getUserId(), origService.getUniqueId(), "start_apache");

		// add resource instances
		ResourceInstanceOperationTest riTest = new ResourceInstanceOperationTest();
		riTest.setOperations(titanGenericDao, capabilityTypeOperation, requirementOperation, capabilityOperation,
				resourceOperation, propertyOperation, resourceInstanceOperation);
		riTest.addResourceInstancesAndRelation(origService.getUniqueId());

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		Either<Service, StorageOperationStatus> serviceBeforeCheckout = serviceOperation.getService(origUniqueId, true);
		assertTrue(serviceBeforeCheckout.isLeft());
		origService = serviceBeforeCheckout.left().value();

		// checkout
		Either<? extends Component, StorageOperationStatus> checkoutResponse = lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Service, origService, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkoutResponse.isLeft());
		Service checkoutResource = (Service) checkoutResponse.left().value();

		assertEquals(checkoutResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(checkoutResource.getVersion(), "0.2");
		assertEquals(checkoutResource.getCreatorUserId(), checkinUser.getUserId());
		assertEquals(checkoutResource.getLastUpdaterUserId(), checkoutUser.getUserId());
		assertEquals(checkoutResource.isHighestVersion(), true);

		assertNotNull(checkoutResource.getArtifacts());
		assertFalse(checkoutResource.getArtifacts().isEmpty());
		assertNotNull(checkoutResource.getComponentInstances());
		assertFalse(checkoutResource.getComponentInstances().isEmpty());
		assertNotNull(checkoutResource.getComponentInstancesRelations());
		assertFalse(checkoutResource.getComponentInstancesRelations().isEmpty());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(checkoutResource.getUniqueId(), NodeTypeEnum.Service, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkoutUser));

		// assert original resource not deleted
		Either<Service, StorageOperationStatus> getOrigResource = serviceOperation.getService(origUniqueId);
		assertEquals("check service created", true, getOrigResource.isLeft());
		// assertEquals("assert original resource not changed", origResource,
		// getOrigResource.left().value());
		assertEquals("assert original service not highest version", false,
				getOrigResource.left().value().isHighestVersion());
	}

	@Test
	public void checkoutServiceTwice() {

		Service resultResource = createTestService(adminUser.getUserId(), "1.0", LifecycleStateEnum.CERTIFIED, null);
		String origUniqueId = resultResource.getUniqueId();
		Either<Service, StorageOperationStatus> origResourceResult = serviceOperation.getService(origUniqueId);
		Service origResource = origResourceResult.left().value();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// first checkout
		Either<? extends Component, StorageOperationStatus> checkoutResponse1 = lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Service, resultResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkoutResponse1.isLeft());

		// second checkout
		Either<? extends Component, StorageOperationStatus> checkoutResponse2 = lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Service, origResource, checkoutUser, resourceOwner, false);
		assertEquals("check checkout failed", true, checkoutResponse2.isRight());
		assertEquals(StorageOperationStatus.ENTITY_ALREADY_EXISTS, checkoutResponse2.right().value());

	}

	/**************************** CHECKIN ********************************************************************/

	@Test
	public void checkinDefaultTest() {

		Resource resultResource = createTestResource(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkin
		Either<Resource, StorageOperationStatus> checkinResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkinComponent(NodeTypeEnum.Resource, resultResource, checkinUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkinResponse.isLeft());
		Resource checkinResource = checkinResponse.left().value();

		assertEquals(checkinResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(checkinResource.getVersion(), "0.1");
		assertEquals(checkinResource.getCreatorUserId(), adminUser.getUserId());
		assertEquals(checkinResource.getLastUpdaterUserId(), checkinUser.getUserId());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(checkinResource.getUniqueId(), NodeTypeEnum.Resource, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkinUser));

	}

	@Test
	public void checkinFromRfcTest() {

		Resource resultResource = createTestResource(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkin
		Either<Resource, StorageOperationStatus> checkinResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkinComponent(NodeTypeEnum.Resource, resultResource, checkinUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, checkinResponse.isLeft());

		// rfc
		Either<Resource, StorageOperationStatus> rfcResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Resource, checkinResponse.left().value(), rfcUser,
						checkinUser, false);
		assertEquals("check resource object is returned", true, checkinResponse.isLeft());

		// checkin (cancel rfc)
		checkinResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkinComponent(NodeTypeEnum.Resource, rfcResponse.left().value(), checkinUser, rfcUser, false);
		assertEquals("check resource object is returned", true, checkinResponse.isLeft());
		resultResource = checkinResponse.left().value();

		assertEquals(resultResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(resultResource.getVersion(), "0.1");
		assertEquals(resultResource.getCreatorUserId(), adminUser.getUserId());
		assertEquals(resultResource.getLastUpdaterUserId(), checkinUser.getUserId());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(resultResource.getUniqueId(), NodeTypeEnum.Resource, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkinUser));

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(resultResource.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

	}

	/*** SERVICE */
	@Test
	public void checkinServiceDefaultTest() {

		Service resultService = createTestService(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null);
		String origUniqueId = resultService.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkin
		Either<? extends Component, StorageOperationStatus> checkinResponse = lifecycleOperation
				.checkinComponent(NodeTypeEnum.Service, resultService, checkinUser, resourceOwner, false);
		assertEquals("check service object is returned", true, checkinResponse.isLeft());
		Service checkinResource = (Service) checkinResponse.left().value();

		assertEquals(checkinResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(checkinResource.getVersion(), "0.1");
		assertEquals(checkinResource.getCreatorUserId(), adminUser.getUserId());
		assertEquals(checkinResource.getLastUpdaterUserId(), checkinUser.getUserId());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(checkinResource.getUniqueId(), NodeTypeEnum.Service, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkinUser));

	}

	@Test
	public void checkinServiceFromRfcTest() {

		Service resultResource = createTestService(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkin
		Either<? extends Component, StorageOperationStatus> checkinResponse = lifecycleOperation
				.checkinComponent(NodeTypeEnum.Service, resultResource, checkinUser, resourceOwner, false);
		assertEquals("check service object is returned", true, checkinResponse.isLeft());

		// rfc
		Either<? extends Component, StorageOperationStatus> rfcResponse = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, checkinResponse.left().value(), rfcUser,
						checkinUser, false);
		assertEquals("check service object is returned", true, checkinResponse.isLeft());

		// checkin (cancel rfc)
		checkinResponse = lifecycleOperation.checkinComponent(NodeTypeEnum.Service, rfcResponse.left().value(),
				checkinUser, rfcUser, false);
		assertEquals("check resource object is returned", true, checkinResponse.isLeft());
		resultResource = (Service) checkinResponse.left().value();

		assertEquals(resultResource.getLifecycleState(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(resultResource.getVersion(), "0.1");
		assertEquals(resultResource.getCreatorUserId(), adminUser.getUserId());
		assertEquals(resultResource.getLastUpdaterUserId(), checkinUser.getUserId());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(resultResource.getUniqueId(), NodeTypeEnum.Service, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(checkinUser));

		// assert relations
		ServiceMetadataDataDefinition metadata = new ServiceMetadataDataDefinition();
		metadata.setUniqueId(resultResource.getUniqueId());
		ServiceMetadataData resourceData = new ServiceMetadataData(metadata);
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

	}

	/****************************
	 * UNDO CHECKOUT
	 ********************************************************************/

	@Test
	public void undoCheckoutNewResourceTest() {

		Resource resultResource = createTestResource(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		//

		// undo checkout
		Either<Resource, StorageOperationStatus> undoCheckoutResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.undoCheckout(NodeTypeEnum.Resource, resultResource, adminUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, undoCheckoutResponse.isLeft());

		Either<Resource, StorageOperationStatus> origResourceResult = resourceOperation.getResource(origUniqueId);
		assertTrue(origResourceResult.isRight());
		/*
		 * assertTrue(origResourceResult.isLeft());
		 * assertTrue(origResourceResult.left().value().getIsDeleted() == true);
		 */
	}

	@Test
	public void undoCheckoutNewFullResourceTest() {

		Resource resultResource = createFullTestResource(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// undo checkout
		Either<Resource, StorageOperationStatus> undoCheckoutResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.undoCheckout(NodeTypeEnum.Resource, resultResource, adminUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, undoCheckoutResponse.isLeft());

		Either<Resource, StorageOperationStatus> origResourceResult = resourceOperation.getResource(origUniqueId);
		/*
		 * assertTrue(origResourceResult.isLeft());
		 * assertTrue(origResourceResult.left().value().getIsDeleted() == true);
		 */ assertTrue(origResourceResult.isRight());

		String interfaceId = origUniqueId + "." + INTERFACE_NAME;
		Either<InterfaceData, TitanOperationStatus> node = titanGenericDao
				.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Interface), interfaceId, InterfaceData.class);
		assertTrue(node.isRight());

		String operationId = interfaceId + "." + INTERFACE_OPERATION_CREATE;
		Either<OperationData, TitanOperationStatus> op = titanGenericDao.getNode(
				UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.InterfaceOperation), operationId, OperationData.class);
		assertTrue(op.isRight());

		String capabilityId = "capability." + origUniqueId + "." + CAPABILITY_NAME;
		Either<CapabilityData, TitanOperationStatus> capability = titanGenericDao
				.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), capabilityId, CapabilityData.class);
		assertTrue(capability.isRight());

		String requirementId = origUniqueId + "." + REQUIREMENT_NAME;
		Either<RequirementData, TitanOperationStatus> req = titanGenericDao.getNode(
				UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Requirement), requirementId, RequirementData.class);
		assertTrue(req.isRight());

	}

	@Test
	public void undoCheckoutExistingResourceTest() {

		Resource resultResource = createTestResource(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(resultResource.getUniqueId(), NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		String prevResourceId = resultResource.getUniqueId();
		Either<Resource, StorageOperationStatus> result2 = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Resource, resultResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource created", true, result2.isLeft());
		Resource resultResource2 = result2.left().value();

		// get resource owner
		getOwnerResponse = lifecycleOperation.getComponentOwner(resultResource2.getUniqueId(), NodeTypeEnum.Resource,
				false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		resourceOwner = getOwnerResponse.left().value();
		assertEquals(resourceOwner, checkoutUser);

		// undo checkout
		Either<Resource, StorageOperationStatus> undoCheckoutResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.undoCheckout(NodeTypeEnum.Resource, resultResource2, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, undoCheckoutResponse.isLeft());

		// get previous resource
		Either<Resource, StorageOperationStatus> resourceAfterUndo = resourceOperation.getResource(prevResourceId);
		assertTrue(resourceAfterUndo.isLeft());
		Resource actualResource = resourceAfterUndo.left().value();
		assertTrue(actualResource.isHighestVersion());
		assertEquals(adminUser.getUserId(), actualResource.getCreatorUserId());
		assertEquals(adminUser.getUserId(), actualResource.getLastUpdaterUserId());
		assertEquals("0.1", actualResource.getVersion());
		assertEquals(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, actualResource.getLifecycleState());

		Either<Resource, StorageOperationStatus> origResourceResult = resourceOperation
				.getResource(resultResource2.getUniqueId());
		/*
		 * assertTrue(origResourceResult.isLeft());
		 * assertTrue(origResourceResult.left().value().getIsDeleted() == true);
		 */ assertTrue(origResourceResult.isRight());

	}

	/**** SERVICE ***/
	@Test
	public void undoCheckoutNewServiceTest() {

		Service resultResource = createTestService(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		//

		// undo checkout
		Either<? extends Component, StorageOperationStatus> undoCheckoutResponse = lifecycleOperation
				.undoCheckout(NodeTypeEnum.Service, resultResource, adminUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, undoCheckoutResponse.isLeft());

		Either<Service, StorageOperationStatus> origResourceResult = serviceOperation.getService(origUniqueId);
		/*
		 * assertTrue(origResourceResult.isLeft());
		 * assertTrue(origResourceResult.left().value().getIsDeleted() == true);
		 */ assertTrue(origResourceResult.isRight());

	}

	@Test
	public void undoCheckoutNewFullServiceTest() {

		Service origService = createTestService(checkinUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);
		String origUniqueId = origService.getUniqueId();

		// add artifacts
		addArtifactToService(checkinUser.getUserId(), origService.getUniqueId(), "install_apache");
		addArtifactToService(checkinUser.getUserId(), origService.getUniqueId(), "start_apache");

		// add resource instances
		ResourceInstanceOperationTest riTest = new ResourceInstanceOperationTest();
		riTest.setOperations(titanGenericDao, capabilityTypeOperation, requirementOperation, capabilityOperation,
				resourceOperation, propertyOperation, resourceInstanceOperation);
		riTest.addResourceInstancesAndRelation(origService.getUniqueId());

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		Either<Service, StorageOperationStatus> service = serviceOperation.getService(origUniqueId);
		assertTrue(service.isLeft());

		Service resultResource = service.left().value();
		List<ComponentInstance> resourceInstances = resultResource.getComponentInstances();

		// undo checkout
		Either<? extends Component, StorageOperationStatus> undoCheckoutResponse = lifecycleOperation
				.undoCheckout(NodeTypeEnum.Service, resultResource, adminUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, undoCheckoutResponse.isLeft());

		Either<Service, StorageOperationStatus> origResourceResult = serviceOperation.getService(origUniqueId);
		/*
		 * assertTrue(origResourceResult.isLeft());
		 * assertTrue(origResourceResult.left().value().getIsDeleted() == true);
		 */ assertTrue(origResourceResult.isRight());

		for (ComponentInstance ri : resourceInstances) {
			Either<ComponentInstanceData, TitanOperationStatus> node = titanGenericDao.getNode(
					UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), ri.getUniqueId(),
					ComponentInstanceData.class);
			assertTrue(node.isRight());
		}

	}

	@Test
	public void undoCheckoutExistingServiceTest() {

		Service resultResource = createTestService(adminUser.getUserId(), "0.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(resultResource.getUniqueId(), NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		String prevResourceId = resultResource.getUniqueId();
		Either<? extends Component, StorageOperationStatus> result2 = lifecycleOperation
				.checkoutComponent(NodeTypeEnum.Service, resultResource, checkoutUser, resourceOwner, false);
		assertEquals("check resource created", true, result2.isLeft());
		Component resultResource2 = result2.left().value();
		String result2Uid = resultResource.getUniqueId();

		// get resource owner
		getOwnerResponse = lifecycleOperation.getComponentOwner(resultResource2.getUniqueId(), NodeTypeEnum.Resource,
				false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		resourceOwner = getOwnerResponse.left().value();
		assertEquals(resourceOwner, checkoutUser);

		// undo checkout
		Either<? extends Component, StorageOperationStatus> undoCheckoutResponse = lifecycleOperation
				.undoCheckout(NodeTypeEnum.Service, resultResource2, checkoutUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, undoCheckoutResponse.isLeft());

		// get previous resource
		Either<Service, StorageOperationStatus> resourceAfterUndo = serviceOperation.getService(prevResourceId);
		assertTrue(resourceAfterUndo.isLeft());
		Service actualResource = resourceAfterUndo.left().value();
		assertTrue(actualResource.isHighestVersion());
		assertEquals(adminUser.getUserId(), actualResource.getCreatorUserId());
		assertEquals(adminUser.getUserId(), actualResource.getLastUpdaterUserId());
		assertEquals("0.1", actualResource.getVersion());
		assertEquals(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, actualResource.getLifecycleState());

		Either<Service, StorageOperationStatus> origResourceResult = serviceOperation.getService(result2Uid);
		/*
		 * assertTrue(origResourceResult.isLeft());
		 * assertTrue(origResourceResult.left().value().getIsDeleted() == true);
		 */ assertTrue(origResourceResult.isRight());

	}

	/****************************
	 * CERTIFICATION REQUEST
	 ********************************************************************/

	@Test
	public void certReqDefaultTest() {
		Resource actualResource = testCertificationRequest(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(actualResource.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(adminUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

	}

	@Test
	public void atomicCheckinCertReqTest() {
		Resource actualResource = testCertificationRequest(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(actualResource.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	private Resource testCertificationRequest(LifecycleStateEnum preState) {

		Resource resultResource = createTestResource(adminUser.getUserId(), "0.1", preState, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkin
		Either<Resource, StorageOperationStatus> certReqResponse = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Resource, resultResource, rfcUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, certReqResponse.isLeft());
		Resource resourceAfterChange = certReqResponse.left().value();

		assertEquals(resourceAfterChange.getLifecycleState(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertEquals(resourceAfterChange.getVersion(), "0.1");
		assertEquals(resourceAfterChange.getCreatorUserId(), adminUser.getUserId());
		assertEquals(resourceAfterChange.getLastUpdaterUserId(), rfcUser.getUserId());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(resourceAfterChange.getUniqueId(), NodeTypeEnum.Resource, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(rfcUser));

		return resourceAfterChange;
	}

	/** SERVICE **/
	@Test
	public void certServiceReqDefaultTest() {
		Service actualResource = testServiceCertificationRequest(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

		// assert relations
		ServiceMetadataDataDefinition metadata = new ServiceMetadataDataDefinition();
		metadata.setUniqueId(actualResource.getUniqueId());
		ServiceMetadataData serviceData = new ServiceMetadataData(metadata);
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(serviceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(serviceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(adminUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

	}

	@Test
	public void atomicServiceCheckinCertReqTest() {
		Service actualResource = testServiceCertificationRequest(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

		// assert relations
		ServiceMetadataDataDefinition metadata = new ServiceMetadataDataDefinition();
		metadata.setUniqueId(actualResource.getUniqueId());
		ServiceMetadataData serviceData = new ServiceMetadataData(metadata);
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(serviceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(serviceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	private Service testServiceCertificationRequest(LifecycleStateEnum preState) {

		Service resultResource = createTestService(adminUser.getUserId(), "0.1", preState, null);
		String origUniqueId = resultResource.getUniqueId();

		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation.getComponentOwner(origUniqueId,
				NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();

		// checkin
		Either<? extends Component, StorageOperationStatus> certReqResponse = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultResource, rfcUser, resourceOwner, false);
		assertEquals("check resource object is returned", true, certReqResponse.isLeft());
		Service resourceAfterChange = (Service) certReqResponse.left().value();

		assertEquals(resourceAfterChange.getLifecycleState(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertEquals(resourceAfterChange.getVersion(), "0.1");
		assertEquals(resourceAfterChange.getCreatorUserId(), adminUser.getUserId());
		assertEquals(resourceAfterChange.getLastUpdaterUserId(), rfcUser.getUserId());

		// assert owner changed
		Either<User, StorageOperationStatus> getOwnerCheckoutResponse = lifecycleOperation
				.getComponentOwner(resourceAfterChange.getUniqueId(), NodeTypeEnum.Service, false);
		assertEquals("check user object is returned", true, getOwnerCheckoutResponse.isLeft());
		resourceOwner = getOwnerCheckoutResponse.left().value();
		assertTrue(resourceOwner.equals(rfcUser));

		return resourceAfterChange;
	}

	/****************************
	 * START CERTIFICATION
	 ********************************************************************/

	@Test
	public void startCertificationTest() {

		Resource resultResource = createTestResource(checkinUser.getUserId(), "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// certification request
		Either<Resource, StorageOperationStatus> requestCertificationResult = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Resource, resultResource, rfcUser, checkinUser, false);
		assertTrue(requestCertificationResult.isLeft());

		// start certification
		Either<Resource, StorageOperationStatus> startCertificationResult = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Resource, resultResource, testerUser, rfcUser, false);

		assertEquals(true, startCertificationResult.isLeft());
		Resource actualResource = startCertificationResult.left().value();

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(actualResource.getUniqueId(), NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();
		assertEquals(testerUser.getUserId(), resourceOwner.getUserId());

		assertTrue(actualResource.isHighestVersion());
		assertEquals(checkinUser.getUserId(), actualResource.getCreatorUserId());
		assertEquals(testerUser.getUserId(), actualResource.getLastUpdaterUserId());
		assertEquals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, actualResource.getLifecycleState());

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(actualResource.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(testerUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	/** SERVICE */
	@Test
	public void startServiceCertificationTest() {

		Service resultResource = createTestService(checkinUser.getUserId(), "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// certification request
		Either<? extends Component, StorageOperationStatus> requestCertificationResult = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultResource, rfcUser, checkinUser, false);
		assertTrue(requestCertificationResult.isLeft());

		// start certification
		Either<? extends Component, StorageOperationStatus> startCertificationResult = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultResource, testerUser, rfcUser, false);

		assertEquals(true, startCertificationResult.isLeft());
		Service actualResource = (Service) startCertificationResult.left().value();

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(actualResource.getUniqueId(), NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();
		assertEquals(testerUser.getUserId(), resourceOwner.getUserId());

		assertTrue(actualResource.isHighestVersion());
		assertEquals(checkinUser.getUserId(), actualResource.getCreatorUserId());
		assertEquals(testerUser.getUserId(), actualResource.getLastUpdaterUserId());
		assertEquals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, actualResource.getLifecycleState());

		// assert relations
		ServiceMetadataDataDefinition metadata = new ServiceMetadataDataDefinition();
		metadata.setUniqueId(actualResource.getUniqueId());
		ServiceMetadataData serviceData = new ServiceMetadataData(metadata);
		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(serviceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(testerUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(serviceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(serviceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	/****************************
	 * FAIL CERTIFICATION
	 ********************************************************************/

	@Test
	public void failCertificationTest() {

		Resource actualResource = certificationStatusChange(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, checkinUser);

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(actualResource.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		// old edges removed
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		// new state is checkin
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	/*** SERVICE **/

	@Test
	public void failCertificationServiceTest() {

		Service actualService = certificationStatusChangeService(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, checkinUser);

		// assert relations
		ServiceMetadataData resourceData = new ServiceMetadataData((ServiceMetadataDataDefinition) actualService
				.getComponentMetadataDefinition().getMetadataDataDefinition());
		Map<String, Object> props = new HashMap<String, Object>();

		// old edges removed
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		// new state is checkin
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	/****************************
	 * CANCEL CERTIFICATION
	 ********************************************************************/

	@Test
	public void cancelCertificationTest() {

		Resource actualResource = certificationStatusChange(LifecycleStateEnum.READY_FOR_CERTIFICATION, rfcUser);

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(actualResource.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		// old edges removed
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		// new state is rfc
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	/** SERVICE **/
	@Test
	public void cancelCertificationServiceTest() {

		Service actualService = certificationStatusChangeService(LifecycleStateEnum.READY_FOR_CERTIFICATION, rfcUser);

		// assert relations
		ServiceMetadataData ServiceNode = new ServiceMetadataData();
		ServiceNode.getMetadataDataDefinition().setUniqueId(actualService.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		// old edges removed
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(ServiceNode, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(ServiceNode,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(checkinUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		// new state is rfc
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(ServiceNode, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());
	}

	/**************************** CERTIFY ********************************************************************/

	@Test
	public void certifyTest() {

		Resource resultResource = createTestResource(checkinUser.getUserId(), "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// certification request
		Either<Resource, StorageOperationStatus> requestCertificationResult = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Resource, resultResource, rfcUser, checkinUser, false);
		assertTrue(requestCertificationResult.isLeft());

		// start certification
		Either<Resource, StorageOperationStatus> startCertificationResult = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Resource, resultResource, testerUser, rfcUser, false);
		assertEquals(true, startCertificationResult.isLeft());
		Resource actualResource = startCertificationResult.left().value();

		// cancel certification
		Either<? extends Component, StorageOperationStatus> CertificationResult = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Resource, actualResource, testerUser, testerUser, false);

		assertEquals(true, CertificationResult.isLeft());
		actualResource = (Resource) CertificationResult.left().value();

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(actualResource.getUniqueId(), NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();
		assertEquals(testerUser.getUserId(), resourceOwner.getUserId());

		assertTrue(actualResource.isHighestVersion());
		assertEquals(checkinUser.getUserId(), actualResource.getCreatorUserId());
		assertEquals(testerUser.getUserId(), actualResource.getLastUpdaterUserId());
		assertEquals(LifecycleStateEnum.CERTIFIED, actualResource.getLifecycleState());

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(actualResource.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		// old edges removed
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		// new state is certified
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(testerUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

	}

	/******** SERVICE **/

	@Test
	public void certifyServiceTest() {

		Service resultService = createTestService(checkinUser.getUserId(), "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// certification request
		Either<? extends Component, StorageOperationStatus> requestCertificationResult = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService, rfcUser, checkinUser, false);
		assertTrue(requestCertificationResult.isLeft());

		// start certification
		Either<? extends Component, StorageOperationStatus> startCertificationResult = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService, testerUser, rfcUser, false);
		assertEquals(true, startCertificationResult.isLeft());
		Service actualService = (Service) startCertificationResult.left().value();

		// cancel certification
		Either<? extends Component, StorageOperationStatus> CertificationResult = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Service, actualService, testerUser, testerUser, false);

		assertEquals(true, CertificationResult.isLeft());
		actualService = (Service) CertificationResult.left().value();

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(actualService.getUniqueId(), NodeTypeEnum.Service, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();
		assertEquals(testerUser.getUserId(), resourceOwner.getUserId());

		assertTrue(actualService.isHighestVersion());
		assertEquals(checkinUser.getUserId(), actualService.getCreatorUserId());
		assertEquals(testerUser.getUserId(), actualService.getLastUpdaterUserId());
		assertEquals(LifecycleStateEnum.CERTIFIED, actualService.getLifecycleState());

		// assert relations
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(actualService.getUniqueId());
		Map<String, Object> props = new HashMap<String, Object>();

		// old edges removed
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<GraphRelation, TitanOperationStatus> incomingRelationByCriteria = titanGenericDao
				.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isRight());

		// new state is certified
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData, GraphEdgeLabels.STATE,
				props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(testerUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		incomingRelationByCriteria = titanGenericDao.getIncomingRelationByCriteria(resourceData,
				GraphEdgeLabels.LAST_STATE, props);
		assertTrue(incomingRelationByCriteria.isLeft());
		assertEquals(rfcUser.getUserId(), incomingRelationByCriteria.left().value().getFrom().getIdValue());

	}

	@Test
	public void testDeleteOldVersionsResource() {
		// simulate
		createTestResource(checkinUser.getUserId(), "1.0", LifecycleStateEnum.CERTIFIED, null);
		Resource resourceNewVersion = createTestResource(checkinUser.getUserId(), "1.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);
		createTestResource(checkinUser.getUserId(), "1.2", LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				resourceNewVersion.getUUID());
		createTestResource(checkinUser.getUserId(), "1.3", LifecycleStateEnum.CERTIFICATION_IN_PROGRESS,
				resourceNewVersion.getUUID());
		Resource certifiedResource = createTestResource(checkinUser.getUserId(), "2.0", LifecycleStateEnum.CERTIFIED,
				resourceNewVersion.getUUID());

		Either<Boolean, StorageOperationStatus> deleteOldComponentVersions = lifecycleOperation
				.deleteOldComponentVersions(NodeTypeEnum.Resource, certifiedResource.getName(),
						certifiedResource.getUUID(), false);

		assertTrue(deleteOldComponentVersions.isLeft());

		String resourceName = certifiedResource.getName();
		Either<List<Resource>, StorageOperationStatus> resource = resourceOperation
				.getResourceByNameAndVersion(resourceName, "1.0", false);
		assertTrue(resource.isLeft());

		resource = resourceOperation.getResourceByNameAndVersion(resourceName, "2.0", false);
		assertTrue(resource.isLeft());

		resource = resourceOperation.getResourceByNameAndVersion(resourceName, "1.1", false);
		assertTrue(resource.isLeft());
		assertTrue(resource.left().value().size() == 1);
		Resource deleted = resource.left().value().get(0);
		assertTrue(deleted.getIsDeleted());
		// assertEquals(StorageOperationStatus.NOT_FOUND,
		// resource.right().value());

		resource = resourceOperation.getResourceByNameAndVersion(resourceName, "1.2", false);
		// assertTrue(resource.isRight());
		// assertEquals(StorageOperationStatus.NOT_FOUND,
		// resource.right().value());
		assertTrue(resource.isLeft());
		assertTrue(resource.left().value().size() == 1);
		deleted = resource.left().value().get(0);
		assertTrue(deleted.getIsDeleted());

		resource = resourceOperation.getResourceByNameAndVersion(resourceName, "1.3", false);
		// assertTrue(resource.isRight());
		// assertEquals(StorageOperationStatus.NOT_FOUND,
		// resource.right().value());
		assertTrue(resource.isLeft());
		assertTrue(resource.left().value().size() == 1);
		deleted = resource.left().value().get(0);
		assertTrue(deleted.getIsDeleted());
	}
	
	@Test
	public void testDeleteOldVersionsService() {
		// simulate
		createTestService(checkinUser.getUserId(), "1.0", LifecycleStateEnum.CERTIFIED, null);
		Service serviceNewUUid = createTestService(checkinUser.getUserId(), "1.1",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);
		createTestService(checkinUser.getUserId(), "1.2", LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				serviceNewUUid.getUUID());
		createTestService(checkinUser.getUserId(), "1.3", LifecycleStateEnum.CERTIFICATION_IN_PROGRESS,
				serviceNewUUid.getUUID());
		Service certifiedService = createTestService(checkinUser.getUserId(), "2.0", LifecycleStateEnum.CERTIFIED,
				serviceNewUUid.getUUID());

		Either<Boolean, StorageOperationStatus> deleteOldComponentVersions = lifecycleOperation
				.deleteOldComponentVersions(NodeTypeEnum.Service, certifiedService.getName(),
						certifiedService.getUUID(), false);

		assertTrue(deleteOldComponentVersions.isLeft());

		String resourceName = certifiedService.getName();
		Either<Service, StorageOperationStatus> service = serviceOperation.getServiceByNameAndVersion(resourceName,
				"1.0", null, false);
		assertTrue(service.isLeft());

		service = serviceOperation.getServiceByNameAndVersion(resourceName, "2.0", null, false);
		assertTrue(service.isLeft());

		service = serviceOperation.getServiceByNameAndVersion(resourceName, "1.1", null, false);
		/*
		 * assertTrue(resource.isRight());
		 * assertEquals(StorageOperationStatus.NOT_FOUND,
		 * resource.right().value());
		 */
		assertTrue(service.isLeft());
		assertTrue(service.left().value().getIsDeleted());

		service = serviceOperation.getServiceByNameAndVersion(resourceName, "1.2", null, false);

		service = serviceOperation.getServiceByNameAndVersion(resourceName, "1.3", null, false);
		/*
		 * assertTrue(service.isRight());
		 * assertEquals(StorageOperationStatus.NOT_FOUND,
		 * service.right().value());
		 */
		assertTrue(service.isLeft());
		assertTrue(service.left().value().getIsDeleted());

		service = serviceOperation.getServiceByNameAndVersion(resourceName, "1.3", null, false);
		/*
		 * assertTrue(service.isRight());
		 * assertEquals(StorageOperationStatus.NOT_FOUND,
		 * service.right().value());
		 */
		assertTrue(service.isLeft());
		assertTrue(service.left().value().getIsDeleted());

	}

	private Resource certificationStatusChange(LifecycleStateEnum nextState, User expectedOwner) {
		Resource resultResource = createTestResource(checkinUser.getUserId(), "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// certification request
		Either<Resource, StorageOperationStatus> requestCertificationResult = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Resource, resultResource, rfcUser, checkinUser, false);
		assertTrue(requestCertificationResult.isLeft());

		// start certification
		Either<Resource, StorageOperationStatus> startCertificationResult = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Resource, resultResource, testerUser, rfcUser, false);
		assertEquals(true, startCertificationResult.isLeft());
		Resource actualResource = startCertificationResult.left().value();

		// cancel certification
		Either<Resource, StorageOperationStatus> failCertificationResult = (Either<Resource, StorageOperationStatus>) lifecycleOperation
				.cancelOrFailCertification(NodeTypeEnum.Resource, actualResource, testerUser, testerUser, nextState,
						false);

		assertEquals(true, failCertificationResult.isLeft());
		actualResource = failCertificationResult.left().value();

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(actualResource.getUniqueId(), NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();
		assertEquals(expectedOwner, resourceOwner);

		assertTrue(actualResource.isHighestVersion());
		assertEquals(checkinUser.getUserId(), actualResource.getCreatorUserId());
		assertEquals(testerUser.getUserId(), actualResource.getLastUpdaterUserId());
		assertEquals(nextState, actualResource.getLifecycleState());
		return actualResource;
	}

	private Service certificationStatusChangeService(LifecycleStateEnum nextState, User expectedOwner) {
		Service resultService = createTestService(checkinUser.getUserId(), "0.2",
				LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, null);

		// certification request
		Either<? extends Component, StorageOperationStatus> requestCertificationResult = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Service, resultService, rfcUser, checkinUser, false);
		assertTrue(requestCertificationResult.isLeft());

		// start certification
		Either<? extends Component, StorageOperationStatus> startCertificationResult = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Service, resultService, testerUser, rfcUser, false);
		assertEquals(true, startCertificationResult.isLeft());
		Service actualService = (Service) startCertificationResult.left().value();

		// cancel certification
		Either<? extends Component, StorageOperationStatus> failCertificationResult = lifecycleOperation
				.cancelOrFailCertification(NodeTypeEnum.Service, actualService, testerUser, testerUser, nextState,
						false);

		assertEquals(true, failCertificationResult.isLeft());
		actualService = (Service) failCertificationResult.left().value();

		// get resource owner
		Either<User, StorageOperationStatus> getOwnerResponse = lifecycleOperation
				.getComponentOwner(actualService.getUniqueId(), NodeTypeEnum.Resource, false);

		assertEquals("check user object is returned", true, getOwnerResponse.isLeft());
		User resourceOwner = getOwnerResponse.left().value();
		assertEquals(expectedOwner, resourceOwner);

		assertTrue(actualService.isHighestVersion());
		assertEquals(checkinUser.getUserId(), actualService.getCreatorUserId());
		assertEquals(testerUser.getUserId(), actualService.getLastUpdaterUserId());
		assertEquals(nextState, actualService.getLifecycleState());
		return actualService;
	}

	private Resource createTestResource(String userId, String version, LifecycleStateEnum state, String uuid) {
		// create resource in graph

		Resource resource2 = buildResourceMetadata(userId, CATEGORY_NAME);
		resource2.setVersion(version);
		;
		resource2.setLifecycleState(state);
		resource2.setUUID(uuid);

		Either<Resource, StorageOperationStatus> result = resourceOperation.createResource(resource2);
		assertEquals("check resource created", true, result.isLeft());
		Resource resultResource = result.left().value();
		return resultResource;
	}

	private Service createTestService(String userId, String version, LifecycleStateEnum state, String uuid) {
		// create resource in graph

		Service service = new Service();
		service.setName(SERVICE_NAME);
		service.setVersion(version);
		service.setDescription("description 1");
		service.setCreatorUserId(userId);
		service.setContactId("contactId@sdc.com");
		CategoryDefinition category = new CategoryDefinition();
		category.setName(CATEGORY_NAME);

		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(category);
		service.setCategories(categories);
		service.setIcon("images/my.png");
		List<String> tags = new ArrayList<String>();
		tags.add("TAG1");
		tags.add("TAG2");
		service.setTags(tags);
		service.setUUID(uuid);

		service.setLifecycleState(state);

		Either<Service, StorageOperationStatus> result = serviceOperation.createService(service);
		assertEquals("check service created", true, result.isLeft());
		Service resultResource = result.left().value();
		return resultResource;
	}

	private Resource createFullTestResource(String userId, String version, LifecycleStateEnum state) {

		Resource resource2 = buildResourceMetadata(userId, CATEGORY_NAME);
		resource2.setVersion(version);
		;
		resource2.setLifecycleState(state);

		InterfaceDefinition inter = new InterfaceDefinition(INTERFACE_NAME, "interface description", null);

		Operation operation = new Operation();
		operation.setDescription("op description");
		operation.setUniqueId(inter.getUniqueId() + "." + INTERFACE_OPERATION_CREATE);

		ArtifactDataDefinition artifactDataDef = new ArtifactDataDefinition();
		artifactDataDef.setArtifactChecksum("YTg2Mjg4MWJhNmI5NzBiNzdDFkMWI=");
		artifactDataDef.setArtifactName("create_myRoot.sh");
		artifactDataDef.setArtifactLabel("create_myRoot");
		artifactDataDef.setArtifactType("SHELL");
		artifactDataDef.setDescription("good description");
		artifactDataDef.setEsId("esId");
		artifactDataDef.setUniqueId(operation.getUniqueId() + "." + artifactDataDef.getArtifactLabel());
		ArtifactDefinition artifactDef = new ArtifactDefinition(artifactDataDef, "UEsDBAoAAAAIAAeLb0bDQz");

		operation.setImplementation(artifactDef);
		operation.setCreationDate(System.currentTimeMillis());
		Map<String, Operation> ops = new HashMap<>();
		ops.put(INTERFACE_OPERATION_CREATE, operation);
		inter.setOperations(ops);

		Map<String, InterfaceDefinition> interfaces = new HashMap<>();
		interfaces.put(INTERFACE_NAME, inter);

		resource2.setInterfaces(interfaces);

		String capabilityTypeName = CAPABILITY_HOSTED_ON;
		createCapabilityOnGraph(capabilityTypeName);

		// create capability definition
		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		capabilityDefinition.setDescription("my capability");
		capabilityDefinition.setType(capabilityTypeName);
		capabilityDefinition.setName(CAPABILITY_NAME);
		Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
		List<String> validSourceTypes = new ArrayList<String>();
		validSourceTypes.add("tosca.nodes.SC");
		capabilityDefinition.setValidSourceTypes(validSourceTypes);
		List<CapabilityDefinition> caplist = new ArrayList<CapabilityDefinition>();
		caplist.add(capabilityDefinition);
		capabilities.put(capabilityTypeName, caplist);
		resource2.setCapabilities(capabilities);

		// add requirement definition
		RequirementDefinition reqDefinition = new RequirementDefinition();
		// reqDefinition.setNode(reqNodeName);
		// reqDefinition.setRelationship(reqRelationship);

		reqDefinition.setCapability(capabilityTypeName);
		reqDefinition.setName(REQUIREMENT_NAME);
		Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
		List<RequirementDefinition> reqlist = new ArrayList<RequirementDefinition>();
		reqlist.add(reqDefinition);
		requirements.put(capabilityTypeName, reqlist);
		resource2.setRequirements(requirements);

		Either<Resource, StorageOperationStatus> result = resourceOperation.createResource(resource2);
		assertEquals("check resource created", true, result.isLeft());
		Resource resultResource = result.left().value();

		// add artifacts to resource
		// ArtifactDataDefinition artifactDataDef = new
		// ArtifactDataDefinition();
		artifactDataDef.setArtifactChecksum("YTg2Mjg4MWJhNmI5NzBiNzdDFkMWI=");
		artifactDataDef.setArtifactName("create_myRoot.sh");
		artifactDataDef.setArtifactLabel("create_myRoot");
		artifactDataDef.setArtifactType("SHELL");
		artifactDataDef.setDescription("good description");
		artifactDataDef.setEsId("esId");
		artifactDataDef.setUniqueId(resultResource.getUniqueId() + "." + artifactDataDef.getArtifactLabel());
		artifactDef = new ArtifactDefinition(artifactDataDef, "UEsDBAoAAAAIAAeLb0bDQz");
		// artifacts.put("myArtifact", artifactDef);
		// resource2.setArtifacts(artifacts);

		Either<ArtifactDefinition, StorageOperationStatus> addArifactToResource = artifactOperation
				.addArifactToComponent(artifactDef, resultResource.getUniqueId(), NodeTypeEnum.Resource, false, true);
		assertTrue(addArifactToResource.isLeft());

		Either<Resource, StorageOperationStatus> resource = resourceOperation.getResource(resultResource.getUniqueId());
		assertTrue(resource.isLeft());

		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String json = prettyGson.toJson(resource.left().value());
		log.debug(json);
		return resource.left().value();
	}

	private void createCapabilityOnGraph(String capabilityTypeName) {

		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
		capabilityTypeDefinition.setDescription("desc1");
		capabilityTypeDefinition.setType(capabilityTypeName);
		Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
		String propName1 = "disk_size";
		PropertyDefinition property1 = buildProperty1();
		properties.put(propName1, property1);
		capabilityTypeDefinition.setProperties(properties);

		Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation
				.addCapabilityType(capabilityTypeDefinition);
		assertTrue(addCapabilityType1.isLeft());
	}

	private User convertUserDataToUser(UserData modifierData) {
		User modifier = new User();
		modifier.setUserId(modifierData.getUserId());
		modifier.setEmail(modifierData.getEmail());
		modifier.setFirstName(modifierData.getFirstName());
		modifier.setLastName(modifierData.getLastName());
		modifier.setRole(modifierData.getRole());
		return modifier;
	}

	private Resource buildResourceMetadata(String userId, String category) {
		// deleteAndCreateCategory(category);

		Resource resource = new Resource();
		resource.setName("my-resource");
		resource.setVersion("1.0");
		;
		resource.setDescription("description 1");
		resource.setAbstract(false);
		resource.setCreatorUserId(userId);
		resource.setContactId("contactId@sdc.com");
		resource.setVendorName("vendor 1");
		resource.setVendorRelease("1.0.0");
		String[] categoryArr = category.split("/");
		resource.addCategory(categoryArr[0], categoryArr[1]);
		resource.setIcon("images/my.png");
		List<String> tags = new ArrayList<String>();
		tags.add("TAG1");
		tags.add("TAG2");
		resource.setTags(tags);
		return resource;
	}

	public UserData deleteAndCreateUser(String userId, String firstName, String lastName, String role) {
		UserData userData = new UserData();
		userData.setUserId(userId);
		userData.setFirstName(firstName);
		userData.setLastName(lastName);
		userData.setRole(role);

		titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
		titanGenericDao.createNode(userData, UserData.class);
		titanGenericDao.commit();

		return userData;
	}
	
	private PropertyDefinition buildProperty1() {
		PropertyDefinition property1 = new PropertyDefinition();
		property1.setDefaultValue("10");
		property1.setDescription(
				"Size of the local disk, in Gigabytes (GB), available to applications running on the Compute node.");
		property1.setType(ToscaType.INTEGER.name().toLowerCase());
		List<PropertyConstraint> constraints = new ArrayList<PropertyConstraint>();
		GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
		constraints.add(propertyConstraint1);

		LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
		constraints.add(propertyConstraint2);

		property1.setConstraints(constraints);
		return property1;
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

}
