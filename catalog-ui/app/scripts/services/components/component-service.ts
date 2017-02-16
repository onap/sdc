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
/// <reference path="../../references"/>
module Sdc.Services.Components {

    'use strict';

    declare let CryptoJS:any;

    export interface IComponentService {

        getComponent(id:string);
        updateComponent(component:Models.Components.Component):ng.IPromise<Models.Components.Component>;
        changeLifecycleState(component:Models.Components.Component, state:string, userRemarks:any):ng.IPromise<Models.Components.Component> ;
        validateName(newName:string, subtype?:string):ng.IPromise<Models.IValidate>;
        createComponent(component:Models.Components.Component):ng.IPromise<Models.Components.Component>;
        addOrUpdateArtifact(componentId:string, artifact:Models.ArtifactModel):ng.IPromise<Models.ArtifactModel>;
        deleteArtifact(componentId:string, artifact:string, artifactLabel):ng.IPromise<Models.ArtifactModel>;
        addProperty(componentId:string, property:Models.PropertyModel):ng.IPromise<Models.PropertyModel>;
        updateProperty(componentId:string, property:Models.PropertyModel):ng.IPromise<Models.PropertyModel>;
        addAttribute(componentId:string, attribute:Models.AttributeModel):ng.IPromise<Models.AttributeModel>;
        updateAttribute(componentId:string, attribute:Models.AttributeModel):ng.IPromise<Models.AttributeModel>;
        deleteProperty(componentId:string, propertyId:string):ng.IPromise<Models.PropertyModel>;
        deleteAttribute(componentId:string, attributeId:string):ng.IPromise<Models.AttributeModel>;
        changeResourceInstanceVersion(componentId:string, componentInstanceId:string, componentUid:string):ng.IPromise<Models.ComponentsInstances.ComponentInstance>;
        updateInstanceArtifact(componentId:string, instanceId:string, artifact:Models.ArtifactModel):ng.IPromise<Models.ArtifactModel>;
        addInstanceArtifact(componentId: string, instanceId: string, artifact:Models.ArtifactModel):ng.IPromise<Models.ArtifactModel>;
        deleteInstanceArtifact(componentId: string, instanceId: string, artifact:string, artifactLabel):ng.IPromise<Models.ArtifactModel>;
        createComponentInstance(componentId:string, componentInstance:Models.ComponentsInstances.ComponentInstance):ng.IPromise<Models.ComponentsInstances.ComponentInstance>;
        updateComponentInstance(componentId:string, componentInstance:Models.ComponentsInstances.ComponentInstance):ng.IPromise<Models.ComponentsInstances.ComponentInstance>;
        updateMultipleComponentInstances(componentId:string, instances:Array<Models.ComponentsInstances.ComponentInstance>):ng.IPromise< Array<Models.ComponentsInstances.ComponentInstance>>;
        downloadArtifact(componentId:string, artifactId:string):ng.IPromise<Models.IFileDownload>;
        uploadInstanceEnvFile(componentId:string, instanceId:string, artifact:Models.ArtifactModel):ng.IPromise<Models.ArtifactModel>;
        downloadInstanceArtifact(componentId:string, instanceId:string, artifactId:string):ng.IPromise<Models.IFileDownload>;
        deleteComponentInstance(componentId:string, componentInstanceId:string):ng.IPromise<Models.ComponentsInstances.ComponentInstance>;
        createRelation(componentId:string, link:Models.RelationshipModel):ng.IPromise<Models.RelationshipModel>;
        deleteRelation(componentId:string, link:Models.RelationshipModel):ng.IPromise<Models.RelationshipModel>;
        getRequirementsCapabilities(componentId:string):ng.IPromise<any>;
        updateInstanceProperty(componentId:string, property:Models.PropertyModel):ng.IPromise<Models.PropertyModel>;
        updateInstanceAttribute(componentId:string, attribute:Models.AttributeModel):ng.IPromise<Models.AttributeModel>;
        getComponentInstancesFilteredByInputsAndProperties(componentId:string, searchText:string):ng.IPromise<Array<Models.ComponentsInstances.ComponentInstance>>
        getComponentInstanceInputs(componentId:string, instanceId:string, originComponentUid):ng.IPromise<Array<Models.InputModel>>;
        getComponentInputs(componentId:string):ng.IPromise<Array<Models.InputModel>>;
        getComponentInstanceInputProperties(componentId:string, instanceId:string, inputId:string):ng.IPromise<Array<Models.PropertyModel>>;
        getModuleForDisplay(componentId:string, moduleId:string):ng.IPromise<Models.DisplayModule>;
        updateGroupMetadata(componentId:string, group:Models.Module):ng.IPromise<Models.Module>;
        getComponentInputInputs(serviceId:string, input:string): ng.IPromise<Array<Models.InputModel>>;
        createInputsFromInstancesInputs(serviceId:string, instancesInputsMap:Models.InstancesInputsMap): ng.IPromise<Array<Models.InputModel>>;
        createInputsFromInstancesInputsProperties(resourceId:string, instanceInputsPropertiesMap:Models.InstanceInputsPropertiesMap): ng.IPromise<Array<Models.PropertyModel>>;
        deleteComponentInput(serviceId:string, inputId:string):ng.IPromise<Models.InputModel>;
    }

