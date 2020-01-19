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

package org.openecomp.sdc.be.components.merge;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RelationsComparator {

    /**
     *
     * @param oldResource the old resource
     * @param newResource the new resource
     * @return true if there was a change in one of the relations between the old and new resource
     * a change in relation is determine by comparing the relations type, node, capability and requirement name
     */
    public boolean isRelationsChanged(Resource oldResource,  Resource newResource) {
        Map<String, List<RequirementCapabilityRelDef>> oldRelationsByInstance = oldResource.groupRelationsFromCsarByInstanceName(oldResource);
        Map<String, List<RequirementCapabilityRelDef>> newRelationsByInstance = newResource.groupRelationsFromCsarByInstanceName(newResource);
        for (Map.Entry<String, List<RequirementCapabilityRelDef>> relationByInst : newRelationsByInstance.entrySet()) {
            List<RequirementCapabilityRelDef> oldRelations = oldRelationsByInstance.get(relationByInst.getKey());
            List<RequirementCapabilityRelDef> newRelations = relationByInst.getValue();
            if (isInstanceRelationsChanged(oldResource, oldRelations, newResource, newRelations)) {
                return true;
            }
        }
        return false;

    }

    private boolean isInstanceRelationsChanged(Resource oldResource, List<RequirementCapabilityRelDef> oldRelations, Resource newResource, List<RequirementCapabilityRelDef> newRelations) {
        if (oldRelations == null || oldRelations.size() != newRelations.size()){
            return true;
        }
        return newRelations.stream().anyMatch(newRelation -> !findRelation(oldResource, oldRelations, newResource, newRelation));
    }



    private boolean findRelation(Resource oldResource, List<RequirementCapabilityRelDef> oldRelations, Resource newResource, RequirementCapabilityRelDef newRelation) {
        for (RequirementCapabilityRelDef oldRelation : oldRelations) {
            RelationshipInfo oldRelationship = oldRelation.resolveSingleRelationship().getRelation();
            RelationshipInfo newRelationship = newRelation.resolveSingleRelationship().getRelation();
            if (oldRelationship != null && newRelationship != null && isRelationEqual(oldRelationship, newRelationship) && isRelationToNodeEquals(oldResource, oldRelation, newResource, newRelation)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRelationToNodeEquals(Resource oldResource, RequirementCapabilityRelDef oldRelation, Resource newResource, RequirementCapabilityRelDef newRelation) {
        String oldToNodeId = oldRelation.getToNode();
        String newToNodeId = newRelation.getToNode();
        Optional<ComponentInstance> oldRelationToNode = oldResource.getComponentInstanceById(oldToNodeId);
        Optional<ComponentInstance> newRelationToNode = newResource.getComponentInstanceById(newToNodeId);
        return oldRelationToNode.isPresent() && newRelationToNode.isPresent() && oldRelationToNode.get().getInvariantName().equals(newRelationToNode.get().getInvariantName());
    }

    private boolean isRelationEqual(RelationshipInfo oldRelationship, RelationshipInfo newRelationship) {
        return isRelationshipTypeEquals(oldRelationship, newRelationship) &&
               isRelationshipCapabilityEquals(oldRelationship, newRelationship) &&
               isRelationshipReqNameEquals(oldRelationship, newRelationship);
    }

    private boolean isRelationshipCapabilityEquals(RelationshipInfo oldRelationship, RelationshipInfo newRelationship) {
        if(oldRelationship.getCapabilityUid() !=null && newRelationship.getCapabilityUid() != null){
            return oldRelationship.getCapabilityUid().equals(newRelationship.getCapabilityUid());
        }
        else if(oldRelationship.getCapabilityUid() == null && newRelationship.getCapabilityUid() == null){
            return true;
        }
        return false;
    }

    private boolean isRelationshipTypeEquals(RelationshipInfo oldRelationship, RelationshipInfo newRelationship) {
        return oldRelationship.getRelationship().getType().equals(newRelationship.getRelationship().getType());
    }

    private boolean isRelationshipReqNameEquals(RelationshipInfo oldRelationship, RelationshipInfo newRelationship) {
        if(oldRelationship.getRequirement() != null && newRelationship.getRequirement() != null){
            return oldRelationship.getRequirement().equals(newRelationship.getRequirement());
        }
        else if(oldRelationship.getRequirement() == null && newRelationship.getRequirement() == null){
            return true;
        }
        return false;
    }

}
