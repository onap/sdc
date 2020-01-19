import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { CatalogComponent } from "./catalog.component";
import { LayoutModule } from "../../components/layout/layout.module";
import { UiElementsModule } from "../../components/ui/ui-elements.module";
import { GlobalPipesModule } from "../../pipes/global-pipes.module";
import { TranslateModule } from "../../shared/translator/translate.module";
import { SdcUiComponentsModule } from "onap-ui-angular";
import {SdcTileModule} from "../../components/ui/tile/sdc-tile.module";

@NgModule({
    declarations: [
        CatalogComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        LayoutModule,
        UiElementsModule,
        GlobalPipesModule,
        TranslateModule,
        SdcTileModule
    ],
    exports: [
        CatalogComponent
    ],
    entryComponents: [
        CatalogComponent
    ],
    providers: []
})
export class CatalogModule {
}
