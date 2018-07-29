package org.openecomp.sdc.be.model.normatives;

public class ToscaTypeMetadata {

    private String icon;
    private String displayName;

    public ToscaTypeMetadata() {
    }

    public ToscaTypeMetadata(String icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
