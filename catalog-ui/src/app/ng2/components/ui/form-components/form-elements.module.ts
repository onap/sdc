/**
 * Created by rc2122 on 9/5/2017.
 */
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {UiElementPopoverInputComponent} from "./popover-input/ui-element-popover-input.component";
import {UiElementIntegerInputComponent} from "./integer-input/ui-element-integer-input.component";
import {UiElementInputComponent} from "./input/ui-element-input.component";
import {UiElementDropDownComponent} from "./dropdown/ui-element-dropdown.component";
import {UiElementBase} from "./ui-element-base.component";
import {CheckboxModule} from "./checkbox/checkbox.module";
import {RadioButtonComponent} from "./radio-buttons/radio-buttons.component";
import {PopoverModule} from "../popover/popover.module";
import {TooltipModule} from "../tooltip/tooltip.module";


@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        PopoverModule,
        ReactiveFormsModule,
        TooltipModule,
        CheckboxModule],

    declarations: [UiElementDropDownComponent,
        UiElementInputComponent,
        UiElementIntegerInputComponent,
        UiElementPopoverInputComponent,
        UiElementBase,
        RadioButtonComponent],

    exports: [UiElementDropDownComponent,
        UiElementInputComponent,
        UiElementIntegerInputComponent,
        UiElementPopoverInputComponent,
        RadioButtonComponent,
        TooltipModule,
        CheckboxModule]
})
export class FormElementsModule { }