package org.openecomp.sdc.be.components.validation;

/**
 * Created by chaya on 11/14/2017.
 */
public enum ApiResourceEnum {

    ENVIRONMENT_ID("Environment ID"),
    RESOURCE_ID("Resource ID"),
    SERVICE_ID("Service ID");

    private String value;

    ApiResourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
