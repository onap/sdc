/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";
import {ToscaCustomFunction} from "../../../../../models/tosca-custom-function";
import {ToscaFunctionParameter} from "../../../../../models/tosca-function-parameter";
import {ToscaStringParameter} from "../../../../../models/tosca-string-parameter";
import {ToscaFunctionType} from "../../../../../models/tosca-function-type.enum";
import {PropertyBEModel} from "../../../../../models/properties-inputs/property-be-model";
import {PROPERTY_TYPES} from "../../../../../utils/constants";
import {InstanceFeDetails} from "../../../../../models/instance-fe-details";
import {ToscaFunctionValidationEvent} from "../tosca-function.component";
import {ToscaFunction} from "../../../../../models/tosca-function";
import {ToscaGetFunction} from "../../../../../models/tosca-get-function";
import {CustomToscaFunction} from "../../../../../models/default-custom-functions";
import {ToscaGetFunctionValidationEvent} from "../tosca-get-function/tosca-get-function.component";

@Component({
    selector: 'app-tosca-custom-function',
    templateUrl: './tosca-custom-function.component.html',
    styleUrls: ['./tosca-custom-function.component.less']
})
export class ToscaCustomFunctionComponent implements OnInit {

    @Input() toscaCustomFunction: ToscaCustomFunction;
    @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
    @Input() customToscaFunctions: Array<CustomToscaFunction> = [];
    @Input() name: string;
    @Input() type: ToscaFunctionType;
    @Input() propertyType: string;
    @Input() propertySchemaType: string = undefined;
    @Input() isDefaultCustomFunction: boolean;
    @Output() onValidFunction: EventEmitter<ToscaCustomFunction> = new EventEmitter<ToscaCustomFunction>();
    @Output() onValidityChange: EventEmitter<ToscaCustomFunctionValidationEvent> = new EventEmitter<ToscaCustomFunctionValidationEvent>();

    toscaGetFunction: ToscaFunction;
    customFunctionFormName: FormControl = new FormControl('', [Validators.required, Validators.minLength(1)]);
    customParameterFormArray: FormArray = new FormArray([], Validators.minLength(1));
    formGroup: FormGroup = new FormGroup(
        {
            'customParameterList': this.customParameterFormArray,
            'customName': this.customFunctionFormName
        }
    );

    parameters: ToscaFunctionParameter[] = [];
    propertyInputList: Array<PropertyBEModel> = [];
    previousType: ToscaFunctionType;

    STRING_FUNCTION_TYPE = ToscaFunctionType.STRING;
    GET_INPUT = ToscaFunctionType.GET_INPUT;

    ngOnInit() {
        this.initForm();
    }

    ngOnChanges() {
        if (this.previousType && this.previousType != this.type) {
            this.propertyInputList = [];
            this.customParameterFormArray = new FormArray([], Validators.minLength(1));
            this.parameters = [];
        }
        this.fillVariables();
        if (this.name && this.isDefaultCustomFunction) {
            this.customFunctionFormName.setValue(this.name);
            this.emitOnValidityChange();
        } else {
            this.name = '';
        }
        this.previousType = this.type;
    }

    private initForm(): void {
        this.formGroup.valueChanges.subscribe(() => {
            this.emitOnValidityChange();
            if (this.formGroup.valid) {
                this.onValidFunction.emit(this.buildCustomFunctionFromForm());
            }
        });
    }

