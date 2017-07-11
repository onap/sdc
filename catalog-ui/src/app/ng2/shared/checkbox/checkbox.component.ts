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

import { Component, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
//import { trigger, state, style, transition, animate, keyframes } from '@angular/core';

@Component({
    selector: 'checkbox',
    templateUrl: './checkbox.component.html',
    styleUrls: ['./checkbox.component.less'],
    encapsulation: ViewEncapsulation.None
    // animations: [
    //     trigger('checkEffect', [
    //         state('true', style({ position: 'absolute', left: '2px', top: '5px', width: '10px', height: '10px', display: 'none', opacity: '.5' })),
    //         state('false', style({ left: '-18px', top: '-15px', height: '50px', width: '50px', opacity: '0' })),
    //         transition('1 => 0', animate('150ms ease-out')),
    //         transition('0 => 1', animate('150ms ease-in'))
    //     ])
    // ]
})
export class CheckboxComponent  {
    
    @Input() checkboxStyle: string;
    @Input() label: string;
    @Input() checked: boolean;
    @Input() disabled: boolean;
    @Output() checkedChange: EventEmitter<any> = new EventEmitter<any>();

    toggleState(newValue:boolean) {
        this.checkedChange.emit(newValue);
    }
}

