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

package org.openecomp.sdc.be.resources.data.category;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.category.SubCategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class SubCategoryData extends GraphNode {

	private SubCategoryDataDefinition subCategoryDataDefinition;

	public SubCategoryData(NodeTypeEnum label) {
		super(label);
		subCategoryDataDefinition = new SubCategoryDataDefinition();
	}

	public SubCategoryData(NodeTypeEnum label, SubCategoryDataDefinition subCategoryDataDefinition) {
		super(label);
		this.subCategoryDataDefinition = subCategoryDataDefinition;
	}

	public SubCategoryData(Map<String, Object> properties) {
		this(NodeTypeEnum.getByName((String) properties.get(GraphPropertiesDictionary.LABEL.getProperty())));

		subCategoryDataDefinition
				.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		subCategoryDataDefinition
				.setNormalizedName((String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty()));
		subCategoryDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> iconsfromJson = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.ICONS.getProperty()), listType);
		subCategoryDataDefinition.setIcons(iconsfromJson);
	}

	public SubCategoryDataDefinition getSubCategoryDataDefinition() {
		return subCategoryDataDefinition;
	}

	@Override
	public Object getUniqueId() {
		return subCategoryDataDefinition.getUniqueId();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, subCategoryDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.NAME, subCategoryDataDefinition.getName());
		addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, subCategoryDataDefinition.getNormalizedName());
		// String icons=getGson().toJson(subCategoryDataDefinition.getIcons());
		// addIfExists(map, GraphPropertiesDictionary.ICONS, icons);
		addIfExists(map, GraphPropertiesDictionary.ICONS, subCategoryDataDefinition.getIcons());
		return map;
	}
}
