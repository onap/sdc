/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { AutoCompleteModule } from 'onap-ui-angular/dist/autocomplete/autocomplete.module';
import { SvgIconModule } from 'onap-ui-angular/dist/svg-icon/svg-icon.module';
import { LayoutModule } from '../../components/layout/layout.module';
import { UiElementsModule } from '../../components/ui/ui-elements.module';
import { CacheService } from '../../services/cache.service';
import { DataTypeService } from '../../services/data-type.service';
import { ModalService } from '../../services/modal.service';
import { TranslateModule } from '../../shared/translator/translate.module';
import { InterfaceOperationHandlerModule } from '../composition/interface-operations/operation-creator/interface-operation-handler.module';
import { ConstraintsModule } from '../properties-assignment/constraints/constraints.module';
import { TypeWorkspaceGeneralComponent } from './type-workspace-general/type-workspace-general.component';
import { AddPropertyComponent } from './type-workspace-properties/add-property/add-property.component';
import { TypeWorkspacePropertiesComponent } from './type-workspace-properties/type-workspace-properties.component';
import { TypeWorkspaceToscaArtifactPageComponent } from './type-workspace-tosca-artifacts/type-workspace-tosca-artifact-page.component';
import { TypeWorkspaceComponent } from './type-workspace.component';
import { TypeWorkspaceService } from './type-workspace.service';
import { WorkspaceMenuComponent } from './workspace-menu/workspace-menu.component';

@NgModule({
   imports: [
        CommonModule,
        UiElementsModule,
        LayoutModule,
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
        TypeWorkspaceService
    ],
    entryComponents: [TypeWorkspaceComponent, AddPropertyComponent],
    exports: [TypeWorkspaceComponent]
})
export class TypeWorkspaceModule {
}
