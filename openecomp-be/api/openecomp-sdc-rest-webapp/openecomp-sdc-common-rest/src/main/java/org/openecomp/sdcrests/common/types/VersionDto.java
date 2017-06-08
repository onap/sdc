package org.openecomp.sdcrests.common.types;

/**
 * Created by SVISHNEV on 3/5/2017.
 */
public class VersionDto {
    String id;
    String label;

    public VersionDto(){

    }

    public VersionDto(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
