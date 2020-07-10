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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapComponentInstanceExternalRefs;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapInterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;

@Getter
@AllArgsConstructor
public enum VertexTypeEnum {
	NODE_TYPE					("node_type",					null),
	TOPOLOGY_TEMPLATE			("topology_template", 		CompositionDataDefinition.class),
	ARTIFACTS					("artifacts",					ArtifactDataDefinition.class),
	TOSCA_ARTIFACTS				("tosca_artifacts",			ArtifactDataDefinition.class),
	DEPLOYMENT_ARTIFACTS		("deployment_artifacts",		ArtifactDataDefinition.class),
	INST_DEPLOYMENT_ARTIFACTS	("inst_deployment_artifacts",	MapArtifactDataDefinition.class),
	INTERFACE_ARTIFACTS			("interface_artifacts",		InterfaceDataDefinition.class),
	INSTANCE_ARTIFACTS			("instance_artifacts",		MapArtifactDataDefinition.class),
	PROPERTIES					("properties",				PropertyDataDefinition.class),
	CAPABILITIES				("capabilities",				ListCapabilityDataDefinition.class),
	CAPABILITIES_PROPERTIES		("capabilities_properties",	MapPropertiesDataDefinition.class),
	REQUIREMENTS				("requirements",				ListRequirementDataDefinition.class),
	ATTRIBUTES					("attributes",				AttributeDataDefinition.class),
	RESOURCE_CATEGORY			("resourceNewCategory",		null),
	RESOURCE_SUBCATEGORY		("resourceSubcategory",		null),
	SERVICE_CATEGORY			("serviceNewCategory", 		null),
	ADDITIONAL_INFORMATION		("additional_information",    AdditionalInfoParameterDataDefinition.class),
	USER						("user",						null),
	INPUTS						("inputs",					PropertyDataDefinition.class),
	GROUPS						("groups",					GroupDataDefinition.class),
	INST_ATTRIBUTES				("instAttributes",			MapPropertiesDataDefinition.class),
	INST_PROPERTIES				("instProperties",			MapPropertiesDataDefinition.class),
	INST_INPUTS					("instInputs",				MapPropertiesDataDefinition.class),
	INST_GROUPS					("instGroups",				MapGroupsDataDefinition.class),
	SERVICE_API_ARTIFACTS		("serviceApiArtifacts",		ArtifactDataDefinition.class),
	CALCULATED_CAPABILITIES 	("calculatedCapabilities",	MapListCapabilityDataDefinition.class),
	FULLFILLED_CAPABILITIES 	("fullfilledCapabilities",	MapListCapabilityDataDefinition.class),
	CALCULATED_REQUIREMENTS 	("calculatedRequirements",	MapListRequirementDataDefinition.class),
	FULLFILLED_REQUIREMENTS		("fullfilledRequirements",	MapListRequirementDataDefinition.class),
	CALCULATED_CAP_PROPERTIES	("calculatedCapProperties",	MapCapabilityProperty.class),
	FORWARDING_PATH             ("path",                  	ForwardingPathDataDefinition.class),
	POLICIES					("policies",					PolicyDataDefinition.class),
	EXTERNAL_REF				("componentInstanceExtRefs",  MapComponentInstanceExternalRefs.class),
	CATALOG_ROOT                ("catalogRoot",               null),
	ARCHIVE_ROOT                ("archiveRoot",               null),
	INTERFACE		            ("interface",			        InterfaceDataDefinition.class),
	INTERFACE_OPERATION			("interfaceOperation",		OperationDataDefinition.class),
	NODE_FILTER_TEMPLATE		("NodeTemplateFilter",        CINodeFilterDataDefinition.class),
	SUBSTITUTION_FILTER_TEMPLATE ("substitution_mapping",     SubstitutionFilterDataDefinition.class),
	INST_INTERFACES             ("InstInterfaces",            MapInterfaceDataDefinition.class),
	DATA_TYPES					("data_types", 				DataTypeDataDefinition.class);

	private final String name;
	private final Class classOfJson;

	public static VertexTypeEnum getByName(String name){
		for ( VertexTypeEnum inst : VertexTypeEnum.values() ){
			if ( inst.getName().equals(name) ){
				return inst;
			}
		}
		return null;
	}
}
