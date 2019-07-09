/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

import java.util.Collections;

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
        relation.resolveSingleRelationship().getRelation().setRequirement(reqName);
        return this;
    }

    public RelationsBuilder setRelationType(String type) {
        relation.resolveSingleRelationship().getRelation().getRelationship().setType(type);
        return this;
    }

    public RelationsBuilder setCapabilityUID(String uid) {
        relation.resolveSingleRelationship().getRelation().setCapabilityUid(uid);
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
