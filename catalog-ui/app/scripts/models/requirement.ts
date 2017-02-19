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
 /*
 * Created by obarda on 4/20/2016.
 */
/// <reference path="../references"/>
module Sdc.Models {
    'use strict';
    //this is an object contains keys, when each key has matching array.
    // for example: key = tosca.capabilities.network. and the match array is array of requirements objects
    export class RequirementsGroup{
        constructor(requirementGroupObj?:Models.RequirementsGroup){
            _.forEach(requirementGroupObj, (requirementsArrayObj:Array<Models.Requirement>, instance) => {
                this[instance] = [];
                _.forEach(requirementsArrayObj, (requirement:Models.Requirement):void => {
                    this[instance].push(new Models.Requirement(requirement));
                });
            });
        }
    }

    export class Requirement {

        //server data
        capability:string;
        name: string;
        ownerId: string;
        ownerName:string;
        node:string;
        uniqueId:string;
        relationship: string;
        minOccurrences: string;
        maxOccurrences: string;
        //custom
        filterTerm:string;
        constructor(requirement?:Requirement) {

            if(requirement) {
                this.capability = requirement.capability;
                this.name = requirement.name;
                this.ownerId = requirement.ownerId;
                this.ownerName = requirement.ownerName;
                this.node = requirement.node;
                this.uniqueId = requirement.uniqueId;
                this.relationship = requirement.relationship;
                this.minOccurrences = requirement.minOccurrences;
                this.maxOccurrences = requirement.maxOccurrences;
                this.initFilterTerm();

            }
        }

        public getFullTitle():string {
            return this.ownerName + ': ' + this.name +
                ': [' + this.minOccurrences + ', ' + this.maxOccurrences + ']';
        }

        public toJSON = ():any => {
            this.filterTerm = undefined;
            return this;
        };

        private initFilterTerm = ():void =>{
            this.filterTerm = (this.name + " ") +
                (this.ownerName + " " ) +
                (this.capability ? (this.capability.substring("tosca.capabilities.".length) + " " ) : "") +
                (this.node? (this.node.substring("tosca.nodes.".length) +" ") : "") +
                (this.relationship? (this.relationship.substring("tosca.relationships.".length) +" ") : "") +
                this.minOccurrences+","+this.maxOccurrences;
        }
    }
}


