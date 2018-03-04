package org.openecomp.sdc.be.components.merge.instance;

import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

public class RelationMergeInfo {
    private String capReqType;
    private String vfcInstanceName;
    private RequirementCapabilityRelDef relDef;
    private String capReqName;

    public RelationMergeInfo(String capReqType, String capReqName, String vfcInstanceName, RequirementCapabilityRelDef relDef) {
        this.capReqType = capReqType;
        this.capReqName = capReqName;
        this.vfcInstanceName = vfcInstanceName;
        this.relDef = relDef;
    }

    public String getCapReqType() {
        return capReqType;
    }

    public void setCapReqType(String type) {
        this.capReqType = type;
    }

    public String getVfcInstanceName() {
        return vfcInstanceName;
    }

    public void setVfcInstanceName(String vfcInstanceName) {
        this.vfcInstanceName = vfcInstanceName;
    }

    public RequirementCapabilityRelDef getRelDef() {
        return relDef;
    }

    public void setRelDef(RequirementCapabilityRelDef relDef) {
        this.relDef = relDef;
    }

    public String getCapReqName() {
        return capReqName;
    }

    public void setCapReqName(String capReqName) {
        this.capReqName = capReqName;
    }
}