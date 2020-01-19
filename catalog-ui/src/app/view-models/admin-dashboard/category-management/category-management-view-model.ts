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
import {ModalsHandler, ValidationUtils} from "app/utils";
import {ICategoryResource} from "app/services";
import {CacheService} from "app/services-ng2";
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
                templateUrl: '../add-category-modal/add-category-modal-view.html',
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

        this.$scope.serviceCategories = this.cacheService.get('serviceCategories');
        this.$scope.resourceCategories = this.cacheService.get('resourceCategories');
    }
}
