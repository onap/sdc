/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { FormElementsModule } from 'app/ng2/components/ui/form-components/form-elements.module';
import { UiElementsModule } from 'app/ng2/components/ui/ui-elements.module';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { ToscaFunctionComponent } from './tosca-function.component';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { ToscaGetFunctionComponent } from './tosca-get-function/tosca-get-function.component';
import { ToscaConcatFunctionComponent } from './tosca-concat-function/tosca-concat-function.component';
import { YamlFunctionComponent } from './yaml-function/yaml-function.component';

@NgModule({
    declarations: [
        ToscaFunctionComponent,
        ToscaGetFunctionComponent,
        ToscaConcatFunctionComponent,
        YamlFunctionComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        FormElementsModule,
        UiElementsModule,
        TranslateModule,
        SdcUiComponentsModule
    ],
    exports: [],
    entryComponents: [
        ToscaFunctionComponent
    ],
    providers: []
})

export class ToscaFunctionModule {}
