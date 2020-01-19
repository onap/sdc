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

import { Component, Input, OnInit } from "@angular/core";
import { SdcUiComponents, SdcUiCommon, SdcUiServices } from "onap-ui-angular";
import { EditNameModalComponent } from "app/ng2/pages/composition/panel/panel-header/edit-name-modal/edit-name-modal.component";
import {Component as TopologyTemplate, FullComponentInstance, GroupInstance, PolicyInstance, Requirement, Capability, ComponentInstance} from "app/models";
import { Select } from "@ngxs/store";
import { Observable } from "rxjs/Observable";
import { Subscription } from "rxjs";
import {GRAPH_EVENTS} from "../../../../../utils/constants";
import { CompositionService } from "app/ng2/pages/composition/composition.service";
import {EventListenerService} from "../../../../../services/event-listener-service";
import { ComponentInstanceServiceNg2 } from "app/ng2/services/component-instance-services/component-instance.service";
import { WorkspaceService } from "app/ng2/pages/workspace/workspace.service";
import { GroupsService, PoliciesService } from "app/services-ng2";
import { UIZoneInstanceObject } from "../../../../../models/ui-models/ui-zone-instance-object";
import {SelectedComponentType} from "../../common/store/graph.actions";
import * as _ from 'lodash';
import {GraphState} from "../../common/store/graph.state";


@Component({
    selector: 'ng2-composition-panel-header',
    templateUrl: './panel-header.component.html',
    styleUrls: ['./panel-header.component.less']
})
export class CompositionPanelHeaderComponent implements OnInit {
    @Input() isViewOnly: boolean;
    @Input() selectedComponent: FullComponentInstance | TopologyTemplate | GroupInstance | PolicyInstance;
    @Select(GraphState.getSelectedComponentType) selectedComponentType$:Observable<SelectedComponentType>;


    constructor(private modalService: SdcUiServices.ModalService,
                private groupService: GroupsService,
                private policiesService: PoliciesService,
                private eventListenerService: EventListenerService,
                private compositionService: CompositionService,
                private workspaceService: WorkspaceService,
                private componentInstanceService: ComponentInstanceServiceNg2) { }

    private iconClassName: string;
    private valueEditModalInstance: SdcUiComponents.ModalComponent;
    private isTopologyTemplateSelected: boolean;
    private componentTypeSubscription: Subscription;

    ngOnInit(): void {
        this.componentTypeSubscription = this.selectedComponentType$.subscribe((newComponentType) => {

            this.initClasses(newComponentType);
            this.isTopologyTemplateSelected = (newComponentType === SelectedComponentType.TOPOLOGY_TEMPLATE) ? true : false;
        });
    }

    ngOnDestroy() {
        if(this.componentTypeSubscription) {
            this.componentTypeSubscription.unsubscribe();
        }
    }

    private initClasses = (componentType:SelectedComponentType): void => {
        if (componentType === SelectedComponentType.POLICY) {
            this.iconClassName = "sprite-policy-icons policy";
        } else if (componentType === SelectedComponentType.GROUP) {
            this.iconClassName = "sprite-group-icons group";
        } else {
            this.iconClassName = undefined;
        }
    }

    private renameInstance = (): void => {
        const modalConfig = {
            title: "Edit Name",
            size: "sm",
            type: SdcUiCommon.ModalType.custom,
            testId: "renameInstanceModal",
            buttons: [
                {id: 'saveButton', text: 'OK', size: 'xsm', callback: this.saveInstanceName, closeModal: false},
                {id: 'cancelButton', text: 'Cancel', size: 'sm', closeModal: true}
            ] as SdcUiCommon.IModalButtonComponent[]
        } as SdcUiCommon.IModalConfig;
        this.valueEditModalInstance = this.modalService.openCustomModal(modalConfig, EditNameModalComponent, {name: this.selectedComponent.name, validityChangedCallback: this.enableOrDisableSaveButton});
    };

    private enableOrDisableSaveButton = (shouldEnable: boolean): void => {
        let saveButton: SdcUiComponents.ModalButtonComponent = this.valueEditModalInstance.getButtonById('saveButton');
        saveButton.disabled = !shouldEnable;
    }

    private saveInstanceName = ():void => {
        let nameFromModal:string = this.valueEditModalInstance.innerModalContent.instance.name;

        if(nameFromModal != this.selectedComponent.name){
            let oldName = this.selectedComponent.name;
            this.selectedComponent.name = nameFromModal;
            this.valueEditModalInstance.buttons[0].disabled = true;

            let onFailed = (error) => { 
                this.selectedComponent.name = oldName;
                this.valueEditModalInstance.buttons[0].disabled = false;
            };

            if(this.selectedComponent instanceof FullComponentInstance){
                let onSuccess = (componentInstance:ComponentInstance) => {
                    //update requirements and capabilities owner name
                    _.forEach((<FullComponentInstance>this.selectedComponent).requirements, (requirementsArray:Array<Requirement>) => {
                        _.forEach(requirementsArray, (requirement:Requirement):void => {
                            requirement.ownerName = componentInstance.name;
                        });
                    });

                    _.forEach((<FullComponentInstance>this.selectedComponent).capabilities, (capabilitiesArray:Array<Capability>) => {
                        _.forEach(capabilitiesArray, (capability:Capability):void => {
                            capability.ownerName = componentInstance.name;
                        });
                    });
                    this.valueEditModalInstance.closeModal();
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, this.selectedComponent);
                };

                this.componentInstanceService.updateComponentInstance(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, new ComponentInstance(this.selectedComponent))
                    .subscribe(onSuccess, onFailed);
            } else if (this.selectedComponent instanceof PolicyInstance) {
                this.policiesService.updateName(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.selectedComponent.uniqueId, nameFromModal).subscribe((success)=>{
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, this.selectedComponent);
                    this.valueEditModalInstance.closeModal();
                }, onFailed);
            } else if (this.selectedComponent instanceof GroupInstance){
                this.groupService.updateName(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.selectedComponent.uniqueId, nameFromModal).subscribe((success)=>{
                    this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, this.selectedComponent);
                    this.valueEditModalInstance.closeModal();
                }, onFailed);
            }
        }  else {
            this.valueEditModalInstance.closeModal();
        }
    };

    private deleteInstance = (): void => {
        let title:string = "Delete Confirmation";
        let message:string = "Are you sure you would like to delete "+ this.selectedComponent.name + "?";
        const okButton = {testId: "OK", text: "OK", type: SdcUiCommon.ButtonType.warning, callback: this.deleteInstanceConfirmed, closeModal: true} as SdcUiComponents.ModalButtonComponent;
        this.modalService.openWarningModal(title, message, "delete-modal", [okButton]);
    };

    private deleteInstanceConfirmed: Function = () => {
        if(this.selectedComponent instanceof FullComponentInstance){
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE , this.selectedComponent.uniqueId);
        }
        else if(this.selectedComponent instanceof PolicyInstance){
            this.policiesService.deletePolicy(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.selectedComponent.uniqueId).subscribe((success)=>{
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_ZONE_INSTANCE ,
                    new UIZoneInstanceObject(this.selectedComponent.uniqueId, 1));
            }, (err) => {});

        }
        else if(this.selectedComponent instanceof GroupInstance){
            this.groupService.deleteGroup(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.selectedComponent.uniqueId).subscribe((success)=>{
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_ZONE_INSTANCE ,
                    new UIZoneInstanceObject(this.selectedComponent.uniqueId, 0));
            }, (err) => {});

        }
    };
}

