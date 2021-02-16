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

import {NgModule} from "@angular/core";
import {AttributesTableComponent} from "./attributes-table.component";
import {DynamicAttributeComponent} from "./dynamic-attribute/dynamic-attribute.component";
import {FormsModule} from "@angular/forms";
import {UiElementsModule} from "../../ui/ui-elements.module";
import {CommonModule} from "@angular/common";
import {FilterChildAttributesPipe} from "./pipes/filterChildAttributes.pipe";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";
import {MultilineEllipsisModule} from "../../../shared/multiline-ellipsis/multiline-ellipsis.module";
import {AttributesService} from "../../../services/attributes.service";

@NgModule({
  imports: [
    FormsModule,
    CommonModule,
    GlobalPipesModule,
    UiElementsModule,
    MultilineEllipsisModule
  ],
  declarations: [
    FilterChildAttributesPipe,
    DynamicAttributeComponent,
    AttributesTableComponent
  ],
  exports: [AttributesTableComponent, DynamicAttributeComponent],
  providers: [FilterChildAttributesPipe, AttributesService]
})
export class AttributeTableModule {
}