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

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionaryExtractor;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

import java.util.Map;

public class ResourceMetadataData extends ComponentMetadataData {

	public ResourceMetadataData() {
		super(NodeTypeEnum.Resource, new ResourceMetadataDataDefinition());
	}

	public ResourceMetadataData(ComponentMetadataDataDefinition metadataDataDefinition) {
		super(NodeTypeEnum.Resource, metadataDataDefinition);
	}

	public ResourceMetadataData(GraphPropertiesDictionaryExtractor extractor) {
		super(NodeTypeEnum.Resource, new ResourceMetadataDataDefinition(), extractor);
		((ResourceMetadataDataDefinition) metadataDataDefinition).setVendorName(extractor.getVendorName());
		((ResourceMetadataDataDefinition) metadataDataDefinition).setVendorRelease(extractor.getVendorRelease());
		((ResourceMetadataDataDefinition) metadataDataDefinition).setResourceType(extractor.getResourceType());
		((ResourceMetadataDataDefinition) metadataDataDefinition).setAbstract(extractor.isAbstract());
		((ResourceMetadataDataDefinition) metadataDataDefinition).setCost(extractor.getCost());
		((ResourceMetadataDataDefinition) metadataDataDefinition).setLicenseType(extractor.getLicenseType());
		((ResourceMetadataDataDefinition) metadataDataDefinition).setToscaResourceName(extractor.getToscaResourceName());
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> graphMap = super.toGraphMap();
		addIfExists(graphMap, GraphPropertiesDictionary.VENDOR_NAME, ((ResourceMetadataDataDefinition) metadataDataDefinition).getVendorName());
		addIfExists(graphMap, GraphPropertiesDictionary.VENDOR_RELEASE, ((ResourceMetadataDataDefinition) metadataDataDefinition).getVendorRelease());
		addIfExists(graphMap, GraphPropertiesDictionary.RESOURCE_TYPE, ((ResourceMetadataDataDefinition) metadataDataDefinition).getResourceType().name());
		addIfExists(graphMap, GraphPropertiesDictionary.IS_ABSTRACT, ((ResourceMetadataDataDefinition) metadataDataDefinition).isAbstract());
		addIfExists(graphMap, GraphPropertiesDictionary.COST, ((ResourceMetadataDataDefinition) metadataDataDefinition).getCost());
		addIfExists(graphMap, GraphPropertiesDictionary.LICENSE_TYPE, ((ResourceMetadataDataDefinition) metadataDataDefinition).getLicenseType());
		addIfExists(graphMap, GraphPropertiesDictionary.TOSCA_RESOURCE_NAME, ((ResourceMetadataDataDefinition) metadataDataDefinition).getToscaResourceName());
		return graphMap;
	}

}
