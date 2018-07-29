package org.openecomp.sdc.ci.tests.tosca.datatypes;

import java.util.ArrayList;
import java.util.List;

public class ToscaTypesDefinition {

    private String tosca_definitions_version;
    private List<String> imports = new ArrayList<>();

    public String getTosca_definitions_version() {
        return tosca_definitions_version;
    }

    public void setTosca_definitions_version(String tosca_definitions_version) {
        this.tosca_definitions_version = tosca_definitions_version;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }
}
