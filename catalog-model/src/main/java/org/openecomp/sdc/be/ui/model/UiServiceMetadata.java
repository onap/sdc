package org.openecomp.sdc.be.ui.model;

import java.util.List;

import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class UiServiceMetadata extends UiComponentMetadata {
	
	private String distributionStatus;
	private Boolean ecompGeneratedNaming;
	private String namingPolicy;

	public UiServiceMetadata(List<CategoryDefinition> categories, ServiceMetadataDataDefinition metadata) {
		super(categories, metadata);
		this.distributionStatus = metadata.getDistributionStatus();
		this.ecompGeneratedNaming = metadata.isEcompGeneratedNaming();
		this.namingPolicy = metadata.getNamingPolicy();
	}	
	
	public String getDistributionStatus() {
		return distributionStatus;
	}

	public void setDistributionStatus(String distributionStatus) {
		this.distributionStatus = distributionStatus;
	}
	
	public Boolean getEcompGeneratedNaming() {
		return ecompGeneratedNaming;
	}

	public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
		this.ecompGeneratedNaming = ecompGeneratedNaming;
	}

	public String getNamingPolicy() {
		return namingPolicy;
	}

	public void setNamingPolicy(String namingPolicy) {
		this.namingPolicy = namingPolicy;
	}

}
