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
import {Requirement} from "../requirement";
import {Capability} from "../capability";
import {Relationship, RelationshipModel, RelationType} from "./relationship";
import {PropertyModel} from "../properties";

export class Match {
    requirement:Requirement;
    capability:Capability;
    isFromTo:boolean;
    fromNode:string;
    toNode:string;
    capabilityProperties:Array<PropertyModel>;  // use this to store the capability properties, since there are times the capability itself is not available (when fulfilled).
    private _relationship:Relationship;

    constructor(requirement:Requirement, capability:Capability, isFromTo:boolean, fromNode:string, toNode:string) {
        this.requirement = requirement;
        this.capability = capability;
        this.isFromTo = isFromTo;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    // NOTE: Hold the relationship instance for cases capability / requirement are not available (when fulfilled).
    //      In case relationship instance is not manually saved to here, then build the relationship from the given capability and requirement.
    public get relationship():Relationship {
        if (!this._relationship) {
            this._relationship = this.matchToRelation();
        }
        return this._relationship;
    }
    public set relationship(relationship) {
        this._relationship = relationship;
    }

    public matchToRelation = ():Relationship => {
        const relationship:Relationship = new Relationship();
        if (this.capability) {
            relationship.capability = this.capability.name;
            relationship.capabilityOwnerId = this.capability.ownerId;
            relationship.capabilityUid = this.capability.uniqueId;
            relationship.relationship = new RelationType(this.capability.type);
        }
        if (this.requirement) {
            relationship.requirement = this.requirement.name;
            relationship.requirementOwnerId = this.requirement.ownerId;
            relationship.requirementUid = this.requirement.uniqueId;
        }
        return relationship;
    };

    public getDisplayText = (menuSide:string):string => {
        if (this.isFromTo && 'left' == menuSide || !this.isFromTo && 'right' == menuSide) {
            return this.requirement.getFullTitle();
        }
        return this.capability.getFullTitle();
    };

    public isOwner = (id:string):boolean => {
        return this.capability.ownerId === id || this.requirement.ownerId === id;
    };

    public matchToRelationModel = ():RelationshipModel => {
        let relationshipModel:RelationshipModel = new RelationshipModel();
        let relationship:Relationship = this.matchToRelation();
        relationshipModel.setRelationshipModelParams(this.fromNode, this.toNode, [relationship]);
        return relationshipModel;
    };
}

