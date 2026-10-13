import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { ArtifactFormModule } from '../../../components/forms/artifacts-form/artifact-form.module';
import { ArtifactsService } from '../../../components/forms/artifacts-form/artifacts.service';
import { GenericArtifactBrowserModule } from '../../../components/logic/generic-artifact-browser/generic-artifact-browser.module';
import { UiElementsModule } from '../../../components/ui/ui-elements.module';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { TranslatePipe } from '../../../shared/translator/translate.pipe';
import { DeploymentArtifactsPageComponent } from './deployment-artifacts-page.component';

@NgModule({
    declarations: [
        DeploymentArtifactsPageComponent
    ],
    imports: [
        TranslateModule,
        CommonModule,
        SdcUiComponentsModule,
        NgxDatatableModule,
        UiElementsModule,
        ArtifactFormModule,
        GenericArtifactBrowserModule
    ],
    exports: [
        DeploymentArtifactsPageComponent
    ],
    entryComponents: [
        DeploymentArtifactsPageComponent
    ],
    providers: [ArtifactsService]
})
export class DeploymentArtifactsPageModule {
}
