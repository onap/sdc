/**
 * Created by obarda on 1/1/2017.
 */
/// <reference path="../../../../references"/>
module Sdc.Graph.Utils {

    export class MatchCapabilitiesRequirementsUtils {

        constructor() {
        }



        public static linkable(requirement1:Models.Requirement, requirement2:Models.Requirement, vlCapability:Models.Capability):boolean {
            return MatchCapabilitiesRequirementsUtils.isMatch(requirement1, vlCapability) && MatchCapabilitiesRequirementsUtils.isMatch(requirement2, vlCapability);
        };


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

        // -------------------------------------------ALL FUNCTIONS NEED REFACTORING---------------------------------------------------------------//

        private static requirementFulfilled(fromNodeId:string, requirement:any, links:Array<Models.CompositionCiLinkBase>):boolean {
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

        private static isMatch(requirement:Models.Requirement, capability:Models.Capability):boolean {
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

        private getFromToMatches(requirements1:Models.RequirementsGroup,
                                 requirements2:Models.RequirementsGroup,
                                 capabilities:Models.CapabilitiesGroup,
                                 links:Array<Models.CompositionCiLinkBase>,
                                 fromId:string,
                                 toId:string,
                                 vlCapability?:Models.Capability):Array<Models.MatchBase> {
            let matches:Array<Models.MatchBase> = new Array<Models.MatchBase>();
            _.forEach(requirements1, (requirementValue:Array<Models.Requirement>, key) => {
                _.forEach(requirementValue, (requirement:Models.Requirement) => {
                    if (requirement.name !== "dependency" && !MatchCapabilitiesRequirementsUtils.requirementFulfilled(fromId, requirement, links)) {
                        _.forEach(capabilities, (capabilityValue:Array<Models.Capability>, key) => {
                            _.forEach(capabilityValue, (capability:Models.Capability) => {
                                if (MatchCapabilitiesRequirementsUtils.isMatch(requirement, capability)) {
                                    let match:Models.MatchReqToCapability = new Models.MatchReqToCapability(requirement, capability, true, fromId, toId);
                                    matches.push(match);
                                }
                            });
                        });
                        if (vlCapability) {
                            _.forEach(requirements2, (requirement2Value:Array<Models.Requirement>, key) => {
                                _.forEach(requirement2Value, (requirement2:Models.Requirement) => {
                                    if (!MatchCapabilitiesRequirementsUtils.requirementFulfilled(toId, requirement2, links) && MatchCapabilitiesRequirementsUtils.linkable(requirement, requirement2, vlCapability)) {
                                        let match:Models.MatchReqToReq = new Models.MatchReqToReq(requirement, requirement2, true, fromId, toId);
                                        matches.push(match);
                                    }
                                });
                            });
                        }
                    }
                });
            });
            return matches;
        }

        private getToFromMatches(requirements:Models.RequirementsGroup, capabilities:Models.CapabilitiesGroup, links:Array<Models.CompositionCiLinkBase>, fromId:string, toId:string):Array<Models.MatchReqToCapability> {
            let matches:Array<Models.MatchReqToCapability> = [];
            _.forEach(requirements, (requirementValue:Array<Models.Requirement>, key) => {
                _.forEach(requirementValue, (requirement:Models.Requirement) => {
                    if (requirement.name !== "dependency" && !MatchCapabilitiesRequirementsUtils.requirementFulfilled(toId, requirement, links)) {
                        _.forEach(capabilities, (capabilityValue:Array<Models.Capability>, key) => {
                            _.forEach(capabilityValue, (capability:Models.Capability) => {
                                if (MatchCapabilitiesRequirementsUtils.isMatch(requirement, capability)) {
                                    let match:Models.MatchReqToCapability = new Models.MatchReqToCapability(requirement, capability, false, toId, fromId);
                                    matches.push(match);
                                }
                            });
                        });
                    }
                });
            });
            return matches;
        }

        public getMatchedRequirementsCapabilities(fromComponentInstance:Models.ComponentsInstances.ComponentInstance,
                                                  toComponentInstance:Models.ComponentsInstances.ComponentInstance,
                                                  links:Array<Models.CompositionCiLinkBase>,
                                                  vl?:Models.Components.Component):Array<Models.MatchBase> {//TODO allow for VL array
            let linkCapability;
            if (vl) {
                let linkCapabilities:Array<Models.Capability> = vl.capabilities.findValueByKey('linkable');
                if (linkCapabilities) {
                    linkCapability = linkCapabilities[0];
                }
            }
            let fromToMatches:Array<Models.MatchBase> = this.getFromToMatches(fromComponentInstance.requirements,
                toComponentInstance.requirements,
                toComponentInstance.capabilities,
                links,
                fromComponentInstance.uniqueId,
                toComponentInstance.uniqueId,
                linkCapability);
            let toFromMatches:Array<Models.MatchReqToCapability> = this.getToFromMatches(toComponentInstance.requirements,
                fromComponentInstance.capabilities,
                links,
                fromComponentInstance.uniqueId,
                toComponentInstance.uniqueId);

            return fromToMatches.concat(toFromMatches);
        }





        /**
         * Step I: Check if capabilities of component match requirements of nodeDataArray
         * 1. Get component capabilities and loop on each capability
         * 2. Inside the loop, perform another loop on all nodeDataArray, and fetch the requirements for each one
         * 3. Loop on the requirements, and verify match (see in code the rules)
         *
         * Step II: Check if requirements of component match capabilities of nodeDataArray
         * 1. Get component requirements and loop on each requirement
         * 2.
         *
         * @param component         - this is the hovered resource of the left panel of composition screen
         * @param nodeDataArray     - Array of resource instances that are on the canvas
         * @param links             -getMatchedRequirementsCapabilities
         * @param vl                -
         * @returns {any[]|T[]}
         */
        public findByMatchingCapabilitiesToRequirements(component:Models.Components.Component,
                                                        nodeDataArray:Array<Models.Graph.CompositionCiNodeBase>,
                                                        links:Array<Models.CompositionCiLinkBase>,
                                                        vl?:Models.Components.Component):Array<any> {//TODO allow for VL array
            let res = [];

            // STEP I
            {
                let capabilities:any = component.capabilities;
                _.forEach(capabilities, (capabilityValue:Array<any>, capabilityKey)=> {
                    _.forEach(capabilityValue, (capability)=> {
                        _.forEach(nodeDataArray, (node:Models.Graph.CompositionCiNodeBase)=> {
                            if (node && node.componentInstance) {
                                let requirements:any = node.componentInstance.requirements;
                                let fromNodeId:string = node.componentInstance.uniqueId;
                                _.forEach(requirements, (requirementValue:Array<any>, requirementKey)=> {
                                    _.forEach(requirementValue, (requirement)=> {
                                        if (requirement.name !== "dependency" && MatchCapabilitiesRequirementsUtils.isMatch(requirement, capability)
                                            && !MatchCapabilitiesRequirementsUtils.requirementFulfilled(fromNodeId, requirement, links)) {
                                            res.push(node);
                                        }
                                    });
                                });
                            }
                        });
                    });
                });
            }

            // STEP II
            {
                let requirements:any = component.requirements;
                let fromNodeId:string = component.uniqueId;
                let linkCapability:Array<Models.Capability> = vl ? vl.capabilities.findValueByKey('linkable') : undefined;

                _.forEach(requirements, (requirementValue:Array<any>, requirementKey)=> {
                    _.forEach(requirementValue, (requirement)=> {
                        if (requirement.name !== "dependency" && !MatchCapabilitiesRequirementsUtils.requirementFulfilled(fromNodeId, requirement, links)) {
                            _.forEach(nodeDataArray, (node:any)=> {
                                if (node && node.componentInstance && node.category !== 'groupCp') {
                                    let capabilities:any = node.componentInstance.capabilities;
                                    _.forEach(capabilities, (capabilityValue:Array<any>, capabilityKey)=> {
                                        _.forEach(capabilityValue, (capability)=> {
                                            if (MatchCapabilitiesRequirementsUtils.isMatch(requirement, capability)) {
                                                res.push(node);
                                            }
                                        });
                                    });
                                    if (linkCapability) {
                                        let linkRequirements = node.componentInstance.requirements;
                                        _.forEach(linkRequirements, (value:Array<any>, key)=> {
                                            _.forEach(value, (linkRequirement)=> {
                                                if (!MatchCapabilitiesRequirementsUtils.requirementFulfilled(node.componentInstance.uniqueId, linkRequirement, links)
                                                    && MatchCapabilitiesRequirementsUtils.linkable(requirement, linkRequirement, linkCapability[0])) {
                                                    res.push(node);
                                                }
                                            });
                                        });
                                    }
                                }
                            });
                        }
                    });
                });
            }

            return _.uniq(res);
        };
    }

    MatchCapabilitiesRequirementsUtils.$inject = [];
}