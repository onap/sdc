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

import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabiltyProperty;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabiltyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;


public enum VertexTypeEnum {
	NODE_TYPE					("node_type",					null),
	TOPOLOGY_TEMPLATE			("topology_template", 			CompositionDataDefinition.class),
	ARTIFACTS					("artifacts",					ArtifactDataDefinition.class),
	TOSCA_ARTIFACTS				("tosca_artifacts",				ArtifactDataDefinition.class),
	DEPLOYMENT_ARTIFACTS		("deployment_artifacts",		ArtifactDataDefinition.class),	
	INST_DEPLOYMENT_ARTIFACTS	("inst_deployment_artifacts",	MapArtifactDataDefinition.class),
	INTERFACE_ARTIFACTS			("interface_artifacts",			InterfaceDataDefinition.class),
	INSTANCE_ARTIFACTS			("instance_artifacts",			MapArtifactDataDefinition.class),
	PROPERTIES					("properties",					PropertyDataDefinition.class),
	CAPABILTIES					("capabilities",				ListCapabilityDataDefinition.class),
	CAPABILITIES_PROPERTIES		("capabilities_properties",		MapPropertiesDataDefinition.class),	
	REQUIREMENTS				("requirements",				ListRequirementDataDefinition.class),
	ATTRIBUTES					("attributes",					PropertyDataDefinition.class),
	RESOURCE_CATEGORY			("resourceNewCategory",			null),
	RESOURCE_SUBCATEGORY		("resourceSubcategory",			null),
	SERVICE_CATEGORY			("serviceNewCategory", 			null), 
	ADDITIONAL_INFORMATION		("additional_information",      AdditionalInfoParameterDataDefinition.class),
	USER						("user",						null),
	INPUTS						("inputs",						PropertyDataDefinition.class),
	GROUPS						("groups",						GroupDataDefinition.class),
	INST_ATTRIBUTES				("instAttributes",				MapPropertiesDataDefinition.class),
	INST_PROPERTIES				("instProperties",				MapPropertiesDataDefinition.class),
	INST_INPUTS					("instInputs",					MapPropertiesDataDefinition.class),
	INST_GROUPS					("instGroups",					MapGroupsDataDefinition.class),
	SERVICE_API_ARTIFACTS		("serviceApiArtifacts",			ArtifactDataDefinition.class),
	CALCULATED_CAPABILITIES 	("calculatedCapabilities",		MapListCapabiltyDataDefinition.class),
	FULLFILLED_CAPABILITIES 	("fullfilledCapabilities",		MapListCapabiltyDataDefinition.class), 
	CALCULATED_REQUIREMENTS 	("calculatedRequirements",		MapListRequirementDataDefinition.class),
	FULLFILLED_REQUIREMENTS		("fullfilledRequirements",		MapListRequirementDataDefinition.class),
	CALCULATED_CAP_PROPERTIES	("calculatedCapProperties",		MapCapabiltyProperty.class)
;

	private String name;
	private Class classOfJson;
	VertexTypeEnum(String name, Class clazz){
		this.name = name;
		classOfJson = clazz;
	}
	
	public String getName() {
		return name;
	}

	public Class getClassOfJson() {
		return classOfJson;
	}

	public static VertexTypeEnum getByName(String name){
		for ( VertexTypeEnum inst : VertexTypeEnum.values() ){
			if ( inst.getName().equals(name) ){
				return inst;
			}
		}
		return null;
	}
}
