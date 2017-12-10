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
    onSave();
}

@Component({
    template: ``,
    styles: []
})
export class UiElementBase {

    protected validation = ValidationConfiguration.validation;
    protected control: FormControl;

    // Two way binding for value (need to write the "Change" word like this)
    @Output('valueChange') baseEmitter: EventEmitter<string> = new EventEmitter<any>();
    @Input('value') set setValueValue(value) {
        this.value = value;
    }

    @Input() name: string;
    @Input() type: string;
    @Input() value: any;
    @Input() pattern: any;
    @Input() readonly:boolean;

    constructor() {
        //this.control = new FormControl('', [Validators.required]);
        this.control = new FormControl('', []);
    }

}
