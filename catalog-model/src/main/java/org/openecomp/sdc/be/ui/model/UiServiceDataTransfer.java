package org.openecomp.sdc.be.ui.model;

import java.util.Map;

import org.openecomp.sdc.be.model.ArtifactDefinition;

public class UiServiceDataTransfer extends UiComponentDataTransfer {
	

	private Map<String, ArtifactDefinition> serviceApiArtifacts;

	private UiServiceMetadata metadata;
	
	public UiServiceMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(UiServiceMetadata metadata) {
		this.metadata = metadata;
	}

	public Map<String, ArtifactDefinition> getServiceApiArtifacts() {
		return serviceApiArtifacts;
	}

	public void setServiceApiArtifacts(Map<String, ArtifactDefinition> serviceApiArtifacts) {
		this.serviceApiArtifacts = serviceApiArtifacts;
	}
}
