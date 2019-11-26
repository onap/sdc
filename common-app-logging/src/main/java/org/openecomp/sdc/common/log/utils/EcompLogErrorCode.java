package org.openecomp.sdc.common.log.utils;


public enum EcompLogErrorCode {
    E_399("Internal Invalid Object. Description: %s"),
    E_210(
            "Connection problem towards U-EB server. Reason: %s");


    String description;
    String resolution;

    EcompLogErrorCode(String description, String resolution) {
        this.description = description;
        this.resolution = resolution;
    }

    EcompLogErrorCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    }
