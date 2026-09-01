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

import * as _ from "lodash";
import {AdditionalInformationModel} from '../additional-information';
import {IAppMenu} from '../app-config';
import {ArtifactGroupModel, ArtifactModel} from '../artifacts';
import {AttributeModel, AttributesGroup} from '../attributes';
import {CapabilitiesGroup} from '../capability';
import {IMainCategory} from '../category';
import {AsdcComment} from '../comments';
import {ComponentInstance} from '../componentsInstances/componentInstance';
import {IFileDownload} from '../file-download';
import {RelationshipModel} from '../graph/relationship';
import {InputModel} from '../inputs';
import {DisplayModule, Module} from '../modules/base-module';
import {OperationModel} from '../operation';
import {PropertiesGroup, PropertyModel} from '../properties';
import {RequirementsGroup} from '../requirement';
import {IValidate} from '../validate';

import {IComponentService} from "../../services/components/component-service";
import {CommonUtils} from "../../utils/common-utils";
import {ArtifactGroupType} from "../../utils/constants";
import {ComponentMetadata} from "../component-metadata";
import { PolicyInstance } from "app/models/graph/zones/policy-instance";
import { GroupInstance } from "../graph/zones/group-instance";
import { Metadata } from "app/models/metadata";


// import {}
export interface IComponent {

    //---------------------------------------------- API CALLS ----------------------------------------------------//

    //Component API
    getComponent():Promise<Component>;
    updateComponent():Promise<Component>;
    createComponentOnServer():Promise<Component>;
    changeLifecycleState(state:string, commentObj:AsdcComment):Promise<Component>;
    validateName(newName:string):Promise<IValidate>;

    //Artifacts API
    downloadInstanceArtifact(artifactId:string):Promise<IFileDownload>;
    downloadArtifact(artifactId:string):Promise<IFileDownload>;

    //Property API
    deleteProperty(propertyId:string):Promise<void>;

    //---------------------------------------------- HELP FUNCTIONS ----------------------------------------------------//

    getComponentSubType():string;
    isAlreadyCertified():boolean;
    isService():boolean;
    isResource():boolean;
    isComplex():boolean;
    getAllVersionsAsSortedArray():Array<any>;
    getStatus(sdcMenu:IAppMenu):string;
}


export abstract class Component implements IComponent {

    //server data
    public abstract:string;
    public uniqueId:string;
    public uuid:string;
    public invariantUUID:string;
    public name:string;
    public version:string;
    public creationDate:number;
    public lastUpdateDate:number;
    public description:string;
    public lifecycleState:string;
    public tags:Array<string>;
    public icon:string;
    public contactId:string;
    public allVersions:any;
    public creatorUserId:string;
    public creatorFullName:string;
    public lastUpdaterUserId:string;
    public lastUpdaterFullName:string;
    public componentType:string;
    public deploymentArtifacts:ArtifactGroupModel;
    public artifacts:ArtifactGroupModel;
    public toscaArtifacts:ArtifactGroupModel;
    public interfaceOperations:Array<OperationModel>;
    public distributionStatus:string;
    public categories:Array<IMainCategory>;
    public categoryNormalizedName: string;
    public subCategoryNormalizedName: string;
    public componentInstancesProperties:PropertiesGroup;
    public componentInstancesAttributes:AttributesGroup;
    public componentInstancesRelations:Array<RelationshipModel>;
    public componentInstances:Array<ComponentInstance>;
    public inputs:Array<InputModel>;
    public capabilities:CapabilitiesGroup;
    public requirements:RequirementsGroup;
    public additionalInformation:any;
    public properties:Array<PropertyModel>;
    public attributes:Array<AttributeModel>;
    public highestVersion:boolean;
    public vendorName:string;
    public vendorRelease:string;
    public derivedList:Array<any>;
    public interfaces:any;
    public normalizedName:string;
    public systemName:string;
    public policies:Array<PolicyInstance>;
    public groupInstances:Array<GroupInstance>
    public modules:Array<Module>;
    //custom properties
    public componentService:IComponentService;
    public filterTerm:string;
    public iconSprite:string;
    public selectedInstance:ComponentInstance;
    public mainCategory:string;
    public subCategory:string;
    public selectedCategory:string;
    public showMenu:boolean;
    public isArchived:boolean;
    public vspArchived: boolean;
    public componentMetadata: ComponentMetadata;
    public categorySpecificMetadata: Metadata = new Metadata();
    public derivedFromGenericType: string;
    public derivedFromGenericVersion: string;
    public model: string;

