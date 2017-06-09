import {Requirement, CompositionCiLinkBase, ComponentInstance, CapabilitiesGroup, RequirementsGroup, MatchReqToCapability, MatchBase,
    MatchReqToReq,CompositionCiNodeBase, Component, Capability} from "app/models";
/**
 * Created by obarda on 1/1/2017.
 */

export class MatchCapabilitiesRequirementsUtils {

    constructor() {
    }

    public static linkable(requirement1:Requirement, requirement2:Requirement, vlCapability:Capability):boolean {
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

    private static requirementFulfilled(fromNodeId:string, requirement:any, links:Array<CompositionCiLinkBase>):boolean {
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

    private getFromToMatches(requirements1:RequirementsGroup,
                             requirements2:RequirementsGroup,
                             capabilities:CapabilitiesGroup,
                             links:Array<CompositionCiLinkBase>,
                             fromId:string,
                             toId:string,
                             vlCapability?:Capability):Array<MatchBase> {
        let matches:Array<MatchBase> = new Array<MatchBase>();
        _.forEach(requirements1, (requirementValue:Array<Requirement>, key) => {
            _.forEach(requirementValue, (requirement:Requirement) => {
                if (requirement.name !== "dependency" && !MatchCapabilitiesRequirementsUtils.requirementFulfilled(fromId, requirement, links)) {
                    _.forEach(capabilities, (capabilityValue:Array<Capability>, key) => {
                        _.forEach(capabilityValue, (capability:Capability) => {
                            if (MatchCapabilitiesRequirementsUtils.isMatch(requirement, capability)) {
                                let match:MatchReqToCapability = new MatchReqToCapability(requirement, capability, true, fromId, toId);
                                matches.push(match);
                            }
                        });
                    });
                    if (vlCapability) {
                        _.forEach(requirements2, (requirement2Value:Array<Requirement>, key) => {
                            _.forEach(requirement2Value, (requirement2:Requirement) => {
                                if (!MatchCapabilitiesRequirementsUtils.requirementFulfilled(toId, requirement2, links) && MatchCapabilitiesRequirementsUtils.linkable(requirement, requirement2, vlCapability)) {
                                    let match:MatchReqToReq = new MatchReqToReq(requirement, requirement2, true, fromId, toId);
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

    private getToFromMatches(requirements:RequirementsGroup, capabilities:CapabilitiesGroup, links:Array<CompositionCiLinkBase>, fromId:string, toId:string):Array<MatchReqToCapability> {
        let matches:Array<MatchReqToCapability> = [];
        _.forEach(requirements, (requirementValue:Array<Requirement>, key) => {
            _.forEach(requirementValue, (requirement:Requirement) => {
                if (requirement.name !== "dependency" && !MatchCapabilitiesRequirementsUtils.requirementFulfilled(toId, requirement, links)) {
                    _.forEach(capabilities, (capabilityValue:Array<Capability>, key) => {
                        _.forEach(capabilityValue, (capability:Capability) => {
                            if (MatchCapabilitiesRequirementsUtils.isMatch(requirement, capability)) {
                                let match:MatchReqToCapability = new MatchReqToCapability(requirement, capability, false, toId, fromId);
                                matches.push(match);
                            }
                        });
                    });
                }
            });
        });
        return matches;
    }

    public getMatchedRequirementsCapabilities(fromComponentInstance:ComponentInstance,
                                              toComponentInstance:ComponentInstance,
                                              links:Array<CompositionCiLinkBase>,
                                              vl?:Component):Array<MatchBase> {//TODO allow for VL array
        let linkCapability;
        if (vl) {
            let linkCapabilities:Array<Capability> = vl.capabilities.findValueByKey('linkable');
            if (linkCapabilities) {
                linkCapability = linkCapabilities[0];
            }
        }
        let fromToMatches:Array<MatchBase> = this.getFromToMatches(fromComponentInstance.requirements,
            toComponentInstance.requirements,
            toComponentInstance.capabilities,
            links,
            fromComponentInstance.uniqueId,
            toComponentInstance.uniqueId,
            linkCapability);
        let toFromMatches:Array<MatchReqToCapability> = this.getToFromMatches(toComponentInstance.requirements,
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
    public findByMatchingCapabilitiesToRequirements(component:Component,
                                                    nodeDataArray:Array<CompositionCiNodeBase>,
                                                    links:Array<CompositionCiLinkBase>,
                                                    vl?:Component):Array<any> {//TODO allow for VL array
        let res = [];

        // STEP I
        {
            let capabilities:any = component.capabilities;
            _.forEach(capabilities, (capabilityValue:Array<any>, capabilityKey)=> {
                _.forEach(capabilityValue, (capability)=> {
                    _.forEach(nodeDataArray, (node:CompositionCiNodeBase)=> {
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
            let linkCapability:Array<Capability> = vl ? vl.capabilities.findValueByKey('linkable') : undefined;

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
