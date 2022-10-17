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

import * as _ from "lodash";
import { Component as NgComponent, Inject } from '@angular/core';
import { SdcUiCommon, SdcUiServices } from "onap-ui-angular";
import { CacheService, CatalogService } from "app/services-ng2";
import { SdcConfigToken, ISdcConfig } from "../../config/sdc-config.config";
import { SdcMenuToken, IAppMenu } from "../../config/sdc-menu.config";
import {
    Component,
    ICategoryBase,
    IMainCategory,
    ISubCategory,
    IConfigStatuses,
    ICatalogSelector,
    CatalogSelectorTypes,
    DataTypeModel
} from "app/models";
import { ResourceNamePipe } from "../../pipes/resource-name.pipe";
import { EntityFilterPipe, IEntityFilterObject, ISearchFilter} from "../../pipes/entity-filter.pipe";
import {DEFAULT_MODEL_NAME, States} from "app/utils/constants";

interface Gui {
    onComponentSubTypesClick:Function;
    onComponentTypeClick:Function;
    onCategoryClick:Function;
    onStatusClick:Function;
    onModelClick:Function;
    changeFilterTerm:Function;
}

interface IFilterParams {
    components: string[];
    categories: string[];
    statuses: (string)[];
    models: string[];
    order: [string, boolean];
    term: string;
    active: boolean;
}

interface ICheckboxesFilterMap {
    [key: string]: Array<string>;
    _main: Array<string>;
}

interface ICheckboxesFilterKeys {
    componentTypes: ICheckboxesFilterMap;
    categories: ICheckboxesFilterMap;
    statuses: ICheckboxesFilterMap;
    models: ICheckboxesFilterMap;
}

interface ICategoriesMap {
    [key: string]: {
        category: ICategoryBase,
        parent: ICategoryBase
    }
}

@NgComponent({
    selector: 'catalog',
    templateUrl: './catalog.component.html',
    styleUrls:['./catalog.component.less']
})
export class CatalogComponent {
    public checkboxesFilter:IEntityFilterObject;
    public checkboxesFilterKeys:ICheckboxesFilterKeys;
    public gui:Gui;
    public categories:Array<IMainCategory>;
    public models: Array<any> = new Array();
    public filteredCategories:Array<IMainCategory>;
    public confStatus:IConfigStatuses;
    public componentTypes:{[key:string]: Array<string>};
    public catalogItems:Array<Component | DataTypeModel>;
    public catalogFilteredItems:Array<Component | DataTypeModel>;
    public catalogFilteredSlicedItems:Array<Component | DataTypeModel>;
    public expandedSection:Array<string>;
    public version:string;
    public sortBy:string;
    public reverse:boolean;
    public filterParams:IFilterParams;
    public search:ISearchFilter;

    //this is for UI paging
    public numberOfItemToDisplay:number;

    public selectedCatalogItem: ICatalogSelector;
    public catalogSelectorItems: Array<ICatalogSelector>;
    public showCatalogSelector: boolean;

    public typesChecklistModel: SdcUiCommon.ChecklistModel;
    public categoriesChecklistModel: SdcUiCommon.ChecklistModel;
    public statusChecklistModel: SdcUiCommon.ChecklistModel;
    public modelsChecklistModel: SdcUiCommon.ChecklistModel;

    private defaultFilterParams:IFilterParams = {
        components: [],
        categories: [],
        statuses: [],
        models: [],
        order: ['lastUpdateDate', true],
        term: '',
        active: true
    };
    private categoriesMap:ICategoriesMap;

    constructor(
        @Inject(SdcConfigToken) private sdcConfig:ISdcConfig,
        @Inject(SdcMenuToken) public sdcMenu:IAppMenu,
        @Inject("$state") private $state:ng.ui.IStateService,
        private cacheService:CacheService,
        private catalogService:CatalogService,
        private resourceNamePipe:ResourceNamePipe,
        private loaderService: SdcUiServices.LoaderService
    ) {}

    ngOnInit(): void {
        this.initGui();
        this.initLeftSwitch();
        this.initScopeMembers();
        this.loadFilterParams();
        this.initCatalogData(); // Async task to get catalog from server.
    }

