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
    Match,
    LinkMenu,
    ComponentInstance,
    LeftPaletteComponent,
    Capability,
    Requirement,
    Relationship,
    PropertyModel,
    Component,
    ConnectRelationModel,
    CompositionCiNodeBase,
    CompositionCiNodeVl,
    ModalModel,
    ButtonModel,
    NodesFactory/*,
    AssetPopoverObj*/
} from "app/models";
import {ComponentInstanceFactory, ComponentFactory, GRAPH_EVENTS, GraphColors} from "app/utils";
import {EventListenerService, LoaderService} from "app/services";
import {CompositionGraphLinkUtils} from "./utils/composition-graph-links-utils";
import {CompositionGraphGeneralUtils} from "./utils/composition-graph-general-utils";
import {CompositionGraphNodesUtils} from "./utils/composition-graph-nodes-utils";
import {CommonGraphUtils} from "../common/common-graph-utils";
import {MatchCapabilitiesRequirementsUtils} from "./utils/match-capability-requierment-utils";
import {CompositionGraphPaletteUtils} from "./utils/composition-graph-palette-utils";
import {ComponentInstanceNodesStyle} from "../common/style/component-instances-nodes-style";
import {CytoscapeEdgeEditation} from 'third-party/cytoscape.js-edge-editation/CytoscapeEdgeEditation.js';
import {ComponentServiceNg2} from "../../../ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "../../../ng2/services/responses/component-generic-response";
import {ModalService} from "../../../ng2/services/modal.service";

import {ConnectionWizardService} from "../../../ng2/pages/connection-wizard/connection-wizard.service";
import {StepModel} from "../../../models/wizard-step";
import {FromNodeStepComponent} from "app/ng2/pages/connection-wizard/from-node-step/from-node-step.component";
import {PropertiesStepComponent} from "app/ng2/pages/connection-wizard/properties-step/properties-step.component";
import {ToNodeStepComponent} from "app/ng2/pages/connection-wizard/to-node-step/to-node-step.component";
import {ConnectionWizardHeaderComponent} from "app/ng2/pages/connection-wizard/connection-wizard-header/connection-wizard-header.component";
import {ConnectionPropertiesViewComponent} from "../../../ng2/pages/connection-wizard/connection-properties-view/connection-properties-view.component";
import {ComponentInstanceServiceNg2} from "../../../ng2/services/component-instance-services/component-instance.service";
import {EVENTS} from "../../../utils/constants";
import {PropertyBEModel} from "../../../models/properties-inputs/property-be-model";

interface ICompositionGraphScope extends ng.IScope {

    component:Component;
    isLoading: boolean;
    isViewOnly: boolean;
    withSidebar: boolean;
    // Link menu - create link menu
    relationMenuDirectiveObj:ConnectRelationModel;
    isLinkMenuOpen:boolean;
    createLinkFromMenu:(chosenMatch:Match, vl:Component)=>void;
    saveChangedCapabilityProperties:()=>Promise<PropertyBEModel[]>;

    //modify link menu - for now only delete menu
    relationMenuTimeout:ng.IPromise<any>;
    linkMenuObject:LinkMenu;

    //left palette functions callbacks
    dropCallback(event:JQueryEventObject, ui:any):void;
    beforeDropCallback(event:IDragDropEvent):void;
    verifyDrop(event:JQueryEventObject, ui:any):void;

    //Links menus
    viewRelation(link:Cy.CollectionEdges):void;
    deleteRelation(link:Cy.CollectionEdges):void;
    hideRelationMenu();

    //search,zoom in/out/all
    componentInstanceNames: Array<string>; //id, name
    zoom(zoomIn: boolean): void;
    zoomAll(nodes?:Cy.CollectionNodes): void;
    getAutoCompleteValues(searchTerm: string):void;
    highlightSearchMatches(searchTerm: string): void;

    canvasMenuProps:any;
    
    /*//asset popover menu
    assetPopoverObj:AssetPopoverObj;
    assetPopoverOpen:boolean;
    hideAssetPopover():void;
    deleteNode(nodeId:string):void;*/
}

