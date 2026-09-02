import { NgModule } from "@angular/core";
import {PluginNotConnectedComponent} from "./plugin-not-connected.component";
import {TranslateModule} from "../../shared/translator/translate.module";

@NgModule({
    declarations: [
        PluginNotConnectedComponent
    ],
    imports: [TranslateModule],
    exports: [PluginNotConnectedComponent],
    entryComponents: [
        PluginNotConnectedComponent
    ]
})
export class PluginNotConnectedModule {

}

