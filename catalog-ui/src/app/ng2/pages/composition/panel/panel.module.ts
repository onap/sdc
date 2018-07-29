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
import {NgModule} from "@angular/core";
import {HttpModule} from "@angular/http";
import {FormsModule} from "@angular/forms";
import {BrowserModule} from "@angular/platform-browser";
import {CompositionPanelComponent} from "./panel.component";
import {CompositionPanelHeaderModule} from "app/ng2/pages/composition/panel/panel-header/panel-header.module";
import {GroupTabsModule} from "./panel-tabs/groups/group-tabs.module";
import {PolicyTabsModule} from "./panel-tabs/policies/policy-tabs.module";
import {SdcUiComponents} from "sdc-ui/lib/angular";
import {UiElementsModule} from 'app/ng2/components/ui/ui-elements.module';
import {AddElementsModule} from "../../../components/ui/modal/add-elements/add-elements.module";

@NgModule({
    declarations: [
        CompositionPanelComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        CompositionPanelHeaderModule,
        PolicyTabsModule,
        GroupTabsModule,
        UiElementsModule,
        AddElementsModule
    ],
    entryComponents: [
        CompositionPanelComponent
    ],
    exports: [],
    providers: [SdcUiComponents.ModalService]
})
export class CompositionPanelModule {

}
