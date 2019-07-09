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

package org.openecomp.sdc.be.model;

import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
/**
 * Contains the Capability, Requirement and Relationship info
 */
public class CapabilityRequirementRelationship {

    private RelationshipInfo relation;
    private CapabilityDataDefinition capability;
    private RequirementDataDefinition requirement;

    public RelationshipInfo getRelation() {
        return relation;
    }
    public void setRelation(RelationshipInfo relation) {
        this.relation = relation;
    }
    public CapabilityDataDefinition getCapability() {
        return capability;
    }
    public void setCapability(CapabilityDataDefinition capability) {
        this.capability = capability;
    }
    public RequirementDataDefinition getRequirement() {
        return requirement;
    }
    public void setRequirement(RequirementDataDefinition requirement) {
        this.requirement = requirement;
    }
}
