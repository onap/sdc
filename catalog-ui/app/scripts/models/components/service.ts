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
module Sdc.Models.Components {
    'use strict';


    export class Service extends Component {

        public serviceApiArtifacts:Models.ArtifactGroupModel;
        public componentService:Services.Components.IServiceService;

        constructor(componentService:Services.Components.IServiceService, $q:ng.IQService, component?:Service) {
            super(componentService, $q, component);
            if (component) {
                this.serviceApiArtifacts = new Models.ArtifactGroupModel(component.serviceApiArtifacts);
                this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
                if (component.categories && component.categories[0]) {
                    this.mainCategory = component.categories[0].name;
                    this.selectedCategory = this.mainCategory;
                }
            }
            this.componentService = componentService;
            this.iconSprite = "sprite-services-icons";
        }

        public getDistributionsList = ():ng.IPromise<Array<Models.Distribution>> => {
            return this.componentService.getDistributionsList(this.uuid);
        };

        public getDistributionsComponent = (distributionId:string):ng.IPromise<Array<Models.DistributionComponent>> => {
            return this.componentService.getDistributionComponents(distributionId);
        };

        public markAsDeployed = (distributionId:string):ng.IPromise<any> => {
            return this.componentService.markAsDeployed(this.uniqueId, distributionId);
        };

        /* we need to change the name of the input to vfInstanceName + input name before sending to server in order to create the inputs on the service
        *  we also need to remove already selected inputs (the inputs that already create on server, and disabled in the view - but they are selected so they are still in the view model
        */
        public createInputsFormInstances = (instancesInputsMap:Models.InstancesInputsMapData):ng.IPromise<Array<Models.InputModel>> => {
            let deferred = this.$q.defer();

             let instancesInputsMapToCreate: Models.InstancesInputsMapData = new Models.InstancesInputsMapData();
            _.forEach(instancesInputsMap, (inputs:Array<Models.InputModel>, instanceId:string) => {

                if(inputs && inputs.length > 0) {
                    let componentInstance:Models.ComponentsInstances.ComponentInstance = _.find(this.componentInstances, (instace:Models.ComponentsInstances.ComponentInstance) => {
                       return instace.uniqueId === instanceId;
                    });
                    instancesInputsMapToCreate[instanceId] = new Array<Models.InputModel>();
                    _.forEach(inputs, (input:Models.InputModel) => {

                        if(!input.isAlreadySelected) {
                            let newInput = new Models.InputModel(input);
                            newInput.name = componentInstance.normalizedName + '_' + input.name;
                            instancesInputsMapToCreate[instanceId].push(newInput);
                        }
                    });
                    if( instancesInputsMapToCreate[instanceId].length === 0) {
                        delete instancesInputsMapToCreate[instanceId];
                    }
                } else {
                    delete instancesInputsMapToCreate[instanceId];
                }
            });

            if(Object.keys(instancesInputsMapToCreate).length > 0) {
                let deferred = this.$q.defer();
                let onSuccess = (inputsCreated: Array<Models.InputModel>):void => {
                    this.inputs = inputsCreated.concat(this.inputs);
                    deferred.resolve(inputsCreated);
                };
                let onFailed = (error:any): void => {
                    deferred.reject(error);
                };
               this.componentService.createInputsFromInstancesInputs(this.uniqueId, new Models.InstancesInputsMap(instancesInputsMapToCreate)).then(onSuccess, onFailed);
            }
            return deferred.promise;
        };

        // we need to change the name of the input to vfInstanceName + input name before sending to server in order to create the inputs on the service
        public getServiceInputInputs = (inputId:string):ng.IPromise<Array<Models.InputModel>> => {
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
        
        public deleteServiceInput = (inputId:string):ng.IPromise<Models.InputModel> => {
            var deferred = this.$q.defer();
            
            var onSuccess = (input: Models.InputModel):void => {
                deferred.resolve(input)
            };
            
            var onFailed = (error:any) : void => {
                deferred.reject(error);
            };
            
            this.componentService.deleteComponentInput(this.uniqueId, inputId).then(onSuccess, onFailed);
            return deferred.promise;
        };

        public getArtifactsByType = (artifactGroupType:string):Models.ArtifactGroupModel => {
            switch (artifactGroupType) {
                case Utils.Constants.ArtifactGroupType.DEPLOYMENT:
                    return this.deploymentArtifacts;
                case Utils.Constants.ArtifactGroupType.INFORMATION:
                    return this.artifacts;
                case Utils.Constants.ArtifactGroupType.SERVICE_API:
                    return this.serviceApiArtifacts;
            }
        };
    }
}

