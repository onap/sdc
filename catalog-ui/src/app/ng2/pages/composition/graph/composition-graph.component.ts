/**
 * Created by ob0695 on 4/24/2018.
 */
import { AfterViewInit, Component, ElementRef, HostBinding, Input } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import {
    ButtonModel,
    Component as TopologyTemplate,
    ComponentInstance,
    CompositionCiNodeBase,
    ConnectRelationModel,
    GroupInstance,
    LeftPaletteComponent,
    LinkMenu,
    Match,
    ModalModel,
    NodesFactory,
    Point,
    PolicyInstance,
    PropertyBEModel,
    Relationship,
    StepModel,
    Zone,
    ZoneInstance,
    ZoneInstanceAssignmentType,
    ZoneInstanceMode,
    ZoneInstanceType
} from 'app/models';
import { ForwardingPath } from 'app/models/forwarding-path';
import { CompositionCiServicePathLink } from 'app/models/graph/graph-links/composition-graph-links/composition-ci-service-path-link';
import { UIZoneInstanceObject } from 'app/models/ui-models/ui-zone-instance-object';
import { CompositionService } from 'app/ng2/pages/composition/composition.service';
import { CommonGraphUtils } from 'app/ng2/pages/composition/graph/common/common-graph-utils';
import { ComponentInstanceNodesStyle } from 'app/ng2/pages/composition/graph/common/style/component-instances-nodes-style';
import { ConnectionPropertiesViewComponent } from 'app/ng2/pages/composition/graph/connection-wizard/connection-properties-view/connection-properties-view.component';
import { ConnectionWizardHeaderComponent } from 'app/ng2/pages/composition/graph/connection-wizard/connection-wizard-header/connection-wizard-header.component';
import { ConnectionWizardService } from 'app/ng2/pages/composition/graph/connection-wizard/connection-wizard.service';
import { FromNodeStepComponent } from 'app/ng2/pages/composition/graph/connection-wizard/from-node-step/from-node-step.component';
import { PropertiesStepComponent } from 'app/ng2/pages/composition/graph/connection-wizard/properties-step/properties-step.component';
import { ToNodeStepComponent } from 'app/ng2/pages/composition/graph/connection-wizard/to-node-step/to-node-step.component';
import { WorkspaceService } from 'app/ng2/pages/workspace/workspace.service';
import { ComponentInstanceServiceNg2 } from 'app/ng2/services/component-instance-services/component-instance.service';
import { TopologyTemplateService } from 'app/ng2/services/component-services/topology-template.service';
import { ModalService } from 'app/ng2/services/modal.service';
import { ComponentGenericResponse } from 'app/ng2/services/responses/component-generic-response';
import { ServiceGenericResponse } from 'app/ng2/services/responses/service-generic-response';
import { WorkspaceState } from 'app/ng2/store/states/workspace.state';
import { EventListenerService } from 'app/services';
import { ComponentInstanceFactory, EVENTS, SdcElementType } from 'app/utils';
import { ComponentType, GRAPH_EVENTS, GraphColors, DEPENDENCY_EVENTS } from 'app/utils/constants';
import * as _ from 'lodash';
import { DndDropEvent } from 'ngx-drag-drop/ngx-drag-drop';
import { SdcUiServices } from 'onap-ui-angular';
import { NotificationSettings } from 'onap-ui-angular/dist/notifications/utilities/notification.config';
import { menuItem } from 'onap-ui-angular/dist/simple-popup-menu/menu-data.interface';
import { CytoscapeEdgeEditation } from '../../../../../third-party/cytoscape.js-edge-editation/CytoscapeEdgeEditation.js';
import { SelectedComponentType, SetSelectedComponentAction } from '../common/store/graph.actions';
import { GraphState } from '../common/store/graph.state';
import {
    CompositionGraphGeneralUtils,
    CompositionGraphNodesUtils,
    CompositionGraphZoneUtils,
    MatchCapabilitiesRequirementsUtils
} from './utils';
import { CompositionGraphLinkUtils } from './utils/composition-graph-links-utils';
import { CompositionGraphPaletteUtils } from './utils/composition-graph-palette-utils';
import { ServicePathGraphUtils } from './utils/composition-graph-service-path-utils';

declare const window: any;

@Component({
    selector: 'composition-graph',
    templateUrl: './composition-graph.component.html',
    styleUrls: ['./composition-graph.component.less']
})

