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
import {
    PROPERTY_TYPES, ModalsHandler, ValidationUtils, PROPERTY_VALUE_CONSTRAINTS, FormState, PROPERTY_DATA} from "app/utils";
import {DataTypesService} from "app/services";
import {PropertyModel, DataTypesMap, Component} from "app/models";
import {ComponentInstance} from "../../../../models/componentsInstances/componentInstance";

export interface IEditPropertyModel {
    property:PropertyModel;
    types:Array<string>;
    simpleTypes:Array<string>;
}

interface IPropertyFormViewModelScope extends ng.IScope {
    forms:any;
    editForm:ng.IFormController;
    footerButtons:Array<any>;
    isNew:boolean;
    isLoading:boolean;
    isService:boolean;
    validationPattern:RegExp;
    propertyNameValidationPattern:RegExp;
    commentValidationPattern:RegExp;
    editPropertyModel:IEditPropertyModel;
    modalInstanceProperty:ng.ui.bootstrap.IModalServiceInstance;
    currentPropertyIndex:number;
    isLastProperty:boolean;
    myValue:any;
    nonPrimitiveTypes:Array<string>;
    dataTypes:DataTypesMap;
    isTypeDataType:boolean;
    maxLength:number;
    isPropertyValueOwner:boolean;
    isVnfConfiguration:boolean;

    validateJson(json:string):boolean;
    save(doNotCloseModal?:boolean):void;
    getValidationPattern(type:string):RegExp;
    validateIntRange(value:string):boolean;
    close():void;
    onValueChange():void;
    onSchemaTypeChange():void;
    onTypeChange(resetSchema:boolean):void;
    showSchema():boolean;
    delete(property:PropertyModel):void;
    getPrev():void;
    getNext():void;
    isSimpleType(typeName:string):boolean;
    getDefaultValue():any;
}

export class PropertyFormViewModel {

    static '$inject' = [
        '$scope',
        'Sdc.Services.DataTypesService',
        '$uibModalInstance',
        'property',
        'ValidationPattern',
        'PropertyNameValidationPattern',
        'CommentValidationPattern',
        'ValidationUtils',
        'component',
        '$filter',
        'ModalsHandler',
        'filteredProperties',
        '$timeout',
        'isPropertyValueOwner'
    ];

    private formState:FormState;

    constructor(private $scope:IPropertyFormViewModelScope,
                private DataTypesService:DataTypesService,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private property:PropertyModel,
                private ValidationPattern:RegExp,
                private PropertyNameValidationPattern:RegExp,
                private CommentValidationPattern:RegExp,
                private ValidationUtils:ValidationUtils,
                private component:Component,
                private $filter:ng.IFilterService,
                private ModalsHandler:ModalsHandler,
                private filteredProperties:Array<PropertyModel>,
                private $timeout:ng.ITimeoutService,
                private isPropertyValueOwner:boolean) {

        this.formState = angular.isDefined(property.name) ? FormState.UPDATE : FormState.CREATE;
        this.initScope();
    }

    private initResource = ():void => {
        this.$scope.editPropertyModel.property = new PropertyModel(this.property);
        this.$scope.editPropertyModel.property.type = this.property.type ? this.property.type : null;
        this.$scope.editPropertyModel.property.value = this.$scope.editPropertyModel.property.value || this.$scope.editPropertyModel.property.defaultValue;
        this.setMaxLength();
        this.initAddOnLabels();
    };

    //init property add-ons labels that show up at the left side of the input.
    private initAddOnLabels = () => {
        if (this.$scope.editPropertyModel.property.name == 'network_role' && this.$scope.isService) {
            //the server sends back the normalized name. Remove it (to prevent interference with validation) and set the addon label to the component name directly.
            //Note: this cant be done in properties.ts because we dont have access to the component
            if (this.$scope.editPropertyModel.property.value) {
                let splitProp = this.$scope.editPropertyModel.property.value.split(new RegExp(this.component.normalizedName + '.', "gi"));
                this.$scope.editPropertyModel.property.value = splitProp.pop();
            }
            this.$scope.editPropertyModel.property.addOn = this.component.name;
        }
    }

