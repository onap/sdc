package org.openecomp.sdc.be.info;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class RelationshipData {

    @JsonProperty("relationship-key")
    private String relationshipKey;

    @JsonProperty("relationship-value")
    private String relationshipValue;

    public void setRelationshipKey(String relationshipKey) {
        this.relationshipKey = relationshipKey;
    }

    public String getRelationshipKey() {
        return this.relationshipKey;
    }

    public void setRelationshipValue(String relationshipValue) {
        this.relationshipValue = relationshipValue;
    }

    public String getRelationshipValue() {
        return this.relationshipValue;
    }
}
