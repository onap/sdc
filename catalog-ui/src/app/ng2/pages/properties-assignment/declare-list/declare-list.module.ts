/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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

import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {DeclareListComponent} from "./declare-list.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {TranslateModule} from "../../../shared/translator/translate.module";

@NgModule({
    declarations: [
        DeclareListComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [
        DeclareListComponent
    ],
    providers: []
})

export class DeclareListModule {}
