import { Component, ComponentRef, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import * as _ from 'lodash';
import { SdcUiServices } from 'onap-ui-angular';
import { Capability, CapabilityUI } from '../../../../models/capability';
import { Requirement, RequirementUI } from '../../../../models/requirement';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { ComponentGenericResponse } from '../../../services/responses/component-generic-response';
import { WorkspaceService } from '../workspace.service';
import { CapabilitiesComponent } from './capabilities/capabilities.component';
import { RequirmentsComponent } from './requirements/requirments.components';
import {ReqAndCapabilitiesService} from "./req-and-capabilities.service";
import {CapabilitiesEditorComponent} from "./capabilities/capabilityEditor/capabilities-editor.component";
import {ModalComponent} from "onap-ui-angular/dist/modals/modal.component";
import {EventListenerService} from "../../../../services/event-listener-service";
import {RequirementsEditorComponent} from "./requirements/requirementEditor/requirements-editor.component";
import {ComponentState} from "../../../../utils/constants";

@Component({
    selector: 'req-and-capabilities',
    templateUrl: './req-and-capabilities.component.html',
    styleUrls: ['./req-and-capabilities.component.less']
})
export class ReqAndCapabilitiesComponent implements OnInit {

    @ViewChild('requirmentsContainer', { read: ViewContainerRef }) requirmentsContainer: ViewContainerRef;
    @ViewChild('capabilitiesContainer', { read: ViewContainerRef }) capabilitiesContainer: ViewContainerRef;
    private requirements: Requirement[] = [];
    private requirementsUI: RequirementUI[] = [];
    private capabilities: Capability[] = [];
    private selectTabName: string = 'REQUIREMENTS';
    private notEmptyTable: boolean = true;
    private instanceRef: ComponentRef<any>;
    private customModalInstance: ModalComponent;
    readonly INPUTS_FOR_CAPABILITIES: string = 'INPUTS_FOR_CAPABILITIES';
    readonly INPUTS_FOR_REQUIREMENTS: string = 'INPUTS_FOR_REQUIREMENTS';

    constructor(private workspaceService: WorkspaceService,
                private loaderService: SdcUiServices.LoaderService,
                private topologyTemplateService: TopologyTemplateService,
                private createDynamicComponentService:  SdcUiServices.CreateDynamicComponentService,
                private reqAndCapabilitiesService : ReqAndCapabilitiesService,
                private modalService: SdcUiServices.ModalService,
                private eventListenerService: EventListenerService) {
    }

    ngOnInit(): void {
        this.initCapabilitiesAndRequirements();

        this.eventListenerService.registerObserverCallback('CAPABILITIES_UPDATED', () => {
            this.loadReqOrCap();
        });

        this.eventListenerService.registerObserverCallback('REQUIREMENTS_UPDATED', () => {
            this.loadReqOrCap();
        });
    }



    private initCapabilitiesAndRequirements(): void {
        if (!this.workspaceService.metadata.capabilities || !this.workspaceService.metadata.requirements) {
            this.loaderService.activate();
            this.topologyTemplateService.getRequirementsAndCapabilitiesWithProperties
            (this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId)
                .subscribe((response: ComponentGenericResponse) => {
                    this.workspaceService.metadata.capabilities = response.capabilities;
                    this.workspaceService.metadata.requirements = response.requirements;
                    this.initReqOrCap();
                    this.loaderService.deactivate();
                }, (error) => {
                    this.loaderService.deactivate();
                });
        } else {
            this.initReqOrCap();
        }
    }

    private initReqOrCap() {
        this.populateReqOrCap('requirements');
        this.extendRequirementsToRequiremnetsUI(this.requirements);
        this.populateReqOrCap('capabilities');
        this.loadReqOrCap();
    }

    private populateReqOrCap(instanceName: string) {
        _.forEach(this.workspaceService.metadata[instanceName], (concatArray: any[], name) => {
            this[instanceName] = this[instanceName].concat(concatArray);
        });
    }

    private updateFilter(event) {
        const val = event.target.value.toLowerCase();
        if (this.selectTabName === 'REQUIREMENTS') {
            this.instanceRef.instance.requirements = this.requirementsUI.filter((req: Requirement) => {
                return !val || this.filterRequirments(req, val);
            });
        } else {
            this.instanceRef.instance.capabilities = this.capabilities.filter((cap: Capability) => {
                return !val || this.filterCapabilities(cap, val);
            });
        }

    }

    private selectTab($event) {
        this.selectTabName = $event.title.contains('Requirement') ? 'REQUIREMENTS' : 'CATPABILITIES';
        this.loadReqOrCap();
    }

    private showAdd() {
        return this.workspaceService.metadata.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT;
    }

    private async loadReqOrCap() {
        if (this.instanceRef) {
            this.instanceRef.destroy();
        }

        if (this.selectTabName === 'REQUIREMENTS') {
            this.notEmptyTable = this.requirementsUI.length !== 0;
            this.instanceRef = this.createDynamicComponentService.
            insertComponentDynamically(RequirmentsComponent, {requirements: this.requirementsUI}, this.requirmentsContainer);
            // TODO - Keep the initInputs, so it will be called only for the first time - no need to wait to thse API's every time that a user switches tab
            await this.reqAndCapabilitiesService.initInputs(this.INPUTS_FOR_REQUIREMENTS);
        } else {
            this.notEmptyTable = this.capabilities.length !== 0;
            this.instanceRef = this.createDynamicComponentService.
            insertComponentDynamically(CapabilitiesComponent, {capabilities: this.capabilities}, this.capabilitiesContainer);
            // TODO - Keep the initInputs, so it will be called only for the first time - no need to wait to thse API's every time that a user switches tab
            await this.reqAndCapabilitiesService.initInputs(this.INPUTS_FOR_CAPABILITIES);
        }
    }

    private filterCapabilities(capability: Capability, val: string): boolean {
        return _.includes([capability.name, capability.description, capability.validSourceTypes.join(),
                capability.minOccurrences, capability.maxOccurrences].join('').toLowerCase(), val) ||
            (capability.type && capability.type.replace('tosca.capabilities.', '').toLowerCase().indexOf(val) !== -1);
    }

    private filterRequirments(requirement: Requirement, val: string): boolean {
        return _.includes([requirement.name, requirement.minOccurrences, requirement.maxOccurrences].join('').toLowerCase(), val) ||
            (requirement.capability && requirement.capability.substring('tosca.capabilities.'.length).toLowerCase().indexOf(val) !== -1) ||
            (requirement.node && requirement.node.substring('tosca.node.'.length).toLowerCase().indexOf(val) !== -1) ||
            (requirement.relationship && requirement.relationship.substring('tosca.relationship.'.length)
                .toLowerCase().indexOf(val) !== -1);
    }

    private addCapability() {
        let modalConfig = {
            size: 'md',
            title:  'Add Capability',
            type: 'custom',
            buttons: [
                {
                    id: 'saveButton',
                    text: ('Create'),
                    size: "'x-small'",
                    callback: () => this.createCapability(),
                    closeModal: true
                },
                {text: "Cancel", size: "'x-small'", closeModal: true}]
        };
        let modalInputs = {
            capabilityTypesList: this.reqAndCapabilitiesService.getCapabilityTypesList(),
        };

        this.customModalInstance = this.modalService.openCustomModal(modalConfig, CapabilitiesEditorComponent, {input: modalInputs});
        this.customModalInstance.innerModalContent.instance.
        onValidationChange.subscribe((isValid) => this.customModalInstance.getButtonById('saveButton').disabled = !isValid);
    }

    private createCapability() {
        const capability = this.customModalInstance.innerModalContent.instance.capabilityData;
        this.loaderService.activate();
        if (!capability.uniqueId) {
            this.topologyTemplateService.createCapability(this.workspaceService.metadata.getTypeUrl(), this.workspaceService.metadata.uniqueId, capability).subscribe((result) => {
                this.capabilities.unshift(new CapabilityUI(result[0], this.workspaceService.metadata.uniqueId));
                this.loadReqOrCap();
                this.loaderService.deactivate();
            }, () => {
                this.loaderService.deactivate();
            });
        }
    }

    private addRequirement () {
        let modalConfig = {
            size: 'md',
            title: 'Add Requirement',
            type: 'custom',
            buttons: [
                {
                    id: 'saveButton',
                    text: ('Create'),
                    size: "'x-small'",
                    callback: () => this.createRequirement(),
                    closeModal: true
                },
                {text: "Cancel", size: "'x-small'", closeModal: true}]
        };
        let modalInputs = {
            // requirement: req,
            relationshipTypesList: this.reqAndCapabilitiesService.getRelationsShipeTypeList(),
            nodeTypesList: this.reqAndCapabilitiesService.getNodeTypesList(),
            capabilityTypesList: this.reqAndCapabilitiesService.getCapabilityTypesList(),
            // isReadonly: this.$scope.isViewMode() || !this.$scope.isDesigner(),
        };

        this.customModalInstance = this.modalService.openCustomModal(modalConfig, RequirementsEditorComponent, {input: modalInputs});
        this.customModalInstance.innerModalContent.instance.
        onValidationChange.subscribe((isValid) => this.customModalInstance.getButtonById('saveButton').disabled = !isValid);
    }


    private createRequirement() {
        const requirement = this.customModalInstance.innerModalContent.instance.requirementData;
        this.loaderService.activate();
        if (!requirement.uniqueId) {
            this.topologyTemplateService.createRequirement(this.workspaceService.metadata.getTypeUrl(), this.workspaceService.metadata.uniqueId, requirement).subscribe(result => {
                this.requirementsUI.unshift(new RequirementUI(result[0], this.workspaceService.metadata.uniqueId));
                this.loadReqOrCap();
                this.loaderService.deactivate();
            }, () => {
                this.loaderService.deactivate();
            });
        }
    }

    private extendRequirementsToRequiremnetsUI(requirements: Requirement[]) {
        this.requirements.map((requirement) => {
           this.requirementsUI.push(new RequirementUI(requirement, this.workspaceService.metadata.uniqueId));
        });
    }
}
