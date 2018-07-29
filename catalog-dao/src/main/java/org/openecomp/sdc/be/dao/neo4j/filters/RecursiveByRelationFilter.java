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

package org.openecomp.sdc.be.dao.neo4j.filters;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class RecursiveByRelationFilter extends RecursiveFilter {

	private GraphNode node;
	private String relationType;

	public RecursiveByRelationFilter() {
		super();
	}

	public RecursiveByRelationFilter(NodeTypeEnum nodeType, GraphNode node) {
		super(nodeType);
		this.node = node;
	}

	public RecursiveByRelationFilter(NodeTypeEnum nodeType) {
		super(nodeType);
	}

	public RecursiveByRelationFilter(NodeTypeEnum nodeType, GraphNode node, String relationType) {
		super(nodeType);
		this.node = node;
		this.relationType = relationType;
	}

	public RecursiveByRelationFilter addNode(GraphNode node) {
		this.node = node;
		return this;
	}

	public RecursiveByRelationFilter addRelation(String relationType) {
		this.relationType = relationType;
		return this;
	}

	public GraphNode getNode() {
		return node;
	}

	public void setNode(GraphNode node) {
		this.node = node;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	@Override
	public String toString() {
		return "RecursiveByRelationFilter [node=" + node + ", relationType=" + relationType + "]";
	}

}
