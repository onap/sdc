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

import {Injectable} from "@angular/core";
import {CompositionGraphGeneralUtils, RequirementAndCapabilities} from "./composition-graph-general-utils";
import {CommonGraphUtils} from "../common/common-graph-utils";
import {EventListenerService} from "../../../../../services/event-listener-service";
import {ResourceNamePipe} from "app/ng2/pipes/resource-name.pipe";
import {ComponentInstanceFactory} from "app/utils/component-instance-factory";
import {GRAPH_EVENTS, GraphUIObjects} from "app/utils/constants";
import {TopologyTemplateService} from "app/ng2/services/component-services/topology-template.service";
import {DndDropEvent} from "ngx-drag-drop/ngx-drag-drop";
import {SdcUiServices} from "onap-ui-angular"
import { Component as TopologyTemplate, NodesFactory, CapabilitiesGroup, RequirementsGroup,
     CompositionCiNodeBase, ComponentInstance, LeftPaletteComponent, Point } from "app/models";
import {CompositionService} from "../../composition.service";
import {WorkspaceService} from "app/ng2/pages/workspace/workspace.service";
import { QueueServiceUtils } from "app/ng2/utils/queue-service-utils";
import {ComponentGenericResponse} from "../../../../services/responses/component-generic-response";
import {MatchCapabilitiesRequirementsUtils} from "./match-capability-requierment-utils";
import {CompositionGraphNodesUtils} from "./index";

@Injectable()
export class CompositionGraphPaletteUtils {

    constructor(private generalGraphUtils:CompositionGraphGeneralUtils,
                private nodesFactory:NodesFactory,
                private commonGraphUtils:CommonGraphUtils,
                private queueServiceUtils:QueueServiceUtils,
                private eventListenerService:EventListenerService,
                private topologyTemplateService: TopologyTemplateService,
                private loaderService: SdcUiServices.LoaderService,
                private compositionService: CompositionService,
                private workspaceService: WorkspaceService,
                private matchCapabilitiesRequirementsUtils: MatchCapabilitiesRequirementsUtils,
                private nodesGraphUtils: CompositionGraphNodesUtils) {
    }

    /**
     *
     * @param Calculate matching nodes, highlight the matching nodes and fade the non matching nodes
     * @param leftPaletteComponent
     * @param _cy
     * @returns void
     * @private
     */

    public onComponentHoverIn = (leftPaletteComponent: LeftPaletteComponent, _cy: Cy.Instance) => {
        const nodesData = this.nodesGraphUtils.getAllNodesData(_cy.nodes());
        const nodesLinks = this.generalGraphUtils.getAllCompositionCiLinks(_cy);

        if (this.generalGraphUtils.componentRequirementsAndCapabilitiesCaching.containsKey(leftPaletteComponent.uniqueId)) {
            const reqAndCap: RequirementAndCapabilities = this.generalGraphUtils.componentRequirementsAndCapabilitiesCaching.getValue(leftPaletteComponent.uniqueId);
            const filteredNodesData = this.matchCapabilitiesRequirementsUtils.findMatchingNodesToComponentInstance(
                { uniqueId: leftPaletteComponent.uniqueId, requirements: reqAndCap.requirements, capabilities: reqAndCap.capabilities} as ComponentInstance, nodesData, nodesLinks);

            this.matchCapabilitiesRequirementsUtils.highlightMatchingComponents(filteredNodesData, _cy);
            this.matchCapabilitiesRequirementsUtils.fadeNonMachingComponents(filteredNodesData, nodesData, _cy);
        } else {

            this.topologyTemplateService.getCapabilitiesAndRequirements(leftPaletteComponent.componentType, leftPaletteComponent.uniqueId).subscribe((response: ComponentGenericResponse) => {
                let reqAndCap: RequirementAndCapabilities = {
                    capabilities: response.capabilities,
                    requirements: response.requirements
                }
                this.generalGraphUtils.componentRequirementsAndCapabilitiesCaching.setValue(leftPaletteComponent.uniqueId, reqAndCap);
            });
        }
    }

    /**
     * Calculate the dragged element (html element) position on canvas
     * @param cy
     * @param event
     * @param position
     * @returns {Cy.BoundingBox}
     * @private
     */
    private _getNodeBBox(cy:Cy.Instance, event:DragEvent, position?:Cy.Position, eventPosition?: Point) {
        let bbox = <Cy.BoundingBox>{};
        if (!position) {
            position = event ? this.commonGraphUtils.getCytoscapeNodePosition(cy, event) : eventPosition;
        }
        let cushionWidth:number = 40;
        let cushionHeight:number = 40;

        bbox.x1 = position.x - cushionWidth / 2;
        bbox.y1 = position.y - cushionHeight / 2;
        bbox.x2 = position.x + cushionWidth / 2;
        bbox.y2 = position.y + cushionHeight / 2;
        return bbox;
    }

