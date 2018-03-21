import {NgModule} from "@angular/core";
import { CommonModule } from '@angular/common';
import {PluginFrameComponent} from "./plugin-frame.component";
import {LayoutModule} from "../../layout/layout.module";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";
import {UiElementsModule} from "../ui-elements.module";
import {PluginNotConnectedModule} from "../../../pages/plugin-not-connected/plugin-not-connected-module";


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
