import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { Observable } from 'rxjs/Rx';
import { Mock } from 'ts-mockery';
import { ConfigureFn, configureTests } from '../../../../../../../jest/test-config.helper';
import { ComponentMetadata } from '../../../../../../models/component-metadata';
import { GroupInstance } from '../../../../../../models/graph/zones/group-instance';
import { EventListenerService } from '../../../../../../services/event-listener-service';
import { GroupsService } from '../../../../../services/groups.service';
import { TranslateService } from '../../../../../shared/translator/translate.service';
import { WorkspaceService } from '../../../../workspace/workspace.service';
import { CompositionService } from '../../../composition.service';
import { GroupMembersTabComponent } from './group-members-tab.component';

describe('group members tab component', () => {

    let fixture: ComponentFixture<GroupMembersTabComponent>;

    // Mocks
    let workspaceServiceMock: Partial<WorkspaceService>;
    let eventsListenerServiceMock: Partial<EventListenerService>;
    let groupServiceMock: Partial<GroupsService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let compositionServiceMock: Partial<CompositionService>;
    let modalServiceMock: Partial<SdcUiServices.ModalService>;

    const membersToAdd = [
        {uniqueId: '1', name: 'inst1'},
        {uniqueId: '2', name: 'inst2'},
    ];

    beforeEach(
        async(() => {

            eventsListenerServiceMock = {};

            groupServiceMock = Mock.of<GroupsService>(
                {
                    updateMembers: jest.fn().mockImplementation((compType, uid, groupUniqueId, updatedMembers) => {
                            if (updatedMembers === undefined) {
                                return Observable.throwError('error');
                            } else {
                                return Observable.of(updatedMembers);
                            }
                        }
                    )});

            compositionServiceMock = {
                getComponentInstances: jest.fn().mockImplementation( () => {
                        return [{uniqueId: '1', name: 'inst1'},
                            {uniqueId: '2', name: 'inst2'},
                            {uniqueId: '3', name: 'inst3'},
                            {uniqueId: '4', name: 'inst4'},
                            {uniqueId: '5', name: 'inst5'}
                        ];
                    }
                )
            };

            workspaceServiceMock = {
                metadata: Mock.of<ComponentMetadata>()
            };

            const addMemberModalInstance = {
                innerModalContent: { instance: { existingElements: membersToAdd }},
                closeModal: jest.fn()
            };

            modalServiceMock = {
                openInfoModal: jest.fn(),
                openCustomModal: jest.fn().mockImplementation(() => addMemberModalInstance)
            };

            loaderServiceMock = {
                activate: jest.fn(),
                deactivate: jest.fn()
            };

            const groupInstanceMock = Mock.of<GroupInstance>();

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [GroupMembersTabComponent],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: TranslateService, useValue: { translate: jest.fn() }},
                        {provide: GroupsService, useValue: groupServiceMock},
                        {provide: SdcUiServices.ModalService, useValue: modalServiceMock },
                        {provide: EventListenerService, useValue: eventsListenerServiceMock },
                        {provide: CompositionService, useValue: compositionServiceMock },
                        {provide: WorkspaceService, useValue: workspaceServiceMock},
                        {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock }
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(GroupMembersTabComponent);
                fixture.componentInstance.group = groupInstanceMock;
            });
        })
    );

    it('test that initially all members are available for adding', () => {
        const testedComponent = fixture.componentInstance;

        // No members are currently in the group, all 5 members should be returned
        const optionalMembersToAdd = testedComponent.getOptionalsMembersToAdd();
        expect(optionalMembersToAdd).toHaveLength(5);
    });

    it('test list of available instances to add does not include existing members', () => {
        const testedComponent = fixture.componentInstance;

        // Mock the group instance to return the members that we are about to add
        testedComponent.group.getMembersAsUiObject = jest.fn().mockImplementation( () => membersToAdd);

        // The opened modal shall return 2 members to be added
        testedComponent.openAddMembersModal();
        testedComponent.addMembers(); // Shall add 2 members (1,2)

        // Now the getOptionalsMembersToAdd shall return 3 which are the members that were no added yet
        const optionalMembersToAdd = testedComponent.getOptionalsMembersToAdd();
        expect(optionalMembersToAdd).toHaveLength(3);
    });
});
