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
import java.util.List;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;

public class GraphNeighbourTable {

	List<GraphNode> nodes = new ArrayList<GraphNode>();

	List<NodeRelation> directedEdges = new ArrayList<NodeRelation>();

	public List<GraphNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<GraphNode> nodes) {
		this.nodes = nodes;
	}

	public List<NodeRelation> getDirectedEdges() {
		return directedEdges;
	}

	public void setDirectedEdges(List<NodeRelation> directedEdges) {
		this.directedEdges = directedEdges;
	}

	public int addNode(GraphNode node) {
		this.nodes.add(node);
		return this.nodes.size() - 1;
	}

	public void addEdge(NodeRelation directedEdge) {
		this.directedEdges.add(directedEdge);
	}

	@Override
	public String toString() {
		return "GraphNeighbourTable [nodes=" + nodes + ", directedEdges=" + directedEdges + "]";
	}

}
