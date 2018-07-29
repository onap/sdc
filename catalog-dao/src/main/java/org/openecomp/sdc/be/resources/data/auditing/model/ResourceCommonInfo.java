package org.openecomp.sdc.be.resources.data.auditing.model;

public class ResourceCommonInfo {

    private String resourceName;
    private String resourceType;

    public ResourceCommonInfo(){}

    public ResourceCommonInfo(String resourceName, String resourceType) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
    }

    public ResourceCommonInfo(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

}
