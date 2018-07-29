package org.openecomp.sdc.be.datamodel;

import java.util.Set;

public class ServiceRelations extends java.util.HashSet<NameIdPairWrapper> {

    public ServiceRelations() {
    }

    public ServiceRelations(Set<NameIdPairWrapper> relations) {
        addAll(relations);
    }

    public Set<NameIdPairWrapper> getRelations() {
        return this;
    }

    public void setRelations(Set<NameIdPairWrapper> relations) {
        clear();
        this.addAll(relations);
    }
}
