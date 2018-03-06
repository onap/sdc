package org.openecomp.sdc.be.model.catalog;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public class CatalogComponent {

    private String version;
    private ComponentTypeEnum componentType;
    private String icon;
    private String uniqueId;
    private String lifecycleState;
    private long lastUpdateDate;
    private String name;
    private String resourceType;
    private String categoryNormalizedName;
    private String subCategoryNormalizedName;
    private String distributionStatus;

    public String getCategoryNormalizedName() {
        return categoryNormalizedName;
    }

    public void setCategoryNormalizedName(String categoryNormalizedName) {
        this.categoryNormalizedName = categoryNormalizedName;
    }

    public String getSubCategoryNormalizedName() {
        return subCategoryNormalizedName;
    }

    public void setSubCategoryNormalizedName(String subCategoryNormalizedName) {
        this.subCategoryNormalizedName = subCategoryNormalizedName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setComponentType(ComponentTypeEnum componentType) {
        this.componentType = componentType;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getVersion() {
        return version;
    }

    public ComponentTypeEnum getComponentType() {
        return componentType;
    }

    public String getIcon() {
        return icon;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

	public String getDistributionStatus() {
		return distributionStatus;
	}

	public void setDistributionStatus(String distributionStatus) {
		this.distributionStatus = distributionStatus;
	}
    
}
