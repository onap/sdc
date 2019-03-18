import {NgModule} from "@angular/core";
import {PropertiesTableComponent} from "./properties-table.component";
import {DynamicPropertyComponent} from "./dynamic-property/dynamic-property.component";
import {FormsModule} from "@angular/forms";
import {UiElementsModule} from "../../ui/ui-elements.module";
import {CommonModule} from "@angular/common";
import {HttpModule} from "@angular/http";
import {FilterChildPropertiesPipe} from "./pipes/filterChildProperties.pipe";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";
import {PropertiesService} from "../../../services/properties.service";
import {MultilineEllipsisModule} from "../../../shared/multiline-ellipsis/multiline-ellipsis.module";

@NgModule({
    imports: [
        FormsModule,
        HttpModule,
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