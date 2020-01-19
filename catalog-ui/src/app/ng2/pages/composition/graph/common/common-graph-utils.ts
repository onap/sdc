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
import {
    CommonNodeBase,
    Relationship,
    CompositionCiNodeBase
} from "app/models";
import {CompositionCiServicePathLink} from "app/models/graph/graph-links/composition-graph-links/composition-ci-service-path-link";
import {Requirement, Capability} from "app/models";
import {Injectable} from "@angular/core";



@Injectable()
export class CommonGraphUtils {

    constructor() {

    }

    public safeApply = (scope:ng.IScope, fn:any) => { //todo remove to general utils
        let phase = scope.$root.$$phase;
        if (phase == '$apply' || phase == '$digest') {
            if (fn && (typeof(fn) === 'function')) {
                fn();
            }
        } else {
            scope.$apply(fn);
        }
    };

    /**
     * Draw node on the graph
     * @param cy
     * @param compositionGraphNode
     * @param position
     * @returns {CollectionElements}
     */
    public addNodeToGraph(cy:Cy.Instance, compositionGraphNode:CommonNodeBase, position?:Cy.Position):Cy.CollectionElements {

        let node = cy.add(<Cy.ElementDefinition> {
            group: 'nodes',
            position: position,
            data: compositionGraphNode,
            classes: compositionGraphNode.classes
        });

        this.initNodeTooltip(node);
        return node;
    };

    /**
     * The function will create a component instance node by the componentInstance position.
     * If the node is UCPE the function will create all cp lan&wan for the ucpe
     * @param cy
     * @param compositionGraphNode
     * @returns {Cy.CollectionElements}
     */
    public addComponentInstanceNodeToGraph(cy:Cy.Instance, compositionGraphNode:CompositionCiNodeBase):Cy.CollectionElements {

        let nodePosition = {
            x: +compositionGraphNode.componentInstance.posX,
            y: +compositionGraphNode.componentInstance.posY
        };

        let node = this.addNodeToGraph(cy, compositionGraphNode, nodePosition);
        return node;
    };

    /**
     * Add service path link to graph - only draw the link
     * @param cy
     * @param link
     */
    public insertServicePathLinkToGraph = (cy:Cy.Instance, link:CompositionCiServicePathLink) => {
        let linkElement = cy.add({
            group: 'edges',
            data: link,
            classes: link.classes
        });
        this.initServicePathTooltip(linkElement, link);
    };

    /**
     * Returns function for the link tooltip content
     * @param {Relationship} linkRelation
     * @param {Requirement} requirement
     * @param {Capability} capability
     * @returns {() => string}
     * @private
     */
    private _getLinkTooltipContent(linkRelation:Relationship, requirement?:Requirement, capability?:Capability):string {
        return '<div class="line">' +
            '<span class="req-cap-label">R: </span>' +
            '<span>' + (requirement ? requirement.getTitle() : linkRelation.relation.requirement) + '</span>' +
            '</div>' +
            '<div class="line">' +
            '<div class="sprite-new link-tooltip-arrow"></div>' +
            '<span class="req-cap-label">C: </span>' +
            '<span>' + (capability ? capability.getTitle() : linkRelation.relation.capability) + '</span>' +
            '</div>';
    }

    /**
     * This function will init qtip tooltip on the link
     * @param linkElement - the link we want the tooltip to apply on,
     * @param link
     * @param getLinkRequirementCapability
     * link - the link obj
     */
    public initLinkTooltip(linkElement:Cy.CollectionElements, link:Relationship, getLinkRequirementCapability:Function) {
        const content = () => this._getLinkTooltipContent(link);  // base tooltip content without owner names
        const render = (event, api) => {
            // on render (called once at first show), get the link requirement and capability and change to full tooltip content (with owner names)
            getLinkRequirementCapability().then((linkReqCap) => {
                const fullContent = () => this._getLinkTooltipContent(link, linkReqCap.requirement, linkReqCap.capability);
                api.set('content.text', fullContent);
            });
        };
        linkElement.qtip(this.prepareInitTooltipData({content, events: {render}}));
    };

    /**
     *
     * @param linkElement
     * @param link
     */
    public initServicePathTooltip(linkElement:Cy.CollectionElements, link:CompositionCiServicePathLink) {
        let content = function () {
            return '<div class="line">' +
                '<div>' + link.pathName + '</div>' +
                '</div>';
        };
        linkElement.qtip(this.prepareInitTooltipData({content}));
    };

