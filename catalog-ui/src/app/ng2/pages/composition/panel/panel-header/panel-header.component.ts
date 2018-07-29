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

import { Component,  Input,  AfterViewInit, SimpleChanges, OnInit, OnChanges } from "@angular/core";
import {  SdcUiComponents } from "sdc-ui/lib/angular";
import { IModalConfig } from 'sdc-ui/lib/angular/modals/models/modal-config';
import { ZoneInstanceType } from 'app/models/graph/zones/zone-instance';
import { ValueEditComponent } from './../../../../components/ui/forms/value-edit/value-edit.component';
import { Component as TopologyTemplate, ComponentInstance, IAppMenu } from "app/models";
import { PoliciesService } from '../../../../services/policies.service';
import { GroupsService } from '../../../../services/groups.service';
import {IZoneService} from "../../../../../models/graph/zones/zone";
import { EventListenerService, LoaderService } from "../../../../../services";
import { GRAPH_EVENTS, EVENTS } from "../../../../../utils";
import { UIZoneInstanceObject } from "../../../../../models/ui-models/ui-zone-instance-object";
import { ModalButtonComponent } from "sdc-ui/lib/angular/components";

@Component({
    selector: 'ng2-composition-panel-header',
    templateUrl: './panel-header.component.html',
    styleUrls: ['./panel-header.component.less']
})
export class CompositionPanelHeaderComponent implements OnInit, OnChanges {

    @Input() topologyTemplate: TopologyTemplate;
    @Input() selectedZoneInstanceType: ZoneInstanceType;
    @Input() selectedZoneInstanceId: string;
    @Input() name: string;
    @Input() nonCertified: boolean;
    @Input() isViewOnly: boolean;
    @Input() isLoading: boolean;

    constructor(private groupsService:GroupsService, private policiesService: PoliciesService, 
                private modalService:SdcUiComponents.ModalService, private eventListenerService:EventListenerService) { }

    private service:IZoneService;
    private iconClassName: string;

    ngOnInit(): void {
        this.init();
    }

    ngOnChanges (changes:SimpleChanges):void {
        if(changes.selectedZoneInstanceId){
            this.init();
        }
    }

    ngOnDestroy() {
        
        
    }
    private init = (): void => {
        if (this.selectedZoneInstanceType === ZoneInstanceType.POLICY) {
            this.iconClassName = "sprite-policy-icons policy";
            this.service = this.policiesService;
        } else if (this.selectedZoneInstanceType === ZoneInstanceType.GROUP) {
            this.iconClassName = "sprite-group-icons group";
            this.service = this.groupsService;
        } else {
            this.iconClassName = "sprite-resource-icons defaulticon";
        }
    }

    private renameInstance = (): void => {
        const modalConfig = {
            title: "Edit Name",
            size: "sm",
            type: "custom",
            testId: "renameInstanceModal",
            buttons: [
                {id: 'saveButton', text: 'OK', size: 'xsm', callback: this.saveInstanceName, closeModal: false},
                {id: 'cancelButton', text: 'Cancel', size: 'sm', closeModal: true} 
            ] as ModalButtonComponent[]
        } as IModalConfig;
        this.modalService.openCustomModal(modalConfig, ValueEditComponent, {name: this.name, validityChangedCallback: this.enableOrDisableSaveButton});
    };

    private enableOrDisableSaveButton = (shouldEnable: boolean): void => {
        let saveButton: ModalButtonComponent = this.modalService.getCurrentInstance().getButtonById('saveButton');
        saveButton.disabled = !shouldEnable;
    }

    private saveInstanceName = ():void => {
        let currentModal = this.modalService.getCurrentInstance();
        let nameFromModal:string = currentModal.innerModalContent.instance.name;

        if(nameFromModal != this.name){
            currentModal.buttons[0].disabled = true;
            this.service.updateName(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.selectedZoneInstanceId, nameFromModal).subscribe((success)=>{
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_ZONE_INSTANCE_NAME_CHANGED, nameFromModal);
                this.modalService.closeModal();
            }, (error)=> {
                currentModal.buttons[0].disabled = false;
            });
        } else {
            this.modalService.closeModal();
        }
    };
    
    private deleteInstance = (): void => {
        let title:string = "Delete Confirmation";
        let message:string = "Are you sure you would like to delete "+ this.name + "?";
        this.modalService.openAlertModal(title, message, "OK", this.deleteInstanceConfirmed, "deleteInstanceModal");
    };

    private deleteInstanceConfirmed = () => {
        this.eventListenerService.notifyObservers(EVENTS.SHOW_LOADER_EVENT + 'composition-graph');
        this.service.deleteZoneInstance(this.topologyTemplate.componentType, this.topologyTemplate.uniqueId, this.selectedZoneInstanceId).finally(()=> {
            this.eventListenerService.notifyObservers(EVENTS.HIDE_LOADER_EVENT + 'composition-graph');
        }).subscribe(()=> {
            let deletedItem:UIZoneInstanceObject = new UIZoneInstanceObject(this.selectedZoneInstanceId, this.selectedZoneInstanceType, this.name);
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_DELETE_ZONE_INSTANCE, deletedItem);
        });
    };

}

