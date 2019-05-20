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

import com.google.gson.Gson;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.JanusGraphEdge;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.attribute.Text;
import org.janusgraph.core.schema.JanusGraphManagement;
import fj.data.Either;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.*;
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

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class })
public class JanusGraphGenericDaoTest {
	private static Logger log = LoggerFactory.getLogger(JanusGraphGenericDaoTest.class.getName());
	private static ConfigurationManager configurationManager;

	@Resource(name = "janusgraph-generic-dao")
	private JanusGraphGenericDao janusGraphDao;

	@BeforeClass
	public static void setupBeforeClass() {
		ExternalConfiguration.setAppName("catalog-dao");
		String appConfigDir = "src/test/resources/config/catalog-dao";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		configurationManager = new ConfigurationManager(configurationSource);
		configurationManager.getConfiguration().setTitanCfgFile("../catalog-be/src/main/resources/config/janusgraph.properties");
		configurationManager.getConfiguration().setTitanInMemoryGraph(true);
	}

	// @Test
	public void testcheckEdgeProps() {
		JanusGraph graph = janusGraphDao.getGraph().left().value();
		JanusGraphVertex v1 = graph.addVertex();
		v1.property("prop1", 123);
		JanusGraphVertex v2 = graph.addVertex();
		v2.property("prop1", 456);
		JanusGraphEdge addEdge = v1.addEdge("label11", v2);
		addEdge.property("edgeProp", "my prop edge");
		graph.tx().commit();

		Either<JanusGraphVertex, JanusGraphOperationStatus> v11 = janusGraphDao.getVertexByProperty("prop1", 123);
		Iterator<Edge> edges = v11.left().value().edges(Direction.OUT, "label11");
		Edge edge = edges.next();
		// String value = (String)edge.value("edgeProp");
		String value = (String) janusGraphDao.getProperty(edge, "edgeProp");
		log.debug(value);

	}

	@Test
	public void testCrudNode() {

		String id = "userId12345abc";
		UserData userData = new UserData("Myname123", "Mylastname", id, "email123", "Tester",
				UserStatusEnum.ACTIVE.name(), null);

		Either<UserData, JanusGraphOperationStatus> newNode = janusGraphDao.createNode(userData, UserData.class);

		assertTrue(newNode.isLeft());

		log.debug("{}", newNode.left().value());

		janusGraphDao.commit();

		ImmutablePair<String, Object> keyValueId = userData.getKeyValueId();
		Either<UserData, JanusGraphOperationStatus> node = janusGraphDao.getNode(keyValueId.getKey(), keyValueId.getValue(),
				UserData.class);
		janusGraphDao.commit();
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());

