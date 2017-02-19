/**
 * Created by obarda on 11/9/2016.
 */

/// <reference path="../../../../references"/>
module Sdc.Graph.Utils {

    export class CompositionGraphNodesUtils {
        constructor(private NodesFactory:Sdc.Utils.NodesFactory,  private $log:ng.ILogService,
                    private GeneralGraphUtils:Graph.Utils.CompositionGraphGeneralUtils,
                    private commonGraphUtils: Sdc.Graph.Utils.CommonGraphUtils,
                    private eventListenerService: Services.EventListenerService,
                    private loaderService:Services.LoaderService) {

        }

        /**
         * Returns component instances for all nodes passed in
         * @param nodes - Cy nodes
         * @returns {any[]}
         */
        public getAllNodesData(nodes:Cy.CollectionNodes) {
            return _.map(nodes, (node:Cy.CollectionFirstNode)=> {
                return node.data();
            })
        };

        /**
         * Deletes component instances on server and then removes it from the graph as well
         * @param cy
         * @param component
         * @param nodeToDelete
         */
        public deleteNode(cy: Cy.Instance, component:Models.Components.Component, nodeToDelete:Cy.CollectionNodes):void {

            this.loaderService.showLoader('composition-graph');
            let onSuccess:(response:Models.ComponentsInstances.ComponentInstance) => void = (response:Models.ComponentsInstances.ComponentInstance) => {
                console.info('onSuccess', response);

                //if node to delete is a UCPE, remove all children (except UCPE-CPs) and remove their "hostedOn" links
                if (nodeToDelete.data().isUcpe){
                    _.each(cy.nodes('[?isInsideGroup]'), (node)=>{
                       this.eventListenerService.notifyObservers(Sdc.Utils.Constants.GRAPH_EVENTS.ON_REMOVE_NODE_FROM_UCPE, node, nodeToDelete);
                    });
                }

                //check whether the node is connected to any VLs that only have one other connection. If so, delete that VL as well
                if(!(nodeToDelete.data() instanceof Sdc.Models.Graph.CompositionCiNodeVl)){
                    let connectedVls:Array<Cy.CollectionFirstNode> = this.getConnectedVlToNode(nodeToDelete);
                    this.handleConnectedVlsToDelete(connectedVls);
                }

                //update UI
                cy.remove(nodeToDelete);

            };

            let onFailed:(response:any) => void = (response:any) => {
                console.info('onFailed', response);
            };


            this.GeneralGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIActionWithReleaseCallback(
                () => component.deleteComponentInstance(nodeToDelete.data().componentInstance.uniqueId).then(onSuccess, onFailed),
                () => this.loaderService.hideLoader('composition-graph')
            );

        };


        /**
         * Finds all VLs connected to a single node
         * @param node
         * @returns {Array<Cy.CollectionFirstNode>}
         */
        public getConnectedVlToNode = (node: Cy.CollectionNodes): Array<Cy.CollectionFirstNode> => {
            let connectedVls: Array<Cy.CollectionFirstNode> = new Array<Cy.CollectionFirstNode>();
            _.forEach(node.connectedEdges().connectedNodes(), (node: Cy.CollectionFirstNode) => {
                if (node.data() instanceof Models.Graph.CompositionCiNodeVl) {
                    connectedVls.push(node);
                }
            });
            return connectedVls;
        };


        /**
         * Delete all VLs that have only two connected nodes (this function is called when deleting a node)
         * @param connectedVls
         */
        public handleConnectedVlsToDelete = (connectedVls: Array<Cy.CollectionFirstNode>) => {
            _.forEach(connectedVls, (vlToDelete: Cy.CollectionNodes) => {

                if (vlToDelete.connectedEdges().length === 2) { // if vl connected only to 2 nodes need to delete the vl
                    this.eventListenerService.notifyObservers(Sdc.Utils.Constants.GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, vlToDelete.data().componentInstance);
                }
            });
        };


        /**
         * This function is called when moving a node in or out of UCPE.
         * Deletes all connected VLs that have less than 2 valid connections remaining after the move
         * Returns the collection of vls that are in the process of deletion (async) to prevent duplicate calls while deletion is in progress
         * @param component
         * @param cy
         * @param node - node that was moved in/out of ucpe
         */
        public deleteNodeVLsUponMoveToOrFromUCPE = (component:Models.Components.Component, cy:Cy.Instance, node:Cy.CollectionNodes):Cy.CollectionNodes =>{
            if(node.data() instanceof Models.Graph.CompositionCiNodeVl){ return;}

            let connectedVLsToDelete:Cy.CollectionNodes = cy.collection();
            _.forEach(node.neighborhood('node'), (connectedNode) => {

               //Find all neighboring nodes that are VLs
               if(connectedNode.data() instanceof  Models.Graph.CompositionCiNodeVl){

                   //check VL's neighbors to see if it has 2 or more nodes whose location is compatible with VL (regardless of whether VL is in or out of UCPE)
                   let compatibleNodeCount = 0;
                   let vlNeighborhood = connectedNode.neighborhood('node');
                   _.forEach(vlNeighborhood, (vlNeighborNode)=>{
                       if(this.commonGraphUtils.nodeLocationsCompatible(cy, connectedNode, vlNeighborNode)) {
                            compatibleNodeCount ++;
                       }
                   });

                   if(compatibleNodeCount < 2) {
                       connectedVLsToDelete = connectedVLsToDelete.add(connectedNode);
                   }
               }
            });

            connectedVLsToDelete.each((i, vlToDelete:Cy.CollectionNodes)=>{
                this.deleteNode(cy, component, vlToDelete);
            });
            return connectedVLsToDelete;
        };

