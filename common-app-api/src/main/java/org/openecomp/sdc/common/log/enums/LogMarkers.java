package org.openecomp.sdc.common.log.enums;

/**
 * Created by mm288v on 12/27/2017.
 * This enum reflects the Marker text in logback.xml file per each ecomp marker
 */
public enum LogMarkers {
    DEBUG_MARKER("DEBUG_MARKER"),
    ERROR_MARKER("ERROR_MARKER"),
    AUDIT_MARKER("AUDIT_MARKER"),
    METRIC_MARKER("METRICS");

    private String text;

    LogMarkers (String text){
        this.text = text;
    }

    public String text(){
        return text;
    }

}
