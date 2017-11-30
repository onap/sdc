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
import {Component, IAppMenu, LeftPanelModel, NodesFactory, LeftPaletteComponent, CompositionCiNodeBase, ComponentInstance} from "app/models";
import {CompositionGraphGeneralUtils} from "../composition-graph/utils/composition-graph-general-utils";
import {EventListenerService} from "app/services";
import {ResourceType, GRAPH_EVENTS, EVENTS, ComponentInstanceFactory, ModalsHandler} from "app/utils";
import 'sdc-angular-dragdrop';
import {LeftPaletteLoaderService} from "../../../services/components/utils/composition-left-palette-service";
import {Resource} from "app/models/components/resource";
import {ComponentType} from "app/utils/constants";

interface IPaletteScope {
    components:Array<LeftPaletteComponent>;
    currentComponent:Component;
    model:any;
    displaySortedCategories:any;
    expandedSection:string;
    dragElement:JQuery;
    dragbleNode:{
        event:JQueryEventObject,
        components:LeftPaletteComponent,
        ui:any
    }

    sectionClick:(section:string)=>void;
    searchComponents:(searchText:string)=>void;
    onMouseOver:(displayComponent:LeftPaletteComponent)=>void;
    onMouseOut:(displayComponent:LeftPaletteComponent)=>void;
    dragStartCallback:(event:JQueryEventObject, ui, displayComponent:LeftPaletteComponent)=>void;
    dragStopCallback:()=>void;
    onDragCallback:(event:JQueryEventObject) => void;

    setElementTemplate:(e:JQueryEventObject)=>void;

    isOnDrag:boolean;
    isDragable:boolean;
    isLoading:boolean;
    isViewOnly:boolean;
}

export class Palette implements ng.IDirective {
    constructor(private $log:ng.ILogService,
                private LeftPaletteLoaderService: LeftPaletteLoaderService,
                private sdcConfig,
                private ComponentFactory,
                private ComponentInstanceFactory:ComponentInstanceFactory,
                private NodesFactory:NodesFactory,
                private CompositionGraphGeneralUtils:CompositionGraphGeneralUtils,
                private EventListenerService:EventListenerService,
                private sdcMenu:IAppMenu,
                private ModalsHandler:ModalsHandler) {

    }

    private fetchingComponentFromServer:boolean = false;
    private nodeHtmlSubstitute:JQuery;

    scope = {
        currentComponent: '=',
        isViewOnly: '=',
        isLoading: '='
    };
    restrict = 'E';
    template = require('./palette.html');

    link = (scope:IPaletteScope, el:JQuery) => {
        this.nodeHtmlSubstitute = $('<div class="node-substitute"><span></span><img /></div>');
        el.append(this.nodeHtmlSubstitute);
        this.registerEventListenerForLeftPalette(scope);
        // this.LeftPaletteLoaderService.loadLeftPanel(scope.currentComponent.componentType);

        this.initComponents(scope);
        this.initEvents(scope);
        this.initDragEvents(scope);
        this._initExpandedSection(scope, '');
        el.on('$destroy', ()=> {
            //remove listener of download event
            this.unRegisterEventListenerForLeftPalette(scope);
        });
    };

    private getUpdateLeftPaletteEventName = (component:Component):string => {
        switch (component.componentType) {
            case ComponentType.SERVICE:
                return EVENTS.SERVICE_LEFT_PALETTE_UPDATE_EVENT;
            case ComponentType.RESOURCE:
                if((<Resource>component).resourceType == ResourceType.PNF){
                    return EVENTS.RESOURCE_PNF_LEFT_PALETTE_UPDATE_EVENT;
                }else{
                    return EVENTS.RESOURCE_LEFT_PALETTE_UPDATE_EVENT;
                }
            default:
                console.log('ERROR: Component type '+ component.componentType + ' is not exists');
        }
    };

