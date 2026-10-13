import { Injectable } from '@angular/core';
import { Store } from '@ngxs/store';
import { SdcUiServices } from 'onap-ui-angular';
import { CapabilityTypeModel } from '../../../models/capability-types';
import { NodeTypeModel } from '../../../models/node-types';
import { RelationshipTypeModel } from '../../../models/relationship-types';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { ToscaTypesService } from '../../../services/tosca-types.service';
import { WorkspaceService } from '../workspace.service';

@Injectable()
export class ReqAndCapabilitiesService {

    readonly INPUTS_FOR_REQUIREMENTS: string = 'INPUTS_FOR_REQUIREMENTS';
    readonly INPUTS_FOR_CAPABILITIES: string = 'INPUTS_FOR_CAPABILITIES';

    private capabilityTypesList: CapabilityTypeModel[];
    private relationshipTypesList: RelationshipTypeModel[];
    private nodeTypesList: NodeTypeModel[];

    constructor(
        private workspaceService: WorkspaceService,
        private modalService: SdcUiServices.ModalService,
        private loaderService: SdcUiServices.LoaderService,
        private topologyTemplateService: TopologyTemplateService,
        private store: Store,
        private toscaTypesService: ToscaTypesService) {}

    public isViewOnly = (): boolean => {
        return this.store.selectSnapshot((state) => state.workspace.isViewOnly);
    }

    public isDesigner = (): boolean => {
        return this.store.selectSnapshot((state) => state.workspace.isDesigner);
    }

    public async initInputs(initInputsFor: string) {

        // -- COMMON for both --
        this.capabilityTypesList = [];
        const capabilityTypesResult = await this.toscaTypesService.fetchCapabilityTypes(this.workspaceService.metadata.model);
        Object.keys(capabilityTypesResult).forEach((key) => {this.capabilityTypesList.push(capabilityTypesResult[key]); });
        this.capabilityTypesList.sort((capabilityType1, capabilityType2) => capabilityType1.toscaPresentation.type.localeCompare(capabilityType2.toscaPresentation.type));

        if (initInputsFor === 'INPUTS_FOR_REQUIREMENTS') {
            this.relationshipTypesList = [];
            const relationshipTypesResult = await this.toscaTypesService.fetchRelationshipTypes(this.workspaceService.metadata.model);
            Object.keys(relationshipTypesResult).forEach((key) => {this.relationshipTypesList.push(relationshipTypesResult[key]); });
            this.relationshipTypesList.sort((relationship1, relationship2) => relationship1.toscaPresentation.type.localeCompare(relationship2.toscaPresentation.type));

            this.nodeTypesList = [];
            const nodeTypesResult = await this.toscaTypesService.fetchNodeTypes(this.workspaceService.metadata.model);
            Object.keys(nodeTypesResult).forEach((key) => {this.nodeTypesList.push(nodeTypesResult[key]); });
            this.nodeTypesList.sort((node1, node2) => node1.componentMetadataDefinition.componentMetadataDataDefinition.toscaResourceName.localeCompare(node2.componentMetadataDefinition.componentMetadataDataDefinition.toscaResourceName));
        }
    }

    getCapabilityTypesList() {
        return this.capabilityTypesList;
    }

    getRelationsShipeTypeList() {
        return this.relationshipTypesList;
    }

    getNodeTypesList() {
        return this.nodeTypesList;
    }
}
