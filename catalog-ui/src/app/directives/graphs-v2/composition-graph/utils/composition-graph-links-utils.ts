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

/**
 * Created by obarda on 6/28/2016.
 */
import {GraphUIObjects} from "app/utils";
import {LoaderService} from "app/services";
import {
    NodeUcpe,
    CompositionCiNodeVf,
    Match,
    CompositionCiNodeBase,
    RelationshipModel,
    ConnectRelationModel,
    LinksFactory,
    Component,
    LinkMenu,
    Point,
    CompositionCiLinkBase
} from "app/models";
import {CommonGraphUtils} from "../../common/common-graph-utils";
import {CompositionGraphGeneralUtils} from "./composition-graph-general-utils";
import {MatchCapabilitiesRequirementsUtils} from "./match-capability-requierment-utils";

export class CompositionGraphLinkUtils {

    constructor(private linksFactory:LinksFactory,
                private loaderService:LoaderService,
                private generalGraphUtils:CompositionGraphGeneralUtils,
                private commonGraphUtils:CommonGraphUtils,
                private matchCapabilitiesRequirementsUtils:MatchCapabilitiesRequirementsUtils) {
    }


    /**
     * Delete the link on server and then remove it from graph
     * @param component
     * @param releaseLoading - true/false release the loader when finished
     * @param link - the link to delete
     */
    public deleteLink = (cy:Cy.Instance, component:Component, releaseLoading:boolean, link:Cy.CollectionEdges) => {

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
    public createLink = (link:CompositionCiLinkBase, cy:Cy.Instance, component:Component):void => {

        this.loaderService.showLoader('composition-graph');

        let onSuccess:(response:RelationshipModel) => void = (relation:RelationshipModel) => {
            link.setRelation(relation);
            this.commonGraphUtils.insertLinkToGraph(cy, link);
        };

        link.updateLinkDirection();

        this.generalGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIActionWithReleaseCallback(
            () => component.createRelation(link.relation).then(onSuccess),
            () => this.loaderService.hideLoader('composition-graph')
        );
    };

    private createSimpleLink = (match:Match, cy:Cy.Instance, component:Component):void => {
        let newRelation:RelationshipModel = match.matchToRelationModel();
        let linkObg:CompositionCiLinkBase = this.linksFactory.createGraphLink(cy, newRelation, newRelation.relationships[0]);
        this.createLink(linkObg, cy, component);
    };

    public createLinkFromMenu = (cy:Cy.Instance, chosenMatch:Match, component:Component):void => {

        if (chosenMatch) {
            if (chosenMatch && chosenMatch instanceof Match) {
                this.createSimpleLink(chosenMatch, cy, component);
            }
        }
    };


    /**
     * Filters the matches for UCPE links so that shown requirements and capabilites are only related to the selected ucpe-cp
     * @param fromNode
     * @param toNode
     * @param matchesArray
     * @returns {Array<MatchBase>}
     */
    public filterUcpeLinks(fromNode:CompositionCiNodeBase, toNode:CompositionCiNodeBase, matchesArray:Array<Match>):any {

        let matchLink:Array<Match>;

        if (fromNode.isUcpePart) {
            matchLink = _.filter(matchesArray, (match:Match) => {
                return match.isOwner(fromNode.id);
            });
        }

        if (toNode.isUcpePart) {
            matchLink = _.filter(matchesArray, (match:Match) => {
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
    public onLinkDrawn(cy:Cy.Instance, fromNode:Cy.CollectionFirstNode, toNode:Cy.CollectionFirstNode):ConnectRelationModel {

        if (!this.commonGraphUtils.nodeLocationsCompatible(cy, fromNode, toNode)) {
            return null;
        }
        let linkModel:Array<CompositionCiLinkBase> = this.generalGraphUtils.getAllCompositionCiLinks(cy);

        let possibleRelations:Array<Match> = this.matchCapabilitiesRequirementsUtils.getMatchedRequirementsCapabilities(fromNode.data().componentInstance,
            toNode.data().componentInstance, linkModel);

        //filter relations found to limit to specific ucpe-cp
        possibleRelations = this.filterUcpeLinks(fromNode.data(), toNode.data(), possibleRelations);

        //if found possibleRelations between the nodes we create relation menu directive and open the link menu
        if (possibleRelations.length) {
            // let menuPosition = this.generalGraphUtils.getLinkMenuPosition(cy, toNode.renderedPoint());
            return new ConnectRelationModel(fromNode.data(), toNode.data(), possibleRelations);
        }
        return null;
    };


    /**
     *  when we drag instance in to UCPE or out of UCPE  - get all links we need to delete - one node in ucpe and one node outside of ucpe
     * @param node - the node we dragged into or out of the ucpe
     */
    public deleteLinksWhenNodeMovedFromOrToUCPE(component:Component, cy:Cy.Instance, nodeMoved:Cy.CollectionNodes, vlsPendingDeletion?:Cy.CollectionNodes):void {


        let linksToDelete:Cy.CollectionElements = cy.collection();
        _.forEach(nodeMoved.neighborhood('node'), (neighborNode)=> {

            if (neighborNode.data().isUcpePart) { //existing connections to ucpe or ucpe-cp - we want to delete even though nodeLocationsCompatible will technically return true
                linksToDelete = linksToDelete.add(nodeMoved.edgesWith(neighborNode)); // This will delete the ucpe-host-link, or the vl-ucpe-link if nodeMoved is vl
            } else if (!this.commonGraphUtils.nodeLocationsCompatible(cy, nodeMoved, neighborNode)) { //connection to regular node or vl - check if locations are compatible
                if (!vlsPendingDeletion || !vlsPendingDeletion.intersect(neighborNode).length) { //Check if this is a link to a VL pending deletion, to prevent double deletion of between the node moved and vl
                    linksToDelete = linksToDelete.add(nodeMoved.edgesWith(neighborNode));
                }
            }
        });

        linksToDelete.each((i, link)=> {
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
    public createVfToUcpeLink = (component:Component, cy:Cy.Instance, ucpeNode:NodeUcpe, vfNode:CompositionCiNodeVf):void => {
        let hostedOnMatch:Match = this.generalGraphUtils.canBeHostedOn(cy, ucpeNode.componentInstance, vfNode.componentInstance);
        /* create relation */
        let newRelation = new RelationshipModel();
        newRelation.fromNode = ucpeNode.id;
        newRelation.toNode = vfNode.id;

        let link:CompositionCiLinkBase = this.linksFactory.createUcpeHostLink(newRelation);
        link.relation = hostedOnMatch.matchToRelationModel();
        this.createLink(link, cy, component);
    };


    /**
     * Handles click event on links.
     * If one edge selected: do nothing.
     * Two or more edges: first click - select all, secondary click - select single.
     * @param cy
     * @param event
     */
    public handleLinkClick(cy:Cy.Instance, event:Cy.EventObject) {
        if (cy.$('edge:selected').length > 1 && event.cyTarget[0].selected()) {
            cy.$(':selected').unselect();
        } else {

            let vl:Cy.CollectionNodes = event.cyTarget[0].target('.vl-node');
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
     * @returns {Point}
     */
    public calculateLinkMenuPosition(event, elementWidth, elementHeight):Point {
        let point:Point = new Point(event.originalEvent.clientX, event.originalEvent.clientY);
        if (event.originalEvent.view.screen.height - elementHeight < point.y) {
            point.y = event.originalEvent.view.screen.height - elementHeight;
        }
        if (event.originalEvent.view.screen.width - elementWidth < point.x) {
            point.x = event.originalEvent.view.screen.width - elementWidth;
        }
        return point;
    };


    /**
     * Gets the menu that is displayed when you click an existing link.
     * @param link
     * @param event
     * @returns {LinkMenu}
     */
    public getModifyLinkMenu(link:Cy.CollectionFirstEdge, event:Cy.EventObject):LinkMenu {
        let point:Point = this.calculateLinkMenuPosition(event, GraphUIObjects.MENU_LINK_VL_WIDTH_OFFSET, GraphUIObjects.MENU_LINK_VL_HEIGHT_OFFSET);
        let menu:LinkMenu = new LinkMenu(point, true, link);
        return menu;
    };

}


CompositionGraphLinkUtils.$inject = [
    'LinksFactory',
    'LoaderService',
    'CompositionGraphGeneralUtils',
    'CommonGraphUtils',
    'MatchCapabilitiesRequirementsUtils'
];
