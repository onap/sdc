package org.openecomp.sdc.asdctool.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanException;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;

public class TitanGraphInitializer {

	private static Logger logger = LoggerFactory.getLogger(TitanGraphInitializer.class.getName());
	private static TitanGraph graph;


	public static boolean createGraph(String titanCfgFile) {
		logger.info("** createGraph with {}", titanCfgFile);
		try {
			logger.info("createGraph : try to load file {}", titanCfgFile);
			graph = TitanFactory.open(titanCfgFile);
			if (graph.isClosed()) {
				return false;
			}

		} catch (TitanException e) {
			logger.info("createGraph : failed to open Titan graph with configuration file: {}", titanCfgFile, e);
			return false;
		}
		
		createIndexesAndDefaults();
		
		logger.info("** Titan graph created ");

		return true;
	}

	private static boolean isVertexExist(Map<String, Object> properties) {
		TitanGraphQuery query = graph.query();

		if (properties != null && !properties.isEmpty()) {
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}
		Iterable<Vertex> vertecies = query.vertices();
		java.util.Iterator<Vertex> iterator = vertecies.iterator();
		if (iterator.hasNext()) {
			return true;
		}
		return false;
	}

	private static void createDefaultUsers() {
		List<UserData> users = createUserList();
		for (UserData user : users) {
			Vertex vertex = null;
			Map<String, Object> checkedProperties = new HashMap<String, Object>();
			checkedProperties.put(GraphPropertiesDictionary.USERID.getProperty(), user.getUserId());
			checkedProperties.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.User.getName());
			Map<String, Object> properties = null;
			if (!isVertexExist(checkedProperties)) {
				vertex = graph.addVertex();
				vertex.property(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.User.getName());
				properties = user.toGraphMap();
				for (Map.Entry<String, Object> entry : properties.entrySet()) {
					vertex.property(entry.getKey(), entry.getValue());
				}
			}
		}
		graph.tx().commit();

	}

	private static List<UserData> createUserList() {
		LinkedList<UserData> users = new LinkedList<UserData>();
		users.add(getDefaultUserAdmin1());
		users.add(getDefaultUserAdmin2());
		users.add(getDefaultUserDesigner1());
		users.add(getDefaultUserDesigner2());
		users.add(getDefaultUserTester1());
		users.add(getDefaultUserTester2());
		users.add(getDefaultUserTester3());
		users.add(getDefaultUserGovernor1());
		users.add(getDefaultUserGovernor2());
		users.add(getDefaultUserOps1());
		users.add(getDefaultUserOps2());
		users.add(getDefaultUserProductManager1());
		users.add(getDefaultUserProductManager2());
		users.add(getDefaultUserProductStrategist1());
		users.add(getDefaultUserProductStrategist2());
		users.add(getDefaultUserProductStrategist3());
		return users;
	}

	private static UserData getDefaultUserAdmin1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("jh0003");
		userData.setEmail("admin@sdc.com");
		userData.setFirstName("Jimmy");
		userData.setLastName("Hendrix");
		userData.setRole("ADMIN");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserAdmin2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("tr0001");
		userData.setEmail("admin@sdc.com");
		userData.setFirstName("Todd");
		userData.setLastName("Rundgren");
		userData.setRole("ADMIN");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserDesigner1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("cs0008");
		userData.setEmail("designer@sdc.com");
		userData.setFirstName("Carlos");
		userData.setLastName("Santana");
		userData.setRole("DESIGNER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserDesigner2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("me0009");
		userData.setEmail("designer@sdc.com");
		userData.setFirstName("Melissa");
		userData.setLastName("Etheridge");
		userData.setRole("DESIGNER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserTester1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("jm0007");
		userData.setEmail("tester@sdc.com");
		userData.setFirstName("Joni");
		userData.setLastName("Mitchell");
		userData.setRole("TESTER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserTester2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("kb0004");
		userData.setEmail("tester@sdc.com");
		userData.setFirstName("Kate");
		userData.setLastName("Bush");
		userData.setRole("TESTER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserTester3() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("jt0005");
		userData.setEmail("tester@sdc.com");
		userData.setFirstName("James");
		userData.setLastName("Taylor");
		userData.setRole("TESTER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserOps1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("op0001");
		userData.setEmail("ops@sdc.com");
		userData.setFirstName("Steve");
		userData.setLastName("Regev");
		userData.setRole("OPS");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserOps2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("af0006");
		userData.setEmail("designer@sdc.com");
		userData.setFirstName("Aretha");
		userData.setLastName("Franklin");
		userData.setRole("OPS");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserGovernor1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("gv0001");
		userData.setEmail("governor@sdc.com");
		userData.setFirstName("David");
		userData.setLastName("Shadmi");
		userData.setRole("GOVERNOR");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserGovernor2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ah0002");
		userData.setEmail("admin@sdc.com");
		userData.setFirstName("Alex");
		userData.setLastName("Harvey");
		userData.setRole("GOVERNOR");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserProductManager1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("pm0001");
		userData.setEmail("pm1@sdc.com");
		userData.setFirstName("Teddy");
		userData.setLastName("Isashar");
		userData.setRole("PRODUCT_MANAGER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserProductManager2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("pm0002");
		userData.setEmail("pm2@sdc.com");
		userData.setFirstName("Sarah");
		userData.setLastName("Bettens");
		userData.setRole("PRODUCT_MANAGER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserProductStrategist1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ps0001");
		userData.setEmail("ps1@sdc.com");
		userData.setFirstName("Eden");
		userData.setLastName("Rozin");
		userData.setRole("PRODUCT_STRATEGIST");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserProductStrategist2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ps0002");
		userData.setEmail("ps2@sdc.com");
		userData.setFirstName("Ella");
		userData.setLastName("Kvetny");
		userData.setRole("PRODUCT_STRATEGIST");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static UserData getDefaultUserProductStrategist3() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ps0003");
		userData.setEmail("ps3@sdc.com");
		userData.setFirstName("Geva");
		userData.setLastName("Alon");
		userData.setRole("PRODUCT_STRATEGIST");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private static void createVertexIndixes() {
		logger.info("** createVertexIndixes started");

		TitanManagement graphMgt = graph.openManagement();
		TitanGraphIndex index = null;
		for (GraphPropertiesDictionary prop : GraphPropertiesDictionary.values()) {
			PropertyKey propKey = null;
			if (!graphMgt.containsPropertyKey(prop.getProperty())) {
				Class<?> clazz = prop.getClazz();
				if (!ArrayList.class.getName().equals(clazz.getName()) && !HashMap.class.getName().equals(clazz.getName())) {
					propKey = graphMgt.makePropertyKey(prop.getProperty()).dataType(prop.getClazz()).make();
				}
			} else {
				propKey = graphMgt.getPropertyKey(prop.getProperty());
			}
			if (prop.isIndexed()) {
				if (!graphMgt.containsGraphIndex(prop.getProperty())) {
					if (prop.isUnique()) {
						index = graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).unique().buildCompositeIndex();

						graphMgt.setConsistency(propKey, ConsistencyModifier.LOCK); // Ensures
																					// only
																					// one
																					// name
																					// per
																					// vertex
						graphMgt.setConsistency(index, ConsistencyModifier.LOCK); // Ensures
																					// name
																					// uniqueness
																					// in
																					// the
																					// graph

					} else {
						graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).buildCompositeIndex();
					}
				}
			}
		}
		graphMgt.commit();
		logger.info("** createVertexIndixes ended");

	}

	private static void createEdgeIndixes() {
		logger.info("** createEdgeIndixes started");
		TitanManagement graphMgt = graph.openManagement();
		for (GraphEdgePropertiesDictionary prop : GraphEdgePropertiesDictionary.values()) {
			if (!graphMgt.containsGraphIndex(prop.getProperty())) {
				PropertyKey propKey = graphMgt.makePropertyKey(prop.getProperty()).dataType(prop.getClazz()).make();
				graphMgt.buildIndex(prop.getProperty(), Edge.class).addKey(propKey).buildCompositeIndex();

			}
		}
		graphMgt.commit();
		logger.info("** createEdgeIndixes ended");
	}

	private static void createIndexesAndDefaults() {
		createVertexIndixes();
		createEdgeIndixes();
		createDefaultUsers();
	}
}
