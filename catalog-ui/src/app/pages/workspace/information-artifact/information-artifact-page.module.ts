import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {UiElementsModule} from "../../../components/ui/ui-elements.module";
import {InformationArtifactPageComponent} from "./information-artifact-page.component";
import {ArtifactFormModule} from "../../../components/forms/artifacts-form/artifact-form.module";
import {ArtifactsService} from "../../../components/forms/artifacts-form/artifacts.service";

@NgModule({
    declarations: [
        InformationArtifactPageComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        NgxDatatableModule,
        UiElementsModule,
        ArtifactFormModule
    ],
    exports: [
        InformationArtifactPageComponent
    ],
    entryComponents: [
        InformationArtifactPageComponent
    ],
    providers:[ArtifactsService]
})
export class InformationArtifactPageModule {
}
