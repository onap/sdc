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

package org.openecomp.sdc.be.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.AdditionalInfoParameterData;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.GraphNodeLock;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.google.gson.Gson;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.core.schema.TitanManagement;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class })
public class TitanGenericDaoTest {
	private static Logger log = LoggerFactory.getLogger(TitanGenericDaoTest.class.getName());
	private static ConfigurationManager configurationManager;

	@Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@BeforeClass
	public static void setupBeforeClass() {
		ExternalConfiguration.setAppName("catalog-dao");
		String appConfigDir = "src/test/resources/config/catalog-dao";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		configurationManager = new ConfigurationManager(configurationSource);
		configurationManager.getConfiguration()
				.setTitanCfgFile("../catalog-be/src/main/resources/config/titan.properties");
		configurationManager.getConfiguration().setTitanInMemoryGraph(true);
	}

	// @Test
	public void testcheckEdgeProps() {
		TitanGraph graph = titanDao.getGraph().left().value();
		TitanVertex v1 = graph.addVertex();
		v1.property("prop1", 123);
		TitanVertex v2 = graph.addVertex();
		v2.property("prop1", 456);
		TitanEdge addEdge = v1.addEdge("label11", v2);
		addEdge.property("edgeProp", "my prop edge");
		graph.tx().commit();

		Either<TitanVertex, TitanOperationStatus> v11 = titanDao.getVertexByProperty("prop1", 123);
		Iterator<Edge> edges = v11.left().value().edges(Direction.OUT, "label11");
		Edge edge = edges.next();
		// String value = (String)edge.value("edgeProp");
		String value = (String) titanDao.getProperty(edge, "edgeProp");
		log.debug(value);

	}

	@Test
	public void testCrudNode() {

		String id = "userId12345abc";
		UserData userData = new UserData("Myname123", "Mylastname", id, "email123", "Tester",
				UserStatusEnum.ACTIVE.name(), null);

		Either<UserData, TitanOperationStatus> newNode = titanDao.createNode(userData, UserData.class);

		assertTrue(newNode.isLeft());

		log.debug("{}", newNode.left().value());

		titanDao.commit();

		ImmutablePair<String, Object> keyValueId = userData.getKeyValueId();
		Either<UserData, TitanOperationStatus> node = titanDao.getNode(keyValueId.getKey(), keyValueId.getValue(),
				UserData.class);
		titanDao.commit();
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());

