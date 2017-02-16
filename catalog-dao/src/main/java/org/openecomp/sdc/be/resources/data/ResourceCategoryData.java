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

import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class ResourceCategoryData extends CategoryData {

	private String categoryName;

	public ResourceCategoryData() {
		super(NodeTypeEnum.ResourceCategory);
	}

	public ResourceCategoryData(String categoryName, String name) {
		super(name, "", NodeTypeEnum.ResourceCategory);
		this.categoryName = categoryName;
		createUniqueId();
	}

	public ResourceCategoryData(Map<String, Object> properties) {
		super(properties, NodeTypeEnum.ResourceCategory);
		setCategoryName((String) properties.get(GraphPropertiesDictionary.CATEGORY_NAME.getProperty()));
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	protected void createUniqueId() {
		setUniqueId(getLabel() + "." + this.categoryName + "." + getName());
	}

	@Override
	public String toString() {
		return "ResourceCategoryData [categoryName=" + categoryName + "]" + super.toString();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> graphMap = super.toGraphMap();
		addIfExists(graphMap, GraphPropertiesDictionary.CATEGORY_NAME, categoryName);
		return graphMap;
	}

}
