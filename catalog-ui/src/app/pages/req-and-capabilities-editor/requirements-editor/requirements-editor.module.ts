import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from 'app/shared/translator/translate.module';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { FormElementsModule } from '../../../components/ui/form-components/form-elements.module';
import { RequirementsEditorComponent } from './requirements-editor.component';

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
