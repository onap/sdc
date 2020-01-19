import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {UiElementsModule} from "../../../components/ui/ui-elements.module";
import {ArtifactFormModule} from "../../../components/forms/artifacts-form/artifact-form.module";
import {ArtifactsService} from "../../../components/forms/artifacts-form/artifacts.service";
import {DeploymentArtifactsPageComponent} from "./deployment-artifacts-page.component";
import {TranslatePipe} from "../../../shared/translator/translate.pipe";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {GenericArtifactBrowserModule} from "../../../components/logic/generic-artifact-browser/generic-artifact-browser.module";

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
    providers:[ArtifactsService]
})
export class DeploymentArtifactsPageModule {
}
