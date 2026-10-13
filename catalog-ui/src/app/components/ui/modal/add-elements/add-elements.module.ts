/**
 * Created by ob0695 on 11.04.2018.
 */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { AddElementsComponent } from './add-elements.component';

@NgModule({
    declarations: [
        AddElementsComponent
    ],

    imports: [
        CommonModule,
        SdcUiComponentsModule
    ],

    entryComponents: [
        AddElementsComponent
    ],
    exports: [],
    providers: []
})
export class AddElementsModule {

}
