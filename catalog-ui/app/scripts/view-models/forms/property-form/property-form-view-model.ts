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
/// <reference path="../../../references"/>

module Sdc.ViewModels {
    'use strict';

    export interface IEditPropertyModel {
        property: Models.PropertyModel;
        types: Array<string>;
        simpleTypes: Array<string>;
        sources: Array<string>;
    }

    interface IPropertyFormViewModelScope extends ng.IScope {
        forms:any;
        editForm:ng.IFormController;
        footerButtons: Array<any>;
        isNew: boolean;
        isLoading: boolean;
        isService: boolean;
        validationPattern: RegExp;
        propertyNameValidationPattern: RegExp;
        commentValidationPattern: RegExp;
        editPropertyModel: IEditPropertyModel;
        modalInstanceProperty:ng.ui.bootstrap.IModalServiceInstance;
        currentPropertyIndex:number;
        isLastProperty:boolean;
        myValue:any;
        nonPrimitiveTypes:Array<string>;
        dataTypes:Models.DataTypesMap;
        isTypeDataType:boolean;
        maxLength:number;

        save(doNotCloseModal?:boolean): void;
        getValidationPattern(type:string): RegExp;
        validateIntRange(value:string):boolean;
        close(): void;
        onValueChange(): void;
        onSchemaTypeChange():void;
        onTypeChange(resetSchema:boolean): void;
        isPropertyValueOwner():boolean;
        showSchema(): boolean;
        delete(property:Models.PropertyModel): void;
        getPrev(): void;
        getNext(): void;
        isSimpleType(typeName:string):boolean;
        getDefaultValue():any;
    }

    export class PropertyFormViewModel {

        static '$inject' = [
            '$scope',
            'Sdc.Services.DataTypesService',
            '$modalInstance',
            'property',
            'ValidationPattern',
            'PropertyNameValidationPattern',
            'CommentValidationPattern',
            'ValidationUtils',
            'component',
            '$filter',
            'ModalsHandler',
            'filteredProperties',
            '$timeout'
        ];

        private formState: Sdc.Utils.Constants.FormState;

        constructor(private $scope:IPropertyFormViewModelScope,
                    private DataTypesService:Sdc.Services.DataTypesService,
                    private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                    private property: Models.PropertyModel,
                    private ValidationPattern:RegExp,
                    private PropertyNameValidationPattern: RegExp,
                    private CommentValidationPattern:RegExp,
                    private ValidationUtils:Sdc.Utils.ValidationUtils,
                    private component: Models.Components.Component,
                    private $filter:ng.IFilterService,
                    private ModalsHandler:Utils.ModalsHandler,
                    private filteredProperties: Array<Models.PropertyModel>,
                    private $timeout: ng.ITimeoutService) {

            this.formState = angular.isDefined(property.name) ?  Utils.Constants.FormState.UPDATE :  Utils.Constants.FormState.CREATE;
            this.initScope();
        }

        private initResource = ():void => {
            this.$scope.editPropertyModel.property = new Sdc.Models.PropertyModel(this.property);
            this.$scope.editPropertyModel.property.type = this.property.type? this.property.type: null;
            this.setMaxLength();
            // if (this.$scope.editPropertyModel.types.indexOf(this.property.type) === -1 && !this.$scope.isNew) {
            //     this.property.type = "string";
            // }
        };

        private initEditPropertyModel = ():void => {
            this.$scope.editPropertyModel = {
                property: null,
                types: Utils.Constants.PROPERTY_DATA.TYPES,
                simpleTypes: Utils.Constants.PROPERTY_DATA.SIMPLE_TYPES,
                sources: Utils.Constants.PROPERTY_DATA.SOURCES
            };

            this.initResource();
        };

        private initForNotSimpleType = ():void => {
            let property = this.$scope.editPropertyModel.property;
            this.$scope.isTypeDataType=this.DataTypesService.isDataTypeForPropertyType(this.$scope.editPropertyModel.property,this.$scope.dataTypes);
            if(property.type && this.$scope.editPropertyModel.simpleTypes.indexOf(property.type)==-1){
                if(!(property.value||property.defaultValue)) {
                    switch (property.type) {
                        case Utils.Constants.PROPERTY_TYPES.MAP:
                            this.$scope.myValue = {'':null};
                            break;
                        case Utils.Constants.PROPERTY_TYPES.LIST:
                            this.$scope.myValue = [];
                            break;
                        default:
                            this.$scope.myValue = {};
                    }
                }else{
                    this.$scope.myValue = JSON.parse(property.value||property.defaultValue);
                }
            }
        };

        private setMaxLength = ():void => {
            switch (this.$scope.editPropertyModel.property.type) {
                case Utils.Constants.PROPERTY_TYPES.MAP:
                case Utils.Constants.PROPERTY_TYPES.LIST:
                    this.$scope.maxLength = this.$scope.editPropertyModel.property.schema.property.type == Utils.Constants.PROPERTY_TYPES.JSON?
                        Utils.Constants.PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH:
                        Utils.Constants.PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
                    break;
                case Utils.Constants.PROPERTY_TYPES.JSON:
                    this.$scope.maxLength = Utils.Constants.PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH;
                    break;
                default:
                    this.$scope.maxLength = Utils.Constants.PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
            }
        };