    private initEditPropertyModel = ():void => {
        this.$scope.editPropertyModel = {
            property: null,
            types: PROPERTY_DATA.TYPES,
            simpleTypes: PROPERTY_DATA.SIMPLE_TYPES
        };

        this.initResource();
    };

    private initForNotSimpleType = ():void => {
        let property = this.$scope.editPropertyModel.property;
        this.$scope.isTypeDataType = this.DataTypesService.isDataTypeForPropertyType(this.$scope.editPropertyModel.property);
        if (property.type && this.$scope.editPropertyModel.simpleTypes.indexOf(property.type) == -1) {
            if (!(property.value || property.defaultValue)) {
                switch (property.type) {
                    case PROPERTY_TYPES.MAP:
                        this.$scope.myValue = {'': null};
                        break;
                    case PROPERTY_TYPES.LIST:
                        this.$scope.myValue = [];
                        break;
                    default:
                        this.$scope.myValue = {};
                }
            } else {
                this.$scope.myValue = JSON.parse(property.value || property.defaultValue);
            }
        }
    };

    private setMaxLength = ():void => {
        switch (this.$scope.editPropertyModel.property.type) {
            case PROPERTY_TYPES.MAP:
            case PROPERTY_TYPES.LIST:
                this.$scope.maxLength = this.$scope.editPropertyModel.property.schema.property.type == PROPERTY_TYPES.JSON ?
                    PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH :
                    PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
                break;
            case PROPERTY_TYPES.JSON:
                this.$scope.maxLength = PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH;
                break;
            default:
                this.$scope.maxLength =PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
        }
    };


