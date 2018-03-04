package org.openecomp.sdc.be.info;

import java.util.ArrayList;
import java.util.List;

public class RelationshipList {
    private List<Relationship> relationship;

    public List<Relationship> getRelationship() {
        if(relationship == null) {
            relationship = new ArrayList<>();
        }
        return relationship;
    }

    public void setRelationship(List<Relationship> relationship) {
        this.relationship = relationship;
    }

}
