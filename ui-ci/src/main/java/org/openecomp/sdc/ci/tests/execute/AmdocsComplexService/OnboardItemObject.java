package org.openecomp.sdc.ci.tests.execute.AmdocsComplexService;


public class OnboardItemObject extends OnboardItemObjectReqDetails {

    private String itemId;
    private String name;
    private String baseId;
    private String status;

    public OnboardItemObject(){super();}

    public OnboardItemObject(String itemId, String name, String baseId, String status) {
        this.itemId = itemId;
        this.name = name;
        this.baseId = baseId;
        this.status = status;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String itemId) {
        this.name = name;
    }

    public String getBaseId() {
        return baseId;
    }

    public void setBaseId(String baseId) {
        this.baseId = baseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OnboardItemObject{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", baseId='" + baseId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
