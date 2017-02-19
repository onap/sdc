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
/// <reference path="../../references"/>
module Sdc.ViewModels {

    'use strict';

    interface Checkboxes {
        componentTypes:Array<string>;
        resourceSubTypes:Array<string>;
    }

    interface CheckboxesFilter {
        // Types
        selectedComponentTypes:Array<string>;
        selectedResourceSubTypes:Array<string>;
        // Categories
        selectedCategoriesModel:Array<string>;
        // Statuses
        selectedStatuses:Array<string>;
    }

    interface Gui {
        isLoading: boolean;
        onResourceSubTypesClick:Function;
        onComponentTypeClick:Function;
        onCategoryClick:Function;
        onSubcategoryClick:Function;
        onGroupClick:Function;
    }

    export interface ICatalogViewModelScope extends ng.IScope {
        checkboxes:Checkboxes;
        checkboxesFilter:CheckboxesFilter;
        gui:Gui;

        categories: Array<Models.IMainCategory>;
        confStatus: Models.IConfigStatuses;
        sdcMenu:Models.IAppMenu;
        catalogFilterdItems: Array<Models.Components.Component>;
        expandedSection: Array<string>;
        actionStrategy: any;
        user: Models.IUserProperties;
        catalogMenuItem: any;
        version:string;
        sortBy:string;
        reverse:boolean;

        //this is for UI paging
        numberOfItemToDisplay:number;
        isAllItemDisplay: boolean;

        openViewerModal(isResource: boolean, uniqueId: string): void;
        changeLifecycleState(entity:any,state:string): void;
        sectionClick (section:string):void;
        order(sortBy:string): void;
        getNumOfElements(num:number): string;
        goToComponent(component:Models.Components.Component):void;
        raiseNumberOfElementToDisplay():void;
    }

    export class CatalogViewModel {
        static '$inject' = [
            '$scope',
            '$filter',
            'Sdc.Services.EntityService',
            'sdcConfig',
            'sdcMenu',
            '$state',
            '$q',
            'Sdc.Services.UserResourceService',
            '$modal',
            '$templateCache',
            'Sdc.Services.CacheService',
            'ComponentFactory',
            'ChangeLifecycleStateHandler',
            'ModalsHandler',
            'MenuHandler'
        ];

        constructor(private $scope:ICatalogViewModelScope,
                    private $filter:ng.IFilterService,
                    private EntityService:Services.EntityService,
                    private sdcConfig:Models.IAppConfigurtaion,
                    private sdcMenu:Models.IAppMenu,
                    private $state:any,
                    private $q:any,
                    private userResourceService:Sdc.Services.IUserResourceClass,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private cacheService:Services.CacheService,
                    private ComponentFactory: Sdc.Utils.ComponentFactory,
                    private ChangeLifecycleStateHandler: Sdc.Utils.ChangeLifecycleStateHandler,
                    private OpenViewModalHandler: Utils.ModalsHandler,
                    private MenuHandler: Utils.MenuHandler
            ) {

            this.initScopeMembers();
            this.initCatalogData(); // Async task to get catalog from server.
            this.initScopeMethods();
        }

        private initCatalogData = ():void => {
            let onSuccess = (followedResponse:Array<Models.Components.Component>):void => {
                this.$scope.catalogFilterdItems = followedResponse;
                this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.catalogFilterdItems.length;
                this.$scope.categories = this.cacheService.get('serviceCategories').concat(this.cacheService.get('resourceCategories')).concat(this.cacheService.get('productCategories'));
                this.$scope.gui.isLoading = false;
            };

            let onError = ():void => {
                console.info('Failed to load catalog CatalogViewModel::initCatalog');
                this.$scope.gui.isLoading = false;
            };
            this.EntityService.getCatalog().then(onSuccess, onError);
        };



