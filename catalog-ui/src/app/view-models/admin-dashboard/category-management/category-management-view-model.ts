'use strict';
import {ModalsHandler, ValidationUtils} from "app/utils";
import {CacheService, ICategoryResource} from "app/services";
import {IAppConfigurtaion} from "app/models";
import {ComponentType} from "../../../utils/constants";

interface ICategoryManagementViewModelScope extends ng.IScope {
    SERVICE:string;
    RESOURCE:string;
    categoriesToShow:Array<ICategoryResource>;
    serviceCategories:Array<ICategoryResource>;
    resourceCategories:Array<ICategoryResource>;
    selectedCategory:ICategoryResource;
    selectedSubCategory:ICategoryResource;
    modalInstance:ng.ui.bootstrap.IModalServiceInstance;
    isLoading:boolean;
    type:string;
    namePattern:RegExp;

    selectCategory(category:ICategoryResource):void;
    selectSubCategory(subcategory:ICategoryResource):void;
    selectType(type:string):void;
    deleteCategory(category:ICategoryResource, subCategory:ICategoryResource):void;
    createCategoryModal(parentCategory:ICategoryResource):void;
}

export class CategoryManagementViewModel {
    static '$inject' = [
        '$scope',
        'sdcConfig',
        'Sdc.Services.CacheService',
        '$uibModal',
        '$filter',
        'ValidationUtils',
        'ModalsHandler'
    ];

    constructor(private $scope:ICategoryManagementViewModelScope,
                private sdcConfig:IAppConfigurtaion,
                private cacheService:CacheService,
                private $uibModal:ng.ui.bootstrap.IModalService,
                private $filter:ng.IFilterService,
                private ValidationUtils:ValidationUtils,
                private ModalsHandler:ModalsHandler) {

        this.initScope();
        this.$scope.selectType(ComponentType.SERVICE.toLocaleLowerCase());

    }

    private initScope = ():void => {
        let scope:ICategoryManagementViewModelScope = this.$scope;
        scope.SERVICE = ComponentType.SERVICE.toLocaleLowerCase();
        scope.RESOURCE = ComponentType.RESOURCE.toLocaleLowerCase();

        scope.namePattern = this.ValidationUtils.getValidationPattern('category');

        scope.selectCategory = (category:ICategoryResource) => {
            if (scope.selectedCategory !== category) {
                scope.selectedSubCategory = null;
            }
            scope.selectedCategory = category;
        };
        scope.selectSubCategory = (subcategory:ICategoryResource) => {
            scope.selectedSubCategory = subcategory;
        };
        scope.selectType = (type:string):void => {
            if (scope.type !== type) {
                scope.selectedCategory = null;
                scope.selectedSubCategory = null;
            }

            scope.type = type;
            scope.categoriesToShow = scope[type + 'Categories'];
        };

        scope.createCategoryModal = (parentCategory:ICategoryResource):void => {
            //can't create a sub category for service
            if (parentCategory && scope.type === ComponentType.SERVICE.toLowerCase()) {
                return;
            }

            let type:string = scope.type;

            let onOk = (newCategory:ICategoryResource):void => {
                if (!parentCategory) {
                    scope[type + 'Categories'].push(newCategory);
                } else {
                    if (!parentCategory.subcategories) {
                        parentCategory.subcategories = [];
                    }
                    parentCategory.subcategories.push(newCategory);
                }
            };

            let onCancel = ():void => {

            };

            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: 'src/app/view-models/admin-dashboard/add-category-modal/add-category-modal-view.html',
                controller: 'Sdc.ViewModels.AddCategoryModalViewModel',
                size: 'sdc-xsm',
                backdrop: 'static',
                scope: scope,
                resolve: {
                    parentCategory: function () {
                        return parentCategory;
                    },
                    type: function () {
                        return type;
                    }
                }
            };

            scope.modalInstance = this.$uibModal.open(modalOptions);
            scope.modalInstance.result.then(onOk, onCancel);

        };

        scope.deleteCategory = (category:ICategoryResource, subCategory:ICategoryResource):void => {

            let onOk = ():void => {

                scope.isLoading = true;
                let type:string = scope.type;

                let onError = (response):void => {
                    scope.isLoading = false;
                    console.info('onFaild', response);
                };

                let onSuccess = (response:any):void => {
                    let arr:Array<ICategoryResource>;

                    if (!subCategory) {
                        arr = this.$scope[type + 'Categories'];
                        arr.splice(arr.indexOf(category), 1);
                        if (category === scope.selectedCategory) {
                            scope.selectedCategory = null;
                            scope.selectedSubCategory = null;
                        }
                    } else {
                        arr = category.subcategories;
                        arr.splice(arr.indexOf(subCategory), 1);
                    }

                    scope.isLoading = false;
                };

                if (!subCategory) {
                    category.$delete({
                            types: type + "s",
                            categoryId: category.uniqueId
                        }
                        , onSuccess, onError);
                } else {
                    category.$deleteSubCategory({
                            types: type + "s",
                            categoryId: category.uniqueId,
                            subCategoryId: subCategory.uniqueId,
                        }
                        , onSuccess, onError);
                }
            };
            let modelType:string = subCategory ? 'sub category' : 'category';
            let title:string = this.$filter('translate')("DELETE_CATEGORY_MODAL_HEADER", "{'modelType': '" + modelType + "' }");
            let message:string = this.$filter('translate')("DELETE_CATEGORY_MODAL_CATEGORY_NAME", "{'modelType': '" + modelType + "' }");

            this.ModalsHandler.openConfirmationModal(title, message, false, 'sdc-xsm').then(onOk);
        };

        this.$scope.serviceCategories = this.cacheService.get('serviceCategories');
        this.$scope.resourceCategories = this.cacheService.get('resourceCategories');
    }
}
