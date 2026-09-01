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
import * as _ from "lodash";
import {IServiceService} from "../../services/components/service-service";
import {ArtifactGroupModel} from '../artifacts';
import {Distribution, DistributionComponent} from '../distribution';
import {Component} from './component';
import {ArtifactGroupType} from "../../utils/constants";
import {FileUploadModel} from "../file-upload-model";
import {ComponentMetadata} from "../component-metadata";
import {ForwardingPath} from "app/models/forwarding-path";

export class Service extends Component {

    public serviceApiArtifacts:ArtifactGroupModel;
    public componentService:IServiceService;
    public ecompGeneratedNaming:boolean;
    public namingPolicy:string;
    public serviceType:string;
    public serviceRole:string;
    public serviceFunction:string;
    public environmentContext:string;
    public instantiationType:string;
    public forwardingPaths:{ [key:string]:ForwardingPath } = {};
    public payloadData: string;
    public payloadName: string;
    public importedFile: FileUploadModel;

    // Onboarding parameters
    public csarUUID: string;
    public csarVersion: string;
    public csarPackageType: string;
    public packageId: string;

    constructor(componentService:IServiceService, component?:Service) {
        super(componentService, component);
        this.ecompGeneratedNaming = true;
        if (component) {
            this.serviceApiArtifacts = new ArtifactGroupModel(component.serviceApiArtifacts);
            this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
            this.ecompGeneratedNaming = component.ecompGeneratedNaming;
            this.namingPolicy = component.namingPolicy;
            this.serviceType = component.serviceType;
            this.serviceRole = component.serviceRole;
            this.serviceFunction = component.serviceFunction;
            this.instantiationType = component.instantiationType;
            this.environmentContext = component.environmentContext;
            this.payloadData = component.payloadData ? component.payloadData : undefined;
            this.payloadName = component.payloadName ? component.payloadName : undefined;
            this.csarUUID = component.csarUUID;
            this.csarVersion = component.csarVersion;
            if (component.categories && component.categories[0]) {
                this.mainCategory = component.categories[0].name;
                this.selectedCategory = this.mainCategory;
                this.importedFile = component.importedFile;
            }
        }
        this.componentService = componentService;
        this.iconSprite = "sprite-services-icons";
    }

    public isService = ():boolean => true;

    public importComponentOnServer = (): Promise<Component> => {
        return new Promise<Component>((resolve, reject) => {
            this.handleTags();
            if (this.importedFile) {
                this.payloadData = this.importedFile.base64;
                this.payloadName = this.importedFile.filename;
            }
            this.componentService.importComponent(this).then(
                (component: Service): void => {
                    this.payloadData = undefined;
                    this.payloadName = undefined;
                    resolve(component);
                },
                (error: any): void => {
                    reject(error);
                });
        });
    };

    public getDistributionsList = ():Promise<Array<Distribution>> => {
        return this.componentService.getDistributionsList(this.uuid);
    };

    public getDistributionsComponent = (distributionId:string):Promise<Array<DistributionComponent>> => {
        return this.componentService.getDistributionComponents(distributionId);
    };

    public markAsDeployed = (distributionId:string):Promise<any> => {
        return this.componentService.markAsDeployed(this.uniqueId, distributionId);
    };

    public getArtifactsByType = (artifactGroupType:string):ArtifactGroupModel => {
        switch (artifactGroupType) {
            case ArtifactGroupType.DEPLOYMENT:
                return this.deploymentArtifacts;
            case ArtifactGroupType.INFORMATION:
                return this.artifacts;
            case ArtifactGroupType.SERVICE_API:
                return this.serviceApiArtifacts;
        }
    };

    getTypeUrl():string {
        return 'services/';
    }

    public setComponentMetadata(componentMetadata: ComponentMetadata) {
        super.setComponentMetadata(componentMetadata);
        this.ecompGeneratedNaming = componentMetadata.ecompGeneratedNaming != undefined && componentMetadata.ecompGeneratedNaming;
        this.namingPolicy = componentMetadata.namingPolicy;
        this.serviceType = componentMetadata.serviceType;
        this.serviceRole = componentMetadata.serviceRole;
        this.serviceFunction = componentMetadata.serviceFunction;
        this.environmentContext = componentMetadata.environmentContext;
        this.instantiationType = componentMetadata.instantiationType;
        this.setComponentDisplayData();
    }

    setComponentDisplayData():void {
        this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
        if (this.categories && this.categories[0]) {
            this.mainCategory = this.categories[0].name;
            this.selectedCategory = this.mainCategory;
        }
        this.iconSprite = "sprite-services-icons";
    }

    public isSubstituteCandidate(): boolean {
        return !!this.derivedFromGenericType;
    }

    public toJSON = ():any => {
        let temp = _.cloneDeep(this);
        temp.componentService = undefined;
        temp.filterTerm = undefined;
        temp.iconSprite = undefined;
        temp.mainCategory = undefined;
        temp.subCategory = undefined;
        temp.selectedInstance = undefined;
        temp.showMenu = undefined;
        temp.selectedCategory = undefined;
        temp.modules = undefined;
        temp.groupInstances = undefined;
        temp.policies = undefined;
        return temp;
    };
}

