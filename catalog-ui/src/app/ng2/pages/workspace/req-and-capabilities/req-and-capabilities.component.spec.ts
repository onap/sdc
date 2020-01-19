import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import { NO_ERRORS_SCHEMA} from "@angular/core";
import {ConfigureFn, configureTests} from "../../../../../jest/test-config.helper";

import {Observable} from "rxjs/Observable";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {SdcUiServices, SdcUiCommon} from "onap-ui-angular";
import 'rxjs/add/observable/of';
import {ReqAndCapabilitiesComponent} from "./req-and-capabilities.component";
import {ReqAndCapabilitiesService} from "./req-and-capabilities.service";
import {WorkspaceService} from "../workspace.service";
import {
    capabilitiesMock,
    filterRequirmentsMock,
    requirementMock
} from "../../../../../jest/mocks/req-and-capabilities.mock";
import {ComponentMetadata} from "../../../../models/component-metadata";
import { TopologyTemplateService } from "../../../services/component-services/topology-template.service";
import {EventListenerService} from "../../../../services/event-listener-service";

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
});
