import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GlobalPipesModule } from '../../../pipes/global-pipes.module';
import { PropertiesService } from '../../../services/properties.service';
import { MultilineEllipsisModule } from '../../../shared/multiline-ellipsis/multiline-ellipsis.module';
import { UiElementsModule } from '../../ui/ui-elements.module';
import { DynamicPropertyComponent } from './dynamic-property/dynamic-property.component';
import { FilterChildPropertiesPipe } from './pipes/filter-child-properties.pipe';
import { PropertiesTableComponent } from './properties-table.component';

@NgModule({
    imports: [
        FormsModule,
        CommonModule,
        GlobalPipesModule,
        UiElementsModule,
        MultilineEllipsisModule
        ],
    declarations: [
        FilterChildPropertiesPipe,
        DynamicPropertyComponent,
        PropertiesTableComponent
    ],
    exports: [PropertiesTableComponent, DynamicPropertyComponent],
    providers: [FilterChildPropertiesPipe, PropertiesService]
})
export class PropertyTableModule {
}
