import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormElementsModule } from 'app/components/ui/form-components/form-elements.module';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { ServiceDependenciesEditorComponent } from './service-dependencies-editor.component';
import { PropertyTableModule } from 'app/components/logic/properties-table/property-table.module';
import {TranslateModule} from "../../shared/translator/translate.module";
import {ToscaFunctionModule} from "../properties-assignment/tosca-function/tosca-function.module";

@NgModule({
    declarations: [
        ServiceDependenciesEditorComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule,
        PropertyTableModule,
        TranslateModule,
        ToscaFunctionModule
    ],
    exports: [],
    entryComponents: [
        ServiceDependenciesEditorComponent
    ],
    providers: []
})
export class ServiceDependenciesEditorModule {
}
