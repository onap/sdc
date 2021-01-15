/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {SdcUiComponentsModule} from 'onap-ui-angular';
import {GlobalPipesModule} from "../../pipes/global-pipes.module";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from "../../shared/translator/translate.module";
import {TopologyTemplateService} from "../../services/component-services/topology-template.service";
import {AttributesOutputsComponent} from "./attributes-outputs.page.component";
import {TabModule} from "../../components/ui/tabs/tabs.module";
import {UiElementsModule} from "../../components/ui/ui-elements.module"
import {HierarchyNavigationModule} from "../../components/logic/hierarchy-navigtion/hierarchy-navigation.module";
import {AttributesService} from "../../services/attributes.service";
import {HierarchyNavService} from "./services/hierarchy-nav.service";
import {AttributesUtils} from "./services/attributes.utils";
import {OutputsUtils} from "./services/outputs.utils";
import { OutputsTableComponent } from "app/ng2/components/logic/outputs-table/outputs-table.component";
import {AttributeTableModule} from "../../components/logic/attributes-table/attribute-table.module";

@NgModule({
  declarations: [
    AttributesOutputsComponent,
    OutputsTableComponent
  ],
  imports: [
    CommonModule,
    SdcUiComponentsModule,
    GlobalPipesModule,
    NgxDatatableModule,
    TabModule,
    HierarchyNavigationModule,
    UiElementsModule,
    TranslateModule,
    AttributeTableModule
  ],
  exports: [
    AttributesOutputsComponent
  ],
  entryComponents: [
    AttributesOutputsComponent
  ],
  providers: [TopologyTemplateService, AttributesService, HierarchyNavService, AttributesUtils, OutputsUtils]
})

export class AttributesOutputsModule {
}
