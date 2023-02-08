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
import {ComponentMetadata, PropertyBEModel, PropertyDeclareAPIModel, DerivedFEProperty} from 'app/models';
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {ToscaGetFunctionType} from "../../../../models/tosca-get-function-type";
import {InstanceFeDetails} from "../../../../models/instance-fe-details";
import {ToscaGetFunction} from "../../../../models/tosca-get-function";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ToscaFunctionType} from "../../../../models/tosca-function-type.enum";
import {ToscaGetFunctionValidationEvent} from "./tosca-get-function/tosca-get-function.component";
import {ToscaFunction} from "../../../../models/tosca-function";
import {ToscaConcatFunctionValidationEvent} from "./tosca-concat-function/tosca-concat-function.component";
import {PROPERTY_TYPES, PROPERTY_DATA} from "../../../../utils/constants";
import {YamlFunctionValidationEvent} from "./yaml-function/yaml-function.component";
import {ToscaConcatFunction} from "../../../../models/tosca-concat-function";
import {YamlFunction} from "../../../../models/yaml-function";

@Component({
    selector: 'tosca-function',
    templateUrl: './tosca-function.component.html',
    styleUrls: ['./tosca-function.component.less'],
})
export class ToscaFunctionComponent implements OnInit, OnChanges {

    @Input() property: PropertyBEModel;
    @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
    @Input() allowClear: boolean = true;
    @Input() compositionMap: boolean = false;
    @Input() compositionMapKey: string = "";
    @Output() onValidFunction: EventEmitter<ToscaGetFunction> = new EventEmitter<ToscaGetFunction>();
    @Output() onValidityChange: EventEmitter<ToscaFunctionValidationEvent> = new EventEmitter<ToscaFunctionValidationEvent>();

    toscaFunctionForm: FormControl = new FormControl(undefined, [Validators.required]);
    toscaFunctionTypeForm: FormControl = new FormControl(undefined, Validators.required);
    formGroup: FormGroup = new FormGroup({
        'toscaFunction': this.toscaFunctionForm,
        'toscaFunctionType': this.toscaFunctionTypeForm,
    });

    isLoading: boolean = false;
    toscaFunction: ToscaFunction;
    toscaFunctions: Array<string> = [];

    private isInitialized: boolean = false;
    private componentMetadata: ComponentMetadata;

    constructor(private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService) {
    }

