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
    import ISubCategory = Sdc.Models.ISubCategory;
    import IMainCategory = Sdc.Models.IMainCategory;
    'use strict';

    /*
    * TODO: The template (derived from is not necessary here).
    * Need to delete it from all remarks.
    * */

    export class UIModel {
        tosca:Sdc.Directives.FileUploadModel;
        name:string;
        description:string;
        vendorName:string;
        vendorRelease:string;
        category:string;
        tags:Array<string>;
        userId:string;
        icon:string;
        projectCode:string;
        fullName:string;
        isAlreadyCertified:boolean;
    }

    export class Validation {
        validationPattern: RegExp;
        contactIdValidationPattern: RegExp;
        tagValidationPattern: RegExp;
        vendorValidationPattern: RegExp;
        commentValidationPattern: RegExp;
        projectCodeValidationPattern: RegExp;
    }

    export interface IGeneralStepScope extends IWizardCreationStepScope {
        model:UIModel;
        validation:Validation;
        editForm:ng.IFormController;
        component: Models.Components.Component;
        categories: Array<IMainCategory>;
        latestComponentName:string;
        latestCategoryId: string;
        latestVendorName: string;
        isNew:boolean;
        toscaFileExtensions:any;
        isCreate:boolean;

        onToscaFileChange():void
        validateField(field:any):boolean;
        validateName(isInit:boolean): void;
        calculateUnique(mainCategory:string, subCategory:string):string; // Build unique string from main and sub category
        calculatedTagsMaxLength():number;
        setIconToDefault():void;
        onVendorNameChange(oldVendorName: string): void;
    }

    export class GeneralStepViewModel implements IWizardCreationStep {

        static '$inject' = [
            '$scope',
            'Sdc.Services.CacheService',
            'ValidationPattern',
            'ContactIdValidationPattern',
            'TagValidationPattern',
            'VendorValidationPattern',
            'CommentValidationPattern',
            'ValidationUtils',
            'sdcConfig',
            'ComponentFactory',
            'ProjectCodeValidationPattern'
        ];

        constructor(private $scope:IGeneralStepScope,
                    private cacheService:Services.CacheService,
                    private ValidationPattern:RegExp,
                    private ContactIdValidationPattern:RegExp,
                    private TagValidationPattern:RegExp,
                    private VendorValidationPattern:RegExp,
                    private CommentValidationPattern: RegExp,
                    private ValidationUtils: Sdc.Utils.ValidationUtils,
                    private sdcConfig:Models.IAppConfigurtaion,
                    private ComponentFactory: Sdc.Utils.ComponentFactory,
                    private ProjectCodeValidationPattern:RegExp
            ) {

            this.$scope.registerChild(this);
            this.initScopeValidation();
            this.initScopeMethods();
            this.initScope();
            this.$scope.isCreate = this.$scope.data.importFile === undefined;
        }

        private initScopeValidation = (): void => {
            this.$scope.validation = new Validation();
            this.$scope.validation.validationPattern = this.ValidationPattern;
            this.$scope.validation.contactIdValidationPattern = this.ContactIdValidationPattern;
            this.$scope.validation.tagValidationPattern = this.TagValidationPattern;
            this.$scope.validation.vendorValidationPattern = this.VendorValidationPattern;
            this.$scope.validation.commentValidationPattern = this.CommentValidationPattern;
            this.$scope.validation.projectCodeValidationPattern = this.ProjectCodeValidationPattern;
        };

        private initScope = ():void => {

            // Init UIModel
            this.$scope.model = new UIModel();

            // Init categories
            if(this.$scope.data.componentType === Utils.Constants.ComponentType.RESOURCE){
                this.$scope.categories = this.cacheService.get('resourceCategories');
            }
            if (this.$scope.data.componentType === Utils.Constants.ComponentType.SERVICE) {
                this.$scope.categories = this.cacheService.get('serviceCategories');
            }

            this.$scope.model.category='';

            //init file extenstions
            this.$scope.toscaFileExtensions = this.sdcConfig.toscaFileExtension;

            // Init Tosca import file
            if (this.$scope.data.importFile) {
                this.$scope.model.tosca = this.$scope.data.importFile;
            }

            // Case insert or update
            this.$scope.component = this.$scope.getComponent();
            if ( this.$scope.component!==undefined){
                // Update mode

                //this.$scope.latestCategoryId = this.$scope.component[0].uniqueId;
                //this.$scope.latestVendorName = this.$scope.component.vendorName;
                this.$scope.latestComponentName = this.$scope.component.name;
                this.$scope.isNew=false;
                this.resource2ModelUi(this.$scope.component);
            } else {
                // Create mode
                this.$scope.isNew=true;
                this.$scope.model.tags=[]; // Init tags
                this.$scope.model.userId = this.cacheService.get("user").userId; // Fill user ID from logged in user
                this.$scope.model.icon = Utils.Constants.DEFAULT_ICON; // Set the default icon
                this.$scope.component =  this.ComponentFactory.createEmptyComponent(this.$scope.data.componentType);
            }
        };

        private initScopeMethods = ():void => {

            this.$scope.validateField = (field:any):boolean => {
                if (field && field.$dirty && field.$invalid){
                    return true;
                }
                return false;
            };

            this.$scope.validateName = (isInit:boolean):void => {
                if (isInit===undefined){isInit=false;}

                let name = this.$scope.model.name;
                if (!name || name===""){
                    if (this.$scope.editForm
                        && this.$scope.editForm["componentName"]
                        && this.$scope.editForm["componentName"].$error){

                        // Clear the error name already exists
                        this.$scope.editForm["componentName"].$setValidity('nameExist', true);
                    }

                    return;
                }
                let subtype:string = Utils.Constants.ComponentType.RESOURCE == this.$scope.data.componentType?
                                        this.$scope.data.importFile? 'VFC':'VF' : undefined;

                let onFailed = (response) => {
                    //console.info('onFaild', response);
                    //this.$scope.isLoading = false;
                };

                let onSuccess = (validation:Models.IValidate) => {
                    this.$scope.editForm["componentName"].$setValidity('nameExist', validation.isValid);
                };

                if (isInit){
                    // When page is init after update
                    if (this.$scope.model.name !== this.$scope.latestComponentName){
                        if(!this.$scope.component.isProduct()) {//TODO remove when backend is ready
                            this.$scope.component.validateName(name, subtype).then(onSuccess, onFailed);
                        }
                    }
                } else {
                    // Validating on change (has debounce)
                    if (this.$scope.editForm
                        && this.$scope.editForm["componentName"]
                        && this.$scope.editForm["componentName"].$error
                        && !this.$scope.editForm["componentName"].$error.pattern
                        && this.$scope.model.name !== this.$scope.latestComponentName
                    ) {
                        if(!this.$scope.component.isProduct()) { //TODO remove when backend is ready
                            this.$scope.component.validateName(name, subtype).then(onSuccess, onFailed);
                        }
                    } else if (this.$scope.model.name === this.$scope.latestComponentName) {
                        // Clear the error
                        this.$scope.editForm["componentName"].$setValidity('nameExist', true);
                    }
                }
            };

            this.$scope.calculateUnique = (mainCategory:string, subCategory:string):string => {
                let uniqueId: string = mainCategory;
                if(subCategory) {
                    uniqueId += "_#_" + subCategory; // Set the select category combobox to show the selected category.
                }
                return uniqueId;
            };

            // Notify the parent if this step valid or not.
            this.$scope.$watch("editForm.$valid", (newVal, oldVal) => {
                //console.log("editForm validation: " + newVal);
                this.$scope.setValidState(newVal);
            });

            this.$scope.setIconToDefault = ():void => {
                this.$scope.model.icon = Utils.Constants.DEFAULT_ICON;
            };

            this.$scope.onVendorNameChange = (oldVendorName: string):void => {
                if(this.$scope.component.icon === oldVendorName) {
                    this.$scope.setIconToDefault();
                }
            };
        };

        public save = (callback:Function):void => {
            this.modelUi2Resource();

            let onFailed = (response) => {
                callback(false);
            };

            let onSuccess = (component:Models.Components.Component) => {
                this.$scope.component = component;
                this.$scope.setComponent(this.$scope.component);
                this.$scope.latestComponentName = (component.name);
                callback(true);
            };

            try {
                //Send the form with attached tosca file.
                if (this.$scope.isNew===true) {
                    this.ComponentFactory.createComponentOnServer(this.$scope.component).then(onSuccess, onFailed);
                } else {
                    this.$scope.component.updateComponent().then(onSuccess, onFailed);
                }
            }catch(e){
                //console.log("ERROR: Error in updating/creating component: " + e);
                callback(false);
            }

        };

        public back = (callback:Function):void => {
            callback(true);
        }

        // Fill the resource properties object with data from UIModel
        private modelUi2Resource = ():void => {

            this.$scope.component.name = this.$scope.model.name;
            this.$scope.component.description = this.ValidationUtils.stripAndSanitize(this.$scope.model.description);
            this.$scope.component.vendorName = this.$scope.model.vendorName;
            this.$scope.component.vendorRelease = this.$scope.model.vendorRelease;
            this.$scope.component.tags = angular.copy(this.$scope.model.tags);
            this.$scope.component.tags.push(this.$scope.model.name);
            this.$scope.component.contactId = this.$scope.model.userId;
            this.$scope.component.icon = this.$scope.model.icon;

            if(this.$scope.component.isResource()) {
                (<Models.Components.Resource>this.$scope.component).resourceType = "VF";

                // Handle the tosca file
                if (this.$scope.model.tosca && this.$scope.isNew)  {
                    (<Models.Components.Resource>this.$scope.component).payloadData = this.$scope.model.tosca.base64;
                    (<Models.Components.Resource>this.$scope.component).payloadName = this.$scope.model.tosca.filename;
                }

                this.$scope.component.categories = this.convertCategoryStringToOneArray();
            }

            if(this.$scope.component.isProduct()) {
                this.$scope.component.projectCode = this.$scope.model.projectCode;
                // Handle the tosca file
                this.$scope.component.categories = undefined;
                (<Models.Components.Product>this.$scope.component).contacts = new Array<string>();
                (<Models.Components.Product>this.$scope.component).contacts.push(this.$scope.component.contactId);
                (<Models.Components.Product>this.$scope.component).fullName = this.$scope.model.fullName;
            }

            if(this.$scope.component.isService()) {
                this.$scope.component.projectCode = this.$scope.model.projectCode;
                this.$scope.component.categories = this.convertCategoryStringToOneArray();
            }
        };

        // Fill the UIModel from data from resource properties
        private resource2ModelUi = (component: Models.Components.Component):void => {
            this.$scope.model.name = component.name;
            this.$scope.model.description = component.description;
            this.$scope.model.vendorName = component.vendorName;
            this.$scope.model.vendorRelease = component.vendorRelease;
            this.$scope.model.tags = _.reject(component.tags, (item)=>{return item===component.name});
            this.$scope.model.userId = component.contactId;
            this.$scope.model.icon = component.icon;
            this.$scope.model.projectCode = component.projectCode;
            this.$scope.model.isAlreadyCertified = component.isAlreadyCertified();

            if(!this.$scope.component.isProduct()) {
                this.$scope.model.category = this.convertCategoryOneArrayToString(component.categories);
            }

            if(component.isProduct()) {
                this.$scope.model.fullName = (<Models.Components.Product>component).fullName;

            }

        };

        // Convert category string MainCategory_#_SubCategory to Array with one item (like the server except)
        private convertCategoryStringToOneArray = ():Array<Models.IMainCategory> => {
            let tmp = this.$scope.model.category.split("_#_");
            let mainCategory = tmp[0];
            let subCategory = tmp[1];

            // Find the selected category and add the relevant sub category.
            let selectedMainCategory:IMainCategory = <Models.IMainCategory>_.find(this.$scope.categories, function (item) {
                return item["name"] === mainCategory
            });
            let mainCategoryClone = jQuery.extend(true, {}, selectedMainCategory);
            if(subCategory) {
                mainCategoryClone['subcategories'] = [{
                    "name": subCategory
                }];
            }
            let tmpSelected = <Models.IMainCategory> mainCategoryClone;

            let result:Array<Models.IMainCategory> = [];
            result.push(tmpSelected);

            return result;
        };

        private convertCategoryOneArrayToString = (categories:Array<Models.IMainCategory>):string => {
            let mainCategory:string = categories[0].name;
            let subCategory:string = '';
            if(categories[0].subcategories) {
                subCategory = categories[0].subcategories[0].name;
            }
            return this.$scope.calculateUnique(mainCategory, subCategory);
        };

    }

}
