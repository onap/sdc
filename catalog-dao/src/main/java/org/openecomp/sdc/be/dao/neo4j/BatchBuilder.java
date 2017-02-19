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

package org.openecomp.sdc.be.dao.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;

public class BatchBuilder {
	// private Map<String, List<Neo4jNode>> nodes;
	// private List<Neo4jRelation> relations;
	//
	private List<GraphElement> elements;

	// TODO add filter

	protected BatchBuilder() {
		// nodes = new HashMap<String, List<Neo4jNode>>();
		// relations = new ArrayList<Neo4jRelation>();
		elements = new ArrayList<GraphElement>();
	}

	public static BatchBuilder getBuilder() {
		return new BatchBuilder();
	}

	public BatchBuilder add(GraphElement element) {
		elements.add(element);
		return this;
	}

	public List<GraphElement> getElements() {
		return elements;
	}

	// public BatchBuilder add( Neo4jNode element ){
	// String label = element.getLabel();
	// List<Neo4jNode> list = nodes.get(label);
	// if ( list == null ){
	// list = new ArrayList<Neo4jNode>();
	// }
	// list.add(element);
	// nodes.put(label, list);

	// return this;
	// }
	// public BatchBuilder add( Neo4jRelation relation ){
	// relations.add(relation);
	// return this;
	// }
	//
	// public Map<String, List<Neo4jNode>> getNodes() {
	// return nodes;
	// }
	//
	// public List<Neo4jRelation> getRelations() {
	// return relations;
	// }

}
