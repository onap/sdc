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
import * as _ from "lodash";
import {GraphUIObjects} from "app/utils";
import {
    Match,
    CompositionCiNodeBase,
    RelationshipModel,
    ConnectRelationModel,
    LinksFactory,
    Component,
    LinkMenu,
    Point,
    CompositionCiLinkBase,
    Requirement,
    Capability,
    Relationship,
    ComponentInstance
} from "app/models";
import {CommonGraphUtils} from "../common/common-graph-utils";
import {CompositionGraphGeneralUtils} from "./composition-graph-general-utils";
import {MatchCapabilitiesRequirementsUtils} from "./match-capability-requierment-utils";
import {CompositionCiServicePathLink} from "app/models/graph/graph-links/composition-graph-links/composition-ci-service-path-link";
import {Injectable} from "@angular/core";
import {QueueServiceUtils} from "app/ng2/utils/queue-service-utils";
import {TopologyTemplateService} from "app/ng2/services/component-services/topology-template.service";
import {SdcUiServices} from "onap-ui-angular";
import {CompositionService} from "../../composition.service";
import {WorkspaceService} from "app/ng2/pages/workspace/workspace.service";

@Injectable()
export class CompositionGraphLinkUtils {

    constructor(private linksFactory: LinksFactory,
                private generalGraphUtils: CompositionGraphGeneralUtils,
                private commonGraphUtils: CommonGraphUtils,
                private queueServiceUtils: QueueServiceUtils,
                private matchCapabilitiesRequirementsUtils: MatchCapabilitiesRequirementsUtils,
                private topologyTemplateService: TopologyTemplateService,
                private loaderService: SdcUiServices.LoaderService,
                private compositionService: CompositionService,
                private workspaceService: WorkspaceService) {


    }

    /**
     * Delete the link on server and then remove it from graph
     * @param component
     * @param releaseLoading - true/false release the loader when finished
     * @param link - the link to delete
     */
    public deleteLink = (cy: Cy.Instance, component: Component, releaseLoading: boolean, link: Cy.CollectionEdges) => {

        this.loaderService.activate();
        this.queueServiceUtils.addBlockingUIAction(() => {
            this.topologyTemplateService.deleteRelation(this.workspaceService.metadata.uniqueId, this.workspaceService.metadata.componentType, link.data().relation).subscribe((deletedRelation) => {
                this.compositionService.deleteRelation(deletedRelation);
                cy.remove(link);
                this.loaderService.deactivate();
            }, (error) => {this.loaderService.deactivate()});
        });
    };

    /**
     * create the link on server and than draw it on graph
     * @param link - the link to create
     * @param cy
     * @param component
     */
    public createLink = (link: CompositionCiLinkBase, cy: Cy.Instance): void => {

        this.loaderService.activate();
        link.updateLinkDirection();

        this.queueServiceUtils.addBlockingUIAction(() => {
            this.topologyTemplateService.createRelation(this.workspaceService.metadata.uniqueId, this.workspaceService.metadata.componentType, link.relation).subscribe((relation) => {
                link.setRelation(relation);
                this.insertLinkToGraph(cy, link);
                this.compositionService.addRelation(relation);
                this.loaderService.deactivate();
            }, (error) => {this.loaderService.deactivate()})
        });
    };

    private createSimpleLink = (match: Match, cy: Cy.Instance): void => {
        let newRelation: RelationshipModel = match.matchToRelationModel();
        let linkObg: CompositionCiLinkBase = this.linksFactory.createGraphLink(cy, newRelation, newRelation.relationships[0]);
        this.createLink(linkObg, cy);
    };

    public createLinkFromMenu = (cy: Cy.Instance, chosenMatch: Match): void => {

        if (chosenMatch) {
            if (chosenMatch && chosenMatch instanceof Match) {
                this.createSimpleLink(chosenMatch, cy);
            }
        }
    }

