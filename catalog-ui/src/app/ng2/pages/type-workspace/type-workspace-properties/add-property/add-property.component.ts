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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {PropertyBEModel} from "../../../../../models/properties-inputs/property-be-model";
import {AbstractControl, FormControl, FormGroup, ValidationErrors, Validators} from "@angular/forms";
import {PROPERTY_DATA} from "../../../../../utils/constants";
import {DataTypeService} from "../../../../services/data-type.service";
import {DataTypeModel} from "../../../../../models/data-types";
import {Subscription} from "rxjs";
import {ToscaTypeHelper} from "../../../../../utils/tosca-type-helper";
import {SchemaProperty, SchemaPropertyGroupModel} from "../../../../../models/schema-property";

@Component({
    selector: 'app-add-property',
    templateUrl: './add-property.component.html',
    styleUrls: ['./add-property.component.less']
})
export class AddPropertyComponent implements OnInit, OnDestroy {

    @Input() property: PropertyBEModel;
    @Input() readOnly: boolean = true;
    @Input() model: string;

    @Output() onValidityChange: EventEmitter<PropertyValidationEvent> = new EventEmitter<PropertyValidationEvent>();

    private valueChangesSub: Subscription;
    private descriptionForm: FormControl = new FormControl(undefined);
    private requiredForm: FormControl = new FormControl(false, Validators.required);
    nameForm: FormControl = new FormControl(undefined, [Validators.required]);
    typeForm: FormControl = new FormControl(undefined, Validators.required);
    schemaForm: FormControl = new FormControl(undefined, (control: AbstractControl): ValidationErrors | null => {
        if (this.typeNeedsSchema() && !control.value) {
            return {required: true};
        }
        return null;
    });
    hasDefaultValueForm: FormControl = new FormControl(false, Validators.required);
    defaultValueForm: FormControl = new FormControl(undefined);
    formGroup: FormGroup = new FormGroup({
        'name': this.nameForm,
        'description': this.descriptionForm,
        'type': this.typeForm,
        'required': this.requiredForm,
        'schema': this.schemaForm,
        'defaultValue': this.defaultValueForm,
        'hasDefaultValue': this.hasDefaultValueForm,
    });

    isLoading: boolean = false;
    showSchema: boolean = false;
    typeList: string[];
    dataTypeMap: Map<string, DataTypeModel>;
    dataType: DataTypeModel;
    schemaTypeList: string[];

    constructor(private dataTypeService: DataTypeService) {
    }

    ngOnInit(): void {
        this.isLoading = true;
        this.initTypeAndSchemaDropdown().then(() => this.updateDataType());
        this.initForm();
        this.valueChangesSub = this.formGroup.valueChanges.subscribe(() => {
            this.emitValidityChange();
        });
    }

    ngOnDestroy(): void {
        if (this.valueChangesSub) {
            this.valueChangesSub.unsubscribe();
        }
    }

    onSchemaChange(): void {
        this.resetDefaultValue();
    }

    onTypeChange(): void {
        this.schemaForm.setValue(null);
        this.showSchema = this.typeNeedsSchema();
        this.updateDataType();
        this.resetDefaultValue();
    }

    private updateDataType(): void {
        this.dataType = this.dataTypeMap.get(this.typeForm.value);
    }

    private initForm(): void {
        if (!this.property) {
            return;
        }

        this.nameForm.setValue(this.property.name);
        this.descriptionForm.setValue(this.property.description);
        this.typeForm.setValue(this.property.type);
        this.showSchema = this.typeNeedsSchema();
        this.requiredForm.setValue(this.property.required);
        this.schemaForm.setValue(this.property.schemaType);
        this.initDefaultValueForm();
    }

    private initDefaultValueForm() {
        if (this.property.defaultValue == undefined) {
            return;
        }
        let defaultValue;
        if (!this.isTypeSimple() && typeof this.property.defaultValue === 'string') {
            defaultValue = JSON.parse(this.property.defaultValue);
        } else {
            defaultValue = this.property.defaultValue;
        }
        this.defaultValueForm.setValue(defaultValue);
        this.hasDefaultValueForm.setValue(true);
    }

    private typeNeedsSchema() {
        return PROPERTY_DATA.SCHEMA_TYPES.indexOf(this.typeForm.value) > -1;
    }

    private initTypeAndSchemaDropdown(): Promise<Map<string, DataTypeModel>> {
        const primitiveTypes: string[] = Array.from(PROPERTY_DATA.TYPES).sort((a, b) => a.localeCompare(b));
        const promise = this.dataTypeService.findAllDataTypesByModel(this.model);
        promise.then((dataTypeMap: Map<string, DataTypeModel>) => {
            this.dataTypeMap = dataTypeMap;
            const nonPrimitiveTypes: string[] = Array.from(dataTypeMap.keys()).filter(type => {
                return primitiveTypes.indexOf(type) === -1;
            });
            nonPrimitiveTypes.sort((a, b) => a.localeCompare(b));
            this.typeList = [...primitiveTypes, ...nonPrimitiveTypes];
            this.schemaTypeList = Array.from(this.typeList);
            this.isLoading = false;
        });
        return promise;
    }

    private emitValidityChange(): void {
        const isValid: boolean = this.formGroup.valid;
        this.onValidityChange.emit({
            isValid: isValid,
            property: isValid ? this.buildPropertyFromForm() : undefined
        });
    }

    private buildPropertyFromForm(): PropertyBEModel {
        const property = new PropertyBEModel();
        property.name = this.nameForm.value;
        property.type = this.typeForm.value;
        if (this.schemaForm.value) {
            property.schemaType = this.schemaForm.value;
        }
        property.description = this.descriptionForm.value;
        if (this.hasDefaultValueForm.value === true) {
            property.defaultValue = this.defaultValueForm.value;
        }
        return property;
    }

    public isTypeSimple(): boolean {
        return ToscaTypeHelper.isTypeSimple(this.typeForm.value);
    }

    public isTypeList(): boolean {
        return ToscaTypeHelper.isTypeList(this.typeForm.value);
    }

    public isTypeMap(): boolean {
        return ToscaTypeHelper.isTypeMap(this.typeForm.value);
    }

    public isTypeComplex(): boolean {
        return ToscaTypeHelper.isTypeComplex(this.typeForm.value);
    }

    private isTypeRange() {
        return ToscaTypeHelper.isTypeRange(this.typeForm.value);
    }

    onPropertyValueChange($event: any): void {
        this.defaultValueForm.setValue($event.value);
    }

    showDefaultValue(): boolean {
        if (this.readOnly) {
            return this.defaultValueForm.value != undefined && this.dataTypeMap && this.typeForm.valid && this.schemaForm.valid;
        }
        return this.dataTypeMap && this.typeForm.valid && this.schemaForm.valid;
    }

    getDataType(type: string): DataTypeModel {
        return this.dataTypeMap.get(type);
    }

    private resetDefaultValue(): void {
        this.defaultValueForm.reset();
        if (this.isTypeComplex() || this.isTypeMap()) {
            this.defaultValueForm.setValue({});
        } else if (this.isTypeList() || this.isTypeRange()) {
            this.defaultValueForm.setValue([]);
        }
    }

    buildSchemaGroupProperty(): SchemaPropertyGroupModel {
        const schemaProperty = new SchemaProperty();
        schemaProperty.type = this.schemaForm.value
        return new SchemaPropertyGroupModel(schemaProperty);
    }

}

export class PropertyValidationEvent {
    isValid: boolean;
    property: PropertyBEModel;
}