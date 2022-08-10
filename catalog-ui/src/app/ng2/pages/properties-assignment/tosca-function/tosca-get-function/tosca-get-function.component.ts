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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {AttributeModel, ComponentMetadata, DataTypeModel, PropertyBEModel, PropertyModel} from 'app/models';
import {TopologyTemplateService} from "../../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../../workspace/workspace.service";
import {PropertiesService} from "../../../../services/properties.service";
import {PROPERTY_DATA, PROPERTY_TYPES} from "../../../../../utils/constants";
import {DataTypeService} from "../../../../services/data-type.service";
import {ToscaGetFunctionType} from "../../../../../models/tosca-get-function-type";
import {TranslateService} from "../../../../shared/translator/translate.service";
import {ComponentGenericResponse} from '../../../../services/responses/component-generic-response';
import {Observable} from 'rxjs/Observable';
import {PropertySource} from "../../../../../models/property-source";
import {InstanceFeDetails} from "../../../../../models/instance-fe-details";
import {ToscaGetFunction} from "../../../../../models/tosca-get-function";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ToscaGetFunctionTypeConverter} from "../../../../../models/tosca-get-function-type-converter";

@Component({
    selector: 'app-tosca-get-function',
    templateUrl: './tosca-get-function.component.html',
    styleUrls: ['./tosca-get-function.component.less']
})
export class ToscaGetFunctionComponent implements OnInit, OnChanges {

    @Input() property: PropertyBEModel;
    @Input() toscaGetFunction: ToscaGetFunction;
    @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
    @Input() functionType: ToscaGetFunctionType;
    @Output() onValidFunction: EventEmitter<ToscaGetFunction> = new EventEmitter<ToscaGetFunction>();
    @Output() onValidityChange: EventEmitter<ToscaGetFunctionValidationEvent> = new EventEmitter<ToscaGetFunctionValidationEvent>();

    formGroup: FormGroup = new FormGroup({
        'selectedProperty': new FormControl(undefined, Validators.required),
        'propertySource': new FormControl(undefined, Validators.required)
    });

    isLoading: boolean = false;
    propertyDropdownList: Array<PropertyDropdownValue> = [];
    propertySourceList: Array<string> = [];
    instanceNameAndIdMap: Map<string, string> = new Map<string, string>();
    dropdownValuesLabel: string;
    dropDownErrorMsg: string;

    private isInitialized: boolean = false;
    private componentMetadata: ComponentMetadata;

    constructor(private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private propertiesService: PropertiesService,
                private dataTypeService: DataTypeService,
                private translateService: TranslateService) {
    }

    ngOnInit(): void {
        this.componentMetadata = this.workspaceService.metadata;
        this.formGroup.valueChanges.subscribe(() => {
            if (!this.isInitialized) {
                return;
            }
            this.onValidityChange.emit({
                isValid: this.formGroup.valid,
                toscaGetFunction: this.formGroup.valid ? this.buildGetFunctionFromForm() : undefined
            });
            if (this.formGroup.valid) {
                this.onValidFunction.emit(this.buildGetFunctionFromForm());
            }
        });
        this.loadPropertySourceDropdown();
        this.loadPropertyDropdownLabel();
        this.initToscaGetFunction().subscribe(() => {
            this.isInitialized = true;
        });

    }

    ngOnChanges(_changes: SimpleChanges): void {
        if (!this.isInitialized) {
            return;
        }
        this.isInitialized = false;
        this.resetForm();
        this.loadPropertySourceDropdown();
        this.loadPropertyDropdownLabel();
        this.initToscaGetFunction().subscribe(() => {
            this.isInitialized = true;
        });
    }

    private initToscaGetFunction(): Observable<void> {
        return new Observable(subscriber => {
            if (!this.toscaGetFunction) {
                if (this.isGetInput()) {
                    this.setSelfPropertySource();
                    this.loadPropertyDropdown();
                }
                subscriber.next();
                return;
            }
            if (this.toscaGetFunction.propertySource == PropertySource.SELF) {
                this.propertySource.setValue(PropertySource.SELF);
            } else if (this.toscaGetFunction.propertySource == PropertySource.INSTANCE) {
                this.propertySource
                .setValue(this.propertySourceList.find(source => this.toscaGetFunction.sourceName === source));
            }
            if (this.propertySource.valid) {
                this.loadPropertyDropdown(() => {
                    this.selectedProperty
                    .setValue(this.propertyDropdownList.find(property => property.propertyName === this.toscaGetFunction.propertyName));
                    subscriber.next();
                });
            } else {
                subscriber.next();
            }
        });
    }

