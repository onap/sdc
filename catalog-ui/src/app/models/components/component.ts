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
import {AsdcComment, ArtifactModel, ArtifactGroupModel, IFileDownload, PropertyModel, PropertiesGroup, AttributeModel, AttributesGroup, ComponentInstance,
    InputModel, DisplayModule, Module, IValidate, RelationshipModel, IMainCategory, RequirementsGroup, CapabilitiesGroup, AdditionalInformationModel,
    Resource, IAppMenu, OperationModel, Service} from "../../models";

import {IComponentService} from "../../services/components/component-service";
import {CommonUtils} from "../../utils/common-utils";
import {QueueUtils} from "../../utils/functions";
import {ArtifactGroupType} from "../../utils/constants";
import {ComponentMetadata} from "../component-metadata";
import {Capability} from "../capability";
import {Requirement} from "../requirement";
import {Relationship} from "../graph/relationship";
import { PolicyInstance } from "app/models/graph/zones/policy-instance";
import { GroupInstance } from "../graph/zones/group-instance";
import { Metadata } from "app/models/metadata";
import {Model} from "../model";


// import {}
export interface IComponent {

    //---------------------------------------------- API CALLS ----------------------------------------------------//

    //Component API
    getComponent():ng.IPromise<Component>;
    updateComponent():ng.IPromise<Component>;
    createComponentOnServer():ng.IPromise<Component>;
    changeLifecycleState(state:string, commentObj:AsdcComment):ng.IPromise<Component>;
    validateName(newName:string):ng.IPromise<IValidate>;

