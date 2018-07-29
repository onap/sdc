package org.openecomp.sdc.be.components.impl.model;

import org.openecomp.sdc.be.model.normatives.ToscaTypeMetadata;

import java.util.Map;

public class ToscaTypeImportData {

    private String toscaTypesYml;
    private Map<String, ToscaTypeMetadata> toscaTypeMetadata;

    public ToscaTypeImportData(String toscaTypesYml, Map<String, ToscaTypeMetadata> toscaTypeMetadata) {
        this.toscaTypesYml = toscaTypesYml;
        this.toscaTypeMetadata = toscaTypeMetadata;
    }

    public String getToscaTypesYml() {
        return toscaTypesYml;
    }

    public Map<String, ToscaTypeMetadata> getToscaTypeMetadata() {
        return toscaTypeMetadata;
    }
}
