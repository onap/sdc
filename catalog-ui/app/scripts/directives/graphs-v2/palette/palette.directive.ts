/// <reference path="../../../references"/>

module Sdc.Directives {
    import Dictionary = Sdc.Utils.Dictionary;
    import GRAPH_EVENTS = Sdc.Utils.Constants.GRAPH_EVENTS;
    import ImageCreatorService = Sdc.Utils.ImageCreatorService;
    interface IPaletteScope {
        components: any;
        currentComponent: any;
        model: any;
        displaySortedCategories: any;
        expandedSection: string;

        p2pVL: Models.Components.Component;
        mp2mpVL: Models.Components.Component;
        vlType: string;
        dragElement: JQuery;
        dragbleNode: {
            event: JQueryEventObject,
            components: Models.DisplayComponent,
            ui: any
        }

        sectionClick: (section: string)=>void;
        searchComponents: (searchText: string)=>void;
        onMouseOver: (displayComponent: Models.DisplayComponent)=>void;
        onMouseOut: (displayComponent: Models.DisplayComponent)=>void;
        dragStartCallback: (event: JQueryEventObject, ui, displayComponent: Models.DisplayComponent)=>void;
        dragStopCallback: ()=>void;
        onDragCallback: (event:JQueryEventObject) => void;
        setElementTemplate: (e: JQueryEventObject)=>void;

        isOnDrag: boolean;
        isDragable: boolean;
        isLoading: boolean;
        isViewOnly: boolean;
    }

    export class Palette implements ng.IDirective {
        constructor(private $log: ng.ILogService,
                    private LeftPaletteLoaderService,
                    private sdcConfig,
                    private ComponentFactory,
                    private ComponentInstanceFactory: Utils.ComponentInstanceFactory,
                    private NodesFactory: Utils.NodesFactory,
                    private CompositionGraphGeneralUtils: Graph.Utils.CompositionGraphGeneralUtils,
                    private EventListenerService: Services.EventListenerService,
                    private sdcMenu: Models.IAppMenu) {

        }

        private fetchingComponentFromServer: boolean = false;
        private nodeHtmlSubstitute: JQuery;

        scope = {
            components: '=',
            currentComponent: '=',
            isViewOnly: '=',
            isLoading: '='
        };
        restrict = 'E';
        templateUrl = '/app/scripts/directives/graphs-v2/palette/palette.html';

        link = (scope: IPaletteScope, el: JQuery) => {
            this.nodeHtmlSubstitute = $('<div class="node-substitute"><span></span><img /></div>');
            el.append(this.nodeHtmlSubstitute);

            this.initComponents(scope);
            this.initScopeVls(scope);
            this.initEvents(scope);
            this.initDragEvents(scope);
            this._initExpandedSection(scope, '');
        };

        private leftPanelResourceFilter(resourcesNotAbstract: Array<Models.DisplayComponent>, resourceFilterTypes: Array<string>): Array<Models.DisplayComponent> {
            let filterResources = _.filter(resourcesNotAbstract, (component) => {
                return resourceFilterTypes.indexOf(component.getComponentSubType()) > -1;
            });
            return filterResources;
        }

        private initLeftPanel(leftPanelComponents: Array<Models.DisplayComponent>, resourceFilterTypes: Array<string>): Models.LeftPanelModel {
            let leftPanelModel = new Models.LeftPanelModel();

            if (resourceFilterTypes && resourceFilterTypes.length) {
                leftPanelComponents = this.leftPanelResourceFilter(leftPanelComponents, resourceFilterTypes);
            }
            leftPanelModel.numberOfElements = leftPanelComponents && leftPanelComponents.length || 0;

            if (leftPanelComponents && leftPanelComponents.length) {

                let categories: any = _.groupBy(leftPanelComponents, 'mainCategory');
                for (let category in categories)
                    categories[category] = _.groupBy(categories[category], 'subCategory');

                leftPanelModel.sortedCategories = categories;
            }
            return leftPanelModel;
        }

        private initScopeVls(scope: IPaletteScope): void {
            let vls = this.LeftPaletteLoaderService.getFullDataComponentList(Utils.Constants.ResourceType.VL);
            scope.vlType = null;
            vls.forEach((item) => {
                let key = _.find(Object.keys(item.capabilities), (key) => {
                    return _.includes(key.toLowerCase(), 'linkable');
                });
                let linkable = item.capabilities[key];
                if (linkable) {
                    if (linkable[0].maxOccurrences == '2') {
                        scope.p2pVL = _.find(vls, (component: Models.Components.Component) => {
                            return component.uniqueId === item.uniqueId;
                        });

                    } else {//assuming unbounded occurrences
                        scope.mp2mpVL = _.find(vls, (component: Models.Components.Component) => {
                            return component.uniqueId === item.uniqueId;
                        });
                    }
                }
            });
        };

