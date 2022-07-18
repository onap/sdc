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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {YamlFunction} from "../../../../../models/yaml-function";
import {FormControl, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn} from "@angular/forms";
import * as jsYaml from 'js-yaml';

@Component({
    selector: 'app-yaml-function',
    templateUrl: './yaml-function.component.html',
    styleUrls: ['./yaml-function.component.less']
})
export class YamlFunctionComponent implements OnInit {

    @Input() yamlFunction: YamlFunction;
    @Output() onValidityChange: EventEmitter<YamlFunctionValidationEvent> = new EventEmitter<YamlFunctionValidationEvent>();

    yamlValueForm: FormControl = new FormControl('', [Validators.minLength(1), YamlValidator()]);
    formGroup: FormGroup = new FormGroup(
        {
            'value': this.yamlValueForm
        }
    );

    ngOnInit() {
        this.formGroup.valueChanges.subscribe(() => {
            this.onValidityChange.emit({
                isValid: this.formGroup.valid,
                value: this.formGroup.valid ? this.buildYamlFunctionFromForm() : undefined
            });
        });
        if (this.yamlFunction) {
            this.yamlValueForm.setValue(this.yamlFunction.value);
        }
    }

    private buildYamlFunctionFromForm(): YamlFunction {
        const yamlFunction = new YamlFunction();
        yamlFunction.value = this.yamlValueForm.value;
        return yamlFunction;
    }
}

export class YamlFunctionValidationEvent {
    isValid: boolean;
    value: YamlFunction;
}

export function YamlValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        if (!control.value) {
            return {invalidYaml: {value: control.value}};
        }
        try {
            jsYaml.load(control.value);
            return null;
        } catch (e) {
            return {invalidYaml: {value: control.value}};
        }
    };
}