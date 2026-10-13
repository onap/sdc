import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { GlobalPipesModule } from 'app/pipes/global-pipes.module';
import { EventListenerService } from 'app/services/event-listener.service';
import { DndModule } from 'ngx-drag-drop';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { PaletteElementComponent } from './palette-element/palette-element.component';
import { PaletteComponent } from './palette.component';
import { CompositionPaletteService } from './services/palette.service';

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
