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

import { NgModule } from "@angular/core";
import { UiElementCheckBoxComponent } from './elements-ui/checkbox/ui-element-checkbox.component';
import { UiElementDropDownComponent } from './elements-ui/dropdown/ui-element-dropdown.component';
import { UiElementInputComponent } from './elements-ui/input/ui-element-input.component';
import { DynamicElementComponent } from "app/ng2/components/dynamic-element/dynamic-element.component";
import { BrowserModule } from '@angular/platform-browser'
import { FormsModule, ReactiveFormsModule } from '@angular/forms'
import { UiElementPopoverInputComponent } from "./elements-ui/popover-input/ui-element-popover-input.component";
import {PopoverModule} from "../popover/popover.module";
import {TooltipModule} from "../tooltip/tooltip.module";
import {UiElementIntegerInputComponent} from "./elements-ui/integer-input/ui-element-integer-input.component";

@NgModule({
    declarations: [
        DynamicElementComponent,
        UiElementInputComponent,
        UiElementCheckBoxComponent,
        UiElementDropDownComponent,
        UiElementPopoverInputComponent,
        UiElementIntegerInputComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        PopoverModule,
        ReactiveFormsModule,
        TooltipModule
    ],
    exports: [
        DynamicElementComponent
    ],
    providers: []
})
export class DynamicElementModule {

}
