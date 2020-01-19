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
import * as _ from "lodash";
import {ModalsHandler, ValidationUtils, EVENTS, CHANGE_COMPONENT_CSAR_VERSION_FLAG, ComponentType, DEFAULT_ICON,
    ResourceType, ComponentState, instantiationType, ComponentFactory} from "app/utils";
import { EventListenerService, ProgressService} from "app/services";
import {CacheService, OnboardingService, ImportVSPService} from "app/services-ng2";
import {IAppConfigurtaion, IValidate, IMainCategory, Resource, ISubCategory,Service, ICsarComponent, Component} from "app/models";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {Dictionary} from "lodash";
import { PREVIOUS_CSAR_COMPONENT } from "../../../../utils/constants";
import { Observable, Subject } from "rxjs";


export class Validation {
    componentNameValidationPattern:RegExp;
    contactIdValidationPattern:RegExp;
    tagValidationPattern:RegExp;
    VendorReleaseValidationPattern:RegExp;
    VendorNameValidationPattern:RegExp;
    VendorModelNumberValidationPattern:RegExp;
    commentValidationPattern:RegExp;
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
    importCsarProProgressKey:string;
    browseFileLabel:string;
    componentCategories:componentCategories;
    instantiationTypes:Array<instantiationType>;
    isHiddenCategorySelected: boolean;

    save():Promise<any>;
    revert():void;
    onImportFileChange():void;
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
    initInstantiationTypes():void;
    onInstantiationTypeChange():void;
    updateIcon():void;
    possibleToUpdateIcon():boolean;
}

