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

public class NodeRelation {

	private int fromIndex;
	private int toIndex;
	private Neo4jEdge edge;

	public NodeRelation(int fromIndex, int toIndex, Neo4jEdge edge) {
		super();
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		this.edge = edge;
	}

	public int getFromIndex() {
		return fromIndex;
	}

	public void setFromIndex(int fromIndex) {
		this.fromIndex = fromIndex;
	}

	public int getToIndex() {
		return toIndex;
	}

	public void setToIndex(int toIndex) {
		this.toIndex = toIndex;
	}

	public Neo4jEdge getEdge() {
		return edge;
	}

	public void setEdge(Neo4jEdge edge) {
		this.edge = edge;
	}

	@Override
	public String toString() {
		return "NodeRelation [fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", edge=" + edge + "]";
	}

}