    private initLeftSwitch = ():void => {
        this.showCatalogSelector = false;

        this.catalogSelectorItems = [
            {value: CatalogSelectorTypes.Active, title: "Active Items", header: "Active"},
            {value: CatalogSelectorTypes.Archive, title: "Archive", header: "Archived"}
        ];
        // set active items is default
        this.selectedCatalogItem = this.catalogSelectorItems[0];
    };

    private initCatalogData = ():void => {
        if(this.selectedCatalogItem.value === CatalogSelectorTypes.Archive){
            this.getArchiveCatalogItems();
        } else {
            this.getActiveCatalogItems();
        }
    };


    private initScopeMembers = ():void => {
        this.numberOfItemToDisplay = 0;
        this.categories = this.makeSortedCategories(this.cacheService.get('serviceCategories').concat(this.cacheService.get('resourceCategories')))
            .map((cat) => <IMainCategory>cat);

        var modelList = this.cacheService.get('models');
        modelList.sort((o:any, o1:any) => new String(o.modelType).localeCompare(o1.modelType));
        modelList.forEach(m => {
            if (m.derivedFrom) {
                this.models[m.derivedFrom].push(m.name);
            } else {
                this.models[m.name] = [];
            }
        });
        this.models[DEFAULT_MODEL_NAME] = [];
        this.confStatus = this.sdcMenu.statuses;
        this.expandedSection = ["type", "category", "status", "model"];
        this.catalogItems = [];
        this.search = {FilterTerm: ""};
        this.categoriesMap = this.initCategoriesMap();
        this.initCheckboxesFilter();
        this.initCheckboxesFilterKeys();
        this.buildCheckboxLists();

        this.version = this.cacheService.get('version');
        this.sortBy = 'lastUpdateDate';
        this.reverse = true;
    };

    private buildCheckboxLists() {
        this.buildChecklistModelForTypes();
        this.buildChecklistModelForCategories();
        this.buildChecklistModelForStatuses();
        this.buildChecklistModelForModels();
    }

    private getTestIdForCheckboxByText = ( text: string ):string => {
        return 'checkbox-' + text.toLowerCase().replace(/ /g, '');
    }

    private buildChecklistModelForTypes() {
        this.componentTypes = {
            Resource: ['VF', 'VFC', 'CR', 'PNF', 'CP', 'VL'],
            Service: null,
            TOSCA_Type: ['Data Type']
        };
        this.typesChecklistModel = new SdcUiCommon.ChecklistModel(this.checkboxesFilterKeys.componentTypes._main,
            Object.keys(this.componentTypes).map((ct) => {
                let subChecklist = null;
                if (this.componentTypes[ct]) {
                    this.checkboxesFilterKeys.componentTypes[ct] = this.checkboxesFilterKeys.componentTypes[ct] || [];
                    subChecklist = new SdcUiCommon.ChecklistModel(this.checkboxesFilterKeys.componentTypes[ct],
                        this.componentTypes[ct].map((st) => {
                            const stKey = [ct, st].join('.');
                            const testId = this.getTestIdForCheckboxByText(st);
                            return new SdcUiCommon.ChecklistItemModel(st, false, this.checkboxesFilterKeys.componentTypes[ct].indexOf(stKey) !== -1, null, testId, stKey);
                        })
                    );
                }
                const testId = this.getTestIdForCheckboxByText(ct);
                return new SdcUiCommon.ChecklistItemModel(ct, false, this.checkboxesFilterKeys.componentTypes._main.indexOf(ct) !== -1, subChecklist, testId, ct);
            })
        );
    }

