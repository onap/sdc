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

package org.openecomp.sdc.asdctool.impl;

import org.janusgraph.core.*;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JanusGraphInitializer {

	private static Logger logger = LoggerFactory.getLogger(JanusGraphInitializer.class.getName());
	private static JanusGraph graph;

	public static boolean createGraph(String janusGraphCfgFile) {
		logger.info("** createGraph with {}", janusGraphCfgFile);
		try {
			logger.info("createGraph : try to load file {}", janusGraphCfgFile);
			graph = JanusGraphFactory.open(janusGraphCfgFile);
			if (graph.isClosed()) {
				return false;
			}

		} catch (JanusGraphException e) {
			logger.info("createGraph : failed to open JanusGraph graph with configuration file: {}", janusGraphCfgFile, e);
			return false;
		}

		createIndexesAndDefaults();

		logger.info("** JanusGraph graph created ");

		return true;
	}

	private static boolean isVertexExist(Map<String, Object> properties) {
		JanusGraphQuery query = graph.query();

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

	private static boolean isVertexNotExist(Map<String, Object> properties) {
		return !isVertexExist(properties);
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

		JanusGraphManagement graphMgt = graph.openManagement();
		JanusGraphIndex index = null;
		for (GraphPropertiesDictionary prop : GraphPropertiesDictionary.values()) {
			PropertyKey propKey = null;
			if (!graphMgt.containsPropertyKey(prop.getProperty())) {
				Class<?> clazz = prop.getClazz();
				if (!clazz.isAssignableFrom(ArrayList.class) && !clazz.isAssignableFrom(HashMap.class)) {
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
		JanusGraphManagement graphMgt = graph.openManagement();
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
		createRootVertex(VertexTypeEnum.CATALOG_ROOT);
		createRootVertex(VertexTypeEnum.ARCHIVE_ROOT);
	}

	private static void createRootVertex(VertexTypeEnum vertexTypeEnum) {
		Map<String, Object> checkedProperties = new HashMap<>();
		checkedProperties.put(GraphPropertiesDictionary.LABEL.getProperty(), vertexTypeEnum.getName());
		if (isVertexNotExist(checkedProperties)) {
			Vertex vertex = graph.addVertex();
			vertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty(), IdBuilderUtils.generateUniqueId());
			vertex.property(GraphPropertyEnum.LABEL.getProperty(), vertexTypeEnum.getName());
			graph.tx().commit();
		}
	}

}
