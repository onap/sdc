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
import { Component, Input, Output, EventEmitter, OnChanges, HostBinding, OnDestroy, OnInit } from "@angular/core";
import { TranslateService } from './../../../../../shared/translator/translate.service';
import { PoliciesService } from "../../../../../services/policies.service";
import { PolicyInstance } from './../../../../../../models/graph/zones/policy-instance';
import { SdcUiComponents, SdcUiCommon, SdcUiServices } from "onap-ui-angular";
import { AddElementsComponent } from "../../../../../components/ui/modal/add-elements/add-elements.component";
import { TargetUiObject } from "../../../../../../models/ui-models/ui-target-object";
import { ComponentInstance } from "../../../../../../models/componentsInstances/componentInstance";
import { TargetOrMemberType } from "../../../../../../utils/constants";
import { GRAPH_EVENTS } from 'app/utils';
import { EventListenerService } from 'app/services/event-listener-service';
import { CompositionService } from "app/ng2/pages/composition/composition.service";
import { WorkspaceService } from "app/ng2/pages/workspace/workspace.service";
import { Store } from "@ngxs/store";
import { Select } from "@ngxs/store";
import { Observable } from "rxjs";
import { tap } from "rxjs/operators";
import {GraphState} from "../../../common/store/graph.state";

@Component({
    selector: 'policy-targets-tab',
    templateUrl: './policy-targets-tab.component.html',
    styleUrls: ['policy-targets-tab.component.less']
})
 
export class PolicyTargetsTabComponent implements OnInit {

    @Input() input:any;


    @Input() isViewOnly: boolean;
    @HostBinding('class') classes = 'component-details-panel-tab-policy-targets';
    @Select(GraphState.getSelectedComponent) policy$: Observable<PolicyInstance>;
    public policy: PolicyInstance;
    private subscription;
    
    private addModalInstance: SdcUiComponents.ModalComponent;
    public targets: Array<TargetUiObject>; // UI object to hold all targets with names.


    constructor(private translateService: TranslateService,
        private policiesService: PoliciesService,
        private modalService: SdcUiServices.ModalService,
        private eventListenerService: EventListenerService,
        private compositionService: CompositionService,
        private workspaceService: WorkspaceService,
        private loaderService: SdcUiServices.LoaderService,
        private store: Store
    ) { }

    ngOnInit() {
        this.subscription = this.policy$.pipe(
            tap((policy) => {
                if(policy instanceof PolicyInstance){
                    this.policy = policy;
                    this.targets = this.policy.getTargetsAsUiObject(<ComponentInstance[]>this.compositionService.componentInstances, this.compositionService.groupInstances);                      
                }
            })).subscribe(); 
    }

    ngOnDestroy () {
        if(this.subscription)
            this.subscription.unsubscribe();
    }

    deleteTarget(target: TargetUiObject): void {
        this.loaderService.activate();
        this.policiesService.deletePolicyTarget(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.policy, target.uniqueId, target.type).subscribe(
            (policyInstance:PolicyInstance) => {
                this.targets = this.targets.filter(item => item.uniqueId !== target.uniqueId);
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, policyInstance);
                // this.store.dispatch(new UpdateSelectedComponentAction({uniqueId: policyInstance.uniqueId, type:ComponentType.}));
            },
            error => {
                console.log("Error deleting target!");
                this.loaderService.deactivate();
            },
            () => this.loaderService.deactivate()
        );
    }

   
    addTargets = ():void => {
        
        var targetsToAdd:Array<TargetUiObject> = this.addModalInstance.innerModalContent.instance.existingElements; //TODO refactor sdc-ui modal in order to return the data
        if(targetsToAdd.length > 0) {
            this.addModalInstance.closeModal();
            this.loaderService.activate();
            var updatedTargets: Array<TargetUiObject> = _.union(this.targets, targetsToAdd);
            this.policiesService.updateTargets(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.policy.uniqueId, updatedTargets).subscribe(
                (updatedPolicyInstance:PolicyInstance) => {
                    this.targets = updatedTargets;
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, updatedPolicyInstance);
                    // this.store.dispatch(new UpdateSelectedComponentAction({component: updatedPolicyInstance}));
                },
                error => {
                    console.log("Error updating targets!");
                    this.loaderService.deactivate();
                },
                () => this.loaderService.deactivate()
            );
        }
    }

    getOptionalsTargetsToAdd():Array<TargetUiObject> {
        let optionalsTargetsToAdd:Array<TargetUiObject> = [];
        // adding all instances as optional targets to add if not already exist
        _.forEach(this.compositionService.componentInstances, (instance:ComponentInstance) => {
            if (!_.some(this.targets, (target:TargetUiObject) => {
                    return target.uniqueId === instance.uniqueId
                })) {
                optionalsTargetsToAdd.push(new TargetUiObject(instance.uniqueId, TargetOrMemberType.COMPONENT_INSTANCES, instance.name));
            }
        });

        // adding all groups as optional targets to add if not already exist
        _.forEach(this.compositionService.groupInstances, (groupInstance:ComponentInstance) => { // adding all instances as optional targets to add if not already exist
            if (!_.some(this.targets, (target:TargetUiObject) => {
                    return target.uniqueId === groupInstance.uniqueId
                })) {
                optionalsTargetsToAdd.push(new TargetUiObject(groupInstance.uniqueId, TargetOrMemberType.GROUPS, groupInstance.name));
            }
        });

        return optionalsTargetsToAdd;
    }

    openAddTargetModal(): void {
        let addTargetModalConfig = {
            title: this.policy.name + " ADD TARGETS",
            size: "md",
            type: SdcUiCommon.ModalType.custom,
            testId: "addTargetsModal",
            buttons: [
                {text: "ADD TARGETS", size: 'xsm', callback: this.addTargets, closeModal: false},
                {text: 'CANCEL', size: 'sm', type: "secondary", closeModal: true}
            ]
        } as SdcUiCommon.IModalConfig;
        var optionalTargetsToAdd = this.getOptionalsTargetsToAdd();
        this.addModalInstance = this.modalService.openCustomModal(addTargetModalConfig, AddElementsComponent, {
            elementsToAdd: optionalTargetsToAdd,
            elementName: "target"
        });
    }
}
