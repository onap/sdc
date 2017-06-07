'use strict';
import {ICompositionViewModelScope} from "../../composition-view-model";
import {CapabilitiesGroup, Requirement, RequirementsGroup} from "app/models";
import {ComponentServiceNg2} from "app/ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "app/ng2/services/responses/component-generic-response";
import {GRAPH_EVENTS} from "app/utils";
import {EventListenerService} from "app/services";
import {ComponentInstance, Capability} from "app/models";

interface IRelationsViewModelScope extends ICompositionViewModelScope {
    isLoading:boolean;
    $parent:ICompositionViewModelScope;
    getRelation(requirement:any):any;
    capabilities:Array<Capability>;
    requirements:Array<Requirement>;

    //for complex components
    capabilitiesInstancesMap:InstanceCapabilitiesMap;
    requirementsInstancesMap:InstanceRequirementsMap;
}
export class InstanceCapabilitiesMap {
    [key:string]:Array<Capability>;
}

export class InstanceRequirementsMap {
    [key:string]:Array<Requirement>;
}

export class RelationsViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        'ComponentServiceNg2',
        'EventListenerService'
    ];

    constructor(private $scope:IRelationsViewModelScope,
                private $filter:ng.IFilterService,
                private ComponentServiceNg2:ComponentServiceNg2,
                private eventListenerService:EventListenerService) {
        this.initScope();
    }

    private loadComplexComponentData = () => {
        this.$scope.isLoading = true;
        this.ComponentServiceNg2.getCapabilitiesAndRequirements(this.$scope.currentComponent.componentType, this.$scope.currentComponent.uniqueId).subscribe((response:ComponentGenericResponse) => {
            this.$scope.currentComponent.capabilities = response.capabilities;
            this.$scope.currentComponent.requirements = response.requirements;
            this.setScopeCapabilitiesRequirements(this.$scope.currentComponent.capabilities, this.$scope.currentComponent.requirements);
            this.initInstancesMap();
            this.$scope.isLoading = false;
        });
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
        this.$scope.capabilities = this.extractValuesFromMap(capabilities);
        this.$scope.requirements = this.extractValuesFromMap(requirements);
    }


    private initInstancesMap = ():void => {

        this.$scope.capabilitiesInstancesMap = new InstanceCapabilitiesMap();
        _.forEach(this.$scope.capabilities, (capability:Capability) => {
            if (this.$scope.capabilitiesInstancesMap[capability.ownerName]) {
                this.$scope.capabilitiesInstancesMap[capability.ownerName] = this.$scope.capabilitiesInstancesMap[capability.ownerName].concat(capability);
            } else {
                this.$scope.capabilitiesInstancesMap[capability.ownerName] = new Array<Capability>(capability);
            }
        });

        this.$scope.requirementsInstancesMap = new InstanceRequirementsMap();
        _.forEach(this.$scope.requirements, (requirement:Requirement) => {
            if (this.$scope.requirementsInstancesMap[requirement.ownerName]) {
                this.$scope.requirementsInstancesMap[requirement.ownerName] = this.$scope.requirementsInstancesMap[requirement.ownerName].concat(requirement);
            } else {
                this.$scope.requirementsInstancesMap[requirement.ownerName] = new Array<Requirement>(requirement);
            }
        });
    }

    private initRequirementsAndCapabilities = (needUpdate?: boolean) => {

        // if instance selected, we take the requirement and capabilities of the instance - always exist because we load them with the graph
        if (this.$scope.isComponentInstanceSelected()) {
            this.$scope.isLoading = false;
            this.setScopeCapabilitiesRequirements(this.$scope.currentComponent.selectedInstance.capabilities, this.$scope.currentComponent.selectedInstance.requirements);
            if (this.$scope.currentComponent.selectedInstance.originType === 'VF') {
                this.initInstancesMap();
            }
        } else {
            // if instance not selected, we take the requirement and capabilities of the VF/SERVICE, if not exist we call api
            if (needUpdate || !this.$scope.currentComponent.capabilities || !this.$scope.currentComponent.requirements) {
                this.loadComplexComponentData();

            } else {
                this.$scope.isLoading = false;
                this.setScopeCapabilitiesRequirements(this.$scope.currentComponent.capabilities, this.$scope.currentComponent.requirements);
                this.initInstancesMap();
            }
        }
    }

    private updateRequirementCapabilities = () => {
        if (!this.$scope.isComponentInstanceSelected()) {
            this.loadComplexComponentData();
        }
    }

    private initEvents = ():void => {
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_NODE_SELECTED, this.initRequirementsAndCapabilities);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, this.updateRequirementCapabilities);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE,  this.updateRequirementCapabilities);
        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE,  this.updateRequirementCapabilities);
    }

    private initScope = ():void => {

        this.$scope.requirements = [];
        this.$scope.capabilities = [];

        this.initEvents();
        this.initRequirementsAndCapabilities();

        this.$scope.isCurrentDisplayComponentIsComplex = ():boolean => {
            if (this.$scope.isComponentInstanceSelected()) {
                if (this.$scope.currentComponent.selectedInstance.originType === 'VF') {
                    return true;
                }
                return false;
            } else {
                return this.$scope.currentComponent.isComplex();
            }
        }

        this.$scope.$on('$destroy', () => {

            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_NODE_SELECTED, this.initRequirementsAndCapabilities);
            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, this.updateRequirementCapabilities);
            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE, this.updateRequirementCapabilities);
            this.eventListenerService.unRegisterObserver(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, this.updateRequirementCapabilities);
        });

    }
}
