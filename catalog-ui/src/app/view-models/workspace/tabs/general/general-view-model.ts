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
import {Dictionary} from "lodash";
import {
    ComponentFactory,
    ComponentState,
    ComponentType,
    DEFAULT_ICON,
    EVENTS,
    instantiationType,
    ModalsHandler,
    ResourceType,
    ValidationUtils,
    FileUtils,
    ServiceCsarReader
} from "app/utils";
import {EventListenerService, ProgressService} from "app/services";
import {CacheService, ElementService, ModelService, ImportVSPService, OnboardingService} from "app/services-ng2";
import {Component, IAppConfigurtaion, ICsarComponent, IMainCategory, IMetadataKey, ISubCategory, IValidate, Resource, Service} from "app/models";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {CATEGORY_SERVICE_METADATA_KEYS, PREVIOUS_CSAR_COMPONENT, DEFAULT_MODEL_NAME} from "../../../../utils/constants";
import {Observable} from "rxjs";
import {Model} from "../../../../models/model";

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

export class componentModel {
    selectedModel:string;
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
    componentModel:componentModel;
    instantiationTypes:Array<instantiationType>;
    isHiddenCategorySelected: boolean;
    isModelRequired: boolean;

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
    onModelChange():void;
    onBaseTypeChange():void;
    openOnBoardingModal():void;
    initCategories():void;
    initEnvironmentContext():void;
    initInstantiationTypes():void;
    initBaseTypes():void;
    onInstantiationTypeChange():void;
    updateIcon():void;
    possibleToUpdateIcon():boolean;
    initModel():void;
    isVspImport(): boolean;
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
        'FileUtils',
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
        'ElementService',
        'ModelService',
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
                private FileUtils: FileUtils,
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
                private elementService: ElementService,
                private modelService: ModelService,
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
                    if (onboardCsarFilesMap && onboardCsarFilesMap[csarUUID]) {
                        this.$scope.importedToscaBrowseFileText = onboardCsarFilesMap[csarUUID][csarVersion];
                    }
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

        this.$scope.browseFileLabel = (this.$scope.component.isResource() && ((<Resource>this.$scope.component).resourceType === ResourceType.VF || (<Resource>this.$scope.component).resourceType === 'SRVC')) ||  this.$scope.component.isService() ? 'Upload File:' : 'Upload VFC:';
        this.$scope.progressService = this.progressService;
        this.$scope.componentCategories = new componentCategories();
        this.$scope.componentCategories.selectedCategory = this.$scope.component.selectedCategory;

        // Init UIModel
        this.$scope.component.tags = _.without(this.$scope.component.tags, this.$scope.component.name);

        // Init categories
        this.$scope.initCategories();

        // Init Environment Context
        this.$scope.initEnvironmentContext();

