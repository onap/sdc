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

package org.openecomp.sdc.be.dao.jsongraph.types;

public enum EdgeLabelEnum {
	ARTIFACTS,
	DEPLOYMENT_ARTIFACTS,
	INST_DEPLOYMENT_ARTIFACTS,
	INSTANCE_ARTIFACTS,
	INTERFACE_ARTIFACTS,
	TOSCA_ARTIFACTS,
	PROPERTIES,
	CAPABILITIES,
	CAPABILITIES_PROPERTIES,
	REQUIREMENTS,
	ATTRIBUTES,
	ADDITIONAL_INFORMATION,
	CATEGORY,
	DERIVED_FROM,
	STATE,
	LAST_STATE,
	LAST_MODIFIER,
	VERSION,
	CREATOR,
	SUB_CATEGORY,
	INPUTS,
	GROUPS,
	INST_PROPERTIES,
	INST_INPUTS,
	INST_ATTRIBUTES,
	INST_GROUPS,
	SERVICE_API_ARTIFACTS,
	FORWARDING_PATH,
	CALCULATED_CAPABILITIES,
	FULLFILLED_CAPABILITIES,
	CALCULATED_REQUIREMENTS,
	FULLFILLED_REQUIREMENTS,
	LAST_DISTRIBUTION_STATE_MODIFIER,
	CALCULATED_CAP_PROPERTIES,
	POLICIES,
	EXTERNAL_REFS,
	CATALOG_ELEMENT,
    ARCHIVE_ELEMENT,
	INSTANCE_OF,
	PROXY_OF,
	ALLOTTED_OF,
	INTERFACE,
	INTERFACE_OPERATION,
	INST_INTERFACES,
	NODE_FILTER_TEMPLATE;

    /**
	 * Returns EdgeLabelEnum according received name
	 * @param name
	 * @return
	 */
	public static EdgeLabelEnum getEdgeLabelEnum(String name){
		for(EdgeLabelEnum currLabel : EdgeLabelEnum.values()){
			if (currLabel.name().equals(name)){
				return currLabel;
			}
		}
		return null;
	}

	public boolean isInstanceArtifactsLabel() {
		return this.equals(INSTANCE_ARTIFACTS) || this.equals(INST_DEPLOYMENT_ARTIFACTS);
	}
}
