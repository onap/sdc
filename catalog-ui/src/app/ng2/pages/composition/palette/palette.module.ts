import { NgModule } from "@angular/core";
import { CompositionPaletteService } from "./services/palette.service";
import { PaletteComponent } from "./palette.component";
import { SdcUiComponentsModule } from "onap-ui-angular";
import { GlobalPipesModule } from "app/ng2/pipes/global-pipes.module";
import { CommonModule } from "@angular/common";
import { DndModule } from "ngx-drag-drop";
import {PaletteElementComponent} from "./palette-element/palette-element.component";
import {EventListenerService} from "app/services/event-listener-service";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";

@NgModule({
    declarations: [PaletteComponent, PaletteElementComponent],
    imports: [CommonModule, SdcUiComponentsModule, GlobalPipesModule, UiElementsModule, DndModule],
    exports: [PaletteComponent],
    entryComponents: [PaletteComponent],
    providers: [CompositionPaletteService, EventListenerService]
})
export class PaletteModule {

    constructor() {

    }

}