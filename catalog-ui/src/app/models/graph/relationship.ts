/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
'use strict';
import {Capability} from "../capability";
import {Requirement} from "../requirement";

export class RelationshipModel {
    fromNode:string;
    toNode:string;
    relationships:Array<Relationship>;

    constructor(relationshipModel?:RelationshipModel, singleRelationship?:Relationship) {
        if (relationshipModel) {
            this.fromNode = relationshipModel.fromNode;
            this.toNode = relationshipModel.toNode;
            this.relationships = [];
            if (relationshipModel.relationships && !singleRelationship) {
                _.forEach(relationshipModel.relationships, (relation:Relationship):void => {
                    this.relationships.push(new Relationship(relation));
                });
            } else if (singleRelationship) {
                this.relationships.push(singleRelationship);
            }
        }
    }

    public setRelationshipModelParams(fromNode:string, toNode:string, relationships:Array<Relationship>) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.relationships = relationships;
    }
}

export class RelationType {
    type:string;

    constructor(type?:string) {
        if (type) {
            this.type = type;
        }
    }
}

export class RelationshipType {
    id:string;
    capability:string;
    capabilityOwnerId:string;
    capabilityUid:string;
    relationship:RelationType;
    requirement:string;
    requirementOwnerId:string;
    requirementUid:string;

    constructor(relationship?:RelationshipType) {
        if (relationship) {
            this.id = relationship.id;
            this.capability = relationship.capability;
            this.capabilityOwnerId = relationship.capabilityOwnerId;
            this.capabilityUid = relationship.capabilityUid;
            this.relationship = new RelationType(relationship.relationship.type);
            this.requirement = relationship.requirement;
            this.requirementOwnerId = relationship.requirementOwnerId;
            this.requirementUid = relationship.requirementUid;
        } else {
            this.relationship = new RelationType();
        }
    }

    public setRelationProperties = (capability?:Capability, requirement?:Requirement)=> {
        if (capability) {
            this.capability = capability.name;
            this.capabilityOwnerId = capability.ownerId;
            this.capabilityUid = capability.uniqueId;
            this.relationship = new RelationType(capability.type);
        }
        if (requirement) {
            this.requirement = requirement.name;
            this.requirementOwnerId = requirement.ownerId;
            this.requirementUid = requirement.uniqueId;
        }
    };
}

export class Relationship {
    relation: RelationshipType;
    capability?: Capability;
    requirement?: Requirement;

    constructor(fullRelationship?:Relationship) {
        if (fullRelationship) {
            this.relation = new RelationshipType(fullRelationship.relation);
            this.capability = fullRelationship.capability && new Capability(fullRelationship.capability);
            this.requirement = fullRelationship.requirement && new Requirement(fullRelationship.requirement);
        } else {
            this.relation = new RelationshipType();
        }
    }

    public setRelationProperties(capability?:Capability, requirement?:Requirement) {
        this.relation.setRelationProperties(capability, requirement);
        this.capability = capability;
        this.requirement = requirement;
    };
}
