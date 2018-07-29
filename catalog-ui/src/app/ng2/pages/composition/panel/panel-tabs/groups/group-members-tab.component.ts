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

import * as _ from "lodash";
import { Component, Input, Output, EventEmitter, OnChanges, HostBinding } from "@angular/core";
import { TranslateService } from './../../../../../shared/translator/translate.service';
import { Component as TopologyTemplate } from "app/models";
import { GroupInstance } from "app/models/graph/zones/group-instance";
import { GroupsService } from "../../../../../services/groups.service";
import { SimpleChanges } from "@angular/core/src/metadata/lifecycle_hooks";
import { MemberUiObject } from "../../../../../../models/ui-models/ui-member-object";
import { IModalConfig } from "sdc-ui/lib/angular/modals/models/modal-config";
import { AddElementsComponent } from "../../../../../components/ui/modal/add-elements/add-elements.component";
import { GRAPH_EVENTS } from 'app/utils';
import { EventListenerService } from 'app/services/event-listener-service';
import { ComponentInstance } from "../../../../../../models/componentsInstances/componentInstance";
import { SdcUiComponents } from "sdc-ui/lib/angular";

@Component({
    selector: 'group-members-tab',
    templateUrl: './group-members-tab.component.html',
    styleUrls: ['./../base/base-tab.component.less', 'group-members-tab.component.less']
})

export class GroupMembersTabComponent implements OnChanges {
    

    private members: Array<MemberUiObject>;

    @Input() group: GroupInstance;
    @Input() topologyTemplate: TopologyTemplate;
    @Input() isViewOnly: boolean;
    @Output() isLoading: EventEmitter<boolean> = new EventEmitter<boolean>();
    @HostBinding('class') classes = 'component-details-panel-tab-group-members';

    constructor(private translateService: TranslateService,
        private groupsService: GroupsService,
        private modalService: SdcUiComponents.ModalService,
        private eventListenerService: EventListenerService
    ) {
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, this.initMembers)
    }

    ngOnChanges(changes:SimpleChanges):void {
        this.initMembers();
    }

    deleteMember = (member: MemberUiObject):void => {
        this.isLoading.emit(true);
        this.groupsService.deleteGroupMember(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.group, member.uniqueId).subscribe(
            (updatedMembers:Array<string>) => {
                this.group.members = updatedMembers;
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, this.group);
            },
            error => console.log("Error deleting member!"),
            () => this.isLoading.emit(false)
        );
    }

    private initMembers = (groupInstance?: GroupInstance) => {
        this.group = groupInstance ? groupInstance : this.group;
        this.members = this.group.getMembersAsUiObject(this.topologyTemplate.componentInstances);
    }

    addMembers = ():void => {
        var membersToAdd:Array<MemberUiObject> = this.modalService.getCurrentInstance().innerModalContent.instance.existingElements; //TODO refactor sdc-ui modal in order to return the data
        if(membersToAdd.length > 0) {
            this.modalService.closeModal();
            this.isLoading.emit(true);
            var updatedMembers: Array<MemberUiObject> = _.union(this.members, membersToAdd);
            this.groupsService.updateMembers(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.group.uniqueId, updatedMembers).subscribe(
                (updatedMembers:Array<string>) => {
                    this.group.members = updatedMembers;
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, this.group);
                },
                error => {
                    console.log("Error updating members!");
                }, () =>
                    this.isLoading.emit(false)
            );
        }
    }

    getOptionalsMembersToAdd():Array<MemberUiObject> {
        
        let optionalsMembersToAdd:Array<MemberUiObject> = [];

        // adding all instances as optional members to add if not already exist
        _.forEach(this.topologyTemplate.componentInstances, (instance:ComponentInstance) => {
            if (!_.some(this.members, (member:MemberUiObject) => {
                    return member.uniqueId === instance.uniqueId
                })) {
                optionalsMembersToAdd.push(new MemberUiObject(instance.uniqueId, instance.name));
            }
        });
        return optionalsMembersToAdd;
    }

    openAddMembersModal():void {
        let addMembersModalConfig:IModalConfig = {
            title: this.group.name + " ADD MEMBERS",
            size: "md",
            type: "custom",
            testId: "addMembersModal",
            buttons: [
                {text: 'ADD MEMBERS', size: 'xsm', callback: this.addMembers, closeModal: false},
                {text: 'CANCEL', size: 'sm', type: "secondary", closeModal: true}
            ]
        };
        var optionalsMembersToAdd = this.getOptionalsMembersToAdd();
        this.modalService.openCustomModal(addMembersModalConfig, AddElementsComponent, {
            elementsToAdd: optionalsMembersToAdd,
            elementName: "member"
        });
    }
}
