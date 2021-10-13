import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { Observable } from 'rxjs';
import { Mock } from 'ts-mockery';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { Service } from '../../../../models/components/service';
import { Resource } from '../../../../models/components/resource';
import { GroupInstance } from '../../../../models/graph/zones/group-instance';
import { PolicyInstance } from '../../../../models/graph/zones/policy-instance';
import { ArtifactGroupType } from '../../../../utils/constants';
import { WorkspaceState } from '../../../store/states/workspace.state';
import { CompositionPanelComponent } from './composition-panel.component';
import { ArtifactsTabComponent } from './panel-tabs/artifacts-tab/artifacts-tab.component';
import { GroupMembersTabComponent } from './panel-tabs/group-members-tab/group-members-tab.component';
import { GroupOrPolicyPropertiesTab } from './panel-tabs/group-or-policy-properties-tab/group-or-policy-properties-tab.component';
import { InfoTabComponent } from './panel-tabs/info-tab/info-tab.component';
import { PolicyTargetsTabComponent } from './panel-tabs/policy-targets-tab/policy-targets-tab.component';
import { PropertiesTabComponent } from './panel-tabs/properties-tab/properties-tab.component';
import { ReqAndCapabilitiesTabComponent } from './panel-tabs/req-capabilities-tab/req-capabilities-tab.component';
import {SubstitutionFilterTabComponent} from "./panel-tabs/substitution-filter-tab/substitution-filter-tab.component";
import {InterfaceOperationsComponent} from "../interface-operatons/interface-operations.component";

