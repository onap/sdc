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
import {ModalsHandler, ValidationUtils, EVENTS, CHANGE_COMPONENT_CSAR_VERSION_FLAG, ComponentType, DEFAULT_ICON,
    ResourceType, ComponentState} from "app/utils";
import {CacheService, EventListenerService, ProgressService, OnboardingService} from "app/services";
import {IAppConfigurtaion, IValidate, IMainCategory, Resource, ISubCategory,Service, ICsarComponent} from "app/models";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {Dictionary} from "lodash";

export class Validation {
    componentNameValidationPattern:RegExp;
    contactIdValidationPattern:RegExp;
    tagValidationPattern:RegExp;
    VendorReleaseValidationPattern:RegExp;
    VendorNameValidationPattern:RegExp;
    VendorModelNumberValidationPattern:RegExp;
    commentValidationPattern:RegExp;
    projectCodeValidationPattern:RegExp;
}

export class componentCategories {//categories field bind to this obj in order to solve this bug: DE242059
    selectedCategory:string;
}

export interface IEnvironmentContext {
    defaultValue:string;
    validValues:Array<string>;
}

export interface IGeneralScope extends IWorkspaceViewModelScope {
    validation:Validation;
    editForm:ng.IFormController;
    categories:Array<IMainCategory>;
    environmentContextObj:IEnvironmentContext;
    latestCategoryId:string;
    latestVendorName:string;
    importedFileExtension:any;
    isCreate:boolean;
    isShowFileBrowse:boolean;
    isShowOnboardingSelectionBrowse:boolean;
    importedToscaBrowseFileText:string;
    importCsarProgressKey:string;
    browseFileLabel:string;
    componentCategories:componentCategories;

    onToscaFileChange():void;
    validateField(field:any):boolean;
    validateName(isInit:boolean):void;
    calculateUnique(mainCategory:string, subCategory:string):string; // Build unique string from main and sub category
    onVendorNameChange(oldVendorName:string):void;
    convertCategoryStringToOneArray(category:string, subcategory:string):Array<IMainCategory>;
    onCategoryChange():void;
    onEcompGeneratedNamingChange():void;
    openOnBoardingModal():void;
    initCategoreis():void;
    initEnvironmentContext():void;
    updateIcon():void;
    possibleToUpdateIcon():boolean;
}

export class GeneralViewModel {

    static '$inject' = [
        '$scope',
        'Sdc.Services.CacheService',
        'ComponentNameValidationPattern',
        'ContactIdValidationPattern',
        'TagValidationPattern',
        'VendorReleaseValidationPattern',
        'VendorNameValidationPattern',
        'VendorModelNumberValidationPattern',
        'CommentValidationPattern',
        'ValidationUtils',
        'sdcConfig',
        'ProjectCodeValidationPattern',
        '$state',
        'ModalsHandler',
        'EventListenerService',
        'Notification',
        'Sdc.Services.ProgressService',
        '$interval',
        '$filter',
        '$timeout',
        'Sdc.Services.OnboardingService'
    ];

    constructor(private $scope:IGeneralScope,
                private cacheService:CacheService,
                private ComponentNameValidationPattern:RegExp,
                private ContactIdValidationPattern:RegExp,
                private TagValidationPattern:RegExp,
                private VendorReleaseValidationPattern:RegExp,
                private VendorNameValidationPattern:RegExp,
                private VendorModelNumberValidationPattern:RegExp,
                private CommentValidationPattern:RegExp,
                private ValidationUtils:ValidationUtils,
                private sdcConfig:IAppConfigurtaion,
                private ProjectCodeValidationPattern:RegExp,
                private $state:ng.ui.IStateService,
                private ModalsHandler:ModalsHandler,
                private EventListenerService:EventListenerService,
                private Notification:any,
                private progressService:ProgressService,
                protected $interval:any,
                private $filter:ng.IFilterService,
                private $timeout:ng.ITimeoutService,
                private onBoardingService:OnboardingService) {

        this.initScopeValidation();
        this.initScopeMethods();
        this.initScope();
    }




