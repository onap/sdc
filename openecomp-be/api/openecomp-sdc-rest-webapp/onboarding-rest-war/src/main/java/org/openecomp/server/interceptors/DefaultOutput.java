package org.openecomp.server.interceptors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultOutput implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int status;

    private final Object entity;

    private Map<String, Object> metadata = new HashMap<>();

    public DefaultOutput(int status, Object entity) {
        this.status = status;
        this.entity = entity;
    }

    @JsonProperty("status")
    public int getStatus() {
        return status;
    }

    @JsonProperty("entity")
    public Object getEntity() {
        return entity;
    }

    @JsonProperty("metadata")
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(Map<String, ?> metadataMap) {
        if (metadataMap != null) {
            metadataMap.forEach((key, valueList) -> {
                if (valueList instanceof Iterable) {
                    for (Object val : (Iterable<?>) valueList) {
                        this.metadata.put(key, val);
                        break; // only pick first value
                    }
                } else {
                    this.metadata.put(key, valueList);
                }
            });
        }
    }
}
