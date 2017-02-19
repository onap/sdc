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

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class RecursiveFilter extends MatchFilter {

	private List<String> childRelationTypes;
	NodeTypeEnum nodeType;

	public RecursiveFilter() {
		childRelationTypes = new ArrayList<String>();
	}

	public RecursiveFilter(NodeTypeEnum nodeType) {
		childRelationTypes = new ArrayList<String>();
		this.nodeType = nodeType;
	}

	public RecursiveFilter addChildRelationType(String type) {
		childRelationTypes.add(type);
		return this;
	}

	public List<String> getChildRelationTypes() {
		return childRelationTypes;
	}

	public void setChildRelationTypes(List<String> childRelationTypes) {
		this.childRelationTypes = childRelationTypes;
	}

	public NodeTypeEnum getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeTypeEnum nodeType) {
		this.nodeType = nodeType;
	}

	@Override
	public String toString() {
		return "RecursiveFilter [childRelationTypes=" + childRelationTypes + ", nodeType=" + nodeType + "]";
	}

}