        private initScopeMembers = ():void => {
            // Gui init
            this.$scope.gui = <Gui>{};
            this.$scope.gui.isLoading = true;
            this.$scope.numberOfItemToDisplay = 0;
            //this.$scope.categories = this.cacheService.get('categoriesMap');
            this.$scope.sdcMenu = this.sdcMenu;
            this.$scope.confStatus = this.sdcMenu.statuses;
            this.$scope.expandedSection = ["type", "cssClasses", "product-category", "status"];
            this.$scope.user = this.userResourceService.getLoggedinUser();
            this.$scope.catalogMenuItem = this.sdcMenu.catalogMenuItem;
            this.$scope.version = this.cacheService.get('version');
            this.$scope.sortBy = 'lastUpdateDate';
            this.$scope.reverse = true;


            // Checklist init
            this.$scope.checkboxes = <Checkboxes>{};
            this.$scope.checkboxes.componentTypes = ['Resource', 'Service', 'Product'];
            this.$scope.checkboxes.resourceSubTypes = ['VF', 'VFC', 'CP', 'VL'];

            // Checkboxes filter init
            this.$scope.checkboxesFilter = <CheckboxesFilter>{};
            this.$scope.checkboxesFilter.selectedComponentTypes = [];
            this.$scope.checkboxesFilter.selectedResourceSubTypes = [];
            this.$scope.checkboxesFilter.selectedCategoriesModel = [];
            this.$scope.checkboxesFilter.selectedStatuses = [];

      //      this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.catalogFilterdItems.length;
        };