		userData.setRole("Designer");
		node = titanDao.updateNode(userData, UserData.class);
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());
		assertEquals(null, "Designer", node.left().value().getRole());
		titanDao.commit();

		node = titanDao.deleteNode(userData, UserData.class);
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());
		titanDao.commit();

		node = titanDao.getNode(keyValueId.getKey(), keyValueId.getValue(), UserData.class);
		assertTrue(node.isRight());
		log.debug("{}", node.right().value());

	}

	@Test
	public void testGetByCategoryAndAll() {

		// create 2 nodes
		String id = "userId12345abc";
		UserData userData1 = new UserData("Myname123", "Mylastname", id, "email123", "Tester",
				UserStatusEnum.ACTIVE.name(), null);

		Either<UserData, TitanOperationStatus> node1 = titanDao.createNode(userData1, UserData.class);
		assertTrue(node1.isLeft());
		log.debug("{}", node1.left().value());

		id = "userIddfkoer45abc";
		UserData userData2 = new UserData("Mynadyhme123", "Mylasghtname", id, "emaighdl123", "Designer",
				UserStatusEnum.ACTIVE.name(), null);
		Either<UserData, TitanOperationStatus> node2 = titanDao.createNode(userData2, UserData.class);
		assertTrue(node2.isLeft());
		log.debug("{}", node2.left().value());

		titanDao.commit();

		ImmutablePair<String, Object> keyValueId1 = userData1.getKeyValueId();
		// get first node
		Either<UserData, TitanOperationStatus> node = titanDao.getNode(keyValueId1.getKey(), keyValueId1.getValue(),
				UserData.class);
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());
		titanDao.commit();

		// get all must be 2 + 1 default user = 3
		Either<List<UserData>, TitanOperationStatus> all = titanDao.getAll(NodeTypeEnum.User, UserData.class);
		assertTrue(all.isLeft());
		assertTrue(all.left().value().size() > 0);

		log.debug("{}", all.left().value());

		Map<String, Object> props = new HashMap<String, Object>();

		props.put(keyValueId1.getKey(), keyValueId1.getValue());

		// get by criteria. must be 1
		Either<List<UserData>, TitanOperationStatus> byCriteria = titanDao.getByCriteria(NodeTypeEnum.User, props,
				UserData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals(1, byCriteria.left().value().size());

		log.debug("{}", byCriteria.left().value());

		// delete all nodes
		node = titanDao.deleteNode(userData1, UserData.class);
		assertTrue(node.isLeft());
		node = titanDao.deleteNode(userData2, UserData.class);
		assertTrue(node.isLeft());
	}

	@Test
	public void testGetEdgesForNode() {
		String id = "userId12345abc";
		UserData userData = new UserData("Myname123", "Mylastname", id, "email123", UserRoleEnum.ADMIN.name(),
				UserStatusEnum.ACTIVE.name(), null);
		titanDao.createNode(userData, UserData.class);
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setName("resourceForLock");
		resourceData.getMetadataDataDefinition().setVersion("0.1");
		resourceData.getMetadataDataDefinition().setState("newState");
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		titanDao.createNode(resourceData, ResourceMetadataData.class);
		titanDao.createRelation(userData, resourceData, GraphEdgeLabels.LAST_MODIFIER, null);
		titanDao.commit();

		Either<List<Edge>, TitanOperationStatus> eitherEdges = titanDao.getEdgesForNode(userData, Direction.OUT);
		assertTrue(eitherEdges.isLeft());
		assertTrue(eitherEdges.left().value().size() == 1);

		eitherEdges = titanDao.getEdgesForNode(userData, Direction.IN);
		assertTrue(eitherEdges.isLeft());
		assertTrue(eitherEdges.left().value().size() == 0);

		eitherEdges = titanDao.getEdgesForNode(resourceData, Direction.OUT);
		assertTrue(eitherEdges.isLeft());
		assertTrue(eitherEdges.left().value().size() == 0);

		eitherEdges = titanDao.getEdgesForNode(resourceData, Direction.IN);
		assertTrue(eitherEdges.isLeft());
		assertTrue(eitherEdges.left().value().size() == 1);

		eitherEdges = titanDao.getEdgesForNode(resourceData, Direction.BOTH);
		assertTrue(eitherEdges.isLeft());
		assertTrue(eitherEdges.left().value().size() == 1);

		eitherEdges = titanDao.getEdgesForNode(userData, Direction.BOTH);
		assertTrue(eitherEdges.isLeft());
		assertTrue(eitherEdges.left().value().size() == 1);

		titanDao.deleteNode(userData, UserData.class);
		titanDao.deleteNode(resourceData, ResourceMetadataData.class);
		titanDao.commit();
	}

	@Test
	public void testLockElement() {
		
		ResourceMetadataData resourceData = new ResourceMetadataData();

		resourceData.getMetadataDataDefinition().setName("resourceForLock");
		resourceData.getMetadataDataDefinition().setVersion("0.1");
		resourceData.getMetadataDataDefinition().setState("newState");
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		Either<ResourceMetadataData, TitanOperationStatus> resource1 = titanDao.createNode(resourceData,
				ResourceMetadataData.class);
		assertTrue(resource1.isLeft());
		titanDao.commit();
		String lockId = "lock_" + resourceData.getLabel() + "_" + resource1.left().value().getUniqueId();

		Either<GraphNodeLock, TitanOperationStatus> nodeLock = titanDao
				.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, nodeLock.right().value());

		TitanOperationStatus status = titanDao.lockElement(resourceData);
		assertEquals(TitanOperationStatus.OK, status);

		nodeLock = titanDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isLeft());
		assertEquals(lockId, nodeLock.left().value().getUniqueId());

		titanDao.commit();

		status = titanDao.lockElement(resourceData);
		assertEquals(TitanOperationStatus.ALREADY_LOCKED, status);

		status = titanDao.releaseElement(resourceData);
		assertEquals(TitanOperationStatus.OK, status);

		nodeLock = titanDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, nodeLock.right().value());
		titanDao.deleteNode(resourceData, ResourceMetadataData.class);
		titanDao.commit();

	}

	@Test
	public void testReLockElement() throws InterruptedException {
		
		ResourceMetadataData resourceData = new ResourceMetadataData();

		resourceData.getMetadataDataDefinition().setName("resourceForReLock");
		resourceData.getMetadataDataDefinition().setVersion("0.1");
		resourceData.getMetadataDataDefinition().setState("newState");
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		Either<ResourceMetadataData, TitanOperationStatus> resource1 = titanDao.createNode(resourceData,
				ResourceMetadataData.class);
		assertTrue(resource1.isLeft());
		titanDao.commit();
		String lockId = "lock_" + resourceData.getLabel() + "_" + resource1.left().value().getUniqueId();

		Either<GraphNodeLock, TitanOperationStatus> nodeLock = titanDao
				.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, nodeLock.right().value());

		// lock
		TitanOperationStatus status = titanDao.lockElement(resourceData);
		assertEquals(TitanOperationStatus.OK, status);

		nodeLock = titanDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isLeft());
		assertEquals(lockId, nodeLock.left().value().getUniqueId());
		long time1 = nodeLock.left().value().getTime();

		titanDao.commit();

		// timeout
		configurationManager.getConfiguration().setTitanLockTimeout(2L);
		Thread.sleep(5001);

		// relock
		status = titanDao.lockElement(resourceData);
		assertEquals(TitanOperationStatus.OK, status);

		nodeLock = titanDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isLeft());
		assertEquals(lockId, nodeLock.left().value().getUniqueId());

		long time2 = nodeLock.left().value().getTime();

		assertTrue(time2 > time1);

		status = titanDao.releaseElement(resourceData);
		assertEquals(TitanOperationStatus.OK, status);

		nodeLock = titanDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, nodeLock.right().value());

		titanDao.deleteNode(resourceData, ResourceMetadataData.class);
		titanDao.commit();

	}

	@Test
	public void testBoolean() {
		ResourceMetadataData resourceData = new ResourceMetadataData();

		resourceData.getMetadataDataDefinition().setName("resourceForLock");
		resourceData.getMetadataDataDefinition().setVersion("0.1");
		resourceData.getMetadataDataDefinition().setState("NOT_CERTIFIED_CHECKOUT");
		resourceData.getMetadataDataDefinition().setHighestVersion(true);
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		Either<ResourceMetadataData, TitanOperationStatus> resource1 = titanDao.createNode(resourceData,
				ResourceMetadataData.class);
		assertTrue(resource1.isLeft());

		resourceData = new ResourceMetadataData();

		resourceData.getMetadataDataDefinition().setName("resourceForLock");
		resourceData.getMetadataDataDefinition().setVersion("0.2");
		resourceData.getMetadataDataDefinition().setState("NOT_CERTIFIED_CHECKOUT");
		resourceData.getMetadataDataDefinition().setHighestVersion(false);
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		Either<ResourceMetadataData, TitanOperationStatus> resource2 = titanDao.createNode(resourceData,
				ResourceMetadataData.class);
		titanDao.commit();

		Map<String, Object> props = new HashMap<String, Object>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), "NOT_CERTIFIED_CHECKOUT");
		props.put("name", "resourceForLock");
		props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), false);

		// get by criteria. must be 1
		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = titanDao
				.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());

		titanDao.deleteNode(resource1.left().value(), ResourceMetadataData.class);

		titanDao.deleteNode(resource2.left().value(), ResourceMetadataData.class);
		titanDao.commit();
	}

	// @Test
	public void testStringSearch() {
		TitanGraph graph;

		BaseConfiguration conf = new BaseConfiguration();
		conf.setProperty("storage.backend", "inmemory");
		graph = TitanFactory.open(conf);

		// TitanManagement graphMgt = graph.getManagementSystem();
		TitanManagement graphMgt = graph.openManagement();
		PropertyKey propKey = graphMgt.makePropertyKey("string1").dataType(String.class).make();
		graphMgt.buildIndex("string1", Vertex.class).addKey(propKey).unique().buildCompositeIndex();

		propKey = graphMgt.makePropertyKey("string2").dataType(String.class).make();

		// graphMgt.buildIndex("string2", Vertex.class).addKey(propKey,
		// Mapping.TEXT.getParameter()).buildMixedIndex("search");
		graphMgt.buildIndex("string2", Vertex.class).addKey(propKey).unique().buildCompositeIndex();
		graphMgt.commit();

		// TitanVertex v = graph.addVertex();
		// v.addProperty("string1", "My new String 1");
		// v.addProperty("string2", "String11");
		// graph.commit();
		//
		// v = graph.addVertex();
		// v.addProperty("string1", "my new string 1");
		// v.addProperty("string2", "string11");
		// graph.commit();
		//
		// System.out.println("First index search - case");
		//
		// Iterable<Vertex> vertices = graph.getVertices("string1", "My new
		// String 1");
		// Iterator<Vertex> iter = vertices.iterator();
		// while ( iter.hasNext() ){
		// Vertex ver = iter.next();
		// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
		// }
		// System.out.println("First index search non case");
		//
		// vertices = graph.getVertices("string1", "my new string 1");
		// iter = vertices.iterator();
		// while ( iter.hasNext() ){
		// Vertex ver = iter.next();
		// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
		// }
		// System.out.println("Second index search case");
		//
		// vertices = graph.getVertices("string2", "String11");
		// iter = vertices.iterator();
		// while ( iter.hasNext() ){
		// Vertex ver = iter.next();
		// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
		// }
		// System.out.println("second index search non case");
		//
		// vertices = graph.getVertices("string2", "string11");
		// iter = vertices.iterator();
		// while ( iter.hasNext() ){
		// Vertex ver = iter.next();
		// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
		// }
		// System.out.println("Query index search case");
		// vertices = graph.query().has("string1", "My new String
		// 1").vertices();
		// iter = vertices.iterator();
		// while ( iter.hasNext() ){
		// Vertex ver = iter.next();
		// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
		// }
		// System.out.println("Query index search non case");
		// vertices = graph.query().has("string1", "my new string
		// 1").vertices();
		// iter = vertices.iterator();
		// while ( iter.hasNext() ){
		// Vertex ver = iter.next();
		// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
		// }

		log.debug("**** predicat index search non case");
		Iterable<TitanVertex> vertices = graph.query().has("string1", Text.REGEX, "my new string 1").vertices();
		Iterator<TitanVertex> iter = vertices.iterator();
		while (iter.hasNext()) {
			Vertex ver = iter.next();
			// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
			log.debug("{}", titanDao.getProperties(ver));
		}

	}

	@Test
	public void testDuplicateResultDueToTitanBug() {

		ResourceMetadataData resourceData1 = new ResourceMetadataData();
		resourceData1.getMetadataDataDefinition().setUniqueId("A");
		((ResourceMetadataDataDefinition) resourceData1.getMetadataDataDefinition()).setAbstract(true);
		resourceData1.getMetadataDataDefinition().setName("aaaa");

		Either<ResourceMetadataData, TitanOperationStatus> newNode1 = titanDao.createNode(resourceData1,
				ResourceMetadataData.class);
		assertTrue(newNode1.isLeft());
		log.debug("{}", newNode1.left().value());
		// titanDao.commit();

		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), true);
		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = titanDao
				.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals("check one result returned", 1, byCriteria.left().value().size());
		// titanDao.commit();

		ResourceMetadataData resourceToUpdate = new ResourceMetadataData();
		((ResourceMetadataDataDefinition) resourceToUpdate.getMetadataDataDefinition()).setAbstract(false);
		resourceToUpdate.getMetadataDataDefinition().setUniqueId("A");
		Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceToUpdate,
				ResourceMetadataData.class);
		assertTrue(updateNode.isLeft());
		// titanDao.commit();

		byCriteria = titanDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isRight());
		assertEquals("check one result returned due to titan bug", TitanOperationStatus.NOT_FOUND,
				byCriteria.right().value());

		AdditionalInfoParameterData infoParameterData = new AdditionalInfoParameterData();
		infoParameterData.getAdditionalInfoParameterDataDefinition().setUniqueId("123");
		Map<String, String> idToKey = new HashMap<>();
		idToKey.put("key1", "value1");
		infoParameterData.setIdToKey(idToKey);

		Either<AdditionalInfoParameterData, TitanOperationStatus> newNode2 = titanDao.createNode(infoParameterData,
				AdditionalInfoParameterData.class);
		assertTrue(newNode2.isLeft());
		log.debug("{}", newNode2.left().value());
		// titanDao.commit();

		Map<String, String> idToKey2 = new HashMap<>();
		idToKey2.put("key1", "value2");

		Map<String, Object> props2 = new HashMap<>();
		props2.put(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), "123");
		Gson gson = new Gson();
		props2.put(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY.getProperty(), gson.toJson(idToKey2));
		// props2.put(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY.getProperty(),
		// idToKey2);

		Either<List<AdditionalInfoParameterData>, TitanOperationStatus> byCriteria2 = titanDao
				.getByCriteria(NodeTypeEnum.AdditionalInfoParameters, props2, AdditionalInfoParameterData.class);
		assertTrue(byCriteria2.isRight());
		assertEquals("check one result returned due to titan bug", TitanOperationStatus.NOT_FOUND,
				byCriteria2.right().value());

		infoParameterData.setIdToKey(idToKey2);

		Either<AdditionalInfoParameterData, TitanOperationStatus> updateNode2 = titanDao.updateNode(infoParameterData,
				AdditionalInfoParameterData.class);
		assertTrue(updateNode2.isLeft());
		// titanDao.commit();

		props2.put(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY.getProperty(), idToKey);
		byCriteria2 = titanDao.getByCriteria(NodeTypeEnum.AdditionalInfoParameters, props2,
				AdditionalInfoParameterData.class);
		assertTrue(byCriteria2.isRight());
		assertEquals("check one result returned due to titan bug", TitanOperationStatus.NOT_FOUND,
				byCriteria2.right().value());

		ComponentInstanceData resourceInstanceData = new ComponentInstanceData();
		resourceInstanceData.getComponentInstDataDefinition().setUniqueId("ri123");
		resourceInstanceData.getComponentInstDataDefinition().setPosX("22");
		resourceInstanceData.getComponentInstDataDefinition().setName("myresource_1");

		Either<ComponentInstanceData, TitanOperationStatus> newNode3 = titanDao.createNode(resourceInstanceData,
				ComponentInstanceData.class);
		assertTrue(newNode3.isLeft());
		log.debug("{}", newNode3.left().value());
		// titanDao.commit();

		resourceInstanceData.getComponentInstDataDefinition().setPosX("50");
		Either<ComponentInstanceData, TitanOperationStatus> updateNode3 = titanDao.updateNode(resourceInstanceData,
				ComponentInstanceData.class);
		assertTrue(updateNode3.isLeft());
		// titanDao.commit();

		resourceInstanceData.getComponentInstDataDefinition().setName("myresource_2");
		updateNode3 = titanDao.updateNode(resourceInstanceData, ComponentInstanceData.class);
		assertTrue(updateNode3.isLeft());
		// titanDao.commit();

		Map<String, Object> props3 = new HashMap<>();
		props3.put("positionX", "22");
		Either<List<ComponentInstanceData>, TitanOperationStatus> byCriteria3 = titanDao
				.getByCriteria(NodeTypeEnum.ResourceInstance, props3, ComponentInstanceData.class);
		assertTrue(byCriteria3.isRight());
		assertEquals("check one result returned due to titan bug", TitanOperationStatus.NOT_FOUND,
				byCriteria3.right().value());

		props3.put("positionX", "50");
		byCriteria3 = titanDao.getByCriteria(NodeTypeEnum.ResourceInstance, props3, ComponentInstanceData.class);
		assertTrue(byCriteria3.isLeft());

		/////////////////////////// check integer ////////////////////////

		ArtifactData artifactData = new ArtifactData();
		artifactData.getArtifactDataDefinition().setUniqueId("ad234");
		artifactData.getArtifactDataDefinition().setTimeout(100);

		Either<ArtifactData, TitanOperationStatus> newNode4 = titanDao.createNode(artifactData, ArtifactData.class);
		assertTrue(newNode4.isLeft());
		log.debug("{}", newNode4.left().value());
		// titanDao.commit();

		artifactData.getArtifactDataDefinition().setTimeout(50);
		Either<ArtifactData, TitanOperationStatus> updateNode4 = titanDao.updateNode(artifactData, ArtifactData.class);
		assertTrue(updateNode4.isLeft());
		// titanDao.commit();

		Map<String, Object> props4 = new HashMap<>();
		props4.put("timeout", 100);
		Either<List<ArtifactData>, TitanOperationStatus> byCriteria4 = titanDao.getByCriteria(NodeTypeEnum.ArtifactRef,
				props4, ArtifactData.class);
		assertTrue(byCriteria4.isRight());
		assertEquals("check one result returned due to titan bug", TitanOperationStatus.NOT_FOUND,
				byCriteria4.right().value());

		props4.put("timeout", 50);
		byCriteria4 = titanDao.getByCriteria(NodeTypeEnum.ArtifactRef, props4, ArtifactData.class);
		assertTrue(byCriteria4.isLeft());

		titanDao.rollback();
	}

	@Test
	public void testDuplicateResultUSeHasNotQueryDueToTitanBug() {
		
		String name = "bbbb";

		ResourceMetadataData resourceData1 = new ResourceMetadataData();
		resourceData1.getMetadataDataDefinition().setUniqueId("A");
		((ResourceMetadataDataDefinition) resourceData1.getMetadataDataDefinition()).setAbstract(true);
		resourceData1.getMetadataDataDefinition().setName(name);

		Either<ResourceMetadataData, TitanOperationStatus> newNode1 = titanDao.createNode(resourceData1,
				ResourceMetadataData.class);
		assertTrue(newNode1.isLeft());
		log.debug("{}", newNode1.left().value());
		// titanDao.commit();

		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), true);
		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = titanDao
				.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals("check one result returned", 1, byCriteria.left().value().size());
		// titanDao.commit();

		ResourceMetadataData resourceToUpdate = new ResourceMetadataData();
		((ResourceMetadataDataDefinition) resourceToUpdate.getMetadataDataDefinition()).setAbstract(false);
		resourceToUpdate.getMetadataDataDefinition().setUniqueId("A");
		Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanDao.updateNode(resourceToUpdate,
				ResourceMetadataData.class);
		assertTrue(updateNode.isLeft());
		// titanDao.commit();

		// no result where isAbstract = true
		byCriteria = titanDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isRight());
		assertEquals("check one result returned due to titan bug", TitanOperationStatus.NOT_FOUND,
				byCriteria.right().value());

		// one result where isAbstract != true
		byCriteria = titanDao.getByCriteria(NodeTypeEnum.Resource, null, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals("check one result returned", 1, byCriteria.left().value().size());

		props.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), false);
		byCriteria = titanDao.getByCriteria(NodeTypeEnum.Resource, null, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isRight());
		assertEquals("check one result returned due to titan bug", TitanOperationStatus.NOT_FOUND,
				byCriteria.right().value());

		titanDao.rollback();

	}
	
}