export class CompositionGraphComponent implements AfterViewInit {

    @Select(WorkspaceState.isViewOnly) isViewOnly$: boolean;
    @Select(GraphState.withSidebar) withSidebar$: boolean;
    @Input() topologyTemplate: TopologyTemplate;
    @HostBinding('attr.data-tests-id') dataTestId: string;
    @Input() testId: string;

    // tslint:disable:variable-name
    private _cy: Cy.Instance;
    private zoneTagMode: string;
    private activeZoneInstance: ZoneInstance;
    private zones: Zone[];
    private currentlyClickedNodePosition: Cy.Position;
    private dragElement: JQuery;
    private dragComponent: ComponentInstance;
    private componentInstanceNames: string[];
    private topologyTemplateId: string;
    private topologyTemplateType: string;

    constructor(private elRef: ElementRef,
                private nodesFactory: NodesFactory,
                private eventListenerService: EventListenerService,
                private compositionGraphZoneUtils: CompositionGraphZoneUtils,
                private generalGraphUtils: CompositionGraphGeneralUtils,
                private compositionGraphLinkUtils: CompositionGraphLinkUtils,
                private nodesGraphUtils: CompositionGraphNodesUtils,
                private connectionWizardService: ConnectionWizardService,
                private commonGraphUtils: CommonGraphUtils,
                private modalService: ModalService,
                private compositionGraphPaletteUtils: CompositionGraphPaletteUtils,
                private topologyTemplateService: TopologyTemplateService,
                private componentInstanceService: ComponentInstanceServiceNg2,
                private matchCapabilitiesRequirementsUtils: MatchCapabilitiesRequirementsUtils,
                private store: Store,
                private compositionService: CompositionService,
                private loaderService: SdcUiServices.LoaderService,
                private workspaceService: WorkspaceService,
                private notificationService: SdcUiServices.NotificationsService,
                private simplePopupMenuService: SdcUiServices.simplePopupMenuService,
                private servicePathGraphUtils: ServicePathGraphUtils) {
    }

    ngOnInit() {
        this.dataTestId = this.testId;
        this.topologyTemplateId = this.workspaceService.metadata.uniqueId;
        this.topologyTemplateType = this.workspaceService.metadata.componentType;

        this.store.dispatch(new SetSelectedComponentAction({
            component: this.topologyTemplate,
            type: SelectedComponentType.TOPOLOGY_TEMPLATE
        }));
        this.eventListenerService.registerObserverCallback(EVENTS.ON_CHECKOUT, () => {
            this.loadGraphData();
        });
        this.loadCompositionData();
    }

    ngAfterViewInit() {
        this.loadGraph();
    }

    ngOnDestroy() {
        this._cy.destroy();
        _.forEach(GRAPH_EVENTS, (event) => {
            this.eventListenerService.unRegisterObserver(event);
        });
        this.eventListenerService.unRegisterObserver(EVENTS.ON_CHECKOUT);
        this.eventListenerService.unRegisterObserver(DEPENDENCY_EVENTS.ON_DEPENDENCY_CHANGE);
    }

    public isViewOnly = (): boolean => {
        return this.store.selectSnapshot((state) => state.workspace.isViewOnly);
    }

    public zoom = (zoomIn: boolean): void => {
        const currentZoom: number = this._cy.zoom();
        if (zoomIn) {
            this.generalGraphUtils.zoomGraphTo(this._cy, currentZoom + .1);
        } else {
            this.generalGraphUtils.zoomGraphTo(this._cy, currentZoom - .1);
        }
    }

    public zoomAllWithoutSidebar = () => {
        setTimeout(() => { // wait for sidebar changes to take effect before zooming
            this.generalGraphUtils.zoomAll(this._cy);
        });
    }

    public getAutoCompleteValues = (searchTerm: string) => {
        if (searchTerm.length > 1) { // US requirement: only display search results after 2nd letter typed.
            const nodes: Cy.CollectionNodes = this.nodesGraphUtils.getMatchingNodesByName(this._cy, searchTerm);
            this.componentInstanceNames = _.map(nodes, (node) => node.data('name'));
        } else {
            this.componentInstanceNames = [];
        }
    }

