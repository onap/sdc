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
import {ComponentMetadata, DataTypeModel, PropertyBEModel, PropertyModel} from 'app/models';
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {PropertiesService} from "../../../services/properties.service";
import {PROPERTY_DATA} from "../../../../utils/constants";
import {DataTypeService} from "../../../services/data-type.service";
import {ToscaGetFunctionType} from "../../../../models/tosca-get-function-type";
import {TranslateService} from "../../../shared/translator/translate.service";
import {ComponentGenericResponse} from '../../../services/responses/component-generic-response';
import {Observable} from 'rxjs/Observable';
import {PropertySource} from "../../../../models/property-source";
import {InstanceFeDetails} from "../../../../models/instance-fe-details";
import {ToscaGetFunction} from "../../../../models/tosca-get-function";

@Component({
    selector: 'tosca-function',
    templateUrl: './tosca-function.component.html',
    styleUrls: ['./tosca-function.component.less'],
})
export class ToscaFunctionComponent {

    @Input() property: PropertyBEModel;
    @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();

    TOSCA_FUNCTION_GET_PROPERTY = ToscaGetFunctionType.GET_PROPERTY;

    selectedProperty: PropertyDropdownValue;
    isLoading: boolean = false;
    propertyDropdownList: Array<PropertyDropdownValue> = [];
    toscaFunctions: Array<string> = [];
    propertySourceList: Array<string> = [];
    instanceNameAndIdMap: Map<string, string> = new Map<string, string>();
    dropdownValuesLabel: string;
    dropDownErrorMsg: string;
    propertySource: string
    toscaGetFunction: ToscaGetFunction = new ToscaGetFunction();

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
        this.loadPropertySourceDropdown();
    }

    private loadToscaFunctions(): void {
        this.toscaFunctions.push(ToscaGetFunctionType.GET_INPUT);
        this.toscaFunctions.push(ToscaGetFunctionType.GET_PROPERTY);
    }

    private loadPropertySourceDropdown() {
        this.propertySourceList.push(PropertySource.SELF);
        this.componentInstanceMap.forEach((value, key) => {
            const instanceName = value.name;
            this.instanceNameAndIdMap.set(instanceName, key);
            if (instanceName !== PropertySource.SELF) {
                this.addToPropertySource(instanceName);
            }
        });
    }

    private addToPropertySource(source: string) {
        this.propertySourceList.push(source);
        this.propertySourceList.sort((a, b) => {
            if (a === PropertySource.SELF) {
                return -1;
            } else if (b === PropertySource.SELF) {
                return 1;
            }

            return a.localeCompare(b);
        });
    }

    onToscaFunctionChange(): void {
        this.toscaGetFunction.propertyUniqueId = undefined;
        this.toscaGetFunction.propertyName = undefined;
        this.toscaGetFunction.propertySource = undefined;
        this.toscaGetFunction.sourceUniqueId = undefined;
        this.toscaGetFunction.sourceName = undefined;
        this.toscaGetFunction.propertyPathFromSource = undefined;
        this.propertySource = undefined;
        if (this.isGetInputSelected()) {
            this.setSelfPropertySource();
            this.loadDropdownValueLabel();
            this.loadDropdownValues();
        }
    }

    private loadDropdownValueLabel(): void {
        if (!this.toscaGetFunction.functionType) {
            return;
        }
        if (this.isGetInputSelected()) {
            this.dropdownValuesLabel = this.translateService.translate('INPUT_DROPDOWN_LABEL');
        } else if (this.isGetPropertySelected()) {
            this.dropdownValuesLabel = this.translateService.translate('TOSCA_FUNCTION_PROPERTY_DROPDOWN_LABEL');
        }
    }

    private loadDropdownValues(): void {
        if (!this.toscaGetFunction.functionType) {
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
        const propertiesObservable: Observable<ComponentGenericResponse> = this.getPropertyObservable();
        propertiesObservable.subscribe( (response: ComponentGenericResponse) => {
            const properties: PropertyBEModel[] = this.extractProperties(response);
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

    private extractProperties(componentGenericResponse: ComponentGenericResponse): PropertyBEModel[] {
        if (this.isGetInputSelected()) {
            return componentGenericResponse.inputs;
        }
        if (this.isGetPropertySelected()) {
            if (this.propertySource === PropertySource.SELF) {
                return componentGenericResponse.properties;
            }
            const componentInstanceProperties: PropertyModel[] = componentGenericResponse.componentInstancesProperties[this.instanceNameAndIdMap.get(this.propertySource)];
            return this.removeSelectedProperty(componentInstanceProperties);
        }
    }

    private getPropertyObservable(): Observable<ComponentGenericResponse> {
        if (this.isGetInputSelected()) {
            return this.topologyTemplateService.getComponentInputsValues(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
        }
        if (this.isGetPropertySelected()) {
            if (this.propertySource === PropertySource.SELF) {
                return this.topologyTemplateService.findAllComponentProperties(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
            }
            return this.topologyTemplateService.getComponentInstanceProperties(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
        }
    }

    private removeSelectedProperty(componentInstanceProperties: PropertyModel[]): PropertyModel[] {
        if (!componentInstanceProperties) {
            return [];
        }
        return componentInstanceProperties.filter(property =>
            (property.uniqueId !== this.property.uniqueId) ||
            (property.uniqueId === this.property.uniqueId && property.resourceInstanceUniqueId !== this.property.parentUniqueId)
        );
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
                    propertyPath: [...parentPropertyList.map(property => property.name), dataTypeProperty.name]
                });
            } else if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(dataTypeProperty.type) === -1) {
                this.fillPropertyDropdownWithMatchingChildProperties(dataTypeProperty, [...parentPropertyList])
            }
        });
    }

    private isGetPropertySelected() {
        return this.toscaGetFunction.functionType === ToscaGetFunctionType.GET_PROPERTY;
    }

    private isGetInputSelected() {
        return this.toscaGetFunction.functionType === ToscaGetFunctionType.GET_INPUT;
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
        if (this.toscaGetFunction.functionType === ToscaGetFunctionType.GET_PROPERTY) {
            return this.toscaGetFunction.propertySource && !this.isLoading && !this.dropDownErrorMsg;
        }

        return this.toscaGetFunction.functionType && !this.isLoading && !this.dropDownErrorMsg;
    }

    onPropertySourceChange() {
        if (!this.toscaGetFunction.functionType || !this.propertySource) {
            return;
        }
        this.toscaGetFunction.propertyUniqueId = undefined;
        this.toscaGetFunction.propertyName = undefined;
        this.toscaGetFunction.propertyPathFromSource = undefined;
        if (this.propertySource === PropertySource.SELF) {
            this.setSelfPropertySource();
        } else {
            this.toscaGetFunction.propertySource = PropertySource.INSTANCE;
            this.toscaGetFunction.sourceName = this.propertySource;
            this.toscaGetFunction.sourceUniqueId = this.instanceNameAndIdMap.get(this.propertySource);
        }
        this.loadDropdownValueLabel();
        this.resetDropDown();
        this.loadPropertiesInDropdown();
    }

    private setSelfPropertySource() {
        this.toscaGetFunction.propertySource = PropertySource.SELF;
        this.toscaGetFunction.sourceName = this.componentMetadata.name;
        this.toscaGetFunction.sourceUniqueId = this.componentMetadata.uniqueId;
    }

    onPropertyChange() {
        this.toscaGetFunction.propertyUniqueId = this.selectedProperty.propertyId;
        this.toscaGetFunction.propertyName = this.selectedProperty.propertyName;
        this.toscaGetFunction.propertyPathFromSource = this.selectedProperty.propertyPath;
    }

}

export interface PropertyDropdownValue {
    propertyName: string;
    propertyId: string;
    propertyLabel: string;
    propertyPath: Array<string>;
}
