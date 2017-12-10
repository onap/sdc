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
 * Created by obarda on 2/4/2016.
 */
'use strict';
import {ArtifactGroupModel, CapabilitiesGroup,RequirementsGroup, PropertyModel, InputModel, Module} from "../../models";
import {ResourceType,ComponentType} from "../../utils/constants";
import {Capability} from "../capability";
import {Requirement} from "../requirement";

export class ComponentInstance {

    public componentUid:string;
    public componentName:string;
    public posX:number;
    public posY:number;
    public componentVersion:string;
    public description:string;
    public icon:string;
    public name:string;
    public normalizedName:string;
    public originType:string;
    public deploymentArtifacts:ArtifactGroupModel;
    public artifacts:ArtifactGroupModel;
    public propertyValueCounter:number;
    public uniqueId:string;
    public creationTime:number;
    public modificationTime:number;
    public capabilities:CapabilitiesGroup;
    public requirements:RequirementsGroup;
    public customizationUUID:string;
    public sourceModelInvariant:string;
    public sourceModelName:string;
    public sourceModelUid:string;
    public sourceModelUuid:string;
    //custom properties
    public certified:boolean;
    public iconSprite:string;
    public inputs:Array<InputModel>;
    public properties:Array<PropertyModel>;
    public groupInstances:Array<Module>;
    public invariantName:string;

    constructor(componentInstance?:ComponentInstance) {

        if (componentInstance) {
            this.componentUid = componentInstance.componentUid;
            this.componentName = componentInstance.componentName;

            this.componentVersion = componentInstance.componentVersion;
            this.description = componentInstance.description;
            this.icon = componentInstance.icon;
            this.name = componentInstance.name;
            this.normalizedName = componentInstance.normalizedName;
            this.originType = componentInstance.originType;
            this.deploymentArtifacts = new ArtifactGroupModel(componentInstance.deploymentArtifacts);
            this.artifacts = new ArtifactGroupModel(componentInstance.artifacts);
            this.uniqueId = componentInstance.uniqueId;
            this.creationTime = componentInstance.creationTime;
            this.modificationTime = componentInstance.modificationTime;
            this.propertyValueCounter = componentInstance.propertyValueCounter;
            this.capabilities = new CapabilitiesGroup(componentInstance.capabilities);
            this.requirements = new RequirementsGroup(componentInstance.requirements);
            this.certified = componentInstance.certified;
            this.customizationUUID = componentInstance.customizationUUID;
            this.updatePosition(componentInstance.posX, componentInstance.posY);
            this.groupInstances = componentInstance.groupInstances;
            this.invariantName = componentInstance.invariantName;
            this.sourceModelInvariant = componentInstance.sourceModelInvariant;
            this.sourceModelName = componentInstance.sourceModelName;
            this.sourceModelUid = componentInstance.sourceModelUid;
            this.sourceModelUuid = componentInstance.sourceModelUuid;
        }
    }

    public isUcpe = ():boolean => {
        if (this.originType === 'VF' && this.capabilities && this.capabilities['tosca.capabilities.Container'] && this.name.toLowerCase().indexOf('ucpe') > -1) {
            return true;
        }
        return false;
    };

    public isVl = ():boolean => {
        return this.originType === 'VL';
    };

    public isComplex = () : boolean => {
        return this.originType === ResourceType.VF || this.originType === ResourceType.PNF || this.originType === ResourceType.CVFC  ;
    }

    public isServiceProxy = () :boolean => {
        return this.originType === ComponentType.SERVICE_PROXY;
    }

    public setInstanceRC = ():void=> {
        _.forEach(this.requirements, (requirementValue:Array<any>, requirementKey)=> {
            _.forEach(requirementValue, (requirement)=> {
                if (!requirement.ownerName) {
                    requirement['ownerId'] = this.uniqueId;
                    requirement['ownerName'] = this.name;
                }
            });
        });
        _.forEach(this.capabilities, (capabilityValue:Array<any>, capabilityKey)=> {
            _.forEach(capabilityValue, (capability)=> {
                if (!capability.ownerName) {
                    capability['ownerId'] = this.uniqueId;
                    capability['ownerName'] = this.name;
                }
            });
        });
    };

    public updatePosition(posX:number, posY:number) {
        this.posX = posX;
        this.posY = posY;
    }

    public findRequirement(reqType:string, uniqueId:string, ownerId:string, name:string):Requirement|undefined {
        let requirement:Requirement = undefined;
        const searchGroup = (reqType) ? [this.requirements[reqType]] : this.requirements;
        _.some(_.keys(searchGroup), (searchType) => {
            requirement = _.find<Requirement>(searchGroup[searchType], (req:Requirement) => (
                req.uniqueId === uniqueId && req.ownerId === ownerId && req.name === name
            ));
            return requirement;
        });
        return requirement;
    }

    public findCapability(capType:string, uniqueId:string, ownerId:string, name:string):Capability|undefined {
        let capability:Capability = undefined;
        const searchGroup = (capType) ? [this.capabilities[capType]] : this.capabilities;
        _.some(_.keys(searchGroup), (searchType) => {
            capability = _.find<Capability>(searchGroup[searchType], (cap:Capability) => (
                cap.uniqueId === uniqueId && cap.ownerId === ownerId && cap.name === name
            ));
            return capability;
        });
        return capability;
    }

    public toJSON = ():any => {
        let temp = angular.copy(this);
        temp.certified = undefined;
        temp.iconSprite = undefined;
        temp.inputs = undefined;
        temp.groupInstances = undefined;
        temp.properties = undefined;
        temp.requirements = undefined;
        temp.capabilities = undefined;
        return temp;
    };
}
