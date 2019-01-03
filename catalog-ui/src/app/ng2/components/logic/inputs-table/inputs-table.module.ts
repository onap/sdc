import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {UiElementsModule} from "../../ui/ui-elements.module";
import {CommonModule} from "@angular/common";
import {HttpModule} from "@angular/http";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";
import {FilterChildInputsPipe} from "./pipes/filterChildInputs.pipe";
import {PropertiesService} from "../../../services/properties.service";
import {MultilineEllipsisModule} from "../../../shared/multiline-ellipsis/multiline-ellipsis.module";
import {DynamicInputComponent} from "./dynamic-input/dynamic-input.component";
import {InputsTableComponent} from "./inputs-table.component";

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
    DynamicInputComponent,
    FilterChildInputsPipe,
    InputsTableComponent
  ],
  exports: [InputsTableComponent],
  providers: [FilterChildInputsPipe, PropertiesService]
})
export class InputsTableModule {
}