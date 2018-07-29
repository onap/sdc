package org.openecomp.sdc.be.components.upgrade;

public class UpgradeRequest {
    private String serviceId;
    private String resourceId;
     
    public UpgradeRequest(){
        
    }
    public UpgradeRequest(String serviceId ){
        this.serviceId = serviceId;
    }
    public UpgradeRequest(String serviceId, String resourceId ){
        this.serviceId = serviceId;
        this.resourceId = resourceId;
    }
    
    public String getServiceId() {
        return serviceId;
    }
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    

}