    /**
     * Create the component instance, update data from parent component in the left palette and notify on_insert_to_ucpe if component was dragg into ucpe
     * @param cy
     * @param fullComponent
     * @param event
     * @param component
     */
    private _createComponentInstanceOnGraphFromPaletteComponent(cy:Cy.Instance, fullComponent:LeftPaletteComponent, event:DragEvent) {

        let componentInstanceToCreate:ComponentInstance = ComponentInstanceFactory.createComponentInstanceFromComponent(fullComponent); 
        let cytoscapePosition:Cy.Position = this.commonGraphUtils.getCytoscapeNodePosition(cy, event);
        componentInstanceToCreate.posX = cytoscapePosition.x;
        componentInstanceToCreate.posY = cytoscapePosition.y;

        let onFailedCreatingInstance:(error:any) => void = (error:any) => {
            this.loaderService.deactivate();
        };

        //on success - update node data
        let onSuccessCreatingInstance = (createInstance:ComponentInstance):void => {

            this.loaderService.deactivate();
            this.compositionService.addComponentInstance(createInstance);
            createInstance.name = ResourceNamePipe.getDisplayName(createInstance.name);
            createInstance.requirements = new RequirementsGroup(createInstance.requirements);
            createInstance.capabilities = new CapabilitiesGroup(createInstance.capabilities);
            createInstance.componentVersion = fullComponent.version;
            createInstance.icon = fullComponent.icon;
            createInstance.setInstanceRC();

            let newNode:CompositionCiNodeBase = this.nodesFactory.createNode(createInstance);
            this.commonGraphUtils.addComponentInstanceNodeToGraph(cy, newNode);
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE);
        };

        this.queueServiceUtils.addBlockingUIAction(() => {
            let uniqueId = this.workspaceService.metadata.uniqueId;
            let componentType = this.workspaceService.metadata.componentType;
            this.topologyTemplateService.createComponentInstance(componentType, uniqueId, componentInstanceToCreate).subscribe(onSuccessCreatingInstance, onFailedCreatingInstance);

        });
    }
    //
    // /**
    //  * Thid function applay red/green background when component dragged from palette
    //  * @param cy
    //  * @param event
    //  * @param dragElement
    //  * @param dragComponent
    //  */
    // public onComponentDrag(cy:Cy.Instance, event) {
    //     let draggedElement = document.getElementById("draggable_element");
    //     // event.dataTransfer.setDragImage(draggableElement, 0, 0);
    //     if (event.clientX < GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET || event.clientY < GraphUIObjects.DIAGRAM_HEADER_OFFSET) { //hovering over palette. Dont bother computing validity of drop
    //         draggedElement.className = 'invalid-drag';
    //         event.dataTransfer.setDragImage(draggedElement.cloneNode(true), 0, 0);
    //         return;
    //     }
    //
    //     let offsetPosition = {
    //         x: event.clientX - GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET,
    //         y: event.clientY - GraphUIObjects.DIAGRAM_HEADER_OFFSET
    //     };
    //     let bbox = this._getNodeBBox(cy, event, offsetPosition);
    //
    //     if (this.generalGraphUtils.isPaletteDropValid(cy, bbox)) {
    //         draggedElement.className = 'valid-drag';
    //         event.dataTransfer.setDragImage(draggedElement.cloneNode(true), 0, 0);
    //         // event.dataTransfer.setDragImage(draggedElement, 0, 0);
    //         // event.dataTransfer.setDragImage(draggedElement, 0, 0);
    //
    //     } else {
    //         draggedElement.className = 'invalid-drag';
    //         event.dataTransfer.setDragImage(draggedElement.cloneNode(true), 0, 0);
    //     }
    // }

    public isDragValid(cy:Cy.Instance, position: Point):boolean {
        if (position.x < GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET || position.y < GraphUIObjects.DIAGRAM_HEADER_OFFSET) { //hovering over palette. Dont bother computing validity of drop
            return false;
        }
        
        let offsetPosition = {
            x: position.x - GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET,
            y: position.y - GraphUIObjects.DIAGRAM_HEADER_OFFSET
        };
        let bbox = this._getNodeBBox(cy, null, offsetPosition, position);

        if (this.generalGraphUtils.isPaletteDropValid(cy, bbox)) {
            return true;
        } else {
            return false;
        }
    } 
    /**
     *  This function is called when after dropping node on canvas
     *  Check if the capability & requirements fulfilled and if not get from server
     * @param cy
     * @param dragEvent
     * @param component
     */
    public addNodeFromPalette(cy:Cy.Instance, dragEvent:DndDropEvent) {
        this.loaderService.activate();

        let draggedComponent:LeftPaletteComponent = dragEvent.data;

        if (this.generalGraphUtils.componentRequirementsAndCapabilitiesCaching.containsKey(draggedComponent.uniqueId)) {
            let fullComponent = this.generalGraphUtils.componentRequirementsAndCapabilitiesCaching.getValue(draggedComponent.uniqueId);
            draggedComponent.capabilities = fullComponent.capabilities;
            draggedComponent.requirements = fullComponent.requirements;
            this._createComponentInstanceOnGraphFromPaletteComponent(cy, draggedComponent, dragEvent.event);

        } else {

            this.topologyTemplateService.getFullComponent(draggedComponent.componentType, draggedComponent.uniqueId).subscribe((topologyTemplate:TopologyTemplate) => {
                draggedComponent.capabilities = topologyTemplate.capabilities;
                draggedComponent.requirements = topologyTemplate.requirements;
                this._createComponentInstanceOnGraphFromPaletteComponent(cy, draggedComponent, dragEvent.event);
            });
        }
    }
}

