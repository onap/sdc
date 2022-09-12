import { Injectable } from "@angular/core";
import { TopologyTemplateService } from "../../../services/component-services/topology-template.service";
import { Store } from "@ngxs/store";
import { SdcUiServices } from "onap-ui-angular";
import { CapabilityTypeModel } from "../../../../models/capability-types";
import { RelationshipTypeModel } from "../../../../models/relationship-types";
import { NodeTypeModel } from "../../../../models/node-types";
import { WorkspaceService } from "../workspace.service";
import { ToscaTypesServiceNg2 } from "../../../services/tosca-types.service";



@Injectable()
export class ReqAndCapabilitiesService {

    private capabilityTypesList: CapabilityTypeModel[];
    private relationshipTypesList: RelationshipTypeModel[];
    private nodeTypesList: NodeTypeModel[];
    private requirementsListUpdated: boolean = false;
    private nodeTypeListUpdated: boolean = false;

    readonly INPUTS_FOR_REQUIREMENTS: string = 'INPUTS_FOR_REQUIREMENTS';
    readonly INPUTS_FOR_CAPABILITIES: string = 'INPUTS_FOR_CAPABILITIES';

    constructor(
        private workspaceService: WorkspaceService,
        private modalService: SdcUiServices.ModalService,
        private loaderService: SdcUiServices.LoaderService,
        private topologyTemplateService: TopologyTemplateService,
        private store: Store,
        private toscaTypesServiceNg2: ToscaTypesServiceNg2){}

    public isViewOnly = (): boolean => {
        return this.store.selectSnapshot((state) => state.workspace.isViewOnly);
    }

    public isDesigner = (): boolean => {
        return this.store.selectSnapshot((state) => state.workspace.isDesigner);
    }

    public async initInputs(initInputsFor: string) {

        // -- COMMON for both --
        this.capabilityTypesList = [];
        let capabilityTypesResult = await this.toscaTypesServiceNg2.fetchCapabilityTypes(this.workspaceService.metadata.model);
        Object.keys(capabilityTypesResult).forEach(key => {this.capabilityTypesList.push(capabilityTypesResult[key])})

        if (initInputsFor === 'INPUTS_FOR_REQUIREMENTS') {
            if (!this.requirementsListUpdated){
                this.relationshipTypesList = [];
                let relationshipTypesResult = await this.toscaTypesServiceNg2.fetchRelationshipTypes(this.workspaceService.metadata.model);
                Object.keys(relationshipTypesResult).forEach(key => {this.relationshipTypesList.push(relationshipTypesResult[key])});
                this.requirementsListUpdated = true;
            }

            if (!this.nodeTypeListUpdated){
                this.nodeTypesList = [];
                let nodeTypesResult = await this.toscaTypesServiceNg2.fetchNodeTypes();
                Object.keys(nodeTypesResult).forEach(key => {this.nodeTypesList.push(nodeTypesResult[key])})
                this.nodeTypeListUpdated = true;
            }
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