    ngOnInit(): void {
        this.componentMetadata = this.workspaceService.metadata;
        this.toscaFunction = this.property.toscaFunction ? this.property.toscaFunction : undefined;
        this.loadToscaFunctions();
        this.formGroup.valueChanges.subscribe(() => {
            if (!this.isInitialized) {
                return;
            }
            this.emitValidityChange();
            if (this.formGroup.valid) {
                this.onValidFunction.emit(this.toscaFunctionForm.value);
            }
        });
        this.initToscaFunction();
        this.emitValidityChange();
        this.isInitialized = true;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.property) {
            this.resetForm();
            this.toscaFunction = this.property.toscaFunction ? this.property.toscaFunction : undefined;
            this.initToscaFunction();
            this.loadToscaFunctions();
            this.emitValidityChange();
        }
    }

    private validate(): boolean {
        return (!this.toscaFunctionForm.value && !this.toscaFunctionTypeForm.value) || this.formGroup.valid;
    }

    private initToscaFunction(): void {
        if (this.compositionMap && this.property.subPropertyToscaFunctions) {
            let keyToFind = [this.compositionMapKey];
            let subPropertyToscaFunction = this.property.subPropertyToscaFunctions.find(subPropertyToscaFunction => this.areEqual(subPropertyToscaFunction.subPropertyPath, keyToFind));

                if (subPropertyToscaFunction){
	                this.toscaFunction = subPropertyToscaFunction.toscaFunction;
                    this.toscaFunctionForm.setValue(this.toscaFunction);
                    this.toscaFunctionTypeForm.setValue(this.toscaFunction.type);
                }
                return;
        }
	    if (this.property instanceof PropertyDeclareAPIModel && this.property.subPropertyToscaFunctions && (<PropertyDeclareAPIModel> this.property).propertiesName){
	        let propertiesPath = (<PropertyDeclareAPIModel> this.property).propertiesName.split("#");
            if (propertiesPath.length > 1){
                let keyToFind = [];
                if (this.property.type == PROPERTY_TYPES.MAP || this.property.type == PROPERTY_TYPES.LIST) {
                    if (this.property.type == PROPERTY_TYPES.LIST && this.property.schemaType == PROPERTY_TYPES.MAP) {
                        keyToFind.push((<DerivedFEProperty>this.property.input).parentMapKey);
                    }
                    keyToFind.push((<DerivedFEProperty>this.property.input).mapKey);
                    if (this.property.schemaType != PROPERTY_TYPES.MAP && PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.property.schemaType) === -1) {
                        keyToFind.push(propertiesPath.reverse()[0]);
                    }
                }
                let subPropertyToscaFunction = this.property.subPropertyToscaFunctions.find(subPropertyToscaFunction => this.areEqual(subPropertyToscaFunction.subPropertyPath, keyToFind.length > 0 ? keyToFind : propertiesPath.slice(1)));

                if (subPropertyToscaFunction){
	                this.toscaFunction = subPropertyToscaFunction.toscaFunction;
                    this.toscaFunctionForm.setValue(this.toscaFunction);
                    this.toscaFunctionTypeForm.setValue(this.toscaFunction.type);
                }
                return;
            }
        }

        if (!this.property.isToscaFunction()) {
            return;
        }
        this.toscaFunctionForm.setValue(this.property.toscaFunction);
        this.toscaFunctionTypeForm.setValue(this.property.toscaFunction.type);
    }

    private areEqual(array1: string[], array2: string[]): boolean {
	    return array1.length === array2.length && array1.every(function(value, index) { return value === array2[index]})
    }

    private loadToscaFunctions(): void {
        this.toscaFunctions = [];
        this.toscaFunctions.push(ToscaFunctionType.GET_ATTRIBUTE);
        this.toscaFunctions.push(ToscaFunctionType.GET_INPUT);
        this.toscaFunctions.push(ToscaFunctionType.GET_PROPERTY);
        if (this.property.type === PROPERTY_TYPES.STRING) {
            this.toscaFunctions.push(ToscaFunctionType.CONCAT);
        }
        this.toscaFunctions.push(ToscaFunctionType.YAML);
    }

    private resetForm(): void {
        this.formGroup.reset();
        this.toscaFunction = undefined;
    }

    private isGetPropertySelected(): boolean {
        return this.formGroup.get('toscaFunctionType').value === ToscaGetFunctionType.GET_PROPERTY;
    }

    private isGetAttributeSelected(): boolean {
        return this.formGroup.get('toscaFunctionType').value === ToscaGetFunctionType.GET_ATTRIBUTE;
    }

    private isGetInputSelected(): boolean {
        return this.formGroup.get('toscaFunctionType').value === ToscaGetFunctionType.GET_INPUT;
    }

    isConcatSelected(): boolean {
        return this.formGroup.get('toscaFunctionType').value === ToscaFunctionType.CONCAT;
    }

    isGetFunctionSelected(): boolean {
        return this.isGetInputSelected() || this.isGetPropertySelected() || this.isGetAttributeSelected();
    }

    isYamlFunctionSelected(): boolean {
        return this.formGroup.get('toscaFunctionType').value === ToscaFunctionType.YAML;
    }

    onClearValues(): void {
        this.resetForm();
    }

    showClearButton(): boolean {
        return this.allowClear && this.toscaFunctionTypeForm.value;
    }

    onConcatFunctionValidityChange(validationEvent: ToscaConcatFunctionValidationEvent): void {
        if (validationEvent.isValid) {
            this.toscaFunctionForm.setValue(validationEvent.toscaConcatFunction);
        } else {
            this.toscaFunctionForm.setValue(undefined);
        }
    }

    onGetFunctionValidityChange(validationEvent: ToscaGetFunctionValidationEvent): void {
        if (validationEvent.isValid) {
            this.toscaFunctionForm.setValue(validationEvent.toscaGetFunction);
        } else {
            this.toscaFunctionForm.setValue(undefined);
        }
    }

    onYamlFunctionValidityChange(validationEvent: YamlFunctionValidationEvent): void {
        if (validationEvent.isValid) {
            this.toscaFunctionForm.setValue(validationEvent.value);
        } else {
            this.toscaFunctionForm.setValue(undefined);
        }
    }

    onFunctionTypeChange(): void {
        this.toscaFunction = undefined;
        this.toscaFunctionForm.reset();
    }

    private emitValidityChange(): void {
        const isValid: boolean = this.validate();
        this.onValidityChange.emit({
            isValid: isValid,
            toscaFunction: isValid ? this.buildFunctionFromForm() : undefined
        });
    }

    private buildFunctionFromForm(): ToscaFunction {
        if (!this.toscaFunctionTypeForm.value) {
            return undefined;
        }
        if (this.isConcatSelected()) {
            return new ToscaConcatFunction(this.toscaFunctionForm.value);
        }
        if (this.isGetFunctionSelected()) {
            return new ToscaGetFunction(this.toscaFunctionForm.value);
        }
        if (this.isYamlFunctionSelected()) {
            return new YamlFunction(this.toscaFunctionForm.value);
        }

        console.error(`Function ${this.toscaFunctionTypeForm.value} not supported`);
    }
}

export class ToscaFunctionValidationEvent {
    isValid: boolean;
    toscaFunction: ToscaFunction;
}