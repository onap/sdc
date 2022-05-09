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
import {
    ComponentMetadata, DataTypeModel, PropertyBEModel
} from 'app/models';
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {PropertiesService} from "../../../services/properties.service";
import {PROPERTY_DATA} from "../../../../utils/constants";
import {DataTypeService} from "../../../services/data-type.service";
import {ToscaGetFunctionType} from "../../../../models/tosca-get-function-type.enum";
import {TranslateService} from "../../../shared/translator/translate.service";

@Component({
    selector: 'tosca-function',
    templateUrl: './tosca-function.component.html',
    styleUrls: ['./tosca-function.component.less'],
})

export class ToscaFunctionComponent {

    selectToscaFunction;
    selectValue;
    isLoading: boolean;
    propertyType: string;
    dropdownValues: Array<PropertyBEModel> = [];
    toscaFunctions: Array<string> = [];
    dropdownValuesLabel: string;

    private dataTypeProperties: Array<PropertyBEModel> = [];
    private componentMetadata: ComponentMetadata;

    constructor(private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private propertiesService: PropertiesService,
                private dataTypeService: DataTypeService,
                private translateService: TranslateService) {
    }

    ngOnInit() {
        this.componentMetadata = this.workspaceService.metadata;
        this.propertyType = this.propertiesService.getCheckedPropertyType();
        this.loadToscaFunctions();
    }

    private loadToscaFunctions(): void {
        this.toscaFunctions.push(ToscaGetFunctionType.GET_INPUT.toLowerCase());
    }

    onToscaFunctionChange(): void {
        this.loadDropdownValueLabel();
        this.loadDropdownValues();
    }

    private loadDropdownValueLabel(): void {
        if (this.selectToscaFunction) {
            if (this.selectToscaFunction === ToscaGetFunctionType.GET_INPUT.toLowerCase()) {
                this.dropdownValuesLabel = this.translateService.translate('INPUT_DROPDOWN_LABEL');
            }
        }
    }

    private loadDropdownValues(): void {
        if (this.selectToscaFunction) {
            this.dropdownValues = [];
            if (this.selectToscaFunction === ToscaGetFunctionType.GET_INPUT.toLowerCase()) {
                this.loadInputValues(this.propertyType);
            }
        }
    }

    private loadInputValues(propertyType: string): void {
        this.isLoading = true;
        this.topologyTemplateService.getComponentInputsValues(this.componentMetadata.componentType, this.componentMetadata.uniqueId)
        .subscribe((response) => {
            response.inputs.forEach((inputProperty: any) => {
                if (propertyType === inputProperty.type) {
                    this.dropdownValues.push(inputProperty);
                } else if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(inputProperty.type) === -1 && inputProperty.type !== propertyType) {
                    this.buildInputDataForComplexType(inputProperty, propertyType);
                }
            });
        }, () => {
            //error ignored
        }, () => {
            this.isLoading = false;
        });
    }

    private buildInputDataForComplexType(inputProperty: PropertyBEModel, propertyType: string) {
        let dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, inputProperty.type);
        if (dataTypeFound && dataTypeFound.properties) {
            dataTypeFound.properties.forEach(dataTypeProperty => {
                let inputData = inputProperty.name + "->" + dataTypeProperty.name;
                dataTypeProperty.name = inputData;
                if (this.dataTypeProperties.indexOf(dataTypeProperty) === -1 && dataTypeProperty.type === propertyType) {
                    this.dropdownValues.push(dataTypeProperty);
                }
            });
        }
    }
}
