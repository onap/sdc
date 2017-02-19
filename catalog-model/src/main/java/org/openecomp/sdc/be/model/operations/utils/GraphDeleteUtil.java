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

package org.openecomp.sdc.be.model.operations.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphDeleteUtil {

	private static Logger log = LoggerFactory.getLogger(GraphDeleteUtil.class.getName());

	public TitanOperationStatus deleteChildrenNodes(Vertex rootVertex, GraphEdgeLabels edgeType) {

		// Iterable<Edge> edgesCreatorIterable =
		// rootVertex.getEdges(Direction.OUT,
		// edgeType.name());
		Iterator<Edge> edgesCreatorIterator = rootVertex.edges(Direction.OUT, edgeType.getProperty());

		while (edgesCreatorIterator.hasNext()) {
			Edge edge = edgesCreatorIterator.next();
			Vertex incomingVertex = edge.inVertex();
			Iterator<Edge> outEdges = incomingVertex.edges(Direction.OUT);

			if (outEdges.hasNext()) {
				return TitanOperationStatus.CANNOT_DELETE_NON_LEAF_NODE;
			} else {
				Map<String, Object> properties = null;
				if (log.isDebugEnabled()) {
					properties = getProperties(incomingVertex);
					log.debug("Going to delete vertex {}", properties);
				}
				incomingVertex.remove();
				if (log.isDebugEnabled()) {
					log.debug("After deleting vertex {}", properties);
				}
			}

		}

		//
		// if (edgesCreatorIterable != null) {
		// for (Edge edge : edgesCreatorIterable) {
		//
		// Vertex incomingVertex = edge.getVertex(Direction.IN);
		// Iterable<Edge> outEdges = incomingVertex.getEdges(Direction.OUT);
		// if (outEdges != null) {
		// if (outEdges.iterator().hasNext()) {
		// return TitanOperationStatus.CANNOT_DELETE_NON_LEAF_NODE;
		// } else {
		// Map<String, Object> properties = null;
		// if (log.isDebugEnabled()) {
		// properties = ElementHelper.getProperties(incomingVertex);
		// log.debug("Going to delete vertex {}", properties);
		// }
		// incomingVertex.remove();
		// if (log.isDebugEnabled()) {
		// log.debug("After deleting vertex {}", properties);
		// }
		// }
		// }
		//
		// }
		// }

		return TitanOperationStatus.OK;

	}

	public Map<String, Object> getProperties(Element element) {

		Map<String, Object> result = null;

		if (element.keys() != null && element.keys().size() > 0) {
			Map<String, Property> propertyMap = ElementHelper.propertyMap(element,
					element.keys().toArray(new String[element.keys().size()]));
			result = new HashMap<String, Object>();

			for (Entry<String, Property> entry : propertyMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue().value();

				result.put(key, value);
			}
		}
		return result;
	}

}