    public highlightSearchMatches = (searchTerm: string) => {
        this.nodesGraphUtils.highlightMatchingNodesByName(this._cy, searchTerm);
        const matchingNodes: Cy.CollectionNodes = this.nodesGraphUtils.getMatchingNodesByName(this._cy, searchTerm);
        this.generalGraphUtils.zoomAll(this._cy, matchingNodes);
    }

    public onDrop = (dndEvent: DndDropEvent) => {
        this.compositionGraphPaletteUtils.addNodeFromPalette(this._cy, dndEvent);
    }

    public openServicePathMenu = ($event): void => {

        const menuConfig: menuItem[] = [];
        if (!this.isViewOnly()) {
            menuConfig.push({
                text: 'Create Service Flow',
                action: () => this.servicePathGraphUtils.onCreateServicePath()
            });
        }
        menuConfig.push({
            text: 'Service Flows List',
            type: '',
            action: () => this.servicePathGraphUtils.onListServicePath()
        });
        const popup = this.simplePopupMenuService.openBaseMenu(menuConfig, {
            x: $event.x,
            y: $event.y
        });

    }

    public deletePathsOnCy = () => {
        this.servicePathGraphUtils.deletePathsFromGraph(this._cy);
    }

    public drawPathOnCy = (data: ForwardingPath) => {
        this.servicePathGraphUtils.drawPath(this._cy, data);
    }

