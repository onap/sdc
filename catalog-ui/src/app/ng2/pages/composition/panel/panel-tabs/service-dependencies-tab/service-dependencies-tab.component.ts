/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {Component, Input, OnInit} from '@angular/core';
import {Store} from '@ngxs/store';
import {
    CapabilitiesGroup, Capability,
    Component as TopologyTemplate,
    FullComponentInstance,
    PropertiesGroup,
    PropertyBEModel, PropertyModel, ComponentInstance,
} from 'app/models';
import {ResourceType} from "app/utils";
import {DEPENDENCY_EVENTS} from 'app/utils/constants';
import {ComponentMetadata} from '../../../../../../models/component-metadata';
import {ServiceInstanceObject} from '../../../../../../models/service-instance-properties-and-interfaces';
import {EventListenerService} from '../../../../../../services/event-listener-service';
import {TopologyTemplateService} from '../../../../../services/component-services/topology-template.service';
import {ComponentInstanceServiceNg2} from '../../../../../services/component-instance-services/component-instance.service';
import {ComponentGenericResponse} from '../../../../../services/responses/component-generic-response';
import {WorkspaceService} from '../../../../workspace/workspace.service';
import {SelectedComponentType} from '../../../common/store/graph.actions';
import {CompositionService} from '../../../composition.service';
import {FilterConstraint} from "../../../../../../models/filter-constraint";

@Component({
    selector: 'service-dependencies-tab',
    templateUrl: 'service-dependencies-tab.component.html',
    styleUrls: ['service-dependencies-tab.component.less']
})
export class ServiceDependenciesTabComponent implements OnInit {
    isComponentInstanceSelected: boolean;

    selectedInstanceSiblings: ServiceInstanceObject[];
    componentInstancesConstraints: any[];
    selectedInstanceConstraints: FilterConstraint[];
    selectedInstanceProperties: PropertyBEModel[];
    componentInstanceProperties: PropertiesGroup;
    componentInstanceCapabilityProperties: CapabilitiesGroup;
    metaData: ComponentMetadata;
    componentInstanceCapabilitiesMap : Map<string, PropertyModel[]> = new Map();

    @Input() isViewOnly: boolean;
    @Input() componentType: SelectedComponentType;
    @Input() component: FullComponentInstance | TopologyTemplate;
    @Input() input: any;

    constructor(private store: Store,
                private topologyTemplateService: TopologyTemplateService,
                private componentInstanceServiceNg2: ComponentInstanceServiceNg2,
                private workspaceService: WorkspaceService,
                private compositionService: CompositionService,
                private eventListenerService: EventListenerService) {
    }

    ngOnInit(): void {
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

    public updateSelectedInstanceCapabilitiesConstraints = (constraintsList:Array<FilterConstraint>):void => {
        this.componentInstancesConstraints[this.component.uniqueId].capabilities = constraintsList;
        this.selectedInstanceConstraints = this.componentInstancesConstraints[this.component.uniqueId].capabilities;
    }

    private initInstancesWithProperties = (): void => {
        if (this.component instanceof FullComponentInstance && this.isInput(this.component.resourceType)) {
            this.componentInstanceServiceNg2
            .getComponentInstanceInputsByIdAndType(this.metaData.uniqueId, this.metaData.componentType, this.component as ComponentInstance)
            .subscribe(response => {
                this.selectedInstanceProperties = response;
            });
        } else {
            this.topologyTemplateService.getComponentInstanceProperties(this.metaData.componentType, this.metaData.uniqueId).subscribe((genericResponse: ComponentGenericResponse) => {
                this.componentInstanceProperties = genericResponse.componentInstancesProperties;
                this.updateInstanceAttributes();
            });
        }
    }

    private isInput = (instanceType:string):boolean =>{
        return instanceType === ResourceType.VF || instanceType === ResourceType.PNF || instanceType === ResourceType.CVFC || instanceType === ResourceType.CR;
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

    private initInstancesWithCapabilityProperties(): void {
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
