/**
 * Created by ob0695 on 6/4/2018.
 */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxsModule } from '@ngxs/store';
import { CompositionPanelModule } from 'app/pages/composition/panel/composition-panel.module';
import { DndModule } from 'ngx-drag-drop';
import { GraphState } from './common/store/graph.state';
import { CompositionPageComponent } from './composition-page.component';
import { CompositionService } from './composition.service';
import { CompositionGraphModule } from './graph/composition-graph.module';
import { PaletteModule } from './palette/palette.module';

@NgModule({
    declarations: [CompositionPageComponent],
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
