import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { HomeComponent } from "./home.component";
import { LayoutModule } from "../../components/layout/layout.module";
import { UiElementsModule } from "../../components/ui/ui-elements.module";
import { GlobalPipesModule } from "../../pipes/global-pipes.module";
import { TranslateModule } from "../../shared/translator/translate.module";
import { SdcUiComponentsModule } from "onap-ui-angular";

@NgModule({
    declarations: [
        HomeComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        LayoutModule,
        UiElementsModule,
        GlobalPipesModule,
        TranslateModule
    ],
    exports: [
        HomeComponent
    ],
    entryComponents: [
        HomeComponent
    ],
    providers: []
})
export class HomeModule {
}
