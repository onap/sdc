import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PropertyTableModule } from 'app/components/logic/properties-table/property-table.module';
import { FormElementsModule } from 'app/components/ui/form-components/form-elements.module';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { TranslateModule } from 'app/shared/translator/translate.module';
import { ServiceConsumptionCreatorComponent } from './service-consumption-editor.component';

@NgModule({
    declarations: [
        ServiceConsumptionCreatorComponent
    ],
    imports: [CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule,
        PropertyTableModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [
        ServiceConsumptionCreatorComponent
    ],
    providers: []
})
export class ServiceConsumptionCreatorModule {
}
