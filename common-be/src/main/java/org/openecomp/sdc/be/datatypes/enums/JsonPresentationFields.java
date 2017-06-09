package org.openecomp.sdc.be.datatypes.enums;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;


//@JsonDeserialize(using = MyDeserializer.class)

public enum JsonPresentationFields {
	UNIQUE_ID						("uniqueId", 				GraphPropertyEnum.UNIQUE_ID), 
	HIGHEST_VERSION					("highestVersion",			GraphPropertyEnum.IS_HIGHEST_VERSION),
	LIFECYCLE_STATE					("lifecycleState", 			GraphPropertyEnum.STATE), 
	CREATION_DATE					("creationDate", 			null), 
	LAST_UPDATE_DATE				("lastUpdateDate", 			null), 
	SYSTEM_NAME						("systemName",				GraphPropertyEnum.SYSTEM_NAME), 
	NAME							("name", 					GraphPropertyEnum.NAME), 
	VERSION							("version", 				GraphPropertyEnum.VERSION), 
	NORMALIZED_NAME					("normalizedName", 			GraphPropertyEnum.NORMALIZED_NAME),
	UUID							("UUID", 					GraphPropertyEnum.UUID), 
	RESOURCE_TYPE					("resourceType", 			GraphPropertyEnum.RESOURCE_TYPE),
	COMPONENT_TYPE					("componentType", 			GraphPropertyEnum.COMPONENT_TYPE),
	IS_DELETED						("isDeleted", 				GraphPropertyEnum.IS_DELETED),
	ECOMP_GENERATED_NAMING          ("ecompGeneratedNaming",    null),
	NAMING_POLICY                   ("namingPolicy",            null),
	TOSCA_RESOURCE_NAME				("toscaResourceName", 		GraphPropertyEnum.TOSCA_RESOURCE_NAME),
	DESCRIPTION						("description",				null),
	TYPE							("type",					null),
	DERIVED_FROM					("derivedFrom", 			null),
	VENDOR_NAME						("vendorName",				null),
	VENDOR_RELEASE					("vendorRelease",			null),
	CONFORMANCE_LEVEL				("conformanceLevel",		null),
	ICON							("icon",					null),
	TAGS							("tags",					null),
	INVARIANT_UUID					("invariantUuid", 			GraphPropertyEnum.INVARIANT_UUID),
	CSAR_UUID						("csarUuid",				GraphPropertyEnum.CSAR_UUID),
	CSAR_VERSION					("csarVersion",				null),
	IMPORTED_TOSCA_CHECKSUM			("importedToscaChecksum",	null),
	CONTACT_ID						("contactId",				null),
	PROJECT_CODE					("projectCode", 			null),
	DISTRIBUTION_STATUS				("distributionStatus", 		GraphPropertyEnum.DISTRIBUTION_STATUS),
	DERIVED_FROM_GENERIC_TYPE("derivedFromGenericType", null),
	DERIVED_FROM_GENERIC_VERSION("derivedFromGenericVersion", null),

	////Artifact
	ARTIFACT_TYPE					("artifactType", 			null),
	ARTIFACT_REF					("artifactRef", 			null),
	ARTIFACT_REPOSITORY				("artifactRepository", 		null),
	ARTIFACT_CHECKSUM				("artifactChecksum", 		null),
	ARTIFACT_CREATOR 				("artifactCreator", 		null),
	USER_ID_CREATOR 				("userIdCreator", 			null),
	USER_ID_LAST_UPDATER 			("userIdLastUpdater", 		null),
	CREATOR_FULL_NAME				("creatorFullName",			null),
	UPDATER_FULL_NAME				("updaterFullName", 		null),
	
	ES_ID 							("esId", 					null),
	ARTIFACT_LABEL					("artifactLabel", 			null),
	IS_ABSTRACT 					("mandatory", 				null),
	ARTIFACT_DISPLAY_NAME			("artifactDisplayName", 	null),
	API_URL 						("apiUrl", 					null),
	SERVICE_API 					("serviceApi", 				null),
	ARTIFACT_VERSION 				("artifactVersion", 		null),
	ARTIFACT_UUID 					("artifactUUID", 			null),
	PAYLOAD_UPDATE_DATE				("payloadUpdateDate", 		null),
	HEAT_PARAMS_UPDATE_DATE 		("heatParamsUpdateDate",	null),
	GENERATED 						("generated", 				null),
	ARTIFACT_GROUP_TYPE 			("artifactGroupType", 		null),
	ARTIFACT_TIMEOUT				("timeout",					null),
	REQUIRED_ARTIFACTS				("requiredArtifacts",		null),
	DUPLICATED 						("duplicated", 				null),
	HEAT_PARAMETERS 				("heatParameters", 			null),
	GENERATED_FROM_ID 		("generatedFromId", 		null),
	
	
	// end artifacts
	
	
	//property
	DEFINITION						("definition", 					null),
	DEFAULT_VALUE					("defaultValue", 				null),
	REQUIRED						("required", 					null),
	PASSWORD						("password", 					null),
	CONSTRAINTS						("constraints", 				null),
	PROPERTIES						("properties", 					null),
	PROPERTY						("property", 					null),
	SCHEMA							("schema", 						null),
	VALUE							("value", 						null),
	PARENT_UNIQUE_ID				("parentUniqueId", 				null),
	
