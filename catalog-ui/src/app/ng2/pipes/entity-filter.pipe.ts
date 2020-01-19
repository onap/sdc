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

import {Pipe, PipeTransform} from "@angular/core";
import {Component, Resource} from "app/models";
import {ComponentType} from "app/utils/constants";

export interface ISearchFilter {
    [key:string]: string;
}

export interface IEntityFilterObject {
    // Types
    selectedComponentTypes?:Array<string>;
    selectedResourceSubTypes?:Array<string>;
    // Categories
    selectedCategoriesModel?:Array<string>;
    // Statuses
    selectedStatuses?:Array<string>;
    // distributed
    distributed?:Array<string>;
    // search
    search?:ISearchFilter;
    
}

@Pipe({name: 'entity-filter'})
export class EntityFilterPipe implements PipeTransform{
    constructor() {
    }

    public static transform(components:Array<Component>, filter:IEntityFilterObject) {
        let filteredComponents:Array<Component> = components;

        // filter by type
        // --------------------------------------------------------------------------
        if ((filter.selectedComponentTypes && filter.selectedComponentTypes.length > 0) || (filter.selectedResourceSubTypes && filter.selectedResourceSubTypes.length > 0)) {
            let filteredTypes = [];
            angular.forEach(components, (component:Component):void => {
                // Filter by component type
                let typeLower:string = component.componentType.toLowerCase();
                let typeFirstCapital:string = typeLower.charAt(0).toUpperCase() + typeLower.slice(1);
                if (filter.selectedComponentTypes.indexOf(typeFirstCapital) !== -1) {
                    filteredTypes.push(component);
                }

                // Filter by resource sub type, only in case the resource checkbox was not selected (because in this case we already added all the components in above section).
                if (component.isResource() && filter.selectedComponentTypes.indexOf("Resource") === -1 && filter.selectedResourceSubTypes.length > 0) {
                    //filteredComponents.pop(); // Remove the last inserted component.
                    let resource:Resource = <Resource>component;
                    if (filter.selectedResourceSubTypes.indexOf(resource.getComponentSubType()) !== -1) {
                        filteredTypes.push(component);
                    }
                }
            });
            filteredComponents = filteredTypes;
        }

        // filter by categories & subcategories & groupings
        // --------------------------------------------------------------------------
        if (filter.selectedCategoriesModel && filter.selectedCategoriesModel.length > 0) {
            let filteredCategories = [];
            angular.forEach(filteredComponents, (component:Component):void => {
                let componentCategory = component.categoryNormalizedName +
                    ((component.subCategoryNormalizedName) ? '.' + component.subCategoryNormalizedName : '');
                if (component.componentType === ComponentType.RESOURCE) {
                    componentCategory = 'resourceNewCategory.' + componentCategory;
                } else if (component.componentType === ComponentType.SERVICE) {
                    componentCategory = 'serviceNewCategory.' + componentCategory;
                }
                if (filter.selectedCategoriesModel.indexOf(componentCategory) !== -1) {
                    filteredCategories.push(component);
                }
            });
            filteredComponents = filteredCategories;
        }

        // filter by statuses
        // --------------------------------------------------------------------------
        if (filter.selectedStatuses && filter.selectedStatuses.length > 0) {

            let filteredStatuses = [];
            angular.forEach(filteredComponents, (component:Component):void => {
                if (filter.selectedStatuses.indexOf(component.lifecycleState) > -1) {
                    filteredStatuses.push(component);
                }
                //if status DISTRIBUTED && CERTIFIED are selected the component will added in  CERTIFIED status , not need to add twice
                if (filter.selectedStatuses.indexOf('DISTRIBUTED') > -1 && !(filter.selectedStatuses.indexOf('CERTIFIED') > -1)) {
                    if (component.distributionStatus && component.distributionStatus.indexOf('DISTRIBUTED') > -1 && component.lifecycleState.indexOf('CERTIFIED') > -1) {
                        filteredStatuses.push(component);
                    }
                }
            });
            filteredComponents = filteredStatuses;
        }

        // filter by statuses and distributed
        // --------------------------------------------------------------------------
        if (filter.distributed != undefined && filter.distributed.length > 0) {
            let filterDistributed:Array<any> = filter.distributed;
            let filteredDistributed = [];
            angular.forEach(filteredComponents, (entity) => {
                filterDistributed.forEach((distribute) => {
                    let distributeItem = distribute.split(',');
                    distributeItem.forEach((item) => {
                        if (item !== undefined && entity.distributionStatus === item) {
                            filteredDistributed.push(entity);
                        }
                    })
                });
            });
            filteredComponents = filteredDistributed;
        }

        // filter by search
        // --------------------------------------------------------------------------
        if (filter.search != undefined) {
            Object.keys(filter.search).forEach((searchKey) => {
                let searchVal = filter.search[searchKey];
                if (searchVal) {
                    searchVal = searchVal.toLowerCase();
                    filteredComponents = filteredComponents.filter((component:Component) =>
                        component[searchKey].toLowerCase().indexOf(searchVal) !== -1);
                }
            });
        }

        return filteredComponents;
    }

    public transform(components:Array<Component>, filter:IEntityFilterObject) {
        return EntityFilterPipe.transform(components, filter);
    }
}