        private initScope = ():void => {

            //scope properties
            this.$scope.forms = {};
            this.$scope.validationPattern = this.ValidationPattern;
            this.$scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;
            this.$scope.commentValidationPattern = this.CommentValidationPattern;
            this.$scope.isLoading = false;
            this.$scope.isNew = (this.formState === Utils.Constants.FormState.CREATE);
            this.$scope.isService = this.component.isService();
            this.$scope.modalInstanceProperty = this.$modalInstance;
            this.$scope.currentPropertyIndex = _.findIndex(this.filteredProperties, i=> i.name == this.property.name );
            this.$scope.isLastProperty= this.$scope.currentPropertyIndex==(this.filteredProperties.length-1);

            this.initEditPropertyModel();

            this.DataTypesService.getAllDataTypes().then((response:any) => {
                this.$scope.dataTypes = response;
                delete response['tosca.datatypes.Root'];
                this.$scope.nonPrimitiveTypes =_.filter(Object.keys(response),(type:string)=>{
                    return this.$scope.editPropertyModel.types.indexOf(type)==-1;
                });
                this.initForNotSimpleType();
            }, (err)=> {});





            //scope methods
            this.$scope.save = (doNotCloseModal?:boolean):void => {
                let property:Models.PropertyModel = this.$scope.editPropertyModel.property;
                this.$scope.editPropertyModel.property.description = this.ValidationUtils.stripAndSanitize(this.$scope.editPropertyModel.property.description);
                ////if read only - just closes the modal
                if (this.$scope.editPropertyModel.property.readonly && !this.$scope.isPropertyValueOwner()) {
                    this.$modalInstance.close();
                    return;
                }

                this.$scope.isLoading = true;

                let onPropertyFaild = (response):void => {
                    console.info('onFaild', response);
                    this.$scope.isLoading = false;
                };

                let onPropertySuccess = (propertyFromBE:Models.PropertyModel):void => {
                    console.info('onPropertyResourceSuccess : ', propertyFromBE);
                    this.$scope.isLoading = false;

                    if (!doNotCloseModal) {
                        this.$modalInstance.close();
                    } else {
                        this.$scope.forms.editForm.$setPristine();
                        this.$scope.editPropertyModel.property = new Models.PropertyModel();
                    }
                };

                //in case we have uniqueId we call update method
                if (this.$scope.isPropertyValueOwner()) {
                    if(!this.$scope.editPropertyModel.property.simpleType && !this.$scope.isSimpleType(property.type)){
                        let myValueString:string = JSON.stringify(this.$scope.myValue);
                        property.value = myValueString;
                    }
                    this.component.updateInstanceProperty(property).then(onPropertySuccess, onPropertyFaild);
                } else {
                    if(!this.$scope.editPropertyModel.property.simpleType && !this.$scope.isSimpleType(property.type)){
                        let myValueString:string = JSON.stringify(this.$scope.myValue);
                        property.defaultValue = myValueString;
                    }else{
                        this.$scope.editPropertyModel.property.defaultValue = this.$scope.editPropertyModel.property.value;
                    }
                    this.component.addOrUpdateProperty(property).then(onPropertySuccess, onPropertyFaild);
                }
            };


            this.$scope.isPropertyValueOwner = ():boolean=> {
                return this.component.isService() || !!this.component.selectedInstance;
            };

            this.$scope.getPrev = ():void=> {
                this.property = this.filteredProperties[--this.$scope.currentPropertyIndex];
                this.initResource();
                this.initForNotSimpleType();
                this.$scope.isLastProperty=false;
            };

            this.$scope.getNext = ():void=> {
                this.property = this.filteredProperties[++this.$scope.currentPropertyIndex];
                this.initResource();
                this.initForNotSimpleType();
                this.$scope.isLastProperty= this.$scope.currentPropertyIndex==(this.filteredProperties.length-1);
            };

            this.$scope.isSimpleType = (typeName:string):boolean=>{
                return typeName && this.$scope.editPropertyModel.simpleTypes.indexOf(typeName)!=-1;
            };

            this.$scope.showSchema = () :boolean => {
                return [Utils.Constants.PROPERTY_TYPES.LIST, Utils.Constants.PROPERTY_TYPES.MAP].indexOf(this.$scope.editPropertyModel.property.type) > -1;
            };

            this.$scope.getValidationPattern = (type:string):RegExp => {
                return this.ValidationUtils.getValidationPattern(type);
            };

            this.$scope.validateIntRange = (value:string):boolean => {
                return !value || this.ValidationUtils.validateIntRange(value);
            };

            this.$scope.close = ():void => {
                this.$modalInstance.close();
            };

            // put default value when instance value is empty
            this.$scope.onValueChange = ():void => {
                if (!this.$scope.editPropertyModel.property.value) {
                    if (this.$scope.isPropertyValueOwner()) {
                        this.$scope.editPropertyModel.property.value = this.$scope.editPropertyModel.property.defaultValue;
                    }
                }
            };

            // Add the done button at the footer.
            this.$scope.footerButtons = [
                {'name': 'Save', 'css': 'blue', 'callback': this.$scope.save },
                {'name': 'Cancel', 'css': 'grey', 'callback':this.$scope.close }
            ];

            this.$scope.$watch("forms.editForm.$invalid", (newVal, oldVal) => {
                this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
            });

            this.$scope.getDefaultValue = ():any => {
                return this.$scope.isPropertyValueOwner() ? this.$scope.editPropertyModel.property.defaultValue : null;
            };

            this.$scope.onTypeChange = ():void => {
                this.$scope.editPropertyModel.property.value = '';
                this.$scope.editPropertyModel.property.defaultValue = '';
                this.setMaxLength();
                this.initForNotSimpleType();
            };

            this.$scope.onSchemaTypeChange = ():void => {
                if(this.$scope.editPropertyModel.property.type==Utils.Constants.PROPERTY_TYPES.MAP){
                    this.$scope.myValue={'':null};
                }else if(this.$scope.editPropertyModel.property.type==Utils.Constants.PROPERTY_TYPES.LIST){
                    this.$scope.myValue=[];
                }
                this.setMaxLength();
            };

            this.$scope.delete = (property:Models.PropertyModel):void => {
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
}