	COMPONENT_INSTANCES				("componentInstances", 			null),
	RELATIONS						("relations", 					null),
	
	
	//capability
	VALID_SOURCE_TYPE				("validSourceTypes", 			null),
	CREATION_TIME				("creationTime", 				null),
	MODIFICATION_TIME				("modificationTime", 			null),
	CAPABILITY_SOURCES				("capabilitySources", 			null),
	MAX_OCCURRENCES					("maxOccurrences", 				null),
	MIN_OCCURRENCES					("minOccurrences", 				null),
	OWNER_NAME						("ownerName", 					null),
	OWNER_ID						("ownerId", 					null),
	LEFT_OCCURRENCES				("leftOccurences",				null),	
	CAPABILITY_ID					("capabiltyId", 				null),
	PATH							("path", 						null),
	SOURCE							("source", 						null),
	
	//Requirement
	CAPAPILITY						("capability", 					null),
	NODE							("node", 						null),		
	RELATIONSHIP					("relationship", 				null),
	VALID_SOURCE_TYPES				("validSourceTypes", 			null),
	REQUIREMENT_ID					("requirementId", 				null),
	
	//Relation
	CAPABILTY_OWNER_ID				("capabilityOwnerId", 			null),
	REQUIREMENT_OWNER_ID			("requirementOwnerId", 			null),
	FROM_ID							("fromId", 						null),
	TO_ID							("toId", 						null),
	REQUIREMENT						("requirement",					null),
		
	//Groups

	GROUP_INVARIANT_UUID			("invariantUUID",				null), 
	GROUP_UUID						("groupUUID",					null), 	
	GROUP_MEMBER					("members",						null), 
	GROUP_ARTIFACTS					("artifacts",					null),
	GROUP_ARTIFACTS_UUID			(" artifactsUuid",				null),
	GROUP_PROPERTIES				(" properties",					null),
	GROUP_UNIQUE_ID					(" groupUid",					null),
	POS_X							(" posX",						null),
	POS_Y							(" posY",						null),
	PROPERTY_VALUE_COUNTER			(" propertyValueCounter",		null),
	CUSTOMIZATION_UUID				(" customizationUUID",			null),
	GROUP_NAME						(" groupName",					null),
	GROUP_INSTANCE_ARTIFACTS		(" groupInstanceArtifacts",		null),
	GROUP_INSTANCE_ARTIFACTS_UUID	(" groupInstanceArtifactsUuid",	null),
	GROUP_INSTANCE_PROPERTIES		(" groupInstancesProperties",	null),

	;
	

	private String presentation;
	private GraphPropertyEnum storedAs;

	JsonPresentationFields(String presentation, GraphPropertyEnum storedAs) {
		this.presentation = presentation;
		this.storedAs = storedAs;
	}

	@JsonValue
	public String getPresentation() {
		return presentation;
	}

	public void setPresentation(String presentation) {
		this.presentation = presentation;
	}

	public GraphPropertyEnum getStoredAs() {
		return storedAs;
	}
	
	public void setStoredAs(GraphPropertyEnum storedAs) {
		this.storedAs = storedAs;
	}

	public static String getPresentationByGraphProperty(GraphPropertyEnum property) {
		for(JsonPresentationFields currPresentation : JsonPresentationFields.values()){
			if(currPresentation.getStoredAs() == property){
				return currPresentation.getPresentation();
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return presentation;
	}

	@JsonCreator
	public static JsonPresentationFields getByPresentation(String presentation) {
		for (JsonPresentationFields inst : JsonPresentationFields.values()) {
			if (inst.getPresentation().equals(presentation)) {
				return inst;
			}
		}
		return null;
	}
	
}
