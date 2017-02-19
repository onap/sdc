/**
 * Created by obarda on 6/28/2016.
 */
/// <reference path="../../../../references"/>
module Sdc.Graph.Utils {

    import ImageCreatorService = Sdc.Utils.ImageCreatorService;
    import Module = Sdc.Models.Module;
    export class CompositionGraphLinkUtils {

        private p2pVL:Models.Components.Component;
        private mp2mpVL:Models.Components.Component;

        constructor(private linksFactory:Sdc.Utils.LinksFactory,
                    private loaderService:Services.LoaderService,
                    private generalGraphUtils:Sdc.Graph.Utils.CompositionGraphGeneralUtils,
                    private leftPaletteLoaderService:Services.Components.LeftPaletteLoaderService,
                    private componentInstanceFactory:Sdc.Utils.ComponentInstanceFactory,
                    private nodesFactory:Sdc.Utils.NodesFactory,
                    private commonGraphUtils: Sdc.Graph.Utils.CommonGraphUtils,
                    private matchCapabilitiesRequirementsUtils: Graph.Utils.MatchCapabilitiesRequirementsUtils) {

            this.initScopeVls();

        }


        /**
         * Delete the link on server and then remove it from graph
         * @param component
         * @param releaseLoading - true/false release the loader when finished
         * @param link - the link to delete
         */
        public deleteLink = (cy:Cy.Instance, component:Models.Components.Component, releaseLoading:boolean, link:Cy.CollectionEdges) => {

            this.loaderService.showLoader('composition-graph');
            let onSuccessDeleteRelation = (response) => {
                cy.remove(link);
            };

            if (!releaseLoading) {
                this.generalGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIAction(
                    () => component.deleteRelation(link.data().relation).then(onSuccessDeleteRelation)
                );
            } else {
                this.generalGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIActionWithReleaseCallback(
                    () => component.deleteRelation(link.data().relation).then(onSuccessDeleteRelation),
                    () => this.loaderService.hideLoader('composition-graph'));
            }
        };

        /**
         * create the link on server and than draw it on graph
         * @param link - the link to create
         * @param cy
         * @param component
         */
        public createLink = (link:Models.CompositionCiLinkBase, cy:Cy.Instance, component:Models.Components.Component):void => {

            this.loaderService.showLoader('composition-graph');

            let onSuccess:(response:Models.RelationshipModel) => void = (relation:Models.RelationshipModel) => {
                link.setRelation(relation);
                this.commonGraphUtils.insertLinkToGraph(cy, link);
            };

            link.updateLinkDirection();

            this.generalGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIActionWithReleaseCallback(
                () => component.createRelation(link.relation).then(onSuccess),
                () => this.loaderService.hideLoader('composition-graph')
            );
        };


        public initScopeVls = ():void => {

            let vls = this.leftPaletteLoaderService.getFullDataComponentList(Sdc.Utils.Constants.ResourceType.VL);
            vls.forEach((item) => {
                let key = _.find(Object.keys(item.capabilities), (key) => {
                    return _.includes(key.toLowerCase(), 'linkable');
                });
                let linkable = item.capabilities[key];
                if (linkable) {
                    if (linkable[0].maxOccurrences == '2') {
                        this.p2pVL = _.find(vls, (component:Models.Components.Component) => {
                            return component.uniqueId === item.uniqueId;
                        });

                    } else {//assuming unbounded occurrences
                        this.mp2mpVL = _.find(vls, (component:Models.Components.Component) => {
                            return component.uniqueId === item.uniqueId;
                        });
                    }
                }
            });
        };

        private setVLlinks = (match:Models.MatchReqToReq, vl:Models.ComponentsInstances.ComponentInstance):Array<Models.RelationshipModel> => {

            let relationship1 = new Models.Relationship();
            let relationship2 = new Models.Relationship();
            let newRelationshipModel1 = new Models.RelationshipModel();
            let newRelationshipModel2 = new Models.RelationshipModel();

            let capability:Models.Capability = vl.capabilities.findValueByKey('linkable')[0];
            relationship1.setRelationProperties(capability, match.requirement);
            relationship2.setRelationProperties(capability, match.secondRequirement);

            newRelationshipModel1.setRelationshipModelParams(match.fromNode, vl.uniqueId, [relationship1]);
            newRelationshipModel2.setRelationshipModelParams(match.toNode, vl.uniqueId, [relationship2]);

            return [newRelationshipModel1, newRelationshipModel2];
        };

