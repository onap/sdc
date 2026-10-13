import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormElementsModule } from 'app/components/ui/form-components/form-elements.module';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { TranslateModule } from 'app/shared/translator/translate.module';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { CapabilitiesEditorComponent } from './capabilities-editor.component';

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
