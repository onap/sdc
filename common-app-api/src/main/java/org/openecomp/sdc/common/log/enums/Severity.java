package org.openecomp.sdc.common.log.enums;

/**
 * Created by dd4296 on 12/14/2017.
 */
public enum Severity {
    OK(0),
    WARNING(1),
    CRITICAL(2),
    DOWN(3),
    UNREACHABLE(4);

    int severityType;

    Severity(int serveryType) {
        this.severityType = serveryType;
    }

    public int getSeverityType() {
        return severityType;
    }
}