        private createVlinks = (cy:Cy.Instance, component:Models.Components.Component, matchReqToReq:Models.MatchReqToReq, vl:Models.Components.Component):void => {

            let componentInstance:Models.ComponentsInstances.ComponentInstance = this.componentInstanceFactory.createComponentInstanceFromComponent(vl);
            let fromNodePosition:Cy.Position = cy.getElementById(matchReqToReq.fromNode).relativePosition();
            let toNodePosition:Cy.Position = cy.getElementById(matchReqToReq.toNode).relativePosition();
            let location:Cy.Position = {
                x: 0.5 * (fromNodePosition.x + toNodePosition.x),
                y: 0.5 * (fromNodePosition.y + toNodePosition.y)
            }

            componentInstance.posX = location.x;
            componentInstance.posY = location.y;

            let onFailed:(error:any) => void = (error:any) => {
                this.loaderService.hideLoader('composition-graph');
                console.info('onFailed', error);
            };

            let onSuccess = (response:Models.ComponentsInstances.ComponentInstance):void => {

                console.info('onSuccses', response);
                response.requirements = new Models.RequirementsGroup(vl.requirements);
                response.capabilities = new Models.CapabilitiesGroup(vl.capabilities);
                response.componentVersion = vl.version;
                response.setInstanceRC();

                let newLinks = this.setVLlinks(matchReqToReq, response);
                let newNode = this.nodesFactory.createNode(response);

                this.commonGraphUtils.addComponentInstanceNodeToGraph(cy, newNode);

                _.forEach(newLinks, (link) => {
                    let linkObg:Models.CompositionCiLinkBase = this.linksFactory.createGraphLink(cy, link, link.relationships[0]);
                    this.createLink(linkObg, cy, component);
                });
            };
            component.createComponentInstance(componentInstance).then(onSuccess, onFailed);
        };

        private createSimpleLink = (match:Models.MatchReqToCapability, cy:Cy.Instance, component:Models.Components.Component):void => {
            let newRelation:Models.RelationshipModel = match.matchToRelationModel();
            let linkObg:Models.CompositionCiLinkBase = this.linksFactory.createGraphLink(cy,newRelation, newRelation.relationships[0]);
            this.createLink(linkObg, cy, component);
        };

        public createLinkFromMenu = (cy:Cy.Instance, chosenMatch:Models.MatchBase, vl:Models.Components.Component, component:Models.Components.Component):void => {

            if (chosenMatch) {
                if (chosenMatch && chosenMatch instanceof Models.MatchReqToReq) {
                    this.createVlinks(cy, component, chosenMatch, vl); //TODO orit implement
                }
                if (chosenMatch && chosenMatch instanceof Models.MatchReqToCapability) {
                    this.createSimpleLink(chosenMatch, cy, component);
                }
            }
        };


        /**
         * Filters the matches for UCPE links so that shown requirements and capabilites are only related to the selected ucpe-cp
         * @param fromNode
         * @param toNode
         * @param matchesArray
         * @returns {Array<Models.MatchBase>}
         */
        public filterUcpeLinks(fromNode: Models.Graph.CompositionCiNodeBase, toNode: Models.Graph.CompositionCiNodeBase, matchesArray: Array<Models.MatchBase>): any {

            let matchLink: Array<Models.MatchBase>;

            if (fromNode.isUcpePart) {
                matchLink = _.filter(matchesArray, (match: Models.MatchBase) => {
                    return match.isOwner(fromNode.id);
                });
            }

            if (toNode.isUcpePart) {
                matchLink = _.filter(matchesArray, (match: Models.MatchBase) => {
                    return match.isOwner(toNode.id);
                });
            }
            return matchLink ? matchLink : matchesArray;
        }


        /**
         * open the connect link menu if the link drawn is valid - match  requirements & capabilities
         * @param cy
         * @param fromNode
         * @param toNode
         * @returns {any}
         */
        public onLinkDrawn(cy:Cy.Instance, fromNode:Cy.CollectionFirstNode, toNode:Cy.CollectionFirstNode):Models.RelationMenuDirectiveObj {

            if(!this.commonGraphUtils.nodeLocationsCompatible(cy, fromNode, toNode)){ return null; }
            let linkModel:Array<Models.CompositionCiLinkBase> = this.generalGraphUtils.getAllCompositionCiLinks(cy);

            let possibleRelations:Array<Models.MatchBase> = this.matchCapabilitiesRequirementsUtils.getMatchedRequirementsCapabilities(fromNode.data().componentInstance,
                toNode.data().componentInstance, linkModel, this.mp2mpVL); //TODO orit - add p2p and mp2mp

            //filter relations found to limit to specific ucpe-cp
            possibleRelations = this.filterUcpeLinks(fromNode.data(), toNode.data(), possibleRelations);

            //if found possibleRelations between the nodes we create relation menu directive and open the link menu
            if (possibleRelations.length) {
                let menuPosition = this.generalGraphUtils.getLinkMenuPosition(cy, toNode.renderedPoint());
                return new Models.RelationMenuDirectiveObj(fromNode.data(), toNode.data(), this.mp2mpVL, this.p2pVL, menuPosition, possibleRelations);
            }
            return null;
        };


