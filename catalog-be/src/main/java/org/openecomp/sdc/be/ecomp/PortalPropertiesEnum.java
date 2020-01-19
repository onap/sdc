package org.openecomp.sdc.be.ecomp;

public enum PortalPropertiesEnum {
    APP_NAME("portal_app_name"),
    ECOMP_REST_URL("ecomp_rest_url"),
    PASSWORD("portal_pass"),
    UEB_APP_KEY("ueb_app_key"),
    USER("portal_user");

    private final String value;

    PortalPropertiesEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}