		userData.setRole("Designer");
		node = janusGraphDao.updateNode(userData, UserData.class);
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());
		assertEquals(null, "Designer", node.left().value().getRole());
		janusGraphDao.commit();

		node = janusGraphDao.deleteNode(userData, UserData.class);
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());
		janusGraphDao.commit();

		node = janusGraphDao.getNode(keyValueId.getKey(), keyValueId.getValue(), UserData.class);
		assertTrue(node.isRight());
		log.debug("{}", node.right().value());

	}

	@Test
	public void testGetByCategoryAndAll() {

		// create 2 nodes
		String id = "userId12345abc";
		UserData userData1 = new UserData("Myname123", "Mylastname", id, "email123", "Tester",
				UserStatusEnum.ACTIVE.name(), null);

		Either<UserData, JanusGraphOperationStatus> node1 = janusGraphDao.createNode(userData1, UserData.class);
		assertTrue(node1.isLeft());
		log.debug("{}", node1.left().value());

		id = "userIddfkoer45abc";
		UserData userData2 = new UserData("Mynadyhme123", "Mylasghtname", id, "emaighdl123", "Designer",
				UserStatusEnum.ACTIVE.name(), null);
		Either<UserData, JanusGraphOperationStatus> node2 = janusGraphDao.createNode(userData2, UserData.class);
		assertTrue(node2.isLeft());
		log.debug("{}", node2.left().value());

		janusGraphDao.commit();

		ImmutablePair<String, Object> keyValueId1 = userData1.getKeyValueId();
		// get first node
		Either<UserData, JanusGraphOperationStatus> node = janusGraphDao.getNode(keyValueId1.getKey(), keyValueId1.getValue(),
				UserData.class);
		assertTrue(node.isLeft());
		log.debug("{}", node.left().value());
		janusGraphDao.commit();

		// get all must be 2 + 1 default user = 3
		Either<List<UserData>, JanusGraphOperationStatus> all = janusGraphDao.getAll(NodeTypeEnum.User, UserData.class);
		assertTrue(all.isLeft());
		assertTrue(all.left().value().size() > 0);

		log.debug("{}", all.left().value());

		Map<String, Object> props = new HashMap<>();

		props.put(keyValueId1.getKey(), keyValueId1.getValue());

		// get by criteria. must be 1
		Either<List<UserData>, JanusGraphOperationStatus> byCriteria = janusGraphDao.getByCriteria(NodeTypeEnum.User, props,
				UserData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals(1, byCriteria.left().value().size());

		log.debug("{}", byCriteria.left().value());

		// delete all nodes
		node = janusGraphDao.deleteNode(userData1, UserData.class);
		assertTrue(node.isLeft());
		node = janusGraphDao.deleteNode(userData2, UserData.class);
		assertTrue(node.isLeft());
	}

	@Test
	public void testGetEdgesForNode() {
		String id = "userId12345abc";
		UserData userData = new UserData("Myname123", "Mylastname", id, "email123", UserRoleEnum.ADMIN.name(),
				UserStatusEnum.ACTIVE.name(), null);
		janusGraphDao.createNode(userData, UserData.class);
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setName("resourceForLock");
		resourceData.getMetadataDataDefinition().setVersion("0.1");
		resourceData.getMetadataDataDefinition().setState("newState");
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		janusGraphDao.createNode(resourceData, ResourceMetadataData.class);
		janusGraphDao.createRelation(userData, resourceData, GraphEdgeLabels.LAST_MODIFIER, null);
		janusGraphDao.commit();

		Either<List<Edge>, JanusGraphOperationStatus> eitherEdges = janusGraphDao.getEdgesForNode(userData, Direction.OUT);
		assertTrue(eitherEdges.isLeft());
        assertEquals(1, eitherEdges.left().value().size());

		eitherEdges = janusGraphDao.getEdgesForNode(userData, Direction.IN);
		assertTrue(eitherEdges.isLeft());
        assertEquals(0, eitherEdges.left().value().size());

		eitherEdges = janusGraphDao.getEdgesForNode(resourceData, Direction.OUT);
		assertTrue(eitherEdges.isLeft());
        assertEquals(0, eitherEdges.left().value().size());

		eitherEdges = janusGraphDao.getEdgesForNode(resourceData, Direction.IN);
		assertTrue(eitherEdges.isLeft());
        assertEquals(1, eitherEdges.left().value().size());

		eitherEdges = janusGraphDao.getEdgesForNode(resourceData, Direction.BOTH);
		assertTrue(eitherEdges.isLeft());
        assertEquals(1, eitherEdges.left().value().size());

		eitherEdges = janusGraphDao.getEdgesForNode(userData, Direction.BOTH);
		assertTrue(eitherEdges.isLeft());
        assertEquals(1, eitherEdges.left().value().size());

		janusGraphDao.deleteNode(userData, UserData.class);
		janusGraphDao.deleteNode(resourceData, ResourceMetadataData.class);
		janusGraphDao.commit();
	}

	@Test
	public void testLockElement() {
		
		ResourceMetadataData resourceData = new ResourceMetadataData();

		resourceData.getMetadataDataDefinition().setName("resourceForLock");
		resourceData.getMetadataDataDefinition().setVersion("0.1");
		resourceData.getMetadataDataDefinition().setState("newState");
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		Either<ResourceMetadataData, JanusGraphOperationStatus> resource1 = janusGraphDao.createNode(resourceData,
				ResourceMetadataData.class);
		assertTrue(resource1.isLeft());
		janusGraphDao.commit();
		String lockId = "lock_" + resourceData.getLabel() + "_" + resource1.left().value().getUniqueId();

		Either<GraphNodeLock, JanusGraphOperationStatus> nodeLock = janusGraphDao
				.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(JanusGraphOperationStatus.NOT_FOUND, nodeLock.right().value());

		JanusGraphOperationStatus status = janusGraphDao.lockElement(resourceData);
		assertEquals(JanusGraphOperationStatus.OK, status);

		nodeLock = janusGraphDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isLeft());
		assertEquals(lockId, nodeLock.left().value().getUniqueId());

		janusGraphDao.commit();

		status = janusGraphDao.lockElement(resourceData);
		assertEquals(JanusGraphOperationStatus.ALREADY_LOCKED, status);

		status = janusGraphDao.releaseElement(resourceData);
		assertEquals(JanusGraphOperationStatus.OK, status);

		nodeLock = janusGraphDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(JanusGraphOperationStatus.NOT_FOUND, nodeLock.right().value());
		janusGraphDao.deleteNode(resourceData, ResourceMetadataData.class);
		janusGraphDao.commit();

	}

	@Test
	public void testReLockElement() throws InterruptedException {
		
		ResourceMetadataData resourceData = new ResourceMetadataData();

		resourceData.getMetadataDataDefinition().setName("resourceForReLock");
		resourceData.getMetadataDataDefinition().setVersion("0.1");
		resourceData.getMetadataDataDefinition().setState("newState");
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		Either<ResourceMetadataData, JanusGraphOperationStatus> resource1 = janusGraphDao.createNode(resourceData,
				ResourceMetadataData.class);
		assertTrue(resource1.isLeft());
		janusGraphDao.commit();
		String lockId = "lock_" + resourceData.getLabel() + "_" + resource1.left().value().getUniqueId();

		Either<GraphNodeLock, JanusGraphOperationStatus> nodeLock = janusGraphDao
				.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(JanusGraphOperationStatus.NOT_FOUND, nodeLock.right().value());

		// lock
		JanusGraphOperationStatus status = janusGraphDao.lockElement(resourceData);
		assertEquals(JanusGraphOperationStatus.OK, status);

		nodeLock = janusGraphDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isLeft());
		assertEquals(lockId, nodeLock.left().value().getUniqueId());
		long time1 = nodeLock.left().value().getTime();

		janusGraphDao.commit();

		// timeout
		configurationManager.getConfiguration().setTitanLockTimeout(2L);
		Thread.sleep(5001);

		// relock
		status = janusGraphDao.lockElement(resourceData);
		assertEquals(JanusGraphOperationStatus.OK, status);

		nodeLock = janusGraphDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isLeft());
		assertEquals(lockId, nodeLock.left().value().getUniqueId());

		long time2 = nodeLock.left().value().getTime();

		assertTrue(time2 > time1);

		status = janusGraphDao.releaseElement(resourceData);
		assertEquals(JanusGraphOperationStatus.OK, status);

		nodeLock = janusGraphDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), lockId, GraphNodeLock.class);
		assertTrue(nodeLock.isRight());
		assertEquals(JanusGraphOperationStatus.NOT_FOUND, nodeLock.right().value());

		janusGraphDao.deleteNode(resourceData, ResourceMetadataData.class);
		janusGraphDao.commit();

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

		Either<ResourceMetadataData, JanusGraphOperationStatus> resource1 = janusGraphDao.createNode(resourceData,
				ResourceMetadataData.class);
		assertTrue(resource1.isLeft());

		resourceData = new ResourceMetadataData();

		resourceData.getMetadataDataDefinition().setName("resourceForLock");
		resourceData.getMetadataDataDefinition().setVersion("0.2");
		resourceData.getMetadataDataDefinition().setState("NOT_CERTIFIED_CHECKOUT");
		resourceData.getMetadataDataDefinition().setHighestVersion(false);
		resourceData.getMetadataDataDefinition().setUniqueId(resourceData.getMetadataDataDefinition().getName() + "."
				+ resourceData.getMetadataDataDefinition().getVersion());

		Either<ResourceMetadataData, JanusGraphOperationStatus> resource2 = janusGraphDao.createNode(resourceData,
				ResourceMetadataData.class);
		janusGraphDao.commit();

		Map<String, Object> props = new HashMap<>();

		props.put(GraphPropertiesDictionary.STATE.getProperty(), "NOT_CERTIFIED_CHECKOUT");
		props.put("name", "resourceForLock");
		props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), false);

		// get by criteria. must be 1
		Either<List<ResourceMetadataData>, JanusGraphOperationStatus> byCriteria = janusGraphDao
				.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());

		janusGraphDao.deleteNode(resource1.left().value(), ResourceMetadataData.class);

		janusGraphDao.deleteNode(resource2.left().value(), ResourceMetadataData.class);
		janusGraphDao.commit();
	}

	// @Test
	public void testStringSearch() {
		JanusGraph graph;

		BaseConfiguration conf = new BaseConfiguration();
		conf.setProperty("storage.backend", "inmemory");
		graph = JanusGraphFactory.open(conf);

		// JanusGraphManagement graphMgt = graph.getManagementSystem();
		JanusGraphManagement graphMgt = graph.openManagement();
		PropertyKey propKey = graphMgt.makePropertyKey("string1").dataType(String.class).make();
		graphMgt.buildIndex("string1", Vertex.class).addKey(propKey).unique().buildCompositeIndex();

		propKey = graphMgt.makePropertyKey("string2").dataType(String.class).make();

		graphMgt.buildIndex("string2", Vertex.class).addKey(propKey).unique().buildCompositeIndex();
		graphMgt.commit();


		log.debug("**** predicat index search non case");
		Iterable<JanusGraphVertex> vertices = graph.query().has("string1", Text.REGEX, "my new string 1").vertices();
		Iterator<JanusGraphVertex> iter = vertices.iterator();
		while (iter.hasNext()) {
			Vertex ver = iter.next();
			// System.out.println(com.tinkerpop.blueprints.util.ElementHelper.getProperties(ver));
			log.debug("{}", janusGraphDao.getProperties(ver));
		}

	}

	@Test
	public void testDuplicateResultDueToJanusGraphBug() {

		ResourceMetadataData resourceData1 = new ResourceMetadataData();
		resourceData1.getMetadataDataDefinition().setUniqueId("A");
		((ResourceMetadataDataDefinition) resourceData1.getMetadataDataDefinition()).setAbstract(true);
		resourceData1.getMetadataDataDefinition().setName("aaaa");

		Either<ResourceMetadataData, JanusGraphOperationStatus> newNode1 = janusGraphDao.createNode(resourceData1,
				ResourceMetadataData.class);
		assertTrue(newNode1.isLeft());
		log.debug("{}", newNode1.left().value());
		// janusGraphDao.commit();

		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), true);
		Either<List<ResourceMetadataData>, JanusGraphOperationStatus> byCriteria = janusGraphDao
				.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals("check one result returned", 1, byCriteria.left().value().size());
		// janusGraphDao.commit();

		ResourceMetadataData resourceToUpdate = new ResourceMetadataData();
		((ResourceMetadataDataDefinition) resourceToUpdate.getMetadataDataDefinition()).setAbstract(false);
		resourceToUpdate.getMetadataDataDefinition().setUniqueId("A");
		Either<ResourceMetadataData, JanusGraphOperationStatus> updateNode = janusGraphDao.updateNode(resourceToUpdate,
				ResourceMetadataData.class);
		assertTrue(updateNode.isLeft());
		// janusGraphDao.commit();

		byCriteria = janusGraphDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isRight());
		assertEquals("check one result returned due to janusgraph bug", JanusGraphOperationStatus.NOT_FOUND,
				byCriteria.right().value());

		AdditionalInfoParameterData infoParameterData = new AdditionalInfoParameterData();
		infoParameterData.getAdditionalInfoParameterDataDefinition().setUniqueId("123");
		Map<String, String> idToKey = new HashMap<>();
		idToKey.put("key1", "value1");
		infoParameterData.setIdToKey(idToKey);

		Either<AdditionalInfoParameterData, JanusGraphOperationStatus> newNode2 = janusGraphDao.createNode(infoParameterData,
				AdditionalInfoParameterData.class);
		assertTrue(newNode2.isLeft());
		log.debug("{}", newNode2.left().value());
		// janusGraphDao.commit();

		Map<String, String> idToKey2 = new HashMap<>();
		idToKey2.put("key1", "value2");

		Map<String, Object> props2 = new HashMap<>();
		props2.put(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), "123");
		Gson gson = new Gson();
		props2.put(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY.getProperty(), gson.toJson(idToKey2));
		// props2.put(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY.getProperty(),
		// idToKey2);

		Either<List<AdditionalInfoParameterData>, JanusGraphOperationStatus> byCriteria2 = janusGraphDao
				.getByCriteria(NodeTypeEnum.AdditionalInfoParameters, props2, AdditionalInfoParameterData.class);
		assertTrue(byCriteria2.isRight());
		assertEquals("check one result returned due to janusgraph bug", JanusGraphOperationStatus.NOT_FOUND,
				byCriteria2.right().value());

		infoParameterData.setIdToKey(idToKey2);

		Either<AdditionalInfoParameterData, JanusGraphOperationStatus> updateNode2 = janusGraphDao.updateNode(infoParameterData,
				AdditionalInfoParameterData.class);
		assertTrue(updateNode2.isLeft());
		// janusGraphDao.commit();

		props2.put(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY.getProperty(), idToKey);
		byCriteria2 = janusGraphDao.getByCriteria(NodeTypeEnum.AdditionalInfoParameters, props2,
				AdditionalInfoParameterData.class);
		assertTrue(byCriteria2.isRight());
		assertEquals("check one result returned due to janusgraph bug", JanusGraphOperationStatus.NOT_FOUND,
				byCriteria2.right().value());

		ComponentInstanceData resourceInstanceData = new ComponentInstanceData();
		resourceInstanceData.getComponentInstDataDefinition().setUniqueId("ri123");
		resourceInstanceData.getComponentInstDataDefinition().setPosX("22");
		resourceInstanceData.getComponentInstDataDefinition().setName("myresource_1");

		Either<ComponentInstanceData, JanusGraphOperationStatus> newNode3 = janusGraphDao.createNode(resourceInstanceData,
				ComponentInstanceData.class);
		assertTrue(newNode3.isLeft());
		log.debug("{}", newNode3.left().value());
		// janusGraphDao.commit();

		resourceInstanceData.getComponentInstDataDefinition().setPosX("50");
		Either<ComponentInstanceData, JanusGraphOperationStatus> updateNode3 = janusGraphDao.updateNode(resourceInstanceData,
				ComponentInstanceData.class);
		assertTrue(updateNode3.isLeft());
		// janusGraphDao.commit();

		resourceInstanceData.getComponentInstDataDefinition().setName("myresource_2");
		updateNode3 = janusGraphDao.updateNode(resourceInstanceData, ComponentInstanceData.class);
		assertTrue(updateNode3.isLeft());
		// janusGraphDao.commit();

		Map<String, Object> props3 = new HashMap<>();
		props3.put("positionX", "22");
		Either<List<ComponentInstanceData>, JanusGraphOperationStatus> byCriteria3 = janusGraphDao
				.getByCriteria(NodeTypeEnum.ResourceInstance, props3, ComponentInstanceData.class);
		assertTrue(byCriteria3.isRight());
		assertEquals("check one result returned due to janusgraph bug", JanusGraphOperationStatus.NOT_FOUND,
				byCriteria3.right().value());

		props3.put("positionX", "50");
		byCriteria3 = janusGraphDao.getByCriteria(NodeTypeEnum.ResourceInstance, props3, ComponentInstanceData.class);
		assertTrue(byCriteria3.isLeft());

		/////////////////////////// check integer ////////////////////////

		ArtifactData artifactData = new ArtifactData();
		artifactData.getArtifactDataDefinition().setUniqueId("ad234");
		artifactData.getArtifactDataDefinition().setTimeout(100);

		Either<ArtifactData, JanusGraphOperationStatus> newNode4 = janusGraphDao.createNode(artifactData, ArtifactData.class);
		assertTrue(newNode4.isLeft());
		log.debug("{}", newNode4.left().value());
		// janusGraphDao.commit();

		artifactData.getArtifactDataDefinition().setTimeout(50);
		Either<ArtifactData, JanusGraphOperationStatus> updateNode4 = janusGraphDao.updateNode(artifactData, ArtifactData.class);
		assertTrue(updateNode4.isLeft());
		// janusGraphDao.commit();

		Map<String, Object> props4 = new HashMap<>();
		props4.put("timeout", 100);
		Either<List<ArtifactData>, JanusGraphOperationStatus> byCriteria4 = janusGraphDao.getByCriteria(NodeTypeEnum.ArtifactRef,
				props4, ArtifactData.class);
		assertTrue(byCriteria4.isRight());
		assertEquals("check one result returned due to janusgraph bug", JanusGraphOperationStatus.NOT_FOUND,
				byCriteria4.right().value());

		props4.put("timeout", 50);
		byCriteria4 = janusGraphDao.getByCriteria(NodeTypeEnum.ArtifactRef, props4, ArtifactData.class);
		assertTrue(byCriteria4.isLeft());

		janusGraphDao.rollback();
	}

	@Test
	public void testDuplicateResultUSeHasNotQueryDueToJanusGraphBug() {
		
		String name = "bbbb";

		ResourceMetadataData resourceData1 = new ResourceMetadataData();
		resourceData1.getMetadataDataDefinition().setUniqueId("A");
		((ResourceMetadataDataDefinition) resourceData1.getMetadataDataDefinition()).setAbstract(true);
		resourceData1.getMetadataDataDefinition().setName(name);

		Either<ResourceMetadataData, JanusGraphOperationStatus> newNode1 = janusGraphDao.createNode(resourceData1,
				ResourceMetadataData.class);
		assertTrue(newNode1.isLeft());
		log.debug("{}", newNode1.left().value());
		// janusGraphDao.commit();

		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), true);
		Either<List<ResourceMetadataData>, JanusGraphOperationStatus> byCriteria = janusGraphDao
				.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals("check one result returned", 1, byCriteria.left().value().size());
		// janusGraphDao.commit();

		ResourceMetadataData resourceToUpdate = new ResourceMetadataData();
		((ResourceMetadataDataDefinition) resourceToUpdate.getMetadataDataDefinition()).setAbstract(false);
		resourceToUpdate.getMetadataDataDefinition().setUniqueId("A");
		Either<ResourceMetadataData, JanusGraphOperationStatus> updateNode = janusGraphDao.updateNode(resourceToUpdate,
				ResourceMetadataData.class);
		assertTrue(updateNode.isLeft());
		// janusGraphDao.commit();

		// no result where isAbstract = true
		byCriteria = janusGraphDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isRight());
		assertEquals("check one result returned due to janusgraph bug", JanusGraphOperationStatus.NOT_FOUND,
				byCriteria.right().value());

		// one result where isAbstract != true
		byCriteria = janusGraphDao.getByCriteria(NodeTypeEnum.Resource, null, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isLeft());
		assertEquals("check one result returned", 1, byCriteria.left().value().size());

		props.put(GraphPropertiesDictionary.IS_ABSTRACT.getProperty(), false);
		byCriteria = janusGraphDao.getByCriteria(NodeTypeEnum.Resource, null, props, ResourceMetadataData.class);
		assertTrue(byCriteria.isRight());
		assertEquals("check one result returned due to janusgraph bug", JanusGraphOperationStatus.NOT_FOUND,
				byCriteria.right().value());

		janusGraphDao.rollback();

	}
	
}
