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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ComponentMetadata, PropertyBEModel} from 'app/models';
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
import {PROPERTY_TYPES} from "../../../../utils/constants";
import {YamlFunctionValidationEvent} from "./yaml-function/yaml-function.component";

@Component({
    selector: 'tosca-function',
    templateUrl: './tosca-function.component.html',
    styleUrls: ['./tosca-function.component.less'],
})
export class ToscaFunctionComponent implements OnInit {

    @Input() property: PropertyBEModel;
    @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
    @Input() allowClear: boolean = true;
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
        this.initToscaGetFunction();
        this.emitValidityChange();
        this.isInitialized = true;
    }

    private validate() {
        return (!this.toscaFunctionForm.value && !this.toscaFunctionTypeForm.value) || this.formGroup.valid;
    }

    private initToscaGetFunction() {
        if (!this.property.isToscaFunction()) {
            return;
        }
        this.toscaFunctionForm.setValue(this.property.toscaFunction);
        this.toscaFunctionTypeForm.setValue(this.property.toscaFunction.type);
    }

    private loadToscaFunctions(): void {
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

    onClearValues() {
        this.resetForm();
    }

    showClearButton(): boolean {
        return this.allowClear && this.toscaFunctionTypeForm.value;
    }

    onConcatFunctionValidityChange(validationEvent: ToscaConcatFunctionValidationEvent) {
        if (validationEvent.isValid) {
            this.toscaFunctionForm.setValue(validationEvent.toscaConcatFunction);
        } else {
            this.toscaFunctionForm.setValue(undefined);
        }
    }

    onGetFunctionValidityChange(validationEvent: ToscaGetFunctionValidationEvent) {
        if (validationEvent.isValid) {
            this.toscaFunctionForm.setValue(validationEvent.toscaGetFunction);
        } else {
            this.toscaFunctionForm.setValue(undefined);
        }
    }

    onYamlFunctionValidityChange(validationEvent: YamlFunctionValidationEvent) {
        if (validationEvent.isValid) {
            this.toscaFunctionForm.setValue(validationEvent.value);
        } else {
            this.toscaFunctionForm.setValue(undefined);
        }
    }

    private emitValidityChange() {
        const isValid = this.validate();
        this.onValidityChange.emit({
            isValid: isValid,
            toscaFunction: isValid ? this.toscaFunctionForm.value : undefined
        });
    }

}

export class ToscaFunctionValidationEvent {
    isValid: boolean;
    toscaFunction: ToscaFunction;
}