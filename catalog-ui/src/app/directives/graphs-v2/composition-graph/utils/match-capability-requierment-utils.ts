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

import {
    Requirement, CompositionCiLinkBase, CapabilitiesGroup, RequirementsGroup, Match,
    CompositionCiNodeBase, Component, Capability
} from "app/models";
import {ComponentInstance} from "../../../../models/componentsInstances/componentInstance";
/**
 * Created by obarda on 1/1/2017.
 */

export class MatchCapabilitiesRequirementsUtils {

    /**
     * Shows + icon in corner of each node passed in
     * @param filteredNodesData
     * @param cy
     */
    public highlightMatchingComponents(filteredNodesData, cy:Cy.Instance) {
        _.each(filteredNodesData, (data:any) => {
            let node = cy.getElementById(data.id);
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
    public fadeNonMachingComponents(filteredNodesData, nodesData, cy:Cy.Instance, hoveredNodeData?) {
        let fadeNodes = _.xorWith(nodesData, filteredNodesData, (node1, node2) => {
            return node1.id === node2.id;
        });
        if (hoveredNodeData) {
            _.remove(fadeNodes, hoveredNodeData);
        }
        cy.batch(()=> {
            _.each(fadeNodes, (node) => {
                cy.getElementById(node.id).style({'background-image-opacity': 0.4});
            });
        })
    }

    /**
     * Resets all nodes to regular opacity
     * @param cy
     */
    public resetFadedNodes(cy:Cy.Instance) {
        cy.batch(()=> {
            cy.nodes().style({'background-image-opacity': 1});
        })
    }

    private static isRequirementFulfilled(fromNodeId:string, requirement:any, links:Array<CompositionCiLinkBase>):boolean {
        return _.some(links, {
            'relation': {
                'fromNode': fromNodeId,
                'relationships': [{
                    'requirementOwnerId': requirement.ownerId,
                    'requirement': requirement.name,
                    'relationship': {
                        'type': requirement.relationship
                    }
                }
                ]
            }
        });
    };

    private static isMatch(requirement:Requirement, capability:Capability):boolean {
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
    };

    public getMatchedRequirementsCapabilities(fromComponentInstance:ComponentInstance,
                                              toComponentInstance:ComponentInstance,
                                              links:Array<CompositionCiLinkBase>):Array<Match> {
        let fromToMatches:Array<Match> = this.getMatches(fromComponentInstance.requirements,
            toComponentInstance.capabilities,
            links,
            fromComponentInstance.uniqueId,
            toComponentInstance.uniqueId, true);
        let toFromMatches:Array<Match> = this.getMatches(toComponentInstance.requirements,
            fromComponentInstance.capabilities,
            links,
            fromComponentInstance.uniqueId,
            toComponentInstance.uniqueId, false);

        return fromToMatches.concat(toFromMatches);
    }

    /***** REFACTORED FUNCTIONS START HERE *****/

    public getMatches(requirements:RequirementsGroup, capabilities:CapabilitiesGroup, links:Array<CompositionCiLinkBase>,
                      fromId:string, toId:string, isFromTo: boolean):Array<Match> {
        let matches:Array<Match> = [];
        let unfulfilledReqs = this.getUnfulfilledRequirements(fromId, requirements, links);
        _.forEach(unfulfilledReqs, (req)=> {
            _.forEach(_.flatten(_.values(capabilities)), (capability:Capability)=> {
                if (MatchCapabilitiesRequirementsUtils.isMatch(req, capability)) {
                    if(isFromTo) {
                        matches.push(new Match(req, capability, isFromTo, fromId, toId));
                    } else{
                        matches.push(new Match(req, capability, isFromTo, toId, fromId));
                    }
                }
            });
        });
        return matches;
    }

    public getUnfulfilledRequirements = (fromNodeId:string, requirements:RequirementsGroup, links:Array<CompositionCiLinkBase>):Array<Requirement>=> {

        let requirementArray:Array<Requirement> = [];
        _.forEach(_.flatten(_.values(requirements)), (requirement:Requirement)=> {
            if (requirement.name !== "dependency" && !MatchCapabilitiesRequirementsUtils.isRequirementFulfilled(fromNodeId, requirement, links)) {
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
    public containsMatch = (requirements:Array<Requirement>, capabilities:CapabilitiesGroup):boolean => {
        return _.some(requirements, (req:Requirement)=> {
            return _.some(_.flatten(_.values(capabilities)), (capability:Capability) => {
                return MatchCapabilitiesRequirementsUtils.isMatch(req, capability);
            });
        });
    };

    /**
     * Returns array of nodes that can connect to the component.
     * In order to connect, one of the following conditions must be met:
     * 1. component has an unfulfilled requirement that matches a node's capabilities
     * 2. node has an unfulfilled requirement that matches the component's capabilities
     * 3. vl is passed in which has the capability to fulfill requirement from component and requirement on node.
     */
    public findMatchingNodes(component:Component, nodeDataArray:Array<CompositionCiNodeBase>,
                             links:Array<CompositionCiLinkBase>):Array<any> //TODO allow for VL array and TEST
    {
        let componentRequirements:Array<Requirement> = this.getUnfulfilledRequirements(component.uniqueId, component.requirements, links);
        return _.filter(nodeDataArray, (node:any)=> {
            if (node && node.componentInstance) {

                //Check if component has an unfulfilled requirement that can be met by one of nodes's capabilities (#1)
                if (componentRequirements.length && node.category !== 'groupCp' && this.containsMatch(componentRequirements, node.componentInstance.capabilities)) {
                    return true;

                } else { //Check if node has unfulfilled requirement that can be filled by component (#2)
                    let nodeRequirements:Array<Requirement> = this.getUnfulfilledRequirements(node.componentInstance.uniqueId, node.componentInstance.requirements, links);
                    if (!nodeRequirements.length) return false;
                    if (this.containsMatch(nodeRequirements, component.capabilities)) {
                        return true;
                    }
                }
            }
        });
    }
}

MatchCapabilitiesRequirementsUtils.$inject = [];
