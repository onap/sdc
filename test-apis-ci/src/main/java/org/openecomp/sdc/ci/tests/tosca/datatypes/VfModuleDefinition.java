package org.openecomp.sdc.ci.tests.tosca.datatypes;

import java.io.Serializable;
import java.util.List;

public class VfModuleDefinition extends ToscaGroupsTopologyTemplateDefinition implements Serializable {

    private List<String> artifacts;

    public static final long serialVersionUID = -6373756459967949586L;

    public List<String> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<String> artifacts) {
        this.artifacts = artifacts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VfModuleDefinition)) return false;
        if (!super.equals(o)) return false;

        VfModuleDefinition that = (VfModuleDefinition) o;

        return artifacts != null ? artifacts.containsAll(that.artifacts) : that.artifacts == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (artifacts != null ? artifacts.hashCode() : 0);
        return result;
    }
}
