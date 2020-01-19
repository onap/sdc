import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {ToNodeStepComponent} from "./to-node-step.component";
import {ConnectionWizardService} from "../connection-wizard.service";
import {ConfigureFn, configureTests} from "../../../../../../../jest/test-config.helper";
import {Match} from "../../../../../../models/graph/match-relation";


describe('to-node-step component', () => {

    let fixture: ComponentFixture<ToNodeStepComponent>;
    let connectionWizardServiceMock: Partial<ConnectionWizardService>;

    beforeEach(
        async(() => {

            connectionWizardServiceMock = {
                // selectedMatch: new Match(null, null, true, '',''),
                selectedMatch: {
                    isFromTo: false
                },
                getOptionalRequirementsByInstanceUniqueId: jest.fn().mockReturnValue(5),
                getOptionalCapabilitiesByInstanceUniqueId: jest.fn().mockReturnValue(10)
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [ToNodeStepComponent],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: ConnectionWizardService, useValue: connectionWizardServiceMock}
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(ToNodeStepComponent);
            });
        })
    );


    it('should match current snapshot', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should test the ngOnInit with isFromTo = false', () => {
        const component = TestBed.createComponent(ToNodeStepComponent);
        let service = TestBed.get(ConnectionWizardService);
        service.selectedMatch.isFromTo = false;
        component.componentInstance.ngOnInit();
        expect(component.componentInstance.displayRequirementsOrCapabilities).toEqual("Requirement");
        expect(connectionWizardServiceMock.getOptionalRequirementsByInstanceUniqueId).toHaveBeenCalledWith(false, connectionWizardServiceMock.selectedMatch.capability);
        expect(component.componentInstance.optionalRequirementsMap).toEqual(5);
        expect(component.componentInstance.optionalCapabilitiesMap).toEqual({});
    });


    it('should test the ngOnInit with isFromTo = true', () => {
        const component = TestBed.createComponent(ToNodeStepComponent);
        let service = TestBed.get(ConnectionWizardService);
        service.selectedMatch.isFromTo = true;
        component.componentInstance.ngOnInit();
        expect(component.componentInstance.displayRequirementsOrCapabilities).toEqual("Capability");
        expect(connectionWizardServiceMock.getOptionalCapabilitiesByInstanceUniqueId).toHaveBeenCalledWith(true, connectionWizardServiceMock.selectedMatch.requirement);
        expect(component.componentInstance.optionalCapabilitiesMap).toEqual(10);
        expect(component.componentInstance.optionalRequirementsMap).toEqual({});
    });

});