    private initScopeValidation = ():void => {
        this.$scope.validation = new Validation();
        this.$scope.validation.componentNameValidationPattern = this.ComponentNameValidationPattern;
        this.$scope.validation.contactIdValidationPattern = this.ContactIdValidationPattern;
        this.$scope.validation.tagValidationPattern = this.TagValidationPattern;
        this.$scope.validation.VendorReleaseValidationPattern = this.VendorReleaseValidationPattern;
        this.$scope.validation.VendorNameValidationPattern = this.VendorNameValidationPattern;
        this.$scope.validation.VendorModelNumberValidationPattern = this.VendorModelNumberValidationPattern;
        this.$scope.validation.commentValidationPattern = this.CommentValidationPattern;
        this.$scope.validation.projectCodeValidationPattern = this.ProjectCodeValidationPattern;
    };

    private initImportedToscaBrowseFile = ():void =>{
        // Init the decision if to show onboarding
        this.$scope.isShowOnboardingSelectionBrowse = false;
        if (this.$scope.component.isResource() &&
            this.$scope.isEditMode() &&
            (<Resource>this.$scope.component).resourceType == ResourceType.VF &&
            (<Resource>this.$scope.component).csarUUID) {
            this.$scope.isShowOnboardingSelectionBrowse = true;
            let onboardCsarFilesMap:Dictionary<Dictionary<string>> = this.cacheService.get('onboardCsarFilesMap');
            // The onboardCsarFilesMap in cache contains map of [packageId]:[vsp display name for brows]
            // if the map is empty - Do request to BE
            if(onboardCsarFilesMap) {
                if (onboardCsarFilesMap[(<Resource>this.$scope.component).csarUUID]){
                    this.$scope.importedToscaBrowseFileText = onboardCsarFilesMap[(<Resource>this.$scope.component).csarUUID][(<Resource>this.$scope.component).csarVersion];
                }
            }
            if(!onboardCsarFilesMap || !this.$scope.importedToscaBrowseFileText){

                let onSuccess = (vsps:Array<ICsarComponent>): void =>{
                    onboardCsarFilesMap = {};
                    _.each(vsps, (vsp:ICsarComponent)=>{
                        onboardCsarFilesMap[vsp.packageId] = onboardCsarFilesMap[vsp.packageId] || {};
                        onboardCsarFilesMap[vsp.packageId][vsp.version] = vsp.vspName + " (" + vsp.version + ")";
                    });
                    this.cacheService.set('onboardCsarFilesMap', onboardCsarFilesMap);
                    this.$scope.importedToscaBrowseFileText = onboardCsarFilesMap[(<Resource>this.$scope.component).csarUUID][(<Resource>this.$scope.component).csarVersion];
                };

                let onError = (): void =>{
                    console.log("Error getting onboarding list");
                };

                this.onBoardingService.getOnboardingVSPs().then(onSuccess, onError);
            }
        }
    };

    private initScope = ():void => {

        // Work around to change the csar version
        if (this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
            (<Resource>this.$scope.component).csarVersion = this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
        }

        this.$scope.importCsarProgressKey = "importCsarProgressKey";
        this.$scope.browseFileLabel = this.$scope.component.isResource() && (<Resource>this.$scope.component).resourceType === ResourceType.VF ? "Upload file" : "Upload VFC";
        this.$scope.progressService = this.progressService;
        this.$scope.componentCategories = new componentCategories();
        this.$scope.componentCategories.selectedCategory = this.$scope.component.selectedCategory;

        // Init UIModel
        this.$scope.component.tags = _.without(this.$scope.component.tags, this.$scope.component.name);

        // Init categories
        this.$scope.initCategoreis();

        // Init Environment Context
        this.$scope.initEnvironmentContext();

        // Init the decision if to show file browse.
        this.$scope.isShowFileBrowse = false;
        if (this.$scope.component.isResource()) {
            let resource:Resource = <Resource>this.$scope.component;
            console.log(resource.name + ": " + resource.csarUUID);
            if (resource.importedFile) { // Component has imported file.
                this.$scope.isShowFileBrowse = true;
            }
            if (this.$scope.isEditMode() && resource.resourceType == ResourceType.VF && !resource.csarUUID) {
                this.$scope.isShowFileBrowse = true;
            }
        }

        this.initImportedToscaBrowseFile();

        //init file extensions based on the file that was imported.
        if (this.$scope.component.isResource() && (<Resource>this.$scope.component).importedFile) {
            let fileName:string = (<Resource>this.$scope.component).importedFile.filename;
            let fileExtension:string = fileName.split(".").pop();
            if (this.sdcConfig.csarFileExtension.indexOf(fileExtension.toLowerCase()) !== -1) {
                this.$scope.importedFileExtension = this.sdcConfig.csarFileExtension;
                (<Resource>this.$scope.component).importedFile.filetype = "csar";
            } else if (this.sdcConfig.toscaFileExtension.indexOf(fileExtension.toLowerCase()) !== -1) {
                (<Resource>this.$scope.component).importedFile.filetype = "yaml";
                this.$scope.importedFileExtension = this.sdcConfig.toscaFileExtension;
            }
        } else if (this.$scope.isEditMode() && (<Resource>this.$scope.component).resourceType === ResourceType.VF) {
            this.$scope.importedFileExtension = this.sdcConfig.csarFileExtension;
            //(<Resource>this.$scope.component).importedFile.filetype="csar";
        }

        this.$scope.setValidState(true);

        this.$scope.calculateUnique = (mainCategory:string, subCategory:string):string => {
            let uniqueId:string = mainCategory;
            if (subCategory) {
                uniqueId += "_#_" + subCategory; // Set the select category combobox to show the selected category.
            }
            return uniqueId;
        };

        //TODO remove this after handling contact in UI
        if (this.$scope.isCreateMode()) {
            this.$scope.component.contactId = this.cacheService.get("user").userId;
            this.$scope.originComponent.contactId = this.$scope.component.contactId;
        }

    };