    private registerEventListenerForLeftPalette = (scope:IPaletteScope):void => {
        let updateEventName:string = this.getUpdateLeftPaletteEventName(scope.currentComponent);
        this.EventListenerService.registerObserverCallback(updateEventName, () => {
            this.updateLeftPanelDisplay(scope);
        });
    };

    private unRegisterEventListenerForLeftPalette = (scope:IPaletteScope):void => {
        let updateEventName:string = this.getUpdateLeftPaletteEventName(scope.currentComponent);
        this.EventListenerService.unRegisterObserver(updateEventName);
    };

    private leftPanelResourceFilter(resourcesNotAbstract:Array<LeftPaletteComponent>, resourceFilterTypes:Array<string>):Array<LeftPaletteComponent> {
        let filterResources = _.filter(resourcesNotAbstract, (component) => {
            return resourceFilterTypes.indexOf(component.getComponentSubType()) > -1;
        });
        return filterResources;
    }

    private initLeftPanel(leftPanelComponents:Array<LeftPaletteComponent>, resourceFilterTypes:Array<string>):LeftPanelModel {
        let leftPanelModel = new LeftPanelModel();
       
        if (resourceFilterTypes && resourceFilterTypes.length) {
            leftPanelComponents = this.leftPanelResourceFilter(leftPanelComponents, resourceFilterTypes);
        }
        leftPanelModel.numberOfElements = leftPanelComponents && leftPanelComponents.length || 0;

        if (leftPanelComponents && leftPanelComponents.length) {

            let categories:any = _.groupBy(leftPanelComponents, 'mainCategory');
            for (let category in categories)
                categories[category] = _.groupBy(categories[category], 'subCategory');

            leftPanelModel.sortedCategories = categories;
        }
        return leftPanelModel;
    }


    private initEvents(scope:IPaletteScope) {
        /**
         *
         * @param section
         */
        scope.sectionClick = (section:string) => {
            if (section === scope.expandedSection) {
                scope.expandedSection = '';
                return;
            }
            scope.expandedSection = section;
        };

        scope.onMouseOver = (displayComponent:LeftPaletteComponent) => {
            if (scope.isOnDrag) {
                return;
            }
            scope.isOnDrag = true;

            this.EventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_IN, displayComponent);
            this.$log.debug('palette::onMouseOver:: fired');
        };

