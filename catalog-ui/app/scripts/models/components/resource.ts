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
 * Created by obarda on 2/3/2016.
 */
/// <reference path="../../references"/>
module Sdc.Models.Components {
    'use strict';

    export class Resource extends Component {

        public interfaces: any;
        public derivedFrom:Array<string>;
        public componentService: Services.Components.IResourceService;
        public resourceType:string;
        public payloadData:string;
        public payloadName:string;
        public importedFile: Sdc.Directives.FileUploadModel;

        // Onboarding parameters
        public csarUUID:string;
        public csarVersion:string;
        public csarPackageType:string;
        public packageId:string;

        constructor(componentService: Services.Components.IResourceService, $q: ng.IQService, component?:Resource) {
            super(componentService, $q, component);
            if(component) {

                this.interfaces = component.interfaces;
                this.derivedFrom = component.derivedFrom;
                this.payloadData = component.payloadData ? component.payloadData : undefined;
                this.payloadName = component.payloadName ? component.payloadName : undefined;
                this.resourceType = component.resourceType;
                this.csarUUID = component.csarUUID;
                this.csarVersion = component.csarVersion;
                this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version + ' ' + this.resourceType;

                if (component.categories && component.categories[0] && component.categories[0].subcategories && component.categories[0].subcategories[0]) {
                    component.mainCategory = component.categories[0].name;
                    component.subCategory = component.categories[0].subcategories[0].name;
                    this.selectedCategory =  component.mainCategory +  "_#_" + component.subCategory;
                    this.importedFile = component.importedFile;
                }
            } else {
                this.resourceType = Utils.Constants.ResourceType.VF;
            }

            this.componentService = componentService;
            this.iconSprite = "sprite-resource-icons";
        }

        public getComponentSubType = ():string => {
            return this.resourceType;
        };

        public isComplex = ():boolean => {
            return this.resourceType === Utils.Constants.ResourceType.VF;
        };

        public isVl = ():boolean => {
            return Utils.Constants.ResourceType.VL == this.resourceType;
        };

        public isCsarComponent = ():boolean => {
          return !!this.csarUUID;
        };

        public createComponentOnServer = ():ng.IPromise<Models.Components.Component> => {
            let deferred = this.$q.defer();
            let onSuccess = (component:Models.Components.Resource):void  => {
                this.payloadData = undefined;
                this.payloadName = undefined;
                deferred.resolve(component);
            };
            let onError = (error:any):void => {
                deferred.reject(error);
            };

            this.handleTags();
            if(this.importedFile){
                this.payloadData = this.importedFile.base64;
                this.payloadName = this.importedFile.filename;
            }
            this.componentService.createComponent(this).then(onSuccess, onError);
            return deferred.promise;
        };

        /* we need to change the name of the input to vfInstanceName + input name before sending to server in order to create the inputs on the service
         *  we also need to remove already selected inputs (the inputs that already create on server, and disabled in the view - but they are selected so they are still in the view model
         */
        public createInputsFormInstances = (instanceInputsPropertiesMap:Models.InstanceInputsPropertiesMapData):ng.IPromise<Array<Models.InputModel>> => {
            let deferred = this.$q.defer();
            /*
            let instanceInputsPropertiesMapToCreate: Models.InstanceInputsPropertiesMapData = new Models.InstanceInputsPropertiesMapData();
            _.forEach(instanceInputsPropertiesMap, (properties:Array<Models.PropertyModel>, instanceId:string) => {

                if(properties && properties.length > 0) {
                    let componentInstance:Models.ComponentsInstances.ComponentInstance = _.find(this.componentInstances, (instace:Models.ComponentsInstances.ComponentInstance) => {
                        return instace.uniqueId === instanceId;
                    });

                    instanceInputsPropertiesMapToCreate[instanceId] = new Array<Models.PropertyModel>();
                    _.forEach(properties, (property:Models.PropertyModel) => {

                        if(!property.isAlreadySelected) {
                            let newInput = new Models.PropertyModel(property);
                            newInput.name = componentInstance.normalizedName + '_' + property.name;
                            instanceInputsPropertiesMapToCreate[instanceId].push(newInput);
                        }
                    });
                    if( instanceInputsPropertiesMapToCreate[instanceId].length === 0) {
                        delete instanceInputsPropertiesMapToCreate[instanceId];
                    }
                } else {
                    delete instanceInputsPropertiesMapToCreate[instanceId];
                }
            });

            if(Object.keys(instanceInputsPropertiesMapToCreate).length > 0) {
                let deferred = this.$q.defer();
                let onSuccess = (propertiesCreated: Array<Models.PropertyModel>):void => {
                    this.inputs = propertiesCreated.concat(this.inputs);
                    deferred.resolve(propertiesCreated);
                };
                let onFailed = (error:any): void => {
                    deferred.reject(error);
                };
                this.componentService.createInputsFromInstancesInputsProperties(this.uniqueId, new Models.InstanceInputsPropertiesMap(instanceInputsPropertiesMapToCreate)).then(onSuccess, onFailed);
            }
             */
            return deferred.promise;
        };

        // we need to change the name of the input to vfInstanceName + input name before sending to server in order to create the inputs on the service
        public getResourceInputInputs = (inputId:string):ng.IPromise<Array<Models.InputModel>> => {
            let deferred = this.$q.defer();
            let onSuccess = (inputInputs: Array<Models.InputModel>):void => {
                let input: Models.InputModel = _.find(this.inputs, (input:Models.InputModel) => {
                    return input.uniqueId === inputId;
                });
                input.inputs = inputInputs;
                deferred.resolve(inputInputs);
            };
            let onFailed = (error:any): void => {
                deferred.reject(error);
            };
            this.componentService.getComponentInputInputs(this.uniqueId, inputId).then(onSuccess, onFailed);
            return deferred.promise;
        };

        public toJSON = ():any => {
            this.componentService = undefined;
            this.filterTerm = undefined;
            this.iconSprite = undefined;
            this.mainCategory = undefined;
            this.subCategory = undefined;
            this.selectedInstance = undefined;
            this.showMenu = undefined;
            this.$q = undefined;
            this.selectedCategory = undefined;
            this.importedFile = undefined;
            return this;
        };
    }
}


