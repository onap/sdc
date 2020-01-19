import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "onap-ui-angular";

import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import { ReqAndCapabilitiesComponent } from "./req-and-capabilities.component";
import { CommonModule } from "@angular/common";

import {RequirmentsComponent } from "./requirements/requirments.components";
import { CapabilitiesComponent } from "./capabilities/capabilities.component";
import { CapabilitiesPropertiesComponent } from "./capabilities/capabilities-properties/capabilities-properties";
import {ReqAndCapabilitiesService} from "./req-and-capabilities.service";
import {RequirementsEditorComponent} from "./requirements/requirementEditor/requirements-editor.component";
import {CapabilitiesEditorComponent} from "./capabilities/capabilityEditor/capabilities-editor.component";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {ToscaTypesServiceNg2} from "../../../services/tosca-types.service";

@NgModule({
    declarations: [
        ReqAndCapabilitiesComponent,
        CapabilitiesComponent,
        RequirmentsComponent,
        CapabilitiesPropertiesComponent,
        RequirementsEditorComponent,
        CapabilitiesEditorComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        NgxDatatableModule,
        TranslateModule
    ],
    exports: [
        ReqAndCapabilitiesComponent,
        CapabilitiesComponent,
        RequirmentsComponent,
        CapabilitiesPropertiesComponent
    ],
    entryComponents: [
        ReqAndCapabilitiesComponent,
        CapabilitiesComponent,
        RequirmentsComponent,
        CapabilitiesPropertiesComponent,
        RequirementsEditorComponent,
        CapabilitiesEditorComponent
    ],
    providers: [  ReqAndCapabilitiesService]
})
export class reqAndCapabilitiesModule {
}