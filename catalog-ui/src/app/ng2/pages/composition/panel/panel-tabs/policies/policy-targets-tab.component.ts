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
import { Component, Input, Output, EventEmitter, OnChanges, HostBinding, OnDestroy } from "@angular/core";
import { TranslateService } from './../../../../../shared/translator/translate.service';
import { Component as TopologyTemplate } from "app/models";
import { PoliciesService } from "../../../../../services/policies.service";
import { PolicyInstance, PolicyTargetsMap } from './../../../../../../models/graph/zones/policy-instance';
import { SimpleChanges } from "@angular/core/src/metadata/lifecycle_hooks";
import { SdcUiComponents } from "sdc-ui/lib/angular";
import { IModalConfig } from "sdc-ui/lib/angular/modals/models/modal-config";
import { AddElementsComponent } from "../../../../../components/ui/modal/add-elements/add-elements.component";
import { TargetUiObject } from "../../../../../../models/ui-models/ui-target-object";
import { ComponentInstance } from "../../../../../../models/componentsInstances/componentInstance";
import { TargetOrMemberType } from "../../../../../../utils/constants";
import { GRAPH_EVENTS } from 'app/utils';
import { EventListenerService } from 'app/services/event-listener-service';

@Component({
    selector: 'policy-targets-tab',
    templateUrl: './policy-targets-tab.component.html',
    styleUrls: ['./../base/base-tab.component.less', 'policy-targets-tab.component.less']
})

export class PolicyTargetsTabComponent implements OnChanges, OnDestroy {

    private targets: Array<TargetUiObject>; // UI object to hold all targets with names.

    @Input() policy: PolicyInstance;
    @Input() topologyTemplate: TopologyTemplate;
    @Input() isViewOnly: boolean;
    @Output() isLoading: EventEmitter<boolean> = new EventEmitter<boolean>();
    @HostBinding('class') classes = 'component-details-panel-tab-policy-targets';

    constructor(private translateService: TranslateService,
        private policiesService: PoliciesService,
        private modalService: SdcUiComponents.ModalService,
        private eventListenerService: EventListenerService
    ) {
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, this.initTargets)
    }

    ngOnChanges(changes:SimpleChanges):void {
        this.initTargets();
    }

    ngOnDestroy() {
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE);
    }

    deleteTarget(target: TargetUiObject): void {
        this.isLoading.emit(true);
        this.policiesService.deletePolicyTarget(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.policy, target.uniqueId, target.type).subscribe(
            (policyInstance:PolicyInstance) => {
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, policyInstance);
            },
            error => console.log("Error deleting target!"),
            () => this.isLoading.emit(false)
        );
    }

    private initTargets = (policyInstance?: PolicyInstance) => {
        this.policy = policyInstance ? policyInstance : this.policy;
        this.targets = this.policy.getTargetsAsUiObject(this.topologyTemplate.componentInstances, this.topologyTemplate.groupInstances);
    }
   
    addTargets = ():void => {
        
        var targetsToAdd:Array<TargetUiObject> = this.modalService.getCurrentInstance().innerModalContent.instance.existingElements; //TODO refactor sdc-ui modal in order to return the data
        if(targetsToAdd.length > 0) {
            this.modalService.closeModal();
            this.isLoading.emit(true);
            var updatedTarget: Array<TargetUiObject> = _.union(this.targets, targetsToAdd);
            this.policiesService.updateTargets(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.policy.uniqueId, updatedTarget).subscribe(
                (updatedPolicyInstance:PolicyInstance) => {
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, updatedPolicyInstance);
                },
                error => {
                    console.log("Error updating targets!");
                },
                () => this.isLoading.emit(false)
            );
        }
    }

    getOptionalsTargetsToAdd():Array<TargetUiObject> {
        let optionalsTargetsToAdd:Array<TargetUiObject> = [];
        // adding all instances as optional targets to add if not already exist
        _.forEach(this.topologyTemplate.componentInstances, (instance:ComponentInstance) => {
            if (!_.some(this.targets, (target:TargetUiObject) => {
                    return target.uniqueId === instance.uniqueId
                })) {
                optionalsTargetsToAdd.push(new TargetUiObject(instance.uniqueId, TargetOrMemberType.COMPONENT_INSTANCES, instance.name));
            }
        });

        // adding all groups as optional targets to add if not already exist
        _.forEach(this.topologyTemplate.groupInstances, (groupInstance:ComponentInstance) => { // adding all instances as optional targets to add if not already exist
            if (!_.some(this.targets, (target:TargetUiObject) => {
                    return target.uniqueId === groupInstance.uniqueId
                })) {
                optionalsTargetsToAdd.push(new TargetUiObject(groupInstance.uniqueId, TargetOrMemberType.GROUPS, groupInstance.name));
            }
        });

        return optionalsTargetsToAdd;
    }

    openAddTargetModal(): void {
        let addTargetModalConfig: IModalConfig = {
            title: this.policy.name + " ADD TARGETS",
            size: "md",
            type: "custom",
            testId: "addTargetsModal",
            buttons: [
                {text: "ADD TARGETS", size: 'xsm', callback: this.addTargets, closeModal: false},
                {text: 'CANCEL', size: 'sm', type: "secondary", closeModal: true}
            ]
        };
        var optionalTargetsToAdd = this.getOptionalsTargetsToAdd();
        this.modalService.openCustomModal(addTargetModalConfig, AddElementsComponent, {
            elementsToAdd: optionalTargetsToAdd,
            elementName: "target"
        });

    }
}
