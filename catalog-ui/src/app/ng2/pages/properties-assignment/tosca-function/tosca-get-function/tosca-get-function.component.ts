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
import {AttributeBEModel, ComponentMetadata, DataTypeModel, PropertyBEModel, PropertyModel, PropertyDeclareAPIModel, DerivedFEProperty} from 'app/models';
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
    @Input() overridingType: PROPERTY_TYPES;
    @Input() toscaGetFunction: ToscaGetFunction;
    @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
    @Input() functionType: ToscaGetFunctionType;
    @Input() compositionMap: boolean;
    @Input() compositionMapKey: string;
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
    indexListValues:Array<ToscaIndexObject>;
    parentListTypeFlag : boolean;

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
        this.indexListValues = [];
        if (this.property != null) {
            this.parentListTypeFlag = (this.property.type != PROPERTY_TYPES.LIST && (!this.isComplexType(this.property.type) || (this.isComplexType(this.property.type) 
                                    && this.property instanceof PropertyDeclareAPIModel && (<PropertyDeclareAPIModel> this.property).input instanceof DerivedFEProperty && this.property.input.type != PROPERTY_TYPES.LIST)));
        }
        this.formGroup.valueChanges.subscribe(() => {
            this.formValidation();
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
                    if (this.toscaGetFunction.toscaIndexList && this.toscaGetFunction.toscaIndexList.length > 0) {
                        let tempSelectedProperty : PropertyDropdownValue = this.selectedProperty.value;
                        this.toscaGetFunction.toscaIndexList.forEach((indexValue: string, index) => {
                            let tempIndexFlag = false;
                            let tempNestedFlag = false;
                            let tempIndexValue = "0";
                            let tempIndexProperty = tempSelectedProperty;
                            let subPropertyDropdownList : Array<PropertyDropdownValue> = [];
                            if (index%2 == 0) {
                                tempIndexFlag = true;
                                tempIndexValue = indexValue;
                                tempSelectedProperty = null;
                                if (this.toscaGetFunction.toscaIndexList[index+1]) {
                                    tempNestedFlag = true;
                                    if (tempIndexProperty.schemaType != null) {
                                        const dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, tempIndexProperty.schemaType);
                                        this.addPropertiesToDropdown(dataTypeFound.properties, subPropertyDropdownList);
                                        tempSelectedProperty = subPropertyDropdownList.find(property => property.propertyName === this.toscaGetFunction.toscaIndexList[index+1])
                                    }
                                }
                                let tempIndexValueMap : ToscaIndexObject = {indexFlag : tempIndexFlag, nestedFlag : tempNestedFlag, indexValue: tempIndexValue, indexProperty: tempSelectedProperty, subPropertyArray: subPropertyDropdownList};
                                this.indexListValues.push(tempIndexValueMap);
                            }
                        });
                    }
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
        if (this.indexListValues.length > 0) {
            let indexAndProperty : Array<string> = [];
            this.indexListValues.forEach((indexObject : ToscaIndexObject) => {
                indexAndProperty.push(indexObject.indexValue);
                if(indexObject.nestedFlag && indexObject.indexProperty != null) {
                    indexAndProperty.push(indexObject.indexProperty.propertyName);
                }
            });
            toscaGetFunction.toscaIndexList = indexAndProperty;
        }
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

    private formValidation(): void {
        if (!this.isInitialized) {
            return;
        }
        let formGroupStatus : boolean = this.formGroup.valid;
        const selectedProperty: PropertyDropdownValue = this.formGroup.value.selectedProperty;
        if (selectedProperty != null && selectedProperty.isList && formGroupStatus && this.indexListValues.length > 0) {
            this.indexListValues.forEach((indexObject : ToscaIndexObject, index) => {
                if (indexObject.indexValue == '') {
                    formGroupStatus = false;
                    return;
                }
                if (indexObject.nestedFlag && indexObject.indexProperty == null) {
                    formGroupStatus = false;
                    return;
                }
            });
        }
        this.onValidityChange.emit({
            isValid: formGroupStatus,
            toscaGetFunction: this.formGroup.valid ? this.buildGetFunctionFromForm() : undefined
        });
        if (this.formGroup.valid) {
            this.onValidFunction.emit(this.buildGetFunctionFromForm());
        }
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
        this.indexListValues = [];
        this.propertyDropdownList = [];
    }

    private fillPropertyDropdownValues(onComplete?: () => any): void {
        this.startLoading();
        const propertiesObservable: Observable<ComponentGenericResponse> = this.getPropertyObservable();
        propertiesObservable.subscribe( (response: ComponentGenericResponse) => {
            const properties: Array<PropertyBEModel | AttributeBEModel> = this.extractProperties(response);
            if (!properties || properties.length === 0) {
                const msgCode = this.getNotFoundMsgCode();
                this.dropDownErrorMsg = this.translateService.translate(msgCode, {type: this.overridingType != undefined ? this.overridingType : this.propertyTypeToString()});
                return;
            }
            this.addPropertiesToDropdown(properties, this.propertyDropdownList);
            if (this.propertyDropdownList.length == 0) {
                const msgCode = this.getNotFoundMsgCode();
                this.dropDownErrorMsg = this.translateService.translate(msgCode, {type: this.overridingType != undefined ? this.overridingType : this.propertyTypeToString()});
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
	    if (this.isSubProperty()){
            if ((this.property instanceof PropertyDeclareAPIModel && (<PropertyDeclareAPIModel> this.property).input instanceof DerivedFEProperty)
                || this.compositionMap) {
                if(this.isComplexType(this.property.schemaType) && !this.compositionMap){
                    let mapChildProp : DerivedFEProperty = (<DerivedFEProperty> (<PropertyDeclareAPIModel> this.property).input);
                    let propertySchemaType = mapChildProp.type;
                    if (this.property.type == PROPERTY_TYPES.MAP || propertySchemaType == PROPERTY_TYPES.MAP) {
                        if (mapChildProp.mapKey != '' && mapChildProp.mapKey != null && mapChildProp.schema.property.type != null) {
                            propertySchemaType = mapChildProp.schema.property.type;
                        }
                    }
                    if ((propertySchemaType == PROPERTY_TYPES.MAP || (propertySchemaType == PROPERTY_TYPES.LIST && mapChildProp.schema.property.type == PROPERTY_TYPES.MAP))
                        && mapChildProp.isChildOfListOrMap) {
                        propertySchemaType = PROPERTY_TYPES.STRING;
                    }
                    return  propertySchemaType;
                }else{
                    return this.property.schema.property.type;
                }
            }
	        return this.getType((<PropertyDeclareAPIModel>this.property).propertiesName.split("#").slice(1),  this.property.type);
        }
        if (this.property.schemaType) {
            return `${this.property.type} of ${this.property.schemaType}`;
        }
        return this.property.type;
    }

    private isSubProperty(): boolean{
	    return this.property instanceof PropertyDeclareAPIModel && (<PropertyDeclareAPIModel>this.property).propertiesName && (<PropertyDeclareAPIModel>this.property).propertiesName.length > 1;
    }

    private extractProperties(componentGenericResponse: ComponentGenericResponse): Array<PropertyBEModel | AttributeBEModel> {
        if (this.isGetInput()) {
            return componentGenericResponse.inputs;
        }
        const instanceId = this.instanceNameAndIdMap.get(this.propertySource.value);
        if (this.isGetProperty()) {
            if (this.isPropertySourceSelf()) {
                return componentGenericResponse.properties;
            }
            return this.removeSelectedProperty(componentGenericResponse.componentInstancesProperties[instanceId]);
        }
        if (this.isPropertySourceSelf()) {
            return [...(componentGenericResponse.attributes || []), ...(componentGenericResponse.properties || [])];
        }
        return [...(componentGenericResponse.componentInstancesAttributes[instanceId] || []),
            ...(componentGenericResponse.componentInstancesProperties[instanceId] || [])];
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
                return this.topologyTemplateService.findAllComponentAttributesAndProperties(this.componentMetadata.componentType, this.componentMetadata.uniqueId);
            }
            return this.topologyTemplateService.getComponentInstanceAttributesAndProperties(this.componentMetadata.uniqueId, this.componentMetadata.componentType);
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

    private addPropertyToDropdown(propertyDropdownValue: PropertyDropdownValue, propertyList: Array<PropertyDropdownValue>): void {
        propertyList.push(propertyDropdownValue);
        propertyList.sort((a, b) => a.propertyLabel.localeCompare(b.propertyLabel));
    }

    private addPropertiesToDropdown(properties: Array<PropertyBEModel | AttributeBEModel>, propertyList: Array<PropertyDropdownValue>): void {
        for (const property of properties) {
            if (this.hasSameType(property)) {
                this.addPropertyToDropdown({
                    propertyName: property.name,
                    propertyId: property.uniqueId,
                    propertyLabel: property.name,
                    propertyPath: [property.name],
                    isList: property.type === PROPERTY_TYPES.LIST,
                    schemaType: (property.type === PROPERTY_TYPES.LIST && this.isComplexType(property.schema.property.type)) ? property.schema.property.type : null
                },propertyList);
            } else if (this.isComplexType(property.type)) {
                this.fillPropertyDropdownWithMatchingChildProperties(property,propertyList);
            }
        }
    }

    private fillPropertyDropdownWithMatchingChildProperties(inputProperty: PropertyBEModel | AttributeBEModel, propertyList: Array<PropertyDropdownValue>,
                                                            parentPropertyList: Array<PropertyBEModel | AttributeBEModel> = []): void {
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
                    propertyPath: [...parentPropertyList.map(property => property.name), dataTypeProperty.name],
                    isList : dataTypeProperty.type === PROPERTY_TYPES.LIST,
                    schemaType: (dataTypeProperty.type === PROPERTY_TYPES.LIST && this.isComplexType(dataTypeProperty.schema.property.type)) ? dataTypeProperty.schema.property.type : null
                }, propertyList);
            } else if (this.isComplexType(dataTypeProperty.type)) {
                this.fillPropertyDropdownWithMatchingChildProperties(dataTypeProperty, propertyList, [...parentPropertyList])
            }
        });
    }

    private hasSameType(property: PropertyBEModel | AttributeBEModel): boolean {
        if (this.overridingType != undefined) {
            return property.type === this.overridingType;
        }
        if (this.property.type === PROPERTY_TYPES.ANY) {
            return true;
        }
        let validPropertyType = (this.parentListTypeFlag && property.type === PROPERTY_TYPES.LIST) ? property.schema.property.type : property.type;
        if (this.parentListTypeFlag && property.type === PROPERTY_TYPES.LIST && this.isComplexType(validPropertyType)) {
            let returnFlag : boolean = false;
            const dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, validPropertyType);
            if (dataTypeFound && dataTypeFound.properties) {
                dataTypeFound.properties.forEach(dataTypeProperty => {
                    if (this.hasSameType(dataTypeProperty)) {
                        returnFlag =  true;
                    }
                });
            }
            return returnFlag;
        }
        if (this.typeHasSchema(this.property.type)) {
            if ((this.property instanceof PropertyDeclareAPIModel && (<PropertyDeclareAPIModel> this.property).input instanceof DerivedFEProperty) || this.compositionMap) {
                let childObject : DerivedFEProperty = (<DerivedFEProperty>(<PropertyDeclareAPIModel> this.property).input);
                let childSchemaType = this.property.schemaType != null ? this.property.schemaType : childObject.type;
                if(this.isComplexType(childSchemaType) && !this.compositionMap){
                    if (childObject.type == PROPERTY_TYPES.MAP && childObject.isChildOfListOrMap) {
                        return validPropertyType === PROPERTY_TYPES.STRING;
                    }
                    return validPropertyType === childObject.type;
                }else{
                    return validPropertyType === this.property.schema.property.type;
                }
            }
            if (!property.schema || !property.schema.property) {
                return false;
            }
            return validPropertyType === this.property.type && this.property.schema.property.type === property.schema.property.type;
        }
        if (this.property.schema.property.isDataType && this.property instanceof PropertyDeclareAPIModel && (<PropertyDeclareAPIModel>this.property).propertiesName){
            let typeToMatch = (<PropertyDeclareAPIModel> this.property).input.type;
            let childObject : DerivedFEProperty = (<DerivedFEProperty>(<PropertyDeclareAPIModel> this.property).input);
            if (childObject.type == PROPERTY_TYPES.MAP && childObject.isChildOfListOrMap) {
                typeToMatch = PROPERTY_TYPES.STRING;
            }
            return validPropertyType === typeToMatch;
        }

        return validPropertyType === this.property.type;
    }

    private getType(propertyPath:string[], type: string): string {
	    const dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, type);
        let nestedProperty = dataTypeFound.properties.find(property => property.name === propertyPath[0]);
        if (propertyPath.length === 1){
	        return nestedProperty.type;
        } 
        return this.getType(propertyPath.slice(1), nestedProperty.type);
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
        this.indexListValues = [];
        if (!this.functionType || !this.propertySource.valid) {
            return;
        }
        this.loadPropertyDropdown();
    }

    onPropertyValueChange(): void {
        let toscaIndexFlag = false;
        let nestedToscaFlag = false;
        this.indexListValues = [];
        let subPropertyDropdownList : Array<PropertyDropdownValue> = [];
        const selectedProperty: PropertyDropdownValue = this.selectedProperty.value;
        if (this.parentListTypeFlag && selectedProperty.isList) {
            toscaIndexFlag = true;
            if (selectedProperty.schemaType != null) {
                nestedToscaFlag = true;
                const dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, selectedProperty.schemaType);
                this.addPropertiesToDropdown(dataTypeFound.properties, subPropertyDropdownList);
            }
        }
        if (toscaIndexFlag || nestedToscaFlag) {
            let indexValueMap : ToscaIndexObject = {indexFlag : toscaIndexFlag, nestedFlag : nestedToscaFlag, indexValue: "0", indexProperty: null, subPropertyArray: subPropertyDropdownList};
            this.indexListValues.push(indexValueMap);
        }
        this.formValidation();
    }

    onSubPropertyValueChange(indexObject : ToscaIndexObject, elementIndex: number): void {
        let toscaIndexFlag = false;
        let nestedToscaFlag = false;
        let subPropertyDropdownList : Array<PropertyDropdownValue> = [];
        let selectedProperty: PropertyDropdownValue = indexObject.indexProperty;
        if (selectedProperty.isList) {
            toscaIndexFlag = true;
            if (selectedProperty.schemaType != null) {
                nestedToscaFlag = true;
                const dataTypeFound: DataTypeModel = this.dataTypeService.getDataTypeByModelAndTypeName(this.componentMetadata.model, selectedProperty.schemaType);
                this.addPropertiesToDropdown(dataTypeFound.properties, subPropertyDropdownList);
            }
        }
        if (toscaIndexFlag || nestedToscaFlag) {
            let indexValueMap : ToscaIndexObject = {indexFlag : toscaIndexFlag, nestedFlag : nestedToscaFlag, indexValue: "0", indexProperty: null, subPropertyArray: subPropertyDropdownList};
            if(!this.indexListValues[elementIndex+1]) {
                this.indexListValues.push(indexValueMap);
            } else {
                this.indexListValues[elementIndex+1] = indexValueMap;
            }
        } else {
            if(this.indexListValues[elementIndex+1]) {
                this.indexListValues.splice((elementIndex+1),1);
            }
        }
        this.formValidation();
    }

    indexTokenChange(indexObject : ToscaIndexObject): void {
        if ((indexObject.indexValue).toLowerCase() === 'index' ) {
            this.formValidation();
        }

        const regEx = /^[0-9]*$/;
        const error = document.getElementById('error');

        if (!(regEx.test(indexObject.indexValue)) && (indexObject.indexValue).toLowerCase() !== 'index') {
            error.textContent='Invalid value - must be an integer or INDEX';
            this.onValidityChange.emit({
                isValid: false,
                toscaGetFunction: this.formGroup.valid ? this.buildGetFunctionFromForm() : undefined
            });
        } else {
            error.textContent='';
            this.formValidation();
        }
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

    onChangeIndexValue(index: ToscaIndexObject, value: any) {
        this.indexTokenChange(index);
    }
}

export interface PropertyDropdownValue {
    propertyName: string;
    propertyId: string;
    propertyLabel: string;
    propertyPath: Array<string>;
    isList: boolean;
    schemaType: string;
}

export interface ToscaIndexObject {
    indexFlag: boolean;
    nestedFlag: boolean;
    indexValue: string;
    indexProperty: PropertyDropdownValue;
    subPropertyArray: Array<PropertyDropdownValue>;
}

export interface ToscaGetFunctionValidationEvent {
    isValid: boolean,
    toscaGetFunction: ToscaGetFunction,
}
