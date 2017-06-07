package org.openecomp.sdc.be.model.jsontitan.datamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.slf4j.MDC;

public abstract class ToscaElement {
	
	protected Map<String, Object> metadata;
	protected List<CategoryDefinition> categories;
	protected Map<String, ArtifactDataDefinition> toscaArtifacts;
	private Map<String, ArtifactDataDefinition> artifacts;
	private Map<String, ArtifactDataDefinition> deploymentArtifacts;
	private Map<String, AdditionalInfoParameterDataDefinition> additionalInformation;
	private Map<String, PropertyDataDefinition> properties;
	
	protected ToscaElementTypeEnum toscaType;
	// User
	private String creatorUserId;
	private String creatorFullName;
	private String lastUpdaterUserId;
	private String lastUpdaterFullName;

	private Map<String, String> allVersions;
	
	public ToscaElement(ToscaElementTypeEnum toscaType){
		this.toscaType = toscaType;
	}
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}
	public Map<String, ArtifactDataDefinition> getToscaArtifacts() {
		return toscaArtifacts;
	}

	public void setToscaArtifacts(Map<String, ArtifactDataDefinition> toscaArtifacts) {
		this.toscaArtifacts = toscaArtifacts;
	}

	public ToscaElementTypeEnum getToscaType() {
		return toscaType;
	}

	public void setToscaType(ToscaElementTypeEnum toscaType) {
		this.toscaType = toscaType;
	}
	public Map<String, ArtifactDataDefinition> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Map<String, ArtifactDataDefinition> artifacts) {
		this.artifacts = artifacts;
	}

	public Map<String, ArtifactDataDefinition> getDeploymentArtifacts() {
		return deploymentArtifacts;
	}

	public void setDeploymentArtifacts(Map<String, ArtifactDataDefinition> deploymentArtifacts) {
		this.deploymentArtifacts = deploymentArtifacts;
	}
	public Map<String, AdditionalInfoParameterDataDefinition> getAdditionalInformation() {
		return additionalInformation;
	}
	public void setAdditionalInformation(Map<String, AdditionalInfoParameterDataDefinition> additionalInformation) {
		this.additionalInformation = additionalInformation;
	}
	public Map<String, PropertyDataDefinition> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, PropertyDataDefinition> properties) {
		this.properties = properties;
	}

	public Map<String, String> getAllVersions() {
		return allVersions;
	}

	public void setAllVersions(Map<String, String> allVersions) {
		this.allVersions = allVersions;
	}

	// metadata properties
	// ----------------------------
	public Object getMetadataValue(JsonPresentationFields name) {
		return getMetadataValueOrDefault(name, null);
	}

	public Object getMetadataValueOrDefault(JsonPresentationFields name, Object defaultVal) {
		if (metadata != null) {
			return metadata.getOrDefault(name.getPresentation(), defaultVal);
		}
		return null;
	}

	public void setMetadataValue(JsonPresentationFields name, Object value) {
		if (metadata == null) {
			metadata = new HashMap<String, Object>();
		}
		metadata.put(name.getPresentation(), value);

	}
	// --------------------
	public String getUUID() {
		return (String) getMetadataValue(JsonPresentationFields.UUID);
	}

	public void setUUID(String uuid) {
		setMetadataValue(JsonPresentationFields.UUID, uuid);
	}

	public String getVersion() {
		return (String) getMetadataValue(JsonPresentationFields.VERSION);
	}

	public String getNormalizedName() {
		return (String) getMetadataValue(JsonPresentationFields.NORMALIZED_NAME);
	}

	public void setNormalizedName(String normaliseComponentName) {
		setMetadataValue(JsonPresentationFields.NORMALIZED_NAME, normaliseComponentName);
	}

	public String getName() {
		return (String) getMetadataValue(JsonPresentationFields.NAME);
	}

	public String getSystemName() {
		return (String) getMetadataValue(JsonPresentationFields.SYSTEM_NAME);
	}
	public void setSystemName(String systemName) {
		setMetadataValue(JsonPresentationFields.SYSTEM_NAME, systemName);
	}

	public void setLifecycleState(LifecycleStateEnum state) {
		if(state != null)
			setMetadataValue(JsonPresentationFields.LIFECYCLE_STATE, state.name());
	}

	public LifecycleStateEnum getLifecycleState() {
		return LifecycleStateEnum.findState( (String) getMetadataValue(JsonPresentationFields.LIFECYCLE_STATE));
	}

	public Long getCreationDate() {
		return (Long) getMetadataValue(JsonPresentationFields.CREATION_DATE);
	}

	public void setCreationDate(Long currentDate) {
		setMetadataValue(JsonPresentationFields.CREATION_DATE, currentDate);
	}

	public void setLastUpdateDate(Long currentDate) {
		setMetadataValue(JsonPresentationFields.LAST_UPDATE_DATE, currentDate);
	}
	public Long getLastUpdateDate() {
		return (Long) getMetadataValue(JsonPresentationFields.LAST_UPDATE_DATE);
	}

	public String getUniqueId() {
		return (String) getMetadataValue(JsonPresentationFields.UNIQUE_ID);
	}
	public void setUniqueId(String uniqueId) {
		 setMetadataValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
	}

	public void setHighestVersion(Boolean isHighest) {
		 setMetadataValue(JsonPresentationFields.HIGHEST_VERSION, isHighest);
		
	}
	public Boolean isHighestVersion() {
		return (Boolean) getMetadataValue(JsonPresentationFields.HIGHEST_VERSION);
		
	}
	public ResourceTypeEnum getResourceType() {
		String resourceType = (String) getMetadataValue(JsonPresentationFields.RESOURCE_TYPE);
		return resourceType != null ? ResourceTypeEnum.valueOf(resourceType) : null;
	}

	public void setResourceType(ResourceTypeEnum resourceType) {
		if(resourceType != null)
			setMetadataValue(JsonPresentationFields.RESOURCE_TYPE, resourceType.name());
	}
	
	public ComponentTypeEnum getComponentType() {
		return ComponentTypeEnum.valueOf((String) getMetadataValue(JsonPresentationFields.COMPONENT_TYPE));
	}

	public void setComponentType(ComponentTypeEnum componentType) {
		if(componentType != null)
			setMetadataValue(JsonPresentationFields.COMPONENT_TYPE, componentType.name());
	}
	
	public String getDerivedFromGenericType(){
		return (String) getMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_TYPE);
	}
	
	public void setDerivedFromGenericType(String derivedFromGenericType){
		setMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_TYPE, derivedFromGenericType);
	}
	
	public String getDerivedFromGenericVersion(){
		return (String) getMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_VERSION);
	}
	
	public void setDerivedFromGenericVersion(String derivedFromGenericVersion){
		setMetadataValue(JsonPresentationFields.DERIVED_FROM_GENERIC_VERSION, derivedFromGenericVersion);
	}


	public String getCreatorUserId() {
		return creatorUserId;
	}

	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}

	public String getCreatorFullName() {
		return creatorFullName;
	}

	public void setCreatorFullName(String creatorFullName) {
		this.creatorFullName = creatorFullName;
	}

	public String getLastUpdaterUserId() {
		return lastUpdaterUserId;
	}

	public void setLastUpdaterUserId(String lastUpdaterUserId) {
		this.lastUpdaterUserId = lastUpdaterUserId;
	}

	public String getLastUpdaterFullName() {
		return lastUpdaterFullName;
	}

	public void setLastUpdaterFullName(String lastUpdaterFullName) {
		this.lastUpdaterFullName = lastUpdaterFullName;
	}

	public void generateUUID() {
		String prevUUID = getUUID();
		String version = getVersion();
		if ((prevUUID == null && NodeTypeOperation.uuidNormativeNewVersion.matcher(version).matches()) || NodeTypeOperation.uuidNewVersion.matcher(version).matches()) {
			UUID uuid = UUID.randomUUID();
			setUUID(uuid.toString());
			MDC.put("serviceInstanceID", uuid.toString());
		}
	}

}
