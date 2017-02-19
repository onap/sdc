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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class PolicyTypeData extends GraphNode {

	private PolicyTypeDataDefinition policyTypeDataDefinition;
	private static final Type mapType = new TypeToken<HashMap<String, String>>() {
	}.getType();
	private static final Type listType = new TypeToken<List<String>>() {
	}.getType();

	public PolicyTypeData() {
		super(NodeTypeEnum.PolicyType);
		policyTypeDataDefinition = new PolicyTypeDataDefinition();
	}

	public PolicyTypeData(PolicyTypeDataDefinition policyTypeDataDefinition) {
		super(NodeTypeEnum.PolicyType);
		this.policyTypeDataDefinition = policyTypeDataDefinition;
	}

	public PolicyTypeData(Map<String, Object> properties) {

		this();

		policyTypeDataDefinition
				.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		HashMap<String, String> metatdata = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.METADATA.getProperty()), mapType);
		policyTypeDataDefinition.setMetadata(metatdata);

		List<String> members = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.TARGETS.getProperty()), listType);
		policyTypeDataDefinition.setTargets(members);

		policyTypeDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		policyTypeDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));

		policyTypeDataDefinition.setHighestVersion(
				(boolean) properties.get(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty()));

		policyTypeDataDefinition.setVersion((String) properties.get(GraphPropertiesDictionary.VERSION.getProperty()));

		policyTypeDataDefinition
				.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		policyTypeDataDefinition
				.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, policyTypeDataDefinition.getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.TYPE, policyTypeDataDefinition.getType());

		addIfExists(map, GraphPropertiesDictionary.VERSION, policyTypeDataDefinition.getVersion());

		addIfExists(map, GraphPropertiesDictionary.IS_HIGHEST_VERSION, policyTypeDataDefinition.isHighestVersion());

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, policyTypeDataDefinition.getDescription());

		addIfExists(map, GraphPropertiesDictionary.METADATA, policyTypeDataDefinition.getMetadata());

		addIfExists(map, GraphPropertiesDictionary.TARGETS, policyTypeDataDefinition.getTargets());

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, policyTypeDataDefinition.getCreationTime());

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, policyTypeDataDefinition.getModificationTime());

		return map;
	}

	@Override
	public String toString() {
		return "PolicyTypeData [policyTypeDataDefinition=" + policyTypeDataDefinition + "]";
	}

	@Override
	public String getUniqueId() {
		return this.policyTypeDataDefinition.getUniqueId();
	}

	public PolicyTypeDataDefinition getPolicyTypeDataDefinition() {
		return policyTypeDataDefinition;
	}

	public void setPolicyTypeDataDefinition(PolicyTypeDataDefinition policyTypeDataDefinition) {
		this.policyTypeDataDefinition = policyTypeDataDefinition;
	}

}