    /**
     * open the connect link menu if the link drawn is valid - match  requirements & capabilities
     * @param cy
     * @param fromNode
     * @param toNode
     * @returns {any}
     */
    public onLinkDrawn(cy: Cy.Instance, fromNode: Cy.CollectionFirstNode, toNode: Cy.CollectionFirstNode): ConnectRelationModel {

        let linkModel: Array<CompositionCiLinkBase> = this.generalGraphUtils.getAllCompositionCiLinks(cy);

        let possibleRelations: Array<Match> = this.matchCapabilitiesRequirementsUtils.getMatchedRequirementsCapabilities(fromNode.data().componentInstance,
            toNode.data().componentInstance, linkModel);

        //if found possibleRelations between the nodes we create relation menu directive and open the link menu
        if (possibleRelations.length) {
            // let menuPosition = this.generalGraphUtils.getLinkMenuPosition(cy, toNode.renderedPoint());
            return new ConnectRelationModel(fromNode.data(), toNode.data(), possibleRelations);
        }
        return null;
    };

    private handlePathLink(cy: Cy.Instance, event: Cy.EventObject) {
        let linkData = event.cyTarget.data();
        let selectedPathId = linkData.pathId;
        let pathEdges = cy.collection(`[pathId='${selectedPathId}']`);
        if (pathEdges.length > 1) {
            setTimeout(() => {
                pathEdges.select();
            }, 0);
        }
    }

    private handleVLLink(event: Cy.EventObject) {
        let vl: Cy.CollectionNodes = event.cyTarget[0].target('.vl-node');
        let connectedEdges: Cy.CollectionEdges = vl.connectedEdges(`[type!="${CompositionCiServicePathLink.LINK_TYPE}"]`);
        if (vl.length && connectedEdges.length > 1) {
            setTimeout(() => {
                vl.select();
                connectedEdges.select();
            }, 0);
        }
    }


