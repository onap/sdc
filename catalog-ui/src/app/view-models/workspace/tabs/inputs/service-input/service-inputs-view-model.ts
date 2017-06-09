'use strict';
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {ComponentInstance, InstancesInputsOrPropertiesMapData, Service, IAppMenu, InputModel, PropertyModel, InputPropertyBase} from "app/models";
import {DataTypesService} from "app/services";
import {ModalsHandler, ResourceType} from "app/utils";


interface IServiceInputsViewModelScope extends IWorkspaceViewModelScope {

    vfInstancesList:Array<ComponentInstance>;
    instanceInputsMap:InstancesInputsOrPropertiesMapData; //this is tha map object that hold the selected inputs and the inputs we already used
    instancePropertiesMap:InstancesInputsOrPropertiesMapData;
    component:Service;
    sdcMenu:IAppMenu;
    isViewOnly:boolean;
    isArrowDisabled:boolean;
    onArrowPressed():void;
    checkArrowState():void;
    loadComponentInputs():void;
    loadInstanceInputs(instance:ComponentInstance):ng.IPromise<boolean> ;
    loadInstanceProperties(instance:ComponentInstance):ng.IPromise<boolean> ;
    loadInputPropertiesForInstance(instanceId:string, input:InputModel):ng.IPromise<boolean> ;
    loadInputInputs(input:InputModel):ng.IPromise<boolean>;
    deleteInput(input:InputModel):void
    openEditValueModal(input:InputModel):void;
    openSelectPropertyDataTypeViewModel(instanceId:string, property:PropertyModel):void;
    openEditPropertyDataTypeViewModel(property:PropertyModel):void;
    dataTypesService:DataTypesService;
}

export class ServiceInputsViewModel {

    static '$inject' = [
        '$scope',
        '$q',
        'ModalsHandler',
        'Sdc.Services.DataTypesService'
    ];

    constructor(private $scope:IServiceInputsViewModelScope,
                private $q:ng.IQService,
                private ModalsHandler:ModalsHandler,
                private DataTypesService:DataTypesService) {
        this.initScope();
        this.$scope.updateSelectedMenuItem();
        this.$scope.isViewOnly = this.$scope.isViewMode();
    }


    private getInputsOrPropertiesAlreadySelected = (instanceNormalizeName:string, arrayToFilter:Array<InputPropertyBase>):Array<any> => {
        let alreadySelectedInput = [];
        _.forEach(arrayToFilter, (inputOrProperty:InputPropertyBase) => {
            let expectedServiceInputName = instanceNormalizeName + '_' + inputOrProperty.name;
            let inputAlreadyInService = _.find(this.$scope.component.inputs, (serviceInput:InputModel) => {
                //Checking if the input prefix is the instance name + '_' + property/input name (prefix because we don't need to check full name for complex dataType)
                return serviceInput.name.substring(0, expectedServiceInputName.length) === expectedServiceInputName;
            });
            if (inputAlreadyInService) {
                inputOrProperty.isAlreadySelected = true;
                alreadySelectedInput.push(inputOrProperty);
            } else {
                inputOrProperty.isAlreadySelected = false;
            }
        });
        return alreadySelectedInput;
    };


    /*
     * When loading the screen again, we need to disabled the inputs that already created on the service,
     * we do that by comparing the service input name, to the instance name + '_' + the resource instance input name.
     */
    private disableEnableSelectedInputsOrPropertiesOnInit = (instance:ComponentInstance):void => {

        if (instance.originType === ResourceType.VF) {
            this.$scope.instanceInputsMap[instance.uniqueId] = this.getInputsOrPropertiesAlreadySelected(instance.normalizedName, instance.inputs);
        } else {
            this.$scope.instancePropertiesMap[instance.uniqueId] = this.getInputsOrPropertiesAlreadySelected(instance.normalizedName, instance.properties);
        }
    };

    /*
     * Enable Input/Property after delete
     */
    private enableInputsAfterDelete = (propertiesOrInputsDeletes:Array<InputPropertyBase>):void => {

        _.forEach(propertiesOrInputsDeletes, (deletedInputInput:InputPropertyBase) => { //Enable all component instance inputs deleted

            let inputOrPropertyDeleted:InputPropertyBase = _.find(this.$scope.instanceInputsMap[deletedInputInput.componentInstanceId], (inputOrProperty:InputPropertyBase) => {
                return inputOrProperty.uniqueId === deletedInputInput.uniqueId;
            });
            inputOrPropertyDeleted.isAlreadySelected = false;
            delete _.remove(this.$scope.instanceInputsMap[deletedInputInput.componentInstanceId], {uniqueId: inputOrPropertyDeleted.uniqueId})[0];
        });
    };

