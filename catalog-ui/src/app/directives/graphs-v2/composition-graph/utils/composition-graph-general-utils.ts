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

import {ComponentInstance, Component, Match, CompositionCiLinkBase, CompositionCiNodeUcpeCp} from "app/models";
import {QueueUtils, Dictionary, GraphUIObjects} from "app/utils";
import {LoaderService} from "app/services";
import {MatchCapabilitiesRequirementsUtils} from "./match-capability-requierment-utils";
import {CommonGraphUtils} from "../../common/common-graph-utils";


export class CompositionGraphGeneralUtils {

    public componentRequirementsAndCapabilitiesCaching = new Dictionary<string, Component>();
    protected static graphUtilsUpdateQueue:QueueUtils;

    constructor(private $q:ng.IQService,
                private LoaderService:LoaderService,
                private commonGraphUtils:CommonGraphUtils,
                private matchCapabilitiesRequirementsUtils:MatchCapabilitiesRequirementsUtils) {
        CompositionGraphGeneralUtils.graphUtilsUpdateQueue = new QueueUtils(this.$q);
    }


    /**
     * Get the offset for the link creation Menu
     * @param point
     * @returns {Cy.Position}
     */
    public calcMenuOffset:Function = (point:Cy.Position):Cy.Position => {
        point.x = point.x + 60;
        point.y = point.y + 105;
        return point;
    };

    /**
     *  return the top left position of the link menu
     * @param cy
     * @param targetNodePosition
     * @returns {Cy.Position}
     */
    public getLinkMenuPosition = (cy:Cy.Instance, targetNodePosition:Cy.Position) => {
        let menuPosition:Cy.Position = this.calcMenuOffset(targetNodePosition); //get the link mid point
        if ($(document.body).height() < menuPosition.y + GraphUIObjects.LINK_MENU_HEIGHT + $(document.getElementsByClassName('sdc-composition-graph-wrapper')).offset().top) { // if position menu is overflow bottom
            menuPosition.y = $(document.body).height() - GraphUIObjects.TOP_HEADER_HEIGHT - GraphUIObjects.LINK_MENU_HEIGHT;
        }
        return menuPosition;
    };


    public zoomGraphTo = (cy:Cy.Instance, zoomLevel: number):void => {
        let zy = cy.height() / 2;
        let zx = cy.width() / 2;
        cy.zoom({
            level: zoomLevel,
            renderedPosition: { x: zx, y: zy }
        });
    }
    /**
     * will return true/false if two nodes overlapping
     *
     * @param graph node
     */
    private isNodesOverlapping(node:Cy.CollectionFirstNode, draggedNode:Cy.CollectionFirstNode):boolean {

        let nodeBoundingBox:Cy.BoundingBox = node.renderedBoundingBox();
        let secondNodeBoundingBox:Cy.BoundingBox = draggedNode.renderedBoundingBox();

        return this.isBBoxOverlapping(nodeBoundingBox, secondNodeBoundingBox);
    }

    /**
     * Checks whether the bounding boxes of two nodes are overlapping on any side
     * @param nodeOneBBox
     * @param nodeTwoBBox
     * @returns {boolean}
     */
    private isBBoxOverlapping(nodeOneBBox:Cy.BoundingBox, nodeTwoBBox:Cy.BoundingBox) {
        return (((nodeOneBBox.x1 < nodeTwoBBox.x1 && nodeOneBBox.x2 > nodeTwoBBox.x1) ||
        (nodeOneBBox.x1 < nodeTwoBBox.x2 && nodeOneBBox.x2 > nodeTwoBBox.x2) ||
        (nodeTwoBBox.x1 < nodeOneBBox.x1 && nodeTwoBBox.x2 > nodeOneBBox.x2)) &&
        ((nodeOneBBox.y1 < nodeTwoBBox.y1 && nodeOneBBox.y2 > nodeTwoBBox.y1) ||
        (nodeOneBBox.y1 < nodeTwoBBox.y2 && nodeOneBBox.y2 > nodeTwoBBox.y2) ||
        (nodeTwoBBox.y1 < nodeOneBBox.y1 && nodeTwoBBox.y2 > nodeOneBBox.y2)))
    }


    /**
     * Checks whether a specific component instance can be hosted on the UCPE instance
     * @param cy - Cytoscape instance
     * @param fromUcpeInstance
     * @param toComponentInstance
     * @returns {Match}
     */
    public canBeHostedOn(cy:Cy.Instance, fromUcpeInstance:ComponentInstance, toComponentInstance:ComponentInstance):Match {

        let matches:Array<Match> = this.matchCapabilitiesRequirementsUtils.getMatchedRequirementsCapabilities(fromUcpeInstance, toComponentInstance, this.getAllCompositionCiLinks(cy));
        let hostedOnMatch:Match = _.find(matches, (match:Match) => {
            return match.requirement.capability.toLowerCase() === 'tosca.capabilities.container';
        });

        return hostedOnMatch;
    };


    /**
     * Checks whether node can be dropped into UCPE
     * @param cy
     * @param nodeToInsert
     * @param ucpeNode
     * @returns {boolean}
     */
    private isValidDropInsideUCPE(cy:Cy.Instance, nodeToInsert:ComponentInstance, ucpeNode:ComponentInstance):boolean {

        let hostedOnMatch:Match = this.canBeHostedOn(cy, ucpeNode, nodeToInsert);
        let result:boolean = !angular.isUndefined(hostedOnMatch) || nodeToInsert.isVl(); //group validation
        return result;

    };


