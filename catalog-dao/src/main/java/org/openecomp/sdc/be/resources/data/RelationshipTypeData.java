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

import com.google.gson.reflect.TypeToken;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationshipTypeData extends GraphNode {

	RelationshipInstDataDefinition relationshipTypeDataDefinition;

	public RelationshipTypeData() {
		super(NodeTypeEnum.RelationshipType);
		relationshipTypeDataDefinition = new RelationshipInstDataDefinition();
	}

	public RelationshipTypeData(RelationshipInstDataDefinition relationshipTypeDataDefinition) {
		super(NodeTypeEnum.RelationshipType);
		this.relationshipTypeDataDefinition = relationshipTypeDataDefinition;
	}

	public RelationshipTypeData(Map<String, Object> properties) {

		this();

		relationshipTypeDataDefinition
				.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		relationshipTypeDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		relationshipTypeDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));

		Type listSourceType = new TypeToken<List<String>>() {
		}.getType();
		List<String> validSourceTypesfromJson = getGson().fromJson(
				(String) properties.get(GraphPropertiesDictionary.VALID_SOURCE_TYPES.getProperty()), listSourceType);

		relationshipTypeDataDefinition.setValidSourceTypes(validSourceTypesfromJson);

        Type listTargetType = new TypeToken<List<String>>() {
        }.getType();
        List<String> validTargetTypesfromJson = getGson().fromJson(
                (String) properties.get(GraphPropertiesDictionary.VALID_TARGET_TYPES.getProperty()), listTargetType);

        relationshipTypeDataDefinition.setValidTargetTypes(validTargetTypesfromJson);

		// relationshipTypeDataDefinition.setValidSourceTypes((List<String>)
		// properties.get(GraphPropertiesDictionary.VALID_SOURCE_TYPES
		// .getProperty()));

		relationshipTypeDataDefinition
				.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		relationshipTypeDataDefinition
				.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		// capabilityTypeDataDefinition.setVersion(version);

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, relationshipTypeDataDefinition.getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.TYPE, relationshipTypeDataDefinition.getType());

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, relationshipTypeDataDefinition.getDescription());

		// String validSourceTypesToJson =
		// getGson().toJson(relationshipTypeDataDefinition.getValidSourceTypes());

		// addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES,
		// validSourceTypesToJson);

		addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES,
				relationshipTypeDataDefinition.getValidSourceTypes());

        addIfExists(map, GraphPropertiesDictionary.VALID_TARGET_TYPES,
                relationshipTypeDataDefinition.getValidTargetTypes());

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, relationshipTypeDataDefinition.getCreationTime());

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE,
				relationshipTypeDataDefinition.getModificationTime());

		return map;
	}

	public RelationshipInstDataDefinition getRelationshipTypeDataDefinition() {
		return relationshipTypeDataDefinition;
	}

	public void setRelationshipTypeDataDefinition(RelationshipInstDataDefinition relationshipTypeDataDefinition) {
		this.relationshipTypeDataDefinition = relationshipTypeDataDefinition;
	}

	@Override
	public String getUniqueId() {
		return this.relationshipTypeDataDefinition.getUniqueId();
	}

	@Override
	public String toString() {
		return "RelationshipTypeData [relationshipTypeDataDefinition=" + relationshipTypeDataDefinition + "]";
	}

}
