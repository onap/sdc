package org.openecomp.sdc.asdctool.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
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

	private static void createDefaultAdminUser() {
		createUser(getDefaultUserAdmin());
		graph.tx().commit();

	}

	private static void createUser(UserData user) {
		Map<String, Object> checkedProperties = new HashMap<>();
		checkedProperties.put(GraphPropertiesDictionary.USERID.getProperty(), user.getUserId());
		checkedProperties.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.User.getName());
		Map<String, Object> properties = null;
		if (!isVertexExist(checkedProperties)) {
            Vertex vertex = graph.addVertex();
            vertex.property(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.User.getName());
            properties = user.toGraphMap();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                vertex.property(entry.getKey(), entry.getValue());
            }
        }
	}

	private static UserData getDefaultUserAdmin() {
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
		createDefaultAdminUser();
	}
}
