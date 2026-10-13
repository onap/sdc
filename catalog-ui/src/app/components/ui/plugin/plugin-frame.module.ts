import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { PluginNotConnectedModule } from '../../../pages/plugin-not-connected/plugin-not-connected-module';
import { GlobalPipesModule } from '../../../pipes/global-pipes.module';
import { LayoutModule } from '../../layout/layout.module';
import { UiElementsModule } from '../ui-elements.module';
import { PluginFrameComponent } from './plugin-frame.component';

@NgModule({
    declarations: [
        PluginFrameComponent
    ],
    imports: [
        CommonModule,
        LayoutModule,
        PluginNotConnectedModule,
        GlobalPipesModule,
        UiElementsModule
    ],
    entryComponents: [PluginFrameComponent],
    exports: [
        PluginFrameComponent
    ],
    providers: []
})
export class PluginFrameModule {

}
