import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "onap-ui-angular";
import { DatatableA11yModule } from '../../../components/ui/datatable-a11y/datatable-a11y.module';
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {ToscaArtifactPageComponent} from "./tosca-artifact-page.component";
import {UiElementsModule} from "../../../components/ui/ui-elements.module";

@NgModule({
    declarations: [
        ToscaArtifactPageComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        NgxDatatableModule,
        DatatableA11yModule,
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
