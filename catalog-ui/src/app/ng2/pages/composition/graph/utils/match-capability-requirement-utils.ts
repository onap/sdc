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
import { Injectable } from '@angular/core';
import {
    CapabilitiesGroup, Capability, ComponentInstance, CompositionCiLinkBase,
    CompositionCiNodeBase, Match, Requirement, RequirementsGroup
} from 'app/models';
import * as _ from 'lodash';

/**
 * Created by obarda on 1/1/2017.
 */
@Injectable()
export class MatchCapabilitiesRequirementsUtils {

    /**
     * Shows + icon in corner of each node passed in
     * @param filteredNodesData
     * @param cy
     */
    public highlightMatchingComponents(filteredNodesData, cy: Cy.Instance) {
        _.each(filteredNodesData, (data: any) => {
            const node = cy.getElementById(data.id);
            cy.emit('showhandle', [node]);
        });
    }

    /**
     * Adds opacity to each node that cannot be linked to hovered node
     * @param filteredNodesData
     * @param nodesData
     * @param cy
     * @param hoveredNodeData
     */
    public fadeNonMachingComponents(filteredNodesData, nodesData, cy: Cy.Instance, hoveredNodeData?) {
        const fadeNodes = _.xorWith(nodesData, filteredNodesData, (node1, node2) => {
            return node1.id === node2.id;
        });
        if (hoveredNodeData) {
            _.remove(fadeNodes, hoveredNodeData);
        }
        cy.batch(() => {
            _.each(fadeNodes, (node) => {
                cy.getElementById(node.id).style({'background-image-opacity': 0.4});
            });
        });
    }

    /**
     * Resets all nodes to regular opacity
     * @param cy
     */
    public resetFadedNodes(cy: Cy.Instance) {
        cy.batch(() => {
            cy.nodes().style({'background-image-opacity': 1});
        });
    }

    public getMatchedRequirementsCapabilities(fromComponentInstance: ComponentInstance,
                                              toComponentInstance: ComponentInstance,
                                              links: CompositionCiLinkBase[]): Match[] {
        const fromToMatches: Match[] = this.getMatches(fromComponentInstance.requirements,
            toComponentInstance.capabilities,
            links,
            fromComponentInstance.uniqueId,
            toComponentInstance.uniqueId, true);
        const toFromMatches: Match[] = this.getMatches(toComponentInstance.requirements,
            fromComponentInstance.capabilities,
            links,
            toComponentInstance.uniqueId,
            fromComponentInstance.uniqueId, false);

        return fromToMatches.concat(toFromMatches);
    }

    /***** REFACTORED FUNCTIONS START HERE *****/

    public getMatches(requirements: RequirementsGroup, capabilities: CapabilitiesGroup, links: CompositionCiLinkBase[],
                      fromId: string, toId: string, isFromTo: boolean): Match[] {
        const matches: Match[] = [];
        const unfulfilledReqs = this.getUnfulfilledRequirements(fromId, requirements, links);
        _.forEach(unfulfilledReqs, (req) => {
            _.forEach(_.flatten(_.values(capabilities)), (capability: Capability) => {
                if (this.isMatch(req, capability)) {
                    if (isFromTo) {
                        matches.push(new Match(req, capability, isFromTo, fromId, toId));
                    } else {
                        matches.push(new Match(req, capability, isFromTo, toId, fromId));
                    }
                }
            });
        });
        return matches;
    }

    public getUnfulfilledRequirements = (fromNodeId: string, requirements: RequirementsGroup, links: CompositionCiLinkBase[]): Requirement[] => {
        const requirementArray: Requirement[] = [];
        _.forEach(_.flatten(_.values(requirements)), (requirement: Requirement) => {
            if (!this.isRequirementFulfilled(fromNodeId, requirement, links)) {
                requirementArray.push(requirement);
            }
        });
        return requirementArray;
    };

    /**
     * Returns true if there is a match between the capabilities and requirements that are passed in
     * @param requirements
     * @param capabilities
     * @returns {boolean}
     */
    public containsMatch = (requirements: Requirement[], capabilities: CapabilitiesGroup): boolean => {
        return _.some(requirements, (req: Requirement) => {
            return _.some(_.flatten(_.values(capabilities)), (capability: Capability) => {
                return this.isMatch(req, capability);
            });
        });
    }

    public hasUnfulfilledRequirementContainingMatch = (node: CompositionCiNodeBase, componentRequirements: Requirement[], capabilities: CapabilitiesGroup, links: CompositionCiLinkBase[]) => {
        if (node && node.componentInstance) {
            // Check if node has unfulfilled requirement that can be filled by component (#2)
            const nodeRequirements: Requirement[] = this.getUnfulfilledRequirements(node.componentInstance.uniqueId, node.componentInstance.requirements, links);
            if (!nodeRequirements.length) {
                return false;
            }
            if (this.containsMatch(nodeRequirements, capabilities)) {
                return true;
            }
        }
    }

    /**
     * Returns array of nodes that can connect to the component.
     * In order to connect, one of the following conditions must be met:
     * 1. component has an unfulfilled requirement that matches a node's capabilities
     * 2. node has an unfulfilled requirement that matches the component's capabilities
     * 3. vl is passed in which has the capability to fulfill requirement from component and requirement on node.
     */
    public findMatchingNodesToComponentInstance(componentInstance: ComponentInstance, nodeDataArray: CompositionCiNodeBase[], links: CompositionCiLinkBase[]): any[] {
        return _.filter(nodeDataArray, (node: CompositionCiNodeBase) => {
            const matchedRequirementsCapabilities = this.getMatchedRequirementsCapabilities(node.componentInstance, componentInstance, links);
            return matchedRequirementsCapabilities && matchedRequirementsCapabilities.length > 0;
        });
    }

    public isMatch(requirement: Requirement, capability: Capability): boolean {
        if (capability.type === requirement.capability) {
            if (requirement.node) {
                if (_.includes(capability.capabilitySources, requirement.node)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private isRequirementFulfilled(fromNodeId: string, requirement: any, links: CompositionCiLinkBase[]): boolean {
        return _.some(links, {
            relation: {
                fromNode: fromNodeId,
                relationships: [{
                    relation: {
                        requirementOwnerId: requirement.ownerId,
                        requirement: requirement.name,
                        relationship: {
                            type: requirement.relationship
                        }

                    }
                }]
            }
        });
    }

}
