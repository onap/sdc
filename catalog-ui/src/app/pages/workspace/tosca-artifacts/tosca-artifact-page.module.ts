import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { UiElementsModule } from '../../../components/ui/ui-elements.module';
import { GlobalPipesModule } from '../../../pipes/global-pipes.module';
import { ToscaArtifactPageComponent } from './tosca-artifact-page.component';

@NgModule({
    declarations: [
        ToscaArtifactPageComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        NgxDatatableModule,
        UiElementsModule
    ],
    exports: [
        ToscaArtifactPageComponent
    ],
    entryComponents: [
        ToscaArtifactPageComponent
    ],

})
export class ToscaArtifactPageModule {
}
