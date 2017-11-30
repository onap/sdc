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

package org.openecomp.sdc.be.model.operations.impl.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
//import com.tinkerpop.blueprints.Direction;
//import com.tinkerpop.blueprints.Edge;
//import com.tinkerpop.blueprints.Vertex;
//import com.tinkerpop.blueprints.util.ElementHelper;

public class PrintGraph {

	public void printGraphVertices(TitanGraph graph) {

		Iterable<TitanVertex> vertices = graph.query().vertices();

		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				Vertex vertex = iterator.next();

				// System.out.println(vertex);
				// System.out.println(ElementHelper.getProperties(vertex));
				// System.out.println("=======================================");
			}

		}
		// graph.commit();
		graph.tx().commit();
	}

	public void printGraphEdges(TitanGraph graph) {

		Iterable<TitanEdge> vertices = graph.query().edges();

		if (vertices != null) {
			Iterator<TitanEdge> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				Edge edge = iterator.next();

				// System.out.println(edge);
				// System.out.println("edge=" + edge.getLabel() + ",
				// properties="+ ElementHelper.getProperties(edge));
				// System.out.println("edge=" + edge.label() + ", properties="+
				// getProperties(edge));
				// System.out.println("=======================================");
			}

		}
		// graph.commit();
		graph.tx().commit();
	}

	public String buildGraphForWebgraphWiz(TitanGraph graph) {

		StringBuilder builder = new StringBuilder();
		builder.append("digraph finite_state_machine {\n");
		builder.append("rankdir=LR;\n");
		builder.append("size=\"15,10\" \n");
		// node [shape = doublecircle]; LR_0 LR_3 LR_4 LR_8;
		// node [shape = circle];

		Iterable<TitanVertex> vertices = graph.query().vertices();

		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				Vertex vertex = iterator.next();

				// System.out.println(vertex);
				// System.out.println(ElementHelper.getProperties(vertex));
				// System.out.println(getProperties(vertex));
				// System.out.println("=======================================");

				Map<String, Object> properties = getProperties(vertex);

				String nodeLabel = (String) properties.get(GraphPropertiesDictionary.LABEL.getProperty());

				String color = getColorByNodeType(nodeLabel);

				String uid = getNodeIdByLabel(nodeLabel, properties);

				// System.out.println("uid=" + uid);

				String nodeRecord = buildNodeRecord(uid, color, properties);

				// System.out.println(nodeRecord);

				builder.append(nodeRecord);

				// if (nodeLabel.equals(NodeTypeEnum.Category)) {
				//
				// String
				//
				// } else (nodeLabel.equals(NodeTypeEnum.User)) {
				//
				// }

			}

		}

		Iterable<TitanEdge> edges = graph.query().edges();

		if (edges != null) {
			Iterator<TitanEdge> iterator = edges.iterator();
			while (iterator.hasNext()) {
				Edge edge = iterator.next();

				// Vertex vertexFrom = edge.getVertex(Direction.OUT);
				// Vertex vertexTo = edge.getVertex(Direction.IN);
				Vertex vertexFrom = edge.outVertex();
				Vertex vertexTo = edge.inVertex();

				// String fromUid =
				// getNodeIdByLabel((String)vertexFrom.getProperty(GraphPropertiesDictionary.LABEL.getProperty()),
				// ElementHelper.getProperties(vertexFrom));
				// String toUid =
				// getNodeIdByLabel((String)vertexTo.getProperty(GraphPropertiesDictionary.LABEL.getProperty()),
				// ElementHelper.getProperties(vertexTo));
				String fromUid = getNodeIdByLabel(vertexFrom.value(GraphPropertiesDictionary.LABEL.getProperty()),
						getProperties(vertexFrom));
				String toUid = getNodeIdByLabel(vertexTo.value(GraphPropertiesDictionary.LABEL.getProperty()),
						getProperties(vertexTo));

				// String edgeLabel = edge.getLabel();
				String edgeLabel = edge.label();

				// String edgeRecord = buildEdgeRecord(fromUid, toUid,
				// edgeLabel, ElementHelper.getProperties(edge));
				String edgeRecord = buildEdgeRecord(fromUid, toUid, edgeLabel, getProperties(edge));

				builder.append(edgeRecord);

				// System.out.println(edge);
				// System.out.println("edge=" + edge.getLabel() + ",
				// properties="
				// + ElementHelper.getProperties(edge));
				// System.out.println("edge=" + edge.label() + ", properties="
				// + getProperties(edge));
				// System.out.println("=======================================");
			}

		}

		builder.append(" } ");

		return builder.toString();

	}

	// LR_0 [ style = "bold" color = "red" shape = "Mrecord" label =
	// "hello&#92;nworld | { name | apache } | { version | 1.0 } | { uid |
	// apache.1.0 } | { state| CERTIFIED } |{ b |{c|<here> d|e}| f}| g | h"
	// ]

	// LR_0 -> LR_2 [ label = "SS(B)" ];
	// LR_0 -> LR_1 [ label = "SS(S)" ];
	// LR_1 -> LR_3 [ label = "S($end)" ];
	// LR_2 -> LR_6 [ label = "SS(b)" ];
	// LR_2 -> LR_5 [ label = "SS(a)" ];
	// LR_2 -> LR_4 [ label = "S(A)" ];
	// LR_5 -> LR_7 [ label = "S(b)" ];
	// LR_5 -> LR_5 [ label = "S(a)" ];
	// LR_6 -> LR_6 [ label = "S(b)" ];
	// LR_6 -> LR_5 [ label = "S(a)" ];
	// LR_7 -> LR_8 [ label = "S(b)" ];
	// LR_7 -> LR_5 [ label = "S(a)" ];
	// LR_8 -> LR_6 [ label = "S(b)" ];
	// LR_8 -> LR_5 [ label = "S(a)" ];

	private String buildEdgeRecord(String fromUid, String toUid, String edgeLabel, Map<String, Object> properties) {

		StringBuilder builder = new StringBuilder();
		// LR_0 -> LR_2 [ label = "SS(B)" ];

		String generatedProps = generateStringFromProperties(properties);

		String color = getEdgeColorByLabel(edgeLabel);

		builder.append("\"" + fromUid + "\"" + " -> " + "\"" + toUid + "\"" + " [ color = " + color + " label = \""
				+ edgeLabel + "(" + generatedProps + ")\"" + " ] " + "\n");

		return builder.toString();
	}

	private String getEdgeColorByLabel(String edgeLabel) {

		GraphEdgeLabels edgeLabelEnum = GraphEdgeLabels.getByName(edgeLabel);

		String color = "black";

		switch (edgeLabelEnum) {
		case PROPERTY:
			color = "orange";
			break;
		case CAPABILITY:
			break;
		case DERIVED_FROM:
			color = "red";
		default:
			break;
		}

		return color;
	}

	private String generateStringFromProperties(Map<String, Object> properties) {

		StringBuilder builder = new StringBuilder();

		if (properties != null) {
			for (Entry<String, Object> entry : properties.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().toString();
				builder.append(key + "=" + value + "__");
			}
		}
		return builder.toString();

	}

	private String buildNodeRecord(String uid, String color, Map<String, Object> properties) {

		StringBuilder builder = new StringBuilder();

		builder.append("\"" + uid + "\"" + " [ ");
		builder.append("style = \"bold\" ");
		builder.append(" color = \"" + color + "\"");
		builder.append("shape = \"Mrecord\" ");

		String label = "";
		int maxKeyLength = 0;
		for (Entry<String, Object> entry1 : properties.entrySet()) {
			String key = entry1.getKey();
			int keyLength = key.length();
			if (keyLength > maxKeyLength) {
				maxKeyLength = keyLength;
			}
		}

		boolean first = true;
		for (Entry<String, Object> entry : properties.entrySet()) {

			String key = entry.getKey();
			String value = entry.getValue().toString();

			if (key.equals(GraphPropertiesDictionary.CONSTRAINTS.getProperty())) {
				value = value.replaceAll("[^\\w\\s]", "_");
			}

			key = padKey(key, maxKeyLength);

			if (first) {
				first = false;
			} else {
				label += " | ";
			}
			label += " { " + key + " | " + value + " } ";
		}

		builder.append("label = \"" + label + "\" ");
		builder.append(" ] ");

		// LR_0 [ style = "bold" color = "red" shape = "Mrecord" label =
		// "hello&#92;nworld | { name | apache } | { version | 1.0 } | { uid |
		// apache.1.0 } | { state| CERTIFIED } |{ b |{c|<here> d|e}| f}| g | h"
		// ]

		builder.append(" \n ");
		return builder.toString();
	}

	private String getNodeIdByLabel(String nodeLabel, Map<String, Object> properties) {

		NodeTypeEnum typeEnum = NodeTypeEnum.getByName(nodeLabel);

		String uid = null;
		switch (typeEnum) {

		case User:
			uid = (String) properties.get(GraphPropertiesDictionary.USERID.getProperty());
			break;
		case ServiceCategory:
		case ResourceCategory:
		case Tag:
			uid = (String) properties.get(GraphPropertiesDictionary.NAME.getProperty());
			break;

		default:
			uid = (String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
			break;
		}

		return uid;
	}

	private String getColorByNodeType(String nodeLabel) {

		NodeTypeEnum typeEnum = NodeTypeEnum.getByName(nodeLabel);

		String color = "red";
		switch (typeEnum) {
		case ServiceCategory:
			color = "blue";
			break;
		case ResourceCategory:
			color = "blue";
			break;
		case Resource:
			color = "forestgreen";
			break;
		case User:
			color = "green";
			break;
		case Capability:
			color = "lightgreen";
			break;
		case CapabilityType:
			color = "gray";
			break;
		case Property:
			color = "cyan";
			break;
		case RelationshipType:
			color = "darkorchid";
			break;
		case Requirement:
			color = "gold";
			break;
		case RequirementImpl:
			// color = "forestgreen";
			color = "gold";
			break;
		case Service:
			color = "cyan4";
			break;
		case Tag:
			color = "dimgrey";
			break;
		default:
			break;

		}

		return color;
	}

	private String padKey(String key, int maxKeyLength) {

		int len = key.length();
		for (int i = len; i < maxKeyLength; i++) {
			key += " ";
		}

		return key;
	}

	public int getNumberOfVertices(TitanGraph graph) {
		int counter = 0;
		Iterable<TitanVertex> vertices = graph.query().vertices();

		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				Vertex vertex = iterator.next();
				counter++;
			}
		}
		return counter;
	}

	public Set<String> getVerticesSet(TitanGraph titanGraph) {

		Set<String> set = new HashSet<String>();

		Iterable<TitanVertex> vertices = titanGraph.query().vertices();

		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				Vertex vertex = iterator.next();

				// System.out.println(vertex);
				// System.out.println(ElementHelper.getProperties(vertex));
				// System.out.println(getProperties(vertex));
				// System.out.println("=======================================");

				// Map<String, Object> properties =
				// ElementHelper.getProperties(vertex);
				Map<String, Object> properties = getProperties(vertex);

				String nodeLabel = (String) properties.get(GraphPropertiesDictionary.LABEL.getProperty());

				String uid = getNodeIdByLabel(nodeLabel, properties);

				set.add(uid);
			}
		}

		return set;

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
