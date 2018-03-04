package org.openecomp.sdc.be.datamodel;

import java.io.Serializable;
import java.util.Set;

public class ServiceRelations extends java.util.HashSet<NameIdPairWrapper> implements Serializable {



    public ServiceRelations() {
    }

    public ServiceRelations(Set<NameIdPairWrapper> relations) {
        super();
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
