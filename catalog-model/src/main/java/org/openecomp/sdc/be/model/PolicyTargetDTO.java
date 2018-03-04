package org.openecomp.sdc.be.model;

import java.util.List;

public class PolicyTargetDTO {


    private String type;
    private List<String> uniqueIds;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getUniqueIds() {
        return uniqueIds;
    }

    public void setUniqueIds(List<String> ids) {
        this.uniqueIds = ids;
    }





}
