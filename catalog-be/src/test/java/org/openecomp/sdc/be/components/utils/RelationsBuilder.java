package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

import java.util.ArrayList;
import java.util.Collections;

public class RelationsBuilder {

    private RequirementCapabilityRelDef relation;

    public RelationsBuilder() {
        relation = new RequirementCapabilityRelDef();
        RequirementAndRelationshipPair requirementAndRelationshipPair = new RequirementAndRelationshipPair();
        RelationshipImpl relationship = new RelationshipImpl();
        requirementAndRelationshipPair.setRelationships(relationship);
        relation.setRelationships(Collections.singletonList(requirementAndRelationshipPair));
    }

    public RelationsBuilder setFromNode(String fromNode) {
        relation.setFromNode(fromNode);
        return this;
    }

    public RelationsBuilder setRequirementName(String reqName) {
        relation.getSingleRelationship().setRequirement(reqName);
        return this;
    }

    public RelationsBuilder setRelationType(String type) {
        relation.getSingleRelationship().getRelationship().setType(type);
        return this;
    }

    public RelationsBuilder setCapabilityUID(String uid) {
        relation.getSingleRelationship().setCapabilityUid(uid);
        return this;
    }

    public RelationsBuilder setToNode(String toNode) {
        relation.setToNode(toNode);
        return this;
    }

    public RequirementCapabilityRelDef build() {
        return relation;
    }

}
