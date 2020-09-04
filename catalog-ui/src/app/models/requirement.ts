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

/**
 * Created by obarda on 4/20/2016.
 */
'use strict';
import * as _ from "lodash";
import {RequirementCapabilityModel} from "./capability";

//this is an object contains keys, when each key has matching array.
// for example: key = tosca.capabilities.network. and the match array is array of requirements objects
export class RequirementsGroup {
    [key: string]: Array<Requirement>;
    constructor(requirementGroupObj?:RequirementsGroup) {
        _.forEach(requirementGroupObj, (requirementsArrayObj:Array<Requirement>, instance) => {
            this[instance] = [];
            _.forEach(requirementsArrayObj, (requirement:Requirement):void => {
                this[instance].push(new Requirement(requirement));
            });
        });
    }
}

export class Requirement implements RequirementCapabilityModel{

    //server data
    capability:string;
    name:string;
    ownerId:string;
    ownerName:string;
    parentName: string;
    node:string;
    uniqueId:string;
    relationship:string;
    leftOccurrences:string;
    minOccurrences:string | number;
    maxOccurrences:string;
    external:boolean;
    //custom
    filterTerm:string;

    constructor(requirement?:Requirement) {

        if (requirement) {
            this.capability = requirement.capability;
            this.name = requirement.name;
            this.ownerId = requirement.ownerId;
            this.ownerName = requirement.ownerName;
            this.parentName = requirement.parentName;
            this.node = requirement.node;
            this.uniqueId = requirement.uniqueId;
            this.relationship = requirement.relationship;
            this.leftOccurrences = requirement.leftOccurrences;
            this.minOccurrences = requirement.minOccurrences;
            this.maxOccurrences = requirement.maxOccurrences;
            this.external = requirement.external;
            this.initFilterTerm();

        }
    }

    public getTitle():string {
        return this.ownerName + ': ' + this.name;
    }

    public getFullTitle():string {
         return this.getTitle() + ': [' + this.minOccurrences + ', ' + this.maxOccurrences + ']';
    }

    public toJSON = ():any => {
        this.filterTerm = undefined;
        return this;
    };

    private initFilterTerm = ():void => {
        this.filterTerm = (this.name + " ") +
            (this.ownerName + " " ) +
            (this.capability ? (this.capability.substring("tosca.capabilities.".length) + " " ) : "") +
            (this.node ? (this.node.substring("tosca.nodes.".length) + " ") : "") +
            (this.relationship ? (this.relationship.substring("tosca.relationships.".length) + " ") : "") +
            this.minOccurrences + "," + this.maxOccurrences;
    }

    public isFulfilled() {
        return parseInt(this.leftOccurrences) === 0;
    }
}

// tslint:disable-next-line:max-classes-per-file
export class RequirementUI extends Requirement {
    isCreatedManually: boolean;

    constructor(input: Requirement, componentUniqueId: string) {
        super(input);
        this.isCreatedManually = input.ownerId === componentUniqueId;
    }
}