    public onTapEnd = (event: Cy.EventObject) => {
        if (this.zoneTagMode) {
            return;
        }
        if (event.cyTarget === this._cy) { // On Background clicked
            if (this._cy.$('node:selected').length === 0) { // if the background click but not dragged
                if (this.activeZoneInstance) {
                    this.unsetActiveZoneInstance();
                    this.selectTopologyTemplate();
                } else {
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED);
                    this.selectTopologyTemplate();
                }

            }
        } else if (event.cyTarget[0].isEdge()) { // and Edge clicked
            this.compositionGraphLinkUtils.handleLinkClick(this._cy, event);
            if (event.cyTarget[0].data().type === CompositionCiServicePathLink.LINK_TYPE) {
                return;
            }
            this.openModifyLinkMenu(this.compositionGraphLinkUtils.getModifyLinkMenu(event.cyTarget[0], event), event);
        } else { // On Node clicked

            this._cy.nodes(':grabbed').style({'overlay-opacity': 0});

            const newPosition = event.cyTarget[0].position();
            // node position changed (drop after drag event) - we need to update position
            if (this.currentlyClickedNodePosition.x !== newPosition.x || this.currentlyClickedNodePosition.y !== newPosition.y) {
                const nodesMoved: Cy.CollectionNodes = this._cy.$(':grabbed');
                this.nodesGraphUtils.onNodesPositionChanged(this._cy, this.topologyTemplate, nodesMoved);
            } else {
                if (this.activeZoneInstance) {
                    this.unsetActiveZoneInstance();
                }
                this.selectComponentInstance(event.cyTarget[0].data().componentInstance);
            }
        }
    }

    private registerCytoscapeGraphEvents() {

        this._cy.on('addedgemouseup', (event, data) => {
            const connectRelationModel: ConnectRelationModel = this.compositionGraphLinkUtils.onLinkDrawn(this._cy, data.source, data.target);
            if (connectRelationModel != null) {
                this.connectionWizardService.setRelationMenuDirectiveObj(connectRelationModel);
                this.connectionWizardService.selectedMatch = null;

                const steps: StepModel[] = [];
                const fromNodeName: string = connectRelationModel.fromNode.componentInstance.name;
                const toNodeName: string = connectRelationModel.toNode.componentInstance.name;
                steps.push(new StepModel(fromNodeName, FromNodeStepComponent));
                steps.push(new StepModel(toNodeName, ToNodeStepComponent));
                steps.push(new StepModel('Properties', PropertiesStepComponent));
                const wizardTitle = 'Connect: ' + fromNodeName + ' to ' + toNodeName;
                const modalInstance = this.modalService.createMultiStepsWizard(wizardTitle, steps, this.createLinkFromMenu, ConnectionWizardHeaderComponent);
                modalInstance.instance.open();
            }
        });

        this._cy.on('tapstart', 'node', (event: Cy.EventObject) => {
            this.currentlyClickedNodePosition = angular.copy(event.cyTarget[0].position()); // update node position on drag
        });

        this._cy.on('drag', 'node', (event: Cy.EventObject) => {
            if (event.cyTarget.data().componentSubType !== SdcElementType.POLICY && event.cyTarget.data().componentSubType !== SdcElementType.GROUP) {
                event.cyTarget.style({'overlay-opacity': 0.24});
                if (this.generalGraphUtils.isValidDrop(this._cy, event.cyTarget)) {
                    event.cyTarget.style({'overlay-color': GraphColors.NODE_BACKGROUND_COLOR});
                } else {
                    event.cyTarget.style({'overlay-color': GraphColors.NODE_OVERLAPPING_BACKGROUND_COLOR});
                }
            }
        });

        this._cy.on('handlemouseover', (event, payload) => {
            // no need to add opacity while we are dragging and hovering othe nodes- or if opacity was already calculated for these nodes
            if (payload.node.grabbed() || this._cy.scratch('_edge_editation_highlights') === true) {
                return;
            }

            if (this.zoneTagMode) {
                this.zoneTagMode = this.zones[this.activeZoneInstance.type].getHoverTagModeId();
                return;
            }

            const nodesData = this.nodesGraphUtils.getAllNodesData(this._cy.nodes());
            const nodesLinks = this.generalGraphUtils.getAllCompositionCiLinks(this._cy);
            const instance = payload.node.data().componentInstance;
            const filteredNodesData = this.matchCapabilitiesRequirementsUtils.findMatchingNodesToComponentInstance(instance, nodesData, nodesLinks);
            this.matchCapabilitiesRequirementsUtils.highlightMatchingComponents(filteredNodesData, this._cy);
            this.matchCapabilitiesRequirementsUtils.fadeNonMachingComponents(filteredNodesData, nodesData, this._cy, payload.node.data());

            this._cy.scratch()._edge_editation_highlights = true;
        });

        this._cy.on('handlemouseout', () => {
            if (this.zoneTagMode) {
                this.zoneTagMode = this.zones[this.activeZoneInstance.type].getTagModeId();
                return;
            }
            if (this._cy.scratch('_edge_editation_highlights') === true) {
                this._cy.removeScratch('_edge_editation_highlights');
                this._cy.emit('hidehandles');
                this.matchCapabilitiesRequirementsUtils.resetFadedNodes(this._cy);
            }
        });

        this._cy.on('tapend', (event: Cy.EventObject) => {
            this.onTapEnd(event);
        });

        this._cy.on('boxselect', 'node', (event: Cy.EventObject) => {
            this.unsetActiveZoneInstance();
            this.selectComponentInstance(event.cyTarget.data().componentInstance);
        });

        this._cy.on('canvasredraw', (event: Cy.EventObject) => {
            if (this.zoneTagMode) {
                this.compositionGraphZoneUtils.showZoneTagIndications(this._cy, this.activeZoneInstance);
            }
        });

        this._cy.on('handletagclick', (event: Cy.EventObject, eventData: any) => {
            this.compositionGraphZoneUtils.handleTagClick(this._cy, this.activeZoneInstance, eventData.nodeId);
        });
    }

    private initViewMode() {

        if (this.isViewOnly()) {
            // remove event listeners
            this._cy.off('drag');
            this._cy.off('handlemouseout');
            this._cy.off('handlemouseover');
            this._cy.off('canvasredraw');
            this._cy.off('handletagclick');
            this._cy.edges().unselectify();
        }
    }

    private saveChangedCapabilityProperties = (): Promise<PropertyBEModel[]> => {
        return new Promise<PropertyBEModel[]>((resolve) => {
            const capabilityPropertiesBE: PropertyBEModel[] = this.connectionWizardService.changedCapabilityProperties.map((prop) => {
                prop.value = prop.getJSONValue();
                const propBE = new PropertyBEModel(prop);
                propBE.parentUniqueId = this.connectionWizardService.selectedMatch.relationship.relation.capabilityOwnerId;
                return propBE;
            });
            if (capabilityPropertiesBE.length > 0) {
                // if there are capability properties to update, then first update capability properties and then resolve promise
                this.componentInstanceService
                    .updateInstanceCapabilityProperties(
                        this.topologyTemplate,
                        this.connectionWizardService.selectedMatch.toNode,
                        this.connectionWizardService.selectedMatch.capability,
                        capabilityPropertiesBE
                    )
                    .subscribe((response) => {
                        console.log('Update resource instance capability properties response: ', response);
                        this.connectionWizardService.changedCapabilityProperties = [];
                        resolve(capabilityPropertiesBE);
                    });
            } else {
                // no capability properties to update, immediately resolve promise
                resolve(capabilityPropertiesBE);
            }
        });
    }

    private loadCompositionData = () => {
        this.loaderService.activate();
        this.topologyTemplateService.getComponentCompositionData(this.topologyTemplateId, this.topologyTemplateType).subscribe((response: ComponentGenericResponse) => {
            if (this.topologyTemplateType === ComponentType.SERVICE) {
                this.compositionService.forwardingPaths = (response as ServiceGenericResponse).forwardingPaths;
            }
            this.compositionService.componentInstances = response.componentInstances;
            this.compositionService.componentInstancesRelations = response.componentInstancesRelations;
            this.compositionService.groupInstances = response.groupInstances;
            this.compositionService.policies = response.policies;
            this.loadGraphData();
            this.loaderService.deactivate();
        }, (error) => { this.loaderService.deactivate(); });
    }

    private loadGraph = () => {
        const graphEl = this.elRef.nativeElement.querySelector('.sdc-composition-graph-wrapper');
        this.initGraph(graphEl);
        this.zones = this.compositionGraphZoneUtils.createCompositionZones();
        this.registerCytoscapeGraphEvents();
        this.registerCustomEvents();
        this.initViewMode();
    }

    private initGraphNodes() {

        setTimeout(() => {
            const handles = new CytoscapeEdgeEditation();
            handles.init(this._cy);
            if (!this.isViewOnly()) { // Init nodes handle extension - enable dynamic links
                handles.initNodeEvents();
                handles.registerHandle(ComponentInstanceNodesStyle.getAddEdgeHandle());
            }
            handles.registerHandle(ComponentInstanceNodesStyle.getTagHandle());
            handles.registerHandle(ComponentInstanceNodesStyle.getTaggedPolicyHandle());
            handles.registerHandle(ComponentInstanceNodesStyle.getTaggedGroupHandle());
        }, 0);

        _.each(this.compositionService.componentInstances, (instance) => {
            const compositionGraphNode: CompositionCiNodeBase = this.nodesFactory.createNode(instance);
            this.commonGraphUtils.addComponentInstanceNodeToGraph(this._cy, compositionGraphNode);
        });

    }

    private loadGraphData = () => {
        this.initGraphNodes();
        this.compositionGraphLinkUtils.initGraphLinks(this._cy, this.compositionService.componentInstancesRelations);
        this.compositionGraphZoneUtils.initZoneInstances(this.zones);
        setTimeout(() => { // Need setTimeout so that angular canvas changes will take effect before resize & center
            this.generalGraphUtils.zoomAllWithMax(this._cy, 1);
        });
        this.componentInstanceNames = _.map(this._cy.nodes(), (node) => node.data('name'));
    }

    private initGraph(graphEl: JQuery) {

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
            autolock: this.isViewOnly(),
            autoungrabify: this.isViewOnly()
        });

        // Testing Bridge that allows Cypress tests to select a component on canvas not via DOM
        if (window.Cypress) {
            window.testBridge = this.createCanvasTestBridge();
        }
    }

    private createCanvasTestBridge(): any {
        return {
            selectComponentInstance: (componentName: string) => {
                const matchingNodesByName = this.nodesGraphUtils.getMatchingNodesByName(this._cy, componentName);
                const component = new ComponentInstance(matchingNodesByName.first().data().componentInstance);
                this.selectComponentInstance(component);
            }
        };
    }

    // -------------------------------------------- ZONES---------------------------------------------------------//
    private zoneMinimizeToggle = (zoneType: ZoneInstanceType): void => {
        this.zones[zoneType].minimized = !this.zones[zoneType].minimized;
    }

    private zoneInstanceModeChanged = (newMode: ZoneInstanceMode, instance: ZoneInstance, zoneId: ZoneInstanceType): void => {
        if (this.zoneTagMode) { // we're in tag mode.
            if (instance === this.activeZoneInstance && newMode === ZoneInstanceMode.NONE) { // we want to turn tag mode off.
                this.zoneTagMode = null;
                this.activeZoneInstance.mode = ZoneInstanceMode.SELECTED;
                this.compositionGraphZoneUtils.endCyTagMode(this._cy);
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_CANVAS_TAG_END, instance);

            }
        } else {
            // when active zone instance gets hover/none, don't actually change mode, just show/hide indications
            if (instance !== this.activeZoneInstance || (instance === this.activeZoneInstance && newMode > ZoneInstanceMode.HOVER)) {
                instance.mode = newMode;
            }

            if (newMode === ZoneInstanceMode.NONE) {
                this.compositionGraphZoneUtils.hideZoneTagIndications(this._cy);
                if (this.zones[ZoneInstanceType.GROUP]) {
                    this.compositionGraphZoneUtils.hideGroupZoneIndications(this.zones[ZoneInstanceType.GROUP].instances);
                }
            }
            if (newMode >= ZoneInstanceMode.HOVER) {
                this.compositionGraphZoneUtils.showZoneTagIndications(this._cy, instance);
                if (instance.type === ZoneInstanceType.POLICY && this.zones[ZoneInstanceType.GROUP]) {
                    this.compositionGraphZoneUtils.showGroupZoneIndications(this.zones[ZoneInstanceType.GROUP].instances, instance);
                }
            }
            if (newMode >= ZoneInstanceMode.SELECTED) {
                this._cy.$('node:selected').unselect();
                if (this.activeZoneInstance && this.activeZoneInstance !== instance && newMode >= ZoneInstanceMode.SELECTED) {
                    this.activeZoneInstance.mode = ZoneInstanceMode.NONE;
                }
                this.activeZoneInstance = instance;
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_ZONE_INSTANCE_SELECTED, instance);
                this.store.dispatch(new SetSelectedComponentAction({
                    component: instance.instanceData,
                    type: SelectedComponentType[ZoneInstanceType[instance.type]]
                }));
            }
            if (newMode === ZoneInstanceMode.TAG) {
                this.compositionGraphZoneUtils.startCyTagMode(this._cy);
                this.zoneTagMode = this.zones[zoneId].getTagModeId();
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_CANVAS_TAG_START, zoneId);
            }
        }
    }

    private zoneInstanceTagged = (taggedInstance: ZoneInstance) => {
        this.activeZoneInstance.addOrRemoveAssignment(taggedInstance.instanceData.uniqueId, ZoneInstanceAssignmentType.GROUPS);
        const newHandle: string = this.compositionGraphZoneUtils.getCorrectHandleForNode(taggedInstance.instanceData.uniqueId, this.activeZoneInstance);
        taggedInstance.showHandle(newHandle);
    }

    private unsetActiveZoneInstance = (): void => {
        if (this.activeZoneInstance) {
            this.activeZoneInstance.mode = ZoneInstanceMode.NONE;
            this.activeZoneInstance = null;
            this.zoneTagMode = null;
        }
    }

    private selectComponentInstance = (componentInstance: ComponentInstance) => {
        this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_NODE_SELECTED, componentInstance);
        this.store.dispatch(new SetSelectedComponentAction({
            component: componentInstance,
            type: SelectedComponentType.COMPONENT_INSTANCE
        }));
    }

    private selectTopologyTemplate = () => {
        this.store.dispatch(new SetSelectedComponentAction({
            component: this.topologyTemplate,
            type: SelectedComponentType.TOPOLOGY_TEMPLATE
        }));
    }

    private zoneBackgroundClicked = (): void => {
        if (!this.zoneTagMode && this.activeZoneInstance) {
            this.unsetActiveZoneInstance();
            this.selectTopologyTemplate();
        }
    }

    private zoneAssignmentSaveStart = () => {
        this.loaderService.activate();
    }

    private zoneAssignmentSaveComplete = (success: boolean) => {
        this.loaderService.deactivate();
        if (!success) {
            this.notificationService.push(new NotificationSettings('error', 'Update Failed', 'Error'));
        }
    }

    private deleteZoneInstance = (deletedInstance: UIZoneInstanceObject) => {
        if (deletedInstance.type === ZoneInstanceType.POLICY) {
            this.compositionService.policies = this.compositionService.policies.filter((policy) => policy.uniqueId !== deletedInstance.uniqueId);
        } else if (deletedInstance.type === ZoneInstanceType.GROUP) {
            this.compositionService.groupInstances = this.compositionService.groupInstances.filter((group) => group.uniqueId !== deletedInstance.uniqueId);
        }
        // remove it from zones
        this.zones[deletedInstance.type].removeInstance(deletedInstance.uniqueId);
        if (deletedInstance.type === ZoneInstanceType.GROUP && !_.isEmpty(this.zones[ZoneInstanceType.POLICY])) {
            this.compositionGraphZoneUtils.updateTargetsOrMembersOnCanvasDelete(deletedInstance.uniqueId, [this.zones[ZoneInstanceType.POLICY]], ZoneInstanceAssignmentType.GROUPS);
        }
        this.selectTopologyTemplate();
    }
    // -------------------------------------------------------------------------------------------------------------//

    private registerCustomEvents() {

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, (groupInstance: GroupInstance) => {
            this.compositionGraphZoneUtils.findAndUpdateZoneInstanceData(this.zones, groupInstance);
            this.notificationService.push(new NotificationSettings('success', 'Group Updated', 'Success'));
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, (policyInstance: PolicyInstance) => {
            this.compositionGraphZoneUtils.findAndUpdateZoneInstanceData(this.zones, policyInstance);
            this.notificationService.push(new NotificationSettings('success', 'Policy Updated', 'Success'));
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_IN, (leftPaletteComponent: LeftPaletteComponent) => {
            this.compositionGraphPaletteUtils.onComponentHoverIn(leftPaletteComponent, this._cy);
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_ADD_ZONE_INSTANCE_FROM_PALETTE,
            (component: TopologyTemplate, paletteComponent: LeftPaletteComponent, startPosition: Point) => {

                const zoneType: ZoneInstanceType = this.compositionGraphZoneUtils.getZoneTypeForPaletteComponent(paletteComponent.categoryType);
                this.compositionGraphZoneUtils.showZone(this.zones[zoneType]);

                this.loaderService.activate();
                this.compositionGraphZoneUtils.createZoneInstanceFromLeftPalette(zoneType, paletteComponent.type).subscribe((zoneInstance: ZoneInstance) => {
                    this.loaderService.deactivate();
                    this.compositionGraphZoneUtils.addInstanceToZone(this.zones[zoneInstance.type], zoneInstance, true);
                    this.compositionGraphZoneUtils.createPaletteToZoneAnimation(startPosition, zoneType, zoneInstance);
                }, (error) => {
                    this.loaderService.deactivate();
                });
            });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_OUT, () => {
            this._cy.emit('hidehandles');
            this.matchCapabilitiesRequirementsUtils.resetFadedNodes(this._cy);
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_START, (dragElement, dragComponent) => {
            this.dragElement = dragElement;
            this.dragComponent = ComponentInstanceFactory.createComponentInstanceFromComponent(dragComponent);
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_ACTION, (position: Point) => {
            const draggedElement = document.getElementById('draggable_element');
            draggedElement.className = this.compositionGraphPaletteUtils.isDragValid(this._cy, position) ? 'valid-drag' : 'invalid-drag';
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DROP, (event: DndDropEvent) => {
            this.onDrop(event);
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, (component: ComponentInstance) => {
            const selectedNode = this._cy.getElementById(component.uniqueId);
            selectedNode.data().componentInstance.name = component.name;
            selectedNode.data('name', component.name); // used for tooltip
            selectedNode.data('displayName', selectedNode.data().getDisplayName()); // abbreviated
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, (componentInstanceId: string) => {
            const nodeToDelete = this._cy.getElementById(componentInstanceId);
            this.nodesGraphUtils.deleteNode(this._cy, this.topologyTemplate, nodeToDelete);
            this.selectTopologyTemplate();
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_ZONE_INSTANCE, (deletedInstance: UIZoneInstanceObject) => {
            this.deleteZoneInstance(deletedInstance);
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE_SUCCESS, (componentInstanceId: string) => {
            if (!_.isEmpty(this.zones)) {
                this.compositionGraphZoneUtils.updateTargetsOrMembersOnCanvasDelete(componentInstanceId, this.zones, ZoneInstanceAssignmentType.COMPONENT_INSTANCES);
            }
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_EDGE, (releaseLoading: boolean, linksToDelete: Cy.CollectionEdges) => {
            this.compositionGraphLinkUtils.deleteLink(this._cy, this.topologyTemplate, releaseLoading, linksToDelete);
        });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_VERSION_CHANGED, (component: ComponentInstance) => {
            // Remove everything from graph and reload it all
            this._cy.elements().remove();
            this.loadCompositionData();
            setTimeout(() => { this._cy.getElementById(component.uniqueId).select(); }, 1000);
            this.selectComponentInstance(component);
        });
        this.eventListenerService.registerObserverCallback(DEPENDENCY_EVENTS.ON_DEPENDENCY_CHANGE, (ischecked: boolean) => {
            if (ischecked) {
                this._cy.$('node:selected').addClass('dependent');
            } else {
                // due to defect in cytoscape, just changing the class does not replace the icon, and i need to revert to original icon with no markings.
                this._cy.$('node:selected').removeClass('dependent');
                this._cy.$('node:selected').style({'background-image': this._cy.$('node:selected').data('originalImg')});
            }
        });
    }
    private createLinkFromMenu = (): void => {
        this.saveChangedCapabilityProperties().then(() => {
            this.compositionGraphLinkUtils.createLinkFromMenu(this._cy, this.connectionWizardService.selectedMatch);
        });
    }

    private deleteRelation = (link: Cy.CollectionEdges) => {
        // if multiple edges selected, delete the VL itself so edges get deleted automatically
        if (this._cy.$('edge:selected').length > 1) {
            this.nodesGraphUtils.deleteNode(this._cy, this.topologyTemplate, this._cy.$('node:selected'));
        } else {
            this.compositionGraphLinkUtils.deleteLink(this._cy, this.topologyTemplate, true, link);
        }
    }

    private viewRelation = (link: Cy.CollectionEdges) => {

        const linkData = link.data();
        const sourceNode: CompositionCiNodeBase = link.source().data();
        const targetNode: CompositionCiNodeBase = link.target().data();
        const relationship: Relationship = linkData.relation.relationships[0];

        this.compositionGraphLinkUtils.getRelationRequirementCapability(relationship, sourceNode.componentInstance, targetNode.componentInstance).then((objReqCap) => {
            const capability = objReqCap.capability;
            const requirement = objReqCap.requirement;

            this.connectionWizardService.connectRelationModel = new ConnectRelationModel(sourceNode, targetNode, []);
            this.connectionWizardService.selectedMatch = new Match(requirement, capability, true, linkData.source, linkData.target);
            this.connectionWizardService.selectedMatch.relationship = relationship;

            const title = `Connection Properties`;
            const saveButton: ButtonModel = new ButtonModel('Save', 'blue', () => {
                this.saveChangedCapabilityProperties().then(() => {
                    this.modalService.closeCurrentModal();
                });
            });
            const cancelButton: ButtonModel = new ButtonModel('Cancel', 'white', () => {
                this.modalService.closeCurrentModal();
            });
            const modal = new ModalModel('xl', title, '', [saveButton, cancelButton]);
            const modalInstance = this.modalService.createCustomModal(modal);
            this.modalService.addDynamicContentToModal(modalInstance, ConnectionPropertiesViewComponent);
            modalInstance.instance.open();

            new Promise((resolve) => {
                if (!this.connectionWizardService.selectedMatch.capability.properties) {
                    this.componentInstanceService.getInstanceCapabilityProperties(this.topologyTemplateType, this.topologyTemplateId, linkData.target, capability)
                        .subscribe(() => {
                            resolve();
                        }, () => { /* do nothing */ });
                } else {
                    resolve();
                }
            }).then(() => {
                this.modalService.addDynamicContentToModal(modalInstance, ConnectionPropertiesViewComponent);
            });
        }, () => { /* do nothing */ });
    }

    private openModifyLinkMenu = (linkMenuObject: LinkMenu, $event) => {

        const menuConfig: menuItem[] = [{
            text: 'View',
            iconName: 'eye-o',
            iconType: 'common',
            iconMode: 'secondary',
            iconSize: 'small',
            type: '',
            action: () => this.viewRelation(linkMenuObject.link as Cy.CollectionEdges)
        }];

        if (!this.isViewOnly()) {
            menuConfig.push({
                text: 'Delete',
                iconName: 'trash-o',
                iconType: 'common',
                iconMode: 'secondary',
                iconSize: 'small',
                type: '',
                action: () => this.deleteRelation(linkMenuObject.link as Cy.CollectionEdges)
            });
        }
        this.simplePopupMenuService.openBaseMenu(menuConfig, {
            x: $event.originalEvent.x,
            y: $event.originalEvent.y
        });
    }

}
