import {TestBed} from "@angular/core/testing";
import {WorkspaceService} from "../../../../pages/workspace/workspace.service";
import { ConnectionWizardService } from "app/ng2/pages/composition/graph/connection-wizard/connection-wizard.service";
import { ConnectRelationModel, Match, Requirement, Capability } from "app/models";
import { Mock } from "ts-mockery/dist";

describe('Connection Wizard Service', () => {

    let service: ConnectionWizardService;

    const connectRelationModelMock = Mock.of<ConnectRelationModel>({
        possibleRelations: [
            Mock.of<Match>({isFromTo: true, requirement: Mock.of<Requirement>({uniqueId: 'requirement1', capability: "cap1"}), capability: Mock.of<Capability>({uniqueId: 'capability1', type: 'othertype'})}),
            Mock.of<Match>({isFromTo: true, requirement: Mock.of<Requirement>({uniqueId: 'requirement2', capability: "cap1"}), capability: Mock.of<Capability>({uniqueId: 'capability2', type: 'tosca'})}),
            Mock.of<Match>({isFromTo: true, requirement: Mock.of<Requirement>({uniqueId: 'requirement3', capability: "cap1"}), capability: Mock.of<Capability>({uniqueId: 'capability3', type: 'tosca'})}),
            Mock.of<Match>({isFromTo: true, requirement: Mock.of<Requirement>({uniqueId: 'requirement4', capability: "cap1"}), capability: Mock.of<Capability>({uniqueId: 'capability2', type: 'tosca'})}),
            Mock.of<Match>({isFromTo: true, requirement: Mock.of<Requirement>({uniqueId: 'requirement5', capability: "cap2"}), capability: Mock.of<Capability>({uniqueId: 'capability1', type: 'tosca'})}),
            Mock.of<Match>({isFromTo: false, requirement: Mock.of<Requirement>({uniqueId: 'requirement6', capability: "cap2"}), capability: Mock.of<Capability>({uniqueId: 'capability2', type: 'tosca'})}),
            Mock.of<Match>({isFromTo: false, requirement: Mock.of<Requirement>({uniqueId: 'requirement7', capability: "cap2"}), capability: Mock.of<Capability>({uniqueId: 'capability1', type: 'othertype'})})
        ]
    });

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [ConnectionWizardService,
				{provide: WorkspaceService, useValue: {}}
            ]
        });

        service = TestBed.get(ConnectionWizardService);
        service.connectRelationModel = connectRelationModelMock;
    });

    describe('getOptionalRequirementsByInstanceUniqueId', ()  => {
        it('if no capability to match is sent in and isFromTo is true, ALL isFromTo==true requirements are returned', () => {
            const requirements = service.getOptionalRequirementsByInstanceUniqueId(true);
            expect(requirements['cap1'].length).toBe(4);
            expect(requirements['cap2'].length).toBe(1);
        });

        it('if no capability to match is sent in and isFromTo is false, ALL isFromTo==false requirements are returned', () => {
            const requirements = service.getOptionalRequirementsByInstanceUniqueId(false);
            expect(requirements['cap1']).toBeUndefined();
            expect(requirements['cap2'].length).toBe(2);
        });

        it('if capability to match IS sent in and isFromTo is true, matches with the same uniqueID and isFromTo==true are returned', () => {
            const capability = Mock.of<Capability>({uniqueId: 'capability1'});
            const requirements = service.getOptionalRequirementsByInstanceUniqueId(true, capability);
            expect(requirements['cap1'].length).toBe(1);
            expect(requirements['cap2'].length).toBe(1);
        });

        it('if capability to match IS sent in and isFromTo is false, requirements with the same uniqueID and isFromTo==false are returned', () => {
            const capability = Mock.of<Capability>({uniqueId: 'capability1'});
            const requirements = service.getOptionalRequirementsByInstanceUniqueId(false, capability);
            expect(requirements['cap1']).toBeUndefined();
            expect(requirements['cap2'].length).toBe(1);
        });
    })

    describe('getOptionalCapabilitiesByInstanceUniqueId', ()  => {
        it('if requirement to match IS sent in and isFromTo is true, matches with the same uniqueID and isFromTo==true are returned', () => {
            const requirement = Mock.of<Requirement>({uniqueId: 'requirement1'});
            const capabilities = service.getOptionalCapabilitiesByInstanceUniqueId(true, requirement);
            expect(capabilities['othertype'].length).toBe(1);
            expect(capabilities['tosca']).toBeUndefined();
        });

        it('if no requirement to match is sent in and isFromTo is true, a UNIQUE list of all capabilities with isFromTo==true are returned', () => {
            const capabilities = service.getOptionalCapabilitiesByInstanceUniqueId(true);
            expect(capabilities['othertype'].length).toBe(1);
            expect(capabilities['tosca'].length).toBe(2);
        });

        it('if no requirement to match is sent in and isFromTo is false, all capabilities with isFromTo==false are returned', () => {
            const capabilities = service.getOptionalCapabilitiesByInstanceUniqueId(false);
            expect(capabilities['othertype'].length).toBe(1);
            expect(capabilities['tosca'].length).toBe(1);
        });
    });

});

