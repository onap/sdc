import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { LayoutModule } from '../../components/layout/layout.module';
import { PluginFrameModule } from '../../components/ui/plugin/plugin-frame.module';
import { UiElementsModule } from '../../components/ui/ui-elements.module';
import { PluginContextViewPageComponent } from './plugin-context-view/plugin-context-view.page.component';
import { PluginTabViewPageComponent } from './plugin-tab-view/plugin-tab-view.page.component';

@NgModule({
    declarations: [
        PluginContextViewPageComponent,
        PluginTabViewPageComponent
    ],
    imports: [
        CommonModule,
        PluginFrameModule,
        UiElementsModule,
        LayoutModule,
        HttpModule
    ],
    exports: [
        PluginContextViewPageComponent,
        PluginTabViewPageComponent
    ],
    entryComponents: [
        PluginContextViewPageComponent,
        PluginTabViewPageComponent
    ]
})
export class PluginsModule {

}