        private initScopeMethods = ():void => {
            this.$scope.sectionClick = (section:string):void => {
                let index:number = this.$scope.expandedSection.indexOf(section);
                if (index!==-1) {
                    this.$scope.expandedSection.splice(index,1);
                } else {
                    this.$scope.expandedSection.push(section);
                }
            };


            this.$scope.order = (sortBy:string):void => {//default sort by descending last update. default for alphabetical = ascending
                this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : (sortBy === 'lastUpdateDate') ? true: false;
                this.$scope.sortBy = sortBy;
            };


            this.$scope.goToComponent = (component:Models.Components.Component):void => {
                this.$scope.gui.isLoading = true;
                this.$state.go('workspace.general', {id: component.uniqueId, type:component.componentType.toLowerCase()});
            };


            // Will print the number of elements found in catalog
            this.$scope.getNumOfElements = (num:number) : string => {
                if (!num || num===0){
                    return "No Elements found";
                } else if (num===1){
                    return "1 Element found";
                }else {
                    return num + " Elements found";
                }
            };

            /**
             * Select | unselect sub resource when resource is clicked | unclicked.
             * @param type
             */
            this.$scope.gui.onComponentTypeClick = (type:string): void => {
                if (type==='Resource'){
                    if (this.$scope.checkboxesFilter.selectedComponentTypes.indexOf('Resource')===-1){
                        // If the resource was not selected, unselect all childs.
                        this.$scope.checkboxesFilter.selectedResourceSubTypes = [];
                    } else {
                        // If the resource was selected, select all childs
                        this.$scope.checkboxesFilter.selectedResourceSubTypes = angular.copy(this.$scope.checkboxes.resourceSubTypes);
                    }
                }
            };

            /**
             * Selecting | unselect resources when sub resource is clicked | unclicked.
             */
            this.$scope.gui.onResourceSubTypesClick = ():void => {
                if (this.$scope.checkboxesFilter.selectedResourceSubTypes && this.$scope.checkboxesFilter.selectedResourceSubTypes.length===this.$scope.checkboxes.resourceSubTypes.length){
                    this.$scope.checkboxesFilter.selectedComponentTypes.push('Resource');
                } else {
                    this.$scope.checkboxesFilter.selectedComponentTypes = _.without(this.$scope.checkboxesFilter.selectedComponentTypes,'Resource');
                }
            };

            this.$scope.gui.onCategoryClick = (category:Models.IMainCategory): void => {
                // Select | Unselect all childs
                if (this.isCategorySelected(category.uniqueId)){
                    this.$scope.checkboxesFilter.selectedCategoriesModel = this.$scope.checkboxesFilter.selectedCategoriesModel.concat(angular.copy(_.map(category.subcategories, (item) => { return item.uniqueId; })));
                    if (category.subcategories) {
                        category.subcategories.forEach((sub:Models.ISubCategory)=> { // Loop on all selected subcategories and mark the childrens
                            this.$scope.checkboxesFilter.selectedCategoriesModel = this.$scope.checkboxesFilter.selectedCategoriesModel.concat(angular.copy(_.map(sub.groupings, (item) => {
                                return item.uniqueId;
                            })));
                        });
                    }
                } else {
                    this.$scope.checkboxesFilter.selectedCategoriesModel = _.difference(this.$scope.checkboxesFilter.selectedCategoriesModel, _.map(category.subcategories, (item) => { return item.uniqueId; }));
                    if (category.subcategories) {
                        category.subcategories.forEach((sub:Models.ISubCategory)=> { // Loop on all selected subcategories and un mark the childrens
                            this.$scope.checkboxesFilter.selectedCategoriesModel = _.difference(this.$scope.checkboxesFilter.selectedCategoriesModel, _.map(sub.groupings, (item) => {
                                return item.uniqueId;
                            }));
                        });
                    }
                }
            };

            this.$scope.gui.onSubcategoryClick = (category:Models.IMainCategory, subCategory:Models.ISubCategory) : void => {
                // Select | Unselect all childs
                if (this.isCategorySelected(subCategory.uniqueId)){
                    this.$scope.checkboxesFilter.selectedCategoriesModel = this.$scope.checkboxesFilter.selectedCategoriesModel.concat(angular.copy(_.map(subCategory.groupings, (item) => { return item.uniqueId; })));
                } else {
                    this.$scope.checkboxesFilter.selectedCategoriesModel = _.difference(this.$scope.checkboxesFilter.selectedCategoriesModel, _.map(subCategory.groupings, (item) => { return item.uniqueId; }));
                }

                // Mark | Un mark the parent when all childs selected.
                if (this.areAllCategoryChildsSelected(category)){
                    // Add the category to checkboxesFilter.selectedCategoriesModel
                    this.$scope.checkboxesFilter.selectedCategoriesModel.push(category.uniqueId);
                } else {
                    this.$scope.checkboxesFilter.selectedCategoriesModel = _.without(this.$scope.checkboxesFilter.selectedCategoriesModel, category.uniqueId);
                }

            };

            this.$scope.raiseNumberOfElementToDisplay = () : void => {
                this.$scope.numberOfItemToDisplay = this.$scope.numberOfItemToDisplay +35;
                if(this.$scope.catalogFilterdItems) {
                    this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.catalogFilterdItems.length;
                }
            };

            this.$scope.gui.onGroupClick = (subCategory:Models.ISubCategory) : void => {
                // Mark | Un mark the parent when all childs selected.
                if (this.areAllSubCategoryChildsSelected(subCategory)){
                    // Add the category to checkboxesFilter.selectedCategoriesModel
                    this.$scope.checkboxesFilter.selectedCategoriesModel.push(subCategory.uniqueId);
                } else {
                    this.$scope.checkboxesFilter.selectedCategoriesModel = _.without(this.$scope.checkboxesFilter.selectedCategoriesModel, subCategory.uniqueId);
                }
            };


        };

        private areAllCategoryChildsSelected = (category:Models.IMainCategory):boolean => {
            if (!category.subcategories){return false;}
            let allIds = _.map(category.subcategories, (sub:Models.ISubCategory)=>{return sub.uniqueId;});
            let total = _.intersection(this.$scope.checkboxesFilter.selectedCategoriesModel, allIds);
            return total.length === category.subcategories.length?true:false;
        };

        private areAllSubCategoryChildsSelected = (subCategory:Models.ISubCategory):boolean => {
            if (!subCategory.groupings){return false;}
            let allIds = _.map(subCategory.groupings, (group:Models.IGroup)=>{return group.uniqueId;});
            let total = _.intersection(this.$scope.checkboxesFilter.selectedCategoriesModel, allIds);
            return total.length === subCategory.groupings.length?true:false;
        };

        private isCategorySelected = (uniqueId:string):boolean => {
            if (this.$scope.checkboxesFilter.selectedCategoriesModel.indexOf(uniqueId)!==-1){
                return true;
            }
            return false;
        };

    }
}
