/// <reference path="../../../references"/>
module Sdc.Directives {

    import ComponentFactory = Sdc.Utils.ComponentFactory;
    import LoaderService = Sdc.Services.LoaderService;
    import GRAPH_EVENTS = Sdc.Utils.Constants.GRAPH_EVENTS;

    interface ICompositionGraphScope extends ng.IScope {

        component:Models.Components.Component;
        isViewOnly:boolean;
        // Link menu - create link menu
        relationMenuDirectiveObj:Models.RelationMenuDirectiveObj;
        isLinkMenuOpen:boolean;
        createLinkFromMenu:(chosenMatch:Models.MatchBase, vl:Models.Components.Component)=>void;

        //modify link menu - for now only delete menu
        relationMenuTimeout:ng.IPromise<any>;
        linkMenuObject:Models.LinkMenu;

        //left palette functions callbacks
        dropCallback(event:JQueryEventObject, ui:any):void;
        beforeDropCallback(event:IDragDropEvent):void;
        verifyDrop(event:JQueryEventObject, ui:any):void;

        //Links menus
        deleteRelation(link:Cy.CollectionEdges):void;
        hideRelationMenu();
    }

    export class CompositionGraph implements ng.IDirective {
        private _cy:Cy.Instance;
        private _currentlyCLickedNodePosition:Cy.Position;
        private $document:JQuery = $(document);
        private dragElement:JQuery;
        private dragComponent: Sdc.Models.ComponentsInstances.ComponentInstance;

        constructor(private $q:ng.IQService,
                    private $filter:ng.IFilterService,
                    private $log:ng.ILogService,
                    private $timeout:ng.ITimeoutService,
                    private NodesFactory:Sdc.Utils.NodesFactory,
                    private CompositionGraphLinkUtils:Sdc.Graph.Utils.CompositionGraphLinkUtils,
                    private GeneralGraphUtils:Graph.Utils.CompositionGraphGeneralUtils,
                    private ComponentInstanceFactory:Utils.ComponentInstanceFactory,
                    private NodesGraphUtils:Sdc.Graph.Utils.CompositionGraphNodesUtils,
                    private eventListenerService:Services.EventListenerService,
                    private ComponentFactory:ComponentFactory,
                    private LoaderService:LoaderService,
                    private commonGraphUtils:Graph.Utils.CommonGraphUtils,
                    private matchCapabilitiesRequirementsUtils:Graph.Utils.MatchCapabilitiesRequirementsUtils) {

        }

        restrict = 'E';
        templateUrl = '/app/scripts/directives/graphs-v2/composition-graph/composition-graph.html';
        scope = {
            component: '=',
            isViewOnly: '='
        };

        link = (scope:ICompositionGraphScope, el:JQuery) => {
            this.loadGraph(scope, el);

            scope.$on('$destroy', () => {
                this._cy.destroy();
                _.forEach(GRAPH_EVENTS, (event) => {
                    this.eventListenerService.unRegisterObserver(event);
                });
            });

        };

        private loadGraph = (scope:ICompositionGraphScope, el:JQuery) => {


            let graphEl = el.find('.sdc-composition-graph-wrapper');
            this.initGraph(graphEl, scope.isViewOnly);
            this.initGraphNodes(scope.component.componentInstances, scope.isViewOnly);
            this.commonGraphUtils.initGraphLinks(this._cy, scope.component.componentInstancesRelations);
            this.commonGraphUtils.initUcpeChildren(this._cy);
            this.initDropZone(scope);
            this.registerCytoscapeGraphEvents(scope);
            this.registerCustomEvents(scope, el);
            this.initViewMode(scope.isViewOnly);

        };

        private initGraph(graphEl:JQuery, isViewOnly:boolean) {

            this._cy = cytoscape({
                container: graphEl,
                style: Sdc.Graph.Utils.ComponentIntanceNodesStyle.getCompositionGraphStyle(),
                zoomingEnabled: false,
                selectionType: 'single',
                boxSelectionEnabled: true,
                autolock: isViewOnly,
                autoungrabify: isViewOnly
            });
        }