    private initScope = ():void => {

        //scope properties
        this.$scope.forms = {};
        this.$scope.validationPattern = this.ValidationPattern;
        this.$scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;
        this.$scope.commentValidationPattern = this.CommentValidationPattern;
        this.$scope.isLoading = false;
        this.$scope.isNew = (this.formState === FormState.CREATE);
        this.$scope.isService = this.component.isService();
        this.$scope.modalInstanceProperty = this.$uibModalInstance;
        this.$scope.currentPropertyIndex = _.findIndex(this.filteredProperties, i=> i.name == this.property.name);
        this.$scope.isLastProperty = this.$scope.currentPropertyIndex == (this.filteredProperties.length - 1);
        this.$scope.dataTypes = this.DataTypesService.getAllDataTypes();
        this.$scope.isPropertyValueOwner = this.isPropertyValueOwner;
        this.initEditPropertyModel();

        //check if property of VnfConfiguration
        this.$scope.isVnfConfiguration = false;
        if(angular.isArray(this.component.componentInstances)) {
            var componentPropertyOwner:ComponentInstance = this.component.componentInstances.find((ci:ComponentInstance) => {
                return ci.uniqueId === this.property.resourceInstanceUniqueId;
            });
            if (componentPropertyOwner.componentName === 'vnfConfiguration') {
                this.$scope.isVnfConfiguration = true;
            }
        }

        this.$scope.nonPrimitiveTypes = _.filter(Object.keys(this.$scope.dataTypes), (type:string)=> {
            return this.$scope.editPropertyModel.types.indexOf(type) == -1;
        });
        this.initForNotSimpleType();


        this.$scope.validateJson = (json:string):boolean => {
            if (!json) {
                return true;
            }
            return this.ValidationUtils.validateJson(json);
        };


        //scope methods
        this.$scope.save = (doNotCloseModal?:boolean):void => {
            let property:PropertyModel = this.$scope.editPropertyModel.property;
            this.$scope.editPropertyModel.property.description = this.ValidationUtils.stripAndSanitize(this.$scope.editPropertyModel.property.description);
            //if read only - or no changes made - just closes the modal
            //need to check for property.value changes manually to detect if map properties deleted
            if ((this.$scope.editPropertyModel.property.readonly && !this.$scope.isPropertyValueOwner)
                || (!this.$scope.forms.editForm.$dirty && angular.equals(JSON.stringify(this.$scope.myValue), this.$scope.editPropertyModel.property.value))) {
                this.$uibModalInstance.close();
                return;
            }

            this.$scope.isLoading = true;

            let onPropertyFaild = (response):void => {
                console.info('onFaild', response);
                this.$scope.isLoading = false;
            };

            let onPropertySuccess = (propertyFromBE:PropertyModel):void => {
                console.info('onPropertyResourceSuccess : ', propertyFromBE);
                this.$scope.isLoading = false;
                this.filteredProperties[this.$scope.currentPropertyIndex] = propertyFromBE;
                if (!doNotCloseModal) {
                    this.$uibModalInstance.close(propertyFromBE);
                } else {
                    this.$scope.forms.editForm.$setPristine();
                    this.$scope.editPropertyModel.property = new PropertyModel();
                }
            };

            //in case we have uniqueId we call update method
            if (this.$scope.isPropertyValueOwner) {
                if (!this.$scope.editPropertyModel.property.simpleType && !this.$scope.isSimpleType(property.type)) {
                    let myValueString:string = JSON.stringify(this.$scope.myValue);
                    property.value = myValueString;
                }
                this.component.updateInstanceProperty(property).then(onPropertySuccess, onPropertyFaild);
            } else {
                if (!this.$scope.editPropertyModel.property.simpleType && !this.$scope.isSimpleType(property.type)) {
                    let myValueString:string = JSON.stringify(this.$scope.myValue);
                    property.defaultValue = myValueString;
                } else {
                    this.$scope.editPropertyModel.property.defaultValue = this.$scope.editPropertyModel.property.value;
                }
                this.component.addOrUpdateProperty(property).then(onPropertySuccess, onPropertyFaild);
            }
        };

        this.$scope.getPrev = ():void=> {
            this.property = this.filteredProperties[--this.$scope.currentPropertyIndex];
            this.initResource();
            this.initForNotSimpleType();
            this.$scope.isLastProperty = false;
        };

        this.$scope.getNext = ():void=> {
            this.property = this.filteredProperties[++this.$scope.currentPropertyIndex];
            this.initResource();
            this.initForNotSimpleType();
            this.$scope.isLastProperty = this.$scope.currentPropertyIndex == (this.filteredProperties.length - 1);
        };

        this.$scope.isSimpleType = (typeName:string):boolean=> {
            return typeName && this.$scope.editPropertyModel.simpleTypes.indexOf(typeName) != -1;
        };

        this.$scope.showSchema = ():boolean => {
            return [PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP].indexOf(this.$scope.editPropertyModel.property.type) > -1;
        };

        this.$scope.getValidationPattern = (type:string):RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        this.$scope.validateIntRange = (value:string):boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        this.$scope.close = ():void => {
            this.$uibModalInstance.close();
        };

        // put default value when instance value is empty
        this.$scope.onValueChange = ():void => {
            if (!this.$scope.editPropertyModel.property.value) {
                if (this.$scope.isPropertyValueOwner) {
                    this.$scope.editPropertyModel.property.value = this.$scope.editPropertyModel.property.defaultValue;
                }
            }
        };

        // Add the done button at the footer.
        this.$scope.footerButtons = [
            {'name': 'Save', 'css': 'blue', 'callback': this.$scope.save},
            {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
        ];

        this.$scope.$watch("forms.editForm.$invalid", (newVal, oldVal) => {
            this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
        });

        this.$scope.getDefaultValue = ():any => {
            return this.$scope.isPropertyValueOwner ? this.$scope.editPropertyModel.property.defaultValue : null;
        };

        this.$scope.onTypeChange = ():void => {
            this.$scope.editPropertyModel.property.value = '';
            this.$scope.editPropertyModel.property.defaultValue = '';
            this.setMaxLength();
            this.initForNotSimpleType();
        };

        this.$scope.onSchemaTypeChange = ():void => {
            if (this.$scope.editPropertyModel.property.type == PROPERTY_TYPES.MAP) {
                this.$scope.myValue = {'': null};
            } else if (this.$scope.editPropertyModel.property.type == PROPERTY_TYPES.LIST) {
                this.$scope.myValue = [];
            }
            this.setMaxLength();
        };

        this.$scope.delete = (property:PropertyModel):void => {
            let onOk = ():void => {
                this.component.deleteProperty(property.uniqueId).then(
                    this.$scope.close
                );
            };
            let title:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TEXT", "{'name': '" + property.name + "'}");
            this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
        };
    }
}
