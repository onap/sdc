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
import {ArtifactModel, IFileDownload, InstancesInputsPropertiesMap, InputModel, IValidate, RelationshipModel, PropertyModel, Component, ComponentInstance,
    AttributeModel, IAppConfigurtaion, Resource, Module, DisplayModule, ArtifactGroupModel, InputsAndProperties} from "app/models";
import {ComponentInstanceFactory, CommonUtils} from "app/utils";
import {SharingService} from "../sharing-service";
import {ComponentMetadata} from "../../models/component-metadata";

export interface IComponentService {

    getComponent(id:string);
    updateComponent(component:Component):ng.IPromise<Component>;
    changeLifecycleState(component:Component, state:string, userRemarks:any):ng.IPromise<ComponentMetadata> ;
    validateName(newName:string, subtype?:string):ng.IPromise<IValidate>;
    createComponent(component:Component):ng.IPromise<Component>;
    addOrUpdateArtifact(componentId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    deleteArtifact(componentId:string, artifact:string, artifactLabel):ng.IPromise<ArtifactModel>;
    addProperty(componentId:string, property:PropertyModel):ng.IPromise<PropertyModel>;
    updateProperty(componentId:string, property:PropertyModel):ng.IPromise<PropertyModel>;
    addAttribute(componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel>;
    updateAttribute(componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel>;
    deleteProperty(componentId:string, propertyId:string):ng.IPromise<PropertyModel>;
    deleteAttribute(componentId:string, attributeId:string):ng.IPromise<AttributeModel>;
    changeResourceInstanceVersion(componentId:string, componentInstanceId:string, componentUid:string):ng.IPromise<ComponentInstance>;
    updateInstanceArtifact(componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    addInstanceArtifact(componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    deleteInstanceArtifact(componentId:string, instanceId:string, artifact:string, artifactLabel):ng.IPromise<ArtifactModel>;
    createComponentInstance(componentId:string, componentInstance:ComponentInstance):ng.IPromise<ComponentInstance>;
    updateComponentInstance(componentId:string, componentInstance:ComponentInstance):ng.IPromise<ComponentInstance>;
    updateMultipleComponentInstances(componentId:string, instances:Array<ComponentInstance>):ng.IPromise< Array<ComponentInstance>>;
    downloadArtifact(componentId:string, artifactId:string):ng.IPromise<IFileDownload>;
    uploadInstanceEnvFile(componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel>;
    downloadInstanceArtifact(componentId:string, instanceId:string, artifactId:string):ng.IPromise<IFileDownload>;
    deleteComponentInstance(componentId:string, componentInstanceId:string):ng.IPromise<ComponentInstance>;
    createRelation(componentId:string, link:RelationshipModel):ng.IPromise<RelationshipModel>;
    deleteRelation(componentId:string, link:RelationshipModel):ng.IPromise<RelationshipModel>;
    fetchRelation(componentId:string, linkId:string):ng.IPromise<RelationshipModel>;
    getRequirementsCapabilities(componentId:string):ng.IPromise<any>;
    updateInstanceProperty(componentId:string, property:PropertyModel):ng.IPromise<PropertyModel>;
    updateInstanceAttribute(componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel>;
    getComponentInstancesFilteredByInputsAndProperties(componentId:string, searchText:string):ng.IPromise<Array<ComponentInstance>>
    getComponentInstanceInputs(componentId:string, instanceId:string, originComponentUid):ng.IPromise<Array<InputModel>>;
    getComponentInputs(componentId:string):ng.IPromise<Array<InputModel>>;
    getComponentInstanceInputProperties(componentId:string, instanceId:string, inputId:string):ng.IPromise<Array<PropertyModel>>;
    getComponentInstanceProperties(componentId:string, instanceId:string):ng.IPromise<Array<PropertyModel>>;
    getModuleForDisplay(componentId:string, moduleId:string):ng.IPromise<DisplayModule>;
    getComponentInstanceModule(componentId:string, componentInstanceId:string, moduleId:string):ng.IPromise<DisplayModule>;
    updateGroupMetadata(componentId:string, group:Module):ng.IPromise<Module>;
    getComponentInputInputsAndProperties(serviceId:string, input:string):ng.IPromise<InputsAndProperties>;
    createInputsFromInstancesInputs(serviceId:string, instancesInputsMap:InstancesInputsPropertiesMap):ng.IPromise<Array<InputModel>>;
    createInputsFromInstancesInputsProperties(resourceId:string, instanceInputsPropertiesMap:InstancesInputsPropertiesMap):ng.IPromise<Array<PropertyModel>>;
    deleteComponentInput(serviceId:string, inputId:string):ng.IPromise<InputModel>;
    getArtifactByGroupType(componentId:string, artifactGroupType:string):ng.IPromise<ArtifactGroupModel>;
    getComponentInstanceArtifactsByGroupType(componentId:string, componentInstanceId:string, artifactGroupType:string):ng.IPromise<ArtifactGroupModel>;
}

export class ComponentService implements IComponentService {

    static '$inject' = [
        'Restangular',
        'sdcConfig',
        'Sdc.Services.SharingService',
        '$q',
        '$base64'
    ];

    constructor(protected restangular:restangular.IElement,
                protected sdcConfig:IAppConfigurtaion,
                protected sharingService:SharingService,
                protected $q:ng.IQService,
                protected $base64:any
               ) {

        this.restangular.setBaseUrl(sdcConfig.api.root + sdcConfig.api.component_api_root);
        this.restangular.setRequestInterceptor(function (elem, operation) {
            if (operation === "remove") {
                return null;
            }
            return elem;
        });
        //    this.restangular.setDefaultHeaders({'Content-Type': 'application/json; charset=UTF-8'});
    }

    //this function is override by each service, we need to change this method to abstract when updtaing typescript version
    protected createComponentObject = (component:Component):Component => {
        return component;
    };

    public getComponent = (id:string):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        this.restangular.one(id).get().then((response:Component) => {
            let component:Component = this.createComponentObject(response);
            //console.log("Component Loaded successfully : ", component);
            deferred.resolve(component);
        }, (err)=> {
            console.log("Failed to load component with ID: " + id);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateComponent = (component:Component):ng.IPromise<Component> => {
        // If this is resource
        if (component instanceof Resource) {
            let resource:Resource = <Resource>component;
            if (resource.importedFile) {
                // Update resource with payload data.
                return this.updateResourceWithPayload(resource);
            } else {
                if (component.csarUUID) {
                    // Update resource without payload data.
                    return this.updateResource(component);
                } else {
                    // Update resource without payload data (metadata).
                    return this.updateResourceMetadata(component);
                }
            }
        } else {
            return this.updateService(component);
        }
    };

    private updateService = (component:Component):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        this.restangular.one(component.uniqueId).one("metadata").customPUT(JSON.stringify(component)).then((response:Component) => {
            let component:Component = this.createComponentObject(response);
            deferred.resolve(component);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    private updateResource = (component:Component):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        this.restangular.one(component.uniqueId).customPUT(JSON.stringify(component)).then((response:Component) => {
            let component:Component = this.createComponentObject(response);
            deferred.resolve(component);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    private updateResourceMetadata = (component:Component):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        this.restangular.one(component.uniqueId).one('metadata').customPUT(JSON.stringify(component)).then((response:Component) => {
            let component:Component = this.createComponentObject(response);
            deferred.resolve(component);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    /**
     * Only resource can be updated with payload data
     * @param component
     * @returns {IPromise<T>}
     */
    private updateResourceWithPayload = (resource:Resource):ng.IPromise<Component> => {
        let deferred = this.$q.defer();

        resource.payloadData = resource.importedFile.base64;
        resource.payloadName = resource.importedFile.filename;
        let headerObj = this.getHeaderMd5(resource);

        this.restangular.one(resource.uniqueId).customPUT(JSON.stringify(resource), '', {}, headerObj).then((response:Component) => {
            let componentResult:Component = this.createComponentObject(response);
            deferred.resolve(componentResult);
        }, (err)=> {
            deferred.reject(err);
        });

        return deferred.promise;
    };

    public createComponent = (component:Component):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        let headerObj = this.getHeaderMd5(component);
        this.restangular.customPOST(JSON.stringify(component), '', {}, headerObj).then((response:Component) => {
            let component:Component = this.createComponentObject(response);
            deferred.resolve(component);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public validateName = (newName:string, subtype?:string):ng.IPromise<IValidate> => {
        let deferred = this.$q.defer();
        this.restangular.one("validate-name").one(newName).get({'subtype': subtype}).then((response:any) => {
            deferred.resolve(response.plain());
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public changeLifecycleState = (component:Component, state:string, userRemarks:any):ng.IPromise<ComponentMetadata> => {
        let deferred = this.$q.defer();
        this.restangular.one(component.uniqueId).one(state).customPOST(userRemarks).then((response:ComponentMetadata) => {
            this.sharingService.addUuidValue(response.uniqueId, response.uuid);
            let component:ComponentMetadata = new ComponentMetadata().deserialize(response);
            deferred.resolve(component);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    // ------------------------------------------------ Artifacts API --------------------------------------------------//
    public addOrUpdateArtifact = (componentId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        let headerObj = {};
        if (artifact.payloadData) {
            headerObj = this.getHeaderMd5(artifact);
        }
        this.restangular.one(componentId).one("artifacts").customPOST(JSON.stringify(artifact), artifact.uniqueId, {}, headerObj).then((response:any) => {
            deferred.resolve(response.plain());
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public downloadArtifact = (componentId:string, artifactId:string):ng.IPromise<IFileDownload> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("artifacts").one(artifactId).get().then((response:any) => {
            deferred.resolve(response.plain());
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public deleteArtifact = (componentId:string, artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("artifacts").one(artifactId).remove({'operation': artifactLabel}).then((response:ArtifactModel) => {
            deferred.resolve(response);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public getArtifactByGroupType = (componentId:string, artifactGroupType:string):ng.IPromise<ArtifactGroupModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("artifactsByType").one(artifactGroupType).get().then((response:any) => {
            var artifacts:ArtifactGroupModel = new ArtifactGroupModel(response.plain());
            deferred.resolve(artifacts);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public getComponentInstanceArtifactsByGroupType = (componentId:string, componentInstanceId:string, artifactGroupType:string):ng.IPromise<ArtifactGroupModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstances").one(componentInstanceId).one("artifactsByType").one(artifactGroupType).get().then((response:any) => {
            var artifacts:ArtifactGroupModel = new ArtifactGroupModel(response.plain());
            deferred.resolve(artifacts);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };


    // ------------------------------------------------ Properties API --------------------------------------------------//
    public addProperty = (componentId:string, property:PropertyModel):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("properties").customPOST(property.convertToServerObject()).then((response:any) => {
            let property:PropertyModel = new PropertyModel(response[Object.keys(response)[0]]);
            deferred.resolve(property);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateProperty = (componentId:string, property:PropertyModel):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("properties").one(property.uniqueId).customPUT(property.convertToServerObject()).then((response:any) => {
            let property:PropertyModel = new PropertyModel(response[Object.keys(response)[0]]);
            deferred.resolve(property);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public deleteProperty = (componentId:string, propertyId:string):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("properties").one(propertyId).remove().then((response:any) => {
            deferred.resolve(response);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    // ------------------------------------------------ Attributes API --------------------------------------------------//
    public addAttribute = (componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("attributes").customPOST(attribute.convertToServerObject()).then((response:any) => {
            let attribute:AttributeModel = new AttributeModel(response);
            deferred.resolve(attribute);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateAttribute = (componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("attributes").one(attribute.uniqueId).customPUT(attribute.convertToServerObject()).then((response:any) => {
            let attribute:AttributeModel = new AttributeModel(response);
            deferred.resolve(attribute);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public deleteAttribute = (componentId:string, attributeId:string):ng.IPromise<AttributeModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("attributes").one(attributeId).remove().then((response:any) => {
            deferred.resolve(response);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    // ------------------------------------------------ Component Instances API --------------------------------------------------//

    public createComponentInstance = (componentId:string, componentInstance:ComponentInstance):ng.IPromise<ComponentInstance> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstance").customPOST(JSON.stringify(componentInstance)).then((response:any) => {
            let componentInstance:ComponentInstance = ComponentInstanceFactory.createComponentInstance(response);
            console.log("Component Instance created", componentInstance);
            deferred.resolve(componentInstance);
        }, (err)=> {
            console.log("Failed to create componentInstance. With Name: " + componentInstance.name);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateComponentInstance = (componentId:string, componentInstance:ComponentInstance):ng.IPromise<ComponentInstance> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstance").one(componentInstance.uniqueId).customPOST(JSON.stringify(componentInstance)).then((response:any) => {
            let componentInstance:ComponentInstance = ComponentInstanceFactory.createComponentInstance(response);
            console.log("Component Instance was updated", componentInstance);
            deferred.resolve(componentInstance);
        }, (err)=> {
            console.log("Failed to update componentInstance. With ID: " + componentInstance.uniqueId + "Name: " + componentInstance.name);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateMultipleComponentInstances = (componentId:string, instances:Array<ComponentInstance>):ng.IPromise<Array<ComponentInstance>> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstance/multipleComponentInstance").customPOST(JSON.stringify(instances)).then((response:any) => {
            console.log("Multiple Component Instances was updated", response);
            let updateInstances:Array<ComponentInstance> = new Array<ComponentInstance>();
            _.forEach(response, (componentInstance:ComponentInstance) => {
                let updatedComponentInstance:ComponentInstance = ComponentInstanceFactory.createComponentInstance(componentInstance);
                updateInstances.push(updatedComponentInstance);
            });
            deferred.resolve(updateInstances);
        }, (err)=> {
            console.log("Failed to update Multiple componentInstance.");
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public deleteComponentInstance = (componentId:string, componentInstanceId:string):ng.IPromise<ComponentInstance> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstance").one(componentInstanceId).remove().then(() => {
            console.log("Component Instance was deleted");
            deferred.resolve();
        }, (err)=> {
            console.log("Failed to delete componentInstance. With ID: " + componentInstanceId);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public changeResourceInstanceVersion = (componentId:string, componentInstanceId:string, componentUid:string):ng.IPromise<ComponentInstance> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstance").one(componentInstanceId).one("changeVersion").customPOST({'componentUid': componentUid}).then((response:any) => {
            let componentInstance:ComponentInstance = ComponentInstanceFactory.createComponentInstance(response);
            deferred.resolve(componentInstance);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public downloadInstanceArtifact = (componentId:string, instanceId:string, artifactId:string):ng.IPromise<IFileDownload> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstances").one(instanceId).one("artifacts").one(artifactId).get().then((response:any) => {
            deferred.resolve(response.plain());
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateInstanceArtifact = (componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        let headerObj = {};
        if (artifact.payloadData) {
            headerObj = this.getHeaderMd5(artifact);
        }
        this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").customPOST(JSON.stringify(artifact), artifact.uniqueId, {}, headerObj).then((response:any) => {
            let newArtifact = new ArtifactModel(response);
            deferred.resolve(newArtifact);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public addInstanceArtifact = (componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        let headerObj = {};
        if (artifact.payloadData) {
            headerObj = this.getHeaderMd5(artifact);
        }
        this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").customPOST(JSON.stringify(artifact), artifact.uniqueId, {}, headerObj).then((response:any) => {
            let artifact:ArtifactModel = new ArtifactModel(response.plain());
            deferred.resolve(artifact);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public deleteInstanceArtifact = (componentId:string, instanceId:string, artifactId:string, artifactLabel:string):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").one(artifactId).remove({'operation': artifactLabel}).then((response:ArtifactModel) => {
            deferred.resolve(response);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public uploadInstanceEnvFile = (componentId:string, instanceId:string, artifact:ArtifactModel):ng.IPromise<ArtifactModel> => {
        let deferred = this.$q.defer();
        let headerObj = {};
        if (artifact.payloadData) {
            headerObj = this.getHeaderMd5(artifact);
        }
        this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").customPOST(JSON.stringify(artifact), artifact.uniqueId, {}, headerObj).then((response:any) => {
            let newArtifact = new ArtifactModel(response);
            deferred.resolve(newArtifact);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateInstanceProperty = (componentId:string, property:PropertyModel):ng.IPromise<PropertyModel> => {
        let deferred = this.$q.defer();
        let instanceId = property.resourceInstanceUniqueId;
        this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("property").customPOST(JSON.stringify(property)).then((response:any) => {
            let newProperty = new PropertyModel(response);
            newProperty.readonly = true;
            newProperty.resourceInstanceUniqueId = instanceId;
            deferred.resolve(newProperty);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public updateInstanceAttribute = (componentId:string, attribute:AttributeModel):ng.IPromise<AttributeModel> => {
        let deferred = this.$q.defer();
        let instanceId = attribute.resourceInstanceUniqueId;
        this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("attribute").customPOST(JSON.stringify(attribute)).then((response:any) => {
            let newAttribute = new AttributeModel(response);
            newAttribute.readonly = true;
            newAttribute.resourceInstanceUniqueId = instanceId;
            deferred.resolve(newAttribute);
        }, (err)=> {
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public createRelation = (componentId:string, link:RelationshipModel):ng.IPromise<RelationshipModel> => {
        let deferred = this.$q.defer();
        const linkPayload:RelationshipModel = new RelationshipModel(link);
        linkPayload.relationships.forEach((rel) => {
            delete rel.capability;
            delete rel.requirement;
        });
        this.restangular.one(componentId).one("resourceInstance").one("associate").customPOST(JSON.stringify(linkPayload)).then((response:any) => {
            let relation:RelationshipModel = new RelationshipModel(response.plain());
            console.log("Link created successfully ", relation);
            deferred.resolve(relation);
        }, (err)=> {
            console.log("Failed to create Link From: " + link.fromNode + "To: " + link.toNode);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public deleteRelation = (componentId:string, link:RelationshipModel):ng.IPromise<RelationshipModel> => {
        let deferred = this.$q.defer();
        const linkPayload:RelationshipModel = new RelationshipModel(link);
        linkPayload.relationships.forEach((rel) => {
            delete rel.capability;
            delete rel.requirement;
        });
        this.restangular.one(componentId).one("resourceInstance").one("dissociate").customPUT(JSON.stringify(linkPayload)).then((response:any) => {
            let relation:RelationshipModel = new RelationshipModel(response);
            console.log("Link deleted successfully ", relation);
            deferred.resolve(relation);
        }, (err)=> {
            console.log("Failed to delete Link From: " + link.fromNode + "To: " + link.toNode);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public fetchRelation = (componentId:string, linkId:string):ng.IPromise<RelationshipModel> => {
        let deferred = this.$q.defer<RelationshipModel>();
        this.restangular.one(componentId).one("relationId").one(linkId).get().then((response:any) => {
            let relation:RelationshipModel = new RelationshipModel(response);
            console.log("Link fetched successfully ", relation);
            deferred.resolve(relation);
        }, (err)=> {
            console.log("Failed to fetch Link Id: " + linkId);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public getRequirementsCapabilities = (componentId:string):ng.IPromise<any> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("requirmentsCapabilities").get().then((response:any) => {
            console.log("Component requirement capabilities recived: ", response);
            deferred.resolve(response);
        }, (err)=> {
            console.log("Failed to get requirements & capabilities");
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public getModuleForDisplay = (componentId:string, moduleId:string):ng.IPromise<DisplayModule> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("groups").one(moduleId).get().then((response:any) => {
            console.log("module loaded successfully: ", response);
            let module:DisplayModule = new DisplayModule(response);
            deferred.resolve(module);
        }, (err)=> {
            console.log("Failed to get module with id: ", moduleId);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public getComponentInstanceModule = (componentId:string, componentInstanceId:string, moduleId:string):ng.IPromise<Module> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("resourceInstance").one(componentInstanceId).one("groupInstance").one(moduleId).get().then((response:any) => {
            console.log("module loaded successfully: ", response);
            let module:DisplayModule = new DisplayModule(response);
            deferred.resolve(module);
        }, (err)=> {
            console.log("Failed to get module with id: ", moduleId);
            deferred.reject(err);
        });
        return deferred.promise;
    };

    public getComponentInstancesFilteredByInputsAndProperties = (componentId:string, searchText?:string):ng.IPromise<Array<ComponentInstance>> => {
        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("componentInstances").get({'searchText': searchText}).then((response:any) => {
            console.log("component instances return successfully: ", response);
            let componentInstances:Array<ComponentInstance> = CommonUtils.initComponentInstances(response);
            deferred.resolve(componentInstances);
        }, (err) => {
            console.log("Failed to get component instances of component with id: " + componentId);
            deferred.reject(err);
        });

        return deferred.promise;
    };

    public getComponentInstanceInputs = (componentId:string, instanceId:string, originComponentUid):ng.IPromise<Array<InputModel>> => {

        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("componentInstances").one(instanceId).one(originComponentUid).one("inputs").get().then((response:any) => {
            console.log("component instance input return successfully: ", response);
            let inputsArray:Array<InputModel> = new Array<InputModel>();
            _.forEach(response, (inputObj:InputModel) => {
                inputsArray.push(new InputModel(inputObj));
            });
            deferred.resolve(inputsArray);
        }, (err) => {
            console.log("Failed to get component instance input with id: " + instanceId);
            deferred.reject(err);
        });

        return deferred.promise;
    };

    public getComponentInputs = (componentId:string):ng.IPromise<Array<InputModel>> => {

        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("inputs").get().then((response:any) => {
            console.log("component inputs return successfully: ", response);
            let inputsArray:Array<InputModel> = new Array<InputModel>();
            _.forEach(response, (inputObj:InputModel) => {
                inputsArray.push(new InputModel(inputObj));
            });
            deferred.resolve(inputsArray);
        }, (err) => {
            console.log("Failed to get component inputs for component with id: " + componentId);
            deferred.reject(err);
        });

        return deferred.promise;
    };

    public getComponentInstanceInputProperties = (componentId:string, instanceId:string, inputId:string):ng.IPromise<Array<PropertyModel>> => {

        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("componentInstances").one(instanceId).one(inputId).one("properties").get().then((response:any) => {
            console.log("component instance input properties return successfully: ", response);
            let propertiesArray:Array<PropertyModel> = new Array<PropertyModel>();
            _.forEach(response, (propertyObj:PropertyModel) => {
                propertiesArray.push(new PropertyModel(propertyObj));
            });
            deferred.resolve(propertiesArray);
        }, (err) => {
            console.log("Failed to get component instance input properties with instanceId: " + instanceId + "and input id: " + inputId);
            deferred.reject(err);
        });

        return deferred.promise;
    };


    public getComponentInstanceProperties = (componentId:string, instanceId:string):ng.IPromise<Array<PropertyModel>> => {

        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("componentInstances").one(instanceId).one("properties").get().then((response:any) => {
            console.log("component instance  properties return successfully: ", response);
            let propertiesArray:Array<PropertyModel> = new Array<PropertyModel>();
            _.forEach(response, (propertyObj:PropertyModel) => {
                propertiesArray.push(new PropertyModel(propertyObj));
            });
            deferred.resolve(propertiesArray);
        }, (err) => {
            console.log("Failed to get component instance  properties with instanceId: " + instanceId);
            deferred.reject(err);
        });

        return deferred.promise;
    };

    public updateGroupMetadata = (componentId:string, group:Module):ng.IPromise<Module> => {

        let deferred = this.$q.defer();
        this.restangular.one(componentId).one("groups").one(group.uniqueId).one("metadata").customPUT(JSON.stringify(group)).then((response:Module) => {
            console.log("group metadata updated successfully: ", response);
            let updatedGroup:Module = new Module(response);

            deferred.resolve(updatedGroup);
        }, (err) => {
            console.log("Failed to update group metadata for component: " + componentId + " for group with id: " + group.uniqueId);
            deferred.reject(err);
        });

        return deferred.promise;
    };

    public getComponentInputInputsAndProperties = (serviceId:string, inputId:string):ng.IPromise<InputsAndProperties> => {
        let defer = this.$q.defer<any>();
        this.restangular.one(serviceId).one("inputs").one(inputId).get().then((response:InputsAndProperties) => {

            let inputsArray:Array<InputModel> = new Array<InputModel>();
            _.forEach(response.inputs, (inputObj:InputModel) => {
                inputsArray.push(new InputModel(inputObj));
            });

            let propertiesArray:Array<PropertyModel> = new Array<PropertyModel>();
            _.forEach(response.properties, (property:PropertyModel) => {
                propertiesArray.push(new PropertyModel(property));
            });

            defer.resolve(new InputsAndProperties(inputsArray, propertiesArray));
        }, (err)=> {
            console.log("failed to get inputs of input : ", err);
            defer.reject(err);
        });
        return defer.promise;
    };

    createInputsFromInstancesInputsProperties = (resourceId:string, instancePropertyMap:InstancesInputsPropertiesMap):ng.IPromise<Array<PropertyModel>> => {
        let defer = this.$q.defer<any>();
        this.restangular.one(resourceId).one("create/properties").customPOST(instancePropertyMap).then((response:any) => {
            let inputsArray:Array<PropertyModel> = new Array<PropertyModel>();
            _.forEach(response, (inputObj:PropertyModel) => {
                inputsArray.push(new PropertyModel(inputObj));
            });
            defer.resolve(inputsArray);
        }, (err)=> {
            console.log("failed to create service inputs from VF instances inputs : ", err);
            defer.reject(err);
        });
        return defer.promise;
    };

    createInputsFromInstancesInputs = (serviceId:string, instancesMap:InstancesInputsPropertiesMap):ng.IPromise<Array<InputModel>> => {
        let defer = this.$q.defer<any>();
        this.restangular.one(serviceId).one("create/inputs").customPOST(instancesMap).then((response:any) => {
            let inputsArray:Array<InputModel> = new Array<InputModel>();
            _.forEach(response, (inputObj:InputModel) => {
                inputsArray.push(new InputModel(inputObj));
            });
            defer.resolve(inputsArray);
        }, (err)=> {
            console.log("failed to create service inputs from VF instances inputs : ", err);
            defer.reject(err);
        });
        return defer.promise;
    };

    deleteComponentInput = (serviceId:string, inputId:string):ng.IPromise<InputModel> => {
        let defer = this.$q.defer();
        this.restangular.one(serviceId).one("delete").one(inputId).one("input").remove().then((response:any) => {
            let inputToDelete = new InputModel(response);
            defer.resolve(inputToDelete);
        }, (err)=> {
            console.log("failed to delete input from service: ", err);
            defer.reject(err);
        });
        return defer.promise;
    };

    private getHeaderMd5 = (object:any):any => {
        let headerObj = {};
        // This is ugly workaround!!!
        // The md5 result is not correct if we do not add the line JSON.stringify(resource); twice.
        JSON.stringify(object);
        let componentString:string = JSON.stringify(object);
        let md5Result = md5(componentString).toLowerCase();
        headerObj = {'Content-MD5': this.$base64.encode(md5Result)};
        return headerObj;
    };

}
