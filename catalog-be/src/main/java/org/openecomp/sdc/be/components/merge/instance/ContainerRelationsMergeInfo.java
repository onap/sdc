package org.openecomp.sdc.be.components.merge.instance;

import java.util.List;

public class ContainerRelationsMergeInfo {
    private List<RelationMergeInfo> fromRelationsInfo;
    private List<RelationMergeInfo> toRelationsInfo;

    ContainerRelationsMergeInfo(List<RelationMergeInfo> fromRelationsInfo, List<RelationMergeInfo> toRelationsInfo) {
        this.fromRelationsInfo = fromRelationsInfo;
        this.toRelationsInfo = toRelationsInfo;
    }

    public List<RelationMergeInfo> getFromRelationsInfo() {
        return fromRelationsInfo;
    }

    public List<RelationMergeInfo> getToRelationsInfo() {
        return toRelationsInfo;
    }

}