    private buildGetFunctionFromForm() {
        const toscaGetFunction = new ToscaGetFunction();
        toscaGetFunction.type = ToscaGetFunctionTypeConverter.convertToToscaFunctionType(this.functionType);
        toscaGetFunction.functionType = this.functionType;
        const propertySource = this.propertySource.value;
        if (this.isPropertySourceSelf()) {
            toscaGetFunction.propertySource = propertySource
            toscaGetFunction.sourceName = this.componentMetadata.name;
            toscaGetFunction.sourceUniqueId = this.componentMetadata.uniqueId;
        } else {
            toscaGetFunction.propertySource = PropertySource.INSTANCE;
            toscaGetFunction.sourceName = propertySource;
            toscaGetFunction.sourceUniqueId = this.instanceNameAndIdMap.get(propertySource);
        }

        const selectedProperty: PropertyDropdownValue = this.selectedProperty.value;
        toscaGetFunction.propertyUniqueId = selectedProperty.propertyId;
        toscaGetFunction.propertyName = selectedProperty.propertyName;
        toscaGetFunction.propertyPathFromSource = selectedProperty.propertyPath;

        return toscaGetFunction;
    }

    private loadPropertySourceDropdown(): void {
        if (this.isGetInput()) {
            return;
        }
        this.propertySourceList = [];
        this.propertySourceList.push(PropertySource.SELF);
        this.componentInstanceMap.forEach((value, key) => {
            const instanceName = value.name;
            this.instanceNameAndIdMap.set(instanceName, key);
            if (instanceName !== PropertySource.SELF) {
                this.addToPropertySource(instanceName);
            }
        });
    }

