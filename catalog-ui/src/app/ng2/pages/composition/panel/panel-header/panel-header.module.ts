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
import { FormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { CompositionPanelHeaderComponent } from "./panel-header.component";
import { UiElementsModule } from './../../../../components/ui/ui-elements.module';
import { SdcUiComponentsModule } from "onap-ui-angular";
import { EditNameModalComponent } from "app/ng2/pages/composition/panel/panel-header/edit-name-modal/edit-name-modal.component";

@NgModule({
    declarations: [
        CompositionPanelHeaderComponent,
        EditNameModalComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        UiElementsModule,
        SdcUiComponentsModule
    ],
    entryComponents: [
        CompositionPanelHeaderComponent, EditNameModalComponent
    ],
    exports: [
        CompositionPanelHeaderComponent
    ],
})
export class CompositionPanelHeaderModule {

}
