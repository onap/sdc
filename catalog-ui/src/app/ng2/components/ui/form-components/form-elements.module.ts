/**
 * Created by rc2122 on 9/5/2017.
 */
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { SdcUiComponentsModule } from 'onap-ui-angular/dist';
import { PopoverModule } from '../popover/popover.module';
import { TooltipModule } from '../tooltip/tooltip.module';
import { CheckboxModule } from './checkbox/checkbox.module';
import { UiElementDropDownComponent } from './dropdown/ui-element-dropdown.component';
import { UiElementInputComponent } from './input/ui-element-input.component';
import { UiElementIntegerInputComponent } from './integer-input/ui-element-integer-input.component';
import { UiElementPopoverInputComponent } from './popover-input/ui-element-popover-input.component';
import { RadioButtonComponent } from './radio-buttons/radio-buttons.component';
import { UiElementBase } from './ui-element-base.component';

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        PopoverModule,
        ReactiveFormsModule,
        TooltipModule,
        CheckboxModule,
        SdcUiComponentsModule],

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
