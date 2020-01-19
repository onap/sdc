/**
 * Created by ob0695 on 6/4/2018.
 */
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {CompositionGraphModule} from "./graph/composition-graph.module";
import {CompositionPageComponent} from "./composition-page.component";
import {NgxsModule} from "@ngxs/store";
import {PaletteModule} from "./palette/palette.module";
import {PalettePopupPanelComponent} from "./palette/palette-popup-panel/palette-popup-panel.component";
import { CompositionPanelModule } from "app/ng2/pages/composition/panel/composition-panel.module";
import {CompositionService} from "./composition.service";
import {DndModule} from "ngx-drag-drop";
import {GraphState} from "./common/store/graph.state";

@NgModule({
    declarations: [CompositionPageComponent, PalettePopupPanelComponent],
    imports: [CommonModule,
        CompositionGraphModule,
        CompositionPanelModule,
        PaletteModule,
        DndModule,
        NgxsModule.forFeature([
            GraphState])],
    exports: [CompositionPageComponent],
    entryComponents: [CompositionPageComponent],
    providers: [CompositionService]
})
export class CompositionPageModule {
}