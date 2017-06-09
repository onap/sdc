package org.openecomp.sdc.be.datatypes.enums;

public enum ComponentFieldsEnum {
	
	PROPERTIES("properties"), 
	INPUTS("inputs"), 
	USERS("users"),
	GROUPS("groups"), 
	COMPONENT_INSTANCES("componentInstances"),
	COMPONENT_INSTANCES_PROPERTIES("componentInstancesProperties"), 
	CAPABILITIES("capabilities"), 
	REQUIREMENTS("requirements"),
	ALL_VERSIONS("allVersions"), 
	ADDITIONAL_INFORMATION("additionalInformation"),
	ARTIFACTS("artifacts"), 
	INTERFACES("interfaces"), 
	DERIVED_FROM("derivedFrom"),
	ATTRIBUTES("attributes"), 
	COMPONENT_INSTANCES_ATTRIBUTES("componentInstancesAttributes"),
	COMPONENT_INSTANCE_INPUTS("componentInstancesInputs"),
	COMPONENT_INSTANCE_RELATION("componentInstancesRelations"),
	DEPLOYMENT_ARTIFACTS("deploymentArtifacts"),
	TOSCA_ARTIFACTS("toscaArtifacts"),
	SERVICE_API_ARTIFACTS("serviceApiArtifacts"),
	METADATA("metadata"),
	CATEGORIES("categories"),
	INSTANCE_CAPABILTY_PROPERTIES("instanceCapabiltyProperties"); 
	
	
	private String value;
	
	private ComponentFieldsEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	
	public static ComponentFieldsEnum findByValue(String value) {
		ComponentFieldsEnum ret = null;
		for (ComponentFieldsEnum curr : ComponentFieldsEnum.values()) {
			if (curr.getValue().equals(value)) {
				ret = curr;
				return ret;
			}
		}
		return ret;
	}
}