export class CompositionGraph implements ng.IDirective {
    private _cy:Cy.Instance;
    private _currentlyCLickedNodePosition:Cy.Position;
    // private $document:JQuery = $(document);
    private dragElement:JQuery;
    private dragComponent:ComponentInstance;

    constructor(private $q:ng.IQService,
                private $log:ng.ILogService,
                private $timeout:ng.ITimeoutService,
                private NodesFactory:NodesFactory,
                private CompositionGraphLinkUtils:CompositionGraphLinkUtils,
                private GeneralGraphUtils:CompositionGraphGeneralUtils,
                private ComponentInstanceFactory:ComponentInstanceFactory,
                private NodesGraphUtils:CompositionGraphNodesUtils,
                private eventListenerService:EventListenerService,
                private ComponentFactory:ComponentFactory,
                private LoaderService:LoaderService,
                private commonGraphUtils:CommonGraphUtils,
                private matchCapabilitiesRequirementsUtils:MatchCapabilitiesRequirementsUtils,
                private CompositionGraphPaletteUtils:CompositionGraphPaletteUtils,
                private ComponentServiceNg2: ComponentServiceNg2,
                private ModalServiceNg2: ModalService,
                private ConnectionWizardServiceNg2: ConnectionWizardService,
                private ComponentInstanceServiceNg2: ComponentInstanceServiceNg2) {

    }

    restrict = 'E';
    template = require('./composition-graph.html');
    scope = {
        component: '=',
        isViewOnly: '=',
        withSidebar: '='
    };

