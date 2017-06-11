package org.openecomp.sdc.be.ui.model;

import java.util.List;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class UiResourceMetadata extends UiComponentMetadata {
	
	private String vendorName;
	private String vendorRelease;
	private ResourceTypeEnum resourceType = ResourceTypeEnum.VFC;
	private Boolean isAbstract;
	private String cost;
	private String licenseType;
	private String toscaResourceName;
	private List<String> derivedFrom;
	
	
	public UiResourceMetadata(List<CategoryDefinition> categories, List<String> derivedFrom, ResourceMetadataDataDefinition metadata) {
		super(categories, metadata);
		this.vendorName = metadata.getVendorName();
		this.vendorRelease = metadata.getVendorRelease();
		this.resourceType = metadata.getResourceType();
		this.cost = metadata.getCost();
		this.licenseType = metadata.getLicenseType();
		this.toscaResourceName = metadata.getToscaResourceName();
		this.derivedFrom = derivedFrom;
	}
	
	public UiResourceMetadata(){}
	
	public List<String> getDerivedFrom() {
		return derivedFrom;
	}


	public void setDerivedFrom(List<String> derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	
	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorRelease() {
		return vendorRelease;
	}

	public void setVendorRelease(String vendorRelease) {
		this.vendorRelease = vendorRelease;
	}

	public ResourceTypeEnum getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceTypeEnum resourceType) {
		this.resourceType = resourceType;
	}

	public Boolean getIsAbstract() {
		return isAbstract;
	}

	public void setIsAbstract(Boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}

	public String getToscaResourceName() {
		return toscaResourceName;
	}

	public void setToscaResourceName(String toscaResourceName) {
		this.toscaResourceName = toscaResourceName;
	}



}



