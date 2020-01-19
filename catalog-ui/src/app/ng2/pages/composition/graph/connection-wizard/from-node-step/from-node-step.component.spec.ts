import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Capability, Match } from 'app/models';
import { ConfigureFn, configureTests } from '../../../../../../../jest/test-config.helper';
import { Requirement } from '../../../../../../models/requirement';
import { ConnectionWizardService } from '../connection-wizard.service';
import { FromNodeStepComponent } from './from-node-step.component';

describe('from-node-step component', () => {

    let fixture: ComponentFixture<FromNodeStepComponent>;
    let connectionWizardServiceMockWithoutSelectedMatch: Partial<ConnectionWizardService>;
    let connectionWizardServiceMockWithSelectedMatch: Partial<ConnectionWizardService>;

    const connectionWizardServiceMockSelectedMatchWithRequirements = {requirement: 'val'};

    connectionWizardServiceMockWithoutSelectedMatch = {
        getOptionalRequirementsByInstanceUniqueId: jest.fn().mockReturnValue(5),
        getOptionalCapabilitiesByInstanceUniqueId: jest.fn().mockReturnValue(10),

        connectRelationModel: {
            fromNode: {
                componentInstance: {
                    uniqueId : 'testUniqueID'
                }
            }
        }
    };

    connectionWizardServiceMockWithSelectedMatch = {
        selectedMatch: connectionWizardServiceMockSelectedMatchWithRequirements,
        getOptionalRequirementsByInstanceUniqueId: jest.fn().mockReturnValue(5),
        getOptionalCapabilitiesByInstanceUniqueId: jest.fn().mockReturnValue(10)
    };

    let expectedConnectionWizardServiceMock = connectionWizardServiceMockWithoutSelectedMatch;

    beforeEach(
        async(() => {
            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [FromNodeStepComponent],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: ConnectionWizardService, useValue: expectedConnectionWizardServiceMock}
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(FromNodeStepComponent);
            });
        })
    );


    it('should match current snapshot', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('preventBack return true - always', () => {
        fixture.componentInstance.ngOnInit();
        const result = fixture.componentInstance.preventBack();
        expect(result).toEqual(true);
    });

    it('preventNext return true since selectedMatch does not exist in connectionWizardServiceMock', () => {
        fixture.componentInstance.ngOnInit();
        const result = fixture.componentInstance.preventNext();
        expect(result).toEqual(true);
    });

    it('preventNext return false since to selectedMatch or selectedMatch.capability & selectedMatch.requirement does exist in connectionWizardServiceMock', () => {
        fixture.componentInstance.connectWizardService = connectionWizardServiceMockWithSelectedMatch;
        fixture.componentInstance.ngOnInit();
        const result = fixture.componentInstance.preventNext();
        expect(result).toEqual(false);
    });

    it('updateSelectedReqOrCap is called with instance of requirement, the selectMatch will be set to an Instance of Match of type Requirement', () => {
        const requirement = new Requirement();
        fixture.componentInstance.updateSelectedReqOrCap(requirement);
        const expectedSelectedMatch = fixture.componentInstance.connectWizardService.selectedMatch;

        expect(expectedSelectedMatch).toBeInstanceOf(Match);
        expect(expectedSelectedMatch.capability).toBe(null);
        expect(expectedSelectedMatch.fromNode).toBe('testUniqueID');
        expect(expectedSelectedMatch.isFromTo).toBe(true);
        expect(expectedSelectedMatch.toNode).toBe(null);
        expect(expectedSelectedMatch.requirement).toBeInstanceOf(Requirement);
    });

    it('updateSelectedReqOrCap is called with instance of capability, the selectMatch will be set to an Instance of Match of type Capability', () => {
        const capability = new Capability();
        fixture.componentInstance.updateSelectedReqOrCap(capability);
        const expectedSelectedMatch = fixture.componentInstance.connectWizardService.selectedMatch;

        expect(expectedSelectedMatch).toBeInstanceOf(Match);
        expect(expectedSelectedMatch.requirement).toBe(null);
        expect(expectedSelectedMatch.fromNode).toBe(null);
        expect(expectedSelectedMatch.isFromTo).toBe(false);
        expect(expectedSelectedMatch.toNode).toBe('testUniqueID');
        expect(expectedSelectedMatch.capability).toBeInstanceOf(Capability);
    });

    it('updateSelectedReqOrCap is called with null, the selectMatch will be set to null', () => {
        fixture.componentInstance.updateSelectedReqOrCap(null);
        const expectedSelectedMatch = fixture.componentInstance.connectWizardService.selectedMatch;

        expect(expectedSelectedMatch).toBe(null);
    });

});