    /**
     * Handles click event on links.
     * If one edge selected: do nothing.
     * Two or more edges: first click - select all, secondary click - select single.
     * @param cy
     * @param event
     */
    public handleLinkClick(cy: Cy.Instance, event: Cy.EventObject) {
        if (cy.$('edge:selected').length > 1 && event.cyTarget[0].selected()) {
            cy.$(':selected').unselect();
        } else {
            if (event.cyTarget[0].data().type === CompositionCiServicePathLink.LINK_TYPE) {
                this.handlePathLink(cy, event);
            }
            else {
                this.handleVLLink(event);
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
    public calculateLinkMenuPosition(event, elementWidth, elementHeight): Point {
        let point: Point = new Point(event.originalEvent.clientX, event.originalEvent.clientY);
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
    public getModifyLinkMenu(link: Cy.CollectionFirstEdge, event: Cy.EventObject): LinkMenu {
        let point: Point = this.calculateLinkMenuPosition(event, GraphUIObjects.MENU_LINK_VL_WIDTH_OFFSET, GraphUIObjects.MENU_LINK_VL_HEIGHT_OFFSET);
        let menu: LinkMenu = new LinkMenu(point, true, link);
        return menu;
    };

    /**
     * Returns relation source and target nodes.
     * @param nodes - all nodes in graph in order to find the edge connecting the two nodes
     * @param fromNodeId
     * @param toNodeId
     * @returns [source, target] array of source node and target node.
     */
    public getRelationNodes(nodes: Cy.CollectionNodes, fromNodeId: string, toNodeId: string) {
        return [
            _.find(nodes, (node: Cy.CollectionFirst) => node.data().id === fromNodeId),
            _.find(nodes, (node: Cy.CollectionFirst) => node.data().id === toNodeId)
        ];
    }


    /**
     *  go over the relations and draw links on the graph
     * @param cy
     * @param getRelationRequirementCapability - function to get requirement and capability of a relation
     */
    public initGraphLinks(cy: Cy.Instance, relations: RelationshipModel[]) {
        if (relations) {
            _.forEach(relations, (relationshipModel: RelationshipModel) => {
                _.forEach(relationshipModel.relationships, (relationship: Relationship) => {
                    let linkToCreate = this.linksFactory.createGraphLink(cy, relationshipModel, relationship);
                    this.insertLinkToGraph(cy, linkToCreate);
                });
            });
        }
    }

    /**
     * Add link to graph - only draw the link
     * @param cy
     * @param link
     * @param getRelationRequirementCapability
     */
    public insertLinkToGraph = (cy: Cy.Instance, link: CompositionCiLinkBase) => {
        const relationNodes = this.getRelationNodes(cy.nodes(), link.source, link.target);
        const sourceNode: CompositionCiNodeBase = relationNodes[0] && relationNodes[0].data();
        const targetNode: CompositionCiNodeBase = relationNodes[1] && relationNodes[1].data();
        if ((sourceNode && !sourceNode.certified) || (targetNode && !targetNode.certified)) {
            link.classes = 'not-certified-link';
        }
        let linkElement = cy.add({
            group: 'edges',
            data: link,
            classes: link.classes
        });

        const getLinkRequirementCapability = () =>
            this.getRelationRequirementCapability(link.relation.relationships[0], sourceNode.componentInstance, targetNode.componentInstance);
        this.commonGraphUtils.initLinkTooltip(linkElement, link.relation.relationships[0], getLinkRequirementCapability);
    };

    public syncComponentByRelation(relation: RelationshipModel) {
        let componentInstances = this.compositionService.getComponentInstances();
        relation.relationships.forEach((rel) => {
            if (rel.capability) {
                const toComponentInstance: ComponentInstance = componentInstances.find((inst) => inst.uniqueId === relation.toNode);
                const toComponentInstanceCapability: Capability = toComponentInstance.findCapability(
                    rel.capability.type, rel.capability.uniqueId, rel.capability.ownerId, rel.capability.name);
                const isCapabilityFulfilled: boolean = rel.capability.isFulfilled();
                if (isCapabilityFulfilled && toComponentInstanceCapability) {
                    // if capability is fulfilled and in component, then remove it
                    console.log('Capability is fulfilled', rel.capability.getFullTitle(), rel.capability.leftOccurrences);
                    toComponentInstance.capabilities[rel.capability.type].splice(
                        toComponentInstance.capabilities[rel.capability.type].findIndex((cap) => cap === toComponentInstanceCapability), 1
                    )
                } else if (!isCapabilityFulfilled && !toComponentInstanceCapability) {
                    // if capability is unfulfilled and not in component, then add it
                    console.log('Capability is unfulfilled', rel.capability.getFullTitle(), rel.capability.leftOccurrences);
                    toComponentInstance.capabilities[rel.capability.type].push(rel.capability);
                }
            }
            if (rel.requirement) {
                const fromComponentInstance: ComponentInstance = componentInstances.find((inst) => inst.uniqueId === relation.fromNode);
                const fromComponentInstanceRequirement: Requirement = fromComponentInstance.findRequirement(
                    rel.requirement.capability, rel.requirement.uniqueId, rel.requirement.ownerId, rel.requirement.name);
                const isRequirementFulfilled: boolean = rel.requirement.isFulfilled();
                if (isRequirementFulfilled && fromComponentInstanceRequirement) {
                    // if requirement is fulfilled and in component, then remove it
                    console.log('Requirement is fulfilled', rel.requirement.getFullTitle(), rel.requirement.leftOccurrences);
                    fromComponentInstance.requirements[rel.requirement.capability].splice(
                        fromComponentInstance.requirements[rel.requirement.capability].findIndex((req) => req === fromComponentInstanceRequirement), 1
                    )
                } else if (!isRequirementFulfilled && !fromComponentInstanceRequirement) {
                    // if requirement is unfulfilled and not in component, then add it
                    console.log('Requirement is unfulfilled', rel.requirement.getFullTitle(), rel.requirement.leftOccurrences);
                    fromComponentInstance.requirements[rel.requirement.capability].push(rel.requirement);
                }
            }
        });
    }

    public getRelationRequirementCapability(relationship: Relationship, sourceNode: ComponentInstance, targetNode: ComponentInstance): Promise<{ requirement: Requirement, capability: Capability }> {
        // try find the requirement and capability in the source and target component instances:
        let capability: Capability = targetNode.findCapability(undefined,
            relationship.relation.capabilityUid,
            relationship.relation.capabilityOwnerId,
            relationship.relation.capability);
        let requirement: Requirement = sourceNode.findRequirement(undefined,
            relationship.relation.requirementUid,
            relationship.relation.requirementOwnerId,
            relationship.relation.requirement);

        return new Promise<{ requirement: Requirement, capability: Capability }>((resolve, reject) => {
            if (capability && requirement) {
                resolve({capability, requirement});
            }
            else {
                // if requirement and/or capability is missing, then fetch the full relation with its requirement and capability:
                this.topologyTemplateService.fetchRelation(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, relationship.relation.id).subscribe((fetchedRelation) => {
                    this.syncComponentByRelation(fetchedRelation);
                    resolve({
                        capability: capability || fetchedRelation.relationships[0].capability,
                        requirement: requirement || fetchedRelation.relationships[0].requirement
                    });
                }, reject);
            }
        });
    }
}

