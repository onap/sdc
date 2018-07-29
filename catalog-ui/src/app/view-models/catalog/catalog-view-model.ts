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
import {Component, IMainCategory, IGroup, IConfigStatuses, IAppMenu, IAppConfigurtaion, IUserProperties, ISubCategory, ICategoryBase} from "app/models";
import {EntityService, CacheService} from "app/services";
import {ComponentFactory, ResourceType, MenuHandler, ChangeLifecycleStateHandler} from "app/utils";
import {UserService} from "../../ng2/services/user.service";
import {ArchiveService} from "../../ng2/services/archive.service";
import { ICatalogSelector, CatalogSelectorTypes } from "../../models/catalogSelector";
import {IConfigStatus} from "../../models/app-config";

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
    selectedStatuses:Array<Array<string>>;
}

interface Gui {
    isLoading:boolean;
    onComponentSubTypesClick:Function;
    onComponentTypeClick:Function;
    onCategoryClick:Function;
    onStatusClick:Function;
    changeFilterTerm:Function;
}

interface IFilterParams {
    components: string[];
    categories: string[];
    statuses: (string)[];
    order: [string, boolean];
    term: string;
    active: boolean;
}

interface ICategoriesMap {
    [key: string]: {
        category: ICategoryBase,
        parent: ICategoryBase
    }
}

export interface ICatalogViewModelScope extends ng.IScope {
    checkboxes:Checkboxes;
    checkboxesFilter:CheckboxesFilter;
    gui:Gui;

    categories:Array<IMainCategory>;
    confStatus:IConfigStatuses;
    sdcMenu:IAppMenu;
    catalogFilterdItems:Array<Component>;
    expandedSection:Array<string>;
    actionStrategy:any;
    user:IUserProperties;
    catalogMenuItem:any;
    version:string;
    sortBy:string;
    reverse:boolean;
    vfcmtType:string;

    //this is for UI paging
    numberOfItemToDisplay:number;
    isAllItemDisplay:boolean;
    catalogFilteredItemsNum:number;
    changeLifecycleState(entity:any, state:string):void;
    sectionClick (section:string):void;
    order(sortBy:string):void;
    getElementFoundTitle(num:number):string;
    goToComponent(component:Component):void;
    raiseNumberOfElementToDisplay():void;

    selectedCatalogItem: ICatalogSelector;
    catalogSelectorItems: Array<ICatalogSelector>;
    showCatalogSelector: boolean;
    catalogAllItems:Array<Component>; /* fake data */
    elementFoundTitle: string;
    elementTypeTitle: string;

