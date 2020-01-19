import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { Component as TopologyTemplate, Capability, Requirement, CapabilitiesGroup, RequirementsGroup, ComponentInstance, FullComponentInstance } from "app/models";
import { Store } from "@ngxs/store";
import { GRAPH_EVENTS } from "app/utils";
import { ComponentGenericResponse } from "app/ng2/services/responses/component-generic-response";
import { TopologyTemplateService } from "app/ng2/services/component-services/topology-template.service";
import { EventListenerService } from "app/services";
import { WorkspaceService } from "app/ng2/pages/workspace/workspace.service";
import { CompositionService } from "app/ng2/pages/composition/composition.service";
import {SelectedComponentType, TogglePanelLoadingAction} from "../../../common/store/graph.actions";


export class InstanceCapabilitiesMap {
    [key:string]:Array<Capability>;
}

export class InstanceRequirementsMap {
    [key:string]:Array<Requirement>;
}

@Component({
    selector: 'req-capabilities-tab',
    templateUrl: './req-capabilities-tab.component.html',
    styleUrls: ['./req-capabilities-tab.component.less']
})
export class ReqAndCapabilitiesTabComponent implements OnInit, OnDestroy {

    isComponentInstanceSelected: boolean;
    capabilities:Array<Capability>;
    requirements:Array<Requirement>;
    capabilitiesInstancesMap:InstanceCapabilitiesMap;
    requirementsInstancesMap:InstanceRequirementsMap;
    objectKeys = Object.keys;
    
    @Input() isViewOnly: boolean;
    @Input() componentType: SelectedComponentType;
    @Input() component: TopologyTemplate | FullComponentInstance;
    @Input() input: any;


    constructor(private store: Store,
        private topologyTemplateService:TopologyTemplateService,
        private workspaceService: WorkspaceService,
        private compositionService: CompositionService,
        private eventListenerService:EventListenerService) { }

    ngOnInit(): void {

        this.isComponentInstanceSelected = this.componentType === SelectedComponentType.COMPONENT_INSTANCE;

        this.requirements = [];
        this.capabilities = [];
        this.initEvents();
        this.initRequirementsAndCapabilities();
        
     }

     private initEvents = ():void => {
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_NODE_SELECTED, this.initRequirementsAndCapabilities);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, this.updateRequirementCapabilities);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE,  this.updateRequirementCapabilities);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE,  this.updateRequirementCapabilities);
    }
    
     ngOnDestroy(): void {
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_NODE_SELECTED, this.initRequirementsAndCapabilities);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, this.updateRequirementCapabilities);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE, this.updateRequirementCapabilities);
        this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, this.updateRequirementCapabilities);
     }

     public isCurrentDisplayComponentIsComplex = ():boolean => {
        
        if (this.component instanceof FullComponentInstance) {
            if (this.component.originType === 'VF') {
                return true;
            }
            return false;
        } else {
            return this.component.isComplex();
        }
    }

    private loadComplexComponentData = () => {
        this.store.dispatch(new TogglePanelLoadingAction({isLoading: true}));

        this.topologyTemplateService.getCapabilitiesAndRequirements(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId).subscribe((response:ComponentGenericResponse) => {
            this.workspaceService.metadata.capabilities = response.capabilities;
            this.workspaceService.metadata.requirements = response.requirements;
            this.setScopeCapabilitiesRequirements(response.capabilities, response.requirements);
            this.initInstancesMap();
            this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
        }, (error) => { this.store.dispatch(new TogglePanelLoadingAction({isLoading: false})); });
    }


    private extractValuesFromMap = (map:CapabilitiesGroup | RequirementsGroup):Array<any> => {
        let values = [];
        _.forEach(map, (capabilitiesOrRequirements:Array<Capability> | Array<Requirement>, key) => {
                values = values.concat(capabilitiesOrRequirements)
            }
        );
        return values;
    }

    private setScopeCapabilitiesRequirements = (capabilities:CapabilitiesGroup, requirements:RequirementsGroup) => {
        this.capabilities = this.extractValuesFromMap(capabilities);
        this.requirements = this.extractValuesFromMap(requirements);
    }


    private initInstancesMap = ():void => {

        this.capabilitiesInstancesMap = new InstanceCapabilitiesMap();
        _.forEach(this.capabilities, (capability:Capability) => {
            if (this.capabilitiesInstancesMap[capability.ownerName]) {
                this.capabilitiesInstancesMap[capability.ownerName] = this.capabilitiesInstancesMap[capability.ownerName].concat(capability);
            } else {
                this.capabilitiesInstancesMap[capability.ownerName] = new Array<Capability>(capability);
            }
        });

        this.requirementsInstancesMap = new InstanceRequirementsMap();
        _.forEach(this.requirements, (requirement:Requirement) => {
            if (this.requirementsInstancesMap[requirement.ownerName]) {
                this.requirementsInstancesMap[requirement.ownerName] = this.requirementsInstancesMap[requirement.ownerName].concat(requirement);
            } else {
                this.requirementsInstancesMap[requirement.ownerName] = new Array<Requirement>(requirement);
            }
        });
    }

    private initRequirementsAndCapabilities = (needUpdate?: boolean) => {

        // if instance selected, we take the requirement and capabilities of the instance - always exist because we load them with the graph
        if (this.component instanceof FullComponentInstance) {
            this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
            this.setScopeCapabilitiesRequirements(this.component.capabilities, this.component.requirements);
            if (this.component.originType === 'VF') {
                this.initInstancesMap();
            }
        } else {
            // if instance not selected, we take the requirement and capabilities of the VF/SERVICE, if not exist we call api
            if (needUpdate || !this.component.capabilities || !this.component.requirements) {
                this.loadComplexComponentData();

            } else {
                this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
                this.setScopeCapabilitiesRequirements(this.component.capabilities, this.component.requirements);
                this.initInstancesMap();
            }
        }
    }

    private updateRequirementCapabilities = () => {
        if (!this.isComponentInstanceSelected) {
            this.loadComplexComponentData();
        }
    }




}

