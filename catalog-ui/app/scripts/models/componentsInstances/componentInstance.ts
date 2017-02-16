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
/// <reference path="../../references"/>
module Sdc.Models.ComponentsInstances {
    'use strict';

    export class ComponentInstance{

        public componentUid: string;
        public componentName:string;
        public posX: number;
        public posY: number;
        public componentVersion:string;
        public description: string;
        public icon: string;
        public name: string;
        public normalizedName:string;
        public originType: string;
        public deploymentArtifacts: Models.ArtifactGroupModel;
        public propertyValueCounter: number;
        public uniqueId: string;
        public creationTime: number;
        public modificationTime: number;
        public capabilities: Models.CapabilitiesGroup;
        public requirements: Models.RequirementsGroup;

        //custom properties
        public certified: boolean;
        public iconSprite:string;
        public inputs: Array<Models.InputModel>;
        public properties: Array<Models.PropertyModel>;

        constructor(componentInstance?: ComponentInstance) {

            if(componentInstance) {
                this.componentUid = componentInstance.componentUid;
                this.componentName = componentInstance.componentName;

                this.componentVersion = componentInstance.componentVersion;
                this.description = componentInstance.description;
                this.icon = componentInstance.icon;
                this.name = componentInstance.name;
                this.normalizedName = componentInstance.normalizedName;
                this.originType = componentInstance.originType;
                this.deploymentArtifacts = new Models.ArtifactGroupModel(componentInstance.deploymentArtifacts);
                this.uniqueId = componentInstance.uniqueId;
                this.creationTime = componentInstance.creationTime;
                this.modificationTime = componentInstance.modificationTime;
                this.propertyValueCounter = componentInstance.propertyValueCounter;
                this.capabilities = new Models.CapabilitiesGroup(componentInstance.capabilities);
                this.requirements = new Models.RequirementsGroup(componentInstance.requirements);
                this.certified = componentInstance.certified;
                this.updatePosition(componentInstance.posX, componentInstance.posY);
            }
        }

        public isUcpe = ():boolean =>{
            if(this.originType === 'VF' && this.capabilities && this.capabilities['tosca.capabilities.Container'] && this.name.toLowerCase().indexOf('ucpe') > -1){
                return true;
            }
            return false;
        };

        public isVl = ():boolean =>{
            return this.originType === 'VL';
        };


        public setInstanceRC = ():void=>{
            _.forEach(this.requirements, (requirementValue:Array<any>, requirementKey)=> {
                _.forEach(requirementValue, (requirement)=> {
                    if (!requirement.ownerName){
                        requirement['ownerId'] = this.uniqueId;
                        requirement['ownerName'] = this.name;
                    }
                });
            });
            _.forEach(this.capabilities, (capabilityValue:Array<any>, capabilityKey)=> {
                _.forEach(capabilityValue, (capability)=> {
                    if (!capability.ownerName){
                        capability['ownerId'] = this.uniqueId;
                        capability['ownerName'] = this.name;
                    }
                });
            });
        };

        public updatePosition (posX:number, posY:number) {
            this.posX = posX;
            this.posY = posY;
        }

        public toJSON = ():any => {

            var serverInstance = angular.copy(this);
            serverInstance.certified = undefined;
            serverInstance.iconSprite = undefined;
            serverInstance.inputs = undefined;
            serverInstance.properties = undefined;
            serverInstance.requirements = undefined;
            serverInstance.capabilities = undefined;
            return serverInstance;
        };
    }

}