        private initViewMode(isViewOnly:boolean) {

            if (isViewOnly) {
                //remove event listeners
                this._cy.off('drag');
                this._cy.off('handlemouseout');
                this._cy.off('handlemouseover');
                this._cy.edges().unselectify();
            }
        };

        private registerCustomEvents(scope:ICompositionGraphScope, el:JQuery) {

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_IN, (component:Models.DisplayComponent) => {
                this.$log.info(`composition-graph::registerEventServiceEvents:: palette hover on component: ${component.uniqueId}`);

                let nodesData = this.NodesGraphUtils.getAllNodesData(this._cy.nodes());
                let nodesLinks = this.GeneralGraphUtils.getAllCompositionCiLinks(this._cy);

                if (this.GeneralGraphUtils.componentRequirementsAndCapabilitiesCaching.containsKey(component.uniqueId)) {
                    let cacheComponent = this.GeneralGraphUtils.componentRequirementsAndCapabilitiesCaching.getValue(component.uniqueId);
                    let filteredNodesData = this.matchCapabilitiesRequirementsUtils.findByMatchingCapabilitiesToRequirements(cacheComponent, nodesData, nodesLinks);

                    this.matchCapabilitiesRequirementsUtils.highlightMatchingComponents(filteredNodesData, this._cy);
                    this.matchCapabilitiesRequirementsUtils.fadeNonMachingComponents(filteredNodesData, nodesData, this._cy);

                    return;
                }

                component.component.updateRequirementsCapabilities()
                    .then((res) => {
                        component.component.capabilities = res.capabilities;
                        component.component.requirements = res.requirements;

                        let filteredNodesData = this.matchCapabilitiesRequirementsUtils.findByMatchingCapabilitiesToRequirements(component.component, nodesData, nodesLinks);
                        this.matchCapabilitiesRequirementsUtils.fadeNonMachingComponents(filteredNodesData, nodesData, this._cy);
                        this.matchCapabilitiesRequirementsUtils.highlightMatchingComponents(filteredNodesData, this._cy)
                    });
            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_OUT, () => {
                this._cy.emit('hidehandles');
                this.matchCapabilitiesRequirementsUtils.resetFadedNodes(this._cy);
            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_START, (dragElement, dragComponent) => {

                this.dragElement = dragElement;
                this.dragComponent = this.ComponentInstanceFactory.createComponentInstanceFromComponent(dragComponent);
            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_ACTION, (event:IDragDropEvent) => {
                this._onComponentDrag(event);

            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, (component:Models.ComponentsInstances.ComponentInstance) => {

                let selectedNode = this._cy.getElementById(component.uniqueId);
                selectedNode.data().componentInstance.name = component.name;
                selectedNode.data('displayName', selectedNode.data().getDisplayName());

            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, (componentInstance:Models.ComponentsInstances.ComponentInstance) => {
                let nodeToDelete = this._cy.getElementById(componentInstance.uniqueId);
                this.NodesGraphUtils.deleteNode(this._cy, scope.component, nodeToDelete);
            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_MULTIPLE_COMPONENTS, () => {

                this._cy.$('node:selected').each((i:number, node:Cy.CollectionNodes) => {
                    this.NodesGraphUtils.deleteNode(this._cy, scope.component, node);
                });

            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_EDGE, (releaseLoading:boolean, linksToDelete:Cy.CollectionEdges) => {
                this.CompositionGraphLinkUtils.deleteLink(this._cy, scope.component, releaseLoading, linksToDelete);
            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_INSERT_NODE_TO_UCPE, (node:Cy.CollectionNodes, ucpe:Cy.CollectionNodes, updateExistingNode: boolean) => {

                this.commonGraphUtils.initUcpeChildData(node, ucpe);
                //check if item is a VL, and if so, skip adding the binding to ucpe
                if(!(node.data() instanceof Sdc.Models.Graph.CompositionCiNodeVl)){
                    this.CompositionGraphLinkUtils.createVfToUcpeLink(scope.component, this._cy, ucpe.data(), node.data()); //create link from the node to the ucpe
                }

                if(updateExistingNode){
                    let vlsPendingDeletion:Cy.CollectionNodes = this.NodesGraphUtils.deleteNodeVLsUponMoveToOrFromUCPE(scope.component, node.cy(), node); //delete connected VLs that no longer have 2 links
                    this.CompositionGraphLinkUtils.deleteLinksWhenNodeMovedFromOrToUCPE(scope.component, node.cy(), node, vlsPendingDeletion); //delete all connected links if needed
                    this.GeneralGraphUtils.pushUpdateComponentInstanceActionToQueue(scope.component, true, node.data().componentInstance); //update componentInstance position
                }

            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_REMOVE_NODE_FROM_UCPE, (node:Cy.CollectionNodes, ucpe:Cy.CollectionNodes) => {
                this.commonGraphUtils.removeUcpeChildData(node);
                let vlsPendingDeletion:Cy.CollectionNodes = this.NodesGraphUtils.deleteNodeVLsUponMoveToOrFromUCPE(scope.component, node.cy(), node);
                this.CompositionGraphLinkUtils.deleteLinksWhenNodeMovedFromOrToUCPE(scope.component, node.cy(), node, vlsPendingDeletion); //delete all connected links if needed
                this.GeneralGraphUtils.pushUpdateComponentInstanceActionToQueue(scope.component, true, node.data().componentInstance); //update componentInstance position
            });

            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_VERSION_CHANGED, (component:Models.Components.Component) => {
                scope.component = component;
                this.loadGraph(scope, el);
            });


            scope.createLinkFromMenu = (chosenMatch:Models.MatchBase, vl:Models.Components.Component):void => {
                scope.isLinkMenuOpen = false;

                this.CompositionGraphLinkUtils.createLinkFromMenu(this._cy, chosenMatch, vl, scope.component);
            };

            scope.hideRelationMenu = () => {
                this.commonGraphUtils.safeApply(scope, () => {
                    scope.linkMenuObject = null;
                    this.$timeout.cancel(scope.relationMenuTimeout);
                });
            };


            scope.deleteRelation = (link:Cy.CollectionEdges) => {
                scope.hideRelationMenu();

                //if multiple edges selected, delete the VL itself so edges get deleted automatically
                if (this._cy.$('edge:selected').length > 1) {
                    this.NodesGraphUtils.deleteNode(this._cy, scope.component, this._cy.$('node:selected'));
                } else {
                    this.CompositionGraphLinkUtils.deleteLink(this._cy, scope.component, true, link);
                }
            };
        }


        private registerCytoscapeGraphEvents(scope:ICompositionGraphScope) {

            this._cy.on('addedgemouseup', (event, data) => {
                scope.relationMenuDirectiveObj = this.CompositionGraphLinkUtils.onLinkDrawn(this._cy, data.source, data.target);
                if (scope.relationMenuDirectiveObj != null) {
                    scope.$apply(() => {
                        scope.isLinkMenuOpen = true;
                    });
                }
            });
            this._cy.on('tapstart', 'node', (event:Cy.EventObject) => {
                this._currentlyCLickedNodePosition = angular.copy(event.cyTarget[0].position()); //update node position on drag
                if(event.cyTarget.data().isUcpe){
                    this._cy.nodes('.ucpe-cp').unlock();
                    event.cyTarget.style('opacity', 0.5);
                }
            });

            this._cy.on('drag', 'node', (event:Cy.EventObject) => {

                if (event.cyTarget.data().isDraggable) {
                    event.cyTarget.style({'overlay-opacity': 0.24});
                    if (this.GeneralGraphUtils.isValidDrop(this._cy, event.cyTarget)) {
                        event.cyTarget.style({'overlay-color': Utils.Constants.GraphColors.NODE_BACKGROUND_COLOR});
                    } else {
                        event.cyTarget.style({'overlay-color': Utils.Constants.GraphColors.NODE_OVERLAPPING_BACKGROUND_COLOR});
                    }
                }

                if(event.cyTarget.data().isUcpe){
                    let pos = event.cyTarget.position();

                    this._cy.nodes('[?isInsideGroup]').positions((i, node)=>{
                        return {
                            x: pos.x + node.data("ucpeOffset").x,
                            y: pos.y + node.data("ucpeOffset").y
                        }
                    });
                }
            });


            this._cy.on('handlemouseover', (event, payload) => {

                if (payload.node.grabbed()) { //no need to add opacity while we are dragging and hovering othe nodes
                    return;
                }

                let nodesData = this.NodesGraphUtils.getAllNodesData(this._cy.nodes());
                let nodesLinks = this.GeneralGraphUtils.getAllCompositionCiLinks(this._cy);

                let linkableNodes = this.commonGraphUtils.getLinkableNodes(this._cy, payload.node);
                let filteredNodesData = this.matchCapabilitiesRequirementsUtils.findByMatchingCapabilitiesToRequirements(payload.node.data().componentInstance, linkableNodes, nodesLinks);
                this.matchCapabilitiesRequirementsUtils.highlightMatchingComponents(filteredNodesData, this._cy);
                this.matchCapabilitiesRequirementsUtils.fadeNonMachingComponents(filteredNodesData, nodesData, this._cy, payload.node.data());

            });

            this._cy.on('handlemouseout', () => {
                this._cy.emit('hidehandles');
                this.matchCapabilitiesRequirementsUtils.resetFadedNodes(this._cy);
            });


            this._cy.on('tapend', (event:Cy.EventObject) => {

                if (event.cyTarget === this._cy) { //On Background clicked
                    if (this._cy.$('node:selected').length === 0) { //if the background click but not dragged
                        this.eventListenerService.notifyObservers(Sdc.Utils.Constants.GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED);
                    }
                    scope.hideRelationMenu();
                }

                else if (event.cyTarget.isEdge()) { //On Edge clicked
                    if (scope.isViewOnly) return;
                    this.CompositionGraphLinkUtils.handleLinkClick(this._cy, event);
                    this.openModifyLinkMenu(scope, this.CompositionGraphLinkUtils.getModifyLinkMenu(event.cyTarget[0], event), 6000);
                }

                else { //On Node clicked
                    this._cy.nodes(':grabbed').style({'overlay-opacity': 0});

                    let isUcpe:boolean = event.cyTarget.data().isUcpe;
                    let newPosition = event.cyTarget[0].position();
                    //node position changed (drop after drag event) - we need to update position
                    if (this._currentlyCLickedNodePosition.x !== newPosition.x || this._currentlyCLickedNodePosition.y !== newPosition.y) {
                        let nodesMoved:Cy.CollectionNodes = this._cy.$(':grabbed');
                        if(isUcpe){
                            nodesMoved = nodesMoved.add(this._cy.nodes('[?isInsideGroup]:free')); //'child' nodes will not be recognized as "grabbed" elements within cytoscape. manually add them to collection of nodes moved.
                        }
                        this.NodesGraphUtils.onNodesPositionChanged(this._cy, scope.component, nodesMoved);
                    } else {
                        this.$log.debug('composition-graph::onNodeSelectedEvent:: fired');
                        scope.$apply(() => {
                            this.eventListenerService.notifyObservers(Sdc.Utils.Constants.GRAPH_EVENTS.ON_NODE_SELECTED, event.cyTarget.data().componentInstance);
                        });
                    }

                    if(isUcpe){
                        this._cy.nodes('.ucpe-cp').lock();
                        event.cyTarget.style('opacity', 1);
                    }

                }
            });

            this._cy.on('boxselect', 'node', (event:Cy.EventObject) => {
                this.eventListenerService.notifyObservers(Utils.Constants.GRAPH_EVENTS.ON_NODE_SELECTED, event.cyTarget.data().componentInstance);
            });
        }

        private openModifyLinkMenu = (scope:ICompositionGraphScope, linkMenuObject:Models.LinkMenu, timeOutInMilliseconds?:number) => {

            this.commonGraphUtils.safeApply(scope, () => {
                scope.linkMenuObject = linkMenuObject;
            });

            scope.relationMenuTimeout = this.$timeout(() => {
                scope.hideRelationMenu();
            }, timeOutInMilliseconds ? timeOutInMilliseconds : 6000);
        };

        private initGraphNodes(componentInstances:Models.ComponentsInstances.ComponentInstance[], isViewOnly:boolean) {

            if (!isViewOnly) { //Init nodes handle extension - enable dynamic links
                setTimeout(()=> {
                    let handles = new CytoscapeEdgeEditation;
                    handles.init(this._cy, 18);
                    handles.registerHandle(Sdc.Graph.Utils.ComponentIntanceNodesStyle.getBasicNodeHanlde());
                    handles.registerHandle(Sdc.Graph.Utils.ComponentIntanceNodesStyle.getBasicSmallNodeHandle());
                    handles.registerHandle(Sdc.Graph.Utils.ComponentIntanceNodesStyle.getUcpeCpNodeHandle());
                }, 0);
            }

            _.each(componentInstances, (instance) => {
                let compositionGraphNode:Models.Graph.CompositionCiNodeBase = this.NodesFactory.createNode(instance);
                this.commonGraphUtils.addComponentInstanceNodeToGraph(this._cy, compositionGraphNode);
            });



        }


        private initDropZone(scope:ICompositionGraphScope) {

            if (scope.isViewOnly) {
                return;
            }
            scope.dropCallback = (event:IDragDropEvent) => {
                this.$log.debug(`composition-graph::dropCallback:: fired`);
                this.addNode(event, scope);
            };

            scope.verifyDrop = (event:JQueryEventObject) => {

                if(this.dragElement.hasClass('red')){
                    return false;
                }
                return true;
            };

            scope.beforeDropCallback = (event:IDragDropEvent): ng.IPromise<void> => {
                let deferred: ng.IDeferred<void> = this.$q.defer<void>();
                if(this.dragElement.hasClass('red')){
                    deferred.reject();
                } else {
                    deferred.resolve();
                }

                return deferred.promise;
            }
        }

        private _getNodeBBox(event:IDragDropEvent, position?:Cy.Position) {
            let bbox = <Cy.BoundingBox>{};
            if (!position) {
                position = this.commonGraphUtils.getCytoscapeNodePosition(this._cy, event);
            }
            let cushionWidth:number = 40;
            let cushionHeight:number = 40;

            bbox.x1 = position.x - cushionWidth / 2;
            bbox.y1 = position.y - cushionHeight / 2;
            bbox.x2 = position.x + cushionWidth / 2;
            bbox.y2 = position.y + cushionHeight / 2;
            return bbox;
        }

        private createComponentInstanceOnGraphFromComponent(fullComponent:Models.Components.Component, event:IDragDropEvent, scope:ICompositionGraphScope) {

            let componentInstanceToCreate:Models.ComponentsInstances.ComponentInstance = this.ComponentInstanceFactory.createComponentInstanceFromComponent(fullComponent);
            let cytoscapePosition:Cy.Position = this.commonGraphUtils.getCytoscapeNodePosition(this._cy, event);

            componentInstanceToCreate.posX = cytoscapePosition.x;
            componentInstanceToCreate.posY = cytoscapePosition.y;


            let onFailedCreatingInstance:(error:any) => void = (error:any) => {
                this.LoaderService.hideLoader('composition-graph');
            };

            //on success - update node data
            let onSuccessCreatingInstance = (createInstance:Models.ComponentsInstances.ComponentInstance):void => {

                this.LoaderService.hideLoader('composition-graph');

                createInstance.name = this.$filter('resourceName')(createInstance.name);
                createInstance.requirements = new Models.RequirementsGroup(fullComponent.requirements);
                createInstance.capabilities = new Models.CapabilitiesGroup(fullComponent.capabilities);
                createInstance.componentVersion = fullComponent.version;
                createInstance.icon = fullComponent.icon;
                createInstance.setInstanceRC();

                let newNode:Models.Graph.CompositionCiNodeBase = this.NodesFactory.createNode(createInstance);
                let cyNode:Cy.CollectionNodes = this.commonGraphUtils.addComponentInstanceNodeToGraph(this._cy, newNode);

                //check if node was dropped into a UCPE
                let ucpe:Cy.CollectionElements  = this.commonGraphUtils.isInUcpe(this._cy, cyNode.boundingbox());
                if (ucpe.length > 0) {
                    this.eventListenerService.notifyObservers(Utils.Constants.GRAPH_EVENTS.ON_INSERT_NODE_TO_UCPE, cyNode, ucpe, false);
                }

            };

            // Create the component instance on server
            this.GeneralGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIAction(() => {
                scope.component.createComponentInstance(componentInstanceToCreate).then(onSuccessCreatingInstance, onFailedCreatingInstance);
            });
        }

        private _onComponentDrag(event:IDragDropEvent) {

            if(event.clientX < Sdc.Utils.Constants.GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET || event.clientY < Sdc.Utils.Constants.GraphUIObjects.DIAGRAM_HEADER_OFFSET){ //hovering over palette. Dont bother computing validity of drop
                this.dragElement.removeClass('red');
                return;
            }

            let offsetPosition = {x: event.clientX - Sdc.Utils.Constants.GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET, y: event.clientY - Sdc.Utils.Constants.GraphUIObjects.DIAGRAM_HEADER_OFFSET}
            let bbox = this._getNodeBBox(event, offsetPosition);
            
            if (this.GeneralGraphUtils.isPaletteDropValid(this._cy, bbox, this.dragComponent)) {
                this.dragElement.removeClass('red');
            } else {
                this.dragElement.addClass('red');
            }
        }

        private addNode(event:IDragDropEvent, scope:ICompositionGraphScope) {
            this.LoaderService.showLoader('composition-graph');

            this.$log.debug('composition-graph::addNode:: fired');
            let draggedComponent:Models.Components.Component = event.dataTransfer.component;

            if (this.GeneralGraphUtils.componentRequirementsAndCapabilitiesCaching.containsKey(draggedComponent.uniqueId)) {
                this.$log.debug('composition-graph::addNode:: capabilities found in cache, creating component');
                let fullComponent = this.GeneralGraphUtils.componentRequirementsAndCapabilitiesCaching.getValue(draggedComponent.uniqueId);
                this.createComponentInstanceOnGraphFromComponent(fullComponent, event, scope);
                return;
            }

            this.$log.debug('composition-graph::addNode:: capabilities not found, requesting from server');
            this.ComponentFactory.getComponentFromServer(draggedComponent.getComponentSubType(), draggedComponent.uniqueId)
                .then((fullComponent:Models.Components.Component) => {
                    this.createComponentInstanceOnGraphFromComponent(fullComponent, event, scope);
                });
        }

        public static factory = ($q,
                                 $filter,
                                 $log,
                                 $timeout,
                                 NodesFactory,
                                 LinksGraphUtils,
                                 GeneralGraphUtils,
                                 ComponentInstanceFactory,
                                 NodesGraphUtils,
                                 EventListenerService,
                                 ComponentFactory,
                                 LoaderService,
                                 CommonGraphUtils,
                                 MatchCapabilitiesRequirementsUtils) => {
            return new CompositionGraph(
                $q,
                $filter,
                $log,
                $timeout,
                NodesFactory,
                LinksGraphUtils,
                GeneralGraphUtils,
                ComponentInstanceFactory,
                NodesGraphUtils,
                EventListenerService,
                ComponentFactory,
                LoaderService,
                CommonGraphUtils,
                MatchCapabilitiesRequirementsUtils);
        }
    }

    CompositionGraph.factory.$inject = [
        '$q',
        '$filter',
        '$log',
        '$timeout',
        'NodesFactory',
        'CompositionGraphLinkUtils',
        'CompositionGraphGeneralUtils',
        'ComponentInstanceFactory',
        'CompositionGraphNodesUtils',
        'EventListenerService',
        'ComponentFactory',
        'LoaderService',
        'CommonGraphUtils',
        'MatchCapabilitiesRequirementsUtils'
    ];
}