    selectLeftSwitchItem (item: ICatalogSelector): void;
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
        'UserServiceNg2',
        'Sdc.Services.CacheService',
        'ComponentFactory',
        'ChangeLifecycleStateHandler',
        'MenuHandler',
        'ArchiveServiceNg2'
    ];

    private defaultFilterParams:IFilterParams = {
        components: [],
        categories: [],
        statuses: [],
        order: ['lastUpdateDate', true],
        term: '',
        active: true
    };
    private categoriesMap:ICategoriesMap;

    constructor(private $scope:ICatalogViewModelScope,
                private $filter:ng.IFilterService,
                private EntityService:EntityService,
                private sdcConfig:IAppConfigurtaion,
                private sdcMenu:IAppMenu,
                private $state:ng.ui.IStateService,
                private $q:ng.IQService,
                private userService:UserService,
                private cacheService:CacheService,
                private ComponentFactory:ComponentFactory,
                private ChangeLifecycleStateHandler:ChangeLifecycleStateHandler,
                private MenuHandler:MenuHandler,
                private ArchiveService:ArchiveService
            ) {


        this.initLeftSwitch();
        this.initScopeMembers();
        this.loadFilterParams(); 
        this.initCatalogData(); // Async task to get catalog from server.
        this.initScopeMethods();
    }


    private initLeftSwitch = ():void => {
        this.$scope.showCatalogSelector = false;

        this.$scope.catalogSelectorItems = [
            {value: CatalogSelectorTypes.Active, title: "Active Items", header: "Active"},
            {value: CatalogSelectorTypes.Archive, title: "Archive", header: "Archived"}
        ];
        // set active items is default
        this.$scope.selectedCatalogItem = this.$scope.catalogSelectorItems[0];
    };

    private initCatalogData = ():void => {
        if(this.$scope.selectedCatalogItem.value === CatalogSelectorTypes.Archive){
            this.getArchiveCatalogItems();
        } else {
            this.getActiveCatalogItems();
        }
    };

    private initScopeMembers = ():void => {
        // Gui init
        this.$scope.gui = <Gui>{};
        this.$scope.numberOfItemToDisplay = 0;
        this.$scope.categories = this.cacheService.get('serviceCategories').concat(this.cacheService.get('resourceCategories')).map((cat) => <IMainCategory>cat);
        this.$scope.sdcMenu = this.sdcMenu;
        this.$scope.confStatus = this.sdcMenu.statuses;
        this.$scope.expandedSection = ["type", "category", "status"];
        this.$scope.user = this.userService.getLoggedinUser();
        this.$scope.catalogMenuItem = this.sdcMenu.catalogMenuItem;

        // Checklist init
        this.$scope.checkboxes = <Checkboxes>{};
        this.$scope.checkboxes.componentTypes = ['Resource', 'Service'];
        this.$scope.checkboxes.resourceSubTypes = ['VF', 'VFC', 'CR', 'PNF', 'CP', 'VL'];
        this.categoriesMap = this.initCategoriesMap();

        this.initCheckboxesFilter();
        this.$scope.version = this.cacheService.get('version');
        this.$scope.sortBy = 'lastUpdateDate';
        this.$scope.reverse = true;

    };

    private initCheckboxesFilter() {
        // Checkboxes filter init
        this.$scope.checkboxesFilter = <CheckboxesFilter>{};
        this.$scope.checkboxesFilter.selectedComponentTypes = [];
        this.$scope.checkboxesFilter.selectedResourceSubTypes = [];
        this.$scope.checkboxesFilter.selectedCategoriesModel = [];
        this.$scope.checkboxesFilter.selectedStatuses = [];
    }

    private initCategoriesMap(categoriesList?:(ICategoryBase)[], parentCategory:ICategoryBase=null): ICategoriesMap {
        categoriesList = (categoriesList) ? categoriesList : this.$scope.categories;

        // Init categories map
        return categoriesList.reduce((acc, cat) => {
            acc[cat.uniqueId] = {
                category: cat,
                parent: parentCategory
            };
            const catChildren = ((<IMainCategory>cat).subcategories)
                ? (<IMainCategory>cat).subcategories
                : (((<ISubCategory>cat).groupings)
                    ? (<ISubCategory>cat).groupings
                    : null);
            if (catChildren) {
                Object.assign(acc, this.initCategoriesMap(catChildren, cat));
            }
            return acc;
        }, <ICategoriesMap>{});
    }

    private initScopeMethods = ():void => {
        this.$scope.selectLeftSwitchItem = (item: ICatalogSelector): void => {

            if (this.$scope.selectedCatalogItem.value !== item.value) {
                this.$scope.selectedCatalogItem = item;
                switch (item.value) {
                    case CatalogSelectorTypes.Active:
                        this.getActiveCatalogItems(true);
                        break;

                    case CatalogSelectorTypes.Archive:
                        this.getArchiveCatalogItems(true);
                        break;
                }
                this.changeFilterParams({active: (item.value === CatalogSelectorTypes.Active)})
            }
        };

        this.$scope.sectionClick = (section: string): void => {
            let index: number = this.$scope.expandedSection.indexOf(section);
            if (index !== -1) {
                this.$scope.expandedSection.splice(index, 1);
            } else {
                this.$scope.expandedSection.push(section);
            }
        };


        this.$scope.order = (sortBy: string): void => {//default sort by descending last update. default for alphabetical = ascending
            this.changeFilterParams({
                order: (this.$scope.filterParams.order[0] === sortBy)
                    ? [sortBy, !this.$scope.filterParams.order[1]]
                    : [sortBy, sortBy === 'lastUpdateDate']
            });
        };


        this.$scope.goToComponent = (component: Component): void => {
            this.$scope.gui.isLoading = true;
            this.$state.go('workspace.general', {id: component.uniqueId, type: component.componentType.toLowerCase()});
        };


        // Will print the number of elements found in catalog
        this.$scope.getNumOfElements = (num:number):string => {
            if (!num || num === 0) {
                return `No <b>${this.$scope.selectedCatalogItem.header}</b> Elements found`;
            } else if (num === 1) {
                return `1 <b>${this.$scope.selectedCatalogItem.header}</b> Element found`;
            } else {
                return num + ` <b>${this.$scope.selectedCatalogItem.header}</b> Elements found`;
            }
        };

        /**
         * Select | unselect sub resource when resource is clicked | unclicked.
         * @param type
         */
        this.$scope.gui.onComponentTypeClick = (compType: string, checked?: boolean): void => {
            let components = angular.copy(this.$scope.filterParams.components);
            const compIdx = components.indexOf(compType);
            checked = (checked !== undefined) ? checked : compIdx === -1;
            if (checked && compIdx === -1) {
                components.push(compType);
                components = this.cleanSubsFromList(components);
            } else if (!checked && compIdx !== -1) {
                components.splice(compIdx, 1);
            }
            this.changeFilterParams({
                components: components
            });
        };

        /**
         * Selecting | unselect resources when sub resource is clicked | unclicked.
         */
        this.$scope.gui.onComponentSubTypesClick = (compSubType: string, compType: string, checked?: boolean): void => {
            const componentSubTypesCheckboxes = this.$scope.checkboxes[compType.toLowerCase() + 'SubTypes'];
            if (componentSubTypesCheckboxes) {
                let components = angular.copy(this.$scope.filterParams.components);
                let componentSubTypes = components.filter((st) => st.startsWith(compType + '.'));

                const compSubTypeValue = compType + '.' + compSubType;
                const compSubTypeValueIdx = components.indexOf(compSubTypeValue);
                checked = (checked !== undefined) ? checked : compSubTypeValueIdx === -1;
                if (checked && compSubTypeValueIdx === -1) {
                    components.push(compSubTypeValue);
                    componentSubTypes.push(compSubTypeValue);

                    // if all sub types are checked, then check the main component type
                    if (componentSubTypes.length === componentSubTypesCheckboxes.length) {
                        this.$scope.gui.onComponentTypeClick(compType, true);
                        return;
                    }
                } else if (!checked) {
                    const compIdx = components.indexOf(compType);
                    // if sub type exists, then remove it
                    if (compSubTypeValueIdx !== -1) {
                        components.splice(compSubTypeValueIdx, 1);
                    }
                    // else, if sub type doesn't exists, but its parent main component type exists,
                    // then remove the main type and push all sub types except the current
                    else if (compIdx !== -1) {
                        components.splice(compIdx, 1);
                        componentSubTypesCheckboxes.forEach((st) => {
                            if (st !== compSubType) {
                                components.push(compType + '.' + st);
                            }
                        });
                    }
                }

                this.changeFilterParams({
                    components
                });
            }
        };

        this.$scope.gui.onCategoryClick = (category: ICategoryBase, checked?: boolean): void => {
            let categories: string[] = angular.copy(this.$scope.filterParams.categories);
            let parentCategory: ICategoryBase = this.categoriesMap[category.uniqueId].parent;

            // add the category to selected categories list
            const categoryIdx = categories.indexOf(category.uniqueId);
            checked = (checked !== undefined) ? checked : categoryIdx === -1;
            if (checked && categoryIdx === -1) {
                categories.push(category.uniqueId);

                // check if all parent category children are checked, then check the parent category
                if (parentCategory) {
                    if (this.getParentCategoryChildren(parentCategory).every((ch) => categories.indexOf(ch.uniqueId) !== -1)) {
                        this.$scope.gui.onCategoryClick(parentCategory, true);
                        return;
                    }
                }

                categories = this.cleanSubsFromList(categories);
            } else if (!checked) {
                // if category exists, then remove it
                if (categoryIdx !== -1) {
                    categories.splice(categoryIdx, 1);
                }
                // else, if category doesn't exists, but one of its parent categories exists,
                // then remove that parent category and push all its children categories except the current
                else {
                    let prevParentCategory: ICategoryBase = category;
                    let additionalCategories: string[] = [];
                    while (parentCategory) {
                        // add parent category children to list for replacing the parent category (if will be found later)
                        additionalCategories = additionalCategories.concat(
                            this.getParentCategoryChildren(parentCategory)
                                .filter((ch) => ch.uniqueId !== prevParentCategory.uniqueId)
                                .map((ch) => ch.uniqueId));

                        const parentCategoryIdx = categories.indexOf(parentCategory.uniqueId);
                        if (parentCategoryIdx !== -1) {
                            categories.splice(parentCategoryIdx, 1);
                            categories = categories.concat(additionalCategories);
                            break;
                        } else {
                            prevParentCategory = parentCategory;
                            parentCategory = this.categoriesMap[parentCategory.uniqueId].parent;
                        }
                    }
                }
            }

            this.changeFilterParams({
                categories
            });
        };

        this.$scope.gui.onStatusClick = (statusKey: string, status: IConfigStatus, checked?: boolean) => {
            const statuses = angular.copy(this.$scope.filterParams.statuses);

            // add the status key to selected statuses list
            const statusIdx = statuses.indexOf(statusKey);
            checked = (checked !== undefined) ? checked : statusIdx === -1;
            if (checked && statusIdx === -1) {
                statuses.push(statusKey);
            } else if (!checked && statusIdx !== -1) {
                statuses.splice(statusIdx, 1);
            }

            this.changeFilterParams({
                statuses
            });
        };

        this.$scope.gui.changeFilterTerm = (filterTerm: string) => {
            this.changeFilterParams({
                term: filterTerm
            });
        };

        this.$scope.raiseNumberOfElementToDisplay = (): void => {
            this.$scope.numberOfItemToDisplay = this.$scope.numberOfItemToDisplay + 35;
            if (this.$scope.catalogFilterdItems) {
                this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.catalogFilterdItems.length;
            }
        };

    }

    private getAllCategoryChildrenIdsFlat(category:ICategoryBase) {
        let catChildrenIds = [];
        if ((<IMainCategory>category).subcategories) {
            catChildrenIds = (<IMainCategory>category).subcategories.reduce((acc, scat) => {
                    return acc.concat(this.getAllCategoryChildrenIdsFlat(scat));
                }, (<IMainCategory>category).subcategories.map((scat) => scat.uniqueId));
        }
        else if ((<ISubCategory>category).groupings) {
            catChildrenIds = (<ISubCategory>category).groupings.map((g) => g.uniqueId);
        }
        return catChildrenIds;
    }

    private getParentCategoryChildren(parentCategory:ICategoryBase): ICategoryBase[] {
        if ((<IMainCategory>parentCategory).subcategories) {
            return (<IMainCategory>parentCategory).subcategories;
        } else if ((<ISubCategory>parentCategory).groupings) {
            return (<ISubCategory>parentCategory).groupings;
        }
        return [];
    }

    private cleanSubsFromList(list:Array<string>, delimiter:string='.', removeSubsList?:Array<string>) {
        let curRemoveSubsList = (removeSubsList || list).slice().sort();  // by default remove any children of any item in list
        while (curRemoveSubsList.length) {
            const curRemoveSubItem = curRemoveSubsList.shift();
            const removeSubListFilter = (x) => !x.startsWith(curRemoveSubItem + delimiter);
            list = list.filter(removeSubListFilter);
            curRemoveSubsList = curRemoveSubsList.filter(removeSubListFilter);
        }
        return list;
    }

    private applyFilterParamsToView(filterParams:IFilterParams) {
        // reset checkboxes filter
        this.initCheckboxesFilter();

        this.applyFilterParamsComponents(filterParams);
        this.applyFilterParamsCategories(filterParams);
        this.applyFilterParamsStatuses(filterParams);
        this.applyFilterParamsOrder(filterParams);
        this.applyFilterParamsTerm(filterParams);
    }

    private applyFilterParamsComponents(filterParams:IFilterParams) {
        const componentList = [];
        const componentSubTypesLists = {};
        filterParams.components.forEach((compStr) => {
            const compWithSub = compStr.split('.', 2);
            const mainComp = compWithSub[0];
            const subComp = compWithSub[1];
            if (!subComp) {  // main component type
                componentList.push(mainComp);

                // if component type has sub types list, then add all component sub types
                const checkboxesSubTypeKey = mainComp.toLowerCase() + 'SubTypes';
                if (this.$scope.checkboxes.hasOwnProperty(checkboxesSubTypeKey)) {
                    componentSubTypesLists[mainComp] = angular.copy(this.$scope.checkboxes[checkboxesSubTypeKey]);
                }
            } else {  // sub component type
                // init component sub types list
                if (!componentSubTypesLists.hasOwnProperty(mainComp)) {
                    componentSubTypesLists[mainComp] = [];
                }
                // add sub type to list if not exist
                if (componentSubTypesLists[mainComp].indexOf(subComp) === -1) {
                    componentSubTypesLists[mainComp].push(subComp);
                }
            }
        });
        this.$scope.checkboxesFilter.selectedComponentTypes = componentList;
        Object.keys(componentSubTypesLists).forEach((tKey) => {
            const compSelectedSubTypeKey = 'selected' + tKey + 'SubTypes';
            if (this.$scope.checkboxesFilter.hasOwnProperty(compSelectedSubTypeKey)) {
                this.$scope.checkboxesFilter[compSelectedSubTypeKey] = componentSubTypesLists[tKey];
            }
        });

        let selectedCatalogIndex = filterParams.active ? CatalogSelectorTypes.Active : CatalogSelectorTypes.Archive;
        this.$scope.selectedCatalogItem = this.$scope.catalogSelectorItems[selectedCatalogIndex];
        
    }

    private applyFilterParamsCategories(filterParams:IFilterParams) {
        this.$scope.checkboxesFilter.selectedCategoriesModel = filterParams.categories.reduce((acc, c) => {
            acc.push(c);
            const cat = this.categoriesMap[c].category;
            if (cat) {
                acc = acc.concat(this.getAllCategoryChildrenIdsFlat(cat));
            }
            return acc;
        }, []);
    }

    private getActiveCatalogItems(forceReload?: boolean): void {

        if (forceReload || this.componentShouldReload()) {
            this.$scope.gui.isLoading = true;
            let onSuccess = (followedResponse:Array<Component>):void => {
                this.updateCatalogItems(followedResponse);
                this.$scope.gui.isLoading = false;
                this.cacheService.set('breadcrumbsComponentsState', this.$state.current.name);  //catalog
                this.cacheService.set('breadcrumbsComponents', followedResponse);
            };

            let onError = ():void => {
                console.info('Failed to load catalog CatalogViewModel::getActiveCatalogItems');
                this.$scope.gui.isLoading = false;
            };
            this.EntityService.getCatalog().then(onSuccess, onError);
        } else {
            let cachedComponents = this.cacheService.get('breadcrumbsComponents');
            this.updateCatalogItems(cachedComponents);
        }
    }

    private getArchiveCatalogItems(forceReload?: boolean): void {
        if(forceReload || !this.cacheService.contains("archiveComponents")) {
            this.$scope.gui.isLoading = true;
            let onSuccess = (followedResponse:Array<Component>):void => {
                this.cacheService.set("archiveComponents", followedResponse);
                this.updateCatalogItems(followedResponse);
                this.$scope.gui.isLoading = false;
            };
    
            let onError = ():void => {
                console.info('Failed to load catalog CatalogViewModel::getArchiveCatalogItems');
                this.$scope.gui.isLoading = false;
            };
    
            this.ArchiveService.getArchiveCatalog().subscribe(onSuccess, onError);
        } else {
            let archiveCache = this.cacheService.get("archiveComponents");
            this.updateCatalogItems(archiveCache);
        }

    }

    private updateCatalogItems = (items:Array<Component>):void => {
        this.$scope.catalogFilterdItems = items;
        this.$scope.isAllItemDisplay = this.$scope.numberOfItemToDisplay >= this.$scope.catalogFilterdItems.length;
        this.$scope.categories = this.cacheService.get('serviceCategories').concat(this.cacheService.get('resourceCategories'));
    }

    private componentShouldReload = ():boolean => {
        let breadcrumbsValid: boolean = (this.$state.current.name === this.cacheService.get('breadcrumbsComponentsState') && this.cacheService.contains('breadcrumbsComponents'));
        return !breadcrumbsValid || this.isDefaultFilter();
    }

    private isDefaultFilter = (): boolean => {
        return angular.equals(this.defaultFilterParams, this.$scope.filterParams);
    }

    private applyFilterParamsStatuses(filterParams: IFilterParams) {
        this.$scope.checkboxesFilter.selectedStatuses = filterParams.statuses.reduce((acc, stKey:string) => {
            const status = this.$scope.confStatus[stKey];
            if (status) {
                acc.push(status.values);
            }
            return acc;
        }, []);
    }

    private applyFilterParamsOrder(filterParams: IFilterParams) {
        this.$scope.sortBy = filterParams.order[0];
        this.$scope.reverse = filterParams.order[1];
    }

    private applyFilterParamsTerm(filterParams: IFilterParams) {
        this.$scope.search = {
            filterTerm: filterParams.term
        };
    }

    private loadFilterParams() {
        const params = this.$state.params;
        this.$scope.filterParams = angular.copy(this.defaultFilterParams);
        Object.keys(params).forEach((k) => {
            if (!angular.isUndefined(params[k])) {
                let newVal;
                let filterKey = k.substr('filter.'.length);
                switch (k) {
                    case 'filter.components':
                    case 'filter.categories':
                        newVal = _.uniq(params[k].split(','));
                        newVal = this.cleanSubsFromList(newVal);
                        break;
                    case 'filter.statuses':
                        newVal = _.uniq(params[k].split(','));
                        break;
                    case 'filter.order':
                        newVal = params[k].startsWith('-') ? [params[k].substr(1), true] : [params[k], false];
                        break;
                    case 'filter.term':
                        newVal = params[k];
                        break;
                    case 'filter.active':
                        newVal = (params[k] === "true" || params[k] === true);
                        break;
                    default:
                        // unknown filter key
                        filterKey = null;
                }
                if (filterKey) {
                    this.$scope.filterParams[filterKey] = newVal;
                }
            }
        });
        // re-set filter params with valid values
        this.applyFilterParamsToView(this.$scope.filterParams);

    }

    private changeFilterParams(changedFilterParams) {
        const newParams = {};
        Object.keys(changedFilterParams).forEach((k) => {
            let newVal;
            switch (k) {
                case 'components':
                case 'categories':
                case 'statuses':
                    newVal = changedFilterParams[k] && changedFilterParams[k].length ? changedFilterParams[k].join(',') : null;
                    break;
                case 'order':
                    newVal = (changedFilterParams[k][1] ? '-' : '') + changedFilterParams[k][0];
                    break;
                case 'term':
                    newVal = changedFilterParams[k] ? changedFilterParams[k] : null;
                    break;
                case 'active':
                    newVal = changedFilterParams[k];
                    break;
                default:
                    return;
            }
            this.$scope.filterParams[k] = changedFilterParams[k];
            newParams['filter.' + k] = newVal;
        });
        this.$state.go('.', newParams, {location: 'replace', notify: false}).then(() => {
            this.applyFilterParamsToView(this.$scope.filterParams);
        });
    }
}
