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

import {
    Component,
    IAppMenu,
    LeftPanelModel,
    NodesFactory,
    LeftPaletteComponent,
    CompositionCiNodeBase,
    ComponentInstance
} from "app/models";
import {CompositionGraphGeneralUtils} from "../composition-graph/utils/composition-graph-general-utils";
import {EventListenerService} from "app/services";
import {ResourceType, GRAPH_EVENTS, EVENTS, ComponentInstanceFactory, ModalsHandler} from "app/utils";
import 'sdc-angular-dragdrop';
import {LeftPaletteLoaderService} from "../../../services/components/utils/composition-left-palette-service";

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

    private registerEventListenerForLeftPalette = (scope:IPaletteScope):void => {
        if (scope.currentComponent.isResource()) {
            this.EventListenerService.registerObserverCallback(EVENTS.RESOURCE_LEFT_PALETTE_UPDATE_EVENT, () => {
                this.updateLeftPanelDisplay(scope);
            });
        }
        if (scope.currentComponent.isService()) {
            this.EventListenerService.registerObserverCallback(EVENTS.SERVICE_LEFT_PALETTE_UPDATE_EVENT, () => {
                this.updateLeftPanelDisplay(scope);
            });
        }
        if (scope.currentComponent.isProduct()) {
            this.EventListenerService.registerObserverCallback(EVENTS.PRODUCT_LEFT_PALETTE_UPDATE_EVENT, () => {
                this.updateLeftPanelDisplay(scope);
            });
        }
    };

    private unRegisterEventListenerForLeftPalette = (scope:IPaletteScope):void => {
        if (scope.currentComponent.isResource()) {
            this.EventListenerService.unRegisterObserver(EVENTS.RESOURCE_LEFT_PALETTE_UPDATE_EVENT);
        }
        if (scope.currentComponent.isService()) {
            this.EventListenerService.unRegisterObserver(EVENTS.SERVICE_LEFT_PALETTE_UPDATE_EVENT);
        }
        if (scope.currentComponent.isProduct()) {
            this.EventListenerService.unRegisterObserver(EVENTS.PRODUCT_LEFT_PALETTE_UPDATE_EVENT);
        }
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
            //
            // if (this.CompositionGraphGeneralUtils.componentRequirementsAndCapabilitiesCaching.containsKey(displayComponent.uniqueId)) {
            //     this.$log.debug(`palette::onMouseOver:: component id ${displayComponent.uniqueId} found in cache`);
            //     let cacheComponent:Component = this.CompositionGraphGeneralUtils.componentRequirementsAndCapabilitiesCaching.getValue(displayComponent.uniqueId);
            //
            //     //TODO: Danny: fire event to highlight matching nodes
            //     //showMatchingNodes(cacheComponent);
            //     return;
            // }
            //
            // this.$log.debug(`palette::onMouseOver:: component id ${displayComponent.uniqueId} not found in cache, initiating server get`);
            // // This will bring the component from the server including requirements and capabilities
            // // Check that we do not fetch many times, because only in the success we add the component to componentRequirementsAndCapabilitiesCaching
            // if (this.fetchingComponentFromServer) {
            //     return;
            // }
            //
            // this.fetchingComponentFromServer = true;
            // this.ComponentFactory.getComponentFromServer(displayComponent.componentSubType, displayComponent.uniqueId)
            //     .then((component:Component) => {
            //         this.$log.debug(`palette::onMouseOver:: component id ${displayComponent.uniqueId} fetch success`);
            //         // this.LeftPaletteLoaderService.updateSpecificComponentLeftPalette(component, scope.currentComponent.componentType);
            //         this.CompositionGraphGeneralUtils.componentRequirementsAndCapabilitiesCaching.setValue(component.uniqueId, component);
            //         this.fetchingComponentFromServer = false;
            //
            //         //TODO: Danny: fire event to highlight matching nodes
            //         //showMatchingNodes(component);
            //     })
            //     .catch(() => {
            //         this.$log.debug('palette::onMouseOver:: component id fetch error');
            //         this.fetchingComponentFromServer = false;
            //     });


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
        scope.components = this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(scope.currentComponent.componentType);
        scope.model = this.initLeftPanel(scope.components, resourceFilterTypes);
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

            let component = _.find(this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(scope.currentComponent.componentType), (componentFullData:LeftPaletteComponent) => {
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
            let dragComponent:LeftPaletteComponent = _.find(this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(scope.currentComponent.componentType),
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