    private addToPropertySource(source: string): void {
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

    private loadPropertyDropdown(onComplete?: () => any): void  {
        this.loadPropertyDropdownLabel();
        this.loadPropertyDropdownValues(onComplete);
    }

    private resetForm(): void {
        this.formGroup.reset();
    }

    private loadPropertyDropdownLabel(): void {
        if (!this.functionType) {
            return;
        }
        if (this.isGetInput()) {
            this.dropdownValuesLabel = this.translateService.translate('INPUT_DROPDOWN_LABEL');
        } else if (this.isGetProperty()) {
            this.dropdownValuesLabel = this.translateService.translate('TOSCA_FUNCTION_PROPERTY_DROPDOWN_LABEL');
        } else if (this.isGetAttribute()) {
            this.dropdownValuesLabel = this.translateService.translate('TOSCA_FUNCTION_ATTRIBUTE_DROPDOWN_LABEL');
        }
    }

    private loadPropertyDropdownValues(onComplete?: () => any): void {
        if (!this.functionType) {
            return;
        }
        this.resetPropertyDropdown();
        this.fillPropertyDropdownValues(onComplete);
    }

    private resetPropertyDropdown(): void {
        this.dropDownErrorMsg = undefined;
        this.selectedProperty.reset();
        this.propertyDropdownList = [];
    }

    private fillPropertyDropdownValues(onComplete?: () => any): void {
        this.startLoading();
        const propertiesObservable: Observable<ComponentGenericResponse> = this.getPropertyObservable();
        propertiesObservable.subscribe( (response: ComponentGenericResponse) => {
            const properties: Array<PropertyBEModel | AttributeModel> = this.extractProperties(response);
            if (!properties || properties.length === 0) {
                const msgCode = this.getNotFoundMsgCode();
                this.dropDownErrorMsg = this.translateService.translate(msgCode, {type: this.propertyTypeToString()});
                return;
            }
            this.addPropertiesToDropdown(properties);
            if (this.propertyDropdownList.length == 0) {
                const msgCode = this.getNotFoundMsgCode();
                this.dropDownErrorMsg = this.translateService.translate(msgCode, {type: this.propertyTypeToString()});
            }
        }, (error) => {
            console.error('An error occurred while loading properties.', error);
            this.stopLoading();
        }, () => {
            if (onComplete) {
                onComplete();
            }
            this.stopLoading();
        });
    }

    private getNotFoundMsgCode(): string {
        if (this.isGetInput()) {
            return 'TOSCA_FUNCTION_NO_INPUT_FOUND';
        }
        if (this.isGetAttribute()) {
            return 'TOSCA_FUNCTION_NO_ATTRIBUTE_FOUND';
        }
        if (this.isGetProperty()) {
            return 'TOSCA_FUNCTION_NO_PROPERTY_FOUND';
        }

        return undefined;
    }

    private propertyTypeToString() {
        if (this.property.schemaType) {
            return `${this.property.type} of ${this.property.schemaType}`;
        }
        return this.property.type;
    }

    private extractProperties(componentGenericResponse: ComponentGenericResponse): Array<PropertyBEModel | AttributeModel> {
        if (this.isGetInput()) {
            return componentGenericResponse.inputs;
        }
        const propertySource = this.propertySource.value;
        if (this.isGetProperty()) {
            if (this.isPropertySourceSelf()) {
                return componentGenericResponse.properties;
            }
            const componentInstanceProperties: PropertyModel[] = componentGenericResponse.componentInstancesProperties[this.instanceNameAndIdMap.get(propertySource)];
            return this.removeSelectedProperty(componentInstanceProperties);
        }
        if (this.isPropertySourceSelf()) {
            return componentGenericResponse.attributes;
        }
        return componentGenericResponse.componentInstancesAttributes[this.instanceNameAndIdMap.get(propertySource)];
    }

    private isPropertySourceSelf() {
        return this.propertySource.value === PropertySource.SELF;
    }

    private getPropertyObservable(): Observable<ComponentGenericResponse> {
        if (this.isGetInput()) {
            return this.topologyTemplateService.getComponentInputsValues(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
        }
        if (this.isGetProperty()) {
            if (this.isPropertySourceSelf()) {
                return this.topologyTemplateService.findAllComponentProperties(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
            }
            return this.topologyTemplateService.getComponentInstanceProperties(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
        }
        if (this.isGetAttribute()) {
            if (this.isPropertySourceSelf()) {
                return this.topologyTemplateService.findAllComponentAttributes(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
            }
            return this.topologyTemplateService.findAllComponentInstanceAttributes(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
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

    private addPropertyToDropdown(propertyDropdownValue: PropertyDropdownValue): void {
        this.propertyDropdownList.push(propertyDropdownValue);
        this.propertyDropdownList.sort((a, b) => a.propertyLabel.localeCompare(b.propertyLabel));
    }

    private addPropertiesToDropdown(properties: Array<PropertyBEModel | AttributeModel>): void {
        for (const property of properties) {
            if (this.hasSameType(property)) {
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

    private fillPropertyDropdownWithMatchingChildProperties(inputProperty: PropertyBEModel | AttributeModel,
                                                            parentPropertyList: Array<PropertyBEModel | AttributeModel> = []): void {
        const dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, inputProperty.type);
        if (!dataTypeFound || !dataTypeFound.properties) {
            return;
        }
        parentPropertyList.push(inputProperty);
        dataTypeFound.properties.forEach(dataTypeProperty => {
            if (this.hasSameType(dataTypeProperty)) {
                this.addPropertyToDropdown({
                    propertyName: dataTypeProperty.name,
                    propertyId: parentPropertyList[0].uniqueId,
                    propertyLabel: parentPropertyList.map(property => property.name).join('->') + '->' + dataTypeProperty.name,
                    propertyPath: [...parentPropertyList.map(property => property.name), dataTypeProperty.name]
                });
            } else if (this.isComplexType(dataTypeProperty.type)) {
                this.fillPropertyDropdownWithMatchingChildProperties(dataTypeProperty, [...parentPropertyList])
            }
        });
    }

    private hasSameType(property: PropertyBEModel | AttributeModel) {
        if (this.typeHasSchema(this.property.type)) {
            if (!property.schema || !property.schema.property) {
                return false;
            }
            return property.type === this.property.type && this.property.schema.property.type === property.schema.property.type;
        }

        return property.type === this.property.type;
    }

    private isGetProperty(): boolean {
        return this.functionType === ToscaGetFunctionType.GET_PROPERTY;
    }

    private isGetAttribute(): boolean {
        return this.functionType === ToscaGetFunctionType.GET_ATTRIBUTE;
    }

    private isGetInput(): boolean {
        return this.functionType === ToscaGetFunctionType.GET_INPUT;
    }

    private isComplexType(propertyType: string): boolean {
        return PROPERTY_DATA.SIMPLE_TYPES.indexOf(propertyType) === -1;
    }

    private typeHasSchema(propertyType: string): boolean {
        return PROPERTY_TYPES.MAP === propertyType || PROPERTY_TYPES.LIST === propertyType;
    }

    private stopLoading(): void {
        this.isLoading = false;
    }

    private startLoading(): void {
        this.isLoading = true;
    }

    showPropertyDropdown(): boolean {
        if (this.isGetProperty() || this.isGetAttribute()) {
            return this.propertySource.valid && !this.isLoading && !this.dropDownErrorMsg;
        }

        return this.functionType && !this.isLoading && !this.dropDownErrorMsg;
    }

    onPropertySourceChange(): void {
        this.selectedProperty.reset();
        if (!this.functionType || !this.propertySource.valid) {
            return;
        }
        this.loadPropertyDropdown();
    }

    showPropertySourceDropdown(): boolean {
        return this.isGetProperty() || this.isGetAttribute();
    }

    private setSelfPropertySource(): void {
        this.propertySource.setValue(PropertySource.SELF);
    }

    private get propertySource(): FormControl {
        return this.formGroup.get('propertySource') as FormControl;
    }

    private get selectedProperty(): FormControl {
        return this.formGroup.get('selectedProperty') as FormControl;
    }

}

export interface PropertyDropdownValue {
    propertyName: string;
    propertyId: string;
    propertyLabel: string;
    propertyPath: Array<string>;
}

export interface ToscaGetFunctionValidationEvent {
    isValid: boolean,
    toscaGetFunction: ToscaGetFunction,
}