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
import { HttpModule } from "@angular/http";
import { FormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { UiElementsModule } from 'app/ng2/components/ui/ui-elements.module';
import { ExpandCollapseComponent } from 'app/ng2/components/ui/expand-collapse/expand-collapse.component';
import { PoliciesService } from "../../../../../services/policies.service";
import { GroupInformationTabComponent } from './group-information-tab.component';
import { TooltipModule } from './../../../../../components/ui/tooltip/tooltip.module';
import { GroupTabsComponent } from "./group-tabs.component";
import { SdcUiComponentsModule } from "sdc-ui/lib/angular";
import { GroupMembersTabComponent } from './group-members-tab.component';
import { TranslateModule } from './../../../../../shared/translator/translate.module';
import { GroupPropertiesTabComponent } from "./group-properties-tab.component";

@NgModule({
    declarations: [
        GroupInformationTabComponent,
        GroupMembersTabComponent,
        GroupTabsComponent,
        GroupPropertiesTabComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        TooltipModule,
        UiElementsModule,
        SdcUiComponentsModule,
        TranslateModule
    ],
    entryComponents: [
        GroupInformationTabComponent,
        GroupMembersTabComponent,
        GroupTabsComponent,
        GroupPropertiesTabComponent,
        ExpandCollapseComponent
    ],
    exports: [
        TooltipModule,
        GroupInformationTabComponent,
        GroupMembersTabComponent,
        GroupTabsComponent,
        GroupPropertiesTabComponent
    ],
    providers: [
        PoliciesService
    ]
})
export class GroupTabsModule {

}
