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

import {AsdcComment, ArtifactModel, ArtifactGroupModel, IFileDownload, PropertyModel, PropertiesGroup, AttributeModel, AttributesGroup, ComponentInstance,
    InputModel, DisplayModule, Module, IValidate, RelationshipModel, IMainCategory, RequirementsGroup, CapabilitiesGroup, AdditionalInformationModel,
    Resource, IAppMenu, Service} from "../../models";

import {IComponentService} from "../../services/components/component-service";
import {CommonUtils} from "../../utils/common-utils";
import {QueueUtils} from "../../utils/functions";
import {ArtifactGroupType} from "../../utils/constants";
import {ComponentMetadata} from "../component-metadata";

// import {}
export interface IComponent {

    //---------------------------------------------- API CALLS ----------------------------------------------------//

    //Component API
    getComponent():ng.IPromise<Component>;
    updateComponent():ng.IPromise<Component>;
    createComponentOnServer():ng.IPromise<Component>;
    changeLifecycleState(state:string, commentObj:AsdcComment):ng.IPromise<Component>;
    validateName(newName:string):ng.IPromise<IValidate>;
    updateRequirementsCapabilities():ng.IPromise<any>;

    //Artifacts API
    addOrUpdateArtifact(artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    updateMultipleArtifacts(artifacts:Array<ArtifactModel>):ng.IPromise<any>;
    deleteArtifact(artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel>;
    downloadInstanceArtifact(artifactId:string):ng.IPromise<IFileDownload>;
    downloadArtifact(artifactId:string):ng.IPromise<IFileDownload>;
    getArtifactByGroupType(artifactGroupType:string):ng.IPromise<ArtifactGroupModel>;


    //Property API
    addOrUpdateProperty(property:PropertyModel):ng.IPromise<PropertyModel>;
    deleteProperty(propertyId:string):ng.IPromise<PropertyModel>;
    updateInstanceProperty(property:PropertyModel):ng.IPromise<PropertyModel>;

    //Attribute API
    deleteAttribute(attributeId:string):ng.IPromise<AttributeModel>;
    addOrUpdateAttribute(attribute:AttributeModel):ng.IPromise<AttributeModel>;
    updateInstanceAttribute(attribute:AttributeModel):ng.IPromise<AttributeModel>;




    //Component Instance API
    createComponentInstance(componentInstance:ComponentInstance):ng.IPromise<ComponentInstance>;
    deleteComponentInstance(componentInstanceId:string):ng.IPromise<ComponentInstance>;
    addOrUpdateInstanceArtifact(artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    deleteInstanceArtifact(artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel>;
    uploadInstanceEnvFile(artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    changeComponentInstanceVersion(componentUid:string):ng.IPromise<Component>;
    updateComponentInstance(componentInstance:ComponentInstance):ng.IPromise<ComponentInstance>;
    updateMultipleComponentInstances(instances:Array<ComponentInstance>):ng.IPromise<Array<ComponentInstance>>;

    //Inputs API
    getComponentInstanceInputProperties(componentInstanceId:string, inputId:string):ng.IPromise<Array<PropertyModel>>
    getComponentInstanceProperties(componentInstanceId:string):ng.IPromise<Array<PropertyModel>>
    getComponentInputs(componentId:string):ng.IPromise<Array<InputModel>>;

    createRelation(link:RelationshipModel):ng.IPromise<RelationshipModel>;
    deleteRelation(link:RelationshipModel):ng.IPromise<RelationshipModel>;


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
    getAdditionalInformation():Array<AdditionalInformationModel>;
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
    public distributionStatus:string;
    public categories:Array<IMainCategory>;
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
    public projectCode:string;
    public groups:Array<Module>;
    //custom properties
    public componentService:IComponentService;
    public filterTerm:string;
    public iconSprite:string;
    public selectedInstance:ComponentInstance;
    public mainCategory:string;
    public subCategory:string;
    public selectedCategory:string;
    public showMenu:boolean;


    constructor(componentService:IComponentService, protected $q:ng.IQService, component?:Component) {
        if (component) {
            this.abstract = component.abstract;
            this.uniqueId = component.uniqueId;
            this.uuid = component.uuid;
            this.invariantUUID = component.invariantUUID;
            this.additionalInformation = component.additionalInformation;
            this.artifacts = new ArtifactGroupModel(component.artifacts);
            this.toscaArtifacts = new ArtifactGroupModel(component.toscaArtifacts);
            this.contactId = component.contactId;
            this.categories = component.categories;
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
            this.projectCode = component.projectCode;
            this.inputs = component.inputs;
            this.componentInstances = CommonUtils.initComponentInstances(component.componentInstances);
            this.properties = CommonUtils.initProperties(component.properties, this.uniqueId);
            this.attributes = CommonUtils.initAttributes(component.attributes, this.uniqueId);
            this.selectedInstance = component.selectedInstance;
            this.iconSprite = component.iconSprite;
            this.showMenu = true;
            this.groups = CommonUtils.initModules(component.groups);
        }

        //custom properties
        this.componentService = componentService;
    }

    public setUniqueId = (uniqueId:string):void => {
        this.uniqueId = uniqueId;
    };

    public setSelectedInstance = (componentInstance:ComponentInstance):void => {
        this.selectedInstance = componentInstance;
    };


    //------------------------------------------ API Calls ----------------------------------------------------------------//
    public changeLifecycleState = (state:string, commentObj:AsdcComment):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        let onSuccess = (componentMetadata:ComponentMetadata):void => {
            this.setComponentMetadata(componentMetadata);
            // this.version = componentMetadata.version;
            this.lifecycleState = componentMetadata.lifecycleState;

            deferred.resolve(this);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.changeLifecycleState(this, state, JSON.stringify(commentObj)).then(onSuccess, onError);
        return deferred.promise;
    };

    public getComponent = ():ng.IPromise<Component> => {
        return this.componentService.getComponent(this.uniqueId);
    };

    public createComponentOnServer = ():ng.IPromise<Component> => {
        this.handleTags();
        return this.componentService.createComponent(this);
    };

    public updateComponent = ():ng.IPromise<Component> => {
        this.handleTags();
        return this.componentService.updateComponent(this);
    };

    public validateName = (newName:string, subtype?:string):ng.IPromise<IValidate> => {
        return this.componentService.validateName(newName, subtype);
    };

    public downloadArtifact = (artifactId:string):ng.IPromise<IFileDownload> => {
        return this.componentService.downloadArtifact(this.uniqueId, artifactId);
    };

    public addOrUpdateArtifact = (artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
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

    public updateMultipleArtifacts = (artifacts:Array<ArtifactModel>):ng.IPromise<any>=> {
        let deferred = this.$q.defer();
        let onSuccess = (response:any):void => {
            deferred.resolve(response);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };
        let q = new QueueUtils(this.$q);

        _.forEach(artifacts, (artifact)=> {
            q.addBlockingUIAction(()=> this.addOrUpdateArtifact(artifact).then(onSuccess, onError));
        });
        return deferred.promise;
    };


    public deleteArtifact = (artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
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

    public getArtifactByGroupType = (artifactGroupType:string):ng.IPromise<ArtifactGroupModel> => {

        let deferred = this.$q.defer();
        let onSuccess = (response:ArtifactGroupModel):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getArtifactByGroupType(this.uniqueId, artifactGroupType).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public getComponentInstanceArtifactsByGroupType = (componentInstanceId:string, artifactGroupType:string):ng.IPromise<ArtifactGroupModel> => {

        let deferred = this.$q.defer();
        let onSuccess = (response:ArtifactGroupModel):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInstanceArtifactsByGroupType(this.uniqueId, componentInstanceId, artifactGroupType).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public addOrUpdateProperty = (property:PropertyModel):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer();

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
        let deferred = this.$q.defer();

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
        let deferred = this.$q.defer();
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
        let deferred = this.$q.defer();
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

    public updateInstanceProperty = (property:PropertyModel):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer();
        let onSuccess = (newProperty:PropertyModel):void => {
            // find exist instance property in parent component for update the new value ( find bu uniqueId & path)
            let existProperty:PropertyModel = <PropertyModel>_.find(this.componentInstancesProperties[newProperty.resourceInstanceUniqueId], {
                uniqueId: newProperty.uniqueId,
                path: newProperty.path
            });
            let index = this.componentInstancesProperties[newProperty.resourceInstanceUniqueId].indexOf(existProperty);
            this.componentInstancesProperties[newProperty.resourceInstanceUniqueId][index] = newProperty;
            deferred.resolve(newProperty);
        };
        let onFailed = (error:any):void => {
            console.log('Failed to update property value');
            deferred.reject(error);
        };
        this.componentService.updateInstanceProperty(this.uniqueId, property).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public updateInstanceAttribute = (attribute:AttributeModel):ng.IPromise<AttributeModel> => {
        let deferred = this.$q.defer();
        let onSuccess = (newAttribute:AttributeModel):void => {
            let existAttribute:AttributeModel = <AttributeModel>_.find(this.componentInstancesAttributes[newAttribute.resourceInstanceUniqueId], {uniqueId: newAttribute.uniqueId});
            let index = this.componentInstancesAttributes[newAttribute.resourceInstanceUniqueId].indexOf(existAttribute);
            this.componentInstancesAttributes[newAttribute.resourceInstanceUniqueId][index] = newAttribute;
            deferred.resolve(newAttribute);
        };
        let onFailed = (error:any):void => {
            console.log('Failed to update attribute value');
            deferred.reject(error);
        };
        this.componentService.updateInstanceAttribute(this.uniqueId, attribute).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public downloadInstanceArtifact = (artifactId:string):ng.IPromise<IFileDownload> => {
        return this.componentService.downloadInstanceArtifact(this.uniqueId, this.selectedInstance.uniqueId, artifactId);
    };

    public deleteInstanceArtifact = (artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        let onSuccess = (artifactObj:ArtifactModel):void => {
            let newArtifact = new ArtifactModel(artifactObj);
            let artifacts = this.selectedInstance.deploymentArtifacts;
            if (newArtifact.mandatory || newArtifact.serviceApi) {//?????????
                artifacts[newArtifact.artifactLabel] = newArtifact;
            }
            else {
                delete artifacts[artifactLabel];
            }
            deferred.resolve(newArtifact);
        };
        this.componentService.deleteInstanceArtifact(this.uniqueId, this.selectedInstance.uniqueId, artifactId, artifactLabel).then(onSuccess);
        return deferred.promise;
    };

    public addOrUpdateInstanceArtifact = (artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        let onSuccess = (artifactObj:ArtifactModel):void => {
            switch (artifactObj.artifactGroupType) {
                case ArtifactGroupType.DEPLOYMENT:
                    this.selectedInstance.deploymentArtifacts[artifactObj.artifactLabel] = artifactObj;
                    break;
                case ArtifactGroupType.INFORMATION:
                    this.selectedInstance.artifacts[artifactObj.artifactLabel] = artifactObj;
                    break;
            }
            deferred.resolve(artifactObj);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };
        if (artifact.uniqueId) {
            this.componentService.updateInstanceArtifact(this.uniqueId, this.selectedInstance.uniqueId, artifact).then(onSuccess, onError);
        } else {
            this.componentService.addInstanceArtifact(this.uniqueId, this.selectedInstance.uniqueId, artifact).then(onSuccess, onError);
        }
        return deferred.promise;
    };

    public uploadInstanceEnvFile = (artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        let onSuccess = (artifactObj:ArtifactModel):void => {
            this.selectedInstance.deploymentArtifacts[artifactObj.artifactLabel] = artifactObj;
            deferred.resolve(artifactObj);
        };
        let onError = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.uploadInstanceEnvFile(this.uniqueId, this.selectedInstance.uniqueId, artifact).then(onSuccess, onError);
        return deferred.promise;
    };

    //this function will update the instance version than the function call getComponent to update the current component and return the new instance version
    public changeComponentInstanceVersion = (componentUid:string):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        let onSuccess = (componentInstance:ComponentInstance):void => {
            let onSuccess = (component:Component):void => {
                component.setSelectedInstance(componentInstance);
                deferred.resolve(component);
            };
            this.getComponent().then(onSuccess, onFailed);
        };
        this.componentService.changeResourceInstanceVersion(this.uniqueId, this.selectedInstance.uniqueId, componentUid).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public createComponentInstance = (componentInstance:ComponentInstance):ng.IPromise<ComponentInstance> => {
        let deferred = this.$q.defer();
        let onSuccess = (instance:ComponentInstance):void => {
            this.componentInstances.push(instance);
            deferred.resolve(instance);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.createComponentInstance(this.uniqueId, componentInstance).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public updateComponentInstance = (componentInstance:ComponentInstance):ng.IPromise<ComponentInstance> => {
        let deferred = this.$q.defer();
        let onSuccess = (updatedInstance:ComponentInstance):void => {
            let componentInstance:ComponentInstance = _.find(this.componentInstances, (instance:ComponentInstance) => {
                return instance.uniqueId === updatedInstance.uniqueId;
            });

            let index = this.componentInstances.indexOf(componentInstance);
            this.componentInstances[index] = componentInstance;
            deferred.resolve(updatedInstance);

        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.updateComponentInstance(this.uniqueId, componentInstance).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public updateMultipleComponentInstances = (instances:Array<ComponentInstance>):ng.IPromise<Array<ComponentInstance>> => {
        let deferred = this.$q.defer();
        let onSuccess = (updatedInstances:Array<ComponentInstance>):void => {
            _.forEach(updatedInstances, (updatedComponentInstance) => {
                let componentInstance:ComponentInstance = _.find(this.componentInstances, (instance:ComponentInstance) => {
                    return instance.uniqueId === updatedComponentInstance.uniqueId;
                });

                let index = this.componentInstances.indexOf(componentInstance);
                this.componentInstances[index] = componentInstance;

            });
            deferred.resolve(updatedInstances);

        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.updateMultipleComponentInstances(this.uniqueId, instances).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public deleteComponentInstance = (componentInstanceId:string):ng.IPromise<ComponentInstance> => {
        let deferred = this.$q.defer();
        let onSuccess = ():void => {
            let onSuccess = (component:Component):void => {
                this.componentInstances = CommonUtils.initComponentInstances(component.componentInstances);
                this.componentInstancesProperties = new PropertiesGroup(component.componentInstancesProperties);
                this.componentInstancesAttributes = new AttributesGroup(component.componentInstancesAttributes);
                this.groups = component.groups;
                this.componentInstancesRelations = CommonUtils.initComponentInstanceRelations(component.componentInstancesRelations);
                deferred.resolve();
            };
            this.getComponent().then(onSuccess);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.deleteComponentInstance(this.uniqueId, componentInstanceId).then(onSuccess, onFailed);
        return deferred.promise;
    };


    public createRelation = (relation:RelationshipModel):ng.IPromise<RelationshipModel> => {
        let deferred = this.$q.defer();
        let onSuccess = (relation:RelationshipModel):void => {
            console.info('Link created successfully', relation);
            if (!this.componentInstancesRelations) {
                this.componentInstancesRelations = [];
            }
            this.componentInstancesRelations.push(new RelationshipModel(relation));
            deferred.resolve(relation);
        };
        let onFailed = (error:any):void => {
            console.info('Failed to create relation', error);
            deferred.reject(error);
        };
        this.componentService.createRelation(this.uniqueId, relation).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public deleteRelation = (relation:RelationshipModel):ng.IPromise<RelationshipModel> => {
        let deferred = this.$q.defer();
        let onSuccess = (responseRelation:RelationshipModel):void => {
            console.log("Link Deleted In Server");
            let relationToDelete = _.find(this.componentInstancesRelations, (item) => {
                return item.fromNode === relation.fromNode && item.toNode === relation.toNode && _.some(item.relationships, (relationship)=> {
                        return angular.equals(relation.relationships[0], relationship);
                    });
            });
            let index = this.componentInstancesRelations.indexOf(relationToDelete);
            if (relationToDelete != undefined && index > -1) {
                if (relationToDelete.relationships.length == 1) {
                    this.componentInstancesRelations.splice(index, 1);
                } else {
                    this.componentInstancesRelations[index].relationships =
                        _.reject(this.componentInstancesRelations[index].relationships, relation.relationships[0]);
                }
            } else {
                console.error("Error while deleting relation - the return delete relation from server was not found in UI")
            }
            deferred.resolve(relation);
        };
        let onFailed = (error:any):void => {
            console.error("Failed To Delete Link");
            deferred.reject(error);
        };
        this.componentService.deleteRelation(this.uniqueId, relation).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public updateRequirementsCapabilities = ():ng.IPromise<any> => {
        let deferred = this.$q.defer();
        let onSuccess = (response:any):void => {
            this.capabilities = response.capabilities;
            this.requirements = response.requirements;
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getRequirementsCapabilities(this.uniqueId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    public getModuleForDisplay = (moduleId:string):ng.IPromise<DisplayModule> => {

        let deferred = this.$q.defer();
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

        let deferred = this.$q.defer();
        let onSuccess = (response:DisplayModule):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInstanceModule(this.uniqueId, componentInstanceId, moduleId).then(onSuccess, onFailed);
        return deferred.promise;
    };


    // this function get all instances filtered by inputs and properties (optional) - if no search string insert - this function will
    // get all the instances of the component (in service only VF instances)
    public getComponentInstancesFilteredByInputsAndProperties = (searchText?:string):ng.IPromise<Array<ComponentInstance>> => {

        let deferred = this.$q.defer();
        let onSuccess = (response:Array<ComponentInstance>):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInstancesFilteredByInputsAndProperties(this.uniqueId, searchText).then(onSuccess, onFailed);
        return deferred.promise;
    };


    // get inputs for instance - Pagination function
    public getComponentInputs = ():ng.IPromise<Array<InputModel>> => {

        let deferred = this.$q.defer();
        let onSuccess = (inputsRes:Array<InputModel>):void => {
            this.inputs = inputsRes;
            deferred.resolve(inputsRes);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInputs(this.uniqueId).then(onSuccess, onFailed);
        return deferred.promise;
    };


    // get inputs instance - Pagination function
    public getComponentInstanceInputs = (componentInstanceId:string, originComponentUid:string):ng.IPromise<Array<InputModel>> => {

        let deferred = this.$q.defer();
        let onSuccess = (response:Array<InputModel>):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInstanceInputs(this.uniqueId, componentInstanceId, originComponentUid).then(onSuccess, onFailed);
        return deferred.promise;
    };

    // get inputs inatnce - Pagination function
    public getComponentInstanceInputProperties = (componentInstanceId:string, inputId:string):ng.IPromise<Array<PropertyModel>> => {

        let deferred = this.$q.defer();
        let onSuccess = (response:Array<PropertyModel>):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInstanceInputProperties(this.uniqueId, componentInstanceId, inputId).then(onSuccess, onFailed);
        return deferred.promise;
    };

    // get inputs inatnce - Pagination function
    public getComponentInstanceProperties = (componentInstanceId:string):ng.IPromise<Array<PropertyModel>> => {

        let deferred = this.$q.defer();
        let onSuccess = (response:Array<PropertyModel>):void => {
            deferred.resolve(response);
        };
        let onFailed = (error:any):void => {
            deferred.reject(error);
        };
        this.componentService.getComponentInstanceProperties(this.uniqueId, componentInstanceId).then(onSuccess, onFailed);
        return deferred.promise;
    };


    public updateGroupMetadata = (module:Module):ng.IPromise<Module> => {

        let deferred = this.$q.defer();

        let onSuccess = (updatedModule:Module):void => {
            let groupIndex:number = _.indexOf(this.groups, _.find(this.groups, (module:Module) => {
                return module.uniqueId === updatedModule.uniqueId;
            }));

            if (groupIndex !== -1) {
                this.groups[groupIndex] = updatedModule;
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

    public getAdditionalInformation = ():Array<AdditionalInformationModel> => {
        let additionalInformationObject:any = _.find(this.additionalInformation, (obj:any):boolean => {
            return obj.parentUniqueId == this.uniqueId;
        });
        if (additionalInformationObject) {
            return additionalInformationObject.parameters;
        }
        return [];
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
        this.projectCode = componentMetadata.projectCode;
        this.categories = componentMetadata.categories;

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
        return temp;
    };
}

