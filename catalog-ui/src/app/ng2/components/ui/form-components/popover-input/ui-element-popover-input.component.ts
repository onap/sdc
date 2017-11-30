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

import {Component, ViewChild, ElementRef, Input} from '@angular/core';
import {ButtonsModelMap, ButtonModel} from "app/models";
import {PopoverContentComponent} from "../../popover/popover-content.component";
import {UiElementBase, UiElementBaseInterface} from "../ui-element-base.component";

@Component({
    selector: 'ui-element-popover-input',
    templateUrl: './ui-element-popover-input.component.html',
    styleUrls: ['./ui-element-popover-input.component.less']
})
export class UiElementPopoverInputComponent extends UiElementBase implements UiElementBaseInterface {
    @ViewChild('textArea') textArea: ElementRef;
    @ViewChild('popoverForm') popoverContentComponent: PopoverContentComponent;

    saveButton: ButtonModel;
    buttonsArray: ButtonsModelMap;

    onSave = ():void => {
        if (!this.control.invalid){
            this.baseEmitter.emit(this.value);
            this.popoverContentComponent.hide();
        }
    }

    constructor() {
        super();
        // Create Save button and insert to buttons map
        this.saveButton = new ButtonModel('save', 'blue', this.onSave);
        this.buttonsArray = { 'test': this.saveButton };

        // Define the regex pattern for this controller
        this.pattern = this.validation.validationPatterns.comment;

        // Disable / Enable button according to validation
        //this.control.valueChanges.subscribe(data => this.saveButton.disabled = this.control.invalid);
    }
}