// tslint:disable-next-line:max-classes-per-file
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
        '$state',
        'ModalsHandler',
        'EventListenerService',
        'Notification',
        'Sdc.Services.ProgressService',
        '$interval',
        '$filter',
        '$timeout',
        'OnboardingService',
        'ComponentFactory',
        'ImportVSPService',
        '$stateParams'
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
                private $state:ng.ui.IStateService,
                private ModalsHandler:ModalsHandler,
                private EventListenerService:EventListenerService,
                private Notification:any,
                private progressService:ProgressService,
                protected $interval:any,
                private $filter:ng.IFilterService,
                private $timeout:ng.ITimeoutService,
                private onBoardingService: OnboardingService,
                private ComponentFactory:ComponentFactory,
                private importVSPService: ImportVSPService,
                private $stateParams: any) {

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
    };

    private loadOnboardingFileCache = (): Observable<Dictionary<Dictionary<string>>> => {
        let onboardCsarFilesMap:Dictionary<Dictionary<string>>;
        let onSuccess = (vsps:Array<ICsarComponent>) => {
            onboardCsarFilesMap = {};
            _.each(vsps, (vsp:ICsarComponent)=>{
                onboardCsarFilesMap[vsp.packageId] = onboardCsarFilesMap[vsp.packageId] || {};
                onboardCsarFilesMap[vsp.packageId][vsp.version] = vsp.vspName + " (" + vsp.version + ")";
            });
            this.cacheService.set('onboardCsarFilesMap', onboardCsarFilesMap);
            return onboardCsarFilesMap;
        };
        let onError = (): void =>{
            console.log("Error getting onboarding list");
        };
        return this.onBoardingService.getOnboardingVSPs().map(onSuccess, onError);
    };

    private setImportedFileText = ():void => {

        if(!this.$scope.isShowOnboardingSelectionBrowse) return;

        //these variables makes it easier to read this logic
        let csarUUID:string = (<Resource>this.$scope.component).csarUUID;
        let csarVersion:string = (<Resource>this.$scope.component).csarVersion;

        let onboardCsarFilesMap:Dictionary<Dictionary<string>> = this.cacheService.get('onboardCsarFilesMap');
        let assignFileName = ():void => {
            if(this.$scope.component.vspArchived){
                this.$scope.importedToscaBrowseFileText = 'VSP is archived';
            } else {
                if(this.$stateParams.componentCsar && this.$scope.component.lifecycleState === 'NOT_CERTIFIED_CHECKIN' && !this.$scope.isCreateMode()) {
                    this.$scope.importedToscaBrowseFileText = this.$scope.originComponent.name + ' (' + (this.$scope.originComponent as Resource).csarVersion + ')';
                } else {
                    this.$scope.importedToscaBrowseFileText = onboardCsarFilesMap[csarUUID][csarVersion];
                }
            }
        }


        if(this.$scope.component.vspArchived || (onboardCsarFilesMap && onboardCsarFilesMap[csarUUID] && onboardCsarFilesMap[csarUUID][csarVersion])){ //check that the file name is already in cache
            assignFileName();
        } else {
            this.loadOnboardingFileCache().subscribe((onboardingFiles) => {
                onboardCsarFilesMap = onboardingFiles;
                this.cacheService.set('onboardCsarFilesMap', onboardingFiles);
                assignFileName();
            }, ()=> {});
        }

    }

    isCreateModeAvailable(verifyObj:string): boolean {
        var isCheckout:boolean = ComponentState.NOT_CERTIFIED_CHECKOUT === this.$scope.component.lifecycleState;
        return this.$scope.isCreateMode() || (isCheckout && !verifyObj)
    }

    private initScope = ():void => {

        this.$scope.importCsarProgressKey = "importCsarProgressKey";

        this.$scope.browseFileLabel = this.$scope.component.isResource() && (<Resource>this.$scope.component).resourceType === ResourceType.VF ? 'VSP' : 'Upload VFC';
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
            if (resource.resourceType === ResourceType.VF && !resource.csarUUID) {
                this.$scope.isShowFileBrowse = true;
            }
        } else if(this.$scope.component.isService()){
            // Init Instantiation types
            this.$scope.initInstantiationTypes();
        }

        if (this.cacheService.get(PREVIOUS_CSAR_COMPONENT)) { //keep the old component in the cache until checkout, so we dont need to pass it around
            this.$scope.setOriginComponent(this.cacheService.get(PREVIOUS_CSAR_COMPONENT));
            this.cacheService.remove(PREVIOUS_CSAR_COMPONENT);
        }

        if (this.$stateParams.componentCsar && !this.$scope.isCreateMode()) {
            this.$scope.updateUnsavedFileFlag(true);
            // We are coming from update VSP modal we need to automatically checkout (if needed) and save the VF
            if (this.$scope.component.lifecycleState !== ComponentState.NOT_CERTIFIED_CHECKOUT) {
                // Checkout is needed after that a save will be invoked in workspace-view.handleLifeCycleStateChange
                this.EventListenerService.notifyObservers(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE, 'checkOut');
                // if(this.$scope.component.lifecycleState !== 'NOT_CERTIFIED_CHECKIN') {
                //     (<Resource>this.$scope.component).csarVersion = this.$stateParams.componentCsar.csarVersion;
                // }
            } else {
                this.$scope.save();
            }
        }


        if (this.$scope.component.isResource() &&
            (this.$scope.component as Resource).resourceType === ResourceType.VF ||
                (this.$scope.component as Resource).resourceType === ResourceType.PNF && (this.$scope.component as Resource).csarUUID) {
            this.$scope.isShowOnboardingSelectionBrowse = true;
            this.setImportedFileText();
        } else {
            this.$scope.isShowOnboardingSelectionBrowse = false;
        }


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
            this.$scope.restoreFile = angular.copy((<Resource>this.$scope.originComponent).importedFile); //create backup
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


        this.$scope.$on('$destroy', () => {
            this.EventListenerService.unRegisterObserver(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE);
            this.EventListenerService.unRegisterObserver(EVENTS.ON_LIFECYCLE_CHANGE);
        });

    };

    // Convert category string MainCategory_#_SubCategory to Array with one item (like the server except)
    private convertCategoryStringToOneArray = ():IMainCategory[] => {
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

        let result:IMainCategory[] = [];
        result.push(tmpSelected);

        return result;
    };

    private updateComponentNameInBreadcrumbs = ():void => {
        // update breadcrum after changing name
        this.$scope.breadcrumbsModel[1].updateSelectedMenuItemText(this.$scope.component.getComponentSubType() + ': ' + this.$scope.component.name);
        this.$scope.updateMenuComponentName(this.$scope.component.name);
    };

    //Find if a category is applicable for External API or not
    private isHiddenCategory = (category: string) => {
        let items: Array<any> = new Array<any>();
        items = this.$scope.sdcMenu.component_workspace_menu_option[this.$scope.component.getComponentSubType()];
        for(let index = 0; index < items.length; ++index) {
            if ((items[index].hiddenCategories && items[index].hiddenCategories.indexOf(category) > -1)) {
                return true;
            }
        }
        return false;
    };

    private filteredCategories = () => {
        let tempCategories: Array<IMainCategory> = new Array<IMainCategory>();
        this.$scope.categories.forEach((category) => {
            if (!this.isHiddenCategory(category.name)
                && this.$scope.isCreateMode()
            ) {
                tempCategories.push(category);
            } else if ((ComponentState.NOT_CERTIFIED_CHECKOUT === this.$scope.component.lifecycleState)
                && !this.isHiddenCategory(this.$scope.component.selectedCategory)
                && !this.isHiddenCategory(category.name)
            ) {
                tempCategories.push(category);
            } else if ((ComponentState.NOT_CERTIFIED_CHECKOUT === this.$scope.component.lifecycleState)
                && this.isHiddenCategory(this.$scope.component.selectedCategory)) {
                tempCategories.push(category);
            }
        });

        return tempCategories;
    };    
   
    private initScopeMethods = ():void => {

        this.$scope.initCategoreis = ():void => {
            if (this.$scope.componentType === ComponentType.RESOURCE) {
                this.$scope.categories = this.cacheService.get('resourceCategories');

            }
            if (this.$scope.componentType === ComponentType.SERVICE) {
                this.$scope.categories = this.cacheService.get('serviceCategories');

                //Remove categories from dropdown applicable for External API
                if (this.$scope.isCreateMode() || (ComponentState.NOT_CERTIFIED_CHECKOUT === this.$scope.component.lifecycleState)) {
                    this.$scope.categories = this.filteredCategories();
                    //Flag to disbale category if service is created through External API
                    this.$scope.isHiddenCategorySelected = this.isHiddenCategory(this.$scope.component.selectedCategory);
                }
                
            }
        };

        this.$scope.initInstantiationTypes = ():void => {
            if (this.$scope.componentType === ComponentType.SERVICE) {
                this.$scope.instantiationTypes = new Array();
                this.$scope.instantiationTypes.push(instantiationType.A_LA_CARTE);
                this.$scope.instantiationTypes.push(instantiationType.MACRO);
                var instantiationTypeField:string =(<Service>this.$scope.component).instantiationType;
                if (instantiationTypeField === ""){
                    this.$scope.instantiationTypes.push("");
                }
                else if (this.isCreateModeAvailable(instantiationTypeField)) {
                    (<Service>this.$scope.component).instantiationType = instantiationType.A_LA_CARTE;

                }
            }
        };

        this.$scope.initEnvironmentContext = ():void => {
            if (this.$scope.componentType === ComponentType.SERVICE) {
                this.$scope.environmentContextObj = this.cacheService.get('UIConfiguration').environmentContext;
                var environmentContext:string =(<Service>this.$scope.component).environmentContext;
                // In creation new service OR check outing old service without environmentContext parameter - set default value
                if(this.isCreateModeAvailable(environmentContext)){
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
            if(this.$scope.component.vspArchived) return;
            let csarUUID = (<Resource>this.$scope.component).csarUUID;
            let csarVersion = (<Resource>this.$scope.component).csarVersion;
            this.importVSPService.openOnboardingModal(csarUUID, csarVersion).subscribe((result) => {
                this.ComponentFactory.getComponentWithMetadataFromServer(result.type.toUpperCase(), result.previousComponent.uniqueId).then(
                    (component:Component)=> {
                    if (result.componentCsar && component.isResource()){
                        this.cacheService.set(PREVIOUS_CSAR_COMPONENT, angular.copy(component));
                        component = this.ComponentFactory.updateComponentFromCsar(result.componentCsar, <Resource>component);
                    }
                    this.$scope.setComponent(component);
                    this.$scope.save();
                    this.setImportedFileText();
                }, ()=> {
                    // ERROR
                });
            })
        };

        this.$scope.updateIcon = ():void => {
            this.ModalsHandler.openUpdateIconModal(this.$scope.component).then((isDirty:boolean)=> {
                if(isDirty && !this.$scope.isCreateMode()){
                    this.setUnsavedChanges(true);
                }
            }, ()=> {
                // ERROR
            });
        };

        this.$scope.possibleToUpdateIcon = ():boolean => {
            if(this.$scope.componentCategories.selectedCategory && (!this.$scope.component.isResource() || this.$scope.component.vendorName) && !this.$scope.component.isAlreadyCertified()){
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
            const subtype:string = ComponentType.RESOURCE == this.$scope.componentType ? this.$scope.component.getComponentSubType() : undefined;

            const onFailed = (response) => {
                // console.info('onFaild', response);
                // this.$scope.isLoading = false;
            };

            const onSuccess = (validation:IValidate) => {
                this.$scope.editForm['componentName'].$setValidity('nameExist', validation.isValid);
                if (validation.isValid) {
                    // update breadcrumb after changing name
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
                    if (!(this.$scope.componentType === ComponentType.RESOURCE && (this.$scope.component as Resource).csarUUID !== undefined)
                    ) {
                        this.$scope.component.validateName(name, subtype).then(onSuccess, onFailed);
                    }
                } else if (this.$scope.editForm && this.$scope.originComponent.name && this.$scope.component.name.toUpperCase() === this.$scope.originComponent.name.toUpperCase()) {
                    // Clear the error
                    this.$scope.editForm['componentName'].$setValidity('nameExist', true);
                }
            }
        };


        this.EventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE, (nextState) => {
            if (this.$state.current.data.unsavedChanges && this.$scope.isValidForm) {
                this.$scope.save().then(() => {
                    this.$scope.handleChangeLifecycleState(nextState);
                }, () => {
                    console.error('Save failed, unable to change lifecycle state to ' + nextState);
                });
            } else if(!this.$scope.isValidForm){
                console.error('Form is not valid');
            } else {
                let newCsarVersion:string;
                if(this.$scope.unsavedFile) {
                    newCsarVersion = (this.$scope.component as Resource).csarVersion;
                }
                if(this.$stateParams.componentCsar && !this.$scope.isCreateMode()) {
                    const onError = (): void => {
                        if (this.$scope.component.lifecycleState === 'NOT_CERTIFIED_CHECKIN') {
                            this.$scope.revert();
                        }
                    };
                    this.$scope.handleChangeLifecycleState(nextState, newCsarVersion, onError);

                } else {
                    this.$scope.handleChangeLifecycleState(nextState, newCsarVersion);
                }
            }
        });

        this.$scope.revert = ():void => {
            // in state of import file leave the file in place

            this.$scope.setComponent(this.ComponentFactory.createComponent(this.$scope.originComponent));

            if (this.$scope.component.isResource() && this.$scope.restoreFile) {
                (this.$scope.component as Resource).importedFile = angular.copy(this.$scope.restoreFile);
            }

            this.setImportedFileText();
            this.$scope.updateBreadcrumbs(this.$scope.component); // update on workspace

            this.$scope.componentCategories.selectedCategory = this.$scope.originComponent.selectedCategory;
            this.setUnsavedChanges(false);
            this.$scope.updateUnsavedFileFlag(false);
            this.$scope.editForm.$setPristine();
        };

        this.$scope.onImportFileChange = () => {

            if( !this.$scope.restoreFile && this.$scope.editForm.fileElement.value && this.$scope.editForm.fileElement.value.filename || // if file started empty but we have added a new one
                this.$scope.restoreFile && !angular.equals(this.$scope.restoreFile, this.$scope.editForm.fileElement.value)){ // or file was swapped for a new one
                this.$scope.updateUnsavedFileFlag(true);
            } else {
                this.$scope.updateUnsavedFileFlag(false);
                this.$scope.editForm.fileElement.$setPristine();
            }
        };

        this.$scope.$watchCollection('component.name', (newData: any): void => {
            this.$scope.validateName(false);
        });

        // Notify the parent if this step valid or not.
        this.$scope.$watch('editForm.$valid', (newVal, oldVal) => {
            this.$scope.setValidState(newVal);
        });

        this.$scope.$watch('editForm.$dirty', (newVal, oldVal) => {
            if (newVal && !this.$scope.isCreateMode()) {
                this.setUnsavedChanges(true);
            }

        });

        this.$scope.onCategoryChange = (): void => {
            this.$scope.component.selectedCategory = this.$scope.componentCategories.selectedCategory;
            this.$scope.component.categories = this.convertCategoryStringToOneArray();
            this.$scope.component.icon = DEFAULT_ICON;
        };

        this.$scope.onEcompGeneratedNamingChange = (): void => {
            if (!(this.$scope.component as Service).ecompGeneratedNaming) {
                (this.$scope.component as Service).namingPolicy = '';
            }
        };

        this.$scope.onVendorNameChange = (oldVendorName: string): void => {
            if (this.$scope.component.icon === oldVendorName) {
                this.$scope.component.icon = DEFAULT_ICON;
            }
        };
        this.EventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE, this.$scope.reload);

    }

    private setUnsavedChanges = (hasChanges: boolean): void => {
        this.$state.current.data.unsavedChanges = hasChanges;
    }

}