        scope.onMouseOut = () => {
            scope.isOnDrag = false;
            this.EventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_OUT);
        }
    }

    private initComponents(scope:IPaletteScope) {
        scope.searchComponents = (searchText:any):void => {
            scope.displaySortedCategories = this._searchComponents(searchText, scope.model.sortedCategories);
            this._initExpandedSection(scope, searchText);
        };

        scope.isDragable = scope.currentComponent.isComplex();
        this.updateLeftPanelDisplay(scope);
    }

    private updateLeftPanelDisplay(scope:IPaletteScope) {
        let entityType:string = scope.currentComponent.componentType.toLowerCase();
        let resourceFilterTypes:Array<string> = this.sdcConfig.resourceTypesFilter[entityType];
         scope.components = this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(scope.currentComponent);
        //remove the container component  from the list 
        let componentTempToDisplay = angular.copy(scope.components);
        componentTempToDisplay = _.remove(componentTempToDisplay, function (component) {
            return component.component.invariantUUID !== scope.currentComponent.invariantUUID;
        });
        scope.model = this.initLeftPanel(componentTempToDisplay, resourceFilterTypes);
        scope.displaySortedCategories = angular.copy(scope.model.sortedCategories);
    };

    private _initExpandedSection(scope:IPaletteScope, searchText:string):void {
        if (searchText == '') {
            let isContainingCategory:boolean = false;
            let categoryToExpand:string;
            if (scope.currentComponent && scope.currentComponent.categories && scope.currentComponent.categories[0]) {
                categoryToExpand = this.sdcMenu.categoriesDictionary[scope.currentComponent.categories[0].name];
                for (let category in scope.model.sortedCategories) {
                    if (categoryToExpand == category) {
                        isContainingCategory = true;
                        break;
                    }
                }
            }
            isContainingCategory ? scope.expandedSection = categoryToExpand : scope.expandedSection = 'Generic';
        }
        else {
            scope.expandedSection = Object.keys(scope.displaySortedCategories).sort()[0];
        }
    };

    private initDragEvents(scope:IPaletteScope) {
        scope.dragStartCallback = (event:IDragDropEvent, ui, displayComponent:LeftPaletteComponent):void => {
            if (scope.isLoading || !scope.isDragable || scope.isViewOnly) {
                return;
            }

            let component = _.find(this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(scope.currentComponent), (componentFullData:LeftPaletteComponent) => {
                return displayComponent.uniqueId === componentFullData.uniqueId;
            });
            this.EventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_START, scope.dragElement, component);

            scope.isOnDrag = true;

            // this.graphUtils.showMatchingNodes(component, myDiagram, scope.sdcConfig.imagesPath);
            // document.addEventListener('mousemove', moveOnDocument);
            event.dataTransfer.component = component;
        };

        scope.dragStopCallback = () => {
            scope.isOnDrag = false;
        };

        scope.onDragCallback = (event:IDragDropEvent):void => {
            this.EventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_ACTION, event);
        };
        scope.setElementTemplate = (e) => {
            let dragComponent:LeftPaletteComponent = _.find(this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(scope.currentComponent),
                (fullComponent:LeftPaletteComponent) => {
                    return (<any>angular.element(e.currentTarget).scope()).component.uniqueId === fullComponent.uniqueId;
                });
            let componentInstance:ComponentInstance = this.ComponentInstanceFactory.createComponentInstanceFromComponent(dragComponent);
            let node:CompositionCiNodeBase = this.NodesFactory.createNode(componentInstance);

            // myDiagram.dragFromPalette = node;
            this.nodeHtmlSubstitute.find("img").attr('src', node.img);
            scope.dragElement = this.nodeHtmlSubstitute.clone().show();

            return scope.dragElement;
        };
    }

    private _searchComponents = (searchText:string, categories:any):void => {
        let displaySortedCategories = angular.copy(categories);
        if (searchText != '') {
            angular.forEach(categories, function (category:any, categoryKey) {

                angular.forEach(category, function (subcategory:Array<LeftPaletteComponent>, subcategoryKey) {
                    let filteredResources = [];
                    angular.forEach(subcategory, function (component:LeftPaletteComponent) {

                        let resourceFilterTerm:string = component.searchFilterTerms;
                        if (resourceFilterTerm.indexOf(searchText.toLowerCase()) >= 0) {
                            filteredResources.push(component);
                        }
                    });
                    if (filteredResources.length > 0) {
                        displaySortedCategories[categoryKey][subcategoryKey] = filteredResources;
                    }
                    else {
                        delete  displaySortedCategories[categoryKey][subcategoryKey];
                    }
                });
                if (!(Object.keys(displaySortedCategories[categoryKey]).length > 0)) {
                    delete  displaySortedCategories[categoryKey];
                }

            });
        }
        return displaySortedCategories;
    };

    public static factory = ($log,
                             LeftPaletteLoaderService,
                             sdcConfig,
                             ComponentFactory,
                             ComponentInstanceFactory,
                             NodesFactory,
                             CompositionGraphGeneralUtils,
                             EventListenerService,
                             sdcMenu,
                             ModalsHandler) => {
        return new Palette($log,
            LeftPaletteLoaderService,
            sdcConfig,
            ComponentFactory,
            ComponentInstanceFactory,
            NodesFactory,
            CompositionGraphGeneralUtils,
            EventListenerService,
            sdcMenu,
            ModalsHandler);
    };
}

Palette.factory.$inject = [
    '$log',
    'LeftPaletteLoaderService',
    'sdcConfig',
    'ComponentFactory',
    'ComponentInstanceFactory',
    'NodesFactory',
    'CompositionGraphGeneralUtils',
    'EventListenerService',
    'sdcMenu',
    'ModalsHandler'
];
