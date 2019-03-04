import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {CapabilitiesEditorComponent} from "./capabilities-editor.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {SdcUiComponentsModule} from "sdc-ui/lib/angular/index";

@NgModule({
    declarations: [
        CapabilitiesEditorComponent
    ],
    imports: [CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule,
        TranslateModule,
        SdcUiComponentsModule
    ],
    exports: [],
    entryComponents: [
        CapabilitiesEditorComponent
    ],
    providers: []
})
export class CapabilitiesEditorModule {
}