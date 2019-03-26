package org.openecomp.sdc.be.info;

import java.util.List;

public class GenericArtifactQueryInfo {

    private List<String> fields;
    private String parentId;
    private String artifactUniqueId;

    public GenericArtifactQueryInfo() {
    }

    public GenericArtifactQueryInfo(List<String> fields, String parentId, String artifactUniqueId) {
        this.fields = fields;
        this.parentId = parentId;
        this.artifactUniqueId = artifactUniqueId;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setArtifactUniqueId(String artifactUniqueId) {
        this.artifactUniqueId = artifactUniqueId;
    }

    public List<String> getFields() {
        return fields;
    }

    public String getParentId() {
        return parentId;
    }

    public String getArtifactUniqueId() {
        return artifactUniqueId;
    }
}
