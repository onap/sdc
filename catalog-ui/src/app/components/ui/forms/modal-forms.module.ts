import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { UiElementsModule } from '../ui-elements.module';
import { UnsavedChangesComponent } from './unsaved-changes/unsaved-changes.component';

@NgModule({
    declarations: [
        UnsavedChangesComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        UiElementsModule
    ],
    exports: [UnsavedChangesComponent],
    entryComponents: [ UnsavedChangesComponent
    ],
    providers: []
})
export class ModalFormsModule {

}
