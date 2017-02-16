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
/// <reference path="../references"/>
module Sdc.Models {
    'use strict';

    //this object contains keys, each key contain ArtifactModel
    export class ArtifactGroupModel{
        constructor(artifacts?:Models.ArtifactGroupModel) {
            _.forEach(artifacts, (artifact:Models.ArtifactModel, key) => {
                this[key] = new Models.ArtifactModel(artifact);
            });
        }

        public filteredByType (type:string): Models.ArtifactGroupModel {
            return JSON.parse(JSON.stringify(_.pick(this, (artifact)=>{ return artifact.artifactType == type})));
        };
    }

    export class ArtifactModel {

        artifactDisplayName:string;
        artifactGroupType:string;
        uniqueId:string;
        artifactName:string;
        artifactLabel:string;
        artifactType:string;
        artifactUUID:string;
        artifactVersion:string;
        creatorFullName:string;
        creationDate:number;
        lastUpdateDate:number;
        description:string;
        mandatory:boolean;
        serviceApi:boolean;
        payloadData:string;
        timeout:number;
        esId:string;
        "Content-MD5":string;
        artifactChecksum:string;
        apiUrl:string;
        heatParameters:Array<any>;
        generatedFromId:string;

        //custom properties
        selected:boolean;
        originalDescription:string;

        constructor(artifact?:ArtifactModel) {
            if(artifact) {
                this.artifactDisplayName = artifact.artifactDisplayName;
                this.artifactGroupType = artifact.artifactGroupType;
                this.uniqueId = artifact.uniqueId;
                this.artifactName = artifact.artifactName;
                this.artifactLabel = artifact.artifactLabel;
                this.artifactType = artifact.artifactType;
                this.artifactUUID = artifact.artifactUUID;
                this.artifactVersion = artifact.artifactVersion;
                this.creatorFullName = artifact.creatorFullName;
                this.creationDate = artifact.creationDate;
                this.lastUpdateDate = artifact.lastUpdateDate;
                this.description = artifact.description;
                this.mandatory = artifact.mandatory;
                this.serviceApi = artifact.serviceApi;
                this.payloadData = artifact.payloadData;
                this.timeout = artifact.timeout;
                this.esId = artifact.esId;
                this["Content-MD5"] = artifact["Content-MD5"];
                this.artifactChecksum = artifact.artifactChecksum;
                this.apiUrl = artifact.apiUrl;
                this.heatParameters = _.sortBy(artifact.heatParameters, 'name');
                this.generatedFromId = artifact.generatedFromId;
                this.selected = artifact.selected ? artifact.selected : false;
                this.originalDescription = artifact.description;
            }
        }

        public isHEAT = ():boolean => {
            return Utils.Constants.ArtifactType.HEAT === this.artifactType.substring(0,4);
        };

        // public isEditableInInstanceLevel = ():boolean => {
        //     return true;
        // };

        public isThirdParty = ():boolean => {
            return _.has(Utils.Constants.ArtifactType.THIRD_PARTY_RESERVED_TYPES, this.artifactType);
        };

        public toJSON = ():any => {
            this.selected = undefined;
            this.originalDescription = undefined;
            return this;
        };
    }
}