    private buildChecklistModelForCategories() {
        this.categoriesChecklistModel = new SdcUiCommon.ChecklistModel(this.checkboxesFilterKeys.categories._main,
            (this.filteredCategories || this.categories).map((cat) => {
                this.checkboxesFilterKeys.categories[cat.uniqueId] = this.checkboxesFilterKeys.categories[cat.uniqueId] || [];
                const subCategoriesChecklistModel = new SdcUiCommon.ChecklistModel(this.checkboxesFilterKeys.categories[cat.uniqueId],
                    (cat.subcategories || []).map((scat) => {
                        this.checkboxesFilterKeys.categories[scat.uniqueId] = this.checkboxesFilterKeys.categories[scat.uniqueId] || [];
                        const groupingsChecklistModel = new SdcUiCommon.ChecklistModel(this.checkboxesFilterKeys.categories[scat.uniqueId],
                            (scat.groupings || []).map(gcat =>
                                new SdcUiCommon.ChecklistItemModel(gcat.name, false, this.checkboxesFilterKeys.categories[scat.uniqueId].indexOf(gcat.uniqueId) !== -1, null, this.getTestIdForCheckboxByText(gcat.uniqueId), gcat.uniqueId))
                        );
                        return new SdcUiCommon.ChecklistItemModel(scat.name, false, this.checkboxesFilterKeys.categories[cat.uniqueId].indexOf(scat.uniqueId) !== -1, groupingsChecklistModel, this.getTestIdForCheckboxByText(scat.uniqueId), scat.uniqueId);
                    })
                );
                return new SdcUiCommon.ChecklistItemModel(cat.name, false, this.checkboxesFilterKeys.categories._main.indexOf(cat.uniqueId) !== -1, subCategoriesChecklistModel, this.getTestIdForCheckboxByText(cat.uniqueId), cat.uniqueId);
            })
        );
    }

    private buildChecklistModelForModels() {
        this.modelsChecklistModel = new SdcUiCommon.ChecklistModel(this.checkboxesFilterKeys.models._main,
            Object.keys(this.models).map((modelName) => {
                var modelList = this.models[modelName];
                modelList.unshift(modelName);
                return new SdcUiCommon.ChecklistItemModel(
                    modelName,
                    false,
                    this.checkboxesFilterKeys.models._main.indexOf(modelName) !== -1,
                    null,
                    this.getTestIdForCheckboxByText(modelName),
                    modelList);
        }));
    }

    private buildChecklistModelForStatuses() {
        // For statuses checklist model, use the statuses keys as values. On applying filtering map the statuses keys to statuses values.
        this.statusChecklistModel = new SdcUiCommon.ChecklistModel(this.checkboxesFilterKeys.statuses._main,
            Object.keys(this.confStatus).map((sKey) => new SdcUiCommon.ChecklistItemModel(
                this.confStatus[sKey].name, 
                false, 
                this.checkboxesFilterKeys.statuses._main.indexOf(sKey) !== -1, 
                null, 
                this.getTestIdForCheckboxByText(sKey), 
                sKey))
        );
    }

    private initCheckboxesFilter() {
        // Checkboxes filter init
        this.checkboxesFilter = <IEntityFilterObject>{};
        this.checkboxesFilter.selectedComponentTypes = [];
        this.checkboxesFilter.selectedResourceSubTypes = [];
        this.checkboxesFilter.selectedToscaSubTypes = [];
        this.checkboxesFilter.selectedCategoriesModel = [];
        this.checkboxesFilter.selectedStatuses = [];
        this.checkboxesFilter.selectedModels = [];
    }

    private initCheckboxesFilterKeys() {
        // init checkboxes filter keys (for checklists values):
        this.checkboxesFilterKeys = <ICheckboxesFilterKeys>{};
        this.checkboxesFilterKeys.componentTypes = { _main: [] };
        this.checkboxesFilterKeys.categories = { _main: [] };
        this.checkboxesFilterKeys.statuses = { _main: [] };
        this.checkboxesFilterKeys.models = { _main: [] };
    }

