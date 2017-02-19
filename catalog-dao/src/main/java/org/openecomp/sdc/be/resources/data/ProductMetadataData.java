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
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.ProductMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class ProductMetadataData extends ComponentMetadataData {

	public ProductMetadataData() {
		super(NodeTypeEnum.Product, new ProductMetadataDataDefinition());
	}

	public ProductMetadataData(ProductMetadataDataDefinition metadataDataDefinition) {
		super(NodeTypeEnum.Product, metadataDataDefinition);
	}

	public ProductMetadataData(Map<String, Object> properties) {
		super(NodeTypeEnum.Product, new ProductMetadataDataDefinition(), properties);
		((ProductMetadataDataDefinition) metadataDataDefinition)
				.setFullName((String) properties.get(GraphPropertiesDictionary.FULL_NAME.getProperty()));
		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> contactsfromJson = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.CONTACTS.getProperty()), listType);
		((ProductMetadataDataDefinition) metadataDataDefinition).setContacts(contactsfromJson);
		((ProductMetadataDataDefinition) metadataDataDefinition)
				.setIsActive((Boolean) properties.get(GraphPropertiesDictionary.IS_ACTIVE.getProperty()));
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> graphMap = super.toGraphMap();
		addIfExists(graphMap, GraphPropertiesDictionary.FULL_NAME,
				((ProductMetadataDataDefinition) metadataDataDefinition).getFullName());
		addIfExists(graphMap, GraphPropertiesDictionary.CONTACTS,
				((ProductMetadataDataDefinition) metadataDataDefinition).getContacts());
		addIfExists(graphMap, GraphPropertiesDictionary.IS_ACTIVE,
				((ProductMetadataDataDefinition) metadataDataDefinition).getIsActive());
		return graphMap;
	}

	@Override
	public String toString() {
		return "ProductMetadataData [metadataDataDefinition=" + metadataDataDefinition + "]";
	}
}
