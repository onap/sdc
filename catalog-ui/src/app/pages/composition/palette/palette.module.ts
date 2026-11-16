import { NgModule } from "@angular/core";
import { PaletteComponent } from "./palette.component";
import { SdcUiComponentsModule } from "onap-ui-angular";
import { GlobalPipesModule } from "app/pipes/global-pipes.module";
import { CommonModule } from "@angular/common";
import { DndModule } from "ngx-drag-drop";
import {PaletteElementComponent} from "./palette-element/palette-element.component";
import {UiElementsModule} from "app/components/ui/ui-elements.module";
import {GlossaryModule} from "app/shared/glossary/glossary.module";

@NgModule({
    declarations: [PaletteComponent, PaletteElementComponent],
    imports: [CommonModule, SdcUiComponentsModule, GlobalPipesModule, UiElementsModule, DndModule, GlossaryModule],
    exports: [PaletteComponent],
    entryComponents: [PaletteComponent],
})
export class PaletteModule {

    constructor() {

    }

}