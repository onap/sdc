import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {CompositionGraphComponent} from "./composition-graph.component";
import {ZoneModules} from "./canvas-zone/zones-module";
import {DndModule} from "ngx-drag-drop";
import { UiElementsModule } from "app/components/ui/ui-elements.module";
import {ServicePathSelectorModule} from "./service-path-selector/service-path-selector.module";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {CanvasSearchModule} from "./canvas-search/canvas-search.module";


@NgModule({
    declarations: [CompositionGraphComponent],
    imports: [CommonModule,
        ServicePathSelectorModule,
        SdcUiComponentsModule,
        UiElementsModule,
        ZoneModules,
        CanvasSearchModule,
        DndModule],
    exports: [CompositionGraphComponent],
    entryComponents: [CompositionGraphComponent],
})
export class CompositionGraphModule {
}