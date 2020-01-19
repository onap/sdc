import {NgModule} from "@angular/core";
import {PluginContextViewPageComponent} from "./plugin-context-view/plugin-context-view.page.component";
import {PluginFrameModule} from "../../components/ui/plugin/plugin-frame.module";
import {CommonModule} from "@angular/common";
import {UiElementsModule} from "../../components/ui/ui-elements.module";
import {PluginTabViewPageComponent} from "./plugin-tab-view/plugin-tab-view.page.component";
import {LayoutModule} from "../../components/layout/layout.module";
import {HttpModule} from "@angular/http";

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

