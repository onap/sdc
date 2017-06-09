import { NgModule } from "@angular/core";
import { UiElementCheckBoxComponent } from './elements-ui/checkbox/ui-element-checkbox.component';
import { UiElementDropDownComponent } from './elements-ui/dropdown/ui-element-dropdown.component';
import { UiElementInputComponent } from './elements-ui/input/ui-element-input.component';
import { DynamicElementComponent } from "app/ng2/components/dynamic-element/dynamic-element.component";
import { BrowserModule } from '@angular/platform-browser'
import { FormsModule, ReactiveFormsModule } from '@angular/forms'
import { UiElementPopoverInputComponent } from "./elements-ui/popover-input/ui-element-popover-input.component";
import {PopoverModule} from "../popover/popover.module";
import {TooltipModule} from "../tooltip/tooltip.module";

@NgModule({
    declarations: [
        DynamicElementComponent,
        UiElementInputComponent,
        UiElementCheckBoxComponent,
        UiElementDropDownComponent,
        UiElementPopoverInputComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        PopoverModule,
        ReactiveFormsModule,
        TooltipModule
    ],
    exports: [
        DynamicElementComponent
    ],
    providers: []
})
export class DynamicElementModule {

}
