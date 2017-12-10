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

import {CommonNodeBase, CompositionCiLinkBase, RelationshipModel, Relationship, CompositionCiNodeBase, NodesFactory, LinksFactory} from "app/models";
import {GraphUIObjects} from "app/utils";
/**
 * Created by obarda on 12/21/2016.
 */
export class CommonGraphUtils {

    constructor(private NodesFactory:NodesFactory, private LinksFactory:LinksFactory) {

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

            if(!node.data().isUcpe) { //ucpe should not have tooltip
                this.initNodeTooltip(node);
            }
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
        if (compositionGraphNode.isUcpe) {
            this.createUcpeCpNodes(cy, node);
        }
        return node;
    };

    /**
     * This function will create CP_WAN & CP_LAN  for the UCPE. this is a special node on the group that will behave like ports on the ucpe
     * @param cy
     * @param ucpeGraphNode
     */
    private createUcpeCpNodes(cy:Cy.Instance, ucpeGraphNode:Cy.CollectionNodes):void {

        let requirementsArray:Array<any> = ucpeGraphNode.data().componentInstance.requirements["tosca.capabilities.Node"];
        //show only LAN or WAN requirements
        requirementsArray = _.reject(requirementsArray, (requirement:any) => {
            let name:string = requirement.ownerName.toLowerCase();
            return name.indexOf('lan') === -1 && name.indexOf('wan') === -1;
        });
        requirementsArray.sort(function (a, b) {
            let nameA = a.ownerName.toLowerCase().match(/[^ ]+/)[0];
            let nameB = b.ownerName.toLowerCase().match(/[^ ]+/)[0];
            let numA = _.last(a.ownerName.toLowerCase().split(' '));
            let numB = _.last(b.ownerName.toLowerCase().split(' '));

            if (nameA === nameB) return numA > numB ? 1 : -1;
            return nameA < nameB ? 1 : -1;
        });
        let position = angular.copy(ucpeGraphNode.boundingbox());
        //add CP nodes to group
        let topCps:number = 0;
        for (let i = 0; i < requirementsArray.length; i++) {

            let cpNode = this.NodesFactory.createUcpeCpNode(angular.copy(ucpeGraphNode.data().componentInstance));
            cpNode.componentInstance.capabilities = requirementsArray[i];
            cpNode.id = requirementsArray[i].ownerId;
            cpNode.group = ucpeGraphNode.data().componentInstance.uniqueId;
            cpNode.name = requirementsArray[i].ownerName; //for tooltip
            cpNode.displayName = requirementsArray[i].ownerName;
            cpNode.displayName = cpNode.displayName.length > 5 ? cpNode.displayName.substring(0, 5) + '...' : cpNode.displayName;


            if (cpNode.name.toLowerCase().indexOf('lan') > -1) {
                cpNode.textPosition = "top";
                cpNode.componentInstance.posX = position.x1 + (i * 90) - (topCps * 90) + 53;
                cpNode.componentInstance.posY = position.y1 + 400 + 27;
            } else {
                cpNode.textPosition = "bottom";
                cpNode.componentInstance.posX = position.x1 + (topCps * 90) + 53;
                cpNode.componentInstance.posY = position.y1 + 27;
                topCps++;
            }
            let cyCpNode = this.addComponentInstanceNodeToGraph(cy, cpNode);
            cyCpNode.lock();
        }
    };

    /**
     *
     * @param nodes - all nodes in graph in order to find the edge connecting the two nodes
     * @param fromNodeId
     * @param toNodeId
     * @returns {boolean} true/false if the edge is certified (from node and to node are certified)
     */
    public isRelationCertified(nodes:Cy.CollectionNodes, fromNodeId:string, toNodeId:string):boolean {
        let resourceTemp = _.filter(nodes, function (node:Cy.CollectionFirst) {
            return node.data().id === fromNodeId || node.data().id === toNodeId;
        });
        let certified:boolean = true;

        _.forEach(resourceTemp, (item) => {
            certified = certified && item.data().certified;
        });

        return certified;
    }

    /**
     * Add link to graph - only draw the link
     * @param cy
     * @param link
     */
    public insertLinkToGraph = (cy:Cy.Instance, link:CompositionCiLinkBase) => {

        if (!this.isRelationCertified(cy.nodes(), link.source, link.target)) {
            link.classes = 'not-certified-link';
        }
        let linkElement = cy.add({
            group: 'edges',
            data: link,
            classes: link.classes
        });
        this.initLinkTooltip(linkElement, link);
    };

