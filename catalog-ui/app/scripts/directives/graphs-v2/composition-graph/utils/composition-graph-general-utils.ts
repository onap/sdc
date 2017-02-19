/// <reference path="../../../../references"/>
module Sdc.Graph.Utils {

    import Dictionary = Sdc.Utils.Dictionary;

    export class CompositionGraphGeneralUtils {

        public componentRequirementsAndCapabilitiesCaching = new Dictionary<string, Models.Components.Component>();
        protected static graphUtilsUpdateQueue: Sdc.Utils.Functions.QueueUtils;

        constructor(private $q: ng.IQService,
                    private LoaderService: Services.LoaderService,
                    private commonGraphUtils: Sdc.Graph.Utils.CommonGraphUtils,
                    private matchCapabilitiesRequirementsUtils: Graph.Utils.MatchCapabilitiesRequirementsUtils) {
            CompositionGraphGeneralUtils.graphUtilsUpdateQueue = new Sdc.Utils.Functions.QueueUtils(this.$q);
        }


        /**
         * Get the offset for the link creation Menu
         * @param point
         * @returns {Cy.Position}
         */
        public calcMenuOffset: Function = (point: Cy.Position): Cy.Position => {
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
        public getLinkMenuPosition = (cy: Cy.Instance, targetNodePosition: Cy.Position) => {
            let menuPosition: Cy.Position = this.calcMenuOffset(targetNodePosition); //get the link mid point
            if (document.body.scrollHeight < menuPosition.y + Sdc.Utils.Constants.GraphUIObjects.LINK_MENU_HEIGHT + $(document.getElementsByClassName('sdc-composition-graph-wrapper')).offset().top) { // if position menu is overflow bottom
                menuPosition.y = document.body.scrollHeight - Sdc.Utils.Constants.GraphUIObjects.TOP_HEADER_HEIGHT - Sdc.Utils.Constants.GraphUIObjects.LINK_MENU_HEIGHT;
            }
            return menuPosition;
        };


        /**
         * will return true/false if two nodes overlapping
         *
         * @param graph node
         */
        private isNodesOverlapping(node: Cy.CollectionFirstNode, draggedNode: Cy.CollectionFirstNode): boolean {

            let nodeBoundingBox: Cy.BoundingBox = node.renderedBoundingBox();
            let secondNodeBoundingBox: Cy.BoundingBox = draggedNode.renderedBoundingBox();

            return this.isBBoxOverlapping(nodeBoundingBox, secondNodeBoundingBox);
        }

        /**
         * Checks whether the bounding boxes of two nodes are overlapping on any side
         * @param nodeOneBBox
         * @param nodeTwoBBox
         * @returns {boolean}
         */
        private isBBoxOverlapping(nodeOneBBox: Cy.BoundingBox, nodeTwoBBox: Cy.BoundingBox) {
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
         * @returns {Models.MatchReqToCapability}
         */
        public canBeHostedOn(cy: Cy.Instance, fromUcpeInstance: Models.ComponentsInstances.ComponentInstance, toComponentInstance: Models.ComponentsInstances.ComponentInstance): Models.MatchReqToCapability {

            let matches: Array<Models.MatchBase> = this.matchCapabilitiesRequirementsUtils.getMatchedRequirementsCapabilities(fromUcpeInstance, toComponentInstance, this.getAllCompositionCiLinks(cy));
            let hostedOnMatch: Models.MatchBase = _.find(matches, (match: Models.MatchReqToCapability) => {
                return match.requirement.capability.toLowerCase() === 'tosca.capabilities.container';
            });

            return <Models.MatchReqToCapability>hostedOnMatch;
        };


        /**
         * Checks whether node can be dropped into UCPE
         * @param cy
         * @param nodeToInsert
         * @param ucpeNode
         * @returns {boolean}
         */
        private isValidDropInsideUCPE(cy: Cy.Instance, nodeToInsert: Models.ComponentsInstances.ComponentInstance, ucpeNode:  Models.ComponentsInstances.ComponentInstance): boolean {

                let hostedOnMatch: Models.MatchReqToCapability = this.canBeHostedOn(cy, ucpeNode, nodeToInsert);
                let result: boolean = !angular.isUndefined(hostedOnMatch) || nodeToInsert.isVl(); //group validation
                return result;

        };


        /**
         * For drops from palette, checks whether the node can be dropped. If node is being held over another node, check if capable of hosting
         * @param cy
         * @param pseudoNodeBBox
         * @param paletteComponentInstance
         * @returns {boolean}
         */
        public isPaletteDropValid(cy: Cy.Instance, pseudoNodeBBox: Cy.BoundingBox, paletteComponentInstance:Sdc.Models.ComponentsInstances.ComponentInstance) {

            let componentIsUCPE:boolean = (paletteComponentInstance.capabilities && paletteComponentInstance.capabilities['tosca.capabilities.Container'] && paletteComponentInstance.name.toLowerCase().indexOf('ucpe') > -1);

            if(componentIsUCPE && cy.nodes('[?isUcpe]').length > 0)  { //second UCPE not allowed
                return false;
            }

            let illegalOverlappingNodes = _.filter(cy.nodes("[isSdcElement]"), (graphNode: Cy.CollectionFirstNode) => {

                if(this.isBBoxOverlapping(pseudoNodeBBox, graphNode.renderedBoundingBox())){
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
        public isValidDrop(cy: Cy.Instance, draggedNode: Cy.CollectionFirstNode): boolean {

            let illegalOverlappingNodes = _.filter(cy.nodes("[isSdcElement]"), (graphNode: Cy.CollectionFirstNode) => { //all sdc nodes, removing child nodes (childe node allways collaps

                if (draggedNode.data().isUcpe && (graphNode.isChild() || graphNode.data().isInsideGroup)) { //ucpe cps always inside ucpe, no overlapping
                    return false;
                }
                if(draggedNode.data().isInsideGroup && (!draggedNode.active() || graphNode.data().isUcpe)) {
                    return false;
                }

                if (!draggedNode.data().isUcpe && !(draggedNode.data() instanceof Sdc.Models.Graph.CompositionCiNodeUcpeCp) && graphNode.data().isUcpe) { //case we are dragging a node into UCPE
                    let isEntirelyInUCPE:boolean = this.commonGraphUtils.isFirstBoxContainsInSecondBox(draggedNode.renderedBoundingBox(), graphNode.renderedBoundingBox());
                    if (isEntirelyInUCPE){
                        if(this.isValidDropInsideUCPE(cy, draggedNode.data().componentInstance, graphNode.data().componentInstance)){ //if this is valid insert into ucpe, we return false - no illegal overlapping nodes
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
        public isGroupValidDrop(cy: Cy.Instance, nodesArray: Cy.CollectionNodes): boolean {
            var filterDraggedNodes = nodesArray.filter('[?isDraggable]');
            let isValidDrop = _.every(filterDraggedNodes, (node: Cy.CollectionFirstNode) => {
                return this.isValidDrop(cy, node);

            });
            return isValidDrop;
        };

        /**
         * get all links in diagram
         * @param cy
         * @returns {any[]|boolean[]}
         */
        public getAllCompositionCiLinks = (cy: Cy.Instance): Array<Models.CompositionCiLinkBase> => {
            return _.map(cy.edges("[isSdcElement]"), (edge: Cy.CollectionEdges) => {
                return edge.data();
            });
        };


        /**
         * Get Graph Utils server queue
         * @returns {Sdc.Utils.Functions.QueueUtils}
         */
        public getGraphUtilsServerUpdateQueue(): Sdc.Utils.Functions.QueueUtils {
            return CompositionGraphGeneralUtils.graphUtilsUpdateQueue;
        }
        ;

        /**
         *
         * @param blockAction - true/false if this is a block action
         * @param instances
         * @param component
         */
        public pushMultipleUpdateComponentInstancesRequestToQueue = (blockAction: boolean, instances: Array<Models.ComponentsInstances.ComponentInstance>, component: Models.Components.Component): void => {
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
        public pushUpdateComponentInstanceActionToQueue = (component: Models.Components.Component, blockAction: boolean, updatedInstance: Models.ComponentsInstances.ComponentInstance): void => {

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

    CompositionGraphGeneralUtils.$inject = ['$q', 'LoaderService',  'CommonGraphUtils', 'MatchCapabilitiesRequirementsUtils'];
}