        private initEvents(scope: IPaletteScope) {
            /**
             *
             * @param section
             */
            scope.sectionClick = (section: string) => {
                if (section === scope.expandedSection) {
                    scope.expandedSection = '';
                    return;
                }
                scope.expandedSection = section;
            };

            scope.onMouseOver = (displayComponent: Models.DisplayComponent) => {
                if (scope.isOnDrag) {
                    return;
                }
                scope.isOnDrag = true;

                this.EventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_IN, displayComponent);
                this.$log.debug('palette::onMouseOver:: fired');

                if (this.CompositionGraphGeneralUtils.componentRequirementsAndCapabilitiesCaching.containsKey(displayComponent.uniqueId)) {
                    this.$log.debug(`palette::onMouseOver:: component id ${displayComponent.uniqueId} found in cache`);
                    let cacheComponent: Models.Components.Component = this.CompositionGraphGeneralUtils.componentRequirementsAndCapabilitiesCaching.getValue(displayComponent.uniqueId);

                    //TODO: Danny: fire event to highlight matching nodes
                    //showMatchingNodes(cacheComponent);
                    return;
                }

                this.$log.debug(`palette::onMouseOver:: component id ${displayComponent.uniqueId} not found in cache, initiating server get`);
                // This will bring the component from the server including requirements and capabilities
                // Check that we do not fetch many times, because only in the success we add the component to componentRequirementsAndCapabilitiesCaching
                if (this.fetchingComponentFromServer) {
                    return;
                }

                this.fetchingComponentFromServer = true;
                this.ComponentFactory.getComponentFromServer(displayComponent.componentSubType, displayComponent.uniqueId)
                    .then((component: Models.Components.Component) => {
                        this.$log.debug(`palette::onMouseOver:: component id ${displayComponent.uniqueId} fetch success`);
                        this.LeftPaletteLoaderService.updateSpecificComponentLeftPalette(component, scope.currentComponent.componentType);
                        this.CompositionGraphGeneralUtils.componentRequirementsAndCapabilitiesCaching.setValue(component.uniqueId, component);
                        this.fetchingComponentFromServer = false;

                        //TODO: Danny: fire event to highlight matching nodes
                        //showMatchingNodes(component);
                    })
                    .catch(() => {
                        this.$log.debug('palette::onMouseOver:: component id fetch error');
                        this.fetchingComponentFromServer = false;
                    });


            };

            scope.onMouseOut = () => {
                scope.isOnDrag = false;
                this.EventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_OUT);
            }
        }

        private initComponents(scope: IPaletteScope) {
            scope.searchComponents = (searchText: any): void => {
                scope.displaySortedCategories = this._searchComponents(searchText, scope.model.sortedCategories);
                this._initExpandedSection(scope, searchText);
            };

            scope.isDragable = scope.currentComponent.isComplex();
            let entityType: string = scope.currentComponent.componentType.toLowerCase();
            let resourceFilterTypes: Array<string> = this.sdcConfig.resourceTypesFilter[entityType];

            scope.components = this.LeftPaletteLoaderService.getLeftPanelComponentsForDisplay(scope.currentComponent.componentType);
            scope.model = this.initLeftPanel(scope.components, resourceFilterTypes);
            scope.displaySortedCategories = angular.copy(scope.model.sortedCategories);
        }

        private _initExpandedSection(scope: IPaletteScope, searchText: string): void {
            if (searchText == '') {
                let isContainingCategory: boolean = false;
                let categoryToExpand: string;
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

        private initDragEvents(scope: IPaletteScope) {
            scope.dragStartCallback = (event: IDragDropEvent, ui, displayComponent: Models.DisplayComponent): void => {
                if (scope.isLoading || !scope.isDragable || scope.isViewOnly) {
                    return;
                }

                let component = _.find(this.LeftPaletteLoaderService.getFullDataComponentListWithVls(scope.currentComponent.componentType), (componentFullData: Models.DisplayComponent) => {
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

            scope.onDragCallback = (event:IDragDropEvent): void => {
                this.EventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_ACTION, event);
            };
            scope.setElementTemplate = (e) => {
                let dragComponent: Models.Components.Component = _.find(this.LeftPaletteLoaderService.getFullDataComponentListWithVls(scope.currentComponent.componentType),
                    (fullComponent: Models.Components.Component) => {
                        return (<any>angular.element(e.currentTarget).scope()).component.uniqueId === fullComponent.uniqueId;
                    });
                let componentInstance: Models.ComponentsInstances.ComponentInstance = this.ComponentInstanceFactory.createComponentInstanceFromComponent(dragComponent);
                let node: Models.Graph.CompositionCiNodeBase = this.NodesFactory.createNode(componentInstance);

                // myDiagram.dragFromPalette = node;
                this.nodeHtmlSubstitute.find("img").attr('src', node.img);
                scope.dragElement = this.nodeHtmlSubstitute.clone().show();

                return scope.dragElement;
            };
        }

        private _searchComponents = (searchText: string, categories: any): void => {
            let displaySortedCategories = angular.copy(categories);
            if (searchText != '') {
                angular.forEach(categories, function (category: any, categoryKey) {

                    angular.forEach(category, function (subcategory: Array<Models.DisplayComponent>, subcategoryKey) {
                        let filteredResources = [];
                        angular.forEach(subcategory, function (component: Models.DisplayComponent) {

                            let resourceFilterTerm: string = component.searchFilterTerms;
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
                                 sdcMenu) => {
            return new Palette($log,
                LeftPaletteLoaderService,
                sdcConfig,
                ComponentFactory,
                ComponentInstanceFactory,
                NodesFactory,
                CompositionGraphGeneralUtils,
                EventListenerService,
                sdcMenu);
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
        'sdcMenu'
    ];
}