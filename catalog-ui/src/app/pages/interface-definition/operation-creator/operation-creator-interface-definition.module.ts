/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2022 Nordix Foundation. All rights reserved.
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
import {ParamRowComponent} from "./param-row/param-row.component";
import {OperationCreatorInterfaceDefinitionComponent} from "./operation-creator-interface-definition.component";
import {UiElementsModule} from "../../../components/ui/ui-elements.module";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "../../../components/ui/form-components/form-elements.module";

@NgModule({
    declarations: [
        OperationCreatorInterfaceDefinitionComponent,
        ParamRowComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        FormsModule,
        FormElementsModule,
        TranslateModule,
        UiElementsModule
    ],
    exports: [],
    entryComponents: [
        OperationCreatorInterfaceDefinitionComponent
    ],
    providers: []
})

export class OperationCreatorInterfaceDefinitionModule {
}
