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

import {Component, Input} from '@angular/core';
import {ComponentMetadata, DataTypeModel, PropertyBEModel} from 'app/models';
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {PropertiesService} from "../../../services/properties.service";
import {PROPERTY_DATA} from "../../../../utils/constants";
import {DataTypeService} from "../../../services/data-type.service";
import {ToscaGetFunctionType} from "../../../../models/tosca-get-function-type";
import {TranslateService} from "../../../shared/translator/translate.service";
import {ComponentGenericResponse} from '../../../services/responses/component-generic-response';
import {Observable} from 'rxjs/Observable';

@Component({
    selector: 'tosca-function',
    templateUrl: './tosca-function.component.html',
    styleUrls: ['./tosca-function.component.less'],
})
export class ToscaFunctionComponent {

    @Input() property: PropertyBEModel;

    selectToscaFunction;
    selectedProperty: PropertyDropdownValue;
    isLoading: boolean = false;
    propertyDropdownList: Array<PropertyDropdownValue> = [];
    toscaFunctions: Array<string> = [];
    dropdownValuesLabel: string;
    dropDownErrorMsg: string;

    private componentMetadata: ComponentMetadata;

    constructor(private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private propertiesService: PropertiesService,
                private dataTypeService: DataTypeService,
                private translateService: TranslateService) {
    }

    ngOnInit() {
        this.componentMetadata = this.workspaceService.metadata;
        this.loadToscaFunctions();
    }

    private loadToscaFunctions(): void {
        this.toscaFunctions.push(ToscaGetFunctionType.GET_INPUT.toLowerCase());
        this.toscaFunctions.push(ToscaGetFunctionType.GET_PROPERTY.toLowerCase());
    }

    onToscaFunctionChange(): void {
        this.loadDropdownValueLabel();
        this.loadDropdownValues();
    }

    private loadDropdownValueLabel(): void {
        if (!this.selectToscaFunction) {
            return;
        }
        if (this.isGetInputSelected()) {
            this.dropdownValuesLabel = this.translateService.translate('INPUT_DROPDOWN_LABEL');
        } else if (this.isGetPropertySelected()) {
            this.dropdownValuesLabel = this.translateService.translate('TOSCA_FUNCTION_PROPERTY_DROPDOWN_LABEL');
        }
    }

    private loadDropdownValues(): void {
        if (!this.selectToscaFunction) {
            return;
        }
        this.resetDropDown();
        this.loadPropertiesInDropdown();
    }

    private resetDropDown() {
        this.dropDownErrorMsg = undefined;
        this.propertyDropdownList = [];
    }

    private loadPropertiesInDropdown() {
        this.startLoading();
        let propertiesObservable: Observable<ComponentGenericResponse>
        if (this.isGetInputSelected()) {
            propertiesObservable = this.topologyTemplateService.getComponentInputsValues(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
        } else if (this.isGetPropertySelected()) {
            propertiesObservable = this.topologyTemplateService.findAllComponentProperties(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
        }
        propertiesObservable
            .subscribe( (response: ComponentGenericResponse) => {
                let properties: PropertyBEModel[] = this.isGetInputSelected() ? response.inputs : response.properties;
                if (!properties || properties.length === 0) {
                    const msgCode = this.isGetInputSelected() ? 'TOSCA_FUNCTION_NO_INPUT_FOUND' : 'TOSCA_FUNCTION_NO_PROPERTY_FOUND';
                    this.dropDownErrorMsg = this.translateService.translate(msgCode, {type: this.property.type});
                    return;
                }
                this.addPropertiesToDropdown(properties);
                if (this.propertyDropdownList.length == 0) {
                    const msgCode = this.isGetInputSelected() ? 'TOSCA_FUNCTION_NO_INPUT_FOUND' : 'TOSCA_FUNCTION_NO_PROPERTY_FOUND';
                    this.dropDownErrorMsg = this.translateService.translate(msgCode, {type: this.property.type});
                }
            }, (error) => {
                console.error('An error occurred while loading properties.', error);
            }, () => {
                this.stopLoading();
            });
    }

    private addPropertyToDropdown(propertyDropdownValue: PropertyDropdownValue) {
        this.propertyDropdownList.push(propertyDropdownValue);
        this.propertyDropdownList.sort((a, b) => a.propertyLabel.localeCompare(b.propertyLabel));
    }

    private addPropertiesToDropdown(properties: PropertyBEModel[]) {
        for (const property of properties) {
            if (this.property.type === property.type) {
                this.addPropertyToDropdown({
                    propertyName: property.name,
                    propertyId: property.uniqueId,
                    propertyLabel: property.name,
                    toscaFunction: this.selectToscaFunction,
                    propertyPath: [property.name]
                });
            } else if (this.isComplexType(property.type)) {
                this.fillPropertyDropdownWithMatchingChildProperties(property);
            }
        }
    }

    private fillPropertyDropdownWithMatchingChildProperties(inputProperty: PropertyBEModel, parentPropertyList: Array<PropertyBEModel> = []) {
        const dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, inputProperty.type);
        if (!dataTypeFound || !dataTypeFound.properties) {
            return;
        }
        parentPropertyList.push(inputProperty);
        dataTypeFound.properties.forEach(dataTypeProperty => {
            if (dataTypeProperty.type === this.property.type) {
                this.addPropertyToDropdown({
                    propertyName: dataTypeProperty.name,
                    propertyId: parentPropertyList[0].uniqueId,
                    propertyLabel: parentPropertyList.map(property => property.name).join('->') + '->' + dataTypeProperty.name,
                    toscaFunction: this.selectToscaFunction,
                    propertyPath: [...parentPropertyList.map(property => property.name), dataTypeProperty.name]
                });
            } else if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(dataTypeProperty.type) === -1) {
                this.fillPropertyDropdownWithMatchingChildProperties(dataTypeProperty, [...parentPropertyList])
            }
        });
    }

    private isGetPropertySelected() {
        return this.selectToscaFunction === ToscaGetFunctionType.GET_PROPERTY.toLowerCase();
    }

    private isGetInputSelected() {
        return this.selectToscaFunction === ToscaGetFunctionType.GET_INPUT.toLowerCase();
    }

    private isComplexType(propertyType: string) {
        return PROPERTY_DATA.SIMPLE_TYPES.indexOf(propertyType) === -1;
    }

    private stopLoading() {
        this.isLoading = false;
    }

    private startLoading() {
        this.isLoading = true;
    }

    showDropdown(): boolean {
        return this.selectToscaFunction && !this.isLoading && !this.dropDownErrorMsg;
    }

}

export interface PropertyDropdownValue {
    propertyName: string;
    propertyId: string;
    propertyLabel: string;
    toscaFunction: ToscaGetFunctionType;
    propertyPath: Array<string>;
}
