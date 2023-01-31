/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TypeWorkspaceComponent} from "./type-workspace.component";
import {WorkspaceMenuComponent} from "./workspace-menu/workspace-menu.component";
import {CacheService} from "../../services/cache.service";
import {UiElementsModule} from "../../components/ui/ui-elements.module";
import {LayoutModule} from "../../components/layout/layout.module";
import {TypeWorkspaceGeneralComponent} from './type-workspace-general/type-workspace-general.component';
import {UpgradeModule} from "@angular/upgrade/static";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "../../shared/translator/translate.module";
import {DataTypeService} from "../../services/data-type.service";
import { TypeWorkspacePropertiesComponent } from './type-workspace-properties/type-workspace-properties.component';
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {SvgIconModule} from "onap-ui-angular/dist/svg-icon/svg-icon.module";
import {TypeWorkspaceToscaArtifactPageComponent} from "./type-workspace-tosca-artifacts/type-workspace-tosca-artifact-page.component";
import {ModalService} from "../../services/modal.service";
import {AddPropertyComponent} from './type-workspace-properties/add-property/add-property.component';
import {InterfaceOperationHandlerModule} from "../composition/interface-operatons/operation-creator/interface-operation-handler.module";
import {AutoCompleteModule} from "onap-ui-angular/dist/autocomplete/autocomplete.module";
import {ConstraintsModule} from "../properties-assignment/constraints/constraints.module";

@NgModule({
   imports: [
        CommonModule,
        UiElementsModule,
        LayoutModule,
        UpgradeModule,
        ReactiveFormsModule,
        TranslateModule,
        FormsModule,
        InterfaceOperationHandlerModule,
        NgxDatatableModule,
        SvgIconModule,
        AutoCompleteModule,
       ConstraintsModule
    ],
    declarations: [
        TypeWorkspaceComponent,
        WorkspaceMenuComponent,
        TypeWorkspaceGeneralComponent,
        TypeWorkspacePropertiesComponent,
        TypeWorkspaceToscaArtifactPageComponent,
        AddPropertyComponent,
    ],
    providers: [
        CacheService,
        WorkspaceMenuComponent,
        DataTypeService,
        ModalService,
        FileReader
    ],
    entryComponents: [TypeWorkspaceComponent, AddPropertyComponent],
    exports: [TypeWorkspaceComponent]
})
export class TypeWorkspaceModule {
}