    link = (scope:ICompositionGraphScope, el:JQuery) => {

        this.loadGraph(scope, el);

        if(scope.component.componentInstances && scope.component.componentInstancesRelations) {
            this.loadGraphData(scope);
        } else {
            //when we don't have the data we register to on graph load event
            this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_COMPOSITION_GRAPH_DATA_LOADED, () => {
                this.loadGraphData(scope);
            });
        }
        scope.$on('$destroy', () => {
            this._cy.destroy();
            _.forEach(GRAPH_EVENTS, (event) => {
                this.eventListenerService.unRegisterObserver(event);
            });
            this.eventListenerService.unRegisterObserver(EVENTS.SHOW_LOADER_EVENT + 'composition-graph');
            this.eventListenerService.unRegisterObserver(EVENTS.HIDE_LOADER_EVENT + 'composition-graph');
        });

    };

    private loadGraphData = (scope:ICompositionGraphScope) => {
        this.initGraphNodes(scope.component.componentInstances, scope.isViewOnly);
        this.commonGraphUtils.initGraphLinks(this._cy, scope.component.componentInstancesRelations);
        this.commonGraphUtils.initUcpeChildren(this._cy);
    }

    private loadGraph = (scope:ICompositionGraphScope, el:JQuery) => {

        let graphEl = el.find('.sdc-composition-graph-wrapper');
        this.initGraph(graphEl, scope.isViewOnly);
        this.initDropZone(scope);
        this.registerCytoscapeGraphEvents(scope);
        this.registerCustomEvents(scope, el);
        this.initViewMode(scope.isViewOnly);

    };

    private initGraph(graphEl:JQuery, isViewOnly:boolean) {

        this._cy = cytoscape({
            container: graphEl,
            style: ComponentInstanceNodesStyle.getCompositionGraphStyle(),
            zoomingEnabled: true,
            maxZoom: 1.2,
            minZoom: .1,
            userZoomingEnabled: false,
            userPanningEnabled: true,
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

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_IN, (leftPaletteComponent:LeftPaletteComponent) => {
            this.$log.info(`composition-graph::registerEventServiceEvents:: palette hover on component: ${leftPaletteComponent.uniqueId}`);

            let nodesData = this.NodesGraphUtils.getAllNodesData(this._cy.nodes());
            let nodesLinks = this.GeneralGraphUtils.getAllCompositionCiLinks(this._cy);

            if (this.GeneralGraphUtils.componentRequirementsAndCapabilitiesCaching.containsKey(leftPaletteComponent.uniqueId)) {
                let cacheComponent = this.GeneralGraphUtils.componentRequirementsAndCapabilitiesCaching.getValue(leftPaletteComponent.uniqueId);
                let filteredNodesData = this.matchCapabilitiesRequirementsUtils.findMatchingNodes(cacheComponent, nodesData, nodesLinks);

                this.matchCapabilitiesRequirementsUtils.highlightMatchingComponents(filteredNodesData, this._cy);
                this.matchCapabilitiesRequirementsUtils.fadeNonMachingComponents(filteredNodesData, nodesData, this._cy);

                return;
            }

            //----------------------- ORIT TO FIX------------------------//

            this.ComponentServiceNg2.getCapabilitiesAndRequirements(leftPaletteComponent.componentType, leftPaletteComponent.uniqueId).subscribe((response: ComponentGenericResponse) => {

                    let component = this.ComponentFactory.createEmptyComponent(leftPaletteComponent.componentType);
                    component.uniqueId = component.uniqueId;
                    component.capabilities = response.capabilities;
                    component.requirements = response.requirements;
                    this.GeneralGraphUtils.componentRequirementsAndCapabilitiesCaching.setValue(leftPaletteComponent.uniqueId, component);
                    let filteredNodesData = this.matchCapabilitiesRequirementsUtils.findMatchingNodes(component, nodesData, nodesLinks);
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
            this.CompositionGraphPaletteUtils.onComponentDrag(this._cy, event, this.dragElement, this.dragComponent);

        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, (component:ComponentInstance) => {

            let selectedNode = this._cy.getElementById(component.uniqueId);
            selectedNode.data().componentInstance.name = component.name;
            selectedNode.data('name', component.name); //used for tooltip
            selectedNode.data('displayName', selectedNode.data().getDisplayName()); //abbreviated

        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, (componentInstance:ComponentInstance) => {
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

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_INSERT_NODE_TO_UCPE, (node:Cy.CollectionNodes, ucpe:Cy.CollectionNodes, updateExistingNode:boolean) => {

            this.commonGraphUtils.initUcpeChildData(node, ucpe);
            //check if item is a VL, and if so, skip adding the binding to ucpe
            if (!(node.data() instanceof CompositionCiNodeVl)) {
                this.CompositionGraphLinkUtils.createVfToUcpeLink(scope.component, this._cy, ucpe.data(), node.data()); //create link from the node to the ucpe
            }

            if (updateExistingNode) {
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

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_VERSION_CHANGED, (component:Component) => {
            scope.component = component;
            this._cy.elements().remove();
            this.loadGraphData(scope);
        });

        scope.zoom = (zoomIn: boolean):void => {
            let currentZoom: number = this._cy.zoom();
            if (zoomIn) {
                this.GeneralGraphUtils.zoomGraphTo(this._cy, currentZoom + .1);
            } else {
                this.GeneralGraphUtils.zoomGraphTo(this._cy, currentZoom - .1);
            }
        }

        //Zooms to fit all of the nodes in the collection passed in. If no nodes are passed in, will zoom to fit all nodes on graph
        scope.zoomAll = (nodes?:Cy.CollectionNodes) => {
            if (!nodes || !nodes.length) {
                nodes = this._cy.nodes();
            }
            
            scope.withSidebar = false;
            this._cy.animate({
                fit: { eles: nodes, padding: 20 },
                center: { eles: nodes }
            }, { duration: 400 });
        };

        scope.getAutoCompleteValues = (searchTerm: string) => {
            if (searchTerm.length > 1) { //US requirement: only display search results after 2nd letter typed.
                let nodes: Cy.CollectionNodes = this.NodesGraphUtils.getMatchingNodesByName(this._cy, searchTerm);
                scope.componentInstanceNames = _.map(nodes, node => node.data('name'));
            } else {
                scope.componentInstanceNames = [];
            }
        };

        scope.highlightSearchMatches = (searchTerm: string) => {           
            this.NodesGraphUtils.highlightMatchingNodesByName(this._cy, searchTerm);
            let matchingNodes: Cy.CollectionNodes = this.NodesGraphUtils.getMatchingNodesByName(this._cy, searchTerm);
            scope.zoomAll(matchingNodes);
        };

        scope.saveChangedCapabilityProperties = ():Promise<PropertyBEModel[]> => {
            return new Promise<PropertyBEModel[]>((resolve) => {
                const capabilityPropertiesBE: PropertyBEModel[] = this.ConnectionWizardServiceNg2.changedCapabilityProperties.map((prop) => {
                    prop.value = prop.getJSONValue();
                    const propBE = new PropertyBEModel(prop);
                    propBE.parentUniqueId = this.ConnectionWizardServiceNg2.selectedMatch.relationship.relation.capabilityOwnerId;
                    return propBE;
                });
                if (capabilityPropertiesBE.length > 0) {
                    // if there are capability properties to update, then first update capability properties and then resolve promise
                    this.ComponentInstanceServiceNg2
                        .updateInstanceCapabilityProperties(
                            scope.component,
                            this.ConnectionWizardServiceNg2.selectedMatch.toNode,
                            this.ConnectionWizardServiceNg2.selectedMatch.capability.type,
                            this.ConnectionWizardServiceNg2.selectedMatch.capability.name,
                            capabilityPropertiesBE
                        )
                        .subscribe((response) => {
                            console.log("Update resource instance capability properties response: ", response);
                            response.forEach((resProperty) => {
                                this.ConnectionWizardServiceNg2.selectedMatch.capabilityProperties.find((property) => {
                                    return property.uniqueId == resProperty.uniqueId;
                                }).value = resProperty.value;
                            });
                            this.ConnectionWizardServiceNg2.changedCapabilityProperties = [];
                            resolve(capabilityPropertiesBE);
                        });
                } else {
                    // no capability properties to update, immediately resolve promise
                    resolve(capabilityPropertiesBE);
                }
            });
        };

        scope.createLinkFromMenu = ():void => {
            scope.isLinkMenuOpen = false;

            scope.saveChangedCapabilityProperties().then(() => {
                //create link:
                this.CompositionGraphLinkUtils
                    .createLinkFromMenu(this._cy, this.ConnectionWizardServiceNg2.selectedMatch, scope.component);
            });
        };

        scope.hideRelationMenu = () => {
            this.commonGraphUtils.safeApply(scope, () => {
                delete scope.canvasMenuProps;
                this.$timeout.cancel(scope.relationMenuTimeout);
            });
        };

        scope.viewRelation = (link:Cy.CollectionEdges) => {
            scope.hideRelationMenu();

            const linkData = link.data();
            const sourceNode:CompositionCiNodeBase = link.source().data();
            const targetNode:CompositionCiNodeBase = link.target().data();
            const relationship:Relationship = linkData.relation.relationships[0];

            let capability:Capability;
            _.some(_.values(targetNode.componentInstance.capabilities), (capGroup) => {
                //item.uniqueId + item.ownerId + item.name === (selectedReqOrCapModel.uniqueId + selectedReqOrCapModel.ownerId + selectedReqOrCapModel.name)
                capability = _.find<Capability>(_.values<Capability>(capGroup), (cap:Capability) => (
                    cap.uniqueId === relationship.relation.capabilityUid &&
                    cap.ownerId === relationship.relation.capabilityOwnerId &&
                    cap.name === relationship.relation.capability
                ));
                return capability;
            });
            let requirement:Requirement;
            _.some(_.values(sourceNode.componentInstance.requirements), (reqGroup) => {
                requirement = _.find<Requirement>(_.values<Requirement>(reqGroup), (req:Requirement) => (
                    req.uniqueId === relationship.relation.requirementUid &&
                    req.ownerId === relationship.relation.requirementOwnerId &&
                    req.name === relationship.relation.requirement
                ));
                return requirement;
            });

            new Promise<{capability:Capability, requirement:Requirement}>((resolve, reject) => {
                if (capability && requirement) {
                    resolve({capability, requirement});
                }
                else {
                    scope.component.fetchRelation(relationship.relation.id).then((fetchedRelation) => {
                        resolve({
                            capability: fetchedRelation.relationships[0].capability,
                            requirement: fetchedRelation.relationships[0].requirement
                        });
                    }, reject);
                }
            }).then((objReqCap) => {
                capability = objReqCap.capability;
                requirement = objReqCap.requirement;

                this.ConnectionWizardServiceNg2.currentComponent = scope.component;
                this.ConnectionWizardServiceNg2.connectRelationModel = new ConnectRelationModel(sourceNode, targetNode, []);
                this.ConnectionWizardServiceNg2.selectedMatch = new Match(requirement, capability, true, linkData.source, linkData.target);
                this.ConnectionWizardServiceNg2.selectedMatch.relationship = relationship;

                const title = `Connection Properties`;
                const saveButton: ButtonModel = new ButtonModel('Save', 'blue', () => {
                    scope.saveChangedCapabilityProperties().then(() => { this.ModalServiceNg2.closeCurrentModal(); })
                });
                const cancelButton: ButtonModel = new ButtonModel('Cancel', 'white', () => { this.ModalServiceNg2.closeCurrentModal(); });
                const modal = new ModalModel('xl', title, '', [saveButton, cancelButton]);
                const modalInstance = this.ModalServiceNg2.createCustomModal(modal);
                this.ModalServiceNg2.addDynamicContentToModal(modalInstance, ConnectionPropertiesViewComponent);
                modalInstance.instance.open();

                this.ComponentInstanceServiceNg2.getInstanceCapabilityProperties(scope.component, linkData.target, capability.type, capability.name)
                    .subscribe((response: Array<PropertyModel>) => {
                        this.ConnectionWizardServiceNg2.selectedMatch.capabilityProperties = response;
                        this.ModalServiceNg2.addDynamicContentToModal(modalInstance, ConnectionPropertiesViewComponent);
                    }, (error) => {});
            }, (error) => {});
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

        /*
        scope.hideAssetPopover = ():void => {

            this.commonGraphUtils.safeApply(scope, () => {
                scope.assetPopoverOpen = false;
                scope.assetPopoverObj = null;
            });
        };

        scope.deleteNode = (nodeId:string):void => {
            if (!scope.isViewOnly) {
                this.NodesGraphUtils.confirmDeleteNode(nodeId, this._cy, scope.component);
                //scope.hideAssetPopover();
            }
        };*/
    }

    private registerCytoscapeGraphEvents(scope:ICompositionGraphScope) {

        this._cy.on('addedgemouseup', (event, data) => {
            scope.relationMenuDirectiveObj = this.CompositionGraphLinkUtils.onLinkDrawn(this._cy, data.source, data.target);
            if (scope.relationMenuDirectiveObj != null) {
                this.ConnectionWizardServiceNg2.setRelationMenuDirectiveObj(scope.relationMenuDirectiveObj);
                this.ConnectionWizardServiceNg2.currentComponent = scope.component;
                //TODO: init with the selected values
                this.ConnectionWizardServiceNg2.selectedMatch = null;
                
                let steps:Array<StepModel> = [];
                let fromNodeName:string = scope.relationMenuDirectiveObj.fromNode.componentInstance.name;
                let toNodeName:string = scope.relationMenuDirectiveObj.toNode.componentInstance.name;
                steps.push(new StepModel(fromNodeName, FromNodeStepComponent));
                steps.push(new StepModel(toNodeName, ToNodeStepComponent));
                steps.push(new StepModel('Properties', PropertiesStepComponent));
                let wizardTitle = 'Connect: ' + fromNodeName + ' to ' + toNodeName;
                let modalInstance = this.ModalServiceNg2.createMultiStepsWizard(wizardTitle, steps, scope.createLinkFromMenu, ConnectionWizardHeaderComponent);
                modalInstance.instance.open();

                //
                // this.ModalServiceNg2.createMultiStepsWizard('Connect', )Connect
                // scope.$apply(() => {
                //     scope.isLinkMenuOpen = true;
                // });
            }
        });
        this._cy.on('tapstart', 'node', (event:Cy.EventObject) => {
            this._currentlyCLickedNodePosition = angular.copy(event.cyTarget[0].position()); //update node position on drag
            if (event.cyTarget.data().isUcpe) {
                this._cy.nodes('.ucpe-cp').unlock();
                event.cyTarget.style('opacity', 0.5);
            }
            //scope.hideAssetPopover();
        });

        this._cy.on('drag', 'node', (event:Cy.EventObject) => {

            if (event.cyTarget.data().isDraggable) {
                event.cyTarget.style({'overlay-opacity': 0.24});
                if (this.GeneralGraphUtils.isValidDrop(this._cy, event.cyTarget)) {
                    event.cyTarget.style({'overlay-color': GraphColors.NODE_BACKGROUND_COLOR});
                } else {
                    event.cyTarget.style({'overlay-color': GraphColors.NODE_OVERLAPPING_BACKGROUND_COLOR});
                }
            }

            if (event.cyTarget.data().isUcpe) {
                let pos = event.cyTarget.position();

                this._cy.nodes('[?isInsideGroup]').positions((i, node)=> {
                    return {
                        x: pos.x + node.data("ucpeOffset").x,
                        y: pos.y + node.data("ucpeOffset").y
                    }
                });
            }
        });

       /* this._cy.on('mouseover', 'node', (event:Cy.EventObject) => {
            if (!this._cy.scratch('_edge_editation_highlights')) {
                this.commonGraphUtils.safeApply(scope, () => {
                    this.showNodePopoverMenu(scope, event.cyTarget[0]);
                });
            }
        });

        this._cy.on('mouseout', 'node', (event:Cy.EventObject) => {
            scope.hideAssetPopover();
        });*/
        this._cy.on('handlemouseover', (event, payload) => {

            if (payload.node.grabbed() || this._cy.scratch('_edge_editation_highlights') === true) { //no need to add opacity while we are dragging and hovering othe nodes- or if opacity was already calculated for these nodes
                return;
            }
            let nodesData = this.NodesGraphUtils.getAllNodesData(this._cy.nodes());
            let nodesLinks = this.GeneralGraphUtils.getAllCompositionCiLinks(this._cy);

            let linkableNodes = this.commonGraphUtils.getLinkableNodes(this._cy, payload.node);
            let filteredNodesData = this.matchCapabilitiesRequirementsUtils.findMatchingNodes(payload.node.data().componentInstance, linkableNodes, nodesLinks);
            this.matchCapabilitiesRequirementsUtils.highlightMatchingComponents(filteredNodesData, this._cy);
            this.matchCapabilitiesRequirementsUtils.fadeNonMachingComponents(filteredNodesData, nodesData, this._cy, payload.node.data());
            
            this._cy.scratch()._edge_editation_highlights = true;
            /*scope.hideAssetPopover();*/
        });

        this._cy.on('handlemouseout', () => {
            if (this._cy.scratch('_edge_editation_highlights') === true) {
                this._cy.removeScratch('_edge_editation_highlights');
                this._cy.emit('hidehandles');
                this.matchCapabilitiesRequirementsUtils.resetFadedNodes(this._cy);
            }
        });


        this._cy.on('tapend', (event:Cy.EventObject) => {

            if (event.cyTarget === this._cy) { //On Background clicked
                if (this._cy.$('node:selected').length === 0) { //if the background click but not dragged
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED);
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
                    if (isUcpe) {
                        nodesMoved = nodesMoved.add(this._cy.nodes('[?isInsideGroup]:free')); //'child' nodes will not be recognized as "grabbed" elements within cytoscape. manually add them to collection of nodes moved.
                    }
                    this.NodesGraphUtils.onNodesPositionChanged(this._cy, scope.component, nodesMoved);
                } else {
                    this.$log.debug('composition-graph::onNodeSelectedEvent:: fired');
                    scope.$apply(() => {
                        this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_NODE_SELECTED, event.cyTarget.data().componentInstance);
                        //open node popover menu
                        //this.showNodePopoverMenu(scope, event.cyTarget[0]);
                    });
                }

                if (isUcpe) {
                    this._cy.nodes('.ucpe-cp').lock();
                    event.cyTarget.style('opacity', 1);
                }

            }
        });

        this._cy.on('boxselect', 'node', (event:Cy.EventObject) => {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_NODE_SELECTED, event.cyTarget.data().componentInstance);
        });
    }

    /*
    private showNodePopoverMenu = (scope:ICompositionGraphScope, node:Cy.CollectionNodes) => {

        scope.assetPopoverObj = this.NodesGraphUtils.createAssetPopover(this._cy, node, scope.isViewOnly);
        scope.assetPopoverOpen = true;

    };*/
    private openModifyLinkMenu = (scope:ICompositionGraphScope, linkMenuObject:LinkMenu, timeOutInMilliseconds?:number) => {
        scope.hideRelationMenu();
        this.$timeout(() => {
            scope.canvasMenuProps = {
                open: true,
                styleClass: 'w-sdc-canvas-menu-list',
                items: [],
                position: {
                    x: `${linkMenuObject.position.x}px`,
                    y: `${linkMenuObject.position.y}px`
                }
            };

            if (this._cy.$('edge:selected').length === 1) {
                scope.canvasMenuProps.items.push({
                    contents: 'View',
                    styleClass: 'w-sdc-canvas-menu-item-view',
                    action: () => {
                        scope.viewRelation(<Cy.CollectionEdges>linkMenuObject.link);
                    }
                });
            }
            scope.canvasMenuProps.items.push({
                contents: 'Delete',
                styleClass: 'w-sdc-canvas-menu-item-delete',
                action: () => {
                    scope.deleteRelation(<Cy.CollectionEdges>linkMenuObject.link);
                }
            });

            scope.relationMenuTimeout = this.$timeout(() => {
                scope.hideRelationMenu();
            }, timeOutInMilliseconds ? timeOutInMilliseconds : 6000);
        });
    };

    private initGraphNodes(componentInstances:ComponentInstance[], isViewOnly:boolean) {

        if (!isViewOnly) { //Init nodes handle extension - enable dynamic links
            setTimeout(()=> {
                let handles = new CytoscapeEdgeEditation;
                handles.init(this._cy, 18);
                handles.registerHandle(ComponentInstanceNodesStyle.getBasicNodeHanlde());
                handles.registerHandle(ComponentInstanceNodesStyle.getBasicSmallNodeHandle());
                handles.registerHandle(ComponentInstanceNodesStyle.getUcpeCpNodeHandle());
            }, 0);
        }

        _.each(componentInstances, (instance) => {
            let compositionGraphNode:CompositionCiNodeBase = this.NodesFactory.createNode(instance);
            this.commonGraphUtils.addComponentInstanceNodeToGraph(this._cy, compositionGraphNode);
        });
    }


    private initDropZone(scope:ICompositionGraphScope) {

        if (scope.isViewOnly) {
            return;
        }
        scope.dropCallback = (event:IDragDropEvent) => {
            this.$log.debug(`composition-graph::dropCallback:: fired`);
            this.CompositionGraphPaletteUtils.addNodeFromPalette(this._cy, event, scope.component);
        };

        scope.verifyDrop = (event:JQueryEventObject) => {

            if (this.dragElement.hasClass('red')) {
                return false;
            }
            return true;
        };

        scope.beforeDropCallback = (event:IDragDropEvent):ng.IPromise<void> => {
            let deferred:ng.IDeferred<void> = this.$q.defer<void>();
            if (this.dragElement.hasClass('red')) {
                deferred.reject();
            } else {
                deferred.resolve();
            }

            return deferred.promise;
        }
    }

    public static factory = ($q,
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
                             MatchCapabilitiesRequirementsUtils,
                             CompositionGraphPaletteUtils,
                             ComponentServiceNg2,
                             ModalService,
                             ConnectionWizardService,
                             ComponentInstanceServiceNg2) => {
        return new CompositionGraph(
            $q,
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
            MatchCapabilitiesRequirementsUtils,
            CompositionGraphPaletteUtils,
            ComponentServiceNg2,
            ModalService,
            ConnectionWizardService,
            ComponentInstanceServiceNg2);
    }
}

CompositionGraph.factory.$inject = [
    '$q',
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
    'MatchCapabilitiesRequirementsUtils',
    'CompositionGraphPaletteUtils',
    'ComponentServiceNg2',
    'ModalServiceNg2',
    'ConnectionWizardServiceNg2',
    'ComponentInstanceServiceNg2'
];
