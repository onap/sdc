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

import {Component, Input} from '@angular/core';
import { UiElementBase, UiElementBaseInterface } from './../ui-element-base.component';
import {PROPERTY_DATA} from "../../../../../utils/constants";

@Component({
    selector: 'ui-element-range-input',
    templateUrl: './ui-element-range-input.component.html',
    styleUrls: ['./ui-element-range-input.component.less'],
})
export class UiElementRangeInputComponent extends UiElementBase implements UiElementBaseInterface {
    @Input() lowerBound: any;
    @Input() upperBound: any;
    step: number;
    constructor() {
        super();
        this.pattern = this.validation.validationPatterns.comment;
        this.value = new Array(2);
        this.value[0] = this.lowerBound;
        this.value[1] = this.upperBound;
    }

    ngOnInit(){
        this.step = 0;
        if (this.type === 'float') {
            this.step = 0.01;
        }
        if (this.type === 'integer') {
            this.step = 0;
        }
    }

    isFloatType(): boolean {
        return this.type === 'float';
    }

    isIntegerType(): boolean {
        return this.type === 'integer' || this.type === 'range' || this.type === 'timestamp';
    }

    isStringType(): boolean {
        return this.type === 'string' || PROPERTY_DATA.SCALAR_TYPES.indexOf(this.type) > -1;
    }

    onChangeMin() {
        if (!this.value) {
            this.value = new Array(2);
        }
        this.value.splice(0, 1, this.lowerBound);
        this.baseEmitter.emit({
            value: this.value ,
            isValid: this.isValidRange()
        });
    }

    onChangeMax() {
        if (!this.value) {
            this.value = new Array(2);
        }
        this.value.splice(1, 1, this.upperBound);
        this.baseEmitter.emit({
            value: this.value,
            isValid: this.isValidRange()
        });
    }

    getInRangeValue(valueIndex: number): string {
        if(!this.value || !this.value[valueIndex]) {
            return "";
        }
        return this.value[valueIndex];
    }

    isNumber(value: string | number): boolean
    {
        return ((value != undefined) &&
            (value != null) &&
            (value !== '') &&
            !isNaN(Number(value.toString())));
    }

    isValidRange(): boolean
    {
        return this.isNumber(this.value[0])
        && (this.getInRangeValue(1) === "UNBOUNDED"
                || (this.isNumber(this.value[1])
                    && this.getInRangeValue(0) <= this.getInRangeValue(1)));
    }
}
