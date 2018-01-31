import {NgModule} from "@angular/core";
import { CommonModule } from '@angular/common';
import {PluginFrameComponent} from "./plugin-frame.component";
import {LayoutModule} from "../../layout/layout.module";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";


@NgModule({
    declarations: [
        PluginFrameComponent
    ],
    imports: [
        CommonModule,
        LayoutModule,
        GlobalPipesModule
    ],
    entryComponents: [PluginFrameComponent],
    exports: [
        PluginFrameComponent
    ],
    providers: []
})
export class PluginFrameModule {

}
