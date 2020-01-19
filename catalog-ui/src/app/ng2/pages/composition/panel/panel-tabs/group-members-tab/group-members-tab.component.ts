/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import { Component, HostBinding, Input, OnDestroy, OnInit } from '@angular/core';
import { Select } from '@ngxs/store';
import { GroupInstance } from 'app/models/graph/zones/group-instance';
import { CompositionService } from 'app/ng2/pages/composition/composition.service';
import { WorkspaceService } from 'app/ng2/pages/workspace/workspace.service';
import { EventListenerService } from 'app/services/event-listener-service';
import { GRAPH_EVENTS } from 'app/utils';
import * as _ from 'lodash';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { Observable, Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ComponentInstance } from '../../../../../../models/componentsInstances/componentInstance';
import { MemberUiObject } from '../../../../../../models/ui-models/ui-member-object';
import { AddElementsComponent } from '../../../../../components/ui/modal/add-elements/add-elements.component';
import {GraphState} from "../../../common/store/graph.state";
import { GroupsService } from '../../../../../services/groups.service';
import { TranslateService } from '../../../../../shared/translator/translate.service';

@Component({
    selector: 'group-members-tab',
    templateUrl: './group-members-tab.component.html',
    styleUrls: ['./../policy-targets-tab/policy-targets-tab.component.less']
})

export class GroupMembersTabComponent implements OnInit, OnDestroy {

    @Input() group: GroupInstance;
    @Input() isViewOnly: boolean;
    @Select(GraphState.getSelectedComponent) group$: Observable<GroupInstance>;
    @HostBinding('class') classes = 'component-details-panel-tab-group-members';

    private members: MemberUiObject[];
    private addMemberModalInstance: SdcUiComponents.ModalComponent;
    private subscription: Subscription;

    constructor(
        private translateService: TranslateService,
        private groupsService: GroupsService,
        private modalService: SdcUiServices.ModalService,
        private eventListenerService: EventListenerService,
        private compositionService: CompositionService,
        private workspaceService: WorkspaceService,
        private loaderService: SdcUiServices.LoaderService
    ) {
    }

    ngOnInit() {
        this.subscription = this.group$.pipe(
            tap((group) => {
                this.group = group;
                this.members = this.group.getMembersAsUiObject(this.compositionService.componentInstances);
            })).subscribe();
    }

    ngOnDestroy() {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    deleteMember = (member: MemberUiObject): void => {
        this.loaderService.activate();
        this.groupsService.deleteGroupMember(
            this.workspaceService.metadata.componentType,
            this.workspaceService.metadata.uniqueId,
            this.group,
            member.uniqueId).subscribe(
            (updatedMembers: string[]) => {
                this.group.members = updatedMembers;
                this.initMembers();
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, this.group);
            },
            () => console.log('Error deleting member!'),
            () => this.loaderService.deactivate()
        );
    }

    addMembers = (): void => {
        // TODO refactor sdc-ui modal in order to return the data
        const membersToAdd: MemberUiObject[] = this.addMemberModalInstance.innerModalContent.instance.existingElements;
        if (membersToAdd.length > 0) {
            this.addMemberModalInstance.closeModal();
            this.loaderService.activate();
            const locallyUpdatedMembers: MemberUiObject[] = _.union(this.members, membersToAdd);
            this.groupsService.updateMembers(
                this.workspaceService.metadata.componentType,
                this.workspaceService.metadata.uniqueId,
                this.group.uniqueId,
                locallyUpdatedMembers).subscribe(
                (updatedMembers: string[]) => {
                    this.group.members = updatedMembers;
                    this.initMembers();
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, this.group);
                },
                () => {
                    console.log('Error updating members!');
                }, () =>
                this.loaderService.deactivate()
            );
        }
    }

    getOptionalsMembersToAdd(): MemberUiObject[] {
        const optionalsMembersToAdd: MemberUiObject[] = [];
        // adding all instances as optional members to add if not already exist
        _.forEach(this.compositionService.getComponentInstances(), (instance: ComponentInstance) => {
            if (!_.some(this.members, (member: MemberUiObject) => {
                    return member.uniqueId === instance.uniqueId;
                })) {
                optionalsMembersToAdd.push(new MemberUiObject(instance.uniqueId, instance.name));
            }
        });
        return optionalsMembersToAdd;
    }

    openAddMembersModal(): void {
        const addMembersModalConfig = {
            title: this.group.name + ' ADD MEMBERS',
            size: 'md',
            type: SdcUiCommon.ModalType.custom,
            testId: 'addMembersModal',
            buttons: [
                {text: 'ADD MEMBERS', size: 'medium', callback: this.addMembers, closeModal: false},
                {text: 'CANCEL', size: 'sm', type: 'secondary', closeModal: true}
            ]
        } as SdcUiCommon.IModalConfig;
        const optionalsMembersToAdd = this.getOptionalsMembersToAdd();
        this.addMemberModalInstance = this.modalService.openCustomModal(addMembersModalConfig, AddElementsComponent, {
            elementsToAdd: optionalsMembersToAdd,
            elementName: 'member'
        });
    }

    private initMembers = (groupInstance?: GroupInstance) => {
        this.group = groupInstance ? groupInstance : this.group;
        this.members = this.group.getMembersAsUiObject(this.compositionService.getComponentInstances());
    }
}