    constructor(componentService:IComponentService, component?:Component) {
        if (component) {
            this.abstract = component.abstract;
            this.uniqueId = component.uniqueId;
            this.uuid = component.uuid;
            this.invariantUUID = component.invariantUUID;
            this.additionalInformation = component.additionalInformation;
            this.artifacts = new ArtifactGroupModel(component.artifacts);
            this.toscaArtifacts = new ArtifactGroupModel(component.toscaArtifacts);
            this.interfaceOperations = component.interfaceOperations;
            this.contactId = component.contactId;
            this.categories = component.categories;
            this.categoryNormalizedName = component.categoryNormalizedName;
            this.subCategoryNormalizedName = component.subCategoryNormalizedName;
            this.creatorUserId = component.creatorUserId;
            this.creationDate = component.creationDate;
            this.creatorFullName = component.creatorFullName;
            this.description = component.description;
            this.icon = component.icon;
            this.lastUpdateDate = component.lastUpdateDate;
            this.lastUpdaterUserId = component.lastUpdaterUserId;
            this.lastUpdaterFullName = component.lastUpdaterFullName;
            this.lifecycleState = component.lifecycleState;
            this.componentInstancesRelations = CommonUtils.initComponentInstanceRelations(component.componentInstancesRelations);
            this.componentInstancesProperties = new PropertiesGroup(component.componentInstancesProperties);
            this.componentInstancesAttributes = new AttributesGroup(component.componentInstancesAttributes);
            this.name = component.name;
            this.version = component.version;
            this.tags = component.tags ? _.cloneDeep(component.tags) : [];
            this.capabilities = new CapabilitiesGroup(component.capabilities);
            this.requirements = new RequirementsGroup(component.requirements);
            this.allVersions = component.allVersions;
            this.deploymentArtifacts = new ArtifactGroupModel(component.deploymentArtifacts);
            this.componentType = component.componentType;
            this.distributionStatus = component.distributionStatus;
            this.highestVersion = component.highestVersion;
            this.vendorName = component.vendorName;
            this.vendorRelease = component.vendorRelease;
            this.derivedList = component.derivedList;
            this.interfaces = component.interfaces;
            this.normalizedName = component.normalizedName;
            this.systemName = component.systemName;
            this.inputs = component.inputs;
            this.componentInstances = CommonUtils.initComponentInstances(component.componentInstances);
            this.properties = CommonUtils.initProperties(component.properties, this.uniqueId);
            this.attributes = CommonUtils.initAttributes(component.attributes, this.uniqueId);
            this.selectedInstance = component.selectedInstance;
            this.iconSprite = component.iconSprite;
            this.showMenu = true;
            this.modules = component.modules;
            this.groupInstances = component.groupInstances;
            this.policies = component.policies;
            this.isArchived = component.isArchived;
            this.vspArchived = component.vspArchived;

            if (component.componentMetadata) {
                this.componentMetadata = component.componentMetadata;
            }
            if (component.categorySpecificMetadata && component.categories && component.categories[0]){
                this.copyCategoryMetadata(component);
                this.copySubcategoryMetadata(component);
            }

            this.derivedFromGenericType = component.derivedFromGenericType;
            this.derivedFromGenericVersion = component.derivedFromGenericVersion;
            this.model = component.model;
        }

        //custom properties
        this.componentService = componentService;
    }