        /**
         * This function will update nodes position. if the new position is into or out of ucpe, the node will trigger the ucpe events
         * @param cy
         * @param component
         * @param nodesMoved - the node/multiple nodes now moved by the user
         */
        public onNodesPositionChanged = (cy: Cy.Instance, component:Models.Components.Component, nodesMoved: Cy.CollectionNodes): void => {

            if (nodesMoved.length === 0) {
                return;
            }

            let isValidMove:boolean = this.GeneralGraphUtils.isGroupValidDrop(cy, nodesMoved);
            if (isValidMove) {

                this.$log.debug(`composition-graph::ValidDrop:: updating node position`);
                let instancesToUpdateInNonBlockingAction:Array<Models.ComponentsInstances.ComponentInstance> = new Array<Models.ComponentsInstances.ComponentInstance>();

                _.each(nodesMoved, (node:Cy.CollectionFirstNode)=> {  //update all nodes new position

                    if(node.data().isUcpePart && !node.data().isUcpe){ return; }//No need to update UCPE-CPs

                    //update position
                    let newPosition:Cy.Position = this.commonGraphUtils.getNodePosition(node);
                    node.data().componentInstance.updatePosition(newPosition.x, newPosition.y);

                    //check if node moved to or from UCPE
                    let ucpe = this.commonGraphUtils.isInUcpe(node.cy(), node.boundingbox());
                    if(node.data().isInsideGroup || ucpe.length) {
                        this.handleUcpeChildMove(node, ucpe, instancesToUpdateInNonBlockingAction);
                    } else {
                        instancesToUpdateInNonBlockingAction.push(node.data().componentInstance);
                    }

                });

                if (instancesToUpdateInNonBlockingAction.length > 0) {
                    this.GeneralGraphUtils.pushMultipleUpdateComponentInstancesRequestToQueue(false, instancesToUpdateInNonBlockingAction, component);
                }
            } else {
                this.$log.debug(`composition-graph::notValidDrop:: node return to latest position`);
                //reset nodes position
                nodesMoved.positions((i, node) => {
                    return {
                        x: +node.data().componentInstance.posX,
                        y: +node.data().componentInstance.posY
                    };
                })
            }

            this.GeneralGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIActionWithReleaseCallback(() => {
            }, () => {
                this.loaderService.hideLoader('composition-graph');
            });

        };

        /**
         * Checks whether the node has been added or removed from UCPE and triggers appropriate events
         * @param node - node moved
         * @param ucpeContainer - UCPE container that the node has been moved to. When moving a node out of ucpe, param will be empty
         * @param instancesToUpdateInNonBlockingAction
         */
        public handleUcpeChildMove(node:Cy.CollectionFirstNode, ucpeContainer:Cy.CollectionElements, instancesToUpdateInNonBlockingAction:Array<Models.ComponentsInstances.ComponentInstance>){

            if(node.data().isInsideGroup){
                if(ucpeContainer.length){ //moving node within UCPE. Simply update position
                    this.commonGraphUtils.updateUcpeChildPosition(<Cy.CollectionNodes>node, ucpeContainer);
                    instancesToUpdateInNonBlockingAction.push(node.data().componentInstance);
                } else { //removing node from UCPE. Notify observers
                    this.eventListenerService.notifyObservers(Sdc.Utils.Constants.GRAPH_EVENTS.ON_REMOVE_NODE_FROM_UCPE, node, ucpeContainer);
                }
            } else if(!node.data().isInsideGroup && ucpeContainer.length && !node.data().isUcpePart){ //adding node to UCPE
                this.eventListenerService.notifyObservers(Sdc.Utils.Constants.GRAPH_EVENTS.ON_INSERT_NODE_TO_UCPE, node, ucpeContainer, true);
            }
        }

    }


    CompositionGraphNodesUtils.$inject = ['NodesFactory', '$log', 'CompositionGraphGeneralUtils', 'CommonGraphUtils', 'EventListenerService', 'LoaderService'];
}