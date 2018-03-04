package org.openecomp.sdc.asdctool.impl.validator.utils;

import java.util.List;
import java.util.Map;



public class VfModuleArtifactPayloadEx {
	
	private String vfModuleModelName, vfModuleModelInvariantUUID, vfModuleModelVersion, vfModuleModelUUID, vfModuleModelCustomizationUUID, vfModuleModelDescription;
	private Boolean isBase;
	private List<String> artifacts;
	private Map< String, Object> properties;



	public String getVfModuleModelName() {
		return vfModuleModelName;
	}

	public void setVfModuleModelName(String vfModuleModelName) {
		this.vfModuleModelName = vfModuleModelName;
	}

	public String getVfModuleModelInvariantUUID() {
		return vfModuleModelInvariantUUID;
	}

	public void setVfModuleModelInvariantUUID(String vfModuleModelInvariantUUID) {
		this.vfModuleModelInvariantUUID = vfModuleModelInvariantUUID;
	}

	public String getVfModuleModelVersion() {
		return vfModuleModelVersion;
	}

	public void setVfModuleModelVersion(String vfModuleModelVersion) {
		this.vfModuleModelVersion = vfModuleModelVersion;
	}

	public String getVfModuleModelUUID() {
		return vfModuleModelUUID;
	}

	public void setVfModuleModelUUID(String vfModuleModelUUID) {
		this.vfModuleModelUUID = vfModuleModelUUID;
	}

	public String getVfModuleModelCustomizationUUID() {
		return vfModuleModelCustomizationUUID;
	}

	public void setVfModuleModelCustomizationUUID(String vfModuleModelCustomizationUUID) {
		this.vfModuleModelCustomizationUUID = vfModuleModelCustomizationUUID;
	}

	public String getVfModuleModelDescription() {
		return vfModuleModelDescription;
	}

	public void setVfModuleModelDescription(String vfModuleModelDescription) {
		this.vfModuleModelDescription = vfModuleModelDescription;
	}

	public Boolean getIsBase() {
		return isBase;
	}

	public void setIsBase(Boolean isBase) {
		this.isBase = isBase;
	}

	public List<String> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<String> artifacts) {
		this.artifacts = artifacts;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	
	

}