    /**
     * This function will init qtip tooltip on the link
     * @params linkElement - the link we want the tooltip to apply on,
     * link - the link obj
     */
    public initLinkTooltip(linkElement:Cy.CollectionElements, link:CompositionCiLinkBase) {

        let opts = {
            content: function () {
                return '<div class="line">' +
                            '<span class="req-cap-label">R: </span>' +
                            '<span>'+ link.relation.relationships[0].relation.requirement + '</span>' +
                        '</div>' +
                        '<div class="line">' +
                            '<div class="sprite-new link-tooltip-arrow"></div>' +
                            '<span class="req-cap-label">C: </span>' +
                            '<span>' + link.relation.relationships[0].relation.capability + '</span>' +
                        '</div>';
            },
            position: {
                my: 'top center',
                at: 'bottom center',
                adjust: {x:0, y:0}
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
            includeLabels: true
        };

        linkElement.qtip(opts);
    };

    /**
     *  go over the relations and draw links on the graph
     * @param cy
     * @param instancesRelations
     */
    public initGraphLinks(cy:Cy.Instance, instancesRelations:Array<RelationshipModel>) {

        if (instancesRelations) {
            _.forEach(instancesRelations, (relationshipModel:RelationshipModel) => {
                _.forEach(relationshipModel.relationships, (relationship:Relationship) => {
                    let linkToCreate = this.LinksFactory.createGraphLink(cy, relationshipModel, relationship);
                    this.insertLinkToGraph(cy, linkToCreate);
                });
            });
        }
    }

    /**
     *  Determine which nodes are in the UCPE and set child data for them.
     * @param cy
     */
    public initUcpeChildren(cy:Cy.Instance) {
        let ucpe:Cy.CollectionNodes = cy.nodes('[?isUcpe]'); // Get ucpe on graph if exist
        _.each(cy.edges('.ucpe-host-link'), (link)=> {

            let ucpeChild:Cy.CollectionNodes = (link.source().id() == ucpe.id()) ? link.target() : link.source();
            this.initUcpeChildData(ucpeChild, ucpe);

            //vls dont have ucpe-host-link connection, so need to find them and iterate separately
            let connectedVLs = ucpeChild.connectedEdges().connectedNodes('.vl-node');
            _.forEach(connectedVLs, (vl)=> { //all connected vls must be UCPE children because not allowed to connect to a VL outside of the UCPE
                this.initUcpeChildData(vl, ucpe);
            });
        });
    }

    /**
     *  Set properties for nodes contained by the UCPE
     * @param childNode- node contained in UCPE
     * @param ucpe- ucpe container node
     */
    public initUcpeChildData(childNode:Cy.CollectionNodes, ucpe:Cy.CollectionNodes) {

        if (!childNode.data('isInsideGroup')) {
            this.updateUcpeChildPosition(childNode, ucpe);
            childNode.data({isInsideGroup: true});
        }

    }

    /**
     *  Updates UCPE child node offset, which allows child nodes to be dragged in synchronization with ucpe
     * @param childNode- node contained in UCPE
     * @param ucpe- ucpe container node
     */
    public updateUcpeChildPosition(childNode:Cy.CollectionNodes, ucpe:Cy.CollectionNodes) {
        let childPos:Cy.Position = childNode.relativePosition();
        let ucpePos:Cy.Position = ucpe.relativePosition();
        let offset:Cy.Position = {
            x: childPos.x - ucpePos.x,
            y: childPos.y - ucpePos.y
        };
        childNode.data("ucpeOffset", offset);
    }

    /**
     *  Removes ucpe-child properties from the node
     * @param childNode- node being removed from UCPE
     */
    public removeUcpeChildData(childNode:Cy.CollectionNodes) {
        childNode.removeData("ucpeOffset");
        childNode.data({isInsideGroup: false});

    }


    public HTMLCoordsToCytoscapeCoords(cytoscapeBoundingBox:Cy.Extent, mousePos:Cy.Position):Cy.Position {
        return {x: mousePos.x + cytoscapeBoundingBox.x1, y: mousePos.y + cytoscapeBoundingBox.y1}
    };


    public getCytoscapeNodePosition = (cy:Cy.Instance, event:IDragDropEvent):Cy.Position => {
        let targetOffset = $(event.target).offset();
        let x = event.pageX - targetOffset.left;
        let y = event.pageY - targetOffset.top;

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
     * Check if node node bounds position is inside any ucpe on graph, and return the ucpe
     * @param {diagram} the diagram.
     * @param {nodeActualBounds} the actual bound position of the node.
     * @return the ucpe if found else return null
     */
    public isInUcpe = (cy:Cy.Instance, nodeBounds:Cy.BoundingBox):Cy.CollectionElements => {

        let ucpeNodes = cy.nodes('[?isUcpe]').filterFn((ucpeNode) => {
            return this.isFirstBoxContainsInSecondBox(nodeBounds, ucpeNode.boundingbox());
        });
        return ucpeNodes;
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
            if (this.nodeLocationsCompatible(cy, node, tempNode)) {
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
    public nodeLocationsCompatible(cy:Cy.Instance, node1:Cy.CollectionFirstNode, node2:Cy.CollectionFirstNode) {

            let ucpe = cy.nodes('[?isUcpe]');
            if(!ucpe.length){ return true; }
            if(node1.data().isUcpePart || node2.data().isUcpePart) { return true; }

        return (this.isFirstBoxContainsInSecondBox(node1.boundingbox(), ucpe.boundingbox()) == this.isFirstBoxContainsInSecondBox(node2.boundingbox(), ucpe.boundingbox()));

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
                    adjust: {x:0, y:-5}
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
    
            if (node.data().isUcpePart){ //fix tooltip positioning for UCPE-cps
                opts.position.adjust = {x:0, y:20};
            }
    
            node.qtip(opts);
        };
}

CommonGraphUtils.$inject = ['NodesFactory', 'LinksFactory'];
