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
    import IAngularEvent = angular.IAngularEvent;
    import ComponentInstance = Sdc.Models.ComponentsInstances.ComponentInstance;


    interface IServiceInputsViewModelScope extends IWorkspaceViewModelScope {

        vfInstancesList: Array<ComponentInstance>;
        selectedInputs:Array<Models.InputModel>;
        instanceInputsMap:Models.InstancesInputsMapData; //this is tha map object that hold the selected inputs and the inputs we already used
        component:Models.Components.Service;
        sdcMenu:Models.IAppMenu;

        onArrowPressed():void;
        loadComponentInputs(): void;
        loadInstanceInputs(instance:ComponentInstance): ng.IPromise<boolean> ;
        loadInputPropertiesForInstance(instanceId:string, input:Models.InputModel): ng.IPromise<boolean> ;
        loadInputInputs(input:Models.InputModel): ng.IPromise<boolean>;
        deleteInput(input:Models.InputModel):void
    }

    export class ServiceInputsViewModel {

        static '$inject' = [
            '$scope',
            '$q',
            'ModalsHandler'
        ];

        constructor(private $scope:IServiceInputsViewModelScope,
                    private $q: ng.IQService,
                    private ModalsHandler: Sdc.Utils.ModalsHandler) {
            this.initScope();
        }

        /*
         * When loading the screen again, we need to disabled the inputs that already created on the service,
         * we do that by comparing the service input name, to the instance name + '_' + the resource instance input name.
         */
        private disableEnableSelectedInputs = (instance: ComponentInstance): void => {

            let alreadySelectedInput = new Array<Models.InputModel>();
            _.forEach(instance.inputs, (input:Models.InputModel) => {
                let expectedServiceInputName =  instance.normalizedName + '_' + input.name;
                let inputAlreadyInService: Models.InputModel = _.find(this.$scope.component.inputs, (serviceInput: Models.InputModel) => {
                    return serviceInput.name === expectedServiceInputName;
                });
                if(inputAlreadyInService) {
                    input.isAlreadySelected = true;
                    alreadySelectedInput.push(input);
                } else {
                    input.isAlreadySelected = false;
                }
            });
            this.$scope.instanceInputsMap[instance.uniqueId] = alreadySelectedInput;
        };

        private initScope = (): void => {

            this.$scope.instanceInputsMap = new Models.InstancesInputsMapData();
            this.$scope.isLoading = true;
            this.$scope.selectedInputs = new Array<Models.InputModel>();

            // Why do we need this? we call this later.
            //this.$scope.component.getComponentInputs();

            let onSuccess = (componentInstances:Array<ComponentInstance>) => {
                console.log("component instances loaded: ", componentInstances);
                this.$scope.vfInstancesList = componentInstances;
                this.$scope.isLoading = false;
            };

            //This function will get al component instance for the left table - in future the instances will be filter according to search text
            this.$scope.component.getComponentInstancesFilteredByInputsAndProperties().then(onSuccess);

            // This function will get the service inputs for the right table
            this.$scope.component.getComponentInputs();


            /*
             * When clicking on instance in the left table, this function will load all instance inputs
             */
            this.$scope.loadInstanceInputs = (instance:ComponentInstance): ng.IPromise<boolean> => {
                let deferred = this.$q.defer();

                let onSuccess = (inputs:Array<Models.InputModel>) => {
                    instance.inputs = inputs;
                    this.disableEnableSelectedInputs(instance);
                    deferred.resolve(true);
                };

                let onError = () => {
                    deferred.resolve(false);
                };

                if(!instance.inputs) {
                    this.$scope.component.getComponentInstanceInputs(instance.uniqueId, instance.componentUid).then(onSuccess, onError);
                    this.disableEnableSelectedInputs(instance);
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
             * When clicking on input in the right table, this function will load all inputs of the selected input
             */
            this.$scope.loadInputInputs = (input:Models.InputModel): ng.IPromise<boolean> => {
                let deferred = this.$q.defer();

                let onSuccess = () => { deferred.resolve(true); };
                let onError = () => { deferred.resolve(false); };

                if(!input.inputs) { // Caching, if exists do not get it.
                    this.$scope.component.getServiceInputInputs(input.uniqueId).then(onSuccess, onError);
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

                    //disabled all the inputs in the left table
                    _.forEach(this.$scope.instanceInputsMap, (inputs:Array<Models.InputModel>, instanceId:string) => {
                        _.forEach(inputs, (input:Models.InputModel) => {
                            input.isAlreadySelected = true;
                        });
                    });

                    this.addColorToItems(inputsCreated);
                };

               this.$scope.component.createInputsFormInstances(this.$scope.instanceInputsMap).then(onSuccess);
            };

            this.$scope.deleteInput = (input: Models.InputModel):void => {

                var onDelete = ():void => {
                    var onSuccess = (deletedInput: Models.InputModel, componentInstanceId:string):void => {
                        // Remove from component.inputs the deleted input (service inputs)
                        var remainingServiceInputs:Array<Models.InputModel> = _.filter(this.$scope.component.inputs, (input:Models.InputModel):boolean => {
                            return input.uniqueId !== deletedInput.uniqueId;
                        });
                        this.$scope.component.inputs = remainingServiceInputs;

                        // Find the instance that contains the deleted input, and set disable|enable the deleted input
                        var deletedInputComponentInstance:ComponentInstance = _.find(this.$scope.vfInstancesList, (instanceWithChildToDelete:ComponentInstance):boolean => {
                            return instanceWithChildToDelete.uniqueId === componentInstanceId;
                        });
                        this.disableEnableSelectedInputs(deletedInputComponentInstance);
                    };

                    var onFailed = (error:any) : void => {
                        console.log("Error deleting input");
                    };

                    this.addColorToItems([input]);

                    // Get service inputs of input (so after delete we will know the component instance)
                    this.$scope.loadInputInputs(input).then((result:boolean):void=>{
                        if (result && input.inputs.length>0) {
                            var componentInstanceId:string = input.inputs[0].componentInstanceId;
                            this.$scope.component.deleteServiceInput(input.uniqueId).then((deletedInput: Models.InputModel):void => {
                                onSuccess(deletedInput, componentInstanceId);
                            }, onFailed);
                        }
                    });
                };

                // Get confirmation modal text from menu.json
                var state = "deleteInput";
                var title:string = this.$scope.sdcMenu.alertMessages[state].title;
                var message:string = this.$scope.sdcMenu.alertMessages[state].message.format([input.name]);

                // Open confirmation modal
                this.ModalsHandler.openAlertModal(title, message).then(onDelete);
            }
        };

        private addColorToItems = (inputsCreated:Array<Models.InputModel>):void => {

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

    }
}
