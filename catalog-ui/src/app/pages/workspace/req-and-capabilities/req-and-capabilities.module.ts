import { NgModule } from '@angular/core';
import { SdcUiComponentsModule } from 'onap-ui-angular';

import { CommonModule } from '@angular/common';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { ReqAndCapabilitiesComponent } from './req-and-capabilities.component';

import { ToscaTypesService } from '../../../services/tosca-types.service';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { CapabilitiesEditorComponent } from './capabilities/capabilities-editor/capabilities-editor.component';
import { CapabilitiesPropertiesComponent } from './capabilities/capabilities-properties/capabilities-properties';
import { CapabilitiesComponent } from './capabilities/capabilities.component';
import { ReqAndCapabilitiesService } from './req-and-capabilities.service';
import { RequirementsEditorComponent } from './requirements/requirements-editor/requirements-editor.component';
import { RequirementsComponent } from './requirements/requirements.component';

@NgModule({
    declarations: [
        ReqAndCapabilitiesComponent,
        CapabilitiesComponent,
        RequirementsComponent,
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
        RequirementsComponent,
        CapabilitiesPropertiesComponent
    ],
    entryComponents: [
        ReqAndCapabilitiesComponent,
        CapabilitiesComponent,
        RequirementsComponent,
        CapabilitiesPropertiesComponent,
        RequirementsEditorComponent,
        CapabilitiesEditorComponent
    ],
    providers: [  ReqAndCapabilitiesService]
})
export class reqAndCapabilitiesModule {
}
