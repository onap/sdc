import { NgModule } from "@angular/core";
import { CommonModule } from '@angular/common';
import { SdcUiComponentsModule } from "onap-ui-angular";
import { UnsavedChangesComponent } from "./unsaved-changes/unsaved-changes.component";
import { UiElementsModule } from "../ui-elements.module";

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