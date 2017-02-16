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
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

public class ResourceMetadataData extends ComponentMetadataData {

	public ResourceMetadataData() {
		super(NodeTypeEnum.Resource, new ResourceMetadataDataDefinition());
	}

	public ResourceMetadataData(ComponentMetadataDataDefinition metadataDataDefinition) {
		super(NodeTypeEnum.Resource, metadataDataDefinition);
	}

	public ResourceMetadataData(Map<String, Object> properties) {
		super(NodeTypeEnum.Resource, new ResourceMetadataDataDefinition(), properties);
		((ResourceMetadataDataDefinition) metadataDataDefinition)
				.setVendorName((String) properties.get(GraphPropertiesDictionary.VENDOR_NAME.getProperty()));
		((ResourceMetadataDataDefinition) metadataDataDefinition)
				.setVendorRelease((String) properties.get(GraphPropertiesDictionary.VENDOR_RELEASE.getProperty()));
		((ResourceMetadataDataDefinition) metadataDataDefinition).setResourceType(ResourceTypeEnum
				.valueOf((String) properties.get(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty())));
		((ResourceMetadataDataDefinition) metadataDataDefinition)
				.setAbstract((Boolean) properties.get(GraphPropertiesDictionary.IS_ABSTRACT.getProperty()));
		((ResourceMetadataDataDefinition) metadataDataDefinition)
				.setCost((String) properties.get(GraphPropertiesDictionary.COST.getProperty()));
		((ResourceMetadataDataDefinition) metadataDataDefinition)
				.setLicenseType((String) properties.get(GraphPropertiesDictionary.LICENSE_TYPE.getProperty()));
		((ResourceMetadataDataDefinition) metadataDataDefinition).setToscaResourceName(
				(String) properties.get(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty()));

	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> graphMap = super.toGraphMap();
		addIfExists(graphMap, GraphPropertiesDictionary.VENDOR_NAME,
				((ResourceMetadataDataDefinition) metadataDataDefinition).getVendorName());
		addIfExists(graphMap, GraphPropertiesDictionary.VENDOR_RELEASE,
				((ResourceMetadataDataDefinition) metadataDataDefinition).getVendorRelease());
		addIfExists(graphMap, GraphPropertiesDictionary.RESOURCE_TYPE,
				((ResourceMetadataDataDefinition) metadataDataDefinition).getResourceType().name());
		addIfExists(graphMap, GraphPropertiesDictionary.IS_ABSTRACT,
				((ResourceMetadataDataDefinition) metadataDataDefinition).isAbstract());
		addIfExists(graphMap, GraphPropertiesDictionary.COST,
				((ResourceMetadataDataDefinition) metadataDataDefinition).getCost());
		addIfExists(graphMap, GraphPropertiesDictionary.LICENSE_TYPE,
				((ResourceMetadataDataDefinition) metadataDataDefinition).getLicenseType());
		addIfExists(graphMap, GraphPropertiesDictionary.TOSCA_RESOURCE_NAME,
				((ResourceMetadataDataDefinition) metadataDataDefinition).getToscaResourceName());
		return graphMap;
	}

}
