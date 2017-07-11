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
import {ComponentFactory} from "app/utils";
import {Product, IGroup, ISubCategory, IMainCategory} from "app/models";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {CacheService} from "app/services";

export interface IProductHierarchyScope extends IWorkspaceViewModelScope {

    categoriesOptions:Array<IMainCategory>;
    product:Product;
    isLoading:boolean;
    showDropDown:boolean;

    onInputTextClicked():void;
    onGroupSelected(category:IMainCategory, subcategory:ISubCategory, group:IGroup):void;
    clickOutside():void;
    deleteGroup(uniqueId:string):void;
}

export class ProductHierarchyViewModel {

    static '$inject' = [
        '$scope',
        'Sdc.Services.CacheService',
        'ComponentFactory',
        '$state'
    ];

    constructor(private $scope:IProductHierarchyScope,
                private cacheService:CacheService,
                private ComponentFactory:ComponentFactory,
                private $state:ng.ui.IStateService) {


        this.$scope.product = <Product>this.$scope.getComponent();
        this.$scope.setValidState(true);
        this.initScope();
        this.$scope.updateSelectedMenuItem();
    }

    private initCategories = () => {
        this.$scope.categoriesOptions = angular.copy(this.cacheService.get('productCategories'));
        let selectedGroup:Array<IGroup> = [];
        _.forEach(this.$scope.product.categories, (category:IMainCategory) => {
            _.forEach(category.subcategories, (subcategory:ISubCategory) => {
                selectedGroup = selectedGroup.concat(subcategory.groupings);
            });
        });
        _.forEach(this.$scope.categoriesOptions, (category:IMainCategory) => {
            _.forEach(category.subcategories, (subcategory:ISubCategory) => {
                _.forEach(subcategory.groupings, (group:ISubCategory) => {
                    let componentGroup:IGroup = _.find(selectedGroup, (componentGroupObj) => {
                        return componentGroupObj.uniqueId == group.uniqueId;
                    });
                    if (componentGroup) {
                        group.isDisabled = true;
                    }
                });
            });
        });
    };

    private setFormValidation = ():void => {

        this.$scope.setValidState(true);

    };

    private initScope = ():void => {
        this.$scope.isLoading = false;
        this.$scope.showDropDown = false;
        this.initCategories();
        this.setFormValidation();

        this.$scope.onGroupSelected = (category:IMainCategory, subcategory:ISubCategory, group:IGroup):void => {
            this.$scope.product.addGroup(category, subcategory, group);
            this.$state.current.data.unsavedChanges = !this.$scope.isViewMode();
            group.isDisabled = true;
            this.$scope.showDropDown = false;
            this.setFormValidation();
        };

        this.$scope.onInputTextClicked = ():void => {//just edit the component in place, no pop up nor server update ?
            this.$scope.showDropDown = !this.$scope.showDropDown;
        };

        this.$scope.clickOutside = ():any => {
            this.$scope.showDropDown = false;
        };

        this.$scope.deleteGroup = (uniqueId:string):void => {
            //delete group from component
            this.$scope.product.deleteGroup(uniqueId);
            this.$state.current.data.unsavedChanges = !this.$scope.isViewMode();
            this.setFormValidation();
            //enabled group
            _.forEach(this.$scope.categoriesOptions, (category:IMainCategory) => {
                _.forEach(category.subcategories, (subcategory:ISubCategory) => {
                    let groupObj:IGroup = _.find(subcategory.groupings, (group) => {
                        return group.uniqueId === uniqueId;
                    });
                    if (groupObj) {
                        groupObj.isDisabled = false;
                    }
                });
            });
        }
    };
}