    private prepareInitTooltipData(options?:Object) {
        return _.merge({
            position: {
                my: 'top center',
                at: 'bottom center',
                adjust: {x: 0, y: 0},
                effect: false
            },
            style: {
                classes: 'qtip-dark qtip-rounded qtip-custom link-qtip',
                tip: {
                    width: 16,
                    height: 8
                }
            },
            show: {
                event: 'mouseover',
                delay: 1000
            },
            hide: {event: 'mouseout mousedown'},
            includeLabels: true,
            events: {}
        }, options);
    }

    public HTMLCoordsToCytoscapeCoords(cytoscapeBoundingBox:Cy.Extent, mousePos:Cy.Position):Cy.Position {
        return {x: mousePos.x + cytoscapeBoundingBox.x1, y: mousePos.y + cytoscapeBoundingBox.y1}
    };


    public getCytoscapeNodePosition = (cy:Cy.Instance, event:DragEvent | MouseEvent):Cy.Position => {
        let targetOffset = $(event.target).offset();
        if(event instanceof DragEvent) {
            targetOffset = $('canvas').offset();
        }
        
        let x = (event.pageX - targetOffset.left) / cy.zoom();
        let y = (event.pageY - targetOffset.top) / cy.zoom();

        return this.HTMLCoordsToCytoscapeCoords(cy.extent(), {
            x: x,
            y: y
        });
    };


    public getNodePosition(node:Cy.CollectionFirstNode):Cy.Position {
        let nodePosition = node.relativePoint();
        if (node.data().isUcpe) { //UCPEs use bounding box and not relative point.
            nodePosition = {x: node.boundingbox().x1, y: node.boundingbox().y1};
        }

        return nodePosition;
    }

    /**
     * Generic function that can be used for any html elements overlaid on canvas
     * Returns the html position of a node on canvas, including left palette and header offsets. Option to pass in additional offset to add to return position.
     * @param node
     * @param additionalOffset
     * @returns {Cy.Position}

     public getNodePositionWithOffset = (node:Cy.CollectionFirstNode, additionalOffset?:Cy.Position): Cy.Position => {
            if(!additionalOffset) additionalOffset = {x: 0, y:0};

            let nodePosition = node.renderedPosition();
            let posWithOffset:Cy.Position = {
                x: nodePosition.x + GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET + additionalOffset.x,
                y: nodePosition.y + GraphUIObjects.COMPOSITION_HEADER_OFFSET + additionalOffset.y
            };
            return posWithOffset;
        };*/

    /**
     *  return true/false if first node contains in second - this used in order to verify is node is entirely inside ucpe
     * @param firstBox
     * @param secondBox
     * @returns {boolean}
     */
    public isFirstBoxContainsInSecondBox(firstBox:Cy.BoundingBox, secondBox:Cy.BoundingBox) {

        return firstBox.x1 > secondBox.x1 && firstBox.x2 < secondBox.x2 && firstBox.y1 > secondBox.y1 && firstBox.y2 < secondBox.y2;

    };

    /**
     *
     * @param cy
     * @param node
     * @returns {Array}
     */
    public getLinkableNodes(cy:Cy.Instance, node:Cy.CollectionFirstNode):Array<CompositionCiNodeBase> {
        let compatibleNodes = [];
        _.each(cy.nodes(), (tempNode)=> {
            if (this.nodeLocationsCompatible(node, tempNode)) {
                compatibleNodes.push(tempNode.data());
            }
        });
        return compatibleNodes;
    }

    /**
     * Checks whether node locations are compatible in reference to UCPEs.
     * Returns true if both nodes are in UCPE or both nodes out, or one node is UCPEpart.
     * @param node1
     * @param node2
     */
    public nodeLocationsCompatible(node1:Cy.CollectionFirstNode, node2:Cy.CollectionFirstNode) {
        return (this.isFirstBoxContainsInSecondBox(node1.boundingbox(), node2.boundingbox()));
    }

    /**
     * This function will init qtip tooltip on the node
     * @param node - the node we want the tooltip to apply on
     */
    public initNodeTooltip(node:Cy.CollectionNodes) {

        let opts = {
            content: function () {
                return this.data('name');
            },
            position: {
                my: 'top center',
                at: 'bottom center',
                adjust: {x: 0, y: -5}
            },
            style: {
                classes: 'qtip-dark qtip-rounded qtip-custom',
                tip: {
                    width: 16,
                    height: 8
                }
            },
            show: {
                event: 'mouseover',
                delay: 1000
            },
            hide: {event: 'mouseout mousedown'},
            includeLabels: true
        };

        if (node.data().isUcpePart) { //fix tooltip positioning for UCPE-cps
            opts.position.adjust = {x: 0, y: 20};
        }

        node.qtip(opts);
    };
}

