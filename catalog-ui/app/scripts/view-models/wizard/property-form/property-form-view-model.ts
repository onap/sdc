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

module Sdc.ViewModels.Wizard {
    'use strict';

    export interface IEditPropertyModel{
        property: Models.PropertyModel;
        types: Array<string>;
        simpleTypes: Array<string>;
        sources: Array<string>;
    }

    export interface IPropertyFormViewModelScope extends ng.IScope{

        $$childTail: any;
        editForm:ng.IFormController;
        forms:any;
        footerButtons: Array<any>;
        isNew: boolean;
        isLoading: boolean;
        validationPattern: RegExp;
        propertyNameValidationPattern: RegExp;
        integerValidationPattern: RegExp;
        floatValidationPattern: RegExp;
        commentValidationPattern: RegExp;
        listRegex: Sdc.Utils.IMapRegex;
        mapRegex: Sdc.Utils.IMapRegex;
        editPropertyModel: IEditPropertyModel;
        modalInstanceProperty: ng.ui.bootstrap.IModalServiceInstance;
        save(doNotCloseModal?: boolean): void;
        saveAndAnother(): void;
        getValidation(): RegExp;
        validateIntRange(value:string):boolean;
        close(): void;
        onValueChange(): void;
        onTypeChange(resetSchema:boolean): void;
        showSchema(): boolean;
        getValidationTranslate():string;
        validateUniqueKeys(viewValue:string):boolean;
    }

    export class PropertyFormViewModel{

        private originalValue: string;

        static '$inject' = [
            '$scope',
            '$modalInstance',
            'property',
            'ValidationPattern',
            'PropertyNameValidationPattern',
            'IntegerNoLeadingZeroValidationPattern',
            'FloatValidationPattern',
            'CommentValidationPattern',
            'ValidationUtils',
            'component'
        ];

        private formState: Utils.Constants.FormState;
        private entityId: string;
        private resourceInstanceUniqueId: string;
        private readonly: boolean;

        constructor(
            private $scope:IPropertyFormViewModelScope,
            private $modalInstance: ng.ui.bootstrap.IModalServiceInstance,
            private property : Models.PropertyModel,
            private ValidationPattern : RegExp,
            private PropertyNameValidationPattern: RegExp,
            private IntegerNoLeadingZeroValidationPattern : RegExp,
            private FloatValidationPattern : RegExp,
            private CommentValidationPattern: RegExp,
            private ValidationUtils: Sdc.Utils.ValidationUtils,
            private component:Models.Components.Component
        ){
            this.entityId = this.component.uniqueId;
            this.formState = angular.isDefined(property.name) ? Utils.Constants.FormState.UPDATE : Utils.Constants.FormState.CREATE;
            this.initScope();
        }


        private initResource = (): void => {
            this.$scope.editPropertyModel.property = new Sdc.Models.PropertyModel(this.property);
            this.originalValue = this.property.defaultValue;
            if(this.$scope.editPropertyModel.types.indexOf(this.property.type) === -1 && !this.$scope.isNew){
                this.property.type = "string";
            }
        };

        private initEditPropertyModel = (): void => {
            this.$scope.editPropertyModel = {
                property: null,
                types: ['integer', 'string', 'float', 'boolean', 'list', 'map'],
                simpleTypes: ['integer', 'string', 'float', 'boolean'],
                sources: ['A&AI', 'Order', 'Runtime']
            };

            this.initResource();
        };

