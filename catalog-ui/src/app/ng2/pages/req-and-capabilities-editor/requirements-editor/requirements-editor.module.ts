import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {RequirementsEditorComponent} from "./requirements-editor.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "../../../components/ui/form-components/form-elements.module";
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {SdcUiComponentsModule} from "sdc-ui/lib/angular/index";

@NgModule({
    declarations: [
        RequirementsEditorComponent
    ],
    imports: [CommonModule,
        FormsModule,
        FormElementsModule,
        TranslateModule,
        SdcUiComponentsModule
    ],
    exports: [],
    entryComponents: [
        RequirementsEditorComponent
    ],
    providers: []
})
export class RequirementsEditorModule {
}