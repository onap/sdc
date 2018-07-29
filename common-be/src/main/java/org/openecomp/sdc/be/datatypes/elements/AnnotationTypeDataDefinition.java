package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class AnnotationTypeDataDefinition extends ToscaDataDefinition {

    protected String uniqueId;
    protected String type; 
    protected String description;

    protected Long creationTime;
    protected Long modificationTime;

    protected String version;
    protected boolean highestVersion;

    public AnnotationTypeDataDefinition() {}

    public AnnotationTypeDataDefinition(AnnotationTypeDataDefinition other) {
        uniqueId = other.uniqueId;
        type = other.type;
        version = other.version;
        description = other.description;
        creationTime = other.creationTime;
        modificationTime = other.modificationTime;
        highestVersion = other.highestVersion;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Long modificationTime) {
        this.modificationTime = modificationTime;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isHighestVersion() {
        return highestVersion;
    }

    public void setHighestVersion(boolean highestVersion) {
        this.highestVersion = highestVersion;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": [uniqueId=" + uniqueId + ", type=" + getType()
                + ", version=" + version + ", highestVersion=" + highestVersion
                + ", description=" + description
                + ", creationTime=" + creationTime + ", modificationTime=" + modificationTime + "]";
    }


}
