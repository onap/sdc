import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { SdcUiCommon, SdcUiServices } from 'onap-ui-angular';
import { ModalComponent } from 'onap-ui-angular/dist/modals/modal.component';
import { Capability, CapabilityUI } from '../../../../models/capability';
import { TopologyTemplateService } from '../../../../services/component-services/topology-template.service';
import { EventListenerService } from '../../../../services/event-listener.service';
import { WorkspaceService } from '../../workspace.service';
import { ReqAndCapabilitiesService } from '../req-and-capabilities.service';
import { CapabilitiesEditorComponent } from './capabilities-editor/capabilities-editor.component';

@Component({
    selector: 'capabilities',
    templateUrl: './capabilities.component.html',
    styleUrls: ['./capabilities.component.less', '../../../../../assets/styles/table-style.less']
})
export class CapabilitiesComponent {
    @Input() public capabilities: Capability[];
    @ViewChild('capabilitiesTable') capabilitiesTable: any;
    private customModalInstance: ModalComponent;

    constructor(
        private workspaceService: WorkspaceService,
        private loaderService: SdcUiServices.LoaderService,
        private topologyTemplateService: TopologyTemplateService,
        private reqAndCapabilitiesService: ReqAndCapabilitiesService,
        private modalService: SdcUiServices.ModalService,
        private eventListenerService: EventListenerService) {
    }

    public onSelectCapabilities({ selected }) {
    }

    editCapability(cap: CapabilityUI) {
        const modalConfig = {
            size: 'md',
            title: 'Update Capability',
            type: SdcUiCommon.ModalType.custom,
            buttons: [
                {
                    id: 'saveButton',
                    text: ('Update'),
                    size: '\'x-small\'',
                    callback: () => this.updateCapability(),
                    closeModal: true
                },
                {text: 'Cancel', size: '\'x-small\'', closeModal: true}]
        };
        const modalInputs = {
            capability: cap,
            capabilityTypesList: this.reqAndCapabilitiesService.getCapabilityTypesList(),
        };

        this.customModalInstance = this.modalService.openCustomModal(modalConfig, CapabilitiesEditorComponent, {input: modalInputs});
        this.customModalInstance.innerModalContent.instance.
        onValidationChange.subscribe((isValid) => this.customModalInstance.getButtonById('saveButton').disabled = !isValid);
    }

    expendRow(row) {
        this.capabilitiesTable.rowDetail.toggleExpandRow(row);
    }

    private updateCapability() {
        const capability = this.customModalInstance.innerModalContent.instance.capabilityData;
        this.loaderService.activate();
        if (capability.uniqueId) {
            this.topologyTemplateService.updateCapability(this.workspaceService.metadata.getTypeUrl(), this.workspaceService.metadata.uniqueId, capability).subscribe((result) => {
                const index = this.capabilities.findIndex((cap) => result[0].uniqueId === cap.uniqueId);
                this.capabilities[index] = new CapabilityUI(result[0], this.workspaceService.metadata.uniqueId);
                this.loaderService.deactivate();
                this.eventListenerService.notifyObservers('CAPABILITIES_UPDATED');
            }, () => {
                this.loaderService.deactivate();
            });
        }
    }

}