    private copyCategoryMetadata = (component:Component):void => {
        if (component.categories[0].metadataKeys){
            for (let key of Object.keys(component.categorySpecificMetadata)) {
                if (component.categories[0].metadataKeys.some(metadataKey => metadataKey.name == key)) {
                    this.categorySpecificMetadata[key] = component.categorySpecificMetadata[key];
                }
            }
        }
    }
    private copySubcategoryMetadata = (component:Component):void => {
        if (component.categories[0].subcategories && component.categories[0].subcategories[0] && component.categories[0].subcategories[0].metadataKeys){
            for (let key of Object.keys(component.categorySpecificMetadata)) {
                if (component.categories[0].subcategories[0].metadataKeys.some(metadataKey => metadataKey.name == key)) {
                    this.categorySpecificMetadata[key] = component.categorySpecificMetadata[key];
                }
            }
        }
    }

    public setUniqueId = (uniqueId:string):void => {
        this.uniqueId = uniqueId;
    };

    //------------------------------------------ API Calls ----------------------------------------------------------------//
    public changeLifecycleState = (state:string, commentObj:AsdcComment):Promise<Component> => {
        return new Promise<Component>((resolve, reject) => {
            this.componentService.changeLifecycleState(this, state, commentObj).then(
                (componentMetadata:ComponentMetadata):void => {
                    this.setComponentMetadata(componentMetadata);
                    this.lifecycleState = componentMetadata.lifecycleState;
                    resolve(this);
                },
                (error:any):void => {
                    reject(error);
                });
        });
    };

    public getComponent = ():Promise<Component> => {
        return this.componentService.getComponent(this.uniqueId);
    };

    public createComponentOnServer = ():Promise<Component> => {
        this.handleTags();
        return this.componentService.createComponent(this);
    };
    
    public importComponentOnServer = (): Promise<Component> => {
        this.handleTags();
        return this.componentService.importComponent(this);
    };

    public updateComponent = ():Promise<Component> => {
        this.handleTags();
        return this.componentService.updateComponent(this);
    };

    public validateName = (newName:string, subtype?:string):Promise<IValidate> => {
        return this.componentService.validateName(newName, subtype);
    };

    public downloadArtifact = (artifactId: string): Promise<IFileDownload> => {
        if(this.vendorName === 'IsService'){
            return this.componentService.downloadArtifact(this.uniqueId, artifactId, this.vendorName);
        }else{
            return this.componentService.downloadArtifact(this.uniqueId, artifactId);
        }
    };

    // Resolves with no value: the sole caller (ng2/pages/workspace/properties-tab) only needs to know
    // the round-trip finished so it can refresh the table.
    public deleteProperty = (propertyId:string):Promise<void> => {
        return new Promise<void>((resolve, reject) => {
            this.componentService.deleteProperty(this.uniqueId, propertyId).then(
                ():void => {
                    delete _.remove(this.properties, {uniqueId: propertyId})[0];
                    resolve();
                },
                ():void => {
                    reject();
                });
        });
    };

    public downloadInstanceArtifact = (artifactId:string):Promise<IFileDownload> => {
        return this.componentService.downloadInstanceArtifact(this.uniqueId, this.selectedInstance.uniqueId, artifactId);
    };

    //------------------------------------------ Help Functions ----------------------------------------------------------------//

    public isService = ():boolean => {
        return false;
    };

    public isResource = ():boolean => {
        return false;
    };

    public getComponentSubType = ():string => {
        return this.componentType;
    };

    public isAlreadyCertified = ():boolean => {
        return parseInt(this.version) >= 1;
    };

    public isComplex = ():boolean => {
        return true;
    };

    //sort string version value from hash to sorted version (i.e 1.9 before 1.11)
    private sortVersions = (v1:string, v2:string):number => {
        let ver1 = v1.split('.');
        let ver2 = v2.split('.');
        let diff = parseInt(_.first(ver1)) - parseInt(_.first(ver2));
        if (!diff) {
            return parseInt(_.last(ver1)) - parseInt(_.last(ver2));
        }
        return diff;
    };

