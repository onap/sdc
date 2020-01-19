import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Mock } from 'ts-mockery';
import {
    CapabilitiesGroup,
    Capability, ComponentInstance, CompositionCiLinkBase, CompositionCiNodeBase, CompositionCiNodeCp,
    CompositionCiNodeVf, CompositionCiNodeVl,
    Requirement, RequirementsGroup
} from '../../../../../models';
import { MatchCapabilitiesRequirementsUtils } from './match-capability-requierment-utils';

describe('match capability requirements utils service ', () => {

    const bindableReq = Mock.of<Requirement>({
        capability : 'tosca.capabilities.network.Bindable',
        name: 'virtualBinding',
        relationship: 'tosca.relationships.network.BindsTo',
        uniqueId: 'eef99154-8039-4227-ba68-62a32e6b0d98.virtualBinding',
        ownerId : 'extcp0',
        ownerName : 's'
    });

    const virtualLinkReq = Mock.of<Requirement>({
        capability: 'tosca.capabilities.network.Linkable',
        name: 'virtualLink',
        relationship: 'tosca.relationships.network.LinksTo',
        uniqueId: 'eef99154-8039-4227-ba68-62a32e6b0d98.virtualLink',
        ownerId : '',
        ownerName : 's'
    });

    const storeAttachmentReq = Mock.of<Requirement>({
        capability: 'tosca.capabilities.Attachment',
        name: 'local_storage',
        relationship: 'tosca.relationships.AttachesTo',
        uniqueId: 'eef99154-8039-4227-ba68-62a32e6b0d98.local_storage',
        node: 'tosca.nodes.BlockStorage',
        ownerId : '',
        ownerName : 's'
    });

    const vlAttachmentReq = Mock.of<Requirement>({
        capability: 'tosca.capabilities.Attachment',
        name: 'local_storage',
        relationship: 'tosca.relationships.AttachesTo',
        uniqueId: 'eef99154-8039-4227-ba68-62a32e6b0d98.local_storage',
        node: 'tosca.nodes.BlockStorage',
        ownerId : '',
        ownerName : 's'
    });

    const extVirtualLinkReq = Mock.of<Requirement>({
        capability: 'tosca.capabilities.network.Linkable',
        name: 'external_virtualLink',
        relationship: 'tosca.relationships.network.LinksTo',
        uniqueId: 'eef99154-8039-4227-ba68-62a32e6b0d98.external_virtualLink'
    });

    const dependencyReq = Mock.of<Requirement>({
        capability: 'tosca.capabilities.Node',
        name: 'dependency',
        relationship: 'tosca.relationships.DependsOn',
        uniqueId: 'eef99154-8039-4227-ba68-62a32e6b0d98.dependency'
    });

    const featureCap = Mock.of<Capability>({
        type: 'tosca.capabilities.Node',
        name: 'feature',
        uniqueId: 'capability.ddf1301e-866b-4fa3-bc4f-edbd81e532cd.feature',
        maxOccurrences: 'UNBOUNDED',
        minOccurrences: '1'
    });

    const internalConnPointCap = Mock.of<Capability>({
        type: 'tosca.capabilities.Node',
        name: 'internal_connectionPoint',
        capabilitySources : ['org.openecomp.resource.cp.extCP'],
        uniqueId: 'capability.ddf1301e-866b-4fa3-bc4f-edbd81e532cd.internal_connectionPoint',
        maxOccurrences: 'UNBOUNDED',
        minOccurrences: '1'
    });

    const blockStoreAttachmentCap = Mock.of<Capability>({
        type: 'tosca.capabilities.Attachment',
        name: 'attachment',
        capabilitySources: ['tosca.nodes.BlockStorage'],
        uniqueId: 'capability.ddf1301e-866b-4fa3-bc4f-edbd81e532cd.attachment',
        maxOccurrences: 'UNBOUNDED',
        minOccurrences: '1'
    });

    const bindingCap = Mock.of<Capability>({
        type: 'tosca.capabilities.network.Bindable',
        name: 'binding',
        capabilitySources: ['tosca.nodes.Compute'],
        uniqueId: 'capability.ddf1301e-866b-4fa3-bc4f-edbd81e532cd.binding',
        maxOccurrences: 'UNBOUNDED',
        minOccurrences: '1',
    });

    const linkableCap = Mock.of<Capability>({
        type: 'tosca.capabilities.network.Linkable',
        capabilitySources: ['org.openecomp.resource.vl.extVL'],
        uniqueId: 'capability.ddf1301e-866b-4fa3-bc4f-edbd81e532cd.virtual_linkable',
        maxOccurrences: 'UNBOUNDED',
        minOccurrences: '1'
    });

    const nodeCompute = Mock.of<CompositionCiNodeVf>({
        name: 'Compute 0',
        componentInstance: Mock.of<ComponentInstance>({
            componentName: 'Compute',
            uniqueId : 'compute0',
            requirements: Mock.of<RequirementsGroup>({
                'tosca.capabilities.Node' : [ dependencyReq ],
                'tosca.capabilities.Attachment' : [ storeAttachmentReq ]
            }),
            capabilities: Mock.of<CapabilitiesGroup>({
                'tosca.capabilities.network.Bindable' : [ bindingCap ],
                'tosca.capabilities.Node' : [ featureCap ]
            })
        })
    });

    const nodeBlockStorage = Mock.of<CompositionCiNodeVf>({
        name: 'BlockStorage 0',
        componentInstance: Mock.of<ComponentInstance>({
            componentName: 'BlockStorage',
            uniqueId : 'blockstorage0',
            requirements: Mock.of<RequirementsGroup>({
                'tosca.capabilities.Node' : [ dependencyReq ]
            }),
            capabilities: Mock.of<CapabilitiesGroup>({
                'tosca.capabilities.Attachment' : [ blockStoreAttachmentCap ],
                'tosca.capabilities.Node' : [ featureCap ]
            })
        })
    });

    const nodeVl = Mock.of<CompositionCiNodeVl>({
        name: 'ExtVL 0',
        componentInstance: Mock.of<ComponentInstance>({
            componentName: 'BlockStorage',
            uniqueId : 'extvl0',
            requirements: Mock.of<RequirementsGroup>({
                'tosca.capabilities.Node' : [ dependencyReq ]
            }),
            capabilities: Mock.of<CapabilitiesGroup>({
                'tosca.capabilities.network.Linkable' : [ linkableCap ],
                'tosca.capabilities.Node' : [ featureCap ]
            })
        })
    });

    const nodeCp = Mock.of<CompositionCiNodeCp>({
        name: 'ExtCP 0',
        componentInstance: Mock.of<ComponentInstance>({
            componentName: 'ExtCP',
            uniqueId : 'extcp0',
            requirements: Mock.of<RequirementsGroup>({
                'tosca.capabilities.network.Linkable' : [ virtualLinkReq ],
                'tosca.capabilities.network.Bindable' : [ bindableReq ]
            }),
            capabilities: Mock.of<CapabilitiesGroup>({
                'tosca.capabilities.Node' : [ featureCap ]
            })
        })
    });

    let service: MatchCapabilitiesRequirementsUtils;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [MatchCapabilitiesRequirementsUtils]
        });

        service = TestBed.get(MatchCapabilitiesRequirementsUtils);
    });

    it('match capability requirements utils should be defined', () => {
        console.log(JSON.stringify(service));
        expect(service).toBeDefined();
    });

    describe('isMatch function ', () => {

        it('capability type not equal to requirement capability, match is false', () => {
            const requirement = Mock.of<Requirement>({capability: 'tosca.capabilities.network.Linkable11'});
            const capability = Mock.of<Capability>({type: 'tosca.capabilities.network.Linkable'});
            expect(service.isMatch(requirement, capability)).toBeFalsy();
        });

        it('capability type equal to requirement capability and requirement node not exist, match is true', () => {
            const requirement = Mock.of<Requirement>({capability: 'tosca.capabilities.network.Linkable'});
            const capability = Mock.of<Capability>({type: 'tosca.capabilities.network.Linkable'});
            expect(service.isMatch(requirement, capability)).toBeTruthy();
        });

        it('is match - capability type equal to requirement capability and requirement node exist and includes in capability sources, match is true', () => {
            const requirement = Mock.of<Requirement>({capability: 'tosca.capabilities.network.Linkable', node: 'node1'});
            const capability = Mock.of<Capability>({
                type: 'tosca.capabilities.network.Linkable',
                capabilitySources: ['node1', 'node2', 'node3']
            });
            expect(service.isMatch(requirement, capability)).toBeTruthy();
        });

        it('no match - capability type equal to requirement capability and requirement node but not includes in capability sources, match is false', () => {
            const requirement = Mock.of<Requirement>({capability: 'tosca.capabilities.network.Linkable', node: 'node4'});
            const capability = Mock.of<Capability>({
                type: 'tosca.capabilities.network.Linkable',
                capabilitySources: ['node1', 'node2', 'node3']
            });
            expect(service.isMatch(requirement, capability)).toBeFalsy();
        });
    });

    describe('hasUnfulfilledRequirementContainingMatch function ', () => {

        it('node have no componentInstance, return false', () => {
            const node = Mock.of<CompositionCiNodeVf>({componentInstance: undefined});
            expect(service.hasUnfulfilledRequirementContainingMatch(node, [], {}, [])).toBeFalsy();
        });

        it('node have componentInstance data but no unfulfilled requirements, return false', () => {
            const node = Mock.of<CompositionCiNodeVf>({componentInstance: Mock.of<ComponentInstance>()});
            jest.spyOn(service, 'getUnfulfilledRequirements').mockReturnValue([]);
            expect(service.hasUnfulfilledRequirementContainingMatch(node, [], {}, [])).toBeFalsy();
        });

        it('node have componentInstance data and unfulfilled requirements but no match found, return false', () => {
            const node = Mock.of<CompositionCiNodeVf>({componentInstance: Mock.of<ComponentInstance>()});
            jest.spyOn(service, 'getUnfulfilledRequirements').mockReturnValue([Mock.of<Requirement>(), Mock.of<Requirement>()]);
            jest.spyOn(service, 'containsMatch').mockReturnValue(false);
            expect(service.hasUnfulfilledRequirementContainingMatch(node, [], {}, [])).toBeFalsy();
        });

        it('node have componentInstance data with unfulfilled requirements and match found, return true', () => {
            const node = Mock.of<CompositionCiNodeVf>({componentInstance: Mock.of<ComponentInstance>()});
            jest.spyOn(service, 'getUnfulfilledRequirements').mockReturnValue([Mock.of<Requirement>(), Mock.of<Requirement>()]);
            jest.spyOn(service, 'containsMatch').mockReturnValue(true);
            expect(service.hasUnfulfilledRequirementContainingMatch(node, [], {}, [])).toBeTruthy();
        });
    });

    describe('getMatches function ', () => {
        let fromId: string;
        let toId: string;

        beforeEach(() => {
            fromId = 'from_id';
            toId = 'to_id';
        });

        it('node have no unfulfilled requirements, return empty match array', () => {
            jest.spyOn(service, 'getUnfulfilledRequirements').mockReturnValue([]);
            expect(service.getMatches({}, {}, [], fromId, toId, true)).toHaveLength(0);
        });

        it('node have unfulfilled requirements but no capabilities, return empty match array', () => {
            jest.spyOn(service, 'getUnfulfilledRequirements').mockReturnValue([Mock.of<Requirement>(), Mock.of<Requirement>()]);
            expect(service.getMatches({}, {}, [], fromId, toId, true)).toHaveLength(0);
        });

        it('node have unfulfilled requirements and capabilities but no match found, return empty match array', () => {
            jest.spyOn(service, 'getUnfulfilledRequirements').mockReturnValue([Mock.of<Requirement>(), Mock.of<Requirement>()]);
            jest.spyOn(service, 'isMatch').mockReturnValue(false);
            expect(service.getMatches({}, {}, [], fromId, toId, true)).toHaveLength(0);
        });

        it('node have 2 unfulfilled requirements and 2 capabilities and match found, return 4 matches', () => {
            jest.spyOn(service, 'getUnfulfilledRequirements').mockReturnValue([Mock.of<Requirement>(), Mock.of<Requirement>()]);
            const capabilities = {aaa: Mock.of<Capability>(), bbb: Mock.of<Capability>()};
            jest.spyOn(service, 'isMatch').mockReturnValue(true);
            expect(service.getMatches({}, capabilities, [], fromId, toId, true)).toHaveLength(4);
        });
    });

    describe('Find matching nodes ===>', () => {

       it('should find matching nodes with component instance', () => {
           const nodes = [ nodeBlockStorage, nodeCompute, nodeVl ];
           let matchingNodes: any;

           // Compute can connect to Block Store
           matchingNodes = service.findMatchingNodesToComponentInstance(nodeCompute.componentInstance, nodes, []);
           expect(matchingNodes).toHaveLength(1);
           expect(matchingNodes).toContain(nodeBlockStorage);

           // Block Storage can connect to Compute
           matchingNodes = service.findMatchingNodesToComponentInstance(nodeBlockStorage.componentInstance, nodes, []);
           expect(matchingNodes).toHaveLength(1);
           expect(matchingNodes).toContain(nodeCompute);

           // Vl has no matches
           matchingNodes = service.findMatchingNodesToComponentInstance(nodeVl.componentInstance, nodes, []);
           expect(matchingNodes).toHaveLength(0);

           // CP should be able to connect to VL and Compute
           matchingNodes = service.findMatchingNodesToComponentInstance(nodeCp.componentInstance, nodes, []);
           expect(matchingNodes).toHaveLength(2);
           expect(matchingNodes).toContain(nodeCompute);
           expect(matchingNodes).toContain(nodeVl);
       });

       it('try with empty list of nodes', () => {
            const nodes = [ ];
            let matchingNodes: any;

            // Compute can connect to Block Store
            matchingNodes = service.findMatchingNodesToComponentInstance(nodeCompute.componentInstance, nodes, []);
            expect(matchingNodes).toHaveLength(0);
        });

       it('should detect fulfilled connection with compute node', () => {
            const nodes = [ nodeBlockStorage, nodeCompute, nodeVl ];
            let matchingNodes: any;
            const link = {
                relation: {
                    fromNode: 'extcp0',
                    toNode: 'compute0',
                    relationships: [{
                        relation: {
                            requirementOwnerId: 'extcp0',
                            requirement: 'virtualBinding',
                            relationship: {
                                type: 'tosca.relationships.network.BindsTo'
                            }

                        }
                    }]
                }
            };

            const links = [link];
            // CP should be able to connect to VL only since it already has a link with compute
            matchingNodes = service.findMatchingNodesToComponentInstance(nodeCp.componentInstance, nodes, links as CompositionCiLinkBase[]);
            expect(matchingNodes).toHaveLength(1);
            expect(matchingNodes).toContain(nodeVl);
        });
    });
});
