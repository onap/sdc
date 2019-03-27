/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
import {GenericArtifactBrowserComponent} from "./generic-artifact-browser.component";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {GabService} from "../../../services/gab.service";
import {TopNavComponent} from "../../layout/top-nav/top-nav.component";

@NgModule({
    declarations: [
        GenericArtifactBrowserComponent
    ],
    imports: [
        NgxDatatableModule
    ],
    entryComponents: [ //need to add anything that will be dynamically created
        GenericArtifactBrowserComponent
    ],
    exports: [],
    providers: [GabService]
})
export class GenericArtifactBrowserModule {
}