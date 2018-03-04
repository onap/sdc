package org.openecomp.sdc.be.info;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class RelatedToProperty {

    @JsonProperty("property-key")
    private String propertyKey;

    @JsonProperty("property-value")
    private String propertyValue;

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