    private initCategoriesMap(categoriesList?:(ICategoryBase)[], parentCategory:ICategoryBase=null): ICategoriesMap {
        categoriesList = (categoriesList) ? categoriesList : this.categories;

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

    public selectLeftSwitchItem(item: ICatalogSelector): void {
        if (this.selectedCatalogItem.value !== item.value) {
            this.selectedCatalogItem = item;
            switch (item.value) {
                case CatalogSelectorTypes.Active:
                    this.getActiveCatalogItems(true);
                    break;

                case CatalogSelectorTypes.Archive:
                    this.getArchiveCatalogItems(true);
                    break;
            }
            this.changeFilterParams({active: (item.value === CatalogSelectorTypes.Active)});
        }
    }

    public sectionClick(section: string): void {
        let index: number = this.expandedSection.indexOf(section);
        if (index !== -1) {
            this.expandedSection.splice(index, 1);
        } else {
            this.expandedSection.push(section);
        }
    }


    private makeFilterParamsFromCheckboxes(checklistModel:SdcUiCommon.ChecklistModel): Array<string> {
        return checklistModel.checkboxes.reduce((acc, chbox) => {
            if (checklistModel.selectedValues.indexOf(chbox.value) !== -1) {
                acc.push(chbox.value);
            } else if (chbox.subLevelChecklist) {  // else, if checkbox is not checked, then try to get values from sub checklists
                acc.push(...this.makeFilterParamsFromCheckboxes(chbox.subLevelChecklist));
            }
            return acc;
        }, []);
    }

    //default sort by descending last update. default for alphabetical = ascending
    public order(sortBy: string): void {
        this.changeFilterParams({
            order: (this.filterParams.order[0] === sortBy)
                ? [sortBy, !this.filterParams.order[1]]
                : [sortBy, sortBy === 'lastUpdateDate']
        });
    }


    public goToComponent(component: Component | DataTypeModel): void {
        if (component instanceof DataTypeModel) {
            this.$state.go(States.TYPE_WORKSPACE,  {type: component.getSubToscaType().toLowerCase(), id: component.uniqueId});
        } else {
            this.$state.go(States.WORKSPACE_GENERAL, {id: component.uniqueId, type: component.componentType.toLowerCase()});
        }
    }


    // Will print the number of elements found in catalog
    public getNumOfElements(num:number):string {
        if (!num || num === 0) {
            return `No <b>${this.selectedCatalogItem.header}</b> Elements found`;
        } else if (num === 1) {
            return `1 <b>${this.selectedCatalogItem.header}</b> Element found`;
        } else {
            return num + ` <b>${this.selectedCatalogItem.header}</b> Elements found`;
        }
    }

    public initGui(): void {
        this.gui = <Gui>{};

        /**
         * Select | unselect sub resource when resource is clicked | unclicked.
         * @param type
         */
        this.gui.onComponentTypeClick = (): void => {
            this.changeFilterParams({
                components: this.makeFilterParamsFromCheckboxes(this.typesChecklistModel)
            });
        };

        this.gui.onCategoryClick = (): void => {
            this.changeFilterParams({
                categories: this.makeFilterParamsFromCheckboxes(this.categoriesChecklistModel)
            });
        };

        this.gui.onStatusClick = (statusChecklistItem: SdcUiCommon.ChecklistItemModel) => {
            this.changeFilterParams({
                statuses: this.makeFilterParamsFromCheckboxes(this.statusChecklistModel)
            });
        };

        this.gui.changeFilterTerm = (filterTerm: string) => {
            this.changeFilterParams({
                term: filterTerm
            });
        };
 
        this.gui.onModelClick = (): void => {
            this.changeFilterParams({
                models: this.makeFilterParamsFromCheckboxes(this.modelsChecklistModel)
            });
        };
    }

    public raiseNumberOfElementToDisplay(recalculate:boolean = false): void {
        const scrollPageAmount = 35;
        if (!this.catalogFilteredItems) {
            this.numberOfItemToDisplay = 0;
        } else if (this.catalogFilteredItems.length > this.numberOfItemToDisplay || recalculate) {
            let fullPagesAmount = Math.ceil(this.numberOfItemToDisplay / scrollPageAmount) * scrollPageAmount;
            if (!recalculate || fullPagesAmount === 0) {  //TODO trigger infiniteScroll to check bottom and fire onBottomHit by itself (sdc-ui)
                fullPagesAmount += scrollPageAmount;
            }
            this.numberOfItemToDisplay = Math.min(this.catalogFilteredItems.length, fullPagesAmount);
            this.catalogFilteredSlicedItems = this.catalogFilteredItems.slice(0, this.numberOfItemToDisplay);
        }
    }

    private isDefaultFilter = (): boolean => {
        return angular.equals(this.defaultFilterParams, this.filterParams);
    }

    private componentShouldReload = ():boolean => {
        let breadcrumbsValid: boolean = (this.$state.current.name === this.cacheService.get('breadcrumbsComponentsState') && this.cacheService.contains('breadcrumbsComponents'));
        return !breadcrumbsValid || this.isDefaultFilter();
    }

    private getActiveCatalogItems(forceReload?: boolean): void {
        if (forceReload || this.componentShouldReload()) {
            this.loaderService.activate();

            let onSuccess = (followedResponse:Array<Component | DataTypeModel>):void => {
                this.updateCatalogItems(followedResponse);
                this.loaderService.deactivate();
                this.cacheService.set('breadcrumbsComponentsState', this.$state.current.name);  //catalog
                this.cacheService.set('breadcrumbsComponents', followedResponse);
                
            };

            let onError = ():void => {
                console.info('Failed to load catalog CatalogViewModel::getActiveCatalogItems');
                this.loaderService.deactivate();
            };
            this.catalogService.getCatalog().subscribe(onSuccess, onError);
        } else {
            let cachedComponents = this.cacheService.get('breadcrumbsComponents');
            this.updateCatalogItems(cachedComponents);
        }
    }

    private getArchiveCatalogItems(forceReload?: boolean): void {
        if(forceReload || !this.cacheService.contains("archiveComponents")) {
            this.loaderService.activate();
            let onSuccess = (followedResponse:Array<Component>):void => {
                this.cacheService.set("archiveComponents", followedResponse);
                this.loaderService.deactivate();
                this.updateCatalogItems(followedResponse);
            };

            let onError = ():void => {
                console.info('Failed to load catalog CatalogViewModel::getArchiveCatalogItems');
                this.loaderService.deactivate();
            };

            this.catalogService.getArchiveCatalog().subscribe(onSuccess, onError);
        } else {
            let archiveCache = this.cacheService.get("archiveComponents");
            this.updateCatalogItems(archiveCache);
        }
    }

    private updateCatalogItems = (items:Array<Component | DataTypeModel>):void => {
        this.catalogItems = items;
        this.catalogItems.forEach(this.addFilterTermToComponent);
        this.filterCatalogItems();
    }

    private applyFilterParamsToView(filterParams:IFilterParams) {
        // reset checkboxes filter
        this.initCheckboxesFilter();

        this.filterCatalogCategories();

        this.applyFilterParamsComponents(filterParams);
        this.applyFilterParamsCategories(filterParams);
        this.applyFilterParamsStatuses(filterParams);
        this.applyFilterParamsModels(filterParams);
        this.applyFilterParamsOrder(filterParams);
        this.applyFilterParamsTerm(filterParams);

        // do filters when filter params are changed:
        this.filterCatalogItems();
    }

    private filterCatalogCategories() {
        this.filteredCategories = this.makeFilteredCategories(this.categories, this.checkboxesFilter.selectedComponentTypes);
        this.buildChecklistModelForCategories();
    }

    private filterCatalogItems() {
        this.catalogFilteredItems = this.makeFilteredItems(this.catalogItems, this.checkboxesFilter, this.search, this.sortBy, this.reverse);
        this.raiseNumberOfElementToDisplay(true);
        this.catalogFilteredSlicedItems = this.catalogFilteredItems.slice(0, this.numberOfItemToDisplay);
    }

    private applyFilterParamsToCheckboxes(checklistModel:SdcUiCommon.ChecklistModel, filterParamsList:Array<string>) {
        checklistModel.checkboxes.forEach((chbox) => {
            // if checkbox is checked, then add it to selected values if not there, and select all sub checkboxes
            if (filterParamsList.indexOf(chbox.value) !== -1 && checklistModel.selectedValues.indexOf(chbox.value) === -1) {
                checklistModel.selectedValues.push(chbox.value);
                if (chbox.subLevelChecklist) {
                    this.applyFilterParamsToCheckboxes(chbox.subLevelChecklist, chbox.subLevelChecklist.checkboxes.map((subchbox) => subchbox.value));
                }
            } else if ( chbox.subLevelChecklist ) {
                this.applyFilterParamsToCheckboxes(chbox.subLevelChecklist, filterParamsList);
            }
        });
    }

    private applyFilterParamsComponents(filterParams:IFilterParams) {
        this.applyFilterParamsToCheckboxes(this.typesChecklistModel, filterParams.components);
        this.checkboxesFilter.selectedComponentTypes = this.checkboxesFilterKeys.componentTypes._main;
        Object.keys(this.checkboxesFilterKeys.componentTypes).forEach((chKey) => {
            if (chKey !== '_main') {
                if (chKey === 'TOSCA_Type') {
                    this.checkboxesFilter['selectedToscaSubTypes'] = this.checkboxesFilterKeys.componentTypes[chKey].map((st) => st.substr(chKey.length + 1));
                } else if (chKey === 'Resource') {
                    this.checkboxesFilter['selected' + chKey + 'SubTypes'] = this.checkboxesFilterKeys.componentTypes[chKey].map((st) => st.substr(chKey.length + 1));
                }
            }
        });

        let selectedCatalogIndex = filterParams.active ? CatalogSelectorTypes.Active : CatalogSelectorTypes.Archive;
        this.selectedCatalogItem = this.catalogSelectorItems[selectedCatalogIndex];
    }

    private applyFilterParamsCategories(filterParams:IFilterParams) {
        this.applyFilterParamsToCheckboxes(this.categoriesChecklistModel, filterParams.categories);
        this.checkboxesFilter.selectedCategoriesModel = <Array<string>>_.flatMap(this.checkboxesFilterKeys.categories);
    }

    private applyFilterParamsStatuses(filterParams: IFilterParams) {
        this.applyFilterParamsToCheckboxes(this.statusChecklistModel, filterParams.statuses);
        this.checkboxesFilter.selectedStatuses = _.reduce(_.flatMap(this.checkboxesFilterKeys.statuses), (stats, st:string) => [...stats, ...this.confStatus[st].values], []);
    }

    private applyFilterParamsModels(filterParams: IFilterParams) {
        this.applyFilterParamsToCheckboxes(this.modelsChecklistModel, filterParams.models);
        this.checkboxesFilter.selectedModels = _.flatMap(this.checkboxesFilterKeys.models);
    }

    private applyFilterParamsOrder(filterParams: IFilterParams) {
        this.sortBy = filterParams.order[0];
        this.reverse = filterParams.order[1];
    }

    private applyFilterParamsTerm(filterParams: IFilterParams) {
        this.search = {
            filterTerm: filterParams.term
        };
    }

    private loadFilterParams() {
        const params = this.$state.params;
        this.filterParams = angular.copy(this.defaultFilterParams);
        Object.keys(params).forEach((k) => {
            if (!angular.isUndefined(params[k])) {
                let newVal;
                let paramsChecklist: SdcUiCommon.ChecklistModel = null;
                let filterKey = k.substr('filter.'.length);
                switch (k) {
                    case 'filter.components':
                        paramsChecklist = paramsChecklist || this.typesChecklistModel;
                    case 'filter.categories':
                        paramsChecklist = paramsChecklist || this.categoriesChecklistModel;
                    case 'filter.statuses':
                        paramsChecklist = paramsChecklist || this.statusChecklistModel;
                    case 'filter.models':
                        paramsChecklist = paramsChecklist || this.modelsChecklistModel;

                        // for those cases above - split param by comma and make reduced checklist values for filter params (url)
                        newVal = _.uniq(params[k].split(','));
                        break;
                    case 'filter.order':
                        newVal = params[k].startsWith('-') ? [params[k].substr(1), true] : [params[k], false];
                        break;
                    case 'filter.term':
                        newVal = params[k];
                        break;
                    case 'filter.active':
                        newVal = (params[k] === "true" || params[k] === true)? true : false;
                        break;
                    default:
                        // unknown filter key
                        filterKey = null;
                }
                if (filterKey) {
                    this.filterParams[filterKey] = newVal;
                }
            }
        });
        // re-set filter params with valid values, and then re-build checklists
        this.changeFilterParams(this.filterParams, true);
    }

    private changeFilterParams(changedFilterParams, rebuild:boolean = false) {
        const newParams = {};
        Object.keys(changedFilterParams).forEach((k) => {
            let newVal;
            switch (k) {
                case 'components':
                case 'categories':
                case 'statuses':
                case 'models':
                    newVal = changedFilterParams[k] && changedFilterParams[k].length ? changedFilterParams[k].join(',') : null;
                    break;
                case 'order':
                    newVal = (changedFilterParams[k][1] ? '-' : '') + changedFilterParams[k][0];
                    break;
                case 'term':
                    newVal = changedFilterParams[k] ? changedFilterParams[k] : null;
                    break;
                case 'active':
                    newVal = (changedFilterParams[k] === "true" || changedFilterParams[k] === true);
                    break;
                default:
                    return;
            }
            this.filterParams[k] = changedFilterParams[k];
            newParams['filter.' + k] = newVal;
        });
        this.$state.go('.', newParams, {location: 'replace', notify: false}).then(() => {
            if (rebuild) {
                // fix the filter params to only valid values for checkboxes
                this.changeFilterParams({
                    components: this.makeFilterParamsFromCheckboxes(this.typesChecklistModel),
                    categories: this.makeFilterParamsFromCheckboxes(this.categoriesChecklistModel),
                    statuses: this.makeFilterParamsFromCheckboxes(this.statusChecklistModel),
                    models: this.makeFilterParamsFromCheckboxes(this.modelsChecklistModel)
                });
                // rebuild the checkboxes to show selected
                this.buildCheckboxLists();
            }
        }).catch(function (err) {
            console.log(err.name + ":", err.message);
        });
        this.applyFilterParamsToView(this.filterParams);
    }

    private makeFilteredCategories(categories:Array<IMainCategory>, selectedTypes:Array<string>=[]): Array<IMainCategory> {
        let filteredCategories = categories.slice();

        const filteredMainTypes = selectedTypes.reduce((acc, st) => {
            const mainType = st.split('.')[0];
            if (acc.indexOf(mainType) === -1) {
                acc.push(mainType);
            }
            return acc;
        }, []);

        // filter by selected types
        if (filteredMainTypes.length) {
            const filteredTypesCategories = filteredMainTypes.reduce((acc, mainType: string) => {
                acc.push(...this.cacheService.get(mainType.toLowerCase() + 'Categories'));
                return acc;
            }, []);

            filteredCategories = _.intersectionBy(filteredCategories, filteredTypesCategories, c => c.uniqueId);
        }

        return filteredCategories;
    }

    private makeSortedCategories(categories:Array<IMainCategory|ISubCategory|ICategoryBase>, sortBy?:any): Array<IMainCategory|ISubCategory|ICategoryBase> {
        sortBy = (sortBy !== undefined) ? sortBy : ['name'];
        let sortedCategories = categories.map(cat => Object.assign({}, cat));  // copy each object in the array
        sortedCategories = _.sortBy(sortedCategories, sortBy);

        // inner sort of subcategories and groupings
        sortedCategories.forEach((cat) => {
            if ('subcategories' in cat && cat['subcategories'] && cat['subcategories'].length > 0) {
                cat['subcategories'] = this.makeSortedCategories(cat['subcategories'], sortBy);
            }
            if ('groupings' in cat && cat['groupings'] && cat['groupings'].length > 0) {
                cat['groupings'] = this.makeSortedCategories(cat['groupings'], sortBy);
            }
        });

        return sortedCategories;
    }

    private addFilterTermToComponent(component:Component | DataTypeModel) {
        if (component instanceof Component) {
            component.filterTerm = component.name + ' ' + component.description + ' ' + component.tags.toString() + ' ' + component.version;
            component.filterTerm = component.filterTerm.toLowerCase();
        }
    }

    private makeFilteredItems(catalogItems:Array<Component | DataTypeModel>, filter:IEntityFilterObject, search:ISearchFilter, sortBy:string, reverse:boolean) {
        let filteredComponents:Array<Component | DataTypeModel> = catalogItems;

        // common entity filter
        // --------------------------------------------------------------------------
        filter = Object.assign({ search }, filter);  // add search to entity filter object
        filteredComponents = EntityFilterPipe.transform(filteredComponents, filter);

        // sort
        // --------------------------------------------------------------------------
        if (sortBy) {
            switch (sortBy) {
                case 'resourceName':
                    filteredComponents = _.sortBy(filteredComponents, cat => this.resourceNamePipe.transform(cat.name));
                    break;
                default:
                    filteredComponents = _.sortBy(filteredComponents, [sortBy]);
            }
            if (reverse) {
                _.reverse(filteredComponents);
            }
        }
        return filteredComponents;
    }
}
