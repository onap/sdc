'use strict';

import {ArtifactType} from './../utils';
import {HeatParameterModel} from "./heat-parameters";

//this object contains keys, each key contain ArtifactModel
export class ArtifactGroupModel {
    
    constructor(artifacts?:ArtifactGroupModel) {
        _.forEach(artifacts, (artifact:ArtifactModel, key) => {
            this[key] = new ArtifactModel(artifact);
        });
    }

    public filteredByType(type:string):ArtifactGroupModel {
        let tmpArtifactGroupModel = new ArtifactGroupModel();
        _.each(Object.keys(this), (key)=>{
            if (this[key].artifactType === type) {
                tmpArtifactGroupModel[key] = this[key];
            }
        });
        return tmpArtifactGroupModel;
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
    heatParameters:Array<HeatParameterModel>;
    generatedFromId:string;

    //custom properties
    selected:boolean;
    originalDescription:string;
    envArtifact:ArtifactModel;

    constructor(artifact?:ArtifactModel) {
        if (artifact) {
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
        return ArtifactType.HEAT === this.artifactType || ArtifactType.HEAT_VOL === this.artifactType || ArtifactType.HEAT_NET === this.artifactType;
    };

    public isThirdParty = ():boolean => {
        return _.has(ArtifactType.THIRD_PARTY_RESERVED_TYPES, this.artifactType);
    };

    public toJSON = ():any => {
        this.selected = undefined;
        this.originalDescription = undefined;
        this.envArtifact = undefined;
        return this;
    };
}


