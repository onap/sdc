import {Input, Component, OnInit} from "@angular/core";
import {Requirement, RequirementUI} from "../../../../../models/requirement";
import {RequirementsEditorComponent} from "./requirementEditor/requirements-editor.component";
import {WorkspaceService} from "../../workspace.service";
import {TopologyTemplateService} from "../../../../services/component-services/topology-template.service";
import {ReqAndCapabilitiesService} from "../req-and-capabilities.service";
import {EventListenerService} from "../../../../../services/event-listener-service";
import {ModalComponent} from "onap-ui-angular/dist/modals/modal.component";
import {SdcUiServices} from "onap-ui-angular";
import sortedIndexBy = require("lodash/sortedIndexBy");

@Component({
    selector: 'requirments',
    templateUrl: './requirments.components.html',
    styleUrls: ['../../../../../../assets/styles/table-style.less', './requirements.component.less']
})



export class RequirmentsComponent implements OnInit {
    @Input() public requirements: Array<RequirementUI>;
    private customModalInstance: ModalComponent;

    constructor(
        private workspaceService: WorkspaceService,
        private loaderService: SdcUiServices.LoaderService,
        private topologyTemplateService: TopologyTemplateService,
        private reqAndCapabilitiesService : ReqAndCapabilitiesService,
        private modalService: SdcUiServices.ModalService,
        private eventListenerService: EventListenerService) {
    }


    ngOnInit(): void {
        let isCreatedManually: RequirementUI[] = [];
        let isImportedFromFile: RequirementUI[] = [];

        isCreatedManually = this.requirements.filter((requirement) => requirement.isCreatedManually);
        isImportedFromFile = this.requirements.filter((requirement) => !requirement.isCreatedManually);

        this.requirements = [];

        isCreatedManually.map((requirement) => this.requirements.push(requirement));
        isImportedFromFile.map((requirement) => this.requirements.push(requirement));

    }



    editRequirement(req) {

        let modalConfig = {
            size: 'md',
            title: 'Update Requirement',
            type: 'custom',
            buttons: [
                {
                    id: 'saveButton',
                    text: ('Update'),
                    size: "'x-small'",
                    callback: () => this.updateRequirement(),
                    closeModal: true
                },
                {text: "Cancel", size: "'x-small'", closeModal: true}]
        };
        let modalInputs = {
            requirement: req,
            relationshipTypesList: this.reqAndCapabilitiesService.getRelationsShipeTypeList(),
            nodeTypesList: this.reqAndCapabilitiesService.getNodeTypesList(),
            capabilityTypesList: this.reqAndCapabilitiesService.getCapabilityTypesList(),
            // isReadonly: this.$scope.isViewMode() || !this.$scope.isDesigner(),
        };

        this.customModalInstance = this.modalService.openCustomModal(modalConfig, RequirementsEditorComponent, {input: modalInputs});
        this.customModalInstance.innerModalContent.instance.
        onValidationChange.subscribe((isValid) => this.customModalInstance.getButtonById('saveButton').disabled = !isValid);

    }

    private updateRequirement() {
        const requirement = this.customModalInstance.innerModalContent.instance.requirementData;
        this.loaderService.activate();
        if (requirement.uniqueId) {
            this.topologyTemplateService.updateRequirement(this.workspaceService.metadata.getTypeUrl(), this.workspaceService.metadata.uniqueId, requirement).subscribe(result => {
                    let index = this.requirements.findIndex(req => result[0].uniqueId === req.uniqueId);
                    this.requirements[index] = new RequirementUI(result[0], this.workspaceService.metadata.uniqueId);
                    this.eventListenerService.notifyObservers('REQUIREMENTS_UPDATED');
                    this.loaderService.deactivate();
                }, () => {
                this.loaderService.deactivate();
                });
        }
    }

    getRowClass(row) {
        if (!row.isCreatedManually) {
            return {
                'importedFromFile': true
            };
        }
    }

}