    // Convert category string MainCategory_#_SubCategory to Array with one item (like the server except)
    private convertCategoryStringToOneArray = ():Array<IMainCategory> => {
        let tmp = this.$scope.component.selectedCategory.split("_#_");
        let mainCategory = tmp[0];
        let subCategory = tmp[1];

        // Find the selected category and add the relevant sub category.
        let selectedMainCategory:IMainCategory = <IMainCategory>_.find(this.$scope.categories, function (item) {
            return item["name"] === mainCategory;

        });

        let mainCategoryClone = angular.copy(selectedMainCategory);
        if (subCategory) {
            let selectedSubcategory = <ISubCategory>_.find(selectedMainCategory.subcategories, function (item) {
                return item["name"] === subCategory;
            });
            mainCategoryClone['subcategories'] = [angular.copy(selectedSubcategory)];
        }
        let tmpSelected = <IMainCategory> mainCategoryClone;

        let result:Array<IMainCategory> = [];
        result.push(tmpSelected);

        return result;
    };

    private updateComponentNameInBreadcrumbs = ():void => {
        //update breadcrum after changing name
        this.$scope.breadcrumbsModel[1].updateSelectedMenuItemText(this.$scope.component.getComponentSubType() + ': ' + this.$scope.component.name);
        this.$scope.updateMenuComponentName(this.$scope.component.name);
    };