        // Init Models
        this.$scope.initModel();

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
            let service: Service = <Service>this.$scope.component;
            console.log(service.name + ": " + service.csarUUID);
            if (service.importedFile) {
                this.$scope.isShowFileBrowse = true;
                (<Service>this.$scope.component).ecompGeneratedNaming = true;
                let blob = this.FileUtils.base64toBlob(service.importedFile.base64, "zip");
                new ServiceCsarReader().read(blob).then((serviceCsar) => {
                    serviceCsar.serviceMetadata.contactId = this.cacheService.get("user").userId;
                    (<Service>this.$scope.component).setComponentMetadata(serviceCsar.serviceMetadata);
                    (<Service>this.$scope.component).model = serviceCsar.serviceMetadata.model;
                    this.$scope.onModelChange();
                    this.$scope.componentCategories.selectedCategory = serviceCsar.serviceMetadata.selectedCategory;
                    this.$scope.onCategoryChange();
                    serviceCsar.extraServiceMetadata.forEach((value: string, key: string) => {
                        if(this.getMetadataKey(key)) {
                            (<Service>this.$scope.component).categorySpecificMetadata[key] = value;
                        }
                    });
                    (<Service>this.$scope.component).derivedFromGenericType = serviceCsar.substitutionNodeType;
                    this.$scope.onBaseTypeChange();
                });
            }
            if (this.$scope.isEditMode() && service.serviceType == 'Service' && !service.csarUUID) {
                this.$scope.isShowFileBrowse = true;
            }
            // Init Instantiation types
            this.$scope.initInstantiationTypes();
            this.$scope.initBaseTypes();
        }

        if (this.cacheService.get(PREVIOUS_CSAR_COMPONENT)) { //keep the old component in the cache until checkout, so we dont need to pass it around
            this.$scope.setOriginComponent(this.cacheService.get(PREVIOUS_CSAR_COMPONENT));
            this.cacheService.remove(PREVIOUS_CSAR_COMPONENT);
        }

        if (this.$stateParams.componentCsar && !this.$scope.isCreateMode()) {
            this.$scope.updateUnsavedFileFlag(true);
            this.$scope.save();
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

        this.$scope.initCategories = ():void => {
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

        this.$scope.initBaseTypes = ():void => {
            if (this.$scope.componentType === ComponentType.SERVICE && this.$scope.component && this.$scope.component.categories) {
                if (!this.$scope.component.derivedFromGenericType) {
                    this.$scope.component.derivedFromGenericVersion = undefined;
                    this.$scope.showBaseTypeVersions = false;
                    return;
                }
                let modelName = this.$scope.component.model ? this.$scope.component.model : null;
                const categoryName = this.$scope.component.categories[0].name;
                this.elementService.getCategoryBaseTypes(categoryName, modelName).subscribe((data: ListBaseTypesResponse) => {
                    this.$scope.baseTypes = []
                    this.$scope.baseTypeVersions = []
                    this.$scope.isBaseTypeRequired = data.required;
                    data.baseTypes.forEach(baseType => {
                        this.$scope.baseTypes.push(baseType.toscaResourceName);
                        if (baseType.toscaResourceName === this.$scope.component.derivedFromGenericType) {
                            baseType.versions.reverse().forEach(version => this.$scope.baseTypeVersions.push(version));
                        }
                    });
                    this.$scope.showBaseTypeVersions = true;
                })
            }
        };

        this.$scope.initModel = ():void => {
            this.$scope.isModelRequired = false;
            this.$scope.models = [];
            this.$scope.defaultModelOption = DEFAULT_MODEL_NAME;
            this.$scope.showDefaultModelOption = true;
            if (this.$scope.componentType === ComponentType.SERVICE) {
                this.filterCategoriesByModel(this.$scope.component.model);
            }
            if (this.$scope.isCreateMode() && this.$scope.isVspImport()) {
                if (this.$scope.component.componentMetadata.models) {
                    this.$scope.isModelRequired = true;
                    const modelOptions = this.$scope.component.componentMetadata.models;
                    if (modelOptions.length == 1) {
                        this.$scope.models = modelOptions;
                        this.$scope.component.model = modelOptions[0];
                        this.$scope.showDefaultModelOption = false;
                    } else {
                        this.$scope.models = modelOptions.sort();
                        this.$scope.defaultModelOption = 'Select';
                    }
                }
                return;
            }

            if (!this.$scope.isCreateMode() && this.$scope.isVspImport()){
                this.modelService.getModels().subscribe((modelsFound: Model[]) => {
                    modelsFound.sort().forEach(model => {
                        if (this.$scope.component.model != undefined) {
                            if (model.modelType == "NORMATIVE_EXTENSION") {
                                this.$scope.component.model = model.derivedFrom;
                                this.$scope.models.push(model.derivedFrom)
                            } else {
                                this.$scope.component.model = model.name;
                                this.$scope.models.push(model.name)
                            }
                        }
                    });
                });
            } else {
                this.modelService.getModelsOfType("normative").subscribe((modelsFound: Model[]) => {
                    modelsFound.sort().forEach(model => {this.$scope.models.push(model.name)});
                });
            }
        };

        this.$scope.isVspImport = (): boolean => {
            if (!this.$scope.component || !this.$scope.component.isResource()) {
                return false;
            }

            const resource = <Resource>this.$scope.component;
            return resource.isCsarComponent();
        }

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

            let subtype:string = ComponentType.RESOURCE == this.$scope.componentType ? this.$scope.component.getComponentSubType() : undefined;
            if (subtype == "SRVC") {
                subtype = "VF"
            }

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
            if (this.$scope.component.selectedCategory) {
                this.$scope.component.categories = this.convertCategoryStringToOneArray();
                this.$scope.component.icon = DEFAULT_ICON;
                if (this.$scope.component.categories[0].metadataKeys) {
                    for (let metadataKey of this.$scope.component.categories[0].metadataKeys) {
                        if (!this.$scope.component.categorySpecificMetadata[metadataKey.name]) {
                            this.$scope.component.categorySpecificMetadata[metadataKey.name] = metadataKey.defaultValue ? metadataKey.defaultValue : "";
                        }
                    }
                }
                if (this.$scope.component.categories[0].subcategories && this.$scope.component.categories[0].subcategories[0].metadataKeys) {
                    for (let metadataKey of this.$scope.component.categories[0].subcategories[0].metadataKeys) {
                        if (!this.$scope.component.categorySpecificMetadata[metadataKey.name]) {
                            this.$scope.component.categorySpecificMetadata[metadataKey.name] = metadataKey.defaultValue ? metadataKey.defaultValue : "";
                        }
                    }
                }
                if (this.$scope.componentType === ComponentType.SERVICE && this.$scope.component.categories[0]) {
                    const modelName : string = this.$scope.component.model ? this.$scope.component.model : null;
                    this.elementService.getCategoryBaseTypes(this.$scope.component.categories[0].name, modelName)
                    .subscribe((data: ListBaseTypesResponse) => {
                        if (this.$scope.isCreateMode()) {
                            this.loadBaseTypes(data);
                        } else {
                            let isValidForBaseType:boolean = data.baseTypes.some(baseType => {
                                return !this.$scope.component.derivedFromGenericType ||
                                    baseType.toscaResourceName === this.$scope.component.derivedFromGenericType;
                            });
                            this.$scope.editForm['category'].$setValidity('validForBaseType', isValidForBaseType);
                        }
                    });
                }
            } else {
                this.clearBaseTypes();
            }
        };

        this.$scope.onEcompGeneratedNamingChange = (): void => {
            if (!(this.$scope.component as Service).ecompGeneratedNaming) {
                (this.$scope.component as Service).namingPolicy = '';
            }
        };

        this.$scope.getCategoryDisplayNameOrName = (mainCategory: any): string => {
            return mainCategory.displayName ? mainCategory.displayName : mainCategory.name ;
        }

        this.$scope.onBaseTypeChange = (): void => {
            if (!this.$scope.component.derivedFromGenericType) {
                this.$scope.component.derivedFromGenericVersion = undefined;
                this.$scope.showBaseTypeVersions = false;
                return;
            }

            const modelName : string = this.$scope.component.model ? this.$scope.component.model : null;
            const categoryName = this.$scope.component.categories[0].name;
            this.elementService.getCategoryBaseTypes(categoryName, modelName).subscribe((baseTypeResponseList: ListBaseTypesResponse) => {
                this.$scope.baseTypeVersions = []
                baseTypeResponseList.baseTypes.forEach(baseType => {
                    if (baseType.toscaResourceName === this.$scope.component.derivedFromGenericType) {
                        baseType.versions.reverse().forEach(version => this.$scope.baseTypeVersions.push(version));
                        this.$scope.component.derivedFromGenericVersion = baseType.versions[0];
                    }
                });
                this.$scope.showBaseTypeVersions = true;
            });
        };

        this.$scope.onModelChange = (): void => {
            if (this.$scope.componentType === ComponentType.SERVICE && this.$scope.component && this.$scope.categories) {
                let modelName = this.$scope.component.model ? this.$scope.component.model : null;
                this.$scope.component.categories = undefined;
                this.$scope.component.selectedCategory = undefined;
                this.$scope.componentCategories.selectedCategory = undefined;
                this.filterCategoriesByModel(modelName);
                this.filterBaseTypesByModelAndCategory(modelName)
            }
        };

        this.$scope.onVendorNameChange = (oldVendorName: string): void => {
            if (this.$scope.component.icon === oldVendorName) {
                this.$scope.component.icon = DEFAULT_ICON;
            }
        };

        this.EventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE, this.$scope.reload);

        this.$scope.isMetadataKeyMandatory = (key: string): boolean => {
            let metadataKey = this.getMetadataKey(key);
            return metadataKey && metadataKey.mandatory;
        }

        this.$scope.getMetadataKeyValidValues = (key: string): string[] => {
            let metadataKey = this.getMetadataKey(key);
            if (metadataKey) {
                return metadataKey.validValues;
            }
            return [];
        }

        this.$scope.getMetadataDisplayName = (key: string): string => {
            let metadataKey = this.getMetadataKey(key);
            if (metadataKey) {
                return metadataKey.displayName ? metadataKey.displayName : metadataKey.name;
            }
            return "";
        }

        this.$scope.isMetadataKeyForComponentCategory = (key: string): boolean => {
            return this.getMetadataKey(key) != null;
        }

        this.$scope.isCategoryServiceMetadataKey = (key: string): boolean => {
            return this.isServiceMetadataKey(key);
        }

        this.$scope.isMetadataKeyForComponentCategoryService = (key: string, attribute: string): boolean => {
            let metadatakey = this.getMetadataKey(key);
            if (metadatakey && (!this.$scope.component[attribute] || !metadatakey.validValues.find(v => v === this.$scope.component[attribute]))) {
                this.$scope.component[attribute] = metadatakey.defaultValue;
            }
            return metadatakey != null;
        }
    }

    private filterCategoriesByModel(modelName:string) {
        // reload categories
        this.$scope.initCategories();
        this.$scope.categories = this.$scope.categories.filter(category =>
            !modelName ? category.models.indexOf(DEFAULT_MODEL_NAME) !== -1 : category.models !== null && category.models.indexOf(modelName) !== -1);
    }


    private filterBaseTypesByModelAndCategory(modelName:string) {
        let categories = this.$scope.component.categories;
        if (categories) {
            this.elementService.getCategoryBaseTypes(categories[0].name, modelName).subscribe((data: ListBaseTypesResponse) => {
                this.loadBaseTypes(data);
            });
            return;
        }
        this.clearBaseTypes();
    }

    private loadBaseTypes(baseTypeResponseList: ListBaseTypesResponse) {
        this.$scope.isBaseTypeRequired = baseTypeResponseList.required;
        this.$scope.baseTypes = [];
        this.$scope.baseTypeVersions = [];
        baseTypeResponseList.baseTypes.forEach(baseType => this.$scope.baseTypes.push(baseType.toscaResourceName));
        if (this.$scope.isBaseTypeRequired) {
            const baseType = baseTypeResponseList.baseTypes[0];
            baseType.versions.reverse().forEach(version => this.$scope.baseTypeVersions.push(version));
            if(!this.$scope.component.derivedFromGenericType) {
                this.$scope.component.derivedFromGenericType = baseType.toscaResourceName;
            }
            this.$scope.component.derivedFromGenericVersion = this.$scope.baseTypeVersions[0];
            this.$scope.showBaseTypeVersions = true;
            return
        }
        this.$scope.component.derivedFromGenericType = undefined;
        this.$scope.component.derivedFromGenericVersion = undefined;
        this.$scope.showBaseTypeVersions = false;
    }

    private clearBaseTypes() {
        this.$scope.isBaseTypeRequired = false;
        this.$scope.baseTypes = [];
        this.$scope.baseTypeVersions = [];
        this.$scope.component.derivedFromGenericType = undefined;
        this.$scope.component.derivedFromGenericVersion = undefined;
        this.$scope.showBaseTypeVersions = false;
    }

    private setUnsavedChanges = (hasChanges: boolean): void => {
        this.$state.current.data.unsavedChanges = hasChanges;
    }

    private getMetadataKey(key: string) : IMetadataKey {
        if (this.$scope.component.categories) {
            let metadataKey = this.getSubcategoryMetadataKey(this.$scope.component.categories, key);
            if (!metadataKey){
                return this.getCategoryMetadataKey(this.$scope.component.categories, key);
            }
            return metadataKey;
        }
        return null;
    }

    private getSubcategoryMetadataKey(categories: IMainCategory[], key: string) : IMetadataKey {
        if (categories[0].subcategories && categories[0].subcategories[0].metadataKeys && categories[0].subcategories[0].metadataKeys.some(metadataKey => metadataKey.name == key)) {
            return categories[0].subcategories[0].metadataKeys.find(metadataKey => metadataKey.name == key);
        }
        return null;
    }

    private getCategoryMetadataKey(categories: IMainCategory[], key: string) : IMetadataKey {
        if (categories[0].metadataKeys && categories[0].metadataKeys.some(metadataKey => metadataKey.name == key)) {
            return categories[0].metadataKeys.find(metadataKey => metadataKey.name == key);
        }
        return null;
    }

    private isServiceMetadataKey(key: string) : boolean {
        return CATEGORY_SERVICE_METADATA_KEYS.indexOf(key) > -1;
    }

}
