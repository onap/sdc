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
/// <reference path="../../../../../references"/>
module Sdc.ViewModels {
    'use strict';
    import Dictionary = Sdc.Utils.Dictionary;
    import InputModel = Sdc.Models.InputModel;

    export interface IInputsViewModelScope extends IWorkspaceViewModelScope {
        InstanceInputsProperties:Models.InstanceInputsPropertiesMapData; //this is tha map object that hold the selected inputs and the inputs we already used
        vfInstancesList: Array<Models.ComponentsInstances.ComponentInstance>;
        component:Models.Components.Resource;

        onArrowPressed():void;
        getInputPropertiesForInstance(instanceId:string, instance:Models.ComponentsInstances.ComponentInstance): ng.IPromise<boolean> ;
        loadInputPropertiesForInstance(instanceId:string, input:Models.InputModel): ng.IPromise<boolean> ;
        loadInputInputs(input:Models.InputModel): ng.IPromise<boolean>;
    }

    export class ResourceInputsViewModel {

        static '$inject' = [
            '$scope',
            '$q'
        ];

        constructor(private $scope:IInputsViewModelScope, private $q: ng.IQService) {
            this.initScope();
        }

        private initScope = (): void => {

            this.$scope.InstanceInputsProperties = new Models.InstanceInputsPropertiesMapData();
            this.$scope.vfInstancesList = this.$scope.component.componentInstances;

            // Need to cast all inputs to InputModel for the search to work
            let tmpInputs:Array<Models.InputModel> = new Array<Models.InputModel>();
            _.each(this.$scope.component.inputs, (input):void => {
                tmpInputs.push(new Models.InputModel(input));
            });
            this.$scope.component.inputs = tmpInputs;
            // This function is not supported for resource
            //this.$scope.component.getComponentInputs();

            /*
             * When clicking on instance input in the left or right table, this function will load all properties of the selected input
             */
            this.$scope.getInputPropertiesForInstance = (instanceId:string, instance:Models.ComponentsInstances.ComponentInstance): ng.IPromise<boolean> => {
                let deferred = this.$q.defer();
                instance.properties = this.$scope.component.componentInstancesProperties[instanceId];
                deferred.resolve(true);
                return deferred.promise;
            };

            /*
             * When clicking on input in the right table, this function will load all inputs of the selected input
             */
            this.$scope.loadInputInputs = (input:Models.InputModel): ng.IPromise<boolean> => {
                let deferred = this.$q.defer();

                let onSuccess = () => { deferred.resolve(true); };
                let onError = () => { deferred.resolve(false); };

                if(!input.inputs) {
                    this.$scope.component.getResourceInputInputs(input.uniqueId).then(onSuccess, onError);
                } else {
                    deferred.resolve(true);
                }
                return deferred.promise;
            };

            /*
             * When clicking on instance input in the left or right table, this function will load all properties of the selected input
             */
            this.$scope.loadInputPropertiesForInstance = (instanceId:string, input:Models.InputModel): ng.IPromise<boolean> => {
                let deferred = this.$q.defer();

                let onSuccess = (properties:Array<Models.PropertyModel>) => {
                    input.properties = properties;
                    deferred.resolve(true);
                };

                let onError = () => {
                    deferred.resolve(false)
                };

                if(!input.properties) {
                    this.$scope.component.getComponentInstanceInputProperties(instanceId, input.uniqueId).then(onSuccess, onError);
                } else {
                    deferred.resolve(true);
                }
                return deferred.promise;
            };

            /*
             * When pressing the arrow, we create service inputs from the inputs selected
             */
            this.$scope.onArrowPressed = ():void => {
                let onSuccess = (inputsCreated: Array<Models.InputModel>) => {

                    //disabled  all the inputs in the left table
                    _.forEach(this.$scope.InstanceInputsProperties, (properties:Array<Models.PropertyModel>) => {
                        _.forEach(properties, (property:Models.PropertyModel) => {
                            property.isAlreadySelected = true;
                        });
                    });

                    // Adding color to the new inputs (right table)
                    _.forEach(inputsCreated, (input) => {
                        input.isNew = true;
                    });

                    // Removing color to the new inputs (right table)
                    setTimeout(() => {
                        _.forEach(inputsCreated, (input) => {
                            input.isNew = false;
                        });
                        this.$scope.$apply();
                    }, 3000);
                };

                this.$scope.component.createInputsFormInstances(this.$scope.InstanceInputsProperties).then(onSuccess);
            };

        }

    }
}
