
import { Component, Input } from '@angular/core';
import { Store } from '@ngxs/store';
import {
    CapabilitiesGroup,
    Capability,
    Component as TopologyTemplate,
    ComponentInstance,
    FullComponentInstance,
    InputBEModel,
    InputsGroup,
    InterfaceModel,
    PropertiesGroup
} from 'app/models';
import { ComponentMetadata } from '../../../../../../models/component-metadata';
import { ServiceInstanceObject } from '../../../../../../models/service-instance-properties-and-interfaces';
import { EventListenerService } from '../../../../../../services/event-listener-service';
import { TopologyTemplateService } from '../../../../../services/component-services/topology-template.service';
import { ComponentGenericResponse } from '../../../../../services/responses/component-generic-response';
import { WorkspaceService } from '../../../../workspace/workspace.service';
import { SelectedComponentType } from '../../../common/store/graph.actions';
import { CompositionService } from '../../../composition.service';

@Component({
    selector: 'service-consumption-tab',
    templateUrl: './service-consumption-tab.component.html',
    styleUrls: ['./service-consumption-tab.component.less'],
})
export class ServiceConsumptionTabComponent {
    isComponentInstanceSelected: boolean;

    instancesMappedList: ServiceInstanceObject[];
    componentInstancesProperties: PropertiesGroup;
    componentInstancesInputs: InputsGroup;
    componentInstancesInterfaces: Map<string, InterfaceModel[]>;
    componentInputs: InputBEModel[];
    componentCapabilities: Capability[];
    instancesCapabilitiesMap: Map<string, Capability[]>;
    metadata: ComponentMetadata;

    @Input() isViewOnly: boolean;
    @Input() componentType: SelectedComponentType;
    @Input() component: TopologyTemplate | FullComponentInstance;
    @Input() input: any;

    constructor(private store: Store,
                private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private compositionService: CompositionService,
                private eventListenerService: EventListenerService ) {}
    ngOnInit() {
        this.metadata = this.workspaceService.metadata;
        this.isComponentInstanceSelected = this.componentType === SelectedComponentType.COMPONENT_INSTANCE;
        this.initInstances();
    }

    private initInstances = (): void => {
        this.topologyTemplateService.getServiceConsumptionData(this.metadata.componentType, this.metadata.uniqueId).subscribe((genericResponse: ComponentGenericResponse) => {
            this.componentInstancesProperties = genericResponse.componentInstancesProperties;
            this.componentInstancesInputs = genericResponse.componentInstancesInputs;
            this.componentInstancesInterfaces = genericResponse.componentInstancesInterfaces;
            this.componentInputs = genericResponse.inputs;
            this.buildInstancesCapabilitiesMap(genericResponse.componentInstances);
            this.updateInstanceAttributes();
        });
    }

    private buildInstancesCapabilitiesMap = (componentInstances: Array<ComponentInstance>): void => {
        this.instancesCapabilitiesMap = new Map();
        let flattenCapabilities = [];
        _.forEach(componentInstances, (componentInstance) => {
            flattenCapabilities = CapabilitiesGroup.getFlattenedCapabilities(componentInstance.capabilities);
            this.instancesCapabilitiesMap[componentInstance.uniqueId] = _.filter(flattenCapabilities, cap => cap.properties && cap.ownerId === componentInstance.uniqueId);
        });
    }

    private updateInstanceAttributes = (): void => {
        if (this.isComponentInstanceSelected && this.componentInstancesProperties) {
            this.instancesMappedList = this.compositionService.componentInstances.map((coInstance) => new ServiceInstanceObject({
                id: coInstance.uniqueId,
                name: coInstance.name,
                properties: this.componentInstancesProperties[coInstance.uniqueId] || [],
                inputs: this.componentInstancesInputs[coInstance.uniqueId] || [],
                interfaces: this.componentInstancesInterfaces[coInstance.uniqueId] || []
            }));
        }
    }

}
