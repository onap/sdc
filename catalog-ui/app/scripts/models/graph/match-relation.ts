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
/// <reference path="../../references"/>
module Sdc.Models {
    'use strict';

    export class MatchBase {
        requirement:Models.Requirement;
        isFromTo:boolean;
        fromNode:string;
        toNode:string;

        constructor(requirement:Models.Requirement, isFromTo:boolean, fromNode:string, toNode:string) {
            this.requirement = requirement;
            this.isFromTo = isFromTo;
            this.fromNode = fromNode;
            this.toNode = toNode;
        }

        public getDisplayText = (menuSide:string):string => {return '';};

        public isOwner = (id:string):boolean => { return false; }

    }

    export class MatchReqToReq extends MatchBase {

        secondRequirement:Models.Requirement;

        constructor(requirement:Models.Requirement, secondRequirement:Models.Requirement, isFromTo:boolean, fromNode:string, toNode:string) {
            super(requirement, isFromTo, fromNode, toNode);
            this.secondRequirement = secondRequirement;
        }

        public getDisplayText = (menuSide:string):string => {
            if ('left' == menuSide) {
                return this.requirement.getFullTitle();
            }
            return this.secondRequirement.getFullTitle();
        };

        public isOwner = (id:string):boolean => {
            return this.secondRequirement.ownerId === id || this.requirement.ownerId === id;
        }
    }

    export class MatchReqToCapability extends MatchBase {

        capability:Models.Capability;

        constructor(requirement:Models.Requirement, capability:Models.Capability, isFromTo:boolean, fromNode:string, toNode:string) {
            super(requirement, isFromTo, fromNode, toNode);
            this.capability = capability;
        }

        public matchToRelation = ():Models.Relationship => {
            let relationship:Models.Relationship = new Models.Relationship();
            relationship.capability = this.capability.name;
            relationship.capabilityOwnerId = this.capability.ownerId;
            relationship.capabilityUid = this.capability.uniqueId;
            relationship.relationship = new Models.RelationType(this.capability.type);
            relationship.requirement = this.requirement.name;
            relationship.requirementOwnerId = this.requirement.ownerId;
            relationship.requirementUid = this.requirement.uniqueId;
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


        public matchToRelationModel = ():Models.RelationshipModel => {
            let relationshipModel:Models.RelationshipModel = new Models.RelationshipModel();
            let relationship:Models.Relationship = this.matchToRelation();
            relationshipModel.setRelationshipModelParams(this.fromNode, this.toNode, [relationship]);
            return relationshipModel;
        };
    }

}