    private initScopeMethods = ():void => {

        this.$scope.initCategoreis = ():void => {
            if (this.$scope.componentType === ComponentType.RESOURCE) {
                this.$scope.categories = this.cacheService.get('resourceCategories');

            }
            if (this.$scope.componentType === ComponentType.SERVICE) {
                this.$scope.categories = this.cacheService.get('serviceCategories');
            }
        };


        this.$scope.initEnvironmentContext = ():void => {
            if (this.$scope.componentType === ComponentType.SERVICE) {
                this.$scope.environmentContextObj = this.cacheService.get('UIConfiguration').environmentContext;
                var environmentContext:string =(<Service>this.$scope.component).environmentContext;
                var isCheckout:boolean = ComponentState.NOT_CERTIFIED_CHECKOUT === this.$scope.component.lifecycleState;
                // In creation new service OR check outing old service without environmentContext parameter - set default value
                if(this.$scope.isCreateMode() || (isCheckout && !environmentContext)){
                    (<Service>this.$scope.component).environmentContext = this.$scope.environmentContextObj.defaultValue;
                }
            }
        };

        this.$scope.validateField = (field:any):boolean => {
            if (field && field.$dirty && field.$invalid) {
                return true;
            }
            return false;
        };

        this.$scope.openOnBoardingModal = ():void => {
            let csarUUID = (<Resource>this.$scope.component).csarUUID;
            this.ModalsHandler.openOnboadrdingModal('Update', csarUUID).then(()=> {
                // OK
                this.$scope.uploadFileChangedInGeneralTab();
            }, ()=> {
                // ERROR
            });
        };

        this.$scope.updateIcon = ():void => {
            this.ModalsHandler.openUpdateIconModal(this.$scope.component).then((isDirty:boolean)=> {
                if(!this.$scope.isCreateMode()){
                    this.$state.current.data.unsavedChanges = this.$state.current.data.unsavedChanges || isDirty;
                }
            }, ()=> {
                // ERROR
            });
        };

        this.$scope.possibleToUpdateIcon = ():boolean => {
            if(this.$scope.componentCategories.selectedCategory && (!this.$scope.component.isResource() || this.$scope.component.vendorName)){
                return true;
            }else{
                return false;
            }
        }

        this.$scope.validateName = (isInit:boolean):void => {
            if (isInit === undefined) {
                isInit = false;
            }

            let name = this.$scope.component.name;
            if (!name || name === "") {
                if (this.$scope.editForm
                    && this.$scope.editForm["componentName"]
                    && this.$scope.editForm["componentName"].$error) {

                    // Clear the error name already exists
                    this.$scope.editForm["componentName"].$setValidity('nameExist', true);
                }

                return;
            }
            //?????????????????????????
            let subtype:string = ComponentType.RESOURCE == this.$scope.componentType ? this.$scope.component.getComponentSubType() : undefined;

            let onFailed = (response) => {
                //console.info('onFaild', response);
                //this.$scope.isLoading = false;
            };

            let onSuccess = (validation:IValidate) => {
                this.$scope.editForm["componentName"].$setValidity('nameExist', validation.isValid);
                if (validation.isValid) {
                    //update breadcrumb after changing name
                    this.updateComponentNameInBreadcrumbs();
                }
            };

            if (isInit) {
                // When page is init after update
                if (this.$scope.component.name !== this.$scope.originComponent.name) {
                    if (!(this.$scope.componentType === ComponentType.RESOURCE && (<Resource>this.$scope.component).csarUUID !== undefined)
                    ) {
                        this.$scope.component.validateName(name, subtype).then(onSuccess, onFailed);
                    }
                }
            } else {
                // Validating on change (has debounce)
                if (this.$scope.editForm
                    && this.$scope.editForm["componentName"]
                    && this.$scope.editForm["componentName"].$error
                    && !this.$scope.editForm["componentName"].$error.pattern
                    && (!this.$scope.originComponent.name || this.$scope.component.name.toUpperCase() !== this.$scope.originComponent.name.toUpperCase())
                ) {
                    if (!(this.$scope.componentType === ComponentType.RESOURCE && (<Resource>this.$scope.component).csarUUID !== undefined)
                    ) {
                        this.$scope.component.validateName(name, subtype).then(onSuccess, onFailed);
                    }
                } else if (this.$scope.originComponent.name && this.$scope.component.name.toUpperCase() === this.$scope.originComponent.name.toUpperCase()) {
                    // Clear the error
                    this.$scope.editForm["componentName"].$setValidity('nameExist', true);
                }
            }
        };

        this.$scope.$watchCollection('component.name', (newData:any):void => {
            this.$scope.validateName(false);
        });

        // Notify the parent if this step valid or not.
        this.$scope.$watch("editForm.$valid", (newVal, oldVal) => {
            this.$scope.setValidState(newVal);
        });

        this.$scope.$watch("editForm.$dirty", (newVal, oldVal) => {
            if (newVal !== oldVal) {
                this.$state.current.data.unsavedChanges = newVal && !this.$scope.isCreateMode();
            }
        });

        this.$scope.onCategoryChange = ():void => {
            this.$scope.component.selectedCategory = this.$scope.componentCategories.selectedCategory;
            this.$scope.component.categories = this.convertCategoryStringToOneArray();
            this.$scope.component.icon = DEFAULT_ICON;
        };

        this.$scope.onEcompGeneratedNamingChange = ():void =>{
            if(!(<Service>this.$scope.component).ecompGeneratedNaming){
                (<Service>this.$scope.component).namingPolicy = '';
            }
        };

        this.$scope.onVendorNameChange = (oldVendorName:string):void => {
            if (this.$scope.component.icon === oldVendorName) {
                this.$scope.component.icon = DEFAULT_ICON;
            }
        };
        this.EventListenerService.registerObserverCallback(EVENTS.ON_CHECKOUT, this.$scope.reload);
        this.EventListenerService.registerObserverCallback(EVENTS.ON_REVERT, ()=>{
            this.$scope.componentCategories.selectedCategory = this.$scope.originComponent.selectedCategory;
        });
    };
}