        /**
         *  when we drag instance in to UCPE or out of UCPE  - get all links we need to delete - one node in ucpe and one node outside of ucpe
         * @param node - the node we dragged into or out of the ucpe
         */
        public deleteLinksWhenNodeMovedFromOrToUCPE(component:Models.Components.Component, cy:Cy.Instance, nodeMoved:Cy.CollectionNodes, vlsPendingDeletion?:Cy.CollectionNodes):void {


            let linksToDelete:Cy.CollectionElements = cy.collection();
            _.forEach(nodeMoved.neighborhood('node'), (neighborNode)=>{

                if(neighborNode.data().isUcpePart){ //existing connections to ucpe or ucpe-cp - we want to delete even though nodeLocationsCompatible will technically return true
                    linksToDelete = linksToDelete.add(nodeMoved.edgesWith(neighborNode)); // This will delete the ucpe-host-link, or the vl-ucpe-link if nodeMoved is vl
                } else if(!this.commonGraphUtils.nodeLocationsCompatible(cy, nodeMoved, neighborNode)){ //connection to regular node or vl - check if locations are compatible
                   if(!vlsPendingDeletion || !vlsPendingDeletion.intersect(neighborNode).length){ //Check if this is a link to a VL pending deletion, to prevent double deletion of between the node moved and vl
                       linksToDelete = linksToDelete.add(nodeMoved.edgesWith(neighborNode));
                   }
                }
            });



            linksToDelete.each((i, link)=>{
                this.deleteLink(cy, component, false, link);
            });

        };


        /**
         * Creates a hostedOn link between a VF and UCPE
         * @param component
         * @param cy
         * @param ucpeNode
         * @param vfNode
         */
        public createVfToUcpeLink = (component: Models.Components.Component, cy:Cy.Instance, ucpeNode:Models.Graph.NodeUcpe, vfNode:Models.Graph.CompositionCiNodeVf):void => {
            let hostedOnMatch:Models.MatchReqToCapability = this.generalGraphUtils.canBeHostedOn(cy, ucpeNode.componentInstance, vfNode.componentInstance);
            /* create relation */
            let newRelation = new Models.RelationshipModel();
            newRelation.fromNode = ucpeNode.id;
            newRelation.toNode = vfNode.id;

            let link:Models.CompositionCiLinkBase = this.linksFactory.createUcpeHostLink(newRelation);
            link.relation = hostedOnMatch.matchToRelationModel();
            this.createLink(link, cy, component);
        };


        /**
         * Handles click event on links.
         * If one edge selected: do nothing.
         /*Two edges selected - always select all
         /* Three or more edges: first click - select all, secondary click - select single.
         * @param cy
         * @param event
         */
        public handleLinkClick(cy:Cy.Instance, event : Cy.EventObject) {
            if(cy.$('edge:selected').length > 2 && event.cyTarget[0].selected()) {
                cy.$(':selected').unselect();
            } else {

                let vl: Cy.CollectionNodes = event.cyTarget[0].target('.vl-node');
                let connectedEdges:Cy.CollectionEdges = vl.connectedEdges();
                if (vl.length && connectedEdges.length > 1) {

                    setTimeout(() => {
                        vl.select();
                        connectedEdges.select();
                    }, 0);
                }
            }

        }


        /**
         * Calculates the position for the menu that modifies an existing link
         * @param event
         * @param elementWidth
         * @param elementHeight
         * @returns {Sdc.Models.Graph.Point}
         */
        public calculateLinkMenuPosition(event, elementWidth, elementHeight): Sdc.Models.Graph.Point {
            let point: Sdc.Models.Graph.Point = new Sdc.Models.Graph.Point(event.originalEvent.x,event.originalEvent.y);
            if(event.originalEvent.view.screen.height-elementHeight<point.y){
                point.y = event.originalEvent.view.screen.height-elementHeight;
            }
            if(event.originalEvent.view.screen.width-elementWidth<point.x){
                point.x = event.originalEvent.view.screen.width-elementWidth;
            }
            return point;
        };


        /**
         * Gets the menu that is displayed when you click an existing link.
         * @param link
         * @param event
         * @returns {Models.LinkMenu}
         */
        public getModifyLinkMenu(link:Cy.CollectionFirstEdge, event:Cy.EventObject):Models.LinkMenu{
            let point:Sdc.Models.Graph.Point = this.calculateLinkMenuPosition(event,Sdc.Utils.Constants.GraphUIObjects.MENU_LINK_VL_WIDTH_OFFSET,Sdc.Utils.Constants.GraphUIObjects.MENU_LINK_VL_HEIGHT_OFFSET);
            let menu:Models.LinkMenu = new Models.LinkMenu(point, true, link);
            return menu;
        };

    }



    CompositionGraphLinkUtils.$inject = [
        'LinksFactory',
        'LoaderService',
        'CompositionGraphGeneralUtils',
        'LeftPaletteLoaderService',
        'ComponentInstanceFactory',
        'NodesFactory',
        'CommonGraphUtils',
        'MatchCapabilitiesRequirementsUtils'
    ];
}