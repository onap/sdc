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
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class ServiceMetadataData extends ComponentMetadataData {

	public ServiceMetadataData() {
		super(NodeTypeEnum.Service, new ServiceMetadataDataDefinition());
	}

	public ServiceMetadataData(ServiceMetadataDataDefinition serviceMetadataDataDefinition) {
		super(NodeTypeEnum.Service, serviceMetadataDataDefinition);
	}

	public ServiceMetadataData(Map<String, Object> properties) {
		super(NodeTypeEnum.Service, new ServiceMetadataDataDefinition(), properties);
		((ServiceMetadataDataDefinition) metadataDataDefinition)
				.setProjectCode((String) properties.get(GraphPropertiesDictionary.PROJECT_CODE.getProperty()));
		((ServiceMetadataDataDefinition) metadataDataDefinition).setDistributionStatus(
				(String) properties.get(GraphPropertiesDictionary.DISTRIBUTION_STATUS.getProperty()));
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> graphMap = super.toGraphMap();
		addIfExists(graphMap, GraphPropertiesDictionary.PROJECT_CODE,
				((ServiceMetadataDataDefinition) metadataDataDefinition).getProjectCode());
		addIfExists(graphMap, GraphPropertiesDictionary.DISTRIBUTION_STATUS,
				((ServiceMetadataDataDefinition) metadataDataDefinition).getDistributionStatus());
		return graphMap;
	}

}
