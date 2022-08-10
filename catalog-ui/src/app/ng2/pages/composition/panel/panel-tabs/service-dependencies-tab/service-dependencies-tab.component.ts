import {Component, Input} from '@angular/core';
import {Store} from '@ngxs/store';
import {
    CapabilitiesGroup, Capability,
    Component as TopologyTemplate,
    FullComponentInstance,
    PropertiesGroup,
    PropertyBEModel, PropertyModel,
} from 'app/models';
import {DEPENDENCY_EVENTS} from 'app/utils/constants';
import {ComponentMetadata} from '../../../../../../models/component-metadata';
import {ServiceInstanceObject} from '../../../../../../models/service-instance-properties-and-interfaces';
import {EventListenerService} from '../../../../../../services/event-listener-service';
import {TopologyTemplateService} from '../../../../../services/component-services/topology-template.service';
import {ComponentGenericResponse} from '../../../../../services/responses/component-generic-response';
import {WorkspaceService} from '../../../../workspace/workspace.service';
import {SelectedComponentType} from '../../../common/store/graph.actions';
import {CompositionService} from '../../../composition.service';
import {CapabilityFilterConstraint} from "../../../../../../models/capability-filter-constraint";
import {FilterConstraint} from "../../../../../../models/filter-constraint";

@Component({
    selector: 'service-dependencies-tab',
    templateUrl: 'service-dependencies-tab.component.html',
    styleUrls: ['service-dependencies-tab.component.less']
})
export class ServiceDependenciesTabComponent {
    isComponentInstanceSelected: boolean;

    selectedInstanceSiblings: ServiceInstanceObject[];
    componentInstancesConstraints: any[];
    selectedInstanceConstraints: FilterConstraint[];
    selectedInstanceProperties: PropertyBEModel[];
    componentInstanceProperties: PropertiesGroup;
    componentInstanceCapabilityProperties: CapabilitiesGroup;
    metaData: ComponentMetadata;
    componentInstanceCapabilitiesMap : Map<string, PropertyModel[]> = new Map();
    componentInstanceCapabilitiesNames: string[];

    @Input() isViewOnly: boolean;
    @Input() componentType: SelectedComponentType;
    @Input() component: FullComponentInstance | TopologyTemplate;
    @Input() input: any;

    constructor(private store: Store,
                private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private compositionService: CompositionService,
                private eventListenerService: EventListenerService) {
    }

    ngOnInit() {
        this.metaData = this.workspaceService.metadata;
        this.isComponentInstanceSelected = this.componentType === SelectedComponentType.COMPONENT_INSTANCE;
        this.initInstancesWithProperties();
        this.loadConstraints();
        this.initInstancesWithProperties();
        this.initInstancesWithCapabilityProperties()
    }

    public loadConstraints = (): void => {
        this.topologyTemplateService.getServiceFilterConstraints(this.metaData.componentType, this.metaData.uniqueId).subscribe((response) => {
            this.componentInstancesConstraints = response.nodeFilterforNode;
        });
    }

    public notifyDependencyEventsObserver = (isChecked: boolean): void => {
        this.eventListenerService.notifyObservers(DEPENDENCY_EVENTS.ON_DEPENDENCY_CHANGE, isChecked);
    }

    public updateSelectedInstanceConstraints = (constraintsList:Array<FilterConstraint>):void => {
        this.componentInstancesConstraints[this.component.uniqueId].properties = constraintsList;
        this.selectedInstanceConstraints = this.componentInstancesConstraints[this.component.uniqueId].properties;
    }

    public updateSelectedInstanceCapabilitiesConstraints = (constraintsList:Array<CapabilityFilterConstraint>):void => {
        this.componentInstancesConstraints[this.component.uniqueId].capabilities = constraintsList;
        this.selectedInstanceConstraints = this.componentInstancesConstraints[this.component.uniqueId].capabilities;
    }

    private initInstancesWithProperties = (): void => {
        this.topologyTemplateService.getComponentInstanceProperties(this.metaData.componentType, this.metaData.uniqueId).subscribe((genericResponse: ComponentGenericResponse) => {
            this.componentInstanceProperties = genericResponse.componentInstancesProperties;
            this.updateInstanceAttributes();
        });
    }

    private updateInstanceAttributes = (): void => {
        if (this.isComponentInstanceSelected && this.componentInstanceProperties) {
            const instancesMappedList = this.compositionService.componentInstances.map((coInstance) => new ServiceInstanceObject({
                id: coInstance.uniqueId,
                name: coInstance.name,
                properties: this.componentInstanceProperties[coInstance.uniqueId] || []
            }));
            this.selectedInstanceProperties = this.componentInstanceProperties[this.component.uniqueId];
            this.selectedInstanceSiblings = instancesMappedList.filter((coInstance) => coInstance.id !== this.component.uniqueId);
        }
    }

    private initInstancesWithCapabilityProperties = (): void => {
        this.componentInstanceCapabilityProperties = this.component.capabilities;
        this.updateComponentInstanceCapabilities();
    }

    private updateComponentInstanceCapabilities = (): void => {
        if (this.isComponentInstanceSelected && this.componentInstanceCapabilityProperties) {
            _.forEach(_.flatten(_.values(this.componentInstanceCapabilityProperties)), (capability: Capability) => {
                if (capability.properties) {
                    this.componentInstanceCapabilitiesMap.set(capability.name, capability.properties);
                }
            });
        }
    }

}