    export class ComponentService implements IComponentService {

        static '$inject' = [
            '$log',
            'Restangular',
            'sdcConfig',
            'Sdc.Services.SharingService',
            '$q',
            '$interval',
            '$base64',
            'ComponentInstanceFactory'
        ];

        constructor(protected $log: ng.ILogService,
                    protected restangular:restangular.IElement,
                    protected sdcConfig:Models.IAppConfigurtaion,
                    protected sharingService:Sdc.Services.SharingService,
                    protected $q:ng.IQService,
                    protected $interval:any,
                    protected $base64:any,
                    protected ComponentInstanceFactory:Utils.ComponentInstanceFactory) {

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
        protected createComponentObject = (component:Models.Components.Component):Models.Components.Component => {
            return component;
        };

        public getComponent = (id:string):ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();
            this.restangular.one(id).get().then((response:Models.Components.Component) => {
                let component:Models.Components.Component = this.createComponentObject(response);
                //this.$log.debug("Component Loaded successfully : ", component);
                deferred.resolve(component);
            }, (err)=> {
                this.$log.debug("Failed to load component with ID: " + id);
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateComponent = (component:Models.Components.Component):ng.IPromise<Models.Components.Component> => {
            // If this is resource
            if (component instanceof Sdc.Models.Components.Resource) {
                let resource:Sdc.Models.Components.Resource = <Sdc.Models.Components.Resource>component;
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

        private updateService = (component:Models.Components.Component):ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();
            this.restangular.one(component.uniqueId).one("metadata").customPUT(JSON.stringify(component)).then((response:Models.Components.Component) => {
                let component:Models.Components.Component = this.createComponentObject(response);
                deferred.resolve(component);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        private updateResource = (component:Models.Components.Component):ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();
            this.restangular.one(component.uniqueId).customPUT(JSON.stringify(component)).then((response:Models.Components.Component) => {
                let component:Models.Components.Component = this.createComponentObject(response);
                deferred.resolve(component);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        private updateResourceMetadata = (component:Models.Components.Component):ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();
            this.restangular.one(component.uniqueId).one('metadata').customPUT(JSON.stringify(component)).then((response:Models.Components.Component) => {
                let component:Models.Components.Component = this.createComponentObject(response);
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
        private updateResourceWithPayload = (resource:Sdc.Models.Components.Resource):ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();

            resource.payloadData = resource.importedFile.base64;
            resource.payloadName = resource.importedFile.filename;
            let headerObj = this.getHeaderMd5(resource);

            this.restangular.one(resource.uniqueId).customPUT(JSON.stringify(resource), '', {}, headerObj).then((response:Models.Components.Component) => {
                let componentResult:Models.Components.Component = this.createComponentObject(response);
                deferred.resolve(componentResult);
            }, (err)=> {
                deferred.reject(err);
            });

            return deferred.promise;
        };

        public createComponent = (component:Models.Components.Component):ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();
            let headerObj = this.getHeaderMd5(component);
            this.restangular.customPOST(JSON.stringify(component), '', {}, headerObj).then((response:Models.Components.Component) => {
                let component:Models.Components.Component = this.createComponentObject(response);
                deferred.resolve(component);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public validateName = (newName:string, subtype?:string):ng.IPromise<Models.IValidate> => {
            let deferred = this.$q.defer();
            this.restangular.one("validate-name").one(newName).get({'subtype': subtype}).then((response:any) => {
                deferred.resolve(response.plain());
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public changeLifecycleState = (component:Models.Components.Component, state:string, userRemarks:any):ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();
            this.restangular.one(component.uniqueId).one(state).customPOST(userRemarks).then((response:Models.Components.Component) => {
                this.sharingService.addUuidValue(response.uniqueId, response.uuid);
                let component:Models.Components.Component = this.createComponentObject(response);
                deferred.resolve(component);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        // ------------------------------------------------ Artifacts API --------------------------------------------------//
        public addOrUpdateArtifact = (componentId:string, artifact:Models.ArtifactModel):ng.IPromise<Models.ArtifactModel> => {
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

        public downloadArtifact = (componentId:string, artifactId:string):ng.IPromise<Models.IFileDownload> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("artifacts").one(artifactId).get().then((response:any) => {
                deferred.resolve(response.plain());
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public deleteArtifact = (componentId:string, artifactId:string, artifactLabel:string):ng.IPromise<Models.ArtifactModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("artifacts").one(artifactId).remove({'operation': artifactLabel}).then((response:Models.ArtifactModel) => {
                deferred.resolve(response);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };


        // ------------------------------------------------ Properties API --------------------------------------------------//
        public addProperty = (componentId:string, property:Models.PropertyModel):ng.IPromise<Models.PropertyModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("properties").customPOST(property.convertToServerObject()).then((response:any) => {
                let property:Models.PropertyModel = new Models.PropertyModel(response[Object.keys(response)[0]]);
                deferred.resolve(property);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateProperty = (componentId:string, property:Models.PropertyModel):ng.IPromise<Models.PropertyModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("properties").one(property.uniqueId).customPUT(property.convertToServerObject()).then((response:any) => {
                let property:Models.PropertyModel = new Models.PropertyModel(response[Object.keys(response)[0]]);
                deferred.resolve(property);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public deleteProperty = (componentId:string, propertyId:string):ng.IPromise<Models.PropertyModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("properties").one(propertyId).remove().then((response:any) => {
                deferred.resolve(response);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        // ------------------------------------------------ Attributes API --------------------------------------------------//
        public addAttribute = (componentId:string, attribute:Models.AttributeModel):ng.IPromise<Models.AttributeModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("attributes").customPOST(attribute.convertToServerObject()).then((response:any) => {
                let attribute:Models.AttributeModel = new Models.AttributeModel(response);
                deferred.resolve(attribute);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateAttribute = (componentId:string, attribute:Models.AttributeModel):ng.IPromise<Models.AttributeModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("attributes").one(attribute.uniqueId).customPUT(attribute.convertToServerObject()).then((response:any) => {
                let attribute:Models.AttributeModel = new Models.AttributeModel(response);
                deferred.resolve(attribute);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public deleteAttribute = (componentId:string, attributeId:string):ng.IPromise<Models.AttributeModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("attributes").one(attributeId).remove().then((response:any) => {
                deferred.resolve(response);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        // ------------------------------------------------ Component Instances API --------------------------------------------------//

        public createComponentInstance = (componentId:string, componentInstance:Models.ComponentsInstances.ComponentInstance):ng.IPromise<Models.ComponentsInstances.ComponentInstance> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance").customPOST(JSON.stringify(componentInstance)).then((response:any) => {
                let componentInstance:Models.ComponentsInstances.ComponentInstance = Utils.ComponentInstanceFactory.createComponentInstance(response);
                this.$log.debug("Component Instance created", componentInstance);
                deferred.resolve(componentInstance);
            }, (err)=> {
                this.$log.debug("Failed to create componentInstance. With Name: " + componentInstance.name);
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateComponentInstance = (componentId:string, componentInstance:Models.ComponentsInstances.ComponentInstance):ng.IPromise<Models.ComponentsInstances.ComponentInstance> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance").one(componentInstance.uniqueId).customPOST(JSON.stringify(componentInstance)).then((response:any) => {
                let componentInstance:Models.ComponentsInstances.ComponentInstance = Utils.ComponentInstanceFactory.createComponentInstance(response);
                this.$log.debug("Component Instance was updated", componentInstance);
                deferred.resolve(componentInstance);
            }, (err)=> {
                this.$log.debug("Failed to update componentInstance. With ID: " + componentInstance.uniqueId + "Name: " + componentInstance.name);
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateMultipleComponentInstances = (componentId:string, instances:Array<Models.ComponentsInstances.ComponentInstance>):ng.IPromise<Array<Models.ComponentsInstances.ComponentInstance>> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance/multipleComponentInstance").customPOST(JSON.stringify(instances)).then((response:any) => {
                this.$log.debug("Multiple Component Instances was updated", response);
                let updateInstances:Array<Models.ComponentsInstances.ComponentInstance> = new Array<Models.ComponentsInstances.ComponentInstance>();
                _.forEach(response, (componentInstance:Models.ComponentsInstances.ComponentInstance) => {
                    let updatedComponentInstance:Models.ComponentsInstances.ComponentInstance = Utils.ComponentInstanceFactory.createComponentInstance(componentInstance);
                    updateInstances.push(updatedComponentInstance);
                });
                deferred.resolve(updateInstances);
            }, (err)=> {
                this.$log.debug("Failed to update Multiple componentInstance.");
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public deleteComponentInstance = (componentId:string, componentInstanceId:string):ng.IPromise<Models.ComponentsInstances.ComponentInstance> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance").one(componentInstanceId).remove().then(() => {
                this.$log.debug("Component Instance was deleted");
                deferred.resolve();
            }, (err)=> {
                this.$log.debug("Failed to delete componentInstance. With ID: " + componentInstanceId);
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public changeResourceInstanceVersion = (componentId:string, componentInstanceId:string, componentUid:string):ng.IPromise<Models.ComponentsInstances.ComponentInstance> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance").one(componentInstanceId).one("changeVersion").customPOST({'componentUid': componentUid}).then((response:any) => {
                let componentInstance:Models.ComponentsInstances.ComponentInstance = Utils.ComponentInstanceFactory.createComponentInstance(response);
                deferred.resolve(componentInstance);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public downloadInstanceArtifact = (componentId:string, instanceId:string, artifactId:string):ng.IPromise<Models.IFileDownload> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstances").one(instanceId).one("artifacts").one(artifactId).get().then((response:any) => {
                deferred.resolve(response.plain());
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateInstanceArtifact = (componentId:string, instanceId:string, artifact:Models.ArtifactModel):ng.IPromise<Models.ArtifactModel> => {
            let deferred = this.$q.defer();
            let headerObj = {};
            if(artifact.payloadData){
                headerObj = this.getHeaderMd5(artifact);
            }
            this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").customPOST(JSON.stringify(artifact), artifact.uniqueId , {}, headerObj).then((response: any) => {
                let newArtifact = new Models.ArtifactModel(response);
                deferred.resolve(newArtifact);
            }, (err)=>{
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public addInstanceArtifact = (componentId: string, instanceId: string, artifact:Models.ArtifactModel): ng.IPromise<Models.ArtifactModel> => {
            let deferred = this.$q.defer();
            let headerObj = {};
            if(artifact.payloadData){
                headerObj = this.getHeaderMd5(artifact);
            }
            this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").customPOST(JSON.stringify(artifact), artifact.uniqueId , {}, headerObj).then((response: any) => {
                let artifact:Models.ArtifactModel = new Models.ArtifactModel(response.plain());
                deferred.resolve(artifact);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public deleteInstanceArtifact = (componentId: string , instanceId: string, artifactId:string, artifactLabel: string): ng.IPromise<Models.ArtifactModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").one(artifactId).remove({'operation': artifactLabel}).then((response: Models.ArtifactModel) => {
                deferred.resolve(response);
            }, (err)=>{
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public uploadInstanceEnvFile = (componentId:string, instanceId:string, artifact:Models.ArtifactModel):ng.IPromise<Models.ArtifactModel> => {
            let deferred = this.$q.defer();
            let headerObj = {};
            if (artifact.payloadData) {
                headerObj = this.getHeaderMd5(artifact);
            }
            this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("artifacts").customPOST(JSON.stringify(artifact), artifact.uniqueId, {}, headerObj).then((response:any) => {
                let newArtifact = new Models.ArtifactModel(response);
                deferred.resolve(newArtifact);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateInstanceProperty = (componentId:string, property:Models.PropertyModel):ng.IPromise<Models.PropertyModel> => {
            let deferred = this.$q.defer();
            let instanceId = property.resourceInstanceUniqueId;
            this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("property").customPOST(JSON.stringify(property)).then((response:any) => {
                let newProperty = new Models.PropertyModel(response);
                newProperty.readonly = true;
                newProperty.resourceInstanceUniqueId = instanceId;
                deferred.resolve(newProperty);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public updateInstanceAttribute = (componentId:string, attribute:Models.AttributeModel):ng.IPromise<Models.AttributeModel> => {
            let deferred = this.$q.defer();
            let instanceId = attribute.resourceInstanceUniqueId;
            this.restangular.one(componentId).one("resourceInstance").one(instanceId).one("attribute").customPOST(JSON.stringify(attribute)).then((response:any) => {
                let newAttribute = new Models.AttributeModel(response);
                newAttribute.readonly = true;
                newAttribute.resourceInstanceUniqueId = instanceId;
                deferred.resolve(newAttribute);
            }, (err)=> {
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public createRelation = (componentId:string, link:Models.RelationshipModel):ng.IPromise<Models.RelationshipModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance").one("associate").customPOST(JSON.stringify(link)).then((response:any) => {
                let relation:Models.RelationshipModel = new Models.RelationshipModel(response.plain());
                this.$log.debug("Link created successfully ", relation);
                deferred.resolve(relation);
            }, (err)=> {
                this.$log.debug("Failed to create Link From: " + link.fromNode + "To: " + link.toNode);
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public deleteRelation = (componentId:string, link:Models.RelationshipModel):ng.IPromise<Models.RelationshipModel> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("resourceInstance").one("dissociate").customPUT(JSON.stringify(link)).then((response:any) => {
                let relation:Models.RelationshipModel = new Models.RelationshipModel(response);
                this.$log.debug("Link deleted successfully ", relation);
                deferred.resolve(relation);
            }, (err)=> {
                this.$log.debug("Failed to delete Link From: " + link.fromNode + "To: " + link.toNode);
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public getRequirementsCapabilities = (componentId:string):ng.IPromise<any> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("requirmentsCapabilities").get().then((response:any) => {
                this.$log.debug("Component requirement capabilities recived: ", response);
                deferred.resolve(response);
            }, (err)=> {
                this.$log.debug("Failed to get requirements & capabilities");
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public getModuleForDisplay = (componentId:string, moduleId:string):ng.IPromise<Models.DisplayModule> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("groups").one(moduleId).get().then((response:any) => {
                this.$log.debug("module loaded successfully: ", response);
                let module:Models.DisplayModule = new Models.DisplayModule(response);
                deferred.resolve(module);
            }, (err)=> {
                this.$log.debug("Failed to get module with id: ", moduleId);
                deferred.reject(err);
            });
            return deferred.promise;
        };

        public getComponentInstancesFilteredByInputsAndProperties = (componentId:string, searchText?:string):ng.IPromise<Array<Models.ComponentsInstances.ComponentInstance>> => {
            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("componentInstances").get({'searchText': searchText}).then((response:any) => {
                this.$log.debug("component instances return successfully: ", response);
                let componentInstances:Array<Models.ComponentsInstances.ComponentInstance> = Utils.CommonUtils.initComponentInstances(response);
                deferred.resolve(componentInstances);
            }, (err) => {
                this.$log.debug("Failed to get component instances of component with id: " + componentId);
                deferred.reject(err);
            });

            return deferred.promise;
        };

        public getComponentInstanceInputs = (componentId:string, instanceId:string, originComponentUid):ng.IPromise<Array<Models.InputModel>> => {

            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("componentInstances").one(instanceId).one(originComponentUid).one("inputs").get().then((response:any) => {
                this.$log.debug("component instance input return successfully: ", response);
                let inputsArray:Array<Models.InputModel> = new Array<Models.InputModel>();
                _.forEach(response, (inputObj:Models.InputModel) => {
                    inputsArray.push(new Models.InputModel(inputObj));
                });
                deferred.resolve(inputsArray);
            }, (err) => {
                this.$log.debug("Failed to get component instance input with id: " + instanceId);
                deferred.reject(err);
            });

            return deferred.promise;
        };

        public getComponentInputs = (componentId:string):ng.IPromise<Array<Models.InputModel>> => {

            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("inputs").get().then((response:any) => {
                this.$log.debug("component inputs return successfully: ", response);
                let inputsArray:Array<Models.InputModel> = new Array<Models.InputModel>();
                _.forEach(response, (inputObj:Models.InputModel) => {
                    inputsArray.push(new Models.InputModel(inputObj));
                });
                deferred.resolve(inputsArray);
            }, (err) => {
                this.$log.debug("Failed to get component inputs for component with id: " + componentId);
                deferred.reject(err);
            });

            return deferred.promise;
        };

        public getComponentInstanceInputProperties = (componentId:string, instanceId:string, inputId:string):ng.IPromise<Array<Models.PropertyModel>> => {

            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("componentInstances").one(instanceId).one(inputId).one("properties").get().then((response:any) => {
                this.$log.debug("component instance input properties return successfully: ", response);
                let propertiesArray:Array<Models.PropertyModel> = new Array<Models.PropertyModel>();
                _.forEach(response, (propertyObj:Models.PropertyModel) => {
                    propertiesArray.push(new Models.PropertyModel(propertyObj));
                });
                deferred.resolve(propertiesArray);
            }, (err) => {
                this.$log.debug("Failed to get component instance input properties with instanceId: " + instanceId + "and input id: " + inputId);
                deferred.reject(err);
            });

            return deferred.promise;
        };

        public updateGroupMetadata = (componentId:string, group:Models.Module):ng.IPromise<Models.Module> => {

            let deferred = this.$q.defer();
            this.restangular.one(componentId).one("groups").one(group.uniqueId).one("metadata").customPUT(JSON.stringify(group)).then((response:Models.Module) => {
                this.$log.debug("group metadata updated successfully: ", response);
                let updatedGroup:Models.Module = new Models.Module(response);

                deferred.resolve(updatedGroup);
            }, (err) => {
                this.$log.debug("Failed to update group metadata for component: " + componentId + " for group with id: " + group.uniqueId);
                deferred.reject(err);
            });

            return deferred.promise;
        };

        public getComponentInputInputs = (serviceId:string, inputId:string): ng.IPromise<Array<Models.InputModel>>  => {
            let defer = this.$q.defer<any>();
            this.restangular.one(serviceId).one("inputs").one(inputId).one("inputs").get().then((response: any) => {
                let inputsArray:Array<Models.InputModel> = new Array<Models.InputModel>();
                _.forEach(response, (inputObj:Models.InputModel) => {
                    inputsArray.push(new Models.InputModel(inputObj));
                });
                defer.resolve(inputsArray);
            }, (err)=>{
                this.$log.debug("failed to get inputs of input : ", err);
                defer.reject(err);
            });
            return defer.promise;
        };

        createInputsFromInstancesInputsProperties = (resourceId:string, instancePropertyMap:Models.InstanceInputsPropertiesMap): ng.IPromise<Array<Models.PropertyModel>>  => {
            let defer = this.$q.defer<any>();
            this.restangular.one(resourceId).one("create/properties").customPOST(instancePropertyMap).then((response: any) => {
                let inputsArray:Array<Models.PropertyModel> = new Array<Models.PropertyModel>();
                _.forEach(response, (inputObj:Models.PropertyModel) => {
                    inputsArray.push(new Models.PropertyModel(inputObj));
                });
                defer.resolve(inputsArray);
            }, (err)=>{
                this.$log.debug("failed to create service inputs from VF instances inputs : ", err);
                defer.reject(err);
            });
            return defer.promise;
        };

        createInputsFromInstancesInputs = (serviceId:string, instancesMap:Models.InstancesInputsMap): ng.IPromise<Array<Models.InputModel>>  => {
            let defer = this.$q.defer<any>();
            this.restangular.one(serviceId).one("create/inputs").customPOST(instancesMap).then((response: any) => {
                let inputsArray:Array<Models.InputModel> = new Array<Models.InputModel>();
                _.forEach(response, (inputObj:Models.InputModel) => {
                    inputsArray.push(new Models.InputModel(inputObj));
                });
                defer.resolve(inputsArray);
            }, (err)=>{
                this.$log.debug("failed to create service inputs from VF instances inputs : ", err);
                defer.reject(err);
            });
            return defer.promise;
        };

        deleteComponentInput = (serviceId:string, inputId:string) : ng.IPromise<Models.InputModel> => {
            var defer = this.$q.defer();
            this.restangular.one(serviceId).one("delete").one(inputId).one("input").remove().then((response: any) => {
                var inputToDelete = new Models.InputModel(response);

                defer.resolve(inputToDelete);
            }, (err)=> {
                console.log("failed to delete input from service: ", err);
                defer.reject(err);
            });
            return defer.promise;
        };

        private getHeaderMd5 = (object:any):any => {
            let headerObj={};
            // This is ugly workaround!!!
            // The md5 result is not correct if we do not add the line JSON.stringify(resource); twice.
            JSON.stringify(object);
            let componentString:string = JSON.stringify(object);
            let md5Result = md5(componentString).toLowerCase();
            headerObj = {'Content-MD5': this.$base64.encode(md5Result)};
            return headerObj;
        };

    }
}
