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
'use strict';
import {Component} from "./component";
import {IProductService} from "../../services/components/product-service";
import {IGroup, ISubCategory, IMainCategory, ICategoryBase} from "../category";
import {ComponentMetadata} from "../component-metadata";

export class Product extends Component {

    public contacts:Array<string>;
    public componentService:IProductService;
    public fullName:string;

    constructor(componentService:IProductService, $q:ng.IQService, component?:Product) {
        super(componentService, $q, component);

        if (component) {
            this.fullName = component.fullName;
            this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
            this.contacts = component.contacts;
        }
        this.componentService = componentService;
        this.iconSprite = "sprite-product-icons";
    }

    public deleteGroup = (uniqueId:string):void => {
        _.forEach(this.categories, (category:IMainCategory) => {
            _.forEach(category.subcategories, (subcategory:ISubCategory) => {
                subcategory.groupings = _.reject(subcategory.groupings, (group:IGroup) => {
                    return group.uniqueId === uniqueId;
                });
                if (subcategory.groupings.length == 0) { // if there is no groups, delete the subcategory
                    category.subcategories = _.reject(category.subcategories, (subcategoryObj:ISubCategory) => {
                        return subcategoryObj.uniqueId === subcategory.uniqueId;
                    });
                    if (category.subcategories.length == 0) { // if there is no subcategory, delete the category
                        this.categories = _.reject(this.categories, (categoryObj:IMainCategory) => {
                            return categoryObj.uniqueId === category.uniqueId;
                        });
                    }
                }
            });
        });
    };

    private getCategoryObjectById = (categoriesArray:Array<ICategoryBase>, categoryUniqueId:string):ICategoryBase => {
        let categorySelected = _.find(categoriesArray, (category) => {
            return category.uniqueId === categoryUniqueId;
        });
        return categorySelected;
    };

    public addGroup = (category:IMainCategory, subcategory:ISubCategory, group:IGroup):void => {
        if (!this.categories) {
            this.categories = new Array<IMainCategory>();
        }
        let existingCategory:IMainCategory = <IMainCategory>this.getCategoryObjectById(this.categories, category.uniqueId);
        let newGroup = angular.copy(group);
        newGroup.filterTerms = undefined;
        newGroup.isDisabled = undefined;
        if (!existingCategory) {
            let newCategory:IMainCategory = angular.copy(category);
            newCategory.filteredGroup = undefined;
            newCategory.subcategories = [];
            let newSubcategory:ISubCategory = angular.copy(subcategory);
            newSubcategory.groupings = [];
            newSubcategory.groupings.push(newGroup);
            newCategory.subcategories.push(newSubcategory);
            this.categories.push(newCategory);
        }
        else {
            let existingSubcategory:ISubCategory = <ISubCategory> this.getCategoryObjectById(existingCategory.subcategories, subcategory.uniqueId);
            if (!existingSubcategory) {
                let newSubcategory:ISubCategory = angular.copy(subcategory);
                newSubcategory.groupings = [];
                newSubcategory.groupings.push(newGroup);
                existingCategory.subcategories.push(newSubcategory);

            } else {
                let existingGroup:IGroup = <IGroup> this.getCategoryObjectById(existingSubcategory.groupings, group.uniqueId);
                if (!existingGroup) {
                    existingSubcategory.groupings.push(newGroup);
                }
            }
        }
    };

    getTypeUrl():string {
        return 'products/';
    }

    public setComponentMetadata(componentMetadata:ComponentMetadata) {
        super.setComponentMetadata(componentMetadata);
        this.setComponentDisplayData();
    };

    setComponentDisplayData():void {
        this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version;
        this.iconSprite = "sprite-product-icons";
    }
}