    /*
     * Enable Input/Property after delete
     */
    private enablePropertiesAfterDelete = (propertiesOrInputsDeletes:Array<InputPropertyBase>):void => {

        _.forEach(propertiesOrInputsDeletes, (deletedInputInput:InputPropertyBase) => { //Enable all component instance inputs deleted
            let componentInstance = _.find(this.$scope.vfInstancesList, (instance:ComponentInstance) => {
                return instance.uniqueId === deletedInputInput.componentInstanceId;
            });
            let inputOrPropertyDeleted:InputPropertyBase = _.find(this.$scope.instancePropertiesMap[deletedInputInput.componentInstanceId], (inputOrProperty:InputPropertyBase) => {
                return inputOrProperty.uniqueId === deletedInputInput.uniqueId;
            });

            let expectedName = componentInstance.normalizedName + '_' + inputOrPropertyDeleted.name;
            let isAnotherInputExist = _.find(this.$scope.component.inputs, (input:InputModel) => {
                return input.name.substring(0, expectedName.length) === expectedName;
            });
            if (!isAnotherInputExist) {
                inputOrPropertyDeleted.isAlreadySelected = false;
                delete _.remove(this.$scope.instancePropertiesMap[deletedInputInput.componentInstanceId], {uniqueId: inputOrPropertyDeleted.uniqueId})[0];
            }
        });
    };

