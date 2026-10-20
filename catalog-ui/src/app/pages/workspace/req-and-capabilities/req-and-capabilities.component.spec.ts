import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import { NO_ERRORS_SCHEMA} from "@angular/core";
import {ConfigureFn, configureTests} from "../../../../jest/test-config.helper";

import {Observable} from "rxjs/Observable";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {SdcUiServices, SdcUiCommon} from "onap-ui-angular";
import 'rxjs/add/observable/of';
import {ReqAndCapabilitiesComponent} from "./req-and-capabilities.component";
import {ReqAndCapabilitiesService} from "./req-and-capabilities.service";
import {CapabilitiesComponent} from "./capabilities/capabilities.component";
import {RequirementsComponent} from "./requirements/requirements.component";
import {CapabilitiesPropertiesComponent} from "./capabilities/capabilities-properties/capabilities-properties";
import {WorkspaceService} from "../workspace.service";
import {
    capabilitiesMock,
    filterRequirmentsMock,
    requirementMock
} from "../../../../jest/mocks/req-and-capabilities.mock";
import {ComponentMetadata} from "../../../models/component-metadata";
import { TopologyTemplateService } from "../../../services/component-services/topology-template.service";
import {EventListenerService} from "../../../services/event-listener.service";

describe('req and capabilities component', () => {

    let fixture: ComponentFixture<ReqAndCapabilitiesComponent>;
    let workspaceServiceMock: Partial<WorkspaceService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;
    let createDynamicComponentServiceMock: Partial<SdcUiServices.CreateDynamicComponentService>
    let reqAndCapabilitiesService: Partial<ReqAndCapabilitiesService>;
    let modalService: Partial<SdcUiServices.ModalService>;
    let eventListenerService: Partial<EventListenerService>;



    beforeEach(
        async(() => {

            workspaceServiceMock = {
               metadata: new ComponentMetadata()
            };

            topologyTemplateServiceMock = {
                getRequirementsAndCapabilitiesWithProperties: jest.fn().mockImplementation(() =>
                Observable.of({requirements: {'tosca.requirements.Node': requirementMock},
                capabilities: {'tosca.capabilities.Node': capabilitiesMock}}))
            };

            loaderServiceMock = {
                activate : jest.fn(),
                deactivate: jest.fn()
            }
            createDynamicComponentServiceMock = {
                insertComponentDynamically: jest.fn()
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [ReqAndCapabilitiesComponent],
                    imports: [NgxDatatableModule],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        { provide: WorkspaceService, useValue: workspaceServiceMock },
                        { provide: SdcUiServices.LoaderService, useValue: loaderServiceMock },
                        { provide: TopologyTemplateService, useValue: topologyTemplateServiceMock },
                        { provide: SdcUiServices.CreateDynamicComponentService, useValue: createDynamicComponentServiceMock },
                        { provide: ReqAndCapabilitiesService, useValue: reqAndCapabilitiesService },
                        { provide: SdcUiServices.ModalService, useValue: modalService },
                        { provide: EventListenerService, useValue: eventListenerService }
                    ],
                });
            };
            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(ReqAndCapabilitiesComponent);
            });
        })
    );

    it('should see exactly 2 requirement in requirements table when call initCapabilitiesAndRequirements and meta data requirements null', () => {
        workspaceServiceMock.metadata.requirements = null;
        fixture.componentInstance.initCapabilitiesAndRequirements();
        expect(workspaceServiceMock.metadata.requirements["tosca.requirements.Node"].length).toBe(3);
    });
    it('should see exactly 2 capabilities in capabilities table when call initCapabilitiesAndRequirements and meta data capabilities null', () => {
        workspaceServiceMock.metadata.capabilities = null;
        fixture.componentInstance.initCapabilitiesAndRequirements();
        expect(workspaceServiceMock.metadata.capabilities["tosca.capabilities.Node"].length).toBe(2);
    });

    it('capabilities array papulated when call populateReqOrCap with capabilities', () => {
        workspaceServiceMock.metadata.capabilities = {"tosca.capabilities.Node": capabilitiesMock, "tosca.capabilities.Scalable": capabilitiesMock};
        fixture.componentInstance.populateReqOrCap("capabilities");
        expect(fixture.componentInstance.capabilities.length).toBe(4);
    });

    it('create requirements component when call loadReqOrCap with true', () => {
        createDynamicComponentServiceMock.insertComponentDynamically.mockImplementation(() => { return {instance: {requirements: requirementMock}}});
        fixture.componentInstance.requirements = requirementMock;
        fixture.componentInstance.loadReqOrCap(true);
        expect(fixture.componentInstance.instanceRef.instance.requirements.length).toEqual(3);
    });

    it('create capabilities component when call loadReqOrCap with false', () => {
        fixture.componentInstance.instanceRef = {instance: {requirements: null}};
        createDynamicComponentServiceMock.insertComponentDynamically.mockImplementation(() => { return {instance: {capabilities: capabilitiesMock}}});
        fixture.componentInstance.capabilities = capabilitiesMock;
        fixture.componentInstance.requirementsUI = filterRequirmentsMock;
        let event = {
            target : {
                value : 'root'
            }
        }
        fixture.componentInstance.updateFilter(event);
        expect(fixture.componentInstance.instanceRef.instance.requirements.length).toBe(1);
    });

    it('should filter 1 capabilities when searching and call updateFilter function and instanceRef is capabilities component', () => {
        fixture.componentInstance.instanceRef = {instance: {capabilities: null}};
        fixture.componentInstance.capabilities = capabilitiesMock;
        fixture.componentInstance.selectTabName = 'CAPABILITIES';
        let event = {
            target : {
                value : '1source'
            }
        }
        fixture.componentInstance.updateFilter(event);
        expect(fixture.componentInstance.instanceRef.instance.capabilities[0].type).toBe("tosca.capabilities.Node");
    });

    it('loads the capabilities table when the Capabilities tab is selected', () => {
        fixture.componentInstance.instanceRef = null;
        createDynamicComponentServiceMock.insertComponentDynamically.mockClear();
        fixture.componentInstance.selectTab({title: 'Capabilities(2)'});
        expect(fixture.componentInstance.selectTabName).not.toBe('REQUIREMENTS');
        expect(createDynamicComponentServiceMock.insertComponentDynamically.mock.calls[0][0]).toBe(CapabilitiesComponent);
    });

    it('opens the add dialogs with Create as the primary button and Cancel as the secondary one', () => {
        const component = fixture.componentInstance as any;
        const openCustomModal = jest.fn().mockReturnValue(
            {innerModalContent: {instance: {onValidationChange: {subscribe: jest.fn()}}}});
        component.modalService = {openCustomModal};
        component.reqAndCapabilitiesService = {
            getCapabilityTypesList: jest.fn(), getRelationsShipeTypeList: jest.fn(), getNodeTypesList: jest.fn()
        };

        component.addRequirement();
        component.addCapability();

        openCustomModal.mock.calls.forEach(([config]) => {
            expect(config.buttons.map((b) => [b.text, b.type || SdcUiCommon.ButtonType.primary, b.size])).toEqual([
                ['Create', SdcUiCommon.ButtonType.primary, 'x-small'],
                ['Cancel', SdcUiCommon.ButtonType.secondary, 'x-small'],
            ]);
        });
        expect(openCustomModal).toHaveBeenCalledTimes(2);
    });
});