    private fillVariables() {
        if (!this.toscaCustomFunction) {
            if (this.type === this.GET_INPUT && this.parameters.length < 1) {
                this.addFunction();
            }
            return;
        }
        if (this.toscaCustomFunction.parameters) {
            this.name = this.toscaCustomFunction.name;
            this.customFunctionFormName.setValue(this.name)
            this.parameters = Array.from(this.toscaCustomFunction.parameters);
            for (const parameter of this.parameters) {
                if (this.type === this.GET_INPUT) {
                    this.toscaGetFunction = parameter as ToscaGetFunction;
                    this.addToscaFunctionToParameters(parameter);
                    return;
                }
                if (parameter.type !== PROPERTY_TYPES.STRING) {
                    this.addToscaFunctionToParameters(parameter);
                } else {
                    this.propertyInputList.push(undefined);
                    this.customParameterFormArray.push(
                        new FormControl(parameter.value, [Validators.required, Validators.minLength(1)])
                    );
                }
            }
        }
        if (this.type === this.GET_INPUT && this.parameters.length < 1) {
            this.addFunction();
        }
    }

    private addToscaFunctionToParameters(parameter: ToscaFunctionParameter) {
        const propertyBEModel = this.createProperty(parameter.value);
        propertyBEModel.toscaFunction = <ToscaFunction> parameter;
        this.propertyInputList.push(propertyBEModel);
        this.customParameterFormArray.push(
            new FormControl(parameter, [Validators.required, Validators.minLength(1)])
        );
    }

    private buildCustomFunctionFromForm(): ToscaCustomFunction {
        const toscaCustomFunction1 = new ToscaCustomFunction();
        toscaCustomFunction1.name = this.customFunctionFormName.value;
        this.customParameterFormArray.controls.forEach(control => {
            const value = control.value;
            if (!value) {
                return;
            }
            if (typeof value === 'string') {
                const stringParameter = new ToscaStringParameter();
                stringParameter.value = value;
                toscaCustomFunction1.parameters.push(stringParameter);
            } else {
                toscaCustomFunction1.parameters.push(control.value);
            }
        });

        return toscaCustomFunction1;
    }

    private emitOnValidityChange() {
        this.onValidityChange.emit({
            isValid: this.formGroup.valid,
            toscaCustomFunction: this.formGroup.valid ? this.buildCustomFunctionFromForm() : undefined
        })
    }

    addFunction(): void {
        this.propertyInputList.push(this.createProperty());
        this.parameters.push({} as ToscaFunctionParameter);
        this.customParameterFormArray.push(
            new FormControl(undefined, [Validators.required, Validators.minLength(1)])
        );
    }

    addStringParameter(): void {
        const toscaStringParameter = new ToscaStringParameter();
        toscaStringParameter.value = '';
        this.propertyInputList.push(undefined);
        this.customParameterFormArray.push(
            new FormControl('', [Validators.required, Validators.minLength(1)])
        );
        this.parameters.push(toscaStringParameter);
        console.log(this.customParameterFormArray)
    }

    removeParameter(position): void {
        this.propertyInputList.splice(position, 1);
        this.parameters.splice(position, 1);
        this.customParameterFormArray.removeAt(position);
    }

    createProperty(value?: any): PropertyBEModel {
        const property = new PropertyBEModel();
        if (this.type === this.GET_INPUT) {
            property.type = this.propertyType;
            if (this.propertySchemaType) {
                property.schemaType = this.propertySchemaType;
            }
        } else {
            property.type = PROPERTY_TYPES.ANY;
        }
        
        property.value = value ? value : undefined;
        return property;
    }

    onFunctionValidityChange(event: ToscaFunctionValidationEvent, index: number): void {
        if (event.isValid && event.toscaFunction) {
            this.customParameterFormArray.controls[index].setValue(event.toscaFunction)
        } else {
            this.customParameterFormArray.controls[index].setValue(undefined);
        }
    }

    onGetFunctionValidityChange(event: ToscaGetFunctionValidationEvent, index: number): void {
        if (event.isValid && event.toscaGetFunction) {
            this.customParameterFormArray.controls[index].setValue(event.toscaGetFunction);
        } else {
            this.customParameterFormArray.controls[index].setValue(undefined);
        }
        this.emitOnValidityChange();
    }
}

export interface ToscaCustomFunctionValidationEvent {
    isValid: boolean,
    toscaCustomFunction: ToscaCustomFunction,
}