    private initScope = ():void => {

        this.$scope.instanceInputsMap = new InstancesInputsOrPropertiesMapData();
        this.$scope.instancePropertiesMap = new InstancesInputsOrPropertiesMapData();
        this.$scope.isLoading = true;
        this.$scope.isArrowDisabled = true;
        // Why do we need this? we call this later.
        //this.$scope.component.getComponentInputs();

        let onSuccess = (componentInstances:Array<ComponentInstance>) => {
            console.log("component instances loaded: ", componentInstances);
            this.$scope.vfInstancesList = componentInstances;
            this.$scope.isLoading = false;
        };

        //This function will get al component instance for the left table - in
        // future the instances will be filter according to search text
        this.$scope.component.getComponentInstancesFilteredByInputsAndProperties().then(onSuccess);

        // This function will get the service inputs for the right table
        this.$scope.component.getComponentInputs();

        /*
         * When clicking on instance in the left table, this function will load all instance inputs
         */
        this.$scope.loadInstanceInputs = (instance:ComponentInstance):ng.IPromise<boolean> => {
            let deferred = this.$q.defer();

            let onSuccess = (inputs:Array<InputModel>) => {
                instance.inputs = inputs;
                this.disableEnableSelectedInputsOrPropertiesOnInit(instance);
                deferred.resolve(true);
            };

            let onError = () => {
                deferred.resolve(false);
            };

            if (!instance.inputs) {
                this.$scope.component.getComponentInstanceInputs(instance.uniqueId, instance.componentUid).then(onSuccess, onError);
                //this.disableEnableSelectedInputs(instance);
            } else {
                deferred.resolve(true);
            }
            return deferred.promise;
        };


        this.$scope.loadInstanceProperties = (instance:ComponentInstance):ng.IPromise<boolean> => {
            let deferred = this.$q.defer();

            let onSuccess = (properties:Array<PropertyModel>) => {
                instance.properties = properties;
                this.disableEnableSelectedInputsOrPropertiesOnInit(instance);
                deferred.resolve(true);
            };

            let onError = () => {
                deferred.resolve(false);
            };

            if (!instance.properties) {
                this.$scope.component.getComponentInstanceProperties(instance.uniqueId).then(onSuccess, onError);
            } else {
                deferred.resolve(true);
            }
            return deferred.promise;
        };

        /*
         * When clicking on instance input in the left or right table, this function will load all properties of the selected input
         */
        this.$scope.loadInputPropertiesForInstance = (instanceId:string, input:InputModel):ng.IPromise<boolean> => {
            let deferred = this.$q.defer();

            let onSuccess = (properties:Array<PropertyModel>) => {
                input.properties = properties;
                deferred.resolve(true);
            };

            let onError = () => {
                deferred.resolve(false)
            };

            if (!input.properties) {
                this.$scope.component.getComponentInstanceInputProperties(instanceId, input.uniqueId).then(onSuccess, onError);
            } else {
                deferred.resolve(true);
            }
            return deferred.promise;
        };

        /*
         * When clicking on input in the right table, this function will load all inputs of the selected input
         */
        this.$scope.loadInputInputs = (input:InputModel):ng.IPromise<boolean> => {
            let deferred = this.$q.defer();

            let onSuccess = () => {
                deferred.resolve(true);
            };
            let onError = () => {
                deferred.resolve(false);
            };

            if (!input.inputs) { // Caching, if exists do not get it.
                this.$scope.component.getServiceInputInputsAndProperties(input.uniqueId).then(onSuccess, onError);
            } else {
                deferred.resolve(true);
            }
            return deferred.promise;
        };

        /*
         * When pressing the arrow, we create service inputs from the inputs selected
         */
        this.$scope.onArrowPressed = ():void => {
            let onSuccess = (inputsCreated:Array<InputModel>) => {

                //disabled all the inputs in the left table
                _.forEach(this.$scope.instanceInputsMap, (inputs:Array<InputModel>, instanceId:string) => {
                    _.forEach(inputs, (input:InputModel) => {
                        input.isAlreadySelected = true;
                    });
                });
                _.forEach(this.$scope.instancePropertiesMap, (properties:Array<PropertyModel>, instanceId:string) => {
                    _.forEach(properties, (property:PropertyModel) => {
                        property.isAlreadySelected = true;
                    });
                });
                this.addColorToItems(inputsCreated);
            };

            let onFailed = (error:any) => {
                this.$scope.isArrowDisabled = false;
                console.log("Error declaring input/property");
            };

            this.$scope.isArrowDisabled = true;
            this.$scope.component.createInputsFormInstances(this.$scope.instanceInputsMap, this.$scope.instancePropertiesMap).then(onSuccess, onFailed);
        };


        /* Iterates through array of selected inputs and properties and returns true if there is at least one new selection on left */
        this.$scope.checkArrowState = ()=> {

            let newInputSelected:boolean = _.some(this.$scope.instanceInputsMap, (inputs:Array<InputModel>) => {
                return _.some(inputs, (input:InputModel)=> {
                    return input.isAlreadySelected === false;
                });
            });

            let newPropSelected:boolean = _.some(this.$scope.instancePropertiesMap, (properties:Array<PropertyModel>) => {
                return _.some(properties, (property:PropertyModel) => {
                    return property.isAlreadySelected === false;
                });
            });

            this.$scope.isArrowDisabled = !(newInputSelected || newPropSelected);

        };

        this.$scope.deleteInput = (inputToDelete:InputModel):void => {

            let onDelete = ():void => {

                let onSuccess = (deletedInput:InputModel):void => {
                    if (deletedInput.inputs && deletedInput.inputs.length > 0) { // Enable input declared from input
                        this.enableInputsAfterDelete(deletedInput.inputs);
                    }

                    if (deletedInput.properties && deletedInput.properties.length > 0) { // Enable properties
                        this.enablePropertiesAfterDelete(deletedInput.properties);
                    }
                    deletedInput.isDeleteDisabled = false;
                    this.$scope.checkArrowState();

                };

                let onFailed = (error:any):void => {
                    console.log("Error deleting input");
                    inputToDelete.isDeleteDisabled = false;
                };

                inputToDelete.isDeleteDisabled = true;
                this.addColorToItems([inputToDelete]);
                this.$scope.component.deleteServiceInput(inputToDelete.uniqueId).then((deletedInput:InputModel):void => {
                    onSuccess(deletedInput);
                }, onFailed);
            };

            // Get confirmation modal text from menu.json
            let state = "deleteInput";
            let title:string = this.$scope.sdcMenu.alertMessages[state].title;
            let message:string = this.$scope.sdcMenu.alertMessages[state].message.format([inputToDelete.name]);

            // Open confirmation modal
            this.ModalsHandler.openAlertModal(title, message).then(onDelete);
        };

        this.$scope.openEditValueModal = (input:InputModel) => {
            this.ModalsHandler.openEditInputValueModal(input);
        };

        this.$scope.openSelectPropertyDataTypeViewModel = (instanceId:string, property:PropertyModel) => {
            //to open the select data type modal
            let selectedInstance = _.find(this.$scope.vfInstancesList, {uniqueId: instanceId});
            this.DataTypesService.selectedInstance = selectedInstance; //set the selected instance on the service for compering the input name on the service & the complex property
            this.DataTypesService.selectedComponentInputs = this.$scope.component.inputs; // set all the service inputs on the data type service
            let filteredPropertiesMap = _.filter(this.$scope.instancePropertiesMap[instanceId], (instanceProperty)=> {
                return instanceProperty.name == property.name;
            });//get all properties under the specific property
            this.DataTypesService.selectedPropertiesName = property.propertiesName;

            this.ModalsHandler.openSelectDataTypeModal(property, this.$scope.component, this.$scope.component.properties, filteredPropertiesMap).then((selectedProperty:PropertyModel)=> {
                if (selectedProperty && selectedProperty.propertiesName) {
                    let propertyToUpdate:PropertyModel = _.find(selectedInstance.properties, {uniqueId: selectedProperty.uniqueId});
                    let existingProperty:PropertyModel = (<PropertyModel>_.find(this.$scope.instancePropertiesMap[instanceId], {uniqueId: propertyToUpdate.uniqueId}));

                    if (existingProperty) {
                        existingProperty.propertiesName = selectedProperty.propertiesName;
                        existingProperty.input = selectedProperty.input;
                        existingProperty.isAlreadySelected = false;
                    } else {
                        propertyToUpdate.propertiesName = selectedProperty.propertiesName;
                        propertyToUpdate.input = selectedProperty.input;
                        this.$scope.instancePropertiesMap[instanceId].push(propertyToUpdate);

                    }
                    this.$scope.checkArrowState();

                }
            });
        };


        this.$scope.openEditPropertyDataTypeViewModel = (property:PropertyModel)=> {
            this.ModalsHandler.openEditPropertyModal(property, this.$scope.component, this.$scope.component.properties, false).then(() => {
            });
        }
    };

    private addColorToItems = (inputsCreated:Array<InputModel>):void => {

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
