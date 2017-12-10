package org.openecomp.sdc.be.components.utils;

import java.util.Collections;

import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

public class RelationsBuilder {

    private RequirementCapabilityRelDef relation;

    public RelationsBuilder() {
        relation = new RequirementCapabilityRelDef();
        RelationshipInfo requirementAndRelationshipPair = new RelationshipInfo();
        RelationshipImpl relationship = new RelationshipImpl();
        requirementAndRelationshipPair.setRelationships(relationship);
		CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
		capReqRel.setRelation(requirementAndRelationshipPair);
        relation.setRelationships(Collections.singletonList(capReqRel));
    }

    public RelationsBuilder setFromNode(String fromNode) {
        relation.setFromNode(fromNode);
        return this;
    }

    public RelationsBuilder setRequirementName(String reqName) {
        relation.getSingleRelationship().getRelation().setRequirement(reqName);
        return this;
    }

    public RelationsBuilder setRelationType(String type) {
        relation.getSingleRelationship().getRelation().getRelationship().setType(type);
        return this;
    }

    public RelationsBuilder setCapabilityUID(String uid) {
        relation.getSingleRelationship().getRelation().setCapabilityUid(uid);
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