describe('req and capabilities template', () => {
    const template: string = require('./req-and-capabilities.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    it('renders Add Requirement and Add Capability as tertiary buttons', () => {
        const adds = Array.from(doc.querySelectorAll('sdc-button.req-and-cap-add'));
        expect(adds.map((el) => el.getAttribute('[text]'))).toEqual([`'Add Requirement'`, `'Add Capability'`]);
        expect(adds.map((el) => el.getAttribute('[testId]'))).toEqual([`'add-requirement-button'`, `'add-capability-button'`]);
        adds.forEach((el) => {
            expect(el.getAttribute('[type]')).toBe(`'tertiary'`);
            expect(el.getAttribute('[icon_name]')).toBe(`'plus'`);
        });
    });
});

describe('req and capabilities while the workspace is view-only', () => {
    const reqAndCapService = (viewOnly: boolean): any => ({
        isViewOnly: jest.fn(() => viewOnly),
        getCapabilityTypesList: jest.fn(), getRelationsShipeTypeList: jest.fn(), getNodeTypesList: jest.fn()
    });
    const modalService = (): any => ({
        openCustomModal: jest.fn(() => ({
            innerModalContent: {instance: {onValidationChange: {subscribe: jest.fn()}}},
            getButtonById: jest.fn()
        }))
    });

    it('offers Add only while the workspace is editable', () => {
        expect(new ReqAndCapabilitiesComponent(null, null, null, null, reqAndCapService(false), null, null).showAdd()).toBe(true);
        expect(new ReqAndCapabilitiesComponent(null, null, null, null, reqAndCapService(true), null, null).showAdd()).toBe(false);
    });

    [
        ['capability', (service, modal) => new CapabilitiesComponent(null, null, null, service, modal, null).editCapability(<any>{})],
        ['requirement', (service, modal) => new RequirementsComponent(null, null, null, service, modal, null).editRequirement({})]
    ].forEach(([kind, open]: [string, (service, modal) => void]) => {
        it(`opens a ${kind} for update while editable`, () => {
            const modal = modalService();
            open(reqAndCapService(false), modal);
            const [config, , {input}] = modal.openCustomModal.mock.calls[0];
            expect(config.buttons.map((b) => b.text)).toEqual(['Update', 'Cancel']);
            expect(input.isReadonly).toBe(false);
        });

        it(`opens a ${kind} read-only with only Close while view-only`, () => {
            const modal = modalService();
            open(reqAndCapService(true), modal);
            const [config, , {input}] = modal.openCustomModal.mock.calls[0];
            expect(config.buttons.map((b) => b.text)).toEqual(['Close']);
            expect(input.isReadonly).toBe(true);
        });
    });

    it('opens a capability property view-only while the workspace is view-only', () => {
        const modalsHandler: any = {openEditPropertyModal: jest.fn()};
        const properties = new CapabilitiesPropertiesComponent(modalsHandler, <any>{metadata: {uniqueId: 'vf-id'}}, reqAndCapService(true));
        (properties as any).updateProperty(<any>{name: 'p1'});
        expect(modalsHandler.openEditPropertyModal.mock.calls[0][6]).toBe(true);
    });
});
