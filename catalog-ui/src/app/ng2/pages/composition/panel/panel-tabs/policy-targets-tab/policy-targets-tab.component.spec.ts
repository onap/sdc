import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { Observable } from 'rxjs/Rx';
import { Mock } from 'ts-mockery';
import { ConfigureFn, configureTests } from '../../../../../../../jest/test-config.helper';
import { ComponentMetadata } from '../../../../../../models/component-metadata';
import { EventListenerService } from '../../../../../../services/event-listener-service';
import { TranslateService } from '../../../../../shared/translator/translate.service';
import { WorkspaceService } from '../../../../workspace/workspace.service';
import { CompositionService } from '../../../composition.service';
import { PolicyTargetsTabComponent } from "app/ng2/pages/composition/panel/panel-tabs/policy-targets-tab/policy-targets-tab.component";
import { PoliciesService } from "app/services-ng2";
import { PolicyInstance, GroupInstance } from "app/models";
import { NgxsModule } from "@ngxs/store";
import { GraphState } from "app/ng2/pages/composition/common/store/graph.state";
import { WorkspaceState } from "app/ng2/store/states/workspace.state";
import { TargetUiObject } from "app/models/ui-models/ui-target-object";
import { TargetOrMemberType } from "app/utils";




describe('policy targets tab component', () => {

    let fixture: ComponentFixture<PolicyTargetsTabComponent>;
    let component: PolicyTargetsTabComponent;

    let policiesServiceMock = Mock.of<PoliciesService>(
        {
            updateTargets: jest.fn().mockImplementation((compType, uid, policyUniqueId, updatedTargets) => {
                    if (updatedTargets === undefined) {
                        return Observable.throwError('error');
                    } else {
                        return Observable.of(updatedTargets);
                    }
                }
    )});

    let compositionServiceMock = {
        componentInstances: [{uniqueId: '1', name: 'inst1'},
                    {uniqueId: '2', name: 'inst2'},
                    {uniqueId: '3', name: 'inst3'},
                    {uniqueId: '4', name: 'inst4'},
                    {uniqueId: '5', name: 'inst5'}
        ],
        groupInstances : [
            Mock.of<GroupInstance>({uniqueId: "group1", name: "group1"}),
            Mock.of<GroupInstance>({uniqueId: "group2", name: "group2"}),
            Mock.of<GroupInstance>({uniqueId: "group3", name: "group3"})
        ]
    };

    let workspaceServiceMock = {
        metadata: Mock.of<ComponentMetadata>()
    };

    let modalServiceMock = {
        openInfoModal: jest.fn(),
        openCustomModal: jest.fn().mockImplementation(() => { return  {
            innerModalContent: { instance: { existingElements: targetsToAdd }},
            closeModal: jest.fn()
        }})
    };

    let loaderServiceMock = {
        activate: jest.fn(),
        deactivate: jest.fn()
    };

    const targetsToAdd = [
        <TargetUiObject>{uniqueId: '1', name: 'inst1', type: TargetOrMemberType.COMPONENT_INSTANCES},
        <TargetUiObject>{uniqueId: "group1", name: "group1", type: TargetOrMemberType.GROUPS}
    ];

    const policyInstanceMock = Mock.of<PolicyInstance>(
        { getTargetsAsUiObject: jest.fn().mockImplementation( () => targetsToAdd)
    });

    beforeEach(() => {
        TestBed.configureTestingModule({
                declarations: [PolicyTargetsTabComponent],
                imports: [NgxsModule.forRoot([WorkspaceState])],
                schemas: [NO_ERRORS_SCHEMA],
                providers: [
                    {provide: TranslateService, useValue: { translate: jest.fn() }},
                    {provide: PoliciesService, useValue: policiesServiceMock},
                    {provide: SdcUiServices.ModalService, useValue: modalServiceMock },
                    {provide: EventListenerService, useValue: {} },
                    {provide: CompositionService, useValue: compositionServiceMock },
                    {provide: WorkspaceService, useValue: workspaceServiceMock},
                    {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock }
                ],
        });
        
        fixture = TestBed.createComponent(PolicyTargetsTabComponent);
        component = fixture.componentInstance;
        component.policy = policyInstanceMock;
    });
    

    it('if there are no existing targets, all component instances AND all groups are available for adding', () => {
        component.targets = [];
        const optionalTargetsToAdd = component.getOptionalsTargetsToAdd();
        expect(optionalTargetsToAdd).toHaveLength(8);
    });

    it('list of available instances to add does not include existing targets', () => {
        component.targets = targetsToAdd;
        const optionalMembersToAdd = component.getOptionalsTargetsToAdd();
        expect(optionalMembersToAdd).toHaveLength(6);
    });
});