describe('composition-panel component', () => {

    let fixture: ComponentFixture<CompositionPanelComponent>;
    let store: Store;

    const tabs = {
        infoTab: {
            titleIcon: 'info-circle',
            component: InfoTabComponent,
            input: {},
            isActive: true,
            tooltipText: 'Information',
            testId: 'detail-tab-information'
        },
        policyProperties: {
            titleIcon: 'settings-o',
            component: GroupOrPolicyPropertiesTab,
            input: {type: 'policy'},
            isActive: false,
            tooltipText: 'Properties',
            testId: 'detail-tab-policy-properties'
        },
        policyTargets: {
            titleIcon: 'inputs-o',
            component: PolicyTargetsTabComponent,
            input: {},
            isActive: false,
            tooltipText: 'Targets',
            testId: 'detail-tab-policy-targets'
        },
        groupMembers: {
            titleIcon: 'inputs-o',
            component: GroupMembersTabComponent,
            input: {},
            isActive: false,
            tooltipText: 'Members',
            testId: 'detail-tab-group-members'
        },
        groupProperties: {
            titleIcon: 'settings-o',
            component: GroupOrPolicyPropertiesTab,
            input: {type: 'group'},
            isActive: false,
            tooltipText: 'Properties',
            testId: 'detail-tab-group-properties'
        },
        deploymentArtifacts: {
            titleIcon: 'deployment-artifacts-o',
            component: ArtifactsTabComponent,
            input: {type: ArtifactGroupType.DEPLOYMENT},
            isActive: false,
            tooltipText: 'Deployment Artifacts',
            testId: 'detail-tab-deployment-artifacts'
        },
        apiArtifacts: {
            titleIcon: 'api-o',
            component: ArtifactsTabComponent,
            input: {type: ArtifactGroupType.SERVICE_API},
            isActive: false,
            tooltipText: 'API Artifacts',
            testId: 'detail-tab-api-artifacts'
        },
        infoArtifacts: {
            titleIcon: 'info-square-o',
            component: ArtifactsTabComponent,
            input: {type: ArtifactGroupType.INFORMATION},
            isActive: false,
            tooltipText: 'Information Artifacts',
            testId: 'detail-tab-information-artifacts'
        },
        properties: {
            titleIcon: 'settings-o', component: PropertiesTabComponent,
            input: {title: 'Properties and Attributes'}, isActive: false, tooltipText: 'Properties',
            testId: 'detail-tab-properties-attributes'
        },
        reqAndCapabilities: {
            titleIcon: 'req-capabilities-o', component: ReqAndCapabilitiesTabComponent, input: {},
            isActive: false, tooltipText: 'Requirements and Capabilities',
            testId: 'detail-tab-requirements-capabilities'
        },
        substitutionFilter: {
            titleIcon: 'composition-o',
            component: SubstitutionFilterTabComponent,
            input: {title: 'SUBSTITUTION FILTER'},
            isActive: false,
            tooltipText: 'Substitution Filter',
            testId: 'detail-tab-substitution-filter'
        },
        inputs: {
            titleIcon: 'inputs-o',
            component: PropertiesTabComponent,
            input: {title: 'Inputs'},
            isActive: false,
            tooltipText: 'Inputs',
            testId: 'detail-tab-inputs'
        },
        settings: {
            titleIcon: 'settings-o',
            component: PropertiesTabComponent,
            input: {},
            isActive: false,
            tooltipText: 'Settings',
            testId: 'detail-tab-settings'
        },
        interfaceOperations: {
            titleIcon: 'composition-o',
            component: InterfaceOperationsComponent,
            input: {title: 'Interface Operations'},
            isActive: false,
            tooltipText: 'Interface Operations',
            testId: 'detail-tab-interface-operations'
        }
    };

    beforeEach(
        async(() => {

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [CompositionPanelComponent],
                    imports: [NgxsModule.forRoot([WorkspaceState])],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(CompositionPanelComponent);
                store = testBed.get(Store);
            });
        })
    );

    it('When PolicyInstance Selected => Expect (info, policyTargets and policyProperties) tabs appear', () => {

        const testInstance = new PolicyInstance();

        fixture.componentInstance.initTabs(testInstance);
        expect (fixture.componentInstance.tabs.length).toBe(3);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.policyTargets);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.policyProperties);
    });

    it('should match current snapshot of composition-panel component.', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('When Topology Template is Service and no instance is selected Expect tabs info, deployment, inputs, info, api, substitution filter', () => {
        const selectedComponent: Service = new Service(null, null);
        selectedComponent.isResource = jest.fn(() => false);
        selectedComponent.isService = jest.fn(() => true);
        selectedComponent.isSubstituteCandidate = jest.fn(() => true);
        fixture.componentInstance.store.select = jest.fn(() => Observable.of(selectedComponent));

        fixture.componentInstance.topologyTemplate = selectedComponent;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect(fixture.componentInstance.tabs.length).toBe(6);
        expect(fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect(fixture.componentInstance.tabs[1]).toEqual(tabs.deploymentArtifacts);
        expect(fixture.componentInstance.tabs[2]).toEqual(tabs.inputs);
        expect(fixture.componentInstance.tabs[3]).toEqual(tabs.infoArtifacts);
        expect(fixture.componentInstance.tabs[4]).toEqual(tabs.apiArtifacts);
        expect(fixture.componentInstance.tabs[5]).toEqual(tabs.substitutionFilter);

    });

    it('When Topology Template is Service without base type, and no instance is selected. Expect tabs info, deployment, inputs, info and api', () => {

        const selectedComponent: Service = new Service(null, null);
        selectedComponent.isResource = jest.fn(() => false);
        selectedComponent.isService = jest.fn(() => true);
        selectedComponent.isSubstituteCandidate = jest.fn(() => false);
        fixture.componentInstance.store.select = jest.fn(() => Observable.of(selectedComponent));

        fixture.componentInstance.topologyTemplate = selectedComponent;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect(fixture.componentInstance.tabs.length).toBe(5);
        expect(fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect(fixture.componentInstance.tabs[1]).toEqual(tabs.deploymentArtifacts);
        expect(fixture.componentInstance.tabs[2]).toEqual(tabs.inputs);
        expect(fixture.componentInstance.tabs[3]).toEqual(tabs.infoArtifacts);
        expect(fixture.componentInstance.tabs[4]).toEqual(tabs.apiArtifacts);

    });

    it('When Topology Template is Resource and no instance is selected Expect (info, deployment, inputs, info and api)', () => {

        const selectedComponent: Service = new Service(null, null);
        selectedComponent.isResource = jest.fn(() => true);
        selectedComponent.isService = jest.fn(() => false );

        fixture.componentInstance.store.select = jest.fn(() => Observable.of(selectedComponent));

        fixture.componentInstance.topologyTemplate = selectedComponent;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect (fixture.componentInstance.tabs.length).toBe(5);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.deploymentArtifacts);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.properties);
        expect (fixture.componentInstance.tabs[3]).toEqual(tabs.infoArtifacts);
        expect (fixture.componentInstance.tabs[4]).toEqual(tabs.reqAndCapabilities);

    });

    it('When Topology Template is Service and proxyService instance is selected ' +
        'Expect (info, deployment, inputs, info and api)', () => {

        const selectedComponent: Service = new Service(null, null);
        selectedComponent.isResource = jest.fn(() => false);
        selectedComponent.isService = jest.fn(() => true );
        selectedComponent.isSubstituteCandidate = jest.fn(() => true );

        fixture.componentInstance.store.select = jest.fn(() => Observable.of(selectedComponent));
        fixture.componentInstance.selectedComponentIsServiceProxyInstance = jest.fn(() => true);

        fixture.componentInstance.topologyTemplate = selectedComponent;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect (fixture.componentInstance.tabs.length).toBe(7);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.properties);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.reqAndCapabilities);
        expect (fixture.componentInstance.tabs[6]).toEqual(tabs.interfaceOperations);

    });

    it('When Topology Template is Resource and VL is selected ' +
        'Expect (info, deployment, inputs, info and api)', () => {

        const topologyTemplate: Resource = new Resource(null, null);
        topologyTemplate.isResource = jest.fn(() => true);
        topologyTemplate.isService = jest.fn(() => false );

        const vlMock = Mock.of<Resource>({ resourceType : 'VL', isResource : () => true, isService : () => false });
        fixture.componentInstance.store.select = jest.fn(() => Observable.of(vlMock));

        fixture.componentInstance.topologyTemplate = topologyTemplate;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect (fixture.componentInstance.tabs.length).toBe(6);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.deploymentArtifacts);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.properties);
        expect (fixture.componentInstance.tabs[3]).toEqual(tabs.infoArtifacts);
        expect (fixture.componentInstance.tabs[4]).toEqual(tabs.reqAndCapabilities);

    });

    it('When Topology Template is Service and VL is selected ' +
        'Expect (info, deployment, inputs, info and api)', () => {

        const topologyTemplate: Service = new Service(null, null);
        topologyTemplate.isResource = jest.fn(() => true);
        topologyTemplate.isService = jest.fn(() => false );

        const vlMock = Mock.of<Resource>({ resourceType : 'VL', isResource : () => true, isService : () => false });
        fixture.componentInstance.store.select = jest.fn(() => Observable.of(vlMock));

        fixture.componentInstance.topologyTemplate = topologyTemplate;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect (fixture.componentInstance.tabs.length).toBe(5);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.deploymentArtifacts);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.properties);
        expect (fixture.componentInstance.tabs[3]).toEqual(tabs.infoArtifacts);
        expect (fixture.componentInstance.tabs[4]).toEqual(tabs.reqAndCapabilities);

    });

    it('When GroupInstance Selected => Expect (info, groupMembers and groupProperties) tabs appear.', () => {

        const testInstance = new GroupInstance();
        fixture.componentInstance.initTabs(testInstance);

        expect (fixture.componentInstance.tabs.length).toBe(3);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.groupMembers);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.groupProperties);
    });

});
