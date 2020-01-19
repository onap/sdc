import {Capability, CapabilityUI} from "../../../../../models/capability";
import { ViewChild, Input, OnInit, Component } from "@angular/core";
import {SdcUiServices} from "onap-ui-angular";
import {CapabilitiesEditorComponent} from "./capabilityEditor/capabilities-editor.component";
import {WorkspaceService} from "../../workspace.service";
import {TopologyTemplateService} from "../../../../services/component-services/topology-template.service";
import {ReqAndCapabilitiesService} from "../req-and-capabilities.service";
import {ModalComponent} from "onap-ui-angular/dist/modals/modal.component";
import {EventListenerService} from "../../../../../services/event-listener-service";


@Component({
    selector: 'capabilities',
    templateUrl: './capabilities.component.html',
    styleUrls: ['./capabilities.component.less','../../../../../../assets/styles/table-style.less']
})
export class CapabilitiesComponent {
    @Input() public capabilities: Array<Capability>;
    @ViewChild('capabilitiesTable') capabilitiesTable: any;
    private customModalInstance: ModalComponent;

    constructor(
        private workspaceService: WorkspaceService,
                private loaderService: SdcUiServices.LoaderService,
                private topologyTemplateService: TopologyTemplateService,
                private reqAndCapabilitiesService : ReqAndCapabilitiesService,
                private modalService: SdcUiServices.ModalService,
                private eventListenerService: EventListenerService) {
    }

    private onSelectCapabilities({ selected }) {
    }

    editCapability(cap: CapabilityUI) {
        let modalConfig = {
            size: 'md',
            title: 'Update Capability',
            type: 'custom',
            buttons: [
                {
                    id: 'saveButton',
                    text: ('Update'),
                    size: "'x-small'",
                    callback: () => this.updateCapability(),
                    closeModal: true
                },
                {text: "Cancel", size: "'x-small'", closeModal: true}]
        };
        let modalInputs = {
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
                let index = this.capabilities.findIndex((cap) => result[0].uniqueId === cap.uniqueId);
                    this.capabilities[index] = new CapabilityUI(result[0], this.workspaceService.metadata.uniqueId);
                this.loaderService.deactivate();
                this.eventListenerService.notifyObservers('CAPABILITIES_UPDATED');
            }, () => {
                this.loaderService.deactivate();
            });
        }
    }


}