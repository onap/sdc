import { NgModule } from '@angular/core';
import { TranslateModule } from '../../shared/translator/translate.module';
import { PluginNotConnectedComponent } from './plugin-not-connected.component';

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
