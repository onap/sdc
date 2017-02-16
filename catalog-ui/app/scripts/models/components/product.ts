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
/**
 * Created by obarda on 2/4/2016.
 */
/// <reference path="../../references"/>
module Sdc.Models.Components {
    'use strict';

    export class Product extends Component{

        public contacts:Array<string>;
        public componentService: Services.Components.IProductService;
        public fullName: string;

        constructor(componentService: Services.Components.IProductService, $q:ng.IQService, component?:Product) {
            super(componentService, $q, component);

            if(component) {
                this.fullName = component.fullName;
                this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
                this.contacts = component.contacts;
            }
            this.componentService = componentService;
            this.iconSprite = "sprite-product-icons";
        }

        public deleteGroup = (uniqueId: string): void => {
            _.forEach(this.categories, (category: Models.IMainCategory) => {
                _.forEach(category.subcategories, (subcategory:Models.ISubCategory) => {
                    subcategory.groupings = _.reject (subcategory.groupings, (group:Models.IGroup) => {
                        return group.uniqueId === uniqueId;
                    });
                    if(subcategory.groupings.length == 0){ // if there is no groups, delete the subcategory
                        category.subcategories = _.reject (category.subcategories, (subcategoryObj:Models.ISubCategory) => {
                            return subcategoryObj.uniqueId === subcategory.uniqueId;
                        });
                        if(category.subcategories.length == 0){ // if there is no subcategory, delete the category
                            this.categories = _.reject (this.categories , (categoryObj:Models.IMainCategory) => {
                                return categoryObj.uniqueId === category.uniqueId;
                            });
                        }
                    }
                });
            });
        };

        private getCategoryObjectById = (categoriesArray:Array<Models.ICategoryBase>, categoryUniqueId:string):Models.ICategoryBase => {
            let categorySelected =  _.find(categoriesArray, (category) => {
                return category.uniqueId === categoryUniqueId;
            });
            return categorySelected;
        };

        public addGroup = (category: Models.IMainCategory, subcategory: Models.ISubCategory, group: Models.IGroup): void => {
            if(!this.categories){
                this.categories = new Array<Models.IMainCategory>();
            }
            let existingCategory:Models.IMainCategory = <Models.IMainCategory>this.getCategoryObjectById(this.categories, category.uniqueId);
            let newGroup = angular.copy(group);
            newGroup.filterTerms = undefined;
            newGroup.isDisabled = undefined;
            if(!existingCategory){
                let newCategory: Models.IMainCategory = angular.copy(category);
                newCategory.filteredGroup = undefined;
                newCategory.subcategories = [];
                let newSubcategory:Models.ISubCategory = angular.copy(subcategory);
                newSubcategory.groupings = [];
                newSubcategory.groupings.push(newGroup);
                newCategory.subcategories.push(newSubcategory);
                this.categories.push(newCategory);
            }
            else{
                let existingSubcategory:Models.ISubCategory = <Models.ISubCategory> this.getCategoryObjectById(existingCategory.subcategories, subcategory.uniqueId);
                if(!existingSubcategory){
                    let newSubcategory:Models.ISubCategory = angular.copy(subcategory);
                    newSubcategory.groupings = [];
                    newSubcategory.groupings.push(newGroup);
                    existingCategory.subcategories.push(newSubcategory);

                } else {
                    let existingGroup:Models.IGroup =  <Models.IGroup> this.getCategoryObjectById(existingSubcategory.groupings, group.uniqueId);
                    if(!existingGroup){
                        existingSubcategory.groupings.push(newGroup);
                    }
                }
            }
        };

    }
}

