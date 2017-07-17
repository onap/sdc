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
import { PropertiesAssignmentComponent } from "./properties-assignment.page.component";
import { HierarchyNavigationComponent } from "./../../components/hierarchy-navigtion/hierarchy-navigation.component";
import { BrowserModule } from "@angular/platform-browser";
import { FormsModule } from "@angular/forms";
import { HttpModule } from "@angular/http";
import { TabModule } from '../../shared/tabs/tabs.module';
import { CheckboxModule} from '../../shared/checkbox/checkbox.module';
import { PropertiesTableComponent } from '../../components/properties-table/properties-table.component';
import { InputsTableComponent } from '../../components/inputs-table/inputs-table.component';
import { ContentAfterLastDotPipe } from "../../pipes/contentAfterLastDot.pipe";
import { SearchFilterPipe } from "../../pipes/searchFilter.pipe";
import { FilterChildPropertiesPipe } from "../../pipes/filterChildProperties.pipe";
import { DataTypeService } from './../../services/data-type.service';
import { PropertiesService } from './../../services/properties.service';
import { HierarchyNavService } from './../../services/hierarchy-nav.service';
import { PropertiesUtils } from './properties.utils';
import { DynamicElementModule } from 'app/ng2/components/dynamic-element/dynamic-element.module';
import { DynamicPropertyComponent } from './../../components/properties-table/dynamic-property/dynamic-property.component';
import { PopoverModule } from "../../components/popover/popover.module";
import { ModalModule } from "../../components/modal/modal.module";
import { FilterPropertiesAssignmentComponent } from "./../../components/filter-properties-assignment/filter-properties-assignment.component";
import { GroupByPipe } from 'app/ng2/pipes/groupBy.pipe';
import { KeysPipe } from 'app/ng2/pipes/keys.pipe';
import {TooltipModule} from "../../components/tooltip/tooltip.module";
import { ComponentModeService } from "app/ng2/services/component-mode.service"
import {LoaderComponent} from "app/ng2/components/loader/loader.component"
import {HttpInterceptor} from "../../services/http.interceptor.service";

@NgModule({
    declarations: [
        PropertiesAssignmentComponent,
        PropertiesTableComponent,
        InputsTableComponent,
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SearchFilterPipe,
        FilterChildPropertiesPipe,
        HierarchyNavigationComponent,
        DynamicPropertyComponent,
        // PopoverContentComponent,
        // PopoverComponent,
        FilterPropertiesAssignmentComponent,
        LoaderComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        TabModule,
        CheckboxModule,
        DynamicElementModule,
        PopoverModule,
        TooltipModule,
        ModalModule
    ],
    entryComponents: [PropertiesAssignmentComponent],
    exports: [
        PropertiesAssignmentComponent
        // PopoverContentComponent,
        // PopoverComponent
    ],
    providers: [PropertiesService, HierarchyNavService, PropertiesUtils, DataTypeService,HttpInterceptor, ContentAfterLastDotPipe, GroupByPipe, KeysPipe, ComponentModeService]
})
export class PropertiesAssignmentModule {

}
