/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2020 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

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
    PropertiesGroup,
    PropertyBEModel,
} from 'app/models';
import { SUBSTITUTION_FILTER_EVENTS } from 'app/utils/constants';
import { ComponentMetadata } from '../../../../../../models/component-metadata';
import { ServiceInstanceObject } from '../../../../../../models/service-instance-properties-and-interfaces';
import { EventListenerService } from '../../../../../../services/event-listener-service';
import { ConstraintObject } from '../../../../../components/logic/service-dependencies/service-dependencies.component';
import { TopologyTemplateService } from '../../../../../services/component-services/topology-template.service';
import { ComponentGenericResponse } from '../../../../../services/responses/component-generic-response';
import { WorkspaceService } from '../../../../workspace/workspace.service';
import { SelectedComponentType } from '../../../common/store/graph.actions';
import { CompositionService } from '../../../composition.service';

@Component({
    selector: 'substitution-filter-tab',
    templateUrl: 'substitution-filter-tab.component.html',
    styleUrls: ['substitution-filter-tab.component.less']
})
export class SubstitutionFilterTabComponent {
    isComponentInstanceSelected: boolean;

    selectedInstanceSiblings: ServiceInstanceObject[];
    componentInstancesConstraints: any[];
    selectedInstanceConstraints: ConstraintObject[];
    selectedInstanceProperties: PropertyBEModel[];
    componentInstanceProperties: PropertiesGroup;
    metaData: ComponentMetadata;

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
    }

    public loadConstraints = (): void => {
        this.topologyTemplateService.getSubstitutionFilterConstraints(this.metaData.componentType, this.metaData.uniqueId).subscribe((response) => {
            this.componentInstancesConstraints = response.substitutionFilterForTopologyTemplate;
        });
    }

    public notifyDependencyEventsObserver = (isChecked: boolean): void => {
        this.eventListenerService.notifyObservers(SUBSTITUTION_FILTER_EVENTS.ON_SUBSTITUTION_FILTER_CHANGE, isChecked);
    }

    public updateSelectedInstanceConstraints = (constraintsList:Array<ConstraintObject>):void => {
        this.componentInstancesConstraints[this.component.uniqueId].properties = constraintsList;
        this.selectedInstanceConstraints = this.componentInstancesConstraints[this.component.uniqueId].properties;
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
}
