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

import {Component, Output, EventEmitter} from '@angular/core';
import { UiElementBase, UiElementBaseInterface } from './../ui-element-base.component';
import {ConstraintTypes} from "../../../../pages/properties-assignment/constraints/constraints.component";
import {PROPERTY_DATA, PROPERTY_TYPES} from "../../../../../utils/constants";

@Component({
    selector: 'ui-element-valid-values-input',
    templateUrl: './ui-element-valid-values-input.component.html',
    styleUrls: ['./ui-element-valid-values-input.component.less'],
})
export class UiElementValidValuesInputComponent extends UiElementBase implements UiElementBaseInterface {
    @Output() onConstraintChange: EventEmitter<any> = new EventEmitter<any>();
    constructor() {
        super();
        this.pattern = this.validation.validationPatterns.comment;
    }

    showStringField(): boolean {
        return this.type === PROPERTY_TYPES.STRING || PROPERTY_DATA.SCALAR_TYPES.indexOf(this.type) > -1;
    }

    showIntegerField(): boolean {
        return this.type === PROPERTY_TYPES.INTEGER || this.type === PROPERTY_TYPES.TIMESTAMP;
    }

    addToList(){
        if (!this.value) {
            this.value = new Array();
        }
        this.value.push("");
        this.baseEmitter.emit({
            value: this.value,
            isValid: false
        });
        this.emitOnConstraintChange()
    }

    onChangeConstrainValueIndex(newValue: any, valueIndex: number) {
        if(!this.value) {
            this.value = new Array();
        }
        this.value[valueIndex] = newValue;
        this.baseEmitter.emit({
            value: this.value,
            isValid: newValue != "" && !this.doesArrayContaintEmptyValues(this.value)
        });
        this.emitOnConstraintChange();
    }

    private emitOnConstraintChange(): void {
        this.onConstraintChange.emit({
            valid: this.validateConstraints()
        });
    }
    private validateConstraints(): boolean {
        if (Array.isArray(this.value)) {
            return !(this.value.length == 0 || this.doesArrayContaintEmptyValues(this.value));
        }
        return this.value && this.type != ConstraintTypes.null
    }

    removeFromList(valueIndex: number){
        this.value.splice(valueIndex, 1);
        this.baseEmitter.emit({
            value: this.value,
            isValid: !this.doesArrayContaintEmptyValues(this.value)
        });
        this.emitOnConstraintChange()
    }

    trackByFn(index) {
        return index;
    }

    private doesArrayContaintEmptyValues(arr) {
        for(const element of arr) {
            if(element === "") return true;
        }
        return false;
    }
}
