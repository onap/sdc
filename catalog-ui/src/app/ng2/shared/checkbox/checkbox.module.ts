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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { CheckboxComponent } from './checkbox.component';


@NgModule({
    imports: [CommonModule, BrowserModule, FormsModule],
    declarations: [CheckboxComponent],
    bootstrap: [],
    exports: [CheckboxComponent]
})
export class CheckboxModule { }

/** README: **/

/** USAGE Example:
 *In page.module.ts: import CheckboxModule
 *In HTML:
 *<checkbox checkboxStyle="class-goes-here" [label]="'Text goes here'" [disabled]="variable-goes-here" [(checked)]="default-or-variable-goes-here" (checkedChange)="change-event-goes-here()"></checkbox>
 */

/**STYLING: (ViewEncapsulation is set to None to allow styles to be overridden or customized)
 *
 * To create or override styles:
 * Use /deep/ or >>> prefix to override styles via other components stylesheets
 */
