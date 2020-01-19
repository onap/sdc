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
import { ArtifactGroupType, ResourceType } from '../../../../utils/constants';
import { WorkspaceState } from '../../../store/states/workspace.state';
import { CompositionPanelComponent } from './composition-panel.component';
import { ArtifactsTabComponent } from './panel-tabs/artifacts-tab/artifacts-tab.component';
import { GroupMembersTabComponent } from './panel-tabs/group-members-tab/group-members-tab.component';
import { GroupOrPolicyPropertiesTab } from './panel-tabs/group-or-policy-properties-tab/group-or-policy-properties-tab.component';
import { InfoTabComponent } from './panel-tabs/info-tab/info-tab.component';
import { PolicyTargetsTabComponent } from './panel-tabs/policy-targets-tab/policy-targets-tab.component';
import { PropertiesTabComponent } from './panel-tabs/properties-tab/properties-tab.component';
import { ReqAndCapabilitiesTabComponent } from './panel-tabs/req-capabilities-tab/req-capabilities-tab.component';

describe('composition-panel component', () => {

    let fixture: ComponentFixture<CompositionPanelComponent>;
    let store: Store;

    const tabs = {
            infoTab : {titleIcon: 'info-circle', component: InfoTabComponent, input: {}, isActive: true, tooltipText: 'Information'},
            policyProperties: {
                titleIcon: 'settings-o', component: GroupOrPolicyPropertiesTab, input: {type: 'policy'}, isActive: false, tooltipText: 'Properties'
            },
            policyTargets: {titleIcon: 'inputs-o', component: PolicyTargetsTabComponent, input: {}, isActive: false, tooltipText: 'Targets'},
            groupMembers: {titleIcon: 'inputs-o', component: GroupMembersTabComponent, input: {}, isActive: false, tooltipText: 'Members'},
            groupProperties: {
                titleIcon: 'settings-o', component: GroupOrPolicyPropertiesTab, input: {type: 'group'}, isActive: false, tooltipText: 'Properties'
            },
            deploymentArtifacts: {
                titleIcon: 'deployment-artifacts-o', component: ArtifactsTabComponent,
                input: { type: ArtifactGroupType.DEPLOYMENT}, isActive: false, tooltipText: 'Deployment Artifacts'
            },
            apiArtifacts: {
                titleIcon: 'api-o', component: ArtifactsTabComponent,
                input: { type:  ArtifactGroupType.SERVICE_API}, isActive: false, tooltipText: 'API Artifacts'
            },
            infoArtifacts: {
                titleIcon: 'info-square-o', component: ArtifactsTabComponent,
                input: { type: ArtifactGroupType.INFORMATION}, isActive: false, tooltipText: 'Information Artifacts'
            },
            properties: {
                titleIcon: 'settings-o', component: PropertiesTabComponent,
                input: {title: 'Properties and Attributes'}, isActive: false, tooltipText: 'Properties'
            },
            reqAndCapabilities : {
                titleIcon: 'req-capabilities-o', component: ReqAndCapabilitiesTabComponent, input: {},
                isActive: false, tooltipText: 'Requirements and Capabilities'
            },
            inputs: {titleIcon: 'inputs-o', component: PropertiesTabComponent, input: {title: 'Inputs'}, isActive: false, tooltipText: 'Inputs'},
            settings: {titleIcon: 'settings-o', component: PropertiesTabComponent, input: {}, isActive: false, tooltipText: 'Settings'},
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

    it('When Topology Template is Service and no instance is selected Expect (info, deployment, inputs, info and api)', () => {

        const selectedComponent: Service = new Service(null, null);
        selectedComponent.isResource = jest.fn(() => false);
        selectedComponent.isService = jest.fn(() => true );

        fixture.componentInstance.store.select = jest.fn(() => Observable.of(selectedComponent));

        // const pnfMock = Mock.of<Service>({ isResource : () => false });
        fixture.componentInstance.topologyTemplate = selectedComponent;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect (fixture.componentInstance.tabs.length).toBe(5);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.deploymentArtifacts);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.inputs);
        expect (fixture.componentInstance.tabs[3]).toEqual(tabs.infoArtifacts);
        expect (fixture.componentInstance.tabs[4]).toEqual(tabs.apiArtifacts);

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

        fixture.componentInstance.store.select = jest.fn(() => Observable.of(selectedComponent));
        fixture.componentInstance.selectedComponentIsServiceProxyInstance = jest.fn(() => true);

        // const pnfMock = Mock.of<Service>({ isResource : () => false });
        fixture.componentInstance.topologyTemplate = selectedComponent;

        // Call ngOnInit
        fixture.componentInstance.ngOnInit();

        // Expect that
        expect (fixture.componentInstance.tabs.length).toBe(5);
        expect (fixture.componentInstance.tabs[0]).toEqual(tabs.infoTab);
        expect (fixture.componentInstance.tabs[1]).toEqual(tabs.properties);
        expect (fixture.componentInstance.tabs[2]).toEqual(tabs.reqAndCapabilities);

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
        expect (fixture.componentInstance.tabs.length).toBe(5);
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
