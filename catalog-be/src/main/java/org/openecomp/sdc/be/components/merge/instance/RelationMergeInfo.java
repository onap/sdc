package org.openecomp.sdc.be.components.merge.instance;

import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

public class RelationMergeInfo {
    private String capReqType;
    private String capOwnerName;
    private RequirementCapabilityRelDef relDef;
    private String capReqName;

    public RelationMergeInfo(String capReqType, String capReqName, String capOwnerName, RequirementCapabilityRelDef relDef) {
        this.capReqType = capReqType;
        this.capReqName = capReqName;
        this.capOwnerName = capOwnerName;
        this.relDef = relDef;
    }

    public String getCapReqType() {
        return capReqType;
    }

    public String getCapOwnerName() {
        return capOwnerName;
    }
    public RequirementCapabilityRelDef getRelDef() {
        return relDef;
    }

    public String getCapReqName() {
        return capReqName;
    }

}