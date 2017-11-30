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
import {HierarchyNavigationComponent} from "../../components/logic/hierarchy-navigtion/hierarchy-navigation.component";
import {HttpModule} from "@angular/http";
import {FormsModule} from "@angular/forms";
import {PropertyTableModule} from "../../components/logic/properties-table/property-table.module";
import {UiElementsModule} from "../../components/ui/ui-elements.module";
import {GlobalPipesModule} from "../../pipes/global-pipes.module";
import {BrowserModule} from "@angular/platform-browser";
import {FilterPropertiesAssignmentComponent} from "../../components/logic/filter-properties-assignment/filter-properties-assignment.component";
import {InputsTableComponent} from "../../components/logic/inputs-table/inputs-table.component";
import {PropertiesService} from "../../services/properties.service";
import {DataTypeService} from "../../services/data-type.service";
import {PropertiesAssignmentComponent} from "./properties-assignment.page.component";
import {HierarchyNavService} from "./services/hierarchy-nav.service";
import {PropertiesUtils} from "./services/properties.utils";
import {ComponentModeService} from "../../services/component-services/component-mode.service";

@NgModule({
    declarations: [
        PropertiesAssignmentComponent,
        InputsTableComponent,
        HierarchyNavigationComponent,
        FilterPropertiesAssignmentComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        GlobalPipesModule,
        PropertyTableModule,
        UiElementsModule],
    
    entryComponents: [PropertiesAssignmentComponent],
    exports: [
        PropertiesAssignmentComponent
    ],
    providers: [PropertiesService, HierarchyNavService, PropertiesUtils, DataTypeService, ComponentModeService]
})
export class PropertiesAssignmentModule {

}
