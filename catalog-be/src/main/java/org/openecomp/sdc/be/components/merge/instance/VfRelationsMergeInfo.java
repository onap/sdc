package org.openecomp.sdc.be.components.merge.instance;

import java.util.List;

public class VfRelationsMergeInfo {
    private List<RelationMergeInfo> fromRelationsInfo;
    private List<RelationMergeInfo> toRelationsInfo;

    public VfRelationsMergeInfo(List<RelationMergeInfo> fromRelationsInfo, List<RelationMergeInfo> toRelationsInfo) {
        this.fromRelationsInfo = fromRelationsInfo;
        this.toRelationsInfo = toRelationsInfo;
    }

    public List<RelationMergeInfo> getFromRelationsInfo() {
        return fromRelationsInfo;
    }

    public void setFromRelationsInfo(List<RelationMergeInfo> fromRelationsInfo) {
        this.fromRelationsInfo = fromRelationsInfo;
    }

    public List<RelationMergeInfo> getToRelationsInfo() {
        return toRelationsInfo;
    }

    public void setToRelationsInfo(List<RelationMergeInfo> toRelationsInfo) {
        this.toRelationsInfo = toRelationsInfo;
    }

}