    //Artifacts API
    addOrUpdateArtifact(artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    deleteArtifact(artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel>;
    downloadInstanceArtifact(artifactId:string):ng.IPromise<IFileDownload>;
    downloadArtifact(artifactId:string):ng.IPromise<IFileDownload>;

    //Property API
    addOrUpdateProperty(property:PropertyModel):ng.IPromise<PropertyModel>;
    deleteProperty(propertyId:string):ng.IPromise<PropertyModel>;

    //Attribute API
    deleteAttribute(attributeId:string):ng.IPromise<AttributeModel>;
    addOrUpdateAttribute(attribute:AttributeModel):ng.IPromise<AttributeModel>;

    //Modules
    getModuleForDisplay(moduleId:string):ng.IPromise<DisplayModule>;
    getModuleInstanceForDisplay(componentInstanceId:string, moduleId:string):ng.IPromise<DisplayModule>;
    updateGroupMetadata(group:Module):ng.IPromise<Module>;

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

    constructor(componentService:IComponentService, protected $q:ng.IQService, component?:Component) {
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
            this.tags = [];
            angular.copy(component.tags, this.tags);
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
    public changeLifecycleState = (state:string, commentObj:AsdcComment):ng.IPromise<Component> => {
        console.log('changeLifecycleState called', state);
        let deferred = this.$q.defer<Component>();
        let onSuccess = (componentMetadata:ComponentMetadata):void => {
            console.log('changeLifecycleState onSuccess', componentMetadata);
            this.setComponentMetadata(componentMetadata);
            // this.version = componentMetadata.version;
            this.lifecycleState = componentMetadata.lifecycleState;

            deferred.resolve(this);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.changeLifecycleState(this, state, commentObj).then(onSuccess, onError);
        return deferred.promise;
    };

    public getComponent = ():ng.IPromise<Component> => {
        return this.componentService.getComponent(this.uniqueId);
    };

    public createComponentOnServer = ():ng.IPromise<Component> => {
        this.handleTags();
        return this.componentService.createComponent(this);
    };
    
    public importComponentOnServer = (): ng.IPromise<Component> => {
        this.handleTags();
        return this.componentService.importComponent(this);
    };

    public updateComponent = ():ng.IPromise<Component> => {
        this.handleTags();
        return this.componentService.updateComponent(this);
    };

    public validateName = (newName:string, subtype?:string):ng.IPromise<IValidate> => {
        return this.componentService.validateName(newName, subtype);
    };

    public downloadArtifact = (artifactId: string): ng.IPromise<IFileDownload> => {
        if(this.vendorName === 'IsService'){
            return this.componentService.downloadArtifact(this.uniqueId, artifactId, this.vendorName);
        }else{
            return this.componentService.downloadArtifact(this.uniqueId, artifactId);
        }
    };

    public addOrUpdateArtifact = (artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer<ArtifactModel>();
        let onSuccess = (artifactObj:ArtifactModel):void => {
            let newArtifact = new ArtifactModel(artifactObj);
            let artifacts = this.getArtifactsByType(artifactObj.artifactGroupType);
            artifacts[artifactObj.artifactLabel] = newArtifact;
            deferred.resolve(newArtifact);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.addOrUpdateArtifact(this.uniqueId, artifact).then(onSuccess, onError);
        return deferred.promise;
    };

    public deleteArtifact = (artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer<ArtifactModel>();
        let onSuccess = (artifactObj:ArtifactModel):void => {
            let newArtifact = new ArtifactModel(artifactObj);
            let artifacts = this.getArtifactsByType(artifactObj.artifactGroupType);
            if (newArtifact.mandatory || newArtifact.serviceApi) {
                artifacts[newArtifact.artifactLabel] = newArtifact;
            }
            else {
                delete artifacts[artifactLabel];
            }
            deferred.resolve(newArtifact);
        };
        this.componentService.deleteArtifact(this.uniqueId, artifactId, artifactLabel).then(onSuccess);
        return deferred.promise;
    };

    public addOrUpdateProperty = (property:PropertyModel):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer<PropertyModel>();

        let onError = (error:any):void => {
            deferred.reject(error);
        };

        if (!property.uniqueId) {
            let onSuccess = (property:PropertyModel):void => {
                let newProperty = new PropertyModel(property);
                this.properties.push(newProperty);
                deferred.resolve(newProperty);
            };
            this.componentService.addProperty(this.uniqueId, property).then(onSuccess, onError);
        }
        else {
            let onSuccess = (newProperty:PropertyModel):void => {
                // find exist instance property in parent component for update the new value ( find bu uniqueId )
                let existProperty:PropertyModel = <PropertyModel>_.find(this.properties, {uniqueId: newProperty.uniqueId});
                let propertyIndex = this.properties.indexOf(existProperty);
                this.properties[propertyIndex] = newProperty;
                deferred.resolve(newProperty);
            };
            this.componentService.updateProperty(this.uniqueId, property).then(onSuccess, onError);
        }
        return deferred.promise;
    };

    public addOrUpdateAttribute = (attribute:AttributeModel):ng.IPromise<AttributeModel> => {
        let deferred = this.$q.defer<AttributeModel>();

        let onError = (error:any):void => {
            deferred.reject(error);
        };

        if (!attribute.uniqueId) {
            let onSuccess = (attribute:AttributeModel):void => {
                let newAttribute = new AttributeModel(attribute);
                this.attributes.push(newAttribute);
                deferred.resolve(newAttribute);
            };
            this.componentService.addAttribute(this.uniqueId, attribute).then(onSuccess, onError);
        }
        else {
            let onSuccess = (newAttribute:AttributeModel):void => {
                let existAttribute:AttributeModel = <AttributeModel>_.find(this.attributes, {uniqueId: newAttribute.uniqueId});
                let attributeIndex = this.attributes.indexOf(existAttribute);
                newAttribute.readonly = this.uniqueId != newAttribute.parentUniqueId;
                this.attributes[attributeIndex] = newAttribute;
                deferred.resolve(newAttribute);
            };
            this.componentService.updateAttribute(this.uniqueId, attribute).then(onSuccess, onError);
        }
        return deferred.promise;
    };

    public deleteProperty = (propertyId:string):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer<PropertyModel>();
        let onSuccess = ():void => {
            console.log("Property deleted");
            delete _.remove(this.properties, {uniqueId: propertyId})[0];
            deferred.resolve();
        };
        let onFailed = ():void => {
            console.log("Failed to delete property");
            deferred.reject();
        };
        this.componentService.deleteProperty(this.uniqueId, propertyId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public deleteAttribute = (attributeId:string):ng.IPromise<AttributeModel> => {
        let deferred = this.$q.defer<AttributeModel>();
        let onSuccess = ():void => {
            console.log("Attribute deleted");
            delete _.remove(this.attributes, {uniqueId: attributeId})[0];
        };
        let onFailed = ():void => {
            console.log("Failed to delete attribute");
        };
        this.componentService.deleteAttribute(this.uniqueId, attributeId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public downloadInstanceArtifact = (artifactId:string):ng.IPromise<IFileDownload> => {
        return this.componentService.downloadInstanceArtifact(this.uniqueId, this.selectedInstance.uniqueId, artifactId);
    };

    public getModuleForDisplay = (moduleId:string):ng.IPromise<DisplayModule> => {

        let deferred = this.$q.defer<DisplayModule>();
        let onSuccess = (response:DisplayModule):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getModuleForDisplay(this.uniqueId, moduleId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public getModuleInstanceForDisplay = (componentInstanceId:string, moduleId:string):ng.IPromise<DisplayModule> => {

        let deferred = this.$q.defer<DisplayModule>();
        let onSuccess = (response:DisplayModule):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInstanceModule(this.uniqueId, componentInstanceId, moduleId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public updateGroupMetadata = (module:Module):ng.IPromise<Module> => {

        let deferred = this.$q.defer<Module>();

        let onSuccess = (updatedModule:Module):void => {
            let groupIndex:number = _.indexOf(this.modules, _.find(this.modules, (module:Module) => {
                return module.uniqueId === updatedModule.uniqueId;
            }));

            if (groupIndex !== -1) {
                this.modules[groupIndex] = updatedModule;
            }
            deferred.resolve(updatedModule);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };

        this.componentService.updateGroupMetadata(this.uniqueId, module).then(onSuccess, onFailed);

        return deferred.promise;
    };

    //------------------------------------------ Help Functions ----------------------------------------------------------------//

    public isService = ():boolean => {
        return this instanceof Service;
    };

    public isResource = ():boolean => {
        return this instanceof Resource;
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
        this.tags = angular.copy(componentMetadata.tags, this.tags);
        this.allVersions = componentMetadata.allVersions;
        this.componentType = componentMetadata.componentType;
        this.distributionStatus = componentMetadata.distributionStatus;
        this.highestVersion = componentMetadata.highestVersion;
        this.vendorName = componentMetadata.vendorName;
        this.vendorRelease = componentMetadata.vendorRelease;
        this.derivedList = componentMetadata.derivedList;
        this.normalizedName = componentMetadata.normalizedName;
        this.systemName = componentMetadata.systemName;
        this.categories = componentMetadata.categories;
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
        let temp = angular.copy(this);
        temp.componentService = undefined;
        temp.filterTerm = undefined;
        temp.iconSprite = undefined;
        temp.mainCategory = undefined;
        temp.subCategory = undefined;
        temp.selectedInstance = undefined;
        temp.showMenu = undefined;
        temp.$q = undefined;
        temp.selectedCategory = undefined;
        temp.modules = undefined
        temp.groupInstances = undefined;
        temp.policies = undefined;
        return temp;
    };
}

