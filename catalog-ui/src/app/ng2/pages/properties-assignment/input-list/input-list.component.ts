/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

import {Component} from '@angular/core';
import {InputBEModel, ComponentMetadata} from 'app/models';
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {PropertiesService} from "../../../services/properties.service";

@Component({
    selector: 'input-list',
    templateUrl: './input-list.component.html',
    styleUrls: ['./input-list.component.less'],
})

export class InputListComponent {

    selectInputValue;
    inputModel: Array<InputBEModel> = [];
    isLoading: boolean;
    propertyType: string;

    private componentMetadata: ComponentMetadata;

    constructor(private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private propertiesService: PropertiesService
    ) {}

    ngOnInit() {
        this.componentMetadata = this.workspaceService.metadata;
        this.propertyType = this.propertiesService.getCheckedPropertyType();
        this.loadInputValues(this.propertyType);
    }

    private loadInputValues(propertyType: string): void {
        this.isLoading = true;
        this.topologyTemplateService.getComponentInputsValues(this.componentMetadata.componentType, this.componentMetadata.uniqueId)
        .subscribe((response) => {
            response.inputs.forEach((input: any) => {
                if (input.type === propertyType) {
                    this.inputModel.push(input);
                }
            });
        }, () => {
            //error ignored
        }, () => {
            this.isLoading = false;
        });
    }
}
