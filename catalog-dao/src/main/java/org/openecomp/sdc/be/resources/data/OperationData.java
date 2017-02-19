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

package org.openecomp.sdc.be.resources.data;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class OperationData extends GraphNode {

	OperationDataDefinition operationDataDefinition;

	protected OperationData() {
		super(NodeTypeEnum.InterfaceOperation);
		operationDataDefinition = new OperationDataDefinition();
	}

	public OperationData(OperationDataDefinition operationDataDefinition) {
		super(NodeTypeEnum.InterfaceOperation);
		this.operationDataDefinition = operationDataDefinition;

	}

	public OperationData(OperationData operationData) {
		super(NodeTypeEnum.InterfaceOperation);
		this.operationDataDefinition = operationData.getOperationDataDefinition();

	}

	public OperationData(Map<String, Object> properties) {
		this();
		operationDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		operationDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		operationDataDefinition
				.setCreationDate((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
		operationDataDefinition
				.setLastUpdateDate((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
	}

	public OperationDataDefinition getOperationDataDefinition() {
		return operationDataDefinition;
	}

	public void setOperationDataDefinition(OperationDataDefinition operationDataDefinition) {
		this.operationDataDefinition = operationDataDefinition;
	}

	@Override
	public Object getUniqueId() {
		return operationDataDefinition.getUniqueId();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, operationDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, operationDataDefinition.getCreationDate());
		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, operationDataDefinition.getLastUpdateDate());
		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, operationDataDefinition.getDescription());

		return map;
	}

}