    /**
     * For drops from palette, checks whether the node can be dropped. If node is being held over another node, check if capable of hosting
     * @param cy
     * @param pseudoNodeBBox
     * @param paletteComponentInstance
     * @returns {boolean}
     */
    public isPaletteDropValid(cy:Cy.Instance, pseudoNodeBBox:Cy.BoundingBox, paletteComponentInstance:ComponentInstance) {

        let componentIsUCPE:boolean = (paletteComponentInstance.capabilities && paletteComponentInstance.capabilities['tosca.capabilities.Container'] && paletteComponentInstance.name.toLowerCase().indexOf('ucpe') > -1);

        if (componentIsUCPE && cy.nodes('[?isUcpe]').length > 0) { //second UCPE not allowed
            return false;
        }

        let illegalOverlappingNodes = _.filter(cy.nodes("[isSdcElement]"), (graphNode:Cy.CollectionFirstNode) => {

            if (this.isBBoxOverlapping(pseudoNodeBBox, graphNode.renderedBoundingBox())) {
                if (!componentIsUCPE && graphNode.data().isUcpe) {
                    return !this.isValidDropInsideUCPE(cy, paletteComponentInstance, graphNode.data().componentInstance); //if this is valid insert into ucpe, we return false - no illegal overlapping nodes
                }
                return true;
            }

            return false;
        });

        return illegalOverlappingNodes.length === 0;
    }

    /**
     * will return true/false if a drop of a single node is valid
     *
     * @param graph node
     */
    public isValidDrop(cy:Cy.Instance, draggedNode:Cy.CollectionFirstNode):boolean {

        let illegalOverlappingNodes = _.filter(cy.nodes("[isSdcElement]"), (graphNode:Cy.CollectionFirstNode) => { //all sdc nodes, removing child nodes (childe node allways collaps

            if (draggedNode.data().isUcpe && (graphNode.isChild() || graphNode.data().isInsideGroup)) { //ucpe cps always inside ucpe, no overlapping
                return false;
            }
            if (draggedNode.data().isInsideGroup && (!draggedNode.active() || graphNode.data().isUcpe)) {
                return false;
            }

            if (!draggedNode.data().isUcpe && !(draggedNode.data() instanceof CompositionCiNodeUcpeCp) && graphNode.data().isUcpe) { //case we are dragging a node into UCPE
                let isEntirelyInUCPE:boolean = this.commonGraphUtils.isFirstBoxContainsInSecondBox(draggedNode.renderedBoundingBox(), graphNode.renderedBoundingBox());
                if (isEntirelyInUCPE) {
                    if (this.isValidDropInsideUCPE(cy, draggedNode.data().componentInstance, graphNode.data().componentInstance)) { //if this is valid insert into ucpe, we return false - no illegal overlapping nodes
                        return false;
                    }
                }
            }
            return graphNode.data().id !== draggedNode.data().id && this.isNodesOverlapping(draggedNode, graphNode);

        });
        // return false;
        return illegalOverlappingNodes.length === 0;
    };

    /**
     * will return true/false if the move of the nodes is valid (no node overlapping and verifying if insert into UCPE is valid)
     *
     * @param  nodesArray - the selected drags nodes
     */
    public isGroupValidDrop(cy:Cy.Instance, nodesArray:Cy.CollectionNodes):boolean {
        let filterDraggedNodes = nodesArray.filter('[?isDraggable]');
        let isValidDrop = _.every(filterDraggedNodes, (node:Cy.CollectionFirstNode) => {
            return this.isValidDrop(cy, node);

        });
        return isValidDrop;
    };

    /**
     * get all links in diagram
     * @param cy
     * @returns {any[]|boolean[]}
     */
    public getAllCompositionCiLinks = (cy:Cy.Instance):Array<CompositionCiLinkBase> => {
        return _.map(cy.edges("[isSdcElement]"), (edge:Cy.CollectionEdges) => {
            return edge.data();
        });
    };


    /**
     * Get Graph Utils server queue
     * @returns {QueueUtils}
     */
    public getGraphUtilsServerUpdateQueue():QueueUtils {
        return CompositionGraphGeneralUtils.graphUtilsUpdateQueue;
    }
    ;

    /**
     *
     * @param blockAction - true/false if this is a block action
     * @param instances
     * @param component
     */
    public pushMultipleUpdateComponentInstancesRequestToQueue = (blockAction:boolean, instances:Array<ComponentInstance>, component:Component):void => {
        if (blockAction) {
            this.getGraphUtilsServerUpdateQueue().addBlockingUIAction(
                () => component.updateMultipleComponentInstances(instances)
            );
        } else {
            this.getGraphUtilsServerUpdateQueue().addNonBlockingUIAction(
                () => component.updateMultipleComponentInstances(instances),
                () => this.LoaderService.hideLoader('composition-graph'));
        }
    };

    /**
     * this function will update component instance data
     * @param blockAction - true/false if this is a block action
     * @param updatedInstance
     */
    public pushUpdateComponentInstanceActionToQueue = (component:Component, blockAction:boolean, updatedInstance:ComponentInstance):void => {

        if (blockAction) {
            this.LoaderService.showLoader('composition-graph');
            this.getGraphUtilsServerUpdateQueue().addBlockingUIAction(
                () => component.updateComponentInstance(updatedInstance)
            );
        } else {
            this.getGraphUtilsServerUpdateQueue().addNonBlockingUIAction(
                () => component.updateComponentInstance(updatedInstance),
                () => this.LoaderService.hideLoader('composition-graph'));
        }
    };
}

CompositionGraphGeneralUtils.$inject = ['$q', 'LoaderService', 'CommonGraphUtils', 'MatchCapabilitiesRequirementsUtils'];
