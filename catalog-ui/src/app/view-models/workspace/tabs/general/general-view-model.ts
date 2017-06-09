'use strict';
import {ModalsHandler, ValidationUtils, EVENTS, CHANGE_COMPONENT_CSAR_VERSION_FLAG, ComponentType, DEFAULT_ICON,
    ResourceType} from "app/utils";
import {CacheService, EventListenerService, ProgressService} from "app/services";
import {IAppConfigurtaion, Product, IValidate, IMainCategory, Resource, ISubCategory,Service} from "app/models";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";

export class Validation {
    componentNameValidationPattern:RegExp;
    contactIdValidationPattern:RegExp;
    tagValidationPattern:RegExp;
    vendorValidationPattern:RegExp;
    commentValidationPattern:RegExp;
    projectCodeValidationPattern:RegExp;
}

export class componentCategories {//categories field bind to this obj in order to solve this bug: DE242059
    selectedCategory:string;
}

export interface IGeneralScope extends IWorkspaceViewModelScope {
    validation:Validation;
    editForm:ng.IFormController;
    categories:Array<IMainCategory>;
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

    onToscaFileChange():void
    validateField(field:any):boolean;
    validateName(isInit:boolean):void;
    calculateUnique(mainCategory:string, subCategory:string):string; // Build unique string from main and sub category
    onVendorNameChange(oldVendorName:string):void;
    convertCategoryStringToOneArray(category:string, subcategory:string):Array<IMainCategory>;
    onCategoryChange():void;
    onEcompGeneratedNamingChange():void;
    openOnBoardingModal():void;
    initCategoreis():void;
}

export class GeneralViewModel {

    static '$inject' = [
        '$scope',
        'Sdc.Services.CacheService',
        'ComponentNameValidationPattern',
        'ContactIdValidationPattern',
        'TagValidationPattern',
        'VendorValidationPattern',
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
        '$timeout'
    ];

    constructor(private $scope:IGeneralScope,
                private cacheService:CacheService,
                private ComponentNameValidationPattern:RegExp,
                private ContactIdValidationPattern:RegExp,
                private TagValidationPattern:RegExp,
                private VendorValidationPattern:RegExp,
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
                private $timeout:ng.ITimeoutService) {

        this.initScopeValidation();
        this.initScopeMethods();
        this.initScope();
        this.$scope.updateSelectedMenuItem();
    }




    private initScopeValidation = ():void => {
        this.$scope.validation = new Validation();
        this.$scope.validation.componentNameValidationPattern = this.ComponentNameValidationPattern;
        this.$scope.validation.contactIdValidationPattern = this.ContactIdValidationPattern;
        this.$scope.validation.tagValidationPattern = this.TagValidationPattern;
        this.$scope.validation.vendorValidationPattern = this.VendorValidationPattern;
        this.$scope.validation.commentValidationPattern = this.CommentValidationPattern;
        this.$scope.validation.projectCodeValidationPattern = this.ProjectCodeValidationPattern;
    };

    private initScope = ():void => {

        // Work around to change the csar version
        if (this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
            (<Resource>this.$scope.component).csarVersion = this.cacheService.get(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
        }

        this.$scope.importedToscaBrowseFileText = this.$scope.component.name + " (" + (<Resource>this.$scope.component).csarVersion + ")";
        this.$scope.importCsarProgressKey = "importCsarProgressKey";
        this.$scope.browseFileLabel = this.$scope.component.isResource() && (<Resource>this.$scope.component).resourceType === ResourceType.VF ? "Upload file" : "Upload VFC";
        this.$scope.progressService = this.progressService;
        this.$scope.componentCategories = new componentCategories();
        this.$scope.componentCategories.selectedCategory = this.$scope.component.selectedCategory;

        // Workaround to short vendor name to 25 chars
        // Amdocs send 27 chars, and the validation pattern is 25 chars.
        if (this.$scope.component.vendorName) {
            this.$scope.component.vendorName = this.$scope.component.vendorName.substr(0, 25);
        }

        // Init UIModel
        this.$scope.component.tags = _.without(this.$scope.component.tags, this.$scope.component.name);

        // Init categories
        this.$scope.initCategoreis();

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
        ;

        // Init the decision if to show onboarding
        this.$scope.isShowOnboardingSelectionBrowse = false;
        if (this.$scope.component.isResource() &&
            this.$scope.isEditMode() &&
            (<Resource>this.$scope.component).resourceType == ResourceType.VF &&
            (<Resource>this.$scope.component).csarUUID) {
            this.$scope.isShowOnboardingSelectionBrowse = true;
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
        if (this.$scope.component.isProduct() && this.$scope.isCreateMode()) {
            (<Product>this.$scope.component).contacts = [];
            (<Product>this.$scope.component).contacts.push(this.cacheService.get("user").userId);
        } else if (this.$scope.isCreateMode()) {
            this.$scope.component.contactId = this.cacheService.get("user").userId;
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
    };
}
