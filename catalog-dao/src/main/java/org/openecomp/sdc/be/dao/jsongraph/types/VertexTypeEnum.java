package org.openecomp.sdc.be.dao.jsongraph.types;

import org.openecomp.sdc.be.datatypes.elements.*;


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
	ATTRIBUTES					("attributes",					AttributeDataDefinition.class),
	RESOURCE_CATEGORY			("resourceNewCategory",			null),
	RESOURCE_SUBCATEGORY		("resourceSubcategory",			null),
	SERVICE_CATEGORY			("serviceNewCategory", 			null), 
	ADDITIONAL_INFORMATION		("additional_information",      AdditionalInfoParameterDataDefinition.class),
	USER						("user",						null),
	INPUTS						("inputs",						PropertyDataDefinition.class),
	GROUPS						("groups",						GroupDataDefinition.class),
	INST_ATTRIBUTES				("instAttributes",				MapAttributesDataDefinition.class),
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
