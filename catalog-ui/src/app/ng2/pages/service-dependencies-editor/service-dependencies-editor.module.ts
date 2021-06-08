import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormElementsModule } from 'app/ng2/components/ui/form-components/form-elements.module';
import { UiElementsModule } from 'app/ng2/components/ui/ui-elements.module';
import { ServiceDependenciesEditorComponent } from './service-dependencies-editor.component';
import { PropertyTableModule } from 'app/ng2/components/logic/properties-table/property-table.module';

@NgModule({
    declarations: [
        ServiceDependenciesEditorComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule,
        PropertyTableModule
    ],
    exports: [],
    entryComponents: [
        ServiceDependenciesEditorComponent
    ],
    providers: []
})
export class ServiceDependenciesEditorModule {
}