    public getAllVersionsAsSortedArray = ():Array<any> => {
        let res = [];
        if (this.allVersions) {
            let keys = Object.keys(this.allVersions).sort(this.sortVersions);
            _.forEach(keys, (key)=> {
                res.push({
                    versionNumber: key,
                    versionId: this.allVersions[key]
                })
            });
        }
        return res;
    };

    public isLatestVersion = ():boolean => {
        if (this.allVersions) {
            return this.version === _.last(Object.keys(this.allVersions).sort(this.sortVersions));
        } else {
            return true;
        }

    };


    public handleTags = ():void => {
        let isContainTag = _.find(this.tags, (tag)=> {
            return tag === this.name;
        });
        if (!isContainTag) {
            this.tags.push(this.name);
        }
    };

    public getArtifactsByType = (artifactGroupType:string):ArtifactGroupModel => {
        switch (artifactGroupType) {
            case ArtifactGroupType.DEPLOYMENT:
                return this.deploymentArtifacts;
            case ArtifactGroupType.INFORMATION:
                return this.artifacts;
        }
    };

    public getStatus = (sdcMenu:IAppMenu):string => {
        let status:string = sdcMenu.LifeCycleStatuses[this.lifecycleState].text;
        if (this.lifecycleState == "CERTIFIED" && sdcMenu.DistributionStatuses[this.distributionStatus]) {
            status = sdcMenu.DistributionStatuses[this.distributionStatus].text;
        }
        return status;
    };

    public abstract setComponentDisplayData():void;
    public abstract getTypeUrl():string;

    public setComponentMetadata(componentMetadata: ComponentMetadata) {
        this.abstract = componentMetadata.abstract;
        this.uniqueId = componentMetadata.uniqueId;
        this.uuid = componentMetadata.uuid;
        this.invariantUUID = componentMetadata.invariantUUID;
        this.contactId = componentMetadata.contactId;
        this.categories = componentMetadata.categories;
        this.creatorUserId = componentMetadata.creatorUserId;
        this.creationDate = componentMetadata.creationDate;
        this.creatorFullName = componentMetadata.creatorFullName;
        this.description = componentMetadata.description;
        this.icon = componentMetadata.icon;
        this.lastUpdateDate = componentMetadata.lastUpdateDate;
        this.lastUpdaterUserId = componentMetadata.lastUpdaterUserId;
        this.lastUpdaterFullName = componentMetadata.lastUpdaterFullName;
        this.lifecycleState = componentMetadata.lifecycleState;
        this.name = componentMetadata.name;
        this.version = componentMetadata.version;
        // Falls back to [] rather than undefined: the BE may omit tags, and displayComponent /
        // the catalog filter term call tags.join()/.toString() unguarded. angular.copy gave this
        // for free by emptying the existing array destination instead of replacing it. Unlike
        // angular.copy(src, dest) this replaces the array rather than refilling it, so a caller
        // holding a reference across this call would go stale — none does; every consumer re-reads
        // component.tags (general-tab calls patchFormFromComponent right after setComponentMetadata).
        this.tags = componentMetadata.tags ? _.cloneDeep(componentMetadata.tags) : [];
        this.allVersions = componentMetadata.allVersions;
        this.componentType = componentMetadata.componentType;
        this.distributionStatus = componentMetadata.distributionStatus;
        this.highestVersion = componentMetadata.highestVersion;
        this.vendorName = componentMetadata.vendorName;
        this.vendorRelease = componentMetadata.vendorRelease;
        this.derivedList = componentMetadata.derivedList;
        this.normalizedName = componentMetadata.normalizedName;
        this.systemName = componentMetadata.systemName;
        this.isArchived = componentMetadata.isArchived;
        this.vspArchived = componentMetadata.vspArchived;
        this.componentMetadata = componentMetadata;
        if (componentMetadata.categorySpecificMetadata){
            this.categorySpecificMetadata = componentMetadata.categorySpecificMetadata;
        } else {
            this.categorySpecificMetadata = new Metadata();
        }
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
        temp.modules = undefined
        temp.groupInstances = undefined;
        temp.policies = undefined;
        return temp;
    };
}

