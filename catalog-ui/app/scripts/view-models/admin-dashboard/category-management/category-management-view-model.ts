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

    interface ICategoryManagementViewModelScope extends ng.IScope {
        SERVICE:string;
        RESOURCE:string;
        categoriesToShow: Array<Sdc.Services.ICategoryResource>;
        serviceCategories: Array<Sdc.Services.ICategoryResource>;
        resourceCategories: Array<Sdc.Services.ICategoryResource>;
        selectedCategory: Sdc.Services.ICategoryResource;
        selectedSubCategory: Sdc.Services.ICategoryResource;
        modalInstance:ng.ui.bootstrap.IModalServiceInstance;
        isLoading:boolean;
        type:string;
        namePattern:RegExp;

        selectCategory(category:Sdc.Services.ICategoryResource) :void;
        selectSubCategory(subcategory:Sdc.Services.ICategoryResource) :void;
        selectType(type:string) :void;
        deleteCategory(category:Sdc.Services.ICategoryResource, subCategory:Sdc.Services.ICategoryResource) :void;
        createCategoryModal(parentCategory:Sdc.Services.ICategoryResource) :void;
    }

    export class CategoryManagementViewModel {
        static '$inject' = [
            '$scope',
            'sdcConfig',
            'Sdc.Services.CacheService',
            '$templateCache',
            '$modal',
            '$filter',
            'ValidationUtils',
            'ModalsHandler'
        ];

        constructor(private $scope:ICategoryManagementViewModelScope,
                    private sdcConfig:Models.IAppConfigurtaion,
                    private cacheService:Services.CacheService,
                    private $templateCache:ng.ITemplateCacheService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $filter:ng.IFilterService,
                    private ValidationUtils: Sdc.Utils.ValidationUtils,
                    private ModalsHandler: Utils.ModalsHandler
        ) {

            this.initScope();
            this.$scope.selectType(Sdc.Utils.Constants.ComponentType.SERVICE.toLocaleLowerCase());

        }

        private initScope = ():void => {
            let scope:ICategoryManagementViewModelScope = this.$scope;
            scope.SERVICE = Sdc.Utils.Constants.ComponentType.SERVICE.toLocaleLowerCase();
            scope.RESOURCE = Sdc.Utils.Constants.ComponentType.RESOURCE.toLocaleLowerCase();

            scope.namePattern = this.ValidationUtils.getValidationPattern('cssClasses');

            scope.selectCategory = (category :Sdc.Services.ICategoryResource) => {
                if(scope.selectedCategory !== category) {
                    scope.selectedSubCategory = null;
                }
                scope.selectedCategory = category;
            };
            scope.selectSubCategory = (subcategory :Sdc.Services.ICategoryResource) => {
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

            scope.createCategoryModal = (parentCategory:Sdc.Services.ICategoryResource):void => {
                //can't create a sub category for service
                if(parentCategory && scope.type === Sdc.Utils.Constants.ComponentType.SERVICE.toLowerCase()) {
                    return;
                }

                let type:string = scope.type;

                let onOk = (newCategory :Sdc.Services.ICategoryResource):void => {
                    if(!parentCategory) {
                        scope[type + 'Categories'].push(newCategory);
                    }else{
                        if(!parentCategory.subcategories) {
                            parentCategory.subcategories = [];
                        }
                        parentCategory.subcategories.push(newCategory);
                    }
                };

                let onCancel = ():void => {

                };

                let modalOptions:ng.ui.bootstrap.IModalSettings = {
                    template: this.$templateCache.get('/app/scripts/view-models/admin-dashboard/add-category-modal/add-category-modal-view.html'),
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

                scope.modalInstance = this.$modal.open(modalOptions);
                scope.modalInstance.result.then(onOk, onCancel);

            };

            scope.deleteCategory = (category: Sdc.Services.ICategoryResource, subCategory: Sdc.Services.ICategoryResource): void => {

                let onOk = ():void => {

                    scope.isLoading = true;
                    let type:string = scope.type;

                    let onError = (response):void => {
                        scope.isLoading = false;
                        console.info('onFaild', response);
                    };

                    let onSuccess = (response: any) :void => {
                        let arr:Array<Sdc.Services.ICategoryResource>;

                        if(!subCategory) {
                            arr = this.$scope[type + 'Categories'];
                            arr.splice(arr.indexOf(category), 1);
                            if(category === scope.selectedCategory) {
                                scope.selectedCategory = null;
                                scope.selectedSubCategory = null;
                            }
                        } else {
                            arr = category.subcategories;
                            arr.splice(arr.indexOf(subCategory), 1);
                        }

                        scope.isLoading = false;
                    };

                    if(!subCategory) {
                        category.$delete({
                                types: type+"s",
                                categoryId: category.uniqueId
                            }
                            , onSuccess, onError);
                    } else {
                        category.$deleteSubCategory({
                                types: type+"s",
                                categoryId: category.uniqueId,
                                subCategoryId: subCategory.uniqueId,
                            }
                            , onSuccess, onError);
                    }
                };
                let modelType:string = subCategory ? 'sub category' : 'cssClasses';
                let title:string = this.$filter('translate')("DELETE_CATEGORY_MODAL_HEADER", "{'modelType': '" + modelType +"' }");
                let message:string = this.$filter('translate')("DELETE_CATEGORY_MODAL_CATEGORY_NAME", "{'modelType': '" + modelType +"' }");

                this.ModalsHandler.openConfirmationModal(title, message, false, 'sdc-xsm').then(onOk);
            };

            this.$scope.serviceCategories = this.cacheService.get('serviceCategories');
            this.$scope.resourceCategories = this.cacheService.get('resourceCategories');
        }
    }
}
