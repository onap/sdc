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

    export interface IEditAttributeModel {
        attribute: Models.AttributeModel;
        types: Array<string>;
        simpleTypes: Array<string>;
    }

    interface IAttributeFormViewModelScope extends ng.IScope {
        $$childTail: any;
        forms:any;
        editForm:ng.IFormController;
        footerButtons: Array<any>;
        isService: boolean;
        editAttributeModel: IEditAttributeModel;
        modalInstanceAttribute:ng.ui.bootstrap.IModalServiceInstance;
        isNew: boolean;
        listRegex: Sdc.Utils.IMapRegex;
        mapRegex: Sdc.Utils.IMapRegex;
        propertyNameValidationPattern: RegExp;
        commentValidationPattern: RegExp;
        isLoading: boolean;
        validationPattern: RegExp;

        save():void;
        close(): void;
        onTypeChange():void;
        onValueChange(): void;
        isAttributeValueOwner():boolean;
        validateIntRange(value:string):boolean;
        validateUniqueKeys(viewValue:string):boolean;
        getValidationTranslate(): string;
        showSchema(): boolean;
        isSchemaEditable(): boolean;
        validateName():void;
    }

    export class AttributeFormViewModel {

        static '$inject' = [
            '$scope',
            '$modalInstance',
            'attribute',
            'ValidationUtils',
            'CommentValidationPattern',
            'PropertyNameValidationPattern',
            'component'
        ];

        private formState: Sdc.Utils.Constants.FormState;


        constructor(private $scope:IAttributeFormViewModelScope,
                    private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                    private attribute: Models.AttributeModel,
                    private ValidationUtils:Sdc.Utils.ValidationUtils,
                    private CommentValidationPattern:RegExp,
                    private PropertyNameValidationPattern: RegExp,
                    private component: Models.Components.Component) {
            this.formState = angular.isDefined(attribute.name) ?  Utils.Constants.FormState.UPDATE :  Utils.Constants.FormState.CREATE;
            this.initScope();
        }

        private initResource = ():void => {
            this.$scope.editAttributeModel.attribute = new Sdc.Models.AttributeModel(this.attribute);
            if (this.$scope.editAttributeModel.types.indexOf(this.attribute.type) === -1) {//attribute defaulte type is string too?
                this.attribute.type = "string";
            }
        };

        private initEditAttributeModel = ():void => {
            this.$scope.editAttributeModel = {
                attribute: null,
                types: ['integer', 'string', 'float', 'boolean', 'list', 'map'],
                simpleTypes: ['integer', 'string', 'float', 'boolean']
            };

            this.initResource();
        };

        private initScope = ():void => {

            //scope attributes
            this.$scope.forms = {};
            this.$scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;
            this.$scope.commentValidationPattern = this.CommentValidationPattern;

            this.$scope.modalInstanceAttribute = this.$modalInstance;
            this.$scope.listRegex = this.ValidationUtils.getPropertyListPatterns();
            this.$scope.mapRegex = this.ValidationUtils.getPropertyMapPatterns();

            this.$scope.isNew = (this.formState === Utils.Constants.FormState.CREATE);
            this.$scope.isLoading = false;

            this.initEditAttributeModel();
            this.setValidationPattern();

            //scope methods
            this.$scope.save = ():void => {
                if(!this.$scope.forms.editForm.$invalid){
                    let attribute:Models.AttributeModel = this.$scope.editAttributeModel.attribute;
                    this.$scope.editAttributeModel.attribute.description = this.ValidationUtils.stripAndSanitize(this.$scope.editAttributeModel.attribute.description);
                    ////if read only - just closes the modal
                    if (this.$scope.editAttributeModel.attribute.readonly && !this.$scope.isAttributeValueOwner()) {
                        this.$modalInstance.close();
                        return;
                    }
                    this.$scope.isLoading = true;
                    let onAttributeFaild = (response):void => {
                        console.info('onFaild', response);
                        this.$scope.isLoading = false;
                    };

                    let onAttributeSuccess = (attributeFromBE:Models.AttributeModel):void => {
                        console.info('onAttributeResourceSuccess : ', attributeFromBE);
                        this.$scope.isLoading = false;
                        this.$modalInstance.close();
                    };

                    //in case we have uniqueId we call update method
                    if (this.$scope.isAttributeValueOwner()) {
                        this.component.updateInstanceAttribute(attribute).then(onAttributeSuccess, onAttributeFaild);
                    } else {
                        this.component.addOrUpdateAttribute(attribute).then(onAttributeSuccess, onAttributeFaild);
                    }
                }
            };

            this.$scope.close = ():void => {
                this.$modalInstance.close();
            };

            this.$scope.validateName = ():void => {
                let existsAttr: Models.AttributeModel = _.find(this.component.attributes, (attribute:Models.AttributeModel) => {
                    return attribute.name === this.$scope.editAttributeModel.attribute.name;
                });
                if(existsAttr){
                    this.$scope.forms.editForm["attributeName"].$setValidity('nameExist', false);
                }else{
                    this.$scope.forms.editForm["attributeName"].$setValidity('nameExist', true);
                }

            };

            this.$scope.onTypeChange = ():void => {
                this.$scope.editAttributeModel.attribute.value = '';
                this.$scope.editAttributeModel.attribute.defaultValue = '';
                this.setValidationPattern();
            };

            this.$scope.isAttributeValueOwner = ():boolean=> {
                return this.component.isService() || !!this.component.selectedInstance;
            };

            this.$scope.onValueChange = ():void => {
                if (!this.$scope.editAttributeModel.attribute.value) {
                    if (this.$scope.isAttributeValueOwner()) {
                        this.$scope.editAttributeModel.attribute.value = this.$scope.editAttributeModel.attribute.defaultValue;
                    }
                }
            };


            this.$scope.validateUniqueKeys = (viewValue:string) : boolean => {
                if(this.$scope.editAttributeModel.attribute.type === 'map') {
                    return this.ValidationUtils.validateUniqueKeys(viewValue);
                }
                else {
                    return true; //always valid if not a map
                }
            };

            this.$scope.validateIntRange = (value:string):boolean => {
                return !value || this.ValidationUtils.validateIntRange(value);
            };

            this.$scope.isSchemaEditable = () :boolean => {
                let schemaType=this.$scope.editAttributeModel.attribute.schema.property.type;
                return this.$scope.editAttributeModel.simpleTypes.indexOf(schemaType) > -1||!schemaType;
            };

            this.$scope.showSchema = () :boolean => {
                return ['list', 'map'].indexOf(this.$scope.editAttributeModel.attribute.type) > -1;
            };

            this.$scope.getValidationTranslate = () : string => {
                let result = "ATTRIBUTE_EDIT_PATTERN";
                if (this.$scope.showSchema()) {

                    result = "ATTRIBUTE_EDIT_" + this.$scope.editAttributeModel.attribute.type.toUpperCase();

                    if(this.$scope.editAttributeModel.attribute.schema.property.type === Utils.Constants.PROPERTY_TYPES.STRING) {
                        result += "_STRING";
                    }else if(this.$scope.editAttributeModel.attribute.schema.property.type === Utils.Constants.PROPERTY_TYPES.BOOLEAN) {
                        result += "_BOOLEAN";
                    } else {
                        result += "_GENERIC";
                    }
                }

                return result;
            };

            // Add the done button at the footer.
            this.$scope.footerButtons = [
                {'name': 'Done', 'css':'blue', 'callback': this.$scope.save},
                {'name':'Cancel', 'css':'grey', 'callback': this.$scope.close}
            ];

            this.$scope.$watchCollection("forms.editForm.$invalid", (newVal, oldVal) => {
                this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
            });

        }


        private setValidationPattern = ():void => {

            if(this.$scope.editAttributeModel.attribute.type === 'list') {
                this.$scope.validationPattern = this.$scope.listRegex[this.$scope.editAttributeModel.attribute.schema.property.type];
            }
            else if(this.$scope.editAttributeModel.attribute.type === 'map') {
                this.$scope.validationPattern = this.$scope.mapRegex[this.$scope.editAttributeModel.attribute.schema.property.type];
            }
            else{
                this.$scope.validationPattern = this.ValidationUtils.getValidationPattern(this.$scope.editAttributeModel.attribute.type);
            }

        };


    }
}
