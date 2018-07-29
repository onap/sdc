/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import { Component, EventEmitter, Input, Output } from '@angular/core'
import { ValidationConfiguration } from "app/models";
import { FormControl, Validators } from '@angular/forms';

export interface UiElementBaseInterface {
    onChange();
}

export interface IUiElementChangeEvent {
    value: any;
    isValid: boolean;
}

@Component({
    template: ``,
    styles: []
})
export class UiElementBase {

    protected validation = ValidationConfiguration.validation;
    protected control: FormControl;

    @Input() value: any;
    @Output() valueChange: EventEmitter<any> = new EventEmitter<any>();
    @Output('elementChanged') baseEmitter: EventEmitter<IUiElementChangeEvent> = new EventEmitter<IUiElementChangeEvent>();

    @Input() name: string;
    @Input() type: string;
    @Input() path: string;
    @Input() pattern: any;
    @Input() readonly:boolean;

    @Input() testId:string;

    constructor() {
        //this.control = new FormControl('', [Validators.required]);
        this.control = new FormControl('', []);

        this.baseEmitter.subscribe((changeEvent: IUiElementChangeEvent) => {
            this.valueChange.emit(changeEvent.value);
        })
    }

    onChange() {
        this.baseEmitter.emit({
            value: this.value,
            isValid: this.control.valid
        });
    }

}