        private initScope = (): void => {

            this.$scope.modalInstanceProperty = this.$modalInstance;
            //scope properties
            this.$scope.validationPattern = this.ValidationPattern;
            this.$scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;
            this.$scope.integerValidationPattern = this.IntegerNoLeadingZeroValidationPattern;
            this.$scope.floatValidationPattern = this.FloatValidationPattern;
            this.$scope.commentValidationPattern = this.CommentValidationPattern;

            //map & list validation patterns
            this.$scope.listRegex = this.ValidationUtils.getPropertyListPatterns();
            this.$scope.mapRegex = this.ValidationUtils.getPropertyMapPatterns();

            this.$scope.isLoading = false;
            this.$scope.isNew = (this.formState === Utils.Constants.FormState.CREATE);
            this.initEditPropertyModel();

            //scope methods
            this.$scope.save = (): void => {
                this.$scope.editPropertyModel.property.description = this.ValidationUtils.stripAndSanitize(this.$scope.editPropertyModel.property.description);
                this.$scope.isLoading = true;

                let onFailed = (response) => {
                    console.info('onFaild',response);
                    this.$scope.isLoading = false;
                    this.$scope.editPropertyModel.property.readonly = this.readonly;
                    this.$scope.editPropertyModel.property.resourceInstanceUniqueId = this.resourceInstanceUniqueId;
                };

                let onSuccess = (property: Models.PropertyModel): void => {
                    console.info('property added : ',property);
                    this.$scope.isLoading = false;
                    property.resourceInstanceUniqueId = this.resourceInstanceUniqueId;
                    property.readonly = (property.parentUniqueId !== this.component.uniqueId) /*|| this.component.isService()*/;

                    this.$modalInstance.close();
                };

                this.resourceInstanceUniqueId = this.$scope.editPropertyModel.property.resourceInstanceUniqueId;
                this.readonly = this.$scope.editPropertyModel.property.readonly;
                this.$scope.editPropertyModel.property.defaultValue = this.$scope.editPropertyModel.property.defaultValue ? this.$scope.editPropertyModel.property.defaultValue:null;

                this.component.addOrUpdateProperty(this.$scope.editPropertyModel.property).then(onSuccess, onFailed);
            };

            this.$scope.saveAndAnother = (): void => {
                this.$scope.save();
            };

            this.$scope.showSchema = () :boolean => {
                return ['list', 'map'].indexOf(this.$scope.editPropertyModel.property.type) > -1;
            };

            this.$scope.getValidationTranslate = () : string => {
                let result = "PROPERTY_EDIT_PATTERN";
                if (this.$scope.showSchema()) {

                    result = "PROPERTY_EDIT_" + this.$scope.editPropertyModel.property.type.toUpperCase();

                    if(this.$scope.editPropertyModel.property.schema.property.type === 'string') {
                        result += "_STRING";
                    } else {
                        result += "_GENERIC";
                    }
                }

                return result;
            };

            this.$scope.getValidation = () : RegExp => {
                let type = this.$scope.editPropertyModel.property.type;
                switch (type){
                    case 'integer':
                        return this.$scope.integerValidationPattern;
                    case 'float':
                        return this.$scope.floatValidationPattern;
                    case 'list':
                        return this.$scope.listRegex[this.$scope.editPropertyModel.property.schema.property.type];
                    case 'map':
                        return this.$scope.mapRegex[this.$scope.editPropertyModel.property.schema.property.type];
                    default :
                        return null;
                }
            };

            this.$scope.validateUniqueKeys = (viewValue:string) : boolean => {
                if(this.$scope.editPropertyModel.property.type === 'map') {
                    return this.ValidationUtils.validateUniqueKeys(viewValue);
                }
                else {
                    return true; //always valid if not a map
                }
            };

           this.$scope.validateIntRange = (value:string):boolean => {
                return !value || this.ValidationUtils.validateIntRange(value);
           };

            this.$scope.close = (): void => {
                this.$modalInstance.close();
            };

            this.$scope.onValueChange = (): void => {
                if(!this.$scope.editPropertyModel.property.defaultValue && this.$scope.editPropertyModel.property.required) {
                    this.$scope.editPropertyModel.property.defaultValue = this.originalValue;
                }
            };

            this.$scope.onTypeChange = (resetSchema:boolean): void => {
                this.$scope.editPropertyModel.property.defaultValue = '';
                if (resetSchema) {
                    this.$scope.editPropertyModel.property.schema.property.type = '';
                }
            };

            //new form layout for import asset
            this.$scope.forms = {};
            this.$scope.footerButtons = [
                {'name': this.$scope.isNew ? 'Add' : 'Update', 'css':'blue', 'callback': this.$scope.save},
                {'name':'Cancel', 'css':'grey', 'callback': this.$scope.close}
            ];

            this.$scope.$watch('forms.editForm.$invalid', () => {
                this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
            });

        }

    }
}
