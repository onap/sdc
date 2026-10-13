import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GlobalPipesModule } from 'app/pipes/global-pipes.module';
import { FormElementsModule } from '../../ui/form-components/form-elements.module';
import { UiElementsModule } from '../../ui/ui-elements.module';
import { PropertyTableModule } from '../properties-table/property-table.module';
import { SelectRequirementOrCapabilityComponent } from './select-requirement-or-capability.component';

@NgModule({
    declarations: [
        SelectRequirementOrCapabilityComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        GlobalPipesModule,
        UiElementsModule,
        PropertyTableModule
    ],

    exports: [SelectRequirementOrCapabilityComponent],
    providers: []
})
export class SelectRequirementOrCapabilityModule {
}
