/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {TranslateModule} from "app/ng2/shared/translator/translate.module";
import {AddInputComponent} from './add-input/add-input.component';
import {InputListComponent} from './input-list/input-list.component';
import {InputListItemComponent} from './input-list/input-list-item/input-list-item.component';
import {PropertyParamRowComponent} from "./property-param-row/property-param-row.component";
import {InterfaceOperationHandlerComponent} from "./interface-operation-handler.component";
import {SdcUiComponentsModule} from "onap-ui-angular/dist";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {PropertyTableModule} from "app/ng2/components/logic/properties-table/property-table.module";
import {ToscaFunctionModule} from '../../../properties-assignment/tosca-function/tosca-function.module';

@NgModule({
    declarations: [
        InterfaceOperationHandlerComponent,
        PropertyParamRowComponent,
        AddInputComponent,
        InputListComponent,
        InputListItemComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        FormsModule,
        FormElementsModule,
        TranslateModule,
        UiElementsModule,
        PropertyTableModule,
        ReactiveFormsModule,
        ToscaFunctionModule
    ],
    exports: [
        PropertyParamRowComponent,
        InputListItemComponent
    ],
    entryComponents: [
        InterfaceOperationHandlerComponent
    ],
    providers: []
})

export class InterfaceOperationHandlerModule {
}
