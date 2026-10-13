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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { PropertyTableModule } from 'app/components/logic/properties-table/property-table.module';
import { FormElementsModule } from 'app/components/ui/form-components/form-elements.module';
import { TabModule } from 'app/components/ui/tabs/tabs.module';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { TranslateModule } from 'app/shared/translator/translate.module';
import { SdcUiComponentsModule } from 'onap-ui-angular/dist';
import { ToscaFunctionModule } from '../../../properties-assignment/tosca-function/tosca-function.module';
import { ActivitiesListComponent } from './activities-list/activities-list.component';
import { AddInputComponent } from './add-input/add-input.component';
import { FiltersListComponent } from './filters-list/filters-list.component';
import { InputListItemComponent } from './input-list/input-list-item/input-list-item.component';
import { InputListComponent } from './input-list/input-list.component';
import { InterfaceOperationHandlerComponent } from './interface-operation-handler.component';
import { PropertyParamRowComponent } from './property-param-row/property-param-row.component';

@NgModule({
    declarations: [
        InterfaceOperationHandlerComponent,
        PropertyParamRowComponent,
        AddInputComponent,
        InputListComponent,
        InputListItemComponent,
        FiltersListComponent,
        ActivitiesListComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        BrowserModule,
        FormsModule,
        FormElementsModule,
        TranslateModule,
        UiElementsModule,
        PropertyTableModule,
        ReactiveFormsModule,
        ToscaFunctionModule,
        TabModule
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
