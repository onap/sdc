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

@NgModule({
    imports: [
        FormsModule,
        HttpModule,
        CommonModule,
        GlobalPipesModule,
        UiElementsModule
        ],
    declarations: [
        FilterChildPropertiesPipe,
        DynamicPropertyComponent,
        PropertiesTableComponent
    ],
    exports: [PropertiesTableComponent],
    providers: [FilterChildPropertiesPipe, PropertiesService]
})
export class PropertyTableModule {
}