import { NgModule } from "@angular/core";
import { CommonModule } from '@angular/common';
import { SdcUiComponentsModule } from "sdc-ui/lib/angular";
import { ValueEditComponent } from './value-edit/value-edit.component';
import { UnsavedChangesComponent } from "./unsaved-changes/unsaved-changes.component";
import { UiElementsModule } from "../ui-elements.module";



@NgModule({
    declarations: [
        ValueEditComponent,
        UnsavedChangesComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        UiElementsModule
    ],
    exports: [ValueEditComponent, UnsavedChangesComponent],
    entryComponents: [ UnsavedChangesComponent
    ],
    providers: []
})
export class ModalFormsModule {

}